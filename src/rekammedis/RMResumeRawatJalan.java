package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import kepegawaian.DlgCariPetugas;
import java.awt.BasicStroke;
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
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import widget.ThemeConfig;

/**
 * RESUME RAWAT JALAN (RM 01) - RSUD Demang Sepulau Raya.
 *
 * <p><b>Berkunci NO.RM, bukan no_rawat.</b> Keputusan user: satu lembar untuk
 * satu PASIEN, menghimpun seluruh kunjungan rawat jalannya. Itulah sebabnya
 * kolom DPJP ada di TIAP BARIS pada kertas - tiap baris bisa dokter yang
 * berbeda. Berbeda dari RM 32 dan RM 33 yang keduanya berkunci
 * {@code no_rawat}.</p>
 *
 * <p><b>Form ini hanya memiliki BLOK KEPALA.</b> Tujuh kolom grid pada kertas
 * sudah diisi dokter lewat {@code DlgRawatJalan} setiap kunjungan dan tersimpan
 * di {@code pemeriksaan_ralan}; gridnya dirakit saat menampilkan ringkasan dan
 * saat mencetak, tidak diketik ulang di sini. Menggandakannya berarti dua
 * sumber kebenaran untuk catatan klinis yang sama - kalau dokter memperbaiki
 * SOAP-nya, salinan di sini jadi basi tanpa ada yang tahu.</p>
 *
 * <p><b>Apa yang bisa diambil dari DlgRawatJalan - hasil penelusuran:</b></p>
 * <ul>
 *   <li>ALERGI OBAT: ada {@code TAlergi} yang disimpan ke
 *       {@code pemeriksaan_ralan.alergi}, tapi per kunjungan, hanya
 *       varchar(80) satu baris, dan TERISI 0 DARI 66 BARIS pada data yang ada.
 *       Praktis tidak ada yang bisa diambil - diketik manual.</li>
 *   <li>RIWAYAT KESEHATAN: {@code BtnRiwayat} membuka
 *       {@link RMRiwayatPerawatan} (37 ribu baris) yang menghimpun riwayat dari
 *       semua modul, tapi itu PENAMPIL, bukan sumber tujuh baris berkurasi.
 *       Yang benar-benar bisa dipakai adalah diagnosa lampau + tahun -
 *       lihat {@link #usulkanRiwayat()}.</li>
 *   <li>PERHATIAN KHUSUS: DlgRawatJalan cuma punya TOMBOL yang membuka
 *       asesmennya (Risiko Jatuh Anak/Dewasa/Geriatri/Lansia/Neonatus/
 *       Psikiatri, Korban Kekerasan, Kekerasan pada Perempuan), tidak menyimpan
 *       hasilnya; hasilnya tersebar di 8+ tabel. Diketik manual.</li>
 * </ul>
 *
 * <p>Ditulis tangan dengan {@link GridBagLayout} tanpa berkas {@code .form} dan
 * tanpa blok GEN, mengikuti preseden RM 20/17-01/21/33. Tanpa gerbang hak
 * akses, mengikuti RM 21 dan RM 33.</p>
 */
public final class RMResumeRawatJalan extends javax.swing.JDialog {

    private static final int BARIS_ALERGI = 3;
    private static final int BARIS_RIWAYAT = 7;
    /** Token presentasi yang disamakan dengan Pengkajian Awal Medis IGD. */
    private static final Color WARNA_HEADER_IGD = new Color(0x12, 0x43, 0x3F);
    private static final Color WARNA_BORDER_FIELD_IGD = new Color(0xD8, 0xDB, 0xD6);

    /**
     * Lima penanda tetap "Perhatian Khusus": label yang ditampilkan dan nama
     * kolomnya. Dipakai bersama oleh tata letak, {@link #KOLOM_RM01},
     * {@link #nilaiRm01()}, dan pemuatan record.
     */
    private static final String[][] PERHATIAN = {
        {"LANSIA", "khusus_lansia"},
        {"DISABILITAS", "khusus_disabilitas"},
        {"RESIKO KEKERASAN", "khusus_resiko_kekerasan"},
        {"RESIKO JATUH", "khusus_resiko_jatuh"},
        {"DNR", "khusus_dnr"}
    };

    /**
     * Nama kolom {@code resume_rawat_jalan} di luar {@code no_rkm_medis}.
     * Urutannya WAJIB sama persis dengan {@link #nilaiRm01()} - dijamin karena
     * keduanya dirakit dengan perulangan yang bentuknya identik.
     */
    private static final String[] KOLOM_RM01 = rakitKolom();

    private static String[] rakitKolom() {
        List<String> k = new ArrayList<>();
        k.add("tanggal");
        k.add("jam");
        for (int n = 1; n <= BARIS_ALERGI; n++) {
            k.add("alergi" + n);
        }
        for (int n = 1; n <= BARIS_RIWAYAT; n++) {
            k.add("riwayat" + n);
            k.add("tahun" + n);
        }
        for (String[] p : PERHATIAN) {
            k.add(p[1]);
        }
        k.add("khusus_lainnya");
        k.add("nip");
        return k.toArray(new String[0]);
    }

    private final DefaultTableModel tabMode;
    private final DefaultTableModel tabModeKunjungan;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;

    // ---------- Kerangka ----------
    private widget.InternalFrame internalFrame1, internalFrame2, internalFrame3;
    private widget.panelisi panelGlass8, panelGlass9;
    private javax.swing.JTabbedPane TabRawat;
    private widget.ScrollPane scrollInput;
    private widget.PanelBiasa FormInput;
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnPrint, BtnAll, BtnKeluar, BtnPetugas;
    private JPanel panelKontenSeksi;
    private JPanel[] kartuSeksi;
    private JPanel[] barisNavigasi;
    private JPanel[] navigasiBawah;
    private JLabel[] labelHitungSeksi;
    private JLabel lblProgressPersen, lblProgressCatatan, lblStatusFooter;
    private javax.swing.JProgressBar barKelengkapan;
    private JLabel lblNamaPasienKartu, lblInfoPasienKartu, lblNoRmKartu;
    private AvatarPasien avatarPasien;
    private widget.Button BtnTampilSemua;
    private int seksiAktif = 0;
    private boolean tampilSemuaSeksi = false;

    // ---------- Header ----------
    private widget.TextBox TNoRM, TPasien, TglLahir, TUmur, Jk;
    private widget.Tanggal TglResume;
    private widget.ComboBox JamResume, MenitResume, DetikResume;
    private widget.TextBox KdPetugas, NmPetugas;
    private DlgCariPetugas petugas;

    // ---------- Badan ----------
    private final widget.TextBox[] Alergi = new widget.TextBox[BARIS_ALERGI];
    private final widget.TextBox[] Riwayat = new widget.TextBox[BARIS_RIWAYAT];
    private final widget.TextBox[] Tahun = new widget.TextBox[BARIS_RIWAYAT];
    private widget.Button BtnUsulRiwayat;
    private final widget.CekBox[] CekKhusus = new widget.CekBox[PERHATIAN.length];
    private widget.TextBox KhususLainnya;
    private widget.Table tbKunjungan;
    private widget.Button BtnFinalisasiKunjungan, BtnCetakLanjutan;
    private widget.Label LCountKunjungan;

    // ---------- Tab Data ----------
    private widget.ScrollPane Scroll, ScrollHTML;
    private widget.Table tbObat;
    private widget.TextBox TCari, TNoRM1, TPasien1;
    private widget.Button BtnCari, BtnPrint1;
    private widget.Label LCount;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JMenuItem MnCetak;
    private widget.PanelBiasa PanelAccor;
    private widget.panelisi FormMenu;
    private widget.CekBox ChkAccor;
    private widget.editorpane LoadHTML;

    public RMResumeRawatJalan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.RM", "Nama Pasien", "Tgl.Lahir", "J.K.", "Tanggal", "Jam", "NIP", "Petugas"
        }) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
        };
        tabModeKunjungan = new DefaultTableModel(null, new Object[]{
            "Tanggal/Jam", "DPJP", "Diagnosa", "Uraian Klinis", "Rencana Penatalaksanaan",
            "Catatan Penting", "Nama dan E-Paraf", "Status Paraf", "No.Rawat",
            "Tanggal", "Jam", "NIP", "ID Snapshot", "Hash Snapshot", "Nama Pemeriksa"
        }) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
        };

        rakitKerangka();
        rakitHeader();
        rakitBadan();
        rakitTabData();

        widget.IconStyle.pasang(this);
        ChkAccor.setSelected(false);
        isMenu();
        setSize(1250, 700);
    }

    // =================================================================
    //  KERANGKA
    // =================================================================

    private void rakitKerangka() {
        internalFrame1 = new widget.InternalFrame();
        internalFrame1.setBorder(null);
        internalFrame1.setFont(new java.awt.Font(ThemeConfig.FONT_FAMILY, java.awt.Font.ITALIC, 12));
        internalFrame1.setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(WARNA_HEADER_IGD);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        topBar.setPreferredSize(new Dimension(44, 34));

        JPanel judulBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 7));
        judulBar.setOpaque(false);
        JLabel judul = labelModern("Resume Rawat Jalan", 13, Font.BOLD, Color.WHITE);
        JLabel kode = labelModern("RM 01", 10, Font.BOLD, new Color(0xA9, 0xDB, 0xD8));
        kode.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2B, 0x5C, 0x57)),
                BorderFactory.createEmptyBorder(1, 6, 1, 6)));
        judulBar.add(judul);
        judulBar.add(kode);
        topBar.add(judulBar, BorderLayout.WEST);

        JLabel rumahSakit = labelModern(akses.getnamars(), 11, Font.PLAIN,
                new Color(0xC5, 0xD7, 0xD4));
        topBar.add(rumahSakit, BorderLayout.EAST);
        internalFrame1.add(topBar, BorderLayout.NORTH);

        panelGlass8 = new widget.panelisi();
        panelGlass8.setPreferredSize(new Dimension(44, 56));
        panelGlass8.setLayout(new BorderLayout(10, 0));
        panelGlass8.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeConfig.BORDER),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        panelGlass8.setBackground(ThemeConfig.SURFACE);

        BtnSimpan = tombolFooter("Simpan", "/picture/save-16x16.png", 'S');
        BtnSimpan.addActionListener(evt -> simpan());
        BtnBatal = tombolFooter("Baru", "/picture/New.png", 'N');
        BtnBatal.addActionListener(evt -> emptTeks());
        BtnHapus = tombolFooter("Hapus", "/picture/hapus.png", 'H');
        BtnHapus.addActionListener(evt -> aksiHapus());
        BtnEdit = tombolFooter("Ganti", "/picture/edit.png", 'G');
        BtnEdit.addActionListener(evt -> aksiGanti());
        BtnPrint = tombolFooter("Cetak", "/picture/print.png", 'P');
        BtnPrint.addActionListener(evt -> cetakTerpilih());
        BtnAll = tombolFooter("Semua", "/picture/repeat.png", 'M');
        BtnAll.addActionListener(evt -> { TCari.setText(""); runBackground(() -> tampil()); });
        BtnKeluar = tombolFooter("Keluar", "/picture/exit.png", 'K');
        BtnKeluar.addActionListener(evt -> dispose());

        JPanel aksiKiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        aksiKiri.setOpaque(false);
        aksiKiri.add(BtnSimpan);
        aksiKiri.add(BtnBatal);
        aksiKiri.add(BtnEdit);
        aksiKiri.add(BtnHapus);
        aksiKiri.add(BtnPrint);
        aksiKiri.add(BtnAll);

        JPanel aksiKanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        aksiKanan.setOpaque(false);
        lblStatusFooter = labelModern("Belum ada isian klinis", 11, Font.PLAIN, ThemeConfig.TEXT_MUTED);
        aksiKanan.add(lblStatusFooter);
        aksiKanan.add(BtnKeluar);

        panelGlass8.add(aksiKiri, BorderLayout.WEST);
        panelGlass8.add(aksiKanan, BorderLayout.EAST);
        internalFrame1.add(panelGlass8, BorderLayout.PAGE_END);

        TabRawat = new javax.swing.JTabbedPane();
        TabRawat.setFont(new java.awt.Font(ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        TabRawat.putClientProperty("JTabbedPane.tabType", "underlined");
        TabRawat.putClientProperty("JTabbedPane.showContentSeparator", Boolean.TRUE);
        TabRawat.putClientProperty("JTabbedPane.tabHeight", 34);
        TabRawat.setBorder(null);
        TabRawat.setBackground(ThemeConfig.SURFACE);
        TabRawat.setForeground(ThemeConfig.TEXT_NORMAL);

        internalFrame2 = new widget.InternalFrame();
        internalFrame2.setBorder(null);
        internalFrame2.setLayout(new BorderLayout());

        FormInput = new widget.PanelBiasa();
        FormInput.setLayout(new BorderLayout(18, 0));
        FormInput.setBackground(ThemeConfig.SURFACE);
        FormInput.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        scrollInput = new widget.ScrollPane();
        scrollInput.setViewportView(FormInput);
        scrollInput.setBorder(null);
        scrollInput.getVerticalScrollBar().setUnitIncrement(16);
        internalFrame2.add(scrollInput, BorderLayout.CENTER);
        TabRawat.addTab("Input Resume", internalFrame2);

        internalFrame1.add(TabRawat, BorderLayout.CENTER);
        getContentPane().add(internalFrame1, BorderLayout.CENTER);
    }

    private widget.Button tombolFooter(String teks, String ikon, char mnemonic) {
        widget.Button b = new widget.Button();
        try {
            b.setIcon(new javax.swing.ImageIcon(getClass().getResource(ikon)));
        } catch (Exception e) {
            // Ikon raster lama disembunyikan widget.Button kalau tombol punya
            // teks; IconStyle.pasang() yang mengisi ikon sesungguhnya.
        }
        b.setMnemonic(mnemonic);
        b.setText(teks);
        b.setToolTipText("Alt+" + mnemonic);
        b.setPreferredSize(new Dimension(94, 34));
        b.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
        b.setFocusPainted(false);
        b.setRoundRect(false);
        b.setBackground(ThemeConfig.SURFACE);
        b.setForeground("Hapus".equals(teks) ? ThemeConfig.DANGER : ThemeConfig.TEXT_NORMAL);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder("Simpan".equals(teks)
                        ? ThemeConfig.PRIMARY : ThemeConfig.BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return b;
    }

    // =================================================================
    //  HELPER TATA LETAK
    // =================================================================

    private GridBagConstraints konstrain(int gridx, int gridy) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 5, 3, 5);
        // fill=NONE disengaja: kalau HORIZONTAL, kotak sempit ikut melebar
        // mengikuti kolom terlebar sehingga tampak kosong-melompong.
        c.fill = GridBagConstraints.NONE;
        return c;
    }

    private void tambahPengisiKolom(JPanel panel, int gridxFiller) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridxFiller;
        c.gridy = 0;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(Box.createHorizontalGlue(), c);
    }

    /**
     * Menyerap sisa tinggi di baris paling bawah supaya isi rapat ke ATAS.
     * Tanpa ini GridBagLayout men-TENGAH-kan seluruh grid secara vertikal -
     * bug yang baru ketahuan di RM 17-01 setelah form-nya benar-benar dirender.
     */
    private void tambahPengisiBaris(JPanel panel, int gridyFiller) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = gridyFiller;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.VERTICAL;
        panel.add(Box.createVerticalGlue(), c);
    }

    private widget.PanelBiasa panelSeksi(String judul) {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new GridBagLayout());
        TitledBorder tb = javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(ThemeConfig.BORDER), judul);
        tb.setTitleFont(ThemeConfig.FONT_BOLD);
        tb.setTitleColor(ThemeConfig.PRIMARY);
        p.setBorder(tb);
        return p;
    }

    private widget.Label labelKolom(String teks) {
        widget.Label l = new widget.Label();
        l.setText(teks + " :");
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    /** Label bebas rata kiri. widget.Label defaultnya rata KANAN. */
    private widget.Label teks(String isi) {
        widget.Label l = new widget.Label();
        l.setText(isi);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    private JPanel baris(Component... komponen) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        for (Component k : komponen) {
            p.add(k);
        }
        return p;
    }

    private widget.TextBox kotak(int lebar) {
        widget.TextBox t = new widget.TextBox();
        // WAJIB: constructor widget.TextBox diam-diam setSize(WIDTH,23) dengan
        // WIDTH=1 (ImageObserver.WIDTH). Tanpa ini field menciut ~1px.
        t.setPreferredSize(new Dimension(lebar, 23));
        buatKotakRM01(t);
        return t;
    }

    private widget.TextBox kotakBaca(int lebar) {
        widget.TextBox t = kotak(lebar);
        t.setEditable(false);
        return t;
    }

    private widget.CekBox cek(String teks) {
        widget.CekBox c = new widget.CekBox();
        c.setText(teks);
        return c;
    }

    private widget.Tanggal tanggal(String format, int lebar) {
        widget.Tanggal t = new widget.Tanggal();
        // widget.Tanggal buatan tangan TIDAK otomatis dapat displayFormat
        // seperti versi Form Editor; tanpa ini substring(11,19) meledak.
        t.setDisplayFormat(format);
        t.setDate(new Date());
        t.setPreferredSize(new Dimension(lebar, 23));
        buatKotakRM01(t);
        return t;
    }

    /** Jam RM01 dipisah dari tanggal mengikuti pola header Keperawatan IGD. */
    private void initWaktuResume() {
        JamResume = comboWaktu(24);
        MenitResume = comboWaktu(60);
        DetikResume = comboWaktu(60);
        Calendar sekarang = Calendar.getInstance();
        JamResume.setSelectedItem(String.format("%02d", sekarang.get(Calendar.HOUR_OF_DAY)));
        MenitResume.setSelectedItem(String.format("%02d", sekarang.get(Calendar.MINUTE)));
        DetikResume.setSelectedItem(String.format("%02d", sekarang.get(Calendar.SECOND)));
    }

    private widget.ComboBox comboWaktu(int batas) {
        String[] pilihan = new String[batas];
        for (int n = 0; n < batas; n++) pilihan[n] = String.format("%02d", n);
        widget.ComboBox c = new widget.ComboBox();
        c.setModel(new javax.swing.DefaultComboBoxModel(pilihan));
        c.setPreferredSize(new Dimension(58, 26));
        buatKotakRM01(c);
        return c;
    }

    private JPanel buatPanelWaktuResume() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(194, 26));
        panel.add(JamResume);
        panel.add(pemisahWaktu());
        panel.add(MenitResume);
        panel.add(pemisahWaktu());
        panel.add(DetikResume);
        return panel;
    }

    private JLabel pemisahWaktu() {
        JLabel pemisah = labelModern(":", 11, Font.PLAIN, ThemeConfig.TEXT_MUTED);
        pemisah.setHorizontalAlignment(SwingConstants.CENTER);
        pemisah.setPreferredSize(new Dimension(4, 26));
        return pemisah;
    }

    private String jamResume() {
        return JamResume.getSelectedItem() + ":" + MenitResume.getSelectedItem()
                + ":" + DetikResume.getSelectedItem();
    }

    private String tanggalDanJamResume() {
        return TglResume.getSelectedItem() + " " + jamResume();
    }

    private void setJamResume(String jam) {
        String[] bagian = (jam == null ? "" : jam).split(":");
        JamResume.setSelectedItem(bagian.length > 0 && bagian[0].matches("\\d{2}") ? bagian[0] : "00");
        MenitResume.setSelectedItem(bagian.length > 1 && bagian[1].matches("\\d{2}") ? bagian[1] : "00");
        DetikResume.setSelectedItem(bagian.length > 2 && bagian[2].matches("\\d{2}") ? bagian[2] : "00");
    }

    /**
     * Mengikuti pola Pengkajian Awal Medis IGD: FlatLaf perlu diberi arc=0
     * pada komponen aktual supaya field benar-benar siku-siku, bukan hanya
     * border pembungkusnya yang kotak.
     */
    private static void buatKotakRM01(javax.swing.JComponent komponen) {
        komponen.putClientProperty("Component.arc", 0);
        komponen.putClientProperty("TextComponent.arc", 0);
        komponen.putClientProperty("JTextField.arc", 0);
        komponen.putClientProperty("ComboBox.arc", 0);
        komponen.putClientProperty("JComponent.roundRect", false);

        if (komponen instanceof javax.swing.text.JTextComponent) {
            komponen.setBorder(BorderFactory.createLineBorder(WARNA_BORDER_FIELD_IGD));
            komponen.setBackground(Color.WHITE);
            komponen.setOpaque(true);
            komponen.putClientProperty("FlatLaf.style",
                    "background: #FFFFFF; inactiveBackground: #FFFFFF; disabledBackground: #FFFFFF;");
        } else if (komponen instanceof javax.swing.JComboBox) {
            komponen.setBorder(BorderFactory.createLineBorder(WARNA_BORDER_FIELD_IGD));
            komponen.setBackground(Color.WHITE);
            komponen.setOpaque(true);
            komponen.putClientProperty("FlatLaf.style",
                    "background: #FFFFFF; disabledBackground: #FFFFFF; buttonBackground: #FFFFFF; editableBackground: #FFFFFF;");
        }
    }

    private void tambahSeksi(JPanel panel, int gridy) {
        GridBagConstraints c = konstrain(0, gridy);
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        FormInput.add(panel, c);
    }

    // =================================================================
    //  HEADER
    // =================================================================

    private void rakitHeader() {
        TNoRM = kotakBaca(110);
        TPasien = kotakBaca(230);
        TglLahir = kotakBaca(100);
        TUmur = kotakBaca(110);
        Jk = kotakBaca(90);
        TglResume = tanggal("dd-MM-yyyy", 164);
        initWaktuResume();
        KdPetugas = kotakBaca(110);
        NmPetugas = kotakBaca(210);
        KdPetugas.setVisible(false);
        BtnPetugas = tombolPilihPetugas();
        NmPetugas.setToolTipText("Pilih petugas pengisi");

        JPanel banner = new JPanel(new GridBagLayout());
        banner.setBackground(ThemeConfig.SURFACE);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConfig.BORDER_SOFT),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JPanel kartuPasien = new JPanel(new GridBagLayout());
        kartuPasien.setBackground(ThemeConfig.SURFACE_ALT);
        kartuPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        Dimension ukuranKartu = new Dimension(310, 112);
        kartuPasien.setMinimumSize(ukuranKartu);
        kartuPasien.setPreferredSize(ukuranKartu);

        JLabel judulKartu = labelModern("IDENTITAS PASIEN", 10, Font.BOLD, ThemeConfig.TEXT_MUTED);
        JLabel terkunci = labelModern("TERKUNCI", 9, Font.BOLD, ThemeConfig.TEXT_FAINT);
        terkunci.setOpaque(true);
        terkunci.setBackground(ThemeConfig.BORDER_SOFT);
        terkunci.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        avatarPasien = new AvatarPasien();
        avatarPasien.setPreferredSize(new Dimension(40, 40));
        avatarPasien.setMinimumSize(new Dimension(40, 40));
        lblNamaPasienKartu = labelModern("Pasien belum dipilih", 15, Font.BOLD, ThemeConfig.TEXT_NORMAL);
        lblInfoPasienKartu = labelModern("-", 12, Font.PLAIN, ThemeConfig.TEXT_MUTED);
        lblNoRmKartu = labelModern("No. RM    -", 11, Font.PLAIN, ThemeConfig.TEXT_MUTED);

        GridBagConstraints k = new GridBagConstraints();
        k.gridx = 0;
        k.gridy = 0;
        k.gridheight = 4;
        k.anchor = GridBagConstraints.NORTHWEST;
        k.insets = new Insets(0, 0, 0, 10);
        kartuPasien.add(avatarPasien, k);
        k.gridx = 1;
        k.gridy = 0;
        k.gridheight = 1;
        k.weightx = 1.0;
        k.fill = GridBagConstraints.HORIZONTAL;
        k.insets = new Insets(0, 0, 0, 0);
        kartuPasien.add(judulKartu, k);
        k.gridx = 2;
        k.weightx = 0.0;
        k.fill = GridBagConstraints.NONE;
        k.anchor = GridBagConstraints.EAST;
        kartuPasien.add(terkunci, k);
        k.gridx = 1;
        k.gridwidth = 2;
        k.weightx = 1.0;
        k.fill = GridBagConstraints.HORIZONTAL;
        k.anchor = GridBagConstraints.WEST;
        k.gridy = 1;
        k.insets = new Insets(7, 0, 0, 0);
        kartuPasien.add(lblNamaPasienKartu, k);
        k.gridy = 2;
        k.insets = new Insets(2, 0, 0, 0);
        kartuPasien.add(lblInfoPasienKartu, k);
        k.gridy = 3;
        k.insets = new Insets(7, 0, 0, 0);
        kartuPasien.add(lblNoRmKartu, k);

        JPanel fieldHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        fieldHeader.setOpaque(false);
        fieldHeader.add(buatFieldHeader("TANGGAL", TglResume));
        fieldHeader.add(buatFieldHeader("JAM", buatPanelWaktuResume()));
        fieldHeader.add(buatFieldHeader("PETUGAS PENGISI", buatLookupPetugas()));

        GridBagConstraints f = new GridBagConstraints();
        f.gridx = 0;
        f.gridy = 0;
        f.fill = GridBagConstraints.VERTICAL;
        f.weighty = 1.0;
        f.insets = new Insets(0, 0, 0, 16);
        banner.add(kartuPasien, f);
        f.gridx = 1;
        f.weightx = 1.0;
        f.fill = GridBagConstraints.HORIZONTAL;
        f.anchor = GridBagConstraints.NORTH;
        f.insets = new Insets(0, 0, 0, 0);
        banner.add(fieldHeader, f);
        internalFrame2.add(banner, BorderLayout.NORTH);

        DocumentListener sinkronIdentitas = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { perbaruiKartuPasien(); }
            @Override public void removeUpdate(DocumentEvent e) { perbaruiKartuPasien(); }
            @Override public void changedUpdate(DocumentEvent e) { perbaruiKartuPasien(); }
        };
        TNoRM.getDocument().addDocumentListener(sinkronIdentitas);
        TPasien.getDocument().addDocumentListener(sinkronIdentitas);
        TglLahir.getDocument().addDocumentListener(sinkronIdentitas);
        TUmur.getDocument().addDocumentListener(sinkronIdentitas);
        Jk.getDocument().addDocumentListener(sinkronIdentitas);
        perbaruiKartuPasien();
    }

    // =================================================================
    //  BADAN
    // =================================================================

    private void rakitBadan() {
        kartuSeksi = new JPanel[3];
        barisNavigasi = new JPanel[3];
        navigasiBawah = new JPanel[3];
        labelHitungSeksi = new JLabel[3];

        JPanel panelAlergi = panelIsiSeksi();
        for (int n = 0; n < BARIS_ALERGI; n++) {
            Alergi[n] = kotak(560);
            panelAlergi.add(labelModern("ALERGI " + (n + 1), 11, Font.BOLD,
                    ThemeConfig.TEXT_MUTED), konstrainIsi(0, n, 0.0));
            panelAlergi.add(Alergi[n], konstrainIsi(1, n, 1.0));
        }
        kartuSeksi[0] = buatKartuSeksi(0, "Alergi Obat",
                "Tiga catatan alergi obat pasien", panelAlergi);

        JPanel panelRiwayat = panelIsiSeksi();

        BtnUsulRiwayat = new widget.Button();
        BtnUsulRiwayat.setText("Ambil Riwayat Diagnosis");
        BtnUsulRiwayat.setToolTipText("Isi baris yang masih kosong dari diagnosis terdahulu beserta tahunnya");
        BtnUsulRiwayat.setPreferredSize(new Dimension(230, 32));
        BtnUsulRiwayat.setRoundRect(false);
        BtnUsulRiwayat.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER));
        BtnUsulRiwayat.addActionListener(evt -> usulkanRiwayat());
        GridBagConstraints cb = konstrainIsi(1, 0, 1.0);
        panelRiwayat.add(BtnUsulRiwayat, cb);

        for (int n = 0; n < BARIS_RIWAYAT; n++) {
            Riwayat[n] = kotak(500);
            Tahun[n] = kotak(72);
            panelRiwayat.add(labelModern("RIWAYAT " + (n + 1), 11, Font.BOLD,
                    ThemeConfig.TEXT_MUTED), konstrainIsi(0, n + 1, 0.0));
            panelRiwayat.add(baris(Riwayat[n], labelModern("Tahun", 11, Font.PLAIN,
                    ThemeConfig.TEXT_MUTED), Tahun[n]), konstrainIsi(1, n + 1, 1.0));
        }
        kartuSeksi[1] = buatKartuSeksi(1, "Riwayat Kesehatan Pasien",
                "Catatan perawatan, pengobatan, operasi, atau tindakan medis sebelumnya", panelRiwayat);

        JPanel panelKhusus = panelIsiSeksi();
        for (int n = 0; n < PERHATIAN.length; n++) {
            CekKhusus[n] = cek(PERHATIAN[n][0]);
            CekKhusus[n].setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
            CekKhusus[n].setBackground(ThemeConfig.SURFACE);
            panelKhusus.add(CekKhusus[n], konstrainIsi(n % 2, n / 2, 1.0));
        }
        KhususLainnya = kotak(330);
        panelKhusus.add(baris(labelModern("LAINNYA", 11, Font.BOLD,
                ThemeConfig.TEXT_MUTED), KhususLainnya), konstrainIsi(1, 2, 1.0));
        kartuSeksi[2] = buatKartuSeksi(2, "Perhatian Khusus",
                "Penanda kondisi yang memerlukan perhatian saat pelayanan", panelKhusus);

        panelKontenSeksi = new JPanel();
        panelKontenSeksi.setOpaque(false);
        panelKontenSeksi.setLayout(new BoxLayout(panelKontenSeksi, BoxLayout.Y_AXIS));

        for (int n = 0; n < kartuSeksi.length; n++) {
            kartuSeksi[n].remove(navigasiBawah[n]);
        }

        JPanel kolomKiri = new JPanel();
        kolomKiri.setOpaque(false);
        kolomKiri.setLayout(new BoxLayout(kolomKiri, BoxLayout.Y_AXIS));
        kartuSeksi[0].setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        kartuSeksi[2].setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        kolomKiri.add(kartuSeksi[0]);
        kolomKiri.add(Box.createVerticalStrut(12));
        kolomKiri.add(kartuSeksi[2]);

        JPanel blokKlinis = new JPanel(new GridBagLayout());
        blokKlinis.setOpaque(false);
        blokKlinis.setAlignmentX(Component.LEFT_ALIGNMENT);
        blokKlinis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 510));
        GridBagConstraints bk = new GridBagConstraints();
        bk.gridy = 0;
        bk.fill = GridBagConstraints.BOTH;
        bk.anchor = GridBagConstraints.NORTHWEST;
        bk.weighty = 1.0;
        bk.gridx = 0;
        bk.weightx = 0.42;
        bk.insets = new Insets(0, 0, 0, 12);
        blokKlinis.add(kolomKiri, bk);
        bk.gridx = 1;
        bk.weightx = 0.58;
        bk.insets = new Insets(0, 0, 0, 0);
        blokKlinis.add(kartuSeksi[1], bk);

        panelKontenSeksi.add(buatProgresRingkas());
        panelKontenSeksi.add(Box.createVerticalStrut(12));
        panelKontenSeksi.add(blokKlinis);
        panelKontenSeksi.add(Box.createVerticalStrut(14));
        panelKontenSeksi.add(buatPanelKunjungan());
        FormInput.add(panelKontenSeksi, BorderLayout.CENTER);

        pasangPemantauKelengkapan();
        perbaruiKelengkapan();
    }

    private JPanel buatProgresRingkas() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(ThemeConfig.SURFACE_ALT);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        p.add(labelModern("KELENGKAPAN DATA KEPALA RM01", 10, Font.BOLD,
                ThemeConfig.TEXT_MUTED), BorderLayout.WEST);

        JPanel nilai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nilai.setOpaque(false);
        lblProgressCatatan = labelModern("0 dari 16 elemen terisi", 10, Font.PLAIN,
                ThemeConfig.TEXT_FAINT);
        barKelengkapan = new javax.swing.JProgressBar(0, 100);
        barKelengkapan.setPreferredSize(new Dimension(150, 7));
        barKelengkapan.setForeground(ThemeConfig.PRIMARY);
        barKelengkapan.setBackground(ThemeConfig.BORDER_SOFT);
        barKelengkapan.setBorder(null);
        lblProgressPersen = labelModern("0%", 12, Font.BOLD, ThemeConfig.TEXT_NORMAL);
        nilai.add(lblProgressCatatan);
        nilai.add(barKelengkapan);
        nilai.add(lblProgressPersen);
        p.add(nilai, BorderLayout.EAST);
        return p;
    }

    private JPanel buatPanelKunjungan() {
        JPanel kartu = new JPanel(new BorderLayout(0, 8));
        kartu.setBackground(ThemeConfig.SURFACE);
        kartu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        kartu.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);
        JPanel judul = new JPanel();
        judul.setOpaque(false);
        judul.setLayout(new BoxLayout(judul, BoxLayout.Y_AXIS));
        judul.add(labelModern("Riwayat Kunjungan Rawat Jalan", 14, Font.BOLD,
                ThemeConfig.TEXT_NORMAL));
        judul.add(labelModern("Catatan SOAP otomatis; e-paraf internal hanya oleh DPJP kunjungan",
                10, Font.PLAIN, ThemeConfig.TEXT_FAINT));
        kepala.add(judul, BorderLayout.WEST);
        LCountKunjungan = new widget.Label();
        LCountKunjungan.setHorizontalAlignment(SwingConstants.RIGHT);
        LCountKunjungan.setText("0 kunjungan");
        kepala.add(LCountKunjungan, BorderLayout.EAST);
        kartu.add(kepala, BorderLayout.NORTH);

        tbKunjungan = new widget.Table();
        tbKunjungan.setModel(tabModeKunjungan);
        tbKunjungan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbKunjungan.setRowHeight(54);
        tbKunjungan.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        int[] lebar = {105, 140, 170, 245, 220, 170, 180};
        for (int n = 0; n < lebar.length; n++) {
            tbKunjungan.getColumnModel().getColumn(n).setPreferredWidth(lebar[n]);
        }
        for (int n = 7; n < tabModeKunjungan.getColumnCount(); n++) {
            TableColumn kolom = tbKunjungan.getColumnModel().getColumn(n);
            kolom.setMinWidth(0);
            kolom.setMaxWidth(0);
            kolom.setPreferredWidth(0);
        }
        tbKunjungan.getSelectionModel().addListSelectionListener(e -> perbaruiAksiKunjungan());
        widget.ScrollPane scroll = new widget.ScrollPane();
        scroll.setViewportView(tbKunjungan);
        scroll.setPreferredSize(new Dimension(1100, 220));
        teruskanScrollSaatBatas(scroll);
        kartu.add(scroll, BorderLayout.CENTER);

        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        aksi.setOpaque(false);
        BtnFinalisasiKunjungan = tombolNavigasi("Finalisasi & E-Paraf");
        BtnFinalisasiKunjungan.setPreferredSize(new Dimension(170, 32));
        BtnFinalisasiKunjungan.addActionListener(e -> finalisasiKunjungan());
        BtnCetakLanjutan = tombolNavigasi("Cetak Lanjutan");
        BtnCetakLanjutan.setPreferredSize(new Dimension(135, 32));
        BtnCetakLanjutan.addActionListener(e -> cetakLanjutanKunjungan());
        aksi.add(BtnFinalisasiKunjungan);
        aksi.add(BtnCetakLanjutan);
        aksi.add(labelModern("Hanya akun DPJP; e-paraf internal tidak menggantikan paraf dan stempel basah.",
                10, Font.ITALIC, ThemeConfig.TEXT_FAINT));
        kartu.add(aksi, BorderLayout.SOUTH);
        perbaruiAksiKunjungan();
        return kartu;
    }

    /**
     * JScrollPane di dalam halaman biasanya menahan mouse-wheel. Saat isi tabel
     * sudah mencapai batas, teruskan gerakan ke scroll halaman utama agar form
     * tidak terasa macet ketika pointer berada di atas tabel kunjungan.
     */
    private void teruskanScrollSaatBatas(javax.swing.JScrollPane scrollDalam) {
        scrollDalam.addMouseWheelListener(e -> {
            javax.swing.JScrollBar dalam = scrollDalam.getVerticalScrollBar();
            boolean keBawah = e.getWheelRotation() > 0;
            boolean diBatas = !dalam.isVisible()
                    || (keBawah
                    ? dalam.getValue() + dalam.getVisibleAmount() >= dalam.getMaximum()
                    : dalam.getValue() <= dalam.getMinimum());
            if (diBatas && scrollInput != null) {
                javax.swing.JScrollBar luar = scrollInput.getVerticalScrollBar();
                int arah = keBawah ? 1 : -1;
                int langkah = Math.max(16, luar.getUnitIncrement(arah))
                        * Math.max(1, Math.abs(e.getWheelRotation())) * 3;
                luar.setValue(luar.getValue() + (keBawah ? langkah : -langkah));
                e.consume();
            }
        });
    }

    private JPanel panelIsiSeksi() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeConfig.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 22, 16, 22));
        return panel;
    }

    private GridBagConstraints konstrainIsi(int x, int y, double bobotX) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.weightx = bobotX;
        c.fill = bobotX > 0 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(7, x == 0 ? 0 : 14, 7, 0);
        return c;
    }

    private JPanel buatKartuSeksi(int indeks, String judul, String subjudul, JPanel isi) {
        JPanel kartu = new JPanel(new BorderLayout());
        kartu.setBackground(ThemeConfig.SURFACE);
        kartu.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT));
        kartu.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.setMaximumSize(new Dimension(Integer.MAX_VALUE, indeks == 1 ? 430 : 245));

        JPanel kepala = new JPanel(new BorderLayout(12, 0));
        kepala.setBackground(ThemeConfig.SURFACE_ALT);
        kepala.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConfig.BORDER_SOFT),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        JLabel nomor = labelModern(String.valueOf(indeks + 1), 11, Font.BOLD, Color.WHITE);
        nomor.setOpaque(true);
        nomor.setBackground(ThemeConfig.PRIMARY);
        nomor.setHorizontalAlignment(SwingConstants.CENTER);
        nomor.setPreferredSize(new Dimension(26, 26));
        kepala.add(nomor, BorderLayout.WEST);

        JPanel teksJudul = new JPanel();
        teksJudul.setOpaque(false);
        teksJudul.setLayout(new BoxLayout(teksJudul, BoxLayout.Y_AXIS));
        teksJudul.add(labelModern(judul, 14, Font.BOLD, ThemeConfig.TEXT_NORMAL));
        teksJudul.add(Box.createVerticalStrut(2));
        teksJudul.add(labelModern(subjudul, 11, Font.PLAIN, ThemeConfig.TEXT_FAINT));
        kepala.add(teksJudul, BorderLayout.CENTER);
        labelHitungSeksi[indeks] = labelModern("Belum diisi", 10, Font.BOLD, ThemeConfig.WARN);
        labelHitungSeksi[indeks].setOpaque(true);
        labelHitungSeksi[indeks].setBackground(ThemeConfig.WARN_BG);
        labelHitungSeksi[indeks].setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        kepala.add(labelHitungSeksi[indeks], BorderLayout.EAST);

        kartu.add(kepala, BorderLayout.NORTH);
        kartu.add(isi, BorderLayout.CENTER);
        navigasiBawah[indeks] = buatNavigasiBawah(indeks);
        kartu.add(navigasiBawah[indeks], BorderLayout.SOUTH);
        return kartu;
    }

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(280, 480));

        JPanel progres = new JPanel();
        progres.setLayout(new BoxLayout(progres, BoxLayout.Y_AXIS));
        progres.setBackground(ThemeConfig.SURFACE);
        progres.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT),
                BorderFactory.createEmptyBorder(12, 13, 12, 13)));
        progres.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        progres.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel kepalaProgres = new JPanel(new BorderLayout());
        kepalaProgres.setOpaque(false);
        kepalaProgres.add(labelModern("KELENGKAPAN ISIAN", 10, Font.BOLD,
                ThemeConfig.TEXT_MUTED), BorderLayout.WEST);
        lblProgressPersen = labelModern("0%", 14, Font.BOLD, ThemeConfig.TEXT_NORMAL);
        kepalaProgres.add(lblProgressPersen, BorderLayout.EAST);
        progres.add(kepalaProgres);
        progres.add(Box.createVerticalStrut(9));
        barKelengkapan = new javax.swing.JProgressBar(0, 100);
        barKelengkapan.setValue(0);
        barKelengkapan.setStringPainted(false);
        barKelengkapan.setForeground(ThemeConfig.PRIMARY);
        barKelengkapan.setBackground(ThemeConfig.BORDER_SOFT);
        barKelengkapan.setBorder(null);
        barKelengkapan.setPreferredSize(new Dimension(250, 6));
        barKelengkapan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        barKelengkapan.setAlignmentX(Component.LEFT_ALIGNMENT);
        progres.add(barKelengkapan);
        progres.add(Box.createVerticalStrut(8));
        lblProgressCatatan = labelModern("0 dari 16 elemen terisi", 10, Font.PLAIN,
                ThemeConfig.TEXT_FAINT);
        progres.add(lblProgressCatatan);
        sidebar.add(progres);
        sidebar.add(Box.createVerticalStrut(12));

        JPanel navigasi = new JPanel();
        navigasi.setLayout(new BoxLayout(navigasi, BoxLayout.Y_AXIS));
        navigasi.setBackground(ThemeConfig.SURFACE);
        navigasi.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT));
        navigasi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 158));
        navigasi.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel kepalaNav = labelModern("BAGIAN RESUME", 10, Font.BOLD, ThemeConfig.TEXT_MUTED);
        kepalaNav.setBorder(BorderFactory.createEmptyBorder(11, 13, 11, 13));
        kepalaNav.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigasi.add(kepalaNav);
        String[] judul = {"Alergi Obat", "Riwayat Kesehatan", "Perhatian Khusus"};
        for (int n = 0; n < judul.length; n++) {
            final int indeks = n;
            JPanel barisNav = new JPanel(new BorderLayout(8, 0));
            barisNav.setBackground(ThemeConfig.SURFACE);
            barisNav.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            barisNav.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            barisNav.setAlignmentX(Component.LEFT_ALIGNMENT);
            barisNav.add(labelModern(String.valueOf(n + 1), 10, Font.BOLD,
                    ThemeConfig.TEXT_FAINT), BorderLayout.WEST);
            barisNav.add(labelModern(judul[n], 12, Font.PLAIN,
                    ThemeConfig.TEXT_NORMAL), BorderLayout.CENTER);
            barisNav.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            barisNav.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    tampilkanSeksi(indeks);
                }
            });
            barisNavigasi[n] = barisNav;
            navigasi.add(barisNav);
        }
        sidebar.add(navigasi);
        sidebar.add(Box.createVerticalStrut(12));

        BtnTampilSemua = new widget.Button();
        BtnTampilSemua.setText("Tampilkan Semua Bagian");
        BtnTampilSemua.setRoundRect(false);
        BtnTampilSemua.setPreferredSize(new Dimension(280, 34));
        BtnTampilSemua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        BtnTampilSemua.setAlignmentX(Component.LEFT_ALIGNMENT);
        BtnTampilSemua.setBorder(BorderFactory.createLineBorder(ThemeConfig.PRIMARY));
        BtnTampilSemua.addActionListener(e -> {
            if (tampilSemuaSeksi) {
                tampilkanSeksi(seksiAktif);
            } else {
                tampilkanSemuaSeksi();
            }
        });
        sidebar.add(BtnTampilSemua);
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JPanel buatNavigasiBawah(int indeks) {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(ThemeConfig.SURFACE_ALT);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeConfig.BORDER_SOFT),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        widget.Button sebelum = tombolNavigasi(indeks == 0 ? "Awal" : "‹ Sebelumnya");
        sebelum.setEnabled(indeks > 0);
        if (indeks > 0) sebelum.addActionListener(e -> tampilkanSeksi(indeks - 1));
        widget.Button sesudah = tombolNavigasi(indeks == kartuSeksi.length - 1 ? "Akhir" : "Berikutnya ›");
        sesudah.setEnabled(indeks < kartuSeksi.length - 1);
        if (indeks < kartuSeksi.length - 1) sesudah.addActionListener(e -> tampilkanSeksi(indeks + 1));
        nav.add(sebelum, BorderLayout.WEST);
        nav.add(labelModern("Bagian " + (indeks + 1) + " dari " + kartuSeksi.length,
                11, Font.PLAIN, ThemeConfig.TEXT_FAINT), BorderLayout.CENTER);
        ((JLabel) nav.getComponent(1)).setHorizontalAlignment(SwingConstants.CENTER);
        nav.add(sesudah, BorderLayout.EAST);
        return nav;
    }

    private widget.Button tombolNavigasi(String teks) {
        widget.Button b = new widget.Button();
        b.setText(teks);
        b.setRoundRect(false);
        b.setPreferredSize(new Dimension(112, 30));
        b.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER));
        return b;
    }

    private void tampilkanSeksi(int indeks) {
        if (BtnTampilSemua == null) {
            return;
        }
        seksiAktif = Math.max(0, Math.min(indeks, kartuSeksi.length - 1));
        tampilSemuaSeksi = false;
        panelKontenSeksi.removeAll();
        panelKontenSeksi.setLayout(new BoxLayout(panelKontenSeksi, BoxLayout.Y_AXIS));
        panelKontenSeksi.add(kartuSeksi[seksiAktif]);
        for (JPanel nav : navigasiBawah) nav.setVisible(true);
        BtnTampilSemua.setText("Tampilkan Semua Bagian");
        perbaruiNavigasi();
        panelKontenSeksi.revalidate();
        panelKontenSeksi.repaint();
        SwingUtilities.invokeLater(() -> scrollInput.getVerticalScrollBar().setValue(0));
    }

    private void tampilkanSemuaSeksi() {
        if (BtnTampilSemua == null) {
            return;
        }
        tampilSemuaSeksi = true;
        panelKontenSeksi.removeAll();
        panelKontenSeksi.setLayout(new BoxLayout(panelKontenSeksi, BoxLayout.Y_AXIS));
        for (int n = 0; n < kartuSeksi.length; n++) {
            navigasiBawah[n].setVisible(false);
            panelKontenSeksi.add(kartuSeksi[n]);
            if (n < kartuSeksi.length - 1) panelKontenSeksi.add(Box.createVerticalStrut(14));
        }
        BtnTampilSemua.setText("Tampilkan Per Bagian");
        perbaruiNavigasi();
        panelKontenSeksi.revalidate();
        panelKontenSeksi.repaint();
        SwingUtilities.invokeLater(() -> scrollInput.getVerticalScrollBar().setValue(0));
    }

    private void perbaruiNavigasi() {
        if (barisNavigasi == null || barisNavigasi[0] == null) {
            return;
        }
        for (int n = 0; n < barisNavigasi.length; n++) {
            boolean aktif = !tampilSemuaSeksi && n == seksiAktif;
            barisNavigasi[n].setBackground(aktif ? new Color(0xF1, 0xF7, 0xF6) : ThemeConfig.SURFACE);
            barisNavigasi[n].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, aktif ? 3 : 0, 1, 0,
                            aktif ? ThemeConfig.PRIMARY : ThemeConfig.BORDER_SOFT),
                    BorderFactory.createEmptyBorder(9, aktif ? 9 : 12, 9, 12)));
        }
    }

    private JPanel buatFieldHeader(String label, Component komponen) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel l = labelModern(label, 10, Font.BOLD, ThemeConfig.TEXT_MUTED);
        p.add(l, BorderLayout.NORTH);
        Dimension ukuran = komponen.getPreferredSize();
        int lebar = ukuran != null && ukuran.width > 0 ? ukuran.width : 0;
        komponen.setPreferredSize(new Dimension(lebar, 26));
        if (komponen instanceof JComponent) {
            ((JComponent) komponen).putClientProperty("Component.arc", 0);
        }
        p.add(komponen, BorderLayout.CENTER);
        return p;
    }

    /** Field petugas hanya-baca dengan tombol pencarian master petugas. */
    private JPanel buatLookupPetugas() {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        p.setBorder(null);
        p.setPreferredSize(new Dimension(300, 26));

        NmPetugas.setPreferredSize(new Dimension(268, 26));
        NmPetugas.setBackground(Color.WHITE);
        NmPetugas.setOpaque(true);
        NmPetugas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WARNA_BORDER_FIELD_IGD),
                BorderFactory.createEmptyBorder(2, 8, 2, 4)));
        NmPetugas.putClientProperty("lookupField", Boolean.TRUE);
        NmPetugas.putClientProperty("Component.arc", 0);
        NmPetugas.putClientProperty("TextComponent.arc", 0);
        NmPetugas.putClientProperty("JTextField.arc", 0);
        NmPetugas.putClientProperty("JComponent.roundRect", false);

        BtnPetugas.setPreferredSize(new Dimension(28, 26));
        BtnPetugas.setBackground(Color.WHITE);
        BtnPetugas.setGlassColor(Color.WHITE);
        BtnPetugas.setRoundRect(false);
        BtnPetugas.setBorder(BorderFactory.createLineBorder(WARNA_BORDER_FIELD_IGD));
        BtnPetugas.putClientProperty("Component.arc", 0);
        BtnPetugas.putClientProperty("Button.arc", 0);
        BtnPetugas.putClientProperty("JButton.buttonType", "square");
        BtnPetugas.putClientProperty("JComponent.roundRect", false);

        p.add(NmPetugas, BorderLayout.CENTER);
        p.add(BtnPetugas, BorderLayout.EAST);
        return p;
    }

    private widget.Button tombolPilihPetugas() {
        widget.Button b = new widget.Button();
        try {
            b.setIcon(widget.IconStyle.muat("search.svg", 14, widget.IconStyle.ABU));
        } catch (Exception e) {
            b.setText("...");
        }
        b.setToolTipText("Pilih petugas pengisi");
        b.addActionListener(evt -> pilihPetugas());
        return b;
    }

    private void pilihPetugas() {
        if (petugas == null || !petugas.isDisplayable()) {
            petugas = new DlgCariPetugas(null, false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (petugas != null && petugas.getTable().getSelectedRow() != -1) {
                        int baris = petugas.getTable().getSelectedRow();
                        KdPetugas.setText(petugas.getTable().getValueAt(baris, 0).toString());
                        NmPetugas.setText(petugas.getTable().getValueAt(baris, 1).toString());
                    }
                    BtnPetugas.requestFocus();
                    petugas = null;
                }
            });
            petugas.setSize(Math.max(720, getWidth() - 20), Math.max(420, getHeight() - 20));
            petugas.setLocationRelativeTo(this);
        }
        if (petugas == null) {
            return;
        }
        if (petugas.isVisible()) {
            petugas.toFront();
            return;
        }
        petugas.isCek();
        petugas.emptTeks();
        petugas.setVisible(true);
    }

    private JLabel labelModern(String teks, int ukuran, int gaya, Color warna) {
        JLabel label = new JLabel(teks);
        label.setFont(new Font(ThemeConfig.FONT_FAMILY, gaya, ukuran));
        label.setForeground(warna);
        return label;
    }

    private void perbaruiKartuPasien() {
        if (lblNamaPasienKartu == null) return;
        String nama = TPasien.getText().trim();
        lblNamaPasienKartu.setText(nama.isEmpty() ? "Pasien belum dipilih" : nama.toUpperCase());
        String info = (Jk.getText().trim().isEmpty() ? "-" : Jk.getText().trim())
                + " · " + (TUmur.getText().trim().isEmpty() ? "-" : TUmur.getText().trim())
                + " · " + (TglLahir.getText().trim().isEmpty() ? "Tgl. lahir -" : TglLahir.getText().trim());
        lblInfoPasienKartu.setText(info);
        lblNoRmKartu.setText("No. RM    " + (TNoRM.getText().trim().isEmpty() ? "-" : TNoRM.getText().trim()));
        avatarPasien.setNama(nama);
    }

    private void pasangPemantauKelengkapan() {
        DocumentListener listener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { perbaruiKelengkapan(); }
            @Override public void removeUpdate(DocumentEvent e) { perbaruiKelengkapan(); }
            @Override public void changedUpdate(DocumentEvent e) { perbaruiKelengkapan(); }
        };
        for (widget.TextBox t : Alergi) t.getDocument().addDocumentListener(listener);
        for (widget.TextBox t : Riwayat) t.getDocument().addDocumentListener(listener);
        for (widget.TextBox t : Tahun) t.getDocument().addDocumentListener(listener);
        KhususLainnya.getDocument().addDocumentListener(listener);
        for (widget.CekBox c : CekKhusus) c.addItemListener(e -> perbaruiKelengkapan());
    }

    private void perbaruiKelengkapan() {
        if (lblProgressPersen == null) return;
        int alergi = 0;
        for (widget.TextBox t : Alergi) if (!t.getText().trim().isEmpty()) alergi++;
        int riwayat = 0;
        for (widget.TextBox t : Riwayat) if (!t.getText().trim().isEmpty()) riwayat++;
        int khusus = 0;
        for (widget.CekBox c : CekKhusus) if (c.isSelected()) khusus++;
        if (!KhususLainnya.getText().trim().isEmpty()) khusus++;
        int terisi = alergi + riwayat + khusus;
        int total = BARIS_ALERGI + BARIS_RIWAYAT + PERHATIAN.length + 1;
        int persen = Math.round((terisi * 100f) / total);
        lblProgressPersen.setText(persen + "%");
        barKelengkapan.setValue(persen);
        lblProgressCatatan.setText(terisi + " dari " + total + " elemen terisi");
        lblStatusFooter.setText(terisi == 0 ? "Belum ada isian klinis"
                : "Kelengkapan isian " + persen + "% · " + terisi + "/" + total);
        aturHitungSeksi(0, alergi, BARIS_ALERGI);
        aturHitungSeksi(1, riwayat, BARIS_RIWAYAT);
        aturHitungSeksi(2, khusus, PERHATIAN.length + 1);
    }

    private void aturHitungSeksi(int indeks, int terisi, int total) {
        JLabel l = labelHitungSeksi[indeks];
        l.setText(terisi == 0 ? "Belum diisi" : terisi + "/" + total + " terisi");
        l.setForeground(terisi > 0 ? ThemeConfig.OK : ThemeConfig.WARN);
        l.setBackground(terisi > 0 ? ThemeConfig.OK_BG : ThemeConfig.WARN_BG);
    }

    private static final class AvatarPasien extends JComponent {
        private String inisial = "--";

        void setNama(String nama) {
            String bersih = nama == null ? "" : nama.trim();
            if (bersih.isEmpty()) {
                inisial = "--";
            } else {
                String[] bagian = bersih.split("\\s+");
                inisial = bagian[0].substring(0, 1).toUpperCase();
                if (bagian.length > 1) inisial += bagian[bagian.length - 1].substring(0, 1).toUpperCase();
            }
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int sisi = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - sisi) / 2;
            int y = (getHeight() - sisi) / 2;
            g2.setColor(new Color(0xE9, 0xF5, 0xF2));
            g2.fillOval(x, y, sisi, sisi);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(ThemeConfig.PRIMARY);
            g2.drawOval(x, y, sisi, sisi);
            g2.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 13));
            int lebar = g2.getFontMetrics().stringWidth(inisial);
            int tinggi = g2.getFontMetrics().getAscent();
            g2.drawString(inisial, (getWidth() - lebar) / 2, (getHeight() + tinggi) / 2 - 2);
            g2.dispose();
        }
    }

    /**
     * "LAINNYA" pada kertas hanya punya kotak centang dan garis isian, tanpa
     * kolom penandanya sendiri - jadi cukup labelnya saja, dan yang disimpan
     * adalah teksnya.
     */
    private widget.Label cekLainnyaLabel() {
        return teks("LAINNYA :");
    }

    // =================================================================
    //  TAB DATA
    // =================================================================

    private void rakitTabData() {
        internalFrame3 = new widget.InternalFrame();
        internalFrame3.setBorder(null);
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetak = new javax.swing.JMenuItem();
        MnCetak.setText("Cetak Resume Rawat Jalan");
        MnCetak.addActionListener(evt -> cetakTerpilih());
        jPopupMenu1.add(MnCetak);

        tbObat = new widget.Table();
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbObat.setRowHeight(28);
        tbObat.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        tbObat.setGridColor(ThemeConfig.BORDER_SOFT);
        tbObat.setSelectionBackground(ThemeConfig.TABLE_SELECTION_BG);
        tbObat.setSelectionForeground(ThemeConfig.TABLE_SELECTION_TEXT);
        tbObat.getTableHeader().setBackground(ThemeConfig.TABLE_HEADER_BG);
        tbObat.getTableHeader().setForeground(ThemeConfig.TABLE_HEADER_TEXT);
        tbObat.getTableHeader().setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        tbObat.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tbObat.getTableHeader().setReorderingAllowed(false);
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        int[] lebar = {80, 190, 85, 60, 90, 70, 100, 170};
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
        Scroll.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT));
        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9 = new widget.panelisi();
        panelGlass9.setPreferredSize(new Dimension(44, 44));
        panelGlass9.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        panelGlass9.setBackground(ThemeConfig.SURFACE_ALT);
        panelGlass9.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, ThemeConfig.BORDER_SOFT));
        TCari = kotak(205);
        TCari.setPreferredSize(new Dimension(205, 26));
        TCari.putClientProperty("JTextField.placeholderText", "Ketik No. RM atau nama...");
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    runBackground(() -> tampil());
                }
            }
        });
        BtnCari = new widget.Button();
        javax.swing.Icon ikonCari = widget.IconStyle.muat(
                "search.svg", 14, widget.IconStyle.ABU);
        if (ikonCari != null) {
            BtnCari.setIcon(ikonCari);
        } else {
            BtnCari.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/picture/Search-16x16.png")));
        }
        BtnCari.setPreferredSize(new Dimension(28, 26));
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Terapkan pencarian No. RM / nama pasien (Alt+3)");
        BtnCari.setRoundRect(false);
        BtnCari.setBorder(BorderFactory.createLineBorder(WARNA_BORDER_FIELD_IGD));
        buatKotakRM01(BtnCari);
        BtnCari.addActionListener(evt -> runBackground(() -> tampil()));
        LCount = new widget.Label();
        LCount.setText("0");
        LCount.setHorizontalAlignment(SwingConstants.LEFT);
        LCount.setPreferredSize(new Dimension(60, 26));

        // Tidak ada penyaring tanggal: lembar ini berkunci PASIEN, bukan
        // kunjungan, jadi menyaringnya per tanggal justru menyembunyikan
        // lembar pasien yang resume-nya dibuat lama.
        widget.Label labelCari = teks("Cari (No. RM / Nama) :");
        labelCari.setPreferredSize(new Dimension(130, 26));
        panelGlass9.add(labelCari);
        panelGlass9.add(TCari);
        panelGlass9.add(BtnCari);
        widget.Label labelRecord = teks("Record :");
        labelRecord.setPreferredSize(new Dimension(55, 26));
        panelGlass9.add(labelRecord);
        panelGlass9.add(LCount);
        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

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

        TNoRM1 = kotakBaca(100);
        TNoRM1.setPreferredSize(new Dimension(100, 26));
        TPasien1 = kotakBaca(250);
        TPasien1.setPreferredSize(new Dimension(250, 26));
        BtnPrint1 = new widget.Button();
        javax.swing.Icon ikonCetak = widget.IconStyle.muat(
                "printer.svg", 14, widget.IconStyle.ABU);
        if (ikonCetak != null) {
            BtnPrint1.setIcon(ikonCetak);
        } else {
            BtnPrint1.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/picture/item (copy).png")));
        }
        BtnPrint1.setPreferredSize(new Dimension(28, 26));
        BtnPrint1.setRoundRect(false);
        BtnPrint1.setBorder(BorderFactory.createLineBorder(WARNA_BORDER_FIELD_IGD));
        buatKotakRM01(BtnPrint1);
        BtnPrint1.setToolTipText("Cetak resume pasien ini");
        BtnPrint1.addActionListener(evt -> cetakTerpilih());

        FormMenu = new widget.panelisi();
        FormMenu.setPreferredSize(new Dimension(44, 44));
        FormMenu.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 9));
        FormMenu.setBackground(ThemeConfig.SURFACE);
        widget.Label labelPasien = teks("Pasien :");
        labelPasien.setPreferredSize(new Dimension(55, 26));
        FormMenu.add(labelPasien);
        FormMenu.add(TNoRM1);
        FormMenu.add(TPasien1);
        FormMenu.add(BtnPrint1);

        ChkAccor = new widget.CekBox();
        ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png")));
        ChkAccor.setPreferredSize(new Dimension(15, 15));
        ChkAccor.addActionListener(evt -> isMenu());

        PanelAccor = new widget.PanelBiasa();
        PanelAccor.setLayout(new java.awt.BorderLayout(1, 1));
        PanelAccor.setPreferredSize(new Dimension(470, 44));
        PanelAccor.setBackground(ThemeConfig.SURFACE);
        PanelAccor.add(ChkAccor, java.awt.BorderLayout.WEST);
        PanelAccor.add(FormMenu, java.awt.BorderLayout.NORTH);
        PanelAccor.add(ScrollHTML, java.awt.BorderLayout.CENTER);
        internalFrame3.add(PanelAccor, java.awt.BorderLayout.EAST);

        TabRawat.addTab("Data Resume", internalFrame3);
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
    //  LAPISAN DATA
    // =================================================================

    private String[] nilaiRm01() {
        List<String> v = new ArrayList<>();
        v.add(Valid.SetTgl(TglResume.getSelectedItem() + ""));
        v.add(jamResume());
        for (int n = 0; n < BARIS_ALERGI; n++) {
            v.add(kosongKeNull(Alergi[n].getText()));
        }
        for (int n = 0; n < BARIS_RIWAYAT; n++) {
            v.add(kosongKeNull(Riwayat[n].getText()));
            // Tahun hanya bermakna kalau riwayatnya diisi - tanpa gerbang ini,
            // tahun yang tertinggal dari pengisian sebelumnya ikut tersimpan
            // untuk baris yang sebenarnya kosong.
            v.add(Riwayat[n].getText().trim().isEmpty() ? null : kosongKeNull(Tahun[n].getText()));
        }
        for (int n = 0; n < PERHATIAN.length; n++) {
            v.add(CekKhusus[n].isSelected() ? "Ya" : "Tidak");
        }
        v.add(kosongKeNull(KhususLainnya.getText()));
        v.add(KdPetugas.getText());
        return v.toArray(new String[0]);
    }

    private String kosongKeNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    /**
     * Mengisi baris riwayat yang MASIH KOSONG dari diagnosa kunjungan-kunjungan
     * sebelumnya.
     *
     * <p>Ini satu-satunya blok kepala yang benar-benar punya sumber di sistem.
     * Hasilnya disaring UNIK per penyakit - pada data nyata satu penyakit bisa
     * muncul berkali-kali karena pasien berobat berulang untuk keluhan yang
     * sama, dan tujuh baris di kertas akan habis oleh satu penyakit saja.</p>
     *
     * <p>Baris yang SUDAH diisi petugas tidak pernah ditimpa - ini usulan,
     * bukan pengganti penilaian petugas.</p>
     */
    private void usulkanRiwayat() {
        isiRiwayatDariDiagnosaLampau(true);
    }

    private int isiRiwayatDariDiagnosaLampau(boolean tampilkanPesan) {
        if (TNoRM.getText().trim().isEmpty()) {
            if (tampilkanPesan) {
                JOptionPane.showMessageDialog(rootPane, "Silahkan pilih pasien terlebih dahulu..!!");
            }
            return 0;
        }
        List<String[]> usul = new ArrayList<>();
        try {
            ps = koneksi.prepareStatement(
                    "select penyakit.nm_penyakit,max(year(reg_periksa.tgl_registrasi)) as tahun "
                    + "from reg_periksa "
                    + "inner join diagnosa_pasien on diagnosa_pasien.no_rawat=reg_periksa.no_rawat "
                    + "inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "
                    + "where reg_periksa.no_rkm_medis=? "
                    + "group by penyakit.nm_penyakit "
                    + "order by max(reg_periksa.tgl_registrasi) desc,penyakit.nm_penyakit limit " + BARIS_RIWAYAT);
            try {
                ps.setString(1, TNoRM.getText());
                rs = ps.executeQuery();
                while (rs.next()) {
                    usul.add(new String[]{teksAman(rs.getString("nm_penyakit")),
                        teksAman(rs.getString("tahun"))});
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }

        if (usul.isEmpty()) {
            if (tampilkanPesan) {
                JOptionPane.showMessageDialog(rootPane,
                        "Tidak ada diagnosis terdahulu yang tercatat untuk pasien ini.");
            }
            return 0;
        }
        List<String> sudahAda = new ArrayList<>();
        for (int n = 0; n < BARIS_RIWAYAT; n++) {
            String nilai = Riwayat[n].getText().trim();
            if (!nilai.isEmpty()) {
                sudahAda.add(nilai.toLowerCase());
            }
        }
        int terisi = 0;
        for (String[] kandidat : usul) {
            String kunci = kandidat[0].trim().toLowerCase();
            if (kunci.isEmpty() || sudahAda.contains(kunci)) {
                continue;
            }
            for (int n = 0; n < BARIS_RIWAYAT; n++) {
                if (Riwayat[n].getText().trim().isEmpty()) {
                    Riwayat[n].setText(kandidat[0]);
                    Tahun[n].setText(kandidat[1]);
                    sudahAda.add(kunci);
                    terisi++;
                    break;
                }
            }
        }
        if (tampilkanPesan) {
            JOptionPane.showMessageDialog(rootPane, terisi == 0
                    ? "Semua diagnosis terdahulu sudah ada atau seluruh baris riwayat telah terisi."
                    : terisi + " baris riwayat diisikan dari diagnosis terdahulu beserta tahunnya.\n"
                    + "Silahkan diperiksa dan disesuaikan sebelum disimpan.");
        }
        return terisi;
    }

    private void simpan() {
        if (TNoRM.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "Silahkan pilih pasien terlebih dahulu..!!");
            return;
        }
        if (KdPetugas.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "Petugas belum terisi..!!");
            return;
        }
        StringBuilder kol = new StringBuilder("no_rkm_medis");
        StringBuilder tanya = new StringBuilder("?");
        for (String k : KOLOM_RM01) {
            kol.append(",").append(k);
            tanya.append(",?");
        }
        try (PreparedStatement pstm = koneksi.prepareStatement(
                "insert into resume_rawat_jalan(" + kol + ") values(" + tanya + ")")) {
            pstm.setString(1, TNoRM.getText());
            String[] nilai = nilaiRm01();
            for (int n = 0; n < nilai.length; n++) {
                pstm.setString(n + 2, nilai[n]);
            }
            pstm.executeUpdate();
            runBackground(() -> tampil());
            TabRawat.setSelectedIndex(1);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan : " + e.getMessage()
                    + "\n\nKalau pesannya soal duplikat, pasien ini sudah punya lembar RM 01."
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
        StringBuilder set = new StringBuilder();
        for (String k : KOLOM_RM01) {
            if (set.length() > 0) {
                set.append(",");
            }
            set.append(k).append("=?");
        }
        String[] nilai = nilaiRm01();
        String[] param = new String[nilai.length + 1];
        System.arraycopy(nilai, 0, param, 0, nilai.length);
        param[nilai.length] = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();

        if (Sequel.mengedittf("resume_rawat_jalan", "no_rkm_medis=?", set.toString(),
                param.length, param)) {
            runBackground(() -> tampil());
            TabRawat.setSelectedIndex(1);
        }
    }

    private void aksiHapus() {
        if (tbObat.getSelectedRow() > -1) {
            if (Sequel.queryu2tf("delete from resume_rawat_jalan where no_rkm_medis=?", 1,
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
        String sql =
                "select a.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,a.tanggal,a.jam,a.nip,"
                + "ifnull(petugas.nama,'') as nm_petugas "
                + "from resume_rawat_jalan a "
                + "inner join pasien on a.no_rkm_medis=pasien.no_rkm_medis "
                + "left join petugas on a.nip=petugas.nip ";
        if (pakaiCari) {
            sql += "where a.no_rkm_medis like ? or pasien.nm_pasien like ? ";
        }
        return sql + "order by a.tanggal desc,a.jam desc";
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        boolean pakaiCari = !TCari.getText().trim().isEmpty();
        try {
            ps = koneksi.prepareStatement(sqlDaftar(pakaiCari));
            try {
                if (pakaiCari) {
                    ps.setString(1, "%" + TCari.getText() + "%");
                    ps.setString(2, "%" + TCari.getText() + "%");
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rkm_medis"), rs.getString("nm_pasien"),
                        rs.getDate("tgl_lahir"), rs.getString("jk"), rs.getString("tanggal"),
                        rs.getString("jam"), rs.getString("nip"), rs.getString("nm_petugas")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    private void getData() {
        if (tbObat.getSelectedRow() == -1) {
            return;
        }
        TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
        muatIdentitas();
        if (adaRecord(TNoRM.getText())) {
            muatRecord(TNoRM.getText());
        }
        isiRiwayatDariDiagnosaLampau(false);
        tampilKunjungan();
        bangunRingkasanHtml();
        ChkAccor.setSelected(true);
        isMenu();
    }

    private void isiDariRecord() throws Exception {
        String tgl = teksAman(rs.getString("tanggal"));
        String jam = teksAman(rs.getString("jam"));
        if (!tgl.isEmpty()) {
            Valid.SetTgl2(TglResume, tgl + " 00:00:00");
            setJamResume(jam);
        }
        for (int n = 0; n < BARIS_ALERGI; n++) {
            Alergi[n].setText(teksAman(rs.getString("alergi" + (n + 1))));
        }
        for (int n = 0; n < BARIS_RIWAYAT; n++) {
            Riwayat[n].setText(teksAman(rs.getString("riwayat" + (n + 1))));
            Tahun[n].setText(teksAman(rs.getString("tahun" + (n + 1))));
        }
        for (int n = 0; n < PERHATIAN.length; n++) {
            CekKhusus[n].setSelected("Ya".equals(rs.getString(PERHATIAN[n][1])));
        }
        KhususLainnya.setText(teksAman(rs.getString("khusus_lainnya")));

        String nip = teksAman(rs.getString("nip"));
        KdPetugas.setText(nip);
        NmPetugas.setText(nip.isEmpty() ? ""
                : teksAman(Sequel.cariIsi("select nama from petugas where nip=?", nip)));
    }

    private String teksAman(String s) {
        return s == null ? "" : s;
    }

    private String htmlAman(String s) {
        return teksAman(s).replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    public void emptTeks() {
        for (int n = 0; n < BARIS_ALERGI; n++) {
            Alergi[n].setText("");
        }
        for (int n = 0; n < BARIS_RIWAYAT; n++) {
            Riwayat[n].setText("");
            Tahun[n].setText("");
        }
        for (int n = 0; n < PERHATIAN.length; n++) {
            CekKhusus[n].setSelected(false);
        }
        KhususLainnya.setText("");
        KdPetugas.setText("");
        NmPetugas.setText("");
        TglResume.setDate(new Date());
        Calendar sekarang = Calendar.getInstance();
        setJamResume(String.format("%02d:%02d:%02d", sekarang.get(Calendar.HOUR_OF_DAY),
                sekarang.get(Calendar.MINUTE), sekarang.get(Calendar.SECOND)));
        Valid.tabelKosong(tabModeKunjungan);
        LCountKunjungan.setText("0 kunjungan");
        perbaruiAksiKunjungan();
        TabRawat.setSelectedIndex(0);
    }

    private void muatIdentitas() {
        try {
            ps = koneksi.prepareStatement(
                    "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                    + "timestampdiff(year,pasien.tgl_lahir,curdate()) as umur "
                    + "from pasien where pasien.no_rkm_medis=?");
            try {
                ps.setString(1, TNoRM.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    TPasien.setText(teksAman(rs.getString("nm_pasien")));
                    TglLahir.setText(teksAman(rs.getString("tgl_lahir")));
                    TUmur.setText(teksAman(rs.getString("umur")) + " Th");
                    Jk.setText(teksAman(rs.getString("jk")));
                    TNoRM1.setText(TNoRM.getText());
                    TPasien1.setText(TPasien.getText());
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    // =================================================================
    //  RINGKASAN HTML
    // =================================================================

    private void bangunRingkasanHtml() {
        StringBuilder h = new StringBuilder();
        h.append("<html><table width='100%' cellpadding='2' cellspacing='0'>");

        judulHtml(h, "IDENTITAS");
        barisHtml(h, "No.RM / Nama", TNoRM.getText() + " / " + TPasien.getText());
        barisHtml(h, "Tgl.Lahir / Umur", TglLahir.getText() + " / " + TUmur.getText());
        barisHtml(h, "Tanggal & Jam", tanggalDanJamResume());
        barisHtml(h, "Petugas", NmPetugas.getText());

        judulHtml(h, "ALERGI OBAT");
        boolean adaAlergi = false;
        for (int n = 0; n < BARIS_ALERGI; n++) {
            if (!Alergi[n].getText().trim().isEmpty()) {
                barisHtml(h, String.valueOf(n + 1), Alergi[n].getText());
                adaAlergi = true;
            }
        }
        if (!adaAlergi) {
            barisHtml(h, "-", "<i>tidak ada / belum diisi</i>");
        }

        judulHtml(h, "RIWAYAT KESEHATAN PASIEN");
        boolean adaRiwayat = false;
        for (int n = 0; n < BARIS_RIWAYAT; n++) {
            if (!Riwayat[n].getText().trim().isEmpty()) {
                barisHtml(h, String.valueOf(n + 1),
                        Riwayat[n].getText() + " &mdash; Tahun " + Tahun[n].getText());
                adaRiwayat = true;
            }
        }
        if (!adaRiwayat) {
            barisHtml(h, "-", "<i>belum diisi</i>");
        }

        judulHtml(h, "PERHATIAN KHUSUS");
        StringBuilder khusus = new StringBuilder();
        for (int n = 0; n < PERHATIAN.length; n++) {
            if (CekKhusus[n].isSelected()) {
                khusus.append(khusus.length() > 0 ? ", " : "").append(PERHATIAN[n][0]);
            }
        }
        if (!KhususLainnya.getText().trim().isEmpty()) {
            khusus.append(khusus.length() > 0 ? ", " : "").append(KhususLainnya.getText());
        }
        barisHtml(h, "Penanda", khusus.length() == 0 ? "<i>tidak ada</i>" : khusus.toString());

        // Pratinjau grid yang akan tercetak. Dirakit dari pemeriksaan_ralan,
        // BUKAN dari tabel form ini - lihat catatan kelas.
        judulHtml(h, "RIWAYAT KUNJUNGAN (dari SOAP rawat jalan)");
        tambahBarisKunjungan(h);

        h.append("</table></html>");
        LoadHTML.setText(h.toString());
        LoadHTML.setCaretPosition(0);
    }

    private void tambahBarisKunjungan(StringBuilder h) {
        int jml = 0;
        try {
            ps = koneksi.prepareStatement(sqlKunjungan() + " limit 15");
            try {
                ps.setString(1, TNoRM.getText());
                rs = ps.executeQuery();
                while (rs.next()) {
                    barisHtml(h, teksAman(rs.getString("tgl_perawatan")) + "<br>"
                            + teksAman(rs.getString("jam_rawat")),
                            "<b>" + teksAman(rs.getString("nm_dokter")) + "</b><br>"
                            + teksAman(rs.getString("diagnosa")) + "<br>"
                            + teksAman(rs.getString("uraian_klinis")));
                    jml++;
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
        if (jml == 0) {
            barisHtml(h, "-", "<i>belum ada catatan SOAP rawat jalan untuk pasien ini</i>");
        }
    }

    private String sqlKunjunganUi() {
        return "select date_format(pr.tgl_perawatan,'%d-%m-%Y') as tgl_tampil,"
                + "date_format(pr.tgl_perawatan,'%Y-%m-%d') as tgl_sumber,"
                + "time_format(pr.jam_rawat,'%H:%i:%s') as jam_sumber,pr.no_rawat,pr.nip,"
                + "ifnull(dokter.nm_dokter,'') as nm_dokter,"
                + "ifnull((select group_concat(distinct penyakit.nm_penyakit separator ', ') "
                + "from diagnosa_pasien inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "
                + "where diagnosa_pasien.no_rawat=pr.no_rawat),'') as diagnosa,"
                + "concat_ws('\n',nullif(pr.keluhan,''),nullif(pr.pemeriksaan,''),nullif(pr.penilaian,'')) as uraian_klinis,"
                + "concat_ws('\n',nullif(pr.rtl,''),nullif(pr.instruksi,'')) as penatalaksanaan,"
                + "ifnull(pr.evaluasi,'') as catatan_penting,"
                + "coalesce(nullif(petugas.nama,''),nullif(dokter_pemeriksa.nm_dokter,''),'') as nm_pemeriksa,"
                + "ifnull(s.id,'') as id_snapshot,ifnull(s.hash_dokumen,'') as hash_snapshot "
                + "from pemeriksaan_ralan pr "
                + "inner join reg_periksa on pr.no_rawat=reg_periksa.no_rawat "
                + "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                + "left join petugas on pr.nip=petugas.nip "
                + "left join dokter dokter_pemeriksa on pr.nip=dokter_pemeriksa.kd_dokter "
                + "left join resume_rawat_jalan_kunjungan s on s.id=(select max(s2.id) "
                + "from resume_rawat_jalan_kunjungan s2 where s2.no_rawat=pr.no_rawat "
                + "and s2.tgl_perawatan=pr.tgl_perawatan and s2.jam_rawat=pr.jam_rawat "
                + "and s2.nip_pemeriksa=pr.nip) "
                + "where reg_periksa.no_rkm_medis=? and reg_periksa.status_lanjut='Ralan' "
                + "order by pr.tgl_perawatan desc,pr.jam_rawat desc";
    }

    private void tampilKunjungan() {
        if (tbKunjungan == null) {
            return;
        }
        Valid.tabelKosong(tabModeKunjungan);
        if (TNoRM.getText().trim().isEmpty()) {
            LCountKunjungan.setText("0 kunjungan");
            perbaruiAksiKunjungan();
            return;
        }
        try (PreparedStatement pstm = koneksi.prepareStatement(sqlKunjunganUi())) {
            pstm.setString(1, TNoRM.getText());
            try (ResultSet hasil = pstm.executeQuery()) {
                while (hasil.next()) {
                    String[] data = {
                        TNoRM.getText(), teksAman(hasil.getString("no_rawat")),
                        teksAman(hasil.getString("tgl_sumber")), teksAman(hasil.getString("jam_sumber")),
                        teksAman(hasil.getString("nm_dokter")), teksAman(hasil.getString("diagnosa")),
                        teksAman(hasil.getString("uraian_klinis")), teksAman(hasil.getString("penatalaksanaan")),
                        teksAman(hasil.getString("catatan_penting")), teksAman(hasil.getString("nip")),
                        teksAman(hasil.getString("nm_pemeriksa"))
                    };
                    String hashSekarang = hashKunjungan(data);
                    String idSnapshot = teksAman(hasil.getString("id_snapshot"));
                    String hashSnapshot = teksAman(hasil.getString("hash_snapshot"));
                    String statusParaf = idSnapshot.isEmpty() ? "Belum diparaf"
                            : (hashSekarang.equals(hashSnapshot) ? "Ditandatangani" : "Perlu addendum");
                    String namaDanParaf = "<html><div style='text-align:center'><b>"
                            + htmlAman(statusParaf) + "</b><br>" + htmlAman(data[4])
                            + "</div></html>";
                    tabModeKunjungan.addRow(new Object[]{
                        hasil.getString("tgl_tampil") + " " + hasil.getString("jam_sumber"),
                        data[4], data[5], data[6], data[7], data[8], namaDanParaf,
                        statusParaf, data[1], data[2], data[3], data[9], idSnapshot,
                        hashSnapshot, data[10]
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane,
                    "Data kunjungan belum dapat dimuat. Pastikan migrasi "
                    + "sik_resume_rawat_jalan_rm01_eparaf.sql sudah dijalankan.\n\n" + e.getMessage());
        }
        LCountKunjungan.setText(tabModeKunjungan.getRowCount() + " kunjungan");
        perbaruiAksiKunjungan();
    }

    private void perbaruiAksiKunjungan() {
        if (BtnFinalisasiKunjungan == null) {
            return;
        }
        int baris = tbKunjungan == null ? -1 : tbKunjungan.getSelectedRow();
        boolean terpilih = baris >= 0;
        String statusParaf = terpilih ? teksAman(tabModeKunjungan.getValueAt(baris, 7) + "") : "";
        boolean adaSnapshot = terpilih && !teksAman(tabModeKunjungan.getValueAt(baris, 12) + "").isEmpty();
        BtnFinalisasiKunjungan.setEnabled(terpilih && !"Ditandatangani".equals(statusParaf));
        BtnFinalisasiKunjungan.setText("Perlu addendum".equals(statusParaf)
                ? "Buat Addendum & E-Paraf" : "Finalisasi & E-Paraf");
        BtnCetakLanjutan.setEnabled(adaSnapshot);
    }

    private String[] dataKunjunganTerpilih() {
        int baris = tbKunjungan.getSelectedRow();
        if (baris < 0) {
            return null;
        }
        return new String[]{
            TNoRM.getText(),
            teksAman(tabModeKunjungan.getValueAt(baris, 8) + ""),
            teksAman(tabModeKunjungan.getValueAt(baris, 9) + ""),
            teksAman(tabModeKunjungan.getValueAt(baris, 10) + ""),
            teksAman(tabModeKunjungan.getValueAt(baris, 1) + ""),
            teksAman(tabModeKunjungan.getValueAt(baris, 2) + ""),
            teksAman(tabModeKunjungan.getValueAt(baris, 3) + ""),
            teksAman(tabModeKunjungan.getValueAt(baris, 4) + ""),
            teksAman(tabModeKunjungan.getValueAt(baris, 5) + ""),
            teksAman(tabModeKunjungan.getValueAt(baris, 11) + ""),
            teksAman(tabModeKunjungan.getValueAt(baris, 14) + "")
        };
    }

    private String hashKunjungan(String[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder isi = new StringBuilder("RM01|1");
            for (String nilai : data) {
                isi.append('|').append(teksAman(nilai).replace("\r\n", "\n").trim());
            }
            byte[] hash = digest.digest(isi.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder heksa = new StringBuilder(64);
            for (byte b : hash) {
                heksa.append(String.format("%02x", b & 0xff));
            }
            return heksa.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 tidak tersedia", e);
        }
    }

    private void finalisasiKunjungan() {
        String[] data = dataKunjunganTerpilih();
        if (data == null) {
            JOptionPane.showMessageDialog(rootPane, "Silahkan pilih kunjungan terlebih dahulu.");
            return;
        }
        if ("Admin Utama".equals(akses.getkode())) {
            JOptionPane.showMessageDialog(rootPane,
                    "Admin Utama tidak boleh menandatangani catatan klinis. Silahkan login dengan akun DPJP.");
            return;
        }
        String kodeDpjp = kodeDpjpKunjungan(data[1]);
        if (kodeDpjp.isEmpty()) {
            JOptionPane.showMessageDialog(rootPane,
                    "DPJP untuk kunjungan ini belum tersedia. E-paraf tidak dapat dilakukan.");
            return;
        }
        if (!akses.getkode().equals(kodeDpjp)) {
            JOptionPane.showMessageDialog(rootPane,
                    "DPJP kunjungan ini adalah " + data[4] + " (" + kodeDpjp + ").\n"
                    + "E-paraf hanya dapat dilakukan melalui akun DPJP tersebut.");
            return;
        }
        int baris = tbKunjungan.getSelectedRow();
        boolean addendum = "Perlu addendum".equals(tabModeKunjungan.getValueAt(baris, 7) + "");
        int jawab = JOptionPane.showConfirmDialog(rootPane,
                (addendum ? "Sumber SOAP berubah setelah paraf sebelumnya. Buat addendum baru?"
                        : "Finalisasi catatan kunjungan ini dengan e-paraf internal?")
                + "\n\nSetelah ditandatangani, snapshot tidak dapat diubah.",
                addendum ? "Konfirmasi Addendum" : "Konfirmasi E-Paraf Internal",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (jawab != JOptionPane.YES_OPTION) {
            return;
        }
        if (!konfirmasiPasswordAktif()) {
            return;
        }

        int versi = 1;
        try (PreparedStatement cek = koneksi.prepareStatement(
                "select ifnull(max(versi),0)+1 from resume_rawat_jalan_kunjungan "
                + "where no_rawat=? and tgl_perawatan=? and jam_rawat=? and nip_pemeriksa=?")) {
            cek.setString(1, data[1]);
            cek.setString(2, data[2]);
            cek.setString(3, data[3]);
            cek.setString(4, data[9]);
            try (ResultSet hasil = cek.executeQuery()) {
                if (hasil.next()) versi = hasil.getInt(1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, "Gagal menentukan versi e-paraf: " + e.getMessage());
            return;
        }

        try {
            if (simpanSnapshotKunjungan(data, versi, addendum, kodeDpjp)) {
                tampilKunjungan();
                JOptionPane.showMessageDialog(rootPane,
                        (addendum ? "Addendum" : "Catatan") + " berhasil diberi e-paraf internal.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, "Gagal menyimpan e-paraf internal: " + e.getMessage());
        }
    }

    private String kodeDpjpKunjungan(String noRawat) {
        try (PreparedStatement cek = koneksi.prepareStatement(
                "select kd_dokter from reg_periksa where no_rawat=?")) {
            cek.setString(1, noRawat);
            try (ResultSet hasil = cek.executeQuery()) {
                return hasil.next() ? teksAman(hasil.getString("kd_dokter")) : "";
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
            return "";
        }
    }

    /**
     * Menyimpan salinan tidak-mutable dari catatan kunjungan yang disahkan.
     * Dipisahkan dari dialog agar operasi basis data dapat diuji dalam transaksi
     * yang kemudian di-rollback tanpa melewati pemeriksaan identitas pada UI.
     */
    private boolean simpanSnapshotKunjungan(String[] data, int versi, boolean addendum,
            String penandaTangan) throws Exception {
        String sql = "insert into resume_rawat_jalan_kunjungan("
                + "no_rkm_medis,no_rawat,tgl_perawatan,jam_rawat,kd_dokter_dpjp,nm_dokter_dpjp,"
                + "diagnosa,uraian_klinis,penatalaksanaan,catatan_penting,nip_pemeriksa,nm_pemeriksa,"
                + "versi,jenis_catatan,jenis_paraf,status_paraf,ditandatangani_oleh,waktu_paraf,hash_dokumen) "
                + "select ?,?,?,?,reg_periksa.kd_dokter,?,?,?,?,?,?,?,?,?,'Internal SIMRS',"
                + "'Ditandatangani',?,now(),? from reg_periksa where reg_periksa.no_rawat=?";
        try (PreparedStatement simpan = koneksi.prepareStatement(sql)) {
            int n = 1;
            simpan.setString(n++, data[0]);
            simpan.setString(n++, data[1]);
            simpan.setString(n++, data[2]);
            simpan.setString(n++, data[3]);
            simpan.setString(n++, data[4]);
            simpan.setString(n++, data[5]);
            simpan.setString(n++, data[6]);
            simpan.setString(n++, data[7]);
            simpan.setString(n++, data[8]);
            simpan.setString(n++, data[9]);
            simpan.setString(n++, data[10]);
            simpan.setInt(n++, versi);
            simpan.setString(n++, addendum ? "Addendum" : "Final");
            simpan.setString(n++, penandaTangan);
            simpan.setString(n++, hashKunjungan(data));
            simpan.setString(n++, data[1]);
            return simpan.executeUpdate() == 1;
        }
    }

    private boolean konfirmasiPasswordAktif() {
        JPasswordField sandi = new JPasswordField(18);
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Masukkan password akun " + akses.getkode() + ":"), BorderLayout.NORTH);
        panel.add(sandi, BorderLayout.CENTER);
        int hasil = JOptionPane.showConfirmDialog(rootPane, panel, "Konfirmasi Identitas Penanda Tangan",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (hasil != JOptionPane.OK_OPTION) {
            java.util.Arrays.fill(sandi.getPassword(), '\0');
            return false;
        }
        char[] karakter = sandi.getPassword();
        String password = new String(karakter);
        java.util.Arrays.fill(karakter, '\0');
        boolean valid = false;
        try (PreparedStatement cek = koneksi.prepareStatement(
                "select count(*) from user where id_user=AES_ENCRYPT(?,'nur') "
                + "and password=AES_ENCRYPT(?,'windi')")) {
            cek.setString(1, akses.getkode());
            cek.setString(2, password);
            try (ResultSet hasilCek = cek.executeQuery()) {
                valid = hasilCek.next() && hasilCek.getInt(1) == 1;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, "Gagal memverifikasi akun: " + e.getMessage());
        } finally {
            password = null;
            sandi.setText("");
        }
        if (!valid) {
            JOptionPane.showMessageDialog(rootPane, "Password tidak sesuai. E-paraf dibatalkan.");
        }
        return valid;
    }

    private void cetakLanjutanKunjungan() {
        int baris = tbKunjungan.getSelectedRow();
        if (baris < 0) return;
        String id = teksAman(tabModeKunjungan.getValueAt(baris, 12) + "");
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "Finalisasi dan e-paraf kunjungan sebelum mencetak.");
            return;
        }
        String[] pilihan = {"Lembar", "PDF"};
        Object mode = JOptionPane.showInputDialog(rootPane, "Pilih keluaran lembar lanjutan:", "Cetak RM01",
                JOptionPane.QUESTION_MESSAGE, null, pilihan, pilihan[0]);
        if (mode == null) return;

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        muatParamKepala(param, TNoRM.getText());
        String qry = "select no_rawat,date_format(tgl_perawatan,'%d-%m-%Y') as tgl_perawatan,"
                + "time_format(jam_rawat,'%H:%i:%s') as jam_rawat,nm_dokter_dpjp as nm_dokter,"
                + "diagnosa,uraian_klinis,penatalaksanaan,catatan_penting,nm_dokter_dpjp as nm_petugas,"
                + "concat('E-paraf internal DPJP oleh ',nm_dokter_dpjp,' (',ditandatangani_oleh,') pada ',"
                + "date_format(waktu_paraf,'%d-%m-%Y %H:%i:%s'),' | Versi ',versi,' ',jenis_catatan) as eparaf_internal,"
                + "concat('RM01|',id,'|',hash_dokumen) as qr_eparaf,"
                + "hash_dokumen,versi,jenis_catatan from resume_rawat_jalan_kunjungan where id=" + id;
        if ("PDF".equals(mode)) {
            Valid.MyReportqrypdf("rptResumeRawatJalanRM01Lanjutan.jasper", "report",
                    "::[ Lembar Lanjutan RM 01 ]::", qry, param);
        } else {
            Valid.MyReportqry("rptResumeRawatJalanRM01Lanjutan.jasper", "report",
                    "::[ Lembar Lanjutan RM 01 ]::", qry, param);
        }
        Sequel.queryu2("update resume_rawat_jalan_kunjungan set jumlah_cetak=jumlah_cetak+1,"
                + "cetak_terakhir=now() where id=?", 1, new String[]{id});
    }

    private void judulHtml(StringBuilder h, String judul) {
        h.append("<tr class='isi'><td colspan='2' bgcolor='#FFFAF8'><b>").append(judul).append("</b></td></tr>");
    }

    private void barisHtml(StringBuilder h, String label, String nilai) {
        h.append("<tr class='isi'><td width='22%' valign='top'>").append(label)
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
        if (mode == null) {
            return;
        }
        String noRM = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        // Blok kepala dikirim sebagai PARAMETER, bukan field: nilainya sama
        // untuk seluruh halaman, sementara field ikut berganti tiap baris
        // kunjungan pada band detail.
        muatParamKepala(param, noRM);

        String qry = sqlKunjunganCetak(noRM);
        if ("PDF".equals(mode)) {
            Valid.MyReportqrypdf("rptResumeRawatJalanRM01.jasper", "report",
                    "::[ Resume Rawat Jalan ]::", qry, param);
        } else {
            Valid.MyReportqry("rptResumeRawatJalanRM01.jasper", "report",
                    "::[ Resume Rawat Jalan ]::", qry, param);
        }
    }

    /**
     * Mengisi blok kepala (alergi, riwayat, perhatian khusus) sebagai parameter
     * laporan. Dibaca dari tabel form ini, bukan dari widget, supaya yang
     * tercetak adalah yang TERSIMPAN - bukan ketikan yang belum disimpan.
     */
    private void muatParamKepala(Map<String, Object> param, String noRM) {
        // Identitas pasien juga PARAMETER, bukan field. Kalau diambil dari
        // query, pasien yang sudah punya lembar RM 01 tapi BELUM punya satu pun
        // catatan SOAP akan menghasilkan nol baris - dan seluruh kepala lembar
        // ikut kosong. Sebagai parameter, kepala tetap tercetak utuh dan yang
        // kosong hanya gridnya.
        param.put("no_rkm_medis", noRM);
        param.put("nm_pasien", "");
        param.put("tgl_lahir", "");
        param.put("jk", "");
        param.put("umur", "");
        param.put("tgl_masuk_rs", "");
        try {
            ps = koneksi.prepareStatement(
                    "select pasien.nm_pasien,date_format(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                    + "timestampdiff(year,pasien.tgl_lahir,curdate()) as umur,"
                    + "(select date_format(min(reg_periksa.tgl_registrasi),'%d-%m-%Y') from reg_periksa "
                    + "where reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "and reg_periksa.status_lanjut='Ralan') as tgl_masuk_rs "
                    + "from pasien where pasien.no_rkm_medis=?");
            try {
                ps.setString(1, noRM);
                rs = ps.executeQuery();
                if (rs.next()) {
                    param.put("nm_pasien", teksAman(rs.getString("nm_pasien")));
                    param.put("tgl_lahir", teksAman(rs.getString("tgl_lahir")));
                    param.put("jk", teksAman(rs.getString("jk")));
                    param.put("umur", teksAman(rs.getString("umur")) + " Th");
                    param.put("tgl_masuk_rs", teksAman(rs.getString("tgl_masuk_rs")));
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }

        for (int n = 1; n <= BARIS_ALERGI; n++) {
            param.put("alergi" + n, "");
        }
        for (int n = 1; n <= BARIS_RIWAYAT; n++) {
            param.put("riwayat" + n, "");
            param.put("tahun" + n, "");
        }
        for (String[] p : PERHATIAN) {
            param.put(p[1], "Tidak");
        }
        param.put("khusus_lainnya", "");
        try {
            ps = koneksi.prepareStatement("select * from resume_rawat_jalan where no_rkm_medis=?");
            try {
                ps.setString(1, noRM);
                rs = ps.executeQuery();
                if (rs.next()) {
                    for (int n = 1; n <= BARIS_ALERGI; n++) {
                        param.put("alergi" + n, teksAman(rs.getString("alergi" + n)));
                    }
                    for (int n = 1; n <= BARIS_RIWAYAT; n++) {
                        param.put("riwayat" + n, teksAman(rs.getString("riwayat" + n)));
                        param.put("tahun" + n, teksAman(rs.getString("tahun" + n)));
                    }
                    for (String[] p : PERHATIAN) {
                        param.put(p[1], teksAman(rs.getString(p[1])));
                    }
                    param.put("khusus_lainnya", teksAman(rs.getString("khusus_lainnya")));
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    /**
     * Grid RM 01, dirakit dari {@code pemeriksaan_ralan} - satu baris per
     * catatan SOAP rawat jalan pasien, lintas kunjungan.
     *
     * <p>Hanya rawat jalan: {@code reg_periksa.status_lanjut='Ralan'}.
     * {@code left join} pada diagnosa dan dokter supaya kunjungan yang belum
     * didiagnosa tetap muncul - kalau inner join, baris SOAP-nya hilang diam
     * diam dari lembar.</p>
     */
    private String sqlKunjungan() {
        return "select date_format(pr.tgl_perawatan,'%d-%m-%Y') as tgl_perawatan,pr.jam_rawat,"
                + "coalesce(nullif(s.nm_dokter_dpjp,''),ifnull(dokter.nm_dokter,'')) as nm_dokter,"
                + "coalesce(s.diagnosa,ifnull((select group_concat(distinct penyakit.nm_penyakit separator ', ') "
                + "from diagnosa_pasien inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "
                + "where diagnosa_pasien.no_rawat=pr.no_rawat),'')) as diagnosa,"
                + "coalesce(s.uraian_klinis,concat_ws('\n',nullif(pr.keluhan,''),nullif(pr.pemeriksaan,''),nullif(pr.penilaian,''))) as uraian_klinis,"
                + "coalesce(s.penatalaksanaan,concat_ws('\n',nullif(pr.rtl,''),nullif(pr.instruksi,''))) as penatalaksanaan,"
                + "coalesce(s.catatan_penting,ifnull(pr.evaluasi,'')) as catatan_penting,"
                + "coalesce(nullif(s.nm_dokter_dpjp,''),ifnull(dokter.nm_dokter,'')) as nm_petugas,"
                + "ifnull(concat('RM01|',s.id,'|',s.hash_dokumen),'') as qr_eparaf "
                + "from pemeriksaan_ralan pr "
                + "inner join reg_periksa on pr.no_rawat=reg_periksa.no_rawat "
                + "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                + "left join resume_rawat_jalan_kunjungan s on s.id=(select max(s2.id) "
                + "from resume_rawat_jalan_kunjungan s2 where s2.no_rawat=pr.no_rawat "
                + "and s2.tgl_perawatan=pr.tgl_perawatan and s2.jam_rawat=pr.jam_rawat "
                + "and s2.nip_pemeriksa=pr.nip) "
                + "where reg_periksa.no_rkm_medis=? and reg_periksa.status_lanjut='Ralan' "
                + "order by pr.tgl_perawatan desc,pr.jam_rawat desc";
    }

    /**
     * Query cetak = grid saja. Identitas pasien dan blok kepala dikirim lewat
     * parameter (lihat {@link #muatParamKepala}), sehingga lembar tetap
     * tercetak utuh walau pasien belum punya satu pun catatan SOAP.
     */
    private String sqlKunjunganCetak(String noRM) {
        return sqlKunjungan().replace("where reg_periksa.no_rkm_medis=?",
                "where reg_periksa.no_rkm_medis='" + noRM + "'");
    }

    // =================================================================
    //  API dipakai pemanggil
    // =================================================================

    /** Dipanggil dari DlgRawatJalan / menu utama dengan No.RM pasien. */
    public void setNoRm(String noRM) {
        TNoRM.setText(noRM);
        TCari.setText(noRM);
        muatIdentitas();
        if (adaRecord(noRM)) {
            muatRecord(noRM);
        }
        isiRiwayatDariDiagnosaLampau(false);
        tampilKunjungan();
        bangunRingkasanHtml();
    }

    private boolean adaRecord(String noRM) {
        String ada = Sequel.cariIsi(
                "select no_rkm_medis from resume_rawat_jalan where no_rkm_medis=?", noRM);
        return ada != null && !ada.trim().isEmpty();
    }

    private void muatRecord(String noRM) {
        try {
            ps = koneksi.prepareStatement("select * from resume_rawat_jalan where no_rkm_medis=?");
            try {
                ps.setString(1, noRM);
                rs = ps.executeQuery();
                if (rs.next()) {
                    isiDariRecord();
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    public void setTampil() {
        TabRawat.setSelectedIndex(1);
    }

    /**
     * Dipertahankan demi kompatibilitas pemanggil lama. Petugas RM01 dipilih
     * eksplisit melalui tombol pencarian agar NIP selalu berasal dari master.
     */
    public void isCek() {
        // Tidak ada prefill; pengguna memilih petugas dari DlgCariPetugas.
    }

    // =================================================================
    //  INFRASTRUKTUR
    // =================================================================

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
