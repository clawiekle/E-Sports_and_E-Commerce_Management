package dao;

import dsa.SinglyLinkedList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Simple persistent activity feed shown in "Recent Activity" on the admin dashboard. */
public class ActivityDAO {

    public void log(String message) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO activity_log (message) VALUES (?)")) {
            ps.setString(1, message);
            ps.executeUpdate();
        }
    }

    /** Newest first, each line formatted like "Sep 29, 3:05 PM  -  Team "X" was added". */
    public SinglyLinkedList<String> recent(int limit) throws SQLException {
        String sql = "SELECT message, DATE_FORMAT(created_at, '%b %e, %l:%i %p') AS t " +
                "FROM activity_log ORDER BY logID DESC LIMIT ?";
        SinglyLinkedList<String> lines = new SinglyLinkedList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lines.add(rs.getString("t") + "  -  " + rs.getString("message"));
                }
            }
        }
        return lines;
    }
}