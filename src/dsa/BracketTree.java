package dsa;

import model.Match;
import model.Team;

public class BracketTree {

    private final MatchNode[][] levels; // levels[0] = leaves ... levels[rounds] = {root}
    private final int rounds;

    private BracketTree(MatchNode[][] levels) {
        this.levels = levels;
        this.rounds = levels.length - 1;
    }

    public static BracketTree build(SinglyLinkedList teams, SinglyLinkedList results) {
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
        int t = 0;
        for (int i = 0; i < pairs; i++) {
            entrants[2 * i] = (Team) teams.get(t++);
            entrants[2 * i + 1] = (i < byes) ? null : (Team) teams.get(t++);
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


    public Team getChampion() {
        MatchNode root = getRoot();
        return root.isMatch() && root.isPlayed() ? root.getTeam() : null;
    }

    /** Every real match, earliest round first. */
    public SinglyLinkedList getMatches() {
        SinglyLinkedList list = new SinglyLinkedList();
        for (int r = 1; r <= rounds; r++) {
            for (MatchNode node : levels[r]) {      // plain array, so for-each is fine
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