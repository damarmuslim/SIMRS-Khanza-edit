package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
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
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariDokter;
import widget.ThemeConfig;

/**
 * RINGKASAN PASIEN MASUK KELUAR (RM 17-01) - RSUD Demang Sepulau Raya.
 *
 * <p>Dibangun ulang dengan Swing/JasperReports sesuai pola design handoff
 * formulir rekam medis modern: Dark Teal Header Bar (#123C3A), Card Identitas
 * Pasien (262px, #F7F8F5), 4 Kolom Header Status Rawat, Left Navigation Rail (300px)
 * dengan Progres Bar Isian Wajib & Tally Seksi, Card Seksi I s.d VII berbingkai crisp,
 * RadioPill interaktif, dan Footer tombol standar Khanza.</p>
 */
public final class RMRingkasanPasienMasukKeluar extends javax.swing.JDialog {

    private static final String[] KOLOM_RM17 = {
        "nama_suami", "nama_istri", "nama_ayah",
        "masuk_melalui", "dikirim_oleh", "kasus_polisi",
        "diagnose_sementara", "alergi_terhadap",
        "diagnose_akhir", "komplikasi", "diagnose_lain", "kode_icd",
        "tindakan_1", "tindakan_1_tanggal", "tindakan_1_dokter",
        "tindakan_2", "tindakan_2_tanggal", "tindakan_2_dokter",
        "tindakan_3", "tindakan_3_tanggal", "tindakan_3_dokter",
        "tindakan_4", "tindakan_4_tanggal", "tindakan_4_dokter",
        "meninggal_tanggal", "meninggal_jam",
        "keadaan_keluar", "rujuk_ke", "cara_keluar_meninggal",
        "kunjungan_ulang_tanggal", "ke_klinik", "kd_dokter_merawat",
        "sk_penyakit_a", "sk_lama_a", "sk_penyakit_b", "sk_lama_b",
        "sk_penyakit_c", "sk_lama_c", "sk_penyakit_lain", "sk_lama_lain",
        "rp_macam", "rp_cara_kejadian", "rp_sifat_jenis",
        "km_janin_lahir_mati", "km_sebab",
        "pk_persalinan", "pk_kehamilan",
        "op_dilakukan", "op_jam",
        "sk_tanggal", "kd_pemberi_keterangan"
    };

    private static final String KD_POLI_IGD = "IGDK";

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
            int w = Math.max(92, text.length() * 9 + 32);
            setPreferredSize(new Dimension(w, 30));

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

    private static final class ScrollablePanel extends widget.PanelBiasa implements javax.swing.Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 48;
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

    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;
    private DlgCariDokter dokter;
    private int tujuanPilihDokter = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;

    // ---------- Kerangka Utama ----------
    private widget.InternalFrame internalFrame1, internalFrame2, internalFrame3;
    private JPanel topBarGelap;
    private widget.PanelBiasa barPasienMedis, railMedis;
    private widget.ScrollPane scrollRailMedis, scrollSeksiMedis;
    private widget.panelisi panelGlass8, panelGlass9;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnPrint, BtnAll, BtnKeluar;

    // ---------- Identitas Pasien (Display Card & Hidden Fields) ----------
    private widget.TextBox TNoRw, TNoRM, TPasien, TmpLahir, TglLahir, TUsia, Jk;
    private widget.TextBox TAlamat, TTelepon, TStatus, TAgama, TPekerjaan, TPendidikan;
    private widget.TextBox TMasukTgl, TMasukJam, TRuang, TKeluarTgl, TKeluarJam;
    private widget.TextBox TNamaIbu;

    private widget.Label lblKartuNama, lblKartuInisial, lblKartuInfo;
    private JLabel lblValRM, lblValRawat, lblValLokasi;

    // ---------- 4 Header Fields ----------
    private widget.TextBox txtHdrRuang, txtHdrMasuk, txtHdrKeluar, txtHdrLama;

    // ---------- Panel Kiri (Rail) ----------
    private widget.Label lblProgresPersen, lblProgresCaption, lblFooterNote;
    private JProgressBar pbProgres;
    private final widget.PanelBiasa[] barisRail = new widget.PanelBiasa[7];
    private final JLabel[] lblRailTally = new JLabel[7];
    private widget.Button btnToggleMode;
    private boolean satuHalaman = true;
    private int halamanAktif = 0;

    // ---------- Seksi Cards & Tally Badges ----------
    private final widget.PanelBiasa[] kartuSeksi = new widget.PanelBiasa[7];
    private final JLabel[] lblTallySeksi = new JLabel[7];
    private ScrollablePanel FormInput;

    // ---------- Identitas keluarga ----------
    private widget.TextBox NamaSuami, NamaIstri, NamaAyah;

    // ---------- Masuk & pengirim ----------
    private widget.ComboBox MasukMelalui;
    private widget.TextBox DikirimOleh;
    private widget.RadioButton RbKasusPolisiYa, RbKasusPolisiTidak;
    private final ButtonGroup bgKasusPolisi = new ButtonGroup();
    private RadioPill pillKasusPolisiYa, pillKasusPolisiTidak;

    // ---------- Diagnosa ----------
    private widget.TextBox DiagnoseSementara, AlergiTerhadap;
    private widget.TextBox DiagnoseAkhir, Komplikasi, DiagnoseLain, KodeIcd;

    // ---------- Keadaan Keluar ----------
    private widget.RadioButton RbKeluarSembuh, RbKeluarDirujuk, RbKeluarPulangPaksa, RbKeluarMeninggal;
    private final ButtonGroup bgKeadaanKeluar = new ButtonGroup();
    private RadioPill pillKeluarSembuh, pillKeluarDirujuk, pillKeluarPulangPaksa, pillKeluarMeninggal;
    private widget.ComboBox CaraKeluarMeninggal;
    private widget.TextBox RujukKe, KeKlinik;
    private widget.RadioButton RbKunjunganTidak, RbKunjunganYa;
    private final ButtonGroup bgKunjunganUlang = new ButtonGroup();
    private RadioPill pillKunjunganTidak, pillKunjunganYa;
    private JPanel pnlExtraKunjungan;
    private widget.Tanggal KunjunganUlangTgl;
    private widget.TextBox KdDokterMerawat, NmDokterMerawat, TNipDokter;
    private widget.Button BtnDokterMerawat;

    // ---------- Tindakan ----------
    private final widget.TextBox[] Tindakan = new widget.TextBox[4];
    private final widget.Tanggal[] TindakanTgl = new widget.Tanggal[4];
    private final widget.TextBox[] TindakanKdDokter = new widget.TextBox[4];
    private final widget.TextBox[] TindakanNmDokter = new widget.TextBox[4];
    private final widget.Button[] BtnTindakanDokter = new widget.Button[4];

    // ---------- Halaman 2 : Sebab Kematian (kondisional) ----------
    private widget.CekBox ChkMeninggal;
    private widget.Tanggal MeninggalTgl;
    private widget.TextBox SkPenyakitA, SkLamaA, SkPenyakitB, SkLamaB, SkPenyakitC, SkLamaC;
    private widget.TextBox SkPenyakitLain, SkLamaLain;
    private widget.TextBox RpMacam, RpCaraKejadian, RpSifatJenis;
    private widget.RadioButton RbKmYa, RbKmTidak, RbPkPersalinanYa, RbPkPersalinanTidak,
            RbPkKehamilanYa, RbPkKehamilanTidak, RbOpYa, RbOpTidak;
    private final ButtonGroup bgKm = new ButtonGroup();
    private final ButtonGroup bgPkPersalinan = new ButtonGroup();
    private final ButtonGroup bgPkKehamilan = new ButtonGroup();
    private final ButtonGroup bgOp = new ButtonGroup();
    private RadioPill pillKmYa, pillKmTidak, pillPkPersalinanYa, pillPkPersalinanTidak,
            pillPkKehamilanYa, pillPkKehamilanTidak, pillOpYa, pillOpTidak;
    private widget.TextBox KmSebab, OpJam;
    private widget.Tanggal SkTanggal;
    private widget.TextBox KdPemberiKeterangan, NmPemberiKeterangan;
    private widget.Button BtnPemberiKeterangan;

    // ---------- Tab Data ----------
    private widget.ScrollPane Scroll, ScrollHTML;
    private widget.Table tbObat;
    private widget.Tanggal DTPCari1, DTPCari2;
    private widget.TextBox TCari, TNoRM1, TPasien1;
    private widget.Button BtnCari, BtnPrint1;
    private widget.Label LCount;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JMenuItem MnCetakRingkasan;
    private widget.PanelBiasa PanelAccor;
    private widget.panelisi FormMenu;
    private widget.CekBox ChkAccor;
    private widget.editorpane LoadHTML;

    public RMRingkasanPasienMasukKeluar(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.RM", "Nama Pasien", "Tgl.Lahir", "J.K.",
            "Kode Dokter", "Dokter Yang Merawat", "Keadaan Keluar", "Diagnose Akhir"
        }) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
        };

        rakitKerangka();
        rakitHeaderDanBanner();
        rakitRailNavigasi();
        rakitHalamanSeksi();
        rakitTabData();

        widget.IconStyle.pasang(this);
        bersihkanIkonRadio(getContentPane());

        ChkAccor.setSelected(false);
        isMenu();
        setSize(1280, 720);

        pasangListenerProgres();
        bukaBagian(0);
        hitungProgres();
    }

    // =================================================================
    //  KERANGKA UTAMA
    // =================================================================

    private void rakitKerangka() {
        internalFrame1 = new widget.InternalFrame();
        internalFrame1.setBorder(null);
        internalFrame1.setLayout(new BorderLayout());

        // 1. Dark Teal Top Bar (#123C3A)
        topBarGelap = new JPanel(new BorderLayout());
        topBarGelap.setBackground(new Color(0x12, 0x3C, 0x3A));
        topBarGelap.setPreferredSize(new Dimension(0, 34));
        topBarGelap.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JPanel pnlKiriAtas = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlKiriAtas.setOpaque(false);

        JLabel lblJudulTop = new JLabel("Ringkasan Pasien Masuk Keluar");
        lblJudulTop.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblJudulTop.setForeground(new Color(0xDF, 0xE9, 0xE6));

        JLabel lblBadgeRM = new JLabel("RM 17-01");
        lblBadgeRM.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadgeRM.setForeground(new Color(0x8F, 0xB5, 0xAE));
        lblBadgeRM.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2C, 0x5A, 0x56), 1),
                BorderFactory.createEmptyBorder(1, 6, 1, 6)));

        pnlKiriAtas.add(lblJudulTop);
        pnlKiriAtas.add(lblBadgeRM);

        JLabel lblRsud = new JLabel("RSUD Demang Sepulau Raya");
        lblRsud.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRsud.setForeground(new Color(0x8F, 0xB5, 0xAE));

        topBarGelap.add(pnlKiriAtas, BorderLayout.WEST);
        topBarGelap.add(lblRsud, BorderLayout.EAST);
        internalFrame1.add(topBarGelap, BorderLayout.PAGE_START);

        // 2. Footer Bar Standar Khanza (panelGlass8)
        panelGlass8 = new widget.panelisi();
        panelGlass8.setName("panelGlass8");
        panelGlass8.setPreferredSize(new Dimension(44, 54));
        panelGlass8.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        BtnSimpan = tombolFooter("Simpan", "/picture/save-16x16.png", 'S');
        BtnSimpan.addActionListener(evt -> simpan());
        BtnBatal = tombolFooter("Baru", "/picture/Cancel-2-16x16.png", 'B');
        BtnBatal.addActionListener(evt -> emptTeks());
        BtnHapus = tombolFooter("Hapus", "/picture/stop_f2.png", 'H');
        BtnHapus.addActionListener(evt -> aksiHapus());
        BtnEdit = tombolFooter("Ganti", "/picture/inventaris.png", 'G');
        BtnEdit.addActionListener(evt -> aksiGanti());
        BtnPrint = tombolFooter("Cetak", "/picture/b_print.png", 'T');
        BtnPrint.addActionListener(evt -> cetakTerpilih());
        BtnAll = tombolFooter("Semua", "/picture/Search-16x16.png", 'M');
        BtnAll.addActionListener(evt -> { TCari.setText(""); runBackground(() -> tampil()); });
        BtnKeluar = tombolFooter("Keluar", "/picture/exit.png", 'K');
        BtnKeluar.addActionListener(evt -> dispose());

        panelGlass8.add(BtnSimpan);
        panelGlass8.add(BtnBatal);
        panelGlass8.add(BtnHapus);
        panelGlass8.add(BtnEdit);
        panelGlass8.add(BtnPrint);
        panelGlass8.add(BtnAll);
        panelGlass8.add(BtnKeluar);
        internalFrame1.add(panelGlass8, BorderLayout.PAGE_END);

        // 3. Underlined TabRawat
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

        TabRawat.addTab("Formulir Ringkasan", internalFrame2);
        internalFrame1.add(TabRawat, BorderLayout.CENTER);
        getContentPane().add(internalFrame1, BorderLayout.CENTER);
    }

    private widget.Button tombolFooter(String teks, String ikon, char mnemonic) {
        widget.Button b = new widget.Button();
        try {
            b.setIcon(new ImageIcon(getClass().getResource(ikon)));
        } catch (Exception ignored) {}
        b.setMnemonic(mnemonic);
        b.setText(teks);
        b.setToolTipText("Alt+" + mnemonic);
        b.setPreferredSize(new Dimension(100, 30));
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

        // Field Tersembunyi Master
        TNoRw = new widget.TextBox(); TNoRw.setVisible(false);
        TNoRM = new widget.TextBox(); TNoRM.setVisible(false);
        TPasien = new widget.TextBox(); TPasien.setVisible(false);
        TmpLahir = new widget.TextBox(); TmpLahir.setVisible(false);
        TglLahir = new widget.TextBox(); TglLahir.setVisible(false);
        TUsia = new widget.TextBox(); TUsia.setVisible(false);
        Jk = new widget.TextBox(); Jk.setVisible(false);
        TAlamat = new widget.TextBox(); TAlamat.setVisible(false);
        TTelepon = new widget.TextBox(); TTelepon.setVisible(false);
        TStatus = new widget.TextBox(); TStatus.setVisible(false);
        TAgama = new widget.TextBox(); TAgama.setVisible(false);
        TPekerjaan = new widget.TextBox(); TPekerjaan.setVisible(false);
        TPendidikan = new widget.TextBox(); TPendidikan.setVisible(false);
        TNamaIbu = new widget.TextBox(); TNamaIbu.setVisible(false);
        TMasukTgl = new widget.TextBox(); TMasukTgl.setVisible(false);
        TMasukJam = new widget.TextBox(); TMasukJam.setVisible(false);
        TRuang = new widget.TextBox(); TRuang.setVisible(false);
        TKeluarTgl = new widget.TextBox(); TKeluarTgl.setVisible(false);
        TKeluarJam = new widget.TextBox(); TKeluarJam.setVisible(false);

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
        lblKartuInisial.setForeground(new Color(0x0D, 0x7A, 0x70));
        lblKartuInisial.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKartuInisial.setPreferredSize(new Dimension(42, 42));
        lblKartuInisial.setBorder(BorderFactory.createLineBorder(new Color(0x0D, 0x7A, 0x70, 0x40)));

        JPanel pnlNamaDetail = new JPanel();
        pnlNamaDetail.setOpaque(false);
        pnlNamaDetail.setLayout(new BoxLayout(pnlNamaDetail, BoxLayout.Y_AXIS));

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
        lblValLokasi = new JLabel("—");

        pnlNomorDetail.add(buatBarisNomorPanel("No. RM", lblValRM));
        pnlNomorDetail.add(buatBarisNomorPanel("No. Rawat", lblValRawat));
        pnlNomorDetail.add(buatBarisNomorPanel("Lokasi", lblValLokasi));

        GridBagConstraints kc = new GridBagConstraints();
        kc.gridx = 0; kc.gridy = 0; kc.fill = GridBagConstraints.HORIZONTAL; kc.weightx = 1.0;
        kartuId.add(pnlAtasId, kc);
        kc.gridy = 1; kc.insets = new Insets(8, 0, 8, 0);
        kartuId.add(pnlAvatarNama, kc);
        kc.gridy = 2; kc.insets = new Insets(0, 0, 0, 0);
        kartuId.add(pnlNomorDetail, kc);

        // 2. Grid 4 Kolom Header Status Rawat (Kanan)
        JPanel gridHeader = new JPanel(new java.awt.GridLayout(1, 4, 14, 0));
        gridHeader.setOpaque(false);

        txtHdrRuang = kotakHeader();
        txtHdrRuang.setEditable(false);
        txtHdrMasuk = kotakHeader();
        txtHdrMasuk.setEditable(false);
        txtHdrKeluar = kotakHeader();
        txtHdrKeluar.setEditable(false);
        txtHdrLama = kotakHeader();
        txtHdrLama.setEditable(false);

        gridHeader.add(buatKotakHeaderItem("DIRAWAT DI RUANG", txtHdrRuang, "/picture/lock.png", true));
        gridHeader.add(buatKotakHeaderItem("MASUK (TANGGAL & JAM)", txtHdrMasuk, "/picture/lock.png", true));
        gridHeader.add(buatKotakHeaderItem("KELUAR (TANGGAL & JAM)", txtHdrKeluar, "/picture/calendar.png", false));
        gridHeader.add(buatKotakHeaderItem("LAMA DIRAWAT", txtHdrLama, "/picture/011.png", true));

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0; bc.gridy = 0; bc.anchor = GridBagConstraints.WEST; bc.insets = new Insets(0, 0, 0, 16);
        barPasienMedis.add(kartuId, bc);

        bc.gridx = 1; bc.fill = GridBagConstraints.BOTH; bc.weightx = 1.0; bc.weighty = 1.0; bc.insets = new Insets(0, 0, 0, 0);
        barPasienMedis.add(gridHeader, bc);

        internalFrame2.add(barPasienMedis, BorderLayout.NORTH);
    }

    private JPanel buatBarisNomorPanel(String label, JLabel valLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(0x9A, 0xA1, 0x9C));
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        valLabel.setForeground(new Color(0x5D, 0x64, 0x5F));
        valLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(l, BorderLayout.WEST);
        p.add(valLabel, BorderLayout.EAST);
        return p;
    }

    private widget.TextBox kotakHeader() {
        widget.TextBox t = new widget.TextBox();
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.putClientProperty("JComponent.roundRect", false);
        t.putClientProperty("Component.arc", 0);
        t.putClientProperty("ModernStyle.active", true);
        t.setBorder(null);
        t.setOpaque(false);
        return t;
    }

    private JPanel buatKotakHeaderItem(String label, widget.TextBox field, String ikonPath, boolean locked) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(0x9A, 0xA1, 0x9C));

        JPanel box = new JPanel(new BorderLayout(6, 0));
        box.setPreferredSize(new Dimension(100, 32));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        box.setBackground(locked ? new Color(0xFB, 0xFC, 0xFA) : Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCF, 0xD3, 0xCE), 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 8)));

        field.setOpaque(false);
        field.setBorder(null);
        box.add(field, BorderLayout.CENTER);

        JLabel iconLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(ikonPath));
            Image scaled = icon.getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception ignored) {}
        box.add(iconLabel, BorderLayout.EAST);

        wrap.add(l);
        wrap.add(Box.createVerticalStrut(4));
        wrap.add(box);
        return wrap;
    }

    // =================================================================
    //  RAIL NAVIGASI KIRI (300px)
    // =================================================================

    private void rakitRailNavigasi() {
        railMedis = new widget.PanelBiasa();
        railMedis.setLayout(new BoxLayout(railMedis, BoxLayout.Y_AXIS));
        railMedis.setOpaque(false);
        railMedis.setPreferredSize(new Dimension(300, 600));
        railMedis.setMinimumSize(new Dimension(300, 600));
        railMedis.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));

        // 1. Card Isian Wajib (Progress Bar)
        widget.PanelBiasa cardProgres = new widget.PanelBiasa();
        cardProgres.setBackground(Color.WHITE);
        cardProgres.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDC, 0xDF, 0xDA), 1),
                BorderFactory.createEmptyBorder(14, 14, 12, 14)));
        cardProgres.setLayout(new BoxLayout(cardProgres, BoxLayout.Y_AXIS));
        cardProgres.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardProgres.setPreferredSize(new Dimension(300, 95));
        cardProgres.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        JPanel pnlHeadProgres = new JPanel(new BorderLayout());
        pnlHeadProgres.setOpaque(false);
        lblProgresCaption = new widget.Label();
        lblProgresCaption.setText("ISIAN WAJIB");
        lblProgresCaption.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblProgresCaption.setForeground(new Color(0x8B, 0x92, 0x8D));

        lblProgresPersen = new widget.Label();
        lblProgresPersen.setText("0%");
        lblProgresPersen.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblProgresPersen.setForeground(new Color(0x1C, 0x1F, 0x1D));

        pnlHeadProgres.add(lblProgresCaption, BorderLayout.WEST);
        pnlHeadProgres.add(lblProgresPersen, BorderLayout.EAST);

        pbProgres = new JProgressBar(0, 100);
        pbProgres.setValue(0);
        pbProgres.setPreferredSize(new Dimension(270, 6));
        pbProgres.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        pbProgres.setForeground(new Color(0x0D, 0x7A, 0x70));
        pbProgres.setBackground(new Color(0xEA, 0xEC, 0xE8));
        pbProgres.setBorder(null);

        lblFooterNote = new widget.Label();
        lblFooterNote.setText("0/5 isian wajib · 9 opsional terisi");
        lblFooterNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooterNote.setForeground(new Color(0x9A, 0xA1, 0x9C));

        cardProgres.add(pnlHeadProgres);
        cardProgres.add(Box.createVerticalStrut(8));
        cardProgres.add(pbProgres);
        cardProgres.add(Box.createVerticalStrut(8));
        cardProgres.add(lblFooterNote);

        // 2. Card Bagian Pengkajian
        widget.PanelBiasa cardSeksi = new widget.PanelBiasa();
        cardSeksi.setBackground(Color.WHITE);
        cardSeksi.setBorder(BorderFactory.createLineBorder(new Color(0xDC, 0xDF, 0xDA), 1));
        cardSeksi.setLayout(new BoxLayout(cardSeksi, BoxLayout.Y_AXIS));
        cardSeksi.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardSeksi.setPreferredSize(new Dimension(300, 310));
        cardSeksi.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel pnlTitleSeksi = new JPanel(new BorderLayout());
        pnlTitleSeksi.setOpaque(false);
        pnlTitleSeksi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEC, 0xEE, 0xEA)),
                BorderFactory.createEmptyBorder(11, 14, 11, 14)));
        JLabel lblTitleSeksi = new JLabel("BAGIAN PENGKAJIAN");
        lblTitleSeksi.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitleSeksi.setForeground(new Color(0x8B, 0x92, 0x8D));
        pnlTitleSeksi.add(lblTitleSeksi, BorderLayout.WEST);
        cardSeksi.add(pnlTitleSeksi);

        String[] roman = {"I", "II", "III", "IV", "V", "VI", "VII"};
        String[] titles = {
            "Identitas Pasien", "Nama Lengkap Keluarga", "Masuk Melalui / Dikirim",
            "Diagnosa", "Keadaan Keluar", "Tindakan", "Sebab Kematian"
        };

        for (int k = 0; k < 7; k++) {
            final int idx = k;
            barisRail[k] = new widget.PanelBiasa();
            barisRail[k].setLayout(new BorderLayout(10, 0));
            barisRail[k].setOpaque(true);
            barisRail[k].setBackground(Color.WHITE);
            barisRail[k].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 1, 0, new Color(0xF4, 0xF5, 0xF2)),
                    BorderFactory.createEmptyBorder(9, 11, 9, 14)));
            barisRail[k].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel lblNum = new JLabel(roman[k]);
            lblNum.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblNum.setForeground(new Color(0xB3, 0xB8, 0xB3));
            lblNum.setPreferredSize(new Dimension(24, 16));

            JLabel lblNama = new JLabel(titles[k]);
            lblNama.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblNama.setForeground(new Color(0x3D, 0x44, 0x41));

            lblRailTally[k] = new JLabel("–");
            lblRailTally[k].setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblRailTally[k].setForeground(new Color(0x9A, 0xA1, 0x9C));

            barisRail[k].add(lblNum, BorderLayout.WEST);
            barisRail[k].add(lblNama, BorderLayout.CENTER);
            barisRail[k].add(lblRailTally[k], BorderLayout.EAST);

            barisRail[k].addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    bukaBagian(idx);
                }
            });

            cardSeksi.add(barisRail[k]);
        }

        // 3. Tombol Toggle Mode
        btnToggleMode = new widget.Button();
        btnToggleMode.setText("Tampilkan Semua Bagian");
        btnToggleMode.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnToggleMode.setForeground(new Color(0x0D, 0x7A, 0x70));
        btnToggleMode.setBackground(Color.WHITE);
        btnToggleMode.setPreferredSize(new Dimension(300, 34));
        btnToggleMode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnToggleMode.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnToggleMode.setBorder(BorderFactory.createLineBorder(new Color(0x0D, 0x7A, 0x70, 0x66), 1));
        btnToggleMode.addActionListener(evt -> toggleModeTampil());

        railMedis.add(cardProgres);
        railMedis.add(Box.createVerticalStrut(12));
        railMedis.add(cardSeksi);
        railMedis.add(Box.createVerticalStrut(12));
        railMedis.add(btnToggleMode);
        railMedis.add(Box.createVerticalGlue());
    }

    // =================================================================
    //  HALAMAN SEKSI & KARTU FORM
    // =================================================================

    private void rakitHalamanSeksi() {
        JPanel wadahUtama = new JPanel(new BorderLayout(20, 0));
        wadahUtama.setOpaque(true);
        wadahUtama.setBackground(Color.WHITE);
        wadahUtama.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        wadahUtama.add(railMedis, BorderLayout.WEST);

        FormInput = new ScrollablePanel();
        FormInput.setLayout(new GridBagLayout());
        FormInput.setOpaque(false);
        FormInput.setBackground(Color.WHITE);

        // Membangun ke-7 Seksi
        kartuSeksi[0] = buatSeksi1Identitas();
        kartuSeksi[1] = buatSeksi2Keluarga();
        kartuSeksi[2] = buatSeksi3Masuk();
        kartuSeksi[3] = buatSeksi4Diagnosa();
        kartuSeksi[4] = buatSeksi5Keluar();
        kartuSeksi[5] = buatSeksi6Tindakan();
        kartuSeksi[6] = buatSeksi7Kematian();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        for (int k = 0; k < 7; k++) {
            c.gridy = k;
            c.insets = new Insets(0, 0, 16, 0);
            FormInput.add(kartuSeksi[k], c);
        }

        // Glue di baris ke-7 untuk menyerap semua sisa ruang vertikal ke bawah,
        // sehingga kartu-kartu di atasnya selalu pas di atas (flush ke NORTH)
        c.gridy = 7;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        FormInput.add(Box.createGlue(), c);

        scrollSeksiMedis = new widget.ScrollPane();
        scrollSeksiMedis.setViewportView(FormInput);
        scrollSeksiMedis.setBorder(null);
        scrollSeksiMedis.setOpaque(true);
        scrollSeksiMedis.setBackground(Color.WHITE);
        scrollSeksiMedis.getViewport().setOpaque(true);
        scrollSeksiMedis.getViewport().setBackground(Color.WHITE);
        scrollSeksiMedis.getVerticalScrollBar().setUnitIncrement(16);

        wadahUtama.add(scrollSeksiMedis, BorderLayout.CENTER);
        internalFrame2.setBackground(Color.WHITE);
        internalFrame2.add(wadahUtama, BorderLayout.CENTER);
    }

    private widget.PanelBiasa buatKartuSeksi(int idx, String roman, String title, String subtitle, JPanel isiPanel) {
        widget.PanelBiasa card = new widget.PanelBiasa();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(0xDC, 0xDF, 0xDA), 1));

        // Header
        JPanel pnlHead = new JPanel(new BorderLayout(14, 0));
        pnlHead.setBackground(new Color(0xFB, 0xFC, 0xFA));
        pnlHead.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEC, 0xEE, 0xEA)),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel lblBadge = new JLabel(roman, SwingConstants.CENTER);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(new Color(0x0D, 0x7A, 0x70));
        lblBadge.setPreferredSize(new Dimension(26, 26));

        JPanel pnlJudul = new JPanel();
        pnlJudul.setOpaque(false);
        pnlJudul.setLayout(new BoxLayout(pnlJudul, BoxLayout.Y_AXIS));

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblT.setForeground(new Color(0x1C, 0x1F, 0x1D));

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(0x9A, 0xA1, 0x9C));

        pnlJudul.add(lblT);
        pnlJudul.add(Box.createVerticalStrut(2));
        pnlJudul.add(lblSub);

        JPanel pnlKiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlKiri.setOpaque(false);
        pnlKiri.add(lblBadge);
        pnlKiri.add(pnlJudul);

        lblTallySeksi[idx] = new JLabel("–");
        lblTallySeksi[idx].setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTallySeksi[idx].setOpaque(true);
        lblTallySeksi[idx].setBorder(BorderFactory.createEmptyBorder(4, 9, 4, 9));

        pnlHead.add(pnlKiri, BorderLayout.WEST);
        pnlHead.add(lblTallySeksi[idx], BorderLayout.EAST);
        card.add(pnlHead, BorderLayout.NORTH);

        // Body: isiPanel dibungkus di BorderLayout.NORTH agar selalu menempel pas di atas card
        JPanel pnlBody = new JPanel(new BorderLayout());
        pnlBody.setOpaque(false);
        pnlBody.add(isiPanel, BorderLayout.NORTH);
        card.add(pnlBody, BorderLayout.CENTER);

        // Stepper Footer
        JPanel pnlStepper = new JPanel(new BorderLayout());
        pnlStepper.setBackground(new Color(0xFB, 0xFC, 0xFA));
        pnlStepper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xEC, 0xEE, 0xEA)),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        String[] titles = {
            "Identitas Pasien", "Nama Lengkap Keluarga", "Masuk Melalui / Dikirim",
            "Diagnosa", "Keadaan Keluar", "Tindakan", "Sebab Kematian"
        };

        widget.Button btnPrev = new widget.Button();
        if (idx > 0) {
            btnPrev.setText("‹ " + titles[idx - 1]);
            btnPrev.setEnabled(true);
        } else {
            btnPrev.setText("‹ Awal");
            btnPrev.setEnabled(false);
        }
        btnPrev.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnPrev.setPreferredSize(new Dimension(180, 30));
        btnPrev.addActionListener(evt -> bukaBagian(idx - 1));

        JLabel lblStep = new JLabel("Bagian " + (idx + 1) + " dari 7", SwingConstants.CENTER);
        lblStep.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStep.setForeground(new Color(0x9A, 0xA1, 0x9C));

        widget.Button btnNext = new widget.Button();
        if (idx < 6) {
            btnNext.setText(titles[idx + 1] + " ›");
            btnNext.setEnabled(true);
        } else {
            btnNext.setText("Akhir ›");
            btnNext.setEnabled(false);
        }
        btnNext.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnNext.setPreferredSize(new Dimension(180, 30));
        btnNext.addActionListener(evt -> bukaBagian(idx + 1));

        pnlStepper.add(btnPrev, BorderLayout.WEST);
        pnlStepper.add(lblStep, BorderLayout.CENTER);
        pnlStepper.add(btnNext, BorderLayout.EAST);

        card.add(pnlStepper, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buatFormGridPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(14, 24, 16, 24));
        return p;
    }

    private void tambahBarisForm(JPanel pnl, int row, String labelTeks, boolean wajib, Component kontrol) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = row;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(5, 0, 5, 20);

        JLabel lbl = new JLabel(labelTeks + (wajib ? " *" : ""));
        lbl.setFont(new Font("Segoe UI", wajib ? Font.BOLD : Font.PLAIN, 12));
        lbl.setForeground(wajib ? new Color(0x1C, 0x1F, 0x1D) : new Color(0x3D, 0x44, 0x41));
        lbl.setPreferredSize(new Dimension(210, 32));
        pnl.add(lbl, c);

        c.gridx = 1; c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(5, 0, 5, 0);
        pnl.add(kontrol, c);
    }

    private widget.TextBox buatTextBox(int lebar, boolean readOnly, String placeholder) {
        widget.TextBox t = new widget.TextBox();
        t.setPreferredSize(new Dimension(lebar, 32));
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setEditable(!readOnly);
        t.putClientProperty("JComponent.roundRect", false);
        t.putClientProperty("Component.arc", 0);
        t.putClientProperty("ModernStyle.active", true);
        if (readOnly) {
            t.setBackground(new Color(0xF4, 0xF6, 0xF2));
            t.setForeground(new Color(0x5D, 0x64, 0x5F));
        } else {
            t.setBackground(Color.WHITE);
            t.setForeground(new Color(0x1C, 0x1F, 0x1D));
        }
        if (placeholder != null && !placeholder.isEmpty()) {
            t.setPlaceholder(placeholder);
        }
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCF, 0xD3, 0xCE), 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        return t;
    }

    // =================================================================
    //  SEKSI I S.D VII
    // =================================================================

    // 1. Identitas Pasien (Read-Only)
    private widget.PanelBiasa buatSeksi1Identitas() {
        JPanel p = buatFormGridPanel();
        widget.TextBox txtTmpTgl = buatTextBox(360, true, "—");
        widget.TextBox txtUsia = buatTextBox(360, true, "—");
        widget.TextBox txtJk = buatTextBox(360, true, "—");
        widget.TextBox txtAlamat = buatTextBox(520, true, "—");
        widget.TextBox txtTelp = buatTextBox(360, true, "—");
        widget.TextBox txtAgama = buatTextBox(360, true, "—");
        widget.TextBox txtPekerjaan = buatTextBox(360, true, "—");
        widget.TextBox txtPendidikan = buatTextBox(360, true, "—");
        widget.TextBox txtIbu = buatTextBox(360, true, "—");

        tambahBarisForm(p, 0, "Tempat / Tgl Lahir", false, txtTmpTgl);
        tambahBarisForm(p, 1, "Usia", false, txtUsia);
        tambahBarisForm(p, 2, "L / P", false, txtJk);
        tambahBarisForm(p, 3, "Alamat", false, txtAlamat);
        tambahBarisForm(p, 4, "Telepon", false, txtTelp);
        tambahBarisForm(p, 5, "Status / Agama", false, txtAgama);
        tambahBarisForm(p, 6, "Pekerjaan", false, txtPekerjaan);
        tambahBarisForm(p, 7, "Pendidikan", false, txtPendidikan);
        tambahBarisForm(p, 8, "Nama Lengkap Ibu", false, txtIbu);

        // Binding perubahan master ke input display
        TglLahir.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                txtTmpTgl.setText(TmpLahir.getText() + (TglLahir.getText().isEmpty() ? "" : ", " + TglLahir.getText()));
                txtUsia.setText(TUsia.getText());
                txtJk.setText(Jk.getText());
                txtAlamat.setText(TAlamat.getText());
                txtTelp.setText(TTelepon.getText());
                txtAgama.setText(TStatus.getText() + (TAgama.getText().isEmpty() ? "" : " / " + TAgama.getText()));
                txtPekerjaan.setText(TPekerjaan.getText());
                txtPendidikan.setText(TPendidikan.getText());
                txtIbu.setText(TNamaIbu.getText());
            }
        });

        return buatKartuSeksi(0, "I", "Identitas Pasien", "Otomatis dari pendaftaran", p);
    }

    // 2. Nama Lengkap Keluarga
    private widget.PanelBiasa buatSeksi2Keluarga() {
        JPanel p = buatFormGridPanel();
        NamaSuami = buatTextBox(360, false, "Nama lengkap suami");
        NamaAyah = buatTextBox(360, false, "Nama lengkap ayah");
        NamaIstri = buatTextBox(360, false, "Nama lengkap istri");

        tambahBarisForm(p, 0, "Nama Lengkap Suami", false, NamaSuami);
        tambahBarisForm(p, 1, "Nama Lengkap Ayah", false, NamaAyah);
        tambahBarisForm(p, 2, "Nama Lengkap Istri", false, NamaIstri);

        return buatKartuSeksi(1, "II", "Nama Lengkap Keluarga", "Nama ibu diambil otomatis dari data pasien", p);
    }

    // 3. Masuk Melalui / Dikirim Oleh
    private widget.PanelBiasa buatSeksi3Masuk() {
        JPanel p = buatFormGridPanel();

        MasukMelalui = new widget.ComboBox();
        MasukMelalui.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"", "Poliklinik", "IGD", "Rujukan Faskes Lain"}));
        MasukMelalui.setPreferredSize(new Dimension(280, 32));
        MasukMelalui.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        MasukMelalui.setBackground(Color.WHITE);

        DikirimOleh = buatTextBox(360, false, "Misal: RS Lain, Puskesmas, Dokter, Kemauan Sendiri");

        RbKasusPolisiYa = new widget.RadioButton();
        RbKasusPolisiTidak = new widget.RadioButton();
        bgKasusPolisi.add(RbKasusPolisiYa);
        bgKasusPolisi.add(RbKasusPolisiTidak);
        pillKasusPolisiYa = new RadioPill(RbKasusPolisiYa, "Ya");
        pillKasusPolisiTidak = new RadioPill(RbKasusPolisiTidak, "Tidak");

        JPanel pnlPolisi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlPolisi.setOpaque(false);
        pnlPolisi.add(pillKasusPolisiYa);
        pnlPolisi.add(pillKasusPolisiTidak);

        tambahBarisForm(p, 0, "Masuk Melalui", true, MasukMelalui);
        tambahBarisForm(p, 1, "Dikirim Oleh", false, DikirimOleh);
        tambahBarisForm(p, 2, "Kasus Polisi", true, pnlPolisi);

        return buatKartuSeksi(2, "III", "Masuk Melalui / Dikirim Oleh", "Jalur masuk pasien & kasus polisi", p);
    }

    // 4. Diagnosa
    private widget.PanelBiasa buatSeksi4Diagnosa() {
        JPanel p = buatFormGridPanel();

        DiagnoseSementara = buatTextBox(480, false, "Diagnosa awal / sementara");
        AlergiTerhadap = buatTextBox(480, false, "Riwayat alergi");
        DiagnoseAkhir = buatTextBox(480, false, "Diagnosa utama");
        KodeIcd = buatTextBox(160, false, "Kode ICD-10");
        Komplikasi = buatTextBox(480, false, "Komplikasi penyakit");
        DiagnoseLain = buatTextBox(480, false, "Diagnosa sekunder / penyerta");

        tambahBarisForm(p, 0, "Diagnose Sementara", true, DiagnoseSementara);
        tambahBarisForm(p, 1, "Alergi Terhadap", false, AlergiTerhadap);
        tambahBarisForm(p, 2, "Diagnose Akhir", true, DiagnoseAkhir);
        tambahBarisForm(p, 3, "Kode ICD", false, KodeIcd);
        tambahBarisForm(p, 4, "Komplikasi", false, Komplikasi);
        tambahBarisForm(p, 5, "Diagnose Lain", false, DiagnoseLain);

        return buatKartuSeksi(3, "IV", "Diagnosa", "Diagnosis sementara, akhir, alergi & komplikasi", p);
    }

    // 5. Keadaan Keluar
    private widget.PanelBiasa buatSeksi5Keluar() {
        JPanel p = buatFormGridPanel();

        RbKeluarSembuh = new widget.RadioButton();
        RbKeluarDirujuk = new widget.RadioButton();
        RbKeluarPulangPaksa = new widget.RadioButton();
        RbKeluarMeninggal = new widget.RadioButton();
        bgKeadaanKeluar.add(RbKeluarSembuh);
        bgKeadaanKeluar.add(RbKeluarDirujuk);
        bgKeadaanKeluar.add(RbKeluarPulangPaksa);
        bgKeadaanKeluar.add(RbKeluarMeninggal);

        pillKeluarSembuh = new RadioPill(RbKeluarSembuh, "Sembuh");
        pillKeluarDirujuk = new RadioPill(RbKeluarDirujuk, "Dirujuk");
        pillKeluarPulangPaksa = new RadioPill(RbKeluarPulangPaksa, "Pulang Paksa");
        pillKeluarMeninggal = new RadioPill(RbKeluarMeninggal, "Meninggal");

        JPanel pnlPillKeluar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlPillKeluar.setOpaque(false);
        pnlPillKeluar.add(pillKeluarSembuh);
        pnlPillKeluar.add(pillKeluarDirujuk);
        pnlPillKeluar.add(pillKeluarPulangPaksa);
        pnlPillKeluar.add(pillKeluarMeninggal);

        CaraKeluarMeninggal = new widget.ComboBox();
        CaraKeluarMeninggal.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"—", "< 48 Jam Perawatan", "≥ 48 Jam Perawatan"}));
        CaraKeluarMeninggal.setPreferredSize(new Dimension(220, 32));
        CaraKeluarMeninggal.setBackground(Color.WHITE);

        RujukKe = buatTextBox(360, false, "—"); // Sebab meninggal / Rujuk ke

        RbKunjunganTidak = new widget.RadioButton();
        RbKunjunganYa = new widget.RadioButton();
        bgKunjunganUlang.add(RbKunjunganTidak);
        bgKunjunganUlang.add(RbKunjunganYa);
        pillKunjunganTidak = new RadioPill(RbKunjunganTidak, "Tidak");
        pillKunjunganYa = new RadioPill(RbKunjunganYa, "Ya");

        pnlExtraKunjungan = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlExtraKunjungan.setOpaque(false);
        pnlExtraKunjungan.add(pillKunjunganTidak);
        pnlExtraKunjungan.add(pillKunjunganYa);

        KunjunganUlangTgl = new widget.Tanggal();
        KunjunganUlangTgl.setDisplayFormat("dd-MM-yyyy");
        KunjunganUlangTgl.setDate(new Date());
        KunjunganUlangTgl.setPreferredSize(new Dimension(100, 32));

        KeKlinik = buatTextBox(200, false, "Klinik tujuan");
        pnlExtraKunjungan.add(KunjunganUlangTgl);
        pnlExtraKunjungan.add(KeKlinik);
        KunjunganUlangTgl.setVisible(false);
        KeKlinik.setVisible(false);

        RbKunjunganYa.addItemListener(e -> {
            boolean ya = RbKunjunganYa.isSelected();
            KunjunganUlangTgl.setVisible(ya);
            KeKlinik.setVisible(ya);
            pnlExtraKunjungan.revalidate();
            pnlExtraKunjungan.repaint();
        });

        // Dokter yang merawat
        KdDokterMerawat = buatTextBox(70, true, "");
        NmDokterMerawat = buatTextBox(220, true, "Pilih dokter");
        TNipDokter = buatTextBox(160, true, "NIP");
        BtnDokterMerawat = new widget.Button();
        BtnDokterMerawat.setIcon(new ImageIcon(getClass().getResource("/picture/190.png")));
        BtnDokterMerawat.setPreferredSize(new Dimension(28, 30));
        BtnDokterMerawat.setToolTipText("Cari dokter yang merawat");
        BtnDokterMerawat.addActionListener(evt -> bukaCariDokter(0));

        JPanel pnlDokter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlDokter.setOpaque(false);
        pnlDokter.add(KdDokterMerawat);
        pnlDokter.add(NmDokterMerawat);
        pnlDokter.add(BtnDokterMerawat);
        pnlDokter.add(new JLabel("NIP :"));
        pnlDokter.add(TNipDokter);

        tambahBarisForm(p, 0, "Keadaan Keluar", true, pnlPillKeluar);
        tambahBarisForm(p, 1, "Bila Meninggal, Terjadi", false, CaraKeluarMeninggal);
        tambahBarisForm(p, 2, "Sebab Meninggal", false, RujukKe);
        tambahBarisForm(p, 3, "Kunjungan Ulang Direncanakan", false, pnlExtraKunjungan);
        tambahBarisForm(p, 4, "Dokter Yang Merawat", false, pnlDokter);

        RbKeluarMeninggal.addItemListener(e -> {
            boolean m = RbKeluarMeninggal.isSelected();
            if (ChkMeninggal != null) ChkMeninggal.setSelected(m);
            CaraKeluarMeninggal.setEnabled(m);
            kartuSeksi[6].setVisible(m || !satuHalaman ? m : (halamanAktif == 6));
            barisRail[6].setVisible(m);
            if (FormInput != null) {
                FormInput.revalidate();
                FormInput.repaint();
            }
        });

        return buatKartuSeksi(4, "V", "Keadaan Keluar", "Cara keluar, sebab meninggal & kunjungan ulang", p);
    }

    // 6. Tindakan (4 baris)
    private widget.PanelBiasa buatSeksi6Tindakan() {
        JPanel p = buatFormGridPanel();
        String[] namaTindakan = {"1. Operasi Ke 1", "2. Operasi Ke 2", "3. Operasi Khusus", "4. Tindakan Lain"};

        for (int n = 0; n < 4; n++) {
            Tindakan[n] = buatTextBox(260, false, "Nama tindakan / operasi");
            TindakanTgl[n] = new widget.Tanggal();
            TindakanTgl[n].setDisplayFormat("dd-MM-yyyy");
            TindakanTgl[n].setDate(new Date());
            TindakanTgl[n].setPreferredSize(new Dimension(100, 32));
            TindakanKdDokter[n] = buatTextBox(70, true, "");
            TindakanNmDokter[n] = buatTextBox(180, true, "Dokter operator");

            final int idx = n;
            BtnTindakanDokter[n] = new widget.Button();
            BtnTindakanDokter[n].setIcon(new ImageIcon(getClass().getResource("/picture/190.png")));
            BtnTindakanDokter[n].setPreferredSize(new Dimension(28, 30));
            BtnTindakanDokter[n].setToolTipText("Cari dokter");
            BtnTindakanDokter[n].addActionListener(evt -> bukaCariDokter(idx + 1));

            JPanel rowTindakan = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            rowTindakan.setOpaque(false);
            rowTindakan.add(Tindakan[n]);
            rowTindakan.add(new JLabel("Tgl :"));
            rowTindakan.add(TindakanTgl[n]);
            rowTindakan.add(new JLabel("Dokter :"));
            rowTindakan.add(TindakanKdDokter[n]);
            rowTindakan.add(TindakanNmDokter[n]);
            rowTindakan.add(BtnTindakanDokter[n]);

            tambahBarisForm(p, n, namaTindakan[n], false, rowTindakan);
        }

        return buatKartuSeksi(5, "VI", "Tindakan", "Operasi dan tindakan medis selama perawatan", p);
    }

    // 7. Sebab Kematian (Halaman 2 - Kondisional)
    private widget.PanelBiasa buatSeksi7Kematian() {
        JPanel p = buatFormGridPanel();

        ChkMeninggal = new widget.CekBox();
        ChkMeninggal.setVisible(false);

        SkPenyakitA = buatTextBox(360, false, "Penyakit penyebab langsung kematian");
        SkLamaA = buatTextBox(140, false, "Lama sakit");
        JPanel pnlA = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlA.setOpaque(false); pnlA.add(SkPenyakitA); pnlA.add(new JLabel("Lamanya :")); pnlA.add(SkLamaA);

        SkPenyakitB = buatTextBox(360, false, "Disebabkan oleh penyakit antara");
        SkLamaB = buatTextBox(140, false, "Lama sakit");
        JPanel pnlB = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlB.setOpaque(false); pnlB.add(SkPenyakitB); pnlB.add(new JLabel("Lamanya :")); pnlB.add(SkLamaB);

        SkPenyakitC = buatTextBox(360, false, "Penyakit pokok penyebab dasar");
        SkLamaC = buatTextBox(140, false, "Lama sakit");
        JPanel pnlC = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlC.setOpaque(false); pnlC.add(SkPenyakitC); pnlC.add(new JLabel("Lamanya :")); pnlC.add(SkLamaC);

        SkPenyakitLain = buatTextBox(360, false, "Penyakit lain yang berpengaruh");
        SkLamaLain = buatTextBox(140, false, "Lama sakit");
        JPanel pnlLain = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlLain.setOpaque(false); pnlLain.add(SkPenyakitLain); pnlLain.add(new JLabel("Lamanya :")); pnlLain.add(SkLamaLain);

        RpMacam = buatTextBox(360, false, "Macam ruda paksa");
        RpCaraKejadian = buatTextBox(360, false, "Cara kejadian ruda paksa");
        RpSifatJenis = buatTextBox(360, false, "Sifat jenis kerusakan tubuh");

        RbKmYa = new widget.RadioButton(); RbKmTidak = new widget.RadioButton();
        bgKm.add(RbKmYa); bgKm.add(RbKmTidak);
        pillKmYa = new RadioPill(RbKmYa, "Ya"); pillKmTidak = new RadioPill(RbKmTidak, "Tidak");
        KmSebab = buatTextBox(240, false, "Sebab kematian janin");
        JPanel pnlKm = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlKm.setOpaque(false); pnlKm.add(pillKmYa); pnlKm.add(pillKmTidak); pnlKm.add(new JLabel("Sebab :")); pnlKm.add(KmSebab);

        RbPkPersalinanYa = new widget.RadioButton(); RbPkPersalinanTidak = new widget.RadioButton();
        bgPkPersalinan.add(RbPkPersalinanYa); bgPkPersalinan.add(RbPkPersalinanTidak);
        pillPkPersalinanYa = new RadioPill(RbPkPersalinanYa, "Ya"); pillPkPersalinanTidak = new RadioPill(RbPkPersalinanTidak, "Tidak");

        RbPkKehamilanYa = new widget.RadioButton(); RbPkKehamilanTidak = new widget.RadioButton();
        bgPkKehamilan.add(RbPkKehamilanYa); bgPkKehamilan.add(RbPkKehamilanTidak);
        pillPkKehamilanYa = new RadioPill(RbPkKehamilanYa, "Ya"); pillPkKehamilanTidak = new RadioPill(RbPkKehamilanTidak, "Tidak");

        JPanel pnlPersalinan = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlPersalinan.setOpaque(false);
        pnlPersalinan.add(new JLabel("Persalinan:")); pnlPersalinan.add(pillPkPersalinanYa); pnlPersalinan.add(pillPkPersalinanTidak);
        pnlPersalinan.add(new JLabel("   Kehamilan:")); pnlPersalinan.add(pillPkKehamilanYa); pnlPersalinan.add(pillPkKehamilanTidak);

        RbOpYa = new widget.RadioButton(); RbOpTidak = new widget.RadioButton();
        bgOp.add(RbOpYa); bgOp.add(RbOpTidak);
        pillOpYa = new RadioPill(RbOpYa, "Ya"); pillOpTidak = new RadioPill(RbOpTidak, "Tidak");
        OpJam = buatTextBox(120, false, "Jam operasi");
        JPanel pnlOp = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlOp.setOpaque(false); pnlOp.add(pillOpYa); pnlOp.add(pillOpTidak); pnlOp.add(new JLabel("Jam Operasi :")); pnlOp.add(OpJam);

        SkTanggal = new widget.Tanggal();
        SkTanggal.setDisplayFormat("dd-MM-yyyy");
        SkTanggal.setDate(new Date());
        SkTanggal.setPreferredSize(new Dimension(100, 32));

        KdPemberiKeterangan = buatTextBox(70, true, "");
        NmPemberiKeterangan = buatTextBox(200, true, "Pilih dokter pemeriksa");
        BtnPemberiKeterangan = new widget.Button();
        BtnPemberiKeterangan.setIcon(new ImageIcon(getClass().getResource("/picture/190.png")));
        BtnPemberiKeterangan.setPreferredSize(new Dimension(28, 30));
        BtnPemberiKeterangan.addActionListener(evt -> bukaCariDokter(5));

        JPanel pnlKeterangan = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlKeterangan.setOpaque(false);
        pnlKeterangan.add(SkTanggal);
        pnlKeterangan.add(new JLabel("   Dokter :"));
        pnlKeterangan.add(KdPemberiKeterangan);
        pnlKeterangan.add(NmPemberiKeterangan);
        pnlKeterangan.add(BtnPemberiKeterangan);

        MeninggalTgl = new widget.Tanggal();
        MeninggalTgl.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        MeninggalTgl.setVisible(false);

        tambahBarisForm(p, 0, "a. Penyebab Langsung", false, pnlA);
        tambahBarisForm(p, 1, "b. Disebabkan Oleh", false, pnlB);
        tambahBarisForm(p, 2, "c. Disebabkan Oleh", false, pnlC);
        tambahBarisForm(p, 3, "Penyakit Lain Berpengaruh", false, pnlLain);
        tambahBarisForm(p, 4, "I. Ruda Paksa - Macam", false, RpMacam);
        tambahBarisForm(p, 5, "   Cara Kejadian", false, RpCaraKejadian);
        tambahBarisForm(p, 6, "   Sifat Jenis Kerusakan", false, RpSifatJenis);
        tambahBarisForm(p, 7, "II. Janin Lahir Mati", false, pnlKm);
        tambahBarisForm(p, 8, "III. Peristiwa Kehamilan", false, pnlPersalinan);
        tambahBarisForm(p, 9, "IV. Dilakukan Operasi", false, pnlOp);
        tambahBarisForm(p, 10, "Tanggal & Keterangan Dokter", false, pnlKeterangan);

        widget.PanelBiasa card = buatKartuSeksi(6, "VII", "Sebab Kematian", "Halaman 2 — Diisi bila pasien meninggal dunia", p);
        card.setVisible(false);
        barisRail[6].setVisible(false);
        return card;
    }

    // =================================================================
    //  REAKTIVITAS & NAVIGASI
    // =================================================================

    private void bukaBagian(int idx) {
        if (idx < 0 || idx > 6) return;
        halamanAktif = idx;

        for (int k = 0; k < 7; k++) {
            boolean aktif = (k == idx);
            barisRail[k].setBackground(aktif ? new Color(0xF1, 0xF7, 0xF6) : Color.WHITE);
            barisRail[k].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 1, 0, aktif ? new Color(0x0D, 0x7A, 0x70) : new Color(0xF4, 0xF5, 0xF2)),
                    BorderFactory.createEmptyBorder(9, 11, 9, 14)));
        }

        if (satuHalaman) {
            for (int k = 0; k < 7; k++) {
                kartuSeksi[k].setVisible(k == idx);
            }
            if (idx == 6 && !RbKeluarMeninggal.isSelected()) {
                kartuSeksi[6].setVisible(true);
            }
            if (FormInput != null) {
                FormInput.revalidate();
                FormInput.repaint();
            }
            scrollSeksiMedis.getVerticalScrollBar().setValue(0);
        } else {
            SwingUtilities.invokeLater(() -> {
                kartuSeksi[idx].scrollRectToVisible(kartuSeksi[idx].getBounds());
            });
        }
    }

    private void toggleModeTampil() {
        satuHalaman = !satuHalaman;
        btnToggleMode.setText(satuHalaman ? "Tampilkan Semua Bagian" : "Tampilkan Per Bagian");

        for (int k = 0; k < 7; k++) {
            if (satuHalaman) {
                kartuSeksi[k].setVisible(k == halamanAktif);
            } else {
                kartuSeksi[k].setVisible(k != 6 || RbKeluarMeninggal.isSelected());
            }
        }
        if (FormInput != null) {
            FormInput.revalidate();
            FormInput.repaint();
        }
    }

    private void hitungProgres() {
        // 5 Isian Wajib:
        // 1. Masuk Melalui
        // 2. Kasus Polisi
        // 3. Diagnose Sementara
        // 4. Diagnose Akhir
        // 5. Keadaan Keluar
        int wajibTerisi = 0;
        if (MasukMelalui.getSelectedIndex() > 0) wajibTerisi++;
        if (RbKasusPolisiYa.isSelected() || RbKasusPolisiTidak.isSelected()) wajibTerisi++;
        if (!DiagnoseSementara.getText().trim().isEmpty()) wajibTerisi++;
        if (!DiagnoseAkhir.getText().trim().isEmpty()) wajibTerisi++;
        if (RbKeluarSembuh.isSelected() || RbKeluarDirujuk.isSelected()
                || RbKeluarPulangPaksa.isSelected() || RbKeluarMeninggal.isSelected()) wajibTerisi++;

        int persen = (wajibTerisi * 100) / 5;
        pbProgres.setValue(persen);
        lblProgresPersen.setText(persen + "%");

        if (persen == 100) {
            pbProgres.setForeground(new Color(0x1A, 0x6B, 0x4C));
            lblProgresCaption.setText("SEMUA ISIAN WAJIB TERPENUHI");
        } else {
            pbProgres.setForeground(new Color(0x0D, 0x7A, 0x70));
            lblProgresCaption.setText("ISIAN WAJIB");
        }

        // Tally per seksi
        // Seksi I: 9 opsional
        lblTallySeksi[0].setText("9 isian opsional");
        lblTallySeksi[0].setBackground(new Color(0xE8, 0xF3, 0xED));
        lblTallySeksi[0].setForeground(new Color(0x1A, 0x6B, 0x4C));
        lblRailTally[0].setText("–");

        // Seksi II: Nama Keluarga (opsional)
        lblTallySeksi[1].setText("Opsional");
        lblTallySeksi[1].setBackground(new Color(0xF2, 0xF3, 0xF0));
        lblTallySeksi[1].setForeground(new Color(0x9A, 0xA1, 0x9C));
        lblRailTally[1].setText("–");

        // Seksi III: Masuk & Pengirim (2 wajib)
        int s3 = (MasukMelalui.getSelectedIndex() > 0 ? 1 : 0)
                + ((RbKasusPolisiYa.isSelected() || RbKasusPolisiTidak.isSelected()) ? 1 : 0);
        lblRailTally[2].setText(s3 + "/2");
        setTallyPill(lblTallySeksi[2], s3, 2);

        // Seksi IV: Diagnosa (2 wajib: Sementara & Akhir)
        int s4 = (!DiagnoseSementara.getText().trim().isEmpty() ? 1 : 0)
                + (!DiagnoseAkhir.getText().trim().isEmpty() ? 1 : 0);
        lblRailTally[3].setText(s4 + "/2");
        setTallyPill(lblTallySeksi[3], s4, 2);

        // Seksi V: Keadaan Keluar (1 wajib)
        int s5 = (RbKeluarSembuh.isSelected() || RbKeluarDirujuk.isSelected()
                || RbKeluarPulangPaksa.isSelected() || RbKeluarMeninggal.isSelected()) ? 1 : 0;
        lblRailTally[4].setText(s5 + "/1");
        setTallyPill(lblTallySeksi[4], s5, 1);

        // Seksi VI: Tindakan
        lblTallySeksi[5].setText("Opsional");
        lblTallySeksi[5].setBackground(new Color(0xF2, 0xF3, 0xF0));
        lblTallySeksi[5].setForeground(new Color(0x9A, 0xA1, 0x9C));
        lblRailTally[5].setText("Opsional");

        // Seksi VII: Sebab Kematian
        lblTallySeksi[6].setText("Halaman 2");
        lblTallySeksi[6].setBackground(new Color(0xF2, 0xF3, 0xF0));
        lblTallySeksi[6].setForeground(new Color(0x9A, 0xA1, 0x9C));
        lblRailTally[6].setText("Opsional");

        lblFooterNote.setText(wajibTerisi + "/5 isian wajib · 9 opsional terisi");
    }

    private void setTallyPill(JLabel lbl, int terisi, int total) {
        if (terisi >= total) {
            lbl.setText("Wajib lengkap");
            lbl.setBackground(new Color(0xE8, 0xF3, 0xED));
            lbl.setForeground(new Color(0x1A, 0x6B, 0x4C));
        } else {
            lbl.setText(terisi + "/" + total + " wajib terisi");
            lbl.setBackground(new Color(0xFD, 0xF3, 0xE7));
            lbl.setForeground(new Color(0x8A, 0x5A, 0x12));
        }
    }

    private void pasangListenerProgres() {
        MasukMelalui.addActionListener(evt -> hitungProgres());
        RbKasusPolisiYa.addActionListener(evt -> hitungProgres());
        RbKasusPolisiTidak.addActionListener(evt -> hitungProgres());

        KeyAdapter ka = new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { hitungProgres(); }
        };
        DiagnoseSementara.addKeyListener(ka);
        DiagnoseAkhir.addKeyListener(ka);

        RbKeluarSembuh.addActionListener(evt -> hitungProgres());
        RbKeluarDirujuk.addActionListener(evt -> hitungProgres());
        RbKeluarPulangPaksa.addActionListener(evt -> hitungProgres());
        RbKeluarMeninggal.addActionListener(evt -> hitungProgres());
    }

    // =================================================================
    //  TAB DATA
    // =================================================================

    private void rakitTabData() {
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetakRingkasan = new javax.swing.JMenuItem();
        MnCetakRingkasan.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        MnCetakRingkasan.setIcon(new ImageIcon(getClass().getResource("/picture/category.png")));
        MnCetakRingkasan.setText("Cetak Ringkasan Pasien Masuk Keluar");
        MnCetakRingkasan.setPreferredSize(new Dimension(260, 26));
        MnCetakRingkasan.addActionListener(evt -> cetakTerpilih());
        jPopupMenu1.add(MnCetakRingkasan);

        internalFrame3 = new widget.InternalFrame();
        internalFrame3.setBorder(null);
        internalFrame3.setLayout(new BorderLayout(1, 1));

        tbObat = new widget.Table();
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        int[] lebar = {105, 70, 160, 80, 60, 80, 170, 110, 250};
        for (i = 0; i < lebar.length; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebar[i]);
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        tbObat.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent evt) {
                if (tabMode.getRowCount() != 0) getData();
            }
        });
        tbObat.addKeyListener(new KeyAdapter() {
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

        panelGlass9 = new widget.panelisi();
        panelGlass9.setPreferredSize(new Dimension(44, 44));
        panelGlass9.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        DTPCari1 = new widget.Tanggal(); DTPCari1.setDisplayFormat("dd-MM-yyyy"); DTPCari1.setDate(new Date()); DTPCari1.setPreferredSize(new Dimension(95, 23));
        DTPCari2 = new widget.Tanggal(); DTPCari2.setDisplayFormat("dd-MM-yyyy"); DTPCari2.setDate(new Date()); DTPCari2.setPreferredSize(new Dimension(95, 23));
        TCari = new widget.TextBox(); TCari.setPreferredSize(new Dimension(200, 23));
        TCari.addKeyListener(new KeyAdapter() {
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

        JLabel lTgl = new JLabel("Tgl.Masuk :"); lTgl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JLabel lSd = new JLabel("s.d"); lSd.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JLabel lKey = new JLabel("   Key Word :"); lKey.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JLabel lJml = new JLabel("   Jml :"); lJml.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        panelGlass9.add(lTgl);
        panelGlass9.add(DTPCari1);
        panelGlass9.add(lSd);
        panelGlass9.add(DTPCari2);
        panelGlass9.add(lKey);
        panelGlass9.add(TCari);
        panelGlass9.add(BtnCari);
        panelGlass9.add(lJml);
        panelGlass9.add(LCount);
        internalFrame3.add(panelGlass9, BorderLayout.PAGE_END);

        LoadHTML = new widget.editorpane();
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(false);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right:1px solid #e2e7dd;font:8.5px tahoma;height:12px;"
                + "border-bottom:1px solid #e2e7dd;background:#ffffff;color:#323232;}");
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);

        ScrollHTML = new widget.ScrollPane();
        ScrollHTML.setViewportView(LoadHTML);

        TNoRM1 = buatTextBox(90, true, "");
        TPasien1 = buatTextBox(180, true, "");
        BtnPrint1 = new widget.Button();
        BtnPrint1.setIcon(new ImageIcon(getClass().getResource("/picture/item (copy).png")));
        BtnPrint1.setPreferredSize(new Dimension(28, 23));
        BtnPrint1.setToolTipText("Cetak ringkasan pasien ini");
        BtnPrint1.addActionListener(evt -> cetakTerpilih());

        FormMenu = new widget.panelisi();
        FormMenu.setPreferredSize(new Dimension(44, 44));
        FormMenu.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        FormMenu.add(new JLabel("Pasien :"));
        FormMenu.add(TNoRM1);
        FormMenu.add(TPasien1);
        FormMenu.add(BtnPrint1);

        ChkAccor = new widget.CekBox();
        ChkAccor.setIcon(new ImageIcon(getClass().getResource("/picture/kiri.png")));
        ChkAccor.setPreferredSize(new Dimension(15, 15));
        ChkAccor.addActionListener(evt -> isMenu());

        PanelAccor = new widget.PanelBiasa();
        PanelAccor.setLayout(new BorderLayout(1, 1));
        PanelAccor.setPreferredSize(new Dimension(470, 44));
        PanelAccor.add(ChkAccor, BorderLayout.WEST);
        PanelAccor.add(FormMenu, BorderLayout.NORTH);
        PanelAccor.add(ScrollHTML, BorderLayout.CENTER);
        internalFrame3.add(PanelAccor, BorderLayout.EAST);

        TabRawat.addTab("Data Ringkasan", internalFrame3);
        TabRawat.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent evt) {
                if (TabRawat.getSelectedIndex() == 1) {
                    runBackground(() -> tampil());
                }
            }
        });
    }

    private void isMenu() {
        if (ChkAccor.isSelected()) {
            ChkAccor.setIcon(new ImageIcon(getClass().getResource("/picture/kanan.png")));
            PanelAccor.setPreferredSize(new Dimension(470, 44));
            FormMenu.setVisible(true);
            ScrollHTML.setVisible(true);
        } else {
            ChkAccor.setIcon(new ImageIcon(getClass().getResource("/picture/kiri.png")));
            PanelAccor.setPreferredSize(new Dimension(15, 44));
            FormMenu.setVisible(false);
            ScrollHTML.setVisible(false);
        }
        PanelAccor.revalidate();
        PanelAccor.repaint();
    }

    // =================================================================
    //  DIALOG DOKTER
    // =================================================================

    private void bukaCariDokter(int tujuan) {
        tujuanPilihDokter = tujuan;
        if (dokter == null || !dokter.isDisplayable()) {
            dokter = new DlgCariDokter(null, false);
            dokter.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dokter.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if (dokter.getTable().getSelectedRow() != -1) {
                        terapkanPilihanDokter(
                                dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString(),
                                dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString());
                    }
                    dokter = null;
                }
            });
            dokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            dokter.setLocationRelativeTo(internalFrame1);
        }
        dokter.isCek();
        if (dokter.isVisible()) {
            dokter.toFront();
            return;
        }
        dokter.setVisible(true);
    }

    private void terapkanPilihanDokter(String kd, String nm) {
        if (kd == null || kd.trim().isEmpty()) return;
        if (tujuanPilihDokter == 0) {
            KdDokterMerawat.setText(kd);
            NmDokterMerawat.setText(nm);
            TNipDokter.setText(nipDokter(kd, nm));
        } else if (tujuanPilihDokter >= 1 && tujuanPilihDokter <= 4) {
            TindakanKdDokter[tujuanPilihDokter - 1].setText(kd);
            TindakanNmDokter[tujuanPilihDokter - 1].setText(nm);
        } else if (tujuanPilihDokter == 5) {
            KdPemberiKeterangan.setText(kd);
            NmPemberiKeterangan.setText(nm);
        }
    }

    private String nipDokter(String kdDokter, String namaDokter) {
        String nip = Sequel.cariIsi("select pegawai.nik from pegawai where pegawai.nik=?", kdDokter);
        if (nip == null || nip.trim().isEmpty()) {
            nip = Sequel.cariIsi("select pegawai.nik from pegawai where pegawai.nama=?", namaDokter);
        }
        return nip == null ? "" : nip;
    }

    // =================================================================
    //  PERSISTENCE & LAPISAN DATA
    // =================================================================

    private String getPilihanKeadaanKeluar() {
        if (RbKeluarSembuh.isSelected()) return "Sembuh";
        if (RbKeluarDirujuk.isSelected()) return "Rujuk";
        if (RbKeluarPulangPaksa.isSelected()) return "Pulang Paksa";
        if (RbKeluarMeninggal.isSelected()) return null; // Kematian ditandai meninggal_tanggal
        return null;
    }

    private String getPilihanCaraMeninggal() {
        if (CaraKeluarMeninggal.getSelectedIndex() <= 0) return null;
        String s = CaraKeluarMeninggal.getSelectedItem().toString();
        if (s.contains("<")) return "Sebelum 48 Jam";
        if (s.contains("≥") || s.contains(">=")) return "Sesudah 48 Jam";
        return s;
    }

    private String getPilihanMasukMelalui() {
        if (MasukMelalui.getSelectedIndex() <= 0) return null;
        String s = MasukMelalui.getSelectedItem().toString();
        if ("Poliklinik".equalsIgnoreCase(s)) return "Poliklinik";
        if ("IGD".equalsIgnoreCase(s)) return "IGD";
        return "IGD"; // fallback enum safe
    }

    private String[] nilaiRm17() {
        return new String[]{
            NamaSuami.getText(), NamaIstri.getText(), NamaAyah.getText(),
            getPilihanMasukMelalui(), kosongKeNull(DikirimOleh.getText()), pilihan(bgKasusPolisi),
            DiagnoseSementara.getText(), AlergiTerhadap.getText(),
            DiagnoseAkhir.getText(), Komplikasi.getText(), DiagnoseLain.getText(), KodeIcd.getText(),
            Tindakan[0].getText(), tglTindakan(0), kodeTindakanDokter(0),
            Tindakan[1].getText(), tglTindakan(1), kodeTindakanDokter(1),
            Tindakan[2].getText(), tglTindakan(2), kodeTindakanDokter(2),
            Tindakan[3].getText(), tglTindakan(3), kodeTindakanDokter(3),
            meninggalTanggal(), meninggalJam(),
            getPilihanKeadaanKeluar(), RujukKe.getText(), getPilihanCaraMeninggal(),
            RbKunjunganYa.isSelected() ? Valid.SetTgl(KunjunganUlangTgl.getSelectedItem() + "") : null,
            KeKlinik.getText(), kosongKeNull(KdDokterMerawat.getText()),
            SkPenyakitA.getText(), SkLamaA.getText(), SkPenyakitB.getText(), SkLamaB.getText(),
            SkPenyakitC.getText(), SkLamaC.getText(), SkPenyakitLain.getText(), SkLamaLain.getText(),
            RpMacam.getText(), RpCaraKejadian.getText(), RpSifatJenis.getText(),
            pilihan(bgKm), KmSebab.getText(),
            pilihan(bgPkPersalinan), pilihan(bgPkKehamilan),
            pilihan(bgOp), OpJam.getText(),
            RbKeluarMeninggal.isSelected() ? Valid.SetTgl(SkTanggal.getSelectedItem() + "") : null,
            kosongKeNull(KdPemberiKeterangan.getText())
        };
    }

    private String tglTindakan(int idx) {
        return Tindakan[idx].getText().trim().isEmpty()
                ? null : Valid.SetTgl(TindakanTgl[idx].getSelectedItem() + "");
    }

    private String kodeTindakanDokter(int idx) {
        return Tindakan[idx].getText().trim().isEmpty()
                ? null : kosongKeNull(TindakanKdDokter[idx].getText());
    }

    private String meninggalTanggal() {
        if (!RbKeluarMeninggal.isSelected()) return null;
        String tgl = MeninggalTgl.getSelectedItem() + "";
        return tgl.length() >= 10 ? Valid.SetTgl(tgl.substring(0, 10)) : null;
    }

    private String meninggalJam() {
        if (!RbKeluarMeninggal.isSelected()) return null;
        String tgl = MeninggalTgl.getSelectedItem() + "";
        return tgl.length() >= 19 ? tgl.substring(11, 19) : "00:00:00";
    }

    private String kosongKeNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private String pilihan(ButtonGroup bg) {
        for (java.util.Enumeration<javax.swing.AbstractButton> e = bg.getElements(); e.hasMoreElements();) {
            javax.swing.AbstractButton b = e.nextElement();
            if (b.isSelected()) return b.getText();
        }
        return null;
    }

    private void simpan() {
        if (TNoRw.getText().trim().isEmpty()) {
            Valid.textKosong(TNoRw, "Pasien");
            return;
        }
        if (adaRecord(TNoRw.getText())) {
            JOptionPane.showMessageDialog(rootPane,
                    "Data ringkasan untuk pasien ini sudah ada. Gunakan tombol 'Ganti' untuk memperbarui.");
            return;
        }
        StringBuilder kol = new StringBuilder("no_rawat");
        StringBuilder tanya = new StringBuilder("?");
        for (String k : KOLOM_RM17) {
            kol.append(",").append(k);
            tanya.append(",?");
        }
        String[] nilai = nilaiRm17();
        String[] param = new String[nilai.length + 1];
        param[0] = TNoRw.getText();
        System.arraycopy(nilai, 0, param, 1, nilai.length);

        if (Sequel.menyimpantf("ringkasan_pasien_masuk_keluar",
                "insert into ringkasan_pasien_masuk_keluar(" + kol + ") values(" + tanya + ")",
                kol.toString(), param.length, param)) {
            runBackground(() -> tampil());
            bangunRingkasanHtml();
        }
    }

    private void aksiGanti() {
        if (TNoRw.getText().trim().isEmpty()) {
            Valid.textKosong(TNoRw, "Pasien");
            return;
        }
        if (!adaRecord(TNoRw.getText())) {
            JOptionPane.showMessageDialog(rootPane,
                    "Belum ada data tersimpan untuk pasien ini. Gunakan tombol 'Simpan'.");
            return;
        }
        StringBuilder set = new StringBuilder();
        for (int idx = 0; idx < KOLOM_RM17.length; idx++) {
            if (idx > 0) set.append(",");
            set.append(KOLOM_RM17[idx]).append("=?");
        }
        String[] nilai = nilaiRm17();
        String[] param = new String[nilai.length + 1];
        System.arraycopy(nilai, 0, param, 0, nilai.length);
        param[nilai.length] = TNoRw.getText();

        if (Sequel.mengedittf("ringkasan_pasien_masuk_keluar", "no_rawat=?", set.toString(),
                param.length, param)) {
            runBackground(() -> tampil());
            bangunRingkasanHtml();
        }
    }

    private void aksiHapus() {
        if (tbObat.getSelectedRow() <= -1) {
            JOptionPane.showMessageDialog(rootPane, "Silahkan pilih data yang mau dihapus pada tabel..!!");
            return;
        }
        if (JOptionPane.showConfirmDialog(rootPane, "Yakin data mau dihapus?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (Sequel.queryu2tf("delete from ringkasan_pasien_masuk_keluar where no_rawat=?", 1,
                    new String[]{tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()})) {
                runBackground(() -> tampil());
                emptTeks();
            }
        }
    }

    public void tampil() {
        Valid.tabelKosong(tabMode);
        boolean pakaiCari = !TCari.getText().trim().isEmpty();
        try {
            ps = koneksi.prepareStatement(sqlDaftar(pakaiCari));
            try {
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
                if (pakaiCari) {
                    for (int p = 3; p <= 7; p++) {
                        ps.setString(p, "%" + TCari.getText() + "%");
                    }
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"), rs.getString("no_rkm_medis"), rs.getString("nm_pasien"),
                        rs.getDate("tgl_lahir"), rs.getString("jk"), rs.getString("kd_dokter_merawat"),
                        rs.getString("nm_dokter"), rs.getString("keadaan_keluar"), rs.getString("diagnose_akhir")
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

    private String sqlDaftar(boolean pakaiCari) {
        String dasar = "select r.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.jk,"
                + "r.kd_dokter_merawat,dokter.nm_dokter,r.keadaan_keluar,r.diagnose_akhir "
                + "from ringkasan_pasien_masuk_keluar r "
                + "inner join reg_periksa on r.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join dokter on r.kd_dokter_merawat=dokter.kd_dokter "
                + "where reg_periksa.tgl_registrasi between ? and ? ";
        if (!pakaiCari) {
            return dasar + "order by reg_periksa.tgl_registrasi desc";
        }
        return dasar + "and (r.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? "
                + "or dokter.nm_dokter like ? or r.diagnose_akhir like ?) "
                + "order by reg_periksa.tgl_registrasi desc";
    }

    private void getData() {
        if (tbObat.getSelectedRow() == -1) return;
        TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
        muatIdentitas();
        try {
            ps = koneksi.prepareStatement("select * from ringkasan_pasien_masuk_keluar where no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
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
        bangunRingkasanHtml();
        ChkAccor.setSelected(true);
        isMenu();
    }

    private void isiDariRecord() throws Exception {
        NamaSuami.setText(teksAman(rs.getString("nama_suami")));
        NamaIstri.setText(teksAman(rs.getString("nama_istri")));
        NamaAyah.setText(teksAman(rs.getString("nama_ayah")));

        String mm = rs.getString("masuk_melalui");
        if (mm != null && !mm.isEmpty()) {
            MasukMelalui.setSelectedItem(mm);
        } else {
            MasukMelalui.setSelectedIndex(0);
        }

        DikirimOleh.setText(teksAman(rs.getString("dikirim_oleh")));

        String kp = rs.getString("kasus_polisi");
        if ("Ya".equalsIgnoreCase(kp)) RbKasusPolisiYa.setSelected(true);
        else if ("Tidak".equalsIgnoreCase(kp)) RbKasusPolisiTidak.setSelected(true);
        else bgKasusPolisi.clearSelection();

        DiagnoseSementara.setText(teksAman(rs.getString("diagnose_sementara")));
        AlergiTerhadap.setText(teksAman(rs.getString("alergi_terhadap")));
        DiagnoseAkhir.setText(teksAman(rs.getString("diagnose_akhir")));
        Komplikasi.setText(teksAman(rs.getString("komplikasi")));
        DiagnoseLain.setText(teksAman(rs.getString("diagnose_lain")));
        KodeIcd.setText(teksAman(rs.getString("kode_icd")));

        for (int n = 0; n < 4; n++) {
            Tindakan[n].setText(teksAman(rs.getString("tindakan_" + (n + 1))));
            String tgl = rs.getString("tindakan_" + (n + 1) + "_tanggal");
            if (tgl != null && !tgl.isEmpty()) Valid.SetTgl2(TindakanTgl[n], tgl);
            String kd = teksAman(rs.getString("tindakan_" + (n + 1) + "_dokter"));
            TindakanKdDokter[n].setText(kd);
            TindakanNmDokter[n].setText(kd.isEmpty() ? "" : Sequel.CariDokter(kd));
        }

        String kk = rs.getString("keadaan_keluar");
        String mTgl = rs.getString("meninggal_tanggal");
        if (mTgl != null && !mTgl.isEmpty()) {
            RbKeluarMeninggal.setSelected(true);
            String jam = teksAman(rs.getString("meninggal_jam"));
            Valid.SetTgl2(MeninggalTgl, mTgl + " " + (jam.isEmpty() ? "00:00:00" : jam));
        } else if ("Sembuh".equalsIgnoreCase(kk)) {
            RbKeluarSembuh.setSelected(true);
        } else if ("Rujuk".equalsIgnoreCase(kk) || "Dirujuk".equalsIgnoreCase(kk)) {
            RbKeluarDirujuk.setSelected(true);
        } else if ("Pulang Paksa".equalsIgnoreCase(kk)) {
            RbKeluarPulangPaksa.setSelected(true);
        } else {
            bgKeadaanKeluar.clearSelection();
        }

        RujukKe.setText(teksAman(rs.getString("rujuk_ke")));

        String cm = rs.getString("cara_keluar_meninggal");
        if (cm != null && cm.contains("Sebelum")) CaraKeluarMeninggal.setSelectedItem("< 48 Jam Perawatan");
        else if (cm != null && cm.contains("Sesudah")) CaraKeluarMeninggal.setSelectedItem("≥ 48 Jam Perawatan");
        else CaraKeluarMeninggal.setSelectedIndex(0);

        String ku = rs.getString("kunjungan_ulang_tanggal");
        if (ku != null && !ku.isEmpty()) {
            RbKunjunganYa.setSelected(true);
            Valid.SetTgl2(KunjunganUlangTgl, ku);
        } else {
            RbKunjunganTidak.setSelected(true);
        }
        KeKlinik.setText(teksAman(rs.getString("ke_klinik")));

        String kdMerawat = teksAman(rs.getString("kd_dokter_merawat"));
        KdDokterMerawat.setText(kdMerawat);
        NmDokterMerawat.setText(kdMerawat.isEmpty() ? "" : Sequel.CariDokter(kdMerawat));
        TNipDokter.setText(kdMerawat.isEmpty() ? "" : nipDokter(kdMerawat, NmDokterMerawat.getText()));

        SkPenyakitA.setText(teksAman(rs.getString("sk_penyakit_a")));
        SkLamaA.setText(teksAman(rs.getString("sk_lama_a")));
        SkPenyakitB.setText(teksAman(rs.getString("sk_penyakit_b")));
        SkLamaB.setText(teksAman(rs.getString("sk_lama_b")));
        SkPenyakitC.setText(teksAman(rs.getString("sk_penyakit_c")));
        SkLamaC.setText(teksAman(rs.getString("sk_lama_c")));
        SkPenyakitLain.setText(teksAman(rs.getString("sk_penyakit_lain")));
        SkLamaLain.setText(teksAman(rs.getString("sk_lama_lain")));
        RpMacam.setText(teksAman(rs.getString("rp_macam")));
        RpCaraKejadian.setText(teksAman(rs.getString("rp_cara_kejadian")));
        RpSifatJenis.setText(teksAman(rs.getString("rp_sifat_jenis")));

        pilihRadioPill(RbKmYa, RbKmTidak, rs.getString("km_janin_lahir_mati"));
        KmSebab.setText(teksAman(rs.getString("km_sebab")));
        pilihRadioPill(RbPkPersalinanYa, RbPkPersalinanTidak, rs.getString("pk_persalinan"));
        pilihRadioPill(RbPkKehamilanYa, RbPkKehamilanTidak, rs.getString("pk_kehamilan"));
        pilihRadioPill(RbOpYa, RbOpTidak, rs.getString("op_dilakukan"));
        OpJam.setText(teksAman(rs.getString("op_jam")));

        String skTgl = rs.getString("sk_tanggal");
        if (skTgl != null && !skTgl.isEmpty()) Valid.SetTgl2(SkTanggal, skTgl);
        String kdPk = teksAman(rs.getString("kd_pemberi_keterangan"));
        KdPemberiKeterangan.setText(kdPk);
        NmPemberiKeterangan.setText(kdPk.isEmpty() ? "" : Sequel.CariDokter(kdPk));

        hitungProgres();
    }

    private void pilihRadioPill(widget.RadioButton rYa, widget.RadioButton rTdk, String val) {
        if ("Ya".equalsIgnoreCase(val)) rYa.setSelected(true);
        else if ("Tidak".equalsIgnoreCase(val)) rTdk.setSelected(true);
        else {
            rYa.setSelected(false);
            rTdk.setSelected(false);
        }
    }

    private String teksAman(String s) {
        return s == null ? "" : s;
    }

    public void emptTeks() {
        NamaSuami.setText("");
        NamaIstri.setText("");
        NamaAyah.setText("");
        MasukMelalui.setSelectedIndex(0);
        DikirimOleh.setText("");
        bgKasusPolisi.clearSelection();
        pillKasusPolisiYa.perbarui();
        pillKasusPolisiTidak.perbarui();

        DiagnoseSementara.setText("");
        AlergiTerhadap.setText("");
        DiagnoseAkhir.setText("");
        Komplikasi.setText("");
        DiagnoseLain.setText("");
        KodeIcd.setText("");

        for (int n = 0; n < 4; n++) {
            Tindakan[n].setText("");
            TindakanTgl[n].setDate(new Date());
            TindakanKdDokter[n].setText("");
            TindakanNmDokter[n].setText("");
        }

        bgKeadaanKeluar.clearSelection();
        pillKeluarSembuh.perbarui();
        pillKeluarDirujuk.perbarui();
        pillKeluarPulangPaksa.perbarui();
        pillKeluarMeninggal.perbarui();

        CaraKeluarMeninggal.setSelectedIndex(0);
        RujukKe.setText("");
        bgKunjunganUlang.clearSelection();
        pillKunjunganTidak.perbarui();
        pillKunjunganYa.perbarui();
        KunjunganUlangTgl.setDate(new Date());
        KeKlinik.setText("");
        KunjunganUlangTgl.setVisible(false);
        KeKlinik.setVisible(false);

        KdDokterMerawat.setText("");
        NmDokterMerawat.setText("");
        TNipDokter.setText("");

        SkPenyakitA.setText("");
        SkLamaA.setText("");
        SkPenyakitB.setText("");
        SkLamaB.setText("");
        SkPenyakitC.setText("");
        SkLamaC.setText("");
        SkPenyakitLain.setText("");
        SkLamaLain.setText("");
        RpMacam.setText("");
        RpCaraKejadian.setText("");
        RpSifatJenis.setText("");

        bgKm.clearSelection();
        KmSebab.setText("");
        bgPkPersalinan.clearSelection();
        bgPkKehamilan.clearSelection();
        bgOp.clearSelection();
        OpJam.setText("");
        SkTanggal.setDate(new Date());
        KdPemberiKeterangan.setText("");
        NmPemberiKeterangan.setText("");

        kartuSeksi[6].setVisible(false);
        barisRail[6].setVisible(false);

        if (!TNoRw.getText().trim().isEmpty()) {
            muatIdentitas();
            praIsiDariSistem();
        }
        bukaBagian(0);
        hitungProgres();
        TabRawat.setSelectedIndex(0);
    }

    private void muatIdentitas() {
        try {
            ps = koneksi.prepareStatement(
                    "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.tmp_lahir,pasien.tgl_lahir,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.no_tlp,pasien.stts_nikah,"
                    + "pasien.agama,pasien.pekerjaan,pasien.pnd,pasien.nm_ibu,"
                    + "concat(ifnull(pasien.alamat,''),', ',ifnull(kelurahan.nm_kel,''),', ',"
                    + "ifnull(kecamatan.nm_kec,''),', ',ifnull(kabupaten.nm_kab,'')) as alamat,"
                    + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as usia "
                    + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "left join kelurahan on pasien.kd_kel=kelurahan.kd_kel "
                    + "left join kecamatan on pasien.kd_kec=kecamatan.kd_kec "
                    + "left join kabupaten on pasien.kd_kab=kabupaten.kd_kab "
                    + "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRM.setText(teksAman(rs.getString("no_rkm_medis")));
                    TPasien.setText(teksAman(rs.getString("nm_pasien")));
                    TmpLahir.setText(teksAman(rs.getString("tmp_lahir")));
                    TglLahir.setText(teksAman(rs.getString("tgl_lahir")));
                    TUsia.setText(teksAman(rs.getString("usia")));
                    Jk.setText(teksAman(rs.getString("jk")));
                    TAlamat.setText(teksAman(rs.getString("alamat")));
                    TTelepon.setText(teksAman(rs.getString("no_tlp")));
                    TStatus.setText(teksAman(rs.getString("stts_nikah")));
                    TAgama.setText(teksAman(rs.getString("agama")));
                    TPekerjaan.setText(teksAman(rs.getString("pekerjaan")));
                    TPendidikan.setText(teksAman(rs.getString("pnd")));
                    TNamaIbu.setText(teksAman(rs.getString("nm_ibu")));

                    TNoRM1.setText(TNoRM.getText());
                    TPasien1.setText(TPasien.getText());

                    // Update Patient Card UI
                    lblKartuNama.setText(TPasien.getText().toUpperCase());
                    lblKartuInisial.setText(buatInisial(TPasien.getText()));
                    lblKartuInfo.setText(Jk.getText() + " · " + TUsia.getText());
                    lblValRM.setText(TNoRM.getText());
                    lblValRawat.setText(TNoRw.getText());
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
        muatMasukKeluar();
    }

    private String buatInisial(String nama) {
        if (nama == null || nama.trim().isEmpty()) return "—";
        String[] parts = nama.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    private void muatMasukKeluar() {
        TMasukTgl.setText(tanpaTanggalNol(Sequel.cariIsi(
                "select date_format(min(tgl_masuk),'%d-%m-%Y') from kamar_inap where no_rawat=?", TNoRw.getText())));
        TMasukJam.setText(teksAman(Sequel.cariIsi(
                "select jam_masuk from kamar_inap where no_rawat=? order by tgl_masuk asc limit 1", TNoRw.getText())));
        TRuang.setText(teksAman(Sequel.cariIsi(
                "select bangsal.nm_bangsal from kamar_inap "
                + "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "
                + "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "
                + "where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1", TNoRw.getText())));
        TKeluarTgl.setText(tanpaTanggalNol(Sequel.cariIsi(
                "select date_format(tgl_keluar,'%d-%m-%Y') from kamar_inap "
                + "where no_rawat=? and tgl_keluar<>'0000-00-00' order by tgl_keluar desc limit 1", TNoRw.getText())));
        TKeluarJam.setText(TKeluarTgl.getText().isEmpty() ? "" : teksAman(Sequel.cariIsi(
                "select jam_keluar from kamar_inap "
                + "where no_rawat=? and tgl_keluar<>'0000-00-00' order by tgl_keluar desc limit 1", TNoRw.getText())));

        // Update 4 Header Fields
        txtHdrRuang.setText(TRuang.getText().isEmpty() ? "—" : TRuang.getText());
        txtHdrMasuk.setText(TMasukTgl.getText().isEmpty() ? "—" : (TMasukTgl.getText() + " " + TMasukJam.getText()));
        if (TKeluarTgl.getText().isEmpty()) {
            txtHdrKeluar.setText("Kosong = masih dirawat");
            txtHdrKeluar.setForeground(new Color(0x9A, 0xA1, 0x9C));
        } else {
            txtHdrKeluar.setText(TKeluarTgl.getText() + " " + TKeluarJam.getText());
            txtHdrKeluar.setForeground(new Color(0x1C, 0x1F, 0x1D));
        }
        txtHdrLama.setText(hitungLamaRawat(TMasukTgl.getText(), TKeluarTgl.getText()));
        lblValLokasi.setText(TRuang.getText().isEmpty() ? "—" : TRuang.getText());
    }

    private String hitungLamaRawat(String tglMasuk, String tglKeluar) {
        if (tglMasuk.isEmpty() || tglKeluar.isEmpty()) return "—";
        try {
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            Date d1 = df.parse(tglMasuk);
            Date d2 = df.parse(tglKeluar);
            long diff = (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24);
            return Math.max(1, diff) + " hari";
        } catch (Exception e) {
            return "—";
        }
    }

    private String tanpaTanggalNol(String s) {
        if (s == null || s.trim().isEmpty() || s.startsWith("00-00-0000")) return "";
        return s;
    }

    private void praIsiDariSistem() {
        String noRawat = TNoRw.getText();

        String kdPoli = teksAman(Sequel.cariIsi("select kd_poli from reg_periksa where no_rawat=?", noRawat));
        if (!kdPoli.isEmpty()) {
            MasukMelalui.setSelectedItem(KD_POLI_IGD.equals(kdPoli) ? "IGD" : "Poliklinik");
        }

        DiagnoseSementara.setText(pertamaTerisi(
                Sequel.cariIsi("select diagnosa_awal from resume_pasien_ranap where no_rawat=?", noRawat),
                Sequel.cariIsi("select diagnosa_awal from kamar_inap where no_rawat=? order by tgl_masuk asc limit 1", noRawat)));
        AlergiTerhadap.setText(teksAman(
                Sequel.cariIsi("select alergi from resume_pasien_ranap where no_rawat=?", noRawat)));
        DiagnoseAkhir.setText(pertamaTerisi(
                Sequel.cariIsi("select diagnosa_utama from resume_pasien_ranap where no_rawat=?", noRawat),
                Sequel.cariIsi("select diagnosa_akhir from kamar_inap where no_rawat=? order by tgl_masuk desc limit 1", noRawat)));
        KodeIcd.setText(teksAman(
                Sequel.cariIsi("select kd_diagnosa_utama from resume_pasien_ranap where no_rawat=?", noRawat)));
        DiagnoseLain.setText(teksAman(Sequel.cariIsi(
                "select concat_ws(', ',nullif(diagnosa_sekunder,''),nullif(diagnosa_sekunder2,''),"
                + "nullif(diagnosa_sekunder3,''),nullif(diagnosa_sekunder4,'')) "
                + "from resume_pasien_ranap where no_rawat=?", noRawat)));

        praIsiTindakan(noRawat);
        praIsiKeluar(noRawat);

        String kdDokter = pertamaTerisi(
                Sequel.cariIsi("select kd_dokter from dpjp_ranap where no_rawat=? limit 1", noRawat),
                Sequel.cariIsi("select kd_dokter from reg_periksa where no_rawat=?", noRawat));
        if (!kdDokter.isEmpty()) {
            KdDokterMerawat.setText(kdDokter);
            NmDokterMerawat.setText(Sequel.CariDokter(kdDokter));
            TNipDokter.setText(nipDokter(kdDokter, NmDokterMerawat.getText()));
        }
        hitungProgres();
    }

    private void praIsiTindakan(String noRawat) {
        int slot = 0;
        try {
            ps = koneksi.prepareStatement(
                    "select operasi.tgl_operasi,operasi.operator1,ifnull(paket_operasi.nm_perawatan,operasi.kode_paket) as nm_tindakan "
                    + "from operasi left join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                    + "where operasi.no_rawat=? order by operasi.tgl_operasi");
            try {
                ps.setString(1, noRawat);
                rs = ps.executeQuery();
                while (rs.next() && slot < 4) {
                    Tindakan[slot].setText(teksAman(rs.getString("nm_tindakan")));
                    String tgl = rs.getString("tgl_operasi");
                    if (tgl != null && !tgl.isEmpty()) Valid.SetTgl2(TindakanTgl[slot], tgl);
                    String op = teksAman(rs.getString("operator1"));
                    TindakanKdDokter[slot].setText(op);
                    TindakanNmDokter[slot].setText(op.isEmpty() ? "" : Sequel.CariDokter(op));
                    slot++;
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }

        try {
            ps = koneksi.prepareStatement(
                    "select prosedur_pasien.kode,ifnull(icd9.deskripsi_panjang,prosedur_pasien.kode) as nm_prosedur "
                    + "from prosedur_pasien left join icd9 on prosedur_pasien.kode=icd9.kode "
                    + "where prosedur_pasien.no_rawat=? order by prosedur_pasien.prioritas");
            try {
                ps.setString(1, noRawat);
                rs = ps.executeQuery();
                while (rs.next() && slot < 4) {
                    Tindakan[slot].setText(teksAman(rs.getString("nm_prosedur")));
                    slot++;
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    private void praIsiKeluar(String noRawat) {
        String stts = teksAman(Sequel.cariIsi(
                "select stts_pulang from kamar_inap where no_rawat=? and stts_pulang<>'Pindah Kamar' "
                + "order by tgl_masuk desc limit 1", noRawat));
        String keadaan = teksAman(Sequel.cariIsi(
                "select keadaan from resume_pasien_ranap where no_rawat=?", noRawat));
        String caraKeluar = teksAman(Sequel.cariIsi(
                "select cara_keluar from resume_pasien_ranap where no_rawat=?", noRawat));

        if ("Meninggal".equalsIgnoreCase(stts) || "Meninggal".equalsIgnoreCase(keadaan)) {
            RbKeluarMeninggal.setSelected(true);
        } else if ("Sembuh".equalsIgnoreCase(stts) || "Sehat".equalsIgnoreCase(stts) || "Sembuh".equalsIgnoreCase(keadaan)) {
            RbKeluarSembuh.setSelected(true);
        } else if ("Membaik".equalsIgnoreCase(stts) || "Membaik".equalsIgnoreCase(keadaan) || "Berobat Jalan".equalsIgnoreCase(stts)) {
            RbKeluarSembuh.setSelected(true);
        } else if ("APS".equalsIgnoreCase(stts) || "Pulang Paksa".equalsIgnoreCase(stts)
                || "Atas Permintaan Sendiri".equalsIgnoreCase(stts) || "Pulang Atas Permintaan Sendiri".equalsIgnoreCase(caraKeluar)) {
            RbKeluarPulangPaksa.setSelected(true);
        } else if ("Rujuk".equalsIgnoreCase(stts) || "Pindah RS".equalsIgnoreCase(caraKeluar)) {
            RbKeluarDirujuk.setSelected(true);
        }

        if (RbKeluarMeninggal.isSelected()) {
            String jam = Sequel.cariIsi(
                    "select timestampdiff(hour,concat(min(tgl_masuk),' ',min(jam_masuk)),"
                    + "concat(max(tgl_keluar),' ',max(jam_keluar))) from kamar_inap "
                    + "where no_rawat=? and tgl_keluar<>'0000-00-00'", noRawat);
            if (jam != null && !jam.trim().isEmpty()) {
                try {
                    CaraKeluarMeninggal.setSelectedItem(
                            Integer.parseInt(jam.trim()) < 48 ? "< 48 Jam Perawatan" : "≥ 48 Jam Perawatan");
                } catch (NumberFormatException ignored) {}
            }
            String tglKeluar = Sequel.cariIsi(
                    "select concat(tgl_keluar,' ',jam_keluar) from kamar_inap "
                    + "where no_rawat=? and tgl_keluar<>'0000-00-00' order by tgl_keluar desc limit 1", noRawat);
            if (tglKeluar != null && !tglKeluar.trim().isEmpty()) {
                Valid.SetTgl2(MeninggalTgl, tglKeluar);
            }
        }

        String kontrol = Sequel.cariIsi(
                "select kontrol from resume_pasien_ranap where no_rawat=?", noRawat);
        if (kontrol != null && !kontrol.trim().isEmpty() && !kontrol.startsWith("0000")) {
            RbKunjunganYa.setSelected(true);
            Valid.SetTgl2(KunjunganUlangTgl, kontrol);
        } else {
            RbKunjunganTidak.setSelected(true);
        }
    }

    private String pertamaTerisi(String... calon) {
        for (String c : calon) {
            if (c != null && !c.trim().isEmpty()) return c;
        }
        return "";
    }

    // =================================================================
    //  RINGKASAN HTML
    // =================================================================

    private void bangunRingkasanHtml() {
        StringBuilder h = new StringBuilder();
        h.append("<html><table width='100%' cellpadding='2' cellspacing='0'>");
        judulHtml(h, "IDENTITAS");
        barisHtml(h, "No.Rawat", TNoRw.getText());
        barisHtml(h, "No.RM / Nama", TNoRM.getText() + " / " + TPasien.getText());
        barisHtml(h, "Tempat/Tgl Lahir", TmpLahir.getText() + " / " + TglLahir.getText());
        barisHtml(h, "Usia / L-P", TUsia.getText() + " / " + Jk.getText());
        barisHtml(h, "Alamat", TAlamat.getText());
        barisHtml(h, "Telepon", TTelepon.getText());
        barisHtml(h, "Status / Agama", TStatus.getText() + " / " + TAgama.getText());
        barisHtml(h, "Pekerjaan / Pendidikan", TPekerjaan.getText() + " / " + TPendidikan.getText());
        barisHtml(h, "Nama Ibu / Ayah", TNamaIbu.getText() + " / " + NamaAyah.getText());
        barisHtml(h, "Suami / Istri", NamaSuami.getText() + " / " + NamaIstri.getText());

        judulHtml(h, "MASUK & KELUAR");
        barisHtml(h, "Masuk", TMasukTgl.getText() + " " + TMasukJam.getText());
        barisHtml(h, "Dirawat di Ruang", TRuang.getText());
        barisHtml(h, "Masuk Melalui", String.valueOf(MasukMelalui.getSelectedItem()));
        barisHtml(h, "Dikirim Oleh", DikirimOleh.getText());
        barisHtml(h, "Kasus Polisi", teksAman(pilihan(bgKasusPolisi)));
        barisHtml(h, "Keluar", TKeluarTgl.getText() + " " + TKeluarJam.getText());
        barisHtml(h, "Keadaan Keluar", getPilihanKeadaanKeluar());
        barisHtml(h, "Rujuk ke / Sebab", RujukKe.getText());

        judulHtml(h, "DIAGNOSA & TINDAKAN");
        barisHtml(h, "Diagnose Sementara", DiagnoseSementara.getText());
        barisHtml(h, "Alergi Terhadap", AlergiTerhadap.getText());
        barisHtml(h, "Diagnose Akhir", DiagnoseAkhir.getText());
        barisHtml(h, "Komplikasi", Komplikasi.getText());
        barisHtml(h, "Diagnose Lain", DiagnoseLain.getText());
        barisHtml(h, "Kode ICD", KodeIcd.getText());
        String[] labelTindakan = {"Operasi Ke 1", "Operasi Ke 2", "Operasi Khusus", "Tindakan Lain"};
        for (int n = 0; n < 4; n++) {
            if (!Tindakan[n].getText().trim().isEmpty()) {
                barisHtml(h, labelTindakan[n], Tindakan[n].getText()
                        + " (" + TindakanTgl[n].getSelectedItem() + ") "
                        + TindakanNmDokter[n].getText());
            }
        }

        judulHtml(h, "DOKTER YANG MERAWAT");
        barisHtml(h, "Nama", NmDokterMerawat.getText());
        barisHtml(h, "NIP", TNipDokter.getText());

        if (RbKeluarMeninggal.isSelected()) {
            judulHtml(h, "SEBAB KEMATIAN");
            barisHtml(h, "Cara Keluar", String.valueOf(CaraKeluarMeninggal.getSelectedItem()));
            barisHtml(h, "a. Penyebab langsung", SkPenyakitA.getText() + " (" + SkLamaA.getText() + ")");
            barisHtml(h, "b. Disebabkan oleh", SkPenyakitB.getText() + " (" + SkLamaB.getText() + ")");
            barisHtml(h, "c. Disebabkan oleh", SkPenyakitC.getText() + " (" + SkLamaC.getText() + ")");
            barisHtml(h, "Penyakit lain", SkPenyakitLain.getText() + " (" + SkLamaLain.getText() + ")");
            barisHtml(h, "Pemberi Keterangan", NmPemberiKeterangan.getText());
        }
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

    // =================================================================
    //  CETAK
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

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        param.put("finger_dokter", tandaTangan(
                tbObat.getValueAt(tbObat.getSelectedRow(), 5) == null
                        ? "" : tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString(),
                tbObat.getValueAt(tbObat.getSelectedRow(), 6) == null
                        ? "" : tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString()));

        String qry = sqlCetak(noRawat);
        if ("PDF".equals(mode)) {
            Valid.MyReportqrypdf("rptRingkasanPasienMasukKeluarRM17.jasper", "report",
                    "::[ Ringkasan Pasien Masuk Keluar ]::", qry, param);
        } else {
            Valid.MyReportqry("rptRingkasanPasienMasukKeluarRM17.jasper", "report",
                    "::[ Ringkasan Pasien Masuk Keluar ]::", qry, param);
        }
    }

    private String tandaTangan(String kdDokter, String namaDokter) {
        String finger = Sequel.cariIsi(
                "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id "
                + "where pegawai.nik=?", kdDokter);
        return "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                + "\nDitandatangani secara elektronik oleh " + namaDokter
                + "\nID " + (finger == null || finger.isEmpty() ? kdDokter : finger);
    }

    private String sqlCetak(String noRawat) {
        return "select r.*,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tmp_lahir,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.no_tlp,pasien.stts_nikah,"
                + "pasien.agama,pasien.pekerjaan,pasien.pnd,pasien.nm_ibu,"
                + "concat(ifnull(pasien.alamat,''),', ',ifnull(kelurahan.nm_kel,''),', ',"
                + "ifnull(kecamatan.nm_kec,''),', ',ifnull(kabupaten.nm_kab,'')) as alamat,"
                + "ifnull(kelurahan.nm_kel,'') as nm_kel,ifnull(kecamatan.nm_kec,'') as nm_kec,"
                + "ifnull(kabupaten.nm_kab,'') as nm_kab,ifnull(propinsi.nm_prop,'') as nm_prop,"
                + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as usia,"
                + "(select date_format(min(ki.tgl_masuk),'%d-%m-%Y') from kamar_inap ki where ki.no_rawat=r.no_rawat) as tgl_masuk,"
                + "(select ki.jam_masuk from kamar_inap ki where ki.no_rawat=r.no_rawat order by ki.tgl_masuk asc limit 1) as jam_masuk,"
                + "(select bangsal.nm_bangsal from kamar_inap ki inner join kamar on ki.kd_kamar=kamar.kd_kamar "
                + "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal where ki.no_rawat=r.no_rawat "
                + "order by ki.tgl_masuk desc limit 1) as nm_ruang,"
                + "(select date_format(ki.tgl_keluar,'%d-%m-%Y') from kamar_inap ki where ki.no_rawat=r.no_rawat "
                + "and ki.tgl_keluar<>'0000-00-00' order by ki.tgl_keluar desc limit 1) as tgl_keluar,"
                + "(select ki.jam_keluar from kamar_inap ki where ki.no_rawat=r.no_rawat "
                + "and ki.tgl_keluar<>'0000-00-00' order by ki.tgl_keluar desc limit 1) as jam_keluar,"
                + "dokter.nm_dokter as nm_dokter_merawat,"
                + "(select pegawai.nik from pegawai where pegawai.nik=r.kd_dokter_merawat "
                + "or pegawai.nama=dokter.nm_dokter limit 1) as nip_dokter,"
                + "pemberi.nm_dokter as nm_pemberi_keterangan,"
                + "d1.nm_dokter as nm_tindakan_1_dokter,d2.nm_dokter as nm_tindakan_2_dokter,"
                + "d3.nm_dokter as nm_tindakan_3_dokter,d4.nm_dokter as nm_tindakan_4_dokter "
                + "from ringkasan_pasien_masuk_keluar r "
                + "inner join reg_periksa on r.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join kelurahan on pasien.kd_kel=kelurahan.kd_kel "
                + "left join kecamatan on pasien.kd_kec=kecamatan.kd_kec "
                + "left join kabupaten on pasien.kd_kab=kabupaten.kd_kab "
                + "left join propinsi on pasien.kd_prop=propinsi.kd_prop "
                + "left join dokter on r.kd_dokter_merawat=dokter.kd_dokter "
                + "left join dokter pemberi on r.kd_pemberi_keterangan=pemberi.kd_dokter "
                + "left join dokter d1 on r.tindakan_1_dokter=d1.kd_dokter "
                + "left join dokter d2 on r.tindakan_2_dokter=d2.kd_dokter "
                + "left join dokter d3 on r.tindakan_3_dokter=d3.kd_dokter "
                + "left join dokter d4 on r.tindakan_4_dokter=d4.kd_dokter "
                + "where r.no_rawat='" + noRawat + "'";
    }

    // =================================================================
    //  PUBLIC API DIBUTUHKAN PEMANGGIL (DlgKamarInap / frmUtama)
    // =================================================================

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        muatIdentitas();
        if (adaRecord(norwt)) {
            muatRecord(norwt);
        } else {
            praIsiDariSistem();
        }
        bangunRingkasanHtml();
        bukaBagian(0);
        hitungProgres();
    }

    private boolean adaRecord(String noRawat) {
        String ada = Sequel.cariIsi(
                "select no_rawat from ringkasan_pasien_masuk_keluar where no_rawat=?", noRawat);
        return ada != null && !ada.trim().isEmpty();
    }

    private void muatRecord(String noRawat) {
        try {
            ps = koneksi.prepareStatement("select * from ringkasan_pasien_masuk_keluar where no_rawat=?");
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
        BtnSimpan.setEnabled(akses.getringkasan_pasien_masuk_keluar());
        BtnHapus.setEnabled(akses.getringkasan_pasien_masuk_keluar());
        BtnEdit.setEnabled(akses.getringkasan_pasien_masuk_keluar());
    }

    public void isRawat() {
        // Compatibility hook for caller if invoked
    }

    // =================================================================
    //  INFRASTRUKTUR
    // =================================================================

    private void runBackground(Runnable task) {
        if (ceksukses || executor.isShutdown() || executor.isTerminated() || !isDisplayable()) return;
        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) setCursor(Cursor.getDefaultCursor());
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
