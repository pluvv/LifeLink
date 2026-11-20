package com.lifelink.servlet;

import com.lifelink.model.User;
import com.lifelink.util.DB;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/inventory")
public class InventoryServlet extends HttpServlet {
    private static final String[] GROUPS = {"A+","A-","B+","B-","AB+","AB-","O+","O-"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = (User) req.getSession(false).getAttribute("user");
        if (!"BLOOD_BANK".equals(user.getRole())) { res.sendError(403); return; }
        int[] units = new int[GROUPS.length];
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT blood_group, units FROM blood_inventory WHERE bank_user_id=?")) {
            p.setLong(1, user.getId());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    for (int i=0; i<GROUPS.length; i++) if (GROUPS[i].equals(r.getString("blood_group"))) units[i]=r.getInt("units");
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Could not load blood inventory", e);
            res.sendError(500); return;
        }
        req.setAttribute("bloodGroups", GROUPS);
        req.setAttribute("inventoryUnits", units);
        req.getRequestDispatcher("/WEB-INF/views/inventory.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        User user = (User) req.getSession(false).getAttribute("user");
        if (!"BLOOD_BANK".equals(user.getRole())) { res.sendError(403); return; }
        int[] units = new int[GROUPS.length];
        try {
            for (int i=0; i<GROUPS.length; i++) {
                String value = req.getParameter("units_" + i);
                if (value == null || !value.matches("\\d{1,5}")) throw new IllegalArgumentException();
                units[i] = Integer.parseInt(value);
                if (units[i] > 99999) throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            res.sendRedirect(req.getContextPath()+"/inventory?error=invalid"); return;
        }

        String sql = "INSERT INTO blood_inventory(bank_user_id,blood_group,units) VALUES(?,?,?) " +
                     "ON DUPLICATE KEY UPDATE units=VALUES(units)";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            c.setAutoCommit(false);
            try {
                for (int i=0; i<GROUPS.length; i++) {
                    p.setLong(1, user.getId()); p.setString(2, GROUPS[i]); p.setInt(3, units[i]); p.addBatch();
                }
                p.executeBatch(); c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        } catch (SQLException e) {
            getServletContext().log("Could not save blood inventory", e);
            res.sendRedirect(req.getContextPath()+"/inventory?error=save");
            return;
        }
        res.sendRedirect(req.getContextPath()+"/inventory?saved=1");
    }
}