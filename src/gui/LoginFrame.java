package gui;

import model.User;
import service.AuthService;
import service.LoginResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.function.Consumer;


public class LoginFrame extends JFrame {

    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 15);
    private static final Color ERROR_COLOR = new Color(255, 0, 0);
    private static final Color SUCCESS_COLOR = new Color(170, 255, 200);

    private final AuthService authService = new AuthService();
    private final Consumer<User> onSuccess;

    private final GlassPanel card = new GlassPanel();
    private final JLabel title = new JLabel("Login", SwingConstants.CENTER);
    private final JLabel subtitle = new JLabel("E-Sports & E-Commerce Management", SwingConstants.CENTER);
    private final RoundedField usernameField = new RoundedField("Username", false);
    private final RoundedField passwordField = new RoundedField("Password", true);
    private final RoundedField confirmField = new RoundedField("Confirm password", true);
    private final JPanel options = new JPanel(new BorderLayout());
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);
    private final PillButton submitBtn = new PillButton("Log in");
    private final JLabel switchAsk = new JLabel("Don't have an account?");
    private final JLabel switchLink = new JLabel("Register");

    private boolean registerMode = false;

    public LoginFrame(Consumer<User> onSuccess) {
        super("E-Sports & E-Commerce Management - Login");
        this.onSuccess = onSuccess;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 720);
        setMinimumSize(new Dimension(560, 690));
        setLocationRelativeTo(null);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(Color.BLACK);
        buildCard();
        background.add(card);
        setContentPane(background);

        setMode(false);
        submitBtn.addActionListener(e -> submit());
        getRootPane().setDefaultButton(submitBtn);
    }

    // ---------------------------------------------------------------- layout

    private void buildCard() {
        card.setPreferredSize(new Dimension(440, 640));
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(30, 45, 30, 45));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 4, 0);
        card.add(title, c);

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(255, 255, 255, 190));
        c.gridy = 1;
        c.insets = new Insets(0, 0, 32, 0);
        card.add(subtitle, c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 22, 0);
        card.add(usernameField, c);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 18, 0);
        card.add(passwordField, c);

        c.gridy = 4; // only visible in Register mode
        card.add(confirmField, c);

        JLabel forgot = new JLabel("Forgot Password?");
        forgot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        forgot.setForeground(Color.WHITE);
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this, "Please contact an administrator to reset your password.");
            }
            @Override public void mouseEntered(MouseEvent e) { forgot.setForeground(new Color(255, 190, 235)); }
            @Override public void mouseExited(MouseEvent e) { forgot.setForeground(Color.WHITE); }
        });

        options.setOpaque(false);
        options.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        options.add(forgot, BorderLayout.EAST);
        c.gridy = 5; // only visible in Login mode
        c.insets = new Insets(0, 0, 10, 0);
        card.add(options, c);

        messageLabel.setFont(FONT);
        messageLabel.setForeground(ERROR_COLOR);
        c.gridy = 6;
        c.insets = new Insets(0, 0, 14, 0);
        card.add(messageLabel, c);

        c.gridy = 7;
        c.insets = new Insets(0, 0, 22, 0);
        card.add(submitBtn, c);

        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchRow.setOpaque(false);
        switchAsk.setFont(FONT);
        switchAsk.setForeground(new Color(255, 255, 255, 200));
        switchLink.setFont(FONT_BOLD);
        switchLink.setForeground(Color.WHITE);
        switchLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switchLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { setMode(!registerMode); }
            @Override public void mouseEntered(MouseEvent e) { switchLink.setForeground(new Color(255, 190, 235)); }
            @Override public void mouseExited(MouseEvent e) { switchLink.setForeground(Color.WHITE); }
        });
        switchRow.add(switchAsk);
        switchRow.add(switchLink);
        c.gridy = 8;
        c.insets = new Insets(0, 0, 0, 0);
        card.add(switchRow, c);
    }

    private void setMode(boolean register) {
        registerMode = register;
        title.setText(register ? "Register" : "Login");
        subtitle.setText(register ? "Create your fan account" : "E-Sports & E-Commerce Management");
        setTitle("E-Sports & E-Commerce Management - " + (register ? "Register" : "Login"));
        confirmField.setVisible(register);
        confirmField.setText("");
        options.setVisible(!register);
        submitBtn.setText(register ? "Register" : "Log in");
        switchAsk.setText(register ? "Already have an account?" : "Don't have an account?");
        switchLink.setText(register ? "Login" : "Register");
        showMessage("", true);
        card.revalidate();
        card.repaint();
    }

    // ----------------------------------------------------------------- logic

    private void showMessage(String text, boolean error) {
        messageLabel.setForeground(error ? ERROR_COLOR : SUCCESS_COLOR);
        messageLabel.setText(text.isEmpty() ? " " : text);
    }

    private void submit() {
        if (registerMode) attemptRegister();
        else attemptLogin();
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Enter your username and password.", true);
            return;
        }

        submitBtn.setEnabled(false);
        try {
            LoginResult result = authService.login(username, password);

            switch (result.getStatus()) {
                case USER_NOT_FOUND:
                    // NO -> Account not found -> Register (typed username and password are kept)
                    setMode(true);
                    showMessage("Account not found. Please register.", true);
                    confirmField.requestFocusInWindow();
                    break;

                case WRONG_PASSWORD:
                    // Wrong -> Try again
                    showMessage("Incorrect password. Try again.", true);
                    passwordField.setText("");
                    passwordField.requestFocusInWindow();
                    break;

                case SUCCESS:
                    // Correct -> Check role (Main opens the Admin UI or the Fan UI)
                    dispose();
                    onSuccess.accept(result.getUser());
                    break;
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        } finally {
            submitBtn.setEnabled(true);
        }
    }

    private void attemptRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showMessage("Fill in all the fields.", true);
            return;
        }
        if (!password.equals(confirm)) {
            showMessage("Passwords do not match.", true);
            confirmField.setText("");
            return;
        }

        submitBtn.setEnabled(false);
        try {
            String error = authService.registerFan(username, password);
            if (error != null) {
                showMessage(error, true);
                return;
            }
            setMode(false);
            passwordField.setText("");
            showMessage("Account created! You can now log in.", false);
            passwordField.requestFocusInWindow();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        } finally {
            submitBtn.setEnabled(true);
        }
    }

    private void showDatabaseError(SQLException ex) {
        showMessage("", true);
        JOptionPane.showMessageDialog(this,
                "Could not reach the database. Is MySQL running and has DatabaseConnection been run once?\n\n" + ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------------------------------- custom widgets


    private static class GlassPanel extends JPanel {
        GlassPanel() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            Shape shape = new RoundRectangle2D.Float(1, 1, w - 3, h - 3, 44, 44);


            g2.setPaint(new RadialGradientPaint(new Point(w / 2, (int) (h * 0.32)), Math.max(w, h) * 0.85f,
                    new float[]{0f, 0.55f, 1f},
                    new Color[]{new Color(205, 90, 165, 170), new Color(125, 60, 175, 130), new Color(60, 30, 120, 105)}));
            g2.fill(shape);


            g2.setClip(shape);
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 55), 0, h * 0.5f, new Color(255, 255, 255, 0)));
            g2.fillRect(0, 0, w, h / 2);
            g2.setClip(null);

            // edge
            g2.setStroke(new BasicStroke(1.6f));
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 190), 0, h, new Color(255, 255, 255, 90)));
            g2.draw(shape);
            g2.dispose();
        }
    }


    private static class RoundedField extends JPasswordField {
        private final String placeholder;
        private final boolean secret;
        private boolean revealed = false;

        RoundedField(String placeholder, boolean secret) {
            this.placeholder = placeholder;
            this.secret = secret;
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 16));
            setForeground(new Color(45, 45, 55));
            setCaretColor(new Color(45, 45, 55));
            setSelectionColor(new Color(200, 160, 230));
            setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 52));
            setEchoChar(secret ? '\u2022' : (char) 0);
            setPreferredSize(new Dimension(300, 52));

            if (secret) { // the eye icon toggles show / hide
                MouseAdapter eyeHandler = new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        if (overEye(e)) {
                            revealed = !revealed;
                            setEchoChar(revealed ? (char) 0 : '\u2022');
                            repaint();
                        }
                    }
                    @Override public void mouseMoved(MouseEvent e) {
                        setCursor(Cursor.getPredefinedCursor(overEye(e) ? Cursor.HAND_CURSOR : Cursor.TEXT_CURSOR));
                    }
                };
                addMouseListener(eyeHandler);
                addMouseMotionListener(eyeHandler);
            }
        }

        private boolean overEye(MouseEvent e) {
            return e.getX() >= getWidth() - 50 && e.getX() <= getWidth() - 8;
        }

        @Override public void setEchoChar(char c) {
            super.setEchoChar(secret ? c : (char) 0); // username field is never masked
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            boolean focus = isFocusOwner();

            // soft drop shadow, then the light-grey body
            g2.setColor(new Color(0, 0, 0, 45));
            g2.fillRoundRect(1, 3, w - 2, h - 4, 22, 22);
            g2.setColor(new Color(244, 244, 247));
            g2.fillRoundRect(0, 0, w - 1, h - 3, 22, 22);

            // focus ring
            g2.setStroke(new BasicStroke(focus ? 2.2f : 1f));
            g2.setColor(focus ? new Color(230, 110, 190) : new Color(255, 255, 255, 160));
            g2.drawRoundRect(1, 1, w - 3, h - 5, 22, 22);

            super.paintComponent(g2); // the typed text

            if (getPassword().length == 0) {
                g2.setColor(new Color(120, 120, 135));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (h - 3 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, getInsets().left, y);
            }

            // icon on the right
            g2.setColor(new Color(70, 70, 85));
            int ix = w - 42, iy = (h - 3) / 2;
            if (secret) drawEye(g2, ix, iy, revealed); else drawPerson(g2, ix + 4, iy);
            g2.dispose();
        }

        private void drawEye(Graphics2D g2, int x, int cy, boolean open) {
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            Color ink = new Color(70, 70, 85);
            g2.setColor(ink);

            // almond outline
            Path2D eye = new Path2D.Float();
            eye.moveTo(x, cy);
            eye.curveTo(x + 6, cy - 10, x + 18, cy - 10, x + 24, cy);
            eye.curveTo(x + 18, cy + 10, x + 6, cy + 10, x, cy);
            eye.closePath();
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(eye);

            // iris + highlight
            g2.fillOval(x + 7, cy - 5, 10, 10);
            g2.setColor(new Color(244, 244, 247));
            g2.fillOval(x + 12, cy - 4, 3, 3);

            if (!open) {
                // slash with a small gap around it so it reads cleanly over the eye
                g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(244, 244, 247));
                g2.drawLine(x + 3, cy + 9, x + 21, cy - 9);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(ink);
                g2.drawLine(x + 3, cy + 9, x + 21, cy - 9);
            }
        }

        private void drawPerson(Graphics2D g2, int x, int cy) {
            g2.fillOval(x + 3, cy - 9, 10, 10);                   // head
            g2.fillArc(x - 1, cy + 3, 18, 16, 0, 180);            // shoulders
        }
    }

    private static class PillButton extends JButton {
        PillButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 17));
            setForeground(new Color(40, 20, 70));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(300, 48));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color fill = Color.WHITE;
            if (!isEnabled()) fill = new Color(255, 255, 255, 120);
            else if (getModel().isPressed()) fill = new Color(225, 205, 240);
            else if (getModel().isRollover()) fill = new Color(255, 225, 245);

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
}