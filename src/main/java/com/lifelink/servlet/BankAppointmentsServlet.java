package com.lifelink.servlet;

import com.lifelink.model.User;
import com.lifelink.util.DB;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/bank/appointments")
public class BankAppointmentsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = (User) req.getSession(false).getAttribute("user");
        if (!"BLOOD_BANK".equals(user.getRole())) { res.sendError(403); return; }
        String sql = "SELECT a.id,a.scheduled_at,a.status,a.notes,u.full_name,u.email,d.blood_group " +
                     "FROM donation_appointments a JOIN users u ON u.id=a.donor_user_id " +
                     "JOIN donor_profiles d ON d.user_id=a.donor_user_id " +
                     "WHERE a.bank_user_id=? ORDER BY CASE a.status WHEN 'PENDING' THEN 0 WHEN 'APPROVED' THEN 1 ELSE 2 END,a.scheduled_at";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, user.getId());
            try (ResultSet r = p.executeQuery()) {
                List<Map<String,Object>> appointments = new ArrayList<>();
                while (r.next()) {
                    Map<String,Object> item = new HashMap<>();
                    item.put("id", r.getLong("id"));
                    item.put("when", r.getTimestamp("scheduled_at"));
                    item.put("status", r.getString("status"));
                    item.put("notes", r.getString("notes"));
                    item.put("donor", r.getString("full_name"));
                    item.put("email", r.getString("email"));
                    item.put("bloodGroup", r.getString("blood_group"));
                    appointments.add(item);
                }
                req.setAttribute("appointments", appointments);
            }
        } catch (SQLException e) {
            getServletContext().log("Could not load bank appointments", e);
            res.sendError(500); return;
        }
        req.getRequestDispatcher("/WEB-INF/views/bank-appointments.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        User user = (User) req.getSession(false).getAttribute("user");
        if (!"BLOOD_BANK".equals(user.getRole())) { res.sendError(403); return; }
        String rawId = req.getParameter("appointmentId");
        String action = req.getParameter("action");
        if (rawId == null || !rawId.matches("\\d{1,18}") ||
            !Arrays.asList("approve", "reject", "complete").contains(action)) {
            res.sendRedirect(req.getContextPath() + "/bank/appointments?error=invalid"); return;
        }
        long id = Long.parseLong(rawId);
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                String status;
                String bloodGroup = null;
                try (PreparedStatement lock = c.prepareStatement(
                        "SELECT a.status,d.blood_group FROM donation_appointments a " +
                        "JOIN donor_profiles d ON d.user_id=a.donor_user_id " +
                        "WHERE a.id=? AND a.bank_user_id=? FOR UPDATE")) {
                    lock.setLong(1, id); lock.setLong(2, user.getId());
                    try (ResultSet r = lock.executeQuery()) {
                        if (!r.next()) { c.rollback(); res.sendRedirect(req.getContextPath()+"/bank/appointments?error=missing"); return; }
                        status = r.getString("status");
                        bloodGroup = r.getString("blood_group");
                    }
                }
                int changed = 0;
                if ("approve".equals(action) && "PENDING".equals(status)) {
                    try (PreparedStatement p = c.prepareStatement("UPDATE donation_appointments SET status='APPROVED' WHERE id=? AND bank_user_id=? AND status='PENDING'")) {
                        p.setLong(1,id); p.setLong(2,user.getId()); changed=p.executeUpdate();
                    }
                } else if ("reject".equals(action) && "PENDING".equals(status)) {
                    try (PreparedStatement p = c.prepareStatement("UPDATE donation_appointments SET status='REJECTED' WHERE id=? AND bank_user_id=? AND status='PENDING'")) {
                        p.setLong(1,id); p.setLong(2,user.getId()); changed=p.executeUpdate();
                    }
                } else if ("complete".equals(action) && "APPROVED".equals(status)) {
                    try (PreparedStatement p = c.prepareStatement("UPDATE donation_appointments SET status='COMPLETED' WHERE id=? AND bank_user_id=? AND status='APPROVED'")) {
                        p.setLong(1,id); p.setLong(2,user.getId()); changed=p.executeUpdate();
                    }
                    if (changed == 1) {
                        try (PreparedStatement stock = c.prepareStatement(
                                "INSERT INTO blood_inventory(bank_user_id,blood_group,units) VALUES(?,?,1) " +
                                "ON DUPLICATE KEY UPDATE units=units+1")) {
                            stock.setLong(1,user.getId()); stock.setString(2,bloodGroup); stock.executeUpdate();
                        }
                    }
                } else {
                    c.rollback();
                    res.sendRedirect(req.getContextPath()+"/bank/appointments?error=transition"); return;
                }
                if (changed != 1) { c.rollback(); res.sendRedirect(req.getContextPath()+"/bank/appointments?error=transition"); return; }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback(); throw e;
            }
        } catch (SQLException e) {
            getServletContext().log("Could not update donation appointment",e);
            res.sendRedirect(req.getContextPath()+"/bank/appointments?error=save"); return;
        }
        res.sendRedirect(req.getContextPath()+"/bank/appointments?updated=1");
    }
}
