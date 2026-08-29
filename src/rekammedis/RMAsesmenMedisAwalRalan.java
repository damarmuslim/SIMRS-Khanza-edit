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
 * ASESMEN MEDIS AWAL RAWAT JALAN (RM 02-1) - RSUD Demang Sepulau Raya.
 * Satu lembar per KUNJUNGAN rawat jalan.
 *
 * <p><b>Kenapa tabel dan form BARU.</b> Tiga tabel yang sudah ada
 * dipertimbangkan dan ditolak - keputusan user, dan alasannya tepat:</p>
 * <ul>
 *   <li>{@code asesmen_pendidikan_pasien_ranap} (RM 21) isinya SAMA PERSIS
 *       untuk blok edukasi, tapi PK-nya {@code no_rawat} saja sehingga pasien
 *       yang punya RM 21 dan RM 02-1 akan saling menimpa.</li>
 *   <li>{@code edukasi_pasien_keluarga_rj} memang tabel edukasi rawat jalan,
 *       tapi bentuknya berbeda jauh (66 kolom, penamaan lain) dan tidak punya
 *       "tingkatan pendidikan".</li>
 *   <li>{@code penilaian_medis_ralan} bagian klinisnya cocok, tapi dipakai form
 *       "Pengkajian Awal Medis Rawat Jalan" bawaan Khanza beserta 11 tabel
 *       turunan per spesialisasi - menambah 40 kolom ke sana mengganggu modul
 *       yang sudah jalan.</li>
 * </ul>
 *
 * <p>Konsekuensi yang disadari: RM 02-1 tidak berbagi data dengan ketiganya,
 * dan ketiga form itu tidak disentuh sama sekali.</p>
 *
 * <p><b>Tingkat risiko jatuh tidak disimpan</b> - ia turunan murni dari dua
 * jawaban Get Up and Go ("Tidak beresiko bila tidak ditemukan a dan b").
 * Menyimpannya berarti menyimpan sesuatu yang bisa desinkron dari dasarnya.</p>
 *
 * <p>Ditulis tangan dengan {@link GridBagLayout} tanpa berkas {@code .form} dan
 * tanpa blok GEN, mengikuti preseden RM 20/17-01/21/33/01. Tanpa gerbang hak
 * akses, mengikuti RM 21/33/01.</p>
 */
public final class RMAsesmenMedisAwalRalan extends javax.swing.JDialog {

    /**
     * Nama kolom {@code asesmen_medis_awal_ralan} di luar {@code no_rawat}.
     * Urutannya WAJIB sama persis dengan {@link #nilaiRm02()}.
     */
    private static final String[] KOLOM_RM02 = {
        "tanggal", "jam",
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
        "kesediaan_menerima",
        "klinik", "ket_klinik",
        "rpd_ada", "rpd_dm", "rpd_jantung", "rpd_hipertensi", "rpd_lain1", "rpd_lain2",
        "alergi_ada", "alergi1", "alergi2", "alergi3", "alergi4",
        "rpk_ada", "rpk_dm", "rpk_jantung", "rpk_hipertensi", "rpk_lain1", "rpk_lain2",
        "jatuh_cara_berjalan", "jatuh_menopang",
        "keluhan_utama", "rps",
        "keadaan_umum", "nadi", "nafas", "suhu", "tensi_sistolik", "tensi_diastolik",
        "bb", "tinggi", "status_gizi",
        "diagnosis1", "diagnosis2", "rencana_terapi",
        "kd_dokter"
    };

    private static final String[] PILIHAN_PENDIDIKAN = {"", "Tidak Sekolah", "SD", "SMP",
        "SMA", "Akademi", "Sarjana", "Lain-lain"};
    private static final String[] PILIHAN_AKTIF_PASIF = {"Aktif", "Pasif"};
    private static final String[] PILIHAN_KLINIK = {"", "Penyakit Dalam", "Bedah",
        "Kebidanan & Kandungan", "Anak", "Penyakit Syaraf", "Penyakit Paru", "Mata",
        "Kulit & Kelamin", "THT", "Gigi", "Rehabilitasi Medik", "Lain-lain"};
    private static final String[] PILIHAN_GIZI = {"", "Buruk", "Kurang", "Baik", "Lebih"};

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
    private widget.TextBox KdDokter, NmDokter;

    // ---------- Edukasi ----------
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

    // ---------- Klinik & riwayat ----------
    private widget.ComboBox Klinik;
    private widget.TextBox KetKlinik;
    private final ButtonGroup bgRpd = new ButtonGroup();
    private widget.CekBox RpdDm, RpdJantung, RpdHipertensi;
    private widget.TextBox RpdLain1, RpdLain2;
    private final ButtonGroup bgAlergi = new ButtonGroup();
    private widget.TextBox Alergi1, Alergi2, Alergi3, Alergi4;
    private final ButtonGroup bgRpk = new ButtonGroup();
    private widget.CekBox RpkDm, RpkJantung, RpkHipertensi;
    private widget.TextBox RpkLain1, RpkLain2;

    // ---------- Risiko jatuh ----------
    private final ButtonGroup bgJatuh1 = new ButtonGroup();
    private final ButtonGroup bgJatuh2 = new ButtonGroup();
    private widget.Label LabelRisiko;

    // ---------- Anamnesis & fisik ----------
    private widget.TextArea KeluhanUtama, Rps, RencanaTerapi;
    private widget.TextBox KeadaanUmum, Nadi, Nafas, Suhu, Sistolik, Diastolik, Bb, Tinggi;
    private widget.ComboBox StatusGizi;
    private widget.TextBox Diagnosis1, Diagnosis2;

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

    public RMAsesmenMedisAwalRalan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.RM", "Nama Pasien", "Tgl.Lahir", "J.K.",
            "Tanggal", "Jam", "Klinik", "Kd.Dokter", "Dokter"
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
                "::[ Asesmen Medis Awal Rawat Jalan (RM 02-1) ]::"));
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
     * Mengembalikan ikon bawaan pada semua radio button - {@code IconStyle}
     * memasang ikon SVG ke SEMUA AbstractButton berdasarkan kata kunci teksnya
     * dan JRadioButton ikut kena.
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
            // Ikon raster lama disembunyikan widget.Button kalau tombol punya teks.
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

    /** Tanpa ini GridBagLayout men-TENGAH-kan grid secara vertikal (bug RM 17-01). */
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

    /** widget.Label defaultnya rata KANAN. */
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
        // WAJIB: constructor diam-diam setSize(WIDTH,23) dengan WIDTH=1.
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

    /** Sepasang radio - teksnya SENGAJA sama persis dengan nilai ENUM. */
    private JPanel pilihanDua(ButtonGroup grup, String a, String b) {
        return baris(radio(a, grup), radio(b, grup));
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
        // Tanpa setDisplayFormat, substring(11,19) meledak.
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
        widget.PanelBiasa p = panelSeksi("IDENTITAS PASIEN (otomatis dari pendaftaran)");

        TNoRw = kotakBaca(140);
        TNoRM = kotakBaca(90);
        TPasien = kotakBaca(210);
        TglLahir = kotakBaca(90);
        TUmur = kotakBaca(110);
        Jk = kotakBaca(80);
        TglAsesmen = tanggal("dd-MM-yyyy HH:mm:ss", 150);
        KdDokter = kotakBaca(110);
        NmDokter = kotakBaca(210);

        int r = 0;
        p.add(labelKolom("No.Rawat"), konstrain(0, r));
        p.add(baris(TNoRw, teks("   No.RM :"), TNoRM, teks("   Nama Pasien :"), TPasien), konstrain(1, r));
        r++;
        p.add(labelKolom("Tgl.Lahir"), konstrain(0, r));
        p.add(baris(TglLahir, teks("   Umur :"), TUmur, teks("   L/P :"), Jk), konstrain(1, r));
        r++;
        p.add(labelKolom("Tanggal & Jam"), konstrain(0, r));
        p.add(baris(TglAsesmen, teks("   DPJP :"), KdDokter, NmDokter), konstrain(1, r));

        tambahPengisiKolom(p, 2);
        tambahSeksi(p, 0);
    }

    // =================================================================
    //  BADAN
    // =================================================================

    private void rakitBadan() {
        int seksi = 1;

        // ---------- PERENCANAAN PEMBERIAN EDUKASI ----------
        widget.PanelBiasa pe = panelSeksi("PERENCANAAN PEMBERIAN EDUKASI");
        KeyakinanNilai = new widget.TextArea();
        Budaya = new widget.TextArea();
        TingkatPendidikan = combo(PILIHAN_PENDIDIKAN, 150);
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
        pe.add(labelKolom("1. Keyakinan dan nilai-nilai"), konstrain(0, r));
        GridBagConstraints c = konstrain(1, r);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        pe.add(areaTeks(KeyakinanNilai, 2, 430, 42), c);
        r++;

        pe.add(labelKolom("2. Budaya pasien dan keluarga"), konstrain(0, r));
        c = konstrain(1, r);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        pe.add(areaTeks(Budaya, 2, 430, 42), c);
        r++;

        pe.add(labelKolom("3a. Kemampuan membaca"), konstrain(0, r));
        pe.add(pilihanDua(bgMembaca, "Tidak", "Ya"), konstrain(1, r));
        r++;
        pe.add(labelKolom("3b. Tingkatan pendidikan"), konstrain(0, r));
        pe.add(baris(TingkatPendidikan, teks("   Lain-lain :"), KetTingkatPendidikan), konstrain(1, r));
        r++;
        pe.add(labelKolom("3c. Bahasa sehari-hari"), konstrain(0, r));
        pe.add(baris(CekIndonesia, CmbIndonesia, teks("     "), CekInggris, CmbInggris), konstrain(1, r));
        r++;
        pe.add(labelKolom("     Daerah, sebutkan"), konstrain(0, r));
        pe.add(baris(Daerah1, CmbDaerah1, teks("   dan :"), Daerah2, CmbDaerah2), konstrain(1, r));
        r++;
        pe.add(labelKolom("3d. Penerjemah Bahasa"), konstrain(0, r));
        pe.add(baris(pilihanDua(bgPenerjemah, "Tidak", "Ya"), teks("   Ya, sebutkan :"), KetPenerjemah), konstrain(1, r));
        r++;
        pe.add(labelKolom("4. Hambatan Emosional"), konstrain(0, r));
        pe.add(baris(pilihanDua(bgEmosional, "Tidak", "Ya"), teks("   Ya, sebutkan :"), KetEmosional), konstrain(1, r));
        r++;
        pe.add(labelKolom("5. Hambatan Motivasi"), konstrain(0, r));
        pe.add(baris(pilihanDua(bgMotivasi, "Tidak", "Ya"), teks("   Ya, sebutkan :"), KetMotivasi), konstrain(1, r));
        r++;
        pe.add(labelKolom("6a. Keterbatasan Fisik"), konstrain(0, r));
        pe.add(baris(pilihanDua(bgFisik, "Tidak", "Ya"), teks("   Ya, sebutkan :"), KetFisik), konstrain(1, r));
        r++;
        pe.add(labelKolom("6b. Keterbatasan Kognitif"), konstrain(0, r));
        pe.add(baris(pilihanDua(bgKognitif, "Tidak", "Ya"), teks("   Ya, sebutkan :"), KetKognitif), konstrain(1, r));
        r++;
        pe.add(labelKolom("7. Kesediaan menerima informasi"), konstrain(0, r));
        pe.add(pilihanDua(bgKesediaan, "Tidak", "Ya"), konstrain(1, r));
        tambahPengisiKolom(pe, 2);
        tambahSeksi(pe, seksi++);

        // ---------- KLINIK ----------
        widget.PanelBiasa pk = panelSeksi("KLINIK");
        Klinik = combo(PILIHAN_KLINIK, 200);
        KetKlinik = kotak(280);
        pk.add(labelKolom("Klinik"), konstrain(0, 0));
        pk.add(baris(Klinik, teks("   Lain-lain, sebutkan :"), KetKlinik), konstrain(1, 0));
        tambahPengisiKolom(pk, 2);
        tambahSeksi(pk, seksi++);

        // ---------- RIWAYAT ----------
        widget.PanelBiasa pr = panelSeksi("RIWAYAT PENYAKIT DAHULU / ALERGI / SAKIT PADA KELUARGA");
        RpdDm = cek("Diabetes Melitus");
        RpdJantung = cek("Penyakit Jantung");
        RpdHipertensi = cek("Hipertensi");
        RpdLain1 = kotak(220);
        RpdLain2 = kotak(220);
        Alergi1 = kotak(220);
        Alergi2 = kotak(220);
        Alergi3 = kotak(220);
        Alergi4 = kotak(220);
        RpkDm = cek("Diabetes Melitus");
        RpkJantung = cek("Penyakit Jantung");
        RpkHipertensi = cek("Hipertensi");
        RpkLain1 = kotak(220);
        RpkLain2 = kotak(220);

        int q = 0;
        pr.add(labelKolom("Riwayat Penyakit Dahulu"), konstrain(0, q));
        pr.add(pilihanDua(bgRpd, "Tidak ada", "Ada"), konstrain(1, q));
        q++;
        pr.add(teks(""), konstrain(0, q));
        pr.add(baris(RpdDm, RpdJantung, RpdHipertensi), konstrain(1, q));
        q++;
        pr.add(teks(""), konstrain(0, q));
        pr.add(baris(RpdLain1, RpdLain2), konstrain(1, q));
        q++;

        pr.add(labelKolom("Riwayat Alergi"), konstrain(0, q));
        pr.add(pilihanDua(bgAlergi, "Tidak ada", "Ada"), konstrain(1, q));
        q++;
        pr.add(teks(""), konstrain(0, q));
        pr.add(baris(Alergi1, Alergi2), konstrain(1, q));
        q++;
        pr.add(teks(""), konstrain(0, q));
        pr.add(baris(Alergi3, Alergi4), konstrain(1, q));
        q++;

        pr.add(labelKolom("Riwayat Sakit pada Keluarga"), konstrain(0, q));
        pr.add(pilihanDua(bgRpk, "Tidak ada", "Ada"), konstrain(1, q));
        q++;
        pr.add(teks(""), konstrain(0, q));
        pr.add(baris(RpkDm, RpkJantung, RpkHipertensi), konstrain(1, q));
        q++;
        pr.add(teks(""), konstrain(0, q));
        pr.add(baris(RpkLain1, RpkLain2), konstrain(1, q));
        tambahPengisiKolom(pr, 2);
        tambahSeksi(pr, seksi++);

        // ---------- RISIKO JATUH ----------
        widget.PanelBiasa pj = panelSeksi("PENGKAJIAN RISIKO PASIEN JATUH (Skala Jatuh Get Up And Go Test)");
        LabelRisiko = new widget.Label();
        LabelRisiko.setHorizontalAlignment(SwingConstants.LEFT);
        LabelRisiko.setText("Tingkat risiko : -");
        pj.add(teks("<html>1. Cara berjalan pasien (salah satu atau lebih):<br>"
                + "&nbsp;&nbsp;&nbsp;a. Tidak seimbang / sempoyongan / limbung<br>"
                + "&nbsp;&nbsp;&nbsp;b. Jalan dengan alat bantu (kruk, tripot, kursi roda)</html>"), konstrain(0, 0));
        pj.add(pilihanJatuh(bgJatuh1), konstrain(1, 0));
        pj.add(teks("<html>2. Menopang saat akan duduk. Pasien tampak memegang<br>"
                + "&nbsp;&nbsp;&nbsp;pinggiran kursi/meja sebagai penopang</html>"), konstrain(0, 1));
        pj.add(pilihanJatuh(bgJatuh2), konstrain(1, 1));
        pj.add(teks("Keterangan : tidak beresiko bila keduanya Tidak; beresiko bila salah satu Ya"), konstrain(0, 2));
        pj.add(LabelRisiko, konstrain(1, 2));
        tambahPengisiKolom(pj, 2);
        tambahSeksi(pj, seksi++);

        // ---------- ANAMNESIS & PEMERIKSAAN FISIK ----------
        widget.PanelBiasa pa = panelSeksi("ANAMNESIS & PEMERIKSAAN FISIK");
        KeluhanUtama = new widget.TextArea();
        Rps = new widget.TextArea();
        KeadaanUmum = kotak(300);
        Nadi = kotak(60);
        Nafas = kotak(60);
        Suhu = kotak(60);
        Sistolik = kotak(50);
        Diastolik = kotak(50);
        Bb = kotak(60);
        Tinggi = kotak(60);
        StatusGizi = combo(PILIHAN_GIZI, 110);

        int a = 0;
        pa.add(labelKolom("Keluhan Utama"), konstrain(0, a));
        c = konstrain(1, a);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        pa.add(areaTeks(KeluhanUtama, 2, 430, 42), c);
        a++;
        pa.add(labelKolom("Riwayat Penyakit Sekarang"), konstrain(0, a));
        c = konstrain(1, a);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        pa.add(areaTeks(Rps, 2, 430, 42), c);
        a++;
        pa.add(labelKolom("Keadaan Umum"), konstrain(0, a));
        pa.add(KeadaanUmum, konstrain(1, a));
        a++;
        pa.add(labelKolom("Nadi / Nafas"), konstrain(0, a));
        pa.add(baris(Nadi, teks("/menit    Nafas :"), Nafas, teks("/menit")), konstrain(1, a));
        a++;
        pa.add(labelKolom("Suhu / Tensi"), konstrain(0, a));
        pa.add(baris(Suhu, teks("°C    Tensi :"), Sistolik, teks("/"), Diastolik, teks("mmHg")), konstrain(1, a));
        a++;
        pa.add(labelKolom("BB / Tinggi"), konstrain(0, a));
        pa.add(baris(Bb, teks("kg    Tinggi :"), Tinggi, teks("cm    Status Gizi :"), StatusGizi), konstrain(1, a));
        tambahPengisiKolom(pa, 2);
        tambahSeksi(pa, seksi++);

        // ---------- DIAGNOSIS & RENCANA ----------
        widget.PanelBiasa pd = panelSeksi("DIAGNOSIS & RENCANA TERAPI");
        Diagnosis1 = kotak(500);
        Diagnosis2 = kotak(500);
        RencanaTerapi = new widget.TextArea();
        pd.add(labelKolom("Diagnosis 1"), konstrain(0, 0));
        pd.add(Diagnosis1, konstrain(1, 0));
        pd.add(labelKolom("Diagnosis 2"), konstrain(0, 1));
        pd.add(Diagnosis2, konstrain(1, 1));
        pd.add(labelKolom("Rencana & Terapi"), konstrain(0, 2));
        c = konstrain(1, 2);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        pd.add(areaTeks(RencanaTerapi, 3, 500, 58), c);
        tambahPengisiKolom(pd, 2);
        tambahSeksi(pd, seksi++);

        tambahPengisiBaris(FormInput, seksi);
    }

    /** Radio Ya/Tidak untuk kriteria jatuh, sekaligus memperbarui label risiko. */
    private JPanel pilihanJatuh(ButtonGroup grup) {
        widget.RadioButton ya = radio("Ya", grup);
        widget.RadioButton tidak = radio("Tidak", grup);
        ya.addActionListener(evt -> hitungRisikoJatuh());
        tidak.addActionListener(evt -> hitungRisikoJatuh());
        return baris(ya, tidak);
    }

    /**
     * Tingkat risiko DIHITUNG, tidak disimpan - ia turunan murni dari kedua
     * jawaban. Menyimpannya berarti menyimpan sesuatu yang bisa desinkron.
     */
    private void hitungRisikoJatuh() {
        String a = pilihan(bgJatuh1);
        String b = pilihan(bgJatuh2);
        if (a == null && b == null) {
            LabelRisiko.setText("Tingkat risiko : -");
        } else if ("Ya".equals(a) || "Ya".equals(b)) {
            LabelRisiko.setText("Tingkat risiko : BERESIKO");
        } else {
            LabelRisiko.setText("Tingkat risiko : Tidak beresiko");
        }
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
        MnCetak.setText("Cetak Asesmen Medis Awal Rawat Jalan");
        MnCetak.addActionListener(evt -> cetakTerpilih());
        jPopupMenu1.add(MnCetak);

        tbObat = new widget.Table();
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbObat.setComponentPopupMenu(jPopupMenu1);
        int[] lebar = {105, 70, 170, 80, 60, 85, 65, 130, 90, 160};
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
        styleSheet.addRule(".isi td{border-right:1px solid #e2e7dd;font:8.5px tahoma;height:12px;"
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

    private String[] nilaiRm02() {
        List<String> v = new ArrayList<>();
        v.add(Valid.SetTgl(TglAsesmen.getSelectedItem() + ""));
        v.add(TglAsesmen.getSelectedItem().toString().substring(11, 19));
        v.add(kosongKeNull(KeyakinanNilai.getText()));
        v.add(kosongKeNull(Budaya.getText()));
        v.add(pilihan(bgMembaca));
        v.add(kosongKeNull(TingkatPendidikan));
        v.add(kosongKeNull(KetTingkatPendidikan.getText()));
        // Bahasa hanya bermakna kalau dicentang - tanpa gerbang ini, pilihan
        // yang tertinggal dari pengisian sebelumnya ikut tersimpan.
        v.add(CekIndonesia.isSelected() ? CmbIndonesia.getSelectedItem().toString() : null);
        v.add(CekInggris.isSelected() ? CmbInggris.getSelectedItem().toString() : null);
        v.add(kosongKeNull(Daerah1.getText()));
        v.add(bahasaDaerah(Daerah1, CmbDaerah1));
        v.add(kosongKeNull(Daerah2.getText()));
        v.add(bahasaDaerah(Daerah2, CmbDaerah2));
        v.add(pilihan(bgPenerjemah));
        v.add(kosongKeNull(KetPenerjemah.getText()));
        v.add(pilihan(bgEmosional));
        v.add(kosongKeNull(KetEmosional.getText()));
        v.add(pilihan(bgMotivasi));
        v.add(kosongKeNull(KetMotivasi.getText()));
        v.add(pilihan(bgFisik));
        v.add(kosongKeNull(KetFisik.getText()));
        v.add(pilihan(bgKognitif));
        v.add(kosongKeNull(KetKognitif.getText()));
        v.add(pilihan(bgKesediaan));
        v.add(kosongKeNull(Klinik));
        v.add(kosongKeNull(KetKlinik.getText()));
        v.add(pilihan(bgRpd));
        v.add(ya(RpdDm));
        v.add(ya(RpdJantung));
        v.add(ya(RpdHipertensi));
        v.add(kosongKeNull(RpdLain1.getText()));
        v.add(kosongKeNull(RpdLain2.getText()));
        v.add(pilihan(bgAlergi));
        v.add(kosongKeNull(Alergi1.getText()));
        v.add(kosongKeNull(Alergi2.getText()));
        v.add(kosongKeNull(Alergi3.getText()));
        v.add(kosongKeNull(Alergi4.getText()));
        v.add(pilihan(bgRpk));
        v.add(ya(RpkDm));
        v.add(ya(RpkJantung));
        v.add(ya(RpkHipertensi));
        v.add(kosongKeNull(RpkLain1.getText()));
        v.add(kosongKeNull(RpkLain2.getText()));
        v.add(pilihan(bgJatuh1));
        v.add(pilihan(bgJatuh2));
        v.add(kosongKeNull(KeluhanUtama.getText()));
        v.add(kosongKeNull(Rps.getText()));
        v.add(kosongKeNull(KeadaanUmum.getText()));
        v.add(kosongKeNull(Nadi.getText()));
        v.add(kosongKeNull(Nafas.getText()));
        v.add(kosongKeNull(Suhu.getText()));
        v.add(kosongKeNull(Sistolik.getText()));
        v.add(kosongKeNull(Diastolik.getText()));
        v.add(kosongKeNull(Bb.getText()));
        v.add(kosongKeNull(Tinggi.getText()));
        v.add(kosongKeNull(StatusGizi));
        v.add(kosongKeNull(Diagnosis1.getText()));
        v.add(kosongKeNull(Diagnosis2.getText()));
        v.add(kosongKeNull(RencanaTerapi.getText()));
        v.add(KdDokter.getText());
        return v.toArray(new String[0]);
    }

    private String ya(AbstractButton b) {
        return b.isSelected() ? "Ya" : "Tidak";
    }

    /** Kemampuan bahasa daerah hanya bermakna kalau nama bahasanya diisi. */
    private String bahasaDaerah(widget.TextBox nama, widget.ComboBox kemampuan) {
        return nama.getText().trim().isEmpty() ? null : kemampuan.getSelectedItem().toString();
    }

    /**
     * Item combo kosong disimpan sebagai NULL. Kolom-kolom ini bertipe ENUM
     * tanpa nilai string kosong; menyimpan "" ke ENUM ditolak MySQL atau (pada
     * mode non-strict) diam-diam berubah jadi nilai kosong yang tidak sah.
     */
    private String kosongKeNull(widget.ComboBox combo) {
        Object v = combo.getSelectedItem();
        return kosongKeNull(v == null ? null : v.toString());
    }

    private String kosongKeNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    /** Teks radio terpilih - SENGAJA sama persis dengan nilai ENUM. */
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
        StringBuilder kol = new StringBuilder("no_rawat");
        StringBuilder tanya = new StringBuilder("?");
        for (String k : KOLOM_RM02) {
            kol.append(",").append(k);
            tanya.append(",?");
        }
        try (PreparedStatement pstm = koneksi.prepareStatement(
                "insert into asesmen_medis_awal_ralan(" + kol + ") values(" + tanya + ")")) {
            pstm.setString(1, TNoRw.getText());
            String[] nilai = nilaiRm02();
            for (int n = 0; n < nilai.length; n++) {
                pstm.setString(n + 2, nilai[n]);
            }
            pstm.executeUpdate();
            runBackground(() -> tampil());
            TabRawat.setSelectedIndex(1);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan : " + e.getMessage()
                    + "\n\nKalau pesannya soal duplikat, kunjungan ini sudah punya lembar RM 02-1."
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
        for (String k : KOLOM_RM02) {
            if (set.length() > 0) {
                set.append(",");
            }
            set.append(k).append("=?");
        }
        String[] nilai = nilaiRm02();
        String[] param = new String[nilai.length + 1];
        System.arraycopy(nilai, 0, param, 0, nilai.length);
        param[nilai.length] = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();

        if (Sequel.mengedittf("asesmen_medis_awal_ralan", "no_rawat=?", set.toString(),
                param.length, param)) {
            runBackground(() -> tampil());
            TabRawat.setSelectedIndex(1);
        }
    }

    private void aksiHapus() {
        if (tbObat.getSelectedRow() > -1) {
            if (Sequel.queryu2tf("delete from asesmen_medis_awal_ralan where no_rawat=?", 1,
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
                + "ifnull(a.klinik,'') as klinik,a.kd_dokter,ifnull(dokter.nm_dokter,'') as nm_dokter "
                + "from asesmen_medis_awal_ralan a "
                + "inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join dokter on a.kd_dokter=dokter.kd_dokter "
                + "where a.tanggal between ? and ? ";
        if (pakaiCari) {
            sql += "and (a.no_rawat like ? or pasien.no_rkm_medis like ? "
                    + "or pasien.nm_pasien like ? or dokter.nm_dokter like ?) ";
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
                        rs.getString("jam"), rs.getString("klinik"), rs.getString("kd_dokter"),
                        rs.getString("nm_dokter")
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
        }
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

        pilihCombo(Klinik, rs.getString("klinik"));
        KetKlinik.setText(teksAman(rs.getString("ket_klinik")));

        pilihRadio(bgRpd, rs.getString("rpd_ada"));
        RpdDm.setSelected("Ya".equals(rs.getString("rpd_dm")));
        RpdJantung.setSelected("Ya".equals(rs.getString("rpd_jantung")));
        RpdHipertensi.setSelected("Ya".equals(rs.getString("rpd_hipertensi")));
        RpdLain1.setText(teksAman(rs.getString("rpd_lain1")));
        RpdLain2.setText(teksAman(rs.getString("rpd_lain2")));

        pilihRadio(bgAlergi, rs.getString("alergi_ada"));
        Alergi1.setText(teksAman(rs.getString("alergi1")));
        Alergi2.setText(teksAman(rs.getString("alergi2")));
        Alergi3.setText(teksAman(rs.getString("alergi3")));
        Alergi4.setText(teksAman(rs.getString("alergi4")));

        pilihRadio(bgRpk, rs.getString("rpk_ada"));
        RpkDm.setSelected("Ya".equals(rs.getString("rpk_dm")));
        RpkJantung.setSelected("Ya".equals(rs.getString("rpk_jantung")));
        RpkHipertensi.setSelected("Ya".equals(rs.getString("rpk_hipertensi")));
        RpkLain1.setText(teksAman(rs.getString("rpk_lain1")));
        RpkLain2.setText(teksAman(rs.getString("rpk_lain2")));

        pilihRadio(bgJatuh1, rs.getString("jatuh_cara_berjalan"));
        pilihRadio(bgJatuh2, rs.getString("jatuh_menopang"));
        hitungRisikoJatuh();

        KeluhanUtama.setText(teksAman(rs.getString("keluhan_utama")));
        Rps.setText(teksAman(rs.getString("rps")));
        KeadaanUmum.setText(teksAman(rs.getString("keadaan_umum")));
        Nadi.setText(teksAman(rs.getString("nadi")));
        Nafas.setText(teksAman(rs.getString("nafas")));
        Suhu.setText(teksAman(rs.getString("suhu")));
        Sistolik.setText(teksAman(rs.getString("tensi_sistolik")));
        Diastolik.setText(teksAman(rs.getString("tensi_diastolik")));
        Bb.setText(teksAman(rs.getString("bb")));
        Tinggi.setText(teksAman(rs.getString("tinggi")));
        pilihCombo(StatusGizi, rs.getString("status_gizi"));

        Diagnosis1.setText(teksAman(rs.getString("diagnosis1")));
        Diagnosis2.setText(teksAman(rs.getString("diagnosis2")));
        RencanaTerapi.setText(teksAman(rs.getString("rencana_terapi")));

        String kd = teksAman(rs.getString("kd_dokter"));
        KdDokter.setText(kd);
        NmDokter.setText(kd.isEmpty() ? ""
                : teksAman(Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", kd)));
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
        Klinik.setSelectedIndex(0);
        KetKlinik.setText("");
        bgRpd.clearSelection();
        RpdDm.setSelected(false);
        RpdJantung.setSelected(false);
        RpdHipertensi.setSelected(false);
        RpdLain1.setText("");
        RpdLain2.setText("");
        bgAlergi.clearSelection();
        Alergi1.setText("");
        Alergi2.setText("");
        Alergi3.setText("");
        Alergi4.setText("");
        bgRpk.clearSelection();
        RpkDm.setSelected(false);
        RpkJantung.setSelected(false);
        RpkHipertensi.setSelected(false);
        RpkLain1.setText("");
        RpkLain2.setText("");
        bgJatuh1.clearSelection();
        bgJatuh2.clearSelection();
        hitungRisikoJatuh();
        KeluhanUtama.setText("");
        Rps.setText("");
        KeadaanUmum.setText("");
        Nadi.setText("");
        Nafas.setText("");
        Suhu.setText("");
        Sistolik.setText("");
        Diastolik.setText("");
        Bb.setText("");
        Tinggi.setText("");
        StatusGizi.setSelectedIndex(0);
        Diagnosis1.setText("");
        Diagnosis2.setText("");
        RencanaTerapi.setText("");
        TglAsesmen.setDate(new Date());
        TabRawat.setSelectedIndex(0);
    }

    /**
     * Identitas pasien dan DPJP dari master. Tidak disimpan di tabel form ini -
     * selalu dibaca ulang supaya lembar mengikuti data pendaftaran.
     */
    private void muatIdentitas() {
        try {
            ps = koneksi.prepareStatement(
                    "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                    + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,"
                    + "reg_periksa.kd_dokter,ifnull(dokter.nm_dokter,'') as nm_dokter,"
                    + "ifnull(poliklinik.nm_poli,'') as nm_poli "
                    + "from reg_periksa "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                    + "left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "
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
                    // DPJP diusulkan dari dokter pendaftaran; tetap bisa diubah.
                    if (KdDokter.getText().trim().isEmpty()) {
                        KdDokter.setText(teksAman(rs.getString("kd_dokter")));
                        NmDokter.setText(teksAman(rs.getString("nm_dokter")));
                    }
                    usulKlinik(teksAman(rs.getString("nm_poli")));
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
     * Mengusulkan Klinik dari nama poliklinik pasien - hanya kalau petugas
     * belum memilih. Dicocokkan longgar karena nama poli di RS tidak selalu
     * sama persis dengan dua belas pilihan pada kertas; yang tidak cocok
     * dibiarkan kosong supaya petugas memilih sendiri, bukan ditebak.
     */
    private void usulKlinik(String namaPoli) {
        if (Klinik.getSelectedIndex() > 0 || namaPoli.trim().isEmpty()) {
            return;
        }
        String p = namaPoli.toUpperCase();
        for (int n = 1; n < PILIHAN_KLINIK.length - 1; n++) {
            String kandidat = PILIHAN_KLINIK[n].toUpperCase();
            String inti = kandidat.split(" ")[0];
            if (p.contains(kandidat) || (inti.length() >= 4 && p.contains(inti))) {
                Klinik.setSelectedItem(PILIHAN_KLINIK[n]);
                return;
            }
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
        barisHtml(h, "Klinik", String.valueOf(Klinik.getSelectedItem()));
        barisHtml(h, "DPJP", NmDokter.getText());

        judulHtml(h, "PERENCANAAN PEMBERIAN EDUKASI");
        barisHtml(h, "Keyakinan dan nilai", KeyakinanNilai.getText());
        barisHtml(h, "Budaya", Budaya.getText());
        barisHtml(h, "Kemampuan membaca", teksAman(pilihan(bgMembaca)));
        barisHtml(h, "Tingkat pendidikan", String.valueOf(TingkatPendidikan.getSelectedItem()));
        StringBuilder bhs = new StringBuilder();
        if (CekIndonesia.isSelected()) {
            bhs.append("Indonesia ").append(CmbIndonesia.getSelectedItem());
        }
        if (CekInggris.isSelected()) {
            bhs.append(bhs.length() > 0 ? ", " : "").append("Inggris ").append(CmbInggris.getSelectedItem());
        }
        if (!Daerah1.getText().trim().isEmpty()) {
            bhs.append(bhs.length() > 0 ? ", " : "").append(Daerah1.getText()).append(" ").append(CmbDaerah1.getSelectedItem());
        }
        if (!Daerah2.getText().trim().isEmpty()) {
            bhs.append(bhs.length() > 0 ? ", " : "").append(Daerah2.getText()).append(" ").append(CmbDaerah2.getSelectedItem());
        }
        barisHtml(h, "Bahasa sehari-hari", bhs.length() == 0 ? "<i>belum diisi</i>" : bhs.toString());
        barisHtml(h, "Penerjemah", gabung(pilihan(bgPenerjemah), KetPenerjemah.getText()));
        barisHtml(h, "Hambatan emosional", gabung(pilihan(bgEmosional), KetEmosional.getText()));
        barisHtml(h, "Hambatan motivasi", gabung(pilihan(bgMotivasi), KetMotivasi.getText()));
        barisHtml(h, "Keterbatasan fisik", gabung(pilihan(bgFisik), KetFisik.getText()));
        barisHtml(h, "Keterbatasan kognitif", gabung(pilihan(bgKognitif), KetKognitif.getText()));
        barisHtml(h, "Kesediaan menerima", teksAman(pilihan(bgKesediaan)));

        judulHtml(h, "RIWAYAT");
        barisHtml(h, "Penyakit dahulu", riwayat(pilihan(bgRpd), RpdDm, RpdJantung, RpdHipertensi,
                RpdLain1.getText(), RpdLain2.getText()));
        StringBuilder al = new StringBuilder();
        for (String x : new String[]{Alergi1.getText(), Alergi2.getText(), Alergi3.getText(), Alergi4.getText()}) {
            if (!x.trim().isEmpty()) {
                al.append(al.length() > 0 ? ", " : "").append(x);
            }
        }
        barisHtml(h, "Alergi", teksAman(pilihan(bgAlergi))
                + (al.length() == 0 ? "" : " &mdash; " + al));
        barisHtml(h, "Sakit pada keluarga", riwayat(pilihan(bgRpk), RpkDm, RpkJantung, RpkHipertensi,
                RpkLain1.getText(), RpkLain2.getText()));

        judulHtml(h, "RISIKO JATUH (Get Up And Go)");
        barisHtml(h, "Cara berjalan", teksAman(pilihan(bgJatuh1)));
        barisHtml(h, "Menopang saat duduk", teksAman(pilihan(bgJatuh2)));
        barisHtml(h, "Tingkat risiko", "<b>" + LabelRisiko.getText().replace("Tingkat risiko : ", "") + "</b>");

        judulHtml(h, "ANAMNESIS & PEMERIKSAAN FISIK");
        barisHtml(h, "Keluhan utama", KeluhanUtama.getText());
        barisHtml(h, "Riwayat penyakit sekarang", Rps.getText());
        barisHtml(h, "Keadaan umum", KeadaanUmum.getText());
        barisHtml(h, "Tanda vital", "N " + Nadi.getText() + "  RR " + Nafas.getText()
                + "  S " + Suhu.getText() + "  TD " + Sistolik.getText() + "/" + Diastolik.getText());
        barisHtml(h, "BB / TB / Gizi", Bb.getText() + " kg / " + Tinggi.getText() + " cm / "
                + StatusGizi.getSelectedItem());

        judulHtml(h, "DIAGNOSIS & RENCANA");
        barisHtml(h, "Diagnosis 1", Diagnosis1.getText());
        barisHtml(h, "Diagnosis 2", Diagnosis2.getText());
        barisHtml(h, "Rencana & Terapi", RencanaTerapi.getText());

        h.append("</table></html>");
        LoadHTML.setText(h.toString());
        LoadHTML.setCaretPosition(0);
    }

    private String gabung(String pilihan, String keterangan) {
        String p = teksAman(pilihan);
        return keterangan.trim().isEmpty() ? p : p + " - " + keterangan;
    }

    private String riwayat(String ada, widget.CekBox dm, widget.CekBox jantung,
            widget.CekBox hipertensi, String lain1, String lain2) {
        StringBuilder b = new StringBuilder(teksAman(ada));
        StringBuilder d = new StringBuilder();
        if (dm.isSelected()) {
            d.append("Diabetes Melitus");
        }
        if (jantung.isSelected()) {
            d.append(d.length() > 0 ? ", " : "").append("Penyakit Jantung");
        }
        if (hipertensi.isSelected()) {
            d.append(d.length() > 0 ? ", " : "").append("Hipertensi");
        }
        for (String x : new String[]{lain1, lain2}) {
            if (!x.trim().isEmpty()) {
                d.append(d.length() > 0 ? ", " : "").append(x);
            }
        }
        return d.length() == 0 ? b.toString() : b + " &mdash; " + d;
    }

    private void judulHtml(StringBuilder h, String judul) {
        h.append("<tr class='isi'><td colspan='2' bgcolor='#FFFAF8'><b>").append(judul).append("</b></td></tr>");
    }

    private void barisHtml(StringBuilder h, String label, String nilai) {
        h.append("<tr class='isi'><td width='34%' valign='top'>").append(label)
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
        String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        String kd = tbObat.getValueAt(tbObat.getSelectedRow(), 8) == null
                ? "" : tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString();
        String nama = tbObat.getValueAt(tbObat.getSelectedRow(), 9) == null
                ? "" : tbObat.getValueAt(tbObat.getSelectedRow(), 9).toString();

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        param.put("norawat", noRawat);
        param.put("finger", tandaTangan(kd, nama));
        Valid.MyReport("rptAsesmenMedisAwalRalanRM02.jasper", "report",
                "::[ Asesmen Medis Awal Rawat Jalan ]::", param);
    }

    private String tandaTangan(String kdDokter, String nama) {
        String nik = Sequel.cariIsi("select nik from dokter where kd_dokter=?", kdDokter);
        String finger = Sequel.cariIsi(
                "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id "
                + "where pegawai.nik=?", nik == null ? "" : nik);
        return "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                + "\nDitandatangani secara elektronik oleh " + nama
                + "\nID " + (finger == null || finger.isEmpty() ? kdDokter : finger);
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
                "select no_rawat from asesmen_medis_awal_ralan where no_rawat=?", noRawat);
        return ada != null && !ada.trim().isEmpty();
    }

    private void muatRecord(String noRawat) {
        try {
            ps = koneksi.prepareStatement("select * from asesmen_medis_awal_ralan where no_rawat=?");
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
     * Tanpa gerbang hak akses, mengikuti RM 21/33/01. Method ini tetap ada
     * karena pemanggil memanggilnya; tugasnya mengisi DPJP dengan dokter
     * pendaftaran kalau belum terisi.
     */
    public void isCek() {
        // DPJP diisi lewat muatIdentitas() dari reg_periksa.kd_dokter.
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
