package dao;

import dsa.SinglyLinkedList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DashboardDAO {

    public int countTeams() throws SQLException { return scalar("SELECT COUNT(*) FROM team_table"); }
    public int countTournaments() throws SQLException { return scalar("SELECT COUNT(*) FROM tournament_table"); }
    public int countMatches() throws SQLException { return scalar("SELECT COUNT(*) FROM match_table"); }

    /** Latest tournaments as one-line summaries: "Name - Game (registered/max teams)". */
    public SinglyLinkedList tournamentSummaries(int limit) throws SQLException {
        String sql = "SELECT tr.tournament_name, g.game_name, tr.max_teams, " +
                "(SELECT COUNT(*) FROM tournament_teams_table tt WHERE tt.tournamentID = tr.tournamentID) AS registered " +
                "FROM tournament_table tr JOIN games_table g ON tr.gameID = g.gameID " +
                "ORDER BY tr.tournamentID DESC LIMIT " + limit;
        SinglyLinkedList lines = new SinglyLinkedList();
        Connection conn = DatabaseConnection.getConnection();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lines.add(rs.getString("tournament_name") + "  -  " + rs.getString("game_name")
                        + " (" + rs.getInt("registered") + "/" + rs.getInt("max_teams") + " teams)");
            }
        }
        return lines;
    }

    private int scalar(String sql) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}