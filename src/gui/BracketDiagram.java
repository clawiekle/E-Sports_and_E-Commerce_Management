package gui;

import dsa.BracketTree;
import dsa.MatchNode;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Draws the bracket tree like the mockup: entrants on the left, the final on the right,
 * then a trophy and the champion box (blue) once the final has been played.
 */
public class BracketDiagram extends JPanel {

    private static final int BOX_W = 150;
    private static final int BOX_H = 30;
    private static final int COL_GAP = 60;
    private static final int ROW_GAP = 46;
    private static final int MARGIN = 30;

    private static final Color BOX = Theme.YELLOW;
    private static final Color BOX_TBD = new Color(0x9A, 0x7B, 0x83);
    private static final Color CHAMPION = new Color(0xA3, 0x23, 0x45);

    private final BracketTree tree;
    private final Map<MatchNode, Point> pos = new HashMap<>();
    private int nextLeaf = 0;

    public BracketDiagram(BracketTree tree) {
        this.tree = tree;
        setBackground(Theme.GREY);
        layout(tree.getRoot());
        int width = MARGIN * 2 + (tree.getRounds() + 1) * (BOX_W + COL_GAP) + 80 + BOX_W;
        int height = MARGIN * 2 + tree.getLeafCount() * ROW_GAP;
        setPreferredSize(new Dimension(width, height));
    }

    /** Leaves get consecutive rows; every parent sits centred between its two children. */
    private int layout(MatchNode node) {
        int x = MARGIN + node.getRound() * (BOX_W + COL_GAP);
        int y;
        if (node.isLeaf()) {
            y = MARGIN + nextLeaf * ROW_GAP;
            nextLeaf++;
        } else {
            y = (layout(node.getLeft()) + layout(node.getRight())) / 2;
        }
        pos.put(node, new Point(x, y));
        return y;
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // connector lines
        g2.setColor(new Color(0x55, 0x55, 0x55));
        g2.setStroke(new BasicStroke(1.5f));
        for (Map.Entry<MatchNode, Point> e : pos.entrySet()) {
            MatchNode parent = e.getKey().getParent();
            if (parent == null) continue;
            Point from = e.getValue();
            Point to = pos.get(parent);
            int x1 = from.x + BOX_W;
            int mid = x1 + COL_GAP / 2;
            g2.drawLine(x1, from.y + BOX_H / 2, mid, from.y + BOX_H / 2);
            g2.drawLine(mid, from.y + BOX_H / 2, mid, to.y + BOX_H / 2);
            g2.drawLine(mid, to.y + BOX_H / 2, to.x, to.y + BOX_H / 2);
        }

        // boxes
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (Map.Entry<MatchNode, Point> e : pos.entrySet()) {
            MatchNode node = e.getKey();
            Point p = e.getValue();
            String text;
            Color fill = BOX;
            if (node.isLeaf() && node.isBye()) {
                text = "BYE";
                fill = BOX_TBD;
            } else if (node.getTeam() == null) {
                text = "TBD";
                fill = BOX_TBD;
            } else {
                text = node.getTeam().getTeamName();
            }
            drawBox(g2, p.x, p.y, text, fill);
        }

        // root -> trophy -> champion
        Point root = pos.get(tree.getRoot());
        int cy = root.y + BOX_H / 2;
        int lineEnd = root.x + BOX_W + COL_GAP / 2;
        g2.setColor(new Color(0x55, 0x55, 0x55));
        g2.drawLine(root.x + BOX_W, cy, lineEnd + 20, cy);
        drawTrophy(g2, lineEnd + 38, cy);
        boolean decided = tree.getChampion() != null;
        drawBox(g2, lineEnd + 70, root.y, decided ? tree.getChampion().getTeamName() : "Champion TBD",
                decided ? CHAMPION : BOX_TBD);
        g2.dispose();
    }

    private void drawBox(Graphics2D g2, int x, int y, String text, Color fill) {
        g2.setColor(fill);
        g2.fillRoundRect(x, y, BOX_W, BOX_H, 8, 8);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        Shape oldClip = g2.getClip();
        g2.clipRect(x + 4, y, BOX_W - 8, BOX_H);
        g2.drawString(text, x + 10, y + (BOX_H - fm.getHeight()) / 2 + fm.getAscent());
        g2.setClip(oldClip);
    }

    /** Small trophy drawn with shapes (no emoji font needed). */
    private void drawTrophy(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(0xE0, 0xA8, 0x20));
        g2.fillArc(cx - 12, cy - 16, 24, 26, 180, 180);       // cup
        g2.fillRect(cx - 12, cy - 16, 24, 6);                 // rim
        g2.setStroke(new BasicStroke(2f));
        g2.drawArc(cx - 19, cy - 14, 12, 12, 90, 180);        // handles
        g2.drawArc(cx + 7, cy - 14, 12, 12, 270, 180);
        g2.fillRect(cx - 2, cy + 8, 4, 6);                    // stem
        g2.fillRoundRect(cx - 9, cy + 13, 18, 4, 3, 3);       // base
    }
}