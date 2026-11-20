package com.lifelink.dao;

import com.lifelink.model.User;
import com.lifelink.util.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id,full_name,email,password_hash,role,status FROM users WHERE email=?";
        try (Connection con = DB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(rs.getLong("id"), rs.getString("full_name"), rs.getString("email"),
                    rs.getString("role"), rs.getString("status"));
            }
        }
    }

    public String getPasswordHash(String email) throws SQLException {
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT password_hash FROM users WHERE email=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM users WHERE email=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
}
