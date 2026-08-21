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
    private final DefaultTableModel tabMode,tabModeMasalah,tabModeDetailMasalah,tabModeRencana,tabModeDetailRencana;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,ps2;
    private ResultSet rs,rs2;
    private int i=0,jml=0,index=0;
    private DlgCariPetugas petugas;
    private MasterMasalahKeperawatanIGD masalahkeperawatan;
    private MasterRencanaKeperawatanIGD rencanakeperawatan;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private boolean[] pilih; 
    private String[] kode,masalah;
    private String masalahkeperawatanigd="",finger=""; 
    private File file;
    private FileWriter fileWriter;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private FileReader myObj;
    private String TANGGALMUNDUR="yes";
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public RMPenilaianAwalKeperawatanIGD(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        ModernStyle.terapkan(this);
        initKepTambahan();
        initKepTambahanHal2();
        initKepTambahanHal3();
        initObatInfusTambahan();
        initKepTambahanHal4();

        // Header + width setup di bawah dulunya dideklarasikan 81 kolom
        // (sisa desain lama sebelum tampil() di-refactor jadi SELECT
        // ringkas) - dirapikan jadi 7 kolom yg persis sesuai yg diisi
        // tampil() (no_rawat,no_rkm_medis,nm_pasien,tgl_lahir,tanggal,
        // nip,nama), pola sama seperti tabMode di Triase.
        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat","No.RM","Nama Pasien","Tgl.Lahir","Tanggal","NIP","Nama Petugas"
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 7; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(160);
            }else if(i==3){
                column.setPreferredWidth(65);
            }else if(i==4){
                column.setPreferredWidth(120);
            }else if(i==5){
                column.setPreferredWidth(90);
            }else if(i==6){
                column.setPreferredWidth(150);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeMasalah=new DefaultTableModel(null,new Object[]{
                "P","KODE","MASALAH KEPERAWATAN"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Double.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbMasalahKeperawatan.setModel(tabModeMasalah);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbMasalahKeperawatan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbMasalahKeperawatan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 3; i++) {
            TableColumn column = tbMasalahKeperawatan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==2){
                column.setPreferredWidth(350);
            }
        }
        tbMasalahKeperawatan.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeDetailMasalah=new DefaultTableModel(null,new Object[]{
                "Kode","Masalah Keperawatan"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbMasalahDetailMasalah.setModel(tabModeDetailMasalah);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbMasalahDetailMasalah.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbMasalahDetailMasalah.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 2; i++) {
            TableColumn column = tbMasalahDetailMasalah.getColumnModel().getColumn(i);
            if(i==0){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==1){
                column.setPreferredWidth(420);
            }
        }
        tbMasalahDetailMasalah.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeRencana=new DefaultTableModel(null,new Object[]{
                "P","KODE","RENCANA KEPERAWATAN"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Double.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbRencanaKeperawatan.setModel(tabModeRencana);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbRencanaKeperawatan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbRencanaKeperawatan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 3; i++) {
            TableColumn column = tbRencanaKeperawatan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==2){
                column.setPreferredWidth(350);
            }
        }
        tbRencanaKeperawatan.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeDetailRencana=new DefaultTableModel(null,new Object[]{
                "Kode","Rencana Keperawatan"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbRencanaDetail.setModel(tabModeDetailRencana);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbRencanaDetail.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbRencanaDetail.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 2; i++) {
            TableColumn column = tbRencanaDetail.getColumnModel().getColumn(i);
            if(i==0){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==1){
                column.setPreferredWidth(420);
            }
        }
        tbRencanaDetail.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        KeluhanUtama.setDocument(new batasInput((int)150).getKata(KeluhanUtama));
        RPD.setDocument(new batasInput((int)100).getKata(RPD));
        RPO.setDocument(new batasInput((int)100).getKata(RPO));
        Gravida.setDocument(new batasInput((byte)20).getKata(Gravida));
        Para.setDocument(new batasInput((byte)20).getKata(Para));
        Abortus.setDocument(new batasInput((byte)20).getKata(Abortus));
        HPHT.setDocument(new batasInput((byte)20).getKata(HPHT));
        JumlahPerdarahan.setDocument(new batasInput((byte)5).getKata(JumlahPerdarahan));
        WarnaPerdarahan.setDocument(new batasInput((int)40).getKata(WarnaPerdarahan));
        BAB.setDocument(new batasInput((byte)2).getKata(BAB));
        XBAB.setDocument(new batasInput((byte)10).getKata(XBAB));
        KBAB.setDocument(new batasInput((int)40).getKata(KBAB));
        WBAB.setDocument(new batasInput((int)40).getKata(WBAB));
        BAK.setDocument(new batasInput((byte)2).getKata(BAK));
        XBAK.setDocument(new batasInput((byte)10).getKata(XBAK));
        WBAK.setDocument(new batasInput((int)40).getKata(WBAK));
        LBAK.setDocument(new batasInput((int)40).getKata(LBAK));
        Dilaporkan.setDocument(new batasInput((int)50).getKata(Dilaporkan));
        Sebutkan.setDocument(new batasInput((int)50).getKata(Sebutkan));
        KetTinggal.setDocument(new batasInput((int)50).getKata(KetTinggal));
        KetBudaya.setDocument(new batasInput((int)50).getKata(KetBudaya));
        KetPendidikanPJ.setDocument(new batasInput((int)50).getKata(KetPendidikanPJ));
        KetEdukasi.setDocument(new batasInput((int)50).getKata(KetEdukasi));
        KetAlatBantu.setDocument(new batasInput((int)50).getKata(KetAlatBantu));
        KetProvokes.setDocument(new batasInput((int)40).getKata(KetProvokes));
        KetQuality.setDocument(new batasInput((int)50).getKata(KetQuality));
        Lokasi.setDocument(new batasInput((int)50).getKata(Lokasi));
        Durasi.setDocument(new batasInput((int)25).getKata(Durasi));
        KetNyeri.setDocument(new batasInput((int)40).getKata(KetNyeri));
        KetDokter.setDocument(new batasInput((byte)15).getKata(KetDokter));
        KetLapor.setDocument(new batasInput((int)15).getKata(KetLapor));
        Rencana.setDocument(new batasInput((int)200).getKata(Rencana));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
        
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
        );
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);
        
        try {
            TANGGALMUNDUR=koneksiDB.TANGGALMUNDUR();
        } catch (Exception e) {
            TANGGALMUNDUR="yes";
        }
        
        ChkAccor.setSelected(false);
        isMenu();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pengkajian Awal Keperawatan IGD ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
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
        jLabel52.setText("V. SKALA NYERI");
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

        ADL.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mandiri", "Bantuan Minimal", "Bantuan Sebagian", "Ketergantungan Total" }));
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

        TinggalDengan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sendiri", "Orang Tua", "Suami / Istri", "Lainnya" }));
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

        Hasil.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak beresiko (tidak ditemukan a dan b)", "Resiko rendah (ditemukan a/b)", "Resiko tinggi (ditemukan a dan b)" }));
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

        jLabel70.setText("b. Menopang saat akan duduk, tampak memegang pinggiran kursi atau meja / benda lain sebagai penopang :");
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

        Nyeri.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada Nyeri", "Nyeri Akut", "Nyeri Kronis" }));
        Nyeri.setName("Nyeri"); // NOI18N
        Nyeri.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NyeriKeyPressed(evt);
            }
        });
        FormInput.add(Nyeri);
        Nyeri.setBounds(375, 700, 130, 23);

        Provokes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Proses Penyakit", "Benturan", "Lain-lain" }));
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

        Quality.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Seperti Tertusuk", "Berdenyut", "Teriris", "Tertindih", "Tertiban", "Lain-lain" }));
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

        SkalaNyeri.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
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

        NyeriHilang.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Istirahat", "Medengar Musik", "Minum Obat" }));
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
        jLabel94.setText("III. RIWAYAT PSIKOLOGIS - SOSIAL - EKONOMI - BUDAYA - SPIRITUAL");
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
        jLabel56.setText("IV. PENGKAJIAN FUNGSI");
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
        jLabel63.setText("VI. PENGKAJIAN RESIKO JATUH (GET UP AND GO)");
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
        jLabel98.setText("I. RIWAYAT KESEHATAN PASIEN");
        jLabel98.setName("jLabel98"); // NOI18N
        FormInput.add(jLabel98);
        jLabel98.setBounds(10, 70, 180, 23);

        jLabel90.setText("Tekanan Intrakranial :");
        jLabel90.setName("jLabel90"); // NOI18N
        FormInput.add(jLabel90);
        jLabel90.setBounds(0, 230, 150, 23);

        Tekanan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TAK", "Sakit Kepala", "Muntah", "Pusing", "Bingung" }));
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

        Neurosensorik.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TAK", "Spasme Otot", "Perubahan Sensorik", "Perubahan Motorik", "Perubahan Bentuk Ekstremitas", "Penurunan Tingkat Kesadaran", "Fraktur/Dislokasi", "Luksasio", "Kerusakan Jaringan/Luka" }));
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

        Integumen.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TAK", "Luka Bakar", "Luka Robek", "Lecet", "Luka Decubitus", "Luka Gangren" }));
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

        Edema.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Ekstremitas", "Seluruh Tubuh", "Asites", "Palpebrae" }));
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

        Intoksikasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Ada", "Gigitan Binatang", "Zat Kimia", "Gas", "Obat" }));
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
        jLabel116.setText("II. PEMERIKSAAN FISIK");
        jLabel116.setName("jLabel116"); // NOI18N
        FormInput.add(jLabel116);
        jLabel116.setBounds(10, 210, 180, 23);

        jLabel117.setText("a. Kondisi Psikologis :");
        jLabel117.setName("jLabel117"); // NOI18N
        FormInput.add(jLabel117);
        jLabel117.setBounds(0, 420, 148, 23);

        Psikologis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada Masalah", "Marah", "Takut", "Depresi", "Cepat Lelah", "Cemas", "Gelisah", "Lain-lain" }));
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

        Perilaku.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Perilaku Kekerasan", "Gangguan Efek", "Gangguan Memori", "Halusinasi", "Kecenderungan Percobaan Bunuh Diri", "Lainnya", "-" }));
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

        Hubungan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Harmonis", "Kurang Harmonis", "Tidak Harmonis", "Konflik Besar" }));
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

        PendidikanPJ.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "TS", "TK", "SD", "SMP", "SMA", "SLTA/SEDERAJAT", "D1", "D2", "D3", "D4", "S1", "S2", "S3" }));
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
        jLabel19.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "02-05-2024" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "02-05-2024" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(195, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Alt+3");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
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

        scrollPane6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)), "Rencana Keperawatan Lainnya :", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
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

    private widget.PanelBiasa panelChecklistKep(int kolom, widget.CekBox... cek){
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new java.awt.GridLayout(0,kolom,10,2));
        for(widget.CekBox c : cek){ p.add(c); }
        return p;
    }

    // Pengganti Valid.SetTgl2(JDateTimePicker,String) khusus utk widget.Jam
    // (bukan JDateTimePicker) - logic parse persis sama dgn validasi.java,
    // TIDAK mengubah fungsi.validasi.java krn dipakai file lain juga.
    private void setJamDariString(widget.Jam jam, String tglJam){
        try {
            jam.setJam(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(tglJam.replaceAll("'","")));
        } catch (java.text.ParseException ex) {
            jam.setJam(new Date());
        }
    }

    private java.awt.GridBagConstraints kepConstraint(int gridx,int gridy){
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = gridx; c.gridy = gridy;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.insets = new java.awt.Insets(3,5,3,5);
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        return c;
    }

    private int tambahBarisKepJudul(widget.PanelBiasa panel,int row,String judul){
        widget.Label lbl = new widget.Label();
        lbl.setText(judul);
        lbl.setFont(lbl.getFont().deriveFont(java.awt.Font.BOLD));
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        java.awt.GridBagConstraints c = kepConstraint(0,row);
        c.gridwidth = 2;
        panel.add(lbl,c);
        return row+1;
    }

    private int tambahBarisKepField(widget.PanelBiasa panel,int row,String label,java.awt.Component field){
        widget.Label lbl = new widget.Label();
        // Label kosong ("") dipakai di banyak baris (checkbox/panel yg
        // teksnya sudah ada di komponennya sendiri) - kalau selalu
        // ditambah " :" hasilnya tanda ":" nyangkut sendirian tanpa teks
        // di depannya. Cuma tambahkan " :" kalau label-nya memang ada isi.
        lbl.setText(label.isEmpty() ? "" : label+" :");
        java.awt.GridBagConstraints c1 = kepConstraint(0,row);
        panel.add(lbl,c1);
        // fill=NONE/anchor=WEST (bukan HORIZONTAL) - field/panel FlowLayout
        // duduk pas di lebar kontennya sendiri, rapat ke label, TIDAK
        // melebar ikut lebar kolom 1 (yg ditentukan baris lain yg lebih
        // lebar) - permintaan user supaya tidak ada "ruang kosong" di
        // dalam field/dropdown yg sebenarnya sempit.
        java.awt.GridBagConstraints c2 = kepConstraint(1,row);
        c2.anchor = java.awt.GridBagConstraints.WEST;
        c2.fill = java.awt.GridBagConstraints.NONE;
        c2.weightx = 0;
        panel.add(field,c2);
        return row+1;
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
    private void initKepTambahan(){
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
        label13.setVisible(false); TCariRencana.setVisible(false);
        BtnCariRencana.setVisible(false); BtnAllRencana.setVisible(false); BtnTambahRencana.setVisible(false);

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
        widget.Label lblKpDokter = new widget.Label(); lblKpDokter.setText("Dokter :");
        lblKpDokter.setPreferredSize(new java.awt.Dimension(70,23));
        FormInput.add(lblKpDokter); lblKpDokter.setBounds(0, 70, 70, 23);
        KdDokterKp = new widget.TextBox(); KdDokterKp.setPreferredSize(new java.awt.Dimension(100,23)); KdDokterKp.setEditable(false);
        FormInput.add(KdDokterKp); KdDokterKp.setBounds(74, 70, 100, 23);
        NmDokterKp = new widget.TextBox(); NmDokterKp.setPreferredSize(new java.awt.Dimension(180,23)); NmDokterKp.setEditable(false);
        FormInput.add(NmDokterKp); NmDokterKp.setBounds(176, 70, 180, 23);
        BtnDokterKp = new widget.Button();
        BtnDokterKp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnDokterKp.setPreferredSize(new java.awt.Dimension(28,23));
        BtnDokterKp.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariDokter dlg = new DlgCariDokter(null,false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if(dlg.getTable().getSelectedRow()!= -1){
                        KdDokterKp.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),0).toString());
                        NmDokterKp.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),1).toString());
                    }
                    BtnDokterKp.requestFocus();
                }
            });
            dlg.isCek();
            dlg.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        FormInput.add(BtnDokterKp); BtnDokterKp.setBounds(358, 70, 28, 23);

        panelKepAtas = new widget.PanelBiasa();
        panelKepAtas.setLayout(new java.awt.GridBagLayout());
        int row = 0;

        // -- Riwayat Alergi --
        row = tambahBarisKepJudul(panelKepAtas, row, "RIWAYAT ALERGI");
        RbAlergiTidak = new widget.RadioButton(); RbAlergiTidak.setText("Tidak");
        RbAlergiYa = new widget.RadioButton(); RbAlergiYa.setText("Ya, Sebutkan");
        bgAlergi = new javax.swing.ButtonGroup();
        bgAlergi.add(RbAlergiTidak); bgAlergi.add(RbAlergiYa);
        TAlergiSebutkan = new widget.TextBox(); TAlergiSebutkan.setPreferredSize(new java.awt.Dimension(250,23));
        widget.PanelBiasa panelAlergi = new widget.PanelBiasa();
        panelAlergi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelAlergi.add(RbAlergiTidak); panelAlergi.add(RbAlergiYa); panelAlergi.add(TAlergiSebutkan);
        row = tambahBarisKepField(panelKepAtas, row, "Riwayat Alergi", panelAlergi);

        // -- Keadaan Umum (Tanda Vital) --
        // Ditata 2 kolom DALAM PANEL TERSENDIRI (panelKeadaanUmum, GridBag
        // sendiri) - bukan langsung di kolom panelKepAtas seperti versi
        // lama. Kalau langsung di kolom panelKepAtas, gridx=1 dipakai
        // BARENG section lain (Riwayat Alergi/Status Kehamilan dst yg
        // butuh ~550px), jadi kolomnya ikut melebar & jarak Tekanan
        // Darah->Suhu jadi jauh walau field-nya sendiri sudah 100px.
        // Dibungkus panel sendiri, kolomnya cuma dihitung dari isinya
        // sendiri (Td|Suhu, Spo|Nadi, Bb|Rr, Tb|kosong) - rapat.
        row = tambahBarisKepJudul(panelKepAtas, row, "KEADAAN UMUM");
        TdKep = new widget.TextBox(); TdKep.setPreferredSize(new java.awt.Dimension(100,26));
        SuhuKep = new widget.TextBox(); SuhuKep.setPreferredSize(new java.awt.Dimension(100,26));
        SpoKep = new widget.TextBox(); SpoKep.setPreferredSize(new java.awt.Dimension(100,26));
        NadiKep = new widget.TextBox(); NadiKep.setPreferredSize(new java.awt.Dimension(100,26));
        BbKep = new widget.TextBox(); BbKep.setPreferredSize(new java.awt.Dimension(100,26));
        RrKep = new widget.TextBox(); RrKep.setPreferredSize(new java.awt.Dimension(100,26));
        TbKep = new widget.TextBox(); TbKep.setPreferredSize(new java.awt.Dimension(100,26));

        widget.PanelBiasa panelKeadaanUmum = new widget.PanelBiasa();
        panelKeadaanUmum.setLayout(new java.awt.GridBagLayout());
        int rowKU = 0;

        widget.Label lblTdKep = new widget.Label(); lblTdKep.setText("Tekanan Darah (mmHg) :");
        java.awt.GridBagConstraints cLblTdKep = kepConstraint(0,rowKU); cLblTdKep.anchor = java.awt.GridBagConstraints.EAST; cLblTdKep.fill = java.awt.GridBagConstraints.NONE;
        panelKeadaanUmum.add(lblTdKep, cLblTdKep);
        java.awt.GridBagConstraints cTdKep = kepConstraint(1,rowKU); cTdKep.anchor = java.awt.GridBagConstraints.WEST; cTdKep.fill = java.awt.GridBagConstraints.NONE; cTdKep.weightx = 0;
        panelKeadaanUmum.add(TdKep, cTdKep);
        widget.Label lblSuhuKep = new widget.Label(); lblSuhuKep.setText("Suhu (°C) :");
        java.awt.GridBagConstraints cLblSuhuKep = kepConstraint(2,rowKU); cLblSuhuKep.anchor = java.awt.GridBagConstraints.EAST; cLblSuhuKep.fill = java.awt.GridBagConstraints.NONE; cLblSuhuKep.insets = new java.awt.Insets(3,20,3,5);
        panelKeadaanUmum.add(lblSuhuKep, cLblSuhuKep);
        java.awt.GridBagConstraints cSuhuKep = kepConstraint(3,rowKU); cSuhuKep.anchor = java.awt.GridBagConstraints.WEST; cSuhuKep.fill = java.awt.GridBagConstraints.NONE; cSuhuKep.weightx = 0;
        panelKeadaanUmum.add(SuhuKep, cSuhuKep);
        // Tinggi Badan digabung ke baris ke-1 (sejajar Tekanan Darah &
        // Suhu) di gridx=4/5, bukan baris sendiri - supaya total cuma 3
        // baris (permintaan user).
        widget.Label lblTbKep = new widget.Label(); lblTbKep.setText("Tinggi Badan (Cm) :");
        java.awt.GridBagConstraints cLblTbKep = kepConstraint(4,rowKU); cLblTbKep.anchor = java.awt.GridBagConstraints.EAST; cLblTbKep.fill = java.awt.GridBagConstraints.NONE; cLblTbKep.insets = new java.awt.Insets(3,20,3,5);
        panelKeadaanUmum.add(lblTbKep, cLblTbKep);
        java.awt.GridBagConstraints cTbKep = kepConstraint(5,rowKU); cTbKep.anchor = java.awt.GridBagConstraints.WEST; cTbKep.fill = java.awt.GridBagConstraints.NONE; cTbKep.weightx = 0;
        panelKeadaanUmum.add(TbKep, cTbKep);
        rowKU++;

        widget.Label lblSpoKep = new widget.Label(); lblSpoKep.setText("SpO2 (%) :");
        java.awt.GridBagConstraints cLblSpoKep = kepConstraint(0,rowKU); cLblSpoKep.anchor = java.awt.GridBagConstraints.EAST; cLblSpoKep.fill = java.awt.GridBagConstraints.NONE;
        panelKeadaanUmum.add(lblSpoKep, cLblSpoKep);
        java.awt.GridBagConstraints cSpoKep = kepConstraint(1,rowKU); cSpoKep.anchor = java.awt.GridBagConstraints.WEST; cSpoKep.fill = java.awt.GridBagConstraints.NONE; cSpoKep.weightx = 0;
        panelKeadaanUmum.add(SpoKep, cSpoKep);
        widget.Label lblNadiKep = new widget.Label(); lblNadiKep.setText("Frekuensi Nadi (x/menit) :");
        java.awt.GridBagConstraints cLblNadiKep = kepConstraint(2,rowKU); cLblNadiKep.anchor = java.awt.GridBagConstraints.EAST; cLblNadiKep.fill = java.awt.GridBagConstraints.NONE; cLblNadiKep.insets = new java.awt.Insets(3,20,3,5);
        panelKeadaanUmum.add(lblNadiKep, cLblNadiKep);
        java.awt.GridBagConstraints cNadiKep = kepConstraint(3,rowKU); cNadiKep.anchor = java.awt.GridBagConstraints.WEST; cNadiKep.fill = java.awt.GridBagConstraints.NONE; cNadiKep.weightx = 0;
        panelKeadaanUmum.add(NadiKep, cNadiKep);
        rowKU++;

        widget.Label lblBbKep = new widget.Label(); lblBbKep.setText("Berat Badan (Kg) :");
        java.awt.GridBagConstraints cLblBbKep = kepConstraint(0,rowKU); cLblBbKep.anchor = java.awt.GridBagConstraints.EAST; cLblBbKep.fill = java.awt.GridBagConstraints.NONE;
        panelKeadaanUmum.add(lblBbKep, cLblBbKep);
        java.awt.GridBagConstraints cBbKep = kepConstraint(1,rowKU); cBbKep.anchor = java.awt.GridBagConstraints.WEST; cBbKep.fill = java.awt.GridBagConstraints.NONE; cBbKep.weightx = 0;
        panelKeadaanUmum.add(BbKep, cBbKep);
        widget.Label lblRrKep = new widget.Label(); lblRrKep.setText("Frekuensi Nafas (x/menit) :");
        java.awt.GridBagConstraints cLblRrKep = kepConstraint(2,rowKU); cLblRrKep.anchor = java.awt.GridBagConstraints.EAST; cLblRrKep.fill = java.awt.GridBagConstraints.NONE; cLblRrKep.insets = new java.awt.Insets(3,20,3,5);
        panelKeadaanUmum.add(lblRrKep, cLblRrKep);
        java.awt.GridBagConstraints cRrKep = kepConstraint(3,rowKU); cRrKep.anchor = java.awt.GridBagConstraints.WEST; cRrKep.fill = java.awt.GridBagConstraints.NONE; cRrKep.weightx = 0;
        panelKeadaanUmum.add(RrKep, cRrKep);
        rowKU++;

        // Filler kolom paling kanan (gridx=6) menyerap sisa lebar baris
        // supaya kolom KEADAAN UMUM tetap rapat di kiri, bukan field yg
        // melebar. Tidak terlihat (Box glue, 0x0).
        java.awt.GridBagConstraints cFillerKeadaanUmum = kepConstraint(6, 0);
        cFillerKeadaanUmum.gridheight = rowKU;
        cFillerKeadaanUmum.weightx = 1.0;
        cFillerKeadaanUmum.fill = java.awt.GridBagConstraints.BOTH;
        cFillerKeadaanUmum.insets = new java.awt.Insets(0,0,0,0);
        panelKeadaanUmum.add(javax.swing.Box.createHorizontalGlue(), cFillerKeadaanUmum);

        // panelKeadaanUmum ditempel 1 baris di panelKepAtas, gridwidth
        // REMAINDER (pakai semua sisa lebar baris) + fill HORIZONTAL supaya
        // lebar totalnya sama dgn baris lain, tapi kolom INTERNALnya
        // (barusan) tetap independen/rapat.
        java.awt.GridBagConstraints cKeadaanUmum = kepConstraint(0, row);
        cKeadaanUmum.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cKeadaanUmum.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cKeadaanUmum.insets = new java.awt.Insets(0,0,0,0);
        panelKepAtas.add(panelKeadaanUmum, cKeadaanUmum);
        row++;

        // -- Status Kehamilan --
        // Widget lama StatusKehamilan/Gravida/Para/Abortus/HPHT direparent
        // dari section "I. RIWAYAT KESEHATAN PASIEN" (posisi asli y=150-180)
        // ke sini supaya urutan sesuai foto fisik RM 03-4 (persis setelah
        // Keadaan Umum). Label lama disembunyikan, diganti label baru lewat
        // tambahBarisKepField().
        jLabel78.setVisible(false); jLabel29.setVisible(false); jLabel32.setVisible(false);
        jLabel33.setVisible(false); jLabel35.setVisible(false);
        row = tambahBarisKepJudul(panelKepAtas, row, "STATUS KEHAMILAN");
        StatusKehamilan.setPreferredSize(new java.awt.Dimension(110,23));
        row = tambahBarisKepField(panelKepAtas, row, "Status Kehamilan", StatusKehamilan);
        // widget.TextBox lama TIDAK punya .setPreferredSize() (cuma
        // .setBounds() GEN block, diabaikan begitu dipindah ke dalam
        // FlowLayout bersarang di GridBagLayout) - tanpa ini kolomnya
        // menciut ke ~1px (bug yg sama spt di sesi RM 03-2, lihat
        // PROGRESS.md poin 5 "Bug ditemukan & diperbaiki").
        Gravida.setPreferredSize(new java.awt.Dimension(85,23));
        Para.setPreferredSize(new java.awt.Dimension(85,23));
        Abortus.setPreferredSize(new java.awt.Dimension(85,23));
        HPHT.setPreferredSize(new java.awt.Dimension(120,23));
        widget.PanelBiasa panelKehamilan = new widget.PanelBiasa();
        panelKehamilan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        widget.Label lblGravida = new widget.Label(); lblGravida.setText("Gravida");
        widget.Label lblPara = new widget.Label(); lblPara.setText("Para");
        widget.Label lblAbortus = new widget.Label(); lblAbortus.setText("Abortus");
        widget.Label lblHPHT = new widget.Label(); lblHPHT.setText("HPHT");
        panelKehamilan.add(lblGravida); panelKehamilan.add(Gravida);
        panelKehamilan.add(lblPara); panelKehamilan.add(Para);
        panelKehamilan.add(lblAbortus); panelKehamilan.add(Abortus);
        panelKehamilan.add(lblHPHT); panelKehamilan.add(HPHT);
        // Ditulis manual (bukan lewat tambahBarisKepField) supaya label-nya
        // punya referensi (lblKehamilan) - dipakai toggle show/hide di bawah.
        widget.Label lblKehamilan = new widget.Label(); lblKehamilan.setText("Gravida / Para / Abortus / HPHT :");
        java.awt.GridBagConstraints cLblKehamilan = kepConstraint(0,row);
        panelKepAtas.add(lblKehamilan, cLblKehamilan);
        java.awt.GridBagConstraints cKehamilan = kepConstraint(1,row);
        cKehamilan.anchor = java.awt.GridBagConstraints.WEST;
        cKehamilan.fill = java.awt.GridBagConstraints.NONE;
        cKehamilan.weightx = 0;
        panelKepAtas.add(panelKehamilan, cKehamilan);
        row++;

        // Baris Gravida/Para/Abortus/HPHT cuma tampil kalau Status Kehamilan
        // = "Hamil" (permintaan user). Toggle di runtime harus ikut
        // menggeser panelKepDua/Tiga/Empat/Obat & tinggi FormInput, krn
        // tinggi panelKepAtas berubah begitu barisnya muncul/hilang -
        // kalau tidak, baris2 di bawahnya akan tertimpa/ada celah.
        StatusKehamilan.addActionListener((java.awt.event.ActionEvent e) -> {
            boolean hamil = "Hamil".equals(StatusKehamilan.getSelectedItem());
            if (lblKehamilan.isVisible() == hamil) return;
            lblKehamilan.setVisible(hamil);
            panelKehamilan.setVisible(hamil);

            int tinggiLama = panelKepAtas.getHeight();
            java.awt.Dimension ukuranBaru = panelKepAtas.getPreferredSize();
            panelKepAtas.setSize(ukuranBaru.width, ukuranBaru.height);
            panelKepAtas.revalidate();
            int delta = ukuranBaru.height - tinggiLama;
            if (delta != 0) {
                panelKepDua.setLocation(panelKepDua.getX(), panelKepDua.getY() + delta);
                panelKepTiga.setLocation(panelKepTiga.getX(), panelKepTiga.getY() + delta);
                panelKepEmpat.setLocation(panelKepEmpat.getX(), panelKepEmpat.getY() + delta);
                panelKepObat.setLocation(panelKepObat.getX(), panelKepObat.getY() + delta);
                java.awt.Dimension ukuranForm = FormInput.getPreferredSize();
                FormInput.setPreferredSize(new java.awt.Dimension(ukuranForm.width, ukuranForm.height + delta));
            }
            FormInput.revalidate();
            FormInput.repaint();
        });
        // Set kondisi awal sesuai pilihan default combo (index 0 = "Tidak
        // Hamil") - TIDAK lewat listener di atas krn panelKepDua/Tiga/Empat/
        // Obat belum dibuat di titik ini (baru dibuat di initKepTambahanHal2/
        // 3/4 & initObatInfusTambahan(), dipanggil setelah method ini).
        boolean hamilAwal = "Hamil".equals(StatusKehamilan.getSelectedItem());
        lblKehamilan.setVisible(hamilAwal);
        panelKehamilan.setVisible(hamilAwal);

        // -- Asesmen Psikologis --
        row = tambahBarisKepJudul(panelKepAtas, row, "ASESMEN PSIKOLOGIS");
        CekPsiTenang = new widget.CekBox(); CekPsiTenang.setText("Tenang");
        CekPsiCemas = new widget.CekBox(); CekPsiCemas.setText("Cemas");
        CekPsiTakut = new widget.CekBox(); CekPsiTakut.setText("Takut");
        CekPsiSedih = new widget.CekBox(); CekPsiSedih.setText("Sedih");
        CekPsiMarah = new widget.CekBox(); CekPsiMarah.setText("Marah");
        CekPsiBunuhDiri = new widget.CekBox(); CekPsiBunuhDiri.setText("Kecenderungan Bunuh Diri");
        java.awt.GridBagConstraints cPsi = kepConstraint(0,row); cPsi.gridwidth=2;
        // Urutan diatur ulang (bukan urutan deklarasi) supaya isi baris 1 =
        // Tenang/Cemas/Marah dan baris 2 = Takut/Sedih/Kecenderungan Bunuh
        // Diri, sesuai GridLayout(0,3) row-major.
        panelKepAtas.add(panelChecklistKep(3, CekPsiTenang,CekPsiCemas,CekPsiMarah,CekPsiTakut,CekPsiSedih,CekPsiBunuhDiri), cPsi);
        row++;
        RbMasalahPrilakuTidak = new widget.RadioButton(); RbMasalahPrilakuTidak.setText("Tidak");
        RbMasalahPrilakuYa = new widget.RadioButton(); RbMasalahPrilakuYa.setText("Ada, Sebutkan");
        bgMasalahPrilaku = new javax.swing.ButtonGroup();
        bgMasalahPrilaku.add(RbMasalahPrilakuTidak); bgMasalahPrilaku.add(RbMasalahPrilakuYa);
        TMasalahPrilakuSebutkan = new widget.TextBox(); TMasalahPrilakuSebutkan.setPreferredSize(new java.awt.Dimension(250,23));
        widget.PanelBiasa panelMasalahPrilaku = new widget.PanelBiasa();
        panelMasalahPrilaku.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelMasalahPrilaku.add(RbMasalahPrilakuTidak); panelMasalahPrilaku.add(RbMasalahPrilakuYa); panelMasalahPrilaku.add(TMasalahPrilakuSebutkan);
        row = tambahBarisKepField(panelKepAtas, row, "Masalah Prilaku", panelMasalahPrilaku);

        // -- Status Sosial dan Ekonomis --
        row = tambahBarisKepJudul(panelKepAtas, row, "STATUS SOSIAL DAN EKONOMIS");
        RbNikahBelumMenikah = new widget.RadioButton(); RbNikahBelumMenikah.setText("Belum Menikah");
        RbNikahMenikah = new widget.RadioButton(); RbNikahMenikah.setText("Menikah");
        RbNikahCerai = new widget.RadioButton(); RbNikahCerai.setText("Cerai");
        RbNikahLainnya = new widget.RadioButton(); RbNikahLainnya.setText("Lainnya");
        bgStatusPernikahan = new javax.swing.ButtonGroup();
        bgStatusPernikahan.add(RbNikahBelumMenikah); bgStatusPernikahan.add(RbNikahMenikah);
        bgStatusPernikahan.add(RbNikahCerai); bgStatusPernikahan.add(RbNikahLainnya);
        TKetStatusPernikahan = new widget.TextBox(); TKetStatusPernikahan.setPreferredSize(new java.awt.Dimension(150,23));
        widget.PanelBiasa panelStatusNikah = new widget.PanelBiasa();
        panelStatusNikah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelStatusNikah.add(RbNikahBelumMenikah); panelStatusNikah.add(RbNikahMenikah);
        panelStatusNikah.add(RbNikahCerai); panelStatusNikah.add(RbNikahLainnya); panelStatusNikah.add(TKetStatusPernikahan);
        row = tambahBarisKepField(panelKepAtas, row, "Status Pernikahan", panelStatusNikah);

        RbTinggalSendiri = new widget.RadioButton(); RbTinggalSendiri.setText("Rumah Sendiri");
        RbTinggalKeluarga = new widget.RadioButton(); RbTinggalKeluarga.setText("Rumah Keluarganya");
        RbTinggalLainnya = new widget.RadioButton(); RbTinggalLainnya.setText("Lainnya");
        bgTempatTinggal = new javax.swing.ButtonGroup();
        bgTempatTinggal.add(RbTinggalSendiri); bgTempatTinggal.add(RbTinggalKeluarga); bgTempatTinggal.add(RbTinggalLainnya);
        TKetTempatTinggal = new widget.TextBox(); TKetTempatTinggal.setPreferredSize(new java.awt.Dimension(150,23));
        widget.PanelBiasa panelTempatTinggal = new widget.PanelBiasa();
        panelTempatTinggal.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelTempatTinggal.add(RbTinggalSendiri); panelTempatTinggal.add(RbTinggalKeluarga);
        panelTempatTinggal.add(RbTinggalLainnya); panelTempatTinggal.add(TKetTempatTinggal);
        row = tambahBarisKepField(panelKepAtas, row, "Tempat Tinggal", panelTempatTinggal);

        TPekerjaanKep = new widget.TextBox(); TPekerjaanKep.setPreferredSize(new java.awt.Dimension(250,23));
        row = tambahBarisKepField(panelKepAtas, row, "Pekerjaan", TPekerjaanKep);
        // Baris input "Pendidikan Pasien" dihapus dari tampilan (permintaan
        // user - tidak dipakai). TPendidikanPasienKep TETAP dibuat (bukan
        // dihapus totalj) krn masih dipakai getData()/simpan()/emptTeks()/
        // bangunRingkasanHtml() utk kolom DB pendidikan_pasien_kep - kalau
        // dihapus total akan NullPointerException di situ. Cukup tidak
        // ditambahkan ke panelKepAtas, jadi tidak ada baris/UI-nya lagi.
        TPendidikanPasienKep = new widget.TextBox(); TPendidikanPasienKep.setPreferredSize(new java.awt.Dimension(250,23));

        // -- Skrining Geriatri --
        row = tambahBarisKepJudul(panelKepAtas, row, "SKRINING GERIATRI");
        RbGeri1Ya = new widget.RadioButton(); RbGeri1Ya.setText("Ya");
        RbGeri1Tidak = new widget.RadioButton(); RbGeri1Tidak.setText("Tidak");
        bgGeri1 = new javax.swing.ButtonGroup(); bgGeri1.add(RbGeri1Ya); bgGeri1.add(RbGeri1Tidak);
        widget.PanelBiasa panelGeri1 = new widget.PanelBiasa();
        panelGeri1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelGeri1.add(RbGeri1Ya); panelGeri1.add(RbGeri1Tidak);
        row = tambahBarisKepField(panelKepAtas, row, "≥60 Th dengan >1 Penyakit Fisik/Psikis", panelGeri1);

        RbGeri2Ya = new widget.RadioButton(); RbGeri2Ya.setText("Ya");
        RbGeri2Tidak = new widget.RadioButton(); RbGeri2Tidak.setText("Tidak");
        bgGeri2 = new javax.swing.ButtonGroup(); bgGeri2.add(RbGeri2Ya); bgGeri2.add(RbGeri2Tidak);
        widget.PanelBiasa panelGeri2 = new widget.PanelBiasa();
        panelGeri2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelGeri2.add(RbGeri2Ya); panelGeri2.add(RbGeri2Tidak);
        row = tambahBarisKepField(panelKepAtas, row, "≥60 Th 1 Penyakit Fisik/Psikis + Penurunan Fungsi", panelGeri2);

        RbGeri3Ya = new widget.RadioButton(); RbGeri3Ya.setText("Ya");
        RbGeri3Tidak = new widget.RadioButton(); RbGeri3Tidak.setText("Tidak");
        bgGeri3 = new javax.swing.ButtonGroup(); bgGeri3.add(RbGeri3Ya); bgGeri3.add(RbGeri3Tidak);
        widget.PanelBiasa panelGeri3 = new widget.PanelBiasa();
        panelGeri3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelGeri3.add(RbGeri3Ya); panelGeri3.add(RbGeri3Tidak);
        row = tambahBarisKepField(panelKepAtas, row, "≥70 Th dengan 1 Penyakit Fisik/Psikologis", panelGeri3);

        // -- Skrining Resiko Pasien Jatuh (Get Up and Go Test) --
        row = tambahBarisKepJudul(panelKepAtas, row, "SKRINING RESIKO PASIEN JATUH (GET UP AND GO TEST)");
        // Hasil dihitung otomatis (lihat hitungHasilJatuh()) - setiap
        // perubahan pilihan A/B memicu hitung ulang.
        java.awt.event.ActionListener alJatuh = (java.awt.event.ActionEvent e) -> hitungHasilJatuh();
        RbJatuhCaraBerjalanYa = new widget.RadioButton(); RbJatuhCaraBerjalanYa.setText("Ya");
        RbJatuhCaraBerjalanTidak = new widget.RadioButton(); RbJatuhCaraBerjalanTidak.setText("Tidak");
        bgJatuhCaraBerjalan = new javax.swing.ButtonGroup();
        bgJatuhCaraBerjalan.add(RbJatuhCaraBerjalanYa); bgJatuhCaraBerjalan.add(RbJatuhCaraBerjalanTidak);
        RbJatuhCaraBerjalanYa.addActionListener(alJatuh); RbJatuhCaraBerjalanTidak.addActionListener(alJatuh);
        widget.PanelBiasa panelJatuhA = new widget.PanelBiasa();
        panelJatuhA.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelJatuhA.add(RbJatuhCaraBerjalanYa); panelJatuhA.add(RbJatuhCaraBerjalanTidak);
        row = tambahBarisKepField(panelKepAtas, row, "A. Cara Berjalan Tidak Seimbang/Pakai Alat Bantu", panelJatuhA);

        RbJatuhMenopangYa = new widget.RadioButton(); RbJatuhMenopangYa.setText("Ya");
        RbJatuhMenopangTidak = new widget.RadioButton(); RbJatuhMenopangTidak.setText("Tidak");
        bgJatuhMenopang = new javax.swing.ButtonGroup();
        bgJatuhMenopang.add(RbJatuhMenopangYa); bgJatuhMenopang.add(RbJatuhMenopangTidak);
        RbJatuhMenopangYa.addActionListener(alJatuh); RbJatuhMenopangTidak.addActionListener(alJatuh);
        widget.PanelBiasa panelJatuhB = new widget.PanelBiasa();
        panelJatuhB.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelJatuhB.add(RbJatuhMenopangYa); panelJatuhB.add(RbJatuhMenopangTidak);
        row = tambahBarisKepField(panelKepAtas, row, "B. Menopang Saat Akan Duduk", panelJatuhB);

        RbJatuhTidakBeresiko = new widget.RadioButton(); RbJatuhTidakBeresiko.setText("Tidak Beresiko");
        RbJatuhBeresiko = new widget.RadioButton(); RbJatuhBeresiko.setText("Beresiko");
        bgJatuhHasil = new javax.swing.ButtonGroup(); bgJatuhHasil.add(RbJatuhTidakBeresiko); bgJatuhHasil.add(RbJatuhBeresiko);
        widget.PanelBiasa panelJatuhHasil = new widget.PanelBiasa();
        panelJatuhHasil.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelJatuhHasil.add(RbJatuhTidakBeresiko); panelJatuhHasil.add(RbJatuhBeresiko);
        row = tambahBarisKepField(panelKepAtas, row, "Hasil (Tidak Beresiko bila A dan B tidak ditemukan)", panelJatuhHasil);

        // -- Pengkajian Spiritual, Kepercayaan, Fungsional dan Budaya --
        // Agama/Bahasa/Cacat Tubuh REUSE widget Agama/Bahasa/CacatFisik yang
        // sudah ada (dari registrasi pasien) - TIDAK diulang di sini.
        row = tambahBarisKepJudul(panelKepAtas, row, "PENGKAJIAN SPIRITUAL, KEPERCAYAAN, FUNGSIONAL DAN BUDAYA");
        // Agama diubah jadi input manual (kolom baru agama_manual) atas
        // permintaan user - sebelumnya reuse widget readonly dari
        // registrasi pasien. Widget lama Agama/Bahasa/CacatFisik
        // dipensiunkan (lihat retiredRiwayat di bawah) - "Bahasa
        // Sehari-hari" dihapus total dari UI (tak ada di foto fisik).
        TAgama = new widget.TextBox(); TAgama.setPreferredSize(new java.awt.Dimension(200,23));
        row = tambahBarisKepField(panelKepAtas, row, "Agama", TAgama);
        RbWajibIbadahBaligh = new widget.RadioButton(); RbWajibIbadahBaligh.setText("Baligh");
        RbWajibIbadahBelumBaligh = new widget.RadioButton(); RbWajibIbadahBelumBaligh.setText("Belum Baligh");
        RbWajibIbadahHalanganLain = new widget.RadioButton(); RbWajibIbadahHalanganLain.setText("Halangan Lain");
        bgWajibIbadah = new javax.swing.ButtonGroup();
        bgWajibIbadah.add(RbWajibIbadahBaligh); bgWajibIbadah.add(RbWajibIbadahBelumBaligh); bgWajibIbadah.add(RbWajibIbadahHalanganLain);
        TKetWajibIbadah = new widget.TextBox(); TKetWajibIbadah.setPreferredSize(new java.awt.Dimension(150,23));
        widget.PanelBiasa panelWajibIbadah = new widget.PanelBiasa();
        panelWajibIbadah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelWajibIbadah.add(RbWajibIbadahBaligh); panelWajibIbadah.add(RbWajibIbadahBelumBaligh);
        panelWajibIbadah.add(RbWajibIbadahHalanganLain); panelWajibIbadah.add(TKetWajibIbadah);
        row = tambahBarisKepField(panelKepAtas, row, "Wajib Ibadah", panelWajibIbadah);

        RbThaharohBerwudhu = new widget.RadioButton(); RbThaharohBerwudhu.setText("Berwudhu");
        RbThaharohTayamum = new widget.RadioButton(); RbThaharohTayamum.setText("Tayamum");
        bgThaharoh = new javax.swing.ButtonGroup(); bgThaharoh.add(RbThaharohBerwudhu); bgThaharoh.add(RbThaharohTayamum);
        widget.PanelBiasa panelThaharoh = new widget.PanelBiasa();
        panelThaharoh.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelThaharoh.add(RbThaharohBerwudhu); panelThaharoh.add(RbThaharohTayamum);
        row = tambahBarisKepField(panelKepAtas, row, "Thaharoh", panelThaharoh);

        RbSholatBerdiri = new widget.RadioButton(); RbSholatBerdiri.setText("Berdiri");
        RbSholatDuduk = new widget.RadioButton(); RbSholatDuduk.setText("Duduk");
        RbSholatBerbaring = new widget.RadioButton(); RbSholatBerbaring.setText("Berbaring");
        bgSholat = new javax.swing.ButtonGroup();
        bgSholat.add(RbSholatBerdiri); bgSholat.add(RbSholatDuduk); bgSholat.add(RbSholatBerbaring);
        widget.PanelBiasa panelSholat = new widget.PanelBiasa();
        panelSholat.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelSholat.add(RbSholatBerdiri); panelSholat.add(RbSholatDuduk); panelSholat.add(RbSholatBerbaring);
        row = tambahBarisKepField(panelKepAtas, row, "Sholat", panelSholat);

        RbNilaiKepercayaanTidak = new widget.RadioButton(); RbNilaiKepercayaanTidak.setText("Tidak");
        RbNilaiKepercayaanYa = new widget.RadioButton(); RbNilaiKepercayaanYa.setText("Ya");
        bgNilaiKepercayaan = new javax.swing.ButtonGroup();
        bgNilaiKepercayaan.add(RbNilaiKepercayaanTidak); bgNilaiKepercayaan.add(RbNilaiKepercayaanYa);
        TKetNilaiKepercayaan = new widget.TextBox(); TKetNilaiKepercayaan.setPreferredSize(new java.awt.Dimension(200,23));
        widget.PanelBiasa panelNilaiKepercayaan = new widget.PanelBiasa();
        panelNilaiKepercayaan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelNilaiKepercayaan.add(RbNilaiKepercayaanTidak); panelNilaiKepercayaan.add(RbNilaiKepercayaanYa); panelNilaiKepercayaan.add(TKetNilaiKepercayaan);
        row = tambahBarisKepField(panelKepAtas, row, "Nilai dan Kepercayaan Tertentu", panelNilaiKepercayaan);

        // Hambatan Budaya/Bahasa (StatusBudaya/KetBudaya) direparent dari
        // section lama - masih field aktif yg dipertahankan, cuma
        // posisinya disatukan ke sini sesuai foto.
        // StatusBudaya/KetBudaya juga butuh .setPreferredSize() eksplisit
        // - alasan sama spt Gravida/Para/Abortus/HPHT di atas.
        StatusBudaya.setPreferredSize(new java.awt.Dimension(100,23));
        KetBudaya.setPreferredSize(new java.awt.Dimension(180,23));
        widget.PanelBiasa panelBudaya = new widget.PanelBiasa();
        panelBudaya.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelBudaya.add(StatusBudaya); panelBudaya.add(KetBudaya);
        row = tambahBarisKepField(panelKepAtas, row, "Hambatan Budaya / Bahasa", panelBudaya);
        jLabel95.setVisible(false);
        // Cacat Tubuh/Disabilitas diubah jadi checkbox manual sesuai foto
        // fisik (permintaan user) - widget lama CacatFisik (readonly, auto
        // dari registrasi) dipensiunkan.
        CekCacatTidak = new widget.CekBox(); CekCacatTidak.setText("Tidak");
        CekCacatTunaNetra = new widget.CekBox(); CekCacatTunaNetra.setText("Tuna Netra");
        CekCacatTunaDaksa = new widget.CekBox(); CekCacatTunaDaksa.setText("Tuna Daksa");
        CekCacatTunaRungu = new widget.CekBox(); CekCacatTunaRungu.setText("Tuna Rungu");
        CekCacatTunaWicara = new widget.CekBox(); CekCacatTunaWicara.setText("Tuna Wicara");
        row = tambahBarisKepField(panelKepAtas, row, "Cacat Tubuh / Disabilitas",
            panelChecklistKep(5, CekCacatTidak,CekCacatTunaNetra,CekCacatTunaDaksa,CekCacatTunaRungu,CekCacatTunaWicara));

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
            jLabel9, scrollPane4,   // Riwayat Penggunaan Obat (RPO)
            jLabel31, scrollPane2,  // Riwayat Penyakit Dahulu (RPD)
            jLabel77, Agama,        // Agama lama (readonly registrasi) - digantikan TAgama (manual)
            jLabel76, Bahasa,       // Bahasa Sehari-hari - dihapus total, tak ada di foto fisik
            jLabel51, CacatFisik,   // Cacat Tubuh lama (readonly registrasi) - digantikan checkbox
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
        for(java.awt.Component c: retiredRiwayat) c.setVisible(false);
        for(java.awt.Component c: retiredSectionII) c.setVisible(false);
        for(java.awt.Component c: retiredSectionIII) c.setVisible(false);
        for(java.awt.Component c: retiredSectionIV) c.setVisible(false);
        for(java.awt.Component c: retiredSectionV) c.setVisible(false);
        for(java.awt.Component c: retiredSectionVI) c.setVisible(false);

        java.awt.Dimension ukuranKepAtas = panelKepAtas.getPreferredSize();
        // [Reposisi] Geser SEMUA komponen lama (II. Pemeriksaan Fisik s/d
        // Diagnosis Keperawatan/Rencana Lainnya, y asli 210-1203) turun
        // sejauh tinggi panelKepAtas, supaya panelKepAtas bisa disisipkan
        // PERSIS setelah I. Riwayat Kesehatan (y=70-210) - mengikuti urutan
        // fisik formulir (Riwayat Alergi dst tepat setelah Keluhan Utama).
        // Iterasi generik lewat FormInput.getComponents()+cek getY(), BUKAN
        // daftar widget hardcode - pola sama seperti reposisi di RM 03-2.
        geserKep1 = ukuranKepAtas.height + 20;
        for(java.awt.Component c : FormInput.getComponents()){
            if(c.getY() >= 210){
                c.setLocation(c.getX(), c.getY() + geserKep1);
            }
        }
        // panelKepAtas mulai setelah kotak "Keluhan Utama" (scrollPane1),
        // bukan lagi y=210 tetap - supaya tidak tumpang tindih dgn box
        // Keluhan Utama. Dihitung dari posisi/tinggi scrollPane1 yg
        // SEBENARNYA (y=120, bukan y=90 - lihat setBounds di atas), bukan
        // hardcode "90+143" lagi; formula lama itu basi & bikin
        // panelKepAtas (border "RIWAYAT ALERGI") mulai 30px lebih awal dari
        // ujung bawah kotak Keluhan Utama, jadi kepotong tumpang tindih.
        int yMulaiKepAtas = scrollPane1.getY() + scrollPane1.getHeight() + 20;
        FormInput.add(panelKepAtas);
        // Lebar dipakai dari preferredSize GridBagLayout sendiri (bukan
        // hardcode 840) - KEADAAN UMUM 2 kolom + panelKehamilan/panelAlergi
        // butuh >840px, kalau dipaksa 840 GridBagLayout menyusutkan SEMUA
        // kolom scr proporsional (weightx cuma ngatur kelebihan ruang,
        // bukan defisit) -> field menyusut & row yg wrap ketimpa row
        // bawahnya. lebarFormInput (di bawah) ikut disesuaikan supaya
        // scrollInput bisa scroll horizontal sampai ke tepi panel ini.
        panelKepAtas.setBounds(5, yMulaiKepAtas, ukuranKepAtas.width, ukuranKepAtas.height);
        lebarFormInput = Math.max(870, 5 + ukuranKepAtas.width + 20);
        // Semua konten lama di y>=210 sekarang disembunyikan/direparent ke
        // section baru (lihat blok retired* & reparent Agama/StatusKehamilan
        // dst di atas) - jadi panel berikutnya (panelKepDua/Tiga/dst) tidak
        // perlu lagi mencadangkan ruang setinggi blok lama (dulu y=210-1203),
        // cukup ditempel tepat setelah panelKepAtas.
        yBawahLama = yMulaiKepAtas + ukuranKepAtas.height + 20;
        FormInput.setPreferredSize(new java.awt.Dimension(lebarFormInput, yBawahLama + 20));
    }

    /**
     * [RM 03-5] Membangun section baru HALAMAN 2 formulir fisik: Skrining
     * Gizi Dewasa (MST), Skrining Gizi Anak, dan toggle Nyeri Ya/Tidak
     * (field baru `nyeri_ada` - `Lokasi`/`SkalaNyeri` widget LAMA TIDAK
     * dipindah dari posisi aslinya, tetap dipakai apa adanya dari sana).
     * Ditempel di bawah panelKepAtas.
     */
    private void initKepTambahanHal2(){
        panelKepDua = new widget.PanelBiasa();
        panelKepDua.setLayout(new java.awt.GridBagLayout());
        int row = 0;

        // -- Skrining Gizi Dewasa (Malnutrition Screening Tool / MST) --
        row = tambahBarisKepJudul(panelKepDua, row, "SKRINING GIZI DEWASA (MST)");
        RbMstTidakAda = new widget.RadioButton(); RbMstTidakAda.setText("Tidak Ada Penurunan BB");
        RbMstTidakYakin = new widget.RadioButton(); RbMstTidakYakin.setText("Tidak Yakin/Baju Longgar");
        RbMstYa = new widget.RadioButton(); RbMstYa.setText("Ya, Ada Penurunan BB");
        bgMstPenurunanBb = new javax.swing.ButtonGroup();
        bgMstPenurunanBb.add(RbMstTidakAda); bgMstPenurunanBb.add(RbMstTidakYakin); bgMstPenurunanBb.add(RbMstYa);
        // Total Skor MST dihitung otomatis (lihat hitungTotalMst()) - setiap
        // perubahan pilihan yg mempengaruhi skor memicu hitung ulang.
        RbMstTidakAda.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        RbMstTidakYakin.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        RbMstYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        widget.PanelBiasa panelMstPenurunan = new widget.PanelBiasa();
        panelMstPenurunan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelMstPenurunan.add(RbMstTidakAda); panelMstPenurunan.add(RbMstTidakYakin); panelMstPenurunan.add(RbMstYa);
        row = tambahBarisKepField(panelKepDua, row, "Penurunan BB 6 Bulan Terakhir", panelMstPenurunan);

        MstPenurunanBbKg = new widget.ComboBox();
        MstPenurunanBbKg.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"","1-5 kg","6-10 kg","11-15 kg",">15 kg"}));
        MstPenurunanBbKg.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        row = tambahBarisKepField(panelKepDua, row, "Jumlah Penurunan (kalau Ya)", MstPenurunanBbKg);

        RbMstAsupanYa = new widget.RadioButton(); RbMstAsupanYa.setText("Ya");
        RbMstAsupanTidak = new widget.RadioButton(); RbMstAsupanTidak.setText("Tidak");
        bgMstAsupan = new javax.swing.ButtonGroup(); bgMstAsupan.add(RbMstAsupanYa); bgMstAsupan.add(RbMstAsupanTidak);
        RbMstAsupanYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        RbMstAsupanTidak.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        widget.PanelBiasa panelMstAsupan = new widget.PanelBiasa();
        panelMstAsupan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelMstAsupan.add(RbMstAsupanYa); panelMstAsupan.add(RbMstAsupanTidak);
        row = tambahBarisKepField(panelKepDua, row, "Asupan Makan Berkurang (Tidak Nafsu Makan)", panelMstAsupan);

        TMstTotalSkor = new widget.TextBox(); TMstTotalSkor.setPreferredSize(new java.awt.Dimension(60,23));
        TMstTotalSkor.setEditable(false);
        row = tambahBarisKepField(panelKepDua, row, "Total Skor", TMstTotalSkor);

        RbMstDiagnosisYa = new widget.RadioButton(); RbMstDiagnosisYa.setText("Ya");
        RbMstDiagnosisTidak = new widget.RadioButton(); RbMstDiagnosisTidak.setText("Tidak");
        bgMstDiagnosis = new javax.swing.ButtonGroup(); bgMstDiagnosis.add(RbMstDiagnosisYa); bgMstDiagnosis.add(RbMstDiagnosisTidak);
        // Ikut mempengaruhi Kesimpulan (lihat hitungTotalMst()) - pasien
        // dgn diagnosis khusus otomatis "Risiko" meski skor <2.
        RbMstDiagnosisYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        RbMstDiagnosisTidak.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalMst());
        TMstDiagnosisSebutkan = new widget.TextBox(); TMstDiagnosisSebutkan.setPreferredSize(new java.awt.Dimension(200,23));
        widget.PanelBiasa panelMstDiagnosis = new widget.PanelBiasa();
        panelMstDiagnosis.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelMstDiagnosis.add(RbMstDiagnosisYa); panelMstDiagnosis.add(RbMstDiagnosisTidak); panelMstDiagnosis.add(TMstDiagnosisSebutkan);
        row = tambahBarisKepField(panelKepDua, row, "Pasien Diagnosis Khusus (DM/Kemoterapi/dst)", panelMstDiagnosis);

        RbMstRisiko = new widget.RadioButton(); RbMstRisiko.setText("Risiko");
        RbMstTidakRisiko = new widget.RadioButton(); RbMstTidakRisiko.setText("Tidak Risiko");
        bgMstKesimpulan = new javax.swing.ButtonGroup(); bgMstKesimpulan.add(RbMstRisiko); bgMstKesimpulan.add(RbMstTidakRisiko);
        widget.PanelBiasa panelMstKesimpulan = new widget.PanelBiasa();
        panelMstKesimpulan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelMstKesimpulan.add(RbMstRisiko); panelMstKesimpulan.add(RbMstTidakRisiko);
        row = tambahBarisKepField(panelKepDua, row, "Kesimpulan", panelMstKesimpulan);

        // -- Skrining Gizi Anak (Usia 0-14 tahun) --
        row = tambahBarisKepJudul(panelKepDua, row, "SKRINING GIZI ANAK (USIA 0-14 TAHUN)");
        RbGiziAnakKurusYa = new widget.RadioButton(); RbGiziAnakKurusYa.setText("Ya");
        RbGiziAnakKurusTidak = new widget.RadioButton(); RbGiziAnakKurusTidak.setText("Tidak");
        bgGiziAnakKurus = new javax.swing.ButtonGroup(); bgGiziAnakKurus.add(RbGiziAnakKurusYa); bgGiziAnakKurus.add(RbGiziAnakKurusTidak);
        // Total Skor & Kesimpulan Gizi Anak dihitung otomatis (lihat
        // hitungTotalGiziAnak()) - setiap perubahan salah satu dari 4
        // kriteria Ya/Tidak memicu hitung ulang.
        RbGiziAnakKurusYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        RbGiziAnakKurusTidak.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        widget.PanelBiasa panelGiziKurus = new widget.PanelBiasa();
        panelGiziKurus.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelGiziKurus.add(RbGiziAnakKurusYa); panelGiziKurus.add(RbGiziAnakKurusTidak);
        row = tambahBarisKepField(panelKepDua, row, "Pasien Tampak Kurus", panelGiziKurus);

        RbGiziAnakTurunBbYa = new widget.RadioButton(); RbGiziAnakTurunBbYa.setText("Ya");
        RbGiziAnakTurunBbTidak = new widget.RadioButton(); RbGiziAnakTurunBbTidak.setText("Tidak");
        bgGiziAnakTurunBb = new javax.swing.ButtonGroup(); bgGiziAnakTurunBb.add(RbGiziAnakTurunBbYa); bgGiziAnakTurunBb.add(RbGiziAnakTurunBbTidak);
        RbGiziAnakTurunBbYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        RbGiziAnakTurunBbTidak.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        widget.PanelBiasa panelGiziTurunBb = new widget.PanelBiasa();
        panelGiziTurunBb.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelGiziTurunBb.add(RbGiziAnakTurunBbYa); panelGiziTurunBb.add(RbGiziAnakTurunBbTidak);
        row = tambahBarisKepField(panelKepDua, row, "Penurunan BB Sebulan Terakhir", panelGiziTurunBb);

        RbGiziAnakKondisiYa = new widget.RadioButton(); RbGiziAnakKondisiYa.setText("Ya");
        RbGiziAnakKondisiTidak = new widget.RadioButton(); RbGiziAnakKondisiTidak.setText("Tidak");
        bgGiziAnakKondisi = new javax.swing.ButtonGroup(); bgGiziAnakKondisi.add(RbGiziAnakKondisiYa); bgGiziAnakKondisi.add(RbGiziAnakKondisiTidak);
        RbGiziAnakKondisiYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        RbGiziAnakKondisiTidak.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        widget.PanelBiasa panelGiziKondisi = new widget.PanelBiasa();
        panelGiziKondisi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelGiziKondisi.add(RbGiziAnakKondisiYa); panelGiziKondisi.add(RbGiziAnakKondisiTidak);
        row = tambahBarisKepField(panelKepDua, row, "Diare ≥5x/hari atau Muntah >3x/hari (Seminggu Terakhir)", panelGiziKondisi);

        RbGiziAnakRisikoYa = new widget.RadioButton(); RbGiziAnakRisikoYa.setText("Ya");
        RbGiziAnakRisikoTidak = new widget.RadioButton(); RbGiziAnakRisikoTidak.setText("Tidak");
        bgGiziAnakRisiko = new javax.swing.ButtonGroup(); bgGiziAnakRisiko.add(RbGiziAnakRisikoYa); bgGiziAnakRisiko.add(RbGiziAnakRisikoTidak);
        RbGiziAnakRisikoYa.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        RbGiziAnakRisikoTidak.addActionListener((java.awt.event.ActionEvent e) -> hitungTotalGiziAnak());
        widget.PanelBiasa panelGiziRisiko = new widget.PanelBiasa();
        panelGiziRisiko.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelGiziRisiko.add(RbGiziAnakRisikoYa); panelGiziRisiko.add(RbGiziAnakRisikoTidak);
        row = tambahBarisKepField(panelKepDua, row, "Ada Penyakit/Keadaan Risiko Malnutrisi", panelGiziRisiko);

        TGiziAnakTotalSkor = new widget.TextBox(); TGiziAnakTotalSkor.setPreferredSize(new java.awt.Dimension(60,23));
        TGiziAnakTotalSkor.setEditable(false);
        row = tambahBarisKepField(panelKepDua, row, "Total Skor", TGiziAnakTotalSkor);

        RbGiziAnakRendah = new widget.RadioButton(); RbGiziAnakRendah.setText("Risiko Rendah");
        RbGiziAnakSedang = new widget.RadioButton(); RbGiziAnakSedang.setText("Risiko Sedang");
        RbGiziAnakTinggi = new widget.RadioButton(); RbGiziAnakTinggi.setText("Risiko Tinggi");
        bgGiziAnakKesimpulan = new javax.swing.ButtonGroup();
        bgGiziAnakKesimpulan.add(RbGiziAnakRendah); bgGiziAnakKesimpulan.add(RbGiziAnakSedang); bgGiziAnakKesimpulan.add(RbGiziAnakTinggi);
        widget.PanelBiasa panelGiziKesimpulan = new widget.PanelBiasa();
        panelGiziKesimpulan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelGiziKesimpulan.add(RbGiziAnakRendah); panelGiziKesimpulan.add(RbGiziAnakSedang); panelGiziKesimpulan.add(RbGiziAnakTinggi);
        row = tambahBarisKepField(panelKepDua, row, "Kesimpulan", panelGiziKesimpulan);

        // -- Asesmen Nyeri --
        row = tambahBarisKepJudul(panelKepDua, row, "ASESMEN NYERI");
        RbNyeriAdaYa = new widget.RadioButton(); RbNyeriAdaYa.setText("Ya");
        RbNyeriAdaTidak = new widget.RadioButton(); RbNyeriAdaTidak.setText("Tidak");
        bgNyeriAda = new javax.swing.ButtonGroup(); bgNyeriAda.add(RbNyeriAdaYa); bgNyeriAda.add(RbNyeriAdaTidak);
        widget.PanelBiasa panelNyeriAda = new widget.PanelBiasa();
        panelNyeriAda.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelNyeriAda.add(RbNyeriAdaYa); panelNyeriAda.add(RbNyeriAdaTidak);
        row = tambahBarisKepField(panelKepDua, row, "Nyeri", panelNyeriAda);
        // Lokasi/SkalaNyeri direparent dari section "V. SKALA NYERI" lama
        // ke sini supaya satu grup dgn toggle Nyeri Ya/Tidak, sesuai foto
        // fisik RM 03-5 ("Nyeri: Tidak/Ya, Lokasi..."). Label lama
        // (jLabel83/85) disembunyikan, diganti label baru di sini.
        jLabel83.setVisible(false); jLabel85.setVisible(false);
        // Lokasi widget.TextBox lama TIDAK punya .setPreferredSize() (cuma
        // .setBounds() GEN block, diabaikan begitu dipindah ke GridBagLayout)
        // - tanpa ini kolomnya menciut ke ~1px, sama spt bug Gravida/Para dulu.
        Lokasi.setPreferredSize(new java.awt.Dimension(220,23));
        row = tambahBarisKepField(panelKepDua, row, "Lokasi", Lokasi);
        row = tambahBarisKepField(panelKepDua, row, "Skala Nyeri", SkalaNyeri);
        // Gambar skala nyeri (Numeric Rating Scale + Wong Baker Faces Pain
        // Scale, "/picture/nyeri.png") - widget lama PanelWall direparent
        // ke sini dari section "V. SKALA NYERI" lama (dulu jadi diagram
        // background dekat field Provokes/Quality), supaya tampil sesuai
        // foto fisik RM 03-5. BUKAN diagram lokasi tubuh spt dugaan awal.
        PanelWall.setPreferredSize(new java.awt.Dimension(360,145));
        java.awt.GridBagConstraints cSkalaNyeriGambar = kepConstraint(0,row); cSkalaNyeriGambar.gridwidth=2;
        panelKepDua.add(PanelWall, cSkalaNyeriGambar);
        row++;

        java.awt.Dimension ukuranKepDua = panelKepDua.getPreferredSize();
        // [Reposisi] panelKepDua (Halaman 2) ditempel tepat setelah
        // panelKepAtas (yBawahLama - lihat initKepTambahan()), krn konten
        // lama di antaranya sudah disembunyikan/direparent semua.
        int yBawahDua = yBawahLama;
        FormInput.add(panelKepDua);
        panelKepDua.setBounds(5, yBawahDua, ukuranKepDua.width, ukuranKepDua.height);
        lebarFormInput = Math.max(lebarFormInput, 5 + ukuranKepDua.width + 20);
        FormInput.setPreferredSize(new java.awt.Dimension(lebarFormInput, yBawahDua + ukuranKepDua.height + 20));
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
    private void initKepTambahanHal3(){
        panelKepTiga = new widget.PanelBiasa();
        panelKepTiga.setLayout(new java.awt.GridBagLayout());
        int row = 0;

        // -- FLACC --
        row = tambahBarisKepJudul(panelKepTiga, row, "FLACC (Face, Legs, Activity, Cry, Consolability) - Bayi/Anak <3 Tahun");
        String[] flaccItems = new String[]{"","0","1","2"};
        // Total FLACC dihitung otomatis (lihat hitungTotalFlacc()) - setiap
        // perubahan salah satu dari 5 kriteria memicu hitung ulang.
        java.awt.event.ActionListener alFlacc = (java.awt.event.ActionEvent e) -> hitungTotalFlacc();
        FlaccWajah = new widget.ComboBox(); FlaccWajah.setModel(new javax.swing.DefaultComboBoxModel(flaccItems));
        FlaccWajah.setPreferredSize(new java.awt.Dimension(120,26));
        FlaccWajah.addActionListener(alFlacc);
        FlaccKaki = new widget.ComboBox(); FlaccKaki.setModel(new javax.swing.DefaultComboBoxModel(flaccItems));
        FlaccKaki.setPreferredSize(new java.awt.Dimension(120,26));
        FlaccKaki.addActionListener(alFlacc);
        FlaccAktivitas = new widget.ComboBox(); FlaccAktivitas.setModel(new javax.swing.DefaultComboBoxModel(flaccItems));
        FlaccAktivitas.setPreferredSize(new java.awt.Dimension(120,26));
        FlaccAktivitas.addActionListener(alFlacc);
        FlaccMenangis = new widget.ComboBox(); FlaccMenangis.setModel(new javax.swing.DefaultComboBoxModel(flaccItems));
        FlaccMenangis.setPreferredSize(new java.awt.Dimension(120,26));
        FlaccMenangis.addActionListener(alFlacc);
        FlaccBersuara = new widget.ComboBox(); FlaccBersuara.setModel(new javax.swing.DefaultComboBoxModel(flaccItems));
        FlaccBersuara.setPreferredSize(new java.awt.Dimension(120,26));
        FlaccBersuara.addActionListener(alFlacc);

        // Dibungkus panel tersendiri (pola sama spt KEADAAN UMUM) supaya
        // kolomnya rapat, tidak ikut lebar kolom 1 milik section lain -
        // 3 baris: Wajah|Kaki, Aktivitas|Menangis, Bersuara|(kosong).
        widget.PanelBiasa panelFlacc = new widget.PanelBiasa();
        panelFlacc.setLayout(new java.awt.GridBagLayout());
        int rowFlacc = 0;

        widget.Label lblFlaccWajah = new widget.Label(); lblFlaccWajah.setText("Wajah :");
        java.awt.GridBagConstraints cLblFlaccWajah = kepConstraint(0,rowFlacc); cLblFlaccWajah.anchor = java.awt.GridBagConstraints.EAST; cLblFlaccWajah.fill = java.awt.GridBagConstraints.NONE;
        panelFlacc.add(lblFlaccWajah, cLblFlaccWajah);
        java.awt.GridBagConstraints cFlaccWajah = kepConstraint(1,rowFlacc); cFlaccWajah.anchor = java.awt.GridBagConstraints.WEST; cFlaccWajah.fill = java.awt.GridBagConstraints.NONE; cFlaccWajah.weightx = 0;
        panelFlacc.add(FlaccWajah, cFlaccWajah);
        widget.Label lblFlaccKaki = new widget.Label(); lblFlaccKaki.setText("Kaki :");
        java.awt.GridBagConstraints cLblFlaccKaki = kepConstraint(2,rowFlacc); cLblFlaccKaki.anchor = java.awt.GridBagConstraints.EAST; cLblFlaccKaki.fill = java.awt.GridBagConstraints.NONE; cLblFlaccKaki.insets = new java.awt.Insets(3,20,3,5);
        panelFlacc.add(lblFlaccKaki, cLblFlaccKaki);
        java.awt.GridBagConstraints cFlaccKaki = kepConstraint(3,rowFlacc); cFlaccKaki.anchor = java.awt.GridBagConstraints.WEST; cFlaccKaki.fill = java.awt.GridBagConstraints.NONE; cFlaccKaki.weightx = 0;
        panelFlacc.add(FlaccKaki, cFlaccKaki);
        rowFlacc++;

        widget.Label lblFlaccAktivitas = new widget.Label(); lblFlaccAktivitas.setText("Aktivitas :");
        java.awt.GridBagConstraints cLblFlaccAktivitas = kepConstraint(0,rowFlacc); cLblFlaccAktivitas.anchor = java.awt.GridBagConstraints.EAST; cLblFlaccAktivitas.fill = java.awt.GridBagConstraints.NONE;
        panelFlacc.add(lblFlaccAktivitas, cLblFlaccAktivitas);
        java.awt.GridBagConstraints cFlaccAktivitas = kepConstraint(1,rowFlacc); cFlaccAktivitas.anchor = java.awt.GridBagConstraints.WEST; cFlaccAktivitas.fill = java.awt.GridBagConstraints.NONE; cFlaccAktivitas.weightx = 0;
        panelFlacc.add(FlaccAktivitas, cFlaccAktivitas);
        widget.Label lblFlaccMenangis = new widget.Label(); lblFlaccMenangis.setText("Menangis :");
        java.awt.GridBagConstraints cLblFlaccMenangis = kepConstraint(2,rowFlacc); cLblFlaccMenangis.anchor = java.awt.GridBagConstraints.EAST; cLblFlaccMenangis.fill = java.awt.GridBagConstraints.NONE; cLblFlaccMenangis.insets = new java.awt.Insets(3,20,3,5);
        panelFlacc.add(lblFlaccMenangis, cLblFlaccMenangis);
        java.awt.GridBagConstraints cFlaccMenangis = kepConstraint(3,rowFlacc); cFlaccMenangis.anchor = java.awt.GridBagConstraints.WEST; cFlaccMenangis.fill = java.awt.GridBagConstraints.NONE; cFlaccMenangis.weightx = 0;
        panelFlacc.add(FlaccMenangis, cFlaccMenangis);
        rowFlacc++;

        widget.Label lblFlaccBersuara = new widget.Label(); lblFlaccBersuara.setText("Bersuara :");
        java.awt.GridBagConstraints cLblFlaccBersuara = kepConstraint(0,rowFlacc); cLblFlaccBersuara.anchor = java.awt.GridBagConstraints.EAST; cLblFlaccBersuara.fill = java.awt.GridBagConstraints.NONE;
        panelFlacc.add(lblFlaccBersuara, cLblFlaccBersuara);
        java.awt.GridBagConstraints cFlaccBersuara = kepConstraint(1,rowFlacc); cFlaccBersuara.anchor = java.awt.GridBagConstraints.WEST; cFlaccBersuara.fill = java.awt.GridBagConstraints.NONE; cFlaccBersuara.weightx = 0;
        panelFlacc.add(FlaccBersuara, cFlaccBersuara);
        rowFlacc++;

        java.awt.GridBagConstraints cFillerFlacc = kepConstraint(4, 0);
        cFillerFlacc.gridheight = rowFlacc;
        cFillerFlacc.weightx = 1.0;
        cFillerFlacc.fill = java.awt.GridBagConstraints.BOTH;
        cFillerFlacc.insets = new java.awt.Insets(0,0,0,0);
        panelFlacc.add(javax.swing.Box.createHorizontalGlue(), cFillerFlacc);

        java.awt.GridBagConstraints cFlaccPanel = kepConstraint(0, row);
        cFlaccPanel.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cFlaccPanel.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cFlaccPanel.insets = new java.awt.Insets(0,0,0,0);
        panelKepTiga.add(panelFlacc, cFlaccPanel);
        row++;

        // Total tetap baris tersendiri di bawah semua kriteria (permintaan
        // user), bukan ikut masuk ke dalam panelFlacc di atas.
        TFlaccTotal = new widget.TextBox(); TFlaccTotal.setPreferredSize(new java.awt.Dimension(60,23));
        TFlaccTotal.setEditable(false);
        row = tambahBarisKepField(panelKepTiga, row, "Total", TFlaccTotal);

        // -- PQRST -- Provocation/Quality dipindah dari grid 2-kolom (bikin
        // celah lebar antar checkbox, tidak sejajar dgn labelnya) ke
        // FlowLayout 1 baris - pola sama persis Radiation/Severity/Time di
        // bawah (label lalu jawabannya langsung di sebelahnya) - permintaan
        // user.
        row = tambahBarisKepJudul(panelKepTiga, row, "PQRST");
        CekProvCahaya = new widget.CekBox(); CekProvCahaya.setText("Cahaya");
        CekProvGelap = new widget.CekBox(); CekProvGelap.setText("Gelap");
        CekProvGerakan = new widget.CekBox(); CekProvGerakan.setText("Gerakan");
        CekProvBerbaring = new widget.CekBox(); CekProvBerbaring.setText("Berbaring");
        widget.PanelBiasa panelProvocation = new widget.PanelBiasa();
        panelProvocation.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelProvocation.add(CekProvCahaya); panelProvocation.add(CekProvGelap);
        panelProvocation.add(CekProvGerakan); panelProvocation.add(CekProvBerbaring);
        row = tambahBarisKepField(panelKepTiga, row, "Provocation (Faktor Yang Memperberat Nyeri)", panelProvocation);

        CekQualTumpul = new widget.CekBox(); CekQualTumpul.setText("Tumpul");
        CekQualDitusuk = new widget.CekBox(); CekQualDitusuk.setText("Ditusuk");
        CekQualBerdenyut = new widget.CekBox(); CekQualBerdenyut.setText("Berdenyut");
        CekQualTajam = new widget.CekBox(); CekQualTajam.setText("Tajam");
        CekQualPanas = new widget.CekBox(); CekQualPanas.setText("Panas");
        widget.PanelBiasa panelQuality = new widget.PanelBiasa();
        panelQuality.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelQuality.add(CekQualTumpul); panelQuality.add(CekQualDitusuk); panelQuality.add(CekQualBerdenyut);
        panelQuality.add(CekQualTajam); panelQuality.add(CekQualPanas);
        row = tambahBarisKepField(panelKepTiga, row, "Quality (Kualitas Nyeri)", panelQuality);

        RbRadiationTidak = new widget.RadioButton(); RbRadiationTidak.setText("Tidak");
        RbRadiationYa = new widget.RadioButton(); RbRadiationYa.setText("Ya, ke");
        bgRadiation = new javax.swing.ButtonGroup(); bgRadiation.add(RbRadiationTidak); bgRadiation.add(RbRadiationYa);
        TRadiationKe = new widget.TextBox(); TRadiationKe.setPreferredSize(new java.awt.Dimension(200,23));
        widget.PanelBiasa panelRadiation = new widget.PanelBiasa();
        panelRadiation.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelRadiation.add(RbRadiationTidak); panelRadiation.add(RbRadiationYa); panelRadiation.add(TRadiationKe);
        row = tambahBarisKepField(panelKepTiga, row, "Radiation (Menjalar ke Tubuh Lain)", panelRadiation);

        RbSeverityTidakNyeri = new widget.RadioButton(); RbSeverityTidakNyeri.setText("Tidak Nyeri");
        RbSeverityRingan = new widget.RadioButton(); RbSeverityRingan.setText("Ringan");
        RbSeveritySedang = new widget.RadioButton(); RbSeveritySedang.setText("Sedang");
        RbSeverityBerat = new widget.RadioButton(); RbSeverityBerat.setText("Berat");
        bgSeverity = new javax.swing.ButtonGroup();
        bgSeverity.add(RbSeverityTidakNyeri); bgSeverity.add(RbSeverityRingan); bgSeverity.add(RbSeveritySedang); bgSeverity.add(RbSeverityBerat);
        widget.PanelBiasa panelSeverity = new widget.PanelBiasa();
        panelSeverity.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelSeverity.add(RbSeverityTidakNyeri); panelSeverity.add(RbSeverityRingan); panelSeverity.add(RbSeveritySedang); panelSeverity.add(RbSeverityBerat);
        row = tambahBarisKepField(panelKepTiga, row, "Severity (Tingkat Keparahan Nyeri)", panelSeverity);

        RbTimeTerusMenerus = new widget.RadioButton(); RbTimeTerusMenerus.setText("Terus Menerus");
        RbTimeHilangTimbul = new widget.RadioButton(); RbTimeHilangTimbul.setText("Hilang Timbul");
        RbTimeKurang30 = new widget.RadioButton(); RbTimeKurang30.setText("<30 Menit");
        RbTimeLebih30 = new widget.RadioButton(); RbTimeLebih30.setText(">30 Menit");
        bgTime = new javax.swing.ButtonGroup();
        bgTime.add(RbTimeTerusMenerus); bgTime.add(RbTimeHilangTimbul); bgTime.add(RbTimeKurang30); bgTime.add(RbTimeLebih30);
        widget.PanelBiasa panelTime = new widget.PanelBiasa();
        panelTime.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelTime.add(RbTimeTerusMenerus); panelTime.add(RbTimeHilangTimbul); panelTime.add(RbTimeKurang30); panelTime.add(RbTimeLebih30);
        row = tambahBarisKepField(panelKepTiga, row, "Time (Nyeri Berlangsung)", panelTime);

        // -- I. Diagnosis Keperawatan --
        // Scroll8/tbMasalahKeperawatan (mekanisme checklist+master table
        // lama, dipertahankan apa adanya - lihat Keputusan Arsitektur #2 di
        // PROGRESS.md) direparent dari posisi lama (blok terpisah jauh di
        // atas, y asli 1020) ke sini, sesuai urutan foto fisik RM 03-6.
        row = tambahBarisKepJudul(panelKepTiga, row, "I. DIAGNOSIS KEPERAWATAN");
        Scroll8.setPreferredSize(new java.awt.Dimension(400,140));
        widget.PanelBiasa panelCariMasalah = new widget.PanelBiasa();
        panelCariMasalah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelCariMasalah.add(label12); panelCariMasalah.add(TCariMasalah);
        panelCariMasalah.add(BtnCariMasalah); panelCariMasalah.add(BtnAllMasalah); panelCariMasalah.add(BtnTambahMasalah);
        java.awt.GridBagConstraints cMasalah = kepConstraint(0,row); cMasalah.gridwidth=2;
        panelKepTiga.add(Scroll8, cMasalah);
        row++;
        java.awt.GridBagConstraints cMasalahCari = kepConstraint(0,row); cMasalahCari.gridwidth=2;
        panelKepTiga.add(panelCariMasalah, cMasalahCari);
        row++;

        // -- II. Intervensi Keperawatan --
        row = tambahBarisKepJudul(panelKepTiga, row, "II. INTERVENSI KEPERAWATAN");
        CekIntervManajemenJalanNafas = new widget.CekBox(); CekIntervManajemenJalanNafas.setText("Manajemen Jalan Nafas");
        CekIntervSuctioningJalanNafas = new widget.CekBox(); CekIntervSuctioningJalanNafas.setText("Suctioning Jalan Nafas");
        CekIntervManajemenAsamBasa = new widget.CekBox(); CekIntervManajemenAsamBasa.setText("Manajemen Asam Basa");
        CekIntervManajemenSyok = new widget.CekBox(); CekIntervManajemenSyok.setText("Manajemen Syok");
        CekIntervManajemenNyeri = new widget.CekBox(); CekIntervManajemenNyeri.setText("Manajemen Nyeri");
        CekIntervManajemenCairan = new widget.CekBox(); CekIntervManajemenCairan.setText("Manajemen Cairan");
        CekIntervMonitoringCairan = new widget.CekBox(); CekIntervMonitoringCairan.setText("Monitoring Cairan");
        CekIntervManajemenLingkungan = new widget.CekBox(); CekIntervManajemenLingkungan.setText("Manajemen Lingkungan");
        CekIntervPendidikanKesehatan = new widget.CekBox(); CekIntervPendidikanKesehatan.setText("Pendidikan Kesehatan");
        CekIntervFeverTreatment = new widget.CekBox(); CekIntervFeverTreatment.setText("Fever Treatment");
        CekIntervManajemenSensasiPerifer = new widget.CekBox(); CekIntervManajemenSensasiPerifer.setText("Manajemen Sensasi Perifer");
        CekIntervMonitoringKardiovaskuler = new widget.CekBox(); CekIntervMonitoringKardiovaskuler.setText("Monitoring Status Kardiovaskuler");
        CekIntervPencegahanPenekananLuka = new widget.CekBox(); CekIntervPencegahanPenekananLuka.setText("Pencegahan Penekanan pada Luka");
        java.awt.GridBagConstraints cInterv = kepConstraint(0,row); cInterv.gridwidth=2;
        panelKepTiga.add(panelChecklistKep(2, CekIntervManajemenJalanNafas,CekIntervSuctioningJalanNafas,CekIntervManajemenAsamBasa,
            CekIntervManajemenSyok,CekIntervManajemenNyeri,CekIntervManajemenCairan,CekIntervMonitoringCairan,
            CekIntervManajemenLingkungan,CekIntervPendidikanKesehatan,CekIntervFeverTreatment,
            CekIntervManajemenSensasiPerifer,CekIntervMonitoringKardiovaskuler,CekIntervPencegahanPenekananLuka), cInterv);
        row++;
        TIntervLainnya = new widget.TextBox(); TIntervLainnya.setPreferredSize(new java.awt.Dimension(300,23));
        row = tambahBarisKepField(panelKepTiga, row, "Lainnya", TIntervLainnya);

        // -- III. Tindakan Keperawatan Mandiri --
        // TabRencanaKeperawatan (isi tinggal tab "Rencana Keperawatan
        // Lainnya"/field Rencana TextArea setelah tab checklist lama
        // dihapus - lihat TabRencanaKeperawatan.removeTabAt(0) di
        // initKepTambahan()) direparent dari posisi lama ke sini, sesuai
        // urutan foto fisik RM 03-6.
        row = tambahBarisKepJudul(panelKepTiga, row, "III. TINDAKAN KEPERAWATAN MANDIRI");
        TabRencanaKeperawatan.setPreferredSize(new java.awt.Dimension(420,143));
        // Kontrol "Key Word"/cari/ceklis-semua/tambah (label13/TCariRencana/
        // BtnCariRencana/BtnAllRencana/BtnTambahRencana) TIDAK diikutkan -
        // itu semua cuma dipakai utk tab checklist "Rencana Keperawatan"
        // lama (tbRencanaKeperawatan) yg sudah dipensiunkan, jadi kalau
        // ikut ditampilkan tidak berfungsi sama sekali (permintaan user).
        java.awt.GridBagConstraints cRencana = kepConstraint(0,row); cRencana.gridwidth=2;
        panelKepTiga.add(TabRencanaKeperawatan, cRencana);
        row++;

        // -- IV. Kardiopulmonal --
        row = tambahBarisKepJudul(panelKepTiga, row, "IV. KARDIOPULMONAL");
        CekKardioPulseOximetri = new widget.CekBox(); CekKardioPulseOximetri.setText("Pulse Oximetri (SpO2)");
        row = tambahBarisKepField(panelKepTiga, row, "", CekKardioPulseOximetri);
        CekKardioIntubasi = new widget.CekBox(); CekKardioIntubasi.setText("Intubasi");
        row = tambahBarisKepField(panelKepTiga, row, "", CekKardioIntubasi);
        CekKardioRjp = new widget.CekBox(); CekKardioRjp.setText("RJP");
        TKardioRjpKet = new widget.TextBox(); TKardioRjpKet.setPreferredSize(new java.awt.Dimension(200,23));
        widget.PanelBiasa panelKardioRjp = new widget.PanelBiasa();
        panelKardioRjp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKardioRjp.add(CekKardioRjp); panelKardioRjp.add(TKardioRjpKet);
        row = tambahBarisKepField(panelKepTiga, row, "", panelKardioRjp);
        CekKardioKardioversiDcShock = new widget.CekBox(); CekKardioKardioversiDcShock.setText("Kardioversi / DC Shock");
        row = tambahBarisKepField(panelKepTiga, row, "", CekKardioKardioversiDcShock);

        // -- V. Tindakan Penunjang --
        row = tambahBarisKepJudul(panelKepTiga, row, "V. TINDAKAN PENUNJANG");
        CekPenunjangCekLab = new widget.CekBox(); CekPenunjangCekLab.setText("Cek Lab");
        TPenunjangCekLabKet = new widget.TextBox(); TPenunjangCekLabKet.setPreferredSize(new java.awt.Dimension(250,23));
        widget.PanelBiasa panelPenunjangLab = new widget.PanelBiasa();
        panelPenunjangLab.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelPenunjangLab.add(CekPenunjangCekLab); panelPenunjangLab.add(TPenunjangCekLabKet);
        row = tambahBarisKepField(panelKepTiga, row, "", panelPenunjangLab);
        CekPenunjangRontgen = new widget.CekBox(); CekPenunjangRontgen.setText("Rontgen");
        TPenunjangRontgenKet = new widget.TextBox(); TPenunjangRontgenKet.setPreferredSize(new java.awt.Dimension(250,23));
        widget.PanelBiasa panelPenunjangRontgen = new widget.PanelBiasa();
        panelPenunjangRontgen.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelPenunjangRontgen.add(CekPenunjangRontgen); panelPenunjangRontgen.add(TPenunjangRontgenKet);
        row = tambahBarisKepField(panelKepTiga, row, "", panelPenunjangRontgen);
        CekPenunjangCtScan = new widget.CekBox(); CekPenunjangCtScan.setText("CT Scan");
        TPenunjangCtScanKet = new widget.TextBox(); TPenunjangCtScanKet.setPreferredSize(new java.awt.Dimension(250,23));
        widget.PanelBiasa panelPenunjangCt = new widget.PanelBiasa();
        panelPenunjangCt.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelPenunjangCt.add(CekPenunjangCtScan); panelPenunjangCt.add(TPenunjangCtScanKet);
        row = tambahBarisKepField(panelKepTiga, row, "", panelPenunjangCt);

        // -- VI. Tindakan Kolaborasi --
        row = tambahBarisKepJudul(panelKepTiga, row, "VI. TINDAKAN KOLABORASI");
        CekKolabO2 = new widget.CekBox(); CekKolabO2.setText("O2");
        TKolabO2Ket = new widget.TextBox(); TKolabO2Ket.setPreferredSize(new java.awt.Dimension(200,23));
        widget.Label lblKolabO2 = new widget.Label(); lblKolabO2.setText("l/m nasal canul/NRM/RM/CPAP :");
        widget.PanelBiasa panelKolabO2 = new widget.PanelBiasa();
        panelKolabO2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKolabO2.add(CekKolabO2); panelKolabO2.add(lblKolabO2); panelKolabO2.add(TKolabO2Ket);
        row = tambahBarisKepField(panelKepTiga, row, "", panelKolabO2);
        CekKolabEkgMonitor = new widget.CekBox(); CekKolabEkgMonitor.setText("EKG / Monitor EKG");
        row = tambahBarisKepField(panelKepTiga, row, "", CekKolabEkgMonitor);
        CekKolabNebulizer = new widget.CekBox(); CekKolabNebulizer.setText("Nebulizer");
        row = tambahBarisKepField(panelKepTiga, row, "", CekKolabNebulizer);
        CekKolabPasangInfus = new widget.CekBox(); CekKolabPasangInfus.setText("Pasang Infus");
        TKolabPasangInfusKet = new widget.TextBox(); TKolabPasangInfusKet.setPreferredSize(new java.awt.Dimension(200,23));
        widget.PanelBiasa panelKolabInfus = new widget.PanelBiasa();
        panelKolabInfus.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKolabInfus.add(CekKolabPasangInfus); panelKolabInfus.add(TKolabPasangInfusKet);
        row = tambahBarisKepField(panelKepTiga, row, "", panelKolabInfus);
        CekKolabInjeksiSyringePump = new widget.CekBox(); CekKolabInjeksiSyringePump.setText("Injeksi / Syringe Pump");
        row = tambahBarisKepField(panelKepTiga, row, "", CekKolabInjeksiSyringePump);
        CekKolabTransfusiDarah = new widget.CekBox(); CekKolabTransfusiDarah.setText("Transfusi Darah");
        TKolabTransfusiDarahKet = new widget.TextBox(); TKolabTransfusiDarahKet.setPreferredSize(new java.awt.Dimension(200,23));
        widget.PanelBiasa panelKolabTransfusi = new widget.PanelBiasa();
        panelKolabTransfusi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKolabTransfusi.add(CekKolabTransfusiDarah); panelKolabTransfusi.add(TKolabTransfusiDarahKet);
        row = tambahBarisKepField(panelKepTiga, row, "", panelKolabTransfusi);
        CekKolabNgt = new widget.CekBox(); CekKolabNgt.setText("NGT, Ukuran");
        TKolabNgtUkuran = new widget.TextBox(); TKolabNgtUkuran.setPreferredSize(new java.awt.Dimension(100,23));
        widget.PanelBiasa panelKolabNgt = new widget.PanelBiasa();
        panelKolabNgt.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKolabNgt.add(CekKolabNgt); panelKolabNgt.add(TKolabNgtUkuran);
        row = tambahBarisKepField(panelKepTiga, row, "", panelKolabNgt);
        CekKolabBilasLambung = new widget.CekBox(); CekKolabBilasLambung.setText("Bilas Lambung");
        TKolabBilasLambungKet = new widget.TextBox(); TKolabBilasLambungKet.setPreferredSize(new java.awt.Dimension(200,23));
        widget.PanelBiasa panelKolabBilas = new widget.PanelBiasa();
        panelKolabBilas.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKolabBilas.add(CekKolabBilasLambung); panelKolabBilas.add(TKolabBilasLambungKet);
        row = tambahBarisKepField(panelKepTiga, row, "", panelKolabBilas);
        CekKolabDowerCatheter = new widget.CekBox(); CekKolabDowerCatheter.setText("Dower Catheter, Ukuran");
        TKolabDowerCatheterUkuran = new widget.TextBox(); TKolabDowerCatheterUkuran.setPreferredSize(new java.awt.Dimension(100,23));
        widget.PanelBiasa panelKolabDower = new widget.PanelBiasa();
        panelKolabDower.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKolabDower.add(CekKolabDowerCatheter); panelKolabDower.add(TKolabDowerCatheterUkuran);
        row = tambahBarisKepField(panelKepTiga, row, "", panelKolabDower);
        CekKolabRestrainPengaman = new widget.CekBox(); CekKolabRestrainPengaman.setText("Restrain / Pengaman");
        row = tambahBarisKepField(panelKepTiga, row, "", CekKolabRestrainPengaman);

        // -- VII. Trauma / Bedah --
        row = tambahBarisKepJudul(panelKepTiga, row, "VII. TRAUMA / BEDAH");
        CekTraumaCervicalCollar = new widget.CekBox(); CekTraumaCervicalCollar.setText("Cervical Collar");
        CekTraumaImmobilisasiTulangBelakang = new widget.CekBox(); CekTraumaImmobilisasiTulangBelakang.setText("Immobilisasi Tulang Belakang");
        CekTraumaGips = new widget.CekBox(); CekTraumaGips.setText("Gips");
        CekTraumaPerawatanLuka = new widget.CekBox(); CekTraumaPerawatanLuka.setText("Perawatan Luka");
        CekTraumaJahitLuka = new widget.CekBox(); CekTraumaJahitLuka.setText("Jahit Luka");
        CekTraumaBalutanSpalk = new widget.CekBox(); CekTraumaBalutanSpalk.setText("Balutan / Spalk");
        CekTraumaSelangDadaWsd = new widget.CekBox(); CekTraumaSelangDadaWsd.setText("Selang Dada / WSD");
        java.awt.GridBagConstraints cTrauma = kepConstraint(0,row); cTrauma.gridwidth=2;
        panelKepTiga.add(panelChecklistKep(2, CekTraumaCervicalCollar,CekTraumaImmobilisasiTulangBelakang,CekTraumaGips,
            CekTraumaPerawatanLuka,CekTraumaJahitLuka,CekTraumaBalutanSpalk,CekTraumaSelangDadaWsd), cTrauma);
        row++;
        TTraumaLainnya = new widget.TextBox(); TTraumaLainnya.setPreferredSize(new java.awt.Dimension(300,23));
        row = tambahBarisKepField(panelKepTiga, row, "Lainnya", TTraumaLainnya);

        // -- VIII. THT / Mata --
        row = tambahBarisKepJudul(panelKepTiga, row, "VIII. THT / MATA");
        CekThtIrigasiMataTelinga = new widget.CekBox(); CekThtIrigasiMataTelinga.setText("Irigasi Mata / Irigasi Telinga");
        CekThtAmbilKorpusAlienum = new widget.CekBox(); CekThtAmbilKorpusAlienum.setText("Ambil Korpus Alienum di Telinga/Hidung/Mata");
        java.awt.GridBagConstraints cTht = kepConstraint(0,row); cTht.gridwidth=2;
        panelKepTiga.add(panelChecklistKep(2, CekThtIrigasiMataTelinga,CekThtAmbilKorpusAlienum), cTht);
        row++;

        // -- IX. Tindakan Obgyn --
        row = tambahBarisKepJudul(panelKepTiga, row, "IX. TINDAKAN OBGYN");
        CekObgynMembantuPartus = new widget.CekBox(); CekObgynMembantuPartus.setText("Membantu Partus");
        CekObgynPeriksaGinekologi = new widget.CekBox(); CekObgynPeriksaGinekologi.setText("Periksa Ginekologi");
        CekObgynDopplerCtgUsg = new widget.CekBox(); CekObgynDopplerCtgUsg.setText("Doppler, CTG, USG");
        CekObgynPasangTampon = new widget.CekBox(); CekObgynPasangTampon.setText("Pasang Tampon");
        java.awt.GridBagConstraints cObgyn = kepConstraint(0,row); cObgyn.gridwidth=2;
        panelKepTiga.add(panelChecklistKep(2, CekObgynMembantuPartus,CekObgynPeriksaGinekologi,CekObgynDopplerCtgUsg,CekObgynPasangTampon), cObgyn);
        row++;

        java.awt.Dimension ukuranKepTiga = panelKepTiga.getPreferredSize();
        java.awt.Dimension ukuranKepDua3 = panelKepDua.getPreferredSize();
        int yBawahTiga = yBawahLama + ukuranKepDua3.height + 10;
        FormInput.add(panelKepTiga);
        panelKepTiga.setBounds(5, yBawahTiga, ukuranKepTiga.width, ukuranKepTiga.height);
        lebarFormInput = Math.max(lebarFormInput, 5 + ukuranKepTiga.width + 20);
        FormInput.setPreferredSize(new java.awt.Dimension(lebarFormInput, yBawahTiga + ukuranKepTiga.height + 20));
    }

    /**
     * Hasil Skrining Resiko Pasien Jatuh dihitung otomatis sesuai aturan
     * formulir fisik: Beresiko bila ditemukan salah satu antara A (Cara
     * Berjalan) atau B (Menopang Saat Akan Duduk) = Ya, Tidak Beresiko
     * bila A dan B tidak ditemukan (keduanya Tidak/belum diisi), dipicu
     * tiap ada perubahan pilihan A/B.
     */
    private void hitungHasilJatuh(){
        if(RbJatuhCaraBerjalanYa.isSelected() || RbJatuhMenopangYa.isSelected()){
            RbJatuhBeresiko.setSelected(true);
        }else{
            RbJatuhTidakBeresiko.setSelected(true);
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
    private void hitungTotalMst(){
        int skor = 0;
        if(RbMstTidakYakin.isSelected()){
            skor += 2;
        }else if(RbMstYa.isSelected()){
            Object kg = MstPenurunanBbKg.getSelectedItem();
            String kgTeks = kg==null?"":kg.toString();
            if("1-5 kg".equals(kgTeks)) skor += 1;
            else if("6-10 kg".equals(kgTeks)) skor += 2;
            else if("11-15 kg".equals(kgTeks)) skor += 3;
            else if(">15 kg".equals(kgTeks)) skor += 4;
        }
        if(RbMstAsupanYa.isSelected()) skor += 1;
        TMstTotalSkor.setText(String.valueOf(skor));
        if(skor>=2 || RbMstDiagnosisYa.isSelected()){
            RbMstRisiko.setSelected(true);
        }else{
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
    private void hitungTotalGiziAnak(){
        int skor = 0;
        if(RbGiziAnakKurusYa.isSelected()) skor += 1;
        if(RbGiziAnakTurunBbYa.isSelected()) skor += 1;
        if(RbGiziAnakKondisiYa.isSelected()) skor += 1;
        if(RbGiziAnakRisikoYa.isSelected()) skor += 2;
        TGiziAnakTotalSkor.setText(String.valueOf(skor));
        if(skor==0){
            RbGiziAnakRendah.setSelected(true);
        }else if(skor<=3){
            RbGiziAnakSedang.setSelected(true);
        }else{
            RbGiziAnakTinggi.setSelected(true);
        }
    }

    /**
     * Total Skor FLACC dihitung otomatis dari jumlah 5 kriteria (Wajah/
     * Kaki/Aktivitas/Menangis/Bersuara), masing-masing bernilai 0-2 sesuai
     * ComboBox yang dipilih, dipicu tiap ada perubahan pilihan.
     */
    private void hitungTotalFlacc(){
        widget.ComboBox[] semuaFlacc = {FlaccWajah,FlaccKaki,FlaccAktivitas,FlaccMenangis,FlaccBersuara};
        int total = 0;
        for(widget.ComboBox cmb : semuaFlacc){
            Object v = cmb.getSelectedItem();
            String teks = v==null?"":v.toString();
            if(!teks.isEmpty()) total += Integer.parseInt(teks);
        }
        TFlaccTotal.setText(String.valueOf(total));
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
    private void initKepTambahanHal4(){
        panelKepEmpat = new widget.PanelBiasa();
        panelKepEmpat.setLayout(new java.awt.GridBagLayout());
        int row = 0;

        row = tambahBarisKepJudul(panelKepEmpat, row, "KONDISI PASIEN SAAT PINDAH / PULANG DARI IGD");
        // widget.Jam (JSpinner jam murni) - bukan widget.Tanggal lagi, krn
        // widget.Tanggal (uz.ncipro.calendar.JDateTimePicker) popup-nya
        // SELALU kalender tanggal, tidak pernah ada kontrol jam sama sekali
        // walau displayFormat "HH:mm:ss" (cuma ganti format TEKS-nya, bukan
        // popup-nya) - permintaan user, field "Pukul" harusnya nunjukin jam.
        DTPKpPukul = new widget.Jam();
        // Tanggal ikut ditampilkan & tersimpan (bukan cuma jam) - permintaan
        // user, krn bisa beda dari tanggal kunjungan (pasien masuk/pindah
        // lewat tengah malam). kp_pukul di DB diubah dari TIME ke DATETIME.
        DTPKpPukul.tampilkanTanggal();
        DTPKpPukul.setPreferredSize(new java.awt.Dimension(190,23));
        DTPKpPukul.setJam(new Date());
        row = tambahBarisKepField(panelKepEmpat, row, "Pukul", DTPKpPukul);

        // GCS/TD/Nadi/Suhu/Pernafasan/SpO2 ditata 3 baris x 2 kolom (pola
        // sama spt KEADAAN UMUM) - lebar field disamakan dgn Suhu (60px),
        // permintaan user.
        TKpGcsE = new widget.TextBox(); TKpGcsE.setPreferredSize(new java.awt.Dimension(60,23));
        TKpGcsV = new widget.TextBox(); TKpGcsV.setPreferredSize(new java.awt.Dimension(60,23));
        TKpGcsM = new widget.TextBox(); TKpGcsM.setPreferredSize(new java.awt.Dimension(60,23));
        widget.PanelBiasa panelKpGcs = new widget.PanelBiasa();
        panelKpGcs.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKpGcs.add(new widget.Label()); // spacer kecil (konsisten style baris lain)
        panelKpGcs.add(TKpGcsE); panelKpGcs.add(TKpGcsV); panelKpGcs.add(TKpGcsM);
        TKpTd = new widget.TextBox(); TKpTd.setPreferredSize(new java.awt.Dimension(60,23));
        TKpNadi = new widget.TextBox(); TKpNadi.setPreferredSize(new java.awt.Dimension(60,23));
        RbKpNadiReguler = new widget.RadioButton(); RbKpNadiReguler.setText("Reguler");
        RbKpNadiIreguler = new widget.RadioButton(); RbKpNadiIreguler.setText("Ireguler");
        bgKpNadiReguler = new javax.swing.ButtonGroup(); bgKpNadiReguler.add(RbKpNadiReguler); bgKpNadiReguler.add(RbKpNadiIreguler);
        widget.PanelBiasa panelKpNadi = new widget.PanelBiasa();
        panelKpNadi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKpNadi.add(TKpNadi); panelKpNadi.add(RbKpNadiReguler); panelKpNadi.add(RbKpNadiIreguler);
        TKpSuhu = new widget.TextBox(); TKpSuhu.setPreferredSize(new java.awt.Dimension(60,23));
        TKpPernafasan = new widget.TextBox(); TKpPernafasan.setPreferredSize(new java.awt.Dimension(60,23));
        TKpSpo = new widget.TextBox(); TKpSpo.setPreferredSize(new java.awt.Dimension(60,23));

        widget.PanelBiasa panelKpVital = new widget.PanelBiasa();
        panelKpVital.setLayout(new java.awt.GridBagLayout());
        int rowKpVital = 0;

        widget.Label lblKpGcs = new widget.Label(); lblKpGcs.setText("GCS (E/V/M) :");
        java.awt.GridBagConstraints cLblKpGcs = kepConstraint(0,rowKpVital); cLblKpGcs.anchor = java.awt.GridBagConstraints.EAST; cLblKpGcs.fill = java.awt.GridBagConstraints.NONE;
        panelKpVital.add(lblKpGcs, cLblKpGcs);
        java.awt.GridBagConstraints cKpGcs = kepConstraint(1,rowKpVital); cKpGcs.anchor = java.awt.GridBagConstraints.WEST; cKpGcs.fill = java.awt.GridBagConstraints.NONE; cKpGcs.weightx = 0;
        panelKpVital.add(panelKpGcs, cKpGcs);
        widget.Label lblKpTd = new widget.Label(); lblKpTd.setText("TD (mmHg) :");
        java.awt.GridBagConstraints cLblKpTd = kepConstraint(2,rowKpVital); cLblKpTd.anchor = java.awt.GridBagConstraints.EAST; cLblKpTd.fill = java.awt.GridBagConstraints.NONE; cLblKpTd.insets = new java.awt.Insets(3,20,3,5);
        panelKpVital.add(lblKpTd, cLblKpTd);
        java.awt.GridBagConstraints cKpTd = kepConstraint(3,rowKpVital); cKpTd.anchor = java.awt.GridBagConstraints.WEST; cKpTd.fill = java.awt.GridBagConstraints.NONE; cKpTd.weightx = 0;
        panelKpVital.add(TKpTd, cKpTd);
        rowKpVital++;

        widget.Label lblKpNadi = new widget.Label(); lblKpNadi.setText("Nadi (x/menit) :");
        java.awt.GridBagConstraints cLblKpNadi = kepConstraint(0,rowKpVital); cLblKpNadi.anchor = java.awt.GridBagConstraints.EAST; cLblKpNadi.fill = java.awt.GridBagConstraints.NONE;
        panelKpVital.add(lblKpNadi, cLblKpNadi);
        java.awt.GridBagConstraints cKpNadi = kepConstraint(1,rowKpVital); cKpNadi.anchor = java.awt.GridBagConstraints.WEST; cKpNadi.fill = java.awt.GridBagConstraints.NONE; cKpNadi.weightx = 0;
        panelKpVital.add(panelKpNadi, cKpNadi);
        widget.Label lblKpSuhu = new widget.Label(); lblKpSuhu.setText("Suhu (°C) :");
        java.awt.GridBagConstraints cLblKpSuhu = kepConstraint(2,rowKpVital); cLblKpSuhu.anchor = java.awt.GridBagConstraints.EAST; cLblKpSuhu.fill = java.awt.GridBagConstraints.NONE; cLblKpSuhu.insets = new java.awt.Insets(3,20,3,5);
        panelKpVital.add(lblKpSuhu, cLblKpSuhu);
        java.awt.GridBagConstraints cKpSuhu = kepConstraint(3,rowKpVital); cKpSuhu.anchor = java.awt.GridBagConstraints.WEST; cKpSuhu.fill = java.awt.GridBagConstraints.NONE; cKpSuhu.weightx = 0;
        panelKpVital.add(TKpSuhu, cKpSuhu);
        rowKpVital++;

        widget.Label lblKpPernafasan = new widget.Label(); lblKpPernafasan.setText("Pernafasan (x/menit) :");
        java.awt.GridBagConstraints cLblKpPernafasan = kepConstraint(0,rowKpVital); cLblKpPernafasan.anchor = java.awt.GridBagConstraints.EAST; cLblKpPernafasan.fill = java.awt.GridBagConstraints.NONE;
        panelKpVital.add(lblKpPernafasan, cLblKpPernafasan);
        java.awt.GridBagConstraints cKpPernafasan = kepConstraint(1,rowKpVital); cKpPernafasan.anchor = java.awt.GridBagConstraints.WEST; cKpPernafasan.fill = java.awt.GridBagConstraints.NONE; cKpPernafasan.weightx = 0;
        panelKpVital.add(TKpPernafasan, cKpPernafasan);
        widget.Label lblKpSpo = new widget.Label(); lblKpSpo.setText("SpO2 (%) :");
        java.awt.GridBagConstraints cLblKpSpo = kepConstraint(2,rowKpVital); cLblKpSpo.anchor = java.awt.GridBagConstraints.EAST; cLblKpSpo.fill = java.awt.GridBagConstraints.NONE; cLblKpSpo.insets = new java.awt.Insets(3,20,3,5);
        panelKpVital.add(lblKpSpo, cLblKpSpo);
        java.awt.GridBagConstraints cKpSpo = kepConstraint(3,rowKpVital); cKpSpo.anchor = java.awt.GridBagConstraints.WEST; cKpSpo.fill = java.awt.GridBagConstraints.NONE; cKpSpo.weightx = 0;
        panelKpVital.add(TKpSpo, cKpSpo);
        rowKpVital++;

        java.awt.GridBagConstraints cFillerKpVital = kepConstraint(4, 0);
        cFillerKpVital.gridheight = rowKpVital;
        cFillerKpVital.weightx = 1.0;
        cFillerKpVital.fill = java.awt.GridBagConstraints.BOTH;
        cFillerKpVital.insets = new java.awt.Insets(0,0,0,0);
        panelKpVital.add(javax.swing.Box.createHorizontalGlue(), cFillerKpVital);

        java.awt.GridBagConstraints cKpVitalPanel = kepConstraint(0, row);
        cKpVitalPanel.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cKpVitalPanel.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cKpVitalPanel.insets = new java.awt.Insets(0,0,0,0);
        panelKepEmpat.add(panelKpVital, cKpVitalPanel);
        row++;

        // Masuk Rumah Sakit & Petugas yang Menerima disejajarkan 1 baris
        // (permintaan user). Ruang & Petugas Penerima KEDUANYA dipilih lewat
        // dialog pencarian (pola sama BtnObatDiperiksa/BtnObatDiberikan di
        // atas) - bukan ComboBox/input manual - permintaan user.
        CekKpMasukRs = new widget.CekBox(); CekKpMasukRs.setText("Masuk Rumah Sakit, di Ruang");
        TKpRuang = new widget.TextBox(); TKpRuang.setPreferredSize(new java.awt.Dimension(220,23)); TKpRuang.setEditable(false);
        BtnKpRuang = new widget.Button();
        BtnKpRuang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnKpRuang.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariKamar dlg = new DlgCariKamar(null,false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if(dlg.getTable().getSelectedRow()!= -1){
                        TKpRuang.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),1).toString());
                    }
                    BtnKpRuang.requestFocus();
                }
            });
            dlg.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        widget.PanelBiasa panelKpMasukRs = new widget.PanelBiasa();
        panelKpMasukRs.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKpMasukRs.add(CekKpMasukRs); panelKpMasukRs.add(TKpRuang); panelKpMasukRs.add(BtnKpRuang);

        TKpPetugasPenerima = new widget.TextBox(); TKpPetugasPenerima.setPreferredSize(new java.awt.Dimension(70,23)); TKpPetugasPenerima.setEditable(false);
        NmKpPetugasPenerima = new widget.TextBox(); NmKpPetugasPenerima.setPreferredSize(new java.awt.Dimension(180,23)); NmKpPetugasPenerima.setEditable(false);
        BtnKpPetugasPenerima = new widget.Button();
        BtnKpPetugasPenerima.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnKpPetugasPenerima.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariPetugas dlg = new DlgCariPetugas(null,false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if(dlg.getTable().getSelectedRow()!= -1){
                        TKpPetugasPenerima.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),0).toString());
                        NmKpPetugasPenerima.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),1).toString());
                    }
                    BtnKpPetugasPenerima.requestFocus();
                }
            });
            dlg.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        widget.PanelBiasa panelKpPetugasPenerima = new widget.PanelBiasa();
        panelKpPetugasPenerima.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKpPetugasPenerima.add(TKpPetugasPenerima); panelKpPetugasPenerima.add(NmKpPetugasPenerima); panelKpPetugasPenerima.add(BtnKpPetugasPenerima);

        widget.PanelBiasa panelKpMasukPetugas = new widget.PanelBiasa();
        panelKpMasukPetugas.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints cKpMasukRs = kepConstraint(0,0); cKpMasukRs.gridwidth = 2; cKpMasukRs.anchor = java.awt.GridBagConstraints.WEST; cKpMasukRs.fill = java.awt.GridBagConstraints.NONE;
        panelKpMasukPetugas.add(panelKpMasukRs, cKpMasukRs);
        widget.Label lblKpPetugasPenerima = new widget.Label(); lblKpPetugasPenerima.setText("Petugas yang Menerima :");
        java.awt.GridBagConstraints cLblKpPetugasPenerima = kepConstraint(2,0); cLblKpPetugasPenerima.anchor = java.awt.GridBagConstraints.EAST; cLblKpPetugasPenerima.fill = java.awt.GridBagConstraints.NONE; cLblKpPetugasPenerima.insets = new java.awt.Insets(3,20,3,5);
        panelKpMasukPetugas.add(lblKpPetugasPenerima, cLblKpPetugasPenerima);
        java.awt.GridBagConstraints cKpPetugasPenerima = kepConstraint(3,0); cKpPetugasPenerima.anchor = java.awt.GridBagConstraints.WEST; cKpPetugasPenerima.fill = java.awt.GridBagConstraints.NONE; cKpPetugasPenerima.weightx = 0;
        panelKpMasukPetugas.add(panelKpPetugasPenerima, cKpPetugasPenerima);
        java.awt.GridBagConstraints cFillerKpMasukPetugas = kepConstraint(4, 0);
        cFillerKpMasukPetugas.weightx = 1.0;
        cFillerKpMasukPetugas.fill = java.awt.GridBagConstraints.BOTH;
        cFillerKpMasukPetugas.insets = new java.awt.Insets(0,0,0,0);
        panelKpMasukPetugas.add(javax.swing.Box.createHorizontalGlue(), cFillerKpMasukPetugas);

        java.awt.GridBagConstraints cKpMasukPetugasPanel = kepConstraint(0, row);
        cKpMasukPetugasPanel.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cKpMasukPetugasPanel.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cKpMasukPetugasPanel.insets = new java.awt.Insets(0,0,0,0);
        panelKepEmpat.add(panelKpMasukPetugas, cKpMasukPetugasPanel);
        row++;

        RbKpPendampingDokter = new widget.RadioButton(); RbKpPendampingDokter.setText("Dokter");
        RbKpPendampingPerawat = new widget.RadioButton(); RbKpPendampingPerawat.setText("Perawat");
        bgKpPendamping = new javax.swing.ButtonGroup(); bgKpPendamping.add(RbKpPendampingDokter); bgKpPendamping.add(RbKpPendampingPerawat);
        widget.PanelBiasa panelKpPendamping = new widget.PanelBiasa();
        panelKpPendamping.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKpPendamping.add(RbKpPendampingDokter); panelKpPendamping.add(RbKpPendampingPerawat);
        row = tambahBarisKepField(panelKepEmpat, row, "Pendamping", panelKpPendamping);

        RbKpTlKamarOperasi = new widget.RadioButton(); RbKpTlKamarOperasi.setText("Kamar Operasi");
        RbKpTlKamarBersalin = new widget.RadioButton(); RbKpTlKamarBersalin.setText("Kamar Bersalin");
        RbKpTlHcu = new widget.RadioButton(); RbKpTlHcu.setText("HCU");
        RbKpTlKamarJenazah = new widget.RadioButton(); RbKpTlKamarJenazah.setText("Kamar Jenazah");
        RbKpTlLainnya = new widget.RadioButton(); RbKpTlLainnya.setText("Lainnya");
        bgKpTindakanLanjutan = new javax.swing.ButtonGroup();
        bgKpTindakanLanjutan.add(RbKpTlKamarOperasi); bgKpTindakanLanjutan.add(RbKpTlKamarBersalin);
        bgKpTindakanLanjutan.add(RbKpTlHcu); bgKpTindakanLanjutan.add(RbKpTlKamarJenazah); bgKpTindakanLanjutan.add(RbKpTlLainnya);
        TKpTindakanLanjutanKet = new widget.TextBox(); TKpTindakanLanjutanKet.setPreferredSize(new java.awt.Dimension(150,23));
        widget.PanelBiasa panelKpTl = new widget.PanelBiasa();
        panelKpTl.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKpTl.add(RbKpTlKamarOperasi); panelKpTl.add(RbKpTlKamarBersalin); panelKpTl.add(RbKpTlHcu);
        panelKpTl.add(RbKpTlKamarJenazah); panelKpTl.add(RbKpTlLainnya); panelKpTl.add(TKpTindakanLanjutanKet);
        row = tambahBarisKepField(panelKepEmpat, row, "Tindakan Lanjutan di", panelKpTl);

        // Dirujuk & Alasan Dirujuk disejajarkan 1 baris (permintaan user).
        CekKpDirujuk = new widget.CekBox(); CekKpDirujuk.setText("Dirujuk ke RS");
        TKpDirujukKet = new widget.TextBox(); TKpDirujukKet.setPreferredSize(new java.awt.Dimension(150,23));
        widget.PanelBiasa panelKpDirujuk = new widget.PanelBiasa();
        panelKpDirujuk.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKpDirujuk.add(CekKpDirujuk); panelKpDirujuk.add(TKpDirujukKet);
        TKpDirujukAlasan = new widget.TextBox(); TKpDirujukAlasan.setPreferredSize(new java.awt.Dimension(300,23));

        widget.PanelBiasa panelKpDirujukAlasan = new widget.PanelBiasa();
        panelKpDirujukAlasan.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints cKpDirujuk = kepConstraint(0,0); cKpDirujuk.gridwidth = 2; cKpDirujuk.anchor = java.awt.GridBagConstraints.WEST; cKpDirujuk.fill = java.awt.GridBagConstraints.NONE;
        panelKpDirujukAlasan.add(panelKpDirujuk, cKpDirujuk);
        widget.Label lblKpDirujukAlasan = new widget.Label(); lblKpDirujukAlasan.setText("Alasan Dirujuk :");
        java.awt.GridBagConstraints cLblKpDirujukAlasan = kepConstraint(2,0); cLblKpDirujukAlasan.anchor = java.awt.GridBagConstraints.EAST; cLblKpDirujukAlasan.fill = java.awt.GridBagConstraints.NONE; cLblKpDirujukAlasan.insets = new java.awt.Insets(3,20,3,5);
        panelKpDirujukAlasan.add(lblKpDirujukAlasan, cLblKpDirujukAlasan);
        java.awt.GridBagConstraints cKpDirujukAlasan = kepConstraint(3,0); cKpDirujukAlasan.anchor = java.awt.GridBagConstraints.WEST; cKpDirujukAlasan.fill = java.awt.GridBagConstraints.NONE; cKpDirujukAlasan.weightx = 0;
        panelKpDirujukAlasan.add(TKpDirujukAlasan, cKpDirujukAlasan);
        java.awt.GridBagConstraints cFillerKpDirujukAlasan = kepConstraint(4, 0);
        cFillerKpDirujukAlasan.weightx = 1.0;
        cFillerKpDirujukAlasan.fill = java.awt.GridBagConstraints.BOTH;
        cFillerKpDirujukAlasan.insets = new java.awt.Insets(0,0,0,0);
        panelKpDirujukAlasan.add(javax.swing.Box.createHorizontalGlue(), cFillerKpDirujukAlasan);

        java.awt.GridBagConstraints cKpDirujukAlasanPanel = kepConstraint(0, row);
        cKpDirujukAlasanPanel.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cKpDirujukAlasanPanel.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cKpDirujukAlasanPanel.insets = new java.awt.Insets(0,0,0,0);
        panelKepEmpat.add(panelKpDirujukAlasan, cKpDirujukAlasanPanel);
        row++;

        // Dipulangkan, Meninggal, dan Death On Arrival disejajarkan 1
        // baris bertiga (permintaan user).
        CekKpDipulangkan = new widget.CekBox(); CekKpDipulangkan.setText("Dipulangkan, Pukul");
        DTPKpDipulangkanPukul = new widget.Jam();
        DTPKpDipulangkanPukul.tampilkanTanggal();
        DTPKpDipulangkanPukul.setPreferredSize(new java.awt.Dimension(190,23));
        DTPKpDipulangkanPukul.setJam(new Date());
        widget.PanelBiasa panelKpDipulangkan = new widget.PanelBiasa();
        panelKpDipulangkan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKpDipulangkan.add(CekKpDipulangkan); panelKpDipulangkan.add(DTPKpDipulangkanPukul);

        CekKpMeninggal = new widget.CekBox(); CekKpMeninggal.setText("Meninggal, Pukul");
        DTPKpMeninggalPukul = new widget.Jam();
        DTPKpMeninggalPukul.tampilkanTanggal();
        DTPKpMeninggalPukul.setPreferredSize(new java.awt.Dimension(190,23));
        DTPKpMeninggalPukul.setJam(new Date());
        widget.PanelBiasa panelKpMeninggal = new widget.PanelBiasa();
        panelKpMeninggal.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKpMeninggal.add(CekKpMeninggal); panelKpMeninggal.add(DTPKpMeninggalPukul);

        CekKpDoa = new widget.CekBox(); CekKpDoa.setText("Death On Arrival");

        widget.PanelBiasa panelKpAkhir = new widget.PanelBiasa();
        panelKpAkhir.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints cKpDipulangkan = kepConstraint(0,0); cKpDipulangkan.anchor = java.awt.GridBagConstraints.WEST; cKpDipulangkan.fill = java.awt.GridBagConstraints.NONE;
        panelKpAkhir.add(panelKpDipulangkan, cKpDipulangkan);
        java.awt.GridBagConstraints cKpMeninggal = kepConstraint(1,0); cKpMeninggal.anchor = java.awt.GridBagConstraints.WEST; cKpMeninggal.fill = java.awt.GridBagConstraints.NONE; cKpMeninggal.insets = new java.awt.Insets(3,20,3,5);
        panelKpAkhir.add(panelKpMeninggal, cKpMeninggal);
        java.awt.GridBagConstraints cKpDoa = kepConstraint(2,0); cKpDoa.anchor = java.awt.GridBagConstraints.WEST; cKpDoa.fill = java.awt.GridBagConstraints.NONE; cKpDoa.insets = new java.awt.Insets(3,20,3,5);
        panelKpAkhir.add(CekKpDoa, cKpDoa);
        java.awt.GridBagConstraints cFillerKpAkhir = kepConstraint(3, 0);
        cFillerKpAkhir.weightx = 1.0;
        cFillerKpAkhir.fill = java.awt.GridBagConstraints.BOTH;
        cFillerKpAkhir.insets = new java.awt.Insets(0,0,0,0);
        panelKpAkhir.add(javax.swing.Box.createHorizontalGlue(), cFillerKpAkhir);

        java.awt.GridBagConstraints cKpAkhirPanel = kepConstraint(0, row);
        cKpAkhirPanel.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cKpAkhirPanel.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cKpAkhirPanel.insets = new java.awt.Insets(0,0,0,0);
        panelKepEmpat.add(panelKpAkhir, cKpAkhirPanel);
        row++;

        row = tambahBarisKepJudul(panelKepEmpat, row, "Pendidikan Kesehatan Pasien Pulang");
        CekKpEdukasiMakanObat = new widget.CekBox(); CekKpEdukasiMakanObat.setText("Makan/Minum Obat Teratur");
        CekKpEdukasiLuka = new widget.CekBox(); CekKpEdukasiLuka.setText("Jaga Kebersihan Luka");
        java.awt.GridBagConstraints cKpEdu = kepConstraint(0,row); cKpEdu.gridwidth=2;
        panelKepEmpat.add(panelChecklistKep(2, CekKpEdukasiMakanObat,CekKpEdukasiLuka), cKpEdu);
        row++;
        // Diet & Lain-lain disejajarkan 1 baris (permintaan user).
        CekKpEdukasiDiet = new widget.CekBox(); CekKpEdukasiDiet.setText("Diet");
        TKpEdukasiDietKet = new widget.TextBox(); TKpEdukasiDietKet.setPreferredSize(new java.awt.Dimension(200,23));
        widget.PanelBiasa panelKpEdukasiDiet = new widget.PanelBiasa();
        panelKpEdukasiDiet.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKpEdukasiDiet.add(CekKpEdukasiDiet); panelKpEdukasiDiet.add(TKpEdukasiDietKet);
        CekKpEdukasiLain = new widget.CekBox(); CekKpEdukasiLain.setText("Lain-lain");
        TKpEdukasiLainKet = new widget.TextBox(); TKpEdukasiLainKet.setPreferredSize(new java.awt.Dimension(200,23));
        widget.PanelBiasa panelKpEdukasiLain = new widget.PanelBiasa();
        panelKpEdukasiLain.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKpEdukasiLain.add(CekKpEdukasiLain); panelKpEdukasiLain.add(TKpEdukasiLainKet);

        widget.PanelBiasa panelKpEdukasiDietLain = new widget.PanelBiasa();
        panelKpEdukasiDietLain.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints cKpEdukasiDiet = kepConstraint(0,0); cKpEdukasiDiet.anchor = java.awt.GridBagConstraints.WEST; cKpEdukasiDiet.fill = java.awt.GridBagConstraints.NONE;
        panelKpEdukasiDietLain.add(panelKpEdukasiDiet, cKpEdukasiDiet);
        java.awt.GridBagConstraints cKpEdukasiLain = kepConstraint(1,0); cKpEdukasiLain.anchor = java.awt.GridBagConstraints.WEST; cKpEdukasiLain.fill = java.awt.GridBagConstraints.NONE; cKpEdukasiLain.insets = new java.awt.Insets(3,20,3,5);
        panelKpEdukasiDietLain.add(panelKpEdukasiLain, cKpEdukasiLain);
        java.awt.GridBagConstraints cFillerKpEdukasiDietLain = kepConstraint(2, 0);
        cFillerKpEdukasiDietLain.weightx = 1.0;
        cFillerKpEdukasiDietLain.fill = java.awt.GridBagConstraints.BOTH;
        cFillerKpEdukasiDietLain.insets = new java.awt.Insets(0,0,0,0);
        panelKpEdukasiDietLain.add(javax.swing.Box.createHorizontalGlue(), cFillerKpEdukasiDietLain);

        java.awt.GridBagConstraints cKpEdukasiDietLainPanel = kepConstraint(0, row);
        cKpEdukasiDietLainPanel.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cKpEdukasiDietLainPanel.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cKpEdukasiDietLainPanel.insets = new java.awt.Insets(0,0,0,0);
        panelKepEmpat.add(panelKpEdukasiDietLain, cKpEdukasiDietLainPanel);
        row++;

        // Dokter & Perawat yang bertanggung jawab dipindah ke header
        // (permintaan user) - lihat initKepTambahan() utk field Dokter,
        // header "Petugas" dipakai langsung sbg Perawat/Bidan di
        // nilaiKep(). Tidak ada lagi baris input di section ini.

        java.awt.Dimension ukuranKepEmpat = panelKepEmpat.getPreferredSize();
        java.awt.Dimension ukuranKepDua4 = panelKepDua.getPreferredSize();
        java.awt.Dimension ukuranKepTiga4 = panelKepTiga.getPreferredSize();
        java.awt.Dimension ukuranObatInfus4 = panelKepObat.getPreferredSize();
        int yBawahEmpat = yBawahLama + ukuranKepDua4.height + 10 + ukuranKepTiga4.height + 10 + ukuranObatInfus4.height + 10;
        FormInput.add(panelKepEmpat);
        panelKepEmpat.setBounds(5, yBawahEmpat, ukuranKepEmpat.width, ukuranKepEmpat.height);
        lebarFormInput = Math.max(lebarFormInput, 5 + ukuranKepEmpat.width + 20);
        FormInput.setPreferredSize(new java.awt.Dimension(lebarFormInput, yBawahEmpat + ukuranKepEmpat.height + 20));
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
    private void initObatInfusTambahan(){
        panelKepObat = new widget.PanelBiasa();
        panelKepObat.setLayout(new java.awt.GridBagLayout());
        int row = 0;
        row = tambahBarisKepJudul(panelKepObat, row, "PEMBERIAN OBAT / INFUS");

        tabModeObatInfus = new DefaultTableModel(null, new Object[]{"Pukul","Nama Obat/Infus","Dosis","Rute","Diperiksa Oleh","Diberikan Oleh"}){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObatInfus = new widget.Table();
        tbObatInfus.setModel(tabModeObatInfus);
        widget.ScrollPane scrollObatInfus = new widget.ScrollPane();
        scrollObatInfus.setViewportView(tbObatInfus);
        scrollObatInfus.setPreferredSize(new java.awt.Dimension(800,120));
        java.awt.GridBagConstraints cTabelObat = kepConstraint(0,row); cTabelObat.gridwidth=2;
        panelKepObat.add(scrollObatInfus, cTabelObat);
        row++;

        // Pukul|Nama Obat/Infus & Dosis|Rute ditata 2 kolom, 2 baris dalam
        // panel tersendiri (pola sama spt KEADAAN UMUM) - permintaan user.
        DTPObatPukul = new widget.Jam();
        DTPObatPukul.setPreferredSize(new java.awt.Dimension(150,23));
        DTPObatPukul.setJam(new Date());
        // Nama Obat/Infus - ambil dari data master `databarang` lewat
        // DlgCariNamaObat (baru, murni SELECT read-only) supaya nama
        // konsisten dgn data barang, TANPA mengurangi stok obat yg ada
        // (permintaan eksplisit user - dialog itu tidak pernah menyentuh
        // tabel stok `gudangbarang`/`data_batch` sama sekali). TObatNama
        // tetap TextBox biasa (bisa diketik manual juga), cuma diisi
        // otomatis kalau user pilih lewat tombol cari.
        TObatNama = new widget.TextBox(); TObatNama.setPreferredSize(new java.awt.Dimension(200,23));
        BtnObatNama = new widget.Button();
        BtnObatNama.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnObatNama.setPreferredSize(new java.awt.Dimension(28,23));
        BtnObatNama.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariNamaObat dlgObat = new DlgCariNamaObat(null,false);
            dlgObat.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if(dlgObat.getTable().getSelectedRow()!= -1){
                        TObatNama.setText(dlgObat.getTable().getValueAt(dlgObat.getTable().getSelectedRow(),1).toString());
                    }
                    BtnObatNama.requestFocus();
                }
            });
            dlgObat.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dlgObat.setLocationRelativeTo(internalFrame1);
            dlgObat.setVisible(true);
        });
        widget.PanelBiasa panelObatNama = new widget.PanelBiasa();
        panelObatNama.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelObatNama.add(TObatNama); panelObatNama.add(BtnObatNama);
        TObatDosis = new widget.TextBox(); TObatDosis.setPreferredSize(new java.awt.Dimension(150,23));
        TObatRute = new widget.TextBox(); TObatRute.setPreferredSize(new java.awt.Dimension(150,23));

        widget.PanelBiasa panelObatBaris = new widget.PanelBiasa();
        panelObatBaris.setLayout(new java.awt.GridBagLayout());
        int rowObat = 0;

        widget.Label lblObatPukul = new widget.Label(); lblObatPukul.setText("Pukul :");
        java.awt.GridBagConstraints cLblObatPukul = kepConstraint(0,rowObat); cLblObatPukul.anchor = java.awt.GridBagConstraints.EAST; cLblObatPukul.fill = java.awt.GridBagConstraints.NONE;
        panelObatBaris.add(lblObatPukul, cLblObatPukul);
        java.awt.GridBagConstraints cObatPukul = kepConstraint(1,rowObat); cObatPukul.anchor = java.awt.GridBagConstraints.WEST; cObatPukul.fill = java.awt.GridBagConstraints.NONE; cObatPukul.weightx = 0;
        panelObatBaris.add(DTPObatPukul, cObatPukul);
        widget.Label lblObatNama = new widget.Label(); lblObatNama.setText("Nama Obat/Infus :");
        java.awt.GridBagConstraints cLblObatNama = kepConstraint(2,rowObat); cLblObatNama.anchor = java.awt.GridBagConstraints.EAST; cLblObatNama.fill = java.awt.GridBagConstraints.NONE; cLblObatNama.insets = new java.awt.Insets(3,20,3,5);
        panelObatBaris.add(lblObatNama, cLblObatNama);
        java.awt.GridBagConstraints cObatNama = kepConstraint(3,rowObat); cObatNama.anchor = java.awt.GridBagConstraints.WEST; cObatNama.fill = java.awt.GridBagConstraints.NONE; cObatNama.weightx = 0;
        panelObatBaris.add(panelObatNama, cObatNama);
        rowObat++;

        widget.Label lblObatDosis = new widget.Label(); lblObatDosis.setText("Dosis :");
        java.awt.GridBagConstraints cLblObatDosis = kepConstraint(0,rowObat); cLblObatDosis.anchor = java.awt.GridBagConstraints.EAST; cLblObatDosis.fill = java.awt.GridBagConstraints.NONE;
        panelObatBaris.add(lblObatDosis, cLblObatDosis);
        java.awt.GridBagConstraints cObatDosis = kepConstraint(1,rowObat); cObatDosis.anchor = java.awt.GridBagConstraints.WEST; cObatDosis.fill = java.awt.GridBagConstraints.NONE; cObatDosis.weightx = 0;
        panelObatBaris.add(TObatDosis, cObatDosis);
        widget.Label lblObatRute = new widget.Label(); lblObatRute.setText("Rute :");
        java.awt.GridBagConstraints cLblObatRute = kepConstraint(2,rowObat); cLblObatRute.anchor = java.awt.GridBagConstraints.EAST; cLblObatRute.fill = java.awt.GridBagConstraints.NONE; cLblObatRute.insets = new java.awt.Insets(3,20,3,5);
        panelObatBaris.add(lblObatRute, cLblObatRute);
        java.awt.GridBagConstraints cObatRute = kepConstraint(3,rowObat); cObatRute.anchor = java.awt.GridBagConstraints.WEST; cObatRute.fill = java.awt.GridBagConstraints.NONE; cObatRute.weightx = 0;
        panelObatBaris.add(TObatRute, cObatRute);
        rowObat++;

        java.awt.GridBagConstraints cFillerObat = kepConstraint(4, 0);
        cFillerObat.gridheight = rowObat;
        cFillerObat.weightx = 1.0;
        cFillerObat.fill = java.awt.GridBagConstraints.BOTH;
        cFillerObat.insets = new java.awt.Insets(0,0,0,0);
        panelObatBaris.add(javax.swing.Box.createHorizontalGlue(), cFillerObat);

        java.awt.GridBagConstraints cObatBarisPanel = kepConstraint(0, row);
        cObatBarisPanel.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cObatBarisPanel.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cObatBarisPanel.insets = new java.awt.Insets(0,0,0,0);
        panelKepObat.add(panelObatBaris, cObatBarisPanel);
        row++;

        KdObatDiperiksa = new widget.TextBox(); KdObatDiperiksa.setPreferredSize(new java.awt.Dimension(70,23)); KdObatDiperiksa.setEditable(false);
        NmObatDiperiksa = new widget.TextBox(); NmObatDiperiksa.setPreferredSize(new java.awt.Dimension(200,23)); NmObatDiperiksa.setEditable(false);
        BtnObatDiperiksa = new widget.Button();
        BtnObatDiperiksa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnObatDiperiksa.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariPetugas dlg = new DlgCariPetugas(null,false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if(dlg.getTable().getSelectedRow()!= -1){
                        KdObatDiperiksa.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),0).toString());
                        NmObatDiperiksa.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),1).toString());
                    }
                    BtnObatDiperiksa.requestFocus();
                }
            });
            dlg.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        widget.PanelBiasa panelObatDiperiksa = new widget.PanelBiasa();
        panelObatDiperiksa.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelObatDiperiksa.add(KdObatDiperiksa); panelObatDiperiksa.add(NmObatDiperiksa); panelObatDiperiksa.add(BtnObatDiperiksa);

        KdObatDiberikan = new widget.TextBox(); KdObatDiberikan.setPreferredSize(new java.awt.Dimension(70,23)); KdObatDiberikan.setEditable(false);
        NmObatDiberikan = new widget.TextBox(); NmObatDiberikan.setPreferredSize(new java.awt.Dimension(200,23)); NmObatDiberikan.setEditable(false);
        BtnObatDiberikan = new widget.Button();
        BtnObatDiberikan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnObatDiberikan.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariPetugas dlg = new DlgCariPetugas(null,false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if(dlg.getTable().getSelectedRow()!= -1){
                        KdObatDiberikan.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),0).toString());
                        NmObatDiberikan.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),1).toString());
                    }
                    BtnObatDiberikan.requestFocus();
                }
            });
            dlg.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        widget.PanelBiasa panelObatDiberikan = new widget.PanelBiasa();
        panelObatDiberikan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelObatDiberikan.add(KdObatDiberikan); panelObatDiberikan.add(NmObatDiberikan); panelObatDiberikan.add(BtnObatDiberikan);

        // Diperiksa Oleh & Diberikan Oleh disejajarkan 1 baris (permintaan
        // user), pola sama spt panelObatBaris (Pukul/Nama/Dosis/Rute) di atas.
        widget.PanelBiasa panelObatPetugas = new widget.PanelBiasa();
        panelObatPetugas.setLayout(new java.awt.GridBagLayout());
        widget.Label lblObatDiperiksa = new widget.Label(); lblObatDiperiksa.setText("Diperiksa Oleh :");
        java.awt.GridBagConstraints cLblObatDiperiksa = kepConstraint(0,0); cLblObatDiperiksa.anchor = java.awt.GridBagConstraints.EAST; cLblObatDiperiksa.fill = java.awt.GridBagConstraints.NONE;
        panelObatPetugas.add(lblObatDiperiksa, cLblObatDiperiksa);
        java.awt.GridBagConstraints cObatDiperiksa = kepConstraint(1,0); cObatDiperiksa.anchor = java.awt.GridBagConstraints.WEST; cObatDiperiksa.fill = java.awt.GridBagConstraints.NONE; cObatDiperiksa.weightx = 0;
        panelObatPetugas.add(panelObatDiperiksa, cObatDiperiksa);
        widget.Label lblObatDiberikan = new widget.Label(); lblObatDiberikan.setText("Diberikan Oleh :");
        java.awt.GridBagConstraints cLblObatDiberikan = kepConstraint(2,0); cLblObatDiberikan.anchor = java.awt.GridBagConstraints.EAST; cLblObatDiberikan.fill = java.awt.GridBagConstraints.NONE; cLblObatDiberikan.insets = new java.awt.Insets(3,20,3,5);
        panelObatPetugas.add(lblObatDiberikan, cLblObatDiberikan);
        java.awt.GridBagConstraints cObatDiberikan = kepConstraint(3,0); cObatDiberikan.anchor = java.awt.GridBagConstraints.WEST; cObatDiberikan.fill = java.awt.GridBagConstraints.NONE; cObatDiberikan.weightx = 0;
        panelObatPetugas.add(panelObatDiberikan, cObatDiberikan);
        java.awt.GridBagConstraints cFillerObatPetugas = kepConstraint(4, 0);
        cFillerObatPetugas.weightx = 1.0;
        cFillerObatPetugas.fill = java.awt.GridBagConstraints.BOTH;
        cFillerObatPetugas.insets = new java.awt.Insets(0,0,0,0);
        panelObatPetugas.add(javax.swing.Box.createHorizontalGlue(), cFillerObatPetugas);

        java.awt.GridBagConstraints cObatPetugasPanel = kepConstraint(0, row);
        cObatPetugasPanel.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cObatPetugasPanel.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cObatPetugasPanel.insets = new java.awt.Insets(0,0,0,0);
        panelKepObat.add(panelObatPetugas, cObatPetugasPanel);
        row++;

        // widget.Button (glass style) butuh .setPreferredSize() eksplisit
        // spy latar/rounded-background-nya kelihatan - tanpa ini cuma
        // teks polos yg tampil (bug yg sama spt TextBox/ComboBox, lihat
        // BtnSimpan yg sudah benar pakai setPreferredSize(100,30) sbg
        // acuan). Ikon disamakan dgn gaya tombol lain di aplikasi ini.
        BtnTambahObat = new widget.Button(); BtnTambahObat.setText("Tambah");
        BtnTambahObat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png")));
        BtnTambahObat.setPreferredSize(new java.awt.Dimension(110,28));
        BtnTambahObat.addActionListener((java.awt.event.ActionEvent evt) -> simpanObat());
        BtnHapusObat = new widget.Button(); BtnHapusObat.setText("Hapus Baris Terpilih");
        BtnHapusObat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Delete.png")));
        BtnHapusObat.setPreferredSize(new java.awt.Dimension(180,28));
        BtnHapusObat.addActionListener((java.awt.event.ActionEvent evt) -> hapusObat());
        widget.PanelBiasa panelObatTombol = new widget.PanelBiasa();
        panelObatTombol.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelObatTombol.add(BtnTambahObat); panelObatTombol.add(BtnHapusObat);
        row = tambahBarisKepField(panelKepObat, row, "", panelObatTombol);

        java.awt.Dimension ukuranKepObat = panelKepObat.getPreferredSize();
        java.awt.Dimension ukuranKepDuaO = panelKepDua.getPreferredSize();
        java.awt.Dimension ukuranKepTigaO = panelKepTiga.getPreferredSize();
        int yBawahObat = yBawahLama + ukuranKepDuaO.height + 10 + ukuranKepTigaO.height + 10;
        FormInput.add(panelKepObat);
        panelKepObat.setBounds(5, yBawahObat, ukuranKepObat.width, ukuranKepObat.height);
        lebarFormInput = Math.max(lebarFormInput, 5 + ukuranKepObat.width + 20);
        FormInput.setPreferredSize(new java.awt.Dimension(lebarFormInput, yBawahObat + ukuranKepObat.height + 20));
    }

    /**
     * [Lanjutan RM 03-6] Muat ulang daftar Pemberian Obat/Infus utk
     * no_rawat yang sedang dibuka - dipanggil dari isRawat()/getData().
     */
    private void tampilObatInfus(String noRawat){
        Valid.tabelKosong(tabModeObatInfus);
        try{
            PreparedStatement psObat = koneksi.prepareStatement(
                "select penilaian_awal_keperawatan_igd_obat.*, "+
                "p1.nama as nama_diperiksa, p2.nama as nama_diberikan "+
                "from penilaian_awal_keperawatan_igd_obat "+
                "left join petugas p1 on p1.nip=penilaian_awal_keperawatan_igd_obat.nip_diperiksa "+
                "left join petugas p2 on p2.nip=penilaian_awal_keperawatan_igd_obat.nip_diberikan "+
                "where penilaian_awal_keperawatan_igd_obat.no_rawat=? order by tanggal,pukul");
            try{
                psObat.setString(1,noRawat);
                ResultSet rsObat = psObat.executeQuery();
                try{
                    while(rsObat.next()){
                        tabModeObatInfus.addRow(new Object[]{
                            rsObat.getString("pukul"),rsObat.getString("nama_obat"),rsObat.getString("dosis"),rsObat.getString("rute"),
                            rsObat.getString("nama_diperiksa"),rsObat.getString("nama_diberikan")
                        });
                    }
                } finally { if(rsObat!=null){ rsObat.close(); } }
            } finally { if(psObat!=null){ psObat.close(); } }
        }catch(Exception e){
            System.out.println("Notif Tampil Obat Infus : "+e);
        }
    }

    private void emptTeksObatInfus(){
        DTPObatPukul.setJam(new Date());
        TObatNama.setText("");
        TObatDosis.setText("");
        TObatRute.setText("");
        KdObatDiperiksa.setText("");
        NmObatDiperiksa.setText("");
        KdObatDiberikan.setText("");
        NmObatDiberikan.setText("");
    }

    private void simpanObat(){
        if(TNoRw.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Silahkan pilih pasien (No.Rawat) terlebih dahulu..!!");
            return;
        }
        if(TObatNama.getText().trim().equals("")){
            Valid.textKosong(TObatNama,"Nama Obat/Infus");
            return;
        }
        try{
            PreparedStatement psSimpanObat = koneksi.prepareStatement(
                "insert into penilaian_awal_keperawatan_igd_obat (no_rawat,tanggal,pukul,nama_obat,dosis,rute,nip_diperiksa,nip_diberikan) values (?,?,?,?,?,?,?,?)");
            try{
                psSimpanObat.setString(1,TNoRw.getText());
                psSimpanObat.setString(2,Valid.SetTgl(TglAsuhan.getSelectedItem()+""));
                psSimpanObat.setString(3,DTPObatPukul.getTeksJam());
                psSimpanObat.setString(4,TObatNama.getText());
                psSimpanObat.setString(5,TObatDosis.getText());
                psSimpanObat.setString(6,TObatRute.getText());
                psSimpanObat.setString(7,KdObatDiperiksa.getText());
                psSimpanObat.setString(8,KdObatDiberikan.getText());
                psSimpanObat.executeUpdate();
                tabModeObatInfus.addRow(new Object[]{
                    DTPObatPukul.getTeksJam(),TObatNama.getText(),TObatDosis.getText(),TObatRute.getText(),
                    NmObatDiperiksa.getText(),NmObatDiberikan.getText()
                });
                emptTeksObatInfus();
            } finally { if(psSimpanObat!=null){ psSimpanObat.close(); } }
        }catch(Exception e){
            System.out.println("Notif Simpan Obat Infus : "+e);
            JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan. Kemungkinan Pukul+Nama Obat yang sama sudah dimasukkan sebelumnya...!");
        }
    }

    private void hapusObat(){
        if(tbObatInfus.getSelectedRow()==-1){
            JOptionPane.showMessageDialog(null,"Silahkan pilih baris yang mau dihapus terlebih dahulu..!!");
            return;
        }
        try{
            PreparedStatement psHapusObat = koneksi.prepareStatement(
                "delete from penilaian_awal_keperawatan_igd_obat where no_rawat=? and tanggal=? and pukul=? and nama_obat=?");
            try{
                psHapusObat.setString(1,TNoRw.getText());
                psHapusObat.setString(2,Valid.SetTgl(TglAsuhan.getSelectedItem()+""));
                psHapusObat.setString(3,tbObatInfus.getValueAt(tbObatInfus.getSelectedRow(),0).toString());
                psHapusObat.setString(4,tbObatInfus.getValueAt(tbObatInfus.getSelectedRow(),1).toString());
                psHapusObat.executeUpdate();
                tabModeObatInfus.removeRow(tbObatInfus.getSelectedRow());
            } finally { if(psHapusObat!=null){ psHapusObat.close(); } }
        }catch(Exception e){
            System.out.println("Notif Hapus Obat Infus : "+e);
            JOptionPane.showMessageDialog(null,"Maaf, gagal menghapus..!!");
        }
    }

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{            
            Valid.pindah(evt,TCari,BtnPetugas);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(KeluhanUtama.getText().trim().equals("")){
            Valid.textKosong(KeluhanUtama,"Riwayat Penyakit Sekarang");
        }else if(NmPetugas.getText().trim().equals("")){
            Valid.textKosong(BtnPetugas,"Petugas");
        }else{
            if(akses.getkode().equals("Admin Utama")){
                simpan();
            }else{
                if(TanggalRegistrasi.getText().equals("")){
                    TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                }
                if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19))==true){
                    simpan();
                }
            }
        }
    
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,Rencana,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(tbObat.getSelectedRow()>-1){
            if(akses.getkode().equals("Admin Utama")){
                hapus();
            }else{
                if(KdPetugas.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString())){
                    if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString(),Sequel.ambiltanggalsekarang())==true){
                        hapus();
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"Hanya bisa dihapus oleh petugas yang bersangkutan..!!");
                }
            }
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
        }            
            
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(KeluhanUtama.getText().trim().equals("")){
            Valid.textKosong(KeluhanUtama,"Riwayat Penyakit Sekarang");
        }else if(NmPetugas.getText().trim().equals("")){
            Valid.textKosong(BtnPetugas,"Petugas");
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else{
                    if(KdPetugas.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString())){
                        if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString(),Sequel.ambiltanggalsekarang())==true){
                            if(TanggalRegistrasi.getText().equals("")){
                                TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                            }
                            if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19))==true){
                                ganti();
                            }
                        }
                    }else{
                        JOptionPane.showMessageDialog(null,"Hanya bisa diganti oleh petugas yang bersangkutan..!!");
                    }
                }
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
            }   
        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnKeluarActionPerformed(null);
        }else{Valid.pindah(evt,BtnEdit,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            try{
                if(TCari.getText().equals("")){
                    ps=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,pasien.agama,bahasa_pasien.nama_bahasa,cacat_fisik.nama_cacat,penilaian_awal_keperawatan_igd.tanggal,penilaian_awal_keperawatan_igd.informasi,"+
                            "penilaian_awal_keperawatan_igd.keluhan_utama,penilaian_awal_keperawatan_igd.rpd,penilaian_awal_keperawatan_igd.rpo,penilaian_awal_keperawatan_igd.status_kehamilan,penilaian_awal_keperawatan_igd.gravida,penilaian_awal_keperawatan_igd.para,"+
                            "penilaian_awal_keperawatan_igd.abortus,penilaian_awal_keperawatan_igd.hpht,penilaian_awal_keperawatan_igd.tekanan,penilaian_awal_keperawatan_igd.pupil,penilaian_awal_keperawatan_igd.neurosensorik,penilaian_awal_keperawatan_igd.integumen,penilaian_awal_keperawatan_igd.turgor,"+ 
                            "penilaian_awal_keperawatan_igd.edema,penilaian_awal_keperawatan_igd.mukosa,penilaian_awal_keperawatan_igd.perdarahan,penilaian_awal_keperawatan_igd.jumlah_perdarahan,penilaian_awal_keperawatan_igd.warna_perdarahan,penilaian_awal_keperawatan_igd.intoksikasi,"+
                            "penilaian_awal_keperawatan_igd.bab,penilaian_awal_keperawatan_igd.xbab,penilaian_awal_keperawatan_igd.kbab,penilaian_awal_keperawatan_igd.wbab,penilaian_awal_keperawatan_igd.bak,penilaian_awal_keperawatan_igd.xbak,penilaian_awal_keperawatan_igd.wbak,"+
                            "penilaian_awal_keperawatan_igd.lbak,penilaian_awal_keperawatan_igd.psikologis,penilaian_awal_keperawatan_igd.jiwa,penilaian_awal_keperawatan_igd.perilaku,penilaian_awal_keperawatan_igd.dilaporkan,penilaian_awal_keperawatan_igd.sebutkan,penilaian_awal_keperawatan_igd.hubungan,pasien.stts_nikah,"+ 
                            "penilaian_awal_keperawatan_igd.tinggal_dengan,penilaian_awal_keperawatan_igd.ket_tinggal,pasien.pekerjaan,penjab.png_jawab,penilaian_awal_keperawatan_igd.budaya,penilaian_awal_keperawatan_igd.ket_budaya,pasien.pnd,penilaian_awal_keperawatan_igd.pendidikan_pj,penilaian_awal_keperawatan_igd.ket_pendidikan_pj,"+  
                            "penilaian_awal_keperawatan_igd.edukasi,penilaian_awal_keperawatan_igd.ket_edukasi,penilaian_awal_keperawatan_igd.kemampuan,penilaian_awal_keperawatan_igd.aktifitas,penilaian_awal_keperawatan_igd.alat_bantu,penilaian_awal_keperawatan_igd.ket_bantu,"+
                            "penilaian_awal_keperawatan_igd.nyeri,penilaian_awal_keperawatan_igd.provokes,penilaian_awal_keperawatan_igd.ket_provokes,penilaian_awal_keperawatan_igd.quality,penilaian_awal_keperawatan_igd.ket_quality,penilaian_awal_keperawatan_igd.lokasi,penilaian_awal_keperawatan_igd.menyebar,"+
                            "penilaian_awal_keperawatan_igd.skala_nyeri,penilaian_awal_keperawatan_igd.durasi,penilaian_awal_keperawatan_igd.nyeri_hilang,penilaian_awal_keperawatan_igd.ket_nyeri,penilaian_awal_keperawatan_igd.pada_dokter,penilaian_awal_keperawatan_igd.ket_dokter,"+
                            "penilaian_awal_keperawatan_igd.berjalan_a,penilaian_awal_keperawatan_igd.berjalan_b,penilaian_awal_keperawatan_igd.berjalan_c,penilaian_awal_keperawatan_igd.hasil,penilaian_awal_keperawatan_igd.lapor,penilaian_awal_keperawatan_igd.ket_lapor,"+
                            "penilaian_awal_keperawatan_igd.rencana,penilaian_awal_keperawatan_igd.nip,petugas.nama "+
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join penilaian_awal_keperawatan_igd on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat "+
                            "inner join petugas on penilaian_awal_keperawatan_igd.nip=petugas.nip "+
                            "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                            "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj "+
                            "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik where "+
                            "penilaian_awal_keperawatan_igd.tanggal between ? and ? order by penilaian_awal_keperawatan_igd.tanggal");
                }else{
                    ps=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,pasien.agama,bahasa_pasien.nama_bahasa,cacat_fisik.nama_cacat,penilaian_awal_keperawatan_igd.tanggal,penilaian_awal_keperawatan_igd.informasi,"+
                            "penilaian_awal_keperawatan_igd.keluhan_utama,penilaian_awal_keperawatan_igd.rpd,penilaian_awal_keperawatan_igd.rpo,penilaian_awal_keperawatan_igd.status_kehamilan,penilaian_awal_keperawatan_igd.gravida,penilaian_awal_keperawatan_igd.para,"+
                            "penilaian_awal_keperawatan_igd.abortus,penilaian_awal_keperawatan_igd.hpht,penilaian_awal_keperawatan_igd.tekanan,penilaian_awal_keperawatan_igd.pupil,penilaian_awal_keperawatan_igd.neurosensorik,penilaian_awal_keperawatan_igd.integumen,penilaian_awal_keperawatan_igd.turgor,"+ 
                            "penilaian_awal_keperawatan_igd.edema,penilaian_awal_keperawatan_igd.mukosa,penilaian_awal_keperawatan_igd.perdarahan,penilaian_awal_keperawatan_igd.jumlah_perdarahan,penilaian_awal_keperawatan_igd.warna_perdarahan,penilaian_awal_keperawatan_igd.intoksikasi,"+
                            "penilaian_awal_keperawatan_igd.bab,penilaian_awal_keperawatan_igd.xbab,penilaian_awal_keperawatan_igd.kbab,penilaian_awal_keperawatan_igd.wbab,penilaian_awal_keperawatan_igd.bak,penilaian_awal_keperawatan_igd.xbak,penilaian_awal_keperawatan_igd.wbak,"+
                            "penilaian_awal_keperawatan_igd.lbak,penilaian_awal_keperawatan_igd.psikologis,penilaian_awal_keperawatan_igd.jiwa,penilaian_awal_keperawatan_igd.perilaku,penilaian_awal_keperawatan_igd.dilaporkan,penilaian_awal_keperawatan_igd.sebutkan,penilaian_awal_keperawatan_igd.hubungan,pasien.stts_nikah,"+ 
                            "penilaian_awal_keperawatan_igd.tinggal_dengan,penilaian_awal_keperawatan_igd.ket_tinggal,pasien.pekerjaan,penjab.png_jawab,penilaian_awal_keperawatan_igd.budaya,penilaian_awal_keperawatan_igd.ket_budaya,pasien.pnd,penilaian_awal_keperawatan_igd.pendidikan_pj,penilaian_awal_keperawatan_igd.ket_pendidikan_pj,"+  
                            "penilaian_awal_keperawatan_igd.edukasi,penilaian_awal_keperawatan_igd.ket_edukasi,penilaian_awal_keperawatan_igd.kemampuan,penilaian_awal_keperawatan_igd.aktifitas,penilaian_awal_keperawatan_igd.alat_bantu,penilaian_awal_keperawatan_igd.ket_bantu,"+
                            "penilaian_awal_keperawatan_igd.nyeri,penilaian_awal_keperawatan_igd.provokes,penilaian_awal_keperawatan_igd.ket_provokes,penilaian_awal_keperawatan_igd.quality,penilaian_awal_keperawatan_igd.ket_quality,penilaian_awal_keperawatan_igd.lokasi,penilaian_awal_keperawatan_igd.menyebar,"+
                            "penilaian_awal_keperawatan_igd.skala_nyeri,penilaian_awal_keperawatan_igd.durasi,penilaian_awal_keperawatan_igd.nyeri_hilang,penilaian_awal_keperawatan_igd.ket_nyeri,penilaian_awal_keperawatan_igd.pada_dokter,penilaian_awal_keperawatan_igd.ket_dokter,"+
                            "penilaian_awal_keperawatan_igd.berjalan_a,penilaian_awal_keperawatan_igd.berjalan_b,penilaian_awal_keperawatan_igd.berjalan_c,penilaian_awal_keperawatan_igd.hasil,penilaian_awal_keperawatan_igd.lapor,penilaian_awal_keperawatan_igd.ket_lapor,"+
                            "penilaian_awal_keperawatan_igd.rencana,penilaian_awal_keperawatan_igd.nip,petugas.nama "+
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join penilaian_awal_keperawatan_igd on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat "+
                            "inner join petugas on penilaian_awal_keperawatan_igd.nip=petugas.nip "+
                            "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                            "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj "+
                            "inner join cacat_fisik on cacat_fisik.id=pasien.cacat_fisik where "+
                            "penilaian_awal_keperawatan_igd.tanggal between ? and ? and "+
                            "(reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "+
                            "penilaian_awal_keperawatan_igd.nip like ? or petugas.nama like ?) "+
                            "order by penilaian_awal_keperawatan_igd.tanggal");
                }

                try {
                    if(TCari.getText().equals("")){
                        ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                        ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    }else{
                        ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                        ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                        ps.setString(3,"%"+TCari.getText()+"%");
                        ps.setString(4,"%"+TCari.getText()+"%");
                        ps.setString(5,"%"+TCari.getText()+"%");
                        ps.setString(6,"%"+TCari.getText()+"%");
                        ps.setString(7,"%"+TCari.getText()+"%");
                    }  
                    rs=ps.executeQuery();
                    StringBuilder htmlContent = new StringBuilder();
                    htmlContent.append(                             
                        "<tr class='isi'>"+
                            "<td valign='middle' bgcolor='#FFFAFA' align='center' width='9%'><b>PASIEN & PETUGAS</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAFA' align='center' width='10%'><b>I. RIWAYAT KESEHATAN PASIEN</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAFA' align='center' width='15%'><b>II. PEMERIKSAAN FISIK</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAFA' align='center' width='14%'><b>III. RIWAYAT PSIKOLOGIS - SOSIAL - EKONOMI - BUDAYA - SPIRITUAL</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAFA' align='center' width='8%'><b>IV. PENGKAJIAN FUNGSI</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAFA' align='center' width='12%'><b>V. SKALA NYERI</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAFA' align='center' width='16%'><b>VI. PENGKAJIAN RESIKO JATUH (GET UP AND GO)</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAFA' align='center' width='11%'><b>MASALAH & RENCANA KEPERAWATAN</b></td>"+
                        "</tr>"
                    );
                    while(rs.next()){
                        masalahkeperawatanigd="";
                        ps2=koneksi.prepareStatement(
                            "select master_masalah_keperawatan_igd.kode_masalah,master_masalah_keperawatan_igd.nama_masalah from master_masalah_keperawatan_igd "+
                            "inner join penilaian_awal_keperawatan_igd_masalah on penilaian_awal_keperawatan_igd_masalah.kode_masalah=master_masalah_keperawatan_igd.kode_masalah "+
                            "where penilaian_awal_keperawatan_igd_masalah.no_rawat=? order by penilaian_awal_keperawatan_igd_masalah.kode_masalah");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            while(rs2.next()){
                                masalahkeperawatanigd=rs2.getString("nama_masalah")+", "+masalahkeperawatanigd;
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                        htmlContent.append(
                            "<tr class='isi'>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>No.Rawat</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("no_rawat")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>No.R.M.</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("no_rkm_medis")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Nama Pasien</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("nm_pasien")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>J.K.</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("jk")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Tgl.Lahir</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("tgl_lahir")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Agama</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("agama")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Bahasa</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("nama_bahasa")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Pekerjaan</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("pekerjaan")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Pembayaran</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("png_jawab")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Pendidikan</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("pnd")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Stts.Nikah</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("stts_nikah")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Cacat Fisik</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("nama_cacat")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Petugas</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("nip")+" "+rs.getString("nama")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Tgl.Asuhan</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("tanggal")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Informasi</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("informasi")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>RPS</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("keluhan_utama")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>RPD</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("rpd")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>RPO</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("rpo")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Stts.Hami</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("status_kehamilan")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>HPHT</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("hpht")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Para</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("para")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Abortus</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("abortus")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='32%' valign='top'>Gravida</td><td valign='top'>:&nbsp;</td><td width='67%' valign='top'>"+rs.getString("gravida")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Tekanan Intrakranial</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("tekanan")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Pupil</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("pupil")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Neurosensorik / Muskuloskeletal</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("neurosensorik")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Integumen</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("integumen")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Turgor kulit</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("turgor")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Edema</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("edema")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Mukosa Mulut</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("mukosa")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Perdarahan</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("perdarahan")+", Jumlah : "+rs.getString("jumlah_perdarahan")+", Warna : "+rs.getString("warna_perdarahan")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Intoksikasi</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("intoksikasi")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Eliminasi</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+
                                                "BAB : -Frekuensi : "+rs.getString("bab")+" x / "+rs.getString("xbab")+" -Konsistensi : "+rs.getString("kbab")+" -Warna : "+rs.getString("wbab")+"<br>"+
                                                "BAK : -Frekuensi : "+rs.getString("bak")+" x / "+rs.getString("xbak")+" -Warna : "+rs.getString("wbak")+" -Lain-lain : "+rs.getString("lbak")+"<br>"+
                                            "</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Kondisi Psikologis</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("psikologis")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Gangguan Jiwa Di Masa Lalu</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("jiwa")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Adakah perilaku</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("perilaku")+", Dilaporkan Ke : "+rs.getString("dilaporkan")+", Sebutkan : "+rs.getString("sebutkan")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Hubungan Pasien Dengan Anggota Keluarga</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("hubungan")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Tinggal Dengan</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("tinggal_dengan")+(rs.getString("ket_tinggal").equals("")?"":", "+rs.getString("ket_tinggal"))+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Kepercayaan / Budaya / Nilai-nilai Khusus</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("budaya")+(rs.getString("ket_budaya").equals("")?"":", "+rs.getString("ket_budaya"))+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Pendidikan P.J.</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("pendidikan_pj")+(rs.getString("ket_pendidikan_pj").equals("")?"":", "+rs.getString("ket_pendidikan_pj"))+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Edukasi Diberikan Kepada</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("edukasi")+(rs.getString("ket_edukasi").equals("")?"":", "+rs.getString("ket_edukasi"))+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Kemampuan Aktifitas Sehari-hari</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("kemampuan")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Aktifitas</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("aktifitas")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='34%' valign='top'>Alat Bantu</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("alat_bantu")+(rs.getString("ket_bantu").equals("")?"":", "+rs.getString("ket_bantu"))+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Tingkat Nyeri</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("nyeri")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Provokes</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("provokes")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Ket. Provokes</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("ket_provokes")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Kualitas</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("quality")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Ket. Kualitas</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("ket_quality")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Lokas</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("lokasi")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Menyebar</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("menyebar")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Skala Nyeri</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("skala_nyeri")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Durasi</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("durasi")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Nyeri Hilang</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("nyeri_hilang")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Ket. Hilang Nyeri</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("ket_nyeri")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Lapor Ke Dokter</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("pada_dokter")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='44%' valign='top'>Jam Lapor</td><td valign='top'>:&nbsp;</td><td width='55%' valign='top'>"+rs.getString("ket_dokter")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Tidak seimbang/sempoyongan/limbung</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("berjalan_a")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Jalan dengan menggunakan alat bantu (kruk, tripot, kursi roda, orang lain)</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("berjalan_b")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Menopang saat akan duduk, tampak memegang pinggiran kursi atau meja/benda lain sebagai penopang</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("berjalan_c")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Hasil</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("hasil")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Dilaporan ke dokter?</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("lapor")+"</td>"+
                                        "</tr>"+
                                        "<tr class='isi2'>"+
                                            "<td width='64%' valign='top'>Jam Lapor</td><td valign='top'>:&nbsp;</td><td width='35%' valign='top'>"+rs.getString("ket_lapor")+"</td>"+
                                        "</tr>"+
                                    "</table>"+
                                "</td>"+
                                "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                    "Masalah Keperawatan : "+masalahkeperawatanigd+"<br><br>"+
                                    "Rencana Keperawatan : "+rs.getString("rencana")+
                                "</td>"+
                            "</tr>"
                        );
                    }
                    LoadHTML.setText(
                        "<html>"+
                          "<table width='100%' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
                           htmlContent.toString()+
                          "</table>"+
                        "</html>"
                    );
                    htmlContent=null;

                    File g = new File("file2.css");            
                    BufferedWriter bg = new BufferedWriter(new FileWriter(g));
                    bg.write(
                        ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                        ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
                        ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                        ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                        ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                        ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                        ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                        ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                        ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
                    );
                    bg.close();

                    File f = new File("DataPenilaianAwalKeperawatanIGD.html");            
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
                    bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
                                "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                                "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                    "<tr class='isi2'>"+
                                        "<td valign='top' align='center'>"+
                                            "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                            akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                            akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                            "<font size='2' face='Tahoma'>DATA PENGKAJIAN AWAL KEPERAWATAN IGD<br><br></font>"+        
                                        "</td>"+
                                   "</tr>"+
                                "</table>")
                    );
                    bw.close();                         
                    Desktop.getDesktop().browse(f.toURI());
                } catch (Exception e) {
                    System.out.println("Notif : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps!=null){
                        ps.close();
                    }
                }

            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        runBackground(() ->tampil());
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() ->tampil());
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            TCari.setText("");
            runBackground(() ->tampil());
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        // [RM 03-4/05/06] Klik baris otomatis mengisi field utk edit (pola
        // sama RM 03-2) - getData() sekarang pola query-ulang-DB, cuma
        // SELECT (aman dipanggil dari klik biasa), user tetap harus tekan
        // Simpan/Edit utk benar-benar menyimpan perubahan.
        if(tabMode.getRowCount()!=0){
            try {
                ChkAccor.setSelected(true);
                isMenu();
                getData();
            } catch (java.lang.NullPointerException e) {
            }
            if((evt.getClickCount()==2)&&(tbObat.getSelectedColumn()==0)){
                TabRawat.setSelectedIndex(0);
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    ChkAccor.setSelected(true);
                    isMenu();
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                try {
                    getData();
                    TabRawat.setSelectedIndex(0);
                    // Pindah ke tab Input - bersihkan & lipat panel
                    // ringkasan (sudah tidak relevan), pola sama Triase.
                    TNoRM1.setText(""); TPasien1.setText(""); LoadHTML.setText("");
                    ChkAccor.setSelected(false);
                    isMenu();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void KdPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdPetugasKeyPressed
        
    }//GEN-LAST:event_KdPetugasKeyPressed

    private void BtnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPetugasActionPerformed
        if (petugas == null || !petugas.isDisplayable()) {
            petugas=new DlgCariPetugas(null,false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(petugas.getTable().getSelectedRow()!= -1){                   
                        KdPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                        NmPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());
                    }  
                    BtnPetugas.requestFocus();
                    petugas=null;
                }
            });

            petugas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            petugas.setLocationRelativeTo(internalFrame1);
        }
        if (petugas == null) return;
        if (!petugas.isVisible()) {
            petugas.isCek();    
            petugas.emptTeks();
        }
        
        if (petugas.isVisible()) {
            petugas.toFront();
            return;
        }
        petugas.setVisible(true); 
    }//GEN-LAST:event_BtnPetugasActionPerformed

    private void BtnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPetugasKeyPressed
        //Valid.pindah(evt,Monitoring,BtnSimpan);
    }//GEN-LAST:event_BtnPetugasKeyPressed

    private void KeluhanUtamaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeluhanUtamaKeyPressed
        Valid.pindah2(evt,Informasi,RPD);
    }//GEN-LAST:event_KeluhanUtamaKeyPressed

    private void RPDKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPDKeyPressed
        Valid.pindah2(evt,KeluhanUtama,RPO);
    }//GEN-LAST:event_RPDKeyPressed

    private void RPOKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPOKeyPressed
        Valid.pindah2(evt,RPD,StatusKehamilan);
    }//GEN-LAST:event_RPOKeyPressed

    private void AktifitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AktifitasKeyPressed
        Valid.pindah(evt,ADL,AlatBantu);
    }//GEN-LAST:event_AktifitasKeyPressed

    private void AlatBantuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AlatBantuKeyPressed
        Valid.pindah(evt,Aktifitas,KetAlatBantu);
    }//GEN-LAST:event_AlatBantuKeyPressed

    private void KetAlatBantuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetAlatBantuKeyPressed
        Valid.pindah(evt,AlatBantu,Nyeri);
    }//GEN-LAST:event_KetAlatBantuKeyPressed

    private void ADLKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ADLKeyPressed
        Valid.pindah(evt,KetEdukasi,Aktifitas);
    }//GEN-LAST:event_ADLKeyPressed

    private void TinggalDenganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TinggalDenganKeyPressed
        Valid.pindah(evt,Hubungan,KetTinggal);
    }//GEN-LAST:event_TinggalDenganKeyPressed

    private void KetTinggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetTinggalKeyPressed
        Valid.pindah(evt,TinggalDengan,StatusBudaya);
    }//GEN-LAST:event_KetTinggalKeyPressed

    private void EdukasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EdukasiKeyPressed
        Valid.pindah(evt,KetPendidikanPJ,KetEdukasi);
    }//GEN-LAST:event_EdukasiKeyPressed

    private void KetEdukasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetEdukasiKeyPressed
        Valid.pindah(evt,Edukasi,ADL);
    }//GEN-LAST:event_KetEdukasiKeyPressed

    private void LaporKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LaporKeyPressed
        Valid.pindah(evt,Hasil,KetLapor);
    }//GEN-LAST:event_LaporKeyPressed

    private void ATSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ATSKeyPressed
        Valid.pindah(evt,KetDokter,BJM);
    }//GEN-LAST:event_ATSKeyPressed

    private void BJMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BJMKeyPressed
        Valid.pindah(evt,ATS,MSA);
    }//GEN-LAST:event_BJMKeyPressed

    private void HasilKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HasilKeyPressed
        Valid.pindah(evt,MSA,Lapor);
    }//GEN-LAST:event_HasilKeyPressed

    private void KetLaporKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetLaporKeyPressed
        Valid.pindah(evt,Lapor,Rencana);
    }//GEN-LAST:event_KetLaporKeyPressed

    private void MSAKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MSAKeyPressed
        Valid.pindah(evt,BJM,Hasil);
    }//GEN-LAST:event_MSAKeyPressed

    private void NyeriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NyeriKeyPressed
        Valid.pindah(evt,KetAlatBantu,Provokes);
    }//GEN-LAST:event_NyeriKeyPressed

    private void ProvokesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ProvokesKeyPressed
        Valid.pindah(evt,Nyeri,KetProvokes);
    }//GEN-LAST:event_ProvokesKeyPressed

    private void KetProvokesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetProvokesKeyPressed
        Valid.pindah(evt,Provokes,Quality);
    }//GEN-LAST:event_KetProvokesKeyPressed

    private void QualityKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_QualityKeyPressed
        Valid.pindah(evt,KetProvokes,KetQuality);
    }//GEN-LAST:event_QualityKeyPressed

    private void KetQualityKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetQualityKeyPressed
        Valid.pindah(evt,Quality,Lokasi);
    }//GEN-LAST:event_KetQualityKeyPressed

    private void LokasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LokasiKeyPressed
        Valid.pindah(evt,KetQuality,Menyebar);
    }//GEN-LAST:event_LokasiKeyPressed

    private void MenyebarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MenyebarKeyPressed
        Valid.pindah(evt,Lokasi,SkalaNyeri);
    }//GEN-LAST:event_MenyebarKeyPressed

    private void SkalaNyeriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaNyeriKeyPressed
        Valid.pindah(evt,Menyebar,Durasi);
    }//GEN-LAST:event_SkalaNyeriKeyPressed

    private void DurasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DurasiKeyPressed
        Valid.pindah(evt,SkalaNyeri,NyeriHilang);
    }//GEN-LAST:event_DurasiKeyPressed

    private void NyeriHilangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NyeriHilangKeyPressed
        Valid.pindah(evt,Durasi,KetNyeri);
    }//GEN-LAST:event_NyeriHilangKeyPressed

    private void KetNyeriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetNyeriKeyPressed
        Valid.pindah(evt,NyeriHilang,PadaDokter);
    }//GEN-LAST:event_KetNyeriKeyPressed

    private void PadaDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PadaDokterKeyPressed
        Valid.pindah(evt,KetNyeri,KetDokter);
    }//GEN-LAST:event_PadaDokterKeyPressed

    private void KetDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetDokterKeyPressed
        Valid.pindah(evt,PadaDokter,ATS);
    }//GEN-LAST:event_KetDokterKeyPressed

    private void TglAsuhanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglAsuhanKeyPressed
        Valid.pindah2(evt,Rencana,RPD);
    }//GEN-LAST:event_TglAsuhanKeyPressed

    private void StatusBudayaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_StatusBudayaKeyPressed
        Valid.pindah(evt,KetTinggal,KetBudaya);
    }//GEN-LAST:event_StatusBudayaKeyPressed

    private void KetBudayaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetBudayaKeyPressed
        Valid.pindah(evt,StatusBudaya,PendidikanPJ);
    }//GEN-LAST:event_KetBudayaKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        try {
            if(Valid.daysOld("./cache/masalahkeperawatanigd.iyem")<30){
                runBackground(() ->tampilMasalah2());
            }else{
                runBackground(() ->tampilMasalah());
            }
        } catch (Exception e) {
        }
        
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
            });
            
            TCariMasalah.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCariMasalah.getText().length()>2){
                        runBackground(() ->tampilMasalah2());
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCariMasalah.getText().length()>2){
                        runBackground(() ->tampilMasalah2());
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCariMasalah.getText().length()>2){
                        runBackground(() ->tampilMasalah2());
                    }
                }
            });
        }
    }//GEN-LAST:event_formWindowOpened

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
        if(tbObat.getSelectedRow()!= -1){
            isMenu();
        }else{
            ChkAccor.setSelected(false);
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data yang mau ditampilkan...!!!!");
        }
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void BtnPrint1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrint1ActionPerformed
       if(tbObat.getSelectedRow()>-1){
            // Query cetak disinkronkan total dgn <queryString> di
            // rptCetakPenilaianAwalKeperawatanIGD.jrxml (redesain RM
            // 03-4/05/06) - masalah_terpilih (Diagnosis Keperawatan) &
            // obat_infus_ringkas (Pemberian Obat/Infus) sekarang dihitung
            // via subquery GROUP_CONCAT langsung di SELECT, jadi tidak
            // perlu lagi pre-fetch terpisah spt versi lama (yg juga masih
            // query ke tabel Rencana Keperawatan lama yg sudah ditinggalkan).
            Map<String, Object> param = new HashMap<>();
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());
            param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),6).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),5).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString()));
            String qryCetak =
                        "select penilaian_awal_keperawatan_igd.*, "+
                        "reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, if(pasien.jk='L','Laki-Laki','Perempuan') as jk, "+
                        "reg_periksa.tgl_registrasi, reg_periksa.jam_reg, "+
                        "petugas.nama as nm_perawat, "+
                        "dokter.nm_dokter, dokter.no_ijn_praktek, "+
                        "(select group_concat(mmk.nama_masalah separator ', ') from penilaian_awal_keperawatan_igd_masalah pakm "+
                        " inner join master_masalah_keperawatan_igd mmk on mmk.kode_masalah=pakm.kode_masalah "+
                        " where pakm.no_rawat=penilaian_awal_keperawatan_igd.no_rawat) as masalah_terpilih, "+
                        "(select group_concat(concat(pako.pukul,' - ',pako.nama_obat,' (',pako.dosis,', ',pako.rute,')') separator '; ') "+
                        " from penilaian_awal_keperawatan_igd_obat pako "+
                        " where pako.no_rawat=penilaian_awal_keperawatan_igd.no_rawat) as obat_infus_ringkas "+
                        "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join penilaian_awal_keperawatan_igd on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat "+
                        "inner join petugas on penilaian_awal_keperawatan_igd.nip=petugas.nip "+
                        "left join dokter on dokter.kd_dokter=penilaian_awal_keperawatan_igd.kp_kd_dokter "+
                        "where reg_periksa.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'";
            String modeCetak = (String)JOptionPane.showInputDialog(null,"Silahkan pilih mode cetak..!","Cetak Laporan Pengkajian Awal Keperawatan IGD",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Lembar","PDF"},"Lembar");
            if(modeCetak==null){
                return;
            }
            if(modeCetak.equals("PDF")){
                Valid.MyReportqrypdf("rptCetakPenilaianAwalKeperawatanIGD.jasper","report","::[ Laporan Pengkajian Awal Keperawatan IGD ]::",qryCetak,param);
            }else{
                Valid.MyReportqry("rptCetakPenilaianAwalKeperawatanIGD.jasper","report","::[ Laporan Pengkajian Awal Keperawatan IGD ]::",qryCetak,param);
            }
        }else{
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu..!!!!");
        }
    }//GEN-LAST:event_BtnPrint1ActionPerformed

    private void TPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TPasienActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TPasienActionPerformed

    private void StatusKehamilanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_StatusKehamilanKeyPressed
        Valid.pindah(evt,RPO,HPHT);
    }//GEN-LAST:event_StatusKehamilanKeyPressed

    private void TekananKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TekananKeyPressed
        Valid.pindah(evt,Gravida,Pupil);
    }//GEN-LAST:event_TekananKeyPressed

    private void PupilKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PupilKeyPressed
        Valid.pindah(evt,Tekanan,Neurosensorik);
    }//GEN-LAST:event_PupilKeyPressed

    private void NeurosensorikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NeurosensorikKeyPressed
        Valid.pindah(evt,Pupil,Integumen);
    }//GEN-LAST:event_NeurosensorikKeyPressed

    private void IntegumenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_IntegumenKeyPressed
        Valid.pindah(evt,Neurosensorik,Turgor);
    }//GEN-LAST:event_IntegumenKeyPressed

    private void TurgorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TurgorKeyPressed
        Valid.pindah(evt,Integumen,Edema);
    }//GEN-LAST:event_TurgorKeyPressed

    private void EdemaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EdemaKeyPressed
        Valid.pindah(evt,Turgor,Mukosa);
    }//GEN-LAST:event_EdemaKeyPressed

    private void MukosaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MukosaKeyPressed
        Valid.pindah(evt,Edema,Perdarahan);
    }//GEN-LAST:event_MukosaKeyPressed

    private void PerdarahanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PerdarahanKeyPressed
        Valid.pindah(evt,Mukosa,JumlahPerdarahan);
    }//GEN-LAST:event_PerdarahanKeyPressed

    private void IntoksikasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_IntoksikasiKeyPressed
        Valid.pindah(evt,WarnaPerdarahan,BAB);
    }//GEN-LAST:event_IntoksikasiKeyPressed

    private void PsikologisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PsikologisKeyPressed
        Valid.pindah(evt,LBAK,Jiwa);
    }//GEN-LAST:event_PsikologisKeyPressed

    private void JiwaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JiwaKeyPressed
        Valid.pindah(evt,Psikologis,Perilaku);
    }//GEN-LAST:event_JiwaKeyPressed

    private void PerilakuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PerilakuKeyPressed
        Valid.pindah(evt,Jiwa,Dilaporkan);
    }//GEN-LAST:event_PerilakuKeyPressed

    private void HubunganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HubunganKeyPressed
        Valid.pindah(evt,Sebutkan,TinggalDengan);
    }//GEN-LAST:event_HubunganKeyPressed

    private void StatusPernikahanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StatusPernikahanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_StatusPernikahanActionPerformed

    private void KetPendidikanPJKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetPendidikanPJKeyPressed
        Valid.pindah(evt,PendidikanPJ,Edukasi);
    }//GEN-LAST:event_KetPendidikanPJKeyPressed

    private void PendidikanPJKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PendidikanPJKeyPressed
        Valid.pindah(evt,KetBudaya,KetPendidikanPJ);
    }//GEN-LAST:event_PendidikanPJKeyPressed

    private void InformasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_InformasiKeyPressed
        Valid.pindah(evt,TglAsuhan,KeluhanUtama);
    }//GEN-LAST:event_InformasiKeyPressed

    private void HPHTKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HPHTKeyPressed
        Valid.pindah(evt,StatusKehamilan,Para);
    }//GEN-LAST:event_HPHTKeyPressed

    private void ParaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ParaKeyPressed
        Valid.pindah(evt,HPHT,Abortus);
    }//GEN-LAST:event_ParaKeyPressed

    private void AbortusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AbortusKeyPressed
        Valid.pindah(evt,Para,Gravida);
    }//GEN-LAST:event_AbortusKeyPressed

    private void GravidaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_GravidaKeyPressed
        Valid.pindah(evt,Abortus,Tekanan);
    }//GEN-LAST:event_GravidaKeyPressed

    private void JumlahPerdarahanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JumlahPerdarahanKeyPressed
        Valid.pindah(evt,Perdarahan,WarnaPerdarahan);
    }//GEN-LAST:event_JumlahPerdarahanKeyPressed

    private void WarnaPerdarahanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_WarnaPerdarahanKeyPressed
        Valid.pindah(evt,JumlahPerdarahan,Intoksikasi);
    }//GEN-LAST:event_WarnaPerdarahanKeyPressed

    private void BABKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BABKeyPressed
        Valid.pindah(evt,Intoksikasi,XBAB);
    }//GEN-LAST:event_BABKeyPressed

    private void XBABKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_XBABKeyPressed
        Valid.pindah(evt,BAB,KBAB);
    }//GEN-LAST:event_XBABKeyPressed

    private void KBABKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KBABKeyPressed
        Valid.pindah(evt,XBAB,WBAB);
    }//GEN-LAST:event_KBABKeyPressed

    private void WBABKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_WBABKeyPressed
        Valid.pindah(evt,KBAB,BAK);
    }//GEN-LAST:event_WBABKeyPressed

    private void BAKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BAKKeyPressed
        Valid.pindah(evt,WBAB,XBAK);
    }//GEN-LAST:event_BAKKeyPressed

    private void XBAKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_XBAKKeyPressed
        Valid.pindah(evt,BAK,WBAK);
    }//GEN-LAST:event_XBAKKeyPressed

    private void WBAKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_WBAKKeyPressed
        Valid.pindah(evt,XBAK,LBAK);
    }//GEN-LAST:event_WBAKKeyPressed

    private void LBAKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LBAKKeyPressed
        Valid.pindah(evt,WBAK,Psikologis);
    }//GEN-LAST:event_LBAKKeyPressed

    private void DilaporkanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DilaporkanKeyPressed
        Valid.pindah(evt,Perilaku,Sebutkan);
    }//GEN-LAST:event_DilaporkanKeyPressed

    private void SebutkanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SebutkanKeyPressed
        Valid.pindah(evt,Dilaporkan,Hubungan);
    }//GEN-LAST:event_SebutkanKeyPressed

    private void tbMasalahKeperawatanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbMasalahKeperawatanMouseClicked
        if(tabModeMasalah.getRowCount()!=0){
            try {
                runBackground(() ->tampilRencana2());
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbMasalahKeperawatanMouseClicked

    private void tbMasalahKeperawatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbMasalahKeperawatanKeyPressed
        if(tabModeMasalah.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                TCariMasalah.setText("");
                TCariMasalah.requestFocus();
            }
        }
    }//GEN-LAST:event_tbMasalahKeperawatanKeyPressed

    private void tbMasalahKeperawatanKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbMasalahKeperawatanKeyReleased
        if(tabModeMasalah.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    runBackground(() ->tampilRencana2());
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbMasalahKeperawatanKeyReleased

    private void BtnTambahMasalahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahMasalahActionPerformed
        if (masalahkeperawatan == null || !masalahkeperawatan.isDisplayable()) {
            masalahkeperawatan=new MasterMasalahKeperawatanIGD(null,false);
            masalahkeperawatan.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            masalahkeperawatan.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    masalahkeperawatan=null;
                }
            });

            masalahkeperawatan.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            masalahkeperawatan.setLocationRelativeTo(internalFrame1);
        }
        if (masalahkeperawatan == null) return;
        if (!masalahkeperawatan.isVisible()) {
            masalahkeperawatan.isCek();    
        }
        
        if (masalahkeperawatan.isVisible()) {
            masalahkeperawatan.toFront();
            return;
        }
        masalahkeperawatan.setVisible(true); 
    }//GEN-LAST:event_BtnTambahMasalahActionPerformed

    private void BtnAllMasalahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllMasalahActionPerformed
        TCari.setText("");
        runBackground(() ->tampilMasalah());
    }//GEN-LAST:event_BtnAllMasalahActionPerformed

    private void BtnAllMasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllMasalahKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllMasalahActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnCariMasalah, TCariMasalah);
        }
    }//GEN-LAST:event_BtnAllMasalahKeyPressed

    private void BtnCariMasalahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariMasalahActionPerformed
        runBackground(() ->tampilMasalah2());
    }//GEN-LAST:event_BtnCariMasalahActionPerformed

    private void BtnCariMasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariMasalahKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            runBackground(() ->tampilMasalah2());
        }else if((evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN)||(evt.getKeyCode()==KeyEvent.VK_TAB)){
            Rencana.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            KetDokter.requestFocus();
        }
    }//GEN-LAST:event_BtnCariMasalahKeyPressed

    private void TCariMasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariMasalahKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            runBackground(() ->tampilMasalah2());
        }else if((evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN)||(evt.getKeyCode()==KeyEvent.VK_TAB)){
            Rencana.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            KetDokter.requestFocus();
        }
    }//GEN-LAST:event_TCariMasalahKeyPressed

    private void RencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RencanaKeyPressed
        Valid.pindah2(evt,TCariMasalah,BtnSimpan);
    }//GEN-LAST:event_RencanaKeyPressed

    private void TCariRencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariRencanaKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            runBackground(() ->tampilRencana2());
        }else if((evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN)||(evt.getKeyCode()==KeyEvent.VK_TAB)){
            BtnCariRencana.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            TCariMasalah.requestFocus();
        }
    }//GEN-LAST:event_TCariRencanaKeyPressed

    private void BtnCariRencanaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariRencanaActionPerformed
        runBackground(() ->tampilRencana2());
    }//GEN-LAST:event_BtnCariRencanaActionPerformed

    private void BtnCariRencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariRencanaKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            runBackground(() ->tampilRencana2());
        }else if((evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN)||(evt.getKeyCode()==KeyEvent.VK_TAB)){
            BtnSimpan.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            TCariRencana.requestFocus();
        }
    }//GEN-LAST:event_BtnCariRencanaKeyPressed

    private void BtnAllRencanaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllRencanaActionPerformed
        TCariRencana.setText("");
        runBackground(() ->LoadRencana());
    }//GEN-LAST:event_BtnAllRencanaActionPerformed

    private void BtnAllRencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllRencanaKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllRencanaActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnCariRencana, TCariRencana);
        }
    }//GEN-LAST:event_BtnAllRencanaKeyPressed

    private void BtnTambahRencanaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahRencanaActionPerformed
        if (rencanakeperawatan == null || !rencanakeperawatan.isDisplayable()) {
            rencanakeperawatan=new MasterRencanaKeperawatanIGD(null,false);
            rencanakeperawatan.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            rencanakeperawatan.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    rencanakeperawatan=null;
                }
            });

            rencanakeperawatan.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            rencanakeperawatan.setLocationRelativeTo(internalFrame1);
        }
        if (rencanakeperawatan == null) return;
        if (!rencanakeperawatan.isVisible()) {
            rencanakeperawatan.isCek();    
        }
        
        if (rencanakeperawatan.isVisible()) {
            rencanakeperawatan.toFront();
            return;
        }
        rencanakeperawatan.setVisible(true); 
    }//GEN-LAST:event_BtnTambahRencanaActionPerformed

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
    // [RM 03-4/03-5/03-6] Field baru HALAMAN 1 - lihat initKepTambahan().
    // ===================================================================
    private widget.PanelBiasa panelKepAtas;
    private int geserKep1;
    private int yBawahLama;
    private int lebarFormInput = 870;

    // -- Riwayat Alergi --
    private widget.RadioButton RbAlergiTidak, RbAlergiYa;
    private javax.swing.ButtonGroup bgAlergi;
    private widget.TextBox TAlergiSebutkan;

    // -- Keadaan Umum (Tanda Vital) --
    private widget.TextBox TdKep, NadiKep, SuhuKep, SpoKep, RrKep, BbKep, TbKep;

    // -- Asesmen Psikologis --
    private widget.CekBox CekPsiTenang, CekPsiCemas, CekPsiTakut, CekPsiSedih, CekPsiMarah, CekPsiBunuhDiri;
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
    private widget.TextBox TAgama;
    private widget.CekBox CekCacatTidak, CekCacatTunaNetra, CekCacatTunaDaksa, CekCacatTunaRungu, CekCacatTunaWicara;

    // ===================================================================
    // [RM 03-5] Field baru HALAMAN 2 - lihat initKepTambahanHal2().
    // ===================================================================
    private widget.PanelBiasa panelKepDua;

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
        CekIntervManajemenSensasiPerifer, CekIntervMonitoringKardiovaskuler, CekIntervPencegahanPenekananLuka;
    private widget.TextBox TIntervLainnya;

    // -- IV. Kardiopulmonal --
    private widget.CekBox CekKardioPulseOximetri, CekKardioIntubasi, CekKardioRjp, CekKardioKardioversiDcShock;
    private widget.TextBox TKardioRjpKet;

    // -- V. Tindakan Penunjang --
    private widget.CekBox CekPenunjangCekLab, CekPenunjangRontgen, CekPenunjangCtScan;
    private widget.TextBox TPenunjangCekLabKet, TPenunjangRontgenKet, TPenunjangCtScanKet;

    // -- VI. Tindakan Kolaborasi --
    private widget.CekBox CekKolabO2, CekKolabEkgMonitor, CekKolabNebulizer, CekKolabPasangInfus,
        CekKolabInjeksiSyringePump, CekKolabTransfusiDarah, CekKolabNgt, CekKolabBilasLambung,
        CekKolabDowerCatheter, CekKolabRestrainPengaman;
    private widget.TextBox TKolabO2Ket, TKolabPasangInfusKet, TKolabTransfusiDarahKet, TKolabNgtUkuran,
        TKolabBilasLambungKet, TKolabDowerCatheterUkuran;

    // -- VII. Trauma / Bedah --
    private widget.CekBox CekTraumaCervicalCollar, CekTraumaImmobilisasiTulangBelakang, CekTraumaGips,
        CekTraumaPerawatanLuka, CekTraumaJahitLuka, CekTraumaBalutanSpalk, CekTraumaSelangDadaWsd;
    private widget.TextBox TTraumaLainnya;

    // -- VIII. THT / Mata --
    private widget.CekBox CekThtIrigasiMataTelinga, CekThtAmbilKorpusAlienum;

    // -- IX. Tindakan Obgyn --
    private widget.CekBox CekObgynMembantuPartus, CekObgynPeriksaGinekologi, CekObgynDopplerCtgUsg, CekObgynPasangTampon;

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
    private widget.Jam DTPKpPukul;
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
    private widget.Jam DTPKpDipulangkanPukul;
    private widget.CekBox CekKpMeninggal;
    private widget.Jam DTPKpMeninggalPukul;
    private widget.CekBox CekKpDoa;
    private widget.CekBox CekKpEdukasiMakanObat, CekKpEdukasiLuka, CekKpEdukasiDiet, CekKpEdukasiLain;
    private widget.TextBox TKpEdukasiDietKet, TKpEdukasiLainKet;
    private widget.TextBox KdDokterKp, NmDokterKp;
    private widget.Button BtnDokterKp;

    // ===================================================================
    // [Lanjutan RM 03-6] Field "Pemberian Obat/Infus" - lihat
    // initObatInfusTambahan()/tampilObatInfus()/simpanObat()/hapusObat().
    // ===================================================================
    private widget.PanelBiasa panelKepObat;
    private DefaultTableModel tabModeObatInfus;
    private widget.Table tbObatInfus;
    private widget.Jam DTPObatPukul;
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
        try{
            if(TCari.getText().equals("")){
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"+
                        "penilaian_awal_keperawatan_igd.tanggal,penilaian_awal_keperawatan_igd.nip,petugas.nama "+
                        "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join penilaian_awal_keperawatan_igd on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat "+
                        "inner join petugas on penilaian_awal_keperawatan_igd.nip=petugas.nip where "+
                        "penilaian_awal_keperawatan_igd.tanggal between ? and ? order by penilaian_awal_keperawatan_igd.tanggal");
            }else{
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"+
                        "penilaian_awal_keperawatan_igd.tanggal,penilaian_awal_keperawatan_igd.nip,petugas.nama "+
                        "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join penilaian_awal_keperawatan_igd on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat "+
                        "inner join petugas on penilaian_awal_keperawatan_igd.nip=petugas.nip where "+
                        "penilaian_awal_keperawatan_igd.tanggal between ? and ? and "+
                        "(reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "+
                        "penilaian_awal_keperawatan_igd.nip like ? or petugas.nama like ?) "+
                        "order by penilaian_awal_keperawatan_igd.tanggal");
            }

            try {
                if(TCari.getText().equals("")){
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                }else{
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,"%"+TCari.getText()+"%");
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("tgl_lahir"),
                        rs.getString("tanggal"),rs.getString("nip"),rs.getString("nama")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }

        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
    }

    public void emptTeks() {
        // -- Riwayat Kesehatan (lama, aktif) --
        TglAsuhan.setDate(new Date());
        Informasi.setSelectedIndex(0);
        KeluhanUtama.setText("");
        RPD.setText("");
        RPO.setText("");
        StatusKehamilan.setSelectedIndex(0);
        Gravida.setText("");
        Para.setText("");
        Abortus.setText("");
        HPHT.setText("");

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
        bgAlergi.clearSelection();
        TAlergiSebutkan.setText("");

        // -- Keadaan Umum (Tanda Vital) --
        TdKep.setText(""); NadiKep.setText(""); SuhuKep.setText(""); SpoKep.setText("");
        RrKep.setText(""); BbKep.setText(""); TbKep.setText("");

        // -- Asesmen Psikologis --
        CekPsiTenang.setSelected(false); CekPsiCemas.setSelected(false); CekPsiTakut.setSelected(false);
        CekPsiSedih.setSelected(false); CekPsiMarah.setSelected(false); CekPsiBunuhDiri.setSelected(false);
        bgMasalahPrilaku.clearSelection(); TMasalahPrilakuSebutkan.setText("");

        // -- Status Sosial dan Ekonomis --
        bgStatusPernikahan.clearSelection(); TKetStatusPernikahan.setText("");
        bgTempatTinggal.clearSelection(); TKetTempatTinggal.setText("");
        TPekerjaanKep.setText(""); TPendidikanPasienKep.setText("");

        // -- Skrining Geriatri --
        bgGeri1.clearSelection(); bgGeri2.clearSelection(); bgGeri3.clearSelection();

        // -- Skrining Resiko Jatuh --
        bgJatuhCaraBerjalan.clearSelection(); bgJatuhMenopang.clearSelection(); bgJatuhHasil.clearSelection();

        // -- Pengkajian Spiritual --
        bgWajibIbadah.clearSelection(); TKetWajibIbadah.setText("");
        bgThaharoh.clearSelection();
        bgSholat.clearSelection();
        bgNilaiKepercayaan.clearSelection(); TKetNilaiKepercayaan.setText("");
        TAgama.setText("");
        CekCacatTidak.setSelected(false); CekCacatTunaNetra.setSelected(false); CekCacatTunaDaksa.setSelected(false);
        CekCacatTunaRungu.setSelected(false); CekCacatTunaWicara.setSelected(false);

        // -- Skrining Gizi Dewasa (MST) --
        bgMstPenurunanBb.clearSelection(); MstPenurunanBbKg.setSelectedIndex(0);
        bgMstAsupan.clearSelection(); TMstTotalSkor.setText("");
        bgMstDiagnosis.clearSelection(); TMstDiagnosisSebutkan.setText("");
        bgMstKesimpulan.clearSelection();

        // -- Skrining Gizi Anak --
        bgGiziAnakKurus.clearSelection(); bgGiziAnakTurunBb.clearSelection();
        bgGiziAnakKondisi.clearSelection(); bgGiziAnakRisiko.clearSelection();
        TGiziAnakTotalSkor.setText(""); bgGiziAnakKesimpulan.clearSelection();

        // -- Asesmen Nyeri (toggle baru) --
        bgNyeriAda.clearSelection();

        // -- FLACC --
        FlaccWajah.setSelectedIndex(0); FlaccKaki.setSelectedIndex(0); FlaccAktivitas.setSelectedIndex(0);
        FlaccMenangis.setSelectedIndex(0); FlaccBersuara.setSelectedIndex(0); TFlaccTotal.setText("");

        // -- PQRST --
        CekProvCahaya.setSelected(false); CekProvGelap.setSelected(false); CekProvGerakan.setSelected(false); CekProvBerbaring.setSelected(false);
        CekQualTumpul.setSelected(false); CekQualDitusuk.setSelected(false); CekQualBerdenyut.setSelected(false); CekQualTajam.setSelected(false); CekQualPanas.setSelected(false);
        bgRadiation.clearSelection(); TRadiationKe.setText("");
        bgSeverity.clearSelection(); bgTime.clearSelection();

        // -- II. Intervensi Keperawatan --
        CekIntervManajemenJalanNafas.setSelected(false); CekIntervSuctioningJalanNafas.setSelected(false);
        CekIntervManajemenAsamBasa.setSelected(false); CekIntervManajemenSyok.setSelected(false);
        CekIntervManajemenNyeri.setSelected(false); CekIntervManajemenCairan.setSelected(false);
        CekIntervMonitoringCairan.setSelected(false); CekIntervManajemenLingkungan.setSelected(false);
        CekIntervPendidikanKesehatan.setSelected(false); CekIntervFeverTreatment.setSelected(false);
        CekIntervManajemenSensasiPerifer.setSelected(false); CekIntervMonitoringKardiovaskuler.setSelected(false);
        CekIntervPencegahanPenekananLuka.setSelected(false); TIntervLainnya.setText("");

        // -- IV. Kardiopulmonal --
        CekKardioPulseOximetri.setSelected(false); CekKardioIntubasi.setSelected(false);
        CekKardioRjp.setSelected(false); TKardioRjpKet.setText("");
        CekKardioKardioversiDcShock.setSelected(false);

        // -- V. Tindakan Penunjang --
        CekPenunjangCekLab.setSelected(false); TPenunjangCekLabKet.setText("");
        CekPenunjangRontgen.setSelected(false); TPenunjangRontgenKet.setText("");
        CekPenunjangCtScan.setSelected(false); TPenunjangCtScanKet.setText("");

        // -- VI. Tindakan Kolaborasi --
        CekKolabO2.setSelected(false); TKolabO2Ket.setText("");
        CekKolabEkgMonitor.setSelected(false); CekKolabNebulizer.setSelected(false);
        CekKolabPasangInfus.setSelected(false); TKolabPasangInfusKet.setText("");
        CekKolabInjeksiSyringePump.setSelected(false);
        CekKolabTransfusiDarah.setSelected(false); TKolabTransfusiDarahKet.setText("");
        CekKolabNgt.setSelected(false); TKolabNgtUkuran.setText("");
        CekKolabBilasLambung.setSelected(false); TKolabBilasLambungKet.setText("");
        CekKolabDowerCatheter.setSelected(false); TKolabDowerCatheterUkuran.setText("");
        CekKolabRestrainPengaman.setSelected(false);

        // -- VII. Trauma / Bedah --
        CekTraumaCervicalCollar.setSelected(false); CekTraumaImmobilisasiTulangBelakang.setSelected(false);
        CekTraumaGips.setSelected(false); CekTraumaPerawatanLuka.setSelected(false);
        CekTraumaJahitLuka.setSelected(false); CekTraumaBalutanSpalk.setSelected(false);
        CekTraumaSelangDadaWsd.setSelected(false); TTraumaLainnya.setText("");

        // -- VIII. THT / Mata --
        CekThtIrigasiMataTelinga.setSelected(false); CekThtAmbilKorpusAlienum.setSelected(false);

        // -- IX. Tindakan Obgyn --
        CekObgynMembantuPartus.setSelected(false); CekObgynPeriksaGinekologi.setSelected(false);
        CekObgynDopplerCtgUsg.setSelected(false); CekObgynPasangTampon.setSelected(false);

        // -- Kondisi Pasien Saat Pindah/Pulang --
        DTPKpPukul.setJam(new Date());
        TKpGcsE.setText(""); TKpGcsV.setText(""); TKpGcsM.setText("");
        TKpTd.setText(""); TKpNadi.setText(""); bgKpNadiReguler.clearSelection();
        TKpSuhu.setText(""); TKpPernafasan.setText(""); TKpSpo.setText("");
        CekKpMasukRs.setSelected(false); TKpRuang.setText("");
        TKpPetugasPenerima.setText(""); NmKpPetugasPenerima.setText("");
        bgKpPendamping.clearSelection();
        bgKpTindakanLanjutan.clearSelection(); TKpTindakanLanjutanKet.setText("");
        CekKpDirujuk.setSelected(false); TKpDirujukKet.setText(""); TKpDirujukAlasan.setText("");
        CekKpDipulangkan.setSelected(false); DTPKpDipulangkanPukul.setJam(new Date());
        CekKpMeninggal.setSelected(false); DTPKpMeninggalPukul.setJam(new Date());
        CekKpDoa.setSelected(false);
        CekKpEdukasiMakanObat.setSelected(false); CekKpEdukasiLuka.setSelected(false);
        CekKpEdukasiDiet.setSelected(false); TKpEdukasiDietKet.setText("");
        CekKpEdukasiLain.setSelected(false); TKpEdukasiLainKet.setText("");
        KdDokterKp.setText(""); NmDokterKp.setText("");

        // -- Pemberian Obat/Infus (tabel terpisah, direset via tampilObatInfus) --
        Valid.tabelKosong(tabModeObatInfus);
        emptTeksObatInfus();

        for (i = 0; i < tabModeMasalah.getRowCount(); i++) {
            tabModeMasalah.setValueAt(false,i,0);
        }
        for (i = 0; i < tabModeRencana.getRowCount(); i++) {
            tabModeRencana.setValueAt(false,i,0);
        }
        TabRawat.setSelectedIndex(0);
        Informasi.requestFocus();
    }

    /**
     * [RM 03-4/05/06] Helper konversi checkbox ke "Ya"/"Tidak", pola sama
     * seperti RM 03-2 - dipakai bareng oleh nilaiKep().
     */
    private String ya(javax.swing.AbstractButton c){
        return c.isSelected() ? "Ya" : "Tidak";
    }

    /**
     * [RM 03-4/05/06] ComboBox dgn opsi kosong sbg placeholder (mis.
     * FLACC/MstPenurunanBbKg) - kalau belum dipilih apa-apa, kembalikan
     * null (bukan "") supaya tidak gagal insert ke kolom ENUM nullable
     * yang tidak punya opsi "".
     */
    private String kosongKeNull(String s){
        return (s==null || s.trim().equals("")) ? null : s;
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
        if(tbObat.getSelectedRow()== -1){ return; }
        String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();
        try{
            PreparedStatement psData = koneksi.prepareStatement(
                "select penilaian_awal_keperawatan_igd.*, "+
                "reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, if(pasien.jk='L','Laki-Laki','Perempuan') as jk, "+
                "pasien.stts_nikah, pasien.pekerjaan as pekerjaan_pasien, pasien.pnd, penjab.png_jawab, "+
                "petugas.nama, dokter.nm_dokter as kp_nm_dokter "+
                "from penilaian_awal_keperawatan_igd "+
                "inner join reg_periksa on reg_periksa.no_rawat=penilaian_awal_keperawatan_igd.no_rawat "+
                "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                "inner join petugas on petugas.nip=penilaian_awal_keperawatan_igd.nip "+
                "left join penjab on penjab.kd_pj=reg_periksa.kd_pj "+
                "left join dokter on dokter.kd_dokter=penilaian_awal_keperawatan_igd.kp_kd_dokter "+
                "where penilaian_awal_keperawatan_igd.no_rawat=?");
            try{
                psData.setString(1,noRawat);
                ResultSet rsData = psData.executeQuery();
                try{
                    if(rsData.next()){
                        TNoRw.setText(noRawat);
                        TNoRM.setText(rsData.getString("no_rkm_medis"));
                        TPasien.setText(rsData.getString("nm_pasien"));
                        Jk.setText(rsData.getString("jk"));
                        TglLahir.setText(rsData.getString("tgl_lahir"));
                        StatusPernikahan.setText(rsData.getString("stts_nikah"));
                        Pekerjaan.setText(rsData.getString("pekerjaan_pasien"));
                        PendidikanPasien.setText(rsData.getString("pnd"));
                        Pembayaran.setText(rsData.getString("png_jawab"));
                        Valid.SetTgl2(TglAsuhan,rsData.getString("tanggal"));

                        // -- Riwayat Kesehatan (lama, aktif) --
                        Informasi.setSelectedItem(rsData.getString("informasi"));
                        KeluhanUtama.setText(rsData.getString("keluhan_utama"));
                        RPD.setText(rsData.getString("rpd"));
                        RPO.setText(rsData.getString("rpo"));
                        StatusKehamilan.setSelectedItem(rsData.getString("status_kehamilan"));
                        Gravida.setText(rsData.getString("gravida"));
                        Para.setText(rsData.getString("para"));
                        Abortus.setText(rsData.getString("abortus"));
                        HPHT.setText(rsData.getString("hpht"));

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
                        StatusBudaya.setSelectedItem(rsData.getString("budaya"));
                        KetBudaya.setText(rsData.getString("ket_budaya"));
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
                        if("Ya".equals(alergiAda)) RbAlergiYa.setSelected(true);
                        else if("Tidak".equals(alergiAda)) RbAlergiTidak.setSelected(true);
                        else bgAlergi.clearSelection();
                        TAlergiSebutkan.setText(rsData.getString("alergi_sebutkan"));

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
                        if("Ya".equals(masalahPrilaku)) RbMasalahPrilakuYa.setSelected(true);
                        else if("Tidak".equals(masalahPrilaku)) RbMasalahPrilakuTidak.setSelected(true);
                        else bgMasalahPrilaku.clearSelection();
                        TMasalahPrilakuSebutkan.setText(rsData.getString("psi_masalah_prilaku_sebutkan"));

                        // -- Status Sosial dan Ekonomis --
                        String statusNikah = rsData.getString("status_pernikahan");
                        if("Belum Menikah".equals(statusNikah)) RbNikahBelumMenikah.setSelected(true);
                        else if("Menikah".equals(statusNikah)) RbNikahMenikah.setSelected(true);
                        else if("Cerai".equals(statusNikah)) RbNikahCerai.setSelected(true);
                        else if("Lainnya".equals(statusNikah)) RbNikahLainnya.setSelected(true);
                        else bgStatusPernikahan.clearSelection();
                        TKetStatusPernikahan.setText(rsData.getString("ket_status_pernikahan"));
                        String tempatTinggal = rsData.getString("tempat_tinggal");
                        if("Rumah Sendiri".equals(tempatTinggal)) RbTinggalSendiri.setSelected(true);
                        else if("Rumah Keluarganya".equals(tempatTinggal)) RbTinggalKeluarga.setSelected(true);
                        else if("Lainnya".equals(tempatTinggal)) RbTinggalLainnya.setSelected(true);
                        else bgTempatTinggal.clearSelection();
                        TKetTempatTinggal.setText(rsData.getString("ket_tempat_tinggal"));
                        TPekerjaanKep.setText(rsData.getString("pekerjaan_kep"));
                        TPendidikanPasienKep.setText(rsData.getString("pendidikan_pasien_kep"));

                        // -- Skrining Geriatri --
                        String geri1 = rsData.getString("geri_60_multi_penyakit");
                        if("Ya".equals(geri1)) RbGeri1Ya.setSelected(true); else if("Tidak".equals(geri1)) RbGeri1Tidak.setSelected(true); else bgGeri1.clearSelection();
                        String geri2 = rsData.getString("geri_60_penurunan_fungsi");
                        if("Ya".equals(geri2)) RbGeri2Ya.setSelected(true); else if("Tidak".equals(geri2)) RbGeri2Tidak.setSelected(true); else bgGeri2.clearSelection();
                        String geri3 = rsData.getString("geri_70_1_penyakit");
                        if("Ya".equals(geri3)) RbGeri3Ya.setSelected(true); else if("Tidak".equals(geri3)) RbGeri3Tidak.setSelected(true); else bgGeri3.clearSelection();

                        // -- Skrining Resiko Jatuh --
                        String jatuhA = rsData.getString("jatuh_cara_berjalan");
                        if("Ya".equals(jatuhA)) RbJatuhCaraBerjalanYa.setSelected(true); else if("Tidak".equals(jatuhA)) RbJatuhCaraBerjalanTidak.setSelected(true); else bgJatuhCaraBerjalan.clearSelection();
                        String jatuhB = rsData.getString("jatuh_menopang_duduk");
                        if("Ya".equals(jatuhB)) RbJatuhMenopangYa.setSelected(true); else if("Tidak".equals(jatuhB)) RbJatuhMenopangTidak.setSelected(true); else bgJatuhMenopang.clearSelection();
                        hitungHasilJatuh();

                        // -- Pengkajian Spiritual --
                        String wajibIbadah = rsData.getString("wajib_ibadah");
                        if("Baligh".equals(wajibIbadah)) RbWajibIbadahBaligh.setSelected(true);
                        else if("Belum Baligh".equals(wajibIbadah)) RbWajibIbadahBelumBaligh.setSelected(true);
                        else if("Halangan Lain".equals(wajibIbadah)) RbWajibIbadahHalanganLain.setSelected(true);
                        else bgWajibIbadah.clearSelection();
                        TKetWajibIbadah.setText(rsData.getString("ket_wajib_ibadah"));
                        String thaharoh = rsData.getString("thaharoh");
                        if("Berwudhu".equals(thaharoh)) RbThaharohBerwudhu.setSelected(true); else if("Tayamum".equals(thaharoh)) RbThaharohTayamum.setSelected(true); else bgThaharoh.clearSelection();
                        String sholat = rsData.getString("sholat");
                        if("Berdiri".equals(sholat)) RbSholatBerdiri.setSelected(true);
                        else if("Duduk".equals(sholat)) RbSholatDuduk.setSelected(true);
                        else if("Berbaring".equals(sholat)) RbSholatBerbaring.setSelected(true);
                        else bgSholat.clearSelection();
                        String nilaiKepercayaan = rsData.getString("nilai_kepercayaan");
                        if("Tidak".equals(nilaiKepercayaan)) RbNilaiKepercayaanTidak.setSelected(true); else if("Ya".equals(nilaiKepercayaan)) RbNilaiKepercayaanYa.setSelected(true); else bgNilaiKepercayaan.clearSelection();
                        TKetNilaiKepercayaan.setText(rsData.getString("ket_nilai_kepercayaan"));
                        TAgama.setText(rsData.getString("agama_manual"));
                        CekCacatTidak.setSelected("Ya".equals(rsData.getString("cacat_tidak")));
                        CekCacatTunaNetra.setSelected("Ya".equals(rsData.getString("cacat_tuna_netra")));
                        CekCacatTunaDaksa.setSelected("Ya".equals(rsData.getString("cacat_tuna_daksa")));
                        CekCacatTunaRungu.setSelected("Ya".equals(rsData.getString("cacat_tuna_rungu")));
                        CekCacatTunaWicara.setSelected("Ya".equals(rsData.getString("cacat_tuna_wicara")));

                        // -- Skrining Gizi Dewasa (MST) --
                        String mstPenurunan = rsData.getString("mst_penurunan_bb");
                        if("Tidak Ada".equals(mstPenurunan)) RbMstTidakAda.setSelected(true);
                        else if("Tidak Yakin".equals(mstPenurunan)) RbMstTidakYakin.setSelected(true);
                        else if("Ya".equals(mstPenurunan)) RbMstYa.setSelected(true);
                        else bgMstPenurunanBb.clearSelection();
                        String mstKg = rsData.getString("mst_penurunan_bb_kg");
                        MstPenurunanBbKg.setSelectedItem(mstKg==null?"":mstKg);
                        String mstAsupan = rsData.getString("mst_asupan_berkurang");
                        if("Ya".equals(mstAsupan)) RbMstAsupanYa.setSelected(true); else if("Tidak".equals(mstAsupan)) RbMstAsupanTidak.setSelected(true); else bgMstAsupan.clearSelection();
                        String mstDiagnosis = rsData.getString("mst_diagnosis_khusus");
                        if("Ya".equals(mstDiagnosis)) RbMstDiagnosisYa.setSelected(true); else if("Tidak".equals(mstDiagnosis)) RbMstDiagnosisTidak.setSelected(true); else bgMstDiagnosis.clearSelection();
                        TMstDiagnosisSebutkan.setText(rsData.getString("mst_diagnosis_khusus_sebutkan"));
                        hitungTotalMst();

                        // -- Skrining Gizi Anak --
                        String giziKurus = rsData.getString("gizi_anak_kurus");
                        if("Ya".equals(giziKurus)) RbGiziAnakKurusYa.setSelected(true); else if("Tidak".equals(giziKurus)) RbGiziAnakKurusTidak.setSelected(true); else bgGiziAnakKurus.clearSelection();
                        String giziTurunBb = rsData.getString("gizi_anak_turun_bb");
                        if("Ya".equals(giziTurunBb)) RbGiziAnakTurunBbYa.setSelected(true); else if("Tidak".equals(giziTurunBb)) RbGiziAnakTurunBbTidak.setSelected(true); else bgGiziAnakTurunBb.clearSelection();
                        String giziKondisi = rsData.getString("gizi_anak_kondisi_khusus");
                        if("Ya".equals(giziKondisi)) RbGiziAnakKondisiYa.setSelected(true); else if("Tidak".equals(giziKondisi)) RbGiziAnakKondisiTidak.setSelected(true); else bgGiziAnakKondisi.clearSelection();
                        String giziRisiko = rsData.getString("gizi_anak_risiko_malnutrisi");
                        if("Ya".equals(giziRisiko)) RbGiziAnakRisikoYa.setSelected(true); else if("Tidak".equals(giziRisiko)) RbGiziAnakRisikoTidak.setSelected(true); else bgGiziAnakRisiko.clearSelection();
                        String giziKesimpulan = rsData.getString("gizi_anak_kesimpulan");
                        if("Risiko Rendah".equals(giziKesimpulan)) RbGiziAnakRendah.setSelected(true);
                        else if("Risiko Sedang".equals(giziKesimpulan)) RbGiziAnakSedang.setSelected(true);
                        else if("Risiko Tinggi".equals(giziKesimpulan)) RbGiziAnakTinggi.setSelected(true);
                        else bgGiziAnakKesimpulan.clearSelection();
                        hitungTotalGiziAnak();

                        // -- Asesmen Nyeri (toggle) --
                        String nyeriAda = rsData.getString("nyeri_ada");
                        if("Ya".equals(nyeriAda)) RbNyeriAdaYa.setSelected(true); else if("Tidak".equals(nyeriAda)) RbNyeriAdaTidak.setSelected(true); else bgNyeriAda.clearSelection();

                        // -- FLACC --
                        String flaccW = rsData.getString("flacc_wajah"); FlaccWajah.setSelectedItem(flaccW==null?"":flaccW);
                        String flaccK = rsData.getString("flacc_kaki"); FlaccKaki.setSelectedItem(flaccK==null?"":flaccK);
                        String flaccA = rsData.getString("flacc_aktivitas"); FlaccAktivitas.setSelectedItem(flaccA==null?"":flaccA);
                        String flaccM = rsData.getString("flacc_menangis"); FlaccMenangis.setSelectedItem(flaccM==null?"":flaccM);
                        String flaccB = rsData.getString("flacc_bersuara"); FlaccBersuara.setSelectedItem(flaccB==null?"":flaccB);
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
                        if("Tidak".equals(radiation)) RbRadiationTidak.setSelected(true); else if("Ya".equals(radiation)) RbRadiationYa.setSelected(true); else bgRadiation.clearSelection();
                        TRadiationKe.setText(rsData.getString("pqrst_radiation_ke"));
                        String severity = rsData.getString("pqrst_severity");
                        if("Tidak Nyeri".equals(severity)) RbSeverityTidakNyeri.setSelected(true);
                        else if("Ringan".equals(severity)) RbSeverityRingan.setSelected(true);
                        else if("Sedang".equals(severity)) RbSeveritySedang.setSelected(true);
                        else if("Berat".equals(severity)) RbSeverityBerat.setSelected(true);
                        else bgSeverity.clearSelection();
                        String time = rsData.getString("pqrst_time");
                        if("Terus Menerus".equals(time)) RbTimeTerusMenerus.setSelected(true);
                        else if("Hilang Timbul".equals(time)) RbTimeHilangTimbul.setSelected(true);
                        else if("<30 Menit".equals(time)) RbTimeKurang30.setSelected(true);
                        else if(">30 Menit".equals(time)) RbTimeLebih30.setSelected(true);
                        else bgTime.clearSelection();

                        // -- II. Intervensi Keperawatan --
                        CekIntervManajemenJalanNafas.setSelected("Ya".equals(rsData.getString("interv_manajemen_jalan_nafas")));
                        CekIntervSuctioningJalanNafas.setSelected("Ya".equals(rsData.getString("interv_suctioning_jalan_nafas")));
                        CekIntervManajemenAsamBasa.setSelected("Ya".equals(rsData.getString("interv_manajemen_asam_basa")));
                        CekIntervManajemenSyok.setSelected("Ya".equals(rsData.getString("interv_manajemen_syok")));
                        CekIntervManajemenNyeri.setSelected("Ya".equals(rsData.getString("interv_manajemen_nyeri")));
                        CekIntervManajemenCairan.setSelected("Ya".equals(rsData.getString("interv_manajemen_cairan")));
                        CekIntervMonitoringCairan.setSelected("Ya".equals(rsData.getString("interv_monitoring_cairan")));
                        CekIntervManajemenLingkungan.setSelected("Ya".equals(rsData.getString("interv_manajemen_lingkungan")));
                        CekIntervPendidikanKesehatan.setSelected("Ya".equals(rsData.getString("interv_pendidikan_kesehatan")));
                        CekIntervFeverTreatment.setSelected("Ya".equals(rsData.getString("interv_fever_treatment")));
                        CekIntervManajemenSensasiPerifer.setSelected("Ya".equals(rsData.getString("interv_manajemen_sensasi_perifer")));
                        CekIntervMonitoringKardiovaskuler.setSelected("Ya".equals(rsData.getString("interv_monitoring_kardiovaskuler")));
                        CekIntervPencegahanPenekananLuka.setSelected("Ya".equals(rsData.getString("interv_pencegahan_penekanan_luka")));
                        TIntervLainnya.setText(rsData.getString("interv_lainnya"));

                        // -- IV. Kardiopulmonal --
                        CekKardioPulseOximetri.setSelected("Ya".equals(rsData.getString("kardio_pulse_oximetri")));
                        CekKardioIntubasi.setSelected("Ya".equals(rsData.getString("kardio_intubasi")));
                        CekKardioRjp.setSelected("Ya".equals(rsData.getString("kardio_rjp")));
                        TKardioRjpKet.setText(rsData.getString("kardio_rjp_ket"));
                        CekKardioKardioversiDcShock.setSelected("Ya".equals(rsData.getString("kardio_kardioversi_dc_shock")));

                        // -- V. Tindakan Penunjang --
                        CekPenunjangCekLab.setSelected("Ya".equals(rsData.getString("penunjang_cek_lab")));
                        TPenunjangCekLabKet.setText(rsData.getString("penunjang_cek_lab_ket"));
                        CekPenunjangRontgen.setSelected("Ya".equals(rsData.getString("penunjang_rontgen")));
                        TPenunjangRontgenKet.setText(rsData.getString("penunjang_rontgen_ket"));
                        CekPenunjangCtScan.setSelected("Ya".equals(rsData.getString("penunjang_ct_scan")));
                        TPenunjangCtScanKet.setText(rsData.getString("penunjang_ct_scan_ket"));

                        // -- VI. Tindakan Kolaborasi --
                        CekKolabO2.setSelected("Ya".equals(rsData.getString("kolab_o2")));
                        TKolabO2Ket.setText(rsData.getString("kolab_o2_ket"));
                        CekKolabEkgMonitor.setSelected("Ya".equals(rsData.getString("kolab_ekg_monitor")));
                        CekKolabNebulizer.setSelected("Ya".equals(rsData.getString("kolab_nebulizer")));
                        CekKolabPasangInfus.setSelected("Ya".equals(rsData.getString("kolab_pasang_infus")));
                        TKolabPasangInfusKet.setText(rsData.getString("kolab_pasang_infus_ket"));
                        CekKolabInjeksiSyringePump.setSelected("Ya".equals(rsData.getString("kolab_injeksi_syringe_pump")));
                        CekKolabTransfusiDarah.setSelected("Ya".equals(rsData.getString("kolab_transfusi_darah")));
                        TKolabTransfusiDarahKet.setText(rsData.getString("kolab_transfusi_darah_ket"));
                        CekKolabNgt.setSelected("Ya".equals(rsData.getString("kolab_ngt")));
                        TKolabNgtUkuran.setText(rsData.getString("kolab_ngt_ukuran"));
                        CekKolabBilasLambung.setSelected("Ya".equals(rsData.getString("kolab_bilas_lambung")));
                        TKolabBilasLambungKet.setText(rsData.getString("kolab_bilas_lambung_ket"));
                        CekKolabDowerCatheter.setSelected("Ya".equals(rsData.getString("kolab_dower_catheter")));
                        TKolabDowerCatheterUkuran.setText(rsData.getString("kolab_dower_catheter_ukuran"));
                        CekKolabRestrainPengaman.setSelected("Ya".equals(rsData.getString("kolab_restrain_pengaman")));

                        // -- VII. Trauma / Bedah --
                        CekTraumaCervicalCollar.setSelected("Ya".equals(rsData.getString("trauma_cervical_collar")));
                        CekTraumaImmobilisasiTulangBelakang.setSelected("Ya".equals(rsData.getString("trauma_immobilisasi_tulang_belakang")));
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
                        CekObgynPeriksaGinekologi.setSelected("Ya".equals(rsData.getString("obgyn_periksa_ginekologi")));
                        CekObgynDopplerCtgUsg.setSelected("Ya".equals(rsData.getString("obgyn_doppler_ctg_usg")));
                        CekObgynPasangTampon.setSelected("Ya".equals(rsData.getString("obgyn_pasang_tampon")));

                        // -- Kondisi Pasien Saat Pindah/Pulang --
                        // kp_pukul (DATETIME) sudah lengkap tanggal+jam - tidak
                        // perlu digabung dgn kolom "tanggal" lagi spt sebelumnya
                        // (dulu kp_pukul cuma TIME, tanpa tanggal sendiri).
                        String kpPukul = rsData.getString("kp_pukul");
                        if(kpPukul!=null && !kpPukul.trim().equals("")){ setJamDariString(DTPKpPukul, kpPukul); }
                        TKpGcsE.setText(rsData.getString("kp_gcs_e"));
                        TKpGcsV.setText(rsData.getString("kp_gcs_v"));
                        TKpGcsM.setText(rsData.getString("kp_gcs_m"));
                        TKpTd.setText(rsData.getString("kp_td"));
                        TKpNadi.setText(rsData.getString("kp_nadi"));
                        String kpNadiReguler = rsData.getString("kp_nadi_reguler");
                        if("Reguler".equals(kpNadiReguler)) RbKpNadiReguler.setSelected(true); else if("Ireguler".equals(kpNadiReguler)) RbKpNadiIreguler.setSelected(true); else bgKpNadiReguler.clearSelection();
                        TKpSuhu.setText(rsData.getString("kp_suhu"));
                        TKpPernafasan.setText(rsData.getString("kp_pernafasan"));
                        TKpSpo.setText(rsData.getString("kp_spo"));
                        CekKpMasukRs.setSelected("Ya".equals(rsData.getString("kp_masuk_rs")));
                        TKpRuang.setText(rsData.getString("kp_ruang"));
                        TKpPetugasPenerima.setText(rsData.getString("kp_petugas_penerima"));
                        NmKpPetugasPenerima.setText(TKpPetugasPenerima.getText().isEmpty()?"":Sequel.CariPetugas(TKpPetugasPenerima.getText()));
                        String kpPendamping = rsData.getString("kp_pendamping");
                        if("Dokter".equals(kpPendamping)) RbKpPendampingDokter.setSelected(true); else if("Perawat".equals(kpPendamping)) RbKpPendampingPerawat.setSelected(true); else bgKpPendamping.clearSelection();
                        String kpTl = rsData.getString("kp_tindakan_lanjutan");
                        if("Kamar Operasi".equals(kpTl)) RbKpTlKamarOperasi.setSelected(true);
                        else if("Kamar Bersalin".equals(kpTl)) RbKpTlKamarBersalin.setSelected(true);
                        else if("HCU".equals(kpTl)) RbKpTlHcu.setSelected(true);
                        else if("Kamar Jenazah".equals(kpTl)) RbKpTlKamarJenazah.setSelected(true);
                        else if("Lainnya".equals(kpTl)) RbKpTlLainnya.setSelected(true);
                        else bgKpTindakanLanjutan.clearSelection();
                        TKpTindakanLanjutanKet.setText(rsData.getString("kp_tindakan_lanjutan_ket"));
                        CekKpDirujuk.setSelected("Ya".equals(rsData.getString("kp_dirujuk")));
                        TKpDirujukKet.setText(rsData.getString("kp_dirujuk_ket"));
                        TKpDirujukAlasan.setText(rsData.getString("kp_dirujuk_alasan"));
                        CekKpDipulangkan.setSelected("Ya".equals(rsData.getString("kp_dipulangkan")));
                        String kpDipulangkanPukul = rsData.getString("kp_dipulangkan_pukul");
                        if(kpDipulangkanPukul!=null && !kpDipulangkanPukul.trim().equals("")){ setJamDariString(DTPKpDipulangkanPukul, kpDipulangkanPukul); }
                        CekKpMeninggal.setSelected("Ya".equals(rsData.getString("kp_meninggal")));
                        String kpMeninggalPukul = rsData.getString("kp_meninggal_pukul");
                        if(kpMeninggalPukul!=null && !kpMeninggalPukul.trim().equals("")){ setJamDariString(DTPKpMeninggalPukul, kpMeninggalPukul); }
                        CekKpDoa.setSelected("Ya".equals(rsData.getString("kp_doa")));
                        CekKpEdukasiMakanObat.setSelected("Ya".equals(rsData.getString("kp_edukasi_makan_obat")));
                        CekKpEdukasiLuka.setSelected("Ya".equals(rsData.getString("kp_edukasi_luka")));
                        CekKpEdukasiDiet.setSelected("Ya".equals(rsData.getString("kp_edukasi_diet")));
                        TKpEdukasiDietKet.setText(rsData.getString("kp_edukasi_diet_ket"));
                        CekKpEdukasiLain.setSelected("Ya".equals(rsData.getString("kp_edukasi_lain")));
                        TKpEdukasiLainKet.setText(rsData.getString("kp_edukasi_lain_ket"));
                        KdDokterKp.setText(rsData.getString("kp_kd_dokter"));
                        NmDokterKp.setText(rsData.getString("kp_nm_dokter"));
                    }
                } finally { if(rsData!=null){ rsData.close(); } }
            } finally { if(psData!=null){ psData.close(); } }
        }catch(Exception e){
            System.out.println("Notif getData RM 03-4/05/06 : "+e);
        }

        // -- I. Diagnosis Keperawatan: tandai baris master yg cocok dgn
        // pilihan tersimpan utk no_rawat ini --
        try{
            java.util.Set<String> kodeTersimpan = new java.util.HashSet<>();
            PreparedStatement psMasalah = koneksi.prepareStatement(
                "select kode_masalah from penilaian_awal_keperawatan_igd_masalah where no_rawat=?");
            try{
                psMasalah.setString(1,noRawat);
                ResultSet rsMasalah = psMasalah.executeQuery();
                try{
                    while(rsMasalah.next()){ kodeTersimpan.add(rsMasalah.getString(1)); }
                } finally { if(rsMasalah!=null){ rsMasalah.close(); } }
            } finally { if(psMasalah!=null){ psMasalah.close(); } }
            for(i=0;i<tabModeMasalah.getRowCount();i++){
                tabModeMasalah.setValueAt(kodeTersimpan.contains(tabModeMasalah.getValueAt(i,1).toString()),i,0);
            }
        }catch(Exception e){
            System.out.println("Notif getData Masalah Keperawatan : "+e);
        }

        tampilObatInfus(noRawat);
        bangunRingkasanHtml();
    }

    /** Satu baris label:nilai utk ringkasan HTML (lihat bangunRingkasanHtml()). */
    private String baris(String label,String nilai){
        return "<tr class='isi'><td valign='middle' width='35%'>"+label+"</td>"+
               "<td valign='middle' width='65%'>"+((nilai==null||nilai.trim().isEmpty())?"-":nilai)+"</td></tr>";
    }

    /** Baris judul section (bgcolor abu-abu muda, span 2 kolom). */
    private String judulBaris(String judul){
        return "<tr class='isi'><td colspan='2' valign='middle' bgcolor='#FFFAF8' align='center'><b>"+judul+"</b></td></tr>";
    }

    /** Baris label:nilai dgn latar warna (utk hasil/kesimpulan yg perlu ditonjolkan). */
    private String barisWarna(String label,String nilai,String warna){
        return "<tr class='isi'><td valign='middle' width='35%'>"+label+"</td>"+
               "<td valign='middle' width='65%' bgcolor='"+warna+"' color='#ffffff'>"+((nilai==null||nilai.trim().isEmpty())?"-":nilai)+"</td></tr>";
    }

    /** Gabungan teks item checkbox yg tercentang (dipisah koma), "-" kalau tidak ada yg dicentang. */
    private String cekList(javax.swing.AbstractButton... cek){
        StringBuilder sb = new StringBuilder();
        for(javax.swing.AbstractButton c : cek){
            if(c.isSelected()){ if(sb.length()>0){ sb.append(", "); } sb.append(c.getText()); }
        }
        return sb.length()==0 ? "-" : sb.toString();
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
    private void bangunRingkasanHtml(){
        StringBuilder h = new StringBuilder();

        h.append(judulBaris("Pasien & Petugas"));
        h.append(baris("Nama Pasien",TPasien.getText()));
        h.append(baris("No.RM",TNoRM.getText()));
        h.append(baris("Tanggal Asuhan",TglAsuhan.getSelectedItem()==null?"":TglAsuhan.getSelectedItem().toString()));
        h.append(baris("Petugas",NmPetugas.getText()));
        h.append(baris("Dokter",NmDokterKp.getText()));

        h.append(judulBaris("I. Riwayat Kesehatan"));
        h.append(baris("Keluhan Utama",KeluhanUtama.getText().replaceAll("(\r\n|\r|\n)","<br>")));

        h.append(judulBaris("Riwayat Alergi"));
        h.append(baris("Alergi",RbAlergiYa.isSelected()?("Ya, "+TAlergiSebutkan.getText()):"Tidak"));

        h.append(judulBaris("Keadaan Umum"));
        h.append(baris("Tanda Vital","TD "+TdKep.getText()+" mmHg, Nadi "+NadiKep.getText()+"x/menit, Suhu "+SuhuKep.getText()+"°C, SpO2 "+SpoKep.getText()+"%, RR "+RrKep.getText()+"x/menit, BB "+BbKep.getText()+"Kg, TB "+TbKep.getText()+"Cm"));

        if("Hamil".equals(StatusKehamilan.getSelectedItem()==null?"":StatusKehamilan.getSelectedItem().toString())){
            h.append(judulBaris("Status Kehamilan"));
            h.append(baris("Hamil","Gravida "+Gravida.getText()+", Para "+Para.getText()+", Abortus "+Abortus.getText()+", HPHT "+HPHT.getText()));
        }

        h.append(judulBaris("Asesmen Psikologis"));
        h.append(baris("Kondisi",cekList(CekPsiTenang,CekPsiCemas,CekPsiTakut,CekPsiSedih,CekPsiMarah,CekPsiBunuhDiri)));
        if(RbMasalahPrilakuYa.isSelected()){
            h.append(baris("Masalah Prilaku","Ada, "+TMasalahPrilakuSebutkan.getText()));
        }

        h.append(judulBaris("Status Sosial dan Ekonomis"));
        h.append(baris("Status Pernikahan",RbNikahBelumMenikah.isSelected()?"Belum Menikah":(RbNikahMenikah.isSelected()?"Menikah":(RbNikahCerai.isSelected()?"Cerai":(RbNikahLainnya.isSelected()?"Lainnya, "+TKetStatusPernikahan.getText():"-")))));
        h.append(baris("Tempat Tinggal",RbTinggalSendiri.isSelected()?"Rumah Sendiri":(RbTinggalKeluarga.isSelected()?"Rumah Keluarganya":(RbTinggalLainnya.isSelected()?"Lainnya, "+TKetTempatTinggal.getText():"-"))));
        h.append(baris("Pekerjaan",TPekerjaanKep.getText()));
        h.append(baris("Pendidikan",TPendidikanPasienKep.getText()));

        h.append(judulBaris("Skrining Geriatri"));
        h.append(baris("≥60th & >1 Penyakit",RbGeri1Ya.isSelected()?"Ya":"Tidak"));
        h.append(baris("≥60th, 1 Penyakit + Penurunan Fungsi",RbGeri2Ya.isSelected()?"Ya":"Tidak"));
        h.append(baris("≥70th & 1 Penyakit",RbGeri3Ya.isSelected()?"Ya":"Tidak"));

        h.append(judulBaris("Skrining Resiko Pasien Jatuh"));
        h.append(baris("A. Cara Berjalan",RbJatuhCaraBerjalanYa.isSelected()?"Ya":"Tidak"));
        h.append(baris("B. Menopang Saat Duduk",RbJatuhMenopangYa.isSelected()?"Ya":"Tidak"));
        h.append(barisWarna("Hasil",RbJatuhBeresiko.isSelected()?"Beresiko":"Tidak Beresiko",RbJatuhBeresiko.isSelected()?"#AA0000":"#00AA00"));

        h.append(judulBaris("Pengkajian Spiritual"));
        h.append(baris("Agama",TAgama.getText()));
        h.append(baris("Wajib Ibadah",RbWajibIbadahBaligh.isSelected()?"Baligh":(RbWajibIbadahBelumBaligh.isSelected()?"Belum Baligh":(RbWajibIbadahHalanganLain.isSelected()?"Halangan Lain, "+TKetWajibIbadah.getText():"-"))));
        h.append(baris("Thaharoh",RbThaharohBerwudhu.isSelected()?"Berwudhu":(RbThaharohTayamum.isSelected()?"Tayamum":"-")));
        h.append(baris("Sholat",RbSholatBerdiri.isSelected()?"Berdiri":(RbSholatDuduk.isSelected()?"Duduk":(RbSholatBerbaring.isSelected()?"Berbaring":"-"))));
        h.append(baris("Nilai/Kepercayaan Tertentu",RbNilaiKepercayaanYa.isSelected()?("Ya, "+TKetNilaiKepercayaan.getText()):"Tidak"));
        h.append(baris("Hambatan Budaya/Bahasa","Ada".equals(StatusBudaya.getSelectedItem()==null?"":StatusBudaya.getSelectedItem().toString())?("Ada, "+KetBudaya.getText()):"Tidak Ada"));
        h.append(baris("Cacat Tubuh/Disabilitas",CekCacatTidak.isSelected()?"Tidak":cekList(CekCacatTunaNetra,CekCacatTunaDaksa,CekCacatTunaRungu,CekCacatTunaWicara)));

        h.append(judulBaris("Skrining Gizi Dewasa (MST)"));
        h.append(baris("Total Skor",TMstTotalSkor.getText()));
        h.append(barisWarna("Kesimpulan",RbMstRisiko.isSelected()?"Risiko":(RbMstTidakRisiko.isSelected()?"Tidak Risiko":"-"),RbMstRisiko.isSelected()?"#AA0000":"#00AA00"));

        h.append(judulBaris("Skrining Gizi Anak"));
        h.append(baris("Total Skor",TGiziAnakTotalSkor.getText()));
        String giziWarna = RbGiziAnakTinggi.isSelected()?"#AA0000":(RbGiziAnakSedang.isSelected()?"#FF9900":"#00AA00");
        h.append(barisWarna("Kesimpulan",RbGiziAnakRendah.isSelected()?"Risiko Rendah":(RbGiziAnakSedang.isSelected()?"Risiko Sedang":(RbGiziAnakTinggi.isSelected()?"Risiko Tinggi":"-")),giziWarna));

        h.append(judulBaris("Asesmen Nyeri"));
        h.append(baris("Nyeri",RbNyeriAdaYa.isSelected()?("Ya, Lokasi: "+Lokasi.getText()+", Skala: "+(SkalaNyeri.getSelectedItem()==null?"":SkalaNyeri.getSelectedItem().toString())):"Tidak"));

        h.append(judulBaris("FLACC"));
        h.append(baris("Total Skor",TFlaccTotal.getText()));

        h.append(judulBaris("PQRST"));
        h.append(baris("Provocation",cekList(CekProvCahaya,CekProvGelap,CekProvGerakan,CekProvBerbaring)));
        h.append(baris("Quality",cekList(CekQualTumpul,CekQualDitusuk,CekQualBerdenyut,CekQualTajam,CekQualPanas)));
        h.append(baris("Radiation",RbRadiationYa.isSelected()?("Ya, ke "+TRadiationKe.getText()):"Tidak"));
        h.append(baris("Severity",RbSeverityTidakNyeri.isSelected()?"Tidak Nyeri":(RbSeverityRingan.isSelected()?"Ringan":(RbSeveritySedang.isSelected()?"Sedang":(RbSeverityBerat.isSelected()?"Berat":"-")))));
        h.append(baris("Time",RbTimeTerusMenerus.isSelected()?"Terus Menerus":(RbTimeHilangTimbul.isSelected()?"Hilang Timbul":(RbTimeKurang30.isSelected()?"<30 Menit":(RbTimeLebih30.isSelected()?">30 Menit":"-")))));

        h.append(judulBaris("I. Diagnosis Keperawatan"));
        StringBuilder masalah = new StringBuilder();
        for(int mi=0;mi<tabModeMasalah.getRowCount();mi++){
            if(Boolean.TRUE.equals(tabModeMasalah.getValueAt(mi,0))){
                if(masalah.length()>0){ masalah.append(", "); }
                masalah.append(tabModeMasalah.getValueAt(mi,2).toString());
            }
        }
        h.append(baris("Masalah Terpilih",masalah.toString()));

        h.append(judulBaris("II. Intervensi Keperawatan"));
        h.append(baris("Tindakan",cekList(CekIntervManajemenJalanNafas,CekIntervSuctioningJalanNafas,CekIntervManajemenAsamBasa,
            CekIntervManajemenSyok,CekIntervManajemenNyeri,CekIntervManajemenCairan,CekIntervMonitoringCairan,
            CekIntervManajemenLingkungan,CekIntervPendidikanKesehatan,CekIntervFeverTreatment,
            CekIntervManajemenSensasiPerifer,CekIntervMonitoringKardiovaskuler,CekIntervPencegahanPenekananLuka)+
            (TIntervLainnya.getText().trim().isEmpty()?"":", "+TIntervLainnya.getText())));

        h.append(judulBaris("III. Tindakan Keperawatan Mandiri"));
        h.append(baris("Rencana",Rencana.getText().replaceAll("(\r\n|\r|\n)","<br>")));

        h.append(judulBaris("IV-IX. Kardiopulmonal / Penunjang / Kolaborasi / Trauma / THT-Mata / Obgyn"));
        h.append(baris("Kardiopulmonal",cekList(CekKardioPulseOximetri,CekKardioIntubasi,CekKardioRjp,CekKardioKardioversiDcShock)));
        h.append(baris("Tindakan Penunjang",cekList(CekPenunjangCekLab,CekPenunjangRontgen,CekPenunjangCtScan)));
        h.append(baris("Tindakan Kolaborasi",cekList(CekKolabO2,CekKolabEkgMonitor,CekKolabNebulizer,CekKolabPasangInfus,
            CekKolabInjeksiSyringePump,CekKolabTransfusiDarah,CekKolabNgt,CekKolabBilasLambung,CekKolabDowerCatheter,CekKolabRestrainPengaman)));
        h.append(baris("Trauma/Bedah",cekList(CekTraumaCervicalCollar,CekTraumaImmobilisasiTulangBelakang,CekTraumaGips,
            CekTraumaPerawatanLuka,CekTraumaJahitLuka,CekTraumaBalutanSpalk,CekTraumaSelangDadaWsd)));
        h.append(baris("THT/Mata",cekList(CekThtIrigasiMataTelinga,CekThtAmbilKorpusAlienum)));
        h.append(baris("Obgyn",cekList(CekObgynMembantuPartus,CekObgynPeriksaGinekologi,CekObgynDopplerCtgUsg,CekObgynPasangTampon)));

        h.append(judulBaris("Pemberian Obat/Infus"));
        if(tabModeObatInfus.getRowCount()==0){
            h.append(baris("Obat/Infus","-"));
        }else{
            for(int oi=0;oi<tabModeObatInfus.getRowCount();oi++){
                h.append(baris(
                    tabModeObatInfus.getValueAt(oi,0)+" - "+tabModeObatInfus.getValueAt(oi,1),
                    "Dosis "+tabModeObatInfus.getValueAt(oi,2)+", Rute "+tabModeObatInfus.getValueAt(oi,3)
                ));
            }
        }

        h.append(judulBaris("Kondisi Pasien Saat Pindah/Pulang dari IGD"));
        String disposisi = CekKpDipulangkan.isSelected()?"Dipulangkan":(CekKpMeninggal.isSelected()?"Meninggal":(CekKpDoa.isSelected()?"Death On Arrival":(CekKpDirujuk.isSelected()?"Dirujuk":(CekKpMasukRs.isSelected()?"Masuk Rumah Sakit":"-"))));
        h.append(baris("Disposisi",disposisi));
        h.append(baris("Pendidikan Kesehatan Pulang",cekList(CekKpEdukasiMakanObat,CekKpEdukasiLuka,CekKpEdukasiDiet,CekKpEdukasiLain)));

        LoadHTML.setText(
            "<html><table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
            h.toString()+
            "</table></html>"
        );
        // JEditorPane bawaan Swing scroll ke BAWAH dokumen setelah setText()
        // (bukan tetap di atas) - akibatnya baris pertama (Nama Pasien/No.RM
        // di section "Pasien & Petugas") tampak "hilang" krn ter-scroll ke
        // luar area terlihat. Paksa kembali ke awal dokumen - pola sama
        // persis yg sudah dipakai di SuratPermintaanSecondOpinion.java.
        LoadHTML.setCaretPosition(0);
    }

    private void isRawat() {
        try {
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien, if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"+
                    "pasien.tgl_lahir,reg_periksa.tgl_registrasi, "+
                    "pasien.stts_nikah,pasien.pekerjaan,pasien.pnd,penjab.png_jawab,reg_periksa.jam_reg "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj "+
                    "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1,TNoRw.getText());
                rs=ps.executeQuery();
                if(rs.next()){
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    StatusPernikahan.setText(rs.getString("stts_nikah"));
                    Pekerjaan.setText(rs.getString("pekerjaan"));
                    PendidikanPasien.setText(rs.getString("pnd"));
                    Pembayaran.setText(rs.getString("png_jawab"));
                    TanggalRegistrasi.setText(rs.getString("tgl_registrasi")+" "+rs.getString("jam_reg"));
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }
        // Pengkajian baru (belum tentu ada data lama) - kosongkan daftar
        // Pemberian Obat/Infus, akan dimuat ulang oleh getData() kalau
        // ternyata baris existing dipilih setelah ini.
        Valid.tabelKosong(tabModeObatInfus);
    }

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);    
        isRawat();
    }
    
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getpenilaian_awal_keperawatan_igd());
        BtnHapus.setEnabled(akses.getpenilaian_awal_keperawatan_igd());
        BtnEdit.setEnabled(akses.getpenilaian_awal_keperawatan_igd());
        BtnEdit.setEnabled(akses.getpenilaian_awal_keperawatan_igd());
        BtnTambahMasalah.setEnabled(akses.getmaster_masalah_keperawatan_igd());  
        BtnTambahRencana.setEnabled(akses.getmaster_rencana_keperawatan_igd()); 
        if(akses.getjml2()>=1){
            KdPetugas.setEditable(false);
            BtnPetugas.setEnabled(false);
            KdPetugas.setText(akses.getkode());
            NmPetugas.setText(Sequel.CariPetugas(KdPetugas.getText()));
            if(NmPetugas.getText().equals("")){
                KdPetugas.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan petugas...!!");
            }
        }  
        
        if(TANGGALMUNDUR.equals("no")){
            if(!akses.getkode().equals("Admin Utama")){
                TglAsuhan.setEditable(false);
                TglAsuhan.setEnabled(false);
            }
        }
    }

    public void setTampil(){
       TabRawat.setSelectedIndex(1);
    }
    
    private void tampilMasalah() {
        try{
            Valid.tabelKosong(tabModeMasalah);
            file=new File("./cache/masalahkeperawatanigd.iyem");
            file.createNewFile();
            fileWriter = new FileWriter(file);
            StringBuilder iyembuilder = new StringBuilder();
            ps=koneksi.prepareStatement("select * from master_masalah_keperawatan_igd order by master_masalah_keperawatan_igd.kode_masalah");
            try {
                rs=ps.executeQuery();
                while(rs.next()){
                    tabModeMasalah.addRow(new Object[]{false,rs.getString(1),rs.getString(2)});
                    iyembuilder.append("{\"KodeMasalah\":\"").append(rs.getString(1)).append("\",\"NamaMasalah\":\"").append(rs.getString(2)).append("\"},");
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
            
            if (iyembuilder.length() > 0) {
                iyembuilder.setLength(iyembuilder.length() - 1);
                fileWriter.write("{\"masalahkeperawatanigd\":["+iyembuilder+"]}");
                fileWriter.flush();
            }
            
            fileWriter.close();
            iyembuilder=null;
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }finally {
            if (fileWriter != null) try { fileWriter.close(); } catch (Exception e) {}
        }
    }
    
    private void tampilMasalah2() {
        try{
            jml=0;
            for(i=0;i<tbMasalahKeperawatan.getRowCount();i++){
                if(tbMasalahKeperawatan.getValueAt(i,0).toString().equals("true")){
                    jml++;
                }
            }

            pilih=new boolean[jml]; 
            kode=new String[jml];
            masalah=new String[jml];

            index=0;        
            for(i=0;i<tbMasalahKeperawatan.getRowCount();i++){
                if(tbMasalahKeperawatan.getValueAt(i,0).toString().equals("true")){
                    pilih[index]=true;
                    kode[index]=tbMasalahKeperawatan.getValueAt(i,1).toString();
                    masalah[index]=tbMasalahKeperawatan.getValueAt(i,2).toString();
                    index++;
                }
            } 

            Valid.tabelKosong(tabModeMasalah);

            for(i=0;i<jml;i++){
                tabModeMasalah.addRow(new Object[] {
                    pilih[i],kode[i],masalah[i]
                });
            }
            
            pilih=null;
            kode=null;
            masalah=null;
            
            myObj = new FileReader("./cache/masalahkeperawatanigd.iyem");
            root = mapper.readTree(myObj);
            response = root.path("masalahkeperawatanigd");
            if(response.isArray()){
                for(JsonNode list:response){
                    if(list.path("KodeMasalah").asText().toLowerCase().contains(TCariMasalah.getText().toLowerCase())||list.path("NamaMasalah").asText().toLowerCase().contains(TCariMasalah.getText().toLowerCase())){
                        tabModeMasalah.addRow(new Object[]{
                            false,list.path("KodeMasalah").asText(),list.path("NamaMasalah").asText()
                        });                    
                    }
                }
            }
            myObj.close();
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }finally {
            if (myObj != null) try { myObj.close(); } catch (Exception e) {}
            response = null;
            root = null;
        }
    }
    
    private void tampilRencana() {
        try{
            file=new File("./cache/rencanakeperawatanigd.iyem");
            file.createNewFile();
            fileWriter = new FileWriter(file);
            StringBuilder iyembuilder = new StringBuilder();
            ps=koneksi.prepareStatement("select * from master_rencana_keperawatan_igd order by master_rencana_keperawatan_igd.kode_rencana");
            try {
                rs=ps.executeQuery();
                while(rs.next()){
                    iyembuilder.append("{\"KodeMasalah\":\"").append(rs.getString(1)).append("\",\"KodeRencana\":\"").append(rs.getString(2)).append("\",\"NamaRencana\":\"").append(rs.getString(3)).append("\"},");
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
            
            if (iyembuilder.length() > 0) {
                iyembuilder.setLength(iyembuilder.length() - 1);
                fileWriter.write("{\"rencanakeperawatanigd\":["+iyembuilder+"]}");
                fileWriter.flush();
            }
            
            fileWriter.close();
            iyembuilder=null;
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }finally {
            if (fileWriter != null) try { fileWriter.close(); } catch (Exception e) {}
        }
    }
    
    private void tampilRencana2() {
        try{
            jml=0;
            for(i=0;i<tbRencanaKeperawatan.getRowCount();i++){
                if(tbRencanaKeperawatan.getValueAt(i,0).toString().equals("true")){
                    jml++;
                }
            }

            pilih=new boolean[jml]; 
            kode=new String[jml];
            masalah=new String[jml];

            index=0;        
            for(i=0;i<tbRencanaKeperawatan.getRowCount();i++){
                if(tbRencanaKeperawatan.getValueAt(i,0).toString().equals("true")){
                    pilih[index]=true;
                    kode[index]=tbRencanaKeperawatan.getValueAt(i,1).toString();
                    masalah[index]=tbRencanaKeperawatan.getValueAt(i,2).toString();
                    index++;
                }
            } 

            Valid.tabelKosong(tabModeRencana);

            for(i=0;i<jml;i++){
                tabModeRencana.addRow(new Object[] {
                    pilih[i],kode[i],masalah[i]
                });
            }
            
            pilih=null;
            kode=null;
            masalah=null;

            myObj = new FileReader("./cache/rencanakeperawatanigd.iyem");
            root = mapper.readTree(myObj);
            response = root.path("rencanakeperawatanigd");
            if(response.isArray()){
                for(i=0;i<tbMasalahKeperawatan.getRowCount();i++){
                    if(tbMasalahKeperawatan.getValueAt(i,0).toString().equals("true")){
                        for(JsonNode list:response){
                            if(list.path("KodeMasalah").asText().toLowerCase().equals(tbMasalahKeperawatan.getValueAt(i,1).toString())&&
                                    list.path("NamaRencana").asText().toLowerCase().contains(TCariRencana.getText().toLowerCase())){
                                tabModeRencana.addRow(new Object[]{
                                    false,list.path("KodeRencana").asText(),list.path("NamaRencana").asText()
                                });                    
                            }
                        }
                    }
                }
            }
            myObj.close();
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }finally {
            if (myObj != null) try { myObj.close(); } catch (Exception e) {}
            response = null;
            root = null;
        }
    }
    
    private void isMenu(){
        if(ChkAccor.isSelected()==true){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(470,HEIGHT));
            FormMenu.setVisible(true);
            ScrollHTML.setVisible(true);
            ChkAccor.setVisible(true);
        }else if(ChkAccor.isSelected()==false){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(15,HEIGHT));
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
        "tanggal","informasi","keluhan_utama","rpd","rpo","status_kehamilan","gravida","para","abortus","hpht",
        "tekanan","pupil","neurosensorik","integumen","turgor","edema","mukosa","perdarahan","jumlah_perdarahan","warna_perdarahan",
        "intoksikasi","bab","xbab","kbab","wbab","bak","xbak","wbak","lbak",
        "hubungan","budaya","ket_budaya","pendidikan_pj","ket_pendidikan_pj","edukasi","ket_edukasi","kemampuan","aktifitas","alat_bantu","ket_bantu",
        "lokasi","skala_nyeri","nyeri_hilang","ket_nyeri","pada_dokter","ket_dokter","lapor","ket_lapor","rencana","nip",

        // -- Riwayat Alergi --
        "alergi_ada","alergi_sebutkan",

        // -- Keadaan Umum (Tanda Vital) --
        "td","nadi","suhu","spo","rr","bb","tb",

        // -- Asesmen Psikologis --
        "psi_tenang","psi_cemas","psi_takut","psi_sedih","psi_marah","psi_bunuh_diri","psi_masalah_prilaku","psi_masalah_prilaku_sebutkan",

        // -- Status Sosial dan Ekonomis --
        "status_pernikahan","ket_status_pernikahan","tempat_tinggal","ket_tempat_tinggal","pekerjaan_kep","pendidikan_pasien_kep",

        // -- Skrining Geriatri --
        "geri_60_multi_penyakit","geri_60_penurunan_fungsi","geri_70_1_penyakit",

        // -- Skrining Resiko Jatuh --
        "jatuh_cara_berjalan","jatuh_menopang_duduk","jatuh_hasil",

        // -- Pengkajian Spiritual --
        "wajib_ibadah","ket_wajib_ibadah","thaharoh","sholat","nilai_kepercayaan","ket_nilai_kepercayaan",
        "agama_manual","cacat_tidak","cacat_tuna_netra","cacat_tuna_daksa","cacat_tuna_rungu","cacat_tuna_wicara",

        // -- Skrining Gizi Dewasa (MST) --
        "mst_penurunan_bb","mst_penurunan_bb_kg","mst_asupan_berkurang","mst_total_skor","mst_diagnosis_khusus","mst_diagnosis_khusus_sebutkan","mst_kesimpulan",

        // -- Skrining Gizi Anak --
        "gizi_anak_kurus","gizi_anak_turun_bb","gizi_anak_kondisi_khusus","gizi_anak_risiko_malnutrisi","gizi_anak_total_skor","gizi_anak_kesimpulan",

        // -- Asesmen Nyeri (toggle) --
        "nyeri_ada",

        // -- FLACC --
        "flacc_wajah","flacc_kaki","flacc_aktivitas","flacc_menangis","flacc_bersuara","flacc_total",

        // -- PQRST --
        "pqrst_prov_cahaya","pqrst_prov_gelap","pqrst_prov_gerakan","pqrst_prov_berbaring",
        "pqrst_qual_tumpul","pqrst_qual_ditusuk","pqrst_qual_berdenyut","pqrst_qual_tajam","pqrst_qual_panas",
        "pqrst_radiation","pqrst_radiation_ke","pqrst_severity","pqrst_time",

        // -- II. Intervensi Keperawatan --
        "interv_manajemen_jalan_nafas","interv_suctioning_jalan_nafas","interv_manajemen_asam_basa","interv_manajemen_syok",
        "interv_manajemen_nyeri","interv_manajemen_cairan","interv_monitoring_cairan","interv_manajemen_lingkungan",
        "interv_pendidikan_kesehatan","interv_fever_treatment","interv_manajemen_sensasi_perifer","interv_monitoring_kardiovaskuler",
        "interv_pencegahan_penekanan_luka","interv_lainnya",

        // -- IV. Kardiopulmonal --
        "kardio_pulse_oximetri","kardio_intubasi","kardio_rjp","kardio_rjp_ket","kardio_kardioversi_dc_shock",

        // -- V. Tindakan Penunjang --
        "penunjang_cek_lab","penunjang_cek_lab_ket","penunjang_rontgen","penunjang_rontgen_ket","penunjang_ct_scan","penunjang_ct_scan_ket",

        // -- VI. Tindakan Kolaborasi --
        "kolab_o2","kolab_o2_ket","kolab_ekg_monitor","kolab_nebulizer","kolab_pasang_infus","kolab_pasang_infus_ket",
        "kolab_injeksi_syringe_pump","kolab_transfusi_darah","kolab_transfusi_darah_ket","kolab_ngt","kolab_ngt_ukuran",
        "kolab_bilas_lambung","kolab_bilas_lambung_ket","kolab_dower_catheter","kolab_dower_catheter_ukuran","kolab_restrain_pengaman",

        // -- VII. Trauma / Bedah --
        "trauma_cervical_collar","trauma_immobilisasi_tulang_belakang","trauma_gips","trauma_perawatan_luka",
        "trauma_jahit_luka","trauma_balutan_spalk","trauma_selang_dada_wsd","trauma_lainnya",

        // -- VIII. THT / Mata --
        "tht_irigasi_mata_telinga","tht_ambil_korpus_alienum",

        // -- IX. Tindakan Obgyn --
        "obgyn_membantu_partus","obgyn_periksa_ginekologi","obgyn_doppler_ctg_usg","obgyn_pasang_tampon",

        // -- Kondisi Pasien Saat Pindah/Pulang --
        "kp_pukul","kp_gcs_e","kp_gcs_v","kp_gcs_m","kp_td","kp_nadi","kp_nadi_reguler","kp_suhu","kp_pernafasan","kp_spo",
        "kp_masuk_rs","kp_ruang","kp_petugas_penerima","kp_pendamping","kp_tindakan_lanjutan","kp_tindakan_lanjutan_ket",
        "kp_dirujuk","kp_dirujuk_ket","kp_dirujuk_alasan","kp_dipulangkan","kp_dipulangkan_pukul","kp_meninggal","kp_meninggal_pukul","kp_doa",
        "kp_edukasi_makan_obat","kp_edukasi_luka","kp_edukasi_diet","kp_edukasi_diet_ket","kp_edukasi_lain","kp_edukasi_lain_ket",
        "kp_kd_dokter","kp_kd_perawat"
    };

    /**
     * [RM 03-4/05/06] Nilai dari semua widget aktif, urutan HARUS sinkron
     * dengan KOLOM_KEP di atas. Dipakai bersama oleh ganti() dan simpan().
     */
    private String[] nilaiKep(){
        String masalahPrilaku = RbMasalahPrilakuYa.isSelected()?"Ya":(RbMasalahPrilakuTidak.isSelected()?"Tidak":null);
        String statusNikah = RbNikahBelumMenikah.isSelected()?"Belum Menikah":(RbNikahMenikah.isSelected()?"Menikah":(RbNikahCerai.isSelected()?"Cerai":(RbNikahLainnya.isSelected()?"Lainnya":null)));
        String tempatTinggal = RbTinggalSendiri.isSelected()?"Rumah Sendiri":(RbTinggalKeluarga.isSelected()?"Rumah Keluarganya":(RbTinggalLainnya.isSelected()?"Lainnya":null));
        String geri1 = RbGeri1Ya.isSelected()?"Ya":(RbGeri1Tidak.isSelected()?"Tidak":null);
        String geri2 = RbGeri2Ya.isSelected()?"Ya":(RbGeri2Tidak.isSelected()?"Tidak":null);
        String geri3 = RbGeri3Ya.isSelected()?"Ya":(RbGeri3Tidak.isSelected()?"Tidak":null);
        String jatuhA = RbJatuhCaraBerjalanYa.isSelected()?"Ya":(RbJatuhCaraBerjalanTidak.isSelected()?"Tidak":null);
        String jatuhB = RbJatuhMenopangYa.isSelected()?"Ya":(RbJatuhMenopangTidak.isSelected()?"Tidak":null);
        String jatuhHasil = RbJatuhTidakBeresiko.isSelected()?"Tidak Beresiko":(RbJatuhBeresiko.isSelected()?"Beresiko":null);
        String wajibIbadah = RbWajibIbadahBaligh.isSelected()?"Baligh":(RbWajibIbadahBelumBaligh.isSelected()?"Belum Baligh":(RbWajibIbadahHalanganLain.isSelected()?"Halangan Lain":null));
        String thaharoh = RbThaharohBerwudhu.isSelected()?"Berwudhu":(RbThaharohTayamum.isSelected()?"Tayamum":null);
        String sholat = RbSholatBerdiri.isSelected()?"Berdiri":(RbSholatDuduk.isSelected()?"Duduk":(RbSholatBerbaring.isSelected()?"Berbaring":null));
        String nilaiKepercayaan = RbNilaiKepercayaanTidak.isSelected()?"Tidak":(RbNilaiKepercayaanYa.isSelected()?"Ya":null);
        String mstPenurunan = RbMstTidakAda.isSelected()?"Tidak Ada":(RbMstTidakYakin.isSelected()?"Tidak Yakin":(RbMstYa.isSelected()?"Ya":null));
        String mstAsupan = RbMstAsupanYa.isSelected()?"Ya":(RbMstAsupanTidak.isSelected()?"Tidak":null);
        String mstDiagnosis = RbMstDiagnosisYa.isSelected()?"Ya":(RbMstDiagnosisTidak.isSelected()?"Tidak":null);
        String mstKesimpulan = RbMstRisiko.isSelected()?"Risiko":(RbMstTidakRisiko.isSelected()?"Tidak Risiko":null);
        String giziKurus = RbGiziAnakKurusYa.isSelected()?"Ya":(RbGiziAnakKurusTidak.isSelected()?"Tidak":null);
        String giziTurunBb = RbGiziAnakTurunBbYa.isSelected()?"Ya":(RbGiziAnakTurunBbTidak.isSelected()?"Tidak":null);
        String giziKondisi = RbGiziAnakKondisiYa.isSelected()?"Ya":(RbGiziAnakKondisiTidak.isSelected()?"Tidak":null);
        String giziRisiko = RbGiziAnakRisikoYa.isSelected()?"Ya":(RbGiziAnakRisikoTidak.isSelected()?"Tidak":null);
        String giziKesimpulan = RbGiziAnakRendah.isSelected()?"Risiko Rendah":(RbGiziAnakSedang.isSelected()?"Risiko Sedang":(RbGiziAnakTinggi.isSelected()?"Risiko Tinggi":null));
        String nyeriAda = RbNyeriAdaYa.isSelected()?"Ya":(RbNyeriAdaTidak.isSelected()?"Tidak":null);
        String radiation = RbRadiationTidak.isSelected()?"Tidak":(RbRadiationYa.isSelected()?"Ya":null);
        String severity = RbSeverityTidakNyeri.isSelected()?"Tidak Nyeri":(RbSeverityRingan.isSelected()?"Ringan":(RbSeveritySedang.isSelected()?"Sedang":(RbSeverityBerat.isSelected()?"Berat":null)));
        String time = RbTimeTerusMenerus.isSelected()?"Terus Menerus":(RbTimeHilangTimbul.isSelected()?"Hilang Timbul":(RbTimeKurang30.isSelected()?"<30 Menit":(RbTimeLebih30.isSelected()?">30 Menit":null)));
        String kpNadiReguler = RbKpNadiReguler.isSelected()?"Reguler":(RbKpNadiIreguler.isSelected()?"Ireguler":null);
        String kpPendamping = RbKpPendampingDokter.isSelected()?"Dokter":(RbKpPendampingPerawat.isSelected()?"Perawat":null);
        String kpTl = RbKpTlKamarOperasi.isSelected()?"Kamar Operasi":(RbKpTlKamarBersalin.isSelected()?"Kamar Bersalin":(RbKpTlHcu.isSelected()?"HCU":(RbKpTlKamarJenazah.isSelected()?"Kamar Jenazah":(RbKpTlLainnya.isSelected()?"Lainnya":null))));
        String kpDipulangkanPukul = CekKpDipulangkan.isSelected()?DTPKpDipulangkanPukul.getTeksSql():null;
        String kpMeninggalPukul = CekKpMeninggal.isSelected()?DTPKpMeninggalPukul.getTeksSql():null;

        return new String[]{
            Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),
            Informasi.getSelectedItem().toString(),KeluhanUtama.getText(),RPD.getText(),RPO.getText(),StatusKehamilan.getSelectedItem().toString(),
            Gravida.getText(),Para.getText(),Abortus.getText(),HPHT.getText(),
            Tekanan.getSelectedItem().toString(),Pupil.getSelectedItem().toString(),Neurosensorik.getSelectedItem().toString(),Integumen.getSelectedItem().toString(),
            Turgor.getSelectedItem().toString(),Edema.getSelectedItem().toString(),Mukosa.getSelectedItem().toString(),Perdarahan.getSelectedItem().toString(),
            JumlahPerdarahan.getText(),WarnaPerdarahan.getText(),
            Intoksikasi.getSelectedItem().toString(),BAB.getText(),XBAB.getText(),KBAB.getText(),WBAB.getText(),BAK.getText(),XBAK.getText(),WBAK.getText(),LBAK.getText(),
            Hubungan.getSelectedItem().toString(),StatusBudaya.getSelectedItem().toString(),KetBudaya.getText(),PendidikanPJ.getSelectedItem().toString(),
            KetPendidikanPJ.getText(),Edukasi.getSelectedItem().toString(),KetEdukasi.getText(),ADL.getSelectedItem().toString(),Aktifitas.getSelectedItem().toString(),
            AlatBantu.getSelectedItem().toString(),KetAlatBantu.getText(),
            Lokasi.getText(),SkalaNyeri.getSelectedItem().toString(),NyeriHilang.getSelectedItem().toString(),KetNyeri.getText(),
            PadaDokter.getSelectedItem().toString(),KetDokter.getText(),Lapor.getSelectedItem().toString(),KetLapor.getText(),Rencana.getText(),KdPetugas.getText(),

            RbAlergiYa.isSelected()?"Ya":"Tidak",TAlergiSebutkan.getText(),

            TdKep.getText(),NadiKep.getText(),SuhuKep.getText(),SpoKep.getText(),RrKep.getText(),BbKep.getText(),TbKep.getText(),

            ya(CekPsiTenang),ya(CekPsiCemas),ya(CekPsiTakut),ya(CekPsiSedih),ya(CekPsiMarah),ya(CekPsiBunuhDiri),masalahPrilaku,TMasalahPrilakuSebutkan.getText(),

            statusNikah,TKetStatusPernikahan.getText(),tempatTinggal,TKetTempatTinggal.getText(),TPekerjaanKep.getText(),TPendidikanPasienKep.getText(),

            geri1,geri2,geri3,

            jatuhA,jatuhB,jatuhHasil,

            wajibIbadah,TKetWajibIbadah.getText(),thaharoh,sholat,nilaiKepercayaan,TKetNilaiKepercayaan.getText(),
            TAgama.getText(),ya(CekCacatTidak),ya(CekCacatTunaNetra),ya(CekCacatTunaDaksa),ya(CekCacatTunaRungu),ya(CekCacatTunaWicara),

            mstPenurunan,kosongKeNull(MstPenurunanBbKg.getSelectedItem().toString()),mstAsupan,TMstTotalSkor.getText(),mstDiagnosis,TMstDiagnosisSebutkan.getText(),mstKesimpulan,

            giziKurus,giziTurunBb,giziKondisi,giziRisiko,TGiziAnakTotalSkor.getText(),giziKesimpulan,

            nyeriAda,

            kosongKeNull(FlaccWajah.getSelectedItem().toString()),kosongKeNull(FlaccKaki.getSelectedItem().toString()),kosongKeNull(FlaccAktivitas.getSelectedItem().toString()),
            kosongKeNull(FlaccMenangis.getSelectedItem().toString()),kosongKeNull(FlaccBersuara.getSelectedItem().toString()),TFlaccTotal.getText(),

            ya(CekProvCahaya),ya(CekProvGelap),ya(CekProvGerakan),ya(CekProvBerbaring),
            ya(CekQualTumpul),ya(CekQualDitusuk),ya(CekQualBerdenyut),ya(CekQualTajam),ya(CekQualPanas),
            radiation,TRadiationKe.getText(),severity,time,

            ya(CekIntervManajemenJalanNafas),ya(CekIntervSuctioningJalanNafas),ya(CekIntervManajemenAsamBasa),ya(CekIntervManajemenSyok),
            ya(CekIntervManajemenNyeri),ya(CekIntervManajemenCairan),ya(CekIntervMonitoringCairan),ya(CekIntervManajemenLingkungan),
            ya(CekIntervPendidikanKesehatan),ya(CekIntervFeverTreatment),ya(CekIntervManajemenSensasiPerifer),ya(CekIntervMonitoringKardiovaskuler),
            ya(CekIntervPencegahanPenekananLuka),TIntervLainnya.getText(),

            ya(CekKardioPulseOximetri),ya(CekKardioIntubasi),ya(CekKardioRjp),TKardioRjpKet.getText(),ya(CekKardioKardioversiDcShock),

            ya(CekPenunjangCekLab),TPenunjangCekLabKet.getText(),ya(CekPenunjangRontgen),TPenunjangRontgenKet.getText(),ya(CekPenunjangCtScan),TPenunjangCtScanKet.getText(),

            ya(CekKolabO2),TKolabO2Ket.getText(),ya(CekKolabEkgMonitor),ya(CekKolabNebulizer),ya(CekKolabPasangInfus),TKolabPasangInfusKet.getText(),
            ya(CekKolabInjeksiSyringePump),ya(CekKolabTransfusiDarah),TKolabTransfusiDarahKet.getText(),ya(CekKolabNgt),TKolabNgtUkuran.getText(),
            ya(CekKolabBilasLambung),TKolabBilasLambungKet.getText(),ya(CekKolabDowerCatheter),TKolabDowerCatheterUkuran.getText(),ya(CekKolabRestrainPengaman),

            ya(CekTraumaCervicalCollar),ya(CekTraumaImmobilisasiTulangBelakang),ya(CekTraumaGips),ya(CekTraumaPerawatanLuka),
            ya(CekTraumaJahitLuka),ya(CekTraumaBalutanSpalk),ya(CekTraumaSelangDadaWsd),TTraumaLainnya.getText(),

            ya(CekThtIrigasiMataTelinga),ya(CekThtAmbilKorpusAlienum),

            ya(CekObgynMembantuPartus),ya(CekObgynPeriksaGinekologi),ya(CekObgynDopplerCtgUsg),ya(CekObgynPasangTampon),

            CekKpMasukRs.isSelected()||CekKpDipulangkan.isSelected()||CekKpMeninggal.isSelected()||CekKpDoa.isSelected()||CekKpDirujuk.isSelected()?DTPKpPukul.getTeksSql():null,
            TKpGcsE.getText(),TKpGcsV.getText(),TKpGcsM.getText(),TKpTd.getText(),TKpNadi.getText(),kpNadiReguler,TKpSuhu.getText(),TKpPernafasan.getText(),TKpSpo.getText(),
            ya(CekKpMasukRs),TKpRuang.getText(),TKpPetugasPenerima.getText(),kpPendamping,kpTl,TKpTindakanLanjutanKet.getText(),
            ya(CekKpDirujuk),TKpDirujukKet.getText(),TKpDirujukAlasan.getText(),ya(CekKpDipulangkan),kpDipulangkanPukul,ya(CekKpMeninggal),kpMeninggalPukul,ya(CekKpDoa),
            ya(CekKpEdukasiMakanObat),ya(CekKpEdukasiLuka),ya(CekKpEdukasiDiet),TKpEdukasiDietKet.getText(),ya(CekKpEdukasiLain),TKpEdukasiLainKet.getText(),
            KdDokterKp.getText(),KdPetugas.getText()
        };
    }

    private void ganti() {
        StringBuilder set = new StringBuilder();
        for(String kolom : KOLOM_KEP){
            set.append(kolom).append("=?,");
        }
        set.setLength(set.length()-1);

        String[] nilai = nilaiKep();
        String[] param = new String[nilai.length+1];
        System.arraycopy(nilai,0,param,0,nilai.length);
        param[nilai.length] = tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();

        if(Sequel.mengedittf("penilaian_awal_keperawatan_igd","no_rawat=?",set.toString(),param.length,param)==true){
            tbObat.setValueAt(TNoRw.getText(),tbObat.getSelectedRow(),0);
            tbObat.setValueAt(TNoRM.getText(),tbObat.getSelectedRow(),1);
            tbObat.setValueAt(TPasien.getText(),tbObat.getSelectedRow(),2);
            tbObat.setValueAt(TglLahir.getText(),tbObat.getSelectedRow(),3);
            tbObat.setValueAt(Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),tbObat.getSelectedRow(),4);
            tbObat.setValueAt(KdPetugas.getText(),tbObat.getSelectedRow(),5);
            tbObat.setValueAt(NmPetugas.getText(),tbObat.getSelectedRow(),6);

            Sequel.meghapus("penilaian_awal_keperawatan_igd_masalah","no_rawat",TNoRw.getText());
            for (i = 0; i < tbMasalahKeperawatan.getRowCount(); i++) {
                if(tbMasalahKeperawatan.getValueAt(i,0).toString().equals("true")){
                    Sequel.menyimpantf2("penilaian_awal_keperawatan_igd_masalah","?,?",2,new String[]{TNoRw.getText(),tbMasalahKeperawatan.getValueAt(i,1).toString()});
                }
            }
            emptTeks();
            TabRawat.setSelectedIndex(1);
        }
    }

    private void hapus() {
        if(Sequel.queryu2tf("delete from penilaian_awal_keperawatan_igd where no_rawat=?",1,new String[]{
                tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })==true){
            TNoRM1.setText("");
            TPasien1.setText("");
            Valid.tabelKosong(tabModeDetailMasalah);
            Valid.tabelKosong(tabModeDetailRencana);
            ChkAccor.setSelected(false);
            isMenu();
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText(""+tabMode.getRowCount());
            emptTeks();
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
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
        for(String k : KOLOM_KEP){
            kolom.append(",").append(k);
            placeholder.append(",?");
        }

        String[] nilai = nilaiKep();
        String[] param = new String[nilai.length+1];
        param[0] = TNoRw.getText();
        System.arraycopy(nilai,0,param,1,nilai.length);

        boolean sukses = false;
        try{
            PreparedStatement psSimpan = koneksi.prepareStatement(
                "insert into penilaian_awal_keperawatan_igd ("+kolom+") values ("+placeholder+")");
            try{
                for(int j=0;j<param.length;j++){
                    psSimpan.setString(j+1,param[j]);
                }
                psSimpan.executeUpdate();
                sukses = true;
            } finally {
                if(psSimpan!=null){ psSimpan.close(); }
            }
        }catch(Exception e){
            System.out.println("Notif Simpan RM 03-4/05/06 : "+e);
            JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan No.Rawat yang sama sudah dimasukkan sebelumnya...!");
        }

        if(sukses){
            tabMode.addRow(new Object[]{
                TNoRw.getText(),TNoRM.getText(),TPasien.getText(),TglLahir.getText(),
                Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),
                KdPetugas.getText(),NmPetugas.getText()
            });
            LCount.setText(""+tabMode.getRowCount());
            for (i = 0; i < tbMasalahKeperawatan.getRowCount(); i++) {
                if(tbMasalahKeperawatan.getValueAt(i,0).toString().equals("true")){
                    Sequel.menyimpantf2("penilaian_awal_keperawatan_igd_masalah","?,?",2,new String[]{TNoRw.getText(),tbMasalahKeperawatan.getValueAt(i,1).toString()});
                }
            }
            emptTeks();
        }
    }
    
    private void LoadRencana(){
        tampilRencana();
        tampilRencana2();
    }
    
    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) return;

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
