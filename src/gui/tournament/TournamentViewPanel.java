package gui.tournament;

import dsa.BracketTree;
import dsa.MatchNode;
import dsa.SinglyLinkedList;
import gui.BracketDiagram;
import gui.Theme;
import model.Standing;
import model.Team;
import model.Tournament;
import service.BracketService;
import service.TournamentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tournament view (Bracket View mockup): header "Game - # teams - Best of N" and the tabs
 * Teams / Matches / Bracket / Standings. Edit / Delete are only offered before the tournament begins.
 */
public class TournamentViewPanel extends JPanel {

    private static final String TEAMS = "Teams";
    private static final String MATCHES = "Matches";
    private static final String BRACKET = "Bracket";
    private static final String STANDINGS = "Standings";
    private static final String[] TAB_NAMES = {TEAMS, MATCHES, BRACKET, STANDINGS};

    /** A database action that returns an error message (null = success). */
    private interface Action { String run() throws SQLException; }

    private final TournamentService service = new TournamentService();
    private final BracketService bracketService = new BracketService();
    private final Runnable onBack;
    private final Consumer<Tournament> onEdit;
    private final Runnable onDeleted;

    private Tournament tournament;
    private String currentTab;

    private final JLabel title = new JLabel();
    private final JLabel subtitle = new JLabel();
    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JButton[] tabButtons = new JButton[TAB_NAMES.length];
    private final JPanel body = new JPanel(new BorderLayout());

    public TournamentViewPanel(Tournament tournament, Runnable onBack, Consumer<Tournament> onEdit, Runnable onDeleted) {
        this.tournament = tournament;
        this.onBack = onBack;
        this.onEdit = onEdit;
        this.onDeleted = onDeleted;

        setLayout(new BorderLayout(0, 8));
        setBackground(Theme.CREAM);
        setBorder(new EmptyBorder(20, 25, 25, 25));

        // ---- header
        JButton back = new JButton("< Back to Tournament Management");
        back.setFont(Theme.BASE);
        back.setBorderPainted(false);
        back.setContentAreaFilled(false);
        back.setFocusPainted(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> onBack.run());
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(back);

        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);

        actions.setOpaque(false);
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(titles, BorderLayout.WEST);
        titleRow.add(actions, BorderLayout.EAST);

        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        tabBar.setOpaque(false);
        for (int i = 0; i < TAB_NAMES.length; i++) {
            String name = TAB_NAMES[i];
            JButton b = new JButton(name);
            b.setFont(Theme.BASE);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> showTab(name));
            tabButtons[i] = b;
            tabBar.add(b);
        }

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{backRow, titleRow}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(c);
        }
        top.add(Box.createVerticalStrut(10));
        tabBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(tabBar);
        JSeparator line = new JSeparator();
        line.setForeground(Color.BLACK);
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(line);
        add(top, BorderLayout.NORTH);

        body.setOpaque(false);
        add(body, BorderLayout.CENTER);

        showTab(tournament.hasBegun() ? BRACKET : TEAMS);
    }

    // ------------------------------------------------------------------- tabs

    /** Re-reads the tournament (it may have started meanwhile) and redraws the header and the tab. */
    private void showTab(String name) {
        currentTab = name;
        body.removeAll();
        try {
            Tournament fresh = service.getById(tournament.getTournamentID());
            if (fresh == null) {
                onBack.run();
                return;
            }
            tournament = fresh;
            updateHeader();
            switch (name) {
                case TEAMS: body.add(buildTeamsTab(), BorderLayout.CENTER); break;
                case MATCHES: body.add(buildMatchesTab(), BorderLayout.CENTER); break;
                case BRACKET: body.add(buildBracketTab(), BorderLayout.CENTER); break;
                default: body.add(buildStandingsTab(), BorderLayout.CENTER);
            }
        } catch (SQLException ex) {
            body.add(message("Could not load data: " + ex.getMessage()), BorderLayout.NORTH);
        }
        for (int i = 0; i < TAB_NAMES.length; i++) {
            boolean active = TAB_NAMES[i].equals(name);
            tabButtons[i].setBorder(active
                    ? BorderFactory.createCompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(3, 10, 3, 10))
                    : new EmptyBorder(4, 11, 4, 11));
        }
        body.revalidate();
        body.repaint();
    }

    private void updateHeader() {
        title.setText(tournament.getTournamentName());
        subtitle.setText(tournament.getGameName() + "  -  " + tournament.getRegisteredTeams() + " teams  -  Best of "
                + tournament.getBestOf() + "  -  " + tournament.getStatus().getLabel()
                + (tournament.getStartDateTime() == null ? ""
                : "  -  " + tournament.getStartDateTime().format(
                java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"))));

        actions.removeAll();
        if (tournament.canModify()) {
            JButton edit = new Theme.Btn("Edit", Theme.MAROON, Theme.INK);
            JButton delete = new Theme.Btn("Delete", new Color(150, 40, 40), Color.WHITE);
            edit.addActionListener(e -> onEdit.accept(tournament));
            delete.addActionListener(e -> deleteTournament());
            actions.add(edit);
            actions.add(delete);
        }
        actions.revalidate();
        actions.repaint();
    }

    // ------------------------------------------------------------- Teams tab

    private JComponent buildTeamsTab() throws SQLException {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 0, 0, 0));

        SinglyLinkedList registered = service.getTeams(tournament);
        JLabel count = new JLabel("Registered teams: " + registered.size() + " / " + tournament.getMaxTeams());
        count.setFont(Theme.BOLD);
        p.add(count, BorderLayout.NORTH);

        DefaultListModel<Team> model = new DefaultListModel<>();
        for (int i = 0; i < registered.size(); i++) model.addElement((Team) registered.get(i));
        JList<Team> list = new JList<>(model);
        list.setFont(Theme.BASE);
        list.setBackground(TournamentPanel.PANEL_GRAY);
        list.setForeground(Color.BLACK);
        list.setSelectionBackground(TournamentPanel.SELECT_GRAY);
        list.setSelectionForeground(Color.BLACK);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean f) {
                Team t = (Team) v;
                JLabel lbl = (JLabel) super.getListCellRendererComponent(l, t.getTeamName() + "   -   " + t.getPlayerName(), i, sel, false);
                lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
                return lbl;
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(TournamentPanel.PANEL_GRAY, 4));
        p.add(scroll, BorderLayout.CENTER);

        if (tournament.canModify()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            row.setOpaque(false);
            JComboBox<Team> combo = new JComboBox<>();
            combo.setFont(Theme.BASE);
            combo.setPreferredSize(new Dimension(240, 36));
            SinglyLinkedList available = service.getAvailableTeams(tournament);
            for (int i = 0; i < available.size(); i++) combo.addItem((Team) available.get(i));
            JButton add = new Theme.Btn("Add Team", Theme.MAROON, Theme.INK);
            JButton remove = new Theme.Btn("Remove Selected", new Color(150, 40, 40), Color.WHITE);
            add.addActionListener(e -> {
                Team team = (Team) combo.getSelectedItem();
                if (team != null) run(() -> service.addTeam(tournament, team));
            });
            remove.addActionListener(e -> {
                Team team = list.getSelectedValue();
                if (team != null) run(() -> service.removeTeam(tournament, team));
            });
            row.add(combo);
            row.add(add);
            row.add(remove);
            p.add(row, BorderLayout.SOUTH);
        } else {
            JLabel locked = new JLabel("The tournament has begun, so teams can no longer be changed.");
            locked.setFont(Theme.BASE);
            p.add(locked, BorderLayout.SOUTH);
        }
        return p;
    }

    // ----------------------------------------------------------- Matches tab

    private JComponent buildMatchesTab() throws SQLException {
        BracketTree tree = bracketService.getBracket(tournament);
        if (tree == null) return message(noBracketText());

        String[] cols = {"Round", "Team 1", "Team 2", "Score", "Result"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<MatchNode> nodes = new ArrayList<>();
        SinglyLinkedList matchList = tree.getMatches();
        for (int i = 0; i < matchList.size(); i++) {
            MatchNode n = (MatchNode) matchList.get(i);
            nodes.add(n);
            boolean ready = n.getTeam1() != null && n.getTeam2() != null;
            model.addRow(new Object[]{
                    tree.roundName(n.getRound()),
                    n.getTeam1() == null ? "TBD" : n.getTeam1().getTeamName(),
                    n.getTeam2() == null ? "TBD" : n.getTeam2().getTeamName(),
                    n.isPlayed() ? n.getScore1() + " - " + n.getScore2() : "-",
                    n.isPlayed() ? n.getTeam().getTeamName() + " won" : ready ? "Pending" : "Waiting for earlier round"});
        }
        JTable table = new JTable(model);
        TournamentPanel.styleTable(table);

        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 0, 0, 0));
        p.add(TournamentPanel.tableScroll(table), BorderLayout.CENTER);

        if (tournament.getStatus() == Tournament.Status.ONGOING) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            row.setOpaque(false);
            JButton record = new Theme.Btn("Record Result", Theme.MAROON, Theme.INK);
            record.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r < 0) {
                    JOptionPane.showMessageDialog(this, "Select a match first.");
                    return;
                }
                recordResult(nodes.get(r));
            });
            row.add(record);
            p.add(row, BorderLayout.SOUTH);
        }
        return p;
    }

    private void recordResult(MatchNode node) {
        if (node.isPlayed() || node.getTeam1() == null || node.getTeam2() == null) {
            JOptionPane.showMessageDialog(this, "That match is not ready, or it was already played.");
            return;
        }
        int need = (tournament.getBestOf() + 1) / 2;
        JSpinner s1 = new JSpinner(new SpinnerNumberModel(0, 0, need, 1));
        JSpinner s2 = new JSpinner(new SpinnerNumberModel(0, 0, need, 1));
        JPanel form = new JPanel(new GridLayout(2, 2, 10, 8));
        form.add(new JLabel(node.getTeam1().getTeamName()));
        form.add(s1);
        form.add(new JLabel(node.getTeam2().getTeamName()));
        form.add(s2);

        int choice = JOptionPane.showConfirmDialog(this, form,
                "Result - Best of " + tournament.getBestOf() + " (first to " + need + ")",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        int a = (Integer) s1.getValue();
        int b = (Integer) s2.getValue();
        run(() -> bracketService.recordResult(tournament, node.getTeam1(), node.getTeam2(), a, b));
    }

    // ----------------------------------------------------------- Bracket tab

    private JComponent buildBracketTab() throws SQLException {
        BracketTree tree = bracketService.getBracket(tournament);
        if (tree == null) return message(noBracketText());
        JScrollPane scroll = new JScrollPane(new BracketDiagram(tree));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.CREAM);
        return scroll;
    }

    // --------------------------------------------------------- Standings tab

    private JComponent buildStandingsTab() throws SQLException {
        SinglyLinkedList standings = bracketService.getStandings(tournament);
        if (standings == null) return message(noBracketText());

        String[] cols = {"Rank", "Team", "Played", "Won", "Lost", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        int rank = 1;
        for (int i = 0; i < standings.size(); i++) {
            Standing s = (Standing) standings.get(i);
            model.addRow(new Object[]{rank++, s.getTeamName(), s.getPlayed(), s.getWon(), s.getLost(), s.getState()});
        }
        JTable table = new JTable(model);
        TournamentPanel.styleTable(table);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 0, 0, 0));
        p.add(TournamentPanel.tableScroll(table), BorderLayout.CENTER);
        return p;
    }

    // ---------------------------------------------------------------- helpers

    private String noBracketText() {
        if (tournament.getStatus() == Tournament.Status.CANCELLED) {
            return "This tournament was cancelled because fewer than 2 teams registered before it started.";
        }
        return "The bracket is created automatically when the tournament starts ("
                + (tournament.getStartDateTime() == null ? "" : tournament.getStartDateTime().format(
                java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")))
                + "). At least 2 teams must be registered.";
    }

    private JLabel message(String text) {
        JLabel l = new JLabel("<html><div style='width:560px;'>" + text + "</div></html>");
        l.setFont(Theme.BASE);
        l.setVerticalAlignment(SwingConstants.TOP);
        l.setBorder(new EmptyBorder(16, 4, 0, 0));
        return l;
    }

    /** Runs a database action, shows its error (if any) and reloads the current tab. */
    private void run(Action action) {
        try {
            String error = action.run();
            if (error != null) JOptionPane.showMessageDialog(this, error, "Notice", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        showTab(currentTab);
    }

    private void deleteTournament() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete \"" + tournament.getTournamentName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            String error = service.delete(tournament);
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Cannot delete", JOptionPane.WARNING_MESSAGE);
                showTab(currentTab);
                return;
            }
            onDeleted.run();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}