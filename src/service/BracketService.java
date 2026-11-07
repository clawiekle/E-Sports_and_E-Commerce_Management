package service;

import dao.ActivityDAO;
import dao.MatchDAO;
import dao.TournamentDAO;
import dsa.BracketTree;
import dsa.MatchNode;
import dsa.SinglyLinkedList;
import model.Match;
import model.Standing;
import model.Team;
import model.Tournament;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Builds the bracket tree for a started tournament, records match results and works out standings. */
public class BracketService {

    private final TournamentDAO tournamentDAO = new TournamentDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    /** The bracket, or null while the tournament is UPCOMING / CANCELLED (no bracket exists yet). */
    public BracketTree getBracket(Tournament t) throws SQLException {
        if (t.getStatus() == Tournament.Status.UPCOMING || t.getStatus() == Tournament.Status.CANCELLED) return null;
        return buildTree(t.getTournamentID());
    }

    /** Seeding is a shuffle seeded with the tournament id, so the draw is random but never changes. */
    private BracketTree buildTree(int tournamentId) throws SQLException {
        SinglyLinkedList teams = tournamentDAO.getTeams(tournamentId);
        if (teams.size() < 2) return null;

        List<Team> shuffled = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) shuffled.add((Team) teams.get(i));
        Collections.shuffle(shuffled, new Random(tournamentId));

        SinglyLinkedList seeded = new SinglyLinkedList();
        for (Team team : shuffled) seeded.add(team);
        return BracketTree.build(seeded, matchDAO.findByTournament(tournamentId));
    }

    /**
     * Records the score of a pending match (team1 / team2 as shown on the node).
     * Best of N: the winner needs exactly (N+1)/2 wins. Completes the tournament after the final.
     * Returns an error message, or null on success.
     */
    public String recordResult(Tournament t, Team team1, Team team2, int score1, int score2) throws SQLException {
        new TournamentService().syncStatuses();
        Tournament fresh = tournamentDAO.getById(t.getTournamentID());
        if (fresh == null) return "This tournament no longer exists.";
        if (fresh.getStatus() != Tournament.Status.ONGOING) return "Results can only be recorded while the tournament is ongoing.";

        int need = (fresh.getBestOf() + 1) / 2;
        int high = Math.max(score1, score2);
        int low = Math.min(score1, score2);
        if (high != need || low < 0 || low >= need) {
            return "In a Best of " + fresh.getBestOf() + " the winner needs exactly " + need
                    + " win(s) and the other team fewer than that.";
        }

        BracketTree tree = buildTree(fresh.getTournamentID());
        if (tree == null) return "No bracket available.";
        boolean ready = false;
        SinglyLinkedList nodes = tree.getMatches();
        for (int i = 0; i < nodes.size(); i++) {
            MatchNode node = (MatchNode) nodes.get(i);
            if (!node.isPlayed() && node.getTeam1() != null && node.getTeam2() != null
                    && node.getTeam1().getTeamID() == team1.getTeamID()
                    && node.getTeam2().getTeamID() == team2.getTeamID()) {
                ready = true;
                break;
            }
        }
        if (!ready) return "That match is not ready yet, or its result was already recorded.";

        Team winner = score1 > score2 ? team1 : team2;
        matchDAO.add(new Match(fresh.getTournamentID(), team1.getTeamID(), team2.getTeamID(),
                score1, score2, winner.getTeamID()));
        activityDAO.log(team1.getTeamName() + " " + score1 + " - " + score2 + " " + team2.getTeamName()
                + " (" + fresh.getTournamentName() + ")");

        BracketTree after = buildTree(fresh.getTournamentID());
        if (after != null && after.getChampion() != null) {
            tournamentDAO.updateStatus(fresh.getTournamentID(), Tournament.Status.COMPLETED);
            activityDAO.log(after.getChampion().getTeamName() + " won \"" + fresh.getTournamentName() + "\"");
        }
        return null;
    }

    /** Standings by series won, or null while the tournament has not started. */
    public SinglyLinkedList getStandings(Tournament t) throws SQLException {
        BracketTree tree = getBracket(t);
        if (tree == null) return null;

        SinglyLinkedList teams = tournamentDAO.getTeams(t.getTournamentID());
        SinglyLinkedList matches = matchDAO.findByTournament(t.getTournamentID());
        Team champion = tree.getChampion();

        List<Standing> rows = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            Team team = (Team) teams.get(i);
            int won = 0;
            int lost = 0;
            for (int j = 0; j < matches.size(); j++) {
                Match m = (Match) matches.get(j);
                if (m.getTeam1ID() != team.getTeamID() && m.getTeam2ID() != team.getTeamID()) continue;
                if (m.getWinnerID() == team.getTeamID()) won++;
                else lost++;
            }
            String state = (champion != null && champion.getTeamID() == team.getTeamID()) ? "Champion"
                    : lost > 0 ? "Eliminated" : "In contention";
            rows.add(new Standing(team.getTeamName(), won + lost, won, lost, state));
        }
        rows.sort((a, b) -> a.getWon() != b.getWon() ? b.getWon() - a.getWon()
                : a.getLost() != b.getLost() ? a.getLost() - b.getLost()
                : a.getTeamName().compareToIgnoreCase(b.getTeamName()));

        SinglyLinkedList result = new SinglyLinkedList();
        for (Standing s : rows) result.add(s);
        return result;
    }
}