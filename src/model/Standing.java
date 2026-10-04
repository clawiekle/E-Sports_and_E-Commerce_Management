package model;

/** One row of the Standings tab. */
public class Standing {
    private final String teamName;
    private final int played;
    private final int won;
    private final int lost;
    private final String state; // Champion / Eliminated / In contention

    public Standing(String teamName, int played, int won, int lost, String state) {
        this.teamName = teamName;
        this.played = played;
        this.won = won;
        this.lost = lost;
        this.state = state;
    }

    public String getTeamName() { return teamName; }
    public int getPlayed() { return played; }
    public int getWon() { return won; }
    public int getLost() { return lost; }
    public String getState() { return state; }
}