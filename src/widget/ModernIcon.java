package widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/**
 * Ikon flat/line sederhana digambar langsung lewat Java2D (tanpa file
 * gambar eksternal) - dipakai utk tombol toolbar Main Window, gaya
 * mengikuti mockup modern medical (garis tipis, minimalis).
 */
public class ModernIcon implements Icon {

    public static final String HOME = "home";
    public static final String USER = "user";
    public static final String PULSE = "pulse";
    public static final String FLASK = "flask";
    public static final String SCAN = "scan";
    public static final String PILL = "pill";
    public static final String BED = "bed";
    public static final String WALK = "walk";
    public static final String KEY = "key";
    public static final String POWER = "power";

    private final String type;
    private final int size;
    private final Color color;
    private final float strokeWidth;

    public ModernIcon(String type, int size, Color color) {
        this.type = type;
        this.size = size;
        this.color = color;
        this.strokeWidth = Math.max(1.5f, size / 11f);
    }

    @Override
    public int getIconWidth() { return size; }

    @Override
    public int getIconHeight() { return size; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(x, y);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float s = size;
        switch (type) {
            case HOME: drawHome(g2, s); break;
            case USER: drawUser(g2, s); break;
            case PULSE: drawPulse(g2, s); break;
            case FLASK: drawFlask(g2, s); break;
            case SCAN: drawScan(g2, s); break;
            case PILL: drawPill(g2, s); break;
            case BED: drawBed(g2, s); break;
            case WALK: drawWalk(g2, s); break;
            case KEY: drawKey(g2, s); break;
            case POWER: drawPower(g2, s); break;
            default: break;
        }
        g2.dispose();
    }

    private void drawHome(Graphics2D g2, float s) {
        Path2D roof = new Path2D.Float();
        roof.moveTo(s * 0.5f, s * 0.10f);
        roof.lineTo(s * 0.88f, s * 0.42f);
        roof.lineTo(s * 0.12f, s * 0.42f);
        roof.closePath();
        g2.draw(roof);
        g2.draw(new Rectangle2D.Float(s * 0.22f, s * 0.42f, s * 0.56f, s * 0.44f));
        g2.draw(new Rectangle2D.Float(s * 0.42f, s * 0.62f, s * 0.16f, s * 0.24f));
    }

    private void drawUser(Graphics2D g2, float s) {
        g2.draw(new Ellipse2D.Float(s * 0.32f, s * 0.10f, s * 0.36f, s * 0.36f));
        Arc2D body = new Arc2D.Float(s * 0.12f, s * 0.48f, s * 0.76f, s * 0.62f, 20, 140, Arc2D.OPEN);
        g2.draw(body);
    }

    private void drawPulse(Graphics2D g2, float s) {
        Path2D p = new Path2D.Float();
        p.moveTo(s * 0.04f, s * 0.55f);
        p.lineTo(s * 0.26f, s * 0.55f);
        p.lineTo(s * 0.37f, s * 0.22f);
        p.lineTo(s * 0.5f, s * 0.80f);
        p.lineTo(s * 0.61f, s * 0.42f);
        p.lineTo(s * 0.70f, s * 0.55f);
        p.lineTo(s * 0.96f, s * 0.55f);
        g2.draw(p);
    }

    private void drawFlask(Graphics2D g2, float s) {
        Path2D p = new Path2D.Float();
        p.moveTo(s * 0.40f, s * 0.10f);
        p.lineTo(s * 0.40f, s * 0.40f);
        p.lineTo(s * 0.14f, s * 0.84f);
        p.quadTo(s * 0.14f, s * 0.92f, s * 0.24f, s * 0.92f);
        p.lineTo(s * 0.76f, s * 0.92f);
        p.quadTo(s * 0.86f, s * 0.92f, s * 0.86f, s * 0.84f);
        p.lineTo(s * 0.60f, s * 0.40f);
        p.lineTo(s * 0.60f, s * 0.10f);
        g2.draw(p);
        g2.draw(new Line2D.Float(s * 0.32f, s * 0.10f, s * 0.68f, s * 0.10f));
        g2.draw(new Line2D.Float(s * 0.26f, s * 0.70f, s * 0.74f, s * 0.70f));
    }

    private void drawScan(Graphics2D g2, float s) {
        g2.draw(new Ellipse2D.Float(s * 0.16f, s * 0.16f, s * 0.68f, s * 0.68f));
        g2.draw(new Ellipse2D.Float(s * 0.38f, s * 0.38f, s * 0.24f, s * 0.24f));
        g2.draw(new Line2D.Float(s * 0.5f, s * 0.02f, s * 0.5f, s * 0.16f));
        g2.draw(new Line2D.Float(s * 0.5f, s * 0.84f, s * 0.5f, s * 0.98f));
        g2.draw(new Line2D.Float(s * 0.02f, s * 0.5f, s * 0.16f, s * 0.5f));
        g2.draw(new Line2D.Float(s * 0.84f, s * 0.5f, s * 0.98f, s * 0.5f));
    }

    private void drawPill(Graphics2D g2, float s) {
        RoundRectangle2D pillShape = new RoundRectangle2D.Float(s * 0.10f, s * 0.32f, s * 0.80f, s * 0.36f, s * 0.36f, s * 0.36f);
        g2.draw(pillShape);
        g2.draw(new Line2D.Float(s * 0.5f, s * 0.32f, s * 0.5f, s * 0.68f));
    }

    private void drawBed(Graphics2D g2, float s) {
        g2.draw(new Line2D.Float(s * 0.10f, s * 0.28f, s * 0.10f, s * 0.86f));
        g2.draw(new Line2D.Float(s * 0.90f, s * 0.50f, s * 0.90f, s * 0.86f));
        g2.draw(new RoundRectangle2D.Float(s * 0.10f, s * 0.50f, s * 0.80f, s * 0.22f, s * 0.06f, s * 0.06f));
        g2.draw(new RoundRectangle2D.Float(s * 0.14f, s * 0.32f, s * 0.22f, s * 0.18f, s * 0.05f, s * 0.05f));
    }

    private void drawWalk(Graphics2D g2, float s) {
        g2.draw(new Ellipse2D.Float(s * 0.40f, s * 0.06f, s * 0.20f, s * 0.20f));
        Path2D body = new Path2D.Float();
        body.moveTo(s * 0.5f, s * 0.28f);
        body.lineTo(s * 0.45f, s * 0.54f);
        body.lineTo(s * 0.24f, s * 0.70f);
        body.moveTo(s * 0.45f, s * 0.54f);
        body.lineTo(s * 0.68f, s * 0.66f);
        body.moveTo(s * 0.5f, s * 0.28f);
        body.lineTo(s * 0.70f, s * 0.40f);
        body.moveTo(s * 0.45f, s * 0.54f);
        body.lineTo(s * 0.35f, s * 0.92f);
        g2.draw(body);
    }

    private void drawKey(Graphics2D g2, float s) {
        g2.draw(new Ellipse2D.Float(s * 0.08f, s * 0.32f, s * 0.34f, s * 0.34f));
        g2.draw(new Line2D.Float(s * 0.38f, s * 0.5f, s * 0.92f, s * 0.5f));
        g2.draw(new Line2D.Float(s * 0.74f, s * 0.5f, s * 0.74f, s * 0.68f));
        g2.draw(new Line2D.Float(s * 0.87f, s * 0.5f, s * 0.87f, s * 0.62f));
    }

    private void drawPower(Graphics2D g2, float s) {
        g2.draw(new Arc2D.Float(s * 0.12f, s * 0.12f, s * 0.76f, s * 0.76f, -60, 300, Arc2D.OPEN));
        g2.draw(new Line2D.Float(s * 0.5f, s * 0.04f, s * 0.5f, s * 0.44f));
    }
}
