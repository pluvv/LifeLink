package com.lifelink.servlet;

import com.lifelink.model.User;
import com.lifelink.util.DB;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/bank/requests")
public class BankRequestsServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        User user=(User)req.getSession(false).getAttribute("user");
        if(!"BLOOD_BANK".equals(user.getRole())){res.sendError(403);return;}
        String sql="SELECT r.id,r.blood_group,r.units,r.status,r.created_at,u.full_name,u.email FROM blood_requests r JOIN users u ON u.id=r.recipient_user_id WHERE r.bank_user_id=? ORDER BY r.created_at DESC";
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,user.getId());try(ResultSet r=p.executeQuery()){
                List<Map<String,Object>> list=new ArrayList<>();while(r.next()){
                    Map<String,Object> m=new HashMap<>();m.put("id",r.getLong("id"));m.put("group",r.getString("blood_group"));m.put("units",r.getInt("units"));m.put("status",r.getString("status"));m.put("created",r.getTimestamp("created_at"));m.put("recipient",r.getString("full_name"));m.put("email",r.getString("email"));list.add(m);
                }req.setAttribute("requests",list);
            }
        }catch(SQLException e){getServletContext().log("Could not load blood requests",e);res.sendError(500);return;}
        req.getRequestDispatcher("/WEB-INF/views/bank-requests.jsp").forward(req,res);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
        User user=(User)req.getSession(false).getAttribute("user");
        if(!"BLOOD_BANK".equals(user.getRole())){res.sendError(403);return;}
        String raw=req.getParameter("requestId"),action=req.getParameter("action");
        if(raw==null||!raw.matches("\\d{1,18}")||!Arrays.asList("approve","reject","fulfill").contains(action)){res.sendRedirect(req.getContextPath()+"/bank/requests?error=invalid");return;}
        long id=Long.parseLong(raw);
        try(Connection c=DB.getConnection()){
            c.setAutoCommit(false);
            try{
                String status,group;int units;
                try(PreparedStatement lock=c.prepareStatement("SELECT status,blood_group,units FROM blood_requests WHERE id=? AND bank_user_id=? FOR UPDATE")){
                    lock.setLong(1,id);lock.setLong(2,user.getId());try(ResultSet r=lock.executeQuery()){
                        if(!r.next()){c.rollback();res.sendRedirect(req.getContextPath()+"/bank/requests?error=missing");return;}
                        status=r.getString("status");group=r.getString("blood_group");units=r.getInt("units");
                    }
                }
                int changed=0;
                if("approve".equals(action)&&"PENDING".equals(status)){
                    try(PreparedStatement p=c.prepareStatement("UPDATE blood_requests SET status='APPROVED' WHERE id=? AND bank_user_id=? AND status='PENDING'")){p.setLong(1,id);p.setLong(2,user.getId());changed=p.executeUpdate();}
                }else if("reject".equals(action)&&"PENDING".equals(status)){
                    try(PreparedStatement p=c.prepareStatement("UPDATE blood_requests SET status='REJECTED' WHERE id=? AND bank_user_id=? AND status='PENDING'")){p.setLong(1,id);p.setLong(2,user.getId());changed=p.executeUpdate();}
                }else if("fulfill".equals(action)&&"APPROVED".equals(status)){
                    try(PreparedStatement p=c.prepareStatement("UPDATE blood_inventory SET units=units-? WHERE bank_user_id=? AND blood_group=? AND units>=?")){p.setInt(1,units);p.setLong(2,user.getId());p.setString(3,group);p.setInt(4,units);if(p.executeUpdate()!=1){c.rollback();res.sendRedirect(req.getContextPath()+"/bank/requests?error=stock");return;}}
                    try(PreparedStatement p=c.prepareStatement("UPDATE blood_requests SET status='FULFILLED' WHERE id=? AND bank_user_id=? AND status='APPROVED'")){p.setLong(1,id);p.setLong(2,user.getId());changed=p.executeUpdate();}
                }else{c.rollback();res.sendRedirect(req.getContextPath()+"/bank/requests?error=transition");return;}
                if(changed!=1){c.rollback();res.sendRedirect(req.getContextPath()+"/bank/requests?error=transition");return;}
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }catch(SQLException e){getServletContext().log("Could not process blood request",e);res.sendRedirect(req.getContextPath()+"/bank/requests?error=save");return;}
        res.sendRedirect(req.getContextPath()+"/bank/requests?updated=1");
    }
}