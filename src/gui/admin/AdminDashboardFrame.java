package gui.admin;

import gui.PlaceholderPanel;
import gui.Theme;
import gui.tournament.TournamentPanel;
import model.Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/** Admin shell: dark maroon sidebar with the logo on the left, the selected section on the right. */
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
        sidebar.setBorder(new EmptyBorder(0, 12, 0, 12));

        sidebar.add(Box.createVerticalStrut(24));
        sidebar.add(buildBrand());
        sidebar.add(Box.createVerticalStrut(28));

        addNav(sidebar, DASHBOARD, "Dashboard", "dashboard");
        addNav(sidebar, TOURNAMENTS, "Tournament Management", "tournament");
        addNav(sidebar, TEAMS, "Team Management", "team");
        addNav(sidebar, MATCHES, "Match Management", "match");
        addNav(sidebar, MERCH, "Merchandise Management", "merch");
        addNav(sidebar, REPORTS, "Reports", "reports");

        sidebar.add(Box.createVerticalGlue());

        JLabel general = new JLabel("GENERAL");
        general.setFont(Theme.CAPTION);
        general.setForeground(Theme.SIDEBAR_MUTED);
        general.setBorder(new EmptyBorder(0, 18, 6, 0));
        general.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(general);

        NavButton logout = new NavButton("Logout", "logout");
        logout.addActionListener(e -> logout());
        sidebar.add(logout);

        sidebar.add(Box.createVerticalStrut(24));
        return sidebar;
    }

    /** Logo on the left, "E-SPORTS" with "ADMIN SYSTEM" underneath. */
    private JPanel buildBrand() {
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        brand.add(new Logo());

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel name = new JLabel("E-SPORTS");
        name.setFont(Theme.BRAND);
        name.setForeground(Theme.INK);

        JLabel sub = new JLabel("ADMIN SYSTEM");
        sub.setFont(Theme.SMALL);
        sub.setForeground(Theme.INK);

        text.add(name);
        text.add(sub);
        brand.add(text);
        return brand;
    }

    private void addNav(JPanel sidebar, String key, String text, String iconName) {
        NavButton button = new NavButton(text, iconName);
        button.addActionListener(e -> showSection(key));
        navButtons.put(key, button);
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(6));
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

    /** Loads /images/<name>.png from the classpath, scaled to the given size; null when the file is missing. */
    private static Image loadImage(String name, int w, int h) {
        URL url = AdminDashboardFrame.class.getResource("/images/" + name + ".png");
        if (url == null) return null;
        return new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    /** Flat sidebar item with an optional icon, hover and "current section" highlight. */
    private static class NavButton extends JButton {
        private static final int ICON_SIZE = 20;
        private final Image icon;
        private boolean active = false;

        NavButton(String text, String iconName) {
            super(text);
            this.icon = loadImage(iconName, ICON_SIZE, ICON_SIZE);
            setFont(Theme.NAV);
            setHorizontalAlignment(LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(210, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            if (active) {
                g2.setColor(Theme.MAROON_DEEP);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
            } else if (getModel().isRollover()) {
                g2.setColor(Theme.MAROON_DARK);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
            }

            int textX = 18;
            if (icon != null) {
                g2.drawImage(icon, 14, (h - ICON_SIZE) / 2, null);
                textX = 14 + ICON_SIZE + 10;
            }
            g2.setColor(Theme.INK);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), textX, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Top-left logo: uses /images/logo.png when present, otherwise draws a simple trophy badge. */
    private static class Logo extends JComponent {
        private final Image image = loadImage("logo", 40, 46);

        Logo() { setPreferredSize(new Dimension(42, 48)); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (image != null) {
                g2.drawImage(image, 1, 1, null);
            } else {
                g2.setColor(Theme.INK);
                g2.fillRoundRect(8, 6, 26, 20, 8, 8);   // cup
                g2.fillRect(19, 26, 4, 10);              // stem
                g2.fillRoundRect(11, 36, 20, 6, 4, 4);   // base
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawArc(2, 8, 12, 12, 90, 180);       // handles
                g2.drawArc(28, 8, 12, 12, -90, 180);
            }
            g2.dispose();
        }
    }
}