package com.lifelink.servlet;
import com.lifelink.util.DB;
import com.lifelink.util.Password;
import java.io.IOException;
import java.sql.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
@WebServlet("/setup-admin")
public class AdminBootstrapServlet extends HttpServlet{
 protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException{
  try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM users WHERE role='ADMIN'")){
   r.next();if(r.getInt(1)>0){res.sendError(404);return;}
  }catch(SQLException e){getServletContext().log("Admin bootstrap check failed",e);res.sendError(500);return;}
  req.getRequestDispatcher("/setup-admin.jsp").include(req,res);
 }
 protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
  String name=req.getParameter("fullName"),email=req.getParameter("email"),password=req.getParameter("password");
  if(name==null||email==null||password==null||name.trim().isEmpty()||password.length()<10||!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")){res.sendRedirect("setup-admin.jsp?error=invalid");return;}
  try(Connection c=DB.getConnection()){
   c.setAutoCommit(false);
   try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM users WHERE role='ADMIN' FOR UPDATE")){
    if(r.next()&&r.getInt(1)>0){c.rollback();res.sendError(404);return;}
   }
   try(PreparedStatement p=c.prepareStatement("INSERT INTO users(full_name,email,password_hash,role,status) VALUES(?,?,?,'ADMIN','APPROVED')")){
    p.setString(1,name.trim());p.setString(2,email.trim().toLowerCase());p.setString(3,Password.hash(password));p.executeUpdate();
   }
   c.commit();res.sendRedirect("login.jsp?adminCreated=1");
  }catch(SQLException e){getServletContext().log("Admin bootstrap error",e);res.sendRedirect("setup-admin.jsp?error=failed");}
 }
}