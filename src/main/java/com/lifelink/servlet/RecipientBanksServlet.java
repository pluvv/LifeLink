package com.lifelink.servlet;

import com.lifelink.model.User;
import com.lifelink.util.DB;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/recipient/banks")
public class RecipientBanksServlet extends HttpServlet {
    private static final String[] GROUPS={"A+","A-","B+","B-","AB+","AB-","O+","O-"};
    private boolean validGroup(String value){return Arrays.asList(GROUPS).contains(value);}

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        User user=(User)req.getSession(false).getAttribute("user");
        if(!"RECIPIENT".equals(user.getRole())){res.sendError(403);return;}
        String sql="SELECT b.user_id,b.bank_name,b.address,b.city,b.phone,(SELECT GROUP_CONCAT(CONCAT(i.blood_group, ' = ', i.units, ' units') ORDER BY i.blood_group SEPARATOR ' · ') FROM blood_inventory i WHERE i.bank_user_id=b.user_id) AS stock_summary " +
                   "FROM blood_banks b JOIN users u ON u.id=b.user_id " +
                   "WHERE u.status='APPROVED' ORDER BY b.city,b.bank_name";
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            List<Map<String,Object>> banks=new ArrayList<>();
            while(r.next()){
                Map<String,Object> m=new HashMap<>();m.put("id",r.getLong("user_id"));m.put("name",r.getString("bank_name"));
                m.put("address",r.getString("address"));m.put("city",r.getString("city"));m.put("phone",r.getString("phone"));
                m.put("stockSummary",r.getString("stock_summary"));banks.add(m);
            }
            req.setAttribute("banks",banks);
        }catch(SQLException e){getServletContext().log("Could not load approved blood banks",e);res.sendError(500);return;}
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(
            "SELECT r.id,r.blood_group,r.units,r.status,r.created_at,b.bank_name,b.city FROM blood_requests r JOIN blood_banks b ON b.user_id=r.bank_user_id WHERE r.recipient_user_id=? ORDER BY r.created_at DESC")){
            p.setLong(1,user.getId());try(ResultSet r=p.executeQuery()){
                List<Map<String,Object>> requests=new ArrayList<>();while(r.next()){
                    Map<String,Object> m=new HashMap<>();m.put("id",r.getLong("id"));m.put("group",r.getString("blood_group"));
                    m.put("units",r.getInt("units"));m.put("status",r.getString("status"));m.put("created",r.getTimestamp("created_at"));
                    m.put("bank",r.getString("bank_name"));m.put("city",r.getString("city"));requests.add(m);
                }req.setAttribute("requests",requests);
            }
        }catch(SQLException e){getServletContext().log("Could not load recipient requests",e);res.sendError(500);return;}
        req.getRequestDispatcher("/WEB-INF/views/recipient-banks.jsp").forward(req,res);
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
        User user=(User)req.getSession(false).getAttribute("user");
        if(!"RECIPIENT".equals(user.getRole())){res.sendError(403);return;}
        String bank=req.getParameter("bankId"),group=req.getParameter("bloodGroup"),rawUnits=req.getParameter("units");
        try{
            if(bank==null||!bank.matches("\\d{1,18}")||!validGroup(group)||rawUnits==null||!rawUnits.matches("\\d{1,3}"))throw new IllegalArgumentException();
            long bankId=Long.parseLong(bank);int units=Integer.parseInt(rawUnits);if(units<1||units>100)throw new IllegalArgumentException();
            String sql="INSERT INTO blood_requests(recipient_user_id,bank_user_id,blood_group,units) SELECT ?,b.user_id,?,? FROM blood_banks b JOIN users u ON u.id=b.user_id WHERE b.user_id=? AND u.status='APPROVED'";
            try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
                p.setLong(1,user.getId());p.setString(2,group);p.setInt(3,units);p.setLong(4,bankId);
                if(p.executeUpdate()!=1){res.sendRedirect(req.getContextPath()+"/recipient/banks?error=bank");return;}
            }
        }catch(IllegalArgumentException e){res.sendRedirect(req.getContextPath()+"/recipient/banks?error=invalid");return;}
        catch(SQLException e){getServletContext().log("Could not submit blood request",e);res.sendRedirect(req.getContextPath()+"/recipient/banks?error=save");return;}
        res.sendRedirect(req.getContextPath()+"/recipient/banks?requested=1");
    }
}