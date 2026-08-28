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
 * RESUME KEPERAWATAN / KEBIDANAN (RM 33) - RSUD Demang Sepulau Raya.
 * Diisi oleh Perawat / Bidan saat pasien RAWAT INAP pulang.
 *
 * <p><b>Kenapa form baru.</b> Sebelum ini tidak ada satu pun resume keperawatan
 * di sistem. Yang ada adalah {@link RMDataResumePasienRanap} dengan tabel
 * {@code resume_pasien_ranap} - itu resume MEDIS: ditandatangani DOKTER
 * ({@code kd_dokter}), isinya diagnosa &amp; prosedur ber-kode ICD, obat pulang,
 * cara keluar. Dua profesi, dua tanda tangan, dua lembar fisik terpisah.
 * {@code penilaian_awal_keperawatan_ranap_masalah} memang berisi masalah
 * keperawatan, tapi diambil saat MASUK dan berupa kode ke tabel master - bukan
 * lima baris teks bebas saat PULANG.</p>
 *
 * <p><b>Tanggal masuk, tanggal keluar, dan ruangan TIDAK disimpan</b> di tabel
 * form ini; ketiganya dibaca dari {@code kamar_inap} setiap kali form dibuka
 * dan setiap kali mencetak. Menyalinnya akan membuat lembar ini basi diam-diam
 * kalau pasien pindah kamar atau tanggal pulangnya dikoreksi. Pasien yang
 * pindah kamar punya BEBERAPA baris {@code kamar_inap}, jadi aturannya:
 * tanggal masuk = yang PALING AWAL, sedangkan ruangan dan tanggal keluar =
 * dari baris TERAKHIR.</p>
 *
 * <p>Empat blok isian masing-masing lima baris bernomor, persis seperti kertas,
 * semuanya teks bebas (keputusan user). Nama kolom database dan nilai yang
 * dikirim dirakit dari konstanta {@link #BLOK} yang sama lewat perulangan yang
 * bentuknya identik - itulah yang menjamin urutan keduanya tidak pernah bisa
 * desinkron.</p>
 *
 * <p>Ditulis tangan dengan {@link GridBagLayout} tanpa berkas {@code .form} dan
 * tanpa blok GEN, mengikuti preseden RM 20, RM 17-01, dan RM 21. Gotcha widget
 * yang dipatuhi: {@code widget.TextBox}/{@code ComboBox}/{@code Tanggal} wajib
 * {@code setPreferredSize()} eksplisit, {@code widget.Label} defaultnya rata
 * KANAN, dan {@code widget.Tanggal} buatan tangan wajib
 * {@code setDisplayFormat(...)}.</p>
 */
public final class RMResumeKeperawatanKebidanan extends javax.swing.JDialog {

    /**
     * Empat blok pada kertas: judul yang ditampilkan dan awalan nama kolomnya
     * di database. Dipakai BERSAMA oleh tata letak, {@link #KOLOM_RM33},
     * {@link #nilaiRm33()}, dan pemuatan record - satu sumber, empat pemakai.
     */
    private static final String[][] BLOK = {
        {"DIAGNOSA KEPERAWATAN / KEBIDANAN YANG TIMBUL", "dx_timbul"},
        {"TINDAKAN KEPERAWATAN / KEBIDANAN", "tindakan"},
        {"DIAGNOSA KEPERAWATAN / KEBIDANAN YANG SUDAH TERATASI", "dx_teratasi"},
        {"RENCANA TINDAK LANJUT / NASIHAT WAKTU PULANG", "rencana"}
    };

    /**
     * Lima baris per blok - ditetapkan oleh formulir fisik, yang memang
     * mencetak tepat lima baris bernomor untuk tiap blok.
     */
    private static final int BARIS_PER_BLOK = 5;

    /**
     * Nama kolom {@code resume_keperawatan_ranap} di luar {@code no_rawat},
     * dipakai bersama oleh {@link #simpan()} dan {@link #ganti()}.
     *
     * <p>Urutannya WAJIB sama persis dengan {@link #nilaiRm33()} - dijamin
     * karena keduanya dirakit dengan perulangan yang bentuknya identik atas
     * {@link #BLOK}.</p>
     */
    private static final String[] KOLOM_RM33 = rakitKolom();

    private static String[] rakitKolom() {
        List<String> k = new ArrayList<>();
        k.add("tanggal");
        k.add("jam");
        k.add("diagnosa_medis");
        k.add("keluhan_utama");
        for (String[] b : BLOK) {
            for (int n = 1; n <= BARIS_PER_BLOK; n++) {
                k.add(b[1] + n);
            }
        }
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
    private widget.TextBox TNoRw, TNoRM, TPasien, TglLahir, TUmur, Jk;
    private widget.TextBox TglMasukRS, TglKeluarRS, Ruangan;
    private widget.Tanggal TglResume;
    private widget.TextBox KdPetugas, NmPetugas;

    // ---------- Badan ----------
    private widget.TextBox DiagnosaMedis;
    private widget.TextArea KeluhanUtama;
    private final widget.TextBox[][] Isian = new widget.TextBox[BLOK.length][BARIS_PER_BLOK];

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

    public RMResumeKeperawatanKebidanan(java.awt.Frame parent, boolean modal) {
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
                "::[ Resume Keperawatan / Kebidanan (RM 33) ]::"));
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
        widget.PanelBiasa panelHeader = panelSeksi("IDENTITAS PASIEN (otomatis dari pendaftaran & kamar inap)");

        TNoRw = kotakBaca(140);
        TNoRM = kotakBaca(90);
        TPasien = kotakBaca(210);
        TglLahir = kotakBaca(90);
        TUmur = kotakBaca(110);
        Jk = kotakBaca(80);
        TglMasukRS = kotakBaca(110);
        TglKeluarRS = kotakBaca(110);
        Ruangan = kotakBaca(200);
        TglResume = tanggal("dd-MM-yyyy HH:mm:ss", 150);
        // 110px, bukan 80: NIP di produksi berbentuk "12/09/1988/001" dan
        // terpotong pada lebar 80.
        KdPetugas = kotakBaca(110);
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

        // Ketiganya dibaca dari kamar_inap, tidak pernah disimpan di tabel form
        // ini - lihat catatan kelas.
        panelHeader.add(labelKolom("Tanggal Masuk"), konstrain(0, row));
        panelHeader.add(baris(TglMasukRS, teks("   Tanggal Keluar :"), TglKeluarRS,
                teks("   Ruangan :"), Ruangan), konstrain(1, row));
        row++;

        panelHeader.add(labelKolom("Tanggal & Jam Resume"), konstrain(0, row));
        panelHeader.add(baris(TglResume,
                teks("   Perawat / Bidan :"), KdPetugas, NmPetugas), konstrain(1, row));

        tambahPengisiKolom(panelHeader, 2);
        tambahSeksi(panelHeader, 0);
    }

    // =================================================================
    //  BADAN
    // =================================================================

    private void rakitBadan() {
        int seksi = 1;

        widget.PanelBiasa panelAtas = panelSeksi("DIAGNOSA MEDIS & KELUHAN UTAMA");
        DiagnosaMedis = kotak(560);
        KeluhanUtama = new widget.TextArea();

        panelAtas.add(labelKolom("Diagnosa Medis"), konstrain(0, 0));
        panelAtas.add(DiagnosaMedis, konstrain(1, 0));
        panelAtas.add(labelKolom("Keluhan Utama"), konstrain(0, 1));
        GridBagConstraints c = konstrain(1, 1);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panelAtas.add(areaTeks(KeluhanUtama, 3, 560, 58), c);
        tambahPengisiKolom(panelAtas, 2);
        tambahSeksi(panelAtas, seksi++);

        // Empat blok, semuanya bentuknya sama: lima baris bernomor.
        for (int b = 0; b < BLOK.length; b++) {
            widget.PanelBiasa p = panelSeksi(BLOK[b][0]);
            for (int n = 0; n < BARIS_PER_BLOK; n++) {
                Isian[b][n] = kotak(620);
                p.add(labelKolom(String.valueOf(n + 1)), konstrain(0, n));
                p.add(Isian[b][n], konstrain(1, n));
            }
            tambahPengisiKolom(p, 2);
            tambahSeksi(p, seksi++);
        }

        tambahPengisiBaris(FormInput, seksi);
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
        MnCetak.setText("Cetak Resume Keperawatan / Kebidanan");
        MnCetak.addActionListener(evt -> cetakTerpilih());
        jPopupMenu1.add(MnCetak);

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
        PanelAccor.setPreferredSize(new Dimension(470, 44));
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

    /**
     * Nilai yang dikirim ke database, urutannya sama persis dengan
     * {@link #KOLOM_RM33} - dijamin karena perulangan atas {@link #BLOK} di
     * sini bentuknya identik dengan yang di {@link #rakitKolom()}.
     */
    private String[] nilaiRm33() {
        List<String> v = new ArrayList<>();
        v.add(Valid.SetTgl(TglResume.getSelectedItem() + ""));
        v.add(TglResume.getSelectedItem().toString().substring(11, 19));
        v.add(kosongKeNull(DiagnosaMedis.getText()));
        v.add(kosongKeNull(KeluhanUtama.getText()));
        for (int b = 0; b < BLOK.length; b++) {
            for (int n = 0; n < BARIS_PER_BLOK; n++) {
                v.add(kosongKeNull(Isian[b][n].getText()));
            }
        }
        v.add(KdPetugas.getText());
        return v.toArray(new String[0]);
    }

    /** Baris yang tidak diisi disimpan NULL, bukan string kosong. */
    private String kosongKeNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
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
        for (String k : KOLOM_RM33) {
            kol.append(",").append(k);
            tanya.append(",?");
        }
        try (PreparedStatement pstm = koneksi.prepareStatement(
                "insert into resume_keperawatan_ranap(" + kol + ") values(" + tanya + ")")) {
            pstm.setString(1, TNoRw.getText());
            String[] nilai = nilaiRm33();
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
        for (String k : KOLOM_RM33) {
            if (set.length() > 0) {
                set.append(",");
            }
            set.append(k).append("=?");
        }
        String[] nilai = nilaiRm33();
        String[] param = new String[nilai.length + 1];
        System.arraycopy(nilai, 0, param, 0, nilai.length);
        param[nilai.length] = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();

        if (Sequel.mengedittf("resume_keperawatan_ranap", "no_rawat=?", set.toString(),
                param.length, param)) {
            runBackground(() -> tampil());
            TabRawat.setSelectedIndex(1);
        }
    }

    private void aksiHapus() {
        if (tbObat.getSelectedRow() > -1) {
            if (Sequel.queryu2tf("delete from resume_keperawatan_ranap where no_rawat=?", 1,
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
                + "from resume_keperawatan_ranap a "
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
            Valid.SetTgl2(TglResume, tgl + " " + (jam.isEmpty() ? "00:00:00" : jam));
        }
        DiagnosaMedis.setText(teksAman(rs.getString("diagnosa_medis")));
        KeluhanUtama.setText(teksAman(rs.getString("keluhan_utama")));
        for (int b = 0; b < BLOK.length; b++) {
            for (int n = 0; n < BARIS_PER_BLOK; n++) {
                Isian[b][n].setText(teksAman(rs.getString(BLOK[b][1] + (n + 1))));
            }
        }
        String nip = teksAman(rs.getString("nip"));
        KdPetugas.setText(nip);
        NmPetugas.setText(nip.isEmpty() ? ""
                : teksAman(Sequel.cariIsi("select nama from petugas where nip=?", nip)));
    }

    private String teksAman(String s) {
        return s == null ? "" : s;
    }

    public void emptTeks() {
        DiagnosaMedis.setText("");
        KeluhanUtama.setText("");
        for (int b = 0; b < BLOK.length; b++) {
            for (int n = 0; n < BARIS_PER_BLOK; n++) {
                Isian[b][n].setText("");
            }
        }
        TglResume.setDate(new Date());
        TabRawat.setSelectedIndex(0);
    }

    /**
     * Identitas pasien dari master, plus tanggal masuk/keluar dan ruangan dari
     * {@code kamar_inap}. Tidak satu pun disimpan di tabel form ini - selalu
     * dibaca ulang supaya lembar mengikuti data yang berlaku.
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
        muatKamarInap();
    }

    /**
     * Tanggal masuk, tanggal keluar, dan ruangan dari {@code kamar_inap}.
     *
     * <p>Pasien yang pindah kamar punya BEBERAPA baris. Aturannya: tanggal
     * masuk diambil dari baris paling AWAL (subquery {@code min}), sedangkan
     * ruangan dan tanggal keluar dari baris TERAKHIR - kalau ruangan diambil
     * dari baris pertama, lembar pulang akan menyebut kamar yang sudah lama
     * ditinggalkan pasien.</p>
     *
     * <p>{@code tgl_keluar='0000-00-00'} berarti pasien BELUM pulang; itu
     * ditampilkan apa adanya sebagai keterangan, bukan sebagai tanggal.</p>
     */
    private void muatKamarInap() {
        TglMasukRS.setText("");
        TglKeluarRS.setText("");
        Ruangan.setText("");
        try {
            ps = koneksi.prepareStatement(sqlKamarInap());
            try {
                ps.setString(1, TNoRw.getText());
                ps.setString(2, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    TglMasukRS.setText(teksAman(rs.getString("tgl_masuk")));
                    String keluar = teksAman(rs.getString("tgl_keluar"));
                    TglKeluarRS.setText(keluar.isEmpty() ? "(belum pulang)" : keluar);
                    Ruangan.setText(teksAman(rs.getString("ruangan")));
                    // Diagnosa medis hanya DIUSULKAN; kalau perawat sudah
                    // menuliskan sendiri, jangan ditimpa.
                    if (DiagnosaMedis.getText().trim().isEmpty()) {
                        DiagnosaMedis.setText(teksAman(rs.getString("diagnosa_medis")));
                    }
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

    private String sqlKamarInap() {
        return "select (select date_format(min(tgl_masuk),'%d-%m-%Y') from kamar_inap "
                + "where no_rawat=?) as tgl_masuk,"
                + "if(ki.tgl_keluar='0000-00-00','',date_format(ki.tgl_keluar,'%d-%m-%Y')) as tgl_keluar,"
                + "bangsal.nm_bangsal as ruangan,"
                + "ifnull(nullif(ki.diagnosa_akhir,''),ki.diagnosa_awal) as diagnosa_medis "
                + "from kamar_inap ki "
                + "inner join kamar on ki.kd_kamar=kamar.kd_kamar "
                + "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "
                + "where ki.no_rawat=? "
                + "order by ki.tgl_masuk desc,ki.jam_masuk desc limit 1";
    }

    /**
     * Keluhan utama diusulkan dari asesmen awal keperawatan rawat inap kalau
     * perawat belum menuliskan apa pun. Hanya usulan - begitu diketik ulang,
     * yang diketik itulah yang disimpan.
     */
    private void usulKeluhanUtama() {
        if (!KeluhanUtama.getText().trim().isEmpty()) {
            return;
        }
        String keluhan = Sequel.cariIsi(
                "select keluhan_utama from penilaian_awal_keperawatan_ranap where no_rawat=?",
                TNoRw.getText());
        KeluhanUtama.setText(teksAman(keluhan));
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
        barisHtml(h, "Masuk / Keluar", TglMasukRS.getText() + " s.d " + TglKeluarRS.getText());
        barisHtml(h, "Ruangan", Ruangan.getText());
        barisHtml(h, "Tanggal & Jam Resume", String.valueOf(TglResume.getSelectedItem()));
        barisHtml(h, "Perawat / Bidan", NmPetugas.getText());

        judulHtml(h, "DIAGNOSA MEDIS & KELUHAN UTAMA");
        barisHtml(h, "Diagnosa Medis", DiagnosaMedis.getText());
        barisHtml(h, "Keluhan Utama", KeluhanUtama.getText());

        // Baris kosong tidak ditampilkan - lima nomor bergaris kosong tidak
        // menambah informasi apa pun di panel sesempit ini.
        for (int b = 0; b < BLOK.length; b++) {
            judulHtml(h, BLOK[b][0]);
            boolean adaIsi = false;
            for (int n = 0; n < BARIS_PER_BLOK; n++) {
                String isi = Isian[b][n].getText().trim();
                if (!isi.isEmpty()) {
                    barisHtml(h, String.valueOf(n + 1), isi);
                    adaIsi = true;
                }
            }
            if (!adaIsi) {
                barisHtml(h, "-", "<i>belum diisi</i>");
            }
        }

        h.append("</table></html>");
        LoadHTML.setText(h.toString());
        LoadHTML.setCaretPosition(0);
    }

    private void judulHtml(StringBuilder h, String judul) {
        h.append("<tr class='isi'><td colspan='2' bgcolor='#FFFAF8'><b>").append(judul).append("</b></td></tr>");
    }

    private void barisHtml(StringBuilder h, String label, String nilai) {
        h.append("<tr class='isi'><td width='30%' valign='top'>").append(label)
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
            Valid.MyReportqrypdf("rptResumeKeperawatanKebidananRM33.jasper", "report",
                    "::[ Resume Keperawatan / Kebidanan ]::", qry, param);
        } else {
            Valid.MyReportqry("rptResumeKeperawatanKebidananRM33.jasper", "report",
                    "::[ Resume Keperawatan / Kebidanan ]::", qry, param);
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

    /**
     * Query cetak. Tanggal masuk/keluar dan ruangan dirakit lewat subquery yang
     * aturannya SAMA dengan {@link #sqlKamarInap()} - masuk dari baris paling
     * awal, ruangan &amp; keluar dari baris terakhir - supaya yang tercetak
     * sama persis dengan yang tampil di form.
     */
    private String sqlCetak(String noRawat) {
        return "select a.*,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                + "concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,"
                + "date_format(a.tanggal,'%d-%m-%Y') as tgl_resume,"
                + "(select date_format(min(tgl_masuk),'%d-%m-%Y') from kamar_inap "
                + "where no_rawat=a.no_rawat) as tgl_masuk_rs,"
                + "ifnull((select if(ki.tgl_keluar='0000-00-00','',date_format(ki.tgl_keluar,'%d-%m-%Y')) "
                + "from kamar_inap ki where ki.no_rawat=a.no_rawat "
                + "order by ki.tgl_masuk desc,ki.jam_masuk desc limit 1),'') as tgl_keluar_rs,"
                + "ifnull((select bangsal.nm_bangsal from kamar_inap ki "
                + "inner join kamar on ki.kd_kamar=kamar.kd_kamar "
                + "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "
                + "where ki.no_rawat=a.no_rawat "
                + "order by ki.tgl_masuk desc,ki.jam_masuk desc limit 1),'') as ruangan,"
                + "ifnull(petugas.nama,'') as nm_petugas "
                + "from resume_keperawatan_ranap a "
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
        } else {
            usulKeluhanUtama();
        }
        bangunRingkasanHtml();
    }

    private boolean adaRecord(String noRawat) {
        String ada = Sequel.cariIsi(
                "select no_rawat from resume_keperawatan_ranap where no_rawat=?", noRawat);
        return ada != null && !ada.trim().isEmpty();
    }

    private void muatRecord(String noRawat) {
        try {
            ps = koneksi.prepareStatement("select * from resume_keperawatan_ranap where no_rawat=?");
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
     * Simpan/Hapus/Ganti tidak pernah dinonaktifkan. Sama seperti RM 21,
     * berbeda dari RM 20 dan RM 17-01 yang keduanya memakai hak akses.
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
