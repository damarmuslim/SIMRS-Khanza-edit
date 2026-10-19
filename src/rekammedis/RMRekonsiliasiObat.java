package rekammedis;

import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import inventory.DlgBarang;
import kepegawaian.DlgCariPetugas;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
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
    private final DlgBarang barang = new DlgBarang(null, false);
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

    // Sidebar Kiri (Identitas Pasien & Contekan Asesmen Medis)
    private JLabel lblAvatarInitials, lblPasienNama, lblPasienJk;
    private JTextField TNoRw, TNoRM, TTglLahir, TUmurStatus;
    private JTextArea txtContekanTataLaksana, txtContekanRPO;
    private JLabel lblContekanAlergi;
    private JPanel panelQuickChips;
    private JButton btnToggleIGD, btnToggleRanap;
    private String tataRanap = "", rpoRanap = "", alergiRanap = "";
    private String tataIGD = "", rpoIGD = "", alergiIGD = "";

    // Header Input Rekonsiliasi (Tab 1)
    private JTextField KdPetugas, NmPetugas, NoRekonsiliasi, KetSumberLain;
    private JSpinner DTPTgl, DTPJam;
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

    // Komponen Alergi Obat (Tab 1)
    private JTextArea AlergiObat, ManifestasiAlergi;
    private JComboBox<String> CmbDampakAlergi;

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
        setSize(1366, 768);
        setMinimumSize(new Dimension(1180, 680));
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

        JLabel lblTitle = new JLabel(" Rekonsiliasi Obat");
        lblTitle.setIcon(getIcon("/picture/Pills.png"));
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

        JLabel lblRS = new JLabel("RSUD Demang Sepulau Raya");
        lblRS.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRS.setForeground(new Color(153, 246, 228));

        headerBar.add(headerLeft, BorderLayout.WEST);
        headerBar.add(lblRS, BorderLayout.EAST);
        getContentPane().add(headerBar, BorderLayout.NORTH);

        // =====================================================================
        // 2. MAIN TABBED PANE (TAB 1: INPUT, TAB 2: DATA & VALIDASI APOTEKER)
        // =====================================================================
        tabPaneUtama = new JTabbedPane();
        tabPaneUtama.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabPaneUtama.setBackground(Color.WHITE);
        tabPaneUtama.addTab("  Input Rekonsiliasi Obat  ", getIcon("/picture/add-file-16x16.png"), buildTabInput());
        tabPaneUtama.addTab("  Data Rekonsiliasi & Validasi Apoteker  ", getIcon("/picture/History2.png"), buildTabData());

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

        // Card 2: Panel Contekan Asesmen Awal Medis (Bebas Notepad!)
        JPanel cardContekan = createCardPanel("CONTEKAN ASESMEN AWAL MEDIS", TEAL_LIGHT, MINT_BORDER);
        cardContekan.setLayout(new BorderLayout(6, 6));

        JPanel pnlContekanHeader = new JPanel(new BorderLayout(4, 0));
        pnlContekanHeader.setOpaque(false);
        pnlContekanHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlContekanHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lblSubJudul = new JLabel("Sumber Asesmen:");
        lblSubJudul.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSubJudul.setForeground(TEAL_DARK);

        JPanel pnlToggle = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        pnlToggle.setOpaque(false);

        btnToggleIGD = new JButton("IGD");
        btnToggleIGD.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnToggleIGD.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleIGD.setMargin(new Insets(1, 6, 1, 6));
        btnToggleIGD.setBackground(Color.WHITE);
        btnToggleIGD.setForeground(TEAL_DARK);
        btnToggleIGD.setBorder(new LineBorder(MINT_BORDER, 1, false));
        btnToggleIGD.addActionListener(e -> tampilkanContekanSumber("IGD"));

        btnToggleRanap = new JButton("Ranap");
        btnToggleRanap.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnToggleRanap.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleRanap.setMargin(new Insets(1, 6, 1, 6));
        btnToggleRanap.setBackground(Color.WHITE);
        btnToggleRanap.setForeground(TEAL_DARK);
        btnToggleRanap.setBorder(new LineBorder(MINT_BORDER, 1, false));
        btnToggleRanap.addActionListener(e -> tampilkanContekanSumber("RANAP"));

        pnlToggle.add(btnToggleIGD);
        pnlToggle.add(btnToggleRanap);

        pnlContekanHeader.add(lblSubJudul, BorderLayout.WEST);
        pnlContekanHeader.add(pnlToggle, BorderLayout.EAST);

        JPanel contekanBody = new JPanel();
        contekanBody.setLayout(new BoxLayout(contekanBody, BoxLayout.Y_AXIS));
        contekanBody.setOpaque(false);

        JLabel lblRtl = new JLabel("1. Tata Laksana / Terapi Dokter:");
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

        JButton btnKirimBlokKeTabel = new JButton(" + Masukkan Teks Diblok ke Tabel");
        btnKirimBlokKeTabel.setIcon(getIcon("/picture/2rightarrow.png"));
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

        lblContekanAlergi = new JLabel("Alergi Medis: -");
        lblContekanAlergi.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblContekanAlergi.setForeground(new Color(159, 18, 57));

        JButton btnSalinAlergi = new JButton(" Salin Alergi");
        btnSalinAlergi.setIcon(getIcon("/picture/editcopy.png"));
        btnSalinAlergi.setBackground(new Color(225, 29, 72));
        btnSalinAlergi.setForeground(Color.WHITE);
        btnSalinAlergi.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnSalinAlergi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalinAlergi.addActionListener(e -> {
            String teks = lblContekanAlergi.getText().replace("Alergi Medis:", "").trim();
            if (!teks.equals("-") && !teks.isEmpty()) {
                AlergiObat.setText(teks);
            }
        });

        panelAlergiContekan.add(lblContekanAlergi, BorderLayout.CENTER);
        panelAlergiContekan.add(btnSalinAlergi, BorderLayout.EAST);

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

        cardContekan.add(contekanBody, BorderLayout.CENTER);

        sidebarKiri.add(cardIdentitas);
        sidebarKiri.add(Box.createVerticalStrut(8));
        sidebarKiri.add(cardContekan);

        // ---------------------------------------------------------------------
        // PANEL KANAN: PARAMETER + INLINE TABLE + ALERGI
        // ---------------------------------------------------------------------
        JPanel mainKanan = new JPanel(new BorderLayout(10, 10));
        mainKanan.setOpaque(false);

        // A. Card Header Parameter Rekonsiliasi
        JPanel cardHeader = createCardPanel("METADATA & PARAMETER WAWANCARA", Color.WHITE, SLATE_BORDER);
        cardHeader.setLayout(new GridLayout(3, 1, 6, 6));

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
        btnCariPetugas.setIcon(getIcon("/picture/190.png"));
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
        JLabel lblWaktuJudul = new JLabel("Tanggal & Jam Wawancara");
        lblWaktuJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblWaktuJudul.setForeground(new Color(71, 85, 105));

        DTPTgl = new JSpinner(new SpinnerDateModel());
        DTPTgl.setEditor(new JSpinner.DateEditor(DTPTgl, "dd-MM-yyyy"));
        DTPTgl.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        DTPJam = new JSpinner(new SpinnerDateModel());
        DTPJam.setEditor(new JSpinner.DateEditor(DTPJam, "HH:mm:ss"));
        DTPJam.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel pnlWaktuInput = new JPanel(new GridLayout(1, 2, 6, 0));
        pnlWaktuInput.setOpaque(false);
        pnlWaktuInput.add(DTPTgl);
        pnlWaktuInput.add(DTPJam);
        pnlWaktu.add(lblWaktuJudul, BorderLayout.NORTH);
        pnlWaktu.add(pnlWaktuInput, BorderLayout.CENTER);

        rowHeader1.add(pnlApoteker);
        rowHeader1.add(pnlNoRekon);
        rowHeader1.add(pnlWaktu);

        // Baris 2: Rekonsiliasi Saat & Penggunaan Obat Sebelum Admisi
        JPanel rowHeader2 = new JPanel(new GridLayout(1, 2, 10, 0));
        rowHeader2.setOpaque(false);

        JPanel pnlSaat = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSaat.setOpaque(false);
        pnlSaat.setBorder(BorderFactory.createTitledBorder(new LineBorder(SLATE_BORDER), " Rekonsiliasi Saat ", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 10), TEAL_DARK));
        rbAdmisi = new JRadioButton("Admisi", true);
        rbTransfer = new JRadioButton("Transfer Antar Ruang");
        rbPulang = new JRadioButton("Pulang");
        ButtonGroup bgSaat = new ButtonGroup();
        bgSaat.add(rbAdmisi); bgSaat.add(rbTransfer); bgSaat.add(rbPulang);
        pnlSaat.add(rbAdmisi); pnlSaat.add(rbTransfer); pnlSaat.add(rbPulang);

        JPanel pnlPakaiObat = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlPakaiObat.setOpaque(false);
        pnlPakaiObat.setBorder(BorderFactory.createTitledBorder(new LineBorder(SLATE_BORDER), " Penggunaan Obat Sebelum Admisi * ", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 10), TEAL_DARK));
        rbPakaiYa = new JRadioButton("Ya, Dengan Rincian Sebagai Berikut", true);
        rbPakaiTidak = new JRadioButton("Tidak Menggunakan Obat Sebelum Admisi");
        ButtonGroup bgPakai = new ButtonGroup();
        bgPakai.add(rbPakaiYa); bgPakai.add(rbPakaiTidak);
        pnlPakaiObat.add(rbPakaiYa); pnlPakaiObat.add(rbPakaiTidak);

        rowHeader2.add(pnlSaat);
        rowHeader2.add(pnlPakaiObat);

        // Baris 3: Sumber Informasi
        JPanel rowHeader3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        rowHeader3.setOpaque(false);
        rowHeader3.setBorder(BorderFactory.createTitledBorder(new LineBorder(SLATE_BORDER), " Sumber Informasi * ", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 10), TEAL_DARK));
        rbSumberRM = new JRadioButton("Status Pasien di Rekam Medis", true);
        rbSumberPasien = new JRadioButton("Pasien/Keluarga Pasien");
        rbSumberLain = new JRadioButton("Lain-lain :");
        ButtonGroup bgSumber = new ButtonGroup();
        bgSumber.add(rbSumberRM); bgSumber.add(rbSumberPasien); bgSumber.add(rbSumberLain);
        KetSumberLain = new JTextField(24);
        KetSumberLain.setEnabled(false);
        KetSumberLain.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rowHeader3.add(rbSumberRM);
        rowHeader3.add(rbSumberPasien);
        rowHeader3.add(rbSumberLain);
        rowHeader3.add(KetSumberLain);

        cardHeader.add(rowHeader1);
        cardHeader.add(rowHeader2);
        cardHeader.add(rowHeader3);

        // B. Card Tengah: INLINE TABLE DAFTAR OBAT DIREKONSILIASI
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
        btnHapusBarisObat.setIcon(getIcon("/picture/stop_f2.png"));
        btnHapusBarisObat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnHapusBarisObat.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnPilihMasterObat = new JButton(" Pilih dari Data Obat/Infus");
        btnPilihMasterObat.setIcon(getIcon("/picture/190.png"));
        btnPilihMasterObat.setBackground(BLUE_OCEAN);
        btnPilihMasterObat.setForeground(Color.WHITE);
        btnPilihMasterObat.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPilihMasterObat.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnTambahInline = new JButton(" Tambah Obat (Inline)");
        btnTambahInline.setIcon(getIcon("/picture/plus_16.png"));
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

        tbObat = new JTable(tabModeObat);
        tbObat.setRowHeight(28);
        tbObat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tbObat.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tbObat.getTableHeader().setBackground(new Color(241, 245, 249));

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

        // ComboBox Cara Pemberian (Kolom 4)
        JComboBox<String> cmbCara = new JComboBox<>(new String[]{
            "Oral", "Injeksi IV", "Injeksi IM", "Injeksi SC", "Infus", "Inhalasi", "Sublingual", "Topikal", "Rektal", "Tetes"
        });
        cmbCara.setEditable(true);
        tbObat.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(cmbCara));

        // ComboBox Tindak Lanjut DPJP (Kolom 5)
        JComboBox<String> cmbTindak = new JComboBox<>(new String[]{
            "Lanjut aturan pakai sama", "Lanjut aturan pakai berubah", "Stop"
        });
        tbObat.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(cmbTindak));

        // Pasang Live Autocomplete pada Kolom 1 (Nama Obat)
        initAutoCompleteObat();

        cardTabelObat.add(toolbarTabel, BorderLayout.NORTH);
        cardTabelObat.add(new JScrollPane(tbObat), BorderLayout.CENTER);

        // C. Card Bawah: Riwayat Alergi Obat
        JPanel cardAlergi = createCardPanel("INFORMASI KLINIS TAMBAHAN - RIWAYAT ALERGI OBAT", Color.WHITE, SLATE_BORDER);
        cardAlergi.setLayout(new GridLayout(1, 3, 12, 0));

        AlergiObat = new JTextArea(2, 18);
        AlergiObat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        AlergiObat.setLineWrap(true);
        AlergiObat.setWrapStyleWord(true);

        ManifestasiAlergi = new JTextArea(2, 18);
        ManifestasiAlergi.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ManifestasiAlergi.setLineWrap(true);
        ManifestasiAlergi.setWrapStyleWord(true);

        CmbDampakAlergi = new JComboBox<>(new String[]{"-", "Ringan", "Sedang", "Berat"});
        CmbDampakAlergi.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel pAlergi1 = new JPanel(new BorderLayout(0, 3));
        pAlergi1.setOpaque(false);
        JLabel lAlergi1 = new JLabel("Obat Penyebab Alergi:");
        lAlergi1.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pAlergi1.add(lAlergi1, BorderLayout.NORTH);
        pAlergi1.add(new JScrollPane(AlergiObat), BorderLayout.CENTER);

        JPanel pAlergi2 = new JPanel(new BorderLayout(0, 3));
        pAlergi2.setOpaque(false);
        JLabel lAlergi2 = new JLabel("Gejala / Manifestasi Klinis:");
        lAlergi2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pAlergi2.add(lAlergi2, BorderLayout.NORTH);
        pAlergi2.add(new JScrollPane(ManifestasiAlergi), BorderLayout.CENTER);

        JPanel pAlergi3 = new JPanel(new BorderLayout(0, 3));
        pAlergi3.setOpaque(false);
        JLabel lAlergi3 = new JLabel("Dampak Alergi:");
        lAlergi3.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pAlergi3.add(lAlergi3, BorderLayout.NORTH);
        pAlergi3.add(CmbDampakAlergi, BorderLayout.CENTER);

        cardAlergi.add(pAlergi1);
        cardAlergi.add(pAlergi2);
        cardAlergi.add(pAlergi3);

        mainKanan.add(cardHeader, BorderLayout.NORTH);
        mainKanan.add(cardTabelObat, BorderLayout.CENTER);
        mainKanan.add(cardAlergi, BorderLayout.SOUTH);

        JScrollPane spSidebar = new JScrollPane(sidebarKiri);
        spSidebar.setBorder(null);
        spSidebar.setOpaque(false);
        spSidebar.getViewport().setOpaque(false);
        spSidebar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spSidebar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        spSidebar.getVerticalScrollBar().setUnitIncrement(16);
        spSidebar.setPreferredSize(new Dimension(360, 600));

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
        btnCariData.setIcon(getIcon("/picture/accept.png"));
        btnCariData.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCariData.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCariData.addActionListener(e -> tampilDataRekonsiliasi());

        JButton btnSemuaData = new JButton(" Semua");
        btnSemuaData.setIcon(getIcon("/picture/Search-16x16.png"));
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
        btnSimpan.setIcon(getIcon("/picture/save-16x16.png"));
        btnSimpan.setBackground(TEAL_MAIN);
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.addActionListener(e -> simpanRekonsiliasi());

        JButton btnBaru = new JButton(" Baru");
        btnBaru.setIcon(getIcon("/picture/Cancel-2-16x16.png"));
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
        btnKonfirmApoteker.setIcon(getIcon("/picture/checked.png"));
        btnKonfirmApoteker.setBackground(GREEN_EMERALD);
        btnKonfirmApoteker.setForeground(Color.WHITE);
        btnKonfirmApoteker.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnKonfirmApoteker.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKonfirmApoteker.addActionListener(e -> konfirmasiApoteker());

        JButton btnGantiData = new JButton(" Ganti / Edit Data");
        btnGantiData.setIcon(getIcon("/picture/inventaris.png"));
        btnGantiData.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGantiData.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGantiData.addActionListener(e -> gantiDataRekonsiliasi());

        JButton btnHapusRekon = new JButton(" Hapus");
        btnHapusRekon.setIcon(getIcon("/picture/stop_f2.png"));
        btnHapusRekon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHapusRekon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapusRekon.addActionListener(e -> hapusRekonsiliasi());

        JButton btnCetak = new JButton(" Cetak RM 03-06A");
        btnCetak.setIcon(getIcon("/picture/b_print.png"));
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
        btnKeluar.setIcon(getIcon("/picture/exit.png"));
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

        // Tambah Baris Obat Inline
        btnTambahInline.addActionListener(e -> {
            tabModeObat.addRow(new Object[]{"", "", "", "", "Oral", "Lanjut aturan pakai sama", "-"});
            updateBadgeObat();
            int lastRow = tabModeObat.getRowCount() - 1;
            tbObat.setRowSelectionInterval(lastRow, lastRow);
            tbObat.scrollRectToVisible(tbObat.getCellRect(lastRow, 1, true));
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

        // Listener hasil pilihan dari DlgBarang (kolom 1 = kode_brng, kolom 2 = nama_brng)
        barang.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (barang.getTable().getSelectedRow() != -1) {
                    int sel = barang.getTable().getSelectedRow();
                    String kode = barang.getTable().getValueAt(sel, 1).toString();
                    String nama = barang.getTable().getValueAt(sel, 2).toString();
                    int selRow = tbObat.getSelectedRow();
                    if (selRow != -1) {
                        tabModeObat.setValueAt(kode, selRow, 0);
                        tabModeObat.setValueAt(nama, selRow, 1);
                    } else {
                        tabModeObat.addRow(new Object[]{kode, nama, "", "", "Oral", "Lanjut aturan pakai sama", "-"});
                    }
                    updateBadgeObat();
                    tbObat.requestFocus();
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

    private void bukaDialogCariObat() {
        barang.emptTeks();
        barang.isCek();
        barang.setSize(getWidth() - 40, getHeight() - 40);
        barang.setLocationRelativeTo(this);
        barang.setVisible(true);
    }

    private void initAutoCompleteObat() {
        popupSaranObat = new JPopupMenu();
        popupSaranObat.setFocusable(false);
        popupSaranObat.setBorder(new LineBorder(TEAL_MAIN, 1, false));

        modelSaranObat = new DefaultListModel<>();
        listSaranObat = new JList<>(modelSaranObat);
        listSaranObat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        listSaranObat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSaranObat.setSelectionBackground(TEAL_MAIN);
        listSaranObat.setSelectionForeground(Color.WHITE);

        JScrollPane scrollSaran = new JScrollPane(listSaranObat);
        scrollSaran.setBorder(null);
        scrollSaran.setPreferredSize(new Dimension(380, 150));
        popupSaranObat.add(scrollSaran);

        txtNamaObatEditor = new JTextField();
        txtNamaObatEditor.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        DefaultCellEditor cellEditorObat = new DefaultCellEditor(txtNamaObatEditor) {
            @Override
            public boolean stopCellEditing() {
                if (popupSaranObat != null && popupSaranObat.isVisible()) {
                    popupSaranObat.setVisible(false);
                }
                return super.stopCellEditing();
            }

            @Override
            public void cancelCellEditing() {
                if (popupSaranObat != null && popupSaranObat.isVisible()) {
                    popupSaranObat.setVisible(false);
                }
                super.cancelCellEditing();
            }
        };
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

        // Mouse click listener pada list saran
        listSaranObat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listSaranObat.getSelectedIndex() != -1) {
                    pilihObatDariSaran(listSaranObat.getSelectedValue());
                }
            }
        });
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
        popupSaranObat.setVisible(false);

        int row = tbObat.getSelectedRow();
        if (row != -1) {
            tabModeObat.setValueAt(kode, row, 0);
            tabModeObat.setValueAt(nama, row, 1);
            if (tbObat.isEditing()) {
                tbObat.getCellEditor().cancelCellEditing();
            }
            tbObat.changeSelection(row, 2, false, false);
            tbObat.editCellAt(row, 2);
            Component editor = tbObat.getEditorComponent();
            if (editor != null) editor.requestFocus();
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
                chip.setIcon(getIcon("/picture/item.png"));
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

        // Default: buka yang ada isinya (prioritas Ranap jika ada, jika tidak ada buka IGD)
        if (adaRanap) {
            tampilkanContekanSumber("RANAP");
        } else if (adaIGD) {
            tampilkanContekanSumber("IGD");
        } else {
            tampilkanContekanSumber("RANAP");
        }

        autoNomorRekonsiliasi();
    }

    private void tampilkanContekanSumber(String sumber) {
        if ("IGD".equalsIgnoreCase(sumber)) {
            btnToggleIGD.setBackground(TEAL_MAIN);
            btnToggleIGD.setForeground(Color.WHITE);
            btnToggleIGD.setBorder(new LineBorder(TEAL_DARK, 1, false));

            btnToggleRanap.setBackground(Color.WHITE);
            btnToggleRanap.setForeground(TEAL_DARK);
            btnToggleRanap.setBorder(new LineBorder(MINT_BORDER, 1, false));

            txtContekanTataLaksana.setText((tataIGD == null || tataIGD.trim().isEmpty()) ? "(Belum ada catatan Tata Laksana di Asesmen Medis IGD)" : tataIGD.trim());
            txtContekanRPO.setText((rpoIGD == null || rpoIGD.trim().isEmpty()) ? "(Belum ada catatan Riwayat Obat di Asesmen Medis IGD)" : rpoIGD.trim());
            lblContekanAlergi.setText("Alergi Medis (IGD): " + ((alergiIGD == null || alergiIGD.trim().isEmpty()) ? "-" : alergiIGD.trim()));
            generateQuickChips(tataIGD);
        } else {
            btnToggleRanap.setBackground(TEAL_MAIN);
            btnToggleRanap.setForeground(Color.WHITE);
            btnToggleRanap.setBorder(new LineBorder(TEAL_DARK, 1, false));

            btnToggleIGD.setBackground(Color.WHITE);
            btnToggleIGD.setForeground(TEAL_DARK);
            btnToggleIGD.setBorder(new LineBorder(MINT_BORDER, 1, false));

            txtContekanTataLaksana.setText((tataRanap == null || tataRanap.trim().isEmpty()) ? "(Belum ada catatan Tata Laksana di Asesmen Medis Ranap)" : tataRanap.trim());
            txtContekanRPO.setText((rpoRanap == null || rpoRanap.trim().isEmpty()) ? "(Belum ada catatan Riwayat Obat di Asesmen Medis Ranap)" : rpoRanap.trim());
            lblContekanAlergi.setText("Alergi Medis (Ranap): " + ((alergiRanap == null || alergiRanap.trim().isEmpty()) ? "-" : alergiRanap.trim()));
            generateQuickChips(tataRanap);
        }
        txtContekanTataLaksana.setCaretPosition(0);
        txtContekanRPO.setCaretPosition(0);
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
        tabModeObat.setRowCount(0);
        AlergiObat.setText("");
        ManifestasiAlergi.setText("");
        CmbDampakAlergi.setSelectedIndex(0);
        rbAdmisi.setSelected(true);
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
        String tgl = new SimpleDateFormat("yyyyMMdd").format((Date) DTPTgl.getValue());
        Valid.autoNomer3(
            "SELECT IFNULL(MAX(CONVERT(RIGHT(no_rekonsiliasi,4),signed)),0) FROM rekonsiliasi_obat WHERE tanggal_wawancara LIKE '"
            + new SimpleDateFormat("yyyy-MM-dd").format((Date) DTPTgl.getValue()) + "%'",
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
        String tglJam = new SimpleDateFormat("yyyy-MM-dd").format((Date) DTPTgl.getValue()) + " "
                + new SimpleDateFormat("HH:mm:ss").format((Date) DTPJam.getValue());
        String saat = rbAdmisi.isSelected() ? "Admisi" : (rbTransfer.isSelected() ? "Transfer Antar Ruang" : "Pulang");
        String alergi = AlergiObat.getText().trim().isEmpty() ? "-" : AlergiObat.getText().trim();
        String manifestasi = ManifestasiAlergi.getText().trim().isEmpty() ? "-" : ManifestasiAlergi.getText().trim();
        String dampak = CmbDampakAlergi.getSelectedItem().toString();
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
                    DTPTgl.setValue(dt);
                    DTPJam.setValue(dt);
                } catch (Exception ex) {}

                // Saat
                String saat = rs.getString("rekonsiliasi_obat_saat");
                if ("Transfer Antar Ruang".equalsIgnoreCase(saat)) rbTransfer.setSelected(true);
                else if ("Pulang".equalsIgnoreCase(saat)) rbPulang.setSelected(true);
                else rbAdmisi.setSelected(true);

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

                // Alergi
                AlergiObat.setText(rs.getString("alergi_obat"));
                ManifestasiAlergi.setText(rs.getString("manifestasi_alergi"));
                CmbDampakAlergi.setSelectedItem(rs.getString("dampak_alergi"));

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
