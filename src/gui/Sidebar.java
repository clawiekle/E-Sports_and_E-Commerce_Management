package com.esports.gui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reusable left navigation sidebar shared by every Admin screen, matching
 * the mockup: yellow background, "ESPORTS" brand, admin badge, nav list
 * with the current screen highlighted, and a Logout link at the bottom.
 */
public class Sidebar extends JPanel {

    private static final Color YELLOW = new Color(0xF4, 0xCB, 0x4A);
    private static final Color YELLOW_DARK = new Color(0xE3, 0xB8, 0x30);
    private static final String[] NAV_ITEMS = {
            "DASHBOARD", "TEAM MANAGEMENT", "TOURNAMENT MANAGEMENT",
            "MATCH MANAGEMENT", "MERCHANDISE MANAGEMENT", "REPORTS"
    };

    private final Map<String, JLabel> navLabels = new LinkedHashMap<>();

    /**
     * @param activeItem   one of NAV_ITEMS, highlighted as the current screen
     * @param adminName    shown next to the admin badge
     * @param onNavigate   called with the clicked nav item's label
     * @param onLogout     called when "LOGOUT" is clicked
     */
    public Sidebar(String activeItem, String adminName, Consumer<String> onNavigate, Runnable onLogout) {
        setLayout(new BorderLayout());
        setBackground(YELLOW);
        setPreferredSize(new Dimension(220, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("ESPORTS");
        brand.setFont(new Font("SansSerif", Font.BOLD, 20));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(brand);
        top.add(Box.createVerticalStrut(16));

        JPanel adminRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        adminRow.setOpaque(false);
        adminRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badge = new JLabel("\u25CF");
        badge.setFont(new Font("SansSerif", Font.PLAIN, 22));
        JLabel adminLabel = new JLabel(adminName.toUpperCase());
        adminLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        adminRow.add(badge);
        adminRow.add(adminLabel);
        top.add(adminRow);
        top.add(Box.createVerticalStrut(4));

        JSeparator separator = new JSeparator();
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        separator.setMaximumSize(new Dimension(160, 2));
        top.add(separator);
        top.add(Box.createVerticalStrut(20));

        for (String item : NAV_ITEMS) {
            JLabel navLabel = buildNavLabel(item, item.equals(activeItem));
            navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            navLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    onNavigate.accept(item);
                }
            });
            navLabels.put(item, navLabel);
            top.add(navLabel);
            top.add(Box.createVerticalStrut(14));
        }

        add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        JLabel generalLabel = new JLabel("GENERAL");
        generalLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        generalLabel.setForeground(new Color(120, 100, 40));
        generalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(generalLabel);
        bottom.add(Box.createVerticalStrut(8));

        JLabel logoutLabel = new JLabel("LOGOUT");
        logoutLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        logoutLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                onLogout.run();
            }
        });
        bottom.add(logoutLabel);

        add(bottom, BorderLayout.SOUTH);
    }

    private JLabel buildNavLabel(String text, boolean active) {
        JLabel label = new JLabel((active ? "\u2590 " : "   ") + text);
        label.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 13));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setOpaque(active);
        if (active) {
            label.setBackground(YELLOW_DARK);
        }
        return label;
    }
}