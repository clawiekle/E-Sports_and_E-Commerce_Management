package model;

public class Team {
    private int teamID;
    private String teamName;
    private String playerName;
    private int gameID;
    private String gameName; // filled when read from the database (joined from games_table)

    public Team() {
    }

    public Team(String teamName, String playerName, int gameID) {
        this.teamName = teamName;
        this.playerName = playerName;
        this.gameID = gameID;
    }

    public Team(int teamID, String teamName, String playerName, int gameID, String gameName) {
        this(teamName, playerName, gameID);
        this.teamID = teamID;
        this.gameName = gameName;
    }

    public int getTeamID() { return teamID; }
    public void setTeamID(int teamID) { this.teamID = teamID; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getGameID() { return gameID; }
    public void setGameID(int gameID) { this.gameID = gameID; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    @Override public String toString() { return teamName; }
}