package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import laporan.DlgCariPenyakit;
import widget.ThemeConfig;

/**
 * ASESMEN MEDIS RAWAT INAP (RM 20) — RSUD Demang Sepulau Raya.
 *
 * <p>Formulir Rekam Medis RM 20 diselaraskan 100% dengan rekomendasi dan visual design Argon
 * (Medical Teal Palette, Sticky Left Sidebar dengan Card Identitas Pasien, Stepper Navigasi 01-06,
 * Indikator Kelengkapan Pengisian Live, dan 6 Card Seksi Vertikal Scroll Tanpa Tab Pengkajian).</p>
 *
 * <p>Data tersimpan pada tabel `asesmen_medis_ranap` dengan primary key `no_rawat`.</p>
 */
public final class RMAsesmenMedisRanap extends javax.swing.JDialog {

    // =========================================================================
    // PALET WARNA RESMI ARGON (MEDICAL TEAL & SLATE — PERSIS DESIGN HANDOFF)
    // =========================================================================
    public static final Color COL_HEADER_DARK   = new Color(10, 46, 44);      // #0A2E2C
    public static final Color COL_TEAL_PRIMARY  = new Color(13, 148, 136);   // #0D9488
    public static final Color COL_TEAL_DARK     = new Color(15, 118, 110);   // #0F766E
    public static final Color COL_TEAL_SOFT     = new Color(240, 253, 250);  // #F0FDFA
    public static final Color COL_TEAL_ACTIVE   = new Color(224, 242, 241);  // #E0F2F1
    public static final Color COL_TEAL_BORDER   = new Color(153, 246, 228);  // #99F6E4
    public static final Color COL_BG_MAIN       = new Color(241, 245, 249);  // #F1F5F9
    public static final Color COL_CARD_BG       = Color.WHITE;
    public static final Color COL_CARD_HEADER   = new Color(248, 250, 252);  // #F8FAFC
    public static final Color COL_BORDER        = new Color(226, 232, 240);  // #E2E8F0
    public static final Color COL_ROW_DIVIDER   = new Color(241, 245, 249);  // #F1F5F9
    public static final Color COL_INPUT_BORDER  = new Color(203, 213, 225);  // #CBD5E1
    public static final Color COL_TEXT_DARK     = new Color(15, 23, 42);     // #0F172A
    public static final Color COL_TEXT_MUTED    = new Color(100, 116, 139);  // #64748B

    public static final Font FONT_TITLE        = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SECTION      = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_ROW_LABEL    = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SUBLABEL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MICRO_HEADER = new Font("Segoe UI", Font.BOLD, 10);
    public static final Font FONT_INPUT        = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO         = new Font("Monospaced", Font.BOLD, 12);

    private static final int LEFT_LABEL_WIDTH = 215;

    private static final String[] KOLOM_RM20 = {
        "tanggal", "kd_dokter_periksa", "kd_dokter_dpjp",
        "keluhan_utama", "keluhan_tambahan", "alergi", "sumber_riwayat", "riwayat_penyakit_pengobatan",
        "keadaan_umum", "kesadaran_mental", "gcs_e", "gcs_m", "gcs_v", "td_sistol", "td_diastol",
        "nadi", "rr", "suhu", "tb", "bb", "umur", "imt", "status_pediatrik",
        "diagnosa_kerja_1", "diagnosa_kerja_2", "diagnosa_kerja_3", "rencana_terapi"
    };

    // Database & SIMRS Functions
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private DlgCariDokter dokter;
    private DlgCariPenyakit penyakit;
    private boolean pilihDpjp = false;
    private int targetDiagnosa = 1;

    // View Cards & Tab Switcher
    private final CardLayout mainViewCardLayout = new CardLayout();
    private final JPanel mainViewContainer = new JPanel(mainViewCardLayout);
    private JScrollPane formScrollPane;
    private JPanel formSectionsContainer;
    private final Map<String, JPanel> sectionCardsMap = new LinkedHashMap<>();
    private final Map<String, JButton> navButtonsMap = new LinkedHashMap<>();

    private JButton btnTabForm;
    private JButton btnTabHistory;
    private JLabel lblLastSaved;

    // Identitas Pasien (Hidden & Display)
    public final JTextField TNoRw = new JTextField();
    public final JTextField TNoRM = new JTextField();
    public final JTextField TPasien = new JTextField();
    public final JTextField TglLahir = new JTextField();
    public final JTextField Jk = new JTextField();
    public final JTextField KdDokterPeriksa = new JTextField();
    public final JTextField KdDokterDpjp = new JTextField();

    private JLabel lblAvatarInisial;
    private JLabel lblPatientName;
    private JLabel lblGenderAge;
    private JLabel lblValRM;
    private JLabel lblValLahir;
    private JLabel lblValTglMasuk;
    private JLabel lblValRawat;
    private JLabel lblValKamar;
    private JLabel lblSavedBadge;

    // Kelengkapan Pengisian Live
    private JProgressBar progressKelengkapan;
    private JLabel lblProgressPercent;
    private JLabel lblProgressCount;

    // Section 01: Pengkajian Awal & Tim Dokter
    public widget.Tanggal TglAsuhan;
    public JTextField txtDokterPemeriksa = createTextField("");
    public JTextField txtDpjp = createTextField("");
    public JComboBox<String> cmbAnamnesis = new JComboBox<>(new String[]{
        "Autoanamnesis", "Aloanamnesis", "Auto & Aloanamnesis"
    });
    public JTextField txtHubungan = createTextField("");
    public JComboBox<String> cmbAsalMasuk = new JComboBox<>(new String[]{
        "IGD", "Poliklinik Rawat Jalan", "Rujukan RS / Faskes Lain", "VK / Kamar Bersalin"
    });
    public JComboBox<String> cmbCaraMasuk = new JComboBox<>(new String[]{
        "Brankar", "Kursi Roda", "Jalan Kaki", "Digendong"
    });

    // Section 02: I. Anamnesa
    public JTextArea txtKeluhanUtama = createTextArea(3);
    public JTextArea txtKeluhanTambahan = createTextArea(2);
    public JTextField txtAlergiObat = createTextField("");
    public JTextField txtAlergiMakanan = createTextField("");
    public JTextArea txtRiwayatPerjalananPenyakit = createTextArea(3);
    public JTextArea txtRiwayatPengobatan = createTextArea(2);

    // Section 03: II. Pemeriksaan Medis
    public SegmentedButtonGroup segKeadaanUmum = new SegmentedButtonGroup(
            new String[]{"Sehat", "Sakit Ringan", "Sakit Sedang", "Sakit Berat"}, "Sakit Sedang"
    );
    public SegmentedButtonGroup segKesadaranMental = new SegmentedButtonGroup(
            new String[]{"Compos Mentis", "Apatis", "Somnolen", "Sopor", "Koma"}, "Compos Mentis"
    );
    public JTextField txtGcsE = createTextField("4");
    public JTextField txtGcsM = createTextField("6");
    public JTextField txtGcsV = createTextField("5");
    public JLabel lblTotalGcs = new JLabel("Total GCS: 15", SwingConstants.CENTER);

    public JTextField txtTdSistol = createTextField("120");
    public JTextField txtTdDiastol = createTextField("80");
    public JTextField txtNadi = createTextField("80");
    public JTextField txtRr = createTextField("20");
    public JTextField txtSuhu = createTextField("36.5");
    public JTextField txtSpo2 = createTextField("98");
    public JTextField txtTb = createTextField("160");
    public JTextField txtBb = createTextField("55");
    public JTextField txtImt = createTextField("21.48");
    public JTextField txtUmur = createTextField("");

    public SegmentedButtonGroup segStatusPediatricGizi = new SegmentedButtonGroup(
            new String[]{"Gizi Kurang", "Baik", "Gizi Lebih"}, "Baik"
    );

    // Section 04: Diagnosa Kerja
    public JTextArea txtDiagnosaKerja1 = createTextArea(2);
    public JTextArea txtDiagnosaKerja2 = createTextArea(2);
    public JTextArea txtDiagnosaKerja3 = createTextArea(2);

    // Section 05: Rencana dan Terapi
    public JTextArea txtRencanaPenunjang = createTextArea(3);
    public JTextArea txtRencanaTerapi = createTextArea(4);

    // Mode Form: Mode Input Baru vs Mode Edit Data
    public enum ModeInput { MODE_INPUT_BARU, MODE_EDIT }
    private ModeInput currentModeInput = ModeInput.MODE_INPUT_BARU;
    private String noRawatSedangDiedit = "";
    private String targetNoRawatTerpilih = "";
    private JPanel pnlBannerEdit;
    private JLabel lblBannerEditText;
    private JButton btnBatalEditBanner;

    // Tab 2: Data Riwayat Asesmen & Pratinjau Dokumen HTML
    private JPanel containerTabData, panelKiriData, panelKananData;
    private DefaultTableModel tabMode;
    private JTable tbObat;
    private widget.Tanggal DTPCari1, DTPCari2;
    private JTextField TCari;
    private JLabel LCount;
    private JLabel lblPreviewDocTitle;
    private JButton btnAksiEditDiPreview;
    private JButton btnAksiFullscreen;
    private JEditorPane editorPreviewHTML;
    private JScrollPane scrollPreviewHTML;

    // Action Buttons
    public JButton BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnPrint, BtnAll, BtnKeluar;

    // Tracked TextAreas for live progress bar
    private final List<JTextArea> trackedTextAreas = new ArrayList<>();

    public RMAsesmenMedisRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Assesmen Medis Rawat Inap (RM 20) — RSUD Demang Sepulau Raya");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1440, 900);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(parent);

        // Inisialisasi Table Model Tab 2
        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.RM", "Nama Pasien", "Tgl.Lahir", "J.K.",
            "Kode Dokter", "Dokter Pemeriksa", "DPJP", "Tanggal", "Diagnosa Kerja"
        }) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
        };

        // Pasang Date Picker
        TglAsuhan = new widget.Tanggal();
        TglAsuhan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TglAsuhan.setDate(new Date());
        TglAsuhan.setPreferredSize(new Dimension(180, 30));
        styleTanggal(TglAsuhan);

        DTPCari1 = new widget.Tanggal();
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setPreferredSize(new Dimension(115, 30));
        styleTanggal(DTPCari1);

        DTPCari2 = new widget.Tanggal();
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setPreferredSize(new Dimension(115, 30));
        styleTanggal(DTPCari2);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(COL_BG_MAIN);

        // 1. Header Bar Atas & Sub-Toolbar
        getContentPane().add(buildTopHeader(), BorderLayout.NORTH);

        // 2. Kontainer View (Tab 1: Formulir Pengkajian, Tab 2: Data Riwayat Asesmen)
        mainViewContainer.setBackground(COL_BG_MAIN);
        mainViewContainer.add(buildFormulirWorkspace(), "FORM");
        mainViewContainer.add(buildHistoryWorkspace(), "HISTORY");
        getContentPane().add(mainViewContainer, BorderLayout.CENTER);

        // 3. Sticky Footer Bawah (Simpan, Baru, Hapus, Ganti, Cetak, Keluar)
        JPanel bottomFooter = buildBottomFooter();
        getContentPane().add(bottomFooter, BorderLayout.SOUTH);
        getContentPane().setComponentZOrder(bottomFooter, 0);

        // 4. Listener Reaktif (GCS, IMT, Sinkronisasi Nama Dokter, & Progress)
        setupReactiveListeners();
        updateGcsCalculation();
        updateImtCalculation();
        updateProgressCalculation();
    }

    // =========================================================================
    // 1. HEADER BAR ATAS & SUB-TOOLBAR (PERSIS ARGON)
    // =========================================================================
    private JPanel buildTopHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());

        // Baris Gelap Atas (#0A2E2C)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COL_HEADER_DARK);
        topBar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTitle.setOpaque(false);

        JLabel title = new JLabel("Assesmen Medis Rawat Inap");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel badgeRm = new JLabel("RM 20", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(17, 94, 89));
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.setColor(new Color(20, 184, 166, 120));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badgeRm.setFont(FONT_MONO);
        badgeRm.setForeground(new Color(204, 251, 241));
        badgeRm.setOpaque(false);
        badgeRm.setBorder(new EmptyBorder(3, 10, 3, 10));

        leftTitle.add(title);
        leftTitle.add(badgeRm);

        JLabel rsName = new JLabel("Pemkab Lampung Tengah • RSUD Demang Sepulau Raya");
        rsName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rsName.setForeground(new Color(167, 243, 208));

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(rsName, BorderLayout.EAST);

        // Sub-Toolbar Putih (Tab Switcher Bersih & Status Penyimpanan)
        JPanel subBar = new JPanel(new BorderLayout());
        subBar.setBackground(Color.WHITE);
        subBar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER),
                new EmptyBorder(0, 20, 0, 20)
        ));

        JPanel tabButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabButtonsPanel.setOpaque(false);

        btnTabForm = createTabButton("Formulir Pengkajian", true);
        btnTabHistory = createTabButton("Data Riwayat Asesmen", false);

        btnTabForm.addActionListener(e -> {
            mainViewCardLayout.show(mainViewContainer, "FORM");
            styleTabButton(btnTabForm, true);
            styleTabButton(btnTabHistory, false);
        });
        btnTabHistory.addActionListener(e -> {
            mainViewCardLayout.show(mainViewContainer, "HISTORY");
            styleTabButton(btnTabHistory, true);
            styleTabButton(btnTabForm, false);
            tampil();
        });

        tabButtonsPanel.add(btnTabForm);
        tabButtonsPanel.add(btnTabHistory);

        JPanel rightStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightStatus.setOpaque(false);

        lblLastSaved = new JLabel("Tersimpan otomatis • RSUD Demang Sepulau Raya");
        lblLastSaved.setFont(FONT_SUBLABEL);
        lblLastSaved.setForeground(COL_TEXT_MUTED);

        rightStatus.add(lblLastSaved);

        subBar.add(tabButtonsPanel, BorderLayout.WEST);
        subBar.add(rightStatus, BorderLayout.EAST);

        wrapper.add(topBar, BorderLayout.NORTH);
        wrapper.add(subBar, BorderLayout.SOUTH);
        return wrapper;
    }

    // =========================================================================
    // STICKY FOOTER BAWAH (TOMBOL SIMPAN & AKSI LENGKAP SIMRS KHANZA)
    // =========================================================================
    private JPanel buildBottomFooter() {
        JPanel footer = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        footer.setOpaque(true);
        footer.setBackground(Color.WHITE);
        footer.setPreferredSize(new Dimension(0, 48));
        footer.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, COL_BORDER),
                new EmptyBorder(6, 16, 6, 16)
        ));

        // Sisi Kiri Footer: Tombol Bantuan (Tarik Admisi) + Hint Status
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JButton btnTarik = createFlatButton("Tarik Admisi", COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnTarik.setToolTipText("Ambil riwayat keluhan & pemeriksaan fisik dari IGD / Poliklinik");
        btnTarik.addActionListener(e -> tarikDataAdmisi(TNoRw.getText().trim(), true));

        JLabel lblFooterHint = new JLabel("Pastikan data asesmen medis telah terisi lengkap sebelum disimpan.");
        lblFooterHint.setFont(FONT_SUBLABEL);
        lblFooterHint.setForeground(COL_TEXT_MUTED);

        leftPanel.add(btnTarik);
        leftPanel.add(lblFooterHint);

        // Sisi Kanan Footer: Tombol Aksi Lengkap SIMRS Khanza (Simpan, Baru, Hapus, Ganti, Cetak, Keluar)
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        BtnSimpan = createFlatButton("Simpan", COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setToolTipText("Simpan data asesmen medis RM 20 (Alt+S)");
        BtnSimpan.addActionListener(e -> {
            if (currentModeInput == ModeInput.MODE_EDIT) {
                aksiGanti();
            } else {
                simpan();
            }
        });

        BtnBatal = createFlatButton("Baru", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        BtnBatal.setMnemonic('B');
        BtnBatal.setToolTipText("Kosongkan form untuk input baru (Alt+B)");
        BtnBatal.addActionListener(e -> batalModeEdit());

        BtnHapus = createFlatButton("Hapus", Color.WHITE, new Color(185, 28, 28), new Color(252, 165, 165));
        BtnHapus.setMnemonic('H');
        BtnHapus.setToolTipText("Hapus data asesmen medis yang dipilih (Alt+H)");
        BtnHapus.addActionListener(e -> aksiHapus());

        BtnEdit = createFlatButton("Ganti", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        BtnEdit.setMnemonic('G');
        BtnEdit.setToolTipText("Perbarui atau edit data asesmen medis yang dipilih (Alt+G)");
        BtnEdit.addActionListener(e -> {
            if (currentModeInput == ModeInput.MODE_EDIT) {
                aksiGanti();
            } else if (tbObat != null && tbObat.getSelectedRow() != -1) {
                mulaiModeEdit(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
            } else if (!TNoRw.getText().trim().isEmpty()) {
                aksiGanti();
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih data yang ingin diganti atau diedit!");
            }
        });

        BtnPrint = createFlatButton("Cetak", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        BtnPrint.setMnemonic('T');
        BtnPrint.setToolTipText("Cetak dokumen asesmen medis RM 20 (Alt+T)");
        BtnPrint.addActionListener(e -> cetakTerpilih());

        BtnKeluar = createFlatButton("Keluar", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setToolTipText("Tutup jendela (Alt+K)");
        BtnKeluar.addActionListener(e -> dispose());

        rightActions.add(BtnSimpan);
        rightActions.add(BtnBatal);
        rightActions.add(BtnHapus);
        rightActions.add(BtnEdit);
        rightActions.add(BtnPrint);
        rightActions.add(BtnKeluar);

        footer.add(leftPanel, BorderLayout.WEST);
        footer.add(rightActions, BorderLayout.EAST);
        return footer;
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        styleTabButton(btn, active);
        return btn;
    }

    private void styleTabButton(JButton btn, boolean active) {
        if (active) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(COL_TEAL_DARK);
            btn.setBackground(COL_TEAL_ACTIVE);
            btn.setOpaque(true);
            btn.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 3, 0, COL_TEAL_PRIMARY),
                    new EmptyBorder(10, 16, 8, 16)
            ));
        } else {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setForeground(COL_TEXT_MUTED);
            btn.setBackground(Color.WHITE);
            btn.setOpaque(true);
            btn.setBorder(new EmptyBorder(10, 16, 11, 16));
        }
    }

    // =========================================================================
    // 2. WORKSPACE FORMULIR (STICKY SIDEBAR KIRI + SCROLLABLE FORM KANAN)
    // =========================================================================
    private JPanel buildFormulirWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout(16, 0));
        workspace.setBackground(COL_BG_MAIN);
        workspace.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Sidebar Kiri Tetap (Sticky) dengan ScrollPane agar aman di layar laptop / resolusi terbatas
        JPanel leftSidebar = buildLeftStickySidebar();
        JScrollPane sidebarScroll = new JScrollPane(leftSidebar);
        sidebarScroll.setBorder(null);
        sidebarScroll.setOpaque(false);
        sidebarScroll.getViewport().setOpaque(false);
        sidebarScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(16);
        sidebarScroll.setPreferredSize(new Dimension(295, 0));
        workspace.add(sidebarScroll, BorderLayout.WEST);

        // Panel Kanan Scrollable Full-Width
        formSectionsContainer = new ScrollableTrackWidthPanel();
        formSectionsContainer.setLayout(new BoxLayout(formSectionsContainer, BoxLayout.Y_AXIS));
        formSectionsContainer.setBackground(COL_BG_MAIN);

        // Banner Mode Edit Data (Tersembunyi secara default, aktif saat mode edit)
        pnlBannerEdit = buildBannerEditPanel();
        pnlBannerEdit.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlBannerEdit.setVisible(false);
        formSectionsContainer.add(pnlBannerEdit);
        formSectionsContainer.add(Box.createVerticalStrut(10));

        addSectionCard("01", buildSection01PengkajianAwal());
        addSectionCard("02", buildSection02Anamnesa());
        addSectionCard("03", buildSection03PemeriksaanMedis());
        addSectionCard("04", buildSection04DiagnosaKerja());
        addSectionCard("05", buildSection05RencanaDanTerapi());

        formScrollPane = new JScrollPane(formSectionsContainer);
        formScrollPane.setBorder(null);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        formScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        workspace.add(formScrollPane, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel buildBannerEditPanel() {
        JPanel banner = new JPanel(new BorderLayout(12, 0));
        banner.setBackground(new Color(254, 243, 199)); // Amber-100
        banner.setBorder(new CompoundBorder(
                new RoundedBorder(new Color(245, 158, 11), 8, 1.2f),
                new EmptyBorder(8, 14, 8, 14)
        ));

        lblBannerEditText = new JLabel("MODE EDIT AKTIF: Anda sedang memperbarui data Asesmen Medis RM 20.");
        lblBannerEditText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBannerEditText.setForeground(new Color(146, 64, 14)); // Amber-800

        btnBatalEditBanner = createFlatButton("Batal Edit / Input Baru", Color.WHITE, new Color(146, 64, 14), new Color(245, 158, 11));
        btnBatalEditBanner.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnBatalEditBanner.addActionListener(e -> batalModeEdit());

        banner.add(lblBannerEditText, BorderLayout.CENTER);
        banner.add(btnBatalEditBanner, BorderLayout.EAST);
        return banner;
    }

    private void addSectionCard(String code, JPanel cardPanel) {
        cardPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionCardsMap.put(code, cardPanel);
        formSectionsContainer.add(cardPanel);
        formSectionsContainer.add(Box.createVerticalStrut(16));
    }

    // =========================================================================
    // 3. SIDEBAR KIRI (IDENTITAS PASIEN RM 20, PROGRESS BAR, STEPPER 01-06)
    // =========================================================================
    private JPanel buildLeftStickySidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(280, 710));

        // A. Card Identitas Pasien
        JPanel patientCard = createBaseCardPanel();
        patientCard.setLayout(new BorderLayout());
        patientCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel patHeader = new JPanel(new BorderLayout());
        patHeader.setBackground(COL_CARD_HEADER);
        patHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER),
                new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel lblIdHeader = new JLabel("IDENTITAS PASIEN");
        lblIdHeader.setFont(FONT_MICRO_HEADER);
        lblIdHeader.setForeground(COL_TEXT_MUTED);

        lblSavedBadge = new JLabel("RM 20 Baru", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(209, 250, 229));
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblSavedBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSavedBadge.setForeground(new Color(4, 120, 87));
        lblSavedBadge.setOpaque(false);
        lblSavedBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        patHeader.add(lblIdHeader, BorderLayout.WEST);
        patHeader.add(lblSavedBadge, BorderLayout.EAST);

        JPanel patBody = new JPanel();
        patBody.setLayout(new BoxLayout(patBody, BoxLayout.Y_AXIS));
        patBody.setOpaque(false);
        patBody.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel avatarRow = new JPanel(new BorderLayout(12, 0));
        avatarRow.setOpaque(false);
        avatarRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        avatarRow.setBorder(new EmptyBorder(2, 2, 4, 2));
        avatarRow.setPreferredSize(new Dimension(255, 48));
        avatarRow.setMinimumSize(new Dimension(255, 48));
        avatarRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 48));

        lblAvatarInisial = new JLabel("PS", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float pad = 1.0f;
                float w = getWidth() - (pad * 2f);
                float h = getHeight() - (pad * 2f);
                g2.setColor(COL_TEAL_SOFT);
                g2.fill(new RoundRectangle2D.Float(pad, pad, w, h, 10, 10));
                g2.setColor(COL_TEAL_BORDER);
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new RoundRectangle2D.Float(pad, pad, w, h, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAvatarInisial.setPreferredSize(new Dimension(42, 42));
        lblAvatarInisial.setMinimumSize(new Dimension(42, 42));
        lblAvatarInisial.setMaximumSize(new Dimension(42, 42));
        lblAvatarInisial.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvatarInisial.setForeground(COL_TEAL_DARK);
        lblAvatarInisial.setOpaque(false);

        JPanel nameBox = new JPanel(new GridLayout(2, 1, 0, 2));
        nameBox.setOpaque(false);
        lblPatientName = new JLabel("-");
        lblPatientName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPatientName.setForeground(COL_TEXT_DARK);
        lblGenderAge = new JLabel("-");
        lblGenderAge.setFont(FONT_SUBLABEL);
        lblGenderAge.setForeground(COL_TEXT_MUTED);
        nameBox.add(lblPatientName);
        nameBox.add(lblGenderAge);

        avatarRow.add(lblAvatarInisial, BorderLayout.WEST);
        avatarRow.add(nameBox, BorderLayout.CENTER);

        patBody.add(avatarRow);
        patBody.add(Box.createVerticalStrut(10));
        patBody.add(new javax.swing.JSeparator());
        patBody.add(Box.createVerticalStrut(8));

        lblValRM = new JLabel("-");
        lblValLahir = new JLabel("-");
        lblValTglMasuk = new JLabel("-");
        lblValRawat = new JLabel("-");
        lblValKamar = new JLabel("-");

        patBody.add(createKeyValueRow("No. RM", lblValRM, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("Tgl Lahir / Umur", lblValLahir, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("Tgl Masuk RS", lblValTglMasuk, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("No. Rawat", lblValRawat, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("Kamar", lblValKamar, false));

        patientCard.add(patHeader, BorderLayout.NORTH);
        patientCard.add(patBody, BorderLayout.CENTER);
        patientCard.setPreferredSize(new Dimension(285, 275));
        patientCard.setMinimumSize(new Dimension(285, 260));
        patientCard.setMaximumSize(new Dimension(Short.MAX_VALUE, 280));

        // B. Card Kelengkapan Pengisian
        JPanel progressCard = createBaseCardPanel();
        progressCard.setLayout(new BoxLayout(progressCard, BoxLayout.Y_AXIS));
        progressCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel progTop = new JPanel(new BorderLayout());
        progTop.setOpaque(false);
        JLabel lblProgTitle = new JLabel("KELENGKAPAN PENGISIAN");
        lblProgTitle.setFont(FONT_MICRO_HEADER);
        lblProgTitle.setForeground(COL_TEXT_MUTED);
        lblProgressPercent = new JLabel("0%");
        lblProgressPercent.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblProgressPercent.setForeground(COL_TEAL_DARK);
        progTop.add(lblProgTitle, BorderLayout.WEST);
        progTop.add(lblProgressPercent, BorderLayout.EAST);

        progressKelengkapan = new JProgressBar(0, 14) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(new Color(241, 245, 249));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                int max = getMaximum();
                int val = getValue();
                if (max > 0 && val > 0) {
                    int fillW = Math.max(h, (int) ((double) val / max * w));
                    g2.setColor(COL_TEAL_PRIMARY);
                    g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h, h, h));
                }
                g2.dispose();
            }
        };
        progressKelengkapan.setValue(0);
        progressKelengkapan.setBorderPainted(false);
        progressKelengkapan.setPreferredSize(new Dimension(250, 8));

        JPanel progBottom = new JPanel(new BorderLayout());
        progBottom.setOpaque(false);
        lblProgressCount = new JLabel("0/14 bagian terisi");
        lblProgressCount.setFont(FONT_SUBLABEL);
        lblProgressCount.setForeground(COL_TEXT_MUTED);
        JLabel lblWajib = new JLabel("Wajib lengkap");
        lblWajib.setFont(FONT_SUBLABEL);
        lblWajib.setForeground(COL_TEAL_DARK);
        progBottom.add(lblProgressCount, BorderLayout.WEST);
        progBottom.add(lblWajib, BorderLayout.EAST);

        progressCard.add(progTop);
        progressCard.add(Box.createVerticalStrut(8));
        progressCard.add(progressKelengkapan);
        progressCard.add(Box.createVerticalStrut(6));
        progressCard.add(progBottom);
        progressCard.setMaximumSize(new Dimension(285, 85));

        // C. Card Bagian Pengkajian (Steppers 01 s/d 05)
        JPanel navCard = createBaseCardPanel();
        navCard.setLayout(new BorderLayout());
        navCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel navHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        navHeader.setBackground(COL_CARD_HEADER);
        navHeader.setBorder(new MatteBorder(0, 0, 1, 0, COL_BORDER));
        JLabel lblNavTitle = new JLabel("BAGIAN PENGKAJIAN");
        lblNavTitle.setFont(FONT_MICRO_HEADER);
        lblNavTitle.setForeground(COL_TEXT_MUTED);
        navHeader.add(lblNavTitle);

        JPanel navList = new JPanel(new GridLayout(5, 1, 0, 4));
        navList.setBackground(Color.WHITE);
        navList.setBorder(new EmptyBorder(6, 6, 6, 6));

        String[][] sections = {
            {"01", "Pengkajian Awal"},
            {"02", "Anamnesis"},
            {"03", "Pemeriksaan Umum & Tanda Vital"},
            {"04", "Diagnosis"},
            {"05", "Program & Perencanaan"}
        };

        for (int i = 0; i < sections.length; i++) {
            final String code = sections[i][0];
            String title = sections[i][1];
            JButton btnNav = new JButton(code + "   " + title) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getBackground() != null && isOpaque()) {
                        g2.setColor(getBackground());
                        g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btnNav.putClientProperty("Component.arc", 8);
            btnNav.setHorizontalAlignment(SwingConstants.LEFT);
            btnNav.setFocusPainted(false);
            btnNav.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            styleNavButton(btnNav, i == 0);
            btnNav.addActionListener(e -> scrollToSection(code));
            navButtonsMap.put(code, btnNav);
            navList.add(btnNav);
        }

        navCard.add(navHeader, BorderLayout.NORTH);
        navCard.add(navList, BorderLayout.CENTER);
        navCard.setMaximumSize(new Dimension(285, 200));

        // D. Card Rujukan Riwayat Medis
        JPanel refCard = createBaseCardPanel();
        refCard.setLayout(new GridLayout(3, 1, 0, 6));
        refCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        refCard.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel lblRef = new JLabel("RUJUKAN RIWAYAT MEDIS");
        lblRef.setFont(FONT_MICRO_HEADER);
        lblRef.setForeground(COL_TEXT_MUTED);

        JButton btnRekonsiliasi = createFlatButton("Cek Rekonsiliasi Obat", COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnRekonsiliasi.addActionListener(e -> bukaRekonsiliasiObat());

        JButton btnRiwayat = createFlatButton("Riwayat Perawatan Pasien", COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnRiwayat.addActionListener(e -> bukaRiwayatPerawatan());

        refCard.add(lblRef);
        refCard.add(btnRekonsiliasi);
        refCard.add(btnRiwayat);
        refCard.setMaximumSize(new Dimension(285, 110));

        sidebar.add(patientCard);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(progressCard);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(navCard);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(refCard);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    public void scrollToSection(String code) {
        for (Map.Entry<String, JButton> entry : navButtonsMap.entrySet()) {
            styleNavButton(entry.getValue(), entry.getKey().equals(code));
        }
        JPanel target = sectionCardsMap.get(code);
        if (target != null && formScrollPane != null) {
            Rectangle bounds = target.getBounds();
            formScrollPane.getViewport().setViewPosition(new Point(0, Math.max(0, bounds.y - 8)));
        }
    }

    private void styleNavButton(JButton btn, boolean active) {
        btn.putClientProperty("Component.arc", 8);
        if (active) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBackground(COL_TEAL_ACTIVE);
            btn.setForeground(COL_TEAL_DARK);
            btn.setOpaque(true);
            btn.setBorder(new CompoundBorder(
                    new RoundedBorder(COL_TEAL_BORDER, 8, 1.0f),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        } else {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setBackground(Color.WHITE);
            btn.setForeground(COL_TEXT_MUTED);
            btn.setOpaque(true);
            btn.setBorder(new EmptyBorder(6, 14, 6, 10));
        }
    }

    // =========================================================================
    // BAGIAN 01: PENGKAJIAN AWAL (WAKTU ASUHAN, TIM DOKTER, ASAL MASUK)
    // =========================================================================
    private JPanel buildSection01PengkajianAwal() {
        JPanel card = createSectionCard("PENGKAJIAN AWAL", "Waktu Asuhan, Tim Dokter, dan Asal Masuk Pasien");
        JPanel body = new JPanel(new GridLayout(2, 1, 0, 12));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Baris 1: 3 Kolom Penuh (Tanggal & Jam, Dokter Pemeriksa, DPJP)
        JPanel row1 = new JPanel(new GridLayout(1, 3, 14, 0));
        row1.setOpaque(false);
        row1.add(wrapFieldWithMicroHeader("TANGGAL & JAM ASUHAN", styleTanggal(TglAsuhan)));
        row1.add(wrapFieldWithMicroHeader("DOKTER YANG MEMERIKSA",
                createLookupFieldPanel(txtDokterPemeriksa, () -> bukaCariDokter(false))));
        row1.add(wrapFieldWithMicroHeader("DOKTER PENANGGUNG JAWAB PELAYANAN (DPJP)",
                createLookupFieldPanel(txtDpjp, () -> bukaCariDokter(true))));

        // Baris 2: 4 Kolom Penuh (Anamnesis, Hubungan, Asal Masuk, Cara ke Ruangan)
        JPanel row2 = new JPanel(new GridLayout(1, 4, 14, 0));
        row2.setOpaque(false);
        txtHubungan.setToolTipText("Hubungan dengan pasien...");
        row2.add(wrapFieldWithMicroHeader("ANAMNESIS", styleComboBox(cmbAnamnesis)));
        row2.add(wrapFieldWithMicroHeader("HUBUNGAN / KELUARGA", txtHubungan));
        row2.add(wrapFieldWithMicroHeader("ASAL MASUK", styleComboBox(cmbAsalMasuk)));
        row2.add(wrapFieldWithMicroHeader("CARA KE RUANGAN", styleComboBox(cmbCaraMasuk)));

        body.add(row1);
        body.add(row2);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // BAGIAN 02: I. ANAMNESA (TABEL 2 KOLOM PERSIS ARGON: LABEL 215PX + INPUT)
    // =========================================================================
    private JPanel buildSection02Anamnesa() {
        JPanel card = createSectionCard(
                "ANAMNESIS",
                "Keluhan Utama, Keluhan Tambahan, Alergi, Riwayat Perjalanan Penyakit & Riwayat Pengobatan (Sendiri / Keluarga)"
        );

        JPanel rowsContainer = new JPanel(new GridBagLayout());
        rowsContainer.setOpaque(false);
        rowsContainer.setBorder(new EmptyBorder(6, 18, 12, 18));

        // a. Keluhan Utama
        addHorizontalFormRow(rowsContainer, 0,
                "a. Keluhan Utama :",
                null,
                wrapTextArea(txtKeluhanUtama, 64),
                true
        );

        // b. Keluhan Tambahan
        addHorizontalFormRow(rowsContainer, 1,
                "b. Keluhan Tambahan :",
                null,
                wrapTextArea(txtKeluhanTambahan, 56),
                true
        );

        // c. Alergi (Alergi Obat & Makanan Sejajar)
        JPanel alergiGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        alergiGrid.setOpaque(false);
        alergiGrid.add(wrapFieldWithMicroHeader("ALERGI OBAT (BILA ADA)", txtAlergiObat));
        alergiGrid.add(wrapFieldWithMicroHeader("ALERGI MAKANAN / LAINNYA (BILA ADA)", txtAlergiMakanan));

        addHorizontalFormRow(rowsContainer, 2,
                "c. Alergi :",
                null,
                alergiGrid,
                true
        );

        // d. Riwayat Perjalanan Penyakit
        addHorizontalFormRow(rowsContainer, 3,
                "d. Riwayat Perjalanan Penyakit :",
                "(Sendiri / Keluarga)",
                wrapTextArea(txtRiwayatPerjalananPenyakit, 68),
                true
        );

        // e. Riwayat Pengobatan
        addHorizontalFormRow(rowsContainer, 4,
                "e. Riwayat Pengobatan :",
                "(Sendiri / Sebelumnya)",
                wrapTextArea(txtRiwayatPengobatan, 58),
                false
        );

        trackedTextAreas.add(txtKeluhanUtama);
        trackedTextAreas.add(txtKeluhanTambahan);
        trackedTextAreas.add(txtRiwayatPerjalananPenyakit);
        trackedTextAreas.add(txtRiwayatPengobatan);

        card.add(rowsContainer, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // BAGIAN 03: PENGKAJIAN MEDIS — II. PEMERIKSAAN MEDIS
    // =========================================================================
    private JPanel buildSection03PemeriksaanMedis() {
        JPanel card = createSectionCard(
                "PEMERIKSAAN UMUM & TANDA VITAL",
                "Keadaan Umum, Kesadaran Mental, Skala GCS, Tensi, Nadi, RR, Suhu, TB, BB, dan Status Pediatric / Gizi"
        );

        JPanel rowsContainer = new JPanel(new GridBagLayout());
        rowsContainer.setOpaque(false);
        rowsContainer.setBorder(new EmptyBorder(6, 18, 12, 18));

        // a. Keadaan Umum
        addHorizontalFormRow(rowsContainer, 0,
                "a. Keadaan Umum :",
                null,
                segKeadaanUmum,
                true
        );

        // b. Kesadaran Mental
        addHorizontalFormRow(rowsContainer, 1,
                "b. Kesadaran Mental :",
                null,
                segKesadaranMental,
                true
        );

        // c. Skala GCS (EYE, MOTORIK, VERBAL, TOTAL)
        JPanel gcsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        gcsGrid.setOpaque(false);

        lblTotalGcs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalGcs.setForeground(COL_TEAL_DARK);
        lblTotalGcs.setOpaque(true);
        lblTotalGcs.setBackground(COL_TEAL_SOFT);
        lblTotalGcs.setBorder(new CompoundBorder(
                new LineBorder(COL_TEAL_BORDER, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));

        gcsGrid.add(wrapFieldWithMicroHeader("EYE (E)", txtGcsE));
        gcsGrid.add(wrapFieldWithMicroHeader("MOTORIK (M)", txtGcsM));
        gcsGrid.add(wrapFieldWithMicroHeader("VERBAL (V)", txtGcsV));
        gcsGrid.add(wrapFieldWithMicroHeader("TOTAL SKALA GCS", lblTotalGcs));

        addHorizontalFormRow(rowsContainer, 2,
                "c. Skala GCS :",
                null,
                gcsGrid,
                true
        );

        // d. Tanda Vital & Antropometri (Grid 2 Baris x 4 Kolom)
        JPanel ttvGrid = new JPanel(new GridLayout(2, 4, 14, 12));
        ttvGrid.setOpaque(false);

        JPanel tdPanel = new JPanel(new BorderLayout(6, 0));
        tdPanel.setOpaque(false);
        JPanel tdInputs = new JPanel(new GridLayout(1, 2, 6, 0));
        tdInputs.setOpaque(false);
        tdInputs.add(txtTdSistol);
        tdInputs.add(txtTdDiastol);
        tdPanel.add(tdInputs, BorderLayout.CENTER);
        tdPanel.add(createInlineUnitLabel("mmHg"), BorderLayout.EAST);

        txtImt.setEditable(false);
        txtImt.setBackground(COL_TEAL_SOFT);
        txtImt.setForeground(COL_TEAL_DARK);
        txtImt.setFont(FONT_MONO);

        ttvGrid.add(wrapFieldWithMicroHeader("TENSI / TEKANAN DARAH", tdPanel));
        ttvGrid.add(wrapFieldWithMicroHeader("NADI", createFieldWithInlineUnit(txtNadi, "x/menit")));
        ttvGrid.add(wrapFieldWithMicroHeader("PERNAPASAN (RR)", createFieldWithInlineUnit(txtRr, "x/menit")));
        ttvGrid.add(wrapFieldWithMicroHeader("SUHU TUBUH (T)", createFieldWithInlineUnit(txtSuhu, "\u00B0C")));

        ttvGrid.add(wrapFieldWithMicroHeader("SATURASI O2 (SpO2)", createFieldWithInlineUnit(txtSpo2, "%")));
        ttvGrid.add(wrapFieldWithMicroHeader("TINGGI BADAN (TB)", createFieldWithInlineUnit(txtTb, "cm")));
        ttvGrid.add(wrapFieldWithMicroHeader("BERAT BADAN (BB)", createFieldWithInlineUnit(txtBb, "kg")));
        ttvGrid.add(wrapFieldWithMicroHeader("INDEKS MASSA TUBUH (IMT)", createFieldWithInlineUnit(txtImt, "kg/m\u00B2")));

        addHorizontalFormRow(rowsContainer, 3,
                "d. Tanda Vital &",
                "Antropometri :",
                ttvGrid,
                true
        );

        // e. Status Pediatric / Gizi
        addHorizontalFormRow(rowsContainer, 4,
                "e. Status Pediatric / Gizi :",
                "Gizi Kurang / Baik / Lebih",
                segStatusPediatricGizi,
                false
        );

        card.add(rowsContainer, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // BAGIAN 04: DIAGNOSA KERJA (1, 2, 3 DENGAN PENCARIAN ICD-10)
    // =========================================================================
    private JPanel buildSection04DiagnosaKerja() {
        JPanel card = createSectionCard(
                "DIAGNOSIS",
                "Daftar Diagnosa Kerja 1, Diagnosa Kerja 2, dan Diagnosa Kerja 3 Terintegrasi Pencarian ICD-10"
        );

        JPanel rowsContainer = new JPanel(new GridBagLayout());
        rowsContainer.setOpaque(false);
        rowsContainer.setBorder(new EmptyBorder(6, 18, 12, 18));

        addDiagnosisFormRow(rowsContainer, 0, "1. Diagnosa Kerja 1 :", txtDiagnosaKerja1, 1, true);
        addDiagnosisFormRow(rowsContainer, 1, "2. Diagnosa Kerja 2 :", txtDiagnosaKerja2, 2, true);
        addDiagnosisFormRow(rowsContainer, 2, "3. Diagnosa Kerja 3 :", txtDiagnosaKerja3, 3, false);

        trackedTextAreas.add(txtDiagnosaKerja1);
        trackedTextAreas.add(txtDiagnosaKerja2);
        trackedTextAreas.add(txtDiagnosaKerja3);

        card.add(rowsContainer, BorderLayout.CENTER);
        return card;
    }

    private void addDiagnosisFormRow(JPanel container, int rowIndex, String labelText, JTextArea targetArea, int targetIdx, boolean showBottomBorder) {
        JPanel rowPanel = new JPanel(new BorderLayout(16, 0));
        rowPanel.setOpaque(false);
        rowPanel.setBorder(new CompoundBorder(
                showBottomBorder ? new MatteBorder(0, 0, 1, 0, COL_ROW_DIVIDER) : new EmptyBorder(0, 0, 0, 0),
                new EmptyBorder(10, 0, 10, 0)
        ));

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);
        leftCol.setPreferredSize(new Dimension(LEFT_LABEL_WIDTH, 58));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_ROW_LABEL);
        lbl.setForeground(COL_TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCariIcd = createSearchButton("Cari ICD-10");
        btnCariIcd.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCariIcd.setMaximumSize(new Dimension(150, 30));
        btnCariIcd.addActionListener(e -> bukaCariPenyakit(targetIdx));

        leftCol.add(lbl);
        leftCol.add(Box.createVerticalStrut(8));
        leftCol.add(btnCariIcd);

        rowPanel.add(leftCol, BorderLayout.WEST);
        rowPanel.add(wrapTextArea(targetArea, 58), BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = rowIndex;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        container.add(rowPanel, gbc);
    }

    // =========================================================================
    // BAGIAN 05: RENCANA DAN TERAPI (PENUNJANG & PENGOBATAN + TEMPLATE CEPAT)
    // =========================================================================
    private JPanel buildSection05RencanaDanTerapi() {
        JPanel card = createSectionCard(
                "PROGRAM & PERENCANAAN",
                "Pemeriksaan Penunjang & Pengobatannya (Laboratorium, Radiologi, Infus, Injeksi, Obat Oral & Monitoring)"
        );

        JPanel rowsContainer = new JPanel(new GridBagLayout());
        rowsContainer.setOpaque(false);
        rowsContainer.setBorder(new EmptyBorder(6, 18, 12, 18));

        // Toolbar Template Cepat
        JPanel topActionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        topActionRow.setOpaque(false);
        JButton btnInsertTemplate = createFlatButton("+ Isi Template Penunjang & Terapi", COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnInsertTemplate.addActionListener(e -> tampilkanTemplateTerapi(btnInsertTemplate));
        topActionRow.add(btnInsertTemplate);

        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.gridx = 0;
        gbcTop.gridy = 0;
        gbcTop.weightx = 1.0;
        gbcTop.fill = GridBagConstraints.HORIZONTAL;
        rowsContainer.add(topActionRow, gbcTop);

        // a. Pemeriksaan Penunjang
        addHorizontalFormRow(rowsContainer, 1,
                "a. Pemeriksaan Penunjang :",
                "(Laboratorium, Radiologi, dll)",
                wrapTextArea(txtRencanaPenunjang, 66),
                true
        );

        // b. Pengobatan & Terapi
        addHorizontalFormRow(rowsContainer, 2,
                "b. Pengobatan & Terapi :",
                "(Infus, Injeksi, Oral, Diet)",
                wrapTextArea(txtRencanaTerapi, 82),
                false
        );

        trackedTextAreas.add(txtRencanaPenunjang);
        trackedTextAreas.add(txtRencanaTerapi);

        card.add(rowsContainer, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // TAB 2: DATA RIWAYAT ASESMEN (SPLIT-VIEW 2-KOLOM DENGAN LIVE HTML PREVIEW)
    // =========================================================================
    private JPanel buildHistoryWorkspace() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COL_BG_MAIN);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // JSplitPane Horizontal: Sisi Kiri Tabel Riwayat + Filter Bawah, Sisi Kanan Live HTML Preview
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(8);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // ---------------------------------------------------------------------
        // SISI KIRI: TABEL DAFTAR RIWAYAT & FILTER PENCARIAN DI BAWAH
        // ---------------------------------------------------------------------
        JPanel leftCard = createBaseCardPanel();
        leftCard.setLayout(new BorderLayout(0, 0));
        leftCard.setBorder(new CompoundBorder(
                new RoundedBorder(COL_BORDER, 12, 1.2f),
                new EmptyBorder(12, 12, 10, 12)
        ));
        leftCard.setPreferredSize(new Dimension(540, 0));

        // Header Sisi Kiri
        JPanel leftHeader = new JPanel(new BorderLayout(8, 0));
        leftHeader.setOpaque(false);
        leftHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblDaftarTitle = new JLabel("Daftar Riwayat Asesmen Medis");
        lblDaftarTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDaftarTitle.setForeground(COL_TEXT_DARK);

        JLabel lblDaftarHint = new JLabel("Pilih baris untuk pratinjau | Dobel-klik untuk edit");
        lblDaftarHint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblDaftarHint.setForeground(COL_TEXT_MUTED);

        leftHeader.add(lblDaftarTitle, BorderLayout.WEST);
        leftHeader.add(lblDaftarHint, BorderLayout.EAST);
        leftCard.add(leftHeader, BorderLayout.NORTH);

        // Tabel Data Riwayat
        tbObat = new JTable(tabMode);
        tbObat.setRowHeight(28);
        tbObat.setFont(FONT_INPUT);
        tbObat.getTableHeader().setFont(FONT_MICRO_HEADER);
        tbObat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbObat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tbObat.getSelectedRow() != -1) {
                targetNoRawatTerpilih = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
                muatPreviewHTML(targetNoRawatTerpilih);
            }
        });
        tbObat.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (tbObat.getSelectedRow() != -1) {
                    targetNoRawatTerpilih = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
                    muatPreviewHTML(targetNoRawatTerpilih);
                    if (e.getClickCount() == 2) {
                        mulaiModeEdit(targetNoRawatTerpilih);
                    }
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(tbObat);
        scrollTable.setBorder(new RoundedBorder(COL_INPUT_BORDER, 8, 1.0f));
        leftCard.add(scrollTable, BorderLayout.CENTER);

        // Panel Filter & Pencarian di Bawah (Persis seperti Rekomendasi / Screenshot)
        JPanel searchBarBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        searchBarBottom.setBackground(Color.WHITE);
        searchBarBottom.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, COL_BORDER),
                new EmptyBorder(6, 0, 0, 0)
        ));

        JLabel lblPeriode = new JLabel("Periode :");
        lblPeriode.setFont(FONT_ROW_LABEL);

        TCari = createTextField("");
        TCari.setPreferredSize(new Dimension(140, 28));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                }
            }
        });

        JButton btnCari = createFlatButton("Cari", COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnCari.setPreferredSize(new Dimension(65, 28));
        btnCari.addActionListener(e -> tampil());

        JButton btnSemua = createFlatButton("Semua", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnSemua.setPreferredSize(new Dimension(70, 28));
        btnSemua.addActionListener(e -> {
            TCari.setText("");
            tampil();
        });

        LCount = new JLabel("0 Rekam Data");
        LCount.setFont(FONT_SUBLABEL);
        LCount.setForeground(COL_TEXT_MUTED);

        searchBarBottom.add(lblPeriode);
        searchBarBottom.add(styleTanggal(DTPCari1));
        searchBarBottom.add(new JLabel("s.d."));
        searchBarBottom.add(styleTanggal(DTPCari2));
        searchBarBottom.add(Box.createHorizontalStrut(6));
        searchBarBottom.add(new JLabel("Keyword :"));
        searchBarBottom.add(TCari);
        searchBarBottom.add(btnCari);
        searchBarBottom.add(btnSemua);
        searchBarBottom.add(Box.createHorizontalStrut(8));
        searchBarBottom.add(LCount);

        leftCard.add(searchBarBottom, BorderLayout.SOUTH);

        // ---------------------------------------------------------------------
        // SISI KANAN: LIVE PRATINJAU DOKUMEN REKAM MEDIS RM 20 (HTML VIEW)
        // ---------------------------------------------------------------------
        JPanel rightCard = createBaseCardPanel();
        rightCard.setLayout(new BorderLayout(0, 8));
        rightCard.setBorder(new CompoundBorder(
                new RoundedBorder(COL_BORDER, 12, 1.2f),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Top Toolbar Pratinjau Dokumen
        JPanel previewToolbar = new JPanel(new BorderLayout(8, 0));
        previewToolbar.setOpaque(false);

        lblPreviewDocTitle = new JLabel("Pratinjau Dokumen Rekam Medis");
        lblPreviewDocTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPreviewDocTitle.setForeground(COL_TEXT_DARK);

        JPanel previewActionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        previewActionBtns.setOpaque(false);

        btnAksiEditDiPreview = createFlatButton("Edit Data Ini", COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnAksiEditDiPreview.setToolTipText("Buka formulir untuk memperbarui data asesmen medis ini");
        btnAksiEditDiPreview.setEnabled(false);
        btnAksiEditDiPreview.addActionListener(e -> {
            if (targetNoRawatTerpilih != null && !targetNoRawatTerpilih.isEmpty()) {
                mulaiModeEdit(targetNoRawatTerpilih);
            }
        });

        btnAksiFullscreen = createFlatButton("Jendela Layar Penuh", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnAksiFullscreen.setToolTipText("Buka dokumen lengkap di jendela baru layar penuh");
        btnAksiFullscreen.setEnabled(false);
        btnAksiFullscreen.addActionListener(e -> {
            if (targetNoRawatTerpilih != null && !targetNoRawatTerpilih.isEmpty()) {
                bukaDialogPreviewFullscreen(targetNoRawatTerpilih);
            }
        });

        JButton btnAksiCetakPreview = createFlatButton("Cetak Dokumen", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnAksiCetakPreview.setToolTipText("Cetak dokumen rekam medis asesmen medis (RM 20)");
        btnAksiCetakPreview.addActionListener(e -> cetakTerpilih());

        previewActionBtns.add(btnAksiEditDiPreview);
        previewActionBtns.add(btnAksiFullscreen);
        previewActionBtns.add(btnAksiCetakPreview);

        previewToolbar.add(lblPreviewDocTitle, BorderLayout.WEST);
        previewToolbar.add(previewActionBtns, BorderLayout.EAST);
        rightCard.add(previewToolbar, BorderLayout.NORTH);

        // Area HTML Editor Pane
        editorPreviewHTML = new JEditorPane();
        editorPreviewHTML.setContentType("text/html");
        editorPreviewHTML.setEditable(false);
        editorPreviewHTML.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPreviewHTML.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        editorPreviewHTML.setText(placeholderHTMLPreview());
        editorPreviewHTML.setCaretPosition(0);

        JScrollPane scrollPreview = new JScrollPane(editorPreviewHTML);
        scrollPreview.setBorder(new RoundedBorder(COL_INPUT_BORDER, 8, 1.0f));
        scrollPreview.getVerticalScrollBar().setUnitIncrement(18);
        rightCard.add(scrollPreview, BorderLayout.CENTER);

        // Gabungkan ke JSplitPane
        splitPane.setLeftComponent(leftCard);
        splitPane.setRightComponent(rightCard);
        splitPane.setDividerLocation(520);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private String placeholderHTMLPreview() {
        return "<html><body style='font-family:Segoe UI,Tahoma,sans-serif; background-color:#F8FAFC; color:#64748B; padding:40px 20px; text-align:center;'>"
                + "<div style='margin-top:60px;'>"
                + "<h3 style='color:#0F766E; font-size:16px; margin-bottom:8px;'>Pratinjau Dokumen Rekam Medis (RM 20)</h3>"
                + "<p style='font-size:12px; color:#475569;'>Silakan klik salah satu baris pada daftar riwayat di sebelah kiri untuk melihat dokumen lengkap.</p>"
                + "<p style='font-size:11px; color:#94A3B8; margin-top:16px;'>RSUD Demang Sepulau Raya &bull; Sistem Informasi Manajemen Rumah Sakit</p>"
                + "</div></body></html>";
    }

    public void muatPreviewHTML(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) return;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            String html = generateHTMLPreviewContent(noRawat);
            if (editorPreviewHTML != null) {
                editorPreviewHTML.setText(html);
                editorPreviewHTML.setCaretPosition(0);
            }
            if (lblPreviewDocTitle != null) {
                lblPreviewDocTitle.setText("Pratinjau Dokumen RM 20 : " + noRawat);
            }
            if (btnAksiEditDiPreview != null) btnAksiEditDiPreview.setEnabled(true);
            if (btnAksiFullscreen != null) btnAksiFullscreen.setEnabled(true);
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private String generateHTMLPreviewContent(String noRawat) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>")
          .append("body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; font-size: 11px; color: #1e293b; background: #ffffff; margin: 12px; }")
          .append("h2 { margin: 0; color: #0f766e; font-size: 15px; font-weight: bold; text-align: center; }")
          .append("h3 { margin: 2px 0 0 0; color: #0f172a; font-size: 12px; font-weight: bold; text-align: center; text-transform: uppercase; }")
          .append(".header-box { border-bottom: 2px solid #0f766e; padding-bottom: 8px; margin-bottom: 12px; }")
          .append(".section-title { background: #0f766e; color: #ffffff; padding: 4px 8px; font-size: 11px; font-weight: bold; margin-top: 10px; margin-bottom: 4px; }")
          .append("table.data-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }")
          .append("table.data-table td { padding: 4px 6px; border: 1px solid #cbd5e1; vertical-align: top; font-size: 11px; }")
          .append(".label-cell { background: #f1f5f9; font-weight: 600; width: 28%; color: #334155; }")
          .append(".val-cell { background: #ffffff; color: #0f172a; }")
          .append(".ttd-table { width: 100%; margin-top: 16px; border: none; }")
          .append(".ttd-table td { border: none; text-align: center; font-size: 11px; padding: 6px; }")
          .append("</style></head><body>");

        // Header RSUD Demang Sepulau Raya
        sb.append("<div class='header-box'>")
          .append("<h2>").append(akses.getnamars()).append("</h2>")
          .append("<div style='text-align:center; font-size:10px; color:#64748b; margin:2px 0;'>")
          .append(akses.getalamatrs()).append(" &bull; ").append(akses.getkabupatenrs()).append(" &bull; Telp: ").append(akses.getkontakrs())
          .append("</div>")
          .append("<h3>ASESMEN AWAL MEDIS RAWAT INAP (RM 20)</h3>")
          .append("</div>");

        String sql = "select asesmen_medis_ranap.*, pasien.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, "
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk, reg_periksa.tgl_registrasi, reg_periksa.jam_reg, "
                + "dokterperiksa.nm_dokter as nm_dokter_periksa, dokterdpjp.nm_dokter as nm_dokter_dpjp "
                + "from asesmen_medis_ranap "
                + "inner join reg_periksa on asesmen_medis_ranap.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join dokter dokterperiksa on asesmen_medis_ranap.kd_dokter_periksa=dokterperiksa.kd_dokter "
                + "left join dokter dokterdpjp on asesmen_medis_ranap.kd_dokter_dpjp=dokterdpjp.kd_dokter "
                + "where asesmen_medis_ranap.no_rawat='" + noRawat + "'";

        try (PreparedStatement psPrev = koneksi.prepareStatement(sql);
             ResultSet rsPrev = psPrev.executeQuery()) {
            if (rsPrev.next()) {
                // Info Pasien
                sb.append("<table class='data-table'>")
                  .append("<tr>")
                  .append("<td class='label-cell'>No. Rawat / No. RM</td><td class='val-cell'><b>").append(noRawat).append("</b> / ").append(rsPrev.getString("no_rkm_medis")).append("</td>")
                  .append("<td class='label-cell'>Tanggal Asuhan</td><td class='val-cell'>").append(rsPrev.getString("tanggal")).append("</td>")
                  .append("</tr><tr>")
                  .append("<td class='label-cell'>Nama Pasien</td><td class='val-cell'><b>").append(rsPrev.getString("nm_pasien")).append("</b> (").append(rsPrev.getString("jk")).append(")</td>")
                  .append("<td class='label-cell'>Tgl Lahir / Usia</td><td class='val-cell'>").append(rsPrev.getString("tgl_lahir")).append(" / ").append(rsPrev.getString("umur")).append("</td>")
                  .append("</tr><tr>")
                  .append("<td class='label-cell'>Dokter Pemeriksa</td><td class='val-cell'>").append(rsPrev.getString("nm_dokter_periksa") != null ? rsPrev.getString("nm_dokter_periksa") : "-").append("</td>")
                  .append("<td class='label-cell'>Dokter DPJP</td><td class='val-cell'>").append(rsPrev.getString("nm_dokter_dpjp") != null ? rsPrev.getString("nm_dokter_dpjp") : "-").append("</td>")
                  .append("</tr></table>");

                // Seksi 1: Pengkajian Awal
                sb.append("<div class='section-title'>I. ANAMNESIS & KELUHAN</div>")
                  .append("<table class='data-table'>")
                  .append("<tr><td class='label-cell'>Keluhan Utama</td><td class='val-cell'>").append(nl2br(rsPrev.getString("keluhan_utama"))).append("</td></tr>")
                  .append("<tr><td class='label-cell'>Keluhan Tambahan</td><td class='val-cell'>").append(nl2br(rsPrev.getString("keluhan_tambahan"))).append("</td></tr>")
                  .append("<tr><td class='label-cell'>Sumber Anamnesis</td><td class='val-cell'>").append(rsPrev.getString("sumber_riwayat")).append("</td></tr>")
                  .append("<tr><td class='label-cell'>Riwayat Alergi</td><td class='val-cell' style='color:#dc2626; font-weight:600;'>").append(rsPrev.getString("riwayat_alergi")).append("</td></tr>")
                  .append("<tr><td class='label-cell'>Riwayat Penyakit & Terapi</td><td class='val-cell'>").append(nl2br(rsPrev.getString("riwayat_penyakit_pengobatan"))).append("</td></tr>")
                  .append("</table>");

                // Seksi 2: Pemeriksaan Fisik Umum & Vital Sign
                sb.append("<div class='section-title'>II. PEMERIKSAAN UMUM & TANDA VITAL</div>")
                  .append("<table class='data-table'>")
                  .append("<tr>")
                  .append("<td class='label-cell'>Keadaan Umum</td><td class='val-cell'>").append(rsPrev.getString("keadaan_umum")).append("</td>")
                  .append("<td class='label-cell'>Kesadaran Mental</td><td class='val-cell'>").append(rsPrev.getString("kesadaran_mental")).append("</td>")
                  .append("</tr><tr>")
                  .append("<td class='label-cell'>GCS (E / M / V)</td><td class='val-cell'>E: ").append(rsPrev.getString("gcs_e")).append(" / M: ").append(rsPrev.getString("gcs_m")).append(" / V: ").append(rsPrev.getString("gcs_v")).append("</td>")
                  .append("<td class='label-cell'>Tekanan Darah</td><td class='val-cell'>").append(rsPrev.getString("td_sistol")).append("/").append(rsPrev.getString("td_diastol")).append(" mmHg</td>")
                  .append("</tr><tr>")
                  .append("<td class='label-cell'>Frekuensi Nadi & RR</td><td class='val-cell'>Nadi: ").append(rsPrev.getString("nadi")).append(" x/mnt &bull; RR: ").append(rsPrev.getString("rr")).append(" x/mnt</td>")
                  .append("<td class='label-cell'>Suhu & SpO2</td><td class='val-cell'>Suhu: ").append(rsPrev.getString("suhu")).append(" &deg;C &bull; SpO2: 98%</td>")
                  .append("</tr><tr>")
                  .append("<td class='label-cell'>Antropometri (TB / BB)</td><td class='val-cell'>TB: ").append(rsPrev.getString("tb")).append(" cm &bull; BB: ").append(rsPrev.getString("bb")).append(" kg</td>")
                  .append("<td class='label-cell'>IMT & Status Gizi</td><td class='val-cell'>IMT: ").append(rsPrev.getString("imt")).append(" (").append(rsPrev.getString("status_pediatrik")).append(")</td>")
                  .append("</tr></table>");

                // Seksi 3: Diagnosa
                sb.append("<div class='section-title'>III. DIAGNOSIS MEDIS</div>")
                  .append("<table class='data-table'>")
                  .append("<tr><td class='label-cell'>Diagnosis Utama / Kerja 1</td><td class='val-cell' style='font-weight:bold; color:#0f766e;'>").append(rsPrev.getString("diagnosa_kerja_1")).append("</td></tr>")
                  .append("<tr><td class='label-cell'>Diagnosis Kerja 2 / Tambahan</td><td class='val-cell'>").append(rsPrev.getString("diagnosa_kerja_2") != null ? rsPrev.getString("diagnosa_kerja_2") : "-").append("</td></tr>")
                  .append("<tr><td class='label-cell'>Diagnosis Kerja 3 / Tambahan</td><td class='val-cell'>").append(rsPrev.getString("diagnosa_kerja_3") != null ? rsPrev.getString("diagnosa_kerja_3") : "-").append("</td></tr>")
                  .append("</table>");

                // Seksi 4: Rencana & Terapi
                sb.append("<div class='section-title'>IV. PROGRAM PERENCANAAN & TERAPI</div>")
                  .append("<table class='data-table'>")
                  .append("<tr><td class='label-cell'>Rencana Terapi & Tindakan</td><td class='val-cell'>").append(nl2br(rsPrev.getString("rencana_terapi"))).append("</td></tr>")
                  .append("</table>");

                // Tanda Tangan
                sb.append("<table class='ttd-table'>")
                  .append("<tr><td width='50%'>Dokter Yang Memeriksa,</td><td width='50%'>Dokter Penanggung Jawab (DPJP),</td></tr>")
                  .append("<tr><td style='padding:24px 0;'><b style='color:#0f766e;'>[ Tanda Tangan Elektronik ]</b><br><span style='font-size:9px; color:#64748b;'>ID Terverifikasi Sistem SIMRS</span></td>")
                  .append("<td style='padding:24px 0;'><b style='color:#0f766e;'>[ Tanda Tangan Elektronik ]</b><br><span style='font-size:9px; color:#64748b;'>ID Terverifikasi Sistem SIMRS</span></td></tr>")
                  .append("<tr><td><b>").append(rsPrev.getString("nm_dokter_periksa") != null ? rsPrev.getString("nm_dokter_periksa") : "-").append("</b></td>")
                  .append("<td><b>").append(rsPrev.getString("nm_dokter_dpjp") != null ? rsPrev.getString("nm_dokter_dpjp") : "-").append("</b></td></tr>")
                  .append("</table>");
            } else {
                sb.append("<div style='text-align:center; padding:30px; color:#94a3b8;'>Data asesmen untuk nomor rawat ini tidak ditemukan.</div>");
            }
        } catch (Exception ex) {
            sb.append("<div style='color:red; padding:20px;'>Gagal memuat pratinjau data: ").append(ex.getMessage()).append("</div>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    private String nl2br(String text) {
        if (text == null || text.trim().isEmpty()) return "-";
        return text.replace("\n", "<br>");
    }

    public void bukaDialogPreviewFullscreen(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) return;
        JDialog dlgFull = new JDialog(this, "Dokumen Asesmen Medis Rawat Inap (RM 20) - " + noRawat, true);
        dlgFull.setSize(920, 720);
        dlgFull.setLocationRelativeTo(this);
        dlgFull.setLayout(new BorderLayout());

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel lblTitle = new JLabel("Dokumen Asesmen Medis RM 20 : " + noRawat);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(COL_TEXT_DARK);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBtns.setOpaque(false);

        JButton btnEditDirect = createFlatButton("Edit Data Ini", COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnEditDirect.addActionListener(e -> {
            dlgFull.dispose();
            mulaiModeEdit(noRawat);
        });

        JButton btnPrintDirect = createFlatButton("Cetak Dokumen", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnPrintDirect.addActionListener(e -> cetakSatu(noRawat));

        JButton btnClose = createFlatButton("Tutup", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnClose.addActionListener(e -> dlgFull.dispose());

        pnlBtns.add(btnEditDirect);
        pnlBtns.add(btnPrintDirect);
        pnlBtns.add(btnClose);

        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(pnlBtns, BorderLayout.EAST);
        dlgFull.add(pnlTop, BorderLayout.NORTH);

        JEditorPane editorFull = new JEditorPane();
        editorFull.setContentType("text/html");
        editorFull.setEditable(false);
        editorFull.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorFull.setText(generateHTMLPreviewContent(noRawat));
        editorFull.setCaretPosition(0);

        JScrollPane scrollFull = new JScrollPane(editorFull);
        scrollFull.setBorder(null);
        scrollFull.getVerticalScrollBar().setUnitIncrement(20);
        dlgFull.add(scrollFull, BorderLayout.CENTER);

        dlgFull.setVisible(true);
    }

    public void mulaiModeEdit(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) return;
        currentModeInput = ModeInput.MODE_EDIT;
        noRawatSedangDiedit = noRawat.trim();
        targetNoRawatTerpilih = noRawat.trim();

        // Muat data ke formulir
        muatData(noRawatSedangDiedit);

        // Perbarui visual banner dan badge
        if (pnlBannerEdit != null) {
            lblBannerEditText.setText("MODE EDIT AKTIF: Anda sedang memperbarui data Asesmen Medis RM 20 untuk No. Rawat [" + noRawatSedangDiedit + "].");
            pnlBannerEdit.setVisible(true);
        }
        if (lblSavedBadge != null) {
            lblSavedBadge.setText("Mode Edit");
            lblSavedBadge.setBackground(new Color(254, 243, 199));
            lblSavedBadge.setForeground(new Color(180, 83, 9));
        }

        // Pindah ke Tab Form
        mainViewCardLayout.show(mainViewContainer, "FORM");
        styleTabButton(btnTabForm, true);
        styleTabButton(btnTabHistory, false);

        // Scroll form ke posisi paling atas
        if (formScrollPane != null) {
            formScrollPane.getVerticalScrollBar().setValue(0);
        }
    }

    public void batalModeEdit() {
        currentModeInput = ModeInput.MODE_INPUT_BARU;
        noRawatSedangDiedit = "";
        if (pnlBannerEdit != null) {
            pnlBannerEdit.setVisible(false);
        }
        emptTeks();
        if (lblSavedBadge != null) {
            lblSavedBadge.setText("RM 20 Baru");
            lblSavedBadge.setBackground(COL_TEAL_SOFT);
            lblSavedBadge.setForeground(COL_TEAL_DARK);
        }
    }

    // =========================================================================
    // REACTIVE LISTENERS (GCS, IMT, SINKRONISASI DOKTER, & PROGRESS BAR)
    // =========================================================================
    private void setupReactiveListeners() {
        DocumentListener gcsListener = new SimpleDocListener(this::updateGcsCalculation);
        txtGcsE.getDocument().addDocumentListener(gcsListener);
        txtGcsM.getDocument().addDocumentListener(gcsListener);
        txtGcsV.getDocument().addDocumentListener(gcsListener);

        DocumentListener imtListener = new SimpleDocListener(this::updateImtCalculation);
        txtTb.getDocument().addDocumentListener(imtListener);
        txtBb.getDocument().addDocumentListener(imtListener);



        DocumentListener progressListener = new SimpleDocListener(this::updateProgressCalculation);
        for (JTextArea area : trackedTextAreas) {
            area.getDocument().addDocumentListener(progressListener);
        }
    }

    private void updateGcsCalculation() {
        try {
            int e = Math.min(4, Math.max(1, Integer.parseInt(txtGcsE.getText().trim())));
            int m = Math.min(6, Math.max(1, Integer.parseInt(txtGcsM.getText().trim())));
            int v = Math.min(5, Math.max(1, Integer.parseInt(txtGcsV.getText().trim())));
            int total = e + m + v;
            lblTotalGcs.setText("Total GCS: " + total);
        } catch (NumberFormatException ex) {
            lblTotalGcs.setText("Total GCS: -");
        }
    }

    private void updateImtCalculation() {
        try {
            double tbCm = Double.parseDouble(txtTb.getText().trim());
            double bbKg = Double.parseDouble(txtBb.getText().trim());
            if (tbCm > 30 && bbKg > 2) {
                double tbM = tbCm / 100.0;
                double imt = bbKg / (tbM * tbM);
                txtImt.setText(new DecimalFormat("0.00").format(imt));
                if (imt < 18.5) {
                    segStatusPediatricGizi.setSelectedValue("Gizi Kurang");
                } else if (imt >= 25.0) {
                    segStatusPediatricGizi.setSelectedValue("Gizi Lebih");
                } else {
                    segStatusPediatricGizi.setSelectedValue("Baik");
                }
            }
        } catch (NumberFormatException ex) {
            txtImt.setText("-");
        }
    }

    private void updateProgressCalculation() {
        int filled = 5; // Parameter dasar: Waktu, Dokter, KU, Kesadaran, GCS
        for (JTextArea area : trackedTextAreas) {
            if (!area.getText().trim().isEmpty()) {
                filled++;
            }
        }
        int total = 14;
        int capped = Math.min(total, filled);
        int pct = (int) Math.round((capped / (double) total) * 100);
        if (progressKelengkapan != null) progressKelengkapan.setValue(capped);
        if (lblProgressPercent != null) lblProgressPercent.setText(pct + "%");
        if (lblProgressCount != null) lblProgressCount.setText(capped + "/" + total + " bagian terisi");
    }

    // =========================================================================
    // DIALOG PENCARIAN DOKTER & ICD-10
    // =========================================================================
    private void bukaCariDokter(boolean untukDpjp) {
        this.pilihDpjp = untukDpjp;
        if (dokter == null) {
            dokter = new DlgCariDokter(null, false);
            dokter.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if (dokter.getTable().getSelectedRow() != -1) {
                        String kd = dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString();
                        String nm = dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString();
                        if (pilihDpjp) {
                            KdDokterDpjp.setText(kd);
                            txtDpjp.setText(nm);
                        } else {
                            KdDokterPeriksa.setText(kd);
                            txtDokterPemeriksa.setText(nm);
                        }
                        updateProgressCalculation();
                    }
                }
            });
        }
        dokter.isCek();
        dokter.setSize(1000, 600);
        dokter.setLocationRelativeTo(this);
        dokter.setVisible(true);
    }

    private void bukaCariPenyakit(int target) {
        this.targetDiagnosa = target;
        if (penyakit == null) {
            penyakit = new DlgCariPenyakit(null, false);
            penyakit.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent ev) {
                    if (penyakit.getTable().getSelectedRow() != -1) {
                        String kode = penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(), 0).toString();
                        String nama = penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(), 1).toString();
                        String hasil = kode + " - " + nama;
                        if (targetDiagnosa == 1) {
                            txtDiagnosaKerja1.setText(hasil);
                            txtDiagnosaKerja1.requestFocus();
                        } else if (targetDiagnosa == 2) {
                            txtDiagnosaKerja2.setText(hasil);
                            txtDiagnosaKerja2.requestFocus();
                        } else if (targetDiagnosa == 3) {
                            txtDiagnosaKerja3.setText(hasil);
                            txtDiagnosaKerja3.requestFocus();
                        }
                        updateProgressCalculation();
                    }
                }
            });
        }
        penyakit.isCek();
        penyakit.emptTeks();
        penyakit.setSize(1000, 600);
        penyakit.setLocationRelativeTo(this);
        penyakit.setVisible(true);
    }

    private void tampilkanTemplateTerapi(Component invoker) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem itemDhf = new JMenuItem("Paket DHF / Demam Berdarah Dengue");
        itemDhf.addActionListener(e -> {
            txtRencanaPenunjang.setText("1. Cek Darah Lengkap (Hb, Ht, Leukosit, Trombosit) serial per 12 jam.\n2. NS1 Ag / Serologi Dengue IgG/IgM.");
            txtRencanaTerapi.setText("- IVFD Ringer Asetat / Asering 20 tetes/menit\n- Paracetamol drip 1000 mg IV (bila Suhu > 38.5 C)\n- Ondansetron 3 x 4 mg IV (bila mual)\n- Minum air 2 - 2.5 liter / 24 jam\n- Observasi tanda kebocoran plasma dan perdarahan");
        });

        JMenuItem itemGea = new JMenuItem("Paket GEA / Gastroenteritis Akut");
        itemGea.addActionListener(e -> {
            txtRencanaPenunjang.setText("1. Feses Lengkap (Makroskopis & Mikroskopis).\n2. Elektrolit Serum (Na, K, Cl), GDS.");
            txtRencanaTerapi.setText("- IVFD Ringer Laktat 20 tetes/menit\n- Inj. Omeprazole 2 x 40 mg IV\n- Inj. Ondansetron 3 x 4 mg IV\n- Attapulgite tab 2 tab tiap BAB cair (maks 12 tab/hari)\n- Oralit 1 sachet tiap BAB cair");
        });

        JMenuItem itemDisp = new JMenuItem("Paket Dispepsia Akut / Gastritis Flare");
        itemDisp.addActionListener(e -> {
            txtRencanaPenunjang.setText("1. Darah Rutin, SGOT, SGPT, Amilase/Lipase (bila indikasi).\n2. USG Abdomen bila nyeri menetap.");
            txtRencanaTerapi.setText("- IVFD NaCl 0.9% 20 tetes/menit\n- Inj. Pantoprazole 1 x 40 mg IV\n- Inj. Metoclopramide 3 x 10 mg IV\n- Sucralfat sirup 3 x 10 ml PO (1 jam sebelum makan)\n- Diet lunak lambung, hindari pedas, asam, dan kafein");
        });

        menu.add(itemDhf);
        menu.add(itemGea);
        menu.add(itemDisp);
        menu.show(invoker, 0, invoker.getHeight());
    }

    private void bukaRekonsiliasiObat() {
        String noRawat = TNoRw.getText().trim();
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih atau muat pasien terlebih dahulu!");
            return;
        }
        try {
            RMRekonsiliasiObat rekon = new RMRekonsiliasiObat(null, false);
            rekon.isCek();
            rekon.setNoRm(noRawat);
            rekon.setSize(1200, 700);
            rekon.setLocationRelativeTo(this);
            rekon.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuka form Rekonsiliasi Obat: " + ex.getMessage());
        }
    }

    private void bukaRiwayatPerawatan() {
        String noRm = TNoRM.getText().trim();
        if (noRm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih atau muat pasien terlebih dahulu!");
            return;
        }
        try {
            RMRiwayatPerawatan riwayat = new RMRiwayatPerawatan(null, false);
            riwayat.setNoRm(noRm, TPasien.getText());
            riwayat.setSize(1200, 700);
            riwayat.setLocationRelativeTo(this);
            riwayat.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuka Riwayat Perawatan: " + ex.getMessage());
        }
    }

    // =========================================================================
    // HELPER LAYOUT & KOMPONEN UI SWING (PERSIS ARGON)
    // =========================================================================
    private JPanel createBaseCardPanel() {
        return new RoundedCardPanel(12);
    }

    private JPanel createSectionCard(String title, String subtitle) {
        JPanel card = createBaseCardPanel();
        card.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 2));
        titleStack.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_SECTION);
        lblTitle.setForeground(COL_TEXT_DARK);
        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_SUBLABEL);
        lblSub.setForeground(COL_TEXT_MUTED);
        titleStack.add(lblTitle);
        titleStack.add(lblSub);

        JLabel reqBadge = new JLabel("Wajib lengkap", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_TEAL_SOFT);
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.setColor(COL_TEAL_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        reqBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        reqBadge.setForeground(COL_TEAL_DARK);
        reqBadge.setOpaque(false);
        reqBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        header.add(titleStack, BorderLayout.WEST);
        header.add(reqBadge, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private void addHorizontalFormRow(
            JPanel container,
            int rowIndex,
            String mainLabel,
            String subLabel,
            JComponent rightComp,
            boolean showBottomBorder
    ) {
        JPanel rowPanel = new JPanel(new BorderLayout(16, 0));
        rowPanel.setOpaque(false);
        rowPanel.setBorder(new CompoundBorder(
                showBottomBorder ? new MatteBorder(0, 0, 1, 0, COL_ROW_DIVIDER) : new EmptyBorder(0, 0, 0, 0),
                new EmptyBorder(10, 0, 10, 0)
        ));

        JPanel leftLabelBox = new JPanel();
        leftLabelBox.setLayout(new BoxLayout(leftLabelBox, BoxLayout.Y_AXIS));
        leftLabelBox.setOpaque(false);
        leftLabelBox.setPreferredSize(new Dimension(LEFT_LABEL_WIDTH, 38));

        JLabel lblMain = new JLabel(mainLabel);
        lblMain.setFont(FONT_ROW_LABEL);
        lblMain.setForeground(COL_TEXT_DARK);
        lblMain.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftLabelBox.add(lblMain);

        if (subLabel != null && !subLabel.isEmpty()) {
            leftLabelBox.add(Box.createVerticalStrut(2));
            JLabel lblSub = new JLabel(subLabel);
            lblSub.setFont(FONT_SUBLABEL);
            lblSub.setForeground(COL_TEXT_MUTED);
            lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
            leftLabelBox.add(lblSub);
        }

        rowPanel.add(leftLabelBox, BorderLayout.WEST);
        rowPanel.add(rightComp, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = rowIndex;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        container.add(rowPanel, gbc);
    }

    private JTextField createTextField(String initialVal) {
        return new RoundedTextField(initialVal, 8);
    }

    private JTextArea createTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 20);
        ta.setFont(FONT_INPUT);
        ta.setForeground(COL_TEXT_DARK);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setOpaque(false);
        ta.setBorder(new EmptyBorder(6, 10, 6, 10));
        return ta;
    }

    private JScrollPane wrapTextArea(JTextArea ta, int prefHeight) {
        RoundedScrollPane sp = new RoundedScrollPane(ta, 8);
        sp.setPreferredSize(new Dimension(400, prefHeight));
        return sp;
    }

    private JComboBox<String> styleComboBox(JComboBox<String> cmb) {
        cmb.setFont(FONT_INPUT);
        cmb.setBackground(Color.WHITE);
        cmb.putClientProperty("Component.arc", 8);
        cmb.setBorder(new CompoundBorder(
                new RoundedBorder(COL_INPUT_BORDER, 8, 1.0f),
                new EmptyBorder(2, 6, 2, 6)
        ));
        return cmb;
    }

    private widget.Tanggal styleTanggal(widget.Tanggal tgl) {
        if (tgl == null) return null;
        tgl.setFont(FONT_INPUT);
        tgl.setForeground(COL_TEXT_DARK);
        tgl.setBackground(Color.WHITE);
        tgl.putClientProperty("Component.arc", 8);
        tgl.putClientProperty("ComboBox.arc", 8);
        tgl.putClientProperty("JComponent.roundRect", false);
        tgl.setBorder(new CompoundBorder(
                new RoundedBorder(COL_INPUT_BORDER, 8, 1.2f),
                new EmptyBorder(0, 10, 0, 8)
        ));
        return tgl;
    }

    private JPanel wrapFieldWithMicroHeader(String microHeaderText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel lbl = new JLabel(microHeaderText);
        lbl.setFont(FONT_MICRO_HEADER);
        lbl.setForeground(COL_TEXT_MUTED);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel createLookupFieldPanel(JTextField tf, Runnable onLookupClick) {
        JButton btn = new JButton(new SearchMagnifierIcon(13, COL_TEXT_MUTED));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Cari Data Master");
        btn.addActionListener(e -> onLookupClick.run());

        JPanel p = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.setColor(COL_BORDER);
                int divX = getWidth() - 34;
                g2.drawLine(divX, 6, divX, getHeight() - 6);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tf.hasFocus() ? COL_TEAL_PRIMARY : COL_INPUT_BORDER);
                g2.setStroke(new BasicStroke(tf.hasFocus() ? 1.5f : 1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        tf.setOpaque(false);
        tf.setBorder(new EmptyBorder(6, 10, 6, 6));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(6, 8, 6, 10));

        p.add(tf, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    private JButton createSearchButton(String text) {
        RoundedButton btn = new RoundedButton(text, Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER, 8);
        btn.setIcon(new SearchMagnifierIcon(12, COL_TEXT_DARK));
        btn.setIconTextGap(6);
        return btn;
    }

    private JPanel createFieldWithInlineUnit(JTextField tf, String unitText) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.add(tf, BorderLayout.CENTER);
        p.add(createInlineUnitLabel(unitText), BorderLayout.EAST);
        return p;
    }

    private JLabel createInlineUnitLabel(String unitText) {
        JLabel lbl = new JLabel(unitText);
        lbl.setFont(FONT_SUBLABEL);
        lbl.setForeground(COL_TEXT_MUTED);
        return lbl;
    }

    private JPanel createKeyValueRow(String key, JLabel lblV, boolean mono) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblK = new JLabel(key);
        lblK.setFont(FONT_SUBLABEL);
        lblK.setForeground(COL_TEXT_MUTED);
        lblV.setFont(mono ? FONT_MONO : new Font("Segoe UI", Font.BOLD, 11));
        lblV.setForeground(COL_TEXT_DARK);
        row.add(lblK, BorderLayout.WEST);
        row.add(lblV, BorderLayout.EAST);
        return row;
    }

    private JButton createFlatButton(String text, Color bg, Color fg, Color borderCol) {
        return new RoundedButton(text, bg, fg, borderCol, 8);
    }

    // =========================================================================
    // KOMPONEN KUSTOM: SEGMENTED BUTTON GROUP (PERSIS ARGON & REKOMENDASI USER)
    // =========================================================================
    public static class SegmentedButtonGroup extends JPanel {
        private final Map<String, SegmentedOptionButton> buttons = new LinkedHashMap<>();
        private String selectedValue;

        public SegmentedButtonGroup(String[] options, String defaultVal) {
            super(new GridLayout(1, options.length, 3, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(3, 3, 3, 3));
            setPreferredSize(new Dimension(0, 36));
            for (String opt : options) {
                SegmentedOptionButton btn = new SegmentedOptionButton(opt);
                btn.addActionListener(e -> setSelectedValue(opt));
                buttons.put(opt, btn);
                add(btn);
            }
            setSelectedValue(defaultVal);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 1. Background Track Container (#F1F5F9)
            g2.setColor(new Color(241, 245, 249));
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
            // 2. Border Track Container (#E2E8F0)
            g2.setColor(new Color(226, 232, 240));
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
            g2.dispose();
            super.paintComponent(g);
        }

        public void setSelectedValue(String val) {
            this.selectedValue = val;
            for (Map.Entry<String, SegmentedOptionButton> entry : buttons.entrySet()) {
                entry.getValue().setSelectedState(entry.getKey().equalsIgnoreCase(val));
            }
        }

        public String getSelectedValue() {
            return selectedValue;
        }

        public Map<String, SegmentedOptionButton> getButtons() {
            return buttons;
        }
    }

    public static class SegmentedOptionButton extends JButton {
        private boolean isOptionSelected = false;
        private static final Color COL_SEL_BG = new Color(13, 148, 136); // #0D9488 Teal Solid
        private static final Color COL_SEL_FG = Color.WHITE;
        private static final Color COL_UNSEL_FG = new Color(71, 85, 105); // #475569 Slate 600
        private static final Color COL_HOVER_BG = new Color(226, 232, 240); // #E2E8F0 Slate 200

        public SegmentedOptionButton(String text) {
            super(text);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(6, 10, 6, 10));
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setForeground(COL_UNSEL_FG);
        }

        public void setSelectedState(boolean sel) {
            this.isOptionSelected = sel;
            if (sel) {
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setForeground(COL_SEL_FG);
            } else {
                setFont(new Font("Segoe UI", Font.PLAIN, 11));
                setForeground(COL_UNSEL_FG);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isOptionSelected) {
                g2.setColor(COL_SEL_BG);
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 6, 6));
            } else if (getModel().isRollover()) {
                g2.setColor(COL_HOVER_BG);
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 6, 6));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================================================================
    // KOMPONEN KUSTOM ROUNDED BORDER, INPUT, SCROLLPANE, BUTTON, & CARD
    // =========================================================================

    public static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int radius;
        private final float strokeWidth;

        public RoundedBorder(Color color, int radius) {
            this(color, radius, 1.0f);
        }

        public RoundedBorder(Color color, int radius, float strokeWidth) {
            this.color = color;
            this.radius = radius;
            this.strokeWidth = strokeWidth;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(strokeWidth));
            float pad = strokeWidth / 2.0f;
            g2.draw(new RoundRectangle2D.Float(x + pad, y + pad, width - strokeWidth, height - strokeWidth, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int p = Math.max(4, (int) Math.ceil(strokeWidth) + 2);
            return new Insets(p, p, p, p);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            int p = Math.max(4, (int) Math.ceil(strokeWidth) + 2);
            insets.left = insets.top = insets.right = insets.bottom = p;
            return insets;
        }
    }

    public static class RoundedTextField extends JTextField {
        private final int arc;
        private Color borderColor = COL_INPUT_BORDER;

        public RoundedTextField(String text) {
            this(text, 8);
        }

        public RoundedTextField(String text, int arc) {
            super(text);
            this.arc = arc;
            setOpaque(false);
            setFont(FONT_INPUT);
            setForeground(COL_TEXT_DARK);
            setBorder(new EmptyBorder(6, 10, 6, 10));
            putClientProperty("Component.arc", arc);
            putClientProperty("TextComponent.arc", arc);
        }

        public void setBorderColor(Color c) {
            this.borderColor = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? (getBackground() != null ? getBackground() : Color.WHITE) : new Color(241, 245, 249));
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hasFocus() ? COL_TEAL_PRIMARY : borderColor);
            g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));
            g2.dispose();
        }
    }

    public static class RoundedScrollPane extends JScrollPane {
        private final int arc;
        private Color borderColor = COL_INPUT_BORDER;

        public RoundedScrollPane(Component view, int arc) {
            super(view);
            this.arc = arc;
            setOpaque(false);
            getViewport().setOpaque(false);
            setBorder(new EmptyBorder(2, 2, 2, 2));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));
            g2.dispose();
        }
    }

    public static class RoundedButton extends JButton {
        private final int arc;
        private Color normalBg;
        private Color borderCol;

        public RoundedButton(String text, Color bg, Color fg, Color borderCol, int arc) {
            super(text);
            this.normalBg = bg;
            this.borderCol = borderCol;
            this.arc = arc;
            setForeground(fg);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setBorder(new EmptyBorder(7, 14, 7, 14));
            putClientProperty("Component.arc", arc);
        }

        public void setColors(Color bg, Color fg, Color border) {
            this.normalBg = bg;
            this.borderCol = border;
            setForeground(fg);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!isEnabled()) {
                g2.setColor(new Color(241, 245, 249));
            } else if (getModel().isPressed()) {
                g2.setColor(normalBg.darker());
            } else if (getModel().isRollover()) {
                g2.setColor(blendColor(normalBg, Color.WHITE, 0.15f));
            } else {
                g2.setColor(normalBg);
            }
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            if (borderCol != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? borderCol : new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));
                g2.dispose();
            }
        }
    }

    public static class RoundedCardPanel extends JPanel {
        private final int arc;
        private Color borderColor = COL_BORDER;

        public RoundedCardPanel() {
            this(12);
        }

        public RoundedCardPanel(int arc) {
            this.arc = arc;
            setOpaque(false);
            setBackground(COL_CARD_BG);
        }

        public void setBorderColor(Color c) {
            this.borderColor = c;
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Gambar latar kartu rounded
            g2.setColor(getBackground() != null ? getBackground() : COL_CARD_BG);
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));

            // 2. Set clipping rounded untuk anak komponen (header/body tidak bocor sudut)
            Graphics2D gChildren = (Graphics2D) g.create();
            gChildren.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gChildren.setClip(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));
            super.paint(gChildren);
            gChildren.dispose();

            // 3. Gambar border kartu di PALING AKHIR di atas semua anak komponen
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth() - 1.2f, getHeight() - 1.2f, arc, arc));
            g2.dispose();
        }
    }

    private static Color blendColor(Color c1, Color c2, float ratio) {
        float r = (1 - ratio) * c1.getRed() + ratio * c2.getRed();
        float g = (1 - ratio) * c1.getGreen() + ratio * c2.getGreen();
        float b = (1 - ratio) * c1.getBlue() + ratio * c2.getBlue();
        return new Color((int) Math.min(255, Math.max(0, r)),
                         (int) Math.min(255, Math.max(0, g)),
                         (int) Math.min(255, Math.max(0, b)));
    }

    // =========================================================================
    // KOMPONEN KUSTOM: PANEL SCROLLABLE FULL-WIDTH AGAR TIDAK MENYEMPIT
    // =========================================================================
    private static class ScrollableTrackWidthPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 90;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // =========================================================================
    // KOMPONEN KUSTOM: IKON KACA PEMBESAR VEKTOR
    // =========================================================================
    private static class SearchMagnifierIcon implements Icon {
        private final int size;
        private final Color color;

        public SearchMagnifierIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int r = (int) (size * 0.65);
            g2.drawOval(x, y, r, r);
            g2.drawLine(x + r - 1, y + r - 1, x + size - 1, y + size - 1);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    private static class SimpleDocListener implements DocumentListener {
        private final Runnable callback;

        public SimpleDocListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            callback.run();
        }
    }

    // =========================================================================
    // OPERASI BASIS DATA & LOGIKA SIMRS KHANZA
    // =========================================================================

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        isRawat();

        int adaAsesmen = Sequel.cariInteger("select count(*) from asesmen_medis_ranap where no_rawat=?", norwt);
        if (adaAsesmen > 0) {
            muatData(norwt);
        } else {
            tarikDataAdmisi(norwt, false);
        }
        updateProgressCalculation();
    }

    public void isRawat() {
        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi,"
                    + "kamar.kd_kamar,bangsal.nm_bangsal "
                    + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "left join kamar_inap on reg_periksa.no_rawat=kamar_inap.no_rawat "
                    + "left join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "
                    + "left join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "
                    + "where reg_periksa.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));

                    String bangsal = rs.getString("nm_bangsal");
                    String kdKamar = rs.getString("kd_kamar");
                    String lokasi = (bangsal != null ? bangsal : "") + (kdKamar != null ? " (" + kdKamar + ")" : "");
                    if (lokasi.trim().isEmpty()) {
                        lokasi = "-";
                    }

                    // Sinkronkan ke Card Pasien Kiri
                    lblPatientName.setText(TPasien.getText());
                    lblValRM.setText(TNoRM.getText());
                    lblValRawat.setText(TNoRw.getText());
                    lblValTglMasuk.setText(new SimpleDateFormat("dd-MM-yyyy").format(rs.getDate("tgl_registrasi")));
                    lblValKamar.setText(lokasi);

                    // Avatar inisial
                    String nama = TPasien.getText().trim();
                    StringBuilder inisial = new StringBuilder();
                    for (String b : nama.split("\\s+")) {
                        if (!b.isEmpty() && inisial.length() < 2) {
                            inisial.append(Character.toUpperCase(b.charAt(0)));
                        }
                    }
                    lblAvatarInisial.setText(inisial.length() == 0 ? "PS" : inisial.toString());

                    // Hitung Umur
                    try {
                        LocalDate lahir = LocalDate.parse(TglLahir.getText());
                        LocalDate skrg = LocalDate.now();
                        Period p = Period.between(lahir, skrg);
                        String umurStr = p.getYears() + " Th " + p.getMonths() + " Bl";
                        txtUmur.setText(p.getYears() + " Th");
                        lblGenderAge.setText(Jk.getText() + " • " + umurStr);
                        lblValLahir.setText(new SimpleDateFormat("dd-MM-yyyy").format(rs.getDate("tgl_lahir")) + " (" + p.getYears() + " Th)");
                    } catch (Exception ex) {
                        lblGenderAge.setText(Jk.getText());
                        lblValLahir.setText(TglLahir.getText());
                    }
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi isRawat : " + e);
        }
    }

    public void muatData(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return;
        }
        try {
            ps = koneksi.prepareStatement(
                    "select asesmen_medis_ranap.*,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                    + "dokterperiksa.nm_dokter as nm_dokter_periksa,dokterdpjp.nm_dokter as nm_dokter_dpjp "
                    + "from asesmen_medis_ranap "
                    + "inner join reg_periksa on asesmen_medis_ranap.no_rawat=reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "left join dokter dokterperiksa on asesmen_medis_ranap.kd_dokter_periksa=dokterperiksa.kd_dokter "
                    + "left join dokter dokterdpjp on asesmen_medis_ranap.kd_dokter_dpjp=dokterdpjp.kd_dokter "
                    + "where asesmen_medis_ranap.no_rawat=?");
            try {
                ps.setString(1, noRawat);
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRw.setText(rs.getString("no_rawat"));
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    Jk.setText(rs.getString("jk"));
                    Valid.SetTgl(TglAsuhan, rs.getString("tanggal"));
                    KdDokterPeriksa.setText(rs.getString("kd_dokter_periksa"));
                    txtDokterPemeriksa.setText(rs.getString("nm_dokter_periksa"));
                    KdDokterDpjp.setText(rs.getString("kd_dokter_dpjp"));
                    txtDpjp.setText(rs.getString("nm_dokter_dpjp"));

                    txtKeluhanUtama.setText(rs.getString("keluhan_utama"));
                    txtKeluhanTambahan.setText(rs.getString("keluhan_tambahan"));

                    // Alergi
                    String al = rs.getString("alergi");
                    if (al != null && al.contains(";")) {
                        String[] parts = al.split(";", 2);
                        txtAlergiObat.setText(parts[0].replace("Obat:", "").trim());
                        txtAlergiMakanan.setText(parts[1].replace("Makanan:", "").trim());
                    } else {
                        txtAlergiObat.setText(al != null ? al : "");
                        txtAlergiMakanan.setText("");
                    }

                    // Anamnesis
                    String sumber = rs.getString("sumber_riwayat");
                    if ("Keluarga".equalsIgnoreCase(sumber)) {
                        cmbAnamnesis.setSelectedItem("Alloanamnesis");
                    } else {
                        cmbAnamnesis.setSelectedItem("Autoanamnesis");
                    }

                    // Riwayat Penyakit & Pengobatan
                    String riw = rs.getString("riwayat_penyakit_pengobatan");
                    if (riw != null && riw.contains("Riwayat Pengobatan:")) {
                        String[] p = riw.split("Riwayat Pengobatan:", 2);
                        txtRiwayatPerjalananPenyakit.setText(p[0].replace("Perjalanan Penyakit:", "").trim());
                        txtRiwayatPengobatan.setText(p[1].trim());
                    } else {
                        txtRiwayatPerjalananPenyakit.setText(riw != null ? riw : "");
                        txtRiwayatPengobatan.setText("");
                    }

                    // Pemeriksaan Fisik
                    segKeadaanUmum.setSelectedValue(rs.getString("keadaan_umum"));
                    segKesadaranMental.setSelectedValue(rs.getString("kesadaran_mental"));
                    txtGcsE.setText(rs.getString("gcs_e"));
                    txtGcsM.setText(rs.getString("gcs_m"));
                    txtGcsV.setText(rs.getString("gcs_v"));
                    txtTdSistol.setText(rs.getString("td_sistol"));
                    txtTdDiastol.setText(rs.getString("td_diastol"));
                    txtNadi.setText(rs.getString("nadi"));
                    txtRr.setText(rs.getString("rr"));
                    txtSuhu.setText(rs.getString("suhu"));
                    txtTb.setText(rs.getString("tb"));
                    txtBb.setText(rs.getString("bb"));
                    txtUmur.setText(rs.getString("umur"));
                    txtImt.setText(rs.getString("imt"));
                    segStatusPediatricGizi.setSelectedValue(rs.getString("status_pediatrik"));

                    txtDiagnosaKerja1.setText(rs.getString("diagnosa_kerja_1"));
                    txtDiagnosaKerja2.setText(rs.getString("diagnosa_kerja_2"));
                    txtDiagnosaKerja3.setText(rs.getString("diagnosa_kerja_3"));

                    // Rencana & Terapi
                    String renc = rs.getString("rencana_terapi");
                    if (renc != null && renc.contains("Pengobatan & Terapi:")) {
                        String[] r = renc.split("Pengobatan & Terapi:", 2);
                        txtRencanaPenunjang.setText(r[0].replace("Pemeriksaan Penunjang:", "").trim());
                        txtRencanaTerapi.setText(r[1].trim());
                    } else {
                        txtRencanaPenunjang.setText("");
                        txtRencanaTerapi.setText(renc != null ? renc : "");
                    }

                    lblSavedBadge.setText("Tersimpan");
                    lblSavedBadge.setBackground(new Color(209, 250, 229));
                    lblSavedBadge.setForeground(new Color(4, 120, 87));

                    updateGcsCalculation();
                    updateImtCalculation();
                    updateProgressCalculation();
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi muatData : " + e);
        }
    }

    public void emptTeks() {
        txtKeluhanUtama.setText("");
        txtKeluhanTambahan.setText("");
        txtAlergiObat.setText("");
        txtAlergiMakanan.setText("");
        txtRiwayatPerjalananPenyakit.setText("");
        txtRiwayatPengobatan.setText("");
        txtDiagnosaKerja1.setText("");
        txtDiagnosaKerja2.setText("");
        txtDiagnosaKerja3.setText("");
        txtRencanaPenunjang.setText("");
        txtRencanaTerapi.setText("");

        txtGcsE.setText("4");
        txtGcsM.setText("6");
        txtGcsV.setText("5");
        txtTdSistol.setText("120");
        txtTdDiastol.setText("80");
        txtNadi.setText("80");
        txtRr.setText("20");
        txtSuhu.setText("36.5");
        txtSpo2.setText("98");

        segKeadaanUmum.setSelectedValue("Sakit Sedang");
        segKesadaranMental.setSelectedValue("Compos Mentis");
        segStatusPediatricGizi.setSelectedValue("Baik");

        currentModeInput = ModeInput.MODE_INPUT_BARU;
        noRawatSedangDiedit = "";
        if (pnlBannerEdit != null) {
            pnlBannerEdit.setVisible(false);
        }

        if (lblSavedBadge != null) {
            lblSavedBadge.setText("RM 20 Baru");
            lblSavedBadge.setBackground(COL_TEAL_SOFT);
            lblSavedBadge.setForeground(COL_TEAL_DARK);
        }

        TglAsuhan.setDate(new Date());
        updateGcsCalculation();
        updateImtCalculation();
        updateProgressCalculation();
        txtKeluhanUtama.requestFocus();
    }

    public void tarikDataAdmisi(String noRawat, boolean konfirmasiManual) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            if (konfirmasiManual) JOptionPane.showMessageDialog(this, "No. Rawat masih kosong!");
            return;
        }

        if (konfirmasiManual) {
            int opsi = JOptionPane.showConfirmDialog(this,
                    "Tarik data TTV, keluhan, dan riwayat dari Pengkajian Admisi (Keperawatan / IGD / Ralan)?\n"
                    + "Field yang sudah terisi akan diperbarui dengan data admisi awal pasien.",
                    "Konfirmasi Tarik Data Admisi", JOptionPane.YES_NO_OPTION);
            if (opsi != JOptionPane.YES_OPTION) return;
        }

        boolean berhasil = false;
        String sumberDitemukan = "";

        // 1. Tarik dari RM 19 (Keperawatan Ranap)
        try (PreparedStatement psKep = koneksi.prepareStatement(
                "select * from penilaian_awal_keperawatan_ranap where no_rawat=? order by tanggal desc limit 1")) {
            psKep.setString(1, noRawat);
            try (ResultSet rsKep = psKep.executeQuery()) {
                if (rsKep.next()) {
                    String rps = rsKep.getString("rps");
                    if (rps != null && !rps.trim().isEmpty()) txtKeluhanUtama.setText(rps);

                    String rpd = rsKep.getString("rpd");
                    String rpo = rsKep.getString("rpo");
                    if (rpd != null || rpo != null) {
                        txtRiwayatPerjalananPenyakit.setText(rpd != null ? rpd : "");
                        txtRiwayatPengobatan.setText(rpo != null ? rpo : "");
                    }

                    String alergi = rsKep.getString("riwayat_alergi");
                    if (alergi != null && !alergi.trim().isEmpty()) txtAlergiObat.setText(alergi);

                    String td = rsKep.getString("pemeriksaan_td");
                    if (td != null && td.contains("/")) {
                        String[] splitTd = td.split("/");
                        if (splitTd.length >= 2) {
                            txtTdSistol.setText(splitTd[0].trim());
                            txtTdDiastol.setText(splitTd[1].trim());
                        }
                    }
                    String nadi = rsKep.getString("pemeriksaan_nadi");
                    if (nadi != null && !nadi.trim().isEmpty()) txtNadi.setText(nadi.trim());
                    String rr = rsKep.getString("pemeriksaan_rr");
                    if (rr != null && !rr.trim().isEmpty()) txtRr.setText(rr.trim());
                    String suhu = rsKep.getString("pemeriksaan_suhu");
                    if (suhu != null && !suhu.trim().isEmpty()) txtSuhu.setText(suhu.trim());
                    String tbVal = rsKep.getString("pemeriksaan_tb");
                    if (tbVal != null && !tbVal.trim().isEmpty()) txtTb.setText(tbVal.trim());
                    String bbVal = rsKep.getString("pemeriksaan_bb");
                    if (bbVal != null && !bbVal.trim().isEmpty()) txtBb.setText(bbVal.trim());

                    String ku = rsKep.getString("pemeriksaan_keadaan_umum");
                    if (ku != null && !ku.trim().isEmpty()) segKeadaanUmum.setSelectedValue(ku);
                    String kes = rsKep.getString("pemeriksaan_mental");
                    if (kes != null && !kes.trim().isEmpty()) segKesadaranMental.setSelectedValue(kes);

                    berhasil = true;
                    sumberDitemukan = "Pengkajian Awal Keperawatan Ranap (RM 19)";
                }
            }
        } catch (Exception ignored) {}

        // 2. Fallback IGD
        if (!berhasil) {
            try (PreparedStatement psIgd = koneksi.prepareStatement(
                    "select * from penilaian_medis_igd where no_rawat=? order by tanggal desc limit 1")) {
                psIgd.setString(1, noRawat);
                try (ResultSet rsIgd = psIgd.executeQuery()) {
                    if (rsIgd.next()) {
                        String kel = rsIgd.getString("keluhan_utama");
                        if (kel != null && !kel.trim().isEmpty()) txtKeluhanUtama.setText(kel);
                        String td = rsIgd.getString("td");
                        if (td != null && td.contains("/")) {
                            String[] s = td.split("/");
                            if (s.length >= 2) {
                                txtTdSistol.setText(s[0].trim());
                                txtTdDiastol.setText(s[1].trim());
                            }
                        }
                        String nd = rsIgd.getString("nadi");
                        if (nd != null && !nd.trim().isEmpty()) txtNadi.setText(nd.trim());
                        String rr = rsIgd.getString("rr");
                        if (rr != null && !rr.trim().isEmpty()) txtRr.setText(rr.trim());
                        String sh = rsIgd.getString("suhu");
                        if (sh != null && !sh.trim().isEmpty()) txtSuhu.setText(sh.trim());
                        String tbVal = rsIgd.getString("tb");
                        if (tbVal != null && !tbVal.trim().isEmpty()) txtTb.setText(tbVal.trim());
                        String bbVal = rsIgd.getString("bb");
                        if (bbVal != null && !bbVal.trim().isEmpty()) txtBb.setText(bbVal.trim());

                        berhasil = true;
                        sumberDitemukan = "Penilaian Medis IGD";
                    }
                }
            } catch (Exception ignored) {}
        }

        if (berhasil) {
            updateGcsCalculation();
            updateImtCalculation();
            updateProgressCalculation();
            if (konfirmasiManual) {
                JOptionPane.showMessageDialog(this, "Data berhasil ditarik dari " + sumberDitemukan + "!");
            }
        } else if (konfirmasiManual) {
            JOptionPane.showMessageDialog(this, "Belum ada data admisi/TTV awal untuk pasien ini.");
        }
    }

    private void simpan() {
        if (TNoRw.getText().trim().isEmpty() || TPasien.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silahkan pilih data pasien terlebih dahulu!");
            return;
        }
        if (KdDokterPeriksa.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silahkan pilih Dokter Yang Memeriksa terlebih dahulu!");
            bukaCariDokter(false);
            return;
        }
        if (KdDokterDpjp.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silahkan pilih Dokter DPJP terlebih dahulu!");
            bukaCariDokter(true);
            return;
        }
        if (txtKeluhanUtama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keluhan Utama wajib diisi!");
            txtKeluhanUtama.requestFocus();
            return;
        }

        String tglAsuhanStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(TglAsuhan.getDate());
        String alergiGabung = txtAlergiObat.getText().trim() + (txtAlergiMakanan.getText().trim().isEmpty() ? "" : "; " + txtAlergiMakanan.getText().trim());
        String sumberRiw = cmbAnamnesis.getSelectedItem().toString().contains("Allo") ? "Keluarga" : "Sendiri";
        String riwPenyakitPengobatan = (txtRiwayatPerjalananPenyakit.getText().trim().isEmpty() ? "" : "Perjalanan Penyakit:\n" + txtRiwayatPerjalananPenyakit.getText().trim() + "\n\n")
                + (txtRiwayatPengobatan.getText().trim().isEmpty() ? "" : "Riwayat Pengobatan:\n" + txtRiwayatPengobatan.getText().trim());
        String rencanaTerapiGabung = (txtRencanaPenunjang.getText().trim().isEmpty() ? "" : "Pemeriksaan Penunjang:\n" + txtRencanaPenunjang.getText().trim() + "\n\n")
                + (txtRencanaTerapi.getText().trim().isEmpty() ? "" : "Pengobatan & Terapi:\n" + txtRencanaTerapi.getText().trim());

        String[] nilai = new String[]{
            tglAsuhanStr,
            KdDokterPeriksa.getText().trim(),
            KdDokterDpjp.getText().trim(),
            txtKeluhanUtama.getText().trim(),
            txtKeluhanTambahan.getText().trim(),
            alergiGabung,
            sumberRiw,
            riwPenyakitPengobatan,
            segKeadaanUmum.getSelectedValue(),
            segKesadaranMental.getSelectedValue(),
            txtGcsE.getText().trim(),
            txtGcsM.getText().trim(),
            txtGcsV.getText().trim(),
            txtTdSistol.getText().trim(),
            txtTdDiastol.getText().trim(),
            txtNadi.getText().trim(),
            txtRr.getText().trim(),
            txtSuhu.getText().trim(),
            txtTb.getText().trim(),
            txtBb.getText().trim(),
            txtUmur.getText().trim(),
            txtImt.getText().trim(),
            segStatusPediatricGizi.getSelectedValue(),
            txtDiagnosaKerja1.getText().trim(),
            txtDiagnosaKerja2.getText().trim(),
            txtDiagnosaKerja3.getText().trim(),
            rencanaTerapiGabung
        };

        StringBuilder sqlInsert = new StringBuilder("insert into asesmen_medis_ranap (no_rawat,");
        for (int k = 0; k < KOLOM_RM20.length; k++) {
            sqlInsert.append(KOLOM_RM20[k]).append(k < KOLOM_RM20.length - 1 ? "," : ") values (?,");
        }
        for (int k = 0; k < KOLOM_RM20.length; k++) {
            sqlInsert.append("?").append(k < KOLOM_RM20.length - 1 ? "," : ")");
        }

        try {
            ps = koneksi.prepareStatement(sqlInsert.toString());
            ps.setString(1, TNoRw.getText().trim());
            for (int k = 0; k < nilai.length; k++) {
                ps.setString(k + 2, nilai[k]);
            }
            if (ps.executeUpdate() > 0) {
                String timeStr = new SimpleDateFormat("HH:mm").format(new Date());
                lblLastSaved.setText("Tersimpan otomatis (" + timeStr + " WIB)");
                lblSavedBadge.setText("Tersimpan");
                lblSavedBadge.setBackground(new Color(209, 250, 229));
                lblSavedBadge.setForeground(new Color(4, 120, 87));
                JOptionPane.showMessageDialog(this, "Data Assesmen Medis Rawat Inap (RM 20) berhasil disimpan!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
                tampil();
            }
        } catch (Exception ex) {
            if (ex.getMessage().contains("Duplicate")) {
                int opsi = JOptionPane.showConfirmDialog(this,
                        "Data asesmen untuk nomor rawat ini sudah ada!\nApakah Anda ingin memperbarui (mengganti) data tersebut?",
                        "Data Sudah Ada", JOptionPane.YES_NO_OPTION);
                if (opsi == JOptionPane.YES_OPTION) {
                    aksiGanti();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
    }

    private void aksiGanti() {
        if (TNoRw.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silahkan pilih data yang mau diganti!");
            return;
        }

        String tglAsuhanStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(TglAsuhan.getDate());
        String alergiGabung = txtAlergiObat.getText().trim() + (txtAlergiMakanan.getText().trim().isEmpty() ? "" : "; " + txtAlergiMakanan.getText().trim());
        String sumberRiw = cmbAnamnesis.getSelectedItem().toString().contains("Allo") ? "Keluarga" : "Sendiri";
        String riwPenyakitPengobatan = (txtRiwayatPerjalananPenyakit.getText().trim().isEmpty() ? "" : "Perjalanan Penyakit:\n" + txtRiwayatPerjalananPenyakit.getText().trim() + "\n\n")
                + (txtRiwayatPengobatan.getText().trim().isEmpty() ? "" : "Riwayat Pengobatan:\n" + txtRiwayatPengobatan.getText().trim());
        String rencanaTerapiGabung = (txtRencanaPenunjang.getText().trim().isEmpty() ? "" : "Pemeriksaan Penunjang:\n" + txtRencanaPenunjang.getText().trim() + "\n\n")
                + (txtRencanaTerapi.getText().trim().isEmpty() ? "" : "Pengobatan & Terapi:\n" + txtRencanaTerapi.getText().trim());

        String[] nilai = new String[]{
            tglAsuhanStr,
            KdDokterPeriksa.getText().trim(),
            KdDokterDpjp.getText().trim(),
            txtKeluhanUtama.getText().trim(),
            txtKeluhanTambahan.getText().trim(),
            alergiGabung,
            sumberRiw,
            riwPenyakitPengobatan,
            segKeadaanUmum.getSelectedValue(),
            segKesadaranMental.getSelectedValue(),
            txtGcsE.getText().trim(),
            txtGcsM.getText().trim(),
            txtGcsV.getText().trim(),
            txtTdSistol.getText().trim(),
            txtTdDiastol.getText().trim(),
            txtNadi.getText().trim(),
            txtRr.getText().trim(),
            txtSuhu.getText().trim(),
            txtTb.getText().trim(),
            txtBb.getText().trim(),
            txtUmur.getText().trim(),
            txtImt.getText().trim(),
            segStatusPediatricGizi.getSelectedValue(),
            txtDiagnosaKerja1.getText().trim(),
            txtDiagnosaKerja2.getText().trim(),
            txtDiagnosaKerja3.getText().trim(),
            rencanaTerapiGabung
        };

        StringBuilder sqlUpdate = new StringBuilder("update asesmen_medis_ranap set ");
        for (int k = 0; k < KOLOM_RM20.length; k++) {
            sqlUpdate.append(KOLOM_RM20[k]).append("=?").append(k < KOLOM_RM20.length - 1 ? "," : " ");
        }
        sqlUpdate.append("where no_rawat=?");

        try {
            ps = koneksi.prepareStatement(sqlUpdate.toString());
            for (int k = 0; k < nilai.length; k++) {
                ps.setString(k + 1, nilai[k]);
            }
            ps.setString(nilai.length + 1, TNoRw.getText().trim());
            if (ps.executeUpdate() > 0) {
                String timeStr = new SimpleDateFormat("HH:mm").format(new Date());
                lblLastSaved.setText("Tersimpan otomatis (" + timeStr + " WIB)");
                JOptionPane.showMessageDialog(this, "Data Assesmen Medis Rawat Inap (RM 20) berhasil diperbarui!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
                tampil();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
    }

    private void aksiHapus() {
        if (tbObat.getSelectedRow() > -1) {
            String kdPemeriksa = tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString();
            if (akses.getkode().equals("Admin Utama") || KdDokterPeriksa.getText().equals(kdPemeriksa)) {
                int opsi = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus data asesmen terpilih?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
                if (opsi == JOptionPane.YES_OPTION) {
                    if (Sequel.queryu2tf("delete from asesmen_medis_ranap where no_rawat=?", 1, new String[]{
                        tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
                    })) {
                        tabMode.removeRow(tbObat.getSelectedRow());
                        LCount.setText(tabMode.getRowCount() + " Rekam Data");
                        emptTeks();
                        JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal menghapus data!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Hanya dokter yang memeriksa yang berhak menghapus data!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Silahkan pilih data terlebih dahulu pada tabel riwayat!");
        }
    }

    private String sqlDaftar(boolean pakaiCari) {
        String sql =
                "select asesmen_medis_ranap.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,asesmen_medis_ranap.kd_dokter_periksa,"
                + "dokterperiksa.nm_dokter as nm_dokter_periksa,dokterdpjp.nm_dokter as nm_dokter_dpjp,"
                + "asesmen_medis_ranap.tanggal,asesmen_medis_ranap.diagnosa_kerja_1 "
                + "from asesmen_medis_ranap "
                + "inner join reg_periksa on asesmen_medis_ranap.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join dokter dokterperiksa on asesmen_medis_ranap.kd_dokter_periksa=dokterperiksa.kd_dokter "
                + "left join dokter dokterdpjp on asesmen_medis_ranap.kd_dokter_dpjp=dokterdpjp.kd_dokter "
                + "where asesmen_medis_ranap.tanggal between ? and ? ";
        if (pakaiCari) {
            sql += "and (asesmen_medis_ranap.no_rawat like ? or pasien.no_rkm_medis like ? "
                    + "or pasien.nm_pasien like ? or dokterperiksa.nm_dokter like ? "
                    + "or dokterdpjp.nm_dokter like ?) ";
        }
        return sql + "order by asesmen_medis_ranap.tanggal desc";
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        boolean pakaiCari = !TCari.getText().trim().isEmpty();
        try {
            ps = koneksi.prepareStatement(sqlDaftar(pakaiCari));
            try {
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                if (pakaiCari) {
                    String cari = "%" + TCari.getText().trim() + "%";
                    for (int k = 3; k <= 7; k++) {
                        ps.setString(k, cari);
                    }
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"), rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"), rs.getString("tgl_lahir"),
                        rs.getString("jk"), rs.getString("kd_dokter_periksa"),
                        rs.getString("nm_dokter_periksa"), rs.getString("nm_dokter_dpjp"),
                        rs.getString("tanggal"), rs.getString("diagnosa_kerja_1")
                    });
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tampil : " + e);
        }
        LCount.setText(tabMode.getRowCount() + " Rekam Data");
    }

    private void getData() {
        if (tbObat.getSelectedRow() == -1) return;
        muatData(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
    }

    private void cetakTerpilih() {
        if (tbObat.getSelectedRow() == -1) {
            if (!TNoRw.getText().trim().isEmpty()) {
                cetakSatu(TNoRw.getText().trim());
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih data yang mau dicetak pada tabel riwayat!");
            }
            return;
        }
        cetakSatu(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
    }

    public void cetakSatu(String noRawat) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

        String kdPeriksa = KdDokterPeriksa.getText().trim();
        String kdDpjp = KdDokterDpjp.getText().trim();
        String tglStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(TglAsuhan.getDate());

        param.put("finger_periksa", tandaTangan(kdPeriksa, txtDokterPemeriksa.getText().trim(), tglStr));
        param.put("finger_dpjp", tandaTangan(kdDpjp, txtDpjp.getText().trim(), tglStr));

        String qry = "select asesmen_medis_ranap.*,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                + "reg_periksa.tgl_registrasi,reg_periksa.jam_reg,"
                + "dokterperiksa.nm_dokter as nm_dokter_periksa,dokterperiksa.no_ijn_praktek as ijin_periksa,"
                + "dokterdpjp.nm_dokter as nm_dokter_dpjp,dokterdpjp.no_ijn_praktek as ijin_dpjp "
                + "from asesmen_medis_ranap "
                + "inner join reg_periksa on asesmen_medis_ranap.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join dokter dokterperiksa on asesmen_medis_ranap.kd_dokter_periksa=dokterperiksa.kd_dokter "
                + "left join dokter dokterdpjp on asesmen_medis_ranap.kd_dokter_dpjp=dokterdpjp.kd_dokter "
                + "where asesmen_medis_ranap.no_rawat='" + noRawat + "'";

        Valid.MyReportqry("rptAsesmenMedisRanapRM20.jasper", "report", "::[ Asesmen Medis Rawat Inap (RM 20) ]::", qry, param);
        this.setCursor(Cursor.getDefaultCursor());
    }

    private String tandaTangan(String kdDokter, String nmDokter, String tanggal) {
        String finger = Sequel.cariIsi(
                "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id "
                + "where pegawai.nik=?", kdDokter);
        return "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                + "\nDitandatangani secara elektronik oleh " + nmDokter
                + "\nID " + (finger == null || finger.isEmpty() ? kdDokter : finger)
                + "\n" + Valid.SetTgl3(tanggal);
    }

    public void isCek() {
        BtnSimpan.setEnabled(akses.getasesmen_medis_ranap());
        BtnHapus.setEnabled(akses.getasesmen_medis_ranap());
        BtnEdit.setEnabled(akses.getasesmen_medis_ranap());
        if (akses.getjml2() >= 1) {
            KdDokterPeriksa.setText(akses.getkode());
            txtDokterPemeriksa.setText(Sequel.CariDokter(KdDokterPeriksa.getText()));
        }
    }

    public void setTampil() {
        mainViewCardLayout.show(mainViewContainer, "HISTORY");
        styleTabButton(btnTabHistory, true);
        styleTabButton(btnTabForm, false);
        tampil();
    }
}
