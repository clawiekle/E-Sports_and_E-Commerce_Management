package dao;

import dsa.SinglyLinkedList;
import model.Game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GameDAO {

    public SinglyLinkedList<Game> getAll() throws SQLException {
        SinglyLinkedList<Game> games = new SinglyLinkedList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT gameID, game_name FROM games_table ORDER BY game_name")) {
            while (rs.next()) {
                games.add(new Game(rs.getInt("gameID"), rs.getString("game_name")));
            }
        }
        return games;
    }

    public boolean nameExists(String name) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM games_table WHERE LOWER(game_name) = LOWER(?)")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void add(String name) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO games_table (game_name) VALUES (?)")) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }
}