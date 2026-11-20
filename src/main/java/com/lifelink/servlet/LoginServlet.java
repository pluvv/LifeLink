package com.lifelink.servlet;

import com.lifelink.dao.UserDAO;
import com.lifelink.model.User;
import com.lifelink.util.Password;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO users = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            response.sendRedirect("login.jsp?error=missing");
            return;
        }
        try {
            User user = users.findByEmail(email.trim().toLowerCase());
            String hash = users.getPasswordHash(email.trim().toLowerCase());
            if (user == null || !Password.verify(password, hash)) {
                response.sendRedirect("login.jsp?error=invalid");
                return;
            }
            if (!"APPROVED".equals(user.getStatus()) && !"ADMIN".equals(user.getRole())) {
                response.sendRedirect("login.jsp?error=" + user.getStatus().toLowerCase());
                return;
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(30 * 60);
            response.sendRedirect("dashboard");
        } catch (SQLException e) {
            getServletContext().log("LifeLink login database error", e);
            response.sendRedirect("login.jsp?error=database");
        }
    }
}
