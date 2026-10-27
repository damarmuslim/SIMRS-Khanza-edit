/*
 * Kontribusi RS Handayani Lampung / Redesain Argon RSUD Demang Sepulau Raya (RM 30-1A)
 */

package rekammedis;

import fungsi.WarnaTablePEWSD;
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
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
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
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import kepegawaian.DlgCariPetugas;
import widget.IconStyle;

/**
 * Early Warning Scoring System Dewasa - RM 30-1A
 * Standar Desain Argon RSUD Demang Sepulau Raya
 *
 * Fitur Utama:
 * 1. Satu Halaman Penuh Vertikal Scroll di Tab 1 (Input Parameter Fisiologis + Bagan Grafik 47 Baris).
 * 2. Sticky Side Rail Navigasi Kiri:
 *    - Kartu Identitas Pasien Putih Bersih di bagian paling atas.
 *    - Live Score Monitor EWS (Total Skor, Peringatan 1 Parameter Skor 3, Breakdown 8 Parameter, Instruksi Eskalasi).
 *    - Pemilih Slot Lembar Observasi (Grid 12 Kolom #1 s/d #12) & Navigasi Lompat ke Seksi.
 *    - Protokol Respon Klinis 5 Tingkat sesuai lembar fisik RM 30-1A.
 * 3. Matriks Bagan Grafik Observasi 47 Baris x 12 Kolom Berwarna (Putih=0, Kuning=1, Oranye=2, Merah=3).
 * 4. Dual-Pane Tab 2 (Riwayat Pemantauan): SplitPane 60:40 dengan tabel riwayat di kiri & Live Pratinjau HTML A4 di kanan.
 * 5. Integrasi Validasi TTE (`DlgOtentikasiTTE` & `pemantauan_pews_dewasa_konfirmasi`) + Cetak Jasper RM 30-1A.
 */
public final class RMPemantauanEWSD extends JDialog {

    // =========================================================================
    // PALET WARNA RESMI MEDICAL TEAL & SLATE (ARGON DESIGN SYSTEM)
    // =========================================================================
    public static final Color COL_HEADER_DARK = new Color(10, 46, 44);        // #0A2E2C
    public static final Color COL_TEAL_BADGE = new Color(17, 94, 89);         // #115E59
    public static final Color COL_TEAL_PRIMARY = new Color(13, 148, 136);     // #0D9488
    public static final Color COL_TEAL_DARK = new Color(15, 118, 110);        // #0F766E
    public static final Color COL_TEAL_SOFT = new Color(240, 253, 250);       // #F0FDFA
    public static final Color COL_TEAL_ACTIVE = new Color(224, 242, 241);     // #E0F2F1
    public static final Color COL_TEAL_BORDER = new Color(153, 246, 228);     // #99F6E4
    public static final Color COL_EMERALD_BG = new Color(209, 250, 229);      // #D1FAE5
    public static final Color COL_EMERALD_TXT = new Color(4, 120, 87);        // #047857
    public static final Color COL_BG_SLATE = new Color(241, 245, 249);        // #F1F5F9
    public static final Color COL_CARD_BG = Color.WHITE;                      // #FFFFFF
    public static final Color COL_CARD_HEADER = new Color(248, 250, 252);     // #F8FAFC
    public static final Color COL_BORDER = new Color(203, 213, 225);          // #CBD5E1
    public static final Color COL_BORDER_DIVIDER = new Color(226, 232, 240);  // #E2E8F0
    public static final Color COL_TEXT_DARK = new Color(15, 23, 42);          // #0F172A
    public static final Color COL_TEXT_MUTED = new Color(100, 116, 139);      // #64748B
    public static final Color COL_ROSE_PRIMARY = new Color(225, 29, 72);      // #E11D48
    public static final Color COL_ROSE_SOFT = new Color(255, 241, 242);       // #FFF1F2
    public static final Color COL_ROSE_BORDER = new Color(254, 205, 211);     // #FECDD3
    public static final Color COL_AMBER_SOFT = new Color(254, 252, 232);      // #FEFCE8
    public static final Color COL_AMBER_BORDER = new Color(253, 230, 138);    // #FDE68A

    // Warna Standar Grafik Fisik EWS RM 30-1A
    public static final Color EWS_RED = new Color(239, 68, 68);               // Skor 3 (#EF4444)
    public static final Color EWS_ORANGE = new Color(249, 115, 22);           // Skor 2 (#F97316)
    public static final Color EWS_YELLOW = new Color(253, 224, 71);           // Skor 1 (#FDE047)
    public static final Color EWS_WHITE = Color.WHITE;                        // Skor 0
    public static final Color EWS_BLUE = new Color(37, 99, 235);              // Henti Jantung (#2563EB)

    // Font Standar Argon
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_ROW_LABEL = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_SUBLABEL = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font FONT_MICRO_HEADER = new Font("Segoe UI", Font.BOLD, 9);
    public static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.BOLD, 11);

    // Konstanta Default Normal RM 30-1A
    private static final String NOL_RESPIRASI = "12 - 20";
    private static final String NOL_SATURASI = ">= 96";
    private static final String NOL_SUPLEMEN = "Tidak";
    private static final String NOL_SISTOLIK = "121 - 140";
    private static final String NOL_NADI = "71 - 80";
    private static final String NOL_KESADARAN = "Sadar";
    private static final String NOL_TEMPERATUR = "36.1 - 37.0";
    private static final int KOLOM_RM301A = 12;

    // --- Data & Koneksi ---
    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private DlgCariPetugas petugas;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private boolean isUpdatingFromCode = false;
    private Timer timerJam;

    // --- State Formulir Pemantauan & Mode Edit ---
    private JScrollPane scrollFormMain;
    private JPanel pnlFormSectionsList;
    private final Map<String, JPanel> mapSectionCards = new LinkedHashMap<>();
    private final Map<String, JButton> mapNavButtons = new LinkedHashMap<>();

    private JPanel pnlBannerEdit;
    private JLabel lblBannerEditNote;
    private JButton btnBatalEdit;
    private boolean isEditMode = false;
    private String editNoRawat = "";
    private String editTanggalSql = "";
    private int selectedSlotIndex = -1;

    private JLabel lblStatusSimpanInfo;
    private JLabel lblFooterHint;

    // --- Komponen Identitas Pasien & Form Legacy (dipertahankan untuk kompatibilitas) ---
    public widget.TextBox TNoRw = createTextBox(17, false);
    public widget.TextBox TNoRM = createTextBox(15, false);
    public widget.TextBox TPasien = createTextBox(40, false);
    public widget.TextBox Umur = createTextBox(15, false);
    public widget.TextBox JK = createTextBox(10, false);
    public widget.TextBox TglLahir = createTextBox(15, false);
    public widget.TextBox TanggalRegistrasi = createTextBox(25, false);

    // --- Parameter & Skor EWS (Komponen asli Khanza) ---
    public widget.Tanggal Tanggal;
    public widget.ComboBox Jam, Menit, Detik;
    public widget.CekBox ChkKejadian;
    public widget.ComboBox cmbSkor1, cmbSkor2, cmbSkor3, cmbSkor4, cmbSkor5, cmbSkor6, cmbSkor7;
    public widget.TextBox Skor1, Skor2, Skor3, Skor4, Skor5, Skor6, Skor7;
    public widget.TextBox TotalSkor, ParameterSkor;
    public widget.TextBox AliranOksigen, GulaDarah;
    public widget.ComboBox AlatOksigen;
    public widget.TextBox KdPetugas, NmPetugas, txtInisialPetugas;
    public widget.Button btnPetugas;

    // --- Field Input Numerik Cepat & Kalkulasi Live ---
    public widget.TextBox txtRR, txtSpO2, txtSuhu, txtSistol, txtNadi;
    private JLabel badgeRR, badgeSpO2, badgeO2, badgeSistol, badgeNadi, badgeAVPU, badgeSuhu;
    private SegmentedOptionButton segAVPU;
    private SegmentedOptionButton segSuplemenO2;
    private JCheckBox chkCodeBlue;
    private JLabel lblEditingSlotMode;

    // --- Sidebar UI Components ---
    private JLabel lblAvatar;
    private JLabel lblPasienNama, lblPasienJKUmur, lblPasienRM, lblPasienLahir, lblPasienMasuk, lblPasienRw, lblPasienLokasi;
    private JLabel lblSavedBadge;

    // Live Score Monitor Card Components
    private RoundedCardPanel liveScoreCard;
    private JPanel liveScoreHeader;
    private JLabel lblSkorTerkiniBesar;
    private JLabel lblBandTerkiniBadge;
    private JLabel lblSingleRedWarning;
    private JLabel lblLiveFreq;
    private JTextArea lblBandTerkiniAksi;
    private JLabel pillBreakRR, pillBreakSpO2, pillBreakO2, pillBreakSBP, pillBreakHR, pillBreakAVPU, pillBreakTemp, pillBreakGDS;

    // Slot Selector Grid (12 Kolom per Lembar)
    private JLabel lblKolomTerisiStatus;
    private final JButton[] slotButtons = new JButton[KOLOM_RM301A];
    private int activeSheetIndex = 0;
    private JLabel lblSheetTitle;
    private JButton btnPrevSheet;
    private JButton btnNextSheet;
    private final EWSMatrixCellRenderer matrixRenderer = new EWSMatrixCellRenderer();

    // --- Matriks Bagan Grafik Observasi 47 Baris ---
    private static class MatrixRowSpec {
        final String groupName;
        final String param;
        final String band;
        final String label;
        final String scoreText;
        final int colorCode; // 0=White, 1=Yellow, 2=Orange, 3=Red, 4=Header/Meta

        MatrixRowSpec(String groupName, String param, String band, String label, String scoreText, int colorCode) {
            this.groupName = groupName;
            this.param = param;
            this.band = band;
            this.label = label;
            this.scoreText = scoreText;
            this.colorCode = colorCode;
        }
    }

    private static class ObsColumnData {
        String tanggalSql;
        String tglPendek;
        String jamPendek;
        String rrBand;
        String rrSkor;
        String spo2Band;
        String spo2Skor;
        String o2Suplemen;
        String o2Skor;
        String sbpBand;
        String sbpSkor;
        String hrBand;
        String hrSkor;
        String avpuBand;
        String avpuSkor;
        String tempBand;
        String tempSkor;
        String totalSkor;
        String kesimpulan;
        String aliranO2;
        String alatO2;
        String gulaDarah;
        String nip;
        String namaPetugas;
        String inisial;
        String statusTte;
        String waktuTte;

        int getTotalInt() {
            try {
                return Integer.parseInt(totalSkor.trim());
            } catch (Exception e) {
                return 0;
            }
        }

        boolean hasSingle3() {
            return "3".equals(rrSkor) || "3".equals(spo2Skor) || "3".equals(o2Skor)
                    || "3".equals(sbpSkor) || "3".equals(hrSkor) || "3".equals(avpuSkor) || "3".equals(tempSkor);
        }
    }

    private final List<MatrixRowSpec> matrixRowSpecs = new ArrayList<>();
    private final List<ObsColumnData> activeObsColumns = new ArrayList<>();
    private DefaultTableModel matrixModel;
    private JTable matrixTable;

    // --- Legacy / Background Data Container ---
    public widget.Table tbObat;
    public widget.ScrollPane Scroll;
    public widget.TextBox TCari;
    public widget.Tanggal DTPCari1, DTPCari2;
    public widget.Button BtnCari, BtnAll;
    public widget.Label LCount;
    private JLabel lblActivePatientBanner;
    private JEditorPane LoadHTML;

    // --- Bottom Action Buttons ---
    public widget.Button BtnBaru, BtnSimpan, BtnHapus, BtnEdit, BtnPrint, BtnKeluar;
    private JButton btnCetakDokumen, btnSimpanForm, btnResetForm;

    // =========================================================================
    // KONSTRUKTOR & INISIALISASI UTAMA
    // =========================================================================
    public RMPemantauanEWSD(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Early Warning Scoring System Dewasa — RSUD Demang Sepulau Raya");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        initMatrixRowSpecifications();
        initComponentsCustom();
        initRM301A();

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.R.M.", "Nama Pasien", "Umur", "JK", "Tanggal",
            "Skor Respirasi", "Skor 1", "Skor Oksigen", "Skor 2",
            "Skor Suplemen Oksigen", "Skor 3", "Skor Tekanan Darah", "Skor 4",
            "Skor Laju Jantung", "Skor 5", "Skor Kesadaran", "Skor 6",
            "Skor Temperatur", "Skor 7", "Ttl.Skor", "Kesimpulan",
            "NIP", "Petugas", "Tgl.Lahir"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] colWidths = {
            105, 65, 160, 45, 25, 125,
            110, 40, 100, 40,
            110, 40, 110, 40,
            100, 40, 100, 40,
            100, 40, 55, 240,
            85, 140, 75
        };
        for (int i = 0; i < colWidths.length; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            column.setPreferredWidth(colWidths[i]);
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTablePEWSD());

        initLookupPetugas();
        initJamTimer();

        setMinimumSize(new Dimension(920, 560));
        Rectangle maxScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int targetW = Math.min(1440, maxScreen.width);
        int targetH = Math.min(860, maxScreen.height);
        setSize(targetW, targetH);
        setLocation(maxScreen.x + (maxScreen.width - targetW) / 2, maxScreen.y + (maxScreen.height - targetH) / 2);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        Rectangle maxScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int safeW = Math.min(width, maxScreen.width);
        int safeH = Math.min(height, maxScreen.height);
        int safeX = x;
        int safeY = y;

        if (safeY + safeH > maxScreen.y + maxScreen.height) {
            safeY = maxScreen.y + maxScreen.height - safeH;
        }
        if (safeY < maxScreen.y) {
            safeY = maxScreen.y;
            safeH = Math.min(safeH, maxScreen.height);
        }
        if (safeX + safeW > maxScreen.x + maxScreen.width) {
            safeX = maxScreen.x + maxScreen.width - safeW;
        }
        if (safeX < maxScreen.x) {
            safeX = maxScreen.x;
            safeW = Math.min(safeW, maxScreen.width);
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
            setBounds(getX(), getY(), getWidth(), getHeight());
        }
        super.setVisible(b);
    }

    private void inisialisasiTabelKonfirmasiTTE() {
        try {
            if (koneksi != null) {
                try (Statement stmt = koneksi.createStatement()) {
                    stmt.execute("CREATE TABLE IF NOT EXISTS pemantauan_pews_dewasa_konfirmasi ("
                            + " no_rawat varchar(17) NOT NULL,"
                            + " tanggal datetime NOT NULL,"
                            + " tgl_validasi_petugas datetime DEFAULT NULL,"
                            + " nip_petugas varchar(20) DEFAULT NULL,"
                            + " status_petugas varchar(30) DEFAULT 'Belum Validasi',"
                            + " tgl_validasi_dokter datetime DEFAULT NULL,"
                            + " kd_dokter varchar(20) DEFAULT NULL,"
                            + " status_dokter varchar(30) DEFAULT 'Belum Validasi',"
                            + " hash_tte varchar(64) DEFAULT NULL,"
                            + " PRIMARY KEY (no_rawat, tanggal)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi inisialisasi tabel TTE EWS Dewasa: " + e.getMessage());
        }
    }

    // =========================================================================
    // 1. DEFINISI 47 BARIS MATRIKS SESUAI BLANGKO FISIK RM 30-1A
    // =========================================================================
    private void initMatrixRowSpecifications() {
        matrixRowSpecs.clear();
        matrixRowSpecs.add(new MatrixRowSpec("WAKTU OBSERVASI", "meta_date", "Tanggal", "Tanggal (DD/MM)", "-", 4));
        matrixRowSpecs.add(new MatrixRowSpec("", "meta_time", "Waktu", "Waktu (Jam)", "-", 4));

        // 1. Laju Pernapasan (5 Baris)
        matrixRowSpecs.add(new MatrixRowSpec("Laju Pernapasan (x/mnt)", "rr", ">= 25", ">= 25", "3", 3));
        matrixRowSpecs.add(new MatrixRowSpec("", "rr", "21 - 24", "21 - 24", "2", 2));
        matrixRowSpecs.add(new MatrixRowSpec("", "rr", "12 - 20", "12 - 20", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "rr", "9 - 11", "9 - 10 (11)", "1", 1));
        matrixRowSpecs.add(new MatrixRowSpec("", "rr", "<= 8", "<= 8", "3", 3));

        // 2. Saturasi Oksigen (4 Baris)
        matrixRowSpecs.add(new MatrixRowSpec("Saturasi Oksigen (%)", "spo2", ">= 96", ">= 96", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "spo2", "94 - 95", "94 - 95", "1", 1));
        matrixRowSpecs.add(new MatrixRowSpec("", "spo2", "92 - 93", "92 - 93", "2", 2));
        matrixRowSpecs.add(new MatrixRowSpec("", "spo2", "<= 91", "<= 91", "3", 3));

        // 3. Alat Bantu Oksigen (2 Baris)
        matrixRowSpecs.add(new MatrixRowSpec("Alat Bantu Oksigen", "o2_lpm", "Aliran", "Aliran oksigen (L/menit)", "2", 2));
        matrixRowSpecs.add(new MatrixRowSpec("", "o2_dev", "Alat", "RM/NRM/NK/Tanpa Alat", "2 / 0", 0));

        // 4. Tekanan Darah Sistolik (12 Baris)
        matrixRowSpecs.add(new MatrixRowSpec("TD Sistolik (mmHg)", "sbp", ">= 220", ">= 220", "3", 3));
        matrixRowSpecs.add(new MatrixRowSpec("", "sbp", "201 - 219", "201 - 219", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "sbp", "181 - 200", "181 - 200", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "sbp", "161 - 180", "161 - 180", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "sbp", "141 - 160", "141 - 160", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "sbp", "121 - 140", "121 - 140", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "sbp", "111 - 120", "111 - 120", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "sbp", "101 - 110", "101 - 110", "1", 1));
        matrixRowSpecs.add(new MatrixRowSpec("", "sbp", "91 - 100", "91 - 100", "2", 2));
        matrixRowSpecs.add(new MatrixRowSpec("", "sbp", "81 - 90", "81 - 90", "3", 3));
        matrixRowSpecs.add(new MatrixRowSpec("", "sbp", "71 - 80", "71 - 80", "3", 3));
        matrixRowSpecs.add(new MatrixRowSpec("", "sbp", "<= 70", "<= 70", "3", 3));

        // 5. Nadi (11 Baris)
        matrixRowSpecs.add(new MatrixRowSpec("Nadi (x/menit)", "hr", ">= 131", ">= 131", "3", 3));
        matrixRowSpecs.add(new MatrixRowSpec("", "hr", "121 - 130", "121 - 130", "2", 2));
        matrixRowSpecs.add(new MatrixRowSpec("", "hr", "111 - 120", "111 - 120", "2", 2));
        matrixRowSpecs.add(new MatrixRowSpec("", "hr", "101 - 110", "101 - 110", "1", 1));
        matrixRowSpecs.add(new MatrixRowSpec("", "hr", "91 - 100", "91 - 100", "1", 1));
        matrixRowSpecs.add(new MatrixRowSpec("", "hr", "81 - 90", "81 - 90", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "hr", "71 - 80", "71 - 80", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "hr", "61 - 70", "61 - 70", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "hr", "51 - 60", "51 - 60", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "hr", "41 - 50", "41 - 50", "1", 1));
        matrixRowSpecs.add(new MatrixRowSpec("", "hr", "<= 40", "<= 40", "3", 3));

        // 6. Level Kesadaran (2 Baris)
        matrixRowSpecs.add(new MatrixRowSpec("Level Kesadaran", "avpu", "Sadar", "Sadar", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "avpu", "C/V/P/U", "C / V / P / U", "3", 3));

        // 7. Temperatur (6 Baris - Band DB memakai titik '.')
        matrixRowSpecs.add(new MatrixRowSpec("Temperatur (°C)", "temp", ">= 39.1", ">= 39,1", "2", 2));
        matrixRowSpecs.add(new MatrixRowSpec("", "temp", "38.1 - 39.0", "38,1 - 39,0", "1", 1));
        matrixRowSpecs.add(new MatrixRowSpec("", "temp", "37.1 - 38.0", "37,1 - 38,0", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "temp", "36.1 - 37.0", "36,1 - 37,0", "0", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "temp", "35.1 - 36.0", "35,1 - 36,0", "1", 1));
        matrixRowSpecs.add(new MatrixRowSpec("", "temp", "<= 35.0", "<= 35,0", "3", 3));

        // 8. TOTAL SKOR & PARAMETER TAMBAHAN (3 Baris)
        matrixRowSpecs.add(new MatrixRowSpec("TOTAL SKOR EWS", "meta_total", "TOTAL", "TOTAL SKOR", "TOTAL", 4));
        matrixRowSpecs.add(new MatrixRowSpec("PARAMETER TAMBAHAN", "meta_gds", "Gula Darah", "Gula Darah (mg/dL)", "mg/dL", 0));
        matrixRowSpecs.add(new MatrixRowSpec("", "meta_init", "Inisial", "Inisial Petugas", "Paraf", 0));
    }

    private void initRM301A() {
        cmbSkor1.setModel(new DefaultComboBoxModel<>(new String[]{">= 25", "21 - 24", "12 - 20", "9 - 11", "<= 8"}));
        cmbSkor2.setModel(new DefaultComboBoxModel<>(new String[]{">= 96", "94 - 95", "92 - 93", "<= 91"}));
        cmbSkor3.setModel(new DefaultComboBoxModel<>(new String[]{"Tidak", "Ya"}));
        cmbSkor4.setModel(new DefaultComboBoxModel<>(new String[]{
            ">= 220", "201 - 219", "181 - 200", "161 - 180", "141 - 160",
            "121 - 140", "111 - 120", "101 - 110", "91 - 100", "81 - 90", "71 - 80", "<= 70"
        }));
        cmbSkor5.setModel(new DefaultComboBoxModel<>(new String[]{
            ">= 131", "121 - 130", "111 - 120", "101 - 110", "91 - 100",
            "81 - 90", "71 - 80", "61 - 70", "51 - 60", "41 - 50", "<= 40"
        }));
        cmbSkor6.setModel(new DefaultComboBoxModel<>(new String[]{"Sadar", "C/V/P/U"}));
        cmbSkor7.setModel(new DefaultComboBoxModel<>(new String[]{
            ">= 39.1", "38.1 - 39.0", "37.1 - 38.0", "36.1 - 37.0", "35.1 - 36.0", "<= 35.0"
        }));
        AlatOksigen.setModel(new DefaultComboBoxModel<>(new String[]{"Tanpa Alat", "NK", "RM", "NRM"}));

        cmbSkor1.setSelectedItem(NOL_RESPIRASI);
        cmbSkor2.setSelectedItem(NOL_SATURASI);
        cmbSkor3.setSelectedItem(NOL_SUPLEMEN);
        cmbSkor4.setSelectedItem(NOL_SISTOLIK);
        cmbSkor5.setSelectedItem(NOL_NADI);
        cmbSkor6.setSelectedItem(NOL_KESADARAN);
        cmbSkor7.setSelectedItem(NOL_TEMPERATUR);
        AlatOksigen.setSelectedItem("Tanpa Alat");

        syncScoreFromCombo();
    }

    private void initLookupPetugas() {
        KdPetugas.setEditable(false);
        NmPetugas.setEditable(false);
        txtInisialPetugas.setEditable(false);

        NmPetugas.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                txtInisialPetugas.setText(getInisial(NmPetugas.getText()));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                txtInisialPetugas.setText(getInisial(NmPetugas.getText()));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                txtInisialPetugas.setText(getInisial(NmPetugas.getText()));
            }
        });
    }

    private void bukaLookupPetugas() {
        if (petugas == null) {
            Window parentWin = SwingUtilities.getWindowAncestor(this);
            java.awt.Frame frameParent = (parentWin instanceof java.awt.Frame) ? (java.awt.Frame) parentWin : null;
            petugas = new DlgCariPetugas(frameParent, false);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (petugas.getTable().getSelectedRow() != -1) {
                        KdPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                        NmPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                        txtInisialPetugas.setText(getInisial(NmPetugas.getText()));
                    }
                    btnPetugas.requestFocus();
                }
            });
        }
        akses.setform("RMPemantauanEWSD");
        petugas.emptTeks();
        petugas.isCek();
        petugas.setSize(this.getWidth() - 40, this.getHeight() - 40);
        petugas.setLocationRelativeTo(this);
        petugas.setVisible(true);
    }

    private void initJamTimer() {
        timerJam = new Timer(1000, e -> {
            if (ChkKejadian != null && ChkKejadian.isSelected() && !isEditMode) {
                Calendar cal = Calendar.getInstance();
                Jam.setSelectedItem(String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
                Menit.setSelectedItem(String.format("%02d", cal.get(Calendar.MINUTE)));
                Detik.setSelectedItem(String.format("%02d", cal.get(Calendar.SECOND)));
            }
        });
        timerJam.start();
    }

    // =========================================================================
    // 2. PERAKITAN TATA LETAK ANTARMUKA (TOP HEADER, WORKSPACE, SIDEBAR, FOOTER)
    // =========================================================================
    private void initComponentsCustom() {
        // Inisialisasi komponen dasar Khanza
        Tanggal = new widget.Tanggal();
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setPreferredSize(new Dimension(135, 28));
        styleTanggal(Tanggal);

        String[] arrJam = new String[24];
        for (int i = 0; i < 24; i++) arrJam[i] = String.format("%02d", i);
        String[] arrMnt = new String[60];
        for (int i = 0; i < 60; i++) arrMnt[i] = String.format("%02d", i);

        Jam = new widget.ComboBox();
        Jam.setModel(new DefaultComboBoxModel<>(arrJam));
        styleComboBox(Jam, 68);

        Menit = new widget.ComboBox();
        Menit.setModel(new DefaultComboBoxModel<>(arrMnt));
        styleComboBox(Menit, 68);

        Detik = new widget.ComboBox();
        Detik.setModel(new DefaultComboBoxModel<>(arrMnt));
        styleComboBox(Detik, 68);

        ChkKejadian = new widget.CekBox();
        ChkKejadian.setText("Jam Otomatis");
        ChkKejadian.setSelected(true);
        ChkKejadian.setFont(FONT_SUBLABEL);
        ChkKejadian.setForeground(COL_TEAL_DARK);
        ChkKejadian.setOpaque(false);

        cmbSkor1 = new widget.ComboBox();
        cmbSkor2 = new widget.ComboBox();
        cmbSkor3 = new widget.ComboBox();
        cmbSkor4 = new widget.ComboBox();
        cmbSkor5 = new widget.ComboBox();
        cmbSkor6 = new widget.ComboBox();
        cmbSkor7 = new widget.ComboBox();
        AlatOksigen = new widget.ComboBox();

        styleComboBox(cmbSkor1, 110);
        styleComboBox(cmbSkor2, 110);
        styleComboBox(cmbSkor3, 95);
        styleComboBox(cmbSkor4, 115);
        styleComboBox(cmbSkor5, 115);
        styleComboBox(cmbSkor6, 110);
        styleComboBox(cmbSkor7, 115);
        styleComboBox(AlatOksigen, 115);

        Skor1 = createTextBox(4, false);
        Skor2 = createTextBox(4, false);
        Skor3 = createTextBox(4, false);
        Skor4 = createTextBox(4, false);
        Skor5 = createTextBox(4, false);
        Skor6 = createTextBox(4, false);
        Skor7 = createTextBox(4, false);
        TotalSkor = createTextBox(6, false);
        ParameterSkor = createTextBox(250, false);

        AliranOksigen = createTextBox(10, true);
        GulaDarah = createTextBox(10, true);

        KdPetugas = createTextBox(20, false);
        NmPetugas = createTextBox(40, false);
        txtInisialPetugas = createTextBox(6, false);
        txtInisialPetugas.setHorizontalAlignment(SwingConstants.CENTER);
        txtInisialPetugas.setFont(FONT_MONO);

        btnPetugas = new widget.Button();
        btnPetugas.setText("...");
        btnPetugas.setToolTipText("Pilih Petugas dari Data Master Petugas");
        btnPetugas.addActionListener(e -> bukaLookupPetugas());

        txtRR = createTextBox(6, true);
        txtSpO2 = createTextBox(6, true);
        txtSistol = createTextBox(6, true);
        txtNadi = createTextBox(6, true);
        txtSuhu = createTextBox(6, true);

        chkCodeBlue = new JCheckBox("Henti Napas / Jantung");
        chkCodeBlue.setOpaque(false);
        chkCodeBlue.setFocusPainted(false);
        chkCodeBlue.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chkCodeBlue.setForeground(COL_ROSE_PRIMARY);
        chkCodeBlue.setFont(new Font("Segoe UI", Font.BOLD, 11));

        // Inisialisasi kompatibilitas tabel riwayat & filter (background data container)
        tbObat = new widget.Table();
        Scroll = new widget.ScrollPane();
        Scroll.setViewportView(tbObat);
        DTPCari1 = new widget.Tanggal();
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari2 = new widget.Tanggal();
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        TCari = createTextBox(100, true);
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        LCount = new widget.Label();
        LCount.setText("0");
        LoadHTML = new JEditorPane();
        LoadHTML.setContentType("text/html");
        LoadHTML.setEditable(false);

        // Compatibility buttons
        BtnBaru = new widget.Button();
        BtnSimpan = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();

        BtnBaru.addActionListener(e -> setModeInputBaru());
        BtnSimpan.addActionListener(e -> {
            if (isEditMode) ganti();
            else simpan();
        });
        BtnEdit.addActionListener(e -> ganti());
        BtnHapus.addActionListener(e -> hapus());
        BtnPrint.addActionListener(e -> cetakRM301A(TNoRw.getText().trim()));
        BtnKeluar.addActionListener(e -> dispose());

        // Pasang listener kalkulasi reaktif
        pasangListenersKalkulasi();

        // Rakit Root Layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(COL_BG_SLATE);

        // 1. Top Header & Sub-Navigation Bar
        getContentPane().add(buildTopHeader(), BorderLayout.NORTH);

        // 2. Main Workspace (Formulir Pemantauan & Bagan Observasi Terpadu Satu Lembar)
        getContentPane().add(buildFormulirWorkspace(), BorderLayout.CENTER);

        // 3. Bottom Sticky Action Bar
        getContentPane().add(buildBottomActionBar(), BorderLayout.SOUTH);
    }

    private JPanel buildTopHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());

        // Baris 1: Dark Medical Teal Header
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
                g2.setColor(COL_TEAL_DARK);
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

        JLabel title = new JLabel("Early Warning Scoring System Dewasa");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel badgeRm = new JLabel("RM 30-1A", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_TEAL_BADGE);
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

        JPanel rightHospital = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        rightHospital.setOpaque(false);
        JLabel lblHospital = new JLabel("RSUD Demang Sepulau Raya  \u2022  Lampung Tengah");
        lblHospital.setFont(FONT_SUBLABEL);
        lblHospital.setForeground(new Color(153, 246, 228));
        rightHospital.add(lblHospital);

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(rightHospital, BorderLayout.EAST);
        wrapper.add(topBar, BorderLayout.NORTH);

        // Baris 2: Sub-Toolbar (Tab Bersih Tanpa Kurung + Aksi Cepat)
        JPanel subToolbar = new JPanel(new BorderLayout());
        subToolbar.setBackground(Color.WHITE);
        subToolbar.setBorder(new MatteBorder(0, 0, 1, 0, COL_BORDER_DIVIDER));
        subToolbar.setPreferredSize(new Dimension(0, 42));

        JPanel leftSubtitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        leftSubtitle.setOpaque(false);
        JLabel lblDocTitle = new JLabel("Formulir Pemantauan & Bagan Observasi Terpadu");
        lblDocTitle.setFont(FONT_SECTION);
        lblDocTitle.setForeground(COL_TEAL_DARK);
        leftSubtitle.add(lblDocTitle);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        rightActions.setOpaque(false);

        lblStatusSimpanInfo = new JLabel("Siap mencatat observasi EWS");
        lblStatusSimpanInfo.setFont(FONT_SUBLABEL);
        lblStatusSimpanInfo.setForeground(COL_TEXT_MUTED);

        JButton btnPullTTV = createFlatButton("Tarik TTV Terakhir", IconStyle.muat("download.svg", 14, COL_TEAL_DARK), COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnPullTTV.setPreferredSize(new Dimension(150, 30));
        btnPullTTV.setToolTipText("Tarik otomatis Tanda Vital terakhir pasien dari CPPT / Pemeriksaan");
        btnPullTTV.addActionListener(e -> tarikDataTTVTerakhir());

        JButton btnDefaultNormal = createFlatButton("Isi Default Normal", null, COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnDefaultNormal.setPreferredSize(new Dimension(135, 30));
        btnDefaultNormal.addActionListener(e -> isiPresetNormal());

        JButton btnSimulasiKritis = createFlatButton("Simulasi Kasus Kritis", null, COL_ROSE_SOFT, COL_ROSE_PRIMARY, COL_ROSE_BORDER);
        btnSimulasiKritis.setPreferredSize(new Dimension(145, 30));
        btnSimulasiKritis.addActionListener(e -> isiPresetKritis());

        rightActions.add(lblStatusSimpanInfo);
        rightActions.add(btnPullTTV);
        rightActions.add(btnDefaultNormal);
        rightActions.add(btnSimulasiKritis);

        subToolbar.add(leftSubtitle, BorderLayout.WEST);
        subToolbar.add(rightActions, BorderLayout.EAST);
        wrapper.add(subToolbar, BorderLayout.CENTER);

        return wrapper;
    }

    // =========================================================================
    // 3. WORKSPACE 1: FORMULIR PEMANTAUAN (SIDEBAR KIRI + SCROLL KANAN)
    // =========================================================================
    private JPanel buildFormulirWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout(14, 0));
        workspace.setBackground(COL_BG_SLATE);
        workspace.setBorder(new EmptyBorder(12, 14, 12, 14));

        // A. Sticky Sidebar Navigasi Kiri (Lebar 315px)
        JPanel leftSidebar = buildLeftStickySidebar();
        JScrollPane sidebarScroll = new JScrollPane(leftSidebar);
        sidebarScroll.setBorder(null);
        sidebarScroll.setBackground(COL_BG_SLATE);
        sidebarScroll.getViewport().setBackground(COL_BG_SLATE);
        sidebarScroll.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
        sidebarScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(16);
        sidebarScroll.getVerticalScrollBar().addAdjustmentListener(e -> {
            sidebarScroll.repaint();
        });
        sidebarScroll.setPreferredSize(new Dimension(315, 0));
        workspace.add(sidebarScroll, BorderLayout.WEST);

        // B. Area Kanan Satu Halaman Penuh Vertikal Scroll
        pnlFormSectionsList = new ScrollableTrackWidthPanel();
        pnlFormSectionsList.setLayout(new BoxLayout(pnlFormSectionsList, BoxLayout.Y_AXIS));
        pnlFormSectionsList.setBackground(COL_BG_SLATE);
        pnlFormSectionsList.setOpaque(true);

        // Banner Mode Edit (Amber) di posisi paling atas
        pnlBannerEdit = buildEditModeBanner();
        pnlBannerEdit.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlBannerEdit.setMaximumSize(new Dimension(Short.MAX_VALUE, 48));
        pnlBannerEdit.setVisible(false);
        pnlFormSectionsList.add(pnlBannerEdit);
        pnlFormSectionsList.add(createFormVerticalSpacer(10));

        // Seksi 01: Input Parameter Fisiologis
        addSectionCard("01", buildSection01InputParameter());
        pnlFormSectionsList.add(createFormVerticalSpacer(14));

        // Seksi 02: Bagan Grafik Observasi EWS (47 Baris x 12 Kolom)
        addSectionCard("02", buildSection02BaganGrafikMatrix());
        pnlFormSectionsList.add(createFormVerticalSpacer(24));

        scrollFormMain = new JScrollPane(pnlFormSectionsList);
        scrollFormMain.setBorder(null);
        scrollFormMain.setBackground(COL_BG_SLATE);
        scrollFormMain.getViewport().setBackground(COL_BG_SLATE);
        scrollFormMain.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
        scrollFormMain.getVerticalScrollBar().setUnitIncrement(24);
        scrollFormMain.getVerticalScrollBar().addAdjustmentListener(e -> {
            scrollFormMain.repaint();
        });
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
                new RoundedBorder(new Color(245, 158, 11), 8, 1.3f),
                new EmptyBorder(8, 14, 8, 14)));

        lblBannerEditNote = new JLabel("MODE EDIT AKTIF: Anda sedang memperbarui data observasi EWS.", SwingConstants.LEFT);
        lblBannerEditNote.setFont(FONT_SECTION);
        lblBannerEditNote.setForeground(new Color(146, 64, 14));

        btnBatalEdit = createFlatButton("Batal Edit / Input Baru", null, Color.WHITE, COL_TEXT_DARK, COL_BORDER);
        btnBatalEdit.setPreferredSize(new Dimension(165, 30));
        btnBatalEdit.addActionListener(e -> setModeInputBaru());

        banner.add(lblBannerEditNote, BorderLayout.WEST);
        banner.add(btnBatalEdit, BorderLayout.EAST);
        return banner;
    }

    public void setModeEditAktif(String noRawat, String tanggalSql, int slotIdx) {
        this.isEditMode = true;
        this.editNoRawat = noRawat;
        this.editTanggalSql = tanggalSql;
        this.selectedSlotIndex = slotIdx;
        if (slotIdx >= 0) {
            this.activeSheetIndex = slotIdx / KOLOM_RM301A;
        }
        if (ChkKejadian != null) ChkKejadian.setSelected(false);

        String infoKolom = (slotIdx >= 0) ? "Kolom #" + (slotIdx + 1) + " (Lembar " + (activeSheetIndex + 1) + ")" : "Data Riwayat";
        lblBannerEditNote.setText("MODE EDIT AKTIF: Memperbarui " + infoKolom + " pada waktu " + tanggalSql + " (No. Rawat: " + noRawat + ")");
        lblEditingSlotMode.setText("Mode: Edit " + infoKolom + " (" + (tanggalSql.length() >= 16 ? tanggalSql.substring(11, 16) : tanggalSql) + ")");
        lblEditingSlotMode.setBackground(COL_AMBER_SOFT);
        lblEditingSlotMode.setForeground(new Color(180, 83, 9));
        pnlBannerEdit.setVisible(true);
        if (btnSimpanForm != null) {
            btnSimpanForm.setText("Simpan Perubahan");
        }
        updateSlotGridHighlight();
        if (scrollFormMain != null) {
            scrollFormMain.getViewport().setViewPosition(new Point(0, 0));
        }
        if (matrixTable != null) matrixTable.repaint();
    }

    public void setModeInputBaru() {
        this.isEditMode = false;
        this.editNoRawat = "";
        this.editTanggalSql = "";
        this.selectedSlotIndex = -1;
        if (ChkKejadian != null) ChkKejadian.setSelected(true);
        if (pnlBannerEdit != null) pnlBannerEdit.setVisible(false);

        int nextSlot = activeObsColumns.size() + 1;
        int nextSheet = activeObsColumns.size() / KOLOM_RM301A;
        this.activeSheetIndex = nextSheet;

        if (lblEditingSlotMode != null) {
            lblEditingSlotMode.setText("Mode: Input Kolom #" + nextSlot + " (Lembar " + (nextSheet + 1) + ")");
            lblEditingSlotMode.setBackground(COL_TEAL_SOFT);
            lblEditingSlotMode.setForeground(COL_TEAL_DARK);
        }
        if (btnSimpanForm != null) {
            btnSimpanForm.setText("Simpan Observasi");
        }
        emptTeks();
        updateSlotGridHighlight();
        if (matrixTable != null) matrixTable.repaint();
    }

    // =========================================================================
    // 4. STICKY SIDEBAR KIRI (IDENTITAS PASIEN + LIVE SCORE + SLOT 12 + PROTOKOL)
    // =========================================================================
    private JPanel buildLeftStickySidebar() {
        JPanel sidebar = new ScrollableTrackWidthPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COL_BG_SLATE);
        sidebar.setOpaque(true);

        // --- CARD 1: IDENTITAS PASIEN (Clean White sesuai Argon) ---
        RoundedCardPanel patCard = new RoundedCardPanel(12);
        patCard.setLayout(new BorderLayout());
        patCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel patHdr = new JPanel(new BorderLayout());
        patHdr.setBackground(COL_CARD_HEADER);
        patHdr.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER_DIVIDER),
                new EmptyBorder(8, 12, 8, 12)));

        JLabel lblIdTitle = new JLabel("IDENTITAS PASIEN");
        lblIdTitle.setFont(FONT_MICRO_HEADER);
        lblIdTitle.setForeground(COL_TEXT_MUTED);

        lblSavedBadge = createPillBadge("Tersimpan", COL_EMERALD_BG, COL_EMERALD_TXT);

        patHdr.add(lblIdTitle, BorderLayout.WEST);
        patHdr.add(lblSavedBadge, BorderLayout.EAST);

        JPanel patBody = new JPanel();
        patBody.setLayout(new BoxLayout(patBody, BoxLayout.Y_AXIS));
        patBody.setOpaque(false);
        patBody.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel avatarRow = new JPanel(new BorderLayout(10, 0));
        avatarRow.setOpaque(false);
        avatarRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblAvatar = new JLabel("PS", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_TEAL_SOFT);
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 10, 10));
                g2.setColor(COL_TEAL_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAvatar.setPreferredSize(new Dimension(42, 42));
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvatar.setForeground(COL_TEAL_DARK);
        lblAvatar.setOpaque(false);

        JPanel nameBox = new JPanel(new GridLayout(2, 1, 0, 1));
        nameBox.setOpaque(false);
        lblPasienNama = new JLabel("-");
        lblPasienNama.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPasienNama.setForeground(COL_TEXT_DARK);
        lblPasienJKUmur = new JLabel("-");
        lblPasienJKUmur.setFont(FONT_SUBLABEL);
        lblPasienJKUmur.setForeground(COL_TEXT_MUTED);
        nameBox.add(lblPasienNama);
        nameBox.add(lblPasienJKUmur);

        avatarRow.add(lblAvatar, BorderLayout.WEST);
        avatarRow.add(nameBox, BorderLayout.CENTER);

        lblPasienRM = new JLabel("-");
        lblPasienLahir = new JLabel("-");
        lblPasienMasuk = new JLabel("-");
        lblPasienRw = new JLabel("-");
        lblPasienLokasi = new JLabel("-");

        patBody.add(avatarRow);
        patBody.add(Box.createVerticalStrut(8));
        patBody.add(new JSeparator());
        patBody.add(Box.createVerticalStrut(6));
        patBody.add(createKeyValueRow("No. RM", lblPasienRM, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("Tgl Lahir / Umur", lblPasienLahir, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("Tgl Masuk RS", lblPasienMasuk, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("No. Rawat", lblPasienRw, true));
        patBody.add(Box.createVerticalStrut(4));
        patBody.add(createKeyValueRow("Kamar / Ruang", lblPasienLokasi, false));

        patCard.add(patHdr, BorderLayout.NORTH);
        patCard.add(patBody, BorderLayout.CENTER);
        patCard.setMaximumSize(new Dimension(Short.MAX_VALUE, 245));

        // --- CARD 2: SKOR EWS SAAT INI (LIVE SCORE MONITOR) ---
        liveScoreCard = new RoundedCardPanel(12);
        liveScoreCard.setLayout(new BorderLayout());
        liveScoreCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        liveScoreHeader = new JPanel(new BorderLayout());
        liveScoreHeader.setBackground(COL_TEAL_DARK);
        liveScoreHeader.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblScoreHdrTitle = new JLabel("SKOR EWS SAAT INI");
        lblScoreHdrTitle.setFont(FONT_MICRO_HEADER);
        lblScoreHdrTitle.setForeground(Color.WHITE);

        lblBandTerkiniBadge = new JLabel("NORMAL");
        lblBandTerkiniBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBandTerkiniBadge.setForeground(Color.WHITE);

        liveScoreHeader.add(lblScoreHdrTitle, BorderLayout.WEST);
        liveScoreHeader.add(lblBandTerkiniBadge, BorderLayout.EAST);

        JPanel scoreBody = new JPanel();
        scoreBody.setLayout(new BoxLayout(scoreBody, BoxLayout.Y_AXIS));
        scoreBody.setOpaque(false);
        scoreBody.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel topScoreRow = new JPanel(new BorderLayout(8, 0));
        topScoreRow.setOpaque(false);
        topScoreRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bigNumBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        bigNumBox.setOpaque(false);
        lblSkorTerkiniBesar = new JLabel("0");
        lblSkorTerkiniBesar.setFont(new Font("Consolas", Font.BOLD, 34));
        lblSkorTerkiniBesar.setForeground(COL_TEXT_DARK);
        JLabel lblMaxPts = new JLabel("/ 20 poin");
        lblMaxPts.setFont(FONT_SUBLABEL);
        lblMaxPts.setForeground(COL_TEXT_MUTED);
        bigNumBox.add(lblSkorTerkiniBesar);
        bigNumBox.add(lblMaxPts);

        lblSingleRedWarning = createPillBadge("! 1 Param Skor 3", new Color(255, 237, 213), new Color(194, 65, 12));
        lblSingleRedWarning.setVisible(false);

        topScoreRow.add(bigNumBox, BorderLayout.WEST);
        topScoreRow.add(lblSingleRedWarning, BorderLayout.EAST);

        // Mini Breakdown 8 Parameter
        JPanel breakdownGrid = new JPanel(new GridLayout(2, 4, 4, 4));
        breakdownGrid.setOpaque(false);
        breakdownGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        pillBreakRR = createMiniBreakPill("RR: 0");
        pillBreakSpO2 = createMiniBreakPill("SpO2: 0");
        pillBreakO2 = createMiniBreakPill("O2: 0");
        pillBreakSBP = createMiniBreakPill("TD: 0");
        pillBreakHR = createMiniBreakPill("Nadi: 0");
        pillBreakAVPU = createMiniBreakPill("AVPU: 0");
        pillBreakTemp = createMiniBreakPill("Suhu: 0");
        pillBreakGDS = createMiniBreakPill("GDS: -");

        breakdownGrid.add(pillBreakRR);
        breakdownGrid.add(pillBreakSpO2);
        breakdownGrid.add(pillBreakO2);
        breakdownGrid.add(pillBreakSBP);
        breakdownGrid.add(pillBreakHR);
        breakdownGrid.add(pillBreakAVPU);
        breakdownGrid.add(pillBreakTemp);
        breakdownGrid.add(pillBreakGDS);

        // Kotak Instruksi Eskalasi
        JPanel protoBox = new JPanel(new BorderLayout(0, 3));
        protoBox.setBackground(COL_CARD_HEADER);
        protoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        protoBox.setBorder(new CompoundBorder(
                new RoundedBorder(COL_BORDER_DIVIDER, 8, 1.0f),
                new EmptyBorder(6, 8, 6, 8)));

        lblLiveFreq = new JLabel("Frekuensi Monitoring: Tiap 8 Jam");
        lblLiveFreq.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblLiveFreq.setForeground(COL_TEAL_DARK);

        lblBandTerkiniAksi = new JTextArea("Monitor rutin per 8 jam.");
        lblBandTerkiniAksi.setWrapStyleWord(true);
        lblBandTerkiniAksi.setLineWrap(true);
        lblBandTerkiniAksi.setEditable(false);
        lblBandTerkiniAksi.setOpaque(false);
        lblBandTerkiniAksi.setFont(FONT_SUBLABEL);
        lblBandTerkiniAksi.setForeground(COL_TEXT_DARK);
        lblBandTerkiniAksi.setRows(3);

        protoBox.add(lblLiveFreq, BorderLayout.NORTH);
        protoBox.add(lblBandTerkiniAksi, BorderLayout.CENTER);

        scoreBody.add(topScoreRow);
        scoreBody.add(Box.createVerticalStrut(6));
        scoreBody.add(breakdownGrid);
        scoreBody.add(Box.createVerticalStrut(8));
        scoreBody.add(protoBox);

        liveScoreCard.add(liveScoreHeader, BorderLayout.NORTH);
        liveScoreCard.add(scoreBody, BorderLayout.CENTER);
        liveScoreCard.setMaximumSize(new Dimension(Short.MAX_VALUE, 235));

        // --- CARD 3: SLOT LEMBAR OBSERVASI (12 KOLOM PER LEMBAR) & LOMPAT SEKSI ---
        RoundedCardPanel slotCard = new RoundedCardPanel(12);
        slotCard.setLayout(new BorderLayout());
        slotCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel slotHdr = new JPanel(new BorderLayout());
        slotHdr.setBackground(COL_CARD_HEADER);
        slotHdr.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER_DIVIDER),
                new EmptyBorder(7, 12, 7, 12)));
        JLabel lblSlotTitle = new JLabel("SLOT OBSERVASI");
        lblSlotTitle.setFont(FONT_MICRO_HEADER);
        lblSlotTitle.setForeground(COL_TEXT_MUTED);
        lblKolomTerisiStatus = new JLabel("0 Terisi (Lbr 1: 0/12)");
        lblKolomTerisiStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblKolomTerisiStatus.setForeground(COL_TEAL_DARK);
        slotHdr.add(lblSlotTitle, BorderLayout.WEST);
        slotHdr.add(lblKolomTerisiStatus, BorderLayout.EAST);

        JPanel slotBody = new JPanel();
        slotBody.setLayout(new BoxLayout(slotBody, BoxLayout.Y_AXIS));
        slotBody.setOpaque(false);
        slotBody.setBorder(new EmptyBorder(8, 10, 8, 10));

        // Paginator Navigasi Lembar (12 Kolom per Lembar)
        JPanel sheetNavRow = new JPanel(new BorderLayout(4, 0));
        sheetNavRow.setOpaque(false);
        sheetNavRow.setBorder(new EmptyBorder(0, 0, 6, 0));
        sheetNavRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnPrevSheet = createFlatButton("◀", null, Color.WHITE, COL_TEXT_DARK, COL_BORDER);
        btnPrevSheet.setPreferredSize(new Dimension(28, 22));
        btnPrevSheet.setToolTipText("Lihat Lembar Sebelumnya (12 Kolom)");
        btnPrevSheet.setEnabled(false);
        btnPrevSheet.addActionListener(e -> {
            if (activeSheetIndex > 0) {
                activeSheetIndex--;
                updateSlotGridHighlight();
            }
        });

        lblSheetTitle = new JLabel("Lembar 1 (Kolom 1 - 12)", SwingConstants.CENTER);
        lblSheetTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSheetTitle.setForeground(COL_HEADER_DARK);

        btnNextSheet = createFlatButton("▶", null, Color.WHITE, COL_TEXT_DARK, COL_BORDER);
        btnNextSheet.setPreferredSize(new Dimension(28, 22));
        btnNextSheet.setToolTipText("Lihat Lembar Berikutnya (12 Kolom)");
        btnNextSheet.setEnabled(false);
        btnNextSheet.addActionListener(e -> {
            int maxSheet = Math.max(0, activeObsColumns.size() / KOLOM_RM301A);
            if (activeSheetIndex < maxSheet) {
                activeSheetIndex++;
                updateSlotGridHighlight();
            }
        });

        sheetNavRow.add(btnPrevSheet, BorderLayout.WEST);
        sheetNavRow.add(lblSheetTitle, BorderLayout.CENTER);
        sheetNavRow.add(btnNextSheet, BorderLayout.EAST);

        JPanel grid12 = new JPanel(new GridLayout(2, 6, 4, 4));
        grid12.setOpaque(false);
        grid12.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < KOLOM_RM301A; i++) {
            final int slotPos = i;
            JButton bSlot = new JButton("#" + (i + 1));
            bSlot.setFont(new Font("Consolas", Font.BOLD, 10));
            bSlot.setFocusPainted(false);
            bSlot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            bSlot.setMargin(new Insets(2, 2, 2, 2));
            bSlot.setPreferredSize(new Dimension(42, 34));
            bSlot.setToolTipText("Klik untuk memilih/mengedit Kolom Observasi");
            bSlot.addActionListener(e -> pilihSlotObservasi(activeSheetIndex * KOLOM_RM301A + slotPos));
            slotButtons[i] = bSlot;
            grid12.add(bSlot);
        }

        // Navigasi Lompat ke Seksi
        JPanel navJumpRow = new JPanel(new GridLayout(1, 2, 6, 0));
        navJumpRow.setOpaque(false);
        navJumpRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnJump01 = createFlatButton("01. Input EWS", null, COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnJump01.setPreferredSize(new Dimension(120, 26));
        btnJump01.addActionListener(e -> scrollToSection("01"));
        mapNavButtons.put("01", btnJump01);

        JButton btnJump02 = createFlatButton("02. Bagan Grafik", null, Color.WHITE, COL_TEXT_DARK, COL_BORDER);
        btnJump02.setPreferredSize(new Dimension(120, 26));
        btnJump02.addActionListener(e -> scrollToSection("02"));
        mapNavButtons.put("02", btnJump02);

        navJumpRow.add(btnJump01);
        navJumpRow.add(btnJump02);

        slotBody.add(sheetNavRow);
        slotBody.add(grid12);
        slotBody.add(Box.createVerticalStrut(8));
        slotBody.add(navJumpRow);

        slotCard.add(slotHdr, BorderLayout.NORTH);
        slotCard.add(slotBody, BorderLayout.CENTER);
        slotCard.setMaximumSize(new Dimension(Short.MAX_VALUE, 185));

        // --- CARD 4: PROTOKOL RESPON KLINIS 5 TINGKAT ---
        RoundedCardPanel legendCard = new RoundedCardPanel(12);
        legendCard.setLayout(new BorderLayout());
        legendCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel legHdr = new JPanel(new BorderLayout());
        legHdr.setBackground(COL_CARD_HEADER);
        legHdr.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER_DIVIDER),
                new EmptyBorder(7, 12, 7, 12)));
        JLabel lblLegTitle = new JLabel("PROTOKOL RESPON KLINIS");
        lblLegTitle.setFont(FONT_MICRO_HEADER);
        lblLegTitle.setForeground(COL_TEXT_MUTED);
        legHdr.add(lblLegTitle, BorderLayout.WEST);

        JPanel legList = new JPanel(new GridLayout(5, 1, 0, 4));
        legList.setOpaque(false);
        legList.setBorder(new EmptyBorder(8, 10, 8, 10));

        legList.add(createLegendRow("SKOR 0", "Monitor rutin per 8 jam", EWS_WHITE, COL_TEXT_DARK));
        legList.add(createLegendRow("1 - 4", "Perawat senior, tiap 4-6 jam", EWS_YELLOW, COL_TEXT_DARK));
        legList.add(createLegendRow("5 - 6 / 3*", "Dokter jaga <5 mnt, tiap 1 jam", EWS_ORANGE, Color.WHITE));
        legList.add(createLegendRow(">= 7", "Resusitasi kontinyu & TMRC", EWS_RED, Color.WHITE));
        legList.add(createLegendRow("HENTI", "RJP & Tim Henti Jantung (118)", EWS_BLUE, Color.WHITE));

        legendCard.add(legHdr, BorderLayout.NORTH);
        legendCard.add(legList, BorderLayout.CENTER);
        legendCard.setMaximumSize(new Dimension(Short.MAX_VALUE, 195));

        sidebar.add(patCard);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(liveScoreCard);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(slotCard);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(legendCard);
        sidebar.add(Box.createVerticalGlue());

        updateSlotGridHighlight();
        return sidebar;
    }

    public void scrollToSection(String code) {
        JPanel target = mapSectionCards.get(code);
        if (target != null && scrollFormMain != null) {
            Rectangle bounds = target.getBounds();
            scrollFormMain.getViewport().setViewPosition(new Point(0, Math.max(0, bounds.y - 8)));
        }
    }

    // =========================================================================
    // 5. SEKSI 01: INPUT PARAMETER FISIOLOGIS (STANDAR ARGON RM 30-1A)
    // =========================================================================
    private JPanel buildSection01InputParameter() {
        RoundedCardPanel card = new RoundedCardPanel(12);
        card.setLayout(new BorderLayout());

        // Header Card 01 (Judul Bersih Tanpa Kurung sesuai Aturan 3)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COL_CARD_HEADER);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER_DIVIDER),
                new EmptyBorder(9, 14, 9, 14)));

        JPanel leftHdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHdr.setOpaque(false);

        JLabel numBadge = createNumberBadge("01");
        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 1));
        titleStack.setOpaque(false);
        JLabel lblTitle = new JLabel("INPUT PARAMETER FISIOLOGIS");
        lblTitle.setFont(FONT_SECTION);
        lblTitle.setForeground(COL_TEXT_DARK);
        JLabel lblSub = new JLabel("Ketik nilai tanda vital atau pilih rentang untuk menghitung skor EWS otomatis");
        lblSub.setFont(FONT_SUBLABEL);
        lblSub.setForeground(COL_TEXT_MUTED);
        titleStack.add(lblTitle);
        titleStack.add(lblSub);

        leftHdr.add(numBadge);
        leftHdr.add(titleStack);

        JPanel rightHdr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightHdr.setOpaque(false);

        lblEditingSlotMode = createPillBadge("Mode: Input Kolom Baru", COL_TEAL_SOFT, COL_TEAL_DARK);
        JButton btnSlotBaru = createFlatButton("+ Kolom Baru", null, Color.WHITE, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnSlotBaru.setPreferredSize(new Dimension(105, 26));
        btnSlotBaru.addActionListener(e -> setModeInputBaru());

        rightHdr.add(lblEditingSlotMode);
        rightHdr.add(btnSlotBaru);

        header.add(leftHdr, BorderLayout.WEST);
        header.add(rightHdr, BorderLayout.EAST);

        // Body Card 01
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 14, 12, 14));

        // Baris A: Konteks Waktu Observasi & Petugas Pengkaji (Master Lookup DlgCariPetugas)
        JPanel rowKonteks = new JPanel(new GridLayout(1, 2, 12, 0));
        rowKonteks.setOpaque(false);
        rowKonteks.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Kotak Waktu Observasi
        RoundedCardPanel boxWaktu = new RoundedCardPanel(8);
        boxWaktu.setBackground(COL_CARD_HEADER);
        boxWaktu.setLayout(new BorderLayout(4, 4));
        boxWaktu.setBorder(new EmptyBorder(6, 10, 6, 10));

        JPanel hdrWaktu = new JPanel(new BorderLayout());
        hdrWaktu.setOpaque(false);
        JLabel lblWaktuTitle = new JLabel("WAKTU OBSERVASI (TANGGAL & JAM)");
        lblWaktuTitle.setFont(FONT_MICRO_HEADER);
        lblWaktuTitle.setForeground(COL_TEXT_MUTED);
        hdrWaktu.add(lblWaktuTitle, BorderLayout.WEST);
        hdrWaktu.add(ChkKejadian, BorderLayout.EAST);

        JPanel pnlTanggalJam = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlTanggalJam.setOpaque(false);
        pnlTanggalJam.add(Tanggal);
        pnlTanggalJam.add(Jam);
        pnlTanggalJam.add(new JLabel(":"));
        pnlTanggalJam.add(Menit);
        pnlTanggalJam.add(new JLabel(":"));
        pnlTanggalJam.add(Detik);

        JButton btnNow = createFlatButton("Sekarang", null, COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnNow.setPreferredSize(new Dimension(78, 26));
        btnNow.addActionListener(e -> {
            Tanggal.setDate(new Date());
            Calendar cal = Calendar.getInstance();
            Jam.setSelectedItem(String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
            Menit.setSelectedItem(String.format("%02d", cal.get(Calendar.MINUTE)));
            Detik.setSelectedItem(String.format("%02d", cal.get(Calendar.SECOND)));
        });
        pnlTanggalJam.add(btnNow);

        boxWaktu.add(hdrWaktu, BorderLayout.NORTH);
        boxWaktu.add(pnlTanggalJam, BorderLayout.CENTER);

        // Kotak Petugas / Perawat (Wajib Master Lookup DlgCariPetugas)
        RoundedCardPanel boxPetugas = new RoundedCardPanel(8);
        boxPetugas.setBackground(COL_CARD_HEADER);
        boxPetugas.setLayout(new BorderLayout(4, 4));
        boxPetugas.setBorder(new EmptyBorder(6, 10, 6, 10));

        JPanel hdrPetugas = new JPanel(new BorderLayout());
        hdrPetugas.setOpaque(false);
        JLabel lblPetugasTitle = new JLabel("PETUGAS / PERAWAT PENGKAJI & INISIAL");
        lblPetugasTitle.setFont(FONT_MICRO_HEADER);
        lblPetugasTitle.setForeground(COL_TEXT_MUTED);
        JLabel lblMasterHint = new JLabel("Master Petugas");
        lblMasterHint.setFont(FONT_SUBLABEL);
        lblMasterHint.setForeground(COL_TEAL_DARK);
        hdrPetugas.add(lblPetugasTitle, BorderLayout.WEST);
        hdrPetugas.add(lblMasterHint, BorderLayout.EAST);

        JPanel pnlPetugasLookup = new JPanel(new BorderLayout(6, 0));
        pnlPetugasLookup.setOpaque(false);
        KdPetugas.setPreferredSize(new Dimension(95, 28));
        txtInisialPetugas.setPreferredSize(new Dimension(48, 28));
        txtInisialPetugas.setToolTipText("Inisial Petugas otomatis untuk baris Paraf di Lembar RM 30-1A");

        JPanel lookupCenter = createLookupFieldPanel(NmPetugas, this::bukaLookupPetugas);
        pnlPetugasLookup.add(KdPetugas, BorderLayout.WEST);
        pnlPetugasLookup.add(lookupCenter, BorderLayout.CENTER);
        pnlPetugasLookup.add(txtInisialPetugas, BorderLayout.EAST);

        boxPetugas.add(hdrPetugas, BorderLayout.NORTH);
        boxPetugas.add(pnlPetugasLookup, BorderLayout.CENTER);

        rowKonteks.add(boxWaktu);
        rowKonteks.add(boxPetugas);

        // Baris B: Grid 8 Kartu Parameter Fisiologis (2 Baris x 4 Kolom)
        JPanel gridParams = new JPanel(new GridLayout(2, 4, 10, 10));
        gridParams.setOpaque(false);
        gridParams.setAlignmentX(Component.LEFT_ALIGNMENT);

        badgeRR = createScoreBadge();
        badgeSpO2 = createScoreBadge();
        badgeO2 = createScoreBadge();
        badgeSistol = createScoreBadge();
        badgeNadi = createScoreBadge();
        badgeAVPU = createScoreBadge();
        badgeSuhu = createScoreBadge();

        // 1. Laju Pernapasan
        gridParams.add(createParameterCard(
                "1. Laju Pernapasan",
                badgeRR,
                buildNumericAndBandRow(txtRR, "x/mnt", cmbSkor1)
        ));

        // 2. Saturasi Oksigen
        gridParams.add(createParameterCard(
                "2. Saturasi Oksigen",
                badgeSpO2,
                buildNumericAndBandRow(txtSpO2, "%", cmbSkor2)
        ));

        // 3. Alat Bantu Oksigen
        segSuplemenO2 = new SegmentedOptionButton(new String[]{"Tidak", "Ya"}, "Tidak");
        segSuplemenO2.addSelectionListener(val -> {
            if (!isUpdatingFromCode) {
                isUpdatingFromCode = true;
                cmbSkor3.setSelectedItem(val);
                if ("Tidak".equals(val)) {
                    AlatOksigen.setSelectedItem("Tanpa Alat");
                    AliranOksigen.setText("");
                } else if ("Tanpa Alat".equals(AlatOksigen.getSelectedItem())) {
                    AlatOksigen.setSelectedItem("NK");
                }
                isUpdatingFromCode = false;
                syncScoreFromCombo();
            }
        });
        JPanel pnlO2Content = new JPanel(new GridLayout(2, 1, 0, 4));
        pnlO2Content.setOpaque(false);
        JPanel o2RowTop = new JPanel(new BorderLayout(4, 0));
        o2RowTop.setOpaque(false);
        o2RowTop.add(segSuplemenO2, BorderLayout.WEST);
        o2RowTop.add(AlatOksigen, BorderLayout.CENTER);
        JPanel o2RowBot = new JPanel(new BorderLayout(4, 0));
        o2RowBot.setOpaque(false);
        JLabel lblLpm = new JLabel("Aliran O2:");
        lblLpm.setFont(FONT_SUBLABEL);
        lblLpm.setForeground(COL_TEXT_MUTED);
        JLabel lblUnitLpm = new JLabel("L/mnt");
        lblUnitLpm.setFont(FONT_SUBLABEL);
        lblUnitLpm.setForeground(COL_TEXT_MUTED);
        o2RowBot.add(lblLpm, BorderLayout.WEST);
        o2RowBot.add(AliranOksigen, BorderLayout.CENTER);
        o2RowBot.add(lblUnitLpm, BorderLayout.EAST);
        pnlO2Content.add(o2RowTop);
        pnlO2Content.add(o2RowBot);
        gridParams.add(createParameterCard("3. Alat Bantu Oksigen", badgeO2, pnlO2Content));

        // 4. TD Sistolik
        gridParams.add(createParameterCard(
                "4. TD Sistolik",
                badgeSistol,
                buildNumericAndBandRow(txtSistol, "mmHg", cmbSkor4)
        ));

        // 5. Frekuensi Nadi
        gridParams.add(createParameterCard(
                "5. Frekuensi Nadi",
                badgeNadi,
                buildNumericAndBandRow(txtNadi, "x/mnt", cmbSkor5)
        ));

        // 6. Level Kesadaran (AVPU)
        segAVPU = new SegmentedOptionButton(new String[]{"Sadar", "C/V/P/U"}, "Sadar");
        segAVPU.addSelectionListener(val -> {
            if (!isUpdatingFromCode) {
                isUpdatingFromCode = true;
                cmbSkor6.setSelectedItem(val);
                isUpdatingFromCode = false;
                syncScoreFromCombo();
            }
        });
        JPanel pnlAvpuContent = new JPanel(new GridLayout(2, 1, 0, 4));
        pnlAvpuContent.setOpaque(false);
        pnlAvpuContent.add(segAVPU);
        JLabel lblAvpuKet = new JLabel("Alert = 0  |  Confused / Voice / Pain / Unresp = 3");
        lblAvpuKet.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblAvpuKet.setForeground(COL_TEXT_MUTED);
        pnlAvpuContent.add(lblAvpuKet);
        gridParams.add(createParameterCard("6. Level Kesadaran", badgeAVPU, pnlAvpuContent));

        // 7. Temperatur
        gridParams.add(createParameterCard(
                "7. Temperatur",
                badgeSuhu,
                buildNumericAndBandRow(txtSuhu, "°C", cmbSkor7)
        ));

        // 8. Parameter Tambahan (Gula Darah & Status Henti Jantung)
        JPanel pnlExtraContent = new JPanel(new GridLayout(2, 1, 0, 4));
        pnlExtraContent.setOpaque(false);
        JPanel gdsRow = new JPanel(new BorderLayout(4, 0));
        gdsRow.setOpaque(false);
        JLabel lblGds = new JLabel("Gula Darah:");
        lblGds.setFont(FONT_SUBLABEL);
        lblGds.setForeground(COL_TEXT_MUTED);
        JLabel lblGdsUnit = new JLabel("mg/dL");
        lblGdsUnit.setFont(FONT_SUBLABEL);
        lblGdsUnit.setForeground(COL_TEXT_MUTED);
        gdsRow.add(lblGds, BorderLayout.WEST);
        gdsRow.add(GulaDarah, BorderLayout.CENTER);
        gdsRow.add(lblGdsUnit, BorderLayout.EAST);
        pnlExtraContent.add(gdsRow);
        pnlExtraContent.add(chkCodeBlue);
        JLabel badgeExtra = createPillBadge("Tambahan", COL_BG_SLATE, COL_TEXT_MUTED);
        gridParams.add(createParameterCard("8. Parameter Tambahan", badgeExtra, pnlExtraContent));

        body.add(rowKonteks);
        body.add(Box.createVerticalStrut(10));
        body.add(gridParams);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildNumericAndBandRow(widget.TextBox txtNum, String unitText, widget.ComboBox cmbBand) {
        JPanel wrapper = new JPanel(new GridLayout(2, 1, 0, 4));
        wrapper.setOpaque(false);

        JPanel numRow = new JPanel(new BorderLayout(4, 0));
        numRow.setOpaque(false);
        JLabel lblKetik = new JLabel("Nilai:");
        lblKetik.setFont(FONT_SUBLABEL);
        lblKetik.setForeground(COL_TEXT_MUTED);
        JLabel lblUnit = new JLabel(unitText);
        lblUnit.setFont(FONT_SUBLABEL);
        lblUnit.setForeground(COL_TEXT_MUTED);
        numRow.add(lblKetik, BorderLayout.WEST);
        numRow.add(txtNum, BorderLayout.CENTER);
        numRow.add(lblUnit, BorderLayout.EAST);

        JPanel bandRow = new JPanel(new BorderLayout(4, 0));
        bandRow.setOpaque(false);
        JLabel lblPita = new JLabel("Pita:");
        lblPita.setFont(FONT_SUBLABEL);
        lblPita.setForeground(COL_TEXT_MUTED);
        bandRow.add(lblPita, BorderLayout.WEST);
        bandRow.add(cmbBand, BorderLayout.CENTER);

        wrapper.add(numRow);
        wrapper.add(bandRow);
        return wrapper;
    }

    private RoundedCardPanel createParameterCard(String title, JLabel scoreBadge, JComponent content) {
        RoundedCardPanel box = new RoundedCardPanel(8);
        box.setBackground(COL_CARD_HEADER);
        box.setLayout(new BorderLayout(0, 6));
        box.setBorder(new EmptyBorder(7, 10, 8, 10));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_ROW_LABEL);
        lbl.setForeground(COL_TEXT_DARK);
        hdr.add(lbl, BorderLayout.WEST);
        if (scoreBadge != null) {
            hdr.add(scoreBadge, BorderLayout.EAST);
        }

        box.add(hdr, BorderLayout.NORTH);
        box.add(content, BorderLayout.CENTER);
        return box;
    }

    // =========================================================================
    // 6. SEKSI 02: BAGAN GRAFIK OBSERVASI EWS (47 BARIS x 12 KOLOM BERWARNA)
    // =========================================================================
    private JPanel buildSection02BaganGrafikMatrix() {
        RoundedCardPanel card = new RoundedCardPanel(12);
        card.setLayout(new BorderLayout());

        // Header Card 02 (Judul Bersih Tanpa Kurung sesuai Aturan 3)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COL_CARD_HEADER);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER_DIVIDER),
                new EmptyBorder(9, 14, 9, 14)));

        JPanel leftHdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHdr.setOpaque(false);

        JLabel numBadge = createNumberBadge("02");
        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 1));
        titleStack.setOpaque(false);
        JLabel lblTitle = new JLabel("BAGAN GRAFIK OBSERVASI EWS");
        lblTitle.setFont(FONT_SECTION);
        lblTitle.setForeground(COL_TEXT_DARK);
        JLabel lblSub = new JLabel("Replika presisi 47 baris lembar fisik RM 30-1A — Klik kolom #1 s/d #12 untuk memuat atau mengedit observasi");
        lblSub.setFont(FONT_SUBLABEL);
        lblSub.setForeground(COL_TEXT_MUTED);
        titleStack.add(lblTitle);
        titleStack.add(lblSub);

        leftHdr.add(numBadge);
        leftHdr.add(titleStack);

        JPanel rightHdr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightHdr.setOpaque(false);

        JButton btnPreviewHtml = createFlatButton("Pratinjau Lembar F4", null, COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnPreviewHtml.setPreferredSize(new Dimension(145, 28));
        btnPreviewHtml.addActionListener(e -> bukaDialogPreviewFullscreen(TNoRw.getText().trim()));

        JButton btnPrintJasper = createFlatButton("Cetak RM 30-1A", IconStyle.muat("printer.svg", 14, Color.WHITE), COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnPrintJasper.setPreferredSize(new Dimension(135, 28));
        btnPrintJasper.addActionListener(e -> cetakRM301A(TNoRw.getText().trim()));

        rightHdr.add(btnPreviewHtml);
        rightHdr.add(btnPrintJasper);

        header.add(leftHdr, BorderLayout.WEST);
        header.add(rightHdr, BorderLayout.EAST);

        // Tabel Matriks Kolom Dinamis: PARAMETER, RENTANG NILAI, #1..#N, SKOR
        String[] cols = new String[15];
        cols[0] = "PARAMETER";
        cols[1] = "RENTANG NILAI";
        for (int i = 0; i < KOLOM_RM301A; i++) {
            cols[i + 2] = "#" + (i + 1);
        }
        cols[14] = "SKOR";

        matrixModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        matrixTable = new JTable(matrixModel) {
            @Override
            public void scrollRectToVisible(Rectangle aRect) {
                // Kunci: Jangan pernah perbolehkan auto-scroll vertikal internal di dalam matrixScroll.
                // Seluruh 47 baris selalu tampil vertikal utuh dari y = 0 sampai y = totalHeight.
                // Hanya izinkan scrolling horizontal (x dan width) di matrixScroll.
                Rectangle r = new Rectangle(aRect.x, 0, aRect.width, getHeight());
                super.scrollRectToVisible(r);
            }
        };
        matrixTable.setRowHeight(21);
        matrixTable.setFont(new Font("Consolas", Font.PLAIN, 11));
        matrixTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        matrixTable.getTableHeader().setBackground(COL_HEADER_DARK);
        matrixTable.getTableHeader().setForeground(Color.WHITE);
        matrixTable.getTableHeader().setReorderingAllowed(false);
        matrixTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        matrixTable.setCellSelectionEnabled(true);
        matrixTable.setShowGrid(true);
        matrixTable.setGridColor(new Color(203, 213, 225));
        matrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int c = 0; c < matrixTable.getColumnCount(); c++) {
            matrixTable.getColumnModel().getColumn(c).setCellRenderer(matrixRenderer);
        }
        matrixTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        matrixTable.getColumnModel().getColumn(1).setPreferredWidth(125);
        for (int i = 0; i < KOLOM_RM301A; i++) {
            matrixTable.getColumnModel().getColumn(i + 2).setPreferredWidth(58);
        }
        matrixTable.getColumnModel().getColumn(14).setPreferredWidth(52);

        matrixTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = matrixTable.getSelectedColumn();
                int totalCols = matrixTable.getColumnCount();
                if (col >= 2 && col < totalCols - 1) {
                    int idx = col - 2;
                    if (idx < activeObsColumns.size()) {
                        pilihSlotObservasi(idx);
                    }
                }
            }
        });
        if (matrixTable.getTableHeader() != null) {
            matrixTable.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            matrixTable.getTableHeader().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int col = matrixTable.columnAtPoint(e.getPoint());
                    int totalCols = matrixTable.getColumnCount();
                    if (col >= 2 && col < totalCols - 1) {
                        int idx = col - 2;
                        if (idx < activeObsColumns.size()) {
                            pilihSlotObservasi(idx);
                        }
                    }
                }
            });
        }

        // Isi baris awal kosong agar 47 baris langsung tampil rapi
        refreshMatrixRows();

        JScrollPane matrixScroll = new JScrollPane(matrixTable);
        matrixScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        matrixScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        matrixScroll.getViewport().setBackground(Color.WHITE);
        matrixScroll.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
        matrixScroll.setBorder(BorderFactory.createEmptyBorder());
        matrixScroll.setPreferredSize(new Dimension(800, 1030));
        matrixScroll.setWheelScrollingEnabled(false);

        // Kunci posisi vertikal viewport matrix selalu 0 agar tidak pernah bergeser/terpotong
        matrixScroll.getViewport().addChangeListener(e -> {
            Point p = matrixScroll.getViewport().getViewPosition();
            if (p.y != 0) {
                matrixScroll.getViewport().setViewPosition(new Point(p.x, 0));
            }
        });

        // Teruskan event mouse wheel vertikal ke parent scrollFormMain agar scroll tidak nyangkut/macet
        MouseWheelListener wheelForwarder = e -> {
            if (e.isShiftDown()) {
                JScrollBar hsb = matrixScroll.getHorizontalScrollBar();
                if (hsb != null && hsb.isVisible()) {
                    int amount = e.getUnitsToScroll() * 16;
                    hsb.setValue(hsb.getValue() + amount);
                }
            } else if (scrollFormMain != null) {
                scrollFormMain.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, scrollFormMain));
            }
        };
        matrixTable.addMouseWheelListener(wheelForwarder);
        if (matrixTable.getTableHeader() != null) {
            matrixTable.getTableHeader().addMouseWheelListener(wheelForwarder);
        }
        matrixScroll.addMouseWheelListener(wheelForwarder);

        JPanel matrixContainer = new JPanel(new BorderLayout());
        matrixContainer.setOpaque(true);
        matrixContainer.setBackground(Color.WHITE);
        matrixContainer.setBorder(new RoundedBorder(COL_BORDER, 8, 1.0f));
        matrixContainer.add(matrixScroll, BorderLayout.CENTER);
        matrixContainer.addMouseWheelListener(wheelForwarder);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 14, 14, 14));
        body.add(matrixContainer, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private class EWSMatrixCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (row < 0 || row >= matrixRowSpecs.size()) {
                return lbl;
            }
            MatrixRowSpec spec = matrixRowSpecs.get(row);
            String text = value != null ? value.toString() : "";
            int totalCols = table.getColumnCount();
            int lastObsCol = totalCols - 2;

            lbl.setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
            lbl.setFont(new Font("Consolas", text.isEmpty() ? Font.PLAIN : Font.BOLD, 11));

            if (col == 0) {
                lbl.setBackground(new Color(241, 245, 249));
                lbl.setForeground(COL_HEADER_DARK);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lbl.setBorder(new CompoundBorder(
                        new LineBorder(new Color(203, 213, 225), 1),
                        new EmptyBorder(0, 6, 0, 4)));
                return lbl;
            }

            if ("meta_total".equals(spec.param) && col >= 2 && col <= lastObsCol) {
                int idx = col - 2;
                if (idx < activeObsColumns.size()) {
                    ObsColumnData obs = activeObsColumns.get(idx);
                    int tot = obs.getTotalInt();
                    if (tot >= 7) {
                        lbl.setBackground(EWS_RED);
                        lbl.setForeground(Color.WHITE);
                    } else if (tot >= 5 || obs.hasSingle3()) {
                        lbl.setBackground(EWS_ORANGE);
                        lbl.setForeground(Color.WHITE);
                    } else if (tot >= 1) {
                        lbl.setBackground(EWS_YELLOW);
                        lbl.setForeground(COL_TEXT_DARK);
                    } else {
                        lbl.setBackground(Color.WHITE);
                        lbl.setForeground(COL_TEXT_DARK);
                    }
                    lbl.setBorder(new LineBorder(COL_HEADER_DARK, idx == selectedSlotIndex ? 2 : 1));
                    return lbl;
                }
            }

            switch (spec.colorCode) {
                case 3:
                    lbl.setBackground(EWS_RED);
                    lbl.setForeground(Color.WHITE);
                    break;
                case 2:
                    lbl.setBackground(EWS_ORANGE);
                    lbl.setForeground(Color.WHITE);
                    break;
                case 1:
                    lbl.setBackground(EWS_YELLOW);
                    lbl.setForeground(COL_TEXT_DARK);
                    break;
                case 4:
                    lbl.setBackground(new Color(226, 232, 240));
                    lbl.setForeground(COL_HEADER_DARK);
                    break;
                default:
                    lbl.setBackground(EWS_WHITE);
                    lbl.setForeground(COL_HEADER_DARK);
                    break;
            }

            boolean isSelectedCol = (col >= 2 && col <= lastObsCol && (col - 2) == selectedSlotIndex);
            if (col >= 2 && col <= lastObsCol && !text.isEmpty() && spec.colorCode <= 3) {
                lbl.setBorder(new LineBorder(COL_HEADER_DARK, 2));
            } else if (isSelectedCol) {
                lbl.setBorder(new MatteBorder(0, 2, 0, 2, COL_TEAL_PRIMARY));
            } else {
                lbl.setBorder(new LineBorder(new Color(203, 213, 225), 1));
            }
            return lbl;
        }
    }



    // =========================================================================
    // 8. DIALOG PRATINJAU DOKUMEN LAYAR PENUH & GENERATOR HTML A4 RM 30-1A
    // =========================================================================
    public void bukaDialogPreviewFullscreen(String noRawat) {
        String targetRw = (noRawat == null || noRawat.trim().isEmpty()) ? TNoRw.getText().trim() : noRawat.trim();
        if (targetRw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih data pasien terlebih dahulu!");
            return;
        }

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parentWin, "Pratinjau Cetak Resmi RM 30-1A — " + TPasien.getText(),
                Dialog.ModalityType.APPLICATION_MODAL);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        dlg.setSize(Math.min(1140, screen.width - 40), Math.min(860, screen.height - 50));
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(COL_HEADER_DARK);
        topBar.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel lblTitle = new JLabel("PRATINJAU LEMBAR OBSERVASI EWS DEWASA (RM 30-1A) — NO. RAWAT: " + targetRw);
        lblTitle.setFont(FONT_SECTION);
        lblTitle.setForeground(Color.WHITE);

        JEditorPane editorFullscreen = new JEditorPane();
        editorFullscreen.setContentType("text/html");
        editorFullscreen.setEditable(false);
        editorFullscreen.setText(buildOfficialPrintHTML());
        editorFullscreen.setCaretPosition(0);

        JPanel pnlAksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlAksi.setOpaque(false);

        JButton btnBrowser = createFlatButton("Buka di Browser", null, COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnBrowser.setPreferredSize(new Dimension(125, 28));
        btnBrowser.addActionListener(e -> exportAndOpenHTMLInBrowser());

        JButton btnPrintHtml = createFlatButton("Cetak HTML (F4)", null, COL_TEAL_BADGE, Color.WHITE, COL_TEAL_BORDER);
        btnPrintHtml.setPreferredSize(new Dimension(130, 28));
        btnPrintHtml.addActionListener(e -> {
            try {
                editorFullscreen.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(dlg, "Gagal mencetak HTML: " + ex.getMessage());
            }
        });

        JButton btnPrintJasperModal = createFlatButton("Cetak Jasper RM 30-1A", IconStyle.muat("printer.svg", 14, Color.WHITE), COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnPrintJasperModal.setPreferredSize(new Dimension(165, 28));
        btnPrintJasperModal.addActionListener(e -> {
            dlg.dispose();
            cetakRM301A(targetRw);
        });

        JButton btnTutup = createFlatButton("Tutup (ESC)", null, Color.WHITE, COL_TEXT_DARK, COL_BORDER);
        btnTutup.setPreferredSize(new Dimension(100, 28));
        btnTutup.addActionListener(e -> dlg.dispose());

        pnlAksi.add(btnBrowser);
        pnlAksi.add(btnPrintHtml);
        pnlAksi.add(btnPrintJasperModal);
        pnlAksi.add(btnTutup);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(pnlAksi, BorderLayout.EAST);
        dlg.add(topBar, BorderLayout.NORTH);
        dlg.add(new JScrollPane(editorFullscreen), BorderLayout.CENTER);

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

    public String buildOfficialPrintHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>");
        sb.append("@page { size: 215mm 330mm; margin: 8mm 10mm; }");
        sb.append("body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; font-size: 8pt; color: #0f172a; margin: 10px; background: #ffffff; }");
        sb.append("table { width: 100%; border-collapse: collapse; margin-bottom: 6px; }");
        sb.append("th, td { border: 1px solid #1e293b; padding: 2px 4px; font-size: 7.5pt; }");
        sb.append(".red { background-color: #ef4444; color: #ffffff; font-weight: bold; }");
        sb.append(".orange { background-color: #f97316; color: #ffffff; font-weight: bold; }");
        sb.append(".yellow { background-color: #fde047; color: #0f172a; font-weight: bold; }");
        sb.append(".blue { background-color: #2563eb; color: #ffffff; font-weight: bold; }");
        sb.append(".white { background-color: #ffffff; color: #0f172a; }");
        sb.append(".meta { background-color: #e2e8f0; color: #0f172a; font-weight: bold; }");
        sb.append("@media print { .page-break { page-break-after: always; break-after: page; } .no-print { display: none !important; } }");
        sb.append("</style></head><body>");

        String namaRs = akses.getnamars() == null || akses.getnamars().isEmpty() ? "RSUD DEMANG SEPULAU RAYA" : akses.getnamars().toUpperCase();
        String alamatRs = akses.getalamatrs() == null || akses.getalamatrs().isEmpty() ? "Jl. Raya Lintas Sumatera, Gunung Sugih, Kab. Lampung Tengah" : akses.getalamatrs();

        int totalObs = activeObsColumns.size();
        int totalSheets = Math.max(1, (int) Math.ceil((double) Math.max(1, totalObs) / KOLOM_RM301A));

        for (int s = 0; s < totalSheets; s++) {
            int startCol = s * KOLOM_RM301A;
            int endCol = startCol + KOLOM_RM301A;

            if (s > 0) {
                sb.append("<div class='page-break' style='height: 1px; margin-top: 15px; margin-bottom: 15px;'></div>");
                sb.append("<div class='no-print' style='text-align: center; margin: 15px 0; color: #64748b; font-size: 8pt; border-top: 1px dashed #cbd5e1; padding-top: 8px;'>&mdash; Lanjutan Lembar ").append(s + 1).append(" dari ").append(totalSheets).append(" (Kertas F4) &mdash;</div>");
            }

            // Top Header Lembar
            sb.append("<div style='display:flex; justify-content:space-between; align-items:center; margin-bottom:2px;'>");
            sb.append("<div style='font-size:7.5pt; color:#475569;'><b>Format Kertas:</b> F4 / Folio (215 x 330 mm)</div>");
            sb.append("<div style='text-align:right; font-weight:bold; font-size:9.5pt;'>RM 30-1A &bull; LEMBAR ").append(s + 1).append(" DARI ").append(totalSheets).append(" (KOLOM ").append(startCol + 1).append(" s/d ").append(endCol).append(")</div>");
            sb.append("</div>");

            // Kop RSUD + Stiker Identitas Pasien
            sb.append("<table><tr>");
            sb.append("<td width='58%' valign='top' style='padding:5px;'>");
            sb.append("<div style='font-size:8pt; font-weight:bold;'>PEMERINTAH KABUPATEN LAMPUNG TENGAH</div>");
            sb.append("<div style='font-size:10.5pt; font-weight:bold;'>").append(namaRs).append("</div>");
            sb.append("<div style='font-size:7pt;'>").append(alamatRs).append("</div>");
            sb.append("<div style='margin-top:4px; background:#0a2e2c; color:#ffffff; font-weight:bold; padding:2px 6px;'>EARLY WARNING SCORING SYSTEM DEWASA</div>");
            sb.append("</td>");
            sb.append("<td width='42%' valign='top' style='padding:5px; background:#f8fafc;'>");
            sb.append("<div><b>No. Rekam Medis:</b> <span style='background:#0f172a; color:#ffffff; font-weight:bold; padding:1px 5px;'>")
                    .append(TNoRM.getText().isEmpty() ? "-" : TNoRM.getText()).append("</span></div>");
            sb.append("<div><b>Nama Pasien:</b> ").append(TPasien.getText().isEmpty() ? "-" : TPasien.getText())
                    .append(" (").append(JK.getText()).append(")</div>");
            sb.append("<div><b>Tgl. Lahir / Umur:</b> ").append(TglLahir.getText()).append(" / ").append(Umur.getText()).append("</div>");
            sb.append("<div><b>No. Rawat:</b> ").append(TNoRw.getText().isEmpty() ? "-" : TNoRw.getText()).append("</div>");
            sb.append("<div><b>Kamar / Ruang:</b> ").append(lblPasienLokasi != null ? lblPasienLokasi.getText() : "-").append("</div>");
            sb.append("</td></tr></table>");

            // Tabel Matriks 47 Baris x 12 Kolom Lembar Ini
            sb.append("<table>");
            sb.append("<tr style='background:#0a2e2c; color:#ffffff; font-weight:bold;'>");
            sb.append("<th align='left'>PARAMETER</th><th>RENTANG</th>");
            for (int c = 0; c < KOLOM_RM301A; c++) {
                sb.append("<th>#").append(startCol + c + 1).append("</th>");
            }
            sb.append("<th>SKOR</th></tr>");

            for (MatrixRowSpec spec : matrixRowSpecs) {
                String rowCss = spec.colorCode == 3 ? "red"
                        : (spec.colorCode == 2 ? "orange"
                        : (spec.colorCode == 1 ? "yellow"
                        : (spec.colorCode == 4 ? "meta" : "white")));
                sb.append("<tr>");
                sb.append("<td class='meta'><b>").append(spec.groupName).append("</b></td>");
                sb.append("<td class='").append(rowCss).append("' align='center'>").append(spec.label).append("</td>");
                for (int c = 0; c < KOLOM_RM301A; c++) {
                    int actualIdx = startCol + c;
                    ObsColumnData obs = (actualIdx < totalObs) ? activeObsColumns.get(actualIdx) : null;
                    String cellVal = resolveMatrixCellText(spec, obs);
                    String cellCss = rowCss;
                    if ("meta_total".equals(spec.param) && obs != null) {
                        int tot = obs.getTotalInt();
                        cellCss = tot >= 7 ? "red" : ((tot >= 5 || obs.hasSingle3()) ? "orange" : (tot >= 1 ? "yellow" : "white"));
                    }
                    sb.append("<td class='").append(cellCss).append("' align='center'>").append(cellVal).append("</td>");
                }
                sb.append("<td class='").append(rowCss).append("' align='center'><b>").append(spec.scoreText).append("</b></td>");
                sb.append("</tr>");
            }
            sb.append("</table>");

            // Tabel Protokol 5 Tingkat
            sb.append("<table>");
            sb.append("<tr><td width='24%' align='center'><b>Skor 0</b></td><td>Monitor rutin per 8 jam</td></tr>");
            sb.append("<tr><td class='yellow' align='center'>SKOR 1-4</td><td style='background:#fef9c3;'>Assesment segera oleh perawat senior, eskalasi perawatan dan monitoring per 4-6 jam, jika diperlukan konsultasi ke dokter jaga</td></tr>");
            sb.append("<tr><td class='orange' align='center'>SKOR 5-6 atau satu parameter SKOR 3</td><td style='background:#ffedd5;'>Assesment segera oleh dokter jaga (respon segera, maks 5 menit), konsultasi DPJP dan spesialis terkait, eskalasi perawatan dan monitoring tiap jam, pertimbangkan perawatan HCU</td></tr>");
            sb.append("<tr><td class='red' align='center'>SKOR &gt;= 7</td><td style='background:#fee2e2;'>Resusitasi dan monitoring secara kontinyu oleh dokter jaga dan perawat senior, panggil tim medis reaksi cepat, konsultasikan DPJP</td></tr>");
            sb.append("<tr><td class='blue' align='center'>HENTI NAPAS / JANTUNG</td><td style='background:#dbeafe;'>Lakukan RJP, aktivasi henti jantung (118), respon tim henti jantung segera, maksimal 5 menit, resusitasi lanjutan oleh tim medis reaksi cepat (respon segera, maksimal 10 menit)</td></tr>");
            sb.append("</table>");

            // Footer Otorisasi TTE untuk lembar ini
            int lastObsInSheet = Math.min(endCol - 1, totalObs - 1);
            ObsColumnData latestSheetObs = (lastObsInSheet >= startCol) ? activeObsColumns.get(lastObsInSheet) : null;
            String verifier = (latestSheetObs != null && !latestSheetObs.namaPetugas.isEmpty())
                    ? latestSheetObs.namaPetugas
                    : (NmPetugas.getText().isEmpty() ? "-" : NmPetugas.getText());
            String statusTteStr = (latestSheetObs != null && "Tervalidasi".equalsIgnoreCase(latestSheetObs.statusTte))
                    ? "TERVALIDASI TTE SISTEM RME (" + latestSheetObs.waktuTte + ")"
                    : "TERCATAT DI REKAM MEDIS ELEKTRONIK";

            sb.append("<table><tr>");
            sb.append("<td width='65%' style='border:none; font-size:7pt; color:#475569;'>");
            sb.append("<b>Keterangan:</b> RM (Simple Mask), NRM (Non-Rebreathing Mask), NK (Nasal Kanul) &bull; AVPU: Sadar, C (Confused), V (Voice), P (Pain), U (Unresponsive).<br/>");
            sb.append("Dicetak melalui SIMRS Khanza &bull; RSUD Demang Sepulau Raya &bull; Lembar ").append(s + 1).append(" dari ").append(totalSheets);
            sb.append("</td>");
            sb.append("<td width='35%' style='background:#f0fdfa; padding:5px; text-align:center;'>");
            sb.append("<div style='color:#0f766e; font-weight:bold; font-size:7pt;'>[ ").append(statusTteStr).append(" ]</div>");
            sb.append("<div style='font-weight:bold; margin-top:2px;'>").append(verifier).append("</div>");
            sb.append("<div style='font-size:6.5pt; color:#475569;'>Perawat / Petugas Penanggung Jawab Lembar ").append(s + 1).append("</div>");
            sb.append("</td></tr></table>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    private void exportAndOpenHTMLInBrowser() {
        try {
            File temp = File.createTempFile("cetak_ews_rm30_1a_", ".html");
            try (Writer w = new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8)) {
                w.write(buildOfficialPrintHTML());
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(temp.toURI());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuka browser: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // 9. ACTION BAR BAWAH (STICKY FOOTER KONTEKSTUAL TAB 1 & TAB 2)
    // =========================================================================
    private JButton createCardFooterButton(String text, Icon icon, Color textCol, boolean isDanger, int minWidth) {
        final Color normalBg = Color.WHITE;
        final Color normalBorder = new Color(216, 219, 214);
        final Color hoverBg = isDanger ? new Color(254, 242, 242) : new Color(241, 245, 249);
        final Color hoverBorder = isDanger ? new Color(248, 113, 113) : COL_TEAL_PRIMARY;
        final Color hoverFg = isDanger ? new Color(185, 28, 28) : COL_TEAL_DARK;

        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (isEnabled()) {
                            hovered = true;
                            setForeground(hoverFg);
                            repaint();
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        setForeground(textCol);
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean activeHover = (hovered || getModel().isRollover()) && isEnabled();
                if (!isEnabled()) {
                    g2.setColor(new Color(248, 250, 252));
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(226, 232, 240));
                } else if (activeHover) {
                    g2.setColor(hoverBg);
                } else {
                    g2.setColor(normalBg);
                }
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean activeHover = (hovered || getModel().isRollover()) && isEnabled();
                if (!isEnabled()) {
                    g2.setColor(new Color(226, 232, 240));
                    g2.setStroke(new BasicStroke(1.0f));
                } else if (activeHover) {
                    g2.setColor(hoverBorder);
                    g2.setStroke(new BasicStroke(1.2f));
                } else {
                    g2.setColor(normalBorder);
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(textCol);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(6);
        }
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setBorder(new EmptyBorder(4, 12, 4, 12));
        Dimension d = btn.getPreferredSize();
        btn.setPreferredSize(new Dimension(Math.max(d.width + 8, minWidth), 30));
        return btn;
    }

    private JPanel buildBottomActionBar() {
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setBackground(Color.WHITE);
        footer.setOpaque(true);
        footer.setPreferredSize(new Dimension(0, 48));
        footer.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, COL_BORDER_DIVIDER),
                new EmptyBorder(0, 8, 0, 8)));

        Color colTextNormal = new Color(50, 50, 50);
        Color colTextDanger = new Color(185, 28, 28);

        JPanel panelAksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelAksi.setOpaque(false);

        // 1. Tombol Simpan Observasi: Solid Medical Teal (#0F766E) senada dengan tombol Cetak RM
        btnSimpanForm = createFlatButton("Simpan Observasi", IconStyle.muat("save.svg", 15, Color.WHITE), COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnSimpanForm.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSimpanForm.setPreferredSize(new Dimension(155, 32));
        btnSimpanForm.setMnemonic('S');
        btnSimpanForm.setToolTipText("Alt+S (Simpan Kolom Baru / Perbarui Kolom Terpilih)");
        btnSimpanForm.addActionListener(e -> {
            if (isEditMode) {
                ganti();
            } else {
                simpan();
            }
        });

        // 2. Tombol Reset / Kolom Baru
        btnResetForm = createCardFooterButton("Reset / Kolom Baru", IconStyle.muat("refresh.svg", 15, IconStyle.HIJAU), colTextNormal, false, 145);
        btnResetForm.setMnemonic('B');
        btnResetForm.setToolTipText("Alt+B (Bersihkan input atau batalkan mode edit)");
        btnResetForm.addActionListener(e -> setModeInputBaru());

        // 3. Tombol Hapus Kolom
        JButton btnHapusKolomForm = createCardFooterButton("Hapus Kolom", IconStyle.muat("trash.svg", 15, IconStyle.MERAH), colTextDanger, true, 120);
        btnHapusKolomForm.setToolTipText("Hapus kolom observasi yang sedang diedit atau dipilih");
        btnHapusKolomForm.addActionListener(e -> hapus());

        // 4. Tombol Pratinjau Cetak Lembar F4
        JButton btnPratinjau = createCardFooterButton("Pratinjau Cetak", IconStyle.muat("file-text.svg", 15, IconStyle.BIRU), colTextNormal, false, 130);
        btnPratinjau.setToolTipText("Buka pratinjau lembar RM 30-1A layar penuh");
        btnPratinjau.addActionListener(e -> bukaDialogPreviewFullscreen(TNoRw.getText().trim()));

        // 5. Tombol Cetak RM 30-1A: Solid Medical Teal (#0F766E)
        btnCetakDokumen = createFlatButton("Cetak RM 30-1A", IconStyle.muat("printer.svg", 15, Color.WHITE), COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnCetakDokumen.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCetakDokumen.setPreferredSize(new Dimension(145, 32));
        btnCetakDokumen.setMnemonic('P');
        btnCetakDokumen.setToolTipText("Alt+P (Cetak formulir resmi RM 30-1A Jasper)");
        btnCetakDokumen.addActionListener(e -> cetakRM301A(TNoRw.getText().trim()));

        // 6. Tombol Keluar
        JButton btnKeluarInput = createCardFooterButton("Keluar", IconStyle.muat("log-out.svg", 15, IconStyle.ABU_MUDA), colTextDanger, true, 96);
        btnKeluarInput.setMnemonic('K');
        btnKeluarInput.setToolTipText("Alt+K (Tutup Jendela EWS Dewasa)");
        btnKeluarInput.addActionListener(e -> dispose());

        panelAksi.add(btnSimpanForm);
        panelAksi.add(btnResetForm);
        panelAksi.add(btnHapusKolomForm);
        panelAksi.add(btnPratinjau);
        panelAksi.add(btnCetakDokumen);
        panelAksi.add(btnKeluarInput);

        JPanel rightInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        rightInfoPanel.setOpaque(false);
        lblFooterHint = new JLabel("Ketik nilai fisiologis atau klik salah satu kolom #1 s/d #12 untuk mengedit.");
        lblFooterHint.setFont(new Font("Segoe UI", Font.PLAIN, 11).deriveFont(11.5f));
        lblFooterHint.setForeground(COL_TEXT_MUTED);
        rightInfoPanel.add(lblFooterHint);

        footer.add(panelAksi, BorderLayout.CENTER);
        footer.add(rightInfoPanel, BorderLayout.EAST);
        return footer;
    }

    // =========================================================================
    // 11. LOGIKA KALKULASI OTOMATIS EWS DEWASA (RM 30-1A)
    // =========================================================================
    private void pasangListenersKalkulasi() {
        DocumentListener numDocListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isUpdatingFromCode) syncFromNumericInputs();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isUpdatingFromCode) syncFromNumericInputs();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!isUpdatingFromCode) syncFromNumericInputs();
            }
        };

        txtRR.getDocument().addDocumentListener(numDocListener);
        txtSpO2.getDocument().addDocumentListener(numDocListener);
        txtSistol.getDocument().addDocumentListener(numDocListener);
        txtNadi.getDocument().addDocumentListener(numDocListener);
        txtSuhu.getDocument().addDocumentListener(numDocListener);
        AliranOksigen.getDocument().addDocumentListener(numDocListener);
        GulaDarah.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLiveMonitorCard();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLiveMonitorCard();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLiveMonitorCard();
            }
        });

        ItemListener comboListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && !isUpdatingFromCode) {
                Object src = e.getSource();
                isUpdatingFromCode = true;
                try {
                    // Jika perawat memilih rentang (Pita) secara langsung dari dropdown,
                    // perbarui atau kosongkan isian Nilai jika angka sebelumnya berada di luar rentang pita yang dipilih
                    // agar tidak pernah terjadi kontradiksi antara Nilai angka dan Pita RM 30-1A.
                    if (src == cmbSkor1 && !isValueMatchingBandRR(txtRR.getText(), String.valueOf(cmbSkor1.getSelectedItem()))) {
                        txtRR.setText("");
                    } else if (src == cmbSkor2 && !isValueMatchingBandSpO2(txtSpO2.getText(), String.valueOf(cmbSkor2.getSelectedItem()))) {
                        txtSpO2.setText("");
                    } else if (src == cmbSkor4 && !isValueMatchingBandSBP(txtSistol.getText(), String.valueOf(cmbSkor4.getSelectedItem()))) {
                        txtSistol.setText("");
                    } else if (src == cmbSkor5 && !isValueMatchingBandHR(txtNadi.getText(), String.valueOf(cmbSkor5.getSelectedItem()))) {
                        txtNadi.setText("");
                    } else if (src == cmbSkor7 && !isValueMatchingBandTemp(txtSuhu.getText(), String.valueOf(cmbSkor7.getSelectedItem()))) {
                        txtSuhu.setText("");
                    }
                } finally {
                    isUpdatingFromCode = false;
                }
                updateBandLockState();
                syncScoreFromCombo();
            }
        };
        cmbSkor1.addItemListener(comboListener);
        cmbSkor2.addItemListener(comboListener);
        cmbSkor3.addItemListener(comboListener);
        cmbSkor4.addItemListener(comboListener);
        cmbSkor5.addItemListener(comboListener);
        cmbSkor6.addItemListener(comboListener);
        cmbSkor7.addItemListener(comboListener);

        AlatOksigen.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && !isUpdatingFromCode) {
                String alat = AlatOksigen.getSelectedItem() != null ? AlatOksigen.getSelectedItem().toString() : "Tanpa Alat";
                isUpdatingFromCode = true;
                if ("Tanpa Alat".equals(alat)) {
                    cmbSkor3.setSelectedItem("Tidak");
                    if (segSuplemenO2 != null) segSuplemenO2.setSelectedValue("Tidak");
                } else {
                    cmbSkor3.setSelectedItem("Ya");
                    if (segSuplemenO2 != null) segSuplemenO2.setSelectedValue("Ya");
                }
                isUpdatingFromCode = false;
                syncScoreFromCombo();
            }
        });

        chkCodeBlue.addActionListener(e -> {
            if (!isUpdatingFromCode) {
                isHitung();
                updateLiveMonitorCard();
            }
        });
    }

    private void syncFromNumericInputs() {
        isUpdatingFromCode = true;
        try {
            // 1. Laju Pernapasan
            String sRR = txtRR.getText().trim();
            if (!sRR.isEmpty()) {
                try {
                    int v = Integer.parseInt(sRR);
                    if (v >= 25) cmbSkor1.setSelectedItem(">= 25");
                    else if (v >= 21) cmbSkor1.setSelectedItem("21 - 24");
                    else if (v >= 12) cmbSkor1.setSelectedItem("12 - 20");
                    else if (v >= 9) cmbSkor1.setSelectedItem("9 - 11");
                    else cmbSkor1.setSelectedItem("<= 8");
                } catch (Exception ignored) {}
            }

            // 2. Saturasi Oksigen
            String sSpO2 = txtSpO2.getText().trim();
            if (!sSpO2.isEmpty()) {
                try {
                    int v = Integer.parseInt(sSpO2);
                    if (v >= 96) cmbSkor2.setSelectedItem(">= 96");
                    else if (v >= 94) cmbSkor2.setSelectedItem("94 - 95");
                    else if (v >= 92) cmbSkor2.setSelectedItem("92 - 93");
                    else cmbSkor2.setSelectedItem("<= 91");
                } catch (Exception ignored) {}
            }

            // 3. Aliran Oksigen -> O2 Suplemen
            String sAliran = AliranOksigen.getText().trim();
            if (!sAliran.isEmpty()) {
                try {
                    double lpm = Double.parseDouble(sAliran.replace(',', '.'));
                    if (lpm > 0) {
                        cmbSkor3.setSelectedItem("Ya");
                        if (segSuplemenO2 != null) segSuplemenO2.setSelectedValue("Ya");
                        if ("Tanpa Alat".equals(AlatOksigen.getSelectedItem())) {
                            AlatOksigen.setSelectedItem("NK");
                        }
                    }
                } catch (Exception ignored) {}
            }

            // 4. TD Sistolik (12 Pita)
            String sSbp = txtSistol.getText().trim();
            if (!sSbp.isEmpty()) {
                try {
                    int v = Integer.parseInt(sSbp);
                    if (v >= 220) cmbSkor4.setSelectedItem(">= 220");
                    else if (v >= 201) cmbSkor4.setSelectedItem("201 - 219");
                    else if (v >= 181) cmbSkor4.setSelectedItem("181 - 200");
                    else if (v >= 161) cmbSkor4.setSelectedItem("161 - 180");
                    else if (v >= 141) cmbSkor4.setSelectedItem("141 - 160");
                    else if (v >= 121) cmbSkor4.setSelectedItem("121 - 140");
                    else if (v >= 111) cmbSkor4.setSelectedItem("111 - 120");
                    else if (v >= 101) cmbSkor4.setSelectedItem("101 - 110");
                    else if (v >= 91) cmbSkor4.setSelectedItem("91 - 100");
                    else if (v >= 81) cmbSkor4.setSelectedItem("81 - 90");
                    else if (v >= 71) cmbSkor4.setSelectedItem("71 - 80");
                    else cmbSkor4.setSelectedItem("<= 70");
                } catch (Exception ignored) {}
            }

            // 5. Frekuensi Nadi (11 Pita)
            String sHr = txtNadi.getText().trim();
            if (!sHr.isEmpty()) {
                try {
                    int v = Integer.parseInt(sHr);
                    if (v >= 131) cmbSkor5.setSelectedItem(">= 131");
                    else if (v >= 121) cmbSkor5.setSelectedItem("121 - 130");
                    else if (v >= 111) cmbSkor5.setSelectedItem("111 - 120");
                    else if (v >= 101) cmbSkor5.setSelectedItem("101 - 110");
                    else if (v >= 91) cmbSkor5.setSelectedItem("91 - 100");
                    else if (v >= 81) cmbSkor5.setSelectedItem("81 - 90");
                    else if (v >= 71) cmbSkor5.setSelectedItem("71 - 80");
                    else if (v >= 61) cmbSkor5.setSelectedItem("61 - 70");
                    else if (v >= 51) cmbSkor5.setSelectedItem("51 - 60");
                    else if (v >= 41) cmbSkor5.setSelectedItem("41 - 50");
                    else cmbSkor5.setSelectedItem("<= 40");
                } catch (Exception ignored) {}
            }

            // 7. Temperatur (6 Pita dengan titik '.')
            String sTemp = txtSuhu.getText().trim().replace(',', '.');
            if (!sTemp.isEmpty()) {
                try {
                    double v = Double.parseDouble(sTemp);
                    if (v >= 39.1) cmbSkor7.setSelectedItem(">= 39.1");
                    else if (v >= 38.1) cmbSkor7.setSelectedItem("38.1 - 39.0");
                    else if (v >= 37.1) cmbSkor7.setSelectedItem("37.1 - 38.0");
                    else if (v >= 36.1) cmbSkor7.setSelectedItem("36.1 - 37.0");
                    else if (v >= 35.1) cmbSkor7.setSelectedItem("35.1 - 36.0");
                    else cmbSkor7.setSelectedItem("<= 35.0");
                } catch (Exception ignored) {}
            }
        } finally {
            isUpdatingFromCode = false;
        }
        updateBandLockState();
        syncScoreFromCombo();
    }

    /**
     * Mengunci dropdown Pita secara otomatis ketika Nilai numerik diisi agar tidak pernah terjadi
     * ketidaksesuaian klinis antara angka tanda vital yang diketik dengan rentang pita RM 30-1A.
     * Jika Nilai dikosongkan (atau saat memuat data riwayat), Pita otomatis terbuka kembali.
     */
    private void updateBandLockState() {
        boolean hasRR = isNumericTextValid(txtRR.getText(), false);
        boolean hasSpO2 = isNumericTextValid(txtSpO2.getText(), false);
        boolean hasSBP = isNumericTextValid(txtSistol.getText(), false);
        boolean hasHR = isNumericTextValid(txtNadi.getText(), false);
        boolean hasTemp = isNumericTextValid(txtSuhu.getText(), true);

        cmbSkor1.setEnabled(!hasRR);
        cmbSkor1.setToolTipText(hasRR
                ? "Rentang pita ditentukan otomatis dari Nilai Laju Pernapasan (" + txtRR.getText().trim() + " x/mnt). Kosongkan Nilai jika ingin memilih pita secara manual."
                : "Pilih rentang pita Laju Pernapasan RM 30-1A");

        cmbSkor2.setEnabled(!hasSpO2);
        cmbSkor2.setToolTipText(hasSpO2
                ? "Rentang pita ditentukan otomatis dari Nilai Saturasi Oksigen (" + txtSpO2.getText().trim() + " %). Kosongkan Nilai jika ingin memilih pita secara manual."
                : "Pilih rentang pita Saturasi Oksigen RM 30-1A");

        cmbSkor4.setEnabled(!hasSBP);
        cmbSkor4.setToolTipText(hasSBP
                ? "Rentang pita ditentukan otomatis dari Nilai TD Sistolik (" + txtSistol.getText().trim() + " mmHg). Kosongkan Nilai jika ingin memilih pita secara manual."
                : "Pilih rentang pita TD Sistolik RM 30-1A");

        cmbSkor5.setEnabled(!hasHR);
        cmbSkor5.setToolTipText(hasHR
                ? "Rentang pita ditentukan otomatis dari Nilai Frekuensi Nadi (" + txtNadi.getText().trim() + " x/mnt). Kosongkan Nilai jika ingin memilih pita secara manual."
                : "Pilih rentang pita Frekuensi Nadi RM 30-1A");

        cmbSkor7.setEnabled(!hasTemp);
        cmbSkor7.setToolTipText(hasTemp
                ? "Rentang pita ditentukan otomatis dari Nilai Temperatur (" + txtSuhu.getText().trim() + " °C). Kosongkan Nilai jika ingin memilih pita secara manual."
                : "Pilih rentang pita Temperatur RM 30-1A");
    }

    private boolean isNumericTextValid(String text, boolean allowDecimal) {
        if (text == null || text.trim().isEmpty()) return false;
        try {
            if (allowDecimal) {
                Double.parseDouble(text.trim().replace(',', '.'));
            } else {
                Integer.parseInt(text.trim());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValueMatchingBandRR(String text, String band) {
        if (!isNumericTextValid(text, false)) return true;
        int v = Integer.parseInt(text.trim());
        String expected = (v >= 25) ? ">= 25" : (v >= 21) ? "21 - 24" : (v >= 12) ? "12 - 20" : (v >= 9) ? "9 - 11" : "<= 8";
        return expected.equals(band);
    }

    private boolean isValueMatchingBandSpO2(String text, String band) {
        if (!isNumericTextValid(text, false)) return true;
        int v = Integer.parseInt(text.trim());
        String expected = (v >= 96) ? ">= 96" : (v >= 94) ? "94 - 95" : (v >= 92) ? "92 - 93" : "<= 91";
        return expected.equals(band);
    }

    private boolean isValueMatchingBandSBP(String text, String band) {
        if (!isNumericTextValid(text, false)) return true;
        int v = Integer.parseInt(text.trim());
        String expected;
        if (v >= 220) expected = ">= 220";
        else if (v >= 201) expected = "201 - 219";
        else if (v >= 181) expected = "181 - 200";
        else if (v >= 161) expected = "161 - 180";
        else if (v >= 141) expected = "141 - 160";
        else if (v >= 121) expected = "121 - 140";
        else if (v >= 111) expected = "111 - 120";
        else if (v >= 101) expected = "101 - 110";
        else if (v >= 91) expected = "91 - 100";
        else if (v >= 81) expected = "81 - 90";
        else if (v >= 71) expected = "71 - 80";
        else expected = "<= 70";
        return expected.equals(band);
    }

    private boolean isValueMatchingBandHR(String text, String band) {
        if (!isNumericTextValid(text, false)) return true;
        int v = Integer.parseInt(text.trim());
        String expected;
        if (v >= 131) expected = ">= 131";
        else if (v >= 121) expected = "121 - 130";
        else if (v >= 111) expected = "111 - 120";
        else if (v >= 101) expected = "101 - 110";
        else if (v >= 91) expected = "91 - 100";
        else if (v >= 81) expected = "81 - 90";
        else if (v >= 71) expected = "71 - 80";
        else if (v >= 61) expected = "61 - 70";
        else if (v >= 51) expected = "51 - 60";
        else if (v >= 41) expected = "41 - 50";
        else expected = "<= 40";
        return expected.equals(band);
    }

    private boolean isValueMatchingBandTemp(String text, String band) {
        if (!isNumericTextValid(text, true)) return true;
        double v = Double.parseDouble(text.trim().replace(',', '.'));
        String expected;
        if (v >= 39.1) expected = ">= 39.1";
        else if (v >= 38.1) expected = "38.1 - 39.0";
        else if (v >= 37.1) expected = "37.1 - 38.0";
        else if (v >= 36.1) expected = "36.1 - 37.0";
        else if (v >= 35.1) expected = "35.1 - 36.0";
        else expected = "<= 35.0";
        return expected.equals(band);
    }

    private void syncScoreFromCombo() {
        updateBandLockState();
        isCombo1();
        isCombo2();
        isCombo3();
        isCombo4();
        isCombo5();
        isCombo6();
        isCombo7();
        isjml();
        isHitung();

        styleScoreBadge(badgeRR, Skor1.getText());
        styleScoreBadge(badgeSpO2, Skor2.getText());
        styleScoreBadge(badgeO2, Skor3.getText());
        styleScoreBadge(badgeSistol, Skor4.getText());
        styleScoreBadge(badgeNadi, Skor5.getText());
        styleScoreBadge(badgeAVPU, Skor6.getText());
        styleScoreBadge(badgeSuhu, Skor7.getText());

        if (segSuplemenO2 != null && cmbSkor3.getSelectedItem() != null) {
            segSuplemenO2.setSelectedValue(cmbSkor3.getSelectedItem().toString());
        }
        if (segAVPU != null && cmbSkor6.getSelectedItem() != null) {
            String k = cmbSkor6.getSelectedItem().toString();
            segAVPU.setSelectedValue("Sadar".equalsIgnoreCase(k) ? "Sadar" : "C/V/P/U");
        }

        updateLiveMonitorCard();
    }

    private void updateLiveMonitorCard() {
        if (liveScoreHeader == null) return;

        int total = 0;
        try {
            total = Integer.parseInt(TotalSkor.getText().trim());
        } catch (Exception ignored) {}

        boolean has3 = false;
        for (widget.TextBox kotak : new widget.TextBox[]{Skor1, Skor2, Skor3, Skor4, Skor5, Skor6, Skor7}) {
            if (kotak != null && "3".equals(kotak.getText().trim())) {
                has3 = true;
                break;
            }
        }
        boolean cb = chkCodeBlue != null && chkCodeBlue.isSelected();

        lblSkorTerkiniBesar.setText(String.valueOf(total));
        lblSingleRedWarning.setVisible(has3 && total < 5 && !cb);

        styleMiniBreakPill(pillBreakRR, "RR", Skor1.getText());
        styleMiniBreakPill(pillBreakSpO2, "SpO2", Skor2.getText());
        styleMiniBreakPill(pillBreakO2, "O2", Skor3.getText());
        styleMiniBreakPill(pillBreakSBP, "TD", Skor4.getText());
        styleMiniBreakPill(pillBreakHR, "Nadi", Skor5.getText());
        styleMiniBreakPill(pillBreakAVPU, "AVPU", Skor6.getText());
        styleMiniBreakPill(pillBreakTemp, "Suhu", Skor7.getText());
        String gdsStr = (GulaDarah != null && !GulaDarah.getText().trim().isEmpty()) ? GulaDarah.getText().trim() : "-";
        pillBreakGDS.setText("GDS: " + gdsStr);

        if (cb) {
            liveScoreHeader.setBackground(EWS_BLUE);
            lblBandTerkiniBadge.setForeground(Color.WHITE);
            lblBandTerkiniBadge.setText("HENTI JANTUNG (118)");
            lblSkorTerkiniBesar.setForeground(EWS_BLUE);
            lblLiveFreq.setText("Respon Tim Henti Jantung < 5 Menit");
            lblLiveFreq.setForeground(EWS_BLUE);
            lblBandTerkiniAksi.setText("Lakukan RJP, aktivasi henti jantung (118), respon tim henti jantung segera (maks 5 menit), resusitasi lanjutan oleh TMRC (maks 10 menit).");
        } else if (total >= 7) {
            liveScoreHeader.setBackground(EWS_RED);
            lblBandTerkiniBadge.setForeground(Color.WHITE);
            lblBandTerkiniBadge.setText("RISIKO TINGGI (SKOR >= 7)");
            lblSkorTerkiniBesar.setForeground(EWS_RED);
            lblLiveFreq.setText("Monitoring Kontinyu & Panggil TMRC");
            lblLiveFreq.setForeground(EWS_RED);
            lblBandTerkiniAksi.setText("Resusitasi dan monitoring secara kontinyu oleh dokter jaga dan perawat senior, panggil tim medis reaksi cepat, konsultasikan DPJP.");
        } else if (total >= 5 || has3) {
            liveScoreHeader.setBackground(EWS_ORANGE);
            lblBandTerkiniBadge.setForeground(Color.WHITE);
            lblBandTerkiniBadge.setText(total >= 5 ? "RISIKO SEDANG (5-6)" : "ESKALASI 1 PARAM SKOR 3");
            lblSkorTerkiniBesar.setForeground(EWS_ORANGE);
            lblLiveFreq.setText("Monitoring Tiap 1 Jam & Dokter Jaga < 5 Menit");
            lblLiveFreq.setForeground(new Color(194, 65, 12));
            lblBandTerkiniAksi.setText("Assesment segera oleh dokter jaga (respon segera, maks 5 menit), konsultasi DPJP dan spesialis terkait, eskalasi perawatan dan monitoring tiap jam, pertimbangkan perawatan HCU.");
        } else if (total >= 1) {
            liveScoreHeader.setBackground(EWS_YELLOW);
            lblBandTerkiniBadge.setForeground(COL_TEXT_DARK);
            lblBandTerkiniBadge.setText("RISIKO RENDAH (1-4)");
            lblSkorTerkiniBesar.setForeground(new Color(180, 83, 9));
            lblLiveFreq.setText("Monitoring Tiap 4-6 Jam (Perawat Senior)");
            lblLiveFreq.setForeground(new Color(180, 83, 9));
            lblBandTerkiniAksi.setText("Assesment segera oleh perawat senior, eskalasi perawatan dan monitoring per 4-6 jam, jika diperlukan konsultasi ke dokter jaga.");
        } else {
            liveScoreHeader.setBackground(COL_TEAL_DARK);
            lblBandTerkiniBadge.setForeground(Color.WHITE);
            lblBandTerkiniBadge.setText("NORMAL (SKOR 0)");
            lblSkorTerkiniBesar.setForeground(COL_TEAL_DARK);
            lblLiveFreq.setText("Frekuensi Monitoring: Tiap 8 Jam");
            lblLiveFreq.setForeground(COL_TEAL_DARK);
            lblBandTerkiniAksi.setText("Monitor rutin per 8 jam.");
        }
    }

    private String skorDari(String pita, String[] petaPitaSkor) {
        for (int n = 0; n < petaPitaSkor.length; n += 2) {
            if (petaPitaSkor[n].equals(pita)) return petaPitaSkor[n + 1];
        }
        return "0";
    }

    private void warna(widget.TextBox kotak, String skor) {
        kotak.setText(skor);
        switch (skor) {
            case "1":
                kotak.setBackground(EWS_YELLOW);
                kotak.setForeground(COL_TEXT_DARK);
                break;
            case "2":
                kotak.setBackground(EWS_ORANGE);
                kotak.setForeground(Color.WHITE);
                break;
            case "3":
                kotak.setBackground(EWS_RED);
                kotak.setForeground(Color.WHITE);
                break;
            default:
                kotak.setBackground(Color.WHITE);
                kotak.setForeground(COL_TEXT_DARK);
                break;
        }
    }

    private void isCombo1() {
        warna(Skor1, skorDari(cmbSkor1.getSelectedItem() == null ? "" : cmbSkor1.getSelectedItem().toString(), new String[]{
            ">= 25", "3", "21 - 24", "2", "12 - 20", "0", "9 - 11", "1", "<= 8", "3",
            "<= 5", "3", "6 - 8", "3", "25 - 34", "3", ">= 35", "3"
        }));
    }

    private void isCombo2() {
        warna(Skor2, skorDari(cmbSkor2.getSelectedItem() == null ? "" : cmbSkor2.getSelectedItem().toString(), new String[]{
            ">= 96", "0", "94 - 95", "1", "92 - 93", "2", "<= 91", "3",
            ">= 95", "0", "<= 92", "3"
        }));
    }

    private void isCombo3() {
        warna(Skor3, (cmbSkor3.getSelectedItem() != null && cmbSkor3.getSelectedItem().equals("Ya")) ? "2" : "0");
    }

    private void isCombo4() {
        warna(Skor4, skorDari(cmbSkor4.getSelectedItem() == null ? "" : cmbSkor4.getSelectedItem().toString(), new String[]{
            ">= 220", "3", "201 - 219", "0", "181 - 200", "0", "161 - 180", "0",
            "141 - 160", "0", "121 - 140", "0", "111 - 120", "0", "101 - 110", "1",
            "91 - 100", "2", "81 - 90", "3", "71 - 80", "3", "<= 70", "3",
            "181 - 220", "2", "111 - 180", "0", "71 - 90", "3"
        }));
    }

    private void isCombo5() {
        warna(Skor5, skorDari(cmbSkor5.getSelectedItem() == null ? "" : cmbSkor5.getSelectedItem().toString(), new String[]{
            ">= 131", "3", "121 - 130", "2", "111 - 120", "2", "101 - 110", "1",
            "91 - 100", "1", "81 - 90", "0", "71 - 80", "0", "61 - 70", "0",
            "51 - 60", "0", "41 - 50", "1", "<= 40", "3",
            ">= 140", "3", "131 - 140", "3", "111 - 130", "2", "91 - 110", "1", "51 - 90", "0"
        }));
    }

    private void isCombo6() {
        warna(Skor6, skorDari(cmbSkor6.getSelectedItem() == null ? "" : cmbSkor6.getSelectedItem().toString(), new String[]{
            "Sadar", "0", "C/V/P/U", "3", "Nyeri/Verbal", "3", "Unrespon", "3"
        }));
    }

    private void isCombo7() {
        warna(Skor7, skorDari(cmbSkor7.getSelectedItem() == null ? "" : cmbSkor7.getSelectedItem().toString(), new String[]{
            ">= 39.1", "2", "38.1 - 39.0", "1", "37.1 - 38.0", "0", "36.1 - 37.0", "0",
            "35.1 - 36.0", "1", "<= 35.0", "3",
            "<= 35", "3", "35.1 - 36", "1", "36.1 - 38", "0", "38.1 - 39", "1", ">= 39", "2"
        }));
    }

    private void isjml() {
        if (!Skor1.getText().isEmpty() && !Skor2.getText().isEmpty() && !Skor3.getText().isEmpty()
                && !Skor4.getText().isEmpty() && !Skor5.getText().isEmpty() && !Skor6.getText().isEmpty() && !Skor7.getText().isEmpty()) {
            int tot = Integer.parseInt(Skor1.getText().trim())
                    + Integer.parseInt(Skor2.getText().trim())
                    + Integer.parseInt(Skor3.getText().trim())
                    + Integer.parseInt(Skor4.getText().trim())
                    + Integer.parseInt(Skor5.getText().trim())
                    + Integer.parseInt(Skor6.getText().trim())
                    + Integer.parseInt(Skor7.getText().trim());
            TotalSkor.setText(String.valueOf(tot));
        }
    }

    private void isHitung() {
        int total;
        try {
            total = Integer.parseInt(TotalSkor.getText().trim());
        } catch (Exception e) {
            return;
        }

        boolean adaParameterTiga = false;
        for (widget.TextBox kotak : new widget.TextBox[]{Skor1, Skor2, Skor3, Skor4, Skor5, Skor6, Skor7}) {
            if ("3".equals(kotak.getText().trim())) {
                adaParameterTiga = true;
                break;
            }
        }

        if (chkCodeBlue != null && chkCodeBlue.isSelected()) {
            ParameterSkor.setText("Lakukan RJP, aktivasi henti jantung (118), respon tim henti jantung segera, maksimal 5 menit, resusitasi lanjutan oleh tim medis reaksi cepat (respon segera, maksimal 10 menit)");
        } else if (total >= 7) {
            ParameterSkor.setText("Resusitasi dan monitoring secara kontinyu oleh dokter jaga dan perawat senior, panggil tim medis reaksi cepat, konsultasikan DPJP");
        } else if (total >= 5 || adaParameterTiga) {
            ParameterSkor.setText("Assesment segera oleh dokter jaga (respon segera, maks 5 menit), konsultasi DPJP dan spesialis terkait, eskalasi perawatan dan monitoring tiap jam, pertimbangkan perawatan HCU");
        } else if (total >= 1) {
            ParameterSkor.setText("Assesment segera oleh perawat senior, eskalasi perawatan dan monitoring per 4-6 jam, jika diperlukan konsultasi ke dokter jaga");
        } else {
            ParameterSkor.setText("Monitor rutin per 8 jam");
        }
    }

    // =========================================================================
    // 12. PRESET SIMULASI, MULTI-SOURCE TTV ADAPTER & PEMILIH SLOT 12 KOLOM
    // =========================================================================
    private void isiPresetNormal() {
        isUpdatingFromCode = true;
        try {
            txtRR.setText("18");
            txtSpO2.setText("98");
            txtSistol.setText("120");
            txtNadi.setText("80");
            txtSuhu.setText("36.6");
            AliranOksigen.setText("");
            AlatOksigen.setSelectedItem("Tanpa Alat");
            GulaDarah.setText("105");
            chkCodeBlue.setSelected(false);

            cmbSkor1.setSelectedItem(NOL_RESPIRASI);
            cmbSkor2.setSelectedItem(NOL_SATURASI);
            cmbSkor3.setSelectedItem(NOL_SUPLEMEN);
            cmbSkor4.setSelectedItem(NOL_SISTOLIK);
            cmbSkor5.setSelectedItem(NOL_NADI);
            cmbSkor6.setSelectedItem(NOL_KESADARAN);
            cmbSkor7.setSelectedItem(NOL_TEMPERATUR);
        } finally {
            isUpdatingFromCode = false;
        }
        syncScoreFromCombo();
    }

    private void isiPresetKritis() {
        isUpdatingFromCode = true;
        try {
            txtRR.setText("28");
            txtSpO2.setText("91");
            txtSistol.setText("88");
            txtNadi.setText("132");
            txtSuhu.setText("39.2");
            AliranOksigen.setText("10");
            AlatOksigen.setSelectedItem("NRM");
            GulaDarah.setText("215");
            chkCodeBlue.setSelected(false);

            cmbSkor1.setSelectedItem(">= 25");
            cmbSkor2.setSelectedItem("<= 91");
            cmbSkor3.setSelectedItem("Ya");
            cmbSkor4.setSelectedItem("81 - 90");
            cmbSkor5.setSelectedItem(">= 131");
            cmbSkor6.setSelectedItem("C/V/P/U");
            cmbSkor7.setSelectedItem(">= 39.1");
        } finally {
            isUpdatingFromCode = false;
        }
        syncScoreFromCombo();
    }

    /**
     * Multi-Source Adapter: Menarik otomatis TTV terakhir dari `pemeriksaan_ranap` atau `pemeriksaan_ralan`.
     */
    private void tarikDataTTVTerakhir() {
        String noRawat = TNoRw.getText().trim();
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data pasien terlebih dahulu!");
            return;
        }

        boolean found = false;
        String suhu = "", tensi = "", nadi = "", resp = "", spo2 = "", kesadaran = "";
        try {
            PreparedStatement psTtv = koneksi.prepareStatement(
                    "select suhu_tubuh, tensi, nadi, respirasi, spo2, kesadaran from pemeriksaan_ranap "
                    + "where no_rawat=? order by tgl_perawatan desc, jam_rawat desc limit 1");
            try {
                psTtv.setString(1, noRawat);
                ResultSet rsTtv = psTtv.executeQuery();
                try {
                    if (rsTtv.next()) {
                        suhu = rsTtv.getString("suhu_tubuh");
                        tensi = rsTtv.getString("tensi");
                        nadi = rsTtv.getString("nadi");
                        resp = rsTtv.getString("respirasi");
                        spo2 = rsTtv.getString("spo2");
                        kesadaran = rsTtv.getString("kesadaran");
                        found = true;
                    }
                } finally {
                    rsTtv.close();
                }
            } finally {
                psTtv.close();
            }

            if (!found) {
                PreparedStatement psRalan = koneksi.prepareStatement(
                        "select suhu_tubuh, tensi, nadi, respirasi, spo2, kesadaran from pemeriksaan_ralan "
                        + "where no_rawat=? order by tgl_perawatan desc, jam_rawat desc limit 1");
                try {
                    psRalan.setString(1, noRawat);
                    ResultSet rsRalan = psRalan.executeQuery();
                    try {
                        if (rsRalan.next()) {
                            suhu = rsRalan.getString("suhu_tubuh");
                            tensi = rsRalan.getString("tensi");
                            nadi = rsRalan.getString("nadi");
                            resp = rsRalan.getString("respirasi");
                            spo2 = rsRalan.getString("spo2");
                            kesadaran = rsRalan.getString("kesadaran");
                            found = true;
                        }
                    } finally {
                        rsRalan.close();
                    }
                } finally {
                    psRalan.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarikDataTTVTerakhir: " + e);
        }

        if (found) {
            if (resp != null && !resp.trim().isEmpty()) txtRR.setText(resp.replaceAll("[^0-9]", ""));
            if (spo2 != null && !spo2.trim().isEmpty()) txtSpO2.setText(spo2.replaceAll("[^0-9]", ""));
            if (nadi != null && !nadi.trim().isEmpty()) txtNadi.setText(nadi.replaceAll("[^0-9]", ""));
            if (suhu != null && !suhu.trim().isEmpty()) txtSuhu.setText(suhu.replace(',', '.').replaceAll("[^0-9.]", ""));
            if (tensi != null && !tensi.trim().isEmpty()) {
                String sbp = tensi.contains("/") ? tensi.split("/")[0] : tensi;
                txtSistol.setText(sbp.replaceAll("[^0-9]", ""));
            }
            if (kesadaran != null) {
                isUpdatingFromCode = true;
                if ("Compos Mentis".equalsIgnoreCase(kesadaran.trim())) {
                    cmbSkor6.setSelectedItem("Sadar");
                } else {
                    cmbSkor6.setSelectedItem("C/V/P/U");
                }
                isUpdatingFromCode = false;
            }
            syncFromNumericInputs();
            lblStatusSimpanInfo.setText("TTV terakhir dari CPPT berhasil dimuat");
        } else {
            JOptionPane.showMessageDialog(this, "Belum ada data TTV di Pemeriksaan Ranap / Ralan untuk pasien ini.");
        }
    }

    private void pilihSlotObservasi(int slotIdx) {
        if (slotIdx < 0 || slotIdx >= activeObsColumns.size()) {
            setModeInputBaru();
            return;
        }
        ObsColumnData obs = activeObsColumns.get(slotIdx);
        selectedSlotIndex = slotIdx;

        // Sinkronkan seleksi baris pada tbObat
        for (int r = 0; r < tabMode.getRowCount(); r++) {
            String rw = tabMode.getValueAt(r, 0).toString();
            String tg = tabMode.getValueAt(r, 5).toString();
            if (rw.equals(TNoRw.getText().trim()) && tg.equals(obs.tanggalSql)) {
                tbObat.setRowSelectionInterval(r, r);
                break;
            }
        }

        isUpdatingFromCode = true;
        try {
            txtRR.setText("");
            txtSpO2.setText("");
            txtSistol.setText("");
            txtNadi.setText("");
            txtSuhu.setText("");

            cmbSkor1.setSelectedItem(obs.rrBand);
            Skor1.setText(obs.rrSkor);
            cmbSkor2.setSelectedItem(obs.spo2Band);
            Skor2.setText(obs.spo2Skor);
            cmbSkor3.setSelectedItem(obs.o2Suplemen);
            Skor3.setText(obs.o2Skor);
            cmbSkor4.setSelectedItem(obs.sbpBand);
            Skor4.setText(obs.sbpSkor);
            cmbSkor5.setSelectedItem(obs.hrBand);
            Skor5.setText(obs.hrSkor);
            cmbSkor6.setSelectedItem(obs.avpuBand);
            Skor6.setText(obs.avpuSkor);
            cmbSkor7.setSelectedItem(obs.tempBand);
            Skor7.setText(obs.tempSkor);
            TotalSkor.setText(obs.totalSkor);
            ParameterSkor.setText(obs.kesimpulan);

            AliranOksigen.setText(obs.aliranO2);
            AlatOksigen.setSelectedItem(obs.alatO2 == null || obs.alatO2.isEmpty() ? "Tanpa Alat" : obs.alatO2);
            GulaDarah.setText(obs.gulaDarah);

            if (obs.nip != null && !obs.nip.isEmpty()) {
                KdPetugas.setText(obs.nip);
                NmPetugas.setText(obs.namaPetugas);
                txtInisialPetugas.setText(obs.inisial);
            }

            Valid.SetTgl(Tanggal, obs.tanggalSql);
            if (obs.tanggalSql != null && obs.tanggalSql.length() >= 19) {
                Jam.setSelectedItem(obs.tanggalSql.substring(11, 13));
                Menit.setSelectedItem(obs.tanggalSql.substring(14, 16));
                Detik.setSelectedItem(obs.tanggalSql.substring(17, 19));
            }
        } finally {
            isUpdatingFromCode = false;
        }

        syncScoreFromCombo();
        setModeEditAktif(TNoRw.getText().trim(), obs.tanggalSql, slotIdx);
        matrixTable.repaint();
    }

    private void updateSlotGridHighlight() {
        int startSlot = activeSheetIndex * KOLOM_RM301A;
        int endSlot = startSlot + KOLOM_RM301A;
        int totalCols = activeObsColumns.size();
        int maxSheet = Math.max(0, totalCols / KOLOM_RM301A);

        if (lblSheetTitle != null) {
            lblSheetTitle.setText("Lembar " + (activeSheetIndex + 1) + " (Kolom " + (startSlot + 1) + " - " + endSlot + ")");
        }
        if (btnPrevSheet != null) {
            btnPrevSheet.setEnabled(activeSheetIndex > 0);
        }
        if (btnNextSheet != null) {
            btnNextSheet.setEnabled(activeSheetIndex < maxSheet);
        }

        int filledInThisSheet = 0;
        for (int i = 0; i < KOLOM_RM301A; i++) {
            int actualSlot = startSlot + i;
            JButton b = slotButtons[i];
            if (b == null) continue;
            if (actualSlot < activeObsColumns.size()) {
                filledInThisSheet++;
                ObsColumnData obs = activeObsColumns.get(actualSlot);
                int tot = obs.getTotalInt();
                b.setText("<html><center>#" + (actualSlot + 1) + "<br><span style='font-size:7.5pt;'>" + obs.jamPendek + " (" + tot + ")</span></center></html>");
                b.setToolTipText("Kolom #" + (actualSlot + 1) + ": " + obs.tglPendek + " " + obs.jamPendek + " | Skor: " + tot);
                if (actualSlot == selectedSlotIndex) {
                    b.setBackground(COL_HEADER_DARK);
                    b.setForeground(Color.WHITE);
                    b.setBorder(new LineBorder(COL_TEAL_PRIMARY, 2, true));
                } else if (tot >= 7) {
                    b.setBackground(new Color(254, 226, 226));
                    b.setForeground(new Color(185, 28, 28));
                    b.setBorder(new LineBorder(new Color(248, 113, 113), 1, true));
                } else if (tot >= 5 || obs.hasSingle3()) {
                    b.setBackground(new Color(255, 237, 213));
                    b.setForeground(new Color(194, 65, 12));
                    b.setBorder(new LineBorder(new Color(251, 146, 60), 1, true));
                } else if (tot >= 1) {
                    b.setBackground(new Color(254, 249, 195));
                    b.setForeground(new Color(161, 98, 7));
                    b.setBorder(new LineBorder(new Color(250, 204, 21), 1, true));
                } else {
                    b.setBackground(COL_TEAL_SOFT);
                    b.setForeground(COL_TEAL_DARK);
                    b.setBorder(new LineBorder(COL_TEAL_BORDER, 1, true));
                }
            } else {
                b.setText("<html><center>#" + (actualSlot + 1) + "<br><span style='font-size:7.5pt;'>Kosong</span></center></html>");
                b.setToolTipText("Slot Kolom #" + (actualSlot + 1) + " (Belum terisi)");
                b.setBackground(Color.WHITE);
                b.setForeground(COL_TEXT_MUTED);
                b.setBorder(new LineBorder(COL_BORDER_DIVIDER, 1, true));
            }
        }

        if (lblKolomTerisiStatus != null) {
            lblKolomTerisiStatus.setText(totalCols + " Terisi (Lbr " + (activeSheetIndex + 1) + ": " + filledInThisSheet + "/12)");
        }
    }

    // =========================================================================
    // 13. OPERASI CRUD DATABASE & MATRIKS RM 30-1A (22 KOLOM POSISIONAL)
    // =========================================================================
    public void emptTeks() {
        Tanggal.setDate(new Date());
        Calendar cal = Calendar.getInstance();
        Jam.setSelectedItem(String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
        Menit.setSelectedItem(String.format("%02d", cal.get(Calendar.MINUTE)));
        Detik.setSelectedItem(String.format("%02d", cal.get(Calendar.SECOND)));

        isUpdatingFromCode = true;
        try {
            txtRR.setText("");
            txtSpO2.setText("");
            txtSuhu.setText("");
            txtSistol.setText("");
            txtNadi.setText("");
            chkCodeBlue.setSelected(false);

            cmbSkor1.setSelectedItem(NOL_RESPIRASI);
            Skor1.setText("0");
            cmbSkor2.setSelectedItem(NOL_SATURASI);
            Skor2.setText("0");
            cmbSkor3.setSelectedItem(NOL_SUPLEMEN);
            Skor3.setText("0");
            cmbSkor4.setSelectedItem(NOL_SISTOLIK);
            Skor4.setText("0");
            cmbSkor5.setSelectedItem(NOL_NADI);
            Skor5.setText("0");
            cmbSkor6.setSelectedItem(NOL_KESADARAN);
            Skor6.setText("0");
            cmbSkor7.setSelectedItem(NOL_TEMPERATUR);
            Skor7.setText("0");
            TotalSkor.setText("0");
            ParameterSkor.setText("Monitor rutin per 8 jam");
            AliranOksigen.setText("");
            AlatOksigen.setSelectedItem("Tanpa Alat");
            GulaDarah.setText("");
        } finally {
            isUpdatingFromCode = false;
        }
        syncScoreFromCombo();
        txtRR.requestFocus();
    }

    private void simpan() {
        if (TNoRw.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pasien belum dipilih!");
            return;
        }
        if (KdPetugas.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Petugas pengisi belum dipilih!");
            return;
        }

        syncScoreFromCombo();
        String tgl = Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem();

        if (Sequel.menyimpantf("pemantauan_pews_dewasa", "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?", "Data", 22, new String[]{
            TNoRw.getText(), tgl,
            cmbSkor1.getSelectedItem().toString(), Skor1.getText(),
            cmbSkor2.getSelectedItem().toString(), Skor2.getText(),
            cmbSkor3.getSelectedItem().toString(), Skor3.getText(),
            cmbSkor4.getSelectedItem().toString(), Skor4.getText(),
            cmbSkor5.getSelectedItem().toString(), Skor5.getText(),
            cmbSkor6.getSelectedItem().toString(), Skor6.getText(),
            cmbSkor7.getSelectedItem().toString(), Skor7.getText(),
            TotalSkor.getText(), ParameterSkor.getText(), KdPetugas.getText(),
            AliranOksigen.getText(), AlatOksigen.getSelectedItem().toString(), GulaDarah.getText()
        })) {
            tabMode.addRow(new Object[]{
                TNoRw.getText(), TNoRM.getText(), TPasien.getText(), Umur.getText(), JK.getText(), tgl,
                cmbSkor1.getSelectedItem().toString(), Skor1.getText(),
                cmbSkor2.getSelectedItem().toString(), Skor2.getText(),
                cmbSkor3.getSelectedItem().toString(), Skor3.getText(),
                cmbSkor4.getSelectedItem().toString(), Skor4.getText(),
                cmbSkor5.getSelectedItem().toString(), Skor5.getText(),
                cmbSkor6.getSelectedItem().toString(), Skor6.getText(),
                cmbSkor7.getSelectedItem().toString(), Skor7.getText(),
                TotalSkor.getText(), ParameterSkor.getText(),
                KdPetugas.getText(), NmPetugas.getText(), TglLahir.getText()
            });
            int cnt = tabMode.getRowCount();
            LCount.setText("" + cnt);
            if (lblStatusSimpanInfo != null) {
                lblStatusSimpanInfo.setText("Tersimpan (" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + " WIB)");
            }
            setModeInputBaru();
            tampilBaganMatrix();
        }
    }

    private void ganti() {
        String targetRw = isEditMode && !editNoRawat.isEmpty()
                ? editNoRawat
                : TNoRw.getText().trim();
        String targetTgl = isEditMode && !editTanggalSql.isEmpty()
                ? editTanggalSql
                : (selectedSlotIndex >= 0 && selectedSlotIndex < activeObsColumns.size() ? activeObsColumns.get(selectedSlotIndex).tanggalSql : "");

        if (targetRw.isEmpty() || targetTgl.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pilih kolom pada bagan observasi yang ingin diubah terlebih dahulu!");
            return;
        }

        syncScoreFromCombo();
        String tgl = Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem();

        if (Sequel.mengedittf("pemantauan_pews_dewasa", "tanggal=? and no_rawat=?",
                "no_rawat=?,tanggal=?,parameter_laju_respirasi=?,skor_laju_respirasi=?,parameter_saturasi_oksigen=?,"
                + "skor_saturasi_oksigen=?,parameter_suplemen_oksigen=?,skor_suplemen_oksigen=?,parameter_tekanan_darah_sistolik=?,skor_tekanan_darah_sistolik=?,"
                + "parameter_laju_jantung=?,skor_laju_jantung=?,parameter_kesadaran=?,skor_kesadaran=?,parameter_temperatur=?,skor_temperatur=?,skor_total=?,"
                + "parameter_total=?,nip=?,aliran_oksigen=?,alat_oksigen=?,gula_darah=?", 24, new String[]{
                    TNoRw.getText(), tgl,
                    cmbSkor1.getSelectedItem().toString(), Skor1.getText(),
                    cmbSkor2.getSelectedItem().toString(), Skor2.getText(),
                    cmbSkor3.getSelectedItem().toString(), Skor3.getText(),
                    cmbSkor4.getSelectedItem().toString(), Skor4.getText(),
                    cmbSkor5.getSelectedItem().toString(), Skor5.getText(),
                    cmbSkor6.getSelectedItem().toString(), Skor6.getText(),
                    cmbSkor7.getSelectedItem().toString(), Skor7.getText(),
                    TotalSkor.getText(), ParameterSkor.getText(), KdPetugas.getText(),
                    AliranOksigen.getText(), AlatOksigen.getSelectedItem().toString(), GulaDarah.getText(),
                    targetTgl, targetRw
                })) {
            for (int r = 0; r < tabMode.getRowCount(); r++) {
                if (targetRw.equals(tabMode.getValueAt(r, 0).toString()) && targetTgl.equals(tabMode.getValueAt(r, 5).toString())) {
                    tabMode.setValueAt(TNoRw.getText(), r, 0);
                    tabMode.setValueAt(TNoRM.getText(), r, 1);
                    tabMode.setValueAt(TPasien.getText(), r, 2);
                    tabMode.setValueAt(Umur.getText(), r, 3);
                    tabMode.setValueAt(JK.getText(), r, 4);
                    tabMode.setValueAt(tgl, r, 5);
                    tabMode.setValueAt(cmbSkor1.getSelectedItem().toString(), r, 6);
                    tabMode.setValueAt(Skor1.getText(), r, 7);
                    tabMode.setValueAt(cmbSkor2.getSelectedItem().toString(), r, 8);
                    tabMode.setValueAt(Skor2.getText(), r, 9);
                    tabMode.setValueAt(cmbSkor3.getSelectedItem().toString(), r, 10);
                    tabMode.setValueAt(Skor3.getText(), r, 11);
                    tabMode.setValueAt(cmbSkor4.getSelectedItem().toString(), r, 12);
                    tabMode.setValueAt(Skor4.getText(), r, 13);
                    tabMode.setValueAt(cmbSkor5.getSelectedItem().toString(), r, 14);
                    tabMode.setValueAt(Skor5.getText(), r, 15);
                    tabMode.setValueAt(cmbSkor6.getSelectedItem().toString(), r, 16);
                    tabMode.setValueAt(Skor6.getText(), r, 17);
                    tabMode.setValueAt(cmbSkor7.getSelectedItem().toString(), r, 18);
                    tabMode.setValueAt(Skor7.getText(), r, 19);
                    tabMode.setValueAt(TotalSkor.getText(), r, 20);
                    tabMode.setValueAt(ParameterSkor.getText(), r, 21);
                    tabMode.setValueAt(KdPetugas.getText(), r, 22);
                    tabMode.setValueAt(NmPetugas.getText(), r, 23);
                    tabMode.setValueAt(TglLahir.getText(), r, 24);
                    break;
                }
            }
            if (lblStatusSimpanInfo != null) {
                lblStatusSimpanInfo.setText("Diperbarui (" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + " WIB)");
            }
            setModeInputBaru();
            tampilBaganMatrix();
        }
    }

    private void hapus() {
        String targetRw = isEditMode && !editNoRawat.isEmpty()
                ? editNoRawat
                : TNoRw.getText().trim();
        String targetTgl = isEditMode && !editTanggalSql.isEmpty()
                ? editTanggalSql
                : (selectedSlotIndex >= 0 && selectedSlotIndex < activeObsColumns.size() ? activeObsColumns.get(selectedSlotIndex).tanggalSql : "");

        if (targetRw.isEmpty() || targetTgl.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pilih kolom pada bagan observasi yang ingin dihapus terlebih dahulu!");
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus data observasi tanggal " + targetTgl + "?",
                "Konfirmasi Hapus Observasi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }

        if (Sequel.queryu2tf("delete from pemantauan_pews_dewasa where tanggal=? and no_rawat=?", 2, new String[]{
            targetTgl, targetRw
        })) {
            Sequel.queryu2tf("delete from pemantauan_pews_dewasa_konfirmasi where tanggal=? and no_rawat=?", 2, new String[]{
                targetTgl, targetRw
            });
            for (int i = 0; i < tabMode.getRowCount(); i++) {
                if (targetRw.equals(tabMode.getValueAt(i, 0).toString())
                        && targetTgl.equals(tabMode.getValueAt(i, 5).toString())) {
                    tabMode.removeRow(i);
                    break;
                }
            }
            int cnt = tabMode.getRowCount();
            LCount.setText("" + cnt);
            setModeInputBaru();
            tampilBaganMatrix();
            JOptionPane.showMessageDialog(this, "Data observasi berhasil dihapus.");
        } else {
            JOptionPane.showMessageDialog(null, "Gagal menghapus data!");
        }
    }

    private void muatTambahanRM301A(String noRawat, String tanggal) {
        try {
            PreparedStatement ps2 = koneksi.prepareStatement(
                    "select aliran_oksigen, alat_oksigen, gula_darah from pemantauan_pews_dewasa where no_rawat=? and tanggal=?");
            try {
                ps2.setString(1, noRawat);
                ps2.setString(2, tanggal);
                ResultSet r = ps2.executeQuery();
                try {
                    if (r.next()) {
                        AliranOksigen.setText(r.getString("aliran_oksigen") == null ? "" : r.getString("aliran_oksigen"));
                        AlatOksigen.setSelectedItem(r.getString("alat_oksigen") == null ? "Tanpa Alat" : r.getString("alat_oksigen"));
                        GulaDarah.setText(r.getString("gula_darah") == null ? "" : r.getString("gula_darah"));
                    }
                } finally {
                    r.close();
                }
            } finally {
                ps2.close();
            }
        } catch (Exception e) {
            System.out.println("Notif muatTambahanRM301A : " + e);
        }
    }

    private void getData() {
        int r = tbObat.getSelectedRow();
        if (r != -1) {
            isUpdatingFromCode = true;
            try {
                String nr = tbObat.getValueAt(r, 0).toString();
                boolean gantiPasien = !nr.equals(TNoRw.getText().trim());
                TNoRw.setText(nr);
                TNoRM.setText(tbObat.getValueAt(r, 1).toString());
                TPasien.setText(tbObat.getValueAt(r, 2).toString());
                Umur.setText(tbObat.getValueAt(r, 3).toString());
                JK.setText(tbObat.getValueAt(r, 4).toString());

                txtRR.setText("");
                txtSpO2.setText("");
                txtSistol.setText("");
                txtNadi.setText("");
                txtSuhu.setText("");

                cmbSkor1.setSelectedItem(tbObat.getValueAt(r, 6).toString());
                Skor1.setText(tbObat.getValueAt(r, 7).toString());
                cmbSkor2.setSelectedItem(tbObat.getValueAt(r, 8).toString());
                Skor2.setText(tbObat.getValueAt(r, 9).toString());
                cmbSkor3.setSelectedItem(tbObat.getValueAt(r, 10).toString());
                Skor3.setText(tbObat.getValueAt(r, 11).toString());
                cmbSkor4.setSelectedItem(tbObat.getValueAt(r, 12).toString());
                Skor4.setText(tbObat.getValueAt(r, 13).toString());
                cmbSkor5.setSelectedItem(tbObat.getValueAt(r, 14).toString());
                Skor5.setText(tbObat.getValueAt(r, 15).toString());
                cmbSkor6.setSelectedItem(tbObat.getValueAt(r, 16).toString());
                Skor6.setText(tbObat.getValueAt(r, 17).toString());
                cmbSkor7.setSelectedItem(tbObat.getValueAt(r, 18).toString());
                Skor7.setText(tbObat.getValueAt(r, 19).toString());

                TotalSkor.setText(tbObat.getValueAt(r, 20).toString());
                ParameterSkor.setText(tbObat.getValueAt(r, 21).toString());

                KdPetugas.setText(tbObat.getValueAt(r, 22).toString());
                NmPetugas.setText(tbObat.getValueAt(r, 23).toString());
                txtInisialPetugas.setText(getInisial(NmPetugas.getText()));
                TglLahir.setText(tbObat.getValueAt(r, 24).toString());

                String tglStr = tbObat.getValueAt(r, 5).toString();
                Valid.SetTgl(Tanggal, tglStr);
                if (tglStr.length() >= 19) {
                    Jam.setSelectedItem(tglStr.substring(11, 13));
                    Menit.setSelectedItem(tglStr.substring(14, 16));
                    Detik.setSelectedItem(tglStr.substring(17, 19));
                }

                muatTambahanRM301A(nr, tglStr);
                if (gantiPasien) {
                    isRawat();
                }
            } finally {
                isUpdatingFromCode = false;
            }
            syncScoreFromCombo();
            tampilBaganMatrix();
            String tglStr = tbObat.getValueAt(r, 5).toString();
            for (int i = 0; i < activeObsColumns.size(); i++) {
                if (activeObsColumns.get(i).tanggalSql != null && activeObsColumns.get(i).tanggalSql.equals(tglStr)) {
                    selectedSlotIndex = i;
                    activeSheetIndex = i / KOLOM_RM301A;
                    updateSlotGridHighlight();
                    if (matrixTable != null) matrixTable.repaint();
                    break;
                }
            }
        }
    }

    private void isRawat() {
        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, date_format(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir, "
                    + "date_format(reg_periksa.tgl_registrasi,'%d-%m-%Y') as tgl_reg_fmt, reg_periksa.tgl_registrasi, "
                    + "reg_periksa.umurdaftar, reg_periksa.sttsumur, reg_periksa.jam_reg "
                    + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    JK.setText(rs.getString("jk"));
                    Umur.setText(rs.getString("umurdaftar") + " " + rs.getString("sttsumur"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    TanggalRegistrasi.setText(rs.getString("tgl_registrasi") + " " + rs.getString("jam_reg"));

                    // Update Kartu Identitas Pasien di Side Rail Kiri
                    lblPasienNama.setText(rs.getString("nm_pasien"));
                    String gender = rs.getString("jk").equalsIgnoreCase("L") ? "Laki-laki" : "Perempuan";
                    lblPasienJKUmur.setText(gender + " \u2022 " + rs.getString("umurdaftar") + " " + rs.getString("sttsumur"));
                    lblPasienRM.setText(rs.getString("no_rkm_medis"));
                    lblPasienLahir.setText(rs.getString("tgl_lahir") + " (" + rs.getString("umurdaftar") + " " + rs.getString("sttsumur") + ")");
                    lblPasienMasuk.setText(rs.getString("tgl_reg_fmt"));
                    lblPasienRw.setText(TNoRw.getText());
                    lblAvatar.setText(getInisial(rs.getString("nm_pasien")));

                    String lokasi = getLokasiPasien(TNoRw.getText());
                    lblPasienLokasi.setText(lokasi);

                    if (lblActivePatientBanner != null) {
                        lblActivePatientBanner.setText("Pasien Terpilih: " + rs.getString("nm_pasien")
                                + " (No. RM: " + rs.getString("no_rkm_medis")
                                + " \u2022 No. Rawat: " + TNoRw.getText()
                                + " \u2022 " + lokasi + ")");
                    }
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif isRawat: " + e);
        }
    }

    private String getLokasiPasien(String noRawat) {
        String kamar = Sequel.cariIsi(
                "select concat(bangsal.nm_bangsal, ' / ', kamar.kd_kamar) from kamar_inap "
                + "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "
                + "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "
                + "where kamar_inap.no_rawat=? and kamar_inap.stts_pulang='-' order by kamar_inap.tgl_masuk desc limit 1",
                noRawat);
        if (kamar == null || kamar.trim().isEmpty()) {
            kamar = Sequel.cariIsi(
                    "select poliklinik.nm_poli from reg_periksa inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli where reg_periksa.no_rawat=?",
                    noRawat);
        }
        return (kamar == null || kamar.trim().isEmpty()) ? "-" : kamar;
    }

    private String getInisial(String nama) {
        if (nama == null || nama.trim().isEmpty()) return "PS";
        StringBuilder b = new StringBuilder();
        for (String kata : nama.trim().split("\\s+")) {
            String bersih = kata.replaceAll("[^A-Za-z]", "");
            if (!bersih.isEmpty() && !bersih.equalsIgnoreCase("Ns") && !bersih.equalsIgnoreCase("dr") && b.length() < 2) {
                b.append(Character.toUpperCase(bersih.charAt(0)));
            }
        }
        if (b.length() == 0) {
            for (String kata : nama.trim().split("\\s+")) {
                if (!kata.isEmpty() && b.length() < 2) {
                    b.append(Character.toUpperCase(kata.charAt(0)));
                }
            }
        }
        return b.length() == 0 ? "PS" : b.toString();
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            if (TCari.getText().trim().isEmpty()) {
                ps = koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"
                        + "pasien.jk,pemantauan_pews_dewasa.tanggal,pemantauan_pews_dewasa.parameter_laju_respirasi,pemantauan_pews_dewasa.skor_laju_respirasi,pemantauan_pews_dewasa.parameter_saturasi_oksigen,"
                        + "pemantauan_pews_dewasa.skor_saturasi_oksigen,pemantauan_pews_dewasa.parameter_suplemen_oksigen,pemantauan_pews_dewasa.skor_suplemen_oksigen,pemantauan_pews_dewasa.parameter_tekanan_darah_sistolik,"
                        + "pemantauan_pews_dewasa.skor_tekanan_darah_sistolik,pemantauan_pews_dewasa.parameter_laju_jantung,pemantauan_pews_dewasa.skor_laju_jantung,pemantauan_pews_dewasa.parameter_kesadaran,"
                        + "pemantauan_pews_dewasa.skor_kesadaran,pemantauan_pews_dewasa.parameter_temperatur,pemantauan_pews_dewasa.skor_temperatur,pemantauan_pews_dewasa.skor_total,pemantauan_pews_dewasa.parameter_total,pemantauan_pews_dewasa.nip,petugas.nama,date_format(pasien.tgl_lahir,'%d-%m-%Y') as lahir "
                        + "from pemantauan_pews_dewasa inner join reg_periksa on pemantauan_pews_dewasa.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join petugas on pemantauan_pews_dewasa.nip=petugas.nip where "
                        + "pemantauan_pews_dewasa.tanggal between ? and ? order by pemantauan_pews_dewasa.tanggal");
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
            } else {
                ps = koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"
                        + "pasien.jk,pemantauan_pews_dewasa.tanggal,pemantauan_pews_dewasa.parameter_laju_respirasi,pemantauan_pews_dewasa.skor_laju_respirasi,pemantauan_pews_dewasa.parameter_saturasi_oksigen,"
                        + "pemantauan_pews_dewasa.skor_saturasi_oksigen,pemantauan_pews_dewasa.parameter_suplemen_oksigen,pemantauan_pews_dewasa.skor_suplemen_oksigen,pemantauan_pews_dewasa.parameter_tekanan_darah_sistolik,"
                        + "pemantauan_pews_dewasa.skor_tekanan_darah_sistolik,pemantauan_pews_dewasa.parameter_laju_jantung,pemantauan_pews_dewasa.skor_laju_jantung,pemantauan_pews_dewasa.parameter_kesadaran,"
                        + "pemantauan_pews_dewasa.skor_kesadaran,pemantauan_pews_dewasa.parameter_temperatur,pemantauan_pews_dewasa.skor_temperatur,pemantauan_pews_dewasa.skor_total,pemantauan_pews_dewasa.parameter_total,pemantauan_pews_dewasa.nip,petugas.nama,date_format(pasien.tgl_lahir,'%d-%m-%Y') as lahir "
                        + "from pemantauan_pews_dewasa inner join reg_periksa on pemantauan_pews_dewasa.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join petugas on pemantauan_pews_dewasa.nip=petugas.nip "
                        + "where pemantauan_pews_dewasa.tanggal between ? and ? and "
                        + "(reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or pemantauan_pews_dewasa.nip like ? or petugas.nama like ?) "
                        + "order by pemantauan_pews_dewasa.tanggal");
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                ps.setString(3, "%" + TCari.getText() + "%");
                ps.setString(4, "%" + TCari.getText() + "%");
                ps.setString(5, "%" + TCari.getText() + "%");
                ps.setString(6, "%" + TCari.getText() + "%");
                ps.setString(7, "%" + TCari.getText() + "%");
            }

            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"), rs.getString("no_rkm_medis"), rs.getString("nm_pasien"),
                        rs.getString("umurdaftar") + " " + rs.getString("sttsumur"), rs.getString("jk"),
                        rs.getString("tanggal"), rs.getString("parameter_laju_respirasi"), rs.getString("skor_laju_respirasi"),
                        rs.getString("parameter_saturasi_oksigen"), rs.getString("skor_saturasi_oksigen"), rs.getString("parameter_suplemen_oksigen"),
                        rs.getString("skor_suplemen_oksigen"), rs.getString("parameter_tekanan_darah_sistolik"), rs.getString("skor_tekanan_darah_sistolik"),
                        rs.getString("parameter_laju_jantung"), rs.getString("skor_laju_jantung"), rs.getString("parameter_kesadaran"),
                        rs.getString("skor_kesadaran"), rs.getString("parameter_temperatur"), rs.getString("skor_temperatur"), rs.getString("skor_total"),
                        rs.getString("parameter_total"), rs.getString("nip"), rs.getString("nama"), rs.getString("lahir")
                    });
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tampil: " + e);
        }
        int cnt = tabMode.getRowCount();
        LCount.setText("" + cnt);
        tampilBaganMatrix();
    }

    /**
     * Memuat seluruh kolom observasi pasien dari database dan merender matriks 47 baris RM 30-1A
     * dengan scroll horizontal serta pratinjau HTML F4 secara real-time.
     */
    public void tampilBaganMatrix() {
        activeObsColumns.clear();
        if (!TNoRw.getText().trim().isEmpty()) {
            try {
                PreparedStatement psMat = koneksi.prepareStatement(
                        "select p.tanggal as tgl_sql, date_format(p.tanggal,'%d/%m') as tgl_short, date_format(p.tanggal,'%H:%i') as jam_short, "
                        + "p.parameter_laju_respirasi, p.skor_laju_respirasi, p.parameter_saturasi_oksigen, p.skor_saturasi_oksigen, "
                        + "p.parameter_suplemen_oksigen, p.skor_suplemen_oksigen, p.parameter_tekanan_darah_sistolik, p.skor_tekanan_darah_sistolik, "
                        + "p.parameter_laju_jantung, p.skor_laju_jantung, p.parameter_kesadaran, p.skor_kesadaran, "
                        + "p.parameter_temperatur, p.skor_temperatur, p.skor_total, p.parameter_total, "
                        + "ifnull(p.aliran_oksigen,'') as aliran, ifnull(p.alat_oksigen,'') as alat, "
                        + "ifnull(p.gula_darah,'') as gula, ifnull(p.nip,'') as nip, ifnull(g.nama,'') as petugas, "
                        + "ifnull(k.status_petugas,'Belum Validasi') as status_tte, "
                        + "ifnull(date_format(ifnull(k.tgl_validasi_dokter, k.tgl_validasi_petugas),'%d-%m-%Y %H:%i'),'-') as waktu_tte "
                        + "from pemantauan_pews_dewasa p "
                        + "left join petugas g on p.nip=g.nip "
                        + "left join pemantauan_pews_dewasa_konfirmasi k on p.no_rawat=k.no_rawat and p.tanggal=k.tanggal "
                        + "where p.no_rawat=? order by p.tanggal asc");
                try {
                    psMat.setString(1, TNoRw.getText().trim());
                    ResultSet rMat = psMat.executeQuery();
                    try {
                        while (rMat.next()) {
                            ObsColumnData d = new ObsColumnData();
                            d.tanggalSql = rMat.getString("tgl_sql");
                            d.tglPendek = rMat.getString("tgl_short");
                            d.jamPendek = rMat.getString("jam_short");
                            d.rrBand = rMat.getString("parameter_laju_respirasi");
                            d.rrSkor = rMat.getString("skor_laju_respirasi");
                            d.spo2Band = rMat.getString("parameter_saturasi_oksigen");
                            d.spo2Skor = rMat.getString("skor_saturasi_oksigen");
                            d.o2Suplemen = rMat.getString("parameter_suplemen_oksigen");
                            d.o2Skor = rMat.getString("skor_suplemen_oksigen");
                            d.sbpBand = rMat.getString("parameter_tekanan_darah_sistolik");
                            d.sbpSkor = rMat.getString("skor_tekanan_darah_sistolik");
                            d.hrBand = rMat.getString("parameter_laju_jantung");
                            d.hrSkor = rMat.getString("skor_laju_jantung");
                            d.avpuBand = rMat.getString("parameter_kesadaran");
                            d.avpuSkor = rMat.getString("skor_kesadaran");
                            d.tempBand = rMat.getString("parameter_temperatur");
                            d.tempSkor = rMat.getString("skor_temperatur");
                            d.totalSkor = rMat.getString("skor_total");
                            d.kesimpulan = rMat.getString("parameter_total");
                            d.aliranO2 = rMat.getString("aliran");
                            d.alatO2 = rMat.getString("alat");
                            d.gulaDarah = rMat.getString("gula");
                            d.nip = rMat.getString("nip");
                            d.namaPetugas = rMat.getString("petugas");
                            d.inisial = getInisial(d.namaPetugas);
                            d.statusTte = rMat.getString("status_tte");
                            d.waktuTte = rMat.getString("waktu_tte");
                            activeObsColumns.add(d);
                        }
                    } finally {
                        rMat.close();
                    }
                } finally {
                    psMat.close();
                }
            } catch (Exception e) {
                System.out.println("Notif tampilBaganMatrix: " + e);
            }
        }

        if (lblSavedBadge != null) {
            if (!activeObsColumns.isEmpty()) {
                lblSavedBadge.setText("Tersimpan (" + activeObsColumns.size() + ")");
                lblSavedBadge.setBackground(COL_EMERALD_BG);
                lblSavedBadge.setForeground(COL_EMERALD_TXT);
            } else {
                lblSavedBadge.setText("Belum Ada Data");
                lblSavedBadge.setBackground(COL_BG_SLATE);
                lblSavedBadge.setForeground(COL_TEXT_MUTED);
            }
        }

        updateSlotGridHighlight();
        refreshMatrixRows();

        if (LoadHTML != null) {
            LoadHTML.setText(buildOfficialPrintHTML());
            LoadHTML.setCaretPosition(0);
        }
    }

    private void refreshMatrixRows() {
        if (matrixModel == null || matrixTable == null) return;
        int totalObs = activeObsColumns.size();
        int numCols = Math.max(KOLOM_RM301A, totalObs);
        int totalTableCols = 2 + numCols + 1;

        if (matrixModel.getColumnCount() != totalTableCols) {
            String[] colNames = new String[totalTableCols];
            colNames[0] = "PARAMETER";
            colNames[1] = "RENTANG NILAI";
            for (int i = 0; i < numCols; i++) {
                colNames[i + 2] = "#" + (i + 1);
            }
            colNames[totalTableCols - 1] = "SKOR";
            matrixModel.setColumnIdentifiers(colNames);

            for (int c = 0; c < totalTableCols; c++) {
                matrixTable.getColumnModel().getColumn(c).setCellRenderer(matrixRenderer);
            }
            matrixTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            matrixTable.getColumnModel().getColumn(1).setPreferredWidth(125);
            for (int i = 0; i < numCols; i++) {
                matrixTable.getColumnModel().getColumn(i + 2).setPreferredWidth(58);
            }
            matrixTable.getColumnModel().getColumn(totalTableCols - 1).setPreferredWidth(52);
        }

        matrixModel.setRowCount(0);
        for (MatrixRowSpec spec : matrixRowSpecs) {
            Object[] row = new Object[totalTableCols];
            row[0] = spec.groupName;
            row[1] = spec.label;
            for (int c = 0; c < numCols; c++) {
                ObsColumnData obs = (c < totalObs) ? activeObsColumns.get(c) : null;
                row[c + 2] = resolveMatrixCellText(spec, obs);
            }
            row[totalTableCols - 1] = spec.scoreText;
            matrixModel.addRow(row);
        }
    }

    private String resolveMatrixCellText(MatrixRowSpec spec, ObsColumnData obs) {
        if (obs == null) return "";
        switch (spec.param) {
            case "meta_date":
                return obs.tglPendek != null ? obs.tglPendek : "";
            case "meta_time":
                return obs.jamPendek != null ? obs.jamPendek : "";
            case "rr":
                return matchBandRR(spec.band, obs.rrBand) ? "\u25CF" : "";
            case "spo2":
                return matchBandSpO2(spec.band, obs.spo2Band) ? "\u25CF" : "";
            case "o2_lpm":
                if (obs.aliranO2 != null && !obs.aliranO2.trim().isEmpty() && !"0".equals(obs.aliranO2.trim())) {
                    return obs.aliranO2.trim() + " L";
                }
                return "Ya".equalsIgnoreCase(obs.o2Suplemen) ? "Ya" : "-";
            case "o2_dev":
                if (obs.alatO2 == null || obs.alatO2.trim().isEmpty() || "Tanpa Alat".equalsIgnoreCase(obs.alatO2.trim())) {
                    return "Tanpa";
                }
                return obs.alatO2.trim();
            case "sbp":
                return matchBandSBP(spec.band, obs.sbpBand) ? "\u25CF" : "";
            case "hr":
                return matchBandHR(spec.band, obs.hrBand) ? "\u25CF" : "";
            case "avpu":
                if ("Sadar".equals(spec.band)) {
                    return "Sadar".equalsIgnoreCase(obs.avpuBand) ? "\u2713" : "";
                }
                return (obs.avpuBand != null && !"Sadar".equalsIgnoreCase(obs.avpuBand)) ? "\u25CF" : "";
            case "temp":
                return matchBandTemp(spec.band, obs.tempBand) ? "\u25CF" : "";
            case "meta_total":
                return obs.totalSkor != null ? obs.totalSkor : "0";
            case "meta_gds":
                return (obs.gulaDarah != null && !obs.gulaDarah.trim().isEmpty()) ? obs.gulaDarah.trim() : "-";
            case "meta_init":
                return obs.inisial != null ? obs.inisial : "";
            default:
                return "";
        }
    }

    private boolean matchBandRR(String rowBand, String dbVal) {
        if (dbVal == null) return false;
        if (rowBand.equals(dbVal)) return true;
        if (">= 25".equals(rowBand) && ("25 - 34".equals(dbVal) || ">= 35".equals(dbVal))) return true;
        if ("<= 8".equals(rowBand) && ("<= 5".equals(dbVal) || "6 - 8".equals(dbVal))) return true;
        return false;
    }

    private boolean matchBandSpO2(String rowBand, String dbVal) {
        if (dbVal == null) return false;
        if (rowBand.equals(dbVal)) return true;
        if (">= 96".equals(rowBand) && ">= 95".equals(dbVal)) return true;
        if ("<= 91".equals(rowBand) && "<= 92".equals(dbVal)) return true;
        return false;
    }

    private boolean matchBandSBP(String rowBand, String dbVal) {
        if (dbVal == null) return false;
        if (rowBand.equals(dbVal)) return true;
        if ("181 - 200".equals(rowBand) && "181 - 220".equals(dbVal)) return true;
        if ("121 - 140".equals(rowBand) && "111 - 180".equals(dbVal)) return true;
        if ("81 - 90".equals(rowBand) && "71 - 90".equals(dbVal)) return true;
        return false;
    }

    private boolean matchBandHR(String rowBand, String dbVal) {
        if (dbVal == null) return false;
        if (rowBand.equals(dbVal)) return true;
        if (">= 131".equals(rowBand) && (">= 140".equals(dbVal) || "131 - 140".equals(dbVal))) return true;
        if ("111 - 120".equals(rowBand) && "111 - 130".equals(dbVal)) return true;
        if ("91 - 100".equals(rowBand) && "91 - 110".equals(dbVal)) return true;
        if ("71 - 80".equals(rowBand) && "51 - 90".equals(dbVal)) return true;
        return false;
    }

    private boolean matchBandTemp(String rowBand, String dbVal) {
        if (dbVal == null) return false;
        String normDb = dbVal.replace(',', '.');
        if (rowBand.equals(normDb)) return true;
        if (">= 39.1".equals(rowBand) && ">= 39".equals(normDb)) return true;
        if ("38.1 - 39.0".equals(rowBand) && "38.1 - 39".equals(normDb)) return true;
        if ("36.1 - 37.0".equals(rowBand) && "36.1 - 38".equals(normDb)) return true;
        if ("35.1 - 36.0".equals(rowBand) && "35.1 - 36".equals(normDb)) return true;
        if ("<= 35.0".equals(rowBand) && "<= 35".equals(normDb)) return true;
        return false;
    }

    // =========================================================================
    // 14. CETAK JASPER REPORTS (RM 30-1A) & PUBLIC CALLER CONTRACT METHODS
    // =========================================================================
    private void cetakRM301A(String noRawat) {
        String targetRw = (noRawat == null || noRawat.trim().isEmpty()) ? TNoRw.getText().trim() : noRawat.trim();
        if (targetRw.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Silakan pilih pasien terlebih dahulu!");
            return;
        }

        int totalObs = activeObsColumns.size();
        int totalSheets = Math.max(1, (int) Math.ceil((double) Math.max(1, totalObs) / KOLOM_RM301A));
        int sheetToPrint = activeSheetIndex;

        if (totalSheets > 1) {
            String[] options = new String[totalSheets + 1];
            options[0] = "Lembar Aktif: Lembar " + (activeSheetIndex + 1) + " (Kolom " + (activeSheetIndex * 12 + 1) + " - " + ((activeSheetIndex + 1) * 12) + ")";
            for (int i = 0; i < totalSheets; i++) {
                options[i + 1] = "Lembar " + (i + 1) + " (Kolom " + (i * 12 + 1) + " - " + ((i + 1) * 12) + ")";
            }
            String chosen = (String) JOptionPane.showInputDialog(
                    this,
                    "Pasien memiliki " + totalSheets + " lembar observasi (" + totalObs + " pengamatan).\nPilih lembar yang ingin dicetak ke formulir fisik F4:",
                    "Pilihan Lembar Cetak RM 30-1A (F4)",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            if (chosen == null) return;
            if (!chosen.equals(options[0])) {
                for (int i = 0; i < totalSheets; i++) {
                    if (chosen.equals(options[i + 1])) {
                        sheetToPrint = i;
                        break;
                    }
                }
            }
        }

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        param.put("norawat", targetRw);
        param.put("ews", susunPetaRM301A(targetRw, sheetToPrint));
        Valid.MyReport("rptFormulirEWSDewasaRM30.jasper", "report",
                "::[ Early Warning Scoring System Dewasa - RM 30-1A (Lembar " + (sheetToPrint + 1) + ") ]::", param);
    }

    private Map<String, Object> susunPetaRM301A(String noRawat) {
        return susunPetaRM301A(noRawat, activeSheetIndex);
    }

    private Map<String, Object> susunPetaRM301A(String noRawat, int sheetIndex) {
        Map<String, Object> ews = new HashMap<>();
        List<String[]> allRows = new ArrayList<>();
        try {
            PreparedStatement ps2 = koneksi.prepareStatement(
                    "select date_format(p.tanggal,'%d-%m-%y') as tgl,date_format(p.tanggal,'%H:%i') as jam,"
                    + "p.parameter_laju_respirasi,p.skor_laju_respirasi,p.parameter_saturasi_oksigen,p.skor_saturasi_oksigen,"
                    + "p.parameter_suplemen_oksigen,p.skor_suplemen_oksigen,p.parameter_tekanan_darah_sistolik,p.skor_tekanan_darah_sistolik,"
                    + "p.parameter_laju_jantung,p.skor_laju_jantung,p.parameter_kesadaran,p.skor_kesadaran,"
                    + "p.parameter_temperatur,p.skor_temperatur,p.skor_total,"
                    + "ifnull(p.aliran_oksigen,'') as aliran,ifnull(p.alat_oksigen,'') as alat,"
                    + "ifnull(p.gula_darah,'') as gula,ifnull(g.nama,'') as petugas "
                    + "from pemantauan_pews_dewasa p left join petugas g on p.nip=g.nip "
                    + "where p.no_rawat=? order by p.tanggal asc");
            try {
                ps2.setString(1, noRawat);
                ResultSet r = ps2.executeQuery();
                try {
                    while (r.next()) {
                        allRows.add(new String[]{
                            r.getString("tgl"), r.getString("jam"),
                            r.getString("parameter_laju_respirasi"), r.getString("skor_laju_respirasi"),
                            r.getString("parameter_saturasi_oksigen"), r.getString("skor_saturasi_oksigen"),
                            r.getString("parameter_suplemen_oksigen"), r.getString("skor_suplemen_oksigen"),
                            r.getString("parameter_tekanan_darah_sistolik"), r.getString("skor_tekanan_darah_sistolik"),
                            r.getString("parameter_laju_jantung"), r.getString("skor_laju_jantung"),
                            r.getString("parameter_kesadaran"), r.getString("skor_kesadaran"),
                            r.getString("parameter_temperatur"), r.getString("skor_temperatur"),
                            r.getString("skor_total"), r.getString("aliran"), r.getString("alat"),
                            r.getString("gula"), getInisial(r.getString("petugas"))
                        });
                    }
                } finally {
                    r.close();
                }
            } finally {
                ps2.close();
            }
        } catch (Exception e) {
            System.out.println("Notif cetakRM301A : " + e);
        }

        int startIdx = sheetIndex * KOLOM_RM301A;
        for (int i = 0; i < KOLOM_RM301A; i++) {
            int actualIdx = startIdx + i;
            int c = i + 1;
            if (actualIdx < allRows.size()) {
                String[] b = allRows.get(actualIdx);
                ews.put("tgl" + c, b[0]);
                ews.put("jam" + c, b[1]);
                ews.put("p1_" + c, b[2]);
                ews.put("s1_" + c, b[3]);
                ews.put("p2_" + c, b[4]);
                ews.put("s2_" + c, b[5]);
                ews.put("p3_" + c, b[6]);
                ews.put("s3_" + c, b[7]);
                ews.put("p4_" + c, b[8]);
                ews.put("s4_" + c, b[9]);
                ews.put("p5_" + c, b[10]);
                ews.put("s5_" + c, b[11]);
                ews.put("p6_" + c, b[12]);
                ews.put("s6_" + c, b[13]);
                ews.put("p7_" + c, b[14]);
                ews.put("s7_" + c, b[15]);
                ews.put("total" + c, b[16]);
                ews.put("aliran" + c, b[17]);
                ews.put("alat" + c, b[18]);
                ews.put("gd" + c, b[19]);
                ews.put("ini" + c, b[20]);
            } else {
                ews.put("tgl" + c, "");
                ews.put("jam" + c, "");
                ews.put("p1_" + c, "");
                ews.put("s1_" + c, "");
                ews.put("p2_" + c, "");
                ews.put("s2_" + c, "");
                ews.put("p3_" + c, "");
                ews.put("s3_" + c, "");
                ews.put("p4_" + c, "");
                ews.put("s4_" + c, "");
                ews.put("p5_" + c, "");
                ews.put("s5_" + c, "");
                ews.put("p6_" + c, "");
                ews.put("s6_" + c, "");
                ews.put("p7_" + c, "");
                ews.put("s7_" + c, "");
                ews.put("total" + c, "");
                ews.put("aliran" + c, "");
                ews.put("alat" + c, "");
                ews.put("gd" + c, "");
                ews.put("ini" + c, "");
            }
        }
        return ews;
    }

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        isRawat();
        runBackground(this::tampil);
    }

    public void isCek() {
        boolean boleh = akses.getpemantauan_pews_dewasa();
        BtnSimpan.setEnabled(boleh);
        BtnHapus.setEnabled(boleh);
        BtnEdit.setEnabled(boleh);
        BtnPrint.setEnabled(boleh);
        if (btnSimpanForm != null) btnSimpanForm.setEnabled(boleh);
        if (akses.getjml2() >= 1) {
            KdPetugas.setEditable(false);
            btnPetugas.setEnabled(false);
            KdPetugas.setText(akses.getkode());
            NmPetugas.setText(Sequel.CariPetugas(KdPetugas.getText()));
            txtInisialPetugas.setText(getInisial(NmPetugas.getText()));
            if (NmPetugas.getText().isEmpty()) {
                KdPetugas.setText("");
                JOptionPane.showMessageDialog(null, "User login bukan petugas...!!");
            }
        }
    }

    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) {
            task.run();
            return;
        }

        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            ceksukses = false;
        }
    }

    @Override
    public void dispose() {
        if (timerJam != null) {
            timerJam.stop();
        }
        executor.shutdownNow();
        super.dispose();
    }

    // =========================================================================
    // 15. HELPER UI BUILDERS & KOMPONEN BORDER ROUNDED (STANDAR ARGON)
    // =========================================================================
    private widget.TextBox createTextBox(int maxLen, boolean editable) {
        return new RoundedTextBox(maxLen, editable, 8);
    }

    private void styleComboBox(widget.ComboBox cmb, int prefWidth) {
        cmb.setFont(FONT_INPUT);
        cmb.setBackground(Color.WHITE);
        cmb.putClientProperty("Component.arc", 8);
        cmb.putClientProperty("ComboBox.arc", 8);
        cmb.setBorder(new CompoundBorder(
                new RoundedBorder(COL_BORDER, 8, 1.0f),
                new EmptyBorder(1, 4, 1, 4)));
        cmb.setPreferredSize(new Dimension(prefWidth, 28));
    }

    private widget.Tanggal styleTanggal(widget.Tanggal tgl) {
        if (tgl == null) return null;
        tgl.setFont(FONT_INPUT);
        tgl.setForeground(COL_TEXT_DARK);
        tgl.setBackground(Color.WHITE);
        tgl.putClientProperty("Component.arc", 8);
        tgl.putClientProperty("ComboBox.arc", 8);
        tgl.setBorder(new CompoundBorder(
                new RoundedBorder(COL_BORDER, 8, 1.0f),
                new EmptyBorder(0, 3, 0, 3)));
        return tgl;
    }

    private JLabel createNumberBadge(String code) {
        JLabel numBadge = new JLabel(code, SwingConstants.CENTER) {
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
        return numBadge;
    }

    private JLabel createPillBadge(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setOpaque(false);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(3, 8, 3, 8));
        return lbl;
    }

    private JLabel createScoreBadge() {
        JLabel l = new JLabel("Skor 0", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setOpaque(false);
        l.setFont(new Font("Consolas", Font.BOLD, 11));
        l.setBackground(COL_BG_SLATE);
        l.setForeground(COL_HEADER_DARK);
        l.setBorder(new EmptyBorder(2, 7, 2, 7));
        return l;
    }

    private void styleScoreBadge(JLabel badge, String scoreStr) {
        if (badge == null) return;
        int s = 0;
        try {
            s = Integer.parseInt(scoreStr.trim());
        } catch (Exception ignored) {}
        badge.setText("Skor " + s);
        if (s >= 3) {
            badge.setBackground(EWS_RED);
            badge.setForeground(Color.WHITE);
        } else if (s == 2) {
            badge.setBackground(EWS_ORANGE);
            badge.setForeground(Color.WHITE);
        } else if (s == 1) {
            badge.setBackground(EWS_YELLOW);
            badge.setForeground(COL_TEXT_DARK);
        } else {
            badge.setBackground(COL_BG_SLATE);
            badge.setForeground(COL_HEADER_DARK);
        }
        badge.repaint();
    }

    private JLabel createMiniBreakPill(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setOpaque(false);
        l.setFont(new Font("Consolas", Font.BOLD, 10));
        l.setBackground(COL_BG_SLATE);
        l.setForeground(COL_TEXT_DARK);
        l.setBorder(new EmptyBorder(3, 4, 3, 4));
        return l;
    }

    private void styleMiniBreakPill(JLabel pill, String label, String scoreStr) {
        if (pill == null) return;
        int s = 0;
        try {
            s = Integer.parseInt(scoreStr.trim());
        } catch (Exception ignored) {}
        pill.setText(label + ": " + s);
        if (s >= 3) {
            pill.setBackground(EWS_RED);
            pill.setForeground(Color.WHITE);
        } else if (s == 2) {
            pill.setBackground(EWS_ORANGE);
            pill.setForeground(Color.WHITE);
        } else if (s == 1) {
            pill.setBackground(EWS_YELLOW);
            pill.setForeground(COL_TEXT_DARK);
        } else {
            pill.setBackground(COL_BG_SLATE);
            pill.setForeground(COL_TEXT_DARK);
        }
        pill.repaint();
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

    private JPanel createLegendRow(String badgeText, String descText, Color badgeBg, Color badgeFg) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(COL_CARD_HEADER);
        row.setBorder(new CompoundBorder(
                new RoundedBorder(COL_BORDER_DIVIDER, 6, 1.0f),
                new EmptyBorder(3, 6, 3, 6)));

        JLabel b = new JLabel(badgeText, SwingConstants.CENTER);
        b.setOpaque(true);
        b.setBackground(badgeBg);
        b.setForeground(badgeFg);
        b.setFont(new Font("Consolas", Font.BOLD, 10));
        b.setPreferredSize(new Dimension(68, 20));
        b.setBorder(new LineBorder(COL_BORDER, 1, true));

        JLabel d = new JLabel(descText);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        d.setForeground(COL_TEXT_DARK);

        row.add(b, BorderLayout.WEST);
        row.add(d, BorderLayout.CENTER);
        return row;
    }

    private JPanel createLookupFieldPanel(widget.TextBox tf, Runnable onLookupClick) {
        JButton btn = new JButton(new SearchMagnifierIcon(13, COL_TEXT_MUTED)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (Boolean.TRUE.equals(getClientProperty("hover"))) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COL_TEAL_SOFT);
                    g2.fill(new RoundRectangle2D.Float(2f, 2f, getWidth() - 4f, getHeight() - 4f, 6, 6));
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Pilih Petugas dari Master Petugas");
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
                int divX = getWidth() - 30;
                g2.drawLine(divX, 4, divX, getHeight() - 4);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tf.hasFocus() ? COL_TEAL_PRIMARY : COL_BORDER);
                g2.setStroke(new BasicStroke(tf.hasFocus() ? 1.5f : 1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(180, 28));
        tf.setOpaque(false);
        tf.setBorder(new EmptyBorder(4, 8, 4, 4));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(4, 6, 4, 8));

        p.add(tf, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    private JButton createFlatButton(String text, Icon icon, Color bg, Color fg, Color borderCol) {
        RoundedButton btn = new RoundedButton(text, bg, fg, borderCol, 8);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(6);
        }
        return btn;
    }

    private widget.Button createRoundedWidgetButton(String text, Color bg, Color fg, Color border) {
        widget.Button btn = new widget.Button();
        btn.setText(text);
        btn.setFont(FONT_SECTION);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new RoundedBorder(border, 8, 1.0f),
                new EmptyBorder(3, 10, 3, 10)));
        btn.setRolloverEnabled(true);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (bg.equals(COL_TEAL_PRIMARY)) {
                    btn.setBackground(COL_TEAL_DARK);
                } else if (bg.equals(Color.WHITE)) {
                    btn.setBackground(COL_BG_SLATE);
                } else {
                    btn.setBackground(blendColor(bg, Color.WHITE, 0.22f));
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
        if (btn == null) return;
        btn.setPreferredSize(null);
        Dimension d = btn.getPreferredSize();
        int targetW = Math.max(d.width + 16, minWidth);
        btn.setPreferredSize(new Dimension(targetW, 32));
        btn.revalidate();
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
    // KELAS KOMPONEN BORDER ROUNDED RESMI (ARC 12px / 8px / 6px + HOVER)
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
            int p = Math.max(3, (int) Math.ceil(strokeWidth) + 1);
            return new Insets(p, p, p, p);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    public static class RoundedTextBox extends widget.TextBox {
        private final int arc;
        private final Color borderColor = COL_BORDER;

        public RoundedTextBox(int maxLen, boolean editable, int arc) {
            super();
            this.arc = arc;
            setEditable(editable);
            setOpaque(false);
            setFont(FONT_INPUT);
            setForeground(COL_TEXT_DARK);
            setBackground(editable ? Color.WHITE : new Color(248, 250, 252));
            setBorder(new EmptyBorder(4, 8, 4, 8));
            setPreferredSize(new Dimension(getPreferredSize().width, 28));
            putClientProperty("Component.arc", arc);
            putClientProperty("TextComponent.arc", arc);
            if (maxLen > 0) {
                setDocument(new batasInput((byte) maxLen).getKata(this));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEditable() ? (getBackground() != null ? getBackground() : Color.WHITE) : new Color(248, 250, 252));
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
        private final Color borderColor = COL_BORDER_DIVIDER;

        public RoundedCardPanel(int arc) {
            this.arc = arc;
            setOpaque(false);
            setBackground(COL_CARD_BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground() != null ? getBackground() : COL_CARD_BG);
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth() - 1.2f, getHeight() - 1.2f, arc, arc));
            g2.dispose();
        }
    }

    public static class RoundedScrollPane extends JScrollPane {
        private final int arc;

        public RoundedScrollPane(Component view, int arc) {
            super(view);
            this.arc = arc;
            setOpaque(false);
            getViewport().setOpaque(true);
            getViewport().setBackground(Color.WHITE);
            getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
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
            g2.setColor(COL_BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc));
            g2.dispose();
        }
    }

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

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(getBackground() != null ? getBackground() : COL_BG_SLATE);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public static class RoundedButton extends JButton {
        private final int arc;
        private final Color normalBg;
        private final Color borderCol;
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
            setBorder(new EmptyBorder(6, 12, 6, 12));
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
            if (c == null) return true;
            return c.getRed() > 215 && c.getGreen() > 215 && c.getBlue() > 215;
        }

        private boolean isSoftTeal(Color c) {
            if (c == null) return false;
            return c.getRed() >= 230 && c.getGreen() >= 245 && c.getBlue() >= 240;
        }

        private boolean isRedTone(Color c) {
            if (c == null) return false;
            return c.getRed() > 180 && c.getGreen() < 120 && c.getBlue() < 130;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = (isHovered || getModel().isRollover()) && isEnabled();
            if (!isEnabled()) {
                g2.setColor(COL_BG_SLATE);
            } else if (getModel().isPressed()) {
                g2.setColor(normalBg != null ? normalBg.darker() : Color.LIGHT_GRAY);
            } else if (hover) {
                if (isSoftTeal(normalBg)) {
                    g2.setColor(new Color(204, 251, 241));
                } else if (isLightColor(normalBg)) {
                    if (isRedTone(getForeground()) || isRedTone(borderCol)) {
                        g2.setColor(new Color(254, 242, 242));
                    } else {
                        g2.setColor(COL_BG_SLATE);
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
                    g2.setColor(COL_BORDER_DIVIDER);
                    g2.setStroke(new BasicStroke(1.0f));
                } else if (hover) {
                    if (isSoftTeal(normalBg)) {
                        g2.setColor(COL_TEAL_PRIMARY);
                        g2.setStroke(new BasicStroke(1.3f));
                    } else if (isRedTone(getForeground()) || isRedTone(borderCol)) {
                        g2.setColor(new Color(248, 113, 113));
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

    public static class SegmentedOptionButton extends JPanel {
        private final List<PillSegmentButton> buttons = new ArrayList<>();
        private String selectedValue;
        private final List<java.util.function.Consumer<String>> listeners = new ArrayList<>();

        public SegmentedOptionButton(String[] options, String defaultOption) {
            this.selectedValue = defaultOption;
            setLayout(new GridLayout(1, Math.max(1, options.length), 3, 0));
            setOpaque(true);
            setBackground(COL_BG_SLATE);
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
                b.setSelectedState(b.getText().equalsIgnoreCase(val));
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
                setBorder(new EmptyBorder(4, 8, 4, 8));
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
                setFont(sel ? new Font("Segoe UI", Font.BOLD, 11) : FONT_INPUT);
                setForeground(sel ? Color.WHITE : new Color(71, 85, 105));
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isOptionSelected) {
                    String t = getText();
                    if ("C/V/P/U".equalsIgnoreCase(t)) {
                        g2.setColor(EWS_RED);
                    } else if ("Ya".equalsIgnoreCase(t)) {
                        g2.setColor(EWS_ORANGE);
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

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMPemantauanEWSD dialog = new RMPemantauanEWSD(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }
}
