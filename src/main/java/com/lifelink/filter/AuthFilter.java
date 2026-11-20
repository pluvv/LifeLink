package com.lifelink.filter;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
@WebFilter("/dashboard")
public class AuthFilter implements Filter {
 public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
   HttpServletRequest req=(HttpServletRequest)request;HttpServletResponse res=(HttpServletResponse)response;
   HttpSession s=req.getSession(false);
   if(s==null||s.getAttribute("user")==null){res.sendRedirect(req.getContextPath()+"/login.jsp");return;}
   chain.doFilter(request,response);
 }
}