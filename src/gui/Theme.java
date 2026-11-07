package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public final class Theme {
    private Theme() {}

    public static final Color MAROON = new Color(0x49, 0x08, 0x17);        // maroon #490817
    public static final Color MAROON_DARK = new Color(0x6E, 0x1A, 0x2E);   // active / header shade
    public static final Color MAROON_LIGHT = new Color(0x8A, 0x2B, 0x42);  // hover shade
    public static final Color CREAM = new Color(0xFF, 0xF8, 0xE7);          // cream #FFF8E7
    public static final Color INK = new Color(0xFF, 0xF8, 0xE7);           // cream text on maroon

    public static final Font BASE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 24);

    /** White rounded-outline look for text fields. */
    public static void styleInput(JComponent c) {
        c.setFont(BASE);
        c.setBackground(Color.WHITE);
        c.setForeground(INK);
        c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(INK, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
    }

    /** Rounded flat panel (the yellow cards). */
    public static class Card extends JPanel {
        private final Color fill;
        private final int arc;

        public Card(Color fill, int arc) {
            this.fill = fill;
            this.arc = arc;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
        }
    }

    /** Rounded flat button with hover / pressed / disabled states. */
    public static class Btn extends JButton {
        private final Color base;
        private final Color textColor;

        public Btn(String text, Color base, Color textColor) {
            super(text);
            this.base = base;
            this.textColor = textColor;
            setFont(BOLD);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override public Dimension getPreferredSize() {
            if (isPreferredSizeSet()) return super.getPreferredSize();
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 36, 38);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color fill = base;
            if (!isEnabled()) fill = new Color(90, 90, 90, 90);
            else if (getModel().isPressed()) fill = base.darker();
            else if (getModel().isRollover()) fill = new Color(Math.min(255, base.getRed() + 45),
                    Math.min(255, base.getGreen() + 45), Math.min(255, base.getBlue() + 45));

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

            g2.setColor(isEnabled() ? textColor : new Color(60, 60, 60, 150));
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
}