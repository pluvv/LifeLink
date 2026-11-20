package com.lifelink.servlet;
import com.lifelink.model.User;
import com.lifelink.util.DB;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
 protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException,ServletException{
  User u=(User)req.getSession(false).getAttribute("user");
  if(u==null||!"ADMIN".equals(u.getRole())){res.sendError(403);return;}
  List<User> pending=new ArrayList<>();
  try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,full_name,email,role,status FROM users WHERE status='PENDING' ORDER BY created_at ASC");ResultSet r=p.executeQuery()){
   while(r.next())pending.add(new User(r.getLong("id"),r.getString("full_name"),r.getString("email"),r.getString("role"),r.getString("status")));
  }catch(SQLException e){getServletContext().log("Admin list error",e);req.setAttribute("error","Unable to load registrations.");}
  req.setAttribute("pending",pending);req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req,res);
 }
 protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
  User u=(User)req.getSession(false).getAttribute("user");
  if(u==null||!"ADMIN".equals(u.getRole())){res.sendError(403);return;}
  String id=req.getParameter("id"),action=req.getParameter("action");
  if(id==null||!id.matches("\\d+")||!("approve".equals(action)||"reject".equals(action))){res.sendError(400);return;}
  String status="approve".equals(action)?"APPROVED":"REJECTED";
  try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET status=? WHERE id=? AND status='PENDING' AND role<>'ADMIN'")){
   p.setString(1,status);p.setLong(2,Long.parseLong(id));p.executeUpdate();
  }catch(SQLException e){getServletContext().log("Admin action error",e);}
  res.sendRedirect(req.getContextPath()+"/admin");
 }
}