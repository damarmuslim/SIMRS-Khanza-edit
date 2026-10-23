package rekammedis;

import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import kepegawaian.DlgCariPetugas;
import widget.IconStyle;
import widget.Tanggal;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * FORMULIR REKONSILIASI OBAT TERPADU (RM 03-06A) - RSUD DEMANG SEPULAU RAYA
 * Mengikuti:
 * 1. UI Modern Teal (#134e4a / #0f766e) & TabbedPane (Tab 1: Input, Tab 2: Data & Validasi Apoteker).
 * 2. Sidebar Kiri: Identitas Pasien (Terkunci) + Panel Contekan Asesmen Awal Medis (Ranap/IGD/Ralan).
 * 3. Inline Editable Table Obat + Dialog Master Obat/Infus (inventory.DlgBarang).
 * 4. Konfirmasi / Validasi Apoteker Terpadu di Tab 2 (tabel rekonsiliasi_obat_konfirmasi).
 * 5. Kepatuhan Posisional Database: 11 kolom rekonsiliasi_obat, 8 kolom detail_obat, 5 kolom konfirmasi.
 * 6. Kompatibilitas Caller SIMRS Khanza: isCek(), setNoRm(String), emptTeks().
 */
public final class RMRekonsiliasiObat extends javax.swing.JDialog {

    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps, psDetail;
    private ResultSet rs, rsDetail;

    // Dialog Bawaan Khanza
    private final DlgCariPetugas petugas = new DlgCariPetugas(null, false);

    // Warna Tema Clean Medical UI (Modern Teal, Mint & Slate)
    private final Color TEAL_DARK    = new Color(19, 78, 74);       // #134e4a
    private final Color TEAL_MAIN    = new Color(15, 118, 110);     // #0f766e
    private final Color TEAL_LIGHT   = new Color(240, 253, 250);   // #f0fdfa (Soft Ice Mint)
    private final Color MINT_BORDER  = new Color(94, 234, 212);     // #5eead4 (Teal Lembut)
    private final Color BLUE_OCEAN   = new Color(3, 105, 161);      // #0369a1 (Biru Medis / Ocean Blue)
    private final Color GREEN_EMERALD= new Color(5, 150, 105);      // #059669 (Hijau Emerald)
    private final Color SLATE_BG     = new Color(241, 245, 249);   // #f1f5f9
    private final Color SLATE_BORDER = new Color(203, 213, 225);   // #cbd5e1

    // Navigasi Utama
    private JTabbedPane tabPaneUtama;

    // Sidebar Kiri (Identitas Pasien & Asesmen Medis / Resep Obat Ranap)
    private JLabel lblAvatarInitials, lblPasienNama, lblPasienJk;
    private JTextField TNoRw, TNoRM, TTglLahir, TUmurStatus;
    private JPanel cardSidebar;
    private TitledBorder borderCardSidebar;
    private JLabel lblSubJudul, lblRtl, lblRpo;
    private JPanel pnlToggle;
    private JTextArea txtContekanTataLaksana, txtContekanRPO;
    private JScrollPane spTataLaksana, spRPO;
    private JPanel panelQuickChips, panelRPOContekan;
    // Riwayat Alergi Input Fields (Tersinkronisasi & Dicetak)
    private JTextField txtAlergiObat, txtManifestasiAlergi;
    private JComboBox<String> cmbDampakAlergi;

    // Tombol Aksi Tambahan (Pilih Pasien, Batch Salin Obat, Tarik Rekon Sebelumnya, Cetak Rekap)
    private JButton btnPilihPasien;
    private JButton btnSalinSemuaObat;
    private JButton btnSalinRekonSebelumnya;
    private JButton btnCetakRekap;

    private JButton btnToggleIGD, btnToggleRanap;
    private String tataRanap = "", rpoRanap = "", alergiRanap = "";
    private String tataIGD = "", rpoIGD = "", alergiIGD = "";
    private String sumberTerpilih = "RANAP";
    private String resepRanapTervalidasi = "";
    private final java.util.List<Map<String, String>> listObatResepTervalidasi = new ArrayList<>();

    // Header Input Rekonsiliasi (Tab 1)
    private JTextField KdPetugas, NmPetugas, NoRekonsiliasi, KetSumberLain;
    private Tanggal DTPTgl;
    private JRadioButton rbAdmisi, rbTransfer, rbPulang;
    private JRadioButton rbPakaiYa, rbPakaiTidak;
    private JRadioButton rbSumberRM, rbSumberPasien, rbSumberLain;
    private JPanel rowHeader2, pnlSaat, pnlPakaiObat, pnlSumber;

    // State Form Tab 1 (Input Rekonsiliasi Obat)
    public enum ModeInput { MODE_INPUT_BARU, MODE_EDIT }
    private ModeInput currentModeInput = ModeInput.MODE_INPUT_BARU;
    private String noRekonSedangDiedit = "";

    // Inline Table Obat Direkonsiliasi (Tab 1)
    private JTable tbObat;
    private DefaultTableModel tabModeObat;
    private JButton btnTambahInline, btnPilihMasterObat, btnHapusBarisObat;
    private JLabel lblBadgeJmlObat;

    // Autocomplete Popup Live Search Obat (Cara 1)
    private JPopupMenu popupSaranObat;
    private DefaultListModel<String> modelSaranObat;
    private JList<String> listSaranObat;
    private final Map<String, String> mapKodeObat = new HashMap<>();
    private JTextField txtNamaObatEditor;
    private int rowObatSedangDiedit = -1;
    private long lastPilihObatTime = 0;

    // UX Maximize/Restore
    private boolean isMaximized = false;
    private Rectangle normalBounds = null;
    private JButton btnMaximize;

    // Komponen Tab 2 (Data Rekonsiliasi & Validasi Apoteker)
    private JPanel containerTabData, panelKiriData, panelKananData;
    private JButton btnPopupPreview;
    private JTable tbDataRekon;
    private DefaultTableModel tabModeDataRekon;
    private JEditorPane LoadHTML;
    private JScrollPane scrollPreviewHTML;
    private JTextField TCari;
    private Tanggal DTPCari1, DTPCari2;
    private JLabel LCount;

    // Tombol Footer Tab 1 & Tab 2
    private JPanel panelFooterInput, panelFooterData;
    private JButton btnSimpan, btnBaru;
    private JCheckBox chkLangsungTTE;
    private JButton btnValidasiTTE, btnGantiData, btnHapusRekon, btnCetak;

    public RMRekonsiliasiObat(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Formulir Rekonsiliasi Obat (RM 03-06A) - RSUD Demang Sepulau Raya ]::");
        Rectangle maxScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int targetW = Math.min(1420, (int) (maxScreen.width * 0.95));
        int targetH = Math.min(860, (int) (maxScreen.height * 0.95));
        setSize(targetW, targetH);
        setMinimumSize(new Dimension(1100, 640));
        setLocationRelativeTo(null);
        initComponentsCustom();
        initListeners();
    }

    private void initComponentsCustom() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(SLATE_BG);

        // =====================================================================
        // 1. TOP HEADER BAR TEAL (#134e4a)
        // =====================================================================
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(TEAL_DARK);
        headerBar.setBorder(new EmptyBorder(8, 20, 8, 20));

        try {
            koneksi.createStatement().executeUpdate("ALTER TABLE rekonsiliasi_obat_detail_obat MODIFY COLUMN tindak_lanjut ENUM('Lanjut','Stop','Lanjut aturan pakai sama','Lanjut aturan pakai berubah','Baru') DEFAULT NULL");
        } catch (Exception ignored) {
        }

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLeft.setOpaque(false);

        JLabel lblTitle = new JLabel("Rekonsiliasi Obat");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblKodeRM = new JLabel(" RM 03-06A ");
        lblKodeRM.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblKodeRM.setForeground(new Color(204, 251, 241));
        lblKodeRM.setBackground(TEAL_MAIN);
        lblKodeRM.setOpaque(true);
        lblKodeRM.setBorder(new CompoundBorder(
            new LineBorder(new Color(13, 148, 136), 1, true),
            new EmptyBorder(2, 6, 2, 6)
        ));

        headerLeft.add(lblTitle);
        headerLeft.add(lblKodeRM);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        JLabel lblRS = new JLabel("RSUD Demang Sepulau Raya");
        lblRS.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRS.setForeground(new Color(153, 246, 228));

        btnMaximize = new JButton();
        btnMaximize.setIcon(createMaximizeIcon(false));
        btnMaximize.setBackground(TEAL_MAIN);
        btnMaximize.setBorder(new LineBorder(new Color(13, 148, 136), 1, true));
        btnMaximize.setPreferredSize(new Dimension(32, 24));
        btnMaximize.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMaximize.setToolTipText("Perbesar / Pulihkan Layar Penuh (Double click header juga bisa)");
        btnMaximize.setFocusPainted(false);
        btnMaximize.addActionListener(e -> toggleMaximize());

        headerRight.add(lblRS);
        headerRight.add(btnMaximize);

        headerBar.add(headerLeft, BorderLayout.WEST);
        headerBar.add(headerRight, BorderLayout.EAST);
        headerBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleMaximize();
                }
            }
        });

        getContentPane().add(headerBar, BorderLayout.NORTH);

        // =====================================================================
        // 2. MAIN TABBED PANE (TAB 1: INPUT, TAB 2: DATA & VALIDASI APOTEKER)
        // =====================================================================
        tabPaneUtama = new JTabbedPane();
        tabPaneUtama.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabPaneUtama.setBackground(Color.WHITE);
        tabPaneUtama.addTab("  Input Rekonsiliasi Obat  ", null, buildTabInput());
        tabPaneUtama.addTab("  Data Rekonsiliasi & Validasi Apoteker  ", null, buildTabData());

        getContentPane().add(tabPaneUtama, BorderLayout.CENTER);

        // =====================================================================
        // 3. BOTTOM STICKY FOOTER BAR
        // =====================================================================
        getContentPane().add(buildFooterBar(), BorderLayout.SOUTH);
    }

    private JPanel buildTabInput() {
        JPanel container = new JPanel(new BorderLayout(12, 12));
        container.setBackground(SLATE_BG);
        container.setBorder(new EmptyBorder(12, 16, 12, 16));

        // ---------------------------------------------------------------------
        // SIDEBAR KIRI: IDENTITAS PASIEN + PANEL CONTEKAN ASESMEN AWAL MEDIS
        // ---------------------------------------------------------------------
        JPanel sidebarKiri = new JPanel();
        sidebarKiri.setLayout(new BoxLayout(sidebarKiri, BoxLayout.Y_AXIS));
        sidebarKiri.setOpaque(false);
        sidebarKiri.setPreferredSize(new Dimension(350, 600));

        // Card 1: Identitas Pasien (TERKUNCI)
        JPanel cardIdentitas = createCardPanel("IDENTITAS PASIEN", Color.WHITE, SLATE_BORDER);
        cardIdentitas.setLayout(new BorderLayout(8, 8));

        // Avatar Initial + Nama & JK
        JPanel pnlAvatarNama = new JPanel(new BorderLayout(10, 0));
        pnlAvatarNama.setOpaque(false);

        lblAvatarInitials = new JLabel("PS", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL_MAIN);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAvatarInitials.setPreferredSize(new Dimension(42, 42));
        lblAvatarInitials.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvatarInitials.setForeground(Color.WHITE);

        JPanel pnlNamaDetail = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlNamaDetail.setOpaque(false);
        lblPasienNama = new JLabel("Nama Pasien");
        lblPasienNama.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPasienNama.setForeground(new Color(30, 41, 59));

        lblPasienJk = new JLabel("-");
        lblPasienJk.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPasienJk.setForeground(new Color(100, 116, 139));

        pnlNamaDetail.add(lblPasienNama);
        pnlNamaDetail.add(lblPasienJk);

        pnlAvatarNama.add(lblAvatarInitials, BorderLayout.WEST);
        pnlAvatarNama.add(pnlNamaDetail, BorderLayout.CENTER);

        JPanel pnlBadgePasien = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlBadgePasien.setOpaque(false);

        btnPilihPasien = new JButton("Pilih Pasien");
        btnPilihPasien.setIcon(IconStyle.muat("search.svg", 11, TEAL_MAIN));
        btnPilihPasien.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnPilihPasien.setBackground(Color.WHITE);
        btnPilihPasien.setForeground(TEAL_DARK);
        btnPilihPasien.setBorder(new CompoundBorder(new LineBorder(TEAL_MAIN, 1, true), new EmptyBorder(2, 6, 2, 6)));
        btnPilihPasien.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPilihPasien.setToolTipText("Cari dan pilih registrasi pasien dari database");
        btnPilihPasien.addActionListener(e -> bukaDialogCariRegPasien());

        JLabel lblBadgeTerkunci = new JLabel(" TERKUNCI ");
        lblBadgeTerkunci.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblBadgeTerkunci.setForeground(new Color(71, 85, 105));
        lblBadgeTerkunci.setBackground(new Color(241, 245, 249));
        lblBadgeTerkunci.setOpaque(true);
        lblBadgeTerkunci.setBorder(new LineBorder(new Color(203, 213, 225), 1, true));

        pnlBadgePasien.add(btnPilihPasien);
        pnlBadgePasien.add(lblBadgeTerkunci);
        pnlAvatarNama.add(pnlBadgePasien, BorderLayout.EAST);

        // Detail Baris Identitas (Compact 2x2 Grid)
        JPanel pnlIdRows = new JPanel(new GridLayout(2, 2, 4, 3));
        pnlIdRows.setOpaque(false);
        pnlIdRows.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, new Color(241, 245, 249)), new EmptyBorder(4, 0, 0, 0)));

        TNoRM = createReadOnlyField("-");
        TNoRw = createReadOnlyField("-");
        TTglLahir = createReadOnlyField("-");
        TUmurStatus = createReadOnlyField("-");

        pnlIdRows.add(createLabeledRowCompact("No. RM:", TNoRM));
        pnlIdRows.add(createLabeledRowCompact("No. Rawat:", TNoRw));
        pnlIdRows.add(createLabeledRowCompact("Lahir:", TTglLahir));
        pnlIdRows.add(createLabeledRowCompact("Umur:", TUmurStatus));

        cardIdentitas.add(pnlAvatarNama, BorderLayout.NORTH);
        cardIdentitas.add(pnlIdRows, BorderLayout.CENTER);
        cardIdentitas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        // Card 2: Panel Asesmen Medis / Resep Obat Ranap (Bebas Notepad!)
        borderCardSidebar = BorderFactory.createTitledBorder(
            new LineBorder(MINT_BORDER, 1, false), " ASESMEN MEDIS (RANAP / IGD) ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11), TEAL_DARK
        );
        cardSidebar = new JPanel();
        cardSidebar.setBackground(TEAL_LIGHT);
        cardSidebar.setBorder(new CompoundBorder(borderCardSidebar, new EmptyBorder(6, 8, 6, 8)));
        cardSidebar.setLayout(new BorderLayout(6, 6));

        JPanel pnlContekanHeader = new JPanel(new BorderLayout(4, 0));
        pnlContekanHeader.setOpaque(false);
        pnlContekanHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlContekanHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        lblSubJudul = new JLabel("Sumber Asesmen:");
        lblSubJudul.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSubJudul.setForeground(TEAL_DARK);

        pnlToggle = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlToggle.setOpaque(false);

        btnToggleIGD = new JButton("IGD");
        btnToggleRanap = new JButton("Ranap");
        styleToggleBtn(btnToggleIGD, false);
        styleToggleBtn(btnToggleRanap, true);
        pasangHoverToggleBtn(btnToggleIGD, "IGD");
        pasangHoverToggleBtn(btnToggleRanap, "RANAP");

        btnToggleIGD.addActionListener(e -> tampilkanContekanSumber("IGD"));
        btnToggleRanap.addActionListener(e -> tampilkanContekanSumber("RANAP"));

        pnlToggle.add(btnToggleIGD);
        pnlToggle.add(btnToggleRanap);

        pnlContekanHeader.add(lblSubJudul, BorderLayout.WEST);
        pnlContekanHeader.add(pnlToggle, BorderLayout.EAST);

        JPanel contekanBody = new JPanel();
        contekanBody.setLayout(new BoxLayout(contekanBody, BoxLayout.Y_AXIS));
        contekanBody.setOpaque(false);

        lblRtl = new JLabel("1. Tata Laksana / Terapi Dokter:");
        lblRtl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRtl.setForeground(TEAL_DARK);
        lblRtl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblRtl.setHorizontalAlignment(SwingConstants.LEFT);
        lblRtl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        txtContekanTataLaksana = new JTextArea(5, 20);
        txtContekanTataLaksana.setEditable(false);
        txtContekanTataLaksana.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtContekanTataLaksana.setLineWrap(true);
        txtContekanTataLaksana.setWrapStyleWord(true);
        txtContekanTataLaksana.setBackground(Color.WHITE);
        txtContekanTataLaksana.setMargin(new Insets(5, 6, 5, 6));

        spTataLaksana = new JScrollPane(txtContekanTataLaksana);
        spTataLaksana.setAlignmentX(Component.LEFT_ALIGNMENT);
        spTataLaksana.setPreferredSize(new Dimension(330, 115));
        spTataLaksana.setMinimumSize(new Dimension(330, 95));

        JPanel pnlActionContekan = new JPanel(new GridLayout(1, 2, 6, 0));
        pnlActionContekan.setOpaque(false);
        pnlActionContekan.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlActionContekan.setPreferredSize(new Dimension(330, 26));
        pnlActionContekan.setMinimumSize(new Dimension(330, 26));
        pnlActionContekan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JButton btnKirimBlokKeTabel = new JButton(" Teks Diblok");
        btnKirimBlokKeTabel.setIcon(IconStyle.muat("plus.svg", 12, Color.WHITE));
        btnKirimBlokKeTabel.setBackground(TEAL_MAIN);
        btnKirimBlokKeTabel.setForeground(Color.WHITE);
        btnKirimBlokKeTabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnKirimBlokKeTabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKirimBlokKeTabel.setToolTipText("Masukkan bagian teks yang diblok pada kotak Tata Laksana ke tabel obat");
        btnKirimBlokKeTabel.addActionListener(e -> kirimTeksTerblokKeTabel());

        btnSalinSemuaObat = new JButton(" Salin Semua Obat");
        btnSalinSemuaObat.setIcon(IconStyle.muat("arrow-right-left.svg", 12, Color.WHITE));
        btnSalinSemuaObat.setBackground(BLUE_OCEAN);
        btnSalinSemuaObat.setForeground(Color.WHITE);
        btnSalinSemuaObat.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnSalinSemuaObat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalinSemuaObat.setToolTipText("Salin seluruh obat dari catatan terapi dokter / resep ranap sekaligus ke tabel rekonsiliasi");
        btnSalinSemuaObat.addActionListener(e -> salinSemuaObatKeTabel());

        pnlActionContekan.add(btnKirimBlokKeTabel);
        pnlActionContekan.add(btnSalinSemuaObat);

        panelQuickChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        panelQuickChips.setOpaque(false);
        panelQuickChips.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelQuickChips.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        lblRpo = new JLabel("2. Riwayat Penggunaan Obat Rumah (RPO):");
        lblRpo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRpo.setForeground(TEAL_DARK);
        lblRpo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblRpo.setHorizontalAlignment(SwingConstants.LEFT);
        lblRpo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        txtContekanRPO = new JTextArea(3, 20);
        txtContekanRPO.setEditable(false);
        txtContekanRPO.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtContekanRPO.setLineWrap(true);
        txtContekanRPO.setWrapStyleWord(true);
        txtContekanRPO.setBackground(Color.WHITE);
        txtContekanRPO.setMargin(new Insets(5, 6, 5, 6));

        spRPO = new JScrollPane(txtContekanRPO);
        spRPO.setAlignmentX(Component.LEFT_ALIGNMENT);
        spRPO.setPreferredSize(new Dimension(330, 65));
        spRPO.setMinimumSize(new Dimension(330, 52));

        panelRPOContekan = new JPanel();
        panelRPOContekan.setLayout(new BoxLayout(panelRPOContekan, BoxLayout.Y_AXIS));
        panelRPOContekan.setOpaque(false);
        panelRPOContekan.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelRPOContekan.add(Box.createVerticalStrut(5));
        panelRPOContekan.add(lblRpo);
        panelRPOContekan.add(Box.createVerticalStrut(3));
        panelRPOContekan.add(spRPO);

        JPanel panelAlergiContekan = new JPanel();
        panelAlergiContekan.setLayout(new BoxLayout(panelAlergiContekan, BoxLayout.Y_AXIS));
        panelAlergiContekan.setBackground(new Color(255, 241, 242)); // Soft Rose-50
        panelAlergiContekan.setBorder(new CompoundBorder(
            new LineBorder(new Color(253, 164, 175), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        panelAlergiContekan.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelAlergiContekan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        JLabel lblAlergiTitle = new JLabel("RIWAYAT ALERGI OBAT (TERSINKRONISASI & DICETAK)");
        lblAlergiTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblAlergiTitle.setForeground(new Color(159, 18, 57));
        lblAlergiTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlRowAlergi1 = new JPanel(new BorderLayout(4, 0));
        pnlRowAlergi1.setOpaque(false);
        JLabel lblObat = new JLabel("Obat Alergi:");
        lblObat.setPreferredSize(new Dimension(68, 20));
        lblObat.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblObat.setForeground(new Color(159, 18, 57));
        txtAlergiObat = new JTextField("-");
        txtAlergiObat.setFont(new Font("Segoe UI", Font.BOLD, 11));
        txtAlergiObat.setBackground(Color.WHITE);
        txtAlergiObat.setBorder(new CompoundBorder(new LineBorder(new Color(253, 164, 175), 1, true), new EmptyBorder(1, 4, 1, 4)));
        pnlRowAlergi1.add(lblObat, BorderLayout.WEST);
        pnlRowAlergi1.add(txtAlergiObat, BorderLayout.CENTER);

        JPanel pnlRowAlergi2 = new JPanel(new GridLayout(1, 2, 6, 0));
        pnlRowAlergi2.setOpaque(false);

        JPanel pnlGejala = new JPanel(new BorderLayout(4, 0));
        pnlGejala.setOpaque(false);
        JLabel lblGejala = new JLabel("Gejala:");
        lblGejala.setPreferredSize(new Dimension(38, 20));
        lblGejala.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblGejala.setForeground(new Color(159, 18, 57));
        txtManifestasiAlergi = new JTextField("-");
        txtManifestasiAlergi.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        txtManifestasiAlergi.setBackground(Color.WHITE);
        txtManifestasiAlergi.setBorder(new CompoundBorder(new LineBorder(new Color(253, 164, 175), 1, true), new EmptyBorder(1, 4, 1, 4)));
        pnlGejala.add(lblGejala, BorderLayout.WEST);
        pnlGejala.add(txtManifestasiAlergi, BorderLayout.CENTER);

        JPanel pnlDampak = new JPanel(new BorderLayout(4, 0));
        pnlDampak.setOpaque(false);
        JLabel lblDampak = new JLabel("Dampak:");
        lblDampak.setPreferredSize(new Dimension(46, 20));
        lblDampak.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDampak.setForeground(new Color(159, 18, 57));
        cmbDampakAlergi = new JComboBox<>(new String[]{"-", "Ringan", "Sedang", "Berat"});
        cmbDampakAlergi.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        cmbDampakAlergi.setBackground(Color.WHITE);
        cmbDampakAlergi.setBorder(new LineBorder(new Color(253, 164, 175), 1, true));
        pnlDampak.add(lblDampak, BorderLayout.WEST);
        pnlDampak.add(cmbDampakAlergi, BorderLayout.CENTER);

        pnlRowAlergi2.add(pnlGejala);
        pnlRowAlergi2.add(pnlDampak);

        panelAlergiContekan.add(lblAlergiTitle);
        panelAlergiContekan.add(Box.createVerticalStrut(4));
        panelAlergiContekan.add(pnlRowAlergi1);
        panelAlergiContekan.add(Box.createVerticalStrut(3));
        panelAlergiContekan.add(pnlRowAlergi2);

        contekanBody.add(pnlContekanHeader);
        contekanBody.add(Box.createVerticalStrut(5));
        contekanBody.add(lblRtl);
        contekanBody.add(Box.createVerticalStrut(3));
        contekanBody.add(spTataLaksana);
        contekanBody.add(Box.createVerticalStrut(4));
        contekanBody.add(pnlActionContekan);
        contekanBody.add(Box.createVerticalStrut(4));
        contekanBody.add(panelQuickChips);
        contekanBody.add(panelRPOContekan);
        contekanBody.add(Box.createVerticalStrut(6));
        contekanBody.add(panelAlergiContekan);

        cardSidebar.add(contekanBody, BorderLayout.CENTER);

        sidebarKiri.add(cardIdentitas);
        sidebarKiri.add(Box.createVerticalStrut(8));
        sidebarKiri.add(cardSidebar);

        // ---------------------------------------------------------------------
        // PANEL KANAN: PARAMETER (KOMPAK 2 BARIS) + INLINE TABLE LUAS & TINGGI
        // ---------------------------------------------------------------------
        JPanel mainKanan = new JPanel(new BorderLayout(10, 10));
        mainKanan.setOpaque(false);

        // A. Card Header Parameter Rekonsiliasi (Dipadatkan Menjadi 2 Baris Kompak)
        JPanel cardHeader = createCardPanel("METADATA & PARAMETER WAWANCARA", Color.WHITE, SLATE_BORDER);
        cardHeader.setLayout(new BoxLayout(cardHeader, BoxLayout.Y_AXIS));

        // Baris 1: Apoteker, No. Rekonsiliasi, Tanggal & Jam
        JPanel rowHeader1 = new JPanel(new GridLayout(1, 3, 10, 0));
        rowHeader1.setOpaque(false);

        JPanel pnlApoteker = new JPanel(new BorderLayout(4, 2));
        pnlApoteker.setOpaque(false);
        JLabel lblApotekerJudul = new JLabel("Apoteker / Petugas Farmasi *");
        lblApotekerJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblApotekerJudul.setForeground(new Color(71, 85, 105));

        KdPetugas = new JTextField(8);
        KdPetugas.setEditable(false);
        KdPetugas.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        NmPetugas = new JTextField();
        NmPetugas.setEditable(false);
        NmPetugas.setFont(new Font("Segoe UI", Font.BOLD, 11));

        JButton btnCariPetugas = new JButton();
        btnCariPetugas.setIcon(IconStyle.muat("search.svg", 13, new Color(71, 85, 105)));
        btnCariPetugas.setToolTipText("Cari Petugas / Apoteker");
        btnCariPetugas.setPreferredSize(new Dimension(28, 23));
        btnCariPetugas.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCariPetugas.addActionListener(e -> {
            petugas.isCek();
            petugas.setSize(getWidth() - 60, getHeight() - 60);
            petugas.setLocationRelativeTo(this);
            petugas.setVisible(true);
        });

        JPanel pnlPetugasInput = new JPanel(new BorderLayout(4, 0));
        pnlPetugasInput.setOpaque(false);
        pnlPetugasInput.add(KdPetugas, BorderLayout.WEST);
        pnlPetugasInput.add(NmPetugas, BorderLayout.CENTER);
        pnlPetugasInput.add(btnCariPetugas, BorderLayout.EAST);
        pnlApoteker.add(lblApotekerJudul, BorderLayout.NORTH);
        pnlApoteker.add(pnlPetugasInput, BorderLayout.CENTER);

        JPanel pnlNoRekon = new JPanel(new BorderLayout(4, 2));
        pnlNoRekon.setOpaque(false);
        JLabel lblNoRekonJudul = new JLabel("No. Rekonsiliasi (Auto)");
        lblNoRekonJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblNoRekonJudul.setForeground(new Color(71, 85, 105));

        NoRekonsiliasi = new JTextField();
        NoRekonsiliasi.setEditable(false);
        NoRekonsiliasi.setBackground(new Color(248, 250, 252));
        NoRekonsiliasi.setFont(new Font("Monospaced", Font.BOLD, 12));
        pnlNoRekon.add(lblNoRekonJudul, BorderLayout.NORTH);
        pnlNoRekon.add(NoRekonsiliasi, BorderLayout.CENTER);

        JPanel pnlWaktu = new JPanel(new BorderLayout(4, 2));
        pnlWaktu.setOpaque(false);
        JLabel lblWaktuJudul = new JLabel("Tanggal Wawancara");
        lblWaktuJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblWaktuJudul.setForeground(new Color(71, 85, 105));

        DTPTgl = new Tanggal();
        DTPTgl.setDisplayFormat("dd-MM-yyyy");
        DTPTgl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        DTPTgl.setPreferredSize(new Dimension(115, 23));
        DTPTgl.setBackground(Color.WHITE);
        DTPTgl.setBorder(new CompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(2, 6, 2, 4)
        ));
        DTPTgl.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                if (currentModeInput != ModeInput.MODE_EDIT) {
                    autoNomorRekonsiliasi();
                }
            }
        });

        JPanel pnlWaktuInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlWaktuInput.setOpaque(false);
        pnlWaktuInput.add(DTPTgl);

        pnlWaktu.add(lblWaktuJudul, BorderLayout.NORTH);
        pnlWaktu.add(pnlWaktuInput, BorderLayout.CENTER);

        rowHeader1.add(pnlApoteker);
        rowHeader1.add(pnlNoRekon);
        rowHeader1.add(pnlWaktu);

        // Baris 2: Parameter Kompak (Rekonsiliasi Saat, Penggunaan Obat Sebelum Admisi, Sumber Informasi)
        rowHeader2 = new JPanel(new GridLayout(1, 3, 8, 0));
        rowHeader2.setOpaque(false);

        // 1. Rekonsiliasi Saat
        pnlSaat = new JPanel(new BorderLayout(0, 2));
        pnlSaat.setOpaque(false);
        pnlSaat.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(2, 6, 2, 6)
        ));
        pnlSaat.setBackground(new Color(248, 250, 252));
        JLabel lblSaat = new JLabel("Rekonsiliasi Saat:");
        lblSaat.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSaat.setForeground(TEAL_DARK);

        JPanel pnlSaatRb = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlSaatRb.setOpaque(false);
        rbAdmisi = new JRadioButton("Admisi", true);
        rbTransfer = new JRadioButton("Transfer Ruang");
        rbPulang = new JRadioButton("Pulang");
        rbAdmisi.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rbTransfer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rbPulang.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ButtonGroup bgSaat = new ButtonGroup();
        bgSaat.add(rbAdmisi); bgSaat.add(rbTransfer); bgSaat.add(rbPulang);
        pnlSaatRb.add(rbAdmisi); pnlSaatRb.add(rbTransfer); pnlSaatRb.add(rbPulang);
        pnlSaat.add(lblSaat, BorderLayout.NORTH);
        pnlSaat.add(pnlSaatRb, BorderLayout.CENTER);

        // 2. Penggunaan Obat Sebelum Admisi
        pnlPakaiObat = new JPanel(new BorderLayout(0, 2));
        pnlPakaiObat.setOpaque(false);
        pnlPakaiObat.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(2, 6, 2, 6)
        ));
        pnlPakaiObat.setBackground(new Color(248, 250, 252));
        JLabel lblPakai = new JLabel("Penggunaan Obat Sebelum Admisi *:");
        lblPakai.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPakai.setForeground(TEAL_DARK);

        JPanel pnlPakaiRb = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlPakaiRb.setOpaque(false);
        rbPakaiYa = new JRadioButton("Ya (Ada Rincian)", true);
        rbPakaiTidak = new JRadioButton("Tidak Pakai");
        rbPakaiYa.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rbPakaiTidak.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ButtonGroup bgPakai = new ButtonGroup();
        bgPakai.add(rbPakaiYa); bgPakai.add(rbPakaiTidak);
        pnlPakaiRb.add(rbPakaiYa); pnlPakaiRb.add(rbPakaiTidak);
        pnlPakaiObat.add(lblPakai, BorderLayout.NORTH);
        pnlPakaiObat.add(pnlPakaiRb, BorderLayout.CENTER);

        // 3. Sumber Informasi
        pnlSumber = new JPanel(new BorderLayout(0, 2));
        pnlSumber.setOpaque(false);
        pnlSumber.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(2, 6, 2, 6)
        ));
        pnlSumber.setBackground(new Color(248, 250, 252));
        JLabel lblSumber = new JLabel("Sumber Informasi *:");
        lblSumber.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSumber.setForeground(TEAL_DARK);

        JPanel pnlSumberRb = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlSumberRb.setOpaque(false);
        rbSumberRM = new JRadioButton("Status RM", true);
        rbSumberPasien = new JRadioButton("Pasien/Kel");
        rbSumberLain = new JRadioButton("Lain:");
        rbSumberRM.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rbSumberPasien.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rbSumberLain.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ButtonGroup bgSumber = new ButtonGroup();
        bgSumber.add(rbSumberRM); bgSumber.add(rbSumberPasien); bgSumber.add(rbSumberLain);
        KetSumberLain = new JTextField(10);
        KetSumberLain.setEnabled(false);
        KetSumberLain.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pnlSumberRb.add(rbSumberRM);
        pnlSumberRb.add(rbSumberPasien);
        pnlSumberRb.add(rbSumberLain);
        pnlSumberRb.add(KetSumberLain);
        pnlSumber.add(lblSumber, BorderLayout.NORTH);
        pnlSumber.add(pnlSumberRb, BorderLayout.CENTER);

        rowHeader2.add(pnlSaat);
        rowHeader2.add(pnlPakaiObat);
        rowHeader2.add(pnlSumber);

        cardHeader.add(rowHeader1);
        cardHeader.add(Box.createVerticalStrut(5));
        cardHeader.add(rowHeader2);

        // B. Card Tengah: INLINE TABLE DAFTAR OBAT DIREKONSILIASI (LUAS & PENUH)
        JPanel cardTabelObat = createCardPanel("DAFTAR OBAT DIREKONSILIASI (INLINE EDITABLE TABLE)", Color.WHITE, TEAL_MAIN);
        cardTabelObat.setLayout(new BorderLayout(6, 6));

        JPanel toolbarTabel = new JPanel(new BorderLayout());
        toolbarTabel.setOpaque(false);
        lblBadgeJmlObat = new JLabel(" 0 obat direkonsiliasi (Ketik nama obat untuk Live Search / Spasi utk master obat) ");
        lblBadgeJmlObat.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadgeJmlObat.setForeground(TEAL_MAIN);

        JPanel btnGroupTabel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnGroupTabel.setOpaque(false);

        btnSalinRekonSebelumnya = new JButton(" Tarik Rekon Sebelumnya");
        btnSalinRekonSebelumnya.setIcon(IconStyle.muat("refresh.svg", 13, new Color(71, 85, 105)));
        btnSalinRekonSebelumnya.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSalinRekonSebelumnya.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalinRekonSebelumnya.setToolTipText("Salin daftar obat dari rekonsiliasi pasien sebelumnya (misal saat Admisi atau Transfer)");
        btnSalinRekonSebelumnya.addActionListener(e -> tarikRekonsiliasiSebelumnya());

        btnHapusBarisObat = new JButton(" Hapus Baris");
        btnHapusBarisObat.setIcon(IconStyle.muat("trash.svg", 14, new Color(225, 29, 72)));
        btnHapusBarisObat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnHapusBarisObat.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnPilihMasterObat = new JButton(" Pilih dari Data Obat/Infus");
        btnPilihMasterObat.setIcon(IconStyle.muat("search.svg", 14, Color.WHITE));
        btnPilihMasterObat.setBackground(BLUE_OCEAN);
        btnPilihMasterObat.setForeground(Color.WHITE);
        btnPilihMasterObat.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPilihMasterObat.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnTambahInline = new JButton(" Tambah Obat (Inline)");
        btnTambahInline.setIcon(IconStyle.muat("plus.svg", 14, Color.WHITE));
        btnTambahInline.setBackground(TEAL_MAIN);
        btnTambahInline.setForeground(Color.WHITE);
        btnTambahInline.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTambahInline.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnGroupTabel.add(btnSalinRekonSebelumnya);
        btnGroupTabel.add(btnHapusBarisObat);
        btnGroupTabel.add(btnPilihMasterObat);
        btnGroupTabel.add(btnTambahInline);

        toolbarTabel.add(lblBadgeJmlObat, BorderLayout.WEST);
        toolbarTabel.add(btnGroupTabel, BorderLayout.EAST);

        // Setup Model & JTable Inline
        tabModeObat = new DefaultTableModel(null, new Object[]{
            "Kode", "Nama Obat", "Dosis Obat",
            "Frekuensi", "Cara Pemberian", "Tindak Lanjut DPJP", "Perubahan Aturan Pakai"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                if (colIndex == 0) return false;
                if (colIndex == 6) {
                    String tindak = String.valueOf(getValueAt(rowIndex, 5));
                    return "Lanjut aturan pakai berubah".equalsIgnoreCase(tindak);
                }
                return true;
            }
        };

        // Listener otomatis bersihkan kolom perubahan aturan jika bukan 'Lanjut aturan pakai berubah'
        tabModeObat.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 5) {
                int row = e.getFirstRow();
                if (row >= 0 && row < tabModeObat.getRowCount()) {
                    String tindak = String.valueOf(tabModeObat.getValueAt(row, 5));
                    if (!"Lanjut aturan pakai berubah".equalsIgnoreCase(tindak)) {
                        Object val6 = tabModeObat.getValueAt(row, 6);
                        if (val6 != null && !val6.toString().trim().isEmpty()) {
                            SwingUtilities.invokeLater(() -> {
                                tabModeObat.setValueAt("", row, 6);
                            });
                        }
                    }
                }
            }
        });

        // Rekomendasi 1: JTable dengan Soft Ice Mint Selection, Zebra Striping Halus, dan Modern Editor Ring
        tbObat = new JTable(tabModeObat) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                boolean isSelected = isRowSelected(row);
                int selRow = getSelectedRow();
                int selCol = getSelectedColumn();
                boolean isCurrentCell = (row == selRow && column == selCol);

                boolean isCol6Disabled = false;
                if (column == 6) {
                    String tindak = String.valueOf(getValueAt(row, 5));
                    if (!"Lanjut aturan pakai berubah".equalsIgnoreCase(tindak)) {
                        isCol6Disabled = true;
                    }
                }

                if (isCurrentCell && !isCol6Disabled) {
                    // Sel aktif yang sedang difokuskan (posisi TAB / kursor): Latar Putih Bersih + Ring Aksen Teal
                    c.setBackground(Color.WHITE);
                    c.setForeground(new Color(15, 23, 42)); // Slate 900 gelap tajam
                    if (c instanceof JComponent) {
                        ((JComponent) c).setBorder(new CompoundBorder(
                            new LineBorder(new Color(13, 148, 136), 2, true), // Ring Teal 2px
                            new EmptyBorder(1, 6, 1, 6)
                        ));
                    }
                } else if (isSelected) {
                    // Baris terpilih lainnya tetap Soft Ice Mint yang sejuk
                    c.setBackground(new Color(204, 251, 241)); // #CCFBF1 Soft Ice Mint Selection
                    if (isCol6Disabled) {
                        c.setForeground(new Color(100, 116, 139)); // Slate muted saat kolom terkunci
                    } else {
                        c.setForeground(new Color(15, 23, 42));    // #0F172A Slate 900 gelap tajam
                    }
                    if (c instanceof JComponent) {
                        ((JComponent) c).setBorder(new EmptyBorder(0, 8, 0, 8));
                    }
                } else {
                    // Baris tidak terpilih: zebra striping rapi seragam
                    c.setBackground((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252)); // Zebra #F8FAFC
                    if (isCol6Disabled) {
                        c.setForeground(new Color(148, 163, 184)); // Muted saat kolom terkunci
                    } else {
                        c.setForeground(new Color(30, 41, 59));
                    }
                    if (c instanceof JComponent) {
                        ((JComponent) c).setBorder(new EmptyBorder(0, 8, 0, 8));
                    }
                }

                if (c instanceof JLabel) {
                    JLabel lbl = (JLabel) c;
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    if (column == 2 || column == 3) {
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    } else {
                        lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                }

                return c;
            }
        };
        tbObat.setRowHeight(30);
        tbObat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tbObat.setSelectionBackground(new Color(204, 251, 241)); // #CCFBF1 Soft Ice Mint
        tbObat.setSelectionForeground(new Color(15, 23, 42));    // #0F172A Slate 900
        tbObat.setGridColor(new Color(226, 232, 240));          // #E2E8F0 Grid halus
        tbObat.setShowGrid(true);
        tbObat.setShowHorizontalLines(true);
        tbObat.setShowVerticalLines(true);
        tbObat.setSurrendersFocusOnKeystroke(true);
        tbObat.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // Pasang aksi tombol TAB dan Shift+TAB pada JTable (Pindah kolom & langsung buka mode edit)
        tbObat.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "tabMaju"
        );
        tbObat.getActionMap().put("tabMaju", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pindahKolomTab(true);
            }
        });

        tbObat.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), "tabMundur"
        );
        tbObat.getActionMap().put("tabMundur", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pindahKolomTab(false);
            }
        });

        tbObat.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tbObat.getTableHeader().setBackground(new Color(241, 245, 249));
        tbObat.getTableHeader().setForeground(new Color(51, 65, 85));
        tbObat.getTableHeader().setPreferredSize(new Dimension(tbObat.getTableHeader().getWidth(), 30));
        tbObat.getTableHeader().setReorderingAllowed(false);

        // Sembunyikan kolom Kode Obat (index 0)
        tbObat.getColumnModel().getColumn(0).setMinWidth(0);
        tbObat.getColumnModel().getColumn(0).setMaxWidth(0);
        tbObat.getColumnModel().getColumn(0).setWidth(0);

        // Lebar kolom
        tbObat.getColumnModel().getColumn(1).setPreferredWidth(270);
        tbObat.getColumnModel().getColumn(2).setPreferredWidth(90);
        tbObat.getColumnModel().getColumn(3).setPreferredWidth(90);
        tbObat.getColumnModel().getColumn(4).setPreferredWidth(120);
        tbObat.getColumnModel().getColumn(5).setPreferredWidth(190);
        tbObat.getColumnModel().getColumn(6).setPreferredWidth(190);

        // Text Editor Kolom 2 (Dosis), 3 (Frekuensi), 6 (Perubahan Aturan Pakai) dengan Ring Teal (Single Click)
        tbObat.getColumnModel().getColumn(2).setCellEditor(createModernTextEditor(SwingConstants.CENTER));
        tbObat.getColumnModel().getColumn(3).setCellEditor(createModernTextEditor(SwingConstants.CENTER));
        tbObat.getColumnModel().getColumn(6).setCellEditor(createModernTextEditor(SwingConstants.LEFT));

        // ComboBox Cara Pemberian (Kolom 4) dengan Ring Teal Modern (Single Click + TAB navigation)
        JComboBox<String> cmbCara = new JComboBox<>(new String[]{
            "Oral", "Injeksi IV", "Injeksi IM", "Injeksi SC", "Infus", "Inhalasi", "Sublingual", "Topikal", "Rektal", "Tetes"
        });
        cmbCara.setEditable(true);
        cmbCara.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cmbCara.setBackground(Color.WHITE);
        cmbCara.setForeground(new Color(15, 23, 42));
        cmbCara.setBorder(new LineBorder(new Color(13, 148, 136), 2, true));
        cmbCara.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
        cmbCara.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet());
        cmbCara.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    pindahKolomTab(!e.isShiftDown());
                    e.consume();
                }
            }
        });
        DefaultCellEditor editorCara = new DefaultCellEditor(cmbCara) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(new LineBorder(new Color(13, 148, 136), 2, true));
                }
                c.setBackground(Color.WHITE);
                c.setForeground(new Color(15, 23, 42));
                return c;
            }
        };
        editorCara.setClickCountToStart(1);
        tbObat.getColumnModel().getColumn(4).setCellEditor(editorCara);

        // ComboBox Tindak Lanjut DPJP (Kolom 5) dengan Ring Teal Modern (Single Click + TAB navigation)
        JComboBox<String> cmbTindak = new JComboBox<>(new String[]{
            "Lanjut aturan pakai sama", "Lanjut aturan pakai berubah", "Stop", "Baru"
        });
        cmbTindak.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cmbTindak.setBackground(Color.WHITE);
        cmbTindak.setForeground(new Color(15, 23, 42));
        cmbTindak.setBorder(new LineBorder(new Color(13, 148, 136), 2, true));
        cmbTindak.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
        cmbTindak.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet());
        cmbTindak.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    pindahKolomTab(!e.isShiftDown());
                    e.consume();
                }
            }
        });
        DefaultCellEditor editorTindak = new DefaultCellEditor(cmbTindak) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(new LineBorder(new Color(13, 148, 136), 2, true));
                }
                c.setBackground(Color.WHITE);
                c.setForeground(new Color(15, 23, 42));
                return c;
            }
        };
        editorTindak.setClickCountToStart(1);
        tbObat.getColumnModel().getColumn(5).setCellEditor(editorTindak);

        // Pasang Live Autocomplete pada Kolom 1 (Nama Obat)
        initAutoCompleteObat();

        cardTabelObat.add(toolbarTabel, BorderLayout.NORTH);
        cardTabelObat.add(new JScrollPane(tbObat), BorderLayout.CENTER);

        // Panel Kanan HANYA berisi Header Parameter (North) dan Tabel Obat (Center)
        mainKanan.add(cardHeader, BorderLayout.NORTH);
        mainKanan.add(cardTabelObat, BorderLayout.CENTER);

        JScrollPane spSidebar = new JScrollPane(sidebarKiri);
        spSidebar.setBorder(null);
        spSidebar.setOpaque(false);
        spSidebar.getViewport().setOpaque(false);
        spSidebar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spSidebar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        spSidebar.getVerticalScrollBar().setUnitIncrement(24);
        spSidebar.setPreferredSize(new Dimension(365, 600));

        // Solusi Masalah Klasik Swing: Smart Mouse Wheel Forwarding dari TextArea ke parent ScrollPane
        pasangSmartMouseWheel(spTataLaksana, txtContekanTataLaksana, spSidebar);
        pasangSmartMouseWheel(spRPO, txtContekanRPO, spSidebar);

        container.add(spSidebar, BorderLayout.WEST);
        container.add(mainKanan, BorderLayout.CENTER);
        return container;
    }

    private JPanel buildTabData() {
        containerTabData = new JPanel(new GridLayout(1, 2, 12, 12));
        containerTabData.setBackground(SLATE_BG);
        containerTabData.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Kiri: Tabel Daftar Riwayat Rekonsiliasi
        panelKiriData = createCardPanel("DAFTAR REKONSILIASI OBAT PASIEN", Color.WHITE, SLATE_BORDER);
        panelKiriData.setLayout(new BorderLayout(6, 6));

        tabModeDataRekon = new DefaultTableModel(null, new Object[]{
            "No. Rekonsiliasi", "No.Rawat", "No.RM", "Nama Pasien", "Tgl Wawancara",
            "Rekonsiliasi Saat", "NIP", "Apoteker", "Status Konfirm"
        }) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbDataRekon = new JTable(tabModeDataRekon);
        tbDataRekon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbDataRekon.setRowHeight(26);
        tbDataRekon.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tbDataRekon.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tbDataRekon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = tbDataRekon.getSelectedRow();
                updateStateButtonsTab2(r);
                if (r != -1) {
                    String noRekon = tbDataRekon.getValueAt(r, 0).toString();
                    muatPreviewHTML(noRekon);
                } else {
                    LoadHTML.setText("");
                }
            }
        });
        tbDataRekon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    if (tbDataRekon.getSelectedRow() != -1) {
                        gantiDataRekonsiliasi();
                    }
                }
            }
        });

        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panelFilter.setOpaque(false);

        DTPCari1 = new Tanggal();
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        DTPCari1.setPreferredSize(new Dimension(105, 23));
        DTPCari1.setBackground(Color.WHITE);
        DTPCari1.setBorder(new CompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(2, 6, 2, 4)
        ));

        DTPCari2 = new Tanggal();
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        DTPCari2.setPreferredSize(new Dimension(105, 23));
        DTPCari2.setBackground(Color.WHITE);
        DTPCari2.setBorder(new CompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(2, 6, 2, 4)
        ));

        TCari = new JTextField(15);
        TCari.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        TCari.setPreferredSize(new Dimension(130, 23));
        TCari.setBackground(Color.WHITE);
        TCari.setBorder(new CompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(2, 6, 2, 4)
        ));
        TCari.addActionListener(e -> tampilDataRekonsiliasi());

        JButton btnCariData = new JButton(" Cari");
        btnCariData.setIcon(IconStyle.muat("search.svg", 13, new Color(71, 85, 105)));
        btnCariData.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCariData.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCariData.addActionListener(e -> tampilDataRekonsiliasi());

        JButton btnSemuaData = new JButton(" Semua");
        btnSemuaData.setIcon(IconStyle.muat("refresh.svg", 13, new Color(71, 85, 105)));
        btnSemuaData.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSemuaData.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSemuaData.addActionListener(e -> {
            TCari.setText("");
            tampilDataRekonsiliasi();
        });

        LCount = new JLabel("Record : 0");
        LCount.setFont(new Font("Segoe UI", Font.BOLD, 11));
        LCount.setForeground(TEAL_MAIN);

        btnCetakRekap = new JButton(" Rekap");
        btnCetakRekap.setIcon(IconStyle.muat("printer.svg", 13, new Color(71, 85, 105)));
        btnCetakRekap.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnCetakRekap.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCetakRekap.setToolTipText("Cetak rekapitulasi data rekonsiliasi obat (rptDataRekonsiliasiObat.jasper)");
        btnCetakRekap.addActionListener(e -> cetakRekapitulasi());

        panelFilter.add(new JLabel("Tgl:"));
        panelFilter.add(DTPCari1);
        panelFilter.add(new JLabel("s.d."));
        panelFilter.add(DTPCari2);
        panelFilter.add(new JLabel("Kata Kunci:"));
        panelFilter.add(TCari);
        panelFilter.add(btnCariData);
        panelFilter.add(btnSemuaData);
        panelFilter.add(btnCetakRekap);
        panelFilter.add(LCount);

        panelKiriData.add(new JScrollPane(tbDataRekon), BorderLayout.CENTER);
        panelKiriData.add(panelFilter, BorderLayout.SOUTH);

        // Kanan: Panel Preview Form Fisik & Status Validasi Apoteker
        panelKananData = createCardPanel("PREVIEW FORM FISIK RM 03-06A & VALIDASI APOTEKER", Color.WHITE, SLATE_BORDER);
        panelKananData.setLayout(new BorderLayout(6, 6));

        JPanel panelToolbarPreview = new JPanel(new BorderLayout(6, 0));
        panelToolbarPreview.setOpaque(false);
        panelToolbarPreview.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel lblTitlePreview = new JLabel(" PRATINJAU DOKUMEN (RM 03-06A)");
        lblTitlePreview.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitlePreview.setForeground(TEAL_DARK);

        JPanel pnlActionPreview = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlActionPreview.setOpaque(false);

        btnPopupPreview = new JButton("Jendela Layar Penuh");
        btnPopupPreview.setBackground(TEAL_MAIN);
        btnPopupPreview.setForeground(Color.WHITE);
        btnPopupPreview.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPopupPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPopupPreview.setEnabled(false);
        btnPopupPreview.setToolTipText("Buka pratinjau dokumen di jendela modal terpisah layar penuh (Full Screen Preview)");
        btnPopupPreview.addActionListener(e -> {
            int r = tbDataRekon.getSelectedRow();
            if (r != -1) {
                bukaDialogPreviewFullscreen(tbDataRekon.getValueAt(r, 0).toString());
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih baris data rekonsiliasi yang ingin dilihat pratinjaunya!");
            }
        });

        pnlActionPreview.add(btnPopupPreview);

        panelToolbarPreview.add(lblTitlePreview, BorderLayout.WEST);
        panelToolbarPreview.add(pnlActionPreview, BorderLayout.EAST);

        LoadHTML = new JEditorPane();
        LoadHTML.setContentType("text/html");
        LoadHTML.setEditable(false);
        LoadHTML.setBackground(Color.WHITE);
        if (LoadHTML.getCaret() instanceof javax.swing.text.DefaultCaret) {
            ((javax.swing.text.DefaultCaret) LoadHTML.getCaret()).setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
        }
        LoadHTML.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int r = tbDataRekon.getSelectedRow();
                    if (r != -1) {
                        bukaDialogPreviewFullscreen(tbDataRekon.getValueAt(r, 0).toString());
                    }
                }
            }
        });

        scrollPreviewHTML = new JScrollPane(LoadHTML);
        panelKananData.add(panelToolbarPreview, BorderLayout.NORTH);
        panelKananData.add(scrollPreviewHTML, BorderLayout.CENTER);

        containerTabData.add(panelKiriData);
        containerTabData.add(panelKananData);
        return containerTabData;
    }

    private JPanel buildFooterBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, SLATE_BORDER), new EmptyBorder(8, 16, 8, 16)));

        // Tombol saat Tab 1 Aktif
        panelFooterInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelFooterInput.setOpaque(false);

        btnSimpan = new JButton(" Simpan");
        btnSimpan.setIcon(IconStyle.muat("save.svg", 15, Color.WHITE));
        btnSimpan.setBackground(TEAL_MAIN);
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.addActionListener(e -> simpanRekonsiliasi());

        btnBaru = new JButton(" Reset / Baru");
        btnBaru.setIcon(IconStyle.muat("refresh.svg", 15, new Color(71, 85, 105)));
        btnBaru.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBaru.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBaru.addActionListener(e -> batalEditAtauBaru());

        chkLangsungTTE = new JCheckBox(" Langsung Validasi & Bubuhkan TTE setelah simpan");
        chkLangsungTTE.setOpaque(false);
        chkLangsungTTE.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkLangsungTTE.setForeground(new Color(51, 65, 85));
        chkLangsungTTE.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelFooterInput.add(btnSimpan);
        panelFooterInput.add(btnBaru);
        panelFooterInput.add(Box.createHorizontalStrut(6));
        panelFooterInput.add(chkLangsungTTE);

        // Tombol saat Tab 2 Aktif (Validasi TTE + Ganti + Hapus + Cetak)
        panelFooterData = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelFooterData.setOpaque(false);
        panelFooterData.setVisible(false);

        btnValidasiTTE = new JButton(" Validasi & Bubuhkan TTE");
        btnValidasiTTE.setIcon(IconStyle.muat("check.svg", 15, Color.WHITE));
        btnValidasiTTE.setBackground(GREEN_EMERALD);
        btnValidasiTTE.setForeground(Color.WHITE);
        btnValidasiTTE.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnValidasiTTE.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnValidasiTTE.setEnabled(false);
        btnValidasiTTE.addActionListener(e -> validasiAtauTarikTTE());

        btnGantiData = new JButton(" Ganti / Edit Data");
        btnGantiData.setIcon(IconStyle.muat("pencil.svg", 15, new Color(71, 85, 105)));
        btnGantiData.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGantiData.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGantiData.setEnabled(false);
        btnGantiData.addActionListener(e -> gantiDataRekonsiliasi());

        btnHapusRekon = new JButton(" Hapus");
        btnHapusRekon.setIcon(IconStyle.muat("trash.svg", 15, new Color(225, 29, 72)));
        btnHapusRekon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHapusRekon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapusRekon.setEnabled(false);
        btnHapusRekon.addActionListener(e -> hapusRekonsiliasi());

        btnCetak = new JButton(" Cetak RM 03-06A");
        btnCetak.setIcon(IconStyle.muat("printer.svg", 15, Color.WHITE));
        btnCetak.setBackground(GREEN_EMERALD);
        btnCetak.setForeground(Color.WHITE);
        btnCetak.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCetak.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCetak.setEnabled(false);
        btnCetak.setToolTipText("Cetak Formulir Rekonsiliasi Obat (RM 03-06A) ke Printer atau PDF");
        btnCetak.addActionListener(e -> cetakRM0306A());
        pasangMenuCetakOpsional(btnCetak);

        panelFooterData.add(btnValidasiTTE);
        panelFooterData.add(btnGantiData);
        panelFooterData.add(btnHapusRekon);
        panelFooterData.add(btnCetak);

        JPanel leftFooterWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftFooterWrapper.setOpaque(false);
        leftFooterWrapper.add(panelFooterInput);
        leftFooterWrapper.add(panelFooterData);

        JButton btnKeluar = new JButton(" Keluar");
        btnKeluar.setIcon(IconStyle.muat("log-out.svg", 15, new Color(225, 29, 72)));
        btnKeluar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnKeluar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKeluar.addActionListener(e -> dispose());

        footer.add(leftFooterWrapper, BorderLayout.WEST);
        footer.add(btnKeluar, BorderLayout.EAST);
        return footer;
    }

    // =========================================================================
    // LOGIKA INTERAKTIF & LISTENERS
    // =========================================================================
    private void initListeners() {
        // Ganti tombol footer sesuai Tab yang dibuka
        tabPaneUtama.addChangeListener(e -> {
            boolean isInput = (tabPaneUtama.getSelectedIndex() == 0);
            panelFooterInput.setVisible(isInput);
            panelFooterData.setVisible(!isInput);
            if (!isInput) {
                tampilDataRekonsiliasi();
                SwingUtilities.invokeLater(() -> {
                    if (scrollPreviewHTML != null) {
                        scrollPreviewHTML.getVerticalScrollBar().setValue(0);
                    }
                    LoadHTML.setCaretPosition(0);
                });
            }
        });

        // Listener Perubahan Tahap Rekonsiliasi Saat (Admisi, Transfer, Pulang)
        ActionListener saatListener = e -> {
            updateVisibilityPenggunaanObat();
            updateModeSidebarSaat();
        };
        rbAdmisi.addActionListener(saatListener);
        rbTransfer.addActionListener(saatListener);
        rbPulang.addActionListener(saatListener);

        // Kondisional: Jika "Tidak Menggunakan Obat Sebelum Admisi", kunci tabel obat
        ActionListener pakaiListener = e -> {
            boolean pakai = rbPakaiYa.isSelected();
            if (!pakai) {
                tabModeObat.setRowCount(0);
                updateBadgeObat();
            }
            tbObat.setEnabled(pakai);
            btnTambahInline.setEnabled(pakai);
            btnPilihMasterObat.setEnabled(pakai);
            btnHapusBarisObat.setEnabled(pakai);
            if (btnSalinRekonSebelumnya != null) btnSalinRekonSebelumnya.setEnabled(pakai);
        };
        rbPakaiYa.addActionListener(pakaiListener);
        rbPakaiTidak.addActionListener(pakaiListener);

        // Kondisional: Sumber Informasi Lain-lain
        ActionListener sumberListener = e -> {
            KetSumberLain.setEnabled(rbSumberLain.isSelected());
            if (!rbSumberLain.isSelected()) KetSumberLain.setText("");
        };
        rbSumberRM.addActionListener(sumberListener);
        rbSumberPasien.addActionListener(sumberListener);
        rbSumberLain.addActionListener(sumberListener);

        // Tambah Baris Obat Inline (Otomatis fokus dan langsung buka edit nama obat)
        btnTambahInline.addActionListener(e -> {
            tabModeObat.addRow(new Object[]{"", "", "", "", "Oral", "Lanjut aturan pakai sama", "-"});
            updateBadgeObat();
            int lastRow = tabModeObat.getRowCount() - 1;
            tbObat.setRowSelectionInterval(lastRow, lastRow);
            tbObat.scrollRectToVisible(tbObat.getCellRect(lastRow, 1, true));
            SwingUtilities.invokeLater(() -> {
                tbObat.editCellAt(lastRow, 1);
                Component comp = tbObat.getEditorComponent();
                if (comp != null) {
                    comp.requestFocusInWindow();
                }
            });
        });

        // Hapus Baris Obat
        btnHapusBarisObat.addActionListener(e -> {
            int r = tbObat.getSelectedRow();
            if (r != -1) {
                tabModeObat.removeRow(r);
            } else if (tabModeObat.getRowCount() > 0) {
                tabModeObat.removeRow(tabModeObat.getRowCount() - 1);
            }
            updateBadgeObat();
        });

        // Buka Dialog Master Obat (inventory.DlgBarang)
        btnPilihMasterObat.addActionListener(e -> bukaDialogCariObat());

        // Shortcut Spasi atau Ctrl+F di kolom Nama Obat untuk memicu DlgBarang
        tbObat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_SPACE || (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F))
                        && tbObat.getSelectedColumn() == 1) {
                    bukaDialogCariObat();
                }
            }
        });

        // Otomatis isi "-" pada kolom Perubahan Aturan Pakai jika tidak berubah
        tabModeObat.addTableModelListener(e -> {
            if (e.getColumn() == 5 && e.getFirstRow() >= 0) {
                int r = e.getFirstRow();
                String tindak = String.valueOf(tabModeObat.getValueAt(r, 5));
                if (!"Lanjut aturan pakai berubah".equalsIgnoreCase(tindak)) {
                    tabModeObat.setValueAt("-", r, 6);
                }
            }
        });


        // Listener hasil pilihan Petugas/Apoteker (kolom 0 = NIP, kolom 1 = Nama)
        petugas.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (petugas.getTable().getSelectedRow() != -1) {
                    int sel = petugas.getTable().getSelectedRow();
                    KdPetugas.setText(petugas.getTable().getValueAt(sel, 0).toString());
                    NmPetugas.setText(petugas.getTable().getValueAt(sel, 1).toString());
                }
            }
        });
    }

    private Icon createMaximizeIcon(final boolean isRestore) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.4f));

                if (!isRestore) {
                    // Kotak persegi bersih Windows 11 style (Maximize)
                    g2.drawRoundRect(x + 1, y + 1, 10, 10, 2, 2);
                } else {
                    // Dua kotak bertumpuk Windows 11 style (Restore)
                    g2.drawRoundRect(x + 4, y, 8, 8, 2, 2);
                    g2.setColor(c != null ? c.getBackground() : TEAL_MAIN);
                    g2.fillRect(x, y + 4, 9, 9);
                    g2.setColor(Color.WHITE);
                    g2.drawRoundRect(x, y + 4, 8, 8, 2, 2);
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 13;
            }

            @Override
            public int getIconHeight() {
                return 13;
            }
        };
    }

    private void toggleMaximize() {
        Rectangle maxScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        if (!isMaximized) {
            normalBounds = getBounds();
            setBounds(maxScreen);
            btnMaximize.setIcon(createMaximizeIcon(true));
            btnMaximize.setToolTipText("Pulihkan Ukuran Jendela (Restore)");
            isMaximized = true;
        } else {
            if (normalBounds != null) {
                setBounds(normalBounds);
            } else {
                Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                int targetW = Math.min(1420, (int) (screen.width * 0.95));
                int targetH = Math.min(860, (int) (screen.height * 0.95));
                setSize(targetW, targetH);
                setLocationRelativeTo(null);
            }
            btnMaximize.setIcon(createMaximizeIcon(false));
            btnMaximize.setToolTipText("Perbesar Layar Penuh (Maximize)");
            isMaximized = false;
        }
    }

    /**
     * Solusi Masalah Klasik Swing: Menggerakkan scrollbar secara langsung (direct scrollbar control)
     * - Jika kursor di TextArea dan isi teks panjang: TextArea di-scroll. Begitu teks mentok atas/bawah, parent ScrollPane yang lanjut di-scroll!
     * - Jika isi teks pendek (tidak butuh scroll): Roda mouse langsung meng-scroll parent ScrollPane (spSidebar)!
     */
    private void pasangSmartMouseWheel(JScrollPane childScroll, JTextArea childText, JScrollPane parentScroll) {
        MouseWheelListener mwl = e -> {
            JScrollBar childBar = childScroll.getVerticalScrollBar();
            JScrollBar parentBar = parentScroll.getVerticalScrollBar();
            int rot = e.getWheelRotation();
            int units = e.getUnitsToScroll();

            boolean childScrollable = (childBar != null && childBar.isVisible() && (childBar.getMaximum() > childBar.getVisibleAmount()));

            if (childScrollable) {
                int oldVal = childBar.getValue();
                int maxVal = childBar.getMaximum() - childBar.getVisibleAmount();
                int minVal = childBar.getMinimum();

                if (rot > 0) { // Scroll ke bawah
                    if (oldVal < maxVal) {
                        int delta = units * childBar.getUnitIncrement();
                        childBar.setValue(Math.min(oldVal + delta, maxVal));
                    } else if (parentBar != null && parentBar.isVisible()) {
                        int delta = units * parentBar.getUnitIncrement();
                        parentBar.setValue(Math.min(parentBar.getValue() + delta, parentBar.getMaximum() - parentBar.getVisibleAmount()));
                    }
                } else if (rot < 0) { // Scroll ke atas
                    if (oldVal > minVal) {
                        int delta = units * childBar.getUnitIncrement();
                        childBar.setValue(Math.max(oldVal + delta, minVal));
                    } else if (parentBar != null && parentBar.isVisible()) {
                        int delta = units * parentBar.getUnitIncrement();
                        parentBar.setValue(Math.max(parentBar.getValue() + delta, parentBar.getMinimum()));
                    }
                }
            } else {
                // Teks pendek, tidak butuh scrollbar di textarea -> LANGSUNG scroll parent (spSidebar)!
                if (parentBar != null && parentBar.isVisible()) {
                    int delta = units * parentBar.getUnitIncrement();
                    if (rot > 0) {
                        parentBar.setValue(Math.min(parentBar.getValue() + delta, parentBar.getMaximum() - parentBar.getVisibleAmount()));
                    } else {
                        parentBar.setValue(Math.max(parentBar.getValue() + delta, parentBar.getMinimum()));
                    }
                }
            }
            e.consume();
        };
        childText.addMouseWheelListener(mwl);
    }

    private void bukaDialogCariObat() {
        DlgCariNamaObat dlgObat = new DlgCariNamaObat(null, false);
        dlgObat.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dlgObat.getTable().getSelectedRow() != -1) {
                    int sel = dlgObat.getTable().getSelectedRow();
                    String kode = dlgObat.getTable().getValueAt(sel, 0).toString();
                    String nama = dlgObat.getTable().getValueAt(sel, 1).toString();
                    int selRow = tbObat.getSelectedRow();
                    if (selRow != -1) {
                        tabModeObat.setValueAt(kode, selRow, 0);
                        tabModeObat.setValueAt(nama, selRow, 1);
                    } else {
                        tabModeObat.addRow(new Object[]{kode, nama, "", "", "Oral", "Lanjut aturan pakai sama", "-"});
                        int last = tabModeObat.getRowCount() - 1;
                        tbObat.setRowSelectionInterval(last, last);
                    }
                    updateBadgeObat();
                    tbObat.requestFocus();
                }
            }
        });
        dlgObat.setSize(Math.min(780, getWidth() - 40), Math.min(520, getHeight() - 40));
        dlgObat.setLocationRelativeTo(this);
        dlgObat.setVisible(true);
    }

    private void pindahKolomTab(final boolean maju) {
        int row = tbObat.getSelectedRow();
        int col = tbObat.getSelectedColumn();
        if (row == -1) row = 0;
        if (col == -1) col = 1;

        if (tbObat.isEditing()) {
            TableCellEditor ed = tbObat.getCellEditor();
            if (ed != null) {
                ed.stopCellEditing();
            }
        }

        int nextRow = row;
        int nextCol = col;

        while (true) {
            if (maju) {
                nextCol++;
                if (nextCol >= tbObat.getColumnCount()) {
                    nextCol = 1; // Lewati kolom 0 (Kode tersembunyi)
                    nextRow++;
                }
                if (nextRow >= tbObat.getRowCount()) {
                    // Otomatis tambah baris baru di ujung tabel
                    tabModeObat.addRow(new Object[]{"", "", "", "", "Oral", "Lanjut aturan pakai sama", "-"});
                    updateBadgeObat();
                    nextRow = tabModeObat.getRowCount() - 1;
                    nextCol = 1;
                }
            } else {
                nextCol--;
                if (nextCol < 1) {
                    nextCol = tbObat.getColumnCount() - 1;
                    nextRow--;
                }
                if (nextRow < 0) {
                    nextRow = 0;
                    nextCol = 1;
                    break;
                }
            }

            if (tbObat.isCellEditable(nextRow, nextCol)) {
                break;
            }
        }

        final int targetRow = nextRow;
        final int targetCol = nextCol;

        tbObat.changeSelection(targetRow, targetCol, false, false);
        tbObat.scrollRectToVisible(tbObat.getCellRect(targetRow, targetCol, true));

        SwingUtilities.invokeLater(() -> {
            tbObat.editCellAt(targetRow, targetCol);
            Component editorComp = tbObat.getEditorComponent();
            if (editorComp != null) {
                editorComp.requestFocusInWindow();
                if (editorComp instanceof JTextField) {
                    ((JTextField) editorComp).selectAll();
                } else if (editorComp instanceof JComboBox) {
                    JComboBox<?> cb = (JComboBox<?>) editorComp;
                    if (cb.isEditable() && cb.getEditor().getEditorComponent() instanceof JTextField) {
                        ((JTextField) cb.getEditor().getEditorComponent()).selectAll();
                    }
                }
            }
        });
    }

    private DefaultCellEditor createModernTextEditor(final int align) {
        final JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txt.setBackground(Color.WHITE);
        txt.setForeground(new Color(15, 23, 42)); // Slate 900
        txt.setHorizontalAlignment(align);
        txt.setBorder(new CompoundBorder(
            new LineBorder(new Color(13, 148, 136), 2, true),
            new EmptyBorder(1, 6, 1, 6)
        ));

        // Tangani tombol TAB agar langsung pindah dan buka mode edit sel berikutnya
        txt.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
        txt.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet());
        txt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    pindahKolomTab(!e.isShiftDown());
                    e.consume();
                }
            }
        });

        DefaultCellEditor editor = new DefaultCellEditor(txt) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(new CompoundBorder(
                        new LineBorder(new Color(13, 148, 136), 2, true),
                        new EmptyBorder(1, 6, 1, 6)
                    ));
                }
                c.setBackground(Color.WHITE);
                c.setForeground(new Color(15, 23, 42));
                SwingUtilities.invokeLater(() -> {
                    txt.selectAll();
                    txt.requestFocusInWindow();
                });
                return c;
            }
        };
        editor.setClickCountToStart(1); // CUKUP 1 KALI KLIK LANGSUNG AKTIF!
        return editor;
    }

    private void initAutoCompleteObat() {
        popupSaranObat = new JPopupMenu();
        popupSaranObat.setFocusable(false);
        popupSaranObat.setBorder(new LineBorder(TEAL_MAIN, 1, false));

        modelSaranObat = new DefaultListModel<>();
        listSaranObat = new JList<>(modelSaranObat);
        listSaranObat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        listSaranObat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSaranObat.setSelectionBackground(new Color(204, 251, 241)); // #CCFBF1 Soft Ice Mint
        listSaranObat.setSelectionForeground(new Color(15, 23, 42));    // Slate 900
        listSaranObat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        listSaranObat.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(5, 10, 5, 10));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                if (isSelected) {
                    lbl.setBackground(new Color(204, 251, 241));
                    lbl.setForeground(new Color(15, 23, 42));
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(30, 41, 59));
                }
                return lbl;
            }
        });

        JScrollPane scrollSaran = new JScrollPane(listSaranObat);
        scrollSaran.setBorder(null);
        scrollSaran.setPreferredSize(new Dimension(380, 150));
        popupSaranObat.add(scrollSaran);

        txtNamaObatEditor = new JTextField();
        txtNamaObatEditor.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtNamaObatEditor.setBackground(Color.WHITE);
        txtNamaObatEditor.setForeground(new Color(15, 23, 42));
        txtNamaObatEditor.setBorder(new CompoundBorder(
            new LineBorder(new Color(13, 148, 136), 2, true),
            new EmptyBorder(1, 6, 1, 6)
        ));

        // Tangani tombol TAB di kolom nama obat agar langsung lompat ke dosis
        txtNamaObatEditor.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
        txtNamaObatEditor.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet());

        DefaultCellEditor cellEditorObat = new DefaultCellEditor(txtNamaObatEditor) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                rowObatSedangDiedit = row;
                Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(new CompoundBorder(
                        new LineBorder(new Color(13, 148, 136), 2, true),
                        new EmptyBorder(1, 6, 1, 6)
                    ));
                }
                c.setBackground(Color.WHITE);
                c.setForeground(new Color(15, 23, 42));
                SwingUtilities.invokeLater(() -> {
                    txtNamaObatEditor.selectAll();
                    txtNamaObatEditor.requestFocusInWindow();
                });
                return c;
            }

            @Override
            public boolean stopCellEditing() {
                try {
                    PointerInfo pi = MouseInfo.getPointerInfo();
                    if (pi != null && popupSaranObat != null && popupSaranObat.isShowing()) {
                        Point mousePos = pi.getLocation();
                        Point popPos = popupSaranObat.getLocationOnScreen();
                        Rectangle popBounds = new Rectangle(popPos.x, popPos.y, popupSaranObat.getWidth(), popupSaranObat.getHeight());
                        if (popBounds.contains(mousePos)) {
                            return super.stopCellEditing();
                        }
                    }
                } catch (Exception ex) {}
                if (popupSaranObat != null && popupSaranObat.isVisible()) {
                    popupSaranObat.setVisible(false);
                }
                return super.stopCellEditing();
            }

            @Override
            public void cancelCellEditing() {
                try {
                    PointerInfo pi = MouseInfo.getPointerInfo();
                    if (pi != null && popupSaranObat != null && popupSaranObat.isShowing()) {
                        Point mousePos = pi.getLocation();
                        Point popPos = popupSaranObat.getLocationOnScreen();
                        Rectangle popBounds = new Rectangle(popPos.x, popPos.y, popupSaranObat.getWidth(), popupSaranObat.getHeight());
                        if (popBounds.contains(mousePos)) {
                            super.cancelCellEditing();
                            return;
                        }
                    }
                } catch (Exception ex) {}
                if (popupSaranObat != null && popupSaranObat.isVisible()) {
                    popupSaranObat.setVisible(false);
                }
                super.cancelCellEditing();
            }
        };
        cellEditorObat.setClickCountToStart(1); // CUKUP 1 KALI KLIK LANGSUNG AKTIF!
        tbObat.getColumnModel().getColumn(1).setCellEditor(cellEditorObat);

        // Document Listener saat mengetik di sel
        txtNamaObatEditor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> cariSaranLive());
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> cariSaranLive());
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> cariSaranLive());
            }
        });

        // Key Listener navigasi keyboard
        txtNamaObatEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    if (popupSaranObat.isVisible()) {
                        popupSaranObat.setVisible(false);
                    }
                    pindahKolomTab(!e.isShiftDown());
                    e.consume();
                    return;
                }

                if (popupSaranObat.isVisible()) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        int next = listSaranObat.getSelectedIndex() + 1;
                        if (next < modelSaranObat.getSize()) {
                            listSaranObat.setSelectedIndex(next);
                            listSaranObat.ensureIndexIsVisible(next);
                        }
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        int prev = listSaranObat.getSelectedIndex() - 1;
                        if (prev >= 0) {
                            listSaranObat.setSelectedIndex(prev);
                            listSaranObat.ensureIndexIsVisible(prev);
                        }
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (listSaranObat.getSelectedIndex() != -1) {
                            pilihObatDariSaran(listSaranObat.getSelectedValue());
                            e.consume();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        popupSaranObat.setVisible(false);
                        e.consume();
                    }
                }
            }
        });

        // Mouse listener pada list saran (tangani klik mouse agar langsung terpilih)
        MouseAdapter mouseHandlerSaran = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                eksekusiPilihDariMouse(e);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                eksekusiPilihDariMouse(e);
            }
            private void eksekusiPilihDariMouse(MouseEvent e) {
                long now = System.currentTimeMillis();
                if (now - lastPilihObatTime < 250) return;
                int index = listSaranObat.locationToIndex(e.getPoint());
                if (index != -1) {
                    lastPilihObatTime = now;
                    listSaranObat.setSelectedIndex(index);
                    String terpilih = modelSaranObat.getElementAt(index);
                    pilihObatDariSaran(terpilih);
                }
            }
        };
        listSaranObat.addMouseListener(mouseHandlerSaran);
    }

    private void cariSaranLive() {
        if (!txtNamaObatEditor.isShowing()) {
            popupSaranObat.setVisible(false);
            return;
        }
        String text = txtNamaObatEditor.getText().trim();
        if (text.length() < 2) {
            popupSaranObat.setVisible(false);
            return;
        }

        modelSaranObat.clear();
        mapKodeObat.clear();
        try {
            ps = koneksi.prepareStatement(
                "SELECT kode_brng, nama_brng FROM databarang WHERE status='1' AND (nama_brng LIKE ? OR kode_brng LIKE ?) ORDER BY nama_brng ASC LIMIT 12"
            );
            ps.setString(1, "%" + text + "%");
            ps.setString(2, "%" + text + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                String kode = rs.getString("kode_brng");
                String nama = rs.getString("nama_brng");
                modelSaranObat.addElement(nama);
                mapKodeObat.put(nama, kode);
            }
        } catch (Exception ex) {
            System.out.println("Notif Cari Live: " + ex);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } catch (Exception e) {}
        }

        if (modelSaranObat.getSize() > 0 && txtNamaObatEditor.isShowing()) {
            listSaranObat.setSelectedIndex(0);
            popupSaranObat.show(txtNamaObatEditor, 0, txtNamaObatEditor.getHeight());
            txtNamaObatEditor.requestFocus();
        } else {
            popupSaranObat.setVisible(false);
        }
    }

    private void pilihObatDariSaran(String nama) {
        if (nama == null || nama.isEmpty()) return;
        String kode = mapKodeObat.getOrDefault(nama, "");
        if (popupSaranObat != null) {
            popupSaranObat.setVisible(false);
        }

        int row = rowObatSedangDiedit;
        if (row < 0 || row >= tabModeObat.getRowCount()) {
            row = tbObat.getEditingRow();
        }
        if (row < 0 || row >= tabModeObat.getRowCount()) {
            row = tbObat.getSelectedRow();
        }

        if (row >= 0 && row < tabModeObat.getRowCount()) {
            final int targetRow = row;
            txtNamaObatEditor.setText(nama);
            tabModeObat.setValueAt(kode, targetRow, 0);
            tabModeObat.setValueAt(nama, targetRow, 1);
            if (tbObat.isEditing()) {
                TableCellEditor ed = tbObat.getCellEditor();
                if (ed != null) ed.cancelCellEditing();
            }
            SwingUtilities.invokeLater(() -> {
                tbObat.changeSelection(targetRow, 2, false, false);
                tbObat.editCellAt(targetRow, 2);
                Component editor = tbObat.getEditorComponent();
                if (editor != null) {
                    editor.requestFocusInWindow();
                    if (editor instanceof JTextField) {
                        ((JTextField) editor).selectAll();
                    }
                }
            });
        }
        updateBadgeObat();
    }

    public static class ItemObatTerdeteksi {
        public String kode = "";
        public String nama = "";
        public String dosis = "-";
        public String aturan = "-";
        public String rute = "Oral";
        public String tindakLanjut = "Lanjut aturan pakai sama";
        public String perubahan = "-";
    }

    private static final Pattern PATTERN_NON_OBAT = Pattern.compile(
            "^(bedrest|tirah\\s+baring|diet|puasa|observasi|pantau|monitoring|balance|konsul|rujuk|cek\\s+lab|rontgen|usg|ekg|aff|lepas|ganti\\s+perban|edukasi|rawat\\s+luka|alih\\s+rawat|persiapan|posisi|oksigen|o2|npo|pasang\\s+dc|pasang\\s+ngt)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATTERN_DOSIS = Pattern.compile(
            "\\b(\\d+([.,]\\d+)?\\s*(mg|mcg|gr|gram|iu|ml|tpm|tab|tablet|kaps|kapsul|amp|ampul|vial|kolf|fls|cth|c|sendok|supp)\\b)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATTERN_FREKUENSI = Pattern.compile(
            "(\\b\\d+\\s*[xX]\\s*\\d+([.,/]\\d+)?(\\s*\\([^)]+\\))?|\\/\\s*\\d+\\s*(jam|hours?)|\\b(tiap|setiap|bila|jika|k\\/p|prn)\\s+[^,\\-]+|\\b\\d+\\s*(jam|hari)\\s*sekali|\\/\\s*bab\\s*cair)",
            Pattern.CASE_INSENSITIVE
    );

    private String cariKodeBarangMaster(String nama) {
        if (nama == null || nama.trim().isEmpty()) return "";
        String clean = nama.toLowerCase()
                           .replaceAll("^(inj|injeksi|tab|tablet|kaps|kapsul|syr|sirup|ivfd|infus|drop|salep)\\.?\\s+", "")
                           .trim();
        String[] words = clean.split("[\\s\\/\\-]+");
        String keyword = words.length > 0 ? words[0] : clean;
        if (keyword.length() < 3) return "";

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("SELECT kode_brng FROM databarang WHERE status='1' AND (nama_brng LIKE ? OR kode_brng LIKE ?) ORDER BY LENGTH(nama_brng) ASC LIMIT 1");
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, keyword + "%");
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("kode_brng");
            }
        } catch (Exception e) {
            // diamkan error untuk menjaga kelancaran antarmuka
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (Exception ex) {}
        }
        return "";
    }

    private boolean isBukanObat(String token) {
        String lower = token.toLowerCase();
        if (PATTERN_NON_OBAT.matcher(lower).find()) {
            if (!PATTERN_DOSIS.matcher(lower).find() && !lower.contains("inj") && !lower.contains("ivfd") && !lower.contains("tab")) {
                return true;
            }
        }
        return false;
    }

    private boolean isObat(String token) {
        String lower = token.toLowerCase();

        // 1. Bentuk sediaan obat
        if (lower.contains("inj") || lower.contains("injeksi") || lower.contains("ivfd") ||
            lower.contains("infus") || lower.contains("drip") || lower.contains("tab") ||
            lower.contains("kaps") || lower.contains("syr") || lower.contains("sirup") ||
            lower.contains("drop") || lower.contains("salep") || lower.contains("cream") ||
            lower.contains("supp") || lower.contains("neb") || lower.contains("pulv") ||
            lower.contains("fls") || lower.contains("amp") || lower.contains("vial")) {
            return true;
        }

        // 2. Cairan infus umum / nama generik umum
        if (lower.matches(".*\\b(rl|nacl|ns|d5|d10|kaen|tutofusin|ringer|asering|maltose|gelafusal|paracetamol|ceftriaxone|ondansentron|pantoprazole|omeprazole|ketorolac|ranitidin|amoxicillin|cefixime|zink|diatab)\\b.*")) {
            return true;
        }

        // 3. Mengandung dosis (misal 500 mg, 4mg, 20 tpm)
        if (PATTERN_DOSIS.matcher(lower).find()) {
            return true;
        }

        // 4. Mengandung pola aturan pakai (3x1, 1x1, /8 jam)
        if (lower.matches(".*\\b\\d+\\s*[xX]\\s*\\d+.*") || lower.matches(".*\\/\\s*\\d+\\s*jam.*")) {
            return true;
        }

        // 5. Cek apakah ada di master databarang RS
        String kode = cariKodeBarangMaster(token);
        return (kode != null && !kode.isEmpty());
    }

    private ItemObatTerdeteksi ekstrakDetailObat(String token) {
        ItemObatTerdeteksi item = new ItemObatTerdeteksi();
        String sisa = token;

        // Tentukan rute pemberian
        String lower = token.toLowerCase();
        if (lower.contains("inj") || lower.contains("injeksi") || lower.contains("ivfd") ||
            lower.contains("infus") || lower.contains("drip") || lower.contains("iv") ||
            lower.contains("im") || lower.contains("sc")) {
            item.rute = "Injeksi/IV";
        } else if (lower.contains("salep") || lower.contains("cream") || lower.contains("krim") ||
                   lower.contains("gel") || lower.contains("tetes") || lower.contains("drop")) {
            item.rute = "Topikal";
        } else if (lower.contains("supp") || lower.contains("rectal")) {
            item.rute = "Rektal";
        } else if (lower.contains("neb") || lower.contains("inhalasi")) {
            item.rute = "Inhalasi";
        } else {
            item.rute = "Oral";
        }

        // Ekstrak pola gabungan misal "3x500mg" atau "3x1 tab"
        Matcher mComb = Pattern.compile("(\\b\\d+\\s*[xX])\\s*(\\d+([.,]\\d+)?\\s*(mg|mcg|gr|gram|iu|ml|tab|tablet|kaps|kapsul|amp|ampul))\\b", Pattern.CASE_INSENSITIVE).matcher(sisa);
        if (mComb.find()) {
            item.aturan = mComb.group(1).trim() + "1";
            item.dosis = mComb.group(2).trim();
            sisa = sisa.substring(0, mComb.start()) + " " + sisa.substring(mComb.end());
        } else {
            // Ekstrak Aturan Pakai / Frekuensi biasa
            Matcher mFrek = PATTERN_FREKUENSI.matcher(sisa);
            if (mFrek.find()) {
                item.aturan = mFrek.group(0).trim();
                if (item.aturan.equalsIgnoreCase("/bab cair")) {
                    item.aturan = "tiap BAB cair";
                }
                sisa = sisa.substring(0, mFrek.start()) + " " + sisa.substring(mFrek.end());
            }

            // Ekstrak Dosis biasa
            Matcher mDosis = PATTERN_DOSIS.matcher(sisa);
            if (mDosis.find()) {
                item.dosis = mDosis.group(0).trim();
                if (item.dosis.toLowerCase().contains("tpm")) {
                    if (item.aturan.isEmpty() || item.aturan.equals("-")) {
                        item.aturan = item.dosis;
                        item.dosis = "-";
                    }
                }
                sisa = sisa.substring(0, mDosis.start()) + " " + sisa.substring(mDosis.end());
            }
        }

        // Cek jika ada sisa keterangan seperti "bila demam", "jika nyeri"
        Matcher mKet = Pattern.compile("\\b(bila|jika|k\\/p|prn)\\s+[^,\\-]+", Pattern.CASE_INSENSITIVE).matcher(sisa);
        if (mKet.find()) {
            String ket = mKet.group(0).trim();
            if (!item.aturan.contains(ket)) {
                item.aturan = (item.aturan.isEmpty() || item.aturan.equals("-")) ? ket : (item.aturan + " (" + ket + ")");
            }
            sisa = sisa.substring(0, mKet.start()) + " " + sisa.substring(mKet.end());
        }

        // Bersihkan sisa nama obat
        String namaBersih = sisa.replaceAll("[\\(\\)\\/\\-;,]", " ")
                                .replaceAll("\\s+", " ")
                                .trim();

        if (namaBersih.isEmpty() || namaBersih.length() < 2) {
            namaBersih = token;
        }

        item.nama = namaBersih;
        if (item.dosis.isEmpty()) item.dosis = "-";
        if (item.aturan.isEmpty()) item.aturan = "-";

        // Cari kode obat di master database
        item.kode = cariKodeBarangMaster(item.nama);

        return item;
    }

    private java.util.List<ItemObatTerdeteksi> parseDaftarObatDariTeks(String teks) {
        java.util.List<ItemObatTerdeteksi> hasil = new ArrayList<>();
        if (teks == null || teks.trim().isEmpty() || teks.startsWith("(Belum ada")) {
            return hasil;
        }

        // Multi-delimiter split: newline, dash with spaces, semicolon, comma (not in decimal), numbered inline list (1. 2. 1) 2)), or bullets
        String[] tokens = teks.split("(\\r?\\n|\\s+[-–—]+\\s+|(?<=\\s|^)[\\-\\*\u2022]\\s+|(?<=\\s|^)\\d+[\\.\\)]\\s+|\\s*;\\s*|\\s*(?<!\\d),(?!\\d)\\s*)");

        for (String raw : tokens) {
            if (raw == null) continue;
            String token = raw.trim().replaceAll("^[\\-\\*\u2022\\d+\\.\\)\\s]+", "").trim();
            if (token.length() < 3) continue;

            if (isBukanObat(token)) continue;
            if (!isObat(token)) continue;

            ItemObatTerdeteksi item = ekstrakDetailObat(token);
            hasil.add(item);
        }
        return hasil;
    }

    private void kirimTeksTerblokKeTabel() {
        String selected = txtContekanTataLaksana.getSelectedText();
        if (selected == null || selected.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Blok/sorot dulu nama obat pada kotak Tata Laksana di sebelah kiri, lalu klik tombol ini.");
            return;
        }
        ItemObatTerdeteksi item = ekstrakDetailObat(selected.trim());
        tabModeObat.addRow(new Object[]{
            item.kode,
            item.nama,
            item.dosis,
            item.aturan,
            item.rute,
            item.tindakLanjut,
            item.perubahan
        });
        updateBadgeObat();
    }

    private void updateBadgeObat() {
        lblBadgeJmlObat.setText(" " + tabModeObat.getRowCount() + " obat direkonsiliasi ");
    }

    /**
     * Membangun chip tombol cepat dari teks tata laksana dokter
     */
    private void generateQuickChips(String tataLaksana) {
        panelQuickChips.removeAll();
        if (tataLaksana == null || tataLaksana.trim().isEmpty() || tataLaksana.startsWith("(Belum ada")) {
            panelQuickChips.revalidate();
            panelQuickChips.repaint();
            return;
        }

        java.util.List<ItemObatTerdeteksi> listObat = parseDaftarObatDariTeks(tataLaksana);
        int count = 0;
        for (ItemObatTerdeteksi item : listObat) {
            String label = item.nama;
            if (label.length() > 20) {
                label = label.substring(0, 18) + "..";
            }
            JButton chip = new JButton(label);
            chip.setIcon(IconStyle.muat("plus.svg", 10, TEAL_MAIN));
            chip.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            chip.setBackground(Color.WHITE);
            chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chip.setToolTipText(item.nama + " (" + item.dosis + ", " + item.aturan + ", " + item.rute + ")");
            chip.addActionListener(e -> {
                tabModeObat.addRow(new Object[]{
                    item.kode,
                    item.nama,
                    item.dosis,
                    item.aturan,
                    item.rute,
                    item.tindakLanjut,
                    item.perubahan
                });
                updateBadgeObat();
            });
            panelQuickChips.add(chip);
            count++;
            if (count >= 8) break;
        }
        panelQuickChips.revalidate();
        panelQuickChips.repaint();
    }

    /**
     * Dipanggil dari caller SIMRS saat membuka formulir untuk nomor rawat pasien tertentu
     */
    public void setNoRm(String norwt) {
        TNoRw.setText(norwt);
        if (TCari != null) {
            TCari.setText(norwt);
        }

        // 1. Muat data pasien
        try {
            ps = koneksi.prepareStatement(
                "SELECT rp.no_rkm_medis, p.nm_pasien, IF(p.jk='L','Laki-Laki','Perempuan') AS jk, p.tgl_lahir, "
                + "rp.umurdaftar, rp.sttsumur, rp.tgl_registrasi "
                + "FROM reg_periksa rp "
                + "INNER JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis "
                + "WHERE rp.no_rawat = ?"
            );
            ps.setString(1, norwt);
            rs = ps.executeQuery();
            if (rs.next()) {
                String noRM = rs.getString("no_rkm_medis");
                String nama = rs.getString("nm_pasien");
                String jk = rs.getString("jk");
                String tglLahir = rs.getString("tgl_lahir");
                String umur = rs.getString("umurdaftar") + " " + rs.getString("sttsumur");

                TNoRM.setText(noRM);
                lblPasienNama.setText(nama);
                lblPasienJk.setText(jk);
                TTglLahir.setText(tglLahir);
                TUmurStatus.setText(umur);

                // Avatar inisial
                String init = getInitials(nama);
                lblAvatarInitials.setText(init);

                if (DTPCari1 != null && rs.getDate("tgl_registrasi") != null) {
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Load Pasien: " + e);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } catch (Exception e) {}
        }

        // 2. Tarik otomatis Contekan Asesmen Awal Medis (Ranap & IGD)
        tataRanap = ""; rpoRanap = ""; alergiRanap = "";
        tataIGD = ""; rpoIGD = ""; alergiIGD = "";

        // A. Cek Asesmen Medis Ranap
        try {
            ps = koneksi.prepareStatement("SELECT tata, rpo, alergi FROM penilaian_medis_ranap WHERE no_rawat=?");
            ps.setString(1, norwt);
            rs = ps.executeQuery();
            if (rs.next()) {
                tataRanap = rs.getString("tata") != null ? rs.getString("tata") : "";
                rpoRanap = rs.getString("rpo") != null ? rs.getString("rpo") : "";
                alergiRanap = rs.getString("alergi") != null ? rs.getString("alergi") : "";
            }
        } catch (Exception e) {
            System.out.println("Notif Cek Ranap: " + e);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } catch (Exception e) {}
        }

        // B. Cek Asesmen Medis IGD
        try {
            ps = koneksi.prepareStatement("SELECT tata, rpo, alergi FROM penilaian_medis_igd WHERE no_rawat=?");
            ps.setString(1, norwt);
            rs = ps.executeQuery();
            if (rs.next()) {
                tataIGD = rs.getString("tata") != null ? rs.getString("tata") : "";
                rpoIGD = rs.getString("rpo") != null ? rs.getString("rpo") : "";
                alergiIGD = rs.getString("alergi") != null ? rs.getString("alergi") : "";
            }
        } catch (Exception e) {
            System.out.println("Notif Cek IGD: " + e);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } catch (Exception e) {}
        }

        // C. Cek Ralan sebagai cadangan jika IGD kosong
        if (tataIGD.isEmpty() && rpoIGD.isEmpty()) {
            try {
                ps = koneksi.prepareStatement("SELECT tata, rpo, alergi FROM penilaian_medis_ralan WHERE no_rawat=?");
                ps.setString(1, norwt);
                rs = ps.executeQuery();
                if (rs.next()) {
                    tataIGD = rs.getString("tata") != null ? rs.getString("tata") : "";
                    rpoIGD = rs.getString("rpo") != null ? rs.getString("rpo") : "";
                    alergiIGD = rs.getString("alergi") != null ? rs.getString("alergi") : "";
                }
            } catch (Exception e) {
                System.out.println("Notif Cek Ralan: " + e);
            } finally {
                try { if (rs != null) rs.close(); if (ps != null) ps.close(); } catch (Exception e) {}
            }
        }

        boolean adaRanap = !tataRanap.trim().isEmpty() || !rpoRanap.trim().isEmpty();
        boolean adaIGD = !tataIGD.trim().isEmpty() || !rpoIGD.trim().isEmpty();

        btnToggleRanap.setText(adaRanap ? "Ranap" : "Ranap (-)");
        btnToggleIGD.setText(adaIGD ? "IGD" : "IGD (-)");

        // 3. Muat data Resep Ranap Tervalidasi Farmasi
        muatResepRanapTervalidasi(norwt);

        // 4. Perbarui Tampilan Sidebar Sesuai Pilihan Rekonsiliasi Saat Ini
        updateModeSidebarSaat();

        autoNomorRekonsiliasi();
    }

    private void updateVisibilityPenggunaanObat() {
        if (rowHeader2 == null || pnlPakaiObat == null) return;
        boolean isAdmisi = rbAdmisi.isSelected();
        if (isAdmisi) {
            rowHeader2.removeAll();
            rowHeader2.setLayout(new GridLayout(1, 3, 8, 0));
            rowHeader2.add(pnlSaat);
            rowHeader2.add(pnlPakaiObat);
            rowHeader2.add(pnlSumber);
            pnlPakaiObat.setVisible(true);

            boolean pakai = rbPakaiYa.isSelected();
            tbObat.setEnabled(pakai);
            btnTambahInline.setEnabled(pakai);
            btnPilihMasterObat.setEnabled(pakai);
            btnHapusBarisObat.setEnabled(pakai);
            if (btnSalinRekonSebelumnya != null) btnSalinRekonSebelumnya.setEnabled(pakai);
        } else {
            pnlPakaiObat.setVisible(false);
            rowHeader2.removeAll();
            rowHeader2.setLayout(new GridLayout(1, 2, 8, 0));
            rowHeader2.add(pnlSaat);
            rowHeader2.add(pnlSumber);

            // Pada Transfer Ruang atau Pulang: tabel obat selalu aktif
            tbObat.setEnabled(true);
            btnTambahInline.setEnabled(true);
            btnPilihMasterObat.setEnabled(true);
            btnHapusBarisObat.setEnabled(true);
            if (btnSalinRekonSebelumnya != null) btnSalinRekonSebelumnya.setEnabled(true);
        }
        rowHeader2.revalidate();
        rowHeader2.repaint();
    }

    private void updateModeSidebarSaat() {
        if (rbTransfer.isSelected() || rbPulang.isSelected()) {
            tampilkanResepRanap();
        } else {
            tampilkanAsesmenMedis();
        }
    }

    private void tampilkanAsesmenMedis() {
        if (borderCardSidebar != null) {
            borderCardSidebar.setTitle(" ASESMEN MEDIS (RANAP / IGD) ");
            if (cardSidebar != null) cardSidebar.repaint();
        }
        if (lblSubJudul != null) {
            lblSubJudul.setText("Sumber Asesmen:");
        }
        if (pnlToggle != null) {
            pnlToggle.removeAll();
            pnlToggle.add(btnToggleIGD);
            pnlToggle.add(btnToggleRanap);
            pnlToggle.revalidate();
            pnlToggle.repaint();
        }
        if (lblRtl != null) {
            lblRtl.setText("1. Tata Laksana / Terapi Dokter:");
        }
        if (panelRPOContekan != null) {
            panelRPOContekan.setVisible(true);
        }
        if (spTataLaksana != null) {
            spTataLaksana.setPreferredSize(new Dimension(330, 115));
            spTataLaksana.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        }
        if (cardSidebar != null) {
            cardSidebar.revalidate();
            cardSidebar.repaint();
        }
        boolean adaRanap = !tataRanap.trim().isEmpty() || !rpoRanap.trim().isEmpty();
        boolean adaIGD = !tataIGD.trim().isEmpty() || !rpoIGD.trim().isEmpty();
        if (adaRanap) {
            tampilkanContekanSumber("RANAP");
        } else if (adaIGD) {
            tampilkanContekanSumber("IGD");
        } else {
            tampilkanContekanSumber(sumberTerpilih);
        }
    }

    private void tampilkanResepRanap() {
        if (borderCardSidebar != null) {
            String judul = rbTransfer.isSelected() ? " RESEP RANAP (TRANSFER RUANG) " : " RESEP RANAP (PULANG) ";
            borderCardSidebar.setTitle(judul);
            if (cardSidebar != null) cardSidebar.repaint();
        }
        if (lblSubJudul != null) {
            lblSubJudul.setText("Status Resep:");
        }
        if (pnlToggle != null) {
            pnlToggle.removeAll();
            JLabel lblBadgeResep = new JLabel(rbTransfer.isSelected() ? " Resep Dokter Ranap (Transfer) " : " Resep Dokter Ranap (Pulang) ");
            lblBadgeResep.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblBadgeResep.setForeground(new Color(15, 118, 110));
            lblBadgeResep.setBackground(new Color(204, 251, 241));
            lblBadgeResep.setOpaque(true);
            lblBadgeResep.setBorder(new CompoundBorder(
                new LineBorder(new Color(45, 212, 191), 1, true),
                new EmptyBorder(1, 5, 1, 5)
            ));

            JButton btnRefreshResep = new JButton();
            btnRefreshResep.setIcon(IconStyle.muat("refresh.svg", 12, new Color(71, 85, 105)));
            btnRefreshResep.setToolTipText("Muat Ulang Resep Ranap");
            btnRefreshResep.setPreferredSize(new Dimension(24, 20));
            btnRefreshResep.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRefreshResep.addActionListener(e -> {
                muatResepRanapTervalidasi(TNoRw.getText().trim());
                tampilkanResepRanap();
            });

            pnlToggle.add(lblBadgeResep);
            pnlToggle.add(btnRefreshResep);
            pnlToggle.revalidate();
            pnlToggle.repaint();
        }
        if (lblRtl != null) {
            lblRtl.setText(rbTransfer.isSelected() ? "Daftar Resep Dokter Ranap Saat Transfer:" : "Daftar Resep Dokter Ranap Pulang:");
        }
        if (panelRPOContekan != null) {
            panelRPOContekan.setVisible(false);
        }
        if (spTataLaksana != null) {
            spTataLaksana.setPreferredSize(new Dimension(330, 245));
            spTataLaksana.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        }
        if (cardSidebar != null) {
            cardSidebar.revalidate();
            cardSidebar.repaint();
        }
        txtContekanTataLaksana.setText((resepRanapTervalidasi == null || resepRanapTervalidasi.trim().isEmpty())
            ? "(Belum ada resep dokter rawat inap yang tercatat untuk no rawat ini)"
            : resepRanapTervalidasi);
        txtContekanTataLaksana.setCaretPosition(0);

        generateQuickChipsResep(listObatResepTervalidasi);

        // Alergi
        String alergi = !alergiRanap.trim().isEmpty() ? alergiRanap : (!alergiIGD.trim().isEmpty() ? alergiIGD : "-");
        if (txtAlergiObat != null) {
            txtAlergiObat.setText(alergi);
        }
    }

    private void styleToggleBtn(JButton btn, boolean aktif) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (aktif) {
            btn.setBackground(TEAL_MAIN);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(
                new LineBorder(TEAL_DARK, 1, true),
                new EmptyBorder(4, 12, 4, 12)
            ));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(71, 85, 105));
            btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 12, 4, 12)
            ));
        }
    }

    private void pasangHoverToggleBtn(JButton btn, String sumber) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!sumber.equalsIgnoreCase(sumberTerpilih)) {
                    btn.setBackground(new Color(241, 245, 249));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!sumber.equalsIgnoreCase(sumberTerpilih)) {
                    btn.setBackground(Color.WHITE);
                }
            }
        });
    }

    private void tampilkanContekanSumber(String sumber) {
        sumberTerpilih = sumber;
        if ("IGD".equalsIgnoreCase(sumber)) {
            styleToggleBtn(btnToggleIGD, true);
            styleToggleBtn(btnToggleRanap, false);

            txtContekanTataLaksana.setText((tataIGD == null || tataIGD.trim().isEmpty()) ? "(Belum ada catatan Tata Laksana di Asesmen Medis IGD)" : tataIGD.trim());
            txtContekanRPO.setText((rpoIGD == null || rpoIGD.trim().isEmpty()) ? "(Belum ada catatan Riwayat Obat di Asesmen Medis IGD)" : rpoIGD.trim());
            if (txtAlergiObat != null && alergiIGD != null && !alergiIGD.trim().isEmpty()) {
                txtAlergiObat.setText(alergiIGD.trim());
            }
            generateQuickChips(tataIGD);
        } else {
            styleToggleBtn(btnToggleRanap, true);
            styleToggleBtn(btnToggleIGD, false);

            txtContekanTataLaksana.setText((tataRanap == null || tataRanap.trim().isEmpty()) ? "(Belum ada catatan Tata Laksana di Asesmen Medis Ranap)" : tataRanap.trim());
            txtContekanRPO.setText((rpoRanap == null || rpoRanap.trim().isEmpty()) ? "(Belum ada catatan Riwayat Obat di Asesmen Medis Ranap)" : rpoRanap.trim());
            if (txtAlergiObat != null && alergiRanap != null && !alergiRanap.trim().isEmpty()) {
                txtAlergiObat.setText(alergiRanap.trim());
            }
            generateQuickChips(tataRanap);
        }
        txtContekanTataLaksana.setCaretPosition(0);
        txtContekanRPO.setCaretPosition(0);
    }

    private void muatResepRanapTervalidasi(String norwt) {
        listObatResepTervalidasi.clear();
        StringBuilder sb = new StringBuilder();
        if (norwt == null || norwt.trim().isEmpty()) {
            resepRanapTervalidasi = "";
            return;
        }

        int totalResep = 0;
        int totalItem = 0;

        // 1. Ambil dari resep_obat khusus status ranap saja (baik tervalidasi maupun order baru dokter)
        try {
            PreparedStatement psResep = koneksi.prepareStatement(
                "SELECT ro.no_resep, ro.tgl_peresepan, ro.jam_peresepan, ro.tgl_perawatan, ro.jam, "
                + "IFNULL(d.nm_dokter, '-') AS nm_dokter, ro.status "
                + "FROM resep_obat ro "
                + "LEFT JOIN dokter d ON ro.kd_dokter = d.kd_dokter "
                + "WHERE ro.no_rawat = ? AND LOWER(ro.status) = 'ranap' "
                + "ORDER BY (ro.tgl_perawatan <> '0000-00-00') DESC, ro.tgl_peresepan DESC, ro.jam_peresepan DESC, ro.no_resep DESC"
            );
            psResep.setString(1, norwt);
            ResultSet rsResep = psResep.executeQuery();
            while (rsResep.next()) {
                totalResep++;
                String noResep = rsResep.getString("no_resep");
                boolean isTervalidasi = !rsResep.getString("tgl_perawatan").equals("0000-00-00");
                String tglBeri = isTervalidasi
                    ? rsResep.getString("tgl_perawatan") + " " + rsResep.getString("jam") + " (Tervalidasi Farmasi)"
                    : rsResep.getString("tgl_peresepan") + " " + rsResep.getString("jam_peresepan") + " (Order Resep Dokter)";
                String dokter = rsResep.getString("nm_dokter");
                String statusResep = rsResep.getString("status");

                sb.append("========================================\n");
                sb.append("RESEP: ").append(noResep).append(" (").append(statusResep.toUpperCase()).append(")\n");
                sb.append("Waktu        : ").append(tglBeri).append("\n");
                sb.append("Dokter       : ").append(dokter).append("\n");
                sb.append("----------------------------------------\n");

                // A. Resep Non-Racikan
                PreparedStatement psDetail = koneksi.prepareStatement(
                    "SELECT rd.kode_brng, b.nama_brng, rd.jml, rd.aturan_pakai, IFNULL(k.satuan, '') AS satuan "
                    + "FROM resep_dokter rd "
                    + "INNER JOIN databarang b ON rd.kode_brng = b.kode_brng "
                    + "LEFT JOIN kodesatuan k ON b.kode_sat = k.kode_sat "
                    + "WHERE rd.no_resep = ?"
                );
                psDetail.setString(1, noResep);
                ResultSet rsDetail = psDetail.executeQuery();
                while (rsDetail.next()) {
                    totalItem++;
                    String kodeBrng = rsDetail.getString("kode_brng");
                    String namaBrng = rsDetail.getString("nama_brng");
                    String jml = rsDetail.getString("jml");
                    String satuan = rsDetail.getString("satuan");
                    String aturan = rsDetail.getString("aturan_pakai");
                    if (aturan == null || aturan.trim().isEmpty()) aturan = "-";

                    sb.append(" • ").append(namaBrng)
                      .append(" (Jml: ").append(jml).append(" ").append(satuan).append(")\n")
                      .append("   Aturan: ").append(aturan).append("\n");

                    Map<String, String> item = new HashMap<>();
                    item.put("kode", kodeBrng);
                    item.put("nama", namaBrng);
                    item.put("aturan", aturan);
                    listObatResepTervalidasi.add(item);
                }
                rsDetail.close();
                psDetail.close();

                // B. Resep Racikan
                PreparedStatement psRacik = koneksi.prepareStatement(
                    "SELECT rdr.no_racik, rdr.nama_racik, rdr.jml_dr, rdr.aturan_pakai, rdr.keterangan "
                    + "FROM resep_dokter_racikan rdr "
                    + "WHERE rdr.no_resep = ?"
                );
                psRacik.setString(1, noResep);
                ResultSet rsRacik = psRacik.executeQuery();
                while (rsRacik.next()) {
                    totalItem++;
                    String noRacik = rsRacik.getString("no_racik");
                    String namaRacik = rsRacik.getString("nama_racik");
                    String jmlDr = rsRacik.getString("jml_dr");
                    String aturanRacik = rsRacik.getString("aturan_pakai");
                    if (aturanRacik == null || aturanRacik.trim().isEmpty()) aturanRacik = "-";

                    StringBuilder sbKomponen = new StringBuilder();
                    PreparedStatement psKomp = koneksi.prepareStatement(
                        "SELECT b.nama_brng "
                        + "FROM resep_dokter_racikan_detail rdd "
                        + "INNER JOIN databarang b ON rdd.kode_brng = b.kode_brng "
                        + "WHERE rdd.no_resep = ? AND rdd.no_racik = ?"
                    );
                    psKomp.setString(1, noResep);
                    psKomp.setString(2, noRacik);
                    ResultSet rsKomp = psKomp.executeQuery();
                    while (rsKomp.next()) {
                        if (sbKomponen.length() > 0) sbKomponen.append(", ");
                        sbKomponen.append(rsKomp.getString("nama_brng"));
                    }
                    rsKomp.close();
                    psKomp.close();

                    sb.append(" • [Racikan] ").append(namaRacik)
                      .append(" (Jml: ").append(jmlDr).append(")\n")
                      .append("   Aturan: ").append(aturanRacik).append("\n");
                    if (sbKomponen.length() > 0) {
                        sb.append("   Komposisi: ").append(sbKomponen).append("\n");
                    }

                    Map<String, String> item = new HashMap<>();
                    item.put("kode", "");
                    item.put("nama", "[Racikan] " + namaRacik);
                    item.put("aturan", aturanRacik);
                    listObatResepTervalidasi.add(item);
                }
                rsRacik.close();
                psRacik.close();

                sb.append("\n");
            }
            rsResep.close();
            psResep.close();
        } catch (Exception e) {
            System.out.println("Notif Muat Resep Ranap: " + e);
        }

        // 2. Fallback: jika resep_obat kosong, cek detail_pemberian_obat (pemberian langsung/depo)
        if (totalResep == 0) {
            try {
                PreparedStatement psDpo = koneksi.prepareStatement(
                    "SELECT dpo.tgl_perawatan, dpo.jam, b.kode_brng, b.nama_brng, dpo.jml, "
                    + "IFNULL(k.satuan, '') AS satuan, IFNULL(ap.aturan, '-') AS aturan_pakai "
                    + "FROM detail_pemberian_obat dpo "
                    + "INNER JOIN databarang b ON dpo.kode_brng = b.kode_brng "
                    + "LEFT JOIN kodesatuan k ON b.kode_sat = k.kode_sat "
                    + "LEFT JOIN aturan_pakai ap ON dpo.no_rawat = ap.no_rawat "
                    + "AND dpo.tgl_perawatan = ap.tgl_perawatan AND dpo.jam = ap.jam AND dpo.kode_brng = ap.kode_brng "
                    + "WHERE dpo.no_rawat = ? AND LOWER(dpo.status) = 'ranap' "
                    + "ORDER BY dpo.tgl_perawatan DESC, dpo.jam DESC"
                );
                psDpo.setString(1, norwt);
                ResultSet rsDpo = psDpo.executeQuery();
                String tglPrev = "";
                while (rsDpo.next()) {
                    String tgl = rsDpo.getString("tgl_perawatan") + " " + rsDpo.getString("jam");
                    if (!tgl.equals(tglPrev)) {
                        if (totalItem > 0) sb.append("\n");
                        sb.append("========================================\n");
                        sb.append("PEMBERIAN OBAT: ").append(tgl).append("\n");
                        sb.append("----------------------------------------\n");
                        tglPrev = tgl;
                    }
                    totalItem++;
                    String kodeBrng = rsDpo.getString("kode_brng");
                    String namaBrng = rsDpo.getString("nama_brng");
                    String jml = rsDpo.getString("jml");
                    String satuan = rsDpo.getString("satuan");
                    String aturan = rsDpo.getString("aturan_pakai");

                    sb.append(" • ").append(namaBrng)
                      .append(" (Jml: ").append(jml).append(" ").append(satuan).append(")\n")
                      .append("   Aturan: ").append(aturan).append("\n");

                    Map<String, String> item = new HashMap<>();
                    item.put("kode", kodeBrng);
                    item.put("nama", namaBrng);
                    item.put("aturan", aturan);
                    listObatResepTervalidasi.add(item);
                }
                rsDpo.close();
                psDpo.close();
            } catch (Exception e) {
                System.out.println("Notif Muat Detail Pemberian: " + e);
            }
        }

        resepRanapTervalidasi = sb.toString();
    }

    private void generateQuickChipsResep(java.util.List<Map<String, String>> list) {
        panelQuickChips.removeAll();
        if (list == null || list.isEmpty()) {
            panelQuickChips.revalidate();
            panelQuickChips.repaint();
            return;
        }

        Set<String> addedNames = new HashSet<>();
        int count = 0;
        for (Map<String, String> item : list) {
            final String nama = item.get("nama");
            final String kode = item.get("kode");
            final String aturan = item.get("aturan");
            if (nama == null || addedNames.contains(nama.toLowerCase())) continue;
            addedNames.add(nama.toLowerCase());

            String label = nama.length() > 22 ? nama.substring(0, 20) + ".." : nama;
            JButton chip = new JButton(label);
            chip.setIcon(IconStyle.muat("plus.svg", 10, TEAL_MAIN));
            chip.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            chip.setBackground(Color.WHITE);
            chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chip.setToolTipText(nama + (aturan != null && !"-".equals(aturan) ? " (Aturan: " + aturan + ")" : ""));
            chip.addActionListener(e -> {
                String tindakLanjut = "Lanjut aturan pakai sama";
                String caraPemberian = "Oral";
                if (nama.toLowerCase().contains("inj") || nama.toLowerCase().contains("infus") || nama.toLowerCase().contains("ivfd")) {
                    caraPemberian = "Injeksi/IV";
                }
                tabModeObat.addRow(new Object[]{
                    kode != null ? kode : "",
                    nama,
                    "",
                    (aturan != null && !"-".equals(aturan)) ? aturan : "",
                    caraPemberian,
                    tindakLanjut,
                    "-"
                });
                updateBadgeObat();
            });
            panelQuickChips.add(chip);
            count++;
            if (count >= 6) break;
        }
        panelQuickChips.revalidate();
        panelQuickChips.repaint();
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "PS";
        String clean = name.trim().replaceAll("(?i)^(tn\\.|ny\\.|nn\\.|an\\.|by\\.)\\s*", "");
        String[] words = clean.split("\\s+");
        if (words.length == 0 || words[0].isEmpty()) return "PS";
        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        } else {
            return ("" + words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
        }
    }

    public void isCek() {
        boolean bolehRekon = akses.getrekonsiliasi_obat();
        if (panelFooterInput != null) {
            panelFooterInput.getComponent(0).setEnabled(bolehRekon);
        }
        if (akses.getjml2() >= 1) {
            KdPetugas.setEditable(false);
            KdPetugas.setText(akses.getkode());
            NmPetugas.setText(Sequel.CariPetugas(KdPetugas.getText()));
            if (NmPetugas.getText().equals("")) {
                KdPetugas.setText("");
                JOptionPane.showMessageDialog(null, "User login bukan petugas farmasi...!!");
            }
        }
    }

    public void setModeInput(ModeInput mode, String noRekon) {
        this.currentModeInput = mode;
        this.noRekonSedangDiedit = (mode == ModeInput.MODE_EDIT) ? noRekon : "";
        if (mode == ModeInput.MODE_EDIT) {
            btnSimpan.setText(" Simpan Perubahan / Update");
            btnSimpan.setBackground(BLUE_OCEAN);
            btnBaru.setText(" Batal Edit");
            btnBaru.setIcon(IconStyle.muat("trash.svg", 15, new Color(225, 29, 72)));
            chkLangsungTTE.setVisible(false);
            chkLangsungTTE.setSelected(false);
            NoRekonsiliasi.setText(noRekon);
        } else {
            btnSimpan.setText(" Simpan");
            btnSimpan.setBackground(TEAL_MAIN);
            btnBaru.setText(" Reset / Baru");
            btnBaru.setIcon(IconStyle.muat("refresh.svg", 15, new Color(71, 85, 105)));
            chkLangsungTTE.setVisible(true);
            chkLangsungTTE.setSelected(false);
        }
    }

    private void batalEditAtauBaru() {
        if (currentModeInput == ModeInput.MODE_EDIT) {
            int konfirm = JOptionPane.showConfirmDialog(
                this,
                "Batalkan proses edit data Rekonsiliasi Obat (" + noRekonSedangDiedit + ")?\nPerubahan yang belum disimpan akan dibatalkan.",
                "Konfirmasi Batal Edit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (konfirm != JOptionPane.YES_OPTION) return;
        }
        emptTeks();
    }

    public void emptTeks() {
        setModeInput(ModeInput.MODE_INPUT_BARU, "");
        DTPTgl.setDate(new Date());
        tabModeObat.setRowCount(0);
        rbAdmisi.setSelected(true);
        updateVisibilityPenggunaanObat();
        updateModeSidebarSaat();
        rbPakaiYa.setSelected(true);
        rbSumberRM.setSelected(true);
        KetSumberLain.setText("");
        KetSumberLain.setEnabled(false);
        if (txtAlergiObat != null) txtAlergiObat.setText("-");
        if (txtManifestasiAlergi != null) txtManifestasiAlergi.setText("-");
        if (cmbDampakAlergi != null) cmbDampakAlergi.setSelectedIndex(0);
        tbObat.setEnabled(true);
        btnTambahInline.setEnabled(true);
        btnPilihMasterObat.setEnabled(true);
        btnHapusBarisObat.setEnabled(true);
        if (btnSalinRekonSebelumnya != null) btnSalinRekonSebelumnya.setEnabled(true);
        updateBadgeObat();
        autoNomorRekonsiliasi();
    }

    private void autoNomorRekonsiliasi() {
        if (currentModeInput == ModeInput.MODE_EDIT && noRekonSedangDiedit != null && !noRekonSedangDiedit.trim().isEmpty()) {
            NoRekonsiliasi.setText(noRekonSedangDiedit);
            return;
        }
        String tglYmd = Valid.SetTgl(DTPTgl.getSelectedItem() + "");
        String tgl = tglYmd.replace("-", "");
        Valid.autoNomer3(
            "SELECT IFNULL(MAX(CONVERT(RIGHT(no_rekonsiliasi,4),signed)),0) FROM rekonsiliasi_obat WHERE tanggal_wawancara LIKE '"
            + tglYmd + "%'",
            "RO" + tgl, 4, NoRekonsiliasi
        );
    }



    // =========================================================================
    // OPERASI DATABASE: SIMPAN, KONFIRMASI, EDIT, HAPUS, CETAK
    // =========================================================================
    private void simpanRekonsiliasi() {
        if (TNoRw.getText().trim().isEmpty() || TNoRw.getText().trim().equals("-")) {
            JOptionPane.showMessageDialog(this, "Silakan pilih pasien terlebih dahulu!");
            return;
        }
        if (KdPetugas.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih Apoteker / Petugas Farmasi terlebih dahulu!");
            return;
        }
        boolean isAdmisi = rbAdmisi.isSelected();
        if (isAdmisi) {
            if (rbPakaiYa.isSelected() && tabModeObat.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tambahkan minimal 1 baris obat, atau pilih 'Tidak Menggunakan Obat Sebelum Admisi'!");
                return;
            }
        } else {
            if (tabModeObat.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tambahkan minimal 1 baris obat yang direkonsiliasi!");
                return;
            }
        }

        if (tbObat.isEditing()) {
            tbObat.getCellEditor().stopCellEditing();
        }

        String noRekon = (currentModeInput == ModeInput.MODE_EDIT && noRekonSedangDiedit != null && !noRekonSedangDiedit.trim().isEmpty())
                ? noRekonSedangDiedit : NoRekonsiliasi.getText().trim();
        String noRawat = TNoRw.getText().trim();
        String tglJam = Valid.SetTgl(DTPTgl.getSelectedItem() + "") + " "
                + new SimpleDateFormat("HH:mm:ss").format(new Date());
        String saat = isAdmisi ? "Admisi" : (rbTransfer.isSelected() ? "Transfer Antar Ruang" : "Pulang");
        String alergi = (txtAlergiObat != null && !txtAlergiObat.getText().trim().isEmpty()) ? txtAlergiObat.getText().trim() : "-";
        String manifestasi = (txtManifestasiAlergi != null && !txtManifestasiAlergi.getText().trim().isEmpty()) ? txtManifestasiAlergi.getText().trim() : "-";
        String dampak = (cmbDampakAlergi != null && cmbDampakAlergi.getSelectedItem() != null) ? cmbDampakAlergi.getSelectedItem().toString() : "-";
        String nip = KdPetugas.getText().trim();
        String penggunaanObat = isAdmisi ? (rbPakaiYa.isSelected() ? "Ya" : "Tidak") : "Tidak";
        String sumberInfo = rbSumberRM.isSelected() ? "Status Pasien di Rekam Medis" :
                (rbSumberPasien.isSelected() ? "Pasien/Keluarga Pasien" : "Lain-lain");
        String sumberLain = rbSumberLain.isSelected() ? KetSumberLain.getText().trim() : "-";

        Sequel.AutoComitFalse();
        boolean sukses = true;

        if (currentModeInput == ModeInput.MODE_EDIT) {
            // MODE EDIT: UPDATE record rekonsiliasi_obat yang lama (tidak membuat record baru)
            if (Sequel.mengedittf2("rekonsiliasi_obat", "no_rekonsiliasi=?",
                "no_rawat=?,tanggal_wawancara=?,rekonsiliasi_obat_saat=?,alergi_obat=?,manifestasi_alergi=?,dampak_alergi=?,nip=?,penggunaan_obat_sebelum_admisi=?,sumber_informasi=?,sumber_informasi_lain=?",
                11, new String[]{
                    noRawat, tglJam, saat, alergi, manifestasi, dampak, nip, penggunaanObat, sumberInfo, sumberLain, noRekon
                })) {

                // Refresh detail obat: hapus detail lama milik noRekon ini lalu simpan detail baru
                Sequel.meghapus("rekonsiliasi_obat_detail_obat", "no_rekonsiliasi", noRekon);
                boolean simpanDetail = isAdmisi ? rbPakaiYa.isSelected() : true;
                if (simpanDetail) {
                    for (int i = 0; i < tabModeObat.getRowCount(); i++) {
                        String namaObat = String.valueOf(tabModeObat.getValueAt(i, 1)).trim();
                        if (!namaObat.isEmpty()) {
                            String dosis = String.valueOf(tabModeObat.getValueAt(i, 2)).trim();
                            String frek = String.valueOf(tabModeObat.getValueAt(i, 3)).trim();
                            String cara = String.valueOf(tabModeObat.getValueAt(i, 4)).trim();
                            String tindak = String.valueOf(tabModeObat.getValueAt(i, 5)).trim();
                            String ubah = String.valueOf(tabModeObat.getValueAt(i, 6)).trim();

                            if (!Sequel.menyimpantf2("rekonsiliasi_obat_detail_obat", "?,?,?,?,?,?,?,?", "Detail Obat", 8, new String[]{
                                noRekon,
                                namaObat,
                                (dosis.isEmpty() ? "-" : dosis),
                                (frek.isEmpty() ? "-" : frek),
                                (cara.isEmpty() ? "Oral" : cara),
                                "-", // waktu_pemberian_terakhir default "-"
                                (tindak.isEmpty() ? "Lanjut aturan pakai sama" : tindak),
                                (ubah.isEmpty() ? "-" : ubah)
                            })) {
                                sukses = false;
                                break;
                            }
                        }
                    }
                }
            } else {
                sukses = false;
            }
        } else {
            // MODE INPUT BARU: INSERT record baru
            if (Sequel.menyimpantf2("rekonsiliasi_obat", "?,?,?,?,?,?,?,?,?,?,?", "No.Rekonsiliasi", 11, new String[]{
                noRekon, noRawat, tglJam, saat, alergi, manifestasi, dampak, nip, penggunaanObat, sumberInfo, sumberLain
            })) {

                boolean simpanDetail = isAdmisi ? rbPakaiYa.isSelected() : true;
                if (simpanDetail) {
                    for (int i = 0; i < tabModeObat.getRowCount(); i++) {
                        String namaObat = String.valueOf(tabModeObat.getValueAt(i, 1)).trim();
                        if (!namaObat.isEmpty()) {
                            String dosis = String.valueOf(tabModeObat.getValueAt(i, 2)).trim();
                            String frek = String.valueOf(tabModeObat.getValueAt(i, 3)).trim();
                            String cara = String.valueOf(tabModeObat.getValueAt(i, 4)).trim();
                            String tindak = String.valueOf(tabModeObat.getValueAt(i, 5)).trim();
                            String ubah = String.valueOf(tabModeObat.getValueAt(i, 6)).trim();

                            if (!Sequel.menyimpantf2("rekonsiliasi_obat_detail_obat", "?,?,?,?,?,?,?,?", "Detail Obat", 8, new String[]{
                                noRekon,
                                namaObat,
                                (dosis.isEmpty() ? "-" : dosis),
                                (frek.isEmpty() ? "-" : frek),
                                (cara.isEmpty() ? "Oral" : cara),
                                "-", // waktu_pemberian_terakhir default "-"
                                (tindak.isEmpty() ? "Lanjut aturan pakai sama" : tindak),
                                (ubah.isEmpty() ? "-" : ubah)
                            })) {
                                sukses = false;
                                break;
                            }
                        }
                    }
                }

                // Jika checkbox 'Langsung Validasi & Bubuhkan TTE' dicentang saat MODE_INPUT_BARU
                if (sukses && chkLangsungTTE.isSelected()) {
                    String waktuSekarang = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    String nipApoteker = (akses.getkode() == null || akses.getkode().trim().isEmpty() || akses.getkode().equals("Admin Utama"))
                            ? nip : akses.getkode();
                    Sequel.meghapus("rekonsiliasi_obat_konfirmasi", "no_rekonsiliasi", noRekon);
                    if (!Sequel.menyimpantf("rekonsiliasi_obat_konfirmasi", "?,?,?,?,?", "Konfirmasi Apoteker", 5, new String[]{
                        noRekon, waktuSekarang, waktuSekarang, nipApoteker, waktuSekarang
                    })) {
                        sukses = false;
                    }
                }
            } else {
                sukses = false;
            }
        }

        if (sukses) {
            Sequel.Commit();
            String msg = (currentModeInput == ModeInput.MODE_EDIT)
                ? "Perubahan Data Rekonsiliasi Obat (" + noRekon + ") berhasil disimpan!"
                : (chkLangsungTTE.isSelected()
                    ? "Data Rekonsiliasi Obat (" + noRekon + ") berhasil disimpan dan langsung divalidasi (TTE)!"
                    : "Data Rekonsiliasi Obat (" + noRekon + ") berhasil disimpan sebagai Draf!");
            JOptionPane.showMessageDialog(this, msg);
            emptTeks();
            tabPaneUtama.setSelectedIndex(1);
            tampilDataRekonsiliasi();
            pilihBarisRekon(noRekon);
        } else {
            Sequel.RollBack();
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data Rekonsiliasi Obat! Periksa kelengkapan data.");
        }
        Sequel.AutoComitTrue();
    }

    /**
     * Mengatur state tombol-tombol aksi di Tab 2 berdasarkan baris dan status data yang dipilih
     */
    private void updateStateButtonsTab2(int selectedRow) {
        if (selectedRow == -1 || selectedRow >= tbDataRekon.getRowCount()) {
            btnValidasiTTE.setEnabled(false);
            btnValidasiTTE.setText(" Validasi & Bubuhkan TTE");
            btnValidasiTTE.setBackground(GREEN_EMERALD);
            btnValidasiTTE.setForeground(Color.WHITE);
            btnGantiData.setEnabled(false);
            btnHapusRekon.setEnabled(false);
            btnCetak.setEnabled(false);
            if (btnPopupPreview != null) btnPopupPreview.setEnabled(false);
            return;
        }

        String status = String.valueOf(tbDataRekon.getValueAt(selectedRow, 8));
        boolean isTervalidasi = status.contains("Tervalidasi");

        btnCetak.setEnabled(true);
        if (btnPopupPreview != null) btnPopupPreview.setEnabled(true);

        if (isTervalidasi) {
            btnValidasiTTE.setEnabled(true);
            btnValidasiTTE.setText(" Batal Validasi / Tarik TTE");
            btnValidasiTTE.setBackground(new Color(217, 119, 6)); // Amber / Orange
            btnValidasiTTE.setForeground(Color.WHITE);

            // Kunci Edit dan Hapus demi integritas data rekam medis ber-TTE
            btnGantiData.setEnabled(false);
            btnHapusRekon.setEnabled(false);
        } else {
            btnValidasiTTE.setEnabled(true);
            btnValidasiTTE.setText(" Validasi & Bubuhkan TTE");
            btnValidasiTTE.setBackground(GREEN_EMERALD);
            btnValidasiTTE.setForeground(Color.WHITE);

            // Buka Edit dan Hapus untuk Draf
            btnGantiData.setEnabled(true);
            btnHapusRekon.setEnabled(true);
        }
    }

    /**
     * Logika Validasi & Bubuhkan TTE atau Pembatalan Validasi (Tarik TTE)
     */
    private void validasiAtauTarikTTE() {
        int r = tbDataRekon.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih baris data rekonsiliasi yang akan diproses!");
            return;
        }

        String noRekon = tbDataRekon.getValueAt(r, 0).toString();
        String status = String.valueOf(tbDataRekon.getValueAt(r, 8));
        boolean isTervalidasi = status.contains("Tervalidasi");

        if (!isTervalidasi) {
            // Validasi & Bubuhkan TTE
            int konfirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin memvalidasi dan membubuhkan TTE pada dokumen ini?\n"
                + "Data yang sudah ber-TTE akan dikunci demi keamanan dan integritas rekam medis.",
                "Konfirmasi Validasi & TTE",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (konfirm == JOptionPane.YES_OPTION) {
                String waktuSekarang = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String nipApoteker = (akses.getkode() == null || akses.getkode().trim().isEmpty() || akses.getkode().equals("Admin Utama"))
                        ? tbDataRekon.getValueAt(r, 6).toString()
                        : akses.getkode();

                Sequel.meghapus("rekonsiliasi_obat_konfirmasi", "no_rekonsiliasi", noRekon);
                if (Sequel.menyimpantf("rekonsiliasi_obat_konfirmasi", "?,?,?,?,?", "Konfirmasi Apoteker", 5, new String[]{
                    noRekon, waktuSekarang, waktuSekarang, nipApoteker, waktuSekarang
                })) {
                    JOptionPane.showMessageDialog(this, "Dokumen rekonsiliasi (" + noRekon + ") berhasil divalidasi dan TTE telah dibubuhkan!");
                    tampilDataRekonsiliasi();
                    pilihBarisRekon(noRekon);
                }
            }
        } else {
            // Batal Validasi / Tarik TTE
            int konfirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin MEMBATALKAN validasi dan MENARIK TTE dari dokumen ini?\n"
                + "Status dokumen akan kembali menjadi Draf dan kunci edit/hapus akan dibuka kembali.",
                "Konfirmasi Batal Validasi / Tarik TTE",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (konfirm == JOptionPane.YES_OPTION) {
                Sequel.meghapus("rekonsiliasi_obat_konfirmasi", "no_rekonsiliasi", noRekon);
                JOptionPane.showMessageDialog(this, "TTE berhasil ditarik. Dokumen (" + noRekon + ") kembali menjadi Draf (Belum Validasi).");
                tampilDataRekonsiliasi();
                pilihBarisRekon(noRekon);
            }
        }
    }

    /**
     * Memuat data rekonsiliasi yang dipilih ke Tab 1 (Input) untuk diedit/dikoreksi
     */
    private void gantiDataRekonsiliasi() {
        int r = tbDataRekon.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu baris data rekonsiliasi yang akan diedit!");
            return;
        }

        String status = String.valueOf(tbDataRekon.getValueAt(r, 8));
        if (status.contains("Tervalidasi")) {
            JOptionPane.showMessageDialog(this,
                "Data Rekonsiliasi ini sudah berstatus TERVALIDASI (TTE) dan dikunci!\n"
                + "Silakan klik 'Batal Validasi / Tarik TTE' terlebih dahulu jika ingin mengedit.",
                "Peringatan - Data Terkunci", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String noRekon = tbDataRekon.getValueAt(r, 0).toString();
        String noRawat = "";
        String tglWawancara = "";
        String saat = "";
        String pakai = "";
        String sumber = "";
        String sumberLain = "";
        String nip = "";
        String alergi = "";
        String manifestasi = "";
        String dampak = "";

        // Gunakan PreparedStatement dan ResultSet lokal agar tidak ditimpa/ditutup oleh pemanggilan setNoRm()
        PreparedStatement psRekon = null;
        ResultSet rsRekon = null;
        try {
            psRekon = koneksi.prepareStatement("SELECT * FROM rekonsiliasi_obat WHERE no_rekonsiliasi = ?");
            psRekon.setString(1, noRekon);
            rsRekon = psRekon.executeQuery();
            if (rsRekon.next()) {
                noRawat = rsRekon.getString("no_rawat");
                tglWawancara = rsRekon.getString("tanggal_wawancara");
                saat = rsRekon.getString("rekonsiliasi_obat_saat");
                pakai = rsRekon.getString("penggunaan_obat_sebelum_admisi");
                sumber = rsRekon.getString("sumber_informasi");
                sumberLain = rsRekon.getString("sumber_informasi_lain");
                nip = rsRekon.getString("nip");
                alergi = rsRekon.getString("alergi_obat");
                manifestasi = rsRekon.getString("manifestasi_alergi");
                dampak = rsRekon.getString("dampak_alergi");
            }
        } catch (Exception e) {
            System.out.println("Notif Ganti Data (Query Header): " + e);
        } finally {
            try { if (rsRekon != null) rsRekon.close(); if (psRekon != null) psRekon.close(); } catch (Exception e) {}
        }

        if (noRawat == null || noRawat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Data rekonsiliasi tidak ditemukan di database!");
            return;
        }

        // 1. Muat data identitas pasien dan contekan catatan dokter TERLEBIH DAHULU
        setNoRm(noRawat);

        // Pulihkan nilai alergi dari data rekonsiliasi yang tersimpan
        if (txtAlergiObat != null) txtAlergiObat.setText((alergi != null && !alergi.trim().isEmpty()) ? alergi : "-");
        if (txtManifestasiAlergi != null) txtManifestasiAlergi.setText((manifestasi != null && !manifestasi.trim().isEmpty()) ? manifestasi : "-");
        if (cmbDampakAlergi != null) cmbDampakAlergi.setSelectedItem((dampak != null && !dampak.trim().isEmpty()) ? dampak : "-");

        // 2. Set mode form ke MODE_EDIT dan kunci nomor rekonsiliasi yang sedang diedit
        setModeInput(ModeInput.MODE_EDIT, noRekon);
        NoRekonsiliasi.setText(noRekon);

        // 3. Waktu wawancara
        try {
            Date dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(tglWawancara);
            DTPTgl.setDate(dt);
        } catch (Exception ex) {}

        // Pastikan kembali nomor rekonsiliasi tidak tertimpa setelah setDate
        NoRekonsiliasi.setText(noRekon);

        // 4. Tahap Rekonsiliasi Saat
        if ("Transfer Antar Ruang".equalsIgnoreCase(saat)) {
            rbTransfer.setSelected(true);
        } else if ("Pulang".equalsIgnoreCase(saat)) {
            rbPulang.setSelected(true);
        } else {
            rbAdmisi.setSelected(true);
        }
        updateVisibilityPenggunaanObat();
        updateModeSidebarSaat();

        // 5. Penggunaan Obat Sebelum Admisi
        if ("Tidak".equalsIgnoreCase(pakai)) {
            rbPakaiTidak.setSelected(true);
            tbObat.setEnabled(false);
            btnTambahInline.setEnabled(false);
            btnPilihMasterObat.setEnabled(false);
            btnHapusBarisObat.setEnabled(false);
            if (btnSalinRekonSebelumnya != null) btnSalinRekonSebelumnya.setEnabled(false);
        } else {
            rbPakaiYa.setSelected(true);
            tbObat.setEnabled(true);
            btnTambahInline.setEnabled(true);
            btnPilihMasterObat.setEnabled(true);
            btnHapusBarisObat.setEnabled(true);
            if (btnSalinRekonSebelumnya != null) btnSalinRekonSebelumnya.setEnabled(true);
        }

        // 6. Sumber Informasi
        if ("Pasien/Keluarga Pasien".equalsIgnoreCase(sumber)) {
            rbSumberPasien.setSelected(true);
            KetSumberLain.setText("");
            KetSumberLain.setEnabled(false);
        } else if ("Lain-lain".equalsIgnoreCase(sumber)) {
            rbSumberLain.setSelected(true);
            KetSumberLain.setEnabled(true);
            KetSumberLain.setText(sumberLain != null && !sumberLain.equals("-") ? sumberLain : "");
        } else {
            rbSumberRM.setSelected(true);
            KetSumberLain.setText("");
            KetSumberLain.setEnabled(false);
        }

        // 7. Apoteker / Petugas Farmasi
        KdPetugas.setText(nip != null ? nip : "");
        NmPetugas.setText(Sequel.CariPetugas(KdPetugas.getText()));

        // 8. Muat Detail Daftar Obat
        tabModeObat.setRowCount(0);
        PreparedStatement psDet = null;
        ResultSet rsDet = null;
        try {
            psDet = koneksi.prepareStatement("SELECT * FROM rekonsiliasi_obat_detail_obat WHERE no_rekonsiliasi = ?");
            psDet.setString(1, noRekon);
            rsDet = psDet.executeQuery();
            while (rsDet.next()) {
                tabModeObat.addRow(new Object[]{
                    "",
                    rsDet.getString("nama_obat"),
                    rsDet.getString("dosis_obat"),
                    rsDet.getString("frekuensi"),
                    rsDet.getString("cara_pemberian"),
                    rsDet.getString("tindak_lanjut"),
                    rsDet.getString("perubahan_aturan_pakai")
                });
            }
        } catch (Exception e) {
            System.out.println("Notif Ganti Data (Query Detail): " + e);
        } finally {
            try { if (rsDet != null) rsDet.close(); if (psDet != null) psDet.close(); } catch (Exception e) {}
        }
        updateBadgeObat();

        // 9. Berpindah ke Tab 1 (Input Rekonsiliasi Obat)
        SwingUtilities.invokeLater(() -> {
            tabPaneUtama.setSelectedIndex(0);
        });
    }

    private void hapusRekonsiliasi() {
        int r = tbDataRekon.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu baris data rekonsiliasi yang akan dihapus!");
            return;
        }

        String status = String.valueOf(tbDataRekon.getValueAt(r, 8));
        if (status.contains("Tervalidasi")) {
            JOptionPane.showMessageDialog(this,
                "Data Rekonsiliasi ini sudah berstatus TERVALIDASI (TTE) dan dikunci!\n"
                + "Data yang sudah ber-TTE tidak boleh dihapus demi integritas rekam medis.",
                "Peringatan - Data Terkunci", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String noRekon = tbDataRekon.getValueAt(r, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus data Rekonsiliasi " + noRekon + "?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Sequel.AutoComitFalse();
            Sequel.meghapus("rekonsiliasi_obat_detail_obat", "no_rekonsiliasi", noRekon);
            Sequel.meghapus("rekonsiliasi_obat_konfirmasi", "no_rekonsiliasi", noRekon);
            Sequel.meghapus("rekonsiliasi_obat", "no_rekonsiliasi", noRekon);
            Sequel.Commit();
            Sequel.AutoComitTrue();
            JOptionPane.showMessageDialog(this, "Data rekonsiliasi berhasil dihapus!");
            tampilDataRekonsiliasi();
            LoadHTML.setText("");
        }
    }

    private void cetakRM0306A() {
        String[] opsi = {"Cetak Lembar ke Printer", "Simpan / Buka File PDF"};
        String pilih = (String) JOptionPane.showInputDialog(
            this, "Silakan pilih tindakan cetak Rekonsiliasi Obat (RM 03-06A):", "Cetak Dokumen",
            JOptionPane.QUESTION_MESSAGE, null, opsi, opsi[0]
        );
        if (pilih == null) return;
        if (pilih.contains("PDF")) {
            cetakRM0306A("PDF");
        } else {
            cetakRM0306A("DirectPrint");
        }
    }

    private void cetakRM0306A(String modeCetak) {
        int r = tbDataRekon.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu baris data rekonsiliasi yang akan dicetak!");
            return;
        }
        String noRekon = tbDataRekon.getValueAt(r, 0).toString();

        try {
            ps = koneksi.prepareStatement(
                "SELECT r.*, p.no_rkm_medis, p.nm_pasien, p.tgl_lahir, IF(p.jk='L','Laki-Laki','Perempuan') AS jk, pt.nama AS nm_apoteker, "
                + "k.dikonfirmasi_apoteker "
                + "FROM rekonsiliasi_obat r "
                + "INNER JOIN reg_periksa rp ON r.no_rawat = rp.no_rawat "
                + "INNER JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis "
                + "INNER JOIN petugas pt ON r.nip = pt.nip "
                + "LEFT JOIN rekonsiliasi_obat_konfirmasi k ON r.no_rekonsiliasi = k.no_rekonsiliasi "
                + "WHERE r.no_rekonsiliasi = ?"
            );
            ps.setString(1, noRekon);
            rs = ps.executeQuery();
            if (rs.next()) {
                String nipApoteker = rs.getString("nip");
                String namaApoteker = rs.getString("nm_apoteker");
                String tglKonfirm = rs.getString("dikonfirmasi_apoteker");
                String fingerHash = Sequel.cariIsi(
                    "SELECT SHA1(sidikjari.sidikjari) FROM sidikjari "
                    + "INNER JOIN pegawai ON pegawai.id=sidikjari.id WHERE pegawai.nik=?", nipApoteker
                );

                Map<String, Object> param = new HashMap<>();
                param.put("namars", akses.getnamars());
                param.put("alamatrs", akses.getalamatrs());
                param.put("kotars", akses.getkabupatenrs());
                param.put("propinsirs", akses.getpropinsirs());
                param.put("kontakrs", akses.getkontakrs());
                param.put("emailrs", akses.getemailrs());
                param.put("logo", Sequel.cariGambar("SELECT setting.logo FROM setting"));
                param.put("pNoRM", rs.getString("no_rkm_medis"));
                param.put("pNamaPasien", rs.getString("nm_pasien"));
                param.put("pJK", rs.getString("jk"));
                param.put("pTglLahir", rs.getString("tgl_lahir"));
                param.put("pNoRawat", rs.getString("no_rawat"));
                param.put("pNoRekon", noRekon);
                param.put("pPenggunaanObatSebelumAdmisi", rs.getString("penggunaan_obat_sebelum_admisi"));
                param.put("pRekonsiliasiSaat", rs.getString("rekonsiliasi_obat_saat"));
                param.put("pTanggalWawancara", rs.getString("tanggal_wawancara"));
                param.put("pSumberInformasi", rs.getString("sumber_informasi"));
                param.put("pSumberInformasiLain", rs.getString("sumber_informasi_lain"));
                param.put("pAlergiObat", rs.getString("alergi_obat"));
                param.put("pManifestasiAlergi", rs.getString("manifestasi_alergi"));
                param.put("pDampakAlergi", rs.getString("dampak_alergi"));
                param.put("pApotekerNama", namaApoteker);
                param.put("pApotekerNip", nipApoteker);
                param.put("pTglKonfirm", (tglKonfirm != null && tglKonfirm.length() >= 10 ? tglKonfirm.substring(0, 10) : ""));

                if (tglKonfirm != null && !tglKonfirm.isEmpty()) {
                    param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten " + akses.getkabupatenrs()
                            + "\nDokumen: Rekonsiliasi Obat (RM 03-06A)"
                            + "\nNo. Rekonsiliasi: " + noRekon
                            + "\nPasien: " + rs.getString("nm_pasien") + " (RM: " + rs.getString("no_rkm_medis") + ")"
                            + "\nDitandatangani secara elektronik oleh:\n" + namaApoteker + " (NIP: " + nipApoteker + ")"
                            + "\nID TTE: " + ((fingerHash == null || fingerHash.isEmpty()) ? nipApoteker : fingerHash)
                            + "\nWaktu Validasi: " + tglKonfirm);
                } else {
                    param.put("finger", "");
                }

                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                String qryCetak = "SELECT nama_obat, dosis_obat, frekuensi, cara_pemberian, tindak_lanjut, perubahan_aturan_pakai "
                        + "FROM rekonsiliasi_obat_detail_obat WHERE no_rekonsiliasi='" + noRekon + "'";

                if ("PDF".equalsIgnoreCase(modeCetak)) {
                    Valid.MyReportqrypdf("rptRekonsiliasiObatForm.jasper", "report", "::[ Form Rekonsiliasi Obat - RM 03-06A ]::", qryCetak, param);
                } else {
                    Valid.MyReportqry("rptRekonsiliasiObatForm.jasper", "report", "::[ Form Rekonsiliasi Obat - RM 03-06A ]::", qryCetak, param);
                }
                this.setCursor(Cursor.getDefaultCursor());
            }
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            System.out.println("Notif Cetak Jasper: " + e);
            // Fallback cetak HTML langsung jika jasper tidak tersedia
            try {
                LoadHTML.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal mencetak: " + ex.getMessage());
            }
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } catch (Exception e) {}
        }
    }

    private void pasangMenuCetakOpsional(JButton btn) {
        JPopupMenu menuCetak = new JPopupMenu();

        JMenuItem itemLembar = new JMenuItem(" Cetak Lembar ke Printer");
        itemLembar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        itemLembar.addActionListener(e -> cetakRM0306A("DirectPrint"));

        JMenuItem itemPDF = new JMenuItem(" Ekspor / Simpan ke PDF");
        itemPDF.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        itemPDF.addActionListener(e -> cetakRM0306A("PDF"));

        menuCetak.add(itemLembar);
        menuCetak.add(itemPDF);

        btn.setComponentPopupMenu(menuCetak);
    }

    private void tampilDataRekonsiliasi() {
        Valid.tabelKosong(tabModeDataRekon);
        try {
            String tgl1 = Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00";
            String tgl2 = Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59";
            String kw = "%" + TCari.getText().trim() + "%";

            String sql = "SELECT r.no_rekonsiliasi, r.no_rawat, p.no_rkm_medis, p.nm_pasien, r.tanggal_wawancara, "
                    + "r.rekonsiliasi_obat_saat, r.nip, pt.nama, "
                    + "IF(k.no_rekonsiliasi IS NULL, 'Belum Validasi (Draf)', 'Tervalidasi (TTE)') AS status_konfirm "
                    + "FROM rekonsiliasi_obat r "
                    + "INNER JOIN reg_periksa rp ON r.no_rawat = rp.no_rawat "
                    + "INNER JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis "
                    + "INNER JOIN petugas pt ON r.nip = pt.nip "
                    + "LEFT JOIN rekonsiliasi_obat_konfirmasi k ON r.no_rekonsiliasi = k.no_rekonsiliasi "
                    + "WHERE r.tanggal_wawancara BETWEEN ? AND ? "
                    + "AND (r.no_rekonsiliasi LIKE ? OR r.no_rawat LIKE ? OR p.no_rkm_medis LIKE ? OR p.nm_pasien LIKE ? OR pt.nama LIKE ?) "
                    + "ORDER BY r.tanggal_wawancara DESC";
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, tgl1);
            ps.setString(2, tgl2);
            ps.setString(3, kw);
            ps.setString(4, kw);
            ps.setString(5, kw);
            ps.setString(6, kw);
            ps.setString(7, kw);
            rs = ps.executeQuery();
            while (rs.next()) {
                tabModeDataRekon.addRow(new Object[]{
                    rs.getString("no_rekonsiliasi"),
                    rs.getString("no_rawat"),
                    rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"),
                    rs.getString("tanggal_wawancara"),
                    rs.getString("rekonsiliasi_obat_saat"),
                    rs.getString("nip"),
                    rs.getString("nama"),
                    rs.getString("status_konfirm")
                });
            }
            LCount.setText("Record : " + tabModeDataRekon.getRowCount());
        } catch (Exception e) {
            System.out.println("Notif Tampil Data: " + e);
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); } catch (Exception e) {}
            updateStateButtonsTab2(tbDataRekon.getSelectedRow());
        }
    }

    private void pilihBarisRekon(String noRekon) {
        if (noRekon == null || noRekon.isEmpty()) return;
        for (int i = 0; i < tabModeDataRekon.getRowCount(); i++) {
            if (noRekon.equals(String.valueOf(tabModeDataRekon.getValueAt(i, 0)))) {
                tbDataRekon.setRowSelectionInterval(i, i);
                tbDataRekon.scrollRectToVisible(tbDataRekon.getCellRect(i, 0, true));
                updateStateButtonsTab2(i);
                muatPreviewHTML(noRekon);
                break;
            }
        }
    }

    /**
     * Membuat file gambar QR Code untuk keperluan stempel digital TTE pada preview dokumen fisik RM 03-06A
     */
    private String generateQrCodeTTE(String content, String noRekon) {
        try {
            File dir = new File("temp");
            if (!dir.exists()) dir.mkdirs();
            String safeNo = noRekon.replaceAll("[^a-zA-Z0-9]", "_");
            File file = new File(dir, "qr_rekon_" + safeNo + ".png");
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 140, 140);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", file.toPath());
            return file.toURI().toURL().toString();
        } catch (Exception e) {
            System.out.println("Notif Generate QR TTE: " + e);
            return null;
        }
    }

    private void muatPreviewHTML(String noRekon) {
        String content = generateHTMLPreviewContent(noRekon);
        LoadHTML.setText(content);
        LoadHTML.setCaretPosition(0);
        SwingUtilities.invokeLater(() -> {
            LoadHTML.setCaretPosition(0);
            if (scrollPreviewHTML != null) {
                scrollPreviewHTML.getVerticalScrollBar().setValue(0);
            }
        });
    }

    /**
     * Membuka Jendela Modal Dialog Pratinjau Layar Penuh (Clean Document Viewer)
     * Tidak langsung mencetak, melainkan menampilkan dokumen secara utuh,
     * dan menyediakan tombol aksi langsung [Cetak Lembar] dan [Simpan PDF].
     */
    private void bukaDialogPreviewFullscreen(String noRekon) {
        if (noRekon == null || noRekon.isEmpty()) {
            int r = tbDataRekon.getSelectedRow();
            if (r != -1) {
                noRekon = tbDataRekon.getValueAt(r, 0).toString();
            }
        }
        if (noRekon == null || noRekon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih data rekonsiliasi terlebih dahulu!");
            return;
        }

        final String targetRekon = noRekon;
        String htmlDokumen = generateHTMLPreviewContent(targetRekon);
        if (htmlDokumen == null || htmlDokumen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gagal memuat pratinjau dokumen!");
            return;
        }

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parentWin, "::[ Pratinjau Dokumen Rekonsiliasi Obat (RM 03-06A) ]::", Dialog.ModalityType.APPLICATION_MODAL);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int dlgW = Math.min(1120, screen.width - 40);
        int dlgH = Math.min(860, screen.height - 60);
        dlg.setSize(dlgW, dlgH);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // Header Dialog Toolbar
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(new Color(15, 23, 42)); // Slate-900
        topBar.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel lblJudul = new JLabel(" PRATINJAU DOKUMEN REKONSILIASI OBAT (RM 03-06A) — NO. " + targetRekon);
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblJudul.setForeground(Color.WHITE);

        JPanel pnlAksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlAksi.setOpaque(false);

        JButton btnCetakLembar = new JButton(" Cetak Lembar ke Printer");
        btnCetakLembar.setIcon(IconStyle.muat("printer.svg", 14, Color.WHITE));
        btnCetakLembar.setBackground(GREEN_EMERALD);
        btnCetakLembar.setForeground(Color.WHITE);
        btnCetakLembar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCetakLembar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCetakLembar.setToolTipText("Cetak dokumen langsung ke printer fisik");
        btnCetakLembar.addActionListener(e -> cetakRM0306A("DirectPrint"));

        JButton btnSimpanPdf = new JButton(" Simpan PDF");
        btnSimpanPdf.setIcon(IconStyle.muat("save.svg", 14, Color.WHITE));
        btnSimpanPdf.setBackground(new Color(217, 119, 6)); // Amber
        btnSimpanPdf.setForeground(Color.WHITE);
        btnSimpanPdf.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSimpanPdf.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpanPdf.setToolTipText("Simpan / ekspor dokumen sebagai file PDF");
        btnSimpanPdf.addActionListener(e -> cetakRM0306A("PDF"));

        JButton btnTutup = new JButton(" Tutup (ESC)");
        btnTutup.setBackground(new Color(71, 85, 105)); // Slate
        btnTutup.setForeground(Color.WHITE);
        btnTutup.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTutup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTutup.setToolTipText("Tutup jendela pratinjau dokumen");
        btnTutup.addActionListener(e -> dlg.dispose());

        pnlAksi.add(btnCetakLembar);
        pnlAksi.add(btnSimpanPdf);
        pnlAksi.add(btnTutup);

        topBar.add(lblJudul, BorderLayout.WEST);
        topBar.add(pnlAksi, BorderLayout.EAST);

        // Center: Dokumen Viewer
        JEditorPane epDokumen = new JEditorPane();
        epDokumen.setContentType("text/html");
        epDokumen.setEditable(false);
        epDokumen.setBackground(new Color(241, 245, 249)); // Slate-100 canvas
        if (epDokumen.getCaret() instanceof javax.swing.text.DefaultCaret) {
            ((javax.swing.text.DefaultCaret) epDokumen.getCaret()).setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
        }
        epDokumen.setText(htmlDokumen);

        JScrollPane sp = new JScrollPane(epDokumen);
        sp.setBorder(null);

        dlg.add(topBar, BorderLayout.NORTH);
        dlg.add(sp, BorderLayout.CENTER);

        // Shortcut ESC untuk menutup dialog
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlg.dispose();
            }
        };
        dlg.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        dlg.getRootPane().getActionMap().put("ESCAPE", escapeAction);

        SwingUtilities.invokeLater(() -> {
            epDokumen.setCaretPosition(0);
            sp.getVerticalScrollBar().setValue(0);
        });

        dlg.setVisible(true);
    }

    /**
     * Menghasilkan struktur HTML pratinjau dokumen RM 03-06A
     * yang 100% selaras dengan cetakan fisik rptRekonsiliasiObatForm.jrxml
     */
    private String generateHTMLPreviewContent(String noRekon) {
        StringBuilder html = new StringBuilder();
        PreparedStatement psPrev = null;
        ResultSet rsPrev = null;
        PreparedStatement psDet = null;
        ResultSet rsDet = null;
        try {
            psPrev = koneksi.prepareStatement(
                "SELECT r.*, p.no_rkm_medis, p.nm_pasien, p.tgl_lahir, IF(p.jk='L','Laki-Laki','Perempuan') AS jk, pt.nama AS nm_apoteker, "
                + "k.diterima_farmasi, k.dikonfirmasi_apoteker, k.diserahkan_pasien "
                + "FROM rekonsiliasi_obat r "
                + "INNER JOIN reg_periksa rp ON r.no_rawat = rp.no_rawat "
                + "INNER JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis "
                + "INNER JOIN petugas pt ON r.nip = pt.nip "
                + "LEFT JOIN rekonsiliasi_obat_konfirmasi k ON r.no_rekonsiliasi = k.no_rekonsiliasi "
                + "WHERE r.no_rekonsiliasi = ?"
            );
            psPrev.setString(1, noRekon);
            rsPrev = psPrev.executeQuery();
            if (rsPrev.next()) {
                String tglKonfirm = rsPrev.getString("dikonfirmasi_apoteker");
                String nipApoteker = rsPrev.getString("nip");
                String nmApoteker = rsPrev.getString("nm_apoteker");
                String noRm = rsPrev.getString("no_rkm_medis");
                String nmPasien = rsPrev.getString("nm_pasien");

                String tglWwncRaw = rsPrev.getString("tanggal_wawancara");
                String tglWwncTampil = (tglWwncRaw != null && tglWwncRaw.length() >= 10) ? tglWwncRaw.substring(0, 10) : (tglWwncRaw != null ? tglWwncRaw : "-");

                String saat = rsPrev.getString("rekonsiliasi_obat_saat");
                boolean isAdmisi = "Admisi".equalsIgnoreCase(saat);
                String judulTahap = "REKONSILIASI OBAT " + (isAdmisi ? "SAAT ADMISI" : ("SAAT " + (saat != null ? saat.toUpperCase() : "")));

                html.append("<html><body style='font-family:sans-serif; font-size:11px; color:#1e293b; padding:12px; background:#ffffff;'>")
                    // Kop Instansi RSUD Demang Sepulau Raya
                    .append("<div align='center' style='border-bottom:2px solid #0f766e; padding-bottom:6px; margin-bottom:12px;'>")
                    .append("<b style='font-size:11px; color:#64748b;'>PEMERINTAH KABUPATEN LAMPUNG TENGAH</b><br>")
                    .append("<b style='font-size:14px; color:#0f766e;'>RSUD DEMANG SEPULAU RAYA</b><br>")
                    .append("<span style='font-size:9px; color:#64748b;'>Jl. Lintas Sumatera, No. 4.A, Terbanggi Agung, Gunung Sugih, Lampung Tengah Kode Pos 34161</span>")
                    .append("</div>")

                    // 1. JUDUL FORMULIR DI TENGAH (DI ATAS DATA PASIEN)
                    .append("<table width='100%' style='margin-bottom:8px;' cellpadding='0' cellspacing='0'><tr>")
                    .append("<td width='20%'>&nbsp;</td>")
                    .append("<td width='60%' align='center'><b style='font-size:13px; color:#0f172a; letter-spacing:1px;'>REKONSILIASI OBAT</b></td>")
                    .append("<td width='20%' align='right' valign='top'><b style='font-size:10px; color:#0f172a;'>RM 03-06A</b><br><span style='font-size:8px; color:#64748b;'>No. ").append(noRekon).append("</span></td>")
                    .append("</tr></table>")

                    // 2. DATA IDENTITAS PASIEN (DI BAWAH JUDUL)
                    .append("<table width='100%' style='margin-bottom:8px; font-size:10.5px; border:1px solid #cbd5e1; background:#f8fafc; padding:6px 10px;' cellpadding='2' cellspacing='0'>")
                    .append("<tr>")
                    .append("<td width='55%' valign='top'>")
                    .append("Nama Pasien : <b>").append(nmPasien).append(" (").append(rsPrev.getString("jk")).append(")</b><br>")
                    .append("No. RM : <b>").append(noRm).append("</b><br>")
                    .append("Tanggal Lahir : <b>").append(rsPrev.getString("tgl_lahir")).append("</b>")
                    .append("</td>")
                    .append("<td width='45%' valign='top'>")
                    .append("No. Rawat : <b>").append(rsPrev.getString("no_rawat")).append("</b><br>")
                    .append("Tanggal Wawancara : <b>").append(tglWwncTampil).append("</b><br>")
                    .append("Tahap Rekonsiliasi : <b style='color:#0f766e;'>").append(saat != null ? saat.toUpperCase() : "-").append("</b>")
                    .append("</td>")
                    .append("</tr></table>");

                // 3. KOTAK PENGGUNAAN OBAT SEBELUM ADMISI (KHUSUS ADMISI)
                if (isAdmisi) {
                    boolean pakaiObat = "Ya".equalsIgnoreCase(rsPrev.getString("penggunaan_obat_sebelum_admisi"));
                    html.append("<div style='background:#f8fafc; border:1px solid #cbd5e1; padding:6px 10px; margin-bottom:8px; font-size:10px;'>")
                        .append("<b>Penggunaan Obat Sebelum Admisi:</b> &nbsp;&nbsp;")
                        .append(pakaiObat ? "<b>[ &#10003; ] Ya, dengan rincian berikut</b> &nbsp;&nbsp;&nbsp;&nbsp; [ &nbsp; ] Tidak Menggunakan Obat Sebelum Admisi"
                                          : "[ &nbsp; ] Ya, dengan rincian berikut &nbsp;&nbsp;&nbsp;&nbsp; <b>[ &#10003; ] Tidak Menggunakan Obat Sebelum Admisi</b>")
                        .append("</div>");
                }

                // 4. TABEL DETAIL OBAT
                html.append("<table width='100%' border='1' cellspacing='0' cellpadding='5' style='border-collapse:collapse; border:1px solid #94a3b8; font-size:10px;'>")
                    .append("<tr bgcolor='#ffffff'><td colspan='7' align='center' style='padding:5px; font-weight:bold; font-size:10.5px; color:#0f172a;'>")
                    .append(judulTahap)
                    .append("</td></tr>")
                    .append("<tr bgcolor='#f1f5f9' align='center' style='font-weight:bold; color:#0f766e;'>")
                    .append("<td width='4%'>No</td><td width='30%'>Nama Obat</td><td width='10%'>Dosis</td><td width='10%'>Frekuensi</td><td width='12%'>Cara Pemberian</td><td width='18%'>Tindak Lanjut DPJP</td><td width='16%'>Perubahan Aturan Pakai</td></tr>");

                psDet = koneksi.prepareStatement("SELECT * FROM rekonsiliasi_obat_detail_obat WHERE no_rekonsiliasi=?");
                psDet.setString(1, noRekon);
                rsDet = psDet.executeQuery();
                int no = 1;
                while (rsDet.next()) {
                    html.append("<tr>")
                        .append("<td align='center'>").append(no++).append("</td>")
                        .append("<td><b>").append(rsDet.getString("nama_obat")).append("</b></td>")
                        .append("<td align='center'>").append(rsDet.getString("dosis_obat")).append("</td>")
                        .append("<td align='center'>").append(rsDet.getString("frekuensi")).append("</td>")
                        .append("<td align='center'>").append(rsDet.getString("cara_pemberian")).append("</td>")
                        .append("<td>").append(rsDet.getString("tindak_lanjut")).append("</td>")
                        .append("<td align='center'>").append(rsDet.getString("perubahan_aturan_pakai")).append("</td>")
                        .append("</tr>");
                }
                if (no == 1) {
                    html.append("<tr><td colspan='7' align='center' style='padding:12px; color:#64748b; font-style:italic;'>— Tidak ada data penggunaan obat yang direkonsiliasi / Tidak membawa obat sebelum admisi —</td></tr>");
                }
                html.append("</table>");

                // 5. RIWAYAT ALERGI
                html.append("<div style='margin-top:8px; font-size:10px; background:#fff1f2; border:1px solid #fecdd3; padding:6px 10px;'>")
                    .append("<b style='color:#9f1239;'>RIWAYAT ALERGI OBAT:</b> &nbsp;&nbsp;")
                    .append("Obat Alergi: <b>").append(rsPrev.getString("alergi_obat")).append("</b> | ")
                    .append("Manifestasi / Gejala: <b>").append(rsPrev.getString("manifestasi_alergi")).append("</b> | ")
                    .append("Dampak Alergi: <b>").append(rsPrev.getString("dampak_alergi")).append("</b>")
                    .append("</div>");

                // 6. LEMBAR BAWAH: SUMBER INFORMASI (KIRI) & TTE APOTEKER (KANAN)
                String tglTtd = (tglKonfirm != null && tglKonfirm.length() >= 10)
                        ? tglKonfirm.substring(0, 10)
                        : (tglWwncTampil);

                String sInformasi = rsPrev.getString("sumber_informasi");
                String sInformasiLain = rsPrev.getString("sumber_informasi_lain");
                boolean sRM = "Rekam Medis".equalsIgnoreCase(sInformasi);
                boolean sPasien = "Pasien / Keluarga Pasien".equalsIgnoreCase(sInformasi) || "Pasien".equalsIgnoreCase(sInformasi) || "Keluarga".equalsIgnoreCase(sInformasi);
                boolean sLain = "Obat / Resep / Lain-lain".equalsIgnoreCase(sInformasi) || (!sRM && !sPasien && sInformasi != null && !sInformasi.isEmpty());

                html.append("<table width='100%' style='margin-top:12px; font-size:10px;' cellpadding='0' cellspacing='0'>")
                    .append("<tr>")
                    .append("<td width='52%' valign='top' style='border:1px solid #cbd5e1; background:#f8fafc; padding:8px;'>")
                    .append("<b style='color:#0f766e;'>SUMBER INFORMASI:</b><br>")
                    .append("<span style='font-size:9.5px; color:#64748b;'>Daftar obat yang digunakan sebelum admisi diperoleh dari:</span><br><br>")
                    .append(sRM ? "<b>[ &#10003; ] Rekam Medis</b><br>" : "[ &nbsp; ] Rekam Medis<br>")
                    .append(sPasien ? "<b>[ &#10003; ] Pasien / Keluarga Pasien</b><br>" : "[ &nbsp; ] Pasien / Keluarga Pasien<br>")
                    .append(sLain ? "<b>[ &#10003; ] Obat / Resep / Lain-lain</b> (" + sInformasiLain + ")" : "[ &nbsp; ] Obat / Resep / Lain-lain")
                    .append("</td>")
                    .append("<td width='6%'>&nbsp;</td>")
                    .append("<td width='42%' align='center' valign='top' style='border:1px solid #cbd5e1; background:#ffffff; padding:8px;'>")
                    .append("Gunung Sugih, <b>").append(tglTtd).append("</b><br>")
                    .append("<hr style='border:0; border-top:1px solid #e2e8f0; margin:4px 0;'>")
                    .append("<b>Apoteker yang melakukan rekonsiliasi obat</b><br><br>");

                if (tglKonfirm != null && !tglKonfirm.isEmpty()) {
                    String fingerHash = Sequel.cariIsi(
                        "SELECT SHA1(sidikjari.sidikjari) FROM sidikjari "
                        + "INNER JOIN pegawai ON pegawai.id=sidikjari.id WHERE pegawai.nik=?", nipApoteker
                    );
                    if (fingerHash == null || fingerHash.isEmpty()) fingerHash = nipApoteker;

                    String qrContent = "Dikeluarkan di " + akses.getnamars() + ", Kabupaten " + akses.getkabupatenrs()
                            + "\nDokumen: Rekonsiliasi Obat (RM 03-06A)"
                            + "\nNo. Rekonsiliasi: " + noRekon
                            + "\nPasien: " + nmPasien + " (RM: " + noRm + ")"
                            + "\nDitandatangani secara elektronik oleh:\n" + nmApoteker + " (NIP: " + nipApoteker + ")"
                            + "\nID TTE: " + fingerHash
                            + "\nWaktu Validasi: " + tglKonfirm;
                    String qrUrl = generateQrCodeTTE(qrContent, noRekon);
                    String tglKonfirmTampil = (tglKonfirm.length() >= 10) ? tglKonfirm.substring(0, 10) : tglKonfirm;

                    html.append("<table align='center' cellpadding='0' cellspacing='0' style='margin:auto; text-align:center;'>")
                        .append("<tr><td align='center'>");
                    if (qrUrl != null) {
                        html.append("<img src='").append(qrUrl).append("' width='80' height='80' style='border:1px solid #0f766e; background:#ffffff;'>");
                    } else {
                        html.append("<div style='border:1px solid #15803d; padding:6px; color:#15803d;'><b>[ TTE TERVERIFIKASI ]</b></div>");
                    }
                    html.append("</td></tr>")
                        .append("<tr><td align='center' style='font-size:8px; color:#15803d; font-weight:bold; padding-top:2px;'>")
                        .append("&#10003; TERVALIDASI TTE RESMI")
                        .append("</td></tr>")
                        .append("<tr><td align='center' style='font-size:7.5px; color:#64748b;'>")
                        .append(tglKonfirmTampil)
                        .append("</td></tr>")
                        .append("</table>");
                } else {
                    html.append("<div style='border:1px dashed #94a3b8; background:#f8fafc; color:#64748b; padding:10px 8px; width:130px; margin:auto; text-align:center;'>")
                        .append("<b style='font-size:10px; color:#475569;'>[ DRAF ]</b><br>")
                        .append("<span style='font-size:8px; color:#94a3b8;'>Belum Divalidasi<br>Belum Ber-TTE</span>")
                        .append("</div>");
                }

                html.append("<br>")
                    .append("<u><b>").append(nmApoteker).append("</b></u><br>")
                    .append("<span style='font-size:9px;'>NIP. ").append(nipApoteker).append("</span>")
                    .append("</td>")
                    .append("</tr>")
                    .append("</table>")

                    .append("<div style='margin-top:14px; border-top:1px solid #e2e8f0; padding-top:4px; font-size:8px; color:#94a3b8; text-align:right;'>")
                    .append("SIMRS Khanza RME - RSUD Demang Sepulau Raya | Dicetak/Dilihat: ").append(new SimpleDateFormat("dd-MM-yyyy").format(new Date()))
                    .append("</div>")

                    .append("</body></html>");
            }
        } catch (Exception e) {
            System.out.println("Notif Generate HTML Preview: " + e);
        } finally {
            try { if (rsDet != null) rsDet.close(); if (psDet != null) psDet.close(); if (rsPrev != null) rsPrev.close(); if (psPrev != null) psPrev.close(); } catch (Exception e) {}
        }
        return html.toString();
    }

    // =========================================================================
    // UI BUILDER HELPERS
    // =========================================================================
    private JPanel createCardPanel(String title, Color bg, Color borderColor) {
        JPanel p = new JPanel();
        p.setBackground(bg);
        TitledBorder tb = BorderFactory.createTitledBorder(
            new LineBorder(borderColor, 1, false), " " + title + " ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11), TEAL_DARK
        );
        p.setBorder(new CompoundBorder(tb, new EmptyBorder(6, 8, 6, 8)));
        return p;
    }

    private JTextField createReadOnlyField(String val) {
        JTextField tf = new JTextField(val);
        tf.setEditable(false);
        tf.setBackground(new Color(248, 250, 252));
        tf.setFont(new Font("Monospaced", Font.BOLD, 11));
        tf.setBorder(new CompoundBorder(new LineBorder(new Color(226, 232, 240)), new EmptyBorder(2, 4, 2, 4)));
        return tf;
    }

    private JPanel createLabeledRow(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(85, 22));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(71, 85, 105));
        p.add(l, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel createLabeledRowCompact(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(55, 20));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l.setForeground(new Color(71, 85, 105));
        p.add(l, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private ImageIcon getIcon(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                return new ImageIcon(url);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Buka form langsung dari menu utama Khanza (frmUtama)
     */
    public void bukaDariMenuUtama() {
        emptTeks();
        tabPaneUtama.setSelectedIndex(1);
        tampilDataRekonsiliasi();
    }

    /**
     * Dialog pencarian registrasi pasien saat dibuka standalone
     */
    private void bukaDialogCariRegPasien() {
        simrskhanza.DlgCariReg reg = new simrskhanza.DlgCariReg(null, false);
        reg.setSize(Math.min(1000, getWidth() - 40), Math.min(650, getHeight() - 40));
        reg.setLocationRelativeTo(this);
        reg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (reg.getTable().getSelectedRow() != -1) {
                    int r = reg.getTable().getSelectedRow();
                    String norwt = reg.getTable().getValueAt(r, 1).toString();
                    setNoRm(norwt);
                }
            }
        });
        reg.setVisible(true);
    }

    /**
     * Salin seluruh obat dari resep ranap tervalidasi atau catatan terapi dokter ke tabel rekonsiliasi
     */
    private void salinSemuaObatKeTabel() {
        if (TNoRw.getText().trim().isEmpty() || TNoRw.getText().trim().equals("-")) {
            JOptionPane.showMessageDialog(this, "Silakan pilih pasien terlebih dahulu!");
            return;
        }

        Set<String> existingNames = new HashSet<>();
        for (int i = 0; i < tabModeObat.getRowCount(); i++) {
            Object val = tabModeObat.getValueAt(i, 1);
            if (val != null) existingNames.add(val.toString().trim().toLowerCase());
        }

        if (rbAdmisi.isSelected() && rbPakaiTidak.isSelected()) {
            rbPakaiYa.setSelected(true);
            tbObat.setEnabled(true);
            btnTambahInline.setEnabled(true);
            btnPilihMasterObat.setEnabled(true);
            btnHapusBarisObat.setEnabled(true);
            if (btnSalinRekonSebelumnya != null) btnSalinRekonSebelumnya.setEnabled(true);
        }

        int disalin = 0;
        if (rbTransfer.isSelected() || rbPulang.isSelected()) {
            if (listObatResepTervalidasi == null || listObatResepTervalidasi.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data resep dokter yang dapat disalin.");
                return;
            }
            for (Map<String, String> item : listObatResepTervalidasi) {
                String nama = item.get("nama");
                String kode = item.get("kode");
                String aturan = item.get("aturan");
                if (nama == null || nama.trim().isEmpty() || existingNames.contains(nama.trim().toLowerCase())) continue;

                String tindakLanjut = "Lanjut aturan pakai sama";
                String caraPemberian = "Oral";
                if (nama.toLowerCase().contains("inj") || nama.toLowerCase().contains("infus") || nama.toLowerCase().contains("ivfd")) {
                    caraPemberian = "Injeksi/IV";
                }
                tabModeObat.addRow(new Object[]{
                    kode != null ? kode : "",
                    nama.trim(),
                    "",
                    (aturan != null && !"-".equals(aturan)) ? aturan.trim() : "",
                    caraPemberian,
                    tindakLanjut,
                    "-"
                });
                existingNames.add(nama.trim().toLowerCase());
                disalin++;
            }
            updateBadgeObat();
            String asal = rbTransfer.isSelected() ? "Resep Ranap Transfer Ruang" : "Resep Ranap Pulang";
            JOptionPane.showMessageDialog(this, disalin + " obat dari " + asal + " berhasil disalin ke tabel rekonsiliasi.");
        } else {
            String tataLaksana = txtContekanTataLaksana.getText();
            if (tataLaksana == null || tataLaksana.trim().isEmpty() || tataLaksana.startsWith("(Belum ada")) {
                JOptionPane.showMessageDialog(this, "Tidak ada catatan tata laksana / terapi dokter yang dapat disalin.");
                return;
            }
            java.util.List<ItemObatTerdeteksi> listObat = parseDaftarObatDariTeks(tataLaksana);
            for (ItemObatTerdeteksi item : listObat) {
                if (existingNames.contains(item.nama.toLowerCase())) continue;
                tabModeObat.addRow(new Object[]{
                    item.kode,
                    item.nama,
                    item.dosis,
                    item.aturan,
                    item.rute,
                    item.tindakLanjut,
                    item.perubahan
                });
                existingNames.add(item.nama.toLowerCase());
                disalin++;
            }
            updateBadgeObat();
            if (disalin > 0) {
                JOptionPane.showMessageDialog(this, disalin + " item obat dari Asesmen Medis berhasil dideteksi dan disalin ke tabel.");
            } else {
                JOptionPane.showMessageDialog(this, "Tidak ada item obat baru yang terdeteksi (atau semua obat sudah ada di tabel).");
            }
        }
    }

    /**
     * Menarik / menyalin daftar obat dari rekonsiliasi pasien sebelumnya (misal dari tahap Admisi)
     */
    private void tarikRekonsiliasiSebelumnya() {
        String noRawat = TNoRw.getText().trim();
        if (noRawat.isEmpty() || noRawat.equals("-")) {
            JOptionPane.showMessageDialog(this, "Silakan pilih pasien terlebih dahulu!");
            return;
        }

        java.util.List<String[]> listRekon = new ArrayList<>();
        PreparedStatement psPrev = null;
        ResultSet rsPrev = null;
        try {
            String sql = "SELECT no_rekonsiliasi, tanggal_wawancara, rekonsiliasi_obat_saat "
                       + "FROM rekonsiliasi_obat WHERE no_rawat = ? ";
            if (currentModeInput == ModeInput.MODE_EDIT && noRekonSedangDiedit != null && !noRekonSedangDiedit.isEmpty()) {
                sql += "AND no_rekonsiliasi <> ? ";
            }
            sql += "ORDER BY tanggal_wawancara DESC";
            psPrev = koneksi.prepareStatement(sql);
            psPrev.setString(1, noRawat);
            if (currentModeInput == ModeInput.MODE_EDIT && noRekonSedangDiedit != null && !noRekonSedangDiedit.isEmpty()) {
                psPrev.setString(2, noRekonSedangDiedit);
            }
            rsPrev = psPrev.executeQuery();
            while (rsPrev.next()) {
                listRekon.add(new String[]{
                    rsPrev.getString("no_rekonsiliasi"),
                    rsPrev.getString("tanggal_wawancara"),
                    rsPrev.getString("rekonsiliasi_obat_saat")
                });
            }
        } catch (Exception e) {
            System.out.println("Notif Cek Rekon Sebelumnya: " + e);
        } finally {
            try { if (rsPrev != null) rsPrev.close(); if (psPrev != null) psPrev.close(); } catch (Exception e) {}
        }

        if (listRekon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Belum ditemukan riwayat rekonsiliasi obat sebelumnya untuk nomor rawat ini (" + noRawat + ").");
            return;
        }

        String targetNoRekon = "";
        if (listRekon.size() == 1) {
            String[] rk = listRekon.get(0);
            int konfirm = JOptionPane.showConfirmDialog(
                this,
                "Ditemukan rekonsiliasi obat sebelumnya:\n"
                + "No. Rekonsiliasi : " + rk[0] + "\n"
                + "Tahap            : " + rk[2] + "\n"
                + "Tanggal          : " + rk[1] + "\n\n"
                + "Salin daftar obat dari rekonsiliasi tersebut ke tabel saat ini?",
                "Konfirmasi Salin Data",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (konfirm != JOptionPane.YES_OPTION) return;
            targetNoRekon = rk[0];
        } else {
            String[] opsi = new String[listRekon.size()];
            for (int i = 0; i < listRekon.size(); i++) {
                String[] rk = listRekon.get(i);
                opsi[i] = rk[0] + " | " + rk[2] + " (" + rk[1] + ")";
            }
            String pilih = (String) JOptionPane.showInputDialog(
                this, "Pilih rekonsiliasi obat sebelumnya yang ingin disalin:", "Pilih Rekonsiliasi",
                JOptionPane.QUESTION_MESSAGE, null, opsi, opsi[0]
            );
            if (pilih == null) return;
            targetNoRekon = pilih.split(" \\| ")[0].trim();
        }

        PreparedStatement psDet = null;
        ResultSet rsDet = null;
        int disalin = 0;
        try {
            psDet = koneksi.prepareStatement("SELECT * FROM rekonsiliasi_obat_detail_obat WHERE no_rekonsiliasi = ?");
            psDet.setString(1, targetNoRekon);
            rsDet = psDet.executeQuery();

            Set<String> existingNames = new HashSet<>();
            for (int i = 0; i < tabModeObat.getRowCount(); i++) {
                Object val = tabModeObat.getValueAt(i, 1);
                if (val != null) existingNames.add(val.toString().trim().toLowerCase());
            }

            if (rbAdmisi.isSelected() && rbPakaiTidak.isSelected()) {
                rbPakaiYa.setSelected(true);
                tbObat.setEnabled(true);
                btnTambahInline.setEnabled(true);
                btnPilihMasterObat.setEnabled(true);
                btnHapusBarisObat.setEnabled(true);
            }

            while (rsDet.next()) {
                String nama = rsDet.getString("nama_obat");
                if (nama == null || nama.trim().isEmpty()) continue;
                if (existingNames.contains(nama.trim().toLowerCase())) continue;

                tabModeObat.addRow(new Object[]{
                    "",
                    nama.trim(),
                    rsDet.getString("dosis_obat"),
                    rsDet.getString("frekuensi"),
                    rsDet.getString("cara_pemberian"),
                    rsDet.getString("tindak_lanjut"),
                    rsDet.getString("perubahan_aturan_pakai")
                });
                existingNames.add(nama.trim().toLowerCase());
                disalin++;
            }
            updateBadgeObat();
            JOptionPane.showMessageDialog(this, disalin + " obat dari Rekonsiliasi (" + targetNoRekon + ") berhasil disalin ke tabel!");
        } catch (Exception e) {
            System.out.println("Notif Salin Detail Rekon: " + e);
            JOptionPane.showMessageDialog(this, "Gagal menyalin obat: " + e.getMessage());
        } finally {
            try { if (rsDet != null) rsDet.close(); if (psDet != null) psDet.close(); } catch (Exception e) {}
        }
    }

    /**
     * Cetak rekapitulasi register data rekonsiliasi obat (rptDataRekonsiliasiObat.jasper)
     */
    private void cetakRekapitulasi() {
        if (tabModeDataRekon.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data rekonsiliasi yang bisa dicetak pada periode filter saat ini!");
            return;
        }

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Sequel.queryu("delete from temporary where temp37='" + akses.getalamatip() + "'");

            String tgl1 = Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00";
            String tgl2 = Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59";
            String kw = "%" + TCari.getText().trim() + "%";

            String sql = "SELECT r.no_rekonsiliasi, CONCAT(p.no_rkm_medis, ' ', p.nm_pasien) AS pasien, "
                       + "r.tanggal_wawancara, r.rekonsiliasi_obat_saat, r.alergi_obat, "
                       + "r.dampak_alergi, r.manifestasi_alergi, CONCAT(r.nip, ' ', pt.nama) AS petugas "
                       + "FROM rekonsiliasi_obat r "
                       + "INNER JOIN reg_periksa rp ON r.no_rawat = rp.no_rawat "
                       + "INNER JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis "
                       + "INNER JOIN petugas pt ON r.nip = pt.nip "
                       + "WHERE r.tanggal_wawancara BETWEEN ? AND ? "
                       + "AND (r.no_rekonsiliasi LIKE ? OR r.no_rawat LIKE ? OR p.no_rkm_medis LIKE ? OR p.nm_pasien LIKE ? OR pt.nama LIKE ?) "
                       + "ORDER BY r.tanggal_wawancara DESC";

            PreparedStatement psRekap = koneksi.prepareStatement(sql);
            psRekap.setString(1, tgl1);
            psRekap.setString(2, tgl2);
            psRekap.setString(3, kw);
            psRekap.setString(4, kw);
            psRekap.setString(5, kw);
            psRekap.setString(6, kw);
            psRekap.setString(7, kw);
            ResultSet rsRekap = psRekap.executeQuery();

            int idx = 0;
            while (rsRekap.next()) {
                Sequel.menyimpan("temporary", "'" + idx + "','" +
                    rsRekap.getString("no_rekonsiliasi") + "','" +
                    rsRekap.getString("pasien") + "','" +
                    rsRekap.getString("tanggal_wawancara") + "','" +
                    rsRekap.getString("rekonsiliasi_obat_saat") + "','" +
                    rsRekap.getString("alergi_obat") + "','" +
                    rsRekap.getString("dampak_alergi") + "','" +
                    rsRekap.getString("manifestasi_alergi") + "','" +
                    rsRekap.getString("petugas") + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','" + akses.getalamatip() + "'", "Rekapitulasi Rekonsiliasi Obat");
                idx++;
            }
            rsRekap.close();
            psRekap.close();

            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("SELECT setting.logo FROM setting"));

            Valid.MyReportqry("rptDataRekonsiliasiObat.jasper", "report", "::[ Data Rekonsiliasi Obat ]::",
                "SELECT * FROM temporary WHERE temporary.temp37='" + akses.getalamatip() + "' ORDER BY temporary.no", param);
        } catch (Exception ex) {
            System.out.println("Notif Cetak Rekapitulasi: " + ex);
            JOptionPane.showMessageDialog(this, "Gagal mencetak rekapitulasi: " + ex.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }
}
