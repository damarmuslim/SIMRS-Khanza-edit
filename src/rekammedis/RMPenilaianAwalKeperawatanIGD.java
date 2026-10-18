/*
 * Kontribusi dari Abdul Wahid, RSUD Cipayung Jakarta Timur
 */

package rekammedis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import widget.ModernStyle;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPetugas;

/**
 *
 * @author perpustakaan
 */
public final class RMPenilaianAwalKeperawatanIGD extends javax.swing.JDialog {
    private final DefaultTableModel tabMode, tabModeMasalah, tabModeDetailMasalah, tabModeRencana, tabModeDetailRencana;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps, ps2;
    private ResultSet rs, rs2;
    private int i = 0, jml = 0, index = 0;
    private DlgCariPetugas petugas;
    private MasterMasalahKeperawatanIGD masalahkeperawatan;
    private MasterRencanaKeperawatanIGD rencanakeperawatan;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private boolean[] pilih;
    private String[] kode, masalah;
    private String masalahkeperawatanigd = "", finger = "";
    private File file;
    private FileWriter fileWriter;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private FileReader myObj;
    private String TANGGALMUNDUR = "yes";

    /**
     * Creates new form DlgRujuk
     * 
     * @param parent
     * @param modal
     */
    public RMPenilaianAwalKeperawatanIGD(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        widget.IconStyle.pasang(this);
        ModernStyle.terapkan(this);
        initKepTambahan();
        initKepTambahanHal2();
        initKepTambahanHal3();
        initObatInfusTambahan();
        initKepTambahanHal4();
        // [Bagian A/C/D] Restyle action bar + susun ulang FormInput jadi
        // tumpukan kartu HALAMAN, lalu rakit rangka 4 zona (bar pasien atas /
        // rail kiri / panel halaman tengah / action bar bawah). WAJIB
        // dipanggil PALING akhir (butuh field Dokter dari initKepTambahan &
        // panelKepTigaDiag dari initKepTambahanHal3), dan susunRangkaKep()
        // sesudah susunHalamanKep() karena memakai FormInput yg sudah
        // tersusun. Hanya menata ulang layout - komponen & logika data
        // (tampil/getData/simpan/dst) tidak diubah.
        initActionBarKep();
        susunHalamanKep();
        susunRangkaKep();
        buatKotakKep(internalFrame1);
        buatKotakKep(FormInput);
        buatKotakKep(barPasienKep);
        pasangTopBarKep();
        widget.IconStyle.pasang(this);
        javax.swing.Icon icPrintHtml = widget.IconStyle.muat("printer.svg", 14, widget.IconStyle.ABU);
        if (icPrintHtml != null) {
            BtnPrint1.setIcon(icPrintHtml);
            BtnPrint1.setToolTipText("Cetak Pengkajian (Alt+T)");
        }
        javax.swing.Icon icPencarian = widget.IconStyle.muat("search.svg", 14, widget.IconStyle.ABU);
        if (icPencarian != null) {
            BtnCari.setIcon(icPencarian);
        }
        // Samakan pola filter dengan Triase IGD: satu tombol kaca pembesar
        // untuk menerapkan tanggal + kata kunci. Reset tetap lewat "Semua".
        panelGlass9.remove(BtnAllCari);
        panelGlass9.revalidate();
        panelGlass9.repaint();
        pasangSemuaListenerProgresKep();
        hitungProgresKep();

        // Header + width setup di bawah dulunya dideklarasikan 81 kolom
        // (sisa desain lama sebelum tampil() di-refactor jadi SELECT
        // ringkas) - dirapikan jadi 7 kolom yg persis sesuai yg diisi
        // tampil() (no_rawat,no_rkm_medis,nm_pasien,tgl_lahir,tanggal,
        // nip,nama), pola sama seperti tabMode di Triase.
        tabMode = new DefaultTableModel(null, new Object[] {
                "No.Rawat", "No.RM", "Nama Pasien", "Tgl.Lahir", "Tanggal", "NIP", "Nama Petugas"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat.setModel(tabMode);

        // tbObat.setDefaultRenderer(Object.class, new
        // WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 7; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(105);
            } else if (i == 1) {
                column.setPreferredWidth(65);
            } else if (i == 2) {
                column.setPreferredWidth(160);
            } else if (i == 3) {
                column.setPreferredWidth(65);
            } else if (i == 4) {
                column.setPreferredWidth(120);
            } else if (i == 5) {
                column.setPreferredWidth(90);
            } else if (i == 6) {
                column.setPreferredWidth(150);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        tabModeMasalah = new DefaultTableModel(null, new Object[] {
                "P", "KODE", "MASALAH KEPERAWATAN"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if (colIndex == 0) {
                    a = true;
                }
                return a;
            }

            Class[] types = new Class[] {
                    java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Double.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbMasalahKeperawatan.setModel(tabModeMasalah);

        // tbObat.setDefaultRenderer(Object.class, new
        // WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbMasalahKeperawatan.setPreferredScrollableViewportSize(new Dimension(500, 500));
        // [Bagian D] Bukan OFF: dgn OFF lebar tabel = jumlah lebar kolomnya
        // (20+0+350 = 370px), jadi begitu scroll pane-nya dilebarkan ke selebar
        // halaman, sisa ~820px jadi bilah header gelap tanpa kolom di bawahnya
        // (kelihatan di render hal-10). Kolom yg menyerap sisa lebar ditentukan
        // oleh batas min/max di bawah, bukan oleh nama mode ini. Model, isi &
        // editable tidak berubah sama sekali.
        tbMasalahKeperawatan.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        for (i = 0; i < 3; i++) {
            TableColumn column = tbMasalahKeperawatan.getColumnModel().getColumn(i);
            if (i == 0) {
                // min=max=20 WAJIB, bukan cuma preferred. Begitu autoResizeMode
                // bukan OFF, JTable.doLayout() membagi kelebihan lebar ke SEMUA
                // kolom yg batas atasnya masih longgar (setWidthsFromPreferredWidths),
                // bukan cuma ke kolom terakhir - nama AUTO_RESIZE_LAST_COLUMN
                // hanya berlaku saat kolom digeser manual. Tanpa pagar ini kolom
                // centang "P" ikut melar jadi 430px (kelihatan di render hal-10).
                column.setMinWidth(20);
                column.setMaxWidth(20);
                column.setPreferredWidth(20);
            } else if (i == 1) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 2) {
                column.setPreferredWidth(350);
            }
        }
        // Gaya tabel form ini (kepala terang, baris 28px, seleksi 0xF2F7FB) -
        // lihat javadoc gayaTabelKep(). Model, isi, editable, lebar kolom dan
        // kolom Boolean "P" tidak berubah sama sekali.
        //
        // fungsi.WarnaTable diganti rendererSelKepTabel() KHUSUS instans ini,
        // bukan karena zebra-nya jelek, tapi karena WarnaTable memaksa warna
        // seleksi navy pada sel yg dirender-nya sementara kolom centang
        // dipegang renderer Boolean bawaan yg menuruti tabel - satu baris
        // terpilih akan jadi dua warna. Kelas WarnaTable tidak disentuh.
        gayaTabelKep(tbMasalahKeperawatan, 28);
        tbMasalahKeperawatan.setDefaultRenderer(Object.class, rendererSelKepTabel());

        tabModeDetailMasalah = new DefaultTableModel(null, new Object[] {
                "Kode", "Masalah Keperawatan"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbMasalahDetailMasalah.setModel(tabModeDetailMasalah);

        // tbObat.setDefaultRenderer(Object.class, new
        // WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbMasalahDetailMasalah.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbMasalahDetailMasalah.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 2; i++) {
            TableColumn column = tbMasalahDetailMasalah.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 1) {
                column.setPreferredWidth(420);
            }
        }
        tbMasalahDetailMasalah.setDefaultRenderer(Object.class, new WarnaTable());

        tabModeRencana = new DefaultTableModel(null, new Object[] {
                "P", "KODE", "RENCANA KEPERAWATAN"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if (colIndex == 0) {
                    a = true;
                }
                return a;
            }

            Class[] types = new Class[] {
                    java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Double.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbRencanaKeperawatan.setModel(tabModeRencana);

        // tbObat.setDefaultRenderer(Object.class, new
        // WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbRencanaKeperawatan.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbRencanaKeperawatan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 3; i++) {
            TableColumn column = tbRencanaKeperawatan.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(20);
            } else if (i == 1) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 2) {
                column.setPreferredWidth(350);
            }
        }
        tbRencanaKeperawatan.setDefaultRenderer(Object.class, new WarnaTable());

        tabModeDetailRencana = new DefaultTableModel(null, new Object[] {
                "Kode", "Rencana Keperawatan"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbRencanaDetail.setModel(tabModeDetailRencana);

        // tbObat.setDefaultRenderer(Object.class, new
        // WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbRencanaDetail.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbRencanaDetail.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 2; i++) {
            TableColumn column = tbRencanaDetail.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 1) {
                column.setPreferredWidth(420);
            }
        }
        tbRencanaDetail.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte) 17).getKata(TNoRw));
        KeluhanUtama.setDocument(new batasInput((int) 150).getKata(KeluhanUtama));
        RPD.setDocument(new batasInput((int) 100).getKata(RPD));
        RPO.setDocument(new batasInput((int) 100).getKata(RPO));
        Gravida.setDocument(new batasInput((byte) 20).getKata(Gravida));
        Para.setDocument(new batasInput((byte) 20).getKata(Para));
        Abortus.setDocument(new batasInput((byte) 20).getKata(Abortus));
        HPHT.setDocument(new batasInput((byte) 20).getKata(HPHT));
        JumlahPerdarahan.setDocument(new batasInput((byte) 5).getKata(JumlahPerdarahan));
        WarnaPerdarahan.setDocument(new batasInput((int) 40).getKata(WarnaPerdarahan));
        BAB.setDocument(new batasInput((byte) 2).getKata(BAB));
        XBAB.setDocument(new batasInput((byte) 10).getKata(XBAB));
        KBAB.setDocument(new batasInput((int) 40).getKata(KBAB));
        WBAB.setDocument(new batasInput((int) 40).getKata(WBAB));
        BAK.setDocument(new batasInput((byte) 2).getKata(BAK));
        XBAK.setDocument(new batasInput((byte) 10).getKata(XBAK));
        WBAK.setDocument(new batasInput((int) 40).getKata(WBAK));
        LBAK.setDocument(new batasInput((int) 40).getKata(LBAK));
        Dilaporkan.setDocument(new batasInput((int) 50).getKata(Dilaporkan));
        Sebutkan.setDocument(new batasInput((int) 50).getKata(Sebutkan));
        KetTinggal.setDocument(new batasInput((int) 50).getKata(KetTinggal));
        KetBudaya.setDocument(new batasInput((int) 50).getKata(KetBudaya));
        KetPendidikanPJ.setDocument(new batasInput((int) 50).getKata(KetPendidikanPJ));
        KetEdukasi.setDocument(new batasInput((int) 50).getKata(KetEdukasi));
        KetAlatBantu.setDocument(new batasInput((int) 50).getKata(KetAlatBantu));
        KetProvokes.setDocument(new batasInput((int) 40).getKata(KetProvokes));
        KetQuality.setDocument(new batasInput((int) 50).getKata(KetQuality));
        Lokasi.setDocument(new batasInput((int) 50).getKata(Lokasi));
        Durasi.setDocument(new batasInput((int) 25).getKata(Durasi));
        KetNyeri.setDocument(new batasInput((int) 40).getKata(KetNyeri));
        KetDokter.setDocument(new batasInput((byte) 15).getKata(KetDokter));
        KetLapor.setDocument(new batasInput((int) 15).getKata(KetLapor));
        Rencana.setDocument(new batasInput((int) 200).getKata(Rencana));
        TCari.setDocument(new batasInput((int) 100).getKata(TCari));

        HTMLEditorKit kit = new HTMLEditorKit();
        // Panel ini hanya menampilkan ringkasan record yang dipilih. Menjaganya
        // read-only mencegah pengguna mengira perubahan teks di sini tersimpan.
        LoadHTML.setEditable(false);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                        +
                        ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}" +
                        ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                        +
                        ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                        +
                        ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}" +
                        ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}" +
                        ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}" +
                        ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}" +
                        ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}");
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);

        try {
            TANGGALMUNDUR = koneksiDB.TANGGALMUNDUR();
        } catch (Exception e) {
            TANGGALMUNDUR = "yes";
        }

        ChkAccor.setSelected(false);
        isMenu();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LoadHTML = new widget.editorpane();
        TanggalRegistrasi = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        label14 = new widget.Label();
        KdPetugas = new widget.TextBox();
        NmPetugas = new widget.TextBox();
        BtnPetugas = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        jLabel9 = new widget.Label();
        Jk = new widget.TextBox();
        jLabel10 = new widget.Label();
        label11 = new widget.Label();
        jLabel11 = new widget.Label();
        jLabel50 = new widget.Label();
        jLabel52 = new widget.Label();
        scrollPane1 = new widget.ScrollPane();
        KeluhanUtama = new widget.TextArea();
        jLabel30 = new widget.Label();
        scrollPane2 = new widget.ScrollPane();
        RPD = new widget.TextArea();
        jLabel31 = new widget.Label();
        scrollPane4 = new widget.ScrollPane();
        RPO = new widget.TextArea();
        Aktifitas = new widget.ComboBox();
        AlatBantu = new widget.ComboBox();
        KetAlatBantu = new widget.TextBox();
        jLabel55 = new widget.Label();
        ADL = new widget.ComboBox();
        jLabel57 = new widget.Label();
        jLabel58 = new widget.Label();
        TinggalDengan = new widget.ComboBox();
        KetTinggal = new widget.TextBox();
        jLabel60 = new widget.Label();
        Edukasi = new widget.ComboBox();
        KetEdukasi = new widget.TextBox();
        jLabel64 = new widget.Label();
        jLabel65 = new widget.Label();
        jLabel66 = new widget.Label();
        Lapor = new widget.ComboBox();
        ATS = new widget.ComboBox();
        BJM = new widget.ComboBox();
        jLabel67 = new widget.Label();
        Hasil = new widget.ComboBox();
        jLabel68 = new widget.Label();
        KetLapor = new widget.TextBox();
        jLabel70 = new widget.Label();
        jLabel72 = new widget.Label();
        MSA = new widget.ComboBox();
        Nyeri = new widget.ComboBox();
        Provokes = new widget.ComboBox();
        KetProvokes = new widget.TextBox();
        jLabel80 = new widget.Label();
        Quality = new widget.ComboBox();
        KetQuality = new widget.TextBox();
        jLabel81 = new widget.Label();
        jLabel82 = new widget.Label();
        Lokasi = new widget.TextBox();
        jLabel83 = new widget.Label();
        Menyebar = new widget.ComboBox();
        jLabel84 = new widget.Label();
        jLabel85 = new widget.Label();
        SkalaNyeri = new widget.ComboBox();
        jLabel86 = new widget.Label();
        Durasi = new widget.TextBox();
        jLabel87 = new widget.Label();
        jLabel88 = new widget.Label();
        NyeriHilang = new widget.ComboBox();
        KetNyeri = new widget.TextBox();
        jLabel89 = new widget.Label();
        PadaDokter = new widget.ComboBox();
        KetDokter = new widget.TextBox();
        TglAsuhan = new widget.Tanggal();
        jLabel94 = new widget.Label();
        jLabel51 = new widget.Label();
        CacatFisik = new widget.TextBox();
        jLabel56 = new widget.Label();
        jLabel95 = new widget.Label();
        StatusBudaya = new widget.ComboBox();
        KetBudaya = new widget.TextBox();
        jLabel97 = new widget.Label();
        jLabel63 = new widget.Label();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();
        PanelWall = new usu.widget.glass.PanelGlass();
        jSeparator9 = new javax.swing.JSeparator();
        jLabel71 = new widget.Label();
        jSeparator10 = new javax.swing.JSeparator();
        Bahasa = new widget.TextBox();
        jLabel76 = new widget.Label();
        jLabel77 = new widget.Label();
        Agama = new widget.TextBox();
        jLabel78 = new widget.Label();
        StatusKehamilan = new widget.ComboBox();
        jLabel29 = new widget.Label();
        Gravida = new widget.TextBox();
        jLabel32 = new widget.Label();
        Para = new widget.TextBox();
        jLabel33 = new widget.Label();
        Abortus = new widget.TextBox();
        jLabel35 = new widget.Label();
        HPHT = new widget.TextBox();
        jSeparator11 = new javax.swing.JSeparator();
        jLabel98 = new widget.Label();
        jLabel90 = new widget.Label();
        Tekanan = new widget.ComboBox();
        jLabel91 = new widget.Label();
        Pupil = new widget.ComboBox();
        Neurosensorik = new widget.ComboBox();
        jLabel100 = new widget.Label();
        jLabel101 = new widget.Label();
        Integumen = new widget.ComboBox();
        jLabel102 = new widget.Label();
        Turgor = new widget.ComboBox();
        jLabel103 = new widget.Label();
        Edema = new widget.ComboBox();
        jLabel104 = new widget.Label();
        Mukosa = new widget.ComboBox();
        jLabel105 = new widget.Label();
        Perdarahan = new widget.ComboBox();
        jLabel36 = new widget.Label();
        JumlahPerdarahan = new widget.TextBox();
        jLabel37 = new widget.Label();
        WarnaPerdarahan = new widget.TextBox();
        jLabel38 = new widget.Label();
        jLabel106 = new widget.Label();
        Intoksikasi = new widget.ComboBox();
        jLabel107 = new widget.Label();
        jLabel108 = new widget.Label();
        KBAB = new widget.TextBox();
        jLabel109 = new widget.Label();
        BAB = new widget.TextBox();
        jLabel110 = new widget.Label();
        XBAB = new widget.TextBox();
        jLabel111 = new widget.Label();
        WBAB = new widget.TextBox();
        jLabel112 = new widget.Label();
        BAK = new widget.TextBox();
        jLabel113 = new widget.Label();
        XBAK = new widget.TextBox();
        jLabel114 = new widget.Label();
        WBAK = new widget.TextBox();
        jLabel115 = new widget.Label();
        LBAK = new widget.TextBox();
        jSeparator12 = new javax.swing.JSeparator();
        jLabel116 = new widget.Label();
        jLabel117 = new widget.Label();
        Psikologis = new widget.ComboBox();
        jLabel119 = new widget.Label();
        Jiwa = new widget.ComboBox();
        jLabel120 = new widget.Label();
        Perilaku = new widget.ComboBox();
        jLabel118 = new widget.Label();
        Dilaporkan = new widget.TextBox();
        jLabel121 = new widget.Label();
        Sebutkan = new widget.TextBox();
        jLabel122 = new widget.Label();
        Hubungan = new widget.ComboBox();
        jLabel123 = new widget.Label();
        StatusPernikahan = new widget.TextBox();
        jLabel124 = new widget.Label();
        Pekerjaan = new widget.TextBox();
        jLabel125 = new widget.Label();
        Pembayaran = new widget.TextBox();
        jLabel126 = new widget.Label();
        PendidikanPasien = new widget.TextBox();
        jLabel127 = new widget.Label();
        PendidikanPJ = new widget.ComboBox();
        KetPendidikanPJ = new widget.TextBox();
        jLabel39 = new widget.Label();
        Informasi = new widget.ComboBox();
        Scroll8 = new widget.ScrollPane();
        tbMasalahKeperawatan = new widget.Table();
        BtnTambahMasalah = new widget.Button();
        BtnAllMasalah = new widget.Button();
        BtnCariMasalah = new widget.Button();
        TCariMasalah = new widget.TextBox();
        label12 = new widget.Label();
        TabRencanaKeperawatan = new javax.swing.JTabbedPane();
        panelBiasa1 = new widget.PanelBiasa();
        Scroll9 = new widget.ScrollPane();
        tbRencanaKeperawatan = new widget.Table();
        scrollPane5 = new widget.ScrollPane();
        Rencana = new widget.TextArea();
        label13 = new widget.Label();
        TCariRencana = new widget.TextBox();
        BtnCariRencana = new widget.Button();
        BtnAllRencana = new widget.Button();
        BtnTambahRencana = new widget.Button();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAllCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        PanelAccor = new widget.PanelBiasa();
        ChkAccor = new widget.CekBox();
        FormMenu = new widget.PanelBiasa();
        jLabel34 = new widget.Label();
        TNoRM1 = new widget.TextBox();
        TPasien1 = new widget.TextBox();
        BtnPrint1 = new widget.Button();
        FormMasalahRencana = new widget.PanelBiasa();
        Scroll7 = new widget.ScrollPane();
        tbMasalahDetailMasalah = new widget.Table();
        Scroll10 = new widget.ScrollPane();
        tbRencanaDetail = new widget.Table();
        scrollPane6 = new widget.ScrollPane();
        DetailRencana = new widget.TextArea();

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        TanggalRegistrasi.setHighlighter(null);
        TanggalRegistrasi.setName("TanggalRegistrasi"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Pengkajian Awal Keperawatan IGD ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11),
                new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        BtnSimpan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal"); // NOI18N
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalActionPerformed(evt);
            }
        });
        BtnBatal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBatalKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        BtnHapus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapusKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setName("BtnEdit"); // NOI18N
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        BtnEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnEditKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnEdit);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnPrint);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnAll);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnKeluar);

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 557));

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(870, 1203));
        FormInput.setLayout(null);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(74, 10, 131, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TPasienActionPerformed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(309, 10, 260, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(207, 10, 100, 23);

        label14.setText("Petugas :");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label14);
        label14.setBounds(0, 40, 70, 23);

        KdPetugas.setEditable(false);
        KdPetugas.setName("KdPetugas"); // NOI18N
        KdPetugas.setPreferredSize(new java.awt.Dimension(80, 23));
        KdPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdPetugasKeyPressed(evt);
            }
        });
        FormInput.add(KdPetugas);
        KdPetugas.setBounds(74, 40, 100, 23);

        NmPetugas.setEditable(false);
        NmPetugas.setName("NmPetugas"); // NOI18N
        NmPetugas.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmPetugas);
        NmPetugas.setBounds(176, 40, 180, 23);

        BtnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPetugas.setMnemonic('2');
        BtnPetugas.setToolTipText("Alt+2");
        BtnPetugas.setName("BtnPetugas"); // NOI18N
        BtnPetugas.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPetugasActionPerformed(evt);
            }
        });
        BtnPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPetugasKeyPressed(evt);
            }
        });
        FormInput.add(BtnPetugas);
        BtnPetugas.setBounds(358, 40, 28, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(580, 10, 60, 23);

        TglLahir.setEditable(false);
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(644, 10, 80, 23);

        jLabel9.setText("Riwayat Penggunaan Obat :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(440, 90, 150, 23);

        Jk.setEditable(false);
        Jk.setHighlighter(null);
        Jk.setName("Jk"); // NOI18N
        FormInput.add(Jk);
        Jk.setBounds(774, 10, 80, 23);

        jLabel10.setText("No.Rawat :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 10, 70, 23);

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label11);
        label11.setBounds(395, 40, 57, 23);

        jLabel11.setText("J.K. :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(740, 10, 30, 23);

        jLabel50.setText("d. Alat Bantu :");
        jLabel50.setName("jLabel50"); // NOI18N
        FormInput.add(jLabel50);
        jLabel50.setBounds(1, 650, 112, 23);

        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel52.setText("SKALA NYERI");
        jLabel52.setName("jLabel52"); // NOI18N
        FormInput.add(jLabel52);
        jLabel52.setBounds(10, 680, 380, 23);

        scrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane1.setName("scrollPane1"); // NOI18N

        KeluhanUtama.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        KeluhanUtama.setColumns(20);
        KeluhanUtama.setRows(5);
        KeluhanUtama.setName("KeluhanUtama"); // NOI18N
        KeluhanUtama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeluhanUtamaKeyPressed(evt);
            }
        });
        scrollPane1.setViewportView(KeluhanUtama);

        FormInput.add(scrollPane1);
        scrollPane1.setBounds(179, 90, 260, 53);

        jLabel30.setText("Riwayat Penyakit Sekarang :");
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(0, 90, 175, 20);

        scrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane2.setName("scrollPane2"); // NOI18N

        RPD.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RPD.setColumns(20);
        RPD.setRows(5);
        RPD.setName("RPD"); // NOI18N
        RPD.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RPDKeyPressed(evt);
            }
        });
        scrollPane2.setViewportView(RPD);

        FormInput.add(scrollPane2);
        scrollPane2.setBounds(179, 150, 260, 53);

        jLabel31.setText("Riwayat Penyakit Dahulu :");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(0, 150, 175, 23);

        scrollPane4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane4.setName("scrollPane4"); // NOI18N

        RPO.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RPO.setColumns(20);
        RPO.setRows(5);
        RPO.setName("RPO"); // NOI18N
        RPO.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RPOKeyPressed(evt);
            }
        });
        scrollPane4.setViewportView(RPO);

        FormInput.add(scrollPane4);
        scrollPane4.setBounds(594, 90, 260, 53);

        Aktifitas.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tirah Baring", "Duduk", "Berjalan" }));
        Aktifitas.setName("Aktifitas"); // NOI18N
        Aktifitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AktifitasKeyPressed(evt);
            }
        });
        FormInput.add(Aktifitas);
        Aktifitas.setBounds(490, 620, 112, 23);

        AlatBantu.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        AlatBantu.setName("AlatBantu"); // NOI18N
        AlatBantu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AlatBantuKeyPressed(evt);
            }
        });
        FormInput.add(AlatBantu);
        AlatBantu.setBounds(117, 650, 80, 23);

        KetAlatBantu.setFocusTraversalPolicyProvider(true);
        KetAlatBantu.setName("KetAlatBantu"); // NOI18N
        KetAlatBantu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetAlatBantuKeyPressed(evt);
            }
        });
        FormInput.add(KetAlatBantu);
        KetAlatBantu.setBounds(200, 650, 402, 23);

        jLabel55.setText("b. Aktifitas :");
        jLabel55.setName("jLabel55"); // NOI18N
        FormInput.add(jLabel55);
        jLabel55.setBounds(406, 620, 80, 23);

        ADL.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Mandiri", "Bantuan Minimal", "Bantuan Sebagian", "Ketergantungan Total" }));
        ADL.setSelectedIndex(3);
        ADL.setName("ADL"); // NOI18N
        ADL.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ADLKeyPressed(evt);
            }
        });
        FormInput.add(ADL);
        ADL.setBounds(227, 620, 160, 23);

        jLabel57.setText("a. Kemampuan Aktifitas Sehari-hari :");
        jLabel57.setName("jLabel57"); // NOI18N
        FormInput.add(jLabel57);
        jLabel57.setBounds(0, 620, 223, 23);

        jLabel58.setText("n. Edukasi Diberikan Kepada :");
        jLabel58.setName("jLabel58"); // NOI18N
        FormInput.add(jLabel58);
        jLabel58.setBounds(437, 570, 160, 23);

        TinggalDengan.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Sendiri", "Orang Tua", "Suami / Istri", "Lainnya" }));
        TinggalDengan.setName("TinggalDengan"); // NOI18N
        TinggalDengan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TinggalDenganKeyPressed(evt);
            }
        });
        FormInput.add(TinggalDengan);
        TinggalDengan.setBounds(142, 510, 112, 23);

        KetTinggal.setFocusTraversalPolicyProvider(true);
        KetTinggal.setName("KetTinggal"); // NOI18N
        KetTinggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetTinggalKeyPressed(evt);
            }
        });
        FormInput.add(KetTinggal);
        KetTinggal.setBounds(257, 510, 126, 23);

        jLabel60.setText("h. Tinggal Dengan :");
        jLabel60.setName("jLabel60"); // NOI18N
        FormInput.add(jLabel60);
        jLabel60.setBounds(0, 510, 138, 23);

        Edukasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pasien", "Keluarga" }));
        Edukasi.setName("Edukasi"); // NOI18N
        Edukasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EdukasiKeyPressed(evt);
            }
        });
        FormInput.add(Edukasi);
        Edukasi.setBounds(601, 570, 100, 23);

        KetEdukasi.setFocusTraversalPolicyProvider(true);
        KetEdukasi.setName("KetEdukasi"); // NOI18N
        KetEdukasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetEdukasiKeyPressed(evt);
            }
        });
        FormInput.add(KetEdukasi);
        KetEdukasi.setBounds(704, 570, 150, 23);

        jLabel64.setText("Jam  :");
        jLabel64.setName("jLabel64"); // NOI18N
        FormInput.add(jLabel64);
        jLabel64.setBounds(720, 850, 50, 23);

        jLabel65.setText("1. Tidak seimbang / sempoyongan / limbung :");
        jLabel65.setName("jLabel65"); // NOI18N
        FormInput.add(jLabel65);
        jLabel65.setBounds(35, 920, 250, 23);

        jLabel66.setText("2. Jalan dengan menggunakan alat bantu (kruk, tripot, kursi roda, orang lain) :");
        jLabel66.setName("jLabel66"); // NOI18N
        FormInput.add(jLabel66);
        jLabel66.setBounds(370, 920, 400, 23);

        Lapor.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        Lapor.setName("Lapor"); // NOI18N
        Lapor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LaporKeyPressed(evt);
            }
        });
        FormInput.add(Lapor);
        Lapor.setBounds(575, 980, 80, 23);

        ATS.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        ATS.setName("ATS"); // NOI18N
        ATS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ATSKeyPressed(evt);
            }
        });
        FormInput.add(ATS);
        ATS.setBounds(289, 920, 80, 23);

        BJM.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        BJM.setName("BJM"); // NOI18N
        BJM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BJMKeyPressed(evt);
            }
        });
        FormInput.add(BJM);
        BJM.setBounds(774, 920, 80, 23);

        jLabel67.setText("Menyebar :");
        jLabel67.setName("jLabel67"); // NOI18N
        FormInput.add(jLabel67);
        jLabel67.setBounds(691, 780, 79, 23);

        Hasil.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak beresiko (tidak ditemukan a dan b)",
                "Resiko rendah (ditemukan a/b)", "Resiko tinggi (ditemukan a dan b)" }));
        Hasil.setName("Hasil"); // NOI18N
        Hasil.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HasilKeyPressed(evt);
            }
        });
        FormInput.add(Hasil);
        Hasil.setBounds(76, 980, 293, 23);

        jLabel68.setText("Hasil :");
        jLabel68.setName("jLabel68"); // NOI18N
        FormInput.add(jLabel68);
        jLabel68.setBounds(0, 980, 72, 23);

        KetLapor.setFocusTraversalPolicyProvider(true);
        KetLapor.setName("KetLapor"); // NOI18N
        KetLapor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetLaporKeyPressed(evt);
            }
        });
        FormInput.add(KetLapor);
        KetLapor.setBounds(774, 980, 80, 23);

        jLabel70.setText(
                "b. Menopang saat akan duduk, tampak memegang pinggiran kursi atau meja / benda lain sebagai penopang :");
        jLabel70.setName("jLabel70"); // NOI18N
        FormInput.add(jLabel70);
        jLabel70.setBounds(0, 950, 571, 23);

        jLabel72.setText("a. Cara Berjalan :");
        jLabel72.setName("jLabel72"); // NOI18N
        FormInput.add(jLabel72);
        jLabel72.setBounds(0, 900, 126, 23);

        MSA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        MSA.setName("MSA"); // NOI18N
        MSA.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MSAKeyPressed(evt);
            }
        });
        FormInput.add(MSA);
        MSA.setBounds(575, 950, 80, 23);

        Nyeri.setModel(
                new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada Nyeri", "Nyeri Akut", "Nyeri Kronis" }));
        Nyeri.setName("Nyeri"); // NOI18N
        Nyeri.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NyeriKeyPressed(evt);
            }
        });
        FormInput.add(Nyeri);
        Nyeri.setBounds(375, 700, 130, 23);

        Provokes.setModel(
                new javax.swing.DefaultComboBoxModel(new String[] { "Proses Penyakit", "Benturan", "Lain-lain" }));
        Provokes.setName("Provokes"); // NOI18N
        Provokes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ProvokesKeyPressed(evt);
            }
        });
        FormInput.add(Provokes);
        Provokes.setBounds(574, 700, 130, 23);

        KetProvokes.setFocusTraversalPolicyProvider(true);
        KetProvokes.setName("KetProvokes"); // NOI18N
        KetProvokes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetProvokesKeyPressed(evt);
            }
        });
        FormInput.add(KetProvokes);
        KetProvokes.setBounds(708, 700, 146, 23);

        jLabel80.setText("Penyebab :");
        jLabel80.setName("jLabel80"); // NOI18N
        FormInput.add(jLabel80);
        jLabel80.setBounds(510, 700, 60, 23);

        Quality.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Seperti Tertusuk", "Berdenyut", "Teriris", "Tertindih", "Tertiban", "Lain-lain" }));
        Quality.setName("Quality"); // NOI18N
        Quality.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                QualityKeyPressed(evt);
            }
        });
        FormInput.add(Quality);
        Quality.setBounds(429, 730, 140, 23);

        KetQuality.setFocusTraversalPolicyProvider(true);
        KetQuality.setName("KetQuality"); // NOI18N
        KetQuality.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetQualityKeyPressed(evt);
            }
        });
        FormInput.add(KetQuality);
        KetQuality.setBounds(573, 730, 281, 23);

        jLabel81.setText("Kualitas :");
        jLabel81.setName("jLabel81"); // NOI18N
        FormInput.add(jLabel81);
        jLabel81.setBounds(370, 730, 55, 23);

        jLabel82.setText("Wilayah :");
        jLabel82.setName("jLabel82"); // NOI18N
        FormInput.add(jLabel82);
        jLabel82.setBounds(370, 760, 55, 23);

        Lokasi.setFocusTraversalPolicyProvider(true);
        Lokasi.setName("Lokasi"); // NOI18N
        Lokasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LokasiKeyPressed(evt);
            }
        });
        FormInput.add(Lokasi);
        Lokasi.setBounds(458, 780, 220, 23);

        jLabel83.setText("Lokasi :");
        jLabel83.setName("jLabel83"); // NOI18N
        FormInput.add(jLabel83);
        jLabel83.setBounds(394, 780, 60, 23);

        Menyebar.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        Menyebar.setName("Menyebar"); // NOI18N
        Menyebar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenyebarKeyPressed(evt);
            }
        });
        FormInput.add(Menyebar);
        Menyebar.setBounds(774, 780, 80, 23);

        jLabel84.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel84.setText("Menit");
        jLabel84.setName("jLabel84"); // NOI18N
        FormInput.add(jLabel84);
        jLabel84.setBounds(815, 810, 35, 23);

        jLabel85.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel85.setText("Skala Nyeri");
        jLabel85.setName("jLabel85"); // NOI18N
        FormInput.add(jLabel85);
        jLabel85.setBounds(428, 810, 60, 23);

        SkalaNyeri.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        SkalaNyeri.setName("SkalaNyeri"); // NOI18N
        SkalaNyeri.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaNyeriKeyPressed(evt);
            }
        });
        FormInput.add(SkalaNyeri);
        SkalaNyeri.setBounds(491, 810, 70, 23);

        jLabel86.setText("Diberitahukan pada dokter ?");
        jLabel86.setName("jLabel86"); // NOI18N
        FormInput.add(jLabel86);
        jLabel86.setBounds(480, 850, 150, 23);

        Durasi.setFocusTraversalPolicyProvider(true);
        Durasi.setName("Durasi"); // NOI18N
        Durasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DurasiKeyPressed(evt);
            }
        });
        FormInput.add(Durasi);
        Durasi.setBounds(721, 810, 90, 23);

        jLabel87.setText("Waktu / Durasi :");
        jLabel87.setName("jLabel87"); // NOI18N
        FormInput.add(jLabel87);
        jLabel87.setBounds(627, 810, 90, 23);

        jLabel88.setText("Severity :");
        jLabel88.setName("jLabel88"); // NOI18N
        FormInput.add(jLabel88);
        jLabel88.setBounds(370, 810, 55, 23);

        NyeriHilang.setModel(
                new javax.swing.DefaultComboBoxModel(new String[] { "Istirahat", "Medengar Musik", "Minum Obat" }));
        NyeriHilang.setName("NyeriHilang"); // NOI18N
        NyeriHilang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NyeriHilangKeyPressed(evt);
            }
        });
        FormInput.add(NyeriHilang);
        NyeriHilang.setBounds(134, 850, 130, 23);

        KetNyeri.setFocusTraversalPolicyProvider(true);
        KetNyeri.setName("KetNyeri"); // NOI18N
        KetNyeri.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetNyeriKeyPressed(evt);
            }
        });
        FormInput.add(KetNyeri);
        KetNyeri.setBounds(268, 850, 150, 23);

        jLabel89.setText("Nyeri hilang bila :");
        jLabel89.setName("jLabel89"); // NOI18N
        FormInput.add(jLabel89);
        jLabel89.setBounds(0, 850, 130, 23);

        PadaDokter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        PadaDokter.setName("PadaDokter"); // NOI18N
        PadaDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PadaDokterKeyPressed(evt);
            }
        });
        FormInput.add(PadaDokter);
        PadaDokter.setBounds(634, 850, 80, 23);

        KetDokter.setFocusTraversalPolicyProvider(true);
        KetDokter.setName("KetDokter"); // NOI18N
        KetDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetDokterKeyPressed(evt);
            }
        });
        FormInput.add(KetDokter);
        KetDokter.setBounds(774, 850, 80, 23);

        TglAsuhan.setForeground(new java.awt.Color(50, 70, 50));
        TglAsuhan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "02-05-2024 05:41:57" }));
        TglAsuhan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TglAsuhan.setName("TglAsuhan"); // NOI18N
        TglAsuhan.setOpaque(false);
        TglAsuhan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglAsuhanKeyPressed(evt);
            }
        });
        FormInput.add(TglAsuhan);
        TglAsuhan.setBounds(456, 40, 130, 23);

        jLabel94.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel94.setText(" RIWAYAT PSIKOLOGIS - SOSIAL - EKONOMI - BUDAYA - SPIRITUAL");
        jLabel94.setName("jLabel94"); // NOI18N
        FormInput.add(jLabel94);
        jLabel94.setBounds(10, 400, 360, 23);

        jLabel51.setText("c. Cacat Fisik :");
        jLabel51.setName("jLabel51"); // NOI18N
        FormInput.add(jLabel51);
        jLabel51.setBounds(615, 620, 100, 23);

        CacatFisik.setEditable(false);
        CacatFisik.setFocusTraversalPolicyProvider(true);
        CacatFisik.setName("CacatFisik"); // NOI18N
        FormInput.add(CacatFisik);
        CacatFisik.setBounds(719, 620, 135, 23);

        jLabel56.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel56.setText("PENGKAJIAN FUNGSI");
        jLabel56.setName("jLabel56"); // NOI18N
        FormInput.add(jLabel56);
        jLabel56.setBounds(10, 600, 230, 23);

        jLabel95.setText("l. Kepercayaan / Budaya / Nilai-nilai Khusus Yang Perlu Diperhatikan :");
        jLabel95.setName("jLabel95"); // NOI18N
        FormInput.add(jLabel95);
        jLabel95.setBounds(217, 540, 350, 23);

        StatusBudaya.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Ada" }));
        StatusBudaya.setName("StatusBudaya"); // NOI18N
        StatusBudaya.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                StatusBudayaKeyPressed(evt);
            }
        });
        FormInput.add(StatusBudaya);
        StatusBudaya.setBounds(571, 540, 100, 23);

        KetBudaya.setFocusTraversalPolicyProvider(true);
        KetBudaya.setName("KetBudaya"); // NOI18N
        KetBudaya.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetBudayaKeyPressed(evt);
            }
        });
        FormInput.add(KetBudaya);
        KetBudaya.setBounds(674, 540, 180, 23);

        jLabel97.setText("Dilaporkan kepada dokter ?");
        jLabel97.setName("jLabel97"); // NOI18N
        FormInput.add(jLabel97);
        jLabel97.setBounds(381, 980, 190, 23);

        jLabel63.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel63.setText("PENGKAJIAN RESIKO JATUH (GET UP AND GO)");
        jLabel63.setName("jLabel63"); // NOI18N
        FormInput.add(jLabel63);
        jLabel63.setBounds(10, 880, 380, 23);

        jSeparator1.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator1.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(0, 70, 880, 1);

        jSeparator4.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator4.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator4.setName("jSeparator4"); // NOI18N
        FormInput.add(jSeparator4);
        jSeparator4.setBounds(0, 600, 880, 1);

        jSeparator6.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator6.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator6.setName("jSeparator6"); // NOI18N
        FormInput.add(jSeparator6);
        jSeparator6.setBounds(0, 880, 880, 1);

        jSeparator8.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator8.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator8.setName("jSeparator8"); // NOI18N
        FormInput.add(jSeparator8);
        jSeparator8.setBounds(0, 680, 880, 1);

        PanelWall.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/nyeri.png"))); // NOI18N
        PanelWall.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall.setRound(false);
        PanelWall.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall.setLayout(null);
        FormInput.add(PanelWall);
        PanelWall.setBounds(40, 700, 320, 130);

        jSeparator9.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator9.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator9.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator9.setName("jSeparator9"); // NOI18N
        FormInput.add(jSeparator9);
        jSeparator9.setBounds(360, 700, 1, 140);

        jLabel71.setText("Jam dilaporkan :");
        jLabel71.setName("jLabel71"); // NOI18N
        FormInput.add(jLabel71);
        jLabel71.setBounds(680, 980, 90, 23);

        jSeparator10.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator10.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator10.setName("jSeparator10"); // NOI18N
        FormInput.add(jSeparator10);
        jSeparator10.setBounds(0, 1010, 880, 1);

        Bahasa.setEditable(false);
        Bahasa.setFocusTraversalPolicyProvider(true);
        Bahasa.setName("Bahasa"); // NOI18N
        FormInput.add(Bahasa);
        Bahasa.setBounds(519, 510, 120, 23);

        jLabel76.setText("i. Bahasa Sehari-hari :");
        jLabel76.setName("jLabel76"); // NOI18N
        FormInput.add(jLabel76);
        jLabel76.setBounds(390, 510, 125, 23);

        jLabel77.setText("k. Agama :");
        jLabel77.setName("jLabel77"); // NOI18N
        FormInput.add(jLabel77);
        jLabel77.setBounds(0, 540, 95, 23);

        Agama.setEditable(false);
        Agama.setFocusTraversalPolicyProvider(true);
        Agama.setName("Agama"); // NOI18N
        FormInput.add(Agama);
        Agama.setBounds(99, 540, 110, 23);

        jLabel78.setText("Status Kehamilan :");
        jLabel78.setName("jLabel78"); // NOI18N
        FormInput.add(jLabel78);
        jLabel78.setBounds(440, 150, 106, 23);

        StatusKehamilan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Hamil", "Hamil" }));
        StatusKehamilan.setName("StatusKehamilan"); // NOI18N
        StatusKehamilan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                StatusKehamilanKeyPressed(evt);
            }
        });
        FormInput.add(StatusKehamilan);
        StatusKehamilan.setBounds(550, 150, 110, 23);

        jLabel29.setText("Gravida :");
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(715, 180, 50, 23);

        Gravida.setHighlighter(null);
        Gravida.setName("Gravida"); // NOI18N
        Gravida.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                GravidaKeyPressed(evt);
            }
        });
        FormInput.add(Gravida);
        Gravida.setBounds(769, 180, 85, 23);

        jLabel32.setText("Para :");
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(440, 180, 43, 23);

        Para.setHighlighter(null);
        Para.setName("Para"); // NOI18N
        Para.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ParaKeyPressed(evt);
            }
        });
        FormInput.add(Para);
        Para.setBounds(487, 180, 85, 23);

        jLabel33.setText("Abortus :");
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput.add(jLabel33);
        jLabel33.setBounds(575, 180, 50, 23);

        Abortus.setHighlighter(null);
        Abortus.setName("Abortus"); // NOI18N
        Abortus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AbortusKeyPressed(evt);
            }
        });
        FormInput.add(Abortus);
        Abortus.setBounds(629, 180, 85, 23);

        jLabel35.setText("HPHT :");
        jLabel35.setName("jLabel35"); // NOI18N
        FormInput.add(jLabel35);
        jLabel35.setBounds(680, 150, 50, 23);

        HPHT.setHighlighter(null);
        HPHT.setName("HPHT"); // NOI18N
        HPHT.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HPHTKeyPressed(evt);
            }
        });
        FormInput.add(HPHT);
        HPHT.setBounds(734, 150, 120, 23);

        jSeparator11.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator11.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator11.setName("jSeparator11"); // NOI18N
        FormInput.add(jSeparator11);
        jSeparator11.setBounds(0, 210, 880, 1);

        jLabel98.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel98.setText("RIWAYAT KESEHATAN PASIEN");
        jLabel98.setName("jLabel98"); // NOI18N
        FormInput.add(jLabel98);
        jLabel98.setBounds(10, 70, 180, 23);

        jLabel90.setText("Tekanan Intrakranial :");
        jLabel90.setName("jLabel90"); // NOI18N
        FormInput.add(jLabel90);
        jLabel90.setBounds(0, 230, 150, 23);

        Tekanan.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "TAK", "Sakit Kepala", "Muntah", "Pusing", "Bingung" }));
        Tekanan.setName("Tekanan"); // NOI18N
        Tekanan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TekananKeyPressed(evt);
            }
        });
        FormInput.add(Tekanan);
        Tekanan.setBounds(154, 230, 112, 23);

        jLabel91.setText("Pupil :");
        jLabel91.setName("jLabel91"); // NOI18N
        FormInput.add(jLabel91);
        jLabel91.setBounds(295, 230, 50, 23);

        Pupil.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Miosis", "Isokor", "Anisokor" }));
        Pupil.setName("Pupil"); // NOI18N
        Pupil.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PupilKeyPressed(evt);
            }
        });
        FormInput.add(Pupil);
        Pupil.setBounds(349, 230, 93, 23);

        Neurosensorik.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TAK", "Spasme Otot",
                "Perubahan Sensorik", "Perubahan Motorik", "Perubahan Bentuk Ekstremitas",
                "Penurunan Tingkat Kesadaran", "Fraktur/Dislokasi", "Luksasio", "Kerusakan Jaringan/Luka" }));
        Neurosensorik.setName("Neurosensorik"); // NOI18N
        Neurosensorik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NeurosensorikKeyPressed(evt);
            }
        });
        FormInput.add(Neurosensorik);
        Neurosensorik.setBounds(654, 230, 200, 23);

        jLabel100.setText("Neurosensorik / Muskuloskeletal :");
        jLabel100.setName("jLabel100"); // NOI18N
        FormInput.add(jLabel100);
        jLabel100.setBounds(470, 230, 180, 23);

        jLabel101.setText("Integumen :");
        jLabel101.setName("jLabel101"); // NOI18N
        FormInput.add(jLabel101);
        jLabel101.setBounds(0, 260, 150, 23);

        Integumen.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "TAK", "Luka Bakar", "Luka Robek", "Lecet", "Luka Decubitus", "Luka Gangren" }));
        Integumen.setName("Integumen"); // NOI18N
        Integumen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IntegumenKeyPressed(evt);
            }
        });
        FormInput.add(Integumen);
        Integumen.setBounds(154, 260, 125, 23);

        jLabel102.setText("Turgor Kulit :");
        jLabel102.setName("jLabel102"); // NOI18N
        FormInput.add(jLabel102);
        jLabel102.setBounds(290, 260, 80, 23);

        Turgor.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Baik", "Menurun" }));
        Turgor.setName("Turgor"); // NOI18N
        Turgor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TurgorKeyPressed(evt);
            }
        });
        FormInput.add(Turgor);
        Turgor.setBounds(374, 260, 93, 23);

        jLabel103.setText("Edema :");
        jLabel103.setName("jLabel103"); // NOI18N
        FormInput.add(jLabel103);
        jLabel103.setBounds(486, 260, 50, 23);

        Edema.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Tidak Ada", "Ekstremitas", "Seluruh Tubuh", "Asites", "Palpebrae" }));
        Edema.setName("Edema"); // NOI18N
        Edema.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EdemaKeyPressed(evt);
            }
        });
        FormInput.add(Edema);
        Edema.setBounds(540, 260, 120, 23);

        jLabel104.setText("Mukosa Mulut :");
        jLabel104.setName("jLabel104"); // NOI18N
        FormInput.add(jLabel104);
        jLabel104.setBounds(670, 260, 90, 23);

        Mukosa.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Lembab", "Kering" }));
        Mukosa.setName("Mukosa"); // NOI18N
        Mukosa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MukosaKeyPressed(evt);
            }
        });
        FormInput.add(Mukosa);
        Mukosa.setBounds(764, 260, 90, 23);

        jLabel105.setText("Perdarahan :");
        jLabel105.setName("jLabel105"); // NOI18N
        FormInput.add(jLabel105);
        jLabel105.setBounds(0, 290, 150, 23);

        Perdarahan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Ada" }));
        Perdarahan.setName("Perdarahan"); // NOI18N
        Perdarahan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PerdarahanKeyPressed(evt);
            }
        });
        FormInput.add(Perdarahan);
        Perdarahan.setBounds(154, 290, 100, 23);

        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel36.setText("cc");
        jLabel36.setName("jLabel36"); // NOI18N
        FormInput.add(jLabel36);
        jLabel36.setBounds(379, 290, 20, 23);

        JumlahPerdarahan.setHighlighter(null);
        JumlahPerdarahan.setName("JumlahPerdarahan"); // NOI18N
        JumlahPerdarahan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JumlahPerdarahanKeyPressed(evt);
            }
        });
        FormInput.add(JumlahPerdarahan);
        JumlahPerdarahan.setBounds(306, 290, 70, 23);

        jLabel37.setText(", Warna :");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(386, 290, 50, 23);

        WarnaPerdarahan.setHighlighter(null);
        WarnaPerdarahan.setName("WarnaPerdarahan"); // NOI18N
        WarnaPerdarahan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                WarnaPerdarahanKeyPressed(evt);
            }
        });
        FormInput.add(WarnaPerdarahan);
        WarnaPerdarahan.setBounds(440, 290, 170, 23);

        jLabel38.setText(", Jumlah :");
        jLabel38.setName("jLabel38"); // NOI18N
        FormInput.add(jLabel38);
        jLabel38.setBounds(252, 290, 50, 23);

        jLabel106.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel106.setText("X/");
        jLabel106.setName("jLabel106"); // NOI18N
        FormInput.add(jLabel106);
        jLabel106.setBounds(282, 340, 13, 23);

        Intoksikasi.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Tidak Ada", "Ada", "Gigitan Binatang", "Zat Kimia", "Gas", "Obat" }));
        Intoksikasi.setSelectedIndex(2);
        Intoksikasi.setName("Intoksikasi"); // NOI18N
        Intoksikasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IntoksikasiKeyPressed(evt);
            }
        });
        FormInput.add(Intoksikasi);
        Intoksikasi.setBounds(719, 290, 135, 23);

        jLabel107.setText("Intoksikasi :");
        jLabel107.setName("jLabel107"); // NOI18N
        FormInput.add(jLabel107);
        jLabel107.setBounds(625, 290, 90, 23);

        jLabel108.setText("Eliminasi :");
        jLabel108.setName("jLabel108"); // NOI18N
        FormInput.add(jLabel108);
        jLabel108.setBounds(0, 320, 150, 23);

        KBAB.setHighlighter(null);
        KBAB.setName("KBAB"); // NOI18N
        KBAB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KBABKeyPressed(evt);
            }
        });
        FormInput.add(KBAB);
        KBAB.setBounds(443, 340, 175, 23);

        jLabel109.setText("Konsistensi :");
        jLabel109.setName("jLabel109"); // NOI18N
        FormInput.add(jLabel109);
        jLabel109.setBounds(369, 340, 70, 23);

        BAB.setHighlighter(null);
        BAB.setName("BAB"); // NOI18N
        BAB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BABKeyPressed(evt);
            }
        });
        FormInput.add(BAB);
        BAB.setBounds(229, 340, 50, 23);

        jLabel110.setText("BAB : Frekuensi :");
        jLabel110.setName("jLabel110"); // NOI18N
        FormInput.add(jLabel110);
        jLabel110.setBounds(135, 340, 90, 23);

        XBAB.setHighlighter(null);
        XBAB.setName("XBAB"); // NOI18N
        XBAB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                XBABKeyPressed(evt);
            }
        });
        FormInput.add(XBAB);
        XBAB.setBounds(298, 340, 70, 23);

        jLabel111.setText("Warna :");
        jLabel111.setName("jLabel111"); // NOI18N
        FormInput.add(jLabel111);
        jLabel111.setBounds(620, 340, 55, 23);

        WBAB.setHighlighter(null);
        WBAB.setName("WBAB"); // NOI18N
        WBAB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                WBABKeyPressed(evt);
            }
        });
        FormInput.add(WBAB);
        WBAB.setBounds(679, 340, 175, 23);

        jLabel112.setText("BAK : Frekuensi :");
        jLabel112.setName("jLabel112"); // NOI18N
        FormInput.add(jLabel112);
        jLabel112.setBounds(135, 370, 90, 23);

        BAK.setHighlighter(null);
        BAK.setName("BAK"); // NOI18N
        BAK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BAKKeyPressed(evt);
            }
        });
        FormInput.add(BAK);
        BAK.setBounds(229, 370, 50, 23);

        jLabel113.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel113.setText("X/");
        jLabel113.setName("jLabel113"); // NOI18N
        FormInput.add(jLabel113);
        jLabel113.setBounds(282, 370, 13, 23);

        XBAK.setHighlighter(null);
        XBAK.setName("XBAK"); // NOI18N
        XBAK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                XBAKKeyPressed(evt);
            }
        });
        FormInput.add(XBAK);
        XBAK.setBounds(298, 370, 70, 23);

        jLabel114.setText("Warna :");
        jLabel114.setName("jLabel114"); // NOI18N
        FormInput.add(jLabel114);
        jLabel114.setBounds(369, 370, 70, 23);

        WBAK.setHighlighter(null);
        WBAK.setName("WBAK"); // NOI18N
        WBAK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                WBAKKeyPressed(evt);
            }
        });
        FormInput.add(WBAK);
        WBAK.setBounds(443, 370, 175, 23);

        jLabel115.setText("Lain-lain :");
        jLabel115.setName("jLabel115"); // NOI18N
        FormInput.add(jLabel115);
        jLabel115.setBounds(620, 370, 55, 23);

        LBAK.setHighlighter(null);
        LBAK.setName("LBAK"); // NOI18N
        LBAK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LBAKKeyPressed(evt);
            }
        });
        FormInput.add(LBAK);
        LBAK.setBounds(679, 370, 175, 23);

        jSeparator12.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator12.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator12.setName("jSeparator12"); // NOI18N
        FormInput.add(jSeparator12);
        jSeparator12.setBounds(0, 400, 880, 1);

        jLabel116.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel116.setText("PEMERIKSAAN FISIK");
        jLabel116.setName("jLabel116"); // NOI18N
        FormInput.add(jLabel116);
        jLabel116.setBounds(10, 210, 180, 23);

        jLabel117.setText("a. Kondisi Psikologis :");
        jLabel117.setName("jLabel117"); // NOI18N
        FormInput.add(jLabel117);
        jLabel117.setBounds(0, 420, 148, 23);

        Psikologis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada Masalah", "Marah", "Takut",
                "Depresi", "Cepat Lelah", "Cemas", "Gelisah", "Lain-lain" }));
        Psikologis.setName("Psikologis"); // NOI18N
        Psikologis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PsikologisKeyPressed(evt);
            }
        });
        FormInput.add(Psikologis);
        Psikologis.setBounds(152, 420, 160, 23);

        jLabel119.setText("b. Gangguan Jiwa Di Masa Lalu :");
        jLabel119.setName("jLabel119"); // NOI18N
        FormInput.add(jLabel119);
        jLabel119.setBounds(311, 420, 180, 23);

        Jiwa.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        Jiwa.setName("Jiwa"); // NOI18N
        Jiwa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JiwaKeyPressed(evt);
            }
        });
        FormInput.add(Jiwa);
        Jiwa.setBounds(495, 420, 90, 23);

        jLabel120.setText("c. Status Pernikahan :");
        jLabel120.setName("jLabel120"); // NOI18N
        FormInput.add(jLabel120);
        jLabel120.setBounds(590, 420, 120, 23);

        Perilaku.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Perilaku Kekerasan", "Gangguan Efek",
                "Gangguan Memori", "Halusinasi", "Kecenderungan Percobaan Bunuh Diri", "Lainnya", "-" }));
        Perilaku.setSelectedIndex(6);
        Perilaku.setName("Perilaku"); // NOI18N
        Perilaku.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PerilakuKeyPressed(evt);
            }
        });
        FormInput.add(Perilaku);
        Perilaku.setBounds(144, 450, 235, 23);

        jLabel118.setText(", Dilaporkan Ke :");
        jLabel118.setName("jLabel118"); // NOI18N
        FormInput.add(jLabel118);
        jLabel118.setBounds(378, 450, 82, 23);

        Dilaporkan.setHighlighter(null);
        Dilaporkan.setName("Dilaporkan"); // NOI18N
        Dilaporkan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DilaporkanKeyPressed(evt);
            }
        });
        FormInput.add(Dilaporkan);
        Dilaporkan.setBounds(464, 450, 163, 23);
        Dilaporkan.getAccessibleContext().setAccessibleName("");

        jLabel121.setText(", Sebutkan :");
        jLabel121.setName("jLabel121"); // NOI18N
        FormInput.add(jLabel121);
        jLabel121.setBounds(625, 450, 62, 23);

        Sebutkan.setHighlighter(null);
        Sebutkan.setName("Sebutkan"); // NOI18N
        Sebutkan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SebutkanKeyPressed(evt);
            }
        });
        FormInput.add(Sebutkan);
        Sebutkan.setBounds(691, 450, 163, 23);
        Sebutkan.getAccessibleContext().setAccessibleName("");

        jLabel122.setText("e. Hubungan Pasien Dengan Anggota Keluarga :");
        jLabel122.setName("jLabel122"); // NOI18N
        FormInput.add(jLabel122);
        jLabel122.setBounds(0, 480, 276, 23);

        Hubungan.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Harmonis", "Kurang Harmonis", "Tidak Harmonis", "Konflik Besar" }));
        Hubungan.setName("Hubungan"); // NOI18N
        Hubungan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HubunganKeyPressed(evt);
            }
        });
        FormInput.add(Hubungan);
        Hubungan.setBounds(280, 480, 135, 23);

        jLabel123.setText("d. Adakah Perilaku :");
        jLabel123.setName("jLabel123"); // NOI18N
        FormInput.add(jLabel123);
        jLabel123.setBounds(0, 450, 140, 23);

        StatusPernikahan.setEditable(false);
        StatusPernikahan.setHighlighter(null);
        StatusPernikahan.setName("StatusPernikahan"); // NOI18N
        StatusPernikahan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StatusPernikahanActionPerformed(evt);
            }
        });
        FormInput.add(StatusPernikahan);
        StatusPernikahan.setBounds(714, 420, 140, 23);

        jLabel124.setText("f. Pekerjaan :");
        jLabel124.setName("jLabel124"); // NOI18N
        FormInput.add(jLabel124);
        jLabel124.setBounds(433, 480, 70, 23);

        Pekerjaan.setEditable(false);
        Pekerjaan.setHighlighter(null);
        Pekerjaan.setName("Pekerjaan"); // NOI18N
        FormInput.add(Pekerjaan);
        Pekerjaan.setBounds(507, 480, 120, 23);

        jLabel125.setText("g. Pembayaran :");
        jLabel125.setName("jLabel125"); // NOI18N
        FormInput.add(jLabel125);
        jLabel125.setBounds(640, 480, 90, 23);

        Pembayaran.setEditable(false);
        Pembayaran.setHighlighter(null);
        Pembayaran.setName("Pembayaran"); // NOI18N
        FormInput.add(Pembayaran);
        Pembayaran.setBounds(734, 480, 120, 23);

        jLabel126.setText("j. Pendidikan :");
        jLabel126.setName("jLabel126"); // NOI18N
        FormInput.add(jLabel126);
        jLabel126.setBounds(650, 510, 80, 23);

        PendidikanPasien.setEditable(false);
        PendidikanPasien.setFocusTraversalPolicyProvider(true);
        PendidikanPasien.setName("PendidikanPasien"); // NOI18N
        FormInput.add(PendidikanPasien);
        PendidikanPasien.setBounds(734, 510, 120, 23);

        jLabel127.setText("m. Pendidikan P.J. :");
        jLabel127.setName("jLabel127"); // NOI18N
        FormInput.add(jLabel127);
        jLabel127.setBounds(0, 570, 138, 23);

        PendidikanPJ.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "TS", "TK", "SD", "SMP", "SMA",
                "SLTA/SEDERAJAT", "D1", "D2", "D3", "D4", "S1", "S2", "S3" }));
        PendidikanPJ.setName("PendidikanPJ"); // NOI18N
        PendidikanPJ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PendidikanPJKeyPressed(evt);
            }
        });
        FormInput.add(PendidikanPJ);
        PendidikanPJ.setBounds(142, 570, 137, 23);

        KetPendidikanPJ.setFocusTraversalPolicyProvider(true);
        KetPendidikanPJ.setName("KetPendidikanPJ"); // NOI18N
        KetPendidikanPJ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetPendidikanPJKeyPressed(evt);
            }
        });
        FormInput.add(KetPendidikanPJ);
        KetPendidikanPJ.setBounds(282, 570, 150, 23);

        jLabel39.setText("Informasi didapat dari :");
        jLabel39.setName("jLabel39"); // NOI18N
        FormInput.add(jLabel39);
        jLabel39.setBounds(592, 40, 130, 23);

        Informasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Autoanamnesis", "Alloanamnesis" }));
        Informasi.setName("Informasi"); // NOI18N
        Informasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                InformasiKeyPressed(evt);
            }
        });
        FormInput.add(Informasi);
        Informasi.setBounds(726, 40, 128, 23);

        Scroll8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll8.setName("Scroll8"); // NOI18N
        Scroll8.setOpaque(true);

        tbMasalahKeperawatan.setName("tbMasalahKeperawatan"); // NOI18N
        tbMasalahKeperawatan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbMasalahKeperawatanMouseClicked(evt);
            }
        });
        tbMasalahKeperawatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbMasalahKeperawatanKeyPressed(evt);
            }

            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbMasalahKeperawatanKeyReleased(evt);
            }
        });
        Scroll8.setViewportView(tbMasalahKeperawatan);

        FormInput.add(Scroll8);
        Scroll8.setBounds(10, 1020, 400, 143);

        BtnTambahMasalah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambahMasalah.setMnemonic('3');
        BtnTambahMasalah.setToolTipText("Alt+3");
        BtnTambahMasalah.setName("BtnTambahMasalah"); // NOI18N
        BtnTambahMasalah.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambahMasalah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahMasalahActionPerformed(evt);
            }
        });
        FormInput.add(BtnTambahMasalah);
        BtnTambahMasalah.setBounds(363, 1170, 28, 23);

        BtnAllMasalah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAllMasalah.setMnemonic('2');
        BtnAllMasalah.setToolTipText("2Alt+2");
        BtnAllMasalah.setName("BtnAllMasalah"); // NOI18N
        BtnAllMasalah.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAllMasalah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllMasalahActionPerformed(evt);
            }
        });
        BtnAllMasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllMasalahKeyPressed(evt);
            }
        });
        FormInput.add(BtnAllMasalah);
        BtnAllMasalah.setBounds(331, 1170, 28, 23);

        BtnCariMasalah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariMasalah.setMnemonic('1');
        BtnCariMasalah.setToolTipText("Alt+1");
        BtnCariMasalah.setName("BtnCariMasalah"); // NOI18N
        BtnCariMasalah.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariMasalah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariMasalahActionPerformed(evt);
            }
        });
        BtnCariMasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariMasalahKeyPressed(evt);
            }
        });
        FormInput.add(BtnCariMasalah);
        BtnCariMasalah.setBounds(299, 1170, 28, 23);

        TCariMasalah.setToolTipText("Alt+C");
        TCariMasalah.setName("TCariMasalah"); // NOI18N
        TCariMasalah.setPreferredSize(new java.awt.Dimension(140, 23));
        TCariMasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariMasalahKeyPressed(evt);
            }
        });
        FormInput.add(TCariMasalah);
        TCariMasalah.setBounds(80, 1170, 215, 23);

        label12.setText("Key Word :");
        label12.setName("label12"); // NOI18N
        label12.setPreferredSize(new java.awt.Dimension(60, 23));
        FormInput.add(label12);
        label12.setBounds(16, 1170, 60, 23);

        TabRencanaKeperawatan.setBackground(new java.awt.Color(255, 255, 254));
        TabRencanaKeperawatan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TabRencanaKeperawatan.setForeground(new java.awt.Color(50, 50, 50));
        TabRencanaKeperawatan.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        TabRencanaKeperawatan.setName("TabRencanaKeperawatan"); // NOI18N

        panelBiasa1.setName("panelBiasa1"); // NOI18N
        panelBiasa1.setLayout(new java.awt.BorderLayout());

        Scroll9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll9.setName("Scroll9"); // NOI18N
        Scroll9.setOpaque(true);

        tbRencanaKeperawatan.setName("tbRencanaKeperawatan"); // NOI18N
        Scroll9.setViewportView(tbRencanaKeperawatan);

        panelBiasa1.add(Scroll9, java.awt.BorderLayout.CENTER);

        TabRencanaKeperawatan.addTab("Rencana Keperawatan", panelBiasa1);

        scrollPane5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane5.setName("scrollPane5"); // NOI18N

        Rencana.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Rencana.setColumns(20);
        Rencana.setRows(5);
        Rencana.setName("Rencana"); // NOI18N
        Rencana.setOpaque(true);
        Rencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RencanaKeyPressed(evt);
            }
        });
        scrollPane5.setViewportView(Rencana);

        TabRencanaKeperawatan.addTab("Rencana Keperawatan Lainnya", scrollPane5);

        FormInput.add(TabRencanaKeperawatan);
        TabRencanaKeperawatan.setBounds(433, 1020, 420, 143);

        label13.setText("Key Word :");
        label13.setName("label13"); // NOI18N
        label13.setPreferredSize(new java.awt.Dimension(60, 23));
        FormInput.add(label13);
        label13.setBounds(439, 1170, 60, 23);

        TCariRencana.setToolTipText("Alt+C");
        TCariRencana.setName("TCariRencana"); // NOI18N
        TCariRencana.setPreferredSize(new java.awt.Dimension(215, 23));
        TCariRencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariRencanaKeyPressed(evt);
            }
        });
        FormInput.add(TCariRencana);
        TCariRencana.setBounds(503, 1170, 235, 23);

        BtnCariRencana.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariRencana.setMnemonic('1');
        BtnCariRencana.setToolTipText("Alt+1");
        BtnCariRencana.setName("BtnCariRencana"); // NOI18N
        BtnCariRencana.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariRencana.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariRencanaActionPerformed(evt);
            }
        });
        BtnCariRencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariRencanaKeyPressed(evt);
            }
        });
        FormInput.add(BtnCariRencana);
        BtnCariRencana.setBounds(742, 1170, 28, 23);

        BtnAllRencana.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAllRencana.setMnemonic('2');
        BtnAllRencana.setToolTipText("2Alt+2");
        BtnAllRencana.setName("BtnAllRencana"); // NOI18N
        BtnAllRencana.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAllRencana.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllRencanaActionPerformed(evt);
            }
        });
        BtnAllRencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllRencanaKeyPressed(evt);
            }
        });
        FormInput.add(BtnAllRencana);
        BtnAllRencana.setBounds(774, 1170, 28, 23);

        BtnTambahRencana.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambahRencana.setMnemonic('3');
        BtnTambahRencana.setToolTipText("Alt+3");
        BtnTambahRencana.setName("BtnTambahRencana"); // NOI18N
        BtnTambahRencana.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambahRencana.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahRencanaActionPerformed(evt);
            }
        });
        FormInput.add(BtnTambahRencana);
        BtnTambahRencana.setBounds(806, 1170, 28, 23);

        scrollInput.setViewportView(FormInput);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Pengkajian", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tgl.Asuhan :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(70, 26));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "02-05-2024" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(125, 26));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 26));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "02-05-2024" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(125, 26));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 26));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(205, 26));
        TCari.putClientProperty("JTextField.placeholderText", "Ketik kata kunci...");
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setPreferredSize(new java.awt.Dimension(28, 26));
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Filter / Terapkan Pencarian (Alt+3)");
        BtnCari.setName("BtnCari"); // NOI18N
        javax.swing.Icon icCariData = widget.IconStyle.muat("check.svg", 14, widget.IconStyle.HIJAU);
        if (icCariData != null) {
            BtnCari.setIcon(icCariData);
        } else {
            BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        }
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnCari);

        BtnAllCari.setPreferredSize(new java.awt.Dimension(28, 26));
        BtnAllCari.setMnemonic('M');
        BtnAllCari.setToolTipText("Tampilkan Semua / Cari Semua Data (Alt+M)");
        BtnAllCari.setName("BtnAllCari"); // NOI18N
        javax.swing.Icon icAllData = widget.IconStyle.muat("search.svg", 14, widget.IconStyle.ABU);
        if (icAllData != null) {
            BtnAllCari.setIcon(icAllData);
        } else {
            BtnAllCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        }
        BtnAllCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAllCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    BtnAllActionPerformed(null);
                }
            }
        });
        panelGlass9.add(BtnAllCari);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(55, 26));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(60, 26));
        panelGlass9.add(LCount);

        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

        PanelAccor.setBackground(new java.awt.Color(255, 255, 255));
        PanelAccor.setName("PanelAccor"); // NOI18N
        PanelAccor.setPreferredSize(new java.awt.Dimension(470, 43));
        PanelAccor.setLayout(new java.awt.BorderLayout(1, 1));

        ChkAccor.setBackground(new java.awt.Color(255, 250, 250));
        ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.setSelected(true);
        ChkAccor.setFocusable(false);
        ChkAccor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkAccor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkAccor.setName("ChkAccor"); // NOI18N
        ChkAccor.setPreferredSize(new java.awt.Dimension(15, 20));
        ChkAccor.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkAccorActionPerformed(evt);
            }
        });
        PanelAccor.add(ChkAccor, java.awt.BorderLayout.WEST);

        FormMenu.setBackground(new java.awt.Color(255, 255, 255));
        FormMenu.setBorder(null);
        FormMenu.setName("FormMenu"); // NOI18N
        FormMenu.setPreferredSize(new java.awt.Dimension(115, 43));
        FormMenu.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        jLabel34.setText("Pasien :");
        jLabel34.setName("jLabel34"); // NOI18N
        jLabel34.setPreferredSize(new java.awt.Dimension(55, 23));
        FormMenu.add(jLabel34);

        TNoRM1.setEditable(false);
        TNoRM1.setHighlighter(null);
        TNoRM1.setName("TNoRM1"); // NOI18N
        TNoRM1.setPreferredSize(new java.awt.Dimension(100, 23));
        FormMenu.add(TNoRM1);

        TPasien1.setEditable(false);
        TPasien1.setBackground(new java.awt.Color(245, 250, 240));
        TPasien1.setHighlighter(null);
        TPasien1.setName("TPasien1"); // NOI18N
        TPasien1.setPreferredSize(new java.awt.Dimension(250, 23));
        FormMenu.add(TPasien1);

        BtnPrint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item (copy).png"))); // NOI18N
        BtnPrint1.setMnemonic('T');
        BtnPrint1.setToolTipText("Alt+T");
        BtnPrint1.setName("BtnPrint1"); // NOI18N
        BtnPrint1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPrint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrint1ActionPerformed(evt);
            }
        });
        FormMenu.add(BtnPrint1);

        PanelAccor.add(FormMenu, java.awt.BorderLayout.NORTH);

        FormMasalahRencana.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)));
        FormMasalahRencana.setName("FormMasalahRencana"); // NOI18N
        FormMasalahRencana.setLayout(new java.awt.GridLayout(3, 0, 1, 1));

        Scroll7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)));
        Scroll7.setName("Scroll7"); // NOI18N
        Scroll7.setOpaque(true);

        tbMasalahDetailMasalah.setName("tbMasalahDetailMasalah"); // NOI18N
        Scroll7.setViewportView(tbMasalahDetailMasalah);

        FormMasalahRencana.add(Scroll7);

        Scroll10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)));
        Scroll10.setName("Scroll10"); // NOI18N
        Scroll10.setOpaque(true);

        tbRencanaDetail.setName("tbRencanaDetail"); // NOI18N
        Scroll10.setViewportView(tbRencanaDetail);

        FormMasalahRencana.add(Scroll10);

        scrollPane6.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)),
                "Rencana Keperawatan Lainnya :", javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 0, 11),
                new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane6.setName("scrollPane6"); // NOI18N

        DetailRencana.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));
        DetailRencana.setColumns(20);
        DetailRencana.setRows(5);
        DetailRencana.setName("DetailRencana"); // NOI18N
        scrollPane6.setViewportView(DetailRencana);

        FormMasalahRencana.add(scrollPane6);

        PanelAccor.add(FormMasalahRencana, java.awt.BorderLayout.CENTER);

        internalFrame3.add(PanelAccor, java.awt.BorderLayout.EAST);

        TabRawat.addTab("Data Pengkajian", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // ===================================================================
    // [RM 03-4/03-5/03-6] Section baru mengikuti formulir fisik Penilaian
    // Awal Keperawatan Gawat Darurat. Pola sama persis dgn initRm032Tambahan()
    // di RMPenilaianAwalMedisIGD.java - panel GridBagLayout baru ditempel
    // ke FormInput (pakai null layout / posisi absolut, widget lama TIDAK
    // disentuh), lewat helper kepConstraint()/tambahBarisKepJudul()/
    // tambahBarisKepField()/panelChecklistKep().
    // ===================================================================

    private widget.PanelBiasa panelChecklistKep(int kolom, widget.CekBox... cek) {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new java.awt.GridLayout(0, kolom, 10, 6));
        for (widget.CekBox c : cek) {
            p.add(c);
        }
        return p;
    }

    private widget.PanelBiasa buatItemCekTeks(final widget.CekBox cek, final widget.TextBox txt, String placeholder) {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(false);
        p.putClientProperty("fillWidth", true);
        p.setLayout(new java.awt.BorderLayout(8, 0));

        if (cek != null) {
            p.add(cek, java.awt.BorderLayout.WEST);
        }

        if (txt != null) {
            txt.setPreferredSize(new java.awt.Dimension(200, 26));
            txt.setMinimumSize(new java.awt.Dimension(120, 26));
            if (placeholder != null && !placeholder.isEmpty()) {
                txt.setPlaceholder(placeholder);
            }
            txt.putClientProperty("JComponent.roundRect", true);
            txt.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6), 1),
                    javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6)));
            txt.setVisible(cek != null && cek.isSelected());
            if (cek != null) {
                txt.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyReleased(java.awt.event.KeyEvent e) {
                        if (txt.getText().length() > 0 && !cek.isSelected()) {
                            cek.setSelected(true);
                            hitungProgresKep();
                        }
                    }
                });
                cek.addItemListener((java.awt.event.ItemEvent e) -> {
                    boolean sel = cek.isSelected();
                    txt.setVisible(sel);
                    if (sel) {
                        if (txt.getText().trim().isEmpty()) {
                            txt.requestFocusInWindow();
                        }
                    } else {
                        txt.setText("");
                    }
                    p.revalidate();
                    p.repaint();
                    java.awt.Component par = p.getParent();
                    if (par != null) {
                        par.revalidate();
                        par.repaint();
                    }
                });
            }
            p.add(txt, java.awt.BorderLayout.CENTER);
        }
        return p;
    }

    private widget.PanelBiasa buatGridDuaKolomKep(java.awt.Component[][] items) {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(false);
        p.putClientProperty("fillWidth", true);
        p.setLayout(new java.awt.GridBagLayout());

        for (int r = 0; r < items.length; r++) {
            java.awt.Component left = items[r].length > 0 ? items[r][0] : null;
            java.awt.Component right = items[r].length > 1 ? items[r][1] : null;

            if (left != null) {
                java.awt.GridBagConstraints cL = new java.awt.GridBagConstraints();
                cL.gridx = 0;
                cL.gridy = r;
                cL.weightx = 0.0;
                cL.fill = java.awt.GridBagConstraints.HORIZONTAL;
                cL.anchor = java.awt.GridBagConstraints.WEST;
                cL.insets = new java.awt.Insets(r == 0 ? 0 : 6, 0, 0, 24);
                p.add(left, cL);
            }
            if (right != null) {
                java.awt.GridBagConstraints cR = new java.awt.GridBagConstraints();
                cR.gridx = 1;
                cR.gridy = r;
                cR.weightx = 0.0;
                cR.fill = java.awt.GridBagConstraints.HORIZONTAL;
                cR.anchor = java.awt.GridBagConstraints.WEST;
                cR.insets = new java.awt.Insets(r == 0 ? 0 : 6, 0, 0, 0);
                p.add(right, cR);
            }
        }
        widget.PanelBiasa filler = new widget.PanelBiasa();
        filler.setOpaque(false);
        java.awt.GridBagConstraints cF = new java.awt.GridBagConstraints();
        cF.gridx = 2;
        cF.gridy = 0;
        cF.gridheight = Math.max(1, items.length);
        cF.weightx = 1.0;
        cF.fill = java.awt.GridBagConstraints.BOTH;
        p.add(filler, cF);

        return p;
    }

    // Pengganti Valid.SetTgl2(JDateTimePicker,String) khusus utk widget.Jam
    // (bukan JDateTimePicker) - logic parse persis sama dgn validasi.java,
    // TIDAK mengubah fungsi.validasi.java krn dipakai file lain juga.
    private void setJamDariString(widget.Jam jam, String tglJam) {
        try {
            jam.setJam(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(tglJam.replaceAll("'", "")));
        } catch (java.text.ParseException ex) {
            jam.setJam(new Date());
        }
    }

    private void setJamDariString(PanelTanggalDanJam jam, String tglJam) {
        if (jam != null) {
            jam.setNilaiSql(tglJam);
        }
    }

    private java.awt.GridBagConstraints kepConstraint(int gridx, int gridy) {
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.insets = new java.awt.Insets(3, 5, 3, 5);
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        return c;
    }

    private int tambahBarisKepJudul(widget.PanelBiasa panel, int row, String judul) {
        // [Layout 3-kolom] Judul section = strip gaya Triase (buatHeaderStripKep)
        // menggantikan label bold biasa. Satu funnel ini otomatis berlaku utk
        // SEMUA 25 judul section di panelKepAtas/Dua/Tiga/Diag/Obat/Empat.
        java.awt.GridBagConstraints c = kepConstraint(0, row);
        c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new java.awt.Insets(row == 0 ? 4 : 14, 5, 6, 5);
        widget.PanelBiasa strip = buatHeaderStripKep(judul);
        panel.add(strip, c);
        // [Bagian C] Satu funnel ini juga jadi tempat pendaftaran rail
        // navigasi - daftar bagian ikut benar dgn sendirinya kalau nanti ada
        // section ditambah/dipindah, tanpa daftar judul kedua yg bisa basi.
        seksiKep.add(new SeksiKep(judul, strip, panel, seksiKep.size()));
        return row + 1;
    }

    /**
     * Anggaran lebar SAAT PANEL DIBANGUN utk memutuskan sebuah label perlu
     * dibungkus HTML supaya turun baris. Sejak Sesi 3c angka ini HANYA melayani
     * sel grid 4-kolom (buatSelKepField); baris biasa punya kolom label sendiri
     * yg lebarnya tetap, LEBAR_LABEL_KEP.
     */
    private static final int LEBAR_ISI_KEP = 330;

    /**
     * [Bagian D] Lebar KOLOM LABEL baris pengkajian - angka dari mockup.
     *
     * DIPATOK, bukan dihitung dari label terpanjang: satu baris = satu panel
     * pembungkus sendiri, jadi label antar baris cuma bisa sejajar kalau
     * kolomnya sama lebar di SEMUA pembungkus. Kalau dihitung dari isi, tiap
     * bagian akan punya kolom label berbeda lebar dan barisnya kembali tidak
     * lurus - persisnya gejala yg mau dihilangkan sesi ini.
     */
    private static final int LEBAR_LABEL_KEP = 232;

    /**
     * Lebar MINIMUM kontrol satu-baris (TextBox/ComboBox/Tanggal/Jam).
     *
     * Cuma MENAIKKAN, tidak pernah menurunkan: kontrol yg memang lebar tidak
     * boleh ikut dikecilkan. Perlu karena lebar kotak teks per field masih
     * warisan setBounds() form lama - ada yg 85px, ada yg 300px di baris
     * berurutan, dan itu terbaca sebagai kolom kontrol bergerigi di render
     * Sesi 3b.
     */
    private static final int LEBAR_KONTROL_KEP = 260;

    /**
     * Batas ATAS lebar kontrol per tipe - angka dari Bagian D butir 5
     * (TextBox 360px, ComboBox 280px).
     *
     * Perlu batas atas, bukan cuma batas bawah LEBAR_KONTROL_KEP: lebar warisan
     * setBounds() form lama ada yg 85px dan ada yg 300-an px di baris
     * berurutan, jadi kolom kontrol dijepit dari DUA sisi supaya ujung kanan
     * kotak teks sejajar, bukan cuma ujung kirinya.
     */
    private static final int MAKS_TEXTBOX_KEP = 360;
    private static final int MAKS_COMBO_KEP = 280;

    /**
     * [Bagian D butir 3] Garis pemisah antar baris pengkajian: SATU garis bawah
     * 1px, bukan kotak.
     *
     * Kotak penuh yg terlihat mengelilingi tiap baris di render Sesi 3c bukan
     * dipasang di sini - itu warisan konstruktor widget.PanelBiasa, yg diam-diam
     * memasang LineBorder(239,244,234) pada SETIAP PanelBiasa tanpa argumen
     * (lihat src/widget/PanelBiasa.java:38). Efeknya 105 kotak bersarang di
     * dalam kartu halaman yg juga berkotak - persis "TitledBorder bersarang" yg
     * dilarang prompt, cuma tanpa judul. Border baris DIGANTI, bukan ditambah.
     */
    private static final java.awt.Color PEMISAH_BARIS_KEP = new java.awt.Color(0xF2, 0xF3, 0xF0);

    /**
     * Padding vertikal di dalam pembungkus baris.
     *
     * Prompt memberi dua angka: 10px normal, 7px "bila mode rapat". Dipakai yg
     * RAPAT untuk seluruh form ini - bukan penyimpangan, tapi karena form ini
     * memang mode rapat: 105 baris terpakai, dan bagian 10 (Diagnosis &
     * Tindakan) saja sudah 36 baris / 1592px tinggi kartu. Selisih 3px x 2 sisi
     * x 36 baris = 216px tambahan scroll di satu bagian, tanpa satu pun baris
     * baru yg terbaca lebih jelas.
     */
    private static final int PADDING_BARIS_KEP = 9;

    /**
     * Penanda tipe pembungkus baris pengkajian.
     *
     * Perlu tipe sendiri (bukan sekadar widget.PanelBiasa) supaya
     * rapikanIsiKep() bisa MENGURANGI anggaran lebar isi sebesar kolom label
     * saat turun ke dalam baris. Tanpa itu checklist di dalam baris ditata
     * selebar anggaran halaman penuh, padahal ruang yg tersisa di kanan label
     * ~232px lebih sempit - dan kartu halamannya ikut jebol melebar.
     */
    private static class BarisKep extends widget.PanelBiasa {
        private static final long serialVersionUID = -1L;
        /**
         * Baris berlabel memakan 232px di kiri; baris tanpa label tidak. Dibawa
         * sbg keadaan, bukan ditebak dari jumlah anak, supaya rapikanIsiKep()
         * tidak mengecilkan checklist selebar-halaman yg justru harus penuh.
         */
        private boolean berlabel;
    }

    private int tambahBarisKepField(widget.PanelBiasa panel, int row, String label, boolean wajib,
            java.awt.Component field) {
        barisPengkajian(panel, row, label, wajib, field);
        return row + 1;
    }

    private int tambahBarisKepField(widget.PanelBiasa panel, int row, String label, java.awt.Component field) {
        return tambahBarisKepField(panel, row, label, false, field);
    }

    /**
     * [Bagian D] Satu BARIS pengkajian: label di KIRI dalam kolom tetap
     * LEBAR_LABEL_KEP, kontrol di kanannya. Memulangkan pembungkusnya supaya
     * baris bersyarat (Gravida/Para/Abortus/HPHT) bisa disembunyikan sebagai
     * SATU komponen.
     *
     * Beda dari signature di prompt (`barisPengkajian(JPanel, String, boolean,
     * JComponent)`) dalam satu hal yg disengaja: ada parameter `row`.
     * Penghitung baris yg sama juga menempatkan strip judul bagian
     * (tambahBarisKepJudul) dan sebelas baris yg ditulis manual lewat
     * kepConstraint(); kalau helper ini menghitung gridy-nya sendiri, urutan
     * baris punya DUA sumber kebenaran yg bisa diam-diam bergeser satu sama
     * lain. Tipe host/kontrol juga mengikuti tipe yg sudah dipakai 71
     * pemanggilnya, bukan JPanel/JComponent.
     */
    private widget.PanelBiasa barisPengkajian(widget.PanelBiasa host, int row, String label,
            boolean wajib, java.awt.Component kontrol) {
        java.awt.GridBagConstraints c = kepConstraint(0, row);
        // REMAINDER, bukan 2: dgn gridwidth=2 baris cuma membentang sampai kolom
        // 1, dan lebar kolom 0-1 ditentukan baris TERLEBAR di panel itu -
        // hasilnya tepi kanan bergerigi PER BAGIAN. Terukur sesudah Sesi 3b:
        // baris SKRINING GIZI DEWASA 850px vs SKRINING GIZI ANAK 771px, di panel
        // yg dua-duanya 1218px, sementara strip judulnya (yg memang REMAINDER)
        // 1206px. Dgn REMAINDER baris ikut sampai kolom pengisi
        // pasangGlueKananKep(), jadi tepi kanannya sama dgn strip judulnya.
        c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.anchor = java.awt.GridBagConstraints.NORTHWEST;
        // Nol di atas-bawah: jarak vertikal antar baris seluruhnya diurus
        // PADDING_BARIS_KEP di DALAM pembungkusnya. Kalau dibagi dua tempat,
        // garis pemisah baris jadi melayang di tengah celah, bukan menempel
        // sebagai batas antar baris seperti di mockup.
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        widget.PanelBiasa baris = buatBarisKepField(label, wajib, kontrol);
        host.add(baris, c);
        return baris;
    }

    /**
     * Badan baris: [label 232px][kontrol]. Label rata KIRI dan rata ATAS - kalau
     * labelnya turun dua baris, kontrolnya tetap sejajar baris pertama label,
     * tidak ikut turun ke tengah.
     *
     * Label KOSONG tidak berarti kolom label kosong selebar 232px: kontrolnya
     * dipasang di gridx 0 membentang dua kolom, jadi baris tanpa label (daftar
     * centang tindakan di panelKepTiga, dsb) tetap mulai dari tepi kiri seperti
     * sebelum sesi ini, tidak menjorok 232px.
     */
    private widget.PanelBiasa buatBarisKepField(String label, boolean wajib, java.awt.Component kontrol) {
        BarisKep baris = new BarisKep();
        baris.setOpaque(false);
        // setBorder MENGGANTI kotak LineBorder bawaan konstruktor PanelBiasa
        // (lihat PEMISAH_BARIS_KEP) dgn garis bawah + padding vertikal.
        baris.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, PEMISAH_BARIS_KEP),
                javax.swing.BorderFactory.createEmptyBorder(
                        PADDING_BARIS_KEP, 0, PADDING_BARIS_KEP, 0)));
        baris.setLayout(new java.awt.GridBagLayout());
        boolean adaLabel = label != null && !label.isEmpty();
        baris.berlabel = adaLabel;
        if (adaLabel) {
            java.awt.GridBagConstraints cl = new java.awt.GridBagConstraints();
            cl.gridx = 0;
            cl.gridy = 0;
            cl.anchor = java.awt.GridBagConstraints.NORTHWEST;
            cl.fill = java.awt.GridBagConstraints.NONE;
            cl.weightx = 0.0;
            cl.insets = new java.awt.Insets(3, 0, 0, 10);
            baris.add(labelBarisKep(label, wajib), cl);
        }
        java.awt.GridBagConstraints ck = new java.awt.GridBagConstraints();
        ck.gridx = adaLabel ? 1 : 0;
        ck.gridy = 0;
        ck.gridwidth = adaLabel ? 1 : 2;
        ck.anchor = java.awt.GridBagConstraints.NORTHWEST;
        boolean isiLebar = (kontrol instanceof javax.swing.JComponent)
                && Boolean.TRUE.equals(((javax.swing.JComponent) kontrol).getClientProperty("fillWidth"));
        ck.fill = isiLebar ? java.awt.GridBagConstraints.HORIZONTAL : java.awt.GridBagConstraints.NONE;
        // Bobot ditaruh di kolom KONTROL, bukan kolom label: sisa lebar baris
        // dibuang ke kanan kontrol (kolomnya melebar, kontrolnya sendiri
        // fill=NONE jadi tetap sebesar isinya), sehingga kolom label tidak
        // pernah ikut melar dan 232px-nya sama di semua baris.
        ck.weightx = 1.0;
        seragamkanKontrolKep(kontrol);
        baris.add(kontrol, ck);
        return baris;
    }

    /**
     * Label kolom kiri: SELALU dibungkus HTML berlebar tetap, lalu preferredSize
     * dan minimumSize-nya dipatok 232px.
     *
     * Dua-duanya perlu, bukan salah satu. HTML berlebar tetap yg membuat label
     * panjang TURUN BARIS di dalam kolomnya (tanpa itu label 400px mendorong
     * kontrol ke kanan dan baris itu sendiri yg jadi tidak lurus); patokan
     * ukuran yg membuat label PENDEK tetap memakan 232px penuh, sehingga
     * kontrol di semua baris mulai di x yg sama. Satuannya 'pt' lewat
     * htmlLebarKep() - Swing memetakan 1 px CSS = 1,3 pt sedangkan 1 pt = 1
     * piksel perangkat, jadi 'px' selalu melebar 30%.
     */
    private widget.Label labelBarisKep(String teks, boolean wajib) {
        widget.Label lbl = new widget.Label();
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        // 12,5pt: angka dari Bagian D butir 3, dan sekaligus yg membedakan label
        // BARIS (12,5) dari mini-label sel grid 4 kolom (10,5) - hierarki itu
        // hilang selama keduanya 11pt seperti sebelum sesi ini. deriveFont(float)
        // wajib: konstruktor Font hanya menerima ukuran bulat.
        lbl.setFont(new java.awt.Font("Segoe UI",
                wajib ? java.awt.Font.BOLD : java.awt.Font.PLAIN, 12).deriveFont(12.5f));
        // Field wajib ditegaskan TIGA cara sekaligus (bold, TEXT_NORMAL, bintang
        // DANGER) - bukan berlebihan: bintang sendirian 4px lebar dan hilang di
        // antara 105 baris, sedangkan warna sendirian tidak terbaca oleh yg buta
        // warna.
        lbl.setForeground(wajib ? widget.ThemeConfig.TEXT_NORMAL : widget.ThemeConfig.TEXT_MUTED);
        lbl.setText(htmlLabelKep(teks, wajib));
        int tinggi = Math.max(19, lbl.getPreferredSize().height);
        lbl.setPreferredSize(new java.awt.Dimension(LEBAR_LABEL_KEP, tinggi));
        lbl.setMinimumSize(new java.awt.Dimension(LEBAR_LABEL_KEP, tinggi));
        return lbl;
    }

    /**
     * Label wajib dapat bintang berwarna DANGER di ujung teksnya - satu penanda
     * saja, tanpa teks tambahan, supaya tidak ada yg terlihat seperti field
     * baru. Belum ada pemanggil dgn wajib=true: daftar 17 field wajibnya
     * Bagian E.
     */
    private String htmlLabelKep(String teks, boolean wajib) {
        String bintang = wajib
                ? " <span style='color:#" + String.format("%06x",
                        widget.ThemeConfig.DANGER.getRGB() & 0xFFFFFF) + "'>*</span>"
                : "";
        return "<html><div style='width:" + LEBAR_LABEL_KEP + "pt'>"
                + escHtmlKep(teks) + bintang + "</div></html>";
    }

    /**
     * Lebar kontrol satu-baris dijepit ke rentang [LEBAR_KONTROL_KEP, batas
     * atas per tipe] - Bagian D butir 5.
     *
     * Hanya widget teks/pilihan tunggal. Panel checklist, tabel, dan area teks
     * sengaja TIDAK disentuh: lebar mereka memang isinya sendiri, dan itu sudah
     * diurus rapikanIsiKep().
     */
    private void seragamkanKontrolKep(java.awt.Component k) {
        buatKotakKep(k);
        if (!(k instanceof widget.TextBox || k instanceof widget.ComboBox
                || k instanceof widget.Tanggal || k instanceof widget.Jam)) {
            return;
        }
        int maks = (k instanceof widget.TextBox) ? MAKS_TEXTBOX_KEP : MAKS_COMBO_KEP;
        java.awt.Dimension d = k.getPreferredSize();
        int lebar = Math.max(LEBAR_KONTROL_KEP, Math.min(maks, d.width));
        if (lebar != d.width) {
            k.setPreferredSize(new java.awt.Dimension(lebar, Math.max(23, d.height)));
        }
    }

    /**
     * Jumlah kolom grid tanda vital / GCS di halaman satu-bagian-per-layar.
     * Empat sesuai mockup Bagian D: 4 x (sel ~145px + insets 10) = ~620px, masih
     * separuh dari lebar halaman 1240px, jadi label terpanjang ("Frekuensi Nafas
     * (x/menit)", 129px) tetap satu baris tanpa dibungkus HTML.
     */
    private static final int KOLOM_VITAL_KEP = 4;

    /**
     * [Bagian D] Sel field di GRID 4 KOLOM - dipakai tanda vital & GCS.
     *
     * Bedanya dari tambahBarisKepField() cuma dua: gridx mengikuti nomor kolom
     * (tiap sel gridwidth 2, jadi kolom 0-1, 2-3, 4-5, 6-7), dan weightx=0.
     * Bobot 0 itu penting - kalau tiap sel berbobot, sisa lebar halaman dibagi
     * rata ke keempat kolom dan gridnya merenggang selebar 1188px, bukan grid
     * rapat spt di mockup. Sisa lebar diserap satu kolom pengisi di kanan
     * panelnya sendiri.
     */
    private void tambahSelKepField(widget.PanelBiasa panel, int kol, int row, String label, java.awt.Component field) {
        java.awt.GridBagConstraints c = kepConstraint(kol * 2, row);
        c.gridwidth = 2;
        c.weightx = 0.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.anchor = java.awt.GridBagConstraints.NORTHWEST;
        c.insets = new java.awt.Insets(2, 5, 4, 5);
        panel.add(buatSelKepField(label, field), c);
    }

    /**
     * Sel "label kecil di ATAS, field di bawah" - sejak Sesi 3c badan ini
     * HANYA melayani grid 4 kolom (tambahSelKepField). Baris biasa pindah ke
     * buatBarisKepField(): label di kiri dalam kolom 232px. Sengaja tidak
     * disatukan - 4 x (232 + kontrol) tidak mungkin masuk ke lebar halaman,
     * jadi sel grid memang harus menumpuk label di atas kontrolnya.
     */
    private widget.PanelBiasa buatSelKepField(String label, java.awt.Component field) {
        widget.PanelBiasa baris = new widget.PanelBiasa();
        baris.setOpaque(false);
        baris.setLayout(new java.awt.BorderLayout(0, 2));
        if (!label.isEmpty()) {
            widget.Label lbl = new widget.Label();
            lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            // 10,5pt sesuai Bagian D butir 7 - mini-label, sengaja lebih kecil
            // dari label baris (12,5) supaya sel grid tidak terbaca sebagai
            // baris penuh. Warna DIBIARKAN TEXT_MUTED, bukan TEXT_FAINT yg
            // diminta prompt: TEXT_FAINT cuma 2,64:1 di atas putih (audit
            // kontras Sesi Triase), dan usul penggantinya masih menunggu
            // keputusan user. Angka tanda vital tidak boleh jadi yg pertama
            // membayar keputusan itu.
            lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11).deriveFont(10.5f));
            lbl.setForeground(widget.ThemeConfig.TEXT_MUTED);
            lbl.setText(label);
            // Label panjang dibungkus HTML dgn lebar tetap supaya IKUT TURUN
            // BARIS, tidak memaksa kartu kolom melebar. Lewat htmlLebarKep()
            // supaya satuannya piksel perangkat - versi lama menulis 'px'
            // langsung sehingga label yg melewati 330px justru DILEBARKAN
            // jadi 429px, kebalikan dari maksudnya.
            if (lbl.getPreferredSize().width > LEBAR_ISI_KEP) {
                lbl.setText(htmlLebarKep(LEBAR_ISI_KEP, label));
            }
            baris.add(lbl, java.awt.BorderLayout.NORTH);
        }
        // WEST (bukan CENTER) supaya field duduk pas di lebar kontennya dan
        // TIDAK melebar mengikuti lebar kolom - permintaan user soal "ruang
        // kosong" di dalam field/dropdown yg sebenarnya sempit.
        baris.add(field, java.awt.BorderLayout.WEST);
        return baris;
    }

    /**
     * Escape minimal utk teks label yg dibungkus HTML (lihat tambahBarisKepField).
     */
    private String escHtmlKep(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Bungkus teks jadi label HTML berlebar TETAP, satuannya PIKSEL PERANGKAT.
     *
     * Satuan CSS yg dipakai 'pt', BUKAN 'px' - dan itu bukan salah tulis.
     * Mesin HTML Swing memetakan 1px = 1.3pt (konstanta lengthMapping di
     * javax.swing.text.html.CSS.LengthUnit), sementara 1pt = 1 piksel.
     * Jadi "width:255px" melahirkan label selebar 331px, 30% lebih lebar
     * dari yg diminta, sedangkan "width:255pt" tepat 255px. Terukur:
     * 180px->233, 200px->260, 255px->331, 330px->429; 180pt->180,
     * 255pt->255, 330pt->330 - kelipatan 1,3 yg persis di tiap sampel.
     *
     * Ini pernah membuat SELURUH baris rail 352px dan kartu progres 357px
     * di zona rail yg cuma 300px dgn HORIZONTAL_SCROLLBAR_NEVER, jadi
     * judul bagian terpotong di kanan. Semua pembungkus HTML berlebar
     * tetap di form ini WAJIB lewat method ini, jangan tulis 'px' lagi.
     */
    private String htmlLebarKep(int lebarPx, String teks) {
        return "<html><div style='width:" + lebarPx + "pt'>" + escHtmlKep(teks) + "</div></html>";
    }

    /**
     * Helper rekursif untuk mengosongkan teks komponen input saat dimatikan / disembunyikan.
     */
    private void resetKomponenKondisional(java.awt.Component c) {
        if (c == null) return;
        if (c instanceof javax.swing.text.JTextComponent) {
            ((javax.swing.text.JTextComponent) c).setText("");
        } else if (c instanceof java.awt.Container) {
            for (java.awt.Component anak : ((java.awt.Container) c).getComponents()) {
                resetKomponenKondisional(anak);
            }
        }
    }

    /**
     * Helper reusable untuk memasang logika kondisional pada CekBox terhadap komponen target.
     * Komponen target akan disembunyikan secara default, dan hanya muncul ketika checkbox dicentang.
     */
    private void pasangKondisionalCek(final widget.CekBox cek, final java.awt.Component... target) {
        if (cek == null || target == null || target.length == 0) return;
        for (java.awt.Component c : target) {
            if (c != null) c.setVisible(false);
        }
        cek.addItemListener((java.awt.event.ItemEvent e) -> {
            boolean aktif = cek.isSelected();
            for (java.awt.Component c : target) {
                if (c != null) {
                    c.setVisible(aktif);
                    if (!aktif) {
                        resetKomponenKondisional(c);
                    }
                }
            }
            java.awt.Container parent = cek.getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
            if (FormInput != null) {
                FormInput.revalidate();
                FormInput.repaint();
            }
            hitungProgresKep();
        });
    }

    // -------------------------------------------------------------------
    // Penyesuai lebar isi (LAYOUT-ONLY). Dipanggil sekali dari
    // buatHalamanKep() utk tiap KARTU HALAMAN, lalu turun rekursif ke semua
    // sub-panel. (Sebelum Bagian D pemanggilnya susunTigaKolomKep() dgn
    // anggaran 330px per kartu kolom; method itu sudah dihapus.) Yg diubah
    // HANYA layout manager / GridBagConstraints / preferredSize - komponen,
    // urutan, isi, listener & logika data tidak disentuh sama sekali.
    // -------------------------------------------------------------------

    /**
     * Warna kotak yg dipasang DIAM-DIAM oleh konstruktor tanpa argumen
     * widget.PanelBiasa() - lihat src/widget/PanelBiasa.java:38.
     */
    private static final java.awt.Color KOTAK_BAWAAN_KEP = new java.awt.Color(239, 244, 234);

    /**
     * Buang kotak bawaan konstruktor widget.PanelBiasa() dari satu komponen.
     *
     * Kenapa disapu rekursif dan bukan diperbaiki di ~40 tempat pemanggilan:
     * setiap `new widget.PanelBiasa()` di form ini - pembungkus grup radio
     * (panelAlergi, panelGeri1..3, panelJatuh*, panelSholat, dst), panel
     * checklist, sel grid, panel bagian hasil pecahSatuIndukKep(), sampai
     * batang aksen 4px - langsung membawa LineBorder(239,244,234) tanpa
     * satu baris kode pun memintanya. Hasilnya kotak bersarang tiga sampai
     * empat tingkat di dalam kartu halaman yg juga berkotak: persis
     * "TitledBorder bersarang" yg dilarang prompt, cuma tanpa judul.
     *
     * Penyapuan diikat ke WARNA, bukan ke tipe border, supaya border yg
     * memang dipasang sengaja tidak ikut terbuang: kartu halaman pakai
     * BORDER_SOFT (0xE4E6E2), chip pakai 0xD8DBD6/PRIMARY, pemisah baris
     * pakai MatteBorder 0xF2F3F0, garis sub-judul 0xECEEEA - tidak ada satu
     * pun yg 0xEFF4EA. Kalau kelak ada border sengaja berwarna sama, dia akan
     * hilang tanpa error, jadi angka ini jangan dipakai untuk apa pun yg lain.
     */
    private void buangKotakBawaanKep(java.awt.Component k) {
        if (!(k instanceof javax.swing.JComponent)) {
            return;
        }
        javax.swing.border.Border b = ((javax.swing.JComponent) k).getBorder();
        if (b instanceof javax.swing.border.LineBorder
                && KOTAK_BAWAAN_KEP.equals(((javax.swing.border.LineBorder) b).getLineColor())) {
            ((javax.swing.JComponent) k).setBorder(null);
        }
    }

    /**
     * Turun rekursif: sesuaikan isi wadah ke anggaran lebar `budget` piksel.
     *
     * `budget` jadi PARAMETER, bukan lagi konstanta LEBAR_ISI_KEP, karena
     * Bagian D mengubah anggarannya: tata letak 3 kolom memberi tiap kartu
     * cuma 330px, sedangkan satu-bagian-per-layar memberi ~1240px. Angka yg
     * sama tidak bisa melayani keduanya - dgn 330 di halaman selebar layar,
     * checklist tetap menumpuk 2 kolom sempit dan sisa 900px dibiarkan kosong.
     */
    private void rapikanIsiKep(java.awt.Container wadah, int budget) {
        buangKotakBawaanKep(wadah);
        buatKotakKep(wadah);
        for (java.awt.Component k : wadah.getComponents()) {
            // Dipanggil utk SETIAP anak, di luar rantai if di bawah: kotak
            // bawaan PanelBiasa menempel pada panel berisi maupun panel kosong
            // (mis. batang aksen 4px), dan keduanya harus bersih.
            buangKotakBawaanKep(k);
            buatKotakKep(k);
            // Cuma JPanel (widget.PanelBiasa dst) yg ditelusuri isinya.
            // JComboBox/JScrollPane juga punya child internal, tapi harus
            // diperlakukan sbg 1 komponen utuh - kalau ditelusuri, lebarnya
            // tak pernah ikut dikecilkan.
            boolean wadahLain = (k instanceof javax.swing.JPanel)
                    && ((java.awt.Container) k).getComponentCount() > 0;
            if (wadahLain) {
                // [Bagian D] Anggaran DIKURANGI kolom label begitu masuk ke
                // pembungkus baris BERLABEL: ruang yg benar-benar tersisa utk
                // kontrol = budget - 232 - jarak. Kalau tetap budget penuh,
                // checklist di dalam baris ditata selebar halaman, lalu barisnya
                // jadi (232 + selebar halaman) dan kartunya jebol melebar -
                // gejalanya scrollbar horizontal muncul kembali, bukan error.
                // Baris TANPA label tidak dikurangi: kontrolnya memang mulai
                // dari tepi kiri dan harus boleh memakai lebar penuh.
                boolean barisBerlabel = (k instanceof BarisKep) && ((BarisKep) k).berlabel;
                rapikanIsiKep((java.awt.Container) k,
                        barisBerlabel ? budget - LEBAR_LABEL_KEP - 10 : budget);
            } else if (k instanceof javax.swing.JScrollPane
                    || k instanceof javax.swing.JTabbedPane) {
                if (k == scrollPane1) {
                    k.setPreferredSize(new java.awt.Dimension(580, 100));
                } else {
                    k.setPreferredSize(new java.awt.Dimension(budget, k.getPreferredSize().height));
                }
            } else if (k.getPreferredSize().width > budget) {
                // Komponen tunggal (TextBox/ComboBox/ScrollPane tabel) yg
                // lebarnya warisan desain 1 kolom (dulu form selebar 870px).
                k.setPreferredSize(new java.awt.Dimension(budget, k.getPreferredSize().height));
            }
        }
        // Bukan rantai else-if: sempitkanAlirKep() MENGGANTI FlowLayout jadi
        // GridLayout, jadi panel yg baru diubahnya harus ikut diperiksa lagi
        // di cabang GridLayout - kalau tidak, checklist yg asalnya FlowLayout
        // tak pernah kena pelukChipGridKep() dan chipnya tetap melar.
        if (wadah.getLayout() instanceof java.awt.FlowLayout) {
            sempitkanAlirKep(wadah, budget);
        }
        if (wadah.getLayout() instanceof java.awt.GridLayout) {
            aturKolomGridKep(wadah, budget);
        } else if (wadah.getLayout() instanceof java.awt.GridBagLayout) {
            // Pelipatan HANYA utk anggaran sempit. Panel spt panelKeadaanUmum,
            // panelFlacc, dan panelKpVital SEKARANG memang grid 4 kolom
            // mini-label yg diminta Bagian D butir 7 (sel di gridx 0/2/4/6,
            // masing2 gridwidth 2). Dilipat cuma karena kartu 330px tak
            // memuatnya; di halaman selebar layar melipatnya berarti membuang
            // desain yg baru saja dipasang.
            if (budget < LEBAR_LIPAT_KEP) {
                lipatDuaKolomKep(wadah);
            }
            pelukChipGbcKep(wadah);
        }
    }

    /**
     * Batas anggaran di mana baris "2 pasang label+field" masih dilipat jadi
     * 1 pasang. Satu pasang label+field terlebar di form ini ~300px, jadi 2
     * pasang butuh ~620px; 700 memberi kelonggaran tanpa menyentuh kasus
     * kartu 330px.
     */
    private static final int LEBAR_LIPAT_KEP = 700;

    /**
     * Versi GridBagLayout dari pelukChipGridKep(): buang fill=HORIZONTAL dari
     * tiap chip supaya pil-nya memeluk teks, bukan melar selebar kolom sel.
     *
     * Perlu terpisah karena tidak semua grup pilihan lahir dari
     * panelChecklistKep() yg ber-GridLayout - sebagian ditempel langsung ke
     * panel GridBag lewat kepConstraint(), yg fill-nya HORIZONTAL. Di render
     * PNG kedua sisa gejalanya masih kelihatan: tiga pilihan "Penurunan BB 6
     * Bulan Terakhir" sama lebar semua (ikut chip terlebar), padahal chip di
     * checklist sebelahnya sudah memeluk teksnya masing-masing.
     *
     * getConstraints() memulangkan KLONA, jadi wajib setConstraints() lagi.
     * Yg diubah cuma fill & anchor - gridx/gridy/insets/weight dibiarkan,
     * jadi posisi & jarak antar baris tidak bergeser sedikit pun.
     */
    private void pelukChipGbcKep(java.awt.Container p) {
        java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) p.getLayout();
        for (java.awt.Component k : p.getComponents()) {
            if (!(k instanceof javax.swing.JToggleButton)) {
                continue;
            }
            java.awt.GridBagConstraints c = gbl.getConstraints(k);
            if (c.fill == java.awt.GridBagConstraints.NONE
                    && c.anchor == java.awt.GridBagConstraints.WEST) {
                continue;
            }
            c.fill = java.awt.GridBagConstraints.NONE;
            c.anchor = java.awt.GridBagConstraints.WEST;
            gbl.setConstraints(k, c);
        }
    }

    /**
     * Panel checklist ber-GridLayout (panelChecklistKep) jumlah kolomnya
     * dipatok saat dibangun - angka yg dipilih untuk kartu 330px. Kolomnya
     * dihitung ulang di sini dari chip TERLEBAR + anggaran nyata, alasan yg
     * sama seperti sempitkanAlirKep(): sesudah chip dipasang tiap pilihan
     * tumbuh ~16px karena padding pil.
     *
     * [Sesi 3c] Sekarang boleh MENAMBAH kolom, tidak cuma mengurangi. Larangan
     * lamanya ("menambah kolom mengubah pengelompokan visual yg disengaja")
     * ternyata melindungi pengelompokan yg tidak pernah ada: angka kolom di
     * seluruh pemanggil panelChecklistKep() dipilih supaya muat di kartu 330px,
     * bukan karena pilihan ke-1 dan ke-2 sebaris punya arti. Akibatnya di
     * halaman selebar 1188px, ASESMEN PSIKOLOGIS terender 2 kolom dgn ~800px
     * kosong di kanannya - terlihat di render halaman 5, dan mockup memintanya
     * mengalir selebar baris.
     *
     * Dibatasi jumlah anaknya sendiri supaya tidak pernah ada kolom kosong di
     * kanan yg menggeser kolom pengisi pelukChipGbcKep().
     */
    private void aturKolomGridKep(java.awt.Container box, int budget) {
        if (box.getComponentCount() < 2) {
            return;
        }
        java.awt.GridLayout grid = (java.awt.GridLayout) box.getLayout();
        int jarak = grid.getHgap();
        int lebarMaks = 0;
        for (java.awt.Component k : box.getComponents()) {
            lebarMaks = Math.max(lebarMaks, k.getPreferredSize().width);
        }
        if (lebarMaks < 1) {
            return;
        }
        int kolom = (budget + jarak) / (lebarMaks + jarak);
        if (kolom < 1) {
            kolom = 1;
        }
        if (kolom > box.getComponentCount()) {
            kolom = box.getComponentCount();
        }
        if (grid.getColumns() > 0 && kolom != grid.getColumns()) {
            box.setLayout(new java.awt.GridLayout(0, kolom, jarak, grid.getVgap()));
            grid = (java.awt.GridLayout) box.getLayout();
        }
        pelukChipGridKep(box, grid.getColumns() > 0 ? grid.getColumns() : kolom,
                jarak, grid.getVgap());
    }

    /**
     * Bikin chip MEMELUK teksnya, bukan melar selebar sel grid.
     *
     * Ini temuan dari render PNG pertama, bukan dari angka: GridLayout selalu
     * memberi semua sel ukuran yg SAMA dan mengisinya penuh, jadi tiap pil
     * jadi persegi panjang selebar kolom - checklist "Manajemen Jalan Nafas"
     * dst. tergambar sebagai garis kotak sepanjang ~570px, dan pasangan
     * "Ya"/"Tidak" jadi sama lebar walau teksnya beda panjang. Sebagai radio
     * button biasa itu tak kelihatan (latarnya transparan); begitu diberi
     * latar + garis pil, melarnya langsung terlihat salah.
     *
     * GridLayout ditukar GridBagLayout dgn kolom SAMA BANYAK, tiap sel
     * anchor=WEST fill=NONE, plus satu kolom pengisi berbobot di kanan supaya
     * sisa ruang dibuang ke sana, bukan dibagi ke chip. Urutan komponen,
     * jumlah kolom, listener, dan nama variabel tidak berubah - murni layout.
     *
     * Panel CAMPURAN (chip + kotak teks "Sebutkan", mis. "Riwayat Alergi:
     * Tidak / Ya, Sebutkan") ikut dikonversi, tapi yg bukan chip tetap
     * fill=HORIZONTAL weightx=1 supaya kotak teksnya masih melar seperti dulu.
     * Versi pertama method ini menolak panel campuran sama sekali, dan
     * render ketiga membuktikan itu salah: 28 chip di 12 panel campuran masih
     * melar sementara tetangganya sudah memeluk teks.
     */
    private void pelukChipGridKep(java.awt.Container box, int kolom, int hgap, int vgap) {
        if (kolom < 1 || box.getComponentCount() < 1) {
            return;
        }
        boolean adaChip = false, adaLain = false;
        for (java.awt.Component k : box.getComponents()) {
            if (k instanceof javax.swing.JToggleButton) {
                adaChip = true;
            } else {
                adaLain = true;
            }
        }
        if (!adaChip) {
            return;
        }
        java.awt.Component[] anak = box.getComponents();
        box.removeAll();
        box.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.anchor = java.awt.GridBagConstraints.WEST;
        for (int i = 0; i < anak.length; i++) {
            boolean chip = anak[i] instanceof javax.swing.JToggleButton;
            c.gridx = i % kolom;
            c.gridy = i / kolom;
            c.insets = new java.awt.Insets(0, 0, vgap, c.gridx == kolom - 1 ? 0 : hgap);
            c.fill = chip ? java.awt.GridBagConstraints.NONE
                    : java.awt.GridBagConstraints.HORIZONTAL;
            c.weightx = chip ? 0.0 : 1.0;
            box.add(anak[i], c);
        }
        // Kolom pengisi HANYA kalau tak ada anak lain yg menuntut sisa ruang -
        // kalau ada kotak teks, dialah yg harus dapat sisanya, bukan glue.
        if (!adaLain) {
            c.gridx = kolom;
            c.gridy = 0;
            c.weightx = 1.0;
            c.fill = java.awt.GridBagConstraints.NONE;
            c.insets = new java.awt.Insets(0, 0, 0, 0);
            box.add(javax.swing.Box.createHorizontalGlue(), c);
        }
    }

    /**
     * Grup radio/cek ber-FlowLayout 1 baris: lebar MINIMUM-nya = lebar 1
     * baris penuh, jadi GridBagLayout tak bisa mengecilkannya. Diganti
     * GridLayout berkolom secukupnya supaya isinya turun baris.
     */
    private void sempitkanAlirKep(java.awt.Container box, int budget) {
        if (box.getComponentCount() < 2 || box.getPreferredSize().width <= budget) {
            return;
        }
        java.awt.FlowLayout alir = (java.awt.FlowLayout) box.getLayout();
        int jarak = alir.getHgap();
        int lebarMaks = 0;
        for (java.awt.Component k : box.getComponents()) {
            lebarMaks = Math.max(lebarMaks, k.getPreferredSize().width);
        }
        if (lebarMaks < 1) {
            return;
        }
        int kolom = (budget + jarak) / (lebarMaks + jarak);
        if (kolom < 1) {
            kolom = 1;
        }
        if (kolom < box.getComponentCount()) {
            box.setLayout(new java.awt.GridLayout(0, kolom, jarak, 6));
        }
    }

    /**
     * Panel grid "2 pasang label+field per baris" (gridx 0..3) dilipat jadi
     * 1 pasang per baris (gridx 0..1) supaya muat di kartu 1/3 lebar. Hanya
     * GridBagConstraints yg dipetakan ulang; glue pengisi lama dibuang krn
     * kolom kanannya sudah tidak ada.
     */
    private void lipatDuaKolomKep(java.awt.Container p) {
        java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) p.getLayout();
        boolean perlu = false;
        for (java.awt.Component k : p.getComponents()) {
            if (!(k instanceof javax.swing.Box.Filler) && gbl.getConstraints(k).gridx >= 2) {
                perlu = true;
                break;
            }
        }
        if (!perlu) {
            return;
        }
        java.util.ArrayList<java.awt.Component> buang = new java.util.ArrayList<>();
        for (java.awt.Component k : p.getComponents()) {
            if (k instanceof javax.swing.Box.Filler) {
                buang.add(k);
                continue;
            }
            java.awt.GridBagConstraints c = gbl.getConstraints(k);
            if (c.gridx < 0 || c.gridy < 0) {
                continue;
            }
            c.gridy = c.gridy * 2 + (c.gridx >= 2 ? 1 : 0);
            c.gridx = c.gridx % 2;
            c.insets = new java.awt.Insets(2, 5, 2, 5);
            gbl.setConstraints(k, c);
        }
        for (java.awt.Component k : buang) {
            p.remove(k);
        }
        java.awt.GridBagConstraints cGlue = new java.awt.GridBagConstraints();
        cGlue.gridx = 2;
        cGlue.gridy = 0;
        cGlue.weightx = 1.0;
        cGlue.fill = java.awt.GridBagConstraints.HORIZONTAL;
        p.add(javax.swing.Box.createHorizontalGlue(), cGlue);
    }

    // =====================================================================
    // [Layout 3-kolom gaya Triase] Helper penataan (LAYOUT-ONLY).
    // Semua method di bawah HANYA menata ulang penempatan komponen yg sudah
    // ada (reparent) + styling via widget.ThemeConfig - meniru helper senama
    // di RMTriaseIGD.java. Tidak ada logika data/CRUD/handler yg diubah.
    // =====================================================================

    /**
     * Latar SOLID utk widget.PanelBiasa. WAJIB dipakai - setBackground() saja
     * TIDAK PERNAH sampai ke layar.
     *
     * widget.PanelBiasa.paintComponent() (src/widget/PanelBiasa.java:63) melukis
     * ULANG seluruh bidangnya dgn gradien warnaAtas->warnaBawah setiap kali
     * isOpaque()==true, dan dua warna itu defaultnya PUTIH PENUH. Urutannya:
     * super.paintComponent() memang mengisi latar sesuai getBackground(), lalu
     * baris berikutnya menimpanya putih 100% (gradientImage opaque, digambar
     * selebar-setinggi panel). Border dan komponen anak lolos - keduanya
     * dilukis di paintBorder()/paintChildren() SESUDAH itu - jadi yg hilang
     * cuma ISIAN warnanya, dan itu sebabnya cacat ini lolos empat sesi.
     *
     * Korban terparah bukan yg samar: batang aksen 4px PRIMARY di strip
     * halaman dan batang 3px penanda bagian aktif di rail keduanya PanelBiasa
     * opaque TANPA anak, jadi yg terlihat 100% putih - dua penanda navigasi
     * paling penting di layout ini tidak pernah tampil. Yg samar (SURFACE_ALT
     * di kartu identitas, kartu progres, strip halaman) ikut hilang jadi putih.
     *
     * getBackground() tetap melaporkan warna yg diminta, jadi harness apa pun
     * yg memeriksa getBackground() akan berkata "aman" - sama persis pola
     * borderPainted=false di Sesi 3c. Karena itu warna diset ke KETIGANYA:
     * background biar getBackground() tidak menipu, warnaAtas/warnaBawah biar
     * benar2 terlukis.
     */
    private static void latarKep(widget.PanelBiasa p, java.awt.Color warna) {
        if (p == null || warna == null) {
            return;
        }
        p.setOpaque(true);
        p.setBackground(warna);
        p.setWarnaAtas(warna);
        p.setWarnaBawah(warna);
    }

    /** Garis tipis di kanan sub-judul, angka dari Bagian D butir 4. */
    private static final java.awt.Color GARIS_SUBJUDUL_KEP = new java.awt.Color(0xEC, 0xEE, 0xEA);

    /**
     * Sub-judul di DALAM satu halaman (mis. "PQRST", "FLACC"): teks 10,5pt BOLD
     * UPPERCASE TEXT_MUTED + garis 1px memanjang ke kanan - Bagian D butir 4.
     *
     * Sebelumnya berupa batang terisi SURFACE_ALT setinggi 32px. Batang itu
     * dibuang, bukan sekadar diperkecil fontnya: strip halaman 52px (
     * buatStripHalamanKep) JUGA batang SURFACE_ALT, jadi halaman seperti
     * "Diagnosis & Tindakan" yg memuat sembilan bagian menampilkan sepuluh
     * batang berwarna sama bertumpuk - dua tingkat hierarki yg terbaca satu
     * tingkat. Dengan gaya garis, batang tinggal satu per halaman dan sub-judul
     * membaca sebagai pemisah, bukan sebagai kepala halaman kedua.
     *
     * Strip ini hanya tampil kalau satu halaman memuat lebih dari satu bagian;
     * lihat buatHalamanKep(). Objeknya tetap widget.PanelBiasa dan tetap dipakai
     * sbg penanda identitas oleh pecahSeksiKep()/kelompokkanHalamanKep().
     */
    private widget.PanelBiasa buatHeaderStripKep(String judul) {
        widget.PanelBiasa strip = new widget.PanelBiasa();
        strip.setOpaque(false);
        // setBorder MENGGANTI LineBorder bawaan konstruktor PanelBiasa
        // (src/widget/PanelBiasa.java:38) - tanpa ini sub-judulnya berkotak.
        // Kanan 0 supaya garisnya benar2 sampai tepi kartu.
        strip.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 6, 0));
        strip.setLayout(new java.awt.GridBagLayout());

        widget.Label l = new widget.Label();
        l.setText(judul == null ? "" : judul.toUpperCase());
        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11).deriveFont(10.5f));
        l.setForeground(widget.ThemeConfig.TEXT_MUTED);

        // JPanel biasa, bukan PanelBiasa: yg dibutuhkan cuma satu persegi 1px
        // berwarna, sedangkan PanelBiasa membawa border bawaan + cache gradien.
        javax.swing.JPanel garis = new javax.swing.JPanel();
        garis.setOpaque(true);
        garis.setBackground(GARIS_SUBJUDUL_KEP);
        garis.setPreferredSize(new java.awt.Dimension(10, 1));

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        strip.add(l, c);
        // fill HORIZONTAL + anchor CENTER: garis melebar ke kanan tapi tingginya
        // tetap 1px dan duduk di tengah tinggi teks, bukan setinggi barisnya.
        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.insets = new java.awt.Insets(0, 10, 0, 0);
        strip.add(garis, c);
        return strip;
    }

    // -------------------------------------------------------------------
    // [Bagian D] Pilihan radio/checkbox digayakan jadi CHIP (pil). Dipasang
    // REKURSIF dari kartu kolom, bukan satu-satu per widget: form ini punya
    // seratusan titik radio/checkbox, dan cara rekursif otomatis ikut benar
    // kalau nanti ada pilihan ditambah/dipindah. Yg diubah HANYA
    // opaque/warna/border/font - ButtonGroup, teks, isSelected, dan seluruh
    // listener data TIDAK disentuh.
    // -------------------------------------------------------------------

    /** Latar chip terpilih: PRIMARY dilarutkan ~10% ke putih. */
    private static final java.awt.Color CHIP_PILIH_BG_KEP = new java.awt.Color(0xE6, 0xF2, 0xF3);

    /** Latar chip saat kursor di atasnya. */
    private static final java.awt.Color CHIP_HOVER_BG_KEP = new java.awt.Color(0xEE, 0xF1, 0xEC);

    /**
     * Latar induk terdekat yg benar-benar opaque. Dipakai BorderChipKep utk
     * menutup 4 sudut: kartu kolom (SURFACE) dan strip (SURFACE_ALT) berbeda
     * warna, jadi warnanya tidak boleh dipatok satu nilai.
     */
    private static java.awt.Color latarEfektifKep(java.awt.Component c) {
        java.awt.Container p = c.getParent();
        while (p != null) {
            if (p.isOpaque() && p.getBackground() != null) {
                return p.getBackground();
            }
            p = p.getParent();
        }
        return widget.ThemeConfig.SURFACE;
    }

    private static final java.awt.Color WARNA_BORDER_KOTAK_KEP = new java.awt.Color(0xD8, 0xDB, 0xD6);
    private static final javax.swing.border.Border BORDER_KOTAK_TEXTFIELD = javax.swing.BorderFactory
            .createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(WARNA_BORDER_KOTAK_KEP, 1),
                    javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8));
    private static final javax.swing.border.Border BORDER_KOTAK_GARIS = javax.swing.BorderFactory
            .createLineBorder(WARNA_BORDER_KOTAK_KEP, 1);

    /**
     * Border kotak/persegi untuk JCheckBox/JRadioButton sesuai tampilan
     * foto/mockup.
     */
    private static final class BorderChipKep implements javax.swing.border.Border {
        private java.awt.Color garis = WARNA_BORDER_KOTAK_KEP;

        void setGaris(java.awt.Color c) {
            garis = c;
        }

        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return new java.awt.Insets(4, 10, 4, 10);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(java.awt.Component c, java.awt.Graphics g,
                int x, int y, int w, int h) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setColor(garis);
            if ((c instanceof javax.swing.AbstractButton) && ((javax.swing.AbstractButton) c).isSelected()) {
                g2.setStroke(new java.awt.BasicStroke(1.5f));
            }
            g2.drawRect(x, y, w - 1, h - 1);
            g2.dispose();
        }
    }

    /**
     * Pastikan semua field input memiliki sudut siku-siku (kotak, arc=0)
     * dan berbingkai datar rapi (#D8DBD6) sesuai tampilan foto/mockup acuan.
     */
    private static void buatKotakKep(java.awt.Component c) {
        if (c == null) {
            return;
        }
        if (c instanceof javax.swing.JComponent) {
            javax.swing.JComponent jc = (javax.swing.JComponent) c;
            jc.putClientProperty("Component.arc", 0);
            jc.putClientProperty("Button.arc", 0);
            jc.putClientProperty("TextComponent.arc", 0);
            jc.putClientProperty("JTextField.arc", 0);
            jc.putClientProperty("ComboBox.arc", 0);
            jc.putClientProperty("JComponent.roundRect", false);

            if (c instanceof javax.swing.text.JTextComponent) {
                if (!(c instanceof javax.swing.JTextArea)) {
                    if (!Boolean.TRUE.equals(jc.getClientProperty("lookupField"))) {
                        jc.setBorder(BORDER_KOTAK_TEXTFIELD);
                    }
                }
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                        "background: #FFFFFF; inactiveBackground: #FFFFFF; disabledBackground: #FFFFFF;");
            } else if (c instanceof javax.swing.JComboBox) {
                jc.setBorder(BORDER_KOTAK_GARIS);
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                        "background: #FFFFFF; disabledBackground: #FFFFFF; buttonBackground: #FFFFFF; editableBackground: #FFFFFF;");
            } else if (c instanceof javax.swing.JScrollPane) {
                jc.setBorder(BORDER_KOTAK_GARIS);
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
            } else if (c instanceof uz.ncipro.calendar.JDateTimePicker) {
                jc.setBorder(BORDER_KOTAK_GARIS);
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style", "background: #FFFFFF; buttonBackground: #FFFFFF;");
            } else if (c instanceof javax.swing.AbstractButton) {
                if (c instanceof widget.Button) {
                    ((widget.Button) c).setRoundRect(false);
                }
            }
        }
        if (c instanceof java.awt.Container) {
            for (java.awt.Component child : ((java.awt.Container) c).getComponents()) {
                buatKotakKep(child);
            }
        }
    }

    /**
     * Turun rekursif dan gayakan tiap JRadioButton/JCheckBox jadi chip.
     * Hanya JPanel yg ditelusuri - sama seperti rapikanIsiKep(), supaya isi
     * internal JComboBox/JScrollPane/JTable tidak ikut disentuh.
     */
    private void gayaChipKep(java.awt.Container wadah) {
        for (java.awt.Component k : wadah.getComponents()) {
            if (k instanceof javax.swing.JRadioButton || k instanceof javax.swing.JCheckBox) {
                pasangChipKep((javax.swing.JToggleButton) k);
            } else if (k instanceof javax.swing.JPanel) {
                gayaChipKep((java.awt.Container) k);
            }
        }
    }

    /** Pasang gaya chip + pemantau status pada satu radio/checkbox. */
    private void pasangChipKep(final javax.swing.JToggleButton t) {
        // Penanda supaya aman dipanggil dua kali (mis. kalau nanti section
        // disusun ulang) - tanpa ini, tiap pemanggilan menambah 1 listener.
        if (Boolean.TRUE.equals(t.getClientProperty("chipKep"))) {
            return;
        }
        t.putClientProperty("chipKep", Boolean.TRUE);
        t.setOpaque(true);
        // WAJIB. BasicRadioButtonUI.installDefaults() memasang
        // borderPainted=false utk radio DAN checkbox (BasicCheckBoxUI
        // turunannya), dan AbstractButton.paintBorder() diam2 melewati
        // border kalau flag itu false. Akibatnya setBorder(BorderChipKep) di
        // bawah tetap MENGUBAH insets - jadi chip memakan ruang 8px kiri/kanan
        // seperti pil - tapi pil-nya TIDAK PERNAH dilukis. Gejalanya di render:
        // radio button biasa yg spasinya agak lega, tanpa error, dan tanpa ada
        // yg bisa ditangkap CekMelar (lebar nyata = lebar preferred, keduanya
        // sudah ikut insets) maupun penanda clientProperty "chipKep".
        // Dipasang SESUDAH UI terpasang supaya LookAndFeel.installProperty
        // menganggapnya nilai dari aplikasi dan tidak menimpanya lagi.
        if (t instanceof javax.swing.JRadioButton && !Boolean.TRUE.equals(t.getClientProperty("pertahankanRadioIcon"))) {
            t.setIcon(null);
            t.setSelectedIcon(null);
        }
        t.setBorderPainted(true);
        t.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        final BorderChipKep bd = new BorderChipKep();
        t.setBorder(bd);
        warnaiChipKep(t, bd, false);
        // itemStateChanged, bukan actionPerformed: pilihan di ButtonGroup yg
        // TER-DESELEKSI tidak pernah memancarkan action, jadi chip lamanya
        // akan tertinggal menyala kalau dipantau lewat action.
        t.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent e) {
                warnaiChipKep(t, bd, false);
            }
        });
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                warnaiChipKep(t, bd, true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                warnaiChipKep(t, bd, false);
            }
        });
    }

    /**
     * Tiga status chip, semuanya pakai token palet yg sudah ada (tidak ada
     * warna baru - lihat larangan "JANGAN bikin palet warna kedua").
     *
     * Garis diam memakai TEXT_FAINT (#9AA19C) = 2,55:1 di atas latar chip
     * SURFACE_ALT (#F7F8F5).
     *
     * Sejarahnya perlu ditulis karena pernah menyesatkan: Sesi 3b mengganti
     * BORDER_SOFT (#E4E6E2, 1,18:1) jadi BORDER (#D9E1E7, 1,24:1) dgn alasan
     * "pil-nya tak kelihatan". Alasan itu SALAH sasaran - pil-nya tak
     * kelihatan bukan karena warnanya, tapi karena borderPainted=false
     * (lihat pasangChipKep) membuatnya tidak pernah dilukis sama sekali.
     * Angka warna apa pun akan tetap tak terlihat. Sesudah flag itu
     * diperbaiki, ketiga nilai diukur ulang lewat CekChip: #D9E1E7 dan usul
     * prompt #D8DBD6 (1,31:1) sama2 cuma bisikan, sedangkan TEXT_FAINT
     * terbaca sebagai pil lembut di render.
     *
     * 2,55:1 MASIH di bawah 3:1 yg diminta WCAG 1.4.11 utk batas komponen,
     * dan itu disengaja: satu-satunya token palet yg lolos 3:1 adalah
     * TEXT_MUTED (~3,9:1), tapi garis sekelam itu di 146 chip mengembalikan
     * kesan "form penuh kotak" yg justru sedang dibongkar. Yg dibeli dgn
     * mengunci ke TEXT_FAINT: keputusan kontras yg masih menggantung di
     * PROGRESS.md (usul TEXT_FAINT -> #737875) otomatis menaikkan pil ini ke
     * ~3,3:1 tanpa perlu menyentuh berkas ini lagi.
     *
     * Terpilih ditandai TIGA hal sekaligus supaya tidak bergantung pada satu
     * kanal saja: latar bernada PRIMARY, garis PRIMARY, dan teks PRIMARY.
     * Font DIBIARKAN PLAIN, tidak di-BOLD-kan: chip di sini tidak dipaksa
     * selebar sel (lihat pelukChipGridKep), jadi mem-BOLD-kan akan mengubah
     * lebarnya dan seluruh baris bergeser tiap kali diklik.
     *
     * Teks yg TIDAK terpilih tetap TEXT_NORMAL (11,99:1), tidak diredupkan ke
     * TEXT_MUTED - ini nama pilihan klinis yg harus terbaca, bukan hiasan,
     * dan TEXT_MUTED cuma 4,02:1 di atas putih.
     */
    private void warnaiChipKep(javax.swing.JToggleButton t, BorderChipKep bd, boolean hover) {
        if (t.isSelected()) {
            t.setForeground(widget.ThemeConfig.PRIMARY);
            t.setBackground(CHIP_PILIH_BG_KEP);
            bd.setGaris(widget.ThemeConfig.PRIMARY);
        } else if (hover) {
            t.setForeground(widget.ThemeConfig.TEXT_NORMAL);
            t.setBackground(CHIP_HOVER_BG_KEP);
            bd.setGaris(widget.ThemeConfig.PRIMARY);
        } else {
            t.setForeground(widget.ThemeConfig.TEXT_NORMAL);
            t.setBackground(java.awt.Color.WHITE);
            bd.setGaris(WARNA_BORDER_KOTAK_KEP);
        }
        t.repaint();
    }

    /** Baris "label di atas + area" (dipakai utk Keluhan Utama/scrollPane1). */
    private widget.PanelBiasa barisAreaKep(String label, java.awt.Component isi) {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(false);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 10, 2, 10));
        p.setLayout(new java.awt.BorderLayout(0, 3));
        widget.Label l = new widget.Label();
        l.setText(label);
        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        l.setForeground(widget.ThemeConfig.TEXT_MUTED);
        p.add(l, java.awt.BorderLayout.NORTH);
        p.add(isi, java.awt.BorderLayout.CENTER);
        return p;
    }

    /** Satu sel banner: label kecil di atas field (field dipaksa tinggi 26). */
    private widget.PanelBiasa hdrCellKep(String label, java.awt.Component field, boolean wajib) {
        widget.PanelBiasa sel = new widget.PanelBiasa();
        sel.setOpaque(false);
        sel.setBorder(null);
        sel.setLayout(new java.awt.BorderLayout(0, 3));
        widget.Label l = new widget.Label();
        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        l.setText(wajib ? "<html>" + label + " <font color='#B3261E'>*</font></html>" : label);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        l.setForeground(widget.ThemeConfig.TEXT_MUTED);
        sel.add(l, java.awt.BorderLayout.NORTH);
        int lebarField = (field.getPreferredSize() != null && field.getPreferredSize().width > 0)
                ? field.getPreferredSize().width
                : 0;
        field.setPreferredSize(new java.awt.Dimension(lebarField, TINGGI_FIELD_KEP));
        sel.add(field, java.awt.BorderLayout.CENTER);
        return sel;
    }

    /** Tinggi seragam field & tombol di bar pasien (Bagian B). */
    private static final int TINGGI_FIELD_KEP = 26;

    /**
     * Tandai field yg memang read-only supaya tidak terlihat seperti isian yg
     * lupa diisi.
     */
    private void tandaiReadOnlyKep(javax.swing.JComponent c) {
        if (c == null) {
            return;
        }
        c.setOpaque(true);
        c.setBackground(java.awt.Color.WHITE);
    }

    /** Pasangan [field 🔍] utk lookup Petugas/Dokter bersudut siku-siku (kotak). */
    private widget.PanelBiasa hdrLookupKep(widget.TextBox tf, widget.Button btn) {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(true);
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
        p.setLayout(new java.awt.BorderLayout(0, 0));
        p.setPreferredSize(new java.awt.Dimension(220, TINGGI_FIELD_KEP));

        tf.setPreferredSize(new java.awt.Dimension(180, TINGGI_FIELD_KEP));
        tf.setBackground(java.awt.Color.WHITE);
        tf.setOpaque(true);
        tf.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 4));
        tf.putClientProperty("lookupField", Boolean.TRUE);
        tf.putClientProperty("Component.arc", 0);
        tf.putClientProperty("TextComponent.arc", 0);
        tf.putClientProperty("JTextField.arc", 0);
        tf.putClientProperty("JComponent.roundRect", false);

        btn.setPreferredSize(new java.awt.Dimension(TINGGI_FIELD_KEP + 2, TINGGI_FIELD_KEP));
        btn.setBackground(java.awt.Color.WHITE);
        btn.setGlassColor(java.awt.Color.WHITE);
        btn.setRoundRect(false);
        btn.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 0, new java.awt.Color(0xD8, 0xDB, 0xD6)));
        btn.putClientProperty("Component.arc", 0);
        btn.putClientProperty("Button.arc", 0);
        btn.putClientProperty("JButton.buttonType", "square");
        btn.putClientProperty("JComponent.roundRect", false);

        p.add(tf, java.awt.BorderLayout.CENTER);
        p.add(btn, java.awt.BorderLayout.EAST);
        return p;
    }

    /** Ambil inisial (maks 2 huruf) dari nama pasien utk avatar kartu. */
    private static String inisialDariNama(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            return "?";
        }
        String bersih = nama.replaceAll("\\(.*?\\)", " ").replaceAll("[^a-zA-Z\\s]", " ").trim();
        String[] kata = bersih.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kata.length && sb.length() < 2; i++) {
            if (!kata[i].isEmpty()) {
                char c = kata[i].charAt(0);
                if (Character.isLetter(c)) {
                    sb.append(Character.toUpperCase(c));
                }
            }
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    /** Komponen bulat yang menggambar inisial nama pasien pada kartu identitas. */
    private static final class AvatarBulatKep extends javax.swing.JComponent {
        private String inisial = "?";

        void setInisial(String s) {
            this.inisial = s;
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            int diameter = Math.max(0, Math.min(getWidth(), getHeight()) - 1);
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;
            g2.setColor(widget.ThemeConfig.ACCENT);
            g2.fillOval(x, y, diameter, diameter);
            g2.setColor(java.awt.Color.WHITE);
            g2.setFont(getFont());
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(inisial);
            int tx = x + (diameter - tw) / 2;
            int ty = y + (diameter - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(inisial, tx, ty);
            g2.dispose();
        }
    }

    /** Isi label kartu identitas dari TextBox tersembunyi (display-only). */
    private void sinkronKartuKep() {
        if (lblKartuNama == null) {
            return;
        }
        String nama = TPasien.getText() == null ? "" : TPasien.getText().trim();
        lblKartuNama.setText(nama.isEmpty() ? "—" : nama);
        if (avatarKartuKep != null) {
            avatarKartuKep.setInisial(inisialDariNama(nama));
        }
        lblKartuRM.setText(barisNomorKep("No. RM", TNoRM.getText()));
        lblKartuRawat.setText(barisNomorKep("No. Rawat", TNoRw.getText()));
        if (lblKartuLokasi != null) {
            lblKartuLokasi.setText(barisNomorKep("Lokasi", "IGD"));
        }
        if (lblKartuInfo != null) {
            String jk = jkPanjangKep(Jk.getText());
            String umur = umurDariTglLahirKep(TglLahir.getText());
            String isi = jk;
            if (!umur.isEmpty()) {
                isi = isi.isEmpty() ? umur : isi + " · " + umur;
            }
            lblKartuInfo.setText(isi.isEmpty() ? " " : isi);
        }
    }

    /**
     * Satu baris "keterangan + nomor" di kartu identitas.
     */
    private String barisNomorKep(String keterangan, String nomor) {
        String n = nomor == null ? "" : nomor.trim();
        return "<html><font color='" + hexKep(widget.ThemeConfig.TEXT_MUTED) + "'>" + escHtmlKep(keterangan)
                + "</font>&nbsp;&nbsp;<font face='Consolas' color='" + hexKep(widget.ThemeConfig.TEXT) + "'>"
                + (n.isEmpty() ? "&mdash;" : escHtmlKep(n)) + "</font></html>";
    }

    /** Color -> "#RRGGBB" utk dipakai di dalam teks HTML label. */
    private String hexKep(java.awt.Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    /** String -> "Laki-laki" / "Perempuan". String kosong kalau tak dikenal. */
    private String jkPanjangKep(String jk) {
        String s = jk == null ? "" : jk.trim();
        if (s.isEmpty()) {
            return "";
        }
        char awal = Character.toUpperCase(s.charAt(0));
        if (awal == 'L') {
            return "Laki-laki";
        }
        if (awal == 'P') {
            return "Perempuan";
        }
        return s;
    }

    /** Umur dari "yyyy-MM-dd" (isi TglLahir). String kosong kalau tak terbaca. */
    private String umurDariTglLahirKep(String tgl) {
        String s = tgl == null ? "" : tgl.trim();
        if (s.length() < 10 || s.startsWith("0000")) {
            return "";
        }
        try {
            java.util.Calendar lahir = java.util.Calendar.getInstance();
            lahir.setTime(new java.text.SimpleDateFormat("yyyy-MM-dd").parse(s.substring(0, 10)));
            java.util.Calendar kini = java.util.Calendar.getInstance();
            int th = kini.get(java.util.Calendar.YEAR) - lahir.get(java.util.Calendar.YEAR);
            if (kini.get(java.util.Calendar.DAY_OF_YEAR) < lahir.get(java.util.Calendar.DAY_OF_YEAR)) {
                th--;
            }
            return th < 0 ? "" : th + " Th";
        } catch (java.text.ParseException e) {
            return "";
        }
    }

    /**
     * Komponen pemilih Tanggal & Jam terpisah pola Registrasi SIMRS Khanza:
     * - Tanggal: widget.Tanggal (kalender "dd-MM-yyyy")
     * - Jam: ComboBox Jam ("00".."23"), Menit ("00".."59"), Detik ("00".."59")
     * - Checkbox timer jalan otomatis real-time (opsional)
     */
    public static class PanelTanggalDanJam extends widget.PanelBiasa {
        public final widget.Tanggal tanggal;
        public final widget.ComboBox cmbJam;
        public final widget.ComboBox cmbMenit;
        public final widget.ComboBox cmbDetik;
        public final widget.CekBox chkJln;
        private javax.swing.Timer timer;

        public PanelTanggalDanJam(boolean adaTimer, int tinggi) {
            setOpaque(false);
            setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));
            setBorder(null);

            tanggal = new widget.Tanggal();
            tanggal.setDisplayFormat("dd-MM-yyyy");
            tanggal.setPreferredSize(new java.awt.Dimension(102, tinggi));
            tanggal.setBackground(java.awt.Color.WHITE);
            tanggal.setOpaque(true);
            tanggal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
            tanggal.putClientProperty("Component.arc", 0);
            tanggal.putClientProperty("TextComponent.arc", 0);
            tanggal.putClientProperty("JComponent.roundRect", false);

            String[] arrJam = new String[24];
            for (int i = 0; i < 24; i++) {
                arrJam[i] = (i < 10 ? "0" : "") + i;
            }
            cmbJam = new widget.ComboBox();
            cmbJam.setModel(new javax.swing.DefaultComboBoxModel(arrJam));
            // 46px membuat FlatLaf memotong isi combo jadi "..." walau nilainya
            // cuma 2 digit (dibuktikan lewat Robot.createScreenCapture nyata,
            // bukan cuma printAll offscreen) - 62px terbukti cukup.
            cmbJam.setPreferredSize(new java.awt.Dimension(62, tinggi));
            cmbJam.setBackground(java.awt.Color.WHITE);
            cmbJam.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
            cmbJam.putClientProperty("Component.arc", 0);

            String[] arrMnt = new String[60];
            for (int i = 0; i < 60; i++) {
                arrMnt[i] = (i < 10 ? "0" : "") + i;
            }
            cmbMenit = new widget.ComboBox();
            cmbMenit.setModel(new javax.swing.DefaultComboBoxModel(arrMnt));
            cmbMenit.setPreferredSize(new java.awt.Dimension(62, tinggi));
            cmbMenit.setBackground(java.awt.Color.WHITE);
            cmbMenit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
            cmbMenit.putClientProperty("Component.arc", 0);

            cmbDetik = new widget.ComboBox();
            cmbDetik.setModel(new javax.swing.DefaultComboBoxModel(arrMnt));
            cmbDetik.setPreferredSize(new java.awt.Dimension(62, tinggi));
            cmbDetik.setBackground(java.awt.Color.WHITE);
            cmbDetik.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
            cmbDetik.putClientProperty("Component.arc", 0);

            add(tanggal);
            add(cmbJam);
            widget.Label sep1 = new widget.Label();
            sep1.setText(":");
            sep1.setPreferredSize(new java.awt.Dimension(4, tinggi));
            sep1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            add(sep1);
            add(cmbMenit);
            widget.Label sep2 = new widget.Label();
            sep2.setText(":");
            sep2.setPreferredSize(new java.awt.Dimension(4, tinggi));
            sep2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            add(sep2);
            add(cmbDetik);

            if (adaTimer) {
                chkJln = new widget.CekBox();
                chkJln.setSelected(true);
                chkJln.setToolTipText("Centang agar tanggal & jam berjalan otomatis. Hapus centang untuk ubah tanggal/jam mundur atau maju");
                chkJln.setPreferredSize(new java.awt.Dimension(28, tinggi));
                add(chkJln);

                timer = new javax.swing.Timer(1000, e -> {
                    if (chkJln.isSelected()) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        tanggal.setDate(cal.getTime());
                        cmbJam.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY)));
                        cmbMenit.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.MINUTE)));
                        cmbDetik.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.SECOND)));
                    }
                });
                timer.start();

                chkJln.addItemListener(e -> {
                    if (chkJln.isSelected()) {
                        setSekarang();
                    }
                });
            } else {
                chkJln = null;
            }

            setSekarang();
            // [BUGFIX RMTriaseIGD] FlowLayout.minimumLayoutSize() manjumlahkan
            // getMinimumSize() tiap anak, bukan getPreferredSize() yg sudah
            // dipatok eksplisit di atas - widget.ComboBox/Tanggal diam-diam
            // punya minimumSize UI (FlatLaf) lebih LEBAR drpd preferredSize
            // kecil yg sengaja dipasang di sini, sehingga getMinimumSize()
            // panel INI (433) bisa lebih besar drpd getPreferredSize()-nya
            // sendiri (310) - kebalikan dari asumsi normal min<=preferred.
            // Efeknya kolom GridBagLayout pemanggil yg pas-pasan (mis. kolom
            // Anamnesa tab PACS RMTriaseIGD, lebar tetap 398px) memepetkan
            // kolom label di sebelahnya sampai terpotong "...", krn algoritma
            // interpolasi min/preferred GridBagLayout kolomnya condong ke
            // minimum yg keliru besar ini. Dipatok = preferred spy panel ini
            // tidak pernah dianggap butuh lebih lebar dari yg sudah didesain.
            setMinimumSize(getPreferredSize());
        }

        public void setSekarang() {
            tanggal.setDate(new Date());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cmbJam.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY)));
            cmbMenit.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.MINUTE)));
            cmbDetik.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.SECOND)));
            if (chkJln != null) {
                chkJln.setSelected(true);
            }
        }

        public void setNilaiSql(String tglJamSql) {
            if (tglJamSql == null || tglJamSql.trim().isEmpty()) {
                setSekarang();
                return;
            }
            try {
                String bersih = tglJamSql.replaceAll("'", "").trim();
                if (bersih.length() >= 10) {
                    java.text.SimpleDateFormat sdfTgl = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    tanggal.setDate(sdfTgl.parse(bersih.substring(0, 10)));
                }
                if (bersih.length() >= 19) {
                    cmbJam.setSelectedItem(bersih.substring(11, 13));
                    cmbMenit.setSelectedItem(bersih.substring(14, 16));
                    cmbDetik.setSelectedItem(bersih.substring(17, 19));
                } else if (bersih.length() >= 16) {
                    cmbJam.setSelectedItem(bersih.substring(11, 13));
                    cmbMenit.setSelectedItem(bersih.substring(14, 16));
                    cmbDetik.setSelectedItem("00");
                }
                if (chkJln != null) {
                    chkJln.setSelected(false);
                }
            } catch (Exception ex) {
                setSekarang();
            }
        }

        public String getTanggalSql(fungsi.validasi valid) {
            return valid.SetTgl(tanggal.getSelectedItem() + "");
        }

        public String getJamSql() {
            return cmbJam.getSelectedItem() + ":" + cmbMenit.getSelectedItem() + ":" + cmbDetik.getSelectedItem();
        }

        public String getTanggalDanJamSql(fungsi.validasi valid) {
            return getTanggalSql(valid) + " " + getJamSql();
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (tanggal != null) tanggal.setEnabled(enabled);
            if (cmbJam != null) cmbJam.setEnabled(enabled);
            if (cmbMenit != null) cmbMenit.setEnabled(enabled);
            if (cmbDetik != null) cmbDetik.setEnabled(enabled);
            if (chkJln != null) chkJln.setEnabled(enabled);
        }

        public void tampilkanTanggal() {
            // Komponen sudah menampilkan tanggal dan jam
        }

        public void setJam(Date d) {
            setSekarang();
        }

        public String getTeksSql() {
            return getTanggalDanJamSql(new fungsi.validasi());
        }

        public void stopTimer() {
            if (timer != null) {
                timer.stop();
            }
        }
    }

    /**
     * Komponen pemilih Jam (HH:mm:ss) terpisah dengan ComboBox manual (pola Registrasi).
     */
    public static class PanelJamSaja extends widget.PanelBiasa {
        public final widget.ComboBox cmbJam;
        public final widget.ComboBox cmbMenit;
        public final widget.ComboBox cmbDetik;
        public final widget.CekBox chkJln;
        private javax.swing.Timer timer;

        public PanelJamSaja(boolean adaTimer, int tinggi) {
            setOpaque(false);
            setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));
            setBorder(null);
            // 190 pas utk combo 46px lama; combo sekarang 62px (fix elipsis
            // "...") + checkbox timer 28px -> lebar total naik jadi ~240.
            setPreferredSize(new java.awt.Dimension(240, tinggi));
            setMinimumSize(new java.awt.Dimension(240, tinggi));

            String[] arrJam = new String[24];
            for (int i = 0; i < 24; i++) {
                arrJam[i] = (i < 10 ? "0" : "") + i;
            }
            cmbJam = new widget.ComboBox();
            cmbJam.setModel(new javax.swing.DefaultComboBoxModel(arrJam));
            // 46px membuat FlatLaf memotong isi combo jadi "..." walau nilainya
            // cuma 2 digit (dibuktikan lewat Robot.createScreenCapture nyata,
            // bukan cuma printAll offscreen) - 62px terbukti cukup.
            cmbJam.setPreferredSize(new java.awt.Dimension(62, tinggi));
            cmbJam.setBackground(java.awt.Color.WHITE);
            cmbJam.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
            cmbJam.putClientProperty("Component.arc", 0);

            String[] arrMnt = new String[60];
            for (int i = 0; i < 60; i++) {
                arrMnt[i] = (i < 10 ? "0" : "") + i;
            }
            cmbMenit = new widget.ComboBox();
            cmbMenit.setModel(new javax.swing.DefaultComboBoxModel(arrMnt));
            cmbMenit.setPreferredSize(new java.awt.Dimension(62, tinggi));
            cmbMenit.setBackground(java.awt.Color.WHITE);
            cmbMenit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
            cmbMenit.putClientProperty("Component.arc", 0);

            cmbDetik = new widget.ComboBox();
            cmbDetik.setModel(new javax.swing.DefaultComboBoxModel(arrMnt));
            cmbDetik.setPreferredSize(new java.awt.Dimension(62, tinggi));
            cmbDetik.setBackground(java.awt.Color.WHITE);
            cmbDetik.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
            cmbDetik.putClientProperty("Component.arc", 0);

            add(cmbJam);
            widget.Label sep1 = new widget.Label();
            sep1.setText(":");
            sep1.setPreferredSize(new java.awt.Dimension(4, tinggi));
            sep1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            add(sep1);
            add(cmbMenit);
            widget.Label sep2 = new widget.Label();
            sep2.setText(":");
            sep2.setPreferredSize(new java.awt.Dimension(4, tinggi));
            sep2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            add(sep2);
            add(cmbDetik);

            if (adaTimer) {
                chkJln = new widget.CekBox();
                chkJln.setSelected(true);
                chkJln.setToolTipText("Centang agar jam berjalan otomatis sesuai waktu sistem. Hapus centang untuk ubah jam mundur atau maju");
                chkJln.setPreferredSize(new java.awt.Dimension(28, tinggi));
                add(chkJln);

                timer = new javax.swing.Timer(1000, e -> {
                    if (chkJln.isSelected()) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cmbJam.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY)));
                        cmbMenit.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.MINUTE)));
                        cmbDetik.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.SECOND)));
                    }
                });
                timer.start();

                chkJln.addItemListener(e -> {
                    if (chkJln.isSelected()) {
                        setSekarang();
                    }
                });
            } else {
                chkJln = null;
            }

            setSekarang();
        }

        public void setSekarang() {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cmbJam.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY)));
            cmbMenit.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.MINUTE)));
            cmbDetik.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.SECOND)));
            if (chkJln != null) {
                chkJln.setSelected(true);
            }
        }

        public void setJamSql(String hhmmss) {
            if (hhmmss == null || hhmmss.trim().isEmpty()) {
                setSekarang();
                return;
            }
            try {
                String bersih = hhmmss.replaceAll("'", "").trim();
                if (bersih.length() >= 8) {
                    cmbJam.setSelectedItem(bersih.substring(0, 2));
                    cmbMenit.setSelectedItem(bersih.substring(3, 5));
                    cmbDetik.setSelectedItem(bersih.substring(6, 8));
                } else if (bersih.length() >= 5) {
                    cmbJam.setSelectedItem(bersih.substring(0, 2));
                    cmbMenit.setSelectedItem(bersih.substring(3, 5));
                    cmbDetik.setSelectedItem("00");
                }
                if (chkJln != null) {
                    chkJln.setSelected(false);
                }
            } catch (Exception ex) {
                setSekarang();
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (cmbJam != null) cmbJam.setEnabled(enabled);
            if (cmbMenit != null) cmbMenit.setEnabled(enabled);
            if (cmbDetik != null) cmbDetik.setEnabled(enabled);
            if (chkJln != null) chkJln.setEnabled(enabled);
        }

        public void setJam(Date d) {
            setSekarang();
        }

        public String getJamSql() {
            return getTeksJam();
        }

        public String getTeksJam() {
            return cmbJam.getSelectedItem() + ":" + cmbMenit.getSelectedItem() + ":" + cmbDetik.getSelectedItem();
        }

        public void stopTimer() {
            if (timer != null) {
                timer.stop();
            }
        }
    }

    /**
     * Inisialisasi pemilih jam, menit, detik dan checkbox timer berjalan (pola
     * Registrasi IGD).
     */
    private void initJamAsuhan() {
        if (CmbJam != null) {
            return;
        }
        CmbJam = new widget.ComboBox();
        CmbJam.setName("CmbJam");
        CmbJam.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23"
        }));
        CmbJam.setPreferredSize(new java.awt.Dimension(48, 24));

        String[] mntDtk = new String[60];
        for (int i = 0; i < 60; i++) {
            mntDtk[i] = (i < 10 ? "0" : "") + i;
        }
        CmbMenit = new widget.ComboBox();
        CmbMenit.setName("CmbMenit");
        CmbMenit.setModel(new javax.swing.DefaultComboBoxModel(mntDtk));
        CmbMenit.setPreferredSize(new java.awt.Dimension(48, 24));

        CmbDetik = new widget.ComboBox();
        CmbDetik.setName("CmbDetik");
        CmbDetik.setModel(new javax.swing.DefaultComboBoxModel(mntDtk));
        CmbDetik.setPreferredSize(new java.awt.Dimension(48, 24));

        ChkJln = new widget.CekBox();
        ChkJln.setName("ChkJln");
        ChkJln.setSelected(true);
        ChkJln.setToolTipText("Centang agar tanggal & jam berjalan otomatis sesuai waktu sistem. Hapus centang untuk ubah tanggal/jam mundur atau maju");
        ChkJln.setPreferredSize(new java.awt.Dimension(28, 24));

        java.awt.event.ActionListener taskPerDetik = (java.awt.event.ActionEvent evt) -> {
            if (ChkJln != null && ChkJln.isSelected()) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                String jam = String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY));
                String menit = String.format("%02d", cal.get(java.util.Calendar.MINUTE));
                String detik = String.format("%02d", cal.get(java.util.Calendar.SECOND));
                CmbJam.setSelectedItem(jam);
                CmbMenit.setSelectedItem(menit);
                CmbDetik.setSelectedItem(detik);
                if (TglAsuhan != null) {
                    TglAsuhan.setDate(cal.getTime());
                }
            }
        };
        timerJam = new javax.swing.Timer(1000, taskPerDetik);
        timerJam.start();

        ChkJln.addItemListener(e -> {
            if (ChkJln.isSelected()) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                CmbJam.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY)));
                CmbMenit.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.MINUTE)));
                CmbDetik.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.SECOND)));
                if (TglAsuhan != null) {
                    TglAsuhan.setDate(cal.getTime());
                }
            }
        });

        java.util.Calendar calAwal = java.util.Calendar.getInstance();
        CmbJam.setSelectedItem(String.format("%02d", calAwal.get(java.util.Calendar.HOUR_OF_DAY)));
        CmbMenit.setSelectedItem(String.format("%02d", calAwal.get(java.util.Calendar.MINUTE)));
        CmbDetik.setSelectedItem(String.format("%02d", calAwal.get(java.util.Calendar.SECOND)));
    }

    private String getJamAsuhan() {
        if (CmbJam != null && CmbMenit != null && CmbDetik != null) {
            return CmbJam.getSelectedItem() + ":" + CmbMenit.getSelectedItem() + ":" + CmbDetik.getSelectedItem();
        }
        return "00:00:00";
    }

    private String getTanggalDanJamAsuhan() {
        return Valid.SetTgl(TglAsuhan.getSelectedItem() + "") + " " + getJamAsuhan();
    }

    /**
     * Membungkus widget.TextBox agar melukis kotak siku-siku murni khusus di form
     * ini,
     * tanpa mengubah kelas global widget.TextBox sehingga form-form lain di SIMRS
     * tetap aman.
     */
    private widget.TextBox buatTextBoxKotakLokal(final widget.TextBox asli) {
        widget.TextBox kotak = new widget.TextBox() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                g.setColor(java.awt.Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                if (ui != null) {
                    java.awt.Graphics g2 = g.create();
                    try {
                        ui.paint(g2, this);
                    } finally {
                        g2.dispose();
                    }
                }

            }
        };
        if (asli != null) {
            kotak.setDocument(asli.getDocument());
            kotak.setEditable(asli.isEditable());
            kotak.setEnabled(asli.isEnabled());
            kotak.setFont(asli.getFont());
            kotak.setName(asli.getName());
            kotak.setText(asli.getText());
        }
        kotak.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)),
                javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        kotak.setBackground(java.awt.Color.WHITE);
        kotak.setOpaque(true);
        kotak.putClientProperty("FlatLaf.style",
                "background: #FFFFFF; inactiveBackground: #FFFFFF; disabledBackground: #FFFFFF;");
        kotak.putClientProperty("Component.arc", 0);
        kotak.putClientProperty("TextComponent.arc", 0);
        kotak.putClientProperty("JTextField.arc", 0);
        kotak.putClientProperty("JComponent.roundRect", false);
        return kotak;
    }

    /**
     * Membuat satu sel input (label atas + input box dan satuan trailing di kanan).
     * Dipakai di grid 4 kolom Bagian 2 (Tanda Vital & Obstetri).
     */
    private widget.PanelBiasa buatSelVital(String label, java.awt.Component field, String unit) {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(false);
        p.setLayout(new java.awt.GridBagLayout());

        widget.Label lbl = new widget.Label();
        lbl.setText(label);
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        lbl.setForeground(new java.awt.Color(0x64, 0x74, 0x8B));

        widget.PanelBiasa boxUnit = new widget.PanelBiasa();
        boxUnit.setOpaque(false);
        boxUnit.setLayout(new java.awt.BorderLayout(6, 0));
        boxUnit.add(field, java.awt.BorderLayout.CENTER);

        if (unit != null && !unit.isEmpty()) {
            widget.Label lblUnit = new widget.Label();
            lblUnit.setText(unit);
            lblUnit.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            lblUnit.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            lblUnit.setForeground(new java.awt.Color(0x64, 0x74, 0x8B));
            lblUnit.setPreferredSize(new java.awt.Dimension(36, 26));
            boxUnit.add(lblUnit, java.awt.BorderLayout.EAST);
        }

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new java.awt.Insets(0, 0, 4, 0);
        p.add(lbl, c);

        c.gridy = 1;
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        p.add(boxUnit, c);

        return p;
    }

    /**
     * Mengatur tanggal HPHT pada dropdown kalender (TglHPHT) dan TextBox backing
     * store (HPHT).
     * Mendukung format yyyy-MM-dd dan dd-MM-yyyy.
     */
    private void setTanggalHpht(String tglStr) {
        if (tglStr == null || tglStr.trim().isEmpty() || "-".equals(tglStr.trim())) {
            if (TglHPHT != null) {
                TglHPHT.setDate(new java.util.Date());
            }
            if (HPHT != null) {
                HPHT.setText("");
            }
            return;
        }
        tglStr = tglStr.trim();
        try {
            if (tglStr.length() == 10 && tglStr.charAt(4) == '-') {
                Valid.SetTgl(TglHPHT, tglStr);
            } else if (tglStr.length() == 10 && tglStr.charAt(2) == '-') {
                java.util.Date d = new java.text.SimpleDateFormat("dd-MM-yyyy").parse(tglStr);
                TglHPHT.setDate(d);
            } else {
                Valid.SetTgl(TglHPHT, tglStr);
            }
        } catch (Exception e) {
            if (TglHPHT != null) {
                TglHPHT.setDate(new java.util.Date());
            }
        }
        if (HPHT != null && TglHPHT != null && TglHPHT.getSelectedItem() != null) {
            HPHT.setText(Valid.SetTgl(TglHPHT.getSelectedItem() + ""));
        }
    }

    /** Banner pasien: kartu identitas (kiri) + grid field header (kanan). */
    private widget.PanelBiasa buatBannerKep() {
        // TextBox identitas jadi sumber data tersembunyi (spt banner Triase).
        TNoRw.setVisible(false);
        TNoRM.setVisible(false);
        TPasien.setVisible(false);

        widget.PanelBiasa banner = new widget.PanelBiasa();
        banner.setOpaque(false);
        banner.setLayout(new java.awt.GridBagLayout());

        // ---- kartu identitas (kiri) ----
        widget.PanelBiasa kartu = new widget.PanelBiasa();
        latarKep(kartu, widget.ThemeConfig.SURFACE_ALT);
        kartu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
                javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        kartu.setLayout(new java.awt.GridBagLayout());
        widget.Label judulKartu = new widget.Label();
        judulKartu.setText("IDENTITAS PASIEN");
        judulKartu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        judulKartu.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        judulKartu.setForeground(widget.ThemeConfig.TEXT_MUTED);

        widget.Label badgeKunci = new widget.Label();
        badgeKunci.setText("  TERKUNCI  ");
        badgeKunci.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        badgeKunci.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 9));
        badgeKunci.setOpaque(true);
        badgeKunci.setBackground(widget.ThemeConfig.BORDER_SOFT);
        badgeKunci.setForeground(widget.ThemeConfig.TEXT_MUTED);

        avatarKartuKep = new AvatarBulatKep();
        java.awt.Dimension davatar = new java.awt.Dimension(40, 40);
        avatarKartuKep.setPreferredSize(davatar);
        avatarKartuKep.setMinimumSize(davatar);
        avatarKartuKep.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));

        lblKartuNama = new widget.Label();
        lblKartuNama.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKartuNama.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        lblKartuNama.setForeground(widget.ThemeConfig.TEXT);
        lblKartuInfo = new widget.Label();
        lblKartuInfo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKartuInfo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        lblKartuInfo.setForeground(widget.ThemeConfig.TEXT_MUTED);
        lblKartuRM = new widget.Label();
        lblKartuRM.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKartuRM.setForeground(widget.ThemeConfig.TEXT_MUTED);
        lblKartuRawat = new widget.Label();
        lblKartuRawat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKartuRawat.setForeground(widget.ThemeConfig.TEXT_MUTED);
        lblKartuLokasi = new widget.Label();
        lblKartuLokasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKartuLokasi.setForeground(widget.ThemeConfig.TEXT_MUTED);

        java.awt.GridBagConstraints k = new java.awt.GridBagConstraints();
        k.gridx = 0;
        k.gridy = 0;
        k.gridheight = 6;
        k.anchor = java.awt.GridBagConstraints.NORTHWEST;
        k.fill = java.awt.GridBagConstraints.NONE;
        k.weightx = 0.0;
        k.insets = new java.awt.Insets(0, 0, 0, 10);
        kartu.add(avatarKartuKep, k);

        k.gridx = 1;
        k.gridy = 0;
        k.gridheight = 1;
        k.anchor = java.awt.GridBagConstraints.WEST;
        k.fill = java.awt.GridBagConstraints.HORIZONTAL;
        k.weightx = 1.0;
        k.insets = new java.awt.Insets(0, 0, 0, 0);
        kartu.add(judulKartu, k);

        k.gridx = 2;
        k.weightx = 0.0;
        k.fill = java.awt.GridBagConstraints.NONE;
        k.anchor = java.awt.GridBagConstraints.EAST;
        kartu.add(badgeKunci, k);

        k.gridx = 1;
        k.gridwidth = 2;
        k.weightx = 1.0;
        k.anchor = java.awt.GridBagConstraints.WEST;
        k.fill = java.awt.GridBagConstraints.HORIZONTAL;
        k.gridy = 1;
        k.insets = new java.awt.Insets(7, 0, 0, 0);
        kartu.add(lblKartuNama, k);
        k.gridy = 2;
        k.insets = new java.awt.Insets(2, 0, 0, 0);
        kartu.add(lblKartuInfo, k);
        k.gridy = 3;
        k.insets = new java.awt.Insets(7, 0, 0, 0);
        kartu.add(lblKartuRM, k);
        k.gridy = 4;
        k.insets = new java.awt.Insets(3, 0, 0, 0);
        kartu.add(lblKartuRawat, k);
        k.gridy = 5;
        k.insets = new java.awt.Insets(3, 0, 0, 0);
        kartu.add(lblKartuLokasi, k);
        k.gridx = 0;
        k.gridwidth = 3;
        k.gridy = 6;
        k.weighty = 1.0;
        k.fill = java.awt.GridBagConstraints.BOTH;
        k.insets = new java.awt.Insets(0, 0, 0, 0);
        kartu.add(javax.swing.Box.createGlue(), k);

        Jk = buatTextBoxKotakLokal(Jk);
        NmPetugas = buatTextBoxKotakLokal(NmPetugas);
        NmDokterKp = buatTextBoxKotakLokal(NmDokterKp);

        javax.swing.event.DocumentListener sinkron = new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                sinkronKartuKep();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                sinkronKartuKep();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                sinkronKartuKep();
            }
        };
        TNoRw.getDocument().addDocumentListener(sinkron);
        TNoRM.getDocument().addDocumentListener(sinkron);
        TPasien.getDocument().addDocumentListener(sinkron);
        Jk.getDocument().addDocumentListener(sinkron);
        TglLahir.getDocument().addDocumentListener(sinkron);
        sinkronKartuKep();

        // ---- grid field header (kanan) ----
        widget.PanelBiasa grid = new widget.PanelBiasa();
        grid.setOpaque(false);
        grid.setLayout(new java.awt.GridBagLayout());

        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icCari = new com.formdev.flatlaf.extras.FlatSVGIcon(
                    "icon/search.svg", 13, 13);
            icCari.setColorFilter(
                    new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> new java.awt.Color(0x64, 0x74, 0x8B)));
            BtnPetugas.setIcon(icCari);
            BtnDokterKp.setIcon(icCari);
        } catch (Throwable ex) {
            BtnPetugas.setText("⌕");
            BtnDokterKp.setText("⌕");
        }
        BtnPetugas.setText("");
        BtnPetugas.setPreferredSize(new java.awt.Dimension(TINGGI_FIELD_KEP + 2, TINGGI_FIELD_KEP));
        BtnPetugas.setBackground(java.awt.Color.WHITE);
        BtnPetugas.setGlassColor(java.awt.Color.WHITE);
        BtnPetugas.setRoundRect(false);
        BtnPetugas.setBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 0, new java.awt.Color(0xD8, 0xDB, 0xD6)));
        BtnPetugas.putClientProperty("Component.arc", 0);
        BtnPetugas.putClientProperty("Button.arc", 0);
        BtnPetugas.putClientProperty("JButton.buttonType", "square");

        BtnDokterKp.setText("");
        BtnDokterKp.setPreferredSize(new java.awt.Dimension(TINGGI_FIELD_KEP + 2, TINGGI_FIELD_KEP));
        BtnDokterKp.setBackground(java.awt.Color.WHITE);
        BtnDokterKp.setGlassColor(java.awt.Color.WHITE);
        BtnDokterKp.setRoundRect(false);
        BtnDokterKp.setBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 0, new java.awt.Color(0xD8, 0xDB, 0xD6)));
        BtnDokterKp.putClientProperty("Component.arc", 0);
        BtnDokterKp.putClientProperty("Button.arc", 0);
        BtnDokterKp.putClientProperty("JButton.buttonType", "square");

        KdPetugas.setVisible(false);
        KdDokterKp.setVisible(false);

        NmPetugas.putClientProperty("JTextField.placeholderText", "Cari petugas...");
        NmDokterKp.putClientProperty("JTextField.placeholderText", "Cari dokter...");

        Jk.setVisible(false);

        Informasi.setBackground(java.awt.Color.WHITE);
        Informasi.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
        Informasi.putClientProperty("Component.arc", 0);
        Informasi.putClientProperty("ComboBox.arc", 0);
        Informasi.putClientProperty("JComponent.roundRect", false);

        tandaiReadOnlyKep(Jk);
        tandaiReadOnlyKep(NmPetugas);
        tandaiReadOnlyKep(NmDokterKp);

        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.gridy = 0;
        g.fill = java.awt.GridBagConstraints.HORIZONTAL;
        g.anchor = java.awt.GridBagConstraints.NORTH;
        g.weightx = 0.25;

        // Tanggal & Jam Asuhan terpisah pola Registrasi:
        // Tanggal menggunakan widget.Tanggal (kalender "dd-MM-yyyy")
        // Jam menggunakan ComboBox Jam, Menit, Detik manual + Checkbox timer real-time
        initJamAsuhan();
        TglAsuhan.setDisplayFormat("dd-MM-yyyy");
        TglAsuhan.setPreferredSize(new java.awt.Dimension(102, TINGGI_FIELD_KEP));
        TglAsuhan.setBackground(java.awt.Color.WHITE);
        TglAsuhan.setOpaque(true);
        TglAsuhan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
        TglAsuhan.putClientProperty("FlatLaf.style", "background: #FFFFFF; buttonBackground: #FFFFFF;");
        TglAsuhan.putClientProperty("Component.arc", 0);
        TglAsuhan.putClientProperty("TextComponent.arc", 0);
        TglAsuhan.putClientProperty("JComponent.roundRect", false);

        CmbJam.setPreferredSize(new java.awt.Dimension(46, TINGGI_FIELD_KEP));
        CmbJam.setBackground(java.awt.Color.WHITE);
        CmbJam.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
        CmbJam.putClientProperty("Component.arc", 0);

        CmbMenit.setPreferredSize(new java.awt.Dimension(46, TINGGI_FIELD_KEP));
        CmbMenit.setBackground(java.awt.Color.WHITE);
        CmbMenit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
        CmbMenit.putClientProperty("Component.arc", 0);

        CmbDetik.setPreferredSize(new java.awt.Dimension(46, TINGGI_FIELD_KEP));
        CmbDetik.setBackground(java.awt.Color.WHITE);
        CmbDetik.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
        CmbDetik.putClientProperty("Component.arc", 0);

        widget.PanelBiasa panelTglJamAsuhan = new widget.PanelBiasa();
        panelTglJamAsuhan.setOpaque(false);
        panelTglJamAsuhan.setPreferredSize(new java.awt.Dimension(292, TINGGI_FIELD_KEP));
        panelTglJamAsuhan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));
        panelTglJamAsuhan.add(TglAsuhan);
        panelTglJamAsuhan.add(CmbJam);
        widget.Label sep1 = new widget.Label();
        sep1.setText(":");
        sep1.setPreferredSize(new java.awt.Dimension(4, TINGGI_FIELD_KEP));
        sep1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panelTglJamAsuhan.add(sep1);
        panelTglJamAsuhan.add(CmbMenit);
        widget.Label sep2 = new widget.Label();
        sep2.setText(":");
        sep2.setPreferredSize(new java.awt.Dimension(4, TINGGI_FIELD_KEP));
        sep2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panelTglJamAsuhan.add(sep2);
        panelTglJamAsuhan.add(CmbDetik);
        panelTglJamAsuhan.add(ChkJln);

        g.gridx = 0;
        g.insets = new java.awt.Insets(0, 0, 0, 12);
        grid.add(hdrCellKep("TANGGAL & JAM", panelTglJamAsuhan, false), g);

        g.gridx = 1;
        g.insets = new java.awt.Insets(0, 0, 0, 12);
        grid.add(hdrCellKep("PETUGAS", hdrLookupKep(NmPetugas, BtnPetugas), false), g);

        g.gridx = 2;
        g.insets = new java.awt.Insets(0, 0, 0, 12);
        grid.add(hdrCellKep("DOKTER", hdrLookupKep(NmDokterKp, BtnDokterKp), false), g);

        g.gridx = 3;
        g.insets = new java.awt.Insets(0, 0, 0, 0);
        grid.add(hdrCellKep("INFORMASI DIDAPAT DARI", Informasi, false), g);

        // ---- rakit banner: kartu (kiri, lebar tetap) + grid (kanan, fleksibel) ----
        java.awt.Dimension dk = new java.awt.Dimension(310, 138);
        kartu.setMinimumSize(dk);
        kartu.setPreferredSize(dk);
        java.awt.GridBagConstraints f = new java.awt.GridBagConstraints();
        f.gridx = 0;
        f.gridy = 0;
        f.fill = java.awt.GridBagConstraints.VERTICAL;
        f.weighty = 1.0;
        f.insets = new java.awt.Insets(0, 0, 0, 16);
        banner.add(kartu, f);
        f.gridx = 1;
        f.weightx = 1.0;
        f.fill = java.awt.GridBagConstraints.HORIZONTAL;
        f.anchor = java.awt.GridBagConstraints.NORTH;
        f.insets = new java.awt.Insets(0, 0, 0, 0);
        banner.add(grid, f);
        return banner;
    }

    /**
     * Helper styling tombol footer agar berwujud kartu (card button):
     * Berlatar putih, border 1px kotak rapi dengan padding dalam, kursor tangan,
     * dan efek hover halus.
     */
    private void gayaTombolCardFooter(final widget.Button b, int lebar) {
        b.setContentAreaFilled(true);
        b.setBorderPainted(true);
        b.setFocusPainted(false);
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        b.setIconTextGap(6);
        b.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        b.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        b.putClientProperty("JButton.buttonType", null);
        b.putClientProperty("JButton.arc", 8);
        b.setMargin(new java.awt.Insets(2, 6, 2, 6));
        b.setPreferredSize(new java.awt.Dimension(lebar, 30));
    }

    /**
     * Restyle action bar bawah (panelGlass8) agar tombol berwujud card yang jelas.
     */
    private void initActionBarKep() {
        BtnSimpan.setText("Simpan");
        gayaTombolCardFooter(BtnSimpan, 96);
        gayaTombolCardFooter(BtnBatal, 90);
        gayaTombolCardFooter(BtnEdit, 90);
        gayaTombolCardFooter(BtnHapus, 92);
        gayaTombolCardFooter(BtnPrint, 90);
        gayaTombolCardFooter(BtnAll, 96);
        gayaTombolCardFooter(BtnKeluar, 96);

        BtnHapus.setForeground(new java.awt.Color(0xB9, 0x1C, 0x1C));
        BtnKeluar.setForeground(new java.awt.Color(0xB9, 0x1C, 0x1C));

        // Pasang icon SVG modern dari IconStyle jika belum terpasang
        widget.IconStyle.pasang(panelGlass8);

        lblProgresBawahKep = new widget.Label();
        lblProgresBawahKep.setText("Isian wajib 0/17 - 17 belum terisi");
        lblProgresBawahKep.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11).deriveFont(11.5f));
        lblProgresBawahKep.setForeground(widget.ThemeConfig.TEXT_MUTED);

        widget.PanelBiasa kiri = new widget.PanelBiasa();
        kiri.setOpaque(false);
        kiri.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 8));
        kiri.add(BtnSimpan);
        kiri.add(BtnBatal);
        kiri.add(BtnEdit);
        kiri.add(BtnHapus);
        kiri.add(BtnPrint);
        kiri.add(BtnAll);
        kiri.add(BtnKeluar);
        widget.PanelBiasa kanan = new widget.PanelBiasa();
        kanan.setOpaque(false);
        kanan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 8));
        kanan.add(lblProgresBawahKep);

        panelGlass8.removeAll();
        panelGlass8.setLayout(new java.awt.BorderLayout());
        panelGlass8.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, widget.ThemeConfig.BORDER_SOFT));
        panelGlass8.add(kiri, java.awt.BorderLayout.CENTER);
        panelGlass8.add(kanan, java.awt.BorderLayout.EAST);
        panelGlass8.revalidate();
        panelGlass8.repaint();
    }

    /**
     * [Bagian D] Pengelompokan 25 bagian nyata jadi tepat 11 HALAMAN sesuai
     * spesifikasi prompt dan mockup.
     */
    private static final String[][] GRUP_HALAMAN_KEP = {
            { "Riwayat Kesehatan & Alergi", "RIWAYAT KESEHATAN PASIEN", "RIWAYAT ALERGI" },
            { "Keadaan Umum, Antropometri & Kehamilan", "KEADAAN UMUM", "STATUS KEHAMILAN" },
            { "Asesmen Psikologis & Status Sosial-Ekonomi", "ASESMEN PSIKOLOGIS", "STATUS SOSIAL DAN EKONOMIS" },
            { "Skrining Geriatri & Risiko Jatuh", "SKRINING GERIATRI",
                    "SKRINING RESIKO PASIEN JATUH (GET UP AND GO TEST)" },
            { "Pengkajian Spiritual, Fungsional & Budaya", "PENGKAJIAN SPIRITUAL, KEPERCAYAAN, FUNGSIONAL DAN BUDAYA" },
            { "Skrining Gizi (MST / Anak)", "SKRINING GIZI DEWASA (MST)", "SKRINING GIZI ANAK (USIA 0-14 TAHUN)" },
            { "Asesmen Nyeri (PQRST / FLACC)", "ASESMEN NYERI" },
            { "Diagnosis, Intervensi & Tindakan Mandiri", "DIAGNOSIS KEPERAWATAN", "INTERVENSI KEPERAWATAN",
                    "TINDAKAN KEPERAWATAN MANDIRI" },
            { "Tindakan Klinis & Kolaborasi", "KARDIOPULMONAL", "TINDAKAN PENUNJANG", "TINDAKAN KOLABORASI",
                    "TRAUMA / BEDAH", "THT / MATA", "TINDAKAN OBGYN" },
            { "Pemberian Obat / Infus", "PEMBERIAN OBAT / INFUS" },
            { "Kondisi Pasien Saat Pindah / Pulang dari IGD", "KONDISI PASIEN SAAT PINDAH / PULANG DARI IGD",
                    "Pendidikan Kesehatan Pasien Pulang" },
    };

    private static final String[] SUBJUDUL_HALAMAN_KEP = {
            "Keluhan utama, riwayat alergi",
            "Tanda vital, berat/tinggi badan",
            "Asesmen psikologis & status sosial-ekonomi",
            "Skrining geriatri & risiko jatuh",
            "Pengkajian spiritual, fungsional & budaya",
            "Skrining gizi dewasa dan anak",
            "Skala nyeri, PQRST, FLACC",
            "Diagnosis & rencana keperawatan",
            "Kardiopulmonal s.d. tindakan obgyn",
            "0 entri dicatat",
            "Kondisi akhir, rujukan, edukasi pulang"
    };

    /**
     * Kunci pencocokan judul: dirapatkan spasinya lalu dibesarkan hurufnya,
     * supaya beda kapital/spasi antara tabel di atas dan pemanggil
     * tambahBarisKepJudul() tidak diam-diam menjatuhkan satu bagian.
     */
    private static String kunciJudulKep(String judul) {
        return judul == null ? "" : judul.trim().replaceAll("\\s+", " ").toUpperCase();
    }

    /**
     * [Bagian D] Pecah 6 panel section raksasa jadi satu panel PER BAGIAN.
     *
     * Sebuah "bagian" di form ini bukan container: ia cuma strip judul di
     * baris ke-N sebuah GridBagLayout besar, diikuti baris-baris isi sampai
     * strip berikutnya. Tanpa container sendiri, "satu bagian per layar" tidak
     * mungkin - tidak ada yg bisa di-setVisible(false).
     *
     * Dipecah SESUDAH panel dibangun (post-hoc), bukan dgn menyunting 25
     * pemanggil tambahBarisKepJudul() + 22 .add() langsung: pola yg sama
     * dipakai rapikanIsiKep() dan gayaChipKep(), dan biayanya nol perubahan di
     * kode pembangun - tidak ada komponen yg dibuat ulang, jadi seluruh
     * listener, ButtonGroup, dan ikatan simpan/tampil tetap utuh.
     */
    private void pecahSeksiKep() {
        java.util.IdentityHashMap<java.awt.Component, SeksiKep> petaStrip = new java.util.IdentityHashMap<>();
        java.util.List<java.awt.Container> induk = new java.util.ArrayList<>();
        for (SeksiKep s : seksiKep) {
            if (s.panel != null || s.strip == null) {
                continue;
            }
            java.awt.Container p = s.strip.getParent();
            if (p == null || !(p.getLayout() instanceof java.awt.GridBagLayout)) {
                continue;
            }
            petaStrip.put(s.strip, s);
            // Induk dikumpulkan dari strip.getParent() yg NYATA, bukan dari
            // s.induk: field panelKepTiga ditukar di tengah
            // initKepTambahanHal3() (isi Diagnosis..Obgyn berakhir sbg
            // panelKepTigaDiag), jadi s.induk utk sembilan bagian itu menunjuk
            // objek yg namanya sudah berpindah.
            boolean ada = false;
            for (java.awt.Container q : induk) {
                if (q == p) {
                    ada = true;
                    break;
                }
            }
            if (!ada) {
                induk.add(p);
            }
        }
        for (java.awt.Container p : induk) {
            pecahSatuIndukKep(p, petaStrip);
        }
    }

    /**
     * Pecah SATU panel induk: sapu anaknya urut (gridy,gridx), buka sub-panel
     * baru tiap kali ketemu strip terdaftar, lalu tempel ulang tiap anak ke
     * sub-panel yg sedang terbuka DENGAN CONSTRAINTS ASLINYA.
     *
     * gridy TIDAK dinomori ulang. GridBagLayout memampatkan baris kosong jadi
     * tinggi nol, jadi baris 7..12 di sub-panel baru berperilaku sama dgn
     * 0..5 - sementara menomori ulang berarti menebak ulang gridwidth dan
     * pasangan gridx tiap baris.
     *
     * Anak yg muncul SEBELUM strip pertama (pengantar) ditinggal di panel
     * aslinya; hari ini tidak ada satu pun (sudah dihitung: keenam panel
     * langsung dibuka strip), tapi kalau nanti ada, ia tetap tampil lewat
     * panel asli itu, bukan hilang.
     */
    private void pecahSatuIndukKep(java.awt.Container p,
            java.util.IdentityHashMap<java.awt.Component, SeksiKep> petaStrip) {
        java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) p.getLayout();
        java.util.List<java.awt.Component> anak = new java.util.ArrayList<>();
        final java.util.IdentityHashMap<java.awt.Component, java.awt.GridBagConstraints> batas = new java.util.IdentityHashMap<>();
        for (java.awt.Component k : p.getComponents()) {
            anak.add(k);
            // getConstraints() memulangkan KLONA - aman disimpan lalu dipakai
            // lagi sesudah removeAll().
            batas.put(k, gbl.getConstraints(k));
        }
        java.util.Collections.sort(anak, new java.util.Comparator<java.awt.Component>() {
            @Override
            public int compare(java.awt.Component a, java.awt.Component b) {
                java.awt.GridBagConstraints ca = batas.get(a), cb = batas.get(b);
                return ca.gridy != cb.gridy ? Integer.compare(ca.gridy, cb.gridy)
                        : Integer.compare(ca.gridx, cb.gridx);
            }
        });
        p.removeAll();
        widget.PanelBiasa tujuan = null;
        java.util.List<widget.PanelBiasa> baru = new java.util.ArrayList<>();
        for (java.awt.Component k : anak) {
            SeksiKep s = petaStrip.get(k);
            if (s != null) {
                tujuan = new widget.PanelBiasa();
                tujuan.setOpaque(false);
                tujuan.setLayout(new java.awt.GridBagLayout());
                s.panel = tujuan;
                baru.add(tujuan);
            }
            (tujuan == null ? p : tujuan).add(k, batas.get(k));
        }
        for (widget.PanelBiasa q : baru) {
            pasangGlueKananKep(q);
        }
    }

    /**
     * Pasang satu kolom pengisi berbobot di kanan panel bagian.
     *
     * WAJIB, bukan hiasan: GridBagLayout MENENGAHKAN seluruh gridnya kalau tak
     * ada satu pun kolom berbobot - aturan yg sudah tercatat di CLAUDE.md. Di
     * tata letak 3 kolom lama itu tak pernah terlihat karena lebar kartu hampir
     * sama dgn lebar preferred isinya; di halaman 1240px selisihnya ~1000px dan
     * isinya melayang di TENGAH dgn gutter kosong di kedua sisi. Render halaman
     * "Keadaan Umum & Tanda Vital" memperlihatkannya telanjang: panelKeadaanUmum
     * yg lebarnya 143px duduk di x=537, bukan x=0.
     *
     * Kenapa baru muncul sekarang: baris dari tambahBarisKepJudul() dan
     * tambahBarisKepField() memang berbobot (weightx=1), jadi biasanya selalu
     * ada yg menahan grid di kiri. Tapi buatHalamanKep() MENYEMBUNYIKAN strip
     * 32px pada halaman berisi satu bagian, dan GridBagLayout mengabaikan
     * komponen tak tampil SEPENUHNYA - bobotnya ikut hilang. Begitu sisa baris
     * di bagian itu cuma wrapper ber-weightx=0 (spt cKeadaanUmum), bobot total
     * kolomnya jadi 0 dan gridnya ditengahkan.
     *
     * Pengisinya ditaruh SESUDAH kolom terpakai terkanan supaya tidak menabrak
     * sel mana pun. Baris ber-gridwidth REMAINDER ikut membentang sampai kolom
     * ini - itu memang yg diminta Bagian D (strip judul & baris isian selebar
     * halaman), bukan efek samping.
     */
    private void pasangGlueKananKep(widget.PanelBiasa panel) {
        java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) panel.getLayout();
        int kolom = 1;
        for (java.awt.Component k : panel.getComponents()) {
            java.awt.GridBagConstraints c = gbl.getConstraints(k);
            // gridwidth 0 = REMAINDER, -1 = RELATIVE: dua-duanya "sampai ujung
            // baris", jadi dihitung 1 kolom saja - kalau tidak, kolom pengisi
            // malah bergeser ke kanan tanpa alasan.
            int lebar = c.gridwidth > 0 ? c.gridwidth : 1;
            kolom = Math.max(kolom, Math.max(0, c.gridx) + lebar);
        }
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = kolom;
        c.gridy = 0;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        panel.add(javax.swing.Box.createHorizontalGlue(), c);
    }

    /**
     * [Bagian D] Susun halamanKep dari seksiKep mengikuti GRUP_HALAMAN_KEP.
     *
     * Menggantikan urutkanSeksiKep() yg dulu MENEBAK urutan tampil dari urutan
     * panel + urutan bangun. Tebakannya benar tapi tidak perlu lagi: urutan
     * halaman sekarang tertulis eksplisit di tabel, dan itu satu-satunya
     * sumbernya. (Catatan Sesi 2 sempat menyebut urutkanSeksiKep() memperbaiki
     * bug urutan nyata - itu salah, urutan bangun ternyata sudah monoton dan
     * pengurutannya no-op; lihat koreksi di entri Sesi 3a PROGRESS.md.)
     *
     * Penyaringan strip yg disembunyikan (section lama yg dipensiunkan) pindah
     * ke sini, dan HARUS jalan sebelum buatHalamanKep() menyembunyikan strip
     * halaman berisi satu bagian - kalau tidak, penyaring ikut menelan bagian
     * yg baru saja disembunyikan karena alasan tampilan.
     */
    private void kelompokkanHalamanKep() {
        halamanKep.clear();
        java.util.Map<String, SeksiKep> perJudul = new java.util.LinkedHashMap<>();
        for (SeksiKep s : seksiKep) {
            if (s.panel == null || s.strip == null) {
                continue;
            }
            perJudul.put(kunciJudulKep(s.judul), s);
        }
        for (int gi = 0; gi < GRUP_HALAMAN_KEP.length; gi++) {
            String[] grup = GRUP_HALAMAN_KEP[gi];
            HalamanKep h = new HalamanKep(grup[0]);
            if (gi < SUBJUDUL_HALAMAN_KEP.length) {
                h.subjudul = SUBJUDUL_HALAMAN_KEP[gi];
            }
            for (int i = 1; i < grup.length; i++) {
                SeksiKep s = perJudul.remove(kunciJudulKep(grup[i]));
                if (s != null) {
                    h.anggota.add(s);
                }
            }
            if (!h.anggota.isEmpty()) {
                halamanKep.add(h);
            }
        }
        for (SeksiKep s : perJudul.values()) {
            HalamanKep h = new HalamanKep(judulRingkasKep(s.judul));
            h.anggota.add(s);
            halamanKep.add(h);
        }
    }

    /**
     * [Bagian C] Isi rail: kartu progres + sakelar mode + satu baris tiap halaman.
     */
    private void isiRailKep() {
        railKep.removeAll();
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.anchor = java.awt.GridBagConstraints.NORTH;

        c.gridy = 0;
        c.insets = new java.awt.Insets(12, 12, 10, 12);
        railKep.add(buatKartuProgresKep(), c);

        widget.Label lblHdr = new widget.Label();
        lblHdr.setText("BAGIAN PENGKAJIAN");
        lblHdr.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        lblHdr.setForeground(widget.ThemeConfig.TEXT_MUTED);
        lblHdr.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblHdr.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 4, 6, 0));
        c.gridy = 1;
        c.insets = new java.awt.Insets(4, 14, 4, 12);
        railKep.add(lblHdr, c);

        c.insets = new java.awt.Insets(0, 8, 0, 8);
        int gy = 2;
        for (int i = 0; i < halamanKep.size(); i++) {
            c.gridy = gy++;
            railKep.add(buatBarisRailKep(i), c);
        }

        c.gridy = gy++;
        c.insets = new java.awt.Insets(10, 10, 10, 10);
        railKep.add(buatToggleModeKep(), c);

        // Pengisi vertikal - tanpa ini GridBagLayout menengahkan seluruh
        // daftar saat rail lebih tinggi dari isinya.
        c.gridy = gy;
        c.weighty = 1.0;
        c.fill = java.awt.GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        railKep.add(javax.swing.Box.createGlue(), c);

        if (!halamanKep.isEmpty()) {
            halamanAktifKep = 0;
            terapkanModeHalamanKep();
            sorotHalamanKep(0);
        }
    }

    /**
     * [Bagian C] Sakelar "Satu bagian" / "Semua bagian" di kepala rail.
     *
     * Dua widget.RadioButton dalam satu ButtonGroup, digayakan lewat
     * gayaChipKep() yg sudah ada - bukan dua widget.Button - supaya "yg sedang
     * aktif" terbaca dari chip terpilih dan tidak perlu kode pewarnaan kedua.
     */
    private widget.PanelBiasa buatToggleModeKep() {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(false);
        p.setLayout(new java.awt.GridBagLayout());

        rbSatuHalamanKep = new widget.RadioButton();
        rbSatuHalamanKep.setText("Satu bagian");
        rbSemuaHalamanKep = new widget.RadioButton();
        rbSemuaHalamanKep.setText("Semua bagian");
        bgModeHalamanKep = new javax.swing.ButtonGroup();
        bgModeHalamanKep.add(rbSatuHalamanKep);
        bgModeHalamanKep.add(rbSemuaHalamanKep);
        rbSatuHalamanKep.setSelected(satuHalamanKep);
        rbSemuaHalamanKep.setSelected(!satuHalamanKep);

        java.awt.event.ActionListener ubah = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                satuHalamanKep = rbSatuHalamanKep.isSelected();
                terapkanModeHalamanKep();
                keHalamanKep(Math.max(0, halamanAktifKep));
            }
        };
        rbSatuHalamanKep.addActionListener(ubah);
        rbSemuaHalamanKep.addActionListener(ubah);

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        p.add(rbSatuHalamanKep, c);
        c.gridx = 1;
        c.insets = new java.awt.Insets(0, 4, 0, 0);
        p.add(rbSemuaHalamanKep, c);
        c.gridx = 2;
        c.weightx = 1.0;
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        p.add(javax.swing.Box.createGlue(), c);
        gayaChipKep(p);
        return p;
    }

    /**
     * [Bagian C] Kartu progres di kepala rail.
     */
    private widget.PanelBiasa buatKartuProgresKep() {
        widget.PanelBiasa kartu = new widget.PanelBiasa();
        latarKep(kartu, widget.ThemeConfig.SURFACE_ALT);
        kartu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
                javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        kartu.setLayout(new java.awt.GridBagLayout());

        lblProgresJudulKep = new widget.Label();
        lblProgresJudulKep.setText("ISIAN WAJIB");
        lblProgresJudulKep.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblProgresJudulKep.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        lblProgresJudulKep.setForeground(widget.ThemeConfig.TEXT_MUTED);

        lblProgresPersenKep = new widget.Label();
        lblProgresPersenKep.setText("0%");
        lblProgresPersenKep.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblProgresPersenKep.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 15));
        lblProgresPersenKep.setForeground(widget.ThemeConfig.TEXT);

        pbProgresKep = new javax.swing.JProgressBar();
        pbProgresKep.setMinimum(0);
        pbProgresKep.setMaximum(100);
        pbProgresKep.setValue(0);
        pbProgresKep.setPreferredSize(new java.awt.Dimension(200, 6));
        pbProgresKep.setStringPainted(false);
        pbProgresKep.setBorder(null);
        pbProgresKep.setBackground(new java.awt.Color(0xE2, 0xE8, 0xF0));
        pbProgresKep.setForeground(widget.ThemeConfig.PRIMARY);

        lblProgresHitungKep = new widget.Label();
        lblProgresHitungKep.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblProgresHitungKep.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        lblProgresHitungKep.setForeground(widget.ThemeConfig.TEXT_MUTED);
        lblProgresHitungKep.setText("0/17 isian wajib · 1 opsional terisi");

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        kartu.add(lblProgresJudulKep, c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        c.fill = java.awt.GridBagConstraints.NONE;
        kartu.add(lblProgresPersenKep, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.insets = new java.awt.Insets(6, 0, 0, 0);
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        kartu.add(pbProgresKep, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.insets = new java.awt.Insets(6, 0, 0, 0);
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        kartu.add(lblProgresHitungKep, c);

        return kartu;
    }

    /**
     * [Bagian C] Satu baris rail = panel yg bisa diklik.
     */
    private widget.PanelBiasa buatBarisRailKep(final int idx) {
        final HalamanKep h = halamanKep.get(idx);
        widget.PanelBiasa baris = new widget.PanelBiasa();
        latarKep(baris, widget.ThemeConfig.SURFACE);
        baris.setLayout(new java.awt.BorderLayout(0, 0));
        baris.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 0, 1, 0));
        baris.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        widget.PanelBiasa aksen = new widget.PanelBiasa();
        latarKep(aksen, widget.ThemeConfig.SURFACE);
        aksen.setPreferredSize(new java.awt.Dimension(3, 10));

        widget.PanelBiasa isi = new widget.PanelBiasa();
        isi.setOpaque(false);
        isi.setLayout(new java.awt.BorderLayout(8, 0));
        isi.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 9, 7, 9));

        String noStr = String.format("%02d", idx + 1);
        widget.Label lblNomor = new widget.Label();
        lblNomor.setText(noStr);
        lblNomor.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 11));
        lblNomor.setForeground(widget.ThemeConfig.TEXT_FAINT);
        lblNomor.setPreferredSize(new java.awt.Dimension(22, 18));

        widget.Label lblJudul = new widget.Label();
        lblJudul.setText(h.judul);
        lblJudul.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        lblJudul.setForeground(widget.ThemeConfig.TEXT_NORMAL);
        lblJudul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        widget.Label lblHitung = new widget.Label();
        lblHitung.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 11));
        lblHitung.setForeground(new java.awt.Color(0xC0, 0x8A, 0x2E));
        lblHitung.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblHitung.setPreferredSize(new java.awt.Dimension(36, 18));
        lblHitung.setText("-");

        isi.add(lblNomor, java.awt.BorderLayout.WEST);
        isi.add(lblJudul, java.awt.BorderLayout.CENTER);
        isi.add(lblHitung, java.awt.BorderLayout.EAST);

        baris.add(aksen, java.awt.BorderLayout.WEST);
        baris.add(isi, java.awt.BorderLayout.CENTER);

        h.baris = baris;
        h.lblJudul = lblJudul;
        h.lblNomor = lblNomor;
        h.lblHitunganRail = lblHitung;
        h.aksen = aksen;

        baris.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                sorotHalamanKep(idx);
                keHalamanKep(idx);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (idx != halamanAktifKep) {
                    latarKep(h.baris, widget.ThemeConfig.SURFACE_ALT);
                    h.baris.repaint();
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (idx != halamanAktifKep) {
                    latarKep(h.baris, widget.ThemeConfig.SURFACE);
                    h.baris.repaint();
                }
            }
        });
        return baris;
    }

    /**
     * Lebar teks judul di rail, dalam piksel perangkat (lihat htmlLebarKep).
     */
    private static final int LEBAR_TEKS_RAIL_KEP = 228;

    /**
     * Buang penomoran romawi di depan judul section.
     */
    private String judulRingkasKep(String judul) {
        if (judul == null) {
            return "";
        }
        return judul.replaceFirst("^\\s*(?:I|II|III|IV|V|VI|VII|VIII|IX|X)\\.\\s*", "").trim();
    }

    /** [Bagian C] Pindahkan sorotan rail ke halaman ke-idx (tidak menggulung). */
    private void sorotHalamanKep(int idx) {
        if (idx < 0 || idx >= halamanKep.size()) {
            return;
        }
        for (int i = 0; i < halamanKep.size(); i++) {
            HalamanKep h = halamanKep.get(i);
            if (h.baris == null) {
                continue;
            }
            latarKep(h.baris, widget.ThemeConfig.SURFACE);
            latarKep(h.aksen, widget.ThemeConfig.SURFACE);
            if (h.lblJudul != null) {
                h.lblJudul.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
                h.lblJudul.setForeground(widget.ThemeConfig.TEXT_NORMAL);
            }
            if (h.lblNomor != null) {
                h.lblNomor.setForeground(widget.ThemeConfig.TEXT_FAINT);
            }
            h.baris.repaint();
        }
        halamanAktifKep = idx;
        HalamanKep h = halamanKep.get(idx);
        if (h.baris != null) {
            latarKep(h.baris, new java.awt.Color(0xF2, 0xF7, 0xFB));
            latarKep(h.aksen, widget.ThemeConfig.PRIMARY);
            if (h.lblJudul != null) {
                h.lblJudul.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
                h.lblJudul.setForeground(widget.ThemeConfig.PRIMARY);
            }
            if (h.lblNomor != null) {
                h.lblNomor.setForeground(widget.ThemeConfig.PRIMARY);
            }
            h.baris.repaint();
        }
    }

    /**
     * [Bagian D] Terapkan mode tampil: satu halaman saja, atau semua ditumpuk.
     *
     * setVisible, bukan CardLayout - prompt mengizinkan keduanya, dan mode
     * "Semua bagian" menuntut halaman TETAP bertumpuk & bisa digulung, yg
     * justru tidak bisa dilakukan CardLayout (ia hanya menampilkan satu kartu).
     * Satu mekanisme melayani dua mode; dua mekanisme berarti dua jalur bug.
     */
    private void terapkanModeHalamanKep() {
        int aktif = Math.max(0, halamanAktifKep);
        for (int i = 0; i < halamanKep.size(); i++) {
            HalamanKep h = halamanKep.get(i);
            if (h.panel != null) {
                h.panel.setVisible(!satuHalamanKep || i == aktif);
            }
        }
        if (FormInput != null) {
            FormInput.revalidate();
            FormInput.repaint();
        }
    }

    /**
     * [Bagian C] Tampilkan halaman ke-idx.
     *
     * Mode "Satu bagian": tidak ada yg perlu digulung - halaman lain memang
     * disembunyikan, jadi cukup memunculkan yg dituju lalu mengembalikan
     * gulungan ke atas. Mode "Semua bagian": gulung sampai kartu halamannya
     * terlihat.
     */
    private void keHalamanKep(int idx) {
        if (idx < 0 || idx >= halamanKep.size() || scrollSeksiKep == null) {
            return;
        }
        HalamanKep h = halamanKep.get(idx);
        if (h.panel == null) {
            return;
        }
        if (satuHalamanKep) {
            terapkanModeHalamanKep();
            scrollSeksiKep.getVerticalScrollBar().setValue(0);
            return;
        }
        java.awt.Rectangle r = javax.swing.SwingUtilities.convertRectangle(
                h.panel.getParent(), h.panel.getBounds(), FormInput);
        r.height = Math.min(r.height, Math.max(1, FormInput.getHeight() - r.y));
        FormInput.scrollRectToVisible(r);
    }

    /**
     * [Bagian A] Rangka layar 4 zona. Dipanggil SESUDAH susunHalamanKep()
     * karena memakai FormInput yg sudah tersusun.
     *
     * internalFrame2 (BorderLayout)
     * NORTH barPasienKep <- hasil buatBannerKep(), tidak lagi menggulung
     * WEST scrollRailKep <- navigasi halaman (isiRailKep)
     * CENTER scrollSeksiKep <- FormInput
     * panelGlass8 sudah menempati PAGE_END internalFrame1 (action bar).
     *
     * Yg berubah dari sebelumnya: bar pasien dulu berada DI DALAM FormInput
     * sehingga ikut hilang ke atas begitu form digulung - identitas pasien
     * lepas dari pandangan justru saat pengisian. Sekarang dipatok di NORTH,
     * hanya panel seksi yg menggulung.
     *
     * `scrollInput` bawaan GEN ditinggalkan sebagai field tak terpakai
     * (dirujuk hanya di dalam blok GEN), bukan dihapus - blok GEN tidak boleh
     * disunting tangan.
     */
    private void susunRangkaKep() {
        // -- zona NORTH: bar pasien --
        barPasienKep = buatBannerKep();
        latarKep(barPasienKep, widget.ThemeConfig.SURFACE);
        barPasienKep.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, widget.ThemeConfig.BORDER_SOFT),
                javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        // -- zona WEST: rail navigasi bagian --
        railKep = new RailKepScrollable();
        latarKep(railKep, widget.ThemeConfig.SURFACE);
        railKep.setLayout(new java.awt.GridBagLayout());
        scrollRailKep = new widget.ScrollPane();
        scrollRailKep.setViewportView(railKep);
        scrollRailKep
                .setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, widget.ThemeConfig.BORDER_SOFT));
        scrollRailKep.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollRailKep.setPreferredSize(new java.awt.Dimension(300, 10));
        scrollRailKep.getVerticalScrollBar().setUnitIncrement(16);
        scrollRailKep.getViewport().setBackground(widget.ThemeConfig.SURFACE);

        // -- zona CENTER: panel seksi --
        scrollSeksiKep = new widget.ScrollPane();
        scrollSeksiKep.setViewportView(FormInput);
        scrollSeksiKep.setBorder(null);
        scrollSeksiKep.getVerticalScrollBar().setUnitIncrement(16);
        scrollSeksiKep.getViewport().setBackground(widget.ThemeConfig.BACKGROUND);

        internalFrame2.removeAll();
        internalFrame2.setLayout(new java.awt.BorderLayout());
        internalFrame2.add(barPasienKep, java.awt.BorderLayout.NORTH);
        internalFrame2.add(scrollRailKep, java.awt.BorderLayout.WEST);
        internalFrame2.add(scrollSeksiKep, java.awt.BorderLayout.CENTER);
        // Isi rail SESUDAH scrollSeksiKep ada - keHalamanKep() memakainya.
        isiRailKep();
        internalFrame2.revalidate();
        internalFrame2.repaint();
    }

    /** Memasang bar header teal 34px di puncak internalFrame1 (PAGE_START). */
    private void pasangTopBarKep() {
        internalFrame1.setBorder(null);

        javax.swing.JPanel pnlTopBar = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setColor(new java.awt.Color(0x12, 0x3c, 0x3a));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pnlTopBar.setOpaque(true);
        pnlTopBar.setPreferredSize(new java.awt.Dimension(100, 34));
        pnlTopBar.setLayout(new java.awt.BorderLayout(12, 0));
        pnlTopBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 0, 16));

        javax.swing.JPanel pnlLeft = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        pnlLeft.setOpaque(false);

        javax.swing.JLabel lblTitle = new javax.swing.JLabel("Pengkajian Awal Keperawatan IGD");
        lblTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblTitle.setForeground(java.awt.Color.WHITE);

        javax.swing.JLabel lblBadge = new javax.swing.JLabel("RM 03-4 / 03-5") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new java.awt.Color(0x12, 0x3c, 0x3a));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 3, 3);
                g2.setColor(new java.awt.Color(0x2a, 0x5d, 0x5a));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 3, 3);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadge.setFont(new java.awt.Font("Segoe UI Mono", java.awt.Font.BOLD, 10));
        lblBadge.setForeground(new java.awt.Color(0x7d, 0xd3, 0xc8));
        lblBadge.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 6, 1, 6));

        pnlLeft.add(lblTitle);
        pnlLeft.add(lblBadge);

        javax.swing.JLabel lblHospital = new javax.swing.JLabel("RSUD Demang Sepulau Raya");
        lblHospital.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        lblHospital.setForeground(new java.awt.Color(0xa2, 0xc7, 0xc2));

        pnlTopBar.add(pnlLeft, java.awt.BorderLayout.WEST);
        pnlTopBar.add(lblHospital, java.awt.BorderLayout.EAST);
        internalFrame1.add(pnlTopBar, java.awt.BorderLayout.PAGE_START);
    }

    /**
     * Assembler utama [Bagian D]: susun ulang FormInput jadi tumpukan HALAMAN,
     * satu halaman per kelompok bagian (lihat GRUP_HALAMAN_KEP).
     *
     * Menggantikan susunTigaKolomKep(), tata letak 3 kartu kolom hasil Sesi
     * 1-2. Tiga kolom DIBUANG, bukan disempurnakan: prompt Bagian D meminta
     * satu bagian mengisi satu layar, dan itu berlawanan arah dgn memecah
     * layar jadi tiga kartu 330px. Efek sampingnya dua masalah Sesi 3a ikut
     * lenyap tanpa kode tambahan - Kolom 3 yg 606px (biang scrollbar
     * horizontal) dan pelipatan baris tanda vital jadi 2 kolom sempit.
     *
     * Reparent, bukan bangun ulang: panel section yg sudah ada dipecah per
     * bagian oleh pecahSeksiKep() lalu ditempel ke kartu halaman. Tidak ada
     * komponen yg dibuat ulang, jadi listener, ButtonGroup, dan ikatan
     * simpan/tampil tetap apa adanya.
     *
     * Bar pasien TIDAK dirakit di sini; ia zona NORTH milik susunRangkaKep()
     * yg dipanggil sesudah method ini.
     */
    private void susunHalamanKep() {
        FormInput.removeAll();
        FormInput.setLayout(new java.awt.GridBagLayout());
        // latarKep(), bukan setBackground(): FormInput ini widget.PanelBiasa dan
        // ia LATAR SATU-SATUNYA yg membuat kartu halaman putih terbaca sbg
        // kartu. Dgn setBackground() saja, #F7F9FB ditimpa putih (lihat
        // latarKep) - kartu putih di atas latar putih, tinggal garis 1px-nya.
        latarKep(FormInput, widget.ThemeConfig.BACKGROUND);
        FormInput.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14));
        // GEN memanggil FormInput.setPreferredSize(870,1203) - reset ke null
        // supaya GridBagLayout yg menghitung ukuran (kalau tidak, lebar/tinggi
        // terkunci & halaman kepotong / scroll salah).
        FormInput.setPreferredSize(null);

        // Panel section dibuat transparan supaya kartu halaman putih tembus.
        widget.PanelBiasa[] seksi = { panelKepAtas, panelKepDua, panelKepTiga,
                panelKepTigaDiag, panelKepObat, panelKepEmpat };
        for (widget.PanelBiasa p : seksi) {
            if (p != null) {
                p.setOpaque(false);
            }
        }

        daftarkanRiwayatKep();
        pecahSeksiKep();
        kelompokkanHalamanKep();

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.anchor = java.awt.GridBagConstraints.NORTH;
        int gy = 0;
        for (int i = 0; i < halamanKep.size(); i++) {
            HalamanKep h = halamanKep.get(i);
            buatHalamanKep(h, i);
            c.gridy = gy++;
            c.insets = new java.awt.Insets(i == 0 ? 0 : 12, 0, 0, 0);
            FormInput.add(h.panel, c);
        }
        // Glue vertikal (aturan CLAUDE.md): tanpa ini GridBagLayout
        // menengahkan kartu halaman saat isinya lebih pendek dari layar.
        c.gridy = gy;
        c.weighty = 1.0;
        c.fill = java.awt.GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        FormInput.add(javax.swing.Box.createGlue(), c);

        updateVisibilitasGizi();
        FormInput.revalidate();
        FormInput.repaint();
    }

    private DlgMasterTemplateKeperawatan dlgMasterTemplateKep;

    private void bukaDialogTemplateKeperawatan() {
        if (dlgMasterTemplateKep == null) {
            dlgMasterTemplateKep = new DlgMasterTemplateKeperawatan(null, true);
        }
        dlgMasterTemplateKep.setPetugas(KdPetugas.getText());
        dlgMasterTemplateKep.setListener((namaTemplate, keluhanUtama, rencanaKeperawatan) -> {
            terapkanTemplateKeperawatan(keluhanUtama, rencanaKeperawatan);
        });
        dlgMasterTemplateKep.setLocationRelativeTo(this);
        dlgMasterTemplateKep.setVisible(true);
    }

    private void terapkanTemplateKeperawatan(String keluhan, String rencana) {
        if (keluhan != null && !keluhan.trim().isEmpty()) {
            if (KeluhanUtama.getText().trim().isEmpty()) {
                KeluhanUtama.setText(keluhan.trim());
            } else {
                int c = JOptionPane.showConfirmDialog(this,
                    "Kolom Keluhan Utama sudah terisi.\nApakah Anda ingin menimpa (Yes) atau menambahkan di bawahnya (No)?",
                    "Konfirmasi Template", JOptionPane.YES_NO_CANCEL_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    KeluhanUtama.setText(keluhan.trim());
                } else if (c == JOptionPane.NO_OPTION) {
                    KeluhanUtama.setText(KeluhanUtama.getText().trim() + "\n" + keluhan.trim());
                }
            }
        }
        if (rencana != null && !rencana.trim().isEmpty()) {
            if (Rencana != null) {
                if (Rencana.getText().trim().isEmpty()) {
                    Rencana.setText(rencana.trim());
                } else {
                    Rencana.setText(Rencana.getText().trim() + "\n" + rencana.trim());
                }
            }
        }
        hitungProgresKep();
    }

    private void tampilkanMenuTemplateKeperawatan(javax.swing.JComponent anchor) {
        javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
        javax.swing.JMenuItem itemKelola = new javax.swing.JMenuItem("Kelola Master Template Keperawatan...");
        itemKelola.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        itemKelola.setForeground(new java.awt.Color(15, 61, 62));
        itemKelola.addActionListener(e -> bukaDialogTemplateKeperawatan());
        popup.add(itemKelola);
        popup.addSeparator();

        try {
            String nip = KdPetugas.getText().trim();
            java.sql.PreparedStatement psTpl = koneksi.prepareStatement(
                "select no_template, nama_template, keluhan_utama, rencana_keperawatan " +
                "from template_asuhan_keperawatan_igd where nip='-' or nip=? order by nama_template asc"
            );
            psTpl.setString(1, nip.isEmpty() ? "-" : nip);
            java.sql.ResultSet rsTpl = psTpl.executeQuery();
            int hitung = 0;
            while (rsTpl.next()) {
                hitung++;
                String nm = rsTpl.getString("nama_template");
                String kel = rsTpl.getString("keluhan_utama");
                String ren = rsTpl.getString("rencana_keperawatan");
                javax.swing.JMenuItem itm = new javax.swing.JMenuItem(nm);
                itm.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
                itm.addActionListener(e -> terapkanTemplateKeperawatan(kel, ren));
                popup.add(itm);
            }
            rsTpl.close();
            psTpl.close();
            if (hitung == 0) {
                javax.swing.JMenuItem empty = new javax.swing.JMenuItem("(Belum ada template tersimpan)");
                empty.setEnabled(false);
                popup.add(empty);
            }
        } catch (Exception ex) {
            System.out.println("Notif load template keperawatan: " + ex);
        }
        popup.show(anchor, 0, anchor.getHeight());
    }

    private widget.PanelBiasa siapkanAreaKeluhanUtamaKep() {
        widget.PanelBiasa panel = new widget.PanelBiasa(new java.awt.BorderLayout(0, 6));
        panel.setOpaque(false);

        widget.PanelBiasa toolbar = new widget.PanelBiasa(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        javax.swing.JButton btnMaster = new javax.swing.JButton("Master Template Keperawatan");
        btnMaster.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        btnMaster.setForeground(new java.awt.Color(15, 61, 62));
        btnMaster.setFocusPainted(false);
        btnMaster.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMaster.setPreferredSize(new java.awt.Dimension(220, 28));
        btnMaster.setToolTipText("Kelola dan pilih format narasi Asuhan Keperawatan IGD");
        btnMaster.addActionListener(e -> bukaDialogTemplateKeperawatan());

        javax.swing.JButton btnMenu = new javax.swing.JButton("Template Cepat  ▼");
        btnMenu.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        btnMenu.setForeground(new java.awt.Color(15, 61, 62));
        btnMenu.setFocusPainted(false);
        btnMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenu.setPreferredSize(new java.awt.Dimension(150, 28));
        btnMenu.setToolTipText("Daftar template narasi keperawatan IGD tersimpan");
        btnMenu.addActionListener(e -> tampilkanMenuTemplateKeperawatan(btnMenu));

        toolbar.add(btnMaster);
        toolbar.add(btnMenu);

        panel.add(toolbar, java.awt.BorderLayout.NORTH);
        panel.add(scrollPane1, java.awt.BorderLayout.CENTER);
        return panel;
    }

    /**
     * Bagian "I. RIWAYAT KESEHATAN PASIEN" tidak lahir dari
     * tambahBarisKepJudul(): isinya scrollPane1 (Keluhan Utama) bawaan GEN yg
     * tidak pernah masuk panel section mana pun. Jadi panelnya dirakit di sini
     * dan didaftarkan lengkap dgn `panel` terisi - pecahSeksiKep() otomatis
     * melewatinya karena syaratnya `panel==null`.
     */
    private void daftarkanRiwayatKep() {
        final String judul = "RIWAYAT KESEHATAN PASIEN";
        for (SeksiKep s : seksiKep) {
            if (kunciJudulKep(judul).equals(kunciJudulKep(s.judul))) {
                return;
            }
        }
        widget.PanelBiasa isi = new widget.PanelBiasa();
        isi.setOpaque(false);
        isi.setLayout(new java.awt.GridBagLayout());
        widget.PanelBiasa strip = buatHeaderStripKep(judul);
        java.awt.GridBagConstraints c = kepConstraint(0, 0);
        c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        isi.add(strip, c);

        scrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT));
        scrollPane1.setPreferredSize(new java.awt.Dimension(580, 100));
        scrollPane1.setMinimumSize(new java.awt.Dimension(580, 100));
        scrollPane1.setOpaque(true);
        scrollPane1.getViewport().setOpaque(true);
        scrollPane1.getViewport().setBackground(java.awt.Color.WHITE);
        KeluhanUtama.setBackground(java.awt.Color.WHITE);
        KeluhanUtama.setOpaque(true);
        KeluhanUtama.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8));
        KeluhanUtama.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        KeluhanUtama.setLineWrap(true);
        KeluhanUtama.setWrapStyleWord(true);
        KeluhanUtama.setPlaceholder("Tuliskan keluhan utama pasien...");
        KeluhanUtama.putClientProperty("JTextField.placeholderText", "Tuliskan keluhan utama pasien...");
        KeluhanUtama.putClientProperty("TextComponent.arc", 0);
        KeluhanUtama.putClientProperty("Component.arc", 0);
        scrollPane1.putClientProperty("Component.arc", 0);

        barisPengkajian(isi, 1, "Keluhan Utama", true, siapkanAreaKeluhanUtamaKep());
        SeksiKep s = new SeksiKep(judul, strip, isi, -1);
        s.panel = isi;
        seksiKep.add(0, s);
    }

    /**
     * Lebar kartu halaman, dalam piksel perangkat.
     */
    private static final int LEBAR_HALAMAN_KEP = 1230;

    /**
     * [Bagian D] Rakit satu kartu halaman: strip judul 52px + panel bagian +
     * navigasi bawah 48px.
     *
     * @param idx index halaman, dipakai utk teks "Bagian i dari N" di strip
     */
    private void buatHalamanKep(HalamanKep h, int idx) {
        widget.PanelBiasa kartu = new widget.PanelBiasa();
        latarKep(kartu, widget.ThemeConfig.SURFACE);
        kartu.setBorder(javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT));
        kartu.setLayout(new java.awt.GridBagLayout());

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.anchor = java.awt.GridBagConstraints.NORTHWEST;
        kartu.add(buatStripHalamanKep(h, idx), c);

        int gy = 1;
        if ("Skrining Gizi (MST / Anak)".equalsIgnoreCase(h.judul) || idx == 5) {
            barisPilihanGizi = buatBarisKepField("Pilihan Skrining Gizi", true, buatPanelRadioPilihanGizi());
            c.gridy = gy++;
            c.insets = new java.awt.Insets(0, 12, 0, 12);
            kartu.add(barisPilihanGizi, c);
        }
        for (SeksiKep s : h.anggota) {
            c.gridy = gy++;
            c.insets = new java.awt.Insets(0, 12, 0, 12);
            kartu.add(s.panel, c);
            if ("SKRINING GIZI DEWASA (MST)".equalsIgnoreCase(s.judul)) {
                panelSeksiGiziDewasa = s.panel;
                panelSeksiGiziDewasa.setVisible(RbPilihGiziDewasa != null && RbPilihGiziDewasa.isSelected());
            } else if ("SKRINING GIZI ANAK (USIA 0-14 TAHUN)".equalsIgnoreCase(s.judul)) {
                panelSeksiGiziAnak = s.panel;
                panelSeksiGiziAnak.setVisible(RbPilihGiziAnak != null && RbPilihGiziAnak.isSelected());
            }
            if (h.anggota.size() == 1
                    || "RIWAYAT KESEHATAN PASIEN".equalsIgnoreCase(s.judul)
                    || "RIWAYAT ALERGI".equalsIgnoreCase(s.judul)
                    || "ASESMEN NYERI".equalsIgnoreCase(s.judul)
                    || "KEADAAN UMUM".equalsIgnoreCase(s.judul)
                    || "STATUS KEHAMILAN".equalsIgnoreCase(s.judul)
                    || "PEMBERIAN OBAT / INFUS".equalsIgnoreCase(s.judul)) {
                s.strip.setVisible(false);
            }
        }
        c.gridy = gy++;
        c.insets = new java.awt.Insets(0, 0, 10, 0);
        kartu.add(javax.swing.Box.createVerticalStrut(1), c);

        // Navigasi bawah kartu 48px
        c.gridy = gy++;
        c.insets = new java.awt.Insets(10, 0, 0, 0);
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        kartu.add(buatNavigasiBawahKep(idx), c);

        // Strut lebar minimum di baris terpisah (gridy 900)
        java.awt.GridBagConstraints cs = new java.awt.GridBagConstraints();
        cs.gridx = 0;
        cs.gridy = 900;
        cs.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        kartu.add(javax.swing.Box.createHorizontalStrut(LEBAR_HALAMAN_KEP - 2), cs);

        h.panel = kartu;
        gayaChipKep(kartu);
        rapikanIsiKep(kartu, LEBAR_HALAMAN_KEP - 42);
    }

    /**
     * [Bagian D] Strip kepala halaman 52px: Badge nomor 26x26 + Judul & Subjudul +
     * Status + Tombol relevan.
     */
    private widget.PanelBiasa buatStripHalamanKep(final HalamanKep h, final int idx) {
        widget.PanelBiasa strip = new widget.PanelBiasa();
        latarKep(strip, new java.awt.Color(0xFB, 0xFC, 0xFA));
        strip.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, widget.ThemeConfig.BORDER_SOFT));
        strip.setLayout(new java.awt.BorderLayout(0, 0));
        strip.setPreferredSize(new java.awt.Dimension(10, 52));

        // -- Bagian KIRI: Badge nomor 26x26 + Judul & Subjudul --
        widget.PanelBiasa kiri = new widget.PanelBiasa();
        kiri.setOpaque(false);
        kiri.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 6));

        widget.Label badge = new widget.Label();
        badge.setText(String.valueOf(idx + 1));
        badge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        badge.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        badge.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        badge.setForeground(java.awt.Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(h.tidakRelevan ? new java.awt.Color(0xA0, 0xAE, 0xC0) : widget.ThemeConfig.PRIMARY);
        badge.setPreferredSize(new java.awt.Dimension(26, 26));
        badge.setMinimumSize(new java.awt.Dimension(26, 26));
        h.lblBadgeNomor = badge;
        kiri.add(badge);

        javax.swing.JPanel pnlJudul = new javax.swing.JPanel();
        pnlJudul.setOpaque(false);
        pnlJudul.setLayout(new javax.swing.BoxLayout(pnlJudul, javax.swing.BoxLayout.Y_AXIS));

        widget.Label lJudul = new widget.Label();
        lJudul.setText(h.judul == null ? "" : h.judul);
        lJudul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lJudul.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        lJudul.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        lJudul.setForeground(widget.ThemeConfig.TEXT);

        widget.Label lSub = new widget.Label();
        lSub.setText(h.subjudul == null ? "" : h.subjudul);
        lSub.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lSub.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        lSub.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        lSub.setForeground(widget.ThemeConfig.TEXT_FAINT);

        pnlJudul.add(lJudul);
        pnlJudul.add(javax.swing.Box.createVerticalStrut(1));
        pnlJudul.add(lSub);
        kiri.add(pnlJudul);

        // -- Bagian KANAN: Status Badge + Tombol Tandai tidak relevan --
        widget.PanelBiasa kanan = new widget.PanelBiasa();
        kanan.setOpaque(false);
        kanan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 11));

        widget.PanelBiasa pnlStatus = new widget.PanelBiasa();
        pnlStatus.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 8, 4));
        pnlStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 6, 1, 6));
        widget.Label lStatus = new widget.Label();
        lStatus.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11).deriveFont(11.5f));
        lStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pnlStatus.add(lStatus);
        h.pnlBadgeStatus = pnlStatus;
        h.lblBadgeStatus = lStatus;
        kanan.add(pnlStatus);

        final widget.Button btnRel = new widget.Button();
        btnRel.setText(h.tidakRelevan ? "↺ Aktifkan kembali" : "Tandai tidak relevan");
        btnRel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        btnRel.setPreferredSize(new java.awt.Dimension(140, 28));
        btnRel.setGlassColor(java.awt.Color.WHITE);
        btnRel.setBackground(java.awt.Color.WHITE);
        btnRel.setForeground(widget.ThemeConfig.TEXT);
        btnRel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
        btnRel.addActionListener((java.awt.event.ActionEvent e) -> {
            h.tidakRelevan = !h.tidakRelevan;
            btnRel.setText(h.tidakRelevan ? "↺ Aktifkan kembali" : "Tandai tidak relevan");
            setHalamanRelevanKep(h, !h.tidakRelevan);
            hitungProgresKep();
        });
        h.btnTidakRelevan = btnRel;
        kanan.add(btnRel);

        strip.add(kiri, java.awt.BorderLayout.WEST);
        strip.add(kanan, java.awt.BorderLayout.EAST);
        return strip;
    }

    /**
     * [Bagian D] Kartu tombol navigasi bawah (< Awal / Judul Halaman >)
     * Berupa kotak putih bersih dengan border 1px, 1 baris lurus tanpa enter,
     * dan panjang kartu otomatis menyesuaikan isi teks.
     */
    private widget.PanelBiasa buatKartuTombolNavigasiKep(String teks, String chevron, boolean arahKanan,
            boolean enabled, final java.lang.Runnable onClick) {
        final widget.PanelBiasa card = new widget.PanelBiasa();
        card.setOpaque(true);
        card.setBackground(java.awt.Color.WHITE);
        card.setLayout(new java.awt.BorderLayout(8, 0));
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)),
                javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12)));

        widget.Label lblTeks = new widget.Label();
        lblTeks.setHorizontalAlignment(arahKanan ? javax.swing.SwingConstants.LEFT : javax.swing.SwingConstants.RIGHT);
        lblTeks.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        lblTeks.setForeground(enabled ? new java.awt.Color(0x1E, 0x29, 0x3B) : new java.awt.Color(0x94, 0xA3, 0xB8));
        lblTeks.setText(teks == null ? "" : teks.trim());

        widget.Label lblChevron = new widget.Label();
        lblChevron.setText(chevron);
        lblChevron.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        lblChevron.setForeground(enabled ? new java.awt.Color(0x64, 0x74, 0x8B) : new java.awt.Color(0xCB, 0xD5, 0xE1));

        if (arahKanan) {
            card.add(lblTeks, java.awt.BorderLayout.CENTER);
            card.add(lblChevron, java.awt.BorderLayout.EAST);
        } else {
            card.add(lblChevron, java.awt.BorderLayout.WEST);
            card.add(lblTeks, java.awt.BorderLayout.CENTER);
        }

        // Panjang card otomatis presisi mengikuti isi teks (satu baris, tanpa kepotong)
        java.awt.FontMetrics fmTeks = lblTeks.getFontMetrics(lblTeks.getFont());
        java.awt.FontMetrics fmChev = lblChevron.getFontMetrics(lblChevron.getFont());
        int pTeks = fmTeks != null ? fmTeks.stringWidth(lblTeks.getText()) : (lblTeks.getText().length() * 7);
        int pChev = fmChev != null ? fmChev.stringWidth(chevron) : 10;
        int lebarCard = pTeks + pChev + 8 + 24 + 10;
        card.setPreferredSize(new java.awt.Dimension(lebarCard, 32));

        if (enabled) {
            card.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            lblTeks.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            lblChevron.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBackground(new java.awt.Color(0xF8, 0xFA, 0xFC));
                    card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.PRIMARY),
                            javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                    card.repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBackground(java.awt.Color.WHITE);
                    card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)),
                            javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                    card.repaint();
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (onClick != null) {
                        onClick.run();
                    }
                }
            };
            card.addMouseListener(ma);
            lblTeks.addMouseListener(ma);
            lblChevron.addMouseListener(ma);
        }

        return card;
    }

    /**
     * [Bagian D] Navigasi bawah kartu: Card Tombol Prev + Label Bagian n dari N +
     * Card Tombol Next.
     */
    private widget.PanelBiasa buatNavigasiBawahKep(final int idx) {
        widget.PanelBiasa nav = new widget.PanelBiasa();
        latarKep(nav, widget.ThemeConfig.SURFACE);
        nav.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, widget.ThemeConfig.BORDER_SOFT));
        nav.setLayout(new java.awt.BorderLayout(0, 0));
        nav.setPreferredSize(new java.awt.Dimension(10, 48));

        widget.PanelBiasa pnlKiri = new widget.PanelBiasa();
        pnlKiri.setOpaque(false);
        pnlKiri.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 8));

        if (idx > 0) {
            pnlKiri.add(buatKartuTombolNavigasiKep(halamanKep.get(idx - 1).judul, "‹", false, true, () -> {
                sorotHalamanKep(idx - 1);
                keHalamanKep(idx - 1);
            }));
        } else {
            pnlKiri.add(buatKartuTombolNavigasiKep("Awal", "‹", false, false, null));
        }

        widget.Label lblPos = new widget.Label();
        lblPos.setText("Bagian " + (idx + 1) + " dari " + halamanKep.size());
        lblPos.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        lblPos.setForeground(widget.ThemeConfig.TEXT_FAINT);
        lblPos.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        widget.PanelBiasa pnlKanan = new widget.PanelBiasa();
        pnlKanan.setOpaque(false);
        pnlKanan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 8));

        if (idx < halamanKep.size() - 1) {
            pnlKanan.add(buatKartuTombolNavigasiKep(halamanKep.get(idx + 1).judul, "›", true, true, () -> {
                sorotHalamanKep(idx + 1);
                keHalamanKep(idx + 1);
            }));
        } else {
            pnlKanan.add(buatKartuTombolNavigasiKep("Akhir", "›", true, false, null));
        }

        nav.add(pnlKiri, java.awt.BorderLayout.WEST);
        nav.add(lblPos, java.awt.BorderLayout.CENTER);
        nav.add(pnlKanan, java.awt.BorderLayout.EAST);
        return nav;
    }

    /**
     * [RM 03-4] Membangun section baru HALAMAN 1 formulir fisik: Riwayat
     * Alergi, Keadaan Umum (Tanda Vital), Asesmen Psikologis, Status
     * Sosial dan Ekonomis, Skrining Geriatri, Skrining Resiko Jatuh,
     * Pengkajian Spiritual/Kepercayaan/Fungsional/Budaya. Ditempel sbg 1
     * panel BARU di bawah konten form lama (belum direposisi mengikuti
     * urutan fisik - menyusul setelah user cek tampilan, sama seperti
     * pola revisi bertahap RM 03-2).
     */
    private void initKepTambahan() {
        // [RM 03-4/05/06] Sub-tab "Rencana Keperawatan" (checklist lama,
        // tbRencanaKeperawatan/master_rencana_keperawatan_igd) disembunyikan
        // - sudah tidak dibaca/ditulis simpan()/ganti() (Intervensi
        // Keperawatan sekarang kolom boolean, lihat initKepTambahanHal3()).
        // Sub-tab "Rencana Keperawatan Lainnya" (field Rencana/TextArea)
        // TETAP ADA - masih aktif dipakai utk "III. Tindakan Keperawatan
        // Mandiri". Index 0 = tab yg ditambahkan pertama di initComponents()
        // ("Rencana Keperawatan"), stabil krn urutan addTab() tidak berubah.
        TabRencanaKeperawatan.removeTabAt(0);

        // Panel accordion "Data Pengkajian" (PanelAccor) tadinya menampilkan
        // FormMasalahRencana (tbMasalahDetailMasalah/tbRencanaDetail/
        // DetailRencana) di CENTER - ternyata TIDAK PERNAH diisi data sama
        // sekali (dead/stale scaffolding). Diganti dgn ScrollHTML+LoadHTML,
        // pola PERSIS sama dengan tab "Data Triase" di RMTriaseIGD.java -
        // ringkasan HTML read-only dibangun di getData() (lihat
        // bangunRingkasanHtml()), ditampilkan begitu baris tabel diklik.
        PanelAccor.remove(FormMasalahRencana);
        ScrollHTML = new widget.ScrollPane();
        ScrollHTML.setBorder(null);
        ScrollHTML.setName("ScrollHTML");
        ScrollHTML.setOpaque(true);
        ScrollHTML.setPreferredSize(new java.awt.Dimension(470, 16));
        ScrollHTML.setViewportView(LoadHTML);
        PanelAccor.add(ScrollHTML, java.awt.BorderLayout.CENTER);

        // Kontrol "Key Word"/cari/ceklis-semua/tambah punya tab checklist
        // lama ikut disembunyikan - tidak ada gunanya lagi (permintaan
        // user, lihat "III. TINDAKAN KEPERAWATAN MANDIRI" di
        // initKepTambahanHal3() yg sekarang cuma pakai TabRencanaKeperawatan
        // tanpa kontrol ini).
        label13.setVisible(false);
        TCariRencana.setVisible(false);
        BtnCariRencana.setVisible(false);
        BtnAllRencana.setVisible(false);
        BtnTambahRencana.setVisible(false);

        // Kotak "Keluhan Utama" diperbesar menyamai ukuran "Rencana
        // Keperawatan Lainnya" (TabRencanaKeperawatan, 420x143) atas
        // permintaan user - ruang di baris ini sudah kosong sejak RPO (yg
        // tadinya sebaris di kanan) disembunyikan. Digeser dari y=90 ke
        // y=120 (+jLabel98 title ikut turun) supaya ada ruang utk baris
        // header baru "Dokter" (lihat di bawah).
        jLabel98.setBounds(10, 100, 180, 23);
        jLabel30.setBounds(0, 120, 175, 20);
        scrollPane1.setBounds(179, 120, 420, 143);

        // -- Dokter (Kondisi Pasien Pulang) dipindah ke header, disamakan
        // pola dgn baris "Petugas" yg sudah ada - permintaan user, supaya
        // tidak perlu diisi lagi terpisah di section paling bawah. Field
        // "Perawat/Bidan" TIDAK dibuatkan baris baru - header "Petugas"
        // yg sudah ada dipakai langsung sbg nilainya (lihat nilaiKep()).
        widget.Label lblKpDokter = new widget.Label();
        lblKpDokter.setText("Dokter :");
        lblKpDokter.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(lblKpDokter);
        lblKpDokter.setBounds(0, 70, 70, 23);
        KdDokterKp = new widget.TextBox();
        KdDokterKp.setPreferredSize(new java.awt.Dimension(100, 23));
        KdDokterKp.setEditable(false);
        FormInput.add(KdDokterKp);
        KdDokterKp.setBounds(74, 70, 100, 23);
        NmDokterKp = new widget.TextBox();
        NmDokterKp.setPreferredSize(new java.awt.Dimension(180, 23));
        NmDokterKp.setEditable(false);
        FormInput.add(NmDokterKp);
        NmDokterKp.setBounds(176, 70, 180, 23);
        BtnDokterKp = new widget.Button();
        BtnDokterKp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnDokterKp.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDokterKp.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariDokter dlg = new DlgCariDokter(null, false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (dlg.getTable().getSelectedRow() != -1) {
                        KdDokterKp.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(), 0).toString());
                        NmDokterKp.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(), 1).toString());
                    }
                    BtnDokterKp.requestFocus();
                }
            });
            dlg.isCek();
            dlg.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        FormInput.add(BtnDokterKp);
        BtnDokterKp.setBounds(358, 70, 28, 23);

        panelKepAtas = new widget.PanelBiasa();
        panelKepAtas.setLayout(new java.awt.GridBagLayout());
        int row = 0;

        // -- Riwayat Alergi --
        row = tambahBarisKepJudul(panelKepAtas, row, "RIWAYAT ALERGI");
        RbAlergiTidak = new widget.RadioButton();
        RbAlergiTidak.setText("Tidak");
        RbAlergiYa = new widget.RadioButton();
        RbAlergiYa.setText("Ya, Sebutkan");
        bgAlergi = new javax.swing.ButtonGroup();
        bgAlergi.add(RbAlergiTidak);
        bgAlergi.add(RbAlergiYa);
        TAlergiSebutkan = new widget.TextBox();
        TAlergiSebutkan.setPreferredSize(new java.awt.Dimension(300, 26));
        TAlergiSebutkan.putClientProperty("JTextField.placeholderText", "Sebutkan alergi...");
        TAlergiSebutkan.putClientProperty("TextComponent.arc", 0);
        TAlergiSebutkan.putClientProperty("Component.arc", 0);
        TAlergiSebutkan.setVisible(false);

        final widget.PanelBiasa panelAlergi = new widget.PanelBiasa();
        panelAlergi.setOpaque(false);
        panelAlergi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
        panelAlergi.add(RbAlergiTidak);
        panelAlergi.add(RbAlergiYa);
        panelAlergi.add(TAlergiSebutkan);

        java.awt.event.ItemListener listenerAlergi = (java.awt.event.ItemEvent e) -> {
            boolean ya = RbAlergiYa.isSelected();
            TAlergiSebutkan.setVisible(ya);
            if (!ya) {
                TAlergiSebutkan.setText("");
            }
            panelAlergi.revalidate();
            panelAlergi.repaint();
            hitungProgresKep();
        };
        RbAlergiTidak.addItemListener(listenerAlergi);
        RbAlergiYa.addItemListener(listenerAlergi);

        row = tambahBarisKepField(panelKepAtas, row, "Riwayat Alergi", true, panelAlergi);

        // -- Keadaan Umum (Tanda Vital) --
        // Ditata 2 kolom DALAM PANEL TERSENDIRI (panelKeadaanUmum, GridBag
        // sendiri) - bukan langsung di kolom panelKepAtas seperti versi
        // lama. Kalau langsung di kolom panelKepAtas, gridx=1 dipakai
        // BARENG section lain (Riwayat Alergi/Status Kehamilan dst yg
        // butuh ~550px), jadi kolomnya ikut melebar & jarak Tekanan
        // Darah->Suhu jadi jauh walau field-nya sendiri sudah 100px.
        // Dibungkus panel sendiri, kolomnya cuma dihitung dari isinya
        // sendiri (Td|Suhu, Spo|Nadi, Bb|Rr, Tb|kosong) - rapat.
        // -- Keadaan Umum (Tanda Vital & Antropometri) --
        row = tambahBarisKepJudul(panelKepAtas, row, "KEADAAN UMUM");
        TdKep = buatTextBoxKotakLokal(null);
        TdKep.setPreferredSize(new java.awt.Dimension(80, 26));
        SuhuKep = buatTextBoxKotakLokal(null);
        SuhuKep.setPreferredSize(new java.awt.Dimension(80, 26));
        SpoKep = buatTextBoxKotakLokal(null);
        SpoKep.setPreferredSize(new java.awt.Dimension(80, 26));
        NadiKep = buatTextBoxKotakLokal(null);
        NadiKep.setPreferredSize(new java.awt.Dimension(80, 26));
        BbKep = buatTextBoxKotakLokal(null);
        BbKep.setPreferredSize(new java.awt.Dimension(80, 26));
        BbKep.putClientProperty("JTextField.placeholderText", "—");
        RrKep = buatTextBoxKotakLokal(null);
        RrKep.setPreferredSize(new java.awt.Dimension(80, 26));
        TbKep = buatTextBoxKotakLokal(null);
        TbKep.setPreferredSize(new java.awt.Dimension(80, 26));
        TbKep.putClientProperty("JTextField.placeholderText", "—");

        widget.PanelBiasa panelKeadaanUmum = new widget.PanelBiasa();
        panelKeadaanUmum.setOpaque(false);
        panelKeadaanUmum.setLayout(new java.awt.GridBagLayout());
        panelKeadaanUmum.putClientProperty("fillWidth", Boolean.TRUE);

        java.awt.GridBagConstraints cVital = new java.awt.GridBagConstraints();
        cVital.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cVital.weightx = 0.25;

        // Baris 1: Tekanan Darah, Nadi, Pernapasan, Suhu
        cVital.gridy = 0;
        cVital.gridx = 0;
        cVital.insets = new java.awt.Insets(0, 0, 10, 14);
        panelKeadaanUmum.add(buatSelVital("Tekanan Darah", TdKep, "mmHg"), cVital);

        cVital.gridx = 1;
        cVital.insets = new java.awt.Insets(0, 0, 10, 14);
        panelKeadaanUmum.add(buatSelVital("Nadi", NadiKep, "/mnt"), cVital);

        cVital.gridx = 2;
        cVital.insets = new java.awt.Insets(0, 0, 10, 14);
        panelKeadaanUmum.add(buatSelVital("Pernapasan", RrKep, "/mnt"), cVital);

        cVital.gridx = 3;
        cVital.insets = new java.awt.Insets(0, 0, 10, 0);
        panelKeadaanUmum.add(buatSelVital("Suhu", SuhuKep, "°C"), cVital);

        // Baris 2: SpO2, Berat Badan, Tinggi Badan, (kosong)
        cVital.gridy = 1;
        cVital.gridx = 0;
        cVital.insets = new java.awt.Insets(0, 0, 0, 14);
        panelKeadaanUmum.add(buatSelVital("SpO2", SpoKep, "%"), cVital);

        cVital.gridx = 1;
        cVital.insets = new java.awt.Insets(0, 0, 0, 14);
        panelKeadaanUmum.add(buatSelVital("Berat Badan", BbKep, "kg"), cVital);

        cVital.gridx = 2;
        cVital.insets = new java.awt.Insets(0, 0, 0, 14);
        panelKeadaanUmum.add(buatSelVital("Tinggi Badan", TbKep, "cm"), cVital);

        cVital.gridx = 3;
        cVital.insets = new java.awt.Insets(0, 0, 0, 0);
        widget.Button btnRefreshTtvKep = new widget.Button();
        btnRefreshTtvKep.setText("Tarik TTV Triase");
        btnRefreshTtvKep.setToolTipText("Tarik / Refresh tanda vital & keluhan dari data Triase IGD / Pemeriksaan Awal");
        btnRefreshTtvKep.setPreferredSize(new java.awt.Dimension(130, 26));
        btnRefreshTtvKep.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        btnRefreshTtvKep.addActionListener(e -> tarikDataTriase(TNoRw.getText(), true));
        panelKeadaanUmum.add(buatSelVital("Aksi TTV", btnRefreshTtvKep, null), cVital);

        row = tambahBarisKepField(panelKepAtas, row, "Tanda Vital & Antropometri", false, panelKeadaanUmum);

        // -- Status Kehamilan --
        jLabel78.setVisible(false);
        jLabel29.setVisible(false);
        jLabel32.setVisible(false);
        jLabel33.setVisible(false);
        jLabel35.setVisible(false);
        row = tambahBarisKepJudul(panelKepAtas, row, "STATUS KEHAMILAN");
        StatusKehamilan.setPreferredSize(new java.awt.Dimension(220, 26));
        widget.PanelBiasa pnlHamil = new widget.PanelBiasa();
        pnlHamil.setOpaque(false);
        pnlHamil.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        pnlHamil.add(StatusKehamilan);
        row = tambahBarisKepField(panelKepAtas, row, "Status Kehamilan", true, pnlHamil);

        Gravida = buatTextBoxKotakLokal(Gravida);
        Gravida.setPreferredSize(new java.awt.Dimension(80, 26));
        Gravida.putClientProperty("JTextField.placeholderText", "—");
        Para = buatTextBoxKotakLokal(Para);
        Para.setPreferredSize(new java.awt.Dimension(80, 26));
        Para.putClientProperty("JTextField.placeholderText", "—");
        Abortus = buatTextBoxKotakLokal(Abortus);
        Abortus.setPreferredSize(new java.awt.Dimension(80, 26));
        Abortus.putClientProperty("JTextField.placeholderText", "—");
        HPHT = buatTextBoxKotakLokal(HPHT);
        HPHT.setVisible(false);

        TglHPHT = new widget.Tanggal();
        TglHPHT.setName("TglHPHT");
        TglHPHT.setDisplayFormat("dd-MM-yyyy");
        TglHPHT.setPreferredSize(new java.awt.Dimension(80, 26));
        TglHPHT.setBackground(java.awt.Color.WHITE);
        TglHPHT.setOpaque(true);
        TglHPHT.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6)));
        TglHPHT.putClientProperty("FlatLaf.style", "background: #FFFFFF; buttonBackground: #FFFFFF;");
        TglHPHT.putClientProperty("Component.arc", 0);
        TglHPHT.putClientProperty("TextComponent.arc", 0);
        TglHPHT.putClientProperty("JComponent.roundRect", false);
        TglHPHT.addItemListener((java.awt.event.ItemEvent e) -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                if ("Hamil".equals(StatusKehamilan.getSelectedItem())) {
                    HPHT.setText(Valid.SetTgl(TglHPHT.getSelectedItem() + ""));
                } else {
                    HPHT.setText("");
                }
                hitungProgresKep();
            }
        });

        widget.PanelBiasa panelKehamilan = new widget.PanelBiasa();
        panelKehamilan.setOpaque(false);
        panelKehamilan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 18, 0));
        panelKehamilan.add(buatSelVital("Gravida", Gravida, null));
        panelKehamilan.add(buatSelVital("Para", Para, null));
        panelKehamilan.add(buatSelVital("Abortus", Abortus, null));
        panelKehamilan.add(buatSelVital("HPHT", TglHPHT, null));

        final widget.PanelBiasa barisKehamilan = barisPengkajian(panelKepAtas, row,
                "Gravida / Para / Abortus / HPHT", false, panelKehamilan);
        row++;

        // Baris Gravida/Para/Abortus/HPHT cuma tampil kalau Status Kehamilan = "Hamil"
        StatusKehamilan.addActionListener((java.awt.event.ActionEvent e) -> {
            boolean hamil = "Hamil".equals(StatusKehamilan.getSelectedItem());
            if (!hamil) {
                Gravida.setText("");
                Para.setText("");
                Abortus.setText("");
                HPHT.setText("");
                if (TglHPHT != null) {
                    TglHPHT.setDate(new java.util.Date());
                }
            } else {
                if (TglHPHT != null && TglHPHT.getSelectedItem() != null) {
                    HPHT.setText(Valid.SetTgl(TglHPHT.getSelectedItem() + ""));
                }
            }
            if (barisKehamilan.isVisible() == hamil) {
                hitungProgresKep();
                return;
            }
            barisKehamilan.setVisible(hamil);
            if (barisKehamilan.getParent() != null) {
                barisKehamilan.getParent().revalidate();
            }
            FormInput.revalidate();
            FormInput.repaint();
            hitungProgresKep();
        });
        // Set kondisi awal sesuai pilihan default combo (index 0 = "Tidak
        // Hamil") - TIDAK lewat listener di atas krn panelKepDua/Tiga/Empat/
        // Obat belum dibuat di titik ini (baru dibuat di initKepTambahanHal2/
        // 3/4 & initObatInfusTambahan(), dipanggil setelah method ini).
        boolean hamilAwal = "Hamil".equals(StatusKehamilan.getSelectedItem());
        barisKehamilan.setVisible(hamilAwal);

        // -- Asesmen Psikologis --
        row = tambahBarisKepJudul(panelKepAtas, row, "ASESMEN PSIKOLOGIS");
        CekPsiTenang = new widget.CekBox();
        CekPsiTenang.setText("Tenang");
        CekPsiCemas = new widget.CekBox();
        CekPsiCemas.setText("Cemas");
        CekPsiTakut = new widget.CekBox();
        CekPsiTakut.setText("Takut");
        CekPsiSedih = new widget.CekBox();
        CekPsiSedih.setText("Sedih");
        CekPsiMarah = new widget.CekBox();
        CekPsiMarah.setText("Marah");
        CekPsiBunuhDiri = new widget.CekBox();
        CekPsiBunuhDiri.setText("Kecenderungan Bunuh Diri");
        // gridwidth REMAINDER, bukan 2, di SEBELAS blok yg ditempel langsung ke
        // panel induk tanpa lewat barisPengkajian(): dgn 2 mereka cuma membentang
        // sampai kolom 1 dan tepi kanannya ditentukan baris terlebar di panel yg
        // sama, jadi ikut bergerigi. Alasan lengkapnya di javadoc
        // barisPengkajian(). Dua pengecualian yg TIDAK diubah: cKpMasukRs dan
        // cKpDirujuk - keduanya di dalam sub-panel 2 kolom sendiri, di situ
        // gridwidth 2 memang berarti "pakai kedua kolom sub-panel ini".
        CekPsiMasalahPerilaku = new widget.CekBox();
        CekPsiMasalahPerilaku.setText("Masalah Perilaku");

        widget.PanelBiasa panelCekPsi = new widget.PanelBiasa();
        panelCekPsi.setOpaque(false);
        panelCekPsi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        panelCekPsi.add(CekPsiTenang);
        panelCekPsi.add(CekPsiCemas);
        panelCekPsi.add(CekPsiTakut);
        panelCekPsi.add(CekPsiSedih);
        panelCekPsi.add(CekPsiMarah);
        panelCekPsi.add(CekPsiBunuhDiri);
        panelCekPsi.add(CekPsiMasalahPerilaku);

        row = tambahBarisKepField(panelKepAtas, row, "Asesmen Psikologis", true, panelCekPsi);

        RbMasalahPrilakuTidak = new widget.RadioButton();
        RbMasalahPrilakuTidak.setText("Tidak");
        RbMasalahPrilakuYa = new widget.RadioButton();
        RbMasalahPrilakuYa.setText("Ada, Sebutkan");
        bgMasalahPrilaku = new javax.swing.ButtonGroup();
        bgMasalahPrilaku.add(RbMasalahPrilakuTidak);
        bgMasalahPrilaku.add(RbMasalahPrilakuYa);
        TMasalahPrilakuSebutkan = new widget.TextBox();
        TMasalahPrilakuSebutkan.setPreferredSize(new java.awt.Dimension(260, 23));
        TMasalahPrilakuSebutkan.putClientProperty("JTextField.placeholderText", "Sebutkan...");
        TMasalahPrilakuSebutkan.setVisible(false);

        widget.PanelBiasa pnlMasalahPrilaku = new widget.PanelBiasa();
        pnlMasalahPrilaku.setOpaque(false);
        pnlMasalahPrilaku.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        pnlMasalahPrilaku.add(RbMasalahPrilakuTidak);
        pnlMasalahPrilaku.add(RbMasalahPrilakuYa);
        pnlMasalahPrilaku.add(TMasalahPrilakuSebutkan);

        barisMasalahPrilaku = barisPengkajian(panelKepAtas, row, "Pilihan Masalah Perilaku", false, pnlMasalahPrilaku);
        barisMasalahPrilaku.setVisible(false);
        row++;

        CekPsiMasalahPerilaku.addItemListener((java.awt.event.ItemEvent e) -> {
            boolean aktif = CekPsiMasalahPerilaku.isSelected();
            if (barisMasalahPrilaku != null) {
                barisMasalahPrilaku.setVisible(aktif);
            }
            if (!aktif) {
                bgMasalahPrilaku.clearSelection();
                TMasalahPrilakuSebutkan.setVisible(false);
                TMasalahPrilakuSebutkan.setText("");
            } else {
                TMasalahPrilakuSebutkan.setVisible(RbMasalahPrilakuYa.isSelected());
            }
            if (barisMasalahPrilaku != null && barisMasalahPrilaku.getParent() != null) {
                barisMasalahPrilaku.getParent().revalidate();
                barisMasalahPrilaku.getParent().repaint();
            }
            FormInput.revalidate();
            FormInput.repaint();
            hitungProgresKep();
        });

        java.awt.event.ItemListener listenerMasalahPrilaku = (java.awt.event.ItemEvent e) -> {
            boolean ya = RbMasalahPrilakuYa.isSelected();
            TMasalahPrilakuSebutkan.setVisible(ya);
            if (!ya) {
                TMasalahPrilakuSebutkan.setText("");
            } else {
                TMasalahPrilakuSebutkan.requestFocusInWindow();
            }
            barisMasalahPrilaku.revalidate();
            barisMasalahPrilaku.repaint();
            FormInput.revalidate();
            FormInput.repaint();
            hitungProgresKep();
        };
        RbMasalahPrilakuTidak.addItemListener(listenerMasalahPrilaku);
        RbMasalahPrilakuYa.addItemListener(listenerMasalahPrilaku);

        // -- Status Sosial dan Ekonomis --
        row = tambahBarisKepJudul(panelKepAtas, row, "STATUS SOSIAL DAN EKONOMIS");
        RbNikahBelumMenikah = new widget.RadioButton();
        RbNikahBelumMenikah.setText("Belum Menikah");
        RbNikahMenikah = new widget.RadioButton();
        RbNikahMenikah.setText("Menikah");
        RbNikahCerai = new widget.RadioButton();
        RbNikahCerai.setText("Cerai");
        RbNikahLainnya = new widget.RadioButton();
        RbNikahLainnya.setText("Lainnya");
        bgStatusPernikahan = new javax.swing.ButtonGroup();
        bgStatusPernikahan.add(RbNikahBelumMenikah);
        bgStatusPernikahan.add(RbNikahMenikah);
        bgStatusPernikahan.add(RbNikahCerai);
        bgStatusPernikahan.add(RbNikahLainnya);
        TKetStatusPernikahan = new widget.TextBox();
        TKetStatusPernikahan.setPreferredSize(new java.awt.Dimension(150, 23));
        TKetStatusPernikahan.putClientProperty("JTextField.placeholderText", "Sebutkan...");
        TKetStatusPernikahan.setVisible(false);
        widget.PanelBiasa panelStatusNikah = new widget.PanelBiasa();
        panelStatusNikah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelStatusNikah.add(RbNikahBelumMenikah);
        panelStatusNikah.add(RbNikahMenikah);
        panelStatusNikah.add(RbNikahCerai);
        panelStatusNikah.add(RbNikahLainnya);
        panelStatusNikah.add(TKetStatusPernikahan);
        row = tambahBarisKepField(panelKepAtas, row, "Status Pernikahan", panelStatusNikah);

        java.awt.event.ItemListener listenerNikah = (java.awt.event.ItemEvent e) -> {
            boolean lainnya = RbNikahLainnya.isSelected();
            TKetStatusPernikahan.setVisible(lainnya);
            if (!lainnya) {
                TKetStatusPernikahan.setText("");
            } else {
                TKetStatusPernikahan.requestFocusInWindow();
            }
            panelStatusNikah.revalidate();
            panelStatusNikah.repaint();
            FormInput.revalidate();
            FormInput.repaint();
            hitungProgresKep();
        };
        RbNikahBelumMenikah.addItemListener(listenerNikah);
        RbNikahMenikah.addItemListener(listenerNikah);
        RbNikahCerai.addItemListener(listenerNikah);
        RbNikahLainnya.addItemListener(listenerNikah);

        RbTinggalSendiri = new widget.RadioButton();
        RbTinggalSendiri.setText("Rumah Sendiri");
        RbTinggalKeluarga = new widget.RadioButton();
        RbTinggalKeluarga.setText("Rumah Keluarganya");
        RbTinggalKeluarga.setIcon(null);
        RbTinggalKeluarga.setSelectedIcon(null);
        RbTinggalLainnya = new widget.RadioButton();
        RbTinggalLainnya.setText("Lainnya");
        bgTempatTinggal = new javax.swing.ButtonGroup();
        bgTempatTinggal.add(RbTinggalSendiri);
        bgTempatTinggal.add(RbTinggalKeluarga);
        bgTempatTinggal.add(RbTinggalLainnya);
        TKetTempatTinggal = new widget.TextBox();
        TKetTempatTinggal.setPreferredSize(new java.awt.Dimension(150, 23));
        TKetTempatTinggal.putClientProperty("JTextField.placeholderText", "Sebutkan...");
        TKetTempatTinggal.setVisible(false);
        widget.PanelBiasa panelTempatTinggal = new widget.PanelBiasa();
        panelTempatTinggal.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelTempatTinggal.add(RbTinggalSendiri);
        panelTempatTinggal.add(RbTinggalKeluarga);
        panelTempatTinggal.add(RbTinggalLainnya);
        panelTempatTinggal.add(TKetTempatTinggal);
        row = tambahBarisKepField(panelKepAtas, row, "Tempat Tinggal", panelTempatTinggal);

        java.awt.event.ItemListener listenerTinggal = (java.awt.event.ItemEvent e) -> {
            boolean lainnya = RbTinggalLainnya.isSelected();
            TKetTempatTinggal.setVisible(lainnya);
            if (!lainnya) {
                TKetTempatTinggal.setText("");
            } else {
                TKetTempatTinggal.requestFocusInWindow();
            }
            panelTempatTinggal.revalidate();
            panelTempatTinggal.repaint();
            FormInput.revalidate();
            FormInput.repaint();
            hitungProgresKep();
        };
        RbTinggalSendiri.addItemListener(listenerTinggal);
        RbTinggalKeluarga.addItemListener(listenerTinggal);
        RbTinggalLainnya.addItemListener(listenerTinggal);

        TPekerjaanKep = new widget.TextBox();
        TPekerjaanKep.setPreferredSize(new java.awt.Dimension(250, 23));
        row = tambahBarisKepField(panelKepAtas, row, "Pekerjaan", TPekerjaanKep);
        // Baris input "Pendidikan Pasien" dihapus dari tampilan (permintaan
        // user - tidak dipakai). TPendidikanPasienKep TETAP dibuat (bukan
        // dihapus totalj) krn masih dipakai getData()/simpan()/emptTeks()/
        // bangunRingkasanHtml() utk kolom DB pendidikan_pasien_kep - kalau
        // dihapus total akan NullPointerException di situ. Cukup tidak
        // ditambahkan ke panelKepAtas, jadi tidak ada baris/UI-nya lagi.
        TPendidikanPasienKep = new widget.TextBox();
        TPendidikanPasienKep.setPreferredSize(new java.awt.Dimension(250, 23));

        // -- Skrining Geriatri --
        row = tambahBarisKepJudul(panelKepAtas, row, "SKRINING GERIATRI");
        RbGeri1Ya = new widget.RadioButton();
        RbGeri1Ya.setText("Ya");
        RbGeri1Tidak = new widget.RadioButton();
        RbGeri1Tidak.setText("Tidak");
        bgGeri1 = new javax.swing.ButtonGroup();
        bgGeri1.add(RbGeri1Ya);
        bgGeri1.add(RbGeri1Tidak);
        widget.PanelBiasa panelGeri1 = new widget.PanelBiasa();
        panelGeri1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelGeri1.add(RbGeri1Ya);
        panelGeri1.add(RbGeri1Tidak);
        row = tambahBarisKepField(panelKepAtas, row, "≥60 Th dengan >1 Penyakit Fisik/Psikis", panelGeri1);

        RbGeri2Ya = new widget.RadioButton();
        RbGeri2Ya.setText("Ya");
        RbGeri2Tidak = new widget.RadioButton();
        RbGeri2Tidak.setText("Tidak");
        bgGeri2 = new javax.swing.ButtonGroup();
        bgGeri2.add(RbGeri2Ya);
        bgGeri2.add(RbGeri2Tidak);
        widget.PanelBiasa panelGeri2 = new widget.PanelBiasa();
        panelGeri2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelGeri2.add(RbGeri2Ya);
        panelGeri2.add(RbGeri2Tidak);
        row = tambahBarisKepField(panelKepAtas, row, "≥60 Th 1 Penyakit Fisik/Psikis + Penurunan Fungsi", panelGeri2);

        RbGeri3Ya = new widget.RadioButton();
        RbGeri3Ya.setText("Ya");
        RbGeri3Tidak = new widget.RadioButton();
        RbGeri3Tidak.setText("Tidak");
        bgGeri3 = new javax.swing.ButtonGroup();
        bgGeri3.add(RbGeri3Ya);
        bgGeri3.add(RbGeri3Tidak);
        widget.PanelBiasa panelGeri3 = new widget.PanelBiasa();
        panelGeri3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelGeri3.add(RbGeri3Ya);
        panelGeri3.add(RbGeri3Tidak);
        row = tambahBarisKepField(panelKepAtas, row, "≥70 Th dengan 1 Penyakit Fisik/Psikologis", panelGeri3);

        // -- Skrining Resiko Pasien Jatuh (Get Up and Go Test) --
        row = tambahBarisKepJudul(panelKepAtas, row, "SKRINING RESIKO PASIEN JATUH (GET UP AND GO TEST)");
        // Hasil dihitung otomatis (lihat hitungHasilJatuh()) - setiap
        // perubahan pilihan A/B memicu hitung ulang.
        java.awt.event.ActionListener alJatuh = (java.awt.event.ActionEvent e) -> hitungHasilJatuh();
        java.awt.event.ItemListener ilJatuh = (java.awt.event.ItemEvent e) -> hitungHasilJatuh();
        RbJatuhCaraBerjalanYa = new widget.RadioButton();
        RbJatuhCaraBerjalanYa.setText("Ya");
        RbJatuhCaraBerjalanTidak = new widget.RadioButton();
        RbJatuhCaraBerjalanTidak.setText("Tidak");
        bgJatuhCaraBerjalan = new javax.swing.ButtonGroup();
        bgJatuhCaraBerjalan.add(RbJatuhCaraBerjalanYa);
        bgJatuhCaraBerjalan.add(RbJatuhCaraBerjalanTidak);
        RbJatuhCaraBerjalanYa.addActionListener(alJatuh);
        RbJatuhCaraBerjalanTidak.addActionListener(alJatuh);
        RbJatuhCaraBerjalanYa.addItemListener(ilJatuh);
        RbJatuhCaraBerjalanTidak.addItemListener(ilJatuh);
        widget.PanelBiasa panelJatuhA = new widget.PanelBiasa();
        panelJatuhA.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelJatuhA.add(RbJatuhCaraBerjalanYa);
        panelJatuhA.add(RbJatuhCaraBerjalanTidak);
        row = tambahBarisKepField(panelKepAtas, row, "A. Cara Berjalan Tidak Seimbang/Pakai Alat Bantu",
                panelJatuhA);

        RbJatuhMenopangYa = new widget.RadioButton();
        RbJatuhMenopangYa.setText("Ya");
        RbJatuhMenopangTidak = new widget.RadioButton();
        RbJatuhMenopangTidak.setText("Tidak");
        bgJatuhMenopang = new javax.swing.ButtonGroup();
        bgJatuhMenopang.add(RbJatuhMenopangYa);
        bgJatuhMenopang.add(RbJatuhMenopangTidak);
        RbJatuhMenopangYa.addActionListener(alJatuh);
        RbJatuhMenopangTidak.addActionListener(alJatuh);
        RbJatuhMenopangYa.addItemListener(ilJatuh);
        RbJatuhMenopangTidak.addItemListener(ilJatuh);
        widget.PanelBiasa panelJatuhB = new widget.PanelBiasa();
        panelJatuhB.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelJatuhB.add(RbJatuhMenopangYa);
        panelJatuhB.add(RbJatuhMenopangTidak);
        row = tambahBarisKepField(panelKepAtas, row, "B. Menopang Saat Akan Duduk", panelJatuhB);

        RbJatuhTidakBeresiko = new widget.RadioButton();
        RbJatuhTidakBeresiko.setText("Tidak Beresiko");
        RbJatuhBeresiko = new widget.RadioButton();
        RbJatuhBeresiko.setText("Beresiko");
        RbJatuhTidakBeresiko.setFocusable(false);
        RbJatuhBeresiko.setFocusable(false);
        // Hasil otomatis: bila diklik pun tetap dipaksa sinkron dengan kriteria A & B
        RbJatuhTidakBeresiko.addActionListener(alJatuh);
        RbJatuhBeresiko.addActionListener(alJatuh);
        RbJatuhTidakBeresiko.setToolTipText("Otomatis dihitung dari kriteria A dan B");
        RbJatuhBeresiko.setToolTipText("Otomatis dihitung dari kriteria A dan B");
        bgJatuhHasil = new javax.swing.ButtonGroup();
        bgJatuhHasil.add(RbJatuhTidakBeresiko);
        bgJatuhHasil.add(RbJatuhBeresiko);
        widget.PanelBiasa panelJatuhHasil = new widget.PanelBiasa();
        panelJatuhHasil.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelJatuhHasil.add(RbJatuhTidakBeresiko);
        panelJatuhHasil.add(RbJatuhBeresiko);
        row = tambahBarisKepField(panelKepAtas, row, "Hasil (Tidak Beresiko bila A dan B tidak ditemukan)", true,
                panelJatuhHasil);

        // -- Pengkajian Spiritual, Kepercayaan, Fungsional dan Budaya --
        // Agama/Bahasa/Cacat Tubuh REUSE widget Agama/Bahasa/CacatFisik yang
        // sudah ada (dari registrasi pasien) - TIDAK diulang di sini.
        row = tambahBarisKepJudul(panelKepAtas, row, "PENGKAJIAN SPIRITUAL, KEPERCAYAAN, FUNGSIONAL DAN BUDAYA");
        // Agama diubah jadi input manual (kolom baru agama_manual) atas
        // permintaan user - sebelumnya reuse widget readonly dari
        // registrasi pasien. Widget lama Agama/Bahasa/CacatFisik
        // dipensiunkan (lihat retiredRiwayat di bawah) - "Bahasa
        // Sehari-hari" dihapus total dari UI (tak ada di foto fisik).
        TAgama = new widget.TextBox(); // Dibiarkan instance agar kompatibel
        CmbAgama = new widget.ComboBox();
        CmbAgama.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
            "-", "Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Konghucu"
        }));
        CmbAgama.setPreferredSize(new java.awt.Dimension(200, 23));
        row = tambahBarisKepField(panelKepAtas, row, "Agama", true, CmbAgama);

        RbWajibIbadahBaligh = new widget.RadioButton();
        RbWajibIbadahBaligh.setText("Baligh");
        RbWajibIbadahBelumBaligh = new widget.RadioButton();
        RbWajibIbadahBelumBaligh.setText("Belum Baligh");
        RbWajibIbadahHalanganLain = new widget.RadioButton();
        RbWajibIbadahHalanganLain.setText("Halangan Lain");
        bgWajibIbadah = new javax.swing.ButtonGroup();
        bgWajibIbadah.add(RbWajibIbadahBaligh);
        bgWajibIbadah.add(RbWajibIbadahBelumBaligh);
        bgWajibIbadah.add(RbWajibIbadahHalanganLain);
        TKetWajibIbadah = new widget.TextBox();
        TKetWajibIbadah.setPreferredSize(new java.awt.Dimension(150, 23));
        TKetWajibIbadah.putClientProperty("JTextField.placeholderText", "Sebutkan halangan...");
        TKetWajibIbadah.setVisible(false);
        widget.PanelBiasa panelWajibIbadah = new widget.PanelBiasa();
        panelWajibIbadah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelWajibIbadah.add(RbWajibIbadahBaligh);
        panelWajibIbadah.add(RbWajibIbadahBelumBaligh);
        panelWajibIbadah.add(RbWajibIbadahHalanganLain);
        panelWajibIbadah.add(TKetWajibIbadah);
        barisWajibIbadah = barisPengkajian(panelKepAtas, row, "Wajib Ibadah", false, panelWajibIbadah);
        barisWajibIbadah.setVisible(false);
        row++;

        java.awt.event.ItemListener listenerWajibIbadah = (java.awt.event.ItemEvent e) -> {
            boolean halangan = RbWajibIbadahHalanganLain.isSelected();
            TKetWajibIbadah.setVisible(halangan);
            if (!halangan) {
                TKetWajibIbadah.setText("");
            } else {
                TKetWajibIbadah.requestFocusInWindow();
            }
            panelWajibIbadah.revalidate();
            panelWajibIbadah.repaint();
            hitungProgresKep();
        };
        RbWajibIbadahBaligh.addItemListener(listenerWajibIbadah);
        RbWajibIbadahBelumBaligh.addItemListener(listenerWajibIbadah);
        RbWajibIbadahHalanganLain.addItemListener(listenerWajibIbadah);

        RbThaharohBerwudhu = new widget.RadioButton();
        RbThaharohBerwudhu.setText("Berwudhu");
        RbThaharohTayamum = new widget.RadioButton();
        RbThaharohTayamum.setText("Tayamum");
        bgThaharoh = new javax.swing.ButtonGroup();
        bgThaharoh.add(RbThaharohBerwudhu);
        bgThaharoh.add(RbThaharohTayamum);
        widget.PanelBiasa panelThaharoh = new widget.PanelBiasa();
        panelThaharoh.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelThaharoh.add(RbThaharohBerwudhu);
        panelThaharoh.add(RbThaharohTayamum);
        barisThaharoh = barisPengkajian(panelKepAtas, row, "Thaharoh", false, panelThaharoh);
        barisThaharoh.setVisible(false);
        row++;

        RbSholatBerdiri = new widget.RadioButton();
        RbSholatBerdiri.setText("Berdiri");
        RbSholatDuduk = new widget.RadioButton();
        RbSholatDuduk.setText("Duduk");
        RbSholatBerbaring = new widget.RadioButton();
        RbSholatBerbaring.setText("Berbaring");
        bgSholat = new javax.swing.ButtonGroup();
        bgSholat.add(RbSholatBerdiri);
        bgSholat.add(RbSholatDuduk);
        bgSholat.add(RbSholatBerbaring);
        widget.PanelBiasa panelSholat = new widget.PanelBiasa();
        panelSholat.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelSholat.add(RbSholatBerdiri);
        panelSholat.add(RbSholatDuduk);
        panelSholat.add(RbSholatBerbaring);
        barisSholat = barisPengkajian(panelKepAtas, row, "Sholat", false, panelSholat);
        barisSholat.setVisible(false);
        row++;

        CmbAgama.addItemListener((java.awt.event.ItemEvent e) -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                boolean islam = "Islam".equalsIgnoreCase(CmbAgama.getSelectedItem().toString());
                barisWajibIbadah.setVisible(islam);
                barisThaharoh.setVisible(islam);
                barisSholat.setVisible(islam);
                if (!islam) {
                    if (bgWajibIbadah != null) bgWajibIbadah.clearSelection();
                    if (TKetWajibIbadah != null) {
                        TKetWajibIbadah.setText("");
                        TKetWajibIbadah.setVisible(false);
                    }
                    if (bgThaharoh != null) bgThaharoh.clearSelection();
                    if (bgSholat != null) bgSholat.clearSelection();
                }
                if (barisWajibIbadah.getParent() != null) {
                    barisWajibIbadah.getParent().revalidate();
                    barisWajibIbadah.getParent().repaint();
                }
                FormInput.revalidate();
                FormInput.repaint();
                hitungProgresKep();
            }
        });

        RbNilaiKepercayaanTidak = new widget.RadioButton();
        RbNilaiKepercayaanTidak.setText("Tidak");
        RbNilaiKepercayaanYa = new widget.RadioButton();
        RbNilaiKepercayaanYa.setText("Ya");
        bgNilaiKepercayaan = new javax.swing.ButtonGroup();
        bgNilaiKepercayaan.add(RbNilaiKepercayaanTidak);
        bgNilaiKepercayaan.add(RbNilaiKepercayaanYa);
        TKetNilaiKepercayaan = new widget.TextBox();
        TKetNilaiKepercayaan.setPreferredSize(new java.awt.Dimension(200, 23));
        TKetNilaiKepercayaan.putClientProperty("JTextField.placeholderText", "Sebutkan nilai/kepercayaan...");
        TKetNilaiKepercayaan.setVisible(false);
        widget.PanelBiasa panelNilaiKepercayaan = new widget.PanelBiasa();
        panelNilaiKepercayaan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelNilaiKepercayaan.add(RbNilaiKepercayaanTidak);
        panelNilaiKepercayaan.add(RbNilaiKepercayaanYa);
        panelNilaiKepercayaan.add(TKetNilaiKepercayaan);
        row = tambahBarisKepField(panelKepAtas, row, "Nilai dan Kepercayaan Tertentu", panelNilaiKepercayaan);

        java.awt.event.ItemListener listenerNilaiKepercayaan = (java.awt.event.ItemEvent e) -> {
            boolean ya = RbNilaiKepercayaanYa.isSelected();
            TKetNilaiKepercayaan.setVisible(ya);
            if (!ya) {
                TKetNilaiKepercayaan.setText("");
            } else {
                TKetNilaiKepercayaan.requestFocusInWindow();
            }
            panelNilaiKepercayaan.revalidate();
            panelNilaiKepercayaan.repaint();
            FormInput.revalidate();
            FormInput.repaint();
            hitungProgresKep();
        };
        RbNilaiKepercayaanTidak.addItemListener(listenerNilaiKepercayaan);
        RbNilaiKepercayaanYa.addItemListener(listenerNilaiKepercayaan);

        // Hambatan Budaya/Bahasa (RbBudayaTidak, RbBudayaAda / KetBudaya)
        StatusBudaya.setVisible(false);
        RbBudayaTidak = new widget.RadioButton();
        RbBudayaTidak.setText("Tidak");
        RbBudayaAda = new widget.RadioButton();
        RbBudayaAda.setText("Ya");
        bgBudaya = new javax.swing.ButtonGroup();
        bgBudaya.add(RbBudayaTidak);
        bgBudaya.add(RbBudayaAda);
        KetBudaya.setPreferredSize(new java.awt.Dimension(200, 23));
        KetBudaya.putClientProperty("JTextField.placeholderText", "Sebutkan hambatan...");
        KetBudaya.setVisible(false);
        widget.PanelBiasa panelBudaya = new widget.PanelBiasa();
        panelBudaya.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelBudaya.add(RbBudayaTidak);
        panelBudaya.add(RbBudayaAda);
        panelBudaya.add(KetBudaya);
        row = tambahBarisKepField(panelKepAtas, row, "Hambatan Budaya / Bahasa", panelBudaya);

        java.awt.event.ItemListener listenerBudaya = (java.awt.event.ItemEvent e) -> {
            boolean ada = RbBudayaAda.isSelected();
            KetBudaya.setVisible(ada);
            if (!ada) {
                KetBudaya.setText("");
            } else {
                KetBudaya.requestFocusInWindow();
            }
            if (StatusBudaya != null) {
                StatusBudaya.setSelectedItem(ada ? "Ada" : "Tidak Ada");
            }
            panelBudaya.revalidate();
            panelBudaya.repaint();
            FormInput.revalidate();
            FormInput.repaint();
            hitungProgresKep();
        };
        RbBudayaTidak.addItemListener(listenerBudaya);
        RbBudayaAda.addItemListener(listenerBudaya);
        jLabel95.setVisible(false);
        // Cacat Tubuh/Disabilitas diubah jadi checkbox manual sesuai foto
        // fisik (permintaan user) - widget lama CacatFisik (readonly, auto
        // dari registrasi) dipensiunkan.
        CekCacatTidak = new widget.CekBox();
        CekCacatTidak.setText("Tidak");
        CekCacatTunaNetra = new widget.CekBox();
        CekCacatTunaNetra.setText("Tuna Netra");
        CekCacatTunaDaksa = new widget.CekBox();
        CekCacatTunaDaksa.setText("Tuna Daksa");
        CekCacatTunaRungu = new widget.CekBox();
        CekCacatTunaRungu.setText("Tuna Rungu");
        CekCacatTunaWicara = new widget.CekBox();
        CekCacatTunaWicara.setText("Tuna Wicara");
        row = tambahBarisKepField(panelKepAtas, row, "Cacat Tubuh / Disabilitas",
                panelChecklistKep(2, CekCacatTidak, CekCacatTunaNetra, CekCacatTunaDaksa, CekCacatTunaRungu,
                        CekCacatTunaWicara)); // 2 kolom (dari 5) - alasan sama spt checklist psikologis

        // -- Retire section/field lama yang tak ada padanan di foto fisik
        // RM 03-4/05/06 (setVisible(false), BUKAN dihapus - kolom DB tetap
        // ada & tetap dibaca/ditulis apa adanya oleh getData()/nilaiKep(),
        // additive-only konsisten pola sepanjang sesi ini). Field yang masih
        // dipakai di section aktif (StatusBudaya/KetBudaya/StatusKehamilan/
        // Gravida/Para/Abortus/HPHT/Lokasi/SkalaNyeri/PanelWall/Scroll8/
        // TabRencanaKeperawatan) TIDAK ada di sini - sudah direparent ke
        // section baru di atas/di initKepTambahanHal2/3(). Agama lama/
        // Bahasa/CacatFisik (readonly registrasi) TERMASUK yang
        // dipensiunkan di sini (lihat retiredRiwayat) - digantikan
        // TAgama (manual) & checkbox Cacat Tubuh, Bahasa dihapus total.
        jLabel30.setText("Keluhan Utama :");
        java.awt.Component[] retiredRiwayat = {
                jLabel9, scrollPane4, // Riwayat Penggunaan Obat (RPO)
                jLabel31, scrollPane2, // Riwayat Penyakit Dahulu (RPD)
                jLabel77, Agama, // Agama lama (readonly registrasi) - digantikan TAgama (manual)
                jLabel76, Bahasa, // Bahasa Sehari-hari - dihapus total, tak ada di foto fisik
                jLabel51, CacatFisik, // Cacat Tubuh lama (readonly registrasi) - digantikan checkbox
        };
        java.awt.Component[] retiredSectionII = {
                jLabel116, jSeparator12,
                jLabel90, Tekanan, jLabel91, Pupil, jLabel100, Neurosensorik,
                jLabel101, Integumen, jLabel102, Turgor, jLabel103, Edema, jLabel104, Mukosa,
                jLabel105, Perdarahan, jLabel36, JumlahPerdarahan, jLabel37, WarnaPerdarahan, jLabel38,
                jLabel106, Intoksikasi, jLabel107,
                jLabel108, KBAB, jLabel109, BAB, jLabel110, XBAB, jLabel111, WBAB,
                jLabel112, BAK, jLabel113, XBAK, jLabel114, WBAK, jLabel115, LBAK,
        };
        java.awt.Component[] retiredSectionIII = {
                jLabel94,
                jLabel117, Psikologis, jLabel119, Jiwa, jLabel120, StatusPernikahan,
                jLabel123, Perilaku, jLabel118, Dilaporkan, jLabel121, Sebutkan,
                jLabel122, Hubungan,
                jLabel124, Pekerjaan, jLabel125, Pembayaran,
                jLabel126, PendidikanPasien,
                jLabel60, TinggalDengan, KetTinggal,
                jLabel58, Edukasi, KetEdukasi,
                jLabel127, PendidikanPJ, KetPendidikanPJ,
        };
        java.awt.Component[] retiredSectionIV = {
                jLabel56, jSeparator4,
                jLabel55, Aktifitas, jLabel57, ADL,
                jLabel50, AlatBantu, KetAlatBantu,
        };
        java.awt.Component[] retiredSectionV = {
                jLabel52, jSeparator8, jSeparator9,
                Nyeri,
                jLabel80, Provokes, KetProvokes,
                jLabel81, Quality, KetQuality,
                jLabel82, jLabel67, Menyebar,
                jLabel87, jLabel84, Durasi,
                jLabel89, NyeriHilang, KetNyeri,
                jLabel86, PadaDokter, KetDokter,
                jLabel88,
        };
        java.awt.Component[] retiredSectionVI = {
                jLabel63, jSeparator6, jSeparator10,
                jLabel72, jLabel65, ATS, jLabel70, jLabel66, BJM,
                jLabel68, Hasil, MSA,
                jLabel97, Lapor, jLabel71, KetLapor, jLabel64,
        };
        for (java.awt.Component c : retiredRiwayat)
            c.setVisible(false);
        for (java.awt.Component c : retiredSectionII)
            c.setVisible(false);
        for (java.awt.Component c : retiredSectionIII)
            c.setVisible(false);
        for (java.awt.Component c : retiredSectionIV)
            c.setVisible(false);
        for (java.awt.Component c : retiredSectionV)
            c.setVisible(false);
        for (java.awt.Component c : retiredSectionVI)
            c.setVisible(false);

        // Penempatan panelKepAtas ditangani susunHalamanKep(): isinya dipecah
        // per seksi oleh pecahSeksiKep(), lalu tiap seksi masuk ke kartu
        // halamannya sendiri. Y-math null-layout lama (geser komponen y>=210 via
        // FormInput.getComponents(), setBounds, lebarFormInput, yBawahLama,
        // FormInput.setPreferredSize) tidak dipakai lagi - GridBagLayout kartu
        // yg mengatur posisi & tinggi otomatis.
    }

    /**
     * [RM 03-5] Membangun section baru HALAMAN 2 formulir fisik: Skrining
     * Gizi Dewasa (MST), Skrining Gizi Anak, dan toggle Nyeri Ya/Tidak
     * (field baru `nyeri_ada` - `Lokasi`/`SkalaNyeri` widget LAMA TIDAK
     * dipindah dari posisi aslinya, tetap dipakai apa adanya dari sana).
     * Ditempel di bawah panelKepAtas.
     */
    private void initPilihanGizi() {
        RbPilihGiziDewasa = new widget.RadioButton();
        RbPilihGiziDewasa.setText("Skrining Gizi Dewasa (MST)");
        RbPilihGiziAnak = new widget.RadioButton();
        RbPilihGiziAnak.setText("Skrining Gizi Anak (Usia 0-14 Tahun)");
        bgPilihGizi = new javax.swing.ButtonGroup();
        bgPilihGizi.add(RbPilihGiziDewasa);
        bgPilihGizi.add(RbPilihGiziAnak);

        RbPilihGiziDewasa.setSelected(true);
        java.awt.event.ItemListener il = (java.awt.event.ItemEvent e) -> {
            updateVisibilitasGizi();
        };
        RbPilihGiziDewasa.addItemListener(il);
        RbPilihGiziAnak.addItemListener(il);
    }

    private widget.PanelBiasa buatPanelRadioPilihanGizi() {
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(false);
        p.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        p.add(RbPilihGiziDewasa);
        p.add(RbPilihGiziAnak);
        return p;
    }

    private void updateVisibilitasGizi() {
        boolean dewasa = RbPilihGiziDewasa != null && RbPilihGiziDewasa.isSelected();
        boolean anak = RbPilihGiziAnak != null && RbPilihGiziAnak.isSelected();

        if (panelSeksiGiziDewasa != null) {
            panelSeksiGiziDewasa.setVisible(dewasa);
        }
        if (panelSeksiGiziAnak != null) {
            panelSeksiGiziAnak.setVisible(anak);
        }

        if (halamanKep != null && halamanKep.size() > 5 && halamanKep.get(5).panel != null) {
            halamanKep.get(5).panel.revalidate();
            halamanKep.get(5).panel.repaint();
        }
        if (FormInput != null) {
            FormInput.revalidate();
            FormInput.repaint();
        }
        hitungProgresKep();
    }

    private void initKepTambahanHal2() {
        initPilihanGizi();
        panelKepDua = new widget.PanelBiasa();
        panelKepDua.setLayout(new java.awt.GridBagLayout());
        int row = 0;

        // -- Skrining Gizi Dewasa (Malnutrition Screening Tool / MST) --
        row = tambahBarisKepJudul(panelKepDua, row, "SKRINING GIZI DEWASA (MST)");
        RbMstTidakAda = new widget.RadioButton();
        RbMstTidakAda.setText("Tidak Ada Penurunan BB");
        RbMstTidakYakin = new widget.RadioButton();
        RbMstTidakYakin.setText("Tidak Yakin/Baju Longgar");
        RbMstYa = new widget.RadioButton();
        RbMstYa.setText("Ya, Ada Penurunan BB");
        bgMstPenurunanBb = new javax.swing.ButtonGroup();
        bgMstPenurunanBb.add(RbMstTidakAda);
        bgMstPenurunanBb.add(RbMstTidakYakin);
        bgMstPenurunanBb.add(RbMstYa);
        // Total Skor MST dihitung otomatis (lihat hitungTotalMst()) - setiap
        // perubahan pilihan yg mempengaruhi skor memicu hitung ulang.
        RbMstTidakAda.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        RbMstTidakYakin.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        RbMstYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        widget.PanelBiasa panelMstPenurunan = new widget.PanelBiasa();
        panelMstPenurunan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelMstPenurunan.add(RbMstTidakAda);
        panelMstPenurunan.add(RbMstTidakYakin);
        panelMstPenurunan.add(RbMstYa);
        row = tambahBarisKepField(panelKepDua, row, "Penurunan BB 6 Bulan Terakhir", true, panelMstPenurunan);

        MstPenurunanBbKg = new widget.ComboBox();
        MstPenurunanBbKg.setModel(
                new javax.swing.DefaultComboBoxModel(new String[] { "", "1-5 kg", "6-10 kg", "11-15 kg", ">15 kg" }));
        MstPenurunanBbKg.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        row = tambahBarisKepField(panelKepDua, row, "Jumlah Penurunan (kalau Ya)", MstPenurunanBbKg);

        RbMstAsupanYa = new widget.RadioButton();
        RbMstAsupanYa.setText("Ya");
        RbMstAsupanTidak = new widget.RadioButton();
        RbMstAsupanTidak.setText("Tidak");
        bgMstAsupan = new javax.swing.ButtonGroup();
        bgMstAsupan.add(RbMstAsupanYa);
        bgMstAsupan.add(RbMstAsupanTidak);
        RbMstAsupanYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        RbMstAsupanTidak.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        widget.PanelBiasa panelMstAsupan = new widget.PanelBiasa();
        panelMstAsupan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelMstAsupan.add(RbMstAsupanYa);
        panelMstAsupan.add(RbMstAsupanTidak);
        row = tambahBarisKepField(panelKepDua, row, "Asupan Makan Berkurang (Tidak Nafsu Makan)", true, panelMstAsupan);

        TMstTotalSkor = new widget.TextBox();
        TMstTotalSkor.setPreferredSize(new java.awt.Dimension(60, 23));
        TMstTotalSkor.setEditable(false);
        row = tambahBarisKepField(panelKepDua, row, "Total Skor", TMstTotalSkor);

        RbMstDiagnosisYa = new widget.RadioButton();
        RbMstDiagnosisYa.setText("Ya");
        RbMstDiagnosisTidak = new widget.RadioButton();
        RbMstDiagnosisTidak.setText("Tidak");
        bgMstDiagnosis = new javax.swing.ButtonGroup();
        bgMstDiagnosis.add(RbMstDiagnosisYa);
        bgMstDiagnosis.add(RbMstDiagnosisTidak);
        TMstDiagnosisSebutkan = new widget.TextBox();
        TMstDiagnosisSebutkan.setPreferredSize(new java.awt.Dimension(280, 26));
        TMstDiagnosisSebutkan.putClientProperty("JTextField.placeholderText", "Sebutkan diagnosis khusus...");
        TMstDiagnosisSebutkan.setVisible(false);

        final widget.PanelBiasa panelMstDiagnosis = new widget.PanelBiasa();
        panelMstDiagnosis.setOpaque(false);
        panelMstDiagnosis.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
        panelMstDiagnosis.add(RbMstDiagnosisYa);
        panelMstDiagnosis.add(RbMstDiagnosisTidak);
        panelMstDiagnosis.add(TMstDiagnosisSebutkan);

        java.awt.event.ItemListener listenerMstDiagnosis = (java.awt.event.ItemEvent e) -> {
            boolean ya = RbMstDiagnosisYa.isSelected();
            TMstDiagnosisSebutkan.setVisible(ya);
            if (!ya) {
                TMstDiagnosisSebutkan.setText("");
            }
            panelMstDiagnosis.revalidate();
            panelMstDiagnosis.repaint();
            hitungTotalMst();
            hitungProgresKep();
        };
        RbMstDiagnosisYa.addItemListener(listenerMstDiagnosis);
        RbMstDiagnosisTidak.addItemListener(listenerMstDiagnosis);

        row = tambahBarisKepField(panelKepDua, row, "Pasien Diagnosis Khusus (DM/Kemoterapi/dst)", panelMstDiagnosis);

        RbMstRisiko = new widget.RadioButton();
        RbMstRisiko.setText("Risiko");
        RbMstTidakRisiko = new widget.RadioButton();
        RbMstTidakRisiko.setText("Tidak Risiko");
        bgMstKesimpulan = new javax.swing.ButtonGroup();
        bgMstKesimpulan.add(RbMstRisiko);
        bgMstKesimpulan.add(RbMstTidakRisiko);
        widget.PanelBiasa panelMstKesimpulan = new widget.PanelBiasa();
        panelMstKesimpulan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelMstKesimpulan.add(RbMstRisiko);
        panelMstKesimpulan.add(RbMstTidakRisiko);
        row = tambahBarisKepField(panelKepDua, row, "Kesimpulan", true, panelMstKesimpulan);

        // -- Skrining Gizi Anak (Usia 0-14 tahun) --
        row = tambahBarisKepJudul(panelKepDua, row, "SKRINING GIZI ANAK (USIA 0-14 TAHUN)");
        RbGiziAnakKurusYa = new widget.RadioButton();
        RbGiziAnakKurusYa.setText("Ya");
        RbGiziAnakKurusTidak = new widget.RadioButton();
        RbGiziAnakKurusTidak.setText("Tidak");
        bgGiziAnakKurus = new javax.swing.ButtonGroup();
        bgGiziAnakKurus.add(RbGiziAnakKurusYa);
        bgGiziAnakKurus.add(RbGiziAnakKurusTidak);
        RbGiziAnakKurusYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        RbGiziAnakKurusTidak.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        widget.PanelBiasa panelGiziKurus = new widget.PanelBiasa();
        panelGiziKurus.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelGiziKurus.add(RbGiziAnakKurusYa);
        panelGiziKurus.add(RbGiziAnakKurusTidak);
        row = tambahBarisKepField(panelKepDua, row, "Pasien Tampak Kurus", panelGiziKurus);

        RbGiziAnakTurunBbYa = new widget.RadioButton();
        RbGiziAnakTurunBbYa.setText("Ya");
        RbGiziAnakTurunBbTidak = new widget.RadioButton();
        RbGiziAnakTurunBbTidak.setText("Tidak");
        bgGiziAnakTurunBb = new javax.swing.ButtonGroup();
        bgGiziAnakTurunBb.add(RbGiziAnakTurunBbYa);
        bgGiziAnakTurunBb.add(RbGiziAnakTurunBbTidak);
        RbGiziAnakTurunBbYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        RbGiziAnakTurunBbTidak.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        widget.PanelBiasa panelGiziTurunBb = new widget.PanelBiasa();
        panelGiziTurunBb.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelGiziTurunBb.add(RbGiziAnakTurunBbYa);
        panelGiziTurunBb.add(RbGiziAnakTurunBbTidak);
        row = tambahBarisKepField(panelKepDua, row, "Penurunan BB Sebulan Terakhir", panelGiziTurunBb);

        RbGiziAnakKondisiYa = new widget.RadioButton();
        RbGiziAnakKondisiYa.setText("Ya");
        RbGiziAnakKondisiTidak = new widget.RadioButton();
        RbGiziAnakKondisiTidak.setText("Tidak");
        bgGiziAnakKondisi = new javax.swing.ButtonGroup();
        bgGiziAnakKondisi.add(RbGiziAnakKondisiYa);
        bgGiziAnakKondisi.add(RbGiziAnakKondisiTidak);
        RbGiziAnakKondisiYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        RbGiziAnakKondisiTidak.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        widget.PanelBiasa panelGiziKondisi = new widget.PanelBiasa();
        panelGiziKondisi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelGiziKondisi.add(RbGiziAnakKondisiYa);
        panelGiziKondisi.add(RbGiziAnakKondisiTidak);
        row = tambahBarisKepField(panelKepDua, row, "Diare ≥5x/hari atau Muntah >3x/hari (Seminggu Terakhir)",
                panelGiziKondisi);

        RbGiziAnakRisikoYa = new widget.RadioButton();
        RbGiziAnakRisikoYa.setText("Ya");
        RbGiziAnakRisikoTidak = new widget.RadioButton();
        RbGiziAnakRisikoTidak.setText("Tidak");
        bgGiziAnakRisiko = new javax.swing.ButtonGroup();
        bgGiziAnakRisiko.add(RbGiziAnakRisikoYa);
        bgGiziAnakRisiko.add(RbGiziAnakRisikoTidak);
        RbGiziAnakRisikoYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        RbGiziAnakRisikoTidak.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        widget.PanelBiasa panelGiziRisiko = new widget.PanelBiasa();
        panelGiziRisiko.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelGiziRisiko.add(RbGiziAnakRisikoYa);
        panelGiziRisiko.add(RbGiziAnakRisikoTidak);
        row = tambahBarisKepField(panelKepDua, row, "Ada Penyakit/Keadaan Risiko Malnutrisi", panelGiziRisiko);

        TGiziAnakTotalSkor = new widget.TextBox();
        TGiziAnakTotalSkor.setPreferredSize(new java.awt.Dimension(60, 23));
        TGiziAnakTotalSkor.setEditable(false);
        row = tambahBarisKepField(panelKepDua, row, "Total Skor", TGiziAnakTotalSkor);

        RbGiziAnakRendah = new widget.RadioButton();
        RbGiziAnakRendah.setText("Risiko Rendah");
        RbGiziAnakSedang = new widget.RadioButton();
        RbGiziAnakSedang.setText("Risiko Sedang");
        RbGiziAnakTinggi = new widget.RadioButton();
        RbGiziAnakTinggi.setText("Risiko Tinggi");
        bgGiziAnakKesimpulan = new javax.swing.ButtonGroup();
        bgGiziAnakKesimpulan.add(RbGiziAnakRendah);
        bgGiziAnakKesimpulan.add(RbGiziAnakSedang);
        bgGiziAnakKesimpulan.add(RbGiziAnakTinggi);
        widget.PanelBiasa panelGiziKesimpulan = new widget.PanelBiasa();
        panelGiziKesimpulan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelGiziKesimpulan.add(RbGiziAnakRendah);
        panelGiziKesimpulan.add(RbGiziAnakSedang);
        panelGiziKesimpulan.add(RbGiziAnakTinggi);
        row = tambahBarisKepField(panelKepDua, row, "Kesimpulan", panelGiziKesimpulan);

        // -- Asesmen Nyeri --
        row = tambahBarisKepJudul(panelKepDua, row, "ASESMEN NYERI");
        RbNyeriAdaYa = new widget.RadioButton();
        RbNyeriAdaYa.setText("Ya");
        RbNyeriAdaTidak = new widget.RadioButton();
        RbNyeriAdaTidak.setText("Tidak");
        bgNyeriAda = new javax.swing.ButtonGroup();
        bgNyeriAda.add(RbNyeriAdaYa);
        bgNyeriAda.add(RbNyeriAdaTidak);
        widget.PanelBiasa panelNyeriAda = new widget.PanelBiasa();
        panelNyeriAda.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelNyeriAda.add(RbNyeriAdaYa);
        panelNyeriAda.add(RbNyeriAdaTidak);
        row = tambahBarisKepField(panelKepDua, row, "Nyeri", true, panelNyeriAda);

        // -- Kategori Usia Nyeri (kondisional) --
        CmbKategoriUsiaNyeri = new widget.ComboBox();
        CmbKategoriUsiaNyeri.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
                "", "Bayi - Anak < 3 Tahun", "Usia 3-8 Tahun", "Usia > 8 Tahun" }));
        CmbKategoriUsiaNyeri.setPreferredSize(new java.awt.Dimension(220, 23));
        CmbKategoriUsiaNyeri.setEnabled(false);
        row = tambahBarisKepField(panelKepDua, row, "Kategori Usia Nyeri", true, CmbKategoriUsiaNyeri);

        jLabel83.setVisible(false);
        jLabel85.setVisible(false);
        Lokasi.setPreferredSize(new java.awt.Dimension(220, 23));

        // ================================================================
        // PANEL SEKSI SKALA NYERI BERSAMA (Usia 3-8 Tahun & Usia > 8 Tahun)
        // Memuat Lokasi & Toggle Button Skala Nyeri (0–10)
        // ================================================================
        panelSeksiNyeriSkalaCommon = new widget.PanelBiasa();
        panelSeksiNyeriSkalaCommon.setLayout(new java.awt.GridBagLayout());
        panelSeksiNyeriSkalaCommon.setVisible(false);
        int rowSkala = 0;

        // Lokasi
        rowSkala = tambahBarisKepField(panelSeksiNyeriSkalaCommon, rowSkala, "Lokasi", Lokasi);

        // Toggle button Skala Nyeri 0-10
        SkalaNyeri.setVisible(false);
        panelSkalaNyeriToggle = new widget.PanelBiasa();
        panelSkalaNyeriToggle.setOpaque(false);
        panelSkalaNyeriToggle.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 3, 0));
        btnGroupSkalaNyeri = new javax.swing.ButtonGroup();
        btnSkalaNyeri = new javax.swing.JToggleButton[11];
        for (int i = 0; i <= 10; i++) {
            final int skor = i;
            final javax.swing.JToggleButton btn = new javax.swing.JToggleButton(String.valueOf(i));
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            btn.setPreferredSize(new java.awt.Dimension(30, 30));
            btn.setFocusPainted(false);
            btn.setMargin(new java.awt.Insets(0, 0, 0, 0));
            btn.addActionListener((java.awt.event.ActionEvent e) -> {
                SkalaNyeri.setSelectedItem(String.valueOf(skor));
                updateGayaSkalaNyeri();
                hitungProgresKep();
            });
            btnGroupSkalaNyeri.add(btn);
            panelSkalaNyeriToggle.add(btn);
            btnSkalaNyeri[i] = btn;
        }

        lblInterpretasiNyeri = new widget.Label();
        lblInterpretasiNyeri.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        lblInterpretasiNyeri.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 0));
        panelSkalaNyeriToggle.add(lblInterpretasiNyeri);

        SkalaNyeri.addItemListener((java.awt.event.ItemEvent e) -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                String val = SkalaNyeri.getSelectedItem() != null ? SkalaNyeri.getSelectedItem().toString() : "";
                try {
                    int skor = Integer.parseInt(val.trim());
                    if (skor >= 0 && skor <= 10 && btnSkalaNyeri != null && btnSkalaNyeri[skor] != null) {
                        btnSkalaNyeri[skor].setSelected(true);
                    }
                } catch (Exception ex) {
                }
                updateGayaSkalaNyeri();
                hitungProgresKep();
            }
        });
        updateGayaSkalaNyeri();

        rowSkala = tambahBarisKepField(panelSeksiNyeriSkalaCommon, rowSkala, "Skala Nyeri (0\u201310)", true, panelSkalaNyeriToggle);

        // Tambahkan panelSeksiNyeriSkalaCommon ke panelKepDua
        java.awt.GridBagConstraints cNyeriSkala = kepConstraint(0, row);
        cNyeriSkala.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cNyeriSkala.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cNyeriSkala.insets = new java.awt.Insets(0, 5, 0, 5);
        panelKepDua.add(panelSeksiNyeriSkalaCommon, cNyeriSkala);
        row++;

        // ================================================================
        // PANEL SEKSI NYERI WONG-BAKER (Usia 3-8 Tahun)
        // ================================================================
        panelSeksiNyeriWongBaker = new widget.PanelBiasa();
        panelSeksiNyeriWongBaker.setLayout(new java.awt.GridBagLayout());
        panelSeksiNyeriWongBaker.setVisible(false);
        int rowWb = 0;

        // Gambar Wong-Baker Faces
        widget.PanelBiasa panelGambarWongBaker = new widget.PanelBiasa();
        panelGambarWongBaker.setOpaque(true);
        panelGambarWongBaker.setBackground(java.awt.Color.WHITE);
        panelGambarWongBaker.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6), 1),
                javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        panelGambarWongBaker.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        final javax.swing.JLabel lblGambarWongBaker = new javax.swing.JLabel();
        lblGambarWongBaker.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblGambarWongBaker.setToolTipText("Klik pada gambar wajah untuk memilih skala nyeri (0, 2, 4, 6, 8, 10)");
        try {
            java.awt.image.BufferedImage imgWb = javax.imageio.ImageIO
                    .read(getClass().getResource("/picture/nyeri_wongbaker.jpg"));
            if (imgWb != null) {
                int targetW = 680;
                int targetH = (int) Math.round((double) imgWb.getHeight() * targetW / imgWb.getWidth());
                java.awt.Image scaled = imgWb.getScaledInstance(targetW, targetH, java.awt.Image.SCALE_SMOOTH);
                lblGambarWongBaker.setIcon(new javax.swing.ImageIcon(scaled));
            }
        } catch (Exception e) {
            try {
                lblGambarWongBaker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/nyeri_wongbaker.jpg")));
            } catch (Exception ex) {
            }
        }

        lblGambarWongBaker.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int x = e.getX();
                int w = lblGambarWongBaker.getWidth();
                if (w <= 0) w = 468;
                double fraksi = (double) x / w;
                int skorDipilih = 0;
                if (fraksi < 0.16) {
                    skorDipilih = 0;
                } else if (fraksi < 0.33) {
                    skorDipilih = 2;
                } else if (fraksi < 0.50) {
                    skorDipilih = 4;
                } else if (fraksi < 0.67) {
                    skorDipilih = 6;
                } else if (fraksi < 0.84) {
                    skorDipilih = 8;
                } else {
                    skorDipilih = 10;
                }
                SkalaNyeri.setSelectedItem(String.valueOf(skorDipilih));
                if (btnSkalaNyeri != null && skorDipilih >= 0 && skorDipilih < btnSkalaNyeri.length
                        && btnSkalaNyeri[skorDipilih] != null) {
                    btnSkalaNyeri[skorDipilih].setSelected(true);
                }
                updateGayaSkalaNyeri();
                hitungProgresKep();
            }
        });
        panelGambarWongBaker.add(lblGambarWongBaker);

        rowWb = tambahBarisKepField(panelSeksiNyeriWongBaker, rowWb, "Wong-Baker FACES\u00AE Pain Rating Scale", panelGambarWongBaker);

        // Tambahkan panelSeksiNyeriWongBaker ke panelKepDua
        java.awt.GridBagConstraints cNyeriWb = kepConstraint(0, row);
        cNyeriWb.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cNyeriWb.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cNyeriWb.insets = new java.awt.Insets(0, 5, 0, 5);
        panelKepDua.add(panelSeksiNyeriWongBaker, cNyeriWb);
        row++;

        // ================================================================
        // PANEL SEKSI NYERI NUMERIC (Usia > 8 Tahun)
        // ================================================================
        panelSeksiNyeriNumeric = new widget.PanelBiasa();
        panelSeksiNyeriNumeric.setLayout(new java.awt.GridBagLayout());
        panelSeksiNyeriNumeric.setVisible(false);
        int rowNum = 0;

        // Gambar Panduan Numeric Rating Scale (gambar baru)
        widget.PanelBiasa panelGambarNumeric = new widget.PanelBiasa();
        panelGambarNumeric.setOpaque(true);
        panelGambarNumeric.setBackground(java.awt.Color.WHITE);
        panelGambarNumeric.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6), 1),
                javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        panelGambarNumeric.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        final javax.swing.JLabel lblGambarNumeric = new javax.swing.JLabel();
        lblGambarNumeric.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblGambarNumeric.setToolTipText("Klik pada gambar untuk memilih skala nyeri");
        try {
            java.awt.image.BufferedImage imgNum = javax.imageio.ImageIO
                    .read(getClass().getResource("/picture/nyeri_numeric.jpg"));
            if (imgNum != null) {
                int targetW = 680;
                int targetH = (int) Math.round((double) imgNum.getHeight() * targetW / imgNum.getWidth());
                java.awt.Image scaled = imgNum.getScaledInstance(targetW, targetH, java.awt.Image.SCALE_SMOOTH);
                lblGambarNumeric.setIcon(new javax.swing.ImageIcon(scaled));
            }
        } catch (Exception e) {
            try {
                lblGambarNumeric.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/nyeri_numeric.jpg")));
            } catch (Exception ex) {
            }
        }

        lblGambarNumeric.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int x = e.getX();
                int w = lblGambarNumeric.getWidth();
                if (w <= 0) w = 600;
                double fraksi = (double) x / w;
                int skorDipilih = (int) Math.round(fraksi * 10);
                if (skorDipilih < 0) skorDipilih = 0;
                if (skorDipilih > 10) skorDipilih = 10;
                SkalaNyeri.setSelectedItem(String.valueOf(skorDipilih));
                if (btnSkalaNyeri != null && skorDipilih >= 0 && skorDipilih < btnSkalaNyeri.length
                        && btnSkalaNyeri[skorDipilih] != null) {
                    btnSkalaNyeri[skorDipilih].setSelected(true);
                }
                updateGayaSkalaNyeri();
                hitungProgresKep();
            }
        });
        panelGambarNumeric.add(lblGambarNumeric);
        rowNum = tambahBarisKepField(panelSeksiNyeriNumeric, rowNum, "Panduan Skala Nyeri", panelGambarNumeric);

        // Tambahkan panelSeksiNyeriNumeric ke panelKepDua
        java.awt.GridBagConstraints cNyeriNumeric = kepConstraint(0, row);
        cNyeriNumeric.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cNyeriNumeric.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cNyeriNumeric.insets = new java.awt.Insets(0, 5, 0, 5);
        panelKepDua.add(panelSeksiNyeriNumeric, cNyeriNumeric);
        row++;

        // ================================================================
        // PANEL SEKSI FLACC (Bayi - Anak < 3 Tahun)
        // ================================================================
        panelSeksiNyeriFlacc = new widget.PanelBiasa();
        panelSeksiNyeriFlacc.setLayout(new java.awt.GridBagLayout());
        panelSeksiNyeriFlacc.setVisible(false);
        int rowFl = 0;

        java.awt.GridBagConstraints cFlaccHdr = kepConstraint(0, rowFl);
        cFlaccHdr.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cFlaccHdr.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cFlaccHdr.weightx = 1.0;
        cFlaccHdr.insets = new java.awt.Insets(10, 5, 3, 5);
        panelSeksiNyeriFlacc.add(buatHeaderStripKep("FLACC (Face, Legs, Activity, Cry, Consolability) - Bayi/Anak <3 Tahun"), cFlaccHdr);
        rowFl++;
        String[] flaccWajahItems = new String[] { "", "Tersenyum / tak ada ekspresi", "Terkadang menangis",
                "Sering menggetarkan dagu" };
        String[] flaccKakiItems = new String[] { "", "Gerakan normal / relaksasi", "Tegang, cemas",
                "Kaki menendang, menarik" };
        String[] flaccAktivitasItems = new String[] { "", "Tidak menangis", "Merengek", "Menangis terus, menjerit" };
        String[] flaccMenangisItems = new String[] { "", "Tidak menangis", "Merengek", "Menangis terus, menjerit" };
        String[] flaccBersuaraItems = new String[] { "", "Bersuara normal", "Tenang bila dipeluk",
                "Sulit untuk menenangkan" };

        java.awt.event.ActionListener alFlacc = (java.awt.event.ActionEvent e) -> hitungTotalFlacc();
        FlaccWajah = new widget.ComboBox();
        FlaccWajah.setModel(new javax.swing.DefaultComboBoxModel(flaccWajahItems));
        FlaccWajah.setPreferredSize(new java.awt.Dimension(175, 26));
        FlaccWajah.addActionListener(alFlacc);
        FlaccKaki = new widget.ComboBox();
        FlaccKaki.setModel(new javax.swing.DefaultComboBoxModel(flaccKakiItems));
        FlaccKaki.setPreferredSize(new java.awt.Dimension(175, 26));
        FlaccKaki.addActionListener(alFlacc);
        FlaccAktivitas = new widget.ComboBox();
        FlaccAktivitas.setModel(new javax.swing.DefaultComboBoxModel(flaccAktivitasItems));
        FlaccAktivitas.setPreferredSize(new java.awt.Dimension(175, 26));
        FlaccAktivitas.addActionListener(alFlacc);
        FlaccMenangis = new widget.ComboBox();
        FlaccMenangis.setModel(new javax.swing.DefaultComboBoxModel(flaccMenangisItems));
        FlaccMenangis.setPreferredSize(new java.awt.Dimension(175, 26));
        FlaccMenangis.addActionListener(alFlacc);
        FlaccBersuara = new widget.ComboBox();
        FlaccBersuara.setModel(new javax.swing.DefaultComboBoxModel(flaccBersuaraItems));
        FlaccBersuara.setPreferredSize(new java.awt.Dimension(175, 26));
        FlaccBersuara.addActionListener(alFlacc);

        TFlaccTotal = new widget.TextBox();
        TFlaccTotal.setPreferredSize(new java.awt.Dimension(80, 26));
        TFlaccTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TFlaccTotal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        TFlaccTotal.setEditable(false);
        TFlaccTotal.setBackground(java.awt.Color.WHITE);
        TFlaccTotal.setOpaque(true);
        TFlaccTotal.putClientProperty("FlatLaf.style",
                "background: #FFFFFF; inactiveBackground: #FFFFFF; disabledBackground: #FFFFFF;");
        TFlaccTotal.setBorder(BORDER_KOTAK_TEXTFIELD);

        widget.PanelBiasa panelFlacc = new widget.PanelBiasa();
        panelFlacc.setOpaque(false);
        panelFlacc.setLayout(new java.awt.GridBagLayout());
        String[] labelFlaccKep = { "Wajah", "Kaki", "Aktivitas", "Menangis", "Bersuara", "Total Skor" };
        java.awt.Component[] isiFlaccKep = { FlaccWajah, FlaccKaki, FlaccAktivitas,
                FlaccMenangis, FlaccBersuara, TFlaccTotal };
        for (int i = 0; i < isiFlaccKep.length; i++) {
            tambahSelKepField(panelFlacc, i, 0, labelFlaccKep[i], isiFlaccKep[i]);
        }

        java.awt.GridBagConstraints cFillerFlacc = kepConstraint(isiFlaccKep.length * 2, 0);
        cFillerFlacc.weightx = 1.0;
        cFillerFlacc.fill = java.awt.GridBagConstraints.BOTH;
        cFillerFlacc.insets = new java.awt.Insets(0, 0, 0, 0);
        panelFlacc.add(javax.swing.Box.createHorizontalGlue(), cFillerFlacc);

        java.awt.GridBagConstraints cFlaccPanel = kepConstraint(0, rowFl);
        cFlaccPanel.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cFlaccPanel.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cFlaccPanel.insets = new java.awt.Insets(0, 5, 8, 5);
        panelSeksiNyeriFlacc.add(panelFlacc, cFlaccPanel);
        rowFl++;

        // Tambahkan panelSeksiNyeriFlacc ke panelKepDua
        java.awt.GridBagConstraints cNyeriFlacc = kepConstraint(0, row);
        cNyeriFlacc.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cNyeriFlacc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cNyeriFlacc.insets = new java.awt.Insets(0, 5, 0, 5);
        panelKepDua.add(panelSeksiNyeriFlacc, cNyeriFlacc);
        row++;

        // ================================================================
        // PANEL SEKSI PQRST (ditampilkan untuk semua usia, dipindah dari hal3)
        // ================================================================
        panelSeksiNyeriPqrst = new widget.PanelBiasa();
        panelSeksiNyeriPqrst.setLayout(new java.awt.GridBagLayout());
        panelSeksiNyeriPqrst.setVisible(false);
        int rowPq = 0;

        java.awt.GridBagConstraints cPqrstHdr = kepConstraint(0, rowPq);
        cPqrstHdr.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cPqrstHdr.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cPqrstHdr.weightx = 1.0;
        cPqrstHdr.insets = new java.awt.Insets(10, 5, 3, 5);
        panelSeksiNyeriPqrst.add(buatHeaderStripKep("PQRST"), cPqrstHdr);
        rowPq++;
        CekProvCahaya = new widget.CekBox();
        CekProvCahaya.setText("Cahaya");
        CekProvGelap = new widget.CekBox();
        CekProvGelap.setText("Gelap");
        CekProvGerakan = new widget.CekBox();
        CekProvGerakan.setText("Gerakan");
        CekProvBerbaring = new widget.CekBox();
        CekProvBerbaring.setText("Berbaring");
        widget.PanelBiasa panelProvocation = new widget.PanelBiasa();
        panelProvocation.setOpaque(false);
        panelProvocation.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelProvocation.add(CekProvCahaya);
        panelProvocation.add(CekProvGelap);
        panelProvocation.add(CekProvGerakan);
        panelProvocation.add(CekProvBerbaring);
        rowPq = tambahBarisKepField(panelSeksiNyeriPqrst, rowPq, "Provocation (Faktor Yang Memperberat Nyeri)", panelProvocation);

        CekQualTumpul = new widget.CekBox();
        CekQualTumpul.setText("Tumpul");
        CekQualDitusuk = new widget.CekBox();
        CekQualDitusuk.setText("Ditusuk");
        CekQualBerdenyut = new widget.CekBox();
        CekQualBerdenyut.setText("Berdenyut");
        CekQualTajam = new widget.CekBox();
        CekQualTajam.setText("Tajam");
        CekQualPanas = new widget.CekBox();
        CekQualPanas.setText("Panas");
        widget.PanelBiasa panelQuality = new widget.PanelBiasa();
        panelQuality.setOpaque(false);
        panelQuality.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelQuality.add(CekQualTumpul);
        panelQuality.add(CekQualDitusuk);
        panelQuality.add(CekQualBerdenyut);
        panelQuality.add(CekQualTajam);
        panelQuality.add(CekQualPanas);
        rowPq = tambahBarisKepField(panelSeksiNyeriPqrst, rowPq, "Quality (Kualitas Nyeri)", panelQuality);

        RbRadiationTidak = new widget.RadioButton();
        RbRadiationTidak.setText("Tidak");
        RbRadiationYa = new widget.RadioButton();
        RbRadiationYa.setText("Ya, ke");
        bgRadiation = new javax.swing.ButtonGroup();
        bgRadiation.add(RbRadiationTidak);
        bgRadiation.add(RbRadiationYa);
        TRadiationKe = new widget.TextBox();
        TRadiationKe.setPreferredSize(new java.awt.Dimension(200, 23));
        TRadiationKe.setBackground(java.awt.Color.WHITE);
        TRadiationKe.setOpaque(true);
        TRadiationKe.setBorder(BORDER_KOTAK_TEXTFIELD);
        TRadiationKe.putClientProperty("FlatLaf.style",
                "background: #FFFFFF; inactiveBackground: #FFFFFF; disabledBackground: #FFFFFF;");
        TRadiationKe.setVisible(false);
        widget.PanelBiasa panelRadiation = new widget.PanelBiasa();
        panelRadiation.setOpaque(false);
        panelRadiation.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelRadiation.add(RbRadiationTidak);
        panelRadiation.add(RbRadiationYa);
        panelRadiation.add(TRadiationKe);
        java.awt.event.ItemListener listenerRadiation = (java.awt.event.ItemEvent e) -> {
            boolean ya = RbRadiationYa.isSelected();
            TRadiationKe.setVisible(ya);
            if (!ya) {
                TRadiationKe.setText("");
            }
            panelRadiation.revalidate();
            panelRadiation.repaint();
            if (FormInput != null) {
                FormInput.revalidate();
                FormInput.repaint();
            }
            hitungProgresKep();
        };
        RbRadiationTidak.addItemListener(listenerRadiation);
        RbRadiationYa.addItemListener(listenerRadiation);
        rowPq = tambahBarisKepField(panelSeksiNyeriPqrst, rowPq, "Radiation (Menjalar ke Tubuh Lain)", panelRadiation);

        RbSeverityTidakNyeri = new widget.RadioButton();
        RbSeverityTidakNyeri.setText("Tidak Nyeri");
        RbSeverityRingan = new widget.RadioButton();
        RbSeverityRingan.setText("Ringan");
        RbSeveritySedang = new widget.RadioButton();
        RbSeveritySedang.setText("Sedang");
        RbSeverityBerat = new widget.RadioButton();
        RbSeverityBerat.setText("Berat");
        bgSeverity = new javax.swing.ButtonGroup();
        bgSeverity.add(RbSeverityTidakNyeri);
        bgSeverity.add(RbSeverityRingan);
        bgSeverity.add(RbSeveritySedang);
        bgSeverity.add(RbSeverityBerat);
        widget.PanelBiasa panelSeverity = new widget.PanelBiasa();
        panelSeverity.setOpaque(false);
        panelSeverity.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelSeverity.add(RbSeverityTidakNyeri);
        panelSeverity.add(RbSeverityRingan);
        panelSeverity.add(RbSeveritySedang);
        panelSeverity.add(RbSeverityBerat);
        rowPq = tambahBarisKepField(panelSeksiNyeriPqrst, rowPq, "Severity (Tingkat Keparahan Nyeri)", panelSeverity);

        RbTimeTerusMenerus = new widget.RadioButton();
        RbTimeTerusMenerus.setText("Terus Menerus");
        RbTimeHilangTimbul = new widget.RadioButton();
        RbTimeHilangTimbul.setText("Hilang Timbul");
        RbTimeKurang30 = new widget.RadioButton();
        RbTimeKurang30.setText("<30 Menit");
        RbTimeLebih30 = new widget.RadioButton();
        RbTimeLebih30.setText(">30 Menit");
        bgTime = new javax.swing.ButtonGroup();
        bgTime.add(RbTimeTerusMenerus);
        bgTime.add(RbTimeHilangTimbul);
        bgTime.add(RbTimeKurang30);
        bgTime.add(RbTimeLebih30);
        widget.PanelBiasa panelTime = new widget.PanelBiasa();
        panelTime.setOpaque(false);
        panelTime.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelTime.add(RbTimeTerusMenerus);
        panelTime.add(RbTimeHilangTimbul);
        panelTime.add(RbTimeKurang30);
        panelTime.add(RbTimeLebih30);
        rowPq = tambahBarisKepField(panelSeksiNyeriPqrst, rowPq, "Time (Nyeri Berlangsung)", panelTime);

        // Tambahkan panelSeksiNyeriPqrst ke panelKepDua
        java.awt.GridBagConstraints cNyeriPqrst = kepConstraint(0, row);
        cNyeriPqrst.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cNyeriPqrst.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cNyeriPqrst.insets = new java.awt.Insets(0, 5, 0, 5);
        panelKepDua.add(panelSeksiNyeriPqrst, cNyeriPqrst);
        row++;

        // -- Logika kondisional Nyeri Tidak: reset semua --
        RbNyeriAdaTidak.addActionListener((java.awt.event.ActionEvent e) -> {
            Lokasi.setText("\u2014");
            Lokasi.setEnabled(false);
            if (btnSkalaNyeri != null && btnSkalaNyeri[0] != null) {
                btnSkalaNyeri[0].setSelected(true);
                updateGayaSkalaNyeri();
                SkalaNyeri.setSelectedItem("0");
            }
            CmbKategoriUsiaNyeri.setSelectedIndex(0);
            CmbKategoriUsiaNyeri.setEnabled(false);
            panelSeksiNyeriSkalaCommon.setVisible(false);
            panelSeksiNyeriNumeric.setVisible(false);
            panelSeksiNyeriWongBaker.setVisible(false);
            panelSeksiNyeriFlacc.setVisible(false);
            panelSeksiNyeriPqrst.setVisible(false);
            hitungProgresKep();
        });
        RbNyeriAdaYa.addActionListener((java.awt.event.ActionEvent e) -> {
            CmbKategoriUsiaNyeri.setEnabled(true);
            if ("\u2014".equals(Lokasi.getText())) {
                Lokasi.setText("");
            }
            Lokasi.setEnabled(true);
            updateVisibilitasNyeriKategori();
            hitungProgresKep();
        });

        // -- Logika kondisional Kategori Usia Nyeri --
        CmbKategoriUsiaNyeri.addItemListener((java.awt.event.ItemEvent e) -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                updateVisibilitasNyeriKategori();
                hitungProgresKep();
            }
        });

        PanelWall.setVisible(false);

        // panelKepDua (Gizi Dewasa/Anak + Asesmen Nyeri) dipecah per seksi oleh
        // pecahSeksiKep() lalu ditempatkan susunHalamanKep() - Y-math
        // null-layout lama dinonaktifkan.
    }

    private void updateVisibilitasNyeriKategori() {
        String pilihan = CmbKategoriUsiaNyeri != null && CmbKategoriUsiaNyeri.getSelectedItem() != null
                ? CmbKategoriUsiaNyeri.getSelectedItem().toString() : "";
        boolean isBayi = "Bayi - Anak < 3 Tahun".equals(pilihan);
        boolean is3_8 = "Usia 3-8 Tahun".equals(pilihan);
        boolean isGt8 = "Usia > 8 Tahun".equals(pilihan);
        if (panelSeksiNyeriFlacc != null) panelSeksiNyeriFlacc.setVisible(isBayi);
        if (panelSeksiNyeriSkalaCommon != null) panelSeksiNyeriSkalaCommon.setVisible(is3_8 || isGt8);
        if (panelSeksiNyeriWongBaker != null) panelSeksiNyeriWongBaker.setVisible(is3_8);
        if (panelSeksiNyeriNumeric != null) panelSeksiNyeriNumeric.setVisible(isGt8);
        if (panelSeksiNyeriPqrst != null) panelSeksiNyeriPqrst.setVisible(isBayi || is3_8 || isGt8);
        if (panelSeksiNyeriFlacc != null && panelSeksiNyeriFlacc.getParent() != null) {
            panelSeksiNyeriFlacc.getParent().revalidate();
            panelSeksiNyeriFlacc.getParent().repaint();
        }
    }

    /**
     * [RM 03-6] Membangun section baru HALAMAN 3 formulir fisik: FLACC,
     * PQRST, II. Intervensi Keperawatan + IV-IX kategori Tindakan
     * (Kardiopulmonal/Tindakan Penunjang/Tindakan Kolaborasi/Trauma-Bedah/
     * THT-Mata/Obgyn) - semua checklist flat pakai kolom boolean langsung
     * (keputusan user), BUKAN tabel master+anak baru. I. Diagnosis
     * Keperawatan TIDAK di sini - mekanisme JTable+checkbox lama
     * (tbMasalahKeperawatan) dipertahankan, tidak disentuh. III. Tindakan
     * Keperawatan Mandiri REUSE widget `Rencana` (TextArea) lama apa
     * adanya.
     */
    private void initKepTambahanHal3() {
        panelKepTiga = new widget.PanelBiasa();
        panelKepTiga.setLayout(new java.awt.GridBagLayout());
        int row = 0;

        // -- Diagnosis Keperawatan --
        row = tambahBarisKepJudul(panelKepTiga, row, "DIAGNOSIS KEPERAWATAN");
        widget.PanelBiasa panelCariMasalah = new widget.PanelBiasa();
        panelCariMasalah.setOpaque(false);
        panelCariMasalah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        label12.setVisible(false);
        TCariMasalah.setPreferredSize(new java.awt.Dimension(260, 26));
        TCariMasalah.putClientProperty("JTextField.placeholderText", "Ketik kata kunci masalah keperawatan...");

        BtnCariMasalah.setPreferredSize(new java.awt.Dimension(28, 26));
        BtnCariMasalah.setToolTipText("Filter / Pilih Masalah Keperawatan (Alt+1)");
        javax.swing.Icon icCariMasalah = widget.IconStyle.muat("check.svg", 14, widget.IconStyle.HIJAU);
        if (icCariMasalah != null) {
            BtnCariMasalah.setIcon(icCariMasalah);
        }

        BtnAllMasalah.setPreferredSize(new java.awt.Dimension(28, 26));
        BtnAllMasalah.setToolTipText("Tampilkan Semua / Cari Masalah Keperawatan (Alt+2)");
        javax.swing.Icon icAllMasalah = widget.IconStyle.muat("search.svg", 14, widget.IconStyle.ABU);
        if (icAllMasalah != null) {
            BtnAllMasalah.setIcon(icAllMasalah);
        }

        BtnTambahMasalah.setPreferredSize(new java.awt.Dimension(28, 26));
        BtnTambahMasalah.setToolTipText("Master Masalah Keperawatan (Alt+3)");
        javax.swing.Icon icTambahMasalah = widget.IconStyle.muat("plus.svg", 14, widget.IconStyle.HIJAU);
        if (icTambahMasalah != null) {
            BtnTambahMasalah.setIcon(icTambahMasalah);
        }

        panelCariMasalah.add(TCariMasalah);
        panelCariMasalah.add(BtnCariMasalah);
        panelCariMasalah.add(BtnAllMasalah);
        panelCariMasalah.add(BtnTambahMasalah);

        widget.PanelBiasa panelGabungMasalah = new widget.PanelBiasa();
        panelGabungMasalah.setOpaque(false);
        panelGabungMasalah.putClientProperty("fillWidth", Boolean.TRUE);
        panelGabungMasalah.setLayout(new java.awt.BorderLayout(0, 6));
        panelGabungMasalah.add(panelCariMasalah, java.awt.BorderLayout.NORTH);
        Scroll8.setPreferredSize(new java.awt.Dimension(Scroll8.getPreferredSize().width, 130));
        panelGabungMasalah.add(Scroll8, java.awt.BorderLayout.CENTER);

        row = tambahBarisKepField(panelKepTiga, row, "Masalah Keperawatan", true, panelGabungMasalah);

        // -- Intervensi Keperawatan --
        row = tambahBarisKepJudul(panelKepTiga, row, "INTERVENSI KEPERAWATAN");
        CekIntervManajemenJalanNafas = new widget.CekBox();
        CekIntervManajemenJalanNafas.setText("Manajemen Jalan Nafas");
        CekIntervSuctioningJalanNafas = new widget.CekBox();
        CekIntervSuctioningJalanNafas.setText("Suctioning Jalan Nafas");
        CekIntervManajemenAsamBasa = new widget.CekBox();
        CekIntervManajemenAsamBasa.setText("Manajemen Asam Basa");
        CekIntervManajemenSyok = new widget.CekBox();
        CekIntervManajemenSyok.setText("Manajemen Syok");
        CekIntervManajemenNyeri = new widget.CekBox();
        CekIntervManajemenNyeri.setText("Manajemen Nyeri");
        CekIntervManajemenCairan = new widget.CekBox();
        CekIntervManajemenCairan.setText("Manajemen Cairan");
        CekIntervMonitoringCairan = new widget.CekBox();
        CekIntervMonitoringCairan.setText("Monitoring Cairan");
        CekIntervManajemenLingkungan = new widget.CekBox();
        CekIntervManajemenLingkungan.setText("Manajemen Lingkungan");
        CekIntervPendidikanKesehatan = new widget.CekBox();
        CekIntervPendidikanKesehatan.setText("Pendidikan Kesehatan");
        CekIntervFeverTreatment = new widget.CekBox();
        CekIntervFeverTreatment.setText("Fever Treatment");
        CekIntervManajemenSensasiPerifer = new widget.CekBox();
        CekIntervManajemenSensasiPerifer.setText("Manajemen Sensasi Perifer");
        CekIntervMonitoringKardiovaskuler = new widget.CekBox();
        CekIntervMonitoringKardiovaskuler.setText("Monitoring Status Kardiovaskuler");
        CekIntervPencegahanPenekananLuka = new widget.CekBox();
        CekIntervPencegahanPenekananLuka.setText("Pencegahan Penekanan pada Luka");

        CekIntervLainnya = new widget.CekBox();
        CekIntervLainnya.setText("Lainnya");
        TIntervLainnya = new widget.TextBox();

        widget.PanelBiasa panelInterv = buatGridDuaKolomKep(new java.awt.Component[][] {
                { buatItemCekTeks(CekIntervManajemenJalanNafas, null, null),
                  buatItemCekTeks(CekIntervManajemenLingkungan, null, null) },
                { buatItemCekTeks(CekIntervSuctioningJalanNafas, null, null),
                  buatItemCekTeks(CekIntervPendidikanKesehatan, null, null) },
                { buatItemCekTeks(CekIntervManajemenAsamBasa, null, null),
                  buatItemCekTeks(CekIntervFeverTreatment, null, null) },
                { buatItemCekTeks(CekIntervManajemenSyok, null, null),
                  buatItemCekTeks(CekIntervManajemenSensasiPerifer, null, null) },
                { buatItemCekTeks(CekIntervManajemenNyeri, null, null),
                  buatItemCekTeks(CekIntervMonitoringKardiovaskuler, null, null) },
                { buatItemCekTeks(CekIntervManajemenCairan, null, null),
                  buatItemCekTeks(CekIntervPencegahanPenekananLuka, null, null) },
                { buatItemCekTeks(CekIntervMonitoringCairan, null, null),
                  buatItemCekTeks(CekIntervLainnya, TIntervLainnya, "Keterangan Intervensi Lainnya") }
        });
        panelInterv.putClientProperty("fillWidth", Boolean.TRUE);
        row = tambahBarisKepField(panelKepTiga, row, "Intervensi Keperawatan", true, panelInterv);

        // -- Tindakan Keperawatan Mandiri --
        row = tambahBarisKepJudul(panelKepTiga, row, "TINDAKAN KEPERAWATAN MANDIRI");
        TabRencanaKeperawatan.setVisible(false);
        scrollPane5.setPreferredSize(new java.awt.Dimension(scrollPane5.getPreferredSize().width, 100));
        scrollPane5.setBorder(BORDER_KOTAK_TEXTFIELD);
        scrollPane5.putClientProperty("fillWidth", Boolean.TRUE);
        Rencana.putClientProperty("JTextField.placeholderText", "Tuliskan rencana dan tindakan keperawatan mandiri...");
        row = tambahBarisKepField(panelKepTiga, row, "Rencana & Tindakan", false, scrollPane5);

        // -- Kardiopulmonal --
        row = tambahBarisKepJudul(panelKepTiga, row, "KARDIOPULMONAL");
        CekKardioPulseOximetri = new widget.CekBox();
        CekKardioPulseOximetri.setText("Pulse Oximetri (SpO2)");
        CekKardioIntubasi = new widget.CekBox();
        CekKardioIntubasi.setText("Intubasi");
        CekKardioRjp = new widget.CekBox();
        CekKardioRjp.setText("RJP");
        TKardioRjpKet = new widget.TextBox();
        TKardioRjpKet.setPreferredSize(new java.awt.Dimension(180, 24));
        TKardioRjpKet.setPlaceholder("Keterangan");
        TKardioRjpKet.putClientProperty("JComponent.roundRect", true);
        TKardioRjpKet.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8, 0xDB, 0xD6), 1),
                javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        pasangKondisionalCek(CekKardioRjp, TKardioRjpKet);
        TKardioRjpKet.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (TKardioRjpKet.getText().length() > 0 && !CekKardioRjp.isSelected()) {
                    CekKardioRjp.setSelected(true);
                    hitungProgresKep();
                }
            }
        });

        CekKardioKardioversiDcShock = new widget.CekBox();
        CekKardioKardioversiDcShock.setText("Kardioversi / DC Shock");

        widget.PanelBiasa itemRjp = new widget.PanelBiasa();
        itemRjp.setOpaque(false);
        itemRjp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
        itemRjp.add(CekKardioRjp);
        itemRjp.add(TKardioRjpKet);

        widget.PanelBiasa panelKardio = new widget.PanelBiasa();
        panelKardio.setOpaque(false);
        panelKardio.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 18, 0));
        panelKardio.add(CekKardioPulseOximetri);
        panelKardio.add(CekKardioIntubasi);
        panelKardio.add(itemRjp);
        panelKardio.add(CekKardioKardioversiDcShock);

        row = tambahBarisKepField(panelKepTiga, row, "Kardiopulmonal", panelKardio);

        // -- Tindakan Penunjang --
        row = tambahBarisKepJudul(panelKepTiga, row, "TINDAKAN PENUNJANG");
        CekPenunjangCekLab = new widget.CekBox();
        TPenunjangCekLabKet = new widget.TextBox();
        CekPenunjangRontgen = new widget.CekBox();
        TPenunjangRontgenKet = new widget.TextBox();
        CekPenunjangCtScan = new widget.CekBox();
        TPenunjangCtScanKet = new widget.TextBox();

        txtPenunjangLab = new javax.swing.JTextArea();
        txtPenunjangRad = new javax.swing.JTextArea();

        panelPenunjangMedis = new PanelPenunjangMedis(txtPenunjangLab, txtPenunjangRad, true);
        panelPenunjangMedis.putClientProperty("fillWidth", Boolean.TRUE);
        panelPenunjangMedis.setKoneksiServer(koneksi, akses.getkode());

        panelPenunjangMedis.btnTarikSemua.addActionListener(e -> {
            tarikSemuaDataPenunjang();
            if (panelPenunjangMedis != null) panelPenunjangMedis.perbaruiSemuaBadge();
        });
        panelPenunjangMedis.btnCekLab.addActionListener(e -> {
            tarikDataLaboratorium(false);
            if (panelPenunjangMedis != null) panelPenunjangMedis.perbaruiSemuaBadge();
        });
        panelPenunjangMedis.btnCekRad.addActionListener(e -> {
            tarikDataRadiologi(false);
            if (panelPenunjangMedis != null) panelPenunjangMedis.perbaruiSemuaBadge();
        });

        txtPenunjangLab.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                String t = txtPenunjangLab.getText().trim();
                TPenunjangCekLabKet.setText(t);
                CekPenunjangCekLab.setSelected(!t.isEmpty());
                hitungProgresKep();
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        txtPenunjangRad.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                String t = txtPenunjangRad.getText().trim();
                TPenunjangRontgenKet.setText(t);
                CekPenunjangRontgen.setSelected(!t.isEmpty());
                if (t.toUpperCase().matches(".*\\b(CT|SCAN|MSCT)\\b.*")) {
                    CekPenunjangCtScan.setSelected(true);
                    TPenunjangCtScanKet.setText(t);
                } else {
                    CekPenunjangCtScan.setSelected(false);
                    TPenunjangCtScanKet.setText("");
                }
                hitungProgresKep();
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        row = tambahBarisKepField(panelKepTiga, row, "", panelPenunjangMedis);

        // -- Tindakan Kolaborasi --
        row = tambahBarisKepJudul(panelKepTiga, row, "TINDAKAN KOLABORASI");
        CekKolabO2 = new widget.CekBox();
        CekKolabO2.setText("O2");
        TKolabO2Ket = new widget.TextBox();
        lblKolabO2 = new widget.Label();
        lblKolabO2.setText("l/m nasal canul/NRM/RM/CPAP :");
        lblKolabO2.setVisible(false);

        CekKolabEkgMonitor = new widget.CekBox();
        CekKolabEkgMonitor.setText("EKG / Monitor EKG");

        CekKolabNebulizer = new widget.CekBox();
        CekKolabNebulizer.setText("Nebulizer");

        CekKolabPasangInfus = new widget.CekBox();
        CekKolabPasangInfus.setText("Pasang Infus");
        TKolabPasangInfusKet = new widget.TextBox();

        CekKolabInjeksiSyringePump = new widget.CekBox();
        CekKolabInjeksiSyringePump.setText("Injeksi / Syringe Pump");

        CekKolabTransfusiDarah = new widget.CekBox();
        CekKolabTransfusiDarah.setText("Transfusi Darah");
        TKolabTransfusiDarahKet = new widget.TextBox();

        CekKolabNgt = new widget.CekBox();
        CekKolabNgt.setText("NGT");
        TKolabNgtUkuran = new widget.TextBox();

        CekKolabDowerCatheter = new widget.CekBox();
        CekKolabDowerCatheter.setText("Dower Catheter");
        TKolabDowerCatheterUkuran = new widget.TextBox();

        CekKolabBilasLambung = new widget.CekBox();
        CekKolabBilasLambung.setText("Bilas Lambung");
        TKolabBilasLambungKet = new widget.TextBox();

        CekKolabRestrainPengaman = new widget.CekBox();
        CekKolabRestrainPengaman.setText("Restrain / Pengaman");

        widget.PanelBiasa panelKolab = buatGridDuaKolomKep(new java.awt.Component[][] {
                { buatItemCekTeks(CekKolabO2, TKolabO2Ket, "l/m nasal canul/NRM/RM/CPAP"),
                  buatItemCekTeks(CekKolabEkgMonitor, null, null) },
                { buatItemCekTeks(CekKolabNebulizer, null, null),
                  buatItemCekTeks(CekKolabPasangInfus, TKolabPasangInfusKet, "Keterangan") },
                { buatItemCekTeks(CekKolabInjeksiSyringePump, null, null),
                  buatItemCekTeks(CekKolabTransfusiDarah, TKolabTransfusiDarahKet, "Keterangan") },
                { buatItemCekTeks(CekKolabNgt, TKolabNgtUkuran, "Ukuran"),
                  buatItemCekTeks(CekKolabDowerCatheter, TKolabDowerCatheterUkuran, "Ukuran") },
                { buatItemCekTeks(CekKolabBilasLambung, TKolabBilasLambungKet, "Keterangan"),
                  buatItemCekTeks(CekKolabRestrainPengaman, null, null) }
        });
        row = tambahBarisKepField(panelKepTiga, row, "Kolaborasi", panelKolab);

        // -- Trauma / Bedah --
        row = tambahBarisKepJudul(panelKepTiga, row, "TRAUMA / BEDAH");
        CekTraumaCervicalCollar = new widget.CekBox();
        CekTraumaCervicalCollar.setText("Cervical Collar");
        CekTraumaImmobilisasiTulangBelakang = new widget.CekBox();
        CekTraumaImmobilisasiTulangBelakang.setText("Immobilisasi Tulang Belakang");
        CekTraumaGips = new widget.CekBox();
        CekTraumaGips.setText("Gips");
        CekTraumaPerawatanLuka = new widget.CekBox();
        CekTraumaPerawatanLuka.setText("Perawatan Luka");
        CekTraumaJahitLuka = new widget.CekBox();
        CekTraumaJahitLuka.setText("Jahit Luka");
        CekTraumaBalutanSpalk = new widget.CekBox();
        CekTraumaBalutanSpalk.setText("Balutan / Spalk");
        CekTraumaSelangDadaWsd = new widget.CekBox();
        CekTraumaSelangDadaWsd.setText("Selang Dada / WSD");

        widget.PanelBiasa panelTrauma = buatGridDuaKolomKep(new java.awt.Component[][] {
                { buatItemCekTeks(CekTraumaCervicalCollar, null, null),
                  buatItemCekTeks(CekTraumaImmobilisasiTulangBelakang, null, null) },
                { buatItemCekTeks(CekTraumaGips, null, null),
                  buatItemCekTeks(CekTraumaPerawatanLuka, null, null) },
                { buatItemCekTeks(CekTraumaJahitLuka, null, null),
                  buatItemCekTeks(CekTraumaBalutanSpalk, null, null) },
                { buatItemCekTeks(CekTraumaSelangDadaWsd, null, null),
                  null }
        });
        row = tambahBarisKepField(panelKepTiga, row, "Trauma / Bedah", panelTrauma);
        TTraumaLainnya = new widget.TextBox();
        TTraumaLainnya.setPreferredSize(new java.awt.Dimension(300, 26));
        TTraumaLainnya.setPlaceholder("Keterangan");
        row = tambahBarisKepField(panelKepTiga, row, "Lainnya", TTraumaLainnya);

        // -- THT / Mata --
        row = tambahBarisKepJudul(panelKepTiga, row, "THT / MATA");
        CekThtIrigasiMataTelinga = new widget.CekBox();
        CekThtIrigasiMataTelinga.setText("Irigasi Mata / Irigasi Telinga");
        CekThtAmbilKorpusAlienum = new widget.CekBox();
        CekThtAmbilKorpusAlienum.setText("Ambil Korpus Alienum di Telinga/Hidung/Mata");

        widget.PanelBiasa panelTht = new widget.PanelBiasa();
        panelTht.setOpaque(false);
        panelTht.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 18, 0));
        panelTht.add(CekThtIrigasiMataTelinga);
        panelTht.add(CekThtAmbilKorpusAlienum);
        row = tambahBarisKepField(panelKepTiga, row, "THT / Mata", panelTht);

        // -- Tindakan Obgyn --
        row = tambahBarisKepJudul(panelKepTiga, row, "TINDAKAN OBGYN");
        CekObgynMembantuPartus = new widget.CekBox();
        CekObgynMembantuPartus.setText("Membantu Partus");
        CekObgynPeriksaGinekologi = new widget.CekBox();
        CekObgynPeriksaGinekologi.setText("Periksa Ginekologi");
        CekObgynDopplerCtgUsg = new widget.CekBox();
        CekObgynDopplerCtgUsg.setText("Doppler, CTG, USG");
        CekObgynPasangTampon = new widget.CekBox();
        CekObgynPasangTampon.setText("Pasang Tampon");

        widget.PanelBiasa panelObgyn = new widget.PanelBiasa();
        panelObgyn.setOpaque(false);
        panelObgyn.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 18, 0));
        panelObgyn.add(CekObgynMembantuPartus);
        panelObgyn.add(CekObgynPeriksaGinekologi);
        panelObgyn.add(CekObgynDopplerCtgUsg);
        panelObgyn.add(CekObgynPasangTampon);
        row = tambahBarisKepField(panelKepTiga, row, "Tindakan Obgyn", panelObgyn);

        // Selesai membangun blok Diagnosis: pindahkan panel hasil ke field
        // panelKepTigaDiag & kembalikan panelKepTiga ke panel FLACC/PQRST.
        // Keduanya dipecah per seksi oleh pecahSeksiKep() lalu ditempatkan
        // susunHalamanKep() - Y-math null-layout lama dinonaktifkan.
        panelKepTigaDiag = panelKepTiga;
    }

    /**
     * Hasil Skrining Resiko Pasien Jatuh dihitung otomatis sesuai aturan
     * formulir fisik: Beresiko bila ditemukan salah satu antara A (Cara
     * Berjalan) atau B (Menopang Saat Akan Duduk) = Ya, Tidak Beresiko
     * bila A dan B tidak ditemukan (keduanya Tidak/belum diisi), dipicu
     * tiap ada perubahan pilihan A/B.
     */
    private void hitungHasilJatuh() {
        if (RbJatuhCaraBerjalanYa != null && RbJatuhMenopangYa != null
                && RbJatuhBeresiko != null && RbJatuhTidakBeresiko != null) {
            if (RbJatuhCaraBerjalanYa.isSelected() || RbJatuhMenopangYa.isSelected()) {
                RbJatuhBeresiko.setSelected(true);
            } else {
                RbJatuhTidakBeresiko.setSelected(true);
            }
            hitungProgresKep();
        }
    }

    /**
     * Total Skor MST (Skrining Gizi Dewasa) dihitung otomatis dari
     * Penurunan BB (Tidak Ada Penurunan BB=0 / Tidak Yakin-Baju Longgar=2 /
     * Ya sesuai jumlah kg: 1-5kg=1, 6-10kg=2, 11-15kg=3, {@literal >}15kg=4)
     * + Asupan Makan Berkurang (Ya=1/Tidak=0), dipicu tiap ada perubahan
     * pilihan (lihat listener di initKepTambahanHal2()). Kesimpulan ikut
     * dihitung otomatis sesuai aturan formulir fisik: Risiko bila skor≥2
     * DAN/ATAU pasien punya diagnosis/kondisi khusus, selain itu Tidak
     * Risiko.
     */
    private void hitungTotalMst() {
        int skor = 0;
        if (RbMstTidakYakin.isSelected()) {
            skor += 2;
        } else if (RbMstYa.isSelected()) {
            Object kg = MstPenurunanBbKg.getSelectedItem();
            String kgTeks = kg == null ? "" : kg.toString();
            if ("1-5 kg".equals(kgTeks))
                skor += 1;
            else if ("6-10 kg".equals(kgTeks))
                skor += 2;
            else if ("11-15 kg".equals(kgTeks))
                skor += 3;
            else if (">15 kg".equals(kgTeks))
                skor += 4;
        }
        if (RbMstAsupanYa.isSelected())
            skor += 1;
        TMstTotalSkor.setText(String.valueOf(skor));
        if (skor >= 2 || RbMstDiagnosisYa.isSelected()) {
            RbMstRisiko.setSelected(true);
        } else {
            RbMstTidakRisiko.setSelected(true);
        }
    }

    /**
     * Total Skor & Kesimpulan Skrining Gizi Anak dihitung otomatis: 4
     * kriteria Ya/Tidak dijumlah (Tampak Kurus=1, Turun BB=1, Kondisi
     * Khusus=1, Risiko Malnutrisi=2), lalu Kesimpulan mengikuti aturan
     * formulir fisik (0=Risiko Rendah, 1-3=Risiko Sedang, 4-5=Risiko
     * Tinggi), dipicu tiap ada perubahan pilihan.
     */
    private void hitungTotalGiziAnak() {
        int skor = 0;
        if (RbGiziAnakKurusYa.isSelected())
            skor += 1;
        if (RbGiziAnakTurunBbYa.isSelected())
            skor += 1;
        if (RbGiziAnakKondisiYa.isSelected())
            skor += 1;
        if (RbGiziAnakRisikoYa.isSelected())
            skor += 2;
        TGiziAnakTotalSkor.setText(String.valueOf(skor));
        if (skor == 0) {
            RbGiziAnakRendah.setSelected(true);
        } else if (skor <= 3) {
            RbGiziAnakSedang.setSelected(true);
        } else {
            RbGiziAnakTinggi.setSelected(true);
        }
    }

    /**
     * Total Skor FLACC dihitung otomatis dari jumlah 5 kriteria (Wajah/
     * Kaki/Aktivitas/Menangis/Bersuara) sesuai tabel formulir fisik RM 03-6.
     * Index 0 = "", Index 1 = skor 0, Index 2 = skor 1, Index 3 = skor 2.
     * Dipicu tiap ada perubahan pilihan ComboBox.
     */
    private void hitungTotalFlacc() {
        widget.ComboBox[] semuaFlacc = { FlaccWajah, FlaccKaki, FlaccAktivitas, FlaccMenangis, FlaccBersuara };
        boolean adaPilihan = false;
        int total = 0;
        for (widget.ComboBox cmb : semuaFlacc) {
            if (cmb != null) {
                int idx = cmb.getSelectedIndex();
                if (idx > 0) {
                    adaPilihan = true;
                    total += (idx - 1);
                }
            }
        }
        if (adaPilihan) {
            TFlaccTotal.setText(String.valueOf(total));
            String ket = "";
            if (total == 0)
                ket = " (Rileks / Nyaman)";
            else if (total <= 3)
                ket = " (Nyeri Ringan)";
            else if (total <= 6)
                ket = " (Nyeri Sedang)";
            else
                ket = " (Nyeri Berat)";
            TFlaccTotal.setToolTipText("Total Skor FLACC: " + total + ket);
        } else {
            TFlaccTotal.setText("");
            TFlaccTotal.setToolTipText("Total Skor FLACC (0-10)");
        }
    }

    /**
     * Mendapatkan nilai skor FLACC ('0','1','2' atau null) dari pilihan ComboBox
     * untuk disimpan ke database MySQL (kolom ENUM('0','1','2')).
     */
    private String getNilaiDbFlacc(widget.ComboBox cmb) {
        if (cmb == null)
            return null;
        int idx = cmb.getSelectedIndex();
        if (idx <= 0)
            return null;
        return String.valueOf(idx - 1);
    }

    /**
     * Memetakan nilai skor database ('0','1','2' atau null) ke pilihan ComboBox
     * FLACC.
     */
    private void setPilihanFlaccDariDb(widget.ComboBox cmb, String nilaiDb) {
        if (cmb == null)
            return;
        if (nilaiDb == null || nilaiDb.trim().isEmpty()) {
            cmb.setSelectedIndex(0);
        } else {
            try {
                int skor = Integer.parseInt(nilaiDb.trim());
                if (skor >= 0 && skor <= 2) {
                    cmb.setSelectedIndex(skor + 1);
                } else {
                    cmb.setSelectedIndex(0);
                }
            } catch (Exception e) {
                cmb.setSelectedIndex(0);
            }
        }
    }

    /**
     * [Lanjutan RM 03-6] Membangun section baru HALAMAN 4 formulir fisik:
     * Kondisi Pasien Saat Pindah/Pulang dari IGD. Tanda tangan Perawat/
     * Bidan + Dokter TIDAK ada field di sini - pakai QR code digital di
     * jrxml cetak (pola sama RM 03-1/03-2). Dokter (KdDokterKp) & Perawat
     * yang bertanggung jawab dipindah ke header (permintaan user) - lihat
     * initKepTambahan(); Perawat/Bidan pakai header "Petugas" langsung,
     * tidak ada field terpisah lagi.
     */
    private void initKepTambahanHal4() {
        panelKepEmpat = new widget.PanelBiasa();
        panelKepEmpat.setLayout(new java.awt.GridBagLayout());
        int row = 0;

        row = tambahBarisKepJudul(panelKepEmpat, row, "KONDISI PASIEN SAAT PINDAH / PULANG DARI IGD");

        // Vital Signs saat Keluar/Pindah: 4 kolom rapi sesuai mockup (Pukul, GCS, TD, Nadi, Suhu, Pernafasan, SpO2)
        DTPKpPukul = new PanelTanggalDanJam(true, 23);

        DTPKpPukulSaja = new PanelJamSaja(true, 26);

        TKpGcsE = buatTextBoxKotakLokal(null);
        TKpGcsE.setPreferredSize(new java.awt.Dimension(80, 26));
        TKpGcsE.putClientProperty("JTextField.placeholderText", "—");

        TKpGcsV = buatTextBoxKotakLokal(null);
        TKpGcsV.setPreferredSize(new java.awt.Dimension(80, 26));
        TKpGcsV.putClientProperty("JTextField.placeholderText", "—");

        TKpGcsM = buatTextBoxKotakLokal(null);
        TKpGcsM.setPreferredSize(new java.awt.Dimension(80, 26));
        TKpGcsM.putClientProperty("JTextField.placeholderText", "—");

        TKpTd = buatTextBoxKotakLokal(null);
        TKpTd.setPreferredSize(new java.awt.Dimension(80, 26));
        TKpTd.putClientProperty("JTextField.placeholderText", "—");

        TKpNadi = buatTextBoxKotakLokal(null);
        TKpNadi.setPreferredSize(new java.awt.Dimension(80, 26));
        TKpNadi.putClientProperty("JTextField.placeholderText", "—");

        TKpSuhu = buatTextBoxKotakLokal(null);
        TKpSuhu.setPreferredSize(new java.awt.Dimension(80, 26));
        TKpSuhu.putClientProperty("JTextField.placeholderText", "—");

        TKpPernafasan = buatTextBoxKotakLokal(null);
        TKpPernafasan.setPreferredSize(new java.awt.Dimension(80, 26));
        TKpPernafasan.putClientProperty("JTextField.placeholderText", "—");

        TKpSpo = buatTextBoxKotakLokal(null);
        TKpSpo.setPreferredSize(new java.awt.Dimension(80, 26));
        TKpSpo.putClientProperty("JTextField.placeholderText", "—");

        widget.PanelBiasa panelKpVital = new widget.PanelBiasa();
        panelKpVital.setOpaque(false);
        panelKpVital.setLayout(new java.awt.GridBagLayout());
        panelKpVital.putClientProperty("fillWidth", Boolean.TRUE);

        java.awt.GridBagConstraints cVital = new java.awt.GridBagConstraints();
        cVital.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cVital.weightx = 0.25;

        // Baris 1: Pukul, GCS - E, GCS - V, GCS - M
        cVital.gridy = 0;
        cVital.gridx = 0;
        cVital.insets = new java.awt.Insets(0, 0, 10, 14);
        panelKpVital.add(buatSelVital("Pukul", DTPKpPukulSaja, null), cVital);

        cVital.gridx = 1;
        cVital.insets = new java.awt.Insets(0, 0, 10, 14);
        panelKpVital.add(buatSelVital("GCS - E", TKpGcsE, null), cVital);

        cVital.gridx = 2;
        cVital.insets = new java.awt.Insets(0, 0, 10, 14);
        panelKpVital.add(buatSelVital("GCS - V", TKpGcsV, null), cVital);

        cVital.gridx = 3;
        cVital.insets = new java.awt.Insets(0, 0, 10, 0);
        panelKpVital.add(buatSelVital("GCS - M", TKpGcsM, null), cVital);

        // Baris 2: TD, Nadi, Suhu, Pernafasan
        cVital.gridy = 1;
        cVital.gridx = 0;
        cVital.insets = new java.awt.Insets(0, 0, 10, 14);
        panelKpVital.add(buatSelVital("TD", TKpTd, "mmHg"), cVital);

        cVital.gridx = 1;
        cVital.insets = new java.awt.Insets(0, 0, 10, 14);
        panelKpVital.add(buatSelVital("Nadi", TKpNadi, "/mnt"), cVital);

        cVital.gridx = 2;
        cVital.insets = new java.awt.Insets(0, 0, 10, 14);
        panelKpVital.add(buatSelVital("Suhu", TKpSuhu, "°C"), cVital);

        cVital.gridx = 3;
        cVital.insets = new java.awt.Insets(0, 0, 10, 0);
        panelKpVital.add(buatSelVital("Pernafasan", TKpPernafasan, "/mnt"), cVital);

        // Baris 3: SpO2, (filler)
        cVital.gridy = 2;
        cVital.gridx = 0;
        cVital.insets = new java.awt.Insets(0, 0, 0, 14);
        panelKpVital.add(buatSelVital("SpO2", TKpSpo, "%"), cVital);

        cVital.gridx = 1;
        cVital.insets = new java.awt.Insets(0, 0, 0, 14);
        panelKpVital.add(javax.swing.Box.createHorizontalGlue(), cVital);

        cVital.gridx = 2;
        cVital.insets = new java.awt.Insets(0, 0, 0, 14);
        panelKpVital.add(javax.swing.Box.createHorizontalGlue(), cVital);

        cVital.gridx = 3;
        cVital.insets = new java.awt.Insets(0, 0, 0, 0);
        panelKpVital.add(javax.swing.Box.createHorizontalGlue(), cVital);

        row = tambahBarisKepField(panelKepEmpat, row, "Kondisi Saat Pindah/Pulang", false, panelKpVital);

        // Keteraturan Nadi
        RbKpNadiReguler = new widget.RadioButton();
        RbKpNadiReguler.setText("Reguler");
        RbKpNadiReguler.putClientProperty("pertahankanRadioIcon", Boolean.TRUE);
        RbKpNadiIreguler = new widget.RadioButton();
        RbKpNadiIreguler.setText("Ireguler");
        RbKpNadiIreguler.putClientProperty("pertahankanRadioIcon", Boolean.TRUE);
        bgKpNadiReguler = new javax.swing.ButtonGroup();
        bgKpNadiReguler.add(RbKpNadiReguler);
        bgKpNadiReguler.add(RbKpNadiIreguler);
        RbKpNadiReguler.setSelected(true);

        widget.PanelBiasa panelKpNadi = new widget.PanelBiasa();
        panelKpNadi.setOpaque(false);
        panelKpNadi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        panelKpNadi.add(RbKpNadiReguler);
        panelKpNadi.add(RbKpNadiIreguler);

        row = tambahBarisKepField(panelKepEmpat, row, "Keteraturan Nadi", false, panelKpNadi);

        // Masuk Rumah Sakit & Petugas yang Menerima
        CekKpMasukRs = new widget.CekBox();
        CekKpMasukRs.setText("Ruang Perawatan");
        TKpRuang = new widget.TextBox();
        TKpRuang.setPreferredSize(new java.awt.Dimension(220, 23));
        TKpRuang.setEditable(false);
        BtnKpRuang = new widget.Button();
        BtnKpRuang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnKpRuang.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariKamar dlg = new DlgCariKamar(null, false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (dlg.getTable().getSelectedRow() != -1) {
                        TKpRuang.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(), 1).toString());
                    }
                    BtnKpRuang.requestFocus();
                }
            });
            dlg.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        widget.PanelBiasa panelKpMasukRs = new widget.PanelBiasa();
        panelKpMasukRs.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelKpMasukRs.add(CekKpMasukRs);
        panelKpMasukRs.add(TKpRuang);
        panelKpMasukRs.add(BtnKpRuang);

        TKpPetugasPenerima = new widget.TextBox();
        TKpPetugasPenerima.setPreferredSize(new java.awt.Dimension(70, 23));
        TKpPetugasPenerima.setEditable(false);
        NmKpPetugasPenerima = new widget.TextBox();
        NmKpPetugasPenerima.setPreferredSize(new java.awt.Dimension(180, 23));
        NmKpPetugasPenerima.setEditable(false);
        BtnKpPetugasPenerima = new widget.Button();
        BtnKpPetugasPenerima.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnKpPetugasPenerima.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariPetugas dlg = new DlgCariPetugas(null, false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (dlg.getTable().getSelectedRow() != -1) {
                        TKpPetugasPenerima
                                .setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(), 0).toString());
                        NmKpPetugasPenerima
                                .setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(), 1).toString());
                    }
                    BtnKpPetugasPenerima.requestFocus();
                }
            });
            dlg.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        widget.PanelBiasa panelKpPetugasPenerima = new widget.PanelBiasa();
        panelKpPetugasPenerima.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelKpPetugasPenerima.add(TKpPetugasPenerima);
        panelKpPetugasPenerima.add(NmKpPetugasPenerima);
        panelKpPetugasPenerima.add(BtnKpPetugasPenerima);

        final widget.PanelBiasa barisKpMasukRs = barisPengkajian(panelKepEmpat, row, "Masuk Rawat Inap", false, panelKpMasukRs);
        row++;

        selPetugasPenerima = barisPengkajian(panelKepEmpat, row, "Petugas yang Menerima", false, panelKpPetugasPenerima);
        selPetugasPenerima.setVisible(false);
        row++;

        pasangKondisionalCek(CekKpMasukRs, TKpRuang, BtnKpRuang, selPetugasPenerima);

        // Pendamping
        RbKpPendampingDokter = new widget.RadioButton();
        RbKpPendampingDokter.setText("Dokter");
        RbKpPendampingPerawat = new widget.RadioButton();
        RbKpPendampingPerawat.setText("Perawat");
        bgKpPendamping = new javax.swing.ButtonGroup();
        bgKpPendamping.add(RbKpPendampingDokter);
        bgKpPendamping.add(RbKpPendampingPerawat);
        widget.PanelBiasa panelKpPendamping = new widget.PanelBiasa();
        panelKpPendamping.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelKpPendamping.add(RbKpPendampingDokter);
        panelKpPendamping.add(RbKpPendampingPerawat);
        row = tambahBarisKepField(panelKepEmpat, row, "Pendamping Pasien", panelKpPendamping);

        // Tindakan Lanjutan
        RbKpTlKamarOperasi = new widget.RadioButton();
        RbKpTlKamarOperasi.setText("Kamar Operasi");
        RbKpTlKamarBersalin = new widget.RadioButton();
        RbKpTlKamarBersalin.setText("Kamar Bersalin");
        RbKpTlHcu = new widget.RadioButton();
        RbKpTlHcu.setText("HCU");
        RbKpTlKamarJenazah = new widget.RadioButton();
        RbKpTlKamarJenazah.setText("Kamar Jenazah");
        RbKpTlLainnya = new widget.RadioButton();
        RbKpTlLainnya.setText("Lainnya");
        bgKpTindakanLanjutan = new javax.swing.ButtonGroup();
        bgKpTindakanLanjutan.add(RbKpTlKamarOperasi);
        bgKpTindakanLanjutan.add(RbKpTlKamarBersalin);
        bgKpTindakanLanjutan.add(RbKpTlHcu);
        bgKpTindakanLanjutan.add(RbKpTlKamarJenazah);
        bgKpTindakanLanjutan.add(RbKpTlLainnya);
        TKpTindakanLanjutanKet = new widget.TextBox();
        TKpTindakanLanjutanKet.setPreferredSize(new java.awt.Dimension(180, 23));
        TKpTindakanLanjutanKet.setVisible(false);
        widget.PanelBiasa panelKpTl = new widget.PanelBiasa();
        panelKpTl.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelKpTl.add(RbKpTlKamarOperasi);
        panelKpTl.add(RbKpTlKamarBersalin);
        panelKpTl.add(RbKpTlHcu);
        panelKpTl.add(RbKpTlKamarJenazah);
        panelKpTl.add(RbKpTlLainnya);
        panelKpTl.add(TKpTindakanLanjutanKet);
        java.awt.event.ItemListener listenerKpTl = (java.awt.event.ItemEvent e) -> {
            boolean lainnya = RbKpTlLainnya.isSelected();
            TKpTindakanLanjutanKet.setVisible(lainnya);
            if (!lainnya) {
                TKpTindakanLanjutanKet.setText("");
            }
            panelKpTl.revalidate();
            panelKpTl.repaint();
            if (FormInput != null) {
                FormInput.revalidate();
                FormInput.repaint();
            }
            hitungProgresKep();
        };
        RbKpTlKamarOperasi.addItemListener(listenerKpTl);
        RbKpTlKamarBersalin.addItemListener(listenerKpTl);
        RbKpTlHcu.addItemListener(listenerKpTl);
        RbKpTlKamarJenazah.addItemListener(listenerKpTl);
        RbKpTlLainnya.addItemListener(listenerKpTl);
        row = tambahBarisKepField(panelKepEmpat, row, "Tindakan Lanjutan", panelKpTl);

        // Dirujuk ke RS & Alasan
        CekKpDirujuk = new widget.CekBox();
        CekKpDirujuk.setText("Dirujuk ke RS");
        TKpDirujukKet = new widget.TextBox();
        TKpDirujukKet.setPreferredSize(new java.awt.Dimension(180, 23));
        widget.PanelBiasa panelKpDirujuk = new widget.PanelBiasa();
        panelKpDirujuk.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelKpDirujuk.add(CekKpDirujuk);
        panelKpDirujuk.add(TKpDirujukKet);
        TKpDirujukAlasan = new widget.TextBox();
        TKpDirujukAlasan.setPreferredSize(new java.awt.Dimension(280, 23));

        final widget.PanelBiasa barisKpDirujuk = barisPengkajian(panelKepEmpat, row, "Rujukan ke RS", false, panelKpDirujuk);
        row++;

        selDirujukAlasan = barisPengkajian(panelKepEmpat, row, "Alasan Dirujuk", false, TKpDirujukAlasan);
        selDirujukAlasan.setVisible(false);
        row++;

        pasangKondisionalCek(CekKpDirujuk, TKpDirujukKet, selDirujukAlasan);

        // Kondisi Akhir: Dipulangkan, Meninggal, Death On Arrival
        CekKpDipulangkan = new widget.CekBox();
        CekKpDipulangkan.setText("Dipulangkan, Pukul");
        DTPKpDipulangkanPukul = new PanelTanggalDanJam(false, 23);
        widget.PanelBiasa panelKpDipulangkan = new widget.PanelBiasa();
        panelKpDipulangkan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelKpDipulangkan.add(CekKpDipulangkan);
        panelKpDipulangkan.add(DTPKpDipulangkanPukul);
        pasangKondisionalCek(CekKpDipulangkan, DTPKpDipulangkanPukul);

        CekKpMeninggal = new widget.CekBox();
        CekKpMeninggal.setText("Meninggal, Pukul");
        DTPKpMeninggalPukul = new PanelTanggalDanJam(false, 23);
        widget.PanelBiasa panelKpMeninggal = new widget.PanelBiasa();
        panelKpMeninggal.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        panelKpMeninggal.add(CekKpMeninggal);
        panelKpMeninggal.add(DTPKpMeninggalPukul);
        pasangKondisionalCek(CekKpMeninggal, DTPKpMeninggalPukul);

        CekKpDoa = new widget.CekBox();
        CekKpDoa.setText("Death On Arrival");

        widget.PanelBiasa panelKpAkhir = new widget.PanelBiasa();
        panelKpAkhir.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints cKpDipulangkan = kepConstraint(0, 0);
        cKpDipulangkan.anchor = java.awt.GridBagConstraints.WEST;
        cKpDipulangkan.fill = java.awt.GridBagConstraints.NONE;
        panelKpAkhir.add(panelKpDipulangkan, cKpDipulangkan);
        java.awt.GridBagConstraints cKpMeninggal = kepConstraint(1, 0);
        cKpMeninggal.anchor = java.awt.GridBagConstraints.WEST;
        cKpMeninggal.fill = java.awt.GridBagConstraints.NONE;
        cKpMeninggal.insets = new java.awt.Insets(3, 15, 3, 5);
        panelKpAkhir.add(panelKpMeninggal, cKpMeninggal);
        java.awt.GridBagConstraints cKpDoa = kepConstraint(2, 0);
        cKpDoa.anchor = java.awt.GridBagConstraints.WEST;
        cKpDoa.fill = java.awt.GridBagConstraints.NONE;
        cKpDoa.insets = new java.awt.Insets(3, 15, 3, 5);
        panelKpAkhir.add(CekKpDoa, cKpDoa);
        java.awt.GridBagConstraints cFillerKpAkhir = kepConstraint(3, 0);
        cFillerKpAkhir.weightx = 1.0;
        cFillerKpAkhir.fill = java.awt.GridBagConstraints.BOTH;
        panelKpAkhir.add(javax.swing.Box.createHorizontalGlue(), cFillerKpAkhir);

        row = tambahBarisKepField(panelKepEmpat, row, "Kondisi Akhir", panelKpAkhir);

        // Pendidikan Kesehatan Pasien Pulang
        row = tambahBarisKepJudul(panelKepEmpat, row, "PENDIDIKAN KESEHATAN PASIEN PULANG");
        CekKpEdukasiMakanObat = new widget.CekBox();
        CekKpEdukasiMakanObat.setText("Makan/Minum Obat Teratur");
        CekKpEdukasiMakanObat.setPreferredSize(new java.awt.Dimension(200, 26));
        CekKpEdukasiLuka = new widget.CekBox();
        CekKpEdukasiLuka.setText("Jaga Kebersihan Luka");
        CekKpEdukasiLuka.setPreferredSize(new java.awt.Dimension(200, 26));

        CekKpEdukasiDiet = new widget.CekBox();
        CekKpEdukasiDiet.setText("Diet");
        CekKpEdukasiDiet.setPreferredSize(new java.awt.Dimension(90, 26));
        TKpEdukasiDietKet = buatTextBoxKotakLokal(null);
        TKpEdukasiDietKet.setPreferredSize(new java.awt.Dimension(240, 26));
        TKpEdukasiDietKet.setPlaceholder("Keterangan Diet");
        TKpEdukasiDietKet.setVisible(false);
        pasangKondisionalCek(CekKpEdukasiDiet, TKpEdukasiDietKet);
        TKpEdukasiDietKet.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (TKpEdukasiDietKet.getText().length() > 0 && !CekKpEdukasiDiet.isSelected()) {
                    CekKpEdukasiDiet.setSelected(true);
                    hitungProgresKep();
                }
            }
        });

        CekKpEdukasiLain = new widget.CekBox();
        CekKpEdukasiLain.setText("Lain-lain");
        CekKpEdukasiLain.setPreferredSize(new java.awt.Dimension(90, 26));
        TKpEdukasiLainKet = buatTextBoxKotakLokal(null);
        TKpEdukasiLainKet.setPreferredSize(new java.awt.Dimension(240, 26));
        TKpEdukasiLainKet.setPlaceholder("Keterangan Edukasi Lain-lain");
        TKpEdukasiLainKet.setVisible(false);
        pasangKondisionalCek(CekKpEdukasiLain, TKpEdukasiLainKet);
        TKpEdukasiLainKet.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (TKpEdukasiLainKet.getText().length() > 0 && !CekKpEdukasiLain.isSelected()) {
                    CekKpEdukasiLain.setSelected(true);
                    hitungProgresKep();
                }
            }
        });

        widget.PanelBiasa panelKpEdu = new widget.PanelBiasa();
        panelKpEdu.setOpaque(false);
        panelKpEdu.setLayout(new java.awt.GridBagLayout());

        java.awt.GridBagConstraints ce = new java.awt.GridBagConstraints();
        ce.anchor = java.awt.GridBagConstraints.WEST;
        ce.fill = java.awt.GridBagConstraints.HORIZONTAL;

        // Baris 0: Makan/Minum Obat Teratur | Diet | [Keterangan Diet]
        ce.gridy = 0;
        ce.gridx = 0;
        ce.insets = new java.awt.Insets(0, 0, 8, 16);
        panelKpEdu.add(CekKpEdukasiMakanObat, ce);

        ce.gridx = 1;
        ce.insets = new java.awt.Insets(0, 0, 8, 8);
        panelKpEdu.add(CekKpEdukasiDiet, ce);

        ce.gridx = 2;
        ce.insets = new java.awt.Insets(0, 0, 8, 10);
        panelKpEdu.add(TKpEdukasiDietKet, ce);

        // Baris 1: Jaga Kebersihan Luka | Lain-lain | [Keterangan Edukasi Lain-lain]
        ce.gridy = 1;
        ce.gridx = 0;
        ce.insets = new java.awt.Insets(0, 0, 0, 16);
        panelKpEdu.add(CekKpEdukasiLuka, ce);

        ce.gridx = 1;
        ce.insets = new java.awt.Insets(0, 0, 0, 8);
        panelKpEdu.add(CekKpEdukasiLain, ce);

        ce.gridx = 2;
        ce.insets = new java.awt.Insets(0, 0, 0, 10);
        panelKpEdu.add(TKpEdukasiLainKet, ce);

        // Filler kanan
        ce.gridy = 0;
        ce.gridx = 3;
        ce.gridheight = 2;
        ce.weightx = 1.0;
        ce.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panelKpEdu.add(javax.swing.Box.createHorizontalGlue(), ce);

        row = tambahBarisKepField(panelKepEmpat, row, "Pendidikan Saat Pulang", false, panelKpEdu);

        // Dokter & Perawat yang bertanggung jawab dipindah ke header
        // (permintaan user) - lihat initKepTambahan() utk field Dokter,
        // header "Petugas" dipakai langsung sbg Perawat/Bidan di
        // nilaiKep(). Tidak ada lagi baris input di section ini.

        // panelKepEmpat (Kondisi Pasien Pindah/Pulang + Pendidikan Kesehatan
        // Pulang) dipecah per seksi oleh pecahSeksiKep() lalu ditempatkan
        // susunHalamanKep() - Y-math null-layout lama dinonaktifkan.
    }

    /**
     * Memperbarui visual toggle button skala nyeri 0-10 dan label interpretasi.
     */
    private void updateGayaSkalaNyeri() {
        if (btnSkalaNyeri == null) {
            return;
        }
        int terpilih = -1;
        for (int i = 0; i <= 10; i++) {
            if (btnSkalaNyeri[i] != null && btnSkalaNyeri[i].isSelected()) {
                terpilih = i;
                break;
            }
        }
        java.awt.Color garis = new java.awt.Color(0xD8, 0xDB, 0xD6);
        for (int i = 0; i <= 10; i++) {
            if (btnSkalaNyeri[i] == null) {
                continue;
            }
            if (i == terpilih) {
                java.awt.Color bgAktif;
                if (i == 0) {
                    bgAktif = widget.ThemeConfig.OK;
                } else if (i <= 3) {
                    bgAktif = new java.awt.Color(0x6B, 0x7A, 0x12);
                } else if (i <= 6) {
                    bgAktif = new java.awt.Color(0xC0, 0x8A, 0x2E);
                } else {
                    bgAktif = widget.ThemeConfig.DANGER;
                }
                btnSkalaNyeri[i].setBackground(bgAktif);
                btnSkalaNyeri[i].setForeground(java.awt.Color.WHITE);
                btnSkalaNyeri[i].setBorder(javax.swing.BorderFactory.createLineBorder(bgAktif));
            } else {
                btnSkalaNyeri[i].setBackground(java.awt.Color.WHITE);
                btnSkalaNyeri[i].setForeground(new java.awt.Color(0x4A, 0x55, 0x68));
                btnSkalaNyeri[i].setBorder(javax.swing.BorderFactory.createLineBorder(garis));
            }
            btnSkalaNyeri[i].repaint();
        }

        if (lblInterpretasiNyeri != null) {
            if (terpilih == 0) {
                lblInterpretasiNyeri.setText("Tidak Nyeri");
                lblInterpretasiNyeri.setForeground(widget.ThemeConfig.OK);
            } else if (terpilih >= 1 && terpilih <= 3) {
                lblInterpretasiNyeri.setText("Nyeri Ringan");
                lblInterpretasiNyeri.setForeground(new java.awt.Color(0x6B, 0x7A, 0x12));
            } else if (terpilih >= 4 && terpilih <= 6) {
                lblInterpretasiNyeri.setText("Nyeri Sedang");
                lblInterpretasiNyeri.setForeground(new java.awt.Color(0xC0, 0x8A, 0x2E));
            } else if (terpilih >= 7 && terpilih <= 10) {
                lblInterpretasiNyeri.setText("Nyeri Berat");
                lblInterpretasiNyeri.setForeground(widget.ThemeConfig.DANGER);
            } else {
                lblInterpretasiNyeri.setText("");
            }
        }
    }

    /**
     * Tandai atau batalkan tanda tidak relevan untuk satu bagian/halaman.
     */
    private void setHalamanRelevanKep(HalamanKep h, boolean relevan) {
        if (h.panel == null) {
            return;
        }
        for (SeksiKep s : h.anggota) {
            if (s.panel != null) {
                setKomponenEnabledRekursif(s.panel, relevan);
            }
        }
        latarKep(h.panel, relevan ? widget.ThemeConfig.SURFACE : new java.awt.Color(0xF6, 0xF7, 0xF4));
        h.panel.repaint();
    }

    private void setKomponenEnabledRekursif(java.awt.Container c, boolean enabled) {
        for (java.awt.Component k : c.getComponents()) {
            if (k instanceof javax.swing.JComponent && !(k instanceof widget.Label)) {
                k.setEnabled(enabled);
            }
            if (k instanceof java.awt.Container) {
                setKomponenEnabledRekursif((java.awt.Container) k, enabled);
            }
        }
    }

    /**
     * Menghitung progres 17 isian wajib dan opsional, serta memperbarui seluruh
     * indikator tampilan.
     */
    private void hitungProgresKep() {
        if (halamanKep == null || halamanKep.isEmpty()) {
            return;
        }

        int totalWajibSemua = 16;
        int totalTerisiWajib = 0;
        int totalOpsional = 0;

        for (int i = 0; i < halamanKep.size(); i++) {
            HalamanKep h = halamanKep.get(i);
            int wajib = 0;
            int terisi = 0;

            switch (i) {
                case 0: // 01 Riwayat Kesehatan & Alergi (2 wajib)
                    wajib = 2;
                    if (KeluhanUtama != null && !KeluhanUtama.getText().trim().isEmpty()) {
                        terisi++;
                    }
                    if (RbAlergiTidak != null && RbAlergiYa != null && TAlergiSebutkan != null) {
                        if (RbAlergiTidak.isSelected()
                                || (RbAlergiYa.isSelected() && !TAlergiSebutkan.getText().trim().isEmpty())) {
                            terisi++;
                        }
                    }
                    break;
                case 1: // 02 Keadaan Umum, Antropometri & Kehamilan (2 wajib)
                    wajib = 2;
                    if (TdKep != null && NadiKep != null && SuhuKep != null) {
                        if (!TdKep.getText().trim().isEmpty() || !NadiKep.getText().trim().isEmpty()
                                || !SuhuKep.getText().trim().isEmpty()) {
                            terisi++;
                        }
                    }
                    if (StatusKehamilan != null && StatusKehamilan.getSelectedIndex() >= 0) {
                        terisi++;
                    }
                    if (BbKep != null && TbKep != null && SpoKep != null) {
                        if (!BbKep.getText().trim().isEmpty() || !TbKep.getText().trim().isEmpty()
                                || !SpoKep.getText().trim().isEmpty()) {
                            totalOpsional++;
                        }
                    }
                    break;
                case 2: // 03 Asesmen Psikologis & Status Sosial-Ekonomi (1 wajib)
                    wajib = 1;
                    if (CekPsiTenang != null && (CekPsiTenang.isSelected() || CekPsiCemas.isSelected()
                            || CekPsiTakut.isSelected() || CekPsiSedih.isSelected() || CekPsiMarah.isSelected()
                            || CekPsiBunuhDiri.isSelected()
                            || (CekPsiMasalahPerilaku != null && CekPsiMasalahPerilaku.isSelected())
                            || (RbMasalahPrilakuTidak != null && RbMasalahPrilakuTidak.isSelected())
                            || (RbMasalahPrilakuYa != null && RbMasalahPrilakuYa.isSelected()))) {
                        terisi++;
                    }
                    if (RbNikahBelumMenikah != null && (RbNikahBelumMenikah.isSelected() || RbNikahMenikah.isSelected()
                            || RbNikahCerai.isSelected() || RbNikahLainnya.isSelected()
                            || RbTinggalSendiri.isSelected() || RbTinggalKeluarga.isSelected() || RbTinggalLainnya.isSelected())) {
                        totalOpsional++;
                    }
                    break;
                case 3: // 04 Skrining Geriatri & Risiko Jatuh (1 wajib)
                    wajib = 1;
                    if (RbJatuhTidakBeresiko != null
                            && (RbJatuhTidakBeresiko.isSelected() || RbJatuhBeresiko.isSelected())) {
                        terisi++;
                    }
                    if (RbJatuhCaraBerjalanYa != null
                            && (RbJatuhCaraBerjalanYa.isSelected() || RbJatuhCaraBerjalanTidak.isSelected())) {
                        totalOpsional++;
                    }
                    if (RbGeri1Ya != null && (RbGeri1Ya.isSelected() || RbGeri1Tidak.isSelected())) {
                        totalOpsional++;
                    }
                    break;
                case 4: // 05 Pengkajian Spiritual, Fungsional & Budaya (1 wajib)
                    wajib = 1;
                    if (CmbAgama != null && CmbAgama.getSelectedIndex() > 0) {
                        terisi++;
                    }
                    if (RbWajibIbadahBaligh != null
                            && (RbWajibIbadahBaligh.isSelected() || RbWajibIbadahBelumBaligh.isSelected()
                                || (RbWajibIbadahHalanganLain != null && RbWajibIbadahHalanganLain.isSelected()
                                    && !TKetWajibIbadah.getText().trim().isEmpty()))) {
                        totalOpsional++;
                    }
                    break;
                case 5: // 06 Skrining Gizi (MST / Anak)
                    boolean dewasaAktif = RbPilihGiziDewasa != null && RbPilihGiziDewasa.isSelected();
                    boolean anakAktif = RbPilihGiziAnak != null && RbPilihGiziAnak.isSelected();
                    if (dewasaAktif) {
                        wajib = 3;
                        if (RbMstTidakAda != null
                                && (RbMstTidakAda.isSelected() || RbMstTidakYakin.isSelected() || RbMstYa.isSelected())) {
                            terisi++;
                        }
                        if (RbMstAsupanYa != null && (RbMstAsupanYa.isSelected() || RbMstAsupanTidak.isSelected())) {
                            terisi++;
                        }
                        if (RbMstRisiko != null && (RbMstRisiko.isSelected() || RbMstTidakRisiko.isSelected())) {
                            terisi++;
                        }
                    } else if (anakAktif) {
                        wajib = 4;
                        if (RbGiziAnakKurusYa != null
                                && (RbGiziAnakKurusYa.isSelected() || RbGiziAnakKurusTidak.isSelected())) {
                            terisi++;
                        }
                        if (RbGiziAnakTurunBbYa != null
                                && (RbGiziAnakTurunBbYa.isSelected() || RbGiziAnakTurunBbTidak.isSelected())) {
                            terisi++;
                        }
                        if (RbGiziAnakKondisiYa != null
                                && (RbGiziAnakKondisiYa.isSelected() || RbGiziAnakKondisiTidak.isSelected())) {
                            terisi++;
                        }
                        if (RbGiziAnakRisikoYa != null
                                && (RbGiziAnakRisikoYa.isSelected() || RbGiziAnakRisikoTidak.isSelected())) {
                            terisi++;
                        }
                    } else {
                        wajib = 1;
                        terisi = 0;
                    }
                    break;
                case 6: // 07 Asesmen Nyeri (PQRST / FLACC)
                    if (RbNyeriAdaTidak != null && RbNyeriAdaTidak.isSelected()) {
                        wajib = 1;
                        terisi = 1;
                    } else if (RbNyeriAdaYa != null && RbNyeriAdaYa.isSelected()) {
                        wajib = 2;
                        terisi = 1;
                        String katUsia = CmbKategoriUsiaNyeri != null && CmbKategoriUsiaNyeri.getSelectedItem() != null
                                ? CmbKategoriUsiaNyeri.getSelectedItem().toString() : "";
                        if ("Bayi - Anak < 3 Tahun".equals(katUsia)) {
                            if (TFlaccTotal != null && !TFlaccTotal.getText().trim().isEmpty()) {
                                terisi++;
                            }
                        } else if ("Usia 3-8 Tahun".equals(katUsia) || "Usia > 8 Tahun".equals(katUsia)) {
                            if (SkalaNyeri != null && SkalaNyeri.getSelectedItem() != null
                                    && !SkalaNyeri.getSelectedItem().toString().trim().isEmpty()) {
                                terisi++;
                            }
                        }
                    } else {
                        wajib = 1;
                        terisi = 0;
                    }
                    if (CekProvCahaya != null && (CekProvCahaya.isSelected() || CekProvGelap.isSelected()
                            || CekProvGerakan.isSelected() || CekProvBerbaring.isSelected())) {
                        totalOpsional++;
                    }
                    break;
                case 7: // 08 I–III. Diagnosis, Intervensi & Tindakan Mandiri (2 wajib)
                    wajib = 2;
                    if (tabModeMasalah != null && tabModeMasalah.getRowCount() > 0) {
                        boolean adaPilih = false;
                        for (int r = 0; r < tabModeMasalah.getRowCount(); r++) {
                            if (Boolean.TRUE.equals(tabModeMasalah.getValueAt(r, 0))) {
                                adaPilih = true;
                                break;
                            }
                        }
                        if (adaPilih) {
                            terisi++;
                        }
                    }
                    if (CekIntervManajemenJalanNafas != null && (CekIntervManajemenJalanNafas.isSelected()
                            || CekIntervSuctioningJalanNafas.isSelected() || CekIntervManajemenAsamBasa.isSelected()
                            || CekIntervManajemenSyok.isSelected() || CekIntervManajemenNyeri.isSelected()
                            || CekIntervManajemenCairan.isSelected() || CekIntervMonitoringCairan.isSelected()
                            || CekIntervManajemenLingkungan.isSelected() || CekIntervPendidikanKesehatan.isSelected()
                            || CekIntervFeverTreatment.isSelected() || CekIntervManajemenSensasiPerifer.isSelected()
                            || CekIntervMonitoringKardiovaskuler.isSelected()
                            || CekIntervPencegahanPenekananLuka.isSelected()
                            || (TIntervLainnya != null && !TIntervLainnya.getText().trim().isEmpty()))) {
                        terisi++;
                    }
                    break;
                case 8: // 09 IV–IX. Tindakan Klinis & Kolaborasi (0 wajib)
                    wajib = 0;
                    if (CekKardioPulseOximetri != null
                            && (CekKardioPulseOximetri.isSelected() || CekPenunjangCekLab.isSelected()
                                    || CekKolabO2.isSelected() || CekTraumaCervicalCollar.isSelected()
                                    || CekThtIrigasiMataTelinga.isSelected() || CekObgynMembantuPartus.isSelected())) {
                        totalOpsional++;
                    }
                    break;
                case 9: // 10 Pemberian Obat / Infus (0 wajib)
                    wajib = 0;
                    int rowObat = tabModeObatInfus != null ? tabModeObatInfus.getRowCount() : 0;
                    if (rowObat > 0) {
                        totalOpsional++;
                    }
                    h.subjudul = rowObat + " entri dicatat";
                    break;
                case 10: // 11 Kondisi Pasien Saat Pindah / Pulang dari IGD (2 wajib)
                    wajib = 2;
                    if (TKpTd != null && (!TKpTd.getText().trim().isEmpty() || !TKpNadi.getText().trim().isEmpty()
                            || !TKpGcsE.getText().trim().isEmpty() || !TKpSuhu.getText().trim().isEmpty())) {
                        terisi++;
                    }
                    if (CekKpMasukRs != null && (CekKpMasukRs.isSelected() || CekKpDipulangkan.isSelected()
                            || CekKpMeninggal.isSelected() || CekKpDoa.isSelected() || CekKpDirujuk.isSelected())) {
                        terisi++;
                    }
                    break;
            }

            h.totalWajib = wajib;
            if (h.tidakRelevan) {
                h.terisiWajib = wajib;
            } else {
                h.terisiWajib = Math.min(wajib, terisi);
            }
            totalTerisiWajib += h.terisiWajib;

            // Perbarui Rail row
            if (h.lblHitunganRail != null) {
                if (h.tidakRelevan) {
                    h.lblHitunganRail.setText("N/A");
                    h.lblHitunganRail.setForeground(widget.ThemeConfig.TEXT_FAINT);
                } else if (wajib == 0) {
                    h.lblHitunganRail.setText("-");
                    h.lblHitunganRail.setForeground(widget.ThemeConfig.TEXT_FAINT);
                } else {
                    h.lblHitunganRail.setText(h.terisiWajib + "/" + wajib);
                    h.lblHitunganRail.setForeground(
                            h.terisiWajib >= wajib ? widget.ThemeConfig.OK : new java.awt.Color(0xC0, 0x8A, 0x2E));
                }
            }

            // Perbarui Card Status Badge
            if (h.lblBadgeStatus != null && h.pnlBadgeStatus != null) {
                if (h.tidakRelevan) {
                    h.pnlBadgeStatus.setBackground(new java.awt.Color(0xF2, 0xF4, 0xF7));
                    h.lblBadgeStatus.setText("Tidak relevan");
                    h.lblBadgeStatus.setForeground(widget.ThemeConfig.TEXT_FAINT);
                } else if (wajib == 0) {
                    h.pnlBadgeStatus.setBackground(new java.awt.Color(0xF2, 0xF4, 0xF7));
                    h.lblBadgeStatus.setText("Opsional");
                    h.lblBadgeStatus.setForeground(widget.ThemeConfig.TEXT_FAINT);
                } else if (h.terisiWajib >= wajib) {
                    h.pnlBadgeStatus.setBackground(new java.awt.Color(0xEB, 0xF7, 0xEE));
                    h.lblBadgeStatus.setText("Wajib lengkap");
                    h.lblBadgeStatus.setForeground(widget.ThemeConfig.OK);
                } else {
                    h.pnlBadgeStatus.setBackground(new java.awt.Color(0xFE, 0xF3, 0xE2));
                    h.lblBadgeStatus.setText(h.terisiWajib + "/" + wajib + " wajib terisi");
                    h.lblBadgeStatus.setForeground(new java.awt.Color(0xC0, 0x8A, 0x2E));
                }
                h.pnlBadgeStatus.repaint();
            }

            if (h.lblBadgeNomor != null) {
                h.lblBadgeNomor.setBackground(
                        h.tidakRelevan ? new java.awt.Color(0xA0, 0xAE, 0xC0) : widget.ThemeConfig.PRIMARY);
                h.lblBadgeNomor.repaint();
            }
        }

        int persen = (totalTerisiWajib * 100) / totalWajibSemua;
        if (lblProgresPersenKep != null) {
            lblProgresPersenKep.setText(persen + "%");
        }
        if (pbProgresKep != null) {
            pbProgresKep.setValue(persen);
            pbProgresKep.setForeground(persen >= 100 ? widget.ThemeConfig.OK : widget.ThemeConfig.PRIMARY);
        }
        if (lblProgresJudulKep != null) {
            lblProgresJudulKep.setText(persen >= 100 ? "SEMUA ISIAN WAJIB TERPENUHI" : "ISIAN WAJIB");
        }
        if (lblProgresHitungKep != null) {
            lblProgresHitungKep.setText(totalTerisiWajib + "/" + totalWajibSemua + " isian wajib · "
                    + (totalOpsional > 0 ? totalOpsional : 1) + " opsional terisi");
        }
        if (lblProgresBawahKep != null) {
            if (totalTerisiWajib >= totalWajibSemua) {
                lblProgresBawahKep.setText("Isian wajib lengkap · siap disimpan");
                lblProgresBawahKep.setForeground(widget.ThemeConfig.OK);
            } else {
                lblProgresBawahKep.setText("Isian wajib " + totalTerisiWajib + "/" + totalWajibSemua + " - "
                        + (totalWajibSemua - totalTerisiWajib) + " belum terisi");
                lblProgresBawahKep.setForeground(widget.ThemeConfig.TEXT_MUTED);
            }
        }
    }

    private void pasangListenerProgresKep(java.awt.Component comp) {
        if (comp instanceof javax.swing.text.JTextComponent) {
            ((javax.swing.text.JTextComponent) comp).getDocument()
                    .addDocumentListener(new javax.swing.event.DocumentListener() {
                        @Override
                        public void insertUpdate(javax.swing.event.DocumentEvent e) {
                            hitungProgresKep();
                        }

                        @Override
                        public void removeUpdate(javax.swing.event.DocumentEvent e) {
                            hitungProgresKep();
                        }

                        @Override
                        public void changedUpdate(javax.swing.event.DocumentEvent e) {
                            hitungProgresKep();
                        }
                    });
        } else if (comp instanceof javax.swing.AbstractButton) {
            ((javax.swing.AbstractButton) comp).addItemListener((java.awt.event.ItemEvent e) -> hitungProgresKep());
        } else if (comp instanceof widget.ComboBox) {
            ((widget.ComboBox) comp).addItemListener((java.awt.event.ItemEvent e) -> hitungProgresKep());
        }
    }

    private void pasangSemuaListenerProgresKep() {
        javax.swing.text.JTextComponent[] texts = {
                KeluhanUtama, TAlergiSebutkan, TdKep, NadiKep, SuhuKep, SpoKep, RrKep, BbKep, TbKep,
                TMasalahPrilakuSebutkan, TKetStatusPernikahan, TKetTempatTinggal, TPekerjaanKep,
                TAgama, TKetWajibIbadah, TKetNilaiKepercayaan, KetBudaya, TMstTotalSkor, TMstDiagnosisSebutkan,
                TGiziAnakTotalSkor, Lokasi, KetNyeri, TFlaccTotal, TRadiationKe, TIntervLainnya,
                TKpTd, TKpNadi, TKpSuhu, TKpPernafasan, TKpSpo, TKpGcsE, TKpGcsV, TKpGcsM,
                TKpRuang, TKpPetugasPenerima
        };
        for (javax.swing.text.JTextComponent t : texts) {
            if (t != null) {
                pasangListenerProgresKep(t);
            }
        }

        javax.swing.AbstractButton[] btns = {
                RbAlergiTidak, RbAlergiYa, RbMasalahPrilakuTidak, RbMasalahPrilakuYa,
                CekPsiTenang, CekPsiCemas, CekPsiTakut, CekPsiSedih, CekPsiMarah, CekPsiBunuhDiri, CekPsiMasalahPerilaku,
                RbNikahBelumMenikah, RbNikahMenikah, RbNikahCerai, RbNikahLainnya,
                RbTinggalSendiri, RbTinggalKeluarga, RbTinggalLainnya,
                RbGeri1Ya, RbGeri1Tidak, RbGeri2Ya, RbGeri2Tidak, RbGeri3Ya, RbGeri3Tidak,
                RbJatuhCaraBerjalanYa, RbJatuhCaraBerjalanTidak, RbJatuhMenopangYa, RbJatuhMenopangTidak,
                RbJatuhTidakBeresiko, RbJatuhBeresiko,
                RbNilaiKepercayaanTidak, RbNilaiKepercayaanYa,
                RbBudayaTidak, RbBudayaAda,
                RbPilihGiziDewasa, RbPilihGiziAnak,
                RbMstTidakAda, RbMstTidakYakin, RbMstYa, RbMstAsupanYa, RbMstAsupanTidak,
                RbMstDiagnosisYa, RbMstDiagnosisTidak, RbMstRisiko, RbMstTidakRisiko,
                RbGiziAnakKurusYa, RbGiziAnakKurusTidak, RbGiziAnakTurunBbYa, RbGiziAnakTurunBbTidak,
                RbGiziAnakKondisiYa, RbGiziAnakKondisiTidak, RbGiziAnakRisikoYa, RbGiziAnakRisikoTidak,
                RbGiziAnakRendah, RbGiziAnakSedang, RbGiziAnakTinggi,
                RbNyeriAdaYa, RbNyeriAdaTidak,
                CekIntervManajemenJalanNafas, CekIntervSuctioningJalanNafas, CekIntervManajemenAsamBasa,
                CekIntervManajemenSyok, CekIntervManajemenNyeri, CekIntervManajemenCairan,
                CekIntervMonitoringCairan, CekIntervManajemenLingkungan, CekIntervPendidikanKesehatan,
                CekIntervFeverTreatment, CekIntervManajemenSensasiPerifer, CekIntervMonitoringKardiovaskuler,
                CekIntervPencegahanPenekananLuka,
                CekKpMasukRs, CekKpDipulangkan, CekKpMeninggal, CekKpDoa, CekKpDirujuk
        };
        for (javax.swing.AbstractButton b : btns) {
            if (b != null) {
                pasangListenerProgresKep(b);
            }
        }

        widget.ComboBox[] combos = {
                StatusKehamilan, CmbAgama, StatusBudaya, SkalaNyeri, MstPenurunanBbKg
        };
        for (widget.ComboBox c : combos) {
            if (c != null) {
                pasangListenerProgresKep(c);
            }
        }

        if (tabModeMasalah != null) {
            tabModeMasalah.addTableModelListener((javax.swing.event.TableModelEvent e) -> hitungProgresKep());
        }
        if (tabModeObatInfus != null) {
            tabModeObatInfus.addTableModelListener((javax.swing.event.TableModelEvent e) -> hitungProgresKep());
        }
    }

    /**
     * [Lanjutan RM 03-6] "Pemberian Obat / Infus" - tabel berulang (1
     * baris per pemberian obat/infus), CRUD independen dari record utama
     * (BUKAN bagian dari simpan()/ganti() record penilaian_awal_
     * keperawatan_igd - langsung tersimpan ke tabel anak begitu tombol
     * Tambah diklik). Pola disalin dari `intervensi_nyeri_farmakologi` /
     * RMDataIntervensiNyeriFarmakologi.java, ditempel sbg panel BARU di
     * bawah panelKepTiga (sebelum panelKepEmpat/Kondisi Pulang - mengikuti
     * urutan fisik: Pemberian Obat/Infus dulu, baru Kondisi Pulang).
     */
    private void initObatInfusTambahan() {
        panelKepObat = new widget.PanelBiasa();
        panelKepObat.setLayout(new java.awt.GridBagLayout());
        int row = 0;
        row = tambahBarisKepJudul(panelKepObat, row, "PEMBERIAN OBAT / INFUS");

        // [Bagian F butir 1] Kolom ke-7 "" = tombol hapus per baris. Sengaja
        // TANPA judul: "HAPUS" di kolom 34px pasti terpotong. Aman thd
        // penyimpanan - tampilObatInfus()/simpanObat() tetap addRow 6 nilai
        // dan DefaultTableModel.justifyRows() menambal sisanya null, sedangkan
        // renderer kolom ini mengabaikan isi selnya (selalu melukis tanda X).
        tabModeObatInfus = new DefaultTableModel(null,
                new Object[] { "Pukul", "Nama Obat/Infus", "Dosis", "Rute", "Diperiksa Oleh", "Diberikan Oleh", "" }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObatInfus = new widget.Table() {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0 && col < getColumnCount() - 1) {
                    Object val = getValueAt(row, col);
                    if (val != null && !val.toString().trim().isEmpty()) {
                        return val.toString().trim();
                    }
                }
                return super.getToolTipText(e);
            }
        };
        tbObatInfus.setModel(tabModeObatInfus);
        gayaTabelObatKep();
        pasangKlikHapusObatKep();

        // [Bagian F butir 5] Label "Belum ada data obat/infus" DILUKIS OLEH
        // SCROLL PANE, bukan oleh tabelnya: dgn 0 baris tinggi JTable = 0px,
        // jadi apa pun yg digambar di paintComponent() tabel tidak punya
        // bidang utk muncul. Digambar SESUDAH super.paintChildren() supaya
        // duduk di atas viewport (ViewPortGlass melukis latarnya sendiri).
        widget.ScrollPane scrollObatInfus = new widget.ScrollPane() {
            @Override
            protected void paintChildren(java.awt.Graphics g) {
                super.paintChildren(g);
                if (tabModeObatInfus == null || tabModeObatInfus.getRowCount() > 0) {
                    return;
                }
                java.awt.Rectangle vp = getViewport().getBounds();
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
                g2.setColor(widget.ThemeConfig.TEXT_FAINT);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String pesan = "Belum ada data obat/infus";
                g2.drawString(pesan, vp.x + Math.max(0, (vp.width - fm.stringWidth(pesan)) / 2),
                        vp.y + (vp.height + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        scrollObatInfus.setViewportView(tbObatInfus);
        scrollObatInfus.setBorder(javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT));
        // Model dikosongkan/diisi dari getData()/emptTeks()/tampilObatInfus()
        // tanpa menyentuh scroll pane-nya, jadi repaint diikat ke model.
        tabModeObatInfus.addTableModelListener((javax.swing.event.TableModelEvent e) -> scrollObatInfus.repaint());
        // Tinggi: kepala 30 + 4 baris x 30 = 150 (baris naik dari 22 ke 30px
        // sesuai butir 2). Lebarnya tetap diserahkan ke rapikanIsiKep().
        scrollObatInfus.setPreferredSize(new java.awt.Dimension(
                scrollObatInfus.getPreferredSize().width, 150));
        java.awt.GridBagConstraints cTabelObat = kepConstraint(0, row);
        cTabelObat.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        panelKepObat.add(scrollObatInfus, cTabelObat);
        row++;

        // [Bagian F butir 3] Enam isian tinggi 30px, disusun 4 sel mini-label
        // (Pukul|Nama|Dosis|Rute) + 2 sel (Diperiksa|Diberikan) - bukan lagi
        // "label :" rata KANAN. Dua panel terpisah supaya lebar kolomnya
        // dihitung sendiri2: sel "Diperiksa Oleh" isinya ~310px (NIP+nama+
        // tombol cari), kalau sebaris grid dgn Pukul maka kolom 0 melebar
        // 310px utk semuanya dan Pukul kebagian 160px kosong.
        //
        // Keduanya dibungkus SATU strip berlatar SURFACE_ALT + LineBorder
        // BORDER_SOFT (bunyi butir 3), supaya "baris input" terbaca sebagai
        // satu area kerja yg terpisah dari tabel di atasnya. CompoundBorder,
        // bukan LineBorder telanjang: padding dalamnya yg menjauhkan isian
        // dari garis strip.
        widget.PanelBiasa stripInputObat = new widget.PanelBiasa();
        stripInputObat.setLayout(new java.awt.GridBagLayout());
        latarKep(stripInputObat, widget.ThemeConfig.SURFACE_ALT);
        stripInputObat.setBorder(new javax.swing.border.CompoundBorder(
                javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
                javax.swing.BorderFactory.createEmptyBorder(6, 10, 8, 10)));

        DTPObatPukul = new PanelJamSaja(true, 30);
        DTPObatPukul.setToolTipText("Pukul pemberian obat/infus");

        TObatNama = new widget.TextBox();
        TObatNama.setPreferredSize(new java.awt.Dimension(200, 30));
        TObatNama.setPlaceholder("Nama Obat/Infus");
        TObatNama.putClientProperty("JTextField.placeholderText", "Nama Obat/Infus");
        buatKotakKep(TObatNama);

        BtnObatNama = new widget.Button();
        BtnObatNama.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnObatNama.setPreferredSize(new java.awt.Dimension(28, 30));
        buatKotakKep(BtnObatNama);
        BtnObatNama.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariNamaObat dlgObat = new DlgCariNamaObat(null, false);
            dlgObat.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (dlgObat.getTable().getSelectedRow() != -1) {
                        TObatNama.setText(
                                dlgObat.getTable().getValueAt(dlgObat.getTable().getSelectedRow(), 1).toString());
                    }
                    BtnObatNama.requestFocus();
                }
            });
            dlgObat.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            dlgObat.setLocationRelativeTo(internalFrame1);
            dlgObat.setVisible(true);
        });
        widget.PanelBiasa panelObatNama = new widget.PanelBiasa();
        panelObatNama.setOpaque(false);
        panelObatNama.setBorder(null);
        panelObatNama.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));
        panelObatNama.add(TObatNama);
        panelObatNama.add(BtnObatNama);

        TObatDosis = new widget.TextBox();
        TObatDosis.setPreferredSize(new java.awt.Dimension(90, 30));
        TObatDosis.setPlaceholder("Dosis");
        TObatDosis.putClientProperty("JTextField.placeholderText", "Dosis");
        buatKotakKep(TObatDosis);

        TObatRute = new widget.TextBox();
        TObatRute.setPreferredSize(new java.awt.Dimension(90, 30));
        TObatRute.setPlaceholder("Rute");
        TObatRute.putClientProperty("JTextField.placeholderText", "Rute");
        buatKotakKep(TObatRute);

        KdObatDiperiksa = new widget.TextBox();
        KdObatDiperiksa.setPreferredSize(new java.awt.Dimension(0, 0));
        KdObatDiperiksa.setVisible(false);
        KdObatDiperiksa.setEditable(false);

        NmObatDiperiksa = new widget.TextBox();
        NmObatDiperiksa.setPreferredSize(new java.awt.Dimension(165, 30));
        NmObatDiperiksa.setPlaceholder("Diperiksa Oleh");
        NmObatDiperiksa.putClientProperty("JTextField.placeholderText", "Diperiksa Oleh");
        NmObatDiperiksa.setToolTipText("Diperiksa Oleh");
        NmObatDiperiksa.setEditable(false);
        tandaiReadOnlyKep(NmObatDiperiksa);
        buatKotakKep(NmObatDiperiksa);
        NmObatDiperiksa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateTip() {
                String t = NmObatDiperiksa.getText().trim();
                NmObatDiperiksa.setToolTipText(t.isEmpty() ? "Diperiksa Oleh" : t);
                NmObatDiperiksa.setCaretPosition(0);
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTip(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTip(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTip(); }
        });

        BtnObatDiperiksa = new widget.Button();
        BtnObatDiperiksa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnObatDiperiksa.setPreferredSize(new java.awt.Dimension(28, 30));
        buatKotakKep(BtnObatDiperiksa);
        BtnObatDiperiksa.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariPetugas dlg = new DlgCariPetugas(null, false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (dlg.getTable().getSelectedRow() != -1) {
                        String nip = dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(), 0).toString();
                        String nama = dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(), 1).toString();
                        KdObatDiperiksa.setText(nip);
                        NmObatDiperiksa.setText(nama);
                        NmObatDiperiksa.setToolTipText(nama);
                        NmObatDiperiksa.setCaretPosition(0);
                    }
                    BtnObatDiperiksa.requestFocus();
                }
            });
            dlg.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        widget.PanelBiasa panelObatDiperiksa = new widget.PanelBiasa();
        panelObatDiperiksa.setOpaque(false);
        panelObatDiperiksa.setBorder(null);
        panelObatDiperiksa.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));
        panelObatDiperiksa.add(KdObatDiperiksa);
        panelObatDiperiksa.add(NmObatDiperiksa);
        panelObatDiperiksa.add(BtnObatDiperiksa);

        KdObatDiberikan = new widget.TextBox();
        KdObatDiberikan.setPreferredSize(new java.awt.Dimension(0, 0));
        KdObatDiberikan.setVisible(false);
        KdObatDiberikan.setEditable(false);

        NmObatDiberikan = new widget.TextBox();
        NmObatDiberikan.setPreferredSize(new java.awt.Dimension(165, 30));
        NmObatDiberikan.setPlaceholder("Diberikan Oleh");
        NmObatDiberikan.putClientProperty("JTextField.placeholderText", "Diberikan Oleh");
        NmObatDiberikan.setToolTipText("Diberikan Oleh");
        NmObatDiberikan.setEditable(false);
        tandaiReadOnlyKep(NmObatDiberikan);
        buatKotakKep(NmObatDiberikan);
        NmObatDiberikan.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateTip() {
                String t = NmObatDiberikan.getText().trim();
                NmObatDiberikan.setToolTipText(t.isEmpty() ? "Diberikan Oleh" : t);
                NmObatDiberikan.setCaretPosition(0);
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTip(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTip(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTip(); }
        });

        BtnObatDiberikan = new widget.Button();
        BtnObatDiberikan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnObatDiberikan.setPreferredSize(new java.awt.Dimension(28, 30));
        buatKotakKep(BtnObatDiberikan);
        BtnObatDiberikan.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariPetugas dlg = new DlgCariPetugas(null, false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (dlg.getTable().getSelectedRow() != -1) {
                        String nip = dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(), 0).toString();
                        String nama = dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(), 1).toString();
                        KdObatDiberikan.setText(nip);
                        NmObatDiberikan.setText(nama);
                        NmObatDiberikan.setToolTipText(nama);
                        NmObatDiberikan.setCaretPosition(0);
                    }
                    BtnObatDiberikan.requestFocus();
                }
            });
            dlg.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        widget.PanelBiasa panelObatDiberikan = new widget.PanelBiasa();
        panelObatDiberikan.setOpaque(false);
        panelObatDiberikan.setBorder(null);
        panelObatDiberikan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));
        panelObatDiberikan.add(KdObatDiberikan);
        panelObatDiberikan.add(NmObatDiberikan);
        panelObatDiberikan.add(BtnObatDiberikan);

        final java.awt.Color BG_TAMBAH_OBAT = new java.awt.Color(0x0D, 0x7A, 0x70);
        final java.awt.Color BG_HOVER_OBAT  = new java.awt.Color(0x0F, 0x8A, 0x7E);
        final java.awt.Color BG_PRESS_OBAT  = new java.awt.Color(0x0A, 0x61, 0x59);

        BtnTambahObat = new widget.Button() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                try {
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed()) {
                        g2.setColor(BG_PRESS_OBAT);
                    } else if (getModel().isRollover()) {
                        g2.setColor(BG_HOVER_OBAT);
                    } else {
                        g2.setColor(BG_TAMBAH_OBAT);
                    }
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } finally {
                    g2.dispose();
                }

                if (getUI() != null) {
                    java.awt.Graphics gUI = g.create();
                    try {
                        getUI().paint(gUI, this);
                    } finally {
                        gUI.dispose();
                    }
                }
            }

            @Override
            public void setIcon(javax.swing.Icon icon) {
                // Abaikan icon eksternal (mis. IconStyle) supaya teks '+ Tambah' murni putih tanpa dobel plus
                super.setIcon(null);
            }

            @Override
            protected void paintBorder(java.awt.Graphics g) {
                // Polos tanpa gradasi / border 3D bawaan ButtonGlass
            }
        };
        BtnTambahObat.setText("+ Tambah");
        BtnTambahObat.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        BtnTambahObat.setForeground(java.awt.Color.WHITE);
        BtnTambahObat.setPreferredSize(new java.awt.Dimension(96, 30));
        BtnTambahObat.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        BtnTambahObat.setOpaque(false);
        BtnTambahObat.setContentAreaFilled(false);
        BtnTambahObat.setFocusPainted(false);
        BtnTambahObat.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
        buatKotakKep(BtnTambahObat);
        BtnTambahObat.addActionListener((java.awt.event.ActionEvent evt) -> simpanObat());

        // [Bagian F butir 3] Susun seluruh isian strip input dalam 1 BARIS horizontal rapi sesuai mockup:
        // [ Pukul ] [ Nama Obat/Infus ] [ Dosis ] [ Rute ] [ Diperiksa Oleh ] [ Diberikan Oleh ] [ + Tambah ]
        stripInputObat.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints cStrip = new java.awt.GridBagConstraints();
        cStrip.gridy = 0;
        cStrip.anchor = java.awt.GridBagConstraints.WEST;
        cStrip.fill = java.awt.GridBagConstraints.NONE;

        cStrip.gridx = 0;
        cStrip.insets = new java.awt.Insets(0, 0, 0, 6);
        stripInputObat.add(DTPObatPukul, cStrip);

        cStrip.gridx = 1;
        cStrip.insets = new java.awt.Insets(0, 0, 0, 6);
        stripInputObat.add(panelObatNama, cStrip);

        cStrip.gridx = 2;
        cStrip.insets = new java.awt.Insets(0, 0, 0, 6);
        stripInputObat.add(TObatDosis, cStrip);

        cStrip.gridx = 3;
        cStrip.insets = new java.awt.Insets(0, 0, 0, 6);
        stripInputObat.add(TObatRute, cStrip);

        cStrip.gridx = 4;
        cStrip.insets = new java.awt.Insets(0, 0, 0, 6);
        stripInputObat.add(panelObatDiperiksa, cStrip);

        cStrip.gridx = 5;
        cStrip.insets = new java.awt.Insets(0, 0, 0, 6);
        stripInputObat.add(panelObatDiberikan, cStrip);

        cStrip.gridx = 6;
        cStrip.insets = new java.awt.Insets(0, 0, 0, 0);
        stripInputObat.add(BtnTambahObat, cStrip);

        cStrip.gridx = 7;
        cStrip.weightx = 1.0;
        cStrip.fill = java.awt.GridBagConstraints.HORIZONTAL;
        stripInputObat.add(javax.swing.Box.createHorizontalGlue(), cStrip);

        java.awt.GridBagConstraints cStripInputObat = kepConstraint(0, row);
        cStripInputObat.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cStripInputObat.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cStripInputObat.insets = new java.awt.Insets(8, 5, 0, 5);
        panelKepObat.add(stripInputObat, cStripInputObat);
        row++;

        // BtnHapusObat TETAP DIBUAT lengkap dgn listener-nya - aturan baku
        // prompt: komponen yg digantikan tampilannya hanya setVisible(false),
        // tidak dihapus. Penggantinya kolom "✕" per baris di tabel (butir 4).
        // Panel tombol lamanya hilang seluruhnya: penghuni satunya lagi,
        // "+ Tambah", sekarang duduk di dalam strip input.
        BtnHapusObat = new widget.Button();
        BtnHapusObat.setText("Hapus Baris Terpilih");
        BtnHapusObat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Delete.png")));
        BtnHapusObat.setPreferredSize(new java.awt.Dimension(180, 28));
        BtnHapusObat.addActionListener((java.awt.event.ActionEvent evt) -> hapusObat());
        BtnHapusObat.setVisible(false);

        // panelKepObat (Pemberian Obat/Infus) jadi seksi tersendiri lewat
        // pecahSeksiKep() lalu ditempatkan susunHalamanKep() - Y-math
        // null-layout lama dinonaktifkan.
    }

    /**
     * Lebar tiap kolom tabel obat/infus, urutan PERSIS Bagian F butir 1:
     * Pukul | Nama Obat/Infus | Dosis | Rute | Diperiksa Oleh | Diberikan Oleh
     * | (hapus). Nilai -1 berarti "biarkan melar": enam kolom lainnya dipatok
     * min = max = preferred, sehingga SELURUH sisa lebar jatuh ke Nama
     * Obat/Infus - itulah kolom yg isinya paling panjang dan paling sering
     * terpotong di tata letak lama.
     */
    private static final int[] LEBAR_KOLOM_OBAT_KEP = { 88, -1, 96, 96, 150, 150, 48 };

    /**
     * [Bagian F butir 2] Gaya tabel yg dipakai form ini: kepala terang
     * SURFACE_ALT + teks 10,5px bold uppercase TEXT_MUTED, baris tinggi
     * `tinggiBaris`, gridline 0xF0F2EE tanpa garis vertikal, seleksi 0xF2F7FB.
     *
     * Dipisah jadi helper krn dipakai DUA tabel: tbObatInfus (halaman 11) dan
     * tbMasalahKeperawatan (halaman 10). Bagian F cuma menyebut tabel obat,
     * tapi keduanya tampil di form yg sama dgn selisih satu halaman - kepala
     * NAVY gelap bawaan widget.Table di sebelah kepala terang yg baru bukan
     * "dua tabel", tapi satu form yg terlihat setengah dikerjakan. Kepala
     * gelap itu juga lebih pekat drpd strip kartu halamannya sendiri, jadi
     * hierarki warnanya terbalik.
     *
     * Semuanya per instans: widget.Table dipakai ratusan form lain (kepala
     * NAVY, rowHeight 22), menyetel gaya ini di widget-nya akan mengubah
     * SELURUH aplikasi.
     *
     * setSelectionForeground() WAJIB ikut diganti: widget.Table mewarisi teks
     * seleksi PUTIH (utk latar seleksi NAVY-nya), sedangkan seleksi kita
     * 0xF2F7FB yg hampir putih - tanpa ini baris terpilih jadi putih di atas
     * putih, jadi teksnya HILANG tepat saat user memilih baris.
     */
    private void gayaTabelKep(JTable tabel, int tinggiBaris) {
        tabel.setRowHeight(tinggiBaris);
        tabel.setGridColor(new java.awt.Color(0xF0F2EE));
        tabel.setShowVerticalLines(false);
        tabel.setIntercellSpacing(new java.awt.Dimension(0, 1));
        tabel.setSelectionBackground(new java.awt.Color(0xF2F7FB));
        tabel.setSelectionForeground(widget.ThemeConfig.TEXT);
        tabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        tabel.setForeground(widget.ThemeConfig.TEXT);
        tabel.setFillsViewportHeight(true);
        tabel.getTableHeader().setReorderingAllowed(false);
        tabel.getTableHeader().setDefaultRenderer(rendererKepalaTabelKep());
        // Latar JTableHeader-nya sendiri ikut diganti, BUKAN cuma renderernya.
        // Renderer hanya melukis petak per kolom; sisa lebar kepala yg tidak
        // tertutup kolom mana pun dilukis oleh header itu sendiri, dan
        // widget.Table menyetelnya NAVY - jadi kepala terang bisa berakhir
        // dgn sisa strip gelap di kanan. Ketahuannya dari dump CekTabelKep
        // ("kepala latar #17324D") padahal renderer sudah benar; dari render
        // PNG-nya tidak kelihatan krn kedua tabel ini kolomnya memang penuh.
        tabel.getTableHeader().setBackground(widget.ThemeConfig.SURFACE_ALT);
        tabel.getTableHeader().setForeground(widget.ThemeConfig.TEXT_MUTED);
        tabel.getTableHeader().setBorder(javax.swing.BorderFactory.createMatteBorder(
                0, 0, 1, 0, widget.ThemeConfig.BORDER_SOFT));
        tabel.getTableHeader().setPreferredSize(new java.awt.Dimension(
                tabel.getTableHeader().getPreferredSize().width, 30));
    }

    /**
     * Tabel obat/infus: gaya bersama di atas + lebar kolom Bagian F butir 1 +
     * renderer kolom hapus.
     */
    private void gayaTabelObatKep() {
        gayaTabelKep(tbObatInfus, 30);
        tbObatInfus.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        javax.swing.table.DefaultTableCellRenderer selBiasa = rendererSelKepTabel();
        for (int i = 0; i < LEBAR_KOLOM_OBAT_KEP.length; i++) {
            TableColumn kol = tbObatInfus.getColumnModel().getColumn(i);
            if (LEBAR_KOLOM_OBAT_KEP[i] < 0) {
                // Kolom melar: min saja yg dipatok, supaya AUTO_RESIZE tidak
                // pernah memerasnya jadi 0 di kartu 330px.
                kol.setPreferredWidth(200);
                kol.setMinWidth(160);
                kol.setCellRenderer(selBiasa);
                continue;
            }
            kol.setPreferredWidth(LEBAR_KOLOM_OBAT_KEP[i]);
            kol.setMinWidth(LEBAR_KOLOM_OBAT_KEP[i]);
            kol.setMaxWidth(LEBAR_KOLOM_OBAT_KEP[i]);
            kol.setCellRenderer(i == LEBAR_KOLOM_OBAT_KEP.length - 1
                    ? rendererHapusObatKep()
                    : selBiasa);
        }
    }

    /**
     * Sel data biasa: cuma padding 8px kiri/kanan, TANPA menyentuh warna.
     *
     * Justru "tanpa warna" itu isinya: renderer yg tidak mengoper-warna akan
     * memakai selectionBackground/selectionForeground milik TABEL-nya, jadi
     * satu baris terpilih pasti sewarna di semua kolomnya.
     *
     * Ini yg menggantikan fungsi.WarnaTable di tbMasalahKeperawatan. WarnaTable
     * memaksa TABLE_SELECTION_BG (navy 0x3A5168) + teks putih pada sel yg
     * dirender-nya, sementara kolom centang "P" bertipe Boolean.class dipegang
     * renderer bawaan JTable yg menuruti tabelnya (0xF2F7FB). Kalau gaya baru
     * dipasang sambil WarnaTable dibiarkan, hasilnya SATU BARIS TERPILIH DUA
     * WARNA: kotak centang pucat menempel di blok navy. Kelasnya sendiri tidak
     * disentuh - dipakai ratusan tabel lain di aplikasi ini.
     *
     * Zebra pink (255,244,244) milik WarnaTable ikut hilang, dan itu memang
     * diinginkan: warnanya hangat di tengah palet hijau-teal, dan tabel ini
     * cuma punya dua kolom tampak (centang lalu teks yg langsung menempel di
     * sebelahnya) - mata tidak perlu menyeberangi baris, jadi zebra tak
     * memberi apa pun selain satu warna asing lagi.
     */
    private javax.swing.table.DefaultTableCellRenderer rendererSelKepTabel() {
        javax.swing.table.DefaultTableCellRenderer sel = new javax.swing.table.DefaultTableCellRenderer();
        sel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
        return sel;
    }

    /**
     * Kepala tabel: latar SURFACE_ALT, teks 10,5px bold UPPERCASE TEXT_MUTED,
     * garis bawah 1px BORDER_SOFT (Bagian F butir 2). Dipakai tbObatInfus dan
     * tbMasalahKeperawatan lewat gayaTabelKep().
     *
     * setOpaque(true) eksplisit: DefaultTableCellRenderer yg dipakai sbg
     * renderer KEPALA tidak selalu opaque, dan kalau tidak, latar SURFACE_ALT
     * tidak pernah terlukis - yg tampil justru latar bawaan JTableHeader.
     *
     * Kontras TEXT_MUTED di atas SURFACE_ALT ~3,9:1, di bawah 4,5:1 yg diminta
     * WCAG utk teks kecil. Sengaja mengikuti bunyi prompt; angkanya otomatis
     * naik kalau keputusan TEXT_MUTED -> #657387 di PROGRESS.md diambil.
     *
     * Padding 8px MENGERUT jadi 2px pada kolom sempit. Bukan kosmetik:
     * kolom centang "P" di tbMasalahKeperawatan lebarnya dipatok 20px, jadi
     * padding 8+8 hanya menyisakan 4px dan JLabel menggantinya dgn "..." -
     * kepala kolomnya berubah jadi elipsis, kelihatan di render hal-10.
     * Diukur dari lebar kolom yg SEDANG dipakai supaya kolom sempit apa pun
     * (mis. kolom "x" 34px di tabel obat) ikut terlindungi tanpa daftar
     * pengecualian.
     */
    private javax.swing.table.TableCellRenderer rendererKepalaTabelKep() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable tabel,
                    Object nilai, boolean terpilih, boolean fokus, int baris, int kolom) {
                super.getTableCellRendererComponent(tabel, nilai, false, false, baris, kolom);
                setOpaque(true);
                setBackground(widget.ThemeConfig.SURFACE_ALT);
                setForeground(widget.ThemeConfig.TEXT_MUTED);
                setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11).deriveFont(10.5f));
                setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                int lebarKolom = kolom < tabel.getColumnModel().getColumnCount()
                        ? tabel.getColumnModel().getColumn(kolom).getWidth()
                        : 0;
                int isi = lebarKolom < 30 ? 2 : 8;
                setBorder(new javax.swing.border.CompoundBorder(
                        javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, widget.ThemeConfig.BORDER_SOFT),
                        javax.swing.BorderFactory.createEmptyBorder(0, isi, 0, isi)));
                setText(nilai == null ? "" : nilai.toString().toUpperCase());
                return this;
            }
        };
    }

    /**
     * Kolom terakhir: tanda hapus, SELALU digambar - isi selnya diabaikan.
     *
     * Itu yg membuat kolom ke-7 aman thd penyimpanan: tampilObatInfus() dan
     * simpanObat() tetap addRow 6 nilai dan DefaultTableModel menambal sel
     * ke-7 dgn null, sedangkan renderer ini tidak pernah membaca `nilai`.
     */
    private int barisHoverHapusObat = -1;

    /**
     * Renderer tombol hapus obat/infus: tombol merah modern dengan simbol ✕ putih tebal.
     */
    private static final class TombolHapusTabelRenderer extends javax.swing.JPanel 
            implements javax.swing.table.TableCellRenderer {
        private boolean hover = false;

        TombolHapusTabelRenderer() {
            super(new java.awt.GridBagLayout());
            setOpaque(true);
            setToolTipText("Hapus baris obat ini");
        }

        void setHover(boolean h) {
            this.hover = h;
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable tabel,
                Object nilai, boolean terpilih, boolean fokus, int baris, int kolom) {
            if (terpilih) {
                setBackground(tabel.getSelectionBackground());
            } else {
                setBackground(baris % 2 == 1 ? widget.ThemeConfig.SURFACE_ALT : java.awt.Color.WHITE);
            }
            return this;
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            try {
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int bw = 28;
                int bh = 22;
                int bx = (w - bw) / 2;
                int by = (h - bh) / 2;

                // Latar tombol merah: hover = merah gelap (#DC2626), normal = merah tegas (#EF4444)
                java.awt.Color bgTombol = hover 
                        ? new java.awt.Color(0xDC, 0x26, 0x26) 
                        : new java.awt.Color(0xEF, 0x44, 0x44);
                g2.setColor(bgTombol);
                g2.fillRoundRect(bx, by, bw, bh, 6, 6);

                // Garis pinggir tombol merah tegas
                g2.setColor(new java.awt.Color(0xB9, 0x1C, 0x1C));
                g2.drawRoundRect(bx, by, bw, bh, 6, 6);

                // Ikon/huruf ✕ putih tebal dan jelas
                g2.setColor(java.awt.Color.WHITE);
                java.awt.Font f = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13);
                g2.setFont(f);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String txt = tandaHapusKep(f);
                int tx = bx + (bw - fm.stringWidth(txt)) / 2;
                int ty = by + (bh - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(txt, tx, ty);
            } finally {
                g2.dispose();
            }
        }
    }

    private final TombolHapusTabelRenderer rendererHapusObat = new TombolHapusTabelRenderer();

    /**
     * Kolom terakhir: tanda hapus berupa tombol merah yang jelas dan dapat diklik.
     */
    private javax.swing.table.TableCellRenderer rendererHapusObatKep() {
        return new javax.swing.table.TableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable tabel,
                    Object nilai, boolean terpilih, boolean fokus, int baris, int kolom) {
                rendererHapusObat.setHover(baris == barisHoverHapusObat);
                return rendererHapusObat.getTableCellRendererComponent(tabel, nilai, terpilih, fokus, baris, kolom);
            }
        };
    }

    /**
     * Tanda hapus kolom terakhir. "✕" (U+2715) dipilih krn bentuknya bukan
     * huruf x, tapi tidak semua font punya glif itu - dan font yg tidak
     * punya melukis KOTAK KOSONG, bukan error. Jadi diperiksa dulu, lalu
     * jatuh ke "×" (U+00D7, ada di semua font Latin) kalau perlu.
     */
    private static String tandaHapusKep(java.awt.Font f) {
        return f.canDisplay('✕') ? "✕" : "×";
    }

    /**
     * [Bagian F butir 4] Klik di kolom terakhir = hapus baris itu, dgn
     * konfirmasi lebih dulu (pola "Yakin mau dihapus obat ...?" yg dipakai
     * form obat lain di aplikasi ini).
     */
    private void pasangKlikHapusObatKep() {
        tbObatInfus.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (barisHoverHapusObat != -1) {
                    barisHoverHapusObat = -1;
                    tbObatInfus.repaint();
                }
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int kolom = tbObatInfus.columnAtPoint(evt.getPoint());
                int baris = tbObatInfus.rowAtPoint(evt.getPoint());
                if (baris < 0 || kolom != tbObatInfus.getColumnCount() - 1) {
                    return;
                }
                tbObatInfus.setRowSelectionInterval(baris, baris);
                Object nama = tabModeObatInfus.getValueAt(baris, 1);
                Object pukul = tabModeObatInfus.getValueAt(baris, 0);
                if (JOptionPane.showConfirmDialog(rootPane,
                        "Yakin mau dihapus obat " + (nama == null ? "" : nama)
                                + " pukul " + (pukul == null ? "" : pukul) + "...?",
                        "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    hapusObat();
                }
            }
        });
        // Kursor tangan dan hover highlight di tombol hapus kolom terakhir
        tbObatInfus.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                int baris = tbObatInfus.rowAtPoint(evt.getPoint());
                int kolom = tbObatInfus.columnAtPoint(evt.getPoint());
                boolean diHapus = (kolom == tbObatInfus.getColumnCount() - 1) && (baris >= 0);
                int barisLama = barisHoverHapusObat;
                barisHoverHapusObat = diHapus ? baris : -1;
                if (barisLama != barisHoverHapusObat) {
                    tbObatInfus.repaint();
                }
                tbObatInfus.setCursor(java.awt.Cursor.getPredefinedCursor(diHapus
                        ? java.awt.Cursor.HAND_CURSOR
                        : java.awt.Cursor.DEFAULT_CURSOR));
            }
        });
    }

    /**
     * Tombol PRIMER: latar ACCENT, teks putih tebal. Dipakai "+ Tambah"
     * (Bagian F butir 3); resepnya dipisah jadi helper supaya BtnSimpan di
     * Bagian G memakai yg SAMA, bukan salinan tiga baris ini.
     *
     * Knob latar widget.Button adalah setGlassColor(), BUKAN setBackground():
     * widget.Button extends usu.widget.ButtonGlass yg melukis latarnya sendiri
     * dari glassColor. setBackground() tidak menghasilkan apa pun DAN tidak
     * memberi error - bentuk jebakan yg sama dgn PanelBiasa (latar diminta,
     * getBackground() melaporkannya, tapi bukan itu yg terlukis).
     */
    private static void jadikanTombolPrimerKep(widget.Button b) {
        b.setGlassColor(widget.ThemeConfig.ACCENT);
        b.setForeground(java.awt.Color.WHITE);
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
    }

    /**
     * Warna tombol primer saat NONAKTIF. ButtonGlass melukis glassColor tanpa
     * peduli isEnabled(), jadi tombol mati harus DIWARNAI mati juga - kalau
     * tidak, "+ Tambah" tetap ACCENT terang dan terlihat bisa diklik padahal
     * setEnabled(false).
     */
    private static void warnaiTombolPrimerKep(widget.Button b, boolean hidup) {
        b.setGlassColor(hidup ? widget.ThemeConfig.ACCENT : new java.awt.Color(0xD8DBD6));
        b.setForeground(hidup ? java.awt.Color.WHITE : widget.ThemeConfig.TEXT_FAINT);
    }

    /**
     * [Lanjutan RM 03-6] Muat ulang daftar Pemberian Obat/Infus utk
     * no_rawat yang sedang dibuka - dipanggil dari isRawat()/getData().
     */
    private void tampilObatInfus(String noRawat) {
        Valid.tabelKosong(tabModeObatInfus);
        try {
            PreparedStatement psObat = koneksi.prepareStatement(
                    "select penilaian_awal_keperawatan_igd_obat.*, " +
                            "p1.nama as nama_diperiksa, p2.nama as nama_diberikan " +
                            "from penilaian_awal_keperawatan_igd_obat " +
                            "left join petugas p1 on p1.nip=penilaian_awal_keperawatan_igd_obat.nip_diperiksa " +
                            "left join petugas p2 on p2.nip=penilaian_awal_keperawatan_igd_obat.nip_diberikan " +
                            "where penilaian_awal_keperawatan_igd_obat.no_rawat=? order by tanggal,pukul");
            try {
                psObat.setString(1, noRawat);
                ResultSet rsObat = psObat.executeQuery();
                try {
                    while (rsObat.next()) {
                        tabModeObatInfus.addRow(new Object[] {
                                rsObat.getString("pukul"), rsObat.getString("nama_obat"), rsObat.getString("dosis"),
                                rsObat.getString("rute"),
                                rsObat.getString("nama_diperiksa"), rsObat.getString("nama_diberikan")
                        });
                    }
                } finally {
                    if (rsObat != null) {
                        rsObat.close();
                    }
                }
            } finally {
                if (psObat != null) {
                    psObat.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Tampil Obat Infus : " + e);
        }
    }

    private void emptTeksObatInfus() {
        DTPObatPukul.setJam(new Date());
        TObatNama.setText("");
        TObatDosis.setText("");
        TObatRute.setText("");
        KdObatDiperiksa.setText("");
        NmObatDiperiksa.setText("");
        KdObatDiberikan.setText("");
        NmObatDiberikan.setText("");
    }

    private void simpanObat() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Silahkan pilih pasien (No.Rawat) terlebih dahulu..!!");
            return;
        }
        if (TObatNama.getText().trim().equals("")) {
            Valid.textKosong(TObatNama, "Nama Obat/Infus");
            return;
        }
        try {
            PreparedStatement psSimpanObat = koneksi.prepareStatement(
                    "insert into penilaian_awal_keperawatan_igd_obat (no_rawat,tanggal,pukul,nama_obat,dosis,rute,nip_diperiksa,nip_diberikan) values (?,?,?,?,?,?,?,?)");
            try {
                psSimpanObat.setString(1, TNoRw.getText());
                psSimpanObat.setString(2, Valid.SetTgl(TglAsuhan.getSelectedItem() + ""));
                psSimpanObat.setString(3, DTPObatPukul.getTeksJam());
                psSimpanObat.setString(4, TObatNama.getText());
                psSimpanObat.setString(5, TObatDosis.getText());
                psSimpanObat.setString(6, TObatRute.getText());
                psSimpanObat.setString(7, KdObatDiperiksa.getText());
                psSimpanObat.setString(8, KdObatDiberikan.getText());
                psSimpanObat.executeUpdate();
                tabModeObatInfus.addRow(new Object[] {
                        DTPObatPukul.getTeksJam(), TObatNama.getText(), TObatDosis.getText(), TObatRute.getText(),
                        NmObatDiperiksa.getText(), NmObatDiberikan.getText()
                });
                emptTeksObatInfus();
            } finally {
                if (psSimpanObat != null) {
                    psSimpanObat.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Simpan Obat Infus : " + e);
            JOptionPane.showMessageDialog(null,
                    "Maaf, gagal menyimpan. Kemungkinan Pukul+Nama Obat yang sama sudah dimasukkan sebelumnya...!");
        }
    }

    private void hapusObat() {
        if (tbObatInfus.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, "Silahkan pilih baris yang mau dihapus terlebih dahulu..!!");
            return;
        }
        try {
            PreparedStatement psHapusObat = koneksi.prepareStatement(
                    "delete from penilaian_awal_keperawatan_igd_obat where no_rawat=? and tanggal=? and pukul=? and nama_obat=?");
            try {
                psHapusObat.setString(1, TNoRw.getText());
                psHapusObat.setString(2, Valid.SetTgl(TglAsuhan.getSelectedItem() + ""));
                psHapusObat.setString(3, tbObatInfus.getValueAt(tbObatInfus.getSelectedRow(), 0).toString());
                psHapusObat.setString(4, tbObatInfus.getValueAt(tbObatInfus.getSelectedRow(), 1).toString());
                psHapusObat.executeUpdate();
                tabModeObatInfus.removeRow(tbObatInfus.getSelectedRow());
            } finally {
                if (psHapusObat != null) {
                    psHapusObat.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Hapus Obat Infus : " + e);
            JOptionPane.showMessageDialog(null, "Maaf, gagal menghapus..!!");
        }
    }

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_TNoRwKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            isRawat();
        } else {
            Valid.pindah(evt, TCari, BtnPetugas);
        }
    }// GEN-LAST:event_TNoRwKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnSimpanActionPerformed
        if (TNoRM.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "Nama Pasien");
        } else if (KeluhanUtama.getText().trim().equals("")) {
            Valid.textKosong(KeluhanUtama, "Riwayat Penyakit Sekarang");
        } else if (NmPetugas.getText().trim().equals("")) {
            Valid.textKosong(BtnPetugas, "Petugas");
        } else {
            if (akses.getkode().equals("Admin Utama")) {
                simpan();
            } else {
                if (TanggalRegistrasi.getText().equals("")) {
                    TanggalRegistrasi.setText(Sequel.cariIsi(
                            "select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",
                            TNoRw.getText()));
                }
                if (Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(), getTanggalDanJamAsuhan()) == true) {
                    simpan();
                }
            }
        }

    }// GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnSimpanKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        } else {
            Valid.pindah(evt, Rencana, BtnBatal);
        }
    }// GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
    }// GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnBatalKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            emptTeks();
        } else {
            Valid.pindah(evt, BtnSimpan, BtnHapus);
        }
    }// GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnHapusActionPerformed
        if (tbObat.getSelectedRow() > -1) {
            if (akses.getkode().equals("Admin Utama")) {
                hapus();
            } else {
                if (KdPetugas.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString())) {
                    if (Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString(),
                            Sequel.ambiltanggalsekarang()) == true) {
                        hapus();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Hanya bisa dihapus oleh petugas yang bersangkutan..!!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
        }

    }// GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnHapusKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnHapusActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
    }// GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnEditActionPerformed
        if (TNoRM.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "Nama Pasien");
        } else if (KeluhanUtama.getText().trim().equals("")) {
            Valid.textKosong(KeluhanUtama, "Riwayat Penyakit Sekarang");
        } else if (NmPetugas.getText().trim().equals("")) {
            Valid.textKosong(BtnPetugas, "Petugas");
        } else {
            if (tbObat.getSelectedRow() > -1) {
                if (akses.getkode().equals("Admin Utama")) {
                    ganti();
                } else {
                    if (KdPetugas.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString())) {
                        if (Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString(),
                                Sequel.ambiltanggalsekarang()) == true) {
                            if (TanggalRegistrasi.getText().equals("")) {
                                TanggalRegistrasi.setText(Sequel.cariIsi(
                                        "select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",
                                        TNoRw.getText()));
                            }
                            if (Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),
                                    getTanggalDanJamAsuhan()) == true) {
                                ganti();
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Hanya bisa diganti oleh petugas yang bersangkutan..!!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
            }
        }
    }// GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnEditKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnEditActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
    }// GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }// GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnKeluarActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnEdit, TCari);
        }
    }// GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        } else if (tabMode.getRowCount() != 0) {
            try {
                if (TCari.getText().equals("")) {
                    ps = koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,pasien.agama,bahasa_pasien.nama_bahasa,cacat_fisik.nama_cacat,penilaian_awal_keperawatan_igd.tanggal,penilaian_awal_keperawatan_igd.informasi,"
                                    +
                                    "penilaian_awal_keperawatan_igd.keluhan_utama,penilaian_awal_keperawatan_igd.rpd,penilaian_awal_keperawatan_igd.rpo,penilaian_awal_keperawatan_igd.status_kehamilan,penilaian_awal_keperawatan_igd.gravida,penilaian_awal_keperawatan_igd.para,"
                                    +
                                    "penilaian_awal_keperawatan_igd.abortus,penilaian_awal_keperawatan_igd.hpht,penilaian_awal_keperawatan_igd.tekanan,penilaian_awal_keperawatan_igd.pupil,penilaian_awal_keperawatan_igd.neurosensorik,penilaian_awal_keperawatan_igd.integumen,penilaian_awal_keperawatan_igd.turgor,"
                                    +
                                    "penilaian_awal_keperawatan_igd.edema,penilaian_awal_keperawatan_igd.mukosa,penilaian_awal_keperawatan_igd.perdarahan,penilaian_awal_keperawatan_igd.jumlah_perdarahan,penilaian_awal_keperawatan_igd.warna_perdarahan,penilaian_awal_keperawatan_igd.intoksikasi,"
                                    +
                                    "penilaian_awal_keperawatan_igd.bab,penilaian_awal_keperawatan_igd.xbab,penilaian_awal_keperawatan_igd.kbab,penilaian_awal_keperawatan_igd.wbab,penilaian_awal_keperawatan_igd.bak,penilaian_awal_keperawatan_igd.xbak,penilaian_awal_keperawatan_igd.wbak,"
                                    +
                                    "penilaian_awal_keperawatan_igd.lbak,penilaian_awal_keperawatan_igd.psikologis,penilaian_awal_keperawatan_igd.jiwa,penilaian_awal_keperawatan_igd.perilaku,penilaian_awal_keperawatan_igd.dilaporkan,penilaian_awal_keperawatan_igd.sebutkan,penilaian_awal_keperawatan_igd.hubungan,pasien.stts_nikah,"
                                    +
                                    "penilaian_awal_keperawatan_igd.tinggal_dengan,penilaian_awal_keperawatan_igd.ket_tinggal,pasien.pekerjaan,penjab.png_jawab,penilaian_awal_keperawatan_igd.budaya,penilaian_awal_keperawatan_igd.ket_budaya,pasien.pnd,penilaian_awal_keperawatan_igd.pendidikan_pj,penilaian_awal_keperawatan_igd.ket_pendidikan_pj,"
                                    +
                                    "penilaian_awal_keperawatan_igd.edukasi,penilaian_awal_keperawatan_igd.ket_edukasi,penilaian_awal_keperawatan_igd.kemampuan,penilaian_awal_keperawatan_igd.aktifitas,penilaian_awal_keperawatan_igd.alat_bantu,penilaian_awal_keperawatan_igd.ket_bantu,"
                                    +
                                    "penilaian_awal_keperawatan_igd.nyeri,penilaian_awal_keperawatan_igd.provokes,penilaian_awal_keperawatan_igd.ket_provokes,penilaian_awal_keperawatan_igd.quality,penilaian_awal_keperawatan_igd.ket_quality,penilaian_awal_keperawatan_igd.lokasi,penilaian_awal_keperawatan_igd.menyebar,"
                                    +
                                    "penilaian_awal_keperawatan_igd.skala_nyeri,penilaian_awal_keperawatan_igd.durasi,penilaian_awal_keperawatan_igd.nyeri_hilang,penilaian_awal_keperawatan_igd.ket_nyeri,penilaian_awal_keperawatan_igd.pada_dokter,penilaian_awal_keperawatan_igd.ket_dokter,"
                                    +
                                    "penilaian_awal_keperawatan_igd.berjalan_a,penilaian_awal_keperawatan_igd.berjalan_b,penilaian_awal_keperawatan_igd.berjalan_c,penilaian_awal_keperawatan_igd.hasil,penilaian_awal_keperawatan_igd.lapor,penilaian_awal_keperawatan_igd.ket_lapor,"
                                    +
                                    "penilaian_awal_keperawatan_igd.rencana,penilaian_awal_keperawatan_igd.nip,petugas.nama "
                                    +
                                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                    +
                                    "inner join penilaian_awal_keperawatan_igd on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat "
                                    +
                                    "inner join petugas on penilaian_awal_keperawatan_igd.nip=petugas.nip " +
                                    "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien " +
                                    "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj " +
                                    "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik where " +
                                    "penilaian_awal_keperawatan_igd.tanggal between ? and ? order by penilaian_awal_keperawatan_igd.tanggal");
                } else {
                    ps = koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,pasien.agama,bahasa_pasien.nama_bahasa,cacat_fisik.nama_cacat,penilaian_awal_keperawatan_igd.tanggal,penilaian_awal_keperawatan_igd.informasi,"
                                    +
                                    "penilaian_awal_keperawatan_igd.keluhan_utama,penilaian_awal_keperawatan_igd.rpd,penilaian_awal_keperawatan_igd.rpo,penilaian_awal_keperawatan_igd.status_kehamilan,penilaian_awal_keperawatan_igd.gravida,penilaian_awal_keperawatan_igd.para,"
                                    +
                                    "penilaian_awal_keperawatan_igd.abortus,penilaian_awal_keperawatan_igd.hpht,penilaian_awal_keperawatan_igd.tekanan,penilaian_awal_keperawatan_igd.pupil,penilaian_awal_keperawatan_igd.neurosensorik,penilaian_awal_keperawatan_igd.integumen,penilaian_awal_keperawatan_igd.turgor,"
                                    +
                                    "penilaian_awal_keperawatan_igd.edema,penilaian_awal_keperawatan_igd.mukosa,penilaian_awal_keperawatan_igd.perdarahan,penilaian_awal_keperawatan_igd.jumlah_perdarahan,penilaian_awal_keperawatan_igd.warna_perdarahan,penilaian_awal_keperawatan_igd.intoksikasi,"
                                    +
                                    "penilaian_awal_keperawatan_igd.bab,penilaian_awal_keperawatan_igd.xbab,penilaian_awal_keperawatan_igd.kbab,penilaian_awal_keperawatan_igd.wbab,penilaian_awal_keperawatan_igd.bak,penilaian_awal_keperawatan_igd.xbak,penilaian_awal_keperawatan_igd.wbak,"
                                    +
                                    "penilaian_awal_keperawatan_igd.lbak,penilaian_awal_keperawatan_igd.psikologis,penilaian_awal_keperawatan_igd.jiwa,penilaian_awal_keperawatan_igd.perilaku,penilaian_awal_keperawatan_igd.dilaporkan,penilaian_awal_keperawatan_igd.sebutkan,penilaian_awal_keperawatan_igd.hubungan,pasien.stts_nikah,"
                                    +
                                    "penilaian_awal_keperawatan_igd.tinggal_dengan,penilaian_awal_keperawatan_igd.ket_tinggal,pasien.pekerjaan,penjab.png_jawab,penilaian_awal_keperawatan_igd.budaya,penilaian_awal_keperawatan_igd.ket_budaya,pasien.pnd,penilaian_awal_keperawatan_igd.pendidikan_pj,penilaian_awal_keperawatan_igd.ket_pendidikan_pj,"
                                    +
                                    "penilaian_awal_keperawatan_igd.edukasi,penilaian_awal_keperawatan_igd.ket_edukasi,penilaian_awal_keperawatan_igd.kemampuan,penilaian_awal_keperawatan_igd.aktifitas,penilaian_awal_keperawatan_igd.alat_bantu,penilaian_awal_keperawatan_igd.ket_bantu,"
                                    +
                                    "penilaian_awal_keperawatan_igd.nyeri,penilaian_awal_keperawatan_igd.provokes,penilaian_awal_keperawatan_igd.ket_provokes,penilaian_awal_keperawatan_igd.quality,penilaian_awal_keperawatan_igd.ket_quality,penilaian_awal_keperawatan_igd.lokasi,penilaian_awal_keperawatan_igd.menyebar,"
                                    +
                                    "penilaian_awal_keperawatan_igd.skala_nyeri,penilaian_awal_keperawatan_igd.durasi,penilaian_awal_keperawatan_igd.nyeri_hilang,penilaian_awal_keperawatan_igd.ket_nyeri,penilaian_awal_keperawatan_igd.pada_dokter,penilaian_awal_keperawatan_igd.ket_dokter,"
                                    +
                                    "penilaian_awal_keperawatan_igd.berjalan_a,penilaian_awal_keperawatan_igd.berjalan_b,penilaian_awal_keperawatan_igd.berjalan_c,penilaian_awal_keperawatan_igd.hasil,penilaian_awal_keperawatan_igd.lapor,penilaian_awal_keperawatan_igd.ket_lapor,"
                                    +
                                    "penilaian_awal_keperawatan_igd.rencana,penilaian_awal_keperawatan_igd.nip,petugas.nama "
                                    +
                                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                    +
                                    "inner join penilaian_awal_keperawatan_igd on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat "
                                    +
                                    "inner join petugas on penilaian_awal_keperawatan_igd.nip=petugas.nip " +
                                    "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien " +
                                    "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj " +
                                    "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik where " +
                                    "penilaian_awal_keperawatan_igd.tanggal between ? and ? and " +
                                    "(reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "
                                    +
                                    "penilaian_awal_keperawatan_igd.nip like ? or petugas.nama like ?) " +
                                    "order by penilaian_awal_keperawatan_igd.tanggal");
                }

                try {
                    if (TCari.getText().equals("")) {
                        ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                        ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                    } else {
                        ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                        ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                        ps.setString(3, "%" + TCari.getText() + "%");
                        ps.setString(4, "%" + TCari.getText() + "%");
                        ps.setString(5, "%" + TCari.getText() + "%");
                        ps.setString(6, "%" + TCari.getText() + "%");
                        ps.setString(7, "%" + TCari.getText() + "%");
                    }
                    rs = ps.executeQuery();
                    StringBuilder htmlContent = new StringBuilder();
                    htmlContent.append(
                            "<tr class='isi'>" +
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='9%'><b>PASIEN & PETUGAS</b></td>"
                                    +
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='10%'><b>I. RIWAYAT KESEHATAN PASIEN</b></td>"
                                    +
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='15%'><b>II. PEMERIKSAAN FISIK</b></td>"
                                    +
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='14%'><b>III. RIWAYAT PSIKOLOGIS - SOSIAL - EKONOMI - BUDAYA - SPIRITUAL</b></td>"
                                    +
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='8%'><b>IV. PENGKAJIAN FUNGSI</b></td>"
                                    +
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='12%'><b>V. SKALA NYERI</b></td>"
                                    +
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='16%'><b>VI. PENGKAJIAN RESIKO JATUH (GET UP AND GO)</b></td>"
                                    +
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='11%'><b>MASALAH & RENCANA KEPERAWATAN</b></td>"
                                    +
                                    "</tr>");
                    while (rs.next()) {
                        masalahkeperawatanigd = "";
                        ps2 = koneksi.prepareStatement(
                                "select master_masalah_keperawatan_igd.kode_masalah,master_masalah_keperawatan_igd.nama_masalah from master_masalah_keperawatan_igd "
                                        +
                                        "inner join penilaian_awal_keperawatan_igd_masalah on penilaian_awal_keperawatan_igd_masalah.kode_masalah=master_masalah_keperawatan_igd.kode_masalah "
                                        +
                                        "where penilaian_awal_keperawatan_igd_masalah.no_rawat=? order by penilaian_awal_keperawatan_igd_masalah.kode_masalah");
                        try {
                            ps2.setString(1, rs.getString("no_rawat"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                masalahkeperawatanigd = rs2.getString("nama_masalah") + ", " + masalahkeperawatanigd;
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        htmlContent.append(
                                "<tr class='isi'>" +
                                        "<td valign='top' cellpadding='0' cellspacing='0'>" +
                                        "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"
                                        +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>No.Rawat</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("no_rawat") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>No.R.M.</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("no_rkm_medis") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Nama Pasien</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("nm_pasien") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>J.K.</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("jk") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Tgl.Lahir</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("tgl_lahir") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Agama</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("agama") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Bahasa</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("nama_bahasa") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Pekerjaan</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("pekerjaan") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Pembayaran</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("png_jawab") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Pendidikan</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("pnd") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Stts.Nikah</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("stts_nikah") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Cacat Fisik</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("nama_cacat") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Petugas</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("nip") + " " + rs.getString("nama") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Tgl.Asuhan</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("tanggal") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Informasi</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("informasi") + "</td>" +
                                        "</tr>" +
                                        "</table>" +
                                        "</td>" +
                                        "<td valign='top' cellpadding='0' cellspacing='0'>" +
                                        "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"
                                        +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>RPS</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("keluhan_utama") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>RPD</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("rpd") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>RPO</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("rpo") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Stts.Hami</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("status_kehamilan") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>HPHT</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("hpht") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Para</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("para") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Abortus</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("abortus") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='32%' valign='top'>Gravida</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"
                                        + rs.getString("gravida") + "</td>" +
                                        "</tr>" +
                                        "</table>" +
                                        "</td>" +
                                        "<td valign='top' cellpadding='0' cellspacing='0'>" +
                                        "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"
                                        +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Tekanan Intrakranial</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("tekanan") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Pupil</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("pupil") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Neurosensorik / Muskuloskeletal</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("neurosensorik") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Integumen</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("integumen") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Turgor kulit</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("turgor") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Edema</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("edema") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Mukosa Mulut</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("mukosa") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Perdarahan</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("perdarahan") + ", Jumlah : " + rs.getString("jumlah_perdarahan")
                                        + ", Warna : " + rs.getString("warna_perdarahan") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Intoksikasi</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("intoksikasi") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Eliminasi</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        +
                                        "BAB : -Frekuensi : " + rs.getString("bab") + " x / " + rs.getString("xbab")
                                        + " -Konsistensi : " + rs.getString("kbab") + " -Warna : "
                                        + rs.getString("wbab") + "<br>" +
                                        "BAK : -Frekuensi : " + rs.getString("bak") + " x / " + rs.getString("xbak")
                                        + " -Warna : " + rs.getString("wbak") + " -Lain-lain : " + rs.getString("lbak")
                                        + "<br>" +
                                        "</td>" +
                                        "</tr>" +
                                        "</table>" +
                                        "</td>" +
                                        "<td valign='top' cellpadding='0' cellspacing='0'>" +
                                        "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"
                                        +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Kondisi Psikologis</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("psikologis") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Gangguan Jiwa Di Masa Lalu</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("jiwa") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Adakah perilaku</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("perilaku") + ", Dilaporkan Ke : " + rs.getString("dilaporkan")
                                        + ", Sebutkan : " + rs.getString("sebutkan") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Hubungan Pasien Dengan Anggota Keluarga</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("hubungan") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Tinggal Dengan</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("tinggal_dengan")
                                        + (rs.getString("ket_tinggal").equals("") ? ""
                                                : ", " + rs.getString("ket_tinggal"))
                                        + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Kepercayaan / Budaya / Nilai-nilai Khusus</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("budaya")
                                        + (rs.getString("ket_budaya").equals("") ? ""
                                                : ", " + rs.getString("ket_budaya"))
                                        + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Pendidikan P.J.</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("pendidikan_pj")
                                        + (rs.getString("ket_pendidikan_pj").equals("") ? ""
                                                : ", " + rs.getString("ket_pendidikan_pj"))
                                        + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Edukasi Diberikan Kepada</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("edukasi")
                                        + (rs.getString("ket_edukasi").equals("") ? ""
                                                : ", " + rs.getString("ket_edukasi"))
                                        + "</td>" +
                                        "</tr>" +
                                        "</table>" +
                                        "</td>" +
                                        "<td valign='top' cellpadding='0' cellspacing='0'>" +
                                        "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"
                                        +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Kemampuan Aktifitas Sehari-hari</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("kemampuan") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Aktifitas</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("aktifitas") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='34%' valign='top'>Alat Bantu</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"
                                        + rs.getString("alat_bantu")
                                        + (rs.getString("ket_bantu").equals("") ? "" : ", " + rs.getString("ket_bantu"))
                                        + "</td>" +
                                        "</tr>" +
                                        "</table>" +
                                        "</td>" +
                                        "<td valign='top' cellpadding='0' cellspacing='0'>" +
                                        "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"
                                        +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Tingkat Nyeri</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("nyeri") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Provokes</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("provokes") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Ket. Provokes</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("ket_provokes") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Kualitas</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("quality") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Ket. Kualitas</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("ket_quality") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Lokas</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("lokasi") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Menyebar</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("menyebar") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Skala Nyeri</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("skala_nyeri") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Durasi</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("durasi") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Nyeri Hilang</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("nyeri_hilang") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Ket. Hilang Nyeri</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("ket_nyeri") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Lapor Ke Dokter</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("pada_dokter") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='44%' valign='top'>Jam Lapor</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"
                                        + rs.getString("ket_dokter") + "</td>" +
                                        "</tr>" +
                                        "</table>" +
                                        "</td>" +
                                        "<td valign='top' cellpadding='0' cellspacing='0'>" +
                                        "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"
                                        +
                                        "<tr class='isi2'>" +
                                        "<td width='64%' valign='top'>Tidak seimbang/sempoyongan/limbung</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"
                                        + rs.getString("berjalan_a") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='64%' valign='top'>Jalan dengan menggunakan alat bantu (kruk, tripot, kursi roda, orang lain)</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"
                                        + rs.getString("berjalan_b") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='64%' valign='top'>Menopang saat akan duduk, tampak memegang pinggiran kursi atau meja/benda lain sebagai penopang</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"
                                        + rs.getString("berjalan_c") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='64%' valign='top'>Hasil</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"
                                        + rs.getString("hasil") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='64%' valign='top'>Dilaporan ke dokter?</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"
                                        + rs.getString("lapor") + "</td>" +
                                        "</tr>" +
                                        "<tr class='isi2'>" +
                                        "<td width='64%' valign='top'>Jam Lapor</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"
                                        + rs.getString("ket_lapor") + "</td>" +
                                        "</tr>" +
                                        "</table>" +
                                        "</td>" +
                                        "<td valign='top' cellpadding='0' cellspacing='0'>" +
                                        "Masalah Keperawatan : " + masalahkeperawatanigd + "<br><br>" +
                                        "Rencana Keperawatan : " + rs.getString("rencana") +
                                        "</td>" +
                                        "</tr>");
                    }
                    LoadHTML.setText(
                            "<html>" +
                                    "<table width='100%' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"
                                    +
                                    htmlContent.toString() +
                                    "</table>" +
                                    "</html>");
                    htmlContent = null;

                    File g = new File("file2.css");
                    BufferedWriter bg = new BufferedWriter(new FileWriter(g));
                    bg.write(
                            ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                                    +
                                    ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"
                                    +
                                    ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                                    +
                                    ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                                    +
                                    ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"
                                    +
                                    ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"
                                    +
                                    ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"
                                    +
                                    ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"
                                    +
                                    ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}");
                    bg.close();

                    File f = new File("DataPenilaianAwalKeperawatanIGD.html");
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                    bw.write(LoadHTML.getText().replaceAll("<head>", "<head>" +
                            "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />" +
                            "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                            +
                            "<tr class='isi2'>" +
                            "<td valign='top' align='center'>" +
                            "<font size='4' face='Tahoma'>" + akses.getnamars() + "</font><br>" +
                            akses.getalamatrs() + ", " + akses.getkabupatenrs() + ", " + akses.getpropinsirs() + "<br>"
                            +
                            akses.getkontakrs() + ", E-mail : " + akses.getemailrs() + "<br><br>" +
                            "<font size='2' face='Tahoma'>DATA PENGKAJIAN AWAL KEPERAWATAN IGD<br><br></font>" +
                            "</td>" +
                            "</tr>" +
                            "</table>"));
                    bw.close();
                    Desktop.getDesktop().browse(f.toURI());
                    // LoadHTML juga dipakai sebagai panel ringkasan record terpilih.
                    // Setelah menjadi buffer cetak massal, kosongkan dan lipat panel
                    // agar isi laporan massal tidak disangka sebagai detail pasien.
                    TNoRM1.setText("");
                    TPasien1.setText("");
                    LoadHTML.setText("");
                    ChkAccor.setSelected(false);
                    isMenu();
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
        }
        this.setCursor(Cursor.getDefaultCursor());
    }// GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnPrintKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnPrintActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
    }// GEN-LAST:event_BtnPrintKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_TCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            BtnCari.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            BtnKeluar.requestFocus();
        }
    }// GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnCariActionPerformed
        runBackground(() -> tampil());
    }// GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnCariActionPerformed(null);
        } else {
            Valid.pindah(evt, TCari, BtnAll);
        }
    }// GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() -> tampil());
    }// GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            TCari.setText("");
            runBackground(() -> tampil());
        } else {
            Valid.pindah(evt, BtnCari, TPasien);
        }
    }// GEN-LAST:event_BtnAllKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_tbObatMouseClicked
        // [RM 03-4/05/06] Klik baris otomatis mengisi field utk edit (pola
        // sama RM 03-2) - getData() sekarang pola query-ulang-DB, cuma
        // SELECT (aman dipanggil dari klik biasa), user tetap harus tekan
        // Simpan/Edit utk benar-benar menyimpan perubahan.
        if (tabMode.getRowCount() != 0) {
            try {
                ChkAccor.setSelected(true);
                isMenu();
                getData();
            } catch (java.lang.NullPointerException e) {
            }
            if ((evt.getClickCount() == 2) && (tbObat.getSelectedColumn() == 0)) {
                TabRawat.setSelectedIndex(0);
            }
        }
    }// GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_tbObatKeyPressed
        if (tabMode.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP)
                    || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    ChkAccor.setSelected(true);
                    isMenu();
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            } else if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                try {
                    getData();
                    TabRawat.setSelectedIndex(0);
                    // Pindah ke tab Input - bersihkan & lipat panel
                    // ringkasan (sudah tidak relevan), pola sama Triase.
                    TNoRM1.setText("");
                    TPasien1.setText("");
                    LoadHTML.setText("");
                    ChkAccor.setSelected(false);
                    isMenu();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }// GEN-LAST:event_tbObatKeyPressed

    private void KdPetugasKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KdPetugasKeyPressed

    }// GEN-LAST:event_KdPetugasKeyPressed

    private void BtnPetugasActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnPetugasActionPerformed
        if (petugas == null || !petugas.isDisplayable()) {
            petugas = new DlgCariPetugas(null, false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (petugas.getTable().getSelectedRow() != -1) {
                        KdPetugas.setText(
                                petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                        NmPetugas.setText(
                                petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                    }
                    BtnPetugas.requestFocus();
                    petugas = null;
                }
            });

            petugas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            petugas.setLocationRelativeTo(internalFrame1);
        }
        if (petugas == null)
            return;
        if (!petugas.isVisible()) {
            petugas.isCek();
            petugas.emptTeks();
        }

        if (petugas.isVisible()) {
            petugas.toFront();
            return;
        }
        petugas.setVisible(true);
    }// GEN-LAST:event_BtnPetugasActionPerformed

    private void BtnPetugasKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnPetugasKeyPressed
        // Valid.pindah(evt,Monitoring,BtnSimpan);
    }// GEN-LAST:event_BtnPetugasKeyPressed

    private void KeluhanUtamaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KeluhanUtamaKeyPressed
        Valid.pindah2(evt, Informasi, RPD);
    }// GEN-LAST:event_KeluhanUtamaKeyPressed

    private void RPDKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_RPDKeyPressed
        Valid.pindah2(evt, KeluhanUtama, RPO);
    }// GEN-LAST:event_RPDKeyPressed

    private void RPOKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_RPOKeyPressed
        Valid.pindah2(evt, RPD, StatusKehamilan);
    }// GEN-LAST:event_RPOKeyPressed

    private void AktifitasKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_AktifitasKeyPressed
        Valid.pindah(evt, ADL, AlatBantu);
    }// GEN-LAST:event_AktifitasKeyPressed

    private void AlatBantuKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_AlatBantuKeyPressed
        Valid.pindah(evt, Aktifitas, KetAlatBantu);
    }// GEN-LAST:event_AlatBantuKeyPressed

    private void KetAlatBantuKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KetAlatBantuKeyPressed
        Valid.pindah(evt, AlatBantu, Nyeri);
    }// GEN-LAST:event_KetAlatBantuKeyPressed

    private void ADLKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_ADLKeyPressed
        Valid.pindah(evt, KetEdukasi, Aktifitas);
    }// GEN-LAST:event_ADLKeyPressed

    private void TinggalDenganKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_TinggalDenganKeyPressed
        Valid.pindah(evt, Hubungan, KetTinggal);
    }// GEN-LAST:event_TinggalDenganKeyPressed

    private void KetTinggalKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KetTinggalKeyPressed
        Valid.pindah(evt, TinggalDengan, StatusBudaya);
    }// GEN-LAST:event_KetTinggalKeyPressed

    private void EdukasiKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_EdukasiKeyPressed
        Valid.pindah(evt, KetPendidikanPJ, KetEdukasi);
    }// GEN-LAST:event_EdukasiKeyPressed

    private void KetEdukasiKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KetEdukasiKeyPressed
        Valid.pindah(evt, Edukasi, ADL);
    }// GEN-LAST:event_KetEdukasiKeyPressed

    private void LaporKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_LaporKeyPressed
        Valid.pindah(evt, Hasil, KetLapor);
    }// GEN-LAST:event_LaporKeyPressed

    private void ATSKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_ATSKeyPressed
        Valid.pindah(evt, KetDokter, BJM);
    }// GEN-LAST:event_ATSKeyPressed

    private void BJMKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BJMKeyPressed
        Valid.pindah(evt, ATS, MSA);
    }// GEN-LAST:event_BJMKeyPressed

    private void HasilKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_HasilKeyPressed
        Valid.pindah(evt, MSA, Lapor);
    }// GEN-LAST:event_HasilKeyPressed

    private void KetLaporKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KetLaporKeyPressed
        Valid.pindah(evt, Lapor, Rencana);
    }// GEN-LAST:event_KetLaporKeyPressed

    private void MSAKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_MSAKeyPressed
        Valid.pindah(evt, BJM, Hasil);
    }// GEN-LAST:event_MSAKeyPressed

    private void NyeriKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_NyeriKeyPressed
        Valid.pindah(evt, KetAlatBantu, Provokes);
    }// GEN-LAST:event_NyeriKeyPressed

    private void ProvokesKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_ProvokesKeyPressed
        Valid.pindah(evt, Nyeri, KetProvokes);
    }// GEN-LAST:event_ProvokesKeyPressed

    private void KetProvokesKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KetProvokesKeyPressed
        Valid.pindah(evt, Provokes, Quality);
    }// GEN-LAST:event_KetProvokesKeyPressed

    private void QualityKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_QualityKeyPressed
        Valid.pindah(evt, KetProvokes, KetQuality);
    }// GEN-LAST:event_QualityKeyPressed

    private void KetQualityKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KetQualityKeyPressed
        Valid.pindah(evt, Quality, Lokasi);
    }// GEN-LAST:event_KetQualityKeyPressed

    private void LokasiKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_LokasiKeyPressed
        Valid.pindah(evt, KetQuality, Menyebar);
    }// GEN-LAST:event_LokasiKeyPressed

    private void MenyebarKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_MenyebarKeyPressed
        Valid.pindah(evt, Lokasi, SkalaNyeri);
    }// GEN-LAST:event_MenyebarKeyPressed

    private void SkalaNyeriKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_SkalaNyeriKeyPressed
        Valid.pindah(evt, Menyebar, Durasi);
    }// GEN-LAST:event_SkalaNyeriKeyPressed

    private void DurasiKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_DurasiKeyPressed
        Valid.pindah(evt, SkalaNyeri, NyeriHilang);
    }// GEN-LAST:event_DurasiKeyPressed

    private void NyeriHilangKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_NyeriHilangKeyPressed
        Valid.pindah(evt, Durasi, KetNyeri);
    }// GEN-LAST:event_NyeriHilangKeyPressed

    private void KetNyeriKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KetNyeriKeyPressed
        Valid.pindah(evt, NyeriHilang, PadaDokter);
    }// GEN-LAST:event_KetNyeriKeyPressed

    private void PadaDokterKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_PadaDokterKeyPressed
        Valid.pindah(evt, KetNyeri, KetDokter);
    }// GEN-LAST:event_PadaDokterKeyPressed

    private void KetDokterKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KetDokterKeyPressed
        Valid.pindah(evt, PadaDokter, ATS);
    }// GEN-LAST:event_KetDokterKeyPressed

    private void TglAsuhanKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_TglAsuhanKeyPressed
        Valid.pindah2(evt, Rencana, RPD);
    }// GEN-LAST:event_TglAsuhanKeyPressed

    private void StatusBudayaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_StatusBudayaKeyPressed
        Valid.pindah(evt, KetTinggal, KetBudaya);
    }// GEN-LAST:event_StatusBudayaKeyPressed

    private void KetBudayaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KetBudayaKeyPressed
        Valid.pindah(evt, StatusBudaya, PendidikanPJ);
    }// GEN-LAST:event_KetBudayaKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowOpened
        try {
            if (Valid.daysOld("./cache/masalahkeperawatanigd.iyem") < 30) {
                runBackground(() -> tampilMasalah2());
            } else {
                runBackground(() -> tampilMasalah());
            }
        } catch (Exception e) {
        }

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }
            });

            TCariMasalah.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCariMasalah.getText().length() > 2) {
                        runBackground(() -> tampilMasalah2());
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCariMasalah.getText().length() > 2) {
                        runBackground(() -> tampilMasalah2());
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCariMasalah.getText().length() > 2) {
                        runBackground(() -> tampilMasalah2());
                    }
                }
            });
        }
    }// GEN-LAST:event_formWindowOpened

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ChkAccorActionPerformed
        if (tbObat.getSelectedRow() != -1) {
            isMenu();
        } else {
            ChkAccor.setSelected(false);
            JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih data yang mau ditampilkan...!!!!");
        }
    }// GEN-LAST:event_ChkAccorActionPerformed

    private void BtnPrint1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnPrint1ActionPerformed
        if (tbObat.getSelectedRow() > -1) {
            // Query cetak disinkronkan total dgn <queryString> di
            // rptCetakPenilaianAwalKeperawatanIGD.jrxml (redesain RM
            // 03-4/05/06) - masalah_terpilih (Diagnosis Keperawatan) &
            // obat_infus_ringkas (Pemberian Obat/Infus) sekarang dihitung
            // via subquery GROUP_CONCAT langsung di SELECT, jadi tidak
            // perlu lagi pre-fetch terpisah spt versi lama (yg juga masih
            // query ke tabel Rencana Keperawatan lama yg sudah ditinggalkan).
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            finger = Sequel.cariIsi(
                    "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",
                    tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
            param.put("finger",
                    "Dikeluarkan di " + akses.getnamars()
                            + "\nKabupaten " + akses.getkabupatenrs()
                            + "\nDitandatangani oleh: "
                            + tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
            String qryCetak = "select penilaian_awal_keperawatan_igd.*, " +
                    "reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, TIMESTAMPDIFF(YEAR,pasien.tgl_lahir,CURDATE()) as umur, if(pasien.jk='L','Laki-Laki','Perempuan') as jk, "
                    +
                    "reg_periksa.tgl_registrasi, reg_periksa.jam_reg, " +
                    "petugas.nama as nm_perawat, coalesce(pegawai_perawat.nik,petugas.nip) as nip_perawat, " +
                    "dokter.nm_dokter, coalesce(pegawai_dokter.nik,dokter.kd_dokter) as nip_dokter, dokter.no_ijn_praktek, " +
                    "(select group_concat(mmk.nama_masalah separator ', ') from penilaian_awal_keperawatan_igd_masalah pakm "
                    +
                    " inner join master_masalah_keperawatan_igd mmk on mmk.kode_masalah=pakm.kode_masalah " +
                    " where pakm.no_rawat=penilaian_awal_keperawatan_igd.no_rawat) as masalah_terpilih, " +
                    "(select group_concat(concat(pako.pukul,'|',pako.nama_obat,'|',pako.dosis,'|',pako.rute,'|',coalesce(pt1.nama,'-'),'|',coalesce(pt2.nama,'-')) order by pako.pukul separator ';;') "
                    +
                    " from penilaian_awal_keperawatan_igd_obat pako " +
                    " left join petugas pt1 on pt1.nip=pako.nip_diperiksa " +
                    " left join petugas pt2 on pt2.nip=pako.nip_diberikan " +
                    " where pako.no_rawat=penilaian_awal_keperawatan_igd.no_rawat) as obat_infus_ringkas "
                    +
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                    "inner join penilaian_awal_keperawatan_igd on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat "
                    +
                    "inner join petugas on penilaian_awal_keperawatan_igd.nip=petugas.nip " +
                    "left join pegawai pegawai_perawat on pegawai_perawat.nik=petugas.nip " +
                    "left join dokter on dokter.kd_dokter=penilaian_awal_keperawatan_igd.kp_kd_dokter " +
                    "left join pegawai pegawai_dokter on pegawai_dokter.nik=dokter.kd_dokter " +
                    "where reg_periksa.no_rawat='" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString() + "'";
            String modeCetak = (String) JOptionPane.showInputDialog(null, "Silahkan pilih mode cetak..!",
                    "Cetak Laporan Pengkajian Awal Keperawatan IGD", JOptionPane.QUESTION_MESSAGE, null,
                    new Object[] { "Lembar", "PDF" }, "Lembar");
            if (modeCetak == null) {
                return;
            }
            if (modeCetak.equals("PDF")) {
                Valid.MyReportqrypdf("rptCetakPenilaianAwalKeperawatanIGD.jasper", "report",
                        "::[ Laporan Pengkajian Awal Keperawatan IGD ]::", qryCetak, param);
            } else {
                Valid.MyReportqry("rptCetakPenilaianAwalKeperawatanIGD.jasper", "report",
                        "::[ Laporan Pengkajian Awal Keperawatan IGD ]::", qryCetak, param);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih data terlebih dahulu..!!!!");
        }
    }// GEN-LAST:event_BtnPrint1ActionPerformed

    private void TPasienActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_TPasienActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_TPasienActionPerformed

    private void StatusKehamilanKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_StatusKehamilanKeyPressed
        Valid.pindah(evt, RPO, HPHT);
    }// GEN-LAST:event_StatusKehamilanKeyPressed

    private void TekananKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_TekananKeyPressed
        Valid.pindah(evt, Gravida, Pupil);
    }// GEN-LAST:event_TekananKeyPressed

    private void PupilKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_PupilKeyPressed
        Valid.pindah(evt, Tekanan, Neurosensorik);
    }// GEN-LAST:event_PupilKeyPressed

    private void NeurosensorikKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_NeurosensorikKeyPressed
        Valid.pindah(evt, Pupil, Integumen);
    }// GEN-LAST:event_NeurosensorikKeyPressed

    private void IntegumenKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_IntegumenKeyPressed
        Valid.pindah(evt, Neurosensorik, Turgor);
    }// GEN-LAST:event_IntegumenKeyPressed

    private void TurgorKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_TurgorKeyPressed
        Valid.pindah(evt, Integumen, Edema);
    }// GEN-LAST:event_TurgorKeyPressed

    private void EdemaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_EdemaKeyPressed
        Valid.pindah(evt, Turgor, Mukosa);
    }// GEN-LAST:event_EdemaKeyPressed

    private void MukosaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_MukosaKeyPressed
        Valid.pindah(evt, Edema, Perdarahan);
    }// GEN-LAST:event_MukosaKeyPressed

    private void PerdarahanKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_PerdarahanKeyPressed
        Valid.pindah(evt, Mukosa, JumlahPerdarahan);
    }// GEN-LAST:event_PerdarahanKeyPressed

    private void IntoksikasiKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_IntoksikasiKeyPressed
        Valid.pindah(evt, WarnaPerdarahan, BAB);
    }// GEN-LAST:event_IntoksikasiKeyPressed

    private void PsikologisKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_PsikologisKeyPressed
        Valid.pindah(evt, LBAK, Jiwa);
    }// GEN-LAST:event_PsikologisKeyPressed

    private void JiwaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_JiwaKeyPressed
        Valid.pindah(evt, Psikologis, Perilaku);
    }// GEN-LAST:event_JiwaKeyPressed

    private void PerilakuKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_PerilakuKeyPressed
        Valid.pindah(evt, Jiwa, Dilaporkan);
    }// GEN-LAST:event_PerilakuKeyPressed

    private void HubunganKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_HubunganKeyPressed
        Valid.pindah(evt, Sebutkan, TinggalDengan);
    }// GEN-LAST:event_HubunganKeyPressed

    private void StatusPernikahanActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_StatusPernikahanActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_StatusPernikahanActionPerformed

    private void KetPendidikanPJKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KetPendidikanPJKeyPressed
        Valid.pindah(evt, PendidikanPJ, Edukasi);
    }// GEN-LAST:event_KetPendidikanPJKeyPressed

    private void PendidikanPJKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_PendidikanPJKeyPressed
        Valid.pindah(evt, KetBudaya, KetPendidikanPJ);
    }// GEN-LAST:event_PendidikanPJKeyPressed

    private void InformasiKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_InformasiKeyPressed
        Valid.pindah(evt, TglAsuhan, KeluhanUtama);
    }// GEN-LAST:event_InformasiKeyPressed

    private void HPHTKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_HPHTKeyPressed
        Valid.pindah(evt, StatusKehamilan, Para);
    }// GEN-LAST:event_HPHTKeyPressed

    private void ParaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_ParaKeyPressed
        Valid.pindah(evt, HPHT, Abortus);
    }// GEN-LAST:event_ParaKeyPressed

    private void AbortusKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_AbortusKeyPressed
        Valid.pindah(evt, Para, Gravida);
    }// GEN-LAST:event_AbortusKeyPressed

    private void GravidaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_GravidaKeyPressed
        Valid.pindah(evt, Abortus, Tekanan);
    }// GEN-LAST:event_GravidaKeyPressed

    private void JumlahPerdarahanKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_JumlahPerdarahanKeyPressed
        Valid.pindah(evt, Perdarahan, WarnaPerdarahan);
    }// GEN-LAST:event_JumlahPerdarahanKeyPressed

    private void WarnaPerdarahanKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_WarnaPerdarahanKeyPressed
        Valid.pindah(evt, JumlahPerdarahan, Intoksikasi);
    }// GEN-LAST:event_WarnaPerdarahanKeyPressed

    private void BABKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BABKeyPressed
        Valid.pindah(evt, Intoksikasi, XBAB);
    }// GEN-LAST:event_BABKeyPressed

    private void XBABKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_XBABKeyPressed
        Valid.pindah(evt, BAB, KBAB);
    }// GEN-LAST:event_XBABKeyPressed

    private void KBABKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_KBABKeyPressed
        Valid.pindah(evt, XBAB, WBAB);
    }// GEN-LAST:event_KBABKeyPressed

    private void WBABKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_WBABKeyPressed
        Valid.pindah(evt, KBAB, BAK);
    }// GEN-LAST:event_WBABKeyPressed

    private void BAKKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BAKKeyPressed
        Valid.pindah(evt, WBAB, XBAK);
    }// GEN-LAST:event_BAKKeyPressed

    private void XBAKKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_XBAKKeyPressed
        Valid.pindah(evt, BAK, WBAK);
    }// GEN-LAST:event_XBAKKeyPressed

    private void WBAKKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_WBAKKeyPressed
        Valid.pindah(evt, XBAK, LBAK);
    }// GEN-LAST:event_WBAKKeyPressed

    private void LBAKKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_LBAKKeyPressed
        Valid.pindah(evt, WBAK, Psikologis);
    }// GEN-LAST:event_LBAKKeyPressed

    private void DilaporkanKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_DilaporkanKeyPressed
        Valid.pindah(evt, Perilaku, Sebutkan);
    }// GEN-LAST:event_DilaporkanKeyPressed

    private void SebutkanKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_SebutkanKeyPressed
        Valid.pindah(evt, Dilaporkan, Hubungan);
    }// GEN-LAST:event_SebutkanKeyPressed

    private void tbMasalahKeperawatanMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_tbMasalahKeperawatanMouseClicked
        if (tabModeMasalah.getRowCount() != 0) {
            try {
                runBackground(() -> tampilRencana2());
            } catch (java.lang.NullPointerException e) {
            }
        }
    }// GEN-LAST:event_tbMasalahKeperawatanMouseClicked

    private void tbMasalahKeperawatanKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_tbMasalahKeperawatanKeyPressed
        if (tabModeMasalah.getRowCount() != 0) {
            if (evt.getKeyCode() == KeyEvent.VK_SHIFT) {
                TCariMasalah.setText("");
                TCariMasalah.requestFocus();
            }
        }
    }// GEN-LAST:event_tbMasalahKeperawatanKeyPressed

    private void tbMasalahKeperawatanKeyReleased(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_tbMasalahKeperawatanKeyReleased
        if (tabModeMasalah.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP)
                    || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    runBackground(() -> tampilRencana2());
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }// GEN-LAST:event_tbMasalahKeperawatanKeyReleased

    private void BtnTambahMasalahActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnTambahMasalahActionPerformed
        if (masalahkeperawatan == null || !masalahkeperawatan.isDisplayable()) {
            masalahkeperawatan = new MasterMasalahKeperawatanIGD(null, false);
            masalahkeperawatan.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            masalahkeperawatan.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    masalahkeperawatan = null;
                }
            });

            masalahkeperawatan.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            masalahkeperawatan.setLocationRelativeTo(internalFrame1);
        }
        if (masalahkeperawatan == null)
            return;
        if (!masalahkeperawatan.isVisible()) {
            masalahkeperawatan.isCek();
        }

        if (masalahkeperawatan.isVisible()) {
            masalahkeperawatan.toFront();
            return;
        }
        masalahkeperawatan.setVisible(true);
    }// GEN-LAST:event_BtnTambahMasalahActionPerformed

    private void BtnAllMasalahActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnAllMasalahActionPerformed
        TCari.setText("");
        runBackground(() -> tampilMasalah());
    }// GEN-LAST:event_BtnAllMasalahActionPerformed

    private void BtnAllMasalahKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnAllMasalahKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnAllMasalahActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCariMasalah, TCariMasalah);
        }
    }// GEN-LAST:event_BtnAllMasalahKeyPressed

    private void BtnCariMasalahActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnCariMasalahActionPerformed
        runBackground(() -> tampilMasalah2());
    }// GEN-LAST:event_BtnCariMasalahActionPerformed

    private void BtnCariMasalahKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnCariMasalahKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            runBackground(() -> tampilMasalah2());
        } else if ((evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) || (evt.getKeyCode() == KeyEvent.VK_TAB)) {
            Rencana.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            KetDokter.requestFocus();
        }
    }// GEN-LAST:event_BtnCariMasalahKeyPressed

    private void TCariMasalahKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_TCariMasalahKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            runBackground(() -> tampilMasalah2());
        } else if ((evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) || (evt.getKeyCode() == KeyEvent.VK_TAB)) {
            Rencana.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            KetDokter.requestFocus();
        }
    }// GEN-LAST:event_TCariMasalahKeyPressed

    private void RencanaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_RencanaKeyPressed
        Valid.pindah2(evt, TCariMasalah, BtnSimpan);
    }// GEN-LAST:event_RencanaKeyPressed

    private void TCariRencanaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_TCariRencanaKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            runBackground(() -> tampilRencana2());
        } else if ((evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) || (evt.getKeyCode() == KeyEvent.VK_TAB)) {
            BtnCariRencana.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            TCariMasalah.requestFocus();
        }
    }// GEN-LAST:event_TCariRencanaKeyPressed

    private void BtnCariRencanaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnCariRencanaActionPerformed
        runBackground(() -> tampilRencana2());
    }// GEN-LAST:event_BtnCariRencanaActionPerformed

    private void BtnCariRencanaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnCariRencanaKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            runBackground(() -> tampilRencana2());
        } else if ((evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) || (evt.getKeyCode() == KeyEvent.VK_TAB)) {
            BtnSimpan.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            TCariRencana.requestFocus();
        }
    }// GEN-LAST:event_BtnCariRencanaKeyPressed

    private void BtnAllRencanaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnAllRencanaActionPerformed
        TCariRencana.setText("");
        runBackground(() -> LoadRencana());
    }// GEN-LAST:event_BtnAllRencanaActionPerformed

    private void BtnAllRencanaKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_BtnAllRencanaKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnAllRencanaActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCariRencana, TCariRencana);
        }
    }// GEN-LAST:event_BtnAllRencanaKeyPressed

    private void BtnTambahRencanaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnTambahRencanaActionPerformed
        if (rencanakeperawatan == null || !rencanakeperawatan.isDisplayable()) {
            rencanakeperawatan = new MasterRencanaKeperawatanIGD(null, false);
            rencanakeperawatan.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            rencanakeperawatan.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    rencanakeperawatan = null;
                }
            });

            rencanakeperawatan.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            rencanakeperawatan.setLocationRelativeTo(internalFrame1);
        }
        if (rencanakeperawatan == null)
            return;
        if (!rencanakeperawatan.isVisible()) {
            rencanakeperawatan.isCek();
        }

        if (rencanakeperawatan.isVisible()) {
            rencanakeperawatan.toFront();
            return;
        }
        rencanakeperawatan.setVisible(true);
    }// GEN-LAST:event_BtnTambahRencanaActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMPenilaianAwalKeperawatanIGD dialog = new RMPenilaianAwalKeperawatanIGD(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // ===================================================================
    // Helper Format Cetakan Penunjang (Konsisten dengan Awal Medis IGD)
    // ===================================================================
    public static String getNamaPemeriksaanLab(String raw) {
        return RMPenilaianAwalMedisIGD.getNamaPemeriksaanLab(raw);
    }

    public static String getStatusPenunjangLab(String raw) {
        return RMPenilaianAwalMedisIGD.getStatusPenunjangLab(raw);
    }

    public static String getPemeriksaanRad(String rontgenRaw, String ctScanRaw) {
        String rad1 = RMPenilaianAwalMedisIGD.getPemeriksaanRad(rontgenRaw);
        String rad2 = RMPenilaianAwalMedisIGD.getPemeriksaanRad(ctScanRaw);
        if (rad1.equals("-") && rad2.equals("-")) return "-";
        if (rad1.equals("-")) return rad2;
        if (rad2.equals("-")) return rad1;
        if (rad1.equalsIgnoreCase(rad2)) return rad1;

        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<String>();
        for (String s : rad1.split(",")) {
            String st = s.trim();
            if (!st.isEmpty() && !st.equals("-")) set.add(st);
        }
        for (String s : rad2.split(",")) {
            String st = s.trim();
            if (!st.isEmpty() && !st.equals("-")) set.add(st);
        }
        if (set.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (String s : set) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(s);
        }
        return sb.toString();
    }

    public static String getStatusRad(String rontgenRaw, String ctScanRaw) {
        String st1 = RMPenilaianAwalMedisIGD.getStatusRad(rontgenRaw);
        String st2 = RMPenilaianAwalMedisIGD.getStatusRad(ctScanRaw);
        if (st1.contains("Proses") || st1.contains("Menunggu") || st2.contains("Proses") || st2.contains("Menunggu")) {
            return "Sedang Proses / Menunggu Hasil";
        }
        if (!st1.equals("-") || !st2.equals("-")) {
            return "Hasil Selesai / Terlampir";
        }
        return "-";
    }

    // ===================================================================
    // [RM 03-4/03-5/03-6] Field baru HALAMAN 1 - lihat initKepTambahan().
    // ===================================================================
    private widget.PanelBiasa panelKepAtas;
    private int geserKep1;
    private int yBawahLama;
    private int lebarFormInput = 870;
    // Panel Diagnosis..Obgyn dipecah dari panelKepTiga (FLACC/PQRST) supaya
    // bisa jadi seksi/halaman sendiri - lihat initKepTambahanHal3() &
    // susunHalamanKep().
    private widget.PanelBiasa panelKepTigaDiag;
    // Label kartu identitas pada banner atas (buatBannerKep/sinkronKartuKep).
    private widget.Label lblKartuNama, lblKartuRM, lblKartuRawat, lblKartuLokasi;
    private AvatarBulatKep avatarKartuKep;
    // Baris turunan "Laki-laki - 36 Th" di kartu identitas. Bukan field baru:
    // isinya dihitung dari Jk + TglLahir yg memang sudah diisi tampil().
    private widget.Label lblKartuInfo;
    // [Bagian A] Tiga zona rangka layar yg dirakit susunRangkaKep().
    // barPasienKep = hasil buatBannerKep() yg dipindah ke NORTH internalFrame2
    // (dulu ikut menggulung di dalam FormInput); railKep = tempat navigasi
    // bagian, masih kosong & disembunyikan sampai Sesi 2.
    private widget.PanelBiasa barPasienKep, railKep;
    private widget.ScrollPane scrollRailKep, scrollSeksiKep;

    /**
     * Panel isi rail harus mengikuti lebar viewport. PanelBiasa biasa memakai
     * preferred width anak-anaknya; ketika scrollbar vertikal muncul, sisi
     * kanan rail tetap selebar ukuran lama lalu tertutup scrollbar karena
     * horizontal scrollbar memang dinonaktifkan.
     */
    private static final class RailKepScrollable extends widget.PanelBiasa
            implements javax.swing.Scrollable {
        @Override
        public java.awt.Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return Math.max(16, visibleRect.height - 16);
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

    /**
     * [Bagian C] Satu entri navigasi bagian.
     *
     * `strip` = header strip yg dihasilkan tambahBarisKepJudul(), dipakai
     * sebagai penanda batas bagian. `induk` = panel section tempat strip itu
     * ditanam SAAT DIBANGUN; dicatat lewat IDENTITAS objek, bukan nama field,
     * karena `panelKepTiga` ditukar di tengah initKepTambahanHal3() (isi FLACC
     * dipisah dari isi Diagnosis..Obgyn yg jadi panelKepTigaDiag).
     *
     * `panel` = sub-panel berisi HANYA bagian ini, hasil pecahSeksiKep(). Ini
     * yg ditempel ke kartu halaman; `induk` sesudah itu tinggal jadi catatan
     * asal-usul. null berarti bagian itu belum/tidak dipecah, dan
     * kelompokkanHalamanKep() melewatinya.
     */
    private static final class SeksiKep {
        final String judul;
        final widget.PanelBiasa strip;
        final widget.PanelBiasa induk;
        final int urutBangun;
        widget.PanelBiasa panel;

        SeksiKep(String judul, widget.PanelBiasa strip, widget.PanelBiasa induk, int urutBangun) {
            this.judul = judul;
            this.strip = strip;
            this.induk = induk;
            this.urutBangun = urutBangun;
        }
    }

    /** Daftar bagian, diisi tambahBarisKepJudul() saat section dibangun. */
    private final java.util.List<SeksiKep> seksiKep = new java.util.ArrayList<>();

    /**
     * [Bagian D] Satu halaman = satu kartu di FormInput + satu baris di rail.
     *
     * Berisi satu bagian atau beberapa bagian sekerabat (lihat
     * GRUP_HALAMAN_KEP). `panel` kartunya, `baris`/`lblJudul`/`aksen` widget
     * baris rail-nya - ketiganya dipegang supaya sorotHalamanKep() bisa
     * mewarnai ulang tanpa mencari-cari di dalam hierarki.
     */
    private static final class HalamanKep {
        final String judul;
        final java.util.List<SeksiKep> anggota = new java.util.ArrayList<>();
        String subjudul = "";
        widget.PanelBiasa panel;
        widget.PanelBiasa baris;
        widget.Label lblJudul;
        widget.Label lblNomor;
        widget.Label lblHitunganRail;
        widget.PanelBiasa aksen;
        widget.Label lblBadgeNomor;
        widget.PanelBiasa pnlBadgeStatus;
        widget.Label lblBadgeStatus;
        widget.Button btnTidakRelevan;
        boolean tidakRelevan = false;
        int totalWajib = 0;
        int terisiWajib = 0;
        int terisiOpsional = 0;

        HalamanKep(String judul) {
            this.judul = judul;
        }
    }

    /** Daftar halaman, disusun kelompokkanHalamanKep() dari GRUP_HALAMAN_KEP. */
    private final java.util.List<HalamanKep> halamanKep = new java.util.ArrayList<>();
    /** Index halaman yg sedang disorot di rail, -1 = belum ada. */
    private int halamanAktifKep = -1;
    /**
     * true = tampilkan satu halaman saja (default, sesuai prompt "satu bagian
     * per layar"); false = semua halaman bertumpuk & bisa digulung.
     */
    private boolean satuHalamanKep = true;
    private widget.RadioButton rbSatuHalamanKep, rbSemuaHalamanKep;
    private javax.swing.ButtonGroup bgModeHalamanKep;
    private widget.Label lblProgresJudulKep, lblProgresHitungKep, lblProgresPersenKep;
    private javax.swing.JProgressBar pbProgresKep;
    private widget.Label lblProgresBawahKep;
    private widget.PanelBiasa panelSkalaNyeriToggle;
    private javax.swing.ButtonGroup btnGroupSkalaNyeri;
    private javax.swing.JToggleButton[] btnSkalaNyeri;
    private widget.Label lblInterpretasiNyeri;

    // -- Tanggal & Jam Registrasi / Asuhan (pola Registrasi IGD) --
    private widget.ComboBox CmbJam, CmbMenit, CmbDetik;
    private widget.CekBox ChkJln;
    private javax.swing.Timer timerJam;

    // -- Riwayat Alergi --
    private widget.RadioButton RbAlergiTidak, RbAlergiYa;
    private javax.swing.ButtonGroup bgAlergi;
    private widget.TextBox TAlergiSebutkan;

    // -- Keadaan Umum (Tanda Vital) --
    private widget.TextBox TdKep, NadiKep, SuhuKep, SpoKep, RrKep, BbKep, TbKep;

    // -- Asesmen Psikologis --
    private widget.CekBox CekPsiTenang, CekPsiCemas, CekPsiTakut, CekPsiSedih, CekPsiMarah, CekPsiBunuhDiri, CekPsiMasalahPerilaku;
    private widget.PanelBiasa barisMasalahPrilaku;
    private widget.RadioButton RbMasalahPrilakuYa, RbMasalahPrilakuTidak;
    private javax.swing.ButtonGroup bgMasalahPrilaku;
    private widget.TextBox TMasalahPrilakuSebutkan;

    // -- Status Sosial dan Ekonomis --
    // `status_pernikahan`/`pekerjaan_kep`/`pendidikan_pasien_kep` SENGAJA
    // widget/kolom BARU terpisah dari StatusPernikahan/Pekerjaan/
    // PendidikanPasien lama (read-only dari data registrasi pasien via
    // stts_nikah/pekerjaan/pnd) - permintaan eksplisit user, widget lama
    // TIDAK disentuh/dihapus.
    private widget.RadioButton RbNikahBelumMenikah, RbNikahMenikah, RbNikahCerai, RbNikahLainnya;
    private javax.swing.ButtonGroup bgStatusPernikahan;
    private widget.TextBox TKetStatusPernikahan;
    private widget.RadioButton RbTinggalSendiri, RbTinggalKeluarga, RbTinggalLainnya;
    private javax.swing.ButtonGroup bgTempatTinggal;
    private widget.TextBox TKetTempatTinggal;
    private widget.TextBox TPekerjaanKep;
    private widget.TextBox TPendidikanPasienKep;

    // -- Skrining Geriatri --
    private widget.RadioButton RbGeri1Ya, RbGeri1Tidak;
    private javax.swing.ButtonGroup bgGeri1;
    private widget.RadioButton RbGeri2Ya, RbGeri2Tidak;
    private javax.swing.ButtonGroup bgGeri2;
    private widget.RadioButton RbGeri3Ya, RbGeri3Tidak;
    private javax.swing.ButtonGroup bgGeri3;

    // -- Skrining Resiko Pasien Jatuh (Get Up and Go) --
    private widget.RadioButton RbJatuhCaraBerjalanYa, RbJatuhCaraBerjalanTidak;
    private javax.swing.ButtonGroup bgJatuhCaraBerjalan;
    private widget.RadioButton RbJatuhMenopangYa, RbJatuhMenopangTidak;
    private javax.swing.ButtonGroup bgJatuhMenopang;
    private widget.RadioButton RbJatuhTidakBeresiko, RbJatuhBeresiko;
    private javax.swing.ButtonGroup bgJatuhHasil;

    // -- Pengkajian Spiritual, Kepercayaan, Fungsional dan Budaya --
    // (Agama/Bahasa/Cacat Tubuh REUSE widget Agama/Bahasa/CacatFisik lama)
    private widget.RadioButton RbWajibIbadahBaligh, RbWajibIbadahBelumBaligh, RbWajibIbadahHalanganLain;
    private javax.swing.ButtonGroup bgWajibIbadah;
    private widget.TextBox TKetWajibIbadah;
    private widget.RadioButton RbThaharohBerwudhu, RbThaharohTayamum;
    private javax.swing.ButtonGroup bgThaharoh;
    private widget.RadioButton RbSholatBerdiri, RbSholatDuduk, RbSholatBerbaring;
    private javax.swing.ButtonGroup bgSholat;
    private widget.RadioButton RbNilaiKepercayaanTidak, RbNilaiKepercayaanYa;
    private javax.swing.ButtonGroup bgNilaiKepercayaan;
    private widget.TextBox TKetNilaiKepercayaan;
    private widget.RadioButton RbBudayaTidak, RbBudayaAda;
    private javax.swing.ButtonGroup bgBudaya;
    private widget.TextBox TAgama;
    private widget.ComboBox CmbAgama;
    private widget.PanelBiasa barisWajibIbadah, barisThaharoh, barisSholat;
    private widget.CekBox CekCacatTidak, CekCacatTunaNetra, CekCacatTunaDaksa, CekCacatTunaRungu, CekCacatTunaWicara;

    private void setAgamaSelection(String agm) {
        if (CmbAgama == null) return;
        if (agm == null || agm.trim().isEmpty() || "-".equals(agm.trim())) {
            CmbAgama.setSelectedIndex(0);
        } else {
            boolean found = false;
            for (int i = 0; i < CmbAgama.getItemCount(); i++) {
                if (agm.trim().equalsIgnoreCase(CmbAgama.getItemAt(i).toString().trim())) {
                    CmbAgama.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                CmbAgama.setSelectedIndex(0);
            }
        }
    }

    // ===================================================================
    // [RM 03-5] Field baru HALAMAN 2 - lihat initKepTambahanHal2().
    // ===================================================================
    private widget.PanelBiasa panelKepDua;

    // -- Pilihan Jenis Skrining Gizi (Dewasa vs Anak) --
    private widget.RadioButton RbPilihGiziDewasa, RbPilihGiziAnak;
    private javax.swing.ButtonGroup bgPilihGizi;
    private widget.PanelBiasa barisPilihanGizi, panelSeksiGiziDewasa, panelSeksiGiziAnak;

    // -- Skrining Gizi Dewasa (MST) --
    private widget.RadioButton RbMstTidakAda, RbMstTidakYakin, RbMstYa;
    private javax.swing.ButtonGroup bgMstPenurunanBb;
    private widget.ComboBox MstPenurunanBbKg;
    private widget.RadioButton RbMstAsupanYa, RbMstAsupanTidak;
    private javax.swing.ButtonGroup bgMstAsupan;
    private widget.TextBox TMstTotalSkor;
    private widget.RadioButton RbMstDiagnosisYa, RbMstDiagnosisTidak;
    private javax.swing.ButtonGroup bgMstDiagnosis;
    private widget.TextBox TMstDiagnosisSebutkan;
    private widget.RadioButton RbMstRisiko, RbMstTidakRisiko;
    private javax.swing.ButtonGroup bgMstKesimpulan;

    // -- Skrining Gizi Anak --
    private widget.RadioButton RbGiziAnakKurusYa, RbGiziAnakKurusTidak;
    private javax.swing.ButtonGroup bgGiziAnakKurus;
    private widget.RadioButton RbGiziAnakTurunBbYa, RbGiziAnakTurunBbTidak;
    private javax.swing.ButtonGroup bgGiziAnakTurunBb;
    private widget.RadioButton RbGiziAnakKondisiYa, RbGiziAnakKondisiTidak;
    private javax.swing.ButtonGroup bgGiziAnakKondisi;
    private widget.RadioButton RbGiziAnakRisikoYa, RbGiziAnakRisikoTidak;
    private javax.swing.ButtonGroup bgGiziAnakRisiko;
    private widget.TextBox TGiziAnakTotalSkor;
    private widget.RadioButton RbGiziAnakRendah, RbGiziAnakSedang, RbGiziAnakTinggi;
    private javax.swing.ButtonGroup bgGiziAnakKesimpulan;

    // -- Asesmen Nyeri (toggle baru) --
    private widget.RadioButton RbNyeriAdaYa, RbNyeriAdaTidak;
    private javax.swing.ButtonGroup bgNyeriAda;
    private widget.ComboBox CmbKategoriUsiaNyeri;
    private widget.PanelBiasa panelSeksiNyeriSkalaCommon; // wrapper bersama: Lokasi + Skala Nyeri 0-10 toggle
    private widget.PanelBiasa panelSeksiNyeriNumeric;   // wrapper: gambar Numeric
    private widget.PanelBiasa panelSeksiNyeriWongBaker; // wrapper: gambar Wong-Baker
    private widget.PanelBiasa panelSeksiNyeriFlacc;     // wrapper: FLACC combos
    private widget.PanelBiasa panelSeksiNyeriPqrst;     // wrapper: PQRST

    // ===================================================================
    // [RM 03-6] Field baru HALAMAN 3 - lihat initKepTambahanHal3(). Diagnosis
    // Keperawatan (I.) TIDAK di sini - mekanisme JTable+checkbox lama
    // (tbMasalahKeperawatan) dipertahankan apa adanya, cuma datanya
    // ditambah (lihat migrasi SQL KELOMPOK W).
    // ===================================================================
    private widget.PanelBiasa panelKepTiga;

    // -- FLACC (bayi/anak <3 tahun) --
    private widget.ComboBox FlaccWajah, FlaccKaki, FlaccAktivitas, FlaccMenangis, FlaccBersuara;
    private widget.TextBox TFlaccTotal;

    // -- PQRST --
    private widget.CekBox CekProvCahaya, CekProvGelap, CekProvGerakan, CekProvBerbaring;
    private widget.CekBox CekQualTumpul, CekQualDitusuk, CekQualBerdenyut, CekQualTajam, CekQualPanas;
    private widget.RadioButton RbRadiationTidak, RbRadiationYa;
    private javax.swing.ButtonGroup bgRadiation;
    private widget.TextBox TRadiationKe;
    private widget.RadioButton RbSeverityTidakNyeri, RbSeverityRingan, RbSeveritySedang, RbSeverityBerat;
    private javax.swing.ButtonGroup bgSeverity;
    private widget.RadioButton RbTimeTerusMenerus, RbTimeHilangTimbul, RbTimeKurang30, RbTimeLebih30;
    private javax.swing.ButtonGroup bgTime;

    // -- II. Intervensi Keperawatan (checklist flat, bukan tabel master+anak) --
    private widget.CekBox CekIntervManajemenJalanNafas, CekIntervSuctioningJalanNafas, CekIntervManajemenAsamBasa,
            CekIntervManajemenSyok, CekIntervManajemenNyeri, CekIntervManajemenCairan, CekIntervMonitoringCairan,
            CekIntervManajemenLingkungan, CekIntervPendidikanKesehatan, CekIntervFeverTreatment,
            CekIntervManajemenSensasiPerifer, CekIntervMonitoringKardiovaskuler, CekIntervPencegahanPenekananLuka,
            CekIntervLainnya;
    private widget.TextBox TIntervLainnya;

    // -- IV. Kardiopulmonal --
    private widget.CekBox CekKardioPulseOximetri, CekKardioIntubasi, CekKardioRjp, CekKardioKardioversiDcShock;
    private widget.TextBox TKardioRjpKet;

    // -- V. Tindakan Penunjang --
    private widget.CekBox CekPenunjangCekLab, CekPenunjangRontgen, CekPenunjangCtScan;
    private widget.TextBox TPenunjangCekLabKet, TPenunjangRontgenKet, TPenunjangCtScanKet;
    private PanelPenunjangMedis panelPenunjangMedis;
    private javax.swing.JTextArea txtPenunjangLab, txtPenunjangRad;

    // -- VI. Tindakan Kolaborasi --
    private widget.CekBox CekKolabO2, CekKolabEkgMonitor, CekKolabNebulizer, CekKolabPasangInfus,
            CekKolabInjeksiSyringePump, CekKolabTransfusiDarah, CekKolabNgt, CekKolabBilasLambung,
            CekKolabDowerCatheter, CekKolabRestrainPengaman;
    private widget.TextBox TKolabO2Ket, TKolabPasangInfusKet, TKolabTransfusiDarahKet, TKolabNgtUkuran,
            TKolabBilasLambungKet, TKolabDowerCatheterUkuran;
    private widget.Label lblKolabO2;

    // -- VII. Trauma / Bedah --
    private widget.CekBox CekTraumaCervicalCollar, CekTraumaImmobilisasiTulangBelakang, CekTraumaGips,
            CekTraumaPerawatanLuka, CekTraumaJahitLuka, CekTraumaBalutanSpalk, CekTraumaSelangDadaWsd;
    private widget.TextBox TTraumaLainnya;

    // -- VIII. THT / Mata --
    private widget.CekBox CekThtIrigasiMataTelinga, CekThtAmbilKorpusAlienum;

    // -- IX. Tindakan Obgyn --
    private widget.CekBox CekObgynMembantuPartus, CekObgynPeriksaGinekologi, CekObgynDopplerCtgUsg,
            CekObgynPasangTampon;

    // ===================================================================
    // [Lanjutan RM 03-6] Field baru HALAMAN 4 (Kondisi Pasien Saat
    // Pindah/Pulang dari IGD) - lihat initKepTambahanHal4(). Tanda tangan
    // Perawat/Bidan+Dokter pakai QR code digital (bukan field di sini,
    // ditangani langsung di jrxml cetak, pola sama RM 03-1/03-2).
    // "Pemberian Obat/Infus" ada di method terpisah (CRUD independen,
    // lihat initObatInfus()) krn strukturnya tabel berulang, bukan field
    // statis.
    // ===================================================================
    private widget.PanelBiasa panelKepEmpat;
    private PanelTanggalDanJam DTPKpPukul;
    private PanelJamSaja DTPKpPukulSaja;
    private widget.TextBox TKpGcsE, TKpGcsV, TKpGcsM, TKpTd, TKpNadi, TKpSuhu, TKpPernafasan, TKpSpo;
    private widget.RadioButton RbKpNadiReguler, RbKpNadiIreguler;
    private javax.swing.ButtonGroup bgKpNadiReguler;
    private widget.CekBox CekKpMasukRs;
    // TKpRuang/TKpPetugasPenerima: penyimpan nilai (read-only) - diisi lewat
    // dialog pencarian (BtnKpRuang -> DlgCariKamar, BtnKpPetugasPenerima ->
    // DlgCariPetugas), bukan diketik manual lagi.
    private widget.TextBox TKpRuang, TKpPetugasPenerima;
    private widget.Button BtnKpRuang;
    private widget.TextBox NmKpPetugasPenerima;
    private widget.Button BtnKpPetugasPenerima;
    private widget.RadioButton RbKpPendampingDokter, RbKpPendampingPerawat;
    private javax.swing.ButtonGroup bgKpPendamping;
    private widget.RadioButton RbKpTlKamarOperasi, RbKpTlKamarBersalin, RbKpTlHcu, RbKpTlKamarJenazah, RbKpTlLainnya;
    private javax.swing.ButtonGroup bgKpTindakanLanjutan;
    private widget.TextBox TKpTindakanLanjutanKet;
    private widget.CekBox CekKpDirujuk;
    private widget.TextBox TKpDirujukKet, TKpDirujukAlasan;
    private widget.CekBox CekKpDipulangkan;
    private PanelTanggalDanJam DTPKpDipulangkanPukul;
    private widget.CekBox CekKpMeninggal;
    private PanelTanggalDanJam DTPKpMeninggalPukul;
    private widget.CekBox CekKpDoa;
    private widget.CekBox CekKpEdukasiMakanObat, CekKpEdukasiLuka, CekKpEdukasiDiet, CekKpEdukasiLain;
    private widget.TextBox TKpEdukasiDietKet, TKpEdukasiLainKet;
    private widget.TextBox KdDokterKp, NmDokterKp;
    private widget.Button BtnDokterKp;
    private widget.PanelBiasa selPetugasPenerima, selDirujukAlasan;

    // ===================================================================
    // [Lanjutan RM 03-6] Field "Pemberian Obat/Infus" - lihat
    // initObatInfusTambahan()/tampilObatInfus()/simpanObat()/hapusObat().
    // ===================================================================
    private widget.PanelBiasa panelKepObat;
    private DefaultTableModel tabModeObatInfus;
    private widget.Table tbObatInfus;
    private PanelJamSaja DTPObatPukul;
    private widget.TextBox TObatNama, TObatDosis, TObatRute;
    private widget.Button BtnObatNama;
    private widget.TextBox KdObatDiperiksa, NmObatDiperiksa;
    private widget.Button BtnObatDiperiksa;
    private widget.TextBox KdObatDiberikan, NmObatDiberikan;
    private widget.Button BtnObatDiberikan;
    private widget.Button BtnTambahObat, BtnHapusObat;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.ComboBox ADL;
    private widget.ComboBox ATS;
    private widget.TextBox Abortus;
    private widget.TextBox Agama;
    private widget.ComboBox Aktifitas;
    private widget.ComboBox AlatBantu;
    private widget.TextBox BAB;
    private widget.TextBox BAK;
    private widget.ComboBox BJM;
    private widget.TextBox Bahasa;
    private widget.Button BtnAll;
    private widget.Button BtnAllCari;
    private widget.Button BtnAllMasalah;
    private widget.Button BtnAllRencana;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnCariMasalah;
    private widget.Button BtnCariRencana;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPetugas;
    private widget.Button BtnPrint;
    private widget.Button BtnPrint1;
    private widget.Button BtnSimpan;
    private widget.Button BtnTambahMasalah;
    private widget.Button BtnTambahRencana;
    private widget.TextBox CacatFisik;
    private widget.CekBox ChkAccor;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.TextArea DetailRencana;
    private widget.TextBox Dilaporkan;
    private widget.TextBox Durasi;
    private widget.ComboBox Edema;
    private widget.ComboBox Edukasi;
    private widget.PanelBiasa FormInput;
    private widget.PanelBiasa FormMasalahRencana;
    private widget.PanelBiasa FormMenu;
    private widget.TextBox Gravida;
    private widget.TextBox HPHT;
    private widget.ComboBox Hasil;
    private widget.ComboBox Hubungan;
    private widget.ComboBox Informasi;
    private widget.ComboBox Integumen;
    private widget.ComboBox Intoksikasi;
    private widget.ComboBox Jiwa;
    private widget.TextBox Jk;
    private widget.TextBox JumlahPerdarahan;
    private widget.TextBox KBAB;
    private widget.TextBox KdPetugas;
    private widget.TextArea KeluhanUtama;
    private widget.TextBox KetAlatBantu;
    private widget.TextBox KetBudaya;
    private widget.TextBox KetDokter;
    private widget.TextBox KetEdukasi;
    private widget.TextBox KetLapor;
    private widget.TextBox KetNyeri;
    private widget.TextBox KetPendidikanPJ;
    private widget.TextBox KetProvokes;
    private widget.TextBox KetQuality;
    private widget.TextBox KetTinggal;
    private widget.TextBox LBAK;
    private widget.Label LCount;
    private widget.ComboBox Lapor;
    private widget.editorpane LoadHTML;
    private widget.ScrollPane ScrollHTML;
    private widget.TextBox Lokasi;
    private widget.ComboBox MSA;
    private widget.ComboBox Menyebar;
    private widget.ComboBox Mukosa;
    private widget.ComboBox Neurosensorik;
    private widget.TextBox NmPetugas;
    private widget.ComboBox Nyeri;
    private widget.ComboBox NyeriHilang;
    private widget.ComboBox PadaDokter;
    private widget.PanelBiasa PanelAccor;
    private usu.widget.glass.PanelGlass PanelWall;
    private widget.TextBox Para;
    private widget.TextBox Pekerjaan;
    private widget.TextBox Pembayaran;
    private widget.ComboBox PendidikanPJ;
    private widget.TextBox PendidikanPasien;
    private widget.ComboBox Perdarahan;
    private widget.ComboBox Perilaku;
    private widget.ComboBox Provokes;
    private widget.ComboBox Psikologis;
    private widget.ComboBox Pupil;
    private widget.ComboBox Quality;
    private widget.TextArea RPD;
    private widget.TextArea RPO;
    private widget.TextArea Rencana;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll10;
    private widget.ScrollPane Scroll7;
    private widget.ScrollPane Scroll8;
    private widget.ScrollPane Scroll9;
    private widget.TextBox Sebutkan;
    private widget.ComboBox SkalaNyeri;
    private widget.ComboBox StatusBudaya;
    private widget.ComboBox StatusKehamilan;
    private widget.TextBox StatusPernikahan;
    private widget.TextBox TCari;
    private widget.TextBox TCariMasalah;
    private widget.TextBox TCariRencana;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRM1;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.TextBox TPasien1;
    private javax.swing.JTabbedPane TabRawat;
    private javax.swing.JTabbedPane TabRencanaKeperawatan;
    private widget.TextBox TanggalRegistrasi;
    private widget.ComboBox Tekanan;
    private widget.Tanggal TglAsuhan;
    private widget.Tanggal TglHPHT;
    private widget.TextBox TglLahir;
    private widget.ComboBox TinggalDengan;
    private widget.ComboBox Turgor;
    private widget.TextBox WBAB;
    private widget.TextBox WBAK;
    private widget.TextBox WarnaPerdarahan;
    private widget.TextBox XBAB;
    private widget.TextBox XBAK;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel100;
    private widget.Label jLabel101;
    private widget.Label jLabel102;
    private widget.Label jLabel103;
    private widget.Label jLabel104;
    private widget.Label jLabel105;
    private widget.Label jLabel106;
    private widget.Label jLabel107;
    private widget.Label jLabel108;
    private widget.Label jLabel109;
    private widget.Label jLabel11;
    private widget.Label jLabel110;
    private widget.Label jLabel111;
    private widget.Label jLabel112;
    private widget.Label jLabel113;
    private widget.Label jLabel114;
    private widget.Label jLabel115;
    private widget.Label jLabel116;
    private widget.Label jLabel117;
    private widget.Label jLabel118;
    private widget.Label jLabel119;
    private widget.Label jLabel120;
    private widget.Label jLabel121;
    private widget.Label jLabel122;
    private widget.Label jLabel123;
    private widget.Label jLabel124;
    private widget.Label jLabel125;
    private widget.Label jLabel126;
    private widget.Label jLabel127;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel29;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel35;
    private widget.Label jLabel36;
    private widget.Label jLabel37;
    private widget.Label jLabel38;
    private widget.Label jLabel39;
    private widget.Label jLabel50;
    private widget.Label jLabel51;
    private widget.Label jLabel52;
    private widget.Label jLabel55;
    private widget.Label jLabel56;
    private widget.Label jLabel57;
    private widget.Label jLabel58;
    private widget.Label jLabel6;
    private widget.Label jLabel60;
    private widget.Label jLabel63;
    private widget.Label jLabel64;
    private widget.Label jLabel65;
    private widget.Label jLabel66;
    private widget.Label jLabel67;
    private widget.Label jLabel68;
    private widget.Label jLabel7;
    private widget.Label jLabel70;
    private widget.Label jLabel71;
    private widget.Label jLabel72;
    private widget.Label jLabel76;
    private widget.Label jLabel77;
    private widget.Label jLabel78;
    private widget.Label jLabel8;
    private widget.Label jLabel80;
    private widget.Label jLabel81;
    private widget.Label jLabel82;
    private widget.Label jLabel83;
    private widget.Label jLabel84;
    private widget.Label jLabel85;
    private widget.Label jLabel86;
    private widget.Label jLabel87;
    private widget.Label jLabel88;
    private widget.Label jLabel89;
    private widget.Label jLabel9;
    private widget.Label jLabel90;
    private widget.Label jLabel91;
    private widget.Label jLabel94;
    private widget.Label jLabel95;
    private widget.Label jLabel97;
    private widget.Label jLabel98;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private widget.Label label11;
    private widget.Label label12;
    private widget.Label label13;
    private widget.Label label14;
    private widget.PanelBiasa panelBiasa1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane1;
    private widget.ScrollPane scrollPane2;
    private widget.ScrollPane scrollPane4;
    private widget.ScrollPane scrollPane5;
    private widget.ScrollPane scrollPane6;
    private widget.Table tbMasalahDetailMasalah;
    private widget.Table tbMasalahKeperawatan;
    private widget.Table tbObat;
    private widget.Table tbRencanaDetail;
    private widget.Table tbRencanaKeperawatan;
    // End of variables declaration//GEN-END:variables

    /**
     * [RM 03-4/05/06] SELECT diringkas - cuma kolom pencarian (tabMode
     * sekarang lean, pola sama RM 03-2). Field lengkap dimuat belakangan
     * oleh getData() (query-ulang-DB) saat baris diklik.
     */
    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            if (TCari.getText().equals("")) {
                ps = koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir," +
                                "penilaian_awal_keperawatan_igd.tanggal,penilaian_awal_keperawatan_igd.nip,petugas.nama "
                                +
                                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                                "inner join penilaian_awal_keperawatan_igd on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat "
                                +
                                "inner join petugas on penilaian_awal_keperawatan_igd.nip=petugas.nip where " +
                                "penilaian_awal_keperawatan_igd.tanggal between ? and ? order by penilaian_awal_keperawatan_igd.tanggal");
            } else {
                ps = koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir," +
                                "penilaian_awal_keperawatan_igd.tanggal,penilaian_awal_keperawatan_igd.nip,petugas.nama "
                                +
                                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                                "inner join penilaian_awal_keperawatan_igd on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat "
                                +
                                "inner join petugas on penilaian_awal_keperawatan_igd.nip=petugas.nip where " +
                                "penilaian_awal_keperawatan_igd.tanggal between ? and ? and " +
                                "(reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "
                                +
                                "penilaian_awal_keperawatan_igd.nip like ? or petugas.nama like ?) " +
                                "order by penilaian_awal_keperawatan_igd.tanggal");
            }

            try {
                if (TCari.getText().equals("")) {
                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                } else {
                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                    ps.setString(3, "%" + TCari.getText() + "%");
                    ps.setString(4, "%" + TCari.getText() + "%");
                    ps.setString(5, "%" + TCari.getText() + "%");
                    ps.setString(6, "%" + TCari.getText() + "%");
                    ps.setString(7, "%" + TCari.getText() + "%");
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[] {
                            rs.getString("no_rawat"), rs.getString("no_rkm_medis"), rs.getString("nm_pasien"),
                            rs.getString("tgl_lahir"),
                            rs.getString("tanggal"), rs.getString("nip"), rs.getString("nama")
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

    public void emptTeks() {
        // -- Riwayat Kesehatan (lama, aktif) --
        TglAsuhan.setDate(new Date());
        if (ChkJln != null) {
            ChkJln.setSelected(true);
        }
        Informasi.setSelectedIndex(0);
        KeluhanUtama.setText("");
        RPD.setText("");
        RPO.setText("");
        StatusKehamilan.setSelectedIndex(0);
        Gravida.setText("");
        Para.setText("");
        Abortus.setText("");
        HPHT.setText("");
        if (TglHPHT != null) {
            TglHPHT.setDate(new Date());
        }

        // -- II. Pemeriksaan Fisik (lama, aktif - tidak ada foto formulir
        // fisik utk section ini di sesi ini, dibiarkan) --
        Tekanan.setSelectedIndex(0);
        Pupil.setSelectedIndex(0);
        Neurosensorik.setSelectedIndex(0);
        Integumen.setSelectedIndex(0);
        Turgor.setSelectedIndex(0);
        Edema.setSelectedIndex(0);
        Mukosa.setSelectedIndex(0);
        Perdarahan.setSelectedIndex(0);
        JumlahPerdarahan.setText("");
        WarnaPerdarahan.setText("");
        Intoksikasi.setSelectedIndex(0);
        BAB.setText("");
        XBAB.setText("");
        KBAB.setText("");
        WBAB.setText("");
        BAK.setText("");
        XBAK.setText("");
        WBAK.setText("");
        LBAK.setText("");

        // (Psikologis/Jiwa/Perilaku/Dilaporkan/Sebutkan, TinggalDengan/
        // KetTinggal, Nyeri, Provokes/KetProvokes/Quality/KetQuality/
        // Menyebar/Durasi, ATS/BJM/MSA/Hasil lama TIDAK direset - dipensiunkan
        // dari UI aktif, diganti field baru di bawah)
        Hubungan.setSelectedIndex(0);
        StatusBudaya.setSelectedIndex(0);
        KetBudaya.setText("");
        PendidikanPJ.setSelectedIndex(0);
        KetPendidikanPJ.setText("");
        Edukasi.setSelectedIndex(0);
        KetEdukasi.setText("");
        ADL.setSelectedIndex(0);
        Aktifitas.setSelectedIndex(0);
        AlatBantu.setSelectedIndex(0);
        KetAlatBantu.setText("");
        Lokasi.setText("");
        SkalaNyeri.setSelectedIndex(0);
        NyeriHilang.setSelectedIndex(0);
        KetNyeri.setText("");
        PadaDokter.setSelectedIndex(0);
        KetDokter.setText("");
        Lapor.setSelectedIndex(0);
        KetLapor.setText("");
        Rencana.setText("");

        // -- Riwayat Alergi --
        if (bgAlergi != null) {
            bgAlergi.clearSelection();
        }
        if (TAlergiSebutkan != null) {
            TAlergiSebutkan.setText("");
            TAlergiSebutkan.setVisible(false);
        }

        // -- Keadaan Umum (Tanda Vital) --
        TdKep.setText("");
        NadiKep.setText("");
        SuhuKep.setText("");
        SpoKep.setText("");
        RrKep.setText("");
        BbKep.setText("");
        TbKep.setText("");

        // -- Asesmen Psikologis --
        CekPsiTenang.setSelected(false);
        CekPsiCemas.setSelected(false);
        CekPsiTakut.setSelected(false);
        CekPsiSedih.setSelected(false);
        CekPsiMarah.setSelected(false);
        CekPsiBunuhDiri.setSelected(false);
        if (CekPsiMasalahPerilaku != null) {
            CekPsiMasalahPerilaku.setSelected(false);
        }
        if (barisMasalahPrilaku != null) {
            barisMasalahPrilaku.setVisible(false);
        }
        bgMasalahPrilaku.clearSelection();
        TMasalahPrilakuSebutkan.setText("");
        TMasalahPrilakuSebutkan.setVisible(false);

        // -- Status Sosial dan Ekonomis --
        bgStatusPernikahan.clearSelection();
        TKetStatusPernikahan.setText("");
        TKetStatusPernikahan.setVisible(false);
        bgTempatTinggal.clearSelection();
        TKetTempatTinggal.setText("");
        TKetTempatTinggal.setVisible(false);
        TPekerjaanKep.setText("");
        TPendidikanPasienKep.setText("");

        // -- Skrining Geriatri --
        bgGeri1.clearSelection();
        bgGeri2.clearSelection();
        bgGeri3.clearSelection();

        // -- Skrining Resiko Jatuh --
        bgJatuhCaraBerjalan.clearSelection();
        bgJatuhMenopang.clearSelection();
        hitungHasilJatuh();

        // -- Pengkajian Spiritual --
        if (CmbAgama != null) {
            CmbAgama.setSelectedIndex(0);
        }
        if (barisWajibIbadah != null) barisWajibIbadah.setVisible(false);
        if (barisThaharoh != null) barisThaharoh.setVisible(false);
        if (barisSholat != null) barisSholat.setVisible(false);
        bgWajibIbadah.clearSelection();
        TKetWajibIbadah.setText("");
        TKetWajibIbadah.setVisible(false);
        bgThaharoh.clearSelection();
        bgSholat.clearSelection();
        bgNilaiKepercayaan.clearSelection();
        TKetNilaiKepercayaan.setText("");
        TKetNilaiKepercayaan.setVisible(false);
        if (bgBudaya != null) {
            bgBudaya.clearSelection();
        }
        KetBudaya.setText("");
        KetBudaya.setVisible(false);
        TAgama.setText("");
        CekCacatTidak.setSelected(false);
        CekCacatTunaNetra.setSelected(false);
        CekCacatTunaDaksa.setSelected(false);
        CekCacatTunaRungu.setSelected(false);
        CekCacatTunaWicara.setSelected(false);

        // -- Pilihan Skrining Gizi (Dewasa vs Anak) --
        if (bgPilihGizi != null) {
            bgPilihGizi.clearSelection();
        }
        if (panelSeksiGiziDewasa != null) {
            panelSeksiGiziDewasa.setVisible(false);
        }
        if (panelSeksiGiziAnak != null) {
            panelSeksiGiziAnak.setVisible(false);
        }

        // -- Skrining Gizi Dewasa (MST) --
        bgMstPenurunanBb.clearSelection();
        MstPenurunanBbKg.setSelectedIndex(0);
        bgMstAsupan.clearSelection();
        TMstTotalSkor.setText("");
        if (bgMstDiagnosis != null) {
            bgMstDiagnosis.clearSelection();
        }
        if (TMstDiagnosisSebutkan != null) {
            TMstDiagnosisSebutkan.setText("");
            TMstDiagnosisSebutkan.setVisible(false);
        }
        bgMstKesimpulan.clearSelection();

        // -- Skrining Gizi Anak --
        bgGiziAnakKurus.clearSelection();
        bgGiziAnakTurunBb.clearSelection();
        bgGiziAnakKondisi.clearSelection();
        bgGiziAnakRisiko.clearSelection();
        TGiziAnakTotalSkor.setText("");
        bgGiziAnakKesimpulan.clearSelection();

        // -- Asesmen Nyeri (toggle baru) --
        bgNyeriAda.clearSelection();
        if (CmbKategoriUsiaNyeri != null) {
            CmbKategoriUsiaNyeri.setSelectedIndex(0);
            CmbKategoriUsiaNyeri.setEnabled(false);
        }
        if (btnGroupSkalaNyeri != null) {
            btnGroupSkalaNyeri.clearSelection();
        }
        if (lblInterpretasiNyeri != null) {
            lblInterpretasiNyeri.setText("");
        }
        if (panelSeksiNyeriSkalaCommon != null) panelSeksiNyeriSkalaCommon.setVisible(false);
        if (panelSeksiNyeriNumeric != null) panelSeksiNyeriNumeric.setVisible(false);
        if (panelSeksiNyeriWongBaker != null) panelSeksiNyeriWongBaker.setVisible(false);
        if (panelSeksiNyeriFlacc != null) panelSeksiNyeriFlacc.setVisible(false);
        if (panelSeksiNyeriPqrst != null) panelSeksiNyeriPqrst.setVisible(false);

        // -- FLACC --
        FlaccWajah.setSelectedIndex(0);
        FlaccKaki.setSelectedIndex(0);
        FlaccAktivitas.setSelectedIndex(0);
        FlaccMenangis.setSelectedIndex(0);
        FlaccBersuara.setSelectedIndex(0);
        TFlaccTotal.setText("");

        // -- PQRST --
        CekProvCahaya.setSelected(false);
        CekProvGelap.setSelected(false);
        CekProvGerakan.setSelected(false);
        CekProvBerbaring.setSelected(false);
        CekQualTumpul.setSelected(false);
        CekQualDitusuk.setSelected(false);
        CekQualBerdenyut.setSelected(false);
        CekQualTajam.setSelected(false);
        CekQualPanas.setSelected(false);
        bgRadiation.clearSelection();
        TRadiationKe.setText("");
        TRadiationKe.setVisible(false);
        bgSeverity.clearSelection();
        bgTime.clearSelection();

        // -- II. Intervensi Keperawatan --
        CekIntervManajemenJalanNafas.setSelected(false);
        CekIntervSuctioningJalanNafas.setSelected(false);
        CekIntervManajemenAsamBasa.setSelected(false);
        CekIntervManajemenSyok.setSelected(false);
        CekIntervManajemenNyeri.setSelected(false);
        CekIntervManajemenCairan.setSelected(false);
        CekIntervMonitoringCairan.setSelected(false);
        CekIntervManajemenLingkungan.setSelected(false);
        CekIntervPendidikanKesehatan.setSelected(false);
        CekIntervFeverTreatment.setSelected(false);
        CekIntervManajemenSensasiPerifer.setSelected(false);
        CekIntervMonitoringKardiovaskuler.setSelected(false);
        CekIntervPencegahanPenekananLuka.setSelected(false);
        if (CekIntervLainnya != null) {
            CekIntervLainnya.setSelected(false);
        }
        TIntervLainnya.setText("");
        TIntervLainnya.setVisible(false);

        // -- IV. Kardiopulmonal --
        CekKardioPulseOximetri.setSelected(false);
        CekKardioIntubasi.setSelected(false);
        CekKardioRjp.setSelected(false);
        TKardioRjpKet.setText("");
        CekKardioKardioversiDcShock.setSelected(false);

        // -- V. Tindakan Penunjang --
        CekPenunjangCekLab.setSelected(false);
        TPenunjangCekLabKet.setText("");
        CekPenunjangRontgen.setSelected(false);
        TPenunjangRontgenKet.setText("");
        CekPenunjangCtScan.setSelected(false);
        TPenunjangCtScanKet.setText("");
        if (txtPenunjangLab != null) txtPenunjangLab.setText("");
        if (txtPenunjangRad != null) txtPenunjangRad.setText("");
        if (panelPenunjangMedis != null) panelPenunjangMedis.perbaruiSemuaBadge();

        // -- VI. Tindakan Kolaborasi --
        CekKolabO2.setSelected(false);
        if (lblKolabO2 != null) lblKolabO2.setVisible(false);
        TKolabO2Ket.setText("");
        CekKolabEkgMonitor.setSelected(false);
        CekKolabNebulizer.setSelected(false);
        CekKolabPasangInfus.setSelected(false);
        TKolabPasangInfusKet.setText("");
        CekKolabInjeksiSyringePump.setSelected(false);
        CekKolabTransfusiDarah.setSelected(false);
        TKolabTransfusiDarahKet.setText("");
        CekKolabNgt.setSelected(false);
        TKolabNgtUkuran.setText("");
        CekKolabBilasLambung.setSelected(false);
        TKolabBilasLambungKet.setText("");
        CekKolabDowerCatheter.setSelected(false);
        TKolabDowerCatheterUkuran.setText("");
        CekKolabRestrainPengaman.setSelected(false);

        // -- VII. Trauma / Bedah --
        CekTraumaCervicalCollar.setSelected(false);
        CekTraumaImmobilisasiTulangBelakang.setSelected(false);
        CekTraumaGips.setSelected(false);
        CekTraumaPerawatanLuka.setSelected(false);
        CekTraumaJahitLuka.setSelected(false);
        CekTraumaBalutanSpalk.setSelected(false);
        CekTraumaSelangDadaWsd.setSelected(false);
        TTraumaLainnya.setText("");

        // -- VIII. THT / Mata --
        CekThtIrigasiMataTelinga.setSelected(false);
        CekThtAmbilKorpusAlienum.setSelected(false);

        // -- IX. Tindakan Obgyn --
        CekObgynMembantuPartus.setSelected(false);
        CekObgynPeriksaGinekologi.setSelected(false);
        CekObgynDopplerCtgUsg.setSelected(false);
        CekObgynPasangTampon.setSelected(false);

        // -- Kondisi Pasien Saat Pindah/Pulang --
        if (DTPKpPukulSaja != null) DTPKpPukulSaja.setSekarang();
        DTPKpPukul.setJam(new Date());
        TKpGcsE.setText("");
        TKpGcsV.setText("");
        TKpGcsM.setText("");
        TKpTd.setText("");
        TKpNadi.setText("");
        bgKpNadiReguler.clearSelection();
        TKpSuhu.setText("");
        TKpPernafasan.setText("");
        TKpSpo.setText("");
        CekKpMasukRs.setSelected(false);
        TKpRuang.setText("");
        TKpRuang.setVisible(false);
        BtnKpRuang.setVisible(false);
        if (selPetugasPenerima != null) selPetugasPenerima.setVisible(false);
        TKpPetugasPenerima.setText("");
        NmKpPetugasPenerima.setText("");
        bgKpPendamping.clearSelection();
        bgKpTindakanLanjutan.clearSelection();
        TKpTindakanLanjutanKet.setText("");
        TKpTindakanLanjutanKet.setVisible(false);
        CekKpDirujuk.setSelected(false);
        TKpDirujukKet.setText("");
        TKpDirujukKet.setVisible(false);
        TKpDirujukAlasan.setText("");
        if (selDirujukAlasan != null) selDirujukAlasan.setVisible(false);
        CekKpDipulangkan.setSelected(false);
        DTPKpDipulangkanPukul.setJam(new Date());
        DTPKpDipulangkanPukul.setVisible(false);
        CekKpMeninggal.setSelected(false);
        DTPKpMeninggalPukul.setJam(new Date());
        DTPKpMeninggalPukul.setVisible(false);
        CekKpDoa.setSelected(false);
        CekKpEdukasiMakanObat.setSelected(false);
        CekKpEdukasiLuka.setSelected(false);
        CekKpEdukasiDiet.setSelected(false);
        TKpEdukasiDietKet.setText("");
        TKpEdukasiDietKet.setVisible(false);
        CekKpEdukasiLain.setSelected(false);
        TKpEdukasiLainKet.setText("");
        TKpEdukasiLainKet.setVisible(false);
        KdDokterKp.setText("");
        NmDokterKp.setText("");

        // -- Pemberian Obat/Infus (tabel terpisah, direset via tampilObatInfus) --
        Valid.tabelKosong(tabModeObatInfus);
        emptTeksObatInfus();

        for (i = 0; i < tabModeMasalah.getRowCount(); i++) {
            tabModeMasalah.setValueAt(false, i, 0);
        }
        for (i = 0; i < tabModeRencana.getRowCount(); i++) {
            tabModeRencana.setValueAt(false, i, 0);
        }
        TNoRM1.setText("");
        TPasien1.setText("");
        TabRawat.setSelectedIndex(0);
        Informasi.requestFocus();
        updateGayaSkalaNyeri();
        hitungProgresKep();
    }

    /**
     * [RM 03-4/05/06] Helper konversi checkbox ke "Ya"/"Tidak", pola sama
     * seperti RM 03-2 - dipakai bareng oleh nilaiKep().
     */
    private String ya(javax.swing.AbstractButton c) {
        return c.isSelected() ? "Ya" : "Tidak";
    }

    /**
     * [RM 03-4/05/06] ComboBox dgn opsi kosong sbg placeholder (mis.
     * FLACC/MstPenurunanBbKg) - kalau belum dipilih apa-apa, kembalikan
     * null (bukan "") supaya tidak gagal insert ke kolom ENUM nullable
     * yang tidak punya opsi "".
     */
    private String kosongKeNull(String s) {
        return (s == null || s.trim().equals("")) ? null : s;
    }

    private String potong(String s, int max) {
        if (s == null) {
            return "";
        }
        s = s.trim();
        return (s.length() > max) ? s.substring(0, max) : s;
    }

    /**
     * [RM 03-4/05/06] Pola query-ulang-DB (sama seperti RM 03-2) - saat
     * baris tabel diklik, SELECT * langsung ke penilaian_awal_keperawatan_igd
     * by no_rawat, isi SEMUA field (lama yang masih aktif + baru) dari
     * ResultSet by nama kolom. tbObat (daftar) cuma perlu no_rawat di
     * kolom 0 - lihat tampil(). Checklist "I. Diagnosis Keperawatan"
     * (tabModeMasalah) di-refresh dgn menandai baris yg cocok dgn
     * penilaian_awal_keperawatan_igd_masalah utk no_rawat ini. Pemberian
     * Obat/Infus (tabModeObatInfus) ikut dimuat ulang via tampilObatInfus().
     */
    private void getData() {
        if (tbObat.getSelectedRow() == -1) {
            return;
        }
        String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        try {
            PreparedStatement psData = koneksi.prepareStatement(
                    "select penilaian_awal_keperawatan_igd.*, " +
                            "reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, if(pasien.jk='L','Laki-Laki','Perempuan') as jk, "
                            +
                            "pasien.stts_nikah, pasien.pekerjaan as pekerjaan_pasien, pasien.pnd, penjab.png_jawab, " +
                            "petugas.nama, dokter.nm_dokter as kp_nm_dokter " +
                            "from penilaian_awal_keperawatan_igd " +
                            "inner join reg_periksa on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat " +
                            "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis " +
                            "inner join petugas on petugas.nip=penilaian_awal_keperawatan_igd.nip " +
                            "left join penjab on penjab.kd_pj=reg_periksa.kd_pj " +
                            "left join dokter on dokter.kd_dokter=penilaian_awal_keperawatan_igd.kp_kd_dokter " +
                            "where penilaian_awal_keperawatan_igd.no_rawat=?");
            try {
                psData.setString(1, noRawat);
                ResultSet rsData = psData.executeQuery();
                try {
                    if (rsData.next()) {
                        TNoRw.setText(noRawat);
                        TNoRM.setText(rsData.getString("no_rkm_medis"));
                        TPasien.setText(rsData.getString("nm_pasien"));
                        TNoRM1.setText(rsData.getString("no_rkm_medis"));
                        TPasien1.setText(rsData.getString("nm_pasien"));
                        Jk.setText(rsData.getString("jk"));
                        TglLahir.setText(rsData.getString("tgl_lahir"));
                        StatusPernikahan.setText(rsData.getString("stts_nikah"));
                        Pekerjaan.setText(rsData.getString("pekerjaan_pasien"));
                        PendidikanPasien.setText(rsData.getString("pnd"));
                        Pembayaran.setText(rsData.getString("png_jawab"));
                        String tglAsuhanStr = rsData.getString("tanggal");
                        Valid.SetTgl2(TglAsuhan, tglAsuhanStr);
                        if (tglAsuhanStr != null && tglAsuhanStr.length() >= 19 && CmbJam != null && CmbMenit != null
                                && CmbDetik != null && ChkJln != null) {
                            CmbJam.setSelectedItem(tglAsuhanStr.substring(11, 13));
                            CmbMenit.setSelectedItem(tglAsuhanStr.substring(14, 16));
                            CmbDetik.setSelectedItem(tglAsuhanStr.substring(17, 19));
                            ChkJln.setSelected(false);
                        }

                        // -- Riwayat Kesehatan (lama, aktif) --
                        Informasi.setSelectedItem(rsData.getString("informasi"));
                        KeluhanUtama.setText(rsData.getString("keluhan_utama"));
                        RPD.setText(rsData.getString("rpd"));
                        RPO.setText(rsData.getString("rpo"));
                        StatusKehamilan.setSelectedItem(rsData.getString("status_kehamilan"));
                        Gravida.setText(rsData.getString("gravida"));
                        Para.setText(rsData.getString("para"));
                        Abortus.setText(rsData.getString("abortus"));
                        String hphtVal = rsData.getString("hpht");
                        HPHT.setText(hphtVal);
                        setTanggalHpht(hphtVal);

                        // -- II. Pemeriksaan Fisik (lama, aktif) --
                        Tekanan.setSelectedItem(rsData.getString("tekanan"));
                        Pupil.setSelectedItem(rsData.getString("pupil"));
                        Neurosensorik.setSelectedItem(rsData.getString("neurosensorik"));
                        Integumen.setSelectedItem(rsData.getString("integumen"));
                        Turgor.setSelectedItem(rsData.getString("turgor"));
                        Edema.setSelectedItem(rsData.getString("edema"));
                        Mukosa.setSelectedItem(rsData.getString("mukosa"));
                        Perdarahan.setSelectedItem(rsData.getString("perdarahan"));
                        JumlahPerdarahan.setText(rsData.getString("jumlah_perdarahan"));
                        WarnaPerdarahan.setText(rsData.getString("warna_perdarahan"));
                        Intoksikasi.setSelectedItem(rsData.getString("intoksikasi"));
                        BAB.setText(rsData.getString("bab"));
                        XBAB.setText(rsData.getString("xbab"));
                        KBAB.setText(rsData.getString("kbab"));
                        WBAB.setText(rsData.getString("wbab"));
                        BAK.setText(rsData.getString("bak"));
                        XBAK.setText(rsData.getString("xbak"));
                        WBAK.setText(rsData.getString("wbak"));
                        LBAK.setText(rsData.getString("lbak"));

                        Hubungan.setSelectedItem(rsData.getString("hubungan"));
                        String bud = rsData.getString("budaya");
                        StatusBudaya.setSelectedItem(bud);
                        boolean adaBud = "Ada".equalsIgnoreCase(bud) || "Ya".equalsIgnoreCase(bud);
                        boolean tidakBud = "Tidak Ada".equalsIgnoreCase(bud) || "Tidak".equalsIgnoreCase(bud);
                        if (adaBud) {
                            if (RbBudayaAda != null) RbBudayaAda.setSelected(true);
                        } else if (tidakBud) {
                            if (RbBudayaTidak != null) RbBudayaTidak.setSelected(true);
                        } else {
                            if (bgBudaya != null) bgBudaya.clearSelection();
                        }
                        KetBudaya.setText(rsData.getString("ket_budaya"));
                        KetBudaya.setVisible(adaBud);
                        PendidikanPJ.setSelectedItem(rsData.getString("pendidikan_pj"));
                        KetPendidikanPJ.setText(rsData.getString("ket_pendidikan_pj"));
                        Edukasi.setSelectedItem(rsData.getString("edukasi"));
                        KetEdukasi.setText(rsData.getString("ket_edukasi"));
                        ADL.setSelectedItem(rsData.getString("kemampuan"));
                        Aktifitas.setSelectedItem(rsData.getString("aktifitas"));
                        AlatBantu.setSelectedItem(rsData.getString("alat_bantu"));
                        KetAlatBantu.setText(rsData.getString("ket_bantu"));
                        Lokasi.setText(rsData.getString("lokasi"));
                        SkalaNyeri.setSelectedItem(rsData.getString("skala_nyeri"));
                        NyeriHilang.setSelectedItem(rsData.getString("nyeri_hilang"));
                        KetNyeri.setText(rsData.getString("ket_nyeri"));
                        PadaDokter.setSelectedItem(rsData.getString("pada_dokter"));
                        KetDokter.setText(rsData.getString("ket_dokter"));
                        Lapor.setSelectedItem(rsData.getString("lapor"));
                        KetLapor.setText(rsData.getString("ket_lapor"));
                        Rencana.setText(rsData.getString("rencana"));
                        KdPetugas.setText(rsData.getString("nip"));
                        NmPetugas.setText(rsData.getString("nama"));

                        // -- Riwayat Alergi --
                        String alergiAda = rsData.getString("alergi_ada");
                        if ("Ya".equals(alergiAda)) {
                            RbAlergiYa.setSelected(true);
                            TAlergiSebutkan.setText(rsData.getString("alergi_sebutkan"));
                            TAlergiSebutkan.setVisible(true);
                        } else if ("Tidak".equals(alergiAda)) {
                            RbAlergiTidak.setSelected(true);
                            TAlergiSebutkan.setText("");
                            TAlergiSebutkan.setVisible(false);
                        } else {
                            if (bgAlergi != null) {
                                bgAlergi.clearSelection();
                            }
                            TAlergiSebutkan.setText("");
                            TAlergiSebutkan.setVisible(false);
                        }

                        // -- Keadaan Umum --
                        TdKep.setText(rsData.getString("td"));
                        NadiKep.setText(rsData.getString("nadi"));
                        SuhuKep.setText(rsData.getString("suhu"));
                        SpoKep.setText(rsData.getString("spo"));
                        RrKep.setText(rsData.getString("rr"));
                        BbKep.setText(rsData.getString("bb"));
                        TbKep.setText(rsData.getString("tb"));

                        // -- Asesmen Psikologis --
                        CekPsiTenang.setSelected("Ya".equals(rsData.getString("psi_tenang")));
                        CekPsiCemas.setSelected("Ya".equals(rsData.getString("psi_cemas")));
                        CekPsiTakut.setSelected("Ya".equals(rsData.getString("psi_takut")));
                        CekPsiSedih.setSelected("Ya".equals(rsData.getString("psi_sedih")));
                        CekPsiMarah.setSelected("Ya".equals(rsData.getString("psi_marah")));
                        CekPsiBunuhDiri.setSelected("Ya".equals(rsData.getString("psi_bunuh_diri")));
                        String masalahPrilaku = rsData.getString("psi_masalah_prilaku");
                        boolean adaPrilaku = "Ya".equals(masalahPrilaku);
                        boolean tidakPrilaku = "Tidak".equals(masalahPrilaku);
                        if (adaPrilaku || tidakPrilaku) {
                            if (CekPsiMasalahPerilaku != null) CekPsiMasalahPerilaku.setSelected(true);
                            if (barisMasalahPrilaku != null) barisMasalahPrilaku.setVisible(true);
                            if (adaPrilaku) {
                                RbMasalahPrilakuYa.setSelected(true);
                                TMasalahPrilakuSebutkan.setVisible(true);
                                TMasalahPrilakuSebutkan.setText(rsData.getString("psi_masalah_prilaku_sebutkan"));
                            } else {
                                RbMasalahPrilakuTidak.setSelected(true);
                                TMasalahPrilakuSebutkan.setVisible(false);
                                TMasalahPrilakuSebutkan.setText("");
                            }
                        } else {
                            if (CekPsiMasalahPerilaku != null) CekPsiMasalahPerilaku.setSelected(false);
                            if (barisMasalahPrilaku != null) barisMasalahPrilaku.setVisible(false);
                            bgMasalahPrilaku.clearSelection();
                            TMasalahPrilakuSebutkan.setVisible(false);
                            TMasalahPrilakuSebutkan.setText("");
                        }

                        // -- Status Sosial dan Ekonomis --
                        String statusNikah = rsData.getString("status_pernikahan");
                        if ("Belum Menikah".equals(statusNikah))
                            RbNikahBelumMenikah.setSelected(true);
                        else if ("Menikah".equals(statusNikah))
                            RbNikahMenikah.setSelected(true);
                        else if ("Cerai".equals(statusNikah))
                            RbNikahCerai.setSelected(true);
                        else if ("Lainnya".equals(statusNikah))
                            RbNikahLainnya.setSelected(true);
                        else
                            bgStatusPernikahan.clearSelection();
                        boolean nikahLainnya = RbNikahLainnya.isSelected();
                        TKetStatusPernikahan.setVisible(nikahLainnya);
                        TKetStatusPernikahan.setText(nikahLainnya ? rsData.getString("ket_status_pernikahan") : "");

                        String tempatTinggal = rsData.getString("tempat_tinggal");
                        if ("Rumah Sendiri".equals(tempatTinggal))
                            RbTinggalSendiri.setSelected(true);
                        else if ("Rumah Keluarganya".equals(tempatTinggal))
                            RbTinggalKeluarga.setSelected(true);
                        else if ("Lainnya".equals(tempatTinggal))
                            RbTinggalLainnya.setSelected(true);
                        else
                            bgTempatTinggal.clearSelection();
                        boolean tinggalLainnya = RbTinggalLainnya.isSelected();
                        TKetTempatTinggal.setVisible(tinggalLainnya);
                        TKetTempatTinggal.setText(tinggalLainnya ? rsData.getString("ket_tempat_tinggal") : "");
                        TPekerjaanKep.setText(rsData.getString("pekerjaan_kep"));
                        TPendidikanPasienKep.setText(rsData.getString("pendidikan_pasien_kep"));

                        // -- Skrining Geriatri --
                        String geri1 = rsData.getString("geri_60_multi_penyakit");
                        if ("Ya".equals(geri1))
                            RbGeri1Ya.setSelected(true);
                        else if ("Tidak".equals(geri1))
                            RbGeri1Tidak.setSelected(true);
                        else
                            bgGeri1.clearSelection();
                        String geri2 = rsData.getString("geri_60_penurunan_fungsi");
                        if ("Ya".equals(geri2))
                            RbGeri2Ya.setSelected(true);
                        else if ("Tidak".equals(geri2))
                            RbGeri2Tidak.setSelected(true);
                        else
                            bgGeri2.clearSelection();
                        String geri3 = rsData.getString("geri_70_1_penyakit");
                        if ("Ya".equals(geri3))
                            RbGeri3Ya.setSelected(true);
                        else if ("Tidak".equals(geri3))
                            RbGeri3Tidak.setSelected(true);
                        else
                            bgGeri3.clearSelection();

                        // -- Skrining Resiko Jatuh --
                        String jatuhA = rsData.getString("jatuh_cara_berjalan");
                        if ("Ya".equals(jatuhA))
                            RbJatuhCaraBerjalanYa.setSelected(true);
                        else if ("Tidak".equals(jatuhA))
                            RbJatuhCaraBerjalanTidak.setSelected(true);
                        else
                            bgJatuhCaraBerjalan.clearSelection();
                        String jatuhB = rsData.getString("jatuh_menopang_duduk");
                        if ("Ya".equals(jatuhB))
                            RbJatuhMenopangYa.setSelected(true);
                        else if ("Tidak".equals(jatuhB))
                            RbJatuhMenopangTidak.setSelected(true);
                        else
                            bgJatuhMenopang.clearSelection();
                        hitungHasilJatuh();

                        // -- Pengkajian Spiritual --
                        String wajibIbadah = rsData.getString("wajib_ibadah");
                        if ("Baligh".equals(wajibIbadah))
                            RbWajibIbadahBaligh.setSelected(true);
                        else if ("Belum Baligh".equals(wajibIbadah))
                            RbWajibIbadahBelumBaligh.setSelected(true);
                        else if ("Halangan Lain".equals(wajibIbadah))
                            RbWajibIbadahHalanganLain.setSelected(true);
                        else
                            bgWajibIbadah.clearSelection();
                        TKetWajibIbadah.setText(rsData.getString("ket_wajib_ibadah"));
                        String thaharoh = rsData.getString("thaharoh");
                        if ("Berwudhu".equals(thaharoh))
                            RbThaharohBerwudhu.setSelected(true);
                        else if ("Tayamum".equals(thaharoh))
                            RbThaharohTayamum.setSelected(true);
                        else
                            bgThaharoh.clearSelection();
                        String sholat = rsData.getString("sholat");
                        if ("Berdiri".equals(sholat))
                            RbSholatBerdiri.setSelected(true);
                        else if ("Duduk".equals(sholat))
                            RbSholatDuduk.setSelected(true);
                        else if ("Berbaring".equals(sholat))
                            RbSholatBerbaring.setSelected(true);
                        else
                            bgSholat.clearSelection();
                        String nilaiKepercayaan = rsData.getString("nilai_kepercayaan");
                        if ("Tidak".equals(nilaiKepercayaan))
                            RbNilaiKepercayaanTidak.setSelected(true);
                        else if ("Ya".equals(nilaiKepercayaan))
                            RbNilaiKepercayaanYa.setSelected(true);
                        else
                            bgNilaiKepercayaan.clearSelection();
                        TKetNilaiKepercayaan.setText(rsData.getString("ket_nilai_kepercayaan"));
                        TKetNilaiKepercayaan.setVisible("Ya".equals(nilaiKepercayaan));
                        setAgamaSelection(rsData.getString("agama_manual"));
                        boolean islam = "Islam".equalsIgnoreCase(CmbAgama.getSelectedItem() == null ? "" : CmbAgama.getSelectedItem().toString());
                        if (barisWajibIbadah != null) barisWajibIbadah.setVisible(islam);
                        if (barisThaharoh != null) barisThaharoh.setVisible(islam);
                        if (barisSholat != null) barisSholat.setVisible(islam);
                        TKetWajibIbadah.setVisible(islam && "Halangan Lain".equals(wajibIbadah));
                        TAgama.setText(rsData.getString("agama_manual"));
                        CekCacatTidak.setSelected("Ya".equals(rsData.getString("cacat_tidak")));
                        CekCacatTunaNetra.setSelected("Ya".equals(rsData.getString("cacat_tuna_netra")));
                        CekCacatTunaDaksa.setSelected("Ya".equals(rsData.getString("cacat_tuna_daksa")));
                        CekCacatTunaRungu.setSelected("Ya".equals(rsData.getString("cacat_tuna_rungu")));
                        CekCacatTunaWicara.setSelected("Ya".equals(rsData.getString("cacat_tuna_wicara")));

                        // -- Pilihan Skrining Gizi (Dewasa vs Anak) --
                        String mstPenurunan = rsData.getString("mst_penurunan_bb");
                        String mstAsupan = rsData.getString("mst_asupan_berkurang");
                        String mstKesimpulan = rsData.getString("mst_kesimpulan");

                        String giziKurus = rsData.getString("gizi_anak_kurus");
                        String giziKesimpulan = rsData.getString("gizi_anak_kesimpulan");
                        String giziSkor = rsData.getString("gizi_anak_total_skor");

                        boolean adaDewasa = (mstPenurunan != null && !mstPenurunan.trim().isEmpty())
                                || (mstAsupan != null && !mstAsupan.trim().isEmpty())
                                || (mstKesimpulan != null && !mstKesimpulan.trim().isEmpty());
                        boolean adaAnak = (giziKurus != null && !giziKurus.trim().isEmpty())
                                || (giziKesimpulan != null && !giziKesimpulan.trim().isEmpty())
                                || (giziSkor != null && !giziSkor.trim().isEmpty());

                        if (adaDewasa) {
                            if (RbPilihGiziDewasa != null) RbPilihGiziDewasa.setSelected(true);
                            if (panelSeksiGiziDewasa != null) panelSeksiGiziDewasa.setVisible(true);
                            if (panelSeksiGiziAnak != null) panelSeksiGiziAnak.setVisible(false);
                        } else if (adaAnak) {
                            if (RbPilihGiziAnak != null) RbPilihGiziAnak.setSelected(true);
                            if (panelSeksiGiziDewasa != null) panelSeksiGiziDewasa.setVisible(false);
                            if (panelSeksiGiziAnak != null) panelSeksiGiziAnak.setVisible(true);
                        } else {
                            if (bgPilihGizi != null) bgPilihGizi.clearSelection();
                            if (panelSeksiGiziDewasa != null) panelSeksiGiziDewasa.setVisible(false);
                            if (panelSeksiGiziAnak != null) panelSeksiGiziAnak.setVisible(false);
                        }

                        // -- Skrining Gizi Dewasa (MST) --
                        if ("Tidak Ada".equals(mstPenurunan))
                            RbMstTidakAda.setSelected(true);
                        else if ("Tidak Yakin".equals(mstPenurunan))
                            RbMstTidakYakin.setSelected(true);
                        else if ("Ya".equals(mstPenurunan))
                            RbMstYa.setSelected(true);
                        else
                            bgMstPenurunanBb.clearSelection();
                        String mstKg = rsData.getString("mst_penurunan_bb_kg");
                        MstPenurunanBbKg.setSelectedItem(mstKg == null ? "" : mstKg);
                        if ("Ya".equals(mstAsupan))
                            RbMstAsupanYa.setSelected(true);
                        else if ("Tidak".equals(mstAsupan))
                            RbMstAsupanTidak.setSelected(true);
                        else
                            bgMstAsupan.clearSelection();
                        String mstDiagnosis = rsData.getString("mst_diagnosis_khusus");
                        if ("Ya".equals(mstDiagnosis)) {
                            RbMstDiagnosisYa.setSelected(true);
                            TMstDiagnosisSebutkan.setText(rsData.getString("mst_diagnosis_khusus_sebutkan"));
                            TMstDiagnosisSebutkan.setVisible(true);
                        } else if ("Tidak".equals(mstDiagnosis)) {
                            RbMstDiagnosisTidak.setSelected(true);
                            TMstDiagnosisSebutkan.setText("");
                            TMstDiagnosisSebutkan.setVisible(false);
                        } else {
                            if (bgMstDiagnosis != null) {
                                bgMstDiagnosis.clearSelection();
                            }
                            TMstDiagnosisSebutkan.setText("");
                            TMstDiagnosisSebutkan.setVisible(false);
                        }
                        hitungTotalMst();

                        // -- Skrining Gizi Anak --
                        if ("Ya".equals(giziKurus))
                            RbGiziAnakKurusYa.setSelected(true);
                        else if ("Tidak".equals(giziKurus))
                            RbGiziAnakKurusTidak.setSelected(true);
                        else
                            bgGiziAnakKurus.clearSelection();
                        String giziTurunBb = rsData.getString("gizi_anak_turun_bb");
                        if ("Ya".equals(giziTurunBb))
                            RbGiziAnakTurunBbYa.setSelected(true);
                        else if ("Tidak".equals(giziTurunBb))
                            RbGiziAnakTurunBbTidak.setSelected(true);
                        else
                            bgGiziAnakTurunBb.clearSelection();
                        String giziKondisi = rsData.getString("gizi_anak_kondisi_khusus");
                        if ("Ya".equals(giziKondisi))
                            RbGiziAnakKondisiYa.setSelected(true);
                        else if ("Tidak".equals(giziKondisi))
                            RbGiziAnakKondisiTidak.setSelected(true);
                        else
                            bgGiziAnakKondisi.clearSelection();
                        String giziRisiko = rsData.getString("gizi_anak_risiko_malnutrisi");
                        if ("Ya".equals(giziRisiko))
                            RbGiziAnakRisikoYa.setSelected(true);
                        else if ("Tidak".equals(giziRisiko))
                            RbGiziAnakRisikoTidak.setSelected(true);
                        else
                            bgGiziAnakRisiko.clearSelection();
                        if ("Risiko Rendah".equals(giziKesimpulan))
                            RbGiziAnakRendah.setSelected(true);
                        else if ("Risiko Sedang".equals(giziKesimpulan))
                            RbGiziAnakSedang.setSelected(true);
                        else if ("Risiko Tinggi".equals(giziKesimpulan))
                            RbGiziAnakTinggi.setSelected(true);
                        else
                            bgGiziAnakKesimpulan.clearSelection();
                        hitungTotalGiziAnak();

                        // -- Asesmen Nyeri (toggle) --
                        String nyeriAda = rsData.getString("nyeri_ada");
                        if ("Ya".equals(nyeriAda))
                            RbNyeriAdaYa.setSelected(true);
                        else if ("Tidak".equals(nyeriAda))
                            RbNyeriAdaTidak.setSelected(true);
                        else
                            bgNyeriAda.clearSelection();

                        String katUsiaNyeri = rsData.getString("kategori_usia_nyeri");
                        if (CmbKategoriUsiaNyeri != null) {
                            CmbKategoriUsiaNyeri.setSelectedItem(katUsiaNyeri != null ? katUsiaNyeri : "");
                            CmbKategoriUsiaNyeri.setEnabled(!"Tidak".equals(nyeriAda));
                        }
                        if ("Tidak".equals(nyeriAda)) {
                            if (panelSeksiNyeriSkalaCommon != null) panelSeksiNyeriSkalaCommon.setVisible(false);
                            if (panelSeksiNyeriNumeric != null) panelSeksiNyeriNumeric.setVisible(false);
                            if (panelSeksiNyeriWongBaker != null) panelSeksiNyeriWongBaker.setVisible(false);
                            if (panelSeksiNyeriFlacc != null) panelSeksiNyeriFlacc.setVisible(false);
                            if (panelSeksiNyeriPqrst != null) panelSeksiNyeriPqrst.setVisible(false);
                        } else if ("Ya".equals(nyeriAda)) {
                            updateVisibilitasNyeriKategori();
                        }

                        // -- FLACC --
                        setPilihanFlaccDariDb(FlaccWajah, rsData.getString("flacc_wajah"));
                        setPilihanFlaccDariDb(FlaccKaki, rsData.getString("flacc_kaki"));
                        setPilihanFlaccDariDb(FlaccAktivitas, rsData.getString("flacc_aktivitas"));
                        setPilihanFlaccDariDb(FlaccMenangis, rsData.getString("flacc_menangis"));
                        setPilihanFlaccDariDb(FlaccBersuara, rsData.getString("flacc_bersuara"));
                        hitungTotalFlacc();

                        // -- PQRST --
                        CekProvCahaya.setSelected("Ya".equals(rsData.getString("pqrst_prov_cahaya")));
                        CekProvGelap.setSelected("Ya".equals(rsData.getString("pqrst_prov_gelap")));
                        CekProvGerakan.setSelected("Ya".equals(rsData.getString("pqrst_prov_gerakan")));
                        CekProvBerbaring.setSelected("Ya".equals(rsData.getString("pqrst_prov_berbaring")));
                        CekQualTumpul.setSelected("Ya".equals(rsData.getString("pqrst_qual_tumpul")));
                        CekQualDitusuk.setSelected("Ya".equals(rsData.getString("pqrst_qual_ditusuk")));
                        CekQualBerdenyut.setSelected("Ya".equals(rsData.getString("pqrst_qual_berdenyut")));
                        CekQualTajam.setSelected("Ya".equals(rsData.getString("pqrst_qual_tajam")));
                        CekQualPanas.setSelected("Ya".equals(rsData.getString("pqrst_qual_panas")));
                        String radiation = rsData.getString("pqrst_radiation");
                        if ("Tidak".equals(radiation)) {
                            RbRadiationTidak.setSelected(true);
                            TRadiationKe.setVisible(false);
                        } else if ("Ya".equals(radiation)) {
                            RbRadiationYa.setSelected(true);
                            TRadiationKe.setVisible(true);
                        } else {
                            bgRadiation.clearSelection();
                            TRadiationKe.setVisible(false);
                        }
                        TRadiationKe.setText(rsData.getString("pqrst_radiation_ke"));
                        String severity = rsData.getString("pqrst_severity");
                        if ("Tidak Nyeri".equals(severity))
                            RbSeverityTidakNyeri.setSelected(true);
                        else if ("Ringan".equals(severity))
                            RbSeverityRingan.setSelected(true);
                        else if ("Sedang".equals(severity))
                            RbSeveritySedang.setSelected(true);
                        else if ("Berat".equals(severity))
                            RbSeverityBerat.setSelected(true);
                        else
                            bgSeverity.clearSelection();
                        String time = rsData.getString("pqrst_time");
                        if ("Terus Menerus".equals(time))
                            RbTimeTerusMenerus.setSelected(true);
                        else if ("Hilang Timbul".equals(time))
                            RbTimeHilangTimbul.setSelected(true);
                        else if ("<30 Menit".equals(time))
                            RbTimeKurang30.setSelected(true);
                        else if (">30 Menit".equals(time))
                            RbTimeLebih30.setSelected(true);
                        else
                            bgTime.clearSelection();

                        // -- II. Intervensi Keperawatan --
                        CekIntervManajemenJalanNafas
                                .setSelected("Ya".equals(rsData.getString("interv_manajemen_jalan_nafas")));
                        CekIntervSuctioningJalanNafas
                                .setSelected("Ya".equals(rsData.getString("interv_suctioning_jalan_nafas")));
                        CekIntervManajemenAsamBasa
                                .setSelected("Ya".equals(rsData.getString("interv_manajemen_asam_basa")));
                        CekIntervManajemenSyok.setSelected("Ya".equals(rsData.getString("interv_manajemen_syok")));
                        CekIntervManajemenNyeri.setSelected("Ya".equals(rsData.getString("interv_manajemen_nyeri")));
                        CekIntervManajemenCairan.setSelected("Ya".equals(rsData.getString("interv_manajemen_cairan")));
                        CekIntervMonitoringCairan
                                .setSelected("Ya".equals(rsData.getString("interv_monitoring_cairan")));
                        CekIntervManajemenLingkungan
                                .setSelected("Ya".equals(rsData.getString("interv_manajemen_lingkungan")));
                        CekIntervPendidikanKesehatan
                                .setSelected("Ya".equals(rsData.getString("interv_pendidikan_kesehatan")));
                        CekIntervFeverTreatment.setSelected("Ya".equals(rsData.getString("interv_fever_treatment")));
                        CekIntervManajemenSensasiPerifer
                                .setSelected("Ya".equals(rsData.getString("interv_manajemen_sensasi_perifer")));
                        CekIntervMonitoringKardiovaskuler
                                .setSelected("Ya".equals(rsData.getString("interv_monitoring_kardiovaskuler")));
                        CekIntervPencegahanPenekananLuka
                                .setSelected("Ya".equals(rsData.getString("interv_pencegahan_penekanan_luka")));
                        TIntervLainnya.setText(rsData.getString("interv_lainnya"));
                        if (CekIntervLainnya != null) {
                            boolean ada = !TIntervLainnya.getText().trim().isEmpty();
                            CekIntervLainnya.setSelected(ada);
                            TIntervLainnya.setVisible(ada);
                        }

                        // -- IV. Kardiopulmonal --
                        CekKardioPulseOximetri.setSelected("Ya".equals(rsData.getString("kardio_pulse_oximetri")));
                        CekKardioIntubasi.setSelected("Ya".equals(rsData.getString("kardio_intubasi")));
                        boolean kardioRjp = "Ya".equals(rsData.getString("kardio_rjp"));
                        CekKardioRjp.setSelected(kardioRjp);
                        TKardioRjpKet.setText(rsData.getString("kardio_rjp_ket"));
                        CekKardioKardioversiDcShock
                                .setSelected("Ya".equals(rsData.getString("kardio_kardioversi_dc_shock")));

                        // -- V. Tindakan Penunjang --
                        boolean cekLab = "Ya".equals(rsData.getString("penunjang_cek_lab"));
                        CekPenunjangCekLab.setSelected(cekLab);
                        TPenunjangCekLabKet.setText(rsData.getString("penunjang_cek_lab_ket"));
                        boolean rontgen = "Ya".equals(rsData.getString("penunjang_rontgen"));
                        CekPenunjangRontgen.setSelected(rontgen);
                        TPenunjangRontgenKet.setText(rsData.getString("penunjang_rontgen_ket"));
                        boolean ctScan = "Ya".equals(rsData.getString("penunjang_ct_scan"));
                        CekPenunjangCtScan.setSelected(ctScan);
                        TPenunjangCtScanKet.setText(rsData.getString("penunjang_ct_scan_ket"));

                        if (txtPenunjangLab != null) {
                            txtPenunjangLab.setText(TPenunjangCekLabKet.getText());
                        }
                        if (txtPenunjangRad != null) {
                            String radVal = TPenunjangRontgenKet.getText().trim();
                            String ctVal = TPenunjangCtScanKet.getText().trim();
                            if (!ctVal.isEmpty()) {
                                if (radVal.isEmpty()) radVal = ctVal;
                                else if (!radVal.contains(ctVal)) radVal = radVal + ", " + ctVal;
                            }
                            txtPenunjangRad.setText(radVal);
                        }
                        if (panelPenunjangMedis != null) {
                            panelPenunjangMedis.perbaruiSemuaBadge();
                        }

                        // -- VI. Tindakan Kolaborasi --
                        boolean kolabO2 = "Ya".equals(rsData.getString("kolab_o2"));
                        CekKolabO2.setSelected(kolabO2);
                        if (lblKolabO2 != null) lblKolabO2.setVisible(false);
                        TKolabO2Ket.setText(rsData.getString("kolab_o2_ket"));
                        CekKolabEkgMonitor.setSelected("Ya".equals(rsData.getString("kolab_ekg_monitor")));
                        CekKolabNebulizer.setSelected("Ya".equals(rsData.getString("kolab_nebulizer")));
                        boolean pasangInfus = "Ya".equals(rsData.getString("kolab_pasang_infus"));
                        CekKolabPasangInfus.setSelected(pasangInfus);
                        TKolabPasangInfusKet.setText(rsData.getString("kolab_pasang_infus_ket"));
                        CekKolabInjeksiSyringePump
                                .setSelected("Ya".equals(rsData.getString("kolab_injeksi_syringe_pump")));
                        boolean transfusi = "Ya".equals(rsData.getString("kolab_transfusi_darah"));
                        CekKolabTransfusiDarah.setSelected(transfusi);
                        TKolabTransfusiDarahKet.setText(rsData.getString("kolab_transfusi_darah_ket"));
                        boolean kolabNgt = "Ya".equals(rsData.getString("kolab_ngt"));
                        CekKolabNgt.setSelected(kolabNgt);
                        TKolabNgtUkuran.setText(rsData.getString("kolab_ngt_ukuran"));
                        boolean bilas = "Ya".equals(rsData.getString("kolab_bilas_lambung"));
                        CekKolabBilasLambung.setSelected(bilas);
                        TKolabBilasLambungKet.setText(rsData.getString("kolab_bilas_lambung_ket"));
                        boolean dower = "Ya".equals(rsData.getString("kolab_dower_catheter"));
                        CekKolabDowerCatheter.setSelected(dower);
                        TKolabDowerCatheterUkuran.setText(rsData.getString("kolab_dower_catheter_ukuran"));
                        CekKolabRestrainPengaman.setSelected("Ya".equals(rsData.getString("kolab_restrain_pengaman")));

                        // -- VII. Trauma / Bedah --
                        CekTraumaCervicalCollar.setSelected("Ya".equals(rsData.getString("trauma_cervical_collar")));
                        CekTraumaImmobilisasiTulangBelakang
                                .setSelected("Ya".equals(rsData.getString("trauma_immobilisasi_tulang_belakang")));
                        CekTraumaGips.setSelected("Ya".equals(rsData.getString("trauma_gips")));
                        CekTraumaPerawatanLuka.setSelected("Ya".equals(rsData.getString("trauma_perawatan_luka")));
                        CekTraumaJahitLuka.setSelected("Ya".equals(rsData.getString("trauma_jahit_luka")));
                        CekTraumaBalutanSpalk.setSelected("Ya".equals(rsData.getString("trauma_balutan_spalk")));
                        CekTraumaSelangDadaWsd.setSelected("Ya".equals(rsData.getString("trauma_selang_dada_wsd")));
                        TTraumaLainnya.setText(rsData.getString("trauma_lainnya"));

                        // -- VIII. THT / Mata --
                        CekThtIrigasiMataTelinga.setSelected("Ya".equals(rsData.getString("tht_irigasi_mata_telinga")));
                        CekThtAmbilKorpusAlienum.setSelected("Ya".equals(rsData.getString("tht_ambil_korpus_alienum")));

                        // -- IX. Tindakan Obgyn --
                        CekObgynMembantuPartus.setSelected("Ya".equals(rsData.getString("obgyn_membantu_partus")));
                        CekObgynPeriksaGinekologi
                                .setSelected("Ya".equals(rsData.getString("obgyn_periksa_ginekologi")));
                        CekObgynDopplerCtgUsg.setSelected("Ya".equals(rsData.getString("obgyn_doppler_ctg_usg")));
                        CekObgynPasangTampon.setSelected("Ya".equals(rsData.getString("obgyn_pasang_tampon")));

                        // -- Kondisi Pasien Saat Pindah/Pulang --
                        // kp_pukul (DATETIME) sudah lengkap tanggal+jam - tidak
                        // perlu digabung dgn kolom "tanggal" lagi spt sebelumnya
                        // (dulu kp_pukul cuma TIME, tanpa tanggal sendiri).
                        String kpPukul = rsData.getString("kp_pukul");
                        if (kpPukul != null && !kpPukul.trim().equals("")) {
                            setJamDariString(DTPKpPukul, kpPukul);
                            if (DTPKpPukulSaja != null) {
                                String pkl = kpPukul.trim();
                                if (pkl.length() >= 19) pkl = pkl.substring(11, 19);
                                else if (pkl.length() >= 16) pkl = pkl.substring(11, 16);
                                DTPKpPukulSaja.setJamSql(pkl);
                            }
                        } else {
                            if (DTPKpPukulSaja != null) DTPKpPukulSaja.setSekarang();
                        }
                        TKpGcsE.setText(rsData.getString("kp_gcs_e"));
                        TKpGcsV.setText(rsData.getString("kp_gcs_v"));
                        TKpGcsM.setText(rsData.getString("kp_gcs_m"));
                        TKpTd.setText(rsData.getString("kp_td"));
                        TKpNadi.setText(rsData.getString("kp_nadi"));
                        String kpNadiReguler = rsData.getString("kp_nadi_reguler");
                        if ("Reguler".equals(kpNadiReguler))
                            RbKpNadiReguler.setSelected(true);
                        else if ("Ireguler".equals(kpNadiReguler))
                            RbKpNadiIreguler.setSelected(true);
                        else
                            bgKpNadiReguler.clearSelection();
                        TKpSuhu.setText(rsData.getString("kp_suhu"));
                        TKpPernafasan.setText(rsData.getString("kp_pernafasan"));
                        TKpSpo.setText(rsData.getString("kp_spo"));
                        boolean masukRs = "Ya".equals(rsData.getString("kp_masuk_rs"));
                        CekKpMasukRs.setSelected(masukRs);
                        TKpRuang.setVisible(masukRs);
                        BtnKpRuang.setVisible(masukRs);
                        if (selPetugasPenerima != null) selPetugasPenerima.setVisible(masukRs);
                        TKpRuang.setText(rsData.getString("kp_ruang"));
                        TKpPetugasPenerima.setText(rsData.getString("kp_petugas_penerima"));
                        NmKpPetugasPenerima.setText(TKpPetugasPenerima.getText().isEmpty() ? ""
                                : Sequel.CariPetugas(TKpPetugasPenerima.getText()));
                        String kpPendamping = rsData.getString("kp_pendamping");
                        if ("Dokter".equals(kpPendamping))
                            RbKpPendampingDokter.setSelected(true);
                        else if ("Perawat".equals(kpPendamping))
                            RbKpPendampingPerawat.setSelected(true);
                        else
                            bgKpPendamping.clearSelection();
                        String kpTl = rsData.getString("kp_tindakan_lanjutan");
                        if ("Kamar Operasi".equals(kpTl))
                            RbKpTlKamarOperasi.setSelected(true);
                        else if ("Kamar Bersalin".equals(kpTl))
                            RbKpTlKamarBersalin.setSelected(true);
                        else if ("HCU".equals(kpTl))
                            RbKpTlHcu.setSelected(true);
                        else if ("Kamar Jenazah".equals(kpTl))
                            RbKpTlKamarJenazah.setSelected(true);
                        else if ("Lainnya".equals(kpTl))
                            RbKpTlLainnya.setSelected(true);
                        else
                            bgKpTindakanLanjutan.clearSelection();
                        TKpTindakanLanjutanKet.setVisible("Lainnya".equals(kpTl));
                        TKpTindakanLanjutanKet.setText(rsData.getString("kp_tindakan_lanjutan_ket"));
                        boolean dirujuk = "Ya".equals(rsData.getString("kp_dirujuk"));
                        CekKpDirujuk.setSelected(dirujuk);
                        TKpDirujukKet.setVisible(dirujuk);
                        if (selDirujukAlasan != null) selDirujukAlasan.setVisible(dirujuk);
                        TKpDirujukKet.setText(rsData.getString("kp_dirujuk_ket"));
                        TKpDirujukAlasan.setText(rsData.getString("kp_dirujuk_alasan"));
                        boolean dipulangkan = "Ya".equals(rsData.getString("kp_dipulangkan"));
                        CekKpDipulangkan.setSelected(dipulangkan);
                        DTPKpDipulangkanPukul.setVisible(dipulangkan);
                        String kpDipulangkanPukul = rsData.getString("kp_dipulangkan_pukul");
                        if (kpDipulangkanPukul != null && !kpDipulangkanPukul.trim().equals("")) {
                            setJamDariString(DTPKpDipulangkanPukul, kpDipulangkanPukul);
                        }
                        boolean meninggal = "Ya".equals(rsData.getString("kp_meninggal"));
                        CekKpMeninggal.setSelected(meninggal);
                        DTPKpMeninggalPukul.setVisible(meninggal);
                        String kpMeninggalPukul = rsData.getString("kp_meninggal_pukul");
                        if (kpMeninggalPukul != null && !kpMeninggalPukul.trim().equals("")) {
                            setJamDariString(DTPKpMeninggalPukul, kpMeninggalPukul);
                        }
                        CekKpDoa.setSelected("Ya".equals(rsData.getString("kp_doa")));
                        CekKpEdukasiMakanObat.setSelected("Ya".equals(rsData.getString("kp_edukasi_makan_obat")));
                        CekKpEdukasiLuka.setSelected("Ya".equals(rsData.getString("kp_edukasi_luka")));
                        boolean eduDiet = "Ya".equals(rsData.getString("kp_edukasi_diet"));
                        CekKpEdukasiDiet.setSelected(eduDiet);
                        TKpEdukasiDietKet.setVisible(eduDiet);
                        TKpEdukasiDietKet.setText(rsData.getString("kp_edukasi_diet_ket"));
                        boolean eduLain = "Ya".equals(rsData.getString("kp_edukasi_lain"));
                        CekKpEdukasiLain.setSelected(eduLain);
                        TKpEdukasiLainKet.setVisible(eduLain);
                        TKpEdukasiLainKet.setText(rsData.getString("kp_edukasi_lain_ket"));
                        KdDokterKp.setText(rsData.getString("kp_kd_dokter"));
                        NmDokterKp.setText(rsData.getString("kp_nm_dokter"));
                    }
                } finally {
                    if (rsData != null) {
                        rsData.close();
                    }
                }
            } finally {
                if (psData != null) {
                    psData.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif getData RM 03-4/05/06 : " + e);
        }

        // -- I. Diagnosis Keperawatan: tandai baris master yg cocok dgn
        // pilihan tersimpan utk no_rawat ini --
        try {
            java.util.Set<String> kodeTersimpan = new java.util.HashSet<>();
            PreparedStatement psMasalah = koneksi.prepareStatement(
                    "select kode_masalah from penilaian_awal_keperawatan_igd_masalah where no_rawat=?");
            try {
                psMasalah.setString(1, noRawat);
                ResultSet rsMasalah = psMasalah.executeQuery();
                try {
                    while (rsMasalah.next()) {
                        kodeTersimpan.add(rsMasalah.getString(1));
                    }
                } finally {
                    if (rsMasalah != null) {
                        rsMasalah.close();
                    }
                }
            } finally {
                if (psMasalah != null) {
                    psMasalah.close();
                }
            }
            for (i = 0; i < tabModeMasalah.getRowCount(); i++) {
                tabModeMasalah.setValueAt(kodeTersimpan.contains(tabModeMasalah.getValueAt(i, 1).toString()), i, 0);
            }
        } catch (Exception e) {
            System.out.println("Notif getData Masalah Keperawatan : " + e);
        }

        tampilObatInfus(noRawat);
        bangunRingkasanHtml();
        updateGayaSkalaNyeri();
        hitungProgresKep();
    }

    /** Satu baris label:nilai utk ringkasan HTML (lihat bangunRingkasanHtml()). */
    private String baris(String label, String nilai) {
        return "<tr class='isi'><td valign='middle' width='35%'>" + label + "</td>" +
                "<td valign='middle' width='65%'>" + ((nilai == null || nilai.trim().isEmpty()) ? "-" : nilai)
                + "</td></tr>";
    }

    /** Baris judul section (bgcolor abu-abu muda, span 2 kolom). */
    private String judulBaris(String judul) {
        return "<tr class='isi'><td colspan='2' valign='middle' bgcolor='#FFFAF8' align='center'><b>" + judul
                + "</b></td></tr>";
    }

    /**
     * Baris label:nilai dgn latar warna (utk hasil/kesimpulan yg perlu
     * ditonjolkan).
     */
    private String barisWarna(String label, String nilai, String warna) {
        return "<tr class='isi'><td valign='middle' width='35%'>" + label + "</td>" +
                "<td valign='middle' width='65%' bgcolor='" + warna + "' color='#ffffff'>"
                + ((nilai == null || nilai.trim().isEmpty()) ? "-" : nilai) + "</td></tr>";
    }

    /** Nilai dan satuan tanpa menghasilkan teks satuan menggantung saat nilai kosong. */
    private String nilaiSatuanRingkasan(String nilai, String satuan) {
        if (nilai == null || nilai.trim().isEmpty()) {
            return "-";
        }
        return nilai.trim() + (satuan == null || satuan.isEmpty() ? "" : " " + satuan);
    }

    /**
     * Grid ringkas dua kelompok per baris. Cocok untuk angka klinis yang sebelumnya
     * dirangkai sebagai satu kalimat panjang dan hanya dipisahkan tanda koma.
     */
    private String gridDuaKolomRingkasan(String[][] item) {
        StringBuilder grid = new StringBuilder();
        grid.append("<tr class='isi'><td colspan='2' valign='top' cellpadding='0' cellspacing='0'>")
                .append("<table width='100%' border='0' cellpadding='3px' cellspacing='0'>");
        for (int i = 0; i < item.length; i += 2) {
            grid.append("<tr>")
                    .append("<td width='22%' bgcolor='#F7F9F8'>").append(item[i][0]).append("</td>")
                    .append("<td width='28%'><b>").append(item[i][1]).append("</b></td>");
            if (i + 1 < item.length) {
                grid.append("<td width='22%' bgcolor='#F7F9F8'>").append(item[i + 1][0]).append("</td>")
                        .append("<td width='28%'><b>").append(item[i + 1][1]).append("</b></td>");
            } else {
                grid.append("<td width='22%' bgcolor='#F7F9F8'>&nbsp;</td><td width='28%'>&nbsp;</td>");
            }
            grid.append("</tr>");
        }
        grid.append("</table></td></tr>");
        return grid.toString();
    }

    /**
     * Gabungan teks item checkbox yg tercentang (dipisah koma), "-" kalau tidak ada
     * yg dicentang.
     */
    private String cekList(javax.swing.AbstractButton... cek) {
        StringBuilder sb = new StringBuilder();
        for (javax.swing.AbstractButton c : cek) {
            if (c.isSelected()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(c.getText());
            }
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    /**
     * Ringkasan HTML read-only utk panel accordion "Data Pengkajian"
     * (ScrollHTML/LoadHTML) - pola sama persis dgn tab "Data Triase" di
     * RMTriaseIGD.java. Dipanggil di akhir getData() (setelah SEMUA field
     * termasuk checklist Diagnosis Keperawatan/tabModeMasalah dan
     * Pemberian Obat-Infus/tabModeObatInfus selesai dimuat), jadi dibaca
     * langsung dari widget yg sudah terisi - bukan query ulang - supaya
     * ringkasan selalu 100% konsisten dgn apa yg tampil di form edit.
     */
    private void bangunRingkasanHtml() {
        StringBuilder h = new StringBuilder();

        h.append(judulBaris("Pasien & Petugas"));
        h.append(baris("Nama Pasien", TPasien.getText()));
        h.append(baris("No.RM", TNoRM.getText()));
        h.append(baris("No. Rawat", TNoRw.getText()));
        h.append(baris("Jenis Kelamin / Tgl Lahir", Jk.getText() + " / " + TglLahir.getText()));
        h.append(baris("Tanggal Asuhan", getTanggalDanJamAsuhan()));
        h.append(baris("Petugas", NmPetugas.getText()));
        h.append(baris("Dokter", NmDokterKp.getText()));
        h.append(baris("Informasi Didapat Dari",
                Informasi.getSelectedItem() == null ? "-" : Informasi.getSelectedItem().toString()));

        h.append(judulBaris("Riwayat Kesehatan"));
        h.append(baris("Keluhan Utama", KeluhanUtama.getText().replaceAll("(\r\n|\r|\n)", "<br>")));

        h.append(judulBaris("Riwayat Alergi"));
        h.append(baris("Alergi", RbAlergiYa.isSelected() ? ("Ya, " + TAlergiSebutkan.getText()) : "Tidak"));

        h.append(judulBaris("Keadaan Umum"));
        h.append(gridDuaKolomRingkasan(new String[][] {
                { "Tekanan Darah", nilaiSatuanRingkasan(TdKep.getText(), "mmHg") },
                { "Nadi", nilaiSatuanRingkasan(NadiKep.getText(), "x/menit") },
                { "Pernapasan", nilaiSatuanRingkasan(RrKep.getText(), "x/menit") },
                { "Suhu", nilaiSatuanRingkasan(SuhuKep.getText(), "°C") },
                { "SpO2", nilaiSatuanRingkasan(SpoKep.getText(), "%") },
                { "Berat Badan", nilaiSatuanRingkasan(BbKep.getText(), "kg") },
                { "Tinggi Badan", nilaiSatuanRingkasan(TbKep.getText(), "cm") }
        }));

        if ("Hamil".equals(
                StatusKehamilan.getSelectedItem() == null ? "" : StatusKehamilan.getSelectedItem().toString())) {
            h.append(judulBaris("Status Kehamilan"));
            String hphtRingkasan = (TglHPHT != null && TglHPHT.getSelectedItem() != null)
                    ? TglHPHT.getSelectedItem().toString()
                    : HPHT.getText();
            h.append(gridDuaKolomRingkasan(new String[][] {
                    { "Gravida", Gravida.getText().trim().isEmpty() ? "-" : Gravida.getText() },
                    { "Para", Para.getText().trim().isEmpty() ? "-" : Para.getText() },
                    { "Abortus", Abortus.getText().trim().isEmpty() ? "-" : Abortus.getText() },
                    { "HPHT", hphtRingkasan == null || hphtRingkasan.trim().isEmpty() ? "-" : hphtRingkasan }
            }));
        }

        h.append(judulBaris("Asesmen Psikologis"));
        h.append(baris("Kondisi",
                cekList(CekPsiTenang, CekPsiCemas, CekPsiTakut, CekPsiSedih, CekPsiMarah, CekPsiBunuhDiri)));
        if (CekPsiMasalahPerilaku != null && CekPsiMasalahPerilaku.isSelected()) {
            if (RbMasalahPrilakuYa.isSelected()) {
                h.append(baris("Masalah Perilaku", "Ada, " + TMasalahPrilakuSebutkan.getText()));
            } else if (RbMasalahPrilakuTidak.isSelected()) {
                h.append(baris("Masalah Perilaku", "Tidak"));
            }
        }

        h.append(judulBaris("Status Sosial dan Ekonomis"));
        h.append(
                baris("Status Pernikahan",
                        RbNikahBelumMenikah.isSelected() ? "Belum Menikah"
                                : (RbNikahMenikah.isSelected() ? "Menikah"
                                        : (RbNikahCerai.isSelected() ? "Cerai"
                                                : (RbNikahLainnya.isSelected()
                                                        ? ("Lainnya" + (TKetStatusPernikahan.getText().trim().isEmpty() ? "" : (", " + TKetStatusPernikahan.getText())))
                                                        : "-")))));
        h.append(baris("Tempat Tinggal",
                RbTinggalSendiri.isSelected() ? "Rumah Sendiri"
                        : (RbTinggalKeluarga.isSelected() ? "Rumah Keluarganya"
                                : (RbTinggalLainnya.isSelected() ? ("Lainnya" + (TKetTempatTinggal.getText().trim().isEmpty() ? "" : (", " + TKetTempatTinggal.getText()))) : "-"))));
        h.append(baris("Pekerjaan", TPekerjaanKep.getText()));
        h.append(baris("Pendidikan", TPendidikanPasienKep.getText()));

        h.append(judulBaris("Skrining Geriatri"));
        h.append(baris("≥60th & >1 Penyakit", RbGeri1Ya.isSelected() ? "Ya" : "Tidak"));
        h.append(baris("≥60th, 1 Penyakit + Penurunan Fungsi", RbGeri2Ya.isSelected() ? "Ya" : "Tidak"));
        h.append(baris("≥70th & 1 Penyakit", RbGeri3Ya.isSelected() ? "Ya" : "Tidak"));

        h.append(judulBaris("Skrining Resiko Pasien Jatuh"));
        h.append(baris("A. Cara Berjalan", RbJatuhCaraBerjalanYa.isSelected() ? "Ya" : "Tidak"));
        h.append(baris("B. Menopang Saat Duduk", RbJatuhMenopangYa.isSelected() ? "Ya" : "Tidak"));
        h.append(barisWarna("Hasil", RbJatuhBeresiko.isSelected() ? "Beresiko" : "Tidak Beresiko",
                RbJatuhBeresiko.isSelected() ? "#AA0000" : "#00AA00"));

        h.append(judulBaris("Pengkajian Spiritual"));
        String teksAgama = (CmbAgama != null && CmbAgama.getSelectedIndex() > 0) ? CmbAgama.getSelectedItem().toString() : "-";
        h.append(baris("Agama", teksAgama));
        if ("Islam".equalsIgnoreCase(teksAgama)) {
            h.append(baris("Wajib Ibadah",
                    RbWajibIbadahBaligh.isSelected() ? "Baligh"
                            : (RbWajibIbadahBelumBaligh.isSelected() ? "Belum Baligh"
                                    : (RbWajibIbadahHalanganLain.isSelected()
                                            ? "Halangan Lain, " + TKetWajibIbadah.getText()
                                            : "-"))));
            h.append(baris("Thaharoh",
                    RbThaharohBerwudhu.isSelected() ? "Berwudhu" : (RbThaharohTayamum.isSelected() ? "Tayamum" : "-")));
            h.append(baris("Sholat", RbSholatBerdiri.isSelected() ? "Berdiri"
                    : (RbSholatDuduk.isSelected() ? "Duduk" : (RbSholatBerbaring.isSelected() ? "Berbaring" : "-"))));
        }
        h.append(baris("Nilai/Kepercayaan Tertentu",
                RbNilaiKepercayaanYa.isSelected() ? ("Ya, " + TKetNilaiKepercayaan.getText()) : "Tidak"));
        h.append(baris("Hambatan Budaya/Bahasa",
                (RbBudayaAda != null && RbBudayaAda.isSelected())
                        ? ("Ada, " + KetBudaya.getText())
                        : "Tidak Ada"));
        h.append(baris("Cacat Tubuh/Disabilitas", CekCacatTidak.isSelected() ? "Tidak"
                : cekList(CekCacatTunaNetra, CekCacatTunaDaksa, CekCacatTunaRungu, CekCacatTunaWicara)));

        boolean giziDewasaAktif = RbPilihGiziDewasa != null && RbPilihGiziDewasa.isSelected();
        boolean giziAnakAktif = RbPilihGiziAnak != null && RbPilihGiziAnak.isSelected();

        if (giziDewasaAktif) {
            h.append(judulBaris("Skrining Gizi Dewasa (MST)"));
            h.append(baris("Total Skor", TMstTotalSkor.getText()));
            h.append(barisWarna("Kesimpulan",
                    RbMstRisiko.isSelected() ? "Risiko" : (RbMstTidakRisiko.isSelected() ? "Tidak Risiko" : "-"),
                    RbMstRisiko.isSelected() ? "#AA0000" : "#00AA00"));
        } else if (giziAnakAktif) {
            h.append(judulBaris("Skrining Gizi Anak"));
            h.append(baris("Total Skor", TGiziAnakTotalSkor.getText()));
            String giziWarna = RbGiziAnakTinggi.isSelected() ? "#AA0000"
                    : (RbGiziAnakSedang.isSelected() ? "#FF9900" : "#00AA00");
            h.append(barisWarna("Kesimpulan",
                    RbGiziAnakRendah.isSelected() ? "Risiko Rendah"
                            : (RbGiziAnakSedang.isSelected() ? "Risiko Sedang"
                                    : (RbGiziAnakTinggi.isSelected() ? "Risiko Tinggi" : "-")),
                    giziWarna));
        } else {
            h.append(judulBaris("Skrining Gizi"));
            h.append(baris("Status", "Belum dilakukan skrining gizi"));
        }

        h.append(judulBaris("Asesmen Nyeri"));
        h.append(
                baris("Nyeri",
                        RbNyeriAdaYa.isSelected()
                                ? ("Ya, Lokasi: " + Lokasi.getText() + ", Skala: "
                                        + (SkalaNyeri.getSelectedItem() == null ? ""
                                                : SkalaNyeri.getSelectedItem().toString()))
                                : "Tidak"));

        h.append(judulBaris("FLACC"));
        h.append(baris("Total Skor", TFlaccTotal.getText()));

        h.append(judulBaris("PQRST"));
        h.append(baris("Provocation", cekList(CekProvCahaya, CekProvGelap, CekProvGerakan, CekProvBerbaring)));
        h.append(
                baris("Quality", cekList(CekQualTumpul, CekQualDitusuk, CekQualBerdenyut, CekQualTajam, CekQualPanas)));
        h.append(baris("Radiation", RbRadiationYa.isSelected() ? ("Ya, ke " + TRadiationKe.getText()) : "Tidak"));
        h.append(baris("Severity",
                RbSeverityTidakNyeri.isSelected() ? "Tidak Nyeri"
                        : (RbSeverityRingan.isSelected() ? "Ringan"
                                : (RbSeveritySedang.isSelected() ? "Sedang"
                                        : (RbSeverityBerat.isSelected() ? "Berat" : "-")))));
        h.append(baris("Time",
                RbTimeTerusMenerus.isSelected() ? "Terus Menerus"
                        : (RbTimeHilangTimbul.isSelected() ? "Hilang Timbul"
                                : (RbTimeKurang30.isSelected() ? "<30 Menit"
                                        : (RbTimeLebih30.isSelected() ? ">30 Menit" : "-")))));

        h.append(judulBaris("Diagnosis Keperawatan"));
        StringBuilder masalah = new StringBuilder();
        for (int mi = 0; mi < tabModeMasalah.getRowCount(); mi++) {
            if (Boolean.TRUE.equals(tabModeMasalah.getValueAt(mi, 0))) {
                if (masalah.length() > 0) {
                    masalah.append(", ");
                }
                masalah.append(tabModeMasalah.getValueAt(mi, 2).toString());
            }
        }
        h.append(baris("Masalah Terpilih", masalah.toString()));

        h.append(judulBaris("Intervensi Keperawatan"));
        h.append(baris("Tindakan", cekList(CekIntervManajemenJalanNafas, CekIntervSuctioningJalanNafas,
                CekIntervManajemenAsamBasa,
                CekIntervManajemenSyok, CekIntervManajemenNyeri, CekIntervManajemenCairan, CekIntervMonitoringCairan,
                CekIntervManajemenLingkungan, CekIntervPendidikanKesehatan, CekIntervFeverTreatment,
                CekIntervManajemenSensasiPerifer, CekIntervMonitoringKardiovaskuler, CekIntervPencegahanPenekananLuka) +
                (TIntervLainnya.getText().trim().isEmpty() ? "" : ", " + TIntervLainnya.getText())));

        h.append(judulBaris("Tindakan Keperawatan Mandiri"));
        h.append(baris("Rencana", Rencana.getText().replaceAll("(\r\n|\r|\n)", "<br>")));

        h.append(judulBaris("Kardiopulmonal / Penunjang / Kolaborasi / Trauma / THT-Mata / Obgyn"));
        h.append(baris("Kardiopulmonal",
                cekList(CekKardioPulseOximetri, CekKardioIntubasi, CekKardioRjp, CekKardioKardioversiDcShock)));
        h.append(baris("Tindakan Penunjang", cekList(CekPenunjangCekLab, CekPenunjangRontgen, CekPenunjangCtScan)));
        h.append(baris("Tindakan Kolaborasi",
                cekList(CekKolabO2, CekKolabEkgMonitor, CekKolabNebulizer, CekKolabPasangInfus,
                        CekKolabInjeksiSyringePump, CekKolabTransfusiDarah, CekKolabNgt, CekKolabBilasLambung,
                        CekKolabDowerCatheter, CekKolabRestrainPengaman)));
        h.append(baris("Trauma/Bedah",
                cekList(CekTraumaCervicalCollar, CekTraumaImmobilisasiTulangBelakang, CekTraumaGips,
                        CekTraumaPerawatanLuka, CekTraumaJahitLuka, CekTraumaBalutanSpalk, CekTraumaSelangDadaWsd)));
        h.append(baris("THT/Mata", cekList(CekThtIrigasiMataTelinga, CekThtAmbilKorpusAlienum)));
        h.append(baris("Obgyn", cekList(CekObgynMembantuPartus, CekObgynPeriksaGinekologi, CekObgynDopplerCtgUsg,
                CekObgynPasangTampon)));

        h.append(judulBaris("Pemberian Obat/Infus"));
        if (tabModeObatInfus.getRowCount() == 0) {
            h.append(baris("Obat/Infus", "-"));
        } else {
            for (int oi = 0; oi < tabModeObatInfus.getRowCount(); oi++) {
                h.append(baris(
                        tabModeObatInfus.getValueAt(oi, 0) + " - " + tabModeObatInfus.getValueAt(oi, 1),
                        "Dosis " + tabModeObatInfus.getValueAt(oi, 2) + ", Rute "
                                + tabModeObatInfus.getValueAt(oi, 3)));
            }
        }

        h.append(judulBaris("Kondisi Pasien Saat Pindah/Pulang dari IGD"));
        String disposisi = CekKpDipulangkan.isSelected() ? "Dipulangkan"
                : (CekKpMeninggal.isSelected() ? "Meninggal"
                        : (CekKpDoa.isSelected() ? "Death On Arrival"
                                : (CekKpDirujuk.isSelected() ? "Dirujuk"
                                        : (CekKpMasukRs.isSelected() ? "Masuk Rumah Sakit" : "-"))));
        h.append(baris("Disposisi", disposisi));
        h.append(baris("Pendidikan Kesehatan Pulang",
                cekList(CekKpEdukasiMakanObat, CekKpEdukasiLuka, CekKpEdukasiDiet, CekKpEdukasiLain)));

        LoadHTML.setText(
                "<html><table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                        +
                        h.toString() +
                        "</table></html>");
        posisikanRingkasanHtmlKeAtas();
    }

    /** Pastikan ringkasan selalu dibuka dari baris pertama setelah layout HTML selesai. */
    private void posisikanRingkasanHtmlKeAtas() {
        LoadHTML.setCaretPosition(0);
        ScrollHTML.getViewport().setViewPosition(new java.awt.Point(0, 0));
        javax.swing.SwingUtilities.invokeLater(() -> {
            LoadHTML.setCaretPosition(0);
            ScrollHTML.getVerticalScrollBar().setValue(ScrollHTML.getVerticalScrollBar().getMinimum());
            ScrollHTML.getViewport().setViewPosition(new java.awt.Point(0, 0));
        });
    }

    private void tarikDataTriase(String norwt, boolean manual) {
        if (norwt == null || norwt.trim().isEmpty()) {
            if (manual) {
                javax.swing.JOptionPane.showMessageDialog(this, "Nomor Rawat belum dipilih / kosong!", "Pemberitahuan", javax.swing.JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        boolean ditemukan = false;
        try {
            // 1. Coba dari data_triase_igd & data_triase_pacs / primer
            java.sql.PreparedStatement psTriase = koneksi.prepareStatement(
                "select d.tekanan_darah, d.nadi, d.pernapasan, d.suhu, d.saturasi_o2, d.berat_badan, d.tinggi_badan, d.kesadaran, " +
                "coalesce(p.keluhan_utama, r.keluhan_utama, '') as keluhan_utama " +
                "from data_triase_igd d " +
                "left join data_triase_pacs p on p.no_rawat=d.no_rawat " +
                "left join data_triase_igdprimer r on r.no_rawat=d.no_rawat " +
                "where d.no_rawat=?"
            );
            psTriase.setString(1, norwt.trim());
            java.sql.ResultSet rsTriase = psTriase.executeQuery();
            if (rsTriase.next()) {
                String td = rsTriase.getString("tekanan_darah");
                String nadi = rsTriase.getString("nadi");
                String rr = rsTriase.getString("pernapasan");
                String suhu = rsTriase.getString("suhu");
                String spo = rsTriase.getString("saturasi_o2");
                String bb = rsTriase.getString("berat_badan");
                String tb = rsTriase.getString("tinggi_badan");
                String kel = rsTriase.getString("keluhan_utama");

                if (td != null && !td.trim().isEmpty() && (TdKep.getText().trim().isEmpty() || manual)) TdKep.setText(td.trim());
                if (nadi != null && !nadi.trim().isEmpty() && (NadiKep.getText().trim().isEmpty() || manual)) NadiKep.setText(nadi.trim());
                if (rr != null && !rr.trim().isEmpty() && (RrKep.getText().trim().isEmpty() || manual)) RrKep.setText(rr.trim());
                if (suhu != null && !suhu.trim().isEmpty() && (SuhuKep.getText().trim().isEmpty() || manual)) SuhuKep.setText(suhu.trim());
                if (spo != null && !spo.trim().isEmpty() && (SpoKep.getText().trim().isEmpty() || manual)) SpoKep.setText(spo.trim());
                if (bb != null && !bb.trim().isEmpty() && (BbKep.getText().trim().isEmpty() || manual)) BbKep.setText(bb.trim());
                if (tb != null && !tb.trim().isEmpty() && (TbKep.getText().trim().isEmpty() || manual)) TbKep.setText(tb.trim());
                if (kel != null && !kel.trim().isEmpty() && (KeluhanUtama.getText().trim().isEmpty() || manual)) {
                    KeluhanUtama.setText(kel.trim());
                }
                ditemukan = true;
            }
            rsTriase.close();
            psTriase.close();

            // 2. Fallback: Cek pemeriksaan_ralan jika di triase belum ada
            if (!ditemukan) {
                java.sql.PreparedStatement psRalan = koneksi.prepareStatement(
                    "select tensi, nadi, respirasi, suhu_tubuh, spo2, tinggi, berat, keluhan " +
                    "from pemeriksaan_ralan where no_rawat=? order by tgl_perawatan desc, jam_rawat desc limit 1"
                );
                psRalan.setString(1, norwt.trim());
                java.sql.ResultSet rsRalan = psRalan.executeQuery();
                if (rsRalan.next()) {
                    String td = rsRalan.getString("tensi");
                    String nadi = rsRalan.getString("nadi");
                    String rr = rsRalan.getString("respirasi");
                    String suhu = rsRalan.getString("suhu_tubuh");
                    String spo = rsRalan.getString("spo2");
                    String bb = rsRalan.getString("berat");
                    String tb = rsRalan.getString("tinggi");
                    String kel = rsRalan.getString("keluhan");

                    if (td != null && !td.trim().isEmpty() && (TdKep.getText().trim().isEmpty() || manual)) TdKep.setText(td.trim());
                    if (nadi != null && !nadi.trim().isEmpty() && (NadiKep.getText().trim().isEmpty() || manual)) NadiKep.setText(nadi.trim());
                    if (rr != null && !rr.trim().isEmpty() && (RrKep.getText().trim().isEmpty() || manual)) RrKep.setText(rr.trim());
                    if (suhu != null && !suhu.trim().isEmpty() && (SuhuKep.getText().trim().isEmpty() || manual)) SuhuKep.setText(suhu.trim());
                    if (spo != null && !spo.trim().isEmpty() && (SpoKep.getText().trim().isEmpty() || manual)) SpoKep.setText(spo.trim());
                    if (bb != null && !bb.trim().isEmpty() && (BbKep.getText().trim().isEmpty() || manual)) BbKep.setText(bb.trim());
                    if (tb != null && !tb.trim().isEmpty() && (TbKep.getText().trim().isEmpty() || manual)) TbKep.setText(tb.trim());
                    if (kel != null && !kel.trim().isEmpty() && (KeluhanUtama.getText().trim().isEmpty() || manual)) {
                        KeluhanUtama.setText(kel.trim());
                    }
                    ditemukan = true;
                }
                rsRalan.close();
                psRalan.close();
            }

            hitungProgresKep();

            if (manual) {
                if (ditemukan) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Data TTV & Keluhan berhasil disalin dari Triase / Pemeriksaan Awal!", "Sukses", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Belum ada data Triase atau Pemeriksaan Awal untuk pasien ini.", "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarikDataTriase Keperawatan: " + e);
            if (manual) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal menarik data: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean tarikDataLaboratorium(boolean silentIfEmpty) {
        String noRawat = TNoRw.getText().trim();
        if (noRawat.isEmpty()) {
            if (!silentIfEmpty) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Nomor rawat pasien belum dipilih / kosong!", "Peringatan",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        java.util.LinkedHashSet<String> semuaLab = new java.util.LinkedHashSet<>();
        // a. detail_periksa_lab (hasil lab PK)
        String sqlHasilPK = "SELECT DISTINCT jp.nm_perawatan FROM detail_periksa_lab dp " +
                            "INNER JOIN jns_perawatan_lab jp ON dp.kd_jenis_prw = jp.kd_jenis_prw " +
                            "WHERE dp.no_rawat = ? ORDER BY jp.nm_perawatan ASC";
        try (PreparedStatement psLab = koneksi.prepareStatement(sqlHasilPK)) {
            psLab.setString(1, noRawat);
            try (ResultSet rsLab = psLab.executeQuery()) {
                while (rsLab.next()) {
                    String nm = rsLab.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) semuaLab.add(nm.trim());
                }
            }
        } catch (Exception ignored) {}

        // b. periksa_lab
        String sqlPeriksaLab = "SELECT DISTINCT jp.nm_perawatan FROM periksa_lab pl " +
                               "INNER JOIN jns_perawatan_lab jp ON pl.kd_jenis_prw = jp.kd_jenis_prw " +
                               "WHERE pl.no_rawat = ? ORDER BY jp.nm_perawatan ASC";
        try (PreparedStatement psLab = koneksi.prepareStatement(sqlPeriksaLab)) {
            psLab.setString(1, noRawat);
            try (ResultSet rsLab = psLab.executeQuery()) {
                while (rsLab.next()) {
                    String nm = rsLab.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) semuaLab.add(nm.trim());
                }
            }
        } catch (Exception ignored) {}

        // c. permintaan_lab & permintaan_pemeriksaan_lab (order PK)
        String sqlOrderPK = "SELECT DISTINCT jp.nm_perawatan FROM permintaan_lab pl " +
                            "INNER JOIN permintaan_pemeriksaan_lab ppl ON pl.noorder = ppl.noorder " +
                            "INNER JOIN jns_perawatan_lab jp ON ppl.kd_jenis_prw = jp.kd_jenis_prw " +
                            "WHERE pl.no_rawat = ? ORDER BY jp.nm_perawatan ASC";
        try (PreparedStatement psLab = koneksi.prepareStatement(sqlOrderPK)) {
            psLab.setString(1, noRawat);
            try (ResultSet rsLab = psLab.executeQuery()) {
                while (rsLab.next()) {
                    String nm = rsLab.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) semuaLab.add(nm.trim());
                }
            }
        } catch (Exception ignored) {}

        // d. permintaan_labpa (order PA)
        String sqlOrderPA = "SELECT DISTINCT jp.nm_perawatan FROM permintaan_labpa pl " +
                            "INNER JOIN permintaan_pemeriksaan_labpa ppl ON pl.noorder = ppl.noorder " +
                            "INNER JOIN jns_perawatan_lab jp ON ppl.kd_jenis_prw = jp.kd_jenis_prw " +
                            "WHERE pl.no_rawat = ? ORDER BY jp.nm_perawatan ASC";
        try (PreparedStatement psLab = koneksi.prepareStatement(sqlOrderPA)) {
            psLab.setString(1, noRawat);
            try (ResultSet rsLab = psLab.executeQuery()) {
                while (rsLab.next()) {
                    String nm = rsLab.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) semuaLab.add(nm.trim());
                }
            }
        } catch (Exception ignored) {}

        // e. permintaan_labmb (order MB)
        String sqlOrderMB = "SELECT DISTINCT jp.nm_perawatan FROM permintaan_labmb pl " +
                            "INNER JOIN permintaan_pemeriksaan_labmb ppl ON pl.noorder = ppl.noorder " +
                            "INNER JOIN jns_perawatan_lab jp ON ppl.kd_jenis_prw = jp.kd_jenis_prw " +
                            "WHERE pl.no_rawat = ? ORDER BY jp.nm_perawatan ASC";
        try (PreparedStatement psLab = koneksi.prepareStatement(sqlOrderMB)) {
            psLab.setString(1, noRawat);
            try (ResultSet rsLab = psLab.executeQuery()) {
                while (rsLab.next()) {
                    String nm = rsLab.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) semuaLab.add(nm.trim());
                }
            }
        } catch (Exception ignored) {}

        if (!semuaLab.isEmpty()) {
            String hasilLab = String.join(", ", semuaLab);
            if (txtPenunjangLab != null) txtPenunjangLab.setText(hasilLab);
            TPenunjangCekLabKet.setText(hasilLab);
            CekPenunjangCekLab.setSelected(true);
            if (panelPenunjangMedis != null) panelPenunjangMedis.perbaruiSemuaBadge();
            hitungProgresKep();
            if (!silentIfEmpty) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Data pemeriksaan Laboratorium (" + semuaLab.size() + " item) berhasil disalin!",
                    "Sukses", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            return true;
        } else {
            if (!silentIfEmpty) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Belum ada data permintaan maupun hasil pemeriksaan Laboratorium untuk pasien ini.",
                    "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            return false;
        }
    }

    private boolean tarikDataRadiologi(boolean silentIfEmpty) {
        String noRawat = TNoRw.getText().trim();
        if (noRawat.isEmpty()) {
            if (!silentIfEmpty) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Nomor rawat pasien belum dipilih / kosong!", "Peringatan",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        java.util.LinkedHashSet<String> semuaRad = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<String> semuaRontgen = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<String> semuaCtScan = new java.util.LinkedHashSet<>();

        // a. periksa_radiologi
        String sqlPeriksaRad = "SELECT DISTINCT jpr.nm_perawatan FROM periksa_radiologi px " +
                               "INNER JOIN jns_perawatan_radiologi jpr ON px.kd_jenis_prw = jpr.kd_jenis_prw " +
                               "WHERE px.no_rawat = ? ORDER BY jpr.nm_perawatan ASC";
        try (PreparedStatement psRad = koneksi.prepareStatement(sqlPeriksaRad)) {
            psRad.setString(1, noRawat);
            try (ResultSet rsRad = psRad.executeQuery()) {
                while (rsRad.next()) {
                    String nm = rsRad.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) {
                        String clean = nm.trim();
                        semuaRad.add(clean);
                        String upper = clean.toUpperCase();
                        if (upper.contains("CT") || upper.contains("SCAN") || upper.contains("MSCT")) {
                            semuaCtScan.add(clean);
                        } else {
                            semuaRontgen.add(clean);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // b. permintaan_radiologi & permintaan_pemeriksaan_radiologi
        String sqlOrderRad = "SELECT DISTINCT jr.nm_perawatan FROM permintaan_radiologi pr " +
                             "INNER JOIN permintaan_pemeriksaan_radiologi ppr ON pr.noorder = ppr.noorder " +
                             "INNER JOIN jns_perawatan_radiologi jr ON ppr.kd_jenis_prw = jr.kd_jenis_prw " +
                             "WHERE pr.no_rawat = ? ORDER BY jr.nm_perawatan ASC";
        try (PreparedStatement psRad = koneksi.prepareStatement(sqlOrderRad)) {
            psRad.setString(1, noRawat);
            try (ResultSet rsRad = psRad.executeQuery()) {
                while (rsRad.next()) {
                    String nm = rsRad.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) {
                        String clean = nm.trim();
                        semuaRad.add(clean);
                        String upper = clean.toUpperCase();
                        if (upper.contains("CT") || upper.contains("SCAN") || upper.contains("MSCT")) {
                            semuaCtScan.add(clean);
                        } else {
                            semuaRontgen.add(clean);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        if (!semuaRad.isEmpty()) {
            String hasilSemua = String.join(", ", semuaRad);
            if (txtPenunjangRad != null) txtPenunjangRad.setText(hasilSemua);

            if (!semuaRontgen.isEmpty()) {
                CekPenunjangRontgen.setSelected(true);
                TPenunjangRontgenKet.setText(String.join(", ", semuaRontgen));
            } else {
                CekPenunjangRontgen.setSelected(true);
                TPenunjangRontgenKet.setText(hasilSemua);
            }

            if (!semuaCtScan.isEmpty()) {
                CekPenunjangCtScan.setSelected(true);
                TPenunjangCtScanKet.setText(String.join(", ", semuaCtScan));
            } else {
                CekPenunjangCtScan.setSelected(false);
                TPenunjangCtScanKet.setText("");
            }

            if (panelPenunjangMedis != null) panelPenunjangMedis.perbaruiSemuaBadge();
            hitungProgresKep();
            if (!silentIfEmpty) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Data pemeriksaan Radiologi (" + semuaRad.size() + " item) berhasil disalin!",
                    "Sukses", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            return true;
        } else {
            if (!silentIfEmpty) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Belum ada data permintaan maupun hasil pemeriksaan Radiologi untuk pasien ini.",
                    "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            return false;
        }
    }

    private boolean tarikSemuaDataPenunjang() {
        return tarikDataPenunjang(TNoRw.getText(), true);
    }

    private boolean tarikDataPenunjang(String noRawat, boolean manual) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            if (manual) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Nomor rawat pasien belum dipilih / kosong!", "Peringatan",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        boolean adaLab = tarikDataLaboratorium(true);
        boolean adaRad = tarikDataRadiologi(true);
        boolean adaEkg = false;

        // Cek EKG (hasil_pemeriksaan_ekg)
        String sqlEkg = "SELECT 1 FROM hasil_pemeriksaan_ekg WHERE no_rawat = ? LIMIT 1";
        try (PreparedStatement psEkg = koneksi.prepareStatement(sqlEkg)) {
            psEkg.setString(1, noRawat.trim());
            try (ResultSet rsEkg = psEkg.executeQuery()) {
                if (rsEkg.next()) {
                    adaEkg = true;
                    if (CekKolabEkgMonitor != null) {
                        CekKolabEkgMonitor.setSelected(true);
                    }
                }
            }
        } catch (Exception ignored) {}

        if (panelPenunjangMedis != null) panelPenunjangMedis.perbaruiSemuaBadge();
        hitungProgresKep();

        boolean adaData = adaLab || adaRad || adaEkg;
        if (manual) {
            if (adaData) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Data pemeriksaan penunjang (Laboratorium & Radiologi) berhasil disalin dari permintaan/hasil!",
                    "Sukses", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Belum ada data permintaan maupun hasil pemeriksaan penunjang (Lab & Radiologi) untuk pasien ini.",
                    "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        }
        return adaData;
    }

    private void isRawat() {
        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien, if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"
                            +
                            "pasien.tgl_lahir,reg_periksa.tgl_registrasi, " +
                            "pasien.stts_nikah,pasien.pekerjaan,pasien.pnd,penjab.png_jawab,reg_periksa.jam_reg " +
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                            "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj " +
                            "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TNoRM1.setText(rs.getString("no_rkm_medis"));
                    TPasien1.setText(rs.getString("nm_pasien"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    StatusPernikahan.setText(rs.getString("stts_nikah"));
                    Pekerjaan.setText(rs.getString("pekerjaan"));
                    PendidikanPasien.setText(rs.getString("pnd"));
                    Pembayaran.setText(rs.getString("png_jawab"));
                    TanggalRegistrasi.setText(rs.getString("tgl_registrasi") + " " + rs.getString("jam_reg"));
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
        // Pengkajian baru (belum tentu ada data lama) - kosongkan daftar
        // Pemberian Obat/Infus, akan dimuat ulang oleh getData() kalau
        // ternyata baris existing dipilih setelah ini.
        Valid.tabelKosong(tabModeObatInfus);
        
        // Auto-tarik data Triase IGD & Pemeriksaan Penunjang jika belum ada asesmen tersimpan
        if (Sequel.cariInteger("select count(*) from penilaian_awal_keperawatan_igd where no_rawat=?", TNoRw.getText()) == 0) {
            tarikDataTriase(TNoRw.getText(), false);
            tarikDataPenunjang(TNoRw.getText(), false);
        }
    }

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        isRawat();
    }

    public void isCek() {
        BtnSimpan.setEnabled(akses.getpenilaian_awal_keperawatan_igd());
        BtnHapus.setEnabled(akses.getpenilaian_awal_keperawatan_igd());
        BtnEdit.setEnabled(akses.getpenilaian_awal_keperawatan_igd());
        BtnEdit.setEnabled(akses.getpenilaian_awal_keperawatan_igd());
        BtnTambahMasalah.setEnabled(akses.getmaster_masalah_keperawatan_igd());
        BtnTambahRencana.setEnabled(akses.getmaster_rencana_keperawatan_igd());
        if (akses.getjml2() >= 1) {
            KdPetugas.setEditable(false);
            BtnPetugas.setEnabled(false);
            KdPetugas.setText(akses.getkode());
            NmPetugas.setText(Sequel.CariPetugas(KdPetugas.getText()));
            if (NmPetugas.getText().equals("")) {
                KdPetugas.setText("");
                JOptionPane.showMessageDialog(null, "User login bukan petugas...!!");
            }
        }

        if (TANGGALMUNDUR.equals("no")) {
            if (!akses.getkode().equals("Admin Utama")) {
                TglAsuhan.setEditable(false);
                TglAsuhan.setEnabled(false);
                if (CmbJam != null) {
                    CmbJam.setEnabled(false);
                }
                if (CmbMenit != null) {
                    CmbMenit.setEnabled(false);
                }
                if (CmbDetik != null) {
                    CmbDetik.setEnabled(false);
                }
                if (ChkJln != null) {
                    ChkJln.setEnabled(false);
                }
            }
        }
    }

    public void setTampil() {
        TabRawat.setSelectedIndex(1);
    }

    private void tampilMasalah() {
        try {
            Valid.tabelKosong(tabModeMasalah);
            file = new File("./cache/masalahkeperawatanigd.iyem");
            file.createNewFile();
            fileWriter = new FileWriter(file);
            StringBuilder iyembuilder = new StringBuilder();
            ps = koneksi.prepareStatement(
                    "select * from master_masalah_keperawatan_igd order by master_masalah_keperawatan_igd.kode_masalah");
            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabModeMasalah.addRow(new Object[] { false, rs.getString(1), rs.getString(2) });
                    iyembuilder.append("{\"KodeMasalah\":\"").append(rs.getString(1)).append("\",\"NamaMasalah\":\"")
                            .append(rs.getString(2)).append("\"},");
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

            if (iyembuilder.length() > 0) {
                iyembuilder.setLength(iyembuilder.length() - 1);
                fileWriter.write("{\"masalahkeperawatanigd\":[" + iyembuilder + "]}");
                fileWriter.flush();
            }

            fileWriter.close();
            iyembuilder = null;
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            if (fileWriter != null)
                try {
                    fileWriter.close();
                } catch (Exception e) {
                }
        }
    }

    private void tampilMasalah2() {
        try {
            jml = 0;
            for (i = 0; i < tbMasalahKeperawatan.getRowCount(); i++) {
                if (tbMasalahKeperawatan.getValueAt(i, 0).toString().equals("true")) {
                    jml++;
                }
            }

            pilih = new boolean[jml];
            kode = new String[jml];
            masalah = new String[jml];

            index = 0;
            for (i = 0; i < tbMasalahKeperawatan.getRowCount(); i++) {
                if (tbMasalahKeperawatan.getValueAt(i, 0).toString().equals("true")) {
                    pilih[index] = true;
                    kode[index] = tbMasalahKeperawatan.getValueAt(i, 1).toString();
                    masalah[index] = tbMasalahKeperawatan.getValueAt(i, 2).toString();
                    index++;
                }
            }

            Valid.tabelKosong(tabModeMasalah);

            for (i = 0; i < jml; i++) {
                tabModeMasalah.addRow(new Object[] {
                        pilih[i], kode[i], masalah[i]
                });
            }

            pilih = null;
            kode = null;
            masalah = null;

            myObj = new FileReader("./cache/masalahkeperawatanigd.iyem");
            root = mapper.readTree(myObj);
            response = root.path("masalahkeperawatanigd");
            if (response.isArray()) {
                for (JsonNode list : response) {
                    if (list.path("KodeMasalah").asText().toLowerCase().contains(TCariMasalah.getText().toLowerCase())
                            || list.path("NamaMasalah").asText().toLowerCase()
                                    .contains(TCariMasalah.getText().toLowerCase())) {
                        tabModeMasalah.addRow(new Object[] {
                                false, list.path("KodeMasalah").asText(), list.path("NamaMasalah").asText()
                        });
                    }
                }
            }
            myObj.close();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            if (myObj != null)
                try {
                    myObj.close();
                } catch (Exception e) {
                }
            response = null;
            root = null;
        }
    }

    private void tampilRencana() {
        try {
            file = new File("./cache/rencanakeperawatanigd.iyem");
            file.createNewFile();
            fileWriter = new FileWriter(file);
            StringBuilder iyembuilder = new StringBuilder();
            ps = koneksi.prepareStatement(
                    "select * from master_rencana_keperawatan_igd order by master_rencana_keperawatan_igd.kode_rencana");
            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    iyembuilder.append("{\"KodeMasalah\":\"").append(rs.getString(1)).append("\",\"KodeRencana\":\"")
                            .append(rs.getString(2)).append("\",\"NamaRencana\":\"").append(rs.getString(3))
                            .append("\"},");
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

            if (iyembuilder.length() > 0) {
                iyembuilder.setLength(iyembuilder.length() - 1);
                fileWriter.write("{\"rencanakeperawatanigd\":[" + iyembuilder + "]}");
                fileWriter.flush();
            }

            fileWriter.close();
            iyembuilder = null;
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            if (fileWriter != null)
                try {
                    fileWriter.close();
                } catch (Exception e) {
                }
        }
    }

    private void tampilRencana2() {
        try {
            jml = 0;
            for (i = 0; i < tbRencanaKeperawatan.getRowCount(); i++) {
                if (tbRencanaKeperawatan.getValueAt(i, 0).toString().equals("true")) {
                    jml++;
                }
            }

            pilih = new boolean[jml];
            kode = new String[jml];
            masalah = new String[jml];

            index = 0;
            for (i = 0; i < tbRencanaKeperawatan.getRowCount(); i++) {
                if (tbRencanaKeperawatan.getValueAt(i, 0).toString().equals("true")) {
                    pilih[index] = true;
                    kode[index] = tbRencanaKeperawatan.getValueAt(i, 1).toString();
                    masalah[index] = tbRencanaKeperawatan.getValueAt(i, 2).toString();
                    index++;
                }
            }

            Valid.tabelKosong(tabModeRencana);

            for (i = 0; i < jml; i++) {
                tabModeRencana.addRow(new Object[] {
                        pilih[i], kode[i], masalah[i]
                });
            }

            pilih = null;
            kode = null;
            masalah = null;

            myObj = new FileReader("./cache/rencanakeperawatanigd.iyem");
            root = mapper.readTree(myObj);
            response = root.path("rencanakeperawatanigd");
            if (response.isArray()) {
                for (i = 0; i < tbMasalahKeperawatan.getRowCount(); i++) {
                    if (tbMasalahKeperawatan.getValueAt(i, 0).toString().equals("true")) {
                        for (JsonNode list : response) {
                            if (list.path("KodeMasalah").asText().toLowerCase()
                                    .equals(tbMasalahKeperawatan.getValueAt(i, 1).toString()) &&
                                    list.path("NamaRencana").asText().toLowerCase()
                                            .contains(TCariRencana.getText().toLowerCase())) {
                                tabModeRencana.addRow(new Object[] {
                                        false, list.path("KodeRencana").asText(), list.path("NamaRencana").asText()
                                });
                            }
                        }
                    }
                }
            }
            myObj.close();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            if (myObj != null)
                try {
                    myObj.close();
                } catch (Exception e) {
                }
            response = null;
            root = null;
        }
    }

    private void isMenu() {
        if (ChkAccor.isSelected() == true) {
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(470, HEIGHT));
            FormMenu.setVisible(true);
            ScrollHTML.setVisible(true);
            ChkAccor.setVisible(true);
        } else if (ChkAccor.isSelected() == false) {
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(15, HEIGHT));
            FormMenu.setVisible(false);
            ScrollHTML.setVisible(false);
            ChkAccor.setVisible(true);
        }
    }

    /**
     * [RM 03-4/05/06] Kolom aktif tabel `penilaian_awal_keperawatan_igd`
     * (di luar `no_rawat`) yang ditulis oleh simpan()/ganti() - urutan
     * HARUS sinkron dengan nilaiKep(), dipakai bersama supaya SET clause
     * (ganti) dan daftar kolom INSERT (simpan) tidak pernah beda urutan
     * dari nilainya. Kolom lama yang dipensiunkan (psikologis/jiwa/
     * perilaku/dilaporkan/sebutkan, tinggal_dengan/ket_tinggal, nyeri,
     * provokes/ket_provokes/quality/ket_quality/menyebar/durasi, berjalan_
     * a/b/c/hasil) sengaja TIDAK disebut - dapat DEFAULT dari migrasi SQL
     * (KELOMPOK V) saat INSERT.
     */
    private static final String[] KOLOM_KEP = {
            // -- Riwayat Kesehatan + II. Pemeriksaan Fisik (lama, aktif) --
            "tanggal", "informasi", "keluhan_utama", "rpd", "rpo", "status_kehamilan", "gravida", "para", "abortus",
            "hpht",
            "tekanan", "pupil", "neurosensorik", "integumen", "turgor", "edema", "mukosa", "perdarahan",
            "jumlah_perdarahan", "warna_perdarahan",
            "intoksikasi", "bab", "xbab", "kbab", "wbab", "bak", "xbak", "wbak", "lbak",
            "hubungan", "budaya", "ket_budaya", "pendidikan_pj", "ket_pendidikan_pj", "edukasi", "ket_edukasi",
            "kemampuan", "aktifitas", "alat_bantu", "ket_bantu",
            "lokasi", "skala_nyeri", "nyeri_hilang", "ket_nyeri", "pada_dokter", "ket_dokter", "lapor", "ket_lapor",
            "rencana", "nip",

            // -- Riwayat Alergi --
            "alergi_ada", "alergi_sebutkan",

            // -- Keadaan Umum (Tanda Vital) --
            "td", "nadi", "suhu", "spo", "rr", "bb", "tb",

            // -- Asesmen Psikologis --
            "psi_tenang", "psi_cemas", "psi_takut", "psi_sedih", "psi_marah", "psi_bunuh_diri", "psi_masalah_prilaku",
            "psi_masalah_prilaku_sebutkan",

            // -- Status Sosial dan Ekonomis --
            "status_pernikahan", "ket_status_pernikahan", "tempat_tinggal", "ket_tempat_tinggal", "pekerjaan_kep",
            "pendidikan_pasien_kep",

            // -- Skrining Geriatri --
            "geri_60_multi_penyakit", "geri_60_penurunan_fungsi", "geri_70_1_penyakit",

            // -- Skrining Resiko Jatuh --
            "jatuh_cara_berjalan", "jatuh_menopang_duduk", "jatuh_hasil",

            // -- Pengkajian Spiritual --
            "wajib_ibadah", "ket_wajib_ibadah", "thaharoh", "sholat", "nilai_kepercayaan", "ket_nilai_kepercayaan",
            "agama_manual", "cacat_tidak", "cacat_tuna_netra", "cacat_tuna_daksa", "cacat_tuna_rungu",
            "cacat_tuna_wicara",

            // -- Skrining Gizi Dewasa (MST) --
            "mst_penurunan_bb", "mst_penurunan_bb_kg", "mst_asupan_berkurang", "mst_total_skor", "mst_diagnosis_khusus",
            "mst_diagnosis_khusus_sebutkan", "mst_kesimpulan",

            // -- Skrining Gizi Anak --
            "gizi_anak_kurus", "gizi_anak_turun_bb", "gizi_anak_kondisi_khusus", "gizi_anak_risiko_malnutrisi",
            "gizi_anak_total_skor", "gizi_anak_kesimpulan",

            // -- Asesmen Nyeri (toggle) --
            "nyeri_ada", "kategori_usia_nyeri",

            // -- FLACC --
            "flacc_wajah", "flacc_kaki", "flacc_aktivitas", "flacc_menangis", "flacc_bersuara", "flacc_total",

            // -- PQRST --
            "pqrst_prov_cahaya", "pqrst_prov_gelap", "pqrst_prov_gerakan", "pqrst_prov_berbaring",
            "pqrst_qual_tumpul", "pqrst_qual_ditusuk", "pqrst_qual_berdenyut", "pqrst_qual_tajam", "pqrst_qual_panas",
            "pqrst_radiation", "pqrst_radiation_ke", "pqrst_severity", "pqrst_time",

            // -- II. Intervensi Keperawatan --
            "interv_manajemen_jalan_nafas", "interv_suctioning_jalan_nafas", "interv_manajemen_asam_basa",
            "interv_manajemen_syok",
            "interv_manajemen_nyeri", "interv_manajemen_cairan", "interv_monitoring_cairan",
            "interv_manajemen_lingkungan",
            "interv_pendidikan_kesehatan", "interv_fever_treatment", "interv_manajemen_sensasi_perifer",
            "interv_monitoring_kardiovaskuler",
            "interv_pencegahan_penekanan_luka", "interv_lainnya",

            // -- IV. Kardiopulmonal --
            "kardio_pulse_oximetri", "kardio_intubasi", "kardio_rjp", "kardio_rjp_ket", "kardio_kardioversi_dc_shock",

            // -- V. Tindakan Penunjang --
            "penunjang_cek_lab", "penunjang_cek_lab_ket", "penunjang_rontgen", "penunjang_rontgen_ket",
            "penunjang_ct_scan", "penunjang_ct_scan_ket",

            // -- VI. Tindakan Kolaborasi --
            "kolab_o2", "kolab_o2_ket", "kolab_ekg_monitor", "kolab_nebulizer", "kolab_pasang_infus",
            "kolab_pasang_infus_ket",
            "kolab_injeksi_syringe_pump", "kolab_transfusi_darah", "kolab_transfusi_darah_ket", "kolab_ngt",
            "kolab_ngt_ukuran",
            "kolab_bilas_lambung", "kolab_bilas_lambung_ket", "kolab_dower_catheter", "kolab_dower_catheter_ukuran",
            "kolab_restrain_pengaman",

            // -- VII. Trauma / Bedah --
            "trauma_cervical_collar", "trauma_immobilisasi_tulang_belakang", "trauma_gips", "trauma_perawatan_luka",
            "trauma_jahit_luka", "trauma_balutan_spalk", "trauma_selang_dada_wsd", "trauma_lainnya",

            // -- VIII. THT / Mata --
            "tht_irigasi_mata_telinga", "tht_ambil_korpus_alienum",

            // -- IX. Tindakan Obgyn --
            "obgyn_membantu_partus", "obgyn_periksa_ginekologi", "obgyn_doppler_ctg_usg", "obgyn_pasang_tampon",

            // -- Kondisi Pasien Saat Pindah/Pulang --
            "kp_pukul", "kp_gcs_e", "kp_gcs_v", "kp_gcs_m", "kp_td", "kp_nadi", "kp_nadi_reguler", "kp_suhu",
            "kp_pernafasan", "kp_spo",
            "kp_masuk_rs", "kp_ruang", "kp_petugas_penerima", "kp_pendamping", "kp_tindakan_lanjutan",
            "kp_tindakan_lanjutan_ket",
            "kp_dirujuk", "kp_dirujuk_ket", "kp_dirujuk_alasan", "kp_dipulangkan", "kp_dipulangkan_pukul",
            "kp_meninggal", "kp_meninggal_pukul", "kp_doa",
            "kp_edukasi_makan_obat", "kp_edukasi_luka", "kp_edukasi_diet", "kp_edukasi_diet_ket", "kp_edukasi_lain",
            "kp_edukasi_lain_ket",
            "kp_kd_dokter", "kp_kd_perawat"
    };

    /**
     * [RM 03-4/05/06] Nilai dari semua widget aktif, urutan HARUS sinkron
     * dengan KOLOM_KEP di atas. Dipakai bersama oleh ganti() dan simpan().
     */
    private String[] nilaiKep() {
        if (txtPenunjangLab != null) {
            String labTeks = txtPenunjangLab.getText().trim();
            TPenunjangCekLabKet.setText(labTeks);
            CekPenunjangCekLab.setSelected(!labTeks.isEmpty());
        }
        if (txtPenunjangRad != null) {
            String radTeks = txtPenunjangRad.getText().trim();
            TPenunjangRontgenKet.setText(radTeks);
            CekPenunjangRontgen.setSelected(!radTeks.isEmpty());
            if (radTeks.toUpperCase().matches(".*\\b(CT|SCAN|MSCT)\\b.*")) {
                CekPenunjangCtScan.setSelected(true);
                TPenunjangCtScanKet.setText(radTeks);
            } else {
                CekPenunjangCtScan.setSelected(false);
                TPenunjangCtScanKet.setText("");
            }
        }
        boolean psiPerilakuAktif = CekPsiMasalahPerilaku != null && CekPsiMasalahPerilaku.isSelected();
        String masalahPrilaku = (psiPerilakuAktif && RbMasalahPrilakuYa.isSelected()) ? "Ya" : "Tidak";
        String masalahPrilakuSebutkan = (psiPerilakuAktif && RbMasalahPrilakuYa.isSelected())
                ? TMasalahPrilakuSebutkan.getText() : "";
        String statusNikah = RbNikahBelumMenikah.isSelected() ? "Belum Menikah"
                : (RbNikahMenikah.isSelected() ? "Menikah"
                        : (RbNikahCerai.isSelected() ? "Cerai" : (RbNikahLainnya.isSelected() ? "Lainnya" : null)));
        String ketStatusNikah = RbNikahLainnya.isSelected() ? TKetStatusPernikahan.getText() : "";
        String tempatTinggal = RbTinggalSendiri.isSelected() ? "Rumah Sendiri"
                : (RbTinggalKeluarga.isSelected() ? "Rumah Keluarganya"
                        : (RbTinggalLainnya.isSelected() ? "Lainnya" : null));
        String ketTempatTinggal = RbTinggalLainnya.isSelected() ? TKetTempatTinggal.getText() : "";
        String geri1 = RbGeri1Ya.isSelected() ? "Ya" : (RbGeri1Tidak.isSelected() ? "Tidak" : null);
        String geri2 = RbGeri2Ya.isSelected() ? "Ya" : (RbGeri2Tidak.isSelected() ? "Tidak" : null);
        String geri3 = RbGeri3Ya.isSelected() ? "Ya" : (RbGeri3Tidak.isSelected() ? "Tidak" : null);
        String jatuhA = RbJatuhCaraBerjalanYa.isSelected() ? "Ya"
                : (RbJatuhCaraBerjalanTidak.isSelected() ? "Tidak" : null);
        String jatuhB = RbJatuhMenopangYa.isSelected() ? "Ya" : (RbJatuhMenopangTidak.isSelected() ? "Tidak" : null);
        String jatuhHasil = RbJatuhTidakBeresiko.isSelected() ? "Tidak Beresiko"
                : (RbJatuhBeresiko.isSelected() ? "Beresiko" : null);
        String agamaVal = (CmbAgama != null && CmbAgama.getSelectedIndex() > 0) ? CmbAgama.getSelectedItem().toString() : "";
        boolean isIslam = "Islam".equalsIgnoreCase(agamaVal);
        String wajibIbadah = isIslam ? (RbWajibIbadahBaligh.isSelected() ? "Baligh"
                : (RbWajibIbadahBelumBaligh.isSelected() ? "Belum Baligh"
                        : (RbWajibIbadahHalanganLain.isSelected() ? "Halangan Lain" : null))) : null;
        String ketWajibIbadah = (isIslam && RbWajibIbadahHalanganLain.isSelected()) ? TKetWajibIbadah.getText() : "";
        String thaharoh = isIslam ? (RbThaharohBerwudhu.isSelected() ? "Berwudhu"
                : (RbThaharohTayamum.isSelected() ? "Tayamum" : null)) : null;
        String sholat = isIslam ? (RbSholatBerdiri.isSelected() ? "Berdiri"
                : (RbSholatDuduk.isSelected() ? "Duduk" : (RbSholatBerbaring.isSelected() ? "Berbaring" : null))) : null;
        String nilaiKepercayaan = RbNilaiKepercayaanTidak.isSelected() ? "Tidak"
                : (RbNilaiKepercayaanYa.isSelected() ? "Ya" : null);
        String ketNilai = RbNilaiKepercayaanYa.isSelected() ? TKetNilaiKepercayaan.getText() : "";
        boolean giziDewasaAktif = RbPilihGiziDewasa != null && RbPilihGiziDewasa.isSelected();
        boolean giziAnakAktif = RbPilihGiziAnak != null && RbPilihGiziAnak.isSelected();

        String mstPenurunan = giziDewasaAktif ? (RbMstTidakAda.isSelected() ? "Tidak Ada"
                : (RbMstTidakYakin.isSelected() ? "Tidak Yakin" : (RbMstYa.isSelected() ? "Ya" : null))) : null;
        String mstPenurunanKg = giziDewasaAktif ? kosongKeNull(MstPenurunanBbKg.getSelectedItem().toString()) : null;
        String mstAsupan = giziDewasaAktif ? (RbMstAsupanYa.isSelected() ? "Ya" : (RbMstAsupanTidak.isSelected() ? "Tidak" : null)) : null;
        String mstTotalSkor = giziDewasaAktif ? TMstTotalSkor.getText() : "";
        String mstDiagnosis = giziDewasaAktif ? (RbMstDiagnosisYa.isSelected() ? "Ya"
                : (RbMstDiagnosisTidak.isSelected() ? "Tidak" : null)) : null;
        String mstDiagnosisSebutkan = giziDewasaAktif ? TMstDiagnosisSebutkan.getText() : "";
        String mstKesimpulan = giziDewasaAktif ? (RbMstRisiko.isSelected() ? "Risiko"
                : (RbMstTidakRisiko.isSelected() ? "Tidak Risiko" : null)) : null;

        String giziKurus = giziAnakAktif ? (RbGiziAnakKurusYa.isSelected() ? "Ya" : (RbGiziAnakKurusTidak.isSelected() ? "Tidak" : null)) : null;
        String giziTurunBb = giziAnakAktif ? (RbGiziAnakTurunBbYa.isSelected() ? "Ya"
                : (RbGiziAnakTurunBbTidak.isSelected() ? "Tidak" : null)) : null;
        String giziKondisi = giziAnakAktif ? (RbGiziAnakKondisiYa.isSelected() ? "Ya"
                : (RbGiziAnakKondisiTidak.isSelected() ? "Tidak" : null)) : null;
        String giziRisiko = giziAnakAktif ? (RbGiziAnakRisikoYa.isSelected() ? "Ya"
                : (RbGiziAnakRisikoTidak.isSelected() ? "Tidak" : null)) : null;
        String giziAnakTotalSkor = giziAnakAktif ? TGiziAnakTotalSkor.getText() : "";
        String giziKesimpulan = giziAnakAktif ? (RbGiziAnakRendah.isSelected() ? "Risiko Rendah"
                : (RbGiziAnakSedang.isSelected() ? "Risiko Sedang"
                        : (RbGiziAnakTinggi.isSelected() ? "Risiko Tinggi" : null))) : null;
        String nyeriAda = RbNyeriAdaYa.isSelected() ? "Ya" : (RbNyeriAdaTidak.isSelected() ? "Tidak" : null);
        String radiation = RbRadiationTidak.isSelected() ? "Tidak" : (RbRadiationYa.isSelected() ? "Ya" : null);
        String severity = RbSeverityTidakNyeri.isSelected() ? "Tidak Nyeri"
                : (RbSeverityRingan.isSelected() ? "Ringan"
                        : (RbSeveritySedang.isSelected() ? "Sedang" : (RbSeverityBerat.isSelected() ? "Berat" : null)));
        String time = RbTimeTerusMenerus.isSelected() ? "Terus Menerus"
                : (RbTimeHilangTimbul.isSelected() ? "Hilang Timbul"
                        : (RbTimeKurang30.isSelected() ? "<30 Menit"
                                : (RbTimeLebih30.isSelected() ? ">30 Menit" : null)));
        String kpNadiReguler = RbKpNadiReguler.isSelected() ? "Reguler"
                : (RbKpNadiIreguler.isSelected() ? "Ireguler" : null);
        String kpPendamping = RbKpPendampingDokter.isSelected() ? "Dokter"
                : (RbKpPendampingPerawat.isSelected() ? "Perawat" : null);
        String kpTl = RbKpTlKamarOperasi.isSelected() ? "Kamar Operasi"
                : (RbKpTlKamarBersalin.isSelected() ? "Kamar Bersalin"
                        : (RbKpTlHcu.isSelected() ? "HCU"
                                : (RbKpTlKamarJenazah.isSelected() ? "Kamar Jenazah"
                                        : (RbKpTlLainnya.isSelected() ? "Lainnya" : null))));
        String kpDipulangkanPukul = CekKpDipulangkan.isSelected() ? DTPKpDipulangkanPukul.getTeksSql() : null;
        String kpMeninggalPukul = CekKpMeninggal.isSelected() ? DTPKpMeninggalPukul.getTeksSql() : null;

        if ("Hamil".equals(StatusKehamilan.getSelectedItem())) {
            if (TglHPHT != null && TglHPHT.getSelectedItem() != null) {
                HPHT.setText(Valid.SetTgl(TglHPHT.getSelectedItem() + ""));
            }
        } else {
            HPHT.setText("");
        }

        return new String[] {
                getTanggalDanJamAsuhan(),
                Informasi.getSelectedItem().toString(), KeluhanUtama.getText(), RPD.getText(), RPO.getText(),
                StatusKehamilan.getSelectedItem().toString(),
                Gravida.getText(), Para.getText(), Abortus.getText(), HPHT.getText(),
                Tekanan.getSelectedItem().toString(), Pupil.getSelectedItem().toString(),
                Neurosensorik.getSelectedItem().toString(), Integumen.getSelectedItem().toString(),
                Turgor.getSelectedItem().toString(), Edema.getSelectedItem().toString(),
                Mukosa.getSelectedItem().toString(), Perdarahan.getSelectedItem().toString(),
                JumlahPerdarahan.getText(), WarnaPerdarahan.getText(),
                Intoksikasi.getSelectedItem().toString(), BAB.getText(), XBAB.getText(), KBAB.getText(), WBAB.getText(),
                BAK.getText(), XBAK.getText(), WBAK.getText(), LBAK.getText(),
                Hubungan.getSelectedItem().toString(),
                (RbBudayaAda != null && RbBudayaAda.isSelected()) ? "Ada" : "Tidak Ada",
                (RbBudayaAda != null && RbBudayaAda.isSelected()) ? KetBudaya.getText() : "",
                PendidikanPJ.getSelectedItem().toString(),
                KetPendidikanPJ.getText(), Edukasi.getSelectedItem().toString(), KetEdukasi.getText(),
                ADL.getSelectedItem().toString(), Aktifitas.getSelectedItem().toString(),
                AlatBantu.getSelectedItem().toString(), KetAlatBantu.getText(),
                Lokasi.getText(), SkalaNyeri.getSelectedItem().toString(), NyeriHilang.getSelectedItem().toString(),
                KetNyeri.getText(),
                PadaDokter.getSelectedItem().toString(), KetDokter.getText(), Lapor.getSelectedItem().toString(),
                KetLapor.getText(), Rencana.getText(), KdPetugas.getText(),

                RbAlergiYa.isSelected() ? "Ya" : "Tidak", TAlergiSebutkan.getText(),

                TdKep.getText(), NadiKep.getText(), SuhuKep.getText(), SpoKep.getText(), RrKep.getText(),
                BbKep.getText(), TbKep.getText(),

                ya(CekPsiTenang), ya(CekPsiCemas), ya(CekPsiTakut), ya(CekPsiSedih), ya(CekPsiMarah),
                ya(CekPsiBunuhDiri), masalahPrilaku, masalahPrilakuSebutkan,

                statusNikah, ketStatusNikah, tempatTinggal, ketTempatTinggal,
                TPekerjaanKep.getText(), TPendidikanPasienKep.getText(),

                geri1, geri2, geri3,

                jatuhA, jatuhB, jatuhHasil,

                wajibIbadah, ketWajibIbadah, thaharoh, sholat, nilaiKepercayaan,
                ketNilai,
                agamaVal, ya(CekCacatTidak), ya(CekCacatTunaNetra), ya(CekCacatTunaDaksa),
                ya(CekCacatTunaRungu), ya(CekCacatTunaWicara),

                mstPenurunan, mstPenurunanKg, mstAsupan,
                mstTotalSkor, mstDiagnosis, mstDiagnosisSebutkan, mstKesimpulan,

                giziKurus, giziTurunBb, giziKondisi, giziRisiko, giziAnakTotalSkor, giziKesimpulan,

                nyeriAda, (CmbKategoriUsiaNyeri != null && CmbKategoriUsiaNyeri.getSelectedItem() != null ? CmbKategoriUsiaNyeri.getSelectedItem().toString() : ""),

                getNilaiDbFlacc(FlaccWajah), getNilaiDbFlacc(FlaccKaki), getNilaiDbFlacc(FlaccAktivitas),
                getNilaiDbFlacc(FlaccMenangis), getNilaiDbFlacc(FlaccBersuara), TFlaccTotal.getText(),

                ya(CekProvCahaya), ya(CekProvGelap), ya(CekProvGerakan), ya(CekProvBerbaring),
                ya(CekQualTumpul), ya(CekQualDitusuk), ya(CekQualBerdenyut), ya(CekQualTajam), ya(CekQualPanas),
                radiation, TRadiationKe.getText(), severity, time,

                ya(CekIntervManajemenJalanNafas), ya(CekIntervSuctioningJalanNafas), ya(CekIntervManajemenAsamBasa),
                ya(CekIntervManajemenSyok),
                ya(CekIntervManajemenNyeri), ya(CekIntervManajemenCairan), ya(CekIntervMonitoringCairan),
                ya(CekIntervManajemenLingkungan),
                ya(CekIntervPendidikanKesehatan), ya(CekIntervFeverTreatment), ya(CekIntervManajemenSensasiPerifer),
                ya(CekIntervMonitoringKardiovaskuler),
                ya(CekIntervPencegahanPenekananLuka), TIntervLainnya.getText(),

                ya(CekKardioPulseOximetri), ya(CekKardioIntubasi), ya(CekKardioRjp), TKardioRjpKet.getText(),
                ya(CekKardioKardioversiDcShock),

                ya(CekPenunjangCekLab), TPenunjangCekLabKet.getText(), ya(CekPenunjangRontgen),
                TPenunjangRontgenKet.getText(), ya(CekPenunjangCtScan), TPenunjangCtScanKet.getText(),

                ya(CekKolabO2), TKolabO2Ket.getText(), ya(CekKolabEkgMonitor), ya(CekKolabNebulizer),
                ya(CekKolabPasangInfus), TKolabPasangInfusKet.getText(),
                ya(CekKolabInjeksiSyringePump), ya(CekKolabTransfusiDarah), TKolabTransfusiDarahKet.getText(),
                ya(CekKolabNgt), TKolabNgtUkuran.getText(),
                ya(CekKolabBilasLambung), TKolabBilasLambungKet.getText(), ya(CekKolabDowerCatheter),
                TKolabDowerCatheterUkuran.getText(), ya(CekKolabRestrainPengaman),

                ya(CekTraumaCervicalCollar), ya(CekTraumaImmobilisasiTulangBelakang), ya(CekTraumaGips),
                ya(CekTraumaPerawatanLuka),
                ya(CekTraumaJahitLuka), ya(CekTraumaBalutanSpalk), ya(CekTraumaSelangDadaWsd), TTraumaLainnya.getText(),

                ya(CekThtIrigasiMataTelinga), ya(CekThtAmbilKorpusAlienum),

                ya(CekObgynMembantuPartus), ya(CekObgynPeriksaGinekologi), ya(CekObgynDopplerCtgUsg),
                ya(CekObgynPasangTampon),

                (DTPKpPukulSaja != null)
                        ? (((TglAsuhan != null && TglAsuhan.getSelectedItem() != null) ? Valid.SetTgl(TglAsuhan.getSelectedItem() + "") : new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()))
                                + " " + DTPKpPukulSaja.getJamSql())
                        : ((CekKpMasukRs.isSelected() || CekKpDipulangkan.isSelected() || CekKpMeninggal.isSelected()
                                || CekKpDoa.isSelected() || CekKpDirujuk.isSelected()) ? DTPKpPukul.getTeksSql() : null),
                potong(TKpGcsE.getText(), 25), potong(TKpGcsV.getText(), 25), potong(TKpGcsM.getText(), 25),
                potong(TKpTd.getText(), 20), potong(TKpNadi.getText(), 15),
                kpNadiReguler, potong(TKpSuhu.getText(), 15), potong(TKpPernafasan.getText(), 15), potong(TKpSpo.getText(), 15),
                ya(CekKpMasukRs), TKpRuang.getText(), TKpPetugasPenerima.getText(), kpPendamping, kpTl,
                TKpTindakanLanjutanKet.getText(),
                ya(CekKpDirujuk), TKpDirujukKet.getText(), TKpDirujukAlasan.getText(), ya(CekKpDipulangkan),
                kpDipulangkanPukul, ya(CekKpMeninggal), kpMeninggalPukul, ya(CekKpDoa),
                ya(CekKpEdukasiMakanObat), ya(CekKpEdukasiLuka), ya(CekKpEdukasiDiet), TKpEdukasiDietKet.getText(),
                ya(CekKpEdukasiLain), TKpEdukasiLainKet.getText(),
                KdDokterKp.getText(), KdPetugas.getText()
        };
    }

    private void ganti() {
        StringBuilder set = new StringBuilder();
        for (String kolom : KOLOM_KEP) {
            set.append(kolom).append("=?,");
        }
        set.setLength(set.length() - 1);

        String[] nilai = nilaiKep();
        String[] param = new String[nilai.length + 1];
        System.arraycopy(nilai, 0, param, 0, nilai.length);
        param[nilai.length] = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();

        if (Sequel.mengedittf("penilaian_awal_keperawatan_igd", "no_rawat=?", set.toString(), param.length,
                param) == true) {
            tbObat.setValueAt(TNoRw.getText(), tbObat.getSelectedRow(), 0);
            tbObat.setValueAt(TNoRM.getText(), tbObat.getSelectedRow(), 1);
            tbObat.setValueAt(TPasien.getText(), tbObat.getSelectedRow(), 2);
            tbObat.setValueAt(TglLahir.getText(), tbObat.getSelectedRow(), 3);
            tbObat.setValueAt(getTanggalDanJamAsuhan(), tbObat.getSelectedRow(), 4);
            tbObat.setValueAt(KdPetugas.getText(), tbObat.getSelectedRow(), 5);
            tbObat.setValueAt(NmPetugas.getText(), tbObat.getSelectedRow(), 6);

            Sequel.meghapus("penilaian_awal_keperawatan_igd_masalah", "no_rawat", TNoRw.getText());
            for (i = 0; i < tbMasalahKeperawatan.getRowCount(); i++) {
                if (tbMasalahKeperawatan.getValueAt(i, 0).toString().equals("true")) {
                    Sequel.menyimpantf2("penilaian_awal_keperawatan_igd_masalah", "?,?", 2,
                            new String[] { TNoRw.getText(), tbMasalahKeperawatan.getValueAt(i, 1).toString() });
                }
            }
            emptTeks();
            TabRawat.setSelectedIndex(1);
        }
    }

    private void hapus() {
        if (Sequel.queryu2tf("delete from penilaian_awal_keperawatan_igd where no_rawat=?", 1, new String[] {
                tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
        }) == true) {
            TNoRM1.setText("");
            TPasien1.setText("");
            Valid.tabelKosong(tabModeDetailMasalah);
            Valid.tabelKosong(tabModeDetailRencana);
            ChkAccor.setSelected(false);
            isMenu();
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText("" + tabMode.getRowCount());
            emptTeks();
        } else {
            JOptionPane.showMessageDialog(null, "Gagal menghapus..!!");
        }
    }

    /**
     * [RM 03-4/05/06] INSERT manual bernama kolom (bukan Sequel.menyimpantf,
     * yang posisional murni mengikuti urutan fisik SEMUA kolom tabel -
     * tidak cocok utk tabel ini dgn ~224 kolom). Pola sama seperti isRawat()
     * di file ini & simpan() RM 03-2: koneksi.prepareStatement() langsung.
     * Kolom lama yang dipensiunkan tidak disebut - otomatis kebagian
     * DEFAULT dari migrasi (KELOMPOK V di sik_penilaian_awal_keperawatan_
     * igd_rm0345.sql).
     */
    private void simpan() {
        StringBuilder kolom = new StringBuilder("no_rawat");
        StringBuilder placeholder = new StringBuilder("?");
        for (String k : KOLOM_KEP) {
            kolom.append(",").append(k);
            placeholder.append(",?");
        }

        String[] nilai = nilaiKep();
        String[] param = new String[nilai.length + 1];
        param[0] = TNoRw.getText();
        System.arraycopy(nilai, 0, param, 1, nilai.length);

        boolean sukses = false;
        try {
            PreparedStatement psSimpan = koneksi.prepareStatement(
                    "insert into penilaian_awal_keperawatan_igd (" + kolom + ") values (" + placeholder + ")");
            try {
                for (int j = 0; j < param.length; j++) {
                    psSimpan.setString(j + 1, param[j]);
                }
                psSimpan.executeUpdate();
                sukses = true;
            } finally {
                if (psSimpan != null) {
                    psSimpan.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Simpan RM 03-4/05/06 : " + e);
            JOptionPane.showMessageDialog(null,
                    "Maaf, gagal menyimpan data. Kemungkinan No.Rawat yang sama sudah dimasukkan sebelumnya...!");
        }

        if (sukses) {
            tabMode.addRow(new Object[] {
                    TNoRw.getText(), TNoRM.getText(), TPasien.getText(), TglLahir.getText(),
                    getTanggalDanJamAsuhan(),
                    KdPetugas.getText(), NmPetugas.getText()
            });
            LCount.setText("" + tabMode.getRowCount());
            for (i = 0; i < tbMasalahKeperawatan.getRowCount(); i++) {
                if (tbMasalahKeperawatan.getValueAt(i, 0).toString().equals("true")) {
                    Sequel.menyimpantf2("penilaian_awal_keperawatan_igd_masalah", "?,?", 2,
                            new String[] { TNoRw.getText(), tbMasalahKeperawatan.getValueAt(i, 1).toString() });
                }
            }
            emptTeks();
        }
    }

    private void LoadRencana() {
        tampilRencana();
        tampilRencana2();
    }

    private void runBackground(Runnable task) {
        if (ceksukses)
            return;
        if (executor.isShutdown() || executor.isTerminated())
            return;
        if (!isDisplayable())
            return;

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
        if (timerJam != null) {
            timerJam.stop();
        }
        if (DTPKpPukul != null) {
            DTPKpPukul.stopTimer();
        }
        if (DTPKpDipulangkanPukul != null) {
            DTPKpDipulangkanPukul.stopTimer();
        }
        if (DTPKpMeninggalPukul != null) {
            DTPKpMeninggalPukul.stopTimer();
        }
        if (DTPObatPukul != null) {
            DTPObatPukul.stopTimer();
        }
        if (DTPKpPukulSaja != null) {
            DTPKpPukulSaja.stopTimer();
        }
        executor.shutdownNow();
        super.dispose();
    }
}
