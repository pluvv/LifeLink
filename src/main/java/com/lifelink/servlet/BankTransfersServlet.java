package com.lifelink.servlet;

import com.lifelink.model.User;
import com.lifelink.util.DB;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/bank/transfers")
public class BankTransfersServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        User user=(User)req.getSession(false).getAttribute("user");
        if(!"BLOOD_BANK".equals(user.getRole())){res.sendError(403);return;}
        try(Connection c=DB.getConnection();PreparedStatement banks=c.prepareStatement("SELECT b.user_id,b.bank_name,b.city FROM blood_banks b JOIN users u ON u.id=b.user_id WHERE u.status='APPROVED' AND b.user_id<>? ORDER BY b.city,b.bank_name")){
            banks.setLong(1,user.getId());try(ResultSet r=banks.executeQuery()){
                List<Map<String,Object>> list=new ArrayList<>();while(r.next()){Map<String,Object> m=new HashMap<>();m.put("id",r.getLong("user_id"));m.put("name",r.getString("bank_name"));m.put("city",r.getString("city"));list.add(m);}req.setAttribute("banks",list);
            }
        }catch(SQLException e){getServletContext().log("Could not load transfer destinations",e);res.sendError(500);return;}
        String sql="SELECT t.id,t.blood_group,t.units,t.status,t.created_at,f.bank_name AS from_name,f.city AS from_city,to_b.bank_name AS to_name,to_b.city AS to_city,t.from_bank_user_id,t.to_bank_user_id FROM transfer_requests t JOIN blood_banks f ON f.user_id=t.from_bank_user_id JOIN blood_banks to_b ON to_b.user_id=t.to_bank_user_id WHERE t.from_bank_user_id=? OR t.to_bank_user_id=? ORDER BY t.created_at DESC";
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,user.getId());p.setLong(2,user.getId());try(ResultSet r=p.executeQuery()){
                List<Map<String,Object>> list=new ArrayList<>();while(r.next()){Map<String,Object> m=new HashMap<>();m.put("id",r.getLong("id"));m.put("group",r.getString("blood_group"));m.put("units",r.getInt("units"));m.put("status",r.getString("status"));m.put("created",r.getTimestamp("created_at"));m.put("from",r.getString("from_name"));m.put("fromCity",r.getString("from_city"));m.put("to",r.getString("to_name"));m.put("toCity",r.getString("to_city"));m.put("fromId",r.getLong("from_bank_user_id"));m.put("toId",r.getLong("to_bank_user_id"));list.add(m);}req.setAttribute("transfers",list);
            }
        }catch(SQLException e){getServletContext().log("Could not load bank transfers",e);res.sendError(500);return;}
        req.getRequestDispatcher("/WEB-INF/views/bank-transfers.jsp").forward(req,res);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
        User user=(User)req.getSession(false).getAttribute("user");
        if(!"BLOOD_BANK".equals(user.getRole())){res.sendError(403);return;}
        String action=req.getParameter("action");
        if("create".equals(action)){
            String dest=req.getParameter("destinationId"),group=req.getParameter("bloodGroup"),raw=req.getParameter("units");
            try{
                if(dest==null||!dest.matches("\\d{1,18}")||group==null||!Arrays.asList("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(group)||raw==null||!raw.matches("\\d{1,3}"))throw new IllegalArgumentException();
                long destination=Long.parseLong(dest);int units=Integer.parseInt(raw);if(destination==user.getId()||units<1||units>100)throw new IllegalArgumentException();
                String sql="INSERT INTO transfer_requests(from_bank_user_id,to_bank_user_id,blood_group,units) SELECT ?,b.user_id,?,? FROM blood_banks b JOIN users u ON u.id=b.user_id WHERE b.user_id=? AND u.status='APPROVED'";
                try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,user.getId());p.setString(2,group);p.setInt(3,units);p.setLong(4,destination);if(p.executeUpdate()!=1){res.sendRedirect(req.getContextPath()+"/bank/transfers?error=destination");return;}}
            }catch(IllegalArgumentException e){res.sendRedirect(req.getContextPath()+"/bank/transfers?error=invalid");return;}
            catch(SQLException e){getServletContext().log("Could not create transfer request",e);res.sendRedirect(req.getContextPath()+"/bank/transfers?error=save");return;}
            res.sendRedirect(req.getContextPath()+"/bank/transfers?created=1");return;
        }
        String raw=req.getParameter("transferId");
        if(raw==null||!raw.matches("\\d{1,18}")||!Arrays.asList("approve","reject").contains(action)){res.sendRedirect(req.getContextPath()+"/bank/transfers?error=invalid");return;}
        long id=Long.parseLong(raw);
        try(Connection c=DB.getConnection()){
            c.setAutoCommit(false);
            try{
                long from,to;String group,status;int units;
                try(PreparedStatement p=c.prepareStatement("SELECT from_bank_user_id,to_bank_user_id,blood_group,units,status FROM transfer_requests WHERE id=? FOR UPDATE")){
                    p.setLong(1,id);try(ResultSet r=p.executeQuery()){if(!r.next()){c.rollback();res.sendRedirect(req.getContextPath()+"/bank/transfers?error=missing");return;}from=r.getLong("from_bank_user_id");to=r.getLong("to_bank_user_id");group=r.getString("blood_group");units=r.getInt("units");status=r.getString("status");}
                }
                if(to!=user.getId()||!"PENDING".equals(status)){c.rollback();res.sendRedirect(req.getContextPath()+"/bank/transfers?error=permission");return;}
                if("reject".equals(action)){
                    try(PreparedStatement p=c.prepareStatement("UPDATE transfer_requests SET status='REJECTED' WHERE id=? AND status='PENDING'")){p.setLong(1,id);if(p.executeUpdate()!=1){c.rollback();res.sendRedirect(req.getContextPath()+"/bank/transfers?error=state");return;}}
                }else{
                    try(PreparedStatement p=c.prepareStatement("UPDATE blood_inventory SET units=units-? WHERE bank_user_id=? AND blood_group=? AND units>=?")){p.setInt(1,units);p.setLong(2,from);p.setString(3,group);p.setInt(4,units);if(p.executeUpdate()!=1){c.rollback();res.sendRedirect(req.getContextPath()+"/bank/transfers?error=stock");return;}}
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO blood_inventory(bank_user_id,blood_group,units) VALUES(?,?,?) ON DUPLICATE KEY UPDATE units=units+VALUES(units)")){p.setLong(1,to);p.setString(2,group);p.setInt(3,units);p.executeUpdate();}
                    try(PreparedStatement p=c.prepareStatement("UPDATE transfer_requests SET status='COMPLETED' WHERE id=? AND status='PENDING'")){p.setLong(1,id);if(p.executeUpdate()!=1){c.rollback();res.sendRedirect(req.getContextPath()+"/bank/transfers?error=state");return;}}
                }
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }catch(SQLException e){getServletContext().log("Could not process transfer",e);res.sendRedirect(req.getContextPath()+"/bank/transfers?error=save");return;}
        res.sendRedirect(req.getContextPath()+"/bank/transfers?updated=1");
    }
}