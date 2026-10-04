package model;

import java.time.LocalDateTime;

/** A tournament row from tournament_table (game name and team count are joined in). */
public class Tournament {

    public enum Status {
        UPCOMING("Upcoming"), ONGOING("Ongoing"), COMPLETED("Completed"), CANCELLED("Cancelled");

        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public static final int[] TEAM_OPTIONS = {4, 8, 16};
    public static final int[] BEST_OF_OPTIONS = {1, 3, 5};

    private int tournamentID;
    private String tournamentName;
    private int gameID;
    private String gameName;
    private int maxTeams;
    private int bestOf = 3;
    private LocalDateTime startDateTime;
    private Status status = Status.UPCOMING;
    private int registeredTeams;

    public Tournament() {
    }

    public Tournament(String tournamentName, int gameID, int maxTeams, int bestOf, LocalDateTime startDateTime) {
        this.tournamentName = tournamentName;
        this.gameID = gameID;
        this.maxTeams = maxTeams;
        this.bestOf = bestOf;
        this.startDateTime = startDateTime;
    }

    public int getTournamentID() { return tournamentID; }
    public void setTournamentID(int tournamentID) { this.tournamentID = tournamentID; }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public int getGameID() { return gameID; }
    public void setGameID(int gameID) { this.gameID = gameID; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public int getMaxTeams() { return maxTeams; }
    public void setMaxTeams(int maxTeams) { this.maxTeams = maxTeams; }

    public int getBestOf() { return bestOf; }
    public void setBestOf(int bestOf) { this.bestOf = bestOf; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getRegisteredTeams() { return registeredTeams; }
    public void setRegisteredTeams(int registeredTeams) { this.registeredTeams = registeredTeams; }

    /** True once the status moved past UPCOMING or the scheduled date/time has arrived. */
    public boolean hasBegun() {
        return status != Status.UPCOMING
                || (startDateTime != null && !LocalDateTime.now().isBefore(startDateTime));
    }

    /** Game, teams, time and date can only be edited / deleted before the tournament begins. */
    public boolean canModify() { return !hasBegun(); }

    @Override public String toString() { return tournamentName; }
}