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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private JLabel lblSubJudul, lblRtl;
    private JPanel pnlToggle;
    private JTextArea txtContekanTataLaksana, txtContekanRPO;
    private JLabel lblContekanAlergi;
    private JPanel panelQuickChips;
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
    private JTable tbDataRekon;
    private DefaultTableModel tabModeDataRekon;
    private JEditorPane LoadHTML;
    private JTextField TCari;
    private JSpinner DTPCari1, DTPCari2;
    private JLabel LCount;

    // Tombol Footer
    private JPanel panelFooterInput, panelFooterData;

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

        JLabel lblBadgeTerkunci = new JLabel(" TERKUNCI ");
        lblBadgeTerkunci.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblBadgeTerkunci.setForeground(new Color(71, 85, 105));
        lblBadgeTerkunci.setBackground(new Color(241, 245, 249));
        lblBadgeTerkunci.setOpaque(true);
        lblBadgeTerkunci.setBorder(new LineBorder(new Color(203, 213, 225), 1, true));
        pnlAvatarNama.add(lblBadgeTerkunci, BorderLayout.EAST);

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

        JScrollPane spTataLaksana = new JScrollPane(txtContekanTataLaksana);
        spTataLaksana.setAlignmentX(Component.LEFT_ALIGNMENT);
        spTataLaksana.setPreferredSize(new Dimension(330, 115));
        spTataLaksana.setMinimumSize(new Dimension(330, 95));

        JButton btnKirimBlokKeTabel = new JButton(" Masukkan Teks Diblok ke Tabel");
        btnKirimBlokKeTabel.setIcon(IconStyle.muat("plus.svg", 13, Color.WHITE));
        btnKirimBlokKeTabel.setBackground(TEAL_MAIN);
        btnKirimBlokKeTabel.setForeground(Color.WHITE);
        btnKirimBlokKeTabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnKirimBlokKeTabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKirimBlokKeTabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnKirimBlokKeTabel.setPreferredSize(new Dimension(330, 26));
        btnKirimBlokKeTabel.setMinimumSize(new Dimension(330, 26));
        btnKirimBlokKeTabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        btnKirimBlokKeTabel.addActionListener(e -> kirimTeksTerblokKeTabel());

        panelQuickChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        panelQuickChips.setOpaque(false);
        panelQuickChips.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelQuickChips.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel lblRpo = new JLabel("2. Riwayat Penggunaan Obat Rumah (RPO):");
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

        JScrollPane spRPO = new JScrollPane(txtContekanRPO);
        spRPO.setAlignmentX(Component.LEFT_ALIGNMENT);
        spRPO.setPreferredSize(new Dimension(330, 65));
        spRPO.setMinimumSize(new Dimension(330, 52));

        JPanel panelAlergiContekan = new JPanel(new BorderLayout(4, 4));
        panelAlergiContekan.setBackground(Color.WHITE);
        panelAlergiContekan.setBorder(new CompoundBorder(
            new LineBorder(new Color(253, 164, 175), 1, false),
            new EmptyBorder(4, 6, 4, 6)
        ));
        panelAlergiContekan.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelAlergiContekan.setPreferredSize(new Dimension(330, 36));
        panelAlergiContekan.setMinimumSize(new Dimension(330, 36));
        panelAlergiContekan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        lblContekanAlergi = new JLabel("Riwayat Alergi Pasien: -");
        lblContekanAlergi.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblContekanAlergi.setForeground(new Color(159, 18, 57));

        panelAlergiContekan.add(lblContekanAlergi, BorderLayout.CENTER);

        contekanBody.add(pnlContekanHeader);
        contekanBody.add(Box.createVerticalStrut(5));
        contekanBody.add(lblRtl);
        contekanBody.add(Box.createVerticalStrut(3));
        contekanBody.add(spTataLaksana);
        contekanBody.add(Box.createVerticalStrut(4));
        contekanBody.add(btnKirimBlokKeTabel);
        contekanBody.add(Box.createVerticalStrut(4));
        contekanBody.add(panelQuickChips);
        contekanBody.add(Box.createVerticalStrut(5));
        contekanBody.add(lblRpo);
        contekanBody.add(Box.createVerticalStrut(3));
        contekanBody.add(spRPO);
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
        DTPTgl.setPreferredSize(new Dimension(105, 23));
        DTPTgl.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                autoNomorRekonsiliasi();
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
        JPanel rowHeader2 = new JPanel(new GridLayout(1, 3, 8, 0));
        rowHeader2.setOpaque(false);

        // 1. Rekonsiliasi Saat
        JPanel pnlSaat = new JPanel(new BorderLayout(0, 2));
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
        JPanel pnlPakaiObat = new JPanel(new BorderLayout(0, 2));
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
        JPanel pnlSumber = new JPanel(new BorderLayout(0, 2));
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

        btnGroupTabel.add(btnHapusBarisObat);
        btnGroupTabel.add(btnPilihMasterObat);
        btnGroupTabel.add(btnTambahInline);

        toolbarTabel.add(lblBadgeJmlObat, BorderLayout.WEST);
        toolbarTabel.add(btnGroupTabel, BorderLayout.EAST);

        // Setup Model & JTable Inline
        tabModeObat = new DefaultTableModel(null, new Object[]{
            "Kode", "Nama Obat (Ketik Manual / Spasi utk Cari)", "Dosis Obat",
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
            "Lanjut aturan pakai sama", "Lanjut aturan pakai berubah", "Stop"
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
        JPanel container = new JPanel(new GridLayout(1, 2, 12, 12));
        container.setBackground(SLATE_BG);
        container.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Kiri: Tabel Daftar Riwayat Rekonsiliasi
        JPanel panelKiri = createCardPanel("DAFTAR REKONSILIASI OBAT PASIEN", Color.WHITE, SLATE_BORDER);
        panelKiri.setLayout(new BorderLayout(6, 6));

        tabModeDataRekon = new DefaultTableModel(null, new Object[]{
            "No. Rekonsiliasi", "No.Rawat", "No.RM", "Nama Pasien", "Tgl Wawancara",
            "Rekonsiliasi Saat", "NIP", "Apoteker", "Status Konfirm"
        }) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbDataRekon = new JTable(tabModeDataRekon);
        tbDataRekon.setRowHeight(26);
        tbDataRekon.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tbDataRekon.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tbDataRekon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tbDataRekon.getSelectedRow() != -1) {
                    String noRekon = tbDataRekon.getValueAt(tbDataRekon.getSelectedRow(), 0).toString();
                    muatPreviewHTML(noRekon);
                }
            }
        });

        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panelFilter.setOpaque(false);

        DTPCari1 = new JSpinner(new SpinnerDateModel());
        DTPCari1.setEditor(new JSpinner.DateEditor(DTPCari1, "yyyy-MM-dd"));
        DTPCari1.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        DTPCari2 = new JSpinner(new SpinnerDateModel());
        DTPCari2.setEditor(new JSpinner.DateEditor(DTPCari2, "yyyy-MM-dd"));
        DTPCari2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        TCari = new JTextField(15);
        TCari.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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

        panelFilter.add(new JLabel("Tgl:"));
        panelFilter.add(DTPCari1);
        panelFilter.add(new JLabel("s.d."));
        panelFilter.add(DTPCari2);
        panelFilter.add(new JLabel("Kata Kunci:"));
        panelFilter.add(TCari);
        panelFilter.add(btnCariData);
        panelFilter.add(btnSemuaData);
        panelFilter.add(LCount);

        panelKiri.add(new JScrollPane(tbDataRekon), BorderLayout.CENTER);
        panelKiri.add(panelFilter, BorderLayout.SOUTH);

        // Kanan: Panel Preview Form Fisik & Status Validasi Apoteker
        JPanel panelKanan = createCardPanel("PREVIEW FORM FISIK RM 03-06A & VALIDASI APOTEKER", Color.WHITE, SLATE_BORDER);
        panelKanan.setLayout(new BorderLayout(6, 6));

        LoadHTML = new JEditorPane();
        LoadHTML.setContentType("text/html");
        LoadHTML.setEditable(false);
        LoadHTML.setBackground(Color.WHITE);

        panelKanan.add(new JScrollPane(LoadHTML), BorderLayout.CENTER);

        container.add(panelKiri);
        container.add(panelKanan);
        return container;
    }

    private JPanel buildFooterBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, SLATE_BORDER), new EmptyBorder(8, 16, 8, 16)));

        // Tombol saat Tab 1 Aktif
        panelFooterInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelFooterInput.setOpaque(false);

        JButton btnSimpan = new JButton(" Simpan");
        btnSimpan.setIcon(IconStyle.muat("save.svg", 15, Color.WHITE));
        btnSimpan.setBackground(TEAL_MAIN);
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.addActionListener(e -> simpanRekonsiliasi());

        JButton btnBaru = new JButton(" Baru");
        btnBaru.setIcon(IconStyle.muat("refresh.svg", 15, new Color(71, 85, 105)));
        btnBaru.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBaru.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBaru.addActionListener(e -> emptTeks());

        panelFooterInput.add(btnSimpan);
        panelFooterInput.add(btnBaru);

        // Tombol saat Tab 2 Aktif (Validasi Apoteker + Ganti + Hapus + Cetak)
        panelFooterData = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelFooterData.setOpaque(false);
        panelFooterData.setVisible(false);

        JButton btnKonfirmApoteker = new JButton(" Konfirmasi / Validasi Apoteker");
        btnKonfirmApoteker.setIcon(IconStyle.muat("check.svg", 15, Color.WHITE));
        btnKonfirmApoteker.setBackground(GREEN_EMERALD);
        btnKonfirmApoteker.setForeground(Color.WHITE);
        btnKonfirmApoteker.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnKonfirmApoteker.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKonfirmApoteker.addActionListener(e -> konfirmasiApoteker());

        JButton btnGantiData = new JButton(" Ganti / Edit Data");
        btnGantiData.setIcon(IconStyle.muat("pencil.svg", 15, new Color(71, 85, 105)));
        btnGantiData.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGantiData.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGantiData.addActionListener(e -> gantiDataRekonsiliasi());

        JButton btnHapusRekon = new JButton(" Hapus");
        btnHapusRekon.setIcon(IconStyle.muat("trash.svg", 15, new Color(225, 29, 72)));
        btnHapusRekon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHapusRekon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapusRekon.addActionListener(e -> hapusRekonsiliasi());

        JButton btnCetak = new JButton(" Cetak RM 03-06A");
        btnCetak.setIcon(IconStyle.muat("printer.svg", 15, Color.WHITE));
        btnCetak.setBackground(TEAL_MAIN);
        btnCetak.setForeground(Color.WHITE);
        btnCetak.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCetak.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCetak.addActionListener(e -> cetakRM0306A());

        panelFooterData.add(btnKonfirmApoteker);
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
            }
        });

        // Listener Perubahan Tahap Rekonsiliasi Saat (Admisi, Transfer, Pulang)
        ActionListener saatListener = e -> updateModeSidebarSaat();
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

    private void kirimTeksTerblokKeTabel() {
        String selected = txtContekanTataLaksana.getSelectedText();
        if (selected == null || selected.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Blok/sorot dulu nama obat pada kotak Tata Laksana di sebelah kiri, lalu klik tombol ini.");
            return;
        }
        String bersih = selected.trim().replaceAll("^[\\-\\*\u2022\\d+\\.\\)\\s]+", "");
        tabModeObat.addRow(new Object[]{"", bersih, "", "", "Oral", "Lanjut aturan pakai sama", "-"});
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
        if (tataLaksana == null || tataLaksana.trim().isEmpty()) {
            panelQuickChips.revalidate();
            panelQuickChips.repaint();
            return;
        }

        String[] lines = tataLaksana.split("\n");
        int count = 0;
        for (String line : lines) {
            String l = line.trim().replaceAll("^[\\-\\*\u2022\\d+\\.\\)\\s]+", "");
            if (l.length() > 3 && (l.toLowerCase().contains("po") || l.toLowerCase().contains("inj") ||
                    l.toLowerCase().contains("ivfd") || l.toLowerCase().contains("tab") ||
                    l.toLowerCase().contains("syr") || l.toLowerCase().contains("mg") || count < 4)) {
                final String item = l;
                JButton chip = new JButton(item.length() > 22 ? item.substring(0, 20) + ".." : item);
                chip.setIcon(IconStyle.muat("plus.svg", 10, TEAL_MAIN));
                chip.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                chip.setBackground(Color.WHITE);
                chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
                chip.setToolTipText(item);
                chip.addActionListener(e -> {
                    tabModeObat.addRow(new Object[]{"", item, "", "", "Oral", "Lanjut aturan pakai sama", "-"});
                    updateBadgeObat();
                });
                panelQuickChips.add(chip);
                count++;
                if (count >= 5) break;
            }
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
                    DTPCari1.setValue(rs.getDate("tgl_registrasi"));
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

    private void updateModeSidebarSaat() {
        if (rbPulang.isSelected()) {
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
            borderCardSidebar.setTitle(" RESEP OBAT RAWAT INAP ");
            if (cardSidebar != null) cardSidebar.repaint();
        }
        if (lblSubJudul != null) {
            lblSubJudul.setText("Status:");
        }
        if (pnlToggle != null) {
            pnlToggle.removeAll();
            JLabel lblBadgeResep = new JLabel(" Resep Tervalidasi ");
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
            btnRefreshResep.setToolTipText("Muat Ulang Resep Ranap Tervalidasi");
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
            lblRtl.setText("1. Daftar Resep Dokter (Tervalidasi Farmasi):");
        }
        txtContekanTataLaksana.setText((resepRanapTervalidasi == null || resepRanapTervalidasi.trim().isEmpty())
            ? "(Belum ada resep dokter rawat inap yang tervalidasi farmasi untuk no rawat ini)"
            : resepRanapTervalidasi);
        txtContekanTataLaksana.setCaretPosition(0);

        generateQuickChipsResep(listObatResepTervalidasi);

        // Kotak 2: RPO dari Ranap/IGD
        String rpo = !rpoRanap.trim().isEmpty() ? rpoRanap : (!rpoIGD.trim().isEmpty() ? rpoIGD : "");
        txtContekanRPO.setText(rpo.trim().isEmpty() ? "(Tidak ada catatan riwayat obat rumah)" : rpo.trim());
        txtContekanRPO.setCaretPosition(0);

        // Alergi
        String alergi = !alergiRanap.trim().isEmpty() ? alergiRanap : (!alergiIGD.trim().isEmpty() ? alergiIGD : "-");
        lblContekanAlergi.setText("Riwayat Alergi Pasien: " + alergi);
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
            lblContekanAlergi.setText("Riwayat Alergi Pasien (IGD): " + ((alergiIGD == null || alergiIGD.trim().isEmpty()) ? "-" : alergiIGD.trim()));
            generateQuickChips(tataIGD);
        } else {
            styleToggleBtn(btnToggleRanap, true);
            styleToggleBtn(btnToggleIGD, false);

            txtContekanTataLaksana.setText((tataRanap == null || tataRanap.trim().isEmpty()) ? "(Belum ada catatan Tata Laksana di Asesmen Medis Ranap)" : tataRanap.trim());
            txtContekanRPO.setText((rpoRanap == null || rpoRanap.trim().isEmpty()) ? "(Belum ada catatan Riwayat Obat di Asesmen Medis Ranap)" : rpoRanap.trim());
            lblContekanAlergi.setText("Riwayat Alergi Pasien (Ranap): " + ((alergiRanap == null || alergiRanap.trim().isEmpty()) ? "-" : alergiRanap.trim()));
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

        // 1. Ambil dari resep_obat yang sudah tervalidasi (tgl_perawatan <> '0000-00-00')
        try {
            PreparedStatement psResep = koneksi.prepareStatement(
                "SELECT ro.no_resep, ro.tgl_peresepan, ro.jam_peresepan, ro.tgl_perawatan, ro.jam, "
                + "IFNULL(d.nm_dokter, '-') AS nm_dokter, ro.status "
                + "FROM resep_obat ro "
                + "LEFT JOIN dokter d ON ro.kd_dokter = d.kd_dokter "
                + "WHERE ro.no_rawat = ? AND ro.tgl_perawatan <> '0000-00-00' "
                + "ORDER BY ro.tgl_perawatan DESC, ro.jam DESC, ro.no_resep DESC"
            );
            psResep.setString(1, norwt);
            ResultSet rsResep = psResep.executeQuery();
            while (rsResep.next()) {
                totalResep++;
                String noResep = rsResep.getString("no_resep");
                String tglBeri = rsResep.getString("tgl_perawatan") + " " + rsResep.getString("jam");
                String dokter = rsResep.getString("nm_dokter");
                String statusResep = rsResep.getString("status");

                sb.append("========================================\n");
                sb.append("RESEP: ").append(noResep).append(" (").append(statusResep.toUpperCase()).append(")\n");
                sb.append("Tgl Validasi : ").append(tglBeri).append("\n");
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
                    + "WHERE dpo.no_rawat = ? "
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

    public void emptTeks() {
        DTPTgl.setDate(new Date());
        tabModeObat.setRowCount(0);
        rbAdmisi.setSelected(true);
        updateModeSidebarSaat();
        rbPakaiYa.setSelected(true);
        rbSumberRM.setSelected(true);
        KetSumberLain.setText("");
        KetSumberLain.setEnabled(false);
        tbObat.setEnabled(true);
        btnTambahInline.setEnabled(true);
        btnPilihMasterObat.setEnabled(true);
        btnHapusBarisObat.setEnabled(true);
        updateBadgeObat();
        autoNomorRekonsiliasi();
    }

    private void autoNomorRekonsiliasi() {
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
        if (rbPakaiYa.isSelected() && tabModeObat.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tambahkan minimal 1 baris obat, atau pilih 'Tidak Menggunakan Obat Sebelum Admisi'!");
            return;
        }

        if (tbObat.isEditing()) {
            tbObat.getCellEditor().stopCellEditing();
        }

        String noRekon = NoRekonsiliasi.getText().trim();
        String noRawat = TNoRw.getText().trim();
        String tglJam = Valid.SetTgl(DTPTgl.getSelectedItem() + "") + " "
                + new SimpleDateFormat("HH:mm:ss").format(new Date());
        String saat = rbAdmisi.isSelected() ? "Admisi" : (rbTransfer.isSelected() ? "Transfer Antar Ruang" : "Pulang");
        String alergi = "-";
        String manifestasi = "-";
        String dampak = "-";
        String nip = KdPetugas.getText().trim();
        String penggunaanObat = rbPakaiYa.isSelected() ? "Ya" : "Tidak";
        String sumberInfo = rbSumberRM.isSelected() ? "Status Pasien di Rekam Medis" :
                (rbSumberPasien.isSelected() ? "Pasien/Keluarga Pasien" : "Lain-lain");
        String sumberLain = rbSumberLain.isSelected() ? KetSumberLain.getText().trim() : "-";

        Sequel.AutoComitFalse();
        boolean sukses = true;

        // Bersihkan data lama jika no_rekonsiliasi sudah pernah disimpan (mode edit/update)
        Sequel.meghapus("rekonsiliasi_obat_detail_obat", "no_rekonsiliasi", noRekon);
        Sequel.meghapus("rekonsiliasi_obat", "no_rekonsiliasi", noRekon);

        // 1. Simpan Header rekonsiliasi_obat (WAJIB 11 KOLOM POSISIONAL LENGKAP)
        if (Sequel.menyimpantf2("rekonsiliasi_obat", "?,?,?,?,?,?,?,?,?,?,?", "No.Rekonsiliasi", 11, new String[]{
            noRekon, noRawat, tglJam, saat, alergi, manifestasi, dampak, nip, penggunaanObat, sumberInfo, sumberLain
        })) {

            // 2. Simpan Detail rekonsiliasi_obat_detail_obat (8 KOLOM POSISIONAL)
            if (rbPakaiYa.isSelected()) {
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

        if (sukses) {
            Sequel.Commit();
            JOptionPane.showMessageDialog(this, "Data Rekonsiliasi Obat (" + noRekon + ") berhasil disimpan!");
            emptTeks();
            tabPaneUtama.setSelectedIndex(1);
            tampilDataRekonsiliasi();
        } else {
            Sequel.RollBack();
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data Rekonsiliasi Obat! Periksa kelengkapan data.");
        }
        Sequel.AutoComitTrue();
    }

    /**
     * Fitur Konfirmasi / Validasi Apoteker Terpadu di Tab 2
     */
    private void konfirmasiApoteker() {
        int r = tbDataRekon.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu baris data rekonsiliasi yang akan dikonfirmasi Apoteker!");
            return;
        }
        String noRekon = tbDataRekon.getValueAt(r, 0).toString();
        String waktuSekarang = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String nipApoteker = akses.getkode().equals("Admin Utama") ? tbDataRekon.getValueAt(r, 6).toString() : akses.getkode();

        // Simpan / update ke tabel rekonsiliasi_obat_konfirmasi (5 parameter)
        Sequel.meghapus("rekonsiliasi_obat_konfirmasi", "no_rekonsiliasi", noRekon);
        if (Sequel.menyimpantf("rekonsiliasi_obat_konfirmasi", "?,?,?,?,?", "Konfirmasi Apoteker", 5, new String[]{
            noRekon, waktuSekarang, waktuSekarang, nipApoteker, waktuSekarang
        })) {
            JOptionPane.showMessageDialog(this, "Berhasil dikonfirmasi / divalidasi oleh Apoteker!");
            tampilDataRekonsiliasi();
            muatPreviewHTML(noRekon);
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
        String noRekon = tbDataRekon.getValueAt(r, 0).toString();
        try {
            ps = koneksi.prepareStatement("SELECT * FROM rekonsiliasi_obat WHERE no_rekonsiliasi = ?");
            ps.setString(1, noRekon);
            rs = ps.executeQuery();
            if (rs.next()) {
                emptTeks();
                NoRekonsiliasi.setText(noRekon);
                setNoRm(rs.getString("no_rawat"));

                // Waktu wawancara
                try {
                    Date dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("tanggal_wawancara"));
                    DTPTgl.setDate(dt);
                } catch (Exception ex) {}

                // Saat
                String saat = rs.getString("rekonsiliasi_obat_saat");
                if ("Transfer Antar Ruang".equalsIgnoreCase(saat)) rbTransfer.setSelected(true);
                else if ("Pulang".equalsIgnoreCase(saat)) rbPulang.setSelected(true);
                else rbAdmisi.setSelected(true);
                updateModeSidebarSaat();

                // Penggunaan Obat
                String pakai = rs.getString("penggunaan_obat_sebelum_admisi");
                if ("Tidak".equalsIgnoreCase(pakai)) {
                    rbPakaiTidak.setSelected(true);
                    tbObat.setEnabled(false);
                } else {
                    rbPakaiYa.setSelected(true);
                    tbObat.setEnabled(true);
                }

                // Sumber Informasi
                String sumber = rs.getString("sumber_informasi");
                if ("Pasien/Keluarga Pasien".equalsIgnoreCase(sumber)) rbSumberPasien.setSelected(true);
                else if ("Lain-lain".equalsIgnoreCase(sumber)) {
                    rbSumberLain.setSelected(true);
                    KetSumberLain.setEnabled(true);
                    KetSumberLain.setText(rs.getString("sumber_informasi_lain"));
                } else {
                    rbSumberRM.setSelected(true);
                }

                // Petugas
                KdPetugas.setText(rs.getString("nip"));
                NmPetugas.setText(Sequel.CariPetugas(KdPetugas.getText()));

                // Muat Detail Obat
                tabModeObat.setRowCount(0);
                psDetail = koneksi.prepareStatement("SELECT * FROM rekonsiliasi_obat_detail_obat WHERE no_rekonsiliasi = ?");
                psDetail.setString(1, noRekon);
                rsDetail = psDetail.executeQuery();
                while (rsDetail.next()) {
                    tabModeObat.addRow(new Object[]{
                        "",
                        rsDetail.getString("nama_obat"),
                        rsDetail.getString("dosis_obat"),
                        rsDetail.getString("frekuensi"),
                        rsDetail.getString("cara_pemberian"),
                        rsDetail.getString("tindak_lanjut"),
                        rsDetail.getString("perubahan_aturan_pakai")
                    });
                }
                updateBadgeObat();
                tabPaneUtama.setSelectedIndex(0);
            }
        } catch (Exception e) {
            System.out.println("Notif Ganti Data: " + e);
        } finally {
            try { if (rsDetail != null) rsDetail.close(); if (psDetail != null) psDetail.close(); if (rs != null) rs.close(); if (ps != null) ps.close(); } catch (Exception e) {}
        }
    }

    private void hapusRekonsiliasi() {
        int r = tbDataRekon.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu baris data rekonsiliasi yang akan dihapus!");
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
        int r = tbDataRekon.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu baris data rekonsiliasi yang akan dicetak!");
            return;
        }
        String noRekon = tbDataRekon.getValueAt(r, 0).toString();
        String modeCetak = (String) JOptionPane.showInputDialog(
            this, "Silakan pilih mode cetak:", "Cetak Rekonsiliasi Obat (RM 03-06A)",
            JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Lembar", "PDF"}, "Lembar"
        );
        if (modeCetak == null) return;

        try {
            ps = koneksi.prepareStatement(
                "SELECT r.*, p.no_rkm_medis, p.nm_pasien, p.tgl_lahir, pt.nama AS nm_apoteker "
                + "FROM rekonsiliasi_obat r "
                + "INNER JOIN reg_periksa rp ON r.no_rawat = rp.no_rawat "
                + "INNER JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis "
                + "INNER JOIN petugas pt ON r.nip = pt.nip "
                + "WHERE r.no_rekonsiliasi = ?"
            );
            ps.setString(1, noRekon);
            rs = ps.executeQuery();
            if (rs.next()) {
                String nipApoteker = rs.getString("nip");
                String namaApoteker = rs.getString("nm_apoteker");
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
                param.put("pTglLahir", rs.getString("tgl_lahir"));
                param.put("pPenggunaanObatSebelumAdmisi", rs.getString("penggunaan_obat_sebelum_admisi"));
                param.put("pRekonsiliasiSaat", rs.getString("rekonsiliasi_obat_saat"));
                param.put("pTanggalWawancara", rs.getString("tanggal_wawancara"));
                param.put("pSumberInformasi", rs.getString("sumber_informasi"));
                param.put("pSumberInformasiLain", rs.getString("sumber_informasi_lain"));
                param.put("pApotekerNama", namaApoteker);
                param.put("pApotekerNip", nipApoteker);
                param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                        + "\nDitandatangani secara elektronik oleh " + namaApoteker
                        + "\nID " + ((fingerHash == null || fingerHash.isEmpty()) ? nipApoteker : fingerHash)
                        + "\n" + rs.getString("tanggal_wawancara"));

                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                String qryCetak = "SELECT nama_obat, dosis_obat, frekuensi, cara_pemberian, tindak_lanjut, perubahan_aturan_pakai "
                        + "FROM rekonsiliasi_obat_detail_obat WHERE no_rekonsiliasi='" + noRekon + "'";
                if ("PDF".equals(modeCetak)) {
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

    private void tampilDataRekonsiliasi() {
        Valid.tabelKosong(tabModeDataRekon);
        try {
            String tgl1 = new SimpleDateFormat("yyyy-MM-dd").format((Date) DTPCari1.getValue()) + " 00:00:00";
            String tgl2 = new SimpleDateFormat("yyyy-MM-dd").format((Date) DTPCari2.getValue()) + " 23:59:59";
            String kw = "%" + TCari.getText().trim() + "%";

            String sql = "SELECT r.no_rekonsiliasi, r.no_rawat, p.no_rkm_medis, p.nm_pasien, r.tanggal_wawancara, "
                    + "r.rekonsiliasi_obat_saat, r.nip, pt.nama, IF(k.no_rekonsiliasi IS NULL, 'Belum Konfirm', 'Terkonfirmasi') AS status_konfirm "
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
        }
    }

    private void muatPreviewHTML(String noRekon) {
        StringBuilder html = new StringBuilder();
        try {
            ps = koneksi.prepareStatement(
                "SELECT r.*, p.no_rkm_medis, p.nm_pasien, p.tgl_lahir, IF(p.jk='L','Laki-Laki','Perempuan') AS jk, pt.nama AS nm_apoteker, "
                + "k.diterima_farmasi, k.dikonfirmasi_apoteker, k.diserahkan_pasien "
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
                html.append("<html><body style='font-family:sans-serif; font-size:11px; color:#1e293b; padding:10px;'>")
                    .append("<div align='center' style='border-bottom:2px solid #0f766e; padding-bottom:6px; margin-bottom:10px;'>")
                    .append("<b style='font-size:11px; color:#64748b;'>PEMERINTAH KABUPATEN LAMPUNG TENGAH</b><br>")
                    .append("<b style='font-size:14px; color:#0f766e;'>RSUD DEMANG SEPULAU RAYA</b><br>")
                    .append("<span style='font-size:9px; color:#64748b;'>Jl. Lintas Sumatera, No. 4.A, Terbanggi Agung, Gunung Sugih, Lampung Tengah Kode Pos 34161</span>")
                    .append("</div>")

                    .append("<table width='100%' style='margin-bottom:10px; font-size:11px;'><tr>")
                    .append("<td width='60%' valign='top'>")
                    .append("No. RM : <b>").append(rs.getString("no_rkm_medis")).append("</b><br>")
                    .append("Nama Pasien : <b>").append(rs.getString("nm_pasien")).append(" (").append(rs.getString("jk")).append(")</b><br>")
                    .append("Tgl. Lahir : <b>").append(rs.getString("tgl_lahir")).append("</b><br>")
                    .append("No. Rawat : ").append(rs.getString("no_rawat")).append("</td>")
                    .append("<td width='40%' align='right' valign='top'>")
                    .append("<b style='font-size:13px; color:#0f766e;'>REKONSILIASI OBAT (RM 03-06A)</b><br>")
                    .append("Tahap Saat: <b>").append(rs.getString("rekonsiliasi_obat_saat")).append("</b><br>")
                    .append("No. Rekonsiliasi: <code>").append(noRekon).append("</code><br>")
                    .append("Wawancara: ").append(rs.getString("tanggal_wawancara")).append("</td>")
                    .append("</tr></table>");

                // Info Penggunaan & Sumber
                html.append("<table width='100%' style='background:#f8fafc; border:1px solid #cbd5e1; padding:6px; margin-bottom:10px; font-size:10px;'>")
                    .append("<tr>")
                    .append("<td><b>Penggunaan Sebelum Admisi:</b> ").append("Ya".equalsIgnoreCase(rs.getString("penggunaan_obat_sebelum_admisi")) ? "Ya, dengan rincian berikut" : "Tidak Menggunakan Obat").append("</td>")
                    .append("<td><b>Sumber Informasi:</b> ").append(rs.getString("sumber_informasi")).append(" ").append("-".equals(rs.getString("sumber_informasi_lain")) ? "" : "(" + rs.getString("sumber_informasi_lain") + ")").append("</td>")
                    .append("</tr></table>");

                // Tabel Detail Obat
                html.append("<table width='100%' border='1' cellspacing='0' cellpadding='4' style='border-collapse:collapse; border:1px solid #cbd5e1; font-size:10px;'>")
                    .append("<tr bgcolor='#f1f5f9' align='center' style='font-weight:bold; color:#0f766e;'>")
                    .append("<td width='5%'>No</td><td width='32%'>Nama Obat</td><td width='10%'>Dosis</td><td width='10%'>Frekuensi</td><td width='10%'>Cara</td><td width='18%'>Tindak Lanjut DPJP</td><td width='15%'>Perubahan Aturan</td></tr>");

                psDetail = koneksi.prepareStatement("SELECT * FROM rekonsiliasi_obat_detail_obat WHERE no_rekonsiliasi=?");
                psDetail.setString(1, noRekon);
                rsDetail = psDetail.executeQuery();
                int no = 1;
                while (rsDetail.next()) {
                    html.append("<tr>")
                        .append("<td align='center'>").append(no++).append("</td>")
                        .append("<td><b>").append(rsDetail.getString("nama_obat")).append("</b></td>")
                        .append("<td align='center'>").append(rsDetail.getString("dosis_obat")).append("</td>")
                        .append("<td align='center'>").append(rsDetail.getString("frekuensi")).append("</td>")
                        .append("<td align='center'>").append(rsDetail.getString("cara_pemberian")).append("</td>")
                        .append("<td>").append(rsDetail.getString("tindak_lanjut")).append("</td>")
                        .append("<td align='center'>").append(rsDetail.getString("perubahan_aturan_pakai")).append("</td>")
                        .append("</tr>");
                }
                if (no == 1) {
                    html.append("<tr><td colspan='7' align='center' style='color:#64748b; font-style:italic;'>Tidak menggunakan obat sebelum admisi / Tidak ada rincian obat</td></tr>");
                }
                html.append("</table>");

                // Riwayat Alergi
                html.append("<div style='margin-top:10px; font-size:10px; background:#fff1f2; border:1px solid #fecdd3; padding:6px;'>")
                    .append("<b style='color:#9f1239;'>RIWAYAT ALERGI OBAT:</b><br>")
                    .append("Obat Alergi: <b>").append(rs.getString("alergi_obat")).append("</b> | ")
                    .append("Gejala: <b>").append(rs.getString("manifestasi_alergi")).append("</b> | ")
                    .append("Dampak: <b>").append(rs.getString("dampak_alergi")).append("</b>")
                    .append("</div>");

                // Status Validasi Apoteker
                String tglKonfirm = rs.getString("dikonfirmasi_apoteker");
                html.append("<div style='margin-top:10px; padding:8px; border:1px solid #0f766e; background:#f0fdfa; font-size:10px;'>")
                    .append("<b>Apoteker yang Melakukan Rekonsiliasi:</b> ").append(rs.getString("nm_apoteker")).append(" (NIP: ").append(rs.getString("nip")).append(")<br>")
                    .append("<b>Status Validasi Elektronik Apoteker:</b> ")
                    .append(tglKonfirm == null ? "<span style='color:#b45309; font-weight:bold;'>[ Belum Dikonfirmasi Apoteker ]</span>"
                            : "<span style='color:#15803d; font-weight:bold;'>&#10003; Terkonfirmasi Secara Sistem (" + tglKonfirm + ")</span>")
                    .append("</div></body></html>");
            }
            LoadHTML.setText(html.toString());
        } catch (Exception e) {
            System.out.println("Notif Preview HTML: " + e);
        } finally {
            try { if (rsDetail != null) rsDetail.close(); if (psDetail != null) psDetail.close(); if (rs != null) rs.close(); if (ps != null) ps.close(); } catch (Exception e) {}
        }
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
}
