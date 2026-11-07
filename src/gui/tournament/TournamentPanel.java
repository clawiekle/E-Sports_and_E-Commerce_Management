package gui.tournament;

import dsa.SinglyLinkedList;
import gui.Theme;
import model.Tournament;
import service.TournamentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class TournamentPanel extends JPanel {

    static final Color PANEL_GRAY = new Color(0xE2, 0xE1, 0xDD);
    static final Color GRID_GRAY = new Color(0xD0, 0xCF, 0xCB);
    static final Color SELECT_GRAY = new Color(0xCE, 0xCD, 0xC8);

    private static final String LIST = "LIST";
    private static final String OTHER = "OTHER"; // the form or view currently shown

    private final TournamentService service = new TournamentService();
    private final TournamentTableModel tableModel = new TournamentTableModel();
    private final MessageTable table = new MessageTable(tableModel);
    private final SearchField searchField = new SearchField("Search tournament...");

    private final CardLayout cards = new CardLayout();
    private final JPanel stack = new JPanel(cards);
    private Component otherCard;
    private boolean listShowing = true;

    public TournamentPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);
        stack.setOpaque(false);
        stack.add(buildListCard(), LIST);
        add(stack, BorderLayout.CENTER);

        // keeps the Status column honest: tournaments start by themselves when their date/time arrives
        new Timer(30_000, e -> {
            if (listShowing && isShowing()) reloadTable();
        }).start();
    }

    /** Called every time the section is opened from the sidebar. */
    public void refresh() {
        showList();
    }

    // ------------------------------------------------------------- list screen

    private JPanel buildListCard() {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        // title on the left, admin badge on the right
        JLabel title = new JLabel("TOURNAMENT MANAGEMENT");
        title.setFont(Theme.TITLE);
        title.setForeground(Color.BLACK);
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(new AdminBadge(), BorderLayout.EAST);

        // "+ Create Tournament" and the search box, right aligned
        JButton createBtn = new Theme.Btn("+ Create Tournament", Theme.MAROON, Theme.INK);
        createBtn.addActionListener(e -> showForm(null));
        searchField.setPreferredSize(new Dimension(230, 38));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { reloadTable(); }
            @Override public void removeUpdate(DocumentEvent e) { reloadTable(); }
            @Override public void changedUpdate(DocumentEvent e) { reloadTable(); }
        });
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        tools.setOpaque(false);
        tools.add(createBtn);
        tools.add(searchField);

        JPanel north = new JPanel(new BorderLayout(0, 18));
        north.setOpaque(false);
        north.add(titleRow, BorderLayout.NORTH);
        north.add(tools, BorderLayout.SOUTH);
        card.add(north, BorderLayout.NORTH);

        styleTable(table);
        table.setEmptyText("No tournament has been created.");
        int[] widths = {240, 140, 150, 110, 140, 120};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) showView(tableModel.getAt(row));
            }
        });
        card.add(tableScroll(table), BorderLayout.CENTER);

        return card;
    }

    private void reloadTable() {
        try {
            tableModel.setData(service.getAll(searchField.getText()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load tournaments: " + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------- navigation

    private void showList() {
        reloadTable();
        listShowing = true;
        cards.show(stack, LIST);
        dropOtherCard();
    }

    private void showForm(Tournament existing) {
        setOtherCard(new TournamentFormPanel(service, existing,
                () -> { if (existing == null) showList(); else showView(existing); },
                () -> { if (existing == null) showList(); else showView(existing); }));
    }

    private void showView(Tournament tournament) {
        setOtherCard(new TournamentViewPanel(tournament, this::showList, this::showForm, this::showList));
    }

    private void setOtherCard(Component card) {
        dropOtherCard();
        otherCard = card;
        stack.add(card, OTHER);
        listShowing = false;
        cards.show(stack, OTHER);
    }

    private void dropOtherCard() {
        if (otherCard != null) {
            stack.remove(otherCard);
            otherCard = null;
        }
    }

    // ------------------------------------------------------- shared table style

    /** Light-grey table with centred text, a line under the header and thin vertical dividers. */
    static void styleTable(JTable table) {
        table.setOpaque(false);
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(Color.BLACK);
        table.setGridColor(GRID_GRAY);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, r, c);
                setOpaque(sel);
                setBackground(SELECT_GRAY);
                setForeground(Color.BLACK);
                setHorizontalAlignment(CENTER);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, cell);
        table.setDefaultRenderer(Integer.class, cell);

        DefaultTableCellRenderer head = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, false, false, r, c);
                setOpaque(false);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(Color.BLACK);
                setHorizontalAlignment(CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, GRID_GRAY));
                return this;
            }
        };
        table.getTableHeader().setDefaultRenderer(head);
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 46));
    }

    /** The table inside a rounded light-grey panel. */
    static JComponent tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        JViewport headerPort = new JViewport();   // transparent, so the header shows the grey panel
        headerPort.setOpaque(false);
        headerPort.setView(table.getTableHeader());
        scroll.setColumnHeader(headerPort);

        Theme.Card card = new Theme.Card(PANEL_GRAY, 18);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(4, 4, 4, 4));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ------------------------------------------------------------------ widgets

    /** JTable that writes a message in its first column when there are no rows. */
    private static class MessageTable extends JTable {
        private String emptyText = "";

        MessageTable(TableModel model) { super(model); }

        void setEmptyText(String text) { this.emptyText = text; }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() == 0 && emptyText != null && !emptyText.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(new Color(0x77, 0x77, 0x77));
                FontMetrics fm = g2.getFontMetrics();
                int colW = getColumnModel().getColumn(0).getWidth();
                g2.drawString(emptyText, Math.max(8, (colW - fm.stringWidth(emptyText)) / 2), getHeight() / 2);
                g2.dispose();
            }
        }
    }

    /** Rounded grey search box with a magnifier icon and a placeholder. */
    private static class SearchField extends JTextField {
        private final String placeholder;

        SearchField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(Color.BLACK);
            setCaretColor(Color.BLACK);
            setBorder(new EmptyBorder(0, 38, 0, 12));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(PANEL_GRAY);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setColor(GRID_GRAY);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            // magnifier
            int cy = getHeight() / 2;
            g2.setColor(new Color(0x88, 0x88, 0x88));
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawOval(13, cy - 8, 11, 11);
            g2.drawLine(22, cy + 1, 27, cy + 7);
            g2.dispose();

            super.paintComponent(g);

            if (getText().isEmpty()) {
                Graphics2D p = (Graphics2D) g.create();
                p.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                p.setFont(getFont().deriveFont(12f));
                p.setColor(new Color(0x88, 0x88, 0x88));
                FontMetrics fm = p.getFontMetrics();
                p.drawString(placeholder, 38, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                p.dispose();
            }
        }
    }

    /** Round avatar + "Admin" at the top right of the screen. */
    private static class AdminBadge extends JPanel {
        AdminBadge() {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            JComponent avatar = new JComponent() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0xD9, 0xD9, 0xE3));
                    g2.fillOval(0, 0, 36, 36);
                    g2.setColor(new Color(0x22, 0x22, 0x22));
                    g2.fillOval(12, 7, 12, 12);                 // head
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, 36, 36));
                    g2.fillOval(7, 21, 22, 20);                 // shoulders
                    g2.dispose();
                }
            };
            avatar.setPreferredSize(new Dimension(36, 36));
            JLabel name = new JLabel("Admin");
            name.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            add(avatar);
            add(name);
        }
    }

    // ------------------------------------------------------------- table model

    private static class TournamentTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Tournament Name", "Game", "Maximum Teams", "Time", "Date", "Status"};
        private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a");
        private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy");
        private final List<Tournament> rows = new ArrayList<>();

        void setData(SinglyLinkedList data) {
            rows.clear();
            for (int i = 0; i < data.size(); i++) rows.add((Tournament) data.get(i));
            fireTableDataChanged();
        }

        Tournament getAt(int row) { return rows.get(row); }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int c) { return COLUMNS[c]; }

        @Override public Object getValueAt(int r, int c) {
            Tournament t = rows.get(r);
            switch (c) {
                case 0: return t.getTournamentName();
                case 1: return t.getGameName();
                case 2: return t.getMaxTeams();
                case 3: return t.getStartDateTime() == null ? "-" : t.getStartDateTime().format(TIME);
                case 4: return t.getStartDateTime() == null ? "-" : t.getStartDateTime().format(DATE);
                default: return t.getStatus().getLabel();
            }
        }
    }
}