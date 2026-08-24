package Hotel_Reservation;



import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * HotelBackgroundPanel
 * ─────────────────────
 * A self-contained, procedurally-painted "hotel at night" backdrop —
 * no external .jpg/.png assets required, so it always renders correctly
 * regardless of classpath/resource setup.
 *
 * Draws: a deep-navy-to-gold night sky gradient, a hotel tower silhouette
 * with randomly lit windows, a subtle spotlight glow behind the entrance,
 * and (optionally) a translucent glass "content card" area where real
 * Swing components (login form, tabs, etc.) should be placed on top.
 *
 * Usage:
 *   HotelBackgroundPanel bg = new HotelBackgroundPanel();
 *   bg.setLayout(new BorderLayout());     // or null, GridBagLayout, etc.
 *   bg.add(myLoginPanel);
 *   frame.setContentPane(bg);
 */
public class HotelBackgroundPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Deterministic seed so the "lit windows" pattern doesn't flicker/reshuffle on every repaint
    private final long seed = 42L;

    public HotelBackgroundPanel() {
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // ── Night sky gradient (deep navy → warm plum near the horizon) ──
        GradientPaint sky = new GradientPaint(
                0, 0, new Color(10, 8, 38),
                0, h, new Color(58, 24, 74));
        g2.setPaint(sky);
        g2.fillRect(0, 0, w, h);

        // ── Soft gold "moon glow" spotlight, upper-right ──
        Point2D center = new Point2D.Float(w * 0.82f, h * 0.18f);
        float radius = Math.max(w, h) * 0.5f;
        float[] dist = {0f, 1f};
        Color[] colors = {new Color(255, 226, 150, 70), new Color(255, 226, 150, 0)};
        RadialGradientPaint glow = new RadialGradientPaint(center, radius, dist, colors);
        g2.setPaint(glow);
        g2.fillRect(0, 0, w, h);

        // ── Distant skyline (soft silhouettes, low opacity) ──
        Random rFar = new Random(seed);
        g2.setColor(new Color(30, 20, 60, 160));
        int baseFar = (int) (h * 0.72);
        int xFar = -20;
        while (xFar < w + 40) {
            int bw = 40 + rFar.nextInt(50);
            int bh = 60 + rFar.nextInt((int) (h * 0.28));
            g2.fillRect(xFar, baseFar - bh, bw, bh + h);
            xFar += bw + 6;
        }

        // ── Main hotel tower silhouette (foreground, centered) ──
        Random rNear = new Random(seed + 1);
        Color towerColor = new Color(18, 14, 42);
        int towerW = (int) (w * 0.46);
        int towerX = (w - towerW) / 2;
        int towerTop = (int) (h * 0.10);
        int towerBottom = h;
        g2.setColor(towerColor);
        g2.fillRect(towerX, towerTop, towerW, towerBottom - towerTop);

        // Little roof/parapet accent
        g2.fillRect(towerX - 10, towerTop, towerW + 20, 14);

        // Entrance canopy + glow
        int canopyW = (int) (towerW * 0.5);
        int canopyX = towerX + (towerW - canopyW) / 2;
        int canopyY = towerBottom - 46;
        g2.setColor(new Color(255, 215, 0, 210));
        g2.fillRoundRect(canopyX, canopyY, canopyW, 8, 6, 6);
        RadialGradientPaint doorGlow = new RadialGradientPaint(
                new Point2D.Float(canopyX + canopyW / 2f, towerBottom - 6f),
                canopyW * 0.9f,
                new float[]{0f, 1f},
                new Color[]{new Color(255, 215, 0, 90), new Color(255, 215, 0, 0)});
        g2.setPaint(doorGlow);
        g2.fillRect(towerX, towerBottom - 90, towerW, 90);

        // Windows grid — some lit (warm gold), most dark
        int cols = 8;
        int rows = Math.max(6, (towerBottom - towerTop - 60) / 34);
        int marginX = 18;
        int cellW = (towerW - marginX * 2) / cols;
        int cellH = Math.min(30, (towerBottom - towerTop - 60) / Math.max(rows, 1));
        int winW = Math.max(6, cellW - 10);
        int winH = Math.max(8, cellH - 10);

        for (int r = 0; r < rows; r++) {
            int y = towerTop + 30 + r * cellH;
            if (y + winH > towerBottom - 30) break;
            for (int c = 0; c < cols; c++) {
                int x = towerX + marginX + c * cellW;
                boolean lit = rNear.nextInt(100) < 35;
                if (lit) {
                    g2.setColor(new Color(255, 210, 120, 235));
                } else {
                    g2.setColor(new Color(50, 42, 90, 200));
                }
                g2.fillRoundRect(x, y, winW, winH, 3, 3);
            }
        }

        // ── Flag / spire accent on rooftop ──
        g2.setColor(new Color(255, 215, 0));
        int poleX = towerX + towerW / 2;
        g2.fillRect(poleX, towerTop - 26, 2, 26);
        g2.fillPolygon(new int[]{poleX + 2, poleX + 22, poleX + 2},
                       new int[]{towerTop - 26, towerTop - 20, towerTop - 14}, 3);

        // ── Ground / street glow strip ──
        GradientPaint ground = new GradientPaint(
                0, h - 26, new Color(0, 0, 0, 0),
                0, h, new Color(0, 0, 0, 140));
        g2.setPaint(ground);
        g2.fillRect(0, h - 26, w, 26);

        g2.dispose();
    }
}