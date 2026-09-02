package widget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Panel latar untuk dialog login.
 *
 * Menggambar gambar wallpaper dengan mode "cover" (rasio dijaga, kelebihan
 * dipotong di tengah) lalu menimpanya dengan overlay gradien gelap agar kartu
 * login tetap kontras. Jika wallpaper tidak diset, panel menggambar gradien
 * warna tema.
 *
 * @author widget SIMRS Khanza
 */
public class PanelWallpaper extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Gambar wallpaper, null berarti pakai gradien tema. */
    private Image gambar;
    /** Opasitas overlay gelap di atas wallpaper (0-255). */
    private int alphaOverlay = 110;
    /** Warna awal gradien saat tidak ada wallpaper. */
    private Color warnaAwal = ThemeConfig.NAVY;
    /** Warna akhir gradien saat tidak ada wallpaper. */
    private Color warnaAkhir = ThemeConfig.PRIMARY;

    public PanelWallpaper() {
        setOpaque(true);
        setBackground(ThemeConfig.NAVY);
    }

    public Image getGambar() {
        return gambar;
    }

    public void setGambar(Image gambar) {
        this.gambar = gambar;
        repaint();
    }

    /** Set wallpaper dari icon (misal hasil baca BLOB kolom setting.wallpaper). */
    public void setGambar(ImageIcon icon) {
        setGambar(icon == null ? null : icon.getImage());
    }

    public int getAlphaOverlay() {
        return alphaOverlay;
    }

    public void setAlphaOverlay(int alphaOverlay) {
        this.alphaOverlay = Math.max(0, Math.min(255, alphaOverlay));
        repaint();
    }

    public Color getWarnaAwal() {
        return warnaAwal;
    }

    public void setWarnaAwal(Color warnaAwal) {
        this.warnaAwal = warnaAwal;
        repaint();
    }

    public Color getWarnaAkhir() {
        return warnaAkhir;
    }

    public void setWarnaAkhir(Color warnaAkhir) {
        this.warnaAkhir = warnaAkhir;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }

            int iw = gambar == null ? -1 : gambar.getWidth(this);
            int ih = gambar == null ? -1 : gambar.getHeight(this);

            if (iw > 0 && ih > 0) {
                // Mode cover: skala berdasarkan sisi yang paling kurang menutupi.
                double skala = Math.max((double) w / iw, (double) h / ih);
                int tw = (int) Math.ceil(iw * skala);
                int th = (int) Math.ceil(ih * skala);
                int tx = (w - tw) / 2;
                int ty = (h - th) / 2;
                g2.drawImage(gambar, tx, ty, tw, th, this);

                if (alphaOverlay > 0) {
                    // Overlay gradien: lebih gelap di bawah supaya teks nama RS terbaca.
                    g2.setPaint(new GradientPaint(
                            0, 0, new Color(0, 0, 0, (int) (alphaOverlay * 0.55)),
                            0, h, new Color(0, 0, 0, alphaOverlay)));
                    g2.fillRect(0, 0, w, h);
                }
            } else {
                Color c1 = warnaAwal == null ? ThemeConfig.NAVY : warnaAwal;
                Color c2 = warnaAkhir == null ? ThemeConfig.PRIMARY : warnaAkhir;
                g2.setPaint(new GradientPaint(0, 0, c1, w, h, c2));
                g2.fillRect(0, 0, w, h);
            }
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
