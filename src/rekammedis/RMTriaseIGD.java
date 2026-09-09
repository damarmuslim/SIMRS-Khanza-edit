/*
  Dilarang keras menggandakan/mengcopy/menyebarkan/membajak/mendecompile 
  Software ini dalam bentuk apapun tanpa seijin pembuat software
  (Khanza.Soft Media). Bagi yang sengaja membajak softaware ini ta
  npa ijin, kami sumpahi sial 1000 turunan, miskin sampai 500 turu
  nan. Selalu mendapat kecelakaan sampai 400 turunan. Anak pertama
  nya cacat tidak punya kaki sampai 300 turunan. Susah cari jodoh
  sampai umur 50 tahun sampai 200 turunan. Ya Alloh maafkan kami 
  karena telah berdoa buruk, semua ini kami lakukan karena kami ti
  dak pernah rela karya kami dibajak tanpa ijin.
 */

package rekammedis;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
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
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariPegawai;


/**
 *
 * @author perpustakaan
 */
public final class RMTriaseIGD extends javax.swing.JDialog {
    private DefaultTableModel tabMode,tabModePemeriksaan,tabModeSkala1,tabModeSkala2,tabModeSkala3,tabModeSkala4,tabModeSkala5,tabModePemeriksaan2;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,ps2,ps3;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private ResultSet rs,rs2,rs3;
    private int i=0,jml=0,index=0,jmlskala1=0,jmlskala2=0,jmlskala3=0,jmlskala4=0,jmlskala5=0;
    private boolean[] pilih; 
    private String[] kode,pengkajian;
    private String keputusan="",pilihan="",datatriase="",finger="",kodepetugas="";
    private StringBuilder htmlContent;
    private boolean sukses=true;
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public RMTriaseIGD(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        widget.IconStyle.pasang(this);
        initHeaderTambahan();
        initPatientBanner();
        initActionBar();
        this.setLocation(8,1);
        tabMode=new DefaultTableModel(null,new Object[]{
                "No.Rawat","No.RM","Nama Pasien","Tgl.Kunjungan","Cara Masuk","Transportasi",
                "Alasan Kedatangan","Keterangan","Kode Kasus","Macam Kasus"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbTriase.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbTriase.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbTriase.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 10; i++) {
            TableColumn column = tbTriase.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(160);
            }else if(i==3){
                column.setPreferredWidth(120);
            }else if(i==4){
                column.setPreferredWidth(80);
            }else if(i==5){
                column.setPreferredWidth(80);
            }else if(i==6){
                column.setPreferredWidth(110);
            }else if(i==7){
                column.setPreferredWidth(180);
            }else if(i==8){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==9){
                column.setPreferredWidth(300);
            }
        }
        tbTriase.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModePemeriksaan=new DefaultTableModel(null,new Object[]{
                "Kode Pemeriksaan","Pemeriksaan"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbPemeriksaan.setModel(tabModePemeriksaan);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbPemeriksaan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbPemeriksaan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 2; i++) {
            TableColumn column = tbPemeriksaan.getColumnModel().getColumn(i);
            if(i==0){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==1){
                column.setPreferredWidth(255);
            }
        }
        tbPemeriksaan.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModePemeriksaan2=new DefaultTableModel(null,new Object[]{
                "Kode Pemeriksaan","Pemeriksaan"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbPemeriksaan2.setModel(tabModePemeriksaan2);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbPemeriksaan2.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbPemeriksaan2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 2; i++) {
            TableColumn column = tbPemeriksaan2.getColumnModel().getColumn(i);
            if(i==0){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==1){
                column.setPreferredWidth(255);
            }
        }
        tbPemeriksaan2.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeSkala1=new DefaultTableModel(null,new Object[]{
                "P","Kode","Immediate/Segera"
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
        tbSkala1.setModel(tabModeSkala1);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbSkala1.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbSkala1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 3; i++) {
            TableColumn column = tbSkala1.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==2){
                column.setPreferredWidth(340);
            }
        }
        tbSkala1.getTableHeader().setForeground(new Color(170,00,0));
        tbSkala1.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeSkala2=new DefaultTableModel(null,new Object[]{
                "P","Kode","Emergensi"
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
        tbSkala2.setModel(tabModeSkala2);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbSkala2.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbSkala2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 3; i++) {
            TableColumn column = tbSkala2.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==2){
                column.setPreferredWidth(340);
            }
        }
        tbSkala2.getTableHeader().setForeground(new Color(255,00,0));
        tbSkala2.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeSkala3=new DefaultTableModel(null,new Object[]{
                "P","Kode","Urgensi"
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
        tbSkala3.setModel(tabModeSkala3);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbSkala3.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbSkala3.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 3; i++) {
            TableColumn column = tbSkala3.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==2){
                column.setPreferredWidth(340);
            }
        }
        tbSkala3.getTableHeader().setForeground(new Color(200,200,0));
        tbSkala3.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeSkala4=new DefaultTableModel(null,new Object[]{
                "P","Kode","Semi Urgensi/Urgensi Rendah"
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
        tbSkala4.setModel(tabModeSkala4);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbSkala4.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbSkala4.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 3; i++) {
            TableColumn column = tbSkala4.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==2){
                column.setPreferredWidth(340);
            }
        }
        tbSkala4.getTableHeader().setForeground(new Color(0,170,0));
        tbSkala4.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeSkala5=new DefaultTableModel(null,new Object[]{
                "P","Kode","Non Urgensi"
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
        tbSkala5.setModel(tabModeSkala5);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbSkala5.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbSkala5.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 3; i++) {
            TableColumn column = tbSkala5.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==2){
                column.setPreferredWidth(340);
            }
        }
        tbSkala5.getTableHeader().setForeground(new Color(150,150,150));
        tbSkala5.setDefaultRenderer(Object.class, new WarnaTable());
        
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
        TCariPemeriksaan.setDocument(new batasInput((int)100).getKata(TCariPemeriksaan));
        TCariPemeriksaan2.setDocument(new batasInput((int)100).getKata(TCariPemeriksaan2));
        TCariSkala1.setDocument(new batasInput((int)100).getKata(TCariSkala1));
        TCariSkala3.setDocument(new batasInput((int)100).getKata(TCariSkala3));
        KeteranganKedatangan.setDocument(new batasInput((int)100).getKata(KeteranganKedatangan));
        PrimerSuhu.setDocument(new batasInput((byte)5).getKata(PrimerSuhu));
        SekunderSuhu.setDocument(new batasInput((byte)5).getKata(SekunderSuhu));
        PrimerNyeri.setDocument(new batasInput((byte)5).getKata(PrimerNyeri));
        SekunderNyeri.setDocument(new batasInput((byte)5).getKata(SekunderNyeri));
        PrimerTensi.setDocument(new batasInput((byte)8).getKata(PrimerTensi));
        SekunderTensi.setDocument(new batasInput((byte)8).getKata(SekunderTensi));
        PrimerNadi.setDocument(new batasInput((byte)3).getKata(PrimerNadi));
        SekunderNadi.setDocument(new batasInput((byte)3).getKata(SekunderNadi));
        PrimerSaturasi.setDocument(new batasInput((byte)3).getKata(PrimerSaturasi));
        SekunderSaturasi.setDocument(new batasInput((byte)3).getKata(SekunderSaturasi));
        PrimerRespirasi.setDocument(new batasInput((byte)3).getKata(PrimerRespirasi));
        SekunderRespirasi.setDocument(new batasInput((byte)3).getKata(SekunderRespirasi));
        PrimerCatatan.setDocument(new batasInput((int)100).getKata(PrimerCatatan));
        SekunderCatatan.setDocument(new batasInput((int)100).getKata(SekunderCatatan));
        PrimerKeluhanUtama.setDocument(new batasInput((int)400).getKata(PrimerKeluhanUtama));
        SekunderAnamnesa.setDocument(new batasInput((int)400).getKata(SekunderAnamnesa));
       
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        LoadHTML2.setEditable(true);
        LoadHTML2.setEditorKit(kit);
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
        LoadHTML2.setDocument(doc);

        ChkAccor.setSelected(false);
        isMenu();
        initPacsTab();

        // [Bagian E] "Semua field kotak, jangan ada yang membulat" - HANYA
        // form ini (ThemeConfig.ARC global TIDAK disentuh). Dipanggil paling
        // akhir supaya sekali jalan mencakup header (GEN) + seluruh tab PACS
        // yang baru dibangun initPacsTab().
        kotakkanField(internalFrame1);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        LoadHTML2 = new widget.editorpane();
        internalFrame1 = new widget.InternalFrame();
        TabPilihan = new javax.swing.JTabbedPane();
        ScrollTriase = new widget.ScrollPane();
        FormTriase = new widget.InternalFrame();
        TabTriase = new javax.swing.JTabbedPane();
        internalFrame5 = new widget.InternalFrame();
        internalFrame7 = new widget.InternalFrame();
        scrollPane1 = new widget.ScrollPane();
        PrimerKeluhanUtama = new widget.TextArea();
        jLabel9 = new widget.Label();
        jLabel24 = new widget.Label();
        PrimerKubutuhanKusus = new widget.ComboBox();
        jLabel10 = new widget.Label();
        PrimerSuhu = new widget.TextBox();
        jLabel16 = new widget.Label();
        PrimerSaturasi = new widget.TextBox();
        jLabel25 = new widget.Label();
        PrimerNyeri = new widget.TextBox();
        PrimerNadi = new widget.TextBox();
        jLabel26 = new widget.Label();
        PrimerRespirasi = new widget.TextBox();
        jLabel27 = new widget.Label();
        PrimerTensi = new widget.TextBox();
        jLabel11 = new widget.Label();
        internalFrame8 = new widget.InternalFrame();
        jLabel12 = new widget.Label();
        PrimerKodePetugas = new widget.TextBox();
        PrimerNamaPetugas = new widget.TextBox();
        btnPrimerPetugas = new widget.Button();
        jLabel28 = new widget.Label();
        PrimerTanggalTriase = new widget.Tanggal();
        jLabel29 = new widget.Label();
        PrimerResusitasi = new widget.RadioButton();
        PrimerKritis = new widget.RadioButton();
        label14 = new widget.Label();
        PrimerCatatan = new widget.TextBox();
        internalFrame3 = new widget.InternalFrame();
        internalFrame9 = new widget.InternalFrame();
        jPanel3 = new javax.swing.JPanel();
        panelisi5 = new widget.panelisi();
        label10 = new widget.Label();
        TCariPemeriksaan = new widget.TextBox();
        BtnCariPemeriksaan = new widget.Button();
        BtnTambahPemeriksaan = new widget.Button();
        Scroll2 = new widget.ScrollPane();
        tbPemeriksaan = new widget.Table();
        jPanel4 = new javax.swing.JPanel();
        panelisi6 = new widget.panelisi();
        label11 = new widget.Label();
        TCariSkala1 = new widget.TextBox();
        BtnCariSkala1 = new widget.Button();
        BtnTambahSkala1 = new widget.Button();
        TabSkala1dan2 = new javax.swing.JTabbedPane();
        Scroll3 = new widget.ScrollPane();
        tbSkala1 = new widget.Table();
        Scroll4 = new widget.ScrollPane();
        tbSkala2 = new widget.Table();
        internalFrame10 = new widget.InternalFrame();
        internalFrame11 = new widget.InternalFrame();
        scrollPane2 = new widget.ScrollPane();
        SekunderAnamnesa = new widget.TextArea();
        jLabel13 = new widget.Label();
        jLabel14 = new widget.Label();
        SekunderSuhu = new widget.TextBox();
        jLabel17 = new widget.Label();
        SekunderSaturasi = new widget.TextBox();
        jLabel31 = new widget.Label();
        SekunderNyeri = new widget.TextBox();
        SekunderNadi = new widget.TextBox();
        jLabel32 = new widget.Label();
        SekunderRespirasi = new widget.TextBox();
        jLabel33 = new widget.Label();
        SekunderTensi = new widget.TextBox();
        jLabel15 = new widget.Label();
        internalFrame12 = new widget.InternalFrame();
        jLabel30 = new widget.Label();
        SekunderKodePetugas = new widget.TextBox();
        SekunderNamaPetugas = new widget.TextBox();
        btnSekunderPetugas = new widget.Button();
        jLabel37 = new widget.Label();
        SekunderTanggalTriase = new widget.Tanggal();
        jLabel38 = new widget.Label();
        SekunderZonaKuning = new widget.RadioButton();
        SekunderZonaHijau = new widget.RadioButton();
        label15 = new widget.Label();
        SekunderCatatan = new widget.TextBox();
        internalFrame13 = new widget.InternalFrame();
        internalFrame14 = new widget.InternalFrame();
        jPanel5 = new javax.swing.JPanel();
        panelisi7 = new widget.panelisi();
        label12 = new widget.Label();
        TCariPemeriksaan2 = new widget.TextBox();
        BtnCariPemeriksaan1 = new widget.Button();
        BtnTambahPemeriksaan1 = new widget.Button();
        Scroll5 = new widget.ScrollPane();
        tbPemeriksaan2 = new widget.Table();
        jPanel6 = new javax.swing.JPanel();
        panelisi8 = new widget.panelisi();
        label13 = new widget.Label();
        TCariSkala3 = new widget.TextBox();
        BtnCariSkala2 = new widget.Button();
        BtnTambahSkala2 = new widget.Button();
        TabSkala3dan4dan5 = new javax.swing.JTabbedPane();
        Scroll6 = new widget.ScrollPane();
        tbSkala3 = new widget.Table();
        Scroll7 = new widget.ScrollPane();
        tbSkala4 = new widget.Table();
        Scroll8 = new widget.ScrollPane();
        tbSkala5 = new widget.Table();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        jLabel18 = new widget.Label();
        TanggalKunjungan = new widget.Tanggal();
        jLabel20 = new widget.Label();
        CaraMasuk = new widget.ComboBox();
        jLabel22 = new widget.Label();
        Transportasi = new widget.ComboBox();
        jLabel23 = new widget.Label();
        AlasanKedatangan = new widget.ComboBox();
        jLabel5 = new widget.Label();
        KdKasus = new widget.TextBox();
        NmKasus = new widget.TextBox();
        btnKasus = new widget.Button();
        KeteranganKedatangan = new widget.TextBox();
        jLabel8 = new widget.Label();
        internalFrame4 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbTriase = new widget.Table();
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
        ScrollHTML = new widget.ScrollPane();
        LoadHTML = new widget.editorpane();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();

        LoadHTML2.setBorder(null);
        LoadHTML2.setName("LoadHTML2"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Triase IGD ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        TabPilihan.setBackground(new java.awt.Color(255, 255, 254));
        TabPilihan.setForeground(new java.awt.Color(50, 50, 50));
        TabPilihan.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        TabPilihan.setName("TabPilihan"); // NOI18N
        TabPilihan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabPilihanMouseClicked(evt);
            }
        });

        ScrollTriase.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        ScrollTriase.setName("ScrollTriase"); // NOI18N
        ScrollTriase.setOpaque(true);

        FormTriase.setBorder(null);
        FormTriase.setName("FormTriase"); // NOI18N
        FormTriase.setLayout(new java.awt.BorderLayout(1, 1));

        TabTriase.setBackground(new java.awt.Color(255, 255, 254));
        TabTriase.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        TabTriase.setForeground(new java.awt.Color(50, 50, 50));
        TabTriase.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        TabTriase.setName("TabTriase"); // NOI18N
        TabTriase.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabTriaseMouseClicked(evt);
            }
        });

        internalFrame5.setBorder(null);
        internalFrame5.setName("internalFrame5"); // NOI18N
        internalFrame5.setLayout(new java.awt.BorderLayout());

        internalFrame7.setBorder(null);
        internalFrame7.setName("internalFrame7"); // NOI18N
        internalFrame7.setPreferredSize(new java.awt.Dimension(0, 104));
        internalFrame7.setLayout(null);

        scrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane1.setName("scrollPane1"); // NOI18N

        PrimerKeluhanUtama.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        PrimerKeluhanUtama.setColumns(20);
        PrimerKeluhanUtama.setRows(5);
        PrimerKeluhanUtama.setName("PrimerKeluhanUtama"); // NOI18N
        PrimerKeluhanUtama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerKeluhanUtamaKeyPressed(evt);
            }
        });
        scrollPane1.setViewportView(PrimerKeluhanUtama);

        internalFrame7.add(scrollPane1);
        scrollPane1.setBounds(97, 10, 320, 53);

        jLabel9.setText("Keluhan Utama :");
        jLabel9.setName("jLabel9"); // NOI18N
        internalFrame7.add(jLabel9);
        jLabel9.setBounds(0, 10, 93, 23);

        jLabel24.setText("Kebutuhan Khusus :");
        jLabel24.setName("jLabel24"); // NOI18N
        internalFrame7.add(jLabel24);
        jLabel24.setBounds(360, 70, 119, 23);

        PrimerKubutuhanKusus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "UPPA", "Airborne", "Dekontaminan" }));
        PrimerKubutuhanKusus.setName("PrimerKubutuhanKusus"); // NOI18N
        PrimerKubutuhanKusus.setPreferredSize(new java.awt.Dimension(55, 28));
        PrimerKubutuhanKusus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerKubutuhanKususKeyPressed(evt);
            }
        });
        internalFrame7.add(PrimerKubutuhanKusus);
        PrimerKubutuhanKusus.setBounds(483, 70, 202, 23);

        jLabel10.setText("Suhu (C) :");
        jLabel10.setName("jLabel10"); // NOI18N
        internalFrame7.add(jLabel10);
        jLabel10.setBounds(420, 10, 59, 23);

        PrimerSuhu.setFocusTraversalPolicyProvider(true);
        PrimerSuhu.setName("PrimerSuhu"); // NOI18N
        PrimerSuhu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerSuhuKeyPressed(evt);
            }
        });
        internalFrame7.add(PrimerSuhu);
        PrimerSuhu.setBounds(483, 10, 55, 23);

        jLabel16.setText("Saturasi O²(%) :");
        jLabel16.setName("jLabel16"); // NOI18N
        internalFrame7.add(jLabel16);
        jLabel16.setBounds(0, 70, 93, 23);

        PrimerSaturasi.setFocusTraversalPolicyProvider(true);
        PrimerSaturasi.setName("PrimerSaturasi"); // NOI18N
        PrimerSaturasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerSaturasiKeyPressed(evt);
            }
        });
        internalFrame7.add(PrimerSaturasi);
        PrimerSaturasi.setBounds(97, 70, 55, 23);

        jLabel25.setText("Nyeri :");
        jLabel25.setName("jLabel25"); // NOI18N
        internalFrame7.add(jLabel25);
        jLabel25.setBounds(547, 10, 79, 23);

        PrimerNyeri.setHighlighter(null);
        PrimerNyeri.setName("PrimerNyeri"); // NOI18N
        PrimerNyeri.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerNyeriKeyPressed(evt);
            }
        });
        internalFrame7.add(PrimerNyeri);
        PrimerNyeri.setBounds(630, 10, 55, 23);

        PrimerNadi.setFocusTraversalPolicyProvider(true);
        PrimerNadi.setName("PrimerNadi"); // NOI18N
        PrimerNadi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrimerNadiActionPerformed(evt);
            }
        });
        PrimerNadi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerNadiKeyPressed(evt);
            }
        });
        internalFrame7.add(PrimerNadi);
        PrimerNadi.setBounds(630, 40, 55, 23);

        jLabel26.setText("Nadi(/menit) :");
        jLabel26.setName("jLabel26"); // NOI18N
        internalFrame7.add(jLabel26);
        jLabel26.setBounds(547, 40, 79, 23);

        PrimerRespirasi.setHighlighter(null);
        PrimerRespirasi.setName("PrimerRespirasi"); // NOI18N
        PrimerRespirasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerRespirasiKeyPressed(evt);
            }
        });
        internalFrame7.add(PrimerRespirasi);
        PrimerRespirasi.setBounds(284, 70, 55, 23);

        jLabel27.setText("Respirasi(/menit) :");
        jLabel27.setName("jLabel27"); // NOI18N
        internalFrame7.add(jLabel27);
        jLabel27.setBounds(180, 70, 100, 23);

        PrimerTensi.setHighlighter(null);
        PrimerTensi.setName("PrimerTensi"); // NOI18N
        PrimerTensi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerTensiKeyPressed(evt);
            }
        });
        internalFrame7.add(PrimerTensi);
        PrimerTensi.setBounds(483, 40, 55, 23);

        jLabel11.setText("Tensi :");
        jLabel11.setName("jLabel11"); // NOI18N
        internalFrame7.add(jLabel11);
        jLabel11.setBounds(420, 40, 59, 23);

        internalFrame5.add(internalFrame7, java.awt.BorderLayout.PAGE_START);

        internalFrame8.setBorder(null);
        internalFrame8.setName("internalFrame8"); // NOI18N
        internalFrame8.setPreferredSize(new java.awt.Dimension(0, 74));
        internalFrame8.setLayout(null);

        jLabel12.setText("Dokter/Petugas IGD :");
        jLabel12.setName("jLabel12"); // NOI18N
        internalFrame8.add(jLabel12);
        jLabel12.setBounds(212, 40, 120, 23);

        PrimerKodePetugas.setEditable(false);
        PrimerKodePetugas.setHighlighter(null);
        PrimerKodePetugas.setName("PrimerKodePetugas"); // NOI18N
        PrimerKodePetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerKodePetugasKeyPressed(evt);
            }
        });
        internalFrame8.add(PrimerKodePetugas);
        PrimerKodePetugas.setBounds(336, 40, 110, 23);

        PrimerNamaPetugas.setEditable(false);
        PrimerNamaPetugas.setName("PrimerNamaPetugas"); // NOI18N
        PrimerNamaPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerNamaPetugasKeyPressed(evt);
            }
        });
        internalFrame8.add(PrimerNamaPetugas);
        PrimerNamaPetugas.setBounds(448, 40, 210, 23);

        btnPrimerPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPrimerPetugas.setMnemonic('1');
        btnPrimerPetugas.setToolTipText("Alt+1");
        btnPrimerPetugas.setName("btnPrimerPetugas"); // NOI18N
        btnPrimerPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrimerPetugasActionPerformed(evt);
            }
        });
        btnPrimerPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPrimerPetugasKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                btnPrimerPetugasKeyReleased(evt);
            }
        });
        internalFrame8.add(btnPrimerPetugas);
        btnPrimerPetugas.setBounds(660, 40, 28, 23);

        jLabel28.setText("Plan/Keputusan :");
        jLabel28.setName("jLabel28"); // NOI18N
        internalFrame8.add(jLabel28);
        jLabel28.setBounds(362, 10, 90, 23);

        PrimerTanggalTriase.setForeground(new java.awt.Color(50, 70, 50));
        PrimerTanggalTriase.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "01-08-2023 11:11:45" }));
        PrimerTanggalTriase.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        PrimerTanggalTriase.setName("PrimerTanggalTriase"); // NOI18N
        PrimerTanggalTriase.setOpaque(false);
        PrimerTanggalTriase.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerTanggalTriaseKeyPressed(evt);
            }
        });
        internalFrame8.add(PrimerTanggalTriase);
        PrimerTanggalTriase.setBounds(69, 40, 135, 23);

        jLabel29.setText("Tgl.Triase :");
        jLabel29.setName("jLabel29"); // NOI18N
        internalFrame8.add(jLabel29);
        jLabel29.setBounds(0, 40, 65, 23);

        buttonGroup1.add(PrimerResusitasi);
        PrimerResusitasi.setForeground(new java.awt.Color(170, 0, 0));
        PrimerResusitasi.setSelected(true);
        PrimerResusitasi.setText("Ruang Resusitasi");
        PrimerResusitasi.setName("PrimerResusitasi"); // NOI18N
        PrimerResusitasi.setPreferredSize(new java.awt.Dimension(40, 20));
        internalFrame8.add(PrimerResusitasi);
        PrimerResusitasi.setBounds(456, 10, 120, 23);

        buttonGroup1.add(PrimerKritis);
        PrimerKritis.setForeground(new java.awt.Color(250, 0, 0));
        PrimerKritis.setText("Ruang Kritis");
        PrimerKritis.setName("PrimerKritis"); // NOI18N
        internalFrame8.add(PrimerKritis);
        PrimerKritis.setBounds(590, 10, 100, 23);

        label14.setText("Catatan :");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(60, 23));
        internalFrame8.add(label14);
        label14.setBounds(0, 10, 65, 23);

        PrimerCatatan.setToolTipText("Alt+C");
        PrimerCatatan.setName("PrimerCatatan"); // NOI18N
        PrimerCatatan.setPreferredSize(new java.awt.Dimension(140, 23));
        PrimerCatatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrimerCatatanKeyPressed(evt);
            }
        });
        internalFrame8.add(PrimerCatatan);
        PrimerCatatan.setBounds(69, 10, 290, 23);

        internalFrame5.add(internalFrame8, java.awt.BorderLayout.PAGE_END);

        internalFrame3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4));
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        internalFrame9.setBorder(null);
        internalFrame9.setName("internalFrame9"); // NOI18N
        internalFrame9.setPreferredSize(new java.awt.Dimension(691, 74));
        internalFrame9.setLayout(new java.awt.BorderLayout(2, 1));

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(280, 102));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi5.setBorder(null);
        panelisi5.setName("panelisi5"); // NOI18N
        panelisi5.setPreferredSize(new java.awt.Dimension(100, 43));
        panelisi5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label10.setText("Key Word :");
        label10.setName("label10"); // NOI18N
        label10.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi5.add(label10);

        TCariPemeriksaan.setToolTipText("Alt+C");
        TCariPemeriksaan.setName("TCariPemeriksaan"); // NOI18N
        TCariPemeriksaan.setPreferredSize(new java.awt.Dimension(140, 23));
        TCariPemeriksaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariPemeriksaanKeyPressed(evt);
            }
        });
        panelisi5.add(TCariPemeriksaan);

        BtnCariPemeriksaan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariPemeriksaan.setMnemonic('1');
        BtnCariPemeriksaan.setToolTipText("Alt+1");
        BtnCariPemeriksaan.setName("BtnCariPemeriksaan"); // NOI18N
        BtnCariPemeriksaan.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariPemeriksaan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariPemeriksaanActionPerformed(evt);
            }
        });
        BtnCariPemeriksaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariPemeriksaanKeyPressed(evt);
            }
        });
        panelisi5.add(BtnCariPemeriksaan);

        BtnTambahPemeriksaan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambahPemeriksaan.setMnemonic('3');
        BtnTambahPemeriksaan.setToolTipText("Alt+3");
        BtnTambahPemeriksaan.setName("BtnTambahPemeriksaan"); // NOI18N
        BtnTambahPemeriksaan.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambahPemeriksaan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahPemeriksaanActionPerformed(evt);
            }
        });
        panelisi5.add(BtnTambahPemeriksaan);

        jPanel3.add(panelisi5, java.awt.BorderLayout.PAGE_END);

        Scroll2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        tbPemeriksaan.setName("tbPemeriksaan"); // NOI18N
        tbPemeriksaan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbPemeriksaanMouseClicked(evt);
            }
        });
        tbPemeriksaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbPemeriksaanKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbPemeriksaanKeyReleased(evt);
            }
        });
        Scroll2.setViewportView(tbPemeriksaan);

        jPanel3.add(Scroll2, java.awt.BorderLayout.CENTER);

        internalFrame9.add(jPanel3, java.awt.BorderLayout.WEST);

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setOpaque(false);
        jPanel4.setPreferredSize(new java.awt.Dimension(295, 102));
        jPanel4.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi6.setBorder(null);
        panelisi6.setName("panelisi6"); // NOI18N
        panelisi6.setPreferredSize(new java.awt.Dimension(100, 43));
        panelisi6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label11.setForeground(new java.awt.Color(170, 0, 0));
        label11.setText("Key Word :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi6.add(label11);

        TCariSkala1.setForeground(new java.awt.Color(170, 0, 0));
        TCariSkala1.setToolTipText("Alt+C");
        TCariSkala1.setName("TCariSkala1"); // NOI18N
        TCariSkala1.setPreferredSize(new java.awt.Dimension(200, 23));
        TCariSkala1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariSkala1KeyPressed(evt);
            }
        });
        panelisi6.add(TCariSkala1);

        BtnCariSkala1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariSkala1.setMnemonic('1');
        BtnCariSkala1.setToolTipText("Alt+1");
        BtnCariSkala1.setName("BtnCariSkala1"); // NOI18N
        BtnCariSkala1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariSkala1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariSkala1ActionPerformed(evt);
            }
        });
        BtnCariSkala1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariSkala1KeyPressed(evt);
            }
        });
        panelisi6.add(BtnCariSkala1);

        BtnTambahSkala1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambahSkala1.setMnemonic('3');
        BtnTambahSkala1.setToolTipText("Alt+3");
        BtnTambahSkala1.setName("BtnTambahSkala1"); // NOI18N
        BtnTambahSkala1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambahSkala1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahSkala1ActionPerformed(evt);
            }
        });
        panelisi6.add(BtnTambahSkala1);

        jPanel4.add(panelisi6, java.awt.BorderLayout.PAGE_END);

        TabSkala1dan2.setBackground(new java.awt.Color(255, 255, 254));
        TabSkala1dan2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TabSkala1dan2.setForeground(new java.awt.Color(50, 50, 50));
        TabSkala1dan2.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        TabSkala1dan2.setName("TabSkala1dan2"); // NOI18N
        TabSkala1dan2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabSkala1dan2MouseClicked(evt);
            }
        });

        Scroll3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll3.setForeground(new java.awt.Color(170, 0, 0));
        Scroll3.setName("Scroll3"); // NOI18N
        Scroll3.setOpaque(true);

        tbSkala1.setForeground(new java.awt.Color(170, 0, 0));
        tbSkala1.setName("tbSkala1"); // NOI18N
        tbSkala1.setSelectionForeground(new java.awt.Color(0, 255, 0));
        Scroll3.setViewportView(tbSkala1);

        TabSkala1dan2.addTab("Skala 1", Scroll3);

        Scroll4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll4.setName("Scroll4"); // NOI18N
        Scroll4.setOpaque(true);

        tbSkala2.setForeground(new java.awt.Color(255, 0, 0));
        tbSkala2.setName("tbSkala2"); // NOI18N
        tbSkala2.setSelectionForeground(new java.awt.Color(0, 255, 0));
        Scroll4.setViewportView(tbSkala2);

        TabSkala1dan2.addTab("Skala 2", Scroll4);

        jPanel4.add(TabSkala1dan2, java.awt.BorderLayout.CENTER);

        internalFrame9.add(jPanel4, java.awt.BorderLayout.CENTER);

        internalFrame3.add(internalFrame9, java.awt.BorderLayout.WEST);

        internalFrame5.add(internalFrame3, java.awt.BorderLayout.CENTER);

        TabTriase.addTab("Triase Primer", internalFrame5);

        internalFrame10.setBorder(null);
        internalFrame10.setName("internalFrame10"); // NOI18N
        internalFrame10.setLayout(new java.awt.BorderLayout());

        internalFrame11.setBorder(null);
        internalFrame11.setName("internalFrame11"); // NOI18N
        internalFrame11.setPreferredSize(new java.awt.Dimension(0, 104));
        internalFrame11.setLayout(null);

        scrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane2.setName("scrollPane2"); // NOI18N

        SekunderAnamnesa.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        SekunderAnamnesa.setColumns(20);
        SekunderAnamnesa.setRows(5);
        SekunderAnamnesa.setName("SekunderAnamnesa"); // NOI18N
        SekunderAnamnesa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SekunderAnamnesaKeyPressed(evt);
            }
        });
        scrollPane2.setViewportView(SekunderAnamnesa);

        internalFrame11.add(scrollPane2);
        scrollPane2.setBounds(114, 10, 440, 53);

        jLabel13.setText("Anamnesa Singkat :");
        jLabel13.setName("jLabel13"); // NOI18N
        internalFrame11.add(jLabel13);
        jLabel13.setBounds(0, 10, 110, 23);

        jLabel14.setText("Suhu (C) :");
        jLabel14.setName("jLabel14"); // NOI18N
        internalFrame11.add(jLabel14);
        jLabel14.setBounds(547, 10, 79, 23);

        SekunderSuhu.setFocusTraversalPolicyProvider(true);
        SekunderSuhu.setName("SekunderSuhu"); // NOI18N
        SekunderSuhu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SekunderSuhuKeyPressed(evt);
            }
        });
        internalFrame11.add(SekunderSuhu);
        SekunderSuhu.setBounds(630, 10, 55, 23);

        jLabel17.setText("Saturasi O²(%) :");
        jLabel17.setName("jLabel17"); // NOI18N
        internalFrame11.add(jLabel17);
        jLabel17.setBounds(0, 70, 110, 23);

        SekunderSaturasi.setFocusTraversalPolicyProvider(true);
        SekunderSaturasi.setName("SekunderSaturasi"); // NOI18N
        SekunderSaturasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SekunderSaturasiKeyPressed(evt);
            }
        });
        internalFrame11.add(SekunderSaturasi);
        SekunderSaturasi.setBounds(114, 70, 55, 23);

        jLabel31.setText("Nyeri :");
        jLabel31.setName("jLabel31"); // NOI18N
        internalFrame11.add(jLabel31);
        jLabel31.setBounds(547, 40, 79, 23);

        SekunderNyeri.setHighlighter(null);
        SekunderNyeri.setName("SekunderNyeri"); // NOI18N
        SekunderNyeri.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SekunderNyeriKeyPressed(evt);
            }
        });
        internalFrame11.add(SekunderNyeri);
        SekunderNyeri.setBounds(630, 40, 55, 23);

        SekunderNadi.setFocusTraversalPolicyProvider(true);
        SekunderNadi.setName("SekunderNadi"); // NOI18N
        SekunderNadi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SekunderNadiActionPerformed(evt);
            }
        });
        SekunderNadi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SekunderNadiKeyPressed(evt);
            }
        });
        internalFrame11.add(SekunderNadi);
        SekunderNadi.setBounds(630, 70, 55, 23);

        jLabel32.setText("Nadi(/menit) :");
        jLabel32.setName("jLabel32"); // NOI18N
        internalFrame11.add(jLabel32);
        jLabel32.setBounds(547, 70, 79, 23);

        SekunderRespirasi.setHighlighter(null);
        SekunderRespirasi.setName("SekunderRespirasi"); // NOI18N
        SekunderRespirasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SekunderRespirasiKeyPressed(evt);
            }
        });
        internalFrame11.add(SekunderRespirasi);
        SekunderRespirasi.setBounds(309, 70, 55, 23);

        jLabel33.setText("Respirasi(/menit) :");
        jLabel33.setName("jLabel33"); // NOI18N
        internalFrame11.add(jLabel33);
        jLabel33.setBounds(205, 70, 100, 23);

        SekunderTensi.setHighlighter(null);
        SekunderTensi.setName("SekunderTensi"); // NOI18N
        SekunderTensi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SekunderTensiKeyPressed(evt);
            }
        });
        internalFrame11.add(SekunderTensi);
        SekunderTensi.setBounds(458, 70, 55, 23);

        jLabel15.setText("Tensi :");
        jLabel15.setName("jLabel15"); // NOI18N
        internalFrame11.add(jLabel15);
        jLabel15.setBounds(395, 70, 59, 23);

        internalFrame10.add(internalFrame11, java.awt.BorderLayout.PAGE_START);

        internalFrame12.setBorder(null);
        internalFrame12.setName("internalFrame12"); // NOI18N
        internalFrame12.setPreferredSize(new java.awt.Dimension(0, 74));
        internalFrame12.setLayout(null);

        jLabel30.setText("Dokter/Petugas IGD :");
        jLabel30.setName("jLabel30"); // NOI18N
        internalFrame12.add(jLabel30);
        jLabel30.setBounds(202, 40, 130, 23);

        SekunderKodePetugas.setEditable(false);
        SekunderKodePetugas.setHighlighter(null);
        SekunderKodePetugas.setName("SekunderKodePetugas"); // NOI18N
        SekunderKodePetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SekunderKodePetugasKeyPressed(evt);
            }
        });
        internalFrame12.add(SekunderKodePetugas);
        SekunderKodePetugas.setBounds(336, 40, 110, 23);

        SekunderNamaPetugas.setEditable(false);
        SekunderNamaPetugas.setName("SekunderNamaPetugas"); // NOI18N
        SekunderNamaPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SekunderNamaPetugasKeyPressed(evt);
            }
        });
        internalFrame12.add(SekunderNamaPetugas);
        SekunderNamaPetugas.setBounds(448, 40, 210, 23);

        btnSekunderPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnSekunderPetugas.setMnemonic('1');
        btnSekunderPetugas.setToolTipText("Alt+1");
        btnSekunderPetugas.setName("btnSekunderPetugas"); // NOI18N
        btnSekunderPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSekunderPetugasActionPerformed(evt);
            }
        });
        btnSekunderPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnSekunderPetugasKeyPressed(evt);
            }
        });
        internalFrame12.add(btnSekunderPetugas);
        btnSekunderPetugas.setBounds(660, 40, 28, 23);

        jLabel37.setText("Plan/Keputusan :");
        jLabel37.setName("jLabel37"); // NOI18N
        internalFrame12.add(jLabel37);
        jLabel37.setBounds(362, 10, 90, 23);

        SekunderTanggalTriase.setForeground(new java.awt.Color(50, 70, 50));
        SekunderTanggalTriase.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "01-08-2023 11:11:46" }));
        SekunderTanggalTriase.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        SekunderTanggalTriase.setName("SekunderTanggalTriase"); // NOI18N
        SekunderTanggalTriase.setOpaque(false);
        SekunderTanggalTriase.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SekunderTanggalTriaseKeyPressed(evt);
            }
        });
        internalFrame12.add(SekunderTanggalTriase);
        SekunderTanggalTriase.setBounds(69, 40, 135, 23);

        jLabel38.setText("Tgl.Triase :");
        jLabel38.setName("jLabel38"); // NOI18N
        internalFrame12.add(jLabel38);
        jLabel38.setBounds(0, 40, 65, 23);

        buttonGroup1.add(SekunderZonaKuning);
        SekunderZonaKuning.setForeground(new java.awt.Color(200, 200, 0));
        SekunderZonaKuning.setText("Zona Kuning");
        SekunderZonaKuning.setName("SekunderZonaKuning"); // NOI18N
        SekunderZonaKuning.setPreferredSize(new java.awt.Dimension(40, 20));
        internalFrame12.add(SekunderZonaKuning);
        SekunderZonaKuning.setBounds(456, 10, 120, 23);

        buttonGroup1.add(SekunderZonaHijau);
        SekunderZonaHijau.setForeground(new java.awt.Color(0, 170, 0));
        SekunderZonaHijau.setText("Zona Hijau");
        SekunderZonaHijau.setName("SekunderZonaHijau"); // NOI18N
        internalFrame12.add(SekunderZonaHijau);
        SekunderZonaHijau.setBounds(590, 10, 100, 23);

        label15.setText("Catatan :");
        label15.setName("label15"); // NOI18N
        label15.setPreferredSize(new java.awt.Dimension(60, 23));
        internalFrame12.add(label15);
        label15.setBounds(0, 10, 65, 23);

        SekunderCatatan.setToolTipText("Alt+C");
        SekunderCatatan.setName("SekunderCatatan"); // NOI18N
        SekunderCatatan.setPreferredSize(new java.awt.Dimension(140, 23));
        SekunderCatatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SekunderCatatanKeyPressed(evt);
            }
        });
        internalFrame12.add(SekunderCatatan);
        SekunderCatatan.setBounds(69, 10, 290, 23);

        internalFrame10.add(internalFrame12, java.awt.BorderLayout.PAGE_END);

        internalFrame13.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4));
        internalFrame13.setName("internalFrame13"); // NOI18N
        internalFrame13.setLayout(new java.awt.BorderLayout(1, 1));

        internalFrame14.setBorder(null);
        internalFrame14.setName("internalFrame14"); // NOI18N
        internalFrame14.setPreferredSize(new java.awt.Dimension(691, 74));
        internalFrame14.setLayout(new java.awt.BorderLayout(2, 1));

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setOpaque(false);
        jPanel5.setPreferredSize(new java.awt.Dimension(280, 102));
        jPanel5.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi7.setBorder(null);
        panelisi7.setName("panelisi7"); // NOI18N
        panelisi7.setPreferredSize(new java.awt.Dimension(100, 43));
        panelisi7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label12.setText("Key Word :");
        label12.setName("label12"); // NOI18N
        label12.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi7.add(label12);

        TCariPemeriksaan2.setToolTipText("Alt+C");
        TCariPemeriksaan2.setName("TCariPemeriksaan2"); // NOI18N
        TCariPemeriksaan2.setPreferredSize(new java.awt.Dimension(140, 23));
        TCariPemeriksaan2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariPemeriksaan2KeyPressed(evt);
            }
        });
        panelisi7.add(TCariPemeriksaan2);

        BtnCariPemeriksaan1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariPemeriksaan1.setMnemonic('1');
        BtnCariPemeriksaan1.setToolTipText("Alt+1");
        BtnCariPemeriksaan1.setName("BtnCariPemeriksaan1"); // NOI18N
        BtnCariPemeriksaan1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariPemeriksaan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariPemeriksaan1ActionPerformed(evt);
            }
        });
        BtnCariPemeriksaan1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariPemeriksaan1KeyPressed(evt);
            }
        });
        panelisi7.add(BtnCariPemeriksaan1);

        BtnTambahPemeriksaan1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambahPemeriksaan1.setMnemonic('3');
        BtnTambahPemeriksaan1.setToolTipText("Alt+3");
        BtnTambahPemeriksaan1.setName("BtnTambahPemeriksaan1"); // NOI18N
        BtnTambahPemeriksaan1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambahPemeriksaan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahPemeriksaan1ActionPerformed(evt);
            }
        });
        panelisi7.add(BtnTambahPemeriksaan1);

        jPanel5.add(panelisi7, java.awt.BorderLayout.PAGE_END);

        Scroll5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll5.setName("Scroll5"); // NOI18N
        Scroll5.setOpaque(true);

        tbPemeriksaan2.setName("tbPemeriksaan2"); // NOI18N
        tbPemeriksaan2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbPemeriksaan2MouseClicked(evt);
            }
        });
        tbPemeriksaan2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbPemeriksaan2KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbPemeriksaan2KeyReleased(evt);
            }
        });
        Scroll5.setViewportView(tbPemeriksaan2);

        jPanel5.add(Scroll5, java.awt.BorderLayout.CENTER);

        internalFrame14.add(jPanel5, java.awt.BorderLayout.WEST);

        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setOpaque(false);
        jPanel6.setPreferredSize(new java.awt.Dimension(295, 102));
        jPanel6.setLayout(new java.awt.BorderLayout(2, 1));

        panelisi8.setBorder(null);
        panelisi8.setName("panelisi8"); // NOI18N
        panelisi8.setPreferredSize(new java.awt.Dimension(100, 43));
        panelisi8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label13.setForeground(new java.awt.Color(200, 200, 0));
        label13.setText("Key Word :");
        label13.setName("label13"); // NOI18N
        label13.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi8.add(label13);

        TCariSkala3.setForeground(new java.awt.Color(200, 200, 0));
        TCariSkala3.setToolTipText("Alt+C");
        TCariSkala3.setName("TCariSkala3"); // NOI18N
        TCariSkala3.setPreferredSize(new java.awt.Dimension(200, 23));
        TCariSkala3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariSkala3KeyPressed(evt);
            }
        });
        panelisi8.add(TCariSkala3);

        BtnCariSkala2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariSkala2.setMnemonic('1');
        BtnCariSkala2.setToolTipText("Alt+1");
        BtnCariSkala2.setName("BtnCariSkala2"); // NOI18N
        BtnCariSkala2.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariSkala2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariSkala2ActionPerformed(evt);
            }
        });
        BtnCariSkala2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariSkala2KeyPressed(evt);
            }
        });
        panelisi8.add(BtnCariSkala2);

        BtnTambahSkala2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambahSkala2.setMnemonic('3');
        BtnTambahSkala2.setToolTipText("Alt+3");
        BtnTambahSkala2.setName("BtnTambahSkala2"); // NOI18N
        BtnTambahSkala2.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambahSkala2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahSkala2ActionPerformed(evt);
            }
        });
        panelisi8.add(BtnTambahSkala2);

        jPanel6.add(panelisi8, java.awt.BorderLayout.PAGE_END);

        TabSkala3dan4dan5.setBackground(new java.awt.Color(255, 255, 254));
        TabSkala3dan4dan5.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TabSkala3dan4dan5.setForeground(new java.awt.Color(50, 50, 50));
        TabSkala3dan4dan5.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        TabSkala3dan4dan5.setName("TabSkala3dan4dan5"); // NOI18N
        TabSkala3dan4dan5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabSkala3dan4dan5MouseClicked(evt);
            }
        });

        Scroll6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll6.setName("Scroll6"); // NOI18N
        Scroll6.setOpaque(true);

        tbSkala3.setForeground(new java.awt.Color(200, 200, 0));
        tbSkala3.setName("tbSkala3"); // NOI18N
        tbSkala3.setSelectionForeground(new java.awt.Color(0, 255, 0));
        Scroll6.setViewportView(tbSkala3);

        TabSkala3dan4dan5.addTab("Skala 3", Scroll6);

        Scroll7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll7.setName("Scroll7"); // NOI18N
        Scroll7.setOpaque(true);

        tbSkala4.setForeground(new java.awt.Color(0, 170, 0));
        tbSkala4.setName("tbSkala4"); // NOI18N
        tbSkala4.setSelectionForeground(new java.awt.Color(0, 255, 0));
        Scroll7.setViewportView(tbSkala4);

        TabSkala3dan4dan5.addTab("Skala 4", Scroll7);

        Scroll8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll8.setName("Scroll8"); // NOI18N
        Scroll8.setOpaque(true);

        tbSkala5.setForeground(new java.awt.Color(150, 150, 150));
        tbSkala5.setName("tbSkala5"); // NOI18N
        tbSkala5.setSelectionForeground(new java.awt.Color(0, 255, 0));
        Scroll8.setViewportView(tbSkala5);

        TabSkala3dan4dan5.addTab("Skala 5", Scroll8);

        jPanel6.add(TabSkala3dan4dan5, java.awt.BorderLayout.CENTER);

        internalFrame14.add(jPanel6, java.awt.BorderLayout.CENTER);

        internalFrame13.add(internalFrame14, java.awt.BorderLayout.WEST);

        internalFrame10.add(internalFrame13, java.awt.BorderLayout.CENTER);

        TabTriase.addTab("Triase Sekunder", internalFrame10);

        FormTriase.add(TabTriase, java.awt.BorderLayout.CENTER);

        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 104));
        FormInput.setLayout(null);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 89, 23);

        TNoRw.setBackground(new java.awt.Color(245, 250, 240));
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        FormInput.add(TNoRw);
        TNoRw.setBounds(93, 10, 122, 23);

        TPasien.setBackground(new java.awt.Color(245, 250, 240));
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(289, 10, 225, 23);

        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TNoRMActionPerformed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(217, 10, 70, 23);

        jLabel18.setText("Tgl.Kunjungan :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(0, 40, 89, 23);

        TanggalKunjungan.setForeground(new java.awt.Color(50, 70, 50));
        TanggalKunjungan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "01-08-2023 11:11:47" }));
        TanggalKunjungan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TanggalKunjungan.setName("TanggalKunjungan"); // NOI18N
        TanggalKunjungan.setOpaque(false);
        TanggalKunjungan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKunjunganKeyPressed(evt);
            }
        });
        FormInput.add(TanggalKunjungan);
        TanggalKunjungan.setBounds(93, 40, 135, 23);

        jLabel20.setText("Cara Masuk :");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(236, 40, 80, 23);

        CaraMasuk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Jalan", "Brankar", "Kursi Roda", "Digendong" }));
        CaraMasuk.setName("CaraMasuk"); // NOI18N
        CaraMasuk.setPreferredSize(new java.awt.Dimension(55, 28));
        CaraMasuk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CaraMasukKeyPressed(evt);
            }
        });
        FormInput.add(CaraMasuk);
        CaraMasuk.setBounds(320, 40, 110, 23);

        jLabel22.setText("Transportasi :");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(531, 10, 80, 23);

        Transportasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "AGD", "Sendiri", "Swasta" }));
        Transportasi.setName("Transportasi"); // NOI18N
        Transportasi.setPreferredSize(new java.awt.Dimension(55, 28));
        Transportasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TransportasiKeyPressed(evt);
            }
        });
        FormInput.add(Transportasi);
        Transportasi.setBounds(615, 10, 100, 23);

        jLabel23.setText("Alasan Kedatangan :");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(431, 40, 130, 23);

        AlasanKedatangan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Datang Sendiri", "Polisi", "Rujukan", "Bidan", "Puskesmas", "Rumah Sakit", "Poliklinik", "Faskes Lain", "-" }));
        AlasanKedatangan.setName("AlasanKedatangan"); // NOI18N
        AlasanKedatangan.setPreferredSize(new java.awt.Dimension(55, 28));
        AlasanKedatangan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AlasanKedatanganKeyPressed(evt);
            }
        });
        FormInput.add(AlasanKedatangan);
        AlasanKedatangan.setBounds(565, 40, 150, 23);

        jLabel5.setText("Macam Kasus :");
        jLabel5.setName("jLabel5"); // NOI18N
        FormInput.add(jLabel5);
        jLabel5.setBounds(0, 70, 89, 23);

        KdKasus.setEditable(false);
        KdKasus.setHighlighter(null);
        KdKasus.setName("KdKasus"); // NOI18N
        KdKasus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdKasusKeyPressed(evt);
            }
        });
        FormInput.add(KdKasus);
        KdKasus.setBounds(93, 70, 50, 23);

        NmKasus.setEditable(false);
        NmKasus.setName("NmKasus"); // NOI18N
        NmKasus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NmKasusKeyPressed(evt);
            }
        });
        FormInput.add(NmKasus);
        NmKasus.setBounds(145, 70, 250, 23);

        btnKasus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnKasus.setMnemonic('1');
        btnKasus.setToolTipText("Alt+1");
        btnKasus.setName("btnKasus"); // NOI18N
        btnKasus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKasusActionPerformed(evt);
            }
        });
        btnKasus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnKasusKeyPressed(evt);
            }
        });
        FormInput.add(btnKasus);
        btnKasus.setBounds(397, 70, 28, 23);

        KeteranganKedatangan.setName("KeteranganKedatangan"); // NOI18N
        KeteranganKedatangan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganKedatanganKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganKedatangan);
        KeteranganKedatangan.setBounds(520, 70, 195, 23);

        jLabel8.setText("Keterangan  :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(430, 70, 86, 23);

        FormTriase.add(FormInput, java.awt.BorderLayout.PAGE_START);

        ScrollTriase.setViewportView(FormTriase);

        TabPilihan.addTab("Input Triase", ScrollTriase);

        internalFrame4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        internalFrame4.setName("internalFrame4"); // NOI18N
        internalFrame4.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbTriase.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbTriase.setName("tbTriase"); // NOI18N
        tbTriase.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbTriaseMouseClicked(evt);
            }
        });
        tbTriase.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbTriaseKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbTriaseKeyReleased(evt);
            }
        });
        Scroll.setViewportView(tbTriase);

        internalFrame4.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tgl.Triase :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "01-08-2023" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "01-08-2023" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(205, 23));
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
        jLabel7.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass9.add(LCount);

        internalFrame4.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

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

        ScrollHTML.setBorder(null);
        ScrollHTML.setName("ScrollHTML"); // NOI18N
        ScrollHTML.setOpaque(true);
        ScrollHTML.setPreferredSize(new java.awt.Dimension(470, 16));

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N
        ScrollHTML.setViewportView(LoadHTML);

        PanelAccor.add(ScrollHTML, java.awt.BorderLayout.CENTER);

        internalFrame4.add(PanelAccor, java.awt.BorderLayout.EAST);

        TabPilihan.addTab("Data Triase", internalFrame4);

        TabPilihan.setSelectedIndex(1);

        internalFrame1.add(TabPilihan, java.awt.BorderLayout.CENTER);

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

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Pasien");
        }else if(KdKasus.getText().trim().equals("")||NmKasus.getText().trim().equals("")){
            Valid.textKosong(btnKasus,"Macam Kasus");
        }else if(KeteranganKedatangan.equals("")){
            Valid.textKosong(btnKasus,"Keterangan");
        }else{
            Sequel.AutoComitFalse();
            sukses=true;
            if(TabTriase.getSelectedIndex()==0){
                jmlskala1=0;jmlskala2=0;
                for(i=0;i<tbSkala1.getRowCount();i++){ 
                    if(tbSkala1.getValueAt(i,0).toString().equals("true")){
                        jmlskala1++;
                    }
                }
                for(i=0;i<tbSkala2.getRowCount();i++){ 
                    if(tbSkala2.getValueAt(i,0).toString().equals("true")){
                        jmlskala2++;
                    }
                }
                if(PrimerKeluhanUtama.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(PrimerKeluhanUtama,"Keluhan Utama");
                }else if(PrimerSuhu.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(PrimerSuhu,"Suhu");
                }else if(PrimerNyeri.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(PrimerNyeri,"Nyeri");
                }else if(PrimerTensi.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(PrimerTensi,"Tensi");
                }else if(PrimerNadi.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(PrimerNadi,"Nadi");
                }else if(PrimerSaturasi.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(PrimerSaturasi,"Saturasi O²");
                }else if(PrimerRespirasi.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(PrimerRespirasi,"Respirasi");
                }else if(PrimerCatatan.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(PrimerCatatan,"Catatan");
                }else if(PrimerKodePetugas.getText().trim().equals("")||PrimerNamaPetugas.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(btnPrimerPetugas,"Dokter/Petugas Triase");
                }else if((jmlskala1==0)&&(jmlskala2==0)){
                    sukses=false;
                    Valid.textKosong(TCariPemeriksaan,"Skala 1 / Skala 2");
                }else{
                    if(Sequel.menyimpantf("data_triase_igd (no_rawat,tgl_kunjungan,cara_masuk,alat_transportasi,alasan_kedatangan,keterangan_kedatangan,kode_kasus,tekanan_darah,nadi,pernapasan,suhu,saturasi_o2,nyeri,terpasang_infus,terpasang_ngt,terpasang_kateter_urin,nama_pengantar,telp_pengantar)","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","No.Rawat",18,new String[]{
                            TNoRw.getText(),Valid.SetTgl(TanggalKunjungan.getSelectedItem()+"")+" "+TanggalKunjungan.getSelectedItem().toString().substring(11,19),
                            CaraMasuk.getSelectedItem().toString(),Transportasi.getSelectedItem().toString(),AlasanKedatangan.getSelectedItem().toString(),
                            KeteranganKedatangan.getText(),KdKasus.getText(),PrimerTensi.getText(),PrimerNadi.getText(),PrimerRespirasi.getText(),PrimerSuhu.getText(),
                            PrimerSaturasi.getText(),PrimerNyeri.getText(),
                            PacsInfus.isSelected()?"Ya":"Tidak",PacsNgt.isSelected()?"Ya":"Tidak",PacsKateter.isSelected()?"Ya":"Tidak",
                            PacsNamaPengantar.getText(),PacsTelpPengantar.getText()
                        })==true){
                        if(PrimerResusitasi.isSelected()==true){
                            keputusan="Ruang Resusitasi";
                        }else if(PrimerKritis.isSelected()==true){
                            keputusan="Ruang Kritis";
                        }
                        if(Sequel.menyimpantf2("data_triase_igdprimer","?,?,?,?,?,?,?", 7,new String[]{
                            TNoRw.getText(),PrimerKeluhanUtama.getText(),PrimerKubutuhanKusus.getSelectedItem().toString(),PrimerCatatan.getText(),keputusan,
                            Valid.SetTgl(PrimerTanggalTriase.getSelectedItem()+"")+" "+PrimerTanggalTriase.getSelectedItem().toString().substring(11,19), 
                            PrimerKodePetugas.getText()
                            })==true){
                            if(TabSkala1dan2.getSelectedIndex()==0){
                                for(i=0;i<tbSkala1.getRowCount();i++){ 
                                    if(tbSkala1.getValueAt(i,0).toString().equals("true")){
                                        if(Sequel.menyimpantf2("data_triase_igddetail_skala1","?,?","Skala 1",2,new String[]{
                                                TNoRw.getText(),tbSkala1.getValueAt(i,1).toString()
                                            })==true){
                                                tbSkala1.setValueAt(false,i,0);
                                        }else{
                                            sukses=false;
                                        }
                                    }
                                }
                            }else if(TabSkala1dan2.getSelectedIndex()==1){
                                for(i=0;i<tbSkala2.getRowCount();i++){ 
                                    if(tbSkala2.getValueAt(i,0).toString().equals("true")){
                                        if(Sequel.menyimpantf2("data_triase_igddetail_skala2","?,?","Skala 2",2,new String[]{
                                                TNoRw.getText(),tbSkala2.getValueAt(i,1).toString()
                                            })==true){
                                                tbSkala2.setValueAt(false,i,0);
                                        }else{
                                            sukses=false;
                                        }
                                    }
                                }
                            }
                        }else{
                            sukses=false;
                        }
                    }else{
                        sukses=false;
                    }
                }
            }else if(TabTriase.getSelectedIndex()==1){
                jmlskala3=0;jmlskala4=0;jmlskala5=0;
                for(i=0;i<tbSkala3.getRowCount();i++){ 
                    if(tbSkala3.getValueAt(i,0).toString().equals("true")){
                        jmlskala3++;
                    }
                }
                for(i=0;i<tbSkala4.getRowCount();i++){ 
                    if(tbSkala4.getValueAt(i,0).toString().equals("true")){
                        jmlskala4++;
                    }
                }
                for(i=0;i<tbSkala5.getRowCount();i++){ 
                    if(tbSkala5.getValueAt(i,0).toString().equals("true")){
                        jmlskala5++;
                    }
                }
                if(SekunderAnamnesa.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(SekunderAnamnesa,"Anamnesa");
                }else if(SekunderSuhu.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(SekunderSuhu,"Suhu");
                }else if(SekunderNyeri.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(SekunderNyeri,"Nyeri");
                }else if(SekunderTensi.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(SekunderTensi,"Tensi");
                }else if(SekunderNadi.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(SekunderNadi,"Nadi");
                }else if(SekunderSaturasi.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(SekunderSaturasi,"Saturasi O²");
                }else if(SekunderRespirasi.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(SekunderRespirasi,"Respirasi");
                }else if(SekunderCatatan.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(SekunderCatatan,"Catatan");
                }else if(SekunderKodePetugas.getText().trim().equals("")||SekunderNamaPetugas.getText().trim().equals("")){
                    sukses=false;
                    Valid.textKosong(btnSekunderPetugas,"Dokter Sekunder");
                }else if((jmlskala3==0)&&(jmlskala4==0)&&(jmlskala5==0)){
                    sukses=false;
                    Valid.textKosong(TCariPemeriksaan2,"Skala 3 / Skala 4 / Skala 5");
                }else{
                    if(Sequel.menyimpantf("data_triase_igd (no_rawat,tgl_kunjungan,cara_masuk,alat_transportasi,alasan_kedatangan,keterangan_kedatangan,kode_kasus,tekanan_darah,nadi,pernapasan,suhu,saturasi_o2,nyeri,terpasang_infus,terpasang_ngt,terpasang_kateter_urin,nama_pengantar,telp_pengantar)","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","No.Rawat",18,new String[]{
                            TNoRw.getText(),Valid.SetTgl(TanggalKunjungan.getSelectedItem()+"")+" "+TanggalKunjungan.getSelectedItem().toString().substring(11,19),
                            CaraMasuk.getSelectedItem().toString(),Transportasi.getSelectedItem().toString(),AlasanKedatangan.getSelectedItem().toString(),
                            KeteranganKedatangan.getText(),KdKasus.getText(),SekunderTensi.getText(),SekunderNadi.getText(),SekunderRespirasi.getText(),SekunderSuhu.getText(),
                            SekunderSaturasi.getText(),SekunderNyeri.getText(),
                            PacsInfus.isSelected()?"Ya":"Tidak",PacsNgt.isSelected()?"Ya":"Tidak",PacsKateter.isSelected()?"Ya":"Tidak",
                            PacsNamaPengantar.getText(),PacsTelpPengantar.getText()
                        })==true){
                        if(SekunderZonaKuning.isSelected()==true){
                            keputusan="Zona Kuning";
                        }else if(SekunderZonaHijau.isSelected()==true){
                            keputusan="Zona Hijau";
                        }
                        if(Sequel.menyimpantf2("data_triase_igdsekunder","?,?,?,?,?,?", 6,new String[]{
                            TNoRw.getText(),SekunderAnamnesa.getText(),SekunderCatatan.getText(),keputusan,
                            Valid.SetTgl(SekunderTanggalTriase.getSelectedItem()+"")+" "+SekunderTanggalTriase.getSelectedItem().toString().substring(11,19), 
                            SekunderKodePetugas.getText()
                            })==true){
                            if(TabSkala3dan4dan5.getSelectedIndex()==0){
                                for(i=0;i<tbSkala3.getRowCount();i++){ 
                                    if(tbSkala3.getValueAt(i,0).toString().equals("true")){
                                        if(Sequel.menyimpantf2("data_triase_igddetail_skala3","?,?","Skala 3",2,new String[]{
                                                TNoRw.getText(),tbSkala3.getValueAt(i,1).toString()
                                            })==true){
                                                tbSkala3.setValueAt(false,i,0);
                                        }else{
                                            sukses=false;
                                        }
                                    }
                                }
                            }else if(TabSkala3dan4dan5.getSelectedIndex()==1){
                                for(i=0;i<tbSkala4.getRowCount();i++){ 
                                    if(tbSkala4.getValueAt(i,0).toString().equals("true")){
                                        if(Sequel.menyimpantf2("data_triase_igddetail_skala4","?,?","Skala 4",2,new String[]{
                                                TNoRw.getText(),tbSkala4.getValueAt(i,1).toString()
                                            })==true){
                                                tbSkala4.setValueAt(false,i,0);
                                        }else{
                                            sukses=false;
                                        }
                                    }
                                }
                            }else if(TabSkala3dan4dan5.getSelectedIndex()==2){
                                for(i=0;i<tbSkala5.getRowCount();i++){ 
                                    if(tbSkala5.getValueAt(i,0).toString().equals("true")){
                                        if(Sequel.menyimpantf2("data_triase_igddetail_skala5","?,?","Skala 5",2,new String[]{
                                                TNoRw.getText(),tbSkala5.getValueAt(i,1).toString()
                                            })==true){
                                                tbSkala5.setValueAt(false,i,0);
                                        }else{
                                            sukses=false;
                                        }
                                    }
                                }
                            }
                        }else{
                            sukses=false;
                        }
                    }else{
                        sukses=false;
                    }
                }
            }else if(TabTriase.getSelectedIndex()==2){
                // [PACS] Stage d: INSERT header (data_triase_igd) + data_triase_pacs.
                // Cabang baru, terisolasi - tidak menyentuh alur Primer/Sekunder di atas.
                if(validasiPacs()){
                    if(Sequel.menyimpantf("data_triase_igd (no_rawat,tgl_kunjungan,cara_masuk,alat_transportasi,alasan_kedatangan,keterangan_kedatangan,kode_kasus,tekanan_darah,nadi,pernapasan,suhu,saturasi_o2,nyeri,kesadaran,berat_badan,tinggi_badan,kontak_awal_tanggal,kontak_awal_pukul,terpasang_infus,terpasang_ngt,terpasang_kateter_urin,nama_pengantar,telp_pengantar)","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","No.Rawat",23,new String[]{
                            TNoRw.getText(),Valid.SetTgl(TanggalKunjungan.getSelectedItem()+"")+" "+TanggalKunjungan.getSelectedItem().toString().substring(11,19),
                            CaraMasuk.getSelectedItem().toString(),Transportasi.getSelectedItem().toString(),AlasanKedatangan.getSelectedItem().toString(),
                            KeteranganKedatangan.getText(),KdKasus.getText(),PacsTensi.getText(),PacsNadi.getText(),PacsRespirasi.getText(),PacsSuhu.getText(),
                            PacsSaturasi.getText(),PacsNyeri.getText(),PacsKesadaran.getSelectedItem().toString(),PacsBeratBadan.getText(),PacsTinggiBadan.getText(),
                            Valid.SetTgl(TanggalKunjungan.getSelectedItem()+""),TanggalKunjungan.getSelectedItem().toString().substring(11,19),
                            PacsInfus.isSelected()?"Ya":"Tidak",PacsNgt.isSelected()?"Ya":"Tidak",PacsKateter.isSelected()?"Ya":"Tidak",
                            PacsNamaPengantar.getText(),PacsTelpPengantar.getText()
                        })==true){
                        if(Sequel.menyimpantf2("data_triase_pacs (no_rawat,skor_kesadaran,skor_tekanan_darah_sistolik,skor_nadi,skor_respirasi,skor_temperatur,skor_saturasi_o2,total_skor,level_pacs,keputusan_pacs,keputusan_pukul,tanggal_triase,nik,keluhan_utama,catatan_khusus)","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?",15,new String[]{
                                TNoRw.getText(),
                                CmbSkorKesadaranPacs.getSelectedItem().toString(),
                                CmbSkorTDPacs.getSelectedItem().toString(),
                                CmbSkorNadiPacs.getSelectedItem().toString(),
                                CmbSkorRespirasiPacs.getSelectedItem().toString(),
                                CmbSkorTemperaturPacs.getSelectedItem().toString(),
                                CmbSkorSaturasiPacs.getSelectedItem().toString(),
                                LblTotalSkorPacs.getText(),
                                levelPacsTerpilih(),
                                keputusanPacsTerpilih(),
                                DTPKeputusanPukulPacs.getTeksJam(),
                                Valid.SetTgl(TanggalKunjungan.getSelectedItem()+"")+" "+pukulAman(TanggalKunjungan),
                                SekunderKodePetugas.getText(),
                                PacsKeluhanUtama.getText(),
                                PacsCatatanKhusus.getText()
                            })==true){
                            // [Mekanisme Trauma] Hanya insert kalau Kasus = Trauma (panel sedang tampil).
                            // Non-trauma: dilewati, tidak ada baris tersimpan -> hasil cetak otomatis kosong.
                            if(panelMekanismeTrauma.isVisible()){
                                if(Sequel.menyimpantf2("data_triase_mekanisme_trauma (no_rawat,kkl_tunggal,kkl_dengan_lawan,kkl_lawan,kkl_tempat_kejadian,kkl_tanggal_kejadian,kkl_pukul_kejadian,jatuh_ketinggian,jatuh_ketinggian_keterangan,luka_bakar,luka_bakar_keterangan,trauma_fisik,trauma_fisik_keterangan,trauma_zat_kimia,trauma_zat_kimia_keterangan,trauma_zat_lainnya,trauma_zat_lainnya_keterangan)","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?",17,new String[]{
                                        TNoRw.getText(),
                                        CekKklTunggal.isSelected()?"Ya":"Tidak",
                                        CekKklDenganLawan.isSelected()?"Ya":"Tidak",
                                        TKklLawan.getText(),
                                        TKklTempatKejadian.getText(),
                                        panelKklKejadian.getTanggalSql(Valid),
                                        panelKklKejadian.getJamSql(),
                                        CekJatuhKetinggian.isSelected()?"Ya":"Tidak",
                                        TJatuhKetinggianKet.getText(),
                                        CekLukaBakar.isSelected()?"Ya":"Tidak",
                                        TLukaBakarKet.getText(),
                                        CekTraumaFisik.isSelected()?"Ya":"Tidak",
                                        TTraumaFisikKet.getText(),
                                        CekTraumaZatKimia.isSelected()?"Ya":"Tidak",
                                        TTraumaZatKimiaKet.getText(),
                                        CekTraumaZatLainnya.isSelected()?"Ya":"Tidak",
                                        TTraumaZatLainnyaKet.getText()
                                    })==false){
                                    sukses=false;
                                }
                            }
                        }else{
                            sukses=false;
                        }
                    }else{
                        sukses=false;
                    }
                }else{
                    sukses=false;
                }
            }
            if(sukses==true){
                Sequel.Commit();
            }else{
                sukses=false;
                Sequel.RollBack();
            }
            Sequel.AutoComitTrue();
            if(sukses==true){
                emptTeks();
                runBackground(() ->tampil());
            }
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(TabTriase.getSelectedIndex()==0){
            Valid.pindah(evt,btnPrimerPetugas,BtnBatal);
        }else if(TabTriase.getSelectedIndex()==1){
            Valid.pindah(evt,btnSekunderPetugas,BtnBatal);
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
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else{
            if(tbTriase.getSelectedRow()!= -1){
                if(akses.getkode().equals("Admin Utama")){
                    hapus();
                }else{
                    if(akses.getkode().equals(kodepetugas)){
                        hapus();
                    }else{
                        JOptionPane.showMessageDialog(null,"Hanya bisa dihapus oleh petugas yang bersangkutan..!!");
                    }
                }
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data yang mau dihapus terlebih dahulu ...!!!!");
            }
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
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Pasien");
        }else if(KdKasus.getText().trim().equals("")||NmKasus.getText().trim().equals("")){
            Valid.textKosong(btnKasus,"Macam Kasus");
        }else if(KeteranganKedatangan.equals("")){
            Valid.textKosong(btnKasus,"Keterangan");
        }else{
            if(tbTriase.getSelectedRow()> -1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else{
                    if(akses.getkode().equals(kodepetugas)){
                        ganti();
                    }else{
                        JOptionPane.showMessageDialog(null,"Hanya bisa diganti oleh petugas yang bersangkutan..!!");
                    }
                }
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
            TCari.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            keputusan = (String)JOptionPane.showInputDialog(null,"Silahkan pilih laporan..!","Laporan Triase IGD",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Laporan 1","Laporan 2"},"Laporan 1");
            if(keputusan==null){
                this.setCursor(Cursor.getDefaultCursor());
                return;
            }
            switch (keputusan) {
                case "Laporan 1":
                    Map<String, Object> param = new HashMap<>(); 
                    param.put("namars",akses.getnamars());
                    param.put("alamatrs",akses.getalamatrs());
                    param.put("kotars",akses.getkabupatenrs());
                    param.put("propinsirs",akses.getpropinsirs());
                    param.put("kontakrs",akses.getkontakrs());
                    param.put("emailrs",akses.getemailrs());   
                    param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                    Valid.MyReportqry("rptDataTriaseIGD.jasper","report","::[ Data Triase IGD ]::",
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,data_triase_igd.tgl_kunjungan,"+
                        "data_triase_igd.cara_masuk,data_triase_igd.alat_transportasi,data_triase_igd.alasan_kedatangan,"+
                        "data_triase_igd.keterangan_kedatangan,data_triase_igd.kode_kasus,master_triase_macam_kasus.macam_kasus "+
                        "from reg_periksa inner join pasien inner join data_triase_igd inner join master_triase_macam_kasus "+
                        "on reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.no_rawat=data_triase_igd.no_rawat "+
                        "and data_triase_igd.kode_kasus=master_triase_macam_kasus.kode_kasus "+
                        "where data_triase_igd.tgl_kunjungan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59' and reg_periksa.no_rawat like '%"+TCari.getText().trim()+"%' or "+
                        "data_triase_igd.tgl_kunjungan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59' and pasien.no_rkm_medis like '%"+TCari.getText().trim()+"%' or "+
                        "data_triase_igd.tgl_kunjungan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59' and pasien.nm_pasien like '%"+TCari.getText().trim()+"%' or "+
                        "data_triase_igd.tgl_kunjungan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59' and data_triase_igd.cara_masuk like '%"+TCari.getText().trim()+"%' or "+
                        "data_triase_igd.tgl_kunjungan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59' and data_triase_igd.alat_transportasi like '%"+TCari.getText().trim()+"%' or "+
                        "data_triase_igd.tgl_kunjungan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59' and data_triase_igd.alasan_kedatangan like '%"+TCari.getText().trim()+"%' or "+
                        "data_triase_igd.tgl_kunjungan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59' and data_triase_igd.keterangan_kedatangan like '%"+TCari.getText().trim()+"%' or "+
                        "data_triase_igd.tgl_kunjungan between '"+Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00' and '"+Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59' and master_triase_macam_kasus.macam_kasus like '%"+TCari.getText().trim()+"%' order by data_triase_igd.tgl_kunjungan",param);
                    break;
                case "Laporan 2":
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    try {
                        htmlContent = new StringBuilder();
                        htmlContent.append(                             
                            "<tr class='isi'>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='12%'><b>Pasien</b></td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='12%'><b>Kunjungan</b></td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='18%'><b>Kedatangan</b></td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='13%'><b>Keluhan/Anamnesis Singkat</b></td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='10%'><b>Tanda Vital</b></td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='21%'><b>Pemeriksaan</b></td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='14%'><b>Keputusan</b></td>"+
                            "</tr>"
                        );
                        
                        for(i=0;i<tabMode.getRowCount();i++){  
                            htmlContent.append(                             
                                "<tr class='isi'>"+
                                    "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                        "<table width='100%' border='0' cellpadding='0' cellspacing='0'align='center'>"+
                                            "<tr class='isi2'>"+
                                                "<td width='34%' valign='top'>No.Rawat</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+tbTriase.getValueAt(i,0).toString()+"</td>"+
                                            "</tr>"+
                                            "<tr class='isi2'>"+
                                                "<td width='34%' valign='top'>No.R.M.</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+tbTriase.getValueAt(i,1).toString()+"</td>"+
                                            "</tr>"+
                                            "<tr class='isi2'>"+
                                                "<td width='34%' valign='top'>Nama Pasien</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+tbTriase.getValueAt(i,2).toString()+"</td>"+
                                            "</tr>"+
                                        "</table>"+
                                    "</td>"+
                                    "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                        "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"+
                                            "<tr class='isi2'>"+
                                                "<td width='34%' valign='top'>Tgl.Kunjungan</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+tbTriase.getValueAt(i,3).toString()+"</td>"+
                                            "</tr>"+
                                            "<tr class='isi2'>"+
                                                "<td width='34%' valign='top'>Cara Masuk</td><td valign='top'>:</td><td width='65%' valign='top'>"+tbTriase.getValueAt(i,4).toString()+"</td>"+
                                            "</tr>"+
                                            "<tr class='isi2'>"+
                                                "<td width='34%' valign='top'>Transportasi</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+tbTriase.getValueAt(i,5).toString()+"</td>"+
                                            "</tr>"+
                                        "</table>"+
                                    "</td>"+
                                    "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                        "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"+
                                            "<tr class='isi2'>"+
                                                "<td width='31%' valign='top'>Alasan Kedatangan</td><td valign='top'>:&nbsp;</td><td width='68%' valign='top'>"+tbTriase.getValueAt(i,6).toString()+"</td>"+
                                            "</tr>"+
                                            "<tr class='isi2'>"+
                                                "<td width='31%' valign='top'>Keterangan</td><td valign='top'>:&nbsp;</td><td width='68%' valign='top'>"+tbTriase.getValueAt(i,7).toString()+"</td>"+
                                            "</tr>"+
                                            "<tr class='isi2'>"+
                                                "<td width='31%' valign='top'>Macam Kasus</td><td valign='top'>:&nbsp;</td><td width='68%' valign='top'>"+tbTriase.getValueAt(i,9).toString()+"</td>"+
                                            "</tr>"+
                                        "</table>"+
                                    "</td>"
                            );
                            
                            ps=koneksi.prepareStatement(
                                "select data_triase_igdprimer.keluhan_utama,data_triase_igdprimer.kebutuhan_khusus,data_triase_igdprimer.catatan,"+
                                "data_triase_igdprimer.plan,data_triase_igdprimer.tanggaltriase,data_triase_igdprimer.nik,data_triase_igd.tekanan_darah,"+
                                "data_triase_igd.nadi,data_triase_igd.pernapasan,data_triase_igd.suhu,data_triase_igd.saturasi_o2,data_triase_igd.nyeri,"+
                                "data_triase_igd.no_rawat from data_triase_igdprimer inner join data_triase_igd on data_triase_igd.no_rawat="+
                                "data_triase_igdprimer.no_rawat where data_triase_igd.no_rawat=?");
                            try {
                                ps.setString(1,tbTriase.getValueAt(i,0).toString());
                                rs=ps.executeQuery();
                                if(rs.next()){
                                    htmlContent.append(
                                        "<td valign='top'> Keluhan Utama : "+rs.getString("keluhan_utama")+"<br>Kebutuhan Khusus : "+rs.getString("kebutuhan_khusus")+"</td>"+
                                        "<td valign='top'> Suhu (C) : "+rs.getString("suhu")+", Respirasi(/menit) : "+rs.getString("pernapasan")+", Tensi : "+rs.getString("tekanan_darah")+", Nadi(/menit) : "+rs.getString("nadi")+", Saturasi O²(%) : "+rs.getString("saturasi_o2")+", Nyeri : "+rs.getString("nyeri")+"</td>"
                                    );
                                    ps2=koneksi.prepareStatement(
                                        "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                        "from master_triase_pemeriksaan inner join master_triase_skala1 inner join data_triase_igddetail_skala1 "+
                                        "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala1.kode_pemeriksaan and "+
                                        "master_triase_skala1.kode_skala1=data_triase_igddetail_skala1.kode_skala1 where data_triase_igddetail_skala1.no_rawat=? "+
                                        "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                                    try {
                                        ps2.setString(1,rs.getString("no_rawat"));
                                        rs2=ps2.executeQuery();
                                        if(rs2.next()){
                                            htmlContent.append(
                                                "<td valign='top'>"+
                                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"
                                            );
                                            rs2.beforeFirst();
                                            while(rs2.next()){
                                                htmlContent.append(
                                                    "<tr class='isi5'>"+
                                                        "<td width='40%' valign='top'>"+rs2.getString("nama_pemeriksaan")+"</td>"+
                                                        "<td valign='top'>:&nbsp;</td>"+
                                                        "<td width='59%' valign='top'>"+
                                                            "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"
                                                    );
                                                ps3=koneksi.prepareStatement(
                                                    "select master_triase_skala1.pengkajian_skala1 from master_triase_skala1 inner join data_triase_igddetail_skala1 "+
                                                    "on master_triase_skala1.kode_skala1=data_triase_igddetail_skala1.kode_skala1 where "+
                                                    "master_triase_skala1.kode_pemeriksaan=? and data_triase_igddetail_skala1.no_rawat=? "+
                                                    "order by data_triase_igddetail_skala1.kode_skala1");
                                                try {
                                                    ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                                    ps3.setString(2,rs.getString("no_rawat"));
                                                    rs3=ps3.executeQuery();
                                                    while(rs3.next()){
                                                        htmlContent.append(                             
                                                            "<tr class='isi5'>"+
                                                                "<td border='0' valign='middle' width='100%'>"+rs3.getString("pengkajian_skala1")+"</td>"+
                                                            "</tr>"
                                                        );
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("Notif : "+e);
                                                } finally{
                                                    if(rs3!=null){
                                                        rs3.close();
                                                    }
                                                    if(ps3!=null){
                                                        ps3.close();
                                                    }
                                                }
                                                htmlContent.append(
                                                            "</table>"+
                                                        "</td>"+
                                                    "</tr>"
                                                );
                                            }
                                            htmlContent.append(
                                                    "</table>"+
                                                "</td>"
                                            );
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
                                    
                                    ps2=koneksi.prepareStatement(
                                        "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                        "from master_triase_pemeriksaan inner join master_triase_skala2 inner join data_triase_igddetail_skala2 "+
                                        "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala2.kode_pemeriksaan and "+
                                        "master_triase_skala2.kode_skala2=data_triase_igddetail_skala2.kode_skala2 where data_triase_igddetail_skala2.no_rawat=? "+
                                        "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                                    try {
                                        ps2.setString(1,rs.getString("no_rawat"));
                                        rs2=ps2.executeQuery();
                                        if(rs2.next()){
                                            htmlContent.append(
                                                "<td valign='top'>"+
                                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"
                                            );
                                            rs2.beforeFirst();
                                            while(rs2.next()){
                                                htmlContent.append(
                                                    "<tr class='isi6'>"+
                                                        "<td width='40%' valign='top'>"+rs2.getString("nama_pemeriksaan")+"</td>"+
                                                        "<td valign='top'>:&nbsp;</td>"+
                                                        "<td width='59%' valign='top'>"+
                                                            "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"
                                                    );
                                                ps3=koneksi.prepareStatement(
                                                    "select master_triase_skala2.pengkajian_skala2 from master_triase_skala2 inner join data_triase_igddetail_skala2 "+
                                                    "on master_triase_skala2.kode_skala2=data_triase_igddetail_skala2.kode_skala2 where "+
                                                    "master_triase_skala2.kode_pemeriksaan=? and data_triase_igddetail_skala2.no_rawat=? "+
                                                    "order by data_triase_igddetail_skala2.kode_skala2");
                                                try {
                                                    ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                                    ps3.setString(2,rs.getString("no_rawat"));
                                                    rs3=ps3.executeQuery();
                                                    while(rs3.next()){
                                                        htmlContent.append(                             
                                                            "<tr class='isi6'>"+
                                                                "<td border='0' valign='middle' width='100%'>"+rs3.getString("pengkajian_skala2")+"</td>"+
                                                            "</tr>"
                                                        );
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("Notif : "+e);
                                                } finally{
                                                    if(rs3!=null){
                                                        rs3.close();
                                                    }
                                                    if(ps3!=null){
                                                        ps3.close();
                                                    }
                                                }
                                                htmlContent.append(
                                                            "</table>"+
                                                        "</td>"+
                                                    "</tr>"
                                                );
                                            }
                                            htmlContent.append(
                                                    "</table>"+
                                                "</td>"
                                            );
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
                                        "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                            "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"+
                                                "<tr class='isi2'>"+
                                                    "<td width='34%' valign='top'>Keputusan</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>Zona Merah "+rs.getString("plan")+"</td>"+
                                                "</tr>"+
                                                "<tr class='isi2'>"+
                                                    "<td width='34%' valign='top'>Tanggal & Jam</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("tanggaltriase")+"</td>"+
                                                "</tr>"+
                                                "<tr class='isi2'>"+
                                                    "<td width='34%' valign='top'>Catatan</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("catatan")+"</td>"+
                                                "</tr>"+
                                                "<tr class='isi2'>"+
                                                    "<td width='34%' valign='top'>Dokter/Petugas</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("nik")+" "+Sequel.cariIsi("select pegawai.nama from pegawai where pegawai.nik=?",rs.getString("nik"))+"</td>"+
                                                "</tr>"+
                                            "</table>"+
                                        "</td>"
                                    );
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
                            
                            ps=koneksi.prepareStatement(
                                "select data_triase_igdsekunder.anamnesa_singkat,data_triase_igdsekunder.catatan,"+
                                "data_triase_igdsekunder.plan,data_triase_igdsekunder.tanggaltriase,data_triase_igdsekunder.nik,data_triase_igd.tekanan_darah,"+
                                "data_triase_igd.nadi,data_triase_igd.pernapasan,data_triase_igd.suhu,data_triase_igd.saturasi_o2,data_triase_igd.nyeri,"+
                                "data_triase_igd.no_rawat from data_triase_igdsekunder inner join data_triase_igd on data_triase_igd.no_rawat="+
                                "data_triase_igdsekunder.no_rawat where data_triase_igd.no_rawat=?");
                            try {
                                ps.setString(1,tbTriase.getValueAt(i,0).toString());
                                rs=ps.executeQuery();
                                if(rs.next()){
                                    htmlContent.append(
                                        "<td valign='top'> Anamnesa Singkat : "+rs.getString("anamnesa_singkat")+"</td>"+
                                        "<td valign='top'> Suhu (C) : "+rs.getString("suhu")+", Respirasi(/menit) : "+rs.getString("pernapasan")+", Tensi : "+rs.getString("tekanan_darah")+", Nadi(/menit) : "+rs.getString("nadi")+", Saturasi O²(%) : "+rs.getString("saturasi_o2")+", Nyeri : "+rs.getString("nyeri")+"</td>"
                                    );
                                    ps2=koneksi.prepareStatement(
                                        "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                        "from master_triase_pemeriksaan inner join master_triase_skala3 inner join data_triase_igddetail_skala3 "+
                                        "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala3.kode_pemeriksaan and "+
                                        "master_triase_skala3.kode_skala3=data_triase_igddetail_skala3.kode_skala3 where data_triase_igddetail_skala3.no_rawat=? "+
                                        "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                                    try {
                                        ps2.setString(1,rs.getString("no_rawat"));
                                        rs2=ps2.executeQuery();
                                        if(rs2.next()){
                                            htmlContent.append(
                                                "<td valign='top'>"+
                                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"
                                            );
                                            rs2.beforeFirst();
                                            while(rs2.next()){
                                                htmlContent.append(
                                                    "<tr class='isi7'>"+
                                                        "<td width='40%' valign='top'>"+rs2.getString("nama_pemeriksaan")+"</td>"+
                                                        "<td valign='top'>:&nbsp;</td>"+
                                                        "<td width='59%' valign='top'>"+
                                                            "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"
                                                    );
                                                ps3=koneksi.prepareStatement(
                                                    "select master_triase_skala3.pengkajian_skala3 from master_triase_skala3 inner join data_triase_igddetail_skala3 "+
                                                    "on master_triase_skala3.kode_skala3=data_triase_igddetail_skala3.kode_skala3 where "+
                                                    "master_triase_skala3.kode_pemeriksaan=? and data_triase_igddetail_skala3.no_rawat=? "+
                                                    "order by data_triase_igddetail_skala3.kode_skala3");
                                                try {
                                                    ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                                    ps3.setString(2,rs.getString("no_rawat"));
                                                    rs3=ps3.executeQuery();
                                                    while(rs3.next()){
                                                        htmlContent.append(                             
                                                            "<tr class='isi7'>"+
                                                                "<td border='0' valign='middle' width='100%'>"+rs3.getString("pengkajian_skala3")+"</td>"+
                                                            "</tr>"
                                                        );
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("Notif : "+e);
                                                } finally{
                                                    if(rs3!=null){
                                                        rs3.close();
                                                    }
                                                    if(ps3!=null){
                                                        ps3.close();
                                                    }
                                                }
                                                htmlContent.append(
                                                            "</table>"+
                                                        "</td>"+
                                                    "</tr>"
                                                );
                                            }
                                            htmlContent.append(
                                                    "</table>"+
                                                "</td>"
                                            );
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
                                    
                                    ps2=koneksi.prepareStatement(
                                        "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                        "from master_triase_pemeriksaan inner join master_triase_skala4 inner join data_triase_igddetail_skala4 "+
                                        "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala4.kode_pemeriksaan and "+
                                        "master_triase_skala4.kode_skala4=data_triase_igddetail_skala4.kode_skala4 where data_triase_igddetail_skala4.no_rawat=? "+
                                        "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                                    try {
                                        ps2.setString(1,rs.getString("no_rawat"));
                                        rs2=ps2.executeQuery();
                                        if(rs2.next()){
                                            htmlContent.append(
                                                "<td valign='top'>"+
                                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"
                                            );
                                            rs2.beforeFirst();
                                            while(rs2.next()){
                                                htmlContent.append(
                                                    "<tr class='isi8'>"+
                                                        "<td width='40%' valign='top'>"+rs2.getString("nama_pemeriksaan")+"</td>"+
                                                        "<td valign='top'>:&nbsp;</td>"+
                                                        "<td width='59%' valign='top'>"+
                                                            "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"
                                                    );
                                                ps3=koneksi.prepareStatement(
                                                    "select master_triase_skala4.pengkajian_skala4 from master_triase_skala4 inner join data_triase_igddetail_skala4 "+
                                                    "on master_triase_skala4.kode_skala4=data_triase_igddetail_skala4.kode_skala4 where "+
                                                    "master_triase_skala4.kode_pemeriksaan=? and data_triase_igddetail_skala4.no_rawat=? "+
                                                    "order by data_triase_igddetail_skala4.kode_skala4");
                                                try {
                                                    ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                                    ps3.setString(2,rs.getString("no_rawat"));
                                                    rs3=ps3.executeQuery();
                                                    while(rs3.next()){
                                                        htmlContent.append(                             
                                                            "<tr class='isi8'>"+
                                                                "<td border='0' valign='middle' width='100%'>"+rs3.getString("pengkajian_skala4")+"</td>"+
                                                            "</tr>"
                                                        );
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("Notif : "+e);
                                                } finally{
                                                    if(rs3!=null){
                                                        rs3.close();
                                                    }
                                                    if(ps3!=null){
                                                        ps3.close();
                                                    }
                                                }
                                                htmlContent.append(
                                                            "</table>"+
                                                        "</td>"+
                                                    "</tr>"
                                                );
                                            }
                                            htmlContent.append(
                                                    "</table>"+
                                                "</td>"
                                            );
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
                                    
                                    ps2=koneksi.prepareStatement(
                                        "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                        "from master_triase_pemeriksaan inner join master_triase_skala5 inner join data_triase_igddetail_skala5 "+
                                        "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala5.kode_pemeriksaan and "+
                                        "master_triase_skala5.kode_skala5=data_triase_igddetail_skala5.kode_skala5 where data_triase_igddetail_skala5.no_rawat=? "+
                                        "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                                    try {
                                        ps2.setString(1,rs.getString("no_rawat"));
                                        rs2=ps2.executeQuery();
                                        if(rs2.next()){
                                            htmlContent.append(
                                                "<td valign='top'>"+
                                                    "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"
                                            );
                                            rs2.beforeFirst();
                                            while(rs2.next()){
                                                htmlContent.append(
                                                    "<tr class='isi9'>"+
                                                        "<td width='40%' valign='top'>"+rs2.getString("nama_pemeriksaan")+"</td>"+
                                                        "<td valign='top'>:&nbsp;</td>"+
                                                        "<td width='59%' valign='top'>"+
                                                            "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"
                                                    );
                                                ps3=koneksi.prepareStatement(
                                                    "select master_triase_skala5.pengkajian_skala5 from master_triase_skala5 inner join data_triase_igddetail_skala5 "+
                                                    "on master_triase_skala5.kode_skala5=data_triase_igddetail_skala5.kode_skala5 where "+
                                                    "master_triase_skala5.kode_pemeriksaan=? and data_triase_igddetail_skala5.no_rawat=? "+
                                                    "order by data_triase_igddetail_skala5.kode_skala5");
                                                try {
                                                    ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                                    ps3.setString(2,rs.getString("no_rawat"));
                                                    rs3=ps3.executeQuery();
                                                    while(rs3.next()){
                                                        htmlContent.append(                             
                                                            "<tr class='isi9'>"+
                                                                "<td border='0' valign='middle' width='100%'>"+rs3.getString("pengkajian_skala5")+"</td>"+
                                                            "</tr>"
                                                        );
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("Notif : "+e);
                                                } finally{
                                                    if(rs3!=null){
                                                        rs3.close();
                                                    }
                                                    if(ps3!=null){
                                                        ps3.close();
                                                    }
                                                }
                                                htmlContent.append(
                                                            "</table>"+
                                                        "</td>"+
                                                    "</tr>"
                                                );
                                            }
                                            htmlContent.append(
                                                    "</table>"+
                                                "</td>"
                                            );
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
                                        "<td valign='top' cellpadding='0' cellspacing='0'>"+
                                            "<table width='100%' border='0' cellpadding='0' cellspacing='0' align='center'>"+
                                                "<tr class='isi2'>"+
                                                    "<td width='34%' valign='top'>Keputusan</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("plan")+"</td>"+
                                                "</tr>"+
                                                "<tr class='isi2'>"+
                                                    "<td width='34%' valign='top'>Tanggal & Jam</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("tanggaltriase")+"</td>"+
                                                "</tr>"+
                                                "<tr class='isi2'>"+
                                                    "<td width='34%' valign='top'>Catatan</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("catatan")+"</td>"+
                                                "</tr>"+
                                                "<tr class='isi2'>"+
                                                    "<td width='34%' valign='top'>Dokter/Petugas</td><td valign='top'>:&nbsp;</td><td width='65%' valign='top'>"+rs.getString("nik")+" "+Sequel.cariIsi("select pegawai.nama from pegawai where pegawai.nik=?",rs.getString("nik"))+"</td>"+
                                                "</tr>"+
                                            "</table>"+
                                        "</td>"
                                    );
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
                            
                            htmlContent.append(
                                "</tr>"
                            );
                        }
                        
                        LoadHTML2.setText(
                            "<html>"+
                              "<table width='1400px' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
                               htmlContent.toString()+
                              "</table>"+
                            "</html>"
                        );
                        
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

                        File f = new File("DataTriaseIGD.html");            
                        BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
                        bw.write(LoadHTML2.getText().replaceAll("<head>","<head>"+
                                    "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                                    "<table width='1400px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                        "<tr class='isi2'>"+
                                            "<td valign='top' align='center'>"+
                                                "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                                akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                                akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                                "<font size='2' face='Tahoma'>DATA TRIASE IGD<br><br></font>"+        
                                            "</td>"+
                                       "</tr>"+
                                    "</table>")
                        );
                        bw.close();                         
                        Desktop.getDesktop().browse(f.toURI());
                    } catch (Exception e) {
                        System.out.println("Notifikasi : "+e);
                    }
                    this.setCursor(Cursor.getDefaultCursor());
                    TNoRM1.setText("");
                    TPasien1.setText("");
                    LoadHTML.setText("");
                    ChkAccor.setSelected(false);
                    isMenu();
                    break;
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

    private void tbTriaseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbTriaseMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                ChkAccor.setSelected(true);
                isMenu();
                getData();
                muatUntukEdit();
            } catch (java.lang.NullPointerException e) {
                // DIAGNOSTIK sementara: NPE di sini dulu ditelan diam-diam -
                // munculkan supaya "data triase tidak tampil" bisa dilacak.
                System.out.println("[DIAG tbTriaseMouseClicked] "+e);
                e.printStackTrace();
            }
        }
}//GEN-LAST:event_tbTriaseMouseClicked

    /**
     * [Klik untuk edit] Memuat seluruh data baris terpilih di tbTriase ke field input
     * (header bersama + tab Primer/Sekunder/PACS yang relevan, termasuk checkbox
     * pemeriksaan Skala 1-5) supaya bisa langsung diedit tanpa mengetik ulang dari awal.
     * Diekstrak dari jalur lama tbTriaseKeyPressed (tombol Space) supaya bisa dipakai
     * juga dari tbTriaseMouseClicked (klik baris). Aman dipanggil dari klik biasa karena
     * hanya membaca dari database, tidak menyimpan apapun.
     */
    private void muatUntukEdit(){
        if(tbTriase.getSelectedRow()!= -1){
            try {
                Valid.tabelKosong(tabModeSkala1);
                Valid.tabelKosong(tabModeSkala2);
                Valid.tabelKosong(tabModeSkala3);
                Valid.tabelKosong(tabModeSkala4);
                Valid.tabelKosong(tabModeSkala5);
                TNoRw.setText(tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
                TNoRM.setText(tbTriase.getValueAt(tbTriase.getSelectedRow(),1).toString());
                TPasien.setText(tbTriase.getValueAt(tbTriase.getSelectedRow(),2).toString());
                Valid.SetTgl2(TanggalKunjungan,tbTriase.getValueAt(tbTriase.getSelectedRow(),3).toString());
                sinkronPanelTglKunjungan();
                CaraMasuk.setSelectedItem(tbTriase.getValueAt(tbTriase.getSelectedRow(),4).toString());
                Transportasi.setSelectedItem(tbTriase.getValueAt(tbTriase.getSelectedRow(),5).toString());
                AlasanKedatangan.setSelectedItem(tbTriase.getValueAt(tbTriase.getSelectedRow(),6).toString());
                KeteranganKedatangan.setText(tbTriase.getValueAt(tbTriase.getSelectedRow(),7).toString());
                KdKasus.setText(tbTriase.getValueAt(tbTriase.getSelectedRow(),8).toString());
                NmKasus.setText(tbTriase.getValueAt(tbTriase.getSelectedRow(),9).toString());
                // (TabPilihan sengaja TIDAK dipindah ke sini - klik baris cuma memuat data,
                // tetap di tab Data Triase; user pindah manual ke tab Input kalau mau edit)
                // [PACS] Stage f/h: load data lama tab PACS (independen dari Primer/Sekunder di bawah).
                tampilPacs(tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
                // [Header tambahan] Sudah Terpasang/Identitas Pengantar - selalu dimuat langsung dari
                // data_triase_igd (tidak lewat JOIN data_triase_pacs), supaya tetap terisi walau
                // pasien belum pernah punya baris PACS (hanya Primer/Sekunder yang pernah disimpan).
                tampilHeaderTambahan(tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
                ps=koneksi.prepareStatement(
                        "select data_triase_igdprimer.keluhan_utama,data_triase_igdprimer.kebutuhan_khusus,data_triase_igdprimer.catatan,"+
                        "data_triase_igdprimer.plan,data_triase_igdprimer.tanggaltriase,data_triase_igdprimer.nik,data_triase_igd.tekanan_darah,"+
                        "data_triase_igd.nadi,data_triase_igd.pernapasan,data_triase_igd.suhu,data_triase_igd.saturasi_o2,data_triase_igd.nyeri,"+
                        "data_triase_igd.no_rawat from data_triase_igdprimer inner join data_triase_igd on data_triase_igd.no_rawat="+
                        "data_triase_igdprimer.no_rawat where data_triase_igd.no_rawat=?");
                try {
                    ps.setString(1,tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    if(rs.next()){
                        kodepetugas=rs.getString("nik");
                        PrimerKeluhanUtama.setText(rs.getString("keluhan_utama"));
                        PrimerSuhu.setText(rs.getString("suhu"));
                        PrimerNyeri.setText(rs.getString("nyeri"));
                        PrimerTensi.setText(rs.getString("tekanan_darah"));
                        PrimerNadi.setText(rs.getString("nadi"));
                        PrimerSaturasi.setText(rs.getString("saturasi_o2"));
                        PrimerRespirasi.setText(rs.getString("pernapasan"));
                        PrimerKubutuhanKusus.setSelectedItem(rs.getString("kebutuhan_khusus"));
                        TabTriase.setSelectedIndex(0);

                        ps2=koneksi.prepareStatement(
                                "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                "from master_triase_pemeriksaan inner join master_triase_skala1 inner join data_triase_igddetail_skala1 "+
                                "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala1.kode_pemeriksaan and "+
                                "master_triase_skala1.kode_skala1=data_triase_igddetail_skala1.kode_skala1 where data_triase_igddetail_skala1.no_rawat=? "+
                                "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                TabSkala1dan2.setSelectedIndex(0);
                                Valid.tabelKosong(tabModePemeriksaan);
                                Valid.tabelKosong(tabModeSkala1);
                                rs2.beforeFirst();
                                while(rs2.next()){
                                    tabModePemeriksaan.addRow(new Object[]{rs2.getString("kode_pemeriksaan"),rs2.getString("nama_pemeriksaan")});
                                    ps3=koneksi.prepareStatement(
                                            "select master_triase_skala1.kode_skala1,master_triase_skala1.pengkajian_skala1 from master_triase_skala1 inner join data_triase_igddetail_skala1 "+
                                            "on master_triase_skala1.kode_skala1=data_triase_igddetail_skala1.kode_skala1 where "+
                                            "master_triase_skala1.kode_pemeriksaan=? and data_triase_igddetail_skala1.no_rawat=? "+
                                            "order by data_triase_igddetail_skala1.kode_skala1");
                                    try {
                                        ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                        ps3.setString(2,rs.getString("no_rawat"));
                                        rs3=ps3.executeQuery();
                                        while(rs3.next()){
                                            tabModeSkala1.addRow(new Object[]{true,rs3.getString("kode_skala1"),rs3.getString("pengkajian_skala1")});
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rs3!=null){
                                            rs3.close();
                                        }
                                        if(ps3!=null){
                                            ps3.close();
                                        }
                                    }
                                }
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

                        ps2=koneksi.prepareStatement(
                                "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                "from master_triase_pemeriksaan inner join master_triase_skala2 inner join data_triase_igddetail_skala2 "+
                                "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala2.kode_pemeriksaan and "+
                                "master_triase_skala2.kode_skala2=data_triase_igddetail_skala2.kode_skala2 where data_triase_igddetail_skala2.no_rawat=? "+
                                "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                TabSkala1dan2.setSelectedIndex(1);
                                Valid.tabelKosong(tabModePemeriksaan);
                                Valid.tabelKosong(tabModeSkala2);
                                rs2.beforeFirst();
                                while(rs2.next()){
                                    tabModePemeriksaan.addRow(new Object[]{rs2.getString("kode_pemeriksaan"),rs2.getString("nama_pemeriksaan")});
                                    ps3=koneksi.prepareStatement(
                                            "select master_triase_skala2.kode_skala2,master_triase_skala2.pengkajian_skala2 from master_triase_skala2 inner join data_triase_igddetail_skala2 "+
                                            "on master_triase_skala2.kode_skala2=data_triase_igddetail_skala2.kode_skala2 where "+
                                            "master_triase_skala2.kode_pemeriksaan=? and data_triase_igddetail_skala2.no_rawat=? "+
                                            "order by data_triase_igddetail_skala2.kode_skala2");
                                    try {
                                        ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                        ps3.setString(2,rs.getString("no_rawat"));
                                        rs3=ps3.executeQuery();
                                        while(rs3.next()){
                                            tabModeSkala2.addRow(new Object[]{true,rs3.getString("kode_skala2"),rs3.getString("pengkajian_skala2")});
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rs3!=null){
                                            rs3.close();
                                        }
                                        if(ps3!=null){
                                            ps3.close();
                                        }
                                    }
                                }
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

                        if(rs.getString("plan").equals("Ruang Resusitasi")){
                            PrimerResusitasi.setSelected(true);
                        }else if(rs.getString("plan").equals("Ruang Kritis")){
                            PrimerKritis.setSelected(true);
                        }

                        Valid.SetTgl2(PrimerTanggalTriase,rs.getString("tanggaltriase"));
                        PrimerCatatan.setText(rs.getString("catatan"));
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps!=null){
                        ps.close();
                    }
                }

                ps=koneksi.prepareStatement(
                        "select data_triase_igdsekunder.anamnesa_singkat,data_triase_igdsekunder.catatan,"+
                        "data_triase_igdsekunder.plan,data_triase_igdsekunder.tanggaltriase,data_triase_igdsekunder.nik,data_triase_igd.tekanan_darah,"+
                        "data_triase_igd.nadi,data_triase_igd.pernapasan,data_triase_igd.suhu,data_triase_igd.saturasi_o2,data_triase_igd.nyeri,"+
                        "data_triase_igd.no_rawat from data_triase_igdsekunder inner join data_triase_igd on data_triase_igd.no_rawat="+
                        "data_triase_igdsekunder.no_rawat where data_triase_igd.no_rawat=?");
                try {
                    ps.setString(1,tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    if(rs.next()){
                        kodepetugas=rs.getString("nik");
                        // [PACS] Sebelumnya nik Sekunder cuma disimpan ke variabel kodepetugas,
                        // tidak pernah dimuat balik ke widget SekunderKodePetugas/NamaPetugas -
                        // jadi kalau langsung ke tab PACS tanpa mampir tab Sekunder dulu, field
                        // ini kosong dan validasiPacs() (yang sekarang ikut baca field ini) selalu
                        // gagal. Dimuat di sini supaya konsisten dengan field header lain.
                        SekunderKodePetugas.setText(rs.getString("nik"));
                        SekunderNamaPetugas.setText(Sequel.CariPegawai(rs.getString("nik")));
                        SekunderAnamnesa.setText(rs.getString("anamnesa_singkat"));
                        SekunderSuhu.setText(rs.getString("suhu"));
                        SekunderNyeri.setText(rs.getString("nyeri"));
                        SekunderTensi.setText(rs.getString("tekanan_darah"));
                        SekunderNadi.setText(rs.getString("nadi"));
                        SekunderSaturasi.setText(rs.getString("saturasi_o2"));
                        SekunderRespirasi.setText(rs.getString("pernapasan"));
                        TabTriase.setSelectedIndex(1);

                        ps2=koneksi.prepareStatement(
                                "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                "from master_triase_pemeriksaan inner join master_triase_skala3 inner join data_triase_igddetail_skala3 "+
                                "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala3.kode_pemeriksaan and "+
                                "master_triase_skala3.kode_skala3=data_triase_igddetail_skala3.kode_skala3 where data_triase_igddetail_skala3.no_rawat=? "+
                                "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                TabSkala3dan4dan5.setSelectedIndex(0);
                                Valid.tabelKosong(tabModePemeriksaan2);
                                Valid.tabelKosong(tabModeSkala3);
                                rs2.beforeFirst();
                                while(rs2.next()){
                                    tabModePemeriksaan2.addRow(new Object[]{rs2.getString("kode_pemeriksaan"),rs2.getString("nama_pemeriksaan")});
                                    ps3=koneksi.prepareStatement(
                                            "select master_triase_skala3.kode_skala3,master_triase_skala3.pengkajian_skala3 from master_triase_skala3 inner join data_triase_igddetail_skala3 "+
                                            "on master_triase_skala3.kode_skala3=data_triase_igddetail_skala3.kode_skala3 where "+
                                            "master_triase_skala3.kode_pemeriksaan=? and data_triase_igddetail_skala3.no_rawat=? "+
                                            "order by data_triase_igddetail_skala3.kode_skala3");
                                    try {
                                        ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                        ps3.setString(2,rs.getString("no_rawat"));
                                        rs3=ps3.executeQuery();
                                        while(rs3.next()){
                                            tabModeSkala3.addRow(new Object[]{true,rs3.getString("kode_skala3"),rs3.getString("pengkajian_skala3")});
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rs3!=null){
                                            rs3.close();
                                        }
                                        if(ps3!=null){
                                            ps3.close();
                                        }
                                    }
                                }
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

                        ps2=koneksi.prepareStatement(
                                "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                "from master_triase_pemeriksaan inner join master_triase_skala4 inner join data_triase_igddetail_skala4 "+
                                "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala4.kode_pemeriksaan and "+
                                "master_triase_skala4.kode_skala4=data_triase_igddetail_skala4.kode_skala4 where data_triase_igddetail_skala4.no_rawat=? "+
                                "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                TabSkala3dan4dan5.setSelectedIndex(1);
                                Valid.tabelKosong(tabModePemeriksaan2);
                                Valid.tabelKosong(tabModeSkala4);
                                rs2.beforeFirst();
                                while(rs2.next()){
                                    tabModePemeriksaan2.addRow(new Object[]{rs2.getString("kode_pemeriksaan"),rs2.getString("nama_pemeriksaan")});
                                    ps3=koneksi.prepareStatement(
                                            "select master_triase_skala4.kode_skala4,master_triase_skala4.pengkajian_skala4 from master_triase_skala4 inner join data_triase_igddetail_skala4 "+
                                            "on master_triase_skala4.kode_skala4=data_triase_igddetail_skala4.kode_skala4 where "+
                                            "master_triase_skala4.kode_pemeriksaan=? and data_triase_igddetail_skala4.no_rawat=? "+
                                            "order by data_triase_igddetail_skala4.kode_skala4");
                                    try {
                                        ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                        ps3.setString(2,rs.getString("no_rawat"));
                                        rs3=ps3.executeQuery();
                                        while(rs3.next()){
                                            tabModeSkala4.addRow(new Object[]{true,rs3.getString("kode_skala4"),rs3.getString("pengkajian_skala4")});
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rs3!=null){
                                            rs3.close();
                                        }
                                        if(ps3!=null){
                                            ps3.close();
                                        }
                                    }
                                }
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

                        ps2=koneksi.prepareStatement(
                                "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                "from master_triase_pemeriksaan inner join master_triase_skala5 inner join data_triase_igddetail_skala5 "+
                                "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala5.kode_pemeriksaan and "+
                                "master_triase_skala5.kode_skala5=data_triase_igddetail_skala5.kode_skala5 where data_triase_igddetail_skala5.no_rawat=? "+
                                "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                TabSkala3dan4dan5.setSelectedIndex(2);
                                Valid.tabelKosong(tabModePemeriksaan2);
                                Valid.tabelKosong(tabModeSkala5);
                                rs2.beforeFirst();
                                while(rs2.next()){
                                    tabModePemeriksaan2.addRow(new Object[]{rs2.getString("kode_pemeriksaan"),rs2.getString("nama_pemeriksaan")});
                                    ps3=koneksi.prepareStatement(
                                            "select master_triase_skala5.kode_skala5,master_triase_skala5.pengkajian_skala5 from master_triase_skala5 inner join data_triase_igddetail_skala5 "+
                                            "on master_triase_skala5.kode_skala5=data_triase_igddetail_skala5.kode_skala5 where "+
                                            "master_triase_skala5.kode_pemeriksaan=? and data_triase_igddetail_skala5.no_rawat=? "+
                                            "order by data_triase_igddetail_skala5.kode_skala5");
                                    try {
                                        ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                        ps3.setString(2,rs.getString("no_rawat"));
                                        rs3=ps3.executeQuery();
                                        while(rs3.next()){
                                            tabModeSkala5.addRow(new Object[]{true,rs3.getString("kode_skala5"),rs3.getString("pengkajian_skala5")});
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rs3!=null){
                                            rs3.close();
                                        }
                                        if(ps3!=null){
                                            ps3.close();
                                        }
                                    }
                                }
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

                        if(rs.getString("plan").equals("Zona Kuning")){
                            SekunderZonaKuning.setSelected(true);
                        }else if(rs.getString("plan").equals("Zona Hijau")){
                            SekunderZonaHijau.setSelected(true);
                        }

                        Valid.SetTgl2(SekunderTanggalTriase,rs.getString("tanggaltriase"));
                        SekunderCatatan.setText(rs.getString("catatan"));
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
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
        }
    }

    private void tbTriaseKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbTriaseKeyPressed
       if(tabMode.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                TCari.setText("");
                TCari.requestFocus();
            }else if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                if(tbTriase.getSelectedRow()!= -1){
                    muatUntukEdit();
                    // Jalur lama (tombol Space) tetap langsung lompat ke tab Input, beda dari klik biasa.
                    TabPilihan.setSelectedIndex(0);
                    TNoRM1.setText("");
                    TPasien1.setText("");
                    LoadHTML.setText("");
                    ChkAccor.setSelected(false);
                    isMenu();
                }
            }
        }
}//GEN-LAST:event_tbTriaseKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        runBackground(() ->LoadData());
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
        } 
    }//GEN-LAST:event_formWindowOpened

    private void TNoRMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TNoRMActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TNoRMActionPerformed

    private void TabPilihanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabPilihanMouseClicked
        if(TabPilihan.getSelectedIndex()==1){
            runBackground(() ->tampil());
        }
    }//GEN-LAST:event_TabPilihanMouseClicked

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        if(this.getHeight()<620){   
            ScrollTriase.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            FormTriase.setPreferredSize(new Dimension(FormTriase.WIDTH,500));
            if(this.getWidth()<780){
                ScrollTriase.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);                                
                FormTriase.setPreferredSize(new Dimension(770,500));
            }else{
                ScrollTriase.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);                
            }
        }else{
            ScrollTriase.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);            
            if(this.getWidth()<780){
                ScrollTriase.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);                                
                FormTriase.setPreferredSize(new Dimension(770,FormTriase.HEIGHT));
            }else{
                ScrollTriase.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);                
            }
        }
    }//GEN-LAST:event_formWindowActivated

    private void KdKasusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdKasusKeyPressed
        //Valid.pindah(evt, TCari,kdskala);
    }//GEN-LAST:event_KdKasusKeyPressed

    private void NmKasusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NmKasusKeyPressed
        //Valid.pindah(evt,kdskala,BtnSimpan);
    }//GEN-LAST:event_NmKasusKeyPressed

    private void btnKasusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKasusActionPerformed
        MasterTriaseMacamKasus kasus=new MasterTriaseMacamKasus(null,false);
        kasus.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(kasus.getTable().getSelectedRow()!= -1){
                    KdKasus.setText(kasus.getTable().getValueAt(kasus.getTable().getSelectedRow(),0).toString());
                    NmKasus.setText(kasus.getTable().getValueAt(kasus.getTable().getSelectedRow(),1).toString());
                    perbaruiVisibilitasMekanismeTrauma();
                }
                btnKasus.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        
        kasus.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    kasus.dispose();
                }                
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        kasus.isCek();
        kasus.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        kasus.setLocationRelativeTo(internalFrame1);
        kasus.setVisible(true);
    }//GEN-LAST:event_btnKasusActionPerformed

    private void btnKasusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnKasusKeyPressed
        Valid.pindah(evt,AlasanKedatangan,CaraMasuk);
    }//GEN-LAST:event_btnKasusKeyPressed

    private void TabTriaseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabTriaseMouseClicked
        if(TabTriase.getSelectedIndex()==0){
            TabSkala1dan2MouseClicked(null);
        }else if(TabTriase.getSelectedIndex()==1){
            TabSkala3dan4dan5MouseClicked(null);
        }
    }//GEN-LAST:event_TabTriaseMouseClicked

    private void PrimerSuhuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerSuhuKeyPressed
        Valid.pindah(evt,PrimerKeluhanUtama,PrimerNyeri);
    }//GEN-LAST:event_PrimerSuhuKeyPressed

    private void PrimerSaturasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerSaturasiKeyPressed
        Valid.pindah(evt,PrimerNadi,PrimerRespirasi);
    }//GEN-LAST:event_PrimerSaturasiKeyPressed

    private void PrimerNyeriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerNyeriKeyPressed
        Valid.pindah(evt,PrimerSuhu,PrimerTensi);
    }//GEN-LAST:event_PrimerNyeriKeyPressed

    private void PrimerNadiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrimerNadiActionPerformed

    }//GEN-LAST:event_PrimerNadiActionPerformed

    private void PrimerNadiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerNadiKeyPressed
        Valid.pindah(evt,PrimerTensi,PrimerSaturasi);
    }//GEN-LAST:event_PrimerNadiKeyPressed

    private void PrimerRespirasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerRespirasiKeyPressed
        Valid.pindah(evt,PrimerSaturasi,PrimerKubutuhanKusus);
    }//GEN-LAST:event_PrimerRespirasiKeyPressed

    private void PrimerTensiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerTensiKeyPressed
        Valid.pindah(evt,PrimerNyeri,PrimerNadi);
    }//GEN-LAST:event_PrimerTensiKeyPressed

    private void PrimerKodePetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerKodePetugasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PrimerKodePetugasKeyPressed

    private void PrimerNamaPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerNamaPetugasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PrimerNamaPetugasKeyPressed

    private void btnPrimerPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrimerPetugasActionPerformed
        DlgCariPegawai pegawai=new DlgCariPegawai(null,false);
        pegawai.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(pegawai.getTable().getSelectedRow()!= -1){       
                    PrimerKodePetugas.setText(pegawai.getTable().getValueAt(pegawai.getTable().getSelectedRow(),0).toString());
                    PrimerNamaPetugas.setText(pegawai.getTable().getValueAt(pegawai.getTable().getSelectedRow(),1).toString());
                    btnPrimerPetugas.requestFocus();
                }   
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        pegawai.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        pegawai.setLocationRelativeTo(internalFrame1);
        pegawai.setAlwaysOnTop(false);
        pegawai.setVisible(true);
    }//GEN-LAST:event_btnPrimerPetugasActionPerformed

    private void btnPrimerPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPrimerPetugasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPrimerPetugasKeyPressed

    private void TCariPemeriksaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariPemeriksaanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            runBackground(() ->tampilPemeriksaan());
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            PrimerCatatan.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            PrimerKubutuhanKusus.requestFocus();
        }
    }//GEN-LAST:event_TCariPemeriksaanKeyPressed

    private void BtnCariPemeriksaanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariPemeriksaanActionPerformed
        runBackground(() ->tampilPemeriksaan());
    }//GEN-LAST:event_BtnCariPemeriksaanActionPerformed

    private void BtnCariPemeriksaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariPemeriksaanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCariPemeriksaanKeyPressed

    private void BtnTambahPemeriksaanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahPemeriksaanActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MasterTriasePemeriksaan form=new MasterTriasePemeriksaan(null,false);
        form.isCek();
        form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        form.setLocationRelativeTo(internalFrame1);
        form.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnTambahPemeriksaanActionPerformed

    private void TCariSkala1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariSkala1KeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariSkala1ActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCariSkala1.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
    }//GEN-LAST:event_TCariSkala1KeyPressed

    private void BtnCariSkala1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariSkala1ActionPerformed
        if(tbPemeriksaan.getSelectedRow()!= -1){
            runBackground(() ->LoadSkalaPrimer());
        }else{
            JOptionPane.showMessageDialog(null,"Silahkan pilih pemeriksaan terlebih dahulu..!!");
        }
    }//GEN-LAST:event_BtnCariSkala1ActionPerformed

    private void BtnCariSkala1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariSkala1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCariSkala1KeyPressed

    private void BtnTambahSkala1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahSkala1ActionPerformed
        if(TabSkala1dan2.getSelectedIndex()==0){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            MasterTriaseSkala1 form=new MasterTriaseSkala1(null,false);
            form.isCek();
            form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            form.setLocationRelativeTo(internalFrame1);
            form.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        }else if(TabSkala1dan2.getSelectedIndex()==1){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            MasterTriaseSkala2 form=new MasterTriaseSkala2(null,false);
            form.isCek();
            form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            form.setLocationRelativeTo(internalFrame1);
            form.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnTambahSkala1ActionPerformed

    private void tbPemeriksaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbPemeriksaanKeyPressed
        if(tabModePemeriksaan.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                TCariPemeriksaan.setText("");
                TCariPemeriksaan.requestFocus();
            }           
        }
    }//GEN-LAST:event_tbPemeriksaanKeyPressed

    private void tbPemeriksaanKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbPemeriksaanKeyReleased
        if(tabModePemeriksaan.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    runBackground(() ->LoadSkalaPrimer());
                } catch (java.lang.NullPointerException e) {
                }
            }          
        }
    }//GEN-LAST:event_tbPemeriksaanKeyReleased

    private void tbPemeriksaanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbPemeriksaanMouseClicked
        if(tabModePemeriksaan.getRowCount()!=0){
            try {
                runBackground(() ->LoadSkalaPrimer());
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbPemeriksaanMouseClicked

    private void TabSkala1dan2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabSkala1dan2MouseClicked
        if(TabSkala1dan2.getSelectedIndex()==0){
            label11.setForeground(new Color(170,00,0));
            TCariSkala1.setForeground(new Color(170,00,0));
            BtnTambahSkala1.setEnabled(akses.getmaster_triase_skala1());
            PrimerResusitasi.setSelected(true);
        }else if(TabSkala1dan2.getSelectedIndex()==1){
            label11.setForeground(new Color(255,0,0));
            TCariSkala1.setForeground(new Color(255,00,0));
            BtnTambahSkala1.setEnabled(akses.getmaster_triase_skala2());
            PrimerKritis.setSelected(true);
        }
    }//GEN-LAST:event_TabSkala1dan2MouseClicked

    private void SekunderSuhuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SekunderSuhuKeyPressed
        Valid.pindah(evt,SekunderAnamnesa,SekunderNyeri);
    }//GEN-LAST:event_SekunderSuhuKeyPressed

    private void SekunderSaturasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SekunderSaturasiKeyPressed
        Valid.pindah(evt,SekunderNyeri,SekunderRespirasi);
    }//GEN-LAST:event_SekunderSaturasiKeyPressed

    private void SekunderNyeriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SekunderNyeriKeyPressed
        Valid.pindah(evt,SekunderSuhu,SekunderSaturasi);
    }//GEN-LAST:event_SekunderNyeriKeyPressed

    private void SekunderNadiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SekunderNadiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SekunderNadiActionPerformed

    private void SekunderNadiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SekunderNadiKeyPressed
        Valid.pindah(evt,SekunderTensi,TCariPemeriksaan2);
    }//GEN-LAST:event_SekunderNadiKeyPressed

    private void SekunderRespirasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SekunderRespirasiKeyPressed
        Valid.pindah(evt,SekunderSaturasi,SekunderTensi);
    }//GEN-LAST:event_SekunderRespirasiKeyPressed

    private void SekunderTensiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SekunderTensiKeyPressed
        Valid.pindah(evt,SekunderRespirasi,SekunderNadi);
    }//GEN-LAST:event_SekunderTensiKeyPressed

    private void TCariPemeriksaan2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariPemeriksaan2KeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            runBackground(() ->tampilPemeriksaan2());
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            SekunderCatatan.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            SekunderNadi.requestFocus();
        }
    }//GEN-LAST:event_TCariPemeriksaan2KeyPressed

    private void BtnCariPemeriksaan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariPemeriksaan1ActionPerformed
        runBackground(() ->tampilPemeriksaan2());
    }//GEN-LAST:event_BtnCariPemeriksaan1ActionPerformed

    private void BtnCariPemeriksaan1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariPemeriksaan1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCariPemeriksaan1KeyPressed

    private void BtnTambahPemeriksaan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahPemeriksaan1ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MasterTriasePemeriksaan form=new MasterTriasePemeriksaan(null,false);
        form.isCek();
        form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        form.setLocationRelativeTo(internalFrame1);
        form.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnTambahPemeriksaan1ActionPerformed

    private void tbPemeriksaan2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbPemeriksaan2MouseClicked
        if(tabModePemeriksaan2.getRowCount()!=0){
            try {
                runBackground(() ->LoadSkalaSekunder());
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbPemeriksaan2MouseClicked

    private void tbPemeriksaan2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbPemeriksaan2KeyPressed
        if(tabModePemeriksaan2.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                TCariPemeriksaan2.setText("");
                TCariPemeriksaan2.requestFocus();
            }           
        }
    }//GEN-LAST:event_tbPemeriksaan2KeyPressed

    private void tbPemeriksaan2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbPemeriksaan2KeyReleased
        if(tabModePemeriksaan2.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    runBackground(() ->LoadSkalaSekunder());
                } catch (java.lang.NullPointerException e) {
                }
            }          
        }
    }//GEN-LAST:event_tbPemeriksaan2KeyReleased

    private void TCariSkala3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariSkala3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TCariSkala3KeyPressed

    private void BtnCariSkala2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariSkala2ActionPerformed
        if(tbPemeriksaan2.getSelectedRow()!= -1){
            runBackground(() ->LoadSkalaSekunder());
        }else{
            JOptionPane.showMessageDialog(null,"Silahkan pilih pemeriksaan terlebih dahulu..!!");
        }
    }//GEN-LAST:event_BtnCariSkala2ActionPerformed

    private void BtnCariSkala2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariSkala2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCariSkala2KeyPressed

    private void BtnTambahSkala2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahSkala2ActionPerformed
        switch (TabSkala3dan4dan5.getSelectedIndex()) {
            case 0:
                {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    MasterTriaseSkala3 form=new MasterTriaseSkala3(null,false);
                    form.isCek();
                    form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                    form.setLocationRelativeTo(internalFrame1);
                    form.setVisible(true);
                    this.setCursor(Cursor.getDefaultCursor());
                    break;
                }
            case 1:
                {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    MasterTriaseSkala4 form=new MasterTriaseSkala4(null,false);
                    form.isCek();
                    form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                    form.setLocationRelativeTo(internalFrame1);
                    form.setVisible(true);
                    this.setCursor(Cursor.getDefaultCursor());
                    break;
                }
            case 2:
                {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    MasterTriaseSkala5 form=new MasterTriaseSkala5(null,false);
                    form.isCek();
                    form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                    form.setLocationRelativeTo(internalFrame1);
                    form.setVisible(true);
                    this.setCursor(Cursor.getDefaultCursor());
                    break;
                }
            default:
                break;
        }
    }//GEN-LAST:event_BtnTambahSkala2ActionPerformed

    private void TabSkala3dan4dan5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabSkala3dan4dan5MouseClicked
        switch (TabSkala3dan4dan5.getSelectedIndex()) {
            case 0:
                label13.setForeground(new Color(200,200,0));
                TCariSkala3.setForeground(new Color(200,200,0));
                BtnTambahSkala2.setEnabled(akses.getmaster_triase_skala3());
                SekunderZonaKuning.setSelected(true);
                break;
            case 1:
                label13.setForeground(new Color(0,170,0));
                TCariSkala3.setForeground(new Color(0,170,0));
                BtnTambahSkala2.setEnabled(akses.getmaster_triase_skala4());
                SekunderZonaHijau.setSelected(true);
                break;
            case 2:
                label13.setForeground(new Color(150,150,150));
                TCariSkala3.setForeground(new Color(150,150,150));
                BtnTambahSkala2.setEnabled(akses.getmaster_triase_skala5());
                SekunderZonaHijau.setSelected(true);
                break;
            default:
                break;
        }
    }//GEN-LAST:event_TabSkala3dan4dan5MouseClicked

    private void PrimerCatatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerCatatanKeyPressed
        Valid.pindah(evt,TCariPemeriksaan,PrimerTanggalTriase);
    }//GEN-LAST:event_PrimerCatatanKeyPressed

    private void SekunderKodePetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SekunderKodePetugasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_SekunderKodePetugasKeyPressed

    private void SekunderNamaPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SekunderNamaPetugasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_SekunderNamaPetugasKeyPressed

    private void btnSekunderPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSekunderPetugasActionPerformed
        DlgCariPegawai pegawai=new DlgCariPegawai(null,false);
        pegawai.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(pegawai.getTable().getSelectedRow()!= -1){   
                    SekunderKodePetugas.setText(pegawai.getTable().getValueAt(pegawai.getTable().getSelectedRow(),0).toString());
                    SekunderNamaPetugas.setText(pegawai.getTable().getValueAt(pegawai.getTable().getSelectedRow(),1).toString());
                    btnSekunderPetugas.requestFocus();
                }   
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        pegawai.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        pegawai.setLocationRelativeTo(internalFrame1);
        pegawai.setAlwaysOnTop(false);
        pegawai.setVisible(true);
    }//GEN-LAST:event_btnSekunderPetugasActionPerformed

    private void btnSekunderPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnSekunderPetugasKeyPressed
        Valid.pindah(evt,SekunderTanggalTriase,BtnSimpan);
    }//GEN-LAST:event_btnSekunderPetugasKeyPressed

    private void SekunderCatatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SekunderCatatanKeyPressed
        Valid.pindah(evt,TCariPemeriksaan2,SekunderTanggalTriase);
    }//GEN-LAST:event_SekunderCatatanKeyPressed

    private void TransportasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TransportasiKeyPressed
        Valid.pindah(evt,TCari,TanggalKunjungan);
    }//GEN-LAST:event_TransportasiKeyPressed

    private void TanggalKunjunganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKunjunganKeyPressed
        Valid.pindah(evt,Transportasi,CaraMasuk);
    }//GEN-LAST:event_TanggalKunjunganKeyPressed

    private void AlasanKedatanganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AlasanKedatanganKeyPressed
        Valid.pindah(evt,CaraMasuk,btnKasus);
    }//GEN-LAST:event_AlasanKedatanganKeyPressed

    private void CaraMasukKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CaraMasukKeyPressed
        Valid.pindah(evt,TanggalKunjungan,AlasanKedatangan);
    }//GEN-LAST:event_CaraMasukKeyPressed

    private void KeteranganKedatanganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganKedatanganKeyPressed
        if(TabTriase.getSelectedIndex()==0){
            Valid.pindah(evt,CaraMasuk,PrimerKeluhanUtama);
        }else if(TabTriase.getSelectedIndex()==1){
            Valid.pindah(evt,CaraMasuk,SekunderAnamnesa);
        }
    }//GEN-LAST:event_KeteranganKedatanganKeyPressed

    private void SekunderAnamnesaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SekunderAnamnesaKeyPressed
        Valid.pindah2(evt,KeteranganKedatangan,SekunderSuhu);
    }//GEN-LAST:event_SekunderAnamnesaKeyPressed

    private void PrimerKeluhanUtamaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerKeluhanUtamaKeyPressed
       Valid.pindah2(evt,KeteranganKedatangan,PrimerSuhu);
    }//GEN-LAST:event_PrimerKeluhanUtamaKeyPressed

    private void PrimerKubutuhanKususKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerKubutuhanKususKeyPressed
        Valid.pindah(evt,PrimerRespirasi,TCariPemeriksaan);
    }//GEN-LAST:event_PrimerKubutuhanKususKeyPressed

    private void PrimerTanggalTriaseKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PrimerTanggalTriaseKeyPressed
        Valid.pindah(evt,PrimerCatatan,btnPrimerPetugas);
    }//GEN-LAST:event_PrimerTanggalTriaseKeyPressed

    private void btnPrimerPetugasKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPrimerPetugasKeyReleased
        Valid.pindah(evt,PrimerTanggalTriase,BtnSimpan);
    }//GEN-LAST:event_btnPrimerPetugasKeyReleased

    private void SekunderTanggalTriaseKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SekunderTanggalTriaseKeyPressed
        Valid.pindah(evt,SekunderCatatan,btnSekunderPetugas);
    }//GEN-LAST:event_SekunderTanggalTriaseKeyPressed

    private void tbTriaseKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbTriaseKeyReleased
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }          
        }
    }//GEN-LAST:event_tbTriaseKeyReleased

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
        if(tbTriase.getSelectedRow()!= -1){
            isMenu();
        }else{
            ChkAccor.setSelected(false);
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data yang mau ditampilkan triasenya...!!!!");
        }
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void BtnPrint1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrint1ActionPerformed
        // Body method GEN-FIRST/GEN-LAST aman diedit; NetBeans mempertahankannya.
        //
        // SEBELUMNYA: tombol ini memilih lembar berdasarkan warna panel detail -
        // "Triase PACS" ke RM 03-1, selain itu jatuh ke rptLembarTriaseSkala1..5
        // bawaan Khanza sesuai skalanya. Akibatnya record Triase Primer mencetak
        // Skala 1, dan itu GAGAL dengan FileNotFoundException karena
        // rptLembarTriaseSkala1.jasper dan Skala2 memang TIDAK PERNAH ADA di
        // repo - hanya .jrxml-nya. Dilaporkan user dari layar.
        //
        // SEKARANG: selalu mencetak RM 03-1 (rptLembarTriasePacs), karena itulah
        // formulir triase yang dipakai RSUD. Lembar Skala 1-5 adalah formulir
        // bawaan Khanza yang tidak dipakai di sini.
        //
        // Aman untuk record yang belum punya data PACS: query cetakPacs memakai
        // `left join data_triase_pacs`, jadi barisnya tetap ada dan bagian PACS
        // tercetak kosong - sama seperti kertasnya yang memang tinggal diisi.
        if(TNoRM1.getText().trim().equals("")||TPasien1.getText().trim().equals("")){
            Valid.textKosong(TCari,"Pasien");
            return;
        }
        if(tbTriase.getSelectedRow()<=-1){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih dulu barisnya...!!!!");
            return;
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        cetakPacs(tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnPrint1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMTriaseIGD dialog = new RMTriaseIGD(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.ComboBox AlasanKedatangan;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnCariPemeriksaan;
    private widget.Button BtnCariPemeriksaan1;
    private widget.Button BtnCariSkala1;
    private widget.Button BtnCariSkala2;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnPrint1;
    private widget.Button BtnSimpan;
    private widget.Button BtnTambahPemeriksaan;
    private widget.Button BtnTambahPemeriksaan1;
    private widget.Button BtnTambahSkala1;
    private widget.Button BtnTambahSkala2;
    private widget.ComboBox CaraMasuk;
    private widget.CekBox ChkAccor;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.PanelBiasa FormInput;
    private widget.PanelBiasa FormMenu;
    private widget.InternalFrame FormTriase;
    private widget.TextBox KdKasus;
    private widget.TextBox KeteranganKedatangan;
    private widget.Label LCount;
    private widget.editorpane LoadHTML;
    private widget.editorpane LoadHTML2;
    private widget.TextBox NmKasus;
    private widget.PanelBiasa PanelAccor;
    private widget.TextBox PrimerCatatan;
    private widget.TextArea PrimerKeluhanUtama;
    private widget.TextBox PrimerKodePetugas;
    private widget.RadioButton PrimerKritis;
    private widget.ComboBox PrimerKubutuhanKusus;
    private widget.TextBox PrimerNadi;
    private widget.TextBox PrimerNamaPetugas;
    private widget.TextBox PrimerNyeri;
    private widget.TextBox PrimerRespirasi;
    private widget.RadioButton PrimerResusitasi;
    private widget.TextBox PrimerSaturasi;
    private widget.TextBox PrimerSuhu;
    private widget.Tanggal PrimerTanggalTriase;
    private widget.TextBox PrimerTensi;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll2;
    private widget.ScrollPane Scroll3;
    private widget.ScrollPane Scroll4;
    private widget.ScrollPane Scroll5;
    private widget.ScrollPane Scroll6;
    private widget.ScrollPane Scroll7;
    private widget.ScrollPane Scroll8;
    private widget.ScrollPane ScrollHTML;
    private widget.ScrollPane ScrollTriase;
    private widget.TextArea SekunderAnamnesa;
    private widget.TextBox SekunderCatatan;
    private widget.TextBox SekunderKodePetugas;
    private widget.TextBox SekunderNadi;
    private widget.TextBox SekunderNamaPetugas;
    private widget.TextBox SekunderNyeri;
    private widget.TextBox SekunderRespirasi;
    private widget.TextBox SekunderSaturasi;
    private widget.TextBox SekunderSuhu;
    private widget.Tanggal SekunderTanggalTriase;
    private widget.TextBox SekunderTensi;
    private widget.RadioButton SekunderZonaHijau;
    private widget.RadioButton SekunderZonaKuning;
    private widget.TextBox TCari;
    private widget.TextBox TCariPemeriksaan;
    private widget.TextBox TCariPemeriksaan2;
    private widget.TextBox TCariSkala1;
    private widget.TextBox TCariSkala3;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRM1;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.TextBox TPasien1;
    private javax.swing.JTabbedPane TabPilihan;
    private javax.swing.JTabbedPane TabSkala1dan2;
    private javax.swing.JTabbedPane TabSkala3dan4dan5;
    private javax.swing.JTabbedPane TabTriase;
    private widget.Tanggal TanggalKunjungan;
    private widget.ComboBox Transportasi;
    private widget.Button btnKasus;
    private widget.Button btnPrimerPetugas;
    private widget.Button btnSekunderPetugas;
    private javax.swing.ButtonGroup buttonGroup1;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame10;
    private widget.InternalFrame internalFrame11;
    private widget.InternalFrame internalFrame12;
    private widget.InternalFrame internalFrame13;
    private widget.InternalFrame internalFrame14;
    private widget.InternalFrame internalFrame3;
    private widget.InternalFrame internalFrame4;
    private widget.InternalFrame internalFrame5;
    private widget.InternalFrame internalFrame7;
    private widget.InternalFrame internalFrame8;
    private widget.InternalFrame internalFrame9;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel26;
    private widget.Label jLabel27;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel37;
    private widget.Label jLabel38;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private widget.Label label10;
    private widget.Label label11;
    private widget.Label label12;
    private widget.Label label13;
    private widget.Label label14;
    private widget.Label label15;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.panelisi panelisi5;
    private widget.panelisi panelisi6;
    private widget.panelisi panelisi7;
    private widget.panelisi panelisi8;
    private widget.ScrollPane scrollPane1;
    private widget.ScrollPane scrollPane2;
    private widget.Table tbPemeriksaan;
    private widget.Table tbPemeriksaan2;
    private widget.Table tbSkala1;
    private widget.Table tbSkala2;
    private widget.Table tbSkala3;
    private widget.Table tbSkala4;
    private widget.Table tbSkala5;
    private widget.Table tbTriase;
    // End of variables declaration//GEN-END:variables

    // =====================================================================
    // [Header tambahan] "Sudah Terpasang" (Infus/NGT/Kateter) + "Identitas
    // Pengantar" (Nama/Telp) - dipindah dari tab PACS ke header bersama
    // (FormInput) supaya bisa diisi/terlihat dari tab Primer, Sekunder,
    // MAUPUN PACS (kolom data_triase_igd sama, dipakai bersama). Nama field
    // tetap pakai awalan "Pacs" (tidak diganti) supaya kode simpan/muat PACS
    // yang sudah ada (baca widget yang sama persis) tidak perlu diubah.
    // Ditambahkan terisolasi di luar blok GEN-BEGIN/GEN-END - lihat
    // initHeaderTambahan(), dipanggil dari konstruktor setelah initComponents().
    // =====================================================================
    private javax.swing.JToggleButton PacsInfus, PacsNgt, PacsKateter; // [Bagian B.10] chip - dulu widget.CekBox, keduanya turunan AbstractButton
    private widget.TextBox PacsNamaPengantar, PacsTelpPengantar;
    // Fase 4 (Patient Banner): 4 label ini awalnya variabel LOKAL di dalam
    // initHeaderTambahan() - dinaikkan jadi field kelas supaya bisa
    // direparent ulang oleh initPatientBanner() (lihat method itu). Bukan
    // rename/hapus variabel UI - cuma perubahan scope, teks/isi tidak berubah.
    private widget.Label lblTerpasang, lblIdentitasPengantar, lblNamaPengantar, lblTelpPengantar;
    // [Bagian B] Label kartu "IDENTITAS PASIEN" - cerminan read-only dari
    // TNoRw/TNoRM/TPasien (yg sekarang setVisible(false)). Bukan sumber data.
    private widget.Label lblKartuNama, lblKartuJK, lblKartuRM, lblKartuRawat;

    // =====================================================================
    // [PACS] Field baru - Patient's Acuity Categorization Scale (formulir
    // target RM 03-1). Ditambahkan terisolasi di luar blok GEN-BEGIN/GEN-END
    // di atas supaya TIDAK ikut ditimpa NetBeans Form Editor saat form lama
    // (Skala 1-5 Primer/Sekunder) dibuka/disimpan ulang lewat GUI builder.
    // Tab ini berdampingan dengan tab Primer/Sekunder yang sudah ada -
    // tidak mengubah/menghapus fungsi Triase lama.
    // =====================================================================
    private widget.PanelBiasa PanelPacs;
    private AvatarBulatPacs avatarKartuPacs; // [Bagian C.12] avatar/inisial di kartu identitas
    private widget.TextBox PacsTensi, PacsNadi, PacsRespirasi, PacsSuhu, PacsSaturasi, PacsNyeri;
    private javax.swing.JToggleButton tglNyeriYa, tglNyeriTidak; // [Bagian B.9] murni tampilan, tidak menambah kolom DB
    private widget.ComboBox PacsKesadaran;
    private widget.TextBox PacsBeratBadan, PacsTinggiBadan;
    private widget.ComboBox CmbSkorKesadaranPacs, CmbSkorTDPacs, CmbSkorNadiPacs, CmbSkorRespirasiPacs, CmbSkorTemperaturPacs, CmbSkorSaturasiPacs;
    private widget.Label LblTotalSkorPacs;
    private widget.RadioButton RbPacs1, RbPacs2, RbPacs3, RbPacs4;
    private javax.swing.ButtonGroup bgLevelPacs;
    private widget.RadioButton RbPacsResusitasi, RbPacsNonResusitasi, RbPacsPoliKlinik, RbPacsDOA;
    private javax.swing.ButtonGroup bgKeputusanPacs;

    // =====================================================================
    // [PACS - modernisasi tampilan sub-tab, UI-only] Komponen visual baru.
    // TIDAK terikat ke DB: semuanya cuma "cermin" komponen lama di atas yang
    // sekarang di-setVisible(false). Nilai yang disimpan/dimuat tetap dibaca
    // dari komponen lama (PacsKesadaran, CmbSkor*Pacs, RbPacs*, LblTotalSkorPacs).
    // =====================================================================
    private widget.Label[][] selSkorPacs;                 // [6 parameter][4 skor] sel indikator 0-3
    private javax.swing.JToggleButton tblAvpuA, tblAvpuV, tblAvpuP, tblAvpuU;
    private javax.swing.ButtonGroup bgAvpuPacs;
    private widget.PanelBiasa bannerTotalPacs;            // banner "TOTAL SKOR EWS"
    private widget.Label lblBannerTafsirPacs, lblBannerSatuanPacs;
    private widget.PanelBiasa[] kartuLevelPacs;           // 4 kartu Level Triage
    private widget.PanelBiasa[] kartuStripPacs;           // strip warna 4px tiap kartu
    private widget.Label[] kartuJudulPacs, kartuDescPacs;
    private widget.Label lblSaranLevelPacs;               // badge "Saran sistem: PACS n"
    private widget.PanelBiasa[] bungkusKeputusanPacs;     // 4 pembungkus radio Keputusan
    private widget.Label lblRingkasanPacs;                // ringkasan pilihan di action bar bawah
    private widget.PanelBiasa barisMekanismePacs;         // pembungkus (label+panel) Mekanisme Trauma - ikut disembunyikan
    private final java.awt.Color[] warnaLevelPacs = {
        widget.ThemeConfig.DANGER, widget.ThemeConfig.WARN, widget.ThemeConfig.PACS3, widget.ThemeConfig.OK
    };
    private final java.awt.Color[] warnaLevelBgPacs = {
        widget.ThemeConfig.DANGER_BG, widget.ThemeConfig.WARN_BG, widget.ThemeConfig.PACS3_BG, widget.ThemeConfig.OK_BG
    };
    // Teal #0d7a70 sesuai mockup, dipakai chip "Sudah Terpasang" & radio
    // Kesadaran (AVPU) saat terpilih - konstanta lokal form ini, BUKAN
    // ThemeConfig.ACCENT (itu konstanta global dipakai di seluruh aplikasi).
    private static final java.awt.Color WARNA_TERPILIH_PACS = new java.awt.Color(0x0D,0x7A,0x70);
    private static String hexWarna(java.awt.Color c){
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
    // [Bagian D] Pukul Keputusan - reintroduce sesuai mockup: widget jam yang
    // bisa diedit user (dulu dihapus & diturunkan otomatis dari TanggalKunjungan).
    // Tanggal/Jam Triase (tanggal_triase) TETAP diturunkan dari TanggalKunjungan,
    // tidak berubah.
    private RMPenilaianAwalKeperawatanIGD.PanelJamSaja DTPKeputusanPukulPacs;
    private widget.Button btnKeputusanPukulSekarangPacs;
    // (Petugas PACS dihapus - dipakai bersama dengan Dokter/Petugas tab Sekunder:
    // SekunderKodePetugas/SekunderNamaPetugas/btnSekunderPetugas)
    private widget.Button btnPrintPacs;
    // -- Kelompok A sisa: Keluhan --
    // (Kontak Awal dihapus dari tab PACS - nilainya diambil dari TanggalKunjungan/header, tidak perlu inputan terpisah)
    private widget.TextArea PacsKeluhanUtama;
    private widget.Label lblCounterKeluhanUtama; // [Bagian B.8] "N/400" - batas kolom keluhan_utama VARCHAR(400)
    // Catatan Khusus - kolom di formulir fisik RM 03-1 dekat baris Total skor EWS
    // (perlu ALTER TABLE data_triase_pacs ADD COLUMN catatan_khusus - lihat sik_triase_pacs.sql)
    private widget.TextArea PacsCatatanKhusus;
    // -- Kelompok B: Mekanisme Trauma (tampil hanya jika Kasus = Trauma) --
    private widget.PanelBiasa panelMekanismeTrauma;
    private widget.CekBox CekKklTunggal, CekKklDenganLawan, CekJatuhKetinggian, CekLukaBakar, CekTraumaFisik, CekTraumaZatKimia, CekTraumaZatLainnya;
    private widget.TextBox TKklLawan, TKklTempatKejadian, TJatuhKetinggianKet, TLukaBakarKet, TTraumaFisikKet, TTraumaZatKimiaKet, TTraumaZatLainnyaKet;
    // [Bagian E] Tanggal & Pukul Kejadian dipisah jadi kotak tanggal + 3 kotak
    // jam (pola sama seperti PanelJamSaja/"pola Registrasi" di
    // RMPenilaianAwalKeperawatanIGD) - field ini betulan diedit user (waktu
    // kejadian trauma bisa beda dari waktu kunjungan), jadi diganti total,
    // bukan cuma dibungkus.
    private RMPenilaianAwalKeperawatanIGD.PanelTanggalDanJam panelKklKejadian;
    // [Bagian E] Tampilan kotak Tgl/Jam Kunjungan di header - HANYA tampilan,
    // TanggalKunjungan (GEN) tetap satu-satunya sumber nilai (dipakai di ~15
    // tempat baca lain di file ini). Disabled supaya tidak ada yang mengira
    // mengetik di sini mengubah nilai yang tersimpan.
    private RMPenilaianAwalKeperawatanIGD.PanelTanggalDanJam panelTglKunjungan;

    /**
     * [Header tambahan] Baris "Sudah Terpasang" + "Identitas Pengantar",
     * ditambahkan langsung ke FormInput (header bersama Primer/Sekunder/PACS)
     * satu baris di bawah baris "Kode/Macam Kasus" (y=70), sejajar seperti
     * baris header lainnya - bukan dipisah tab PACS lagi. FormInput pakai
     * null layout (posisi absolut via setBounds), sama seperti baris-baris
     * lain di header, jadi aman ditambah dari luar blok GEN-BEGIN/GEN-END.
     * Dipanggil sekali dari constructor, setelah initComponents().
     */
    private void initHeaderTambahan(){
        // [Modernisasi header - Bagian B] initHeaderTambahan() sekarang HANYA
        // membuat objek widget-nya; SELURUH penataan tata letak pindah ke
        // initPatientBanner() (GridBagLayout, bukan setBounds absolut lagi).
        // "Sudah Terpasang" (Infus/NGT/Kateter) DIPINDAH ke kolom Anamnesa
        // sub-tab PACS (temuan klinis, bukan data registrasi) - lihat
        // initPacsTab(). Widget-nya tetap dibuat di sini supaya sudah ada
        // saat initPacsTab() (dipanggil belakangan) menaruhnya ke panel.
        lblTerpasang = new widget.Label();
        lblTerpasang.setText("Sudah Terpasang");

        PacsInfus = new javax.swing.JToggleButton("Infus"); gayakanChipPacs(PacsInfus);
        PacsNgt = new javax.swing.JToggleButton("NGT"); gayakanChipPacs(PacsNgt);
        PacsKateter = new javax.swing.JToggleButton("Kateter Urin"); gayakanChipPacs(PacsKateter);

        lblIdentitasPengantar = new widget.Label(); lblIdentitasPengantar.setText("Identitas Pengantar");
        lblNamaPengantar = new widget.Label(); lblNamaPengantar.setText("Nama");
        lblTelpPengantar = new widget.Label(); lblTelpPengantar.setText("Telp");
        PacsNamaPengantar = new widget.TextBox();
        PacsTelpPengantar = new widget.TextBox();

        // [Bagian B.11] Dipindah kesini (dari initPacsTab()) supaya sudah ada
        // saat initActionBar() (dipanggil sesudah method ini) menaruhnya di
        // action bar bawah bersama Baru/Hapus/Ganti/Semua.
        btnPrintPacs = new widget.Button(); btnPrintPacs.setText("Cetak Lembar Triase PACS");
        btnPrintPacs.addActionListener((java.awt.event.ActionEvent evt) -> btnPrintPacsActionPerformed(evt));
    }

    // ---- konstanta warna header (dari widget.ThemeConfig / Bagian A) ----
    private static java.awt.Color warnaTema(java.awt.Color c){ return c; }

    /**
     * [Modernisasi header - Bagian B] FormInput ditata ulang: 2 sel
     * (MigLayout `[262!][grow,fill]` ditiru dgn GridBagLayout krn MigLayout
     * tidak ada di classpath proyek & aturan "1 file" melarang menambah jar).
     *
     *  - Sel KIRI  : kartu "IDENTITAS PASIEN" (badge TERKUNCI) berisi HANYA
     *                widget.Label (nama, JK+umur, No. RM, No. Rawat).
     *                TextBox lama (TNoRw/TNoRM/TPasien) tetap dibuat &
     *                di-setVisible(false) - logika simpan yg baca .getText()
     *                tidak pecah; label kartu disinkronkan dari isinya.
     *  - Sel KANAN : grid 5 kolom x 2 baris, label DI ATAS field.
     */
    private void initPatientBanner(){
        FormInput.removeAll();
        FormInput.setLayout(new java.awt.GridBagLayout());
        FormInput.setBackground(widget.ThemeConfig.SURFACE);
        FormInput.setBorder(javax.swing.BorderFactory.createEmptyBorder(12,14,12,14));

        // TextBox read-only lama -> disembunyikan, jadi sumber data tersembunyi.
        TNoRw.setVisible(false); TNoRM.setVisible(false); TPasien.setVisible(false);

        // ===================== SEL KIRI: kartu identitas =====================
        widget.PanelBiasa kartu = new widget.PanelBiasa();
        kartu.setLayout(new java.awt.GridBagLayout());
        kartu.setOpaque(true);
        kartu.setBackground(widget.ThemeConfig.SURFACE_ALT);
        kartu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
            javax.swing.BorderFactory.createEmptyBorder(12,14,12,14)));

        widget.Label lblJudulKartu = new widget.Label();
        lblJudulKartu.setText("IDENTITAS PASIEN");
        lblJudulKartu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblJudulKartu.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        lblJudulKartu.setForeground(widget.ThemeConfig.TEXT_MUTED);

        widget.Label badgeKunci = new widget.Label();
        badgeKunci.setText("  TERKUNCI  ");
        badgeKunci.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        badgeKunci.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 9));
        badgeKunci.setOpaque(true);
        badgeKunci.setBackground(widget.ThemeConfig.BORDER_SOFT);
        badgeKunci.setForeground(widget.ThemeConfig.TEXT_MUTED);

        lblKartuNama = new widget.Label();
        lblKartuNama.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKartuNama.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        lblKartuNama.setForeground(widget.ThemeConfig.TEXT);
        lblKartuJK = new widget.Label();
        lblKartuJK.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKartuJK.setForeground(widget.ThemeConfig.TEXT_MUTED);
        lblKartuRM = new widget.Label();
        lblKartuRM.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKartuRM.setForeground(widget.ThemeConfig.TEXT_MUTED);
        lblKartuRawat = new widget.Label();
        lblKartuRawat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblKartuRawat.setForeground(widget.ThemeConfig.TEXT_MUTED);

        // [Bagian C.12] Avatar bulat berisi inisial nama - kolom paling kiri,
        // membentang seluruh tinggi baris judul s/d No. Rawat.
        avatarKartuPacs = new AvatarBulatPacs();
        java.awt.Dimension davatar = new java.awt.Dimension(40,40);
        avatarKartuPacs.setPreferredSize(davatar); avatarKartuPacs.setMinimumSize(davatar);
        avatarKartuPacs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));

        // [Bagian C.12] Progress kelengkapan - KOSMETIK saja utk iterasi ini,
        // belum dihubungkan ke penghitungan isi form sesungguhnya.
        widget.Label lblProgresKartu = new widget.Label();
        lblProgresKartu.setText("Kelengkapan triase");
        lblProgresKartu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblProgresKartu.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 9));
        lblProgresKartu.setForeground(widget.ThemeConfig.TEXT_FAINT);
        javax.swing.JProgressBar progresKartu = new javax.swing.JProgressBar(0,100);
        progresKartu.setValue(50);
        progresKartu.setStringPainted(false);
        java.awt.Dimension dprog = new java.awt.Dimension(10,6);
        progresKartu.setPreferredSize(dprog);

        java.awt.GridBagConstraints k = new java.awt.GridBagConstraints();
        k.gridx=0; k.gridy=0; k.gridheight=5; k.anchor=java.awt.GridBagConstraints.NORTHWEST;
        k.fill=java.awt.GridBagConstraints.NONE; k.weightx=0; k.insets=new java.awt.Insets(0,0,0,10);
        kartu.add(avatarKartuPacs,k);

        k.gridx=1; k.gridy=0; k.gridheight=1; k.anchor=java.awt.GridBagConstraints.WEST;
        k.fill=java.awt.GridBagConstraints.HORIZONTAL; k.weightx=1.0; k.insets=new java.awt.Insets(0,0,0,0);
        kartu.add(lblJudulKartu,k);
        k.gridx=2; k.weightx=0; k.fill=java.awt.GridBagConstraints.NONE;
        k.anchor=java.awt.GridBagConstraints.EAST; kartu.add(badgeKunci,k);
        k.gridx=1; k.gridwidth=2; k.weightx=1.0; k.anchor=java.awt.GridBagConstraints.WEST;
        k.fill=java.awt.GridBagConstraints.HORIZONTAL;
        k.gridy=1; k.insets=new java.awt.Insets(7,0,0,0);  kartu.add(lblKartuNama,k);
        k.gridy=2; k.insets=new java.awt.Insets(2,0,0,0);   kartu.add(lblKartuJK,k);
        k.gridy=3; k.insets=new java.awt.Insets(7,0,0,0);   kartu.add(lblKartuRM,k);
        k.gridy=4; k.insets=new java.awt.Insets(3,0,0,0);   kartu.add(lblKartuRawat,k);
        k.gridx=0; k.gridwidth=3; k.gridy=5; k.insets=new java.awt.Insets(8,0,0,0);
        kartu.add(lblProgresKartu,k);
        k.gridy=6; k.insets=new java.awt.Insets(2,0,0,0);
        kartu.add(progresKartu,k);
        k.gridy=7; k.weighty=1.0; k.fill=java.awt.GridBagConstraints.BOTH; k.insets=new java.awt.Insets(0,0,0,0);
        kartu.add(javax.swing.Box.createGlue(),k);

        // sinkronisasi label kartu <- TextBox tersembunyi
        javax.swing.event.DocumentListener sinkronKartu = new javax.swing.event.DocumentListener(){
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e){ sinkronKartuIdentitas(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e){ sinkronKartuIdentitas(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e){ sinkronKartuIdentitas(); }
        };
        TNoRw.getDocument().addDocumentListener(sinkronKartu);
        TNoRM.getDocument().addDocumentListener(sinkronKartu);
        TPasien.getDocument().addDocumentListener(sinkronKartu);
        sinkronKartuIdentitas();

        // ===================== SEL KANAN: grid 5 x 2 =====================
        widget.PanelBiasa grid = new widget.PanelBiasa();
        grid.setOpaque(false);
        grid.setLayout(new java.awt.GridBagLayout());

        // tombol lookup -> glyph "cari", 30px, di dalam pembungkus berborder.
        btnKasus.setText("⌕"); btnKasus.setIcon(null);
        btnKasus.setPreferredSize(new java.awt.Dimension(30,30));
        btnSekunderPetugas.setText("⌕"); btnSekunderPetugas.setIcon(null);
        btnSekunderPetugas.setPreferredSize(new java.awt.Dimension(30,30));
        KdKasus.setVisible(false);
        SekunderKodePetugas.setVisible(false);

        java.awt.Component selMacamKasus   = hdrLookup(NmKasus, btnKasus);
        java.awt.Component selDokterPetugas = hdrLookup(SekunderNamaPetugas, btnSekunderPetugas);
        // (penanda wajib cukup tanda "*" merah di label - tanpa border merah pada field, sesuai permintaan)

        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.fill=java.awt.GridBagConstraints.HORIZONTAL;
        g.anchor=java.awt.GridBagConstraints.NORTH;
        g.weightx=0.2; g.weighty=0;
        g.insets=new java.awt.Insets(0,0,0,14);
        // baris 1
        // [Bagian E] Tgl. Kunjungan dipecah jadi kotak tanggal + 3 kotak jam
        // ("pola Registrasi") - HANYA tampilan; TanggalKunjungan (GEN) tetap
        // dipertahankan sebagai sumber nilai (dibaca ~15 tempat lain di file
        // ini) dan disinkronkan ke panel ini lewat sinkronPanelTglKunjungan().
        // Disabled supaya jelas ini bukan field yang bisa diketik di sini.
        panelTglKunjungan = new RMPenilaianAwalKeperawatanIGD.PanelTanggalDanJam(false, 30);
        panelTglKunjungan.setEnabled(false);
        sinkronPanelTglKunjungan();
        // hdrCell() memaksa preferredSize field jadi lebar 0 (trik supaya
        // GridBagLayout bagi rata murni via weightx) - itu aman utk field
        // biasa (JComboBox/TextBox ikut melar penuh lewat BorderLayout.
        // CENTER), TAPI panelTglKunjungan isinya FlowLayout (tanggal+3 kotak
        // jam, total >=306px sejak combo jam dilebarkan 46->62px utk fix
        // elipsis "...") yang TIDAK melar mengikuti bounds parent - kalau
        // kolomnya dibiarkan sempit, isinya terpotong. Maka sel pembungkusnya
        // di-override lebar minimalnya di sini saja.
        //
        // TINGGI sengaja TIDAK ikut di-hardcode ke angka tetap (spt sebelumnya,
        // 30) - itu menyebabkan bug nyata: sel ini punya label (NORTH) + field
        // (CENTER) via BorderLayout, tingginya HARUS = tinggi label + gap(3) +
        // tinggi field. Sel header lain aman pakai tinggi 30 polos karena
        // preferredSize mereka TIDAK di-override sama sekali (BorderLayout
        // hitung otomatis label+gap+field). Begitu wrapper ini dipaksa 30
        // padahal butuh lebih, BorderLayout memberi CENTER (panelTglKunjungan)
        // sisa ruang yg SANGAT sempit (~10px, dibuktikan lewat Robot capture
        // nyata: getSize() mengembalikan tinggi 10) - isi field jadi hampir
        // tidak kelihatan sama sekali, bukan cuma terpotong. Ambil tinggi
        // alami hasil hitungan BorderLayout SEBELUM lebar di-override.
        widget.PanelBiasa selTglKunjungan = hdrCell("Tgl. Kunjungan", panelTglKunjungan, false);
        int tinggiAlamiTgl = selTglKunjungan.getPreferredSize().height;
        java.awt.Dimension dTgl = new java.awt.Dimension(314, tinggiAlamiTgl);
        selTglKunjungan.setPreferredSize(dTgl);
        selTglKunjungan.setMinimumSize(dTgl);
        g.gridy=0;
        g.gridx=0; grid.add(selTglKunjungan,g);
        g.gridx=1; grid.add(hdrCell("Cara Masuk",        CaraMasuk,         false),g);
        g.gridx=2; grid.add(hdrCell("Transportasi",      Transportasi,      false),g);
        g.gridx=3; grid.add(hdrCell("Alasan Keterlambatan", AlasanKedatangan,  false),g);
        g.gridx=4; g.insets=new java.awt.Insets(0,0,0,0);
        grid.add(hdrCell("Macam Kasus", selMacamKasus, true),g);
        // baris 2
        g.gridy=1; g.insets=new java.awt.Insets(10,0,0,14);
        g.gridx=0; grid.add(hdrCell("Dokter / Petugas IGD", selDokterPetugas, true),g);
        g.gridx=1; grid.add(hdrCell("Pengantar — Nama", PacsNamaPengantar, false),g);
        g.gridx=2; grid.add(hdrCell("Pengantar — Telp", PacsTelpPengantar, false),g);
        g.gridx=3; g.gridwidth=2; g.weightx=0.4; g.insets=new java.awt.Insets(10,0,0,0);
        grid.add(hdrCell("Keterangan", KeteranganKedatangan, false),g);

        // ===================== rakit FormInput =====================
        java.awt.GridBagConstraints f = new java.awt.GridBagConstraints();
        f.gridx=0; f.gridy=0; f.fill=java.awt.GridBagConstraints.BOTH;
        f.weightx=0; f.weighty=1.0;
        f.insets=new java.awt.Insets(0,0,0,16);
        java.awt.Dimension dk = new java.awt.Dimension(270,118);
        kartu.setMinimumSize(dk); kartu.setPreferredSize(dk);
        FormInput.add(kartu,f);
        f.gridx=1; f.weightx=1.0; f.insets=new java.awt.Insets(0,0,0,0);
        FormInput.add(grid,f);

        // Lebar dinaikkan dari 900 -> 1040 -> 1090 (kenaikan terakhir krn
        // combo jam panelTglKunjungan dilebarkan 46->62px, fix elipsis "...")
        // supaya kolom "Tgl. Kunjungan" (tanggal+3 kotak jam, >=306px) punya
        // ruang, tanpa memepetkan 4 kolom header lain yg lebarnya tetap.
        FormInput.setPreferredSize(new java.awt.Dimension(1090,142));
    }

    /** [Bagian C.12] Ambil inisial (maks 2 huruf) dari nama pasien utk avatar bulat. */
    private static String inisialDariNama(String nama){
        if(nama==null || nama.trim().isEmpty()){ return "?"; }
        String[] kata = nama.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<kata.length && sb.length()<2;i++){
            if(!kata[i].isEmpty()){ sb.append(Character.toUpperCase(kata[i].charAt(0))); }
        }
        return sb.length()==0 ? "?" : sb.toString();
    }

    /** [Bagian C.12] Komponen bulat kecil yang menggambar inisial nama pasien - tidak ada widget avatar siap pakai di src/widget/. */
    private static final class AvatarBulatPacs extends javax.swing.JComponent {
        private String inisial = "?";
        void setInisial(String s){ this.inisial = s; repaint(); }
        @Override protected void paintComponent(java.awt.Graphics g){
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(widget.ThemeConfig.ACCENT);
            g2.fillOval(0,0,getWidth()-1,getHeight()-1);
            g2.setColor(java.awt.Color.WHITE);
            g2.setFont(getFont());
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(inisial);
            g2.drawString(inisial, (getWidth()-tw)/2, (getHeight()+fm.getAscent())/2 - 2);
            g2.dispose();
        }
    }

    /** [Bagian B] Sinkronkan label kartu identitas dari TextBox tersembunyi. */
    private void sinkronKartuIdentitas(){
        if(lblKartuNama==null){ return; }
        String namaPasien = TPasien.getText().trim();
        lblKartuNama.setText(namaPasien.isEmpty()? "—" : namaPasien);
        if(avatarKartuPacs!=null){
            avatarKartuPacs.setInisial(inisialDariNama(namaPasien));
        }
        lblKartuRM.setText("No. RM  "+TNoRM.getText().trim());
        lblKartuRawat.setText("No. Rawat  "+TNoRw.getText().trim());
        String jkUmur = "";
        String rm = TNoRM.getText().trim();
        if(!rm.isEmpty()){
            try{
                String jk = Sequel.cariIsi("select jk from pasien where no_rkm_medis='"+rm.replaceAll("'","")+"'");
                String umur = Sequel.cariIsi("select TIMESTAMPDIFF(YEAR,tgl_lahir,CURDATE()) from pasien where no_rkm_medis='"+rm.replaceAll("'","")+"'");
                if(jk!=null && !jk.isEmpty()){
                    jkUmur = ("L".equalsIgnoreCase(jk)||"Laki-Laki".equalsIgnoreCase(jk)) ? "Laki-laki" : "Perempuan";
                }
                if(umur!=null && !umur.isEmpty()){ jkUmur = jkUmur.isEmpty()? umur+" Th" : jkUmur+" · "+umur+" Th"; }
            }catch(Exception e){ /* label identitas sifatnya tampilan saja */ }
        }
        lblKartuJK.setText(jkUmur);
    }

    /** [Bagian B] Satu sel grid header: label 11px TEXT_MUTED di ATAS field 30px. */
    private widget.PanelBiasa hdrCell(String label, java.awt.Component field, boolean wajib){
        widget.PanelBiasa sel = new widget.PanelBiasa();
        sel.setOpaque(false);
        sel.setLayout(new java.awt.BorderLayout(0,3));
        widget.Label l = new widget.Label();
        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        l.setForeground(widget.ThemeConfig.TEXT_MUTED);
        if(wajib){
            l.setText("<html>"+label+" <font color='#B3261E'>*</font></html>");
        }else{
            l.setText(label);
        }
        if(field instanceof javax.swing.JComponent){
            ((javax.swing.JComponent)field).setPreferredSize(new java.awt.Dimension(0,30));
        }
        sel.add(l, java.awt.BorderLayout.NORTH);
        sel.add(field, java.awt.BorderLayout.CENTER);
        return sel;
    }

    /**
     * [Bagian E] Sinkron kotak tampilan Tgl/Jam Kunjungan dari TanggalKunjungan
     * (GEN, sumber nilai asli) - satu arah saja. Dipanggil tiap kali
     * TanggalKunjungan berubah (baru/reset & pilih baris dari tabel).
     */
    private void sinkronPanelTglKunjungan(){
        if(panelTglKunjungan==null){ return; }
        String s = TanggalKunjungan.getSelectedItem()+"";
        try{
            java.util.Date d = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(s);
            panelTglKunjungan.setNilaiSql(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d));
        }catch(java.text.ParseException pe){
            panelTglKunjungan.setSekarang();
        }
    }

    /**
     * [Bagian B.4] Field lookup = [TextBox | tombol ⌕] berdampingan.
     * TextBox DIBIARKAN apa adanya (widget.TextBox = sudut tajam/kotak,
     * FlatRoundBorder TIDAK dipasang - lihat probe radius) supaya bentuknya
     * SAMA PERSIS dgn TextBox lain di form ini; tidak ada panel berborder
     * yg membungkusnya (dulu bikin sudut lookup beda sendiri dari yg lain).
     */
    private widget.PanelBiasa hdrLookup(widget.TextBox tf, widget.Button btn){
        widget.PanelBiasa w = new widget.PanelBiasa();
        w.setOpaque(false);
        w.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        w.setLayout(new java.awt.BorderLayout(4,0));
        w.add(tf, java.awt.BorderLayout.CENTER);
        w.add(btn, java.awt.BorderLayout.EAST);
        return w;
    }

    /**
     * [Bagian G] Action bar bawah (panelGlass8 - dipakai bersama SEMUA tab
     * form ini). Restyle warna/font + "Keluar" dipisah ke kanan + label
     * ringkasan pilihan. Ditata ulang dari method tangan (pola sama dgn
     * initHeaderTambahan/initPatientBanner) - blok GEN tidak disentuh.
     */
    private void initActionBar(){
        // Simpan: tanpa background warna (permintaan) - cukup teks tebal + label lebih jelas
        BtnSimpan.setText("Simpan Triase");
        BtnSimpan.setGlassColor(java.awt.Color.WHITE);
        BtnSimpan.setForeground(widget.ThemeConfig.TEXT);
        BtnSimpan.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        BtnSimpan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8,0xDB,0xD6)));
        BtnSimpan.setPreferredSize(new java.awt.Dimension(140,32));

        // sekunder: Baru, Ganti, Hapus, Cetak, Semua
        widget.Button[] sekunder = {BtnBatal, BtnEdit, BtnHapus, BtnPrint, BtnAll};
        for(widget.Button b : sekunder){
            b.setGlassColor(java.awt.Color.WHITE);
            b.setForeground(widget.ThemeConfig.TEXT);
            b.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8,0xDB,0xD6)));
            b.setPreferredSize(new java.awt.Dimension(96,32));
        }

        // Keluar: sisi kanan, teks danger-ish
        BtnKeluar.setGlassColor(java.awt.Color.WHITE);
        BtnKeluar.setForeground(new java.awt.Color(0x8A,0x4A,0x46));
        BtnKeluar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8,0xDB,0xD6)));
        BtnKeluar.setPreferredSize(new java.awt.Dimension(96,32));

        lblRingkasanPacs = new widget.Label();
        lblRingkasanPacs.setText("");
        lblRingkasanPacs.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRingkasanPacs.setForeground(widget.ThemeConfig.TEXT_FAINT);

        widget.PanelBiasa kiri = new widget.PanelBiasa();
        kiri.setOpaque(false);
        kiri.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 8));
        btnPrintPacs.setGlassColor(java.awt.Color.WHITE);
        btnPrintPacs.setForeground(widget.ThemeConfig.TEXT);
        btnPrintPacs.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xD8,0xDB,0xD6)));
        btnPrintPacs.setPreferredSize(new java.awt.Dimension(170,32));

        kiri.add(BtnSimpan); kiri.add(BtnBatal); kiri.add(BtnEdit);
        kiri.add(BtnHapus); kiri.add(BtnPrint); kiri.add(BtnAll);
        kiri.add(btnPrintPacs);

        widget.PanelBiasa kanan = new widget.PanelBiasa();
        kanan.setOpaque(false);
        kanan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 8));
        kanan.add(lblRingkasanPacs);
        kanan.add(BtnKeluar);

        panelGlass8.removeAll();
        panelGlass8.setLayout(new java.awt.BorderLayout());
        panelGlass8.setBorder(javax.swing.BorderFactory.createMatteBorder(1,0,0,0, widget.ThemeConfig.BORDER_SOFT));
        panelGlass8.add(kiri, java.awt.BorderLayout.CENTER);
        panelGlass8.add(kanan, java.awt.BorderLayout.EAST);
    }

    /** [Bagian G.4] "Level terpilih: PACS n · <keputusan>" di action bar. */
    private void refreshRingkasanPacs(){
        if(lblRingkasanPacs==null){ return; }
        String lvl = RbPacs1.isSelected()?"PACS 1":RbPacs2.isSelected()?"PACS 2":
                     RbPacs3.isSelected()?"PACS 3":RbPacs4.isSelected()?"PACS 4":null;
        String kep = RbPacsResusitasi.isSelected()?"Ruang Resusitasi":
                     RbPacsNonResusitasi.isSelected()?"Ruang Non Resusitasi":
                     RbPacsPoliKlinik.isSelected()?"Poli Klinik":
                     RbPacsDOA.isSelected()?"Death On Arrival":null;
        if(lvl==null && kep==null){ lblRingkasanPacs.setText(""); return; }
        lblRingkasanPacs.setText("Level terpilih: "+(lvl==null?"—":lvl)+(kep==null?"":" · "+kep));
    }

    /**
     * Membangun tab "PACS" secara terpisah dari initComponents() (blok
     * GEN-BEGIN/GEN-END) sesuai pola Khanza yang sudah ada (widget.* +
     * widget.Tanggal utk tanggal/jam, widget.Button utk buka DlgCariPegawai).
     * Dipanggil sekali dari constructor, setelah initComponents().
     */
    private void initPacsTab(){
        PanelPacs = new widget.PanelBiasa();
        PanelPacs.setLayout(new java.awt.BorderLayout());
        PanelPacs.setOpaque(true);
        PanelPacs.setBackground(widget.ThemeConfig.SURFACE);
        PanelPacs.setBorder(javax.swing.BorderFactory.createEmptyBorder());

        selSkorPacs = new widget.Label[6][4];
        kartuLevelPacs = new widget.PanelBiasa[4];
        kartuStripPacs = new widget.PanelBiasa[4];
        kartuJudulPacs = new widget.Label[4];
        kartuDescPacs  = new widget.Label[4];
        bungkusKeputusanPacs = new widget.PanelBiasa[4];

        // [Bagian C] Badan sub-tab = 3 kolom (Anamnesa 398 | Tanda Vital 398 |
        // Skor EWS & Keputusan grow). TitledBorder bersarang DIHAPUS - judul
        // kolom kini header strip (buatHeaderStrip). GridBagLayout meniru
        // MigLayout `[398!][398!][grow,fill]`.
        widget.PanelBiasa panelBadan = new widget.PanelBiasa();
        panelBadan.setOpaque(true);
        panelBadan.setBackground(widget.ThemeConfig.SURFACE);
        panelBadan.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        panelBadan.setLayout(new java.awt.GridBagLayout());

        // ========================= KOLOM 1: ANAMNESA =========================
        widget.PanelBiasa kolAnamnesa = buatKolomPacs(398,true);
        int rA = 0;
        rA = tambahIsiKolom(kolAnamnesa, rA, buatHeaderStrip("ANAMNESA"));

        PacsKeluhanUtama = new widget.TextArea();
        PacsKeluhanUtama.setBorder(javax.swing.BorderFactory.createEmptyBorder(1,1,1,1));
        PacsKeluhanUtama.setRows(5);
        widget.ScrollPane scrollKeluhanUtama = new widget.ScrollPane();
        scrollKeluhanUtama.setViewportView(PacsKeluhanUtama);
        scrollKeluhanUtama.setBorder(javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER));

        lblCounterKeluhanUtama = new widget.Label();
        lblCounterKeluhanUtama.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCounterKeluhanUtama.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 9));
        lblCounterKeluhanUtama.setForeground(widget.ThemeConfig.TEXT_FAINT);
        refreshCounterKeluhanUtama();
        PacsKeluhanUtama.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e){ refreshCounterKeluhanUtama(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e){ refreshCounterKeluhanUtama(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e){ refreshCounterKeluhanUtama(); }
        });

        widget.PanelBiasa panelKeluhanUtama = new widget.PanelBiasa();
        panelKeluhanUtama.setOpaque(false);
        panelKeluhanUtama.setLayout(new java.awt.BorderLayout(0,2));
        panelKeluhanUtama.add(scrollKeluhanUtama, java.awt.BorderLayout.CENTER);
        panelKeluhanUtama.add(lblCounterKeluhanUtama, java.awt.BorderLayout.SOUTH);
        rA = tambahIsiKolom(kolAnamnesa, rA, barisAreaPacs("Keluhan Utama", panelKeluhanUtama));

        // [Mekanisme Trauma] Hanya tampil kalau Kasus = Trauma (KdKasus != "004").
        // Diperbarui oleh perbaruiVisibilitasMekanismeTrauma(). Isi panel TIDAK
        // diubah - cuma pindah ke kolom Anamnesa.
        panelMekanismeTrauma = new widget.PanelBiasa();
        panelMekanismeTrauma.setOpaque(false);
        panelMekanismeTrauma.setLayout(new java.awt.GridBagLayout());
        int rowMt = 0;
        java.awt.GridBagConstraints cmt;

        CekBoxIsiTeks(CekKklTunggal = new widget.CekBox(),"KKL Tunggal");
        CekBoxIsiTeks(CekKklDenganLawan = new widget.CekBox(),"KKL dengan lawan");
        widget.PanelBiasa panelKkl = new widget.PanelBiasa();
        panelKkl.setOpaque(false);
        panelKkl.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        panelKkl.add(CekKklTunggal); panelKkl.add(CekKklDenganLawan);
        cmt = pacsConstraint(0,rowMt); cmt.gridwidth=2; panelMekanismeTrauma.add(panelKkl,cmt); rowMt++;

        // ukuranPacsFieldPendek() dipanggil eksplisit di tiap TextBox baris
        // Mekanisme Trauma - widget.TextBox diam-diam setSize(1,23) di
        // constructor-nya (gotcha baku proyek ini), tanpa setPreferredSize()
        // eksplisit field-nya menciut ~1px begitu GridBagLayout melayout ulang.
        TKklLawan = ukuranPacsFieldPendek(new widget.TextBox());
        cmt = pacsConstraint(0,rowMt); panelMekanismeTrauma.add(pacsLabelTeks("Lawan :"),cmt);
        cmt = pacsConstraint(1,rowMt); panelMekanismeTrauma.add(TKklLawan,cmt); rowMt++;

        TKklTempatKejadian = ukuranPacsFieldPendek(new widget.TextBox());
        cmt = pacsConstraint(0,rowMt); panelMekanismeTrauma.add(pacsLabelTeks("Tempat Kejadian :"),cmt);
        cmt = pacsConstraint(1,rowMt); panelMekanismeTrauma.add(TKklTempatKejadian,cmt); rowMt++;

        // Label+field baris ini SENGAJA ditumpuk (label di atas, bukan di
        // kolom 0 spt baris lain) - panelKklKejadian (widget tanggal+3 combo
        // jam, pref 310px) + label "Tanggal & Pukul Kejadian :" (pref 138px)
        // kalau dipaksa berdampingan di 2 kolom sempit ini (total <=~370px
        // tersedia) melebihi lebar yg ada, memepetkan kolom label SEMUA baris
        // lain di panel ini sampai terpotong "..." (kolom lebar dihitung dari
        // baris TERLEBAR). Ditumpuk = baris ini pakai lebar penuh sendiri,
        // kolom 0 baris lain kembali dihitung dari label terpanjang berikutnya
        // (jauh lebih pendek) - diverifikasi via render offscreen.
        panelKklKejadian = new RMPenilaianAwalKeperawatanIGD.PanelTanggalDanJam(false, 23);
        cmt = pacsConstraint(0,rowMt); cmt.gridwidth=2; panelMekanismeTrauma.add(pacsLabelTeks("Tanggal & Pukul Kejadian :"),cmt); rowMt++;
        cmt = pacsConstraint(0,rowMt); cmt.gridwidth=2; panelMekanismeTrauma.add(panelKklKejadian,cmt); rowMt++;

        rowMt = tambahBarisMekanismeTrauma(panelMekanismeTrauma,rowMt,"Jatuh dari Ketinggian",CekJatuhKetinggian = new widget.CekBox(),TJatuhKetinggianKet = ukuranPacsFieldPendek(new widget.TextBox()));
        rowMt = tambahBarisMekanismeTrauma(panelMekanismeTrauma,rowMt,"Luka Bakar",CekLukaBakar = new widget.CekBox(),TLukaBakarKet = ukuranPacsFieldPendek(new widget.TextBox()));
        rowMt = tambahBarisMekanismeTrauma(panelMekanismeTrauma,rowMt,"Trauma Fisik",CekTraumaFisik = new widget.CekBox(),TTraumaFisikKet = ukuranPacsFieldPendek(new widget.TextBox()));
        rowMt = tambahBarisMekanismeTrauma(panelMekanismeTrauma,rowMt,"Trauma Zat Kimia",CekTraumaZatKimia = new widget.CekBox(),TTraumaZatKimiaKet = ukuranPacsFieldPendek(new widget.TextBox()));
        rowMt = tambahBarisMekanismeTrauma(panelMekanismeTrauma,rowMt,"Trauma Zat Lainnya",CekTraumaZatLainnya = new widget.CekBox(),TTraumaZatLainnyaKet = ukuranPacsFieldPendek(new widget.TextBox()));
        tambahPengisiKolom(panelMekanismeTrauma);

        barisMekanismePacs = barisAreaPacsWajib("Mekanisme Trauma (khusus Kasus = Trauma)", panelMekanismeTrauma);
        rA = tambahIsiKolom(kolAnamnesa, rA, barisMekanismePacs);
        panelMekanismeTrauma.setVisible(false);
        barisMekanismePacs.setVisible(false);

        // [Bagian B.5] "Sudah Terpasang" dipindah dari header ke sini.
        widget.PanelBiasa panelTerpasang = new widget.PanelBiasa();
        panelTerpasang.setOpaque(false);
        panelTerpasang.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,6,0));
        panelTerpasang.add((java.awt.Component) PacsInfus.getClientProperty("kartuChip"));
        panelTerpasang.add((java.awt.Component) PacsNgt.getClientProperty("kartuChip"));
        panelTerpasang.add((java.awt.Component) PacsKateter.getClientProperty("kartuChip"));
        rA = tambahIsiKolom(kolAnamnesa, rA, barisAreaPacs("Sudah Terpasang", panelTerpasang));
        tutupKolomPacs(kolAnamnesa, rA);

        // ======================= KOLOM 2: TANDA VITAL =======================
        widget.PanelBiasa kolVital = buatKolomPacs(398,true);
        int rV = 0;
        rV = tambahIsiKolom(kolVital, rV, buatHeaderStrip("TANDA VITAL"));
        rV = tambahIsiKolom(kolVital, rV, barisFieldPacs("Tekanan Darah", ukuranPacsFieldPendek(PacsTensi = new widget.TextBox()), "mmHg"));
        rV = tambahIsiKolom(kolVital, rV, barisFieldPacs("Nadi",          ukuranPacsFieldPendek(PacsNadi = new widget.TextBox()), "/mnt"));
        rV = tambahIsiKolom(kolVital, rV, barisFieldPacs("Pernapasan",    ukuranPacsFieldPendek(PacsRespirasi = new widget.TextBox()), "/mnt"));
        rV = tambahIsiKolom(kolVital, rV, barisFieldPacs("Suhu",          ukuranPacsFieldPendek(PacsSuhu = new widget.TextBox()), "°C"));
        rV = tambahIsiKolom(kolVital, rV, barisFieldPacs("Saturasi O₂", ukuranPacsFieldPendek(PacsSaturasi = new widget.TextBox()), "%"));
        PacsNyeri = new widget.TextBox(); ukuranPacsFieldPendek(PacsNyeri);
        javax.swing.ButtonGroup bgNyeriPacs = new javax.swing.ButtonGroup();
        tglNyeriTidak = new javax.swing.JToggleButton("Tidak");
        tglNyeriYa = new javax.swing.JToggleButton("Ya");
        for(javax.swing.JToggleButton tg : new javax.swing.JToggleButton[]{tglNyeriTidak,tglNyeriYa}){
            tg.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
            tg.setFocusPainted(false);
            tg.setMargin(new java.awt.Insets(1,6,1,6));
            bgNyeriPacs.add(tg);
        }
        tglNyeriYa.setSelected(true);
        tglNyeriTidak.addActionListener((java.awt.event.ActionEvent e) -> {
            PacsNyeri.setText("0");
            PacsNyeri.setEnabled(false);
        });
        tglNyeriYa.addActionListener((java.awt.event.ActionEvent e) -> {
            PacsNyeri.setEnabled(true);
        });
        widget.PanelBiasa panelNyeri = new widget.PanelBiasa();
        panelNyeri.setOpaque(false);
        panelNyeri.setLayout(new java.awt.BorderLayout(4,0));
        panelNyeri.add(PacsNyeri, java.awt.BorderLayout.CENTER);
        widget.PanelBiasa togglePanelNyeri = new widget.PanelBiasa();
        togglePanelNyeri.setOpaque(false);
        togglePanelNyeri.setLayout(new java.awt.GridLayout(1,2,2,0));
        togglePanelNyeri.add(tglNyeriTidak); togglePanelNyeri.add(tglNyeriYa);
        panelNyeri.add(togglePanelNyeri, java.awt.BorderLayout.EAST);
        rV = tambahIsiKolom(kolVital, rV, barisFieldPacs("Skala Nyeri", panelNyeri, "/10"));

        PacsKesadaran = new widget.ComboBox();
        PacsKesadaran.addItem("Sadar Penuh");
        PacsKesadaran.addItem("Respon Suara");
        PacsKesadaran.addItem("Respon Nyeri");
        PacsKesadaran.addItem("Tidak Respon");
        PacsKesadaran.setVisible(false);
        rV = tambahIsiKolom(kolVital, rV, barisFieldPacs("Kesadaran (AVPU)", buatPanelAvpu(), ""));
        rV = tambahIsiKolom(kolVital, rV, barisFieldPacs("Berat Badan", ukuranPacsFieldPendek(PacsBeratBadan = new widget.TextBox()), "kg"));
        rV = tambahIsiKolom(kolVital, rV, barisFieldPacs("Tinggi Badan", ukuranPacsFieldPendek(PacsTinggiBadan = new widget.TextBox()), "cm"));
        rV = tambahIsiKolom(kolVital, rV, catatanKecilPacs("Skor 0-3 di kolom SKOR EWS & KEPUTUSAN dihitung OTOMATIS dari tanda vital di atas."));
        tutupKolomPacs(kolVital, rV);

        // [PACS] skor 0-3 dihitung OTOMATIS dari tanda vital (bukan dipilih
        // manual) - perilaku TIDAK berubah, hanya tampilannya (sel indikator).
        java.awt.event.FocusAdapter pemicuSkorOtomatis = new java.awt.event.FocusAdapter(){
            @Override
            public void focusLost(java.awt.event.FocusEvent e){
                hitungSkorOtomatisPacs();
            }
        };
        PacsTensi.addFocusListener(pemicuSkorOtomatis);
        PacsNadi.addFocusListener(pemicuSkorOtomatis);
        PacsRespirasi.addFocusListener(pemicuSkorOtomatis);
        PacsSuhu.addFocusListener(pemicuSkorOtomatis);
        PacsSaturasi.addFocusListener(pemicuSkorOtomatis);
        PacsKesadaran.addItemListener((java.awt.event.ItemEvent e) -> hitungSkorOtomatisPacs());

        // [Bagian C.6] Validasi rentang tanda vital - INDIKATOR VISUAL SAJA.
        pasangValidasiRentangPacs(PacsNadi, 60, 100);
        pasangValidasiRentangPacs(PacsRespirasi, 12, 20);
        pasangValidasiRentangPacs(PacsSuhu, 36, 37.5);
        pasangValidasiRentangPacs(PacsSaturasi, 95, 100);
        pasangValidasiRentangPacs(PacsNyeri, 0, 3);

        // ================ KOLOM 3: SKOR EWS & KEPUTUSAN ================
        widget.PanelBiasa kolEws = buatKolomPacs(0,false);
        int rE = 0;
        rE = tambahIsiKolom(kolEws, rE, buatHeaderStrip("SKOR EWS & KEPUTUSAN"));
        rE = tambahIsiKolom(kolEws, rE, barisSkorHeaderPacs());
        String[] namaParam = {"Tingkat Kesadaran","Tekanan Darah Sistolik","Frekuensi Nadi","Frekuensi Respirasi","Temperatur","Saturasi Oksigen"};
        for(int i=0;i<6;i++){ rE = tambahIsiKolom(kolEws, rE, barisSkorPacs(i, namaParam[i])); }
        rE = tambahIsiKolom(kolEws, rE, catatanKecilPacs("Sel abu-abu berarti skor itu tidak berlaku untuk parameter tsb pada formulir asli PACS-1."));

        // Combo skor lama TETAP dibuat (terikat simpan/tampil) - disembunyikan,
        // jadi sumber nilai utk sel indikator 0-3.
        CmbSkorKesadaranPacs = buatComboSkor();
        CmbSkorTDPacs = buatComboSkor();
        CmbSkorNadiPacs = buatComboSkor();
        CmbSkorRespirasiPacs = buatComboSkor();
        CmbSkorTemperaturPacs = buatComboSkor();
        CmbSkorSaturasiPacs = buatComboSkor();
        widget.ComboBox[] semuaCmbSkor = {CmbSkorKesadaranPacs,CmbSkorTDPacs,CmbSkorNadiPacs,CmbSkorRespirasiPacs,CmbSkorTemperaturPacs,CmbSkorSaturasiPacs};
        java.awt.event.ItemListener hitungSkor = (java.awt.event.ItemEvent e) -> hitungTotalSkorPacs();
        for(widget.ComboBox cmb : semuaCmbSkor){ cmb.setVisible(false); cmb.addItemListener(hitungSkor); }

        LblTotalSkorPacs = new widget.Label();
        LblTotalSkorPacs.setText("0");
        rE = tambahIsiKolom(kolEws, rE, buatBannerTotalPacs());

        rE = tambahIsiKolom(kolEws, rE, barisSaranLevelPacs());
        rE = tambahIsiKolom(kolEws, rE, buatBarisKartuLevelPacs());

        PacsCatatanKhusus = new widget.TextArea();
        PacsCatatanKhusus.setBorder(javax.swing.BorderFactory.createEmptyBorder(1,1,1,1));
        PacsCatatanKhusus.setRows(4);
        PacsCatatanKhusus.setPlaceholder("Catatan tambahan untuk tim penerima...");
        widget.ScrollPane scrollCatatanKhusus = new widget.ScrollPane();
        scrollCatatanKhusus.setViewportView(PacsCatatanKhusus);
        scrollCatatanKhusus.setBorder(javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER));

        rE = tambahIsiKolom(kolEws, rE, buatHeaderStrip("KEPUTUSAN PENEMPATAN"));
        rE = tambahIsiKolom(kolEws, rE, buatBarisKeputusanPacs(scrollCatatanKhusus));

        // [Bagian D] Pukul Keputusan + tombol "Sekarang" - pojok bawah kolom
        // SKOR EWS & KEPUTUSAN, persis posisi di mockup.
        DTPKeputusanPukulPacs = new RMPenilaianAwalKeperawatanIGD.PanelJamSaja(false, 23);
        btnKeputusanPukulSekarangPacs = new widget.Button();
        btnKeputusanPukulSekarangPacs.setText("Sekarang");
        btnKeputusanPukulSekarangPacs.addActionListener((java.awt.event.ActionEvent e) -> DTPKeputusanPukulPacs.setSekarang());
        widget.PanelBiasa panelKeputusanPukul = new widget.PanelBiasa();
        panelKeputusanPukul.setOpaque(false);
        panelKeputusanPukul.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,6,0));
        panelKeputusanPukul.add(DTPKeputusanPukulPacs);
        panelKeputusanPukul.add(btnKeputusanPukulSekarangPacs);
        rE = tambahIsiKolom(kolEws, rE, barisFieldPacs("Pukul Keputusan", panelKeputusanPukul, ""));

        tutupKolomPacs(kolEws, rE);

        // (Tanggal/Jam Triase / tanggal_triase TETAP diturunkan dari
        // TanggalKunjungan saat simpan. Petugas Triase dipakai bersama tab Sekunder.)

        // ===== Rakit 3 kolom ke panelBadan =====
        java.awt.GridBagConstraints cb = new java.awt.GridBagConstraints();
        cb.gridy = 0; cb.weighty = 1.0;
        cb.fill = java.awt.GridBagConstraints.BOTH;
        cb.anchor = java.awt.GridBagConstraints.NORTHWEST;
        cb.gridx = 0; cb.weightx = 0; panelBadan.add(kolAnamnesa, cb);
        cb.gridx = 1; cb.weightx = 0; panelBadan.add(kolVital, cb);
        cb.gridx = 2; cb.weightx = 1.0; panelBadan.add(kolEws, cb);

        PanelPacs.add(panelBadan, java.awt.BorderLayout.CENTER);
        // (btnPrintPacs sekarang dibuat di initHeaderTambahan() dan ditaruh di
        // action bar bawah bersama Baru/Hapus/Ganti/Semua - lihat initActionBar())

        // Listener styling Level & Keputusan + ringkasan action bar.
        java.awt.event.ItemListener liLevel = (java.awt.event.ItemEvent e) -> { refreshKartuLevelPacs(); refreshRingkasanPacs(); };
        RbPacs1.addItemListener(liLevel); RbPacs2.addItemListener(liLevel);
        RbPacs3.addItemListener(liLevel); RbPacs4.addItemListener(liLevel);
        java.awt.event.ItemListener liKeputusan = (java.awt.event.ItemEvent e) -> { refreshBungkusKeputusanPacs(); refreshRingkasanPacs(); };
        RbPacsResusitasi.addItemListener(liKeputusan); RbPacsNonResusitasi.addItemListener(liKeputusan);
        RbPacsPoliKlinik.addItemListener(liKeputusan); RbPacsDOA.addItemListener(liKeputusan);

        // [PACS] Dibungkus widget.ScrollPane dengan ukuran preferred TETAP
        // (bukan mengikuti tinggi asli kontennya), persis pola yang sudah
        // dipakai tab Primer/Sekunder (ScrollTriase, ScrollHTML, dst.) - supaya
        // TabTriase TIDAK melaporkan ukuran preferred raksasa ke layout di atasnya
        // (yang bisa memaksa seluruh dialog/tab Primer-Sekunder ikut menyusut/bergeser).
        // Fase 3: lebar dinaikkan (960->1250) utk menampung layout dua-kolom;
        // tinggi TETAP 420 (konten per kolom kini jauh lebih pendek drpd versi
        // satu-kolom lama, jadi scroll vertikal berkurang signifikan).
        // Bugfix lanjutan: 1250 ternyata KELEBARAN drpd lebar asli kedua kolom
        // (weightx=0.5 di PanelPacs cuma membagi 2 lebar yg TERSEDIA - kalau
        // yg tersedia jauh lebih besar dari kebutuhan konten, sisanya jadi
        // area kosong menganga di kanan viewport scrollPacs, dilaporkan user).
        // Diturunkan ke 1000 - lebih dekat ke kebutuhan nyata kedua kolom
        // (kiri ~450px + kanan ~350px + spacing/border), jadi tidak ada lagi
        // celah kosong lebar tak wajar, sekaligus tetap muat dua kolom penuh.
        //
        // [BUGFIX AKAR MASALAH] Menurunkan angka di setPreferredSize() TIDAK
        // cukup - itu cuma mengecilkan apa yg scrollPacs LAPORKAN sbg
        // kebutuhannya ke TabTriase, TAPI javax.swing.JTabbedPane SELALU
        // merentangkan komponen tab yg sedang aktif memenuhi SELURUH lebar
        // area tab yg tersedia (brp pun lebar dialog sungguhan - bisa lebih
        // lebar dari 1000px kalau tab lain/dialog di-resize besar), TANPA
        // peduli preferredSize scrollPacs. Begitu scrollPacs sendiri
        // direntangkan lebih lebar dari isinya (PanelPacs, dua kolom), sisa
        // lebarnya numpuk sbg area kosong di KANAN (JViewport default
        // menaruh view rata kiri-atas) - inilah akar "tidak di tengah".
        // Fix: viewport TIDAK lagi langsung menampilkan PanelPacs, tapi
        // dibungkus wrapperPacs (implements Scrollable,
        // getScrollableTracksViewportWidth()=true) - ini memaksa JViewport
        // merentangkan wrapperPacs SAMA LEBAR dgn viewport sungguhan (brp pun
        // itu), lalu GridBagLayout wrapperPacs (1 anak, weightx=0 default -
        // constraint kosong) men-TENGAH-KAN PanelPacs di dalamnya - PanelPacs
        // sendiri TETAP di lebar aslinya (dua kolom TIDAK ikut melebar),
        // cuma posisinya sekarang center, bukan rata kiri.
        //
        // [REVISI, dikonfirmasi via screenshot user] "Center" yg dimaksud
        // TERNYATA bukan "PanelPacs tetap sempit & mengambang di tengah
        // dgn margin kosong besar di kiri-kanan" (hasil dari constraint
        // kosong di atas) - user menandai margin kosong itu sbg masalah
        // (screenshot dgn tanda silang X), dan minta kedua KARTU (kiri/
        // kanan) yg MELEBAR mengisi ruang spt referensi screenshot lain,
        // SEMENTARA kolom inputan (Nadi/Suhu/dll) di dalamnya tetap pendek
        // spt semula. Solusinya BUKAN mengecilkan margin, tapi memberi
        // PanelPacs fill=HORIZONTAL+weightx=1.0 di sini (bukan lagi
        // constraint kosong) - PanelPacs (& panelPacsKiri/panelPacsKanan,
        // weightx=0.5 masing2 dari constraint cKolom di atas) ikut
        // meregang mengisi SELURUH lebar wrapperPacs. Kotak input TETAP
        // pendek krn filler tambahPengisiKolom() (gridx=2, weightx=1.0) di
        // masing2 kolom yg menyerap kelebihan lebar itu, BUKAN field-nya -
        // jadi ruang ekstra jatuh sbg blank space DI DALAM kartu (kanan
        // field, sebelum tepi kartu), bukan lagi di LUAR kartu. Diverifikasi
        // via render offscreen sebelum diterapkan di sini.
        ViewportTengahPacs wrapperPacs = new ViewportTengahPacs();
        java.awt.GridBagConstraints cWrapperPacs = new java.awt.GridBagConstraints();
        cWrapperPacs.fill = java.awt.GridBagConstraints.BOTH;
        cWrapperPacs.weightx = 1.0;
        cWrapperPacs.weighty = 1.0;
        cWrapperPacs.anchor = java.awt.GridBagConstraints.NORTHWEST;
        wrapperPacs.add(PanelPacs, cWrapperPacs);

        widget.ScrollPane scrollPacs = new widget.ScrollPane();
        scrollPacs.setViewportView(wrapperPacs);
        // [BUGFIX] "Keluhan Utama mengecil saat Mekanisme Trauma muncul":
        // kolAnamnesa/kolVital dipatok weightx=0 (lebar TETAP 398px, lihat
        // buatKolomPacs()) - GridBagLayout panelBadan cuma "aman" selama
        // total lebar yg tersedia >= 398+398+kebutuhan kolEws (sengaja
        // dipepetkan pas-pasan, lihat catatan lebar 1000->1200 di atas).
        // Kebijakan default scrollbar vertikal ScrollPane ini AS_NEEDED
        // (widget.ModernStyle.pasang()) - begitu perbaruiVisibilitasMekanismeTrauma()
        // menampilkan panelMekanismeTrauma, tinggi konten panelBadan
        // bertambah, scrollbar vertikal MUNCUL, viewport kehilangan ~15-17px
        // lebar mendadak, dan GridBagLayout (sudah pas-pasan) ikut MEMEPET
        // SEMUA kolom - inilah sebab kolom Anamnesa (& label Mekanisme
        // Trauma di dalamnya, spt "Lawan :"/"Tempat Kejadian :") mengecil
        // bersamaan, bukan cuma field yg baru muncul. Dipatok ALWAYS supaya
        // lebar viewport KONSTAN baik sebelum maupun sesudah panel trauma
        // tampil - lebar preferred dinaikkan sedikit (1200->1220) utk
        // mengompensasi jatah scrollbar yg sekarang permanen.
        scrollPacs.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        // Fase 5: layout 3 kolom - target muat 1 layar 1600x900 tanpa scroll
        // vertikal. Lebar preferred dinaikkan (1000->1200->1220), tinggi disesuaikan.
        scrollPacs.setPreferredSize(new java.awt.Dimension(1220,560));
        TabTriase.addTab("PACS", scrollPacs);

        perbaruiVisibilitasMekanismeTrauma();
        refreshAvpuPacs();
        refreshIndikatorSkorPacs();
        refreshBannerTotalPacs();
        refreshSaranLevelPacs();
        refreshKartuLevelPacs();
        refreshBungkusKeputusanPacs();
        refreshRingkasanPacs();
    }

    // =====================================================================
    // [Bagian C/D/E/F] Helper tampilan sub-tab PACS (UI-only).
    // =====================================================================

    /** Kolom badan PACS. lebar>0 -> lebar tetap (strut tak-terlihat); pemisahKanan -> garis 1px di sisi kanan. */
    private widget.PanelBiasa buatKolomPacs(int lebar, boolean pemisahKanan){
        widget.PanelBiasa k = new widget.PanelBiasa();
        k.setOpaque(true);
        k.setBackground(widget.ThemeConfig.SURFACE);
        k.setLayout(new java.awt.GridBagLayout());
        k.setBorder(pemisahKanan
            ? javax.swing.BorderFactory.createMatteBorder(0,0,0,1, widget.ThemeConfig.BORDER_SOFT)
            : javax.swing.BorderFactory.createEmptyBorder());
        if(lebar>0){
            k.setMinimumSize(new java.awt.Dimension(lebar,10));
            java.awt.GridBagConstraints cs = new java.awt.GridBagConstraints();
            cs.gridx=0; cs.gridy=900; cs.fill=java.awt.GridBagConstraints.HORIZONTAL;
            k.add(javax.swing.Box.createHorizontalStrut(lebar), cs);
        }
        return k;
    }

    /** Tambah 1 komponen sebagai baris penuh kolom (rata atas-kiri, lebar penuh). */
    private int tambahIsiKolom(widget.PanelBiasa kolom, int row, java.awt.Component komp){
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx=0; c.gridy=row; c.gridwidth=java.awt.GridBagConstraints.REMAINDER;
        c.weightx=1.0; c.fill=java.awt.GridBagConstraints.HORIZONTAL;
        c.anchor=java.awt.GridBagConstraints.NORTHWEST;
        kolom.add(komp,c);
        return row+1;
    }

    /** Baris pengisi weighty=1 di bawah supaya isi kolom menempel ke atas. */
    private void tutupKolomPacs(widget.PanelBiasa kolom, int row){
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx=0; c.gridy=row; c.weighty=1.0; c.fill=java.awt.GridBagConstraints.BOTH;
        kolom.add(javax.swing.Box.createGlue(), c);
    }

    /** [Bagian C.2] Judul kolom = strip 32px SURFACE_ALT + garis bawah + label bold uppercase. */
    private widget.PanelBiasa buatHeaderStrip(String judul){
        widget.PanelBiasa s = new widget.PanelBiasa();
        s.setOpaque(true);
        s.setBackground(widget.ThemeConfig.SURFACE_ALT);
        s.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0,0,1,0, widget.ThemeConfig.BORDER_SOFT),
            javax.swing.BorderFactory.createEmptyBorder(0,10,0,10)));
        s.setLayout(new java.awt.BorderLayout());
        s.setPreferredSize(new java.awt.Dimension(10,32));
        // GridBagLayout kolom (weighty=0 utk baris ini) bisa menyusutkan baris
        // ini di bawah preferred begitu baris Mekanisme Trauma yg semula
        // tersembunyi berubah tampil - walau total tinggi kolom masih cukup -
        // krn widget.PanelBiasa (JPanel polos) tanpa minimumSize eksplisit
        // jatuh ke minimum BorderLayout yg jauh lebih kecil dari 32px ini.
        s.setMinimumSize(new java.awt.Dimension(10,32));
        widget.Label l = new widget.Label();
        l.setText(judul.toUpperCase());
        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        l.setForeground(widget.ThemeConfig.TEXT_MUTED);
        s.add(l, java.awt.BorderLayout.CENTER);
        return s;
    }

    /** Baris "label kecil di atas + isi lebar penuh" (TextArea / panel checkbox). */
    private widget.PanelBiasa barisAreaPacs(String label, java.awt.Component isi){
        // getMinimumSize() sengaja di-override = getPreferredSize(): tanpa ini,
        // GridBagLayout kolom (weighty=0 utk baris ini) bisa menyusutkan baris
        // ini (mis. Keluhan Utama) sampai separuh tinggi begitu baris lain di
        // kolom yg sama (Mekanisme Trauma) berubah dari tersembunyi jadi
        // tampil - walau total tinggi kolom masih cukup utk semua baris di
        // preferred-nya masing-masing. JPanel polos tanpa minimumSize eksplisit
        // jatuh ke minimum BorderLayout yg jauh lebih kecil dari isinya.
        widget.PanelBiasa p = new widget.PanelBiasa(){
            @Override
            public java.awt.Dimension getMinimumSize(){
                return getPreferredSize();
            }
        };
        p.setOpaque(false);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(6,10,2,10));
        p.setLayout(new java.awt.BorderLayout(0,3));
        widget.Label l = new widget.Label();
        l.setText(label);
        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        l.setForeground(widget.ThemeConfig.TEXT_MUTED);
        p.add(l, java.awt.BorderLayout.NORTH);
        p.add(isi, java.awt.BorderLayout.CENTER);
        return p;
    }

    /** [Bagian B.7] Sama seperti barisAreaPacs, tapi label diberi badge "WAJIB DIISI" (mis. Mekanisme Trauma). */
    private widget.PanelBiasa barisAreaPacsWajib(String label, java.awt.Component isi){
        // Lihat catatan getMinimumSize() di barisAreaPacs() - baris ini rentan
        // bug yg sama (baris lain di kolAnamnesa bisa ikut menyusut) kalau
        // minimumSize tetap dibiarkan ke default JPanel/BorderLayout yg kecil.
        widget.PanelBiasa p = new widget.PanelBiasa(){
            @Override
            public java.awt.Dimension getMinimumSize(){
                return getPreferredSize();
            }
        };
        p.setOpaque(false);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(6,10,2,10));
        p.setLayout(new java.awt.BorderLayout(0,3));

        widget.PanelBiasa judul = new widget.PanelBiasa();
        judul.setOpaque(false);
        judul.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,6,0));
        widget.Label l = new widget.Label();
        l.setText(label);
        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        l.setForeground(widget.ThemeConfig.TEXT_MUTED);
        widget.Label badge = new widget.Label();
        badge.setText("  WAJIB DIISI  ");
        badge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        badge.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 9));
        badge.setOpaque(true);
        badge.setBackground(widget.ThemeConfig.WARN_BG);
        badge.setForeground(widget.ThemeConfig.WARN);
        judul.add(l);
        judul.add(badge);

        p.add(judul, java.awt.BorderLayout.NORTH);
        p.add(isi, java.awt.BorderLayout.CENTER);
        return p;
    }

    /** [Bagian C.4] Baris: label kanan 118px | field grow | satuan mono 52px TEXT_FAINT. */
    private widget.PanelBiasa barisFieldPacs(String label, java.awt.Component field, String satuan){
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(false);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(4,10,4,10));
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridy=0; c.anchor=java.awt.GridBagConstraints.WEST;

        widget.Label l = new widget.Label();
        l.setText(label);
        l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        java.awt.Dimension dl = new java.awt.Dimension(118,24);
        l.setPreferredSize(dl); l.setMinimumSize(dl);
        c.gridx=0; c.weightx=0; c.fill=java.awt.GridBagConstraints.NONE;
        c.insets=new java.awt.Insets(0,0,0,8);
        p.add(l,c);

        c.gridx=1; c.weightx=1.0; c.fill=java.awt.GridBagConstraints.HORIZONTAL;
        c.insets=new java.awt.Insets(0,0,0,0);
        p.add(field,c);

        widget.Label u = new widget.Label();
        u.setText(satuan==null?"":satuan);
        u.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        u.setForeground(widget.ThemeConfig.TEXT_FAINT);
        u.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 11));
        java.awt.Dimension du = new java.awt.Dimension(52,24);
        u.setPreferredSize(du); u.setMinimumSize(du);
        c.gridx=2; c.weightx=0; c.fill=java.awt.GridBagConstraints.NONE;
        c.insets=new java.awt.Insets(0,6,0,0);
        p.add(u,c);
        return p;
    }

    /**
     * [Bagian C.5] 4 JToggleButton Kesadaran (AVPU) -> disinkron ke ComboBox
     * PacsKesadaran (tersembunyi). Disusun VERTIKAL (bukan sejajar horizontal)
     * dengan label kata penuh + skor di ujung kanan, sesuai mockup. Skor yang
     * ditampilkan HARUS ikut PacsScoring.skorKesadaran() (Sadar Penuh=0, tiga
     * lainnya=3 - skor 1/2 memang kosong di formulir, jangan menebak skala baru).
     */
    private widget.PanelBiasa buatPanelAvpu(){
        bgAvpuPacs = new javax.swing.ButtonGroup();
        tblAvpuA = new javax.swing.JToggleButton();
        tblAvpuV = new javax.swing.JToggleButton();
        tblAvpuP = new javax.swing.JToggleButton();
        tblAvpuU = new javax.swing.JToggleButton();
        final javax.swing.JToggleButton[] tg = {tblAvpuA,tblAvpuV,tblAvpuP,tblAvpuU};
        final String[] namaAvpu = {"Sadar Penuh","Respon Suara","Respon Nyeri","Tidak Respon"};
        // Sesuai mockup: daftar vertikal, tiap baris kartu sendiri dgn border
        // sendiri (bukan satu panel besar berbingkai) - lihat refreshAvpuPacs().
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(false);
        p.setLayout(new java.awt.GridLayout(4,1,0,5));
        for(int i=0;i<4;i++){
            final int idx = i;
            int skor = PacsScoring.skorKesadaran(namaAvpu[i]);
            tg[i].putClientProperty("namaAvpu", namaAvpu[i]);
            tg[i].putClientProperty("skorAvpu", skor);
            tg[i].setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            tg[i].setFocusPainted(false);
            tg[i].setBorderPainted(false);
            // contentAreaFilled=false + opaque=false: FlatLaf punya overlay
            // abu-abu tersendiri utk state "selected" toggle button yang tidak
            // bisa ditimpa lewat setBackground() saja (kebukti lewat Robot -
            // pita abu muncul di sisi kanan tombol terpilih walau background
            // sudah WHITE). Solusinya: background/border kartu dipindah ke
            // panel pembungkus polos (yg tidak punya cat "selected" LAF apa
            // pun), tombolnya sendiri jadi transparan, cuma menyumbang teks.
            tg[i].setOpaque(false);
            tg[i].setContentAreaFilled(false);
            widget.PanelBiasa kartu = new widget.PanelBiasa();
            kartu.setOpaque(true);
            kartu.setBackground(java.awt.Color.WHITE);
            kartu.setLayout(new java.awt.BorderLayout());
            kartu.add(tg[i], java.awt.BorderLayout.CENTER);
            tg[i].putClientProperty("kartuAvpu", kartu);
            bgAvpuPacs.add(tg[i]);
            p.add(kartu);
            tg[i].addActionListener((java.awt.event.ActionEvent e) -> {
                PacsKesadaran.setSelectedIndex(idx);
                refreshAvpuPacs();
            });
        }
        return p;
    }

    /**
     * Gaya per baris AVPU sesuai mockup: terpilih = border teal 2px, bulatan
     * radio terisi, teks tebal teal, skor jadi badge oranye teks putih.
     * Tidak terpilih = border abu tipis, bulatan radio kosong, teks & skor abu polos.
     * Background kartu SELALU putih di kedua kondisi (cuma border/teks/badge yg beda).
     */
    private void refreshAvpuPacs(){
        if(tblAvpuA==null){ return; }
        javax.swing.JToggleButton[] tg = {tblAvpuA,tblAvpuV,tblAvpuP,tblAvpuU};
        String hexTerpilih = hexWarna(WARNA_TERPILIH_PACS);
        String hexAbu = hexWarna(widget.ThemeConfig.TEXT_MUTED);
        String hexBadge = hexWarna(widget.ThemeConfig.WARN);
        for(javax.swing.JToggleButton b : tg){
            String nama = (String) b.getClientProperty("namaAvpu");
            Object skorObj = b.getClientProperty("skorAvpu");
            boolean pilih = b.isSelected();
            String warnaTeks = pilih ? hexTerpilih : hexAbu;
            String glyph = pilih ? "&#9679;" : "&#9675;";
            String labelHtml = pilih ? "<b><font color='"+warnaTeks+"'>"+nama+"</font></b>" : "<font color='"+warnaTeks+"'>"+nama+"</font>";
            String tdSkor = pilih
                    ? "<td width='34' align='center' bgcolor='"+hexBadge+"'><font color='#FFFFFF'><b>"+skorObj+"</b></font></td>"
                    : "<td width='34' align='center'><font color='"+hexAbu+"'>"+skorObj+"</font></td>";
            b.setText("<html><table width='100%' cellpadding='3' cellspacing='0' border='0'><tr>"
                    +"<td width='20' align='left'><font color='"+warnaTeks+"' size='+1'>"+glyph+"</font></td>"
                    +"<td align='left'>"+labelHtml+"</td>"
                    +tdSkor
                    +"</tr></table></html>");
            javax.swing.JComponent kartu = (javax.swing.JComponent) b.getClientProperty("kartuAvpu");
            kartu.setBorder(pilih
                    ? javax.swing.BorderFactory.createLineBorder(WARNA_TERPILIH_PACS, 2)
                    : javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER, 1));
        }
    }

    /** Setelah PacsKesadaran diisi dari DB, nyalakan toggle AVPU yang sesuai. */
    private void syncAvpuDariCombo(){
        if(tblAvpuA==null){ return; }
        int i = PacsKesadaran.getSelectedIndex();
        javax.swing.JToggleButton[] tg = {tblAvpuA,tblAvpuV,tblAvpuP,tblAvpuU};
        if(i>=0 && i<4){ tg[i].setSelected(true); } else { bgAvpuPacs.clearSelection(); }
        refreshAvpuPacs();
    }

    /** [Bagian C.6] DocumentListener: di luar rentang -> warna WARN; dalam rentang/kosong -> normal. TIDAK memblokir simpan. */
    private void pasangValidasiRentangPacs(final widget.TextBox tb, final double min, final double max){
        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener(){
            private void cek(){
                String s = tb.getText().trim().replace(",",".");
                boolean luar = false;
                if(!s.isEmpty()){
                    try{ double v = Double.parseDouble(s); luar = (v<min || v>max); }
                    catch(NumberFormatException ex){ luar = false; }
                }
                if(luar){
                    tb.setBackground(widget.ThemeConfig.WARN_BG);
                    tb.setForeground(widget.ThemeConfig.WARN);
                    tb.putClientProperty("JComponent.outline", new Object[]{ widget.ThemeConfig.WARN });
                }else{
                    tb.setBackground(java.awt.Color.WHITE);
                    tb.setForeground(widget.ThemeConfig.TEXT);
                    tb.putClientProperty("JComponent.outline", null);
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e){ cek(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e){ cek(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e){ cek(); }
        };
        tb.getDocument().addDocumentListener(dl);
    }

    /** [Bagian B.10] Gaya chip "sudah terpasang" - opaque, background putih/ACCENT saat terpilih (pola visual sama dengan buatPanelAvpu()). */
    /**
     * [Bagian E] Chip "Sudah Terpasang". Sama seperti AVPU (buatPanelAvpu()):
     * FlatLaf mengecat overlay "selected" sendiri di atas JToggleButton yang
     * opaque+contentAreaFilled, menimpa setBackground()/setForeground() kustom
     * (kebukti lewat Robot - background jadi cyan LAF, bukan WARNA_TERPILIH_PACS,
     * teks tetap hitam bukan putih). Solusinya sama: tombolnya ditransparankan,
     * background/border sungguhan dipindah ke panel kartu pembungkus polos.
     * Kartu-nya diambil belakangan lewat client property "kartuChip" karena
     * tombol dibuat di initHeaderTambahan() tapi ditata-letak di initPacsTab().
     */
    private void gayakanChipPacs(javax.swing.JToggleButton tg){
        String label = tg.getText();
        tg.putClientProperty("labelChip", label);
        tg.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        tg.setFocusPainted(false);
        tg.setBorderPainted(false);
        tg.setOpaque(false);
        tg.setContentAreaFilled(false);

        // JPanel biasa, BUKAN widget.PanelBiasa: PanelBiasa.paintComponent()
        // selalu menimpa dirinya dengan gradasi warnaAtas/warnaBawah milik
        // sendiri (default PUTIH-PUTIH) di atas apa pun yg diisi lewat
        // setBackground() - kebukti lewat Robot, kartu terpilih tetap putih
        // walau setBackground(WARNA_TERPILIH_PACS) sudah dipanggil. JPanel
        // polos menghormati setBackground() apa adanya.
        javax.swing.JPanel kartu = new javax.swing.JPanel();
        kartu.setOpaque(true);
        kartu.setLayout(new java.awt.BorderLayout());
        kartu.add(tg, java.awt.BorderLayout.CENTER);
        tg.putClientProperty("kartuChip", kartu);

        tg.addItemListener((java.awt.event.ItemEvent e) -> refreshChipPacs(tg));
        refreshChipPacs(tg);
    }

    /** [Bagian E] Gambar ulang warna kartu & teks chip sesuai status isSelected(). */
    private void refreshChipPacs(javax.swing.JToggleButton tg){
        String label = (String) tg.getClientProperty("labelChip");
        javax.swing.JComponent kartu = (javax.swing.JComponent) tg.getClientProperty("kartuChip");
        boolean pilih = tg.isSelected();
        java.awt.Color garis = pilih ? WARNA_TERPILIH_PACS : widget.ThemeConfig.BORDER;
        kartu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(garis),
                javax.swing.BorderFactory.createEmptyBorder(3,10,3,10)));
        if(pilih){
            kartu.setBackground(WARNA_TERPILIH_PACS);
            tg.setText("<html><b><font color='#FFFFFF'>"+label+"</font></b></html>");
        }else{
            kartu.setBackground(java.awt.Color.WHITE);
            tg.setText("<html><font color='"+hexWarna(widget.ThemeConfig.TEXT)+"'>"+label+"</font></html>");
        }
    }

    /** [Bagian B.8] Refresh label "N/400" mengikuti isi PacsKeluhanUtama (batas kolom keluhan_utama VARCHAR(400)). */
    private void refreshCounterKeluhanUtama(){
        if(lblCounterKeluhanUtama==null || PacsKeluhanUtama==null){ return; }
        int n = PacsKeluhanUtama.getText().length();
        lblCounterKeluhanUtama.setText(n+"/400");
        lblCounterKeluhanUtama.setForeground(n>400 ? widget.ThemeConfig.WARN : widget.ThemeConfig.TEXT_FAINT);
    }

    /** [A.4/A.5] Catatan kecil statis (satu baris, opaque, font kecil) - dipakai utk penjelasan tambahan di kolom PACS. */
    private widget.PanelBiasa catatanKecilPacs(String teks){
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(true);
        p.setBackground(widget.ThemeConfig.SURFACE_ALT);
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(1,0,0,0, widget.ThemeConfig.BORDER_SOFT),
                javax.swing.BorderFactory.createEmptyBorder(6,10,6,10)));
        p.setLayout(new java.awt.BorderLayout());
        widget.Label lbl = new widget.Label();
        lbl.setText(teks);
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 9));
        lbl.setForeground(widget.ThemeConfig.TEXT_FAINT);
        p.add(lbl, java.awt.BorderLayout.CENTER);
        return p;
    }

    /** [Bagian D] Baris judul tabel skor: "PARAMETER" + kolom 0/1/2/3. */
    private widget.PanelBiasa barisSkorHeaderPacs(){
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(false);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(8,10,4,10));
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridy=0; c.anchor=java.awt.GridBagConstraints.WEST; c.fill=java.awt.GridBagConstraints.HORIZONTAL;
        widget.Label t = new widget.Label();
        t.setText("PARAMETER");
        t.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        t.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        t.setForeground(widget.ThemeConfig.TEXT_MUTED);
        c.gridx=0; c.weightx=1.0; p.add(t,c);
        for(int j=0;j<4;j++){
            widget.Label h = new widget.Label();
            h.setText(String.valueOf(j));
            h.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            h.setForeground(widget.ThemeConfig.TEXT_MUTED);
            java.awt.Dimension d = new java.awt.Dimension(54,20);
            h.setPreferredSize(d); h.setMinimumSize(d);
            c.gridx=1+j; c.weightx=0; c.insets=new java.awt.Insets(0,4,0,0);
            p.add(h,c);
        }
        return p;
    }

    /** [Bagian D.1-4] Baris indikator skor 1 parameter: nama + 4 sel 0-3 (Label read-only), zebra. */
    private widget.PanelBiasa barisSkorPacs(int idx, String nama){
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(true);
        p.setBackground(idx%2==0 ? new java.awt.Color(0xFB,0xFC,0xFA) : java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,10,5,10));
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridy=0; c.anchor=java.awt.GridBagConstraints.WEST; c.fill=java.awt.GridBagConstraints.HORIZONTAL;

        widget.Label lblNama = new widget.Label();
        lblNama.setText(nama);
        lblNama.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        java.awt.Dimension dn = new java.awt.Dimension(150,24);
        lblNama.setPreferredSize(dn); lblNama.setMinimumSize(dn);
        c.gridx=0; c.weightx=1.0; p.add(lblNama,c);

        for(int j=0;j<4;j++){
            widget.Label sel = new widget.Label();
            sel.setText(String.valueOf(j));
            sel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            java.awt.Dimension ds = new java.awt.Dimension(54,24);
            sel.setPreferredSize(ds); sel.setMinimumSize(ds);
            sel.setOpaque(true);
            selSkorPacs[idx][j] = sel;
            c.gridx=1+j; c.weightx=0; c.insets=new java.awt.Insets(0,4,0,0);
            p.add(sel,c);
        }
        return p;
    }

    /** [Bagian D.2-3] Warnai sel indikator dari nilai combo skor (0=OK,1=PACS3,2=WARN,3=DANGER). */
    private void refreshIndikatorSkorPacs(){
        if(selSkorPacs==null){ return; }
        widget.ComboBox[] cmb = {CmbSkorKesadaranPacs,CmbSkorTDPacs,CmbSkorNadiPacs,CmbSkorRespirasiPacs,CmbSkorTemperaturPacs,CmbSkorSaturasiPacs};
        java.awt.Color[] solid = {widget.ThemeConfig.OK, widget.ThemeConfig.PACS3, widget.ThemeConfig.WARN, widget.ThemeConfig.DANGER};
        java.awt.Color[] bg    = {widget.ThemeConfig.OK_BG, widget.ThemeConfig.PACS3_BG, widget.ThemeConfig.WARN_BG, widget.ThemeConfig.DANGER_BG};
        java.awt.Color inaktifBg = new java.awt.Color(0xF4,0xF5,0xF2);
        for(int i=0;i<6;i++){
            int skor = -1;
            Object v = (cmb[i]==null) ? null : cmb[i].getSelectedItem();
            if(v!=null){ try{ skor = Integer.parseInt(v.toString()); }catch(NumberFormatException e){ skor = -1; } }
            for(int j=0;j<4;j++){
                widget.Label sel = selSkorPacs[i][j];
                if(sel==null){ continue; }
                sel.setOpaque(true);
                if(j==skor){
                    sel.setBackground(bg[j]);
                    sel.setForeground(solid[j]);
                    sel.setBorder(javax.swing.BorderFactory.createLineBorder(solid[j]));
                    sel.setFont(sel.getFont().deriveFont(java.awt.Font.BOLD));
                }else{
                    sel.setBackground(inaktifBg);
                    sel.setForeground(widget.ThemeConfig.TEXT_FAINT);
                    sel.setBorder(javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT));
                    sel.setFont(sel.getFont().deriveFont(java.awt.Font.PLAIN));
                }
            }
        }
    }

    /** [Bagian D.5] Banner Total Skor EWS - warna & interpretasi mengikuti level PACS hasil skor. */
    private widget.PanelBiasa buatBannerTotalPacs(){
        bannerTotalPacs = new widget.PanelBiasa();
        bannerTotalPacs.setOpaque(true);
        bannerTotalPacs.setLayout(new java.awt.BorderLayout(12,0));

        widget.PanelBiasa kiri = new widget.PanelBiasa();
        kiri.setOpaque(false);
        kiri.setLayout(new java.awt.GridLayout(2,1));
        widget.Label judul = new widget.Label();
        judul.setText("TOTAL SKOR EWS");
        judul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        judul.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblBannerTafsirPacs = new widget.Label();
        lblBannerTafsirPacs.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblBannerTafsirPacs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        kiri.add(judul); kiri.add(lblBannerTafsirPacs);

        widget.PanelBiasa kanan = new widget.PanelBiasa();
        kanan.setOpaque(false);
        kanan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT,6,0));
        LblTotalSkorPacs.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        LblTotalSkorPacs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        lblBannerSatuanPacs = new widget.Label();
        lblBannerSatuanPacs.setText("/ 18");
        lblBannerSatuanPacs.setForeground(widget.ThemeConfig.TEXT_MUTED);
        kanan.add(LblTotalSkorPacs); kanan.add(lblBannerSatuanPacs);

        bannerTotalPacs.add(kiri, java.awt.BorderLayout.CENTER);
        bannerTotalPacs.add(kanan, java.awt.BorderLayout.EAST);

        widget.PanelBiasa wrap = new widget.PanelBiasa();
        wrap.setOpaque(false);
        wrap.setBorder(javax.swing.BorderFactory.createEmptyBorder(8,10,10,10));
        wrap.setLayout(new java.awt.BorderLayout());
        wrap.add(bannerTotalPacs, java.awt.BorderLayout.CENTER);
        return wrap;
    }

    private void refreshBannerTotalPacs(){
        if(bannerTotalPacs==null){ return; }
        int total;
        try{ total = Integer.parseInt(LblTotalSkorPacs.getText().trim()); }
        catch(NumberFormatException e){ total = -1; }
        java.awt.Color solid, bg; String tafsir;
        if(total<0){ solid = widget.ThemeConfig.TEXT_MUTED; bg = widget.ThemeConfig.SURFACE_ALT; tafsir = "Menunggu tanda vital"; }
        else if(total>=7){ solid = widget.ThemeConfig.DANGER; bg = widget.ThemeConfig.DANGER_BG; tafsir = "Merah — risiko tinggi, respons segera"; }
        else if(total>=4){ solid = widget.ThemeConfig.WARN;   bg = widget.ThemeConfig.WARN_BG;   tafsir = "Kuning — risiko sedang, tinjau dalam 10 menit"; }
        else if(total>=2){ solid = widget.ThemeConfig.PACS3;  bg = widget.ThemeConfig.PACS3_BG;  tafsir = "Kuning-Hijau — risiko rendah, pantau berkala"; }
        else { solid = widget.ThemeConfig.OK; bg = widget.ThemeConfig.OK_BG; tafsir = "Hijau — stabil"; }
        bannerTotalPacs.setBackground(bg);
        bannerTotalPacs.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(solid),
            javax.swing.BorderFactory.createEmptyBorder(12,16,12,16)));
        lblBannerTafsirPacs.setText(tafsir);
        lblBannerTafsirPacs.setForeground(solid);
        LblTotalSkorPacs.setForeground(solid);
    }

    /** [Bagian E.3] Baris: judul "LEVEL TRIAGE (PACS)" + badge "Saran sistem: PACS n". */
    private widget.PanelBiasa barisSaranLevelPacs(){
        lblSaranLevelPacs = new widget.Label();
        lblSaranLevelPacs.setText("Saran sistem: —");
        lblSaranLevelPacs.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSaranLevelPacs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        lblSaranLevelPacs.setOpaque(true);
        lblSaranLevelPacs.setBackground(widget.ThemeConfig.SURFACE_ALT);
        lblSaranLevelPacs.setForeground(widget.ThemeConfig.TEXT_MUTED);
        lblSaranLevelPacs.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
            javax.swing.BorderFactory.createEmptyBorder(3,10,3,10)));

        widget.Label judul = new widget.Label();
        judul.setText("LEVEL TRIAGE (PACS)");
        judul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        judul.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        judul.setForeground(widget.ThemeConfig.TEXT_MUTED);

        widget.Label caption = new widget.Label();
        caption.setText("keputusan klinis petugas — tidak dihitung dari Total");
        caption.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        caption.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 9));
        caption.setForeground(widget.ThemeConfig.TEXT_FAINT);

        widget.PanelBiasa judulWrap = new widget.PanelBiasa();
        judulWrap.setOpaque(false);
        judulWrap.setLayout(new java.awt.GridLayout(2,1));
        judulWrap.add(judul);
        judulWrap.add(caption);

        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(false);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(10,10,0,10));
        p.setLayout(new java.awt.BorderLayout());
        p.add(judulWrap, java.awt.BorderLayout.WEST);
        p.add(lblSaranLevelPacs, java.awt.BorderLayout.EAST);
        return p;
    }

    private int saranLevelDariTotal(int total){
        if(total>=7) return 1;
        if(total>=4) return 2;
        if(total>=2) return 3;
        return 4;
    }

    private void refreshSaranLevelPacs(){
        if(lblSaranLevelPacs==null){ return; }
        try{
            int total = Integer.parseInt(LblTotalSkorPacs.getText().trim());
            lblSaranLevelPacs.setText("Saran sistem: PACS "+saranLevelDariTotal(total));
        }catch(NumberFormatException e){
            lblSaranLevelPacs.setText("Saran sistem: —");
        }
    }

    /** [Bagian E.1] 4 kartu Level Triage; klik kartu -> pilih RbPacsN (tersembunyi, tetap sumber nilai simpan). */
    private widget.PanelBiasa buatBarisKartuLevelPacs(){
        String[] judul = {"PACS 1","PACS 2","PACS 3","PACS 4"};
        String[] desc  = {"Resusitasi · segera","Emergensi · ≤ 10 mnt","Urgen · ≤ 30 mnt","Non-urgen · ≤ 60 mnt"};

        bgLevelPacs = new javax.swing.ButtonGroup();
        RbPacs1 = new widget.RadioButton(); RbPacs1.setText(PacsLevel.PACS_1.getLabel());
        RbPacs2 = new widget.RadioButton(); RbPacs2.setText(PacsLevel.PACS_2.getLabel());
        RbPacs3 = new widget.RadioButton(); RbPacs3.setText(PacsLevel.PACS_3.getLabel());
        RbPacs4 = new widget.RadioButton(); RbPacs4.setText(PacsLevel.PACS_4.getLabel());
        final widget.RadioButton[] rb = {RbPacs1,RbPacs2,RbPacs3,RbPacs4};
        for(widget.RadioButton r : rb){ bgLevelPacs.add(r); r.setVisible(false); }

        widget.PanelBiasa baris = new widget.PanelBiasa();
        baris.setOpaque(false);
        baris.setBorder(javax.swing.BorderFactory.createEmptyBorder(6,10,6,10));
        baris.setLayout(new java.awt.GridLayout(1,4,8,0));
        for(int i=0;i<4;i++){
            final int idx = i;
            widget.PanelBiasa kartu = new widget.PanelBiasa();
            kartu.setOpaque(true);
            kartu.setLayout(new java.awt.BorderLayout());

            widget.PanelBiasa strip = new widget.PanelBiasa();
            strip.setOpaque(true);
            strip.setPreferredSize(new java.awt.Dimension(10,4));

            widget.PanelBiasa isi = new widget.PanelBiasa();
            isi.setOpaque(false);
            isi.setBorder(javax.swing.BorderFactory.createEmptyBorder(8,10,10,10));
            isi.setLayout(new java.awt.GridLayout(2,1));
            widget.Label lj = new widget.Label();
            lj.setText(judul[i]);
            lj.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            lj.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            widget.Label ld = new widget.Label();
            ld.setText(desc[i]);
            ld.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            ld.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            isi.add(lj); isi.add(ld);

            kartu.add(strip, java.awt.BorderLayout.NORTH);
            kartu.add(isi, java.awt.BorderLayout.CENTER);
            kartu.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            kartu.addMouseListener(new java.awt.event.MouseAdapter(){
                @Override public void mousePressed(java.awt.event.MouseEvent e){ rb[idx].setSelected(true); }
            });

            kartuLevelPacs[i]=kartu; kartuStripPacs[i]=strip; kartuJudulPacs[i]=lj; kartuDescPacs[i]=ld;
            baris.add(kartu);
        }
        return baris;
    }

    private java.awt.Color campurWarna(java.awt.Color a, java.awt.Color b, double t){
        int r  = (int)Math.round(a.getRed()  *(1-t) + b.getRed()  *t);
        int g  = (int)Math.round(a.getGreen()*(1-t) + b.getGreen()*t);
        int bl = (int)Math.round(a.getBlue() *(1-t) + b.getBlue() *t);
        return new java.awt.Color(r,g,bl);
    }

    /** [Bagian E.2] Restyle kartu Level dari RbPacs* mana yang terpilih. */
    private void refreshKartuLevelPacs(){
        if(kartuLevelPacs==null){ return; }
        boolean[] aktif = {RbPacs1.isSelected(),RbPacs2.isSelected(),RbPacs3.isSelected(),RbPacs4.isSelected()};
        java.awt.Color inaktifBorder = new java.awt.Color(0xD8,0xDB,0xD6);
        for(int i=0;i<4;i++){
            if(kartuLevelPacs[i]==null){ continue; }
            if(aktif[i]){
                kartuLevelPacs[i].setBackground(warnaLevelBgPacs[i]);
                kartuLevelPacs[i].setBorder(javax.swing.BorderFactory.createMatteBorder(2,2,2,2, warnaLevelPacs[i]));
                kartuStripPacs[i].setBackground(warnaLevelPacs[i]);
                kartuJudulPacs[i].setForeground(warnaLevelPacs[i]);
                kartuDescPacs[i].setForeground(warnaLevelPacs[i]);
            }else{
                kartuLevelPacs[i].setBackground(java.awt.Color.WHITE);
                kartuLevelPacs[i].setBorder(javax.swing.BorderFactory.createLineBorder(inaktifBorder));
                kartuStripPacs[i].setBackground(campurWarna(warnaLevelPacs[i], java.awt.Color.WHITE, 0.58));
                kartuJudulPacs[i].setForeground(widget.ThemeConfig.TEXT);
                kartuDescPacs[i].setForeground(widget.ThemeConfig.TEXT_MUTED);
            }
        }
    }

    /** Label bold kecil di atas satu kolom (dipakai berpasangan spy 2 kolom sejajar tingginya). */
    private widget.Label labelKolomKeputusanPacs(String teks){
        widget.Label l = new widget.Label();
        l.setText(teks);
        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        return l;
    }

    /** [Bagian F] panelKeputusan (4 radio berbungkus) + Catatan Khusus berdampingan. */
    private widget.PanelBiasa buatBarisKeputusanPacs(java.awt.Component scrollCatatan){
        bgKeputusanPacs = new javax.swing.ButtonGroup();
        RbPacsResusitasi = new widget.RadioButton(); RbPacsResusitasi.setText("Ruang Resusitasi");
        RbPacsNonResusitasi = new widget.RadioButton(); RbPacsNonResusitasi.setText("Ruang Non Resusitasi");
        RbPacsPoliKlinik = new widget.RadioButton(); RbPacsPoliKlinik.setText("Poli Klinik/Klinik Umum 24 Jam");
        RbPacsDOA = new widget.RadioButton(); RbPacsDOA.setText("Death On Arrival");
        widget.RadioButton[] rb = {RbPacsResusitasi,RbPacsNonResusitasi,RbPacsPoliKlinik,RbPacsDOA};

        widget.PanelBiasa panelKeputusan = new widget.PanelBiasa();
        panelKeputusan.setOpaque(false);
        panelKeputusan.setLayout(new java.awt.GridLayout(4,1,0,7));
        for(int i=0;i<4;i++){
            bgKeputusanPacs.add(rb[i]);
            widget.PanelBiasa w = new widget.PanelBiasa();
            w.setOpaque(true);
            w.setBackground(java.awt.Color.WHITE);
            w.setLayout(new java.awt.BorderLayout());
            rb[i].setOpaque(false);
            w.add(rb[i], java.awt.BorderLayout.CENTER);
            bungkusKeputusanPacs[i]=w;
            panelKeputusan.add(w);
        }

        // Kolom kiri & kanan MASING-MASING dapat label bold sendiri (bukan cuma
        // strip "KEPUTUSAN PENEMPATAN" di atas seluruh baris) - tanpa label
        // kolom kanan, kotak "Catatan Khusus" tidak pernah kelihatan namanya
        // sendiri; kolom kiri ikut diberi label yg sama supaya kedua kotak
        // sejajar tingginya (BorderLayout NORTH+CENTER keduanya), bukan salah
        // satu turun sendiri krn cuma dia yg punya label di atasnya.
        widget.PanelBiasa kolomKeputusan = new widget.PanelBiasa();
        kolomKeputusan.setOpaque(false);
        kolomKeputusan.setLayout(new java.awt.BorderLayout(0,6));
        kolomKeputusan.add(labelKolomKeputusanPacs("Keputusan Penempatan *"), java.awt.BorderLayout.NORTH);
        kolomKeputusan.add(panelKeputusan, java.awt.BorderLayout.CENTER);

        widget.PanelBiasa kolomCatatan = new widget.PanelBiasa();
        kolomCatatan.setOpaque(false);
        kolomCatatan.setLayout(new java.awt.BorderLayout(0,6));
        kolomCatatan.add(labelKolomKeputusanPacs("Catatan Khusus"), java.awt.BorderLayout.NORTH);
        kolomCatatan.add(scrollCatatan, java.awt.BorderLayout.CENTER);

        widget.PanelBiasa isi = new widget.PanelBiasa();
        isi.setOpaque(false);
        isi.setBorder(javax.swing.BorderFactory.createEmptyBorder(6,10,10,10));
        isi.setLayout(new java.awt.GridLayout(1,2,16,0));
        isi.add(kolomKeputusan);
        isi.add(kolomCatatan);
        return isi;
    }

    private void refreshBungkusKeputusanPacs(){
        if(bungkusKeputusanPacs==null){ return; }
        widget.RadioButton[] rb = {RbPacsResusitasi,RbPacsNonResusitasi,RbPacsPoliKlinik,RbPacsDOA};
        for(int i=0;i<4;i++){
            if(bungkusKeputusanPacs[i]==null){ continue; }
            if(rb[i].isSelected()){
                bungkusKeputusanPacs[i].setBackground(new java.awt.Color(0xF2,0xF7,0xFB));
                bungkusKeputusanPacs[i].setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.ACCENT),
                    javax.swing.BorderFactory.createEmptyBorder(7,10,7,10)));
                rb[i].setForeground(widget.ThemeConfig.ACCENT);
                rb[i].setFont(rb[i].getFont().deriveFont(java.awt.Font.BOLD));
            }else{
                bungkusKeputusanPacs[i].setBackground(java.awt.Color.WHITE);
                bungkusKeputusanPacs[i].setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
                    javax.swing.BorderFactory.createEmptyBorder(7,10,7,10)));
                rb[i].setForeground(widget.ThemeConfig.TEXT);
                rb[i].setFont(rb[i].getFont().deriveFont(java.awt.Font.PLAIN));
            }
        }
    }

    /**
     * Panel pembungkus generik supaya 1 anak (PanelPacs) bisa diregangkan
     * mengisi SELURUH lebar viewport widget.ScrollPane, brp pun lebar
     * viewport sungguhan saat dirender (JTabbedPane merentangkan komponen
     * tab aktif ke lebar area tab yg tersedia, TIDAK peduli preferredSize
     * scrollPacs - lihat catatan di initPacsTab() persis sebelum dipakai).
     * `implements Scrollable` + getScrollableTracksViewportWidth() TRUE
     * adalah mekanisme baku Swing utk kasus ini - JViewport otomatis
     * meregangkan view (wrapperPacs ini) SELEBAR dirinya kalau method ini
     * true. Nama kelas ("Tengah") sejarahnya dari percobaan pertama yg
     * men-tengah-kan PanelPacs (constraint kosong/weightx=0) - sekarang
     * dipakai kebalikannya (constraint fill=HORIZONTAL+weightx=1.0 di
     * initPacsTab()) supaya PanelPacs ikut meregang penuh, bukan cuma
     * ditengahkan sempit - class-nya sendiri generik, cukup 1x reparenting,
     * perilaku akhirnya ditentukan constraint yg dipakai saat .add().
     */
    private static class ViewportTengahPacs extends javax.swing.JPanel implements javax.swing.Scrollable {
        ViewportTengahPacs(){
            super(new java.awt.GridBagLayout());
            setOpaque(false);
        }
        @Override public java.awt.Dimension getPreferredScrollableViewportSize(){ return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect,int orientation,int direction){ return 20; }
        @Override public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect,int orientation,int direction){ return 100; }
        @Override public boolean getScrollableTracksViewportWidth(){ return true; }
        @Override public boolean getScrollableTracksViewportHeight(){ return false; }
    }

    /** Item 3/6: tampilkan warna cetak PacsLevel di tombol pilihan form (PacsLevel.getWarnaCetakHex()). */
    private void pewarnaanTombolPacs(widget.RadioButton tombol,PacsLevel level){
        tombol.setOpaque(true);
        tombol.setBackground(java.awt.Color.decode(level.getWarnaCetakHex()));
        tombol.setForeground(java.awt.Color.WHITE);
    }

    /**
     * Skor 0-3 dihitung OTOMATIS dari nilai tanda vital (lihat
     * hitungSkorOtomatisPacs() / kelas PacsScoring) - combo ini jadi
     * tampilan hasil (read-only), bukan pilihan manual petugas lagi.
     * Item "?" disediakan sebagai jaring pengaman generik (dipakai
     * hitungTotalSkorPacs() kalau ada combo yang belum punya pilihan) -
     * saat ini tidak ada aturan skor PACS yang menghasilkan nilai tak terdefinisi.
     */
    private widget.ComboBox buatComboSkor(){
        widget.ComboBox cmb = new widget.ComboBox();
        cmb.addItem("0"); cmb.addItem("1"); cmb.addItem("2"); cmb.addItem("3"); cmb.addItem("?");
        cmb.setEnabled(false);
        // Ukuran tetap PENDEK (bukan melebar) - isinya cuma 1 digit, lihat
        // catatan ukuranPacsFieldPendek() soal gotcha widget.TextBox/ComboBox.
        cmb.setPreferredSize(new java.awt.Dimension(70,23));
        return cmb;
    }

    /**
     * Ukuran preferred TETAP & PENDEK (130x23) utk field angka tab PACS
     * (Tekanan Darah/Nadi/Suhu/dst). widget.TextBox constructor diam2
     * memanggil setSize(WIDTH,23) dgn WIDTH=1 (java.awt.image.ImageObserver.
     * WIDTH, BUKAN lebar pixel - gotcha lawas yg sama sudah didokumentasikan
     * & diperbaiki dgn pola identik di sesi RM 03-2/03-4-05-06), jadi tanpa
     * .setPreferredSize() eksplisit lebar kolom field GridBagLayout jadi
     * "kebetulan" ikut komponen lain yg lebih lebar di kolom yg sama -
     * bukan lebar yg disengaja. 130px cukup lega utk angka tapi tetap
     * "pas/pendek" (bukan melebar) sesuai permintaan.
     */
    private widget.TextBox ukuranPacsFieldPendek(widget.TextBox t){
        t.setPreferredSize(new java.awt.Dimension(130,23));
        return t;
    }

    private void CekBoxIsiTeks(widget.CekBox cek,String teks){
        cek.setText(teks);
    }

    private widget.Label pacsLabelTeks(String teks){
        widget.Label lbl = new widget.Label();
        lbl.setText(teks);
        // Bugfix alignment: widget.Label (usu.widget.Label) defaultnya RATA
        // KANAN - sudah pernah dicatat di sesi RM 03-2 sbg bug identik di
        // tambahBarisPacsLabel() milik file ini, saat itu sengaja tidak
        // diperbaiki krn di luar scope permintaan waktu itu. Sekarang
        // diperbaiki eksplisit (diverifikasi via render offscreen).
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        return lbl;
    }

    /** Satu baris "[checkbox Ya/Tidak] [keterangan]" utk satu kategori Mekanisme Trauma. */
    private int tambahBarisMekanismeTrauma(widget.PanelBiasa panel,int rowMt,String label,widget.CekBox cek,widget.TextBox ket){
        cek.setText(label);
        java.awt.GridBagConstraints c1 = pacsConstraint(0,rowMt);
        panel.add(cek,c1);
        java.awt.GridBagConstraints c2 = pacsConstraint(1,rowMt);
        panel.add(ket,c2);
        return rowMt+1;
    }

    /**
     * [Mekanisme Trauma] Tampil HANYA kalau Kasus terpilih = Trauma
     * (KdKasus != "004" - "004" = Non Trauma, sesuai master_triase_macam_kasus).
     * Kalau Non Trauma: panel disembunyikan DAN seluruh isiannya dikosongkan,
     * supaya tidak ada data mekanisme trauma "nyangkut" ikut tersimpan untuk
     * kasus non-trauma (hasil cetak jadi kosong sesuai permintaan).
     */
    private void perbaruiVisibilitasMekanismeTrauma(){
        boolean trauma = !KdKasus.getText().trim().equals("004") && !KdKasus.getText().trim().isEmpty();
        panelMekanismeTrauma.setVisible(trauma);
        if(barisMekanismePacs!=null){ barisMekanismePacs.setVisible(trauma); }
        if(!trauma){
            CekKklTunggal.setSelected(false);
            CekKklDenganLawan.setSelected(false);
            TKklLawan.setText("");
            TKklTempatKejadian.setText("");
            panelKklKejadian.setSekarang();
            CekJatuhKetinggian.setSelected(false);
            TJatuhKetinggianKet.setText("");
            CekLukaBakar.setSelected(false);
            TLukaBakarKet.setText("");
            CekTraumaFisik.setSelected(false);
            TTraumaFisikKet.setText("");
            CekTraumaZatKimia.setSelected(false);
            TTraumaZatKimiaKet.setText("");
            CekTraumaZatLainnya.setSelected(false);
            TTraumaZatLainnyaKet.setText("");
        }
    }

    /**
     * [PACS] Ambil bagian jam ("HH:mm:ss", posisi 11-19) dari widget.Tanggal secara
     * aman. widget.Tanggal yang dibangun manual di sini (bukan lewat GEN-BEGIN/GEN-END)
     * kadang toString()-nya lebih pendek dari 19 karakter (StringIndexOutOfBoundsException
     * pernah terjadi persis di sini pada DTPKeputusanPukulPacs) - fallback "00:00:00"
     * kalau formatnya tidak sesuai dugaan, daripada Simpan/Ganti gagal total tanpa pesan.
     */
    private String pukulAman(widget.Tanggal t){
        String s = t.getSelectedItem()+"";
        return s.length()>=19 ? s.substring(11,19) : "00:00:00";
    }

    // [Bagian E] "Semua field kotak, jangan ada yang membulat" - HANYA form
    // ini (widget.ThemeConfig.ARC global TIDAK diubah). Pola sama dgn
    // buatKotakKep() di RMPenilaianAwalKeperawatanIGD.java, TAPI tombol
    // (AbstractButton/widget.Button) SENGAJA tidak disentuh - permintaan
    // user cuma soal "field" (kotak teks/combo/tanggal), bukan tombol/chip
    // yang sudah didesain bulat sesuai mockup Tier A-C.
    private static final java.awt.Color WARNA_BORDER_KOTAK_PACS = new java.awt.Color(0xD8, 0xDB, 0xD6);
    private static final javax.swing.border.Border BORDER_KOTAK_TEXTFIELD_PACS = javax.swing.BorderFactory
            .createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(WARNA_BORDER_KOTAK_PACS, 1),
                    javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8));
    private static final javax.swing.border.Border BORDER_KOTAK_GARIS_PACS = javax.swing.BorderFactory
            .createLineBorder(WARNA_BORDER_KOTAK_PACS, 1);

    private static void kotakkanField(java.awt.Component c){
        if(c==null){ return; }
        if(c instanceof javax.swing.JComponent){
            javax.swing.JComponent jc = (javax.swing.JComponent)c;
            jc.putClientProperty("Component.arc", 0);
            jc.putClientProperty("TextComponent.arc", 0);
            jc.putClientProperty("JTextField.arc", 0);
            jc.putClientProperty("ComboBox.arc", 0);
            jc.putClientProperty("JComponent.roundRect", false);

            if(c instanceof javax.swing.text.JTextComponent){
                if(!(c instanceof javax.swing.JTextArea)){
                    jc.setBorder(BORDER_KOTAK_TEXTFIELD_PACS);
                }
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                        "background: #FFFFFF; inactiveBackground: #FFFFFF; disabledBackground: #FFFFFF;");
            }else if(c instanceof javax.swing.JComboBox){
                jc.setBorder(BORDER_KOTAK_GARIS_PACS);
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                        "background: #FFFFFF; disabledBackground: #FFFFFF; buttonBackground: #FFFFFF; editableBackground: #FFFFFF;");
            }else if(c instanceof javax.swing.JScrollPane){
                jc.setBorder(BORDER_KOTAK_GARIS_PACS);
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
            }else if(c instanceof uz.ncipro.calendar.JDateTimePicker){
                jc.setBorder(BORDER_KOTAK_GARIS_PACS);
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style", "background: #FFFFFF; buttonBackground: #FFFFFF;");
            }
        }
        if(c instanceof java.awt.Container){
            for(java.awt.Component child : ((java.awt.Container)c).getComponents()){
                kotakkanField(child);
            }
        }
    }

    private java.awt.GridBagConstraints pacsConstraint(int gridx,int gridy){
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = gridx; c.gridy = gridy;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.insets = new java.awt.Insets(3,5,3,5);
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        return c;
    }

    /**
     * Bugfix alignment (Fase 3, layout dua-kolom tab PACS): gotcha baku
     * GridBagLayout - kalau SEMUA komponen dalam satu container punya
     * weightx=0 (kasus pacsConstraint() di atas, dipakai konsisten oleh
     * label/field PACS), dan container itu diberi lebih banyak lebar drpd
     * total preferred width kontennya (spt panelPacsKiri/panelPacsKanan yang
     * di-stretch fill=BOTH di kolom 0.5 lebar tab), GridBagLayout MEN-TENGAH-
     * KAN seluruh grid di dalam container-nya, bukan rata kiri - ini akar
     * penyebab label & field "mengambang di tengah/kanan" yang dilaporkan.
     * Filler tak-terlihat (Box.createHorizontalGlue) ditaruh di kolom PALING
     * KANAN (gridx=2 - belum pernah dipakai baris manapun) dengan weightx=1.0
     * supaya SEMUA sisa lebar diserap di situ (tak terlihat), bukan komponen
     * label/field sungguhan - jadi kolom 0 (label) & kolom 1 (input) tetap di
     * lebar aslinya, rata kiri (LEADING), tidak ikut melebar/bergeser.
     * Dipanggil sekali per panel, setelah SEMUA baris isinya selesai ditambah.
     */
    private void tambahPengisiKolom(widget.PanelBiasa panel){
        java.awt.GridBagConstraints cIsi = new java.awt.GridBagConstraints();
        cIsi.gridx = 2; cIsi.gridy = 0;
        cIsi.gridheight = java.awt.GridBagConstraints.REMAINDER;
        cIsi.weightx = 1.0;
        cIsi.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panel.add(javax.swing.Box.createHorizontalGlue(), cIsi);
    }

    /**
     * Fase 3 (layout dua-kolom tab PACS): parameter "panel" ditambahkan
     * supaya baris section-header bisa ditaruh di PanelPacs (judul tab),
     * panelPacsKiri, ATAU panelPacsKanan - sebelumnya hardcode ke
     * PanelPacs. Satu-satunya pemanggil method ini ada di initPacsTab(),
     * jadi aman diubah signature-nya (bukan variabel/field UI).
     */
    private int tambahBarisPacsLabel(widget.PanelBiasa panel,int row,String judul){
        widget.Label lbl = new widget.Label();
        lbl.setText(judul);
        lbl.setFont(lbl.getFont().deriveFont(java.awt.Font.BOLD));
        // Bugfix alignment: lihat catatan pacsLabelTeks() - widget.Label
        // defaultnya rata kanan, dipaksa rata kiri (LEADING) di sini.
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        java.awt.GridBagConstraints c = pacsConstraint(0,row);
        c.gridwidth = 2;
        panel.add(lbl,c);
        return row+1;
    }

    /** Fase 3: lihat catatan javadoc tambahBarisPacsLabel() di atas - parameter "panel" sama alasannya. */
    private int tambahBarisPacsField(widget.PanelBiasa panel,int row,String label,java.awt.Component field){
        widget.Label lbl = new widget.Label();
        lbl.setText(label+" :");
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        java.awt.GridBagConstraints c1 = pacsConstraint(0,row);
        panel.add(lbl,c1);
        java.awt.GridBagConstraints c2 = pacsConstraint(1,row);
        panel.add(field,c2);
        return row+1;
    }

    /**
     * Cetak "rptLembarTriasePacs.jasper" (formulir RM 03-1). Pola sama persis
     * dengan BtnPrintActionPerformed("Laporan 1") - MyReportqry butuh SQL
     * jadi (bukan JRXML queryString) karena datanya dilewatkan lewat
     * JRResultSetDataSource, bukan dieksekusi ulang dari dalam file .jasper.
     */
    private void btnPrintPacsActionPerformed(java.awt.event.ActionEvent evt){
        if(TNoRw.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Pasien");
            return;
        }
        cetakPacs(TNoRw.getText());
    }

    /**
     * Cetak "rptLembarTriasePacs.jasper" (formulir RM 03-1) utk no_rawat tertentu -
     * dipisah dari btnPrintPacsActionPerformed() supaya bisa dipanggil juga dari
     * BtnPrintActionPerformed (tombol Cetak bersama) dengan no_rawat baris terpilih,
     * bukan cuma dari TNoRw (field header tab Input Triase).
     */
    private void cetakPacs(String noRawat){
        Map<String,Object> param = new HashMap<>();
        param.put("namars",akses.getnamars());
        param.put("alamatrs",akses.getalamatrs());
        param.put("kotars",akses.getkabupatenrs());
        param.put("propinsirs",akses.getpropinsirs());
        param.put("kontakrs",akses.getkontakrs());
        param.put("emailrs",akses.getemailrs());
        param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
        param.put("norawat",noRawat);
        String noRawatAman = noRawat.replaceAll("'","");
        // [PACS] QR verifikasi tanda tangan elektronik - pola sama persis dengan
        // BtnPrint1ActionPerformed (Skala 1-5): nama RS, nama petugas, ID sidik
        // jari/NIK, dan tanggal triase, di-encode jadi QR code (lihat GayaLevelPacs...
        // tidak, lihat komponen jr:QRCode di rptLembarTriasePacs.jrxml).
        String nikPetugasPacs = Sequel.cariIsi("select nik from data_triase_pacs where no_rawat=?",noRawatAman);
        String namaPetugasPacs = Sequel.cariIsi("select nama from pegawai where nik=?",nikPetugasPacs);
        String tglTriasePacs = Sequel.cariIsi("select tanggal_triase from data_triase_pacs where no_rawat=?",noRawatAman);
        String fingerHashPacs = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",nikPetugasPacs);
        param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+
            "\nDitandatangani secara elektronik oleh "+namaPetugasPacs+
            "\nID "+(fingerHashPacs==null||fingerHashPacs.isEmpty()?nikPetugasPacs:fingerHashPacs)+
            "\n"+Valid.SetTgl3(tglTriasePacs));
        // [PACS] No. Ijin Praktek cuma terisi kalau petugas triase kebetulan seorang
        // dokter (tabel dokter.kd_dokter = pegawai.nik) - kosong utk perawat/staf lain,
        // itu wajar (bukan bug).
        param.put("nikpetugas",nikPetugasPacs);
        param.put("noijinpraktek",Sequel.cariIsi("select no_ijn_praktek from dokter where kd_dokter=?",nikPetugasPacs));
        String qry =
            "select "+
            "pasien.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, "+
            "TIMESTAMPDIFF(YEAR, pasien.tgl_lahir, CURDATE()) as umur, "+
            "reg_periksa.tgl_registrasi, reg_periksa.jam_reg, "+
            "data_triase_igd.tgl_kunjungan, data_triase_igd.cara_masuk, data_triase_igd.alat_transportasi, "+
            "CASE WHEN data_triase_igd.alat_transportasi='AGD' THEN 'Ambulance' ELSE 'Kendaraan bukan ambulance' END as kendaraan_tampil, "+
            "data_triase_igd.alasan_kedatangan, data_triase_igd.keterangan_kedatangan, data_triase_igd.kode_kasus, "+
            "master_triase_macam_kasus.macam_kasus, "+
            "CASE WHEN data_triase_igd.kode_kasus='004' THEN 'Non Trauma' ELSE 'Trauma' END as kasus_tampil, "+
            "data_triase_igd.kontak_awal_tanggal, data_triase_igd.kontak_awal_pukul, "+
            "data_triase_igd.terpasang_infus, data_triase_igd.terpasang_ngt, data_triase_igd.terpasang_kateter_urin, "+
            "data_triase_igd.nama_pengantar, data_triase_igd.telp_pengantar, "+
            "data_triase_igd.tekanan_darah, data_triase_igd.nadi, data_triase_igd.pernapasan, data_triase_igd.suhu, "+
            "data_triase_igd.saturasi_o2, data_triase_igd.nyeri, data_triase_igd.kesadaran, "+
            "data_triase_igd.berat_badan, data_triase_igd.tinggi_badan, "+
            "data_triase_pacs.keluhan_utama, "+
            "data_triase_pacs.skor_kesadaran, data_triase_pacs.skor_tekanan_darah_sistolik, data_triase_pacs.skor_nadi, "+
            "data_triase_pacs.skor_respirasi, data_triase_pacs.skor_temperatur, data_triase_pacs.skor_saturasi_o2, "+
            "data_triase_pacs.total_skor, data_triase_pacs.level_pacs, data_triase_pacs.keputusan_pacs, "+
            "data_triase_pacs.keputusan_pukul, data_triase_pacs.tanggal_triase, data_triase_pacs.catatan_khusus, "+
            "pegawai.nama as petugas_nama, "+
            "data_triase_mekanisme_trauma.kkl_tunggal, data_triase_mekanisme_trauma.kkl_dengan_lawan, data_triase_mekanisme_trauma.kkl_lawan, "+
            "data_triase_mekanisme_trauma.kkl_tempat_kejadian, data_triase_mekanisme_trauma.kkl_tanggal_kejadian, data_triase_mekanisme_trauma.kkl_pukul_kejadian, "+
            "data_triase_mekanisme_trauma.jatuh_ketinggian, data_triase_mekanisme_trauma.jatuh_ketinggian_keterangan, "+
            "data_triase_mekanisme_trauma.luka_bakar, data_triase_mekanisme_trauma.luka_bakar_keterangan, "+
            "data_triase_mekanisme_trauma.trauma_fisik, data_triase_mekanisme_trauma.trauma_fisik_keterangan, "+
            "data_triase_mekanisme_trauma.trauma_zat_kimia, data_triase_mekanisme_trauma.trauma_zat_kimia_keterangan, "+
            "data_triase_mekanisme_trauma.trauma_zat_lainnya, data_triase_mekanisme_trauma.trauma_zat_lainnya_keterangan "+
            "from data_triase_igd "+
            "inner join reg_periksa on reg_periksa.no_rawat = data_triase_igd.no_rawat "+
            "inner join pasien on pasien.no_rkm_medis = reg_periksa.no_rkm_medis "+
            "left join master_triase_macam_kasus on master_triase_macam_kasus.kode_kasus = data_triase_igd.kode_kasus "+
            "left join data_triase_pacs on data_triase_pacs.no_rawat = data_triase_igd.no_rawat "+
            "left join pegawai on pegawai.nik = data_triase_pacs.nik "+
            "left join data_triase_mekanisme_trauma on data_triase_mekanisme_trauma.no_rawat = data_triase_igd.no_rawat "+
            "where data_triase_igd.no_rawat='"+noRawatAman+"'";
        try {
            pilihan = (String)JOptionPane.showInputDialog(null,"Silahkan pilih Lembar/PDF Triase PACS..!","Pilihan",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Lembar Triase PACS","PDF Triase PACS"},"Lembar Triase PACS");
            switch (pilihan) {
                case "Lembar Triase PACS":
                      Valid.MyReportqry("rptLembarTriasePacs.jasper","report","::[ Triase PACS ]::",qry,param);
                      break;
                case "PDF Triase PACS":
                      Valid.MyReportqrypdf("rptLembarTriasePacs.jasper","report","::[ Triase PACS ]::",qry,param);
                      break;
            }
        } catch (Exception e) {
        }
    }

    /**
     * nilai input -> tentukan kategori -> tentukan skor 0-3 -> tampilkan skor.
     * Dipanggil tiap kali salah satu tanda vital PACS selesai diisi (focusLost)
     * atau Kesadaran diganti. Semua aturan ambil dari kelas PacsScoring - tidak
     * ada angka/kategori yang ditentukan di sini, method ini cuma parsing input
     * mentah lalu menampilkan hasilnya ke combo skor (yang sudah dibuat
     * read-only di buatComboSkor()).
     */
    private void hitungSkorOtomatisPacs(){
        // Kesadaran
        Object kes = PacsKesadaran.getSelectedItem();
        CmbSkorKesadaranPacs.setSelectedItem(String.valueOf(PacsScoring.skorKesadaran(kes==null?null:kes.toString())));

        // Tekanan Darah Sistolik - PacsTensi diisi format "sistolik/diastolik" (mis. 120/80)
        try{
            String teks = PacsTensi.getText().trim();
            if(!teks.isEmpty()){
                String sistolikTeks = teks.contains("/") ? teks.substring(0,teks.indexOf("/")).trim() : teks;
                int sistolik = Integer.parseInt(sistolikTeks);
                CmbSkorTDPacs.setSelectedItem(String.valueOf(PacsScoring.skorTekananDarahSistolik(sistolik)));
            }
        }catch(NumberFormatException e){
            // format Tekanan Darah belum valid/belum lengkap - skor TD dibiarkan apa adanya
        }

        // Nadi
        try{
            String teks = PacsNadi.getText().trim();
            if(!teks.isEmpty()){
                int nadi = Integer.parseInt(teks);
                CmbSkorNadiPacs.setSelectedItem(String.valueOf(PacsScoring.skorNadi(nadi)));
            }
        }catch(NumberFormatException e){
        }

        // Respirasi
        try{
            String teks = PacsRespirasi.getText().trim();
            if(!teks.isEmpty()){
                int respirasi = Integer.parseInt(teks);
                CmbSkorRespirasiPacs.setSelectedItem(String.valueOf(PacsScoring.skorRespirasi(respirasi)));
            }
        }catch(NumberFormatException e){
        }

        // Temperatur
        try{
            String teks = PacsSuhu.getText().trim().replace(",",".");
            if(!teks.isEmpty()){
                double suhu = Double.parseDouble(teks);
                CmbSkorTemperaturPacs.setSelectedItem(String.valueOf(PacsScoring.skorTemperatur(suhu)));
            }
        }catch(NumberFormatException e){
        }

        // Saturasi O2
        try{
            String teks = PacsSaturasi.getText().trim();
            if(!teks.isEmpty()){
                int saturasi = Integer.parseInt(teks);
                CmbSkorSaturasiPacs.setSelectedItem(String.valueOf(PacsScoring.skorSaturasiO2(saturasi)));
            }
        }catch(NumberFormatException e){
        }
        hitungTotalSkorPacs();
        refreshIndikatorSkorPacs();
    }

    /**
     * Stage g: jumlah otomatis 6 skor EWS -> total_skor (dihitung di Java,
     * bukan generated column DB, mengikuti pola project). Kalau salah satu
     * skor masih "?" (nilai input di luar kategori formulir, lihat
     * hitungSkorOtomatisPacs()), Total ikut ditampilkan "?" - tidak dijumlah
     * dengan asumsi 0, supaya tidak menyesatkan.
     */
    private void hitungTotalSkorPacs(){
        widget.ComboBox[] semuaSkor = {CmbSkorKesadaranPacs,CmbSkorTDPacs,CmbSkorNadiPacs,CmbSkorRespirasiPacs,CmbSkorTemperaturPacs,CmbSkorSaturasiPacs};
        int total = 0;
        for(widget.ComboBox cmb : semuaSkor){
            if(cmb.getSelectedItem()==null||"?".equals(cmb.getSelectedItem().toString())){
                LblTotalSkorPacs.setText("?");
                refreshIndikatorSkorPacs();
                refreshBannerTotalPacs();
                refreshSaranLevelPacs();
                return;
            }
            total += Integer.parseInt(cmb.getSelectedItem().toString());
        }
        LblTotalSkorPacs.setText(String.valueOf(total));
        refreshIndikatorSkorPacs();
        refreshBannerTotalPacs();
        refreshSaranLevelPacs();
    }

    /** Stage i: kembalikan seluruh field tab PACS ke kondisi kosong/default (dipanggil dari emptTeks()). */
    private void resetPacsTab(){
        PacsTensi.setText("");
        PacsNadi.setText("");
        PacsRespirasi.setText("");
        PacsSuhu.setText("");
        PacsSaturasi.setText("");
        PacsNyeri.setText("");
        PacsNyeri.setEnabled(true);
        tglNyeriYa.setSelected(true);
        PacsKesadaran.setSelectedIndex(0);
        PacsBeratBadan.setText("");
        PacsTinggiBadan.setText("");
        CmbSkorKesadaranPacs.setSelectedIndex(0);
        CmbSkorTDPacs.setSelectedIndex(0);
        CmbSkorNadiPacs.setSelectedIndex(0);
        CmbSkorRespirasiPacs.setSelectedIndex(0);
        CmbSkorTemperaturPacs.setSelectedIndex(0);
        CmbSkorSaturasiPacs.setSelectedIndex(0);
        LblTotalSkorPacs.setText("0");
        bgLevelPacs.clearSelection();
        bgKeputusanPacs.clearSelection();
        DTPKeputusanPukulPacs.setSekarang();
        // (Tanggal Triase / tanggal_triase tetap diturunkan dari TanggalKunjungan, tidak perlu direset di sini)
        // (Petugas dan Sudah Terpasang/Identitas Pengantar sekarang direset oleh emptTeks(), bukan di sini)
        PacsKeluhanUtama.setText("");
        PacsCatatanKhusus.setText("");
        CekKklTunggal.setSelected(false);
        CekKklDenganLawan.setSelected(false);
        TKklLawan.setText("");
        TKklTempatKejadian.setText("");
        panelKklKejadian.setSekarang();
        CekJatuhKetinggian.setSelected(false);
        TJatuhKetinggianKet.setText("");
        CekLukaBakar.setSelected(false);
        TLukaBakarKet.setText("");
        CekTraumaFisik.setSelected(false);
        TTraumaFisikKet.setText("");
        CekTraumaZatKimia.setSelected(false);
        TTraumaZatKimiaKet.setText("");
        CekTraumaZatLainnya.setSelected(false);
        TTraumaZatLainnyaKet.setText("");
        panelMekanismeTrauma.setVisible(false);
        // [Modernisasi] kembalikan cermin visual ke kondisi kosong
        if(bgAvpuPacs!=null){ bgAvpuPacs.clearSelection(); }
        refreshAvpuPacs();
        refreshIndikatorSkorPacs();
        refreshBannerTotalPacs();
        refreshSaranLevelPacs();
        refreshKartuLevelPacs();
        refreshBungkusKeputusanPacs();
        refreshRingkasanPacs();
    }

    /**
     * [Header tambahan] Muat Sudah Terpasang/Identitas Pengantar LANGSUNG
     * dari data_triase_igd (tanpa JOIN data_triase_pacs) - supaya tetap
     * terisi meski pasien belum pernah punya baris PACS (data_triase_igd
     * selalu ada untuk setiap no_rawat yang tampil di tbTriase).
     */
    private void tampilHeaderTambahan(String noRawat){
        try{
            PreparedStatement psHdr = koneksi.prepareStatement(
                "select terpasang_infus,terpasang_ngt,terpasang_kateter_urin,nama_pengantar,telp_pengantar "+
                "from data_triase_igd where no_rawat=?");
            try{
                psHdr.setString(1,noRawat);
                ResultSet rsHdr = psHdr.executeQuery();
                try{
                    if(rsHdr.next()){
                        PacsInfus.setSelected("Ya".equals(rsHdr.getString("terpasang_infus")));
                        PacsNgt.setSelected("Ya".equals(rsHdr.getString("terpasang_ngt")));
                        PacsKateter.setSelected("Ya".equals(rsHdr.getString("terpasang_kateter_urin")));
                        PacsNamaPengantar.setText(rsHdr.getString("nama_pengantar"));
                        PacsTelpPengantar.setText(rsHdr.getString("telp_pengantar"));
                    }
                } finally {
                    if(rsHdr!=null){ rsHdr.close(); }
                }
            } finally {
                if(psHdr!=null){ psHdr.close(); }
            }
        }catch(Exception e){
            System.out.println("Notif Header Tambahan : "+e);
        }
    }

    /**
     * Stage f/h: SELECT + load data lama data_triase_pacs (join data_triase_igd
     * utk kesadaran/berat_badan/tinggi_badan/tanda vital) saat baris di tbTriase
     * dipilih utk diedit. Dipanggil dari tbTriaseKeyPressed setelah field header
     * bersama (TNoRw/CaraMasuk/dst) terisi. Kalau belum pernah diisi PACS utk
     * no_rawat tsb, tab PACS tetap kosong/default - tidak error.
     */
    private void tampilPacs(String noRawat){
        resetPacsTab();
        try{
            PreparedStatement psPacs = koneksi.prepareStatement(
                "select data_triase_igd.tekanan_darah,data_triase_igd.nadi,data_triase_igd.pernapasan,data_triase_igd.suhu,"+
                "data_triase_igd.saturasi_o2,data_triase_igd.nyeri,data_triase_igd.kesadaran,data_triase_igd.berat_badan,data_triase_igd.tinggi_badan,"+
                "data_triase_igd.kontak_awal_tanggal,data_triase_igd.kontak_awal_pukul,data_triase_igd.terpasang_infus,data_triase_igd.terpasang_ngt,"+
                "data_triase_igd.terpasang_kateter_urin,data_triase_igd.nama_pengantar,data_triase_igd.telp_pengantar,"+
                "data_triase_pacs.skor_kesadaran,data_triase_pacs.skor_tekanan_darah_sistolik,data_triase_pacs.skor_nadi,data_triase_pacs.skor_respirasi,"+
                "data_triase_pacs.skor_temperatur,data_triase_pacs.skor_saturasi_o2,data_triase_pacs.total_skor,data_triase_pacs.level_pacs,"+
                "data_triase_pacs.keputusan_pacs,data_triase_pacs.keputusan_pukul,data_triase_pacs.tanggal_triase,data_triase_pacs.nik,data_triase_pacs.keluhan_utama,data_triase_pacs.catatan_khusus "+
                "from data_triase_pacs inner join data_triase_igd on data_triase_igd.no_rawat=data_triase_pacs.no_rawat "+
                "where data_triase_pacs.no_rawat=?");
            try{
                psPacs.setString(1,noRawat);
                ResultSet rsPacs = psPacs.executeQuery();
                try{
                    if(rsPacs.next()){
                        PacsTensi.setText(rsPacs.getString("tekanan_darah"));
                        PacsNadi.setText(rsPacs.getString("nadi"));
                        PacsRespirasi.setText(rsPacs.getString("pernapasan"));
                        PacsSuhu.setText(rsPacs.getString("suhu"));
                        PacsSaturasi.setText(rsPacs.getString("saturasi_o2"));
                        PacsNyeri.setText(rsPacs.getString("nyeri"));
                        if("0".equals(rsPacs.getString("nyeri"))){
                            tglNyeriTidak.setSelected(true);
                            PacsNyeri.setEnabled(false);
                        }else{
                            tglNyeriYa.setSelected(true);
                            PacsNyeri.setEnabled(true);
                        }
                        if(rsPacs.getString("kesadaran")!=null){
                            PacsKesadaran.setSelectedItem(rsPacs.getString("kesadaran"));
                        }
                        PacsBeratBadan.setText(rsPacs.getString("berat_badan"));
                        PacsTinggiBadan.setText(rsPacs.getString("tinggi_badan"));
                        // (Sudah Terpasang/Identitas Pengantar sekarang dimuat oleh tampilHeaderTambahan(), bukan di sini)
                        PacsKeluhanUtama.setText(rsPacs.getString("keluhan_utama"));
                        PacsCatatanKhusus.setText(rsPacs.getString("catatan_khusus"));
                        CmbSkorKesadaranPacs.setSelectedItem(String.valueOf(rsPacs.getInt("skor_kesadaran")));
                        CmbSkorTDPacs.setSelectedItem(String.valueOf(rsPacs.getInt("skor_tekanan_darah_sistolik")));
                        CmbSkorNadiPacs.setSelectedItem(String.valueOf(rsPacs.getInt("skor_nadi")));
                        CmbSkorRespirasiPacs.setSelectedItem(String.valueOf(rsPacs.getInt("skor_respirasi")));
                        CmbSkorTemperaturPacs.setSelectedItem(String.valueOf(rsPacs.getInt("skor_temperatur")));
                        CmbSkorSaturasiPacs.setSelectedItem(String.valueOf(rsPacs.getInt("skor_saturasi_o2")));
                        LblTotalSkorPacs.setText(String.valueOf(rsPacs.getInt("total_skor")));
                        PacsLevel level = PacsLevel.dariLabel(rsPacs.getString("level_pacs"));
                        if(level==PacsLevel.PACS_1) RbPacs1.setSelected(true);
                        else if(level==PacsLevel.PACS_2) RbPacs2.setSelected(true);
                        else if(level==PacsLevel.PACS_3) RbPacs3.setSelected(true);
                        else if(level==PacsLevel.PACS_4) RbPacs4.setSelected(true);
                        String keput = rsPacs.getString("keputusan_pacs");
                        if("Ruang Resusitasi".equals(keput)) RbPacsResusitasi.setSelected(true);
                        else if("Ruang Non Resusitasi".equals(keput)) RbPacsNonResusitasi.setSelected(true);
                        else if("Poli Klinik/Klinik Umum 24 Jam".equals(keput)) RbPacsPoliKlinik.setSelected(true);
                        else if("Death On Arrival".equals(keput)) RbPacsDOA.setSelected(true);
                        // [Bagian D] Pukul Keputusan - setJamSql sudah menangani null/kosong
                        // dan format tak terduga sendiri (fallback ke jam sekarang).
                        DTPKeputusanPukulPacs.setJamSql(rsPacs.getString("keputusan_pukul"));
                        // (Tanggal/Jam Triase / tanggal_triase TETAP diturunkan dari TanggalKunjungan)
                        // (Petugas PACS sekarang pakai SekunderKodePetugas/SekunderNamaPetugas, tidak dimuat di sini)
                    }
                } finally {
                    if(rsPacs!=null){ rsPacs.close(); }
                }
            } finally {
                if(psPacs!=null){ psPacs.close(); }
            }

            // [Mekanisme Trauma] Query terpisah - baris ini cuma ada kalau Kasus = Trauma.
            PreparedStatement psMt = koneksi.prepareStatement("select * from data_triase_mekanisme_trauma where no_rawat=?");
            try{
                psMt.setString(1,noRawat);
                ResultSet rsMt = psMt.executeQuery();
                try{
                    if(rsMt.next()){
                        CekKklTunggal.setSelected("Ya".equals(rsMt.getString("kkl_tunggal")));
                        CekKklDenganLawan.setSelected("Ya".equals(rsMt.getString("kkl_dengan_lawan")));
                        TKklLawan.setText(rsMt.getString("kkl_lawan"));
                        TKklTempatKejadian.setText(rsMt.getString("kkl_tempat_kejadian"));
                        if(rsMt.getString("kkl_tanggal_kejadian")!=null){
                            String pukul = rsMt.getString("kkl_pukul_kejadian");
                            panelKklKejadian.setNilaiSql(rsMt.getString("kkl_tanggal_kejadian")+" "+(pukul==null?"00:00:00":pukul));
                        }
                        CekJatuhKetinggian.setSelected("Ya".equals(rsMt.getString("jatuh_ketinggian")));
                        TJatuhKetinggianKet.setText(rsMt.getString("jatuh_ketinggian_keterangan"));
                        CekLukaBakar.setSelected("Ya".equals(rsMt.getString("luka_bakar")));
                        TLukaBakarKet.setText(rsMt.getString("luka_bakar_keterangan"));
                        CekTraumaFisik.setSelected("Ya".equals(rsMt.getString("trauma_fisik")));
                        TTraumaFisikKet.setText(rsMt.getString("trauma_fisik_keterangan"));
                        CekTraumaZatKimia.setSelected("Ya".equals(rsMt.getString("trauma_zat_kimia")));
                        TTraumaZatKimiaKet.setText(rsMt.getString("trauma_zat_kimia_keterangan"));
                        CekTraumaZatLainnya.setSelected("Ya".equals(rsMt.getString("trauma_zat_lainnya")));
                        TTraumaZatLainnyaKet.setText(rsMt.getString("trauma_zat_lainnya_keterangan"));
                    }
                } finally {
                    if(rsMt!=null){ rsMt.close(); }
                }
            } finally {
                if(psMt!=null){ psMt.close(); }
            }
        }catch(Exception e){
            System.out.println("Notif PACS : "+e);
        }
        perbaruiVisibilitasMekanismeTrauma();
        // [Modernisasi] sinkronkan cermin visual dgn nilai yg baru dimuat
        syncAvpuDariCombo();
        refreshIndikatorSkorPacs();
        refreshBannerTotalPacs();
        refreshSaranLevelPacs();
        refreshKartuLevelPacs();
        refreshBungkusKeputusanPacs();
        refreshRingkasanPacs();
    }

    /** Stage c: validasi input tab PACS sebelum INSERT/UPDATE. Pola sama seperti validasi Primer/Sekunder (Valid.textKosong). */
    private boolean validasiPacs(){
        boolean valid = true;
        if(PacsTensi.getText().trim().equals("")){
            valid = false;
            Valid.textKosong(PacsTensi,"Tekanan Darah");
        }else if(PacsNadi.getText().trim().equals("")){
            valid = false;
            Valid.textKosong(PacsNadi,"Nadi");
        }else if(PacsRespirasi.getText().trim().equals("")){
            valid = false;
            Valid.textKosong(PacsRespirasi,"Respirasi");
        }else if(PacsSuhu.getText().trim().equals("")){
            valid = false;
            Valid.textKosong(PacsSuhu,"Suhu");
        }else if(PacsSaturasi.getText().trim().equals("")){
            valid = false;
            Valid.textKosong(PacsSaturasi,"Saturasi O2");
        }else if(PacsNyeri.getText().trim().equals("")){
            valid = false;
            Valid.textKosong(PacsNyeri,"Nyeri");
        }else if(!RbPacs1.isSelected()&&!RbPacs2.isSelected()&&!RbPacs3.isSelected()&&!RbPacs4.isSelected()){
            valid = false;
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih Level Triage (PACS) terlebih dahulu...!");
        }else if(!RbPacsResusitasi.isSelected()&&!RbPacsNonResusitasi.isSelected()&&!RbPacsPoliKlinik.isSelected()&&!RbPacsDOA.isSelected()){
            valid = false;
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih Keputusan terlebih dahulu...!");
        }else if(SekunderKodePetugas.getText().trim().equals("")||SekunderNamaPetugas.getText().trim().equals("")){
            valid = false;
            JOptionPane.showMessageDialog(null,"Maaf, Petugas Triase PACS mengikuti Dokter/Petugas di tab Sekunder - silahkan isi terlebih dahulu di tab Sekunder...!");
        }else if("?".equals(LblTotalSkorPacs.getText())){
            valid = false;
            JOptionPane.showMessageDialog(null,"Maaf, Total skor belum bisa dihitung karena ada nilai tanda vital di luar kategori formulir (lihat peringatan sebelumnya). Periksa ulang input sebelum menyimpan...!");
        }
        return valid;
    }

    /**
     * Aturan penentuan PACS = pembacaan pilihan manual petugas (dikonfirmasi
     * pengguna - PACS BUKAN hasil hitungan Total EWS). Logika sesungguhnya
     * ada di PacsLevel.tentukanPacsLevel(), method ini cuma jembatan ke
     * kolom database (level_pacs disimpan sbg label String, lihat sik_triase_pacs.sql).
     */
    private String levelPacsTerpilih(){
        PacsLevel level = PacsLevel.tentukanPacsLevel(RbPacs1.isSelected(),RbPacs2.isSelected(),RbPacs3.isSelected(),RbPacs4.isSelected());
        return level==null?null:level.getLabel();
    }

    private String keputusanPacsTerpilih(){
        if(RbPacsResusitasi.isSelected()) return "Ruang Resusitasi";
        if(RbPacsNonResusitasi.isSelected()) return "Ruang Non Resusitasi";
        if(RbPacsPoliKlinik.isSelected()) return "Poli Klinik/Klinik Umum 24 Jam";
        if(RbPacsDOA.isSelected()) return "Death On Arrival";
        return null;
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,data_triase_igd.tgl_kunjungan,"+
                    "data_triase_igd.cara_masuk,data_triase_igd.alat_transportasi,data_triase_igd.alasan_kedatangan,"+
                    "data_triase_igd.keterangan_kedatangan,data_triase_igd.kode_kasus,master_triase_macam_kasus.macam_kasus "+
                    "from reg_periksa inner join pasien inner join data_triase_igd inner join master_triase_macam_kasus "+
                    "on reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.no_rawat=data_triase_igd.no_rawat "+
                    "and data_triase_igd.kode_kasus=master_triase_macam_kasus.kode_kasus "+
                    "where data_triase_igd.tgl_kunjungan between ? and ? and reg_periksa.no_rawat like ? or "+
                    "data_triase_igd.tgl_kunjungan between ? and ? and pasien.no_rkm_medis like ? or "+
                    "data_triase_igd.tgl_kunjungan between ? and ? and pasien.nm_pasien like ? or "+
                    "data_triase_igd.tgl_kunjungan between ? and ? and data_triase_igd.cara_masuk like ? or "+
                    "data_triase_igd.tgl_kunjungan between ? and ? and data_triase_igd.alat_transportasi like ? or "+
                    "data_triase_igd.tgl_kunjungan between ? and ? and data_triase_igd.alasan_kedatangan like ? or "+
                    "data_triase_igd.tgl_kunjungan between ? and ? and data_triase_igd.keterangan_kedatangan like ? or "+
                    "data_triase_igd.tgl_kunjungan between ? and ? and master_triase_macam_kasus.macam_kasus like ? order by data_triase_igd.tgl_kunjungan");
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                ps.setString(3,"%"+TCari.getText().trim()+"%");
                ps.setString(4,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(5,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                ps.setString(6,"%"+TCari.getText().trim()+"%");
                ps.setString(7,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(8,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                ps.setString(9,"%"+TCari.getText().trim()+"%");
                ps.setString(10,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(11,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                ps.setString(12,"%"+TCari.getText().trim()+"%");
                ps.setString(13,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(14,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                ps.setString(15,"%"+TCari.getText().trim()+"%");
                ps.setString(16,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(17,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                ps.setString(18,"%"+TCari.getText().trim()+"%");
                ps.setString(19,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(20,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                ps.setString(21,"%"+TCari.getText().trim()+"%");
                ps.setString(22,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(23,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                ps.setString(24,"%"+TCari.getText().trim()+"%");
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("tgl_kunjungan"),
                        rs.getString("cara_masuk"),rs.getString("alat_transportasi"),rs.getString("alasan_kedatangan"),
                        rs.getString("keterangan_kedatangan"),rs.getString("kode_kasus"),rs.getString("macam_kasus")
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
    
    private void LoadData(){
        tampilPemeriksaan();
        tampilPemeriksaan2();
        tampil();
    }
    
    private void emptTeks(){
        TNoRw.setText("");
        TPasien.setText("");
        TNoRM.setText("");
        Transportasi.setSelectedIndex(0);
        TanggalKunjungan.setDate(new Date());
        sinkronPanelTglKunjungan();
        AlasanKedatangan.setSelectedIndex(0);
        KdKasus.setText("");
        NmKasus.setText("");
        CaraMasuk.setSelectedIndex(0);
        KeteranganKedatangan.setText("");
        PrimerKeluhanUtama.setText("");
        PrimerSuhu.setText("");
        PrimerNyeri.setText("");
        PrimerTensi.setText("");
        PrimerNadi.setText("");
        PrimerSaturasi.setText("");
        PrimerRespirasi.setText("");
        PrimerKubutuhanKusus.setSelectedIndex(0);
        PrimerCatatan.setText("");
        PrimerTanggalTriase.setDate(new Date());
        SekunderAnamnesa.setText("");
        SekunderSuhu.setText("");
        SekunderNyeri.setText("");
        SekunderSaturasi.setText("");
        SekunderRespirasi.setText("");
        SekunderTensi.setText("");
        SekunderNadi.setText("");
        SekunderCatatan.setText("");
        SekunderTanggalTriase.setDate(new Date());
        // [Header tambahan] Sudah Terpasang/Identitas Pengantar - field bersama Primer/Sekunder/PACS.
        PacsInfus.setSelected(false);
        PacsNgt.setSelected(false);
        PacsKateter.setSelected(false);
        PacsNamaPengantar.setText("");
        PacsTelpPengantar.setText("");
        resetPacsTab();
        TabPilihan.setSelectedIndex(0);
        TabTriase.setSelectedIndex(0);
        Transportasi.requestFocus();
        jmlskala1=0;jmlskala2=0;jmlskala3=0;jmlskala4=0;jmlskala5=0;
        kodepetugas="";
    }
    
    public void setNoRm(String norwt,String norm,String namapasien) {
        emptTeks();
        TNoRw.setText(norwt);
        TNoRM.setText(norm);
        TPasien.setText(namapasien);
        TCari.setText(norwt);   
    }
    
    private void tampilPemeriksaan() {        
        Valid.tabelKosong(tabModePemeriksaan);
        try{
            ps=koneksi.prepareStatement("select * from master_triase_pemeriksaan where kode_pemeriksaan like ? or nama_pemeriksaan like ? order by kode_pemeriksaan");
            try {
                ps.setString(1,"%"+TCariPemeriksaan.getText().trim()+"%");
                ps.setString(2,"%"+TCariPemeriksaan.getText().trim()+"%");
                rs=ps.executeQuery();
                while(rs.next()){
                    tabModePemeriksaan.addRow(new Object[]{rs.getString(1),rs.getString(2)});
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
    }
    
    private void tampilPemeriksaan2() {        
        Valid.tabelKosong(tabModePemeriksaan2);
        try{
            ps=koneksi.prepareStatement("select * from master_triase_pemeriksaan where kode_pemeriksaan like ? or nama_pemeriksaan like ? order by kode_pemeriksaan");
            try {
                ps.setString(1,"%"+TCariPemeriksaan2.getText().trim()+"%");
                ps.setString(2,"%"+TCariPemeriksaan2.getText().trim()+"%");
                rs=ps.executeQuery();
                while(rs.next()){
                    tabModePemeriksaan2.addRow(new Object[]{rs.getString(1),rs.getString(2)});
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
    }
      
    private void tampilskala1() {
        if(tbPemeriksaan.getSelectedRow()!= -1){
            try{
                jml=0;
                for(i=0;i<tbSkala1.getRowCount();i++){
                    if(tbSkala1.getValueAt(i,0).toString().equals("true")){
                        jml++;
                    }
                }
                
                pilih=null;
                pilih=new boolean[jml]; 
                kode=null;
                kode=new String[jml];
                pengkajian=null;
                pengkajian=new String[jml];
                
                index=0;        
                for(i=0;i<tbSkala1.getRowCount();i++){
                    if(tbSkala1.getValueAt(i,0).toString().equals("true")){
                        pilih[index]=true;
                        kode[index]=tbSkala1.getValueAt(i,1).toString();
                        pengkajian[index]=tbSkala1.getValueAt(i,2).toString();
                        index++;
                    }
                } 

                Valid.tabelKosong(tabModeSkala1);
                
                for(i=0;i<jml;i++){
                    tabModeSkala1.addRow(new Object[] {
                        pilih[i],kode[i],pengkajian[i]
                    });
                }
                
                ps=koneksi.prepareStatement(
                        "select * from master_triase_skala1 where kode_pemeriksaan=? and pengkajian_skala1 like ? order by master_triase_skala1.kode_skala1");
                try {
                    ps.setString(1,tbPemeriksaan.getValueAt(tbPemeriksaan.getSelectedRow(),0).toString());
                    ps.setString(2,"%"+TCariSkala1.getText().trim()+"%");
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabModeSkala1.addRow(new Object[]{false,rs.getString("kode_skala1"),rs.getString("pengkajian_skala1")});
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
        }
    }
    
    private void tampilskala2() {
        if(tbPemeriksaan.getSelectedRow()!= -1){
            try{
                jml=0;
                for(i=0;i<tbSkala2.getRowCount();i++){
                    if(tbSkala2.getValueAt(i,0).toString().equals("true")){
                        jml++;
                    }
                }
                
                pilih=null;
                pilih=new boolean[jml]; 
                kode=null;
                kode=new String[jml];
                pengkajian=null;
                pengkajian=new String[jml];
                
                index=0;        
                for(i=0;i<tbSkala2.getRowCount();i++){
                    if(tbSkala2.getValueAt(i,0).toString().equals("true")){
                        pilih[index]=true;
                        kode[index]=tbSkala2.getValueAt(i,1).toString();
                        pengkajian[index]=tbSkala2.getValueAt(i,2).toString();
                        index++;
                    }
                } 

                Valid.tabelKosong(tabModeSkala2);
                
                for(i=0;i<jml;i++){
                    tabModeSkala2.addRow(new Object[] {
                        pilih[i],kode[i],pengkajian[i]
                    });
                }
                
                ps=koneksi.prepareStatement(
                        "select * from master_triase_skala2 where kode_pemeriksaan=? and pengkajian_skala2 like ? order by master_triase_skala2.kode_skala2");
                try {
                    ps.setString(1,tbPemeriksaan.getValueAt(tbPemeriksaan.getSelectedRow(),0).toString());
                    ps.setString(2,"%"+TCariSkala1.getText().trim()+"%");
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabModeSkala2.addRow(new Object[]{false,rs.getString("kode_skala2"),rs.getString("pengkajian_skala2")});
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
        }
    }
    
    private void tampilskala3() {
        if(tbPemeriksaan2.getSelectedRow()!= -1){
            try{
                jml=0;
                for(i=0;i<tbSkala3.getRowCount();i++){
                    if(tbSkala3.getValueAt(i,0).toString().equals("true")){
                        jml++;
                    }
                }
                
                pilih=null;
                pilih=new boolean[jml]; 
                kode=null;
                kode=new String[jml];
                pengkajian=null;
                pengkajian=new String[jml];
                
                index=0;        
                for(i=0;i<tbSkala3.getRowCount();i++){
                    if(tbSkala3.getValueAt(i,0).toString().equals("true")){
                        pilih[index]=true;
                        kode[index]=tbSkala3.getValueAt(i,1).toString();
                        pengkajian[index]=tbSkala3.getValueAt(i,2).toString();
                        index++;
                    }
                } 

                Valid.tabelKosong(tabModeSkala3);
                
                for(i=0;i<jml;i++){
                    tabModeSkala3.addRow(new Object[] {
                        pilih[i],kode[i],pengkajian[i]
                    });
                }
                
                ps=koneksi.prepareStatement(
                        "select * from master_triase_skala3 where kode_pemeriksaan=? and pengkajian_skala3 like ? order by master_triase_skala3.kode_skala3");
                try {
                    ps.setString(1,tbPemeriksaan2.getValueAt(tbPemeriksaan2.getSelectedRow(),0).toString());
                    ps.setString(2,"%"+TCariSkala3.getText().trim()+"%");
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabModeSkala3.addRow(new Object[]{false,rs.getString("kode_skala3"),rs.getString("pengkajian_skala3")});
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
        }
    }
    
    private void tampilskala4() {
        if(tbPemeriksaan2.getSelectedRow()!= -1){
            try{
                jml=0;
                for(i=0;i<tbSkala4.getRowCount();i++){
                    if(tbSkala4.getValueAt(i,0).toString().equals("true")){
                        jml++;
                    }
                }
                
                pilih=null;
                pilih=new boolean[jml]; 
                kode=null;
                kode=new String[jml];
                pengkajian=null;
                pengkajian=new String[jml];
                
                index=0;        
                for(i=0;i<tbSkala4.getRowCount();i++){
                    if(tbSkala4.getValueAt(i,0).toString().equals("true")){
                        pilih[index]=true;
                        kode[index]=tbSkala4.getValueAt(i,1).toString();
                        pengkajian[index]=tbSkala4.getValueAt(i,2).toString();
                        index++;
                    }
                } 

                Valid.tabelKosong(tabModeSkala4);
                
                for(i=0;i<jml;i++){
                    tabModeSkala4.addRow(new Object[] {
                        pilih[i],kode[i],pengkajian[i]
                    });
                }
                
                ps=koneksi.prepareStatement(
                        "select * from master_triase_skala4 where kode_pemeriksaan=? and pengkajian_skala4 like ? order by master_triase_skala4.kode_skala4");
                try {
                    ps.setString(1,tbPemeriksaan2.getValueAt(tbPemeriksaan2.getSelectedRow(),0).toString());
                    ps.setString(2,"%"+TCariSkala3.getText().trim()+"%");
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabModeSkala4.addRow(new Object[]{false,rs.getString("kode_skala4"),rs.getString("pengkajian_skala4")});
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
        }
    }
    
    private void tampilskala5() {
        if(tbPemeriksaan2.getSelectedRow()!= -1){
            try{
                jml=0;
                for(i=0;i<tbSkala5.getRowCount();i++){
                    if(tbSkala5.getValueAt(i,0).toString().equals("true")){
                        jml++;
                    }
                }
                
                pilih=null;
                pilih=new boolean[jml]; 
                kode=null;
                kode=new String[jml];
                pengkajian=null;
                pengkajian=new String[jml];
                
                index=0;        
                for(i=0;i<tbSkala5.getRowCount();i++){
                    if(tbSkala5.getValueAt(i,0).toString().equals("true")){
                        pilih[index]=true;
                        kode[index]=tbSkala5.getValueAt(i,1).toString();
                        pengkajian[index]=tbSkala5.getValueAt(i,2).toString();
                        index++;
                    }
                } 

                Valid.tabelKosong(tabModeSkala5);
                
                for(i=0;i<jml;i++){
                    tabModeSkala5.addRow(new Object[] {
                        pilih[i],kode[i],pengkajian[i]
                    });
                }
                
                ps=koneksi.prepareStatement(
                        "select * from master_triase_skala5 where kode_pemeriksaan=? and pengkajian_skala5 like ? order by master_triase_skala5.kode_skala5");
                try {
                    ps.setString(1,tbPemeriksaan2.getValueAt(tbPemeriksaan2.getSelectedRow(),0).toString());
                    ps.setString(2,"%"+TCariSkala3.getText().trim()+"%");
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabModeSkala5.addRow(new Object[]{false,rs.getString("kode_skala5"),rs.getString("pengkajian_skala5")});
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
        }
    }
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getdata_triase_igd());
        BtnHapus.setEnabled(akses.getdata_triase_igd());
        BtnPrint.setEnabled(akses.getdata_triase_igd());
        BtnEdit.setEnabled(akses.getdata_triase_igd());  
        BtnTambahPemeriksaan.setEnabled(akses.getmaster_triase_pemeriksaan());
        BtnTambahPemeriksaan1.setEnabled(akses.getmaster_triase_pemeriksaan());
        BtnTambahSkala1.setEnabled(akses.getmaster_triase_skala1());
        BtnTambahSkala2.setEnabled(akses.getmaster_triase_skala3());
        
        if(akses.getjml2()>=1){
            btnPrimerPetugas.setEnabled(false);
            btnSekunderPetugas.setEnabled(false);
            PrimerKodePetugas.setText(akses.getkode());
            SekunderKodePetugas.setText(akses.getkode());
            PrimerNamaPetugas.setText(Sequel.CariPegawai(akses.getkode()));
            SekunderNamaPetugas.setText(PrimerNamaPetugas.getText());
        } 
    }
    
    /**
     * Bagian header yg sama utk ketiga jenis triase (Primer/Sekunder/PACS) -
     * field-field yg disimpan bersama di data_triase_igd tapi sebelumnya
     * tidak pernah ditampilkan di panel detail Data Triase (No.RM/Nama/Tgl
     * Lahir cuma tampil di TNoRM1/TPasien1, terpisah dari LoadHTML).
     */
    private String bangunHeaderHtmlTriase(String noRawat){
        StringBuilder header = new StringBuilder();
        PreparedStatement psH = null;
        ResultSet rsH = null;
        try {
            psH = koneksi.prepareStatement(
                "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,data_triase_igd.tgl_kunjungan,"+
                "data_triase_igd.cara_masuk,data_triase_igd.alat_transportasi,data_triase_igd.alasan_kedatangan,"+
                "data_triase_igd.keterangan_kedatangan,data_triase_igd.terpasang_infus,data_triase_igd.terpasang_ngt,"+
                "data_triase_igd.terpasang_kateter_urin,data_triase_igd.nama_pengantar,data_triase_igd.telp_pengantar,"+
                "master_triase_macam_kasus.macam_kasus "+
                "from reg_periksa inner join pasien inner join data_triase_igd inner join master_triase_macam_kasus "+
                "on reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.no_rawat=data_triase_igd.no_rawat "+
                "and data_triase_igd.kode_kasus=master_triase_macam_kasus.kode_kasus "+
                "where data_triase_igd.no_rawat=?");
            psH.setString(1,noRawat);
            rsH = psH.executeQuery();
            if(rsH.next()){
                header.append(
                    "<tr class='isi'>"+
                        "<td valign='middle'>No.RM / Nama Pasien</td>"+
                        "<td valign='middle'>"+rsH.getString("no_rkm_medis")+" - "+rsH.getString("nm_pasien")+"</td>"+
                    "</tr>"+
                    "<tr class='isi'>"+
                        "<td valign='middle'>Tgl Lahir</td>"+
                        "<td valign='middle'>"+(rsH.getString("tgl_lahir")==null?"-":rsH.getString("tgl_lahir"))+"</td>"+
                    "</tr>"+
                    "<tr class='isi'>"+
                        "<td valign='middle'>Tgl Kunjungan</td>"+
                        "<td valign='middle'>"+(rsH.getString("tgl_kunjungan")==null?"-":rsH.getString("tgl_kunjungan"))+"</td>"+
                    "</tr>"+
                    "<tr class='isi'>"+
                        "<td valign='middle'>Cara Masuk / Transportasi</td>"+
                        "<td valign='middle'>"+(rsH.getString("cara_masuk")==null?"-":rsH.getString("cara_masuk"))+" / "+(rsH.getString("alat_transportasi")==null?"-":rsH.getString("alat_transportasi"))+"</td>"+
                    "</tr>"+
                    "<tr class='isi'>"+
                        "<td valign='middle'>Alasan Kedatangan</td>"+
                        "<td valign='middle'>"+(rsH.getString("alasan_kedatangan")==null?"-":rsH.getString("alasan_kedatangan"))+"</td>"+
                    "</tr>"+
                    "<tr class='isi'>"+
                        "<td valign='middle'>Macam Kasus</td>"+
                        "<td valign='middle'>"+(rsH.getString("macam_kasus")==null?"-":rsH.getString("macam_kasus"))+"</td>"+
                    "</tr>"+
                    "<tr class='isi'>"+
                        "<td valign='middle'>Keterangan Kedatangan</td>"+
                        "<td valign='middle'>"+(rsH.getString("keterangan_kedatangan")==null||rsH.getString("keterangan_kedatangan").isEmpty()?"-":rsH.getString("keterangan_kedatangan"))+"</td>"+
                    "</tr>"+
                    "<tr class='isi'>"+
                        "<td valign='middle'>Sudah Terpasang</td>"+
                        "<td valign='middle'>Infus : "+("Ya".equals(rsH.getString("terpasang_infus"))?"Ya":"Tidak")+", NGT : "+("Ya".equals(rsH.getString("terpasang_ngt"))?"Ya":"Tidak")+", Kateter Urin : "+("Ya".equals(rsH.getString("terpasang_kateter_urin"))?"Ya":"Tidak")+"</td>"+
                    "</tr>"+
                    "<tr class='isi'>"+
                        "<td valign='middle'>Identitas Pengantar</td>"+
                        "<td valign='middle'>"+(rsH.getString("nama_pengantar")==null||rsH.getString("nama_pengantar").isEmpty()?"-":rsH.getString("nama_pengantar")+(rsH.getString("telp_pengantar")==null||rsH.getString("telp_pengantar").isEmpty()?"":", "+rsH.getString("telp_pengantar")))+"</td>"+
                    "</tr>"
                );
            }
        } catch (Exception e) {
            System.out.println("Notif header triase : "+e);
        } finally {
            try{ if(rsH!=null){ rsH.close(); } }catch(Exception ex){}
            try{ if(psH!=null){ psH.close(); } }catch(Exception ex){}
        }
        return header.toString();
    }

    private void getData() {
        if(tbTriase.getSelectedRow()!= -1){
            try {
                TNoRM1.setText(tbTriase.getValueAt(tbTriase.getSelectedRow(),1).toString());
                TPasien1.setText(tbTriase.getValueAt(tbTriase.getSelectedRow(),2).toString());
                ps=koneksi.prepareStatement(
                        "select data_triase_igdprimer.keluhan_utama,data_triase_igdprimer.kebutuhan_khusus,data_triase_igdprimer.catatan,"+
                        "data_triase_igdprimer.plan,data_triase_igdprimer.tanggaltriase,data_triase_igdprimer.nik,data_triase_igd.tekanan_darah,"+
                        "data_triase_igd.nadi,data_triase_igd.pernapasan,data_triase_igd.suhu,data_triase_igd.saturasi_o2,data_triase_igd.nyeri,"+
                        "data_triase_igd.no_rawat from data_triase_igdprimer inner join data_triase_igd on data_triase_igd.no_rawat="+
                        "data_triase_igdprimer.no_rawat where data_triase_igd.no_rawat=?");
                try {
                    ps.setString(1,tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    if(rs.next()){
                        kodepetugas=rs.getString("nik");
                        htmlContent = new StringBuilder();
                        htmlContent.append(
                            "<tr class='isi'>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='35%'>Keterangan</td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='65%'>Triase Primer</td>"+
                            "</tr>"
                        );
                        htmlContent.append(bangunHeaderHtmlTriase(rs.getString("no_rawat")));
                        htmlContent.append(
                            "<tr class='isi'>"+
                                "<td valign='middle'>Keluhan Utama</td>"+
                                "<td valign='middle'>"+rs.getString("keluhan_utama").replaceAll("(\r\n|\r|\n|\n\r)","<br>")+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Tanda Vital</td>"+
                                "<td valign='middle'>Suhu (C) : "+rs.getString("suhu")+", Nyeri : "+rs.getString("nyeri")+", Tensi : "+rs.getString("tekanan_darah")+", Nadi(/menit) : "+rs.getString("nadi")+", Saturasi O²(%) : "+rs.getString("saturasi_o2")+", Respirasi(/menit) : "+rs.getString("pernapasan")+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Kebutuhan Khusus</td>"+
                                "<td valign='middle'>"+rs.getString("kebutuhan_khusus")+"</td>"+
                            "</tr>"
                        );
                        
                        ps2=koneksi.prepareStatement(
                                "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                "from master_triase_pemeriksaan inner join master_triase_skala1 inner join data_triase_igddetail_skala1 "+
                                "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala1.kode_pemeriksaan and "+
                                "master_triase_skala1.kode_skala1=data_triase_igddetail_skala1.kode_skala1 where data_triase_igddetail_skala1.no_rawat=? "+
                                "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                htmlContent.append(                             
                                    "<tr class='isi'>"+
                                        "<td valign='middle' bgcolor='#FFFAF8' align='center'>Pemeriksaan</td>"+
                                        "<td valign='middle' bgcolor='#AA0000' color='ffffff' align='center'>Immediate/Segera</td>"+
                                    "</tr>");
                                rs2.beforeFirst();
                                while(rs2.next()){
                                    htmlContent.append(                             
                                        "<tr class='isi'>"+
                                            "<td valign='middle'>"+rs2.getString("nama_pemeriksaan")+"</td>"+
                                            "<td valign='middle' bgcolor='#AA0000' color='ffffff'>"+
                                                "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0'>"
                                    );
                                    ps3=koneksi.prepareStatement(
                                            "select master_triase_skala1.pengkajian_skala1 from master_triase_skala1 inner join data_triase_igddetail_skala1 "+
                                            "on master_triase_skala1.kode_skala1=data_triase_igddetail_skala1.kode_skala1 where "+
                                            "master_triase_skala1.kode_pemeriksaan=? and data_triase_igddetail_skala1.no_rawat=? "+
                                            "order by data_triase_igddetail_skala1.kode_skala1");
                                    try {
                                        ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                        ps3.setString(2,rs.getString("no_rawat"));
                                        rs3=ps3.executeQuery();
                                        while(rs3.next()){
                                            htmlContent.append(                             
                                                "<tr class='isi'>"+
                                                    "<td border='0' valign='middle' bgcolor='#AA0000' color='ffffff' width='100%'>"+rs3.getString("pengkajian_skala1")+"</td>"+
                                                "</tr>"
                                            );
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rs3!=null){
                                            rs3.close();
                                        }
                                        if(ps3!=null){
                                            ps3.close();
                                        }
                                    }
                                    htmlContent.append(
                                                "</table>"+
                                            "</td>"+
                                        "</tr>"
                                    );
                                }
                                keputusan="#AA0000";
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
                        
                        ps2=koneksi.prepareStatement(
                                "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                "from master_triase_pemeriksaan inner join master_triase_skala2 inner join data_triase_igddetail_skala2 "+
                                "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala2.kode_pemeriksaan and "+
                                "master_triase_skala2.kode_skala2=data_triase_igddetail_skala2.kode_skala2 where data_triase_igddetail_skala2.no_rawat=? "+
                                "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                htmlContent.append(                             
                                    "<tr class='isi'>"+
                                        "<td valign='middle' bgcolor='#FFFAF8' align='center'>Pemeriksaan</td>"+
                                        "<td valign='middle' bgcolor='#FF0000' color='ffffff' align='center'>Emergensi</td>"+
                                    "</tr>");
                                rs2.beforeFirst();
                                while(rs2.next()){
                                    htmlContent.append(                             
                                        "<tr class='isi'>"+
                                            "<td valign='middle'>"+rs2.getString("nama_pemeriksaan")+"</td>"+
                                            "<td valign='middle' bgcolor='#FF0000' color='ffffff'>"+
                                                "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0'>"
                                    );
                                    ps3=koneksi.prepareStatement(
                                            "select master_triase_skala2.pengkajian_skala2 from master_triase_skala2 inner join data_triase_igddetail_skala2 "+
                                            "on master_triase_skala2.kode_skala2=data_triase_igddetail_skala2.kode_skala2 where "+
                                            "master_triase_skala2.kode_pemeriksaan=? and data_triase_igddetail_skala2.no_rawat=? "+
                                            "order by data_triase_igddetail_skala2.kode_skala2");
                                    try {
                                        ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                        ps3.setString(2,rs.getString("no_rawat"));
                                        rs3=ps3.executeQuery();
                                        while(rs3.next()){
                                            htmlContent.append(                             
                                                "<tr class='isi'>"+
                                                    "<td border='0' valign='middle' bgcolor='#FF0000' color='ffffff' width='100%'>"+rs3.getString("pengkajian_skala2")+"</td>"+
                                                "</tr>"
                                            );
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rs3!=null){
                                            rs3.close();
                                        }
                                        if(ps3!=null){
                                            ps3.close();
                                        }
                                    }
                                    htmlContent.append(
                                                "</table>"+
                                            "</td>"+
                                        "</tr>"
                                    );
                                }
                                keputusan="#FF0000";
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
                                "<td valign='middle'>Plan/Keputusan</td>"+
                                "<td valign='middle' bgcolor='"+keputusan+"' color='ffffff'>Zona Merah "+rs.getString("plan")+"</td>"+
                            "</tr>"+                       
                            "<tr class='isi'>"+
                                "<td valign='middle'>&nbsp;</td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center'>Dokter/Petugas Triase</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Tanggal & Jam</td>"+
                                "<td valign='middle'>"+rs.getString("tanggaltriase")+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Catatan</td>"+
                                "<td valign='middle'>"+rs.getString("catatan")+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Nama Dokter/Petugas</td>"+
                                "<td valign='middle'>"+rs.getString("nik")+" "+Sequel.cariIsi("select pegawai.nama from pegawai where pegawai.nik=?",rs.getString("nik"))+"</td>"+
                            "</tr>"
                        );
                        
                        LoadHTML.setText(
                            "<html>"+
                              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                               htmlContent.toString()+
                              "</table>"+
                            "</html>");
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps!=null){
                        ps.close();
                    }
                }
                
                ps=koneksi.prepareStatement(
                        "select data_triase_igdsekunder.anamnesa_singkat,data_triase_igdsekunder.catatan,"+
                        "data_triase_igdsekunder.plan,data_triase_igdsekunder.tanggaltriase,data_triase_igdsekunder.nik,data_triase_igd.tekanan_darah,"+
                        "data_triase_igd.nadi,data_triase_igd.pernapasan,data_triase_igd.suhu,data_triase_igd.saturasi_o2,data_triase_igd.nyeri,"+
                        "data_triase_igd.no_rawat from data_triase_igdsekunder inner join data_triase_igd on data_triase_igd.no_rawat="+
                        "data_triase_igdsekunder.no_rawat where data_triase_igd.no_rawat=?");
                try {
                    ps.setString(1,tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    if(rs.next()){
                        kodepetugas=rs.getString("nik");
                        htmlContent = new StringBuilder();
                        htmlContent.append(
                            "<tr class='isi'>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='35%'>Keterangan</td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='65%'>Triase Sekunder</td>"+
                            "</tr>"
                        );
                        htmlContent.append(bangunHeaderHtmlTriase(rs.getString("no_rawat")));
                        htmlContent.append(
                            "<tr class='isi'>"+
                                "<td valign='middle'>Anamnesa Singkat</td>"+
                                "<td valign='middle'>"+rs.getString("anamnesa_singkat").replaceAll("(\r\n|\r|\n|\n\r)","<br>")+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Tanda Vital</td>"+
                                "<td valign='middle'>Suhu (C) : "+rs.getString("suhu")+", Nyeri : "+rs.getString("nyeri")+", Tensi : "+rs.getString("tekanan_darah")+", Nadi(/menit) : "+rs.getString("nadi")+", Saturasi O²(%) : "+rs.getString("saturasi_o2")+", Respirasi(/menit) : "+rs.getString("pernapasan")+"</td>"+
                            "</tr>"
                        );
                        
                        ps2=koneksi.prepareStatement(
                                "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                "from master_triase_pemeriksaan inner join master_triase_skala3 inner join data_triase_igddetail_skala3 "+
                                "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala3.kode_pemeriksaan and "+
                                "master_triase_skala3.kode_skala3=data_triase_igddetail_skala3.kode_skala3 where data_triase_igddetail_skala3.no_rawat=? "+
                                "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                htmlContent.append(                             
                                    "<tr class='isi'>"+
                                        "<td valign='middle' bgcolor='#FFFAF8' align='center'>Pemeriksaan</td>"+
                                        "<td valign='middle' bgcolor='#C8C800' color='ffffff' align='center'>Urgensi</td>"+
                                    "</tr>");
                                rs2.beforeFirst();
                                while(rs2.next()){
                                    htmlContent.append(                             
                                        "<tr class='isi'>"+
                                            "<td valign='middle'>"+rs2.getString("nama_pemeriksaan")+"</td>"+
                                            "<td valign='middle' bgcolor='#C8C800' color='ffffff'>"+
                                                "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0'>"
                                    );
                                    ps3=koneksi.prepareStatement(
                                            "select master_triase_skala3.pengkajian_skala3 from master_triase_skala3 inner join data_triase_igddetail_skala3 "+
                                            "on master_triase_skala3.kode_skala3=data_triase_igddetail_skala3.kode_skala3 where "+
                                            "master_triase_skala3.kode_pemeriksaan=? and data_triase_igddetail_skala3.no_rawat=? "+
                                            "order by data_triase_igddetail_skala3.kode_skala3");
                                    try {
                                        ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                        ps3.setString(2,rs.getString("no_rawat"));
                                        rs3=ps3.executeQuery();
                                        while(rs3.next()){
                                            htmlContent.append(                             
                                                "<tr class='isi'>"+
                                                    "<td border='0' valign='middle' bgcolor='#C8C800' color='ffffff' width='100%'>"+rs3.getString("pengkajian_skala3")+"</td>"+
                                                "</tr>"
                                            );
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rs3!=null){
                                            rs3.close();
                                        }
                                        if(ps3!=null){
                                            ps3.close();
                                        }
                                    }
                                    htmlContent.append(
                                                "</table>"+
                                            "</td>"+
                                        "</tr>"
                                    );
                                }
                                keputusan="#C8C800";
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
                        
                        ps2=koneksi.prepareStatement(
                                "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                "from master_triase_pemeriksaan inner join master_triase_skala4 inner join data_triase_igddetail_skala4 "+
                                "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala4.kode_pemeriksaan and "+
                                "master_triase_skala4.kode_skala4=data_triase_igddetail_skala4.kode_skala4 where data_triase_igddetail_skala4.no_rawat=? "+
                                "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                htmlContent.append(                             
                                    "<tr class='isi'>"+
                                        "<td valign='middle' bgcolor='#FFFAF8' align='center'>Pemeriksaan</td>"+
                                        "<td valign='middle' bgcolor='#00AA00' color='ffffff' align='center'>Semi Urgensi/Urgensi Rendah</td>"+
                                    "</tr>");
                                rs2.beforeFirst();
                                while(rs2.next()){
                                    htmlContent.append(                             
                                        "<tr class='isi'>"+
                                            "<td valign='middle'>"+rs2.getString("nama_pemeriksaan")+"</td>"+
                                            "<td valign='middle' bgcolor='#00AA00' color='ffffff'>"+
                                                "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0'>"
                                    );
                                    ps3=koneksi.prepareStatement(
                                            "select master_triase_skala4.pengkajian_skala4 from master_triase_skala4 inner join data_triase_igddetail_skala4 "+
                                            "on master_triase_skala4.kode_skala4=data_triase_igddetail_skala4.kode_skala4 where "+
                                            "master_triase_skala4.kode_pemeriksaan=? and data_triase_igddetail_skala4.no_rawat=? "+
                                            "order by data_triase_igddetail_skala4.kode_skala4");
                                    try {
                                        ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                        ps3.setString(2,rs.getString("no_rawat"));
                                        rs3=ps3.executeQuery();
                                        while(rs3.next()){
                                            htmlContent.append(                             
                                                "<tr class='isi'>"+
                                                    "<td border='0' valign='middle' bgcolor='#00AA00' color='ffffff' width='100%'>"+rs3.getString("pengkajian_skala4")+"</td>"+
                                                "</tr>"
                                            );
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rs3!=null){
                                            rs3.close();
                                        }
                                        if(ps3!=null){
                                            ps3.close();
                                        }
                                    }
                                    htmlContent.append(
                                                "</table>"+
                                            "</td>"+
                                        "</tr>"
                                    );
                                }
                                keputusan="#00AA00";
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
                        
                        ps2=koneksi.prepareStatement(
                                "select master_triase_pemeriksaan.kode_pemeriksaan,master_triase_pemeriksaan.nama_pemeriksaan "+
                                "from master_triase_pemeriksaan inner join master_triase_skala5 inner join data_triase_igddetail_skala5 "+
                                "on master_triase_pemeriksaan.kode_pemeriksaan=master_triase_skala5.kode_pemeriksaan and "+
                                "master_triase_skala5.kode_skala5=data_triase_igddetail_skala5.kode_skala5 where data_triase_igddetail_skala5.no_rawat=? "+
                                "group by master_triase_pemeriksaan.kode_pemeriksaan order by master_triase_pemeriksaan.kode_pemeriksaan");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                htmlContent.append(                             
                                    "<tr class='isi'>"+
                                        "<td valign='middle' bgcolor='#FFFAF8' align='center'>Pemeriksaan</td>"+
                                        "<td valign='middle' bgcolor='#969696' color='ffffff' align='center'>Non Urgensi</td>"+
                                    "</tr>");
                                rs2.beforeFirst();
                                while(rs2.next()){
                                    htmlContent.append(                             
                                        "<tr class='isi'>"+
                                            "<td valign='middle'>"+rs2.getString("nama_pemeriksaan")+"</td>"+
                                            "<td valign='middle' bgcolor='#969696' color='ffffff'>"+
                                                "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0'>"
                                    );
                                    ps3=koneksi.prepareStatement(
                                            "select master_triase_skala5.pengkajian_skala5 from master_triase_skala5 inner join data_triase_igddetail_skala5 "+
                                            "on master_triase_skala5.kode_skala5=data_triase_igddetail_skala5.kode_skala5 where "+
                                            "master_triase_skala5.kode_pemeriksaan=? and data_triase_igddetail_skala5.no_rawat=? "+
                                            "order by data_triase_igddetail_skala5.kode_skala5");
                                    try {
                                        ps3.setString(1,rs2.getString("kode_pemeriksaan"));
                                        ps3.setString(2,rs.getString("no_rawat"));
                                        rs3=ps3.executeQuery();
                                        while(rs3.next()){
                                            htmlContent.append(                             
                                                "<tr class='isi'>"+
                                                    "<td border='0' valign='middle' bgcolor='#969696' color='ffffff' width='100%'>"+rs3.getString("pengkajian_skala5")+"</td>"+
                                                "</tr>"
                                            );
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rs3!=null){
                                            rs3.close();
                                        }
                                        if(ps3!=null){
                                            ps3.close();
                                        }
                                    }
                                    htmlContent.append(
                                                "</table>"+
                                            "</td>"+
                                        "</tr>"
                                    );
                                }
                                keputusan="#969696";
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
                                "<td valign='middle'>Plan/Keputusan</td>"+
                                "<td valign='middle' bgcolor='"+keputusan+"' color='ffffff'>"+rs.getString("plan")+"</td>"+
                            "</tr>"
                        );
                        htmlContent.append(                             
                            "<tr class='isi'>"+
                                "<td valign='middle'>&nbsp;</td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center'>Dokter/Petugas Triase</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Tanggal & Jam</td>"+
                                "<td valign='middle'>"+rs.getString("tanggaltriase")+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Catatan</td>"+
                                "<td valign='middle'>"+rs.getString("catatan")+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Nama Dokter/Petugas</td>"+
                                "<td valign='middle'>"+rs.getString("nik")+" "+Sequel.cariIsi("select pegawai.nama from pegawai where pegawai.nik=?",rs.getString("nik"))+"</td>"+
                            "</tr>"
                        );
                        
                        LoadHTML.setText(
                            "<html>"+
                              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                               htmlContent.toString()+
                              "</table>"+
                            "</html>");
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps!=null){
                        ps.close();
                    }
                }

                // [PACS] Kalau pasien ini punya data PACS, tampilkan itu di panel detail
                // (menggantikan tampilan Primer/Sekunder di atas kalau ada, karena blok ini
                // dijalankan terakhir) - supaya dokter bisa lihat hasil triase PACS langsung
                // di layar tanpa perlu cetak fisik.
                ps=koneksi.prepareStatement(
                        "select data_triase_pacs.keluhan_utama,data_triase_pacs.skor_kesadaran,data_triase_pacs.skor_tekanan_darah_sistolik,"+
                        "data_triase_pacs.skor_nadi,data_triase_pacs.skor_respirasi,data_triase_pacs.skor_temperatur,data_triase_pacs.skor_saturasi_o2,"+
                        "data_triase_pacs.total_skor,data_triase_pacs.level_pacs,data_triase_pacs.keputusan_pacs,"+
                        "data_triase_pacs.tanggal_triase,data_triase_pacs.nik,data_triase_pacs.catatan_khusus,"+
                        "data_triase_igd.tekanan_darah,data_triase_igd.nadi,data_triase_igd.pernapasan,data_triase_igd.suhu,data_triase_igd.saturasi_o2,data_triase_igd.nyeri,"+
                        "data_triase_igd.kesadaran,data_triase_igd.berat_badan,data_triase_igd.tinggi_badan,data_triase_igd.no_rawat "+
                        "from data_triase_pacs inner join data_triase_igd on data_triase_igd.no_rawat=data_triase_pacs.no_rawat "+
                        "where data_triase_igd.no_rawat=?");
                try {
                    ps.setString(1,tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    if(rs.next()){
                        kodepetugas=rs.getString("nik");
                        htmlContent = new StringBuilder();
                        htmlContent.append(
                            "<tr class='isi'>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='35%'>Keterangan</td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center' width='65%'>Triase PACS</td>"+
                            "</tr>"
                        );
                        htmlContent.append(bangunHeaderHtmlTriase(rs.getString("no_rawat")));
                        htmlContent.append(
                            "<tr class='isi'>"+
                                "<td valign='middle'>Keluhan Utama</td>"+
                                "<td valign='middle'>"+(rs.getString("keluhan_utama")==null?"":rs.getString("keluhan_utama").replaceAll("(\r\n|\r|\n|\n\r)","<br>"))+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Tanda Vital</td>"+
                                "<td valign='middle'>Suhu (C) : "+rs.getString("suhu")+", Nyeri : "+rs.getString("nyeri")+", Tensi : "+rs.getString("tekanan_darah")+", Nadi(/menit) : "+rs.getString("nadi")+", Saturasi O²(%) : "+rs.getString("saturasi_o2")+", Respirasi(/menit) : "+rs.getString("pernapasan")+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Kesadaran</td>"+
                                "<td valign='middle'>"+(rs.getString("kesadaran")==null?"-":rs.getString("kesadaran"))+", Berat Badan (kg) : "+(rs.getString("berat_badan")==null?"-":rs.getString("berat_badan"))+", Tinggi Badan (cm) : "+(rs.getString("tinggi_badan")==null?"-":rs.getString("tinggi_badan"))+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Skor EWS</td>"+
                                "<td valign='middle'>Kesadaran : "+rs.getInt("skor_kesadaran")+", TD Sistolik : "+rs.getInt("skor_tekanan_darah_sistolik")+", Nadi : "+rs.getInt("skor_nadi")+", Respirasi : "+rs.getInt("skor_respirasi")+", Temperatur : "+rs.getInt("skor_temperatur")+", Saturasi O2 : "+rs.getInt("skor_saturasi_o2")+", Total : "+rs.getInt("total_skor")+"</td>"+
                            "</tr>"
                        );

                        PacsLevel level = PacsLevel.dariLabel(rs.getString("level_pacs"));
                        String warnaLevel = level==null?"#FFFAF8":level.getWarnaCetakHex();
                        htmlContent.append(
                            "<tr class='isi'>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center'>Level Triage</td>"+
                                "<td valign='middle' bgcolor='"+warnaLevel+"' color='ffffff' align='center'>"+(level==null?"-":level.getLabel())+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Plan/Keputusan</td>"+
                                "<td valign='middle' bgcolor='"+warnaLevel+"' color='ffffff'>"+(rs.getString("keputusan_pacs")==null?"-":rs.getString("keputusan_pacs"))+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Catatan Khusus</td>"+
                                "<td valign='middle'>"+(rs.getString("catatan_khusus")==null||rs.getString("catatan_khusus").isEmpty()?"-":rs.getString("catatan_khusus").replaceAll("(\r\n|\r|\n|\n\r)","<br>"))+"</td>"+
                            "</tr>"
                        );

                        // [Mekanisme Trauma] Query terpisah - hanya tampil kalau ada baris tersimpan (kasus Trauma).
                        PreparedStatement psMt = koneksi.prepareStatement("select * from data_triase_mekanisme_trauma where no_rawat=?");
                        try{
                            psMt.setString(1,rs.getString("no_rawat"));
                            ResultSet rsMt = psMt.executeQuery();
                            try{
                                if(rsMt.next()){
                                    StringBuilder mekanisme = new StringBuilder();
                                    if("Ya".equals(rsMt.getString("kkl_tunggal"))){ mekanisme.append("KKL Tunggal; "); }
                                    if("Ya".equals(rsMt.getString("kkl_dengan_lawan"))){ mekanisme.append("KKL dengan Lawan ("+rsMt.getString("kkl_lawan")+"); "); }
                                    if("Ya".equals(rsMt.getString("jatuh_ketinggian"))){ mekanisme.append("Jatuh dari Ketinggian ("+rsMt.getString("jatuh_ketinggian_keterangan")+"); "); }
                                    if("Ya".equals(rsMt.getString("luka_bakar"))){ mekanisme.append("Luka Bakar ("+rsMt.getString("luka_bakar_keterangan")+"); "); }
                                    if("Ya".equals(rsMt.getString("trauma_fisik"))){ mekanisme.append("Trauma Fisik ("+rsMt.getString("trauma_fisik_keterangan")+"); "); }
                                    if("Ya".equals(rsMt.getString("trauma_zat_kimia"))){ mekanisme.append("Trauma Zat Kimia ("+rsMt.getString("trauma_zat_kimia_keterangan")+"); "); }
                                    if("Ya".equals(rsMt.getString("trauma_zat_lainnya"))){ mekanisme.append("Trauma Zat Lainnya ("+rsMt.getString("trauma_zat_lainnya_keterangan")+"); "); }
                                    if(mekanisme.length()>0){
                                        htmlContent.append(
                                            "<tr class='isi'>"+
                                                "<td valign='middle'>Mekanisme Trauma</td>"+
                                                "<td valign='middle'>"+mekanisme.toString()+"</td>"+
                                            "</tr>"+
                                            "<tr class='isi'>"+
                                                "<td valign='middle'>Tempat Kejadian</td>"+
                                                "<td valign='middle'>"+(rsMt.getString("kkl_tempat_kejadian")==null||rsMt.getString("kkl_tempat_kejadian").isEmpty()?"-":rsMt.getString("kkl_tempat_kejadian"))+"</td>"+
                                            "</tr>"+
                                            "<tr class='isi'>"+
                                                "<td valign='middle'>Tanggal &amp; Pukul Kejadian</td>"+
                                                "<td valign='middle'>"+(rsMt.getString("kkl_tanggal_kejadian")==null?"-":rsMt.getString("kkl_tanggal_kejadian"))+(rsMt.getString("kkl_pukul_kejadian")==null?"":" "+rsMt.getString("kkl_pukul_kejadian"))+"</td>"+
                                            "</tr>"
                                        );
                                    }
                                }
                            } finally {
                                if(rsMt!=null){ rsMt.close(); }
                            }
                        } finally {
                            if(psMt!=null){ psMt.close(); }
                        }

                        htmlContent.append(
                            "<tr class='isi'>"+
                                "<td valign='middle'>&nbsp;</td>"+
                                "<td valign='middle' bgcolor='#FFFAF8' align='center'>Dokter/Petugas Triase</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Tanggal & Jam</td>"+
                                "<td valign='middle'>"+rs.getString("tanggal_triase")+"</td>"+
                            "</tr>"+
                            "<tr class='isi'>"+
                                "<td valign='middle'>Nama Dokter/Petugas</td>"+
                                "<td valign='middle'>"+rs.getString("nik")+" "+Sequel.cariIsi("select pegawai.nama from pegawai where pegawai.nik=?",rs.getString("nik"))+"</td>"+
                            "</tr>"
                        );

                        LoadHTML.setText(
                            "<html>"+
                              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                               htmlContent.toString()+
                              "</table>"+
                            "</html>");
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi PACS : "+e);
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

    private void hapus() {
        Sequel.meghapus("data_triase_igd","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igdprimer","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igdsekunder","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igddetail_skala1","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igddetail_skala2","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igddetail_skala3","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igddetail_skala4","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igddetail_skala5","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        runBackground(() ->tampil());
        TNoRM1.setText("");
        TPasien1.setText("");
        LoadHTML.setText("");
        ChkAccor.setSelected(false);
        isMenu();
    }

    private void ganti() {
        Sequel.AutoComitFalse();
        sukses=true;
        // [PACS] Guard: blok hapus di bawah ini HANYA utk jalur Primer/Sekunder lama
        // (mutually-exclusive by design). PACS adalah model baru yang berdampingan,
        // bukan menggantikan - jadi TIDAK ikut dihapus saat tab PACS yang sedang diedit,
        // supaya data Primer/Sekunder existing tidak hilang tanpa sengaja.
        if(TabTriase.getSelectedIndex()!=2){
        Sequel.meghapus("data_triase_igdprimer","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igddetail_skala1","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igddetail_skala2","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igdsekunder","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igddetail_skala3","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igddetail_skala4","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        Sequel.meghapus("data_triase_igddetail_skala5","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
        }
        if(TabTriase.getSelectedIndex()==0){
            jmlskala1=0;jmlskala2=0;
            for(i=0;i<tbSkala1.getRowCount();i++){ 
                if(tbSkala1.getValueAt(i,0).toString().equals("true")){
                    jmlskala1++;
                }
            }
            for(i=0;i<tbSkala2.getRowCount();i++){ 
                if(tbSkala2.getValueAt(i,0).toString().equals("true")){
                    jmlskala2++;
                }
            }
            if(PrimerKeluhanUtama.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(PrimerKeluhanUtama,"Keluhan Utama");
            }else if(PrimerSuhu.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(PrimerSuhu,"Suhu");
            }else if(PrimerNyeri.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(PrimerNyeri,"Nyeri");
            }else if(PrimerTensi.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(PrimerTensi,"Tensi");
            }else if(PrimerNadi.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(PrimerNadi,"Nadi");
            }else if(PrimerSaturasi.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(PrimerSaturasi,"Saturasi O²");
            }else if(PrimerRespirasi.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(PrimerRespirasi,"Respirasi");
            }else if(PrimerCatatan.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(PrimerCatatan,"Catatan");
            }else if(PrimerKodePetugas.getText().trim().equals("")||PrimerNamaPetugas.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(btnPrimerPetugas,"Dokter/Petugas Triase");
            }else if((jmlskala1==0)&&(jmlskala2==0)){
                sukses=false;
                Valid.textKosong(TCariPemeriksaan,"Skala 1 / Skala 2");
            }else{
                if(Sequel.mengedittf("data_triase_igd","no_rawat=?","no_rawat=?,tgl_kunjungan=?,cara_masuk=?,alat_transportasi=?,alasan_kedatangan=?,keterangan_kedatangan=?,kode_kasus=?,tekanan_darah=?,nadi=?,pernapasan=?,suhu=?,saturasi_o2=?,nyeri=?,terpasang_infus=?,terpasang_ngt=?,terpasang_kateter_urin=?,nama_pengantar=?,telp_pengantar=?",19,new String[]{
                        TNoRw.getText(),Valid.SetTgl(TanggalKunjungan.getSelectedItem()+"")+" "+TanggalKunjungan.getSelectedItem().toString().substring(11,19),
                        CaraMasuk.getSelectedItem().toString(),Transportasi.getSelectedItem().toString(),AlasanKedatangan.getSelectedItem().toString(),
                        KeteranganKedatangan.getText(),KdKasus.getText(),PrimerTensi.getText(),PrimerNadi.getText(),PrimerRespirasi.getText(),PrimerSuhu.getText(),
                        PrimerSaturasi.getText(),PrimerNyeri.getText(),
                        PacsInfus.isSelected()?"Ya":"Tidak",PacsNgt.isSelected()?"Ya":"Tidak",PacsKateter.isSelected()?"Ya":"Tidak",
                        PacsNamaPengantar.getText(),PacsTelpPengantar.getText(),
                        tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString()
                    })==true){
                    if(PrimerResusitasi.isSelected()==true){
                        keputusan="Ruang Resusitasi";
                    }else if(PrimerKritis.isSelected()==true){
                        keputusan="Ruang Kritis";
                    }
                    if(Sequel.menyimpantf2("data_triase_igdprimer","?,?,?,?,?,?,?", 7,new String[]{
                        TNoRw.getText(),PrimerKeluhanUtama.getText(),PrimerKubutuhanKusus.getSelectedItem().toString(),PrimerCatatan.getText(),keputusan,
                        Valid.SetTgl(PrimerTanggalTriase.getSelectedItem()+"")+" "+PrimerTanggalTriase.getSelectedItem().toString().substring(11,19), 
                        PrimerKodePetugas.getText()
                    })==true){
                        if(TabSkala1dan2.getSelectedIndex()==0){
                            for(i=0;i<tbSkala1.getRowCount();i++){ 
                                if(tbSkala1.getValueAt(i,0).toString().equals("true")){
                                    if(Sequel.menyimpantf2("data_triase_igddetail_skala1","?,?","Skala 1",2,new String[]{
                                            TNoRw.getText(),tbSkala1.getValueAt(i,1).toString()
                                        })==true){
                                            tbSkala1.setValueAt(false,i,0);
                                    }else{
                                        sukses=false;
                                    }
                                }
                            }
                        }else if(TabSkala1dan2.getSelectedIndex()==1){
                            for(i=0;i<tbSkala2.getRowCount();i++){ 
                                if(tbSkala2.getValueAt(i,0).toString().equals("true")){
                                    if(Sequel.menyimpantf2("data_triase_igddetail_skala2","?,?","Skala 2",2,new String[]{
                                            TNoRw.getText(),tbSkala2.getValueAt(i,1).toString()
                                        })==true){
                                            tbSkala2.setValueAt(false,i,0);
                                    }else{
                                        sukses=false;
                                    }
                                }
                            }
                        }
                    }else{
                        sukses=false;
                    }
                }else{
                    sukses=false;
                }
            }
        }else if(TabTriase.getSelectedIndex()==1){
            jmlskala3=0;jmlskala4=0;jmlskala5=0;
            for(i=0;i<tbSkala3.getRowCount();i++){ 
                if(tbSkala3.getValueAt(i,0).toString().equals("true")){
                    jmlskala3++;
                }
            }
            for(i=0;i<tbSkala4.getRowCount();i++){ 
                if(tbSkala4.getValueAt(i,0).toString().equals("true")){
                    jmlskala4++;
                }
            }
            for(i=0;i<tbSkala5.getRowCount();i++){ 
                if(tbSkala5.getValueAt(i,0).toString().equals("true")){
                    jmlskala5++;
                }
            }
            if(SekunderAnamnesa.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(SekunderAnamnesa,"Anamnesa");
            }else if(SekunderSuhu.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(SekunderSuhu,"Suhu");
            }else if(SekunderNyeri.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(SekunderNyeri,"Nyeri");
            }else if(SekunderTensi.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(SekunderTensi,"Tensi");
            }else if(SekunderNadi.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(SekunderNadi,"Nadi");
            }else if(SekunderSaturasi.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(SekunderSaturasi,"Saturasi O²");
            }else if(SekunderRespirasi.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(SekunderRespirasi,"Respirasi");
            }else if(SekunderCatatan.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(SekunderCatatan,"Catatan");
            }else if(SekunderKodePetugas.getText().trim().equals("")||SekunderNamaPetugas.getText().trim().equals("")){
                sukses=false;
                Valid.textKosong(btnSekunderPetugas,"Dokter/Petugas Triase");
            }else if((jmlskala3==0)&&(jmlskala4==0)&&(jmlskala5==0)){
                sukses=false;
                Valid.textKosong(TCariPemeriksaan2,"Skala 3 / Skala 4 / Skala 5");
            }else{
                if(Sequel.mengedittf("data_triase_igd","no_rawat=?","no_rawat=?,tgl_kunjungan=?,cara_masuk=?,alat_transportasi=?,alasan_kedatangan=?,keterangan_kedatangan=?,kode_kasus=?,tekanan_darah=?,nadi=?,pernapasan=?,suhu=?,saturasi_o2=?,nyeri=?,terpasang_infus=?,terpasang_ngt=?,terpasang_kateter_urin=?,nama_pengantar=?,telp_pengantar=?",19,new String[]{
                        TNoRw.getText(),Valid.SetTgl(TanggalKunjungan.getSelectedItem()+"")+" "+TanggalKunjungan.getSelectedItem().toString().substring(11,19),
                        CaraMasuk.getSelectedItem().toString(),Transportasi.getSelectedItem().toString(),AlasanKedatangan.getSelectedItem().toString(),
                        KeteranganKedatangan.getText(),KdKasus.getText(),SekunderTensi.getText(),SekunderNadi.getText(),SekunderRespirasi.getText(),SekunderSuhu.getText(),
                        SekunderSaturasi.getText(),SekunderNyeri.getText(),
                        PacsInfus.isSelected()?"Ya":"Tidak",PacsNgt.isSelected()?"Ya":"Tidak",PacsKateter.isSelected()?"Ya":"Tidak",
                        PacsNamaPengantar.getText(),PacsTelpPengantar.getText(),
                        tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString()
                    })==true){
                    if(SekunderZonaKuning.isSelected()==true){
                        keputusan="Zona Kuning";
                    }else if(SekunderZonaHijau.isSelected()==true){
                        keputusan="Zona Hijau";
                    }
                    if(Sequel.menyimpantf2("data_triase_igdsekunder","?,?,?,?,?,?", 6,new String[]{
                        TNoRw.getText(),SekunderAnamnesa.getText(),SekunderCatatan.getText(),keputusan,
                        Valid.SetTgl(SekunderTanggalTriase.getSelectedItem()+"")+" "+SekunderTanggalTriase.getSelectedItem().toString().substring(11,19), 
                        SekunderKodePetugas.getText()
                    })==true){
                        if(TabSkala3dan4dan5.getSelectedIndex()==0){
                            for(i=0;i<tbSkala3.getRowCount();i++){ 
                                if(tbSkala3.getValueAt(i,0).toString().equals("true")){
                                    if(Sequel.menyimpantf2("data_triase_igddetail_skala3","?,?","Skala 3",2,new String[]{
                                            TNoRw.getText(),tbSkala3.getValueAt(i,1).toString()
                                        })==true){
                                            tbSkala3.setValueAt(false,i,0);
                                    }else{
                                        sukses=false;
                                    }
                                }
                            }
                        }else if(TabSkala3dan4dan5.getSelectedIndex()==1){
                            for(i=0;i<tbSkala4.getRowCount();i++){ 
                                if(tbSkala4.getValueAt(i,0).toString().equals("true")){
                                    if(Sequel.menyimpantf2("data_triase_igddetail_skala4","?,?","Skala 4",2,new String[]{
                                            TNoRw.getText(),tbSkala4.getValueAt(i,1).toString()
                                        })==true){
                                            tbSkala4.setValueAt(false,i,0);
                                    }else{
                                        sukses=false;
                                    }
                                }
                            }
                        }else if(TabSkala3dan4dan5.getSelectedIndex()==2){
                            for(i=0;i<tbSkala5.getRowCount();i++){ 
                                if(tbSkala5.getValueAt(i,0).toString().equals("true")){
                                    if(Sequel.menyimpantf2("data_triase_igddetail_skala5","?,?","Skala 5",2,new String[]{
                                            TNoRw.getText(),tbSkala5.getValueAt(i,1).toString()
                                        })==true){
                                            tbSkala5.setValueAt(false,i,0);
                                    }else{
                                        sukses=false;
                                    }
                                }
                            }
                        }
                    }else{
                        sukses=false;
                    }
                }else{
                    sukses=false;
                }
            }
        }else if(TabTriase.getSelectedIndex()==2){
            // [PACS] Stage e: UPDATE header (data_triase_igd) + data_triase_pacs.
            // Cabang baru, terisolasi - tidak menyentuh alur Primer/Sekunder di atas.
            if(validasiPacs()){
                if(Sequel.mengedittf("data_triase_igd","no_rawat=?","no_rawat=?,tgl_kunjungan=?,cara_masuk=?,alat_transportasi=?,alasan_kedatangan=?,keterangan_kedatangan=?,kode_kasus=?,tekanan_darah=?,nadi=?,pernapasan=?,suhu=?,saturasi_o2=?,nyeri=?,kesadaran=?,berat_badan=?,tinggi_badan=?,kontak_awal_tanggal=?,kontak_awal_pukul=?,terpasang_infus=?,terpasang_ngt=?,terpasang_kateter_urin=?,nama_pengantar=?,telp_pengantar=?",24,new String[]{
                        TNoRw.getText(),Valid.SetTgl(TanggalKunjungan.getSelectedItem()+"")+" "+TanggalKunjungan.getSelectedItem().toString().substring(11,19),
                        CaraMasuk.getSelectedItem().toString(),Transportasi.getSelectedItem().toString(),AlasanKedatangan.getSelectedItem().toString(),
                        KeteranganKedatangan.getText(),KdKasus.getText(),PacsTensi.getText(),PacsNadi.getText(),PacsRespirasi.getText(),PacsSuhu.getText(),
                        PacsSaturasi.getText(),PacsNyeri.getText(),PacsKesadaran.getSelectedItem().toString(),PacsBeratBadan.getText(),PacsTinggiBadan.getText(),
                        Valid.SetTgl(TanggalKunjungan.getSelectedItem()+""),TanggalKunjungan.getSelectedItem().toString().substring(11,19),
                        PacsInfus.isSelected()?"Ya":"Tidak",PacsNgt.isSelected()?"Ya":"Tidak",PacsKateter.isSelected()?"Ya":"Tidak",
                        PacsNamaPengantar.getText(),PacsTelpPengantar.getText(),
                        tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString()
                    })==true){
                    if(Sequel.mengedittf("data_triase_pacs","no_rawat=?","skor_kesadaran=?,skor_tekanan_darah_sistolik=?,skor_nadi=?,skor_respirasi=?,skor_temperatur=?,skor_saturasi_o2=?,total_skor=?,level_pacs=?,keputusan_pacs=?,keputusan_pukul=?,tanggal_triase=?,nik=?,keluhan_utama=?,catatan_khusus=?",15,new String[]{
                            CmbSkorKesadaranPacs.getSelectedItem().toString(),
                            CmbSkorTDPacs.getSelectedItem().toString(),
                            CmbSkorNadiPacs.getSelectedItem().toString(),
                            CmbSkorRespirasiPacs.getSelectedItem().toString(),
                            CmbSkorTemperaturPacs.getSelectedItem().toString(),
                            CmbSkorSaturasiPacs.getSelectedItem().toString(),
                            LblTotalSkorPacs.getText(),
                            levelPacsTerpilih(),
                            keputusanPacsTerpilih(),
                            DTPKeputusanPukulPacs.getTeksJam(),
                            Valid.SetTgl(TanggalKunjungan.getSelectedItem()+"")+" "+pukulAman(TanggalKunjungan),
                            SekunderKodePetugas.getText(),
                            PacsKeluhanUtama.getText(),
                            PacsCatatanKhusus.getText(),
                            tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString()
                        })==false){
                        sukses=false;
                    }
                    // [Mekanisme Trauma] Edit = hapus baris lama lalu insert ulang kalau
                    // sekarang Trauma (pola sama seperti ganti() utk tabel detail skala di atas).
                    Sequel.meghapus("data_triase_mekanisme_trauma","no_rawat",tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString());
                    if(panelMekanismeTrauma.isVisible()){
                        if(Sequel.menyimpantf2("data_triase_mekanisme_trauma (no_rawat,kkl_tunggal,kkl_dengan_lawan,kkl_lawan,kkl_tempat_kejadian,kkl_tanggal_kejadian,kkl_pukul_kejadian,jatuh_ketinggian,jatuh_ketinggian_keterangan,luka_bakar,luka_bakar_keterangan,trauma_fisik,trauma_fisik_keterangan,trauma_zat_kimia,trauma_zat_kimia_keterangan,trauma_zat_lainnya,trauma_zat_lainnya_keterangan)","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?",17,new String[]{
                                tbTriase.getValueAt(tbTriase.getSelectedRow(),0).toString(),
                                CekKklTunggal.isSelected()?"Ya":"Tidak",
                                CekKklDenganLawan.isSelected()?"Ya":"Tidak",
                                TKklLawan.getText(),
                                TKklTempatKejadian.getText(),
                                panelKklKejadian.getTanggalSql(Valid),
                                panelKklKejadian.getJamSql(),
                                CekJatuhKetinggian.isSelected()?"Ya":"Tidak",
                                TJatuhKetinggianKet.getText(),
                                CekLukaBakar.isSelected()?"Ya":"Tidak",
                                TLukaBakarKet.getText(),
                                CekTraumaFisik.isSelected()?"Ya":"Tidak",
                                TTraumaFisikKet.getText(),
                                CekTraumaZatKimia.isSelected()?"Ya":"Tidak",
                                TTraumaZatKimiaKet.getText(),
                                CekTraumaZatLainnya.isSelected()?"Ya":"Tidak",
                                TTraumaZatLainnyaKet.getText()
                            })==false){
                            sukses=false;
                        }
                    }
                }else{
                    sukses=false;
                }
            }else{
                sukses=false;
            }
        }
        if(sukses==true){
            Sequel.Commit();
        }else{
            sukses=false;
            Sequel.RollBack();
        }
        Sequel.AutoComitTrue();
        if(sukses==true){
            emptTeks();
            runBackground(() ->tampil());
            TabPilihan.setSelectedIndex(1);
        }
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

    private void LoadSkalaSekunder() {
        tampilskala3();
        tampilskala4();
        tampilskala5();
    }

    private void LoadSkalaPrimer() {
        tampilskala1();
        tampilskala2();
    }
}
