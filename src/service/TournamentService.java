package service;

import dao.ActivityDAO;
import dao.GameDAO;
import dao.TournamentDAO;
import dsa.SinglyLinkedList;
import model.Game;
import model.Team;
import model.Tournament;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * CRUD, team registration and the real-time start rule for tournaments.
 * Methods that can fail validation return an error message (null = success).
 */
public class TournamentService {

    private final TournamentDAO tournamentDAO = new TournamentDAO();
    private final GameDAO gameDAO = new GameDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    /** Starts every tournament whose scheduled date/time has arrived. Safe to call often. */
    public void syncStatuses() throws SQLException {
        tournamentDAO.startDueTournaments(LocalDateTime.now());
    }

    public SinglyLinkedList<Tournament> getAll(String search) throws SQLException {
        syncStatuses();
        return tournamentDAO.getAll(search == null ? "" : search.trim());
    }

    public Tournament getById(int id) throws SQLException {
        syncStatuses();
        return tournamentDAO.getById(id);
    }

    public SinglyLinkedList<Game> getGames() throws SQLException {
        return gameDAO.getAll();
    }

    // ------------------------------------------------------------------- CRUD

    public String create(Tournament t) throws SQLException {
        t.setTournamentName(clean(t.getTournamentName()));
        String error = validate(t);
        if (error != null) return error;
        tournamentDAO.add(t);
        activityDAO.log("Tournament \"" + t.getTournamentName() + "\" was created");
        return null;
    }

    public String update(Tournament t) throws SQLException {
        syncStatuses();
        Tournament current = tournamentDAO.getById(t.getTournamentID());
        if (current == null) return "This tournament no longer exists.";
        if (current.hasBegun()) return "This tournament has already begun and can no longer be edited.";

        t.setTournamentName(clean(t.getTournamentName()));
        String error = validate(t);
        if (error != null) return error;
        if (current.getGameID() != t.getGameID() && current.getRegisteredTeams() > 0) {
            return "Remove the registered teams before changing the game.";
        }
        if (t.getMaxTeams() < current.getRegisteredTeams()) {
            return "Maximum teams cannot be lower than the " + current.getRegisteredTeams()
                    + " team(s) already registered.";
        }
        tournamentDAO.update(t);
        activityDAO.log("Tournament \"" + t.getTournamentName() + "\" was updated");
        return null;
    }

    public String delete(Tournament t) throws SQLException {
        syncStatuses();
        Tournament current = tournamentDAO.getById(t.getTournamentID());
        if (current == null) return null; // already gone
        if (current.hasBegun()) return "This tournament has already begun and can no longer be deleted.";
        tournamentDAO.delete(t.getTournamentID());
        activityDAO.log("Tournament \"" + t.getTournamentName() + "\" was deleted");
        return null;
    }

    private String validate(Tournament t) throws SQLException {
        String name = t.getTournamentName();
        if (name.isEmpty()) return "Tournament name is required.";
        if (name.length() > 50) return "Tournament name must be 50 characters or less.";
        if (t.getGameID() <= 0) return "Please select a game.";
        if (!contains(Tournament.TEAM_OPTIONS, t.getMaxTeams())) return "Maximum teams must be 4, 8 or 16.";
        if (!contains(Tournament.BEST_OF_OPTIONS, t.getBestOf())) return "Match format must be Best of 1, 3 or 5.";
        if (t.getStartDateTime() == null) return "Tournament date is required.";
        if (!t.getStartDateTime().isAfter(LocalDateTime.now())) return "Tournament date and time must be in the future.";
        if (tournamentDAO.nameExists(name, t.getTournamentID())) return "A tournament with that name already exists.";
        return null;
    }

    // ------------------------------------------------------------------ teams

    public SinglyLinkedList<Team> getTeams(Tournament t) throws SQLException {
        return tournamentDAO.getTeams(t.getTournamentID());
    }

    public SinglyLinkedList<Team> getAvailableTeams(Tournament t) throws SQLException {
        return tournamentDAO.getAvailableTeams(t.getTournamentID(), t.getGameID());
    }

    public String addTeam(Tournament t, Team team) throws SQLException {
        syncStatuses();
        Tournament current = tournamentDAO.getById(t.getTournamentID());
        if (current == null) return "This tournament no longer exists.";
        if (current.hasBegun()) return "The tournament has already begun; teams can no longer be changed.";
        if (team.getGameID() != current.getGameID()) return "That team plays a different game.";
        if (current.getRegisteredTeams() >= current.getMaxTeams()) return "The tournament is already full.";
        for (Team registered : tournamentDAO.getTeams(current.getTournamentID())) {
            if (registered.getTeamID() == team.getTeamID()) return "That team is already registered.";
        }
        tournamentDAO.addTeam(current.getTournamentID(), team.getTeamID());
        activityDAO.log("Team \"" + team.getTeamName() + "\" joined \"" + current.getTournamentName() + "\"");
        return null;
    }

    public String removeTeam(Tournament t, Team team) throws SQLException {
        syncStatuses();
        Tournament current = tournamentDAO.getById(t.getTournamentID());
        if (current == null) return "This tournament no longer exists.";
        if (current.hasBegun()) return "The tournament has already begun; teams can no longer be changed.";
        tournamentDAO.removeTeam(current.getTournamentID(), team.getTeamID());
        activityDAO.log("Team \"" + team.getTeamName() + "\" left \"" + current.getTournamentName() + "\"");
        return null;
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }

    private static boolean contains(int[] options, int value) {
        for (int o : options) if (o == value) return true;
        return false;
    }
}