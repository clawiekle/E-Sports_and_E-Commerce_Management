package dao;

import dsa.SinglyLinkedList;
import model.Match;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MatchDAO {

    public SinglyLinkedList<Match> findByTournament(int tournamentId) throws SQLException {
        String sql = "SELECT matchID, tournamentID, team1ID, team2ID, team1_score, team2_score, winnerID " +
                "FROM match_table WHERE tournamentID = ? ORDER BY matchID";
        SinglyLinkedList<Match> list = new SinglyLinkedList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Match m = new Match(rs.getInt("tournamentID"), rs.getInt("team1ID"), rs.getInt("team2ID"),
                            rs.getInt("team1_score"), rs.getInt("team2_score"), rs.getInt("winnerID"));
                    m.setMatchID(rs.getInt("matchID"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    public void add(Match m) throws SQLException {
        String sql = "INSERT INTO match_table (tournamentID, team1ID, team2ID, team1_score, team2_score, winnerID, is_tiebreaker) " +
                "VALUES (?, ?, ?, ?, ?, ?, FALSE)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getTournamentID());
            ps.setInt(2, m.getTeam1ID());
            ps.setInt(3, m.getTeam2ID());
            ps.setInt(4, m.getTeam1Score());
            ps.setInt(5, m.getTeam2Score());
            ps.setInt(6, m.getWinnerID());
            ps.executeUpdate();
        }
    }
}