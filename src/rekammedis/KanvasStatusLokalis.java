package rekammedis;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Kanvas Status Lokalis Interaktif untuk Penilaian Medis IGD.
 * Berdasarkan rekomendasi folder Argon (status lokalis.html & status lokalis.java).
 * 
 * Presisi 100%: Titik penanda selalu jatuh tepat di bawah ujung pointer mouse.
 */
public class KanvasStatusLokalis extends JPanel {

    public enum ModeAlat {
        LINGKARAN, SILANG, PENSIL
    }

    private static final Color WARNA_MERAH = new Color(217, 4, 41); // #d90429
    private static final Color WARNA_FILL_LINGKARAN = new Color(255, 0, 0, 90); // rgba(255,0,0,0.35)

    // Resolusi internal buffer coretan (2x dari 550x363 untuk ketajaman anti-aliased)
    private static final int BUFFER_LEBAR = 1100;
    private static final int BUFFER_TINGGI = 726;

    private BufferedImage latar;
    private BufferedImage coretan;
    private ModeAlat modeAlat = ModeAlat.LINGKARAN;
    private Point2D.Double titikSebelumnya = null;
    private boolean isDrawingPen = false;

    // Riwayat gambar untuk fitur Undo (maksimal 25 riwayat)
    private final List<BufferedImage> riwayatUndo = new ArrayList<>();
    private static final int MAX_UNDO = 25;

    public KanvasStatusLokalis(int lebar, int tinggi) {
        setLayout(null);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        setPreferredSize(new Dimension(lebar, tinggi));
        setMinimumSize(new Dimension(lebar, tinggi));
        setMaximumSize(new Dimension(lebar, tinggi));
        setSize(lebar, tinggi);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        setToolTipText("Arahkan kursor lalu klik/drag pada tubuh untuk memberi penanda medis");

        try {
            latar = ImageIO.read(getClass().getResource("/picture/semua.png"));
        } catch (Exception e) {
            latar = null;
        }

        inisialisasiLayerCoretan(BUFFER_LEBAR, BUFFER_TINGGI);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isEnabled()) return;
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) {
                    w = getPreferredSize().width;
                    h = getPreferredSize().height;
                }

                int mx = Math.max(0, Math.min(w - 1, e.getX()));
                int my = Math.max(0, Math.min(h - 1, e.getY()));

                double scaleX = (double) coretan.getWidth() / w;
                double scaleY = (double) coretan.getHeight() / h;
                double bufX = mx * scaleX;
                double bufY = my * scaleY;

                if (modeAlat == ModeAlat.LINGKARAN) {
                    gambarLingkaran(bufX, bufY, scaleX, scaleY);
                    simpanRiwayat();
                } else if (modeAlat == ModeAlat.SILANG) {
                    gambarSilang(bufX, bufY, scaleX, scaleY);
                    simpanRiwayat();
                } else if (modeAlat == ModeAlat.PENSIL) {
                    isDrawingPen = true;
                    titikSebelumnya = new Point2D.Double(bufX, bufY);
                    gambarTitikPensil(bufX, bufY, scaleX, scaleY);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDrawingPen) {
                    isDrawingPen = false;
                    titikSebelumnya = null;
                    simpanRiwayat();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isEnabled() || !isDrawingPen || modeAlat != ModeAlat.PENSIL) return;
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) {
                    w = getPreferredSize().width;
                    h = getPreferredSize().height;
                }

                int mx = Math.max(0, Math.min(w - 1, e.getX()));
                int my = Math.max(0, Math.min(h - 1, e.getY()));

                double scaleX = (double) coretan.getWidth() / w;
                double scaleY = (double) coretan.getHeight() / h;
                double bufX = mx * scaleX;
                double bufY = my * scaleY;
                Point2D.Double titikSekarang = new Point2D.Double(bufX, bufY);

                if (titikSebelumnya != null) {
                    gambarGarisPensil(titikSebelumnya, titikSekarang, scaleX);
                    titikSebelumnya = titikSekarang;
                }
            }
        });

        // Simpan state awal kosong
        simpanRiwayat();
    }

    private void inisialisasiLayerCoretan(int w, int h) {
        coretan = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
    }

    public void setModeAlat(ModeAlat mode) {
        this.modeAlat = mode;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public ModeAlat getModeAlat() {
        return modeAlat;
    }

    /**
     * Menggambar lingkaran penanda dengan radius tepat 10px visual, berpusat tepat di bawah kursor.
     */
    public void gambarLingkaran(double bufX, double bufY, double scaleX, double scaleY) {
        Graphics2D g = coretan.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double radVisual = 10.0;
        double radBufX = radVisual * scaleX;
        double radBufY = radVisual * scaleY;
        float strokeBuf = (float) Math.max(1.5, 2.0 * scaleX);

        g.setColor(WARNA_FILL_LINGKARAN);
        g.fillOval(
            (int) Math.round(bufX - radBufX),
            (int) Math.round(bufY - radBufY),
            (int) Math.round(radBufX * 2),
            (int) Math.round(radBufY * 2)
        );

        g.setColor(WARNA_MERAH);
        g.setStroke(new BasicStroke(strokeBuf));
        g.drawOval(
            (int) Math.round(bufX - radBufX),
            (int) Math.round(bufY - radBufY),
            (int) Math.round(radBufX * 2),
            (int) Math.round(radBufY * 2)
        );

        g.dispose();
        repaint();
    }

    /**
     * Menggambar tanda silang 'X' dengan bentangan 8px visual, berpusat tepat di bawah kursor.
     */
    public void gambarSilang(double bufX, double bufY, double scaleX, double scaleY) {
        Graphics2D g = coretan.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double dVisual = 8.0;
        double dBufX = dVisual * scaleX;
        double dBufY = dVisual * scaleY;
        float strokeBuf = (float) Math.max(2.0, 3.0 * scaleX);

        g.setColor(WARNA_MERAH);
        g.setStroke(new BasicStroke(strokeBuf, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g.drawLine(
            (int) Math.round(bufX - dBufX), (int) Math.round(bufY - dBufY),
            (int) Math.round(bufX + dBufX), (int) Math.round(bufY + dBufY)
        );
        g.drawLine(
            (int) Math.round(bufX + dBufX), (int) Math.round(bufY - dBufY),
            (int) Math.round(bufX - dBufX), (int) Math.round(bufY + dBufY)
        );

        g.dispose();
        repaint();
    }

    public void gambarTitikPensil(double bufX, double bufY, double scaleX, double scaleY) {
        Graphics2D g = coretan.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double radVisual = 1.5;
        double radBufX = radVisual * scaleX;
        double radBufY = radVisual * scaleY;

        g.setColor(WARNA_MERAH);
        g.fillOval(
            (int) Math.round(bufX - radBufX),
            (int) Math.round(bufY - radBufY),
            (int) Math.round(radBufX * 2),
            (int) Math.round(radBufY * 2)
        );
        g.dispose();
        repaint();
    }

    public void gambarGarisPensil(Point2D.Double dari, Point2D.Double ke, double scaleX) {
        Graphics2D g = coretan.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float strokeBuf = (float) Math.max(2.0, 3.0 * scaleX);
        g.setColor(WARNA_MERAH);
        g.setStroke(new BasicStroke(strokeBuf, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(
            (int) Math.round(dari.x), (int) Math.round(dari.y),
            (int) Math.round(ke.x), (int) Math.round(ke.y)
        );
        g.dispose();
        repaint();
    }

    private void simpanRiwayat() {
        BufferedImage salinan = salinImage(coretan);
        riwayatUndo.add(salinan);
        if (riwayatUndo.size() > MAX_UNDO) {
            riwayatUndo.remove(0);
        }
    }

    private BufferedImage salinImage(BufferedImage src) {
        BufferedImage salinan = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = salinan.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return salinan;
    }

    public void undoLast() {
        if (riwayatUndo.size() > 1) {
            riwayatUndo.remove(riwayatUndo.size() - 1);
            BufferedImage sebelumnya = riwayatUndo.get(riwayatUndo.size() - 1);
            Graphics2D g = coretan.createGraphics();
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, coretan.getWidth(), coretan.getHeight());
            g.setComposite(AlphaComposite.SrcOver);
            g.drawImage(sebelumnya, 0, 0, null);
            g.dispose();
            repaint();
        } else if (riwayatUndo.size() == 1) {
            hapusCoretan();
        }
    }

    public void hapusCoretan() {
        Graphics2D g = coretan.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, coretan.getWidth(), coretan.getHeight());
        g.dispose();
        riwayatUndo.clear();
        simpanRiwayat();
        repaint();
    }

    public boolean adaCoretan() {
        if (coretan == null) return false;
        for (int y = 0; y < coretan.getHeight(); y++) {
            for (int x = 0; x < coretan.getWidth(); x++) {
                if ((coretan.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getCoretanBase64() {
        try {
            if (!adaCoretan()) return "";
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(coretan, "png", bos);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            System.out.println("Notif getCoretanBase64 Status Lokalis: " + e);
            return "";
        }
    }

    public void setCoretanBase64(String b64) {
        hapusCoretan();
        if (b64 == null || b64.trim().isEmpty()) return;
        try {
            byte[] data = Base64.getDecoder().decode(b64.trim());
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
            if (img != null) {
                Graphics2D g = coretan.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(img, 0, 0, coretan.getWidth(), coretan.getHeight(), null);
                g.dispose();
                riwayatUndo.clear();
                simpanRiwayat();
                repaint();
            }
        } catch (Exception e) {
            System.out.println("Notif setCoretanBase64 Status Lokalis: " + e);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();
        if (latar != null) {
            g2.drawImage(latar, 0, 0, w, h, this);
        }
        if (coretan != null) {
            g2.drawImage(coretan, 0, 0, w, h, this);
        }
    }

    public static BufferedImage gambarCetak(String coretanBase64) {
        BufferedImage bg;
        try {
            bg = ImageIO.read(KanvasStatusLokalis.class.getResource("/picture/semua.png"));
        } catch (Exception e) {
            System.out.println("Notif gambarCetak (semua.png tidak terbaca) : " + e);
            return null;
        }
        if (bg == null) return null;

        BufferedImage hasil = new BufferedImage(BUFFER_LEBAR, BUFFER_TINGGI, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = hasil.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, hasil.getWidth(), hasil.getHeight());
        g.drawImage(bg, 0, 0, BUFFER_LEBAR, BUFFER_TINGGI, null);

        if (coretanBase64 != null && !coretanBase64.trim().isEmpty()) {
            try {
                byte[] data = Base64.getDecoder().decode(coretanBase64.trim());
                BufferedImage coret = ImageIO.read(new ByteArrayInputStream(data));
                if (coret != null) {
                    g.drawImage(coret, 0, 0, BUFFER_LEBAR, BUFFER_TINGGI, null);
                }
            } catch (Exception e) {
                System.out.println("Notif gambarCetak Status Lokalis (coretan error) : " + e);
            }
        }
        g.dispose();
        return hasil;
    }

    public static InputStream gambarCetakStream(String coretanBase64) {
        try {
            BufferedImage img = gambarCetak(coretanBase64);
            if (img != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(img, "png", os);
                return new ByteArrayInputStream(os.toByteArray());
            }
        } catch (Exception e) {
            System.out.println("Notif gambarCetakStream Status Lokalis: " + e);
        }
        try {
            return KanvasStatusLokalis.class.getResource("/picture/semua.png").openStream();
        } catch (Exception e) {
            return null;
        }
    }
}
