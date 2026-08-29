package rekammedis;

/**
 * Kanvas skala nyeri - gambar wajah skala 0-10 ({@code /picture/nyeri.png})
 * dipakai sebagai LATAR, perawat menandai/mencoret di atasnya pakai mouse
 * persis seperti di formulir kertas.
 *
 * <p>Coretan disimpan di layer TRANSPARAN terpisah (bukan menimpa gambar
 * latar), supaya:</p>
 * <ul>
 *   <li>yang diserialisasi ke DB cuma coretannya saja (PNG transparan, kecil -
 *       ratusan byte s/d beberapa KB), bukan gambar latar yang selalu sama dan
 *       sudah ada sebagai resource aplikasi;</li>
 *   <li>latar bisa diganti/di-resize kapan saja tanpa merusak data lama.</li>
 * </ul>
 *
 * <p>Disimpan sebagai base64 (keputusan user) di kolom TEXT - dipilih daripada
 * koordinat garis karena lebih gampang kalau nanti mau dimasukkan ke cetakan
 * Jasper sebagai gambar. Nilai kosong = {@code ""} (bukan base64 gambar
 * blank), dicek via {@code adaCoretan()} supaya tidak menyimpan ribuan byte
 * percuma untuk pasien yang memang tidak ditandai.</p>
 *
 * <p><b>Asalnya inner class di {@code RMPenilaianAwalKeperawatanRanap}</b>
 * (RM 19). Diangkat jadi kelas tersendiri saat RM 02-2 membutuhkan kanvas yang
 * sama - disalin-tempel akan membuat dua salinan yang perlahan berbeda.
 * Kelasnya memang tidak pernah menyentuh state form induknya, jadi
 * pengangkatannya tidak mengubah perilaku apa pun. Perilaku, ukuran layer,
 * warna, dan format base64-nya sengaja dipertahankan apa adanya supaya data
 * RM 19 yang sudah tersimpan tetap terbaca.</p>
 */
public class KanvasNyeri extends javax.swing.JPanel {

    private java.awt.image.BufferedImage latar, coretan;
    private java.awt.Point titikSebelumnya;

    public KanvasNyeri(int lebar, int tinggi) {
        setLayout(null);
        setBackground(java.awt.Color.WHITE);
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180, 180, 180)));
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
        setToolTipText("Tandai skala nyeri dgn cara mencoret pakai mouse (klik-tahan-geser)");
        try {
            latar = javax.imageio.ImageIO.read(getClass().getResource("/picture/nyeri.png"));
        } catch (Exception e) {
            latar = null; // resource hilang - kanvas tetap bisa dicoret di atas latar putih
        }
        coretan = new java.awt.image.BufferedImage(lebar, tinggi, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                titikSebelumnya = e.getPoint();
                gambarGaris(titikSebelumnya, e.getPoint()); // klik tunggal = titik/dot
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { titikSebelumnya = null; }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                if (titikSebelumnya != null) {
                    gambarGaris(titikSebelumnya, e.getPoint());
                    titikSebelumnya = e.getPoint();
                }
            }
        });
    }

    private void gambarGaris(java.awt.Point dari, java.awt.Point ke) {
        java.awt.Graphics2D g = coretan.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(java.awt.Color.RED);
        g.setStroke(new java.awt.BasicStroke(2.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g.drawLine(dari.x, dari.y, ke.x, ke.y);
        g.dispose();
        repaint();
    }

    @Override protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (latar != null) { g.drawImage(latar, 0, 0, getWidth(), getHeight(), this); }
        g.drawImage(coretan, 0, 0, this);
    }

    public void hapusCoretan() {
        java.awt.Graphics2D g = coretan.createGraphics();
        g.setComposite(java.awt.AlphaComposite.Clear);
        g.fillRect(0, 0, coretan.getWidth(), coretan.getHeight());
        g.dispose();
        repaint();
    }

    /** true kalau ada minimal 1 piksel coretan (alpha != 0). */
    private boolean adaCoretan() {
        for (int y = 0; y < coretan.getHeight(); y++) {
            for (int x = 0; x < coretan.getWidth(); x++) {
                if ((coretan.getRGB(x, y) >>> 24) != 0) { return true; }
            }
        }
        return false;
    }

    /** PNG layer coretan sbg base64; "" kalau memang belum dicoret sama sekali. */
    public String getCoretanBase64() {
        try {
            if (!adaCoretan()) { return ""; }
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(coretan, "png", bos);
            return java.util.Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            System.out.println("Notif getCoretanBase64 : " + e);
            return "";
        }
    }

    public void setCoretanBase64(String b64) {
        hapusCoretan();
        if (b64 == null || b64.trim().isEmpty()) { return; }
        try {
            byte[] data = java.util.Base64.getDecoder().decode(b64.trim());
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(data));
            if (img != null) {
                java.awt.Graphics2D g = coretan.createGraphics();
                g.drawImage(img, 0, 0, coretan.getWidth(), coretan.getHeight(), null);
                g.dispose();
                repaint();
            }
        } catch (Exception e) {
            System.out.println("Notif setCoretanBase64 (data coretan lama rusak/format beda?) : " + e);
        }
    }
}
