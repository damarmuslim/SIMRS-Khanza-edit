package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
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
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnPrint, BtnAll, BtnKeluar;

    // ---------- Header ----------
    private widget.TextBox TNoRM, TPasien, TglLahir, TUmur, Jk;
    private widget.Tanggal TglResume;
    private widget.TextBox KdPetugas, NmPetugas;

    // ---------- Badan ----------
    private final widget.TextBox[] Alergi = new widget.TextBox[BARIS_ALERGI];
    private final widget.TextBox[] Riwayat = new widget.TextBox[BARIS_RIWAYAT];
    private final widget.TextBox[] Tahun = new widget.TextBox[BARIS_RIWAYAT];
    private widget.Button BtnUsulRiwayat;
    private final widget.CekBox[] CekKhusus = new widget.CekBox[PERHATIAN.length];
    private widget.TextBox KhususLainnya;

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
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Resume Rawat Jalan (RM 01) ]::"));
        internalFrame1.setFont(new java.awt.Font(ThemeConfig.FONT_FAMILY, java.awt.Font.ITALIC, 12));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8 = new widget.panelisi();
        panelGlass8.setPreferredSize(new Dimension(44, 54));
        panelGlass8.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

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

        panelGlass8.add(BtnSimpan);
        panelGlass8.add(BtnBatal);
        panelGlass8.add(BtnHapus);
        panelGlass8.add(BtnEdit);
        panelGlass8.add(BtnPrint);
        panelGlass8.add(BtnAll);
        panelGlass8.add(BtnKeluar);
        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        TabRawat = new javax.swing.JTabbedPane();
        TabRawat.setFont(new java.awt.Font(ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));

        internalFrame2 = new widget.InternalFrame();
        internalFrame2.setBorder(null);
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput = new widget.PanelBiasa();
        FormInput.setLayout(new GridBagLayout());

        scrollInput = new widget.ScrollPane();
        scrollInput.setViewportView(FormInput);
        scrollInput.getVerticalScrollBar().setUnitIncrement(16);
        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);
        TabRawat.addTab("Input Resume", internalFrame2);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
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
        b.setPreferredSize(new Dimension(100, 30));
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
        tb.setTitleColor(ThemeConfig.NAVY);
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
        return t;
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
        widget.PanelBiasa panelHeader = panelSeksi("IDENTITAS PASIEN (otomatis dari pendaftaran)");

        TNoRM = kotakBaca(110);
        TPasien = kotakBaca(230);
        TglLahir = kotakBaca(100);
        TUmur = kotakBaca(110);
        Jk = kotakBaca(90);
        TglResume = tanggal("dd-MM-yyyy HH:mm:ss", 150);
        KdPetugas = kotakBaca(110);
        NmPetugas = kotakBaca(210);

        int row = 0;
        panelHeader.add(labelKolom("No.RM"), konstrain(0, row));
        panelHeader.add(baris(TNoRM, teks("   Nama Pasien :"), TPasien), konstrain(1, row));
        row++;

        panelHeader.add(labelKolom("Tgl.Lahir"), konstrain(0, row));
        panelHeader.add(baris(TglLahir, teks("   Umur :"), TUmur,
                teks("   L/P :"), Jk), konstrain(1, row));
        row++;

        panelHeader.add(labelKolom("Tanggal & Jam"), konstrain(0, row));
        panelHeader.add(baris(TglResume,
                teks("   Petugas :"), KdPetugas, NmPetugas), konstrain(1, row));

        tambahPengisiKolom(panelHeader, 2);
        tambahSeksi(panelHeader, 0);
    }

    // =================================================================
    //  BADAN
    // =================================================================

    private void rakitBadan() {
        int seksi = 1;

        // ---------------- ALERGI OBAT ----------------
        widget.PanelBiasa panelAlergi = panelSeksi("ALERGI OBAT");
        for (int n = 0; n < BARIS_ALERGI; n++) {
            Alergi[n] = kotak(560);
            panelAlergi.add(labelKolom(String.valueOf(n + 1)), konstrain(0, n));
            panelAlergi.add(Alergi[n], konstrain(1, n));
        }
        tambahPengisiKolom(panelAlergi, 2);
        tambahSeksi(panelAlergi, seksi++);

        // ---------------- RIWAYAT KESEHATAN PASIEN ----------------
        widget.PanelBiasa panelRiwayat = panelSeksi(
                "RIWAYAT KESEHATAN PASIEN (catatan perawatan, pengobatan, operasi atau tindakan medis sebelumnya)");

        BtnUsulRiwayat = new widget.Button();
        BtnUsulRiwayat.setText("Usulkan dari Diagnosa Lampau");
        BtnUsulRiwayat.setToolTipText("Isikan baris yang masih kosong dari diagnosa kunjungan-kunjungan sebelumnya");
        BtnUsulRiwayat.setPreferredSize(new Dimension(210, 23));
        BtnUsulRiwayat.addActionListener(evt -> usulkanRiwayat());
        GridBagConstraints cb = konstrain(1, 0);
        panelRiwayat.add(BtnUsulRiwayat, cb);

        for (int n = 0; n < BARIS_RIWAYAT; n++) {
            Riwayat[n] = kotak(470);
            Tahun[n] = kotak(60);
            panelRiwayat.add(labelKolom(String.valueOf(n + 1)), konstrain(0, n + 1));
            panelRiwayat.add(baris(Riwayat[n], teks("Tahun"), Tahun[n]), konstrain(1, n + 1));
        }
        tambahPengisiKolom(panelRiwayat, 2);
        tambahSeksi(panelRiwayat, seksi++);

        // ---------------- PERHATIAN KHUSUS ----------------
        widget.PanelBiasa panelKhusus = panelSeksi("PERHATIAN KHUSUS");
        // Dua kolom seperti kertas: LANSIA/DISABILITAS, RESIKO KEKERASAN/
        // RESIKO JATUH, DNR/LAINNYA.
        for (int n = 0; n < PERHATIAN.length; n++) {
            CekKhusus[n] = cek(PERHATIAN[n][0]);
            panelKhusus.add(CekKhusus[n], konstrain(n % 2, n / 2));
        }
        KhususLainnya = kotak(330);
        panelKhusus.add(baris(cekLainnyaLabel(), KhususLainnya), konstrain(1, 2));
        tambahPengisiKolom(panelKhusus, 2);
        tambahSeksi(panelKhusus, seksi++);

        tambahPengisiBaris(FormInput, seksi);
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
        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9 = new widget.panelisi();
        panelGlass9.setPreferredSize(new Dimension(44, 44));
        panelGlass9.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        TCari = kotak(240);
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    runBackground(() -> tampil());
                }
            }
        });
        BtnCari = new widget.Button();
        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setPreferredSize(new Dimension(28, 23));
        BtnCari.addActionListener(evt -> runBackground(() -> tampil()));
        LCount = new widget.Label();
        LCount.setText("0");
        LCount.setHorizontalAlignment(SwingConstants.LEFT);
        LCount.setPreferredSize(new Dimension(70, 23));

        // Tidak ada penyaring tanggal: lembar ini berkunci PASIEN, bukan
        // kunjungan, jadi menyaringnya per tanggal justru menyembunyikan
        // lembar pasien yang resume-nya dibuat lama.
        panelGlass9.add(teks("Cari (No.RM / Nama) :"));
        panelGlass9.add(TCari);
        panelGlass9.add(BtnCari);
        panelGlass9.add(teks("   Jml :"));
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

        TNoRM1 = kotakBaca(90);
        TPasien1 = kotakBaca(180);
        BtnPrint1 = new widget.Button();
        BtnPrint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item (copy).png")));
        BtnPrint1.setPreferredSize(new Dimension(28, 23));
        BtnPrint1.setToolTipText("Cetak resume pasien ini");
        BtnPrint1.addActionListener(evt -> cetakTerpilih());

        FormMenu = new widget.panelisi();
        FormMenu.setPreferredSize(new Dimension(44, 44));
        FormMenu.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        FormMenu.add(teks("Pasien :"));
        FormMenu.add(TNoRM1);
        FormMenu.add(TPasien1);
        FormMenu.add(BtnPrint1);

        ChkAccor = new widget.CekBox();
        ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png")));
        ChkAccor.setPreferredSize(new Dimension(15, 15));
        ChkAccor.addActionListener(evt -> isMenu());

        PanelAccor = new widget.PanelBiasa();
        PanelAccor.setLayout(new java.awt.BorderLayout(1, 1));
        PanelAccor.setPreferredSize(new Dimension(500, 44));
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
            PanelAccor.setPreferredSize(new Dimension(500, 44));
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
        v.add(TglResume.getSelectedItem().toString().substring(11, 19));
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
        if (TNoRM.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "Silahkan pilih pasien terlebih dahulu..!!");
            return;
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
                    + "order by tahun desc,penyakit.nm_penyakit limit " + BARIS_RIWAYAT);
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
            JOptionPane.showMessageDialog(rootPane,
                    "Tidak ada diagnosa terdahulu yang tercatat untuk pasien ini.");
            return;
        }
        int terisi = 0;
        int idxUsul = 0;
        for (int n = 0; n < BARIS_RIWAYAT && idxUsul < usul.size(); n++) {
            if (Riwayat[n].getText().trim().isEmpty()) {
                Riwayat[n].setText(usul.get(idxUsul)[0]);
                Tahun[n].setText(usul.get(idxUsul)[1]);
                idxUsul++;
                terisi++;
            }
        }
        JOptionPane.showMessageDialog(rootPane, terisi == 0
                ? "Semua baris riwayat sudah terisi, tidak ada yang diubah."
                : terisi + " baris riwayat diisikan dari diagnosa terdahulu.\n"
                + "Silahkan diperiksa dan disesuaikan sebelum disimpan.");
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
        bangunRingkasanHtml();
        ChkAccor.setSelected(true);
        isMenu();
    }

    private void isiDariRecord() throws Exception {
        String tgl = teksAman(rs.getString("tanggal"));
        String jam = teksAman(rs.getString("jam"));
        if (!tgl.isEmpty()) {
            Valid.SetTgl2(TglResume, tgl + " " + (jam.isEmpty() ? "00:00:00" : jam));
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
        TglResume.setDate(new Date());
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
        barisHtml(h, "Tanggal & Jam", String.valueOf(TglResume.getSelectedItem()));
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
        String nip = tbObat.getValueAt(tbObat.getSelectedRow(), 6) == null
                ? "" : tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString();
        String nama = tbObat.getValueAt(tbObat.getSelectedRow(), 7) == null
                ? "" : tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString();

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        param.put("finger_petugas", tandaTangan(nip, nama));
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
        try {
            ps = koneksi.prepareStatement(
                    "select pasien.nm_pasien,date_format(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                    + "timestampdiff(year,pasien.tgl_lahir,curdate()) as umur "
                    + "from pasien where pasien.no_rkm_medis=?");
            try {
                ps.setString(1, noRM);
                rs = ps.executeQuery();
                if (rs.next()) {
                    param.put("nm_pasien", teksAman(rs.getString("nm_pasien")));
                    param.put("tgl_lahir", teksAman(rs.getString("tgl_lahir")));
                    param.put("jk", teksAman(rs.getString("jk")));
                    param.put("umur", teksAman(rs.getString("umur")) + " Th");
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

    private String tandaTangan(String nip, String nama) {
        String finger = Sequel.cariIsi(
                "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id "
                + "where pegawai.nik=?", nip);
        return "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                + "\nDitandatangani secara elektronik oleh " + nama
                + "\nID " + (finger == null || finger.isEmpty() ? nip : finger);
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
                + "ifnull(dokter.nm_dokter,'') as nm_dokter,"
                + "ifnull((select group_concat(distinct penyakit.nm_penyakit separator ', ') "
                + "from diagnosa_pasien inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "
                + "where diagnosa_pasien.no_rawat=pr.no_rawat),'') as diagnosa,"
                + "concat_ws('\n',nullif(pr.keluhan,''),nullif(pr.pemeriksaan,''),nullif(pr.penilaian,'')) as uraian_klinis,"
                + "concat_ws('\n',nullif(pr.rtl,''),nullif(pr.instruksi,'')) as penatalaksanaan,"
                + "ifnull(pr.evaluasi,'') as catatan_penting,"
                + "ifnull(petugas.nama,'') as nm_petugas "
                + "from pemeriksaan_ralan pr "
                + "inner join reg_periksa on pr.no_rawat=reg_periksa.no_rawat "
                + "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                + "left join petugas on pr.nip=petugas.nip "
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
     * Tanpa gerbang hak akses, mengikuti RM 21 dan RM 33. Method ini tetap ada
     * karena pemanggil memanggilnya; tugasnya hanya mengisi petugas dengan user
     * yang sedang login.
     */
    public void isCek() {
        KdPetugas.setText(akses.getkode());
        NmPetugas.setText(teksAman(
                Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode())));
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
