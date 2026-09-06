package simrskhanza;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Modern Landing Screen for SIMRS Khanza when newly opened.
 * Matches RSUD Demang Sepulau Raya design specification:
 * - Dynamic Wallpaper & Logo from database setting ("Set Aplikasi")
 * - Dark left gradient overlay ensuring maximum legibility
 * - Hospital Typography (Pemda, Subtitle, Nama RS, Alamat)
 * - Security Access Warning badge
 * - Primary Log In and IT Support Call-To-Action buttons
 * - Floating glassmorphism Server Status Card at bottom-right
 */
public class PanelLandingHospital extends JPanel {

    private Image wallpaperImage;
    private Image logoImage;
    private String namaInstansi = "RSUD DEMANG SEPULAU RAYA";
    private String namaPemda = "PEMERINTAH KABUPATEN LAMPUNG TENGAH";
    private String subtitle = "Sistem Informasi Manajemen Rumah Sakit";
    private String alamat = "Jl. Lintas Sumatera Nomor 4.A, Terbanggi Agung, Gunung Sugih";
    private String kontak = "(0725) 5260036 / 0811 7281 119";
    private String email = "rsuddemang01@gmail.com";
    private String versiSimrs = "24.09";

    private final CircularLogoPanel pnlLogo;
    private final JLabel lblPemda;
    private final JLabel lblSubtitle;
    private final JLabel lblNamaRS;
    private final JLabel lblAlamat;
    private final JLabel lblClock;
    private final Timer clockTimer;

    public PanelLandingHospital() {
        // Load initial offline assets if available
        try {
            java.io.File fWall = new java.io.File("./setting/wallpaper.jpg");
            if (fWall.exists()) {
                this.wallpaperImage = javax.imageio.ImageIO.read(fWall);
            }
        } catch (Exception ex) {}

        try {
            java.io.File fLogo = new java.io.File("./setting/logo.png");
            if (!fLogo.exists()) fLogo = new java.io.File("./setting/logo.jpg");
            if (fLogo.exists()) {
                this.logoImage = javax.imageio.ImageIO.read(fLogo);
            }
        } catch (Exception ex) {}

        setOpaque(false);
        setLayout(new BorderLayout());

        // -------------------------------------------------------------
        // 1. LEFT CONTENT HERO (Inside a translucent container)
        // -------------------------------------------------------------
        JPanel pnlLeftContainer = new JPanel();
        pnlLeftContainer.setOpaque(false);
        pnlLeftContainer.setLayout(new BoxLayout(pnlLeftContainer, BoxLayout.Y_AXIS));
        pnlLeftContainer.setBorder(BorderFactory.createEmptyBorder(120, 60, 20, 30));

        // Row 1: Logo + Pemda & Subtitle
        JPanel pnlHeaderRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        pnlHeaderRow.setOpaque(false);
        pnlHeaderRow.setAlignmentX(0.0f);

        pnlLogo = new CircularLogoPanel();
        pnlLogo.setPreferredSize(new Dimension(56, 56));
        if (this.logoImage != null) {
            pnlLogo.setImage(this.logoImage);
        }
        pnlHeaderRow.add(pnlLogo);

        JPanel pnlTitles = new JPanel();
        pnlTitles.setOpaque(false);
        pnlTitles.setLayout(new BoxLayout(pnlTitles, BoxLayout.Y_AXIS));

        lblPemda = new JLabel(namaPemda);
        lblPemda.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPemda.setForeground(new Color(45, 212, 191)); // Mint cyan #2dd4bf

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitle.setForeground(new Color(226, 232, 240)); // Slate-200 #e2e8f0

        pnlTitles.add(Box.createVerticalStrut(4));
        pnlTitles.add(lblPemda);
        pnlTitles.add(Box.createVerticalStrut(3));
        pnlTitles.add(lblSubtitle);

        pnlHeaderRow.add(pnlTitles);
        pnlLeftContainer.add(pnlHeaderRow);

        // Very small gap right above hospital name so logo sits neatly on top
        pnlLeftContainer.add(Box.createVerticalStrut(10));

        // Row 2: Hospital Name (Single straight line uppercase matching mockup)
        lblNamaRS = new JLabel(formatHospitalNameHtml(namaInstansi));
        lblNamaRS.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblNamaRS.setForeground(Color.WHITE);
        lblNamaRS.setAlignmentX(0.0f);
        pnlLeftContainer.add(lblNamaRS);

        pnlLeftContainer.add(Box.createVerticalStrut(8));

        // Row 3: Address
        lblAlamat = new JLabel("<html><div style='color: #cbd5e1; line-height: 1.35;'>" + alamat + "</div></html>");
        lblAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAlamat.setForeground(new Color(203, 213, 225)); // Slate-300
        lblAlamat.setAlignmentX(0.0f);
        pnlLeftContainer.add(lblAlamat);

        pnlLeftContainer.add(Box.createVerticalStrut(18));

        // Row 4: Akses Terbatas Badge
        PillBadgePanel pnlBadge = new PillBadgePanel("AKSES TERBATAS");
        pnlBadge.setAlignmentX(0.0f);
        pnlLeftContainer.add(pnlBadge);

        pnlLeftContainer.add(Box.createVerticalStrut(10));

        // Row 5: Notice description
        JLabel lblNotice = new JLabel("<html><div style='width: 440px; color: #94a3b8; line-height: 1.4;'>"
                + "Silakan masuk dengan akun pegawai untuk membuka modul pelayanan. "
                + "Seluruh aktivitas tercatat dalam log audit sistem.</div></html>");
        lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNotice.setAlignmentX(0.0f);
        pnlLeftContainer.add(lblNotice);

        pnlLeftContainer.add(Box.createVerticalStrut(18));

        // Row 6: Action Buttons
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.setAlignmentX(0.0f);

        // White Primary Log In Button
        JButton btnLoginHero = new JButton("Log In") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(226, 232, 240));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(241, 245, 249));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        try {
            javax.swing.Icon ico = widget.SvgIconLoader.load("/picture/icons/toolbar/login_dark.svg", 16, null);
            if (ico != null) btnLoginHero.setIcon(ico);
            btnLoginHero.setIconTextGap(8);
        } catch (Exception ignored) {}
        btnLoginHero.setContentAreaFilled(false);
        btnLoginHero.setBorderPainted(false);
        btnLoginHero.setFocusPainted(false);
        btnLoginHero.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLoginHero.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLoginHero.setForeground(new Color(11, 51, 44)); // Dark pine
        btnLoginHero.setPreferredSize(new Dimension(115, 38));
        btnLoginHero.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frmUtama.getInstance().triggerLogin();
            }
        });
        pnlButtons.add(btnLoginHero);

        // Dark Translucent IT Support Button
        JButton btnBantuanIT = new JButton("Bantuan & Kontak IT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(30, 41, 59, 230));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(30, 41, 59, 190));
                } else {
                    g2.setColor(new Color(15, 23, 42, 160));
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(new Color(255, 255, 255, 90));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth() - 1.2f, getHeight() - 1.2f, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        try {
            javax.swing.Icon ico = widget.SvgIconLoader.load("/picture/icons/toolbar/headset.svg", 16, "#CBD5E1");
            if (ico != null) btnBantuanIT.setIcon(ico);
            btnBantuanIT.setIconTextGap(8);
        } catch (Exception ignored) {}
        btnBantuanIT.setContentAreaFilled(false);
        btnBantuanIT.setBorderPainted(false);
        btnBantuanIT.setFocusPainted(false);
        btnBantuanIT.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBantuanIT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBantuanIT.setForeground(Color.WHITE);
        btnBantuanIT.setPreferredSize(new Dimension(205, 38));
        btnBantuanIT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tampilkanBantuanIT();
            }
        });
        pnlButtons.add(btnBantuanIT);

        pnlLeftContainer.add(pnlButtons);

        add(pnlLeftContainer, BorderLayout.WEST);

        // -------------------------------------------------------------
        // 2. BOTTOM RIGHT FLOATING STATUS CARD (Glassmorphism)
        // -------------------------------------------------------------
        JPanel pnlBottomArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 35, 25));
        pnlBottomArea.setOpaque(false);

        StatusGlassCard pnlStatusCard = new StatusGlassCard();
        lblClock = pnlStatusCard.getClockLabel();
        pnlBottomArea.add(pnlStatusCard);

        add(pnlBottomArea, BorderLayout.SOUTH);

        // Realtime clock timer (updates every second)
        clockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateClock();
            }
        });
        clockTimer.start();
        updateClock();
    }

    private void updateClock() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
        if (lblClock != null) {
            lblClock.setText(sdf.format(new Date()));
        }
    }

    /**
     * Updates all hospital data dynamically from database setting table.
     */
    public void updateHospitalData(String nama, String pemda, String alm, String ktk, String eml, Image logo, Image wallpaper) {
        if (nama != null && !nama.trim().isEmpty()) {
            this.namaInstansi = nama.trim();
            lblNamaRS.setText(formatHospitalNameHtml(this.namaInstansi));
        }
        if (pemda != null && !pemda.trim().isEmpty()) {
            this.namaPemda = pemda.trim().toUpperCase();
            if (!this.namaPemda.startsWith("PEMERINTAH")) {
                this.namaPemda = "PEMERINTAH KABUPATEN " + this.namaPemda;
            }
            lblPemda.setText(this.namaPemda);
        }
        if (alm != null && !alm.trim().isEmpty()) {
            this.alamat = alm.trim();
            lblAlamat.setText("<html><div style='color: #cbd5e1; line-height: 1.35;'>" + this.alamat + "</div></html>");
        }
        if (ktk != null && !ktk.trim().isEmpty()) {
            this.kontak = ktk.trim();
        }
        if (eml != null && !eml.trim().isEmpty()) {
            this.email = eml.trim();
        }
        if (logo != null) {
            this.logoImage = logo;
            pnlLogo.setImage(logo);
        }
        if (wallpaper != null) {
            this.wallpaperImage = wallpaper;
        }
        repaint();
    }

    private static String formatHospitalNameHtml(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return "<html><nobr><b>RSUD DEMANG SEPULAU RAYA</b></nobr></html>";
        }
        return "<html><nobr><b>" + rawName.trim().toUpperCase() + "</b></nobr></html>";
    }

    private static String toTitleCase(String word) {
        if (word == null || word.isEmpty()) return "";
        if (word.equalsIgnoreCase("RSUD") || word.equalsIgnoreCase("RSU") || word.equalsIgnoreCase("RS") || word.equalsIgnoreCase("UPTD")) {
            return word.toUpperCase();
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    public void setWallpaperImage(Image img) {
        this.wallpaperImage = img;
        repaint();
    }

    public void setLogoImage(Image img) {
        this.logoImage = img;
        pnlLogo.setImage(img);
        repaint();
    }

    private void tampilkanBantuanIT() {
        String msg = "<html><body style='width: 320px; font-family: Segoe UI, sans-serif;'>"
                + "<h3 style='color: #0b332c; margin-bottom: 6px;'>Pusat Bantuan & Kontak IT</h3>"
                + "<p style='margin-bottom: 8px; color: #475569;'>Informasi kontak dukungan SIMRS " + namaInstansi + ":</p>"
                + "<table style='color: #1e293b; font-size: 11px;'>"
                + "<tr><td><b>Telepon / WA</b></td><td>: " + (kontak.isEmpty() ? "-" : kontak) + "</td></tr>"
                + "<tr><td><b>Email Resmi</b></td><td>: " + (email.isEmpty() ? "-" : email) + "</td></tr>"
                + "<tr><td><b>Layanan IT</b></td><td>: Unit SIMRS & EDP (24 Jam)</td></tr>"
                + "</table>"
                + "<p style='margin-top: 10px; color: #64748b; font-size: 10px;'>Silakan hubungi administrator bila mengalami kendala akses sistem.</p>"
                + "</body></html>";
        JOptionPane.showMessageDialog(this, msg, "Kontak IT & Bantuan", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // 1. Draw Wallpaper
        if (wallpaperImage != null) {
            int imgW = wallpaperImage.getWidth(null);
            int imgH = wallpaperImage.getHeight(null);
            if (imgW > 0 && imgH > 0) {
                double scale = Math.max((double) w / imgW, (double) h / imgH);
                int drawW = (int) Math.ceil(imgW * scale);
                int drawH = (int) Math.ceil(imgH * scale);
                int drawX = (w - drawW) / 2;
                int drawY = (h - drawH) / 2;
                g2.drawImage(wallpaperImage, drawX, drawY, drawW, drawH, null);
            } else {
                g2.drawImage(wallpaperImage, 0, 0, w, h, null);
            }
        } else {
            // Gradasi teal gelap sbg fallback - VERTIKAL (0,0 -> 0,h), BUKAN
            // diagonal. GradientPaint(0,0,...,w,h,...) sebelumnya menarik
            // garis dari sudut kiri-atas ke kanan-bawah (diagonal sejati),
            // itu yg tampak sbg "gradasi miring" begitu wallpaper null/tak
            // termuat - persis skenario yg dilaporkan.
            GradientPaint bg = new GradientPaint(0, 0, new Color(11, 41, 35), 0, h, new Color(15, 66, 57));
            g2.setPaint(bg);
            g2.fillRect(0, 0, w, h);
        }

        // 2. Ultra-Smooth Seamless Dark Gradient Overlay (Tanpa Garis / Batas Potongan)
        // Satu kesatuan utuh LinearGradientPaint membentang lebar (78% lebar layar)
        // dengan transisi bertahap yang sangat halus sehingga menyatu sempurna dengan foto.
        int gradEnd = Math.max(900, (int) (w * 0.78f));
        float[] fractions = { 0.0f, 0.32f, 0.46f, 0.62f, 0.78f, 1.0f };
        Color[] colors = {
            new Color(6, 20, 18, 235), // Gelap pekat di sisi teks
            new Color(6, 20, 18, 230), // Menjaga kontras tombol & teks
            new Color(6, 20, 18, 155), // Melandai sangat lembut
            new Color(6, 20, 18, 80),  // Transisi alami
            new Color(6, 20, 18, 20),  // Sangat tipis mendekati transparan
            new Color(6, 20, 18, 0)    // Benar-benar nol, tanpa garis batas apapun
        };

        java.awt.LinearGradientPaint seamlessGradient = new java.awt.LinearGradientPaint(
                0, 0, gradEnd, 0, fractions, colors
        );
        g2.setPaint(seamlessGradient);
        g2.fillRect(0, 0, gradEnd, h);

        // Soft overall dark vignette
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRect(0, 0, w, h);

        g2.dispose();
    }

    // -------------------------------------------------------------
    // SUBCOMPONENT: Circular Glass Logo Frame
    // -------------------------------------------------------------
    private static class CircularLogoPanel extends JPanel {
        private Image img;

        public CircularLogoPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(74, 74));
        }

        public void setImage(Image img) {
            this.img = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int d = Math.min(getWidth(), getHeight()) - 6;
            int x = (getWidth() - d) / 2;
            int y = (getHeight() - d) / 2;

            // Semi-transparent circular glass backing
            g2.setColor(new Color(255, 255, 255, 30));
            g2.fillOval(x, y, d, d);

            if (img != null) {
                Shape oldClip = g2.getClip();
                g2.setClip(new Ellipse2D.Double(x + 2, y + 2, d - 4, d - 4));
                g2.drawImage(img, x + 2, y + 2, d - 4, d - 4, null);
                g2.setClip(oldClip);
            } else {
                // Clean fallback placeholder
                g2.setColor(new Color(255, 255, 255, 180));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "Logo RS";
                g2.drawString(txt, x + (d - fm.stringWidth(txt)) / 2, y + (d / 2) + 4);
            }

            // Outer white glass border ring
            g2.setColor(new Color(255, 255, 255, 120));
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawOval(x, y, d, d);

            g2.dispose();
        }
    }

    // -------------------------------------------------------------
    // SUBCOMPONENT: Akses Terbatas Pill Badge
    // -------------------------------------------------------------
    private static class PillBadgePanel extends JPanel {
        private final String text;

        public PillBadgePanel(String text) {
            this.text = text;
            setOpaque(false);
            setPreferredSize(new Dimension(148, 28));
            setMaximumSize(new Dimension(148, 28));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background pill
            g2.setColor(new Color(13, 148, 136, 45));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));

            // Border pill
            g2.setColor(new Color(45, 212, 191, 120));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, h, h));

            // Draw lock icon
            int iconX = 12;
            int iconY = 7;
            g2.setColor(new Color(94, 234, 212));
            // Lock body
            g2.fillRoundRect(iconX, iconY + 5, 10, 8, 2, 2);
            // Lock shackle
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawArc(iconX + 2, iconY, 6, 8, 0, 180);

            // Badge text
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(new Color(94, 234, 212));
            g2.drawString(text, iconX + 16, h - 9);

            g2.dispose();
        }
    }

    // -------------------------------------------------------------
    // SUBCOMPONENT: Floating Glass Status Card (Bottom-Right)
    // -------------------------------------------------------------
    private static class StatusGlassCard extends JPanel {
        private final JLabel lblClock;

        public StatusGlassCard() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            setPreferredSize(new Dimension(340, 58));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 8, 0, 8);
            gbc.fill = GridBagConstraints.VERTICAL;

            // Column 1: Versi SIMRS
            JPanel col1 = createColumn("VERSI SIMRS", "24.09", null);
            gbc.gridx = 0;
            add(col1, gbc);

            // Divider 1
            gbc.gridx = 1;
            add(createDivider(), gbc);

            // Column 2: Server Status
            JPanel col2 = createColumn("SERVER", "Terhubung", new Color(34, 197, 94));
            gbc.gridx = 2;
            add(col2, gbc);

            // Divider 2
            gbc.gridx = 3;
            add(createDivider(), gbc);

            // Column 3: Server Clock
            lblClock = new JLabel("00.00");
            lblClock.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblClock.setForeground(Color.WHITE);

            JPanel col3 = new JPanel();
            col3.setOpaque(false);
            col3.setLayout(new BoxLayout(col3, BoxLayout.Y_AXIS));
            JLabel lblH3 = new JLabel("WAKTU SERVER");
            lblH3.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lblH3.setForeground(new Color(148, 163, 184)); // Slate-400
            col3.add(lblH3);
            col3.add(Box.createVerticalStrut(2));
            col3.add(lblClock);

            gbc.gridx = 4;
            add(col3, gbc);
        }

        public JLabel getClockLabel() {
            return lblClock;
        }

        private JPanel createColumn(String header, String value, Color dotColor) {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

            JLabel lblH = new JLabel(header);
            lblH.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lblH.setForeground(new Color(148, 163, 184)); // Slate-400
            p.add(lblH);
            p.add(Box.createVerticalStrut(2));

            if (dotColor != null) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                row.setOpaque(false);

                // Small indicator dot
                JPanel dot = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(dotColor);
                        g2.fillOval(0, 4, 7, 7);
                        g2.dispose();
                    }
                };
                dot.setOpaque(false);
                dot.setPreferredSize(new Dimension(8, 16));
                row.add(dot);

                JLabel lblV = new JLabel(value);
                lblV.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblV.setForeground(Color.WHITE);
                row.add(lblV);

                p.add(row);
            } else {
                JLabel lblV = new JLabel(value);
                lblV.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblV.setForeground(Color.WHITE);
                p.add(lblV);
            }
            return p;
        }

        private JPanel createDivider() {
            JPanel div = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.drawLine(getWidth() / 2, 4, getWidth() / 2, getHeight() - 4);
                    g2.dispose();
                }
            };
            div.setOpaque(false);
            div.setPreferredSize(new Dimension(12, 32));
            return div;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Dark translucent card background
            g2.setColor(new Color(15, 23, 42, 195));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

            // Subtle border
            g2.setColor(new Color(255, 255, 255, 35));
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1.0f, h - 1.0f, 12, 12));

            g2.dispose();
        }
    }
}
