package com.lifelink.servlet;
import com.lifelink.model.User;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
 protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException{
   HttpSession s=req.getSession(false);User u=s==null?null:(User)s.getAttribute("user");
   if(u==null){res.sendRedirect("login.jsp");return;}
   req.setAttribute("currentUser",u);
   try{req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req,res);}catch(javax.servlet.ServletException e){throw new IOException(e);}
 }
}