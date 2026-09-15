package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
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
import widget.ThemeConfig;

/**
 * ASESMEN MEDIS RAWAT INAP (RM 20-1) - RSUD Demang Sepulau Raya.
 *
 * <p>Form ini merupakan lembar lanjutan dari RM 20 (Asesmen Medis Rawat Inap).
 * Desain mengadopsi spesifikasi design handoff mockup (Asesmen Medis Rawat Inap RM20-1 Redesign.dc.html)
 * serta pola arsitektur panel kiri (rail navigasi, kartu isian wajib, bagian pengkajian, rujukan data)
 * yang selaras dengan Pengkajian Awal Keperawatan IGD.</p>
 */
public final class RMAsesmenMedisRanap20_1 extends javax.swing.JDialog {

    private static final String[] KOLOM_RM20_1 = {
        "tanggal", "kd_dokter_periksa", "kd_dokter_dpjp", "anamnesis", "hubungan",
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

    private static final class RadioPill extends JPanel {
        private final widget.RadioButton rb;
        private final JLabel lblIkon;
        private final JLabel lblTeks;

        public RadioPill(widget.RadioButton rb, String text) {
            this.rb = rb;
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 6));
            setOpaque(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(text.length() > 8 ? 116 : 88, 30));

            lblIkon = new JLabel();
            lblTeks = new JLabel(text);
            lblTeks.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            add(lblIkon);
            add(lblTeks);

            perbarui();

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    rb.setSelected(true);
                }
            });

            rb.addItemListener(e -> perbarui());
        }

        public void perbarui() {
            boolean on = rb.isSelected();
            if (on) {
                setBackground(new Color(0xF1, 0xF7, 0xF6));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0x0D, 0x7A, 0x70), 1),
                        BorderFactory.createEmptyBorder(0, 4, 0, 4)));
                lblTeks.setForeground(new Color(0x1C, 0x1F, 0x1D));
                lblIkon.setIcon(ICON_RADIO_ON);
            } else {
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xCF, 0xD3, 0xCE), 1),
                        BorderFactory.createEmptyBorder(0, 4, 0, 4)));
                lblTeks.setForeground(new Color(0x3D, 0x44, 0x41));
                lblIkon.setIcon(ICON_RADIO_OFF);
            }
            repaint();
        }
    }

    private static final class RegioFisik {
        final String kode;
        final String labelTeks;
        widget.RadioButton rbNormal;
        widget.RadioButton rbTidakNormal;
        ButtonGroup grup;
        RadioPill pillNormal;
        RadioPill pillTidakNormal;
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
    private int i = 0;
    private DlgCariDokter dokter;
    private boolean pilihDpjp = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;

    // ---------- Kerangka Utama ----------
    private widget.InternalFrame internalFrame1, internalFrame2, internalFrame3;
    private JPanel topBarGelap;
    private widget.PanelBiasa barPasienMedis, railMedis;
    private widget.ScrollPane scrollRailMedis, scrollSeksiMedis;
    private widget.panelisi panelGlass8, panelGlass9;
    private javax.swing.JTabbedPane TabRawat;
    private widget.PanelBiasa FormInput;
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnPrint, BtnAll, BtnKeluar;

    // ---------- Identitas Pasien (Display Card & Hidden Fields) ----------
    private widget.TextBox TNoRw, TNoRM, TPasien, TglLahir, TUmur, Jk;
    private widget.Label lblKartuNama, lblKartuInisial, lblKartuInfo;
    private JLabel lblValRM, lblValRawat, lblValLokasi;

    // ---------- Header Inputs ----------
    private widget.Tanggal TglAsuhan;
    private widget.ComboBox CmbAnamnesis;
    private widget.TextBox THubungan, TAsalMasuk, TCaraRuangan;
    private widget.TextBox KdDokterPeriksa, NmDokterPeriksa, KdDokterDpjp, NmDokterDpjp;
    private widget.Button BtnDokterPeriksa, BtnDokterDpjp;

    // ---------- Panel Kiri (Rail) ----------
    private widget.Label lblProgresPersen, lblProgresHitung, lblRailTally1, lblRailTally2, lblFooterNote;
    private JProgressBar pbProgres;
    private widget.PanelBiasa barisRail1, barisRail2;
    private widget.Button btnToggleMode;
    private boolean satuHalaman = true;
    private int halamanAktif = 0;

    // ---------- Halaman Seksi ----------
    private widget.PanelBiasa kartuBagian1, kartuBagian2;
    private widget.Label lblStatusSeksi1, lblStatusSeksi2;
    private widget.Button btnTidakRelevan1, btnTidakRelevan2;
    private boolean tidakRelevan1 = false;
    private boolean tidakRelevan2 = false;

    // ---------- Bagian I: Pemeriksaan Fisik ----------
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

    // ---------- Bagian II: Asesmen & Program ----------
    private widget.TextArea DiagnosaMedis, DiagnosaBanding;
    private widget.TextArea AdvisTerapi, RencanaTindakan;

    // ---------- Tab Data Tersimpan ----------
    private widget.ScrollPane Scroll, ScrollHTML;
    private widget.Table tbObat;
    private widget.Tanggal DTPCari1, DTPCari2;
    private widget.TextBox TCari;
    private widget.Button BtnCari, BtnPrint1;
    private widget.Label LCount;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JMenuItem MnAsesmenMedis;
    private widget.PanelBiasa PanelAccor;
    private widget.panelisi FormMenu;
    private widget.CekBox ChkAccor;
    private widget.editorpane LoadHTML;

    public RMAsesmenMedisRanap20_1(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.RM", "Nama Pasien", "Tgl.Lahir", "J.K.",
            "Kode Dokter", "Dokter Pemeriksa", "DPJP", "Tanggal", "Anamnesis",
            "Diagnosa Medis", "Diagnosa Banding"
        }) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
        };

        rakitKerangka();
        rakitHeaderDanBanner();
        rakitRailNavigasi();
        rakitHalamanSeksi();
        rakitTabData();
        pasangBatasInput();

        widget.IconStyle.pasang(this);

        ChkAccor.setSelected(false);
        isMenu();
        setSize(1320, 780);
        hitungProgres();
        sorotHalaman(0);
        terapkanModeHalaman();
    }

    // =================================================================
    //  KERANGKA UTAMA
    // =================================================================

    private void rakitKerangka() {
        internalFrame1 = new widget.InternalFrame();
        internalFrame1.setBorder(BorderFactory.createLineBorder(new Color(0xD8, 0xDB, 0xD6)));
        internalFrame1.setLayout(new BorderLayout(0, 0));

        // 1. Top Bar Gelap (Tinggi 34px) tepat di NORTH window paling atas
        topBarGelap = new JPanel(new BorderLayout());
        topBarGelap.setPreferredSize(new Dimension(10, 34));
        topBarGelap.setBackground(new Color(0x12, 0x3C, 0x3A));
        topBarGelap.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JPanel pnlJudulTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlJudulTop.setOpaque(false);
        JLabel lblTopTitle = new JLabel("Asesmen Medis Rawat Inap");
        lblTopTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTopTitle.setForeground(new Color(0xDF, 0xE9, 0xE6));

        JLabel lblTopBadge = new JLabel("RM 20-1");
        lblTopBadge.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblTopBadge.setForeground(new Color(0x8F, 0xB5, 0xAE));
        lblTopBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2C, 0x5A, 0x56)),
                BorderFactory.createEmptyBorder(1, 6, 1, 6)));

        pnlJudulTop.add(lblTopTitle);
        pnlJudulTop.add(lblTopBadge);

        JLabel lblTopRs = new JLabel("RSUD Demang Sepulau Raya");
        lblTopRs.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTopRs.setForeground(new Color(0x8F, 0xB5, 0xAE));

        topBarGelap.add(pnlJudulTop, BorderLayout.WEST);
        topBarGelap.add(lblTopRs, BorderLayout.EAST);
        internalFrame1.add(topBarGelap, BorderLayout.NORTH);

        // 2. Footer Aksi Bawah (Standard Khanza Sesuai Permintaan Pengguna)
        panelGlass8 = new widget.panelisi();
        panelGlass8.setName("panelGlass8");
        panelGlass8.setPreferredSize(new Dimension(44, 54));
        panelGlass8.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        BtnSimpan = new widget.Button();
        BtnSimpan.setIcon(new ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setPreferredSize(new Dimension(100, 30));
        BtnSimpan.addActionListener(evt -> simpan());
        panelGlass8.add(BtnSimpan);

        BtnBatal = new widget.Button();
        BtnBatal.setIcon(new ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png")));
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setPreferredSize(new Dimension(100, 30));
        BtnBatal.addActionListener(evt -> emptTeks());
        panelGlass8.add(BtnBatal);

        BtnHapus = new widget.Button();
        BtnHapus.setIcon(new ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setPreferredSize(new Dimension(100, 30));
        BtnHapus.addActionListener(evt -> aksiHapus());
        panelGlass8.add(BtnHapus);

        BtnEdit = new widget.Button();
        BtnEdit.setIcon(new ImageIcon(getClass().getResource("/picture/inventaris.png")));
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setPreferredSize(new Dimension(100, 30));
        BtnEdit.addActionListener(evt -> aksiGanti());
        panelGlass8.add(BtnEdit);

        BtnPrint = new widget.Button();
        BtnPrint.setIcon(new ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setPreferredSize(new Dimension(100, 30));
        BtnPrint.addActionListener(evt -> cetakTerpilih());
        panelGlass8.add(BtnPrint);

        BtnAll = new widget.Button();
        BtnAll.setIcon(new ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setPreferredSize(new Dimension(100, 30));
        BtnAll.addActionListener(evt -> {
            TCari.setText("");
            tampil();
        });
        panelGlass8.add(BtnAll);

        BtnKeluar = new widget.Button();
        BtnKeluar.setIcon(new ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setPreferredSize(new Dimension(100, 30));
        BtnKeluar.addActionListener(evt -> dispose());
        panelGlass8.add(BtnKeluar);

        internalFrame1.add(panelGlass8, BorderLayout.PAGE_END);

        // 3. Tab Container (Formulir Pengkajian & Data Tersimpan)
        TabRawat = new javax.swing.JTabbedPane();
        TabRawat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        TabRawat.setBackground(Color.WHITE);
        TabRawat.putClientProperty("JTabbedPane.tabType", "underlined");
        TabRawat.putClientProperty("JTabbedPane.showContentSeparator", Boolean.TRUE);
        TabRawat.putClientProperty("JTabbedPane.tabHeight", 34);
        TabRawat.setBorder(null);
        TabRawat.setForeground(new Color(0x6C, 0x73, 0x6E));

        internalFrame2 = new widget.InternalFrame();
        internalFrame2.setBorder(null);
        internalFrame2.setLayout(new BorderLayout());

        TabRawat.addTab("Formulir Pengkajian", internalFrame2);
        internalFrame1.add(TabRawat, BorderLayout.CENTER);
        getContentPane().add(internalFrame1, BorderLayout.CENTER);
    }

    private widget.Button tombolCardFooter(String teks, String ikon, char mnemonic, int lebar, Color warnaTeks, boolean isPrimary) {
        widget.Button b = new widget.Button();
        try {
            b.setIcon(new ImageIcon(getClass().getResource(ikon)));
        } catch (Exception ignored) {
        }
        b.setMnemonic(mnemonic);
        b.setText(teks);
        b.setToolTipText("Alt+" + mnemonic);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setRoundRect(false);
        b.putClientProperty("Component.arc", 2);
        b.putClientProperty("Button.arc", 2);
        b.putClientProperty("JButton.buttonType", "square");
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setPreferredSize(new Dimension(lebar, 34));

        if (isPrimary) {
            b.setBackground(ThemeConfig.PRIMARY);
            b.setGlassColor(ThemeConfig.PRIMARY);
            b.setForeground(Color.WHITE);
            b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        } else {
            b.setBackground(Color.WHITE);
            b.setGlassColor(Color.WHITE);
            b.setForeground(warnaTeks != null ? warnaTeks : new Color(0x3D, 0x44, 0x41));
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xCF, 0xD3, 0xCE)),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        }
        return b;
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
    //  SUBHEADER BAR PASIEN & GRID INPUT
    // =================================================================

    private void rakitHeaderDanBanner() {
        barPasienMedis = new widget.PanelBiasa();
        barPasienMedis.setBackground(Color.WHITE);
        barPasienMedis.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xCF, 0xD3, 0xCE)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        barPasienMedis.setLayout(new GridBagLayout());

        // Field Tersembunyi (Sumber Data)
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

        // 1. Kartu Identitas Pasien (Kiri, lebar 262px)
        widget.PanelBiasa kartuId = new widget.PanelBiasa();
        kartuId.setBackground(new Color(0xF7, 0xF8, 0xF5));
        kartuId.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0, 0xE2, 0xDD)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        kartuId.setLayout(new GridBagLayout());
        kartuId.setPreferredSize(new Dimension(262, 140));

        JLabel lblHdrId = new JLabel("IDENTITAS PASIEN");
        lblHdrId.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHdrId.setForeground(new Color(0x8B, 0x92, 0x8D));

        JLabel lblBadgeTerkunci = new JLabel("TERKUNCI");
        lblBadgeTerkunci.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblBadgeTerkunci.setForeground(new Color(0x7B, 0x82, 0x7D));
        lblBadgeTerkunci.setOpaque(true);
        lblBadgeTerkunci.setBackground(new Color(0xE8, 0xEA, 0xE5));
        lblBadgeTerkunci.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JPanel pnlAtasId = new JPanel(new BorderLayout());
        pnlAtasId.setOpaque(false);
        pnlAtasId.add(lblHdrId, BorderLayout.WEST);
        pnlAtasId.add(lblBadgeTerkunci, BorderLayout.EAST);

        JPanel pnlAvatarNama = new JPanel(new BorderLayout(10, 0));
        pnlAvatarNama.setOpaque(false);

        lblKartuInisial = new widget.Label();
        lblKartuInisial.setHorizontalAlignment(SwingConstants.CENTER);
        lblKartuInisial.setOpaque(true);
        lblKartuInisial.setBackground(new Color(0xEA, 0xF1, 0xF7));
        lblKartuInisial.setForeground(ThemeConfig.PRIMARY);
        lblKartuInisial.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKartuInisial.setPreferredSize(new Dimension(42, 42));
        lblKartuInisial.setBorder(BorderFactory.createLineBorder(new Color(0x0D, 0x7A, 0x70, 0x40)));

        JPanel pnlNamaDetail = new JPanel();
        pnlNamaDetail.setOpaque(false);
        pnlNamaDetail.setLayout(new javax.swing.BoxLayout(pnlNamaDetail, javax.swing.BoxLayout.Y_AXIS));

        lblKartuNama = new widget.Label();
        lblKartuNama.setHorizontalAlignment(SwingConstants.LEFT);
        lblKartuNama.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKartuNama.setForeground(ThemeConfig.TEXT);

        lblKartuInfo = new widget.Label();
        lblKartuInfo.setHorizontalAlignment(SwingConstants.LEFT);
        lblKartuInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblKartuInfo.setForeground(new Color(0x6C, 0x73, 0x6E));

        pnlNamaDetail.add(lblKartuNama);
        pnlNamaDetail.add(Box.createVerticalStrut(2));
        pnlNamaDetail.add(lblKartuInfo);

        pnlAvatarNama.add(lblKartuInisial, BorderLayout.WEST);
        pnlAvatarNama.add(pnlNamaDetail, BorderLayout.CENTER);

        JPanel pnlNomorDetail = new JPanel(new java.awt.GridLayout(3, 1, 0, 2));
        pnlNomorDetail.setOpaque(false);
        pnlNomorDetail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE4, 0xE6, 0xE2)),
                BorderFactory.createEmptyBorder(6, 0, 0, 0)));

        lblValRM = new JLabel("—");
        lblValRawat = new JLabel("—");
        lblValLokasi = new JLabel("Mawar 3B");

        pnlNomorDetail.add(buatBarisNomorPanel("No. RM", lblValRM));
        pnlNomorDetail.add(buatBarisNomorPanel("No. Rawat", lblValRawat));
        pnlNomorDetail.add(buatBarisNomorPanel("Lokasi", lblValLokasi));

        GridBagConstraints kc = new GridBagConstraints();
        kc.gridx = 0;
        kc.gridy = 0;
        kc.fill = GridBagConstraints.HORIZONTAL;
        kc.weightx = 1.0;
        kartuId.add(pnlAtasId, kc);

        kc.gridy = 1;
        kc.insets = new Insets(8, 0, 8, 0);
        kartuId.add(pnlAvatarNama, kc);

        kc.gridy = 2;
        kc.insets = new Insets(0, 0, 0, 0);
        kartuId.add(pnlNomorDetail, kc);

        // 2. Grid Input Header (Kanan)
        JPanel gridHeader = new JPanel(new GridBagLayout());
        gridHeader.setOpaque(false);

        TglAsuhan = new widget.Tanggal();
        TglAsuhan.setDisplayFormat("dd-MM-yyyy HH:mm");
        TglAsuhan.setDate(new Date());
        TglAsuhan.setPreferredSize(new Dimension(145, 32));
        TglAsuhan.setBackground(Color.WHITE);
        gayaKotakInput(TglAsuhan);

        CmbAnamnesis = new widget.ComboBox();
        CmbAnamnesis.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Autoanamnesis", "Aloanamnesis"}));
        CmbAnamnesis.setPreferredSize(new Dimension(145, 32));
        CmbAnamnesis.setBackground(Color.WHITE);
        gayaKotakInput(CmbAnamnesis);

        THubungan = new widget.TextBox();
        THubungan.setPreferredSize(new Dimension(190, 32));
        THubungan.putClientProperty("JTextField.placeholderText", "Nama & hubungan dengan pasien");
        THubungan.setEnabled(false);
        gayaKotakInput(THubungan);

        CmbAnamnesis.addActionListener(evt -> {
            boolean isAllo = "Aloanamnesis".equals(CmbAnamnesis.getSelectedItem());
            THubungan.setEnabled(isAllo);
            if (!isAllo) {
                THubungan.setText("");
            }
        });

        TAsalMasuk = new widget.TextBox();
        TAsalMasuk.setText("IGD");
        TAsalMasuk.setEditable(false);
        TAsalMasuk.setPreferredSize(new Dimension(80, 32));
        TAsalMasuk.setBackground(new Color(0xFB, 0xFC, 0xFA));
        gayaKotakInput(TAsalMasuk);

        JPanel pnlAsalMasuk = new JPanel(new BorderLayout());
        pnlAsalMasuk.putClientProperty("Component.arc", 0);
        pnlAsalMasuk.setBorder(BorderFactory.createLineBorder(new Color(0xCF, 0xD3, 0xCE), 1));
        pnlAsalMasuk.setBackground(new Color(0xFB, 0xFC, 0xFA));
        JLabel lblLock1 = new JLabel();
        try {
            ImageIcon iconLock = new ImageIcon(getClass().getResource("/picture/lock.png"));
            Image imgLock = iconLock.getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
            lblLock1.setIcon(new ImageIcon(imgLock));
        } catch (Exception ignored) {
        }
        TAsalMasuk.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        pnlAsalMasuk.add(TAsalMasuk, BorderLayout.CENTER);
        pnlAsalMasuk.add(lblLock1, BorderLayout.EAST);

        TCaraRuangan = new widget.TextBox();
        TCaraRuangan.setText("Brankar");
        TCaraRuangan.setEditable(false);
        TCaraRuangan.setPreferredSize(new Dimension(100, 32));
        TCaraRuangan.setBackground(new Color(0xFB, 0xFC, 0xFA));
        gayaKotakInput(TCaraRuangan);

        JPanel pnlCaraRuangan = new JPanel(new BorderLayout());
        pnlCaraRuangan.putClientProperty("Component.arc", 0);
        pnlCaraRuangan.setBorder(BorderFactory.createLineBorder(new Color(0xCF, 0xD3, 0xCE), 1));
        pnlCaraRuangan.setBackground(new Color(0xFB, 0xFC, 0xFA));
        JLabel lblLock2 = new JLabel();
        try {
            ImageIcon iconLock = new ImageIcon(getClass().getResource("/picture/lock.png"));
            Image imgLock = iconLock.getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
            lblLock2.setIcon(new ImageIcon(imgLock));
        } catch (Exception ignored) {
        }
        TCaraRuangan.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        pnlCaraRuangan.add(TCaraRuangan, BorderLayout.CENTER);
        pnlCaraRuangan.add(lblLock2, BorderLayout.EAST);

        NmDokterPeriksa = new widget.TextBox();
        NmDokterPeriksa.setPreferredSize(new Dimension(150, 32));
        NmDokterPeriksa.setEditable(false);
        NmDokterPeriksa.putClientProperty("JTextField.placeholderText", "Cari dokter...");
        gayaKotakInput(NmDokterPeriksa);

        BtnDokterPeriksa = new widget.Button();
        try {
            BtnDokterPeriksa.setIcon(new ImageIcon(getClass().getResource("/picture/190.png")));
        } catch (Exception ignored) {
            BtnDokterPeriksa.setText("...");
        }
        BtnDokterPeriksa.setPreferredSize(new Dimension(32, 32));
        BtnDokterPeriksa.setBackground(new Color(0xF4, 0xF5, 0xF2));
        BtnDokterPeriksa.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0xCF, 0xD3, 0xCE)));
        BtnDokterPeriksa.addActionListener(evt -> bukaCariDokter(false));

        JPanel pnlDokterPeriksa = new JPanel(new BorderLayout());
        pnlDokterPeriksa.putClientProperty("Component.arc", 0);
        pnlDokterPeriksa.setBorder(BorderFactory.createLineBorder(new Color(0xCF, 0xD3, 0xCE), 1));
        NmDokterPeriksa.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        pnlDokterPeriksa.add(NmDokterPeriksa, BorderLayout.CENTER);
        pnlDokterPeriksa.add(BtnDokterPeriksa, BorderLayout.EAST);

        NmDokterDpjp = new widget.TextBox();
        NmDokterDpjp.setPreferredSize(new Dimension(150, 32));
        NmDokterDpjp.setEditable(false);
        NmDokterDpjp.putClientProperty("JTextField.placeholderText", "Cari dokter...");
        gayaKotakInput(NmDokterDpjp);

        BtnDokterDpjp = new widget.Button();
        try {
            BtnDokterDpjp.setIcon(new ImageIcon(getClass().getResource("/picture/190.png")));
        } catch (Exception ignored) {
            BtnDokterDpjp.setText("...");
        }
        BtnDokterDpjp.setPreferredSize(new Dimension(32, 32));
        BtnDokterDpjp.setBackground(new Color(0xF4, 0xF5, 0xF2));
        BtnDokterDpjp.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0xCF, 0xD3, 0xCE)));
        BtnDokterDpjp.addActionListener(evt -> bukaCariDokter(true));

        JPanel pnlDokterDpjp = new JPanel(new BorderLayout());
        pnlDokterDpjp.putClientProperty("Component.arc", 0);
        pnlDokterDpjp.setBorder(BorderFactory.createLineBorder(new Color(0xCF, 0xD3, 0xCE), 1));
        NmDokterDpjp.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        pnlDokterDpjp.add(NmDokterDpjp, BorderLayout.CENTER);
        pnlDokterDpjp.add(BtnDokterDpjp, BorderLayout.EAST);

        // Baris 0: TANGGAL, ANAMNESIS, HUBUNGAN, ASAL MASUK
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.insets = new Insets(0, 0, 10, 14);
        gridHeader.add(buatKotakHeader("TANGGAL", TglAsuhan), gc);

        gc.gridx = 1;
        gridHeader.add(buatKotakHeader("ANAMNESIS", CmbAnamnesis), gc);

        gc.gridx = 2;
        gridHeader.add(buatKotakHeader("HUBUNGAN / PEMBERI INFORMASI", THubungan), gc);

        gc.gridx = 3;
        gc.insets = new Insets(0, 0, 10, 0);
        gridHeader.add(buatKotakHeader("ASAL PASIEN MASUK", pnlAsalMasuk), gc);

        // Baris 1: CARA KE RUANGAN, DOKTER PENGKAJIAN, DOKTER DPJP
        gc.gridy = 1;
        gc.gridx = 0;
        gc.insets = new Insets(0, 0, 0, 14);
        gridHeader.add(buatKotakHeader("CARA KE RUANGAN", pnlCaraRuangan), gc);

        gc.gridx = 1;
        gridHeader.add(buatKotakHeader("DOKTER YANG MELAKUKAN PENGKAJIAN", pnlDokterPeriksa), gc);

        gc.gridx = 2;
        gridHeader.add(buatKotakHeader("DOKTER PENANGGUNG JAWAB PELAYANAN", pnlDokterDpjp), gc);

        gc.gridx = 3;
        gc.insets = new Insets(0, 0, 0, 0);
        gridHeader.add(Box.createGlue(), gc);

        // Pasang ke barPasienMedis
        GridBagConstraints bpc = new GridBagConstraints();
        bpc.gridx = 0;
        bpc.gridy = 0;
        bpc.fill = GridBagConstraints.VERTICAL;
        bpc.insets = new Insets(0, 0, 0, 18);
        barPasienMedis.add(kartuId, bpc);

        bpc.gridx = 1;
        bpc.fill = GridBagConstraints.HORIZONTAL;
        bpc.weightx = 1.0;
        bpc.insets = new Insets(0, 0, 0, 0);
        barPasienMedis.add(gridHeader, bpc);

        internalFrame2.add(barPasienMedis, BorderLayout.NORTH);

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

    private void gayaKotakInput(Component c) {
        if (c instanceof javax.swing.JComponent) {
            javax.swing.JComponent jc = (javax.swing.JComponent) c;
            jc.putClientProperty("Component.arc", 0);
            jc.putClientProperty("TextComponent.arc", 0);
            jc.putClientProperty("JTextField.arc", 0);
            jc.putClientProperty("ComboBox.arc", 0);
            jc.putClientProperty("JComponent.roundRect", false);
            jc.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xCF, 0xD3, 0xCE), 1),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        }
    }

    private JPanel buatBarisNomorPanel(String label, JLabel lblVal) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel lblK = new JLabel(label);
        lblK.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblK.setForeground(new Color(0x9A, 0xA1, 0x9C));
        lblVal.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblVal.setForeground(new Color(0x5D, 0x64, 0x5F));
        lblVal.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(lblK, BorderLayout.WEST);
        p.add(lblVal, BorderLayout.EAST);
        return p;
    }

    private JPanel buatKotakHeader(String label, Component field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(0x8B, 0x92, 0x8D));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void sinkronKartuIdentitas() {
        if (lblKartuNama == null) return;
        String nama = TPasien.getText().trim();
        lblKartuNama.setText(nama.isEmpty() ? "—" : nama);

        StringBuilder inisial = new StringBuilder();
        for (String b : nama.split("\\s+")) {
            if (!b.isEmpty() && inisial.length() < 2) {
                inisial.append(Character.toUpperCase(b.charAt(0)));
            }
        }
        lblKartuInisial.setText(inisial.length() == 0 ? "SN" : inisial.toString());

        String jkText = "L".equalsIgnoreCase(Jk.getText().trim()) ? "Laki-Laki" : "Perempuan";
        String umurText = hitungUmur();
        lblKartuInfo.setText(jkText + (umurText.isEmpty() ? "" : " - " + umurText));

        if (lblValRM != null) {
            lblValRM.setText(TNoRM.getText().isEmpty() ? "—" : TNoRM.getText());
        }
        if (lblValRawat != null) {
            lblValRawat.setText(TNoRw.getText().isEmpty() ? "—" : TNoRw.getText());
        }
    }

    // =================================================================
    //  PANEL KIRI (RAIL NAVIGASI ~300PX)
    // =================================================================

    private void rakitRailNavigasi() {
        railMedis = new widget.PanelBiasa();
        railMedis.setBackground(new Color(0xE8, 0xE9, 0xE6));
        railMedis.setLayout(new GridBagLayout());
        railMedis.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 10));

        scrollRailMedis = new widget.ScrollPane();
        scrollRailMedis.setViewportView(railMedis);
        scrollRailMedis.setBorder(null);
        scrollRailMedis.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollRailMedis.setPreferredSize(new Dimension(300, 10));
        scrollRailMedis.getVerticalScrollBar().setUnitIncrement(16);
        scrollRailMedis.getViewport().setBackground(new Color(0xE8, 0xE9, 0xE6));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;

        // 1. Kartu ISIAN WAJIB
        c.gridy = 0;
        c.insets = new Insets(0, 0, 12, 0);
        railMedis.add(buatKartuIsianWajib(), c);

        // 2. Kartu BAGIAN PENGKAJIAN
        c.gridy = 1;
        railMedis.add(buatKartuBagianPengkajian(), c);

        // 3. Kartu RUJUKAN DATA
        c.gridy = 2;
        railMedis.add(buatKartuRujukanData(), c);

        // 4. Tombol Sakelar Tampilkan Semua Bagian
        btnToggleMode = new widget.Button();
        btnToggleMode.setText("Tampilkan Semua Bagian");
        btnToggleMode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggleMode.setBackground(Color.WHITE);
        btnToggleMode.setForeground(ThemeConfig.PRIMARY);
        btnToggleMode.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x0D, 0x7A, 0x70, 0x80)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        btnToggleMode.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggleMode.addActionListener(evt -> toggleModeHalaman());

        c.gridy = 3;
        c.insets = new Insets(6, 0, 16, 0);
        railMedis.add(btnToggleMode, c);

        // Glue Vertikal
        c.gridy = 4;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        railMedis.add(Box.createGlue(), c);

        internalFrame2.add(scrollRailMedis, BorderLayout.WEST);
    }

    private JPanel buatKartuIsianWajib() {
        JPanel kartu = new JPanel(new GridBagLayout());
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDC, 0xDF, 0xDA)),
                BorderFactory.createEmptyBorder(14, 14, 12, 14)));

        JLabel lblJudul = new JLabel("ISIAN WAJIB");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblJudul.setForeground(new Color(0x8B, 0x92, 0x8D));

        lblProgresPersen = new widget.Label();
        lblProgresPersen.setText("0%");
        lblProgresPersen.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblProgresPersen.setForeground(new Color(0x1C, 0x1F, 0x1D));

        pbProgres = new JProgressBar(0, 100);
        pbProgres.setValue(0);
        pbProgres.setPreferredSize(new Dimension(200, 6));
        pbProgres.setForeground(ThemeConfig.PRIMARY);
        pbProgres.setBackground(new Color(0xE6, 0xE8, 0xE3));
        pbProgres.setBorder(null);

        lblProgresHitung = new widget.Label();
        lblProgresHitung.setText("0/14 isian wajib");
        lblProgresHitung.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblProgresHitung.setForeground(new Color(0x9A, 0xA1, 0x9C));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        kartu.add(lblJudul, c);

        c.gridx = 1;
        c.anchor = GridBagConstraints.EAST;
        kartu.add(lblProgresPersen, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(10, 0, 8, 0);
        kartu.add(pbProgres, c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        kartu.add(lblProgresHitung, c);

        return kartu;
    }

    private JPanel buatKartuBagianPengkajian() {
        JPanel kartu = new JPanel(new BorderLayout());
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createLineBorder(new Color(0xDC, 0xDF, 0xDA)));

        JLabel lblJudul = new JLabel("BAGIAN PENGKAJIAN");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblJudul.setForeground(new Color(0x8B, 0x92, 0x8D));
        lblJudul.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEC, 0xEE, 0xEA)),
                BorderFactory.createEmptyBorder(11, 14, 11, 14)));
        kartu.add(lblJudul, BorderLayout.NORTH);

        JPanel pnlDaftar = new JPanel(new GridBagLayout());
        pnlDaftar.setOpaque(false);

        lblRailTally1 = new widget.Label();
        lblRailTally1.setText("0/14");
        lblRailTally1.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblRailTally1.setForeground(new Color(0xC0, 0x8A, 0x2E));

        lblRailTally2 = new widget.Label();
        lblRailTally2.setText("-");
        lblRailTally2.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblRailTally2.setForeground(new Color(0x9A, 0xA1, 0x9C));

        barisRail1 = buatBarisRail(0, "I", "Pemeriksaan Fisik", lblRailTally1);
        barisRail2 = buatBarisRail(1, "II", "Asesmen & Program", lblRailTally2);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        pnlDaftar.add(barisRail1, c);
        c.gridy = 1;
        pnlDaftar.add(barisRail2, c);

        kartu.add(pnlDaftar, BorderLayout.CENTER);
        return kartu;
    }

    private widget.PanelBiasa buatBarisRail(int idx, String romawi, String judul, widget.Label lblTally) {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setBackground(Color.WHITE);
        p.setLayout(new BorderLayout(10, 0));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF4, 0xF5, 0xF2)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblNo = new JLabel(romawi);
        lblNo.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblNo.setForeground(new Color(0x9A, 0xA1, 0x9C));
        lblNo.setPreferredSize(new Dimension(20, 18));

        JLabel lblTeks = new JLabel(judul);
        lblTeks.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTeks.setForeground(new Color(0x3D, 0x44, 0x41));

        p.add(lblNo, BorderLayout.WEST);
        p.add(lblTeks, BorderLayout.CENTER);
        p.add(lblTally, BorderLayout.EAST);

        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                keHalaman(idx);
            }
        });
        return p;
    }

    private JPanel buatKartuRujukanData() {
        JPanel kartu = new JPanel(new BorderLayout());
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createLineBorder(new Color(0xDC, 0xDF, 0xDA)));

        JLabel lblJudul = new JLabel("RUJUKAN DATA");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblJudul.setForeground(new Color(0x8B, 0x92, 0x8D));
        lblJudul.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEC, 0xEE, 0xEA)),
                BorderFactory.createEmptyBorder(11, 14, 11, 14)));
        kartu.add(lblJudul, BorderLayout.NORTH);

        JPanel pnlDaftar = new JPanel(new GridBagLayout());
        pnlDaftar.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        pnlDaftar.add(buatBarisRujukan("Pengkajian Awal Keperawatan", "Ada", true), c);
        c.gridy = 1;
        pnlDaftar.add(buatBarisRujukan("Triase / Asesmen IGD", "Ada", true), c);
        c.gridy = 2;
        pnlDaftar.add(buatBarisRujukan("Rekonsiliasi Obat", "Belum", false), c);

        kartu.add(pnlDaftar, BorderLayout.CENTER);
        return kartu;
    }

    private JPanel buatBarisRujukan(String teks, String status, boolean ada) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF4, 0xF5, 0xF2)),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));

        JLabel lbl = new JLabel(teks);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(0x3D, 0x44, 0x41));

        JLabel badge = new JLabel(status);
        badge.setFont(new Font("Monospaced", Font.BOLD, 10));
        badge.setOpaque(true);
        if (ada) {
            badge.setBackground(new Color(0xE8, 0xF3, 0xED));
            badge.setForeground(new Color(0x1A, 0x6B, 0x4C));
        } else {
            badge.setBackground(new Color(0xF2, 0xF3, 0xF0));
            badge.setForeground(new Color(0x9A, 0xA1, 0x9C));
        }
        badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        p.add(lbl, BorderLayout.CENTER);
        p.add(badge, BorderLayout.EAST);
        return p;
    }

    private void sorotHalaman(int idx) {
        halamanAktif = idx;
        if (barisRail1 != null && barisRail2 != null) {
            barisRail1.setBackground(idx == 0 && satuHalaman ? new Color(0xF1, 0xF7, 0xF6) : Color.WHITE);
            barisRail1.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, idx == 0 && satuHalaman ? 3 : 0, 1, 0,
                            idx == 0 && satuHalaman ? ThemeConfig.PRIMARY : new Color(0xF4, 0xF5, 0xF2)),
                    BorderFactory.createEmptyBorder(10, idx == 0 && satuHalaman ? 11 : 14, 10, 14)));

            barisRail2.setBackground(idx == 1 && satuHalaman ? new Color(0xF1, 0xF7, 0xF6) : Color.WHITE);
            barisRail2.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, idx == 1 && satuHalaman ? 3 : 0, 1, 0,
                            idx == 1 && satuHalaman ? ThemeConfig.PRIMARY : new Color(0xF4, 0xF5, 0xF2)),
                    BorderFactory.createEmptyBorder(10, idx == 1 && satuHalaman ? 11 : 14, 10, 14)));
            barisRail1.repaint();
            barisRail2.repaint();
        }
    }

    private void keHalaman(int idx) {
        sorotHalaman(idx);
        if (satuHalaman) {
            terapkanModeHalaman();
            if (scrollSeksiMedis != null) {
                scrollSeksiMedis.getVerticalScrollBar().setValue(0);
            }
        } else {
            Component target = idx == 0 ? kartuBagian1 : kartuBagian2;
            if (target != null && scrollSeksiMedis != null) {
                scrollSeksiMedis.getVerticalScrollBar().setValue(target.getY());
            }
        }
    }

    private void toggleModeHalaman() {
        satuHalaman = !satuHalaman;
        btnToggleMode.setText(satuHalaman ? "Tampilkan Semua Bagian" : "Tampilkan Per Bagian");
        terapkanModeHalaman();
        sorotHalaman(halamanAktif);
    }

    private void terapkanModeHalaman() {
        if (kartuBagian1 != null && kartuBagian2 != null) {
            if (satuHalaman) {
                kartuBagian1.setVisible(halamanAktif == 0);
                kartuBagian2.setVisible(halamanAktif == 1);
            } else {
                kartuBagian1.setVisible(true);
                kartuBagian2.setVisible(true);
            }
            if (FormInput != null) {
                FormInput.revalidate();
                FormInput.repaint();
            }
        }
    }

    // =================================================================
    //  FORM TENGAH (KARTU BAGIAN I & BAGIAN II)
    // =================================================================

    private void rakitHalamanSeksi() {
        FormInput = new widget.PanelBiasa();
        FormInput.setBackground(new Color(0xE8, 0xE9, 0xE6));
        FormInput.setLayout(new GridBagLayout());
        FormInput.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 20));

        scrollSeksiMedis = new widget.ScrollPane();
        scrollSeksiMedis.setViewportView(FormInput);
        scrollSeksiMedis.setBorder(null);
        scrollSeksiMedis.getVerticalScrollBar().setUnitIncrement(16);
        scrollSeksiMedis.getViewport().setBackground(new Color(0xE8, 0xE9, 0xE6));

        kartuBagian1 = rakitBagian1Fisik();
        kartuBagian2 = rakitBagian2Asesmen();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 16, 0);
        FormInput.add(kartuBagian1, c);

        c.gridy = 1;
        FormInput.add(kartuBagian2, c);

        c.gridy = 2;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        FormInput.add(Box.createGlue(), c);

        internalFrame2.add(scrollSeksiMedis, BorderLayout.CENTER);
    }

    private widget.PanelBiasa rakitBagian1Fisik() {
        widget.PanelBiasa kartu = new widget.PanelBiasa();
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createLineBorder(new Color(0xDC, 0xDF, 0xDA)));
        kartu.setLayout(new BorderLayout());

        // Header Strip Bagian I
        lblStatusSeksi1 = new widget.Label();
        lblStatusSeksi1.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblStatusSeksi1.setOpaque(true);
        lblStatusSeksi1.setBackground(new Color(0xFD, 0xF3, 0xE7));
        lblStatusSeksi1.setForeground(new Color(0x8A, 0x5A, 0x12));
        lblStatusSeksi1.setText("0/14 wajib terisi");
        lblStatusSeksi1.setBorder(BorderFactory.createEmptyBorder(4, 9, 4, 9));

        btnTidakRelevan1 = new widget.Button();
        btnTidakRelevan1.setText("Tandai tidak relevan");
        btnTidakRelevan1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnTidakRelevan1.setBackground(Color.WHITE);
        btnTidakRelevan1.setForeground(new Color(0x6C, 0x73, 0x6E));
        btnTidakRelevan1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD8, 0xDB, 0xD6)),
                BorderFactory.createEmptyBorder(4, 11, 4, 11)));
        btnTidakRelevan1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTidakRelevan1.addActionListener(evt -> {
            tidakRelevan1 = !tidakRelevan1;
            btnTidakRelevan1.setText(tidakRelevan1 ? "↺ Aktifkan kembali" : "Tandai tidak relevan");
            lblStatusSeksi1.setText(tidakRelevan1 ? "Tidak relevan" : "0/14 wajib terisi");
            lblStatusSeksi1.setBackground(tidakRelevan1 ? new Color(0xF2, 0xF3, 0xF0) : new Color(0xFD, 0xF3, 0xE7));
            lblStatusSeksi1.setForeground(tidakRelevan1 ? new Color(0x9A, 0xA1, 0x9C) : new Color(0x8A, 0x5A, 0x12));
            hitungProgres();
        });

        JPanel headerStrip = buatHeaderStripSeksi("I", "Pemeriksaan Fisik", "a-n: Normal / Tidak Normal per sistem",
                lblStatusSeksi1, btnTidakRelevan1);
        kartu.add(headerStrip, BorderLayout.NORTH);

        // Badan Baris a - n + Diagram Tubuh
        JPanel pnlIsi = new JPanel(new GridBagLayout());
        pnlIsi.setOpaque(false);
        pnlIsi.setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));

        GridBagConstraints rc = new GridBagConstraints();
        rc.gridx = 0;
        rc.weightx = 1.0;
        rc.fill = GridBagConstraints.HORIZONTAL;

        int rowIdx = 0;
        for (RegioFisik rf : regios) {
            rc.gridy = rowIdx++;
            pnlIsi.add(buatBarisRegio(rf), rc);
        }

        // Row Diagram Tubuh
        rc.gridy = rowIdx++;
        pnlIsi.add(buatBarisDiagramTubuh(), rc);

        kartu.add(pnlIsi, BorderLayout.CENTER);

        // Stepper Bawah: < Awal | Bagian 1 dari 2 | Asesmen & Program >
        JPanel stepper = buatStepperBawah("< Awal", false, () -> {},
                "Bagian 1 dari 2",
                "Asesmen & Program >", true, () -> keHalaman(1));
        kartu.add(stepper, BorderLayout.SOUTH);

        return kartu;
    }

    private JPanel buatBarisRegio(RegioFisik rf) {
        rf.grup = new ButtonGroup();
        rf.rbNormal = new widget.RadioButton();
        rf.rbNormal.setText("Normal");
        rf.rbTidakNormal = new widget.RadioButton();
        rf.rbTidakNormal.setText("Tidak Normal");
        rf.grup.add(rf.rbNormal);
        rf.grup.add(rf.rbTidakNormal);

        rf.pillNormal = new RadioPill(rf.rbNormal, "Normal");
        rf.pillTidakNormal = new RadioPill(rf.rbTidakNormal, "Tidak Normal");

        rf.txtKet = new widget.TextBox();
        rf.txtKet.setPreferredSize(new Dimension(280, 32));
        rf.txtKet.putClientProperty("JTextField.placeholderText", "Jelaskan temuan fisik...");
        gayaKotakInput(rf.txtKet);
        rf.txtKet.setVisible(false);

        ItemListener il = e -> {
            boolean tidakNormal = rf.rbTidakNormal.isSelected();
            rf.txtKet.setVisible(tidakNormal);
            if (!tidakNormal) {
                rf.txtKet.setText("");
            }
            rf.pillNormal.perbarui();
            rf.pillTidakNormal.perbarui();
            if (rf.panelRow != null) {
                rf.panelRow.revalidate();
                rf.panelRow.repaint();
            }
            hitungProgres();
        };
        rf.rbNormal.addItemListener(il);
        rf.rbTidakNormal.addItemListener(il);

        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF2, 0xF3, 0xF0)));
        rf.panelRow = pnl;

        JLabel lbl = new JLabel(rf.labelTeks + " *");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(0x1C, 0x1F, 0x1D));
        lbl.setPreferredSize(new Dimension(232, 38));

        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlRadio.setOpaque(false);
        pnlRadio.add(rf.pillNormal);
        pnlRadio.add(rf.pillTidakNormal);
        pnlRadio.add(rf.txtKet);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        pnl.add(lbl, c);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        pnl.add(pnlRadio, c);

        return pnl;
    }

    private JPanel buatBarisDiagramTubuh() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 0));

        JLabel lbl = new JLabel("Diagram Tubuh");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(0x3D, 0x44, 0x41));
        lbl.setPreferredSize(new Dimension(232, 30));

        JPanel boxDiagram = new JPanel(new BorderLayout(8, 8));
        boxDiagram.setBackground(new Color(0xFB, 0xFC, 0xFA));
        boxDiagram.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(new Color(0xC6, 0xCA, 0xC5), 4, 4),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        boxDiagram.setPreferredSize(new Dimension(500, 170));

        JLabel lblGambar = new JLabel();
        lblGambar.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/picture/fisiobody.png"));
            Image img = icon.getImage().getScaledInstance(130, 145, Image.SCALE_SMOOTH);
            lblGambar.setIcon(new ImageIcon(img));
        } catch (Exception ignored) {
            lblGambar.setText("Diagram Tubuh");
        }

        JLabel lblKeterangan = new JLabel("Diagram tubuh (anterior/posterior/lateral) untuk penandaan regio — sisipkan aset gambar dari SIMRS");
        lblKeterangan.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblKeterangan.setForeground(new Color(0x9A, 0xA1, 0x9C));
        lblKeterangan.setHorizontalAlignment(SwingConstants.CENTER);

        boxDiagram.add(lblGambar, BorderLayout.CENTER);
        boxDiagram.add(lblKeterangan, BorderLayout.SOUTH);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        pnl.add(lbl, c);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        pnl.add(boxDiagram, c);

        return pnl;
    }

    private void gayaChipRadio(widget.RadioButton rb) {
        boolean on = rb.isSelected();
        rb.setOpaque(true);
        rb.setBackground(on ? new Color(0xF1, 0xF7, 0xF6) : Color.WHITE);
        rb.setForeground(on ? new Color(0x1C, 0x1F, 0x1D) : new Color(0x3D, 0x44, 0x41));
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(on ? ThemeConfig.PRIMARY : new Color(0xCF, 0xD3, 0xCE)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
    }

    private widget.PanelBiasa rakitBagian2Asesmen() {
        widget.PanelBiasa kartu = new widget.PanelBiasa();
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createLineBorder(new Color(0xDC, 0xDF, 0xDA)));
        kartu.setLayout(new BorderLayout());

        // Header Strip Bagian II
        lblStatusSeksi2 = new widget.Label();
        lblStatusSeksi2.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblStatusSeksi2.setOpaque(true);
        lblStatusSeksi2.setBackground(new Color(0xF2, 0xF3, 0xF0));
        lblStatusSeksi2.setForeground(new Color(0x9A, 0xA1, 0x9C));
        lblStatusSeksi2.setText("-");
        lblStatusSeksi2.setBorder(BorderFactory.createEmptyBorder(4, 9, 4, 9));

        btnTidakRelevan2 = new widget.Button();
        btnTidakRelevan2.setText("Tandai tidak relevan");
        btnTidakRelevan2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnTidakRelevan2.setBackground(Color.WHITE);
        btnTidakRelevan2.setForeground(new Color(0x6C, 0x73, 0x6E));
        btnTidakRelevan2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD8, 0xDB, 0xD6)),
                BorderFactory.createEmptyBorder(4, 11, 4, 11)));
        btnTidakRelevan2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTidakRelevan2.addActionListener(evt -> {
            tidakRelevan2 = !tidakRelevan2;
            btnTidakRelevan2.setText(tidakRelevan2 ? "↺ Aktifkan kembali" : "Tandai tidak relevan");
            lblStatusSeksi2.setText(tidakRelevan2 ? "Tidak relevan" : "-");
            hitungProgres();
        });

        JPanel headerStrip = buatHeaderStripSeksi("II", "Asesmen & Program",
                "Diagnosa medis/banding, advis terapi & rencana tindakan", lblStatusSeksi2, btnTidakRelevan2);
        kartu.add(headerStrip, BorderLayout.NORTH);

        // Badan: Row Diagnosa & Row Program
        JPanel pnlIsi = new JPanel(new GridBagLayout());
        pnlIsi.setOpaque(false);
        pnlIsi.setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));

        DiagnosaMedis = new widget.TextArea();
        DiagnosaBanding = new widget.TextArea();
        AdvisTerapi = new widget.TextArea();
        RencanaTindakan = new widget.TextArea();

        GridBagConstraints rc = new GridBagConstraints();
        rc.gridx = 0;
        rc.weightx = 1.0;
        rc.fill = GridBagConstraints.HORIZONTAL;

        rc.gridy = 0;
        pnlIsi.add(buatBarisDualTextArea("Diagnosa", "Diagnosa Medis", DiagnosaMedis,
                "Diagnosa Banding", DiagnosaBanding), rc);

        rc.gridy = 1;
        pnlIsi.add(buatBarisDualTextArea("Program", "Advis Terapi / Pemeriksaan Penunjang", AdvisTerapi,
                "Perencanaan Tindakan", RencanaTindakan), rc);

        kartu.add(pnlIsi, BorderLayout.CENTER);

        // Stepper Bawah: < Pemeriksaan Fisik | Bagian 2 dari 2 | Akhir >
        JPanel stepper = buatStepperBawah("< Pemeriksaan Fisik", true, () -> keHalaman(0),
                "Bagian 2 dari 2",
                "Akhir >", false, () -> {});
        kartu.add(stepper, BorderLayout.SOUTH);

        return kartu;
    }

    private JPanel buatBarisDualTextArea(String labelUtama, String label1, widget.TextArea ta1,
                                         String label2, widget.TextArea ta2) {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF2, 0xF3, 0xF0)));

        JLabel lbl = new JLabel(labelUtama);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(0x3D, 0x44, 0x41));
        lbl.setPreferredSize(new Dimension(232, 38));

        JPanel pnlGrid = new JPanel(new java.awt.GridLayout(1, 2, 16, 0));
        pnlGrid.setOpaque(false);
        pnlGrid.setBorder(BorderFactory.createEmptyBorder(8, 0, 14, 0));

        pnlGrid.add(buatKotakTextArea(label1, ta1));
        pnlGrid.add(buatKotakTextArea(label2, ta2));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        pnl.add(lbl, c);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        pnl.add(pnlGrid, c);

        return pnl;
    }

    private JPanel buatKotakTextArea(String miniLabel, widget.TextArea ta) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);

        JLabel l = new JLabel(miniLabel);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(0x9A, 0xA1, 0x9C));

        ta.setColumns(20);
        ta.setRows(5);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ta.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        widget.ScrollPane sp = new widget.ScrollPane();
        sp.setViewportView(ta);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xCF, 0xD3, 0xCE)));
        sp.setPreferredSize(new Dimension(200, 110));

        p.add(l, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buatHeaderStripSeksi(String romawi, String judul, String subjudul,
                                        widget.Label lblTally, widget.Button btnRelevan) {
        JPanel strip = new JPanel(new BorderLayout(14, 0));
        strip.setBackground(new Color(0xFB, 0xFC, 0xFA));
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEC, 0xEE, 0xEA)),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        JPanel pnlKiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlKiri.setOpaque(false);

        JLabel badgeNo = new JLabel(romawi);
        badgeNo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badgeNo.setForeground(Color.WHITE);
        badgeNo.setHorizontalAlignment(SwingConstants.CENTER);
        badgeNo.setOpaque(true);
        badgeNo.setBackground(ThemeConfig.PRIMARY);
        badgeNo.setPreferredSize(new Dimension(26, 26));

        JPanel pnlTeks = new JPanel();
        pnlTeks.setOpaque(false);
        pnlTeks.setLayout(new javax.swing.BoxLayout(pnlTeks, javax.swing.BoxLayout.Y_AXIS));

        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lJudul.setForeground(new Color(0x1C, 0x1F, 0x1D));

        JLabel lSub = new JLabel(subjudul);
        lSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lSub.setForeground(new Color(0x9A, 0xA1, 0x9C));

        pnlTeks.add(lJudul);
        pnlTeks.add(Box.createVerticalStrut(2));
        pnlTeks.add(lSub);

        pnlKiri.add(badgeNo);
        pnlKiri.add(pnlTeks);

        JPanel pnlKanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlKanan.setOpaque(false);
        pnlKanan.add(lblTally);
        pnlKanan.add(btnRelevan);

        strip.add(pnlKiri, BorderLayout.WEST);
        strip.add(pnlKanan, BorderLayout.EAST);
        return strip;
    }

    private JPanel buatStepperBawah(String prevTeks, boolean prevAktif, Runnable onPrev,
                                    String tengahTeks,
                                    String nextTeks, boolean nextAktif, Runnable onNext) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(0xFB, 0xFC, 0xFA));
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xEC, 0xEE, 0xEA)),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));

        widget.Button btnPrev = buatTombolStepper(prevTeks, prevAktif, onPrev);
        JLabel lblTengah = new JLabel(tengahTeks);
        lblTengah.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTengah.setForeground(new Color(0x9A, 0xA1, 0x9C));
        lblTengah.setHorizontalAlignment(SwingConstants.CENTER);
        widget.Button btnNext = buatTombolStepper(nextTeks, nextAktif, onNext);

        pnl.add(btnPrev, BorderLayout.WEST);
        pnl.add(lblTengah, BorderLayout.CENTER);
        pnl.add(btnNext, BorderLayout.EAST);
        return pnl;
    }

    private widget.Button buatTombolStepper(String teks, boolean aktif, Runnable aksi) {
        widget.Button b = new widget.Button();
        b.setText(teks);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setBackground(Color.WHITE);
        b.setForeground(aktif ? new Color(0x3D, 0x44, 0x41) : new Color(0xC1, 0xC5, 0xC0));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD8, 0xDB, 0xD6)),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        b.setCursor(aktif ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        if (aktif) {
            b.addActionListener(evt -> aksi.run());
        }
        return b;
    }

    private void hitungProgres() {
        int total = 14;
        int terisi = 0;
        for (RegioFisik rf : regios) {
            if (rf.rbNormal.isSelected() || rf.rbTidakNormal.isSelected()) {
                terisi++;
            }
        }
        int persen = (int) Math.round((terisi / (double) total) * 100);

        if (pbProgres != null) {
            pbProgres.setValue(persen);
        }
        if (lblProgresPersen != null) {
            lblProgresPersen.setText(persen + "%");
        }
        if (lblProgresHitung != null) {
            lblProgresHitung.setText(terisi + "/" + total + " isian wajib");
        }
        if (lblRailTally1 != null) {
            lblRailTally1.setText(terisi + "/" + total);
        }
        if (lblStatusSeksi1 != null && !tidakRelevan1) {
            if (terisi == total) {
                lblStatusSeksi1.setText("Wajib lengkap");
                lblStatusSeksi1.setBackground(new Color(0xE8, 0xF3, 0xED));
                lblStatusSeksi1.setForeground(new Color(0x1A, 0x6B, 0x4C));
            } else {
                lblStatusSeksi1.setText(terisi + "/" + total + " wajib terisi");
                lblStatusSeksi1.setBackground(new Color(0xFD, 0xF3, 0xE7));
                lblStatusSeksi1.setForeground(new Color(0x8A, 0x5A, 0x12));
            }
        }
        if (lblFooterNote != null) {
            if (terisi == total) {
                lblFooterNote.setText("Isian wajib lengkap - siap disimpan");
            } else {
                lblFooterNote.setText("Isian wajib " + terisi + "/" + total + " - " + (total - terisi) + " belum terisi");
            }
        }
    }

    private void pasangBatasInput() {
        TNoRw.setDocument(new batasInput((byte) 17).getKata(TNoRw));
        THubungan.setDocument(new batasInput((int) 100).getKata(THubungan));
        for (RegioFisik rf : regios) {
            rf.txtKet.setDocument(new batasInput((int) 255).getKata(rf.txtKet));
        }
        DiagnosaMedis.setDocument(new batasInput((int) 4000).getKata(DiagnosaMedis));
        DiagnosaBanding.setDocument(new batasInput((int) 4000).getKata(DiagnosaBanding));
        AdvisTerapi.setDocument(new batasInput((int) 4000).getKata(AdvisTerapi));
        RencanaTindakan.setDocument(new batasInput((int) 4000).getKata(RencanaTindakan));
        TCari.setDocument(new batasInput((int) 100).getKata(TCari));
    }

    private String hitungUmur() {
        try {
            String lahir = TglLahir.getText().trim();
            if (lahir.isEmpty()) {
                return "";
            }
            LocalDate tglLahir = LocalDate.parse(lahir.substring(0, 10));
            LocalDate tglAsesmen;
            try {
                tglAsesmen = LocalDate.parse(TglAsuhan.getSelectedItem().toString().substring(0, 10),
                        DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (Exception e) {
                tglAsesmen = LocalDate.now();
            }
            if (tglLahir.isAfter(tglAsesmen)) {
                return "";
            }
            Period p = Period.between(tglLahir, tglAsesmen);
            if (p.getYears() > 0) {
                return p.getYears() + " Th";
            }
            if (p.getMonths() > 0) {
                return p.getMonths() + " Bl";
            }
            return p.getDays() + " Hr";
        } catch (Exception e) {
            return "";
        }
    }

    // =================================================================
    //  TAB DATA TERSIMPAN
    // =================================================================

    private void rakitTabData() {
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnAsesmenMedis = new javax.swing.JMenuItem();
        MnAsesmenMedis.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        MnAsesmenMedis.setIcon(new ImageIcon(getClass().getResource("/picture/category.png")));
        MnAsesmenMedis.setText("Cetak Asesmen Medis Rawat Inap (RM 20-1)");
        MnAsesmenMedis.setPreferredSize(new Dimension(260, 26));
        MnAsesmenMedis.addActionListener(evt -> cetakTerpilih());
        jPopupMenu1.add(MnAsesmenMedis);

        internalFrame3 = new widget.InternalFrame();
        internalFrame3.setBorder(null);
        internalFrame3.setLayout(new BorderLayout(1, 1));

        tbObat = new widget.Table();
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);

        int[] lebar = {105, 70, 150, 75, 60, 80, 150, 150, 115, 95, 200, 200};
        for (i = 0; i < lebar.length; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebar[i]);
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        tbObat.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent evt) {
                if (tabMode.getRowCount() != 0) {
                    getData();
                }
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (tabMode.getRowCount() == 0) {
                    return;
                }
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

        panelGlass9 = new widget.panelisi();
        panelGlass9.setPreferredSize(new Dimension(44, 44));
        panelGlass9.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        DTPCari1 = new widget.Tanggal();
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setDate(new Date());
        DTPCari1.setPreferredSize(new Dimension(95, 23));

        DTPCari2 = new widget.Tanggal();
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setDate(new Date());
        DTPCari2.setPreferredSize(new Dimension(95, 23));

        TCari = new widget.TextBox();
        TCari.setPreferredSize(new Dimension(200, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    runBackground(() -> tampil());
                }
            }
        });

        BtnCari = new widget.Button();
        BtnCari.setIcon(new ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setPreferredSize(new Dimension(28, 23));
        BtnCari.addActionListener(evt -> runBackground(() -> tampil()));

        LCount = new widget.Label();
        LCount.setText("0");
        LCount.setHorizontalAlignment(SwingConstants.LEFT);
        LCount.setPreferredSize(new Dimension(70, 23));

        BtnPrint1 = new widget.Button();
        BtnPrint1.setIcon(new ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint1.setPreferredSize(new Dimension(28, 23));
        BtnPrint1.setToolTipText("Cetak data yang terpilih");
        BtnPrint1.addActionListener(evt -> cetakTerpilih());

        widget.Label lTgl = new widget.Label(); lTgl.setText("Tanggal :");
        widget.Label lSd = new widget.Label(); lSd.setText("s.d.");
        widget.Label lKw = new widget.Label(); lKw.setText("Key Word :");
        widget.Label lRec = new widget.Label(); lRec.setText("Record :");

        panelGlass9.add(lTgl);
        panelGlass9.add(DTPCari1);
        panelGlass9.add(lSd);
        panelGlass9.add(DTPCari2);
        panelGlass9.add(lKw);
        panelGlass9.add(TCari);
        panelGlass9.add(BtnCari);
        panelGlass9.add(lRec);
        panelGlass9.add(LCount);
        panelGlass9.add(BtnPrint1);
        internalFrame3.add(panelGlass9, BorderLayout.PAGE_END);

        PanelAccor = new widget.PanelBiasa();
        PanelAccor.setBackground(Color.WHITE);
        PanelAccor.setLayout(new BorderLayout());

        ChkAccor = new widget.CekBox();
        ChkAccor.setIcon(new ImageIcon(getClass().getResource("/picture/kiri.png")));
        ChkAccor.setSelectedIcon(new ImageIcon(getClass().getResource("/picture/kanan.png")));
        ChkAccor.setBackground(new Color(245, 250, 240));
        ChkAccor.setPreferredSize(new Dimension(16, 140));
        ChkAccor.addActionListener(evt -> isMenu());
        PanelAccor.add(ChkAccor, BorderLayout.WEST);

        FormMenu = new widget.panelisi();
        FormMenu.setBackground(Color.WHITE);
        FormMenu.setPreferredSize(new Dimension(420, 44));
        FormMenu.setLayout(new BorderLayout(1, 1));

        ScrollHTML = new widget.ScrollPane();
        LoadHTML = new widget.editorpane();
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}");
        LoadHTML.setEditable(false);
        ScrollHTML.setViewportView(LoadHTML);
        FormMenu.add(ScrollHTML, BorderLayout.CENTER);

        PanelAccor.add(FormMenu, BorderLayout.CENTER);
        internalFrame3.add(PanelAccor, BorderLayout.EAST);

        TabRawat.addTab("Data Tersimpan", internalFrame3);
    }

    private void isMenu() {
        if (ChkAccor.isSelected()) {
            ChkAccor.setToolTipText("Tutup panel ringkasan");
            FormMenu.setVisible(true);
            PanelAccor.setPreferredSize(new Dimension(436, PanelAccor.getHeight()));
            bangunRingkasanHtml();
        } else {
            ChkAccor.setToolTipText("Buka panel ringkasan");
            FormMenu.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(16, PanelAccor.getHeight()));
        }
    }

    // =================================================================
    //  BINDING DATA (INSERT / UPDATE / SELECT)
    // =================================================================

    private String[] nilaiRm20_1() {
        return new String[]{
            Valid.SetTgl(TglAsuhan.getSelectedItem() + "") + " " + (TglAsuhan.getSelectedItem() + "").substring(11, 16) + ":00",
            KdDokterPeriksa.getText(), KdDokterDpjp.getText(),
            CmbAnamnesis.getSelectedItem().toString(), THubungan.getText(),
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
            DiagnosaMedis.getText(), DiagnosaBanding.getText(),
            AdvisTerapi.getText(), RencanaTindakan.getText()
        };
    }

    private String pilihan(ButtonGroup grup) {
        java.util.Enumeration<AbstractButton> e = grup.getElements();
        while (e.hasMoreElements()) {
            AbstractButton b = e.nextElement();
            if (b.isSelected()) {
                return b.getText();
            }
        }
        return "Normal";
    }

    private void pilihRadio(ButtonGroup grup, String nilai) {
        if (nilai == null || nilai.isEmpty()) {
            nilai = "Normal";
        }
        java.util.Enumeration<AbstractButton> e = grup.getElements();
        while (e.hasMoreElements()) {
            AbstractButton b = e.nextElement();
            if (b.getText().equalsIgnoreCase(nilai)) {
                b.setSelected(true);
                return;
            }
        }
    }

    private void simpan() {
        if (TNoRM.getText().trim().isEmpty()) {
            Valid.textKosong(TNoRw, "Nama Pasien");
        } else if (NmDokterPeriksa.getText().trim().isEmpty()) {
            Valid.textKosong(BtnDokterPeriksa, "Dokter Yang Memeriksa");
        } else if (NmDokterDpjp.getText().trim().isEmpty()) {
            Valid.textKosong(BtnDokterDpjp, "Dokter Penanggung Jawab Pelayanan");
        } else if (DiagnosaMedis.getText().trim().isEmpty()) {
            Valid.textKosong(DiagnosaMedis, "Diagnosa Medis");
        } else {
            StringBuilder kol = new StringBuilder("no_rawat");
            StringBuilder tanya = new StringBuilder("?");
            for (String k : KOLOM_RM20_1) {
                kol.append(",").append(k);
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
                emptTeks();
                runBackground(() -> tampil());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Gagal menyimpan : " + e.getMessage());
            }
        }
    }

    private void aksiGanti() {
        if (TNoRM.getText().trim().isEmpty()) {
            Valid.textKosong(TNoRw, "Nama Pasien");
        } else if (NmDokterPeriksa.getText().trim().isEmpty()) {
            Valid.textKosong(BtnDokterPeriksa, "Dokter Yang Memeriksa");
        } else if (NmDokterDpjp.getText().trim().isEmpty()) {
            Valid.textKosong(BtnDokterDpjp, "Dokter Penanggung Jawab Pelayanan");
        } else if (DiagnosaMedis.getText().trim().isEmpty()) {
            Valid.textKosong(DiagnosaMedis, "Diagnosa Medis");
        } else {
            StringBuilder set = new StringBuilder();
            for (int n = 0; n < KOLOM_RM20_1.length; n++) {
                if (n > 0) set.append(",");
                set.append(KOLOM_RM20_1[n]).append("=?");
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
                runBackground(() -> tampil());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Gagal mengganti : " + e.getMessage());
            }
        }
    }

    private void aksiHapus() {
        if (tbObat.getSelectedRow() != -1) {
            if (JOptionPane.showConfirmDialog(null, "Yakin data mau dihapus?", "Konfirmasi",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try (PreparedStatement pstm = koneksi.prepareStatement(
                        "delete from asesmen_medis_ranap_rm20_1 where no_rawat=?")) {
                    pstm.setString(1, tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
                    pstm.executeUpdate();
                    emptTeks();
                    runBackground(() -> tampil());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Gagal menghapus : " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Silahkan pilih data terlebih dahulu..!!");
        }
    }

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            String sql = "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                    + "pasien.tgl_lahir,asesmen_medis_ranap_rm20_1.tanggal,asesmen_medis_ranap_rm20_1.kd_dokter_periksa,dokterperiksa.nm_dokter as dokter_periksa,"
                    + "dokterdpjp.nm_dokter as dokter_dpjp,asesmen_medis_ranap_rm20_1.anamnesis,asesmen_medis_ranap_rm20_1.diagnosa_medis,asesmen_medis_ranap_rm20_1.diagnosa_banding "
                    + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "inner join asesmen_medis_ranap_rm20_1 on reg_periksa.no_rawat=asesmen_medis_ranap_rm20_1.no_rawat "
                    + "left join dokter dokterperiksa on asesmen_medis_ranap_rm20_1.kd_dokter_periksa=dokterperiksa.kd_dokter "
                    + "left join dokter dokterdpjp on asesmen_medis_ranap_rm20_1.kd_dokter_dpjp=dokterdpjp.kd_dokter "
                    + "where asesmen_medis_ranap_rm20_1.tanggal between ? and ? ";
            if (!TCari.getText().trim().isEmpty()) {
                sql += "and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or dokterperiksa.nm_dokter like ?) ";
            }
            sql += "order by asesmen_medis_ranap_rm20_1.tanggal desc";

            try (PreparedStatement pstm = koneksi.prepareStatement(sql)) {
                pstm.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                pstm.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                if (!TCari.getText().trim().isEmpty()) {
                    String param = "%" + TCari.getText().trim() + "%";
                    pstm.setString(3, param);
                    pstm.setString(4, param);
                    pstm.setString(5, param);
                    pstm.setString(6, param);
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
                            rst.getString("anamnesis"),
                            rst.getString("diagnosa_medis"),
                            rst.getString("diagnosa_banding")
                        });
                    }
                }
            }
            LCount.setText("" + tabMode.getRowCount());
        } catch (Exception e) {
            System.out.println("Notif tampil RM20-1 : " + e);
        }
    }

    public void emptTeks() {
        TglAsuhan.setDate(new Date());
        CmbAnamnesis.setSelectedIndex(0);
        THubungan.setText("");
        THubungan.setEnabled(false);
        for (RegioFisik rf : regios) {
            rf.grup.clearSelection();
            rf.txtKet.setText("");
            rf.txtKet.setVisible(false);
            if (rf.pillNormal != null) {
                rf.pillNormal.perbarui();
            }
            if (rf.pillTidakNormal != null) {
                rf.pillTidakNormal.perbarui();
            }
        }
        DiagnosaMedis.setText("");
        DiagnosaBanding.setText("");
        AdvisTerapi.setText("");
        RencanaTindakan.setText("");
        hitungProgres();
        keHalaman(0);
    }

    private void getData() {
        if (tbObat.getSelectedRow() == -1) {
            return;
        }
        TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
        TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString());
        TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 2).toString());
        TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 3).toString());
        Jk.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString());
        sinkronKartuIdentitas();

        KdDokterPeriksa.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
        NmDokterPeriksa.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
        NmDokterDpjp.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString());
        KdDokterDpjp.setText(Sequel.cariIsi(
                "select kd_dokter_dpjp from asesmen_medis_ranap_rm20_1 where no_rawat=?", TNoRw.getText()));

        Valid.SetTgl2(TglAsuhan, tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString());

        try (PreparedStatement pstm = koneksi.prepareStatement(
                "select * from asesmen_medis_ranap_rm20_1 where no_rawat=?")) {
            pstm.setString(1, TNoRw.getText());
            try (ResultSet rst = pstm.executeQuery()) {
                if (rst.next()) {
                    CmbAnamnesis.setSelectedItem(rst.getString("anamnesis"));
                    THubungan.setText(rst.getString("hubungan"));
                    THubungan.setEnabled("Aloanamnesis".equals(rst.getString("anamnesis")));

                    for (RegioFisik rf : regios) {
                        pilihRadio(rf.grup, rst.getString("fisik_" + rf.kode));
                        rf.txtKet.setText(rst.getString("fisik_" + rf.kode + "_ket"));
                        rf.txtKet.setVisible(rf.rbTidakNormal.isSelected());
                        if (rf.pillNormal != null) {
                            rf.pillNormal.perbarui();
                        }
                        if (rf.pillTidakNormal != null) {
                            rf.pillTidakNormal.perbarui();
                        }
                    }

                    DiagnosaMedis.setText(rst.getString("diagnosa_medis"));
                    DiagnosaBanding.setText(rst.getString("diagnosa_banding"));
                    AdvisTerapi.setText(rst.getString("advis_terapi"));
                    RencanaTindakan.setText(rst.getString("rencana_tindakan"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif getData RM20-1 : " + e);
        }

        hitungProgres();
        if (ChkAccor.isSelected()) {
            bangunRingkasanHtml();
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
                    if (lokasi.trim().isEmpty()) {
                        lokasi = "Mawar 3B";
                    }
                    if (lblValLokasi != null) {
                        lblValLokasi.setText(lokasi);
                    }
                    sinkronKartuIdentitas();
                }
            } catch (Exception e) {
                System.out.println("Notif isRawat RM20-1 : " + e);
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif isRawat RM20-1 : " + e);
        }
    }

    private void bangunRingkasanHtml() {
        StringBuilder h = new StringBuilder();
        h.append("<html><table width='100%' cellpadding='2' cellspacing='0'>");
        judulHtml(h, "IDENTITAS");
        barisHtml(h, "No.Rawat", TNoRw.getText());
        barisHtml(h, "No.RM / Nama", TNoRM.getText() + " / " + TPasien.getText());
        barisHtml(h, "Tgl.Lahir / Umur", TglLahir.getText() + " / " + hitungUmur());
        barisHtml(h, "J.K.", Jk.getText());
        barisHtml(h, "Tanggal & Jam", TglAsuhan.getSelectedItem() + "");
        barisHtml(h, "Dokter Pemeriksa", NmDokterPeriksa.getText());
        barisHtml(h, "DPJP", NmDokterDpjp.getText());
        barisHtml(h, "Anamnesis", CmbAnamnesis.getSelectedItem()
                + (THubungan.getText().trim().isEmpty() ? "" : " (" + THubungan.getText() + ")"));

        judulHtml(h, "I. PEMERIKSAAN FISIK (a - n)");
        for (RegioFisik rf : regios) {
            String stat = pilihan(rf.grup);
            String ket = rf.txtKet.getText().trim();
            barisHtml(h, rf.labelTeks, stat + (ket.isEmpty() ? "" : " - " + ket));
        }

        judulHtml(h, "II. ASESMEN & PROGRAM");
        barisHtml(h, "Diagnosa Medis", DiagnosaMedis.getText());
        barisHtml(h, "Diagnosa Banding", DiagnosaBanding.getText());
        barisHtml(h, "Advis Terapi", AdvisTerapi.getText());
        barisHtml(h, "Perencanaan Tindakan", RencanaTindakan.getText());

        h.append("</table></html>");
        LoadHTML.setText(h.toString());
        LoadHTML.setCaretPosition(0);
    }

    private void judulHtml(StringBuilder h, String judul) {
        h.append("<tr class='isi'><td colspan='2' bgcolor='#FFFAF8'><b>").append(judul).append("</b></td></tr>");
    }

    private void barisHtml(StringBuilder h, String label, String nilai) {
        h.append("<tr class='isi'><td width='35%' valign='top'>").append(label)
         .append("</td><td valign='top'>").append(teksAman(nilai).replace("\n", "<br>")).append("</td></tr>");
    }

    private String teksAman(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    // =================================================================
    //  CETAK REPORT
    // =================================================================

    private void cetakTerpilih() {
        if (tbObat.getSelectedRow() <= -1) {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
            return;
        }
        String[] pilihan = {"Lembar", "PDF"};
        Object mode = JOptionPane.showInputDialog(null, "Silahkan pilih keluaran..!!", "Cetak",
                JOptionPane.QUESTION_MESSAGE, null, pilihan, pilihan[0]);
        if (mode == null) {
            return;
        }

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
                + "reg_periksa.tgl_registrasi,reg_periksa.jam_reg,"
                + "dokterperiksa.nm_dokter as nm_dokter_periksa,dokterperiksa.no_ijn_praktek as ijin_periksa,"
                + "dokterdpjp.nm_dokter as nm_dokter_dpjp,dokterdpjp.no_ijn_praktek as ijin_dpjp "
                + "from asesmen_medis_ranap_rm20_1 "
                + "inner join reg_periksa on asesmen_medis_ranap_rm20_1.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join dokter dokterperiksa on asesmen_medis_ranap_rm20_1.kd_dokter_periksa=dokterperiksa.kd_dokter "
                + "left join dokter dokterdpjp on asesmen_medis_ranap_rm20_1.kd_dokter_dpjp=dokterdpjp.kd_dokter "
                + "where asesmen_medis_ranap_rm20_1.no_rawat='" + noRawat + "'";
    }

    // =================================================================
    //  API PUBLIK & DIALOG DOKTER
    // =================================================================

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        isRawat();
    }

    public void setTampil() {
        TabRawat.setSelectedIndex(1);
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
                JOptionPane.showMessageDialog(null, "User login bukan Dokter...!!");
            }
        }
    }

    private void bukaCariDokter(boolean untukDpjp) {
        pilihDpjp = untukDpjp;
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

    private void runBackground(Runnable tugas) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            executor.submit(() -> {
                try {
                    tugas.run();
                } finally {
                    SwingUtilities.invokeLater(() -> setCursor(Cursor.getDefaultCursor()));
                }
            });
        } catch (RejectedExecutionException e) {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
