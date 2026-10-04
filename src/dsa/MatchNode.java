package dsa;

import model.Match;
import model.Team;

/**
 * One node of the bracket tree.
 *  - Leaf (round 0): an entrant team, or a BYE when team == null.
 *  - Inner node (round 1 = first round ... last round = final): a match between the teams
 *    advancing out of its left and right child. {@link #getTeam()} is the team that comes OUT of
 *    this node (the match winner, or the team that got a bye), or null while still undecided (TBD).
 */
public class MatchNode {

    private final int round;
    private final MatchNode left;
    private final MatchNode right;
    private MatchNode parent;

    private Team team;
    private boolean bye;       // leaf only: empty slot
    private boolean played;    // inner node: a result has been recorded
    private int score1;
    private int score2;

    /** Leaf constructor (team == null means a BYE). */
    MatchNode(int round, Team team) {
        this.round = round;
        this.left = null;
        this.right = null;
        this.team = team;
        this.bye = (team == null);
    }

    /** Inner node constructor: works out who advances from the children and the recorded results. */
    MatchNode(int round, MatchNode left, MatchNode right, SinglyLinkedList<Match> results) {
        this.round = round;
        this.left = left;
        this.right = right;
        left.parent = this;
        right.parent = this;

        if (left.bye) {
            this.team = right.team;            // free pass for the other side
        } else if (right.bye) {
            this.team = left.team;
        } else if (left.team != null && right.team != null) {
            for (Match m : results) {
                int a = left.team.getTeamID();
                int b = right.team.getTeamID();
                boolean same = m.getTeam1ID() == a && m.getTeam2ID() == b;
                boolean flipped = m.getTeam1ID() == b && m.getTeam2ID() == a;
                if (same || flipped) {
                    this.played = true;
                    this.score1 = same ? m.getTeam1Score() : m.getTeam2Score();
                    this.score2 = same ? m.getTeam2Score() : m.getTeam1Score();
                    this.team = (m.getWinnerID() == a) ? left.team : right.team;
                    break;
                }
            }
        }
    }

    public int getRound() { return round; }
    public MatchNode getLeft() { return left; }
    public MatchNode getRight() { return right; }
    public MatchNode getParent() { return parent; }
    public boolean isLeaf() { return left == null; }
    public boolean isBye() { return bye; }

    /** The team that comes out of this node (null = BYE slot or still TBD). */
    public Team getTeam() { return team; }

    /** A real match to be played: inner node where neither side is a bye. */
    public boolean isMatch() { return left != null && !left.bye && !right.bye; }

    public Team getTeam1() { return left == null ? null : left.team; }
    public Team getTeam2() { return right == null ? null : right.team; }

    public boolean isPlayed() { return played; }
    public int getScore1() { return score1; }
    public int getScore2() { return score2; }
}