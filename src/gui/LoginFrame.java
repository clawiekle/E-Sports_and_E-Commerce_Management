package gui;

import model.User;
import service.AuthService;
import service.LoginResult;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;


public class LoginFrame extends JFrame {

    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 15);
    private static final Color ERROR_COLOR = new Color(255, 120, 120);
    private static final Color SUCCESS_COLOR = new Color(170, 255, 200);
    private static final Color HOVER_COLOR = new Color(255, 170, 185);

    private final AuthService authService = new AuthService();
    private final Consumer<User> onSuccess;

    // the background must be declared before the card, because the card uses it
    private final Backdrop background = new Backdrop("/image/login_bg.png");
    private final GlassPanel card = new GlassPanel(background);
    private final JLabel logo = new JLabel("", SwingConstants.CENTER);
    private final JLabel title = new JLabel("WELCOME", SwingConstants.CENTER);
    private final JLabel subtitle = new JLabel("E-Sports & E-Commerce Management", SwingConstants.CENTER);
    private final RoundedField usernameField = new RoundedField("username", false);
    private final RoundedField passwordField = new RoundedField("password", true);
    private final RoundedField confirmField = new RoundedField("confirm password", true);
    private final JPanel options = new JPanel(new BorderLayout());
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);
    private final PillButton submitBtn = new PillButton("LOGIN");
    private final JLabel switchAsk = new JLabel("Don't have an account?");
    private final JLabel switchLink = new JLabel("Sign up.");

    private boolean registerMode = false;

    public LoginFrame(Consumer<User> onSuccess) {
        super("E-Sports & E-Commerce Management - Login");
        this.onSuccess = onSuccess;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 720);
        setMinimumSize(new Dimension(560, 690));
        setLocationRelativeTo(null);

        buildCard();
        background.add(card);
        setContentPane(background);

        setMode(false);
        submitBtn.addActionListener(e -> submit());
        getRootPane().setDefaultButton(submitBtn);
    }

    // ---------------------------------------------------------------- layout

    private void buildCard() {
        card.setPreferredSize(new Dimension(360, 540));
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(28, 40, 28, 40));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        // logo (put your logo at src/images/logo.png; hidden if the file is missing)
        ImageIcon icon = loadIcon("/image/logo.png", 90);
        if (icon != null) logo.setIcon(icon);
        else logo.setVisible(false);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 8, 0);
        card.add(logo, c);

        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 2, 0);
        card.add(title, c);

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(255, 255, 255, 170));
        c.gridy = 2;
        c.insets = new Insets(0, 0, 28, 0);
        card.add(subtitle, c);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 18, 0);
        card.add(usernameField, c);

        c.gridy = 4;
        c.insets = new Insets(0, 0, 14, 0);
        card.add(passwordField, c);

        c.gridy = 5; // only visible in Register mode
        card.add(confirmField, c);

        JLabel forgot = new JLabel("Forgot Password?");
        forgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgot.setForeground(Color.WHITE);
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this, "Please contact an administrator to reset your password.");
            }
            @Override public void mouseEntered(MouseEvent e) { forgot.setForeground(HOVER_COLOR); }
            @Override public void mouseExited(MouseEvent e) { forgot.setForeground(Color.WHITE); }
        });

        options.setOpaque(false);
        options.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        options.add(forgot, BorderLayout.EAST);
        c.gridy = 6; // only visible in Login mode
        c.insets = new Insets(0, 0, 8, 0);
        card.add(options, c);

        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(ERROR_COLOR);
        c.gridy = 7;
        c.insets = new Insets(0, 0, 12, 0);
        card.add(messageLabel, c);

        c.gridy = 8;
        c.insets = new Insets(0, 0, 18, 0);
        c.fill = GridBagConstraints.NONE;      // button keeps its own (narrower) width
        c.anchor = GridBagConstraints.CENTER;
        card.add(submitBtn, c);
        c.fill = GridBagConstraints.HORIZONTAL;

        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchRow.setOpaque(false);
        switchAsk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        switchAsk.setForeground(new Color(255, 255, 255, 200));
        switchLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        switchLink.setForeground(Color.WHITE);
        switchLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switchLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { setMode(!registerMode); }
            @Override public void mouseEntered(MouseEvent e) { switchLink.setForeground(HOVER_COLOR); }
            @Override public void mouseExited(MouseEvent e) { switchLink.setForeground(Color.WHITE); }
        });
        switchRow.add(switchAsk);
        switchRow.add(switchLink);
        c.gridy = 9;
        c.insets = new Insets(0, 0, 0, 0);
        card.add(switchRow, c);
    }

    private static ImageIcon loadIcon(String path, int height) {
        URL url = LoginFrame.class.getResource(path);
        if (url == null) return null;
        ImageIcon raw = new ImageIcon(url);
        int w = raw.getIconWidth() * height / Math.max(1, raw.getIconHeight());
        return new ImageIcon(raw.getImage().getScaledInstance(w, height, Image.SCALE_SMOOTH));
    }

    private void setMode(boolean register) {
        registerMode = register;
        title.setText(register ? "REGISTER" : "WELCOME");
        subtitle.setText(register ? "Create your fan account" : "E-Sports & E-Commerce Management");
        setTitle("E-Sports & E-Commerce Management - " + (register ? "Register" : "Login"));
        confirmField.setVisible(register);
        confirmField.setText("");
        options.setVisible(!register);
        submitBtn.setText(register ? "REGISTER" : "LOGIN");
        switchAsk.setText(register ? "Already have an account?" : "Don't have an account?");
        switchLink.setText(register ? "Login." : "Sign up.");
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

    /** Sharp full-window background image. Also keeps a blurred copy for the glass card. */
    private static class Backdrop extends JPanel {
        private final BufferedImage sharp;
        private final BufferedImage blurred;

        Backdrop(String resourcePath) {
            setLayout(new GridBagLayout());
            setBackground(new Color(20, 12, 16));
            sharp = load(resourcePath);
            blurred = sharp == null ? null : blur(sharp);
        }

        private static BufferedImage load(String path) {
            try {
                URL url = LoginFrame.class.getResource(path);
                return url == null ? null : ImageIO.read(url);
            } catch (IOException e) {
                return null;
            }
        }

        /** Shrinks the image and softens it; scaling it back up blurs it further. */
        private static BufferedImage blur(BufferedImage src) {
            int w = Math.max(1, src.getWidth() / 10);
            int h = Math.max(1, src.getHeight() / 10);
            BufferedImage small = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = small.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, w, h, null);
            g.dispose();

            float[] data = new float[25];
            Arrays.fill(data, 1f / 25f);
            ConvolveOp op = new ConvolveOp(new Kernel(5, 5, data), ConvolveOp.EDGE_NO_OP, null);
            small = op.filter(small, null);
            return op.filter(small, null);
        }

        /** Draws an image scaled to cover the whole panel (same placement for the sharp and blurred copy). */
        void drawCover(Graphics2D g2, BufferedImage img) {
            int pw = getWidth(), ph = getHeight();
            double scale = Math.max((double) pw / sharp.getWidth(), (double) ph / sharp.getHeight());
            int w = (int) Math.ceil(sharp.getWidth() * scale);
            int h = (int) Math.ceil(sharp.getHeight() * scale);
            g2.drawImage(img, (pw - w) / 2, (ph - h) / 2, w, h, null);
        }

        BufferedImage getBlurred() { return blurred; }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (sharp == null) {
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                drawCover(g2, sharp);
            }
            g2.dispose();
        }
    }

    /** Frosted glass: paints the blurred part of the background behind it, then a dark tint and a soft edge. */
    private static class GlassPanel extends JPanel {
        private final Backdrop backdrop;

        GlassPanel(Backdrop backdrop) {
            this.backdrop = backdrop;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth(), h = getHeight();
            Shape shape = new RoundRectangle2D.Float(1, 1, w - 3, h - 3, 44, 44);

            // blurred copy of the background, lined up with where the card sits
            if (backdrop.getBlurred() != null) {
                Point p = SwingUtilities.convertPoint(this, 0, 0, backdrop);
                Graphics2D clipped = (Graphics2D) g2.create();
                clipped.setClip(shape);
                clipped.translate(-p.x, -p.y);
                backdrop.drawCover(clipped, backdrop.getBlurred());
                clipped.dispose();
            }

            // dark grey-blue glass tint
            g2.setColor(new Color(45, 50, 68, 150));
            g2.fill(shape);

            // faint light at the top
            g2.setClip(shape);
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 40), 0, h * 0.4f, new Color(255, 255, 255, 0)));
            g2.fillRect(0, 0, w, h / 2);
            g2.setClip(null);

            // edge
            g2.setStroke(new BasicStroke(1.2f));
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 110), 0, h, new Color(255, 255, 255, 40)));
            g2.draw(shape);
            g2.dispose();
        }
    }


    private static class RoundedField extends JPasswordField {
        private static final Color ICON = new Color(255, 255, 255, 225);

        private final String placeholder;
        private final boolean secret;
        private boolean revealed = false;

        RoundedField(String placeholder, boolean secret) {
            this.placeholder = placeholder;
            this.secret = secret;
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            setSelectionColor(new Color(150, 40, 70));
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 50));
            setEchoChar(secret ? '\u2022' : (char) 0);
            setPreferredSize(new Dimension(280, 44));

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

            // see-through body
            g2.setColor(new Color(255, 255, 255, focus ? 85 : 62));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);

            // border / focus ring
            g2.setStroke(new BasicStroke(focus ? 1.8f : 1f));
            g2.setColor(focus ? new Color(230, 90, 120) : new Color(255, 255, 255, 120));
            g2.drawRoundRect(0, 0, w - 2, h - 2, 14, 14);

            super.paintComponent(g2); // the typed text

            if (getPassword().length == 0) {
                g2.setColor(new Color(255, 255, 255, 150));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, getInsets().left, y);
            }

            // icon on the right
            g2.setColor(ICON);
            int ix = w - 40, iy = h / 2;
            if (secret) drawEye(g2, ix, iy, revealed); else drawPerson(g2, ix + 4, iy);
            g2.dispose();
        }

        private void drawEye(Graphics2D g2, int x, int cy, boolean open) {
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setColor(ICON);

            // almond outline
            Path2D eye = new Path2D.Float();
            eye.moveTo(x, cy);
            eye.curveTo(x + 6, cy - 10, x + 18, cy - 10, x + 24, cy);
            eye.curveTo(x + 18, cy + 10, x + 6, cy + 10, x, cy);
            eye.closePath();
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(eye);

            // iris
            g2.fillOval(x + 7, cy - 5, 10, 10);

            if (!open) {
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
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
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(180, 42));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            Color top = new Color(190, 25, 55);
            Color bottom = new Color(115, 10, 35);
            if (!isEnabled()) {
                top = new Color(120, 60, 70);
                bottom = new Color(80, 40, 50);
            } else if (getModel().isPressed()) {
                top = new Color(140, 15, 40);
                bottom = new Color(90, 5, 28);
            } else if (getModel().isRollover()) {
                top = new Color(215, 40, 70);
                bottom = new Color(135, 15, 42);
            }

            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);

            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(getText())) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
}