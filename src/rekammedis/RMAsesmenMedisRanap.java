package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
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
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
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
import kepegawaian.DlgCariDokter;
import widget.ThemeConfig;

/**
 * ASESMEN MEDIS RAWAT INAP (RM 20) - RSUD Demang Sepulau Raya.
 *
 * <p>Form ini <b>TAMBAHAN</b>, bukan pengganti. Form lama
 * {@link RMPenilaianAwalMedisRanapDewasa} ("Awal Medis Ranap Umum") tetap utuh
 * dan tetap muncul di menu - keputusan eksplisit user (Skenario A). Keduanya
 * memakai tabel yang BERBEDA (`asesmen_medis_ranap` vs `penilaian_medis_ranap`)
 * karena PK kedua tabel sama-sama `no_rawat` saja; kalau berbagi tabel, kedua
 * form akan saling menimpa baris yang sama. Konsekuensi yang disadari: kedua
 * form TIDAK berbagi data.</p>
 *
 * <p><b>Kenapa file ini ditulis tangan tanpa {@code .form}/blok GEN.</b>
 * Keberantakan form-form lama di proyek ini berakar pada {@code setLayout(null)}
 * + {@code setBounds()} koordinat piksel absolut di dalam blok GEN yang tidak
 * boleh diedit tangan - menggeser satu field berarti menghitung ulang semua
 * yang di bawahnya, dan sudah beberapa kali menimbulkan bug tumpang tindih
 * (lihat riwayat RM 19 di PROGRESS.md). Di sini seluruh tata letak memakai
 * {@link GridBagLayout}, jadi field menata diri sendiri, ikut melebar saat
 * window di-resize, dan tumpang tindih secara struktural tidak mungkin terjadi.
 * Konsekuensinya: file ini TIDAK bisa dibuka di NetBeans Form Editor - semua
 * perubahan tata letak lewat kode. Preseden hand-written di proyek ini:
 * {@code DlgCariNamaObat.java}, {@code DlgCariKamar.java}.</p>
 *
 * <p><b>Gotcha widget yang sudah terdokumentasi dan dipatuhi di sini:</b></p>
 * <ul>
 *   <li>{@code widget.TextBox}/{@code ComboBox}/{@code Tanggal} constructor-nya
 *       diam-diam memanggil {@code setSize(WIDTH,23)} dengan {@code WIDTH}=1
 *       (warisan {@code ImageObserver.WIDTH}, bukan lebar piksel). Tanpa
 *       {@code setPreferredSize()} eksplisit field akan menciut ~1px dan tidak
 *       bisa diklik. Semua field di file ini memakai helper yang selalu
 *       memasang preferred size.</li>
 *   <li>{@code widget.Label} defaultnya rata KANAN - judul section dipaksa
 *       {@code LEFT} eksplisit.</li>
 *   <li>{@code widget.Tanggal} yang dibangun tangan TIDAK otomatis dapat
 *       {@code setDisplayFormat(...)} seperti versi Form Editor; tanpa itu
 *       {@code getSelectedItem().toString()} formatnya acak dan
 *       {@code substring(11,19)} akan melempar StringIndexOutOfBounds.</li>
 * </ul>
 *
 * @see #tambahPengisiKolom(javax.swing.JPanel, int) catatan bug centering GridBagLayout
 */
public final class RMAsesmenMedisRanap extends javax.swing.JDialog {

    /**
     * Nama kolom tabel `asesmen_medis_ranap` di luar `no_rawat`, dipakai BERSAMA
     * oleh {@link #simpan()} (INSERT) dan {@link #ganti()} (UPDATE) supaya
     * urutan kolom dan urutan nilai tidak pernah bisa desinkron - satu daftar,
     * dua pemakai. Pola ini disalin dari {@code KOLOM_RM032} di
     * {@code RMPenilaianAwalMedisIGD.java}.
     *
     * <p>Urutannya WAJIB sama persis dengan urutan nilai yang dikembalikan
     * {@link #nilaiRm20()}.</p>
     */
    private static final String[] KOLOM_RM20 = {
        "tanggal","kd_dokter_periksa","kd_dokter_dpjp",
        "keluhan_utama","keluhan_tambahan","alergi","sumber_riwayat","riwayat_penyakit_pengobatan",
        "keadaan_umum","kesadaran_mental","gcs_e","gcs_m","gcs_v","td_sistol","td_diastol",
        "nadi","rr","suhu","tb","bb","umur","imt","status_pediatrik",
        "diagnosa_kerja_1","diagnosa_kerja_2","diagnosa_kerja_3","rencana_terapi"
    };

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

    // ---------- Kerangka ----------
    private widget.InternalFrame internalFrame1, internalFrame2, internalFrame3;
    private widget.panelisi panelGlass8, panelGlass9;
    private javax.swing.JTabbedPane TabRawat;
    private widget.ScrollPane scrollInput;
    private widget.PanelBiasa FormInput;
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnPrint, BtnAll, BtnKeluar;

    // ---------- Header ----------
    private widget.TextBox TNoRw, TNoRM, TPasien, TglLahir, TUmur, Jk;
    private widget.Tanggal TglAsuhan;
    private widget.TextBox KdDokterPeriksa, NmDokterPeriksa, KdDokterDpjp, NmDokterDpjp;
    private widget.Button BtnDokterPeriksa, BtnDokterDpjp;

    // ---------- I. Anamnesa ----------
    private widget.TextArea KeluhanUtama, KeluhanTambahan, RiwayatPenyakitPengobatan;
    private widget.TextBox Alergi;
    private widget.RadioButton RbSumberSendiri, RbSumberKeluarga;
    private final ButtonGroup bgSumberRiwayat = new ButtonGroup();

    // ---------- II. Pemeriksaan Medis ----------
    private widget.ComboBox KeadaanUmum, KesadaranMental;
    private widget.TextBox GcsE, GcsM, GcsV, TdSistol, TdDiastol, Nadi, RR, Suhu, TB, BB, TImt;
    private widget.RadioButton RbGiziKurang, RbGiziBaik, RbGiziLebih;
    private final ButtonGroup bgStatusPediatrik = new ButtonGroup();
    private widget.Label LblCatatanGizi;

    // ---------- Diagnosa & Rencana ----------
    private widget.TextBox DiagnosaKerja1, DiagnosaKerja2, DiagnosaKerja3;
    private widget.TextArea RencanaTerapi;

    // ---------- Tab Data ----------
    private widget.ScrollPane Scroll, ScrollHTML;
    private widget.Table tbObat;
    private widget.Tanggal DTPCari1, DTPCari2;
    private widget.TextBox TCari, TNoRM1, TPasien1;
    private widget.Button BtnCari, BtnPrint1;
    private widget.Label LCount;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JMenuItem MnAsesmenMedis;
    private widget.PanelBiasa PanelAccor;
    private widget.panelisi FormMenu;
    private widget.CekBox ChkAccor;
    private widget.editorpane LoadHTML;

    public RMAsesmenMedisRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat","No.RM","Nama Pasien","Tgl.Lahir","J.K.",
            "Kode Dokter","Dokter Pemeriksa","DPJP","Tanggal","Diagnosa Kerja"
        }) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
        };

        rakitKerangka();
        rakitHeader();
        rakitBadan();
        rakitTabData();
        pasangBatasInput();
        pasangHitungOtomatis();

        widget.IconStyle.pasang(this);
        bersihkanIkonRadio(getContentPane());
        // Accordion ringkasan mulai dalam keadaan terlipat - isinya baru relevan
        // setelah ada baris pasien yang diklik (pelajaran dari RM 03-2 poin 19:
        // panel yang terbuka duluan tapi kosong justru mengganggu).
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
                "::[ Asesmen Medis Rawat Inap (RM 20) ]::"));
        internalFrame1.setFont(new java.awt.Font(ThemeConfig.FONT_FAMILY, java.awt.Font.ITALIC, 12));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        // ---- Footer: disamakan dengan form lain (Simpan/Baru/Hapus/Ganti/Cetak/Semua/Keluar)
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

        // ---- Dua tab, sama seperti form sejenis
        TabRawat = new javax.swing.JTabbedPane();
        TabRawat.setFont(new java.awt.Font(ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));

        internalFrame2 = new widget.InternalFrame();
        internalFrame2.setBorder(null);
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput = new widget.PanelBiasa();
        FormInput.setLayout(new GridBagLayout());
        // Sengaja TIDAK ada setPreferredSize di sini: dengan GridBagLayout,
        // ukuran dihitung sendiri dari isinya. Ini yang membuat form tidak
        // pernah menyisakan celah kosong raksasa seperti form ber-null-layout,
        // dan tidak perlu dihitung ulang tiap kali ada field ditambah.

        scrollInput = new widget.ScrollPane();
        scrollInput.setViewportView(FormInput);
        scrollInput.getVerticalScrollBar().setUnitIncrement(16);
        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);
        TabRawat.addTab("Input Asesmen", internalFrame2);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
    }

    /**
     * Mengembalikan ikon bawaan pada semua radio button di dalam form.
     *
     * <p>{@code widget.IconStyle.pasang(this)} menelusuri SEMUA
     * {@code AbstractButton} dan memasang ikon SVG berdasarkan KATA KUNCI pada
     * teksnya. {@code javax.swing.JRadioButton} ikut turunan
     * {@code AbstractButton}, jadi ikut kena - dan teks radio bisa tanpa sengaja
     * mengandung kata kunci. Kasus nyata di form ini: radio <b>"Keluarga"</b>
     * mengandung "keluar" sehingga diberi ikon log-out, terlihat seperti tombol
     * keluar padahal itu pilihan sumber riwayat. Risiko ini sudah diantisipasi
     * saat IconStyle dipasang massal (lihat catatan di PROGRESS.md poin 21).
     *
     * <p>{@code setIcon(null)} pada JRadioButton mengembalikan lingkaran radio
     * bawaan Look&amp;Feel. {@code JCheckBox} SENGAJA tidak ikut dibersihkan -
     * {@code ChkAccor} (toggle accordion) memang memerlukan ikon panah
     * kiri/kanan-nya.
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
            // Ikon raster lama memang disembunyikan otomatis oleh widget.Button
            // kalau tombol punya teks; IconStyle.pasang() yang mengisi ikon SVG
            // sesungguhnya. Jadi kegagalan di sini tidak fatal.
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
        // fill = NONE adalah DEFAULT yang disengaja (bukan HORIZONTAL).
        // Pelajaran Fase 4: helper sejenis di RMPenilaianAwalKeperawatanIGD
        // awalnya fill=HORIZONTAL, akibatnya dropdown & kotak sempit ikut
        // melebar mengikuti kolom terlebar sehingga tampak kosong-melompong.
        // fill=HORIZONTAL hanya dipasang manual pada textarea yang memang
        // harus melebar.
        c.fill = GridBagConstraints.NONE;
        return c;
    }

    /**
     * Penawar bug baku {@link GridBagLayout}: kalau SEMUA komponen dalam satu
     * container punya {@code weightx=0} dan container itu diberi lebih banyak
     * lebar daripada total preferred width isinya, GridBagLayout MEN-TENGAH-KAN
     * seluruh grid, bukan merapatkannya ke kiri - inilah akar bug "label &amp;
     * field mengambang di tengah" yang pernah dilaporkan di tab PACS (Fase 3).
     *
     * <p>Filler tak-terlihat diletakkan di kolom paling kanan dengan
     * {@code weightx=1.0} supaya SEMUA sisa lebar diserap di situ - bukan oleh
     * label/field sungguhan. Hasilnya kolom label &amp; kolom input tetap di
     * lebar aslinya dan rata kiri.</p>
     *
     * <p>Panel yang sudah punya minimal satu baris ber-{@code weightx=1.0}
     * (mis. panel yang berisi textarea melebar) TIDAK perlu filler ini.</p>
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

    /** Label kolom kiri, rata KANAN supaya semua titik dua sejajar satu garis. */
    private widget.Label labelKolom(String teks) {
        widget.Label l = new widget.Label();
        l.setText(teks + " :");
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    /** Label bebas rata kiri (satuan, keterangan, dsb). */
    private widget.Label teks(String isi) {
        widget.Label l = new widget.Label();
        l.setText(isi);
        // widget.Label defaultnya rata KANAN - dipaksa kiri, kalau tidak
        // satuan seperti "mmHg"/"x/mnt" akan mengambang jauh dari fieldnya.
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    /**
     * Merangkai beberapa komponen pendek menjadi SATU baris.
     *
     * <p>Ini kunci permintaan "kalau inputannya sedikit field jangan terlalu
     * besar": field pendek yang saling berhubungan (GCS E/M/V, Tensi sistol/
     * diastol, Nadi/RR/Suhu) dijejer dalam satu sel GridBag, bukan menempati
     * satu baris masing-masing. Blok Pemeriksaan Medis jadi 6 baris padat,
     * bukan 12 baris kurus.</p>
     */
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
        // WIDTH=1 (ImageObserver.WIDTH). Tanpa baris ini field menciut ~1px.
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

    /** TextArea dibungkus ScrollPane, ukuran ditentukan pemanggil. */
    private widget.ScrollPane areaTeks(widget.TextArea area, int baris, int lebar, int tinggi) {
        area.setColumns(20);
        area.setRows(baris);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        widget.ScrollPane sp = new widget.ScrollPane();
        sp.setViewportView(area);
        // widget.ScrollPane sebenarnya sudah punya LineBorder, tapi warnanya
        // hardcode krem (239,244,234) yang praktis tak terlihat di atas latar
        // putih - ditimpa warna Design System supaya kotaknya terbaca.
        sp.setBorder(javax.swing.BorderFactory.createLineBorder(ThemeConfig.BORDER));
        sp.setPreferredSize(new Dimension(lebar, tinggi));
        return sp;
    }

    // =================================================================
    //  HEADER  (susunan disamakan dengan form lain: identitas di atas)
    // =================================================================

    private void rakitHeader() {
        widget.PanelBiasa panelHeader = new widget.PanelBiasa();
        panelHeader.setLayout(new GridBagLayout());

        TNoRw = kotak(140);
        TNoRw.setHighlighter(null);
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                    isRawat();
                }
            }
        });
        TNoRM = kotakBaca(100);
        TPasien = kotakBaca(240);
        TglLahir = kotakBaca(90);
        TUmur = kotakBaca(90);
        Jk = kotakBaca(85);

        TglAsuhan = new widget.Tanggal();
        // WAJIB untuk widget.Tanggal yang dibangun tangan - versi Form Editor
        // mendapat baris ini otomatis, versi tangan tidak. Tanpa ini
        // getSelectedItem().toString() formatnya acak dan substring(11,19)
        // di nilaiRm20() akan melempar StringIndexOutOfBoundsException.
        TglAsuhan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TglAsuhan.setDate(new Date());
        TglAsuhan.setPreferredSize(new Dimension(150, 23));

        KdDokterPeriksa = kotakBaca(90);
        NmDokterPeriksa = kotakBaca(190);
        BtnDokterPeriksa = new widget.Button();
        BtnDokterPeriksa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnDokterPeriksa.setPreferredSize(new Dimension(28, 23));
        BtnDokterPeriksa.setToolTipText("Cari dokter yang memeriksa");
        BtnDokterPeriksa.addActionListener(evt -> bukaCariDokter(false));

        KdDokterDpjp = kotakBaca(90);
        NmDokterDpjp = kotakBaca(190);
        BtnDokterDpjp = new widget.Button();
        BtnDokterDpjp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnDokterDpjp.setPreferredSize(new Dimension(28, 23));
        BtnDokterDpjp.setToolTipText("Cari dokter penanggung jawab pelayanan");
        BtnDokterDpjp.addActionListener(evt -> bukaCariDokter(true));

        int row = 0;
        GridBagConstraints c = konstrain(0, row);
        panelHeader.add(labelKolom("No.Rawat"), c);
        c = konstrain(1, row);
        panelHeader.add(baris(TNoRw, TNoRM, TPasien,
                teks("   Tgl.Lahir :"), TglLahir,
                teks("   Umur :"), TUmur,
                teks("   J.K. :"), Jk), c);
        row++;

        c = konstrain(0, row);
        panelHeader.add(labelKolom("Tanggal"), c);
        c = konstrain(1, row);
        panelHeader.add(baris(TglAsuhan,
                teks("   Dokter Pemeriksa :"), KdDokterPeriksa, NmDokterPeriksa, BtnDokterPeriksa), c);
        row++;

        c = konstrain(0, row);
        panelHeader.add(labelKolom("DPJP"), c);
        c = konstrain(1, row);
        panelHeader.add(baris(KdDokterDpjp, NmDokterDpjp, BtnDokterDpjp,
                teks("   (Dokter Penanggung Jawab Pelayanan)")), c);

        tambahPengisiKolom(panelHeader, 2);

        GridBagConstraints ch = konstrain(0, 0);
        ch.gridwidth = 2;
        ch.fill = GridBagConstraints.HORIZONTAL;
        ch.weightx = 1.0;
        FormInput.add(panelHeader, ch);
    }

    // =================================================================
    //  BADAN - dua kolom, meniru susunan lembar RM 20
    //  (Rencana & Terapi memang berada di sisi kanan pada kertas)
    // =================================================================

    private void rakitBadan() {
        // ---------------- I. ANAMNESA ----------------
        widget.PanelBiasa panelAnamnesa = panelSeksi("I. ANAMNESA");
        KeluhanUtama = new widget.TextArea();
        KeluhanTambahan = new widget.TextArea();
        RiwayatPenyakitPengobatan = new widget.TextArea();
        Alergi = kotak(250);
        RbSumberSendiri = radio("Sendiri", bgSumberRiwayat);
        RbSumberKeluarga = radio("Keluarga", bgSumberRiwayat);

        int row = 0;
        panelAnamnesa.add(labelKolom("Keluhan Utama"), konstrain(0, row));
        GridBagConstraints c = konstrain(1, row);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panelAnamnesa.add(areaTeks(KeluhanUtama, 3, 420, 55), c);
        row++;

        panelAnamnesa.add(labelKolom("Keluhan Tambahan"), konstrain(0, row));
        c = konstrain(1, row);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panelAnamnesa.add(areaTeks(KeluhanTambahan, 3, 420, 55), c);
        row++;

        panelAnamnesa.add(labelKolom("Alergi"), konstrain(0, row));
        panelAnamnesa.add(baris(Alergi), konstrain(1, row));
        row++;

        panelAnamnesa.add(labelKolom("Riwayat Perjalanan Penyakit"), konstrain(0, row));
        panelAnamnesa.add(baris(teks("& Riwayat Pengobatan, diperoleh dari :"), RbSumberSendiri, RbSumberKeluarga),
                konstrain(1, row));
        row++;

        c = konstrain(1, row);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panelAnamnesa.add(areaTeks(RiwayatPenyakitPengobatan, 5, 420, 90), c);
        // Panel ini sudah punya baris ber-weightx=1.0 (textarea), jadi TIDAK
        // perlu tambahPengisiKolom() - grid otomatis merapat ke kiri.

        // ---------------- II. PEMERIKSAAN MEDIS ----------------
        widget.PanelBiasa panelPemeriksaan = panelSeksi("II. PEMERIKSAAN MEDIS");
        KeadaanUmum = combo(new String[]{"Sehat", "Sakit Ringan", "Sakit Sedang", "Sakit Berat"}, 140);
        KesadaranMental = combo(new String[]{"Compos Mentis", "Apatis", "Somnolen", "Sopor", "Koma"}, 140);
        // Lebar field ditentukan dari isi wajarnya, bukan dari ruang tersedia.
        GcsE = kotak(35);
        GcsM = kotak(35);
        GcsV = kotak(35);
        TdSistol = kotak(45);
        TdDiastol = kotak(45);
        Nadi = kotak(45);
        RR = kotak(45);
        Suhu = kotak(45);
        TB = kotak(50);
        BB = kotak(50);
        TImt = kotakBaca(60);
        RbGiziKurang = radio("Gizi Kurang", bgStatusPediatrik);
        RbGiziBaik = radio("Baik", bgStatusPediatrik);
        RbGiziLebih = radio("Gizi Lebih", bgStatusPediatrik);
        LblCatatanGizi = teks("");
        LblCatatanGizi.setForeground(ThemeConfig.TEXT_MUTED);

        row = 0;
        panelPemeriksaan.add(labelKolom("Keadaan Umum"), konstrain(0, row));
        panelPemeriksaan.add(baris(KeadaanUmum,
                teks("   Kesadaran Mental :"), KesadaranMental), konstrain(1, row));
        row++;

        // Kertas menulis "GSC : E ..... M ...... V ......" - "GSC" itu typo lazim
        // dari GCS; urutan E-M-V mengikuti kertas (form lama memakai E,V,M).
        panelPemeriksaan.add(labelKolom("GCS"), konstrain(0, row));
        panelPemeriksaan.add(baris(teks("E"), GcsE, teks("M"), GcsM, teks("V"), GcsV), konstrain(1, row));
        row++;

        panelPemeriksaan.add(labelKolom("Tensi"), konstrain(0, row));
        panelPemeriksaan.add(baris(TdSistol, teks("/"), TdDiastol, teks("mmHg")), konstrain(1, row));
        row++;

        panelPemeriksaan.add(labelKolom("Nadi"), konstrain(0, row));
        panelPemeriksaan.add(baris(Nadi, teks("x/mnt"),
                teks("   RR :"), RR, teks("x/mnt"),
                teks("   Suhu :"), Suhu, teks("°C")), konstrain(1, row));
        row++;

        panelPemeriksaan.add(labelKolom("TB"), konstrain(0, row));
        panelPemeriksaan.add(baris(TB, teks("cm"),
                teks("   BB :"), BB, teks("kg"),
                teks("   IMT :"), TImt), konstrain(1, row));
        row++;

        panelPemeriksaan.add(labelKolom("Status Pediatrik"), konstrain(0, row));
        panelPemeriksaan.add(baris(RbGiziKurang, RbGiziBaik, RbGiziLebih), konstrain(1, row));
        row++;

        GridBagConstraints cg = konstrain(1, row);
        panelPemeriksaan.add(LblCatatanGizi, cg);
        // Panel ini isinya field pendek semua (tidak ada yang weightx=1.0),
        // jadi WAJIB pakai filler supaya tidak ter-tengah.
        tambahPengisiKolom(panelPemeriksaan, 2);

        // ---------------- DIAGNOSA KERJA ----------------
        widget.PanelBiasa panelDiagnosa = panelSeksi("DIAGNOSA KERJA");
        DiagnosaKerja1 = kotak(400);
        DiagnosaKerja2 = kotak(400);
        DiagnosaKerja3 = kotak(400);
        widget.TextBox[] diag = {DiagnosaKerja1, DiagnosaKerja2, DiagnosaKerja3};
        for (int d = 0; d < diag.length; d++) {
            panelDiagnosa.add(labelKolom(String.valueOf(d + 1)), konstrain(0, d));
            GridBagConstraints cd = konstrain(1, d);
            cd.fill = GridBagConstraints.HORIZONTAL;
            cd.weightx = 1.0;
            panelDiagnosa.add(diag[d], cd);
        }

        // ---------------- RENCANA DAN TERAPI (kolom kanan) ----------------
        widget.PanelBiasa panelRencana = panelSeksi("RENCANA DAN TERAPI");
        RencanaTerapi = new widget.TextArea();
        GridBagConstraints cr = konstrain(0, 0);
        cr.fill = GridBagConstraints.HORIZONTAL;
        cr.weightx = 1.0;
        panelRencana.add(teks("(Pemeriksaan Penunjang & Pengobatannya)"), cr);
        cr = konstrain(0, 1);
        cr.fill = GridBagConstraints.BOTH;
        cr.weightx = 1.0;
        cr.weighty = 1.0;
        panelRencana.add(areaTeks(RencanaTerapi, 20, 380, 360), cr);

        // ---------------- Susun dua kolom ----------------
        GridBagConstraints c1 = konstrain(0, 1);
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.weightx = 0.6;
        FormInput.add(panelAnamnesa, c1);

        c1 = konstrain(0, 2);
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.weightx = 0.6;
        FormInput.add(panelPemeriksaan, c1);

        c1 = konstrain(0, 3);
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.weightx = 0.6;
        FormInput.add(panelDiagnosa, c1);

        GridBagConstraints c2 = konstrain(1, 1);
        c2.gridheight = 3;
        c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 0.4;
        c2.weighty = 1.0;
        FormInput.add(panelRencana, c2);
    }

    private void pasangBatasInput() {
        TNoRw.setDocument(new batasInput((byte) 17).getKata(TNoRw));
        KeluhanUtama.setDocument(new batasInput((int) 2000).getKata(KeluhanUtama));
        KeluhanTambahan.setDocument(new batasInput((int) 2000).getKata(KeluhanTambahan));
        RiwayatPenyakitPengobatan.setDocument(new batasInput((int) 4000).getKata(RiwayatPenyakitPengobatan));
        Alergi.setDocument(new batasInput((int) 200).getKata(Alergi));
        GcsE.setDocument(new batasInput((byte) 2).getKata(GcsE));
        GcsM.setDocument(new batasInput((byte) 2).getKata(GcsM));
        GcsV.setDocument(new batasInput((byte) 2).getKata(GcsV));
        TdSistol.setDocument(new batasInput((byte) 4).getKata(TdSistol));
        TdDiastol.setDocument(new batasInput((byte) 4).getKata(TdDiastol));
        Nadi.setDocument(new batasInput((byte) 5).getKata(Nadi));
        RR.setDocument(new batasInput((byte) 5).getKata(RR));
        Suhu.setDocument(new batasInput((byte) 5).getKata(Suhu));
        TB.setDocument(new batasInput((byte) 5).getKata(TB));
        BB.setDocument(new batasInput((byte) 5).getKata(BB));
        DiagnosaKerja1.setDocument(new batasInput((int) 200).getKata(DiagnosaKerja1));
        DiagnosaKerja2.setDocument(new batasInput((int) 200).getKata(DiagnosaKerja2));
        DiagnosaKerja3.setDocument(new batasInput((int) 200).getKata(DiagnosaKerja3));
        RencanaTerapi.setDocument(new batasInput((int) 5000).getKata(RencanaTerapi));
        TCari.setDocument(new batasInput((int) 100).getKata(TCari));
    }

    // =================================================================
    //  HITUNG OTOMATIS (Umur, IMT, saran Status Pediatrik)
    // =================================================================

    private void pasangHitungOtomatis() {
        DocumentListener pemicu = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { hitungImt(); }
            @Override public void removeUpdate(DocumentEvent e) { hitungImt(); }
            @Override public void changedUpdate(DocumentEvent e) { hitungImt(); }
        };
        TB.getDocument().addDocumentListener(pemicu);
        BB.getDocument().addDocumentListener(pemicu);
    }

    /**
     * Umur dihitung dari tanggal lahir pasien terhadap TANGGAL ASESMEN (bukan
     * hari ini), supaya cetakan ulang bertahun kemudian tetap menampilkan umur
     * yang benar saat asesmen dilakukan.
     *
     * <p>Formatnya tahun + bulan, bukan tahun saja: form rawat inap bisa kena
     * pasien bayi, dan {@code TIMESTAMPDIFF(YEAR,...)} akan menampilkan "0"
     * untuk bayi di bawah 1 tahun.</p>
     */
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
                return p.getYears() + " Th " + p.getMonths() + " Bl";
            }
            if (p.getMonths() > 0) {
                return p.getMonths() + " Bl " + p.getDays() + " Hr";
            }
            return p.getDays() + " Hr";
        } catch (Exception e) {
            return "";
        }
    }

    /** Umur dalam tahun penuh; -1 kalau tanggal lahir tidak terbaca. */
    private int umurTahun() {
        try {
            String lahir = TglLahir.getText().trim();
            if (lahir.isEmpty()) {
                return -1;
            }
            LocalDate tglLahir = LocalDate.parse(lahir.substring(0, 10));
            return Period.between(tglLahir, LocalDate.now()).getYears();
        } catch (Exception e) {
            return -1;
        }
    }

    private void perbaruiUmur() {
        TUmur.setText(hitungUmur());
        sarankanStatusPediatrik();
    }

    private void hitungImt() {
        try {
            double tb = Double.parseDouble(TB.getText().trim().replace(',', '.'));
            double bb = Double.parseDouble(BB.getText().trim().replace(',', '.'));
            if (tb <= 0 || bb <= 0) {
                TImt.setText("");
                LblCatatanGizi.setText("");
                return;
            }
            double imt = bb / Math.pow(tb / 100.0, 2);
            TImt.setText(String.format(java.util.Locale.US, "%.1f", imt).replace('.', ','));
        } catch (Exception e) {
            TImt.setText("");
        }
        sarankanStatusPediatrik();
    }

    /**
     * Mengisi radio Status Pediatrik SEBAGAI SARAN - dokter selalu bisa
     * mengubahnya (radio tidak pernah dikunci).
     *
     * <p><b>Untuk pasien anak (&lt;18 th) saran SENGAJA TIDAK diberikan.</b>
     * Ambang IMT dewasa (18,5 / 25) salah secara klinis kalau dipakai untuk
     * anak - standar WHO/Kemenkes memakai BB/TB menurut umur DAN jenis kelamin
     * (z-score), yang butuh tabel antropometri dan belum tersedia di database
     * ini. Memberi saran yang salah lebih berbahaya daripada tidak memberi
     * saran sama sekali, jadi untuk anak IMT tetap ditampilkan tapi keputusan
     * diserahkan penuh ke dokter. Kalau akurasi penuh untuk anak dibutuhkan,
     * itu pekerjaan terpisah (import tabel antropometri per umur x JK).</p>
     */
    private void sarankanStatusPediatrik() {
        String imtTeks = TImt.getText().trim();
        int umur = umurTahun();

        if (imtTeks.isEmpty()) {
            LblCatatanGizi.setText("");
            return;
        }
        if (umur >= 0 && umur < 18) {
            LblCatatanGizi.setText("IMT " + imtTeks + " - standar gizi anak perlu tabel antropometri WHO, "
                    + "silakan tentukan sendiri.");
            return;
        }
        try {
            double imt = Double.parseDouble(imtTeks.replace(',', '.'));
            if (imt < 18.5) {
                RbGiziKurang.setSelected(true);
            } else if (imt <= 25.0) {
                RbGiziBaik.setSelected(true);
            } else {
                RbGiziLebih.setSelected(true);
            }
            LblCatatanGizi.setText("Saran dari IMT " + imtTeks + " (dewasa) - bisa diubah dokter.");
        } catch (Exception e) {
            LblCatatanGizi.setText("");
        }
    }

    // =================================================================
    //  TAB DATA
    // =================================================================

    private void rakitTabData() {
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnAsesmenMedis = new javax.swing.JMenuItem();
        MnAsesmenMedis.setFont(new java.awt.Font(ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        MnAsesmenMedis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png")));
        MnAsesmenMedis.setText("Cetak Asesmen Medis Rawat Inap");
        MnAsesmenMedis.setPreferredSize(new Dimension(240, 26));
        MnAsesmenMedis.addActionListener(evt -> cetakTerpilih());
        jPopupMenu1.add(MnAsesmenMedis);

        internalFrame3 = new widget.InternalFrame();
        internalFrame3.setBorder(null);
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        tbObat = new widget.Table();
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        int[] lebar = {105, 70, 150, 75, 60, 80, 150, 150, 115, 250};
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

        // ---- Panel pencarian di bawah (pola sama Data Triase)
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

        // ---- Accordion ringkasan HTML di kanan (pola Triase/RM 03-2)
        LoadHTML = new widget.editorpane();
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(false);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right:1px solid #e2e7dd;font:8.5px tahoma;height:12px;"
                + "border-bottom:1px solid #e2e7dd;background:#ffffff;color:#323232;}"
                + ".isi2 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#323232;}");
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
    //  DIALOG DOKTER
    // =================================================================

    private void bukaCariDokter(boolean untukDpjp) {
        pilihDpjp = untukDpjp;
        if (dokter == null || !dokter.isDisplayable()) {
            dokter = new DlgCariDokter(null, false);
            dokter.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dokter.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if (dokter.getTable().getSelectedRow() != -1) {
                        String kd = dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString();
                        String nm = dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString();
                        if (pilihDpjp) {
                            KdDokterDpjp.setText(kd);
                            NmDokterDpjp.setText(nm);
                        } else {
                            KdDokterPeriksa.setText(kd);
                            NmDokterPeriksa.setText(nm);
                        }
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

    // =================================================================
    //  LAPISAN DATA
    // =================================================================

    /**
     * Nilai untuk SEMUA kolom di {@link #KOLOM_RM20}, urutannya WAJIB identik.
     * Dipakai bersama {@link #simpan()} dan {@link #ganti()}.
     */
    private String[] nilaiRm20() {
        String tanggal = Valid.SetTgl(TglAsuhan.getSelectedItem() + "") + " "
                + TglAsuhan.getSelectedItem().toString().substring(11, 19);
        return new String[]{
            tanggal, KdDokterPeriksa.getText(), KdDokterDpjp.getText(),
            KeluhanUtama.getText(), KeluhanTambahan.getText(), Alergi.getText(),
            pilihan(bgSumberRiwayat), RiwayatPenyakitPengobatan.getText(),
            KeadaanUmum.getSelectedItem().toString(), KesadaranMental.getSelectedItem().toString(),
            GcsE.getText(), GcsM.getText(), GcsV.getText(),
            TdSistol.getText(), TdDiastol.getText(),
            Nadi.getText(), RR.getText(), Suhu.getText(), TB.getText(), BB.getText(),
            TUmur.getText(), TImt.getText(), pilihan(bgStatusPediatrik),
            DiagnosaKerja1.getText(), DiagnosaKerja2.getText(), DiagnosaKerja3.getText(),
            RencanaTerapi.getText()
        };
    }

    /**
     * Teks radio yang terpilih, atau {@code null} kalau belum ada yang dipilih.
     * Teks tiap radio SENGAJA ditulis sama persis dengan nilai ENUM di database,
     * jadi tidak perlu tabel pemetaan terpisah yang bisa desinkron.
     */
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
        if (TNoRM.getText().trim().isEmpty()) {
            Valid.textKosong(TNoRw, "Nama Pasien");
        } else if (NmDokterPeriksa.getText().trim().isEmpty()) {
            Valid.textKosong(BtnDokterPeriksa, "Dokter Yang Memeriksa");
        } else if (NmDokterDpjp.getText().trim().isEmpty()) {
            Valid.textKosong(BtnDokterDpjp, "Dokter Penanggung Jawab Pelayanan");
        } else if (KeluhanUtama.getText().trim().isEmpty()) {
            Valid.textKosong(KeluhanUtama, "Keluhan Utama");
        } else {
            // INSERT bernama kolom (bukan Sequel.menyimpantf yang posisional) -
            // supaya penambahan kolom di kemudian hari tidak bisa menggeser nilai
            // ke kolom yang salah.
            StringBuilder kol = new StringBuilder("no_rawat");
            StringBuilder tanya = new StringBuilder("?");
            for (String k : KOLOM_RM20) {
                kol.append(",").append(k);
                tanya.append(",?");
            }
            try (PreparedStatement pstm = koneksi.prepareStatement(
                    "insert into asesmen_medis_ranap(" + kol + ") values(" + tanya + ")")) {
                pstm.setString(1, TNoRw.getText());
                String[] nilai = nilaiRm20();
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
        } else if (KeluhanUtama.getText().trim().isEmpty()) {
            Valid.textKosong(KeluhanUtama, "Keluhan Utama");
        } else if (tbObat.getSelectedRow() > -1) {
            if (akses.getkode().equals("Admin Utama")
                    || KdDokterPeriksa.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString())) {
                ganti();
            } else {
                JOptionPane.showMessageDialog(null, "Hanya bisa diganti oleh dokter yang bersangkutan..!!");
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
        }
    }

    private void ganti() {
        StringBuilder set = new StringBuilder();
        for (String k : KOLOM_RM20) {
            if (set.length() > 0) {
                set.append(",");
            }
            set.append(k).append("=?");
        }
        String[] nilai = nilaiRm20();
        String[] param = new String[nilai.length + 1];
        System.arraycopy(nilai, 0, param, 0, nilai.length);
        param[nilai.length] = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();

        if (Sequel.mengedittf("asesmen_medis_ranap", "no_rawat=?", set.toString(), param.length, param)) {
            runBackground(() -> tampil());
            emptTeks();
            TabRawat.setSelectedIndex(1);
        }
    }

    private void aksiHapus() {
        if (tbObat.getSelectedRow() > -1) {
            if (akses.getkode().equals("Admin Utama")
                    || KdDokterPeriksa.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString())) {
                hapus();
            } else {
                JOptionPane.showMessageDialog(null, "Hanya bisa dihapus oleh dokter yang bersangkutan..!!");
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
        }
    }

    private void hapus() {
        if (Sequel.queryu2tf("delete from asesmen_medis_ranap where no_rawat=?", 1, new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
        })) {
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText("" + tabMode.getRowCount());
            TabRawat.setSelectedIndex(1);
        } else {
            JOptionPane.showMessageDialog(null, "Gagal menghapus..!!");
        }
    }

    private String sqlDaftar(boolean pakaiCari) {
        String sql =
                "select asesmen_medis_ranap.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,asesmen_medis_ranap.kd_dokter_periksa,"
                + "dokterperiksa.nm_dokter as nm_dokter_periksa,dokterdpjp.nm_dokter as nm_dokter_dpjp,"
                + "asesmen_medis_ranap.tanggal,asesmen_medis_ranap.diagnosa_kerja_1 "
                + "from asesmen_medis_ranap "
                + "inner join reg_periksa on asesmen_medis_ranap.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join dokter dokterperiksa on asesmen_medis_ranap.kd_dokter_periksa=dokterperiksa.kd_dokter "
                + "left join dokter dokterdpjp on asesmen_medis_ranap.kd_dokter_dpjp=dokterdpjp.kd_dokter "
                + "where asesmen_medis_ranap.tanggal between ? and ? ";
        if (pakaiCari) {
            sql += "and (asesmen_medis_ranap.no_rawat like ? or pasien.no_rkm_medis like ? "
                    + "or pasien.nm_pasien like ? or dokterperiksa.nm_dokter like ? "
                    + "or dokterdpjp.nm_dokter like ?) ";
        }
        return sql + "order by asesmen_medis_ranap.tanggal";
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        boolean pakaiCari = !TCari.getText().trim().isEmpty();
        try {
            ps = koneksi.prepareStatement(sqlDaftar(pakaiCari));
            try {
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                if (pakaiCari) {
                    for (int p = 3; p <= 7; p++) {
                        ps.setString(p, "%" + TCari.getText() + "%");
                    }
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"), rs.getString("no_rkm_medis"), rs.getString("nm_pasien"),
                        rs.getDate("tgl_lahir"), rs.getString("jk"), rs.getString("kd_dokter_periksa"),
                        rs.getString("nm_dokter_periksa"), rs.getString("nm_dokter_dpjp"),
                        rs.getString("tanggal"), rs.getString("diagnosa_kerja_1")
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
            System.out.println("Notifikasi : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    /**
     * Memuat detail satu record dengan QUERY ULANG KE DATABASE, bukan membaca
     * indeks array JTable.
     *
     * <p>Pola array-index ({@code tbObat.getValueAt(row, 27)}) adalah akar
     * beberapa bug di Triase dan sudah ditinggalkan sejak RM 03-2: begitu jumlah
     * kolom tabel daftar berubah, semua indeks bergeser dan meledak saat runtime
     * tanpa peringatan compiler. Dengan query ulang, tabel daftar bebas diringkas
     * kapan saja tanpa menyentuh method ini.</p>
     */
    private void getData() {
        if (tbObat.getSelectedRow() == -1) {
            return;
        }
        String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        try {
            ps = koneksi.prepareStatement(
                    "select asesmen_medis_ranap.*,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                    + "dokterperiksa.nm_dokter as nm_dokter_periksa,dokterdpjp.nm_dokter as nm_dokter_dpjp "
                    + "from asesmen_medis_ranap "
                    + "inner join reg_periksa on asesmen_medis_ranap.no_rawat=reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "left join dokter dokterperiksa on asesmen_medis_ranap.kd_dokter_periksa=dokterperiksa.kd_dokter "
                    + "left join dokter dokterdpjp on asesmen_medis_ranap.kd_dokter_dpjp=dokterdpjp.kd_dokter "
                    + "where asesmen_medis_ranap.no_rawat=?");
            try {
                ps.setString(1, noRawat);
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRw.setText(rs.getString("no_rawat"));
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    Jk.setText(rs.getString("jk"));
                    TNoRM1.setText(rs.getString("no_rkm_medis"));
                    TPasien1.setText(rs.getString("nm_pasien"));
                    Valid.SetTgl2(TglAsuhan, rs.getString("tanggal"));

                    KdDokterPeriksa.setText(teksAman(rs.getString("kd_dokter_periksa")));
                    NmDokterPeriksa.setText(teksAman(rs.getString("nm_dokter_periksa")));
                    KdDokterDpjp.setText(teksAman(rs.getString("kd_dokter_dpjp")));
                    NmDokterDpjp.setText(teksAman(rs.getString("nm_dokter_dpjp")));

                    KeluhanUtama.setText(teksAman(rs.getString("keluhan_utama")));
                    KeluhanTambahan.setText(teksAman(rs.getString("keluhan_tambahan")));
                    Alergi.setText(teksAman(rs.getString("alergi")));
                    pilihRadio(bgSumberRiwayat, rs.getString("sumber_riwayat"));
                    RiwayatPenyakitPengobatan.setText(teksAman(rs.getString("riwayat_penyakit_pengobatan")));

                    pilihCombo(KeadaanUmum, rs.getString("keadaan_umum"));
                    pilihCombo(KesadaranMental, rs.getString("kesadaran_mental"));
                    GcsE.setText(teksAman(rs.getString("gcs_e")));
                    GcsM.setText(teksAman(rs.getString("gcs_m")));
                    GcsV.setText(teksAman(rs.getString("gcs_v")));
                    TdSistol.setText(teksAman(rs.getString("td_sistol")));
                    TdDiastol.setText(teksAman(rs.getString("td_diastol")));
                    Nadi.setText(teksAman(rs.getString("nadi")));
                    RR.setText(teksAman(rs.getString("rr")));
                    Suhu.setText(teksAman(rs.getString("suhu")));
                    TB.setText(teksAman(rs.getString("tb")));
                    BB.setText(teksAman(rs.getString("bb")));
                    // Umur & IMT sengaja diambil dari nilai TERSIMPAN (snapshot
                    // saat asesmen), bukan dihitung ulang - itu justru gunanya
                    // disimpan sebagai jejak audit.
                    TUmur.setText(teksAman(rs.getString("umur")));
                    TImt.setText(teksAman(rs.getString("imt")));
                    pilihRadio(bgStatusPediatrik, rs.getString("status_pediatrik"));

                    DiagnosaKerja1.setText(teksAman(rs.getString("diagnosa_kerja_1")));
                    DiagnosaKerja2.setText(teksAman(rs.getString("diagnosa_kerja_2")));
                    DiagnosaKerja3.setText(teksAman(rs.getString("diagnosa_kerja_3")));
                    RencanaTerapi.setText(teksAman(rs.getString("rencana_terapi")));

                    bangunRingkasanHtml();
                    ChkAccor.setSelected(true);
                    isMenu();
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
        KeluhanUtama.setText("");
        KeluhanTambahan.setText("");
        Alergi.setText("");
        bgSumberRiwayat.clearSelection();
        RiwayatPenyakitPengobatan.setText("");
        KeadaanUmum.setSelectedIndex(0);
        KesadaranMental.setSelectedIndex(0);
        GcsE.setText("");
        GcsM.setText("");
        GcsV.setText("");
        TdSistol.setText("");
        TdDiastol.setText("");
        Nadi.setText("");
        RR.setText("");
        Suhu.setText("");
        TB.setText("");
        BB.setText("");
        TImt.setText("");
        bgStatusPediatrik.clearSelection();
        LblCatatanGizi.setText("");
        DiagnosaKerja1.setText("");
        DiagnosaKerja2.setText("");
        DiagnosaKerja3.setText("");
        RencanaTerapi.setText("");
        // DPJP dikosongkan, tapi Dokter Pemeriksa TIDAK - kalau user yang login
        // adalah dokter, isCek() sudah menguncinya ke dirinya sendiri dan
        // mengosongkannya di sini justru membuat form tidak bisa disimpan.
        KdDokterDpjp.setText("");
        NmDokterDpjp.setText("");
        TglAsuhan.setDate(new Date());
        perbaruiUmur();
        TabRawat.setSelectedIndex(0);
        KeluhanUtama.requestFocus();
    }

    private void isRawat() {
        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi "
                    + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    perbaruiUmur();
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
        barisHtml(h, "J.K.", Jk.getText());
        barisHtml(h, "Tanggal & Jam", TglAsuhan.getSelectedItem() + "");
        barisHtml(h, "Dokter Pemeriksa", NmDokterPeriksa.getText());
        barisHtml(h, "DPJP", NmDokterDpjp.getText());

        judulHtml(h, "I. ANAMNESA");
        barisHtml(h, "Keluhan Utama", KeluhanUtama.getText());
        barisHtml(h, "Keluhan Tambahan", KeluhanTambahan.getText());
        barisHtml(h, "Alergi", Alergi.getText());
        barisHtml(h, "Riwayat Perjalanan Penyakit & Pengobatan",
                teksAman(pilihan(bgSumberRiwayat)) + " - " + RiwayatPenyakitPengobatan.getText());

        judulHtml(h, "II. PEMERIKSAAN MEDIS");
        barisHtml(h, "Keadaan Umum", KeadaanUmum.getSelectedItem() + "");
        barisHtml(h, "Kesadaran Mental", KesadaranMental.getSelectedItem() + "");
        barisHtml(h, "GCS", "E " + GcsE.getText() + "  M " + GcsM.getText() + "  V " + GcsV.getText());
        barisHtml(h, "Tensi", TdSistol.getText() + "/" + TdDiastol.getText() + " mmHg");
        barisHtml(h, "Nadi / RR / Suhu",
                Nadi.getText() + " x/mnt  /  " + RR.getText() + " x/mnt  /  " + Suhu.getText() + " °C");
        barisHtml(h, "TB / BB / IMT",
                TB.getText() + " cm  /  " + BB.getText() + " kg  /  " + TImt.getText());
        barisHtml(h, "Status Pediatrik", teksAman(pilihan(bgStatusPediatrik)));

        judulHtml(h, "DIAGNOSA KERJA");
        barisHtml(h, "1", DiagnosaKerja1.getText());
        barisHtml(h, "2", DiagnosaKerja2.getText());
        barisHtml(h, "3", DiagnosaKerja3.getText());

        judulHtml(h, "RENCANA DAN TERAPI");
        barisHtml(h, "Rencana & Terapi", RencanaTerapi.getText());

        h.append("</table></html>");
        LoadHTML.setText(h.toString());
        // JEditorPane otomatis menggulung ke BAWAH dokumen setelah setText -
        // tanpa baris ini panel terbuka dalam keadaan ter-scroll ke bawah dan
        // baris identitas paling atas tidak terlihat (bug yang sudah dilaporkan
        // dan diperbaiki di RM 03-4 poin 29).
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
                Sequel.cariIsi("select kd_dokter_dpjp from asesmen_medis_ranap where no_rawat=?", noRawat),
                tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString(),
                tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString()));

        String qry = sqlCetak(noRawat);
        if ("PDF".equals(mode)) {
            Valid.MyReportqrypdf("rptAsesmenMedisRanapRM20.jasper", "report",
                    "::[ Asesmen Medis Rawat Inap ]::", qry, param);
        } else {
            Valid.MyReportqry("rptAsesmenMedisRanapRM20.jasper", "report",
                    "::[ Asesmen Medis Rawat Inap ]::", qry, param);
        }
    }

    /**
     * Teks atestasi yang di-encode jadi QR di cetakan.
     *
     * <p>Berbeda dari RM 03-4/5/6 yang memakai QR yang SAMA untuk dua blok
     * tanda tangan (di sana keduanya atestasi petugas yang sama), di RM 20
     * kedua penandatangan benar-benar orang berbeda - jadi masing-masing dapat
     * QR sendiri dengan hash sidik jarinya sendiri.</p>
     */
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
        return "select asesmen_medis_ranap.*,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                + "reg_periksa.tgl_registrasi,reg_periksa.jam_reg,"
                + "dokterperiksa.nm_dokter as nm_dokter_periksa,dokterperiksa.no_ijn_praktek as ijin_periksa,"
                + "dokterdpjp.nm_dokter as nm_dokter_dpjp,dokterdpjp.no_ijn_praktek as ijin_dpjp "
                + "from asesmen_medis_ranap "
                + "inner join reg_periksa on asesmen_medis_ranap.no_rawat=reg_periksa.no_rawat "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join dokter dokterperiksa on asesmen_medis_ranap.kd_dokter_periksa=dokterperiksa.kd_dokter "
                + "left join dokter dokterdpjp on asesmen_medis_ranap.kd_dokter_dpjp=dokterdpjp.kd_dokter "
                + "where asesmen_medis_ranap.no_rawat='" + noRawat + "'";
    }

    // =================================================================
    //  API dipakai pemanggil (frmUtama / DlgKamarInap)
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
            // Aturan lama dipertahankan: kalau yang login seorang dokter, dia
            // hanya boleh mencatat atas nama dirinya sendiri sebagai PEMERIKSA.
            // DPJP SENGAJA dibiarkan bebas dipilih - DPJP umumnya orang lain
            // (dokter penanggung jawab ruangan), kalau ikut dikunci maka DPJP
            // akan selalu sama dengan pemeriksa dan field-nya jadi tidak berguna.
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
