package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import widget.ThemeConfig;

/**
 * ASESMEN AWAL KEPERAWATAN DI INSTALASI RAWAT JALAN (RM 02-2 / RM 02-3)
 * RSUD Demang Sepulau Raya.
 *
 * <p>Dibangun ulang dengan desain modern Swing high-fidelity sesuai panduan
 * desain design_handoff_simrs_rm_forms/README.md dan berkas prototipe
 * Asesmen Awal Keperawatan Rawat Jalan RM02-2 Redesign.dc.html.</p>
 */
public final class RMAsesmenAwalKeperawatanRalan extends javax.swing.JDialog {

    // Palet warna resmi Design System
    private static final Color TEAL_HEADER = new Color(0x12, 0x3c, 0x3a);
    private static final Color TEAL_ACCENT = new Color(0x0d, 0x7a, 0x70);
    private static final Color TEAL_LIGHT = new Color(0xf1, 0xf7, 0xf6);
    private static final Color CHIP_ON_BG = new Color(0xe9, 0xf5, 0xf2);
    private static final Color BG_PAGE = new Color(0xe8, 0xe9, 0xe6);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(0xdc, 0xdf, 0xda);
    private static final Color PATIENT_CARD_BG = new Color(0xf7, 0xf8, 0xf5);
    private static final Color TEXT_DARK = new Color(0x1c, 0x1f, 0x1d);
    private static final Color TEXT_MUTED = new Color(0x9a, 0xa1, 0x9c);
    private static final Color TEXT_LABEL = new Color(0x3d, 0x44, 0x41);
    private static final Color TEXT_LABEL_MUTED = new Color(0x8b, 0x92, 0x8d);
    private static final Color BORDER_GRAY = new Color(0xcf, 0xd3, 0xce);
    private static final Color GREEN_BG = new Color(0xe8, 0xf3, 0xed);
    private static final Color GREEN_FG = new Color(0x1a, 0x6b, 0x4c);
    private static final Color ORANGE_BG = new Color(0xfd, 0xf3, 0xe7);
    private static final Color ORANGE_FG = new Color(0x8a, 0x5a, 0x12);
    private static final Color GRAY_TALLY_BG = new Color(0xf2, 0xf3, 0xf0);
    private static final Color GRAY_TALLY_FG = new Color(0x9a, 0xa1, 0x9c);

    /**
     * Nama kolom asesmen_awal_keperawatan_ralan di luar no_rawat,
     * urut sesuai migrasi SQL.
     */
    private static final String[] KOLOM = {
        "tanggal", "jam", "diagnosa_medis",
        "keluhan_utama", "riwayat_kesehatan",
        "keadaan_umum", "td_sistolik", "td_diastolik", "vas", "hr", "rr", "suhu",
        "kepala", "rambut", "mata", "hidung", "telinga", "mulut", "gigi", "lidah",
        "tenggorokan", "leher", "integumen",
        "payudara_ukuran_bentuk", "payudara_warna", "payudara_kelainan",
        "thorax_paru", "jantung", "abdomen", "genitalia", "anus", "muskuloskeletal",
        "crt", "otot_esd", "otot_ess", "otot_eid", "otot_eis",
        "nutrisi", "eliminasi", "istirahat", "aktivitas",
        "balita_usia_kehamilan", "balita_lahir_di", "balita_ditolong", "balita_anak_ke",
        "balita_bb_lahir", "balita_tb_lahir", "balita_bbtb_sekarang", "balita_kelainan",
        "balita_mendapat", "balita_imunisasi_dasar", "balita_imunisasi_ulang",
        "balita_membalikkan", "balita_duduk", "balita_berdiri", "balita_berjalan",
        "balita_mengoceh", "balita_berbicara",
        "repro_usia_menstruasi", "repro_siklus", "repro_interval", "repro_jumlah",
        "repro_nyeri_skala", "repro_bercak", "repro_hpht", "repro_keluhan_hamil", "repro_obat",
        "obs_g", "obs_p", "obs_o", "obs_jumlah_anak", "obs_laki", "obs_perempuan",
        "obs_anak_terkecil",
        "repro_kb", "repro_pernah",
        "palpasi_tfu", "palpasi_auskultasi", "palpasi_presentasi", "palpasi_masuk_pap",
        "janin_pergerakan", "janin_posisi", "janin_umur_kehamilan", "janin_masalah",
        "repro_informasi_lain", "repro_dirujuk",
        "psikologi", "cemas", "sosiologi", "spiritual", "kebutuhan_informasi",
        "diag_laboratorium", "diag_rontgen", "diag_ekg", "diag_usg", "diag_scanning",
        "diag_terapi_medik",
        "kontrol_rawat_inap", "kontrol_tanggal",
        "dx_oksigen", "dx_nutrisi", "dx_eliminasi", "dx_integritas_kulit",
        "dx_kurang_pengetahuan",
        "nip",
        "nyeri_canvas_gambar"
    };

    private static final String[][] REGIO = CentangRM022.REGIO;
    private static final String[][] KEBIASAAN = CentangRM022.KEBIASAAN;
    private static final String[][] PSIKOSOSIAL = CentangRM022.PSIKOSOSIAL;
    private static final String[][] BALITA_CENTANG = CentangRM022.BALITA_CENTANG;

    private static final String[][] BALITA_TONGGAK = {
        {"balita_membalikkan", "Membalikkan badan"},
        {"balita_duduk", "Duduk"},
        {"balita_berdiri", "Berdiri"},
        {"balita_berjalan", "Berjalan"},
        {"balita_mengoceh", "Mengoceh"},
        {"balita_berbicara", "Berbicara"}
    };

    private static final String[][] REPRO_CENTANG = CentangRM022.REPRO_CENTANG;

    private static final String[] PILIHAN_DIAGNOSA = {
        "dx_oksigen", "Gangguan pemenuhan kebutuhan oksigen",
        "dx_nutrisi", "Gangguan pemenuhan kebutuhan nutrisi",
        "dx_eliminasi", "Gangguan pemenuhan kebutuhan eliminasi",
        "dx_integritas_kulit", "Gangguan integritas kulit",
        "dx_kurang_pengetahuan", "Kurang pengetahuan tentang penyakit"
    };

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
    private static final String[] SECTION_TITLES = {
        "Riwayat Kesehatan",
        "Pemeriksaan Fisik",
        "Pola Kebiasaan Pasien",
        "Pertumbuhan & Perkembangan (Balita)",
        "Kesehatan Reproduksi (Wanita)",
        "Data Psikologis, Sosiologis & Spiritual",
        "Data Pemeriksaan Diagnostik",
        "Kontrol yang Akan Datang",
        "Diagnosa Keperawatan"
    };

    /** Judul ringkas khusus rail agar setiap baris tetap satu tinggi. */
    private static final String[] SECTION_NAV_TITLES = {
        "Riwayat Kesehatan",
        "Pemeriksaan Fisik",
        "Pola Kebiasaan",
        "Pertumbuhan Balita",
        "Kesehatan Reproduksi",
        "Psikologi, Sosial & Spiritual",
        "Pemeriksaan Diagnostik",
        "Rencana Kontrol",
        "Diagnosa Keperawatan"
    };

    private static final String[] SECTION_SUBTITLES = {
        "Keluhan utama & riwayat kesehatan",
        "Keadaan umum, tanda vital, dan temuan per regio",
        "Nutrisi, eliminasi, istirahat, aktivitas",
        "Hanya untuk pasien usia di bawah 5 tahun",
        "Hanya untuk pasien perempuan / ibu hamil",
        "Kondisi psikologis, sosial, dan spiritual pasien",
        "Hasil pemeriksaan penunjang",
        "Rencana kontrol atau rawat inap",
        "Lima diagnosa yang tercetak tetap di kertas"
    };

    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;

    /** Kolom -> kendali yang mengisinya. */
    private final Map<String, Object> kendali = new LinkedHashMap<>();

    // ---------- Kerangka Utama ----------
    private widget.InternalFrame internalFrame1, internalFrame2, internalFrame3;
    private javax.swing.JTabbedPane TabRawat;
    private widget.ScrollPane scrollInput, scrollRail;
    private JPanel FormInputContainer;
    private JPanel SectionsWrapper;
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnPrint, BtnKeluar;

    // ---------- Top Bar & Header Pasien ----------
    private JLabel LblAvatar;
    private JLabel LblNmPasien, LblInfoPasien, LblNoRM, LblNoRw, LblPoli;
    private widget.TextBox TNoRw, TNoRM, TPasien, TglLahir, TUmur, Jk, TPoli;
    private widget.Tanggal TglAsesmen;
    private widget.TextBox JamAsesmen, DiagnosaMedis, NipPerawat, NmPerawat;
    private widget.Button BtnCariPetugas;

    // ---------- Sidebar Navigasi & Progress ----------
    private JLabel LblProgressPct, LblProgressNote, LblValidationFooter;
    private JPanel ProgressBarFill;
    private JButton BtnToggleMode;
    private boolean showAllMode = false;
    private int activeSectionIndex = 0;
    private final JPanel[] navItems = new JPanel[9];
    private final JLabel[] navCounts = new JLabel[9];
    private final JPanel[] sectionCards = new JPanel[9];
    private final Component[] sectionSpacers = new Component[9];
    private final JLabel[] sectionTallies = new JLabel[9];

    // ---------- Kontrol Form Spesifik ----------
    private final ButtonGroup bgKeadaanUmum = new ButtonGroup();
    private final ButtonGroup bgCrt = new ButtonGroup();
    private widget.TextArea TAreaKeluhanUtama, TAreaRiwayatKesehatan;
    private KanvasNyeri kanvasNyeri;
    private widget.CekBox AdaKontrol;
    private widget.Tanggal TglKontrol;
    private final List<ButtonGroup> bgDiagnosaList = new ArrayList<>();

    // ---------- Tab Data Tersimpan ----------
    private widget.ScrollPane Scroll, ScrollHTML;
    private widget.Table tbObat;
    private widget.Tanggal DTPCari1, DTPCari2;
    private widget.TextBox TCari, TNoRM1, TPasien1;
    private widget.Button BtnCari, BtnPrint1, BtnAll;
    private widget.Label LCount;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JMenuItem MnCetak;
    private widget.PanelBiasa PanelAccor;
    private widget.panelisi FormMenu;
    private widget.CekBox ChkAccor;
    private widget.editorpane LoadHTML;

    public RMAsesmenAwalKeperawatanRalan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.RM", "Nama Pasien", "Tgl.Lahir", "J.K.",
            "Tanggal", "Jam", "Keluhan Utama", "NIP", "Perawat/Bidan"
        }) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
        };

        rakitKerangka();
        rakitTopBar();
        rakitHeaderIdentitas();
        rakitBadanForm();
        rakitTabData();
        rakitActionBar();

        periksaPemetaan();

        widget.IconStyle.pasang(this);
        buatKotakRM022(internalFrame1);
        bersihkanIkonRadio(getContentPane());
        ChkAccor.setSelected(false);
        isMenu();
        setSize(1320, 740);

        pasangListenersValidasi();
        updateProgress();
    }

    private void periksaPemetaan() {
        List<String> hilang = new ArrayList<>();
        for (String k : KOLOM) {
            if (!kendali.containsKey(k)) {
                hilang.add(k);
            }
        }
        List<String> nganggur = new ArrayList<>(kendali.keySet());
        for (String k : KOLOM) {
            nganggur.remove(k);
        }
        if (!hilang.isEmpty() || !nganggur.isEmpty()) {
            throw new IllegalStateException("Pemetaan kolom RM 02-2 tidak utuh."
                    + " Tanpa kendali : " + hilang + " | Kendali tanpa kolom : " + nganggur);
        }
    }

    // =================================================================
    //  KOMPONEN KONTROL KUSTOM: PILL RADIO & CHIP CHECKBOX
    // =================================================================

    private static class RadioPill extends widget.RadioButton {
        public RadioPill(String text, ButtonGroup group) {
            super();
            setText(text);
            if (group != null) group.add(this);
            setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(5, 10, 5, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (isSelected()) {
                g2.setColor(TEAL_LIGHT);
                g2.fillRoundRect(1, 1, w - 2, h - 2, 4, 4);
                g2.setColor(TEAL_ACCENT);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 4, 4);
            } else {
                g2.setColor(CARD_BG);
                g2.fillRoundRect(1, 1, w - 2, h - 2, 4, 4);
                g2.setColor(BORDER_GRAY);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 4, 4);
            }

            int dotSize = 13;
            int dotX = 8;
            int dotY = (h - dotSize) / 2;

            if (isSelected()) {
                g2.setColor(TEAL_ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(dotX, dotY, dotSize, dotSize);
                g2.fillOval(dotX + 3, dotY + 3, dotSize - 6, dotSize - 6);
            } else {
                g2.setColor(new Color(0xb3, 0xb8, 0xb3));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(dotX, dotY, dotSize, dotSize);
            }

            g2.setColor(TEXT_DARK);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textX = dotX + dotSize + 6;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setFont(getFont());
            g2.drawString(getText(), textX, textY);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int w = 8 + 13 + 6 + fm.stringWidth(getText()) + 10;
            return new Dimension(w, 28);
        }
    }

    private static class ChipCheckBox extends widget.CekBox {
        public ChipCheckBox(String text) {
            super();
            setText(text);
            setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(5, 9, 5, 9));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (isSelected()) {
                g2.setColor(CHIP_ON_BG);
                g2.fillRoundRect(1, 1, w - 2, h - 2, 4, 4);
                g2.setColor(TEAL_ACCENT);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 4, 4);
            } else {
                g2.setColor(CARD_BG);
                g2.fillRoundRect(1, 1, w - 2, h - 2, 4, 4);
                g2.setColor(BORDER_GRAY);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 4, 4);
            }

            int iconSize = 13;
            int iconX = 8;
            int iconY = (h - iconSize) / 2;

            if (isSelected()) {
                g2.setColor(TEAL_ACCENT);
                g2.fillRoundRect(iconX, iconY, iconSize, iconSize, 3, 3);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(iconX + 3, iconY + 6, iconX + 5, iconY + 9);
                g2.drawLine(iconX + 5, iconY + 9, iconX + 10, iconY + 4);
                g2.setColor(TEAL_ACCENT);
            } else {
                g2.setColor(new Color(0xb3, 0xb8, 0xb3));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(iconX, iconY, iconSize, iconSize, 3, 3);
                g2.setColor(TEXT_LABEL);
            }

            FontMetrics fm = g2.getFontMetrics(getFont());
            int textX = iconX + iconSize + 6;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setFont(getFont());
            g2.drawString(getText(), textX, textY);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int w = 8 + 13 + 6 + fm.stringWidth(getText()) + 10;
            return new Dimension(w, 28);
        }
    }

    /**
     * Grup kotak centang per regio / kebiasaan yang menyatukan temuan koma.
     */
    private final class GrupCentang {
        private final List<ChipCheckBox> kotak = new ArrayList<>();
        private widget.TextBox lain;

        String nilai() {
            StringBuilder b = new StringBuilder();
            for (ChipCheckBox c : kotak) {
                if (c.isSelected()) {
                    b.append(b.length() > 0 ? ", " : "").append(c.getText());
                }
            }
            if (lain != null && !lain.getText().trim().isEmpty()) {
                b.append(b.length() > 0 ? ", " : "").append(lain.getText().trim());
            }
            return b.length() == 0 ? null : b.toString();
        }

        void set(String isi) {
            kosongkan();
            if (isi == null || isi.trim().isEmpty()) {
                return;
            }
            StringBuilder sisa = new StringBuilder();
            for (String potong : isi.split(",")) {
                String t = potong.trim();
                if (t.isEmpty()) {
                    continue;
                }
                boolean ketemu = false;
                for (ChipCheckBox c : kotak) {
                    if (c.getText().equalsIgnoreCase(t)) {
                        c.setSelected(true);
                        ketemu = true;
                        break;
                    }
                }
                if (!ketemu) {
                    sisa.append(sisa.length() > 0 ? ", " : "").append(t);
                }
            }
            if (lain != null) {
                lain.setText(sisa.toString());
            }
        }

        void kosongkan() {
            for (ChipCheckBox c : kotak) {
                c.setSelected(false);
            }
            if (lain != null) {
                lain.setText("");
            }
        }

        boolean terisi() {
            return nilai() != null;
        }
    }

    /** Rail mengikuti lebar viewport dan hanya menggulung secara vertikal. */
    private static final class RailRM022 extends JPanel implements javax.swing.Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return Math.max(18, visibleRect.height - 18);
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

    /** Konten formulir selalu mengikuti lebar viewport; hanya scroll vertikal. */
    private static final class ContentRM022 extends JPanel implements javax.swing.Scrollable {
        ContentRM022(java.awt.LayoutManager layout) {
            super(layout);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) { return 18; }
        @Override public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) {
            return Math.max(18, r.height - 18);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    /** FlowLayout yang melaporkan tinggi setelah komponen turun baris. */
    private static final class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override public Dimension preferredLayoutSize(java.awt.Container target) {
            return ukuranLayout(target, true);
        }

        @Override public Dimension minimumLayoutSize(java.awt.Container target) {
            Dimension d = ukuranLayout(target, false);
            d.width -= getHgap() + 1;
            return d;
        }

        private Dimension ukuranLayout(java.awt.Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int lebarTarget = target.getWidth();
                if (lebarTarget <= 0 && target.getParent() != null) {
                    lebarTarget = target.getParent().getWidth();
                }
                if (lebarTarget <= 0) {
                    lebarTarget = 900;
                }

                java.awt.Insets insets = target.getInsets();
                int batas = Math.max(1, lebarTarget - insets.left - insets.right - getHgap() * 2);
                int lebarBaris = 0;
                int tinggiBaris = 0;
                int lebarMaks = 0;
                int tinggiTotal = 0;

                for (Component komponen : target.getComponents()) {
                    if (!komponen.isVisible()) continue;
                    Dimension ukuran = preferred ? komponen.getPreferredSize() : komponen.getMinimumSize();
                    int tambahan = lebarBaris == 0 ? ukuran.width : getHgap() + ukuran.width;
                    if (lebarBaris > 0 && lebarBaris + tambahan > batas) {
                        lebarMaks = Math.max(lebarMaks, lebarBaris);
                        tinggiTotal += tinggiBaris + getVgap();
                        lebarBaris = ukuran.width;
                        tinggiBaris = ukuran.height;
                    } else {
                        lebarBaris += tambahan;
                        tinggiBaris = Math.max(tinggiBaris, ukuran.height);
                    }
                }

                lebarMaks = Math.max(lebarMaks, lebarBaris);
                tinggiTotal += tinggiBaris;
                return new Dimension(
                        lebarMaks + insets.left + insets.right + getHgap() * 2,
                        tinggiTotal + insets.top + insets.bottom + getVgap() * 2);
            }
        }
    }

    // =================================================================
    //  1. KERANGKA UTAMA
    // =================================================================

    private void rakitKerangka() {
        internalFrame1 = new widget.InternalFrame();
        internalFrame1.setBorder(null);
        internalFrame1.setLayout(new BorderLayout());

        TabRawat = new javax.swing.JTabbedPane();
        TabRawat.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));

        internalFrame2 = new widget.InternalFrame();
        internalFrame2.setBorder(null);
        internalFrame2.setLayout(new BorderLayout());

        FormInputContainer = new ContentRM022(new BorderLayout());
        FormInputContainer.setBackground(BG_PAGE);

        scrollInput = new widget.ScrollPane();
        scrollInput.setViewportView(FormInputContainer);
        scrollInput.setBorder(null);
        scrollInput.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollInput.getVerticalScrollBar().setUnitIncrement(18);
        scrollInput.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                rapikanWrapSetelahLayout();
            }
        });

        internalFrame2.add(scrollInput, BorderLayout.CENTER);
        TabRawat.addTab("Input Formulir Asesmen", internalFrame2);

        internalFrame1.add(TabRawat, BorderLayout.CENTER);
        getContentPane().add(internalFrame1, BorderLayout.CENTER);
    }

    // =================================================================
    //  2. TOP BAR (34px, #123c3a)
    // =================================================================

    private void rakitTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(getWidth(), 34));
        topBar.setBackground(TEAL_HEADER);
        topBar.setBorder(new EmptyBorder(0, 16, 0, 16));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Asesmen Awal Keperawatan Rawat Jalan");
        lblTitle.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 12));
        lblTitle.setForeground(new Color(0xdf, 0xe9, 0xe6));

        JLabel lblBadge = new JLabel("RM 02-2");
        lblBadge.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblBadge.setForeground(new Color(0x8f, 0xb5, 0xae));
        lblBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x2c, 0x5a, 0x56), 1, true),
                new EmptyBorder(1, 6, 1, 6)));

        leftPanel.add(lblTitle);
        leftPanel.add(lblBadge);

        JLabel lblRS = new JLabel("RSUD Demang Sepulau Raya");
        lblRS.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        lblRS.setForeground(new Color(0x8f, 0xb5, 0xae));

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(lblRS, BorderLayout.EAST);

        internalFrame1.add(topBar, BorderLayout.PAGE_START);
    }

    // =================================================================
    //  3. PANEL HEADER PASIEN (262px KARTU + 4 KOLOM INPUT)
    // =================================================================

    private void rakitHeaderIdentitas() {
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_GRAY),
                new EmptyBorder(12, 16, 12, 16)));

        // --- Kartu Pasien (262px, #f7f8f5) ---
        JPanel patientCard = new JPanel(new BorderLayout(0, 6));
        patientCard.setPreferredSize(new Dimension(262, 120));
        patientCard.setMinimumSize(new Dimension(262, 120));
        patientCard.setBackground(PATIENT_CARD_BG);
        patientCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xe0, 0xe2, 0xdd), 1),
                new EmptyBorder(8, 10, 8, 10)));

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel lblCardTitle = new JLabel("IDENTITAS PASIEN");
        lblCardTitle.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lblCardTitle.setForeground(TEXT_LABEL_MUTED);

        JLabel lblLocked = new JLabel("TERKUNCI");
        lblLocked.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 9));
        lblLocked.setForeground(new Color(0x7b, 0x82, 0x7d));
        lblLocked.setOpaque(true);
        lblLocked.setBackground(new Color(0xe8, 0xea, 0xe5));
        lblLocked.setBorder(new EmptyBorder(2, 5, 2, 5));

        cardHeader.add(lblCardTitle, BorderLayout.WEST);
        cardHeader.add(lblLocked, BorderLayout.EAST);

        JPanel cardBody = new JPanel(new BorderLayout(8, 0));
        cardBody.setOpaque(false);

        LblAvatar = new JLabel("PS", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xea, 0xf1, 0xf7));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(new Color(0x0d, 0x7a, 0x70, 80));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        LblAvatar.setPreferredSize(new Dimension(38, 38));
        LblAvatar.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 13));
        LblAvatar.setForeground(TEAL_ACCENT);

        JPanel namePanel = new JPanel(new GridLayout(2, 1, 0, 1));
        namePanel.setOpaque(false);
        LblNmPasien = new JLabel("-");
        LblNmPasien.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 13));
        LblNmPasien.setForeground(TEXT_DARK);
        LblInfoPasien = new JLabel("-");
        LblInfoPasien.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        LblInfoPasien.setForeground(new Color(0x6c, 0x73, 0x6e));
        namePanel.add(LblNmPasien);
        namePanel.add(LblInfoPasien);

        cardBody.add(LblAvatar, BorderLayout.WEST);
        cardBody.add(namePanel, BorderLayout.CENTER);

        JPanel cardMeta = new JPanel(new GridLayout(3, 1, 0, 1));
        cardMeta.setOpaque(false);
        cardMeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xe4, 0xe6, 0xe2)),
                new EmptyBorder(4, 0, 0, 0)));

        LblNoRM = new JLabel("No. RM: -");
        LblNoRM.setFont(new Font("Monospaced", Font.PLAIN, 10));
        LblNoRM.setForeground(new Color(0x5d, 0x64, 0x5f));

        LblNoRw = new JLabel("No. Rawat: -");
        LblNoRw.setFont(new Font("Monospaced", Font.PLAIN, 10));
        LblNoRw.setForeground(new Color(0x5d, 0x64, 0x5f));

        LblPoli = new JLabel("Poli: -");
        LblPoli.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 10));
        LblPoli.setForeground(new Color(0x5d, 0x64, 0x5f));

        cardMeta.add(LblNoRM);
        cardMeta.add(LblNoRw);
        cardMeta.add(LblPoli);

        patientCard.add(cardHeader, BorderLayout.NORTH);
        patientCard.add(cardBody, BorderLayout.CENTER);
        patientCard.add(cardMeta, BorderLayout.SOUTH);

        // Fields hidden untuk data pendaftaran
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        TglLahir = new widget.TextBox();
        TUmur = new widget.TextBox();
        Jk = new widget.TextBox();
        TPoli = new widget.TextBox();

        // --- 4 Kolom Baris Input Header ---
        JPanel headerInputs = new JPanel(new GridBagLayout());
        headerInputs.setOpaque(false);

        // 1. Tanggal Asesmen
        JLabel lblTgl = new JLabel("TANGGAL");
        lblTgl.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lblTgl.setForeground(TEXT_LABEL_MUTED);
        TglAsesmen = new widget.Tanggal();
        TglAsesmen.setDisplayFormat("dd-MM-yyyy");
        TglAsesmen.setDate(new Date());
        TglAsesmen.setPreferredSize(new Dimension(140, 30));
        JPanel colTgl = buatKolomHeader(lblTgl, TglAsesmen);

        // 2. Jam Asesmen
        JLabel lblJam = new JLabel("JAM");
        lblJam.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lblJam.setForeground(TEXT_LABEL_MUTED);
        JamAsesmen = new widget.TextBox();
        JamAsesmen.setText(new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()));
        JamAsesmen.setEditable(false);
        JamAsesmen.setBackground(new Color(0xfb, 0xfc, 0xfa));
        JamAsesmen.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JamAsesmen.setPreferredSize(new Dimension(80, 30));
        JPanel colJam = buatKolomHeader(lblJam, JamAsesmen);

        // 3. Diagnosa Medis
        JLabel lblDx = new JLabel("DIAGNOSA MEDIS");
        lblDx.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lblDx.setForeground(TEXT_LABEL_MUTED);
        DiagnosaMedis = new widget.TextBox();
        DiagnosaMedis.setPreferredSize(new Dimension(180, 30));
        JPanel colDx = buatKolomHeader(lblDx, DiagnosaMedis);

        // 4. Perawat Pengkaji
        JLabel lblPerawat = new JLabel("PERAWAT PENGKAJI");
        lblPerawat.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lblPerawat.setForeground(TEXT_LABEL_MUTED);

        JPanel boxPerawat = new JPanel(new BorderLayout(3, 0));
        boxPerawat.setOpaque(false);
        NipPerawat = new widget.TextBox();
        NipPerawat.setPreferredSize(new Dimension(85, 30));
        NipPerawat.setEditable(false);
        NmPerawat = new widget.TextBox();
        NmPerawat.setPreferredSize(new Dimension(130, 30));
        NmPerawat.setEditable(false);

        BtnCariPetugas = new widget.Button();
        BtnCariPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnCariPetugas.setPreferredSize(new Dimension(30, 30));
        BtnCariPetugas.setToolTipText("Pilih perawat/bidan dari daftar petugas");
        BtnCariPetugas.addActionListener(evt -> pilihPetugas());

        boxPerawat.add(NipPerawat, BorderLayout.WEST);
        boxPerawat.add(NmPerawat, BorderLayout.CENTER);
        boxPerawat.add(BtnCariPetugas, BorderLayout.EAST);
        boxPerawat.setPreferredSize(new Dimension(360, 30));
        JPanel colPerawat = buatKolomHeader(lblPerawat, boxPerawat);

        GridBagConstraints hc = new GridBagConstraints();
        hc.gridy = 0;
        hc.weighty = 1.0;
        hc.anchor = GridBagConstraints.NORTH;
        hc.fill = GridBagConstraints.HORIZONTAL;
        hc.insets = new Insets(0, 0, 0, 14);
        hc.gridx = 0;
        hc.weightx = 0.20;
        headerInputs.add(colTgl, hc);
        hc.gridx = 1;
        hc.weightx = 0.12;
        headerInputs.add(colJam, hc);
        hc.gridx = 2;
        hc.weightx = 0.28;
        headerInputs.add(colDx, hc);
        hc.gridx = 3;
        hc.weightx = 0.40;
        hc.insets = new Insets(0, 0, 0, 0);
        headerInputs.add(colPerawat, hc);

        headerPanel.add(patientCard, BorderLayout.WEST);
        headerPanel.add(headerInputs, BorderLayout.CENTER);

        internalFrame2.add(headerPanel, BorderLayout.NORTH);

        // Daftarkan kendali wajib header
        kendali.put("tanggal", TglAsesmen);
        kendali.put("jam", TglAsesmen);
        kendali.put("diagnosa_medis", DiagnosaMedis);
        kendali.put("nip", NipPerawat);
    }

    /** Kolom header tidak boleh membuat field melar mengikuti tinggi kartu. */
    private JPanel buatKolomHeader(JLabel label, javax.swing.JComponent field) {
        JPanel kolom = new JPanel();
        kolom.setOpaque(false);
        kolom.setLayout(new BoxLayout(kolom, BoxLayout.Y_AXIS));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMinimumSize(new Dimension(40, 30));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        kolom.add(label);
        kolom.add(Box.createVerticalStrut(4));
        kolom.add(field);
        kolom.add(Box.createVerticalGlue());
        return kolom;
    }

    /**
     * Menyamakan seluruh kendali isian RM02-2 dengan bidang kotak pada form
     * Keperawatan IGD. Komponen chip/radio tetap memakai bentuk khususnya.
     */
    private static void buatKotakRM022(Component c) {
        if (c == null) {
            return;
        }
        if (c instanceof javax.swing.JComponent) {
            javax.swing.JComponent jc = (javax.swing.JComponent) c;
            jc.putClientProperty("Component.arc", 0);
            jc.putClientProperty("Button.arc", 0);
            jc.putClientProperty("TextComponent.arc", 0);
            jc.putClientProperty("JTextField.arc", 0);
            jc.putClientProperty("ComboBox.arc", 0);
            jc.putClientProperty("JComponent.roundRect", false);

            if (c instanceof javax.swing.text.JTextComponent) {
                if (!(c instanceof javax.swing.JTextArea)) {
                    jc.setBorder(new LineBorder(BORDER_GRAY, 1));
                }
                jc.setBackground(Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                        "background: #FFFFFF; inactiveBackground: #FFFFFF; disabledBackground: #FFFFFF;");
            } else if (c instanceof javax.swing.JComboBox
                    || c instanceof uz.ncipro.calendar.JDateTimePicker) {
                jc.setBorder(new LineBorder(BORDER_GRAY, 1));
                jc.setBackground(Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                        "background: #FFFFFF; disabledBackground: #FFFFFF; buttonBackground: #FFFFFF;");
            } else if (c instanceof javax.swing.JScrollPane) {
                jc.setBorder(new LineBorder(BORDER_GRAY, 1));
                jc.setBackground(Color.WHITE);
                jc.setOpaque(true);
            } else if (c instanceof widget.Button) {
                ((widget.Button) c).setRoundRect(false);
            }
        }
        if (c instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) c).getComponents()) {
                buatKotakRM022(child);
            }
        }
    }

    // =================================================================
    //  4. BADAN FORM: SIDEBAR (300px) + 9 KARTU SECTION
    // =================================================================

    private void rakitBadanForm() {
        // --- SIDEBAR KIRI (300px) ---
        JPanel sidebar = new RailRM022();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(14, 16, 14, 8));
        sidebar.setBackground(BG_PAGE);

        // Progress Card
        JPanel progressCard = new JPanel(new BorderLayout(0, 8));
        progressCard.setBackground(CARD_BG);
        progressCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1),
                new EmptyBorder(12, 14, 12, 14)));
        progressCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        progressCard.setPreferredSize(new Dimension(274, 95));
        progressCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel progTop = new JPanel(new BorderLayout());
        progTop.setOpaque(false);
        JLabel lblProgTitle = new JLabel("ISIAN WAJIB");
        lblProgTitle.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lblProgTitle.setForeground(TEXT_LABEL_MUTED);

        LblProgressPct = new JLabel("0%");
        LblProgressPct.setFont(new Font("Monospaced", Font.BOLD, 14));
        LblProgressPct.setForeground(TEXT_DARK);

        progTop.add(lblProgTitle, BorderLayout.WEST);
        progTop.add(LblProgressPct, BorderLayout.EAST);

        JPanel progTrack = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        progTrack.setPreferredSize(new Dimension(270, 6));
        progTrack.setBackground(new Color(0xe6, 0xe8, 0xe3));
        ProgressBarFill = new JPanel();
        ProgressBarFill.setPreferredSize(new Dimension(0, 6));
        ProgressBarFill.setBackground(TEAL_ACCENT);
        progTrack.add(ProgressBarFill);

        LblProgressNote = new JLabel("0/3 isian wajib terisi");
        LblProgressNote.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        LblProgressNote.setForeground(TEXT_MUTED);

        progressCard.add(progTop, BorderLayout.NORTH);
        progressCard.add(progTrack, BorderLayout.CENTER);
        progressCard.add(LblProgressNote, BorderLayout.SOUTH);

        sidebar.add(progressCard);
        sidebar.add(Box.createVerticalStrut(12));

        // Navigation Card
        JPanel navCard = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        navCard.setLayout(new BoxLayout(navCard, BoxLayout.Y_AXIS));
        navCard.setBackground(CARD_BG);
        navCard.setBorder(new LineBorder(CARD_BORDER, 1));
        navCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel navHeader = new JPanel(new BorderLayout());
        navHeader.setBackground(CARD_BG);
        navHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xec, 0xee, 0xea)),
                new EmptyBorder(9, 12, 9, 12)));
        navHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        navHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel lblNavTitle = new JLabel("BAGIAN PENGKAJIAN");
        lblNavTitle.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lblNavTitle.setForeground(TEXT_LABEL_MUTED);
        navHeader.add(lblNavTitle, BorderLayout.WEST);
        navCard.add(navHeader);

        for (int idx = 0; idx < 9; idx++) {
            final int secIdx = idx;
            JPanel item = new JPanel(new BorderLayout(8, 0));
            item.setBackground(CARD_BG);
            item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, idx == 0 ? 3 : 0, 1, 0,
                            idx == 0 ? TEAL_ACCENT : new Color(0xf4, 0xf5, 0xf2)),
                    new EmptyBorder(8, 10, 8, 12)));
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
            item.setPreferredSize(new Dimension(272, 36));
            item.setMinimumSize(new Dimension(40, 36));
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            item.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblNum = new JLabel(String.format("%02d", idx + 1));
            lblNum.setFont(new Font("Monospaced", Font.BOLD, 10));
            lblNum.setForeground(idx == 0 ? TEAL_ACCENT : TEXT_MUTED);
            lblNum.setPreferredSize(new Dimension(26, 16));

            JLabel lblSec = new JLabel(SECTION_NAV_TITLES[idx]);
            lblSec.setFont(new Font(ThemeConfig.FONT_FAMILY, idx == 0 ? Font.BOLD : Font.PLAIN, 11));
            lblSec.setForeground(idx == 0 ? TEAL_ACCENT : TEXT_LABEL);

            JLabel lblCount = new JLabel(idx == 0 ? "0/2" : (idx == 1 ? "0/1" : "–"));
            lblCount.setFont(new Font("Monospaced", Font.PLAIN, 10));
            lblCount.setForeground(TEXT_MUTED);

            item.add(lblNum, BorderLayout.WEST);
            item.add(lblSec, BorderLayout.CENTER);
            item.add(lblCount, BorderLayout.EAST);

            item.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    pindahBagian(secIdx);
                }
            });

            navItems[idx] = item;
            navCounts[idx] = lblCount;
            navCard.add(item);
        }

        sidebar.add(navCard);
        sidebar.add(Box.createVerticalStrut(12));

        // Toggle button: Tampilkan Semua / Tampilkan Per Bagian
        BtnToggleMode = new JButton("Tampilkan Semua Bagian");
        BtnToggleMode.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        BtnToggleMode.setForeground(TEAL_ACCENT);
        BtnToggleMode.setBackground(CARD_BG);
        BtnToggleMode.setBorder(new LineBorder(new Color(0x0d, 0x7a, 0x70, 90), 1));
        BtnToggleMode.setCursor(new Cursor(Cursor.HAND_CURSOR));
        BtnToggleMode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        BtnToggleMode.setMinimumSize(new Dimension(40, 32));
        BtnToggleMode.setPreferredSize(new Dimension(274, 32));
        BtnToggleMode.setAlignmentX(Component.LEFT_ALIGNMENT);
        BtnToggleMode.setFocusPainted(false);
        BtnToggleMode.addActionListener(e -> toggleViewMode());

        sidebar.add(BtnToggleMode);
        sidebar.add(Box.createVerticalGlue());

        // --- KONTEN KANAN: 9 KARTU SECTIONS ---
        SectionsWrapper = new JPanel();
        SectionsWrapper.setLayout(new BoxLayout(SectionsWrapper, BoxLayout.Y_AXIS));
        SectionsWrapper.setOpaque(false);

        sectionCards[0] = rakitSeksi1();
        sectionCards[1] = rakitSeksi2();
        sectionCards[2] = rakitSeksi3();
        sectionCards[3] = rakitSeksi4();
        sectionCards[4] = rakitSeksi5();
        sectionCards[5] = rakitSeksi6();
        sectionCards[6] = rakitSeksi7();
        sectionCards[7] = rakitSeksi8();
        sectionCards[8] = rakitSeksi9();

        for (int idx = 0; idx < 9; idx++) {
            SectionsWrapper.add(sectionCards[idx]);
            sectionSpacers[idx] = Box.createVerticalStrut(14);
            SectionsWrapper.add(sectionSpacers[idx]);
        }

        FormInputContainer.removeAll();
        FormInputContainer.setBorder(new EmptyBorder(14, 8, 14, 16));
        FormInputContainer.add(SectionsWrapper, BorderLayout.CENTER);

        scrollRail = new widget.ScrollPane();
        scrollRail.setViewportView(sidebar);
        scrollRail.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_GRAY));
        scrollRail.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollRail.setPreferredSize(new Dimension(300, 10));
        scrollRail.getVerticalScrollBar().setUnitIncrement(18);
        scrollRail.getViewport().setBackground(BG_PAGE);

        internalFrame2.remove(scrollInput);
        internalFrame2.add(scrollRail, BorderLayout.WEST);
        internalFrame2.add(scrollInput, BorderLayout.CENTER);

        pindahBagian(0);
    }

    // =================================================================
    //  HELPER PERAKIT SECTION CARD
    // =================================================================

    private JPanel buatSectionCard(int idx, JPanel contentPanel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(CARD_BORDER, 1));

        // Section Header
        JPanel secHeader = new JPanel(new BorderLayout(12, 0));
        secHeader.setBackground(new Color(0xfb, 0xfc, 0xfa));
        secHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xec, 0xee, 0xea)),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel badge = new JLabel(ROMAN[idx], SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setPreferredSize(new Dimension(26, 26));
        badge.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        badge.setForeground(Color.WHITE);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 1));
        titleBox.setOpaque(false);
        JLabel lblTitle = new JLabel(SECTION_TITLES[idx]);
        lblTitle.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 13));
        lblTitle.setForeground(TEXT_DARK);
        JLabel lblSub = new JLabel(SECTION_SUBTITLES[idx]);
        lblSub.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        lblSub.setForeground(TEXT_MUTED);
        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        JLabel tally = new JLabel("Opsional", SwingConstants.CENTER);
        tally.setFont(new Font("Monospaced", Font.PLAIN, 11));
        tally.setOpaque(true);
        tally.setBackground(GRAY_TALLY_BG);
        tally.setForeground(GRAY_TALLY_FG);
        tally.setBorder(new EmptyBorder(3, 8, 3, 8));
        sectionTallies[idx] = tally;

        secHeader.add(badge, BorderLayout.WEST);
        secHeader.add(titleBox, BorderLayout.CENTER);
        secHeader.add(tally, BorderLayout.EAST);

        // Section Stepper Footer
        JPanel stepper = new JPanel(new BorderLayout());
        stepper.setBackground(new Color(0xfb, 0xfc, 0xfa));
        stepper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xec, 0xee, 0xea)),
                new EmptyBorder(8, 16, 8, 16)));

        final int prevIdx = idx - 1;
        final int nextIdx = idx + 1;

        JButton btnPrev = new JButton("‹ " + (prevIdx >= 0 ? SECTION_TITLES[prevIdx] : "Awal"));
        btnPrev.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        btnPrev.setEnabled(prevIdx >= 0);
        btnPrev.setCursor(new Cursor(prevIdx >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        btnPrev.addActionListener(e -> pindahBagian(prevIdx));

        JLabel lblStep = new JLabel("Bagian " + (idx + 1) + " dari 9");
        lblStep.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        lblStep.setForeground(TEXT_MUTED);

        JButton btnNext = new JButton((nextIdx < 9 ? SECTION_TITLES[nextIdx] : "Akhir") + " ›");
        btnNext.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        btnNext.setEnabled(nextIdx < 9);
        btnNext.setCursor(new Cursor(nextIdx < 9 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        btnNext.addActionListener(e -> pindahBagian(nextIdx));

        stepper.add(btnPrev, BorderLayout.WEST);
        stepper.add(lblStep, BorderLayout.CENTER);
        stepper.add(btnNext, BorderLayout.EAST);

        card.add(secHeader, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);
        card.add(stepper, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buatBarisRow(String label, boolean wajib, Component content) {
        JPanel row = new JPanel(new BorderLayout(14, 0)) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xf4, 0xf5, 0xf2)),
                new EmptyBorder(8, 0, 8, 0)));

        JLabel lbl = new JLabel(label + (wajib ? " *" : ""));
        lbl.setPreferredSize(new Dimension(260, 26));
        lbl.setFont(new Font(ThemeConfig.FONT_FAMILY, wajib ? Font.BOLD : Font.PLAIN, 12));
        lbl.setForeground(wajib ? TEXT_DARK : TEXT_LABEL);
        lbl.setVerticalAlignment(SwingConstants.TOP);

        row.add(lbl, BorderLayout.WEST);
        row.add(content, BorderLayout.CENTER);
        return row;
    }

    private JPanel buatSubheaderRow(String label) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(14, 0, 6, 0));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lbl.setForeground(new Color(0x5d, 0x64, 0x5f));

        JPanel line = new JPanel();
        line.setBackground(new Color(0xec, 0xee, 0xea));
        line.setPreferredSize(new Dimension(100, 1));

        row.add(lbl, BorderLayout.WEST);
        row.add(line, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private void barisRegio(JPanel parent, String kolom, String judul, String pilihan) {
        GrupCentang g = new GrupCentang();
        JPanel flow = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 4));
        flow.setOpaque(false);

        String[] opsi = pilihan.split("\\|");
        for (String o : opsi) {
            ChipCheckBox c = new ChipCheckBox(o);
            c.addActionListener(e -> updateProgress());
            g.kotak.add(c);
            flow.add(c);
        }

        g.lain = new widget.TextBox();
        g.lain.setPreferredSize(new Dimension(180, 28));
        JPanel boxLain = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        boxLain.setOpaque(false);
        JLabel lblLain = new JLabel("Lain-lain:");
        lblLain.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        lblLain.setForeground(TEXT_MUTED);
        boxLain.add(lblLain);
        boxLain.add(g.lain);
        flow.add(boxLain);

        parent.add(buatBarisRow(judul, false, flow));
        kendali.put(kolom, g);
    }

    // =================================================================
    //  SEKSI I: RIWAYAT KESEHATAN
    // =================================================================

    private JPanel rakitSeksi1() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 16, 12, 16));

        TAreaKeluhanUtama = new widget.TextArea();
        TAreaKeluhanUtama.setRows(3);
        TAreaKeluhanUtama.setLineWrap(true);
        TAreaKeluhanUtama.setWrapStyleWord(true);
        widget.ScrollPane spKeluhan = new widget.ScrollPane();
        spKeluhan.setViewportView(TAreaKeluhanUtama);
        spKeluhan.setPreferredSize(new Dimension(580, 60));
        spKeluhan.setBorder(new LineBorder(BORDER_GRAY, 1));
        content.add(buatBarisRow("Keluhan Utama", true, spKeluhan));

        TAreaRiwayatKesehatan = new widget.TextArea();
        TAreaRiwayatKesehatan.setRows(3);
        TAreaRiwayatKesehatan.setLineWrap(true);
        TAreaRiwayatKesehatan.setWrapStyleWord(true);
        widget.ScrollPane spRiw = new widget.ScrollPane();
        spRiw.setViewportView(TAreaRiwayatKesehatan);
        spRiw.setPreferredSize(new Dimension(580, 60));
        spRiw.setBorder(new LineBorder(BORDER_GRAY, 1));
        content.add(buatBarisRow("Riwayat Kesehatan", true, spRiw));

        kendali.put("keluhan_utama", TAreaKeluhanUtama);
        kendali.put("riwayat_kesehatan", TAreaRiwayatKesehatan);

        return buatSectionCard(0, content);
    }

    // =================================================================
    //  SEKSI II: PEMERIKSAAN FISIK
    // =================================================================

    private JPanel rakitSeksi2() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 16, 12, 16));

        // Keadaan umum
        JPanel pKeadaan = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 0));
        pKeadaan.setOpaque(false);
        RadioPill rBaik = new RadioPill("Baik", bgKeadaanUmum);
        RadioPill rLemah = new RadioPill("Lemah", bgKeadaanUmum);
        RadioPill rBuruk = new RadioPill("Buruk", bgKeadaanUmum);
        rBaik.addActionListener(e -> updateProgress());
        rLemah.addActionListener(e -> updateProgress());
        rBuruk.addActionListener(e -> updateProgress());
        pKeadaan.add(rBaik);
        pKeadaan.add(rLemah);
        pKeadaan.add(rBuruk);
        content.add(buatBarisRow("Keadaan Umum", true, pKeadaan));
        kendali.put("keadaan_umum", bgKeadaanUmum);

        // Tanda Vital & Nyeri (Grid 6 input)
        JPanel pTtv = new JPanel(new WrapLayout(FlowLayout.LEFT, 12, 4));
        pTtv.setOpaque(false);

        widget.TextBox tdSistolik = new widget.TextBox();
        tdSistolik.setPreferredSize(new Dimension(45, 28));
        widget.TextBox tdDiastolik = new widget.TextBox();
        tdDiastolik.setPreferredSize(new Dimension(45, 28));
        widget.TextBox vas = new widget.TextBox();
        vas.setPreferredSize(new Dimension(38, 28));
        widget.TextBox hr = new widget.TextBox();
        hr.setPreferredSize(new Dimension(45, 28));
        widget.TextBox rr = new widget.TextBox();
        rr.setPreferredSize(new Dimension(45, 28));
        widget.TextBox suhu = new widget.TextBox();
        suhu.setPreferredSize(new Dimension(45, 28));

        kendali.put("td_sistolik", tdSistolik);
        kendali.put("td_diastolik", tdDiastolik);
        kendali.put("vas", vas);
        kendali.put("hr", hr);
        kendali.put("rr", rr);
        kendali.put("suhu", suhu);

        pTtv.add(new JLabel("TD:"));
        pTtv.add(tdSistolik);
        pTtv.add(new JLabel("/"));
        pTtv.add(tdDiastolik);
        pTtv.add(new JLabel("mmHg   HR:"));
        pTtv.add(hr);
        pTtv.add(new JLabel("x/mnt   RR:"));
        pTtv.add(rr);
        pTtv.add(new JLabel("x/mnt   Suhu:"));
        pTtv.add(suhu);
        pTtv.add(new JLabel("°C   VAS:"));
        pTtv.add(vas);
        pTtv.add(new JLabel("(0-10)"));
        content.add(buatBarisRow("Tanda Vital & VAS", false, pTtv));

        // Skala Nyeri Wong-Baker Kanvas
        kanvasNyeri = new KanvasNyeri(380, 140);
        kanvasNyeri.setPreferredSize(new Dimension(380, 140));
        kendali.put("nyeri_canvas_gambar", kanvasNyeri);

        JButton btnHapusNyeri = new JButton("Hapus Tanda");
        btnHapusNyeri.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        btnHapusNyeri.setPreferredSize(new Dimension(110, 26));
        btnHapusNyeri.addActionListener(e -> kanvasNyeri.hapusCoretan());

        JPanel pNyeri = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 0));
        pNyeri.setOpaque(false);
        pNyeri.add(kanvasNyeri);
        pNyeri.add(btnHapusNyeri);
        content.add(buatBarisRow("Skala Nyeri Wong-Baker", false, pNyeri));

        // Subheader Head-to-Toe
        content.add(buatSubheaderRow("Temuan per Regio (Head-to-Toe)"));

        // 20 Regio Checklist
        for (String[] reg : REGIO) {
            barisRegio(content, reg[0], reg[1], reg[2]);
        }

        // Subheader Sirkulasi & Otot
        content.add(buatSubheaderRow("Sirkulasi & Kekuatan Otot"));

        JPanel pCrt = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 0));
        pCrt.setOpaque(false);
        RadioPill crt1 = new RadioPill("<2 detik", bgCrt);
        RadioPill crt2 = new RadioPill(">2 detik", bgCrt);
        pCrt.add(crt1);
        pCrt.add(crt2);
        content.add(buatBarisRow("CRT", false, pCrt));
        kendali.put("crt", bgCrt);

        JPanel pOtot = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 2));
        pOtot.setOpaque(false);
        widget.TextBox ototEsd = new widget.TextBox(); ototEsd.setPreferredSize(new Dimension(40, 28));
        widget.TextBox ototEss = new widget.TextBox(); ototEss.setPreferredSize(new Dimension(40, 28));
        widget.TextBox ototEid = new widget.TextBox(); ototEid.setPreferredSize(new Dimension(40, 28));
        widget.TextBox ototEis = new widget.TextBox(); ototEis.setPreferredSize(new Dimension(40, 28));
        kendali.put("otot_esd", ototEsd);
        kendali.put("otot_ess", ototEss);
        kendali.put("otot_eid", ototEid);
        kendali.put("otot_eis", ototEis);

        pOtot.add(new JLabel("Sup. Dextra:"));
        pOtot.add(ototEsd);
        pOtot.add(new JLabel("Sinistra:"));
        pOtot.add(ototEss);
        pOtot.add(new JLabel("   Inf. Dextra:"));
        pOtot.add(ototEid);
        pOtot.add(new JLabel("Sinistra:"));
        pOtot.add(ototEis);
        pOtot.add(new JLabel("(nilai 0-5)"));
        content.add(buatBarisRow("Kekuatan Otot", false, pOtot));

        return buatSectionCard(1, content);
    }

    // =================================================================
    //  SEKSI III: POLA KEBIASAAN
    // =================================================================

    private JPanel rakitSeksi3() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 16, 12, 16));

        for (String[] kb : KEBIASAAN) {
            barisRegio(content, kb[0], kb[1], kb[2]);
        }

        return buatSectionCard(2, content);
    }

    // =================================================================
    //  SEKSI IV: PERTUMBUHAN & PERKEMBANGAN BALITA (BERSYARAT)
    // =================================================================

    private JPanel rakitSeksi4() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 16, 12, 16));

        for (String[] b : BALITA_CENTANG) {
            barisRegio(content, b[0], b[1], b[2]);
        }

        JPanel pLahir = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 2));
        pLahir.setOpaque(false);
        widget.TextBox anakKe = new widget.TextBox(); anakKe.setPreferredSize(new Dimension(45, 28));
        widget.TextBox bbLahir = new widget.TextBox(); bbLahir.setPreferredSize(new Dimension(55, 28));
        widget.TextBox tbLahir = new widget.TextBox(); tbLahir.setPreferredSize(new Dimension(55, 28));
        kendali.put("balita_anak_ke", anakKe);
        kendali.put("balita_bb_lahir", bbLahir);
        kendali.put("balita_tb_lahir", tbLahir);

        pLahir.add(new JLabel("Anak ke:"));
        pLahir.add(anakKe);
        pLahir.add(new JLabel("   BB lahir:"));
        pLahir.add(bbLahir);
        pLahir.add(new JLabel("gr   TB lahir:"));
        pLahir.add(tbLahir);
        pLahir.add(new JLabel("cm"));
        content.add(buatBarisRow("Kelahiran", false, pLahir));

        JPanel pBbtb = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 2));
        pBbtb.setOpaque(false);
        widget.TextBox bbTbSekarang = new widget.TextBox(); bbTbSekarang.setPreferredSize(new Dimension(140, 28));
        widget.TextBox kelainanBawaan = new widget.TextBox(); kelainanBawaan.setPreferredSize(new Dimension(240, 28));
        kendali.put("balita_bbtb_sekarang", bbTbSekarang);
        kendali.put("balita_kelainan", kelainanBawaan);

        pBbtb.add(bbTbSekarang);
        pBbtb.add(new JLabel("   Kelainan bawaan:"));
        pBbtb.add(kelainanBawaan);
        content.add(buatBarisRow("BB/TB Sekarang", false, pBbtb));

        JPanel pTonggak = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 4));
        pTonggak.setOpaque(false);
        for (String[] t : BALITA_TONGGAK) {
            widget.TextBox boxT = new widget.TextBox();
            boxT.setPreferredSize(new Dimension(45, 28));
            kendali.put(t[0], boxT);
            pTonggak.add(new JLabel(t[1] + ":"));
            pTonggak.add(boxT);
            pTonggak.add(new JLabel("bln   "));
        }
        content.add(buatBarisRow("Tonggak Perkembangan", false, pTonggak));

        JPanel card = buatSectionCard(3, content);
        card.setVisible(false);
        return card;
    }

    // =================================================================
    //  SEKSI V: KESEHATAN REPRODUKSI WANITA (BERSYARAT)
    // =================================================================

    private JPanel rakitSeksi5() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 16, 12, 16));

        JPanel pHaid = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 2));
        pHaid.setOpaque(false);
        widget.TextBox menarche = new widget.TextBox(); menarche.setPreferredSize(new Dimension(45, 28));
        widget.TextBox interval = new widget.TextBox(); interval.setPreferredSize(new Dimension(50, 28));
        widget.TextBox jumlah = new widget.TextBox(); jumlah.setPreferredSize(new Dimension(65, 28));
        widget.TextBox skalaNyeri = new widget.TextBox(); skalaNyeri.setPreferredSize(new Dimension(40, 28));
        widget.TextBox hpht = new widget.TextBox(); hpht.setPreferredSize(new Dimension(85, 28));
        kendali.put("repro_usia_menstruasi", menarche);
        kendali.put("repro_interval", interval);
        kendali.put("repro_jumlah", jumlah);
        kendali.put("repro_nyeri_skala", skalaNyeri);
        kendali.put("repro_hpht", hpht);

        pHaid.add(new JLabel("Menarche:"));
        pHaid.add(menarche);
        pHaid.add(new JLabel("th   Interval:"));
        pHaid.add(interval);
        pHaid.add(new JLabel("hari   Jumlah:"));
        pHaid.add(jumlah);
        pHaid.add(new JLabel("   Skala Nyeri:"));
        pHaid.add(skalaNyeri);
        pHaid.add(new JLabel("   HPHT:"));
        pHaid.add(hpht);
        content.add(buatBarisRow("Menstruasi", false, pHaid));

        for (String[] rp : REPRO_CENTANG) {
            barisRegio(content, rp[0], rp[1], rp[2]);
        }

        JPanel pKehamilan = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 2));
        pKehamilan.setOpaque(false);
        widget.TextBox kelHamil = new widget.TextBox(); kelHamil.setPreferredSize(new Dimension(240, 28));
        widget.TextBox obatHamil = new widget.TextBox(); obatHamil.setPreferredSize(new Dimension(200, 28));
        kendali.put("repro_keluhan_hamil", kelHamil);
        kendali.put("repro_obat", obatHamil);
        pKehamilan.add(new JLabel("Keluhan:"));
        pKehamilan.add(kelHamil);
        pKehamilan.add(new JLabel("   Obat diminum:"));
        pKehamilan.add(obatHamil);
        content.add(buatBarisRow("Kehamilan Ini", false, pKehamilan));

        JPanel pObs = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 2));
        pObs.setOpaque(false);
        widget.TextBox g = new widget.TextBox(); g.setPreferredSize(new Dimension(35, 28));
        widget.TextBox p = new widget.TextBox(); p.setPreferredSize(new Dimension(35, 28));
        widget.TextBox a = new widget.TextBox(); a.setPreferredSize(new Dimension(35, 28));
        widget.TextBox jmlAnak = new widget.TextBox(); jmlAnak.setPreferredSize(new Dimension(35, 28));
        widget.TextBox l = new widget.TextBox(); l.setPreferredSize(new Dimension(35, 28));
        widget.TextBox pr = new widget.TextBox(); pr.setPreferredSize(new Dimension(35, 28));
        widget.TextBox anakKecil = new widget.TextBox(); anakKecil.setPreferredSize(new Dimension(80, 28));
        kendali.put("obs_g", g);
        kendali.put("obs_p", p);
        kendali.put("obs_o", a);
        kendali.put("obs_jumlah_anak", jmlAnak);
        kendali.put("obs_laki", l);
        kendali.put("obs_perempuan", pr);
        kendali.put("obs_anak_terkecil", anakKecil);

        pObs.add(new JLabel("G:")); pObs.add(g);
        pObs.add(new JLabel("P:")); pObs.add(p);
        pObs.add(new JLabel("A:")); pObs.add(a);
        pObs.add(new JLabel("   Jml anak:")); pObs.add(jmlAnak);
        pObs.add(new JLabel("(L:")); pObs.add(l);
        pObs.add(new JLabel("P:")); pObs.add(pr);
        pObs.add(new JLabel(")   Anak terkecil:")); pObs.add(anakKecil);
        content.add(buatBarisRow("Status Obstetri", false, pObs));

        content.add(buatSubheaderRow("Palpasi & Janin"));

        JPanel pPalpasi = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 2));
        pPalpasi.setOpaque(false);
        widget.TextBox tfu = new widget.TextBox(); tfu.setPreferredSize(new Dimension(70, 28));
        widget.TextBox auskultasi = new widget.TextBox(); auskultasi.setPreferredSize(new Dimension(80, 28));
        kendali.put("palpasi_tfu", tfu);
        kendali.put("palpasi_auskultasi", auskultasi);
        pPalpasi.add(new JLabel("TFU:")); pPalpasi.add(tfu); pPalpasi.add(new JLabel("cm   Auskultasi DJJ:"));
        pPalpasi.add(auskultasi); pPalpasi.add(new JLabel("x/mnt"));
        content.add(buatBarisRow("Palpasi", false, pPalpasi));

        widget.TextBox pres = new widget.TextBox(); pres.setPreferredSize(new Dimension(300, 28));
        kendali.put("palpasi_presentasi", pres);
        content.add(buatBarisRow("Presentasi", false, pres));

        widget.TextBox pap = new widget.TextBox(); pap.setPreferredSize(new Dimension(300, 28));
        kendali.put("palpasi_masuk_pap", pap);
        content.add(buatBarisRow("Masuk PAP", false, pap));

        widget.TextBox gerak = new widget.TextBox(); gerak.setPreferredSize(new Dimension(300, 28));
        kendali.put("janin_pergerakan", gerak);
        content.add(buatBarisRow("Pergerakan Janin", false, gerak));

        widget.TextBox pos = new widget.TextBox(); pos.setPreferredSize(new Dimension(300, 28));
        kendali.put("janin_posisi", pos);
        content.add(buatBarisRow("Posisi Janin", false, pos));

        JPanel pJanin = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 2));
        pJanin.setOpaque(false);
        widget.TextBox umurHamil = new widget.TextBox(); umurHamil.setPreferredSize(new Dimension(65, 28));
        widget.TextBox masalahJanin = new widget.TextBox(); masalahJanin.setPreferredSize(new Dimension(240, 28));
        kendali.put("janin_umur_kehamilan", umurHamil);
        kendali.put("janin_masalah", masalahJanin);
        pJanin.add(new JLabel("Umur kehamilan:")); pJanin.add(umurHamil); pJanin.add(new JLabel("mgg   Masalah:"));
        pJanin.add(masalahJanin);
        content.add(buatBarisRow("Kondisi Janin", false, pJanin));

        JPanel pInfoLain = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 2));
        pInfoLain.setOpaque(false);
        widget.TextBox infoLain = new widget.TextBox(); infoLain.setPreferredSize(new Dimension(240, 28));
        widget.TextBox dirujuk = new widget.TextBox(); dirujuk.setPreferredSize(new Dimension(180, 28));
        kendali.put("repro_informasi_lain", infoLain);
        kendali.put("repro_dirujuk", dirujuk);
        pInfoLain.add(new JLabel("Info lain:")); pInfoLain.add(infoLain);
        pInfoLain.add(new JLabel("   Dirujuk ke:")); pInfoLain.add(dirujuk);
        content.add(buatBarisRow("Informasi Lain", false, pInfoLain));

        JPanel card = buatSectionCard(4, content);
        card.setVisible(false);
        return card;
    }

    // =================================================================
    //  SEKSI VI: DATA PSIKOLOGIS, SOSIOLOGIS & SPIRITUAL
    // =================================================================

    private JPanel rakitSeksi6() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 16, 12, 16));

        for (String[] ps : PSIKOSOSIAL) {
            barisRegio(content, ps[0], ps[1], ps[2]);
        }

        return buatSectionCard(5, content);
    }

    // =================================================================
    //  SEKSI VII: DATA PEMERIKSAAN DIAGNOSTIK
    // =================================================================

    private JPanel rakitSeksi7() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 16, 12, 16));

        widget.TextArea lab = new widget.TextArea();
        widget.ScrollPane spLab = buatAreaDiagnostik(lab);
        content.add(buatBarisRow("Laboratorium", false, spLab));
        kendali.put("diag_laboratorium", lab);

        widget.TextArea rontgen = new widget.TextArea();
        content.add(buatBarisRow("Rontgen", false, buatAreaDiagnostik(rontgen)));
        kendali.put("diag_rontgen", rontgen);

        widget.TextArea ekg = new widget.TextArea();
        content.add(buatBarisRow("EKG", false, buatAreaDiagnostik(ekg)));
        kendali.put("diag_ekg", ekg);

        widget.TextArea usg = new widget.TextArea();
        content.add(buatBarisRow("USG", false, buatAreaDiagnostik(usg)));
        kendali.put("diag_usg", usg);

        widget.TextArea scan = new widget.TextArea();
        content.add(buatBarisRow("Scanning", false, buatAreaDiagnostik(scan)));
        kendali.put("diag_scanning", scan);

        widget.TextArea terapi = new widget.TextArea();
        widget.ScrollPane spTerapi = buatAreaDiagnostik(terapi);
        content.add(buatBarisRow("Terapi Medik", false, spTerapi));
        kendali.put("diag_terapi_medik", terapi);

        return buatSectionCard(6, content);
    }

    /** Seluruh hasil diagnostik memakai area multiline dengan ukuran seragam. */
    private widget.ScrollPane buatAreaDiagnostik(widget.TextArea area) {
        area.setRows(2);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        widget.ScrollPane scroll = new widget.ScrollPane();
        scroll.setViewportView(area);
        scroll.setPreferredSize(new Dimension(540, 50));
        scroll.setMinimumSize(new Dimension(80, 50));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        scroll.setBorder(new LineBorder(BORDER_GRAY, 1));
        return scroll;
    }

    // =================================================================
    //  SEKSI VIII: KONTROL YANG AKAN DATANG
    // =================================================================

    private JPanel rakitSeksi8() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 16, 12, 16));

        widget.TextBox rawatInap = new widget.TextBox(); rawatInap.setPreferredSize(new Dimension(420, 28));
        content.add(buatBarisRow("Rencana Rawat Inap", false, rawatInap));
        kendali.put("kontrol_rawat_inap", rawatInap);

        JPanel pKontrol = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 0));
        pKontrol.setOpaque(false);
        AdaKontrol = new widget.CekBox();
        AdaKontrol.setText("Ada rencana kontrol tanggal:");
        TglKontrol = new widget.Tanggal();
        TglKontrol.setDisplayFormat("dd-MM-yyyy");
        TglKontrol.setDate(new Date());
        TglKontrol.setPreferredSize(new Dimension(140, 28));
        TglKontrol.setEnabled(false);
        AdaKontrol.addActionListener(e -> TglKontrol.setEnabled(AdaKontrol.isSelected()));

        pKontrol.add(AdaKontrol);
        pKontrol.add(TglKontrol);
        content.add(buatBarisRow("Rencana Kontrol", false, pKontrol));
        kendali.put("kontrol_tanggal", TglKontrol);

        return buatSectionCard(7, content);
    }

    // =================================================================
    //  SEKSI IX: DIAGNOSA KEPERAWATAN (5 DIAGNOSA TETAP)
    // =================================================================

    private JPanel rakitSeksi9() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 16, 12, 16));

        for (int n = 0; n < PILIHAN_DIAGNOSA.length; n += 2) {
            String col = PILIHAN_DIAGNOSA[n];
            String label = PILIHAN_DIAGNOSA[n + 1];

            ButtonGroup bg = new ButtonGroup();
            bgDiagnosaList.add(bg);

            JPanel pPil = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 0));
            pPil.setOpaque(false);
            RadioPill rYa = new RadioPill("Ya", bg);
            RadioPill rTidak = new RadioPill("Tidak", bg);
            pPil.add(rYa);
            pPil.add(rTidak);

            content.add(buatBarisRow(label, false, pPil));
            kendali.put(col, bg);
        }

        return buatSectionCard(8, content);
    }

    // =================================================================
    //  5. ACTION BAR BAWAH (56px)
    // =================================================================

    private void rakitActionBar() {
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setPreferredSize(new Dimension(getWidth(), 54));
        bottomBar.setBackground(CARD_BG);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_GRAY));

        JPanel rightStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        rightStatus.setOpaque(false);
        JLabel iconCheck = new JLabel("✓");
        iconCheck.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 14));
        iconCheck.setForeground(TEAL_ACCENT);

        LblValidationFooter = new JLabel("Isian wajib belum lengkap");
        LblValidationFooter.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
        LblValidationFooter.setForeground(TEXT_LABEL_MUTED);

        rightStatus.add(iconCheck);
        rightStatus.add(LblValidationFooter);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        leftActions.setOpaque(false);

        BtnBatal = tombolAksi("Baru", null, false);
        BtnBatal.addActionListener(evt -> emptTeks());

        BtnEdit = tombolAksi("Ganti", null, false);
        BtnEdit.addActionListener(evt -> aksiGanti());

        BtnHapus = tombolAksi("Hapus", new Color(0xa8, 0x44, 0x2e), false);
        BtnHapus.addActionListener(evt -> aksiHapus());

        BtnKeluar = tombolAksi("Keluar", new Color(0xb9, 0x1c, 0x1c), false);
        BtnKeluar.addActionListener(evt -> dispose());

        BtnPrint = tombolAksi("Cetak Formulir", null, false);
        BtnPrint.addActionListener(evt -> cetakTerpilih());

        BtnSimpan = tombolAksi("Simpan", null, true);
        BtnSimpan.addActionListener(evt -> simpan());

        leftActions.add(BtnSimpan);
        leftActions.add(BtnBatal);
        leftActions.add(BtnEdit);
        leftActions.add(BtnHapus);
        leftActions.add(BtnPrint);
        rightStatus.add(BtnKeluar);

        bottomBar.add(leftActions, BorderLayout.CENTER);
        bottomBar.add(rightStatus, BorderLayout.EAST);

        internalFrame1.add(bottomBar, BorderLayout.PAGE_END);
    }

    private widget.Button tombolAksi(String text, Color textTone, boolean isPrimary) {
        widget.Button b = new widget.Button();
        b.setText(text);
        b.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        b.setForeground(textTone != null ? textTone : TEXT_LABEL);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBackground(CARD_BG);
        b.setGlassColor(CARD_BG);
        b.setRoundRect(false);
        b.setFocusPainted(false);
        b.setIconTextGap(6);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        final javax.swing.border.Border normal = BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xd8, 0xdb, 0xd6), 1),
                new EmptyBorder(4, 12, 4, 12));
        final javax.swing.border.Border hover = BorderFactory.createCompoundBorder(
                new LineBorder(TEAL_ACCENT, 1),
                new EmptyBorder(4, 12, 4, 12));
        b.setBorder(normal);
        int lebar = "Cetak Formulir".equals(text) ? 132
                : ("Simpan".equals(text) ? 92 : ("Hapus".equals(text) ? 88 : 86));
        b.setPreferredSize(new Dimension(lebar, 32));
        b.setMinimumSize(new Dimension(lebar, 32));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(TEAL_LIGHT);
                b.setGlassColor(TEAL_LIGHT);
                b.setBorder(hover);
            }

            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(CARD_BG);
                b.setGlassColor(CARD_BG);
                b.setBorder(normal);
            }
        });
        return b;
    }

    // =================================================================
    //  6. TAB DATA TERSIMPAN
    // =================================================================

    private void rakitTabData() {
        internalFrame3 = new widget.InternalFrame();
        internalFrame3.setBorder(null);
        internalFrame3.setLayout(new BorderLayout(1, 1));

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetak = new javax.swing.JMenuItem();
        MnCetak.setText("Cetak Asesmen Awal Keperawatan Rawat Jalan");
        MnCetak.addActionListener(evt -> cetakTerpilih());
        jPopupMenu1.add(MnCetak);

        tbObat = new widget.Table();
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbObat.setComponentPopupMenu(jPopupMenu1);
        int[] lebar = {105, 70, 170, 80, 60, 85, 65, 200, 110, 160};
        for (i = 0; i < lebar.length; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebar[i]);
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (tabMode.getRowCount() != 0) {
                    getData();
                }
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (tabMode.getRowCount() == 0) return;
                int k = evt.getKeyCode();
                if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_UP || k == KeyEvent.VK_DOWN) {
                    getData();
                } else if (k == KeyEvent.VK_SPACE) {
                    getData();
                    TabRawat.setSelectedIndex(0);
                }
            }
        });

        Scroll = new widget.ScrollPane();
        Scroll.setViewportView(tbObat);
        internalFrame3.add(Scroll, BorderLayout.CENTER);

        widget.panelisi panelGlass9 = new widget.panelisi();
        panelGlass9.setPreferredSize(new Dimension(44, 44));
        panelGlass9.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 8));
        DTPCari1 = new widget.Tanggal(); DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari2 = new widget.Tanggal(); DTPCari2.setDisplayFormat("dd-MM-yyyy");
        TCari = new widget.TextBox(); TCari.setPreferredSize(new Dimension(200, 26));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    runBackground(() -> tampil());
                }
            }
        });
        BtnCari = new widget.Button();
        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setPreferredSize(new Dimension(28, 26));
        BtnCari.addActionListener(evt -> runBackground(() -> tampil()));

        BtnAll = new widget.Button();
        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setText("Semua");
        BtnAll.setPreferredSize(new Dimension(85, 26));
        BtnAll.addActionListener(evt -> { TCari.setText(""); runBackground(() -> tampil()); });

        LCount = new widget.Label();
        LCount.setText("0");
        LCount.setPreferredSize(new Dimension(60, 26));

        panelGlass9.add(new JLabel("Tanggal:"));
        panelGlass9.add(DTPCari1);
        panelGlass9.add(new JLabel("s.d"));
        panelGlass9.add(DTPCari2);
        panelGlass9.add(new JLabel("   Kata Kunci:"));
        panelGlass9.add(TCari);
        panelGlass9.add(BtnCari);
        panelGlass9.add(BtnAll);
        panelGlass9.add(new JLabel("   Jml:"));
        panelGlass9.add(LCount);
        internalFrame3.add(panelGlass9, BorderLayout.PAGE_END);

        LoadHTML = new widget.editorpane();
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(false);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(".isi td{border-right:1px solid #e2e7dd;font:9px tahoma;height:12px;"
                + "border-bottom:1px solid #e2e7dd;background:#ffffff;color:#323232;}");
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);

        ScrollHTML = new widget.ScrollPane();
        ScrollHTML.setViewportView(LoadHTML);

        TNoRM1 = new widget.TextBox(); TNoRM1.setPreferredSize(new Dimension(90, 24)); TNoRM1.setEditable(false);
        TPasien1 = new widget.TextBox(); TPasien1.setPreferredSize(new Dimension(170, 24)); TPasien1.setEditable(false);
        BtnPrint1 = new widget.Button();
        BtnPrint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item (copy).png")));
        BtnPrint1.setPreferredSize(new Dimension(28, 24));
        BtnPrint1.setToolTipText("Cetak asesmen pasien ini");
        BtnPrint1.addActionListener(evt -> cetakTerpilih());

        FormMenu = new widget.panelisi();
        FormMenu.setPreferredSize(new Dimension(44, 38));
        FormMenu.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 6));
        FormMenu.add(new JLabel("Pasien:"));
        FormMenu.add(TNoRM1);
        FormMenu.add(TPasien1);
        FormMenu.add(BtnPrint1);

        ChkAccor = new widget.CekBox();
        ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png")));
        ChkAccor.setPreferredSize(new Dimension(15, 15));
        ChkAccor.addActionListener(evt -> isMenu());

        PanelAccor = new widget.PanelBiasa();
        PanelAccor.setLayout(new BorderLayout(1, 1));
        PanelAccor.setPreferredSize(new Dimension(470, 44));
        PanelAccor.add(ChkAccor, BorderLayout.WEST);
        PanelAccor.add(FormMenu, BorderLayout.NORTH);
        PanelAccor.add(ScrollHTML, BorderLayout.CENTER);
        internalFrame3.add(PanelAccor, BorderLayout.EAST);

        TabRawat.addTab("Data Tersimpan", internalFrame3);
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (TabRawat.getSelectedIndex() == 1) {
                    runBackground(() -> tampil());
                }
            }
        });
    }

    private void isMenu() {
        if (ChkAccor.isSelected()) {
            ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png")));
            PanelAccor.setPreferredSize(new Dimension(470, 44));
            FormMenu.setVisible(true);
            ScrollHTML.setVisible(true);
        } else {
            ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png")));
            PanelAccor.setPreferredSize(new Dimension(15, 44));
            FormMenu.setVisible(false);
            ScrollHTML.setVisible(false);
        }
        PanelAccor.revalidate();
        PanelAccor.repaint();
    }

    // =================================================================
    //  7. NAVIGASI, TOGGLE VIEW & VALIDASI PROGRESS
    // =================================================================

    private void pindahBagian(int idx) {
        activeSectionIndex = idx;
        for (int i = 0; i < 9; i++) {
            boolean active = (i == idx);
            navItems[i].setBackground(active ? TEAL_LIGHT : CARD_BG);
            navItems[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, active ? 3 : 0, 1, 0,
                            active ? TEAL_ACCENT : new Color(0xf4, 0xf5, 0xf2)),
                    new EmptyBorder(8, 10, 8, 12)));

            Component[] comps = navItems[i].getComponents();
            if (comps.length >= 2) {
                comps[0].setForeground(active ? TEAL_ACCENT : TEXT_MUTED);
                comps[1].setForeground(active ? TEAL_ACCENT : TEXT_LABEL);
                if (comps[1] instanceof JLabel) {
                    ((JLabel) comps[1]).setFont(new Font(ThemeConfig.FONT_FAMILY, active ? Font.BOLD : Font.PLAIN, 11));
                }
            }

            boolean relevan = i == 3 ? isBalitaVisible : (i == 4 ? isReproVisible : true);
            boolean tampil = relevan && (showAllMode || active);
            sectionCards[i].setVisible(tampil);
            // Jarak 14px hanya diperlukan di mode semua bagian. Jika tetap
            // terlihat saat card disembunyikan, jaraknya menumpuk dan membuat
            // bagian VII/VIII/IX makin turun dari puncak viewport.
            sectionSpacers[i].setVisible(showAllMode && tampil);
        }

        if (showAllMode && sectionCards[idx].isVisible()) {
            SwingUtilities.invokeLater(() -> {
                scrollInput.getViewport().setViewPosition(new java.awt.Point(0, sectionCards[idx].getY()));
            });
        } else if (!showAllMode) {
            SwingUtilities.invokeLater(() ->
                    scrollInput.getViewport().setViewPosition(new java.awt.Point(0, 0)));
        }

        SectionsWrapper.revalidate();
        SectionsWrapper.repaint();
        rapikanWrapSetelahLayout();
    }

    /**
     * WrapLayout baru mengetahui tinggi final setelah viewport memberi lebar
     * nyata. Dua putaran relayout membuat BoxLayout mengambil tinggi hasil
     * bungkus, sehingga baris kedua tidak terpotong.
     */
    private void rapikanWrapSetelahLayout() {
        if (SectionsWrapper == null) return;
        SwingUtilities.invokeLater(() -> {
            invalidasiPohonLayout(SectionsWrapper);
            SectionsWrapper.revalidate();
            SwingUtilities.invokeLater(() -> {
                invalidasiPohonLayout(SectionsWrapper);
                SectionsWrapper.revalidate();
                SectionsWrapper.repaint();
            });
        });
    }

    /** Hapus cache ukuran BoxLayout sampai ke panel wrap terdalam. */
    private static void invalidasiPohonLayout(Component c) {
        if (c instanceof java.awt.Container) {
            java.awt.Container wadah = (java.awt.Container) c;
            for (Component anak : wadah.getComponents()) {
                invalidasiPohonLayout(anak);
            }
            wadah.invalidate();
        }
    }

    private void toggleViewMode() {
        showAllMode = !showAllMode;
        BtnToggleMode.setText(showAllMode ? "Tampilkan Per Bagian" : "Tampilkan Semua Bagian");

        pindahBagian(activeSectionIndex);
    }

    private boolean isBalitaVisible = false;
    private boolean isReproVisible = false;

    private void pasangListenersValidasi() {
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateProgress(); }
            @Override public void removeUpdate(DocumentEvent e) { updateProgress(); }
            @Override public void changedUpdate(DocumentEvent e) { updateProgress(); }
        };
        TAreaKeluhanUtama.getDocument().addDocumentListener(dl);
        TAreaRiwayatKesehatan.getDocument().addDocumentListener(dl);
    }

    private void updateProgress() {
        int wajibTerisi = 0;
        int totalWajib = 3;

        boolean keluhanOk = !TAreaKeluhanUtama.getText().trim().isEmpty();
        boolean riwayatOk = !TAreaRiwayatKesehatan.getText().trim().isEmpty();
        boolean keadaanOk = pilihan(bgKeadaanUmum) != null;

        if (keluhanOk) wajibTerisi++;
        if (riwayatOk) wajibTerisi++;
        if (keadaanOk) wajibTerisi++;

        int pct = (wajibTerisi * 100) / totalWajib;
        LblProgressPct.setText(pct + "%");
        LblProgressNote.setText(wajibTerisi + "/" + totalWajib + " isian wajib terisi");

        int barW = (270 * pct) / 100;
        ProgressBarFill.setPreferredSize(new Dimension(barW, 6));
        ProgressBarFill.setBackground(pct == 100 ? GREEN_FG : TEAL_ACCENT);
        ProgressBarFill.revalidate();
        ProgressBarFill.repaint();

        // Update tally seksi 1
        int s1Terisi = (keluhanOk ? 1 : 0) + (riwayatOk ? 1 : 0);
        navCounts[0].setText(s1Terisi + "/2");
        if (s1Terisi == 2) {
            setTallyBadge(0, "Wajib lengkap", GREEN_BG, GREEN_FG);
        } else {
            setTallyBadge(0, s1Terisi + "/2 wajib terisi", ORANGE_BG, ORANGE_FG);
        }

        // Update tally seksi 2
        navCounts[1].setText(keadaanOk ? "1/1" : "0/1");
        if (keadaanOk) {
            setTallyBadge(1, "Wajib lengkap", GREEN_BG, GREEN_FG);
        } else {
            setTallyBadge(1, "0/1 wajib terisi", ORANGE_BG, ORANGE_FG);
        }

        if (pct == 100) {
            LblValidationFooter.setText("Semua isian wajib terpenuhi · Siap disimpan");
            LblValidationFooter.setForeground(GREEN_FG);
        } else {
            LblValidationFooter.setText("Isian wajib " + wajibTerisi + "/" + totalWajib + " · " + (totalWajib - wajibTerisi) + " belum terisi");
            LblValidationFooter.setForeground(TEXT_LABEL_MUTED);
        }
    }

    private void setTallyBadge(int secIdx, String text, Color bg, Color fg) {
        if (sectionTallies[secIdx] != null) {
            sectionTallies[secIdx].setText(text);
            sectionTallies[secIdx].setBackground(bg);
            sectionTallies[secIdx].setForeground(fg);
        }
    }

    // =================================================================
    //  8. LAPISAN DATA: NILAI & PEMETAAN
    // =================================================================

    private String nilai(String kolom) {
        if ("tanggal".equals(kolom)) {
            return Valid.SetTgl(TglAsesmen.getSelectedItem() + "");
        }
        if ("jam".equals(kolom)) {
            return JamAsesmen.getText().trim().isEmpty()
                    ? TglAsesmen.getSelectedItem().toString().substring(11, 19)
                    : JamAsesmen.getText().trim();
        }
        if ("nip".equals(kolom)) {
            return kosongKeNull(NipPerawat.getText());
        }
        Object k = kendali.get(kolom);
        if (k instanceof GrupCentang) {
            return ((GrupCentang) k).nilai();
        }
        if (k instanceof KanvasNyeri) {
            return kosongKeNull(((KanvasNyeri) k).getCoretanBase64());
        }
        if (k instanceof widget.CekBox) {
            return ((widget.CekBox) k).isSelected() ? "Ya" : "Tidak";
        }
        if (k instanceof ButtonGroup) {
            return pilihan((ButtonGroup) k);
        }
        if (k == TglKontrol) {
            return AdaKontrol.isSelected() ? Valid.SetTgl(TglKontrol.getSelectedItem() + "") : null;
        }
        if (k instanceof javax.swing.text.JTextComponent) {
            return kosongKeNull(((javax.swing.text.JTextComponent) k).getText());
        }
        return null;
    }

    private void set(String kolom, String isi) {
        Object k = kendali.get(kolom);
        if (k instanceof GrupCentang) {
            ((GrupCentang) k).set(isi);
        } else if (k instanceof KanvasNyeri) {
            ((KanvasNyeri) k).setCoretanBase64(isi);
        } else if (k instanceof widget.CekBox) {
            ((widget.CekBox) k).setSelected("Ya".equalsIgnoreCase(teksAman(isi)));
        } else if (k instanceof ButtonGroup) {
            pilihRadio((ButtonGroup) k, isi);
        } else if (k == TglKontrol) {
            boolean ada = isi != null && !isi.trim().isEmpty() && !isi.startsWith("0000");
            AdaKontrol.setSelected(ada);
            TglKontrol.setEnabled(ada);
            if (ada) {
                Valid.SetTgl2(TglKontrol, isi + " 00:00:00");
            }
        } else if (k instanceof javax.swing.text.JTextComponent) {
            ((javax.swing.text.JTextComponent) k).setText(teksAman(isi));
        }
    }

    private String kosongKeNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private String pilihan(ButtonGroup grup) {
        Enumeration<AbstractButton> e = grup.getElements();
        while (e.hasMoreElements()) {
            AbstractButton b = e.nextElement();
            if (b.isSelected()) {
                return b.getText();
            }
        }
        return null;
    }

    private void pilihRadio(ButtonGroup grup, String nilai) {
        grup.clearSelection();
        if (nilai == null) return;
        Enumeration<AbstractButton> e = grup.getElements();
        while (e.hasMoreElements()) {
            AbstractButton b = e.nextElement();
            if (b.getText().equalsIgnoreCase(nilai)) {
                b.setSelected(true);
                return;
            }
        }
    }

    private void simpan() {
        if (TNoRw.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "Silahkan pilih pasien terlebih dahulu..!!");
            return;
        }
        StringBuilder kol = new StringBuilder("no_rawat");
        StringBuilder tanya = new StringBuilder("?");
        for (String k : KOLOM) {
            kol.append(",").append(k);
            tanya.append(",?");
        }
        try (PreparedStatement pstm = koneksi.prepareStatement(
                "insert into asesmen_awal_keperawatan_ralan(" + kol + ") values(" + tanya + ")")) {
            pstm.setString(1, TNoRw.getText());
            for (int n = 0; n < KOLOM.length; n++) {
                pstm.setString(n + 2, nilai(KOLOM[n]));
            }
            pstm.executeUpdate();
            runBackground(() -> tampil());
            TabRawat.setSelectedIndex(1);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan : " + e.getMessage()
                    + "\n\nKalau pesannya soal duplikat, kunjungan ini sudah punya lembar RM 02-2."
                    + "\nSilahkan pilih barisnya di tab Data lalu tekan Ganti.");
        }
    }

    private void aksiGanti() {
        if (tbObat.getSelectedRow() > -1) {
            ganti();
        } else {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
        }
    }

    private void ganti() {
        StringBuilder setSql = new StringBuilder();
        String[] param = new String[KOLOM.length + 1];
        for (int n = 0; n < KOLOM.length; n++) {
            if (setSql.length() > 0) setSql.append(",");
            setSql.append(KOLOM[n]).append("=?");
            param[n] = nilai(KOLOM[n]);
        }
        param[KOLOM.length] = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();

        if (Sequel.mengedittf("asesmen_awal_keperawatan_ralan", "no_rawat=?", setSql.toString(),
                param.length, param)) {
            runBackground(() -> tampil());
            TabRawat.setSelectedIndex(1);
        }
    }

    private void aksiHapus() {
        if (tbObat.getSelectedRow() > -1) {
            if (Sequel.queryu2tf("delete from asesmen_awal_keperawatan_ralan where no_rawat=?", 1,
                    new String[]{tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()})) {
                tabMode.removeRow(tbObat.getSelectedRow());
                LCount.setText("" + tabMode.getRowCount());
            } else {
                JOptionPane.showMessageDialog(null, "Gagal menghapus..!!");
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
        }
    }

    private String sqlDaftar(boolean pakaiCari) {
        String sql = "select a.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,a.tanggal,a.jam,"
                + "ifnull(a.keluhan_utama,'') as keluhan_utama,ifnull(a.nip,'') as nip,"
                + "ifnull(pegawai.nama,'') as nm_perawat "
                + "from asesmen_awal_keperawatan_ralan a "
                + "inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join pegawai on a.nip=pegawai.nik "
                + "where a.tanggal between ? and ? ";
        if (pakaiCari) {
            sql += "and (a.no_rawat like ? or pasien.no_rkm_medis like ? "
                    + "or pasien.nm_pasien like ? or pegawai.nama like ?) ";
        }
        return sql + "order by a.tanggal,a.jam";
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        boolean pakaiCari = !TCari.getText().trim().isEmpty();
        try {
            ps = koneksi.prepareStatement(sqlDaftar(pakaiCari));
            try {
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
                if (pakaiCari) {
                    for (int p = 3; p <= 6; p++) {
                        ps.setString(p, "%" + TCari.getText() + "%");
                    }
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"), rs.getString("no_rkm_medis"), rs.getString("nm_pasien"),
                        rs.getDate("tgl_lahir"), rs.getString("jk"), rs.getString("tanggal"),
                        rs.getString("jam"), rs.getString("keluhan_utama"), rs.getString("nip"),
                        rs.getString("nm_perawat")
                    });
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    private void getData() {
        if (tbObat.getSelectedRow() == -1) return;
        TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
        muatIdentitas();
        if (adaRecord(TNoRw.getText())) {
            muatRecord(TNoRw.getText());
        }
        bangunRingkasanHtml();
        ChkAccor.setSelected(true);
        isMenu();
    }

    private void isiDariRecord() throws Exception {
        String tgl = teksAman(rs.getString("tanggal"));
        String jam = teksAman(rs.getString("jam"));
        if (!tgl.isEmpty()) {
            Valid.SetTgl2(TglAsesmen, tgl + " " + (jam.isEmpty() ? "00:00:00" : jam));
            JamAsesmen.setText(jam);
        }
        for (String k : KOLOM) {
            if ("tanggal".equals(k) || "jam".equals(k)) continue;
            if ("nip".equals(k)) {
                String nip = teksAman(rs.getString("nip"));
                NipPerawat.setText(nip);
                NmPerawat.setText(nip.isEmpty() ? "" : teksAman(
                        Sequel.cariIsi("select nama from pegawai where nik=?", nip)));
                continue;
            }
            set(k, rs.getString(k));
        }

        if (adaIsi(BALITA_CENTANG, BALITA_TONGGAK)) {
            isBalitaVisible = true;
            sectionCards[3].setVisible(true);
        }
        if (adaIsiRepro()) {
            isReproVisible = true;
            sectionCards[4].setVisible(true);
        }
        updateProgress();
    }

    private String teksAman(String s) {
        return s == null ? "" : s;
    }

    public void emptTeks() {
        for (String k : KOLOM) {
            if ("tanggal".equals(k) || "jam".equals(k) || "nip".equals(k)) continue;
            Object c = kendali.get(k);
            if (c instanceof GrupCentang) {
                ((GrupCentang) c).kosongkan();
            } else if (c instanceof KanvasNyeri) {
                ((KanvasNyeri) c).hapusCoretan();
            } else if (c instanceof widget.CekBox) {
                ((widget.CekBox) c).setSelected(false);
            } else if (c instanceof ButtonGroup) {
                ((ButtonGroup) c).clearSelection();
            } else if (c instanceof javax.swing.text.JTextComponent) {
                ((javax.swing.text.JTextComponent) c).setText("");
            }
        }
        AdaKontrol.setSelected(false);
        TglKontrol.setEnabled(false);
        TglKontrol.setDate(new Date());
        TglAsesmen.setDate(new Date());
        JamAsesmen.setText(new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()));
        TabRawat.setSelectedIndex(0);
        updateProgress();
        pindahBagian(0);
    }

    private void muatIdentitas() {
        try {
            ps = koneksi.prepareStatement(
                    "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.jk,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as kelamin,"
                    + "reg_periksa.umurdaftar,reg_periksa.sttsumur,"
                    + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,"
                    + "ifnull(poliklinik.nm_poli,'-') as nm_poli "
                    + "from reg_periksa "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "
                    + "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    String norm = teksAman(rs.getString("no_rkm_medis"));
                    String nm = teksAman(rs.getString("nm_pasien"));
                    String tglLahir = teksAman(rs.getString("tgl_lahir"));
                    String umur = teksAman(rs.getString("umur"));
                    String jk = teksAman(rs.getString("kelamin"));
                    String poli = teksAman(rs.getString("nm_poli"));

                    TNoRM.setText(norm);
                    TPasien.setText(nm);
                    TglLahir.setText(tglLahir);
                    TUmur.setText(umur);
                    Jk.setText(jk);
                    TPoli.setText(poli);

                    LblNoRM.setText("No. RM: " + norm);
                    LblNoRw.setText("No. Rawat: " + TNoRw.getText());
                    LblPoli.setText("Poli: " + poli);
                    LblNmPasien.setText(nm);
                    LblInfoPasien.setText(jk + " · " + umur);

                    // Avatar inisial
                    String[] parts = nm.trim().split("\\s+");
                    String initials = "";
                    if (parts.length >= 2) {
                        initials = (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
                    } else if (parts.length == 1 && !parts[0].isEmpty()) {
                        initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
                    }
                    LblAvatar.setText(initials.isEmpty() ? "PS" : initials);

                    TNoRM1.setText(norm);
                    TPasien1.setText(nm);

                    aturBlokBersyarat(teksAman(rs.getString("jk")),
                            teksAman(rs.getString("umurdaftar")),
                            teksAman(rs.getString("sttsumur")));
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    private void aturBlokBersyarat(String jk, String umurdaftar, String sttsumur) {
        boolean balita;
        String satuan = sttsumur.trim();
        if (satuan.equalsIgnoreCase("Bl") || satuan.equalsIgnoreCase("Hr")) {
            balita = true;
        } else {
            int tahun = -1;
            try {
                tahun = Integer.parseInt(umurdaftar.trim());
            } catch (NumberFormatException ignored) {}
            balita = tahun >= 0 && tahun < 5;
        }
        isBalitaVisible = balita;
        isReproVisible = "P".equalsIgnoreCase(jk.trim());

        navItems[3].setVisible(isBalitaVisible);
        navItems[4].setVisible(isReproVisible);

        pindahBagian(activeSectionIndex);
    }

    private boolean adaIsi(String[][]... kelompok) {
        for (String[][] grup : kelompok) {
            for (String[] baris : grup) {
                Object k = kendali.get(baris[0]);
                if (k instanceof GrupCentang && ((GrupCentang) k).terisi()) {
                    return true;
                }
                if (k instanceof javax.swing.text.JTextComponent
                        && !((javax.swing.text.JTextComponent) k).getText().trim().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean adaIsiRepro() {
        for (String k : KOLOM) {
            if ((k.startsWith("repro_") || k.startsWith("obs_") || k.startsWith("palpasi_")
                    || k.startsWith("janin_")) && nilai(k) != null) {
                return true;
            }
        }
        return false;
    }

    private void bangunRingkasanHtml() {
        if (TNoRw.getText().trim().isEmpty()) {
            LoadHTML.setText("");
            return;
        }
        StringBuilder h = new StringBuilder();
        h.append("<html><body><table width='100%' cellspacing='0' border='0' class='isi'>");
        h.append("<tr class='isi'><td colspan='2' bgcolor='#FFFAF8'><b>ASESMEN AWAL KEPERAWATAN"
                + " RAWAT JALAN (RM 02-2)</b></td></tr>");
        barisHtml(h, "Pasien", TNoRM.getText() + " - " + TPasien.getText());
        barisHtml(h, "Umur / L,P", TUmur.getText() + " / " + Jk.getText());
        barisHtml(h, "Perawat/Bidan", NmPerawat.getText());

        judulHtml(h, "Riwayat Kesehatan");
        barisHtml(h, "Diagnosa medis", nilai("diagnosa_medis"));
        barisHtml(h, "Keluhan utama", nilai("keluhan_utama"));
        barisHtml(h, "Riwayat kesehatan", nilai("riwayat_kesehatan"));

        judulHtml(h, "Pemeriksaan Fisik");
        barisHtml(h, "Keadaan umum", nilai("keadaan_umum"));
        barisHtml(h, "Tanda vital", "TD " + teksAman(nilai("td_sistolik")) + "/"
                + teksAman(nilai("td_diastolik")) + " mmHg, HR " + teksAman(nilai("hr"))
                + ", RR " + teksAman(nilai("rr")) + ", Suhu " + teksAman(nilai("suhu"))
                + ", VAS " + teksAman(nilai("vas")));
        barisHtml(h, "Gambar skala nyeri",
                nilai("nyeri_canvas_gambar") == null ? "tidak ditandai" : "ada tanda");
        for (String[] reg : REGIO) {
            String v = nilai(reg[0]);
            if (v != null) {
                barisHtml(h, reg[1], v);
            }
        }
        barisHtml(h, "CRT", nilai("crt"));

        judulHtml(h, "Pola Kebiasaan");
        for (String[] kb : KEBIASAAN) {
            barisHtml(h, kb[1], nilai(kb[0]));
        }

        if (sectionCards[3].isVisible()) {
            judulHtml(h, "Pertumbuhan dan Perkembangan Balita");
            for (String[] b : BALITA_CENTANG) {
                barisHtml(h, b[1], nilai(b[0]));
            }
            StringBuilder t = new StringBuilder();
            for (String[] tg : BALITA_TONGGAK) {
                String v = nilai(tg[0]);
                if (v != null) {
                    t.append(t.length() > 0 ? ", " : "").append(tg[1]).append(" ").append(v).append(" bln");
                }
            }
            barisHtml(h, "Perkembangan", t.toString());
        }

        if (sectionCards[4].isVisible()) {
            judulHtml(h, "Kesehatan Reproduksi");
            for (String[] rp : REPRO_CENTANG) {
                barisHtml(h, rp[1], nilai(rp[0]));
            }
            barisHtml(h, "Obstetri", "G" + teksAman(nilai("obs_g")) + " P" + teksAman(nilai("obs_p"))
                    + " A" + teksAman(nilai("obs_o")));
        }

        judulHtml(h, "Psikologis, Sosiologis, Spiritual");
        for (String[] ps : PSIKOSOSIAL) {
            barisHtml(h, ps[1], nilai(ps[0]));
        }

        judulHtml(h, "Diagnosa Keperawatan");
        StringBuilder dx = new StringBuilder();
        for (int n = 0; n < PILIHAN_DIAGNOSA.length; n += 2) {
            if ("Ya".equalsIgnoreCase(nilai(PILIHAN_DIAGNOSA[n]))) {
                dx.append(dx.length() > 0 ? "<br>" : "").append("- ").append(PILIHAN_DIAGNOSA[n + 1]);
            }
        }
        barisHtml(h, "Ditegakkan", dx.length() == 0 ? "-" : dx.toString());

        h.append("</table></body></html>");
        LoadHTML.setText(h.toString());
        LoadHTML.setCaretPosition(0);
    }

    private void judulHtml(StringBuilder h, String judul) {
        h.append("<tr class='isi'><td colspan='2' bgcolor='#FFFAF8'><b>").append(judul).append("</b></td></tr>");
    }

    private void barisHtml(StringBuilder h, String label, String nilai) {
        h.append("<tr class='isi'><td width='34%' valign='top'>").append(label)
         .append("</td><td valign='top'>").append(teksAman(nilai).replace("\n", "<br>")).append("</td></tr>");
    }

    private void cetakTerpilih() {
        if (tbObat.getSelectedRow() <= -1 && TNoRw.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
            return;
        }
        String noRawat = tbObat.getSelectedRow() > -1
                ? tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
                : TNoRw.getText();
        String nip = tbObat.getSelectedRow() > -1 && tbObat.getValueAt(tbObat.getSelectedRow(), 8) != null
                ? tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString()
                : NipPerawat.getText();
        String nama = tbObat.getSelectedRow() > -1 && tbObat.getValueAt(tbObat.getSelectedRow(), 9) != null
                ? tbObat.getValueAt(tbObat.getSelectedRow(), 9).toString()
                : NmPerawat.getText();

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        param.put("norawat", noRawat);
        param.put("finger", tandaTangan(nip, nama));
        param.put("gambarnyeri", KanvasNyeri.gambarCetak(Sequel.cariIsi(
                "select nyeri_canvas_gambar from asesmen_awal_keperawatan_ralan where no_rawat=?",
                noRawat)));

        String[] pilihan = {"Lembar", "PDF", "Batal"};
        int jawab = JOptionPane.showOptionDialog(rootPane,
                "Cetak dalam bentuk apa?", "Cetak Asesmen Awal Keperawatan",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                pilihan, pilihan[0]);
        String judul = "::[ Asesmen Awal Keperawatan Rawat Jalan ]::";
        if (jawab == 0) {
            Valid.MyReport("rptAsesmenAwalKeperawatanRalanRM02.jasper", "report", judul, param);
        } else if (jawab == 1) {
            Valid.MyReportPDF("rptAsesmenAwalKeperawatanRalanRM02.jasper", "report", judul, param);
        }
    }

    private String tandaTangan(String nip, String nama) {
        String finger = Sequel.cariIsi(
                "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id "
                + "where pegawai.nik=?", nip == null ? "" : nip);
        return "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                + "\nDitandatangani secara elektronik oleh " + nama
                + "\nID " + (finger == null || finger.isEmpty() ? nip : finger);
    }

    private void pilihPetugas() {
        kepegawaian.DlgCariPetugas petugas = new kepegawaian.DlgCariPetugas(null, false);
        petugas.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                JTable t = petugas.getTable();
                if (t.getSelectedRow() != -1) {
                    NipPerawat.setText(t.getValueAt(t.getSelectedRow(), 0).toString());
                    NmPerawat.setText(t.getValueAt(t.getSelectedRow(), 1).toString());
                }
            }
        });
        petugas.emptTeks();
        petugas.isCek();
        petugas.setSize(getWidth() - 60, getHeight() - 60);
        petugas.setLocationRelativeTo(this);
        petugas.setVisible(true);
    }

    private void bersihkanIkonRadio(java.awt.Container wadah) {
        for (Component c : wadah.getComponents()) {
            if (c instanceof javax.swing.JRadioButton) {
                ((javax.swing.JRadioButton) c).setIcon(null);
            } else if (c instanceof java.awt.Container) {
                bersihkanIkonRadio((java.awt.Container) c);
            }
        }
    }

    // =================================================================
    //  API PEMANGGIL (DlgRawatJalan)
    // =================================================================

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        muatIdentitas();
        if (adaRecord(norwt)) {
            muatRecord(norwt);
        }
        bangunRingkasanHtml();
        updateProgress();
    }

    private boolean adaRecord(String noRawat) {
        String ada = Sequel.cariIsi(
                "select no_rawat from asesmen_awal_keperawatan_ralan where no_rawat=?", noRawat);
        return ada != null && !ada.trim().isEmpty();
    }

    private void muatRecord(String noRawat) {
        try {
            ps = koneksi.prepareStatement(
                    "select * from asesmen_awal_keperawatan_ralan where no_rawat=?");
            try {
                ps.setString(1, noRawat);
                rs = ps.executeQuery();
                if (rs.next()) {
                    isiDariRecord();
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    public void setTampil() {
        TabRawat.setSelectedIndex(1);
    }

    public void isCek() {
        if (NipPerawat.getText().trim().isEmpty()) {
            String nip = akses.getkode();
            if (nip != null && !nip.trim().isEmpty() && !"Admin Utama".equals(nip)) {
                NipPerawat.setText(nip);
                NmPerawat.setText(teksAman(
                        Sequel.cariIsi("select nama from pegawai where nik=?", nip)));
            }
        }
    }

    private void runBackground(Runnable task) {
        if (ceksukses || executor.isShutdown() || executor.isTerminated() || !isDisplayable()) {
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
        executor.shutdownNow();
        super.dispose();
    }
}
