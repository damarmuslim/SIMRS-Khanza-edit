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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
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
 * ASESMEN &amp; PERENCANAAN PENDIDIKAN PASIEN (RM 21) - RSUD Demang Sepulau Raya.
 * Diisi oleh Perawat / Bidan, KHUSUS pasien RAWAT INAP.
 *
 * <p><b>Hubungannya dengan form Edukasi rawat jalan.</b> Isi lembar ini sebagian
 * besar bertumpang tindih dengan {@link RMEdukasiPasienKeluargaRawatJalan}
 * (RM 02-1) - delapan dari sembilan baris Section A punya padanan di tabel
 * {@code edukasi_pasien_keluarga_rj}. Tetapi form itu hanya bisa dibuka dari
 * jalur rawat jalan &amp; IGD; tidak ada satu pun pintu masuk dari
 * {@code DlgKamarInap}/{@code DlgRawatInap}, sehingga untuk pasien rawat inap
 * asesmen edukasi tidak tersedia sama sekali. Form ini mengisi kekosongan itu.</p>
 *
 * <p>Keduanya memakai tabel BERBEDA karena PK masing-masing sama-sama
 * {@code no_rawat} saja - kalau berbagi tabel, keduanya akan saling menimpa
 * baris yang sama. Konsekuensi yang disadari: kedua form TIDAK berbagi data.
 * Form rawat jalan tidak disentuh sama sekali.</p>
 *
 * <p><b>Tiga perbedaan struktural dari form rawat jalan</b> yang ditangani di
 * sini: (1) kolom "Sasaran" pada kertas diisi PER BARIS kebutuhan, bukan sekali
 * untuk seluruh lembar; (2) tiap hambatan punya isian "Ya, sebutkan"; (3) ada
 * "Budaya pasien dan keluarga" yang tidak punya padanan di skema lama.</p>
 *
 * <p>Seluruh isian Section B diisi MANUAL oleh perawat - keputusan user. Selain
 * pemetaannya tidak pasti ("Akademi" di kertas vs D1/D2/D3 di
 * {@code pasien.pnd}), asesmen edukasi memang harus mencerminkan yang dinilai
 * perawat langsung terhadap pasien, bukan data pendaftaran yang bisa sudah
 * basi. Yang terisi otomatis hanya identitas pasien, tanggal/jam, dan perawat
 * yang sedang login.</p>
 *
 * <p>Ditulis tangan dengan {@link GridBagLayout} tanpa berkas {@code .form} dan
 * tanpa blok GEN, mengikuti preseden RM 20 dan RM 17-01. Gotcha widget yang
 * dipatuhi: {@code widget.TextBox}/{@code ComboBox}/{@code Tanggal} wajib
 * {@code setPreferredSize()} eksplisit, {@code widget.Label} defaultnya rata
 * KANAN, dan {@code widget.Tanggal} buatan tangan wajib
 * {@code setDisplayFormat(...)}.</p>
 */
public final class RMAsesmenPendidikanPasien extends javax.swing.JDialog {

    /**
     * Nama kolom {@code asesmen_pendidikan_pasien_ranap} di luar
     * {@code no_rawat}, dipakai BERSAMA oleh {@link #simpan()} dan
     * {@link #ganti()} - satu daftar, dua pemakai, supaya urutan kolom dan
     * urutan nilai tidak pernah bisa desinkron.
     *
     * <p>Urutannya WAJIB sama persis dengan {@link #nilaiRm21()}.</p>
     */
    private static final String[] KOLOM_RM21 = {
        "tanggal", "jam",
        "butuh_diagnosa", "sasaran_diagnosa",
        "butuh_obat", "sasaran_obat",
        "butuh_alat_medis", "sasaran_alat_medis",
        "butuh_diet_nutrisi", "sasaran_diet_nutrisi",
        "butuh_rehabilitasi", "sasaran_rehabilitasi",
        "butuh_manajemen_nyeri", "sasaran_manajemen_nyeri",
        "butuh_ppi_cuci_tangan", "sasaran_ppi_cuci_tangan",
        "butuh_kesehatan_berkelanjutan", "sasaran_kesehatan_berkelanjutan",
        "butuh_lain_lain", "ket_lain_lain",
        "keyakinan_nilai", "budaya",
        "kemampuan_membaca",
        "tingkat_pendidikan", "ket_tingkat_pendidikan",
        "bahasa_indonesia", "bahasa_inggris",
        "bahasa_daerah1", "kemampuan_daerah1", "bahasa_daerah2", "kemampuan_daerah2",
        "penerjemah", "ket_penerjemah",
        "hambatan_emosional", "ket_hambatan_emosional",
        "hambatan_motivasi", "ket_hambatan_motivasi",
        "keterbatasan_fisik", "ket_keterbatasan_fisik",
        "keterbatasan_kognitif", "ket_keterbatasan_kognitif",
        "kesediaan_menerima", "nip"
    };

    /**
     * Delapan baris tetap Section A: label kebutuhan dan pemberi edukasinya.
     * Kolom "Pemberian Edukasi" sudah TERCETAK TETAP di formulir fisik, jadi
     * hanya ditampilkan - tidak ada yang disimpan ke database.
     */
    private static final String[][] BARIS_KEBUTUHAN = {
        {"Diagnosa, penyebab, tanda & gejala", "Dokter"},
        {"Penggunaan obat yang efektif dan aman (potensi efek samping & interaksi)", "Farmasi"},
        {"Penggunaan peralatan medis yang aman dan efektif", "Perawat / Bidan"},
        {"Diet dan Nutrisi", "Ahli Gizi"},
        {"Rehabilitasi Medik", "Terapis"},
        {"Manajemen Nyeri", "Perawat / Bidan"},
        {"Pencegahan & pengendalian infeksi (cuci tangan secara efektif)", "Perawat / Bidan"},
        {"Pemenuhan kebutuhan kesehatan berkelanjutan", "Perawat / Bidan"}
    };

    private static final String[] PILIHAN_SASARAN = {"", "Pasien", "Keluarga", "Pasien & Keluarga"};
    private static final String[] PILIHAN_AKTIF_PASIF = {"Aktif", "Pasif"};

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
    private widget.TextBox TNoRw, TNoRM, TPasien, TglLahir, TUmur, Jk;
    private widget.Tanggal TglAsesmen;
    private widget.TextBox KdPetugas, NmPetugas;

    // ---------- A. Asesmen Kebutuhan Pendidikan ----------
    private final widget.CekBox[] CekButuh = new widget.CekBox[BARIS_KEBUTUHAN.length];
    private final widget.ComboBox[] CmbSasaran = new widget.ComboBox[BARIS_KEBUTUHAN.length];
    private widget.CekBox CekLainLain;
    private widget.TextBox KetLainLain;

    // ---------- B. Perencanaan Pemberian Edukasi ----------
    private widget.TextArea KeyakinanNilai, Budaya;
    private final ButtonGroup bgMembaca = new ButtonGroup();
    private widget.ComboBox TingkatPendidikan;
    private widget.TextBox KetTingkatPendidikan;
    private widget.CekBox CekIndonesia, CekInggris;
    private widget.ComboBox CmbIndonesia, CmbInggris, CmbDaerah1, CmbDaerah2;
    private widget.TextBox Daerah1, Daerah2;
    private final ButtonGroup bgPenerjemah = new ButtonGroup();
    private widget.TextBox KetPenerjemah;
    private final ButtonGroup bgEmosional = new ButtonGroup();
    private widget.TextBox KetEmosional;
    private final ButtonGroup bgMotivasi = new ButtonGroup();
    private widget.TextBox KetMotivasi;
    private final ButtonGroup bgFisik = new ButtonGroup();
    private widget.TextBox KetFisik;
    private final ButtonGroup bgKognitif = new ButtonGroup();
    private widget.TextBox KetKognitif;
    private final ButtonGroup bgKesediaan = new ButtonGroup();

    // ---------- Tab Data ----------
    private widget.ScrollPane Scroll, ScrollHTML;
    private widget.Table tbObat;
    private widget.Tanggal DTPCari1, DTPCari2;
    private widget.TextBox TCari, TNoRM1, TPasien1;
    private widget.Button BtnCari, BtnPrint1;
    private widget.Label LCount;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JMenuItem MnCetak;
    private widget.PanelBiasa PanelAccor;
    private widget.panelisi FormMenu;
    private widget.CekBox ChkAccor;
    private widget.editorpane LoadHTML;

    public RMAsesmenPendidikanPasien(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.RM", "Nama Pasien", "Tgl.Lahir", "J.K.",
            "Tanggal", "Jam", "NIP", "Perawat / Bidan"
        }) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
        };

        rakitKerangka();
        rakitHeader();
        rakitBadan();
        rakitTabData();

        widget.IconStyle.pasang(this);
        bersihkanIkonRadio(getContentPane());
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
                "::[ Asesmen & Perencanaan Pendidikan Pasien (RM 21) ]::"));
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
        TabRawat.addTab("Input Asesmen", internalFrame2);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
    }

    /**
     * Mengembalikan ikon bawaan pada semua radio button.
     *
     * <p>{@code widget.IconStyle.pasang(this)} memasang ikon SVG ke SEMUA
     * {@code AbstractButton} berdasarkan kata kunci pada teksnya, dan
     * {@code JRadioButton} ikut kena. {@code JCheckBox} sengaja tidak ikut
     * dibersihkan: {@code ChkAccor} memang memerlukan ikon panahnya.</p>
     */
    private void bersihkanIkonRadio(java.awt.Container wadah) {
        for (Component c : wadah.getComponents()) {
            if (c instanceof javax.swing.JRadioButton) {
                ((javax.swing.JRadioButton) c).setIcon(null);
            } else if (c instanceof java.awt.Container) {
                bersihkanIkonRadio((java.awt.Container) c);
            }
        }
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

    /** Menyerap sisa lebar di kolom paling kanan supaya grid rata KIRI. */
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
     *
     * <p>Tanpa ini {@link GridBagLayout} men-TENGAH-kan seluruh grid secara
     * vertikal saat container lebih tinggi daripada isinya - bug yang baru
     * ketahuan di RM 17-01 setelah form-nya benar-benar dirender.</p>
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

    private widget.Label teksLebar(String isi, int lebar) {
        widget.Label l = teks(isi);
        l.setPreferredSize(new Dimension(lebar, 20));
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

    private widget.ComboBox combo(String[] isi, int lebar) {
        widget.ComboBox c = new widget.ComboBox();
        c.setModel(new javax.swing.DefaultComboBoxModel<>(isi));
        c.setPreferredSize(new Dimension(lebar, 23));
        return c;
    }

    private widget.RadioButton radio(String teks, ButtonGroup grup) {
        widget.RadioButton r = new widget.RadioButton();
        r.setText(teks);
        grup.add(r);
        return r;
    }

    /** Sepasang radio "Tidak"/"Ya" - teksnya SENGAJA sama dengan nilai ENUM. */
    private JPanel pilihanTidakYa(ButtonGroup grup) {
        return baris(radio("Tidak", grup), radio("Ya", grup));
    }

    private widget.CekBox cek(String teks) {
        widget.CekBox c = new widget.CekBox();
        c.setText(teks);
        return c;
    }

    private widget.ScrollPane areaTeks(widget.TextArea area, int baris, int lebar, int tinggi) {
        area.setColumns(20);
        area.setRows(baris);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        widget.ScrollPane sp = new widget.ScrollPane();
        sp.setViewportView(area);
        sp.setBorder(javax.swing.BorderFactory.createLineBorder(ThemeConfig.BORDER));
        sp.setPreferredSize(new Dimension(lebar, tinggi));
        return sp;
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

        TNoRw = kotakBaca(140);
        TNoRM = kotakBaca(90);
        TPasien = kotakBaca(210);
        TglLahir = kotakBaca(90);
        TUmur = kotakBaca(110);
        Jk = kotakBaca(80);
        TglAsesmen = tanggal("dd-MM-yyyy HH:mm:ss", 150);
        KdPetugas = kotakBaca(80);
        NmPetugas = kotakBaca(210);

        int row = 0;
        panelHeader.add(labelKolom("No.Rawat"), konstrain(0, row));
        panelHeader.add(baris(TNoRw, teks("   No.RM :"), TNoRM,
                teks("   Nama Pasien :"), TPasien), konstrain(1, row));
        row++;

        panelHeader.add(labelKolom("Tgl.Lahir"), konstrain(0, row));
        panelHeader.add(baris(TglLahir, teks("   Umur :"), TUmur,
                teks("   L/P :"), Jk), konstrain(1, row));
        row++;

        panelHeader.add(labelKolom("Tanggal & Jam"), konstrain(0, row));
        panelHeader.add(baris(TglAsesmen,
                teks("   Perawat / Bidan :"), KdPetugas, NmPetugas), konstrain(1, row));

        tambahPengisiKolom(panelHeader, 2);
        tambahSeksi(panelHeader, 0);
    }

    // =================================================================
    //  BADAN
    // =================================================================

    private void rakitBadan() {
        int seksi = 1;

        // ---------------- A. ASESMEN KEBUTUHAN PENDIDIKAN ----------------
        widget.PanelBiasa panelA = panelSeksi("A. ASESMEN KEBUTUHAN PENDIDIKAN");

        // Baris judul kolom, meniru kepala tabel pada formulir fisik.
        panelA.add(teksLebar("Kebutuhan Pendidikan", 400), konstrain(1, 0));
        panelA.add(teksLebar("Pemberian Edukasi", 120), konstrain(2, 0));
        panelA.add(teksLebar("Sasaran ( P / K / P & K )", 170), konstrain(3, 0));

        for (int n = 0; n < BARIS_KEBUTUHAN.length; n++) {
            CekButuh[n] = cek("");
            CmbSasaran[n] = combo(PILIHAN_SASARAN, 170);
            panelA.add(CekButuh[n], konstrain(0, n + 1));
            panelA.add(teksLebar(BARIS_KEBUTUHAN[n][0], 400), konstrain(1, n + 1));
            // Pemberi edukasi hanya DITAMPILKAN - di kertas sudah tercetak tetap,
            // jadi tidak ada yang perlu disimpan ke database.
            panelA.add(teksLebar(BARIS_KEBUTUHAN[n][1], 120), konstrain(2, n + 1));
            panelA.add(CmbSasaran[n], konstrain(3, n + 1));
        }

        int rowLain = BARIS_KEBUTUHAN.length + 1;
        CekLainLain = cek("");
        KetLainLain = kotak(400);
        panelA.add(CekLainLain, konstrain(0, rowLain));
        panelA.add(baris(teks("Lain-lain :"), KetLainLain), konstrain(1, rowLain));
        tambahPengisiKolom(panelA, 4);
        tambahSeksi(panelA, seksi++);

        // ---------------- B. PERENCANAAN PEMBERIAN EDUKASI ----------------
        widget.PanelBiasa panelB = panelSeksi("B. PERENCANAAN PEMBERIAN EDUKASI");
        KeyakinanNilai = new widget.TextArea();
        Budaya = new widget.TextArea();
        TingkatPendidikan = combo(new String[]{"", "Tidak Sekolah", "SD", "SMP", "SMA",
            "Akademi", "Sarjana", "Lain-lain"}, 150);
        KetTingkatPendidikan = kotak(200);
        CekIndonesia = cek("Indonesia");
        CmbIndonesia = combo(PILIHAN_AKTIF_PASIF, 90);
        CekInggris = cek("Inggris");
        CmbInggris = combo(PILIHAN_AKTIF_PASIF, 90);
        Daerah1 = kotak(160);
        CmbDaerah1 = combo(PILIHAN_AKTIF_PASIF, 90);
        Daerah2 = kotak(160);
        CmbDaerah2 = combo(PILIHAN_AKTIF_PASIF, 90);
        KetPenerjemah = kotak(280);
        KetEmosional = kotak(280);
        KetMotivasi = kotak(280);
        KetFisik = kotak(280);
        KetKognitif = kotak(280);

        int r = 0;
        panelB.add(labelKolom("1. Keyakinan dan nilai-nilai"), konstrain(0, r));
        GridBagConstraints c = konstrain(1, r);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panelB.add(areaTeks(KeyakinanNilai, 2, 430, 42), c);
        r++;

        panelB.add(labelKolom("2. Budaya pasien dan keluarga"), konstrain(0, r));
        c = konstrain(1, r);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panelB.add(areaTeks(Budaya, 2, 430, 42), c);
        r++;

        panelB.add(labelKolom("3a. Kemampuan membaca"), konstrain(0, r));
        panelB.add(pilihanTidakYa(bgMembaca), konstrain(1, r));
        r++;

        panelB.add(labelKolom("3b. Tingkatan pendidikan"), konstrain(0, r));
        panelB.add(baris(TingkatPendidikan,
                teks("   Lain-lain, sebutkan :"), KetTingkatPendidikan), konstrain(1, r));
        r++;

        panelB.add(labelKolom("3c. Bahasa sehari-hari"), konstrain(0, r));
        panelB.add(baris(CekIndonesia, CmbIndonesia,
                teks("     "), CekInggris, CmbInggris), konstrain(1, r));
        r++;
        panelB.add(labelKolom("     Daerah, sebutkan"), konstrain(0, r));
        panelB.add(baris(Daerah1, CmbDaerah1,
                teks("     dan :"), Daerah2, CmbDaerah2), konstrain(1, r));
        r++;

        panelB.add(labelKolom("3d. Penerjemah Bahasa"), konstrain(0, r));
        panelB.add(baris(pilihanTidakYa(bgPenerjemah),
                teks("   Ya, sebutkan :"), KetPenerjemah), konstrain(1, r));
        r++;

        panelB.add(labelKolom("4. Hambatan Emosional"), konstrain(0, r));
        panelB.add(baris(pilihanTidakYa(bgEmosional),
                teks("   Ya, sebutkan :"), KetEmosional), konstrain(1, r));
        r++;

        panelB.add(labelKolom("5. Hambatan Motivasi"), konstrain(0, r));
        panelB.add(baris(pilihanTidakYa(bgMotivasi),
                teks("   Ya, sebutkan :"), KetMotivasi), konstrain(1, r));
        r++;

        panelB.add(labelKolom("6a. Keterbatasan fisik"), konstrain(0, r));
        panelB.add(baris(pilihanTidakYa(bgFisik),
                teks("   Ya, sebutkan :"), KetFisik), konstrain(1, r));
        r++;

        panelB.add(labelKolom("6b. Keterbatasan kognitif"), konstrain(0, r));
        panelB.add(baris(pilihanTidakYa(bgKognitif),
                teks("   Ya, sebutkan :"), KetKognitif), konstrain(1, r));
        r++;

        panelB.add(labelKolom("7. Kesediaan pasien menerima informasi dan edukasi"), konstrain(0, r));
        panelB.add(pilihanTidakYa(bgKesediaan), konstrain(1, r));
        tambahSeksi(panelB, seksi++);

        tambahPengisiBaris(FormInput, seksi);
    }

    // =================================================================
    //  TAB DATA
    // =================================================================

    private void rakitTabData() {
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetak = new javax.swing.JMenuItem();
        MnCetak.setFont(new java.awt.Font(ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        MnCetak.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png")));
        MnCetak.setText("Cetak Asesmen & Perencanaan Pendidikan Pasien");
        MnCetak.setPreferredSize(new Dimension(300, 26));
        MnCetak.addActionListener(evt -> cetakTerpilih());
        jPopupMenu1.add(MnCetak);

        internalFrame3 = new widget.InternalFrame();
        internalFrame3.setBorder(null);
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        tbObat = new widget.Table();
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        int[] lebar = {105, 70, 170, 80, 60, 90, 70, 90, 170};
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
        DTPCari1 = tanggal("dd-MM-yyyy", 95);
        DTPCari2 = tanggal("dd-MM-yyyy", 95);
        TCari = kotak(200);
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

        panelGlass9.add(teks("Tanggal :"));
        panelGlass9.add(DTPCari1);
        panelGlass9.add(teks("s.d"));
        panelGlass9.add(DTPCari2);
        panelGlass9.add(teks("   Key Word :"));
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
        BtnPrint1.setToolTipText("Cetak asesmen pasien ini");
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
        PanelAccor.setPreferredSize(new Dimension(470, 44));
        PanelAccor.add(ChkAccor, java.awt.BorderLayout.WEST);
        PanelAccor.add(FormMenu, java.awt.BorderLayout.NORTH);
        PanelAccor.add(ScrollHTML, java.awt.BorderLayout.CENTER);
        internalFrame3.add(PanelAccor, java.awt.BorderLayout.EAST);

        TabRawat.addTab("Data Asesmen", internalFrame3);
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

    private String[] nilaiRm21() {
        String tgl = Valid.SetTgl(TglAsesmen.getSelectedItem() + "");
        String jam = TglAsesmen.getSelectedItem().toString().substring(11, 19);
        return new String[]{
            tgl, jam,
            ya(CekButuh[0]), sasaran(0),
            ya(CekButuh[1]), sasaran(1),
            ya(CekButuh[2]), sasaran(2),
            ya(CekButuh[3]), sasaran(3),
            ya(CekButuh[4]), sasaran(4),
            ya(CekButuh[5]), sasaran(5),
            ya(CekButuh[6]), sasaran(6),
            ya(CekButuh[7]), sasaran(7),
            ya(CekLainLain), KetLainLain.getText(),
            KeyakinanNilai.getText(), Budaya.getText(),
            pilihan(bgMembaca),
            kosongKeNull(TingkatPendidikan), KetTingkatPendidikan.getText(),
            CekIndonesia.isSelected() ? CmbIndonesia.getSelectedItem().toString() : null,
            CekInggris.isSelected() ? CmbInggris.getSelectedItem().toString() : null,
            kosongKeNull(Daerah1.getText()), bahasaDaerah(Daerah1, CmbDaerah1),
            kosongKeNull(Daerah2.getText()), bahasaDaerah(Daerah2, CmbDaerah2),
            pilihan(bgPenerjemah), KetPenerjemah.getText(),
            pilihan(bgEmosional), KetEmosional.getText(),
            pilihan(bgMotivasi), KetMotivasi.getText(),
            pilihan(bgFisik), KetFisik.getText(),
            pilihan(bgKognitif), KetKognitif.getText(),
            pilihan(bgKesediaan), KdPetugas.getText()
        };
    }

    /** Nilai ENUM('Ya','Tidak') dari sebuah checkbox. */
    private String ya(AbstractButton b) {
        return b.isSelected() ? "Ya" : "Tidak";
    }

    /**
     * Sasaran hanya bermakna kalau kebutuhannya dicentang.
     *
     * <p>Tanpa gerbang ini, sasaran yang tertinggal dari pengisian sebelumnya
     * ikut tersimpan untuk baris yang sebenarnya tidak dibutuhkan.</p>
     */
    private String sasaran(int idx) {
        return CekButuh[idx].isSelected() ? kosongKeNull(CmbSasaran[idx]) : null;
    }

    /** Kemampuan bahasa daerah hanya bermakna kalau nama bahasanya diisi. */
    private String bahasaDaerah(widget.TextBox nama, widget.ComboBox kemampuan) {
        return nama.getText().trim().isEmpty() ? null : kemampuan.getSelectedItem().toString();
    }

    /**
     * Item combo kosong disimpan sebagai NULL.
     *
     * <p>Kolom-kolom ini bertipe ENUM tanpa nilai string kosong; menyimpan ""
     * ke ENUM ditolak MySQL atau (pada mode non-strict) diam-diam berubah jadi
     * nilai kosong yang tidak sah.</p>
     */
    private String kosongKeNull(widget.ComboBox combo) {
        Object v = combo.getSelectedItem();
        return kosongKeNull(v == null ? null : v.toString());
    }

    private String kosongKeNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
    }

    /** Teks radio terpilih. Teksnya SENGAJA sama persis dengan nilai ENUM. */
    private String pilihan(ButtonGroup grup) {
        java.util.Enumeration<AbstractButton> e = grup.getElements();
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
        if (nilai == null) {
            return;
        }
        java.util.Enumeration<AbstractButton> e = grup.getElements();
        while (e.hasMoreElements()) {
            AbstractButton b = e.nextElement();
            if (b.getText().equals(nilai)) {
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
        if (KdPetugas.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "Perawat / Bidan belum terisi..!!");
            return;
        }
        StringBuilder kol = new StringBuilder("no_rawat");
        StringBuilder tanya = new StringBuilder("?");
        for (String k : KOLOM_RM21) {
            kol.append(",").append(k);
            tanya.append(",?");
        }
        try (PreparedStatement pstm = koneksi.prepareStatement(
                "insert into asesmen_pendidikan_pasien_ranap(" + kol + ") values(" + tanya + ")")) {
            pstm.setString(1, TNoRw.getText());
            String[] nilai = nilaiRm21();
            for (int n = 0; n < nilai.length; n++) {
                pstm.setString(n + 2, nilai[n]);
            }
            pstm.executeUpdate();
            runBackground(() -> tampil());
            TabRawat.setSelectedIndex(1);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan : " + e.getMessage());
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
        for (String k : KOLOM_RM21) {
            if (set.length() > 0) {
                set.append(",");
            }
            set.append(k).append("=?");
        }
        String[] nilai = nilaiRm21();
        String[] param = new String[nilai.length + 1];
        System.arraycopy(nilai, 0, param, 0, nilai.length);
        param[nilai.length] = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();

        if (Sequel.mengedittf("asesmen_pendidikan_pasien_ranap", "no_rawat=?", set.toString(),
                param.length, param)) {
            runBackground(() -> tampil());
            TabRawat.setSelectedIndex(1);
        }
    }

    private void aksiHapus() {
        if (tbObat.getSelectedRow() > -1) {
            if (Sequel.queryu2tf("delete from asesmen_pendidikan_pasien_ranap where no_rawat=?", 1,
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
                "select a.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,a.tanggal,a.jam,a.nip,"
                + "petugas.nama as nm_petugas "
                + "from asesmen_pendidikan_pasien_ranap a "
                + "inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join petugas on a.nip=petugas.nip "
                + "where a.tanggal between ? and ? ";
        if (pakaiCari) {
            sql += "and (a.no_rawat like ? or pasien.no_rkm_medis like ? "
                    + "or pasien.nm_pasien like ? or petugas.nama like ?) ";
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
        TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
        muatIdentitas();
        try {
            ps = koneksi.prepareStatement(
                    "select * from asesmen_pendidikan_pasien_ranap where no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    isiDariRecord();
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
        bangunRingkasanHtml();
        ChkAccor.setSelected(true);
        isMenu();
    }

    private void isiDariRecord() throws Exception {
        String tgl = teksAman(rs.getString("tanggal"));
        String jam = teksAman(rs.getString("jam"));
        if (!tgl.isEmpty()) {
            Valid.SetTgl2(TglAsesmen, tgl + " " + (jam.isEmpty() ? "00:00:00" : jam));
        }

        String[] kolomButuh = {"butuh_diagnosa", "butuh_obat", "butuh_alat_medis", "butuh_diet_nutrisi",
            "butuh_rehabilitasi", "butuh_manajemen_nyeri", "butuh_ppi_cuci_tangan",
            "butuh_kesehatan_berkelanjutan"};
        String[] kolomSasaran = {"sasaran_diagnosa", "sasaran_obat", "sasaran_alat_medis",
            "sasaran_diet_nutrisi", "sasaran_rehabilitasi", "sasaran_manajemen_nyeri",
            "sasaran_ppi_cuci_tangan", "sasaran_kesehatan_berkelanjutan"};
        for (int n = 0; n < kolomButuh.length; n++) {
            CekButuh[n].setSelected("Ya".equals(rs.getString(kolomButuh[n])));
            pilihCombo(CmbSasaran[n], rs.getString(kolomSasaran[n]));
        }
        CekLainLain.setSelected("Ya".equals(rs.getString("butuh_lain_lain")));
        KetLainLain.setText(teksAman(rs.getString("ket_lain_lain")));

        KeyakinanNilai.setText(teksAman(rs.getString("keyakinan_nilai")));
        Budaya.setText(teksAman(rs.getString("budaya")));
        pilihRadio(bgMembaca, rs.getString("kemampuan_membaca"));
        pilihCombo(TingkatPendidikan, rs.getString("tingkat_pendidikan"));
        KetTingkatPendidikan.setText(teksAman(rs.getString("ket_tingkat_pendidikan")));

        String bIndo = rs.getString("bahasa_indonesia");
        CekIndonesia.setSelected(bIndo != null && !bIndo.isEmpty());
        if (CekIndonesia.isSelected()) {
            CmbIndonesia.setSelectedItem(bIndo);
        }
        String bIng = rs.getString("bahasa_inggris");
        CekInggris.setSelected(bIng != null && !bIng.isEmpty());
        if (CekInggris.isSelected()) {
            CmbInggris.setSelectedItem(bIng);
        }
        Daerah1.setText(teksAman(rs.getString("bahasa_daerah1")));
        pilihCombo(CmbDaerah1, rs.getString("kemampuan_daerah1"));
        Daerah2.setText(teksAman(rs.getString("bahasa_daerah2")));
        pilihCombo(CmbDaerah2, rs.getString("kemampuan_daerah2"));

        pilihRadio(bgPenerjemah, rs.getString("penerjemah"));
        KetPenerjemah.setText(teksAman(rs.getString("ket_penerjemah")));
        pilihRadio(bgEmosional, rs.getString("hambatan_emosional"));
        KetEmosional.setText(teksAman(rs.getString("ket_hambatan_emosional")));
        pilihRadio(bgMotivasi, rs.getString("hambatan_motivasi"));
        KetMotivasi.setText(teksAman(rs.getString("ket_hambatan_motivasi")));
        pilihRadio(bgFisik, rs.getString("keterbatasan_fisik"));
        KetFisik.setText(teksAman(rs.getString("ket_keterbatasan_fisik")));
        pilihRadio(bgKognitif, rs.getString("keterbatasan_kognitif"));
        KetKognitif.setText(teksAman(rs.getString("ket_keterbatasan_kognitif")));
        pilihRadio(bgKesediaan, rs.getString("kesediaan_menerima"));

        String nip = teksAman(rs.getString("nip"));
        KdPetugas.setText(nip);
        NmPetugas.setText(nip.isEmpty() ? ""
                : teksAman(Sequel.cariIsi("select nama from petugas where nip=?", nip)));
    }

    private String teksAman(String s) {
        return s == null ? "" : s;
    }

    private void pilihCombo(widget.ComboBox combo, String nilai) {
        if (nilai == null || nilai.isEmpty()) {
            combo.setSelectedIndex(0);
        } else {
            combo.setSelectedItem(nilai);
        }
    }

    public void emptTeks() {
        for (int n = 0; n < CekButuh.length; n++) {
            CekButuh[n].setSelected(false);
            CmbSasaran[n].setSelectedIndex(0);
        }
        CekLainLain.setSelected(false);
        KetLainLain.setText("");
        KeyakinanNilai.setText("");
        Budaya.setText("");
        bgMembaca.clearSelection();
        TingkatPendidikan.setSelectedIndex(0);
        KetTingkatPendidikan.setText("");
        CekIndonesia.setSelected(false);
        CmbIndonesia.setSelectedIndex(0);
        CekInggris.setSelected(false);
        CmbInggris.setSelectedIndex(0);
        Daerah1.setText("");
        CmbDaerah1.setSelectedIndex(0);
        Daerah2.setText("");
        CmbDaerah2.setSelectedIndex(0);
        bgPenerjemah.clearSelection();
        KetPenerjemah.setText("");
        bgEmosional.clearSelection();
        KetEmosional.setText("");
        bgMotivasi.clearSelection();
        KetMotivasi.setText("");
        bgFisik.clearSelection();
        KetFisik.setText("");
        bgKognitif.clearSelection();
        KetKognitif.setText("");
        bgKesediaan.clearSelection();
        TglAsesmen.setDate(new Date());
        TabRawat.setSelectedIndex(0);
    }

    /**
     * Identitas pasien dari master. Tidak pernah disimpan di tabel form ini -
     * selalu dibaca ulang supaya lembar mengikuti data pendaftaran.
     */
    private void muatIdentitas() {
        try {
            ps = koneksi.prepareStatement(
                    "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                    + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur "
                    + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRM.setText(teksAman(rs.getString("no_rkm_medis")));
                    TPasien.setText(teksAman(rs.getString("nm_pasien")));
                    TglLahir.setText(teksAman(rs.getString("tgl_lahir")));
                    TUmur.setText(teksAman(rs.getString("umur")));
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
        barisHtml(h, "No.Rawat", TNoRw.getText());
        barisHtml(h, "No.RM / Nama", TNoRM.getText() + " / " + TPasien.getText());
        barisHtml(h, "Tgl.Lahir / Umur", TglLahir.getText() + " / " + TUmur.getText());
        barisHtml(h, "Tanggal & Jam", String.valueOf(TglAsesmen.getSelectedItem()));
        barisHtml(h, "Perawat / Bidan", NmPetugas.getText());

        judulHtml(h, "A. ASESMEN KEBUTUHAN PENDIDIKAN");
        for (int n = 0; n < BARIS_KEBUTUHAN.length; n++) {
            if (CekButuh[n].isSelected()) {
                barisHtml(h, BARIS_KEBUTUHAN[n][0],
                        BARIS_KEBUTUHAN[n][1] + " &rarr; " + CmbSasaran[n].getSelectedItem());
            }
        }
        if (CekLainLain.isSelected()) {
            barisHtml(h, "Lain-lain", KetLainLain.getText());
        }

        judulHtml(h, "B. PERENCANAAN PEMBERIAN EDUKASI");
        barisHtml(h, "Keyakinan dan nilai-nilai", KeyakinanNilai.getText());
        barisHtml(h, "Budaya pasien dan keluarga", Budaya.getText());
        barisHtml(h, "Kemampuan membaca", teksAman(pilihan(bgMembaca)));
        barisHtml(h, "Tingkatan pendidikan", String.valueOf(TingkatPendidikan.getSelectedItem())
                + (KetTingkatPendidikan.getText().trim().isEmpty() ? ""
                        : " (" + KetTingkatPendidikan.getText() + ")"));
        StringBuilder bahasa = new StringBuilder();
        if (CekIndonesia.isSelected()) {
            bahasa.append("Indonesia ").append(CmbIndonesia.getSelectedItem());
        }
        if (CekInggris.isSelected()) {
            bahasa.append(bahasa.length() > 0 ? ", " : "").append("Inggris ").append(CmbInggris.getSelectedItem());
        }
        if (!Daerah1.getText().trim().isEmpty()) {
            bahasa.append(bahasa.length() > 0 ? ", " : "").append(Daerah1.getText())
                  .append(" ").append(CmbDaerah1.getSelectedItem());
        }
        if (!Daerah2.getText().trim().isEmpty()) {
            bahasa.append(bahasa.length() > 0 ? ", " : "").append(Daerah2.getText())
                  .append(" ").append(CmbDaerah2.getSelectedItem());
        }
        barisHtml(h, "Bahasa sehari-hari", bahasa.toString());
        barisHtml(h, "Penerjemah Bahasa", gabung(pilihan(bgPenerjemah), KetPenerjemah.getText()));
        barisHtml(h, "Hambatan Emosional", gabung(pilihan(bgEmosional), KetEmosional.getText()));
        barisHtml(h, "Hambatan Motivasi", gabung(pilihan(bgMotivasi), KetMotivasi.getText()));
        barisHtml(h, "Keterbatasan fisik", gabung(pilihan(bgFisik), KetFisik.getText()));
        barisHtml(h, "Keterbatasan kognitif", gabung(pilihan(bgKognitif), KetKognitif.getText()));
        barisHtml(h, "Kesediaan menerima informasi", teksAman(pilihan(bgKesediaan)));
        h.append("</table></html>");
        LoadHTML.setText(h.toString());
        LoadHTML.setCaretPosition(0);
    }

    private String gabung(String pilihan, String keterangan) {
        String p = teksAman(pilihan);
        return keterangan.trim().isEmpty() ? p : p + " - " + keterangan;
    }

    private void judulHtml(StringBuilder h, String judul) {
        h.append("<tr class='isi'><td colspan='2' bgcolor='#FFFAF8'><b>").append(judul).append("</b></td></tr>");
    }

    private void barisHtml(StringBuilder h, String label, String nilai) {
        h.append("<tr class='isi'><td width='40%' valign='top'>").append(label)
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
        String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        String nip = tbObat.getValueAt(tbObat.getSelectedRow(), 7) == null
                ? "" : tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString();
        String nama = tbObat.getValueAt(tbObat.getSelectedRow(), 8) == null
                ? "" : tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString();

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        param.put("finger_petugas", tandaTangan(nip, nama));

        String qry = sqlCetak(noRawat);
        if ("PDF".equals(mode)) {
            Valid.MyReportqrypdf("rptAsesmenPendidikanPasienRM21.jasper", "report",
                    "::[ Asesmen & Perencanaan Pendidikan Pasien ]::", qry, param);
        } else {
            Valid.MyReportqry("rptAsesmenPendidikanPasienRM21.jasper", "report",
                    "::[ Asesmen & Perencanaan Pendidikan Pasien ]::", qry, param);
        }
    }

    /** Teks atestasi yang di-encode jadi QR pada blok tanda tangan cetakan. */
    private String tandaTangan(String nip, String nama) {
        String finger = Sequel.cariIsi(
                "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id "
                + "where pegawai.nik=?", nip);
        return "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                + "\nDitandatangani secara elektronik oleh " + nama
                + "\nID " + (finger == null || finger.isEmpty() ? nip : finger);
    }

    private String sqlCetak(String noRawat) {
        return "select a.*,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,"
                + "date_format(a.tanggal,'%d-%m-%Y') as tgl_asesmen,"
                + "ifnull(petugas.nama,'') as nm_petugas "
                + "from asesmen_pendidikan_pasien_ranap a "
                + "inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join petugas on a.nip=petugas.nip "
                + "where a.no_rawat='" + noRawat + "'";
    }

    // =================================================================
    //  API dipakai pemanggil
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
    }

    private boolean adaRecord(String noRawat) {
        String ada = Sequel.cariIsi(
                "select no_rawat from asesmen_pendidikan_pasien_ranap where no_rawat=?", noRawat);
        return ada != null && !ada.trim().isEmpty();
    }

    private void muatRecord(String noRawat) {
        try {
            ps = koneksi.prepareStatement("select * from asesmen_pendidikan_pasien_ranap where no_rawat=?");
            try {
                ps.setString(1, noRawat);
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
     * Form ini SENGAJA tidak punya pembatasan hak akses - keputusan eksplisit
     * user: siapa pun boleh mengisinya, jadi tidak ada kolom hak akses di tabel
     * {@code user}, tidak ada pengecekan {@code akses.getXxx()}, dan tombol
     * Simpan/Hapus/Ganti tidak pernah dinonaktifkan. Berbeda dari RM 20 dan
     * RM 17-01 yang keduanya memakai hak akses.
     *
     * <p>Method ini tetap ada karena pemanggil (menu klik-kanan Rawat Inap dan
     * menu utama) memanggilnya; tugasnya sekarang hanya mengisi Perawat/Bidan
     * dengan user yang sedang login - lembar ini memang atestasi orang yang
     * mengisinya.</p>
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
