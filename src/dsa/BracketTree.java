package dsa;

import model.Match;
import model.Team;

import java.util.Iterator;

/**
 * Single-elimination bracket stored as a binary tree of {@link MatchNode}s.
 * Leaves are the entrants (seeded in the order given), the root is the final.
 * When the team count is not a power of two, some first-round pairs get a BYE so the
 * tree is always complete (e.g. 6 teams -> 8 slots, 2 byes).
 */
public class BracketTree {

    private final MatchNode[][] levels; // levels[0] = leaves ... levels[rounds] = {root}
    private final int rounds;

    private BracketTree(MatchNode[][] levels) {
        this.levels = levels;
        this.rounds = levels.length - 1;
    }

    /** Builds the bracket for 2+ teams and applies every recorded match result. */
    public static BracketTree build(SinglyLinkedList<Team> teams, SinglyLinkedList<Match> results) {
        int n = teams.size();
        if (n < 2) throw new IllegalArgumentException("A bracket needs at least 2 teams.");

        int size = 2;
        int rounds = 1;
        while (size < n) {
            size *= 2;
            rounds++;
        }

        // place byes in different first-round pairs so no two byes ever meet
        int pairs = size / 2;
        int byes = size - n;
        Team[] entrants = new Team[size];
        Iterator<Team> it = teams.iterator();
        for (int i = 0; i < pairs; i++) {
            entrants[2 * i] = it.next();
            entrants[2 * i + 1] = (i < byes) ? null : it.next();
        }

        MatchNode[][] levels = new MatchNode[rounds + 1][];
        levels[0] = new MatchNode[size];
        for (int i = 0; i < size; i++) levels[0][i] = new MatchNode(0, entrants[i]);

        for (int r = 1; r <= rounds; r++) {
            levels[r] = new MatchNode[levels[r - 1].length / 2];
            for (int i = 0; i < levels[r].length; i++) {
                levels[r][i] = new MatchNode(r, levels[r - 1][2 * i], levels[r - 1][2 * i + 1], results);
            }
        }
        return new BracketTree(levels);
    }

    public MatchNode getRoot() { return levels[rounds][0]; }
    public int getRounds() { return rounds; }
    public int getLeafCount() { return levels[0].length; }

    /** The tournament winner, or null until the final has been played. */
    public Team getChampion() {
        MatchNode root = getRoot();
        return root.isMatch() && root.isPlayed() ? root.getTeam() : null;
    }

    /** Every real match, earliest round first. */
    public SinglyLinkedList<MatchNode> getMatches() {
        SinglyLinkedList<MatchNode> list = new SinglyLinkedList<>();
        for (int r = 1; r <= rounds; r++) {
            for (MatchNode node : levels[r]) {
                if (node.isMatch()) list.add(node);
            }
        }
        return list;
    }

    public String roundName(int round) {
        if (round == rounds) return "Final";
        if (round == rounds - 1) return "Semifinals";
        return "Round " + round;
    }
}