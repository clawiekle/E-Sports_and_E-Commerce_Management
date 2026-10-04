package model;

/** A recorded match result (one row of match_table). */
public class Match {
    private int matchID;
    private int tournamentID;
    private int team1ID;
    private int team2ID;
    private int team1Score;
    private int team2Score;
    private int winnerID;

    public Match() {
    }

    public Match(int tournamentID, int team1ID, int team2ID, int team1Score, int team2Score, int winnerID) {
        this.tournamentID = tournamentID;
        this.team1ID = team1ID;
        this.team2ID = team2ID;
        this.team1Score = team1Score;
        this.team2Score = team2Score;
        this.winnerID = winnerID;
    }

    public int getMatchID() { return matchID; }
    public void setMatchID(int matchID) { this.matchID = matchID; }

    public int getTournamentID() { return tournamentID; }
    public void setTournamentID(int tournamentID) { this.tournamentID = tournamentID; }

    public int getTeam1ID() { return team1ID; }
    public void setTeam1ID(int team1ID) { this.team1ID = team1ID; }

    public int getTeam2ID() { return team2ID; }
    public void setTeam2ID(int team2ID) { this.team2ID = team2ID; }

    public int getTeam1Score() { return team1Score; }
    public void setTeam1Score(int team1Score) { this.team1Score = team1Score; }

    public int getTeam2Score() { return team2Score; }
    public void setTeam2Score(int team2Score) { this.team2Score = team2Score; }

    public int getWinnerID() { return winnerID; }
    public void setWinnerID(int winnerID) { this.winnerID = winnerID; }
}