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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDate;
import java.time.Period;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPetugas;
import laporan.DlgCariPenyakit;
import widget.IconStyle;
import widget.ThemeConfig;

/**
 * TRANSFER PASIEN ANTAR RUANGAN (RM 04-1 & RM 04-2)
 * RSUD Demang Sepulau Raya.
 *
 * Menerapkan Desain Sistem Modern Argon:
 * - Satu Halaman Penuh Vertikal Scroll (7 Seksi modular mengalir dari atas ke
 * bawah).
 * - Sticky Sidebar Navigasi Kiri: Kartu Pasien di posisi paling atas, live
 * indikator
 * kelengkapan, 7 tombol navigasi cepat lompat seksi, dan tombol contekan SIMRS.
 * - Judul bersih tanpa tanda kurung sesuai aturan baku workspace.
 * - Hierarki border rounded: Card arc=12px, Input/Combo/Btn arc=8px, Pill Radio
 * arc=6px.
 * - Tombol dengan rollover dan efek hover interaktif di seluruh kontrol.
 * - Dual-Pane Tab Riwayat: Split view tabel riwayat dan live render pratinjau
 * dokumen HTML
 * serta tombol modal pratinjau layar penuh.
 * - Banner peringatan Mode Edit warna amber mencolok.
 * - Integrasi 4 Otorisasi Barcode TTE dan tabel
 * `transfer_pasien_antar_ruang_konfirmasi`.
 */
public final class RMTransferPasienAntarRuang extends javax.swing.JDialog {

    // =========================================================================
    // PALET WARNA RESMI MEDICAL TEAL & SLATE (ARGON DESIGN SYSTEM)
    // =========================================================================
    public static final Color COL_HEADER_DARK = new Color(10, 46, 44); // #0A2E2C (Teal-950)
    public static final Color COL_TEAL_PRIMARY = new Color(13, 148, 136); // #0D9488 (Teal-600)
    public static final Color COL_TEAL_DARK = new Color(15, 118, 110); // #0F766E (Teal-700)
    public static final Color COL_TEAL_SOFT = new Color(240, 253, 250); // #F0FDFA (Teal-50)
    public static final Color COL_TEAL_ACTIVE = new Color(224, 242, 241); // #E0F2F1
    public static final Color COL_TEAL_BORDER = new Color(153, 246, 228); // #99F6E4 (Teal-200)
    public static final Color COL_BG_MAIN = new Color(241, 245, 249); // #F1F5F9 (Slate-100)
    public static final Color COL_CARD_BG = Color.WHITE; // #FFFFFF
    public static final Color COL_CARD_HEADER = new Color(248, 250, 252); // #F8FAFC (Slate-50)
    public static final Color COL_BORDER = new Color(203, 213, 225); // #CBD5E1 (Slate-300)
    public static final Color COL_BORDER_DIVIDER = new Color(226, 232, 240); // #E2E8F0 (Slate-200)
    public static final Color COL_INPUT_BG = Color.WHITE;
    public static final Color COL_INPUT_BORDER = new Color(203, 213, 225); // #CBD5E1
    public static final Color COL_TEXT_DARK = new Color(15, 23, 42); // #0F172A (Slate-900)
    public static final Color COL_TEXT_MUTED = new Color(100, 116, 139); // #64748B (Slate-500)
    public static final Color COL_ROSE_PRIMARY = new Color(225, 29, 72); // #E11D48
    public static final Color COL_ROSE_SOFT = new Color(255, 241, 242); // #FFF1F2
    public static final Color COL_ROSE_BORDER = new Color(254, 205, 211); // #FECDD3
    public static final Color COL_AMBER_SOFT = new Color(254, 252, 232); // #FEFCE8
    public static final Color COL_AMBER_BORDER = new Color(253, 230, 138); // #FDE68A
    public static final Color COL_EMERALD_SOFT = new Color(209, 250, 229); // #D1FAE5
    public static final Color COL_EMERALD_BORDER = new Color(167, 243, 208); // #A7F3D0
    public static final Color COL_EMERALD_DARK = new Color(4, 120, 87); // #047857

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_ROW_LABEL = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_SUBLABEL = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font FONT_MICRO_HEADER = new Font("Segoe UI", Font.BOLD, 9);
    public static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Monospaced", Font.BOLD, 11);

    private static final int LEFT_LABEL_WIDTH = 190;

    /**
     * Kolom aktif tabel `transfer_pasien_antar_ruang` (79 kolom).
     * Sinkron 100% dengan migrasi `sik_transfer_pasien_antar_ruang_rm04.sql`.
     */
    private static final String[] KOLOM_TRANSFER = {
            "no_rawat", "tanggal_masuk", "tanggal_pindah", "asal_ruang", "ruang_selanjutnya", "diagnosa_utama",
            "diagnosa_sekunder",
            "indikasi_pindah_ruang", "keterangan_indikasi_pindah_ruang", "prosedur_yang_sudah_dilakukan",
            "obat_yang_telah_diberikan",
            "metode_pemindahan_pasien", "pasien_keluarga_menyetujui", "nama_menyetujui", "hubungan_menyetujui",
            "nip_menyerahkan", "nip_menerima",
            "no_tlp_menyetujui", "sudah_dijelaskan_diagnosa", "kelas_asal_ruang", "kelas_ruang_selanjutnya",
            "kd_dokter_merawat_asal_1", "kd_dokter_merawat_asal_2", "kd_dokter_merawat_asal_3",
            "kd_dokter_merawat_tujuan_1", "kd_dokter_merawat_tujuan_2", "kd_dokter_merawat_tujuan_3",

            "kesadaran_transfer", "td_transfer", "hr_transfer", "rr_transfer", "suhu_transfer", "gcs_transfer",
            "spo2_transfer",
            "tingkat_nyeri_transfer", "observasi_terakhir_pukul",

            "oksigen_status", "oksigen_nasal_kanule", "oksigen_nasal_kateter", "oksigen_face_mask",
            "oksigen_sungkup_rebreathing",
            "oksigen_sungkup_non_rebreathing", "oksigen_tracheostomy", "oksigen_ett",

            "line_invasif", "line_invasif_tanggal",
            "kewaspadaan_standar", "kewaspadaan_kontak", "kewaspadaan_airborne", "kewaspadaan_droplet",

            "nutrisi_puasa", "nutrisi_oral", "nutrisi_ngt", "nutrisi_batasan_cairan_cc", "nutrisi_diet_khusus",
            "nutrisi_diet_khusus_jelaskan",

            "eliminasi_bab", "eliminasi_bak", "eliminasi_bak_kateter_no", "eliminasi_bak_kateter_tanggal_pasang",

            "aktifitas", "mobilisasi", "drainase_wsd", "drainase_drain_luka", "drainase_stoma", "drainase_lain",
            "drainase_lain_ket",

            "luka_tidak_ada",
            "luka_operasi_cek", "luka_operasi_ukuran", "luka_operasi_lokasi", "luka_operasi_kondisi",
            "luka_dekubitus_cek", "luka_dekubitus_ukuran", "luka_dekubitus_lokasi", "luka_dekubitus_kondisi",
            "luka_skin_lost_cek", "luka_skin_lost_ukuran", "luka_skin_lost_lokasi", "luka_skin_lost_kondisi",
            "luka_gangren_cek", "luka_gangren_ukuran", "luka_gangren_lokasi", "luka_gangren_kondisi",
            "luka_lain_cek", "luka_lain_ket",

            "hasil_investigasi_abnormal",

            "tindakan_resiko_jatuh", "tindakan_pemasangan_restrain", "tindakan_perawatan_luka", "tindakan_hygiene",
            "riwayat_alergi", "jenis_alergi", "reaksi_alergi",

            "alat_bantu_tanpa", "alat_bantu_dengar", "alat_bantu_kaca_mata", "alat_bantu_gigi_palsu",
            "alat_bantu_tongkat_kruk",
            "alat_bantu_lain", "alat_bantu_lain_ket",

            "tindakan_yang_sudah_dilakukan",

            "penunjang_laboratorium_cek", "penunjang_laboratorium_ket", "penunjang_radiologi_cek",
            "penunjang_radiologi_ket",
            "penunjang_ct_scan_cek", "penunjang_ct_scan_ket", "penunjang_lain_cek", "penunjang_lain_ket",

            "rencana_tindak_lanjut", "masalah_utama_keperawatan", "dokumen_yang_diserahterimakan"
    };

    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private DlgCariPetugas dialogPetugas;
    private DlgCariDokter dialogDokter;
    private DlgCariPenyakit dialogPenyakit;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // =========================================================================
    // STATE NAVIGASI & WORKSPACE
    // =========================================================================
    private final CardLayout cardLayoutViews = new CardLayout();
    private final JPanel pnlWorkspaceViews = new JPanel(cardLayoutViews);
    private JScrollPane scrollFormMain;
    private JPanel pnlFormSectionsList;
    private final Map<String, JPanel> mapSectionCards = new LinkedHashMap<>();
    private final Map<String, JButton> mapNavButtons = new LinkedHashMap<>();
    private final List<JCheckBox> listHandoverChecks = new ArrayList<>();
    private final List<JTextComponent> listTrackedFields = new ArrayList<>();

    // Banner Edit Mode
    private JPanel pnlBannerEdit;
    private JLabel lblBannerEditNote;
    private JButton btnBatalEdit;
    private boolean isEditMode = false;
    private String editNoRawat = "";
    private String editTanggalMasuk = "";

    // Sub-Toolbar Buttons
    private JButton tabBtnForm;
    private JButton tabBtnHistory;
    private JLabel lblHistoryCountBadge;
    private JLabel lblStatusSimpanInfo;

    // Sidebar Widgets
    public JLabel lblAvatarInitial;
    public JLabel lblPatientName;
    public JLabel lblPatientGenderAge;
    public JLabel lblNoRM;
    public JLabel lblNoRw;
    public JLabel lblValRM;
    public JLabel lblValLahir;
    public JLabel lblValTglMasuk;
    public JLabel lblValRawat;
    public JLabel lblValKamarAsal;
    public JLabel lblValKamarTujuan;
    public JLabel lblSavedBadge;
    public JLabel lblRouteTransfer;
    public JProgressBar progressTransfer;
    public JLabel lblProgressPercent;
    public JLabel lblProgressCount;
    public JLabel lblHandoverSummary;

    // Contextual Footer Layout Tab 1 & Tab 2 (Standar RM 20)
    private CardLayout footerCardLayout;
    private JPanel leftFooterWrapper;
    private JPanel panelFooterInput;
    private JPanel panelFooterData;
    private JLabel lblFooterHint;
    public JButton btnValidasiTTE;

    // =========================================================================
    // FIELD INPUT FORMULIR TRANSFER RM 04-1 & RM 04-2
    // =========================================================================

    // Seksi 01: Informasi Transfer & Ruangan
    public widget.TextBox TNoRw = createTextBox(17, false);
    public widget.TextBox TNoRM = createTextBox(15, false);
    public widget.TextBox TPasien = createTextBox(40, false);
    public widget.TextBox TglLahir = createTextBox(15, false);
    public widget.TextBox Jk = createTextBox(15, false);

    public PanelTanggalDanJam TanggalMasuk = new PanelTanggalDanJam(true, 28);
    public PanelTanggalDanJam TanggalPindah = new PanelTanggalDanJam(true, 28);
    public widget.TextBox DiagnosaUtama = createTextBox(100, false);
    public widget.TextBox DiagnosaSekunder = createTextBox(150, true);
    public JButton BtnDiagnosaUtama;

    public widget.TextBox NamaMenyetujui = createTextBox(50, true);
    public widget.ComboBox HubunganMenyetujui;
    public widget.TextBox TNoTlpMenyetujui = createTextBox(15, true);
    public SegmentedOptionButton segPenjelasanDiagnosa = new SegmentedOptionButton(new String[] { "Ya", "Tidak" },
            "Ya");
    public widget.ComboBox MenyetujuiPemindahan;

    public widget.ComboBox PilihRuangAsal;
    public widget.TextBox AsalRuang = createTextBox(30, true);
    public widget.TextBox KelasAsalRuang = createTextBox(20, true);
    public SegmentedOptionButton segKelasAsal = new SegmentedOptionButton(
            new String[] { "Kelas 1", "Kelas 2", "Kelas 3", "Kelas Utama", "Kelas VIP", "Kelas VVIP" }, "Kelas 1", 3);
    public widget.TextBox KdDokterMerawatAsal1 = createTextBox(20, false);
    public widget.TextBox NmDokterMerawatAsal1 = createTextBox(40, false);
    public widget.TextBox KdDokterMerawatAsal2 = createTextBox(20, false);
    public widget.TextBox NmDokterMerawatAsal2 = createTextBox(40, false);
    public widget.TextBox KdDokterMerawatAsal3 = createTextBox(20, false);
    public widget.TextBox NmDokterMerawatAsal3 = createTextBox(40, false);

    public widget.ComboBox PilihRuangTujuan;
    public widget.TextBox RuangSelanjutnya = createTextBox(30, true);
    public widget.TextBox KelasRuangSelanjutnya = createTextBox(20, true);
    public SegmentedOptionButton segKelasTujuan = new SegmentedOptionButton(
            new String[] { "Kelas 1", "Kelas 2", "Kelas 3", "Kelas Utama", "Kelas VIP", "Kelas VVIP" }, "Kelas 1", 3);
    public widget.TextBox KdDokterMerawatTujuan1 = createTextBox(20, false);
    public widget.TextBox NmDokterMerawatTujuan1 = createTextBox(40, false);
    public widget.TextBox KdDokterMerawatTujuan2 = createTextBox(20, false);
    public widget.TextBox NmDokterMerawatTujuan2 = createTextBox(40, false);
    public widget.TextBox KdDokterMerawatTujuan3 = createTextBox(20, false);
    public widget.TextBox NmDokterMerawatTujuan3 = createTextBox(40, false);

    public widget.ComboBox IndikasiPindah;
    public widget.TextBox KeteranganIndikasiPindahRuang = createTextBox(100, true);
    public SegmentedOptionButton segCaraPindah = new SegmentedOptionButton(
            new String[] { "Jalan", "Kursi Roda", "Strecher", "Lainnya" }, "Strecher");
    public widget.TextBox txtCaraPindahLain = createTextBox(50, true);

    // Seksi 02: Kondisi Pasien Saat Transfer
    public PanelTanggalDanJam ObservasiTerakhirPukul = new PanelTanggalDanJam(true, 28);
    public SegmentedOptionButton segKesadaran = new SegmentedOptionButton(
            new String[] { "Compos Mentis", "Gelisah", "Delirium", "Koma" }, "Compos Mentis");
    public widget.TextBox txtGcsE = createTextBox(2, true);
    public widget.TextBox txtGcsM = createTextBox(2, true);
    public widget.TextBox txtGcsV = createTextBox(2, true);
    public JLabel lblTotalGcsBadge = new JLabel("Total GCS: 15 (E4M6V5)", SwingConstants.CENTER) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = getBackground() != null ? getBackground() : COL_TEAL_SOFT;
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
            Color borderC = (Color) getClientProperty("customBorderColor");
            if (borderC != null) {
                g2.setColor(borderC);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    };

    public widget.TextBox txtTdSistol = createTextBox(4, true);
    public widget.TextBox txtTdDiastol = createTextBox(4, true);
    public widget.TextBox txtHr = createTextBox(4, true);
    public widget.TextBox txtRr = createTextBox(4, true);
    public widget.TextBox txtTemp = createTextBox(5, true);
    public widget.TextBox txtSpo2 = createTextBox(4, true);
    public widget.ComboBox TingkatNyeriTransfer;
    public JLabel lblNyeriKeterangan = new JLabel("0 \u00B7 Tidak ada rasa nyeri", SwingConstants.LEFT);

    public SegmentedOptionButton segTherapyOksigen = new SegmentedOptionButton(new String[] { "Tidak", "Ya" }, "Tidak");
    public widget.TextBox txtAliranOksigen = createTextBox(10, true);
    public JCheckBox chkO2NasalKanule = new JCheckBox("Nasal Kanule");
    public JCheckBox chkO2NasalKateter = new JCheckBox("Nasal Kateter");
    public JCheckBox chkO2FaceMask = new JCheckBox("Face Mask");
    public JCheckBox chkO2SungkupRebreathing = new JCheckBox("Sungkup Rebreathing");
    public JCheckBox chkO2SungkupNonRebreathing = new JCheckBox("Sungkup Non Rebreathing");
    public JCheckBox chkO2Tracheostomy = new JCheckBox("Tracheostomy");
    public JCheckBox chkO2Ett = new JCheckBox("ETT");

    // Seksi 03: Data Serah Terima Klinis
    public SegmentedOptionButton segLineInvasif = new SegmentedOptionButton(
            new String[] { "Tidak Ada Line Invasive", "CVP/PICC", "IV Line" }, "IV Line");
    public PanelTanggalDanJam LineInvasifTanggal = new PanelTanggalDanJam(true, 28);

    public widget.TextBox txtProsedur1 = createTextBox(200, true);
    public widget.TextBox txtTglProsedur1 = createTextBox(15, true);
    public widget.TextBox txtProsedur2 = createTextBox(200, true);
    public widget.TextBox txtTglProsedur2 = createTextBox(15, true);
    public widget.TextBox txtProsedur3 = createTextBox(200, true);
    public widget.TextBox txtTglProsedur3 = createTextBox(15, true);

    public JCheckBox chkPrecautionStandar = new JCheckBox("Standar", true);
    public JCheckBox chkPrecautionKontak = new JCheckBox("Kontak");
    public JCheckBox chkPrecautionAirborne = new JCheckBox("Airborne");
    public JCheckBox chkPrecautionDroplet = new JCheckBox("Droplet");

    public SegmentedOptionButton segNutrisi = new SegmentedOptionButton(new String[] { "Puasa", "Oral", "NGT" },
            "Oral");
    public JCheckBox chkBatasanCairan = new JCheckBox("Batasan Cairan :");
    public widget.TextBox TNutrisiBatasanCairanCc = createTextBox(10, true);
    public JCheckBox chkDietKhusus = new JCheckBox("Diet khusus :");
    public widget.TextBox TNutrisiDietKhususJelaskan = createTextBox(100, true);

    public SegmentedOptionButton segBab = new SegmentedOptionButton(
            new String[] { "Normal", "Diare/Konstipasi", "Ileustomy/Colostomy", "Inkontinensia Alvi" }, "Normal");
    public SegmentedOptionButton segBak = new SegmentedOptionButton(
            new String[] { "Normal", "Inkontinensia Urine", "Kateter Urine" }, "Normal");
    public widget.TextBox TEliminasiBakKateterNo = createTextBox(20, true);
    public PanelTanggalDanJam DTPEliminasiBakKateterTanggalPasang = new PanelTanggalDanJam(true, 28);

    // Seksi 04: Aktifitas, Drainase & Integritas Kulit/Luka
    public SegmentedOptionButton segAktifitas = new SegmentedOptionButton(
            new String[] { "Mandiri", "Dibantu Sebagian", "Dibantu Penuh" }, "Dibantu Sebagian");
    public SegmentedOptionButton segMobilisasi = new SegmentedOptionButton(
            new String[] { "Tirah Baring", "Duduk", "Jalan" }, "Tirah Baring");

    public JCheckBox chkDrainWsd = new JCheckBox("WSD");
    public JCheckBox chkDrainStoma = new JCheckBox("Stoma");
    public JCheckBox chkDrainLuka = new JCheckBox("Drain Luka");
    public JCheckBox chkDrainLain = new JCheckBox("Lain-lain :");
    public widget.TextBox TDrainaseLainKet = createTextBox(100, true);

    public JCheckBox chkLukaTidakAda = new JCheckBox("Tidak ada luka (Integritas kulit utuh)", true);
    public DefaultTableModel modelTabelLuka;
    public JTable tableLuka;
    public widget.TextBox TLukaLainKet = createTextBox(100, true);

    // Seksi 05: Investigasi Abnormal, Kebutuhan Khusus & Penunjang
    public JTextArea txtHasilInvestigasiAbnormal = createTextArea(3);
    public JCheckBox chkKhususJatuh = new JCheckBox("Resiko jatuh");
    public JCheckBox chkKhususRestrain = new JCheckBox("Pemasangan restrain");
    public JCheckBox chkKhususRawatLuka = new JCheckBox("Perawatan luka");
    public JCheckBox chkKhususHygiene = new JCheckBox("Hygiene", true);

    public SegmentedOptionButton segRiwayatAlergi = new SegmentedOptionButton(new String[] { "Tidak", "Ya" }, "Tidak");
    public widget.TextBox TJenisAlergi = createTextBox(100, true);
    public widget.TextBox TReaksiAlergi = createTextBox(100, true);

    public JCheckBox chkAlatTanpa = new JCheckBox("Tanpa alat bantu", true);
    public JCheckBox chkAlatDengar = new JCheckBox("Alat bantu dengar");
    public JCheckBox chkAlatKacamata = new JCheckBox("Kaca mata");
    public JCheckBox chkAlatGigiPalsu = new JCheckBox("Gigi palsu");
    public JCheckBox chkAlatTongkat = new JCheckBox("Tongkat / kruk");
    public widget.TextBox TAlatBantuLainKet = createTextBox(100, true);

    public JTextArea txtTindakanSudahDilakukan = createTextArea(3);
    public JCheckBox chkPenunjangLab = new JCheckBox("Laboratorium :");
    public widget.TextBox TPenunjangLaboratoriumKet = createTextBox(200, true);
    public JCheckBox chkPenunjangRad = new JCheckBox("Radiologi :");
    public widget.TextBox TPenunjangRadiologiKet = createTextBox(200, true);
    public JCheckBox chkPenunjangCtScan = new JCheckBox("CT Scan :");
    public widget.TextBox TPenunjangCtScanKet = createTextBox(200, true);
    public JCheckBox chkPenunjangLain = new JCheckBox("Lain-lain :");
    public widget.TextBox TPenunjangLainKet = createTextBox(200, true);

    // Seksi 06: Terapi, Rencana & Dokumen
    public widget.TextBox txtTherapy1 = createTextBox(200, true);
    public widget.TextBox txtTherapy2 = createTextBox(200, true);
    public widget.TextBox txtTherapy3 = createTextBox(200, true);
    public widget.TextBox txtTherapy4 = createTextBox(200, true);
    public widget.TextBox txtTherapy5 = createTextBox(200, true);

    public widget.TextBox txtRtl1 = createTextBox(200, true);
    public widget.TextBox txtRtl2 = createTextBox(200, true);
    public widget.TextBox txtRtl3 = createTextBox(200, true);

    public widget.TextBox txtMasalahKep1 = createTextBox(150, true);
    public widget.TextBox txtMasalahKep2 = createTextBox(150, true);
    public widget.TextBox txtMasalahKep3 = createTextBox(150, true);

    public widget.TextBox txtDokumen1 = createTextBox(150, true);
    public widget.TextBox txtDokumen2 = createTextBox(150, true);
    public widget.TextBox txtDokumen3 = createTextBox(150, true);
    public widget.TextBox txtDokumen4 = createTextBox(150, true);
    public widget.TextBox txtDokumen5 = createTextBox(150, true);

    // Seksi 07: Otorisasi Serah Terima & Barcode TTE
    public widget.TextBox KdPetugasMenyerahkan = createTextBox(20, false);
    public widget.TextBox NmPetugasMenyerahkan = createTextBox(40, false);
    public widget.TextBox KdPetugasMenerima = createTextBox(20, false);
    public widget.TextBox NmPetugasMenerima = createTextBox(40, false);

    public BarcodeTtePanel barcodePengirim = new BarcodeTtePanel("TTE-NRS01-000013", 101);
    public BarcodeTtePanel barcodePenerima = new BarcodeTtePanel("TTE-NRS02-000013", 102);
    public BarcodeTtePanel barcodeDokter = new BarcodeTtePanel("TTE-DPJP-000013", 103);
    public BarcodeTtePanel barcodeKeluarga = new BarcodeTtePanel("PJ-PAS-000013", 104);

    public JLabel lblNamaTtdPengirim = new JLabel("( Ns. Rina Septiani, S.Kep )", SwingConstants.CENTER);
    public JLabel lblNamaTtdPenerima = new JLabel("( Ns. Dewi Sartika, S.Kep )", SwingConstants.CENTER);
    public JLabel lblNamaTtdDokter = new JLabel("( dr. Hilyatul Nadia, Sp.PD )", SwingConstants.CENTER);
    public JLabel lblNamaTtdKeluarga = new JLabel("( Tn. Hendra Saputra )", SwingConstants.CENTER);

    public JLabel lblWaktuTtdPengirim = new JLabel("Waktu: -", SwingConstants.CENTER);
    public JLabel lblWaktuTtdPenerima = new JLabel("Waktu: -", SwingConstants.CENTER);
    public JLabel lblWaktuTtdDokter = new JLabel("Waktu: -", SwingConstants.CENTER);
    public JLabel lblWaktuTtdKeluarga = new JLabel("Waktu: -", SwingConstants.CENTER);

    public JLabel lblStatusVerifPengirim = new JLabel("Belum Validasi", SwingConstants.CENTER);
    public JLabel lblStatusVerifPenerima = new JLabel("Belum Validasi", SwingConstants.CENTER);
    public JLabel lblStatusVerifDokter = new JLabel("Belum Validasi", SwingConstants.CENTER);
    public JLabel lblStatusVerifKeluarga = new JLabel("Belum Verifikasi", SwingConstants.CENTER);

    // =========================================================================
    // KOMPONEN TAB 2: DATA RIWAYAT & DUAL-PANE PREVIEW
    // =========================================================================
    public widget.Table tbObat;
    public widget.Tanggal DTPCari1, DTPCari2;
    public widget.TextBox TCari;
    public widget.Button BtnCari, BtnAll;
    public widget.Label LCount;
    public JEditorPane LoadHTML;
    public JSplitPane splitRiwayat;

    // Bottom Action Bar Buttons (Standar RM 20)
    public JButton BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnPrint, BtnKeluar;
    private JButton btnGantiData, btnHapusData, btnCetakDokumen;

    // Window State & Controls
    private boolean isWindowMaximized = false;
    private Rectangle normalWindowBounds = null;
    private JButton btnMaximizeWindow, btnCloseTopWindow;

    // Item Kamar Helper
    private static final class KamarItem {
        final String label, kdKamar, kelas;

        KamarItem(String label, String kdKamar, String kelas) {
            this.label = label;
            this.kdKamar = kdKamar;
            this.kelas = kelas;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public RMTransferPasienAntarRuang(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Transfer Pasien Antar Ruangan (RM 04-1 & RM 04-2) — RSUD Demang Sepulau Raya");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        tabMode = new DefaultTableModel(null, new Object[] {
                "No.Rawat", "No.RM", "Nama Pasien", "Tgl.Lahir", "J.K.", "Tanggal Masuk", "Tanggal Pindah",
                "Diagnosa Utama",
                "NIP Menyerahkan", "Petugas Yang Menyerahkan", "NIP Menerima", "Petugas Yang Menerima"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };

        initComponents();

        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] colWidths = { 105, 70, 150, 65, 55, 115, 115, 160, 95, 150, 95, 150 };
        for (int c = 0; c < colWidths.length; c++) {
            TableColumn col = tbObat.getColumnModel().getColumn(c);
            col.setPreferredWidth(colWidths[c]);
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        initLookupDialogs();
        muatPilihanKamar(PilihRuangAsal);
        muatPilihanKamar(PilihRuangTujuan);

        pasangListeners();
        updateGcsCalculation();
        updateProgress();

        setMinimumSize(new Dimension(750, 380));
        sesuaikanUkuranOtomatis();
    }

    /**
     * Menghitung area kerja layar yang aman (usable working area),
     * yaitu ukuran layar dikurangi insets sistem (Windows Taskbar di sisi bawah, atas, kiri, atau kanan).
     * Bekerja akurat pada konfigurasi multi-monitor maupun display scaling Windows.
     */
    public Rectangle getUsableScreenBounds() {
        try {
            GraphicsConfiguration gc = getGraphicsConfiguration();
            if (gc == null && getParent() != null && getParent().isShowing()) {
                gc = getParent().getGraphicsConfiguration();
            }
            if (gc == null) {
                gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            }
            Rectangle screenBounds = gc.getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            return new Rectangle(
                    screenBounds.x + insets.left,
                    screenBounds.y + insets.top,
                    screenBounds.width - insets.left - insets.right,
                    screenBounds.height - insets.top - insets.bottom);
        } catch (Exception e) {
            return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        }
    }

    /**
     * Menyesuaikan ukuran dan posisi jendela form secara otomatis dan cerdas
     * mengikuti variasi ukuran layar/monitor aktif di komputer rumah sakit.
     * Menjamin footer tombol aksi 100% selalu tampak utuh di atas taskbar Windows.
     */
    public void sesuaikanUkuranOtomatis() {
        try {
            Rectangle usable = getUsableScreenBounds();
            int maxBottom = usable.y + usable.height;

            Component parent = getParent();
            int targetX = usable.x;
            int targetY = usable.y;
            int targetW = usable.width;
            int targetH = usable.height;

            // Jika dipanggil dari frmUtama (Menu Utama SIMRS Khanza)
            if (parent instanceof JFrame && parent.isShowing()) {
                JFrame parentFrame = (JFrame) parent;
                Point parentPt = parentFrame.getLocationOnScreen();
                // Deteksi batas atas: letakkan tepat di bawah toolbar menu atas frmUtama (~65-95px)
                int topOffset = 0;
                if (parentFrame.getJMenuBar() != null && parentFrame.getJMenuBar().isShowing()) {
                    topOffset += parentFrame.getJMenuBar().getHeight();
                }
                int startY = Math.max(usable.y, parentPt.y + Math.max(topOffset, 65));
                if (startY < maxBottom - 350) {
                    targetY = startY;
                } else {
                    targetY = usable.y;
                }
                targetW = usable.width;
                targetH = maxBottom - targetY;
            } else {
                // Jika dipanggil dari modal lain (DlgRawatInap, DlgKamarInap, DlgIGD, dsb)
                targetW = Math.min(1366, usable.width - 20);
                targetH = Math.min(760, usable.height - 20);
                if (targetW < usable.width && targetH < usable.height) {
                    targetX = usable.x + (usable.width - targetW) / 2;
                    targetY = usable.y + (usable.height - targetH) / 2;
                } else {
                    targetX = usable.x;
                    targetY = usable.y;
                    targetW = usable.width;
                    targetH = usable.height;
                }
            }

            if (targetY + targetH > maxBottom) {
                targetH = maxBottom - targetY;
            }

            setBounds(targetX, targetY, targetW, targetH);
        } catch (Exception ex) {
            Rectangle maxScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            super.setBounds(maxScreen);
        }
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        Rectangle usable = getUsableScreenBounds();
        int maxBottom = usable.y + usable.height;
        int maxRight = usable.x + usable.width;

        int safeW = Math.min(width, usable.width);
        int safeH = Math.min(height, usable.height);
        int safeX = x;
        int safeY = y;

        // Cegah bagian bawah jendela tertutup Windows Taskbar
        if (safeY + safeH > maxBottom) {
            int sisaTinggi = maxBottom - safeY;
            if (sisaTinggi >= 380) {
                // Pangkas tinggi agar pas tepat di atas taskbar tanpa menggeser posisi Y atas
                safeH = sisaTinggi;
            } else {
                // Geser ke atas sejauh yang diperlukan agar tidak menembus batas bawah taskbar
                safeY = Math.max(usable.y, maxBottom - safeH);
                safeH = Math.min(safeH, maxBottom - safeY);
            }
        }
        if (safeY < usable.y) {
            safeY = usable.y;
            safeH = Math.min(safeH, usable.height);
        }

        // Cegah bagian samping jendela keluar layar
        if (safeX + safeW > maxRight) {
            safeX = Math.max(usable.x, maxRight - safeW);
            safeW = Math.min(safeW, maxRight - safeX);
        }
        if (safeX < usable.x) {
            safeX = usable.x;
            safeW = Math.min(safeW, usable.width);
        }

        super.setBounds(safeX, safeY, safeW, safeH);
    }

    @Override
    public void setLocationRelativeTo(Component c) {
        super.setLocationRelativeTo(c);
        setBounds(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            Rectangle usable = getUsableScreenBounds();
            int maxBottom = usable.y + usable.height;
            if (getY() + getHeight() > maxBottom || getWidth() <= 0 || getHeight() <= 0) {
                sesuaikanUkuranOtomatis();
            } else {
                setBounds(getX(), getY(), getWidth(), getHeight());
            }
        }
        super.setVisible(b);
    }

    // =========================================================================
    // PERAKITAN TATA LETAK UTAMA (TOP HEADER, WORKSPACE CARDS, ACTION BAR)
    // =========================================================================

    private void initComponents() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(COL_BG_MAIN);

        // 1. Top Header Bar & Sub-Toolbar
        getContentPane().add(buildTopHeader(), BorderLayout.NORTH);

        // 2. Main Workspace (Tab 1: Formulir Transfer, Tab 2: Data Riwayat Transfer)
        pnlWorkspaceViews.setBackground(COL_BG_MAIN);
        pnlWorkspaceViews.add(buildFormulirWorkspace(), "FORM");
        pnlWorkspaceViews.add(buildHistoryWorkspace(), "HISTORY");
        getContentPane().add(pnlWorkspaceViews, BorderLayout.CENTER);

        // 3. Action Toolbar Bawah (Sticky Footer)
        JPanel bottomFooter = buildBottomActionBar();
        getContentPane().add(bottomFooter, BorderLayout.SOUTH);
        getContentPane().setComponentZOrder(bottomFooter, 0);
    }

    private JPanel buildTopHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());

        // Baris Atas Dark Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COL_HEADER_DARK);
        topBar.setBorder(new EmptyBorder(8, 18, 8, 18));

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTitle.setOpaque(false);

        JLabel badgeIcon = new JLabel("RM", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 118, 110));
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.setColor(new Color(20, 184, 166, 150));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badgeIcon.setPreferredSize(new Dimension(28, 28));
        badgeIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badgeIcon.setForeground(new Color(204, 251, 241));
        badgeIcon.setOpaque(false);

        JLabel title = new JLabel("Transfer Pasien Antar Ruangan");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel badgeRm = new JLabel("RM 04-1 & RM 04-2", SwingConstants.CENTER) {
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

        leftTitle.add(badgeIcon);
        leftTitle.add(title);
        leftTitle.add(badgeRm);

        JPanel rightHospital = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightHospital.setOpaque(false);
        JLabel lblHospital = new JLabel("RSUD Demang Sepulau Raya  \u2022  Lampung Tengah");
        lblHospital.setFont(FONT_SUBLABEL);
        lblHospital.setForeground(new Color(153, 246, 228));
        rightHospital.add(lblHospital);

        btnMaximizeWindow = new JButton();
        btnMaximizeWindow.setIcon(createMaximizeWindowIcon(false));
        btnMaximizeWindow.setBackground(COL_TEAL_PRIMARY);
        btnMaximizeWindow.setBorder(new LineBorder(COL_TEAL_BORDER, 1, true));
        btnMaximizeWindow.setPreferredSize(new Dimension(28, 24));
        btnMaximizeWindow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMaximizeWindow.setToolTipText("Layar Penuh / Pulihkan Ukuran Jendela");
        btnMaximizeWindow.setFocusPainted(false);
        btnMaximizeWindow.addActionListener(e -> toggleMaximizeWindow());

        btnCloseTopWindow = new JButton();
        btnCloseTopWindow.setIcon(createCloseWindowIcon());
        btnCloseTopWindow.setBackground(new Color(159, 18, 57));
        btnCloseTopWindow.setBorder(new LineBorder(new Color(244, 63, 94), 1, true));
        btnCloseTopWindow.setPreferredSize(new Dimension(28, 24));
        btnCloseTopWindow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCloseTopWindow.setToolTipText("Tutup Jendela Transfer (Alt+K)");
        btnCloseTopWindow.setFocusPainted(false);
        btnCloseTopWindow.addActionListener(e -> dispose());

        rightHospital.add(btnMaximizeWindow);
        rightHospital.add(btnCloseTopWindow);

        final Point[] dragPoint = new Point[1];
        topBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragPoint[0] = e.getPoint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleMaximizeWindow();
                }
            }
        });
        topBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] != null && !isWindowMaximized) {
                    Point curr = e.getLocationOnScreen();
                    setLocation(curr.x - dragPoint[0].x, curr.y - dragPoint[0].y);
                }
            }
        });

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(rightHospital, BorderLayout.EAST);
        wrapper.add(topBar, BorderLayout.NORTH);

        // Sub-Toolbar (Tab Peralihan & Status Ringkas)
        JPanel subToolbar = new JPanel(new BorderLayout());
        subToolbar.setBackground(Color.WHITE);
        subToolbar.setBorder(new MatteBorder(0, 0, 1, 0, COL_BORDER_DIVIDER));
        subToolbar.setPreferredSize(new Dimension(0, 42));

        JPanel leftTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        leftTabs.setOpaque(false);

        tabBtnForm = new JButton("Formulir Transfer");
        tabBtnForm.setFont(FONT_SECTION);
        tabBtnForm.setFocusPainted(false);
        tabBtnForm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        styleTabButton(tabBtnForm, true);
        tabBtnForm.addActionListener(e -> switchWorkspaceView("FORM"));

        tabBtnHistory = new JButton("Data Riwayat Transfer");
        tabBtnHistory.setFont(FONT_SECTION);
        tabBtnHistory.setFocusPainted(false);
        tabBtnHistory.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        styleTabButton(tabBtnHistory, false);
        tabBtnHistory.addActionListener(e -> switchWorkspaceView("HISTORY"));

        lblHistoryCountBadge = new JLabel("0", SwingConstants.CENTER);
        lblHistoryCountBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHistoryCountBadge.setOpaque(true);
        lblHistoryCountBadge.setBackground(COL_BG_MAIN);
        lblHistoryCountBadge.setForeground(COL_TEXT_MUTED);
        lblHistoryCountBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        leftTabs.add(tabBtnForm);
        leftTabs.add(tabBtnHistory);
        leftTabs.add(lblHistoryCountBadge);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightActions.setOpaque(false);

        lblStatusSimpanInfo = new JLabel("Form siap diisi");
        lblStatusSimpanInfo.setFont(FONT_SUBLABEL);
        lblStatusSimpanInfo.setForeground(COL_TEXT_MUTED);

        JButton btnDefaultStandar = createRoundedButton("Isi Default Standar", COL_TEAL_SOFT, COL_TEAL_DARK,
                COL_TEAL_BORDER);
        btnDefaultStandar.setPreferredSize(new Dimension(145, 30));
        btnDefaultStandar.addActionListener(e -> applyDefaultNormalValues());

        rightActions.add(lblStatusSimpanInfo);
        rightActions.add(btnDefaultStandar);

        subToolbar.add(leftTabs, BorderLayout.WEST);
        subToolbar.add(rightActions, BorderLayout.EAST);
        wrapper.add(subToolbar, BorderLayout.CENTER);

        return wrapper;
    }

    private void toggleMaximizeWindow() {
        Rectangle usable = getUsableScreenBounds();
        if (!isWindowMaximized) {
            normalWindowBounds = getBounds();
            setBounds(usable);
            if (btnMaximizeWindow != null) {
                btnMaximizeWindow.setIcon(createMaximizeWindowIcon(true));
                btnMaximizeWindow.setToolTipText("Pulihkan Ukuran Jendela (Restore)");
            }
            isWindowMaximized = true;
        } else {
            if (normalWindowBounds != null) {
                setBounds(normalWindowBounds);
            } else {
                sesuaikanUkuranOtomatis();
            }
            if (btnMaximizeWindow != null) {
                btnMaximizeWindow.setIcon(createMaximizeWindowIcon(false));
                btnMaximizeWindow.setToolTipText("Perbesar Layar Penuh (Maximize)");
            }
            isWindowMaximized = false;
        }
    }

    private Icon createMaximizeWindowIcon(final boolean isRestore) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.4f));
                if (!isRestore) {
                    g2.drawRoundRect(x + 2, y + 2, 10, 10, 2, 2);
                } else {
                    g2.drawRoundRect(x + 5, y + 1, 8, 8, 2, 2);
                    g2.setColor(c != null ? c.getBackground() : COL_HEADER_DARK);
                    g2.fillRect(x + 2, y + 4, 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.drawRoundRect(x + 2, y + 4, 8, 8, 2, 2);
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 14;
            }

            @Override
            public int getIconHeight() {
                return 14;
            }
        };
    }

    private Icon createCloseWindowIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawLine(x + 3, y + 3, x + 11, y + 11);
                g2.drawLine(x + 11, y + 3, x + 3, y + 11);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 14;
            }

            @Override
            public int getIconHeight() {
                return 14;
            }
        };
    }

    private void styleTabButton(JButton btn, boolean active) {
        btn.setRolloverEnabled(true);
        if (active) {
            btn.setBackground(COL_TEAL_SOFT);
            btn.setForeground(COL_TEAL_DARK);
            btn.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 3, 0, COL_TEAL_PRIMARY),
                    new EmptyBorder(6, 16, 6, 16)));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(COL_TEXT_MUTED);
            btn.setBorder(new EmptyBorder(6, 16, 6, 16));
        }
    }

    private void switchWorkspaceView(String viewName) {
        cardLayoutViews.show(pnlWorkspaceViews, viewName);
        styleTabButton(tabBtnForm, "FORM".equals(viewName));
        styleTabButton(tabBtnHistory, "HISTORY".equals(viewName));
        if (footerCardLayout != null && leftFooterWrapper != null) {
            footerCardLayout.show(leftFooterWrapper, "FORM".equals(viewName) ? "INPUT" : "DATA");
        }
        if ("HISTORY".equals(viewName)) {
            runBackground(this::tampil);
            updateStateButtonsTab2(tbObat.getSelectedRow());
        }
    }

    // =========================================================================
    // WORKSPACE 1: FORMULIR TRANSFER VERTIKAL SCROLL + STICKY SIDEBAR KIRI
    // =========================================================================

    private JPanel buildFormulirWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout(16, 0));
        workspace.setBackground(COL_BG_MAIN);
        workspace.setBorder(new EmptyBorder(12, 16, 12, 16));

        // A. Sticky Sidebar Navigasi Kiri (Width 295px) dibungkus JScrollPane agar pas
        // di layar laptop
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

        // B. Area Scrollable Form Kanan (Single Full Vertical Scroll - Track Viewport
        // Width)
        pnlFormSectionsList = new ScrollableTrackWidthPanel();
        pnlFormSectionsList.setLayout(new BoxLayout(pnlFormSectionsList, BoxLayout.Y_AXIS));
        pnlFormSectionsList.setOpaque(false);

        // Banner Mode Edit (Paling Atas)
        pnlBannerEdit = buildEditModeBanner();
        pnlBannerEdit.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlBannerEdit.setMaximumSize(new Dimension(Short.MAX_VALUE, 48));
        pnlBannerEdit.setVisible(false);
        pnlFormSectionsList.add(pnlBannerEdit);
        pnlFormSectionsList.add(createFormVerticalSpacer(10));

        // 7 Seksi Modular Form Transfer
        addSectionCard("01", buildSection01InformasiTransfer());
        pnlFormSectionsList.add(createFormVerticalSpacer(14));

        addSectionCard("02", buildSection02KondisiPasien());
        pnlFormSectionsList.add(createFormVerticalSpacer(14));

        addSectionCard("03", buildSection03DataSerahTerima1());
        pnlFormSectionsList.add(createFormVerticalSpacer(14));

        addSectionCard("04", buildSection04AktifitasKulitLuka());
        pnlFormSectionsList.add(createFormVerticalSpacer(14));

        addSectionCard("05", buildSection05InvestigasiAlatPenunjang());
        pnlFormSectionsList.add(createFormVerticalSpacer(14));

        addSectionCard("06", buildSection06TherapyRencanaDokumen());
        pnlFormSectionsList.add(createFormVerticalSpacer(14));

        addSectionCard("07", buildSection07OtorisasiSerahTerima());
        pnlFormSectionsList.add(createFormVerticalSpacer(20));

        scrollFormMain = new JScrollPane(pnlFormSectionsList);
        scrollFormMain.setBorder(null);
        scrollFormMain.setOpaque(false);
        scrollFormMain.getViewport().setOpaque(false);
        scrollFormMain.getVerticalScrollBar().setUnitIncrement(24);
        scrollFormMain.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        workspace.add(scrollFormMain, BorderLayout.CENTER);
        return workspace;
    }

    private Component createFormVerticalSpacer(int height) {
        Component strut = Box.createVerticalStrut(height);
        if (strut instanceof JComponent) {
            ((JComponent) strut).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        return strut;
    }

    private void addSectionCard(String code, JPanel cardPanel) {
        cardPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        mapSectionCards.put(code, cardPanel);
        pnlFormSectionsList.add(cardPanel);
    }

    private JPanel buildEditModeBanner() {
        JPanel banner = new JPanel(new BorderLayout(12, 0));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Short.MAX_VALUE, 48));
        banner.setBackground(COL_AMBER_SOFT);
        banner.setBorder(new CompoundBorder(
                new RoundedBorder(new Color(245, 158, 11), 8, 1.2f),
                new EmptyBorder(8, 14, 8, 14)));

        lblBannerEditNote = new JLabel("MODE EDIT AKTIF: Anda sedang memperbarui data transfer pasien.",
                SwingConstants.LEFT);
        lblBannerEditNote.setFont(FONT_SECTION);
        lblBannerEditNote.setForeground(new Color(146, 64, 14));

        btnBatalEdit = createRoundedButton("Batal Edit / Input Baru", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnBatalEdit.setPreferredSize(new Dimension(160, 30));
        btnBatalEdit.addActionListener(e -> setModeInputBaru());

        banner.add(lblBannerEditNote, BorderLayout.WEST);
        banner.add(btnBatalEdit, BorderLayout.EAST);
        return banner;
    }

    public void setModeEditAktif(String noRawat, String tglMasuk) {
        this.isEditMode = true;
        this.editNoRawat = noRawat;
        this.editTanggalMasuk = tglMasuk;
        lblBannerEditNote.setText("MODE EDIT AKTIF: Anda sedang memperbarui data transfer tanggal " + tglMasuk
                + " (No. Rawat: " + noRawat + ")");
        pnlBannerEdit.setVisible(true);
        switchWorkspaceView("FORM");
        scrollFormMain.getViewport().setViewPosition(new Point(0, 0));
    }

    public void setModeInputBaru() {
        this.isEditMode = false;
        this.editNoRawat = "";
        this.editTanggalMasuk = "";
        pnlBannerEdit.setVisible(false);
        emptTeks();
        isRawat();
    }

    // =========================================================================
    // SIDEBAR KIRI: KARTU PASIEN DI POSISI PALING ATAS & NAVIGASI CEPAT
    // =========================================================================

    private JPanel buildLeftStickySidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(285, 750));

        // 1. KARTU IDENTITAS PASIEN (PALING ATAS SIDEBAR — STANDAR RESMI RM 20)
        JPanel patientCard = createBaseCardPanel();
        patientCard.setLayout(new BorderLayout());
        patientCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel patHeader = new JPanel(new BorderLayout());
        patHeader.setBackground(COL_CARD_HEADER);
        patHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER_DIVIDER),
                new EmptyBorder(8, 12, 8, 12)));
        JLabel lblIdHeader = new JLabel("IDENTITAS PASIEN");
        lblIdHeader.setFont(FONT_MICRO_HEADER);
        lblIdHeader.setForeground(COL_TEXT_MUTED);

        lblSavedBadge = new JLabel("RM 04 (Aktif)", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getBackground() != null ? getBackground() : new Color(209, 250, 229);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblSavedBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSavedBadge.setForeground(COL_EMERALD_DARK);
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

        lblAvatarInitial = new JLabel("PS", SwingConstants.CENTER) {
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
        lblAvatarInitial.setPreferredSize(new Dimension(42, 42));
        lblAvatarInitial.setMinimumSize(new Dimension(42, 42));
        lblAvatarInitial.setMaximumSize(new Dimension(42, 42));
        lblAvatarInitial.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvatarInitial.setForeground(COL_TEAL_DARK);
        lblAvatarInitial.setOpaque(false);

        JPanel nameBox = new JPanel(new GridLayout(2, 1, 0, 2));
        nameBox.setOpaque(false);
        lblPatientName = new JLabel("-");
        lblPatientName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPatientName.setForeground(COL_TEXT_DARK);
        lblPatientGenderAge = new JLabel("-");
        lblPatientGenderAge.setFont(FONT_SUBLABEL);
        lblPatientGenderAge.setForeground(COL_TEXT_MUTED);
        nameBox.add(lblPatientName);
        nameBox.add(lblPatientGenderAge);

        avatarRow.add(lblAvatarInitial, BorderLayout.WEST);
        avatarRow.add(nameBox, BorderLayout.CENTER);

        patBody.add(avatarRow);
        patBody.add(Box.createVerticalStrut(10));
        patBody.add(new JSeparator());
        patBody.add(Box.createVerticalStrut(8));

        lblValRM = new JLabel("-");
        lblValLahir = new JLabel("-");
        lblValTglMasuk = new JLabel("-");
        lblValRawat = new JLabel("-");
        lblValKamarAsal = new JLabel("-");
        lblValKamarTujuan = new JLabel("-");

        patBody.add(createKeyValueRow("No. RM", lblValRM, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("Tgl Lahir / Umur", lblValLahir, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("Tgl Masuk RS", lblValTglMasuk, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("No. Rawat", lblValRawat, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("Kamar Asal", lblValKamarAsal, false));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("Pindah Ke", lblValKamarTujuan, false));

        patientCard.add(patHeader, BorderLayout.NORTH);
        patientCard.add(patBody, BorderLayout.CENTER);
        patientCard.setPreferredSize(new Dimension(285, 305));
        patientCard.setMinimumSize(new Dimension(285, 290));
        patientCard.setMaximumSize(new Dimension(Short.MAX_VALUE, 310));

        // 2. KARTU KELENGKAPAN TRANSFER
        JPanel progressCard = createBaseCardPanel();
        progressCard.setLayout(new BoxLayout(progressCard, BoxLayout.Y_AXIS));
        progressCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel progTop = new JPanel(new BorderLayout());
        progTop.setOpaque(false);
        JLabel lblProgTitle = new JLabel("KELENGKAPAN TRANSFER");
        lblProgTitle.setFont(FONT_MICRO_HEADER);
        lblProgTitle.setForeground(COL_TEXT_MUTED);
        lblProgressPercent = new JLabel("0%");
        lblProgressPercent.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblProgressPercent.setForeground(COL_TEAL_DARK);
        progTop.add(lblProgTitle, BorderLayout.WEST);
        progTop.add(lblProgressPercent, BorderLayout.EAST);

        progressTransfer = new JProgressBar(0, 20) {
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
        progressTransfer.setValue(0);
        progressTransfer.setOpaque(false);
        progressTransfer.setBorderPainted(false);
        progressTransfer.setPreferredSize(new Dimension(260, 8));

        JPanel progBottom = new JPanel(new BorderLayout());
        progBottom.setOpaque(false);
        lblProgressCount = new JLabel("0/20 parameter terisi");
        lblProgressCount.setFont(FONT_SUBLABEL);
        lblProgressCount.setForeground(COL_TEXT_MUTED);
        lblHandoverSummary = new JLabel("Ceklis: 0/6 seksi");
        lblHandoverSummary.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHandoverSummary.setForeground(COL_TEAL_DARK);
        progBottom.add(lblProgressCount, BorderLayout.WEST);
        progBottom.add(lblHandoverSummary, BorderLayout.EAST);

        JButton btnVerifAll = createRoundedButton("Verifikasi Semua Serah Terima", COL_TEAL_SOFT, COL_TEAL_DARK,
                COL_TEAL_BORDER);
        btnVerifAll.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVerifAll.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
        btnVerifAll.addActionListener(e -> {
            for (JCheckBox chk : listHandoverChecks) {
                chk.setSelected(true);
            }
            updateProgress();
        });

        progressCard.add(progTop);
        progressCard.add(Box.createVerticalStrut(6));
        progressCard.add(progressTransfer);
        progressCard.add(Box.createVerticalStrut(6));
        progressCard.add(progBottom);
        progressCard.add(Box.createVerticalStrut(8));
        progressCard.add(btnVerifAll);
        progressCard.setMaximumSize(new Dimension(300, 128));

        // 3. KARTU NAVIGASI 7 SEKSI (LOMPAT KE SEKSI)
        JPanel navCard = createBaseCardPanel();
        navCard.setLayout(new BorderLayout());
        navCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel navHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        navHeader.setBackground(COL_CARD_HEADER);
        navHeader.setBorder(new MatteBorder(0, 0, 1, 0, COL_BORDER_DIVIDER));
        JLabel lblNavTitle = new JLabel("LOMPAT KE SEKSI");
        lblNavTitle.setFont(FONT_MICRO_HEADER);
        lblNavTitle.setForeground(COL_TEXT_MUTED);
        navHeader.add(lblNavTitle);

        JPanel navList = new JPanel(new GridLayout(7, 1, 0, 2));
        navList.setBackground(Color.WHITE);
        navList.setBorder(new EmptyBorder(6, 6, 6, 6));

        String[][] sections = {
                { "01", "Informasi Transfer & Ruangan" },
                { "02", "Kondisi Pasien & TTV" },
                { "03", "Data Serah Terima Klinis" },
                { "04", "Aktifitas, Drainase & Luka" },
                { "05", "Investigasi & Penunjang" },
                { "06", "Terapi, Rencana & Dokumen" },
                { "07", "Otorisasi Serah Terima" }
        };

        for (int i = 0; i < sections.length; i++) {
            final String code = sections[i][0];
            String title = sections[i][1];
            JButton btnNav = new JButton(code + "   " + title);
            btnNav.setHorizontalAlignment(SwingConstants.LEFT);
            btnNav.setFocusPainted(false);
            btnNav.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            styleNavButton(btnNav, i == 0);
            btnNav.addActionListener(e -> scrollToSection(code));
            mapNavButtons.put(code, btnNav);
            navList.add(btnNav);
        }

        navCard.add(navHeader, BorderLayout.NORTH);
        navCard.add(navList, BorderLayout.CENTER);
        navCard.setMaximumSize(new Dimension(300, 260));

        // 4. KARTU INTEGRASI DATA SIMRS
        JPanel refCard = createBaseCardPanel();
        refCard.setLayout(new GridLayout(3, 1, 0, 6));
        refCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        refCard.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel lblRef = new JLabel("INTEGRASI DATA SIMRS");
        lblRef.setFont(FONT_MICRO_HEADER);
        lblRef.setForeground(COL_TEXT_MUTED);

        JButton btnPullIgd = createRoundedButton("Tarik TTV & Terapi dari IGD / RM 20", COL_TEAL_SOFT, COL_TEAL_DARK,
                COL_TEAL_BORDER);
        btnPullIgd.addActionListener(e -> tarikDataIgdDanRanap());

        JButton btnFillDocs = createRoundedButton("Isi Standar Dokumen Serah Terima", COL_TEAL_SOFT, COL_TEAL_DARK,
                COL_TEAL_BORDER);
        btnFillDocs.addActionListener(e -> fillStandardDocuments());

        refCard.add(lblRef);
        refCard.add(btnPullIgd);
        refCard.add(btnFillDocs);
        refCard.setMaximumSize(new Dimension(300, 115));

        sidebar.add(patientCard);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(progressCard);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(navCard);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(refCard);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    public void scrollToSection(String code) {
        for (Map.Entry<String, JButton> entry : mapNavButtons.entrySet()) {
            styleNavButton(entry.getValue(), entry.getKey().equals(code));
        }
        JPanel target = mapSectionCards.get(code);
        if (target != null && scrollFormMain != null) {
            Rectangle bounds = target.getBounds();
            scrollFormMain.getViewport().setViewPosition(new Point(0, Math.max(0, bounds.y - 8)));
        }
    }

    private void styleNavButton(JButton btn, boolean active) {
        btn.setRolloverEnabled(true);
        if (active) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setBackground(COL_TEAL_ACTIVE);
            btn.setForeground(COL_TEAL_DARK);
            btn.setOpaque(true);
            btn.setBorder(new CompoundBorder(
                    new MatteBorder(0, 4, 0, 0, COL_TEAL_PRIMARY),
                    new EmptyBorder(6, 10, 6, 10)));
        } else {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btn.setBackground(Color.WHITE);
            btn.setForeground(COL_TEXT_MUTED);
            btn.setOpaque(true);
            btn.setBorder(new EmptyBorder(6, 14, 6, 10));
        }
    }

    // =========================================================================
    // SEKSI 01: INFORMASI TRANSFER, KELUARGA & RUANGAN (RM 04-1)
    // =========================================================================

    private JPanel buildSection01InformasiTransfer() {
        JPanel card = createSectionCard(
                "01",
                "INFORMASI TRANSFER & RUANGAN",
                "Tanggal masuk & transfer, diagnosa, edukasi keluarga, asal pasien, tujuan pindah, dan cara pindah",
                true);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(8, 16, 12, 16));

        // 1. Waktu Masuk & Waktu Transfer
        JPanel waktuGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        waktuGrid.setOpaque(false);
        waktuGrid.add(wrapFieldWithMicroHeader("TANGGAL & JAM MASUK RAWAT", TanggalMasuk));
        waktuGrid.add(wrapFieldWithMicroHeader("TANGGAL & JAM TRANSFER / PINDAH", TanggalPindah));
        addHorizontalFormRow(rows, 0, "Waktu Masuk & Transfer :", "Pencatatan waktu rekam medis", waktuGrid, true);

        // 2. Diagnosa Utama & Sekunder
        JPanel diagGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        diagGrid.setOpaque(false);

        diagGrid.add(wrapFieldWithMicroHeader("DIAGNOSA UTAMA",
                createLookupFieldPanel(DiagnosaUtama, () -> showPenyakitLookupDialog(DiagnosaUtama))));
        diagGrid.add(wrapFieldWithMicroHeader("DIAGNOSA SEKUNDER", DiagnosaSekunder));
        addHorizontalFormRow(rows, 1, "Diagnosa :", "Diagnosa saat transfer", diagGrid, true);

        // 3. Keluarga yang Menyetujui & Edukasi
        JPanel keluargaBox = new JPanel(new BorderLayout(0, 8));
        keluargaBox.setOpaque(false);

        JPanel kelRow1 = new JPanel(new GridLayout(1, 3, 12, 0));
        kelRow1.setOpaque(false);
        kelRow1.add(wrapFieldWithMicroHeader("NAMA KELUARGA / PJ", NamaMenyetujui));

        HubunganMenyetujui = new widget.ComboBox();
        HubunganMenyetujui.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
                "Kakak", "Adik", "Saudara", "Keluarga", "Kakek", "Nenek", "Orang Tua", "Suami", "Istri",
                "Penanggung Jawab", "Menantu", "Ipar", "Mertua", "-"
        }));
        styleComboBox(HubunganMenyetujui);
        kelRow1.add(wrapFieldWithMicroHeader("HUBUNGAN KELUARGA", HubunganMenyetujui));
        kelRow1.add(wrapFieldWithMicroHeader("NO. TELEPON / HP", TNoTlpMenyetujui));

        JPanel kelRow2 = new JPanel(new BorderLayout(12, 0));
        kelRow2.setBackground(COL_CARD_HEADER);
        kelRow2.setBorder(new CompoundBorder(
                new RoundedBorder(COL_BORDER_DIVIDER, 8, 1.0f),
                new EmptyBorder(6, 10, 6, 10)));
        JLabel lblEdukasi = new JLabel("Pasien / keluarga sudah dijelaskan mengenai diagnosa & risiko transfer :");
        lblEdukasi.setFont(FONT_INPUT);
        lblEdukasi.setForeground(COL_TEXT_DARK);
        segPenjelasanDiagnosa.setPreferredSize(new Dimension(160, 32));
        kelRow2.add(lblEdukasi, BorderLayout.WEST);
        kelRow2.add(segPenjelasanDiagnosa, BorderLayout.EAST);

        keluargaBox.add(kelRow1, BorderLayout.NORTH);
        keluargaBox.add(kelRow2, BorderLayout.SOUTH);
        addHorizontalFormRow(rows, 2, "Keluarga & Persetujuan :", "Identitas penanggung jawab & edukasi", keluargaBox,
                true);

        // 4. Asal Pasien & Pindah Ke (2 Kotak Bersebelahan)
        JPanel asalTujuanGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        asalTujuanGrid.setOpaque(false);

        PilihRuangAsal = new widget.ComboBox();
        styleComboBox(PilihRuangAsal);
        PilihRuangTujuan = new widget.ComboBox();
        styleComboBox(PilihRuangTujuan);

        asalTujuanGrid.add(createWardTransferBox(
                "ASAL PASIEN (UNIT PENGIRIM)",
                PilihRuangAsal, AsalRuang, KelasAsalRuang, segKelasAsal,
                KdDokterMerawatAsal1, NmDokterMerawatAsal1,
                KdDokterMerawatAsal2, NmDokterMerawatAsal2,
                KdDokterMerawatAsal3, NmDokterMerawatAsal3,
                COL_CARD_HEADER, COL_BORDER_DIVIDER));

        asalTujuanGrid.add(createWardTransferBox(
                "PINDAH KE (UNIT PENERIMA)",
                PilihRuangTujuan, RuangSelanjutnya, KelasRuangSelanjutnya, segKelasTujuan,
                KdDokterMerawatTujuan1, NmDokterMerawatTujuan1,
                KdDokterMerawatTujuan2, NmDokterMerawatTujuan2,
                KdDokterMerawatTujuan3, NmDokterMerawatTujuan3,
                COL_TEAL_SOFT, COL_TEAL_BORDER));
        addHorizontalFormRow(rows, 3, "Ruang Asal & Tujuan :", "Ruangan, kelas & dokter penanggung jawab",
                asalTujuanGrid, true);

        // 5. Alasan Pindah Ruang
        JPanel alasanPanel = new JPanel(new BorderLayout(10, 0));
        alasanPanel.setOpaque(false);

        IndikasiPindah = new widget.ComboBox();
        IndikasiPindah.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
                "Kondisi Pasien Stabil", "Kondisi Pasien Tidak Ada Perubahan", "Kondisi Pasien Memburuk",
                "Fasilitas Kurang Memadai", "Fasilitas Butuh Lebih Baik", "Tenaga Membutuhkan Yang Lebih Ahli",
                "Tenaga Kurang", "Lain-lain"
        }));
        styleComboBox(IndikasiPindah);
        IndikasiPindah.setPreferredSize(new Dimension(240, 28));

        alasanPanel.add(IndikasiPindah, BorderLayout.WEST);
        alasanPanel.add(KeteranganIndikasiPindahRuang, BorderLayout.CENTER);
        addHorizontalFormRow(rows, 4, "Alasan Pindah :", "Indikasi klinis kepindahan", alasanPanel, true);

        // 6. Cara Pindah
        JPanel caraPindahPanel = new JPanel(new BorderLayout(10, 0));
        caraPindahPanel.setOpaque(false);
        segCaraPindah.setPreferredSize(new Dimension(400, 32));
        caraPindahPanel.add(segCaraPindah, BorderLayout.WEST);
        txtCaraPindahLain.setPreferredSize(new Dimension(200, 28));
        txtCaraPindahLain.setToolTipText("Keterangan cara pindah lainnya...");
        caraPindahPanel.add(txtCaraPindahLain, BorderLayout.CENTER);
        addHorizontalFormRow(rows, 5, "Cara Pindah :", "Metode pemindahan fisik pasien", caraPindahPanel, false);

        trackField(DiagnosaUtama);
        trackField(NamaMenyetujui);
        trackField(AsalRuang);
        trackField(RuangSelanjutnya);

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel createWardTransferBox(
            String title,
            widget.ComboBox cmbKamar, widget.TextBox txtRuangan, widget.TextBox txtKelas,
            SegmentedOptionButton segKelas,
            widget.TextBox kdDok1, widget.TextBox nmDok1,
            widget.TextBox kdDok2, widget.TextBox nmDok2,
            widget.TextBox kdDok3, widget.TextBox nmDok3,
            Color bgHeader, Color borderCol) {
        RoundedCardPanel box = new RoundedCardPanel(10);
        box.setLayout(new BorderLayout(0, 8));
        box.setBackground(Color.WHITE);
        box.setBorderColor(borderCol);
        box.setBorder(new EmptyBorder(8, 10, 10, 10));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, borderCol),
                new EmptyBorder(4, 6, 6, 6)));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_MICRO_HEADER);
        lblTitle.setForeground(COL_TEXT_DARK);
        head.add(lblTitle, BorderLayout.CENTER);
        box.add(head, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Baris Ruangan & Kelas
        JPanel rowRuang = new JPanel(new BorderLayout(6, 0));
        rowRuang.setOpaque(false);
        cmbKamar.setPreferredSize(new Dimension(165, 28));
        rowRuang.add(cmbKamar, BorderLayout.WEST);
        rowRuang.add(txtRuangan, BorderLayout.CENTER);

        content.add(wrapFieldWithMicroHeader("PILIHAN RUANGAN", rowRuang));
        content.add(Box.createVerticalStrut(6));

        segKelas.setPreferredSize(new Dimension(0, 62));
        content.add(wrapFieldWithMicroHeader("KELAS PERAWATAN", segKelas));
        content.add(Box.createVerticalStrut(8));

        // 3 Dokter Merawat menggunakan createLookupFieldPanel (bebas double border)
        content.add(wrapFieldWithMicroHeader("DOKTER PENANGGUNG JAWAB 1",
                createLookupFieldPanel(nmDok1, () -> showDokterLookupDialog(kdDok1, nmDok1))));
        content.add(Box.createVerticalStrut(4));
        content.add(wrapFieldWithMicroHeader("DOKTER PENANGGUNG JAWAB 2 (KONSUL)",
                createLookupFieldPanel(nmDok2, () -> showDokterLookupDialog(kdDok2, nmDok2))));
        content.add(Box.createVerticalStrut(4));
        content.add(wrapFieldWithMicroHeader("DOKTER PENANGGUNG JAWAB 3 (KONSUL)",
                createLookupFieldPanel(nmDok3, () -> showDokterLookupDialog(kdDok3, nmDok3))));

        box.add(content, BorderLayout.CENTER);
        return box;
    }

    // =========================================================================
    // SEKSI 02: KONDISI PASIEN SAAT TRANSFER (RM 04-1)
    // =========================================================================

    private JPanel buildSection02KondisiPasien() {
        JPanel card = createSectionCard(
                "02",
                "KONDISI PASIEN SAAT TRANSFER",
                "Observasi tanda vital, tingkat kesadaran, skala GCS, evaluasi nyeri, dan dukungan terapi oksigen",
                true);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(8, 16, 12, 16));

        // 1. Observasi Terakhir Pukul & Kesadaran
        JPanel obsKesPanel = new JPanel(new BorderLayout(14, 0));
        obsKesPanel.setOpaque(false);
        JPanel jamBox = wrapFieldWithMicroHeader("OBSERVASI TERAKHIR PUKUL", ObservasiTerakhirPukul);
        jamBox.setPreferredSize(new Dimension(350, 48));
        obsKesPanel.add(jamBox, BorderLayout.WEST);
        segKesadaran.setPreferredSize(new Dimension(0, 32));
        obsKesPanel.add(wrapFieldWithMicroHeader("TINGKAT KESADARAN", segKesadaran), BorderLayout.CENTER);
        addHorizontalFormRow(rows, 0, "Kondisi & Kesadaran :", "Waktu observasi & kesadaran", obsKesPanel, true);

        // 2. Skala GCS (E, M, V) & Total Otomatis
        JPanel gcsGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        gcsGrid.setOpaque(false);

        lblTotalGcsBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalGcsBadge.setForeground(COL_TEAL_DARK);
        lblTotalGcsBadge.setOpaque(false);
        lblTotalGcsBadge.setBackground(COL_TEAL_SOFT);
        lblTotalGcsBadge.putClientProperty("customBorderColor", COL_TEAL_BORDER);
        lblTotalGcsBadge.setBorder(new EmptyBorder(4, 8, 4, 8));

        gcsGrid.add(wrapFieldWithMicroHeader("EYE (E) [1-4]", txtGcsE));
        gcsGrid.add(wrapFieldWithMicroHeader("MOTORIK (M) [1-6]", txtGcsM));
        gcsGrid.add(wrapFieldWithMicroHeader("VERBAL (V) [1-5]", txtGcsV));
        gcsGrid.add(wrapFieldWithMicroHeader("TOTAL SKALA GCS", lblTotalGcsBadge));
        addHorizontalFormRow(rows, 1, "Skala GCS :", "E, M, V & kalkulasi total", gcsGrid, true);

        // 3. Tanda Vital & Nyeri (TD, HR, RR, Temp, SpO2, Nyeri)
        JPanel ttvGrid = new JPanel(new GridLayout(1, 6, 10, 0));
        ttvGrid.setOpaque(false);

        JPanel tdInputs = new JPanel(new GridLayout(1, 2, 4, 0));
        tdInputs.setOpaque(false);
        tdInputs.add(txtTdSistol);
        tdInputs.add(txtTdDiastol);

        ttvGrid.add(wrapFieldWithMicroHeader("TD (mmHg)", tdInputs));
        ttvGrid.add(wrapFieldWithMicroHeader("HR (x/mnt)", txtHr));
        ttvGrid.add(wrapFieldWithMicroHeader("RR (x/mnt)", txtRr));
        ttvGrid.add(wrapFieldWithMicroHeader("SUHU (\u00B0C)", txtTemp));
        ttvGrid.add(wrapFieldWithMicroHeader("SpO2 (%)", txtSpo2));

        TingkatNyeriTransfer = new widget.ComboBox();
        TingkatNyeriTransfer.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        styleComboBox(TingkatNyeriTransfer);
        ttvGrid.add(wrapFieldWithMicroHeader("SKALA NYERI (0-10)", TingkatNyeriTransfer));
        addHorizontalFormRow(rows, 2, "Tanda Vital & Nyeri :", "Hemodinamik & skala nyeri", ttvGrid, true);

        // Keterangan Nyeri
        JPanel nyeriKetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        nyeriKetPanel.setOpaque(false);
        lblNyeriKeterangan.setFont(FONT_INPUT);
        lblNyeriKeterangan.setForeground(COL_EMERALD_DARK);
        nyeriKetPanel.add(lblNyeriKeterangan);
        addHorizontalFormRow(rows, 3, "Keterangan Nyeri :", "Deskripsi tingkatan rasa sakit", nyeriKetPanel, true);

        // 4. Terapi Oksigen
        JPanel o2Container = new JPanel(new BorderLayout(0, 8));
        o2Container.setOpaque(false);

        JPanel o2Top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        o2Top.setOpaque(false);
        segTherapyOksigen.setPreferredSize(new Dimension(160, 32));
        o2Top.add(segTherapyOksigen);
        JLabel lblAliran = new JLabel("Aliran O2 (L/mnt) :");
        lblAliran.setFont(FONT_INPUT);
        o2Top.add(lblAliran);
        txtAliranOksigen.setPreferredSize(new Dimension(80, 28));
        o2Top.add(txtAliranOksigen);

        JPanel o2Checks = new JPanel(new GridLayout(2, 4, 8, 6));
        o2Checks.setOpaque(false);
        for (JCheckBox chk : new JCheckBox[] {
                chkO2NasalKanule, chkO2NasalKateter, chkO2FaceMask, chkO2SungkupRebreathing,
                chkO2SungkupNonRebreathing, chkO2Tracheostomy, chkO2Ett
        }) {
            styleCheckBoxChip(chk);
            o2Checks.add(chk);
        }

        o2Container.add(o2Top, BorderLayout.NORTH);
        o2Container.add(o2Checks, BorderLayout.CENTER);
        addHorizontalFormRow(rows, 4, "Therapy Oksigen :", "Status & pilihan jenis alat", o2Container, false);

        trackField(txtTdSistol);
        trackField(txtHr);
        trackField(txtRr);
        trackField(txtTemp);
        trackField(txtSpo2);

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // SEKSI 03: DATA SERAH TERIMA KLINIS (RM 04-1)
    // =========================================================================

    private JPanel buildSection03DataSerahTerima1() {
        JPanel card = createSectionCard(
                "03",
                "DATA SERAH TERIMA KLINIS",
                "Line invasif, prosedur pembedahan/invasif, kewaspadaan, nutrisi, dan eliminasi urin & fekal",
                true);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(8, 16, 12, 16));

        // 1. Line Invasif
        JPanel linePanel = new JPanel(new BorderLayout(14, 0));
        linePanel.setOpaque(false);
        segLineInvasif.setPreferredSize(new Dimension(400, 32));
        linePanel.add(segLineInvasif, BorderLayout.WEST);
        linePanel.add(wrapFieldWithMicroHeader("TANGGAL PEMASANGAN LINE", LineInvasifTanggal), BorderLayout.CENTER);
        addHorizontalFormRow(rows, 0, "Line Invasif :", "CVP/PIC, IV Line & tanggal pasang", linePanel, true);

        // 2. Prosedur Pembedahan / Invasif yang Sudah Dilakukan
        JPanel prosedurStack = new JPanel(new GridLayout(3, 1, 0, 6));
        prosedurStack.setOpaque(false);
        prosedurStack.add(createProcedureDateRow("1.", txtProsedur1, txtTglProsedur1));
        prosedurStack.add(createProcedureDateRow("2.", txtProsedur2, txtTglProsedur2));
        prosedurStack.add(createProcedureDateRow("3.", txtProsedur3, txtTglProsedur3));
        addHorizontalFormRow(rows, 1, "Prosedur Invasif :", "Tindakan bedah / invasif & tanggal", prosedurStack, true);

        // 3. Kewaspadaan / Precaution
        JPanel precGrid = new JPanel(new GridLayout(1, 4, 10, 0));
        precGrid.setOpaque(false);
        for (JCheckBox chk : new JCheckBox[] { chkPrecautionStandar, chkPrecautionKontak, chkPrecautionAirborne,
                chkPrecautionDroplet }) {
            styleCheckBoxChip(chk);
            precGrid.add(chk);
        }
        addHorizontalFormRow(rows, 2, "Kewaspadaan :", "Standar, kontak, airborne & droplet", precGrid, true);

        // 4. Nutrisi
        JPanel nutrisiBox = new JPanel(new BorderLayout(0, 8));
        nutrisiBox.setOpaque(false);
        segNutrisi.setPreferredSize(new Dimension(0, 36));
        nutrisiBox.add(segNutrisi, BorderLayout.NORTH);

        JPanel nutDetail = new JPanel(new GridLayout(1, 2, 14, 0));
        nutDetail.setOpaque(false);

        JPanel batasanCairanRow = new JPanel(new BorderLayout(6, 0));
        batasanCairanRow.setOpaque(false);
        styleCheckBoxChip(chkBatasanCairan);
        chkBatasanCairan.setPreferredSize(new Dimension(140, 30));
        batasanCairanRow.add(chkBatasanCairan, BorderLayout.WEST);
        batasanCairanRow.add(wrapFieldWithMicroHeader("JUMLAH (CC)", TNutrisiBatasanCairanCc), BorderLayout.CENTER);

        JPanel dietKhususRow = new JPanel(new BorderLayout(6, 0));
        dietKhususRow.setOpaque(false);
        styleCheckBoxChip(chkDietKhusus);
        chkDietKhusus.setPreferredSize(new Dimension(130, 30));
        dietKhususRow.add(chkDietKhusus, BorderLayout.WEST);
        dietKhususRow.add(wrapFieldWithMicroHeader("PENJELASAN DIET", TNutrisiDietKhususJelaskan), BorderLayout.CENTER);

        nutDetail.add(batasanCairanRow);
        nutDetail.add(dietKhususRow);
        nutrisiBox.add(nutDetail, BorderLayout.CENTER);
        addHorizontalFormRow(rows, 3, "Nutrisi & Diet :", "Puasa, oral, NGT & batasan diet", nutrisiBox, true);

        // 5. Eliminasi (BAB & BAK)
        JPanel elimBox = new JPanel(new GridLayout(2, 1, 0, 8));
        elimBox.setOpaque(false);
        segBab.setPreferredSize(new Dimension(0, 32));
        elimBox.add(wrapFieldWithMicroHeader("ELIMINASI BAB", segBab));

        JPanel bakRow = new JPanel(new BorderLayout(10, 0));
        bakRow.setOpaque(false);
        segBak.setPreferredSize(new Dimension(400, 32));
        bakRow.add(segBak, BorderLayout.WEST);

        JPanel kateterDetail = new JPanel(new GridLayout(1, 2, 8, 0));
        kateterDetail.setOpaque(false);
        kateterDetail.add(wrapFieldWithMicroHeader("NO. KATETER", TEliminasiBakKateterNo));
        kateterDetail.add(wrapFieldWithMicroHeader("TGL PASANG KATETER", DTPEliminasiBakKateterTanggalPasang));
        bakRow.add(kateterDetail, BorderLayout.CENTER);

        elimBox.add(wrapFieldWithMicroHeader("ELIMINASI BAK", bakRow));
        addHorizontalFormRow(rows, 4, "Eliminasi :", "Pola defekasi & miksi / kateter", elimBox, false);

        trackField(txtProsedur1);

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProcedureDateRow(String prefix, widget.TextBox tfProc, widget.TextBox tfDate) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(prefix);
        l.setFont(FONT_ROW_LABEL);
        l.setPreferredSize(new Dimension(20, 28));

        tfDate.setPreferredSize(new Dimension(140, 28));
        tfDate.setToolTipText("Format tanggal dd-MM-yyyy...");

        p.add(l, BorderLayout.WEST);
        p.add(tfProc, BorderLayout.CENTER);
        p.add(tfDate, BorderLayout.EAST);
        return p;
    }

    // =========================================================================
    // SEKSI 04: AKTIFITAS, DRAINASE & INTEGRITAS KULIT/LUKA (RM 04-2)
    // =========================================================================

    private JPanel buildSection04AktifitasKulitLuka() {
        JPanel card = createSectionCard(
                "04",
                "AKTIFITAS, DRAINASE & LUKA",
                "Status kemandirian fungsional, mobilisasi, sistem drainase, dan inspeksi integritas kulit / luka",
                true);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(8, 16, 12, 16));

        // 1. Aktifitas & Mobilisasi
        JPanel aktMobGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        aktMobGrid.setOpaque(false);
        segAktifitas.setPreferredSize(new Dimension(0, 32));
        segMobilisasi.setPreferredSize(new Dimension(0, 32));
        aktMobGrid.add(wrapFieldWithMicroHeader("AKTIFITAS", segAktifitas));
        aktMobGrid.add(wrapFieldWithMicroHeader("MOBILISASI", segMobilisasi));
        addHorizontalFormRow(rows, 0, "Status Fungsional :", "Tingkat aktifitas & mobilisasi", aktMobGrid, true);

        // 2. Drainase
        JPanel drainPanel = new JPanel(new BorderLayout(10, 0));
        drainPanel.setOpaque(false);

        JPanel drainChecks = new JPanel(new GridLayout(1, 4, 8, 0));
        drainChecks.setOpaque(false);
        for (JCheckBox chk : new JCheckBox[] { chkDrainWsd, chkDrainStoma, chkDrainLuka, chkDrainLain }) {
            styleCheckBoxChip(chk);
            drainChecks.add(chk);
        }
        drainPanel.add(drainChecks, BorderLayout.CENTER);
        TDrainaseLainKet.setPreferredSize(new Dimension(220, 28));
        drainPanel.add(TDrainaseLainKet, BorderLayout.EAST);
        addHorizontalFormRow(rows, 1, "Drainase :", "WSD, stoma & drain luka", drainPanel, true);

        // 3. Integritas Kulit / Luka
        JPanel kulitContainer = new JPanel(new BorderLayout(0, 8));
        kulitContainer.setOpaque(false);

        styleCheckBoxChip(chkLukaTidakAda);
        kulitContainer.add(chkLukaTidakAda, BorderLayout.NORTH);

        String[] cols = { "Jenis Luka", "Ukuran (cm)", "Lokasi Tubuh", "Kondisi Luka" };
        Object[][] data = {
                { "Luka Operasi", "", "", "" },
                { "Dekubitus", "", "", "" },
                { "Skin Lost", "", "", "" },
                { "Gangren", "", "", "" }
        };
        modelTabelLuka = new DefaultTableModel(data, cols);
        tableLuka = new JTable(modelTabelLuka);
        tableLuka.setRowHeight(26);
        tableLuka.setFont(FONT_INPUT);
        tableLuka.getTableHeader().setFont(FONT_MICRO_HEADER);

        JScrollPane spLuka = new RoundedScrollPane(tableLuka, 8);
        spLuka.setPreferredSize(new Dimension(400, 132));
        kulitContainer.add(spLuka, BorderLayout.CENTER);

        JPanel lukaLainRow = new JPanel(new BorderLayout(8, 0));
        lukaLainRow.setOpaque(false);
        JLabel lblLain = new JLabel("Luka Lain-lain :");
        lblLain.setFont(FONT_SUBLABEL);
        lukaLainRow.add(lblLain, BorderLayout.WEST);
        lukaLainRow.add(TLukaLainKet, BorderLayout.CENTER);
        kulitContainer.add(lukaLainRow, BorderLayout.SOUTH);

        addHorizontalFormRow(rows, 2, "Integritas Kulit / Luka :", "Inspeksi dekubitus, operasi & luka", kulitContainer,
                false);

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // SEKSI 05: INVESTIGASI ABNORMAL & PENUNJANG (RM 04-2)
    // =========================================================================

    private JPanel buildSection05InvestigasiAlatPenunjang() {
        JPanel card = createSectionCard(
                "05",
                "INVESTIGASI ABNORMAL & PENUNJANG",
                "Temuan klinis abnormal, kewaspadaan khusus, riwayat alergi, alat bantu, dan hasil penunjang",
                true);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(8, 16, 12, 16));

        // 1. Hasil Investigasi Abnormal
        addHorizontalFormRow(rows, 0, "Investigasi Abnormal :", "Temuan klinis & laboratorium khusus",
                wrapTextArea(txtHasilInvestigasiAbnormal, 60), true);

        // 2. Tindakan / Kebutuhan Khusus & Riwayat Alergi
        JPanel khususContainer = new JPanel(new BorderLayout(0, 8));
        khususContainer.setOpaque(false);

        JPanel khususChecks = new JPanel(new GridLayout(1, 4, 8, 0));
        khususChecks.setOpaque(false);
        for (JCheckBox chk : new JCheckBox[] { chkKhususJatuh, chkKhususRestrain, chkKhususRawatLuka,
                chkKhususHygiene }) {
            styleCheckBoxChip(chk);
            khususChecks.add(chk);
        }

        JPanel alergiPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        alergiPanel.setOpaque(false);
        segRiwayatAlergi.setPreferredSize(new Dimension(0, 32));
        alergiPanel.add(wrapFieldWithMicroHeader("RIWAYAT ALERGI (TIDAK / YA)", segRiwayatAlergi));
        alergiPanel.add(wrapFieldWithMicroHeader("JENIS ALERGI", TJenisAlergi));
        alergiPanel.add(wrapFieldWithMicroHeader("REAKSI ALERGI", TReaksiAlergi));

        khususContainer.add(khususChecks, BorderLayout.NORTH);
        khususContainer.add(alergiPanel, BorderLayout.CENTER);
        addHorizontalFormRow(rows, 1, "Kebutuhan Khusus & Alergi :", "Resiko jatuh, restrain & alergi", khususContainer,
                true);

        // 3. Alat Bantu yang Digunakan
        JPanel alatContainer = new JPanel(new BorderLayout(0, 6));
        alatContainer.setOpaque(false);
        JPanel alatChecks = new JPanel(new GridLayout(1, 5, 8, 0));
        alatChecks.setOpaque(false);
        for (JCheckBox chk : new JCheckBox[] { chkAlatTanpa, chkAlatDengar, chkAlatKacamata, chkAlatGigiPalsu,
                chkAlatTongkat }) {
            styleCheckBoxChip(chk);
            alatChecks.add(chk);
        }
        alatContainer.add(alatChecks, BorderLayout.NORTH);
        alatContainer.add(wrapFieldWithMicroHeader("ALAT BANTU LAINNYA", TAlatBantuLainKet), BorderLayout.CENTER);
        addHorizontalFormRow(rows, 2, "Alat Bantu :", "Kacamata, gigi palsu, kruk, dll", alatContainer, true);

        // 4. Tindakan yang Sudah Dilakukan
        addHorizontalFormRow(rows, 3, "Tindakan Sudah Dilakukan :", "Penanganan medis & keperawatan",
                wrapTextArea(txtTindakanSudahDilakukan, 60), true);

        // 5. Pemeriksaan Penunjang
        JPanel penunjangStack = new JPanel(new GridLayout(4, 1, 0, 6));
        penunjangStack.setOpaque(false);

        penunjangStack.add(createPenunjangRow(chkPenunjangLab, TPenunjangLaboratoriumKet));
        penunjangStack.add(createPenunjangRow(chkPenunjangRad, TPenunjangRadiologiKet));
        penunjangStack.add(createPenunjangRow(chkPenunjangCtScan, TPenunjangCtScanKet));
        penunjangStack.add(createPenunjangRow(chkPenunjangLain, TPenunjangLainKet));

        addHorizontalFormRow(rows, 4, "Pemeriksaan Penunjang :", "Hasil lab, radiologi, CT Scan & EKG", penunjangStack,
                false);

        trackField(txtHasilInvestigasiAbnormal);
        trackField(txtTindakanSudahDilakukan);

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel createPenunjangRow(JCheckBox chk, widget.TextBox tf) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        styleCheckBoxChip(chk);
        chk.setPreferredSize(new Dimension(150, 28));
        p.add(chk, BorderLayout.WEST);
        p.add(tf, BorderLayout.CENTER);
        return p;
    }

    // =========================================================================
    // SEKSI 06: TERAPI, RENCANA & DOKUMEN (RM 04-2)
    // =========================================================================

    private JPanel buildSection06TherapyRencanaDokumen() {
        JPanel card = createSectionCard(
                "06",
                "TERAPI, RENCANA & DOKUMEN",
                "Terapi obat berjalan (1-5), rencana tindak lanjut (1-3), masalah utama keperawatan, dan dokumen serah terima",
                true);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(8, 16, 12, 16));

        // 1. Terapi yang Diberikan (1 - 5)
        JPanel therapyStack = new JPanel(new GridLayout(5, 1, 0, 4));
        therapyStack.setOpaque(false);
        therapyStack.add(createNumberedInputRow("1.", txtTherapy1));
        therapyStack.add(createNumberedInputRow("2.", txtTherapy2));
        therapyStack.add(createNumberedInputRow("3.", txtTherapy3));
        therapyStack.add(createNumberedInputRow("4.", txtTherapy4));
        therapyStack.add(createNumberedInputRow("5.", txtTherapy5));
        addHorizontalFormRow(rows, 0, "Therapy yang Diberikan :", "Terapi obat & cairan infus berjalan", therapyStack,
                true);

        // 2. Rencana Tindak Lanjut (1 - 3)
        JPanel rtlStack = new JPanel(new GridLayout(3, 1, 0, 4));
        rtlStack.setOpaque(false);
        rtlStack.add(createNumberedInputRow("1.", txtRtl1));
        rtlStack.add(createNumberedInputRow("2.", txtRtl2));
        rtlStack.add(createNumberedInputRow("3.", txtRtl3));
        addHorizontalFormRow(rows, 1, "Rencana Tindak Lanjut :", "Instruksi asuhan ruangan lanjutan", rtlStack, true);

        // 3. Masalah Utama Keperawatan (1 - 3)
        JPanel masalahStack = new JPanel(new GridLayout(3, 1, 0, 4));
        masalahStack.setOpaque(false);
        masalahStack.add(createNumberedInputRow("1.", txtMasalahKep1));
        masalahStack.add(createNumberedInputRow("2.", txtMasalahKep2));
        masalahStack.add(createNumberedInputRow("3.", txtMasalahKep3));
        addHorizontalFormRow(rows, 2, "Masalah Keperawatan :", "Diagnosa / masalah keperawatan aktif", masalahStack,
                true);

        // 4. Dokumen yang Diserahterimakan (1 - 5)
        JPanel dokumenStack = new JPanel(new GridLayout(5, 1, 0, 4));
        dokumenStack.setOpaque(false);
        dokumenStack.add(createNumberedInputRow("1.", txtDokumen1));
        dokumenStack.add(createNumberedInputRow("2.", txtDokumen2));
        dokumenStack.add(createNumberedInputRow("3.", txtDokumen3));
        dokumenStack.add(createNumberedInputRow("4.", txtDokumen4));
        dokumenStack.add(createNumberedInputRow("5.", txtDokumen5));
        addHorizontalFormRow(rows, 3, "Dokumen Serah Terima :", "Kelengkapan berkas RM & lampiran", dokumenStack,
                false);

        trackField(txtTherapy1);
        trackField(txtRtl1);
        trackField(txtMasalahKep1);
        trackField(txtDokumen1);

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel createNumberedInputRow(String num, widget.TextBox tf) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(num);
        l.setFont(FONT_ROW_LABEL);
        l.setPreferredSize(new Dimension(20, 26));
        p.add(l, BorderLayout.WEST);
        p.add(tf, BorderLayout.CENTER);
        return p;
    }

    // =========================================================================
    // SEKSI 07: OTORISASI SERAH TERIMA & BARCODE TTE (RM 04-2)
    // =========================================================================

    private JPanel buildSection07OtorisasiSerahTerima() {
        JPanel card = createSectionCard(
                "07",
                "OTORISASI SERAH TERIMA",
                "Petugas yang menyerahkan & menerima, dokter penanggung jawab, serta tanda tangan persetujuan keluarga",
                false);

        JPanel mainBody = new JPanel();
        mainBody.setLayout(new BoxLayout(mainBody, BoxLayout.Y_AXIS));
        mainBody.setOpaque(false);
        mainBody.setBorder(new EmptyBorder(12, 16, 16, 16));

        // 1. Baris Input Petugas Pengirim & Penerima
        JPanel petugasRow = new JPanel(new GridLayout(1, 2, 16, 0));
        petugasRow.setOpaque(false);

        JPanel p1 = createLookupFieldPanel(NmPetugasMenyerahkan,
                () -> showPetugasLookupDialog(KdPetugasMenyerahkan, NmPetugasMenyerahkan));
        JPanel p2 = createLookupFieldPanel(NmPetugasMenerima,
                () -> showPetugasLookupDialog(KdPetugasMenerima, NmPetugasMenerima));

        petugasRow.add(wrapFieldWithMicroHeader("PETUGAS YANG MENYERAHKAN (RUANG ASAL)", p1));
        petugasRow.add(wrapFieldWithMicroHeader("PETUGAS YANG MENERIMA (RUANG TUJUAN)", p2));
        mainBody.add(petugasRow);
        mainBody.add(Box.createVerticalStrut(12));

        // 2. Baris Dokter Merawat & Persetujuan Keluarga
        JPanel dokKelRow = new JPanel(new GridLayout(1, 2, 16, 0));
        dokKelRow.setOpaque(false);

        JPanel pDok = createLookupFieldPanel(NmDokterMerawatTujuan1,
                () -> showDokterLookupDialog(KdDokterMerawatTujuan1, NmDokterMerawatTujuan1));
        dokKelRow.add(wrapFieldWithMicroHeader("DOKTER PENANGGUNG JAWAB (DPJP)", pDok));

        JPanel kelBox = new JPanel(new GridLayout(1, 2, 8, 0));
        kelBox.setOpaque(false);
        kelBox.add(NamaMenyetujui);
        kelBox.add(HubunganMenyetujui);
        dokKelRow.add(wrapFieldWithMicroHeader("PASIEN / PENANGGUNG JAWAB (NAMA & HUBUNGAN)", kelBox));
        mainBody.add(dokKelRow);
        mainBody.add(Box.createVerticalStrut(14));

        // 3. Banner Informatif TTE (Standar Form SIMRS RM 20)
        RoundedCardPanel bannerInfoTte = new RoundedCardPanel(10);
        bannerInfoTte.setLayout(new BorderLayout(12, 0));
        bannerInfoTte.setBackground(COL_TEAL_SOFT);
        bannerInfoTte.setBorderColor(COL_TEAL_BORDER);
        bannerInfoTte.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblInfoIcon = new JLabel("ℹ");
        lblInfoIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInfoIcon.setForeground(COL_TEAL_DARK);

        JLabel lblInfoTte = new JLabel(
                "<html><b>INFORMASI TTE:</b> Pengesahan & pembubuhan Tanda Tangan Elektronik (TTE) resmi dilakukan melalui Tab <b>Data Riwayat Transfer</b> setelah data tersimpan. QR Code verifikasi TTE otomatis dicantumkan pada lembar pratinjau dan hasil cetak dokumen rekam medis.</html>");
        lblInfoTte.setFont(FONT_INPUT);
        lblInfoTte.setForeground(COL_TEAL_DARK);

        bannerInfoTte.add(lblInfoIcon, BorderLayout.WEST);
        bannerInfoTte.add(lblInfoTte, BorderLayout.CENTER);
        mainBody.add(bannerInfoTte);

        card.add(mainBody, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // WORKSPACE 2: DATA RIWAYAT TRANSFER (DUAL-PANE SPLIT VIEW)
    // =========================================================================

    private JPanel buildHistoryWorkspace() {
        JPanel historyPanel = new JPanel(new BorderLayout(0, 8));
        historyPanel.setBackground(COL_BG_MAIN);
        historyPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // 1. SISI KIRI: TABEL RIWAYAT + FILTER
        JPanel leftPane = createBaseCardPanel();
        leftPane.setLayout(new BorderLayout(0, 8));
        leftPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Filter Header
        JPanel filterBar = new JPanel(new BorderLayout(6, 4));
        filterBar.setOpaque(false);

        JLabel lblHistTitle = new JLabel("Riwayat Transfer Pasien Antar Ruangan");
        lblHistTitle.setFont(FONT_SECTION);
        lblHistTitle.setForeground(COL_TEXT_DARK);
        filterBar.add(lblHistTitle, BorderLayout.NORTH);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterRow.setOpaque(false);

        DTPCari1 = new widget.Tanggal();
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setPreferredSize(new Dimension(100, 28));
        styleTanggal(DTPCari1);

        DTPCari2 = new widget.Tanggal();
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setPreferredSize(new Dimension(100, 28));
        styleTanggal(DTPCari2);

        TCari = createTextBox(100, true);
        TCari.setPreferredSize(new Dimension(160, 28));
        TCari.setToolTipText("Cari No. Rawat / No. RM / Nama / Ruangan...");

        BtnCari = createRoundedButton("Cari", COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        BtnCari.setPreferredSize(new Dimension(60, 28));
        BtnCari.addActionListener(e -> runBackground(this::tampil));

        BtnAll = createRoundedButton("Semua", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        BtnAll.setPreferredSize(new Dimension(65, 28));
        BtnAll.addActionListener(e -> {
            TCari.setText("");
            runBackground(this::tampil);
        });

        LCount = new widget.Label();
        LCount.setText("0");
        LCount.setFont(new Font("Segoe UI", Font.BOLD, 11));
        LCount.setForeground(COL_TEAL_DARK);

        filterRow.add(new JLabel("Periode :"));
        filterRow.add(DTPCari1);
        filterRow.add(new JLabel("s.d."));
        filterRow.add(DTPCari2);
        filterRow.add(TCari);
        filterRow.add(BtnCari);
        filterRow.add(BtnAll);
        filterRow.add(new JLabel("Jml Data :"));
        filterRow.add(LCount);

        filterBar.add(filterRow, BorderLayout.CENTER);
        leftPane.add(filterBar, BorderLayout.NORTH);

        // Tabel Data
        tbObat = new widget.Table();
        tbObat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbObat.setRowHeight(28);
        tbObat.setFont(FONT_INPUT);
        tbObat.getTableHeader().setFont(FONT_MICRO_HEADER);

        tbObat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tbObat.getSelectedRow() != -1) {
                    getData();
                    updateStateButtonsTab2(tbObat.getSelectedRow());
                    if (e.getClickCount() == 2) {
                        bukaDialogPreviewFullscreen(
                                tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString(),
                                tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
                    }
                }
            }
        });

        tbObat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (tbObat.getSelectedRow() != -1) {
                    getData();
                    updateStateButtonsTab2(tbObat.getSelectedRow());
                }
            }
        });

        JScrollPane spTable = new RoundedScrollPane(tbObat, 8);
        leftPane.add(spTable, BorderLayout.CENTER);

        // 2. SISI KANAN: LIVE PRATINJAU DOKUMEN HTML (DUAL-PANE)
        JPanel rightPane = createBaseCardPanel();
        rightPane.setLayout(new BorderLayout(0, 6));
        rightPane.setBorder(new EmptyBorder(10, 12, 10, 12));

        // Toolbar Atas Pratinjau
        JPanel prevToolBar = new JPanel(new BorderLayout());
        prevToolBar.setOpaque(false);
        JLabel lblPrevTitle = new JLabel("PRATINJAU DOKUMEN RM 04");
        lblPrevTitle.setFont(FONT_MICRO_HEADER);
        lblPrevTitle.setForeground(COL_TEXT_MUTED);

        JPanel prevBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        prevBtns.setOpaque(false);

        JButton btnFullscreen = createRoundedButton("Pratinjau Layar Penuh", COL_TEAL_SOFT, COL_TEAL_DARK,
                COL_TEAL_BORDER);
        btnFullscreen.setPreferredSize(new Dimension(150, 26));
        btnFullscreen.addActionListener(e -> {
            if (tbObat.getSelectedRow() != -1) {
                bukaDialogPreviewFullscreen(
                        tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString(),
                        tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih data transfer terlebih dahulu pada tabel riwayat!");
            }
        });

        JButton btnPrintDirect = createRoundedButton("Cetak Dokumen", COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnPrintDirect.setPreferredSize(new Dimension(115, 26));
        btnPrintDirect.addActionListener(e -> cetakLaporanTransfer("Lembar"));

        prevBtns.add(btnFullscreen);
        prevBtns.add(btnPrintDirect);

        prevToolBar.add(lblPrevTitle, BorderLayout.WEST);
        prevToolBar.add(prevBtns, BorderLayout.EAST);
        rightPane.add(prevToolBar, BorderLayout.NORTH);

        // Editor Pane HTML
        LoadHTML = new JEditorPane();
        LoadHTML.setContentType("text/html");
        LoadHTML.setEditable(false);

        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                "body { font-family: Segoe UI, Tahoma, sans-serif; font-size: 10px; color: #0F172A; margin: 8px; }");
        styleSheet.addRule("h1 { font-size: 12px; margin: 4px 0; font-weight: bold; }");
        styleSheet.addRule("h2 { font-size: 11px; margin: 6px 0 2px 0; font-weight: bold; color: #0F766E; }");
        styleSheet.addRule("table { width: 100%; border-collapse: collapse; margin-bottom: 6px; }");
        styleSheet
                .addRule("td { border: 1px solid #CBD5E1; padding: 4px 6px; font-size: 9.5px; vertical-align: top; }");
        styleSheet
                .addRule(".header-cell { background-color: #F8FAFC; font-weight: bold; color: #475569; width: 28%; }");
        styleSheet.addRule(
                ".section-banner { background-color: #F0FDFA; color: #0F766E; font-weight: bold; font-size: 10px; padding: 4px 6px; }");
        styleSheet.addRule(".tte-cell { text-align: center; padding: 6px; background-color: #FAFAFA; }");

        JScrollPane spHtml = new RoundedScrollPane(LoadHTML, 8);
        rightPane.add(spHtml, BorderLayout.CENTER);

        // Pasang Split Pane Dual-Pane (~60:40)
        splitRiwayat = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
        splitRiwayat.setResizeWeight(0.60);
        splitRiwayat.setDividerSize(6);
        splitRiwayat.setBorder(null);

        historyPanel.add(splitRiwayat, BorderLayout.CENTER);
        return historyPanel;
    }

    // =========================================================================
    // MODAL PRATINJAU DOKUMEN LAYAR PENUH (FULLSCREEN MODAL)
    // =========================================================================

    public void bukaDialogPreviewFullscreen(String noRawat, String tanggalMasuk) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih data transfer terlebih dahulu!");
            return;
        }

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parentWin, "Pratinjau Dokumen Transfer Pasien Antar Ruangan (RM 04)",
                Dialog.ModalityType.APPLICATION_MODAL);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        dlg.setSize(Math.min(1150, screen.width - 40), Math.min(880, screen.height - 60));
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // Header Toolbar Modal
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(COL_HEADER_DARK);
        topBar.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel lblTitle = new JLabel("PRATINJAU DOKUMEN TRANSFER RM 04 — NO. RAWAT: " + noRawat);
        lblTitle.setFont(FONT_SECTION);
        lblTitle.setForeground(Color.WHITE);

        JPanel pnlAksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlAksi.setOpaque(false);

        JButton btnPrintModal = createRoundedButton("Cetak Dokumen", COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnPrintModal.addActionListener(e -> {
            dlg.dispose();
            cetakLaporanTransfer("Lembar");
        });

        JButton btnTutup = createRoundedButton("Tutup (ESC)", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnTutup.addActionListener(e -> dlg.dispose());

        pnlAksi.add(btnPrintModal);
        pnlAksi.add(btnTutup);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(pnlAksi, BorderLayout.EAST);
        dlg.add(topBar, BorderLayout.NORTH);

        // Body Preview HTML
        JEditorPane editorFullscreen = new JEditorPane();
        editorFullscreen.setContentType("text/html");
        editorFullscreen.setEditable(false);
        editorFullscreen.setText(bangunDokumenHtmlLengkap(noRawat, tanggalMasuk));
        editorFullscreen.setCaretPosition(0);

        dlg.add(new JScrollPane(editorFullscreen), BorderLayout.CENTER);

        // Shortcut ESC untuk menutup
        dlg.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeModal");
        dlg.getRootPane().getActionMap().put("closeModal", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlg.dispose();
            }
        });

        dlg.setVisible(true);
    }

    // =========================================================================
    // ACTION BAR BAWAH (SIMPAN, BATAL, HAPUS, GANTI, CETAK, KELUAR)
    // =========================================================================

    private JPanel buildBottomActionBar() {
        JPanel footer = new JPanel(new BorderLayout(8, 0)) {
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
                new MatteBorder(1, 0, 0, 0, COL_BORDER_DIVIDER),
                new EmptyBorder(6, 14, 6, 14)));

        // Sub-panel tombol saat Tab 1 (Formulir Transfer) Aktif
        panelFooterInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelFooterInput.setOpaque(false);

        BtnSimpan = createFlatButton(" Simpan", IconStyle.muat("save.svg", 15, Color.WHITE), COL_TEAL_DARK, Color.WHITE,
                COL_TEAL_DARK);
        BtnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setToolTipText("Alt+S (Simpan / Perbarui Transfer Pasien)");
        aturUkuranTombol(BtnSimpan, 115);
        BtnSimpan.addActionListener(e -> {
            if (isEditMode) {
                ganti();
            } else {
                simpan();
            }
        });

        BtnBatal = createFlatButton(" Reset / Baru", IconStyle.muat("refresh.svg", 15, new Color(71, 85, 105)),
                Color.WHITE, new Color(51, 65, 85), COL_INPUT_BORDER);
        BtnBatal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        BtnBatal.setMnemonic('B');
        BtnBatal.setToolTipText("Alt+B (Bersihkan form atau batalkan edit)");
        aturUkuranTombol(BtnBatal, 130);
        BtnBatal.addActionListener(e -> setModeInputBaru());

        panelFooterInput.add(BtnSimpan);
        panelFooterInput.add(BtnBatal);

        // Status Hint di Tab Input
        panelFooterInput.add(Box.createHorizontalStrut(8));
        lblFooterHint = new JLabel(
                "Form transfer siap diisi. Pastikan data serah terima telah diverifikasi oleh petugas kedua ruangan.");
        lblFooterHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooterHint.setForeground(COL_TEXT_MUTED);
        panelFooterInput.add(lblFooterHint);

        // Sub-panel tombol saat Tab 2 (Data Riwayat Transfer) Aktif
        panelFooterData = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelFooterData.setOpaque(false);

        btnGantiData = createFlatButton(" Ganti / Edit Data", IconStyle.muat("pencil.svg", 15, new Color(71, 85, 105)),
                Color.WHITE, new Color(51, 65, 85), COL_INPUT_BORDER);
        btnGantiData.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGantiData.setEnabled(false);
        aturUkuranTombol(btnGantiData, 165);
        btnGantiData.addActionListener(e -> {
            int r = tbObat.getSelectedRow();
            if (r != -1) {
                getData();
                setModeEditAktif(tbObat.getValueAt(r, 0).toString(), tbObat.getValueAt(r, 5).toString());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Silakan pilih baris data transfer yang ingin diganti atau diedit!");
            }
        });

        btnHapusData = createFlatButton(" Hapus", IconStyle.muat("trash.svg", 15, new Color(225, 29, 72)), Color.WHITE,
                new Color(225, 29, 72), new Color(254, 205, 211));
        btnHapusData.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHapusData.setEnabled(false);
        aturUkuranTombol(btnHapusData, 105);
        btnHapusData.addActionListener(e -> hapus());

        btnValidasiTTE = createFlatButton(" Sahkan TTE", IconStyle.muat("clipboard-check.svg", 15, Color.WHITE),
                COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnValidasiTTE.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnValidasiTTE.setEnabled(false);
        aturUkuranTombol(btnValidasiTTE, 155);
        btnValidasiTTE.addActionListener(e -> bukaMenuSahkanTTE(btnValidasiTTE));

        btnCetakDokumen = createFlatButton(" Cetak RM 04", IconStyle.muat("printer.svg", 15, Color.WHITE),
                new Color(16, 185, 129), Color.WHITE, new Color(16, 185, 129));
        btnCetakDokumen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCetakDokumen.setEnabled(false);
        aturUkuranTombol(btnCetakDokumen, 155);
        btnCetakDokumen.addActionListener(e -> cetakLaporanTransfer("Lembar"));
        pasangMenuCetakOpsional(btnCetakDokumen);

        panelFooterData.add(btnGantiData);
        panelFooterData.add(btnHapusData);
        panelFooterData.add(btnValidasiTTE);
        panelFooterData.add(btnCetakDokumen);

        // Alias penugasan kompatibilitas
        BtnEdit = btnGantiData;
        BtnHapus = btnHapusData;
        BtnPrint = btnCetakDokumen;

        footerCardLayout = new CardLayout();
        leftFooterWrapper = new JPanel(footerCardLayout);
        leftFooterWrapper.setOpaque(false);
        leftFooterWrapper.add(panelFooterInput, "INPUT");
        leftFooterWrapper.add(panelFooterData, "DATA");
        footerCardLayout.show(leftFooterWrapper, "INPUT");

        // Sisi Kanan: Tombol Keluar (Konsisten di kedua Tab)
        BtnKeluar = createFlatButton(" Keluar", IconStyle.muat("log-out.svg", 15, new Color(225, 29, 72)), Color.WHITE,
                new Color(225, 29, 72), new Color(254, 205, 211));
        BtnKeluar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setToolTipText("Alt+K (Tutup jendela)");
        aturUkuranTombol(BtnKeluar, 105);
        BtnKeluar.addActionListener(evt -> dispose());

        footer.add(leftFooterWrapper, BorderLayout.WEST);
        footer.add(BtnKeluar, BorderLayout.EAST);
        return footer;
    }

    private void updateStateButtonsTab2(int r) {
        if (btnGantiData == null)
            return;
        boolean barisTerpilih = (r != -1);
        btnGantiData.setEnabled(barisTerpilih);
        btnHapusData.setEnabled(barisTerpilih);
        if (btnValidasiTTE != null) {
            btnValidasiTTE.setEnabled(barisTerpilih);
        }
        if (btnCetakDokumen != null) {
            btnCetakDokumen.setEnabled(barisTerpilih);
        }
    }

    private void pasangMenuCetakOpsional(JButton btn) {
        JPopupMenu menuCetak = new JPopupMenu();

        JMenuItem itemLembar = new JMenuItem(" Cetak Lembar ke Printer");
        itemLembar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        itemLembar.addActionListener(e -> {
            int r = tbObat.getSelectedRow();
            if (r != -1) {
                cetakLaporanTransfer("Lembar");
            } else if (!TNoRw.getText().trim().isEmpty()) {
                cetakLaporanTransfer("Lembar");
            }
        });

        JMenuItem itemPDF = new JMenuItem(" Ekspor / Simpan ke PDF");
        itemPDF.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        itemPDF.addActionListener(e -> {
            int r = tbObat.getSelectedRow();
            if (r != -1) {
                cetakLaporanTransfer("PDF");
            } else if (!TNoRw.getText().trim().isEmpty()) {
                cetakLaporanTransfer("PDF");
            }
        });

        menuCetak.add(itemLembar);
        menuCetak.add(itemPDF);

        btn.setComponentPopupMenu(menuCetak);
        btn.addActionListener(e -> {
            menuCetak.show(btn, 0, btn.getHeight());
        });
    }

    // =========================================================================
    // DATABASE CRUD OPERATIONS (SINKRON DENGAN SKEMA ASLI 79 KOLOM)
    // =========================================================================

    private String comboValOrNull(widget.ComboBox c) {
        if (c == null || c.getSelectedItem() == null)
            return null;
        String s = c.getSelectedItem().toString().trim();
        return s.isEmpty() ? null : s;
    }

    private String ya(JCheckBox c) {
        return (c != null && c.isSelected()) ? "Ya" : "Tidak";
    }

    private String getObatYangDiberikanText() {
        List<String> list = new ArrayList<>();
        for (widget.TextBox t : new widget.TextBox[] { txtTherapy1, txtTherapy2, txtTherapy3, txtTherapy4,
                txtTherapy5 }) {
            String val = t.getText().trim();
            if (!val.isEmpty())
                list.add(val);
        }
        return String.join("\n", list);
    }

    private String getProsedurDilakukanText() {
        List<String> list = new ArrayList<>();
        if (!txtProsedur1.getText().trim().isEmpty()) {
            list.add(txtProsedur1.getText().trim()
                    + (!txtTglProsedur1.getText().trim().isEmpty() ? " (Tgl: " + txtTglProsedur1.getText().trim() + ")"
                            : ""));
        }
        if (!txtProsedur2.getText().trim().isEmpty()) {
            list.add(txtProsedur2.getText().trim()
                    + (!txtTglProsedur2.getText().trim().isEmpty() ? " (Tgl: " + txtTglProsedur2.getText().trim() + ")"
                            : ""));
        }
        if (!txtProsedur3.getText().trim().isEmpty()) {
            list.add(txtProsedur3.getText().trim()
                    + (!txtTglProsedur3.getText().trim().isEmpty() ? " (Tgl: " + txtTglProsedur3.getText().trim() + ")"
                            : ""));
        }
        return String.join("\n", list);
    }

    private String getRtlText() {
        List<String> list = new ArrayList<>();
        for (widget.TextBox t : new widget.TextBox[] { txtRtl1, txtRtl2, txtRtl3 }) {
            String val = t.getText().trim();
            if (!val.isEmpty())
                list.add(val);
        }
        return String.join("\n", list);
    }

    private String getMasalahKepText() {
        List<String> list = new ArrayList<>();
        for (widget.TextBox t : new widget.TextBox[] { txtMasalahKep1, txtMasalahKep2, txtMasalahKep3 }) {
            String val = t.getText().trim();
            if (!val.isEmpty())
                list.add(val);
        }
        return String.join("\n", list);
    }

    private String getDokumenText() {
        List<String> list = new ArrayList<>();
        for (widget.TextBox t : new widget.TextBox[] { txtDokumen1, txtDokumen2, txtDokumen3, txtDokumen4,
                txtDokumen5 }) {
            String val = t.getText().trim();
            if (!val.isEmpty())
                list.add(val);
        }
        return String.join("\n", list);
    }

    private String getGcsText() {
        String e = txtGcsE.getText().trim();
        String m = txtGcsM.getText().trim();
        String v = txtGcsV.getText().trim();
        if (e.isEmpty() && m.isEmpty() && v.isEmpty())
            return "";
        try {
            int tot = Integer.parseInt(e.isEmpty() ? "0" : e) +
                    Integer.parseInt(m.isEmpty() ? "0" : m) +
                    Integer.parseInt(v.isEmpty() ? "0" : v);
            return tot + " (E" + (e.isEmpty() ? "-" : e) + "M" + (m.isEmpty() ? "-" : m) + "V" + (v.isEmpty() ? "-" : v)
                    + ")";
        } catch (Exception ex) {
            return "E" + e + "M" + m + "V" + v;
        }
    }

    private String getTdText() {
        String s = txtTdSistol.getText().trim();
        String d = txtTdDiastol.getText().trim();
        if (s.isEmpty() && d.isEmpty())
            return "";
        return s + "/" + d;
    }

    private String[] nilaiTransfer() {
        String lineInvasif = segLineInvasif.getSelectedValue();
        String lineInvasifTanggal = ("CVP/PICC".equals(lineInvasif) || "IV Line".equals(lineInvasif))
                ? LineInvasifTanggal.getTanggalDanJamSql(Valid)
                : null;

        String kateterTanggal = "Kateter Urine".equals(segBak.getSelectedValue())
                ? DTPEliminasiBakKateterTanggalPasang.getTanggalDanJamSql(Valid)
                : null;

        // Ambil luka dari tabel luka
        String lukaOpUkuran = modelTabelLuka.getValueAt(0, 1) != null ? modelTabelLuka.getValueAt(0, 1).toString() : "";
        String lukaOpLokasi = modelTabelLuka.getValueAt(0, 2) != null ? modelTabelLuka.getValueAt(0, 2).toString() : "";
        String lukaOpKondisi = modelTabelLuka.getValueAt(0, 3) != null ? modelTabelLuka.getValueAt(0, 3).toString()
                : "";
        String lukaOpCek = (!lukaOpUkuran.isEmpty() || !lukaOpLokasi.isEmpty()) ? "Ya" : "Tidak";

        String lukaDekUkuran = modelTabelLuka.getValueAt(1, 1) != null ? modelTabelLuka.getValueAt(1, 1).toString()
                : "";
        String lukaDekLokasi = modelTabelLuka.getValueAt(1, 2) != null ? modelTabelLuka.getValueAt(1, 2).toString()
                : "";
        String lukaDekKondisi = modelTabelLuka.getValueAt(1, 3) != null ? modelTabelLuka.getValueAt(1, 3).toString()
                : "";
        String lukaDekCek = (!lukaDekUkuran.isEmpty() || !lukaDekLokasi.isEmpty()) ? "Ya" : "Tidak";

        String lukaSkinUkuran = modelTabelLuka.getValueAt(2, 1) != null ? modelTabelLuka.getValueAt(2, 1).toString()
                : "";
        String lukaSkinLokasi = modelTabelLuka.getValueAt(2, 2) != null ? modelTabelLuka.getValueAt(2, 2).toString()
                : "";
        String lukaSkinKondisi = modelTabelLuka.getValueAt(2, 3) != null ? modelTabelLuka.getValueAt(2, 3).toString()
                : "";
        String lukaSkinCek = (!lukaSkinUkuran.isEmpty() || !lukaSkinLokasi.isEmpty()) ? "Ya" : "Tidak";

        String lukaGanUkuran = modelTabelLuka.getValueAt(3, 1) != null ? modelTabelLuka.getValueAt(3, 1).toString()
                : "";
        String lukaGanLokasi = modelTabelLuka.getValueAt(3, 2) != null ? modelTabelLuka.getValueAt(3, 2).toString()
                : "";
        String lukaGanKondisi = modelTabelLuka.getValueAt(3, 3) != null ? modelTabelLuka.getValueAt(3, 3).toString()
                : "";
        String lukaGanCek = (!lukaGanUkuran.isEmpty() || !lukaGanLokasi.isEmpty()) ? "Ya" : "Tidak";

        String caraPindah = segCaraPindah.getSelectedValue();
        if ("Lainnya".equalsIgnoreCase(caraPindah)) {
            caraPindah = "Lain-lain";
        }

        return new String[] {
                TNoRw.getText(),
                TanggalMasuk.getTanggalDanJamSql(Valid),
                TanggalPindah.getTanggalDanJamSql(Valid),
                AsalRuang.getText(),
                RuangSelanjutnya.getText(),
                DiagnosaUtama.getText(),
                DiagnosaSekunder.getText(),
                comboValOrNull(IndikasiPindah),
                KeteranganIndikasiPindahRuang.getText(),
                getProsedurDilakukanText(),
                getObatYangDiberikanText(),
                caraPindah,
                "Ya",
                NamaMenyetujui.getText(),
                comboValOrNull(HubunganMenyetujui),
                KdPetugasMenyerahkan.getText(),
                KdPetugasMenerima.getText(),

                TNoTlpMenyetujui.getText(),
                segPenjelasanDiagnosa.getSelectedValue(),
                segKelasAsal.getSelectedValue(),
                segKelasTujuan.getSelectedValue(),
                KdDokterMerawatAsal1.getText(),
                KdDokterMerawatAsal2.getText(),
                KdDokterMerawatAsal3.getText(),
                KdDokterMerawatTujuan1.getText(),
                KdDokterMerawatTujuan2.getText(),
                KdDokterMerawatTujuan3.getText(),

                segKesadaran.getSelectedValue(),
                getTdText(),
                txtHr.getText(),
                txtRr.getText(),
                txtTemp.getText(),
                getGcsText(),
                txtSpo2.getText(),
                comboValOrNull(TingkatNyeriTransfer),
                ObservasiTerakhirPukul.getTanggalDanJamSql(Valid),

                segTherapyOksigen.getSelectedValue(),
                ya(chkO2NasalKanule),
                ya(chkO2NasalKateter),
                ya(chkO2FaceMask),
                ya(chkO2SungkupRebreathing),
                ya(chkO2SungkupNonRebreathing),
                ya(chkO2Tracheostomy),
                ya(chkO2Ett),

                lineInvasif,
                lineInvasifTanggal,
                ya(chkPrecautionStandar),
                ya(chkPrecautionKontak),
                ya(chkPrecautionAirborne),
                ya(chkPrecautionDroplet),

                "Puasa".equals(segNutrisi.getSelectedValue()) ? "Ya" : "Tidak",
                "Oral".equals(segNutrisi.getSelectedValue()) ? "Ya" : "Tidak",
                "NGT".equals(segNutrisi.getSelectedValue()) ? "Ya" : "Tidak",
                (chkBatasanCairan.isSelected() ? TNutrisiBatasanCairanCc.getText()
                        : (TNutrisiBatasanCairanCc.getText().trim().isEmpty() ? ""
                                : TNutrisiBatasanCairanCc.getText())),
                ya(chkDietKhusus),
                TNutrisiDietKhususJelaskan.getText(),

                segBab.getSelectedValue(),
                segBak.getSelectedValue(),
                TEliminasiBakKateterNo.getText(),
                kateterTanggal,

                segAktifitas.getSelectedValue(),
                segMobilisasi.getSelectedValue(),
                ya(chkDrainWsd),
                ya(chkDrainLuka),
                ya(chkDrainStoma),
                ya(chkDrainLain),
                TDrainaseLainKet.getText(),

                ya(chkLukaTidakAda),
                lukaOpCek, lukaOpUkuran, lukaOpLokasi, lukaOpKondisi,
                lukaDekCek, lukaDekUkuran, lukaDekLokasi, lukaDekKondisi,
                lukaSkinCek, lukaSkinUkuran, lukaSkinLokasi, lukaSkinKondisi,
                lukaGanCek, lukaGanUkuran, lukaGanLokasi, lukaGanKondisi,
                !TLukaLainKet.getText().trim().isEmpty() ? "Ya" : "Tidak",
                TLukaLainKet.getText(),

                txtHasilInvestigasiAbnormal.getText(),

                ya(chkKhususJatuh),
                ya(chkKhususRestrain),
                ya(chkKhususRawatLuka),
                ya(chkKhususHygiene),
                segRiwayatAlergi.getSelectedValue(),
                TJenisAlergi.getText(),
                TReaksiAlergi.getText(),

                ya(chkAlatTanpa),
                ya(chkAlatDengar),
                ya(chkAlatKacamata),
                ya(chkAlatGigiPalsu),
                ya(chkAlatTongkat),
                !TAlatBantuLainKet.getText().trim().isEmpty() ? "Ya" : "Tidak",
                TAlatBantuLainKet.getText(),

                txtTindakanSudahDilakukan.getText(),

                ya(chkPenunjangLab),
                TPenunjangLaboratoriumKet.getText(),
                ya(chkPenunjangRad),
                TPenunjangRadiologiKet.getText(),
                ya(chkPenunjangCtScan),
                TPenunjangCtScanKet.getText(),
                ya(chkPenunjangLain),
                TPenunjangLainKet.getText(),

                getRtlText(),
                getMasalahKepText(),
                getDokumenText()
        };
    }

    private void simpan() {
        if (TNoRw.getText().trim().isEmpty()) {
            Valid.textKosong(TNoRw, "Nomor Rawat Pasien");
            return;
        }
        if (AsalRuang.getText().trim().isEmpty()) {
            Valid.textKosong(AsalRuang, "Ruangan Asal");
            return;
        }
        if (RuangSelanjutnya.getText().trim().isEmpty()) {
            Valid.textKosong(RuangSelanjutnya, "Ruangan Tujuan");
            return;
        }

        StringBuilder kolom = new StringBuilder();
        StringBuilder placeholder = new StringBuilder();
        for (String k : KOLOM_TRANSFER) {
            if (kolom.length() > 0) {
                kolom.append(",");
                placeholder.append(",");
            }
            kolom.append(k);
            placeholder.append("?");
        }
        String[] param = nilaiTransfer();

        boolean sukses = false;
        try {
            PreparedStatement psSimpan = koneksi.prepareStatement(
                    "insert into transfer_pasien_antar_ruang (" + kolom + ") values (" + placeholder + ")");
            try {
                for (int idx = 0; idx < param.length; idx++) {
                    psSimpan.setString(idx + 1, param[idx]);
                }
                psSimpan.executeUpdate();
                sukses = true;
            } finally {
                if (psSimpan != null)
                    psSimpan.close();
            }
        } catch (Exception e) {
            System.out.println("Notif Simpan RM 04 : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal menyimpan data transfer pasien. Kemungkinan No. Rawat & Tanggal Masuk yang sama sudah ada!");
        }

        if (sukses) {
            simpanAuditKonfirmasiTte();
            lblStatusSimpanInfo.setText("Tersimpan (" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + " WIB)");
            JOptionPane.showMessageDialog(null, "Data Transfer Pasien Antar Ruangan (RM 04) berhasil disimpan!");
            setModeInputBaru();
            runBackground(this::tampil);
        }
    }

    private void ganti() {
        if (tbObat.getSelectedRow() == -1 && !isEditMode) {
            JOptionPane.showMessageDialog(null, "Silakan pilih baris data transfer yang ingin diedit terlebih dahulu!");
            return;
        }

        String targetRawat = isEditMode ? editNoRawat : tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        String targetTglMasuk = isEditMode ? editTanggalMasuk
                : tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString();

        StringBuilder setClause = new StringBuilder();
        for (String kolom : KOLOM_TRANSFER) {
            setClause.append(kolom).append("=?,");
        }
        setClause.setLength(setClause.length() - 1);

        String[] nilai = nilaiTransfer();
        String[] param = new String[nilai.length + 2];
        System.arraycopy(nilai, 0, param, 0, nilai.length);
        param[nilai.length] = targetRawat;
        param[nilai.length + 1] = targetTglMasuk;

        if (Sequel.mengedittf("transfer_pasien_antar_ruang", "no_rawat=? and tanggal_masuk=?", setClause.toString(),
                param.length, param)) {
            simpanAuditKonfirmasiTte();
            JOptionPane.showMessageDialog(null, "Perubahan data transfer pasien berhasil diperbarui!");
            setModeInputBaru();
            runBackground(this::tampil);
            switchWorkspaceView("HISTORY");
        } else {
            JOptionPane.showMessageDialog(null, "Gagal memperbarui data transfer pasien!");
        }
    }

    private void hapus() {
        if (tbObat.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, "Silakan pilih data transfer yang ingin dihapus pada tabel riwayat!");
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(
                null,
                "Apakah Anda yakin ingin menghapus data transfer pasien ini?",
                "Konfirmasi Hapus Data RM 04",
                JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            String targetRawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
            String targetTglMasuk = tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString();

            if (Sequel.queryu2tf("delete from transfer_pasien_antar_ruang where no_rawat=? and tanggal_masuk=?", 2,
                    new String[] { targetRawat, targetTglMasuk })) {
                Sequel.queryu2tf(
                        "delete from transfer_pasien_antar_ruang_konfirmasi where no_rawat=? and tanggal_masuk=?", 2,
                        new String[] { targetRawat, targetTglMasuk });
                tabMode.removeRow(tbObat.getSelectedRow());
                LCount.setText("" + tabMode.getRowCount());
                lblHistoryCountBadge.setText("" + tabMode.getRowCount());
                JOptionPane.showMessageDialog(null, "Data transfer pasien berhasil dihapus!");
                setModeInputBaru();
            } else {
                JOptionPane.showMessageDialog(null, "Gagal menghapus data transfer pasien!");
            }
        }
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            String sql = "select reg_periksa.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, " +
                    "pasien.tgl_lahir, if(pasien.jk='L','Laki-Laki','Perempuan') as jk, " +
                    "transfer_pasien_antar_ruang.tanggal_masuk, transfer_pasien_antar_ruang.tanggal_pindah, " +
                    "transfer_pasien_antar_ruang.diagnosa_utama, transfer_pasien_antar_ruang.nip_menyerahkan, " +
                    "petugasmenyerahkan.nama as petugasmenyerahkan, transfer_pasien_antar_ruang.nip_menerima, " +
                    "petugasmenerima.nama as petugasmenerima " +
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                    "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "
                    +
                    "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "
                    +
                    "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip ";

            if (TCari.getText().trim().isEmpty()) {
                sql += "where transfer_pasien_antar_ruang.tanggal_masuk between ? and ? order by transfer_pasien_antar_ruang.tanggal_masuk desc";
                ps = koneksi.prepareStatement(sql);
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
            } else {
                sql += "where transfer_pasien_antar_ruang.tanggal_masuk between ? and ? and (" +
                        "reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or " +
                        "transfer_pasien_antar_ruang.asal_ruang like ? or transfer_pasien_antar_ruang.ruang_selanjutnya like ?) "
                        +
                        "order by transfer_pasien_antar_ruang.tanggal_masuk desc";
                ps = koneksi.prepareStatement(sql);
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                String q = "%" + TCari.getText().trim() + "%";
                ps.setString(3, q);
                ps.setString(4, q);
                ps.setString(5, q);
                ps.setString(6, q);
                ps.setString(7, q);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[] {
                        rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"),
                        rs.getDate("tgl_lahir"),
                        rs.getString("jk"),
                        rs.getString("tanggal_masuk"),
                        rs.getString("tanggal_pindah"),
                        rs.getString("diagnosa_utama"),
                        rs.getString("nip_menyerahkan"),
                        rs.getString("petugasmenyerahkan"),
                        rs.getString("nip_menerima"),
                        rs.getString("petugasmenerima")
                });
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tampil RM 04 : " + e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
            } catch (Exception ignored) {
            }
        }

        int rowCount = tabMode.getRowCount();
        LCount.setText("" + rowCount);
        lblHistoryCountBadge.setText("" + rowCount);

        if (rowCount > 0 && tbObat.getSelectedRow() == -1) {
            tbObat.setRowSelectionInterval(0, 0);
            getData();
        }
        updateStateButtonsTab2(tbObat.getSelectedRow());
    }

    private void getData() {
        if (tbObat.getSelectedRow() == -1)
            return;
        String noRawatPilih = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        String tanggalMasukPilih = tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString();

        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, if(pasien.jk='L','Laki-Laki','Perempuan') as jk, pasien.tgl_lahir, "
                            +
                            "transfer_pasien_antar_ruang.*, " +
                            "petugasmenyerahkan.nama as nm_menyerahkan, petugasmenerima.nama as nm_menerima, " +
                            "dokterasal1.nm_dokter as nm_dokter_asal_1, dokterasal2.nm_dokter as nm_dokter_asal_2, dokterasal3.nm_dokter as nm_dokter_asal_3, "
                            +
                            "doktertujuan1.nm_dokter as nm_dokter_tujuan_1, doktertujuan2.nm_dokter as nm_dokter_tujuan_2, doktertujuan3.nm_dokter as nm_dokter_tujuan_3 "
                            +
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                            "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "
                            +
                            "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "
                            +
                            "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip "
                            +
                            "left join dokter as dokterasal1 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_1=dokterasal1.kd_dokter "
                            +
                            "left join dokter as dokterasal2 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_2=dokterasal2.kd_dokter "
                            +
                            "left join dokter as dokterasal3 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_3=dokterasal3.kd_dokter "
                            +
                            "left join dokter as doktertujuan1 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_1=doktertujuan1.kd_dokter "
                            +
                            "left join dokter as doktertujuan2 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_2=doktertujuan2.kd_dokter "
                            +
                            "left join dokter as doktertujuan3 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_3=doktertujuan3.kd_dokter "
                            +
                            "where transfer_pasien_antar_ruang.no_rawat=? and transfer_pasien_antar_ruang.tanggal_masuk=?");
            try {
                ps.setString(1, noRawatPilih);
                ps.setString(2, tanggalMasukPilih);
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRw.setText(rs.getString("no_rawat"));
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    Jk.setText(rs.getString("jk"));
                    TanggalMasuk.setNilaiSql(rs.getString("tanggal_masuk"));
                    TanggalPindah.setNilaiSql(rs.getString("tanggal_pindah"));

                    DiagnosaUtama.setText(nz(rs.getString("diagnosa_utama")));
                    DiagnosaSekunder.setText(nz(rs.getString("diagnosa_sekunder")));

                    NamaMenyetujui.setText(nz(rs.getString("nama_menyetujui")));
                    HubunganMenyetujui.setSelectedItem(nz(rs.getString("hubungan_menyetujui")));
                    TNoTlpMenyetujui.setText(nz(rs.getString("no_tlp_menyetujui")));
                    segPenjelasanDiagnosa.setSelectedValue(nz(rs.getString("sudah_dijelaskan_diagnosa"), "Ya"));

                    AsalRuang.setText(nz(rs.getString("asal_ruang")));
                    KelasAsalRuang.setText(nz(rs.getString("kelas_asal_ruang")));
                    segKelasAsal.setSelectedValue(nz(rs.getString("kelas_asal_ruang"), "Kelas 1"));

                    RuangSelanjutnya.setText(nz(rs.getString("ruang_selanjutnya")));
                    KelasRuangSelanjutnya.setText(nz(rs.getString("kelas_ruang_selanjutnya")));
                    segKelasTujuan.setSelectedValue(nz(rs.getString("kelas_ruang_selanjutnya"), "Kelas 1"));

                    KdDokterMerawatAsal1.setText(nz(rs.getString("kd_dokter_merawat_asal_1")));
                    NmDokterMerawatAsal1.setText(nz(rs.getString("nm_dokter_asal_1")));
                    KdDokterMerawatAsal2.setText(nz(rs.getString("kd_dokter_merawat_asal_2")));
                    NmDokterMerawatAsal2.setText(nz(rs.getString("nm_dokter_asal_2")));
                    KdDokterMerawatAsal3.setText(nz(rs.getString("kd_dokter_merawat_asal_3")));
                    NmDokterMerawatAsal3.setText(nz(rs.getString("nm_dokter_asal_3")));

                    KdDokterMerawatTujuan1.setText(nz(rs.getString("kd_dokter_merawat_tujuan_1")));
                    NmDokterMerawatTujuan1.setText(nz(rs.getString("nm_dokter_tujuan_1")));
                    KdDokterMerawatTujuan2.setText(nz(rs.getString("kd_dokter_merawat_tujuan_2")));
                    NmDokterMerawatTujuan2.setText(nz(rs.getString("nm_dokter_tujuan_2")));
                    KdDokterMerawatTujuan3.setText(nz(rs.getString("kd_dokter_merawat_tujuan_3")));
                    NmDokterMerawatTujuan3.setText(nz(rs.getString("nm_dokter_tujuan_3")));

                    IndikasiPindah.setSelectedItem(nz(rs.getString("indikasi_pindah_ruang")));
                    KeteranganIndikasiPindahRuang.setText(nz(rs.getString("keterangan_indikasi_pindah_ruang")));
                    segCaraPindah.setSelectedValue(nz(rs.getString("metode_pemindahan_pasien"), "Strecher"));

                    if (rs.getString("observasi_terakhir_pukul") != null) {
                        ObservasiTerakhirPukul.setNilaiSql(rs.getString("observasi_terakhir_pukul"));
                    }
                    segKesadaran.setSelectedValue(nz(rs.getString("kesadaran_transfer"), "Compos Mentis"));

                    // GCS
                    String gcsDb = nz(rs.getString("gcs_transfer"));
                    parseGcsToFields(gcsDb);

                    // TTV
                    String tdDb = nz(rs.getString("td_transfer"));
                    if (tdDb.contains("/")) {
                        String[] splitTd = tdDb.split("/");
                        txtTdSistol.setText(splitTd[0].trim());
                        txtTdDiastol.setText(splitTd.length > 1 ? splitTd[1].trim() : "");
                    } else {
                        txtTdSistol.setText(tdDb);
                        txtTdDiastol.setText("");
                    }
                    txtHr.setText(nz(rs.getString("hr_transfer")));
                    txtRr.setText(nz(rs.getString("rr_transfer")));
                    txtTemp.setText(nz(rs.getString("suhu_transfer")));
                    txtSpo2.setText(nz(rs.getString("spo2_transfer")));
                    TingkatNyeriTransfer.setSelectedItem(nz(rs.getString("tingkat_nyeri_transfer"), "0"));
                    updateNyeriDisplay(nz(rs.getString("tingkat_nyeri_transfer"), "0"));

                    segTherapyOksigen.setSelectedValue(nz(rs.getString("oksigen_status"), "Tidak"));
                    chkO2NasalKanule.setSelected("Ya".equals(rs.getString("oksigen_nasal_kanule")));
                    chkO2NasalKateter.setSelected("Ya".equals(rs.getString("oksigen_nasal_kateter")));
                    chkO2FaceMask.setSelected("Ya".equals(rs.getString("oksigen_face_mask")));
                    chkO2SungkupRebreathing.setSelected("Ya".equals(rs.getString("oksigen_sungkup_rebreathing")));
                    chkO2SungkupNonRebreathing
                            .setSelected("Ya".equals(rs.getString("oksigen_sungkup_non_rebreathing")));
                    chkO2Tracheostomy.setSelected("Ya".equals(rs.getString("oksigen_tracheostomy")));
                    chkO2Ett.setSelected("Ya".equals(rs.getString("oksigen_ett")));

                    segLineInvasif.setSelectedValue(nz(rs.getString("line_invasif"), "Tidak Ada Line Invasive"));
                    if (rs.getString("line_invasif_tanggal") != null) {
                        LineInvasifTanggal.setNilaiSql(rs.getString("line_invasif_tanggal"));
                    }

                    // Prosedur
                    parseProsedurText(nz(rs.getString("prosedur_yang_sudah_dilakukan")));

                    chkPrecautionStandar.setSelected("Ya".equals(rs.getString("kewaspadaan_standar")));
                    chkPrecautionKontak.setSelected("Ya".equals(rs.getString("kewaspadaan_kontak")));
                    chkPrecautionAirborne.setSelected("Ya".equals(rs.getString("kewaspadaan_airborne")));
                    chkPrecautionDroplet.setSelected("Ya".equals(rs.getString("kewaspadaan_droplet")));

                    if ("Ya".equals(rs.getString("nutrisi_puasa")))
                        segNutrisi.setSelectedValue("Puasa");
                    else if ("Ya".equals(rs.getString("nutrisi_ngt")))
                        segNutrisi.setSelectedValue("NGT");
                    else
                        segNutrisi.setSelectedValue("Oral");

                    String bCairan = nz(rs.getString("nutrisi_batasan_cairan_cc"));
                    TNutrisiBatasanCairanCc.setText(bCairan);
                    chkBatasanCairan.setSelected(!bCairan.isEmpty());

                    chkDietKhusus.setSelected("Ya".equals(rs.getString("nutrisi_diet_khusus")));
                    TNutrisiDietKhususJelaskan.setText(nz(rs.getString("nutrisi_diet_khusus_jelaskan")));

                    segBab.setSelectedValue(nz(rs.getString("eliminasi_bab"), "Normal"));
                    segBak.setSelectedValue(nz(rs.getString("eliminasi_bak"), "Normal"));
                    TEliminasiBakKateterNo.setText(nz(rs.getString("eliminasi_bak_kateter_no")));
                    if (rs.getString("eliminasi_bak_kateter_tanggal_pasang") != null) {
                        DTPEliminasiBakKateterTanggalPasang
                                .setNilaiSql(rs.getString("eliminasi_bak_kateter_tanggal_pasang"));
                    }

                    segAktifitas.setSelectedValue(nz(rs.getString("aktifitas"), "Dibantu Sebagian"));
                    segMobilisasi.setSelectedValue(nz(rs.getString("mobilisasi"), "Tirah Baring"));

                    chkDrainWsd.setSelected("Ya".equals(rs.getString("drainase_wsd")));
                    chkDrainLuka.setSelected("Ya".equals(rs.getString("drainase_drain_luka")));
                    chkDrainStoma.setSelected("Ya".equals(rs.getString("drainase_stoma")));
                    chkDrainLain.setSelected("Ya".equals(rs.getString("drainase_lain")));
                    TDrainaseLainKet.setText(nz(rs.getString("drainase_lain_ket")));

                    chkLukaTidakAda.setSelected("Ya".equals(rs.getString("luka_tidak_ada")));
                    modelTabelLuka.setValueAt(nz(rs.getString("luka_operasi_ukuran")), 0, 1);
                    modelTabelLuka.setValueAt(nz(rs.getString("luka_operasi_lokasi")), 0, 2);
                    modelTabelLuka.setValueAt(nz(rs.getString("luka_operasi_kondisi")), 0, 3);

                    modelTabelLuka.setValueAt(nz(rs.getString("luka_dekubitus_ukuran")), 1, 1);
                    modelTabelLuka.setValueAt(nz(rs.getString("luka_dekubitus_lokasi")), 1, 2);
                    modelTabelLuka.setValueAt(nz(rs.getString("luka_dekubitus_kondisi")), 1, 3);

                    modelTabelLuka.setValueAt(nz(rs.getString("luka_skin_lost_ukuran")), 2, 1);
                    modelTabelLuka.setValueAt(nz(rs.getString("luka_skin_lost_lokasi")), 2, 2);
                    modelTabelLuka.setValueAt(nz(rs.getString("luka_skin_lost_kondisi")), 2, 3);

                    modelTabelLuka.setValueAt(nz(rs.getString("luka_gangren_ukuran")), 3, 1);
                    modelTabelLuka.setValueAt(nz(rs.getString("luka_gangren_lokasi")), 3, 2);
                    modelTabelLuka.setValueAt(nz(rs.getString("luka_gangren_kondisi")), 3, 3);
                    TLukaLainKet.setText(nz(rs.getString("luka_lain_ket")));

                    txtHasilInvestigasiAbnormal.setText(nz(rs.getString("hasil_investigasi_abnormal")));
                    chkKhususJatuh.setSelected("Ya".equals(rs.getString("tindakan_resiko_jatuh")));
                    chkKhususRestrain.setSelected("Ya".equals(rs.getString("tindakan_pemasangan_restrain")));
                    chkKhususRawatLuka.setSelected("Ya".equals(rs.getString("tindakan_perawatan_luka")));
                    chkKhususHygiene.setSelected("Ya".equals(rs.getString("tindakan_hygiene")));

                    segRiwayatAlergi.setSelectedValue(nz(rs.getString("riwayat_alergi"), "Tidak"));
                    TJenisAlergi.setText(nz(rs.getString("jenis_alergi")));
                    TReaksiAlergi.setText(nz(rs.getString("reaksi_alergi")));

                    chkAlatTanpa.setSelected("Ya".equals(rs.getString("alat_bantu_tanpa")));
                    chkAlatDengar.setSelected("Ya".equals(rs.getString("alat_bantu_dengar")));
                    chkAlatKacamata.setSelected("Ya".equals(rs.getString("alat_bantu_kaca_mata")));
                    chkAlatGigiPalsu.setSelected("Ya".equals(rs.getString("alat_bantu_gigi_palsu")));
                    chkAlatTongkat.setSelected("Ya".equals(rs.getString("alat_bantu_tongkat_kruk")));
                    TAlatBantuLainKet.setText(nz(rs.getString("alat_bantu_lain_ket")));

                    txtTindakanSudahDilakukan.setText(nz(rs.getString("tindakan_yang_sudah_dilakukan")));

                    chkPenunjangLab.setSelected("Ya".equals(rs.getString("penunjang_laboratorium_cek")));
                    TPenunjangLaboratoriumKet.setText(nz(rs.getString("penunjang_laboratorium_ket")));
                    chkPenunjangRad.setSelected("Ya".equals(rs.getString("penunjang_radiologi_cek")));
                    TPenunjangRadiologiKet.setText(nz(rs.getString("penunjang_radiologi_ket")));
                    chkPenunjangCtScan.setSelected("Ya".equals(rs.getString("penunjang_ct_scan_cek")));
                    TPenunjangCtScanKet.setText(nz(rs.getString("penunjang_ct_scan_ket")));
                    chkPenunjangLain.setSelected("Ya".equals(rs.getString("penunjang_lain_cek")));
                    TPenunjangLainKet.setText(nz(rs.getString("penunjang_lain_ket")));

                    // Terapi (1-5), RTL (1-3), Masalah (1-3), Dokumen (1-5)
                    parseLinesToInputs(nz(rs.getString("obat_yang_telah_diberikan")),
                            new widget.TextBox[] { txtTherapy1, txtTherapy2, txtTherapy3, txtTherapy4, txtTherapy5 });
                    parseLinesToInputs(nz(rs.getString("rencana_tindak_lanjut")),
                            new widget.TextBox[] { txtRtl1, txtRtl2, txtRtl3 });
                    parseLinesToInputs(nz(rs.getString("masalah_utama_keperawatan")),
                            new widget.TextBox[] { txtMasalahKep1, txtMasalahKep2, txtMasalahKep3 });
                    parseLinesToInputs(nz(rs.getString("dokumen_yang_diserahterimakan")),
                            new widget.TextBox[] { txtDokumen1, txtDokumen2, txtDokumen3, txtDokumen4, txtDokumen5 });

                    KdPetugasMenyerahkan.setText(nz(rs.getString("nip_menyerahkan")));
                    NmPetugasMenyerahkan.setText(nz(rs.getString("nm_menyerahkan")));
                    KdPetugasMenerima.setText(nz(rs.getString("nip_menerima")));
                    NmPetugasMenerima.setText(nz(rs.getString("nm_menerima")));

                    updatePatientCard();
                    updateGcsCalculation();
                    updateProgress();
                    muatDataKonfirmasiTte(noRawatPilih, tanggalMasukPilih);

                    // Render Live HTML di Pane Kanan
                    LoadHTML.setText(bangunDokumenHtmlLengkap(noRawatPilih, tanggalMasukPilih));
                    LoadHTML.setCaretPosition(0);
                }
            } finally {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif getData RM 04 : " + e);
        }
    }

    private void parseGcsToFields(String gcsStr) {
        if (gcsStr == null || gcsStr.isEmpty())
            return;
        try {
            if (gcsStr.contains("E") && gcsStr.contains("M") && gcsStr.contains("V")) {
                int idxE = gcsStr.indexOf("E");
                int idxM = gcsStr.indexOf("M");
                int idxV = gcsStr.indexOf("V");
                txtGcsE.setText(gcsStr.substring(idxE + 1, idxM).replaceAll("[^0-9]", ""));
                txtGcsM.setText(gcsStr.substring(idxM + 1, idxV).replaceAll("[^0-9]", ""));
                String vPart = gcsStr.substring(idxV + 1).replaceAll("[^0-9]", "");
                txtGcsV.setText(vPart);
            }
        } catch (Exception ex) {
            txtGcsE.setText("4");
            txtGcsM.setText("6");
            txtGcsV.setText("5");
        }
    }

    private void parseProsedurText(String text) {
        txtProsedur1.setText("");
        txtTglProsedur1.setText("");
        txtProsedur2.setText("");
        txtTglProsedur2.setText("");
        txtProsedur3.setText("");
        txtTglProsedur3.setText("");
        if (text == null || text.trim().isEmpty())
            return;

        String[] lines = text.split("\n");
        if (lines.length > 0)
            txtProsedur1.setText(lines[0]);
        if (lines.length > 1)
            txtProsedur2.setText(lines[1]);
        if (lines.length > 2)
            txtProsedur3.setText(lines[2]);
    }

    private void parseLinesToInputs(String multiLineText, widget.TextBox[] fields) {
        for (widget.TextBox f : fields)
            f.setText("");
        if (multiLineText == null || multiLineText.trim().isEmpty())
            return;

        String[] lines = multiLineText.split("\n");
        for (int i = 0; i < Math.min(lines.length, fields.length); i++) {
            fields[i].setText(lines[i].trim());
        }
    }

    private void muatDataKonfirmasiTte(String noRawat, String tglMasuk) {
        try {
            PreparedStatement psKonf = koneksi.prepareStatement(
                    "select * from transfer_pasien_antar_ruang_konfirmasi where no_rawat=? and tanggal_masuk=?");
            try {
                psKonf.setString(1, noRawat);
                psKonf.setString(2, tglMasuk);
                ResultSet rsKonf = psKonf.executeQuery();
                try {
                    if (rsKonf.next()) {
                        String tglMenyerahkan = rsKonf.getString("tgl_validasi_menyerahkan");
                        String statusMenyerahkan = rsKonf.getString("status_menyerahkan");
                        if ("Tervalidasi".equalsIgnoreCase(statusMenyerahkan)) {
                            lblStatusVerifPengirim.setText("\u2713 Tervalidasi (TTE)");
                            lblStatusVerifPengirim.setBackground(COL_EMERALD_SOFT);
                            lblStatusVerifPengirim.setForeground(COL_EMERALD_DARK);
                            lblWaktuTtdPengirim.setText("Waktu: " + tglMenyerahkan);
                            barcodePengirim.regenerateBarcode("TTE-NRS01-" + noRawat.replaceAll("[^0-9]", ""));
                        }

                        String tglMenerima = rsKonf.getString("tgl_validasi_menerima");
                        String statusMenerima = rsKonf.getString("status_menerima");
                        if ("Tervalidasi".equalsIgnoreCase(statusMenerima)) {
                            lblStatusVerifPenerima.setText("\u2713 Tervalidasi (TTE)");
                            lblStatusVerifPenerima.setBackground(COL_EMERALD_SOFT);
                            lblStatusVerifPenerima.setForeground(COL_EMERALD_DARK);
                            lblWaktuTtdPenerima.setText("Waktu: " + tglMenerima);
                            barcodePenerima.regenerateBarcode("TTE-NRS02-" + noRawat.replaceAll("[^0-9]", ""));
                        }

                        String tglDokter = rsKonf.getString("tgl_validasi_dokter");
                        String statusDokter = rsKonf.getString("status_dokter");
                        if ("Tervalidasi".equalsIgnoreCase(statusDokter)) {
                            lblStatusVerifDokter.setText("\u2713 Tervalidasi (TTE)");
                            lblStatusVerifDokter.setBackground(COL_EMERALD_SOFT);
                            lblStatusVerifDokter.setForeground(COL_EMERALD_DARK);
                            lblWaktuTtdDokter.setText("Waktu: " + tglDokter);
                            barcodeDokter.regenerateBarcode("TTE-DPJP-" + noRawat.replaceAll("[^0-9]", ""));
                        }
                    } else {
                        resetStatusTteDisplay();
                    }
                } finally {
                    rsKonf.close();
                }
            } finally {
                psKonf.close();
            }
        } catch (Exception e) {
            resetStatusTteDisplay();
        }
    }

    private void simpanAuditKonfirmasiTte() {
        try {
            PreparedStatement psKonf = koneksi.prepareStatement(
                    "insert into transfer_pasien_antar_ruang_konfirmasi " +
                            "(no_rawat, tanggal_masuk, nip_menyerahkan, nip_menerima, kd_dokter) " +
                            "values (?, ?, ?, ?, ?) " +
                            "on duplicate key update nip_menyerahkan=values(nip_menyerahkan), " +
                            "nip_menerima=values(nip_menerima), kd_dokter=values(kd_dokter)");
            try {
                psKonf.setString(1, TNoRw.getText());
                psKonf.setString(2, TanggalMasuk.getTanggalDanJamSql(Valid));
                psKonf.setString(3, KdPetugasMenyerahkan.getText());
                psKonf.setString(4, KdPetugasMenerima.getText());
                psKonf.setString(5, KdDokterMerawatTujuan1.getText());
                psKonf.executeUpdate();
            } finally {
                psKonf.close();
            }
        } catch (Exception ignored) {
        }
    }

    private void bukaMenuSahkanTTE(Component invoker) {
        int r = tbObat.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih baris data transfer yang ingin disahkan TTE-nya!");
            return;
        }

        JPopupMenu menuTte = new JPopupMenu();

        JMenuItem itemPetugasKirim = new JMenuItem("1a. Sahkan TTE Petugas Menyerahkan (Ruang Asal)");
        itemPetugasKirim.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemPetugasKirim.setIcon(IconStyle.muat("clipboard-check.svg", 14, COL_TEAL_DARK));
        itemPetugasKirim.addActionListener(e -> validasiTtePetugas("menyerahkan"));

        JMenuItem itemPetugasTerima = new JMenuItem("1b. Sahkan TTE Petugas Menerima (Ruang Tujuan)");
        itemPetugasTerima.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemPetugasTerima.setIcon(IconStyle.muat("clipboard-check.svg", 14, COL_TEAL_DARK));
        itemPetugasTerima.addActionListener(e -> validasiTtePetugas("menerima"));

        JMenuItem itemDokter = new JMenuItem("2. Sahkan TTE Dokter Merawat (DPJP)");
        itemDokter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemDokter.setIcon(IconStyle.muat("clipboard-check.svg", 14, COL_TEAL_DARK));
        itemDokter.addActionListener(e -> validasiTteDokter());

        menuTte.add(itemPetugasKirim);
        menuTte.add(itemPetugasTerima);
        menuTte.addSeparator();
        menuTte.add(itemDokter);

        menuTte.show(invoker, 0, -menuTte.getPreferredSize().height - 4);
    }

    private void validasiTtePetugas(String peran) {
        int r = tbObat.getSelectedRow();
        String noRawat = (r != -1) ? tbObat.getValueAt(r, 0).toString().trim() : TNoRw.getText().trim();
        String tglMasuk = (r != -1) ? tbObat.getValueAt(r, 5).toString().trim()
                : TanggalMasuk.getTanggalDanJamSql(Valid);
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih data transfer terlebih dahulu!");
            return;
        }

        String nip = "";
        String nama = "";
        if (r != -1) {
            nip = "menyerahkan".equals(peran) ? tbObat.getValueAt(r, 8).toString().trim()
                    : tbObat.getValueAt(r, 10).toString().trim();
            nama = "menyerahkan".equals(peran) ? tbObat.getValueAt(r, 9).toString().trim()
                    : tbObat.getValueAt(r, 11).toString().trim();
        }
        if (nip.isEmpty()) {
            nip = "menyerahkan".equals(peran) ? KdPetugasMenyerahkan.getText().trim()
                    : KdPetugasMenerima.getText().trim();
            nama = "menyerahkan".equals(peran) ? NmPetugasMenyerahkan.getText().trim()
                    : NmPetugasMenerima.getText().trim();
        }
        if (nip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Petugas yang " + peran + " belum ditentukan!");
            return;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            PreparedStatement psTte = koneksi.prepareStatement(
                    "insert into transfer_pasien_antar_ruang_konfirmasi " +
                            "(no_rawat, tanggal_masuk, "
                            + ("menyerahkan".equals(peran)
                                    ? "tgl_validasi_menyerahkan, nip_menyerahkan, status_menyerahkan"
                                    : "tgl_validasi_menerima, nip_menerima, status_menerima")
                            + ") " +
                            "values (?, ?, ?, ?, 'Tervalidasi') " +
                            "on duplicate key update " +
                            ("menyerahkan".equals(peran)
                                    ? "tgl_validasi_menyerahkan=values(tgl_validasi_menyerahkan), nip_menyerahkan=values(nip_menyerahkan), status_menyerahkan='Tervalidasi'"
                                    : "tgl_validasi_menerima=values(tgl_validasi_menerima), nip_menerima=values(nip_menerima), status_menerima='Tervalidasi'"));
            try {
                psTte.setString(1, noRawat);
                psTte.setString(2, tglMasuk);
                psTte.setString(3, now);
                psTte.setString(4, nip);
                psTte.executeUpdate();

                if (r != -1) {
                    getData();
                }
                JOptionPane.showMessageDialog(this,
                        "Tanda Tangan Elektronik Petugas (" + nama + ") berhasil disahkan!");
            } finally {
                psTte.close();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengesahkan TTE: " + ex.getMessage());
        }
    }

    private void validasiTteDokter() {
        int r = tbObat.getSelectedRow();
        String noRawat = (r != -1) ? tbObat.getValueAt(r, 0).toString().trim() : TNoRw.getText().trim();
        String tglMasuk = (r != -1) ? tbObat.getValueAt(r, 5).toString().trim()
                : TanggalMasuk.getTanggalDanJamSql(Valid);
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih data transfer terlebih dahulu!");
            return;
        }

        String kdDokter = KdDokterMerawatTujuan1.getText().trim();
        String nmDokter = NmDokterMerawatTujuan1.getText().trim();
        if (kdDokter.isEmpty()) {
            kdDokter = KdDokterMerawatAsal1.getText().trim();
            nmDokter = NmDokterMerawatAsal1.getText().trim();
        }
        if (kdDokter.isEmpty() && r != -1) {
            try (PreparedStatement psD = koneksi.prepareStatement(
                    "select kd_dokter_tujuan_1, kd_dokter_asal_1 from transfer_pasien_antar_ruang where no_rawat=? and tanggal_masuk=?")) {
                psD.setString(1, noRawat);
                psD.setString(2, tglMasuk);
                try (ResultSet rsD = psD.executeQuery()) {
                    if (rsD.next()) {
                        kdDokter = rsD.getString("kd_dokter_tujuan_1");
                        if (kdDokter == null || kdDokter.isEmpty()) {
                            kdDokter = rsD.getString("kd_dokter_asal_1");
                        }
                    }
                }
            } catch (Exception ex) {
                // ignore
            }
        }
        if (kdDokter == null || kdDokter.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dokter yang merawat belum ditentukan pada data transfer ini!");
            return;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            PreparedStatement psTte = koneksi.prepareStatement(
                    "insert into transfer_pasien_antar_ruang_konfirmasi " +
                            "(no_rawat, tanggal_masuk, tgl_validasi_dokter, kd_dokter, status_dokter) " +
                            "values (?, ?, ?, ?, 'Tervalidasi') " +
                            "on duplicate key update tgl_validasi_dokter=values(tgl_validasi_dokter), kd_dokter=values(kd_dokter), status_dokter='Tervalidasi'");
            try {
                psTte.setString(1, noRawat);
                psTte.setString(2, tglMasuk);
                psTte.setString(3, now);
                psTte.setString(4, kdDokter);
                psTte.executeUpdate();

                if (r != -1) {
                    getData();
                }
                JOptionPane.showMessageDialog(this, "Tanda Tangan Elektronik Dokter ("
                        + (nmDokter.isEmpty() ? kdDokter : nmDokter) + ") berhasil disahkan!");
            } finally {
                psTte.close();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengesahkan TTE Dokter: " + ex.getMessage());
        }
    }

    private void verifikasiKeluarga() {
        String namaKel = NamaMenyetujui.getText().trim();
        if (namaKel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama keluarga / penanggung jawab belum diisi!");
            return;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        lblStatusVerifKeluarga.setText("\u2713 Terverifikasi");
        lblStatusVerifKeluarga.setBackground(COL_EMERALD_SOFT);
        lblStatusVerifKeluarga.setForeground(COL_EMERALD_DARK);
        lblWaktuTtdKeluarga.setText("Waktu: " + now);
        barcodeKeluarga.regenerateBarcode("PJ-PAS-" + TNoRw.getText().replaceAll("[^0-9]", ""));
        JOptionPane.showMessageDialog(this, "Persetujuan transfer oleh keluarga (" + namaKel + ") terverifikasi!");
    }

    private void resetStatusTteDisplay() {
        lblStatusVerifPengirim.setText("Belum Validasi");
        lblStatusVerifPengirim.setBackground(COL_BG_MAIN);
        lblStatusVerifPengirim.setForeground(COL_TEXT_MUTED);
        lblWaktuTtdPengirim.setText("Waktu: -");

        lblStatusVerifPenerima.setText("Belum Validasi");
        lblStatusVerifPenerima.setBackground(COL_BG_MAIN);
        lblStatusVerifPenerima.setForeground(COL_TEXT_MUTED);
        lblWaktuTtdPenerima.setText("Waktu: -");

        lblStatusVerifDokter.setText("Belum Validasi");
        lblStatusVerifDokter.setBackground(COL_BG_MAIN);
        lblStatusVerifDokter.setForeground(COL_TEXT_MUTED);
        lblWaktuTtdDokter.setText("Waktu: -");

        lblStatusVerifKeluarga.setText("Belum Verifikasi");
        lblStatusVerifKeluarga.setBackground(COL_BG_MAIN);
        lblStatusVerifKeluarga.setForeground(COL_TEXT_MUTED);
        lblWaktuTtdKeluarga.setText("Waktu: -");
    }

    // =========================================================================
    // MULTI-SOURCE ADAPTER & PRE-FILL OTOMATIS (SMART SIMRS DATA FILLER)
    // =========================================================================

    private void tarikDataIgdDanRanap() {
        String noRawat = TNoRw.getText().trim();
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data pasien terlebih dahulu!");
            return;
        }

        int filledCount = 0;
        try {
            // 1. Tarik TTV dari pemeriksaan_ranap atau pemeriksaan_ralan
            PreparedStatement psTtv = koneksi.prepareStatement(
                    "select suhu_tubuh, tensi, nadi, respirasi, gcs, spo2, kesadaran from pemeriksaan_ranap " +
                            "where no_rawat=? order by tgl_perawatan desc, jam_rawat desc limit 1");
            try {
                psTtv.setString(1, noRawat);
                ResultSet rsTtv = psTtv.executeQuery();
                if (rsTtv.next()) {
                    txtTemp.setText(nz(rsTtv.getString("suhu_tubuh")));
                    txtHr.setText(nz(rsTtv.getString("nadi")));
                    txtRr.setText(nz(rsTtv.getString("respirasi")));
                    txtSpo2.setText(nz(rsTtv.getString("spo2")));

                    String td = nz(rsTtv.getString("tensi"));
                    if (td.contains("/")) {
                        String[] p = td.split("/");
                        txtTdSistol.setText(p[0].trim());
                        txtTdDiastol.setText(p.length > 1 ? p[1].trim() : "");
                    }
                    parseGcsToFields(rsTtv.getString("gcs"));
                    segKesadaran.setSelectedValue(nz(rsTtv.getString("kesadaran"), "Compos Mentis"));
                    filledCount++;
                }
                rsTtv.close();
            } finally {
                psTtv.close();
            }

            // 2. Tarik Diagnosa Utama dari diagnosa_pasien / penyakit
            PreparedStatement psDiag = koneksi.prepareStatement(
                    "select diagnosa_pasien.kd_penyakit, penyakit.nm_penyakit from diagnosa_pasien " +
                            "inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit " +
                            "where diagnosa_pasien.no_rawat=? and diagnosa_pasien.prioritas='1' limit 1");
            try {
                psDiag.setString(1, noRawat);
                ResultSet rsDiag = psDiag.executeQuery();
                if (rsDiag.next()) {
                    DiagnosaUtama.setText(rsDiag.getString("kd_penyakit") + " - " + rsDiag.getString("nm_penyakit"));
                    filledCount++;
                }
                rsDiag.close();
            } finally {
                psDiag.close();
            }

            // 3. Tarik Kamar Terakhir Pasien
            PreparedStatement psKamar = koneksi.prepareStatement(
                    "select bangsal.nm_bangsal, kamar.kelas from kamar_inap " +
                            "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar " +
                            "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal " +
                            "where kamar_inap.no_rawat=? order by tgl_masuk desc, jam_masuk desc limit 1");
            try {
                psKamar.setString(1, noRawat);
                ResultSet rsKamar = psKamar.executeQuery();
                if (rsKamar.next()) {
                    AsalRuang.setText(rsKamar.getString("nm_bangsal"));
                    KelasAsalRuang.setText(rsKamar.getString("kelas"));
                    segKelasAsal.setSelectedValue(rsKamar.getString("kelas"));
                    filledCount++;
                }
                rsKamar.close();
            } finally {
                psKamar.close();
            }

            updateGcsCalculation();
            updateProgress();
            updatePatientCard();

            JOptionPane.showMessageDialog(this,
                    "Berhasil menyalin data TTV, Diagnosa & Ruangan dari rekam medis SIMRS (" + filledCount
                            + " kategori disinkronkan).",
                    "Integrasi SIMRS Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyalin data dari SIMRS: " + e.getMessage());
        }
    }

    private void fillStandardDocuments() {
        txtDokumen1.setText("Berkas Rekam Medis Pasien (RM 01, RM 03 IGD & Catatan Rawat)");
        txtDokumen2.setText("Lembar Hasil Pemeriksaan Laboratorium Terakhir");
        txtDokumen3.setText("Foto Rontgen Thorax / Hasil Radiologi & Rekaman EKG");
        txtDokumen4.setText("Formulir Rekonsiliasi Obat & Kartu Pemberian Obat");
        txtDokumen5.setText("Surat Pengantar Rawat Inap & General Consent");
        updateProgress();
        JOptionPane.showMessageDialog(this, "5 Dokumen Standar Serah Terima RM 04 berhasil diisi otomatis.");
    }

    private void applyDefaultNormalValues() {
        if (txtTdSistol.getText().isEmpty())
            txtTdSistol.setText("120");
        if (txtTdDiastol.getText().isEmpty())
            txtTdDiastol.setText("80");
        if (txtHr.getText().isEmpty())
            txtHr.setText("80");
        if (txtRr.getText().isEmpty())
            txtRr.setText("20");
        if (txtTemp.getText().isEmpty())
            txtTemp.setText("36.5");
        if (txtSpo2.getText().isEmpty())
            txtSpo2.setText("98");

        txtGcsE.setText("4");
        txtGcsM.setText("6");
        txtGcsV.setText("5");
        updateGcsCalculation();

        segKesadaran.setSelectedValue("Compos Mentis");
        TingkatNyeriTransfer.setSelectedItem("0");
        updateNyeriDisplay("0");
        segTherapyOksigen.setSelectedValue("Tidak");

        chkPrecautionStandar.setSelected(true);
        segNutrisi.setSelectedValue("Oral");
        segBab.setSelectedValue("Normal");
        segBak.setSelectedValue("Normal");

        segAktifitas.setSelectedValue("Mandiri");
        segMobilisasi.setSelectedValue("Jalan");
        chkLukaTidakAda.setSelected(true);

        chkAlatTanpa.setSelected(true);
        segRiwayatAlergi.setSelectedValue("Tidak");
        TJenisAlergi.setText("Tidak ada");
        TReaksiAlergi.setText("-");

        fillStandardDocuments();
        updateProgress();
    }

    // =========================================================================
    // REACTIVE CALCULATIONS & EVENT LISTENERS
    // =========================================================================

    private void pasangListeners() {
        // Reactive GCS calculation
        DocumentListener gcsListener = new SimpleDocumentListener(this::updateGcsCalculation);
        txtGcsE.getDocument().addDocumentListener(gcsListener);
        txtGcsM.getDocument().addDocumentListener(gcsListener);
        txtGcsV.getDocument().addDocumentListener(gcsListener);

        // Reactive Progress calculation
        DocumentListener progressListener = new SimpleDocumentListener(this::updateProgress);
        for (JTextComponent tc : listTrackedFields) {
            tc.getDocument().addDocumentListener(progressListener);
        }

        // Reactive Signature Name Sync
        DocumentListener sigSync = new SimpleDocumentListener(() -> {
            String p1 = NmPetugasMenyerahkan.getText().trim();
            String p2 = NmPetugasMenerima.getText().trim();
            String dok = NmDokterMerawatTujuan1.getText().trim();
            if (dok.isEmpty())
                dok = NmDokterMerawatAsal1.getText().trim();
            String kel = NamaMenyetujui.getText().trim();

            lblNamaTtdPengirim.setText("( " + (p1.isEmpty() ? "........................................" : p1) + " )");
            lblNamaTtdPenerima.setText("( " + (p2.isEmpty() ? "........................................" : p2) + " )");
            lblNamaTtdDokter.setText("( " + (dok.isEmpty() ? "........................................" : dok) + " )");
            lblNamaTtdKeluarga
                    .setText("( " + (kel.isEmpty() ? "........................................" : kel) + " )");
        });
        NmPetugasMenyerahkan.getDocument().addDocumentListener(sigSync);
        NmPetugasMenerima.getDocument().addDocumentListener(sigSync);
        NmDokterMerawatTujuan1.getDocument().addDocumentListener(sigSync);
        NmDokterMerawatAsal1.getDocument().addDocumentListener(sigSync);
        NamaMenyetujui.getDocument().addDocumentListener(sigSync);

        // Nyeri listener
        TingkatNyeriTransfer.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateNyeriDisplay(TingkatNyeriTransfer.getSelectedItem() + "");
            }
        });

        // Pilih Ruang Kamar Asal / Tujuan listener
        PilihRuangAsal.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && PilihRuangAsal.getSelectedItem() instanceof KamarItem) {
                KamarItem item = (KamarItem) PilihRuangAsal.getSelectedItem();
                if (!item.kdKamar.isEmpty()) {
                    AsalRuang.setText(item.label);
                    KelasAsalRuang.setText(item.kelas);
                    segKelasAsal.setSelectedValue(item.kelas);
                    updatePatientCard();
                }
            }
        });

        PilihRuangTujuan.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && PilihRuangTujuan.getSelectedItem() instanceof KamarItem) {
                KamarItem item = (KamarItem) PilihRuangTujuan.getSelectedItem();
                if (!item.kdKamar.isEmpty()) {
                    RuangSelanjutnya.setText(item.label);
                    KelasRuangSelanjutnya.setText(item.kelas);
                    segKelasTujuan.setSelectedValue(item.kelas);
                    updatePatientCard();
                }
            }
        });

        // Segmented Kelas listener
        segKelasAsal.addSelectionListener(val -> KelasAsalRuang.setText(val));
        segKelasTujuan.addSelectionListener(val -> KelasRuangSelanjutnya.setText(val));
    }

    private void updateGcsCalculation() {
        int e = parseSafeInt(txtGcsE.getText(), 4);
        int m = parseSafeInt(txtGcsM.getText(), 6);
        int v = parseSafeInt(txtGcsV.getText(), 5);
        int total = e + m + v;

        lblTotalGcsBadge.setText("Total GCS: " + total + " (E" + e + "M" + m + "V" + v + ")");
        if (total <= 8) {
            lblTotalGcsBadge.setBackground(COL_ROSE_SOFT);
            lblTotalGcsBadge.setForeground(COL_ROSE_PRIMARY);
            lblTotalGcsBadge.putClientProperty("customBorderColor", COL_ROSE_BORDER);
        } else {
            lblTotalGcsBadge.setBackground(COL_TEAL_SOFT);
            lblTotalGcsBadge.setForeground(COL_TEAL_DARK);
            lblTotalGcsBadge.putClientProperty("customBorderColor", COL_TEAL_BORDER);
        }
        lblTotalGcsBadge.repaint();
    }

    private int parseSafeInt(String text, int def) {
        if (text == null || text.trim().isEmpty())
            return def;
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception ex) {
            return def;
        }
    }

    private void updateNyeriDisplay(String val) {
        int n = parseSafeInt(val, 0);
        String ket;
        Color c;
        if (n == 0) {
            ket = "0 \u00B7 Tidak ada rasa nyeri";
            c = COL_EMERALD_DARK;
        } else if (n <= 3) {
            ket = n + " \u00B7 Nyeri Ringan (Masih dapat beraktifitas & dialihkan)";
            c = new Color(13, 148, 136);
        } else if (n <= 6) {
            ket = n + " \u00B7 Nyeri Sedang (Mengganggu konsentrasi & aktifitas)";
            c = new Color(217, 119, 6);
        } else if (n <= 9) {
            ket = n + " \u00B7 Nyeri Berat (Tidak dapat beraktifitas, butuh analgesik)";
            c = new Color(225, 29, 72);
        } else {
            ket = "10 \u00B7 Nyeri Sangat Hebat (Tak tertahankan, tidak dapat tidur)";
            c = new Color(185, 28, 28);
        }

        lblNyeriKeterangan.setText(ket);
        lblNyeriKeterangan.setForeground(c);
    }

    public void updateProgress() {
        int filled = 0;
        int total = 20;

        if (!TNoRw.getText().trim().isEmpty())
            filled++;
        if (!AsalRuang.getText().trim().isEmpty())
            filled++;
        if (!RuangSelanjutnya.getText().trim().isEmpty())
            filled++;
        if (!DiagnosaUtama.getText().trim().isEmpty())
            filled++;
        if (!NamaMenyetujui.getText().trim().isEmpty())
            filled++;
        if (!KdPetugasMenyerahkan.getText().trim().isEmpty())
            filled++;
        if (!KdPetugasMenerima.getText().trim().isEmpty())
            filled++;
        if (!txtTdSistol.getText().trim().isEmpty())
            filled++;
        if (!txtHr.getText().trim().isEmpty())
            filled++;
        if (!txtRr.getText().trim().isEmpty())
            filled++;
        if (!txtTemp.getText().trim().isEmpty())
            filled++;
        if (!txtGcsE.getText().trim().isEmpty())
            filled++;
        if (!txtSpo2.getText().trim().isEmpty())
            filled++;
        if (!txtProsedur1.getText().trim().isEmpty())
            filled++;
        if (!txtTherapy1.getText().trim().isEmpty())
            filled++;
        if (!txtRtl1.getText().trim().isEmpty())
            filled++;
        if (!txtMasalahKep1.getText().trim().isEmpty())
            filled++;
        if (!txtDokumen1.getText().trim().isEmpty())
            filled++;
        if (!txtHasilInvestigasiAbnormal.getText().trim().isEmpty())
            filled++;
        if (!txtTindakanSudahDilakukan.getText().trim().isEmpty())
            filled++;

        int pct = (int) Math.round(((double) filled / total) * 100);
        progressTransfer.setValue(filled);
        lblProgressPercent.setText(pct + "%");
        lblProgressCount.setText(filled + "/" + total + " parameter terisi");

        int checkedSeksi = 0;
        for (JCheckBox chk : listHandoverChecks) {
            if (chk.isSelected())
                checkedSeksi++;
        }
        lblHandoverSummary.setText("Ceklis: " + checkedSeksi + "/" + listHandoverChecks.size() + " seksi");
    }

    private void updatePatientCard() {
        String nama = TPasien.getText().trim();
        lblPatientName.setText(nama.isEmpty() ? "-" : nama);

        StringBuilder inisial = new StringBuilder();
        for (String b : nama.split("\\s+")) {
            if (!b.isEmpty() && inisial.length() < 2) {
                inisial.append(Character.toUpperCase(b.charAt(0)));
            }
        }
        lblAvatarInitial.setText(inisial.length() == 0 ? "PS" : inisial.toString());

        try {
            LocalDate lahir = LocalDate.parse(TglLahir.getText());
            LocalDate skrg = LocalDate.now();
            Period p = Period.between(lahir, skrg);
            String umurStr = p.getYears() + " Th " + p.getMonths() + " Bl";
            lblPatientGenderAge.setText((Jk.getText().isEmpty() ? "" : (Jk.getText() + " \u2022 ")) + umurStr);
            lblValLahir.setText(TglLahir.getText() + " (" + p.getYears() + " Th)");
        } catch (Exception ex) {
            lblPatientGenderAge
                    .setText(Jk.getText().isEmpty() ? "-" : (Jk.getText() + " \u2022 " + TglLahir.getText()));
            lblValLahir.setText(TglLahir.getText().isEmpty() ? "-" : TglLahir.getText());
        }

        lblValRM.setText(TNoRM.getText().isEmpty() ? "-" : TNoRM.getText());
        lblValRawat.setText(TNoRw.getText().isEmpty() ? "-" : TNoRw.getText());
        lblValKamarAsal.setText(AsalRuang.getText().trim().isEmpty() ? "-" : AsalRuang.getText().trim());
        lblValKamarTujuan
                .setText(RuangSelanjutnya.getText().trim().isEmpty() ? "-" : RuangSelanjutnya.getText().trim());

        if (lblNoRM != null)
            lblNoRM.setText(lblValRM.getText());
        if (lblNoRw != null)
            lblNoRw.setText(lblValRawat.getText());
        if (lblRouteTransfer != null) {
            String rute = AsalRuang.getText() + " \u2192 " + RuangSelanjutnya.getText();
            lblRouteTransfer.setText(rute.replaceAll(" \u2192 $", "").isEmpty() ? "-" : rute);
        }
    }

    private void trackField(JTextComponent comp) {
        listTrackedFields.add(comp);
    }

    // =========================================================================
    // CETAK LAPORAN TRANSFER (JASPER REPORTS)
    // =========================================================================

    private void cetakLaporanTransfer(String mode) {
        if (tbObat.getSelectedRow() == -1 && TNoRw.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Silakan pilih data transfer yang ingin dicetak terlebih dahulu!");
            return;
        }

        String noRawatCetak = isEditMode ? editNoRawat
                : (tbObat.getSelectedRow() != -1 ? tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
                        : TNoRw.getText().trim());
        String tglMasukCetak = isEditMode ? editTanggalMasuk
                : (tbObat.getSelectedRow() != -1 ? tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString()
                        : TanggalMasuk.getTanggalDanJamSql(Valid));

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

        String sqlReport = "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir, "
                +
                "transfer_pasien_antar_ruang.*, " +
                "petugasmenyerahkan.nama as nm_menyerahkan,petugasmenerima.nama as nm_menerima, " +
                "dokterasal1.nm_dokter as nm_dokter_asal_1,dokterasal2.nm_dokter as nm_dokter_asal_2,dokterasal3.nm_dokter as nm_dokter_asal_3, "
                +
                "doktertujuan1.nm_dokter as nm_dokter_tujuan_1,doktertujuan2.nm_dokter as nm_dokter_tujuan_2,doktertujuan3.nm_dokter as nm_dokter_tujuan_3 "
                +
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat " +
                "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "
                +
                "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip "
                +
                "left join dokter as dokterasal1 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_1=dokterasal1.kd_dokter "
                +
                "left join dokter as dokterasal2 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_2=dokterasal2.kd_dokter "
                +
                "left join dokter as dokterasal3 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_3=dokterasal3.kd_dokter "
                +
                "left join dokter as doktertujuan1 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_1=doktertujuan1.kd_dokter "
                +
                "left join dokter as doktertujuan2 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_2=doktertujuan2.kd_dokter "
                +
                "left join dokter as doktertujuan3 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_3=doktertujuan3.kd_dokter "
                +
                "where transfer_pasien_antar_ruang.no_rawat='" + noRawatCetak
                + "' and transfer_pasien_antar_ruang.tanggal_masuk='" + tglMasukCetak + "'";

        Valid.MyReportqry("rptCetakTransferPasienAntarRuang.jasper", "report",
                "::[ Cetak Dokumen Transfer Pasien RM 04 ]::", sqlReport, param);
        this.setCursor(Cursor.getDefaultCursor());
    }

    // =========================================================================
    // GENERATOR DOKUMEN HTML PRESISI FORMAT RESMI BLANGKO RM 04-1 & RM 04-2
    // =========================================================================

    private String bangunDokumenHtmlLengkap(String noRawat, String tanggalMasuk) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
                .append("body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; font-size: 10px; color: #0F172A; margin: 10px; line-height: 1.35; }")
                .append(".kop-table { width: 100%; border-bottom: 2px solid #0F766E; margin-bottom: 8px; padding-bottom: 4px; }")
                .append(".kop-logo { width: 50px; font-weight: bold; color: #0F766E; font-size: 18px; text-align: center; }")
                .append(".kop-title { text-align: center; font-size: 11px; font-weight: bold; color: #0F172A; }")
                .append(".kop-sub { text-align: center; font-size: 9px; color: #475569; }")
                .append(".doc-banner { background-color: #0A2E2C; color: #FFFFFF; font-weight: bold; font-size: 11px; padding: 4px 8px; text-align: center; margin-bottom: 6px; }")
                .append(".card-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }")
                .append(".card-table td { border: 1px solid #CBD5E1; padding: 4px 6px; font-size: 9.5px; vertical-align: top; }")
                .append(".section-header { background-color: #F0FDFA; color: #0F766E; font-weight: bold; font-size: 10px; padding: 4px 6px; border: 1px solid #CBD5E1; }")
                .append(".lbl-cell { background-color: #F8FAFC; font-weight: bold; color: #334155; width: 28%; }")
                .append(".tte-box { text-align: center; padding: 8px; border: 1px solid #E2E8F0; background-color: #FAFAFA; }")
                .append("</style></head><body>");

        // Kop Rumah Sakit
        html.append("<table class='kop-table'><tr>")
                .append("<td class='kop-logo'>RM</td>")
                .append("<td>")
                .append("<div class='kop-title'>PEMERINTAH KABUPATEN LAMPUNG TENGAH<br>RSUD DEMANG SEPULAU RAYA</div>")
                .append("<div class='kop-sub'>Jl. Raya Lintas Sumatera, Gunung Sugih, Lampung Tengah &bull; Telp. (0725) 529828</div>")
                .append("</td>")
                .append("<td style='text-align:right; font-family: monospace; font-weight:bold; font-size:10px;'>RM 04-1 & RM 04-2</td>")
                .append("</tr></table>");

        // Judul Dokumen
        html.append("<div class='doc-banner'>TRANSFER PASIEN ANTAR RUANGAN (RM 04)</div>");

        // Tabel Data Pasien
        html.append("<table class='card-table'>")
                .append("<tr>")
                .append("<td class='lbl-cell'>No. RM / No. Rawat</td><td><b>").append(TNoRM.getText()).append("</b> / ")
                .append(TNoRw.getText()).append("</td>")
                .append("<td class='lbl-cell'>Tgl Masuk Rawat</td><td>").append(TanggalMasuk.getSelectedItem())
                .append("</td>")
                .append("</tr><tr>")
                .append("<td class='lbl-cell'>Nama Pasien</td><td><b>").append(TPasien.getText()).append("</b> (")
                .append(Jk.getText()).append(")</td>")
                .append("<td class='lbl-cell'>Tgl Pindah / Transfer</td><td>").append(TanggalPindah.getSelectedItem())
                .append("</td>")
                .append("</tr><tr>")
                .append("<td class='lbl-cell'>Tanggal Lahir / Umur</td><td>").append(TglLahir.getText()).append("</td>")
                .append("<td class='lbl-cell'>Rute Kepindahan</td><td><b>").append(AsalRuang.getText())
                .append("</b> &rarr; <b>").append(RuangSelanjutnya.getText()).append("</b></td>")
                .append("</tr></table>");

        // 1. Informasi Transfer & Ruangan
        html.append("<div class='section-header'>I. INFORMASI TRANSFER, KELUARGA & RUANGAN</div>");
        html.append("<table class='card-table'>")
                .append("<tr><td class='lbl-cell'>Diagnosa Utama</td><td><b>").append(DiagnosaUtama.getText())
                .append("</b></td></tr>")
                .append("<tr><td class='lbl-cell'>Diagnosa Sekunder</td><td>")
                .append(DiagnosaSekunder.getText().isEmpty() ? "-" : DiagnosaSekunder.getText()).append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Keluarga Menyetujui</td><td>").append(NamaMenyetujui.getText())
                .append(" (").append(HubunganMenyetujui.getSelectedItem()).append(") &bull; Telp: ")
                .append(TNoTlpMenyetujui.getText()).append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Penjelasan Diagnosa</td><td>")
                .append(segPenjelasanDiagnosa.getSelectedValue()).append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Ruang & Dokter Asal</td><td>").append(AsalRuang.getText())
                .append(" (").append(KelasAsalRuang.getText()).append(") &bull; Dokter: ")
                .append(NmDokterMerawatAsal1.getText()).append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Ruang & Dokter Tujuan</td><td>").append(RuangSelanjutnya.getText())
                .append(" (").append(KelasRuangSelanjutnya.getText()).append(") &bull; Dokter: ")
                .append(NmDokterMerawatTujuan1.getText()).append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Alasan Pindah & Cara</td><td>")
                .append(IndikasiPindah.getSelectedItem()).append(" (").append(KeteranganIndikasiPindahRuang.getText())
                .append(") &bull; Cara: <b>").append(segCaraPindah.getSelectedValue()).append("</b></td></tr>")
                .append("</table>");

        // 2. Kondisi Pasien Saat Transfer
        html.append("<div class='section-header'>II. KONDISI PASIEN SAAT TRANSFER</div>");
        html.append("<table class='card-table'>")
                .append("<tr><td class='lbl-cell'>Waktu Observasi</td><td>")
                .append(ObservasiTerakhirPukul.getSelectedItem()).append(" &bull; Kesadaran: <b>")
                .append(segKesadaran.getSelectedValue()).append("</b></td></tr>")
                .append("<tr><td class='lbl-cell'>Skala GCS</td><td><b>").append(lblTotalGcsBadge.getText())
                .append("</b></td></tr>")
                .append("<tr><td class='lbl-cell'>Tanda Vital</td><td>TD: <b>").append(getTdText())
                .append("</b> mmHg &bull; HR: <b>").append(txtHr.getText()).append("</b> x/mnt &bull; RR: <b>")
                .append(txtRr.getText()).append("</b> x/mnt &bull; Suhu: <b>").append(txtTemp.getText())
                .append("</b> &deg;C &bull; SpO2: <b>").append(txtSpo2.getText()).append("</b> %</td></tr>")
                .append("<tr><td class='lbl-cell'>Tingkat Nyeri</td><td>").append(lblNyeriKeterangan.getText())
                .append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Therapy Oksigen</td><td>")
                .append(segTherapyOksigen.getSelectedValue()).append(" (Aliran: ")
                .append(txtAliranOksigen.getText().isEmpty() ? "-" : txtAliranOksigen.getText() + " L/mnt")
                .append(")</td></tr>")
                .append("</table>");

        // 3. Data Serah Terima Klinis
        html.append(
                "<div class='section-header'>III. DATA SERAH TERIMA KLINIS (LINE INVASIF, NUTRISI & ELIMINASI)</div>");
        html.append("<table class='card-table'>")
                .append("<tr><td class='lbl-cell'>Line Invasif</td><td>").append(segLineInvasif.getSelectedValue())
                .append(" (Tgl Pasang: ").append(LineInvasifTanggal.getSelectedItem()).append(")</td></tr>")
                .append("<tr><td class='lbl-cell'>Prosedur Invasif / Operasi</td><td>")
                .append(getProsedurDilakukanText().replaceAll("\n", "<br>")).append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Kewaspadaan</td><td>")
                .append("Standar: " + ya(chkPrecautionStandar) + ", Kontak: " + ya(chkPrecautionKontak) + ", Airborne: "
                        + ya(chkPrecautionAirborne) + ", Droplet: " + ya(chkPrecautionDroplet))
                .append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Nutrisi</td><td>Status: <b>").append(segNutrisi.getSelectedValue())
                .append("</b> &bull; Diet Khusus: ")
                .append(chkDietKhusus.isSelected() ? TNutrisiDietKhususJelaskan.getText() : "-").append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Eliminasi</td><td>BAB: <b>").append(segBab.getSelectedValue())
                .append("</b> &bull; BAK: <b>").append(segBak.getSelectedValue()).append("</b> (No Kateter: ")
                .append(TEliminasiBakKateterNo.getText().isEmpty() ? "-" : TEliminasiBakKateterNo.getText())
                .append(")</td></tr>")
                .append("</table>");

        // 4. Aktifitas, Drainase & Luka
        html.append("<div class='section-header'>IV. AKTIFITAS, DRAINASE & LUKA</div>");
        html.append("<table class='card-table'>")
                .append("<tr><td class='lbl-cell'>Fungsional & Mobilisasi</td><td>Aktifitas: <b>")
                .append(segAktifitas.getSelectedValue()).append("</b> &bull; Mobilisasi: <b>")
                .append(segMobilisasi.getSelectedValue()).append("</b></td></tr>")
                .append("<tr><td class='lbl-cell'>Drainase</td><td>WSD: ").append(ya(chkDrainWsd)).append(", Stoma: ")
                .append(ya(chkDrainStoma)).append(", Drain Luka: ").append(ya(chkDrainLuka)).append(" ")
                .append(TDrainaseLainKet.getText()).append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Integritas Kulit / Luka</td><td>")
                .append(chkLukaTidakAda.isSelected() ? "<b>Tidak Ada Luka (Integritas Kulit Utuh)</b>"
                        : "Ada Luka (Lihat rincian tabel luka)")
                .append("</td></tr>")
                .append("</table>");

        // 5. Investigasi Abnormal & Penunjang
        html.append("<div class='section-header'>V. INVESTIGASI ABNORMAL, ALERGI & PENUNJANG</div>");
        html.append("<table class='card-table'>")
                .append("<tr><td class='lbl-cell'>Investigasi Abnormal</td><td>")
                .append(txtHasilInvestigasiAbnormal.getText().isEmpty() ? "-"
                        : txtHasilInvestigasiAbnormal.getText().replaceAll("\n", "<br>"))
                .append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Riwayat Alergi</td><td>").append(segRiwayatAlergi.getSelectedValue())
                .append(" (Jenis: ").append(TJenisAlergi.getText()).append(", Reaksi: ").append(TReaksiAlergi.getText())
                .append(")</td></tr>")
                .append("<tr><td class='lbl-cell'>Tindakan Sudah Dilakukan</td><td>")
                .append(txtTindakanSudahDilakukan.getText().isEmpty() ? "-"
                        : txtTindakanSudahDilakukan.getText().replaceAll("\n", "<br>"))
                .append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Pemeriksaan Penunjang</td><td>Lab: ")
                .append(TPenunjangLaboratoriumKet.getText()).append("<br>Radiologi: ")
                .append(TPenunjangRadiologiKet.getText()).append("<br>CT Scan: ").append(TPenunjangCtScanKet.getText())
                .append("</td></tr>")
                .append("</table>");

        // 6. Terapi & Rencana Tindak Lanjut
        html.append("<div class='section-header'>VI. TERAPI OBAT & RENCANA TINDAK LANJUT</div>");
        html.append("<table class='card-table'>")
                .append("<tr><td class='lbl-cell'>Terapi yang Diberikan</td><td>")
                .append(getObatYangDiberikanText().isEmpty() ? "-"
                        : getObatYangDiberikanText().replaceAll("\n", "<br>"))
                .append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Rencana Tindak Lanjut</td><td>")
                .append(getRtlText().isEmpty() ? "-" : getRtlText().replaceAll("\n", "<br>")).append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Masalah Keperawatan</td><td>")
                .append(getMasalahKepText().isEmpty() ? "-" : getMasalahKepText().replaceAll("\n", "<br>"))
                .append("</td></tr>")
                .append("<tr><td class='lbl-cell'>Dokumen Diserahterimakan</td><td>")
                .append(getDokumenText().isEmpty() ? "-" : getDokumenText().replaceAll("\n", "<br>"))
                .append("</td></tr>")
                .append("</table>");

        // 7. Otorisasi Serah Terima & TTE
        html.append("<div class='section-header'>VII. OTORISASI SERAH TERIMA & TANDA TANGAN ELEKTRONIK</div>");
        html.append("<table class='card-table'><tr>")
                .append("<td class='tte-box' width='25%'><b>Petugas Menyerahkan</b><br><br><span style='color:#0F766E; font-weight:bold;'>[ QR TTE AKTIF ]</span><br><br><b>")
                .append(NmPetugasMenyerahkan.getText().isEmpty() ? ".........................."
                        : NmPetugasMenyerahkan.getText())
                .append("</b><br>NIP: ").append(KdPetugasMenyerahkan.getText()).append("</td>")
                .append("<td class='tte-box' width='25%'><b>Petugas Menerima</b><br><br><span style='color:#0F766E; font-weight:bold;'>[ QR TTE AKTIF ]</span><br><br><b>")
                .append(NmPetugasMenerima.getText().isEmpty() ? ".........................."
                        : NmPetugasMenerima.getText())
                .append("</b><br>NIP: ").append(KdPetugasMenerima.getText()).append("</td>")
                .append("<td class='tte-box' width='25%'><b>Dokter yang Merawat</b><br><br><span style='color:#0F766E; font-weight:bold;'>[ QR TTE AKTIF ]</span><br><br><b>")
                .append(NmDokterMerawatTujuan1.getText().isEmpty() ? NmDokterMerawatAsal1.getText()
                        : NmDokterMerawatTujuan1.getText())
                .append("</b><br>SIP/Kode: ").append(KdDokterMerawatTujuan1.getText()).append("</td>")
                .append("<td class='tte-box' width='25%'><b>Pasien / Keluarga</b><br><br><span style='color:#0F766E; font-weight:bold;'>[ VERIFIKASI ]</span><br><br><b>")
                .append(NamaMenyetujui.getText().isEmpty() ? ".........................." : NamaMenyetujui.getText())
                .append("</b><br>(").append(HubunganMenyetujui.getSelectedItem()).append(")</td>")
                .append("</tr></table>");

        html.append("</body></html>");
        return html.toString();
    }

    // =========================================================================
    // HELPER UI BUILDERS & KOMPONEN ARGON (STANDAR RESMI RM 20)
    // =========================================================================

    private JPanel createBaseCardPanel() {
        return new RoundedCardPanel(12);
    }

    private JPanel createSectionCard(String numberCode, String title, String subtitle, boolean includeHandoverBadges) {
        JPanel card = createBaseCardPanel();
        card.setLayout(new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COL_CARD_HEADER);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER_DIVIDER),
                new EmptyBorder(10, 16, 10, 16)));

        JPanel titleStack = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleStack.setOpaque(false);

        JLabel numBadge = new JLabel(numberCode, SwingConstants.CENTER) {
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
        numBadge.setPreferredSize(new Dimension(28, 28));
        numBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        numBadge.setForeground(COL_TEAL_DARK);
        numBadge.setOpaque(false);

        JPanel textStack = new JPanel(new GridLayout(2, 1, 0, 1));
        textStack.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_SECTION);
        lblTitle.setForeground(COL_TEXT_DARK);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_SUBLABEL);
        lblSub.setForeground(COL_TEXT_MUTED);

        textStack.add(lblTitle);
        textStack.add(lblSub);

        titleStack.add(numBadge);
        titleStack.add(textStack);
        header.add(titleStack, BorderLayout.WEST);

        if (includeHandoverBadges) {
            JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            badgePanel.setOpaque(false);

            JCheckBox chkHandover = new JCheckBox("Terverifikasi Serah Terima");
            styleCheckBoxChip(chkHandover);
            chkHandover.setSelected(true);
            chkHandover.addItemListener(e -> updateProgress());
            listHandoverChecks.add(chkHandover);

            badgePanel.add(chkHandover);
            header.add(badgePanel, BorderLayout.EAST);
        }

        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private void addHorizontalFormRow(JPanel container, int rowY, String label, String sublabel, Component field,
            boolean separator) {
        JPanel rowPanel = new JPanel(new BorderLayout(14, 0));
        rowPanel.setOpaque(false);
        rowPanel.setBorder(new CompoundBorder(
                separator ? new MatteBorder(0, 0, 1, 0, COL_BORDER_DIVIDER) : new EmptyBorder(0, 0, 0, 0),
                new EmptyBorder(6, 0, 6, 0)));

        JPanel leftLabelBox = new JPanel();
        leftLabelBox.setLayout(new BoxLayout(leftLabelBox, BoxLayout.Y_AXIS));
        leftLabelBox.setOpaque(false);
        leftLabelBox.setPreferredSize(new Dimension(LEFT_LABEL_WIDTH, 34));

        JLabel lMain = new JLabel(label);
        lMain.setFont(FONT_ROW_LABEL);
        lMain.setForeground(COL_TEXT_DARK);
        lMain.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftLabelBox.add(lMain);

        if (sublabel != null && !sublabel.isEmpty()) {
            leftLabelBox.add(Box.createVerticalStrut(2));
            JLabel lSub = new JLabel(sublabel);
            lSub.setFont(FONT_SUBLABEL);
            lSub.setForeground(COL_TEXT_MUTED);
            lSub.setAlignmentX(Component.LEFT_ALIGNMENT);
            leftLabelBox.add(lSub);
        }

        rowPanel.add(leftLabelBox, BorderLayout.WEST);
        rowPanel.add(field, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = rowY;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        container.add(rowPanel, gbc);
    }

    private JPanel wrapFieldWithMicroHeader(String microHeaderText, JComponent field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 3));
        wrapper.setOpaque(false);
        JLabel lbl = new JLabel(microHeaderText);
        lbl.setFont(FONT_MICRO_HEADER);
        lbl.setForeground(COL_TEXT_MUTED);
        wrapper.add(lbl, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    private JScrollPane wrapTextArea(JTextArea ta, int preferredHeight) {
        RoundedScrollPane sp = new RoundedScrollPane(ta, 8);
        sp.setPreferredSize(new Dimension(100, preferredHeight));
        return sp;
    }

    private JPanel createKeyValueRow(String key, JLabel lblValue, boolean mono) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lKey = new JLabel(key);
        lKey.setFont(FONT_SUBLABEL);
        lKey.setForeground(COL_TEXT_MUTED);

        lblValue.setFont(mono ? FONT_MONO : new Font("Segoe UI", Font.BOLD, 11));
        lblValue.setForeground(COL_TEXT_DARK);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lKey, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.EAST);
        return row;
    }

    private widget.TextBox createTextBox(int maxLen, boolean editable) {
        return new RoundedTextBox(maxLen, editable, 8);
    }

    private JTextArea createTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 20);
        ta.setFont(FONT_INPUT);
        ta.setForeground(COL_TEXT_DARK);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setOpaque(false);
        ta.setBorder(new EmptyBorder(5, 8, 5, 8));
        return ta;
    }

    private void styleComboBox(widget.ComboBox cmb) {
        cmb.setFont(FONT_INPUT);
        cmb.setBackground(Color.WHITE);
        cmb.putClientProperty("Component.arc", 8);
        cmb.putClientProperty("ComboBox.arc", 8);
        cmb.putClientProperty("JComboBox.isSquare", false);
        cmb.setBorder(new CompoundBorder(
                new RoundedBorder(COL_INPUT_BORDER, 8, 1.0f),
                new EmptyBorder(2, 6, 2, 6)));
        cmb.setPreferredSize(new Dimension(cmb.getPreferredSize().width, 28));
    }

    private widget.Tanggal styleTanggal(widget.Tanggal tgl) {
        if (tgl == null)
            return null;
        tgl.setFont(FONT_INPUT);
        tgl.setForeground(COL_TEXT_DARK);
        tgl.setBackground(Color.WHITE);
        tgl.putClientProperty("Component.arc", 8);
        tgl.putClientProperty("ComboBox.arc", 8);
        tgl.putClientProperty("JComponent.roundRect", false);
        tgl.setBorder(new CompoundBorder(
                new RoundedBorder(COL_INPUT_BORDER, 8, 1.0f),
                new EmptyBorder(0, 8, 0, 8)));
        return tgl;
    }

    private void styleCheckBoxChip(JCheckBox chk) {
        chk.setFont(FONT_INPUT);
        chk.setForeground(COL_TEXT_DARK);
        chk.setOpaque(false);
        chk.setFocusPainted(false);
        chk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JPanel createLookupFieldPanel(widget.TextBox tf, Runnable onLookupClick) {
        JButton btn = new JButton(new SearchMagnifierIcon(13, COL_TEXT_MUTED)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (Boolean.TRUE.equals(getClientProperty("hover"))) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(240, 253, 250)); // Teal 50
                    g2.fill(new RoundRectangle2D.Float(2f, 2f, getWidth() - 4f, getHeight() - 4f, 6, 6));
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Cari Data Master");
        btn.setRolloverEnabled(true);
        btn.addActionListener(e -> onLookupClick.run());
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setIcon(new SearchMagnifierIcon(13, COL_TEAL_PRIMARY));
                btn.putClientProperty("hover", true);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setIcon(new SearchMagnifierIcon(13, COL_TEXT_MUTED));
                btn.putClientProperty("hover", false);
                btn.repaint();
            }
        });

        JPanel p = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tf.isEditable() ? Color.WHITE : new Color(248, 250, 252));
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.setColor(COL_BORDER_DIVIDER);
                int divX = getWidth() - 32;
                g2.drawLine(divX, 4, divX, getHeight() - 4);
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
        p.setPreferredSize(new Dimension(p.getPreferredSize().width, 28));
        tf.setOpaque(false);
        tf.setBorder(new EmptyBorder(4, 10, 4, 6));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(4, 6, 4, 8));

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

    private JButton createFlatButton(String text, Icon icon, Color bg, Color fg, Color borderCol) {
        RoundedButton btn = new RoundedButton(text, bg, fg, borderCol, 8);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(6);
        }
        return btn;
    }

    private widget.Button createRoundedButton(String text, Color bg, Color fg, Color border) {
        widget.Button btn = new widget.Button();
        btn.setText(text);
        btn.setFont(FONT_SECTION);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new RoundedBorder(border, 8, 1.0f),
                new EmptyBorder(4, 12, 4, 12)));
        btn.setRolloverEnabled(true);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (bg.equals(COL_TEAL_PRIMARY)) {
                    btn.setBackground(COL_TEAL_DARK);
                } else if (bg.equals(Color.WHITE)) {
                    btn.setBackground(COL_BG_MAIN);
                } else {
                    btn.setBackground(bg.brighter());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private void aturUkuranTombol(AbstractButton btn, int minWidth) {
        if (btn == null)
            return;
        btn.setPreferredSize(null);
        Dimension d = btn.getPreferredSize();
        int targetW = Math.max(d.width + 18, minWidth);
        btn.setPreferredSize(new Dimension(targetW, 32));
        btn.revalidate();
    }

    // =========================================================================
    // KELAS KOMPONEN BORDER ROUNDED RESMI (STANDAR ARGON RM 20)
    // =========================================================================

    public static class RoundedBorder implements javax.swing.border.Border {
        private final Color color;
        private final int arc;
        private final float strokeWidth;

        public RoundedBorder(Color color, int arc, float strokeWidth) {
            this.color = color;
            this.arc = arc;
            this.strokeWidth = strokeWidth;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(strokeWidth));
            float offset = strokeWidth / 2.0f;
            g2.draw(new RoundRectangle2D.Float(
                    x + offset, y + offset,
                    width - strokeWidth, height - strokeWidth,
                    arc, arc));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int p = Math.max(4, (int) Math.ceil(strokeWidth) + 2);
            return new Insets(p, p, p, p);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    public static class RoundedTextBox extends widget.TextBox {
        private final int arc;
        private Color borderColor = COL_INPUT_BORDER;

        public RoundedTextBox(int maxLen, boolean editable) {
            this(maxLen, editable, 8);
        }

        public RoundedTextBox(int maxLen, boolean editable, int arc) {
            super();
            this.arc = arc;
            setEditable(editable);
            setOpaque(false);
            setFont(FONT_INPUT);
            setForeground(COL_TEXT_DARK);
            setBackground(editable ? Color.WHITE : new Color(248, 250, 252));
            setBorder(new EmptyBorder(5, 10, 5, 10));
            setPreferredSize(new Dimension(getPreferredSize().width, 28));
            putClientProperty("Component.arc", arc);
            putClientProperty("TextComponent.arc", arc);
            if (maxLen > 0) {
                setDocument(new batasInput((byte) maxLen).getKata(this));
            }
        }

        public void setBorderColor(Color c) {
            this.borderColor = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEditable() ? (getBackground() != null ? getBackground() : Color.WHITE)
                    : new Color(248, 250, 252));
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

    public static class RoundedCardPanel extends JPanel {
        private final int arc;
        private Color borderColor = COL_BORDER_DIVIDER;

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

            // 1. Gambar background rounded kartu
            g2.setColor(getBackground() != null ? getBackground() : COL_CARD_BG);
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));

            // 2. Set clipping rounded untuk anak komponen (header/body tidak bocor sudut)
            Graphics2D gChildren = (Graphics2D) g.create();
            gChildren.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gChildren.setClip(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));
            super.paint(gChildren);
            gChildren.dispose();

            // 3. Gambar border rounded di paling akhir di atas semua komponen
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth() - 1.2f, getHeight() - 1.2f, arc, arc));
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

    // =========================================================================
    // KOMPONEN KUSTOM: PANEL SCROLLABLE FULL-WIDTH AGAR BEBAS OVERFLOW HORIZONTAL
    // =========================================================================
    private static class ScrollableTrackWidthPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 100;
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

    public static class RoundedButton extends JButton {
        private final int arc;
        private Color normalBg;
        private Color borderCol;
        private boolean isHovered = false;

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
            setRolloverEnabled(true);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        private boolean isLightColor(Color c) {
            if (c == null)
                return true;
            return c.getRed() > 215 && c.getGreen() > 215 && c.getBlue() > 215;
        }

        private boolean isSoftTeal(Color c) {
            if (c == null)
                return false;
            return c.getRed() >= 230 && c.getGreen() >= 245 && c.getBlue() >= 240;
        }

        private boolean isRedTone(Color c) {
            if (c == null)
                return false;
            return c.getRed() > 180 && c.getGreen() < 120 && c.getBlue() < 130;
        }

        private boolean isTealTone(Color c) {
            if (c == null)
                return false;
            return c.getGreen() > 100 && (c.getBlue() > 100 || c.getRed() < 80);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = (isHovered || getModel().isRollover()) && isEnabled();
            if (!isEnabled()) {
                g2.setColor(new Color(241, 245, 249));
            } else if (getModel().isPressed()) {
                g2.setColor(normalBg != null ? normalBg.darker() : Color.LIGHT_GRAY);
            } else if (hover) {
                if (isSoftTeal(normalBg)) {
                    g2.setColor(new Color(204, 251, 241)); // Teal 100
                } else if (isLightColor(normalBg)) {
                    Color fg = getForeground();
                    if (isRedTone(fg) || (borderCol != null && isRedTone(borderCol))) {
                        g2.setColor(new Color(254, 242, 242)); // Rose 50
                    } else if (isTealTone(fg) || (borderCol != null && isTealTone(borderCol))) {
                        g2.setColor(new Color(240, 253, 250)); // Teal 50
                    } else {
                        g2.setColor(new Color(241, 245, 249)); // Slate 100
                    }
                } else {
                    g2.setColor(blendColor(normalBg, Color.WHITE, 0.22f));
                }
            } else {
                g2.setColor(normalBg != null ? normalBg : Color.WHITE);
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
                boolean hover = (isHovered || getModel().isRollover()) && isEnabled();
                if (!isEnabled()) {
                    g2.setColor(new Color(226, 232, 240));
                    g2.setStroke(new BasicStroke(1.0f));
                } else if (hover) {
                    Color fg = getForeground();
                    if (isSoftTeal(normalBg)) {
                        g2.setColor(new Color(13, 148, 136));
                        g2.setStroke(new BasicStroke(1.3f));
                    } else if (isRedTone(fg) || isRedTone(borderCol)) {
                        g2.setColor(new Color(248, 113, 113));
                        g2.setStroke(new BasicStroke(1.3f));
                    } else if (isTealTone(fg) || isTealTone(normalBg) || isTealTone(borderCol)) {
                        g2.setColor(new Color(13, 148, 136));
                        g2.setStroke(new BasicStroke(1.3f));
                    } else if (isLightColor(normalBg)) {
                        g2.setColor(new Color(148, 163, 184));
                        g2.setStroke(new BasicStroke(1.2f));
                    } else {
                        g2.setColor(blendColor(borderCol, Color.WHITE, 0.35f));
                        g2.setStroke(new BasicStroke(1.3f));
                    }
                } else {
                    g2.setColor(borderCol);
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));
                g2.dispose();
            }
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

    private void runBackground(Runnable task) {
        try {
            executor.execute(task);
        } catch (Exception ex) {
            SwingUtilities.invokeLater(task);
        }
    }

    private String nz(String val) {
        return (val == null) ? "" : val.trim();
    }

    private String nz(String val, String def) {
        return (val == null || val.trim().isEmpty()) ? def : val.trim();
    }

    // =========================================================================
    // DIALOG LOOKUP MASTER: DOKTER, PETUGAS & PENYAKIT (ATURAN BAKU KHANZA)
    // =========================================================================

    private void initLookupDialogs() {
        dialogDokter = new DlgCariDokter(null, false);
        dialogPetugas = new DlgCariPetugas(null, false);
        dialogPenyakit = new DlgCariPenyakit(null, false);
    }

    private void showDokterLookupDialog(widget.TextBox tfKd, widget.TextBox tfNm) {
        dialogDokter.isCek();
        dialogDokter.emptTeks();
        dialogDokter.setSize(Math.min(900, getWidth() - 40), Math.min(600, getHeight() - 40));
        dialogDokter.setLocationRelativeTo(this);
        dialogDokter.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialogDokter.getTable().getSelectedRow() != -1) {
                    tfKd.setText(
                            dialogDokter.getTable().getValueAt(dialogDokter.getTable().getSelectedRow(), 0).toString());
                    tfNm.setText(
                            dialogDokter.getTable().getValueAt(dialogDokter.getTable().getSelectedRow(), 1).toString());
                    updateProgress();
                }
            }
        });
        dialogDokter.setVisible(true);
    }

    private void showPetugasLookupDialog(widget.TextBox tfKd, widget.TextBox tfNm) {
        dialogPetugas.isCek();
        dialogPetugas.emptTeks();
        dialogPetugas.setSize(Math.min(900, getWidth() - 40), Math.min(600, getHeight() - 40));
        dialogPetugas.setLocationRelativeTo(this);
        dialogPetugas.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialogPetugas.getTable().getSelectedRow() != -1) {
                    tfKd.setText(dialogPetugas.getTable().getValueAt(dialogPetugas.getTable().getSelectedRow(), 0)
                            .toString());
                    tfNm.setText(dialogPetugas.getTable().getValueAt(dialogPetugas.getTable().getSelectedRow(), 1)
                            .toString());
                    updateProgress();
                }
            }
        });
        dialogPetugas.setVisible(true);
    }

    private void showPenyakitLookupDialog(widget.TextBox tfDiag) {
        dialogPenyakit.isCek();
        dialogPenyakit.emptTeks();
        dialogPenyakit.setSize(Math.min(900, getWidth() - 40), Math.min(600, getHeight() - 40));
        dialogPenyakit.setLocationRelativeTo(this);
        dialogPenyakit.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialogPenyakit.getTable().getSelectedRow() != -1) {
                    String kd = dialogPenyakit.getTable().getValueAt(dialogPenyakit.getTable().getSelectedRow(), 0)
                            .toString();
                    String nm = dialogPenyakit.getTable().getValueAt(dialogPenyakit.getTable().getSelectedRow(), 1)
                            .toString();
                    tfDiag.setText(kd + " - " + nm);
                    updateProgress();
                }
            }
        });
        dialogPenyakit.setVisible(true);
    }

    private void muatPilihanKamar(widget.ComboBox cmb) {
        cmb.removeAllItems();
        cmb.addItem(new KamarItem("- Pilih Kamar / Bangsal -", "", ""));
        try {
            PreparedStatement psKamar = koneksi.prepareStatement(
                    "select kamar.kd_kamar, bangsal.nm_bangsal, kamar.kelas " +
                            "from kamar inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal " +
                            "where kamar.statusdata='1' order by bangsal.nm_bangsal asc");
            try {
                ResultSet rsK = psKamar.executeQuery();
                while (rsK.next()) {
                    cmb.addItem(new KamarItem(
                            rsK.getString("nm_bangsal") + " (" + rsK.getString("kd_kamar") + ")",
                            rsK.getString("kd_kamar"),
                            rsK.getString("kelas")));
                }
                rsK.close();
            } finally {
                psKamar.close();
            }
        } catch (Exception ex) {
            System.out.println("Gagal memuat kamar: " + ex);
        }
    }

    // =========================================================================
    // METODE SIKLUS HIDUP & KONTRAK PUBLIK SIMRS KHANZA
    // =========================================================================

    public void isCek() {
        BtnSimpan.setEnabled(akses.gettransfer_pasien_antar_ruang());
        BtnHapus.setEnabled(akses.gettransfer_pasien_antar_ruang());
        BtnEdit.setEnabled(akses.gettransfer_pasien_antar_ruang());
        BtnPrint.setEnabled(akses.gettransfer_pasien_antar_ruang());
    }

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        isRawat();
        runBackground(this::tampil);
    }

    public void setTampil() {
        switchWorkspaceView("HISTORY");
    }

    private void isRawat() {
        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis, pasien.nm_pasien, if(pasien.jk='L','Laki-Laki','Perempuan') as jk, "
                            +
                            "pasien.tgl_lahir, reg_periksa.tgl_registrasi, kamar.kd_kamar, bangsal.nm_bangsal, kamar.kelas "
                            +
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                            "left join kamar_inap on reg_periksa.no_rawat=kamar_inap.no_rawat " +
                            "left join kamar on kamar_inap.kd_kamar=kamar.kd_kamar " +
                            "left join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal " +
                            "where reg_periksa.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1");
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
                    String kelasKamar = rs.getString("kelas");
                    String lokasi = (bangsal != null ? bangsal : "") + (kdKamar != null ? " (" + kdKamar + ")" : "");
                    if (lokasi.trim().isEmpty()) {
                        lokasi = "-";
                    }

                    if (AsalRuang.getText().trim().isEmpty() && !lokasi.equals("-")) {
                        AsalRuang.setText(lokasi);
                        if (kelasKamar != null && !kelasKamar.isEmpty()) {
                            KelasAsalRuang.setText(kelasKamar);
                            segKelasAsal.setSelectedValue(kelasKamar);
                        }
                    }

                    if (rs.getDate("tgl_registrasi") != null) {
                        lblValTglMasuk.setText(new SimpleDateFormat("dd-MM-yyyy").format(rs.getDate("tgl_registrasi")));
                    } else {
                        lblValTglMasuk.setText("-");
                    }

                    updatePatientCard();
                    updateProgress();
                }
            } finally {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif isRawat RM 04 : " + e);
        }
    }

    public void emptTeks() {
        TanggalMasuk.setSekarang();
        TanggalPindah.setSekarang();
        DiagnosaUtama.setText("");
        DiagnosaSekunder.setText("");
        NamaMenyetujui.setText("");
        HubunganMenyetujui.setSelectedIndex(0);
        TNoTlpMenyetujui.setText("");
        segPenjelasanDiagnosa.setSelectedValue("Ya");

        AsalRuang.setText("");
        KelasAsalRuang.setText("");
        RuangSelanjutnya.setText("");
        KelasRuangSelanjutnya.setText("");

        KdDokterMerawatAsal1.setText("");
        NmDokterMerawatAsal1.setText("");
        KdDokterMerawatAsal2.setText("");
        NmDokterMerawatAsal2.setText("");
        KdDokterMerawatAsal3.setText("");
        NmDokterMerawatAsal3.setText("");
        KdDokterMerawatTujuan1.setText("");
        NmDokterMerawatTujuan1.setText("");
        KdDokterMerawatTujuan2.setText("");
        NmDokterMerawatTujuan2.setText("");
        KdDokterMerawatTujuan3.setText("");
        NmDokterMerawatTujuan3.setText("");

        IndikasiPindah.setSelectedIndex(0);
        KeteranganIndikasiPindahRuang.setText("");
        segCaraPindah.setSelectedValue("Strecher");
        txtCaraPindahLain.setText("");

        ObservasiTerakhirPukul.setSekarang();
        segKesadaran.setSelectedValue("Compos Mentis");
        txtGcsE.setText("4");
        txtGcsM.setText("6");
        txtGcsV.setText("5");
        updateGcsCalculation();

        txtTdSistol.setText("");
        txtTdDiastol.setText("");
        txtHr.setText("");
        txtRr.setText("");
        txtTemp.setText("");
        txtSpo2.setText("");
        TingkatNyeriTransfer.setSelectedIndex(0);
        updateNyeriDisplay("0");

        segTherapyOksigen.setSelectedValue("Tidak");
        txtAliranOksigen.setText("");
        chkO2NasalKanule.setSelected(false);
        chkO2NasalKateter.setSelected(false);
        chkO2FaceMask.setSelected(false);
        chkO2SungkupRebreathing.setSelected(false);
        chkO2SungkupNonRebreathing.setSelected(false);
        chkO2Tracheostomy.setSelected(false);
        chkO2Ett.setSelected(false);

        segLineInvasif.setSelectedValue("IV Line");
        LineInvasifTanggal.setSekarang();
        txtProsedur1.setText("");
        txtTglProsedur1.setText("");
        txtProsedur2.setText("");
        txtTglProsedur2.setText("");
        txtProsedur3.setText("");
        txtTglProsedur3.setText("");

        chkPrecautionStandar.setSelected(true);
        chkPrecautionKontak.setSelected(false);
        chkPrecautionAirborne.setSelected(false);
        chkPrecautionDroplet.setSelected(false);

        segNutrisi.setSelectedValue("Oral");
        chkBatasanCairan.setSelected(false);
        TNutrisiBatasanCairanCc.setText("");
        chkDietKhusus.setSelected(false);
        TNutrisiDietKhususJelaskan.setText("");

        segBab.setSelectedValue("Normal");
        segBak.setSelectedValue("Normal");
        TEliminasiBakKateterNo.setText("");
        DTPEliminasiBakKateterTanggalPasang.setSekarang();

        segAktifitas.setSelectedValue("Dibantu Sebagian");
        segMobilisasi.setSelectedValue("Tirah Baring");

        chkDrainWsd.setSelected(false);
        chkDrainStoma.setSelected(false);
        chkDrainLuka.setSelected(false);
        chkDrainLain.setSelected(false);
        TDrainaseLainKet.setText("");

        chkLukaTidakAda.setSelected(true);
        for (int r = 0; r < 4; r++) {
            modelTabelLuka.setValueAt("", r, 1);
            modelTabelLuka.setValueAt("", r, 2);
            modelTabelLuka.setValueAt("", r, 3);
        }
        TLukaLainKet.setText("");

        txtHasilInvestigasiAbnormal.setText("");
        chkKhususJatuh.setSelected(false);
        chkKhususRestrain.setSelected(false);
        chkKhususRawatLuka.setSelected(false);
        chkKhususHygiene.setSelected(true);

        segRiwayatAlergi.setSelectedValue("Tidak");
        TJenisAlergi.setText("Tidak ada");
        TReaksiAlergi.setText("-");

        chkAlatTanpa.setSelected(true);
        chkAlatDengar.setSelected(false);
        chkAlatKacamata.setSelected(false);
        chkAlatGigiPalsu.setSelected(false);
        chkAlatTongkat.setSelected(false);
        TAlatBantuLainKet.setText("");

        txtTindakanSudahDilakukan.setText("");
        chkPenunjangLab.setSelected(false);
        TPenunjangLaboratoriumKet.setText("");
        chkPenunjangRad.setSelected(false);
        TPenunjangRadiologiKet.setText("");
        chkPenunjangCtScan.setSelected(false);
        TPenunjangCtScanKet.setText("");
        chkPenunjangLain.setSelected(false);
        TPenunjangLainKet.setText("");

        txtTherapy1.setText("");
        txtTherapy2.setText("");
        txtTherapy3.setText("");
        txtTherapy4.setText("");
        txtTherapy5.setText("");
        txtRtl1.setText("");
        txtRtl2.setText("");
        txtRtl3.setText("");
        txtMasalahKep1.setText("");
        txtMasalahKep2.setText("");
        txtMasalahKep3.setText("");
        txtDokumen1.setText("");
        txtDokumen2.setText("");
        txtDokumen3.setText("");
        txtDokumen4.setText("");
        txtDokumen5.setText("");

        KdPetugasMenyerahkan.setText("");
        NmPetugasMenyerahkan.setText("");
        KdPetugasMenerima.setText("");
        NmPetugasMenerima.setText("");

        resetStatusTteDisplay();
        updateProgress();
        updatePatientCard();
    }

    @Override
    public void dispose() {
        TanggalMasuk.stopTimer();
        TanggalPindah.stopTimer();
        ObservasiTerakhirPukul.stopTimer();
        LineInvasifTanggal.stopTimer();
        DTPEliminasiBakKateterTanggalPasang.stopTimer();
        executor.shutdown();
        super.dispose();
    }

    // =========================================================================
    // KELAS BANTU: SEGMENTED BUTTON, BARCODE PANEL, TANGGAL & JAM, SEARCH ICON
    // =========================================================================

    public static class SegmentedOptionButton extends JPanel {
        private final String[] options;
        private final List<PillSegmentButton> buttons = new ArrayList<>();
        private String selectedValue;
        private final List<java.util.function.Consumer<String>> listeners = new ArrayList<>();

        public SegmentedOptionButton(String[] options, String defaultOption) {
            this(options, defaultOption, options.length);
        }

        public SegmentedOptionButton(String[] options, String defaultOption, int cols) {
            this.options = options;
            this.selectedValue = defaultOption;
            int colCount = Math.max(1, cols);
            int rowCount = (int) Math.ceil((double) options.length / colCount);
            setLayout(new GridLayout(rowCount, colCount, 3, 3));
            setOpaque(true);
            setBackground(new Color(241, 245, 249)); // Slate 100
            setBorder(new CompoundBorder(
                    new RoundedBorder(COL_BORDER_DIVIDER, 8, 1.0f),
                    new EmptyBorder(2, 2, 2, 2)));

            for (String opt : options) {
                PillSegmentButton btn = new PillSegmentButton(opt);
                btn.setSelectedState(opt.equalsIgnoreCase(defaultOption));

                btn.addActionListener(e -> {
                    selectedValue = opt;
                    for (PillSegmentButton b : buttons) {
                        b.setSelectedState(b == btn);
                    }
                    for (java.util.function.Consumer<String> l : listeners) {
                        l.accept(opt);
                    }
                });

                buttons.add(btn);
                add(btn);
            }
        }

        public String getSelectedValue() {
            return selectedValue;
        }

        public void setSelectedValue(String val) {
            this.selectedValue = val;
            for (PillSegmentButton b : buttons) {
                boolean active = b.getText().equalsIgnoreCase(val);
                b.setSelectedState(active);
            }
        }

        public void addSelectionListener(java.util.function.Consumer<String> l) {
            listeners.add(l);
        }

        public static class PillSegmentButton extends JButton {
            private boolean isOptionSelected = false;
            private boolean isHovered = false;

            public PillSegmentButton(String text) {
                super(text);
                setFocusPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(new EmptyBorder(6, 12, 6, 12));
                setFont(FONT_INPUT);
                setForeground(new Color(71, 85, 105));
                setRolloverEnabled(true);
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (isEnabled() && !isOptionSelected) {
                            isHovered = true;
                            repaint();
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }

            public void setSelectedState(boolean sel) {
                this.isOptionSelected = sel;
                if (sel) {
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                    setForeground(Color.WHITE);
                } else {
                    setFont(FONT_INPUT);
                    setForeground(new Color(71, 85, 105));
                }
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isOptionSelected) {
                    String t = getText();
                    if ("Tidak Normal".equalsIgnoreCase(t) || "Koma".equalsIgnoreCase(t)) {
                        g2.setColor(COL_ROSE_PRIMARY);
                    } else {
                        g2.setColor(COL_TEAL_PRIMARY);
                    }
                    g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 6, 6));
                } else if (isHovered || getModel().isRollover()) {
                    g2.setColor(new Color(226, 232, 240));
                    g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 6, 6));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        }
    }

    public static class BarcodeTtePanel extends JPanel {
        private String barcodeCode;
        private int seed;

        public BarcodeTtePanel(String initialCode, int initialSeed) {
            this.barcodeCode = initialCode;
            this.seed = initialSeed;
            setPreferredSize(new Dimension(68, 68));
            setMinimumSize(new Dimension(68, 68));
            setBackground(Color.WHITE);
        }

        public void regenerateBarcode(String newCode) {
            this.barcodeCode = newCode;
            this.seed = newCode.hashCode();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            int size = Math.min(getWidth(), getHeight());
            int gridCount = 21;
            int cell = Math.max(3, (size - 6) / gridCount);
            int totalPx = cell * gridCount;
            int offsetX = (getWidth() - totalPx) / 2;
            int offsetY = (getHeight() - totalPx) / 2;

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(COL_TEXT_DARK);
            drawFinder(g2, offsetX, offsetY, cell);
            drawFinder(g2, offsetX + (gridCount - 7) * cell, offsetY, cell);
            drawFinder(g2, offsetX, offsetY + (gridCount - 7) * cell, cell);

            for (int i = 8; i < gridCount - 8; i += 2) {
                g2.fillRect(offsetX + i * cell, offsetY + 6 * cell, cell, cell);
                g2.fillRect(offsetX + 6 * cell, offsetY + i * cell, cell, cell);
            }

            Random rng = new Random(seed);
            for (int r = 0; r < gridCount; r++) {
                for (int c = 0; c < gridCount; c++) {
                    boolean inTopLeft = (r < 8 && c < 8);
                    boolean inTopRight = (r < 8 && c >= gridCount - 8);
                    boolean inBottomLeft = (r >= gridCount - 8 && c < 8);
                    boolean isTiming = (r == 6 || c == 6);
                    boolean inCenter = (r >= 9 && r <= 11 && c >= 9 && c <= 11);

                    if (!inTopLeft && !inTopRight && !inBottomLeft && !isTiming && !inCenter) {
                        if (rng.nextBoolean()) {
                            g2.fillRect(offsetX + c * cell, offsetY + r * cell, cell, cell);
                        }
                    }
                }
            }

            g2.setColor(COL_TEAL_DARK);
            g2.fillRect(offsetX + 9 * cell, offsetY + 9 * cell, 3 * cell, 3 * cell);
            g2.setColor(Color.WHITE);
            g2.fillRect(offsetX + 10 * cell, offsetY + 10 * cell, cell, cell);
            g2.dispose();
        }

        private void drawFinder(Graphics2D g2, int x, int y, int cell) {
            g2.setColor(COL_TEXT_DARK);
            g2.fillRect(x, y, 7 * cell, 7 * cell);
            g2.setColor(Color.WHITE);
            g2.fillRect(x + cell, y + cell, 5 * cell, 5 * cell);
            g2.setColor(COL_TEXT_DARK);
            g2.fillRect(x + 2 * cell, y + 2 * cell, 3 * cell, 3 * cell);
        }
    }

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
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
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

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable callback;

        public SimpleDocumentListener(Runnable callback) {
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

    /**
     * Komponen pemisah tanggal dan jam standar IGD SIMRS Khanza dengan styling
     * border rounded.
     */
    public static class PanelTanggalDanJam extends widget.PanelBiasa {
        public final widget.Tanggal tanggal;
        public final widget.ComboBox cmbJam;
        public final widget.ComboBox cmbMenit;
        public final widget.ComboBox cmbDetik;
        public final widget.CekBox chkJln;
        private javax.swing.Timer timer;

        public PanelTanggalDanJam(boolean adaTimer, int tinggi) {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
            setBorder(null);

            tanggal = new widget.Tanggal();
            tanggal.setDisplayFormat("dd-MM-yyyy");
            tanggal.setPreferredSize(new Dimension(120, tinggi));
            tanggal.setBackground(Color.WHITE);
            tanggal.putClientProperty("Component.arc", 8);
            tanggal.putClientProperty("ComboBox.arc", 8);
            tanggal.setBorder(new CompoundBorder(
                    new RoundedBorder(COL_INPUT_BORDER, 8, 1.0f),
                    new EmptyBorder(0, 8, 0, 8)));

            String[] arrJam = new String[24];
            for (int i = 0; i < 24; i++)
                arrJam[i] = (i < 10 ? "0" : "") + i;
            cmbJam = new widget.ComboBox();
            cmbJam.setModel(new javax.swing.DefaultComboBoxModel(arrJam));
            cmbJam.setPreferredSize(new Dimension(66, tinggi));
            cmbJam.setBackground(Color.WHITE);
            cmbJam.putClientProperty("Component.arc", 8);
            cmbJam.setBorder(new CompoundBorder(
                    new RoundedBorder(COL_INPUT_BORDER, 8, 1.0f),
                    new EmptyBorder(2, 4, 2, 4)));

            String[] arrMnt = new String[60];
            for (int i = 0; i < 60; i++)
                arrMnt[i] = (i < 10 ? "0" : "") + i;
            cmbMenit = new widget.ComboBox();
            cmbMenit.setModel(new javax.swing.DefaultComboBoxModel(arrMnt));
            cmbMenit.setPreferredSize(new Dimension(66, tinggi));
            cmbMenit.setBackground(Color.WHITE);
            cmbMenit.putClientProperty("Component.arc", 8);
            cmbMenit.setBorder(new CompoundBorder(
                    new RoundedBorder(COL_INPUT_BORDER, 8, 1.0f),
                    new EmptyBorder(2, 4, 2, 4)));

            cmbDetik = new widget.ComboBox();
            cmbDetik.setModel(new javax.swing.DefaultComboBoxModel(arrMnt));
            cmbDetik.setPreferredSize(new Dimension(66, tinggi));
            cmbDetik.setBackground(Color.WHITE);
            cmbDetik.putClientProperty("Component.arc", 8);
            cmbDetik.setBorder(new CompoundBorder(
                    new RoundedBorder(COL_INPUT_BORDER, 8, 1.0f),
                    new EmptyBorder(2, 4, 2, 4)));

            add(tanggal);
            add(cmbJam);
            JLabel sep1 = new JLabel(":");
            sep1.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 12));
            sep1.setPreferredSize(new Dimension(4, tinggi));
            add(sep1);
            add(cmbMenit);
            JLabel sep2 = new JLabel(":");
            sep2.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 12));
            sep2.setPreferredSize(new Dimension(4, tinggi));
            add(sep2);
            add(cmbDetik);

            if (adaTimer) {
                chkJln = new widget.CekBox();
                chkJln.setSelected(true);
                chkJln.setToolTipText("Centang agar jam berjalan otomatis");
                chkJln.setPreferredSize(new Dimension(24, tinggi));
                add(chkJln);

                timer = new javax.swing.Timer(1000, e -> {
                    if (chkJln.isSelected()) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        tanggal.setDate(cal.getTime());
                        cmbJam.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY)));
                        cmbMenit.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.MINUTE)));
                        cmbDetik.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.SECOND)));
                    }
                });
                timer.start();

                chkJln.addItemListener(e -> {
                    if (chkJln.isSelected())
                        setSekarang();
                });
            } else {
                chkJln = null;
            }

            setSekarang();
        }

        public void setSekarang() {
            tanggal.setDate(new Date());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cmbJam.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY)));
            cmbMenit.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.MINUTE)));
            cmbDetik.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.SECOND)));
            if (chkJln != null)
                chkJln.setSelected(true);
        }

        public void setDate(Date d) {
            if (d == null) {
                setSekarang();
                return;
            }
            tanggal.setDate(d);
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(d);
            cmbJam.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY)));
            cmbMenit.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.MINUTE)));
            cmbDetik.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.SECOND)));
            if (chkJln != null)
                chkJln.setSelected(false);
        }

        public Date getDate() {
            return tanggal.getDate();
        }

        public void setNilaiSql(String tglJamSql) {
            if (tglJamSql == null || tglJamSql.trim().isEmpty()) {
                setSekarang();
                return;
            }
            try {
                String bersih = tglJamSql.replaceAll("'", "").trim();
                if (bersih.length() >= 10) {
                    SimpleDateFormat sdfTgl = new SimpleDateFormat("yyyy-MM-dd");
                    tanggal.setDate(sdfTgl.parse(bersih.substring(0, 10)));
                }
                if (bersih.length() >= 19) {
                    cmbJam.setSelectedItem(bersih.substring(11, 13));
                    cmbMenit.setSelectedItem(bersih.substring(14, 16));
                    cmbDetik.setSelectedItem(bersih.substring(17, 19));
                } else if (bersih.length() >= 16) {
                    cmbJam.setSelectedItem(bersih.substring(11, 13));
                    cmbMenit.setSelectedItem(bersih.substring(14, 16));
                    cmbDetik.setSelectedItem("00");
                }
                if (chkJln != null)
                    chkJln.setSelected(false);
            } catch (Exception ex) {
                setSekarang();
            }
        }

        public String getTanggalSql(validasi valid) {
            return valid.SetTgl(tanggal.getSelectedItem() + "");
        }

        public String getJamSql() {
            return cmbJam.getSelectedItem() + ":" + cmbMenit.getSelectedItem() + ":" + cmbDetik.getSelectedItem();
        }

        public String getTanggalDanJamSql(validasi valid) {
            return getTanggalSql(valid) + " " + getJamSql();
        }

        public Object getSelectedItem() {
            return (tanggal.getSelectedItem() != null ? tanggal.getSelectedItem().toString() : "") + " " + getJamSql();
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (tanggal != null)
                tanggal.setEnabled(enabled);
            if (cmbJam != null)
                cmbJam.setEnabled(enabled);
            if (cmbMenit != null)
                cmbMenit.setEnabled(enabled);
            if (cmbDetik != null)
                cmbDetik.setEnabled(enabled);
            if (chkJln != null)
                chkJln.setEnabled(enabled);
        }

        public void stopTimer() {
            if (timer != null)
                timer.stop();
        }
    }
}
