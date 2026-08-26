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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import kepegawaian.DlgCariDokter;
import widget.ThemeConfig;

/**
 * RINGKASAN PASIEN MASUK KELUAR (RM 17-01) - RSUD Demang Sepulau Raya.
 *
 * <p>Menggantikan jalur cetak lama {@code rptRM2D_2.jasper} ("RM.02
 * REV.01.07-2019") yang ternyata formulir yang BERBEDA - bukan versi lain dari
 * lembar yang sama. Berkas lama tidak dihapus, hanya tidak dipanggil lagi.</p>
 *
 * <p><b>Kenapa perlu form input, padahal jalur lama langsung cetak.</b> Ada
 * sembilan bagian pada lembar fisik yang datanya tidak ada di mana pun dalam
 * skema (nama suami/istri/ayah, dikirim oleh, kasus polisi, komplikasi, ke
 * klinik, rujuk ke) ditambah SATU HALAMAN PENUH "Sebab Kematian" yang nol
 * persen ada padanannya. Kalau tetap cetak-langsung, lembar keluar dengan
 * belasan bagian kosong dan petugas tetap menulis tangan - sama saja dengan
 * keadaan sebelumnya.</p>
 *
 * <p><b>Prinsip pemisahan data.</b> Tabel {@code ringkasan_pasien_masuk_keluar}
 * SENGAJA tidak menyimpan ulang identitas &amp; data registrasi pasien. Semua
 * itu diambil lewat JOIN saat form dibuka dan saat mencetak, supaya lembar ini
 * selalu mengikuti master dan tidak bisa melenceng dari data pendaftaran. Yang
 * disimpan hanya yang tidak ada sumbernya, plus yang sumbernya ada tapi
 * kosakatanya berbeda dari kertas sehingga butuh keputusan manusia.</p>
 *
 * <p><b>Pra-isi otomatis.</b> Atas permintaan user, apa pun yang bisa diisi
 * otomatis diisi otomatis - lihat {@link #praIsiDariSistem()}. Nilainya tetap
 * bisa ditimpa petugas, karena sebagian hanya SARAN (terutama Keadaan Keluar,
 * yang kosakata sistemnya tidak cocok persis dengan lima pilihan di kertas).</p>
 *
 * <p><b>Ditulis tangan tanpa {@code .form}/blok GEN</b>, mengikuti preseden
 * {@code RMAsesmenMedisRanap} (RM 20): seluruh tata letak memakai
 * {@link GridBagLayout} sehingga tumpang tindih tidak mungkin terjadi secara
 * struktural. Konsekuensinya file ini tidak bisa dibuka di NetBeans Form
 * Editor. Gotcha widget yang dipatuhi: {@code widget.TextBox}/{@code ComboBox}/
 * {@code Tanggal} wajib {@code setPreferredSize()} eksplisit (constructor-nya
 * diam-diam {@code setSize(WIDTH,23)} dengan WIDTH=1 warisan
 * {@code ImageObserver}), {@code widget.Label} defaultnya rata KANAN, dan
 * {@code widget.Tanggal} buatan tangan wajib {@code setDisplayFormat(...)}.</p>
 */
public final class RMRingkasanPasienMasukKeluar extends javax.swing.JDialog {

    /**
     * Nama kolom {@code ringkasan_pasien_masuk_keluar} di luar {@code no_rawat},
     * dipakai BERSAMA oleh {@link #simpan()} dan {@link #ganti()} supaya urutan
     * kolom dan urutan nilai tidak pernah bisa desinkron - satu daftar, dua
     * pemakai.
     *
     * <p>Urutannya WAJIB sama persis dengan {@link #nilaiRm17()}.</p>
     */
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

    /** Kode poliklinik IGD - dipakai menebak "Masuk Melalui". */
    private static final String KD_POLI_IGD = "IGDK";

    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;
    private DlgCariDokter dokter;
    /** 0 = dokter merawat, 1..4 = dokter tindakan, 5 = pemberi keterangan kematian. */
    private int tujuanPilihDokter = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;

    // ---------- Kerangka ----------
    private widget.InternalFrame internalFrame1, internalFrame2, internalFrame3;
    private widget.panelisi panelGlass8, panelGlass9;
    private javax.swing.JTabbedPane TabRawat;
    private widget.ScrollPane scrollInput;
    private widget.PanelBiasa FormInput;
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnPrint, BtnAll, BtnKeluar;

    // ---------- Header (semuanya READ-ONLY, ikut master registrasi) ----------
    private widget.TextBox TNoRw, TNoRM, TPasien, TmpLahir, TglLahir, TUsia, Jk;
    private widget.TextBox TAlamat, TTelepon, TStatus, TAgama, TPekerjaan, TPendidikan;
    private widget.TextBox TMasukTgl, TMasukJam, TRuang, TKeluarTgl, TKeluarJam;
    private widget.TextBox TNamaIbu;

    // ---------- Identitas keluarga ----------
    private widget.TextBox NamaSuami, NamaIstri, NamaAyah;

    // ---------- Masuk & pengirim ----------
    private widget.ComboBox MasukMelalui, DikirimOleh;
    private widget.RadioButton RbKasusPolisiYa, RbKasusPolisiTidak;
    private final ButtonGroup bgKasusPolisi = new ButtonGroup();

    // ---------- Diagnosa ----------
    private widget.TextBox DiagnoseSementara, AlergiTerhadap;
    private widget.TextBox DiagnoseAkhir, Komplikasi, DiagnoseLain, KodeIcd;

    // ---------- Tindakan (4 baris tetap, label sudah tercetak di kertas) ----------
    private final widget.TextBox[] Tindakan = new widget.TextBox[4];
    private final widget.Tanggal[] TindakanTgl = new widget.Tanggal[4];
    private final widget.TextBox[] TindakanKdDokter = new widget.TextBox[4];
    private final widget.TextBox[] TindakanNmDokter = new widget.TextBox[4];
    private final widget.Button[] BtnTindakanDokter = new widget.Button[4];

    // ---------- Keluar ----------
    private widget.ComboBox KeadaanKeluar, CaraKeluarMeninggal;
    private widget.TextBox RujukKe, KeKlinik;
    private widget.CekBox ChkKunjunganUlang;
    private widget.Tanggal KunjunganUlangTgl;
    private widget.TextBox KdDokterMerawat, NmDokterMerawat, TNipDokter;
    private widget.Button BtnDokterMerawat;

    // ---------- Halaman 2 : Sebab Kematian (kondisional) ----------
    private widget.CekBox ChkMeninggal;
    private widget.PanelBiasa panelKematian;
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
        rakitHeader();
        rakitBadan();
        rakitTabData();

        widget.IconStyle.pasang(this);
        bersihkanIkonRadio(getContentPane());
        // Panel Sebab Kematian disembunyikan SEBELUM ukuran dihitung.
        // GridBagLayout mengecualikan komponen tak-terlihat dari perhitungan
        // preferred size; kalau urutannya terbalik, ruang untuk panel yang
        // seharusnya tersembunyi tetap ter-reserve dan menyisakan celah kosong
        // (bug yang persis pernah terjadi di RM 03-2 pada panel Bayi Baru Lahir).
        perbaruiVisibilitasKematian();
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
                "::[ Ringkasan Pasien Masuk Keluar (RM 17-01) ]::"));
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
        TabRawat.addTab("Input Ringkasan", internalFrame2);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
    }

    /**
     * Mengembalikan ikon bawaan pada semua radio button.
     *
     * <p>{@code widget.IconStyle.pasang(this)} memasang ikon SVG ke SEMUA
     * {@code AbstractButton} berdasarkan KATA KUNCI pada teksnya, dan
     * {@code JRadioButton} ikut kena. Di form ini radio bernama "Ya"/"Tidak"
     * relatif aman, tapi pembersihan tetap dilakukan mengikuti preseden RM 20 -
     * di sana radio "Keluarga" sempat mendapat ikon log-out karena mengandung
     * kata "keluar". {@code JCheckBox} sengaja tidak ikut: {@code ChkAccor}
     * memang memerlukan ikon panahnya.</p>
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
            // Ikon raster lama memang disembunyikan widget.Button kalau tombol
            // punya teks; IconStyle.pasang() yang mengisi ikon sesungguhnya.
        }
        b.setMnemonic(mnemonic);
        b.setText(teks);
        b.setToolTipText("Alt+" + mnemonic);
        b.setPreferredSize(new Dimension(100, 30));
        return b;
    }

    // =================================================================
    //  HELPER TATA LETAK  (disalin dari pola RM 20)
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

    /**
     * Penawar bug baku GridBagLayout: kalau SEMUA komponen ber-{@code weightx=0}
     * dan container diberi lebih banyak lebar daripada isinya, GridBagLayout
     * MEN-TENGAH-KAN seluruh grid alih-alih merapatkannya ke kiri. Filler
     * tak-terlihat di kolom paling kanan menyerap semua sisa lebar.
     */
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
     * Pasangan vertikal dari {@link #tambahPengisiKolom}.
     *
     * <p>Bug yang sama berlaku pada sumbu Y: kalau SEMUA baris ber-{@code
     * weighty=0} dan container lebih tinggi daripada isinya, GridBagLayout
     * men-TENGAH-kan seluruh grid secara vertikal. Terlihat nyata saat form
     * dirender: seluruh isi mengambang dengan pita kosong besar di atas dan di
     * bawahnya. Glue tak-terlihat di baris paling bawah menyerap semua sisa
     * tinggi sehingga isi terdorong rapat ke atas.</p>
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

    /** Label kolom kiri, rata KANAN supaya semua titik dua sejajar. */
    private widget.Label labelKolom(String teks) {
        widget.Label l = new widget.Label();
        l.setText(teks + " :");
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    /** Label bebas rata kiri (satuan, keterangan). */
    private widget.Label teks(String isi) {
        widget.Label l = new widget.Label();
        l.setText(isi);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    /** Merangkai beberapa komponen pendek jadi SATU baris padat. */
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

    /**
     * Kotak read-only untuk data yang datang dari master registrasi.
     *
     * <p>Sengaja dibuat tidak bisa diedit: data ini TIDAK disimpan di tabel
     * form ini, hanya ditampilkan supaya petugas bisa memverifikasi. Kalau
     * bisa diedit, petugas akan mengira perubahannya tersimpan.</p>
     */
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

    /** widget.Tanggal buatan tangan WAJIB diberi displayFormat eksplisit. */
    private widget.Tanggal tanggal(String format, int lebar) {
        widget.Tanggal t = new widget.Tanggal();
        t.setDisplayFormat(format);
        t.setDate(new Date());
        t.setPreferredSize(new Dimension(lebar, 23));
        return t;
    }

    // =================================================================
    //  HEADER - identitas & data registrasi, seluruhnya read-only
    // =================================================================

    private void rakitHeader() {
        widget.PanelBiasa panelHeader = panelSeksi("IDENTITAS PASIEN (otomatis dari pendaftaran)");

        TNoRw = kotakBaca(140);
        TNoRM = kotakBaca(90);
        TPasien = kotakBaca(200);
        TmpLahir = kotakBaca(130);
        TglLahir = kotakBaca(90);
        TUsia = kotakBaca(110);
        Jk = kotakBaca(80);
        // Alamat dicetak APA ADANYA dari registrasi. Baris JALAN/RT-RW/NO. RUMAH
        // pada kertas tidak dibuatkan field: form pendaftaran memang tidak pernah
        // memisahkannya, dan menurut user RT/RW biasanya sudah ikut ditulis di
        // dalam teks alamat. Keputusan eksplisit user.
        TAlamat = kotakBaca(430);
        TTelepon = kotakBaca(130);
        TStatus = kotakBaca(130);
        TAgama = kotakBaca(110);
        TPekerjaan = kotakBaca(160);
        TPendidikan = kotakBaca(110);
        TNamaIbu = kotakBaca(200);
        TMasukTgl = kotakBaca(90);
        TMasukJam = kotakBaca(80);
        TRuang = kotakBaca(200);
        TKeluarTgl = kotakBaca(90);
        TKeluarJam = kotakBaca(80);

        int row = 0;
        panelHeader.add(labelKolom("No.Rawat"), konstrain(0, row));
        panelHeader.add(baris(TNoRw, teks("   No.RM :"), TNoRM,
                teks("   Nama Pasien :"), TPasien), konstrain(1, row));
        row++;

        panelHeader.add(labelKolom("Tempat/Tgl Lahir"), konstrain(0, row));
        panelHeader.add(baris(TmpLahir, TglLahir,
                teks("   Usia :"), TUsia, teks("   L/P :"), Jk), konstrain(1, row));
        row++;

        panelHeader.add(labelKolom("Alamat"), konstrain(0, row));
        panelHeader.add(baris(TAlamat, teks("   Telepon :"), TTelepon), konstrain(1, row));
        row++;

        panelHeader.add(labelKolom("Status / Agama"), konstrain(0, row));
        panelHeader.add(baris(TStatus, TAgama,
                teks("   Pekerjaan :"), TPekerjaan,
                teks("   Pendidikan :"), TPendidikan), konstrain(1, row));
        row++;

        panelHeader.add(labelKolom("Nama Lengkap Ibu"), konstrain(0, row));
        panelHeader.add(baris(TNamaIbu), konstrain(1, row));
        row++;

        panelHeader.add(labelKolom("Masuk"), konstrain(0, row));
        panelHeader.add(baris(TMasukTgl, teks("Jam :"), TMasukJam,
                teks("   Dirawat di Ruang :"), TRuang), konstrain(1, row));
        row++;

        panelHeader.add(labelKolom("Keluar"), konstrain(0, row));
        panelHeader.add(baris(TKeluarTgl, teks("Jam :"), TKeluarJam,
                teks("   (kosong = pasien masih dirawat)")), konstrain(1, row));

        tambahPengisiKolom(panelHeader, 2);
        tambahSeksi(panelHeader, 0);
    }

    /** Menempel satu panel seksi ke FormInput sebagai baris penuh. */
    private void tambahSeksi(JPanel panel, int gridy) {
        GridBagConstraints c = konstrain(0, gridy);
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        FormInput.add(panel, c);
    }

    // =================================================================
    //  BADAN - urutannya mengikuti lembar fisik RM 17-01
    // =================================================================

    private void rakitBadan() {
        int seksi = 1;

        // ---------------- Identitas keluarga ----------------
        widget.PanelBiasa panelKeluarga = panelSeksi("NAMA LENGKAP KELUARGA");
        NamaSuami = kotak(220);
        NamaIstri = kotak(220);
        NamaAyah = kotak(220);
        panelKeluarga.add(labelKolom("Nama Lengkap Suami"), konstrain(0, 0));
        panelKeluarga.add(baris(NamaSuami, teks("   Nama Lengkap Ayah :"), NamaAyah), konstrain(1, 0));
        panelKeluarga.add(labelKolom("Nama Lengkap Istri"), konstrain(0, 1));
        panelKeluarga.add(baris(NamaIstri, teks("   (Nama Ibu diambil otomatis dari data pasien)")), konstrain(1, 1));
        tambahPengisiKolom(panelKeluarga, 2);
        tambahSeksi(panelKeluarga, seksi++);

        // ---------------- Masuk melalui / dikirim oleh / kasus polisi ----------------
        widget.PanelBiasa panelMasuk = panelSeksi("MASUK MELALUI / DIKIRIM OLEH");
        MasukMelalui = combo(new String[]{"", "IGD", "Poliklinik"}, 150);
        // Tidak bisa ditebak otomatis: rujuk_masuk.kategori_rujuk ternyata
        // kategori medis (Bedah/Non-Bedah/Kebidanan/Anak), bukan jenis pengirim,
        // dan rujuk_masuk.perujuk hanya nama bebas.
        DikirimOleh = combo(new String[]{"", "RS Lain", "Puskesmas", "Para Medis", "Polri",
            "Dokter/Dokter Gizi", "Kemauan Sendiri"}, 180);
        RbKasusPolisiYa = radio("Ya", bgKasusPolisi);
        RbKasusPolisiTidak = radio("Tidak", bgKasusPolisi);

        panelMasuk.add(labelKolom("Masuk Melalui"), konstrain(0, 0));
        panelMasuk.add(baris(MasukMelalui, teks("   Dikirim Oleh :"), DikirimOleh), konstrain(1, 0));
        panelMasuk.add(labelKolom("Kasus Polisi"), konstrain(0, 1));
        panelMasuk.add(baris(RbKasusPolisiYa, RbKasusPolisiTidak), konstrain(1, 1));
        tambahPengisiKolom(panelMasuk, 2);
        tambahSeksi(panelMasuk, seksi++);

        // ---------------- Diagnosa ----------------
        widget.PanelBiasa panelDiagnosa = panelSeksi("DIAGNOSA");
        DiagnoseSementara = kotak(430);
        AlergiTerhadap = kotak(430);
        DiagnoseAkhir = kotak(430);
        Komplikasi = kotak(300);
        DiagnoseLain = kotak(300);
        KodeIcd = kotak(160);

        int r = 0;
        panelDiagnosa.add(labelKolom("Diagnose Sementara"), konstrain(0, r));
        panelDiagnosa.add(baris(DiagnoseSementara), konstrain(1, r));
        r++;
        panelDiagnosa.add(labelKolom("Alergi Terhadap"), konstrain(0, r));
        panelDiagnosa.add(baris(AlergiTerhadap), konstrain(1, r));
        r++;
        panelDiagnosa.add(labelKolom("Diagnose Akhir"), konstrain(0, r));
        panelDiagnosa.add(baris(DiagnoseAkhir, teks("   Kode ICD :"), KodeIcd), konstrain(1, r));
        r++;
        panelDiagnosa.add(labelKolom("Komplikasi"), konstrain(0, r));
        panelDiagnosa.add(baris(Komplikasi, teks("   Diagnose Lain :"), DiagnoseLain), konstrain(1, r));
        tambahPengisiKolom(panelDiagnosa, 2);
        tambahSeksi(panelDiagnosa, seksi++);

        // ---------------- Tindakan ----------------
        widget.PanelBiasa panelTindakan = panelSeksi("TINDAKAN");
        String[] labelTindakan = {"1. Operasi Ke 1", "2. Operasi Ke 2", "3. Operasi Khusus", "4. Tindakan Lain"};
        for (int n = 0; n < 4; n++) {
            Tindakan[n] = kotak(300);
            TindakanTgl[n] = tanggal("dd-MM-yyyy", 100);
            TindakanKdDokter[n] = kotakBaca(70);
            TindakanNmDokter[n] = kotakBaca(180);
            final int idx = n;
            BtnTindakanDokter[n] = new widget.Button();
            BtnTindakanDokter[n].setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
            BtnTindakanDokter[n].setPreferredSize(new Dimension(28, 23));
            BtnTindakanDokter[n].setToolTipText("Cari dokter");
            BtnTindakanDokter[n].addActionListener(evt -> bukaCariDokter(idx + 1));

            panelTindakan.add(labelKolom(labelTindakan[n]), konstrain(0, n));
            panelTindakan.add(baris(Tindakan[n],
                    teks("   Tanggal :"), TindakanTgl[n],
                    teks("   Dokter :"), TindakanKdDokter[n], TindakanNmDokter[n], BtnTindakanDokter[n]),
                    konstrain(1, n));
        }
        GridBagConstraints ct = konstrain(1, 4);
        panelTindakan.add(teks("Tanggal & dokter hanya tersimpan bila kolom tindakannya diisi."), ct);
        tambahPengisiKolom(panelTindakan, 2);
        tambahSeksi(panelTindakan, seksi++);

        // ---------------- Keadaan / cara keluar ----------------
        widget.PanelBiasa panelKeluar = panelSeksi("KEADAAN KELUAR");
        // Lima pilihan ini mengikuti PERSIS kertas. Sistem punya tiga sumber
        // dengan kosakata berbeda (resume.keadaan, resume.cara_keluar,
        // kamar_inap.stts_pulang) dan tidak satu pun cocok persis, jadi sistem
        // hanya MENYARANKAN - petugas yang menentukan.
        KeadaanKeluar = combo(new String[]{"", "Sembuh", "Berobat Jalan", "Pulang Paksa", "Pindah RS", "Rujuk"}, 160);
        RujukKe = kotak(280);
        ChkKunjunganUlang = new widget.CekBox();
        ChkKunjunganUlang.setText("Ada kunjungan ulang");
        KunjunganUlangTgl = tanggal("dd-MM-yyyy", 100);
        KeKlinik = kotak(200);
        KdDokterMerawat = kotakBaca(70);
        NmDokterMerawat = kotakBaca(200);
        TNipDokter = kotakBaca(180);
        BtnDokterMerawat = new widget.Button();
        BtnDokterMerawat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnDokterMerawat.setPreferredSize(new Dimension(28, 23));
        BtnDokterMerawat.setToolTipText("Cari dokter");
        BtnDokterMerawat.addActionListener(evt -> bukaCariDokter(0));

        r = 0;
        panelKeluar.add(labelKolom("Keadaan Keluar"), konstrain(0, r));
        panelKeluar.add(baris(KeadaanKeluar, teks("   Rujuk ke :"), RujukKe), konstrain(1, r));
        r++;
        panelKeluar.add(labelKolom("Kunjungan Ulang"), konstrain(0, r));
        panelKeluar.add(baris(ChkKunjunganUlang, KunjunganUlangTgl,
                teks("   Ke Klinik :"), KeKlinik), konstrain(1, r));
        r++;
        panelKeluar.add(labelKolom("Dokter Yang Merawat"), konstrain(0, r));
        panelKeluar.add(baris(KdDokterMerawat, NmDokterMerawat, BtnDokterMerawat,
                teks("   NIP :"), TNipDokter), konstrain(1, r));
        r++;
        panelKeluar.add(labelKolom("Pasien Meninggal"), konstrain(0, r));
        ChkMeninggal = new widget.CekBox();
        ChkMeninggal.setText("Ya - tampilkan bagian Sebab Kematian (halaman 2)");
        ChkMeninggal.addActionListener(evt -> perbaruiVisibilitasKematian());
        MeninggalTgl = tanggal("dd-MM-yyyy HH:mm:ss", 150);
        CaraKeluarMeninggal = combo(new String[]{"", "Sebelum 48 Jam", "Sesudah 48 Jam"}, 150);
        panelKeluar.add(baris(ChkMeninggal), konstrain(1, r));
        r++;
        panelKeluar.add(labelKolom("Meninggal Tgl/Jam"), konstrain(0, r));
        panelKeluar.add(baris(MeninggalTgl, teks("   Cara Keluar :"), CaraKeluarMeninggal), konstrain(1, r));
        tambahPengisiKolom(panelKeluar, 2);
        tambahSeksi(panelKeluar, seksi++);

        rakitPanelKematian();
        tambahSeksi(panelKematian, seksi++);

        // Glue penutup: menahan seluruh isi rapat ke atas. Wajib diletakkan
        // SETELAH seksi terakhir supaya nomor barisnya benar-benar paling bawah.
        tambahPengisiBaris(FormInput, seksi);
    }

    /** Halaman 2 lembar fisik - hanya relevan bila pasien meninggal. */
    private void rakitPanelKematian() {
        panelKematian = panelSeksi("SEBAB KEMATIAN (halaman 2)");
        SkPenyakitA = kotak(380);
        SkLamaA = kotak(160);
        SkPenyakitB = kotak(380);
        SkLamaB = kotak(160);
        SkPenyakitC = kotak(380);
        SkLamaC = kotak(160);
        SkPenyakitLain = kotak(380);
        SkLamaLain = kotak(160);
        RpMacam = kotak(320);
        RpCaraKejadian = kotak(320);
        RpSifatJenis = kotak(320);
        RbKmYa = radio("Ya", bgKm);
        RbKmTidak = radio("Tidak", bgKm);
        KmSebab = kotak(320);
        RbPkPersalinanYa = radio("Ya", bgPkPersalinan);
        RbPkPersalinanTidak = radio("Tidak", bgPkPersalinan);
        RbPkKehamilanYa = radio("Ya", bgPkKehamilan);
        RbPkKehamilanTidak = radio("Tidak", bgPkKehamilan);
        RbOpYa = radio("Ya", bgOp);
        RbOpTidak = radio("Tidak", bgOp);
        OpJam = kotak(120);
        SkTanggal = tanggal("dd-MM-yyyy", 100);
        KdPemberiKeterangan = kotakBaca(70);
        NmPemberiKeterangan = kotakBaca(200);
        BtnPemberiKeterangan = new widget.Button();
        BtnPemberiKeterangan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnPemberiKeterangan.setPreferredSize(new Dimension(28, 23));
        BtnPemberiKeterangan.setToolTipText("Cari dokter");
        BtnPemberiKeterangan.addActionListener(evt -> bukaCariDokter(5));

        int r = 0;
        panelKematian.add(teks("Lamanya = kira-kira, mulai sakit hingga meninggal dunia."), konstrain(1, r));
        r++;
        panelKematian.add(labelKolom("a. Penyebab langsung"), konstrain(0, r));
        panelKematian.add(baris(SkPenyakitA, teks("   Lamanya :"), SkLamaA), konstrain(1, r));
        r++;
        panelKematian.add(labelKolom("b. Disebabkan oleh"), konstrain(0, r));
        panelKematian.add(baris(SkPenyakitB, teks("   Lamanya :"), SkLamaB), konstrain(1, r));
        r++;
        panelKematian.add(labelKolom("c. Disebabkan oleh"), konstrain(0, r));
        panelKematian.add(baris(SkPenyakitC, teks("   Lamanya :"), SkLamaC), konstrain(1, r));
        r++;
        panelKematian.add(labelKolom("Penyakit lain yang berpengaruh"), konstrain(0, r));
        panelKematian.add(baris(SkPenyakitLain, teks("   Lamanya :"), SkLamaLain), konstrain(1, r));
        r++;

        panelKematian.add(labelKolom("I. Ruda Paksa - Macam"), konstrain(0, r));
        panelKematian.add(baris(RpMacam), konstrain(1, r));
        r++;
        panelKematian.add(labelKolom("   Cara Kejadian"), konstrain(0, r));
        panelKematian.add(baris(RpCaraKejadian), konstrain(1, r));
        r++;
        panelKematian.add(labelKolom("   Sifat Jenis (Kerusakan Tubuh)"), konstrain(0, r));
        panelKematian.add(baris(RpSifatJenis), konstrain(1, r));
        r++;

        panelKematian.add(labelKolom("II. Janin Lahir Mati"), konstrain(0, r));
        panelKematian.add(baris(RbKmYa, RbKmTidak, teks("   Sebab :"), KmSebab), konstrain(1, r));
        r++;

        panelKematian.add(labelKolom("III. Peristiwa Persalinan"), konstrain(0, r));
        panelKematian.add(baris(RbPkPersalinanYa, RbPkPersalinanTidak,
                teks("      Peristiwa Kehamilan :"), RbPkKehamilanYa, RbPkKehamilanTidak), konstrain(1, r));
        r++;

        panelKematian.add(labelKolom("IV. Dilakukan Operasi"), konstrain(0, r));
        panelKematian.add(baris(RbOpYa, RbOpTidak, teks("   Jam Operasi :"), OpJam), konstrain(1, r));
        r++;

        panelKematian.add(labelKolom("Tanggal Keterangan"), konstrain(0, r));
        panelKematian.add(baris(SkTanggal,
                teks("   Pemberi Keterangan :"), KdPemberiKeterangan, NmPemberiKeterangan,
                BtnPemberiKeterangan), konstrain(1, r));
        tambahPengisiKolom(panelKematian, 2);
    }

    private void perbaruiVisibilitasKematian() {
        boolean tampil = ChkMeninggal.isSelected();
        panelKematian.setVisible(tampil);
        MeninggalTgl.setEnabled(tampil);
        CaraKeluarMeninggal.setEnabled(tampil);
        FormInput.revalidate();
        FormInput.repaint();
    }

    // =================================================================
    //  TAB DATA
    // =================================================================

    private void rakitTabData() {
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetakRingkasan = new javax.swing.JMenuItem();
        MnCetakRingkasan.setFont(new java.awt.Font(ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        MnCetakRingkasan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png")));
        MnCetakRingkasan.setText("Cetak Ringkasan Pasien Masuk Keluar");
        MnCetakRingkasan.setPreferredSize(new Dimension(260, 26));
        MnCetakRingkasan.addActionListener(evt -> cetakTerpilih());
        jPopupMenu1.add(MnCetakRingkasan);

        internalFrame3 = new widget.InternalFrame();
        internalFrame3.setBorder(null);
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

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

        panelGlass9.add(teks("Tgl.Masuk :"));
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
        BtnPrint1.setToolTipText("Cetak ringkasan pasien ini");
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

        TabRawat.addTab("Data Ringkasan", internalFrame3);
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
    //  DIALOG DOKTER
    // =================================================================

    /**
     * Membuka pencarian dokter.
     *
     * <p>{@code DlgCariDokter} tidak menyediakan getter hasil pilihan; polanya
     * di codebase ini adalah membaca {@code getTable()} pada saat dialog
     * DITUTUP (kolom 0 = kode dokter, kolom 1 = nama). Disalin dari
     * {@code RMAsesmenMedisRanap} (RM 20).</p>
     *
     * @param tujuan 0 = dokter merawat, 1..4 = dokter tindakan ke-n,
     *               5 = pemberi keterangan sebab kematian
     */
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
        if (kd == null || kd.trim().isEmpty()) {
            return;
        }
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

    /**
     * NIP dokter, diambil dari {@code pegawai.nik} dan ditampilkan APA ADANYA.
     *
     * <p>Keputusan eksplisit user: di produksi isi kolom itu beragam - sebagian
     * NIP (untuk dokter PNS), sebagian kode pegawai - dan untuk sementara
     * ditampilkan apa adanya sampai user memastikan aturannya. Karena itu TIDAK
     * ada penyaringan bentuk di sini.</p>
     *
     * <p>Dua jalur pencocokan dicoba berurutan karena tidak ada kunci resmi
     * yang menghubungkan {@code dokter} ke {@code pegawai}: konvensi codebase
     * memakai {@code pegawai.nik=dokter.kd_dokter} (dipakai di
     * {@code SmartKlaimBPJSKirimFHIR} dan {@code RMTriaseIGD}), tapi konvensi
     * itu hanya berlaku bila nik memang berisi kode dokter. Kalau nik berisi
     * NIP sungguhan, satu-satunya penghubung yang tersisa adalah nama.</p>
     */
    private String nipDokter(String kdDokter, String namaDokter) {
        String nip = Sequel.cariIsi("select pegawai.nik from pegawai where pegawai.nik=?", kdDokter);
        if (nip == null || nip.trim().isEmpty()) {
            nip = Sequel.cariIsi("select pegawai.nik from pegawai where pegawai.nama=?", namaDokter);
        }
        return nip == null ? "" : nip;
    }

    // =================================================================
    //  LAPISAN DATA
    // =================================================================

    private String[] nilaiRm17() {
        return new String[]{
            NamaSuami.getText(), NamaIstri.getText(), NamaAyah.getText(),
            kosongKeNull(MasukMelalui), kosongKeNull(DikirimOleh), pilihan(bgKasusPolisi),
            DiagnoseSementara.getText(), AlergiTerhadap.getText(),
            DiagnoseAkhir.getText(), Komplikasi.getText(), DiagnoseLain.getText(), KodeIcd.getText(),
            Tindakan[0].getText(), tglTindakan(0), kodeTindakanDokter(0),
            Tindakan[1].getText(), tglTindakan(1), kodeTindakanDokter(1),
            Tindakan[2].getText(), tglTindakan(2), kodeTindakanDokter(2),
            Tindakan[3].getText(), tglTindakan(3), kodeTindakanDokter(3),
            meninggalTanggal(), meninggalJam(),
            kosongKeNull(KeadaanKeluar), RujukKe.getText(), kosongKeNull(CaraKeluarMeninggal),
            ChkKunjunganUlang.isSelected() ? Valid.SetTgl(KunjunganUlangTgl.getSelectedItem() + "") : null,
            KeKlinik.getText(), kosongKeNull(KdDokterMerawat.getText()),
            SkPenyakitA.getText(), SkLamaA.getText(), SkPenyakitB.getText(), SkLamaB.getText(),
            SkPenyakitC.getText(), SkLamaC.getText(), SkPenyakitLain.getText(), SkLamaLain.getText(),
            RpMacam.getText(), RpCaraKejadian.getText(), RpSifatJenis.getText(),
            pilihan(bgKm), KmSebab.getText(),
            pilihan(bgPkPersalinan), pilihan(bgPkKehamilan),
            pilihan(bgOp), OpJam.getText(),
            ChkMeninggal.isSelected() ? Valid.SetTgl(SkTanggal.getSelectedItem() + "") : null,
            kosongKeNull(KdPemberiKeterangan.getText())
        };
    }

    /**
     * Tanggal tindakan hanya bermakna kalau uraian tindakannya diisi.
     *
     * <p>{@code widget.Tanggal} selalu punya nilai (default hari ini), jadi
     * tanpa gerbang ini setiap lembar akan menyimpan empat tanggal tindakan
     * palsu untuk baris yang sebenarnya kosong.</p>
     */
    private String tglTindakan(int idx) {
        return Tindakan[idx].getText().trim().isEmpty()
                ? null : Valid.SetTgl(TindakanTgl[idx].getSelectedItem() + "");
    }

    private String kodeTindakanDokter(int idx) {
        return Tindakan[idx].getText().trim().isEmpty()
                ? null : kosongKeNull(TindakanKdDokter[idx].getText());
    }

    private String meninggalTanggal() {
        return ChkMeninggal.isSelected() ? Valid.SetTgl(MeninggalTgl.getSelectedItem() + "") : null;
    }

    private String meninggalJam() {
        return ChkMeninggal.isSelected()
                ? MeninggalTgl.getSelectedItem().toString().substring(11, 19) : null;
    }

    /**
     * Item combo kosong ("") disimpan sebagai NULL.
     *
     * <p>Kolom-kolom ini bertipe ENUM tanpa nilai string kosong; menyimpan ""
     * ke ENUM akan ditolak MySQL atau (pada mode non-strict) diam-diam berubah
     * jadi nilai kosong yang tidak sah.</p>
     */
    private String kosongKeNull(widget.ComboBox combo) {
        Object v = combo.getSelectedItem();
        return kosongKeNull(v == null ? null : v.toString());
    }

    private String kosongKeNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
    }

    /** Teks radio yang terpilih, atau null. Teks radio SENGAJA sama dengan nilai ENUM. */
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
        for (String k : KOLOM_RM17) {
            kol.append(",").append(k);
            tanya.append(",?");
        }
        try (PreparedStatement pstm = koneksi.prepareStatement(
                "insert into ringkasan_pasien_masuk_keluar(" + kol + ") values(" + tanya + ")")) {
            pstm.setString(1, TNoRw.getText());
            String[] nilai = nilaiRm17();
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
        for (String k : KOLOM_RM17) {
            if (set.length() > 0) {
                set.append(",");
            }
            set.append(k).append("=?");
        }
        String[] nilai = nilaiRm17();
        String[] param = new String[nilai.length + 1];
        System.arraycopy(nilai, 0, param, 0, nilai.length);
        param[nilai.length] = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();

        if (Sequel.mengedittf("ringkasan_pasien_masuk_keluar", "no_rawat=?", set.toString(),
                param.length, param)) {
            runBackground(() -> tampil());
            TabRawat.setSelectedIndex(1);
        }
    }

    private void aksiHapus() {
        if (tbObat.getSelectedRow() > -1) {
            if (Sequel.queryu2tf("delete from ringkasan_pasien_masuk_keluar where no_rawat=?", 1,
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

    /**
     * Query daftar. Tanggal yang dipakai sebagai penyaring adalah TANGGAL MASUK
     * dari {@code kamar_inap} baris paling awal - lembar ini memang lembar
     * episode rawat inap, bukan lembar harian.
     */
    private String sqlDaftar(boolean pakaiCari) {
        String sql =
                "select r.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,r.kd_dokter_merawat,"
                + "dokter.nm_dokter,r.keadaan_keluar,r.diagnose_akhir,"
                + "(select min(ki.tgl_masuk) from kamar_inap ki where ki.no_rawat=r.no_rawat) as tgl_masuk "
                + "from ringkasan_pasien_masuk_keluar r "
                + "inner join reg_periksa on r.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join dokter on r.kd_dokter_merawat=dokter.kd_dokter "
                + "where (select min(ki.tgl_masuk) from kamar_inap ki where ki.no_rawat=r.no_rawat) between ? and ? ";
        if (pakaiCari) {
            sql += "and (r.no_rawat like ? or pasien.no_rkm_medis like ? "
                    + "or pasien.nm_pasien like ? or dokter.nm_dokter like ? "
                    + "or r.diagnose_akhir like ?) ";
        }
        return sql + "order by tgl_masuk";
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
        // Identitas & data registrasi SELALU dimuat ulang dari master, bukan
        // dari kolom tabel ini - itu inti prinsip pemisahan data form ini.
        muatIdentitas();
        try {
            ps = koneksi.prepareStatement(
                    "select * from ringkasan_pasien_masuk_keluar where no_rawat=?");
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

    /** Mengisi widget dari ResultSet yang sedang menunjuk baris record. */
    private void isiDariRecord() throws Exception {
        NamaSuami.setText(teksAman(rs.getString("nama_suami")));
        NamaIstri.setText(teksAman(rs.getString("nama_istri")));
        NamaAyah.setText(teksAman(rs.getString("nama_ayah")));
        pilihCombo(MasukMelalui, rs.getString("masuk_melalui"));
        pilihCombo(DikirimOleh, rs.getString("dikirim_oleh"));
        pilihRadio(bgKasusPolisi, rs.getString("kasus_polisi"));

        DiagnoseSementara.setText(teksAman(rs.getString("diagnose_sementara")));
        AlergiTerhadap.setText(teksAman(rs.getString("alergi_terhadap")));
        DiagnoseAkhir.setText(teksAman(rs.getString("diagnose_akhir")));
        Komplikasi.setText(teksAman(rs.getString("komplikasi")));
        DiagnoseLain.setText(teksAman(rs.getString("diagnose_lain")));
        KodeIcd.setText(teksAman(rs.getString("kode_icd")));

        for (int n = 0; n < 4; n++) {
            Tindakan[n].setText(teksAman(rs.getString("tindakan_" + (n + 1))));
            String tgl = rs.getString("tindakan_" + (n + 1) + "_tanggal");
            if (tgl != null && !tgl.isEmpty()) {
                Valid.SetTgl2(TindakanTgl[n], tgl);
            }
            String kd = teksAman(rs.getString("tindakan_" + (n + 1) + "_dokter"));
            TindakanKdDokter[n].setText(kd);
            TindakanNmDokter[n].setText(kd.isEmpty() ? "" : Sequel.CariDokter(kd));
        }

        String mTgl = rs.getString("meninggal_tanggal");
        ChkMeninggal.setSelected(mTgl != null && !mTgl.isEmpty());
        if (ChkMeninggal.isSelected()) {
            String jam = teksAman(rs.getString("meninggal_jam"));
            Valid.SetTgl2(MeninggalTgl, mTgl + " " + (jam.isEmpty() ? "00:00:00" : jam));
        }
        perbaruiVisibilitasKematian();

        pilihCombo(KeadaanKeluar, rs.getString("keadaan_keluar"));
        RujukKe.setText(teksAman(rs.getString("rujuk_ke")));
        pilihCombo(CaraKeluarMeninggal, rs.getString("cara_keluar_meninggal"));

        String ku = rs.getString("kunjungan_ulang_tanggal");
        ChkKunjunganUlang.setSelected(ku != null && !ku.isEmpty());
        if (ChkKunjunganUlang.isSelected()) {
            Valid.SetTgl2(KunjunganUlangTgl, ku);
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
        pilihRadio(bgKm, rs.getString("km_janin_lahir_mati"));
        KmSebab.setText(teksAman(rs.getString("km_sebab")));
        pilihRadio(bgPkPersalinan, rs.getString("pk_persalinan"));
        pilihRadio(bgPkKehamilan, rs.getString("pk_kehamilan"));
        pilihRadio(bgOp, rs.getString("op_dilakukan"));
        OpJam.setText(teksAman(rs.getString("op_jam")));
        String skTgl = rs.getString("sk_tanggal");
        if (skTgl != null && !skTgl.isEmpty()) {
            Valid.SetTgl2(SkTanggal, skTgl);
        }
        String kdPk = teksAman(rs.getString("kd_pemberi_keterangan"));
        KdPemberiKeterangan.setText(kdPk);
        NmPemberiKeterangan.setText(kdPk.isEmpty() ? "" : Sequel.CariDokter(kdPk));
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
        NamaSuami.setText("");
        NamaIstri.setText("");
        NamaAyah.setText("");
        MasukMelalui.setSelectedIndex(0);
        DikirimOleh.setSelectedIndex(0);
        bgKasusPolisi.clearSelection();
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
        ChkMeninggal.setSelected(false);
        MeninggalTgl.setDate(new Date());
        KeadaanKeluar.setSelectedIndex(0);
        RujukKe.setText("");
        CaraKeluarMeninggal.setSelectedIndex(0);
        ChkKunjunganUlang.setSelected(false);
        KunjunganUlangTgl.setDate(new Date());
        KeKlinik.setText("");
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
        perbaruiVisibilitasKematian();
        // Setelah dikosongkan, isi ulang dengan saran sistem - bukan dibiarkan
        // kosong melompong. Permintaan user: apa pun yang bisa otomatis, otomatis.
        if (!TNoRw.getText().trim().isEmpty()) {
            muatIdentitas();
            praIsiDariSistem();
        }
        TabRawat.setSelectedIndex(0);
    }

    // =================================================================
    //  PRA-ISI OTOMATIS
    // =================================================================

    /**
     * Mengisi seluruh bagian header (identitas &amp; data registrasi) dari
     * master. Nilainya tidak pernah disimpan ke tabel form ini.
     *
     * <p>Blok MASUK diambil dari baris {@code kamar_inap} PALING AWAL dan blok
     * KELUAR dari baris PALING AKHIR: satu episode bisa punya beberapa baris
     * karena pindah kamar. Tanggal {@code 0000-00-00} (nilai bawaan MySQL untuk
     * pasien yang masih dirawat) diperlakukan sebagai "belum keluar", bukan
     * dicetak apa adanya.</p>
     */
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
        muatMasukKeluar();
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
        // tgl_keluar '0000-00-00' berarti belum keluar - jangan ditampilkan.
        TKeluarTgl.setText(tanpaTanggalNol(Sequel.cariIsi(
                "select date_format(tgl_keluar,'%d-%m-%Y') from kamar_inap "
                + "where no_rawat=? and tgl_keluar<>'0000-00-00' order by tgl_keluar desc limit 1", TNoRw.getText())));
        TKeluarJam.setText(TKeluarTgl.getText().isEmpty() ? "" : teksAman(Sequel.cariIsi(
                "select jam_keluar from kamar_inap "
                + "where no_rawat=? and tgl_keluar<>'0000-00-00' order by tgl_keluar desc limit 1", TNoRw.getText())));
    }

    private String tanpaTanggalNol(String s) {
        if (s == null || s.trim().isEmpty() || s.startsWith("00-00-0000")) {
            return "";
        }
        return s;
    }

    /**
     * Mengisi bagian yang BOLEH diedit dengan saran dari data yang sudah ada.
     *
     * <p>Dipanggil hanya saat lembar BARU (belum ada record tersimpan) - kalau
     * record sudah ada, {@link #isiDariRecord()} yang menang, karena nilai yang
     * sudah dikoreksi petugas tidak boleh ditimpa saran sistem.</p>
     */
    private void praIsiDariSistem() {
        String noRawat = TNoRw.getText();

        // Masuk melalui: satu-satunya petunjuk yang bisa dipercaya adalah poli
        // registrasi. IGDK = IGD, selain itu dianggap Poliklinik.
        String kdPoli = teksAman(Sequel.cariIsi(
                "select kd_poli from reg_periksa where no_rawat=?", noRawat));
        if (!kdPoli.isEmpty()) {
            MasukMelalui.setSelectedItem(KD_POLI_IGD.equals(kdPoli) ? "IGD" : "Poliklinik");
        }

        // Diagnosa & alergi: resume ranap lebih rinci, kamar_inap jadi cadangan.
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
        // Diagnose Lain = seluruh diagnosa sekunder yang terisi, digabung.
        DiagnoseLain.setText(teksAman(Sequel.cariIsi(
                "select concat_ws(', ',nullif(diagnosa_sekunder,''),nullif(diagnosa_sekunder2,''),"
                + "nullif(diagnosa_sekunder3,''),nullif(diagnosa_sekunder4,'')) "
                + "from resume_pasien_ranap where no_rawat=?", noRawat)));

        praIsiTindakan(noRawat);
        praIsiKeluar(noRawat);

        // Dokter yang merawat: DPJP ranap, cadangan dokter registrasi.
        String kdDokter = pertamaTerisi(
                Sequel.cariIsi("select kd_dokter from dpjp_ranap where no_rawat=? limit 1", noRawat),
                Sequel.cariIsi("select kd_dokter from reg_periksa where no_rawat=?", noRawat));
        if (!kdDokter.isEmpty()) {
            KdDokterMerawat.setText(kdDokter);
            NmDokterMerawat.setText(Sequel.CariDokter(kdDokter));
            TNipDokter.setText(nipDokter(kdDokter, NmDokterMerawat.getText()));
        }
    }

    /**
     * Baris tindakan diisi dari modul operasi (yang punya tanggal &amp; dokter),
     * lalu sisanya dari prosedur_pasien (yang hanya punya kode tindakan).
     */
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
                    if (tgl != null && !tgl.isEmpty()) {
                        Valid.SetTgl2(TindakanTgl[slot], tgl);
                    }
                    String op = teksAman(rs.getString("operator1"));
                    TindakanKdDokter[slot].setText(op);
                    TindakanNmDokter[slot].setText(op.isEmpty() ? "" : Sequel.CariDokter(op));
                    slot++;
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
     * Menyarankan Keadaan Keluar, status meninggal, dan kunjungan ulang.
     *
     * <p>Pemetaan Keadaan Keluar hanya SARAN. Kertas punya lima pilihan,
     * sedangkan sistem punya tiga sumber dengan kosakata berbeda dan tidak satu
     * pun cocok persis - jadi hasil pemetaan di sini tetap harus diperiksa
     * petugas.</p>
     */
    private void praIsiKeluar(String noRawat) {
        String stts = teksAman(Sequel.cariIsi(
                "select stts_pulang from kamar_inap where no_rawat=? and stts_pulang<>'Pindah Kamar' "
                + "order by tgl_masuk desc limit 1", noRawat));
        String keadaan = teksAman(Sequel.cariIsi(
                "select keadaan from resume_pasien_ranap where no_rawat=?", noRawat));
        String caraKeluar = teksAman(Sequel.cariIsi(
                "select cara_keluar from resume_pasien_ranap where no_rawat=?", noRawat));

        String saran = "";
        if ("Meninggal".equalsIgnoreCase(stts) || "Meninggal".equalsIgnoreCase(keadaan)) {
            ChkMeninggal.setSelected(true);
            perbaruiVisibilitasKematian();
        } else if ("Sembuh".equalsIgnoreCase(stts) || "Sehat".equalsIgnoreCase(stts)
                || "Sembuh".equalsIgnoreCase(keadaan)) {
            saran = "Sembuh";
        } else if ("Membaik".equalsIgnoreCase(stts) || "Membaik".equalsIgnoreCase(keadaan)) {
            saran = "Berobat Jalan";
        } else if ("APS".equalsIgnoreCase(stts) || "Pulang Paksa".equalsIgnoreCase(stts)
                || "Atas Permintaan Sendiri".equalsIgnoreCase(stts)
                || "Pulang Atas Permintaan Sendiri".equalsIgnoreCase(caraKeluar)) {
            saran = "Pulang Paksa";
        } else if ("Pindah RS".equalsIgnoreCase(caraKeluar)) {
            saran = "Pindah RS";
        } else if ("Rujuk".equalsIgnoreCase(stts)) {
            saran = "Rujuk";
        }
        if (!saran.isEmpty()) {
            KeadaanKeluar.setSelectedItem(saran);
        }

        if (ChkMeninggal.isSelected()) {
            // Meninggal sebelum/sesudah 48 jam bisa dihitung dari lama dirawat.
            String jam = Sequel.cariIsi(
                    "select timestampdiff(hour,concat(min(tgl_masuk),' ',min(jam_masuk)),"
                    + "concat(max(tgl_keluar),' ',max(jam_keluar))) from kamar_inap "
                    + "where no_rawat=? and tgl_keluar<>'0000-00-00'", noRawat);
            if (jam != null && !jam.trim().isEmpty()) {
                try {
                    CaraKeluarMeninggal.setSelectedItem(
                            Integer.parseInt(jam.trim()) < 48 ? "Sebelum 48 Jam" : "Sesudah 48 Jam");
                } catch (NumberFormatException ex) {
                    // Selisih tidak bisa dihitung (tanggal keluar belum ada) -
                    // biarkan petugas yang memilih.
                }
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
            ChkKunjunganUlang.setSelected(true);
            Valid.SetTgl2(KunjunganUlangTgl, kontrol);
        }
    }

    private String pertamaTerisi(String... calon) {
        for (String c : calon) {
            if (c != null && !c.trim().isEmpty()) {
                return c;
            }
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
        barisHtml(h, "Dikirim Oleh", String.valueOf(DikirimOleh.getSelectedItem()));
        barisHtml(h, "Kasus Polisi", teksAman(pilihan(bgKasusPolisi)));
        barisHtml(h, "Keluar", TKeluarTgl.getText() + " " + TKeluarJam.getText());
        barisHtml(h, "Keadaan Keluar", String.valueOf(KeadaanKeluar.getSelectedItem()));
        barisHtml(h, "Rujuk ke", RujukKe.getText());

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

        if (ChkMeninggal.isSelected()) {
            judulHtml(h, "SEBAB KEMATIAN");
            barisHtml(h, "Meninggal", String.valueOf(MeninggalTgl.getSelectedItem()));
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
        if (mode == null) {
            return;
        }
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

    /** Teks atestasi yang di-encode jadi QR pada blok tanda tangan cetakan. */
    private String tandaTangan(String kdDokter, String namaDokter) {
        String finger = Sequel.cariIsi(
                "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id "
                + "where pegawai.nik=?", kdDokter);
        return "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                + "\nDitandatangani secara elektronik oleh " + namaDokter
                + "\nID " + (finger == null || finger.isEmpty() ? kdDokter : finger);
    }

    /**
     * Query cetak. Identitas &amp; data registrasi ikut di-JOIN di sini (tidak
     * disimpan di tabel form), termasuk blok MASUK dari baris kamar_inap paling
     * awal dan blok KELUAR dari baris paling akhir yang tanggalnya sah.
     */
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
    //  API dipakai pemanggil (frmUtama / DlgKamarInap)
    // =================================================================

    /**
     * Dipanggil pemanggil untuk membuka lembar satu pasien.
     *
     * <p>Kalau pasien itu SUDAH punya record, isinya dimuat apa adanya. Kalau
     * belum, seluruh bagian yang bisa ditebak diisi otomatis dari data yang
     * sudah ada di sistem - permintaan eksplisit user.</p>
     */
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

    public void isCek() {
        BtnSimpan.setEnabled(akses.getringkasan_pasien_masuk_keluar());
        BtnHapus.setEnabled(akses.getringkasan_pasien_masuk_keluar());
        BtnEdit.setEnabled(akses.getringkasan_pasien_masuk_keluar());
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
