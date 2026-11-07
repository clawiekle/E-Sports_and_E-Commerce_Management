package dao;

import dsa.SinglyLinkedList;
import model.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TeamDAO {

    /** All teams (with their game name), optionally filtered by team, player or game name. */
    public SinglyLinkedList getAll(String search) throws SQLException {
        String sql = "SELECT t.teamID, t.team_name, t.player_name, t.gameID, g.game_name " +
                "FROM team_table t JOIN games_table g ON t.gameID = g.gameID ";
        boolean filter = search != null && !search.isEmpty();
        if (filter) {
            sql += "WHERE t.team_name LIKE ? OR t.player_name LIKE ? OR g.game_name LIKE ? ";
        }
        sql += "ORDER BY t.team_name";

        SinglyLinkedList teams = new SinglyLinkedList();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filter) {
                String like = "%" + search + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    teams.add(new Team(rs.getInt("teamID"), rs.getString("team_name"),
                            rs.getString("player_name"), rs.getInt("gameID"), rs.getString("game_name")));
                }
            }
        }
        return teams;
    }

    /** True if another team already uses this name (excludeId = the team being edited, or 0 when adding). */
    public boolean nameExists(String teamName, int excludeId) throws SQLException {
        String sql = "SELECT 1 FROM team_table WHERE LOWER(team_name) = LOWER(?) AND teamID <> ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, teamName);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void add(Team team) throws SQLException {
        String sql = "INSERT INTO team_table (team_name, player_name, gameID) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getPlayerName());
            ps.setInt(3, team.getGameID());
            ps.executeUpdate();
        }
    }

    public void update(Team team) throws SQLException {
        String sql = "UPDATE team_table SET team_name = ?, player_name = ?, gameID = ? WHERE teamID = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getPlayerName());
            ps.setInt(3, team.getGameID());
            ps.setInt(4, team.getTeamID());
            ps.executeUpdate();
        }
    }

    /** Throws SQLIntegrityConstraintViolationException if the team is still used by a tournament, match or merchandise. */
    public void delete(int teamId) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM team_table WHERE teamID = ?")) {
            ps.setInt(1, teamId);
            ps.executeUpdate();
        }
    }
}