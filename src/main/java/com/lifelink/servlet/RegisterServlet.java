package com.lifelink.servlet;

import com.lifelink.util.DB;
import com.lifelink.util.Password;
import java.io.IOException;
import java.sql.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final String[] GROUPS={"A+","A-","B+","B-","AB+","AB-","O+","O-"};
    private String p(HttpServletRequest r,String k){String v=r.getParameter(k);return v==null?"":v.trim();}
    private boolean validGroup(String g){for(String x:GROUPS)if(x.equals(g))return true;return false;}

    protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
        String name=p(req,"fullName"),email=p(req,"email").toLowerCase(),password=req.getParameter("password"),role=p(req,"role");
        String group=p(req,"bloodGroup"),phone=p(req,"phone"),city=p(req,"city"),bank=p(req,"bankName"),address=p(req,"address");
        if(name.isBlank()||email.length()>190||password==null||password.length()<8||!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")||
          !(role.equals("DONOR")||role.equals("RECIPIENT")||role.equals("BLOOD_BANK"))||
          (role.equals("DONOR")&&!validGroup(group))||(role.equals("BLOOD_BANK")&&(bank.isBlank()||address.isBlank()))){
            res.sendRedirect("register.jsp?error=invalid");return;
        }
        try(Connection c=DB.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement u=c.prepareStatement("INSERT INTO users(full_name,email,password_hash,role,status) VALUES(?,?,?,?, 'PENDING')",Statement.RETURN_GENERATED_KEYS)){
                u.setString(1,name);u.setString(2,email);u.setString(3,Password.hash(password));u.setString(4,role);u.executeUpdate();
                long id;try(ResultSet k=u.getGeneratedKeys()){if(!k.next())throw new SQLException("User id missing");id=k.getLong(1);}
                if(role.equals("DONOR"))try(PreparedStatement s=c.prepareStatement("INSERT INTO donor_profiles(user_id,blood_group,phone,city) VALUES(?,?,?,?)")){s.setLong(1,id);s.setString(2,group);s.setString(3,phone);s.setString(4,city);s.executeUpdate();}
                else if(role.equals("RECIPIENT"))try(PreparedStatement s=c.prepareStatement("INSERT INTO recipient_profiles(user_id,phone,city) VALUES(?,?,?)")){s.setLong(1,id);s.setString(2,phone);s.setString(3,city);s.executeUpdate();}
                else try(PreparedStatement s=c.prepareStatement("INSERT INTO blood_banks(user_id,bank_name,address,city,phone) VALUES(?,?,?,?,?)")){s.setLong(1,id);s.setString(2,bank);s.setString(3,address);s.setString(4,city);s.setString(5,phone);s.executeUpdate();}
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
            res.sendRedirect("login.jsp?registered=1");
        }catch(SQLException e){getServletContext().log("Registration error",e);res.sendRedirect("register.jsp?error="+(e.getErrorCode()==1062?"exists":"failed"));}
    }
}