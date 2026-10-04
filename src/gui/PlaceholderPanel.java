package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Stand-in for sections that aren't built yet. */
public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String title) {
        setBackground(Theme.GREY);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.TITLE);
        heading.setForeground(new Color(0, 0, 0));
        add(heading, BorderLayout.NORTH);

        Theme.Card card = new Theme.Card(Theme.YELLOW, 20);
        card.setLayout(new GridBagLayout());
        JLabel note = new JLabel("This section is coming next.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        note.setForeground(Theme.INK);
        card.add(note);
        add(card, BorderLayout.CENTER);
    }
}