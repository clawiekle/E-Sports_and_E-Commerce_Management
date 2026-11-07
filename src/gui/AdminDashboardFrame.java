package gui;

import gui.tournament.TournamentPanel;
import model.Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Admin shell: yellow sidebar on the left, the selected section on the right. */
public class AdminDashboardFrame extends JFrame {

    private static final String DASHBOARD = "dashboard";
    private static final String TEAMS = "teams";
    private static final String TOURNAMENTS = "tournaments";
    private static final String MATCHES = "matches";
    private static final String MERCH = "merch";
    private static final String REPORTS = "reports";

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final DashboardPanel dashboardPanel = new DashboardPanel();
    private final TournamentPanel tournamentPanel = new TournamentPanel();
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();
    private final Runnable onLogout;

    public AdminDashboardFrame(Admin admin, Runnable onLogout) {
        super("ESPORTS - Admin Dashboard");
        this.onLogout = onLogout;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 700);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        content.setBackground(Theme.CREAM);
        content.add(dashboardPanel, DASHBOARD);
        //content.add(teamPanel, TEAMS);
        content.add(tournamentPanel, TOURNAMENTS);
        content.add(new PlaceholderPanel("MATCH MANAGEMENT"), MATCHES);
        content.add(new PlaceholderPanel("MERCHANDISE MANAGEMENT"), MERCH);
        content.add(new PlaceholderPanel("REPORTS"), REPORTS);

        add(buildSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        showSection(DASHBOARD);
    }

    // ---------------------------------------------------------------- sidebar

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.MAROON);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(0, 14, 0, 14));

        sidebar.add(Box.createVerticalStrut(24));

        JLabel brand = new JLabel("ESPORTS");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brand.setForeground(Theme.INK);
        brand.setBorder(new EmptyBorder(0, 18, 0, 0));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);

        sidebar.add(Box.createVerticalStrut(18));

        JPanel profile = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        profile.setOpaque(false);
        profile.setAlignmentX(Component.LEFT_ALIGNMENT);
        profile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        profile.setBorder(new EmptyBorder(0, 8, 0, 0));
        profile.add(new Avatar());
        JLabel role = new JLabel("ADMIN");
        role.setFont(new Font("Segoe UI", Font.BOLD, 12));
        role.setForeground(new Color(255, 253, 208));
        profile.add(role);
        sidebar.add(profile);

        sidebar.add(Box.createVerticalStrut(8));

        JPanel line = new JPanel();
        line.setBackground(Theme.INK);
        line.setPreferredSize(new Dimension(190, 2));
        line.setMaximumSize(new Dimension(190, 2));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(line);

        sidebar.add(Box.createVerticalStrut(24));

        addNav(sidebar, DASHBOARD, "DASHBOARD");
        addNav(sidebar, TEAMS, "TEAM MANAGEMENT");
        addNav(sidebar, TOURNAMENTS, "TOURNAMENT MANAGEMENT");
        addNav(sidebar, MATCHES, "MATCH MANAGEMENT");
        addNav(sidebar, MERCH, "MERCHANDISE MANAGEMENT");
        addNav(sidebar, REPORTS, "REPORTS");

        sidebar.add(Box.createVerticalGlue());

        JLabel general = new JLabel("GENERAL");
        general.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        general.setForeground(new Color(132, 136, 132));
        general.setBorder(new EmptyBorder(0, 18, 6, 0));
        general.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(general);

        NavButton logout = new NavButton("LOGOUT");
        logout.addActionListener(e -> logout());
        sidebar.add(logout);

        sidebar.add(Box.createVerticalStrut(24));
        return sidebar;
    }

    private void addNav(JPanel sidebar, String key, String text) {
        NavButton button = new NavButton(text);
        button.addActionListener(e -> showSection(key));
        navButtons.put(key, button);
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(4));
    }

    private void showSection(String key) {
        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(key));
        }
        cards.show(content, key);

        // sections that show database data reload every time they are opened
        if (DASHBOARD.equals(key)) dashboardPanel.refresh();
        //if (TEAMS.equals(key)) teamPanel.refresh();
        if (TOURNAMENTS.equals(key)) tournamentPanel.refresh();
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Log out of the admin dashboard?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        onLogout.run(); // opens the login screen first so the app never runs without a window
        dispose();
    }

    // ---------------------------------------------------------------- widgets

    /** Flat sidebar item with hover and "current section" highlight. */
    private static class NavButton extends JButton {
        private boolean active = false;

        NavButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setHorizontalAlignment(LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(210, 42));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        }

        void setActive(boolean active) {
            this.active = active;
            setFont(getFont().deriveFont(active ? Font.BOLD : Font.PLAIN));
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            if (active) {
                g2.setColor(Theme.MAROON_DARK);
                g2.fillRoundRect(0, 0, w, h, 14, 14);
                g2.setColor(Theme.INK);
                g2.fillRoundRect(0, 8, 4, h - 16, 4, 4);
            } else if (getModel().isRollover()) {
                g2.setColor(Theme.MAROON_LIGHT);
                g2.fillRoundRect(0, 0, w, h, 14, 14);
            }

            g2.setColor(Theme.INK);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 18, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Black circle avatar next to the ADMIN label. */
    private static class Avatar extends JComponent {
        Avatar() { setPreferredSize(new Dimension(32, 32)); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.INK);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.dispose();
        }
    }
}