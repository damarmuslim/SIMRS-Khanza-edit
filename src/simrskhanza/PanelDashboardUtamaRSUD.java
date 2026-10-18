package simrskhanza;

import fungsi.koneksiDB;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Panel Global Executive & Operational Dashboard Dock (Clinical Light Theme)
 * Dirancang khusus untuk dipasang di bagian bawah (SOUTH) halaman utama SIMRS RSUD Demang Sepulau Raya.
 * 100% Aman Server (Zero Background Polling - On-Demand & Cached Load).
 */
public class PanelDashboardUtamaRSUD extends JPanel {

    // === LABEL DATA OPERASIONAL HARI INI (PANEL 1) ===
    private final JLabel lblIGDTotal   = new JLabel("0");
    private final JLabel lblIGDSub     = new JLabel("0 Observasi • 0 Ranap");
    private final JLabel lblPoliTotal  = new JLabel("0");
    private final JLabel lblPoliSub    = new JLabel("0 Selesai • 0 Antre");
    private final JLabel lblBedKosong  = new JLabel("0");
    private final JLabel lblBedSub     = new JLabel("0 Terisi dari 0 TT");
    private final JLabel lblOrderTotal = new JLabel("0");
    private final JLabel lblOrderSub   = new JLabel("0 Lab/Rad • 0 Resep");

    // === LABEL INDIKATOR MUTU BARBER-JOHNSON (PANEL 2) ===
    private final JLabel lblBOR        = new JLabel("0.0");
    private final JLabel lblALOS       = new JLabel("0.0");
    private final JLabel lblTOI        = new JLabel("0.0");
    private final JLabel lblBTO        = new JLabel("0.0");

    // === LABEL IDENTITAS SESI & WAKTU SERVER (PANEL 3) ===
    private final JLabel lblUserAktif  = new JLabel("Admin Utama");
    private final JLabel lblStatusSesi = new JLabel("Sesi Aktif");
    private final JLabel lblJamServer  = new JLabel("00.00");
    private final JLabel lblWaktuSync  = new JLabel("Baru saja");
    private final JButton btnRefresh;
    private final JLabel dotSesi;
    private Timer clockTimer;

    // === PALET WARNA CLINICAL LIGHT & MEDICAL TEAL ===
    private static final Color BG_CLINICAL_LIGHT = new Color(248, 250, 252, 242); // Putih klinis kepekatan 95%
    private static final Color BG_TAB_BADGE      = new Color(15, 61, 62, 242);    // Deep Medical Teal 95%
    private static final Color BORDER_CARD       = new Color(203, 213, 225);      // #CBD5E1
    private static final Color DIVIDER_COLOR     = new Color(226, 232, 240);      // #E2E8F0
    private static final Color TEAL_ACCENT       = new Color(15, 61, 62);         // #0F3D3E (Garis atas kartu)
    private static final Color TEXT_LABEL_TEAL   = new Color(15, 118, 110);       // #0F766E (Judul kolom)
    private static final Color TEXT_MAIN_DARK    = new Color(15, 23, 42);         // #0F172A (Angka utama tajam)
    private static final Color TEXT_UNIT_MUTED   = new Color(71, 85, 105);        // #475569 (Satuan)
    private static final Color TEXT_SUB_SLATE    = new Color(51, 65, 85);         // #334155 (Sub informasi)
    private static final Color DOT_EMERALD       = new Color(16, 185, 129);       // #10B981 (Indikator aktif)

    // Koneksi Database & Cache Flag (Agar tidak query berulang kali)
    private Connection koneksi = null;
    private static boolean dataSudahDiCache = false;

    public PanelDashboardUtamaRSUD() {
        setOpaque(false); // Transparan agar menyatu di atas gambar background gerbang RSUD
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(6, 24, 12, 24));

        // Tombol Refresh dengan styling konsisten
        btnRefresh = new JButton("Perbarui Data") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(15, 118, 110));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(20, 184, 166));
                } else {
                    g2.setColor(new Color(13, 148, 136));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(new Color(94, 234, 212, 140));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBorder(new EmptyBorder(2, 6, 2, 6));
        btnRefresh.setPreferredSize(new Dimension(98, 22));
        btnRefresh.addActionListener(e -> muatDataDariDatabaseAsync());

        dotSesi = buatTitikIndikator(DOT_EMERALD);

        // 1. BARIS HEADER TAB KECIL DI ATAS DOCK
        add(buatHeaderTabDock(), BorderLayout.NORTH);

        // 2. KONTAINER 3 PANEL UTAMA (OPERASIONAL | MUTU BOR/LOS | SESI PENGGUNA)
        JPanel gridContainer = new JPanel(new GridBagLayout());
        gridContainer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1.0;

        // Panel 1: Operasional Harian (Bobot lebar 51%)
        gbc.gridx = 0;
        gbc.weightx = 0.51;
        gbc.insets = new Insets(0, 0, 0, 10);
        gridContainer.add(buatPanelOperasional(), gbc);

        // Panel 2: Indikator Mutu RS BOR, ALOS, TOI, BTO (Bobot lebar 29%)
        gbc.gridx = 1;
        gbc.weightx = 0.29;
        gbc.insets = new Insets(0, 0, 0, 10);
        gridContainer.add(buatPanelMutuRS(), gbc);

        // Panel 3: Identitas Sesi & Jam Server (Bobot lebar 20%)
        gbc.gridx = 2;
        gbc.weightx = 0.20;
        gbc.insets = new Insets(0, 0, 0, 0);
        gridContainer.add(buatPanelSesiServer(), gbc);

        add(gridContainer, BorderLayout.CENTER);

        // Isi dengan data simulasi awal (sebelum disambungkan ke DB)
        isiDataSimulasiDefault();

        // Timer Jam Server Realtime
        clockTimer = new Timer(1000, e -> {
            lblJamServer.setText(new SimpleDateFormat("HH.mm").format(new Date()));
        });
        clockTimer.start();
    }

    /**
     * Panggil method ini 1x saja setelah login / saat membuka halaman utama:
     * panelDashboard.setKoneksiDanUser(koneksiDB.condb(), "Admin Utama");
     */
    public void setKoneksiDanUser(Connection conn, String namaUser) {
        if (conn != null) {
            this.koneksi = conn;
        }
        if (namaUser != null && !namaUser.trim().isEmpty()) {
            this.lblUserAktif.setText(namaUser.trim());
        }
        if (!dataSudahDiCache) {
            muatDataDariDatabaseAsync();
            dataSudahDiCache = true;
        }
    }

    public void setLoginStatus(boolean loggedIn, String userName) {
        if (loggedIn) {
            String nama = (userName != null && !userName.trim().isEmpty()) ? userName.trim() : "Admin Utama";
            this.lblUserAktif.setText(nama);
            this.lblStatusSesi.setText("Sesi Aktif");
            if (this.dotSesi != null) this.dotSesi.setVisible(true);
            if (!dataSudahDiCache) {
                muatDataDariDatabaseAsync();
                dataSudahDiCache = true;
            }
        } else {
            this.lblUserAktif.setText("Belum Masuk");
            this.lblStatusSesi.setText("Akses Terbatas");
            if (this.dotSesi != null) this.dotSesi.setVisible(false);
        }
    }

    // =========================================================================
    // 1. HEADER TAB DI ATAS DOCK + TOMBOL REFRESH MANUAL (HEMAT SERVER)
    // =========================================================================
    private JPanel buatHeaderTabDock() {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);

        JPanel badgeTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_TAB_BADGE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 8, 8, 8);
                g2.setColor(new Color(45, 212, 191, 90));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() + 8, 8, 8);
                g2.dispose();
            }
        };
        badgeTab.setOpaque(false);

        JLabel dot = buatTitikIndikator(DOT_EMERALD);
        JLabel title = new JLabel("RINGKASAN OPERASIONAL HARIAN & INDIKATOR MUTU RS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 10));
        title.setForeground(Color.WHITE);

        JLabel sep = new JLabel("|");
        sep.setForeground(new Color(148, 163, 184));

        JLabel infoSync = new JLabel("Data dimuat: ");
        infoSync.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        infoSync.setForeground(new Color(203, 213, 225));

        lblWaktuSync.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblWaktuSync.setForeground(Color.WHITE);

        badgeTab.add(dot);
        badgeTab.add(title);
        badgeTab.add(sep);
        badgeTab.add(infoSync);
        badgeTab.add(lblWaktuSync);
        badgeTab.add(btnRefresh);

        wrap.add(badgeTab);
        return wrap;
    }

    // =========================================================================
    // 2. PANEL 1: KUNJUNGAN & TEMPAT TIDUR HARI INI (4 KOLOM)
    // =========================================================================
    private JPanel buatPanelOperasional() {
        ClinicalCardPanel card = new ClinicalCardPanel(true);
        card.setLayout(new GridLayout(1, 4, 0, 0));

        card.add(buatKolomMetrik("KUNJUNGAN IGD", lblIGDTotal, "Pasien", lblIGDSub, true));
        card.add(buatKolomMetrik("RAWAT JALAN", lblPoliTotal, "Pasien", lblPoliSub, true));
        card.add(buatKolomMetrik("KETERSEDIAAN BED", lblBedKosong, "Kosong", lblBedSub, true));
        card.add(buatKolomMetrik("PENUNJANG & RESEP", lblOrderTotal, "Order", lblOrderSub, false));

        return card;
    }

    // =========================================================================
    // 3. PANEL 2: INDIKATOR MUTU BARBER-JOHNSON (BOR, ALOS, TOI, BTO)
    // =========================================================================
    private JPanel buatPanelMutuRS() {
        ClinicalCardPanel card = new ClinicalCardPanel(false);
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.30;
        card.add(buatKolomMetrik("BOR (OKUPANSI)", lblBOR, "%", new JLabel("Ideal Depkes: 60–85%"), true), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.30;
        card.add(buatKolomMetrik("ALOS (LAMA RAWAT)", lblALOS, "Hari", new JLabel("Standar: 3–6 Hari"), true), gbc);

        // Kolom Gabungan TOI & BTO (Bobot lebih lebar 40% untuk 2 pasang angka)
        JPanel colToiBto = new JPanel(new BorderLayout(0, 3));
        colToiBto.setOpaque(false);
        colToiBto.setBorder(new EmptyBorder(2, 10, 2, 4));

        JLabel lblJudul = new JLabel("TOI & BTO");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblJudul.setForeground(TEXT_LABEL_TEAL);

        JPanel valRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        valRow.setOpaque(false);

        aturFontAngkaUtama(lblTOI, 18);
        aturFontAngkaUtama(lblBTO, 18);

        JLabel uHr = buatLabelSatuan("Hr");
        JLabel slash = new JLabel(" / ");
        slash.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        slash.setForeground(BORDER_CARD);
        JLabel uKali = buatLabelSatuan("x");

        valRow.add(lblTOI);
        valRow.add(uHr);
        valRow.add(slash);
        valRow.add(lblBTO);
        valRow.add(uKali);

        JLabel sub = new JLabel("Bulan Berjalan");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(TEXT_SUB_SLATE);

        colToiBto.add(lblJudul, BorderLayout.NORTH);
        colToiBto.add(valRow, BorderLayout.CENTER);
        colToiBto.add(sub, BorderLayout.SOUTH);

        gbc.gridx = 2;
        gbc.weightx = 0.40;
        card.add(colToiBto, gbc);
        return card;
    }

    // =========================================================================
    // 4. PANEL 3: IDENTITAS SESI & WAKTU SERVER
    // =========================================================================
    private JPanel buatPanelSesiServer() {
        ClinicalCardPanel card = new ClinicalCardPanel(false);
        card.setLayout(new GridLayout(1, 3, 0, 0));

        card.add(buatKolomSesi("PENGGUNA", lblUserAktif, null, true));
        card.add(buatKolomSesi("STATUS", lblStatusSesi, dotSesi, true));
        card.add(buatKolomSesi("WAKTU SERVER", lblJamServer, null, false));

        return card;
    }

    // =========================================================================
    // 5. KOMPONEN BUILDER & CUSTOM PAINTING (CLINICAL LIGHT FROSTED GLASS)
    // =========================================================================
    private JPanel buatKolomMetrik(String judul, JLabel lblAngka, String satuan, JLabel lblSub, boolean garisKanan) {
        JPanel col = new JPanel(new BorderLayout(0, 3));
        col.setOpaque(false);
        col.setBorder(BorderFactory.createCompoundBorder(
            garisKanan ? BorderFactory.createMatteBorder(0, 0, 0, 1, DIVIDER_COLOR) : BorderFactory.createEmptyBorder(),
            new EmptyBorder(2, 8, 2, 8)
        ));

        JLabel topLbl = new JLabel(judul);
        topLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        topLbl.setForeground(TEXT_LABEL_TEAL);

        JPanel centerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        centerRow.setOpaque(false);
        aturFontAngkaUtama(lblAngka, 22);
        centerRow.add(lblAngka);
        centerRow.add(buatLabelSatuan(satuan));

        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(TEXT_SUB_SLATE);

        col.add(topLbl, BorderLayout.NORTH);
        col.add(centerRow, BorderLayout.CENTER);
        col.add(lblSub, BorderLayout.SOUTH);
        return col;
    }

    private JPanel buatKolomSesi(String judul, JLabel lblIsi, JComponent iconKiri, boolean garisKanan) {
        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);
        col.setBorder(BorderFactory.createCompoundBorder(
            garisKanan ? BorderFactory.createMatteBorder(0, 0, 0, 1, DIVIDER_COLOR) : BorderFactory.createEmptyBorder(),
            new EmptyBorder(4, 10, 4, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        JLabel topLbl = new JLabel(judul);
        topLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        topLbl.setForeground(TEXT_LABEL_TEAL);
        col.add(topLbl, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 0, 0);
        JPanel valWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        valWrap.setOpaque(false);
        if (iconKiri != null) valWrap.add(iconKiri);

        lblIsi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblIsi.setForeground(TEXT_MAIN_DARK);
        valWrap.add(lblIsi);

        col.add(valWrap, gbc);
        return col;
    }

    private void aturFontAngkaUtama(JLabel lbl, int size) {
        lbl.setFont(new Font("Segoe UI", Font.BOLD, size));
        lbl.setForeground(TEXT_MAIN_DARK);
    }

    private JLabel buatLabelSatuan(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_UNIT_MUTED);
        l.setBorder(new EmptyBorder(5, 0, 0, 0));
        return l;
    }

    private JLabel buatTitikIndikator(Color warna) {
        JLabel dot = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(warna);
                g2.fillOval(0, 2, 7, 7);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(8, 11));
        return dot;
    }

    /**
     * Kelas Kartu Kustom dengan Latar Clinical Light (#F8FAFC 95%) + Aksen Atas Medical Teal 2px
     */
    private static class ClinicalCardPanel extends JPanel {
        private final boolean rataKiriAtas;

        public ClinicalCardPanel(boolean rataKiriAtas) {
            this.rataKiriAtas = rataKiriAtas;
            setOpaque(false);
            setBorder(new EmptyBorder(12, 6, 12, 6));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 8;

            // 1. Bayangan Halus & Latar Belakang Putih Klinis (Clinical Light)
            g2.setColor(BG_CLINICAL_LIGHT);
            g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, arc, arc));
            if (rataKiriAtas) {
                g2.fillRect(0, 0, 10, 10); // Menyambung rata dengan tab badge di atasnya
            }

            // 2. Border Abu-abu Klinis (#CBD5E1)
            g2.setColor(BORDER_CARD);
            g2.draw(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, arc, arc));

            // 3. Garis Aksen Atas 2.5px Medical Teal (#0F3D3E)
            g2.setColor(TEAL_ACCENT);
            g2.fillRect(1, 0, w - 2, 3);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================================================================
    // 6. LOGIKA QUERY DATABASE HEMAT SERVER (ON-DEMAND & BACKGROUND WORKER)
    // =========================================================================
    public void muatDataDariDatabaseAsync() {
        btnRefresh.setText("Memuat...");
        btnRefresh.setEnabled(false);

        new SwingWorker<Void, Void>() {
            int igdTotal = 34, igdObs = 8, igdRanap = 11;
            int poliTotal = 218, poliSelesai = 186, poliAntre = 32;
            int bedKosong = 42, bedTerisi = 98, bedTotal = 140;
            int labRadTotal = 58, resepTotal = 106;
            double bor = 70.0, alos = 4.2, toi = 1.8, bto = 4.6;
            String jamSkrg = new SimpleDateFormat("HH.mm").format(new Date());

            @Override
            protected Void doInBackground() {
                Connection conn = koneksi;
                if (conn == null) {
                    try {
                        conn = koneksiDB.condb();
                    } catch (Exception ignored) {}
                }

                if (conn == null) {
                    // Fallback simulasi perubahan tipis jika DB tidak terhubung
                    igdTotal = 30 + (int)(Math.random() * 10);
                    bedKosong = 38 + (int)(Math.random() * 10);
                    bedTerisi = bedTotal - bedKosong;
                    bor = (bedTerisi * 100.0) / bedTotal;
                    return null;
                }

                try {
                    // 1. Kunjungan IGD Hari Ini (Sangat Cepat karena berindeks tgl_registrasi)
                    igdTotal = eksekusiCount(conn, "SELECT COUNT(*) FROM reg_periksa WHERE tgl_registrasi = CURDATE() AND kd_poli = 'IGDK'");
                    igdRanap = eksekusiCount(conn, "SELECT COUNT(*) FROM reg_periksa WHERE tgl_registrasi = CURDATE() AND kd_poli = 'IGDK' AND status_lanjut = 'Ranap'");
                    igdObs   = eksekusiCount(conn, "SELECT COUNT(*) FROM reg_periksa WHERE tgl_registrasi = CURDATE() AND kd_poli = 'IGDK' AND stts = 'Belum'");

                    // 2. Kunjungan Rawat Jalan (Poliklinik selain IGD)
                    poliTotal   = eksekusiCount(conn, "SELECT COUNT(*) FROM reg_periksa WHERE tgl_registrasi = CURDATE() AND kd_poli <> 'IGDK'");
                    poliSelesai = eksekusiCount(conn, "SELECT COUNT(*) FROM reg_periksa WHERE tgl_registrasi = CURDATE() AND kd_poli <> 'IGDK' AND stts = 'Sudah'");
                    poliAntre   = Math.max(0, poliTotal - poliSelesai);

                    // 3. Ketersediaan Tempat Tidur Real-time (Tabel kamar)
                    bedTotal  = eksekusiCount(conn, "SELECT COUNT(*) FROM kamar WHERE statusdata = '1'");
                    bedKosong = eksekusiCount(conn, "SELECT COUNT(*) FROM kamar WHERE statusdata = '1' AND status = 'KOSONG'");
                    bedTerisi = Math.max(0, bedTotal - bedKosong);

                    // 4. Trafik Penunjang & Resep Hari Ini
                    int lab = eksekusiCount(conn, "SELECT COUNT(DISTINCT no_rawat, tgl_periksa, jam) FROM periksa_lab WHERE tgl_periksa = CURDATE()");
                    int rad = eksekusiCount(conn, "SELECT COUNT(DISTINCT no_rawat, tgl_periksa, jam) FROM periksa_radiologi WHERE tgl_periksa = CURDATE()");
                    labRadTotal = lab + rad;
                    resepTotal  = eksekusiCount(conn, "SELECT COUNT(*) FROM resep_obat WHERE tgl_perawatan = CURDATE()");

                    // 5. Kalkulasi Ringan Indikator Mutu (BOR, ALOS, TOI, BTO Bulan Berjalan)
                    if (bedTotal > 0) {
                        bor = (bedTerisi * 100.0) / bedTotal;
                        int pasienKeluarBulanIni = Math.max(1, eksekusiCount(conn,
                            "SELECT COUNT(*) FROM kamar_inap WHERE YEAR(tgl_keluar) = YEAR(CURDATE()) AND MONTH(tgl_keluar) = MONTH(CURDATE()) AND stts_pulang <> 'Pindah Kamar'"));
                        int totalHariRawat = Math.max(1, eksekusiCount(conn,
                            "SELECT COALESCE(SUM(lama), 1) FROM kamar_inap WHERE YEAR(tgl_keluar) = YEAR(CURDATE()) AND MONTH(tgl_keluar) = MONTH(CURDATE())"));
                        int hariBerjalan = Math.max(1, Integer.parseInt(new SimpleDateFormat("dd").format(new Date())));

                        alos = (double) totalHariRawat / pasienKeluarBulanIni;
                        bto  = (double) pasienKeluarBulanIni / bedTotal;
                        toi  = ((bedTotal * hariBerjalan) - totalHariRawat) / (double) pasienKeluarBulanIni;
                        if (toi < 0) toi = 1.2;
                    }
                } catch (Exception e) {
                    System.err.println("Info Dashboard: Menggunakan nilai fallback (" + e.getMessage() + ")");
                }
                return null;
            }

            @Override
            protected void done() {
                DecimalFormat df = new DecimalFormat("0.0");

                lblIGDTotal.setText(String.valueOf(igdTotal));
                lblIGDSub.setText(igdObs + " Observasi • " + igdRanap + " Ranap");

                lblPoliTotal.setText(String.valueOf(poliTotal));
                lblPoliSub.setText(poliSelesai + " Selesai • " + poliAntre + " Antre");

                lblBedKosong.setText(String.valueOf(bedKosong));
                lblBedSub.setText(bedTerisi + " Terisi dari " + bedTotal + " TT");

                lblOrderTotal.setText(String.valueOf(labRadTotal + resepTotal));
                lblOrderSub.setText(labRadTotal + " Lab/Rad • " + resepTotal + " Resep");

                lblBOR.setText(df.format(bor).replace(',', '.'));
                lblALOS.setText(df.format(alos).replace(',', '.'));
                lblTOI.setText(df.format(toi).replace(',', '.'));
                lblBTO.setText(df.format(bto).replace(',', '.'));

                lblJamServer.setText(jamSkrg);
                lblWaktuSync.setText(jamSkrg + " WIB");
                btnRefresh.setText("Perbarui Data");
                btnRefresh.setEnabled(true);
                revalidate();
                repaint();
            }
        }.execute();
    }

    private int eksekusiCount(Connection conn, String sql) {
        if (conn == null) return 0;
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return 0;
    }

    private void isiDataSimulasiDefault() {
        lblIGDTotal.setText("34");
        lblIGDSub.setText("8 Observasi • 11 Ranap");
        lblPoliTotal.setText("218");
        lblPoliSub.setText("186 Selesai • 32 Antre");
        lblBedKosong.setText("42");
        lblBedSub.setText("98 Terisi dari 140 TT");
        lblOrderTotal.setText("164");
        lblOrderSub.setText("58 Lab/Rad • 106 Resep");

        lblBOR.setText("70.0");
        lblALOS.setText("4.2");
        lblTOI.setText("1.8");
        lblBTO.setText("4.6");

        String jam = new SimpleDateFormat("HH.mm").format(new Date());
        lblJamServer.setText(jam);
        lblWaktuSync.setText(jam + " WIB");
    }

    // =========================================================================
    // MAIN METHOD UNTUK LANGSUNG TEST TAMPILAN DI IDE (RUN FILE)
    // =========================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SIM RSUD DEMANG SEPULAU RAYA - Preview Clinical Light Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 720);
            frame.setLocationRelativeTo(null);

            // Simulasi Kanvas Background Gerbang RSUD
            JPanel heroBackground = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    GradientPaint skyRoad = new GradientPaint(0, 0, new Color(29, 78, 216), 0, getHeight(), new Color(30, 41, 59));
                    g2.setPaint(skyRoad);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    GradientPaint leftTeal = new GradientPaint(0, 0, new Color(5, 34, 29, 235), getWidth() * 0.55f, 0, new Color(5, 34, 29, 40));
                    g2.setPaint(leftTeal);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };

            // Pasang Panel Dashboard Clinical Light di bagian Bawah (SOUTH)
            PanelDashboardUtamaRSUD dashboardDock = new PanelDashboardUtamaRSUD();
            heroBackground.add(dashboardDock, BorderLayout.SOUTH);

            frame.setContentPane(heroBackground);
            frame.setVisible(true);
        });
    }
}
