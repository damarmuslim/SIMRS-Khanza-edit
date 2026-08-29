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
import java.util.LinkedHashMap;
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
 * ASESMEN AWAL KEPERAWATAN DI INSTALASI RAWAT JALAN (RM 02-2 / RM 02-3)
 * RSUD Demang Sepulau Raya. Satu lembar per KUNJUNGAN rawat jalan.
 *
 * <p><b>Kenapa tabel dan form baru.</b>
 * {@code penilaian_awal_keperawatan_ralan} (57 kolom) sudah ada dan sebagian
 * isinya cocok, tapi ia melayani form "Pengkajian Awal Keperawatan Rawat Jalan"
 * bawaan Khanza yang bentuknya lain beserta 13 tabel turunan per kelompok
 * pasien - dan ia tidak punya blok terbesar kertas ini: pemeriksaan fisik 20
 * regio, pola kebiasaan, pertumbuhan balita, dan kesehatan reproduksi.
 * Menambah seratus kolom ke sana akan mengganggu modul yang sudah jalan.</p>
 *
 * <p><b>Satu kolom per REGIO, bukan per kotak centang.</b> Kertas ini punya
 * ~130 kotak centang hanya untuk pemeriksaan fisik. Satu kolom per kotak akan
 * membuat tabelnya ~235 kolom dan mendekati batas ukuran baris InnoDB -
 * masalah yang sudah pernah menabrak {@code penilaian_awal} RM 19 pada 442
 * kolom. Jadi tiap regio disimpan sebagai SATU teks berisi temuan yang
 * dicentang, dipisah koma. Kotak centangnya tetap ada di form; penggabungan
 * terjadi di {@link GrupCentang#nilai()} dan pemulihannya di
 * {@link GrupCentang#set(String)}. Konsekuensi yang disadari: temuan hanya
 * bisa dicari dengan LIKE, tidak bisa diagregasi rapi per temuan.</p>
 *
 * <p><b>Dua blok BERSYARAT</b> - permintaan user. Blok "Pertumbuhan dan
 * Perkembangan Balita" hanya muncul untuk pasien di bawah 5 tahun, dan blok
 * "Kesehatan Reproduksi" hanya untuk pasien perempuan. Kolomnya tetap ada dan
 * tetap tersimpan; yang bersyarat hanya tampilannya, mengikuti pola blok Sebab
 * Kematian di RM 17-01. Pengaman penting: kalau recordnya SUDAH berisi data
 * pada blok itu, bloknya tetap ditampilkan walau syaratnya tidak lagi
 * terpenuhi - tanpa itu, balita yang ulang tahun kelima akan membuat data yang
 * sudah tersimpan tidak bisa dilihat maupun dikoreksi lagi.</p>
 *
 * <p><b>Kolom dan nilai dijaga oleh SATU peta.</b> Berbeda dari RM 02-1 yang
 * memakai dua daftar sejajar ({@code KOLOM_RM02} dan {@code nilaiRm02()}) yang
 * harus dijaga manual, di sini tiap kolom didaftarkan bersama kendalinya di
 * {@link #kendali}. Nilai simpan dan pengisian ulang sama-sama berjalan dengan
 * menelusuri {@link #KOLOM} atas peta itu, jadi salah urutan secara struktural
 * tidak mungkin terjadi. Constructor memverifikasi tiap kolom punya kendali.</p>
 *
 * <p>Ditulis tangan dengan {@link GridBagLayout} tanpa berkas {@code .form} dan
 * tanpa blok GEN, mengikuti preseden RM 20/17-01/21/33/01/02-1. Tanpa gerbang
 * hak akses, mengikuti RM 21/33/01/02-1.</p>
 */
public final class RMAsesmenAwalKeperawatanRalan extends javax.swing.JDialog {

    /**
     * Nama kolom {@code asesmen_awal_keperawatan_ralan} di luar {@code no_rawat},
     * urut sesuai migrasi. Tiap nama di sini WAJIB punya kendali terdaftar di
     * {@link #kendali} - diperiksa di constructor.
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

    /** Daftar pilihan hidup di {@link CentangRM022} - dipakai bersama oleh
     * form ini dan berkas laporan, supaya layar dan kertas tidak bisa berbeda. */
    private static final String[][] REGIO = CentangRM022.REGIO;

    /** Daftar pilihan hidup di {@link CentangRM022} - dipakai bersama oleh
     * form ini dan berkas laporan, supaya layar dan kertas tidak bisa berbeda. */
    private static final String[][] KEBIASAAN = CentangRM022.KEBIASAAN;

    /** Daftar pilihan hidup di {@link CentangRM022} - dipakai bersama oleh
     * form ini dan berkas laporan, supaya layar dan kertas tidak bisa berbeda. */
    private static final String[][] PSIKOSOSIAL = CentangRM022.PSIKOSOSIAL;

    /** Daftar pilihan hidup di {@link CentangRM022} - dipakai bersama oleh
     * form ini dan berkas laporan, supaya layar dan kertas tidak bisa berbeda. */
    private static final String[][] BALITA_CENTANG = CentangRM022.BALITA_CENTANG;

    /** Tonggak perkembangan balita - diisi usia dalam bulan. */
    private static final String[][] BALITA_TONGGAK = {
        {"balita_membalikkan", "Membalikkan badan"},
        {"balita_duduk", "Duduk"},
        {"balita_berdiri", "Berdiri"},
        {"balita_berjalan", "Berjalan"},
        {"balita_mengoceh", "Mengoceh"},
        {"balita_berbicara", "Berbicara"}
    };

    /** Daftar pilihan hidup di {@link CentangRM022} - dipakai bersama oleh
     * form ini dan berkas laporan, supaya layar dan kertas tidak bisa berbeda. */
    private static final String[][] REPRO_CENTANG = CentangRM022.REPRO_CENTANG;

    private static final String[] PILIHAN_DIAGNOSA = {
        "dx_oksigen", "Gangguan pemenuhan kebutuhan oksigen",
        "dx_nutrisi", "Gangguan pemenuhan kebutuhan nutrisi",
        "dx_eliminasi", "Gangguan pemenuhan kebutuhan eliminasi",
        "dx_integritas_kulit", "Gangguan integritas kulit",
        "dx_kurang_pengetahuan", "Kurang pengetahuan tentang penyakit"
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

    /**
     * Kolom -> kendali yang mengisinya. Satu-satunya sumber pemetaan; simpan
     * dan muat sama-sama menelusuri {@link #KOLOM} atas peta ini.
     */
    private final Map<String, Object> kendali = new LinkedHashMap<>();

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
    private widget.TextBox NipPerawat, NmPerawat;

    // ---------- Blok bersyarat ----------
    private widget.PanelBiasa PanelBalita, PanelRepro;

    // ---------- Kontrol yang perlu dipegang di luar peta ----------
    private final ButtonGroup bgKeadaanUmum = new ButtonGroup();
    private final ButtonGroup bgCrt = new ButtonGroup();
    private widget.CekBox AdaKontrol;
    private widget.Tanggal TglKontrol;
    private KanvasNyeri kanvasNyeri;

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
        rakitHeader();
        rakitBadan();
        rakitTabData();

        periksaPemetaan();

        widget.IconStyle.pasang(this);
        bersihkanIkonRadio(getContentPane());
        ChkAccor.setSelected(false);
        isMenu();
        setSize(1250, 700);
    }

    /**
     * Menggagalkan lebih awal kalau ada kolom yang belum punya kendali, atau
     * kendali yang tidak dipakai kolom mana pun. Tanpa ini, kolom yang lupa
     * dirakit akan diam-diam tersimpan NULL selamanya.
     */
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
    //  GRUP CENTANG - inti keputusan "satu kolom per regio"
    // =================================================================

    /**
     * Sekumpulan kotak centang plus satu kotak "Lain-lain", yang bersama-sama
     * mengisi SATU kolom teks. Temuan yang dicentang digabung dengan koma.
     *
     * <p>Saat memuat ulang, tiap potongan dicocokkan dengan teks kotak centang;
     * yang tidak dikenali jatuh ke "Lain-lain". Itu membuat data lama tetap
     * bisa dibuka walau daftar pilihannya kelak berubah - potongan yang
     * pilihannya sudah dihapus tidak hilang, hanya pindah ke Lain-lain.</p>
     */
    private static final class GrupCentang {

        private final List<widget.CekBox> kotak = new ArrayList<>();
        private widget.TextBox lain;

        /**
         * Temuan yang dicentang, digabung koma, lalu isi "Lain-lain" di
         * paling belakang.
         *
         * <p>Urutannya DINORMALKAN ke urutan daftar pilihan, bukan urutan
         * masuknya. Jadi teks yang dibaca dari database bisa pulang dengan
         * urutan berbeda dari saat disimpan - isinya sama persis, hanya
         * urutannya yang diseragamkan. Itu disengaja: cetakan jadi selalu
         * mengikuti urutan yang sama dengan kertasnya, siapa pun yang mengisi
         * dan dari mana pun datanya datang.</p>
         */
        String nilai() {
            StringBuilder b = new StringBuilder();
            for (widget.CekBox c : kotak) {
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
                for (widget.CekBox c : kotak) {
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
            for (widget.CekBox c : kotak) {
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

    // =================================================================
    //  KERANGKA
    // =================================================================

    private void rakitKerangka() {
        internalFrame1 = new widget.InternalFrame();
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Asesmen Awal Keperawatan di Instalasi Rawat Jalan (RM 02-2) ]::"));
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
     * Memilih Perawat/Bidan dari master {@code petugas} lewat
     * {@link kepegawaian.DlgCariPetugas} - kolom 0 NIP, kolom 1 nama.
     *
     * <p>Dialognya dibuka TIDAK modal dan nilainya diambil pada
     * {@code windowClosed}, mengikuti pola yang sudah dipakai modul lain di
     * aplikasi ini. Kalau petugas menutup dialog tanpa memilih, isian lama
     * dibiarkan apa adanya - bukan dikosongkan.</p>
     *
     * <p>{@code petugas.nip} dan {@code pegawai.nik} memakai nilai yang sama
     * (diperiksa: ketujuh baris {@code petugas} semuanya punya pasangan di
     * {@code pegawai}), jadi nama dan QR tanda tangan yang mencari lewat
     * {@code pegawai} tetap ketemu.</p>
     */
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

    private widget.RadioButton radio(String teks, ButtonGroup grup) {
        widget.RadioButton r = new widget.RadioButton();
        r.setText(teks);
        grup.add(r);
        return r;
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

    /**
     * Merakit satu baris "judul : [centang][centang]... [Lain-lain]" lalu
     * mendaftarkannya sebagai kendali kolom {@code kolom}.
     *
     * <p>Kotak centang dibungkus sendiri dalam GridBagLayout dan dipatahkan
     * tiap {@code perBaris} buah. FlowLayout tidak dipakai karena tidak
     * membungkus di dalam GridBag - regio dengan dua belas pilihan akan
     * memanjang ke samping dan memaksa scroll horizontal.</p>
     */
    private void barisRegio(JPanel induk, int gridy, String kolom, String judul,
            String pilihan, int perBaris, boolean pakaiLain) {
        GrupCentang g = new GrupCentang();
        JPanel kotakan = new JPanel(new GridBagLayout());
        kotakan.setOpaque(false);

        String[] opsi = pilihan.split("\\|");
        for (int n = 0; n < opsi.length; n++) {
            widget.CekBox c = cek(opsi[n]);
            g.kotak.add(c);
            GridBagConstraints cc = new GridBagConstraints();
            cc.gridx = n % perBaris;
            cc.gridy = n / perBaris;
            cc.anchor = GridBagConstraints.WEST;
            cc.insets = new Insets(0, 0, 0, 10);
            kotakan.add(c, cc);
        }
        if (pakaiLain) {
            g.lain = kotak(180);
            GridBagConstraints cc = new GridBagConstraints();
            cc.gridx = 0;
            cc.gridy = (opsi.length + perBaris - 1) / perBaris;
            cc.gridwidth = perBaris;
            cc.anchor = GridBagConstraints.WEST;
            cc.insets = new Insets(2, 0, 0, 0);
            kotakan.add(baris(teks("Lain-lain :"), g.lain), cc);
        }

        induk.add(labelKolom(judul), konstrain(0, gridy));
        induk.add(kotakan, konstrain(1, gridy));
        kendali.put(kolom, g);
    }

    /** Kotak teks biasa yang langsung terdaftar sebagai kendali sebuah kolom. */
    private widget.TextBox kotakKolom(String kolom, int lebar) {
        widget.TextBox t = kotak(lebar);
        kendali.put(kolom, t);
        return t;
    }

    private widget.TextArea areaKolom(String kolom) {
        widget.TextArea a = new widget.TextArea();
        kendali.put(kolom, a);
        return a;
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
        // Perawat/Bidan TIDAK boleh diketik manual - dipilih dari master
        // `petugas` lewat DlgCariPetugas. NIP yang diketik tangan gampang salah
        // satu digit, dan akibatnya tidak kelihatan: JOIN ke pegawai gagal diam-
        // diam sehingga nama dan QR tanda tangan tercetak KOSONG pada lembar
        // yang sudah ditandatangani. Aturan ini berlaku untuk seluruh modul RM;
        // lihat CLAUDE.md.
        NipPerawat = kotakBaca(110);
        NmPerawat = kotakBaca(210);
        widget.Button btnPilihPerawat = new widget.Button();
        btnPilihPerawat.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/picture/Search-16x16.png")));
        btnPilihPerawat.setPreferredSize(new Dimension(28, 23));
        btnPilihPerawat.setToolTipText("Pilih perawat/bidan dari daftar petugas");
        btnPilihPerawat.addActionListener(evt -> pilihPetugas());

        // Ketiganya ditangani khusus di nilai()/set(), tapi tetap HARUS
        // terdaftar - periksaPemetaan() menolak kolom tanpa kendali.
        kendali.put("tanggal", TglAsesmen);
        kendali.put("jam", TglAsesmen);
        kendali.put("nip", NipPerawat);

        int r = 0;
        p.add(labelKolom("No.Rawat"), konstrain(0, r));
        p.add(baris(TNoRw, teks("   No.RM :"), TNoRM, teks("   Nama Pasien :"), TPasien), konstrain(1, r));
        r++;
        p.add(labelKolom("Tgl.Lahir"), konstrain(0, r));
        p.add(baris(TglLahir, teks("   Umur :"), TUmur, teks("   L/P :"), Jk), konstrain(1, r));
        r++;
        p.add(labelKolom("Tanggal & Jam"), konstrain(0, r));
        p.add(baris(TglAsesmen, teks("   Perawat/Bidan :"), btnPilihPerawat, NipPerawat,
                NmPerawat), konstrain(1, r));

        tambahPengisiKolom(p, 2);
        tambahSeksi(p, 0);
    }

    // =================================================================
    //  BADAN
    // =================================================================

    private void rakitBadan() {
        int seksi = 1;
        seksi = rakitRiwayat(seksi);
        seksi = rakitFisik(seksi);
        seksi = rakitKebiasaan(seksi);
        seksi = rakitBalita(seksi);
        seksi = rakitReproduksi(seksi);
        seksi = rakitPsikososial(seksi);
        seksi = rakitDiagnostik(seksi);
        seksi = rakitDiagnosa(seksi);

        tambahPengisiKolom(FormInput, 2);
        tambahPengisiBaris(FormInput, seksi);
    }

    private int rakitRiwayat(int seksi) {
        widget.PanelBiasa p = panelSeksi("2. RIWAYAT KESEHATAN");
        int r = 0;
        p.add(labelKolom("Diagnosa medis"), konstrain(0, r));
        p.add(kotakKolom("diagnosa_medis", 420), konstrain(1, r));
        r++;
        p.add(labelKolom("Keluhan utama"), konstrain(0, r));
        p.add(areaTeks(areaKolom("keluhan_utama"), 3, 620, 58), konstrain(1, r));
        r++;
        p.add(labelKolom("Riwayat kesehatan"), konstrain(0, r));
        p.add(areaTeks(areaKolom("riwayat_kesehatan"), 3, 620, 58), konstrain(1, r));
        p.add(teks("dahulu, sekarang, dan keluarga"), konstrain(2, r));

        tambahPengisiKolom(p, 3);
        tambahSeksi(p, seksi);
        return seksi + 1;
    }

    private int rakitFisik(int seksi) {
        widget.PanelBiasa p = panelSeksi("3. PEMERIKSAAN FISIK");
        int r = 0;

        p.add(labelKolom("Keadaan umum"), konstrain(0, r));
        p.add(baris(radio("Baik", bgKeadaanUmum), radio("Lemah", bgKeadaanUmum),
                radio("Buruk", bgKeadaanUmum)), konstrain(1, r));
        kendali.put("keadaan_umum", bgKeadaanUmum);
        r++;

        p.add(labelKolom("Tanda vital"), konstrain(0, r));
        p.add(baris(teks("TD :"), kotakKolom("td_sistolik", 45), teks("/"),
                kotakKolom("td_diastolik", 45), teks("mmHg    HR :"), kotakKolom("hr", 45),
                teks("x/mnt    RR :"), kotakKolom("rr", 45), teks("x/mnt    Suhu :"),
                kotakKolom("suhu", 45), teks("°C    VAS :"), kotakKolom("vas", 35),
                teks("(0-10)")), konstrain(1, r));
        r++;

        // Gambar skala nyeri Wong-Baker yang bisa DICORET pakai mouse, memakai
        // kelas yang sama dengan RM 19 - lihat KanvasNyeri. Angka VAS di baris
        // atas TIDAK diganti olehnya: keduanya berdampingan seperti di kertas,
        // yang satu angka yang satu tanda tangan tangan perawat.
        kanvasNyeri = new KanvasNyeri(380, 150);
        kanvasNyeri.setPreferredSize(new Dimension(380, 150));
        kendali.put("nyeri_canvas_gambar", kanvasNyeri);

        widget.Button btnHapusNyeri = new widget.Button();
        btnHapusNyeri.setText("Hapus tanda");
        btnHapusNyeri.setPreferredSize(new Dimension(110, 23));
        btnHapusNyeri.setToolTipText("Menghapus coretan pada gambar skala nyeri");
        btnHapusNyeri.addActionListener(evt -> kanvasNyeri.hapusCoretan());

        p.add(labelKolom("Skala nyeri"), konstrain(0, r));
        p.add(baris(kanvasNyeri, btnHapusNyeri), konstrain(1, r));
        r++;

        for (String[] reg : REGIO) {
            barisRegio(p, r++, reg[0], reg[1], reg[2], 5, true);
        }

        p.add(labelKolom("CRT"), konstrain(0, r));
        p.add(baris(radio("<2 detik", bgCrt), radio(">2 detik", bgCrt)), konstrain(1, r));
        kendali.put("crt", bgCrt);
        r++;

        p.add(labelKolom("Kekuatan otot"), konstrain(0, r));
        p.add(baris(teks("Ekstremitas superior  dextra :"), kotakKolom("otot_esd", 40),
                teks("  sinistra :"), kotakKolom("otot_ess", 40),
                teks("     inferior  dextra :"), kotakKolom("otot_eid", 40),
                teks("  sinistra :"), kotakKolom("otot_eis", 40),
                teks("  (nilai 0-5)")), konstrain(1, r));

        tambahPengisiKolom(p, 2);
        tambahSeksi(p, seksi);
        return seksi + 1;
    }

    private int rakitKebiasaan(int seksi) {
        widget.PanelBiasa p = panelSeksi("4. POLA KEBIASAAN PASIEN");
        int r = 0;
        for (String[] kb : KEBIASAAN) {
            barisRegio(p, r++, kb[0], kb[1], kb[2], 4, true);
        }
        tambahPengisiKolom(p, 2);
        tambahSeksi(p, seksi);
        return seksi + 1;
    }

    /**
     * Blok BERSYARAT - hanya ditampilkan untuk pasien di bawah 5 tahun, atau
     * kalau recordnya sudah berisi data balita.
     */
    private int rakitBalita(int seksi) {
        PanelBalita = panelSeksi("5. PERTUMBUHAN DAN PERKEMBANGAN (BALITA)");
        int r = 0;

        for (String[] b : BALITA_CENTANG) {
            barisRegio(PanelBalita, r++, b[0], b[1], b[2], 5, true);
        }

        PanelBalita.add(labelKolom("Kelahiran"), konstrain(0, r));
        PanelBalita.add(baris(teks("Anak ke :"), kotakKolom("balita_anak_ke", 45),
                teks("   BB lahir :"), kotakKolom("balita_bb_lahir", 55), teks("gram"),
                teks("   TB lahir :"), kotakKolom("balita_tb_lahir", 55), teks("cm")),
                konstrain(1, r));
        r++;

        PanelBalita.add(labelKolom("BB/TB sekarang"), konstrain(0, r));
        PanelBalita.add(baris(kotakKolom("balita_bbtb_sekarang", 160),
                teks("   Kelainan bawaan :"), kotakKolom("balita_kelainan", 260)), konstrain(1, r));
        r++;

        // Tonggak perkembangan: satu baris, diisi usia dalam bulan.
        JPanel tonggak = new JPanel(new GridBagLayout());
        tonggak.setOpaque(false);
        int n = 0;
        for (String[] t : BALITA_TONGGAK) {
            GridBagConstraints cc = new GridBagConstraints();
            cc.gridx = (n % 3) * 2;
            cc.gridy = n / 3;
            cc.anchor = GridBagConstraints.WEST;
            cc.insets = new Insets(1, 0, 1, 4);
            tonggak.add(teks(t[1] + " :"), cc);
            cc.gridx++;
            cc.insets = new Insets(1, 0, 1, 18);
            tonggak.add(baris(kotakKolom(t[0], 55), teks("bln")), cc);
            n++;
        }
        PanelBalita.add(labelKolom("Perkembangan"), konstrain(0, r));
        PanelBalita.add(tonggak, konstrain(1, r));

        tambahPengisiKolom(PanelBalita, 2);
        tambahSeksi(PanelBalita, seksi);
        PanelBalita.setVisible(false);
        return seksi + 1;
    }

    /**
     * Blok BERSYARAT - hanya ditampilkan untuk pasien perempuan, atau kalau
     * recordnya sudah berisi data reproduksi.
     */
    private int rakitReproduksi(int seksi) {
        PanelRepro = panelSeksi("6. KESEHATAN REPRODUKSI");
        int r = 0;

        PanelRepro.add(labelKolom("Haid"), konstrain(0, r));
        PanelRepro.add(baris(teks("Usia menarche :"), kotakKolom("repro_usia_menstruasi", 45),
                teks("th   Interval :"), kotakKolom("repro_interval", 55),
                teks("hari   Jumlah :"), kotakKolom("repro_jumlah", 70),
                teks("   Skala nyeri haid :"), kotakKolom("repro_nyeri_skala", 40),
                teks("   HPHT :"), kotakKolom("repro_hpht", 90)), konstrain(1, r));
        r++;

        for (String[] rp : REPRO_CENTANG) {
            barisRegio(PanelRepro, r++, rp[0], rp[1], rp[2], 5, true);
        }

        PanelRepro.add(labelKolom("Kehamilan ini"), konstrain(0, r));
        PanelRepro.add(baris(teks("Keluhan :"), kotakKolom("repro_keluhan_hamil", 260),
                teks("   Obat yang diminum :"), kotakKolom("repro_obat", 230)), konstrain(1, r));
        r++;

        PanelRepro.add(labelKolom("Status obstetri"), konstrain(0, r));
        PanelRepro.add(baris(teks("G :"), kotakKolom("obs_g", 35),
                teks("P :"), kotakKolom("obs_p", 35), teks("A :"), kotakKolom("obs_o", 35),
                teks("   Jml anak :"), kotakKolom("obs_jumlah_anak", 35),
                teks("(L :"), kotakKolom("obs_laki", 35), teks("P :"),
                kotakKolom("obs_perempuan", 35), teks(")   Usia anak terkecil :"),
                kotakKolom("obs_anak_terkecil", 90)), konstrain(1, r));
        r++;

        PanelRepro.add(labelKolom("Palpasi"), konstrain(0, r));
        PanelRepro.add(baris(teks("TFU :"), kotakKolom("palpasi_tfu", 70),
                teks("cm   Auskultasi DJJ :"), kotakKolom("palpasi_auskultasi", 90),
                teks("x/mnt")), konstrain(1, r));
        r++;

        PanelRepro.add(labelKolom("Janin"), konstrain(0, r));
        PanelRepro.add(baris(teks("Umur kehamilan :"), kotakKolom("janin_umur_kehamilan", 90),
                teks("mgg   Masalah :"), kotakKolom("janin_masalah", 280)), konstrain(1, r));
        r++;

        PanelRepro.add(labelKolom("Informasi lain"), konstrain(0, r));
        PanelRepro.add(baris(kotakKolom("repro_informasi_lain", 280),
                teks("   Dirujuk ke :"), kotakKolom("repro_dirujuk", 200)), konstrain(1, r));

        tambahPengisiKolom(PanelRepro, 2);
        tambahSeksi(PanelRepro, seksi);
        PanelRepro.setVisible(false);
        return seksi + 1;
    }

    private int rakitPsikososial(int seksi) {
        widget.PanelBiasa p = panelSeksi("7. DATA PSIKOLOGIS, SOSIOLOGIS, DAN SPIRITUAL");
        int r = 0;
        for (String[] ps : PSIKOSOSIAL) {
            barisRegio(p, r++, ps[0], ps[1], ps[2], 4, true);
        }
        tambahPengisiKolom(p, 2);
        tambahSeksi(p, seksi);
        return seksi + 1;
    }

    private int rakitDiagnostik(int seksi) {
        widget.PanelBiasa p = panelSeksi("8. DATA PEMERIKSAAN DIAGNOSTIK & 9. KONTROL");
        int r = 0;
        p.add(labelKolom("Laboratorium"), konstrain(0, r));
        p.add(areaTeks(areaKolom("diag_laboratorium"), 3, 620, 58), konstrain(1, r));
        r++;
        p.add(labelKolom("Rontgen"), konstrain(0, r));
        p.add(kotakKolom("diag_rontgen", 620), konstrain(1, r));
        r++;
        p.add(labelKolom("EKG"), konstrain(0, r));
        p.add(kotakKolom("diag_ekg", 620), konstrain(1, r));
        r++;
        p.add(labelKolom("USG"), konstrain(0, r));
        p.add(kotakKolom("diag_usg", 620), konstrain(1, r));
        r++;
        p.add(labelKolom("Scanning"), konstrain(0, r));
        p.add(kotakKolom("diag_scanning", 620), konstrain(1, r));
        r++;
        p.add(labelKolom("Terapi medik"), konstrain(0, r));
        p.add(areaTeks(areaKolom("diag_terapi_medik"), 3, 620, 58), konstrain(1, r));
        r++;

        AdaKontrol = cek("Ada rencana kontrol");
        TglKontrol = tanggal("dd-MM-yyyy", 110);
        TglKontrol.setEnabled(false);
        AdaKontrol.addActionListener(evt -> TglKontrol.setEnabled(AdaKontrol.isSelected()));
        // Tanggal hanya disimpan kalau dicentang - tanpa gerbang ini, tanggal
        // hari ini ikut tersimpan pada pasien yang memang tidak dijadwalkan.
        kendali.put("kontrol_tanggal", TglKontrol);

        p.add(labelKolom("Kontrol berikutnya"), konstrain(0, r));
        p.add(baris(AdaKontrol, TglKontrol, teks("   Rencana rawat inap :"),
                kotakKolom("kontrol_rawat_inap", 260)), konstrain(1, r));

        tambahPengisiKolom(p, 2);
        tambahSeksi(p, seksi);
        return seksi + 1;
    }

    private int rakitDiagnosa(int seksi) {
        widget.PanelBiasa p = panelSeksi("10. DIAGNOSA KEPERAWATAN");
        JPanel kotakan = new JPanel(new GridBagLayout());
        kotakan.setOpaque(false);
        for (int n = 0; n < PILIHAN_DIAGNOSA.length; n += 2) {
            widget.CekBox c = cek(PILIHAN_DIAGNOSA[n + 1]);
            kendali.put(PILIHAN_DIAGNOSA[n], c);
            GridBagConstraints cc = new GridBagConstraints();
            cc.gridx = (n / 2) % 2;
            cc.gridy = (n / 2) / 2;
            cc.anchor = GridBagConstraints.WEST;
            cc.insets = new Insets(1, 0, 1, 24);
            kotakan.add(c, cc);
        }
        p.add(kotakan, konstrain(0, 0));
        tambahPengisiKolom(p, 1);
        tambahSeksi(p, seksi);
        return seksi + 1;
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
    //  LAPISAN DATA - satu peta, tanpa daftar sejajar
    // =================================================================

    /** Nilai satu kolom, apa pun jenis kendalinya. */
    private String nilai(String kolom) {
        if ("tanggal".equals(kolom)) {
            return Valid.SetTgl(TglAsesmen.getSelectedItem() + "");
        }
        if ("jam".equals(kolom)) {
            return TglAsesmen.getSelectedItem().toString().substring(11, 19);
        }
        if ("nip".equals(kolom)) {
            return kosongKeNull(NipPerawat.getText());
        }
        Object k = kendali.get(kolom);
        if (k instanceof GrupCentang) {
            return ((GrupCentang) k).nilai();
        }
        if (k instanceof KanvasNyeri) {
            // "" kalau memang belum dicoret - kosongKeNull membuatnya NULL,
            // bukan ribuan byte gambar kosong.
            return kosongKeNull(((KanvasNyeri) k).getCoretanBase64());
        }
        if (k instanceof widget.CekBox) {
            // Kolom ENUM('Ya','Tidak') - dicentang berarti Ya.
            return ((widget.CekBox) k).isSelected() ? "Ya" : "Tidak";
        }
        if (k instanceof ButtonGroup) {
            return pilihan((ButtonGroup) k);
        }
        if (k == TglKontrol) {
            // Hanya tanggal kontrol - dan hanya kalau dicentang.
            return AdaKontrol.isSelected()
                    ? Valid.SetTgl(TglKontrol.getSelectedItem() + "") : null;
        }
        if (k instanceof javax.swing.text.JTextComponent) {
            return kosongKeNull(((javax.swing.text.JTextComponent) k).getText());
        }
        return null;
    }

    /** Kebalikan {@link #nilai(String)} - dipakai saat memuat record. */
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
            // Hanya tanggal kontrol yang boleh lewat sini; TglAsesmen terdaftar
            // untuk kolom tanggal/jam tapi diisi sendiri di isiDariRecord().
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
        StringBuilder set = new StringBuilder();
        String[] param = new String[KOLOM.length + 1];
        for (int n = 0; n < KOLOM.length; n++) {
            if (set.length() > 0) {
                set.append(",");
            }
            set.append(KOLOM[n]).append("=?");
            param[n] = nilai(KOLOM[n]);
        }
        param[KOLOM.length] = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();

        if (Sequel.mengedittf("asesmen_awal_keperawatan_ralan", "no_rawat=?", set.toString(),
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
        for (String k : KOLOM) {
            if ("tanggal".equals(k) || "jam".equals(k)) {
                continue;
            }
            if ("nip".equals(k)) {
                String nip = teksAman(rs.getString("nip"));
                NipPerawat.setText(nip);
                NmPerawat.setText(nip.isEmpty() ? "" : teksAman(
                        Sequel.cariIsi("select nama from pegawai where nik=?", nip)));
                continue;
            }
            set(k, rs.getString(k));
        }
        // Blok bersyarat dibuka kalau recordnya memang berisi - lihat catatan
        // kelas: balita yang ulang tahun kelima tidak boleh kehilangan datanya.
        if (adaIsi(BALITA_CENTANG, BALITA_TONGGAK)) {
            PanelBalita.setVisible(true);
        }
        if (adaIsiRepro()) {
            PanelRepro.setVisible(true);
        }
    }

    private String teksAman(String s) {
        return s == null ? "" : s;
    }

    public void emptTeks() {
        for (String k : KOLOM) {
            if ("tanggal".equals(k) || "jam".equals(k) || "nip".equals(k)) {
                continue;
            }
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
        TabRawat.setSelectedIndex(0);
    }

    // =================================================================
    //  IDENTITAS DAN BLOK BERSYARAT
    // =================================================================

    /**
     * Identitas pasien dari master. Tidak disimpan di tabel form ini - selalu
     * dibaca ulang supaya lembar mengikuti data pendaftaran. Sekaligus
     * menentukan blok mana yang ditampilkan.
     */
    private void muatIdentitas() {
        try {
            ps = koneksi.prepareStatement(
                    "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.jk,"
                    + "if(pasien.jk='L','Laki-Laki','Perempuan') as kelamin,"
                    + "reg_periksa.umurdaftar,reg_periksa.sttsumur,"
                    + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur "
                    + "from reg_periksa "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRM.setText(teksAman(rs.getString("no_rkm_medis")));
                    TPasien.setText(teksAman(rs.getString("nm_pasien")));
                    TglLahir.setText(teksAman(rs.getString("tgl_lahir")));
                    TUmur.setText(teksAman(rs.getString("umur")));
                    Jk.setText(teksAman(rs.getString("kelamin")));
                    TNoRM1.setText(TNoRM.getText());
                    TPasien1.setText(TPasien.getText());
                    aturBlokBersyarat(teksAman(rs.getString("jk")),
                            teksAman(rs.getString("umurdaftar")),
                            teksAman(rs.getString("sttsumur")));
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
     * Menampilkan atau menyembunyikan dua blok bersyarat - permintaan user.
     * Balita untuk pasien di bawah 5 tahun, reproduksi untuk pasien perempuan.
     *
     * <p>{@code sttsumur} bernilai Th/Bl/Hr; satuan selain tahun otomatis
     * berarti balita, jadi tidak perlu mengurai tanggal lahir. Kalau umurnya
     * tidak terbaca sama sekali, blok balita DISEMBUNYIKAN - menampilkan blok
     * kosong pada pasien dewasa lebih mengganggu daripada tidak menampilkan
     * blok pada balita yang datanya cacat, dan {@link #isiDariRecord()} tetap
     * membukanya kalau memang sudah ada isinya.</p>
     */
    private void aturBlokBersyarat(String jk, String umurdaftar, String sttsumur) {
        boolean balita;
        String satuan = sttsumur.trim();
        if (satuan.equalsIgnoreCase("Bl") || satuan.equalsIgnoreCase("Hr")) {
            balita = true;
        } else {
            int tahun = -1;
            try {
                tahun = Integer.parseInt(umurdaftar.trim());
            } catch (NumberFormatException e) {
                // Umur tidak terbaca - diperlakukan bukan balita, lihat javadoc.
            }
            balita = tahun >= 0 && tahun < 5;
        }
        PanelBalita.setVisible(balita);
        PanelRepro.setVisible("P".equalsIgnoreCase(jk.trim()));
        FormInput.revalidate();
        FormInput.repaint();
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

    // =================================================================
    //  RINGKASAN HTML
    // =================================================================

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
        // Coretan kanvas tidak bisa ditampilkan di ringkasan HTML, jadi cukup
        // dinyatakan ada atau tidak - meniru cara RM 19 melaporkannya.
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

        if (PanelBalita.isVisible()) {
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

        if (PanelRepro.isVisible()) {
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
            if ("Ya".equals(nilai(PILIHAN_DIAGNOSA[n]))) {
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

    // =================================================================
    //  CETAK
    // =================================================================

    private void cetakTerpilih() {
        if (tbObat.getSelectedRow() <= -1) {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
            return;
        }
        String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        String nip = tbObat.getValueAt(tbObat.getSelectedRow(), 8) == null
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
        param.put("finger", tandaTangan(nip, nama));
        // Coretan skala nyeri dibaca ulang dari DB, bukan dari tabel di layar:
        // kolomnya sengaja tidak ikut dimuat ke tabel karena base64-nya panjang
        // dan tidak ada gunanya ditampilkan sebagai kolom.
        param.put("gambarnyeri", KanvasNyeri.gambarCetak(Sequel.cariIsi(
                "select nyeri_canvas_gambar from asesmen_awal_keperawatan_ralan where no_rawat=?",
                noRawat)));
        // Pilihan bentuk keluaran - permintaan user. "Lembar" membuka penampil
        // Jasper yang bisa langsung dikirim ke printer; "PDF" mengekspor ke
        // report/rptAsesmenAwalKeperawatanRalanRM02.pdf lalu membukanya dengan
        // pembaca PDF bawaan komputer.
        //
        // Yang perlu diketahui: MyReportPDF memakai nama berkas TETAP dari nama
        // laporannya, jadi PDF pasien sebelumnya DITIMPA setiap kali mencetak.
        // Itu perilaku bawaan Khanza yang dipakai seluruh modul lain; dibiarkan
        // sama supaya tidak ada dua kebiasaan berbeda di aplikasi yang sama.
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
        // Batal atau dialog ditutup: tidak melakukan apa-apa.
    }

    private String tandaTangan(String nip, String nama) {
        String finger = Sequel.cariIsi(
                "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id "
                + "where pegawai.nik=?", nip == null ? "" : nip);
        return "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                + "\nDitandatangani secara elektronik oleh " + nama
                + "\nID " + (finger == null || finger.isEmpty() ? nip : finger);
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
     * Tanpa gerbang hak akses, mengikuti RM 21/33/01/02-1. Tugasnya mengisi
     * perawat pengkaji dengan petugas yang sedang login kalau belum terisi.
     */
    public void isCek() {
        if (NipPerawat.getText().trim().isEmpty()) {
            // akses.getkode() adalah NIP petugas yang login - nilai yang sama
            // dipakai RM 32/33 sebagai KdPetugas dan dicocokkan ke pegawai.nik.
            String nip = akses.getkode();
            if (nip != null && !nip.trim().isEmpty() && !"Admin Utama".equals(nip)) {
                NipPerawat.setText(nip);
                NmPerawat.setText(teksAman(
                        Sequel.cariIsi("select nama from pegawai where nik=?", nip)));
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
