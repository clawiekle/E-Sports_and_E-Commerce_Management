package gui.tournament;

import dsa.SinglyLinkedList;
import gui.Theme;
import model.Game;
import model.Tournament;
import service.TournamentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;

public class TournamentFormPanel extends JPanel {

    // ---- colors taken from the design
    private static final Color CARD_LEFT  = new Color(226, 224, 220);
    private static final Color CARD_RIGHT = new Color(224, 222, 218);
    private static final Color FIELD_BG   = new Color(200, 198, 194);
    private static final Color HINT       = new Color(90, 90, 90);

    /** Earliest pickable date: tomorrow. Use LocalDate.now() to also allow today. */
    private static LocalDate minDate() { return LocalDate.now().plusDays(1); }

    private final TournamentService service;
    private final Tournament existing;
    private final Runnable onDone;

    private final PlaceholderField nameField = new PlaceholderField("Enter tournament name");
    private final JComboBox<Game> gameBox = new JComboBox<>();
    private final JRadioButton[] teamButtons = new JRadioButton[Tournament.TEAM_OPTIONS.length];
    private final JRadioButton[] formatButtons = new JRadioButton[Tournament.BEST_OF_OPTIONS.length];
    private final DatePickerField dateField = new DatePickerField();
    private final JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(12, 1, 12, 1));
    private final JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
    private final JToggleButton amBtn = new JToggleButton("AM");
    private final JToggleButton pmBtn = new JToggleButton("PM");
    private final PlaceholderArea descArea = new PlaceholderArea("Enter tournament description...");
    private final JLabel errorLabel = new JLabel(" ");

    public TournamentFormPanel(TournamentService service, Tournament existing, Runnable onDone, Runnable onCancel) {
        this.service = service;
        this.existing = existing;
        this.onDone = onDone;
        boolean edit = existing != null;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.CREAM);
        setBorder(new EmptyBorder(25, 40, 25, 25));

        JLabel heading = new JLabel(edit ? "EDIT TOURNAMENT" : "CREATE TOURNAMENT");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(heading, BorderLayout.NORTH);

        // ---- two-column gray card
        RoundedPanel card = new RoundedPanel(CARD_LEFT, 14);
        card.setLayout(new GridLayout(1, 2));
        card.add(buildLeft());
        card.add(buildRight());

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(card, BorderLayout.CENTER);
        errorLabel.setForeground(new Color(170, 30, 30));
        errorLabel.setFont(Theme.BOLD);
        errorLabel.setBorder(new EmptyBorder(8, 4, 0, 0));
        center.add(errorLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // ---- Cancel / Create Tournament
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        buttons.setOpaque(false);
        JButton cancel = new Theme.Btn("Cancel", Theme.MAROON_LIGHT, Theme.INK);
        JButton submit = new Theme.Btn(edit ? "Save Changes" : "Create Tournament", Theme.MAROON, Color.WHITE);
        cancel.addActionListener(e -> onCancel.run());
        submit.addActionListener(e -> submit());
        buttons.add(cancel);
        buttons.add(submit);
        add(buttons, BorderLayout.SOUTH);

        loadGames();
        fill();
    }

    // ------------------------------------------------------------ left column

    private JPanel buildLeft() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD_LEFT);
        p.setBorder(new EmptyBorder(26, 30, 26, 30));

        // Tournament Name
        p.add(left(label("Tournament Name", true)));
        p.add(Box.createVerticalStrut(6));
        styleField(nameField);
        p.add(capHeight(nameField));
        p.add(Box.createVerticalStrut(24));

        // Select Game
        p.add(left(label("Select Game", true)));
        p.add(Box.createVerticalStrut(6));
        gameBox.setFont(Theme.BASE);
        gameBox.setBackground(FIELD_BG);
        gameBox.setForeground(Color.BLACK);
        gameBox.setPreferredSize(new Dimension(200, 40));
        gameBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                    int index, boolean isSelected, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                setForeground(isSelected ? Color.WHITE : Color.BLACK);
                setBackground(isSelected ? Theme.MAROON : Color.WHITE);
                return this;
            }
        });
        p.add(capHeight(gameBox));
        p.add(Box.createVerticalStrut(24));

        // Maximum Teams
        p.add(left(label("Maximum Teams", true)));
        p.add(Box.createVerticalStrut(6));
        JPanel teams = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        teams.setOpaque(false);
        ButtonGroup teamGroup = new ButtonGroup();
        for (int i = 0; i < teamButtons.length; i++) {
            teamButtons[i] = radio(String.valueOf(Tournament.TEAM_OPTIONS[i]), teamGroup);
            teams.add(teamButtons[i]);
        }
        p.add(capHeight(teams));
        p.add(Box.createVerticalStrut(24));

        // Match Format
        p.add(left(label("Match Format", true)));
        p.add(Box.createVerticalStrut(6));
        JPanel formats = new JPanel(new GridLayout(0, 1, 0, 6));
        formats.setOpaque(false);
        formats.setBorder(new EmptyBorder(0, 8, 0, 0));
        ButtonGroup formatGroup = new ButtonGroup();
        for (int i = 0; i < formatButtons.length; i++) {
            formatButtons[i] = radio("Best of " + Tournament.BEST_OF_OPTIONS[i], formatGroup);
            formats.add(formatButtons[i]);
        }
        p.add(capHeight(formats));

        p.add(Box.createVerticalGlue());   // pushes everything to the top
        return p;
    }

    // ----------------------------------------------------------- right column

    private JPanel buildRight() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD_RIGHT);
        p.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 1, 0, 0, new Color(205, 203, 199)),
                new EmptyBorder(26, 40, 26, 30)));

        // Time
        p.add(left(label("Time", true)));
        p.add(Box.createVerticalStrut(6));
        p.add(capHeight(buildTimeRow()));
        p.add(Box.createVerticalStrut(24));

        // Tournament Date
        p.add(left(label("Tournament Date", true)));
        p.add(Box.createVerticalStrut(6));
        p.add(capHeight(dateField));
        p.add(Box.createVerticalStrut(24));

        // Description
        p.add(left(optionalLabel("Description")));
        p.add(Box.createVerticalStrut(6));
        descArea.setFont(Theme.BASE);
        descArea.setBackground(FIELD_BG);
        descArea.setForeground(Color.BLACK);
        descArea.setCaretColor(Color.BLACK);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane sp = new JScrollPane(descArea);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setPreferredSize(new Dimension(200, 140));
        p.add(capHeight(sp));

        p.add(Box.createVerticalGlue());
        return p;
    }

    /** BoxLayout needs every child aligned the same way, or labels and fields drift sideways. */
    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    /** Full width, but never taller than its preferred height (stops BoxLayout from stretching it). */
    private static JComponent capHeight(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return left(c);
    }

    private JPanel buildTimeRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        styleTimeSpinner(hourSpinner);
        styleTimeSpinner(minuteSpinner);
        JLabel colon = new JLabel(":");
        colon.setFont(Theme.BOLD);
        colon.setForeground(Color.BLACK);

        ButtonGroup ampm = new ButtonGroup();
        for (JToggleButton b : new JToggleButton[]{amBtn, pmBtn}) {
            b.setFont(Theme.BOLD);
            b.setFocusPainted(false);
            b.setBackground(FIELD_BG);
            b.setForeground(Color.BLACK);
            b.setBorder(new EmptyBorder(8, 10, 8, 10));
            b.addChangeListener(e -> {
                b.setBackground(b.isSelected() ? Theme.MAROON : FIELD_BG);
                b.setForeground(b.isSelected() ? Color.WHITE : Color.BLACK);
            });
            ampm.add(b);
        }
        amBtn.setSelected(true);

        row.add(hourSpinner);
        row.add(colon);
        row.add(minuteSpinner);
        row.add(Box.createHorizontalStrut(6));
        row.add(amBtn);
        row.add(pmBtn);
        return row;
    }

    private static void styleTimeSpinner(JSpinner s) {
        s.setEditor(new JSpinner.NumberEditor(s, "00"));
        s.setFont(Theme.BASE);
        s.setPreferredSize(new Dimension(80, 40));
        JFormattedTextField tf = ((JSpinner.DefaultEditor) s.getEditor()).getTextField();
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setBackground(FIELD_BG);
        tf.setForeground(Color.BLACK);
    }

    // -------------------------------------------------------------------- data

    private void loadGames() {
        gameBox.addItem(new Game(0, "Select Game"));
        try {
            SinglyLinkedList games = service.getGames();
            for (int i = 0; i < games.size(); i++)
                gameBox.addItem((Game) games.get(i));   // cast Object back to Game
        } catch (SQLException ex) {
            errorLabel.setText("Could not load games: " + ex.getMessage());
        }
    }

    private void fill() {
        if (existing == null) return;   // create mode: everything starts empty, as in the design

        nameField.setText(existing.getTournamentName());
        for (int i = 0; i < gameBox.getItemCount(); i++)
            if (gameBox.getItemAt(i).getId() == existing.getGameID()) gameBox.setSelectedIndex(i);
        for (int i = 0; i < teamButtons.length; i++)
            if (Tournament.TEAM_OPTIONS[i] == existing.getMaxTeams()) teamButtons[i].setSelected(true);
        for (int i = 0; i < formatButtons.length; i++)
            if (Tournament.BEST_OF_OPTIONS[i] == existing.getBestOf()) formatButtons[i].setSelected(true);

        LocalDateTime start = existing.getStartDateTime();
        if (start != null) {
            dateField.setDate(start.toLocalDate());
            int h = start.getHour();
            hourSpinner.setValue(h % 12 == 0 ? 12 : h % 12);
            minuteSpinner.setValue(start.getMinute());
            (h >= 12 ? pmBtn : amBtn).setSelected(true);
        }
        descArea.setText(existing.getDescription());   // needs Tournament.getDescription()
    }

    private void submit() {
        errorLabel.setText(" ");

        if (nameField.getText().trim().isEmpty()) {
            errorLabel.setText("Please enter a tournament name.");
            return;
        }
        Game game = (Game) gameBox.getSelectedItem();
        if (game == null || game.getId() == 0) {
            errorLabel.setText("Please select a game.");
            return;
        }
        int maxTeams = selected(teamButtons, Tournament.TEAM_OPTIONS);
        if (maxTeams == 0) { errorLabel.setText("Please select the maximum number of teams."); return; }
        int bestOf = selected(formatButtons, Tournament.BEST_OF_OPTIONS);
        if (bestOf == 0)   { errorLabel.setText("Please select a match format."); return; }
        if (dateField.getDate() == null) {
            errorLabel.setText("Please pick a tournament date.");
            return;
        }
        if (dateField.getDate().isBefore(minDate()) && existing == null) {
            errorLabel.setText("Tournament date must be after today.");
            return;
        }

        Tournament t = new Tournament();
        if (existing != null) t.setTournamentID(existing.getTournamentID());
        t.setTournamentName(nameField.getText().trim());
        t.setGameID(game.getId());
        t.setMaxTeams(maxTeams);
        t.setBestOf(bestOf);
        t.setStartDateTime(readDateTime());
        t.setDescription(descArea.getText().trim());   // needs Tournament.setDescription(String)

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
        int h = (Integer) hourSpinner.getValue() % 12;      // 12 -> 0
        if (pmBtn.isSelected()) h += 12;
        int m = (Integer) minuteSpinner.getValue();
        return LocalDateTime.of(dateField.getDate(), LocalTime.of(h, m));
    }

    private static int selected(JRadioButton[] buttons, int[] values) {
        for (int i = 0; i < buttons.length; i++) if (buttons[i].isSelected()) return values[i];
        return 0;
    }

    private static JLabel label(String text, boolean required) {
        String html = "<html>" + text + (required
                ? " <font color='#C80000'>*</font>" : "") + "</html>";
        JLabel l = new JLabel(html);
        l.setFont(Theme.BOLD);
        l.setForeground(Color.BLACK);
        return l;
    }

    private static JLabel optionalLabel(String text) {
        JLabel l = new JLabel("<html>" + text + " <font color='#5A5A5A'>(optional)</font></html>");
        l.setFont(Theme.BOLD);
        l.setForeground(Color.BLACK);
        return l;
    }

    private static JRadioButton radio(String text, ButtonGroup group) {
        JRadioButton r = new JRadioButton(text);
        r.setOpaque(false);
        r.setFont(Theme.BASE);
        r.setForeground(Color.BLACK);
        r.setFocusPainted(false);
        group.add(r);
        return r;
    }

    private static void styleField(JTextField f) {
        f.setFont(Theme.BASE);
        f.setBackground(FIELD_BG);
        f.setForeground(Color.BLACK);
        f.setCaretColor(Color.BLACK);
        f.setBorder(new EmptyBorder(0, 12, 0, 12));
        f.setPreferredSize(new Dimension(200, 40));
    }

    // =========================================================== inner classes

    /** Rounded background panel. */
    private static class RoundedPanel extends JPanel {
        private final Color bg;
        private final int arc;
        RoundedPanel(Color bg, int arc) { this.bg = bg; this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public void paint(Graphics g) {
            // clip children (the two column panels) to the rounded shape
            Graphics2D g2 = (Graphics2D) g.create();
            g2.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
            super.paint(g2);
            g2.dispose();
        }
    }

    /** Text field that shows gray hint text while empty. */
    private static class PlaceholderField extends JTextField {
        private final String hint;
        PlaceholderField(String hint) { this.hint = hint; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) paintHint(g, this, hint, getInsets());
        }
    }

    /** Text area that shows gray hint text while empty. */
    private static class PlaceholderArea extends JTextArea {
        private final String hint;
        PlaceholderArea(String hint) { this.hint = hint; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) paintHint(g, this, hint, getInsets());
        }
    }

    private static void paintHint(Graphics g, JComponent comp, String hint, Insets in) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(HINT);
        g2.setFont(comp.getFont());
        FontMetrics fm = g2.getFontMetrics();
        int y = comp instanceof JTextArea
                ? in.top + fm.getAscent()
                : (comp.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(hint, in.left, y);
        g2.dispose();
    }

    /** "Pick a Date" field: click it (or the calendar icon) to open the calendar popup. */
    private static class DatePickerField extends JPanel {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        private final PlaceholderField display = new PlaceholderField("Pick a Date");
        private LocalDate date;

        DatePickerField() {
            setLayout(new BorderLayout());
            setBackground(FIELD_BG);
            setPreferredSize(new Dimension(200, 40));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            display.setEditable(false);
            display.setFont(Theme.BASE);
            display.setForeground(Color.BLACK);
            display.setBackground(FIELD_BG);
            display.setBorder(new EmptyBorder(0, 12, 0, 12));
            display.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel icon = new JLabel(new CalendarIcon());
            icon.setBorder(new EmptyBorder(0, 0, 0, 12));

            add(display, BorderLayout.CENTER);
            add(icon, BorderLayout.EAST);

            MouseAdapter open = new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { showPopup(); }
            };
            display.addMouseListener(open);
            icon.addMouseListener(open);
            addMouseListener(open);
        }

        LocalDate getDate() { return date; }

        void setDate(LocalDate d) {
            date = d;
            display.setText(d == null ? "" : d.format(FMT));
        }

        private void showPopup() {
            JPopupMenu popup = new JPopupMenu();
            popup.setOpaque(false);
            popup.setBorder(BorderFactory.createEmptyBorder());
            popup.add(new CalendarPanel(date, minDate(),
                    picked -> setDate(picked),
                    () -> popup.setVisible(false)));
            popup.show(this, 0, getHeight() + 2);   // use -getHeight() - 30 to overlay the field instead
        }
    }

    /** Month calendar styled like the design. Days before {@code min} are disabled. */
    private static class CalendarPanel extends JPanel {
        private static final Color FADED = new Color(200, 200, 200);

        private final LocalDate min;
        private final Consumer<LocalDate> onPick;
        private final Runnable onClose;
        private LocalDate pending;                 // day clicked but not yet confirmed with Done
        private YearMonth shown;

        private final JPanel grid = new JPanel(new GridLayout(0, 7, 0, 4));
        private final JLabel title = new JLabel("", SwingConstants.CENTER);
        private final NavButton prev = new NavButton("<");
        private final NavButton next = new NavButton(">");

        CalendarPanel(LocalDate selected, LocalDate min, Consumer<LocalDate> onPick, Runnable onClose) {
            this.min = min;
            this.onPick = onPick;
            this.onClose = onClose;
            this.pending = selected;
            this.shown = YearMonth.from(selected != null && !selected.isBefore(min) ? selected : min);

            setOpaque(false);
            setLayout(new BorderLayout(0, 8));
            setBorder(new EmptyBorder(16, 20, 12, 20));
            setPreferredSize(new Dimension(380, 330));

            // header: [<]  October 2026  [>]
            title.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            title.setForeground(Color.BLACK);
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    new EmptyBorder(0, 0, 10, 0)));
            header.add(prev, BorderLayout.WEST);
            header.add(title, BorderLayout.CENTER);
            header.add(next, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            grid.setOpaque(false);
            add(grid, BorderLayout.CENTER);

            // footer: [Done]
            JButton done = new Theme.Btn("Done", Theme.MAROON, Color.WHITE);
            done.addActionListener(e -> {
                if (pending != null) onPick.accept(pending);
                onClose.run();
            });
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            footer.setOpaque(false);
            footer.add(done);
            add(footer, BorderLayout.SOUTH);

            prev.addActionListener(e -> { shown = shown.minusMonths(1); rebuild(); });
            next.addActionListener(e -> { shown = shown.plusMonths(1); rebuild(); });
            rebuild();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(new Color(225, 225, 225));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
        }

        private void rebuild() {
            title.setText(shown.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + shown.getYear());
            prev.setEnabled(shown.isAfter(YearMonth.from(min)));   // cannot browse into past months

            grid.removeAll();
            for (String n : new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}) {
                JLabel l = new JLabel(n, SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                l.setForeground(new Color(130, 130, 130));
                l.setBorder(new EmptyBorder(6, 0, 6, 0));
                grid.add(l);
            }

            // faded days from the previous month (Sunday = first column)
            int offset = shown.atDay(1).getDayOfWeek().getValue() % 7;
            int prevLen = shown.minusMonths(1).lengthOfMonth();
            for (int i = 0; i < offset; i++) {
                JLabel l = new JLabel(String.valueOf(prevLen - offset + 1 + i), SwingConstants.CENTER);
                l.setFont(Theme.BASE);
                l.setForeground(FADED);
                grid.add(l);
            }

            for (int d = 1; d <= shown.lengthOfMonth(); d++) {
                LocalDate day = shown.atDay(d);
                boolean allowed = !day.isBefore(min);
                boolean isPicked = day.equals(pending);

                JButton b = new JButton(String.valueOf(d));
                b.setFont(Theme.BASE);
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setEnabled(allowed);
                b.setContentAreaFilled(false);
                b.setOpaque(isPicked);
                if (isPicked) {
                    b.setBackground(Theme.MAROON);
                    b.setForeground(Color.WHITE);
                } else {
                    b.setForeground(allowed ? Color.BLACK : FADED);
                }
                if (allowed) {
                    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    b.addActionListener(e -> { pending = day; rebuild(); });
                }
                grid.add(b);
            }
            grid.revalidate();
            grid.repaint();
        }

        /** Small rounded gray arrow button. */
        private static class NavButton extends JButton {
            NavButton(String text) {
                super(text);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setForeground(Color.BLACK);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setPreferredSize(new Dimension(32, 32));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override public void setEnabled(boolean b) {
                super.setEnabled(b);
                setForeground(b ? Color.BLACK : FADED);
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? new Color(238, 238, 238) : new Color(246, 246, 246));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        }
    }

    /** Small calendar icon drawn in code (no image file needed). */
    private static class CalendarIcon implements Icon {
        public int getIconWidth() { return 22; }
        public int getIconHeight() { return 22; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(x + 2, y + 4, 17, 15, 4, 4);
            g2.drawLine(x + 2, y + 9, x + 19, y + 9);
            g2.drawLine(x + 6, y + 2, x + 6, y + 5);
            g2.drawLine(x + 15, y + 2, x + 15, y + 5);
            g2.fillRect(x + 6, y + 12, 3, 2);
            g2.fillRect(x + 12, y + 12, 3, 2);
            g2.fillRect(x + 6, y + 15, 3, 2);
            g2.fillRect(x + 12, y + 15, 3, 2);
            g2.dispose();
        }
    }
}