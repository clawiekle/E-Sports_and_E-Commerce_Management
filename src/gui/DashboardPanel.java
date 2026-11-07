package gui;

import dsa.SinglyLinkedList;
import service.DashboardService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** ADMIN DASHBOARD: four stat cards, the latest tournaments and the recent activity feed. */
public class DashboardPanel extends JPanel {

    private final DashboardService service = new DashboardService();

    private final JLabel teamsValue = valueLabel();
    private final JLabel tournamentsValue = valueLabel();
    private final JLabel matchesValue = valueLabel();
    private final JLabel merchValue = valueLabel();
    private final DefaultListModel<String> tournamentModel = new DefaultListModel<>();
    private final DefaultListModel<String> activityModel = new DefaultListModel<>();

    public DashboardPanel() {
        setBackground(Theme.CREAM);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("ADMIN DASHBOARD");
        title.setFont(Theme.TITLE);
        title.setForeground(new Color(0, 0, 0));
        add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(0, 125));
        stats.add(statCard("Teams", teamsValue));
        stats.add(statCard("Tournament", tournamentsValue));
        stats.add(statCard("Matches", matchesValue));
        stats.add(statCard("Merchandise Sales", merchValue));
        body.add(stats, BorderLayout.NORTH);

        JPanel lists = new JPanel(new GridLayout(1, 2, 14, 0));
        lists.setOpaque(false);
        lists.add(listCard("Today's Tournament", tournamentModel));
        lists.add(listCard("Recent Activity", activityModel));
        body.add(lists, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
    }

    private static JLabel valueLabel() {
        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 36));
        label.setForeground(Theme.INK);
        return label;
    }

    private static JPanel statCard(String title, JLabel value) {
        Theme.Card card = new Theme.Card(Theme.MAROON, 20);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 10, 10, 10));
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        t.setForeground(Theme.INK);
        card.add(t, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private static JPanel listCard(String heading, DefaultListModel<String> model) {
        Theme.Card card = new Theme.Card(Theme.MAROON, 20);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel h = new JLabel(heading);
        h.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        h.setForeground(Theme.INK);
        card.add(h, BorderLayout.NORTH);

        JList<String> list = new JList<>(model);
        list.setOpaque(false);
        list.setFont(Theme.BASE);
        list.setFocusable(false);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean focus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(l, v, i, false, false);
                lbl.setOpaque(false);
                lbl.setForeground(Theme.INK);
                lbl.setBorder(new EmptyBorder(3, 0, 3, 0));
                return lbl;
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /** Reloads every number and list from the database (called each time the dashboard is shown). */
    public void refresh() {
        try {
            teamsValue.setText(String.valueOf(service.teamCount()));
            tournamentsValue.setText(String.valueOf(service.tournamentCount()));
            matchesValue.setText(String.valueOf(service.matchCount()));
            merchValue.setText("-"); // no sales table yet
            fill(tournamentModel, service.tournamentSummaries(8), "No tournaments yet.");
            fill(activityModel, service.recentActivity(8), "No activity yet.");
        } catch (SQLException ex) {
            String msg = "Could not load data: " + ex.getMessage();
            fill(tournamentModel, single(msg), msg);
            fill(activityModel, single(msg), msg);
        }
    }

    private static SinglyLinkedList single(String text) {
        SinglyLinkedList list = new SinglyLinkedList();
        list.add(text);
        return list;
    }

    private static void fill(DefaultListModel<String> model, SinglyLinkedList items, String emptyText) {
        model.clear();
        if (items.isEmpty()) {
            model.addElement(emptyText);
            return;
        }
        for (int i = 0; i < items.size(); i++) model.addElement((String) items.get(i));
    }
}