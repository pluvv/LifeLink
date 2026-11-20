package com.lifelink.filter;

import com.lifelink.model.User;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

@WebFilter(urlPatterns={"/dashboard","/admin","/inventory","/appointments","/bank/appointments","/recipient/banks","/bank/requests"})
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        if (session == null || !(session.getAttribute("user") instanceof User)) {
            res.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        User user = (User) session.getAttribute("user");
        String path = req.getServletPath();
        if ("/admin".equals(path) && !"ADMIN".equals(user.getRole())) { res.sendError(403); return; }
        if ("/inventory".equals(path) && !"BLOOD_BANK".equals(user.getRole())) { res.sendError(403); return; }
        if ("/appointments".equals(path) && !"DONOR".equals(user.getRole())) { res.sendError(403); return; }
        if (("/bank/appointments".equals(path) || "/bank/requests".equals(path)) && !"BLOOD_BANK".equals(user.getRole())) { res.sendError(403); return; }
        if ("/recipient/banks".equals(path) && !"RECIPIENT".equals(user.getRole())) { res.sendError(403); return; }
        chain.doFilter(request, response);
    }
}