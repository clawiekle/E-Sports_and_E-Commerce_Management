package dao;

import dsa.SinglyLinkedList;
import model.Team;
import model.Tournament;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TournamentDAO {

    private static final String SELECT =
            "SELECT tr.tournamentID, tr.tournament_name, tr.gameID, g.game_name, tr.max_teams, tr.best_of, " +
                    "tr.start_datetime, tr.status, " +
                    "(SELECT COUNT(*) FROM tournament_teams_table tt WHERE tt.tournamentID = tr.tournamentID) AS team_count " +
                    "FROM tournament_table tr JOIN games_table g ON tr.gameID = g.gameID ";

    // ------------------------------------------------------------ tournaments

    /** All tournaments (newest first), optionally filtered by tournament or game name. */
    public SinglyLinkedList getAll(String search) throws SQLException {
        boolean filter = search != null && !search.isEmpty();
        String sql = SELECT + (filter ? "WHERE tr.tournament_name LIKE ? OR g.game_name LIKE ? " : "")
                + "ORDER BY tr.tournamentID DESC";

        SinglyLinkedList list = new SinglyLinkedList();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filter) {
                String like = "%" + search + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Returns null if the tournament no longer exists. */
    public Tournament getById(int id) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE tr.tournamentID = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Tournament map(ResultSet rs) throws SQLException {
        Tournament t = new Tournament();
        t.setTournamentID(rs.getInt("tournamentID"));
        t.setTournamentName(rs.getString("tournament_name"));
        t.setGameID(rs.getInt("gameID"));
        t.setGameName(rs.getString("game_name"));
        t.setMaxTeams(rs.getInt("max_teams"));
        t.setBestOf(rs.getInt("best_of"));
        Timestamp ts = rs.getTimestamp("start_datetime");
        t.setStartDateTime(ts == null ? null : ts.toLocalDateTime());
        String status = rs.getString("status");
        t.setStatus(status == null ? Tournament.Status.UPCOMING : Tournament.Status.valueOf(status));
        t.setRegisteredTeams(rs.getInt("team_count"));
        return t;
    }

    /** True if another tournament already uses this name (excludeId = the one being edited, or 0 when adding). */
    public boolean nameExists(String name, int excludeId) throws SQLException {
        String sql = "SELECT 1 FROM tournament_table WHERE LOWER(tournament_name) = LOWER(?) AND tournamentID <> ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void add(Tournament t) throws SQLException {
        String sql = "INSERT INTO tournament_table (tournament_name, gameID, max_teams, best_of, start_datetime, status) " +
                "VALUES (?, ?, ?, ?, ?, 'UPCOMING')";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTournamentName());
            ps.setInt(2, t.getGameID());
            ps.setInt(3, t.getMaxTeams());
            ps.setInt(4, t.getBestOf());
            ps.setTimestamp(5, Timestamp.valueOf(t.getStartDateTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) t.setTournamentID(keys.getInt(1));
            }
        }
    }

    public void update(Tournament t) throws SQLException {
        String sql = "UPDATE tournament_table SET tournament_name = ?, gameID = ?, max_teams = ?, " +
                "best_of = ?, start_datetime = ? WHERE tournamentID = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTournamentName());
            ps.setInt(2, t.getGameID());
            ps.setInt(3, t.getMaxTeams());
            ps.setInt(4, t.getBestOf());
            ps.setTimestamp(5, Timestamp.valueOf(t.getStartDateTime()));
            ps.setInt(6, t.getTournamentID());
            ps.executeUpdate();
        }
    }

    public void updateStatus(int id, Tournament.Status status) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE tournament_table SET status = ? WHERE tournamentID = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Deletes the tournament together with its team registrations (one transaction). */
    public void delete(int id) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        boolean previous = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tournament_teams_table WHERE tournamentID = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tournament_table WHERE tournamentID = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(previous);
        }
    }

    /**
     * Real-time start: UPCOMING tournaments whose date/time has arrived become ONGOING,
     * or CANCELLED when fewer than 2 teams registered (no bracket is possible).
     */
    public void startDueTournaments(LocalDateTime now) throws SQLException {
        Timestamp ts = Timestamp.valueOf(now);
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement cancel = conn.prepareStatement(
                "UPDATE tournament_table SET status = 'CANCELLED' WHERE status = 'UPCOMING' AND start_datetime <= ? " +
                        "AND (SELECT COUNT(*) FROM tournament_teams_table tt WHERE tt.tournamentID = tournament_table.tournamentID) < 2")) {
            cancel.setTimestamp(1, ts);
            cancel.executeUpdate();
        }
        try (PreparedStatement start = conn.prepareStatement(
                "UPDATE tournament_table SET status = 'ONGOING' WHERE status = 'UPCOMING' AND start_datetime <= ?")) {
            start.setTimestamp(1, ts);
            start.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ teams

    public SinglyLinkedList getTeams(int tournamentId) throws SQLException {
        String sql = "SELECT t.teamID, t.team_name, t.player_name, t.gameID, g.game_name " +
                "FROM tournament_teams_table tt JOIN team_table t ON tt.teamID = t.teamID " +
                "JOIN games_table g ON t.gameID = g.gameID WHERE tt.tournamentID = ? ORDER BY t.teamID";
        return queryTeams(sql, tournamentId, 0);
    }

    /** Teams of the given game that are not registered in this tournament yet. */
    public SinglyLinkedList getAvailableTeams(int tournamentId, int gameId) throws SQLException {
        String sql = "SELECT t.teamID, t.team_name, t.player_name, t.gameID, g.game_name " +
                "FROM team_table t JOIN games_table g ON t.gameID = g.gameID " +
                "WHERE t.gameID = ? AND t.teamID NOT IN " +
                "(SELECT teamID FROM tournament_teams_table WHERE tournamentID = ?) ORDER BY t.team_name";
        return queryTeams(sql, gameId, tournamentId);
    }

    private SinglyLinkedList queryTeams(String sql, int first, int second) throws SQLException {
        SinglyLinkedList teams = new SinglyLinkedList();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, first);
            if (second != 0) ps.setInt(2, second);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    teams.add(new Team(rs.getInt("teamID"), rs.getString("team_name"),
                            rs.getString("player_name"), rs.getInt("gameID"), rs.getString("game_name")));
                }
            }
        }
        return teams;
    }

    public void addTeam(int tournamentId, int teamId) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO tournament_teams_table (tournamentID, teamID) VALUES (?, ?)")) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, teamId);
            ps.executeUpdate();
        }
    }

    public void removeTeam(int tournamentId, int teamId) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM tournament_teams_table WHERE tournamentID = ? AND teamID = ?")) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, teamId);
            ps.executeUpdate();
        }
    }
}