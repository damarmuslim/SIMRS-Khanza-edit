/*
 * Kontribusi dari Abdul Wahid, RSUD Cipayung Jakarta Timur
 */


package rekammedis;

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
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariDokter;


/**
 *
 * @author perpustakaan
 */
public final class RMPenilaianAwalMedisIGD extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;
    private DlgCariDokter dokter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private StringBuilder htmlContent;
    private String finger="";
    private String TANGGALMUNDUR="yes";
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public RMPenilaianAwalMedisIGD(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        widget.IconStyle.pasang(this);
        isMenu(); // panel ringkasan HTML disembunyikan dulu sampai user klik baris pasien (lihat getData())

        // [RM 03-2] Header tabMode SENGAJA diringkas jadi kolom pencarian saja
        // (bukan 41 kolom data lengkap spt sebelumnya) - keputusan user:
        // getData() sekarang pola query-ulang-DB (SELECT * by no_rawat saat
        // baris diklik), bukan baca dari array baris JTable, jadi tabMode
        // tidak perlu lagi menyimpan seluruh data double di memori.
        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat","No.RM","Nama Pasien","Tgl.Lahir","J.K.","Kode Dokter","Nama Dokter","Tanggal","Diagnosa Kerja"
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };

        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 9; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(150);
            }else if(i==3){
                column.setPreferredWidth(65);
            }else if(i==4){
                column.setPreferredWidth(55);
            }else if(i==5){
                column.setPreferredWidth(80);
            }else if(i==6){
                column.setPreferredWidth(150);
            }else if(i==7){
                column.setPreferredWidth(115);
            }else if(i==8){
                column.setPreferredWidth(250);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        Hubungan.setDocument(new batasInput((int)30).getKata(Hubungan));
        KeluhanUtama.setDocument(new batasInput((int)2000).getKata(KeluhanUtama));
        RPS.setDocument(new batasInput((int)2000).getKata(RPS));
        RPK.setDocument(new batasInput((int)2000).getKata(RPK));
        RPD.setDocument(new batasInput((int)1000).getKata(RPD));
        RPO.setDocument(new batasInput((int)1000).getKata(RPO));
        Alergi.setDocument(new batasInput((int)50).getKata(Alergi));
        GCS.setDocument(new batasInput((byte)10).getKata(GCS));
        TD.setDocument(new batasInput((byte)8).getKata(TD));
        Nadi.setDocument(new batasInput((byte)5).getKata(Nadi));
        RR.setDocument(new batasInput((byte)5).getKata(RR));
        Suhu.setDocument(new batasInput((byte)5).getKata(Suhu));
        SPO.setDocument(new batasInput((byte)5).getKata(SPO));
        BB.setDocument(new batasInput((byte)5).getKata(BB));
        TB.setDocument(new batasInput((byte)5).getKata(TB));
        KetFisik.setDocument(new batasInput((int)5000).getKata(KetFisik));
        KetLokalis.setDocument(new batasInput((int)3000).getKata(KetLokalis));
        EKG.setDocument(new batasInput((int)3000).getKata(EKG));
        Diagnosis.setDocument(new batasInput((int)500).getKata(Diagnosis));
        Tatalaksana.setDocument(new batasInput((int)5000).getKata(Tatalaksana));
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

        pensiunkanFieldLama();
        initRm032Tambahan();
        daftarkanSeksiLegacyMedis();
        susunRangkaMedis();
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
        PanelAccor = new widget.PanelBiasa();
        ChkAccor = new widget.CekBox();
        FormMenu = new widget.PanelBiasa();
        jLabelPasien1 = new widget.Label();
        TNoRM1 = new widget.TextBox();
        TPasien1 = new widget.TextBox();
        BtnPrint1 = new widget.Button();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnPenilaianMedis = new javax.swing.JMenuItem();
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
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        jLabel9 = new widget.Label();
        Jk = new widget.TextBox();
        jLabel10 = new widget.Label();
        jLabel11 = new widget.Label();
        jLabel12 = new widget.Label();
        BB = new widget.TextBox();
        jLabel13 = new widget.Label();
        TB = new widget.TextBox();
        jLabel15 = new widget.Label();
        jLabel16 = new widget.Label();
        Nadi = new widget.TextBox();
        jLabel17 = new widget.Label();
        jLabel18 = new widget.Label();
        Suhu = new widget.TextBox();
        jLabel22 = new widget.Label();
        TD = new widget.TextBox();
        jLabel20 = new widget.Label();
        jLabel23 = new widget.Label();
        jLabel24 = new widget.Label();
        jLabel25 = new widget.Label();
        RR = new widget.TextBox();
        jLabel26 = new widget.Label();
        jLabel37 = new widget.Label();
        Alergi = new widget.TextBox();
        Anamnesis = new widget.ComboBox();
        scrollPane1 = new widget.ScrollPane();
        KeluhanUtama = new widget.TextArea();
        jLabel30 = new widget.Label();
        scrollPane2 = new widget.ScrollPane();
        RPD = new widget.TextArea();
        jLabel31 = new widget.Label();
        scrollPane3 = new widget.ScrollPane();
        RPK = new widget.TextArea();
        jLabel32 = new widget.Label();
        scrollPane4 = new widget.ScrollPane();
        RPO = new widget.TextArea();
        scrollPane5 = new widget.ScrollPane();
        KetFisik = new widget.TextArea();
        jLabel28 = new widget.Label();
        GCS = new widget.TextBox();
        jLabel94 = new widget.Label();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel38 = new widget.Label();
        Hubungan = new widget.TextBox();
        jLabel33 = new widget.Label();
        scrollPane7 = new widget.ScrollPane();
        RPS = new widget.TextArea();
        jSeparator12 = new javax.swing.JSeparator();
        jLabel39 = new widget.Label();
        Keadaan = new widget.ComboBox();
        jLabel40 = new widget.Label();
        Kesadaran = new widget.ComboBox();
        jLabel41 = new widget.Label();
        jLabel29 = new widget.Label();
        SPO = new widget.TextBox();
        jLabel35 = new widget.Label();
        Kepala = new widget.ComboBox();
        jLabel44 = new widget.Label();
        Gigi = new widget.ComboBox();
        jLabel45 = new widget.Label();
        Leher = new widget.ComboBox();
        jLabel46 = new widget.Label();
        Thoraks = new widget.ComboBox();
        jLabel49 = new widget.Label();
        Abdomen = new widget.ComboBox();
        jLabel50 = new widget.Label();
        Genital = new widget.ComboBox();
        jLabel51 = new widget.Label();
        Ekstremitas = new widget.ComboBox();
        jSeparator13 = new javax.swing.JSeparator();
        jLabel99 = new widget.Label();
        PanelWall = new usu.widget.glass.PanelGlass();
        scrollPane8 = new widget.ScrollPane();
        KetLokalis = new widget.TextArea();
        jLabel79 = new widget.Label();
        jSeparator14 = new javax.swing.JSeparator();
        jLabel100 = new widget.Label();
        scrollPane9 = new widget.ScrollPane();
        EKG = new widget.TextArea();
        jSeparator15 = new javax.swing.JSeparator();
        jLabel101 = new widget.Label();
        scrollPane12 = new widget.ScrollPane();
        Diagnosis = new widget.TextArea();
        jSeparator16 = new javax.swing.JSeparator();
        jLabel102 = new widget.Label();
        scrollPane13 = new widget.ScrollPane();
        Tatalaksana = new widget.TextArea();
        jLabel103 = new widget.Label();
        label11 = new widget.Label();
        TglAsuhan = new widget.Tanggal();
        jLabel42 = new widget.Label();
        Mata = new widget.ComboBox();
        jLabel80 = new widget.Label();
        jLabel81 = new widget.Label();
        scrollPane10 = new widget.ScrollPane();
        Radiologi = new widget.TextArea();
        jLabel82 = new widget.Label();
        scrollPane11 = new widget.ScrollPane();
        Laborat = new widget.TextArea();
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

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnPenilaianMedis.setBackground(new java.awt.Color(255, 255, 254));
        MnPenilaianMedis.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        MnPenilaianMedis.setForeground(new java.awt.Color(50, 50, 50));
        MnPenilaianMedis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnPenilaianMedis.setText("Laporan Pengkajian Medis");
        MnPenilaianMedis.setName("MnPenilaianMedis"); // NOI18N
        MnPenilaianMedis.setPreferredSize(new java.awt.Dimension(220, 26));
        MnPenilaianMedis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnPenilaianMedisActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnPenilaianMedis);

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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pengkajian Awal Medis IGD ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
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
        FormInput.setPreferredSize(new java.awt.Dimension(870, 1383));
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
        FormInput.add(TPasien);
        TPasien.setBounds(309, 10, 260, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(207, 10, 100, 23);

        label14.setText("Dokter :");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label14);
        label14.setBounds(0, 40, 70, 23);

        KdDokter.setEditable(false);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.setPreferredSize(new java.awt.Dimension(80, 23));
        KdDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdDokterKeyPressed(evt);
            }
        });
        FormInput.add(KdDokter);
        KdDokter.setBounds(74, 40, 90, 23);

        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter"); // NOI18N
        NmDokter.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmDokter);
        NmDokter.setBounds(166, 40, 180, 23);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic('2');
        BtnDokter.setToolTipText("Alt+2");
        BtnDokter.setName("BtnDokter"); // NOI18N
        BtnDokter.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokterActionPerformed(evt);
            }
        });
        BtnDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnDokterKeyPressed(evt);
            }
        });
        FormInput.add(BtnDokter);
        BtnDokter.setBounds(348, 40, 28, 23);

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
        jLabel9.setBounds(0, 190, 180, 23);

        Jk.setEditable(false);
        Jk.setHighlighter(null);
        Jk.setName("Jk"); // NOI18N
        FormInput.add(Jk);
        Jk.setBounds(774, 10, 80, 23);

        jLabel10.setText("No.Rawat :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 10, 70, 23);

        jLabel11.setText("J.K. :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(740, 10, 30, 23);

        jLabel12.setText("BB :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(760, 260, 30, 23);

        BB.setFocusTraversalPolicyProvider(true);
        BB.setName("BB"); // NOI18N
        BB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BBKeyPressed(evt);
            }
        });
        FormInput.add(BB);
        BB.setBounds(794, 260, 45, 23);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel13.setText("Kg");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(842, 260, 30, 23);

        TB.setFocusTraversalPolicyProvider(true);
        TB.setName("TB"); // NOI18N
        TB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TBKeyPressed(evt);
            }
        });
        FormInput.add(TB);
        TB.setBounds(672, 260, 45, 23);

        jLabel15.setText("TB :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(638, 260, 30, 23);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText("x/menit");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(370, 290, 50, 23);

        Nadi.setFocusTraversalPolicyProvider(true);
        Nadi.setName("Nadi"); // NOI18N
        Nadi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NadiKeyPressed(evt);
            }
        });
        FormInput.add(Nadi);
        Nadi.setBounds(322, 290, 45, 23);

        jLabel17.setText("Nadi :");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(278, 290, 40, 23);

        jLabel18.setText("Suhu :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(606, 290, 40, 23);

        Suhu.setFocusTraversalPolicyProvider(true);
        Suhu.setName("Suhu"); // NOI18N
        Suhu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SuhuKeyPressed(evt);
            }
        });
        FormInput.add(Suhu);
        Suhu.setBounds(650, 290, 45, 23);

        jLabel22.setText("TD :");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(0, 290, 127, 23);

        TD.setFocusTraversalPolicyProvider(true);
        TD.setName("TD"); // NOI18N
        TD.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TDKeyPressed(evt);
            }
        });
        FormInput.add(TD);
        TD.setBounds(131, 290, 76, 23);

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel20.setText("°C");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(698, 290, 30, 23);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setText("mmHg");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(210, 290, 50, 23);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText(" cm");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(720, 260, 30, 23);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel25.setText("x/menit");
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(527, 290, 50, 23);

        RR.setFocusTraversalPolicyProvider(true);
        RR.setName("RR"); // NOI18N
        RR.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RRKeyPressed(evt);
            }
        });
        FormInput.add(RR);
        RR.setBounds(479, 290, 45, 23);

        jLabel26.setText("RR :");
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(435, 290, 40, 23);

        jLabel37.setText("Riwayat Alergi :");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(440, 190, 150, 23);

        Alergi.setFocusTraversalPolicyProvider(true);
        Alergi.setName("Alergi"); // NOI18N
        Alergi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AlergiKeyPressed(evt);
            }
        });
        FormInput.add(Alergi);
        Alergi.setBounds(594, 190, 260, 23);

        Anamnesis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Autoanamnesis", "Alloanamnesis" }));
        Anamnesis.setName("Anamnesis"); // NOI18N
        Anamnesis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AnamnesisKeyPressed(evt);
            }
        });
        FormInput.add(Anamnesis);
        Anamnesis.setBounds(644, 40, 128, 23);

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
        scrollPane1.setBounds(129, 90, 310, 43);

        jLabel30.setText("Riwayat Penyakit Sekarang :");
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(440, 90, 150, 23);

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
        scrollPane2.setBounds(594, 140, 260, 43);

        jLabel31.setText("Riwayat Penyakit Dahulu :");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(440, 140, 150, 23);

        scrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane3.setName("scrollPane3"); // NOI18N

        RPK.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RPK.setColumns(20);
        RPK.setRows(5);
        RPK.setName("RPK"); // NOI18N
        RPK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RPKKeyPressed(evt);
            }
        });
        scrollPane3.setViewportView(RPK);

        FormInput.add(scrollPane3);
        scrollPane3.setBounds(184, 140, 255, 42);

        jLabel32.setText("Riwayat Penyakit Keluarga :");
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(0, 140, 180, 23);

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
        scrollPane4.setBounds(184, 190, 255, 42);

        scrollPane5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane5.setName("scrollPane5"); // NOI18N

        KetFisik.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        KetFisik.setColumns(20);
        KetFisik.setRows(8);
        KetFisik.setName("KetFisik"); // NOI18N
        KetFisik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetFisikKeyPressed(evt);
            }
        });
        scrollPane5.setViewportView(KetFisik);

        FormInput.add(scrollPane5);
        scrollPane5.setBounds(514, 320, 340, 113);

        jLabel28.setText("GCS(E,V,M) :");
        jLabel28.setName("jLabel28"); // NOI18N
        FormInput.add(jLabel28);
        jLabel28.setBounds(489, 260, 70, 23);

        GCS.setFocusTraversalPolicyProvider(true);
        GCS.setName("GCS"); // NOI18N
        GCS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                GCSKeyPressed(evt);
            }
        });
        FormInput.add(GCS);
        GCS.setBounds(563, 260, 60, 23);

        jLabel94.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel94.setText("II. PEMERIKSAAN FISIK");
        jLabel94.setName("jLabel94"); // NOI18N
        FormInput.add(jLabel94);
        jLabel94.setBounds(10, 240, 180, 23);

        jSeparator1.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator1.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(0, 70, 880, 1);

        jLabel38.setText("Anamnesis :");
        jLabel38.setName("jLabel38"); // NOI18N
        FormInput.add(jLabel38);
        jLabel38.setBounds(570, 40, 70, 23);

        Hubungan.setName("Hubungan"); // NOI18N
        Hubungan.setPreferredSize(new java.awt.Dimension(207, 23));
        Hubungan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HubunganKeyPressed(evt);
            }
        });
        FormInput.add(Hubungan);
        Hubungan.setBounds(774, 40, 80, 23);

        jLabel33.setText("Keluhan Utama :");
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput.add(jLabel33);
        jLabel33.setBounds(0, 90, 125, 23);

        scrollPane7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane7.setName("scrollPane7"); // NOI18N

        RPS.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RPS.setColumns(20);
        RPS.setRows(5);
        RPS.setName("RPS"); // NOI18N
        RPS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RPSKeyPressed(evt);
            }
        });
        scrollPane7.setViewportView(RPS);

        FormInput.add(scrollPane7);
        scrollPane7.setBounds(594, 90, 260, 43);

        jSeparator12.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator12.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator12.setName("jSeparator12"); // NOI18N
        FormInput.add(jSeparator12);
        jSeparator12.setBounds(0, 240, 880, 1);

        jLabel39.setText("Kesadaran :");
        jLabel39.setName("jLabel39"); // NOI18N
        FormInput.add(jLabel39);
        jLabel39.setBounds(265, 260, 70, 23);

        Keadaan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sehat", "Sakit Ringan", "Sakit Sedang", "Sakit Berat" }));
        Keadaan.setName("Keadaan"); // NOI18N
        Keadaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeadaanKeyPressed(evt);
            }
        });
        FormInput.add(Keadaan);
        Keadaan.setBounds(131, 260, 118, 23);

        jLabel40.setText("Kepala :");
        jLabel40.setName("jLabel40"); // NOI18N
        FormInput.add(jLabel40);
        jLabel40.setBounds(0, 320, 127, 23);

        Kesadaran.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Compos Mentis", "Apatis", "Somnolen", "Sopor", "Koma" }));
        Kesadaran.setName("Kesadaran"); // NOI18N
        Kesadaran.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KesadaranKeyPressed(evt);
            }
        });
        FormInput.add(Kesadaran);
        Kesadaran.setBounds(339, 260, 130, 23);

        jLabel41.setText("Keadaan Umum :");
        jLabel41.setName("jLabel41"); // NOI18N
        FormInput.add(jLabel41);
        jLabel41.setBounds(0, 260, 127, 23);

        jLabel29.setText("SpO2 :");
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(750, 290, 40, 23);

        SPO.setFocusTraversalPolicyProvider(true);
        SPO.setName("SPO"); // NOI18N
        SPO.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SPOKeyPressed(evt);
            }
        });
        FormInput.add(SPO);
        SPO.setBounds(794, 290, 45, 23);

        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel35.setText("%");
        jLabel35.setName("jLabel35"); // NOI18N
        FormInput.add(jLabel35);
        jLabel35.setBounds(842, 290, 30, 23);

        Kepala.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Abnormal", "Tidak Diperiksa" }));
        Kepala.setName("Kepala"); // NOI18N
        Kepala.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KepalaKeyPressed(evt);
            }
        });
        FormInput.add(Kepala);
        Kepala.setBounds(131, 320, 128, 23);

        jLabel44.setText("Gigi & Mulut :");
        jLabel44.setName("jLabel44"); // NOI18N
        FormInput.add(jLabel44);
        jLabel44.setBounds(0, 380, 127, 23);

        Gigi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Abnormal", "Tidak Diperiksa" }));
        Gigi.setName("Gigi"); // NOI18N
        Gigi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                GigiKeyPressed(evt);
            }
        });
        FormInput.add(Gigi);
        Gigi.setBounds(131, 380, 128, 23);

        jLabel45.setText("Leher :");
        jLabel45.setName("jLabel45"); // NOI18N
        FormInput.add(jLabel45);
        jLabel45.setBounds(0, 410, 127, 23);

        Leher.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Abnormal", "Tidak Diperiksa" }));
        Leher.setName("Leher"); // NOI18N
        Leher.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LeherKeyPressed(evt);
            }
        });
        FormInput.add(Leher);
        Leher.setBounds(131, 410, 128, 23);

        jLabel46.setText("Thoraks :");
        jLabel46.setName("jLabel46"); // NOI18N
        FormInput.add(jLabel46);
        jLabel46.setBounds(267, 320, 95, 23);

        Thoraks.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Abnormal", "Tidak Diperiksa" }));
        Thoraks.setName("Thoraks"); // NOI18N
        Thoraks.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ThoraksKeyPressed(evt);
            }
        });
        FormInput.add(Thoraks);
        Thoraks.setBounds(366, 320, 128, 23);

        jLabel49.setText("Abdomen :");
        jLabel49.setName("jLabel49"); // NOI18N
        FormInput.add(jLabel49);
        jLabel49.setBounds(267, 350, 95, 23);

        Abdomen.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Abnormal", "Tidak Diperiksa" }));
        Abdomen.setName("Abdomen"); // NOI18N
        Abdomen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AbdomenKeyPressed(evt);
            }
        });
        FormInput.add(Abdomen);
        Abdomen.setBounds(366, 350, 128, 23);

        jLabel50.setText("Genital & Anus :");
        jLabel50.setName("jLabel50"); // NOI18N
        FormInput.add(jLabel50);
        jLabel50.setBounds(267, 380, 95, 23);

        Genital.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Abnormal", "Tidak Diperiksa" }));
        Genital.setName("Genital"); // NOI18N
        Genital.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                GenitalKeyPressed(evt);
            }
        });
        FormInput.add(Genital);
        Genital.setBounds(366, 380, 128, 23);

        jLabel51.setText("Ekstremitas :");
        jLabel51.setName("jLabel51"); // NOI18N
        FormInput.add(jLabel51);
        jLabel51.setBounds(267, 410, 95, 23);

        Ekstremitas.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Abnormal", "Tidak Diperiksa" }));
        Ekstremitas.setName("Ekstremitas"); // NOI18N
        Ekstremitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EkstremitasKeyPressed(evt);
            }
        });
        FormInput.add(Ekstremitas);
        Ekstremitas.setBounds(366, 410, 128, 23);

        jSeparator13.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator13.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator13.setName("jSeparator13"); // NOI18N
        FormInput.add(jSeparator13);
        jSeparator13.setBounds(0, 440, 880, 1);

        jLabel99.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel99.setText("I. RIWAYAT KESEHATAN");
        jLabel99.setName("jLabel99"); // NOI18N
        FormInput.add(jLabel99);
        jLabel99.setBounds(10, 70, 180, 23);

        PanelWall.setBackground(new java.awt.Color(29, 29, 29));
        PanelWall.setBackgroundImage(new javax.swing.ImageIcon(getClass().getResource("/picture/semua.png"))); // NOI18N
        PanelWall.setBackgroundImageType(usu.widget.constan.BackgroundConstan.BACKGROUND_IMAGE_STRECT);
        PanelWall.setPreferredSize(new java.awt.Dimension(200, 200));
        PanelWall.setRound(false);
        PanelWall.setWarna(new java.awt.Color(110, 110, 110));
        PanelWall.setLayout(null);
        FormInput.add(PanelWall);
        PanelWall.setBounds(45, 460, 809, 300);

        scrollPane8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane8.setName("scrollPane8"); // NOI18N

        KetLokalis.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        KetLokalis.setColumns(20);
        KetLokalis.setRows(5);
        KetLokalis.setName("KetLokalis"); // NOI18N
        KetLokalis.setPreferredSize(new java.awt.Dimension(182, 92));
        KetLokalis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetLokalisKeyPressed(evt);
            }
        });
        scrollPane8.setViewportView(KetLokalis);

        FormInput.add(scrollPane8);
        scrollPane8.setBounds(44, 780, 810, 83);

        jLabel79.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel79.setText("Keterangan :");
        jLabel79.setName("jLabel79"); // NOI18N
        FormInput.add(jLabel79);
        jLabel79.setBounds(44, 760, 100, 23);

        jSeparator14.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator14.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator14.setName("jSeparator14"); // NOI18N
        FormInput.add(jSeparator14);
        jSeparator14.setBounds(0, 870, 880, 1);

        jLabel100.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel100.setText("III. STATUS LOKALIS");
        jLabel100.setName("jLabel100"); // NOI18N
        FormInput.add(jLabel100);
        jLabel100.setBounds(10, 440, 180, 23);

        scrollPane9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane9.setName("scrollPane9"); // NOI18N

        EKG.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        EKG.setColumns(20);
        EKG.setRows(5);
        EKG.setName("EKG"); // NOI18N
        EKG.setPreferredSize(new java.awt.Dimension(102, 52));
        EKG.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EKGKeyPressed(evt);
            }
        });
        scrollPane9.setViewportView(EKG);

        FormInput.add(scrollPane9);
        scrollPane9.setBounds(44, 910, 260, 63);

        jSeparator15.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator15.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator15.setName("jSeparator15"); // NOI18N
        FormInput.add(jSeparator15);
        jSeparator15.setBounds(0, 980, 880, 1);

        jLabel101.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel101.setText("IV. PEMERIKSAAN PENUNJANG");
        jLabel101.setName("jLabel101"); // NOI18N
        FormInput.add(jLabel101);
        jLabel101.setBounds(10, 870, 190, 23);

        scrollPane12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane12.setName("scrollPane12"); // NOI18N

        Diagnosis.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Diagnosis.setColumns(20);
        Diagnosis.setRows(3);
        Diagnosis.setName("Diagnosis"); // NOI18N
        Diagnosis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosisKeyPressed(evt);
            }
        });
        scrollPane12.setViewportView(Diagnosis);

        FormInput.add(scrollPane12);
        scrollPane12.setBounds(44, 1000, 810, 43);

        jSeparator16.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator16.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator16.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator16.setName("jSeparator16"); // NOI18N
        FormInput.add(jSeparator16);
        jSeparator16.setBounds(0, 1050, 880, 1);

        jLabel102.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel102.setText("V. DIAGNOSIS/ASESMEN");
        jLabel102.setName("jLabel102"); // NOI18N
        FormInput.add(jLabel102);
        jLabel102.setBounds(10, 980, 190, 23);

        scrollPane13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane13.setName("scrollPane13"); // NOI18N

        Tatalaksana.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Tatalaksana.setColumns(20);
        Tatalaksana.setRows(24);
        Tatalaksana.setName("Tatalaksana"); // NOI18N
        Tatalaksana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TatalaksanaKeyPressed(evt);
            }
        });
        scrollPane13.setViewportView(Tatalaksana);

        FormInput.add(scrollPane13);
        scrollPane13.setBounds(44, 1070, 810, 303);

        jLabel103.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel103.setText("VI. TATALAKSANA");
        jLabel103.setName("jLabel103"); // NOI18N
        FormInput.add(jLabel103);
        jLabel103.setBounds(10, 1050, 190, 23);

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label11);
        label11.setBounds(380, 40, 52, 23);

        TglAsuhan.setForeground(new java.awt.Color(50, 70, 50));
        TglAsuhan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-02-2026 15:39:27" }));
        TglAsuhan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TglAsuhan.setName("TglAsuhan"); // NOI18N
        TglAsuhan.setOpaque(false);
        TglAsuhan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglAsuhanKeyPressed(evt);
            }
        });
        FormInput.add(TglAsuhan);
        TglAsuhan.setBounds(436, 40, 130, 23);

        jLabel42.setText("Mata :");
        jLabel42.setName("jLabel42"); // NOI18N
        FormInput.add(jLabel42);
        jLabel42.setBounds(0, 350, 127, 23);

        Mata.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Abnormal", "Tidak Diperiksa" }));
        Mata.setName("Mata"); // NOI18N
        Mata.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MataKeyPressed(evt);
            }
        });
        FormInput.add(Mata);
        Mata.setBounds(131, 350, 128, 23);

        jLabel80.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel80.setText("EKG :");
        jLabel80.setName("jLabel80"); // NOI18N
        FormInput.add(jLabel80);
        jLabel80.setBounds(44, 890, 150, 23);

        jLabel81.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel81.setText("Radiologi :");
        jLabel81.setName("jLabel81"); // NOI18N
        FormInput.add(jLabel81);
        jLabel81.setBounds(319, 890, 150, 23);

        scrollPane10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane10.setName("scrollPane10"); // NOI18N

        Radiologi.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Radiologi.setColumns(20);
        Radiologi.setRows(5);
        Radiologi.setName("Radiologi"); // NOI18N
        Radiologi.setPreferredSize(new java.awt.Dimension(102, 52));
        Radiologi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RadiologiKeyPressed(evt);
            }
        });
        scrollPane10.setViewportView(Radiologi);

        FormInput.add(scrollPane10);
        scrollPane10.setBounds(319, 910, 260, 63);

        jLabel82.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel82.setText("Laborat :");
        jLabel82.setName("jLabel82"); // NOI18N
        FormInput.add(jLabel82);
        jLabel82.setBounds(594, 890, 150, 23);

        scrollPane11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane11.setName("scrollPane11"); // NOI18N

        Laborat.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Laborat.setColumns(20);
        Laborat.setRows(5);
        Laborat.setName("Laborat"); // NOI18N
        Laborat.setPreferredSize(new java.awt.Dimension(102, 52));
        Laborat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LaboratKeyPressed(evt);
            }
        });
        scrollPane11.setViewportView(Laborat);

        FormInput.add(scrollPane11);
        scrollPane11.setBounds(594, 910, 260, 63);

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
        tbObat.setComponentPopupMenu(jPopupMenu1);
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

        // Panel accordion "Data Pengkajian" (PanelAccor/ChkAccor/FormMenu/
        // ScrollHTML+LoadHTML) - ditampilkan di sebelah tabel Data Pengkajian
        // saat baris diklik (lihat bangunRingkasanHtml(), dipanggil dari
        // getData()), meniru persis pola yg sudah ada di RMTriaseIGD (Data
        // Triase) dan RMPenilaianAwalKeperawatanIGD (Data Pengkajian):
        // ChkAccor (toggle collapse/expand) di WEST, FormMenu (identitas
        // pasien terpilih + tombol cetak) di NORTH, ScrollHTML di CENTER.
        PanelAccor.setBackground(new java.awt.Color(255, 255, 255));
        PanelAccor.setName("PanelAccor");
        PanelAccor.setPreferredSize(new java.awt.Dimension(470, 43));
        PanelAccor.setLayout(new java.awt.BorderLayout(1, 1));

        ChkAccor.setBackground(new java.awt.Color(255, 250, 250));
        ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png")));
        ChkAccor.setSelected(false);
        ChkAccor.setFocusable(false);
        ChkAccor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkAccor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkAccor.setName("ChkAccor");
        ChkAccor.setPreferredSize(new java.awt.Dimension(15, 20));
        ChkAccor.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png")));
        ChkAccor.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png")));
        ChkAccor.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png")));
        ChkAccor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkAccorActionPerformed(evt);
            }
        });
        PanelAccor.add(ChkAccor, java.awt.BorderLayout.WEST);

        FormMenu.setBackground(new java.awt.Color(255, 255, 255));
        FormMenu.setBorder(null);
        FormMenu.setName("FormMenu");
        FormMenu.setPreferredSize(new java.awt.Dimension(115, 43));
        FormMenu.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        jLabelPasien1.setText("Pasien :");
        jLabelPasien1.setName("jLabelPasien1");
        jLabelPasien1.setPreferredSize(new java.awt.Dimension(55, 23));
        FormMenu.add(jLabelPasien1);

        TNoRM1.setEditable(false);
        TNoRM1.setHighlighter(null);
        TNoRM1.setName("TNoRM1");
        TNoRM1.setPreferredSize(new java.awt.Dimension(100, 23));
        FormMenu.add(TNoRM1);

        TPasien1.setEditable(false);
        TPasien1.setBackground(new java.awt.Color(245, 250, 240));
        TPasien1.setHighlighter(null);
        TPasien1.setName("TPasien1");
        TPasien1.setPreferredSize(new java.awt.Dimension(250, 23));
        FormMenu.add(TPasien1);

        BtnPrint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item (copy).png")));
        BtnPrint1.setMnemonic('T');
        BtnPrint1.setToolTipText("Alt+T");
        BtnPrint1.setName("BtnPrint1");
        BtnPrint1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPrint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrint1ActionPerformed(evt);
            }
        });
        FormMenu.add(BtnPrint1);

        PanelAccor.add(FormMenu, java.awt.BorderLayout.NORTH);

        ScrollHTML = new widget.ScrollPane();
        ScrollHTML.setBorder(null);
        ScrollHTML.setName("ScrollHTML");
        ScrollHTML.setOpaque(true);
        ScrollHTML.setPreferredSize(new java.awt.Dimension(470, 16));
        ScrollHTML.setViewportView(LoadHTML);
        PanelAccor.add(ScrollHTML, java.awt.BorderLayout.CENTER);

        internalFrame3.add(PanelAccor, java.awt.BorderLayout.EAST);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tgl.Asuhan :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-02-2026" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "04-02-2026" }));
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

        TabRawat.addTab("Data Pengkajian", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{            
            Valid.pindah(evt,TCari,BtnDokter);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Dokter");
        }else if(KeluhanUtama.getText().trim().equals("")){
            Valid.textKosong(KeluhanUtama,"Keluhan Utama");
        }else if(RPS.getText().trim().equals("")){
            Valid.textKosong(RPS,"Riwayat Penyakit Sekarang");
        }else if(RPK.getText().trim().equals("")){
            Valid.textKosong(RPK,"Riwayat Penyakit Keluarga");
        }else if(RPD.getText().trim().equals("")){
            Valid.textKosong(RPD,"Riwayat Penyakit Dahulu");
        }else if(RPO.getText().trim().equals("")){
            Valid.textKosong(RPO,"Riwayat Pengunaan obat");
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
            Valid.pindah(evt,KetFisik,BtnBatal);
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
                if(KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString())){
                    if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString(),Sequel.ambiltanggalsekarang())==true){
                        hapus();
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"Hanya bisa dihapus oleh dokter yang bersangkutan..!!");
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
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Dokter");
        }else if(KeluhanUtama.getText().trim().equals("")){
            Valid.textKosong(KeluhanUtama,"Keluhan Utama");
        }else if(RPS.getText().trim().equals("")){
            Valid.textKosong(RPS,"Riwayat Penyakit Sekarang");
        }else if(RPK.getText().trim().equals("")){
            Valid.textKosong(RPK,"Riwayat Penyakit Keluarga");
        }else if(RPD.getText().trim().equals("")){
            Valid.textKosong(RPD,"Riwayat Penyakit Dahulu");
        }else if(RPO.getText().trim().equals("")){
            Valid.textKosong(RPO,"Riwayat Pengunaan obat");
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else{
                    if(KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString())){
                        if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString(),Sequel.ambiltanggalsekarang())==true){
                            if(TanggalRegistrasi.getText().equals("")){
                                TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                            }
                            if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19))==true){
                                ganti();
                            }
                        }
                    }else{
                        JOptionPane.showMessageDialog(null,"Hanya bisa diganti oleh dokter yang bersangkutan..!!");
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
                htmlContent = new StringBuilder();
                htmlContent.append(                             
                    "<tr class='isi'>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='105px'><b>No.Rawat</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'><b>No.RM</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'><b>Nama Pasien</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='65px'><b>Tgl.Lahir</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='55px'><b>J.K.</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='80px'><b>NIP</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'><b>Nama Dokter</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='115px'><b>Tanggal</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='80px'><b>Anamnesis</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='100px'><b>Hubungan</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='300px'><b>Keluhan Utama</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'><b>Riwayat Penyakit Sekarang</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'><b>Riwayat Penyakit Dahulu</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'><b>Riwayat Penyakit Keluarga</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'><b>Riwayat Penggunakan Obat</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='120px'><b>Riwayat Alergi</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='90px'><b>Keadaan Umum</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='50px'><b>GCS</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='80px'><b>Kesadaran</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='60px'><b>TD(mmHg)</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='75px'><b>Nadi(x/menit)</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='67px'><b>RR(x/menit)</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='40px'><b>Suhu</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='40px'><b>SpO2</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='40px'><b>BB(Kg)</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='40px'><b>TB(cm)</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='80px'><b>Kepala</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='80px'><b>Mata</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='80px'><b>Gigi & Mulut</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='80px'><b>Leher</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='80px'><b>Thoraks</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='80px'><b>Abdomen</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='80px'><b>Genital & Anus</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='80px'><b>Ekstremitas</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='300px'><b>Ket.Pemeriksaan Fisik</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='200px'><b>Ket.Status Lokalis</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='170px'><b>EKG</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='170px'><b>Radiologi</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='170px'><b>Laborat</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'><b>Diagnosis/Asesmen</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='300px'><b>Tatalaksana</b></td>"+
                    "</tr>"
                );
                for (i = 0; i < tabMode.getRowCount(); i++) {
                    htmlContent.append(
                        "<tr class='isi'>"+
                           "<td valign='top'>"+tbObat.getValueAt(i,0).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,1).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,2).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,3).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,4).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,5).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,6).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,7).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,8).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,9).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,10).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,11).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,12).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,13).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,14).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,15).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,16).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,17).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,18).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,19).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,20).toString()+"</td>"+ 
                            "<td valign='top'>"+tbObat.getValueAt(i,21).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,22).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,23).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,24).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,25).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,26).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,27).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,28).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,29).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,30).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,31).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,32).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,33).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,34).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,35).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,36).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,37).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,38).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,39).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,40).toString()+"</td>"+ 
                        "</tr>");
                }
                LoadHTML.setText(
                    "<html>"+
                      "<table width='4600px' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
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

                File f = new File("DataPenilaianAwalMedisRanap.html");            
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
                bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
                            "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                            "<table width='4600px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                "<tr class='isi2'>"+
                                    "<td valign='top' align='center'>"+
                                        "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                        akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                        akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                        "<font size='2' face='Tahoma'>DATA PENGKAJIAN AWAL MEDIS IGD<br><br></font>"+        
                                    "</td>"+
                               "</tr>"+
                            "</table>")
                );
                bw.close();                         
                Desktop.getDesktop().browse(f.toURI());

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
        if(tabMode.getRowCount()!=0){
            try {
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
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                try {
                    getData();
                    TabRawat.setSelectedIndex(0);
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {
        if(tbObat.getSelectedRow()!= -1){
            isMenu();
        }else{
            ChkAccor.setSelected(false);
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data yang mau ditampilkan...!!!!");
        }
    }

    /** Kolaps/perluas panel ringkasan HTML (ChkAccor) - pola identik RMTriaseIGD/RMPenilaianAwalKeperawatanIGD. */
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

    /** Tombol cetak di panel ringkasan HTML - berbagi logika cetak (cetakLaporanMedis) dgn menu klik-kanan "Laporan Pengkajian Medis", tapi menawarkan pilihan mode Lembar/PDF. */
    private void BtnPrint1ActionPerformed(java.awt.event.ActionEvent evt) {
        if(TNoRM1.getText().trim().equals("")||TPasien1.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu..!!!!");
        }else{
            String modeCetak = (String)JOptionPane.showInputDialog(null,"Silahkan pilih mode cetak..!","Cetak Laporan Pengkajian Awal Medis IGD",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Lembar","PDF"},"Lembar");
            if(modeCetak==null){
                return;
            }
            cetakLaporanMedis(modeCetak);
        }
    }

    private void KdDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdDokterKeyPressed
        
    }//GEN-LAST:event_KdDokterKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        if (dokter == null || !dokter.isDisplayable()) {
            dokter=new DlgCariDokter(null,false);
            dokter.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dokter.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(dokter.getTable().getSelectedRow()!= -1){        
                         KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                         NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                    }  
                    BtnDokter.requestFocus();
                    dokter=null;
                }
            });
            dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dokter.setLocationRelativeTo(internalFrame1);
        }   
        if (dokter == null) return;
        dokter.isCek();
        if (dokter.isVisible()) {
            dokter.toFront();
            return;
        }
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
        Valid.pindah(evt,TCari,BtnSimpan);
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void BBKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BBKeyPressed
        Valid.pindah(evt,TB,TD);
    }//GEN-LAST:event_BBKeyPressed

    private void TBKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TBKeyPressed
        Valid.pindah(evt,GCS,BB);
    }//GEN-LAST:event_TBKeyPressed

    private void NadiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NadiKeyPressed
        Valid.pindah(evt,TD,RR);
    }//GEN-LAST:event_NadiKeyPressed

    private void SuhuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SuhuKeyPressed
        Valid.pindah(evt,RR,SPO);
    }//GEN-LAST:event_SuhuKeyPressed

    private void TDKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TDKeyPressed
        Valid.pindah(evt,BB,Nadi);
    }//GEN-LAST:event_TDKeyPressed

    private void RRKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RRKeyPressed
        Valid.pindah(evt,Nadi,Suhu);
    }//GEN-LAST:event_RRKeyPressed

    private void AlergiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AlergiKeyPressed
        Valid.pindah(evt,RPO,Keadaan);
    }//GEN-LAST:event_AlergiKeyPressed

    private void AnamnesisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AnamnesisKeyPressed
        Valid.pindah(evt,TglAsuhan,Hubungan);
    }//GEN-LAST:event_AnamnesisKeyPressed

    private void KeluhanUtamaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeluhanUtamaKeyPressed
        Valid.pindah2(evt,Hubungan,RPS);
    }//GEN-LAST:event_KeluhanUtamaKeyPressed

    private void RPDKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPDKeyPressed
        Valid.pindah2(evt,RPK,RPO);
    }//GEN-LAST:event_RPDKeyPressed

    private void RPKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPKKeyPressed
        Valid.pindah2(evt,RPS,RPD);
    }//GEN-LAST:event_RPKKeyPressed

    private void RPOKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPOKeyPressed
        Valid.pindah2(evt,RPD,Alergi);
    }//GEN-LAST:event_RPOKeyPressed

    private void KetFisikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetFisikKeyPressed
        Valid.pindah2(evt,Ekstremitas,KetLokalis);
    }//GEN-LAST:event_KetFisikKeyPressed

    private void GCSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_GCSKeyPressed
        Valid.pindah(evt,Kesadaran,TB);
    }//GEN-LAST:event_GCSKeyPressed

    private void RPSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPSKeyPressed
        Valid.pindah2(evt,KeluhanUtama,RPK);
    }//GEN-LAST:event_RPSKeyPressed

    private void KeadaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeadaanKeyPressed
        Valid.pindah(evt,Alergi,Kesadaran);
    }//GEN-LAST:event_KeadaanKeyPressed

    private void KesadaranKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KesadaranKeyPressed
        Valid.pindah(evt,Keadaan,GCS);
    }//GEN-LAST:event_KesadaranKeyPressed

    private void SPOKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SPOKeyPressed
        Valid.pindah(evt,Suhu,Kepala);
    }//GEN-LAST:event_SPOKeyPressed

    private void KepalaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KepalaKeyPressed
        Valid.pindah(evt,SPO,Mata);
    }//GEN-LAST:event_KepalaKeyPressed

    private void GigiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_GigiKeyPressed
        Valid.pindah(evt,Mata,Leher);
    }//GEN-LAST:event_GigiKeyPressed

    private void LeherKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LeherKeyPressed
        Valid.pindah(evt,Gigi,Thoraks);
    }//GEN-LAST:event_LeherKeyPressed

    private void ThoraksKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ThoraksKeyPressed
        Valid.pindah(evt,Leher,Abdomen);
    }//GEN-LAST:event_ThoraksKeyPressed

    private void AbdomenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AbdomenKeyPressed
        Valid.pindah(evt,Thoraks,Genital);
    }//GEN-LAST:event_AbdomenKeyPressed

    private void GenitalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_GenitalKeyPressed
        Valid.pindah(evt,Abdomen,Ekstremitas);
    }//GEN-LAST:event_GenitalKeyPressed

    private void EkstremitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EkstremitasKeyPressed
        Valid.pindah(evt,Genital,KetFisik);
    }//GEN-LAST:event_EkstremitasKeyPressed

    private void KetLokalisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetLokalisKeyPressed
        Valid.pindah2(evt,KetFisik,EKG);
    }//GEN-LAST:event_KetLokalisKeyPressed

    private void EKGKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EKGKeyPressed
        Valid.pindah2(evt,KetLokalis,Radiologi);
    }//GEN-LAST:event_EKGKeyPressed

    private void DiagnosisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosisKeyPressed
        Valid.pindah2(evt,Laborat,Tatalaksana);
    }//GEN-LAST:event_DiagnosisKeyPressed

    private void TatalaksanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TatalaksanaKeyPressed
        Valid.pindah2(evt,Diagnosis,BtnSimpan);
    }//GEN-LAST:event_TatalaksanaKeyPressed

    private void TglAsuhanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglAsuhanKeyPressed
        Valid.pindah(evt,Tatalaksana,Anamnesis);
    }//GEN-LAST:event_TglAsuhanKeyPressed

    private void HubunganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HubunganKeyPressed
        Valid.pindah(evt,Anamnesis,KeluhanUtama);
    }//GEN-LAST:event_HubunganKeyPressed

    private void MnPenilaianMedisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnPenilaianMedisActionPerformed
        cetakLaporanMedis("Lembar");
    }//GEN-LAST:event_MnPenilaianMedisActionPerformed

    /**
     * Cetak "rptCetakPenilaianAwalMedisIGD.jasper" - dipakai bersama oleh menu
     * klik-kanan "Laporan Pengkajian Medis" (selalu mode "Lembar") dan tombol
     * cetak `BtnPrint1` di panel ringkasan (mode dipilih user lewat dialog).
     * mode "Lembar" = print-preview JasperViewer, mode "PDF" = tulis file
     * .pdf ke report/ lalu dibuka otomatis.
     */
    private void cetakLaporanMedis(String mode){
        if(tbObat.getSelectedRow()>-1){
            Map<String, Object> param = new HashMap<>();
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());
            param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
            try {
                param.put("lokalis",getClass().getResource("/picture/semua.png").openStream());
            } catch (Exception e) {
            }
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),6).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),5).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString()));

            String qryCetak =
                "select penilaian_medis_igd.*, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, "+
                "if(pasien.jk='L','Laki-Laki','Perempuan') as jk, dokter.nm_dokter, dokter.no_ijn_praktek, "+
                "TIMESTAMPDIFF(YEAR,pasien.tgl_lahir,CURDATE()) as umur, reg_periksa.tgl_registrasi, reg_periksa.jam_reg, "+
                "konsul_dokter.nm_dokter as konsul_nm_dokter, poliklinik.nm_poli as konsul_nm_poli "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join penilaian_medis_igd on reg_periksa.no_rawat=penilaian_medis_igd.no_rawat "+
                "inner join dokter on penilaian_medis_igd.kd_dokter=dokter.kd_dokter "+
                "left join dokter konsul_dokter on konsul_dokter.kd_dokter=penilaian_medis_igd.konsul_kd_dokter "+
                "left join poliklinik on poliklinik.kd_poli=penilaian_medis_igd.konsul_kd_poli "+
                "where penilaian_medis_igd.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'";

            if("PDF".equals(mode)){
                Valid.MyReportqrypdf("rptCetakPenilaianAwalMedisIGD.jasper","report","::[ Laporan Pengkajian Awal Medis IGD ]::",qryCetak,param);
            }else{
                Valid.MyReportqry("rptCetakPenilaianAwalMedisIGD.jasper","report","::[ Laporan Pengkajian Awal Medis IGD ]::",qryCetak,param);
            }
        }
    }

    private void MataKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MataKeyPressed
        Valid.pindah(evt,Kepala,Gigi);
    }//GEN-LAST:event_MataKeyPressed

    private void RadiologiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RadiologiKeyPressed
        Valid.pindah2(evt,EKG,Laborat);
    }//GEN-LAST:event_RadiologiKeyPressed

    private void LaboratKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LaboratKeyPressed
        Valid.pindah2(evt,Radiologi,Diagnosis);
    }//GEN-LAST:event_LaboratKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
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

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMPenilaianAwalMedisIGD dialog = new RMPenilaianAwalMedisIGD(new javax.swing.JFrame(), true);
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
    private widget.ComboBox Abdomen;
    private widget.TextBox Alergi;
    private widget.ComboBox Anamnesis;
    private widget.TextBox BB;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnDokter;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.TextArea Diagnosis;
    private widget.TextArea EKG;
    private widget.ComboBox Ekstremitas;
    private widget.PanelBiasa FormInput;
    private widget.TextBox GCS;
    private widget.ComboBox Genital;
    private widget.ComboBox Gigi;
    private widget.TextBox Hubungan;
    private widget.TextBox Jk;
    private widget.TextBox KdDokter;
    private widget.ComboBox Keadaan;
    private widget.TextArea KeluhanUtama;
    private widget.ComboBox Kepala;
    private widget.ComboBox Kesadaran;
    private widget.TextArea KetFisik;
    private widget.TextArea KetLokalis;
    private widget.Label LCount;
    private widget.TextArea Laborat;
    private widget.ComboBox Leher;
    private widget.editorpane LoadHTML;
    private widget.ScrollPane ScrollHTML;
    private widget.PanelBiasa PanelAccor;
    private widget.CekBox ChkAccor;
    private widget.PanelBiasa FormMenu;
    private widget.Label jLabelPasien1;
    private widget.TextBox TNoRM1;
    private widget.TextBox TPasien1;
    private widget.Button BtnPrint1;
    private widget.ComboBox Mata;
    private javax.swing.JMenuItem MnPenilaianMedis;
    private widget.TextBox Nadi;
    private widget.TextBox NmDokter;
    private usu.widget.glass.PanelGlass PanelWall;
    private widget.TextArea RPD;
    private widget.TextArea RPK;
    private widget.TextArea RPO;
    private widget.TextArea RPS;
    private widget.TextBox RR;
    private widget.TextArea Radiologi;
    private widget.TextBox SPO;
    private widget.ScrollPane Scroll;
    private widget.TextBox Suhu;
    private widget.TextBox TB;
    private widget.TextBox TCari;
    private widget.TextBox TD;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private javax.swing.JTabbedPane TabRawat;
    private widget.TextBox TanggalRegistrasi;
    private widget.TextArea Tatalaksana;
    private widget.Tanggal TglAsuhan;
    private widget.TextBox TglLahir;
    private widget.ComboBox Thoraks;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel100;
    private widget.Label jLabel101;
    private widget.Label jLabel102;
    private widget.Label jLabel103;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
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
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel35;
    private widget.Label jLabel37;
    private widget.Label jLabel38;
    private widget.Label jLabel39;
    private widget.Label jLabel40;
    private widget.Label jLabel41;
    private widget.Label jLabel42;
    private widget.Label jLabel44;
    private widget.Label jLabel45;
    private widget.Label jLabel46;
    private widget.Label jLabel49;
    private widget.Label jLabel50;
    private widget.Label jLabel51;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel79;
    private widget.Label jLabel8;
    private widget.Label jLabel80;
    private widget.Label jLabel81;
    private widget.Label jLabel82;
    private widget.Label jLabel9;
    private widget.Label jLabel94;
    private widget.Label jLabel99;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private widget.Label label11;
    private widget.Label label14;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane1;
    private widget.ScrollPane scrollPane10;
    private widget.ScrollPane scrollPane11;
    private widget.ScrollPane scrollPane12;
    private widget.ScrollPane scrollPane13;
    private widget.ScrollPane scrollPane2;
    private widget.ScrollPane scrollPane3;
    private widget.ScrollPane scrollPane4;
    private widget.ScrollPane scrollPane5;
    private widget.ScrollPane scrollPane7;
    private widget.ScrollPane scrollPane8;
    private widget.ScrollPane scrollPane9;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    // =====================================================================
    // [RM 03-2] Field baru - Pengkajian Awal Medis Gawat Darurat (formulir
    // target RM 03-2). Ditambahkan terisolasi di luar blok GEN-BEGIN/GEN-END
    // di atas supaya TIDAK ikut ditimpa NetBeans Form Editor saat form lama
    // dibuka/disimpan ulang lewat GUI builder - layout/posisi widget lama
    // TIDAK diubah sama sekali (pola sama seperti PACS di RMTriaseIGD.java).
    // Field lama (Kepala/Mata/Gigi/Leher/Thoraks/Abdomen/Genital/Ekstremitas/
    // GCS/Diagnosis) DIPENSIUNKAN dari UI aktif (disembunyikan, lihat
    // pensiunkanFieldLama()) - kolom DB-nya tetap ada, lihat
    // sik_penilaian_medis_igd_rm032.sql.
    // =====================================================================
    private widget.PanelBiasa panelRm032Atas;
    private widget.PanelBiasa panelRm032Tengah;
    private widget.PanelBiasa panelRm032Diagnosis;
    private widget.PanelBiasa panelRm032;

    // =====================================================================
    // [Sesi 1 - Restrukturisasi UI] Rangka 4 zona + bar pasien tab "Input
    // Pengkajian". Field lama (TNoRw/TPasien/TNoRM/TglLahir/Jk/KdDokter/
    // NmDokter/BtnDokter/TglAsuhan/Anamnesis/Hubungan) TIDAK diganti nama -
    // hanya disembunyikan atau dipindah parent (Container.add() Swing
    // otomatis melepas child dari parent lama), lihat buatBannerMedis().
    // Field baru di bawah ini murni tampilan, tidak pernah dibaca/ditulis
    // oleh getData()/tampil()/emptTeks()/simpan().
    // =====================================================================
    private widget.Label lblKartuNama, lblKartuInfo, lblKartuRM, lblKartuRawat;
    private widget.PanelBiasa barPasienMedis;
    private widget.PanelBiasa railMedis;
    private widget.ScrollPane scrollRailMedis;

    /**
     * [Bagian C] Satu entri navigasi bagian di rail kiri.
     *
     * `strip` = komponen judul section (widget.Label dari tambahBarisRm032Judul,
     * atau salah satu jLabel99/94/100/101/103 legacy), dipakai sebagai sasaran
     * scroll. Tidak perlu field `induk`/`urutBangun` seperti SeksiKep milik
     * Keperawatan - Medis tidak butuh sortir ulang (lihat daftarkanSeksiLegacyMedis()).
     */
    private static final class SeksiMedis {
        final String judul;
        final javax.swing.JComponent strip;
        widget.PanelBiasa baris;
        widget.Label lblJudul;
        widget.PanelBiasa aksen;
        SeksiMedis(String judul, javax.swing.JComponent strip){
            this.judul = judul; this.strip = strip;
        }
    }
    /** Daftar bagian, diisi tambahBarisRm032Judul() + daftarkanSeksiLegacyMedis(). */
    private final java.util.List<SeksiMedis> seksiMedis = new java.util.ArrayList<>();
    /** Index bagian yg sedang disorot di rail, -1 = belum ada. */
    private int seksiAktifMedis = -1;
    private widget.Label lblProgresJudulMedis, lblProgresHitungMedis;

    // -- A. Intervensi Pre Hospital --
    private widget.CekBox CekPreCervicalCollar, CekPreDefibrilasi, CekPreVtp, CekPreBalutBidai, CekPreNgt,
        CekPreRjp, CekPreIntubasi, CekPreDekompresiWsd, CekPreKateterUrin, CekPreInfus, CekPreTidakAda;
    private widget.TextBox TPreObatKet;

    // -- B. Survei Primer - Jalan Napas --
    private widget.CekBox CekJnPaten, CekJnObstruksiPartial, CekJnStridor, CekJnSnoring, CekJnGurgling,
        CekJnWheezing, CekJnObstruksiTotal, CekJnTraumaJalanNapas, CekJnFasialLeherInhalasi,
        CekJnRisikoAspirasi, CekJnBendaAsing;
    private widget.RadioButton RbJnAman, RbJnMengancamJiwa;
    private javax.swing.ButtonGroup bgJnKesimpulan;

    // -- C. Survei Primer - Pernapasan --
    private widget.RadioButton RbNapSpontan, RbNapTidakSpontan; private javax.swing.ButtonGroup bgNapPola;
    private widget.RadioButton RbNapReguler, RbNapIrreguler; private javax.swing.ButtonGroup bgNapReguler;
    private widget.RadioButton RbNapSimetris, RbNapAsimetris; private javax.swing.ButtonGroup bgNapGerakanDada;
    private widget.CekBox CekNapJejasKanan, CekNapJejasKiri;
    private widget.CekBox CekNapTipeNormal, CekNapTipeTakipneu, CekNapTipeKussmaul, CekNapTipeHiperventilasi,
        CekNapTipeBiot, CekNapTipeCheyneStoke, CekNapTipeApneustic, CekNapTipeRetraktif, CekNapTipeFlare;
    private widget.RadioButton RbNapAman, RbNapMengancamJiwa; private javax.swing.ButtonGroup bgNapKesimpulan;

    // -- D. Survei Primer - Sirkulasi --
    private widget.RadioButton RbSirReguler, RbSirIrreguler; private javax.swing.ButtonGroup bgSirNadi;
    private widget.RadioButton RbSirKuat, RbSirLemah; private javax.swing.ButtonGroup bgSirNadiKekuatan;
    private widget.RadioButton RbSirKulitNormal, RbSirKulitPucat; private javax.swing.ButtonGroup bgSirKulitWarna;
    private widget.RadioButton RbSirJaundice, RbSirCyanosis; private javax.swing.ButtonGroup bgSirKulitKondisi;
    private widget.CekBox CekSirBerkeringat;
    private widget.RadioButton RbSirHangat, RbSirDingin; private javax.swing.ButtonGroup bgSirAkral;
    private widget.RadioButton RbSirKering, RbSirBasah; private javax.swing.ButtonGroup bgSirAkralKondisi;
    private widget.RadioButton RbSirCrtKurang2, RbSirCrtLebih2; private javax.swing.ButtonGroup bgSirCrt;
    private widget.RadioButton RbSirAman, RbSirMengancamJiwa; private javax.swing.ButtonGroup bgSirKesimpulan;

    // -- E. Kategori Pasien (toggle Bayi Baru Lahir) + APGAR + Segitiga PAT --
    private widget.ComboBox KategoriPasien;
    private widget.PanelBiasa panelBayiBaruLahir;
    private widget.TextBox TApgarCukupBulan, TApgarCairanAmnionJernih, TApgarPernapasanMenangis, TApgarTonusOtot;
    private widget.TextBox TPatPenampilan, TPatUsahaNapas, TPatPerfusiKulit;

    // -- F. Disabilitas --
    private widget.TextBox TGcsE, TGcsV, TGcsM;
    private widget.RadioButton RbPupilIsokor, RbPupilAnisokor; private javax.swing.ButtonGroup bgPupil;
    private widget.TextBox TDiameterPupilKiri, TDiameterPupilKanan;
    private widget.RadioButton RbReflekKiriPositif, RbReflekKiriNegatif; private javax.swing.ButtonGroup bgReflekKiri;
    private widget.RadioButton RbReflekKananPositif, RbReflekKananNegatif; private javax.swing.ButtonGroup bgReflekKanan;
    private widget.TextBox TMeningealSigns;
    private widget.RadioButton RbLateralisasiKanan, RbLateralisasiKiri, RbLateralisasiTidakAda; private javax.swing.ButtonGroup bgLateralisasi;

    // -- G. Eksposur --
    private widget.CekBox CekEksDeformitas, CekEksEkskoriasi, CekEksContusio, CekEksAbrasi, CekEksPenetrasi,
        CekEksBurn, CekEksTenderness, CekEksLaserasi, CekEksSwelling, CekEksTidakTampakJelas;

    // -- H. Kepala Leher --
    private widget.RadioButton RbKonjungtivaPucat, RbKonjungtivaPink; private javax.swing.ButtonGroup bgKonjungtiva;
    private widget.RadioButton RbSkleraIkterik, RbSkleraTidakIkterik; private javax.swing.ButtonGroup bgSklera;
    private widget.RadioButton RbBibirLidahSianosis, RbBibirLidahTidakSianosis; private javax.swing.ButtonGroup bgBibirLidah;
    private widget.RadioButton RbMukosaKering, RbMukosaBasah; private javax.swing.ButtonGroup bgMukosaMulut;

    // -- I. Leher rinci --
    private widget.RadioButton RbDeviasiKanan, RbDeviasiKiri, RbDeviasiTidakAda; private javax.swing.ButtonGroup bgDeviasiTrakea;
    private widget.RadioButton RbJvpMeningkat, RbJvpTidak; private javax.swing.ButtonGroup bgJvp;
    private widget.RadioButton RbLnnTeraba, RbLnnTidak; private javax.swing.ButtonGroup bgLnn;
    private widget.RadioButton RbTiroidTeraba, RbTiroidTidak; private javax.swing.ButtonGroup bgTiroid;

    // -- J. Thorax --
    private widget.TextArea TJantung, TParu;

    // -- K. Abdomen & Pelvis --
    private widget.TextArea TPunggungPinggang, TEkstremitasPelvis;

    // (Section "Pemeriksaan Penunjang Tambahan" - CT Scan/USG - DIHAPUS atas
    // permintaan user; sudah pakai Pemeriksaan Penunjang bawaan: Radiologi/
    // Laborat/EKG existing + "Pemeriksaan Lainnya" baru di bawah.)
    // "Pemeriksaan Lainnya" SENGAJA dibuat sama persis gaya dgn EKG/Radiologi/
    // Laborat existing (TextArea 20x5 + ScrollPane 260x63, BUKAN checkbox+
    // textbox) - permintaan user supaya seragam dgn 3 field lama.
    private widget.TextArea PemeriksaanLainnya;

    // -- M. Diagnosis (Kerja/Banding) --
    private widget.CekBox CekDiagnosaKerja; private widget.TextArea TDiagnosaKerja;
    private widget.CekBox CekDiagnosaBanding; private widget.TextArea TDiagnosaBanding;

    // -- N. Konsul dr. Spesialis --
    // Dokter & Unit/Poli konsul dipilih lewat dialog pencarian (pola sama
    // seperti pemilihan Dokter/Unit di menu Registrasi, DlgReg.java), BUKAN
    // input manual - permintaan user.
    private widget.TextBox KonsulKdDokter, KonsulNmDokter;
    private widget.Button BtnKonsulDokter;
    private widget.TextBox KonsulKdPoli, KonsulNmPoli;
    private widget.Button BtnKonsulUnit;

    // -- O. Edukasi --
    private widget.CekBox CekEdukasiDiberikan, CekEdukasiTidakBisa;
    private widget.TextBox TEdukasiTidakBisaKet;

    // -- P. Kondisi Saat Pulang --
    private widget.RadioButton RbKondisiMembaik, RbKondisiMemburuk, RbKondisiTetap, RbKondisiMeninggal, RbKondisiDoa;
    private javax.swing.ButtonGroup bgKondisiPulang;
    private widget.Tanggal DTPKondisiPulangJam;

    // -- Q. Tanda Vital Terakhir --
    private widget.TextBox TTdAkhir, TNadiAkhir, TRrAkhir, TSuhuAkhir;

    /**
     * [RM 03-2] Sembunyikan field-field lama yang dipensiunkan dari UI aktif
     * (digantikan section baru RM 03-2 yang lebih rinci). Kolom DB-nya TETAP
     * ADA (data lama tetap terbaca) - lihat sik_penilaian_medis_igd_rm032.sql.
     * Dipanggil sekali dari konstruktor, setelah initComponents(). TIDAK
     * mengubah layout/posisi widget lama - cuma disembunyikan.
     */
    private void pensiunkanFieldLama(){
        GCS.setVisible(false); jLabel28.setVisible(false);
        Kepala.setVisible(false); jLabel40.setVisible(false);
        Mata.setVisible(false); jLabel42.setVisible(false);
        Gigi.setVisible(false); jLabel44.setVisible(false);
        Leher.setVisible(false); jLabel45.setVisible(false);
        Thoraks.setVisible(false); jLabel46.setVisible(false);
        Abdomen.setVisible(false); jLabel49.setVisible(false);
        Genital.setVisible(false); jLabel50.setVisible(false);
        Ekstremitas.setVisible(false); jLabel51.setVisible(false);
        scrollPane12.setVisible(false); jLabel102.setVisible(false);

        // [RM 03-2] Judul section lama (I-VI) DI-RENOMOR jadi VIII, IX, X,
        // XV, XVII supaya urut dengan section baru yang disisipkan di
        // sekitarnya (lihat urutan lengkap di initRm032Tambahan()) - CUMA
        // teks judulnya yang diganti, field/data/posisi TIDAK disentuh sama
        // sekali (masih di dalam initComponents()/GEN block, tidak diedit).
        // "Tatalaksana" jadi "Rencana/Tindakan/Terapi" sesuai istilah RM 03-2
        // - field & datanya (widget Tatalaksana, kolom DB `tata`) tetap sama.
        jLabel99.setText("VIII. RIWAYAT KESEHATAN");
        jLabel94.setText("IX. PEMERIKSAAN FISIK");
        jLabel100.setText("X. STATUS LOKALIS");
        jLabel101.setText("XV. PEMERIKSAAN PENUNJANG");
        jLabel103.setText("XVII. RENCANA/TINDAKAN/TERAPI");
    }

    private widget.Label labelRm032(String teks){
        widget.Label lbl = new widget.Label();
        lbl.setText(teks);
        return lbl;
    }

    private widget.PanelBiasa panelPilihan(javax.swing.ButtonGroup bg, widget.RadioButton... rb){
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,10,0));
        for(widget.RadioButton r : rb){ bg.add(r); p.add(r); }
        return p;
    }

    private widget.PanelBiasa panelChecklist(widget.CekBox... cek){
        // 2 kolom (bukan 3) - GridLayout(0,3) sebelumnya memaksa SEMUA item
        // selebar item terpanjang (mis. "Risiko Aspirasi (Perdarahan/
        // Muntahan)"), yang utk 11 item x 3 kolom bisa >900px, melebihi
        // lebar panel (840px) dan mengacaukan perhitungan kolom GridBagLayout
        // induk utk SEMUA baris berikutnya di panel yang sama (efeknya
        // sampai ke section-section sesudahnya). 2 kolom tetap muat aman.
        // GridLayout dipilih (bukan FlowLayout) supaya tinggi panel tetap
        // deterministik/tidak tergantung lebar container saat preferred
        // size dihitung.
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new java.awt.GridLayout(0,2,10,2));
        for(widget.CekBox c : cek){ p.add(c); }
        return p;
    }

    private java.awt.GridBagConstraints rm032Constraint(int gridx,int gridy){
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = gridx; c.gridy = gridy;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.insets = new java.awt.Insets(3,5,3,5);
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        return c;
    }

    private int tambahBarisRm032Judul(widget.PanelBiasa panel,int row,String judul){
        widget.Label lbl = new widget.Label();
        lbl.setText(judul);
        lbl.setFont(lbl.getFont().deriveFont(java.awt.Font.BOLD));
        // widget.Label default-nya rata kanan (cocok utk label field sempit) -
        // judul section melebar 2 kolom jadi perlu dipaksa rata kiri.
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        java.awt.GridBagConstraints c = rm032Constraint(0,row);
        c.gridwidth = 2;
        panel.add(lbl,c);
        // [Bagian C] Funnel ini jadi tempat pendaftaran rail navigasi - daftar
        // bagian ikut benar sendiri kalau section berikutnya ditambah/dipindah,
        // tanpa daftar judul kedua yg bisa basi.
        seksiMedis.add(new SeksiMedis(judul, lbl));
        return row+1;
    }

    private int tambahBarisRm032Field(widget.PanelBiasa panel,int row,String label,java.awt.Component field){
        widget.Label lbl = new widget.Label();
        lbl.setText(label+" :");
        java.awt.GridBagConstraints c1 = rm032Constraint(0,row);
        panel.add(lbl,c1);
        java.awt.GridBagConstraints c2 = rm032Constraint(1,row);
        panel.add(field,c2);
        return row+1;
    }

    /**
     * [RM 03-2] Membangun seluruh section baru form Pengkajian Awal Medis
     * Gawat Darurat (RM 03-2) - Intervensi Pre Hospital, Survei Primer,
     * Bayi Baru Lahir/APGAR/PAT, Disabilitas, Eksposur, Kepala Leher, Leher
     * rinci, Thorax, Abdomen & Pelvis, Pemeriksaan Penunjang tambahan,
     * Diagnosis (Kerja/Banding), Konsul Spesialis, Edukasi, Kondisi Pulang,
     * Tanda Vital Terakhir. Ditambahkan sebagai 1 panel GridBagLayout BARU
     * di bawah konten form lama (FormInput pakai null layout / posisi
     * absolut - widget/posisi lama TIDAK diubah sama sekali, panel ini
     * cuma ditempel di ruang kosong di bawahnya). Dipanggil sekali dari
     * konstruktor, setelah initComponents().
     */
    private void initRm032Tambahan(){
        // ===== Panel ATAS: Intervensi Pre Hospital, Survei Primer, Kategori
        // Pasien+Bayi Baru Lahir, Disabilitas, Eksposur - ditempel SEBELUM
        // "I. RIWAYAT KESEHATAN" (jLabel99, y=70) supaya urutannya sama
        // dengan formulir fisik RM 03-2 (permintaan user). =====
        panelRm032Atas = new widget.PanelBiasa();
        panelRm032Atas.setLayout(new java.awt.GridBagLayout());
        int row = 0;

        // -- A. Intervensi Pre Hospital --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "I. INTERVENSI PRE HOSPITAL");
        CekPreCervicalCollar = new widget.CekBox(); CekPreCervicalCollar.setText("Cervical Collar");
        CekPreDefibrilasi = new widget.CekBox(); CekPreDefibrilasi.setText("Defibrilasi");
        CekPreVtp = new widget.CekBox(); CekPreVtp.setText("VTP");
        CekPreBalutBidai = new widget.CekBox(); CekPreBalutBidai.setText("Balut/Bidai");
        CekPreNgt = new widget.CekBox(); CekPreNgt.setText("NGT");
        CekPreRjp = new widget.CekBox(); CekPreRjp.setText("RJP");
        CekPreIntubasi = new widget.CekBox(); CekPreIntubasi.setText("Intubasi");
        CekPreDekompresiWsd = new widget.CekBox(); CekPreDekompresiWsd.setText("Dekompresi Jarum/WSD");
        CekPreKateterUrin = new widget.CekBox(); CekPreKateterUrin.setText("Kateter Urin");
        CekPreInfus = new widget.CekBox(); CekPreInfus.setText("Infus");
        CekPreTidakAda = new widget.CekBox(); CekPreTidakAda.setText("Tidak Ada");
        java.awt.GridBagConstraints cA = rm032Constraint(0,row); cA.gridwidth=2;
        panelRm032Atas.add(panelChecklist(CekPreCervicalCollar,CekPreDefibrilasi,CekPreVtp,CekPreBalutBidai,CekPreNgt,
            CekPreRjp,CekPreIntubasi,CekPreDekompresiWsd,CekPreKateterUrin,CekPreInfus,CekPreTidakAda), cA);
        row++;
        TPreObatKet = new widget.TextBox(); TPreObatKet.setPreferredSize(new java.awt.Dimension(250,23));
        row = tambahBarisRm032Field(panelRm032Atas, row, "Obat (Pre Hospital)", TPreObatKet);

        // -- B. Survei Primer - Jalan Napas --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "II. SURVEI PRIMER - JALAN NAPAS");
        CekJnPaten = new widget.CekBox(); CekJnPaten.setText("Paten");
        CekJnObstruksiPartial = new widget.CekBox(); CekJnObstruksiPartial.setText("Obstruksi Partial");
        CekJnStridor = new widget.CekBox(); CekJnStridor.setText("Stridor");
        CekJnSnoring = new widget.CekBox(); CekJnSnoring.setText("Snoring");
        CekJnGurgling = new widget.CekBox(); CekJnGurgling.setText("Gurgling");
        CekJnWheezing = new widget.CekBox(); CekJnWheezing.setText("Wheezing");
        CekJnObstruksiTotal = new widget.CekBox(); CekJnObstruksiTotal.setText("Obstruksi Total");
        CekJnTraumaJalanNapas = new widget.CekBox(); CekJnTraumaJalanNapas.setText("Trauma Jalan Napas");
        CekJnFasialLeherInhalasi = new widget.CekBox(); CekJnFasialLeherInhalasi.setText("Fasial/Leher/Inhalasi");
        CekJnRisikoAspirasi = new widget.CekBox(); CekJnRisikoAspirasi.setText("Risiko Aspirasi (Perdarahan/Muntahan)");
        CekJnBendaAsing = new widget.CekBox(); CekJnBendaAsing.setText("Benda Asing");
        java.awt.GridBagConstraints cB = rm032Constraint(0,row); cB.gridwidth=2;
        panelRm032Atas.add(panelChecklist(CekJnPaten,CekJnObstruksiPartial,CekJnStridor,CekJnSnoring,CekJnGurgling,
            CekJnWheezing,CekJnObstruksiTotal,CekJnTraumaJalanNapas,CekJnFasialLeherInhalasi,
            CekJnRisikoAspirasi,CekJnBendaAsing), cB);
        row++;
        bgJnKesimpulan = new javax.swing.ButtonGroup();
        RbJnAman = new widget.RadioButton(); RbJnAman.setText("Aman");
        RbJnMengancamJiwa = new widget.RadioButton(); RbJnMengancamJiwa.setText("Mengancam Jiwa");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Kesimpulan Jalan Napas", panelPilihan(bgJnKesimpulan,RbJnAman,RbJnMengancamJiwa));

        // -- C. Survei Primer - Pernapasan --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "III. SURVEI PRIMER - PERNAPASAN");
        bgNapPola = new javax.swing.ButtonGroup();
        RbNapSpontan = new widget.RadioButton(); RbNapSpontan.setText("Spontan");
        RbNapTidakSpontan = new widget.RadioButton(); RbNapTidakSpontan.setText("Tidak Spontan");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Pola Napas", panelPilihan(bgNapPola,RbNapSpontan,RbNapTidakSpontan));
        bgNapReguler = new javax.swing.ButtonGroup();
        RbNapReguler = new widget.RadioButton(); RbNapReguler.setText("Reguler");
        RbNapIrreguler = new widget.RadioButton(); RbNapIrreguler.setText("Irreguler");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Keteraturan Napas", panelPilihan(bgNapReguler,RbNapReguler,RbNapIrreguler));
        bgNapGerakanDada = new javax.swing.ButtonGroup();
        RbNapSimetris = new widget.RadioButton(); RbNapSimetris.setText("Simetris");
        RbNapAsimetris = new widget.RadioButton(); RbNapAsimetris.setText("Asimetris");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Gerakan Dada", panelPilihan(bgNapGerakanDada,RbNapSimetris,RbNapAsimetris));
        CekNapJejasKanan = new widget.CekBox(); CekNapJejasKanan.setText("Jejas Dinding Dada Kanan");
        CekNapJejasKiri = new widget.CekBox(); CekNapJejasKiri.setText("Jejas Dinding Dada Kiri");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Jejas Dinding Dada", panelChecklist(CekNapJejasKanan,CekNapJejasKiri));
        CekNapTipeNormal = new widget.CekBox(); CekNapTipeNormal.setText("Normal");
        CekNapTipeTakipneu = new widget.CekBox(); CekNapTipeTakipneu.setText("Takipneu");
        CekNapTipeKussmaul = new widget.CekBox(); CekNapTipeKussmaul.setText("Kussmaul");
        CekNapTipeHiperventilasi = new widget.CekBox(); CekNapTipeHiperventilasi.setText("Hiperventilasi");
        CekNapTipeBiot = new widget.CekBox(); CekNapTipeBiot.setText("Biot");
        CekNapTipeCheyneStoke = new widget.CekBox(); CekNapTipeCheyneStoke.setText("Cheyne Stoke");
        CekNapTipeApneustic = new widget.CekBox(); CekNapTipeApneustic.setText("Apneustic");
        CekNapTipeRetraktif = new widget.CekBox(); CekNapTipeRetraktif.setText("Retraktif");
        CekNapTipeFlare = new widget.CekBox(); CekNapTipeFlare.setText("Flare");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Tipe Pernapasan", panelChecklist(CekNapTipeNormal,CekNapTipeTakipneu,
            CekNapTipeKussmaul,CekNapTipeHiperventilasi,CekNapTipeBiot,CekNapTipeCheyneStoke,
            CekNapTipeApneustic,CekNapTipeRetraktif,CekNapTipeFlare));
        bgNapKesimpulan = new javax.swing.ButtonGroup();
        RbNapAman = new widget.RadioButton(); RbNapAman.setText("Aman");
        RbNapMengancamJiwa = new widget.RadioButton(); RbNapMengancamJiwa.setText("Mengancam Jiwa");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Kesimpulan Pernapasan", panelPilihan(bgNapKesimpulan,RbNapAman,RbNapMengancamJiwa));

        // -- D. Survei Primer - Sirkulasi --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "IV. SURVEI PRIMER - SIRKULASI");
        bgSirNadi = new javax.swing.ButtonGroup();
        RbSirReguler = new widget.RadioButton(); RbSirReguler.setText("Reguler");
        RbSirIrreguler = new widget.RadioButton(); RbSirIrreguler.setText("Irreguler");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Nadi", panelPilihan(bgSirNadi,RbSirReguler,RbSirIrreguler));
        bgSirNadiKekuatan = new javax.swing.ButtonGroup();
        RbSirKuat = new widget.RadioButton(); RbSirKuat.setText("Kuat");
        RbSirLemah = new widget.RadioButton(); RbSirLemah.setText("Lemah");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Kekuatan Nadi", panelPilihan(bgSirNadiKekuatan,RbSirKuat,RbSirLemah));
        bgSirKulitWarna = new javax.swing.ButtonGroup();
        RbSirKulitNormal = new widget.RadioButton(); RbSirKulitNormal.setText("Normal");
        RbSirKulitPucat = new widget.RadioButton(); RbSirKulitPucat.setText("Pucat");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Kulit/Mukosa", panelPilihan(bgSirKulitWarna,RbSirKulitNormal,RbSirKulitPucat));
        bgSirKulitKondisi = new javax.swing.ButtonGroup();
        RbSirJaundice = new widget.RadioButton(); RbSirJaundice.setText("Jaundice");
        RbSirCyanosis = new widget.RadioButton(); RbSirCyanosis.setText("Cyanosis");
        CekSirBerkeringat = new widget.CekBox(); CekSirBerkeringat.setText("Berkeringat");
        widget.PanelBiasa panelSirKulitKondisi = panelPilihan(bgSirKulitKondisi,RbSirJaundice,RbSirCyanosis);
        panelSirKulitKondisi.add(CekSirBerkeringat);
        row = tambahBarisRm032Field(panelRm032Atas, row, "Kondisi Kulit", panelSirKulitKondisi);
        bgSirAkral = new javax.swing.ButtonGroup();
        RbSirHangat = new widget.RadioButton(); RbSirHangat.setText("Hangat");
        RbSirDingin = new widget.RadioButton(); RbSirDingin.setText("Dingin");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Akral", panelPilihan(bgSirAkral,RbSirHangat,RbSirDingin));
        bgSirAkralKondisi = new javax.swing.ButtonGroup();
        RbSirKering = new widget.RadioButton(); RbSirKering.setText("Kering");
        RbSirBasah = new widget.RadioButton(); RbSirBasah.setText("Basah");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Kondisi Akral", panelPilihan(bgSirAkralKondisi,RbSirKering,RbSirBasah));
        bgSirCrt = new javax.swing.ButtonGroup();
        RbSirCrtKurang2 = new widget.RadioButton(); RbSirCrtKurang2.setText("<2 detik");
        RbSirCrtLebih2 = new widget.RadioButton(); RbSirCrtLebih2.setText(">2 detik");
        row = tambahBarisRm032Field(panelRm032Atas, row, "CRT", panelPilihan(bgSirCrt,RbSirCrtKurang2,RbSirCrtLebih2));
        bgSirKesimpulan = new javax.swing.ButtonGroup();
        RbSirAman = new widget.RadioButton(); RbSirAman.setText("Aman");
        RbSirMengancamJiwa = new widget.RadioButton(); RbSirMengancamJiwa.setText("Mengancam Jiwa");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Kesimpulan Sirkulasi", panelPilihan(bgSirKesimpulan,RbSirAman,RbSirMengancamJiwa));

        // -- E. Kategori Pasien + Bayi Baru Lahir (APGAR/PAT) --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "V. KATEGORI PASIEN");
        KategoriPasien = new widget.ComboBox();
        KategoriPasien.setPreferredSize(new java.awt.Dimension(200,23));
        KategoriPasien.addItem("Umum");
        KategoriPasien.addItem("Bayi Baru Lahir");
        KategoriPasien.addItemListener((java.awt.event.ItemEvent e) -> perbaruiVisibilitasBayiBaruLahir());
        row = tambahBarisRm032Field(panelRm032Atas, row, "Kategori Pasien", KategoriPasien);

        panelBayiBaruLahir = new widget.PanelBiasa();
        panelBayiBaruLahir.setLayout(new java.awt.GridBagLayout());
        int rowBbl = 0;
        java.awt.GridBagConstraints cBbl;

        cBbl = rm032Constraint(0,rowBbl); panelBayiBaruLahir.add(labelRm032("Cukup Bulan :"),cBbl);
        TApgarCukupBulan = new widget.TextBox(); TApgarCukupBulan.setPreferredSize(new java.awt.Dimension(200,23));
        cBbl = rm032Constraint(1,rowBbl); panelBayiBaruLahir.add(TApgarCukupBulan,cBbl); rowBbl++;

        cBbl = rm032Constraint(0,rowBbl); panelBayiBaruLahir.add(labelRm032("Cairan Amnion Jernih :"),cBbl);
        TApgarCairanAmnionJernih = new widget.TextBox(); TApgarCairanAmnionJernih.setPreferredSize(new java.awt.Dimension(200,23));
        cBbl = rm032Constraint(1,rowBbl); panelBayiBaruLahir.add(TApgarCairanAmnionJernih,cBbl); rowBbl++;

        cBbl = rm032Constraint(0,rowBbl); panelBayiBaruLahir.add(labelRm032("Pernapasan/Menangis :"),cBbl);
        TApgarPernapasanMenangis = new widget.TextBox(); TApgarPernapasanMenangis.setPreferredSize(new java.awt.Dimension(200,23));
        cBbl = rm032Constraint(1,rowBbl); panelBayiBaruLahir.add(TApgarPernapasanMenangis,cBbl); rowBbl++;

        cBbl = rm032Constraint(0,rowBbl); panelBayiBaruLahir.add(labelRm032("Tonus Otot :"),cBbl);
        TApgarTonusOtot = new widget.TextBox(); TApgarTonusOtot.setPreferredSize(new java.awt.Dimension(200,23));
        cBbl = rm032Constraint(1,rowBbl); panelBayiBaruLahir.add(TApgarTonusOtot,cBbl); rowBbl++;

        cBbl = rm032Constraint(0,rowBbl); panelBayiBaruLahir.add(labelRm032("Penampilan (PAT) :"),cBbl);
        TPatPenampilan = new widget.TextBox(); TPatPenampilan.setPreferredSize(new java.awt.Dimension(200,23));
        cBbl = rm032Constraint(1,rowBbl); panelBayiBaruLahir.add(TPatPenampilan,cBbl); rowBbl++;

        cBbl = rm032Constraint(0,rowBbl); panelBayiBaruLahir.add(labelRm032("Usaha Napas (PAT) :"),cBbl);
        TPatUsahaNapas = new widget.TextBox(); TPatUsahaNapas.setPreferredSize(new java.awt.Dimension(200,23));
        cBbl = rm032Constraint(1,rowBbl); panelBayiBaruLahir.add(TPatUsahaNapas,cBbl); rowBbl++;

        cBbl = rm032Constraint(0,rowBbl); panelBayiBaruLahir.add(labelRm032("Perfusi Kulit (PAT) :"),cBbl);
        TPatPerfusiKulit = new widget.TextBox(); TPatPerfusiKulit.setPreferredSize(new java.awt.Dimension(200,23));
        cBbl = rm032Constraint(1,rowBbl); panelBayiBaruLahir.add(TPatPerfusiKulit,cBbl); rowBbl++;

        java.awt.GridBagConstraints cBblOuter = rm032Constraint(0,row); cBblOuter.gridwidth=2;
        panelRm032Atas.add(panelBayiBaruLahir, cBblOuter);
        row++;
        // Sembunyikan panel Bayi Baru Lahir SEBELUM ukuran panelRm032Atas
        // dihitung di bawah (GridBagLayout mengecualikan komponen yang
        // isVisible()==false dari perhitungan preferred size) - kalau
        // disembunyikan SETELAH dihitung, ruang kosongnya kadung ke-reserve
        // dan menyisakan celah kosong di tampilan.
        perbaruiVisibilitasBayiBaruLahir();

        // -- F. Disabilitas --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "VI. DISABILITAS");
        widget.PanelBiasa panelGcs = new widget.PanelBiasa();
        panelGcs.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,10,0));
        TGcsE = new widget.TextBox(); TGcsE.setPreferredSize(new java.awt.Dimension(40,23));
        TGcsV = new widget.TextBox(); TGcsV.setPreferredSize(new java.awt.Dimension(40,23));
        TGcsM = new widget.TextBox(); TGcsM.setPreferredSize(new java.awt.Dimension(40,23));
        panelGcs.add(labelRm032("E:")); panelGcs.add(TGcsE);
        panelGcs.add(labelRm032("V:")); panelGcs.add(TGcsV);
        panelGcs.add(labelRm032("M:")); panelGcs.add(TGcsM);
        row = tambahBarisRm032Field(panelRm032Atas, row, "GCS", panelGcs);

        bgPupil = new javax.swing.ButtonGroup();
        RbPupilIsokor = new widget.RadioButton(); RbPupilIsokor.setText("Isokor");
        RbPupilAnisokor = new widget.RadioButton(); RbPupilAnisokor.setText("Anisokor");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Pupil", panelPilihan(bgPupil,RbPupilIsokor,RbPupilAnisokor));

        widget.PanelBiasa panelDiameter = new widget.PanelBiasa();
        panelDiameter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,10,0));
        TDiameterPupilKiri = new widget.TextBox(); TDiameterPupilKiri.setPreferredSize(new java.awt.Dimension(60,23));
        TDiameterPupilKanan = new widget.TextBox(); TDiameterPupilKanan.setPreferredSize(new java.awt.Dimension(60,23));
        panelDiameter.add(labelRm032("Kiri:")); panelDiameter.add(TDiameterPupilKiri);
        panelDiameter.add(labelRm032("Kanan:")); panelDiameter.add(TDiameterPupilKanan);
        row = tambahBarisRm032Field(panelRm032Atas, row, "Diameter Pupil (mm)", panelDiameter);

        bgReflekKiri = new javax.swing.ButtonGroup();
        RbReflekKiriPositif = new widget.RadioButton(); RbReflekKiriPositif.setText("Positif");
        RbReflekKiriNegatif = new widget.RadioButton(); RbReflekKiriNegatif.setText("Negatif");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Reflek Cahaya Kiri", panelPilihan(bgReflekKiri,RbReflekKiriPositif,RbReflekKiriNegatif));

        bgReflekKanan = new javax.swing.ButtonGroup();
        RbReflekKananPositif = new widget.RadioButton(); RbReflekKananPositif.setText("Positif");
        RbReflekKananNegatif = new widget.RadioButton(); RbReflekKananNegatif.setText("Negatif");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Reflek Cahaya Kanan", panelPilihan(bgReflekKanan,RbReflekKananPositif,RbReflekKananNegatif));

        TMeningealSigns = new widget.TextBox(); TMeningealSigns.setPreferredSize(new java.awt.Dimension(300,23));
        row = tambahBarisRm032Field(panelRm032Atas, row, "Meningeal Signs", TMeningealSigns);

        bgLateralisasi = new javax.swing.ButtonGroup();
        RbLateralisasiKanan = new widget.RadioButton(); RbLateralisasiKanan.setText("Kanan");
        RbLateralisasiKiri = new widget.RadioButton(); RbLateralisasiKiri.setText("Kiri");
        RbLateralisasiTidakAda = new widget.RadioButton(); RbLateralisasiTidakAda.setText("Tidak Ada");
        row = tambahBarisRm032Field(panelRm032Atas, row, "Lateralisasi", panelPilihan(bgLateralisasi,RbLateralisasiKanan,RbLateralisasiKiri,RbLateralisasiTidakAda));

        // -- G. Eksposur --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "VII. EKSPOSUR");
        CekEksDeformitas = new widget.CekBox(); CekEksDeformitas.setText("Deformitas");
        CekEksEkskoriasi = new widget.CekBox(); CekEksEkskoriasi.setText("Ekskoriasi");
        CekEksContusio = new widget.CekBox(); CekEksContusio.setText("Contusio");
        CekEksAbrasi = new widget.CekBox(); CekEksAbrasi.setText("Abrasi");
        CekEksPenetrasi = new widget.CekBox(); CekEksPenetrasi.setText("Penetrasi");
        CekEksBurn = new widget.CekBox(); CekEksBurn.setText("Burn");
        CekEksTenderness = new widget.CekBox(); CekEksTenderness.setText("Tenderness");
        CekEksLaserasi = new widget.CekBox(); CekEksLaserasi.setText("Laserasi");
        CekEksSwelling = new widget.CekBox(); CekEksSwelling.setText("Swelling");
        CekEksTidakTampakJelas = new widget.CekBox(); CekEksTidakTampakJelas.setText("Tidak Tampak Jelas");
        java.awt.GridBagConstraints cG = rm032Constraint(0,row); cG.gridwidth=2;
        panelRm032Atas.add(panelChecklist(CekEksDeformitas,CekEksEkskoriasi,CekEksContusio,CekEksAbrasi,
            CekEksPenetrasi,CekEksBurn,CekEksTenderness,CekEksLaserasi,CekEksSwelling,CekEksTidakTampakJelas), cG);
        row++;

        // ===== Panel TENGAH: Kepala Leher, Leher rinci, Thorax, Abdomen &
        // Pelvis - ditempel SEBELUM "IV. PEMERIKSAAN PENUNJANG" (jLabel101,
        // y asli 870) supaya urutannya sama dengan formulir fisik (mengikuti
        // "III. STATUS LOKALIS" yang sudah berisi diagram anatomi). =====
        panelRm032Tengah = new widget.PanelBiasa();
        panelRm032Tengah.setLayout(new java.awt.GridBagLayout());
        row = 0;

        // -- H. Kepala Leher --
        row = tambahBarisRm032Judul(panelRm032Tengah, row, "XI. KEPALA LEHER");
        bgKonjungtiva = new javax.swing.ButtonGroup();
        RbKonjungtivaPucat = new widget.RadioButton(); RbKonjungtivaPucat.setText("Pucat");
        RbKonjungtivaPink = new widget.RadioButton(); RbKonjungtivaPink.setText("Pink");
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Conjunctiva", panelPilihan(bgKonjungtiva,RbKonjungtivaPucat,RbKonjungtivaPink));

        bgSklera = new javax.swing.ButtonGroup();
        RbSkleraIkterik = new widget.RadioButton(); RbSkleraIkterik.setText("Ikterik");
        RbSkleraTidakIkterik = new widget.RadioButton(); RbSkleraTidakIkterik.setText("Tidak Ikterik");
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Sklera", panelPilihan(bgSklera,RbSkleraIkterik,RbSkleraTidakIkterik));

        bgBibirLidah = new javax.swing.ButtonGroup();
        RbBibirLidahSianosis = new widget.RadioButton(); RbBibirLidahSianosis.setText("Sianosis");
        RbBibirLidahTidakSianosis = new widget.RadioButton(); RbBibirLidahTidakSianosis.setText("Tidak Sianosis");
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Bibir/Lidah", panelPilihan(bgBibirLidah,RbBibirLidahSianosis,RbBibirLidahTidakSianosis));

        bgMukosaMulut = new javax.swing.ButtonGroup();
        RbMukosaKering = new widget.RadioButton(); RbMukosaKering.setText("Kering");
        RbMukosaBasah = new widget.RadioButton(); RbMukosaBasah.setText("Basah");
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Mukosa", panelPilihan(bgMukosaMulut,RbMukosaKering,RbMukosaBasah));

        // -- I. Leher rinci --
        row = tambahBarisRm032Judul(panelRm032Tengah, row, "XII. LEHER");
        bgDeviasiTrakea = new javax.swing.ButtonGroup();
        RbDeviasiKanan = new widget.RadioButton(); RbDeviasiKanan.setText("Kanan");
        RbDeviasiKiri = new widget.RadioButton(); RbDeviasiKiri.setText("Kiri");
        RbDeviasiTidakAda = new widget.RadioButton(); RbDeviasiTidakAda.setText("Tidak Ada");
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Deviasi Trakea", panelPilihan(bgDeviasiTrakea,RbDeviasiKanan,RbDeviasiKiri,RbDeviasiTidakAda));

        bgJvp = new javax.swing.ButtonGroup();
        RbJvpMeningkat = new widget.RadioButton(); RbJvpMeningkat.setText("Meningkat");
        RbJvpTidak = new widget.RadioButton(); RbJvpTidak.setText("Tidak");
        row = tambahBarisRm032Field(panelRm032Tengah, row, "JVP", panelPilihan(bgJvp,RbJvpMeningkat,RbJvpTidak));

        bgLnn = new javax.swing.ButtonGroup();
        RbLnnTeraba = new widget.RadioButton(); RbLnnTeraba.setText("Teraba");
        RbLnnTidak = new widget.RadioButton(); RbLnnTidak.setText("Tidak");
        row = tambahBarisRm032Field(panelRm032Tengah, row, "LNN", panelPilihan(bgLnn,RbLnnTeraba,RbLnnTidak));

        bgTiroid = new javax.swing.ButtonGroup();
        RbTiroidTeraba = new widget.RadioButton(); RbTiroidTeraba.setText("Teraba");
        RbTiroidTidak = new widget.RadioButton(); RbTiroidTidak.setText("Tidak");
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Tiroid", panelPilihan(bgTiroid,RbTiroidTeraba,RbTiroidTidak));

        // -- J. Thorax --
        row = tambahBarisRm032Judul(panelRm032Tengah, row, "XIII. THORAX");
        TJantung = new widget.TextArea(); TJantung.setRows(2); TJantung.setColumns(40);
        widget.ScrollPane scrollJantung = new widget.ScrollPane(); scrollJantung.setViewportView(TJantung);
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Jantung", scrollJantung);
        TParu = new widget.TextArea(); TParu.setRows(2); TParu.setColumns(40);
        widget.ScrollPane scrollParu = new widget.ScrollPane(); scrollParu.setViewportView(TParu);
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Paru", scrollParu);

        // -- K. Abdomen & Pelvis --
        row = tambahBarisRm032Judul(panelRm032Tengah, row, "XIV. ABDOMEN & PELVIS");
        TPunggungPinggang = new widget.TextArea(); TPunggungPinggang.setRows(2); TPunggungPinggang.setColumns(40);
        widget.ScrollPane scrollPunggung = new widget.ScrollPane(); scrollPunggung.setViewportView(TPunggungPinggang);
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Punggung & Pinggang", scrollPunggung);
        TEkstremitasPelvis = new widget.TextArea(); TEkstremitasPelvis.setRows(2); TEkstremitasPelvis.setColumns(40);
        widget.ScrollPane scrollEksPelvis = new widget.ScrollPane(); scrollEksPelvis.setViewportView(TEkstremitasPelvis);
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Ekstremitas (Abdomen & Pelvis)", scrollEksPelvis);

        // ===== Panel DIAGNOSIS: Diagnosa Kerja/Banding - ditempel SEBELUM
        // "VI. TATALAKSANA", tepat di bawah Pemeriksaan Penunjang (EKG/
        // Radiologi/Laborat/Pemeriksaan Lainnya) - permintaan user, pola
        // geser yang sama. (Section "Pemeriksaan Penunjang Tambahan RM 03-2"
        // - CT Scan/USG - dihapus, sudah pakai Pemeriksaan Penunjang bawaan.) =====
        panelRm032Diagnosis = new widget.PanelBiasa();
        panelRm032Diagnosis.setLayout(new java.awt.GridBagLayout());
        int rowDiagnosis = 0;

        rowDiagnosis = tambahBarisRm032Judul(panelRm032Diagnosis, rowDiagnosis, "XVI. DIAGNOSIS");
        CekDiagnosaKerja = new widget.CekBox(); CekDiagnosaKerja.setText("Diagnosa Kerja");
        TDiagnosaKerja = new widget.TextArea(); TDiagnosaKerja.setRows(2); TDiagnosaKerja.setColumns(40);
        widget.ScrollPane scrollDiagnosaKerja = new widget.ScrollPane(); scrollDiagnosaKerja.setViewportView(TDiagnosaKerja);
        widget.PanelBiasa panelDiagnosaKerja = new widget.PanelBiasa();
        panelDiagnosaKerja.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelDiagnosaKerja.add(CekDiagnosaKerja); panelDiagnosaKerja.add(scrollDiagnosaKerja);
        rowDiagnosis = tambahBarisRm032Field(panelRm032Diagnosis, rowDiagnosis, "Diagnosa Kerja", panelDiagnosaKerja);

        CekDiagnosaBanding = new widget.CekBox(); CekDiagnosaBanding.setText("Diagnosa Banding");
        TDiagnosaBanding = new widget.TextArea(); TDiagnosaBanding.setRows(2); TDiagnosaBanding.setColumns(40);
        widget.ScrollPane scrollDiagnosaBanding = new widget.ScrollPane(); scrollDiagnosaBanding.setViewportView(TDiagnosaBanding);
        widget.PanelBiasa panelDiagnosaBanding = new widget.PanelBiasa();
        panelDiagnosaBanding.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelDiagnosaBanding.add(CekDiagnosaBanding); panelDiagnosaBanding.add(scrollDiagnosaBanding);
        rowDiagnosis = tambahBarisRm032Field(panelRm032Diagnosis, rowDiagnosis, "Diagnosa Banding", panelDiagnosaBanding);

        // ===== Panel BAWAH: sisanya (Konsul dr. Spesialis .. Tanda Vital
        // Terakhir), tetap ditempel di bawah semua konten. =====
        panelRm032 = new widget.PanelBiasa();
        panelRm032.setLayout(new java.awt.GridBagLayout());
        row = 0;

        // -- N. Konsul dr. Spesialis --
        row = tambahBarisRm032Judul(panelRm032, row, "XVIII. KONSUL DR. SPESIALIS");
        KonsulKdDokter = new widget.TextBox(); KonsulKdDokter.setPreferredSize(new java.awt.Dimension(70,23)); KonsulKdDokter.setEditable(false);
        KonsulNmDokter = new widget.TextBox(); KonsulNmDokter.setPreferredSize(new java.awt.Dimension(220,23)); KonsulNmDokter.setEditable(false);
        BtnKonsulDokter = new widget.Button();
        BtnKonsulDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnKonsulDokter.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariDokter dlg = new DlgCariDokter(null,false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if(dlg.getTable().getSelectedRow()!= -1){
                        KonsulKdDokter.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),0).toString());
                        KonsulNmDokter.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),1).toString());
                    }
                    BtnKonsulDokter.requestFocus();
                }
            });
            dlg.isCek();
            dlg.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        widget.PanelBiasa panelKonsulDokter = new widget.PanelBiasa();
        panelKonsulDokter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKonsulDokter.add(KonsulKdDokter); panelKonsulDokter.add(KonsulNmDokter); panelKonsulDokter.add(BtnKonsulDokter);
        row = tambahBarisRm032Field(panelRm032, row, "Dokter Konsul", panelKonsulDokter);

        KonsulKdPoli = new widget.TextBox(); KonsulKdPoli.setPreferredSize(new java.awt.Dimension(70,23)); KonsulKdPoli.setEditable(false);
        KonsulNmPoli = new widget.TextBox(); KonsulNmPoli.setPreferredSize(new java.awt.Dimension(220,23)); KonsulNmPoli.setEditable(false);
        BtnKonsulUnit = new widget.Button();
        BtnKonsulUnit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnKonsulUnit.addActionListener((java.awt.event.ActionEvent evt) -> {
            simrskhanza.DlgCariPoli dlg = new simrskhanza.DlgCariPoli(null,false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if(dlg.getTable().getSelectedRow()!= -1){
                        KonsulKdPoli.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),0).toString());
                        KonsulNmPoli.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),1).toString());
                    }
                    BtnKonsulUnit.requestFocus();
                }
            });
            dlg.isCek();
            dlg.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        widget.PanelBiasa panelKonsulUnit = new widget.PanelBiasa();
        panelKonsulUnit.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelKonsulUnit.add(KonsulKdPoli); panelKonsulUnit.add(KonsulNmPoli); panelKonsulUnit.add(BtnKonsulUnit);
        row = tambahBarisRm032Field(panelRm032, row, "Unit/Poli Konsul", panelKonsulUnit);

        // -- O. Edukasi --
        row = tambahBarisRm032Judul(panelRm032, row, "XIX. EDUKASI");
        CekEdukasiDiberikan = new widget.CekBox(); CekEdukasiDiberikan.setText("Diberikan kepada Pasien/Keluarga");
        row = tambahBarisRm032Field(panelRm032, row, "Edukasi Diberikan", CekEdukasiDiberikan);
        CekEdukasiTidakBisa = new widget.CekBox(); CekEdukasiTidakBisa.setText("Tidak dapat memberikan edukasi karena");
        TEdukasiTidakBisaKet = new widget.TextBox(); TEdukasiTidakBisaKet.setPreferredSize(new java.awt.Dimension(250,23));
        widget.PanelBiasa panelEdukasiTidakBisa = new widget.PanelBiasa();
        panelEdukasiTidakBisa.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        panelEdukasiTidakBisa.add(CekEdukasiTidakBisa); panelEdukasiTidakBisa.add(TEdukasiTidakBisaKet);
        row = tambahBarisRm032Field(panelRm032, row, "Edukasi Tidak Diberikan", panelEdukasiTidakBisa);

        // -- P. Kondisi Saat Pulang --
        row = tambahBarisRm032Judul(panelRm032, row, "XX. KONDISI SAAT PULANG");
        bgKondisiPulang = new javax.swing.ButtonGroup();
        RbKondisiMembaik = new widget.RadioButton(); RbKondisiMembaik.setText("Membaik");
        RbKondisiMemburuk = new widget.RadioButton(); RbKondisiMemburuk.setText("Memburuk");
        RbKondisiTetap = new widget.RadioButton(); RbKondisiTetap.setText("Tetap");
        RbKondisiMeninggal = new widget.RadioButton(); RbKondisiMeninggal.setText("Meninggal");
        RbKondisiDoa = new widget.RadioButton(); RbKondisiDoa.setText("Death On Arrival");
        RbKondisiMembaik.addItemListener((java.awt.event.ItemEvent e) -> perbaruiVisibilitasJamMeninggal());
        RbKondisiMemburuk.addItemListener((java.awt.event.ItemEvent e) -> perbaruiVisibilitasJamMeninggal());
        RbKondisiTetap.addItemListener((java.awt.event.ItemEvent e) -> perbaruiVisibilitasJamMeninggal());
        RbKondisiMeninggal.addItemListener((java.awt.event.ItemEvent e) -> perbaruiVisibilitasJamMeninggal());
        RbKondisiDoa.addItemListener((java.awt.event.ItemEvent e) -> perbaruiVisibilitasJamMeninggal());
        row = tambahBarisRm032Field(panelRm032, row, "Kondisi Pulang", panelPilihan(bgKondisiPulang,
            RbKondisiMembaik,RbKondisiMemburuk,RbKondisiTetap,RbKondisiMeninggal,RbKondisiDoa));
        DTPKondisiPulangJam = new widget.Tanggal();
        DTPKondisiPulangJam.setPreferredSize(new java.awt.Dimension(180,23));
        DTPKondisiPulangJam.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        DTPKondisiPulangJam.setDate(new Date());
        row = tambahBarisRm032Field(panelRm032, row, "Jam Meninggal", DTPKondisiPulangJam);
        DTPKondisiPulangJam.setEnabled(false);

        // -- Q. Tanda Vital Terakhir --
        row = tambahBarisRm032Judul(panelRm032, row, "XXI. TANDA VITAL TERAKHIR");
        TTdAkhir = new widget.TextBox(); TTdAkhir.setPreferredSize(new java.awt.Dimension(80,23));
        row = tambahBarisRm032Field(panelRm032, row, "Tekanan Darah Akhir", TTdAkhir);
        TNadiAkhir = new widget.TextBox(); TNadiAkhir.setPreferredSize(new java.awt.Dimension(80,23));
        row = tambahBarisRm032Field(panelRm032, row, "Nadi Akhir", TNadiAkhir);
        TRrAkhir = new widget.TextBox(); TRrAkhir.setPreferredSize(new java.awt.Dimension(80,23));
        row = tambahBarisRm032Field(panelRm032, row, "RR Akhir", TRrAkhir);
        TSuhuAkhir = new widget.TextBox(); TSuhuAkhir.setPreferredSize(new java.awt.Dimension(80,23));
        row = tambahBarisRm032Field(panelRm032, row, "Suhu Akhir (SB)", TSuhuAkhir);

        // ===== Penempatan akhir. =====
        // panelRm032Atas ditempel SEBELUM "I. RIWAYAT KESEHATAN" (jLabel99,
        // y=70) - permintaan user supaya urutan input sama dengan formulir
        // fisik. Widget lama TIDAK diedit di initComponents()/GEN block;
        // yang dilakukan di sini cuma reposisi terprogram di luar itu (pola
        // sama seperti pemindahan SekunderKodePetugas dkk di
        // RMTriaseIGD.initHeaderTambahan()) - semua komponen existing yang
        // posisinya y>=70 (mulai dari "I. RIWAYAT KESEHATAN" dan semua yang
        // ada di bawahnya) digeser turun sejauh tinggi panelRm032Atas supaya
        // tidak tertindih.
        java.awt.Dimension ukuranAtas = panelRm032Atas.getPreferredSize();
        int thresholdY = 70;
        int geser = ukuranAtas.height + 20;
        for(java.awt.Component c : FormInput.getComponents()){
            if(c.getY() >= thresholdY){
                c.setLocation(c.getX(), c.getY() + geser);
            }
        }
        FormInput.add(panelRm032Atas);
        // x=5 (bukan 10) supaya kompensasi inset kiri 5px dari rm032Constraint()
        // - konten (judul/field) jadi rata mulai x=10, sejajar dgn judul lama
        // (mis. jLabel99 "VIII. RIWAYAT KESEHATAN") yang persis di x=10.
        panelRm032Atas.setBounds(5, thresholdY, 840, ukuranAtas.height);

        // panelRm032Tengah ditempel SEBELUM "IV. PEMERIKSAAN PENUNJANG"
        // (jLabel101, y asli 870, sudah tergeser jadi 870+geser oleh
        // pergeseran di atas) - permintaan user, pola geser yang sama:
        // semua komponen existing yang posisinya (setelah geser pertama)
        // >= 870+geser digeser turun lagi sejauh tinggi panelRm032Tengah.
        java.awt.Dimension ukuranTengah = panelRm032Tengah.getPreferredSize();
        int thresholdY2 = 870 + geser;
        int geser2 = ukuranTengah.height + 20;
        for(java.awt.Component c : FormInput.getComponents()){
            if(c.getY() >= thresholdY2){
                c.setLocation(c.getX(), c.getY() + geser2);
            }
        }
        FormInput.add(panelRm032Tengah);
        panelRm032Tengah.setBounds(5, thresholdY2, 840, ukuranTengah.height);

        // "Pemeriksaan Lainnya" - field baru gaya SAMA PERSIS dgn EKG/
        // Radiologi/Laborat existing (TextArea 20x5 + ScrollPane 260x63),
        // BUKAN checkbox+textbox spt CT Scan/USG - permintaan user supaya
        // seragam. Ditempel sebagai baris baru tepat di bawah baris EKG/
        // Radiologi/Laborat, pakai posisi AKTUAL scrollPane9 (EKG) sbg
        // acuan (sudah tergeser oleh geser+geser2 di atas), bukan angka
        // hardcode, supaya tetap benar berapapun tinggi geser1/geser2.
        widget.Label labelPemeriksaanLainnya = new widget.Label();
        labelPemeriksaanLainnya.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelPemeriksaanLainnya.setText("Pemeriksaan Lainnya :");
        int yLabelLainnya = scrollPane9.getY() + scrollPane9.getHeight() + 15;
        FormInput.add(labelPemeriksaanLainnya);
        labelPemeriksaanLainnya.setBounds(44, yLabelLainnya, 200, 23);

        PemeriksaanLainnya = new widget.TextArea();
        PemeriksaanLainnya.setColumns(20);
        PemeriksaanLainnya.setRows(5);
        PemeriksaanLainnya.setPreferredSize(new java.awt.Dimension(102,52));
        widget.ScrollPane scrollPemeriksaanLainnya = new widget.ScrollPane();
        scrollPemeriksaanLainnya.setViewportView(PemeriksaanLainnya);
        int yFieldLainnya = yLabelLainnya + 20;
        FormInput.add(scrollPemeriksaanLainnya);
        scrollPemeriksaanLainnya.setBounds(44, yFieldLainnya, 260, 63);
        int bottomLainnya = yFieldLainnya + 63;

        // panelRm032Diagnosis ditempel tepat di bawah "Pemeriksaan Lainnya".
        int yDiagnosis = bottomLainnya + 15;
        java.awt.Dimension ukuranDiagnosis = panelRm032Diagnosis.getPreferredSize();
        int bottomDiagnosis = yDiagnosis + ukuranDiagnosis.height;

        // Geser "VI. TATALAKSANA" (jLabel103) + isinya (scrollPane13) kalau
        // posisinya saat ini bertabrakan dengan field baru di atas (Pemeriksaan
        // Lainnya + panel Diagnosis).
        int geser3 = Math.max(0, (bottomDiagnosis + 15) - jLabel103.getY());
        if(geser3 > 0){
            int thresholdY3 = jLabel103.getY();
            for(java.awt.Component c : FormInput.getComponents()){
                if(c.getY() >= thresholdY3){
                    c.setLocation(c.getX(), c.getY() + geser3);
                }
            }
        }
        FormInput.add(panelRm032Diagnosis);
        panelRm032Diagnosis.setBounds(5, yDiagnosis, 840, ukuranDiagnosis.height);

        // panelRm032 (Konsul dr. Spesialis..Tanda Vital Terakhir) tetap di
        // bawah seluruh konten (lama + atas + tengah + geseran). Konten lama
        // semula berakhir di y=1383 (lihat FormInput.setPreferredSize asli di
        // initComponents()), setelah 3x geser jadi 1383+geser+geser2+geser3.
        java.awt.Dimension ukuranRm032 = panelRm032.getPreferredSize();
        int yBawah = 1383 + geser + geser2 + geser3 + 20;
        FormInput.add(panelRm032);
        panelRm032.setBounds(5, yBawah, 840, ukuranRm032.height);
        FormInput.setPreferredSize(new java.awt.Dimension(870, yBawah + ukuranRm032.height + 20));
    }

    /**
     * [Bagian C] Selipkan 5 judul section LEGACY (jLabel99/94/100/101/103) ke
     * posisi yg benar dalam seksiMedis - satu-satunya bagian yg TIDAK lewat
     * funnel tambahBarisRm032Judul() (lihat komentar struktural di kelas ini).
     * jLabel102 ("V", sudah disembunyikan pensiunkanFieldLama()) SENGAJA
     * dikecualikan. Index dihitung eksplisit (bukan cari string judul) supaya
     * gampang diperiksa: sesudah initRm032Tambahan(), seksiMedis berisi 16
     * entri urut [I..VII, XI..XIV, XVI, XVIII..XXI] - splice di titik itu
     * menghasilkan urutan akhir I..XXI.
     */
    private void daftarkanSeksiLegacyMedis(){
        seksiMedis.add(7, new SeksiMedis(jLabel99.getText(), jLabel99));   // VIII
        seksiMedis.add(8, new SeksiMedis(jLabel94.getText(), jLabel94));   // IX
        seksiMedis.add(9, new SeksiMedis(jLabel100.getText(), jLabel100)); // X
        // sesudah splice di atas, "XI..XIV" kini di index 10..13, "XVI" di index 14
        seksiMedis.add(14, new SeksiMedis(jLabel101.getText(), jLabel101)); // XV, sebelum XVI
        // "XVI" kini di index 15, "XVIII.." mulai index 16
        seksiMedis.add(16, new SeksiMedis(jLabel103.getText(), jLabel103)); // XVII, sebelum XVIII
    }

    // =========================================================================
    // [Sesi 1 - Restrukturisasi UI, Bagian A+B] Rangka 4 zona + bar pasien.
    // Pola & alasan desain IDENTIK dgn RMPenilaianAwalKeperawatanIGD.java
    // (susunRangkaKep()/buatBannerKep() dkk, sesi serupa modul perawat) -
    // disalin dgn akhiran "Medis" karena kelasnya beda, bukan krn logikanya
    // beda. Rail navigasi (WEST) sengaja KOSONG di sesi ini, diisi Sesi 2.
    // =========================================================================

    /** Tinggi seragam field & tombol di bar pasien. */
    private static final int TINGGI_FIELD_MEDIS = 32;

    private static void latarMedis(widget.PanelBiasa p, java.awt.Color warna){
        if(p==null || warna==null){ return; }
        p.setOpaque(true);
        p.setBackground(warna);
        p.setWarnaAtas(warna);
        p.setWarnaBawah(warna);
    }

    /** Escape minimal utk teks label yg dibungkus HTML. */
    private String escHtmlMedis(String s){
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    /** Color -> "#RRGGBB" utk dipakai di dalam teks HTML label. */
    private String hexMedis(java.awt.Color c){
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Terima BAIK bentuk mentah 'L'/'P' MAUPUN "Laki-Laki"/"Perempuan".
     * Kolom `pasien.jk` memang enum 'L','P', tapi beberapa jalur di proyek ini
     * sudah memanjangkannya lebih dulu - pernah membuat seluruh pasien
     * tercetak "P" pada laporan Jasper.
     */
    private String jkPanjangMedis(String jk){
        String s = jk==null ? "" : jk.trim();
        if(s.isEmpty()){ return ""; }
        char awal = Character.toUpperCase(s.charAt(0));
        if(awal=='L'){ return "Laki-laki"; }
        if(awal=='P'){ return "Perempuan"; }
        return s;
    }

    /** Umur dari "yyyy-MM-dd" (isi TglLahir). String kosong kalau tak terbaca. */
    private String umurDariTglLahirMedis(String tgl){
        String s = tgl==null ? "" : tgl.trim();
        if(s.length()<10 || s.startsWith("0000")){ return ""; }
        try{
            java.util.Calendar lahir = java.util.Calendar.getInstance();
            lahir.setTime(new java.text.SimpleDateFormat("yyyy-MM-dd").parse(s.substring(0,10)));
            java.util.Calendar kini = java.util.Calendar.getInstance();
            int th = kini.get(java.util.Calendar.YEAR) - lahir.get(java.util.Calendar.YEAR);
            if(kini.get(java.util.Calendar.DAY_OF_YEAR) < lahir.get(java.util.Calendar.DAY_OF_YEAR)){ th--; }
            return th<0 ? "" : th+" Th";
        }catch(java.text.ParseException e){
            return "";
        }
    }

    /**
     * Satu baris "keterangan + nomor" di kartu identitas.
     *
     * Keteranganlah yg diredupkan (TEXT_MUTED), NOMORnya tetap TEXT dan
     * monospace - dua nomor inilah yg paling tidak boleh salah dibaca,
     * TEXT_FAINT di bawah ambang WCAG AA utk kontras.
     */
    private String barisNomorMedis(String keterangan, String nomor){
        String n = nomor==null ? "" : nomor.trim();
        return "<html><font color='"+hexMedis(widget.ThemeConfig.TEXT_MUTED)+"'>"+escHtmlMedis(keterangan)
                +"</font>&nbsp;&nbsp;<font face='Consolas' color='"+hexMedis(widget.ThemeConfig.TEXT)+"'>"
                +(n.isEmpty() ? "&mdash;" : escHtmlMedis(n))+"</font></html>";
    }

    /** Isi label kartu identitas dari TextBox tersembunyi (display-only). */
    private void sinkronKartuMedis(){
        if(lblKartuNama==null){ return; }
        String nama = TPasien.getText()==null ? "" : TPasien.getText().trim();
        lblKartuNama.setText(nama.isEmpty() ? "—" : nama);
        lblKartuRM.setText(barisNomorMedis("No. RM", TNoRM.getText()));
        lblKartuRawat.setText(barisNomorMedis("No. Rawat", TNoRw.getText()));
        if(lblKartuInfo!=null){
            String jk = jkPanjangMedis(Jk.getText());
            String umur = umurDariTglLahirMedis(TglLahir.getText());
            String isi = jk;
            if(!umur.isEmpty()){ isi = isi.isEmpty() ? umur : isi+" · "+umur; }
            lblKartuInfo.setText(isi.isEmpty() ? " " : isi);
        }
    }

    /**
     * Tandai field yg memang read-only supaya tidak terlihat seperti isian yg
     * lupa diisi. Hanya menyentuh warna latar - `setEditable` tetap seperti
     * aslinya, tidak diubah di sini.
     */
    private void tandaiReadOnlyMedis(javax.swing.JComponent c){
        if(c==null){ return; }
        c.setOpaque(true);
        c.setBackground(widget.ThemeConfig.SURFACE_ALT);
    }

    /** Satu sel banner: label kecil di atas field (field dipaksa tinggi seragam). */
    private widget.PanelBiasa hdrCellMedis(String label, java.awt.Component field, boolean wajib){
        widget.PanelBiasa sel = new widget.PanelBiasa();
        sel.setOpaque(false);
        sel.setLayout(new java.awt.BorderLayout(0,3));
        widget.Label l = new widget.Label();
        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        l.setText(wajib ? "<html>"+label+" <font color='#B3261E'>*</font></html>" : label);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        l.setForeground(widget.ThemeConfig.TEXT_MUTED);
        sel.add(l, java.awt.BorderLayout.NORTH);
        field.setPreferredSize(new java.awt.Dimension(0,TINGGI_FIELD_MEDIS));
        sel.add(field, java.awt.BorderLayout.CENTER);
        return sel;
    }

    /** Pasangan [field lookup]. */
    private widget.PanelBiasa hdrLookupMedis(widget.TextBox tf, widget.Button btn){
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(false);
        p.setLayout(new java.awt.BorderLayout(4,0));
        p.add(tf, java.awt.BorderLayout.CENTER);
        p.add(btn, java.awt.BorderLayout.EAST);
        return p;
    }

    /**
     * Banner pasien: kartu identitas (kiri, lebar tetap) + grid field header
     * (kanan: Tanggal/Dokter/Anamnesis/Hubungan).
     *
     * TNoRw/TPasien/TNoRM/TglLahir/Jk/KdDokter disembunyikan (nilainya tetap
     * terisi lewat tampil()/getData(), cuma tak ditampilkan lagi - jadi
     * sumber data kartu identitas). NmDokter/BtnDokter/TglAsuhan/Anamnesis/
     * Hubungan DIPINDAH (reparent) ke grid header - Container.add() Swing
     * otomatis melepasnya dari FormInput, objeknya tetap sama sehingga
     * getData()/tampil()/emptTeks()/simpan() yg memanggil method pada
     * variabel ini tidak perlu diubah sama sekali. Label statis lama
     * (No.Rawat/Dokter/Tgl.Lahir/J.K./Tanggal/Anamnesis) ikut disembunyikan,
     * digantikan label baru di kartu/grid. "Keterangan Anamnesis" pada
     * mockup awal user TIDAK ada sbg field - yg benar-benar ada di posisi
     * itu adalah `Hubungan` (hubungan pelapor dgn pasien, dipakai utk
     * Alloanamnesis), jadi itulah yg dipindah & diberi label "HUBUNGAN".
     */
    private widget.PanelBiasa buatBannerMedis(){
        TNoRw.setVisible(false);
        TPasien.setVisible(false);
        TNoRM.setVisible(false);
        TglLahir.setVisible(false);
        Jk.setVisible(false);
        KdDokter.setVisible(false);
        jLabel10.setVisible(false); // "No.Rawat :"
        jLabel8.setVisible(false);  // "Tgl.Lahir :"
        jLabel11.setVisible(false); // "J.K. :"
        label14.setVisible(false);  // "Dokter :"
        label11.setVisible(false);  // "Tanggal :"
        jLabel38.setVisible(false); // "Anamnesis :"

        widget.PanelBiasa banner = new widget.PanelBiasa();
        banner.setOpaque(false);
        banner.setLayout(new java.awt.GridBagLayout());

        // ---- kartu identitas (kiri) ----
        widget.PanelBiasa kartu = new widget.PanelBiasa();
        latarMedis(kartu, widget.ThemeConfig.SURFACE_ALT);
        kartu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
            javax.swing.BorderFactory.createEmptyBorder(10,12,10,12)));
        kartu.setLayout(new java.awt.GridBagLayout());
        widget.Label judulKartu = new widget.Label();
        judulKartu.setText("IDENTITAS PASIEN");
        judulKartu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        judulKartu.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        judulKartu.setForeground(widget.ThemeConfig.TEXT_MUTED);
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
        java.awt.GridBagConstraints k = new java.awt.GridBagConstraints();
        k.gridx=0; k.gridy=0; k.anchor=java.awt.GridBagConstraints.WEST;
        k.fill=java.awt.GridBagConstraints.HORIZONTAL; k.weightx=1.0;
        kartu.add(judulKartu,k);
        k.gridy=1; k.insets=new java.awt.Insets(8,0,0,0); kartu.add(lblKartuNama,k);
        k.gridy=2; k.insets=new java.awt.Insets(1,0,0,0); kartu.add(lblKartuInfo,k);
        k.gridy=3; k.insets=new java.awt.Insets(8,0,0,0); kartu.add(lblKartuRM,k);
        k.gridy=4; k.insets=new java.awt.Insets(2,0,0,0); kartu.add(lblKartuRawat,k);
        k.gridy=5; k.weighty=1.0; k.fill=java.awt.GridBagConstraints.BOTH; k.insets=new java.awt.Insets(0,0,0,0);
        kartu.add(javax.swing.Box.createGlue(),k);

        javax.swing.event.DocumentListener sinkron = new javax.swing.event.DocumentListener(){
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e){ sinkronKartuMedis(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e){ sinkronKartuMedis(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e){ sinkronKartuMedis(); }
        };
        TNoRw.getDocument().addDocumentListener(sinkron);
        TNoRM.getDocument().addDocumentListener(sinkron);
        TPasien.getDocument().addDocumentListener(sinkron);
        Jk.getDocument().addDocumentListener(sinkron);
        TglLahir.getDocument().addDocumentListener(sinkron);
        sinkronKartuMedis();

        // ---- grid field header (kanan): Tanggal / Dokter / Anamnesis / Hubungan ----
        widget.PanelBiasa grid = new widget.PanelBiasa();
        grid.setOpaque(false);
        grid.setLayout(new java.awt.GridBagLayout());

        NmDokter.setPreferredSize(new java.awt.Dimension(0,TINGGI_FIELD_MEDIS));
        BtnDokter.setPreferredSize(new java.awt.Dimension(TINGGI_FIELD_MEDIS,TINGGI_FIELD_MEDIS));
        tandaiReadOnlyMedis(NmDokter);

        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.gridy=0; g.fill=java.awt.GridBagConstraints.HORIZONTAL; g.anchor=java.awt.GridBagConstraints.NORTH;
        g.weightx=1.0;
        g.gridx=0; g.insets=new java.awt.Insets(0,0,0,12); grid.add(hdrCellMedis("TANGGAL", TglAsuhan, true),g);
        g.gridx=1; grid.add(hdrCellMedis("DOKTER", hdrLookupMedis(NmDokter,BtnDokter), true),g);
        g.gridx=2; grid.add(hdrCellMedis("ANAMNESIS", Anamnesis, false),g);
        g.gridx=3; g.insets=new java.awt.Insets(0,0,0,0); grid.add(hdrCellMedis("HUBUNGAN", Hubungan, false),g);

        // ---- rakit banner: kartu (kiri, lebar tetap) + grid (kanan, fleksibel) ----
        java.awt.Dimension dk = new java.awt.Dimension(300,10);
        kartu.setMinimumSize(dk); kartu.setPreferredSize(dk);
        java.awt.GridBagConstraints f = new java.awt.GridBagConstraints();
        f.gridx=0; f.gridy=0; f.fill=java.awt.GridBagConstraints.BOTH; f.weighty=1.0;
        f.insets=new java.awt.Insets(0,0,0,16);
        banner.add(kartu,f);
        f.gridx=1; f.weightx=1.0; f.insets=new java.awt.Insets(0,0,0,0);
        banner.add(grid,f);
        return banner;
    }

    // =========================================================================
    // [Sesi 2 - Restrukturisasi UI, Bagian C] Isi rail navigasi bagian.
    // Pola & alasan desain IDENTIK dgn RMPenilaianAwalKeperawatanIGD.java
    // (isiRailKep()/buatBarisRailKep() dkk, sesi identik modul perawat),
    // dgn SATU perbedaan sengaja: judul dipakai APA ADANYA (termasuk angka
    // Romawi bawaan I-XXI), TIDAK di-strip & dinomori ulang Arab seperti
    // Keperawatan - Medis sudah punya penomoran baku yg dipakai konsisten
    // di seluruh form (kemungkinan juga di cetakan), jadi dipertahankan
    // supaya rail dan isi form memakai istilah yg sama persis.
    // =========================================================================

    /** Ruang isi yg diikutkan saat menggulung ke sebuah bagian. */
    private static final int TINGGI_KONTEKS_MEDIS = 220;

    /**
     * [Bagian C] Satu baris rail = panel yg bisa diklik (bukan widget.Button).
     *
     * Dipilih panel + MouseListener supaya keadaan aktif/hover dan batang
     * aksen kiri sepenuhnya dikendalikan di sini; widget.Button melukis
     * latarnya sendiri lewat glass color dan sulit diberi batang aksen.
     */
    private widget.PanelBiasa buatBarisRailMedis(final int idx){
        final SeksiMedis s = seksiMedis.get(idx);
        widget.PanelBiasa baris = new widget.PanelBiasa();
        latarMedis(baris, widget.ThemeConfig.SURFACE);
        baris.setLayout(new java.awt.BorderLayout(0,0));
        baris.setBorder(javax.swing.BorderFactory.createEmptyBorder(1,0,1,0));
        baris.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        // Batang aksen kiri 3px - penanda bagian aktif tanpa mengandalkan
        // warna latar saja (pembeda tetap terbaca di layar berkontras rendah).
        widget.PanelBiasa aksen = new widget.PanelBiasa();
        latarMedis(aksen, widget.ThemeConfig.SURFACE);
        aksen.setPreferredSize(new java.awt.Dimension(3,10));

        widget.Label lbl = new widget.Label();
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        lbl.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        lbl.setForeground(widget.ThemeConfig.TEXT_NORMAL);
        lbl.setBorder(javax.swing.BorderFactory.createEmptyBorder(7,9,7,9));
        // Judul dibungkus <div width> supaya MEMBUNGKUS, bukan terpotong.
        lbl.setText("<html><div style='width:"+LEBAR_TEKS_RAIL_MEDIS+"px'>"
                + escHtmlMedis(s.judul) + "</div></html>");

        baris.add(aksen, java.awt.BorderLayout.WEST);
        baris.add(lbl, java.awt.BorderLayout.CENTER);
        s.baris = baris; s.lblJudul = lbl; s.aksen = aksen;

        baris.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseClicked(java.awt.event.MouseEvent e){
                kePosisiSeksiMedis(idx);
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e){
                if(idx!=seksiAktifMedis){ latarMedis(s.baris, widget.ThemeConfig.SURFACE_ALT); }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e){
                if(idx!=seksiAktifMedis){ latarMedis(s.baris, widget.ThemeConfig.SURFACE); }
            }
        });
        return baris;
    }

    /** Lebar teks judul di rail (300px rail - aksen - padding - scrollbar). */
    private static final int LEBAR_TEKS_RAIL_MEDIS = 255;

    /** [Bagian C] Pindahkan sorotan rail ke bagian ke-idx (tidak menggulung). */
    private void sorotSeksiMedis(int idx){
        if(idx<0 || idx>=seksiMedis.size()){ return; }
        seksiAktifMedis = idx;
        for(int i=0;i<seksiMedis.size();i++){
            SeksiMedis s = seksiMedis.get(i);
            if(s.baris==null){ continue; }
            boolean aktif = (i==idx);
            latarMedis(s.baris, aktif ? widget.ThemeConfig.SURFACE_ALT : widget.ThemeConfig.SURFACE);
            latarMedis(s.aksen, aktif ? widget.ThemeConfig.PRIMARY : widget.ThemeConfig.SURFACE);
            s.lblJudul.setForeground(aktif ? widget.ThemeConfig.TEXT : widget.ThemeConfig.TEXT_NORMAL);
            s.lblJudul.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY,
                    aktif ? java.awt.Font.BOLD : java.awt.Font.PLAIN, 11));
        }
        if(lblProgresJudulMedis!=null){
            SeksiMedis s = seksiMedis.get(idx);
            lblProgresJudulMedis.setText("<html><div style='width:"+LEBAR_TEKS_RAIL_MEDIS+"px'>"
                    + escHtmlMedis(s.judul) + "</div></html>");
            lblProgresHitungMedis.setText("Bagian " + (idx+1) + " dari " + seksiMedis.size());
        }
        railMedis.revalidate();
        railMedis.repaint();
    }

    /**
     * [Bagian C] Gulung FormInput sampai header bagian ke-idx terlihat.
     *
     * Ditambah TINGGI_KONTEKS_MEDIS di bawah strip supaya yg tampil bukan
     * cuma judulnya, melainkan judul + beberapa baris pertama isinya.
     * Dihitung LIVE saat klik (bukan di-cache saat construction) karena
     * posisi Y section legacy bergantung geser/geser2/geser3 runtime.
     */
    private void kePosisiSeksiMedis(int idx){
        if(idx<0 || idx>=seksiMedis.size()){ return; }
        SeksiMedis s = seksiMedis.get(idx);
        java.awt.Container induk = s.strip.getParent();
        if(induk==null){ sorotSeksiMedis(idx); return; }
        java.awt.Rectangle r = javax.swing.SwingUtilities.convertRectangle(
                induk, s.strip.getBounds(), FormInput);
        r.height = Math.min(r.height + TINGGI_KONTEKS_MEDIS, Math.max(1, FormInput.getHeight() - r.y));
        FormInput.scrollRectToVisible(r);
        sorotSeksiMedis(idx);
    }

    /**
     * [Bagian C] Kartu progres di kepala rail.
     *
     * Sengaja HANYA menampilkan posisi bagian ("Bagian N dari 21"), bukan
     * hitungan isian wajib/opsional/persentase - itu Bagian E (sesi lain);
     * menaruh angka yg belum dihitung apa pun lebih menyesatkan daripada
     * tidak ada. Keputusan sama persis dgn Keperawatan Sesi 2.
     */
    private widget.PanelBiasa buatKartuProgresMedis(){
        widget.PanelBiasa kartu = new widget.PanelBiasa();
        latarMedis(kartu, widget.ThemeConfig.SURFACE_ALT);
        kartu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
            javax.swing.BorderFactory.createEmptyBorder(10,12,10,12)));
        kartu.setLayout(new java.awt.GridBagLayout());

        widget.Label judul = new widget.Label();
        judul.setText("BAGIAN AKTIF");
        judul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        judul.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
        judul.setForeground(widget.ThemeConfig.TEXT_MUTED);

        lblProgresJudulMedis = new widget.Label();
        lblProgresJudulMedis.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblProgresJudulMedis.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 12));
        lblProgresJudulMedis.setForeground(widget.ThemeConfig.TEXT);
        lblProgresJudulMedis.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        lblProgresHitungMedis = new widget.Label();
        lblProgresHitungMedis.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblProgresHitungMedis.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        lblProgresHitungMedis.setForeground(widget.ThemeConfig.TEXT_MUTED);

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx=0; c.gridy=0; c.weightx=1.0; c.anchor=java.awt.GridBagConstraints.WEST;
        c.fill=java.awt.GridBagConstraints.HORIZONTAL;
        kartu.add(judul,c);
        c.gridy=1; c.insets=new java.awt.Insets(6,0,0,0); kartu.add(lblProgresJudulMedis,c);
        c.gridy=2; c.insets=new java.awt.Insets(4,0,0,0); kartu.add(lblProgresHitungMedis,c);
        return kartu;
    }

    /** [Bagian C] Isi rail: kartu progres di atas + satu baris tiap bagian. */
    private void isiRailMedis(){
        railMedis.removeAll();
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx=0; c.weightx=1.0; c.fill=java.awt.GridBagConstraints.HORIZONTAL;
        c.anchor=java.awt.GridBagConstraints.NORTH;

        c.gridy=0; c.insets=new java.awt.Insets(12,12,10,12);
        railMedis.add(buatKartuProgresMedis(), c);

        c.insets=new java.awt.Insets(0,8,0,8);
        int gy = 1;
        for(int i=0;i<seksiMedis.size();i++){
            c.gridy=gy++;
            railMedis.add(buatBarisRailMedis(i), c);
        }
        // Pengisi vertikal - tanpa ini GridBagLayout menengahkan seluruh
        // daftar saat rail lebih tinggi dari isinya.
        c.gridy=gy; c.weighty=1.0; c.fill=java.awt.GridBagConstraints.BOTH;
        c.insets=new java.awt.Insets(0,0,0,0);
        railMedis.add(javax.swing.Box.createGlue(), c);

        if(!seksiMedis.isEmpty()){ sorotSeksiMedis(0); }
    }

    /**
     * [Bagian A] Rangka layar 4 zona. Dipanggil di akhir konstruktor,
     * sesudah initRm032Tambahan() (FormInput sudah tersusun penuh).
     *
     *   internalFrame2 (BorderLayout)
     *     NORTH  barPasienMedis  <- hasil buatBannerMedis()
     *     WEST   scrollRailMedis <- diisi isiRailMedis() (Sesi 2, Bagian C)
     *     CENTER scrollInput     <- SUDAH ADA dari GEN, sudah membungkus
     *                                FormInput (L1311/1313) - dipakai lagi,
     *                                tidak dibuat ulang.
     *   panelGlass8 (action bar) sudah PAGE_END milik internalFrame1, di
     *   luar scope Sesi 1 (Bagian F) - tidak disentuh.
     */
    private void susunRangkaMedis(){
        barPasienMedis = buatBannerMedis();
        latarMedis(barPasienMedis, widget.ThemeConfig.SURFACE);
        barPasienMedis.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0,0,1,0, widget.ThemeConfig.BORDER_SOFT),
            javax.swing.BorderFactory.createEmptyBorder(12,14,12,14)));

        railMedis = new widget.PanelBiasa();
        latarMedis(railMedis, widget.ThemeConfig.SURFACE);
        railMedis.setLayout(new java.awt.GridBagLayout());
        scrollRailMedis = new widget.ScrollPane();
        scrollRailMedis.setViewportView(railMedis);
        scrollRailMedis.setBorder(javax.swing.BorderFactory.createMatteBorder(0,0,0,1, widget.ThemeConfig.BORDER_SOFT));
        scrollRailMedis.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollRailMedis.setPreferredSize(new java.awt.Dimension(300,10));
        scrollRailMedis.getVerticalScrollBar().setUnitIncrement(16);
        scrollRailMedis.getViewport().setBackground(widget.ThemeConfig.SURFACE);

        internalFrame2.removeAll();
        internalFrame2.setLayout(new java.awt.BorderLayout());
        internalFrame2.add(barPasienMedis, java.awt.BorderLayout.NORTH);
        internalFrame2.add(scrollRailMedis, java.awt.BorderLayout.WEST);
        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);
        // Isi rail SESUDAH scrollInput ada - kePosisiSeksiMedis() memakai
        // FormInput.scrollRectToVisible() yg butuh FormInput sudah ternaung
        // JViewport/JScrollPane.
        isiRailMedis();
        internalFrame2.revalidate();
        internalFrame2.repaint();
    }

    /**
     * [RM 03-2] Toggle panel APGAR/Segitiga PAT - tampil hanya kalau
     * KategoriPasien = "Bayi Baru Lahir", pola sama seperti
     * perbaruiVisibilitasMekanismeTrauma() di RMTriaseIGD.java.
     */
    private void perbaruiVisibilitasBayiBaruLahir(){
        boolean bayi = "Bayi Baru Lahir".equals(KategoriPasien.getSelectedItem());
        panelBayiBaruLahir.setVisible(bayi);
        if(!bayi){
            TApgarCukupBulan.setText("");
            TApgarCairanAmnionJernih.setText("");
            TApgarPernapasanMenangis.setText("");
            TApgarTonusOtot.setText("");
            TPatPenampilan.setText("");
            TPatUsahaNapas.setText("");
            TPatPerfusiKulit.setText("");
        }
    }

    /**
     * [RM 03-2] Field "Jam Meninggal" hanya aktif kalau Kondisi Pulang =
     * Meninggal, dikosongkan otomatis kalau bukan.
     */
    private void perbaruiVisibilitasJamMeninggal(){
        boolean meninggal = RbKondisiMeninggal.isSelected();
        DTPKondisiPulangJam.setEnabled(meninggal);
        if(!meninggal){
            DTPKondisiPulangJam.setDate(new Date());
        }
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            // [RM 03-2] SELECT diringkas - cuma kolom pencarian (tabMode
            // sekarang lean, lihat konstruktor). Field lengkap dimuat
            // belakangan oleh getData() (query-ulang-DB) saat baris diklik.
            if(TCari.getText().trim().equals("")){
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,penilaian_medis_igd.tanggal,"+
                        "penilaian_medis_igd.kd_dokter,penilaian_medis_igd.diagnosa_kerja,dokter.nm_dokter "+
                        "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join penilaian_medis_igd on reg_periksa.no_rawat=penilaian_medis_igd.no_rawat "+
                        "inner join dokter on penilaian_medis_igd.kd_dokter=dokter.kd_dokter where "+
                        "penilaian_medis_igd.tanggal between ? and ? order by penilaian_medis_igd.tanggal");
            }else{
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,penilaian_medis_igd.tanggal,"+
                        "penilaian_medis_igd.kd_dokter,penilaian_medis_igd.diagnosa_kerja,dokter.nm_dokter "+
                        "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join penilaian_medis_igd on reg_periksa.no_rawat=penilaian_medis_igd.no_rawat "+
                        "inner join dokter on penilaian_medis_igd.kd_dokter=dokter.kd_dokter where "+
                        "penilaian_medis_igd.tanggal between ? and ? and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "+
                        "penilaian_medis_igd.kd_dokter like ? or dokter.nm_dokter like ?) order by penilaian_medis_igd.tanggal");
            }

            try {
                if(TCari.getText().trim().equals("")){
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
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getDate("tgl_lahir"),rs.getString("jk"),
                        rs.getString("kd_dokter"),rs.getString("nm_dokter"),rs.getString("tanggal"),rs.getString("diagnosa_kerja")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif Tampil : "+e);
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
        Anamnesis.setSelectedIndex(0);
        Hubungan.setText("");
        KeluhanUtama.setText("");
        RPS.setText("");
        RPK.setText("");
        RPD.setText("");
        RPO.setText("");
        Alergi.setText("");
        Keadaan.setSelectedIndex(0);
        // (GCS/Kepala/Mata/Gigi/Leher/Thoraks/Abdomen/Genital/Ekstremitas/
        // Diagnosis lama TIDAK direset - dipensiunkan dari UI aktif, lihat
        // pensiunkanFieldLama())
        Kesadaran.setSelectedIndex(0);
        TD.setText("");
        Nadi.setText("");
        RR.setText("");
        Suhu.setText("");
        BB.setText("");
        TB.setText("");
        KetFisik.setText("");
        KetLokalis.setText("");
        EKG.setText("");
        Radiologi.setText("");
        Laborat.setText("");
        Tatalaksana.setText("");
        TglAsuhan.setDate(new Date());
        TabRawat.setSelectedIndex(0);

        // -- A. Intervensi Pre Hospital --
        CekPreCervicalCollar.setSelected(false); CekPreDefibrilasi.setSelected(false);
        CekPreVtp.setSelected(false); CekPreBalutBidai.setSelected(false); CekPreNgt.setSelected(false);
        CekPreRjp.setSelected(false); CekPreIntubasi.setSelected(false); CekPreDekompresiWsd.setSelected(false);
        CekPreKateterUrin.setSelected(false); CekPreInfus.setSelected(false); CekPreTidakAda.setSelected(false);
        TPreObatKet.setText("");

        // -- B. Survei Primer - Jalan Napas --
        CekJnPaten.setSelected(false); CekJnObstruksiPartial.setSelected(false); CekJnStridor.setSelected(false);
        CekJnSnoring.setSelected(false); CekJnGurgling.setSelected(false); CekJnWheezing.setSelected(false);
        CekJnObstruksiTotal.setSelected(false); CekJnTraumaJalanNapas.setSelected(false);
        CekJnFasialLeherInhalasi.setSelected(false); CekJnRisikoAspirasi.setSelected(false); CekJnBendaAsing.setSelected(false);
        bgJnKesimpulan.clearSelection();

        // -- C. Survei Primer - Pernapasan --
        bgNapPola.clearSelection(); bgNapReguler.clearSelection(); bgNapGerakanDada.clearSelection();
        CekNapJejasKanan.setSelected(false); CekNapJejasKiri.setSelected(false);
        CekNapTipeNormal.setSelected(false); CekNapTipeTakipneu.setSelected(false); CekNapTipeKussmaul.setSelected(false);
        CekNapTipeHiperventilasi.setSelected(false); CekNapTipeBiot.setSelected(false); CekNapTipeCheyneStoke.setSelected(false);
        CekNapTipeApneustic.setSelected(false); CekNapTipeRetraktif.setSelected(false); CekNapTipeFlare.setSelected(false);
        bgNapKesimpulan.clearSelection();

        // -- D. Survei Primer - Sirkulasi --
        bgSirNadi.clearSelection(); bgSirNadiKekuatan.clearSelection(); bgSirKulitWarna.clearSelection();
        bgSirKulitKondisi.clearSelection(); CekSirBerkeringat.setSelected(false); bgSirAkral.clearSelection();
        bgSirAkralKondisi.clearSelection(); bgSirCrt.clearSelection(); bgSirKesimpulan.clearSelection();

        // -- E. Kategori Pasien + Bayi Baru Lahir (APGAR/PAT) --
        KategoriPasien.setSelectedIndex(0);
        TApgarCukupBulan.setText(""); TApgarCairanAmnionJernih.setText("");
        TApgarPernapasanMenangis.setText(""); TApgarTonusOtot.setText("");
        TPatPenampilan.setText(""); TPatUsahaNapas.setText(""); TPatPerfusiKulit.setText("");
        perbaruiVisibilitasBayiBaruLahir();

        // -- F. Disabilitas --
        TGcsE.setText(""); TGcsV.setText(""); TGcsM.setText("");
        bgPupil.clearSelection();
        TDiameterPupilKiri.setText(""); TDiameterPupilKanan.setText("");
        bgReflekKiri.clearSelection(); bgReflekKanan.clearSelection();
        TMeningealSigns.setText("");
        bgLateralisasi.clearSelection();

        // -- G. Eksposur --
        CekEksDeformitas.setSelected(false); CekEksEkskoriasi.setSelected(false); CekEksContusio.setSelected(false);
        CekEksAbrasi.setSelected(false); CekEksPenetrasi.setSelected(false); CekEksBurn.setSelected(false);
        CekEksTenderness.setSelected(false); CekEksLaserasi.setSelected(false); CekEksSwelling.setSelected(false);
        CekEksTidakTampakJelas.setSelected(false);

        // -- H. Kepala Leher --
        bgKonjungtiva.clearSelection(); bgSklera.clearSelection(); bgBibirLidah.clearSelection(); bgMukosaMulut.clearSelection();

        // -- I. Leher rinci --
        bgDeviasiTrakea.clearSelection(); bgJvp.clearSelection(); bgLnn.clearSelection(); bgTiroid.clearSelection();

        // -- J. Thorax --
        TJantung.setText(""); TParu.setText("");

        // -- K. Abdomen & Pelvis --
        TPunggungPinggang.setText(""); TEkstremitasPelvis.setText("");

        // -- L. Pemeriksaan Lainnya --
        PemeriksaanLainnya.setText("");

        // -- M. Diagnosis (Kerja/Banding) --
        CekDiagnosaKerja.setSelected(false); TDiagnosaKerja.setText("");
        CekDiagnosaBanding.setSelected(false); TDiagnosaBanding.setText("");

        // -- N. Konsul dr. Spesialis --
        KonsulKdDokter.setText(""); KonsulNmDokter.setText(""); KonsulKdPoli.setText(""); KonsulNmPoli.setText("");

        // -- O. Edukasi --
        CekEdukasiDiberikan.setSelected(false);
        CekEdukasiTidakBisa.setSelected(false); TEdukasiTidakBisaKet.setText("");

        // -- P. Kondisi Saat Pulang --
        bgKondisiPulang.clearSelection();
        DTPKondisiPulangJam.setDate(new Date());
        perbaruiVisibilitasJamMeninggal();

        // -- Q. Tanda Vital Terakhir --
        TTdAkhir.setText(""); TNadiAkhir.setText(""); TRrAkhir.setText(""); TSuhuAkhir.setText("");

        Anamnesis.requestFocus();
    }

    /**
     * [RM 03-2] Pola query-ulang-DB (keputusan user, BUKAN pola array-index
     * JTable raksasa yang jadi akar beberapa bug di RMTriaseIGD) - saat baris
     * diklik, SELECT * langsung ke penilaian_medis_igd by no_rawat, isi SEMUA
     * field (lama yang masih aktif + baru RM 03-2) dari ResultSet by nama
     * kolom. tbObat (daftar) cuma perlu no_rawat di kolom 0 - lihat tampil().
     */
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

    /** Teks item radio button yg terpilih dlm satu grup, "-" kalau tidak ada yg terpilih. */
    private String pilihan(javax.swing.AbstractButton... rb){
        for(javax.swing.AbstractButton r : rb){
            if(r.isSelected()){ return r.getText(); }
        }
        return "-";
    }

    /**
     * Ringkasan HTML lengkap (SEMUA field RM 03-2) - ditampilkan di panel
     * ScrollHTML/LoadHTML di sebelah tabel Data Pengkajian saat baris
     * diklik. Dibaca LANGSUNG dari widget yg sudah terisi oleh getData()
     * (bukan query ulang terpisah) - dijamin 100% konsisten dgn form edit.
     * Meniru pola bangunRingkasanHtml() di RMPenilaianAwalKeperawatanIGD.
     */
    private void bangunRingkasanHtml(){
        StringBuilder h = new StringBuilder();

        h.append(judulBaris("PASIEN & PETUGAS"));
        h.append(baris("No.Rawat",TNoRw.getText()));
        h.append(baris("No.RM",TNoRM.getText()));
        h.append(baris("Nama Pasien",TPasien.getText()));
        h.append(baris("Tgl Lahir",TglLahir.getText()));
        h.append(baris("J.K.",Jk.getText()));
        h.append(baris("Dokter",KdDokter.getText()+" "+NmDokter.getText()));
        h.append(baris("Tgl Asuhan",TglAsuhan.getSelectedItem()==null?"-":TglAsuhan.getSelectedItem().toString()));

        h.append(judulBaris("I. RIWAYAT KESEHATAN"));
        h.append(baris("Anamnesis",Anamnesis.getSelectedItem()==null?"-":Anamnesis.getSelectedItem().toString()));
        h.append(baris("Hubungan",Hubungan.getText()));
        h.append(baris("Keluhan Utama",KeluhanUtama.getText()));
        h.append(baris("RPS",RPS.getText()));
        h.append(baris("RPD",RPD.getText()));
        h.append(baris("RPK",RPK.getText()));
        h.append(baris("RPO",RPO.getText()));
        h.append(baris("Alergi",Alergi.getText()));

        h.append(judulBaris("PEMERIKSAAN FISIK"));
        h.append(baris("Keadaan",Keadaan.getSelectedItem()==null?"-":Keadaan.getSelectedItem().toString()));
        h.append(baris("Kesadaran",Kesadaran.getSelectedItem()==null?"-":Kesadaran.getSelectedItem().toString()));
        h.append(baris("Tanda Vital","TD "+TD.getText()+", Nadi "+Nadi.getText()+", RR "+RR.getText()+", Suhu "+Suhu.getText()+", SpO2 "+SPO.getText()+", BB "+BB.getText()+", TB "+TB.getText()));
        h.append(baris("Ket. Fisik",KetFisik.getText()));
        h.append(baris("Ket. Lokalis",KetLokalis.getText()));

        h.append(judulBaris("PEMERIKSAAN PENUNJANG"));
        h.append(baris("EKG",EKG.getText()));
        h.append(baris("Radiologi",Radiologi.getText()));
        h.append(baris("Laborat",Laborat.getText()));

        h.append(judulBaris("A. INTERVENSI PRE HOSPITAL"));
        h.append(baris("Tindakan",cekList(CekPreCervicalCollar,CekPreDefibrilasi,CekPreVtp,CekPreBalutBidai,CekPreNgt,CekPreRjp,CekPreIntubasi,CekPreDekompresiWsd,CekPreKateterUrin,CekPreInfus,CekPreTidakAda)));
        h.append(baris("Ket. Obat",TPreObatKet.getText()));

        h.append(judulBaris("B. SURVEI PRIMER - JALAN NAPAS"));
        h.append(baris("Kondisi",cekList(CekJnPaten,CekJnObstruksiPartial,CekJnStridor,CekJnSnoring,CekJnGurgling,CekJnWheezing,CekJnObstruksiTotal,CekJnTraumaJalanNapas,CekJnFasialLeherInhalasi,CekJnRisikoAspirasi,CekJnBendaAsing)));
        h.append(baris("Kesimpulan",pilihan(RbJnAman,RbJnMengancamJiwa)));

        h.append(judulBaris("C. SURVEI PRIMER - PERNAPASAN"));
        h.append(baris("Pola / Reguler / Gerakan Dada",pilihan(RbNapSpontan,RbNapTidakSpontan)+" / "+pilihan(RbNapReguler,RbNapIrreguler)+" / "+pilihan(RbNapSimetris,RbNapAsimetris)));
        h.append(baris("Jejas",cekList(CekNapJejasKanan,CekNapJejasKiri)));
        h.append(baris("Tipe Napas",cekList(CekNapTipeNormal,CekNapTipeTakipneu,CekNapTipeKussmaul,CekNapTipeHiperventilasi,CekNapTipeBiot,CekNapTipeCheyneStoke,CekNapTipeApneustic,CekNapTipeRetraktif,CekNapTipeFlare)));
        h.append(baris("Kesimpulan",pilihan(RbNapAman,RbNapMengancamJiwa)));

        h.append(judulBaris("D. SURVEI PRIMER - SIRKULASI"));
        h.append(baris("Nadi",pilihan(RbSirReguler,RbSirIrreguler)+", "+pilihan(RbSirKuat,RbSirLemah)));
        h.append(baris("Kulit",pilihan(RbSirKulitNormal,RbSirKulitPucat)+", "+pilihan(RbSirJaundice,RbSirCyanosis)+(CekSirBerkeringat.isSelected()?", Berkeringat":"")));
        h.append(baris("Akral",pilihan(RbSirHangat,RbSirDingin)+", "+pilihan(RbSirKering,RbSirBasah)));
        h.append(baris("CRT",pilihan(RbSirCrtKurang2,RbSirCrtLebih2)));
        h.append(baris("Kesimpulan",pilihan(RbSirAman,RbSirMengancamJiwa)));

        h.append(judulBaris("E. KATEGORI PASIEN"));
        String kategoriPasienStr = KategoriPasien.getSelectedItem()==null?"":KategoriPasien.getSelectedItem().toString();
        h.append(baris("Kategori Pasien",kategoriPasienStr));
        if("Bayi Baru Lahir".equals(kategoriPasienStr)){
            h.append(baris("APGAR","Cukup Bulan: "+TApgarCukupBulan.getText()+", Cairan Amnion Jernih: "+TApgarCairanAmnionJernih.getText()+", Pernapasan/Menangis: "+TApgarPernapasanMenangis.getText()+", Tonus Otot: "+TApgarTonusOtot.getText()));
            h.append(baris("PAT","Penampilan: "+TPatPenampilan.getText()+", Usaha Napas: "+TPatUsahaNapas.getText()+", Perfusi Kulit: "+TPatPerfusiKulit.getText()));
        }

        h.append(judulBaris("F. DISABILITAS"));
        h.append(baris("GCS","E"+TGcsE.getText()+" V"+TGcsV.getText()+" M"+TGcsM.getText()));
        h.append(baris("Pupil",pilihan(RbPupilIsokor,RbPupilAnisokor)+" (Kiri: "+TDiameterPupilKiri.getText()+", Kanan: "+TDiameterPupilKanan.getText()+")"));
        h.append(baris("Reflek Cahaya","Kiri: "+pilihan(RbReflekKiriPositif,RbReflekKiriNegatif)+", Kanan: "+pilihan(RbReflekKananPositif,RbReflekKananNegatif)));
        h.append(baris("Meningeal Signs",TMeningealSigns.getText()));
        h.append(baris("Lateralisasi",pilihan(RbLateralisasiKanan,RbLateralisasiKiri,RbLateralisasiTidakAda)));

        h.append(judulBaris("G. EKSPOSUR"));
        h.append(baris("Kondisi",cekList(CekEksDeformitas,CekEksEkskoriasi,CekEksContusio,CekEksAbrasi,CekEksPenetrasi,CekEksBurn,CekEksTenderness,CekEksLaserasi,CekEksSwelling,CekEksTidakTampakJelas)));

        h.append(judulBaris("H. KEPALA LEHER"));
        h.append(baris("Konjungtiva / Sklera",pilihan(RbKonjungtivaPucat,RbKonjungtivaPink)+" / "+pilihan(RbSkleraIkterik,RbSkleraTidakIkterik)));
        h.append(baris("Bibir/Lidah / Mukosa Mulut",pilihan(RbBibirLidahSianosis,RbBibirLidahTidakSianosis)+" / "+pilihan(RbMukosaKering,RbMukosaBasah)));

        h.append(judulBaris("I. LEHER RINCI"));
        h.append(baris("Deviasi Trakea",pilihan(RbDeviasiKanan,RbDeviasiKiri,RbDeviasiTidakAda)));
        h.append(baris("JVP / LNN / Tiroid",pilihan(RbJvpMeningkat,RbJvpTidak)+" / "+pilihan(RbLnnTeraba,RbLnnTidak)+" / "+pilihan(RbTiroidTeraba,RbTiroidTidak)));

        h.append(judulBaris("J. THORAX"));
        h.append(baris("Jantung",TJantung.getText()));
        h.append(baris("Paru",TParu.getText()));

        h.append(judulBaris("K. ABDOMEN & PELVIS"));
        h.append(baris("Punggung/Pinggang",TPunggungPinggang.getText()));
        h.append(baris("Ekstremitas & Pelvis",TEkstremitasPelvis.getText()));

        h.append(judulBaris("L. PEMERIKSAAN LAINNYA"));
        h.append(baris("Keterangan",PemeriksaanLainnya.getText()));

        h.append(judulBaris("M. DIAGNOSIS"));
        h.append(baris("Diagnosa Kerja",(CekDiagnosaKerja.isSelected()?"[X] ":"[ ] ")+TDiagnosaKerja.getText()));
        h.append(baris("Diagnosa Banding",(CekDiagnosaBanding.isSelected()?"[X] ":"[ ] ")+TDiagnosaBanding.getText()));

        h.append(judulBaris("N. KONSUL DR. SPESIALIS"));
        h.append(baris("Dokter",KonsulKdDokter.getText().trim().isEmpty()?"-":KonsulKdDokter.getText()+" "+KonsulNmDokter.getText()));
        h.append(baris("Unit/Poli",KonsulKdPoli.getText().trim().isEmpty()?"-":KonsulKdPoli.getText()+" "+KonsulNmPoli.getText()));

        h.append(judulBaris("O. EDUKASI"));
        h.append(baris("Status",CekEdukasiDiberikan.isSelected()?"Diberikan":(CekEdukasiTidakBisa.isSelected()?"Tidak Bisa ("+TEdukasiTidakBisaKet.getText()+")":"-")));

        h.append(judulBaris("P. KONDISI SAAT PULANG"));
        String kondisiPulangStr = pilihan(RbKondisiMembaik,RbKondisiMemburuk,RbKondisiTetap,RbKondisiMeninggal,RbKondisiDoa);
        h.append(barisWarna("Kondisi Pulang",kondisiPulangStr+(RbKondisiMeninggal.isSelected()?(", Jam "+DTPKondisiPulangJam.getSelectedItem()):""),
            RbKondisiMeninggal.isSelected()||RbKondisiDoa.isSelected()||RbKondisiMemburuk.isSelected()?"#AA0000":"#00AA00"));

        h.append(judulBaris("Q. TANDA VITAL TERAKHIR"));
        h.append(baris("Tanda Vital","TD "+TTdAkhir.getText()+", Nadi "+TNadiAkhir.getText()+", RR "+TRrAkhir.getText()+", Suhu "+TSuhuAkhir.getText()));

        LoadHTML.setText(
            "<html><table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
            h.toString()+
            "</table></html>"
        );
        // JEditorPane scroll ke bawah dokumen setelah setText() (kuirk Swing
        // baku) - paksa kembali ke atas, sama pola RMPenilaianAwalKeperawatanIGD.
        LoadHTML.setCaretPosition(0);
    }

    private void getData() {
        if(tbObat.getSelectedRow()== -1){ return; }
        String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();
        TNoRM1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
        TPasien1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
        try{
            PreparedStatement psData = koneksi.prepareStatement(
                "select penilaian_medis_igd.*, "+
                "reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, if(pasien.jk='L','Laki-Laki','Perempuan') as jk, "+
                "dokter.nm_dokter, "+
                "konsul_dokter.nm_dokter as konsul_nm_dokter, poliklinik.nm_poli as konsul_nm_poli "+
                "from penilaian_medis_igd "+
                "inner join reg_periksa on reg_periksa.no_rawat=penilaian_medis_igd.no_rawat "+
                "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                "inner join dokter on dokter.kd_dokter=penilaian_medis_igd.kd_dokter "+
                "left join dokter konsul_dokter on konsul_dokter.kd_dokter=penilaian_medis_igd.konsul_kd_dokter "+
                "left join poliklinik on poliklinik.kd_poli=penilaian_medis_igd.konsul_kd_poli "+
                "where penilaian_medis_igd.no_rawat=?");
            try{
                psData.setString(1,noRawat);
                ResultSet rsData = psData.executeQuery();
                try{
                    if(rsData.next()){
                        TNoRw.setText(noRawat);
                        TNoRM.setText(rsData.getString("no_rkm_medis"));
                        TPasien.setText(rsData.getString("nm_pasien"));
                        TglLahir.setText(rsData.getString("tgl_lahir"));
                        Jk.setText(rsData.getString("jk"));
                        KdDokter.setText(rsData.getString("kd_dokter"));
                        NmDokter.setText(rsData.getString("nm_dokter"));
                        Valid.SetTgl2(TglAsuhan,rsData.getString("tanggal"));

                        Anamnesis.setSelectedItem(rsData.getString("anamnesis"));
                        Hubungan.setText(rsData.getString("hubungan"));
                        KeluhanUtama.setText(rsData.getString("keluhan_utama"));
                        RPS.setText(rsData.getString("rps"));
                        RPD.setText(rsData.getString("rpd"));
                        RPK.setText(rsData.getString("rpk"));
                        RPO.setText(rsData.getString("rpo"));
                        Alergi.setText(rsData.getString("alergi"));
                        Keadaan.setSelectedItem(rsData.getString("keadaan"));
                        Kesadaran.setSelectedItem(rsData.getString("kesadaran"));
                        TD.setText(rsData.getString("td"));
                        Nadi.setText(rsData.getString("nadi"));
                        RR.setText(rsData.getString("rr"));
                        Suhu.setText(rsData.getString("suhu"));
                        SPO.setText(rsData.getString("spo"));
                        BB.setText(rsData.getString("bb"));
                        TB.setText(rsData.getString("tb"));
                        KetFisik.setText(rsData.getString("ket_fisik"));
                        KetLokalis.setText(rsData.getString("ket_lokalis"));
                        EKG.setText(rsData.getString("ekg"));
                        Radiologi.setText(rsData.getString("rad"));
                        Laborat.setText(rsData.getString("lab"));
                        Tatalaksana.setText(rsData.getString("tata"));

                        // -- A. Intervensi Pre Hospital --
                        CekPreCervicalCollar.setSelected("Ya".equals(rsData.getString("pre_cervical_collar")));
                        CekPreDefibrilasi.setSelected("Ya".equals(rsData.getString("pre_defibrilasi")));
                        CekPreVtp.setSelected("Ya".equals(rsData.getString("pre_vtp")));
                        CekPreBalutBidai.setSelected("Ya".equals(rsData.getString("pre_balut_bidai")));
                        CekPreNgt.setSelected("Ya".equals(rsData.getString("pre_ngt")));
                        CekPreRjp.setSelected("Ya".equals(rsData.getString("pre_rjp")));
                        CekPreIntubasi.setSelected("Ya".equals(rsData.getString("pre_intubasi")));
                        CekPreDekompresiWsd.setSelected("Ya".equals(rsData.getString("pre_dekompresi_wsd")));
                        CekPreKateterUrin.setSelected("Ya".equals(rsData.getString("pre_kateter_urin")));
                        CekPreInfus.setSelected("Ya".equals(rsData.getString("pre_infus")));
                        CekPreTidakAda.setSelected("Ya".equals(rsData.getString("pre_tidak_ada")));
                        TPreObatKet.setText(rsData.getString("pre_obat_ket"));

                        // -- B. Survei Primer - Jalan Napas --
                        CekJnPaten.setSelected("Ya".equals(rsData.getString("jn_paten")));
                        CekJnObstruksiPartial.setSelected("Ya".equals(rsData.getString("jn_obstruksi_partial")));
                        CekJnStridor.setSelected("Ya".equals(rsData.getString("jn_stridor")));
                        CekJnSnoring.setSelected("Ya".equals(rsData.getString("jn_snoring")));
                        CekJnGurgling.setSelected("Ya".equals(rsData.getString("jn_gurgling")));
                        CekJnWheezing.setSelected("Ya".equals(rsData.getString("jn_wheezing")));
                        CekJnObstruksiTotal.setSelected("Ya".equals(rsData.getString("jn_obstruksi_total")));
                        CekJnTraumaJalanNapas.setSelected("Ya".equals(rsData.getString("jn_trauma_jalan_napas")));
                        CekJnFasialLeherInhalasi.setSelected("Ya".equals(rsData.getString("jn_fasial_leher_inhalasi")));
                        CekJnRisikoAspirasi.setSelected("Ya".equals(rsData.getString("jn_risiko_aspirasi")));
                        CekJnBendaAsing.setSelected("Ya".equals(rsData.getString("jn_benda_asing")));
                        String jnKesimpulan = rsData.getString("jn_kesimpulan");
                        if("Aman".equals(jnKesimpulan)) RbJnAman.setSelected(true);
                        else if("Mengancam Jiwa".equals(jnKesimpulan)) RbJnMengancamJiwa.setSelected(true);
                        else bgJnKesimpulan.clearSelection();

                        // -- C. Survei Primer - Pernapasan --
                        String napPola = rsData.getString("nap_pola");
                        if("Spontan".equals(napPola)) RbNapSpontan.setSelected(true);
                        else if("Tidak Spontan".equals(napPola)) RbNapTidakSpontan.setSelected(true);
                        else bgNapPola.clearSelection();
                        String napReguler = rsData.getString("nap_reguler");
                        if("Reguler".equals(napReguler)) RbNapReguler.setSelected(true);
                        else if("Irreguler".equals(napReguler)) RbNapIrreguler.setSelected(true);
                        else bgNapReguler.clearSelection();
                        String napGerakanDada = rsData.getString("nap_gerakan_dada");
                        if("Simetris".equals(napGerakanDada)) RbNapSimetris.setSelected(true);
                        else if("Asimetris".equals(napGerakanDada)) RbNapAsimetris.setSelected(true);
                        else bgNapGerakanDada.clearSelection();
                        CekNapJejasKanan.setSelected("Ya".equals(rsData.getString("nap_jejas_kanan")));
                        CekNapJejasKiri.setSelected("Ya".equals(rsData.getString("nap_jejas_kiri")));
                        CekNapTipeNormal.setSelected("Ya".equals(rsData.getString("nap_tipe_normal")));
                        CekNapTipeTakipneu.setSelected("Ya".equals(rsData.getString("nap_tipe_takipneu")));
                        CekNapTipeKussmaul.setSelected("Ya".equals(rsData.getString("nap_tipe_kussmaul")));
                        CekNapTipeHiperventilasi.setSelected("Ya".equals(rsData.getString("nap_tipe_hiperventilasi")));
                        CekNapTipeBiot.setSelected("Ya".equals(rsData.getString("nap_tipe_biot")));
                        CekNapTipeCheyneStoke.setSelected("Ya".equals(rsData.getString("nap_tipe_cheyne_stoke")));
                        CekNapTipeApneustic.setSelected("Ya".equals(rsData.getString("nap_tipe_apneustic")));
                        CekNapTipeRetraktif.setSelected("Ya".equals(rsData.getString("nap_tipe_retraktif")));
                        CekNapTipeFlare.setSelected("Ya".equals(rsData.getString("nap_tipe_flare")));
                        String napKesimpulan = rsData.getString("nap_kesimpulan");
                        if("Aman".equals(napKesimpulan)) RbNapAman.setSelected(true);
                        else if("Mengancam Jiwa".equals(napKesimpulan)) RbNapMengancamJiwa.setSelected(true);
                        else bgNapKesimpulan.clearSelection();

                        // -- D. Survei Primer - Sirkulasi --
                        String sirNadi = rsData.getString("sir_nadi");
                        if("Reguler".equals(sirNadi)) RbSirReguler.setSelected(true);
                        else if("Irreguler".equals(sirNadi)) RbSirIrreguler.setSelected(true);
                        else bgSirNadi.clearSelection();
                        String sirNadiKekuatan = rsData.getString("sir_nadi_kekuatan");
                        if("Kuat".equals(sirNadiKekuatan)) RbSirKuat.setSelected(true);
                        else if("Lemah".equals(sirNadiKekuatan)) RbSirLemah.setSelected(true);
                        else bgSirNadiKekuatan.clearSelection();
                        String sirKulitWarna = rsData.getString("sir_kulit_warna");
                        if("Normal".equals(sirKulitWarna)) RbSirKulitNormal.setSelected(true);
                        else if("Pucat".equals(sirKulitWarna)) RbSirKulitPucat.setSelected(true);
                        else bgSirKulitWarna.clearSelection();
                        String sirKulitKondisi = rsData.getString("sir_kulit_kondisi");
                        if("Jaundice".equals(sirKulitKondisi)) RbSirJaundice.setSelected(true);
                        else if("Cyanosis".equals(sirKulitKondisi)) RbSirCyanosis.setSelected(true);
                        else bgSirKulitKondisi.clearSelection();
                        CekSirBerkeringat.setSelected("Ya".equals(rsData.getString("sir_berkeringat")));
                        String sirAkral = rsData.getString("sir_akral");
                        if("Hangat".equals(sirAkral)) RbSirHangat.setSelected(true);
                        else if("Dingin".equals(sirAkral)) RbSirDingin.setSelected(true);
                        else bgSirAkral.clearSelection();
                        String sirAkralKondisi = rsData.getString("sir_akral_kondisi");
                        if("Kering".equals(sirAkralKondisi)) RbSirKering.setSelected(true);
                        else if("Basah".equals(sirAkralKondisi)) RbSirBasah.setSelected(true);
                        else bgSirAkralKondisi.clearSelection();
                        String sirCrt = rsData.getString("sir_crt");
                        if("<2 detik".equals(sirCrt)) RbSirCrtKurang2.setSelected(true);
                        else if(">2 detik".equals(sirCrt)) RbSirCrtLebih2.setSelected(true);
                        else bgSirCrt.clearSelection();
                        String sirKesimpulan = rsData.getString("sir_kesimpulan");
                        if("Aman".equals(sirKesimpulan)) RbSirAman.setSelected(true);
                        else if("Mengancam Jiwa".equals(sirKesimpulan)) RbSirMengancamJiwa.setSelected(true);
                        else bgSirKesimpulan.clearSelection();

                        // -- E. Kategori Pasien + Bayi Baru Lahir (APGAR/PAT) --
                        KategoriPasien.setSelectedItem(rsData.getString("kategori_pasien"));
                        TApgarCukupBulan.setText(rsData.getString("apgar_cukup_bulan"));
                        TApgarCairanAmnionJernih.setText(rsData.getString("apgar_cairan_amnion_jernih"));
                        TApgarPernapasanMenangis.setText(rsData.getString("apgar_pernapasan_menangis"));
                        TApgarTonusOtot.setText(rsData.getString("apgar_tonus_otot"));
                        TPatPenampilan.setText(rsData.getString("pat_penampilan"));
                        TPatUsahaNapas.setText(rsData.getString("pat_usaha_napas"));
                        TPatPerfusiKulit.setText(rsData.getString("pat_perfusi_kulit"));
                        perbaruiVisibilitasBayiBaruLahir();

                        // -- F. Disabilitas --
                        TGcsE.setText(rsData.getString("gcs_e"));
                        TGcsV.setText(rsData.getString("gcs_v"));
                        TGcsM.setText(rsData.getString("gcs_m"));
                        String pupil = rsData.getString("pupil");
                        if("Isokor".equals(pupil)) RbPupilIsokor.setSelected(true);
                        else if("Anisokor".equals(pupil)) RbPupilAnisokor.setSelected(true);
                        else bgPupil.clearSelection();
                        TDiameterPupilKiri.setText(rsData.getString("diameter_pupil_kiri"));
                        TDiameterPupilKanan.setText(rsData.getString("diameter_pupil_kanan"));
                        String reflekKiri = rsData.getString("refleks_cahaya_kiri");
                        if("Positif".equals(reflekKiri)) RbReflekKiriPositif.setSelected(true);
                        else if("Negatif".equals(reflekKiri)) RbReflekKiriNegatif.setSelected(true);
                        else bgReflekKiri.clearSelection();
                        String reflekKanan = rsData.getString("refleks_cahaya_kanan");
                        if("Positif".equals(reflekKanan)) RbReflekKananPositif.setSelected(true);
                        else if("Negatif".equals(reflekKanan)) RbReflekKananNegatif.setSelected(true);
                        else bgReflekKanan.clearSelection();
                        TMeningealSigns.setText(rsData.getString("meningeal_signs"));
                        String lateralisasi = rsData.getString("lateralisasi");
                        if("Kanan".equals(lateralisasi)) RbLateralisasiKanan.setSelected(true);
                        else if("Kiri".equals(lateralisasi)) RbLateralisasiKiri.setSelected(true);
                        else if("Tidak Ada".equals(lateralisasi)) RbLateralisasiTidakAda.setSelected(true);
                        else bgLateralisasi.clearSelection();

                        // -- G. Eksposur --
                        CekEksDeformitas.setSelected("Ya".equals(rsData.getString("eks_deformitas")));
                        CekEksEkskoriasi.setSelected("Ya".equals(rsData.getString("eks_ekskoriasi")));
                        CekEksContusio.setSelected("Ya".equals(rsData.getString("eks_contusio")));
                        CekEksAbrasi.setSelected("Ya".equals(rsData.getString("eks_abrasi")));
                        CekEksPenetrasi.setSelected("Ya".equals(rsData.getString("eks_penetrasi")));
                        CekEksBurn.setSelected("Ya".equals(rsData.getString("eks_burn")));
                        CekEksTenderness.setSelected("Ya".equals(rsData.getString("eks_tenderness")));
                        CekEksLaserasi.setSelected("Ya".equals(rsData.getString("eks_laserasi")));
                        CekEksSwelling.setSelected("Ya".equals(rsData.getString("eks_swelling")));
                        CekEksTidakTampakJelas.setSelected("Ya".equals(rsData.getString("eks_tidak_tampak_jelas")));

                        // -- H. Kepala Leher --
                        String konjungtiva = rsData.getString("konjungtiva");
                        if("Pucat".equals(konjungtiva)) RbKonjungtivaPucat.setSelected(true);
                        else if("Pink".equals(konjungtiva)) RbKonjungtivaPink.setSelected(true);
                        else bgKonjungtiva.clearSelection();
                        String sklera = rsData.getString("sklera");
                        if("Ikterik".equals(sklera)) RbSkleraIkterik.setSelected(true);
                        else if("Tidak Ikterik".equals(sklera)) RbSkleraTidakIkterik.setSelected(true);
                        else bgSklera.clearSelection();
                        String bibirLidah = rsData.getString("bibir_lidah");
                        if("Sianosis".equals(bibirLidah)) RbBibirLidahSianosis.setSelected(true);
                        else if("Tidak Sianosis".equals(bibirLidah)) RbBibirLidahTidakSianosis.setSelected(true);
                        else bgBibirLidah.clearSelection();
                        String mukosaMulut = rsData.getString("mukosa_mulut");
                        if("Kering".equals(mukosaMulut)) RbMukosaKering.setSelected(true);
                        else if("Basah".equals(mukosaMulut)) RbMukosaBasah.setSelected(true);
                        else bgMukosaMulut.clearSelection();

                        // -- I. Leher rinci --
                        String deviasiTrakea = rsData.getString("deviasi_trakea");
                        if("Kanan".equals(deviasiTrakea)) RbDeviasiKanan.setSelected(true);
                        else if("Kiri".equals(deviasiTrakea)) RbDeviasiKiri.setSelected(true);
                        else if("Tidak Ada".equals(deviasiTrakea)) RbDeviasiTidakAda.setSelected(true);
                        else bgDeviasiTrakea.clearSelection();
                        String jvp = rsData.getString("jvp");
                        if("Meningkat".equals(jvp)) RbJvpMeningkat.setSelected(true);
                        else if("Tidak".equals(jvp)) RbJvpTidak.setSelected(true);
                        else bgJvp.clearSelection();
                        String lnn = rsData.getString("lnn");
                        if("Teraba".equals(lnn)) RbLnnTeraba.setSelected(true);
                        else if("Tidak".equals(lnn)) RbLnnTidak.setSelected(true);
                        else bgLnn.clearSelection();
                        String tiroid = rsData.getString("tiroid");
                        if("Teraba".equals(tiroid)) RbTiroidTeraba.setSelected(true);
                        else if("Tidak".equals(tiroid)) RbTiroidTidak.setSelected(true);
                        else bgTiroid.clearSelection();

                        // -- J. Thorax --
                        TJantung.setText(rsData.getString("jantung"));
                        TParu.setText(rsData.getString("paru"));

                        // -- K. Abdomen & Pelvis --
                        TPunggungPinggang.setText(rsData.getString("punggung_pinggang"));
                        TEkstremitasPelvis.setText(rsData.getString("ekstremitas_pelvis"));

                        // -- L. Pemeriksaan Lainnya --
                        PemeriksaanLainnya.setText(rsData.getString("pemeriksaan_lainnya"));

                        // -- M. Diagnosis (Kerja/Banding) --
                        CekDiagnosaKerja.setSelected("Ya".equals(rsData.getString("diagnosa_kerja_cek")));
                        TDiagnosaKerja.setText(rsData.getString("diagnosa_kerja"));
                        CekDiagnosaBanding.setSelected("Ya".equals(rsData.getString("diagnosa_banding_cek")));
                        TDiagnosaBanding.setText(rsData.getString("diagnosa_banding"));

                        // -- N. Konsul dr. Spesialis --
                        KonsulKdDokter.setText(rsData.getString("konsul_kd_dokter"));
                        KonsulNmDokter.setText(rsData.getString("konsul_nm_dokter"));
                        KonsulKdPoli.setText(rsData.getString("konsul_kd_poli"));
                        KonsulNmPoli.setText(rsData.getString("konsul_nm_poli"));

                        // -- O. Edukasi --
                        CekEdukasiDiberikan.setSelected("Ya".equals(rsData.getString("edukasi_diberikan")));
                        CekEdukasiTidakBisa.setSelected("Ya".equals(rsData.getString("edukasi_tidak_bisa")));
                        TEdukasiTidakBisaKet.setText(rsData.getString("edukasi_tidak_bisa_ket"));

                        // -- P. Kondisi Saat Pulang --
                        String kondisiPulang = rsData.getString("kondisi_pulang");
                        bgKondisiPulang.clearSelection();
                        if("Membaik".equals(kondisiPulang)) RbKondisiMembaik.setSelected(true);
                        else if("Memburuk".equals(kondisiPulang)) RbKondisiMemburuk.setSelected(true);
                        else if("Tetap".equals(kondisiPulang)) RbKondisiTetap.setSelected(true);
                        else if("Meninggal".equals(kondisiPulang)) RbKondisiMeninggal.setSelected(true);
                        else if("Death On Arrival".equals(kondisiPulang)) RbKondisiDoa.setSelected(true);
                        String kondisiPulangJam = rsData.getString("kondisi_pulang_jam");
                        if(kondisiPulangJam!=null && !kondisiPulangJam.trim().equals("")){
                            Valid.SetTgl2(DTPKondisiPulangJam,rsData.getString("tanggal")+" "+kondisiPulangJam);
                        }
                        perbaruiVisibilitasJamMeninggal();

                        // -- Q. Tanda Vital Terakhir --
                        TTdAkhir.setText(rsData.getString("td_akhir"));
                        TNadiAkhir.setText(rsData.getString("nadi_akhir"));
                        TRrAkhir.setText(rsData.getString("rr_akhir"));
                        TSuhuAkhir.setText(rsData.getString("suhu_akhir"));

                        bangunRingkasanHtml();
                        ChkAccor.setSelected(true);
                        isMenu();
                    }
                } finally {
                    if(rsData!=null){ rsData.close(); }
                }
            } finally {
                if(psData!=null){ psData.close(); }
            }
        }catch(Exception e){
            System.out.println("Notif getData RM 03-2 : "+e);
        }
    }

    private void isRawat() {
        try {
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien, if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,"+
                    "reg_periksa.tgl_registrasi,reg_periksa.jam_reg "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1,TNoRw.getText());
                rs=ps.executeQuery();
                if(rs.next()){
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
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
            System.out.println("Notif Cari Pasien : "+e);
        }
    }
 
    public void setNoRm(String norwt,Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);    
        isRawat(); 
    }
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getpenilaian_awal_medis_igd());
        BtnHapus.setEnabled(akses.getpenilaian_awal_medis_igd());
        BtnEdit.setEnabled(akses.getpenilaian_awal_medis_igd());
        if(akses.getjml2()>=1){
            KdDokter.setEditable(false);
            BtnDokter.setEnabled(false);
            KdDokter.setText(akses.getkode());
            NmDokter.setText(Sequel.CariDokter(KdDokter.getText()));
            if(NmDokter.getText().equals("")){
                KdDokter.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan Dokter...!!");
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

    private void hapus() {
        if(Sequel.queryu2tf("delete from penilaian_medis_igd where no_rawat=?",1,new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })==true){
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText(""+tabMode.getRowCount());
            TabRawat.setSelectedIndex(1);
            TNoRM1.setText("");
            TPasien1.setText("");
            LoadHTML.setText("");
            ChkAccor.setSelected(false);
            isMenu();
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
        }
    }

    /**
     * [RM 03-2] Kolom aktif tabel `penilaian_medis_igd` (di luar `no_rawat`)
     * yang ditulis oleh simpan()/ganti() - urutan HARUS tetap sinkron
     * dengan nilaiRm032(), dipakai bersama supaya SET clause (ganti) dan
     * daftar kolom INSERT (simpan) tidak pernah beda urutan dari nilainya.
     * 10 kolom lama yang dipensiunkan (kepala/mata/gigi/leher/thoraks/
     * abdomen/genital/ekstremitas/gcs/diagnosis) sengaja TIDAK disebut -
     * dapat DEFAULT dari migrasi SQL (KELOMPOK R) saat INSERT.
     */
    private static final String[] KOLOM_RM032 = {
        "tanggal","kd_dokter","anamnesis","hubungan","keluhan_utama","rps","rpd","rpk","rpo","alergi",
        "keadaan","kesadaran","td","nadi","rr","suhu","spo","bb","tb","ket_fisik","ket_lokalis","ekg","rad","lab","tata",

        "pre_cervical_collar","pre_defibrilasi","pre_vtp","pre_balut_bidai","pre_ngt","pre_rjp","pre_intubasi",
        "pre_dekompresi_wsd","pre_kateter_urin","pre_infus","pre_tidak_ada","pre_obat_ket",

        "jn_paten","jn_obstruksi_partial","jn_stridor","jn_snoring","jn_gurgling","jn_wheezing","jn_obstruksi_total",
        "jn_trauma_jalan_napas","jn_fasial_leher_inhalasi","jn_risiko_aspirasi","jn_benda_asing","jn_kesimpulan",

        "nap_pola","nap_reguler","nap_gerakan_dada","nap_jejas_kanan","nap_jejas_kiri","nap_tipe_normal",
        "nap_tipe_takipneu","nap_tipe_kussmaul","nap_tipe_hiperventilasi","nap_tipe_biot","nap_tipe_cheyne_stoke",
        "nap_tipe_apneustic","nap_tipe_retraktif","nap_tipe_flare","nap_kesimpulan",

        "sir_nadi","sir_nadi_kekuatan","sir_kulit_warna","sir_kulit_kondisi","sir_berkeringat","sir_akral",
        "sir_akral_kondisi","sir_crt","sir_kesimpulan",

        "kategori_pasien","apgar_cukup_bulan","apgar_cairan_amnion_jernih","apgar_pernapasan_menangis",
        "apgar_tonus_otot","pat_penampilan","pat_usaha_napas","pat_perfusi_kulit",

        "gcs_e","gcs_v","gcs_m","pupil","diameter_pupil_kiri","diameter_pupil_kanan","refleks_cahaya_kiri",
        "refleks_cahaya_kanan","meningeal_signs","lateralisasi",

        "eks_deformitas","eks_ekskoriasi","eks_contusio","eks_abrasi","eks_penetrasi","eks_burn","eks_tenderness",
        "eks_laserasi","eks_swelling","eks_tidak_tampak_jelas",

        "konjungtiva","sklera","bibir_lidah","mukosa_mulut",

        "deviasi_trakea","jvp","lnn","tiroid",

        "jantung","paru",

        "punggung_pinggang","ekstremitas_pelvis",

        "pemeriksaan_lainnya",

        "diagnosa_kerja_cek","diagnosa_kerja","diagnosa_banding_cek","diagnosa_banding",

        "konsul_kd_dokter","konsul_kd_poli",

        "edukasi_diberikan","edukasi_tidak_bisa","edukasi_tidak_bisa_ket",

        "kondisi_pulang","kondisi_pulang_jam",

        "td_akhir","nadi_akhir","rr_akhir","suhu_akhir"
    };

    private String ya(javax.swing.AbstractButton c){
        return c.isSelected() ? "Ya" : "Tidak";
    }

    /**
     * [RM 03-2] Nilai dari semua widget aktif, urutan HARUS sinkron dengan
     * KOLOM_RM032 di atas. Dipakai bersama oleh ganti() dan simpan().
     */
    private String[] nilaiRm032(){
        String jnKesimpulan = RbJnAman.isSelected() ? "Aman" : (RbJnMengancamJiwa.isSelected() ? "Mengancam Jiwa" : null);
        String napPola = RbNapSpontan.isSelected() ? "Spontan" : (RbNapTidakSpontan.isSelected() ? "Tidak Spontan" : null);
        String napReguler = RbNapReguler.isSelected() ? "Reguler" : (RbNapIrreguler.isSelected() ? "Irreguler" : null);
        String napGerakanDada = RbNapSimetris.isSelected() ? "Simetris" : (RbNapAsimetris.isSelected() ? "Asimetris" : null);
        String napKesimpulan = RbNapAman.isSelected() ? "Aman" : (RbNapMengancamJiwa.isSelected() ? "Mengancam Jiwa" : null);

        String sirNadi = RbSirReguler.isSelected() ? "Reguler" : (RbSirIrreguler.isSelected() ? "Irreguler" : null);
        String sirNadiKekuatan = RbSirKuat.isSelected() ? "Kuat" : (RbSirLemah.isSelected() ? "Lemah" : null);
        String sirKulitWarna = RbSirKulitNormal.isSelected() ? "Normal" : (RbSirKulitPucat.isSelected() ? "Pucat" : null);
        String sirKulitKondisi = RbSirJaundice.isSelected() ? "Jaundice" : (RbSirCyanosis.isSelected() ? "Cyanosis" : null);
        String sirAkral = RbSirHangat.isSelected() ? "Hangat" : (RbSirDingin.isSelected() ? "Dingin" : null);
        String sirAkralKondisi = RbSirKering.isSelected() ? "Kering" : (RbSirBasah.isSelected() ? "Basah" : null);
        String sirCrt = RbSirCrtKurang2.isSelected() ? "<2 detik" : (RbSirCrtLebih2.isSelected() ? ">2 detik" : null);
        String sirKesimpulan = RbSirAman.isSelected() ? "Aman" : (RbSirMengancamJiwa.isSelected() ? "Mengancam Jiwa" : null);

        String pupil = RbPupilIsokor.isSelected() ? "Isokor" : (RbPupilAnisokor.isSelected() ? "Anisokor" : null);
        String reflekKiri = RbReflekKiriPositif.isSelected() ? "Positif" : (RbReflekKiriNegatif.isSelected() ? "Negatif" : null);
        String reflekKanan = RbReflekKananPositif.isSelected() ? "Positif" : (RbReflekKananNegatif.isSelected() ? "Negatif" : null);
        String lateralisasi = RbLateralisasiKanan.isSelected() ? "Kanan" : (RbLateralisasiKiri.isSelected() ? "Kiri" : (RbLateralisasiTidakAda.isSelected() ? "Tidak Ada" : null));

        String konjungtiva = RbKonjungtivaPucat.isSelected() ? "Pucat" : (RbKonjungtivaPink.isSelected() ? "Pink" : null);
        String sklera = RbSkleraIkterik.isSelected() ? "Ikterik" : (RbSkleraTidakIkterik.isSelected() ? "Tidak Ikterik" : null);
        String bibirLidah = RbBibirLidahSianosis.isSelected() ? "Sianosis" : (RbBibirLidahTidakSianosis.isSelected() ? "Tidak Sianosis" : null);
        String mukosaMulut = RbMukosaKering.isSelected() ? "Kering" : (RbMukosaBasah.isSelected() ? "Basah" : null);

        String deviasiTrakea = RbDeviasiKanan.isSelected() ? "Kanan" : (RbDeviasiKiri.isSelected() ? "Kiri" : (RbDeviasiTidakAda.isSelected() ? "Tidak Ada" : null));
        String jvp = RbJvpMeningkat.isSelected() ? "Meningkat" : (RbJvpTidak.isSelected() ? "Tidak" : null);
        String lnn = RbLnnTeraba.isSelected() ? "Teraba" : (RbLnnTidak.isSelected() ? "Tidak" : null);
        String tiroid = RbTiroidTeraba.isSelected() ? "Teraba" : (RbTiroidTidak.isSelected() ? "Tidak" : null);

        String kondisiPulang = RbKondisiMembaik.isSelected() ? "Membaik" : (RbKondisiMemburuk.isSelected() ? "Memburuk" :
            (RbKondisiTetap.isSelected() ? "Tetap" : (RbKondisiMeninggal.isSelected() ? "Meninggal" :
            (RbKondisiDoa.isSelected() ? "Death On Arrival" : null))));
        String kondisiPulangJam = RbKondisiMeninggal.isSelected() ? DTPKondisiPulangJam.getSelectedItem().toString().substring(11,19) : null;

        return new String[]{
            Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),
            KdDokter.getText(),Anamnesis.getSelectedItem().toString(),Hubungan.getText(),KeluhanUtama.getText(),
            RPS.getText(),RPD.getText(),RPK.getText(),RPO.getText(),Alergi.getText(),
            Keadaan.getSelectedItem().toString(),Kesadaran.getSelectedItem().toString(),TD.getText(),Nadi.getText(),
            RR.getText(),Suhu.getText(),SPO.getText(),BB.getText(),TB.getText(),KetFisik.getText(),
            KetLokalis.getText(),EKG.getText(),Radiologi.getText(),Laborat.getText(),Tatalaksana.getText(),

            ya(CekPreCervicalCollar),ya(CekPreDefibrilasi),ya(CekPreVtp),ya(CekPreBalutBidai),ya(CekPreNgt),
            ya(CekPreRjp),ya(CekPreIntubasi),ya(CekPreDekompresiWsd),ya(CekPreKateterUrin),ya(CekPreInfus),
            ya(CekPreTidakAda),TPreObatKet.getText(),

            ya(CekJnPaten),ya(CekJnObstruksiPartial),ya(CekJnStridor),ya(CekJnSnoring),ya(CekJnGurgling),
            ya(CekJnWheezing),ya(CekJnObstruksiTotal),ya(CekJnTraumaJalanNapas),ya(CekJnFasialLeherInhalasi),
            ya(CekJnRisikoAspirasi),ya(CekJnBendaAsing),jnKesimpulan,

            napPola,napReguler,napGerakanDada,ya(CekNapJejasKanan),ya(CekNapJejasKiri),ya(CekNapTipeNormal),
            ya(CekNapTipeTakipneu),ya(CekNapTipeKussmaul),ya(CekNapTipeHiperventilasi),ya(CekNapTipeBiot),
            ya(CekNapTipeCheyneStoke),ya(CekNapTipeApneustic),ya(CekNapTipeRetraktif),ya(CekNapTipeFlare),napKesimpulan,

            sirNadi,sirNadiKekuatan,sirKulitWarna,sirKulitKondisi,ya(CekSirBerkeringat),sirAkral,sirAkralKondisi,
            sirCrt,sirKesimpulan,

            KategoriPasien.getSelectedItem().toString(),TApgarCukupBulan.getText(),TApgarCairanAmnionJernih.getText(),
            TApgarPernapasanMenangis.getText(),TApgarTonusOtot.getText(),TPatPenampilan.getText(),
            TPatUsahaNapas.getText(),TPatPerfusiKulit.getText(),

            TGcsE.getText(),TGcsV.getText(),TGcsM.getText(),pupil,TDiameterPupilKiri.getText(),
            TDiameterPupilKanan.getText(),reflekKiri,reflekKanan,TMeningealSigns.getText(),lateralisasi,

            ya(CekEksDeformitas),ya(CekEksEkskoriasi),ya(CekEksContusio),ya(CekEksAbrasi),ya(CekEksPenetrasi),
            ya(CekEksBurn),ya(CekEksTenderness),ya(CekEksLaserasi),ya(CekEksSwelling),ya(CekEksTidakTampakJelas),

            konjungtiva,sklera,bibirLidah,mukosaMulut,

            deviasiTrakea,jvp,lnn,tiroid,

            TJantung.getText(),TParu.getText(),

            TPunggungPinggang.getText(),TEkstremitasPelvis.getText(),

            PemeriksaanLainnya.getText(),

            ya(CekDiagnosaKerja),TDiagnosaKerja.getText(),ya(CekDiagnosaBanding),TDiagnosaBanding.getText(),

            KonsulKdDokter.getText(),KonsulKdPoli.getText(),

            ya(CekEdukasiDiberikan),ya(CekEdukasiTidakBisa),TEdukasiTidakBisaKet.getText(),

            kondisiPulang,kondisiPulangJam,

            TTdAkhir.getText(),TNadiAkhir.getText(),TRrAkhir.getText(),TSuhuAkhir.getText()
        };
    }

    private void ganti() {
        StringBuilder set = new StringBuilder();
        for(String kolom : KOLOM_RM032){
            set.append(kolom).append("=?,");
        }
        set.setLength(set.length()-1);

        String[] nilai = nilaiRm032();
        String[] param = new String[nilai.length+1];
        System.arraycopy(nilai,0,param,0,nilai.length);
        param[nilai.length] = tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();

        if(Sequel.mengedittf("penilaian_medis_igd","no_rawat=?",set.toString(),param.length,param)==true){
            tbObat.setValueAt(TNoRw.getText(),tbObat.getSelectedRow(),0);
            tbObat.setValueAt(TNoRM.getText(),tbObat.getSelectedRow(),1);
            tbObat.setValueAt(TPasien.getText(),tbObat.getSelectedRow(),2);
            tbObat.setValueAt(TglLahir.getText(),tbObat.getSelectedRow(),3);
            tbObat.setValueAt(Jk.getText(),tbObat.getSelectedRow(),4);
            tbObat.setValueAt(KdDokter.getText(),tbObat.getSelectedRow(),5);
            tbObat.setValueAt(NmDokter.getText(),tbObat.getSelectedRow(),6);
            tbObat.setValueAt(Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),tbObat.getSelectedRow(),7);
            tbObat.setValueAt(TDiagnosaKerja.getText(),tbObat.getSelectedRow(),8);
            emptTeks();
            TabRawat.setSelectedIndex(1);
        }
    }

    /**
     * [RM 03-2] INSERT manual bernama kolom (bukan Sequel.menyimpantf, yang
     * posisional murni mengikuti urutan fisik SEMUA kolom tabel - tidak
     * cocok utk tabel ini dgn ~131 kolom). Pola sama seperti isRawat() di
     * file ini: koneksi.prepareStatement() langsung. 10 kolom lama yang
     * dipensiunkan tidak disebut - otomatis kebagian DEFAULT dari migrasi
     * (KELOMPOK R di sik_penilaian_medis_igd_rm032.sql).
     */
    private void simpan() {
        StringBuilder kolom = new StringBuilder("no_rawat");
        StringBuilder placeholder = new StringBuilder("?");
        for(String k : KOLOM_RM032){
            kolom.append(",").append(k);
            placeholder.append(",?");
        }

        String[] nilai = nilaiRm032();
        String[] param = new String[nilai.length+1];
        param[0] = TNoRw.getText();
        System.arraycopy(nilai,0,param,1,nilai.length);

        boolean sukses = false;
        try{
            PreparedStatement psSimpan = koneksi.prepareStatement(
                "insert into penilaian_medis_igd ("+kolom+") values ("+placeholder+")");
            try{
                for(int i=0;i<param.length;i++){
                    psSimpan.setString(i+1,param[i]);
                }
                psSimpan.executeUpdate();
                sukses = true;
            } finally {
                if(psSimpan!=null){ psSimpan.close(); }
            }
        }catch(Exception e){
            System.out.println("Notif Simpan RM 03-2 : "+e);
            JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan No.Rawat yang sama sudah dimasukkan sebelumnya...!");
        }

        if(sukses){
            tabMode.addRow(new Object[]{
                TNoRw.getText(),TNoRM.getText(),TPasien.getText(),TglLahir.getText(),Jk.getText(),
                KdDokter.getText(),NmDokter.getText(),
                Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),
                TDiagnosaKerja.getText()
            });
            LCount.setText(""+tabMode.getRowCount());
            emptTeks();
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
}
