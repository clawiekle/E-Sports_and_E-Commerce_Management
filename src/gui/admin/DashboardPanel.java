package gui.admin;

import dsa.SinglyLinkedList;
import gui.Theme;
import service.DashboardService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** ADMIN DASHBOARD: header with profile, four gray stat cards, today's tournaments and recent activity. */
public class DashboardPanel extends JPanel {

    private final DashboardService service = new DashboardService();

    private final JLabel teamsValue = valueLabel();
    private final JLabel tournamentsValue = valueLabel();
    private final JLabel matchesValue = valueLabel();
    private final JLabel merchValue = valueLabel();
    private final ListCard tournamentCard = new ListCard("Today's Tournaments", "No tournament has been created.");
    private final ListCard activityCard = new ListCard("Recent Activity", "No activity has been made yet.");

    public DashboardPanel() {
        setBackground(Theme.CREAM);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(0, 110));
        stats.add(statCard("Teams", teamsValue, "Total Registered"));
        stats.add(statCard("Tournaments", tournamentsValue, "Ongoing/Upcoming"));
        stats.add(statCard("Matches", matchesValue, "Today's Matches"));
        stats.add(statCard("Merchandise Sales", merchValue, "Total Sales"));
        body.add(stats, BorderLayout.NORTH);

        JPanel lists = new JPanel(new GridLayout(1, 2, 14, 0));
        lists.setOpaque(false);
        lists.add(tournamentCard);
        lists.add(activityCard);
        body.add(lists, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------- header

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("ADMIN DASHBOARD");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        JPanel profile = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        profile.setOpaque(false);
        profile.add(new ProfileAvatar());
        JLabel name = new JLabel("Admin");
        name.setFont(Theme.BASE);
        name.setForeground(Theme.TEXT_DARK);
        profile.add(name);
        header.add(profile, BorderLayout.EAST);

        return header;
    }

    // ---------------------------------------------------------------- cards

    private static JLabel valueLabel() {
        JLabel label = new JLabel("--", SwingConstants.CENTER);
        label.setFont(Theme.VALUE);
        label.setForeground(Theme.TEXT_DARK);
        return label;
    }

    private static JPanel statCard(String title, JLabel value, String caption) {
        Theme.Card card = new Theme.Card(Theme.CARD_GRAY, 8);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 10, 10, 10));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(Theme.BASE);
        t.setForeground(Theme.TEXT_DARK);
        card.add(t, BorderLayout.NORTH);

        card.add(value, BorderLayout.CENTER);

        JLabel c = new JLabel(caption, SwingConstants.CENTER);
        c.setFont(Theme.CAPTION);
        c.setForeground(Theme.TEXT_MUTED);
        card.add(c, BorderLayout.SOUTH);
        return card;
    }

    /** Gray card with a heading, a thin divider line and either a list or a centered "nothing yet" phrase. */
    private static class ListCard extends Theme.Card {
        private static final String EMPTY = "empty";
        private static final String LIST = "list";

        private final DefaultListModel<String> model = new DefaultListModel<>();
        private final JLabel emptyLabel = new JLabel("", SwingConstants.CENTER);
        private final CardLayout switcher = new CardLayout();
        private final JPanel center = new JPanel(switcher);
        private final String defaultEmptyText;

        ListCard(String heading, String emptyText) {
            super(Theme.CARD_GRAY, 8);
            this.defaultEmptyText = emptyText;
            setLayout(new BorderLayout(0, 10));
            setBorder(new EmptyBorder(16, 16, 16, 16));

            JLabel h = new JLabel(heading);
            h.setFont(Theme.BOLD);
            h.setForeground(Theme.TEXT_DARK);
            h.setBorder(new EmptyBorder(0, 0, 8, 0));

            JPanel line = new JPanel();
            line.setBackground(Theme.LINE_GRAY);
            line.setPreferredSize(new Dimension(0, 1));

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(h, BorderLayout.NORTH);
            top.add(line, BorderLayout.SOUTH);
            top.setBorder(new EmptyBorder(0, 6, 0, 6));
            add(top, BorderLayout.NORTH);

            emptyLabel.setFont(Theme.CAPTION);
            emptyLabel.setForeground(Theme.TEXT_MUTED);

            JList<String> list = new JList<>(model);
            list.setOpaque(false);
            list.setFont(Theme.BASE);
            list.setFocusable(false);
            list.setCellRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean focus) {
                    JLabel lbl = (JLabel) super.getListCellRendererComponent(l, v, i, false, false);
                    lbl.setOpaque(false);
                    lbl.setForeground(Theme.TEXT_DARK);
                    lbl.setBorder(new EmptyBorder(4, 6, 4, 6));
                    return lbl;
                }
            });
            JScrollPane scroll = new JScrollPane(list);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);

            center.setOpaque(false);
            center.add(emptyLabel, EMPTY);
            center.add(scroll, LIST);
            add(center, BorderLayout.CENTER);

            showEmpty(emptyText);
        }

        void showEmpty(String text) {
            emptyLabel.setText(text);
            switcher.show(center, EMPTY);
        }

        void setItems(SinglyLinkedList items) {
            model.clear();
            if (items == null || items.isEmpty()) {
                showEmpty(defaultEmptyText);
                return;
            }
            for (int i = 0; i < items.size(); i++) model.addElement((String) items.get(i));
            switcher.show(center, LIST);
        }
    }

    /** Light gray circle with a dark person silhouette, shown beside the "Admin" label. */
    private static class ProfileAvatar extends JComponent {
        ProfileAvatar() { setPreferredSize(new Dimension(40, 40)); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(0xCD, 0xCF, 0xD8));
            g2.fillOval(0, 0, w - 1, h - 1);
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, w - 1, h - 1));
            g2.setColor(Theme.TEXT_DARK);
            g2.fillOval(w / 2 - 7, 8, 14, 14);          // head
            g2.fillOval(w / 2 - 14, 24, 28, 24);        // shoulders
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------- data

    /** Reloads every number and list from the database (called each time the dashboard is shown). */
    public void refresh() {
        try {
            teamsValue.setText(String.valueOf(service.teamCount()));
            tournamentsValue.setText(String.valueOf(service.tournamentCount()));
            matchesValue.setText(String.valueOf(service.matchCount()));
            merchValue.setText("--"); // no sales table yet
            tournamentCard.setItems(service.tournamentSummaries(8));
            activityCard.setItems(service.recentActivity(8));
        } catch (SQLException ex) {
            String msg = "Could not load data: " + ex.getMessage();
            tournamentCard.showEmpty(msg);
            activityCard.showEmpty(msg);
        }
    }
}