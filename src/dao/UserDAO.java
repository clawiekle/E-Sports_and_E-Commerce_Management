package dao;

import model.Admin;
import model.Fan;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT userID, username, password, role FROM user_table WHERE username = ?";

        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("userID");
                    String dbUsername = rs.getString("username");
                    String dbPassword = rs.getString("password");
                    String role = rs.getString("role");

                    if ("ADMIN".equalsIgnoreCase(role)) {
                        return new Admin(id, dbUsername, dbPassword);
                    }
                    return new Fan(id, dbUsername, dbPassword);
                }
            }
        }
        return null;
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM user_table WHERE username = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void createFan(String username, String password) throws SQLException {
        String sql = "INSERT INTO user_table (username, password, role) VALUES (?, ?, 'FAN')";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
        }
    }
}