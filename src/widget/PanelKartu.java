package widget;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Panel kartu bersudut membulat dengan drop shadow lembut.
 *
 * Shadow digambar di dalam area komponen, jadi sisi luar sebesar {@link #getPad()}
 * piksel dipakai sebagai ruang shadow. Komponen anak yang diletakkan dengan
 * layout null harus memperhitungkan pad tersebut (atau pakai {@link #getInsets()}).
 *
 * @author widget SIMRS Khanza
 */
public class PanelKartu extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Radius sudut kartu. */
    private int radius = 16;
    /** Lebar area shadow di tiap sisi. */
    private int pad = 12;
    /** Warna badan kartu. */
    private Color warna = ThemeConfig.SURFACE;
    /** Warna garis tepi kartu, null berarti tanpa garis tepi. */
    private Color warnaGaris = ThemeConfig.BORDER;
    /** Opasitas maksimum shadow (0-255). */
    private int alphaShadow = 26;

    public PanelKartu() {
        setOpaque(false);
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = Math.max(0, radius);
        repaint();
    }

    public int getPad() {
        return pad;
    }

    public void setPad(int pad) {
        this.pad = Math.max(0, pad);
        revalidate();
        repaint();
    }

    public Color getWarna() {
        return warna;
    }

    public void setWarna(Color warna) {
        this.warna = warna;
        repaint();
    }

    public Color getWarnaGaris() {
        return warnaGaris;
    }

    public void setWarnaGaris(Color warnaGaris) {
        this.warnaGaris = warnaGaris;
        repaint();
    }

    public int getAlphaShadow() {
        return alphaShadow;
    }

    public void setAlphaShadow(int alphaShadow) {
        this.alphaShadow = Math.max(0, Math.min(255, alphaShadow));
        repaint();
    }

    @Override
    public Insets getInsets() {
        return new Insets(pad, pad, pad, pad);
    }

    @Override
    public Insets getInsets(Insets insets) {
        if (insets == null) {
            return getInsets();
        }
        insets.set(pad, pad, pad, pad);
        return insets;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.SrcOver);

            int w = getWidth();
            int h = getHeight();
            int bodyW = w - (pad * 2);
            int bodyH = h - (pad * 2);
            if (bodyW <= 0 || bodyH <= 0) {
                return;
            }

            // Shadow: beberapa lapis round rect makin luar makin transparan.
            if (pad > 0 && alphaShadow > 0) {
                for (int i = pad; i >= 1; i--) {
                    double rasio = 1d - ((double) i / pad);
                    int alpha = (int) Math.round(alphaShadow * rasio * rasio);
                    if (alpha <= 0) {
                        continue;
                    }
                    g2.setColor(new Color(0, 0, 0, alpha));
                    // digeser 2px ke bawah supaya shadow terasa jatuh ke bawah
                    g2.fillRoundRect(pad - i, pad - i + 2, bodyW + (i * 2), bodyH + (i * 2),
                            radius + i, radius + i);
                }
            }

            // Badan kartu.
            if (warna != null) {
                g2.setColor(warna);
                g2.fillRoundRect(pad, pad, bodyW, bodyH, radius, radius);
            }

            // Garis tepi.
            if (warnaGaris != null) {
                g2.setColor(warnaGaris);
                g2.drawRoundRect(pad, pad, bodyW - 1, bodyH - 1, radius, radius);
            }
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
