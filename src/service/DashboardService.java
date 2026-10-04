package service;

import dsa.SinglyLinkedList;
import dao.ActivityDAO;
import dao.DashboardDAO;

import java.sql.SQLException;

public class DashboardService {

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    public int teamCount() throws SQLException { return dashboardDAO.countTeams(); }
    public int tournamentCount() throws SQLException { return dashboardDAO.countTournaments(); }
    public int matchCount() throws SQLException { return dashboardDAO.countMatches(); }

    public SinglyLinkedList<String> tournamentSummaries(int limit) throws SQLException { return dashboardDAO.tournamentSummaries(limit); }
    public SinglyLinkedList<String> recentActivity(int limit) throws SQLException { return activityDAO.recent(limit); }
}