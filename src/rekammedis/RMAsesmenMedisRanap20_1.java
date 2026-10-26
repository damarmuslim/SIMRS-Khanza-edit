package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariDokter;
import laporan.DlgCariPenyakit;
import widget.ThemeConfig;

/**
 * ASESMEN MEDIS RAWAT INAP (RM 20-1) - KONSOLIDASI TUNGGAL
 * RSUD Demang Sepulau Raya.
 *
 * Form terpadu hasil penggabungan RM 20 dan RM 20-1 sesuai berkas fisik rekam medis:
 * - Format vertikal scroll utuh (tanpa tab pemisah formulir pengkajian).
 * - Side rail navigasi cepat, indikator kelengkapan live, dan rujukan riwayat.
 * - Mencakup 7 seksi lengkap: Anamnesis, TTV & GCS, Penunjang, 14 Organ Fisik,
 *   Kanvas Status Lokalis Anatomi Tubuh 4 View interaktif, Asesmen, dan Program.
 * - Adapter Multi-Sumber: Otomatis membaca dan menarik (pre-fill) riwayat lama
 *   dari `penilaian_medis_ranap` (Medis Umum lama) maupun `asesmen_medis_ranap`.
 */
public final class RMAsesmenMedisRanap20_1 extends javax.swing.JDialog {

    private static final String[] KOLOM_RM20_1 = {
        "tanggal", "kd_dokter_periksa", "kd_dokter_dpjp", "anamnesis", "hubungan",
        "asal_masuk", "cara_masuk",
        "keluhan_utama", "riwayat_penyakit", "riwayat_pengobatan",
        "alergi_obat", "alergi_makanan", "riwayat_lain", "status_ekonomi",
        "keadaan_umum", "kesadaran", "gcs_e", "gcs_m", "gcs_v",
        "td_sistol", "td_diastol", "nadi", "rr", "suhu", "spo2",
        "tb", "bb", "imt", "status_pediatrik",
        "pemeriksaan_penunjang",
        "fisik_kepala", "fisik_kepala_ket",
        "fisik_mata", "fisik_mata_ket",
        "fisik_tht", "fisik_tht_ket",
        "fisik_leher", "fisik_leher_ket",
        "fisik_mulut", "fisik_mulut_ket",
        "fisik_jantung", "fisik_jantung_ket",
        "fisik_thoraks", "fisik_thoraks_ket",
        "fisik_abdomen", "fisik_abdomen_ket",
        "fisik_kulit", "fisik_kulit_ket",
        "fisik_tulang", "fisik_tulang_ket",
        "fisik_saraf", "fisik_saraf_ket",
        "fisik_genitalia", "fisik_genitalia_ket",
        "fisik_lokasi", "fisik_lokasi_ket",
        "fisik_lain", "fisik_lain_ket",
        "lokalis_canvas",
        "diagnosa_medis", "diagnosa_banding", "advis_terapi", "rencana_tindakan"
    };

    private static final javax.swing.Icon ICON_RADIO_ON = new javax.swing.Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x0D, 0x7A, 0x70));
            g2.setStroke(new java.awt.BasicStroke(1.2f));
            g2.drawOval(x, y + 1, 13, 13);
            g2.fillOval(x + 3, y + 4, 7, 7);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 14; }
        @Override public int getIconHeight() { return 14; }
    };

    private static final javax.swing.Icon ICON_RADIO_OFF = new javax.swing.Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xB3, 0xB8, 0xB3));
            g2.setStroke(new java.awt.BasicStroke(1.2f));
            g2.drawOval(x, y + 1, 13, 13);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 14; }
        @Override public int getIconHeight() { return 14; }
    };

    private static final javax.swing.Icon ICON_KOSONG = new javax.swing.Icon() {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {}
        @Override public int getIconWidth() { return 0; }
        @Override public int getIconHeight() { return 0; }
    };

    // Palet Warna Presisi Sesuai Desain Modern Argon (Tailwind Slate & Medical Teal)
    public static final Color COL_HEADER_DARK = new Color(10, 46, 44);      // #0A2E2C (bg-med-950)
    public static final Color COL_TEAL_PRIMARY = new Color(13, 148, 136);   // #0D9488 (med-600)
    public static final Color COL_TEAL_DARK = new Color(15, 118, 110);      // #0F766E (med-700)
    public static final Color COL_TEAL_SOFT = new Color(240, 253, 250);     // #F0FDFA (med-50)
    public static final Color COL_TEAL_BORDER = new Color(153, 246, 228);   // #99F6E4 (med-200)
    public static final Color COL_BG_MAIN = new Color(241, 245, 249);       // #F1F5F9 (bg-slate-100)
    public static final Color COL_CARD_BG = Color.WHITE;                    // #FFFFFF (bg-white)
    public static final Color COL_CARD_HEADER = new Color(248, 250, 252);   // #F8FAFC (bg-slate-50/80)
    public static final Color COL_BORDER = new Color(203, 213, 225);        // #CBD5E1 (slate-300 - tegas, solid, TIDAK PUDAR)
    public static final Color COL_BORDER_DIVIDER = new Color(218, 225, 233);// #DAE1E9 (border-b card header jelas)
    public static final Color COL_INPUT_BG = new Color(248, 250, 252);      // #F8FAFC (bg-slate-50/60)
    public static final Color COL_INPUT_BORDER = new Color(195, 206, 219);  // #C3CEDB (border-slate-300 tegas & rapi)
    public static final Color COL_TEXT_DARK = new Color(15, 23, 42);        // #0F172A (text-slate-900)
    public static final Color COL_TEXT_MUTED = new Color(100, 116, 139);    // #64748B (text-slate-500)
    public static final Color COL_ROSE_PRIMARY = new Color(225, 29, 72);   // #E11D48
    public static final Color COL_ROSE_SOFT = new Color(255, 241, 242);    // #FFF1F2
    public static final Color COL_ROSE_BORDER = new Color(254, 205, 211);  // #FECDD3
    public static final Color COL_AMBER_SOFT = new Color(254, 252, 232);   // #FEFCE8
    public static final Color COL_AMBER_BORDER = new Color(253, 230, 138); // #FDE68A

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_SUBLABEL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Monospaced", Font.BOLD, 12);

    private static final class BorderChipMedis implements javax.swing.border.Border {
        private Color garis = COL_INPUT_BORDER;
        public void setGaris(Color c) { this.garis = c; }
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(4, 6, 4, 6);
        }
        @Override public boolean isBorderOpaque() { return false; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(garis);
            g2.drawRoundRect(x, y, w - 1, h - 1, 6, 6);
            g2.dispose();
        }
    }

    private JPanel panelGridPilihan(int kolom, int hgap, JToggleButton... tombol) {
        JPanel p = new JPanel(new java.awt.GridLayout(1, kolom, hgap, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(new Color(241, 245, 249)); // bg-slate-100
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.0f;
                float offset = strokeW / 2.0f;
                g2.setColor(new Color(226, 232, 240)); // border-slate-200
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(
                        offset, offset, getWidth() - strokeW, getHeight() - strokeW, 8, 8));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, 34));
        p.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));

        for (JToggleButton t : tombol) {
            pasangSegmentedButton(t, p);
            p.add(t);
        }
        return p;
    }

    private void pasangSegmentedButton(final JToggleButton t, final JPanel parent) {
        t.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI() {
            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                AbstractButton b = (AbstractButton) c;
                javax.swing.ButtonModel model = b.getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = c.getWidth();
                int h = c.getHeight();

                if (b.isSelected()) {
                    if ("Tidak Normal".equalsIgnoreCase(b.getText().trim())) {
                        g2.setColor(COL_ROSE_PRIMARY); // #E11D48
                    } else {
                        g2.setColor(COL_TEAL_PRIMARY); // #0D9488
                    }
                    g2.fill(new java.awt.geom.RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 6, 6));
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                } else if (model.isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 180));
                    g2.fill(new java.awt.geom.RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 6, 6));
                    g2.setColor(COL_TEXT_DARK);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                } else {
                    g2.setColor(new Color(71, 85, 105)); // text-slate-600
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                }

                java.awt.FontMetrics fm = g2.getFontMetrics();
                String text = b.getText();
                int textX = (w - fm.stringWidth(text)) / 2;
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, textX, textY);

                g2.dispose();
            }
        });
        t.setOpaque(false);
        t.setContentAreaFilled(false);
        t.setBorderPainted(false);
        t.setFocusPainted(false);
        t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        t.setPreferredSize(new Dimension(0, 28));
        t.addItemListener(e -> {
            if (parent != null) parent.repaint();
            t.repaint();
        });
    }

    private void pasangChipMedis(final JToggleButton t) {
        pasangSegmentedButton(t, null);
    }

    private void aturStatusFisik(RegioFisik rf, boolean tidakNormal) {
        rf.txtKet.setEditable(tidakNormal);
        if (tidakNormal) {
            rf.txtKet.setBackground(Color.WHITE);
            rf.txtKet.setForeground(COL_TEXT_DARK);
            rf.txtKet.putClientProperty("JTextField.placeholderText", "Deskripsikan kelainan fisik...");
            rf.txtKet.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COL_ROSE_PRIMARY, 1),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)));
            if (rf.panelRow != null) {
                rf.panelRow.setBackground(COL_ROSE_SOFT);
                rf.panelRow.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COL_ROSE_BORDER, 1),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            }
        } else {
            rf.txtKet.setText("");
            rf.txtKet.setBackground(COL_CARD_HEADER);
            rf.txtKet.setForeground(COL_TEXT_MUTED);
            rf.txtKet.putClientProperty("JTextField.placeholderText", "Normal / Dalam batas normal");
            rf.txtKet.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COL_BORDER, 1),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)));
            if (rf.panelRow != null) {
                rf.panelRow.setBackground(COL_CARD_HEADER);
                rf.panelRow.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COL_BORDER, 1),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            }
        }
    }

    private static final class RegioFisik {
        final String kode;
        final String labelTeks;
        widget.RadioButton rbNormal;
        widget.RadioButton rbTidakNormal;
        ButtonGroup grup;
        widget.TextBox txtKet;
        JPanel panelRow;

        RegioFisik(String kode, String labelTeks) {
            this.kode = kode;
            this.labelTeks = labelTeks;
        }
    }

    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private DlgCariDokter dokter;
    private DlgCariPenyakit penyakit;
    private boolean pilihDpjp = false;
    private boolean untukDiagnosaBanding = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final int LEBAR_LABEL_KIRI = 232;

    // ---------- Kerangka Utama ----------
    private widget.InternalFrame internalFrame1, internalFrame2, internalFrame3;
    private JPanel topBarGelap;
    private widget.PanelBiasa barPasienMedis, railMedis;
    private widget.ScrollPane scrollRailMedis, scrollSeksiMedis;
    private widget.panelisi panelGlass8, panelGlass9;
    private javax.swing.JTabbedPane TabRawat;
    private widget.PanelBiasa FormInput;
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnPrint, BtnAll, BtnKeluar;
    private JPanel panelFooterInput, panelFooterData;

    // ---------- Identitas Pasien (Display Card & Hidden Fields) ----------
    private widget.TextBox TNoRw, TNoRM, TPasien, TglLahir, TUmur, Jk;
    private widget.Label lblKartuNama, lblKartuInisial, lblKartuInfo;
    private JLabel lblValRM, lblValRawat, lblValLokasi;

    // ---------- Header Inputs ----------
    private widget.Tanggal TglAsuhan;
    private widget.ComboBox CmbAnamnesis, CmbAsalMasuk, CmbCaraMasuk;
    private widget.TextBox THubungan;
    private widget.TextBox KdDokterPeriksa, NmDokterPeriksa, KdDokterDpjp, NmDokterDpjp;
    private widget.Button BtnDokterPeriksa, BtnDokterDpjp;

    // ---------- Panel Kiri (Side Rail Navigasi) ----------
    private widget.Label lblProgresPersen, lblProgresHitung, lblSumberDataBadge;
    private JProgressBar pbProgres;
    private final java.util.List<BarisNavigasiMedis> daftarBarisNavigasi = new java.util.ArrayList<>();
    private int seksiAktif = 0;

    private static class BarisNavigasiMedis {
        JPanel baris;
        JPanel aksen;
        JLabel lblNomor;
        JLabel lblJudul;
    }

    // ---------- Seksi I: Anamnesis ----------
    private widget.TextArea KeluhanUtama, RiwayatPenyakit, RiwayatPengobatan, RiwayatLain;
    private widget.TextBox AlergiObat, AlergiMakanan;
    private ButtonGroup grupStatusEkonomi;
    private widget.RadioButton rbEkonomiBaik, rbEkonomiSedang, rbEkonomiKurang;

    // ---------- Seksi II: Objektif (TTV & GCS) ----------
    private ButtonGroup grupKeadaan, grupKesadaran, grupStatusGizi;
    private widget.RadioButton rbKeadaanSehat, rbKeadaanRingan, rbKeadaanSedang, rbKeadaanBerat;
    private widget.RadioButton rbSadarCompos, rbSadarApatis, rbSadarSomnolen, rbSadarSopor, rbSadarKoma;
    private widget.TextBox GCS_E, GCS_M, GCS_V;
    private widget.Label lblTotalGCS;
    private widget.TextBox TDSistol, TDDiastol, Nadi, RR, Suhu, SPO2;
    private widget.TextBox TB, BB, IMT;
    private widget.RadioButton rbGiziKurang, rbGiziBaik, rbGiziLebih;

    // ---------- Seksi III: Penunjang ----------
    private widget.TextArea PemeriksaanPenunjang;

    // ---------- Seksi IV: Pemeriksaan Fisik 14 Organ ----------
    private final RegioFisik[] regios = {
        new RegioFisik("kepala", "a. Kepala"),
        new RegioFisik("mata", "b. Mata"),
        new RegioFisik("tht", "c. THT"),
        new RegioFisik("leher", "d. Leher"),
        new RegioFisik("mulut", "e. Mulut"),
        new RegioFisik("jantung", "f. Jantung dan Pemb. Darah"),
        new RegioFisik("thoraks", "g. Thoraks, Paru dan Payudara"),
        new RegioFisik("abdomen", "h. Abdomen"),
        new RegioFisik("kulit", "i. Kulit dan Sistem Limfatik"),
        new RegioFisik("tulang", "j. Tulang Belakang & Anggota Tubuh"),
        new RegioFisik("saraf", "k. Sistem Saraf"),
        new RegioFisik("genitalia", "l. Genitalia, Anus, Rektum"),
        new RegioFisik("lokasi", "m. Status Lokasi"),
        new RegioFisik("lain", "n. Lain-lain")
    };

    // ---------- Seksi V: Status Lokalis Kanvas Anatomi ----------
    private KanvasStatusLokalis kanvasLokalis;

    // ---------- Seksi VI: Asesmen (Diagnosis) ----------
    private widget.TextArea DiagnosaMedis, DiagnosaBanding;
    private widget.Button BtnCariDiagnosaMedis, BtnCariDiagnosaBanding;

    // ---------- Seksi VII: Program Terapi ----------
    private widget.TextArea AdvisTerapi, RencanaTindakan;

    // ---------- Panel Komponen Seksi untuk Navigasi Cepat ----------
    private JPanel pnlSeksi0, pnlSeksi1, pnlSeksi2, pnlSeksi3, pnlSeksi4, pnlSeksi5, pnlSeksi6, pnlSeksi7;
    private JPanel pnlDokterPeriksa, pnlDokterDpjp;

    // ---------- Tab Data Tersimpan & Preview HTML / Validasi TTE ----------
    private widget.ScrollPane Scroll, ScrollHTML;
    private widget.Table tbObat;
    private widget.Tanggal DTPCari1, DTPCari2;
    private widget.TextBox TCari;
    private widget.Button BtnCari, BtnPrint1;
    private widget.Label LCount;
    private widget.PanelBiasa PanelAccor;
    private widget.CekBox ChkAccor;
    private widget.editorpane LoadHTML;
    private JPanel panelKiriData, panelKananData;
    private widget.Button btnPopupPreview, btnValidasiTTE, btnGantiData, btnHapusData, btnCetakDokumen;
    private JCheckBox chkLangsungTTE;

    public RMAsesmenMedisRanap20_1(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        try {
            koneksi.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS asesmen_medis_ranap_rm20_1_konfirmasi ("
                + "no_rawat varchar(17) NOT NULL, "
                + "tgl_validasi datetime NOT NULL, "
                + "kd_dokter varchar(20) NOT NULL, "
                + "status_validasi varchar(30) NOT NULL DEFAULT 'Tervalidasi (TTE)', "
                + "PRIMARY KEY (no_rawat)) ENGINE=InnoDB DEFAULT CHARSET=latin1"
            );
        } catch (Exception e) {
            System.out.println("Tabel konfirmasi: " + e);
        }

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.RM", "Nama Pasien", "Tgl.Lahir", "J.K.",
            "Kode Dokter", "Dokter Pemeriksa", "DPJP", "Tanggal", "Sumber Data",
            "Diagnosa Medis", "Diagnosa Banding", "Status Validasi"
        }) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
        };

        rakitKerangka();
        inisialisasiKomponenHeader();
        rakitRailNavigasi();
        rakitFormSatuHalaman();
        rakitTabData();
        pasangBatasInput();

        TabRawat.addChangeListener(e -> {
            boolean isInput = (TabRawat.getSelectedIndex() == 0);
            if (panelFooterInput != null) panelFooterInput.setVisible(isInput);
            if (panelFooterData != null) panelFooterData.setVisible(!isInput);
            if (!isInput) {
                tampil();
            }
        });

        widget.IconStyle.pasang(this);

        if (ChkAccor != null) {
            ChkAccor.setSelected(false);
            isMenu();
        }
        setSize(1340, 800);
        hitungProgres();
    }

    // =================================================================
    //  KERANGKA UTAMA
    // =================================================================

    private void rakitKerangka() {
        internalFrame1 = new widget.InternalFrame();
        internalFrame1.setBorder(BorderFactory.createLineBorder(COL_BORDER));
        internalFrame1.setLayout(new BorderLayout(0, 0));

        // 1. Top Bar Gelap (Tinggi 38px)
        topBarGelap = new JPanel(new BorderLayout());
        topBarGelap.setPreferredSize(new Dimension(10, 38));
        topBarGelap.setBackground(COL_HEADER_DARK);
        topBarGelap.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JPanel pnlJudulTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlJudulTop.setOpaque(false);

        JLabel lblBadgeRM = new JLabel("RM", SwingConstants.CENTER);
        lblBadgeRM.setPreferredSize(new Dimension(26, 26));
        lblBadgeRM.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadgeRM.setForeground(new Color(204, 251, 241));
        lblBadgeRM.setOpaque(true);
        lblBadgeRM.setBackground(COL_TEAL_DARK);
        lblBadgeRM.setBorder(BorderFactory.createLineBorder(new Color(20, 184, 166, 100), 1));

        JLabel lblTopTitle = new JLabel("Asesmen Medis Rawat Inap");
        lblTopTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTopTitle.setForeground(Color.WHITE);

        JLabel lblTopBadge = new JLabel("RM 20-1");
        lblTopBadge.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblTopBadge.setForeground(new Color(204, 251, 241));
        lblTopBadge.setOpaque(true);
        lblTopBadge.setBackground(new Color(17, 94, 89));
        lblTopBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(19, 78, 74), 1),
                BorderFactory.createEmptyBorder(1, 6, 1, 6)));

        pnlJudulTop.add(lblBadgeRM);
        pnlJudulTop.add(lblTopTitle);
        pnlJudulTop.add(lblTopBadge);

        JLabel lblTopRs = new JLabel("RSUD Demang Sepulau Raya");
        lblTopRs.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTopRs.setForeground(new Color(204, 251, 241, 200));

        topBarGelap.add(pnlJudulTop, BorderLayout.WEST);
        topBarGelap.add(lblTopRs, BorderLayout.EAST);
        internalFrame1.add(topBarGelap, BorderLayout.NORTH);

        // 2. Footer Aksi Bawah (Mengikuti Standar Rekon: Tab-Aware Contextual Footer)
        panelGlass8 = new widget.panelisi();
        panelGlass8.setName("panelGlass8");
        panelGlass8.setPreferredSize(new Dimension(44, 48));
        panelGlass8.setBackground(Color.WHITE);
        panelGlass8.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, COL_BORDER),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        panelGlass8.setLayout(new BorderLayout(8, 0));

        // Sub-panel tombol saat Tab 1 (Formulir Pengkajian) Aktif
        panelFooterInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelFooterInput.setOpaque(false);

        BtnSimpan = new widget.Button();
        BtnSimpan.setIcon(widget.IconStyle.muat("save.svg", 15, Color.WHITE));
        BtnSimpan.setBackground(COL_TEAL_DARK); // #0D9488
        BtnSimpan.setForeground(Color.WHITE);
        BtnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        BtnSimpan.setText(" Simpan");
        BtnSimpan.setToolTipText("Alt+S (Simpan / Perbarui Asesmen Medis)");
        BtnSimpan.setPreferredSize(new Dimension(110, 32));
        BtnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        BtnSimpan.putClientProperty("Component.arc", 8);
        BtnSimpan.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COL_TEAL_DARK, 8, 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        BtnSimpan.addActionListener(evt -> simpan());
        panelFooterInput.add(BtnSimpan);

        BtnBatal = new widget.Button();
        BtnBatal.setIcon(widget.IconStyle.muat("refresh.svg", 15, new Color(71, 85, 105)));
        BtnBatal.setBackground(Color.WHITE);
        BtnBatal.setForeground(new Color(51, 65, 85));
        BtnBatal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        BtnBatal.setText(" Reset / Baru");
        BtnBatal.setToolTipText("Alt+B (Bersihkan form untuk pasien baru)");
        BtnBatal.setPreferredSize(new Dimension(115, 32));
        BtnBatal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        BtnBatal.putClientProperty("Component.arc", 8);
        BtnBatal.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COL_INPUT_BORDER, 8, 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        BtnBatal.addActionListener(evt -> emptTeks());
        panelFooterInput.add(BtnBatal);

        panelFooterInput.add(Box.createHorizontalStrut(8));
        chkLangsungTTE = new JCheckBox(" Langsung Validasi & Bubuhkan TTE setelah simpan");
        chkLangsungTTE.setOpaque(false);
        chkLangsungTTE.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkLangsungTTE.setForeground(new Color(51, 65, 85));
        chkLangsungTTE.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelFooterInput.add(chkLangsungTTE);

        // Sub-panel tombol saat Tab 2 (Data Riwayat Asesmen) Aktif
        panelFooterData = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelFooterData.setOpaque(false);
        panelFooterData.setVisible(false); // Sembunyi di awal, muncul saat Tab 2 dibuka

        btnValidasiTTE = new widget.Button();
        btnValidasiTTE.setText(" Validasi & Bubuhkan TTE");
        btnValidasiTTE.setIcon(widget.IconStyle.muat("check.svg", 15, Color.WHITE));
        btnValidasiTTE.setBackground(new Color(16, 185, 129)); // Emerald-500
        btnValidasiTTE.setForeground(Color.WHITE);
        btnValidasiTTE.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnValidasiTTE.setPreferredSize(new Dimension(195, 32));
        btnValidasiTTE.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnValidasiTTE.putClientProperty("Component.arc", 8);
        btnValidasiTTE.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(16, 185, 129), 8, 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        btnValidasiTTE.setEnabled(false);
        btnValidasiTTE.addActionListener(e -> validasiAtauTarikTTE());
        panelFooterData.add(btnValidasiTTE);

        btnGantiData = new widget.Button();
        btnGantiData.setText(" Ganti / Edit Data");
        btnGantiData.setIcon(widget.IconStyle.muat("pencil.svg", 15, new Color(71, 85, 105)));
        btnGantiData.setBackground(Color.WHITE);
        btnGantiData.setForeground(new Color(51, 65, 85));
        btnGantiData.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGantiData.setPreferredSize(new Dimension(135, 32));
        btnGantiData.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGantiData.putClientProperty("Component.arc", 8);
        btnGantiData.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COL_INPUT_BORDER, 8, 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        btnGantiData.setEnabled(false);
        btnGantiData.addActionListener(e -> gantiDataAsesmen());
        panelFooterData.add(btnGantiData);

        btnHapusData = new widget.Button();
        btnHapusData.setText(" Hapus");
        btnHapusData.setIcon(widget.IconStyle.muat("trash.svg", 15, new Color(225, 29, 72)));
        btnHapusData.setBackground(Color.WHITE);
        btnHapusData.setForeground(new Color(225, 29, 72));
        btnHapusData.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHapusData.setPreferredSize(new Dimension(95, 32));
        btnHapusData.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapusData.putClientProperty("Component.arc", 8);
        btnHapusData.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(254, 205, 211), 8, 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        btnHapusData.setEnabled(false);
        btnHapusData.addActionListener(e -> aksiHapus());
        panelFooterData.add(btnHapusData);

        btnCetakDokumen = new widget.Button();
        btnCetakDokumen.setText(" Cetak RM 20-1");
        btnCetakDokumen.setIcon(widget.IconStyle.muat("printer.svg", 15, Color.WHITE));
        btnCetakDokumen.setBackground(new Color(16, 185, 129)); // Emerald-500
        btnCetakDokumen.setForeground(Color.WHITE);
        btnCetakDokumen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCetakDokumen.setPreferredSize(new Dimension(145, 32));
        btnCetakDokumen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCetakDokumen.putClientProperty("Component.arc", 8);
        btnCetakDokumen.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(16, 185, 129), 8, 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        btnCetakDokumen.setEnabled(false);
        btnCetakDokumen.addActionListener(e -> cetakTerpilih());
        pasangMenuCetakOpsional(btnCetakDokumen);
        panelFooterData.add(btnCetakDokumen);

        // Alias penugasan kompatibilitas
        BtnEdit = btnGantiData;
        BtnHapus = btnHapusData;
        BtnPrint = btnCetakDokumen;

        JPanel leftFooterWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftFooterWrapper.setOpaque(false);
        leftFooterWrapper.add(panelFooterInput);
        leftFooterWrapper.add(panelFooterData);

        BtnKeluar = new widget.Button();
        BtnKeluar.setText(" Keluar");
        BtnKeluar.setIcon(widget.IconStyle.muat("log-out.svg", 15, new Color(225, 29, 72)));
        BtnKeluar.setBackground(Color.WHITE);
        BtnKeluar.setForeground(new Color(225, 29, 72));
        BtnKeluar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        BtnKeluar.setPreferredSize(new Dimension(95, 32));
        BtnKeluar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        BtnKeluar.putClientProperty("Component.arc", 8);
        BtnKeluar.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(254, 205, 211), 8, 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        BtnKeluar.addActionListener(evt -> dispose());

        panelGlass8.add(leftFooterWrapper, BorderLayout.WEST);
        panelGlass8.add(BtnKeluar, BorderLayout.EAST);

        internalFrame1.add(panelGlass8, BorderLayout.PAGE_END);

        // 3. Tab Container
        TabRawat = new javax.swing.JTabbedPane();
        TabRawat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        TabRawat.setBackground(Color.WHITE);
        TabRawat.putClientProperty("JTabbedPane.tabType", "underlined");
        TabRawat.putClientProperty("JTabbedPane.showContentSeparator", Boolean.TRUE);
        TabRawat.putClientProperty("JTabbedPane.tabHeight", 36);
        TabRawat.setBorder(null);
        TabRawat.setForeground(COL_TEXT_MUTED);

        internalFrame2 = new widget.InternalFrame();
        internalFrame2.setBorder(null);
        internalFrame2.setBackground(COL_BG_MAIN);
        internalFrame2.setLayout(new BorderLayout());

        TabRawat.addTab("Formulir Pengkajian", internalFrame2);
        internalFrame1.add(TabRawat, BorderLayout.CENTER);
        getContentPane().add(internalFrame1, BorderLayout.CENTER);
    }

    // =================================================================
    //  HEADER & IDENTITAS PASIEN (SEKARANG DI RAIL & SUB-BAB PERTAMA)
    // =================================================================

    private void inisialisasiKomponenHeader() {
        TNoRw = new widget.TextBox();
        TNoRw.setVisible(false);
        TNoRM = new widget.TextBox();
        TNoRM.setVisible(false);
        TPasien = new widget.TextBox();
        TPasien.setVisible(false);
        TglLahir = new widget.TextBox();
        TglLahir.setVisible(false);
        TUmur = new widget.TextBox();
        TUmur.setVisible(false);
        Jk = new widget.TextBox();
        Jk.setVisible(false);
        KdDokterPeriksa = new widget.TextBox();
        KdDokterPeriksa.setVisible(false);
        KdDokterDpjp = new widget.TextBox();
        KdDokterDpjp.setVisible(false);

        // Input Field Waktu & Cara Masuk
        TglAsuhan = new widget.Tanggal();
        TglAsuhan.setDisplayFormat("dd-MM-yyyy HH:mm");
        TglAsuhan.setDate(new Date());
        TglAsuhan.setPreferredSize(new Dimension(145, 30));
        TglAsuhan.setBackground(Color.WHITE);
        gayaKotakInput(TglAsuhan);

        CmbAnamnesis = new widget.ComboBox();
        CmbAnamnesis.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Autoanamnesis", "Aloanamnesis"}));
        CmbAnamnesis.setPreferredSize(new Dimension(135, 30));
        CmbAnamnesis.setBackground(Color.WHITE);
        gayaKotakInput(CmbAnamnesis);

        THubungan = new widget.TextBox();
        THubungan.setPreferredSize(new Dimension(160, 30));
        THubungan.putClientProperty("JTextField.placeholderText", "Hubungan dgn pasien...");
        THubungan.setEnabled(false);
        gayaKotakInput(THubungan);

        CmbAnamnesis.addActionListener(evt -> {
            boolean isAllo = "Aloanamnesis".equalsIgnoreCase(CmbAnamnesis.getSelectedItem().toString());
            THubungan.setEnabled(isAllo);
            if (!isAllo) {
                THubungan.setText("");
            }
        });

        CmbAsalMasuk = new widget.ComboBox();
        CmbAsalMasuk.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"IGD", "Poli Rawat Jalan"}));
        CmbAsalMasuk.setPreferredSize(new Dimension(135, 30));
        CmbAsalMasuk.setBackground(Color.WHITE);
        gayaKotakInput(CmbAsalMasuk);

        CmbCaraMasuk = new widget.ComboBox();
        CmbCaraMasuk.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Berjalan", "Kursi Roda", "Brankar"}));
        CmbCaraMasuk.setSelectedItem("Brankar");
        CmbCaraMasuk.setPreferredSize(new Dimension(135, 30));
        CmbCaraMasuk.setBackground(Color.WHITE);
        gayaKotakInput(CmbCaraMasuk);

        // Dokter Pemeriksa
        NmDokterPeriksa = new widget.TextBox();
        NmDokterPeriksa.setEditable(false);
        NmDokterPeriksa.putClientProperty("JTextField.placeholderText", "Pilih dokter pemeriksa...");

        BtnDokterPeriksa = new widget.Button();
        BtnDokterPeriksa.setIcon(new ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnDokterPeriksa.setText("");
        BtnDokterPeriksa.setToolTipText("Cari Dokter Pemeriksa");
        BtnDokterPeriksa.addActionListener(evt -> bukaCariDokter(false));

        pnlDokterPeriksa = buatInputDenganLookup(NmDokterPeriksa, BtnDokterPeriksa);

        // Dokter DPJP
        NmDokterDpjp = new widget.TextBox();
        NmDokterDpjp.setEditable(false);
        NmDokterDpjp.putClientProperty("JTextField.placeholderText", "Pilih DPJP...");

        BtnDokterDpjp = new widget.Button();
        BtnDokterDpjp.setIcon(new ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnDokterDpjp.setText("");
        BtnDokterDpjp.setToolTipText("Cari DPJP");
        BtnDokterDpjp.addActionListener(evt -> bukaCariDokter(true));

        pnlDokterDpjp = buatInputDenganLookup(NmDokterDpjp, BtnDokterDpjp);

        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sinkronKartuIdentitas(); }
            @Override public void removeUpdate(DocumentEvent e) { sinkronKartuIdentitas(); }
            @Override public void changedUpdate(DocumentEvent e) { sinkronKartuIdentitas(); }
        };
        TNoRw.getDocument().addDocumentListener(dl);
        TNoRM.getDocument().addDocumentListener(dl);
        TPasien.getDocument().addDocumentListener(dl);
        Jk.getDocument().addDocumentListener(dl);
        TglLahir.getDocument().addDocumentListener(dl);
    }

    private JPanel buatKartuIdentitasPasien() {
        final int radius = 14;
        JPanel kartuId = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth() - 1;
                int h = getHeight() - 1;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, radius, radius);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_BORDER);
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, radius, radius));
                g2.dispose();
            }
        };
        kartuId.setOpaque(false);

        JPanel patHeader = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                java.awt.geom.RoundRectangle2D.Float roundTop = new java.awt.geom.RoundRectangle2D.Float(
                        0.6f, 0.6f, w - 1.2f, h + radius, radius, radius);
                g2.clip(new java.awt.Rectangle(0, 0, w, h));
                g2.setColor(COL_CARD_HEADER);
                g2.fill(roundTop);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_BORDER_DIVIDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine(0, h - 1, w, h - 1);
                g2.dispose();
            }
        };
        patHeader.setOpaque(false);
        patHeader.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel lblIdHeader = new JLabel("IDENTITAS PASIEN");
        lblIdHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblIdHeader.setForeground(COL_TEXT_MUTED);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightHeader.setOpaque(false);

        JLabel btnPilihPasien = new JLabel("Pilih Pasien", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Color.WHITE);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 6, 6));
                g2.setColor(COL_INPUT_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnPilihPasien.setPreferredSize(new Dimension(80, 22));
        btnPilihPasien.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnPilihPasien.setForeground(COL_TEAL_DARK);
        btnPilihPasien.setOpaque(false);
        btnPilihPasien.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPilihPasien.setToolTipText("Klik untuk memilih atau mengganti pasien rawat inap");
        btnPilihPasien.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                bukaPilihPasienRanap();
            }
        });

        lblSumberDataBadge = new widget.Label() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblSumberDataBadge.setText("Baru");
        lblSumberDataBadge.setToolTipText("Asesmen Medis Baru");
        lblSumberDataBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSumberDataBadge.setForeground(new Color(4, 120, 87));
        lblSumberDataBadge.setOpaque(false);
        lblSumberDataBadge.setBackground(new Color(209, 250, 229));
        lblSumberDataBadge.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(167, 243, 208), 8, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        rightHeader.add(btnPilihPasien);
        rightHeader.add(lblSumberDataBadge);

        patHeader.add(lblIdHeader, BorderLayout.WEST);
        patHeader.add(rightHeader, BorderLayout.EAST);

        JPanel patBody = new JPanel();
        patBody.setLayout(new BoxLayout(patBody, BoxLayout.Y_AXIS));
        patBody.setOpaque(false);
        patBody.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel avatarRow = new JPanel(new BorderLayout(10, 0));
        avatarRow.setOpaque(false);
        avatarRow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatarRow.setToolTipText("Klik untuk memilih atau mengganti pasien rawat inap");
        avatarRow.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                bukaPilihPasienRanap();
            }
        });

        lblKartuInisial = new widget.Label() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_TEAL_SOFT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblKartuInisial.setText("P");
        lblKartuInisial.setHorizontalAlignment(SwingConstants.CENTER);
        lblKartuInisial.setPreferredSize(new Dimension(44, 44));
        lblKartuInisial.setMinimumSize(new Dimension(44, 44));
        lblKartuInisial.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKartuInisial.setForeground(COL_TEAL_DARK);
        lblKartuInisial.setOpaque(false);
        lblKartuInisial.setBorder(new RoundedBorder(COL_TEAL_BORDER, 10, 1));

        JPanel nameBox = new JPanel();
        nameBox.setOpaque(false);
        nameBox.setLayout(new BoxLayout(nameBox, BoxLayout.Y_AXIS));

        lblKartuNama = new widget.Label();
        lblKartuNama.setText("-");
        lblKartuNama.setHorizontalAlignment(SwingConstants.LEFT);
        lblKartuNama.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblKartuNama.setForeground(COL_TEXT_DARK);

        lblKartuInfo = new widget.Label();
        lblKartuInfo.setText("-");
        lblKartuInfo.setHorizontalAlignment(SwingConstants.LEFT);
        lblKartuInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblKartuInfo.setForeground(COL_TEXT_MUTED);

        nameBox.add(lblKartuNama);
        nameBox.add(Box.createVerticalStrut(2));
        nameBox.add(lblKartuInfo);

        avatarRow.add(lblKartuInisial, BorderLayout.WEST);
        avatarRow.add(nameBox, BorderLayout.CENTER);

        JPanel pnlNomorDetail = new JPanel(new GridLayout(3, 1, 0, 5));
        pnlNomorDetail.setOpaque(false);
        pnlNomorDetail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COL_BORDER),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)));

        lblValRM = new JLabel("-");
        lblValRawat = new JLabel("-");
        lblValLokasi = new JLabel("-");

        pnlNomorDetail.add(buatBarisNomorPanel("No. RM", lblValRM));
        pnlNomorDetail.add(buatBarisNomorPanel("No. Rawat", lblValRawat));
        pnlNomorDetail.add(buatBarisNomorPanel("Kamar", lblValLokasi));

        patBody.add(avatarRow);
        patBody.add(Box.createVerticalStrut(10));
        patBody.add(pnlNomorDetail);

        kartuId.add(patHeader, BorderLayout.NORTH);
        kartuId.add(patBody, BorderLayout.CENTER);
        return kartuId;
    }

    /**
     * Dialog interaktif untuk memilih pasien rawat inap yang sedang aktif dirawat di kamar_inap.
     */
    private void bukaPilihPasienRanap() {
        JDialog dlg = new JDialog(this, "Pilih Pasien Rawat Inap Aktif", true);
        dlg.setSize(860, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnlHeader = new JPanel(new BorderLayout(10, 0));
        pnlHeader.setBackground(COL_CARD_HEADER);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel lblTitle = new JLabel("DAFTAR PASIEN RAWAT INAP AKTIF");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(COL_TEXT_DARK);

        JPanel pnlCari = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlCari.setOpaque(false);
        JLabel lblCari = new JLabel("Cari :");
        lblCari.setFont(FONT_LABEL);
        widget.TextBox txtCari = new widget.TextBox();
        txtCari.setPreferredSize(new Dimension(240, 28));
        txtCari.putClientProperty("JTextField.placeholderText", "Nama / No.RM / No.Rawat / Kamar...");
        gayaKotakInput(txtCari);

        pnlCari.add(lblCari);
        pnlCari.add(txtCari);
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(pnlCari, BorderLayout.EAST);
        dlg.add(pnlHeader, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(null, new Object[]{
            "No. Rawat", "No. RM", "Nama Pasien", "J.K.", "Kamar / Bed", "Bangsal", "Tgl. Masuk", "Dokter DPJP"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        widget.Table tb = new widget.Table();
        tb.setModel(model);
        tb.setPreferredScrollableViewportSize(new Dimension(820, 320));

        widget.ScrollPane scroll = new widget.ScrollPane();
        scroll.setViewportView(tb);
        dlg.add(scroll, BorderLayout.CENTER);

        Runnable muat = () -> {
            Valid.tabelKosong(model);
            String q = txtCari.getText().trim();
            String sql = "select kamar_inap.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, "
                    + "kamar.kd_kamar, bangsal.nm_bangsal, kamar_inap.tgl_masuk, dokter.nm_dokter "
                    + "from kamar_inap "
                    + "inner join reg_periksa on kamar_inap.no_rawat=reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "
                    + "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "
                    + "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                    + "where kamar_inap.stts_pulang='-' "
                    + (q.isEmpty() ? "" : "and (kamar_inap.no_rawat like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or bangsal.nm_bangsal like ? or kamar.kd_kamar like ?) ")
                    + "order by kamar_inap.tgl_masuk desc limit 100";
            try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                if (!q.isEmpty()) {
                    String param = "%" + q + "%";
                    for (int i = 1; i <= 5; i++) ps.setString(i, param);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    boolean ada = false;
                    while (rs.next()) {
                        ada = true;
                        model.addRow(new Object[]{
                            rs.getString("no_rawat"),
                            rs.getString("no_rkm_medis"),
                            rs.getString("nm_pasien"),
                            "L".equalsIgnoreCase(rs.getString("jk")) ? "L" : "P",
                            rs.getString("kd_kamar"),
                            rs.getString("nm_bangsal"),
                            rs.getString("tgl_masuk"),
                            rs.getString("nm_dokter")
                        });
                    }
                    if (!ada && q.isEmpty()) {
                        String sql2 = "select kamar_inap.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, "
                                + "kamar.kd_kamar, bangsal.nm_bangsal, kamar_inap.tgl_masuk, dokter.nm_dokter "
                                + "from kamar_inap "
                                + "inner join reg_periksa on kamar_inap.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "
                                + "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "
                                + "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                                + "order by kamar_inap.tgl_masuk desc limit 50";
                        try (PreparedStatement ps2 = koneksi.prepareStatement(sql2);
                             ResultSet rs2 = ps2.executeQuery()) {
                            while (rs2.next()) {
                                model.addRow(new Object[]{
                                    rs2.getString("no_rawat"),
                                    rs2.getString("no_rkm_medis"),
                                    rs2.getString("nm_pasien"),
                                    "L".equalsIgnoreCase(rs2.getString("jk")) ? "L" : "P",
                                    rs2.getString("kd_kamar"),
                                    rs2.getString("nm_bangsal"),
                                    rs2.getString("tgl_masuk"),
                                    rs2.getString("nm_dokter")
                                });
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error muat pasien ranap: " + ex);
            }
        };

        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { muat.run(); }
            @Override public void removeUpdate(DocumentEvent e) { muat.run(); }
            @Override public void changedUpdate(DocumentEvent e) { muat.run(); }
        });

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COL_BORDER));

        widget.Button btnBatal = createFlatButton("Tutup", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnBatal.addActionListener(e -> dlg.dispose());

        widget.Button btnPilih = createFlatButton("Pilih Pasien", COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnPilih.addActionListener(e -> {
            int row = tb.getSelectedRow();
            if (row != -1) {
                String norwt = tb.getValueAt(row, 0).toString();
                setNoRm(norwt, new Date());
                dlg.dispose();
            } else {
                JOptionPane.showMessageDialog(dlg, "Silahkan klik salah satu baris pasien terlebih dahulu!");
            }
        });

        tb.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tb.getSelectedRow() != -1) {
                    btnPilih.doClick();
                }
            }
        });

        pnlFooter.add(btnBatal);
        pnlFooter.add(btnPilih);
        dlg.add(pnlFooter, BorderLayout.SOUTH);

        muat.run();
        dlg.setVisible(true);
    }

    // =================================================================
    //  PANEL KIRI (SIDE RAIL NAVIGASI)
    // =================================================================

    private void rakitRailNavigasi() {
        railMedis = new widget.PanelBiasa();
        railMedis.setBackground(COL_BG_MAIN);
        railMedis.setLayout(new GridBagLayout());
        railMedis.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        scrollRailMedis = new widget.ScrollPane();
        scrollRailMedis.setViewportView(railMedis);
        scrollRailMedis.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COL_BORDER));
        scrollRailMedis.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollRailMedis.setPreferredSize(new Dimension(340, 10));
        scrollRailMedis.setMinimumSize(new Dimension(320, 10));
        scrollRailMedis.getVerticalScrollBar().setUnitIncrement(16);
        scrollRailMedis.getViewport().setBackground(COL_BG_MAIN);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;

        // 0. KARTU IDENTITAS PASIEN (TERPASANG DI SIDE RAIL)
        c.gridy = 0;
        c.insets = new Insets(0, 0, 12, 0);
        railMedis.add(buatKartuIdentitasPasien(), c);

        // 1. Kartu KELENGKAPAN PENGISIAN
        c.gridy = 1;
        railMedis.add(buatKartuIsianWajib(), c);

        // 2. Kartu NAVIGASI LOMPAT CEPAT
        c.gridy = 2;
        railMedis.add(buatKartuNavigasiLompat(), c);

        // 3. Kartu RUJUKAN DATA MEDIS
        c.gridy = 3;
        railMedis.add(buatKartuRujukanData(), c);

        // Glue Vertikal
        c.gridy = 4;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        railMedis.add(Box.createGlue(), c);

        internalFrame2.add(scrollRailMedis, BorderLayout.WEST);
    }

    private JPanel buatKartuIsianWajib() {
        final int radius = 12;
        JPanel kartu = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Color.WHITE);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, radius, radius));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_BORDER);
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, radius, radius));
                g2.dispose();
            }
        };
        kartu.setOpaque(false);
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel progTop = new JPanel(new BorderLayout());
        progTop.setOpaque(false);
        JLabel lblProgTitle = new JLabel("KELENGKAPAN PENGISIAN");
        lblProgTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblProgTitle.setForeground(COL_TEXT_MUTED);

        lblProgresPersen = new widget.Label();
        lblProgresPersen.setText("0%");
        lblProgresPersen.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblProgresPersen.setForeground(COL_TEAL_DARK);
        progTop.add(lblProgTitle, BorderLayout.WEST);
        progTop.add(lblProgresPersen, BorderLayout.EAST);

        pbProgres = new JProgressBar(0, 100);
        pbProgres.setValue(0);
        pbProgres.setForeground(COL_TEAL_PRIMARY);
        pbProgres.setBackground(COL_BG_MAIN);
        pbProgres.setBorderPainted(false);
        pbProgres.setPreferredSize(new Dimension(240, 8));

        JPanel progBottom = new JPanel(new BorderLayout());
        progBottom.setOpaque(false);
        JLabel lblWajib = new JLabel("Wajib lengkap");
        lblWajib.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblWajib.setForeground(COL_TEAL_DARK);

        lblProgresHitung = new widget.Label();
        lblProgresHitung.setText("0/20 bagian terisi");
        lblProgresHitung.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblProgresHitung.setForeground(COL_TEXT_MUTED);

        progBottom.add(lblWajib, BorderLayout.WEST);
        progBottom.add(lblProgresHitung, BorderLayout.EAST);

        kartu.add(progTop);
        kartu.add(Box.createVerticalStrut(8));
        kartu.add(pbProgres);
        kartu.add(Box.createVerticalStrut(6));
        kartu.add(progBottom);

        return kartu;
    }

    private JPanel buatKartuNavigasiLompat() {
        final int radius = 12;
        JPanel kartu = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Color.WHITE);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, radius, radius));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_BORDER);
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, radius, radius));
                g2.dispose();
            }
        };
        kartu.setOpaque(false);
        kartu.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));

        JLabel lblJudul = new JLabel("BAGIAN PENGKAJIAN");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblJudul.setForeground(COL_TEXT_MUTED);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 8, 8, 8);
        kartu.add(lblJudul, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        String[] menuSeksi = {
            "Pengkajian Awal",
            "Anamnesis",
            "Pemeriksaan Umum",
            "Pemeriksaan Penunjang",
            "Pemeriksaan Fisik",
            "Status Lokalis",
            "Diagnosis",
            "Program & Perencanaan"
        };

        daftarBarisNavigasi.clear();
        for (int idx = 0; idx < menuSeksi.length; idx++) {
            c.gridy = idx + 1;
            c.insets = new Insets(1, 0, 1, 0);
            final int targetIdx = idx;

            final BarisNavigasiMedis row = new BarisNavigasiMedis();

            row.baris = new JPanel(new BorderLayout(8, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (targetIdx == seksiAktif) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(COL_TEAL_SOFT);
                        g2.fillRoundRect(2, 0, getWidth() - 4, getHeight(), 8, 8);
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            row.baris.setOpaque(false);
            row.baris.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 8));
            row.baris.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            row.aksen = new JPanel();
            row.aksen.setPreferredSize(new Dimension(4, 20));
            row.aksen.setBackground(idx == 0 ? COL_TEAL_PRIMARY : Color.WHITE);

            row.lblNomor = new JLabel(String.format("%02d", idx + 1));
            row.lblNomor.setFont(new Font("Monospaced", Font.BOLD, 11));
            row.lblNomor.setForeground(idx == 0 ? COL_TEAL_DARK : COL_TEXT_MUTED);
            row.lblNomor.setPreferredSize(new Dimension(24, 20));

            row.lblJudul = new JLabel(menuSeksi[idx]);
            row.lblJudul.setFont(new Font("Segoe UI", idx == 0 ? Font.BOLD : Font.PLAIN, 11));
            row.lblJudul.setForeground(idx == 0 ? COL_TEAL_DARK : COL_TEXT_DARK);

            JPanel pnlTeks = new JPanel(new BorderLayout(6, 0));
            pnlTeks.setOpaque(false);
            pnlTeks.add(row.lblNomor, BorderLayout.WEST);
            pnlTeks.add(row.lblJudul, BorderLayout.CENTER);

            row.baris.add(row.aksen, BorderLayout.WEST);
            row.baris.add(pnlTeks, BorderLayout.CENTER);

            row.baris.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    lompatKeSeksi(targetIdx);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (targetIdx != seksiAktif) {
                        row.baris.setBackground(COL_CARD_HEADER);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (targetIdx != seksiAktif) {
                        row.baris.setBackground(Color.WHITE);
                    }
                }
            });

            daftarBarisNavigasi.add(row);
            kartu.add(row.baris, c);
        }

        return kartu;
    }

    private void setSorotanNavigasi(int aktifIdx) {
        seksiAktif = aktifIdx;
        for (int i = 0; i < daftarBarisNavigasi.size(); i++) {
            BarisNavigasiMedis row = daftarBarisNavigasi.get(i);
            boolean aktif = (i == aktifIdx);
            row.baris.setBackground(aktif ? COL_TEAL_SOFT : Color.WHITE);
            row.aksen.setBackground(aktif ? COL_TEAL_PRIMARY : Color.WHITE);
            row.lblNomor.setForeground(aktif ? COL_TEAL_DARK : COL_TEXT_MUTED);
            row.lblJudul.setForeground(aktif ? COL_TEAL_DARK : COL_TEXT_DARK);
            row.lblJudul.setFont(new Font("Segoe UI", aktif ? Font.BOLD : Font.PLAIN, 11));
            row.baris.repaint();
        }
    }

    private JPanel buatKartuRujukanData() {
        final int radius = 12;
        JPanel kartu = new JPanel(new GridLayout(3, 1, 0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Color.WHITE);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, radius, radius));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_BORDER);
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, radius, radius));
                g2.dispose();
            }
        };
        kartu.setOpaque(false);
        kartu.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel lblJudul = new JLabel("RUJUKAN RIWAYAT MEDIS");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblJudul.setForeground(COL_TEXT_MUTED);

        widget.Button btnRekon = createFlatButton("Cek Rekonsiliasi Obat", COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnRekon.setHorizontalAlignment(SwingConstants.LEFT);
        btnRekon.addActionListener(e -> pratinjauRekonsiliasiObat());

        widget.Button btnCPPT = createFlatButton("Riwayat Perawatan Pasien", COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnCPPT.setHorizontalAlignment(SwingConstants.LEFT);
        btnCPPT.addActionListener(e -> {
            if (TNoRM.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Pilih data pasien terlebih dahulu!");
                return;
            }
            RMRiwayatPerawatan rwt = new RMRiwayatPerawatan(null, false);
            rwt.setNoRm(TNoRM.getText(), TPasien.getText());
            rwt.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            rwt.setLocationRelativeTo(internalFrame1);
            rwt.setVisible(true);
        });

        kartu.add(lblJudul);
        kartu.add(btnRekon);
        kartu.add(btnCPPT);
        return kartu;
    }

    private void lompatKeSeksi(int idx) {
        setSorotanNavigasi(idx);
        JPanel target = null;
        switch (idx) {
            case 0: target = pnlSeksi0; break;
            case 1: target = pnlSeksi1; break;
            case 2: target = pnlSeksi2; break;
            case 3: target = pnlSeksi3; break;
            case 4: target = pnlSeksi4; break;
            case 5: target = pnlSeksi5; break;
            case 6: target = pnlSeksi6; break;
            case 7: target = pnlSeksi7; break;
        }
        if (target != null && scrollSeksiMedis != null) {
            scrollSeksiMedis.getVerticalScrollBar().setValue(Math.max(0, target.getY() - 8));
        }
    }

    // =================================================================
    //  FORM TENGAH (SATU HALAMAN VERTIKAL SCROLL PENUH)
    // =================================================================

    private void rakitFormSatuHalaman() {
        FormInput = new widget.PanelBiasa();
        FormInput.setBackground(COL_BG_MAIN);
        FormInput.setLayout(new GridBagLayout());
        FormInput.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        scrollSeksiMedis = new widget.ScrollPane();
        scrollSeksiMedis.setViewportView(FormInput);
        scrollSeksiMedis.setBorder(null);
        scrollSeksiMedis.getVerticalScrollBar().setUnitIncrement(20);
        scrollSeksiMedis.getViewport().setBackground(COL_BG_MAIN);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;

        // Sub-Bab 01: Pengkajian Awal (Waktu Asuhan & Tim Dokter)
        pnlSeksi0 = rakitSeksi0MasukDanDokter();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 16, 0);
        FormInput.add(pnlSeksi0, c);

        // Seksi 02: Anamnesis
        pnlSeksi1 = rakitSeksi1Anamnesis();
        c.gridy = 1;
        FormInput.add(pnlSeksi1, c);

        // Seksi 03: Objektif (Pemeriksaan Umum & TTV)
        pnlSeksi2 = rakitSeksi2Objektif();
        c.gridy = 2;
        FormInput.add(pnlSeksi2, c);

        // Seksi 04: Penunjang Pra-Ranap
        pnlSeksi3 = rakitSeksi3Penunjang();
        c.gridy = 3;
        FormInput.add(pnlSeksi3, c);

        // Seksi 05: Pemeriksaan Fisik Sistematis 14 Organ (Head-to-Toe)
        pnlSeksi4 = rakitSeksi4Fisik();
        c.gridy = 4;
        FormInput.add(pnlSeksi4, c);

        // Seksi 06: Status Lokalis (Kanvas Anatomi Tubuh)
        pnlSeksi5 = rakitSeksi5Kanvas();
        c.gridy = 5;
        FormInput.add(pnlSeksi5, c);

        // Seksi 07: Asesmen (Diagnosis)
        pnlSeksi6 = rakitSeksi6Asesmen();
        c.gridy = 6;
        FormInput.add(pnlSeksi6, c);

        // Seksi 08: Program & Perencanaan
        pnlSeksi7 = rakitSeksi7Program();
        c.gridy = 7;
        FormInput.add(pnlSeksi7, c);

        // Glue Vertikal
        c.gridy = 8;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        FormInput.add(Box.createGlue(), c);

        internalFrame2.add(scrollSeksiMedis, BorderLayout.CENTER);

        scrollSeksiMedis.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (scrollSeksiMedis == null || scrollSeksiMedis.getViewport() == null) return;
            int viewY = scrollSeksiMedis.getViewport().getViewPosition().y + 80;
            JPanel[] seksiList = {pnlSeksi0, pnlSeksi1, pnlSeksi2, pnlSeksi3, pnlSeksi4, pnlSeksi5, pnlSeksi6, pnlSeksi7};
            int aktif = 0;
            for (int i = seksiList.length - 1; i >= 0; i--) {
                if (seksiList[i] != null && viewY >= seksiList[i].getY()) {
                    aktif = i;
                    break;
                }
            }
            if (aktif != seksiAktif) {
                setSorotanNavigasi(aktif);
            }
        });
    }

    // ---------- SUB-BAB 01: PENGKAJIAN AWAL (WAKTU ASUHAN & TIM MEDIS PENGKAJI) ----------
    private JPanel rakitSeksi0MasukDanDokter() {
        JPanel kartu = buatKotakSeksiCard("01", "PENGKAJIAN AWAL",
                "Waktu Asuhan, Tim Dokter, dan Asal Masuk Pasien");
        JPanel isi = new JPanel(new GridLayout(2, 1, 0, 12));
        isi.setOpaque(false);
        isi.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // Baris 1: 3 Kolom Sejajar (Tanggal Asuhan, Asal Masuk, Cara ke Ruangan)
        JPanel row1 = new JPanel(new GridLayout(1, 3, 14, 0));
        row1.setOpaque(false);
        row1.add(wrapFieldWithLabel("TANGGAL & JAM ASUHAN", TglAsuhan));
        row1.add(wrapFieldWithLabel("ASAL MASUK", CmbAsalMasuk));
        row1.add(wrapFieldWithLabel("CARA KE RUANGAN", CmbCaraMasuk));

        // Baris 2: 2 Kolom Sejajar (Dokter Pemeriksa / Pengkaji, Dokter DPJP)
        JPanel row2 = new JPanel(new GridLayout(1, 2, 14, 0));
        row2.setOpaque(false);
        row2.add(wrapFieldWithLabel("DOKTER PEMERIKSA / PENGKAJI", pnlDokterPeriksa));
        row2.add(wrapFieldWithLabel("DOKTER PENANGGUNG JAWAB (DPJP)", pnlDokterDpjp));

        isi.add(row1);
        isi.add(row2);
        kartu.add(isi, BorderLayout.CENTER);
        return kartu;
    }

    // ---------- SEKSI 02: ANAMNESIS ----------
    private JPanel rakitSeksi1Anamnesis() {
        JPanel kartu = buatKotakSeksiCard("02", "ANAMNESIS", "Keluhan Utama, Riwayat Penyakit, Alergi dan Status Ekonomi");
        JPanel isi = new JPanel();
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));
        isi.setOpaque(false);
        isi.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // a. Keluhan Utama (Full Width)
        KeluhanUtama = buatTextArea(2);
        isi.add(wrapFieldWithLabel("a. Keluhan Utama *", buatScrollField(KeluhanUtama, 60)));
        isi.add(Box.createVerticalStrut(12));

        // b. Riwayat Penyakit & c. Riwayat Pengobatan (2 Kolom Berdampingan)
        RiwayatPenyakit = buatTextArea(2);
        RiwayatPengobatan = buatTextArea(2);
        JPanel rowHistory = new JPanel(new GridLayout(1, 2, 16, 0));
        rowHistory.setOpaque(false);
        rowHistory.add(wrapFieldWithLabel("b. Riwayat Penyakit (Dahulu, Sekarang, Keluarga)", buatScrollField(RiwayatPenyakit, 60)));
        rowHistory.add(wrapFieldWithLabel("c. Riwayat Pengobatan Sebelumnya", buatScrollField(RiwayatPengobatan, 60)));
        isi.add(rowHistory);
        isi.add(Box.createVerticalStrut(12));

        // d. Riwayat Alergi (2 Box Beraksen Rose & Amber)
        AlergiObat = new widget.TextBox();
        AlergiObat.setPreferredSize(new Dimension(100, 28));
        AlergiObat.putClientProperty("JTextField.placeholderText", "Nama obat & reaksi bila ada...");
        gayaKotakInput(AlergiObat);

        AlergiMakanan = new widget.TextBox();
        AlergiMakanan.setPreferredSize(new Dimension(100, 28));
        AlergiMakanan.putClientProperty("JTextField.placeholderText", "Nama makanan & reaksi bila ada...");
        gayaKotakInput(AlergiMakanan);

        JPanel rowAlergi = new JPanel(new GridLayout(1, 2, 16, 0));
        rowAlergi.setOpaque(false);

        JPanel boxAlergiObat = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_ROSE_SOFT);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_ROSE_BORDER);
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, 8, 8));
                g2.dispose();
            }
        };
        boxAlergiObat.setOpaque(false);
        boxAlergiObat.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel lblAlergiObat = new JLabel("d. ALERGI OBAT (BILA ADA)");
        lblAlergiObat.setFont(FONT_LABEL);
        lblAlergiObat.setForeground(COL_ROSE_PRIMARY);
        boxAlergiObat.add(lblAlergiObat, BorderLayout.NORTH);
        boxAlergiObat.add(AlergiObat, BorderLayout.CENTER);

        JPanel boxAlergiMakanan = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_AMBER_SOFT);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_AMBER_BORDER);
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, 8, 8));
                g2.dispose();
            }
        };
        boxAlergiMakanan.setOpaque(false);
        boxAlergiMakanan.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel lblAlergiMakanan = new JLabel("ALERGI MAKANAN / LAINNYA (BILA ADA)");
        lblAlergiMakanan.setFont(FONT_LABEL);
        lblAlergiMakanan.setForeground(new Color(180, 83, 9));
        boxAlergiMakanan.add(lblAlergiMakanan, BorderLayout.NORTH);
        boxAlergiMakanan.add(AlergiMakanan, BorderLayout.CENTER);

        rowAlergi.add(boxAlergiObat);
        rowAlergi.add(boxAlergiMakanan);
        isi.add(rowAlergi);
        isi.add(Box.createVerticalStrut(12));

        // e. Riwayat Lain & f. Status Ekonomi (2 Kolom)
        RiwayatLain = buatTextArea(2);
        grupStatusEkonomi = new ButtonGroup();
        rbEkonomiBaik = new widget.RadioButton(); rbEkonomiBaik.setText("Baik");
        rbEkonomiSedang = new widget.RadioButton(); rbEkonomiSedang.setText("Sedang"); rbEkonomiSedang.setSelected(true);
        rbEkonomiKurang = new widget.RadioButton(); rbEkonomiKurang.setText("Kurang");
        grupStatusEkonomi.add(rbEkonomiBaik);
        grupStatusEkonomi.add(rbEkonomiSedang);
        grupStatusEkonomi.add(rbEkonomiKurang);

        JPanel rowSocial = new JPanel(new GridLayout(1, 2, 16, 0));
        rowSocial.setOpaque(false);
        rowSocial.add(wrapFieldWithLabel("e. Riwayat Lain (Sosial, Imunisasi, Pernikahan)", buatScrollField(RiwayatLain, 48)));
        rowSocial.add(wrapFieldWithLabel("f. Status Ekonomi", panelGridPilihan(3, 8, rbEkonomiBaik, rbEkonomiSedang, rbEkonomiKurang)));
        isi.add(rowSocial);

        kartu.add(isi, BorderLayout.CENTER);
        return kartu;
    }

    // ---------- SEKSI 03: OBJEKTIF (PEMERIKSAAN UMUM) ----------
    private JPanel rakitSeksi2Objektif() {
        JPanel kartu = buatKotakSeksiCard("03", "PEMERIKSAAN UMUM", "Keadaan Umum, Tingkat Kesadaran dan Skala GCS");
        JPanel isi = new JPanel();
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));
        isi.setOpaque(false);
        isi.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // a. Keadaan Umum & b. Kesadaran (2 Kolom Berdampingan)
        grupKeadaan = new ButtonGroup();
        rbKeadaanSehat = new widget.RadioButton(); rbKeadaanSehat.setText("Sehat");
        rbKeadaanRingan = new widget.RadioButton(); rbKeadaanRingan.setText("Sakit Ringan");
        rbKeadaanSedang = new widget.RadioButton(); rbKeadaanSedang.setText("Sakit Sedang"); rbKeadaanSedang.setSelected(true);
        rbKeadaanBerat = new widget.RadioButton(); rbKeadaanBerat.setText("Sakit Berat");
        grupKeadaan.add(rbKeadaanSehat);
        grupKeadaan.add(rbKeadaanRingan);
        grupKeadaan.add(rbKeadaanSedang);
        grupKeadaan.add(rbKeadaanBerat);

        grupKesadaran = new ButtonGroup();
        rbSadarCompos = new widget.RadioButton(); rbSadarCompos.setText("Compos Mentis"); rbSadarCompos.setSelected(true);
        rbSadarApatis = new widget.RadioButton(); rbSadarApatis.setText("Apatis");
        rbSadarSomnolen = new widget.RadioButton(); rbSadarSomnolen.setText("Somnolen");
        rbSadarSopor = new widget.RadioButton(); rbSadarSopor.setText("Sopor");
        rbSadarKoma = new widget.RadioButton(); rbSadarKoma.setText("Koma");
        grupKesadaran.add(rbSadarCompos);
        grupKesadaran.add(rbSadarApatis);
        grupKesadaran.add(rbSadarSomnolen);
        grupKesadaran.add(rbSadarSopor);
        grupKesadaran.add(rbSadarKoma);

        JPanel rowKuKes = new JPanel(new GridLayout(1, 2, 16, 0));
        rowKuKes.setOpaque(false);
        rowKuKes.add(wrapFieldWithLabel("a. Keadaan Umum", panelGridPilihan(4, 4, rbKeadaanSehat, rbKeadaanRingan, rbKeadaanSedang, rbKeadaanBerat)));
        rowKuKes.add(wrapFieldWithLabel("b. Tingkat Kesadaran", panelGridPilihan(5, 4, rbSadarCompos, rbSadarApatis, rbSadarSomnolen, rbSadarSopor, rbSadarKoma)));
        isi.add(rowKuKes);
        isi.add(Box.createVerticalStrut(14));

        // c. Skala GCS (4 Kolom Sejajar)
        GCS_E = new widget.TextBox(); GCS_E.setText("4"); GCS_E.setPreferredSize(new Dimension(0, 30)); gayaKotakInput(GCS_E);
        GCS_M = new widget.TextBox(); GCS_M.setText("6"); GCS_M.setPreferredSize(new Dimension(0, 30)); gayaKotakInput(GCS_M);
        GCS_V = new widget.TextBox(); GCS_V.setText("5"); GCS_V.setPreferredSize(new Dimension(0, 30)); gayaKotakInput(GCS_V);
        lblTotalGCS = new widget.Label() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_TEAL_SOFT);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblTotalGCS.setText("Total GCS: 15 (E4M6V5)");
        lblTotalGCS.setFont(FONT_MONO);
        lblTotalGCS.setForeground(COL_TEAL_DARK);
        lblTotalGCS.setOpaque(false);
        lblTotalGCS.setHorizontalAlignment(SwingConstants.CENTER);
        lblTotalGCS.setPreferredSize(new Dimension(0, 30));
        lblTotalGCS.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COL_TEAL_BORDER, 8, 1.2f),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));

        Runnable hitungGCS = () -> {
            try {
                int e = Integer.parseInt(GCS_E.getText().trim());
                int m = Integer.parseInt(GCS_M.getText().trim());
                int v = Integer.parseInt(GCS_V.getText().trim());
                lblTotalGCS.setText("Total GCS: " + (e + m + v) + " (E" + e + "M" + m + "V" + v + ")");
            } catch (Exception ex) {
                lblTotalGCS.setText("Total GCS: -");
            }
        };
        DocumentListener gcsDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { hitungGCS.run(); }
            @Override public void removeUpdate(DocumentEvent e) { hitungGCS.run(); }
            @Override public void changedUpdate(DocumentEvent e) { hitungGCS.run(); }
        };
        GCS_E.getDocument().addDocumentListener(gcsDl);
        GCS_M.getDocument().addDocumentListener(gcsDl);
        GCS_V.getDocument().addDocumentListener(gcsDl);

        JPanel rowGcs = new JPanel(new GridLayout(1, 4, 14, 0));
        rowGcs.setOpaque(false);
        rowGcs.add(wrapFieldWithLabel("c. Skala GCS - EYE (E) [1-4]", GCS_E));
        rowGcs.add(wrapFieldWithLabel("MOTORIK (M) [1-6]", GCS_M));
        rowGcs.add(wrapFieldWithLabel("VERBAL (V) [1-5]", GCS_V));
        rowGcs.add(wrapFieldWithLabel("TOTAL SKALA GCS", lblTotalGCS));
        isi.add(rowGcs);

        // Variabel Tanda Vital & Gizi tetap diinisialisasi agar aman bagi adapter DB & report
        TDSistol = new widget.TextBox(); TDSistol.setText("");
        TDDiastol = new widget.TextBox(); TDDiastol.setText("");
        Nadi = new widget.TextBox(); Nadi.setText("");
        RR = new widget.TextBox(); RR.setText("");
        Suhu = new widget.TextBox(); Suhu.setText("");
        SPO2 = new widget.TextBox(); SPO2.setText("");
        TB = new widget.TextBox(); TB.setText("");
        BB = new widget.TextBox(); BB.setText("");
        IMT = new widget.TextBox(); IMT.setText("");
        grupStatusGizi = new ButtonGroup();
        rbGiziKurang = new widget.RadioButton(); rbGiziKurang.setText("Gizi Kurang");
        rbGiziBaik = new widget.RadioButton(); rbGiziBaik.setText("Baik");
        rbGiziLebih = new widget.RadioButton(); rbGiziLebih.setText("Gizi Lebih");
        grupStatusGizi.add(rbGiziKurang); grupStatusGizi.add(rbGiziBaik); grupStatusGizi.add(rbGiziLebih);

        kartu.add(isi, BorderLayout.CENTER);
        return kartu;
    }

    // ---------- SEKSI 04: PEMERIKSAAN PENUNJANG PRA-RAWAT INAP ----------
    private JPanel rakitSeksi3Penunjang() {
        JPanel kartu = buatKotakSeksiCard("04", "PEMERIKSAAN PENUNJANG", "Hasil Laboratorium, Radiologi dan Penunjang Lain");
        JPanel isi = new JPanel(new BorderLayout(0, 8));
        isi.setOpaque(false);
        isi.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setOpaque(false);
        JLabel lbl = new JLabel("Hasil Penunjang Pre-Rawat Inap / Saat Masuk (Laboratorium, Radiologi, EKG, dll)");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(COL_TEXT_DARK);

        JPanel pnlTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlTombol.setOpaque(false);

        widget.Button btnAmbilPenunjang = createFlatButton("Ambil Hasil Lab & Radiologi (Pre-Ranap)", COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_PRIMARY);
        btnAmbilPenunjang.setFont(FONT_LABEL.deriveFont(Font.BOLD, 11f));
        btnAmbilPenunjang.setPreferredSize(new Dimension(270, 28));
        btnAmbilPenunjang.setToolTipText("Tarik dan pilih hasil pemeriksaan Laboratorium & Radiologi riil pasien dari SIMRS");
        btnAmbilPenunjang.addActionListener(e -> bukaDialogAmbilPenunjang());

        pnlTombol.add(btnAmbilPenunjang);

        topRow.add(lbl, BorderLayout.WEST);
        topRow.add(pnlTombol, BorderLayout.EAST);

        PemeriksaanPenunjang = buatTextArea(3);

        isi.add(topRow, BorderLayout.NORTH);
        isi.add(buatScrollField(PemeriksaanPenunjang, 65), BorderLayout.CENTER);

        kartu.add(isi, BorderLayout.CENTER);
        return kartu;
    }

    private static class ItemPenunjang {
        boolean terpilih;
        String waktuPeriksa = "";
        String kategori = "";
        String namaPemeriksaan = "";
        String statusAdmisi = "";
        boolean isPreRanap;
        String ringkasanSingkat = "";
        String formattedText = "";
    }

    private void bukaDialogAmbilPenunjang() {
        if (TNoRw.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih pasien rawat inap terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final java.util.List<ItemPenunjang> daftar = new java.util.ArrayList<>();
        String tglMasukRanap = "";
        String jamMasukRanap = "";

        // 1. Ambil waktu admisi pertama ke kamar rawat inap
        try (PreparedStatement psKamar = koneksi.prepareStatement(
                "select tgl_masuk, jam_masuk from kamar_inap where no_rawat=? order by tgl_masuk asc, jam_masuk asc limit 1")) {
            psKamar.setString(1, TNoRw.getText().trim());
            try (ResultSet rsKamar = psKamar.executeQuery()) {
                if (rsKamar.next()) {
                    tglMasukRanap = rsKamar.getString("tgl_masuk") != null ? rsKamar.getString("tgl_masuk") : "";
                    jamMasukRanap = rsKamar.getString("jam_masuk") != null ? rsKamar.getString("jam_masuk") : "";
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi baca waktu admisi kamar : " + e);
        }

        // 2. Ambil Hasil Laboratorium PK (detail_periksa_lab)
        try (PreparedStatement psLab = koneksi.prepareStatement(
                "select dpl.tgl_periksa, dpl.jam, dpl.kd_jenis_prw, " +
                "coalesce(j.nm_perawatan, 'Laboratorium PK') as nm_perawatan, " +
                "coalesce(pl.status, 'Ralan') as status_rawat, " +
                "coalesce(tl.Pemeriksaan, 'Pemeriksaan') as nama_item, " +
                "dpl.nilai, coalesce(tl.satuan, '') as satuan, " +
                "coalesce(dpl.nilai_rujukan, '') as nilai_rujukan, " +
                "coalesce(dpl.keterangan, '') as keterangan " +
                "from detail_periksa_lab dpl " +
                "left join periksa_lab pl on dpl.no_rawat = pl.no_rawat " +
                "  and dpl.kd_jenis_prw = pl.kd_jenis_prw " +
                "  and dpl.tgl_periksa = pl.tgl_periksa " +
                "  and dpl.jam = pl.jam " +
                "left join jns_perawatan_lab j on dpl.kd_jenis_prw = j.kd_jenis_prw " +
                "left join template_laboratorium tl on dpl.id_template = tl.id_template " +
                "where dpl.no_rawat = ? and dpl.nilai is not null and trim(dpl.nilai) <> '' " +
                "order by dpl.tgl_periksa asc, dpl.jam asc, dpl.kd_jenis_prw asc, tl.urut asc")) {
            psLab.setString(1, TNoRw.getText().trim());
            try (ResultSet rsLab = psLab.executeQuery()) {
                String curGroup = "";
                ItemPenunjang curItem = null;
                StringBuilder sbDetail = null;
                StringBuilder sbRingkas = null;
                int countItem = 0;

                while (rsLab.next()) {
                    String tgl = rsLab.getString("tgl_periksa");
                    String jam = rsLab.getString("jam");
                    String kdPrw = rsLab.getString("kd_jenis_prw");
                    String groupKey = tgl + " " + jam + " " + kdPrw;

                    if (!groupKey.equals(curGroup)) {
                        if (curItem != null) {
                            curItem.formattedText = sbDetail.toString();
                            curItem.ringkasanSingkat = sbRingkas.toString();
                            daftar.add(curItem);
                        }
                        curGroup = groupKey;
                        curItem = new ItemPenunjang();
                        curItem.waktuPeriksa = tgl + " " + jam;
                        curItem.kategori = "Laboratorium PK";
                        curItem.namaPemeriksaan = rsLab.getString("nm_perawatan");

                        String statusRawat = rsLab.getString("status_rawat");
                        boolean isPre = statusRawat.equalsIgnoreCase("Ralan");
                        if (!isPre && !tglMasukRanap.isEmpty()) {
                            isPre = (tgl + " " + jam).compareTo(tglMasukRanap + " " + jamMasukRanap) <= 0;
                        }
                        curItem.isPreRanap = isPre;
                        curItem.terpilih = isPre;
                        curItem.statusAdmisi = isPre ? (statusRawat.equalsIgnoreCase("Ralan") ? "Pre-Ranap (IGD/Poli)" : "Pre-Ranap (Admisi)") : "Bangsal Ranap";

                        sbDetail = new StringBuilder();
                        sbDetail.append("[HASIL LABORATORIUM - ").append(curItem.waktuPeriksa).append(" WIB (").append(curItem.statusAdmisi).append(")]\n");
                        sbDetail.append("Pemeriksaan: ").append(curItem.namaPemeriksaan).append("\n");

                        sbRingkas = new StringBuilder();
                        countItem = 0;
                    }

                    String namaItem = rsLab.getString("nama_item");
                    String nilai = rsLab.getString("nilai");
                    String satuan = rsLab.getString("satuan");
                    String rujukan = rsLab.getString("nilai_rujukan");
                    String ket = rsLab.getString("keterangan");

                    sbDetail.append("- ").append(namaItem).append(" : ").append(nilai);
                    if (!satuan.isEmpty()) sbDetail.append(" ").append(satuan);
                    if (!rujukan.isEmpty()) sbDetail.append(" (Rujukan: ").append(rujukan).append(")");
                    if (!ket.isEmpty()) sbDetail.append(" [").append(ket).append("]");
                    sbDetail.append("\n");

                    if (countItem < 3) {
                        if (countItem > 0) sbRingkas.append(", ");
                        sbRingkas.append(namaItem).append(" ").append(nilai);
                        countItem++;
                    } else if (countItem == 3) {
                        sbRingkas.append("...");
                        countItem++;
                    }
                }
                if (curItem != null) {
                    curItem.formattedText = sbDetail.toString();
                    curItem.ringkasanSingkat = sbRingkas.toString();
                    daftar.add(curItem);
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi baca lab PK : " + e);
        }

        // 3. Ambil Hasil Radiologi (hasil_radiologi)
        try (PreparedStatement psRad = koneksi.prepareStatement(
                "select hr.tgl_periksa, hr.jam, hr.hasil, " +
                "coalesce(j.nm_perawatan, 'Foto Rontgen/Radiologi') as nm_perawatan, " +
                "coalesce(pr.status, 'Ralan') as status_rawat " +
                "from hasil_radiologi hr " +
                "left join periksa_radiologi pr on hr.no_rawat = pr.no_rawat " +
                "  and hr.tgl_periksa = pr.tgl_periksa " +
                "left join jns_perawatan_radiologi j on pr.kd_jenis_prw = j.kd_jenis_prw " +
                "where hr.no_rawat = ? and hr.hasil is not null and trim(hr.hasil) <> '' " +
                "order by hr.tgl_periksa asc, hr.jam asc")) {
            psRad.setString(1, TNoRw.getText().trim());
            try (ResultSet rsRad = psRad.executeQuery()) {
                while (rsRad.next()) {
                    String tgl = rsRad.getString("tgl_periksa");
                    String jam = rsRad.getString("jam");
                    String hasil = rsRad.getString("hasil");
                    String nmPrw = rsRad.getString("nm_perawatan");
                    String statusRawat = rsRad.getString("status_rawat");

                    ItemPenunjang radItem = new ItemPenunjang();
                    radItem.waktuPeriksa = tgl + " " + jam;
                    radItem.kategori = "Radiologi";
                    radItem.namaPemeriksaan = nmPrw;

                    boolean isPre = statusRawat.equalsIgnoreCase("Ralan");
                    if (!isPre && !tglMasukRanap.isEmpty()) {
                        isPre = (tgl + " " + jam).compareTo(tglMasukRanap + " " + jamMasukRanap) <= 0;
                    }
                    radItem.isPreRanap = isPre;
                    radItem.terpilih = isPre;
                    radItem.statusAdmisi = isPre ? (statusRawat.equalsIgnoreCase("Ralan") ? "Pre-Ranap (IGD/Poli)" : "Pre-Ranap (Admisi)") : "Bangsal Ranap";

                    StringBuilder sb = new StringBuilder();
                    sb.append("[HASIL RADIOLOGI - ").append(radItem.waktuPeriksa).append(" WIB (").append(radItem.statusAdmisi).append(")]\n");
                    sb.append("Pemeriksaan: ").append(nmPrw).append("\n");
                    sb.append("Hasil Ekspertise:\n").append(hasil.trim()).append("\n");
                    radItem.formattedText = sb.toString();

                    String ringkas = hasil.replaceAll("[\r\n]+", " ").trim();
                    if (ringkas.length() > 60) ringkas = ringkas.substring(0, 57) + "...";
                    radItem.ringkasanSingkat = ringkas;

                    daftar.add(radItem);
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi baca hasil radiologi : " + e);
        }

        // 4. Ambil Hasil Patologi Anatomi (detail_periksa_labpa)
        try (PreparedStatement psPa = koneksi.prepareStatement(
                "select dpa.tgl_periksa, dpa.jam, " +
                "coalesce(j.nm_perawatan, 'Laboratorium PA') as nm_perawatan, " +
                "dpa.diagnosa_klinik, dpa.makroskopik, dpa.mikroskopik, dpa.kesimpulan, dpa.kesan " +
                "from detail_periksa_labpa dpa " +
                "left join jns_perawatan_lab j on dpa.kd_jenis_prw = j.kd_jenis_prw " +
                "where dpa.no_rawat = ? " +
                "order by dpa.tgl_periksa asc, dpa.jam asc")) {
            psPa.setString(1, TNoRw.getText().trim());
            try (ResultSet rsPa = psPa.executeQuery()) {
                while (rsPa.next()) {
                    String tgl = rsPa.getString("tgl_periksa");
                    String jam = rsPa.getString("jam");
                    String nmPrw = rsPa.getString("nm_perawatan");

                    ItemPenunjang paItem = new ItemPenunjang();
                    paItem.waktuPeriksa = tgl + " " + jam;
                    paItem.kategori = "Laboratorium PA";
                    paItem.namaPemeriksaan = nmPrw;

                    boolean isPre = !tglMasukRanap.isEmpty() && (tgl + " " + jam).compareTo(tglMasukRanap + " " + jamMasukRanap) <= 0;
                    paItem.isPreRanap = isPre;
                    paItem.terpilih = isPre;
                    paItem.statusAdmisi = isPre ? "Pre-Ranap" : "Bangsal Ranap";

                    StringBuilder sb = new StringBuilder();
                    sb.append("[HASIL PATOLOGI ANATOMI - ").append(paItem.waktuPeriksa).append(" WIB (").append(paItem.statusAdmisi).append(")]\n");
                    sb.append("Pemeriksaan: ").append(nmPrw).append("\n");
                    if (rsPa.getString("diagnosa_klinik") != null && !rsPa.getString("diagnosa_klinik").isEmpty()) {
                        sb.append("Diagnosa Klinik: ").append(rsPa.getString("diagnosa_klinik")).append("\n");
                    }
                    if (rsPa.getString("kesimpulan") != null && !rsPa.getString("kesimpulan").isEmpty()) {
                        sb.append("Kesimpulan: ").append(rsPa.getString("kesimpulan")).append("\n");
                    }
                    if (rsPa.getString("kesan") != null && !rsPa.getString("kesan").isEmpty()) {
                        sb.append("Kesan: ").append(rsPa.getString("kesan")).append("\n");
                    }
                    paItem.formattedText = sb.toString();
                    paItem.ringkasanSingkat = rsPa.getString("kesimpulan") != null ? rsPa.getString("kesimpulan") : "Hasil PA Selesai";

                    daftar.add(paItem);
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi baca lab PA : " + e);
        }

        if (daftar.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Belum ada hasil pemeriksaan Laboratorium atau Radiologi yang divalidasi untuk No. Rawat ini di SIMRS.\n" +
                    "Hasil penunjang luar dapat diketik langsung secara manual pada kotak catatan penunjang.",
                    "Tidak Ada Hasil Penunjang di SIMRS", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Tampilkan dialog pop-up pemilihan hasil
        tampilkanDialogPilihanPenunjang(daftar, tglMasukRanap, jamMasukRanap);
    }

    private void tampilkanDialogPilihanPenunjang(final java.util.List<ItemPenunjang> daftar, String tglMasukRanap, String jamMasukRanap) {
        JDialog dlg = new JDialog(this, "Riwayat Hasil Pemeriksaan Penunjang Pasien", true);
        dlg.setSize(880, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // Header dialog
        JPanel pnlHeader = new JPanel(new BorderLayout(8, 4));
        pnlHeader.setBackground(COL_CARD_HEADER);
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COL_BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel lblTitle = new JLabel("HASIL PEMERIKSAAN PENUNJANG (LABORATORIUM & RADIOLOGI)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(COL_TEXT_DARK);

        String admisiInfo = (tglMasukRanap.isEmpty() ? "" : "  |  Masuk Kamar Ranap: " + tglMasukRanap + " " + jamMasukRanap + " WIB");
        JLabel lblSub = new JLabel("Pasien: " + TPasien.getText() + "  |  No. RM: " + TNoRM.getText() + "  |  No. Rawat: " + TNoRw.getText() + admisiInfo);
        lblSub.setFont(FONT_SUBLABEL);
        lblSub.setForeground(COL_TEXT_MUTED);

        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        dlg.add(pnlHeader, BorderLayout.NORTH);

        // Tabel Data
        DefaultTableModel model = new DefaultTableModel(null, new Object[]{
            "Pilih", "Waktu Periksa", "Kategori", "Nama Pemeriksaan", "Status Admisi", "Ringkasan Temuan"
        }) {
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0;
            }
        };

        for (ItemPenunjang it : daftar) {
            model.addRow(new Object[]{
                it.terpilih,
                it.waktuPeriksa,
                it.kategori,
                it.namaPemeriksaan,
                it.statusAdmisi,
                it.ringkasanSingkat
            });
        }

        widget.Table tb = new widget.Table();
        tb.setModel(model);
        tb.setRowHeight(24);
        if (tb.getColumnModel().getColumnCount() >= 6) {
            tb.getColumnModel().getColumn(0).setPreferredWidth(50);
            tb.getColumnModel().getColumn(0).setMaxWidth(60);
            tb.getColumnModel().getColumn(1).setPreferredWidth(130);
            tb.getColumnModel().getColumn(2).setPreferredWidth(110);
            tb.getColumnModel().getColumn(3).setPreferredWidth(180);
            tb.getColumnModel().getColumn(4).setPreferredWidth(130);
            tb.getColumnModel().getColumn(5).setPreferredWidth(220);
        }

        widget.ScrollPane scrollTable = new widget.ScrollPane();
        scrollTable.setViewportView(tb);
        scrollTable.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COL_BORDER));

        // Area Preview
        JTextArea txtPreview = new JTextArea();
        txtPreview.setEditable(false);
        txtPreview.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtPreview.setBackground(new Color(0xFB, 0xFD, 0xFD));
        txtPreview.setForeground(new Color(0x1A, 0x30, 0x38));
        txtPreview.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        widget.ScrollPane scrollPreview = new widget.ScrollPane();
        scrollPreview.setViewportView(txtPreview);
        scrollPreview.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COL_BORDER),
                " Pratinjau Rangkuman Teks yang Akan Disalin ke Formulir ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                FONT_LABEL.deriveFont(Font.BOLD, 10.5f),
                COL_TEAL_DARK));

        JLabel lblCount = new JLabel();
        lblCount.setFont(FONT_LABEL);
        lblCount.setForeground(COL_TEXT_MUTED);

        updatePreviewText(txtPreview, daftar, lblCount);

        model.addTableModelListener(e -> {
            int row = e.getFirstRow();
            if (row >= 0 && row < daftar.size() && e.getColumn() == 0) {
                Boolean val = (Boolean) model.getValueAt(row, 0);
                daftar.get(row).terpilih = (val != null && val);
                updatePreviewText(txtPreview, daftar, lblCount);
            }
        });

        // Toolbar Quick Filter
        JPanel pnlToolbar = new JPanel(new BorderLayout(8, 0));
        pnlToolbar.setOpaque(false);
        pnlToolbar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel pnlAksiFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlAksiFilter.setOpaque(false);

        widget.Button btnPreOnly = createFlatButton("Pilih Pre-Ranap Saja (Rekomendasi)", COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        btnPreOnly.setFont(FONT_LABEL.deriveFont(Font.BOLD, 11f));
        btnPreOnly.addActionListener(e -> {
            for (int i = 0; i < daftar.size(); i++) {
                boolean target = daftar.get(i).isPreRanap;
                daftar.get(i).terpilih = target;
                model.setValueAt(target, i, 0);
            }
            updatePreviewText(txtPreview, daftar, lblCount);
        });

        widget.Button btnAll = createFlatButton("Pilih Semua", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnAll.setFont(FONT_LABEL.deriveFont(11f));
        btnAll.addActionListener(e -> {
            for (int i = 0; i < daftar.size(); i++) {
                daftar.get(i).terpilih = true;
                model.setValueAt(true, i, 0);
            }
            updatePreviewText(txtPreview, daftar, lblCount);
        });

        widget.Button btnNone = createFlatButton("Hapus Pilihan", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnNone.setFont(FONT_LABEL.deriveFont(11f));
        btnNone.addActionListener(e -> {
            for (int i = 0; i < daftar.size(); i++) {
                daftar.get(i).terpilih = false;
                model.setValueAt(false, i, 0);
            }
            updatePreviewText(txtPreview, daftar, lblCount);
        });

        pnlAksiFilter.add(btnPreOnly);
        pnlAksiFilter.add(btnAll);
        pnlAksiFilter.add(btnNone);

        pnlToolbar.add(pnlAksiFilter, BorderLayout.WEST);
        pnlToolbar.add(lblCount, BorderLayout.EAST);

        // Body Tengah: Split Table & Preview
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTable, scrollPreview);
        split.setDividerLocation(200);
        split.setResizeWeight(0.45);
        split.setBorder(null);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.add(pnlToolbar, BorderLayout.NORTH);
        pnlCenter.add(split, BorderLayout.CENTER);
        dlg.add(pnlCenter, BorderLayout.CENTER);

        // Footer Action
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COL_BORDER));

        widget.Button btnBatal = createFlatButton("Batal", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnBatal.setPreferredSize(new Dimension(90, 32));
        btnBatal.setFont(FONT_LABEL);
        btnBatal.addActionListener(e -> dlg.dispose());

        widget.Button btnSalin = createFlatButton("Salin Terpilih ke Formulir", COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_PRIMARY);
        btnSalin.setPreferredSize(new Dimension(210, 32));
        btnSalin.setFont(FONT_LABEL.deriveFont(Font.BOLD, 11.5f));
        btnSalin.addActionListener(e -> {
            StringBuilder sbHasil = new StringBuilder();
            int countTerpilih = 0;
            for (ItemPenunjang it : daftar) {
                if (it.terpilih) {
                    if (sbHasil.length() > 0) sbHasil.append("\n\n");
                    sbHasil.append(it.formattedText.trim());
                    countTerpilih++;
                }
            }

            if (countTerpilih == 0) {
                JOptionPane.showMessageDialog(dlg, "Silahkan centang minimal satu hasil pemeriksaan terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String teksBaru = sbHasil.toString().trim();
            if (PemeriksaanPenunjang.getText().trim().isEmpty()) {
                PemeriksaanPenunjang.setText(teksBaru);
            } else {
                Object[] opsi = new Object[]{"Tambahkan di Bawah (Append)", "Ganti Seluruhnya (Replace)", "Batal"};
                int pilihan = JOptionPane.showOptionDialog(dlg,
                        "Kotak Pemeriksaan Penunjang saat ini sudah memiliki isian catatan.\n" +
                        "Bagaimana Anda ingin menyalin hasil pemeriksaan penunjang ini?",
                        "Konfirmasi Penyalinan Teks",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, opsi, opsi[0]);

                if (pilihan == 0) { // Append
                    PemeriksaanPenunjang.append("\n\n" + teksBaru);
                } else if (pilihan == 1) { // Replace
                    PemeriksaanPenunjang.setText(teksBaru);
                } else {
                    return;
                }
            }

            dlg.dispose();
            PemeriksaanPenunjang.requestFocus();
            hitungProgres();
            JOptionPane.showMessageDialog(RMAsesmenMedisRanap20_1.this,
                    countTerpilih + " hasil pemeriksaan penunjang berhasil disalin ke formulir.",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
        });

        pnlFooter.add(btnBatal);
        pnlFooter.add(btnSalin);
        dlg.add(pnlFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void updatePreviewText(JTextArea txtPreview, java.util.List<ItemPenunjang> daftar, JLabel lblCount) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (ItemPenunjang it : daftar) {
            if (it.terpilih) {
                if (sb.length() > 0) sb.append("\n\n");
                sb.append(it.formattedText.trim());
                count++;
            }
        }
        txtPreview.setText(sb.toString());
        txtPreview.setCaretPosition(0);
        if (lblCount != null) {
            lblCount.setText("Terpilih: " + count + " dari " + daftar.size() + " item");
        }
    }

    // ---------- SEKSI 05: PEMERIKSAAN FISIK 14 ORGAN (a - n) ----------
    private JPanel rakitSeksi4Fisik() {
        JPanel kartu = buatKotakSeksiCard("05", "PEMERIKSAAN FISIK", "Pemeriksaan Sistem Organ Tubuh (Head-to-Toe)");

        // Compact 2-Column Grid Layout for 14 Organ Systems (7 baris x 2 kolom)
        JPanel gridOrgans = new JPanel(new GridLayout(7, 2, 16, 10));
        gridOrgans.setOpaque(false);
        gridOrgans.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        for (RegioFisik rf : regios) {
            gridOrgans.add(buatBarisRegio(rf));
        }

        kartu.add(gridOrgans, BorderLayout.CENTER);
        return kartu;
    }

    private JPanel buatBarisRegio(RegioFisik rf) {
        rf.grup = new ButtonGroup();
        rf.rbNormal = new widget.RadioButton(); rf.rbNormal.setText("Normal"); rf.rbNormal.setSelected(true);
        rf.rbTidakNormal = new widget.RadioButton(); rf.rbTidakNormal.setText("Tidak Normal");
        rf.grup.add(rf.rbNormal);
        rf.grup.add(rf.rbTidakNormal);

        pasangChipMedis(rf.rbNormal);
        pasangChipMedis(rf.rbTidakNormal);

        rf.txtKet = new widget.TextBox();
        rf.txtKet.setPreferredSize(new Dimension(0, 28));
        rf.txtKet.setMinimumSize(new Dimension(100, 28));
        rf.txtKet.setFont(FONT_INPUT);
        gayaKotakInput(rf.txtKet);

        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setOpaque(true);
        rf.panelRow = pnl;

        ItemListener il = e -> {
            boolean tidakNormal = rf.rbTidakNormal.isSelected();
            aturStatusFisik(rf, tidakNormal);
            if (rf.panelRow != null) {
                rf.panelRow.revalidate();
                rf.panelRow.repaint();
            }
            hitungProgres();
        };
        rf.rbNormal.addItemListener(il);
        rf.rbTidakNormal.addItemListener(il);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lbl = new JLabel(rf.labelTeks);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(COL_TEXT_DARK);

        JPanel pnlPills = panelGridPilihan(2, 4, rf.rbNormal, rf.rbTidakNormal);
        pnlPills.setPreferredSize(new Dimension(190, 28));

        topBar.add(lbl, BorderLayout.WEST);
        topBar.add(pnlPills, BorderLayout.EAST);

        pnl.add(topBar, BorderLayout.NORTH);
        pnl.add(rf.txtKet, BorderLayout.CENTER);

        aturStatusFisik(rf, false);
        return pnl;
    }

    // ---------- SEKSI 06: KANVAS ANATOMI TUBUH (STATUS LOKALIS) ----------
    private JPanel rakitSeksi5Kanvas() {
        JPanel kartu = buatKotakSeksiCard("06", "STATUS LOKALIS", "Penandaan Anatomi Tubuh Pasien (Klik kanvas untuk menandai lokasi)");
        JPanel body = new JPanel(new BorderLayout(16, 12));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));

        kanvasLokalis = new KanvasStatusLokalis(550, 363);

        // Toolbar Kanvas
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(COL_CARD_HEADER);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.setOpaque(false);
        JLabel lblTool = new JLabel("Peralatan Kanvas:");
        lblTool.setFont(FONT_LABEL);
        lblTool.setForeground(COL_TEXT_DARK);

        widget.Button btnLingkaran = createFlatButton("Lingkaran (O)", COL_ROSE_PRIMARY, Color.WHITE, COL_ROSE_PRIMARY);
        widget.Button btnSilang = createFlatButton("Silang (X)", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        widget.Button btnPensil = createFlatButton("Pensil Bebas", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);

        btnLingkaran.addActionListener(e -> {
            kanvasLokalis.setModeAlat(KanvasStatusLokalis.ModeAlat.LINGKARAN);
            styleButton(btnLingkaran, COL_ROSE_PRIMARY, Color.WHITE, COL_ROSE_PRIMARY);
            styleButton(btnSilang, Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
            styleButton(btnPensil, Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        });
        btnSilang.addActionListener(e -> {
            kanvasLokalis.setModeAlat(KanvasStatusLokalis.ModeAlat.SILANG);
            styleButton(btnSilang, COL_ROSE_PRIMARY, Color.WHITE, COL_ROSE_PRIMARY);
            styleButton(btnLingkaran, Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
            styleButton(btnPensil, Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        });
        btnPensil.addActionListener(e -> {
            kanvasLokalis.setModeAlat(KanvasStatusLokalis.ModeAlat.PENSIL);
            styleButton(btnPensil, COL_ROSE_PRIMARY, Color.WHITE, COL_ROSE_PRIMARY);
            styleButton(btnLingkaran, Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
            styleButton(btnSilang, Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        });

        leftTools.add(lblTool);
        leftTools.add(btnLingkaran);
        leftTools.add(btnSilang);
        leftTools.add(btnPensil);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setOpaque(false);
        widget.Button btnUndo = createFlatButton("Undo", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        widget.Button btnBersihkan = createFlatButton("Bersihkan Coretan", COL_ROSE_SOFT, COL_ROSE_PRIMARY, COL_ROSE_BORDER);

        btnUndo.addActionListener(e -> kanvasLokalis.undoLast());
        btnBersihkan.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(null, "Hapus semua coretan tanda lokasi di kanvas anatomi?",
                    "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                kanvasLokalis.hapusCoretan();
            }
        });

        rightTools.add(btnUndo);
        rightTools.add(btnBersihkan);

        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);

        // Konten Tengah: Kanvas Anatomi Tubuh 4 View (Luas & Bersih Tanpa Keterangan Teks Samping)
        JPanel pnlKanvasWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlKanvasWrap.setOpaque(false);
        pnlKanvasWrap.add(kanvasLokalis);

        body.add(toolbar, BorderLayout.NORTH);
        body.add(pnlKanvasWrap, BorderLayout.CENTER);

        kartu.add(body, BorderLayout.CENTER);
        return kartu;
    }

    // ---------- SEKSI 07: ASESMEN (DIAGNOSIS) ----------
    private JPanel rakitSeksi6Asesmen() {
        JPanel kartu = buatKotakSeksiCard("07", "DIAGNOSIS", "Diagnosis Medis Kerja dan Diagnosis Banding");
        JPanel body = new JPanel(new GridLayout(2, 1, 0, 14));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // Diagnosa Medis
        DiagnosaMedis = buatTextArea(2);
        BtnCariDiagnosaMedis = createFlatButton("Cari ICD-10", COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        BtnCariDiagnosaMedis.addActionListener(e -> bukaCariPenyakit(false));

        JPanel rowMedis = new JPanel(new BorderLayout(16, 0));
        rowMedis.setOpaque(false);
        JPanel leftMedis = new JPanel(new GridLayout(2, 1, 0, 6));
        leftMedis.setOpaque(false);
        leftMedis.setPreferredSize(new Dimension(210, 56));
        JLabel lblMedis = new JLabel("a. Diagnosis Kerja / Utama *");
        lblMedis.setFont(FONT_LABEL);
        lblMedis.setForeground(COL_TEXT_DARK);
        leftMedis.add(lblMedis);
        leftMedis.add(BtnCariDiagnosaMedis);
        rowMedis.add(leftMedis, BorderLayout.WEST);
        rowMedis.add(buatScrollField(DiagnosaMedis, 55), BorderLayout.CENTER);

        // Diagnosa Banding
        DiagnosaBanding = buatTextArea(2);
        BtnCariDiagnosaBanding = createFlatButton("Cari ICD-10", COL_TEAL_SOFT, COL_TEAL_DARK, COL_TEAL_BORDER);
        BtnCariDiagnosaBanding.addActionListener(e -> bukaCariPenyakit(true));

        JPanel rowBanding = new JPanel(new BorderLayout(16, 0));
        rowBanding.setOpaque(false);
        JPanel leftBanding = new JPanel(new GridLayout(2, 1, 0, 6));
        leftBanding.setOpaque(false);
        leftBanding.setPreferredSize(new Dimension(210, 56));
        JLabel lblBanding = new JLabel("b. Diagnosis Banding");
        lblBanding.setFont(FONT_LABEL);
        lblBanding.setForeground(COL_TEXT_DARK);
        leftBanding.add(lblBanding);
        leftBanding.add(BtnCariDiagnosaBanding);
        rowBanding.add(leftBanding, BorderLayout.WEST);
        rowBanding.add(buatScrollField(DiagnosaBanding, 55), BorderLayout.CENTER);

        body.add(rowMedis);
        body.add(rowBanding);

        kartu.add(body, BorderLayout.CENTER);
        return kartu;
    }

    // ---------- SEKSI 08: PROGRAM & PERENCANAAN ----------
    private JPanel rakitSeksi7Program() {
        JPanel kartu = buatKotakSeksiCard("08", "PROGRAM & PERENCANAAN", "Instruksi Terapi dan Rencana Tindakan");
        JPanel body = new JPanel(new GridLayout(2, 1, 0, 14));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        AdvisTerapi = buatTextArea(3);
        body.add(wrapFieldWithLabel("a. Advis Terapi & Terapi Medis *", buatScrollField(AdvisTerapi, 60)));

        RencanaTindakan = buatTextArea(3);
        body.add(wrapFieldWithLabel("b. Perencanaan Tindakan / Monitoring", buatScrollField(RencanaTindakan, 60)));

        // Footer Card Bawah (selaras dengan Argon, sudut bawah membulat)
        final int radFooter = 14;
        JPanel footer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                java.awt.geom.RoundRectangle2D.Float roundBottom = new java.awt.geom.RoundRectangle2D.Float(
                        0.6f, -radFooter, w - 1.2f, h + radFooter - 0.6f, radFooter, radFooter);
                g2.clip(new java.awt.Rectangle(0, 0, w, h));
                g2.setColor(COL_CARD_HEADER);
                g2.fill(roundBottom);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                g2.setColor(COL_BORDER_DIVIDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine(0, 0, w, 0);
                g2.dispose();
            }
        };
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel lblInfo = new JLabel("Pastikan seluruh bagian bertanda Wajib lengkap telah diperiksa sebelum menyimpan.");
        lblInfo.setFont(FONT_SUBLABEL);
        lblInfo.setForeground(COL_TEXT_MUTED);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);

        widget.Button btnReset = createFlatButton("Reset Isian", Color.WHITE, COL_TEXT_DARK, COL_INPUT_BORDER);
        btnReset.addActionListener(e -> emptTeks());

        widget.Button btnSaveFinal = createFlatButton("Simpan Asesmen Medis (RM 20-1)", COL_TEAL_PRIMARY, Color.WHITE, COL_TEAL_DARK);
        btnSaveFinal.addActionListener(e -> simpan());

        rightBtns.add(btnReset);
        rightBtns.add(btnSaveFinal);

        footer.add(lblInfo, BorderLayout.WEST);
        footer.add(rightBtns, BorderLayout.EAST);

        kartu.add(body, BorderLayout.CENTER);
        kartu.add(footer, BorderLayout.SOUTH);
        return kartu;
    }

    // =================================================================
    //  HELPER UI
    // =================================================================

    private JPanel buatKotakSeksiCard(String numberCode, String judul, String subjudul) {
        final int radius = 14;
        JPanel kartu = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Color.WHITE);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, radius, radius));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_BORDER);
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, radius, radius));
                g2.dispose();
            }
        };
        kartu.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                java.awt.geom.RoundRectangle2D.Float roundTop = new java.awt.geom.RoundRectangle2D.Float(
                        0.6f, 0.6f, w - 1.2f, h + radius, radius, radius);
                g2.clip(new java.awt.Rectangle(0, 0, w, h));
                g2.setColor(COL_CARD_HEADER);
                g2.fill(roundTop);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_BORDER_DIVIDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine(0, h - 1, w, h - 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel badge = new JLabel(numberCode, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_TEAL_PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setPreferredSize(new Dimension(28, 28));
        badge.setFont(FONT_MONO);
        badge.setForeground(Color.WHITE);
        badge.setOpaque(false);

        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 1));
        titleStack.setOpaque(false);
        JLabel lblTitle = new JLabel(judul);
        lblTitle.setFont(FONT_SECTION);
        lblTitle.setForeground(COL_TEXT_DARK);
        JLabel lblSub = new JLabel(subjudul != null ? subjudul : "");
        lblSub.setFont(FONT_SUBLABEL);
        lblSub.setForeground(COL_TEXT_MUTED);
        titleStack.add(lblTitle);
        titleStack.add(lblSub);

        left.add(badge);
        left.add(titleStack);

        JLabel reqBadge = new JLabel("Wajib lengkap") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_TEAL_SOFT);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 6, 6));
                g2.setColor(COL_TEAL_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        reqBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        reqBadge.setForeground(COL_TEAL_DARK);
        reqBadge.setOpaque(false);
        reqBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        header.add(left, BorderLayout.WEST);
        header.add(reqBadge, BorderLayout.EAST);

        kartu.add(header, BorderLayout.NORTH);
        return kartu;
    }

    private JPanel wrapFieldWithLabel(String labelText, Component field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(COL_TEXT_DARK);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel buatInputDenganSatuan(widget.TextBox txt, String unitText, boolean isTealBg) {
        final Color bgUtama = isTealBg ? COL_TEAL_SOFT : COL_INPUT_BG;
        final Color bgUnit = isTealBg ? new Color(204, 251, 241) : new Color(241, 245, 249);
        final Color colBorder = isTealBg ? COL_TEAL_BORDER : COL_INPUT_BORDER;

        JPanel pnl = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(bgUtama);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(colBorder);
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, 8, 8));
                g2.dispose();
            }
        };
        pnl.setOpaque(false);
        pnl.setPreferredSize(new Dimension(0, 30));

        txt.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
        txt.setOpaque(false);
        txt.setBackground(bgUtama);
        txt.setFont(FONT_MONO);
        txt.setForeground(isTealBg ? COL_TEAL_DARK : COL_TEXT_DARK);
        txt.putClientProperty("Component.arc", null);
        txt.putClientProperty("JComponent.roundRect", false);
        txt.putClientProperty("ModernStyle.active", false);

        JLabel lblUnit = new JLabel(unitText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                java.awt.geom.RoundRectangle2D.Float r = new java.awt.geom.RoundRectangle2D.Float(
                        -8, 0.6f, w + 7.4f, h - 1.2f, 8, 8);
                g2.clip(new java.awt.Rectangle(0, 0, w, h));
                g2.setColor(bgUnit);
                g2.fill(r);
                g2.setColor(colBorder);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine(0, 0, 0, h);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblUnit.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblUnit.setForeground(isTealBg ? COL_TEAL_DARK : COL_TEXT_MUTED);
        lblUnit.setOpaque(false);
        lblUnit.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

        pnl.add(txt, BorderLayout.CENTER);
        pnl.add(lblUnit, BorderLayout.EAST);
        return pnl;
    }

    private JPanel buatInputTekananDarah(widget.TextBox sys, widget.TextBox dia) {
        JPanel pnl = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_INPUT_BG);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_INPUT_BORDER);
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(offset, offset, w - strokeW, h - strokeW, 8, 8));
                g2.dispose();
            }
        };
        pnl.setOpaque(false);
        pnl.setPreferredSize(new Dimension(0, 30));

        sys.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        sys.setOpaque(false);
        sys.setBackground(COL_INPUT_BG);
        sys.setHorizontalAlignment(SwingConstants.CENTER);
        sys.setFont(FONT_MONO);
        sys.putClientProperty("Component.arc", null);
        sys.putClientProperty("JComponent.roundRect", false);
        sys.putClientProperty("ModernStyle.active", false);

        dia.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        dia.setOpaque(false);
        dia.setBackground(COL_INPUT_BG);
        dia.setHorizontalAlignment(SwingConstants.CENTER);
        dia.setFont(FONT_MONO);
        dia.putClientProperty("Component.arc", null);
        dia.putClientProperty("JComponent.roundRect", false);
        dia.putClientProperty("ModernStyle.active", false);

        JLabel lblSlash = new JLabel("/", SwingConstants.CENTER);
        lblSlash.setFont(FONT_MONO);
        lblSlash.setForeground(COL_TEXT_MUTED);

        JPanel centerNumbers = new JPanel(new GridLayout(1, 3, 0, 0));
        centerNumbers.setOpaque(false);
        centerNumbers.add(sys);
        centerNumbers.add(lblSlash);
        centerNumbers.add(dia);

        JLabel lblMmHg = new JLabel("mmHg", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                java.awt.geom.RoundRectangle2D.Float r = new java.awt.geom.RoundRectangle2D.Float(
                        -8, 0.6f, w + 7.4f, h - 1.2f, 8, 8);
                g2.clip(new java.awt.Rectangle(0, 0, w, h));
                g2.setColor(new Color(241, 245, 249));
                g2.fill(r);
                g2.setColor(COL_INPUT_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine(0, 0, 0, h);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblMmHg.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMmHg.setForeground(COL_TEXT_MUTED);
        lblMmHg.setOpaque(false);
        lblMmHg.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

        pnl.add(centerNumbers, BorderLayout.CENTER);
        pnl.add(lblMmHg, BorderLayout.EAST);
        return pnl;
    }

    private widget.Button createFlatButton(String text, Color bg, Color fg, Color borderCol) {
        widget.Button btn = new widget.Button();
        btn.setText(text);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        styleButton(btn, bg, fg, borderCol);
        return btn;
    }

    private void styleButton(widget.Button btn, Color bg, Color fg, Color borderCol) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(false);
        btn.putClientProperty("Component.arc", 8);
        btn.putClientProperty("Button.arc", 8);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(borderCol, 8, 1.2f),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
    }

    private widget.TextArea buatTextArea(int rows) {
        widget.TextArea ta = new widget.TextArea();
        ta.setColumns(20);
        ta.setRows(rows);
        ta.setFont(FONT_INPUT);
        ta.setForeground(COL_TEXT_DARK);
        ta.setBackground(COL_INPUT_BG);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return ta;
    }

    private JScrollPane buatScrollField(Component c, int height) {
        if (c instanceof JTextArea) {
            JTextArea ta = (JTextArea) c;
            ta.setBackground(COL_INPUT_BG);
            ta.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        }
        JScrollPane sp = new JScrollPane(c) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_INPUT_BG);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_INPUT_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 8, 8));
                g2.dispose();
            }
        };
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBackground(COL_INPUT_BG);
        sp.setPreferredSize(new Dimension(100, height));
        sp.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel buatInputDenganLookup(widget.TextBox txt, widget.Button btn) {
        JPanel pnl = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_INPUT_BG);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public void paint(Graphics g) {
                // 1. Lukis seluruh latar dan anak-anaknya terlebih dahulu
                super.paint(g);

                // 2. Lukis border membulat Argon TEPAT DI ATAS SELURUH ANAK (tidak bisa tertimpa oleh txt)
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float strokeW = 1.2f;
                float offset = strokeW / 2.0f;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COL_INPUT_BORDER);
                g2.setStroke(new BasicStroke(strokeW));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(
                        offset, offset, w - strokeW, h - strokeW, 8, 8));
                g2.dispose();
            }
        };
        pnl.setOpaque(false);
        pnl.setPreferredSize(new Dimension(180, 30));

        txt.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        txt.setOpaque(false);
        txt.setBackground(COL_INPUT_BG);
        txt.setFont(FONT_INPUT);
        txt.setForeground(COL_TEXT_DARK);
        txt.putClientProperty("Component.arc", null);
        txt.putClientProperty("JComponent.roundRect", false);
        txt.putClientProperty("ModernStyle.active", false);

        btn.setPreferredSize(new Dimension(30, 28));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setRoundRect(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setIcon(new ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        btn.setText("");

        pnl.add(txt, BorderLayout.CENTER);
        pnl.add(btn, BorderLayout.EAST);
        return pnl;
    }

    private void gayaKotakInput(Component c) {
        if (c instanceof javax.swing.JComponent) {
            javax.swing.JComponent jc = (javax.swing.JComponent) c;
            jc.setFont(FONT_INPUT);
            jc.setForeground(COL_TEXT_DARK);
            jc.setBackground(COL_INPUT_BG);
            jc.putClientProperty("Component.arc", 8);
            jc.putClientProperty("TextComponent.arc", 8);
            jc.putClientProperty("JTextField.arc", 8);
            jc.putClientProperty("ComboBox.arc", 8);
            jc.putClientProperty("JComponent.roundRect", true);
            jc.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(COL_INPUT_BORDER, 8, 1.2f),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        }
    }

    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int radius;
        private final float thickness;

        public RoundedBorder(Color color, int radius, float thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        public RoundedBorder(Color color, int radius) {
            this(color, radius, 1.2f);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            float offset = thickness / 2.0f;
            g2.draw(new java.awt.geom.RoundRectangle2D.Float(
                    x + offset, y + offset, width - thickness, height - thickness, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int pad = Math.round(thickness) + 4;
            return new Insets(pad, pad + 4, pad, pad + 4);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            int pad = Math.round(thickness) + 4;
            insets.top = pad;
            insets.left = pad + 4;
            insets.bottom = pad;
            insets.right = pad + 4;
            return insets;
        }
    }

    private JPanel buatBarisNomorPanel(String label, JLabel lblVal) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel lblK = new JLabel(label);
        lblK.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblK.setForeground(COL_TEXT_MUTED);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVal.setForeground(COL_TEXT_DARK);
        lblVal.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(lblK, BorderLayout.WEST);
        p.add(lblVal, BorderLayout.CENTER);
        return p;
    }

    private void sinkronKartuIdentitas() {
        if (lblKartuNama == null) return;
        String nama = TPasien.getText().trim();
        lblKartuNama.setText(nama.isEmpty() ? "-" : nama);
        lblKartuNama.setToolTipText(nama.isEmpty() ? null : nama);

        StringBuilder inisial = new StringBuilder();
        for (String b : nama.split("\\s+")) {
            if (!b.isEmpty() && inisial.length() < 2) {
                inisial.append(Character.toUpperCase(b.charAt(0)));
            }
        }
        lblKartuInisial.setText(inisial.length() == 0 ? "P" : inisial.toString());

        String jkText = "L".equalsIgnoreCase(Jk.getText().trim()) ? "Laki-Laki" : "Perempuan";
        String umurText = hitungUmur();
        String info = jkText + (umurText.isEmpty() ? "" : " - " + umurText);
        lblKartuInfo.setText(info);
        lblKartuInfo.setToolTipText(info);

        if (lblValRM != null) {
            String rm = TNoRM.getText().isEmpty() ? "-" : TNoRM.getText();
            lblValRM.setText(rm);
            lblValRM.setToolTipText(rm);
        }
        if (lblValRawat != null) {
            String rw = TNoRw.getText().isEmpty() ? "-" : TNoRw.getText();
            lblValRawat.setText(rw);
            lblValRawat.setToolTipText(rw);
        }
    }

    private String hitungUmur() {
        String tgl = TglLahir.getText().trim();
        if (tgl.length() < 10) return "";
        try {
            LocalDate lahir = LocalDate.parse(tgl.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Period p = Period.between(lahir, LocalDate.now());
            return p.getYears() + " Th " + p.getMonths() + " Bl";
        } catch (Exception ex) {
            return "";
        }
    }

    private void hitungProgres() {
        if (pbProgres == null) return;
        int terisi = 0;
        int total = 17;

        if (!KeluhanUtama.getText().trim().isEmpty()) terisi++;
        if (!RiwayatPenyakit.getText().trim().isEmpty()) terisi++;
        if (!RiwayatPengobatan.getText().trim().isEmpty()) terisi++;
        if (!AlergiObat.getText().trim().isEmpty() || !AlergiMakanan.getText().trim().isEmpty()) terisi++;
        if (pilihan(grupStatusEkonomi) != null) terisi++;

        if (pilihan(grupKeadaan) != null) terisi++;
        if (pilihan(grupKesadaran) != null) terisi++;
        if (!GCS_E.getText().trim().isEmpty() && !GCS_M.getText().trim().isEmpty() && !GCS_V.getText().trim().isEmpty()) terisi++;

        int organTerisi = 0;
        for (RegioFisik rf : regios) {
            if (rf.rbNormal.isSelected() || (rf.rbTidakNormal.isSelected() && !rf.txtKet.getText().trim().isEmpty())) {
                organTerisi++;
            }
        }
        if (organTerisi >= 10) terisi += 4;
        else if (organTerisi >= 5) terisi += 2;

        if (kanvasLokalis != null && kanvasLokalis.adaCoretan()) terisi += 2;
        if (!DiagnosaMedis.getText().trim().isEmpty()) terisi++;
        if (!DiagnosaBanding.getText().trim().isEmpty()) terisi++;
        if (!AdvisTerapi.getText().trim().isEmpty()) terisi++;
        if (!RencanaTindakan.getText().trim().isEmpty()) terisi++;

        int persen = Math.min(100, (terisi * 100) / total);
        pbProgres.setValue(persen);
        lblProgresPersen.setText(persen + "%");
        lblProgresHitung.setText(terisi + "/" + total + " bagian terisi");
    }

    // =================================================================
    //  BINDING DATA & MULTI-SOURCE ADAPTER
    // =================================================================

    private String[] nilaiRm20_1() {
        return new String[]{
            Valid.SetTgl(TglAsuhan.getSelectedItem() + "") + " " + (TglAsuhan.getSelectedItem() + "").substring(11, 16) + ":00",
            KdDokterPeriksa.getText(), KdDokterDpjp.getText(),
            CmbAnamnesis.getSelectedItem().toString(), THubungan.getText(),
            CmbAsalMasuk.getSelectedItem().toString(), CmbCaraMasuk.getSelectedItem().toString(),
            KeluhanUtama.getText(), RiwayatPenyakit.getText(), RiwayatPengobatan.getText(),
            AlergiObat.getText(), AlergiMakanan.getText(), RiwayatLain.getText(),
            pilihan(grupStatusEkonomi),
            pilihan(grupKeadaan), pilihan(grupKesadaran),
            GCS_E.getText(), GCS_M.getText(), GCS_V.getText(),
            TDSistol.getText(), TDDiastol.getText(),
            Nadi.getText(), RR.getText(), Suhu.getText(), SPO2.getText(),
            TB.getText(), BB.getText(), IMT.getText(), pilihan(grupStatusGizi),
            PemeriksaanPenunjang.getText(),
            pilihan(regios[0].grup), regios[0].txtKet.getText(),
            pilihan(regios[1].grup), regios[1].txtKet.getText(),
            pilihan(regios[2].grup), regios[2].txtKet.getText(),
            pilihan(regios[3].grup), regios[3].txtKet.getText(),
            pilihan(regios[4].grup), regios[4].txtKet.getText(),
            pilihan(regios[5].grup), regios[5].txtKet.getText(),
            pilihan(regios[6].grup), regios[6].txtKet.getText(),
            pilihan(regios[7].grup), regios[7].txtKet.getText(),
            pilihan(regios[8].grup), regios[8].txtKet.getText(),
            pilihan(regios[9].grup), regios[9].txtKet.getText(),
            pilihan(regios[10].grup), regios[10].txtKet.getText(),
            pilihan(regios[11].grup), regios[11].txtKet.getText(),
            pilihan(regios[12].grup), regios[12].txtKet.getText(),
            pilihan(regios[13].grup), regios[13].txtKet.getText(),
            kanvasLokalis == null ? "" : kanvasLokalis.getCoretanBase64(),
            DiagnosaMedis.getText(), DiagnosaBanding.getText(),
            AdvisTerapi.getText(), RencanaTindakan.getText()
        };
    }

    private String pilihan(ButtonGroup grup) {
        if (grup == null) return "";
        Enumeration<AbstractButton> e = grup.getElements();
        while (e.hasMoreElements()) {
            AbstractButton b = e.nextElement();
            if (b.isSelected()) {
                return b.getText();
            }
        }
        return "";
    }

    private void pilihRadio(ButtonGroup grup, String nilai) {
        if (grup == null || nilai == null || nilai.isEmpty()) return;
        Enumeration<AbstractButton> e = grup.getElements();
        while (e.hasMoreElements()) {
            AbstractButton b = e.nextElement();
            if (b.getText().equalsIgnoreCase(nilai.trim())) {
                b.setSelected(true);
                return;
            }
        }
    }

    private void simpan() {
        if (TNoRM.getText().trim().isEmpty()) {
            Valid.textKosong(TNoRw, "Nama Pasien");
        } else if (NmDokterPeriksa.getText().trim().isEmpty()) {
            Valid.textKosong(BtnDokterPeriksa, "Dokter Pemeriksa");
        } else if (NmDokterDpjp.getText().trim().isEmpty()) {
            Valid.textKosong(BtnDokterDpjp, "Dokter DPJP");
        } else if (DiagnosaMedis.getText().trim().isEmpty()) {
            Valid.textKosong(DiagnosaMedis, "Diagnosa Medis");
        } else {
            StringBuilder kol = new StringBuilder("no_rawat");
            StringBuilder tanya = new StringBuilder("?");
            for (String k : KOLOM_RM20_1) {
                kol.append(",`").append(k).append("`");
                tanya.append(",?");
            }
            try (PreparedStatement pstm = koneksi.prepareStatement(
                    "insert into asesmen_medis_ranap_rm20_1(" + kol + ") values(" + tanya + ")")) {
                pstm.setString(1, TNoRw.getText());
                String[] nilai = nilaiRm20_1();
                for (int n = 0; n < nilai.length; n++) {
                    pstm.setString(n + 2, nilai[n]);
                }
                pstm.executeUpdate();

                String rawatSimpan = TNoRw.getText();
                if (chkLangsungTTE != null && chkLangsungTTE.isSelected()) {
                    String waktuSekarang = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    String kdDokter = KdDokterPeriksa.getText().trim().isEmpty() ? KdDokterDpjp.getText() : KdDokterPeriksa.getText();
                    Sequel.meghapus("asesmen_medis_ranap_rm20_1_konfirmasi", "no_rawat", rawatSimpan);
                    Sequel.menyimpantf("asesmen_medis_ranap_rm20_1_konfirmasi", "?,?,?,?", "Konfirmasi Asesmen Medis", 4, new String[]{
                        rawatSimpan, waktuSekarang, kdDokter, "Tervalidasi (TTE)"
                    });
                }

                emptTeks();
                tampil();
                JOptionPane.showMessageDialog(null, "Data Asesmen Medis Ranap (RM 20-1) berhasil disimpan"
                        + (chkLangsungTTE != null && chkLangsungTTE.isSelected() ? " dan langsung Divalidasi (TTE)!" : "!"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Gagal menyimpan : " + e.getMessage());
            }
        }
    }

    private void aksiGanti() {
        if (TNoRM.getText().trim().isEmpty()) {
            Valid.textKosong(TNoRw, "Nama Pasien");
        } else if (NmDokterPeriksa.getText().trim().isEmpty()) {
            Valid.textKosong(BtnDokterPeriksa, "Dokter Pemeriksa");
        } else if (NmDokterDpjp.getText().trim().isEmpty()) {
            Valid.textKosong(BtnDokterDpjp, "Dokter DPJP");
        } else if (DiagnosaMedis.getText().trim().isEmpty()) {
            Valid.textKosong(DiagnosaMedis, "Diagnosa Medis");
        } else {
            if (Sequel.cariInteger("select count(*) from asesmen_medis_ranap_rm20_1_konfirmasi where no_rawat=?", TNoRw.getText()) > 0) {
                JOptionPane.showMessageDialog(this,
                    "Asesmen Medis ini sudah berstatus TERVALIDASI (TTE) dan dikunci!\n"
                    + "Silakan klik 'Batal Validasi / Tarik TTE' di Tab Riwayat terlebih dahulu jika ingin mengedit.",
                    "Peringatan - Data Terkunci", JOptionPane.WARNING_MESSAGE);
                return;
            }

            StringBuilder set = new StringBuilder();
            for (int n = 0; n < KOLOM_RM20_1.length; n++) {
                if (n > 0) set.append(",");
                set.append("`").append(KOLOM_RM20_1[n]).append("`=?");
            }
            try (PreparedStatement pstm = koneksi.prepareStatement(
                    "update asesmen_medis_ranap_rm20_1 set " + set + " where no_rawat=?")) {
                String[] nilai = nilaiRm20_1();
                for (int n = 0; n < nilai.length; n++) {
                    pstm.setString(n + 1, nilai[n]);
                }
                pstm.setString(nilai.length + 1, TNoRw.getText());
                pstm.executeUpdate();
                emptTeks();
                tampil();
                JOptionPane.showMessageDialog(null, "Data Asesmen Medis Ranap (RM 20-1) berhasil diperbarui!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Gagal mengganti : " + e.getMessage());
            }
        }
    }

    private void aksiHapus() {
        int r = tbObat.getSelectedRow();
        if (r != -1) {
            String norwt = tbObat.getValueAt(r, 0).toString();
            if (Sequel.cariInteger("select count(*) from asesmen_medis_ranap_rm20_1_konfirmasi where no_rawat=?", norwt) > 0) {
                JOptionPane.showMessageDialog(this,
                    "Asesmen Medis ini sudah berstatus TERVALIDASI (TTE) dan dikunci!\n"
                    + "Silakan klik 'Batal Validasi / Tarik TTE' terlebih dahulu jika ingin menghapus.",
                    "Peringatan - Data Terkunci", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (JOptionPane.showConfirmDialog(null, "Yakin data asesmen medis (" + norwt + ") mau dihapus?", "Konfirmasi",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try (PreparedStatement pstm = koneksi.prepareStatement(
                        "delete from asesmen_medis_ranap_rm20_1 where no_rawat=?")) {
                    pstm.setString(1, norwt);
                    pstm.executeUpdate();
                    Sequel.meghapus("asesmen_medis_ranap_rm20_1_konfirmasi", "no_rawat", norwt);
                    emptTeks();
                    tampil();
                    if (LoadHTML != null) LoadHTML.setText("");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Gagal menghapus : " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Silahkan pilih data terlebih dahulu..!!");
        }
    }

    public void emptTeks() {
        TglAsuhan.setDate(new Date());
        CmbAnamnesis.setSelectedIndex(0);
        THubungan.setText("");
        THubungan.setEnabled(false);
        CmbAsalMasuk.setSelectedIndex(0);
        CmbCaraMasuk.setSelectedItem("Brankar");

        KeluhanUtama.setText("");
        RiwayatPenyakit.setText("");
        RiwayatPengobatan.setText("");
        AlergiObat.setText("");
        AlergiMakanan.setText("");
        RiwayatLain.setText("");
        rbEkonomiSedang.setSelected(true);

        rbKeadaanSedang.setSelected(true);
        rbSadarCompos.setSelected(true);
        GCS_E.setText("4");
        GCS_M.setText("6");
        GCS_V.setText("5");
        lblTotalGCS.setText("Total GCS: 15");

        TDSistol.setText("120");
        TDDiastol.setText("80");
        Nadi.setText("80");
        RR.setText("20");
        Suhu.setText("36.5");
        SPO2.setText("98");
        TB.setText("");
        BB.setText("");
        IMT.setText("");
        rbGiziBaik.setSelected(true);

        PemeriksaanPenunjang.setText("");

        for (RegioFisik rf : regios) {
            rf.rbNormal.setSelected(true);
            rf.txtKet.setText("");
            aturStatusFisik(rf, false);
        }

        if (kanvasLokalis != null) {
            kanvasLokalis.hapusCoretan();
        }

        DiagnosaMedis.setText("");
        DiagnosaBanding.setText("");
        AdvisTerapi.setText("");
        RencanaTindakan.setText("");

        lblSumberDataBadge.setText("Baru");
        lblSumberDataBadge.setToolTipText("Asesmen Medis Baru");
        lblSumberDataBadge.setBackground(new Color(0xE2, 0xFA, 0xEC));
        lblSumberDataBadge.setForeground(new Color(0x0C, 0x6E, 0x3E));

        hitungProgres();
    }

    /**
     * Memuat data pasien ke form dengan Adapter Multi-Sumber:
     * 1. Cek tabel `asesmen_medis_ranap_rm20_1` (prioritas utama).
     * 2. Cek tabel `asesmen_medis_ranap` (RM 20 ringkas).
     * 3. Cek tabel `penilaian_medis_ranap` (Asesmen Medis Umum lama).
     */
    private void muatDataPasien(String norwt) {
        emptTeks();
        isRawat();

        // 1. Cek tabel RM 20-1 baru
        try (PreparedStatement pstm = koneksi.prepareStatement(
                "select * from asesmen_medis_ranap_rm20_1 where no_rawat=?")) {
            pstm.setString(1, norwt);
            try (ResultSet rst = pstm.executeQuery()) {
                if (rst.next()) {
                    Valid.SetTgl2(TglAsuhan, rst.getString("tanggal"));
                    KdDokterPeriksa.setText(rst.getString("kd_dokter_periksa"));
                    NmDokterPeriksa.setText(Sequel.CariDokter(KdDokterPeriksa.getText()));
                    KdDokterDpjp.setText(rst.getString("kd_dokter_dpjp"));
                    NmDokterDpjp.setText(Sequel.CariDokter(KdDokterDpjp.getText()));

                    CmbAnamnesis.setSelectedItem(rst.getString("anamnesis"));
                    THubungan.setText(rst.getString("hubungan"));
                    THubungan.setEnabled("Aloanamnesis".equalsIgnoreCase(rst.getString("anamnesis")));

                    if (rst.getString("asal_masuk") != null) CmbAsalMasuk.setSelectedItem(rst.getString("asal_masuk"));
                    if (rst.getString("cara_masuk") != null) CmbCaraMasuk.setSelectedItem(rst.getString("cara_masuk"));

                    KeluhanUtama.setText(rst.getString("keluhan_utama"));
                    RiwayatPenyakit.setText(rst.getString("riwayat_penyakit"));
                    RiwayatPengobatan.setText(rst.getString("riwayat_pengobatan"));
                    AlergiObat.setText(rst.getString("alergi_obat"));
                    AlergiMakanan.setText(rst.getString("alergi_makanan"));
                    RiwayatLain.setText(rst.getString("riwayat_lain"));
                    pilihRadio(grupStatusEkonomi, rst.getString("status_ekonomi"));

                    pilihRadio(grupKeadaan, rst.getString("keadaan_umum"));
                    pilihRadio(grupKesadaran, rst.getString("kesadaran"));
                    GCS_E.setText(rst.getString("gcs_e"));
                    GCS_M.setText(rst.getString("gcs_m"));
                    GCS_V.setText(rst.getString("gcs_v"));

                    TDSistol.setText(rst.getString("td_sistol"));
                    TDDiastol.setText(rst.getString("td_diastol"));
                    Nadi.setText(rst.getString("nadi"));
                    RR.setText(rst.getString("rr"));
                    Suhu.setText(rst.getString("suhu"));
                    SPO2.setText(rst.getString("spo2"));
                    TB.setText(rst.getString("tb"));
                    BB.setText(rst.getString("bb"));
                    IMT.setText(rst.getString("imt"));
                    pilihRadio(grupStatusGizi, rst.getString("status_pediatrik"));

                    PemeriksaanPenunjang.setText(rst.getString("pemeriksaan_penunjang"));

                    for (RegioFisik rf : regios) {
                        pilihRadio(rf.grup, rst.getString("fisik_" + rf.kode));
                        rf.txtKet.setText(rst.getString("fisik_" + rf.kode + "_ket"));
                        aturStatusFisik(rf, rf.rbTidakNormal.isSelected());
                    }

                    if (kanvasLokalis != null) {
                        String b64 = rst.getString("lokalis_canvas");
                        if (b64 != null && !b64.trim().isEmpty()) {
                            kanvasLokalis.setCoretanBase64(b64);
                        } else {
                            kanvasLokalis.hapusCoretan();
                        }
                    }

                    DiagnosaMedis.setText(rst.getString("diagnosa_medis"));
                    DiagnosaBanding.setText(rst.getString("diagnosa_banding"));
                    AdvisTerapi.setText(rst.getString("advis_terapi"));
                    RencanaTindakan.setText(rst.getString("rencana_tindakan"));

                    lblSumberDataBadge.setText("Tersimpan");
                    lblSumberDataBadge.setToolTipText("Asesmen Medis Ranap RM 20-1 (Tersimpan)");
                    lblSumberDataBadge.setBackground(new Color(0xE2, 0xFA, 0xEC));
                    lblSumberDataBadge.setForeground(new Color(0x0C, 0x6E, 0x3E));
                    hitungProgres();
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("Notif load RM20-1 : " + e);
        }

        // 2. Cek tabel RM 20 (bila ada)
        try (PreparedStatement pstm = koneksi.prepareStatement(
                "select * from asesmen_medis_ranap where no_rawat=?")) {
            pstm.setString(1, norwt);
            try (ResultSet rst = pstm.executeQuery()) {
                if (rst.next()) {
                    Valid.SetTgl2(TglAsuhan, rst.getString("tanggal"));
                    KdDokterPeriksa.setText(rst.getString("kd_dokter_periksa"));
                    NmDokterPeriksa.setText(Sequel.CariDokter(KdDokterPeriksa.getText()));
                    KdDokterDpjp.setText(rst.getString("kd_dokter_dpjp"));
                    NmDokterDpjp.setText(Sequel.CariDokter(KdDokterDpjp.getText()));

                    KeluhanUtama.setText(rst.getString("keluhan_utama"));
                    RiwayatPenyakit.setText(rst.getString("riwayat_penyakit_pengobatan"));
                    AlergiObat.setText(rst.getString("alergi"));

                    pilihRadio(grupKeadaan, rst.getString("keadaan_umum"));
                    pilihRadio(grupKesadaran, rst.getString("kesadaran_mental"));
                    GCS_E.setText(rst.getString("gcs_e"));
                    GCS_M.setText(rst.getString("gcs_m"));
                    GCS_V.setText(rst.getString("gcs_v"));
                    TDSistol.setText(rst.getString("td_sistol"));
                    TDDiastol.setText(rst.getString("td_diastol"));
                    Nadi.setText(rst.getString("nadi"));
                    RR.setText(rst.getString("rr"));
                    Suhu.setText(rst.getString("suhu"));
                    TB.setText(rst.getString("tb"));
                    BB.setText(rst.getString("bb"));
                    IMT.setText(rst.getString("imt"));
                    pilihRadio(grupStatusGizi, rst.getString("status_pediatrik"));

                    DiagnosaMedis.setText(rst.getString("diagnosa_kerja_1"));
                    DiagnosaBanding.setText(rst.getString("diagnosa_kerja_2"));
                    AdvisTerapi.setText(rst.getString("rencana_terapi"));

                    lblSumberDataBadge.setText("Pre-fill RM 20");
                    lblSumberDataBadge.setToolTipText("Pre-fill dari Penilaian Medis Ranap (RM 20)");
                    lblSumberDataBadge.setBackground(new Color(0xFD, 0xF3, 0xE7));
                    lblSumberDataBadge.setForeground(new Color(0x8A, 0x5A, 0x12));
                    hitungProgres();
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("Notif load RM20 : " + e);
        }

        // 3. Cek tabel lama Medis Umum (`penilaian_medis_ranap`)
        try (PreparedStatement pstm = koneksi.prepareStatement(
                "select * from penilaian_medis_ranap where no_rawat=?")) {
            pstm.setString(1, norwt);
            try (ResultSet rst = pstm.executeQuery()) {
                if (rst.next()) {
                    Valid.SetTgl2(TglAsuhan, rst.getString("tanggal"));
                    KdDokterPeriksa.setText(rst.getString("kd_dokter"));
                    NmDokterPeriksa.setText(Sequel.CariDokter(KdDokterPeriksa.getText()));

                    String ana = rst.getString("anamnesis");
                    if ("Alloanamnesis".equalsIgnoreCase(ana)) ana = "Aloanamnesis";
                    CmbAnamnesis.setSelectedItem(ana);
                    THubungan.setText(rst.getString("hubungan"));
                    THubungan.setEnabled("Aloanamnesis".equalsIgnoreCase(ana));

                    KeluhanUtama.setText(rst.getString("keluhan_utama"));

                    StringBuilder rp = new StringBuilder();
                    if (!rst.getString("rps").isEmpty()) rp.append("RPS: ").append(rst.getString("rps")).append("\n");
                    if (!rst.getString("rpd").isEmpty()) rp.append("RPD: ").append(rst.getString("rpd")).append("\n");
                    if (!rst.getString("rpk").isEmpty()) rp.append("RPK: ").append(rst.getString("rpk"));
                    RiwayatPenyakit.setText(rp.toString().trim());

                    RiwayatPengobatan.setText(rst.getString("rpo"));
                    AlergiObat.setText(rst.getString("alergi"));

                    pilihRadio(grupKeadaan, rst.getString("keadaan"));
                    pilihRadio(grupKesadaran, rst.getString("kesadaran"));

                    // Parse GCS
                    String gcsLama = rst.getString("gcs");
                    if (gcsLama != null) {
                        Matcher mE = Pattern.compile("E\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(gcsLama);
                        Matcher mM = Pattern.compile("M\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(gcsLama);
                        Matcher mV = Pattern.compile("V\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(gcsLama);
                        if (mE.find()) GCS_E.setText(mE.group(1));
                        if (mM.find()) GCS_M.setText(mM.group(1));
                        if (mV.find()) GCS_V.setText(mV.group(1));
                    }

                    // Parse TD
                    String tdLama = rst.getString("td");
                    if (tdLama != null && tdLama.contains("/")) {
                        String[] tdP = tdLama.split("/");
                        TDSistol.setText(tdP[0].replaceAll("[^0-9]", ""));
                        if (tdP.length > 1) TDDiastol.setText(tdP[1].replaceAll("[^0-9]", ""));
                    }

                    Nadi.setText(rst.getString("nadi"));
                    RR.setText(rst.getString("rr"));
                    Suhu.setText(rst.getString("suhu"));
                    SPO2.setText(rst.getString("spo"));
                    TB.setText(rst.getString("tb"));
                    BB.setText(rst.getString("bb"));

                    // Petakan 11 organ lama ke 14 organ baru
                    String ketFisik = rst.getString("ket_fisik");
                    petakanOrganLama(regios[0], rst.getString("kepala"), ketFisik);
                    petakanOrganLama(regios[1], rst.getString("mata"), ketFisik);
                    petakanOrganLama(regios[2], rst.getString("tht"), ketFisik);
                    petakanOrganLama(regios[4], rst.getString("gigi"), ketFisik);
                    petakanOrganLama(regios[5], rst.getString("jantung"), ketFisik);
                    petakanOrganLama(regios[6], rst.getString("thoraks"), ketFisik);
                    petakanOrganLama(regios[7], rst.getString("abdomen"), ketFisik);
                    petakanOrganLama(regios[8], rst.getString("kulit"), ketFisik);
                    petakanOrganLama(regios[9], rst.getString("ekstremitas"), ketFisik);
                    petakanOrganLama(regios[11], rst.getString("genital"), ketFisik);

                    // Penunjang
                    StringBuilder pj = new StringBuilder();
                    if (!rst.getString("lab").isEmpty()) pj.append("Lab: ").append(rst.getString("lab")).append("\n");
                    if (!rst.getString("rad").isEmpty()) pj.append("Rad: ").append(rst.getString("rad")).append("\n");
                    if (!rst.getString("penunjang").isEmpty()) pj.append(rst.getString("penunjang"));
                    PemeriksaanPenunjang.setText(pj.toString().trim());

                    DiagnosaMedis.setText(rst.getString("diagnosis"));
                    AdvisTerapi.setText(rst.getString("tata"));

                    lblSumberDataBadge.setText("Data Lama");
                    lblSumberDataBadge.setToolTipText("Data Tarikan Medis Umum (Lama)");
                    lblSumberDataBadge.setBackground(new Color(0xEA, 0xF1, 0xF7));
                    lblSumberDataBadge.setForeground(new Color(0x12, 0x4B, 0x78));
                    hitungProgres();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif load Medis Umum lama : " + e);
        }
    }

    private void petakanOrganLama(RegioFisik rf, String nilaiLama, String ketGlobal) {
        if ("Abnormal".equalsIgnoreCase(nilaiLama)) {
            rf.rbTidakNormal.setSelected(true);
            if (!ketGlobal.isEmpty()) rf.txtKet.setText(ketGlobal);
            aturStatusFisik(rf, true);
        } else {
            rf.rbNormal.setSelected(true);
            rf.txtKet.setText("");
            aturStatusFisik(rf, false);
        }
    }

    public void isRawat() {
        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi, "
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
                    if (lblValLokasi != null) {
                        String lok = lokasi.trim().isEmpty() ? "-" : lokasi;
                        lblValLokasi.setText(lok);
                        lblValLokasi.setToolTipText(lok);
                    }
                    sinkronKartuIdentitas();
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif isRawat RM20-1 : " + e);
        }
    }

    // =================================================================
    //  TAB DATA TERSIMPAN (UNION MULTI-SUMBER)
    // =================================================================

    private void rakitTabData() {
        internalFrame3 = new widget.InternalFrame();
        internalFrame3.setBorder(null);
        internalFrame3.setLayout(new BorderLayout());

        // Sisi Kiri: Tabel Daftar Riwayat Asesmen
        panelKiriData = new JPanel(new BorderLayout(6, 6));
        panelKiriData.setBackground(Color.WHITE);
        panelKiriData.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 4));

        JPanel panelHeaderKiri = new JPanel(new BorderLayout());
        panelHeaderKiri.setOpaque(false);
        panelHeaderKiri.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COL_BORDER),
            BorderFactory.createEmptyBorder(6, 8, 8, 8)
        ));
        JLabel lblTitleKiri = new JLabel("DAFTAR RIWAYAT ASESMEN RAWAT INAP");
        lblTitleKiri.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitleKiri.setForeground(COL_HEADER_DARK);
        panelHeaderKiri.add(lblTitleKiri, BorderLayout.WEST);
        panelKiriData.add(panelHeaderKiri, BorderLayout.NORTH);

        Scroll = new widget.ScrollPane();
        Scroll.setBorder(new RoundedBorder(COL_BORDER, 8, 1));

        tbObat = new widget.Table();
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbObat.setModel(tabMode);
        tbObat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbObat.setRowHeight(26);
        tbObat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tbObat.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        tbObat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = tbObat.getSelectedRow();
                updateStateButtonsTab2(r);
                if (r != -1) {
                    String noRawat = tbObat.getValueAt(r, 0).toString();
                    muatPreviewHTML(noRawat);
                } else {
                    if (LoadHTML != null) LoadHTML.setText("");
                }
            }
        });

        tbObat.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    if (tbObat.getSelectedRow() != -1) {
                        gantiDataAsesmen();
                    }
                }
            }
        });
        Scroll.setViewportView(tbObat);
        panelKiriData.add(Scroll, BorderLayout.CENTER);

        // Filter Bar Bawah (Tgl. Asuhan s.d., Keyword, Cari, Semua, Record)
        panelGlass9 = new widget.panelisi();
        panelGlass9.setPreferredSize(new Dimension(44, 46));
        panelGlass9.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 8));

        panelGlass9.add(new JLabel("Tgl. Asuhan :"));
        DTPCari1 = new widget.Tanggal();
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setPreferredSize(new Dimension(130, 28));
        gayaKotakInput(DTPCari1);
        panelGlass9.add(DTPCari1);

        panelGlass9.add(new JLabel("s.d."));
        DTPCari2 = new widget.Tanggal();
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setPreferredSize(new Dimension(130, 28));
        gayaKotakInput(DTPCari2);
        panelGlass9.add(DTPCari2);

        panelGlass9.add(Box.createHorizontalStrut(6));
        panelGlass9.add(new JLabel("Keyword :"));
        TCari = new widget.TextBox();
        TCari.setPreferredSize(new Dimension(160, 28));
        gayaKotakInput(TCari);
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                }
            }
        });
        panelGlass9.add(TCari);

        BtnCari = new widget.Button();
        BtnCari.setIcon(new ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setText("Cari");
        BtnCari.setPreferredSize(new Dimension(78, 28));
        BtnCari.putClientProperty("Component.arc", 8);
        BtnCari.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COL_INPUT_BORDER, 8, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        BtnCari.addActionListener(e -> tampil());
        panelGlass9.add(BtnCari);

        widget.Button btnSemua = new widget.Button();
        btnSemua.setIcon(new ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        btnSemua.setText("Semua");
        btnSemua.setPreferredSize(new Dimension(84, 28));
        btnSemua.putClientProperty("Component.arc", 8);
        btnSemua.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COL_INPUT_BORDER, 8, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        btnSemua.addActionListener(e -> {
            TCari.setText("");
            tampil();
        });
        panelGlass9.add(btnSemua);

        panelGlass9.add(Box.createHorizontalStrut(6));
        panelGlass9.add(new JLabel("Record :"));
        LCount = new widget.Label();
        LCount.setText("0");
        LCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        LCount.setForeground(COL_TEAL_DARK);
        panelGlass9.add(LCount);

        panelKiriData.add(panelGlass9, BorderLayout.SOUTH);

        // Sisi Kanan: Panel Pratinjau Dokumen HTML & Validasi TTE
        panelKananData = new JPanel(new BorderLayout(6, 6));
        panelKananData.setBackground(Color.WHITE);
        panelKananData.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 6));

        JPanel panelToolbarPreview = new JPanel(new BorderLayout(8, 0));
        panelToolbarPreview.setOpaque(false);
        panelToolbarPreview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COL_BORDER),
            BorderFactory.createEmptyBorder(6, 8, 8, 8)
        ));

        JLabel lblTitlePreview = new JLabel("PRATINJAU DOKUMEN (RM 20-1)");
        lblTitlePreview.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitlePreview.setForeground(COL_TEAL_DARK);
        panelToolbarPreview.add(lblTitlePreview, BorderLayout.WEST);

        JPanel pnlActionPreview = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlActionPreview.setOpaque(false);

        btnPopupPreview = new widget.Button();
        btnPopupPreview.setText("Jendela Layar Penuh");
        btnPopupPreview.setBackground(COL_TEAL_DARK);
        btnPopupPreview.setForeground(Color.WHITE);
        btnPopupPreview.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPopupPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPopupPreview.setPreferredSize(new Dimension(150, 28));
        btnPopupPreview.putClientProperty("Component.arc", 8);
        btnPopupPreview.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COL_TEAL_DARK, 8, 1),
                BorderFactory.createEmptyBorder(2, 10, 2, 10)));
        btnPopupPreview.setEnabled(false);
        btnPopupPreview.setToolTipText("Buka pratinjau dokumen di jendela modal terpisah layar penuh (Full Screen Preview)");
        btnPopupPreview.addActionListener(e -> {
            int r = tbObat.getSelectedRow();
            if (r != -1) {
                bukaDialogPreviewFullscreen(tbObat.getValueAt(r, 0).toString());
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih baris data asesmen yang ingin dilihat pratinjaunya!");
            }
        });
        pnlActionPreview.add(btnPopupPreview);
        panelToolbarPreview.add(pnlActionPreview, BorderLayout.EAST);

        panelKananData.add(panelToolbarPreview, BorderLayout.NORTH);

        LoadHTML = new widget.editorpane();
        LoadHTML.setContentType("text/html");
        LoadHTML.setEditable(false);
        LoadHTML.setBackground(Color.WHITE);
        if (LoadHTML.getCaret() instanceof javax.swing.text.DefaultCaret) {
            ((javax.swing.text.DefaultCaret) LoadHTML.getCaret()).setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
        }
        LoadHTML.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int r = tbObat.getSelectedRow();
                    if (r != -1) {
                        bukaDialogPreviewFullscreen(tbObat.getValueAt(r, 0).toString());
                    }
                }
            }
        });

        ScrollHTML = new widget.ScrollPane();
        ScrollHTML.setViewportView(LoadHTML);
        ScrollHTML.setBorder(new RoundedBorder(COL_BORDER, 8, 1));
        panelKananData.add(ScrollHTML, BorderLayout.CENTER);

        // Split Pane (Kiri & Kanan)
        JSplitPane splitRiwayat = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelKiriData, panelKananData);
        splitRiwayat.setResizeWeight(0.48);
        splitRiwayat.setDividerSize(6);
        splitRiwayat.setBorder(null);

        internalFrame3.add(splitRiwayat, BorderLayout.CENTER);
        TabRawat.addTab("Data Riwayat Asesmen", internalFrame3);
    }

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            String sql = "SELECT reg_periksa.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, "
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk, pasien.tgl_lahir, "
                    + "asesmen_medis_ranap_rm20_1.tanggal, asesmen_medis_ranap_rm20_1.kd_dokter_periksa, "
                    + "dokterperiksa.nm_dokter as dokter_periksa, dokterdpjp.nm_dokter as dokter_dpjp, "
                    + "'RM 20-1' as sumber, asesmen_medis_ranap_rm20_1.diagnosa_medis, asesmen_medis_ranap_rm20_1.diagnosa_banding, "
                    + "IF(k.no_rawat IS NULL, 'Belum Validasi (Draf)', 'Tervalidasi (TTE)') as status_validasi "
                    + "FROM reg_periksa "
                    + "INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "INNER JOIN asesmen_medis_ranap_rm20_1 ON reg_periksa.no_rawat=asesmen_medis_ranap_rm20_1.no_rawat "
                    + "LEFT JOIN dokter dokterperiksa ON asesmen_medis_ranap_rm20_1.kd_dokter_periksa=dokterperiksa.kd_dokter "
                    + "LEFT JOIN dokter dokterdpjp ON asesmen_medis_ranap_rm20_1.kd_dokter_dpjp=dokterdpjp.kd_dokter "
                    + "LEFT JOIN asesmen_medis_ranap_rm20_1_konfirmasi k ON reg_periksa.no_rawat=k.no_rawat "
                    + "WHERE asesmen_medis_ranap_rm20_1.tanggal between ? and ? ";
            if (!TCari.getText().trim().isEmpty()) {
                sql += "and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or dokterperiksa.nm_dokter like ?) ";
            }

            // UNION dengan data lama Medis Umum yang belum ada di RM 20-1
            sql += "UNION "
                    + "SELECT reg_periksa.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, "
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk, pasien.tgl_lahir, "
                    + "penilaian_medis_ranap.tanggal, penilaian_medis_ranap.kd_dokter, "
                    + "dokterlama.nm_dokter as dokter_periksa, '-' as dokter_dpjp, "
                    + "'Medis Umum (Lama)' as sumber, penilaian_medis_ranap.diagnosis as diagnosa_medis, '-' as diagnosa_banding, "
                    + "'Data Lama (Non-TTE)' as status_validasi "
                    + "FROM reg_periksa "
                    + "INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "INNER JOIN penilaian_medis_ranap ON reg_periksa.no_rawat=penilaian_medis_ranap.no_rawat "
                    + "LEFT JOIN dokter dokterlama ON penilaian_medis_ranap.kd_dokter=dokterlama.kd_dokter "
                    + "WHERE penilaian_medis_ranap.no_rawat NOT IN (SELECT no_rawat FROM asesmen_medis_ranap_rm20_1) "
                    + "AND penilaian_medis_ranap.tanggal between ? and ? ";
            if (!TCari.getText().trim().isEmpty()) {
                sql += "and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or dokterlama.nm_dokter like ?) ";
            }
            sql += "ORDER BY tanggal desc";

            try (PreparedStatement pstm = koneksi.prepareStatement(sql)) {
                String tgl1 = Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00";
                String tgl2 = Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59";
                pstm.setString(1, tgl1);
                pstm.setString(2, tgl2);
                int pIdx = 3;
                if (!TCari.getText().trim().isEmpty()) {
                    String param = "%" + TCari.getText().trim() + "%";
                    pstm.setString(pIdx++, param);
                    pstm.setString(pIdx++, param);
                    pstm.setString(pIdx++, param);
                    pstm.setString(pIdx++, param);
                }
                pstm.setString(pIdx++, tgl1);
                pstm.setString(pIdx++, tgl2);
                if (!TCari.getText().trim().isEmpty()) {
                    String param = "%" + TCari.getText().trim() + "%";
                    pstm.setString(pIdx++, param);
                    pstm.setString(pIdx++, param);
                    pstm.setString(pIdx++, param);
                    pstm.setString(pIdx++, param);
                }

                try (ResultSet rst = pstm.executeQuery()) {
                    while (rst.next()) {
                        tabMode.addRow(new Object[]{
                            rst.getString("no_rawat"),
                            rst.getString("no_rkm_medis"),
                            rst.getString("nm_pasien"),
                            rst.getString("tgl_lahir"),
                            rst.getString("jk"),
                            rst.getString("kd_dokter_periksa"),
                            rst.getString("dokter_periksa"),
                            rst.getString("dokter_dpjp"),
                            rst.getString("tanggal"),
                            rst.getString("sumber"),
                            rst.getString("diagnosa_medis"),
                            rst.getString("diagnosa_banding"),
                            rst.getString("status_validasi")
                        });
                    }
                }
            }

            // Atur lebar kolom tabel riwayat agar rapi dan tidak terpotong
            if (tbObat.getColumnModel().getColumnCount() >= 13) {
                tbObat.getColumnModel().getColumn(0).setPreferredWidth(115); // No.Rawat
                tbObat.getColumnModel().getColumn(1).setPreferredWidth(65);  // No.RM
                tbObat.getColumnModel().getColumn(2).setPreferredWidth(140); // Nama Pasien
                tbObat.getColumnModel().getColumn(3).setPreferredWidth(75);  // Tgl.Lahir
                tbObat.getColumnModel().getColumn(4).setPreferredWidth(60);  // J.K.
                tbObat.getColumnModel().getColumn(5).setPreferredWidth(80);  // Kode Dokter
                tbObat.getColumnModel().getColumn(6).setPreferredWidth(140); // Dokter Pemeriksa
                tbObat.getColumnModel().getColumn(7).setPreferredWidth(140); // DPJP
                tbObat.getColumnModel().getColumn(8).setPreferredWidth(115); // Tanggal
                tbObat.getColumnModel().getColumn(9).setPreferredWidth(85);  // Sumber Data
                tbObat.getColumnModel().getColumn(10).setPreferredWidth(140); // Diagnosa Medis
                tbObat.getColumnModel().getColumn(11).setPreferredWidth(120); // Diagnosa Banding
                tbObat.getColumnModel().getColumn(12).setPreferredWidth(120); // Status Validasi
            }

            LCount.setText("" + tabMode.getRowCount());
        } catch (Exception e) {
            System.out.println("Notif tampil RM20-1 : " + e);
        } finally {
            updateStateButtonsTab2(tbObat.getSelectedRow());
        }
    }

    private void getData() {
        if (tbObat.getSelectedRow() == -1) return;
        String norwt = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        TNoRw.setText(norwt);
        muatDataPasien(norwt);
        TabRawat.setSelectedIndex(0);
    }

    private void updateStateButtonsTab2(int r) {
        if (btnValidasiTTE == null) return;
        if (r == -1) {
            btnValidasiTTE.setEnabled(false);
            btnValidasiTTE.setText("Validasi & Bubuhkan TTE");
            btnValidasiTTE.setBackground(new Color(16, 185, 129));
            btnGantiData.setEnabled(false);
            btnHapusData.setEnabled(false);
            btnCetakDokumen.setEnabled(false);
            btnPopupPreview.setEnabled(false);
            return;
        }

        btnGantiData.setEnabled(true);
        btnHapusData.setEnabled(true);
        btnCetakDokumen.setEnabled(true);
        btnPopupPreview.setEnabled(true);

        String noRawat = tbObat.getValueAt(r, 0).toString();
        String sumber = String.valueOf(tbObat.getValueAt(r, 9));
        boolean isRM20_1 = "RM 20-1".equalsIgnoreCase(sumber);

        if (!isRM20_1) {
            btnValidasiTTE.setEnabled(false);
            btnValidasiTTE.setText("Data Lama (Non-TTE)");
            btnValidasiTTE.setBackground(new Color(148, 163, 184)); // Slate-400
            return;
        }

        btnValidasiTTE.setEnabled(true);
        boolean isTervalidasi = Sequel.cariInteger("select count(*) from asesmen_medis_ranap_rm20_1_konfirmasi where no_rawat=?", noRawat) > 0;
        if (isTervalidasi) {
            btnValidasiTTE.setText("Batal Validasi / Tarik TTE");
            btnValidasiTTE.setBackground(new Color(220, 38, 38)); // Red-600
        } else {
            btnValidasiTTE.setText("Validasi & Bubuhkan TTE");
            btnValidasiTTE.setBackground(new Color(16, 185, 129)); // Emerald-500
        }
    }

    private void gantiDataAsesmen() {
        int r = tbObat.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu baris data asesmen yang akan diedit!");
            return;
        }

        String noRawat = tbObat.getValueAt(r, 0).toString();
        boolean isTervalidasi = Sequel.cariInteger("select count(*) from asesmen_medis_ranap_rm20_1_konfirmasi where no_rawat=?", noRawat) > 0;
        if (isTervalidasi) {
            JOptionPane.showMessageDialog(this,
                "Asesmen Medis ini sudah berstatus TERVALIDASI (TTE) dan dikunci!\n"
                + "Silakan klik 'Batal Validasi / Tarik TTE' terlebih dahulu jika ingin mengedit.",
                "Peringatan - Data Terkunci", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TNoRw.setText(noRawat);
        muatDataPasien(noRawat);
        TabRawat.setSelectedIndex(0);
    }

    private void validasiAtauTarikTTE() {
        int r = tbObat.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih baris data asesmen yang akan diproses!");
            return;
        }

        String noRawat = tbObat.getValueAt(r, 0).toString();
        String sumber = String.valueOf(tbObat.getValueAt(r, 9));
        if (!"RM 20-1".equalsIgnoreCase(sumber)) {
            JOptionPane.showMessageDialog(this, "Data lama tidak mendukung mekanisme TTE digital RM 20-1.");
            return;
        }

        boolean isTervalidasi = Sequel.cariInteger("select count(*) from asesmen_medis_ranap_rm20_1_konfirmasi where no_rawat=?", noRawat) > 0;

        if (!isTervalidasi) {
            int konfirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin memvalidasi dan membubuhkan TTE pada Asesmen Medis ini?\n"
                + "Data yang sudah ber-TTE akan dikunci demi keamanan dan integritas rekam medis.",
                "Konfirmasi Validasi & TTE",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (konfirm == JOptionPane.YES_OPTION) {
                String waktuSekarang = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String kdDokter = (akses.getkode() == null || akses.getkode().trim().isEmpty() || akses.getkode().equals("Admin Utama"))
                        ? tbObat.getValueAt(r, 5).toString()
                        : akses.getkode();

                Sequel.meghapus("asesmen_medis_ranap_rm20_1_konfirmasi", "no_rawat", noRawat);
                if (Sequel.menyimpantf("asesmen_medis_ranap_rm20_1_konfirmasi", "?,?,?,?", "Konfirmasi Asesmen Medis", 4, new String[]{
                    noRawat, waktuSekarang, kdDokter, "Tervalidasi (TTE)"
                })) {
                    JOptionPane.showMessageDialog(this, "Asesmen Medis (" + noRawat + ") berhasil divalidasi dan TTE telah dibubuhkan!");
                    tampil();
                    pilihBarisAsesmen(noRawat);
                }
            }
        } else {
            int konfirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin MEMBATALKAN validasi dan MENARIK TTE dari dokumen ini?\n"
                + "Status dokumen akan kembali menjadi Draf dan kunci edit/hapus akan dibuka kembali.",
                "Konfirmasi Batal Validasi / Tarik TTE",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (konfirm == JOptionPane.YES_OPTION) {
                Sequel.meghapus("asesmen_medis_ranap_rm20_1_konfirmasi", "no_rawat", noRawat);
                JOptionPane.showMessageDialog(this, "TTE berhasil ditarik. Dokumen (" + noRawat + ") kembali menjadi Draf (Belum Validasi).");
                tampil();
                pilihBarisAsesmen(noRawat);
            }
        }
    }

    private void pilihBarisAsesmen(String noRawat) {
        if (noRawat == null || noRawat.isEmpty()) return;
        for (int i = 0; i < tabMode.getRowCount(); i++) {
            if (noRawat.equals(String.valueOf(tabMode.getValueAt(i, 0)))) {
                tbObat.setRowSelectionInterval(i, i);
                tbObat.scrollRectToVisible(tbObat.getCellRect(i, 0, true));
                updateStateButtonsTab2(i);
                muatPreviewHTML(noRawat);
                break;
            }
        }
    }

    private String generateQrCodeTTE(String content, String noRawat) {
        try {
            File dir = new File("temp");
            if (!dir.exists()) dir.mkdirs();
            String safeNo = noRawat.replaceAll("[^a-zA-Z0-9]", "_");
            File file = new File(dir, "qr_rm20_1_" + safeNo + ".png");
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 140, 140);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", file.toPath());
            return file.toURI().toURL().toString();
        } catch (Exception e) {
            System.out.println("Notif Generate QR TTE: " + e);
            return null;
        }
    }

    private void muatPreviewHTML(String noRawat) {
        if (LoadHTML == null) return;
        String content = generateHTMLPreviewContent(noRawat);
        LoadHTML.setText(content);
        LoadHTML.setCaretPosition(0);
        SwingUtilities.invokeLater(() -> {
            LoadHTML.setCaretPosition(0);
            if (ScrollHTML != null) {
                ScrollHTML.getVerticalScrollBar().setValue(0);
            }
        });
    }

    private void pasangMenuCetakOpsional(widget.Button btn) {
        JPopupMenu menuCetak = new JPopupMenu();

        JMenuItem itemLembar = new JMenuItem(" Cetak Lembar ke Printer");
        itemLembar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        itemLembar.addActionListener(e -> {
            int r = tbObat.getSelectedRow();
            if (r != -1) {
                cetakTerpilihDirect(tbObat.getValueAt(r, 0).toString(), "Lembar");
            }
        });

        JMenuItem itemPDF = new JMenuItem(" Ekspor / Simpan ke PDF");
        itemPDF.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        itemPDF.addActionListener(e -> {
            int r = tbObat.getSelectedRow();
            if (r != -1) {
                cetakTerpilihDirect(tbObat.getValueAt(r, 0).toString(), "PDF");
            }
        });

        menuCetak.add(itemLembar);
        menuCetak.add(itemPDF);

        btn.setComponentPopupMenu(menuCetak);
        btn.addActionListener(e -> {
            menuCetak.show(btn, 0, btn.getHeight());
        });
    }

    private void cetakTerpilihDirect(String noRawat, String mode) {
        String kdPeriksa = Sequel.cariIsi("select kd_dokter_periksa from asesmen_medis_ranap_rm20_1 where no_rawat=?", noRawat);
        String kdDpjp = Sequel.cariIsi("select kd_dokter_dpjp from asesmen_medis_ranap_rm20_1 where no_rawat=?", noRawat);
        String tgl = Sequel.cariIsi("select date_format(tanggal,'%d-%m-%Y %H:%i:%s') from asesmen_medis_ranap_rm20_1 where no_rawat=?", noRawat);

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        param.put("finger_periksa", tandaTangan(kdPeriksa, Sequel.CariDokter(kdPeriksa), tgl));
        param.put("finger_dpjp", tandaTangan(kdDpjp, Sequel.CariDokter(kdDpjp), tgl));

        String b64 = Sequel.cariIsi("select lokalis_canvas from asesmen_medis_ranap_rm20_1 where no_rawat=?", noRawat);
        param.put("lokalis_canvas_img", KanvasStatusLokalis.gambarCetakStream(b64));

        String qry = sqlCetak(noRawat);
        if ("PDF".equalsIgnoreCase(mode)) {
            Valid.MyReportqrypdf("rptAsesmenMedisRanapRM20_1.jasper", "report",
                    "::[ Asesmen Medis Rawat Inap (RM 20-1) ]::", qry, param);
        } else {
            Valid.MyReportqry("rptAsesmenMedisRanapRM20_1.jasper", "report",
                    "::[ Asesmen Medis Rawat Inap (RM 20-1) ]::", qry, param);
        }
    }

    private void bukaDialogPreviewFullscreen(String noRawat) {
        if (noRawat == null || noRawat.isEmpty()) {
            int r = tbObat.getSelectedRow();
            if (r != -1) {
                noRawat = tbObat.getValueAt(r, 0).toString();
            }
        }
        if (noRawat == null || noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih data asesmen terlebih dahulu!");
            return;
        }

        final String targetRawat = noRawat;
        String htmlDokumen = generateHTMLPreviewContent(targetRawat);
        if (htmlDokumen == null || htmlDokumen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gagal memuat pratinjau dokumen!");
            return;
        }

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parentWin, "::[ Pratinjau Dokumen Asesmen Medis Rawat Inap (RM 20-1) ]::", Dialog.ModalityType.APPLICATION_MODAL);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int dlgW = Math.min(1120, screen.width - 40);
        int dlgH = Math.min(860, screen.height - 60);
        dlg.setSize(dlgW, dlgH);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // Header Dialog Toolbar
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(new Color(15, 23, 42)); // Slate-900
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JLabel lblJudul = new JLabel("PRATINJAU DOKUMEN ASESMEN MEDIS RAWAT INAP (RM 20-1) — NO. RAWAT: " + targetRawat);
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblJudul.setForeground(Color.WHITE);

        JPanel pnlAksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlAksi.setOpaque(false);

        JButton btnCetakLembar = new JButton(" Cetak Lembar ke Printer");
        btnCetakLembar.setIcon(new ImageIcon(getClass().getResource("/picture/b_print.png")));
        btnCetakLembar.setBackground(new Color(16, 185, 129)); // Emerald
        btnCetakLembar.setForeground(Color.WHITE);
        btnCetakLembar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCetakLembar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCetakLembar.setToolTipText("Cetak dokumen langsung ke printer fisik");
        btnCetakLembar.addActionListener(e -> {
            cetakTerpilihDirect(targetRawat, "Lembar");
        });

        JButton btnSimpanPdf = new JButton(" Simpan PDF");
        btnSimpanPdf.setIcon(new ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        btnSimpanPdf.setBackground(new Color(217, 119, 6)); // Amber
        btnSimpanPdf.setForeground(Color.WHITE);
        btnSimpanPdf.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSimpanPdf.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpanPdf.setToolTipText("Simpan / ekspor dokumen sebagai file PDF");
        btnSimpanPdf.addActionListener(e -> {
            cetakTerpilihDirect(targetRawat, "PDF");
        });

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

        dlg.setVisible(true);
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim().replace("\n", "<br>");
    }

    private String generateHTMLPreviewContent(String noRawat) {
        StringBuilder html = new StringBuilder();
        try (PreparedStatement ps = koneksi.prepareStatement(
                "SELECT a.*, p.no_rkm_medis, p.nm_pasien, p.tgl_lahir, IF(p.jk='L','Laki-Laki','Perempuan') AS jk, "
                + "d1.nm_dokter AS nm_dokter_periksa, d2.nm_dokter AS nm_dokter_dpjp, "
                + "b.nm_bangsal, ki.kd_kamar, "
                + "k.tgl_validasi, k.kd_dokter AS kd_dokter_val, k.status_validasi "
                + "FROM asesmen_medis_ranap_rm20_1 a "
                + "INNER JOIN reg_periksa rp ON a.no_rawat = rp.no_rawat "
                + "INNER JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis "
                + "LEFT JOIN dokter d1 ON a.kd_dokter_periksa = d1.kd_dokter "
                + "LEFT JOIN dokter d2 ON a.kd_dokter_dpjp = d2.kd_dokter "
                + "LEFT JOIN kamar_inap ki ON a.no_rawat = ki.no_rawat AND ki.stts_pulang = '-' "
                + "LEFT JOIN kamar km ON ki.kd_kamar = km.kd_kamar "
                + "LEFT JOIN bangsal b ON km.kd_bangsal = b.kd_bangsal "
                + "LEFT JOIN asesmen_medis_ranap_rm20_1_konfirmasi k ON a.no_rawat = k.no_rawat "
                + "WHERE a.no_rawat = ?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String noRm = rs.getString("no_rkm_medis");
                    String nmPasien = rs.getString("nm_pasien");
                    String jk = rs.getString("jk");
                    String tglLahir = rs.getString("tgl_lahir");
                    String tglAsuhan = rs.getString("tanggal");
                    String nmPeriksa = rs.getString("nm_dokter_periksa");
                    String nmDpjp = rs.getString("nm_dokter_dpjp");
                    String kdPeriksa = rs.getString("kd_dokter_periksa");
                    String asalMasuk = rs.getString("asal_masuk");
                    String caraMasuk = rs.getString("cara_masuk");
                    String bangsal = rs.getString("nm_bangsal");
                    String kdKamar = rs.getString("kd_kamar");
                    String kamar = (bangsal != null ? bangsal : "") + (kdKamar != null ? " (" + kdKamar + ")" : "");
                    if (kamar.trim().isEmpty()) kamar = "-";

                    String tglVal = rs.getString("tgl_validasi");
                    boolean isVal = tglVal != null && !tglVal.isEmpty();

                    html.append("<html><body style='font-family:sans-serif; font-size:11px; color:#1e293b; padding:14px; background:#ffffff;'>");

                    // Kop Instansi RSUD Demang Sepulau Raya
                    html.append("<div align='center' style='border-bottom:2px solid #0f766e; padding-bottom:6px; margin-bottom:10px;'>")
                        .append("<b style='font-size:11px; color:#64748b;'>PEMERINTAH KABUPATEN LAMPUNG TENGAH</b><br>")
                        .append("<b style='font-size:14px; color:#0f766e;'>RSUD DEMANG SEPULAU RAYA</b><br>")
                        .append("<span style='font-size:9px; color:#64748b;'>Jl. Lintas Sumatera No. 4.A, Terbanggi Agung, Gunung Sugih, Lampung Tengah Kode Pos 34161</span>")
                        .append("</div>");

                    // Judul Formulir
                    html.append("<table width='100%' cellpadding='0' cellspacing='0' style='margin-bottom:8px;'><tr>")
                        .append("<td width='20%'>&nbsp;</td>")
                        .append("<td width='60%' align='center'><b style='font-size:13px; color:#0f172a; letter-spacing:1px;'>ASESMEN MEDIS RAWAT INAP</b></td>")
                        .append("<td width='20%' align='right' valign='top'><b style='font-size:11px; color:#0f766e;'>RM 20-1</b><br><span style='font-size:8.5px; color:#64748b;'>No: ").append(noRawat).append("</span></td>")
                        .append("</tr></table>");

                    // Identitas Pasien
                    html.append("<table width='100%' cellpadding='4' cellspacing='0' style='border:1px solid #cbd5e1; background:#f8fafc; margin-bottom:10px; font-size:10.5px;'>")
                        .append("<tr>")
                        .append("<td width='50%' valign='top'>")
                        .append("No. RM : <b>").append(noRm).append("</b><br>")
                        .append("Nama Pasien : <b>").append(nmPasien).append("</b> (").append(jk).append(")<br>")
                        .append("Tgl. Lahir : ").append(tglLahir != null ? tglLahir : "-").append("<br>")
                        .append("Ruangan / Kamar : <b>").append(kamar).append("</b>")
                        .append("</td>")
                        .append("<td width='50%' valign='top'>")
                        .append("No. Rawat : <b>").append(noRawat).append("</b><br>")
                        .append("Tgl. Asuhan : <b>").append(tglAsuhan != null ? tglAsuhan : "-").append("</b><br>")
                        .append("Asal / Cara Masuk : ").append(asalMasuk != null ? asalMasuk : "-").append(" / ").append(caraMasuk != null ? caraMasuk : "-").append("<br>")
                        .append("Dokter DPJP : <b>").append(nmDpjp != null ? nmDpjp : "-").append("</b>")
                        .append("</td>")
                        .append("</tr></table>");

                    // I. ANAMNESIS
                    html.append("<div style='background:#0f766e; color:#ffffff; padding:4px 8px; font-weight:bold; font-size:11px;'>I. ANAMNESIS</div>")
                        .append("<table width='100%' cellpadding='4' cellspacing='0' style='border:1px solid #e2e8f0; margin-bottom:10px; font-size:10.5px;'>")
                        .append("<tr><td width='25%' bgcolor='#f8fafc'><b>Keluhan Utama:</b></td><td>").append(safe(rs.getString("keluhan_utama"))).append("</td></tr>")
                        .append("<tr><td bgcolor='#f8fafc'><b>Riwayat Penyakit Sekarang:</b></td><td>").append(safe(rs.getString("riwayat_penyakit"))).append("</td></tr>")
                        .append("<tr><td bgcolor='#f8fafc'><b>Riwayat Pengobatan:</b></td><td>").append(safe(rs.getString("riwayat_pengobatan"))).append("</td></tr>")
                        .append("<tr><td bgcolor='#f8fafc'><b>Riwayat Alergi:</b></td><td>Obat: <b>").append(safe(rs.getString("alergi_obat"))).append("</b> | Makanan: <b>").append(safe(rs.getString("alergi_makanan"))).append("</b></td></tr>")
                        .append("<tr><td bgcolor='#f8fafc'><b>Riwayat Lain / Ekonomi:</b></td><td>").append(safe(rs.getString("riwayat_lain"))).append(" (Ekonomi: ").append(safe(rs.getString("status_ekonomi"))).append(")</td></tr>")
                        .append("</table>");

                    // II. PEMERIKSAAN FISIK UMUM
                    String gcsE = safe(rs.getString("gcs_e"));
                    String gcsM = safe(rs.getString("gcs_m"));
                    String gcsV = safe(rs.getString("gcs_v"));
                    String totalGcsStr = "";
                    try {
                        int eVal = Integer.parseInt(gcsE.trim());
                        int mVal = Integer.parseInt(gcsM.trim());
                        int vVal = Integer.parseInt(gcsV.trim());
                        totalGcsStr = " &nbsp;(Total GCS: " + (eVal + mVal + vVal) + ")";
                    } catch (Exception ignored) {}

                    html.append("<div style='background:#0f766e; color:#ffffff; padding:4px 8px; font-weight:bold; font-size:11px;'>II. PEMERIKSAAN FISIK UMUM</div>")
                        .append("<table width='100%' cellpadding='4' cellspacing='0' style='border:1px solid #cbd5e1; margin-bottom:10px; font-size:10.5px;'>")
                        .append("<tr>")
                        .append("<td width='22%' bgcolor='#f8fafc'><b>Keadaan Umum:</b></td>")
                        .append("<td width='28%'><b>").append(safe(rs.getString("keadaan_umum"))).append("</b></td>")
                        .append("<td width='22%' bgcolor='#f8fafc'><b>Kesadaran:</b></td>")
                        .append("<td width='28%'><b>").append(safe(rs.getString("kesadaran"))).append("</b></td>")
                        .append("</tr>")
                        .append("<tr>")
                        .append("<td bgcolor='#f8fafc'><b>Skala Koma Glasgow (GCS):</b></td>")
                        .append("<td colspan='3'><b>E: ").append(gcsE.isEmpty() ? "-" : gcsE)
                        .append(" | M: ").append(gcsM.isEmpty() ? "-" : gcsM)
                        .append(" | V: ").append(gcsV.isEmpty() ? "-" : gcsV).append("</b>")
                        .append(totalGcsStr)
                        .append("</td>")
                        .append("</tr>");

                    // TTV HANYA DITAMPILKAN JIKA BENAR-BENAR TERISI DATA RIIL (bukan kosong/strip/0)
                    String tdSis = rs.getString("td_sistol");
                    String tdDia = rs.getString("td_diastol");
                    String nadiVal = rs.getString("nadi");
                    String rrVal = rs.getString("rr");
                    String suhuVal = rs.getString("suhu");
                    String spo2Val = rs.getString("spo2");
                    boolean hasTtv = (tdSis != null && !tdSis.trim().isEmpty() && !tdSis.trim().equals("-") && !tdSis.trim().equals("0"))
                            || (nadiVal != null && !nadiVal.trim().isEmpty() && !nadiVal.trim().equals("-") && !nadiVal.trim().equals("0"))
                            || (suhuVal != null && !suhuVal.trim().isEmpty() && !suhuVal.trim().equals("-") && !suhuVal.trim().equals("0"));
                    if (hasTtv) {
                        html.append("<tr>")
                            .append("<td bgcolor='#f8fafc'><b>Tanda Vital (Riwayat):</b></td>")
                            .append("<td colspan='3'>");
                        if (tdSis != null && !tdSis.trim().isEmpty() && !tdSis.trim().equals("-") && !tdSis.trim().equals("0")) {
                            html.append("TD: <b>").append(tdSis).append("/").append(tdDia != null ? tdDia : "").append(" mmHg</b> &nbsp;|&nbsp; ");
                        }
                        if (nadiVal != null && !nadiVal.trim().isEmpty() && !nadiVal.trim().equals("-") && !nadiVal.trim().equals("0")) {
                            html.append("Nadi: <b>").append(nadiVal).append(" x/mnt</b> &nbsp;|&nbsp; ");
                        }
                        if (rrVal != null && !rrVal.trim().isEmpty() && !rrVal.trim().equals("-") && !rrVal.trim().equals("0")) {
                            html.append("RR: <b>").append(rrVal).append(" x/mnt</b> &nbsp;|&nbsp; ");
                        }
                        if (suhuVal != null && !suhuVal.trim().isEmpty() && !suhuVal.trim().equals("-") && !suhuVal.trim().equals("0")) {
                            html.append("Suhu: <b>").append(suhuVal).append(" °C</b> &nbsp;|&nbsp; ");
                        }
                        if (spo2Val != null && !spo2Val.trim().isEmpty() && !spo2Val.trim().equals("-") && !spo2Val.trim().equals("0")) {
                            html.append("SpO2: <b>").append(spo2Val).append(" %</b>");
                        }
                        html.append("</td></tr>");
                    }
                    html.append("</table>");

                    // III. PEMERIKSAAN FISIK SISTEMATIS
                    html.append("<div style='background:#0f766e; color:#ffffff; padding:4px 8px; font-weight:bold; font-size:11px;'>III. PEMERIKSAAN FISIK SISTEMATIS</div>")
                        .append("<table width='100%' cellpadding='4' cellspacing='0' border='1' style='border-collapse:collapse; border:1px solid #cbd5e1; margin-bottom:12px; font-size:10px;'>")
                        .append("<tr bgcolor='#e2e8f0'>")
                        .append("<td width='6%' align='center' style='border:1px solid #cbd5e1;'><b>No</b></td>")
                        .append("<td width='32%' align='left' style='border:1px solid #cbd5e1;'><b>Sistem Organ</b></td>")
                        .append("<td width='18%' align='center' style='border:1px solid #cbd5e1;'><b>Status</b></td>")
                        .append("<td width='44%' align='left' style='border:1px solid #cbd5e1;'><b>Keterangan Kelainan / Penjelasan</b></td>")
                        .append("</tr>");

                    for (int i = 0; i < regios.length; i++) {
                        String organKey = regios[i].kode;
                        String label = regios[i].labelTeks;
                        String stts = rs.getString("fisik_" + organKey);
                        String ket = rs.getString("fisik_" + organKey + "_ket");
                        boolean isTidakNormal = "Tidak Normal".equalsIgnoreCase(stts);
                        String bgRow = isTidakNormal ? "#fef2f2" : (i % 2 == 0 ? "#ffffff" : "#f8fafc");
                        String statusBadge = isTidakNormal
                                ? "<span style='color:#dc2626;'><b>Tidak Normal</b></span>"
                                : "<span style='color:#059669;'><b>Normal</b></span>";
                        String ketText = (ket != null && !ket.trim().isEmpty() && !ket.trim().equals("-"))
                                ? "<span style='color:#0f172a;'><b>" + safe(ket) + "</b></span>"
                                : "<span style='color:#94a3b8;'>-</span>";

                        html.append("<tr bgcolor='").append(bgRow).append("'>")
                            .append("<td width='6%' align='center' style='border:1px solid #cbd5e1;'>").append(i + 1).append("</td>")
                            .append("<td width='32%' align='left' style='border:1px solid #cbd5e1;'>").append(label).append("</td>")
                            .append("<td width='18%' align='center' style='border:1px solid #cbd5e1;'>").append(statusBadge).append("</td>")
                            .append("<td width='44%' align='left' style='border:1px solid #cbd5e1;'>").append(ketText).append("</td>")
                            .append("</tr>");
                    }
                    html.append("</table>");

                    // IV. STATUS LOKALIS & PENUNJANG
                    String penunjang = rs.getString("pemeriksaan_penunjang");
                    html.append("<div style='background:#0f766e; color:#ffffff; padding:4px 8px; font-weight:bold; font-size:11px;'>IV. STATUS LOKALIS & PENUNJANG</div>")
                        .append("<table width='100%' cellpadding='4' cellspacing='0' style='border:1px solid #e2e8f0; margin-bottom:10px; font-size:10.5px;'>")
                        .append("<tr><td width='25%' bgcolor='#f8fafc'><b>Pemeriksaan Penunjang:</b></td><td>").append(safe(penunjang)).append("</td></tr>")
                        .append("</table>");

                    // V. DIAGNOSIS
                    html.append("<div style='background:#0f766e; color:#ffffff; padding:4px 8px; font-weight:bold; font-size:11px;'>V. DIAGNOSIS</div>")
                        .append("<table width='100%' cellpadding='4' cellspacing='0' style='border:1px solid #e2e8f0; margin-bottom:10px; font-size:10.5px;'>")
                        .append("<tr><td width='25%' bgcolor='#f8fafc'><b>Diagnosis Medis:</b></td><td><b style='color:#0f172a;'>").append(safe(rs.getString("diagnosa_medis"))).append("</b></td></tr>")
                        .append("<tr><td bgcolor='#f8fafc'><b>Diagnosis Banding:</b></td><td>").append(safe(rs.getString("diagnosa_banding"))).append("</td></tr>")
                        .append("</table>");

                    // VI. PROGRAM & PERENCANAAN
                    html.append("<div style='background:#0f766e; color:#ffffff; padding:4px 8px; font-weight:bold; font-size:11px;'>VI. PROGRAM & PERENCANAAN</div>")
                        .append("<table width='100%' cellpadding='4' cellspacing='0' style='border:1px solid #e2e8f0; margin-bottom:14px; font-size:10.5px;'>")
                        .append("<tr><td width='25%' bgcolor='#f8fafc'><b>Advis Terapi:</b></td><td>").append(safe(rs.getString("advis_terapi"))).append("</td></tr>")
                        .append("<tr><td bgcolor='#f8fafc'><b>Rencana Tindakan:</b></td><td>").append(safe(rs.getString("rencana_tindakan"))).append("</td></tr>")
                        .append("</table>");

                    // TANDA TANGAN & VALIDASI TTE
                    html.append("<table width='100%' cellpadding='0' cellspacing='0' style='margin-top:10px;'>")
                        .append("<tr>")
                        .append("<td width='55%'>&nbsp;</td>")
                        .append("<td width='45%' align='center' style='font-size:10.5px;'>")
                        .append("Lampung Tengah, ").append(tglAsuhan != null && tglAsuhan.length() >= 10 ? tglAsuhan.substring(0, 10) : "-").append("<br>")
                        .append("<b>Dokter Pengkaji / DPJP</b><br><br>");

                    if (isVal) {
                        String fingerHash = Sequel.cariIsi(
                            "SELECT SHA1(sidikjari.sidikjari) FROM sidikjari "
                            + "INNER JOIN pegawai ON pegawai.id=sidikjari.id WHERE pegawai.nik=?", kdPeriksa
                        );
                        if (fingerHash == null || fingerHash.isEmpty()) fingerHash = kdPeriksa;

                        String qrContent = "Dikeluarkan di " + akses.getnamars() + ", Kabupaten " + akses.getkabupatenrs()
                                + "\nDokumen: Asesmen Medis Rawat Inap (RM 20-1)"
                                + "\nNo. Rawat: " + noRawat
                                + "\nPasien: " + nmPasien + " (RM: " + noRm + ")"
                                + "\nDitandatangani secara elektronik oleh:\n" + nmPeriksa + " (NIP/Kode: " + kdPeriksa + ")"
                                + "\nID TTE: " + fingerHash
                                + "\nWaktu Validasi: " + tglVal;
                        String qrUrl = generateQrCodeTTE(qrContent, noRawat);

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
                            .append(tglVal)
                            .append("</td></tr>")
                            .append("</table>");
                    } else {
                        html.append("<div style='border:1px dashed #94a3b8; background:#f8fafc; color:#64748b; padding:10px 8px; width:140px; margin:auto; text-align:center;'>")
                            .append("<b style='font-size:10px; color:#475569;'>[ DRAF ]</b><br>")
                            .append("<span style='font-size:8px; color:#94a3b8;'>Belum Divalidasi<br>Belum Ber-TTE</span>")
                            .append("</div>");
                    }

                    html.append("<br>")
                        .append("<u><b>").append(nmPeriksa != null ? nmPeriksa : "-").append("</b></u><br>")
                        .append("<span style='font-size:9px;'>Kode/NIP. ").append(kdPeriksa).append("</span>")
                        .append("</td></tr></table>");

                    html.append("<div style='margin-top:14px; border-top:1px solid #e2e8f0; padding-top:4px; font-size:8px; color:#94a3b8; text-align:right;'>")
                        .append("SIMRS Khanza RME - RSUD Demang Sepulau Raya")
                        .append("</div>");

                    html.append("</body></html>");
                    return html.toString();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Preview HTML: " + e);
        }

        return generateHTMLPreviewLama(noRawat);
    }

    private String generateHTMLPreviewLama(String noRawat) {
        StringBuilder html = new StringBuilder();
        try (PreparedStatement ps = koneksi.prepareStatement(
                "SELECT pm.*, p.no_rkm_medis, p.nm_pasien, p.tgl_lahir, IF(p.jk='L','Laki-Laki','Perempuan') AS jk, "
                + "d.nm_dokter AS nm_dokter_periksa, b.nm_bangsal, ki.kd_kamar "
                + "FROM penilaian_medis_ranap pm "
                + "INNER JOIN reg_periksa rp ON pm.no_rawat = rp.no_rawat "
                + "INNER JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis "
                + "LEFT JOIN dokter d ON pm.kd_dokter = d.kd_dokter "
                + "LEFT JOIN kamar_inap ki ON pm.no_rawat = ki.no_rawat AND ki.stts_pulang = '-' "
                + "LEFT JOIN kamar km ON ki.kd_kamar = km.kd_kamar "
                + "LEFT JOIN bangsal b ON km.kd_bangsal = b.kd_bangsal "
                + "WHERE pm.no_rawat = ?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String noRm = rs.getString("no_rkm_medis");
                    String nmPasien = rs.getString("nm_pasien");
                    String jk = rs.getString("jk");
                    String tglLahir = rs.getString("tgl_lahir");
                    String tglAsuhan = rs.getString("tanggal");
                    String nmPeriksa = rs.getString("nm_dokter_periksa");
                    String kdPeriksa = rs.getString("kd_dokter");
                    String bangsal = rs.getString("nm_bangsal");
                    String kdKamar = rs.getString("kd_kamar");
                    String kamar = (bangsal != null ? bangsal : "") + (kdKamar != null ? " (" + kdKamar + ")" : "");
                    if (kamar.trim().isEmpty()) kamar = "-";

                    html.append("<html><body style='font-family:sans-serif; font-size:11px; color:#1e293b; padding:14px; background:#ffffff;'>");

                    // Kop
                    html.append("<div align='center' style='border-bottom:2px solid #0f766e; padding-bottom:6px; margin-bottom:10px;'>")
                        .append("<b style='font-size:11px; color:#64748b;'>PEMERINTAH KABUPATEN LAMPUNG TENGAH</b><br>")
                        .append("<b style='font-size:14px; color:#0f766e;'>RSUD DEMANG SEPULAU RAYA</b><br>")
                        .append("<span style='font-size:9px; color:#64748b;'>Jl. Lintas Sumatera No. 4.A, Terbanggi Agung, Gunung Sugih, Lampung Tengah Kode Pos 34161</span>")
                        .append("</div>");

                    // Judul Form
                    html.append("<table width='100%' cellpadding='0' cellspacing='0' style='margin-bottom:8px;'><tr>")
                        .append("<td width='20%'>&nbsp;</td>")
                        .append("<td width='60%' align='center'><b style='font-size:13px; color:#0f172a; letter-spacing:1px;'>PENILAIAN MEDIS RAWAT INAP (ARSIP LAMA)</b></td>")
                        .append("<td width='20%' align='right' valign='top'><b style='font-size:11px; color:#64748b;'>ARSIP RM 20</b><br><span style='font-size:8.5px; color:#64748b;'>No: ").append(noRawat).append("</span></td>")
                        .append("</tr></table>");

                    // Identitas Pasien
                    html.append("<table width='100%' cellpadding='4' cellspacing='0' style='border:1px solid #cbd5e1; background:#f8fafc; margin-bottom:10px; font-size:10.5px;'>")
                        .append("<tr>")
                        .append("<td width='50%' valign='top'>")
                        .append("No. RM : <b>").append(noRm).append("</b><br>")
                        .append("Nama Pasien : <b>").append(nmPasien).append("</b> (").append(jk).append(")<br>")
                        .append("Tgl. Lahir : ").append(tglLahir != null ? tglLahir : "-").append("<br>")
                        .append("Ruangan / Kamar : <b>").append(kamar).append("</b>")
                        .append("</td>")
                        .append("<td width='50%' valign='top'>")
                        .append("No. Rawat : <b>").append(noRawat).append("</b><br>")
                        .append("Tgl. Asuhan : <b>").append(tglAsuhan != null ? tglAsuhan : "-").append("</b><br>")
                        .append("Dokter Pengkaji : <b>").append(nmPeriksa != null ? nmPeriksa : "-").append("</b>")
                        .append("</td>")
                        .append("</tr></table>");

                    // Anamnesis
                    html.append("<div style='background:#0f766e; color:#ffffff; padding:4px 8px; font-weight:bold; font-size:11px;'>I. ANAMNESIS</div>")
                        .append("<table width='100%' cellpadding='4' cellspacing='0' style='border:1px solid #e2e8f0; margin-bottom:10px; font-size:10.5px;'>")
                        .append("<tr><td width='25%' bgcolor='#f8fafc'><b>Keluhan Utama:</b></td><td>").append(safe(rs.getString("keluhan_utama"))).append("</td></tr>")
                        .append("<tr><td bgcolor='#f8fafc'><b>Riwayat Penyakit Sekarang:</b></td><td>").append(safe(rs.getString("rps"))).append("</td></tr>")
                        .append("<tr><td bgcolor='#f8fafc'><b>Riwayat Penyakit Dahulu:</b></td><td>").append(safe(rs.getString("rpd"))).append("</td></tr>")
                        .append("<tr><td bgcolor='#f8fafc'><b>Riwayat Pengobatan:</b></td><td>").append(safe(rs.getString("rpo"))).append("</td></tr>")
                        .append("<tr><td bgcolor='#f8fafc'><b>Alergi:</b></td><td>").append(safe(rs.getString("alergi"))).append("</td></tr>")
                        .append("</table>");

                    // II. PEMERIKSAAN FISIK
                    html.append("<div style='background:#0f766e; color:#ffffff; padding:4px 8px; font-weight:bold; font-size:11px;'>II. PEMERIKSAAN FISIK UMUM</div>")
                        .append("<table width='100%' cellpadding='4' cellspacing='0' style='border:1px solid #cbd5e1; margin-bottom:10px; font-size:10.5px;'>")
                        .append("<tr>")
                        .append("<td width='22%' bgcolor='#f8fafc'><b>Keadaan Umum:</b></td>")
                        .append("<td width='28%'><b>").append(safe(rs.getString("keadaan"))).append("</b></td>")
                        .append("<td width='22%' bgcolor='#f8fafc'><b>Kesadaran:</b></td>")
                        .append("<td width='28%'><b>").append(safe(rs.getString("kesadaran"))).append("</b></td>")
                        .append("</tr>")
                        .append("<tr>")
                        .append("<td bgcolor='#f8fafc'><b>Skala Koma Glasgow (GCS):</b></td>")
                        .append("<td colspan='3'><b>").append(safe(rs.getString("gcs"))).append("</b></td>")
                        .append("</tr>");

                    String oldTd = rs.getString("td");
                    String oldNadi = rs.getString("nadi");
                    String oldSuhu = rs.getString("suhu");
                    boolean hasOldTtv = (oldTd != null && !oldTd.trim().isEmpty() && !oldTd.trim().equals("-"))
                            || (oldNadi != null && !oldNadi.trim().isEmpty() && !oldNadi.trim().equals("-"))
                            || (oldSuhu != null && !oldSuhu.trim().isEmpty() && !oldSuhu.trim().equals("-"));
                    if (hasOldTtv) {
                        html.append("<tr>")
                            .append("<td bgcolor='#f8fafc'><b>Tanda Vital (Riwayat):</b></td>")
                            .append("<td colspan='3'>");
                        if (oldTd != null && !oldTd.trim().isEmpty() && !oldTd.trim().equals("-")) {
                            html.append("TD: <b>").append(oldTd).append(" mmHg</b> &nbsp;|&nbsp; ");
                        }
                        if (oldNadi != null && !oldNadi.trim().isEmpty() && !oldNadi.trim().equals("-")) {
                            html.append("Nadi: <b>").append(oldNadi).append(" x/mnt</b> &nbsp;|&nbsp; ");
                        }
                        if (oldSuhu != null && !oldSuhu.trim().isEmpty() && !oldSuhu.trim().equals("-")) {
                            html.append("Suhu: <b>").append(oldSuhu).append(" °C</b>");
                        }
                        html.append("</td></tr>");
                    }
                    html.append("</table>");

                    // Diagnosis & Terapi
                    html.append("<div style='background:#0f766e; color:#ffffff; padding:4px 8px; font-weight:bold; font-size:11px;'>III. DIAGNOSIS & TERAPI</div>")
                        .append("<table width='100%' cellpadding='4' cellspacing='0' style='border:1px solid #cbd5e1; margin-bottom:14px; font-size:10.5px;'>")
                        .append("<tr><td width='25%' bgcolor='#f8fafc'><b>Diagnosis:</b></td><td><b style='color:#0f172a;'>").append(safe(rs.getString("diagnosis"))).append("</b></td></tr>")
                        .append("<tr><td bgcolor='#f8fafc'><b>Tata Laksana:</b></td><td>").append(safe(rs.getString("tata"))).append("</td></tr>")
                        .append("</table>");

                    // TTD Dokter
                    html.append("<table width='100%' cellpadding='0' cellspacing='0' style='margin-top:10px;'>")
                        .append("<tr><td width='60%'>&nbsp;</td>")
                        .append("<td width='40%' align='center' style='font-size:10.5px;'>")
                        .append("Dokter Pengkaji<br><br><br><br>")
                        .append("<u><b>").append(nmPeriksa != null ? nmPeriksa : "-").append("</b></u><br>")
                        .append("<span style='font-size:9px;'>Kode/NIP. ").append(kdPeriksa).append("</span>")
                        .append("</td></tr></table>");

                    html.append("</body></html>");
                    return html.toString();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Preview Lama: " + e);
        }
        return "<html><body style='font-family:sans-serif; padding:20px; color:#64748b; text-align:center;'><h3>Tidak ada data untuk ditampilkan</h3></body></html>";
    }

    private void isMenu() {
        // Mode tanpa accordion
    }

    private void pasangBatasInput() {
        TDSistol.setDocument(new batasInput((byte) 4).getKata(TDSistol));
        TDDiastol.setDocument(new batasInput((byte) 4).getKata(TDDiastol));
        Nadi.setDocument(new batasInput((byte) 5).getKata(Nadi));
        RR.setDocument(new batasInput((byte) 5).getKata(RR));
        Suhu.setDocument(new batasInput((byte) 5).getKata(Suhu));
        SPO2.setDocument(new batasInput((byte) 5).getKata(SPO2));
        TB.setDocument(new batasInput((byte) 5).getKata(TB));
        BB.setDocument(new batasInput((byte) 5).getKata(BB));
        GCS_E.setDocument(new batasInput((byte) 2).getKata(GCS_E));
        GCS_M.setDocument(new batasInput((byte) 2).getKata(GCS_M));
        GCS_V.setDocument(new batasInput((byte) 2).getKata(GCS_V));
    }

    // =================================================================
    //  DIALOG PENCARIAN & RUJUKAN
    // =================================================================

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
                            NmDokterDpjp.setText(nm);
                            BtnDokterDpjp.requestFocus();
                        } else {
                            KdDokterPeriksa.setText(kd);
                            NmDokterPeriksa.setText(nm);
                            BtnDokterPeriksa.requestFocus();
                        }
                    }
                }
            });
        }
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }

    private void bukaCariPenyakit(boolean banding) {
        this.untukDiagnosaBanding = banding;
        if (penyakit == null) {
            penyakit = new DlgCariPenyakit(null, false);
            penyakit.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if (penyakit.getTable().getSelectedRow() != -1) {
                        String kd = penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(), 0).toString();
                        String nm = penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(), 1).toString();
                        String hasil = kd + " - " + nm;
                        if (untukDiagnosaBanding) {
                            String lama = DiagnosaBanding.getText().trim();
                            DiagnosaBanding.setText(lama.isEmpty() ? hasil : lama + "\n" + hasil);
                            DiagnosaBanding.requestFocus();
                        } else {
                            String lama = DiagnosaMedis.getText().trim();
                            DiagnosaMedis.setText(lama.isEmpty() ? hasil : lama + "\n" + hasil);
                            DiagnosaMedis.requestFocus();
                        }
                    }
                }
            });
        }
        penyakit.isCek();
        penyakit.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        penyakit.setLocationRelativeTo(internalFrame1);
        penyakit.setVisible(true);
    }

    private void pratinjauRekonsiliasiObat() {
        String norwt = TNoRw.getText().trim();
        if (norwt.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pilih data pasien terlebih dahulu!");
            return;
        }
        StringBuilder h = new StringBuilder();
        h.append("<html><body style='font-family:sans-serif; font-size:11px; padding:10px;'>")
                .append("<h3 style='color:#0D7A70; margin-bottom:4px;'>Rekonsiliasi Obat Pasien Admisi: ")
                .append(norwt).append("</h3>");

        boolean ada = false;
        try (PreparedStatement psR = koneksi.prepareStatement(
                "select * from rekonsiliasi_obat where no_rawat=? order by tanggal desc")) {
            psR.setString(1, norwt);
            try (ResultSet rsR = psR.executeQuery()) {
                while (rsR.next()) {
                    ada = true;
                    h.append("<div style='background:#f4f9f8; padding:6px; border-left:3px solid #0D7A70; margin-top:8px;'>")
                            .append("<b>Tanggal:</b> ").append(rsR.getString("tanggal"))
                            .append(" | <b>Tipe:</b> ").append(rsR.getString("tipe_rekonsiliasi"))
                            .append("</div><table border='1' cellspacing='0' cellpadding='4' width='100%' style='border-color:#ddd; margin-top:4px;'>")
                            .append("<tr bgcolor='#0D7A70' style='color:white;'>")
                            .append("<th>No</th><th>Nama Obat</th><th>Dosis</th><th>Frekuensi</th><th>Rute</th><th>Alasan</th></tr>");

                    try (PreparedStatement psDet = koneksi.prepareStatement(
                            "select * from rekonsiliasi_obat_detail where id_rekonsiliasi=?")) {
                        psDet.setString(1, rsR.getString("id_rekonsiliasi"));
                        try (ResultSet rsDet = psDet.executeQuery()) {
                            int no = 1;
                            while (rsDet.next()) {
                                h.append("<tr>")
                                        .append("<td align='center'>").append(no++).append("</td>")
                                        .append("<td><b>").append(rsDet.getString("nama_obat")).append("</b></td>")
                                        .append("<td>").append(rsDet.getString("dosis")).append("</td>")
                                        .append("<td>").append(rsDet.getString("frekuensi")).append("</td>")
                                        .append("<td>").append(rsDet.getString("rute_pemberian")).append("</td>")
                                        .append("<td>").append(rsDet.getString("alasan_minum_obat")).append("</td>")
                                        .append("</tr>");
                            }
                        }
                    }
                    h.append("</table>");
                }
            }
        } catch (Exception ex) {
            h.append("<p style='color:red;'>Gagal membaca data: ").append(ex.getMessage()).append("</p>");
        }

        if (!ada) {
            h.append("<p style='color:#888;'>Belum ada data Rekonsiliasi Obat farmasi untuk admisi rawat inap ini.</p>");
        }
        h.append("</body></html>");

        JDialog dlg = new JDialog(this, "Rekonsiliasi Obat Farmasi", true);
        JEditorPane ep = new JEditorPane("text/html", h.toString());
        ep.setEditable(false);
        dlg.getContentPane().add(new JScrollPane(ep));
        dlg.setSize(750, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // =================================================================
    //  CETAK REPORT JASPER (2 HALAMAN UTUH)
    // =================================================================

    private void cetakTerpilih() {
        if (tbObat.getSelectedRow() <= -1) {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
            return;
        }
        String[] pilihan = {"Lembar", "PDF"};
        Object mode = JOptionPane.showInputDialog(null, "Silahkan pilih keluaran..!!", "Cetak",
                JOptionPane.QUESTION_MESSAGE, null, pilihan, pilihan[0]);
        if (mode == null) return;

        String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        String kdPeriksa = tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString();

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        param.put("finger_periksa", tandaTangan(kdPeriksa, tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString(),
                tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString()));
        param.put("finger_dpjp", tandaTangan(
                Sequel.cariIsi("select kd_dokter_dpjp from asesmen_medis_ranap_rm20_1 where no_rawat=?", noRawat),
                tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString(),
                tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString()));

        // Stream Gambar Status Lokalis
        String b64 = Sequel.cariIsi("select lokalis_canvas from asesmen_medis_ranap_rm20_1 where no_rawat=?", noRawat);
        param.put("lokalis_canvas_img", KanvasStatusLokalis.gambarCetakStream(b64));

        String qry = sqlCetak(noRawat);
        if ("PDF".equals(mode)) {
            Valid.MyReportqrypdf("rptAsesmenMedisRanapRM20_1.jasper", "report",
                    "::[ Asesmen Medis Rawat Inap (RM 20-1) ]::", qry, param);
        } else {
            Valid.MyReportqry("rptAsesmenMedisRanapRM20_1.jasper", "report",
                    "::[ Asesmen Medis Rawat Inap (RM 20-1) ]::", qry, param);
        }
    }

    private String tandaTangan(String kdDokter, String namaDokter, String tanggal) {
        String finger = Sequel.cariIsi(
                "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id "
                + "where pegawai.nik=?", kdDokter);
        return "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                + "\nDitandatangani secara elektronik oleh " + namaDokter
                + "\nID " + (finger == null || finger.isEmpty() ? kdDokter : finger)
                + "\n" + Valid.SetTgl3(tanggal);
    }

    private String sqlCetak(String noRawat) {
        return "select asesmen_medis_ranap_rm20_1.*,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur_pasien,"
                + "reg_periksa.tgl_registrasi,reg_periksa.jam_reg,"
                + "dokterperiksa.nm_dokter as nm_dokter_periksa,dokterperiksa.no_ijn_praktek as ijin_periksa,"
                + "dokterdpjp.nm_dokter as nm_dokter_dpjp,dokterdpjp.no_ijn_praktek as ijin_dpjp,"
                + "coalesce((select bangsal.nm_bangsal from kamar_inap inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "
                + "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal where kamar_inap.no_rawat=asesmen_medis_ranap_rm20_1.no_rawat "
                + "order by kamar_inap.tgl_masuk desc limit 1),'-') as nm_bangsal "
                + "from asesmen_medis_ranap_rm20_1 "
                + "inner join reg_periksa on asesmen_medis_ranap_rm20_1.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join dokter dokterperiksa on asesmen_medis_ranap_rm20_1.kd_dokter_periksa=dokterperiksa.kd_dokter "
                + "left join dokter dokterdpjp on asesmen_medis_ranap_rm20_1.kd_dokter_dpjp=dokterdpjp.kd_dokter "
                + "where asesmen_medis_ranap_rm20_1.no_rawat='" + noRawat + "'";
    }

    // =================================================================
    //  API PUBLIK
    // =================================================================

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2 != null ? tgl2 : new Date());
        muatDataPasien(norwt);
    }

    public void setTampil() {
        TabRawat.setSelectedIndex(1);
        tampil();
        if (tbObat.getRowCount() > 0) {
            tbObat.setRowSelectionInterval(0, 0);
            updateStateButtonsTab2(0);
            muatPreviewHTML(tbObat.getValueAt(0, 0).toString());
        }
    }

    public void isCek() {
        BtnSimpan.setEnabled(akses.getasesmen_medis_ranap());
        BtnHapus.setEnabled(akses.getasesmen_medis_ranap());
        BtnEdit.setEnabled(akses.getasesmen_medis_ranap());
        if (akses.getjml2() >= 1) {
            KdDokterPeriksa.setEditable(false);
            BtnDokterPeriksa.setEnabled(false);
            KdDokterPeriksa.setText(akses.getkode());
            NmDokterPeriksa.setText(Sequel.CariDokter(KdDokterPeriksa.getText()));
            if (NmDokterPeriksa.getText().isEmpty()) {
                KdDokterPeriksa.setText("");
                JOptionPane.showMessageDialog(null, "User login bukan Dokter terdaftar!");
            }
        }
    }

    /**
     * Entry point mandiri untuk pengujian langsung (Shift+F6 di NetBeans).
     */
    public static void main(String[] args) {
        try {
            koneksiDB.condb();
            akses.setstatus(true);
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
            }

            RMAsesmenMedisRanap20_1 dialog = new RMAsesmenMedisRanap20_1(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) { System.exit(0); }
                @Override public void windowClosed(WindowEvent e) { System.exit(0); }
            });
            dialog.setLocationRelativeTo(null);
            dialog.isCek();
            dialog.BtnSimpan.setEnabled(true);
            dialog.BtnHapus.setEnabled(true);
            dialog.BtnEdit.setEnabled(true);

            // Auto-load pasien rawat inap terakhir saat preview mandiri (Shift+F6 di NetBeans)
            try {
                sekuel sq = new sekuel();
                String sampleNoRawat = sq.cariIsi(
                    "select kamar_inap.no_rawat from kamar_inap where kamar_inap.stts_pulang='-' order by kamar_inap.tgl_masuk desc limit 1");
                if (sampleNoRawat == null || sampleNoRawat.trim().isEmpty()) {
                    sampleNoRawat = sq.cariIsi(
                        "select kamar_inap.no_rawat from kamar_inap order by kamar_inap.tgl_masuk desc limit 1");
                }
                if (sampleNoRawat == null || sampleNoRawat.trim().isEmpty()) {
                    sampleNoRawat = sq.cariIsi(
                        "select reg_periksa.no_rawat from reg_periksa order by reg_periksa.tgl_registrasi desc limit 1");
                }
                if (sampleNoRawat != null && !sampleNoRawat.trim().isEmpty()) {
                    dialog.setNoRm(sampleNoRawat, new Date());
                }
            } catch (Exception ignored) {
            }

            dialog.setVisible(true);
        });
    }
}
