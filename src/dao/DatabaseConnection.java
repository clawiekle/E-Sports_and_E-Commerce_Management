package dao;
import java.sql.*;
import javax.swing.*;

public class DatabaseConnection {

    private static final String database = "esports_db";
    private static final String URL = "jdbc:mysql://localhost:3306/esports_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection con = null;


    public static Connection getConnection() throws SQLException {
        try {
            if (con == null || con.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                con = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connected to database." + database);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Connection error: " + e.getMessage());
            throw new SQLException("Driver error", e);
        }
        return con;
    }

    public static void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                con = null;
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        String baseUrl = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "";

        Statement stmt = null;
        Connection tempCon = null;

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            tempCon = DriverManager.getConnection(baseUrl, user, pass);
            stmt = tempCon.createStatement();

            //create database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS esports_db");

            stmt.close();
            tempCon.close();

            //connect to database
            tempCon = DriverManager.getConnection(URL, user, pass);
            stmt = tempCon.createStatement();
            System.out.println("Connected Successfully to " + database);

            //table for login (username + role so Admin and Fan can be told apart)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS user_table (" +
                            "userID INT AUTO_INCREMENT PRIMARY KEY," +
                            "username VARCHAR(100) NOT NULL UNIQUE," +
                            "password VARCHAR(50) NOT NULL," +
                            "role VARCHAR(10) NOT NULL DEFAULT 'FAN'" +
                            ")"
            );

            stmt.executeUpdate(
                    "INSERT IGNORE INTO user_table (username, password, role) " +
                            "VALUES ('esadmin', 'esadmin321', 'ADMIN')");
            System.out.println("Table 'user_table' ready.");

            //Games Table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS games_table (" +
                            "gameID INT AUTO_INCREMENT PRIMARY KEY," +
                            "game_name VARCHAR(50) NOT NULL" +
                            ")"
            );
            System.out.println("Table 'Games Table' ready.");

            //seed default games (only when the table is empty)
            ResultSet gameCount = stmt.executeQuery("SELECT COUNT(*) FROM games_table");
            gameCount.next();
            if (gameCount.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO games_table (game_name) VALUES " +
                        "('Mobile Legends'), ('Valorant'), ('Dota 2'), ('League of Legends'), ('Counter-Strike 2')");
                System.out.println("Default games added.");
            }
            gameCount.close();


            //Team Management Table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS team_table (" +
                            "teamID INT AUTO_INCREMENT PRIMARY KEY," +
                            "team_name VARCHAR(50) NOT NULL," +
                            "player_name VARCHAR(50) NOT NULL," +
                            "gameID INT NOT NULL," +
                            "FOREIGN KEY (gameID) REFERENCES games_table(gameID)" +
                            ")"
            );
            System.out.println("Table 'Team Table' ready.");


            //Tournament Management Table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS tournament_table (" +
                            "tournamentID INT AUTO_INCREMENT PRIMARY KEY," +
                            "tournament_name VARCHAR(50) NOT NULL," +
                            "gameID INT NOT NULL," +
                            "max_teams INT NOT NULL," +
                            "FOREIGN KEY (gameID) REFERENCES games_table(gameID)" +
                            ")"
            );
            System.out.println("Table 'Tournament Table' ready.");

            //Tournament Teams (Bridge) Table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS tournament_teams_table (" +
                            "tournamentTeamID INT AUTO_INCREMENT PRIMARY KEY," +
                            "tournamentID INT NOT NULL," +
                            "teamID INT NOT NULL," +
                            "FOREIGN KEY (tournamentID) REFERENCES tournament_table(tournamentID)," +
                            "FOREIGN KEY (teamID) REFERENCES team_table(teamID)" +
                            ")"
            );
            System.out.println("Table 'Tournament Teams Table' ready.");


            //Match Management Table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS match_table (" +
                            "matchID INT AUTO_INCREMENT PRIMARY KEY," +
                            "tournamentID INT NOT NULL," +
                            "team1ID INT NOT NULL," +
                            "team2ID INT NOT NULL," +
                            "team1_score INT NOT NULL," +
                            "team2_score INT NOT NULL," +
                            "winnerID INT," +
                            "is_tiebreaker BOOLEAN NOT NULL DEFAULT FALSE," +
                            "FOREIGN KEY (tournamentID) REFERENCES tournament_table(tournamentID)," +
                            "FOREIGN KEY (team1ID) REFERENCES team_table(teamID)," +
                            "FOREIGN KEY (team2ID) REFERENCES team_table(teamID)," +
                            "FOREIGN KEY (winnerID) REFERENCES team_table(teamID)" +
                            ")"
            );
            System.out.println("Table 'Match Table' ready.");

            //Merchandise Management Table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS merchandise_table (" +
                            "merchID INT AUTO_INCREMENT PRIMARY KEY," +
                            "teamID INT NOT NULL," +
                            "merch_name VARCHAR(50) NOT NULL," +
                            "price DECIMAL(10,2) NOT NULL," +
                            "stock_quantity INT NOT NULL DEFAULT 0," +
                            "FOREIGN KEY (teamID) REFERENCES team_table(teamID)" +
                            ")"
            );
            System.out.println("Table 'Merchandise Table' ready.");

            //Activity log (feeds "Recent Activity" on the admin dashboard)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS activity_log (" +
                            "logID INT AUTO_INCREMENT PRIMARY KEY," +
                            "message VARCHAR(255) NOT NULL," +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")"
            );
            System.out.println("Table 'Activity Log' ready.");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            closeConnection();
        }
    }

}