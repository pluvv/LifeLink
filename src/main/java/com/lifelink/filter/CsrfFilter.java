package com.lifelink.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

@WebFilter(urlPatterns={"/login","/login.jsp","/register","/register.jsp","/setup-admin","/admin","/inventory","/appointments","/bank/*","/recipient/*"})
public class CsrfFilter implements Filter {
    private static final SecureRandom RANDOM=new SecureRandom();
    @Override public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
        HttpServletRequest req=(HttpServletRequest)request;
        HttpServletResponse res=(HttpServletResponse)response;
        HttpSession session=req.getSession("GET".equalsIgnoreCase(req.getMethod()));
        if(session==null){res.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        String token=(String)session.getAttribute("csrfToken");
        if(token==null){byte[] bytes=new byte[32];RANDOM.nextBytes(bytes);StringBuilder hex=new StringBuilder(64);for(byte b:bytes)hex.append(String.format("%02x",b));token=hex.toString();session.setAttribute("csrfToken",token);}
        if("POST".equalsIgnoreCase(req.getMethod())){
            String supplied=req.getParameter("csrfToken");
            if(supplied==null||!MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8))){res.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        }
        chain.doFilter(request,response);
    }
}