package com.lifelink.servlet;

import com.lifelink.model.User;
import com.lifelink.util.DB;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/appointments")
public class AppointmentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user=(User)req.getSession(false).getAttribute("user");
        if(!"DONOR".equals(user.getRole())){res.sendError(403);return;}
        try(Connection c=DB.getConnection();
            PreparedStatement banks=c.prepareStatement("SELECT b.user_id,b.bank_name,b.city FROM blood_banks b JOIN users u ON u.id=b.user_id WHERE u.status='APPROVED' ORDER BY b.bank_name");
            PreparedStatement appts=c.prepareStatement("SELECT a.id,a.scheduled_at,a.status,a.notes,b.bank_name,b.city FROM donation_appointments a JOIN blood_banks b ON b.user_id=a.bank_user_id WHERE a.donor_user_id=? ORDER BY a.scheduled_at DESC")){
            try(ResultSet r=banks.executeQuery()){
                java.util.List<java.util.Map<String,Object>> list=new java.util.ArrayList<>();
                while(r.next()){java.util.Map<String,Object> m=new java.util.HashMap<>();m.put("id",r.getLong("user_id"));m.put("name",r.getString("bank_name"));m.put("city",r.getString("city"));list.add(m);}
                req.setAttribute("banks",list);
            }
            appts.setLong(1,user.getId());
            try(ResultSet r=appts.executeQuery()){
                java.util.List<java.util.Map<String,Object>> list=new java.util.ArrayList<>();
                while(r.next()){java.util.Map<String,Object> m=new java.util.HashMap<>();m.put("id",r.getLong("id"));m.put("when",r.getTimestamp("scheduled_at"));m.put("status",r.getString("status"));m.put("notes",r.getString("notes"));m.put("bank",r.getString("bank_name"));m.put("city",r.getString("city"));list.add(m);}
                req.setAttribute("appointments",list);
            }
        }catch(SQLException e){getServletContext().log("Could not load donor appointments",e);res.sendError(500);return;}
        req.getRequestDispatcher("/WEB-INF/views/appointments.jsp").forward(req,res);
    }

    @Override
    protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
        User user=(User)req.getSession(false).getAttribute("user");
        if(!"DONOR".equals(user.getRole())){res.sendError(403);return;}
        String bank=req.getParameter("bankId"),date=req.getParameter("scheduledAt"),notes=req.getParameter("notes");
        long bankId;LocalDateTime when;
        try{
            if(bank==null||!bank.matches("\\d{1,18}")||date==null)throw new IllegalArgumentException();
            bankId=Long.parseLong(bank);when=LocalDateTime.parse(date);
            if(!when.isAfter(LocalDateTime.now().plusMinutes(30)))throw new IllegalArgumentException();
            if(notes!=null&&notes.length()>500)throw new IllegalArgumentException();
        }catch(RuntimeException e){res.sendRedirect(req.getContextPath()+"/appointments?error=invalid");return;}
        String sql="INSERT INTO donation_appointments(donor_user_id,bank_user_id,scheduled_at,notes) SELECT ?,b.user_id,?,? FROM blood_banks b JOIN users u ON u.id=b.user_id WHERE b.user_id=? AND u.status='APPROVED'";
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,user.getId());p.setTimestamp(2,Timestamp.valueOf(when));p.setString(3,notes==null?"":notes.trim());p.setLong(4,bankId);
            if(p.executeUpdate()!=1){res.sendRedirect(req.getContextPath()+"/appointments?error=bank");return;}
        }catch(SQLException e){getServletContext().log("Could not create donation appointment",e);res.sendRedirect(req.getContextPath()+"/appointments?error=save");return;}
        res.sendRedirect(req.getContextPath()+"/appointments?booked=1");
    }
}