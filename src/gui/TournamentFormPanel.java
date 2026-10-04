package gui;

import dsa.SinglyLinkedList;
import model.Game;
import model.Tournament;
import service.TournamentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/** Create Tournament screen (also used to edit a tournament that hasn't begun yet). */
public class TournamentFormPanel extends JPanel {

    private final TournamentService service;
    private final Tournament existing;
    private final Runnable onDone;

    private final JTextField nameField = new JTextField();
    private final JComboBox<Game> gameBox = new JComboBox<>();
    private final JRadioButton[] teamButtons = new JRadioButton[Tournament.TEAM_OPTIONS.length];
    private final JRadioButton[] formatButtons = new JRadioButton[Tournament.BEST_OF_OPTIONS.length];
    private final JSpinner dateSpinner;
    private final JSpinner timeSpinner;
    private final JLabel errorLabel = new JLabel(" ");

    public TournamentFormPanel(TournamentService service, Tournament existing, Runnable onDone, Runnable onCancel) {
        this.service = service;
        this.existing = existing;
        this.onDone = onDone;
        boolean edit = existing != null;

        LocalDateTime start = edit && existing.getStartDateTime() != null
                ? existing.getStartDateTime()
                : LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        dateSpinner = new JSpinner(new SpinnerDateModel(toDate(start), null, null, Calendar.DAY_OF_MONTH));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        timeSpinner = new JSpinner(new SpinnerDateModel(toDate(start), null, null, Calendar.MINUTE));
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "hh:mm a"));

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.GREY);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel(edit ? "Edit Tournament" : "Create Tournament");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(heading, BorderLayout.NORTH);

        // ---- yellow card: left = name / game / max teams, right = match format box
        Theme.Card card = new Theme.Card(Theme.YELLOW, 24);
        card.setLayout(new GridLayout(1, 2, 30, 0));
        card.setBorder(new EmptyBorder(26, 28, 26, 28));
        card.add(buildLeft());
        card.add(buildRight());

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(card, BorderLayout.NORTH);
        errorLabel.setForeground(new Color(170, 30, 30));
        errorLabel.setFont(Theme.BOLD);
        errorLabel.setBorder(new EmptyBorder(10, 4, 0, 0));
        center.add(errorLabel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ---- Cancel / Create Tournament
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton cancel = new Theme.Btn("Cancel", Theme.YELLOW_LIGHT, Theme.INK);
        JButton submit = new Theme.Btn(edit ? "Save Changes" : "Create Tournament", Theme.YELLOW, Theme.INK);
        cancel.addActionListener(e -> onCancel.run());
        submit.addActionListener(e -> submit());
        buttons.add(cancel);
        buttons.add(submit);
        add(buttons, BorderLayout.SOUTH);

        loadGames();
        fill();
    }

    private JPanel buildLeft() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 4, 0);

        int y = 0;
        c.gridy = y++; p.add(label("Tournament Name"), c);
        styleField(nameField);
        c.gridy = y++; c.insets = new Insets(0, 0, 18, 0); p.add(nameField, c);

        c.insets = new Insets(0, 0, 4, 0);
        c.gridy = y++; p.add(label("Select Game"), c);
        gameBox.setFont(Theme.BASE);
        gameBox.setPreferredSize(new Dimension(200, 36));
        c.gridy = y++; c.insets = new Insets(0, 0, 18, 0); p.add(gameBox, c);

        c.insets = new Insets(0, 0, 4, 0);
        c.gridy = y++; p.add(label("Maximum Teams"), c);
        JPanel teams = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        teams.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < teamButtons.length; i++) {
            teamButtons[i] = radio(String.valueOf(Tournament.TEAM_OPTIONS[i]), group);
            teams.add(teamButtons[i]);
        }
        c.gridy = y; p.add(teams, c);
        return p;
    }

    private JPanel buildRight() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setOpaque(false);
        box.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.INK, 1, true), new EmptyBorder(14, 18, 14, 18)));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 4, 0);

        int y = 0;
        c.gridy = y++; box.add(label("Match Format"), c);
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < formatButtons.length; i++) {
            formatButtons[i] = radio("Best of " + Tournament.BEST_OF_OPTIONS[i], group);
            c.gridy = y++; box.add(formatButtons[i], c);
        }

        c.insets = new Insets(16, 0, 4, 0);
        c.gridy = y++; box.add(label("Tournament Date"), c);
        dateSpinner.setFont(Theme.BASE);
        c.insets = new Insets(0, 0, 8, 0);
        c.gridy = y++; box.add(dateSpinner, c);
        c.insets = new Insets(0, 0, 4, 0);
        c.gridy = y++; box.add(label("Start Time"), c);
        timeSpinner.setFont(Theme.BASE);
        c.gridy = y; box.add(timeSpinner, c);
        return box;
    }

    // -------------------------------------------------------------------- data

    private void loadGames() {
        gameBox.addItem(new Game(0, "-- Select game --"));
        try {
            SinglyLinkedList<Game> games = service.getGames();
            for (Game g : games) gameBox.addItem(g);
        } catch (SQLException ex) {
            errorLabel.setText("Could not load games: " + ex.getMessage());
        }
    }

    private void fill() {
        if (existing == null) {
            teamButtons[1].setSelected(true);   // 8 teams
            formatButtons[1].setSelected(true); // Best of 3
            return;
        }
        nameField.setText(existing.getTournamentName());
        for (int i = 0; i < gameBox.getItemCount(); i++) {
            if (gameBox.getItemAt(i).getId() == existing.getGameID()) gameBox.setSelectedIndex(i);
        }
        for (int i = 0; i < teamButtons.length; i++) {
            if (Tournament.TEAM_OPTIONS[i] == existing.getMaxTeams()) teamButtons[i].setSelected(true);
        }
        for (int i = 0; i < formatButtons.length; i++) {
            if (Tournament.BEST_OF_OPTIONS[i] == existing.getBestOf()) formatButtons[i].setSelected(true);
        }
    }

    private void submit() {
        Game game = (Game) gameBox.getSelectedItem();
        Tournament t = new Tournament();
        if (existing != null) t.setTournamentID(existing.getTournamentID());
        t.setTournamentName(nameField.getText());
        t.setGameID(game == null ? 0 : game.getId());
        t.setMaxTeams(selected(teamButtons, Tournament.TEAM_OPTIONS));
        t.setBestOf(selected(formatButtons, Tournament.BEST_OF_OPTIONS));
        t.setStartDateTime(readDateTime());

        try {
            String error = (existing == null) ? service.create(t) : service.update(t);
            if (error != null) {
                errorLabel.setText(error);
                return;
            }
            onDone.run();
        } catch (SQLException ex) {
            errorLabel.setText("Database error: " + ex.getMessage());
        }
    }

    // ----------------------------------------------------------------- helpers

    private LocalDateTime readDateTime() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate date = ((Date) dateSpinner.getValue()).toInstant().atZone(zone).toLocalDate();
        LocalTime time = ((Date) timeSpinner.getValue()).toInstant().atZone(zone).toLocalTime()
                .withSecond(0).withNano(0);
        return LocalDateTime.of(date, time);
    }

    private static Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static int selected(JRadioButton[] buttons, int[] values) {
        for (int i = 0; i < buttons.length; i++) if (buttons[i].isSelected()) return values[i];
        return 0;
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BOLD);
        l.setForeground(Theme.INK);
        return l;
    }

    private static JRadioButton radio(String text, ButtonGroup group) {
        JRadioButton r = new JRadioButton(text);
        r.setOpaque(false);
        r.setFont(Theme.BASE);
        r.setForeground(Theme.INK);
        r.setFocusPainted(false);
        group.add(r);
        return r;
    }

    private static void styleField(JTextField f) {
        f.setFont(Theme.BASE);
        f.setBackground(Color.WHITE);
        f.setForeground(Color.BLACK);
        f.setCaretColor(Color.BLACK);
        f.setPreferredSize(new Dimension(200, 36));
    }
}