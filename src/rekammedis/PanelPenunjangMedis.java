package rekammedis;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Panel Penunjang Medis IGD - Standar Formal & Elegan RS Pemerintah (Argon)
 * Terdiri dari Grid 2x2:
 * 1. Laborat (dengan badge realtime & tombol cek hasil)
 * 2. Radiologi (dengan badge realtime & tombol cek hasil)
 * 3. EKG Split (Kiri: Teks bacaan + preset chip, Kanan: Foto EKG + Paste WA + Lightbox Zoom Viewer)
 * 4. Pemeriksaan Lainnya (dengan badge realtime & preset chip)
 * Toolbar Atas: Tarik Semua Penunjang + Tips Tombol Cepat
 */
public class PanelPenunjangMedis extends JPanel {

    // === 4 TEXTAREA UTAMA (Bisa menerima instance yang sudah ada atau membuat baru) ===
    public JTextArea txtLaborat;
    public JTextArea txtRadiologi;
    public JTextArea txtEKG;
    public JTextArea txtPemeriksaanLain;

    // === TOMBOL TARIK DATA (ON-DEMAND / TANPA POLLING TIMER) ===
    public JButton btnTarikSemua = new JButton("Tarik Semua Penunjang (Lab & Radiologi)") {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) {
                g2.setColor(new Color(204, 251, 241));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(240, 253, 250));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }

            // Border tegas & hover outline
            if (getModel().isRollover()) {
                g2.setColor(TEAL_RSUD);
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(TEAL_RSUD);
                g2.setStroke(new BasicStroke(1.2f));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    };
    public JButton btnCekLab = new JButton("Cek Hasil");
    public JButton btnCekRad = new JButton("Cek Hasil");
    public JButton btnCekEkg = new JButton("Cek Hasil");

    // === PENAMPUNG GAMBAR EKG (Siap di-upload ke server / berkas rawat) ===
    private BufferedImage gambarEKGAsli = null;
    private File fileEKGTerpilih = null;
    private JLabel lblThumbImage = new JLabel("", SwingConstants.CENTER);
    private JLabel lblMetaUkuran = new JLabel("");
    private JButton btnHapusFotoEKG = new JButton("Hapus");

    // === CACHE RAM UNTUK CHIP SERVER (Hanya di-load 1x agar Server 100% Aman/Tidak Lag) ===
    private static boolean cacheSudahDimuat = false;
    private static final List<ChipItem> cacheChipEKG = new ArrayList<>();
    private static final List<ChipItem> cacheChipLain = new ArrayList<>();

    private JPanel panelChipEKG = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
    private JPanel panelChipLain = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));

    // Badge Status Tiap Kartu
    private JLabel badgeLab = new JLabel();
    private JLabel badgeRad = new JLabel();
    private JLabel badgeEKG = new JLabel();
    private JLabel badgeLain = new JLabel();

    // Aksen Warna HANYA di Garis Samping Kiri Card (#0F3D3E), Header Abu-abu Halus (#F8FAFC)
    private final Color TEAL_RSUD    = new Color(15, 61, 62);         // #0F3D3E (Aksen Garis Samping Kiri Saja)
    private final Color BORDER_CARD  = new Color(203, 213, 225);      // #CBD5E1 (Border netral abu-abu)
    private final Color HEADER_CARD  = new Color(248, 250, 252);      // #F8FAFC (Abu-abu terang untuk header)
    private final Color BORDER_HALUS = new Color(226, 232, 240);      // #E2E8F0 (Garis pemisah netral)
    private final Color TEKS_UTAMA   = new Color(30, 41, 59);         // #1E293B (Teks isi gelap standar)
    private final Color TEKS_JUDUL   = new Color(15, 23, 42);         // #0F172A (Judul kartu gelap tegas)

    // Koneksi Database Opsional (Hubungkan dengan koneksiDB.condb() milik Khanza)
    private Connection koneksiServer = null;
    private String kodeDokterAktif = "-";

    public static class ChipItem {
        public int id;
        public String kategori;
        public String judul;
        public String isi;
        public ChipItem(int id, String kategori, String judul, String isi) {
            this.id = id; this.kategori = kategori; this.judul = judul; this.isi = isi;
        }
    }

    public PanelPenunjangMedis() {
        this(new JTextArea(), new JTextArea(), new JTextArea(), new JTextArea());
    }

    public PanelPenunjangMedis(JTextArea txtLab, JTextArea txtRad, JTextArea txtEkg, JTextArea txtLain) {
        this.txtLaborat = (txtLab != null) ? txtLab : new JTextArea();
        this.txtRadiologi = (txtRad != null) ? txtRad : new JTextArea();
        this.txtEKG = (txtEkg != null) ? txtEkg : new JTextArea();
        this.txtPemeriksaanLain = (txtLain != null) ? txtLain : new JTextArea();

        inisialisasiUI();
    }

    private void inisialisasiUI() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setPreferredSize(new Dimension(840, 540));
        setMinimumSize(new Dimension(600, 500));

        // 1. TOOLBAR ATAS
        add(buatToolbarAtas(), BorderLayout.NORTH);

        // 2. GRID 2x2: BARIS ATAS (LAB & RADIOLOGI) | BARIS BAWAH (EKG & LAINNYA)
        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);

        grid.add(buatCardStandar("Laborat", txtLaborat, badgeLab, btnCekLab, null));
        grid.add(buatCardStandar("Radiologi", txtRadiologi, badgeRad, btnCekRad, null));
        grid.add(buatCardEKGSplit());
        grid.add(buatCardStandar("Pemeriksaan Lainnya", txtPemeriksaanLain, badgeLain, null, panelChipLain));

        add(grid, BorderLayout.CENTER);

        // Pasang menu klik kanan "Jadikan Tombol Cepat (Chip)" pada TextArea EKG & Lainnya
        pasangMenuBlokTeksKeChip(txtEKG, "EKG");
        pasangMenuBlokTeksKeChip(txtPemeriksaanLain, "LAINNYA");

        // Muat Chip (Dari Cache RAM / Server)
        muatDataChipAwal();
        perbaruiSemuaBadge();
    }

    /**
     * Hubungkan ke koneksi MySQL Khanza & kode dokter yang sedang aktif
     */
    public void setKoneksiServer(Connection conn, String kdDokter) {
        this.koneksiServer = conn;
        this.kodeDokterAktif = (kdDokter == null || kdDokter.isEmpty()) ? "-" : kdDokter;
        if (conn != null) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS master_chip_penunjang (" +
                    "  id_chip INT(11) NOT NULL AUTO_INCREMENT," +
                    "  kategori ENUM('EKG','LAINNYA') NOT NULL," +
                    "  kd_dokter VARCHAR(20) NOT NULL DEFAULT '-'," +
                    "  judul_chip VARCHAR(60) NOT NULL," +
                    "  isi_teks TEXT NOT NULL," +
                    "  PRIMARY KEY (id_chip)," +
                    "  KEY kd_dokter (kd_dokter)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1;"
                );
            } catch (Exception ignored) {}
        }
        cacheSudahDimuat = false;
        muatDataChipAwal();
    }

    // =========================================================================
    // 1. TOOLBAR ATAS (HEMAT SERVER / TANPA POLLING)
    // =========================================================================
    private JPanel buatToolbarAtas() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_HALUS),
            new EmptyBorder(0, 0, 8, 0)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        stylePrimaryButton(btnTarikSemua);
        left.add(btnTarikSemua);

        JLabel infoServer = new JLabel(
            "<html>" +
            "  <div style='line-height:130%;'>" +
            "    <font color='#0D9488'>●</font> <b>Tips 1:</b> Tarik otomatis saat buka rawat. Klik '<b>Cek Hasil</b>' untuk perbarui manual.<br>" +
            "    <font color='#0D9488'>●</font> <b>Tips 2:</b> Klik <b>[+]</b> untuk tambah tombol cepat. <b>Klik Kanan</b> pada tombol cepat untuk Ubah / Hapus." +
            "  </div>" +
            "</html>"
        );
        infoServer.setFont(new Font("Tahoma", Font.PLAIN, 10));
        infoServer.setForeground(new Color(71, 85, 105));
        infoServer.setOpaque(true);
        infoServer.setBackground(HEADER_CARD);
        infoServer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CARD),
            new EmptyBorder(4, 10, 4, 10)
        ));

        bar.add(left, BorderLayout.WEST);
        bar.add(infoServer, BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    // 2. KARTU PENUNJANG STANDAR (LABORAT, RADIOLOGI, PEMERIKSAAN LAINNYA)
    // =========================================================================
    private JPanel buatCardStandar(String judul, JTextArea area, JLabel badge, JButton btnSync, JPanel chipBar) {
        JPanel card = buatKerangkaCardSeragam();

        // Header Kartu Seragam
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(HEADER_CARD);
        head.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_HALUS),
            new EmptyBorder(5, 10, 5, 10)
        ));

        JLabel lblTitle = new JLabel(judul);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblTitle.setForeground(TEKS_JUDUL);

        JPanel rightHead = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightHead.setOpaque(false);
        rightHead.add(badge);

        if (btnSync != null) {
            styleMiniButton(btnSync);
            rightHead.add(btnSync);
        }

        head.add(lblTitle, BorderLayout.WEST);
        head.add(rightHead, BorderLayout.EAST);

        // Isi TextArea
        konfigurasiTextArea(area);
        area.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { perbaruiBadgeTeks(area, badge, judul); }
            @Override public void removeUpdate(DocumentEvent e) { perbaruiBadgeTeks(area, badge, judul); }
            @Override public void changedUpdate(DocumentEvent e) { perbaruiBadgeTeks(area, badge, judul); }
        });

        if (area.getParent() != null) {
            area.getParent().remove(area);
        }
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        if (sp.getVerticalScrollBar() != null) {
            sp.getVerticalScrollBar().setUnitIncrement(16);
            sp.getVerticalScrollBar().setBlockIncrement(64);
        }
        pasangMouseWheelBubble(sp);

        card.add(head, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);

        // Jika memiliki baris Chip Cepat di bagian bawah (Pemeriksaan Lainnya)
        if (chipBar != null) {
            JPanel footerChip = buatBarisChipContainer("LAINNYA", chipBar, cacheChipLain, area);
            card.add(footerChip, BorderLayout.SOUTH);
        }

        perbaruiBadgeTeks(area, badge, judul);
        return card;
    }

    // =========================================================================
    // 3. KARTU KHUSUS EKG (SPLIT KIRI TEKS BACAAN + KANAN THUMBNAIL & VIEWER)
    // =========================================================================
    private JPanel buatCardEKGSplit() {
        JPanel card = buatKerangkaCardSeragam();

        // Header EKG
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(HEADER_CARD);
        head.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_HALUS),
            new EmptyBorder(5, 10, 5, 10)
        ));
        JLabel lblTitle = new JLabel("EKG");
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblTitle.setForeground(TEKS_JUDUL);

        JPanel rightHead = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightHead.setOpaque(false);
        rightHead.add(badgeEKG);
        if (btnCekEkg != null) {
            styleMiniButton(btnCekEkg);
            rightHead.add(btnCekEkg);
        }

        head.add(lblTitle, BorderLayout.WEST);
        head.add(rightHead, BorderLayout.EAST);

        // Body Split (Kiri Teks Bacaan, Kanan Fixed Thumbnail Foto EKG)
        JPanel splitBody = new JPanel(new BorderLayout());
        splitBody.setBackground(Color.WHITE);

        // --- SISI KIRI: TEXTAREA + BARIS CHIP EKG ---
        JPanel leftSide = new JPanel(new BorderLayout());
        leftSide.setBackground(Color.WHITE);
        leftSide.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_HALUS));

        konfigurasiTextArea(txtEKG);
        txtEKG.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { perbaruiBadgeEKG(); }
            @Override public void removeUpdate(DocumentEvent e) { perbaruiBadgeEKG(); }
            @Override public void changedUpdate(DocumentEvent e) { perbaruiBadgeEKG(); }
        });

        // Shortcut Ctrl+V di dalam kotak EKG bisa langsung menempelkan gambar dari WA/Clipboard
        txtEKG.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), "pasteEKG");
        txtEKG.getActionMap().put("pasteEKG", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!cobaPasteGambarDariClipboard()) {
                    txtEKG.paste();
                }
            }
        });

        if (txtEKG.getParent() != null) {
            txtEKG.getParent().remove(txtEKG);
        }
        JScrollPane spEKG = new JScrollPane(txtEKG);
        spEKG.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        spEKG.setOpaque(false);
        spEKG.getViewport().setOpaque(false);
        spEKG.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        spEKG.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        if (spEKG.getVerticalScrollBar() != null) {
            spEKG.getVerticalScrollBar().setUnitIncrement(16);
            spEKG.getVerticalScrollBar().setBlockIncrement(64);
        }
        pasangMouseWheelBubble(spEKG);

        JPanel footerChipEKG = buatBarisChipContainer("EKG", panelChipEKG, cacheChipEKG, txtEKG);

        leftSide.add(spEKG, BorderLayout.CENTER);
        leftSide.add(footerChipEKG, BorderLayout.SOUTH);

        // --- SISI KANAN: THUMBNAIL PREVIEW & TOMBOL UPLOAD ---
        JPanel rightSide = new JPanel(new BorderLayout(0, 4));
        rightSide.setBackground(HEADER_CARD);
        rightSide.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel thumbBox = new JPanel(new BorderLayout());
        thumbBox.setBackground(Color.WHITE);
        thumbBox.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184), 1));
        thumbBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tampilkanPlaceholderKosongEKG();

        lblThumbImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gambarEKGAsli == null) {
                    pilihFileGambarEKG();
                } else {
                    bukaDialogZoomViewerEKG();
                }
            }
        });

        JPanel metaBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 1));
        metaBar.setOpaque(false);
        lblMetaUkuran.setFont(new Font("Tahoma", Font.BOLD, 9));
        lblMetaUkuran.setForeground(new Color(100, 116, 139));
        metaBar.add(lblMetaUkuran);

        thumbBox.add(lblThumbImage, BorderLayout.CENTER);
        thumbBox.add(metaBar, BorderLayout.SOUTH);

        // Tombol Bawah Thumbnail (Pilih File | Hapus) - Tanpa Icon & Tanpa Paste WA
        JPanel thumbBtns = new JPanel(new GridLayout(1, 2, 4, 0));
        thumbBtns.setOpaque(false);

        JButton btnPilihFile = new JButton("Pilih File");
        styleCleanMiniButton(btnPilihFile, TEKS_UTAMA);
        styleCleanMiniButton(btnHapusFotoEKG, new Color(185, 28, 28));
        btnHapusFotoEKG.setEnabled(false);

        btnPilihFile.addActionListener(e -> pilihFileGambarEKG());
        btnHapusFotoEKG.addActionListener(e -> hapusFotoEKG());

        thumbBtns.add(btnPilihFile);
        thumbBtns.add(btnHapusFotoEKG);

        rightSide.add(thumbBox, BorderLayout.CENTER);
        rightSide.add(thumbBtns, BorderLayout.SOUTH);

        // Fixed width 180px untuk sisi kanan foto EKG - tidak akan pernah mengecil
        rightSide.setPreferredSize(new Dimension(180, 0));
        rightSide.setMinimumSize(new Dimension(180, 0));

        splitBody.add(rightSide, BorderLayout.EAST);
        splitBody.add(leftSide, BorderLayout.CENTER);

        card.add(head, BorderLayout.NORTH);
        card.add(splitBody, BorderLayout.CENTER);

        perbaruiBadgeEKG();
        return card;
    }

    // =========================================================================
    // 4. LOGIKA UPLOAD, PASTE CLIPBOARD, KOMPRESI, & POP-UP ZOOM VIEWER EKG
    // =========================================================================
    private void tampilkanPlaceholderKosongEKG() {
        lblThumbImage.setIcon(new PenunjangVectorIcon(PenunjangVectorIcon.Type.IMAGE, 26, new Color(148, 163, 184)));
        lblThumbImage.setHorizontalTextPosition(SwingConstants.CENTER);
        lblThumbImage.setVerticalTextPosition(SwingConstants.BOTTOM);
        lblThumbImage.setIconTextGap(5);
        lblThumbImage.setText("<html><center><font color='#64748b' size='2'><b>Upload Foto EKG</b><br>atau tekan <b>Ctrl+V</b></font></center></html>");
        lblMetaUkuran.setText("");
    }

    private void pilihFileGambarEKG() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Pilih Foto / Berkas Rekaman EKG");
        fc.setFileFilter(new FileNameExtensionFilter("File Gambar (JPG, PNG, JPEG)", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fc.getSelectedFile();
                BufferedImage img = ImageIO.read(f);
                if (img != null) {
                    prosesDanPasangGambarEKG(img);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal membaca file gambar: " + ex.getMessage());
            }
        }
    }

    private boolean cobaPasteGambarDariClipboard() {
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image img = (Image) t.getTransferData(DataFlavor.imageFlavor);
                if (img != null) {
                    ImageIcon icon = new ImageIcon(img);
                    int w = icon.getIconWidth();
                    int h = icon.getIconHeight();
                    if (w > 0 && h > 0) {
                        BufferedImage bImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2 = bImg.createGraphics();
                        g2.drawImage(icon.getImage(), 0, 0, null);
                        g2.dispose();
                        prosesDanPasangGambarEKG(bImg);
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    // Kompresi otomatis resolusi gambar agar ringan disimpan ke server (~150 KB)
    private void prosesDanPasangGambarEKG(BufferedImage raw) {
        int maxW = 1600;
        int w = raw.getWidth();
        int h = raw.getHeight();
        if (w > maxW) {
            h = (int) (h * ((double) maxW / w));
            w = maxW;
        }
        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.drawImage(raw, 0, 0, w, h, null);
        g2.dispose();

        this.gambarEKGAsli = resized;

        // Buat Thumbnail Kecil untuk Kotak Kanan EKG
        Image thumb = resized.getScaledInstance(175, 105, Image.SCALE_SMOOTH);
        lblThumbImage.setText("");
        lblThumbImage.setIcon(new ImageIcon(thumb));
        lblThumbImage.setToolTipText("Klik untuk memperbesar & membaca detail grafik EKG");
        lblMetaUkuran.setText("🔍 Klik Perbesar (" + w + "x" + h + ")");
        btnHapusFotoEKG.setEnabled(true);
        perbaruiBadgeEKG();
    }

    public void hapusFotoEKG() {
        gambarEKGAsli = null;
        fileEKGTerpilih = null;
        tampilkanPlaceholderKosongEKG();
        btnHapusFotoEKG.setEnabled(false);
        perbaruiBadgeEKG();
    }

    /** Ambil objek gambar EKG yang sudah terkompresi */
    public BufferedImage getGambarEKGTerkompresi() {
        return gambarEKGAsli;
    }

    public String getGambarEKGBase64() {
        if (gambarEKGAsli == null) return "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(gambarEKGAsli, "jpg", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    public void setGambarEKGBase64(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            hapusFotoEKG();
            return;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(base64.trim());
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            BufferedImage img = ImageIO.read(bais);
            if (img != null) {
                prosesDanPasangGambarEKG(img);
            }
        } catch (Exception e) {
            hapusFotoEKG();
        }
    }

    // JENDELA POP-UP LIGHTBOX ZOOM, PAN (GESER), & ROTATE 90 DERAJAT
    private void bukaDialogZoomViewerEKG() {
        if (gambarEKGAsli == null) return;

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parentWin, "Viewer Lembar Rekaman EKG Pasien", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(1080, 680);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // State Zoom, Geser, & Putar
        final double[] scale = {1.0};
        final int[] rotasi = {0};
        final int[] panX = {0}, panY = {0};
        final Point[] dragStart = {null};

        JLabel lblZoomInfo = new JLabel("100%", SwingConstants.CENTER);
        lblZoomInfo.setForeground(new Color(128, 203, 196));
        lblZoomInfo.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblZoomInfo.setPreferredSize(new Dimension(55, 24));

        // Kanvas Gambar Interaktif
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2 + panX[0];
                int cy = getHeight() / 2 + panY[0];

                AffineTransform at = new AffineTransform();
                at.translate(cx, cy);
                at.scale(scale[0], scale[0]);
                at.rotate(Math.toRadians(rotasi[0]));
                at.translate(-gambarEKGAsli.getWidth() / 2.0, -gambarEKGAsli.getHeight() / 2.0);

                g2.drawImage(gambarEKGAsli, at, null);
                g2.dispose();
            }
        };
        canvas.setBackground(new Color(30, 41, 59));
        canvas.setCursor(new Cursor(Cursor.MOVE_CURSOR));

        // Scroll Mouse untuk Zoom In / Out
        canvas.addMouseWheelListener(e -> {
            double delta = (e.getWheelRotation() < 0) ? 0.2 : -0.2;
            scale[0] = Math.max(0.3, Math.min(5.0, scale[0] + delta));
            lblZoomInfo.setText(Math.round(scale[0] * 100) + "%");
            canvas.repaint();
        });

        // Tahan Klik Kiri untuk Geser Kertas EKG
        canvas.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart[0] != null) {
                    panX[0] += e.getX() - dragStart[0].x;
                    panY[0] += e.getY() - dragStart[0].y;
                    dragStart[0] = e.getPoint();
                    canvas.repaint();
                }
            }
        });

        // Header Toolbar Viewer
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(15, 23, 42));
        topBar.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel lblHint = new JLabel("Viewer EKG  |  Gunakan Scroll Mouse untuk Zoom In/Out • Tahan Klik Kiri untuk Geser Gambar");
        lblHint.setForeground(Color.WHITE);
        lblHint.setFont(new Font("Tahoma", Font.BOLD, 12));

        JPanel toolBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        toolBtns.setOpaque(false);

        JButton bZoomOut = new JButton("➖");
        JButton bZoomIn  = new JButton("➕");
        JButton bRotLeft = new JButton("↺ Putar Kiri");
        JButton bRotRight= new JButton("↻ Putar Kanan");
        JButton bReset   = new JButton("⟲ Reset");
        JButton bTutup   = new JButton("✕ Tutup");

        for (JButton b : new JButton[]{bZoomOut, bZoomIn, bRotLeft, bRotRight, bReset}) {
            b.setBackground(new Color(51, 65, 85));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setFont(new Font("Tahoma", Font.BOLD, 11));
            toolBtns.add(b);
            if (b == bZoomOut) toolBtns.add(lblZoomInfo);
        }
        bTutup.setBackground(new Color(185, 28, 28));
        bTutup.setForeground(Color.WHITE);
        bTutup.setFocusPainted(false);
        bTutup.setFont(new Font("Tahoma", Font.BOLD, 11));
        toolBtns.add(bTutup);

        bZoomOut.addActionListener(e -> { scale[0] = Math.max(0.3, scale[0] - 0.25); lblZoomInfo.setText(Math.round(scale[0]*100)+"%"); canvas.repaint(); });
        bZoomIn.addActionListener(e  -> { scale[0] = Math.min(5.0, scale[0] + 0.25); lblZoomInfo.setText(Math.round(scale[0]*100)+"%"); canvas.repaint(); });
        bRotLeft.addActionListener(e -> { rotasi[0] = (rotasi[0] - 90) % 360; canvas.repaint(); });
        bRotRight.addActionListener(e-> { rotasi[0] = (rotasi[0] + 90) % 360; canvas.repaint(); });
        bReset.addActionListener(e   -> { scale[0] = 1.0; rotasi[0] = 0; panX[0] = 0; panY[0] = 0; lblZoomInfo.setText("100%"); canvas.repaint(); });
        bTutup.addActionListener(e   -> dlg.dispose());

        topBar.add(lblHint, BorderLayout.WEST);
        topBar.add(toolBtns, BorderLayout.EAST);

        // Footer Viewer: Dokter bisa mengetik bacaan EKG sambil melihat grafik yang diperbesar
        JPanel bottomBar = new JPanel(new BorderLayout(12, 0));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, TEAL_RSUD),
            new EmptyBorder(10, 16, 10, 16)
        ));

        JTextArea txtBacaanViewer = new JTextArea(txtEKG.getText(), 3, 40);
        txtBacaanViewer.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtBacaanViewer.setLineWrap(true);
        txtBacaanViewer.setWrapStyleWord(true);

        txtBacaanViewer.getDocument().addDocumentListener(new DocumentListener() {
            private void sync() {
                SwingUtilities.invokeLater(() -> {
                    if (!txtEKG.getText().equals(txtBacaanViewer.getText())) {
                        txtEKG.setText(txtBacaanViewer.getText());
                    }
                });
            }
            @Override public void insertUpdate(DocumentEvent e) { sync(); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); }
        });

        JPanel leftFooter = new JPanel(new BorderLayout(0, 4));
        leftFooter.setOpaque(false);
        JLabel lblKetik = new JLabel("Hasil Bacaan / Interpretasi EKG (Ketik sambil melihat grafik di atas — Otomatis tersinkron ke form utama):");
        lblKetik.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblKetik.setForeground(TEAL_RSUD);
        leftFooter.add(lblKetik, BorderLayout.NORTH);
        leftFooter.add(new JScrollPane(txtBacaanViewer), BorderLayout.CENTER);

        JButton btnSelesai = new JButton("✓ Selesai Baca");
        stylePrimaryButton(btnSelesai);
        btnSelesai.addActionListener(e -> {
            txtEKG.setText(txtBacaanViewer.getText());
            dlg.dispose();
        });

        bottomBar.add(leftFooter, BorderLayout.CENTER);
        bottomBar.add(btnSelesai, BorderLayout.EAST);

        dlg.add(topBar, BorderLayout.NORTH);
        dlg.add(canvas, BorderLayout.CENTER);
        dlg.add(bottomBar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // 5. MANAJEMEN CHIP AKSI CEPAT (TAMBAH [+], EDIT, HAPUS & SIMPAN SERVER)
    // =========================================================================
    private void muatDataChipAwal() {
        if (!cacheSudahDimuat) {
            cacheChipEKG.clear();
            cacheChipLain.clear();

            boolean berhasilDariDB = false;
            if (koneksiServer != null) {
                try (PreparedStatement ps = koneksiServer.prepareStatement(
                        "SELECT id_chip, kategori, judul_chip, isi_teks FROM master_chip_penunjang " +
                        "WHERE kd_dokter IN ('-', ?) ORDER BY id_chip ASC")) {
                    ps.setString(1, kodeDokterAktif);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ChipItem item = new ChipItem(
                                rs.getInt("id_chip"),
                                rs.getString("kategori"),
                                rs.getString("judul_chip"),
                                rs.getString("isi_teks")
                            );
                            if ("EKG".equalsIgnoreCase(item.kategori)) cacheChipEKG.add(item);
                            else if ("LAINNYA".equalsIgnoreCase(item.kategori)) cacheChipLain.add(item);
                            berhasilDariDB = true;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Info: Menggunakan preset chip default (" + e.getMessage() + ")");
                }
            }

            // Jika tabel masih kosong / belum terhubung DB, isi dengan standar klinis
            if (!berhasilDariDB || (cacheChipEKG.isEmpty() && cacheChipLain.isEmpty())) {
                cacheChipEKG.add(new ChipItem(1, "EKG", "+ Sinus Normal", "[HASIL EKG]\n• Irama : Sinus Rhythm, HR 84 x/m, Axis Normal\n• Gelombang P normal, PR 0.16s, QRS 0.08s, ST-T changes (-)\n• Kesan : EKG Dalam Batas Normal"));
                cacheChipEKG.add(new ChipItem(2, "EKG", "+ STEMI", "[HASIL EKG - KRITIS]\n• Irama : Sinus Rhythm, HR 104 x/m\n• Tampak ST-Elevasi > 2 mm\n• Kesan : Acute STEMI"));
                cacheChipEKG.add(new ChipItem(3, "EKG", "+ AF", "[HASIL EKG]\n• Irama : Irregularly irregular, gelombang P absen\n• Kesan : Atrial Fibrillation"));

                cacheChipLain.add(new ChipItem(4, "LAINNYA", "+ GDA Stik", "• GDA Stik : ... mg/dL"));
                cacheChipLain.add(new ChipItem(5, "LAINNYA", "+ USG FAST", "• USG FAST : Free fluid (-), organ intraabdomen intak"));
                cacheChipLain.add(new ChipItem(6, "LAINNYA", "+ CTG", "• CTG : Kategori I, Reaktif, DJJ 140 x/m"));
            }
            cacheSudahDimuat = true;
        }
        renderBarisChip("EKG", panelChipEKG, cacheChipEKG, txtEKG);
        renderBarisChip("LAINNYA", panelChipLain, cacheChipLain, txtPemeriksaanLain);
    }

    private JPanel buatBarisChipContainer(String kategori, JPanel panelItems, List<ChipItem> listData, JTextArea areaTarget) {
        JPanel footer = new JPanel(new BorderLayout(4, 0));
        footer.setBackground(HEADER_CARD);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_HALUS),
            new EmptyBorder(2, 6, 2, 6)
        ));

        JLabel lblCepat = new JLabel("Cepat:");
        lblCepat.setFont(new Font("Tahoma", Font.PLAIN, 10));
        lblCepat.setForeground(new Color(100, 116, 139));
        footer.add(lblCepat, BorderLayout.WEST);

        panelItems.setOpaque(false);
        JScrollPane sp = new JScrollPane(panelItems);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        if (sp.getHorizontalScrollBar() != null) {
            sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 3));
        }
        sp.addMouseWheelListener(e -> {
            JScrollBar hBar = sp.getHorizontalScrollBar();
            if (hBar != null) {
                hBar.setValue(hBar.getValue() + (e.getWheelRotation() * 30));
            }
        });
        footer.add(sp, BorderLayout.CENTER);

        JButton btnPlus = new JButton("+");
        btnPlus.setFont(new Font("Tahoma", Font.BOLD, 10));
        btnPlus.setBackground(Color.WHITE);
        btnPlus.setForeground(TEKS_UTAMA);
        btnPlus.setFocusPainted(false);
        btnPlus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPlus.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CARD, 1),
            new EmptyBorder(2, 7, 2, 7)
        ));
        btnPlus.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnPlus.setBackground(new Color(241, 245, 249)); }
            @Override public void mouseExited(MouseEvent e) { btnPlus.setBackground(Color.WHITE); }
        });
        btnPlus.setToolTipText("Tambah tombol aksi cepat baru (Tersimpan di Server)");
        btnPlus.addActionListener(e -> dialogKelolaChip(kategori, listData, panelItems, areaTarget, -1, ""));
        footer.add(btnPlus, BorderLayout.EAST);

        return footer;
    }

    private void renderBarisChip(String kategori, JPanel panelTarget, List<ChipItem> listData, JTextArea areaTarget) {
        panelTarget.removeAll();

        for (int i = 0; i < listData.size(); i++) {
            final int idx = i;
            final ChipItem item = listData.get(i);

            String labelTampil = item.judul;
            if (labelTampil.length() > 16) {
                labelTampil = labelTampil.substring(0, 14) + "..";
            }

            JButton btnChip = new JButton(labelTampil);
            btnChip.setFont(new Font("Tahoma", Font.BOLD, 10));
            btnChip.setBackground(Color.WHITE);
            btnChip.setForeground(new Color(51, 65, 85));
            btnChip.setFocusPainted(false);
            btnChip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnChip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CARD),
                new EmptyBorder(2, 6, 2, 6)
            ));
            btnChip.setToolTipText("<html><b>" + item.judul + "</b><br>Klik Kiri: Sisipkan teks<br>Klik Kanan: Edit / Hapus<hr>" + item.isi.replace("\n", "<br>") + "</html>");

            // Klik Kiri: Sisipkan Teks
            btnChip.addActionListener(e -> {
                String cur = areaTarget.getText().trim();
                areaTarget.append((cur.isEmpty() ? "" : "\n") + item.isi);
                areaTarget.requestFocus();
            });

            // Klik Kanan: Edit atau Hapus Chip (Langsung Update ke Server & Cache RAM)
            JPopupMenu pop = new JPopupMenu();
            JMenuItem mnEdit = new JMenuItem("✏️ Ubah Tag '" + item.judul + "'");
            JMenuItem mnHapus = new JMenuItem("🗑️ Hapus Tag Ini");

            mnEdit.addActionListener(e -> dialogKelolaChip(kategori, listData, panelTarget, areaTarget, idx, ""));
            mnHapus.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(this, "Hapus tombol cepat '" + item.judul + "'?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    hapusChipDiServer(item.id);
                    listData.remove(idx);
                    renderBarisChip(kategori, panelTarget, listData, areaTarget);
                }
            });
            pop.add(mnEdit);
            pop.add(mnHapus);
            btnChip.setComponentPopupMenu(pop);

            panelTarget.add(btnChip);
        }

        panelTarget.revalidate();
        panelTarget.repaint();
    }

    private void dialogKelolaChip(String kategori, List<ChipItem> listData, JPanel panelTarget, JTextArea areaTarget, int editIdx, String teksBlokAwal) {
        boolean modeEdit = (editIdx >= 0);
        JTextField inpJudul = new JTextField(modeEdit ? listData.get(editIdx).judul : "+ ");
        JTextArea inpIsi = new JTextArea(4, 30);
        inpIsi.setLineWrap(true);
        inpIsi.setWrapStyleWord(true);

        if (modeEdit) {
            inpIsi.setText(listData.get(editIdx).isi);
        } else if (teksBlokAwal != null && !teksBlokAwal.isEmpty()) {
            inpIsi.setText(teksBlokAwal);
        }

        JPanel p = new JPanel(new BorderLayout(0, 8));
        JPanel pTop = new JPanel(new BorderLayout(0, 4));
        pTop.add(new JLabel("Nama Singkat Tombol (Contoh: + RBBB / + Echo) :"), BorderLayout.NORTH);
        pTop.add(inpJudul, BorderLayout.CENTER);

        JPanel pMid = new JPanel(new BorderLayout(0, 4));
        pMid.add(new JLabel("Isi Teks yang Disisipkan :"), BorderLayout.NORTH);
        pMid.add(new JScrollPane(inpIsi), BorderLayout.CENTER);

        p.add(pTop, BorderLayout.NORTH);
        p.add(pMid, BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(this, p,
                (modeEdit ? "Ubah Tombol Cepat (" : "Tambah Tombol Cepat Baru (") + kategori + ")",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String j = inpJudul.getText().trim();
            String isi = inpIsi.getText().trim();
            if (j.isEmpty() || isi.isEmpty()) return;
            if (!j.startsWith("+")) j = "+ " + j;

            if (modeEdit) {
                ChipItem lama = listData.get(editIdx);
                lama.judul = j;
                lama.isi = isi;
                ubahChipDiServer(lama);
            } else {
                int idBaru = simpanChipKeServer(kategori, j, isi);
                listData.add(new ChipItem(idBaru, kategori, j, isi));
            }
            renderBarisChip(kategori, panelTarget, listData, areaTarget);
        }
    }

    private void pasangMenuBlokTeksKeChip(JTextArea area, String kategori) {
        JPopupMenu popArea = new JPopupMenu();
        JMenuItem mnSimpanBlok = new JMenuItem("➕ Simpan Teks yang Diblok Jadi Tombol Cepat (Chip)");
        mnSimpanBlok.addActionListener(e -> {
            String sel = area.getSelectedText();
            if (sel == null || sel.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Blok / sorot dulu kalimat di dalam kotak teks yang ingin dijadikan tombol cepat!");
                return;
            }
            if ("EKG".equals(kategori)) {
                dialogKelolaChip("EKG", cacheChipEKG, panelChipEKG, txtEKG, -1, sel.trim());
            } else {
                dialogKelolaChip("LAINNYA", cacheChipLain, panelChipLain, txtPemeriksaanLain, -1, sel.trim());
            }
        });
        popArea.add(mnSimpanBlok);
        area.setComponentPopupMenu(popArea);
    }

    private int simpanChipKeServer(String kat, String judul, String isi) {
        if (koneksiServer == null) return (int) (System.currentTimeMillis() % 100000);
        try (PreparedStatement ps = koneksiServer.prepareStatement(
                "INSERT INTO master_chip_penunjang (kategori, kd_dokter, judul_chip, isi_teks) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kat);
            ps.setString(2, kodeDokterAktif);
            ps.setString(3, judul);
            ps.setString(4, isi);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Gagal simpan chip ke server: " + e.getMessage());
        }
        return -1;
    }

    private void ubahChipDiServer(ChipItem item) {
        if (koneksiServer == null || item.id <= 0) return;
        try (PreparedStatement ps = koneksiServer.prepareStatement(
                "UPDATE master_chip_penunjang SET judul_chip=?, isi_teks=? WHERE id_chip=?")) {
            ps.setString(1, item.judul);
            ps.setString(2, item.isi);
            ps.setInt(3, item.id);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Gagal update chip di server: " + e.getMessage());
        }
    }

    private void hapusChipDiServer(int idChip) {
        if (koneksiServer == null || idChip <= 0) return;
        try (PreparedStatement ps = koneksiServer.prepareStatement(
                "DELETE FROM master_chip_penunjang WHERE id_chip=?")) {
            ps.setInt(1, idChip);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Gagal hapus chip di server: " + e.getMessage());
        }
    }

    // =========================================================================
    // 6. HELPER STYLING FORMAL & BADGE STATUS KALEM
    // =========================================================================
    private JPanel buatKerangkaCardSeragam() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CARD, 1),
            BorderFactory.createMatteBorder(0, 4, 0, 0, TEAL_RSUD)
        ));
        return card;
    }

    public void perbaruiSemuaBadge() {
        perbaruiBadgeTeks(txtLaborat, badgeLab, "Laborat");
        perbaruiBadgeTeks(txtRadiologi, badgeRad, "Radiologi");
        perbaruiBadgeTeks(txtPemeriksaanLain, badgeLain, "Pemeriksaan Lainnya");
        perbaruiBadgeEKG();
    }

    public void resetForm() {
        txtLaborat.setText("");
        txtRadiologi.setText("");
        txtEKG.setText("");
        txtPemeriksaanLain.setText("");
        hapusFotoEKG();
        perbaruiSemuaBadge();
    }

    private void perbaruiBadgeTeks(JTextArea area, JLabel badge, String judul) {
        String t = area.getText().trim().toUpperCase();
        if (t.isEmpty()) {
            setGayaBadge(badge, "EMPTY", judul.contains("Lain") ? "Opsional" : "Belum Ada Data");
        } else if (t.contains("DALAM PROSES") || t.contains("MENUNGGU")) {
            setGayaBadge(badge, "PENDING", "Dalam Proses Alat");
        } else {
            setGayaBadge(badge, "READY", "Hasil Pemeriksaan Tersedia");
        }
    }

    private void perbaruiBadgeEKG() {
        boolean adaTeks = !txtEKG.getText().trim().isEmpty();
        boolean adaFoto = (gambarEKGAsli != null);
        if (!adaFoto && !adaTeks) {
            setGayaBadge(badgeEKG, "EMPTY", "Belum Ada Data");
        } else if (adaFoto && !adaTeks) {
            setGayaBadge(badgeEKG, "ATTACHED", "Foto EKG Terlampir • Menunggu Bacaan");
        } else if (!adaFoto && adaTeks) {
            setGayaBadge(badgeEKG, "READY", "Hasil Bacaan Tersedia");
        } else {
            setGayaBadge(badgeEKG, "READY", "Lengkap (Bacaan & Foto EKG)");
        }
    }

    private void setGayaBadge(JLabel badge, String tipe, String teks) {
        badge.setFont(new Font("Tahoma", Font.BOLD, 10));
        badge.setOpaque(true);
        badge.setText(" ● " + teks + " ");
        switch (tipe) {
            case "PENDING":
                badge.setBackground(new Color(254, 243, 199));
                badge.setForeground(new Color(180, 83, 9));
                badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(253, 230, 138)),
                    new EmptyBorder(1, 5, 1, 5)
                ));
                break;
            case "ATTACHED":
                badge.setBackground(new Color(240, 249, 255));
                badge.setForeground(new Color(7, 89, 133));
                badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(186, 230, 253)),
                    new EmptyBorder(1, 5, 1, 5)
                ));
                break;
            case "READY":
                badge.setBackground(new Color(240, 253, 244));
                badge.setForeground(new Color(22, 101, 52));
                badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(187, 247, 208)),
                    new EmptyBorder(1, 5, 1, 5)
                ));
                break;
            default:
                badge.setBackground(new Color(241, 245, 249));
                badge.setForeground(new Color(100, 116, 139));
                badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_CARD),
                    new EmptyBorder(1, 5, 1, 5)
                ));
                break;
        }
    }

    private void konfigurasiTextArea(JTextArea a) {
        a.setPreferredSize(null);
        a.setMinimumSize(null);
        a.setMaximumSize(null);
        a.setFont(new Font("Consolas", Font.PLAIN, 11));
        a.setForeground(TEKS_UTAMA);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setMargin(new Insets(6, 8, 6, 8));
        a.setOpaque(true);
        a.setBackground(Color.WHITE);
        a.addCaretListener(e -> {
            try {
                java.awt.Rectangle r = a.modelToView(a.getCaretPosition());
                if (r != null) {
                    a.scrollRectToVisible(r);
                }
            } catch (Exception ignored) {}
        });
    }

    private void pasangMouseWheelBubble(JScrollPane sp) {
        sp.addMouseWheelListener(e -> {
            JScrollBar bar = sp.getVerticalScrollBar();
            if (bar == null || !bar.isVisible()) {
                Container parent = sp.getParent();
                while (parent != null && !(parent instanceof JScrollPane)) {
                    parent = parent.getParent();
                }
                if (parent != null) {
                    parent.dispatchEvent(SwingUtilities.convertMouseEvent(sp, e, parent));
                }
            } else {
                int val = bar.getValue();
                int min = bar.getMinimum();
                int max = bar.getMaximum() - bar.getModel().getExtent();
                int rot = e.getWheelRotation();
                if ((rot < 0 && val <= min) || (rot > 0 && val >= max)) {
                    Container parent = sp.getParent();
                    while (parent != null && !(parent instanceof JScrollPane)) {
                        parent = parent.getParent();
                    }
                    if (parent != null) {
                        parent.dispatchEvent(SwingUtilities.convertMouseEvent(sp, e, parent));
                    }
                }
            }
        });
    }

    private void stylePrimaryButton(JButton b) {
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setRolloverEnabled(true);
        b.setForeground(TEAL_RSUD);
        b.setFont(new Font(widget.ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        b.setIcon(new PenunjangVectorIcon(PenunjangVectorIcon.Type.SYNC, 14, TEAL_RSUD));
        b.setIconTextGap(8);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        b.setPreferredSize(new Dimension(310, 32));
    }

    private void styleMiniButton(JButton b) {
        b.setFont(new Font("Tahoma", Font.BOLD, 10));
        b.setBackground(Color.WHITE);
        b.setForeground(TEKS_UTAMA);
        b.setIcon(new PenunjangVectorIcon(PenunjangVectorIcon.Type.REFRESH, 11, TEKS_UTAMA));
        b.setIconTextGap(5);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CARD),
            new EmptyBorder(3, 7, 3, 7)
        ));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(new Color(241, 245, 249)); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(Color.WHITE); }
        });
    }

    private void styleCleanMiniButton(JButton b, Color fg) {
        b.setFont(new Font("Tahoma", Font.BOLD, 10));
        b.setBackground(Color.WHITE);
        b.setForeground(fg);
        b.setIcon(null);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CARD),
            new EmptyBorder(3, 7, 3, 7)
        ));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(new Color(241, 245, 249)); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(Color.WHITE); }
        });
    }

    // =========================================================================
    // 7. VEKTOR ICON INTERNAL (ANTI-TOFU / TIDAK KOTAK PADA WINDOWS SWING)
    // =========================================================================
    public static class PenunjangVectorIcon implements Icon {
        public enum Type { REFRESH, SYNC, IMAGE, DOT, PLUS }
        private final Type type;
        private final int size;
        private final Color color;

        public PenunjangVectorIcon(Type type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(color != null ? color : c.getForeground());

            float s = size;
            switch (type) {
                case REFRESH:
                case SYNC:
                    g2.setStroke(new BasicStroke(Math.max(1.4f, s / 7.5f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Arc atas
                    g2.draw(new Arc2D.Float(s * 0.12f, s * 0.12f, s * 0.76f, s * 0.76f, 35, 195, Arc2D.OPEN));
                    // Panah atas
                    Path2D a1 = new Path2D.Float();
                    a1.moveTo(s * 0.52f, s * 0.02f);
                    a1.lineTo(s * 0.74f, s * 0.16f);
                    a1.lineTo(s * 0.54f, s * 0.32f);
                    g2.draw(a1);

                    // Arc bawah
                    g2.draw(new Arc2D.Float(s * 0.12f, s * 0.12f, s * 0.76f, s * 0.76f, 215, 195, Arc2D.OPEN));
                    // Panah bawah
                    Path2D a2 = new Path2D.Float();
                    a2.moveTo(s * 0.48f, s * 0.98f);
                    a2.lineTo(s * 0.26f, s * 0.84f);
                    a2.lineTo(s * 0.46f, s * 0.68f);
                    g2.draw(a2);
                    break;

                case IMAGE:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new RoundRectangle2D.Float(s * 0.10f, s * 0.12f, s * 0.80f, s * 0.76f, 4, 4));
                    g2.fill(new Ellipse2D.Float(s * 0.26f, s * 0.28f, s * 0.18f, s * 0.18f));
                    Path2D m = new Path2D.Float();
                    m.moveTo(s * 0.12f, s * 0.76f);
                    m.lineTo(s * 0.38f, s * 0.50f);
                    m.lineTo(s * 0.55f, s * 0.65f);
                    m.lineTo(s * 0.70f, s * 0.46f);
                    m.lineTo(s * 0.88f, s * 0.76f);
                    g2.draw(m);
                    break;

                case DOT:
                    g2.fill(new Ellipse2D.Float(s * 0.2f, s * 0.2f, s * 0.6f, s * 0.6f));
                    break;

                case PLUS:
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Line2D.Float(s * 0.5f, s * 0.2f, s * 0.5f, s * 0.8f));
                    g2.draw(new Line2D.Float(s * 0.2f, s * 0.5f, s * 0.8f, s * 0.5f));
                    break;
            }
            g2.dispose();
        }
    }
}
