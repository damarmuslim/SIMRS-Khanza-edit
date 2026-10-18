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
import java.sql.SQLException;
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
        javax.swing.Icon icPrintHtml = widget.IconStyle.muat("printer.svg", 14, widget.IconStyle.ABU);
        if (icPrintHtml != null) {
            BtnPrint1.setIcon(icPrintHtml);
            BtnPrint1.setToolTipText("Cetak Pengkajian (Alt+T)");
        }
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
        LoadHTML.setEditable(false);
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
        siapkanKontenRiwayatDanKeadaanUmumMedis();
        daftarkanSeksiLegacyMedis();
        pasangChipSemuaMedis();
        initBingkaiMedis();
        initActionBarMedis();
        susunHalamanMedis();
        susunRangkaMedis();
        buatKotakMedis(FormInput);
        buatKotakMedis(barPasienMedis);
        rapikanBarPencarianMedis();
        pasangScrollBertingkatTextAreaMedis(FormInput);
        pasangSemuaListenerProgresMedis();
        hitungProgresMedis();
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
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

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
            Valid.textKosong(KeluhanUtama,"Anamnesis");
        }else if(!diagnosisMedisValid()){
            return;
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
            Valid.textKosong(KeluhanUtama,"Anamnesis");
        }else if(!diagnosisMedisValid()){
            return;
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

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {
        if (TabRawat.getSelectedIndex() == 1) {
            runBackground(() -> tampil());
        }
    }

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
                String b64Lokalis = Sequel.cariIsi("select lokalis_canvas_gambar from penilaian_medis_igd where no_rawat=?", tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
                param.put("lokalis", KanvasStatusLokalis.gambarCetakStream(b64Lokalis));
            } catch (Exception e) {
                try {
                    param.put("lokalis", getClass().getResource("/picture/semua.png").openStream());
                } catch (Exception ex) {}
            }
            try {
                String noRw = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
                String patApp = Sequel.cariIsi("select pat_penampilan from penilaian_medis_igd where no_rawat=?", noRw);
                String patWork = Sequel.cariIsi("select pat_usaha_napas from penilaian_medis_igd where no_rawat=?", noRw);
                String patCirc = Sequel.cariIsi("select pat_perfusi_kulit from penilaian_medis_igd where no_rawat=?", noRw);
                param.put("segitiga_pat", gambarSegitigaPATStream(patApp, patWork, patCirc));
            } catch (Exception e) {
                param.put("segitiga_pat", null);
            }
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            param.put("finger","Dikeluarkan di "+akses.getnamars()+"\nKabupaten "+akses.getkabupatenrs()+"\nDitandatangani oleh: "+tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());

            String qryCetak =
                "select penilaian_medis_igd.*, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, "+
                "if(pasien.jk='L','Laki-Laki','Perempuan') as jk, dokter.nm_dokter, coalesce(pegawai_dokter.nik,dokter.kd_dokter) as nip_dokter, dokter.no_ijn_praktek, "+
                "TIMESTAMPDIFF(YEAR,pasien.tgl_lahir,CURDATE()) as umur, reg_periksa.tgl_registrasi, reg_periksa.jam_reg, "+
                "konsul_dokter.nm_dokter as konsul_nm_dokter, poliklinik.nm_poli as konsul_nm_poli "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join penilaian_medis_igd on reg_periksa.no_rawat=penilaian_medis_igd.no_rawat "+
                "inner join dokter on penilaian_medis_igd.kd_dokter=dokter.kd_dokter "+
                "left join pegawai pegawai_dokter on pegawai_dokter.nik=dokter.kd_dokter "+
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
    /** Konten dua halaman legacy yang dirapikan di luar blok GEN. */
    private widget.PanelBiasa panelRiwayatKesehatanMedis;
    private widget.PanelBiasa panelKeadaanUmumMedis;
    private KanvasStatusLokalis kanvasLokalis;
    private PanelPenunjangMedis panelPenunjangMedis;
    /** Pilihan regio ditulis sebagai prefiks KetLokalis agar narasi bebas tetap utuh. */
    private widget.CekBox[] pilihRegioStatusLokalisMedis;
    private boolean memperbaruiRegioStatusLokalisMedis = false;
    private static final String AWAL_REGIO_STATUS_LOKALIS_MEDIS = "[Regio: ";

    // =====================================================================
    // [Sesi 1 - Restrukturisasi UI] Rangka 4 zona + bar pasien tab "Input
    // Pengkajian". Field lama (TNoRw/TPasien/TNoRM/TglLahir/Jk/KdDokter/
    // NmDokter/BtnDokter/TglAsuhan/Anamnesis/Hubungan) TIDAK diganti nama -
    // hanya disembunyikan atau dipindah parent (Container.add() Swing
    // otomatis melepas child dari parent lama), lihat buatBannerMedis().
    // Field baru di bawah ini murni tampilan, tidak pernah dibaca/ditulis
    // oleh getData()/tampil()/emptTeks()/simpan().
    // =====================================================================
    private widget.Label lblKartuNama, lblKartuInfo, lblKartuRM, lblKartuRawat, lblKartuLokasi;
    private AvatarBulatMedis avatarKartuMedis;
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
    // Sesi 5 (Bagian E): kartu progres wajib global, TERPISAH dari kartu
    // posisi di atas (lihat buatKartuWajibMedis()/hitungProgresMedis()).
    private widget.Label lblWajibJudulMedis, lblWajibPersenMedis, lblWajibHitungMedis;
    private javax.swing.JProgressBar pbWajibMedis;
    /** Disiapkan utk Sesi 6 (Bagian G, bar aksi bawah) - selalu null-checked. */
    private widget.Label lblProgresBawahMedis;

    /** Sesi 3b: satu halaman mewakili satu bagian pengkajian, lihat susunHalamanMedis(). */
    private static final class HalamanMedis {
        final String judul;
        final String subjudul;
        javax.swing.JComponent konten;
        widget.PanelBiasa isi;
        int[] sumber = new int[0];
        widget.PanelBiasa baris;
        widget.Label lblJudul;
        widget.Label lblNomor;
        widget.PanelBiasa aksen;
        // Sesi 5 (Bagian E): indikator wajib & status tidak-relevan, murni UI/session.
        int totalWajib = 0;
        int terisiWajib = 0;
        boolean tidakRelevan = false;
        widget.PanelBiasa pnlBadgeStatus;
        widget.Label lblBadgeStatus;
        widget.Button btnTidakRelevan;
        widget.Label lblHitunganRail;
        HalamanMedis(String judul, String subjudul){
            this.judul = judul;
            this.subjudul = subjudul;
        }
    }
    private final java.util.List<HalamanMedis> halamanMedis = new java.util.ArrayList<>();
    /** Dua puluh satu panel sumber tetap menyimpan seluruh widget klinis. */
    private widget.PanelBiasa[] kontenSumberMedis;
    private boolean halamanMedisSiap = false;
    private boolean menyusunHalamanMedis = false;
    private int halamanAktifMedis = -1;
    private int lebarHalamanAktualMedis = -1;
    private boolean satuHalamanMedis = true;

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
    private widget.CekBox CekSirReguler, CekSirIrreguler;
    private widget.CekBox CekSirKuat, CekSirLemah;
    private widget.CekBox CekSirKulitNormal, CekSirKulitPucat;
    private widget.CekBox CekSirJaundice, CekSirCyanosis, CekSirBerkeringat;
    private widget.CekBox CekSirHangat, CekSirDingin;
    private widget.CekBox CekSirKering, CekSirBasah;
    private widget.CekBox CekSirCrtKurang2, CekSirCrtLebih2;
    private widget.CekBox CekSirAman, CekSirMengancamJiwa;

    // -- E. Kategori Pasien (toggle Bayi Baru Lahir) + APGAR + Segitiga PAT (Argon) --
    private widget.ComboBox KategoriPasien;
    private widget.PanelBiasa panelBayiBaruLahir;
    private widget.TextBox TApgarCukupBulan, TApgarCairanAmnionJernih, TApgarPernapasanMenangis, TApgarTonusOtot;
    private widget.TextBox TPatPenampilan, TPatUsahaNapas, TPatPerfusiKulit;
    private javax.swing.JSpinner spnApgar1, spnApgar5;
    private widget.TextBox txtSkorApgar;
    private javax.swing.JLabel lblBannerResusitasi, lblKesanPAT;
    private SegitigaCanvasPAT canvasSegitigaPAT;
    private javax.swing.JButton btnCukupYa, btnCukupTidak, btnAmnionYa, btnAmnionTidak;
    private javax.swing.JButton btnNapasYa, btnNapasTidak, btnTonusYa, btnTonusTidak;
    private javax.swing.JButton btnPatAppNorm, btnPatAppAbn, btnPatWorkNorm, btnPatWorkAbn, btnPatCircNorm, btnPatCircAbn;
    private boolean cukupBulanYa = true, amnionYa = true, napasYa = true, tonusYa = true;
    private boolean patAppNormal = true, patWorkNormal = true, patCircNormal = true;

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
    private widget.CekBox CekDiagnosaKerja; private widget.TextBox KdDiagnosaKerja, TDiagnosaKerja;
    private widget.CekBox CekDiagnosaBanding; private widget.TextBox TDiagnosaBanding;

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
    private PanelTanggalDanJamMedis DTPKondisiPulangJam;

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
        jLabel99.setText("RIWAYAT KESEHATAN");
        jLabel94.setText("PEMERIKSAAN FISIK");
        jLabel100.setText("STATUS LOKALIS");
        jLabel101.setText("PEMERIKSAAN PENUNJANG");
        jLabel103.setText("RENCANA/TINDAKAN/TERAPI");
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

    private widget.PanelBiasa panelCekBox(widget.CekBox... cek){
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,10,0));
        for(widget.CekBox c : cek){ p.add(c); }
        return p;
    }

    private void tautkanCekBoxEksklusif(final widget.CekBox... cek){
        for(final widget.CekBox c : cek){
            c.addItemListener(e -> {
                if(c.isSelected()){
                    for(widget.CekBox other : cek){
                        if(other != c && other.isSelected()){
                            other.setSelected(false);
                        }
                    }
                }
            });
        }
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

    /** Tandai checklist yang pada mockup Survei Primer memang mengisi dua
     *  kolom field. Panel lain tetap memakai chip selebar teks. */
    private widget.PanelBiasa panelChecklistSurveiPrimer(widget.CekBox... cek){
        widget.PanelBiasa p = panelChecklist(cek);
        p.putClientProperty("checklistSurveiPrimer", Boolean.TRUE);
        return p;
    }

    /** Jalan Napas mengikuti konsep grid dua kolom penuh seperti Intervensi
     *  Pre Hospital. Penanda lokal ini tidak mengubah checklist ABCDE lain. */
    private widget.PanelBiasa panelChecklistJalanNapas(widget.CekBox... cek){
        widget.PanelBiasa p = panelChecklist(cek);
        p.putClientProperty("checklistJalanNapas", Boolean.TRUE);
        return p;
    }

    /** Satu kolom isian pendek dengan keterangan kecil di atasnya. */
    private widget.PanelBiasa kolomFieldSurveiPrimer(String keterangan, java.awt.Component field){
        widget.PanelBiasa kolom = new widget.PanelBiasa(new java.awt.BorderLayout(0,3));
        kolom.setOpaque(false);
        widget.Label label = labelRm032(keterangan);
        label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 10));
        label.setForeground(widget.ThemeConfig.TEXT_FAINT);
        kolom.add(label, java.awt.BorderLayout.NORTH);
        kolom.add(field, java.awt.BorderLayout.CENTER);
        return kolom;
    }

    private java.awt.GridBagConstraints rm032Constraint(int gridx,int gridy){
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = gridx; c.gridy = gridy;
        c.anchor = (gridx == 0) ? java.awt.GridBagConstraints.NORTHWEST : java.awt.GridBagConstraints.WEST;
        c.insets = new java.awt.Insets(3,5,3,5);
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        if(gridx == 0){
            c.weightx = 0.0;
        }else{
            c.weightx = 1.0;
        }
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
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl.setPreferredSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, 23));
        lbl.setMinimumSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, 23));
        java.awt.GridBagConstraints c1 = rm032Constraint(0,row);
        c1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        c1.weightx = 0.0;
        c1.insets = new java.awt.Insets(6,10,6,10);
        panel.add(lbl,c1);
        java.awt.GridBagConstraints c2 = rm032Constraint(1,row);
        c2.insets = new java.awt.Insets(6,10,6,10);
        c2.weightx = 1.0;
        c2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panel.add(field,c2);
        return row+1;
    }

    /** Baris halaman legacy dengan judul rata kiri dan field di kolom kanan. */
    private int tambahBarisHalamanLegacyMedis(widget.PanelBiasa panel, int row,
            String judul, java.awt.Component field, boolean tinggi){
        widget.Label label = labelRm032(judul+" :");
        label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD));
        label.setPreferredSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, 23));
        label.setMinimumSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, 23));
        java.awt.GridBagConstraints cLabel = rm032Constraint(0,row);
        cLabel.insets = new java.awt.Insets(9,10,9,10);
        cLabel.anchor = java.awt.GridBagConstraints.NORTHWEST;
        cLabel.weightx = 0.0;
        panel.add(label,cLabel);
        java.awt.GridBagConstraints cField = rm032Constraint(1,row);
        cField.insets = new java.awt.Insets(9,10,9,10);
        cField.weightx = 1.0;
        cField.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panel.add(field,cField);
        return row+1;
    }

    /** Sel grid vital: keterangan kecil di atas, field dan satuan di bawah. */
    private widget.PanelBiasa selTandaVitalMedis(String judul, widget.TextBox field, String satuan){
        widget.PanelBiasa sel = new widget.PanelBiasa(new java.awt.BorderLayout(0,3));
        sel.setOpaque(false);
        widget.Label label = labelRm032(judul);
        label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 10));
        label.setForeground(widget.ThemeConfig.TEXT_FAINT);
        widget.PanelBiasa isian = new widget.PanelBiasa(new java.awt.BorderLayout(5,0));
        isian.setOpaque(false);
        field.setPreferredSize(new java.awt.Dimension(0,TINGGI_FIELD_MEDIS));
        isian.add(field, java.awt.BorderLayout.CENTER);
        if(!satuan.isEmpty()){
            widget.Label unit = labelRm032(satuan);
            unit.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            unit.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 10));
            unit.setForeground(widget.ThemeConfig.TEXT_FAINT);
            isian.add(unit, java.awt.BorderLayout.EAST);
        }
        sel.add(label, java.awt.BorderLayout.NORTH);
        sel.add(isian, java.awt.BorderLayout.CENTER);
        return sel;
    }

    private DlgMasterTemplateTTV dlgTemplateTtv;
    private void bukaDialogTemplateTtv() {
        if (dlgTemplateTtv == null) {
            dlgTemplateTtv = new DlgMasterTemplateTTV(null, true);
        }
        dlgTemplateTtv.setDokter(KdDokter.getText());
        dlgTemplateTtv.setListener((nama, tensi, nadi, respirasi, suhu, spo2, gcs, kesadaran) -> {
            if (tensi != null && !tensi.isEmpty()) TD.setText(tensi);
            if (nadi != null && !nadi.isEmpty()) Nadi.setText(nadi);
            if (respirasi != null && !respirasi.isEmpty()) RR.setText(respirasi);
            if (suhu != null && !suhu.isEmpty()) Suhu.setText(suhu);
            if (spo2 != null && !spo2.isEmpty()) SPO.setText(spo2);
            if (gcs != null && !gcs.isEmpty()) GCS.setText(gcs);
            if (kesadaran != null && !kesadaran.isEmpty()) {
                Kesadaran.setSelectedItem(kesadaran);
            }
        });
        dlgTemplateTtv.setLocationRelativeTo(this);
        dlgTemplateTtv.setVisible(true);
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
                String kes = rsTriase.getString("kesadaran");
                String kel = rsTriase.getString("keluhan_utama");

                if (td != null && !td.trim().isEmpty()) TD.setText(td.trim());
                if (nadi != null && !nadi.trim().isEmpty()) Nadi.setText(nadi.trim());
                if (rr != null && !rr.trim().isEmpty()) RR.setText(rr.trim());
                if (suhu != null && !suhu.trim().isEmpty()) Suhu.setText(suhu.trim());
                if (spo != null && !spo.trim().isEmpty()) SPO.setText(spo.trim());
                if (bb != null && !bb.trim().isEmpty()) BB.setText(bb.trim());
                if (tb != null && !tb.trim().isEmpty()) TB.setText(tb.trim());
                if (kes != null && !kes.trim().isEmpty()) Kesadaran.setSelectedItem(kes.trim());
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
                    "select tensi, nadi, respirasi, suhu_tubuh, spo2, tinggi, berat, gcs, keluhan " +
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
                    String gcs = rsRalan.getString("gcs");
                    String kel = rsRalan.getString("keluhan");

                    if (td != null && !td.trim().isEmpty()) TD.setText(td.trim());
                    if (nadi != null && !nadi.trim().isEmpty()) Nadi.setText(nadi.trim());
                    if (rr != null && !rr.trim().isEmpty()) RR.setText(rr.trim());
                    if (suhu != null && !suhu.trim().isEmpty()) Suhu.setText(suhu.trim());
                    if (spo != null && !spo.trim().isEmpty()) SPO.setText(spo.trim());
                    if (bb != null && !bb.trim().isEmpty()) BB.setText(bb.trim());
                    if (tb != null && !tb.trim().isEmpty()) TB.setText(tb.trim());
                    if (gcs != null && !gcs.trim().isEmpty()) GCS.setText(gcs.trim());
                    if (kel != null && !kel.trim().isEmpty() && (KeluhanUtama.getText().trim().isEmpty() || manual)) {
                        KeluhanUtama.setText(kel.trim());
                    }
                    ditemukan = true;
                }
                rsRalan.close();
                psRalan.close();
            }

            if (manual) {
                if (ditemukan) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Data TTV & Keluhan berhasil disalin dari Triase / Pemeriksaan Awal!", "Sukses", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Belum ada data Triase atau Pemeriksaan Awal untuk pasien ini.", "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarikDataTriase: " + e);
            if (manual) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal menarik data: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private widget.PanelBiasa selAksiTtvMedis() {
        widget.PanelBiasa sel = new widget.PanelBiasa(new java.awt.BorderLayout(0, 3));
        sel.setOpaque(false);
        widget.Label label = labelRm032("Aksi Cepat TTV :");
        label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 10));
        label.setForeground(widget.ThemeConfig.TEXT_FAINT);

        widget.PanelBiasa tombolPanel = new widget.PanelBiasa(new java.awt.GridLayout(1, 2, 6, 0));
        tombolPanel.setOpaque(false);

        widget.Button btnTpl = new widget.Button();
        btnTpl.setText("Template");
        btnTpl.setToolTipText("Buka / Kelola Template TTV Dokter");
        btnTpl.setPreferredSize(new java.awt.Dimension(0, TINGGI_FIELD_MEDIS));
        btnTpl.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        btnTpl.addActionListener(e -> bukaDialogTemplateTtv());

        widget.Button btnTriase = new widget.Button();
        btnTriase.setText("Tarik TTV");
        btnTriase.setToolTipText("Tarik / Refresh Data TTV & Keluhan dari Triase IGD atau Pemeriksaan Awal");
        btnTriase.setPreferredSize(new java.awt.Dimension(0, TINGGI_FIELD_MEDIS));
        btnTriase.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        btnTriase.addActionListener(e -> tarikDataTriase(TNoRw.getText(), true));

        tombolPanel.add(btnTpl);
        tombolPanel.add(btnTriase);

        sel.add(label, java.awt.BorderLayout.NORTH);
        sel.add(tombolPanel, java.awt.BorderLayout.CENTER);
        return sel;
    }

    /**
     * Pemilih tanggal dan jam terpisah untuk waktu meninggal. Kolom legacy
     * kondisi_pulang_jam tetap menerima HH:mm:ss, sedangkan tanggal dipilih
     * eksplisit agar tidak terikat tanggal asesmen.
     */
    private static class PanelTanggalDanJamMedis extends widget.PanelBiasa {
        private final widget.Tanggal tanggal;
        private final widget.ComboBox jam;
        private final widget.ComboBox menit;
        private final widget.ComboBox detik;

        PanelTanggalDanJamMedis(int tinggi){
            setOpaque(false);
            setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,2,0));
            setBorder(null);
            tanggal = new widget.Tanggal();
            tanggal.setDisplayFormat("dd-MM-yyyy");
            tanggal.setPreferredSize(new java.awt.Dimension(102,tinggi));
            tanggal.setBackground(java.awt.Color.WHITE);
            String[] angkaJam = new String[24];
            for(int i=0;i<angkaJam.length;i++){
                angkaJam[i] = (i<10 ? "0" : "")+i;
            }
            String[] angkaMenit = new String[60];
            for(int i=0;i<angkaMenit.length;i++){
                angkaMenit[i] = (i<10 ? "0" : "")+i;
            }
            jam = comboWaktuMedis(angkaJam,tinggi);
            menit = comboWaktuMedis(angkaMenit,tinggi);
            detik = comboWaktuMedis(angkaMenit,tinggi);
            add(tanggal); add(jam); add(pemisahWaktuMedis(tinggi));
            add(menit); add(pemisahWaktuMedis(tinggi)); add(detik);
            setSekarang();
            setMinimumSize(getPreferredSize());
        }

        private widget.ComboBox comboWaktuMedis(String[] nilai,int tinggi){
            widget.ComboBox combo = new widget.ComboBox();
            combo.setModel(new javax.swing.DefaultComboBoxModel(nilai));
            combo.setPreferredSize(new java.awt.Dimension(62,tinggi));
            combo.setBackground(java.awt.Color.WHITE);
            combo.setBorder(javax.swing.BorderFactory.createLineBorder(
                    new java.awt.Color(0xD8,0xDB,0xD6)));
            combo.putClientProperty("Component.arc",0);
            return combo;
        }

        private widget.Label pemisahWaktuMedis(int tinggi){
            widget.Label pemisah = new widget.Label();
            pemisah.setText(":");
            pemisah.setPreferredSize(new java.awt.Dimension(4,tinggi));
            pemisah.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            return pemisah;
        }

        void setSekarang(){
            java.util.Calendar kalender = java.util.Calendar.getInstance();
            tanggal.setDate(kalender.getTime());
            jam.setSelectedItem(String.format("%02d",kalender.get(java.util.Calendar.HOUR_OF_DAY)));
            menit.setSelectedItem(String.format("%02d",kalender.get(java.util.Calendar.MINUTE)));
            detik.setSelectedItem(String.format("%02d",kalender.get(java.util.Calendar.SECOND)));
        }

        void setNilai(String tanggalSql,String jamSql){
            setSekarang();
            try{
                if(tanggalSql!=null && tanggalSql.trim().length()>=10){
                    tanggal.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd")
                            .parse(tanggalSql.trim().substring(0,10)));
                }
                if(jamSql!=null && jamSql.trim().length()>=5){
                    String nilai = jamSql.trim();
                    jam.setSelectedItem(nilai.substring(0,2));
                    menit.setSelectedItem(nilai.substring(3,5));
                    detik.setSelectedItem(nilai.length()>=8 ? nilai.substring(6,8) : "00");
                }
            }catch(Exception e){
                setSekarang();
            }
        }

        String getJamSql(){
            return jam.getSelectedItem()+":"+menit.getSelectedItem()+":"+detik.getSelectedItem();
        }

        String getTampil(){
            return tanggal.getSelectedItem()+" "+getJamSql();
        }

        @Override public void setEnabled(boolean aktif){
            super.setEnabled(aktif);
            tanggal.setEnabled(aktif); jam.setEnabled(aktif);
            menit.setEnabled(aktif); detik.setEnabled(aktif);
        }
    }

    /** Radio tampilan yang tetap memakai ComboBox lama sebagai sumber nilai. */
    private widget.PanelBiasa pilihanRadioLegacyMedis(final widget.ComboBox combo, String... pilihan){
        final javax.swing.ButtonGroup grup = new javax.swing.ButtonGroup();
        final widget.RadioButton[] radio = new widget.RadioButton[pilihan.length];
        // Ikuti panelPilihan() Survei Primer agar jarak radio konsisten dengan
        // Kesimpulan Jalan Napas; listener di bawah tetap menjadi adaptor combo legacy.
        widget.PanelBiasa panel = panelPilihan(grup);
        for(int i=0;i<pilihan.length;i++){
            final String nilai = pilihan[i];
            radio[i] = new widget.RadioButton();
            radio[i].setText(nilai);
            // Radio adaptor ini dibuat sesudah pasangChipSemuaMedis(), jadi
            // gayanya perlu dipasang langsung agar kotak tiap opsi sama
            // seperti Conjunctiva tanpa mengubah ComboBox sumber nilainya.
            pasangChipMedis(radio[i]);
            grup.add(radio[i]);
            panel.add(radio[i]);
            radio[i].addActionListener(new java.awt.event.ActionListener(){
                @Override public void actionPerformed(java.awt.event.ActionEvent evt){
                    Object terpilih = combo.getSelectedItem();
                    if(terpilih==null || !nilai.equals(terpilih.toString())){
                        combo.setSelectedItem(nilai);
                    }
                }
            });
        }
        combo.addActionListener(new java.awt.event.ActionListener(){
            @Override public void actionPerformed(java.awt.event.ActionEvent evt){
                Object terpilih = combo.getSelectedItem();
                if(terpilih==null){
                    grup.clearSelection();
                    return;
                }
                String nilai = terpilih.toString();
                for(widget.RadioButton r : radio){
                    if(nilai.equals(r.getText()) && !r.isSelected()){
                        r.setSelected(true);
                        return;
                    }
                }
            }
        });
        Object awal = combo.getSelectedItem();
        if(awal!=null){
            for(widget.RadioButton r : radio){
                if(awal.toString().equals(r.getText())){
                    r.setSelected(true);
                    break;
                }
            }
        }
        combo.setVisible(false);
        return panel;
    }

    // =========================================================================
    // TEMPLATE PEMERIKSAAN FISIK HEAD-TO-TOE (IGD)
    // Mengintegrasikan pemeriksaan Kepala, Leher, Thorax (Jantung & Paru), Abdomen, dll.
    // =========================================================================
    private static final String TEMPLATE_FISIK_NORMAL =
        "KEPALA : Normocephal, deformitas (-)\n" +
        "MATA : Konjungtiva anemis (-/-), sklera ikterik (-/-), pupil isokor Ø 3mm/3mm, RC (+/+)\n" +
        "THT : Sekret (-/-), perdarahan (-/-), deviasi septum (-)\n" +
        "MULUT : Mukosa bibir lembab, sianosis (-)\n" +
        "LEHER : Deviasi trakea (-), pembesaran KGB (-), JVP normal, tiroid tidak teraba\n" +
        "THORAX :\n" +
        "  - Cor : BJ I-II murni reguler, murmur (-), gallop (-)\n" +
        "  - Pulmo : Suara napas vesikuler (+/+), ronkhi (-/-), wheezing (-/-)\n" +
        "ABDOMEN : Datar, supel, bising usus (+) normal, nyeri tekan (-), timpani\n" +
        "EKSTREMITAS : Akral hangat, CRT < 2 detik, edema (-/-/-/-)";

    private static final String TEMPLATE_FISIK_TRAUMA =
        "KEPALA : Jejas (-), hematoma (-), deformitas (-)\n" +
        "MATA : Pupil isokor Ø 3mm/3mm, RC (+/+), racoon eyes (-)\n" +
        "THT : Otorrhea (-), rhinorrhea (-), battle sign (-)\n" +
        "LEHER : Jejas (-), deviasi trakea (-), hematoma (-), nyeri tekan (-)\n" +
        "THORAX : Bentuk simetris, jejas (-), krepitasi (-), vesikuler (+/+), ronkhi (-/-)\n" +
        "ABDOMEN : Datar, supel, jejas (-), defans muskular (-), nyeri tekan (-)\n" +
        "PELVIS : Stabilitas pelvis intak, krepitasi (-)\n" +
        "EKSTREMITAS : Deformitas (-), fraktur (-), akral hangat, pulsasi distal kuat, CRT < 2 detik";

    private static final String TEMPLATE_FISIK_ANAK =
        "KEPALA : Normocephal, ubun-ubun besar datar/tidak cekung\n" +
        "MATA : Konjungtiva anemis (-/-), sklera ikterik (-/-), mata cekung (-/-), air mata (+/+)\n" +
        "THT : Sekret (-/-), pernapasan cuping hidung (-)\n" +
        "MULUT : Mukosa bibir lembab, sianosis (-)\n" +
        "LEHER : Pembesaran KGB (-), retraksi suprasternal (-)\n" +
        "THORAX : Simetris, retraksi dinding dada (-), cor BJ I-II reguler, pulmo vesikuler (+/+), ronkhi (-/-), wheezing (-/-)\n" +
        "ABDOMEN : Supel, turgor kulit kembali cepat, bising usus (+) normal, meteorismus (-)\n" +
        "EKSTREMITAS : Akral hangat, CRT < 2 detik, sianosis (-)";

    private static final String TEMPLATE_FISIK_KOSONG =
        "KEPALA : \n" +
        "MATA : \n" +
        "THT : \n" +
        "MULUT : \n" +
        "LEHER : \n" +
        "THORAX :\n" +
        "  - Cor : \n" +
        "  - Pulmo : \n" +
        "ABDOMEN : \n" +
        "EKSTREMITAS : ";

    private void terapkanTemplateFisik(String templateTeks) {
        String existing = KetFisik.getText().trim();
        if (existing.isEmpty()) {
            KetFisik.setText(templateTeks);
            KetFisik.requestFocusInWindow();
            KetFisik.setCaretPosition(0);
            hitungProgresMedis();
            return;
        }

        Object[] options = {"Timpa (Replace)", "Sisipkan di Bawah (Append)", "Batal"};
        int pilihan = javax.swing.JOptionPane.showOptionDialog(
            this,
            "Kolom Pemeriksaan Fisik sudah berisi teks.\n" +
            "Apakah Anda ingin menimpa teks yang ada atau menyisipkan template di baris bawah?",
            "Konfirmasi Template Pemeriksaan Fisik",
            javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]
        );

        if (pilihan == 0) { // Timpa
            KetFisik.setText(templateTeks);
            KetFisik.requestFocusInWindow();
            KetFisik.setCaretPosition(0);
            hitungProgresMedis();
        } else if (pilihan == 1) { // Sisipkan di bawah
            KetFisik.setText(existing + "\n\n" + templateTeks);
            KetFisik.requestFocusInWindow();
            KetFisik.setCaretPosition(KetFisik.getText().length());
            hitungProgresMedis();
        }
    }

    private void simpanTemplateDokterKustom() {
        String teks = KetFisik.getText().trim();
        if (teks.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Tuliskan teks pemeriksaan fisik di kolom terlebih dahulu sebelum menyimpannya sebagai template kustom.",
                "Pemberitahuan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String kd = KdDokter.getText().trim();
        String key = "tpl_fisik_igd_" + (kd.isEmpty() ? "default" : kd);
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(RMPenilaianAwalMedisIGD.class);
        prefs.put(key, teks);
        javax.swing.JOptionPane.showMessageDialog(this,
            "Template kustom berhasil disimpan" + (!kd.isEmpty() ? " untuk dokter: " + NmDokter.getText() : "") + "!",
            "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private void terapkanTemplateDokterKustom() {
        String kd = KdDokter.getText().trim();
        String key = "tpl_fisik_igd_" + (kd.isEmpty() ? "default" : kd);
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(RMPenilaianAwalMedisIGD.class);
        String saved = prefs.get(key, null);
        if (saved == null || saved.trim().isEmpty()) {
            int tanya = javax.swing.JOptionPane.showConfirmDialog(this,
                "Belum ada template kustom yang tersimpan untuk dokter ini.\nApakah ingin menggunakan Template Normal Dewasa?",
                "Template Kustom Belum Ada", javax.swing.JOptionPane.YES_NO_OPTION);
            if (tanya == javax.swing.JOptionPane.YES_OPTION) {
                terapkanTemplateFisik(TEMPLATE_FISIK_NORMAL);
            }
            return;
        }
        terapkanTemplateFisik(saved);
    }

    private DlgMasterTemplatePemeriksaanFisik dlgTemplateFisik;
    private void bukaDialogTemplateFisik() {
        if (dlgTemplateFisik == null) {
            dlgTemplateFisik = new DlgMasterTemplatePemeriksaanFisik(null, true);
        }
        dlgTemplateFisik.setDokter(KdDokter.getText(), NmDokter.getText());
        dlgTemplateFisik.setListener((nama, teksFisik) -> {
            if (teksFisik != null && !teksFisik.trim().isEmpty()) {
                terapkanTemplateFisik(teksFisik);
            }
        });
        dlgTemplateFisik.setLocationRelativeTo(this);
        dlgTemplateFisik.setVisible(true);
    }

    private void pastikanTabelTemplateFisikAda() {
        try {
            PreparedStatement psCreate = koneksi.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `template_pemeriksaan_fisik_dokter` (" +
                "  `no_template` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `kd_dokter` VARCHAR(20) NOT NULL DEFAULT '-'," +
                "  `nama_template` VARCHAR(100) NOT NULL," +
                "  `pemeriksaan_fisik` TEXT NOT NULL," +
                "  INDEX(`kd_dokter`)" +
                ") ENGINE=InnoDB;"
            );
            psCreate.execute();
            psCreate.close();
        } catch (Exception e) {
            System.out.println("Notif pastikan tabel template fisik: " + e);
        }
    }

    private void simpanTeksSebagaiTemplateBaru() {
        String teks = KetFisik.getText().trim();
        if (teks.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Tuliskan format pemeriksaan fisik di kolom terlebih dahulu sebelum menyimpannya ke Master Template.",
                "Pemberitahuan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kdDok = KdDokter.getText().trim();
        String nmDok = NmDokter.getText().trim();

        javax.swing.JTextField txtNamaBaru = new javax.swing.JTextField();
        txtNamaBaru.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 12));

        javax.swing.JComboBox<String> cmbTipe = new javax.swing.JComboBox<>();
        if (!kdDok.isEmpty()) {
            cmbTipe.addItem("Template Pribadi Dokter (" + (!nmDok.isEmpty() ? nmDok : kdDok) + ")");
            cmbTipe.addItem("Template Semua (Umum RS)");
        } else {
            cmbTipe.addItem("Template Semua (Umum RS)");
        }

        javax.swing.JPanel panelInput = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 6));
        panelInput.add(new javax.swing.JLabel("Nama Template Baru (Contoh: Template Status Lokalis Bedah):"));
        panelInput.add(txtNamaBaru);
        panelInput.add(new javax.swing.JLabel("Kategori Kepemilikan:"));
        panelInput.add(cmbTipe);

        int res = javax.swing.JOptionPane.showConfirmDialog(this, panelInput,
            "Simpan ke Master Template Pemeriksaan Fisik",
            javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);

        if (res == javax.swing.JOptionPane.OK_OPTION) {
            String nama = txtNamaBaru.getText().trim();
            if (nama.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Nama template tidak boleh kosong!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean isPribadi = cmbTipe.getSelectedItem() != null && cmbTipe.getSelectedItem().toString().startsWith("Template Pribadi");
            String pemilik = isPribadi ? kdDok : "-";

            try {
                pastikanTabelTemplateFisikAda();
                PreparedStatement ps = koneksi.prepareStatement(
                    "insert into template_pemeriksaan_fisik_dokter(kd_dokter,nama_template,pemeriksaan_fisik) values(?,?,?)"
                );
                ps.setString(1, pemilik);
                ps.setString(2, nama);
                ps.setString(3, teks);
                ps.executeUpdate();
                ps.close();

                javax.swing.JOptionPane.showMessageDialog(this,
                    "Template '" + nama + "' berhasil disimpan ke Master Template (" + (isPribadi ? "Pribadi Dokter" : "Umum RS") + ")!\n" +
                    "Template ini langsung otomatis tersedia di menu Template Cepat ▼.",
                    "Sukses", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                System.out.println("Notif simpan template baru dari form: " + ex);
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyimpan template: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void tampilkanMenuTemplateFisik(javax.swing.JComponent anchor) {
        pastikanTabelTemplateFisikAda();
        javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();

        // 1. Menu Kelola Master Template
        javax.swing.JMenuItem itemKelola = new javax.swing.JMenuItem("Kelola Master Template Pemeriksaan Fisik...");
        itemKelola.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        itemKelola.setForeground(new java.awt.Color(15, 61, 62));
        itemKelola.setIcon(new DlgMasterTemplatePemeriksaanFisik.ArgonVectorIcon(
            DlgMasterTemplatePemeriksaanFisik.ArgonVectorIcon.Type.CHIP_SAVE, 13, new java.awt.Color(15, 61, 62)));
        itemKelola.addActionListener(e -> bukaDialogTemplateFisik());
        popup.add(itemKelola);
        popup.addSeparator();

        String kdDok = KdDokter.getText().trim();
        String nmDok = NmDokter.getText().trim();
        boolean adaPribadi = false;
        boolean adaUmum = false;

        // 2. Ambil Template Pribadi Dokter dari Database
        if (!kdDok.isEmpty()) {
            try {
                PreparedStatement ps = koneksi.prepareStatement(
                    "select no_template, nama_template, pemeriksaan_fisik from template_pemeriksaan_fisik_dokter " +
                    "where kd_dokter=? order by nama_template asc"
                );
                ps.setString(1, kdDok);
                ResultSet rs = ps.executeQuery();
                java.util.List<javax.swing.JMenuItem> listPribadi = new java.util.ArrayList<>();
                while (rs.next()) {
                    String nama = rs.getString("nama_template");
                    String isi = rs.getString("pemeriksaan_fisik");
                    javax.swing.JMenuItem item = new javax.swing.JMenuItem("   *  " + nama);
                    item.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
                    item.setForeground(new java.awt.Color(29, 78, 216));
                    item.addActionListener(e -> terapkanTemplateFisik(isi));
                    listPribadi.add(item);
                }
                rs.close();
                ps.close();

                if (!listPribadi.isEmpty()) {
                    adaPribadi = true;
                    javax.swing.JLabel lblHeaderPribadi = new javax.swing.JLabel("  TEMPLATE PRIBADI SAYA (" + (!nmDok.isEmpty() ? nmDok : kdDok) + ")");
                    lblHeaderPribadi.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
                    lblHeaderPribadi.setForeground(new java.awt.Color(100, 116, 139));
                    popup.add(lblHeaderPribadi);
                    for (javax.swing.JMenuItem mi : listPribadi) {
                        popup.add(mi);
                    }
                    popup.addSeparator();
                }
            } catch (Exception e) {
                System.out.println("Notif load template pribadi: " + e);
            }
        }

        // 3. Ambil Template Umum RS dari Database
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "select no_template, nama_template, pemeriksaan_fisik from template_pemeriksaan_fisik_dokter " +
                "where kd_dokter='-' order by nama_template asc"
            );
            ResultSet rs = ps.executeQuery();
            java.util.List<javax.swing.JMenuItem> listUmum = new java.util.ArrayList<>();
            while (rs.next()) {
                String nama = rs.getString("nama_template");
                String isi = rs.getString("pemeriksaan_fisik");
                javax.swing.JMenuItem item = new javax.swing.JMenuItem(nama);
                item.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
                item.addActionListener(e -> terapkanTemplateFisik(isi));
                listUmum.add(item);
            }
            rs.close();
            ps.close();

            if (!listUmum.isEmpty()) {
                adaUmum = true;
                javax.swing.JLabel lblHeaderUmum = new javax.swing.JLabel("  TEMPLATE UMUM RS");
                lblHeaderUmum.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
                lblHeaderUmum.setForeground(new java.awt.Color(100, 116, 139));
                popup.add(lblHeaderUmum);
                for (javax.swing.JMenuItem mi : listUmum) {
                    popup.add(mi);
                }
                popup.addSeparator();
            }
        } catch (Exception e) {
            System.out.println("Notif load template umum: " + e);
        }

        // Fallback jika database belum ada isinya sama sekali
        if (!adaPribadi && !adaUmum) {
            javax.swing.JMenuItem itemNormal = new javax.swing.JMenuItem("Template Normal Dewasa (Head-to-Toe)");
            itemNormal.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
            itemNormal.addActionListener(e -> terapkanTemplateFisik(TEMPLATE_FISIK_NORMAL));

            javax.swing.JMenuItem itemTrauma = new javax.swing.JMenuItem("Template Kasus Trauma / Bedah");
            itemTrauma.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
            itemTrauma.addActionListener(e -> terapkanTemplateFisik(TEMPLATE_FISIK_TRAUMA));

            javax.swing.JMenuItem itemAnak = new javax.swing.JMenuItem("Template Pediatrik / Anak");
            itemAnak.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
            itemAnak.addActionListener(e -> terapkanTemplateFisik(TEMPLATE_FISIK_ANAK));

            javax.swing.JMenuItem itemKosong = new javax.swing.JMenuItem("Kerangka / Format Kosong (Head-to-Toe)");
            itemKosong.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
            itemKosong.addActionListener(e -> terapkanTemplateFisik(TEMPLATE_FISIK_KOSONG));

            popup.add(itemNormal);
            popup.add(itemTrauma);
            popup.add(itemAnak);
            popup.add(itemKosong);
            popup.addSeparator();
        }

        // 4. Aksi Tambah Cepat ke Master Template
        javax.swing.JMenuItem itemSimpan = new javax.swing.JMenuItem("Simpan Teks Ini Jadi Template Baru di Master...");
        itemSimpan.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        itemSimpan.setForeground(new java.awt.Color(15, 61, 62));
        itemSimpan.setIcon(new DlgMasterTemplatePemeriksaanFisik.ArgonVectorIcon(
            DlgMasterTemplatePemeriksaanFisik.ArgonVectorIcon.Type.CHIP_ADD, 12, new java.awt.Color(15, 61, 62)));
        itemSimpan.addActionListener(e -> simpanTeksSebagaiTemplateBaru());
        popup.add(itemSimpan);

        popup.addSeparator();

        // 5. Aksi Kosongkan Kolom
        javax.swing.JMenuItem itemBersihkan = new javax.swing.JMenuItem("Kosongkan Kolom Pemeriksaan Fisik");
        itemBersihkan.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        itemBersihkan.setForeground(new java.awt.Color(185, 28, 28));
        itemBersihkan.setIcon(new DlgMasterTemplatePemeriksaanFisik.ArgonVectorIcon(
            DlgMasterTemplatePemeriksaanFisik.ArgonVectorIcon.Type.HAPUS, 12, new java.awt.Color(185, 28, 28)));
        itemBersihkan.addActionListener(e -> {
            if (!KetFisik.getText().trim().isEmpty()) {
                int c = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Kosongkan isi kolom pemeriksaan fisik?",
                    "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
                if (c == javax.swing.JOptionPane.YES_OPTION) {
                    KetFisik.setText("");
                    KetFisik.requestFocusInWindow();
                    hitungProgresMedis();
                }
            }
        });
        popup.add(itemBersihkan);

        popup.show(anchor, 0, anchor.getHeight());
    }

    private widget.PanelBiasa siapkanAreaPemeriksaanFisikMedis(){
        widget.PanelBiasa panel = new widget.PanelBiasa(new java.awt.BorderLayout(0, 8));
        panel.setOpaque(false);

        widget.PanelBiasa toolbar = new widget.PanelBiasa(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 2));
        toolbar.setOpaque(false);

        // 1. Tombol Master Template: Button bergaya Argon Outline (selaras dengan Template Cepat)
        javax.swing.JButton btnMaster = new javax.swing.JButton("Master Template Pemeriksaan Fisik") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new java.awt.Color(204, 251, 241));
                } else if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(240, 253, 250));
                } else {
                    g2.setColor(java.awt.Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                // Border tegas
                if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(13, 148, 136));
                    g2.setStroke(new java.awt.BasicStroke(1.5f));
                } else {
                    g2.setColor(new java.awt.Color(15, 61, 62));
                    g2.setStroke(new java.awt.BasicStroke(1.2f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnMaster.setContentAreaFilled(false);
        btnMaster.setFocusPainted(false);
        btnMaster.setBorderPainted(false);
        btnMaster.setOpaque(false);
        btnMaster.setForeground(new java.awt.Color(15, 61, 62));
        btnMaster.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        btnMaster.setIcon(new DlgMasterTemplatePemeriksaanFisik.ArgonVectorIcon(
            DlgMasterTemplatePemeriksaanFisik.ArgonVectorIcon.Type.CHIP_SAVE, 14, new java.awt.Color(15, 61, 62)));
        btnMaster.setIconTextGap(8);
        btnMaster.setToolTipText("Buka Form Master Template Pemeriksaan Fisik: Buat, ubah, hapus, cari, dan kelola template");
        btnMaster.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMaster.setPreferredSize(new java.awt.Dimension(265, 30));
        btnMaster.addActionListener(e -> bukaDialogTemplateFisik());

        // 2. Tombol Template Cepat: Button bergaya Argon Outline / Badge Pill
        javax.swing.JButton btnMenu = new javax.swing.JButton("Template Cepat  ▼") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new java.awt.Color(204, 251, 241));
                } else if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(240, 253, 250));
                } else {
                    g2.setColor(java.awt.Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                // Border tegas
                if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(13, 148, 136));
                    g2.setStroke(new java.awt.BasicStroke(1.5f));
                } else {
                    g2.setColor(new java.awt.Color(15, 61, 62));
                    g2.setStroke(new java.awt.BasicStroke(1.2f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnMenu.setContentAreaFilled(false);
        btnMenu.setFocusPainted(false);
        btnMenu.setBorderPainted(false);
        btnMenu.setOpaque(false);
        btnMenu.setForeground(new java.awt.Color(15, 61, 62));
        btnMenu.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        btnMenu.setIcon(new DlgMasterTemplatePemeriksaanFisik.ArgonVectorIcon(
            DlgMasterTemplatePemeriksaanFisik.ArgonVectorIcon.Type.CHIP_ADD, 13, new java.awt.Color(15, 61, 62)));
        btnMenu.setIconTextGap(7);
        btnMenu.setToolTipText("Pilih cepat template pemeriksaan fisik dari database");
        btnMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenu.setPreferredSize(new java.awt.Dimension(160, 30));
        btnMenu.addActionListener(e -> tampilkanMenuTemplateFisik(btnMenu));

        toolbar.add(btnMaster);
        toolbar.add(btnMenu);

        scrollPane5.setPreferredSize(new java.awt.Dimension(640, 160));
        scrollPane5.setMinimumSize(new java.awt.Dimension(640, 140));

        panel.add(toolbar, java.awt.BorderLayout.NORTH);
        panel.add(scrollPane5, java.awt.BorderLayout.CENTER);
        panel.setPreferredSize(new java.awt.Dimension(640, 204));
        panel.setMinimumSize(new java.awt.Dimension(640, 175));

        return panel;
    }

    /* =========================================================================
     *  TEMPLATE RENCANA / TINDAKAN / TERAPI MEDIS IGD
     * ========================================================================= */

    private static final String TEMPLATE_TERAPI_UMUM =
        "1. MONITORING & OBSERVASI:\n" +
        "   - Observasi tanda vital dan keadaan umum per 15-30 menit\n" +
        "   - Pasang monitor SpO2 / EKG\n" +
        "2. NON-FARMAKOLOGI:\n" +
        "   - Tirah baring (bed rest)\n" +
        "   - Oksigenasi via nasal kanul 2-4 lpm bila SpO2 < 95%\n" +
        "   - Pasang jalur IV line jarum 20G\n" +
        "3. FARMAKOLOGI:\n" +
        "   - Infus Cairan: RL / NaCl 0.9% 500 cc / 8 jam (20 tpm)\n" +
        "   - Injeksi Ranitidine 50 mg / Ondansetron 4 mg IV (bila mual/muntah)\n" +
        "   - Injeksi Ketorolac 30 mg / Paracetamol drip 1 gr IV (bila nyeri/demam)\n" +
        "4. RENCANA LANJUTAN:\n" +
        "   - Evaluasi hasil pemeriksaan laboratorium / radiologi\n" +
        "   - Lapor DPJP untuk advis lanjutan & rencana rawat inap";

    private static final String TEMPLATE_TERAPI_TRAUMA =
        "1. RESUSITASI & STABILISASI (ABCDE):\n" +
        "   - Airway & C-Spine control (pasang cervical collar bila curiga cedera servikal)\n" +
        "   - Oksigenasi via NRM 10-15 lpm bila sesak / saturasi turun\n" +
        "   - Pasang 2 jalur IV line jarum besar (16G/18G), guyur RL / NaCl 0.9% bila syok hipovolemik\n" +
        "2. TINDAKAN BEDAH EMERGENCY:\n" +
        "   - Wound toilet / pencucian dan debridement luka\n" +
        "   - Hecting / jahit situasi & balut tekan (pressure dressing) bila perdarahan aktif\n" +
        "   - Fiksasi / pasang bidai (splint) pada kecurigaan fraktur\n" +
        "3. MEDIKAMENTOSA:\n" +
        "   - ATS 1500 IU IM (setelah skin test) / TT 0.5 cc IM\n" +
        "   - Injeksi Ceftriaxone 1 gr IV (profilaksis luka kotor)\n" +
        "   - Injeksi Ketorolac 30 mg IV / Tramadol 50 mg drip\n" +
        "4. RENCANA TINDAK LANJUT:\n" +
        "   - Rontgen area cedera / Foto Thorax & Pelvis AP\n" +
        "   - Konsul CITO Dokter Spesialis Bedah (DPJP) untuk tatalaksana definitif";

    private static final String TEMPLATE_TERAPI_JANTUNG =
        "1. TATALAKSANA AWAL ACS:\n" +
        "   - Tirah baring total, posisi semi fowler nyaman\n" +
        "   - Oksigenasi bila SpO2 < 90%\n" +
        "   - Pasang IV line NaCl 0.9% maintenance (keep vein open)\n" +
        "2. TERAPI FARMAKOLOGI (MONA / SAT):\n" +
        "   - ISDN 5 mg sublingual (dapat diulang tiap 5 mnt max 3x bila tensi > 90 mmHg)\n" +
        "   - Aspilet 160-320 mg kunyah (loading dose)\n" +
        "   - Clopidogrel 300 mg PO (loading dose)\n" +
        "3. RENCANA PEMERIKSAAN & KONSUL:\n" +
        "   - Rekam serial EKG 12 lead per 15-30 menit\n" +
        "   - Cek Troponin I/T, CK-MB, elektrolit cito\n" +
        "   - Konsul CITO Dokter Spesialis Jantung (Sp.JP) / persiapan rujukan ICCU";

    private static final String TEMPLATE_TERAPI_RESPIRASI =
        "1. TATALAKSANA RESPIRASI:\n" +
        "   - Posisi fowler / tegak\n" +
        "   - Oksigenasi 2-4 lpm via kanul (titrasi SpO2 94-98% atau 88-92% pada PPOK)\n" +
        "2. NEBULISASI & MEDIKAMENTOSA:\n" +
        "   - Nebulisasi Combivent 1 respule + Pulmicort 1 respule (bila perlu)\n" +
        "   - Injeksi Deksametason 5 mg IV atau Metilprednisolon 62.5 mg IV\n" +
        "   - Injeksi Aminofilin drip (bila diinstruksikan DPJP)\n" +
        "3. RENCANA LANJUTAN:\n" +
        "   - Evaluasi keluhan wheezing dan retraksi otot napas pasca nebulisasi\n" +
        "   - Foto Thorax AP/PA";

    private static final String TEMPLATE_TERAPI_ANAK =
        "1. STABILISASI & NON-FARMAKOLOGI:\n" +
        "   - Bebaskan jalan napas, longgarkan pakaian ketat\n" +
        "   - Oksigenasi 1-2 lpm kanul nasal\n" +
        "   - Kompres hangat bila demam tinggi\n" +
        "2. FARMAKOLOGI:\n" +
        "   - Paracetamol drop/sirup 10-15 mg/kgBB PO atau rectal bila kejang demam\n" +
        "   - Bila kejang aktif: Diazepam rectal 5 mg (<10 kg) / 10 mg (>10 kg)\n" +
        "   - Rehidrasi cairan sesuai derajat dehidrasi (KaEN 1B / RL mikro drip)\n" +
        "3. RENCANA:\n" +
        "   - Pemeriksaan DL, GDS, Elektrolit cito\n" +
        "   - Konsul Dokter Spesialis Anak (Sp.A) untuk advis terapi rawat";

    private static final String TEMPLATE_TERAPI_KOSONG =
        "1. MONITORING & OBSERVASI :\n" +
        "   - \n" +
        "2. TINDAKAN MEDIS & KEPERAWATAN :\n" +
        "   - \n" +
        "3. TERAPI CAIRAN & MEDIKAMENTOSA :\n" +
        "   - Cairan Infus : \n" +
        "   - Injeksi : \n" +
        "   - Obat Oral : \n" +
        "4. EDUKASI PASIEN / KELUARGA :\n" +
        "   - \n" +
        "5. RENCANA TINDAK LANJUT :\n" +
        "   - Konsul DPJP / Rencana Rawat Inap / Rawat Jalan";

    private DlgMasterTemplateRencanaTerapi dlgTemplateRencanaTerapi;
    private void bukaDialogTemplateRencanaTerapi() {
        if (dlgTemplateRencanaTerapi == null) {
            dlgTemplateRencanaTerapi = new DlgMasterTemplateRencanaTerapi(null, true);
        }
        dlgTemplateRencanaTerapi.setDokter(KdDokter.getText(), NmDokter.getText());
        dlgTemplateRencanaTerapi.setTemplateListener((nama, isi) -> {
            if (isi != null && !isi.trim().isEmpty()) {
                terapkanTemplateRencanaTerapi(isi);
            }
        });
        dlgTemplateRencanaTerapi.setLocationRelativeTo(this);
        dlgTemplateRencanaTerapi.setVisible(true);
    }

    private void pastikanTabelTemplateRencanaTerapiAda() {
        try {
            PreparedStatement psCreate = koneksi.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `template_rencana_terapi_dokter` (" +
                "  `no_template` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `kd_dokter` VARCHAR(20) NOT NULL DEFAULT '-'," +
                "  `nama_template` VARCHAR(100) NOT NULL," +
                "  `rencana_terapi` TEXT NOT NULL," +
                "  INDEX(`kd_dokter`)" +
                ") ENGINE=InnoDB;"
            );
            psCreate.execute();
            psCreate.close();
        } catch (Exception e) {
            System.out.println("Notif pastikan tabel template rencana terapi: " + e);
        }
    }

    private void simpanTeksSebagaiTemplateRencanaTerapiBaru() {
        String teks = Tatalaksana.getText().trim();
        if (teks.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Tuliskan format rencana / tindakan / terapi di kolom terlebih dahulu sebelum menyimpannya ke Master Template.",
                "Pemberitahuan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kdDok = KdDokter.getText().trim();
        String nmDok = NmDokter.getText().trim();

        javax.swing.JTextField txtNamaBaru = new javax.swing.JTextField();
        txtNamaBaru.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 12));

        javax.swing.JComboBox<String> cmbTipe = new javax.swing.JComboBox<>();
        if (!kdDok.isEmpty()) {
            cmbTipe.addItem("Template Pribadi Dokter (" + (!nmDok.isEmpty() ? nmDok : kdDok) + ")");
            cmbTipe.addItem("Template Semua (Umum RS)");
        } else {
            cmbTipe.addItem("Template Semua (Umum RS)");
        }

        javax.swing.JPanel panelInput = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 6));
        panelInput.add(new javax.swing.JLabel("Nama Template Baru (Contoh: Protokol Terapi Asma Akut):"));
        panelInput.add(txtNamaBaru);
        panelInput.add(new javax.swing.JLabel("Kategori Kepemilikan:"));
        panelInput.add(cmbTipe);

        int res = javax.swing.JOptionPane.showConfirmDialog(this, panelInput,
            "Simpan ke Master Template Rencana & Terapi",
            javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);

        if (res == javax.swing.JOptionPane.OK_OPTION) {
            String nama = txtNamaBaru.getText().trim();
            if (nama.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Nama template tidak boleh kosong!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean isPribadi = cmbTipe.getSelectedItem() != null && cmbTipe.getSelectedItem().toString().startsWith("Template Pribadi");
            String pemilik = isPribadi ? kdDok : "-";

            try {
                pastikanTabelTemplateRencanaTerapiAda();
                PreparedStatement ps = koneksi.prepareStatement(
                    "insert into template_rencana_terapi_dokter(kd_dokter,nama_template,rencana_terapi) values(?,?,?)"
                );
                ps.setString(1, pemilik);
                ps.setString(2, nama);
                ps.setString(3, teks);
                ps.executeUpdate();
                ps.close();

                javax.swing.JOptionPane.showMessageDialog(this,
                    "Template '" + nama + "' berhasil disimpan ke Master Template (" + (isPribadi ? "Pribadi Dokter" : "Umum RS") + ")!\n" +
                    "Template ini langsung otomatis tersedia di menu Template Cepat ▼.",
                    "Sukses", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                System.out.println("Notif simpan template baru rencana terapi dari form: " + ex);
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyimpan template: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void terapkanTemplateRencanaTerapi(String templateTeks) {
        String existing = Tatalaksana.getText().trim();
        if (existing.isEmpty()) {
            Tatalaksana.setText(templateTeks);
            Tatalaksana.requestFocusInWindow();
            Tatalaksana.setCaretPosition(0);
            hitungProgresMedis();
            return;
        }

        Object[] options = {"Timpa (Replace)", "Sisipkan di Bawah (Append)", "Batal"};
        int pilihan = javax.swing.JOptionPane.showOptionDialog(
            this,
            "Kolom Rencana / Tindakan / Terapi sudah berisi teks.\n" +
            "Apakah Anda ingin menimpa teks yang ada atau menyisipkan template di baris bawah?",
            "Konfirmasi Template Terapi",
            javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]
        );

        if (pilihan == 0) { // Timpa
            Tatalaksana.setText(templateTeks);
            Tatalaksana.requestFocusInWindow();
            Tatalaksana.setCaretPosition(0);
            hitungProgresMedis();
        } else if (pilihan == 1) { // Sisipkan di bawah
            Tatalaksana.setText(existing + "\n\n" + templateTeks);
            Tatalaksana.requestFocusInWindow();
            Tatalaksana.setCaretPosition(Tatalaksana.getText().length());
            hitungProgresMedis();
        }
    }

    private void tampilkanMenuTemplateRencanaTerapi(javax.swing.JComponent anchor) {
        pastikanTabelTemplateRencanaTerapiAda();
        javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();

        // 1. Menu Kelola Master Template
        javax.swing.JMenuItem itemKelola = new javax.swing.JMenuItem("Kelola Master Template Rencana & Terapi...");
        itemKelola.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        itemKelola.setForeground(new java.awt.Color(15, 61, 62));
        itemKelola.setIcon(new DlgMasterTemplateRencanaTerapi.ArgonVectorIcon(
            DlgMasterTemplateRencanaTerapi.ArgonVectorIcon.Type.CHIP_SAVE, 13, new java.awt.Color(15, 61, 62)));
        itemKelola.addActionListener(e -> bukaDialogTemplateRencanaTerapi());
        popup.add(itemKelola);
        popup.addSeparator();

        String kdDok = KdDokter.getText().trim();
        String nmDok = NmDokter.getText().trim();
        boolean adaPribadi = false;
        boolean adaUmum = false;

        // 2. Ambil Template Pribadi Dokter dari Database
        if (!kdDok.isEmpty()) {
            try {
                PreparedStatement ps = koneksi.prepareStatement(
                    "select no_template, nama_template, rencana_terapi from template_rencana_terapi_dokter " +
                    "where kd_dokter=? order by nama_template asc"
                );
                ps.setString(1, kdDok);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (!adaPribadi) {
                        javax.swing.JLabel lblHeaderPribadi = new javax.swing.JLabel("  TEMPLATE SAYA (" + (!nmDok.isEmpty() ? nmDok : kdDok) + ") :");
                        lblHeaderPribadi.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
                        lblHeaderPribadi.setForeground(new java.awt.Color(29, 78, 216)); // Biru
                        popup.add(lblHeaderPribadi);
                        adaPribadi = true;
                    }
                    String nama = rs.getString("nama_template");
                    String isi = rs.getString("rencana_terapi");
                    javax.swing.JMenuItem itm = new javax.swing.JMenuItem("   *  " + nama);
                    itm.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
                    itm.setForeground(new java.awt.Color(30, 41, 59));
                    itm.setToolTipText("<html><body style='width:320px; font-size:10px;'>" + isi.replace("\n", "<br>") + "</body></html>");
                    itm.addActionListener(e -> terapkanTemplateRencanaTerapi(isi));
                    popup.add(itm);
                }
                rs.close();
                ps.close();
            } catch (Exception ex) {
                System.out.println("Notif load template pribadi rencana terapi: " + ex);
            }
        }

        if (adaPribadi) {
            popup.addSeparator();
        }

        // 3. Ambil Template Umum RS dari Database
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "select no_template, nama_template, rencana_terapi from template_rencana_terapi_dokter " +
                "where kd_dokter='-' order by nama_template asc"
            );
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!adaUmum) {
                    javax.swing.JLabel lblHeaderUmum = new javax.swing.JLabel("  TEMPLATE STANDAR RS (UMUM) :");
                    lblHeaderUmum.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
                    lblHeaderUmum.setForeground(new java.awt.Color(71, 85, 105)); // Slate
                    popup.add(lblHeaderUmum);
                    adaUmum = true;
                }
                String nama = rs.getString("nama_template");
                String isi = rs.getString("rencana_terapi");
                javax.swing.JMenuItem itm = new javax.swing.JMenuItem("   -  " + nama);
                itm.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
                itm.setToolTipText("<html><body style='width:320px; font-size:10px;'>" + isi.replace("\n", "<br>") + "</body></html>");
                itm.addActionListener(e -> terapkanTemplateRencanaTerapi(isi));
                popup.add(itm);
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            System.out.println("Notif load template umum rencana terapi: " + ex);
        }

        // 4. Jika Database Masih Kosong, Berikan Template Bawaan
        if (!adaUmum && !adaPribadi) {
            javax.swing.JMenuItem itemUmum = new javax.swing.JMenuItem("Template Terapi Umum / Simptomatik (Dewasa)");
            itemUmum.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
            itemUmum.addActionListener(e -> terapkanTemplateRencanaTerapi(TEMPLATE_TERAPI_UMUM));

            javax.swing.JMenuItem itemTrauma = new javax.swing.JMenuItem("Template Kasus Trauma / Bedah (Emergency)");
            itemTrauma.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
            itemTrauma.addActionListener(e -> terapkanTemplateRencanaTerapi(TEMPLATE_TERAPI_TRAUMA));

            javax.swing.JMenuItem itemJantung = new javax.swing.JMenuItem("Template Kardiovaskular (Sindrom Koroner Akut)");
            itemJantung.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
            itemJantung.addActionListener(e -> terapkanTemplateRencanaTerapi(TEMPLATE_TERAPI_JANTUNG));

            javax.swing.JMenuItem itemRespirasi = new javax.swing.JMenuItem("Template Respirasi / Asma Akut / PPOK");
            itemRespirasi.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
            itemRespirasi.addActionListener(e -> terapkanTemplateRencanaTerapi(TEMPLATE_TERAPI_RESPIRASI));

            javax.swing.JMenuItem itemAnak = new javax.swing.JMenuItem("Template Pediatrik / Anak (Febris / Kejang)");
            itemAnak.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
            itemAnak.addActionListener(e -> terapkanTemplateRencanaTerapi(TEMPLATE_TERAPI_ANAK));

            javax.swing.JMenuItem itemKosong = new javax.swing.JMenuItem("Kerangka / Format Terstruktur Kosong");
            itemKosong.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
            itemKosong.addActionListener(e -> terapkanTemplateRencanaTerapi(TEMPLATE_TERAPI_KOSONG));

            popup.add(itemUmum);
            popup.add(itemTrauma);
            popup.add(itemJantung);
            popup.add(itemRespirasi);
            popup.add(itemAnak);
            popup.add(itemKosong);
        }

        popup.addSeparator();

        // 5. Menu Simpan Teks Yang Ada Menjadi Template Baru
        javax.swing.JMenuItem itemSimpan = new javax.swing.JMenuItem("Simpan Teks Ini Sebagai Template Baru...");
        itemSimpan.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        itemSimpan.setForeground(new java.awt.Color(15, 61, 62));
        itemSimpan.setIcon(new DlgMasterTemplateRencanaTerapi.ArgonVectorIcon(
            DlgMasterTemplateRencanaTerapi.ArgonVectorIcon.Type.CHIP_ADD, 12, new java.awt.Color(15, 61, 62)));
        itemSimpan.addActionListener(e -> simpanTeksSebagaiTemplateRencanaTerapiBaru());
        popup.add(itemSimpan);

        // 6. Menu Kosongkan
        javax.swing.JMenuItem itemBersihkan = new javax.swing.JMenuItem("Kosongkan Kolom Rencana / Terapi");
        itemBersihkan.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        itemBersihkan.setForeground(new java.awt.Color(185, 28, 28));
        itemBersihkan.setIcon(new DlgMasterTemplateRencanaTerapi.ArgonVectorIcon(
            DlgMasterTemplateRencanaTerapi.ArgonVectorIcon.Type.HAPUS, 12, new java.awt.Color(185, 28, 28)));
        itemBersihkan.addActionListener(e -> {
            if (!Tatalaksana.getText().trim().isEmpty()) {
                int c = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Kosongkan isi kolom rencana / tindakan / terapi?",
                    "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
                if (c == javax.swing.JOptionPane.YES_OPTION) {
                    Tatalaksana.setText("");
                    Tatalaksana.requestFocusInWindow();
                    hitungProgresMedis();
                }
            }
        });
        popup.add(itemBersihkan);

        popup.show(anchor, 0, anchor.getHeight());
    }

    private widget.PanelBiasa siapkanAreaRencanaTerapiMedis(){
        widget.PanelBiasa panel = new widget.PanelBiasa(new java.awt.BorderLayout(0, 6));
        panel.setOpaque(false);

        widget.PanelBiasa toolbar = new widget.PanelBiasa(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        // 1. Tombol Master Template: Button bergaya Argon Outline / Badge Pill
        javax.swing.JButton btnMaster = new javax.swing.JButton("Master Template Rencana & Terapi") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new java.awt.Color(204, 251, 241));
                } else if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(240, 253, 250));
                } else {
                    g2.setColor(java.awt.Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                // Border tegas
                if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(13, 148, 136));
                    g2.setStroke(new java.awt.BasicStroke(1.5f));
                } else {
                    g2.setColor(new java.awt.Color(15, 61, 62));
                    g2.setStroke(new java.awt.BasicStroke(1.2f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnMaster.setContentAreaFilled(false);
        btnMaster.setFocusPainted(false);
        btnMaster.setBorderPainted(false);
        btnMaster.setOpaque(false);
        btnMaster.setForeground(new java.awt.Color(15, 61, 62));
        btnMaster.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        btnMaster.setIcon(new DlgMasterTemplateRencanaTerapi.ArgonVectorIcon(
            DlgMasterTemplateRencanaTerapi.ArgonVectorIcon.Type.CHIP_SAVE, 14, new java.awt.Color(15, 61, 62)));
        btnMaster.setIconTextGap(8);
        btnMaster.setToolTipText("Buka Form Master Template Rencana & Terapi: Buat, ubah, hapus, cari, dan kelola template");
        btnMaster.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMaster.setPreferredSize(new java.awt.Dimension(265, 30));
        btnMaster.addActionListener(e -> bukaDialogTemplateRencanaTerapi());

        // 2. Tombol Template Cepat: Button bergaya Argon Outline / Badge Pill
        javax.swing.JButton btnMenu = new javax.swing.JButton("Template Cepat  ▼") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new java.awt.Color(204, 251, 241));
                } else if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(240, 253, 250));
                } else {
                    g2.setColor(java.awt.Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                // Border tegas
                if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(13, 148, 136));
                    g2.setStroke(new java.awt.BasicStroke(1.5f));
                } else {
                    g2.setColor(new java.awt.Color(15, 61, 62));
                    g2.setStroke(new java.awt.BasicStroke(1.2f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnMenu.setContentAreaFilled(false);
        btnMenu.setFocusPainted(false);
        btnMenu.setBorderPainted(false);
        btnMenu.setOpaque(false);
        btnMenu.setForeground(new java.awt.Color(15, 61, 62));
        btnMenu.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        btnMenu.setIcon(new DlgMasterTemplateRencanaTerapi.ArgonVectorIcon(
            DlgMasterTemplateRencanaTerapi.ArgonVectorIcon.Type.CHIP_ADD, 13, new java.awt.Color(15, 61, 62)));
        btnMenu.setIconTextGap(7);
        btnMenu.setToolTipText("Pilih cepat template rencana terapi dari database");
        btnMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenu.setPreferredSize(new java.awt.Dimension(160, 30));
        btnMenu.addActionListener(e -> tampilkanMenuTemplateRencanaTerapi(btnMenu));

        toolbar.add(btnMaster);
        toolbar.add(btnMenu);

        scrollPane13.setPreferredSize(new java.awt.Dimension(640, 180));
        scrollPane13.setMinimumSize(new java.awt.Dimension(640, 160));

        panel.add(toolbar, java.awt.BorderLayout.NORTH);
        panel.add(scrollPane13, java.awt.BorderLayout.CENTER);
        panel.setPreferredSize(new java.awt.Dimension(640, 224));
        panel.setMinimumSize(new java.awt.Dimension(640, 195));

        return panel;
    }

    /* =========================================================================
     *  TEMPLATE ANAMNESIS MEDIS IGD (KELUHAN UTAMA & RIWAYAT KESEHATAN)
     * ========================================================================= */

    private DlgMasterTemplateAnamnesis dlgTemplateAnamnesis;
    private void bukaDialogTemplateAnamnesis() {
        if (dlgTemplateAnamnesis == null) {
            dlgTemplateAnamnesis = new DlgMasterTemplateAnamnesis(null, true);
        }
        dlgTemplateAnamnesis.setDokter(KdDokter.getText(), NmDokter.getText());
        dlgTemplateAnamnesis.setTemplateListener((nama, isi) -> {
            if (isi != null && !isi.trim().isEmpty()) {
                terapkanTemplateAnamnesis(isi);
            }
        });
        dlgTemplateAnamnesis.setLocationRelativeTo(this);
        dlgTemplateAnamnesis.setVisible(true);
    }

    private void pastikanTabelTemplateAnamnesisAda() {
        try {
            PreparedStatement psCreate = koneksi.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `template_anamnesis_dokter` (" +
                "  `no_template` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `kd_dokter` VARCHAR(20) NOT NULL DEFAULT '-'," +
                "  `nama_template` VARCHAR(100) NOT NULL," +
                "  `anamnesis` TEXT NOT NULL," +
                "  INDEX(`kd_dokter`)" +
                ") ENGINE=InnoDB;"
            );
            psCreate.execute();
            psCreate.close();
        } catch (Exception e) {
            System.out.println("Notif pastikan tabel template anamnesis: " + e);
        }
    }

    private void simpanTeksSebagaiTemplateAnamnesisBaru() {
        String teks = KeluhanUtama.getText().trim();
        if (teks.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Tuliskan format anamnesis di kolom terlebih dahulu sebelum menyimpannya ke Master Template.",
                "Pemberitahuan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kdDok = KdDokter.getText().trim();
        String nmDok = NmDokter.getText().trim();

        javax.swing.JTextField txtNamaBaru = new javax.swing.JTextField();
        txtNamaBaru.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 12));

        javax.swing.JComboBox<String> cmbTipe = new javax.swing.JComboBox<>();
        if (!kdDok.isEmpty()) {
            cmbTipe.addItem("Template Pribadi Dokter (" + (!nmDok.isEmpty() ? nmDok : kdDok) + ")");
            cmbTipe.addItem("Template Semua (Umum RS)");
        } else {
            cmbTipe.addItem("Template Semua (Umum RS)");
        }

        javax.swing.JPanel panelInput = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 6));
        panelInput.add(new javax.swing.JLabel("Nama Template Baru (Contoh: Sacred Seven Kasus Akut):"));
        panelInput.add(txtNamaBaru);
        panelInput.add(new javax.swing.JLabel("Kategori Kepemilikan:"));
        panelInput.add(cmbTipe);

        int res = javax.swing.JOptionPane.showConfirmDialog(this, panelInput,
            "Simpan ke Master Template Anamnesis",
            javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);

        if (res == javax.swing.JOptionPane.OK_OPTION) {
            String nama = txtNamaBaru.getText().trim();
            if (nama.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Nama template tidak boleh kosong!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean isPribadi = cmbTipe.getSelectedItem() != null && cmbTipe.getSelectedItem().toString().startsWith("Template Pribadi");
            String pemilik = isPribadi ? kdDok : "-";

            try {
                pastikanTabelTemplateAnamnesisAda();
                PreparedStatement ps = koneksi.prepareStatement(
                    "insert into template_anamnesis_dokter(kd_dokter,nama_template,anamnesis) values(?,?,?)"
                );
                ps.setString(1, pemilik);
                ps.setString(2, nama);
                ps.setString(3, teks);
                ps.executeUpdate();
                ps.close();

                javax.swing.JOptionPane.showMessageDialog(this,
                    "Template '" + nama + "' berhasil disimpan ke Master Template (" + (isPribadi ? "Pribadi Dokter" : "Umum RS") + ")!\n" +
                    "Template ini langsung otomatis tersedia di menu Template Cepat ▼.",
                    "Sukses", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                System.out.println("Notif simpan template baru anamnesis dari form: " + ex);
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyimpan template: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void terapkanTemplateAnamnesis(String templateTeks) {
        String existing = KeluhanUtama.getText().trim();
        if (existing.isEmpty()) {
            KeluhanUtama.setText(templateTeks);
            KeluhanUtama.requestFocusInWindow();
            KeluhanUtama.setCaretPosition(0);
            hitungProgresMedis();
            return;
        }

        Object[] options = {"Timpa (Replace)", "Sisipkan di Bawah (Append)", "Batal"};
        int pilihan = javax.swing.JOptionPane.showOptionDialog(
            this,
            "Kolom Anamnesis sudah berisi teks.\n" +
            "Apakah Anda ingin menimpa teks yang ada atau menyisipkan template di baris bawah?",
            "Konfirmasi Template Anamnesis",
            javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]
        );

        if (pilihan == 0) { // Timpa
            KeluhanUtama.setText(templateTeks);
            KeluhanUtama.requestFocusInWindow();
            KeluhanUtama.setCaretPosition(0);
            hitungProgresMedis();
        } else if (pilihan == 1) { // Sisipkan di bawah
            KeluhanUtama.setText(existing + "\n\n" + templateTeks);
            KeluhanUtama.requestFocusInWindow();
            KeluhanUtama.setCaretPosition(KeluhanUtama.getText().length());
            hitungProgresMedis();
        }
    }

    private void tampilkanMenuTemplateAnamnesis(javax.swing.JComponent anchor) {
        pastikanTabelTemplateAnamnesisAda();
        javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();

        // 1. Menu Kelola Master Template
        javax.swing.JMenuItem itemKelola = new javax.swing.JMenuItem("Kelola Master Template Anamnesis...");
        itemKelola.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        itemKelola.setForeground(new java.awt.Color(15, 61, 62));
        itemKelola.setIcon(new DlgMasterTemplateAnamnesis.ArgonVectorIcon(
            DlgMasterTemplateAnamnesis.ArgonVectorIcon.Type.CHIP_SAVE, 13, new java.awt.Color(15, 61, 62)));
        itemKelola.addActionListener(e -> bukaDialogTemplateAnamnesis());
        popup.add(itemKelola);

        // 2. Menu Simpan Teks di Kolom Sebagai Template Baru
        javax.swing.JMenuItem itemSimpan = new javax.swing.JMenuItem("Simpan Teks Kolom Ini Sebagai Template Baru...");
        itemSimpan.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        itemSimpan.setForeground(new java.awt.Color(15, 61, 62));
        itemSimpan.setIcon(new DlgMasterTemplateAnamnesis.ArgonVectorIcon(
            DlgMasterTemplateAnamnesis.ArgonVectorIcon.Type.CHIP_ADD, 12, new java.awt.Color(15, 61, 62)));
        itemSimpan.addActionListener(e -> simpanTeksSebagaiTemplateAnamnesisBaru());
        popup.add(itemSimpan);
        popup.addSeparator();

        String kdDok = KdDokter.getText().trim();
        String nmDok = NmDokter.getText().trim();
        boolean adaPribadi = false;
        boolean adaUmum = false;

        // 3. Ambil Template Pribadi Dokter dari Database
        if (!kdDok.isEmpty()) {
            try {
                PreparedStatement ps = koneksi.prepareStatement(
                    "select no_template, nama_template, anamnesis from template_anamnesis_dokter " +
                    "where kd_dokter=? order by nama_template asc"
                );
                ps.setString(1, kdDok);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (!adaPribadi) {
                        javax.swing.JLabel lblHeaderPribadi = new javax.swing.JLabel("  TEMPLATE SAYA (" + (!nmDok.isEmpty() ? nmDok : kdDok) + ") :");
                        lblHeaderPribadi.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
                        lblHeaderPribadi.setForeground(new java.awt.Color(29, 78, 216)); // Biru
                        popup.add(lblHeaderPribadi);
                        adaPribadi = true;
                    }
                    String nama = rs.getString("nama_template");
                    String isi = rs.getString("anamnesis");
                    javax.swing.JMenuItem itm = new javax.swing.JMenuItem("   *  " + nama);
                    itm.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
                    itm.setForeground(new java.awt.Color(30, 41, 59));
                    itm.setToolTipText("<html><body style='width:340px; font-size:10px;'>" + isi.replace("\n", "<br>") + "</body></html>");
                    itm.addActionListener(e -> terapkanTemplateAnamnesis(isi));
                    popup.add(itm);
                }
                rs.close();
                ps.close();
            } catch (Exception ex) {
                System.out.println("Notif load template pribadi anamnesis: " + ex);
            }
        }

        if (adaPribadi) {
            popup.addSeparator();
        }

        // 4. Ambil Template Umum RS dari Database
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "select no_template, nama_template, anamnesis from template_anamnesis_dokter " +
                "where kd_dokter='-' order by nama_template asc"
            );
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!adaUmum) {
                    javax.swing.JLabel lblHeaderUmum = new javax.swing.JLabel("  TEMPLATE STANDAR RS (UMUM) :");
                    lblHeaderUmum.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
                    lblHeaderUmum.setForeground(new java.awt.Color(71, 85, 105)); // Slate
                    popup.add(lblHeaderUmum);
                    adaUmum = true;
                }
                String nama = rs.getString("nama_template");
                String isi = rs.getString("anamnesis");
                javax.swing.JMenuItem itm = new javax.swing.JMenuItem("   -  " + nama);
                itm.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
                itm.setToolTipText("<html><body style='width:340px; font-size:10px;'>" + isi.replace("\n", "<br>") + "</body></html>");
                itm.addActionListener(e -> terapkanTemplateAnamnesis(isi));
                popup.add(itm);
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            System.out.println("Notif load template umum anamnesis: " + ex);
        }

        // 5. Bersihkan Isian Kolom Anamnesis
        popup.addSeparator();
        javax.swing.JMenuItem itemBersihkan = new javax.swing.JMenuItem("Bersihkan Isian Kolom Anamnesis");
        itemBersihkan.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        itemBersihkan.setForeground(new java.awt.Color(185, 28, 28));
        itemBersihkan.setIcon(new DlgMasterTemplateAnamnesis.ArgonVectorIcon(
            DlgMasterTemplateAnamnesis.ArgonVectorIcon.Type.HAPUS, 12, new java.awt.Color(185, 28, 28)));
        itemBersihkan.addActionListener(e -> {
            if (!KeluhanUtama.getText().trim().isEmpty()) {
                int c = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Kosongkan isi kolom anamnesis?",
                    "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
                if (c == javax.swing.JOptionPane.YES_OPTION) {
                    KeluhanUtama.setText("");
                    KeluhanUtama.requestFocusInWindow();
                    hitungProgresMedis();
                }
            }
        });
        popup.add(itemBersihkan);

        popup.show(anchor, 0, anchor.getHeight());
    }

    private widget.PanelBiasa siapkanAreaAnamnesisMedis(){
        widget.PanelBiasa panel = new widget.PanelBiasa(new java.awt.BorderLayout(0, 6));
        panel.setOpaque(false);

        widget.PanelBiasa toolbar = new widget.PanelBiasa(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        // 1. Tombol Master Template Anamnesis: Button bergaya Argon Outline
        javax.swing.JButton btnMaster = new javax.swing.JButton("Master Template Anamnesis") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new java.awt.Color(204, 251, 241));
                } else if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(240, 253, 250));
                } else {
                    g2.setColor(java.awt.Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(13, 148, 136));
                    g2.setStroke(new java.awt.BasicStroke(1.5f));
                } else {
                    g2.setColor(new java.awt.Color(15, 61, 62));
                    g2.setStroke(new java.awt.BasicStroke(1.2f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnMaster.setContentAreaFilled(false);
        btnMaster.setFocusPainted(false);
        btnMaster.setBorderPainted(false);
        btnMaster.setOpaque(false);
        btnMaster.setForeground(new java.awt.Color(15, 61, 62));
        btnMaster.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        btnMaster.setIcon(new DlgMasterTemplateAnamnesis.ArgonVectorIcon(
            DlgMasterTemplateAnamnesis.ArgonVectorIcon.Type.CHIP_SAVE, 14, new java.awt.Color(15, 61, 62)));
        btnMaster.setIconTextGap(8);
        btnMaster.setToolTipText("Buka Form Master Template Anamnesis: Buat, ubah, hapus, cari, dan kelola template");
        btnMaster.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMaster.setPreferredSize(new java.awt.Dimension(240, 30));
        btnMaster.addActionListener(e -> bukaDialogTemplateAnamnesis());

        // 2. Tombol Template Cepat: Button bergaya Argon Outline / Badge Pill
        javax.swing.JButton btnMenu = new javax.swing.JButton("Template Cepat  ▼") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new java.awt.Color(204, 251, 241));
                } else if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(240, 253, 250));
                } else {
                    g2.setColor(java.awt.Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(13, 148, 136));
                    g2.setStroke(new java.awt.BasicStroke(1.5f));
                } else {
                    g2.setColor(new java.awt.Color(15, 61, 62));
                    g2.setStroke(new java.awt.BasicStroke(1.2f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnMenu.setContentAreaFilled(false);
        btnMenu.setFocusPainted(false);
        btnMenu.setBorderPainted(false);
        btnMenu.setOpaque(false);
        btnMenu.setForeground(new java.awt.Color(15, 61, 62));
        btnMenu.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        btnMenu.setIcon(new DlgMasterTemplateAnamnesis.ArgonVectorIcon(
            DlgMasterTemplateAnamnesis.ArgonVectorIcon.Type.CHIP_ADD, 13, new java.awt.Color(15, 61, 62)));
        btnMenu.setIconTextGap(7);
        btnMenu.setToolTipText("Pilih cepat template anamnesis dari database");
        btnMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenu.setPreferredSize(new java.awt.Dimension(160, 30));
        btnMenu.addActionListener(e -> tampilkanMenuTemplateAnamnesis(btnMenu));

        toolbar.add(btnMaster);
        toolbar.add(btnMenu);

        scrollPane1.setPreferredSize(new java.awt.Dimension(640, 160));
        scrollPane1.setMinimumSize(new java.awt.Dimension(640, 140));

        panel.add(toolbar, java.awt.BorderLayout.NORTH);
        panel.add(scrollPane1, java.awt.BorderLayout.CENTER);
        panel.setPreferredSize(new java.awt.Dimension(640, 204));
        panel.setMinimumSize(new java.awt.Dimension(640, 175));

        return panel;
    }

    /* =========================================================================
     *  TARIK DATA PEMERIKSAAN PENUNJANG (PERMINTAAN & HASIL BRIDGING LAB / RAD)
     * ========================================================================= */

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

        // 1. Ambil nm_perawatan dari hasil lab PK (detail_periksa_lab)
        String sqlHasilPK = "SELECT DISTINCT jp.nm_perawatan " +
                            "FROM detail_periksa_lab dp " +
                            "INNER JOIN jns_perawatan_lab jp ON dp.kd_jenis_prw = jp.kd_jenis_prw " +
                            "WHERE dp.no_rawat = ? " +
                            "ORDER BY jp.nm_perawatan ASC";
        try (PreparedStatement ps = koneksi.prepareStatement(sqlHasilPK)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nm = rs.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) {
                        semuaLab.add(nm.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tarik hasil lab PK: " + e);
        }

        // 2. Ambil nm_perawatan dari periksa_lab langsung
        String sqlPeriksaLab = "SELECT DISTINCT jp.nm_perawatan " +
                               "FROM periksa_lab pl " +
                               "INNER JOIN jns_perawatan_lab jp ON pl.kd_jenis_prw = jp.kd_jenis_prw " +
                               "WHERE pl.no_rawat = ? " +
                               "ORDER BY jp.nm_perawatan ASC";
        try (PreparedStatement ps = koneksi.prepareStatement(sqlPeriksaLab)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nm = rs.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) {
                        semuaLab.add(nm.trim());
                    }
                }
            }
        } catch (Exception e) {
            // periksa_lab opsional
        }

        // 3. Ambil nm_perawatan dari permintaan lab PK (permintaan_pemeriksaan_lab)
        String sqlOrderPK = "SELECT DISTINCT jp.nm_perawatan " +
                            "FROM permintaan_lab pl " +
                            "INNER JOIN permintaan_pemeriksaan_lab ppl ON pl.noorder = ppl.noorder " +
                            "INNER JOIN jns_perawatan_lab jp ON ppl.kd_jenis_prw = jp.kd_jenis_prw " +
                            "WHERE pl.no_rawat = ? " +
                            "ORDER BY jp.nm_perawatan ASC";
        try (PreparedStatement ps = koneksi.prepareStatement(sqlOrderPK)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nm = rs.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) {
                        semuaLab.add(nm.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tarik permintaan lab PK: " + e);
        }

        // 4. Ambil nm_perawatan dari permintaan lab PA (permintaan_pemeriksaan_labpa)
        String sqlOrderPA = "SELECT DISTINCT jp.nm_perawatan " +
                            "FROM permintaan_labpa pl " +
                            "INNER JOIN permintaan_pemeriksaan_labpa ppl ON pl.noorder = ppl.noorder " +
                            "INNER JOIN jns_perawatan_lab jp ON ppl.kd_jenis_prw = jp.kd_jenis_prw " +
                            "WHERE pl.no_rawat = ? " +
                            "ORDER BY jp.nm_perawatan ASC";
        try (PreparedStatement ps = koneksi.prepareStatement(sqlOrderPA)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nm = rs.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) {
                        semuaLab.add(nm.trim());
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        // 5. Ambil nm_perawatan dari permintaan lab MB (permintaan_pemeriksaan_labmb)
        String sqlOrderMB = "SELECT DISTINCT jp.nm_perawatan " +
                            "FROM permintaan_labmb pl " +
                            "INNER JOIN permintaan_pemeriksaan_labmb ppl ON pl.noorder = ppl.noorder " +
                            "INNER JOIN jns_perawatan_lab jp ON ppl.kd_jenis_prw = jp.kd_jenis_prw " +
                            "WHERE pl.no_rawat = ? " +
                            "ORDER BY jp.nm_perawatan ASC";
        try (PreparedStatement ps = koneksi.prepareStatement(sqlOrderMB)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nm = rs.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) {
                        semuaLab.add(nm.trim());
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        if (semuaLab.isEmpty()) {
            if (!silentIfEmpty) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Belum ada data permintaan maupun hasil laboratorium untuk pasien ini di No. Rawat " + noRawat,
                    "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            return false;
        }

        String teksLab = String.join(", ", semuaLab);
        terapkanTeksKePenunjang(Laborat, teksLab, "Laboratorium");
        return true;
    }

    private boolean tarikDataLaboratorium() {
        return tarikDataLaboratorium(false);
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

        // 1. Ambil nm_perawatan dari pemeriksaan radiologi yang sudah dikerjakan (periksa_radiologi)
        String sqlPeriksa = "SELECT DISTINCT jpr.nm_perawatan " +
                            "FROM periksa_radiologi px " +
                            "INNER JOIN jns_perawatan_radiologi jpr ON px.kd_jenis_prw = jpr.kd_jenis_prw " +
                            "WHERE px.no_rawat = ? " +
                            "ORDER BY jpr.nm_perawatan ASC";
        try (PreparedStatement ps = koneksi.prepareStatement(sqlPeriksa)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nm = rs.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) {
                        semuaRad.add(nm.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tarik periksa radiologi: " + e);
        }

        // 2. Ambil nm_perawatan dari permintaan radiologi (permintaan_pemeriksaan_radiologi)
        String sqlOrder = "SELECT DISTINCT jr.nm_perawatan " +
                          "FROM permintaan_radiologi pr " +
                          "INNER JOIN permintaan_pemeriksaan_radiologi ppr ON pr.noorder = ppr.noorder " +
                          "INNER JOIN jns_perawatan_radiologi jr ON ppr.kd_jenis_prw = jr.kd_jenis_prw " +
                          "WHERE pr.no_rawat = ? " +
                          "ORDER BY jr.nm_perawatan ASC";
        try (PreparedStatement ps = koneksi.prepareStatement(sqlOrder)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nm = rs.getString("nm_perawatan");
                    if (nm != null && !nm.trim().isEmpty()) {
                        semuaRad.add(nm.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tarik permintaan radiologi: " + e);
        }

        if (semuaRad.isEmpty()) {
            if (!silentIfEmpty) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Belum ada data permintaan maupun hasil radiologi untuk pasien ini di No. Rawat " + noRawat,
                    "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            return false;
        }

        String teksRad = String.join(", ", semuaRad);
        terapkanTeksKePenunjang(Radiologi, teksRad, "Radiologi");
        return true;
    }

    private boolean tarikDataRadiologi() {
        return tarikDataRadiologi(false);
    }

    private boolean tarikDataEKG(boolean silentIfEmpty) {
        String noRawat = TNoRw.getText().trim();
        if (noRawat.isEmpty()) {
            if (!silentIfEmpty) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Nomor rawat pasien belum dipilih / kosong!", "Peringatan",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        boolean ada = false;
        String sql = "SELECT 1 FROM hasil_pemeriksaan_ekg WHERE no_rawat = ? LIMIT 1";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ada = true;
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tarik hasil EKG: " + e);
        }

        if (!ada) {
            if (!silentIfEmpty) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Belum ada data hasil pemeriksaan EKG untuk pasien ini di No. Rawat " + noRawat,
                    "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            return false;
        }

        terapkanTeksKePenunjang(EKG, "EKG", "EKG");
        return true;
    }

    private boolean tarikDataEKG() {
        return tarikDataEKG(false);
    }

    private void tarikSemuaDataPenunjang() {
        String noRawat = TNoRw.getText().trim();
        if (noRawat.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Nomor rawat pasien belum dipilih / kosong!", "Peringatan",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean adaLab = tarikDataLaboratorium(true);
        boolean adaRad = tarikDataRadiologi(true);
        boolean adaEkg = tarikDataEKG(true);

        if (!adaLab && !adaRad && !adaEkg) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Belum ada data permintaan maupun hasil penunjang (Lab, Radiologi, EKG) untuk pasien ini di No. Rawat " + noRawat,
                "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void terapkanTeksKePenunjang(widget.TextArea target, String teksBaru, String namaField) {
        String existing = target.getText().trim();
        if (existing.isEmpty()) {
            target.setText(teksBaru);
            target.requestFocusInWindow();
            target.setCaretPosition(0);
            hitungProgresMedis();
            return;
        }

        Object[] options = {"Timpa (Replace)", "Sisipkan di Bawah (Append)", "Batal"};
        int pilihan = javax.swing.JOptionPane.showOptionDialog(
            this,
            "Kolom " + namaField + " sudah berisi teks.\n" +
            "Apakah Anda ingin menimpa data yang ada atau menambahkan data hasil/permintaan baru di bawahnya?",
            "Konfirmasi Tarik Data " + namaField,
            javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]
        );

        if (pilihan == 0) { // Timpa
            target.setText(teksBaru);
            target.requestFocusInWindow();
            target.setCaretPosition(0);
            hitungProgresMedis();
        } else if (pilihan == 1) { // Sisipkan di bawah
            target.setText(existing + "\n\n" + teksBaru);
            target.requestFocusInWindow();
            target.setCaretPosition(target.getText().length());
            hitungProgresMedis();
        }
    }

    /**
     * Reparent field legacy untuk dua halaman setelah Survei Primer. Komponen
     * riwayat lama yang tidak lagi ditampilkan sengaja tetap ada agar kolom
     * database dan jalur baca data lama tidak berubah.
     */
    private void siapkanKontenRiwayatDanKeadaanUmumMedis(){
        panelRiwayatKesehatanMedis = new widget.PanelBiasa(new java.awt.GridBagLayout());
        panelKeadaanUmumMedis = new widget.PanelBiasa(new java.awt.GridBagLayout());
        latarMedis(panelRiwayatKesehatanMedis, widget.ThemeConfig.SURFACE);
        latarMedis(panelKeadaanUmumMedis, widget.ThemeConfig.SURFACE);

        // Riwayat Kesehatan mengikuti mockup: satu narasi Anamnesis dilengkapi Toolbar Master Template & Template Cepat
        tambahBarisHalamanLegacyMedis(panelRiwayatKesehatanMedis, 0, "Anamnesis", siapkanAreaAnamnesisMedis(), true);

        // Field riwayat rinci tidak lagi ada pada halaman aktif. Nilainya tetap
        // dibaca dan disimpan pada kolom lama demi kompatibilitas data.
        jLabel30.setVisible(false); scrollPane7.setVisible(false);
        jLabel31.setVisible(false); scrollPane2.setVisible(false);
        jLabel32.setVisible(false); scrollPane3.setVisible(false);
        jLabel9.setVisible(false); scrollPane4.setVisible(false);
        jLabel37.setVisible(false); Alergi.setVisible(false);
        jLabel33.setVisible(false);

        int row = 0;
        row = tambahBarisHalamanLegacyMedis(panelKeadaanUmumMedis, row, "Keadaan Umum",
                pilihanRadioLegacyMedis(Keadaan, "Sehat", "Sakit Ringan", "Sakit Sedang", "Sakit Berat"), false);
        row = tambahBarisHalamanLegacyMedis(panelKeadaanUmumMedis, row, "Kesadaran",
                pilihanRadioLegacyMedis(Kesadaran, "Compos Mentis", "Apatis", "Somnolen", "Sopor", "Koma"), false);

        if(TAMPILKAN_TTV_MEDIS){
            widget.PanelBiasa vital = new widget.PanelBiasa(new java.awt.GridLayout(2,4,28,12));
            vital.setOpaque(false);
            vital.add(selTandaVitalMedis("Tekanan Darah", TD, "mmHg"));
            vital.add(selTandaVitalMedis("Nadi", Nadi, "x/menit"));
            vital.add(selTandaVitalMedis("RR", RR, "x/menit"));
            vital.add(selTandaVitalMedis("Suhu", Suhu, "°C"));
            vital.add(selTandaVitalMedis("SpO2", SPO, "%"));
            vital.add(selTandaVitalMedis("TB", TB, "cm"));
            vital.add(selTandaVitalMedis("BB", BB, "kg"));
            vital.add(selAksiTtvMedis());
            java.awt.GridBagConstraints cVital = rm032Constraint(1,row);
            cVital.insets = new java.awt.Insets(9,10,9,10);
            cVital.weightx = 1.0;
            cVital.fill = java.awt.GridBagConstraints.HORIZONTAL;
            panelKeadaanUmumMedis.add(vital,cVital);
            widget.Label labelVital = labelRm032("Tanda Vital & Antropometri :");
            labelVital.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            labelVital.setFont(labelVital.getFont().deriveFont(java.awt.Font.BOLD));
            labelVital.setPreferredSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, 23));
            labelVital.setMinimumSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, 23));
            java.awt.GridBagConstraints cLabelVital = rm032Constraint(0,row++);
            cLabelVital.anchor = java.awt.GridBagConstraints.NORTHWEST;
            cLabelVital.weightx = 0.0;
            cLabelVital.insets = new java.awt.Insets(13,10,9,10);
            panelKeadaanUmumMedis.add(labelVital,cLabelVital);
        }

        row = tambahBarisHalamanLegacyMedis(panelKeadaanUmumMedis, row, "Keterangan Pemeriksaan Fisik", siapkanAreaPemeriksaanFisikMedis(), true);

        // Label/unit absolut lama ikut disembunyikan setelah field direparent.
        jLabel41.setVisible(false); jLabel39.setVisible(false);
        jLabel22.setVisible(false); jLabel23.setVisible(false);
        jLabel17.setVisible(false); jLabel16.setVisible(false);
        jLabel26.setVisible(false); jLabel25.setVisible(false);
        jLabel18.setVisible(false); jLabel20.setVisible(false);
        jLabel29.setVisible(false); jLabel35.setVisible(false);
        jLabel15.setVisible(false); jLabel24.setVisible(false);
        jLabel12.setVisible(false); jLabel13.setVisible(false);
        jSeparator1.setVisible(false);
        jSeparator12.setVisible(false);
        if(!TAMPILKAN_TTV_MEDIS){
            TD.setVisible(false);
            Nadi.setVisible(false);
            RR.setVisible(false);
            Suhu.setVisible(false);
            SPO.setVisible(false);
            TB.setVisible(false);
            BB.setVisible(false);
        }

        tambahGlueVertikalMedis(panelRiwayatKesehatanMedis, 1);
        tambahGlueVertikalMedis(panelKeadaanUmumMedis, row);
    }

    /**
     * Susun ulang pita legacy Status Lokalis tanpa mengganti objek inputnya.
     * PanelWall dan scrollPane8 direparent dari FormInput agar gambar anatomi,
     * dokumen KetLokalis, batas karakter, reset, baca, dan simpan lama tetap
     * menggunakan instance yang sama.
     */
    private widget.PanelBiasa siapkanKontenStatusLokalisMedis(widget.PanelBiasa pitaLama){
        widget.PanelBiasa panel = new widget.PanelBiasa(new java.awt.GridBagLayout());
        latarMedis(panel, widget.ThemeConfig.SURFACE);

        if(pitaLama!=null){
            pitaLama.remove(PanelWall);
            pitaLama.remove(scrollPane8);
        }

        // Kanvas interaktif status lokalis berdasarkan rekomendasi Argon
        if(kanvasLokalis == null){
            kanvasLokalis = new KanvasStatusLokalis(550, 363);
        }

        widget.PanelBiasa toolbarLokalis = new widget.PanelBiasa(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 4));
        toolbarLokalis.setOpaque(false);

        widget.Label lblAlat = new widget.Label();
        lblAlat.setText("Alat Penanda:");
        lblAlat.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        toolbarLokalis.add(lblAlat);

        widget.ComboBox cbAlat = new widget.ComboBox();
        cbAlat.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "○ Lingkaran Merah (Klik)",
            "✕ Tanda Silang 'X' (Klik)",
            "✎ Pensil Coretan Bebas (Drag)"
        }));
        cbAlat.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        cbAlat.setPreferredSize(new java.awt.Dimension(190, 26));
        cbAlat.addActionListener(e -> {
            int idx = cbAlat.getSelectedIndex();
            if(idx == 0){
                kanvasLokalis.setModeAlat(KanvasStatusLokalis.ModeAlat.LINGKARAN);
            } else if(idx == 1){
                kanvasLokalis.setModeAlat(KanvasStatusLokalis.ModeAlat.SILANG);
            } else {
                kanvasLokalis.setModeAlat(KanvasStatusLokalis.ModeAlat.PENSIL);
            }
        });
        toolbarLokalis.add(cbAlat);

        widget.Button btnUndo = new widget.Button();
        btnUndo.setText("Undo (Batal 1 Tanda)");
        btnUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/136.png")));
        btnUndo.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        btnUndo.setPreferredSize(new java.awt.Dimension(160, 26));
        btnUndo.setToolTipText("Batalkan satu tanda terakhir yang digambar");
        btnUndo.addActionListener(e -> kanvasLokalis.undoLast());
        toolbarLokalis.add(btnUndo);

        widget.Button btnReset = new widget.Button();
        btnReset.setText("Reset Gambar");
        btnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/011.png")));
        btnReset.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        btnReset.setPreferredSize(new java.awt.Dimension(130, 26));
        btnReset.setToolTipText("Hapus seluruh tanda dan kembalikan ke gambar anatomi bersih");
        btnReset.addActionListener(e -> {
            if(kanvasLokalis.adaCoretan()){
                int jawab = javax.swing.JOptionPane.showConfirmDialog(panel, 
                    "Apakah Anda yakin ingin menghapus seluruh tanda pada gambar anatomi?", 
                    "Konfirmasi Reset", javax.swing.JOptionPane.YES_NO_OPTION);
                if(jawab == javax.swing.JOptionPane.YES_OPTION){
                    kanvasLokalis.hapusCoretan();
                }
            } else {
                kanvasLokalis.hapusCoretan();
            }
        });
        toolbarLokalis.add(btnReset);

        // Bungkus kanvas ke dalam panel FlowLayout rata kiri agar dimensinya tetap 550x363 (tidak melar ditarik GridBagLayout)
        widget.PanelBiasa panelKanvasPusat = new widget.PanelBiasa(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        panelKanvasPusat.setOpaque(false);
        panelKanvasPusat.add(kanvasLokalis);

        widget.PanelBiasa wrapperKanvas = new widget.PanelBiasa(new java.awt.BorderLayout(0, 6));
        wrapperKanvas.setOpaque(false);
        wrapperKanvas.add(toolbarLokalis, java.awt.BorderLayout.NORTH);
        wrapperKanvas.add(panelKanvasPusat, java.awt.BorderLayout.CENTER);

        scrollPane8.setPreferredSize(new java.awt.Dimension(600, 110));
        scrollPane8.setMinimumSize(new java.awt.Dimension(320, 90));

        int row = 0;
        row = tambahBarisHalamanLegacyMedis(panel, row, "Pemetaan Anatomi Tubuh", wrapperKanvas, true);
        row = tambahBarisHalamanLegacyMedis(panel, row, "Keterangan Status Lokalis", scrollPane8, true);
        tambahGlueVertikalMedis(panel, row);
        return panel;
    }

    /** Sinkronkan pilihan regio sebagai prefiks pada field legacy KetLokalis.
     *  Narasi bebas setelah prefiks tidak pernah diganti oleh checkbox. */
    private void sinkronRegioStatusLokalisMedis(widget.CekBox[] pilihan, String[] regio){
        if(memperbaruiRegioStatusLokalisMedis){ return; }
        StringBuilder pilihanTeks = new StringBuilder();
        for(int i=0;i<pilihan.length;i++){
            if(pilihan[i].isSelected()){
                if(pilihanTeks.length()>0){ pilihanTeks.append(", "); }
                pilihanTeks.append(i+1);
            }
        }
        String narasi = ambilNarasiStatusLokalisMedis(KetLokalis.getText());
        if(pilihanTeks.length()==0){
            KetLokalis.setText(narasi);
        }else if(narasi.length()==0){
            KetLokalis.setText(AWAL_REGIO_STATUS_LOKALIS_MEDIS+pilihanTeks+"]");
        }else{
            KetLokalis.setText(AWAL_REGIO_STATUS_LOKALIS_MEDIS+pilihanTeks+"]\n"+narasi);
        }
    }

    /** Kembalikan narasi bebas setelah prefiks regio yang dikelola form. */
    private String ambilNarasiStatusLokalisMedis(String nilai){
        if(nilai==null || !nilai.startsWith(AWAL_REGIO_STATUS_LOKALIS_MEDIS)){ return nilai==null ? "" : nilai; }
        int akhir = nilai.indexOf(']');
        if(akhir<0){ return nilai; }
        String narasi = nilai.substring(akhir+1);
        return narasi.startsWith("\r\n") ? narasi.substring(2) :
                (narasi.startsWith("\n") ? narasi.substring(1) : narasi);
    }

    /** Pulihkan checkbox dari prefiks regio; teks lama tanpa prefiks tetap utuh. */
    private void pulihkanRegioStatusLokalisMedis(String nilai){
        if(pilihRegioStatusLokalisMedis==null){ return; }
        memperbaruiRegioStatusLokalisMedis = true;
        try{
            boolean[] terpilih = new boolean[pilihRegioStatusLokalisMedis.length];
            if(nilai!=null && nilai.startsWith(AWAL_REGIO_STATUS_LOKALIS_MEDIS)){
                int akhir = nilai.indexOf(']');
                if(akhir>AWAL_REGIO_STATUS_LOKALIS_MEDIS.length()){
                    String[] nomor = nilai.substring(AWAL_REGIO_STATUS_LOKALIS_MEDIS.length(),akhir).split(",");
                    for(String n : nomor){
                        try{
                            int index = Integer.parseInt(n.trim())-1;
                            if(index>=0 && index<terpilih.length){ terpilih[index] = true; }
                        }catch(NumberFormatException e){ /* Prefiks lama/rusak dibiarkan sebagai narasi. */ }
                    }
                }
            }
            for(int i=0;i<pilihRegioStatusLokalisMedis.length;i++){
                pilihRegioStatusLokalisMedis[i].setSelected(terpilih[i]);
            }
        } finally {
            memperbaruiRegioStatusLokalisMedis = false;
        }
    }

    /** Menahan GridBagLayout agar isi halaman tetap mulai dari sisi atas. */
    private void tambahGlueVertikalMedis(widget.PanelBiasa panel, int row){
        java.awt.GridBagConstraints glue = new java.awt.GridBagConstraints();
        glue.gridx=0; glue.gridy=row; glue.gridwidth=2;
        glue.gridheight=java.awt.GridBagConstraints.REMAINDER;
        glue.weightx=0.0; glue.weighty=1.0; glue.fill=java.awt.GridBagConstraints.BOTH;
        panel.add(javax.swing.Box.createGlue(),glue);
    }

    /** Baris Survei Primer konsisten dua kolom seperti mockup: judul rata kiri
     *  dan area pilihan berada di kolom kanan. Field klinis dan urutannya
     *  tetap sama. */
    private int tambahBarisRm032SurveiPrimer(widget.PanelBiasa panel,int row,String judul,
            java.awt.Component field){
        widget.Label lbl = new widget.Label();
        lbl.setText(judul+" :");
        // widget.Label bawaan rata kanan; seluruh judul Survei Primer rata kiri.
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl.setFont(lbl.getFont().deriveFont(java.awt.Font.BOLD));
        lbl.setPreferredSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, 23));
        lbl.setMinimumSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, 23));
        java.awt.GridBagConstraints c1 = rm032Constraint(0,row);
        c1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        c1.weightx = 0.0;
        boolean checklist = field instanceof javax.swing.JComponent && (
                Boolean.TRUE.equals(((javax.swing.JComponent)field).getClientProperty("checklistJalanNapas")) ||
                Boolean.TRUE.equals(((javax.swing.JComponent)field).getClientProperty("checklistSurveiPrimer")));
        // FlowLayout mempertahankan celah antar pilihan. Penanda ini dipakai
        // saat constraint akhir dirapikan agar margin awalnya tidak ikut ganda.
        if(!checklist && field instanceof javax.swing.JComponent &&
                field instanceof java.awt.Container &&
                ((java.awt.Container)field).getLayout() instanceof java.awt.FlowLayout){
            java.awt.FlowLayout flow = (java.awt.FlowLayout)((java.awt.Container)field).getLayout();
            flow.setHgap(10);
            flow.setVgap(0);
            ((javax.swing.JComponent)field).putClientProperty("fieldSurveiPrimerRingkas", Boolean.TRUE);
        }
        // Daftar dua kolom lebih tinggi daripada label. Judul harus dimulai dari
        // atas seperti Intervensi Diberikan, bukan berada di tengah daftar.
        if(checklist){
            c1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            c1.insets = new java.awt.Insets(13,10,9,10);
        } else {
            c1.insets = new java.awt.Insets(9,10,9,10);
        }
        panel.add(lbl,c1);
        java.awt.GridBagConstraints c2 = rm032Constraint(1,row);
        c2.insets = new java.awt.Insets(9,10,9,10);
        c2.weightx = 1.0;
        c2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panel.add(field,c2);
        return row+1;
    }

    /** Alias Airway supaya pemanggil lama tetap jelas secara klinis. */
    private int tambahBarisRm032Airway(widget.PanelBiasa panel,int row,String judul,
            java.awt.Component field){
        return tambahBarisRm032SurveiPrimer(panel,row,judul,field);
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
        row = tambahBarisRm032Judul(panelRm032Atas, row, "INTERVENSI PRE HOSPITAL");
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
        TPreObatKet = new widget.TextBox();
        // Lebar mengikuti mockup Section 1: cukup untuk obat+dosis, tidak
        // meregang sampai tepi kartu seperti field narasi.
        TPreObatKet.setPreferredSize(new java.awt.Dimension(360, TINGGI_FIELD_MEDIS));
        row = tambahBarisRm032Field(panelRm032Atas, row, "Obat (Pre Hospital)", TPreObatKet);

        // -- B. Survei Primer - Jalan Napas --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "SURVEI PRIMER - JALAN NAPAS");
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
        row = tambahBarisRm032Airway(panelRm032Atas, row, "Temuan Jalan Napas",
            panelChecklistJalanNapas(CekJnPaten,CekJnObstruksiPartial,CekJnStridor,CekJnSnoring,CekJnGurgling,
            CekJnWheezing,CekJnObstruksiTotal,CekJnTraumaJalanNapas,CekJnFasialLeherInhalasi,
            CekJnRisikoAspirasi,CekJnBendaAsing));
        bgJnKesimpulan = new javax.swing.ButtonGroup();
        RbJnAman = new widget.RadioButton(); RbJnAman.setText("Aman");
        RbJnMengancamJiwa = new widget.RadioButton(); RbJnMengancamJiwa.setText("Mengancam Jiwa");
        row = tambahBarisRm032Airway(panelRm032Atas, row, "Kesimpulan Jalan Napas",
            panelPilihan(bgJnKesimpulan,RbJnAman,RbJnMengancamJiwa));

        // -- C. Survei Primer - Pernapasan --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "SURVEI PRIMER - PERNAPASAN");
        bgNapPola = new javax.swing.ButtonGroup();
        RbNapSpontan = new widget.RadioButton(); RbNapSpontan.setText("Spontan");
        RbNapTidakSpontan = new widget.RadioButton(); RbNapTidakSpontan.setText("Tidak Spontan");
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Pola Napas", panelPilihan(bgNapPola,RbNapSpontan,RbNapTidakSpontan));
        bgNapReguler = new javax.swing.ButtonGroup();
        RbNapReguler = new widget.RadioButton(); RbNapReguler.setText("Reguler");
        RbNapIrreguler = new widget.RadioButton(); RbNapIrreguler.setText("Irreguler");
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Keteraturan Napas", panelPilihan(bgNapReguler,RbNapReguler,RbNapIrreguler));
        bgNapGerakanDada = new javax.swing.ButtonGroup();
        RbNapSimetris = new widget.RadioButton(); RbNapSimetris.setText("Simetris");
        RbNapAsimetris = new widget.RadioButton(); RbNapAsimetris.setText("Asimetris");
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Gerakan Dada", panelPilihan(bgNapGerakanDada,RbNapSimetris,RbNapAsimetris));
        CekNapJejasKanan = new widget.CekBox(); CekNapJejasKanan.setText("Jejas Dinding Dada Kanan");
        CekNapJejasKiri = new widget.CekBox(); CekNapJejasKiri.setText("Jejas Dinding Dada Kiri");
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Jejas Dinding Dada", panelChecklistSurveiPrimer(CekNapJejasKanan,CekNapJejasKiri));
        CekNapTipeNormal = new widget.CekBox(); CekNapTipeNormal.setText("Normal");
        CekNapTipeTakipneu = new widget.CekBox(); CekNapTipeTakipneu.setText("Takipneu");
        CekNapTipeKussmaul = new widget.CekBox(); CekNapTipeKussmaul.setText("Kussmaul");
        CekNapTipeHiperventilasi = new widget.CekBox(); CekNapTipeHiperventilasi.setText("Hiperventilasi");
        CekNapTipeBiot = new widget.CekBox(); CekNapTipeBiot.setText("Biot");
        CekNapTipeCheyneStoke = new widget.CekBox(); CekNapTipeCheyneStoke.setText("Cheyne Stoke");
        CekNapTipeApneustic = new widget.CekBox(); CekNapTipeApneustic.setText("Apneustic");
        CekNapTipeRetraktif = new widget.CekBox(); CekNapTipeRetraktif.setText("Retraktif");
        CekNapTipeFlare = new widget.CekBox(); CekNapTipeFlare.setText("Flare");
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Tipe Pernapasan", panelChecklistSurveiPrimer(CekNapTipeNormal,CekNapTipeTakipneu,
            CekNapTipeKussmaul,CekNapTipeHiperventilasi,CekNapTipeBiot,CekNapTipeCheyneStoke,
            CekNapTipeApneustic,CekNapTipeRetraktif,CekNapTipeFlare));
        bgNapKesimpulan = new javax.swing.ButtonGroup();
        RbNapAman = new widget.RadioButton(); RbNapAman.setText("Aman");
        RbNapMengancamJiwa = new widget.RadioButton(); RbNapMengancamJiwa.setText("Mengancam Jiwa");
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Kesimpulan Pernapasan", panelPilihan(bgNapKesimpulan,RbNapAman,RbNapMengancamJiwa));

        // -- D. Survei Primer - Sirkulasi --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "SURVEI PRIMER - SIRKULASI");
        CekSirReguler = new widget.CekBox(); CekSirReguler.setText("Reguler");
        CekSirIrreguler = new widget.CekBox(); CekSirIrreguler.setText("Irreguler");
        tautkanCekBoxEksklusif(CekSirReguler, CekSirIrreguler);
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Nadi", panelCekBox(CekSirReguler, CekSirIrreguler));

        CekSirKuat = new widget.CekBox(); CekSirKuat.setText("Kuat");
        CekSirLemah = new widget.CekBox(); CekSirLemah.setText("Lemah");
        tautkanCekBoxEksklusif(CekSirKuat, CekSirLemah);
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Kekuatan Nadi", panelCekBox(CekSirKuat, CekSirLemah));

        CekSirKulitNormal = new widget.CekBox(); CekSirKulitNormal.setText("Normal");
        CekSirKulitPucat = new widget.CekBox(); CekSirKulitPucat.setText("Pucat");
        tautkanCekBoxEksklusif(CekSirKulitNormal, CekSirKulitPucat);
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Kulit/Mukosa", panelCekBox(CekSirKulitNormal, CekSirKulitPucat));

        CekSirJaundice = new widget.CekBox(); CekSirJaundice.setText("Jaundice");
        CekSirCyanosis = new widget.CekBox(); CekSirCyanosis.setText("Cyanosis");
        CekSirBerkeringat = new widget.CekBox(); CekSirBerkeringat.setText("Berkeringat");
        tautkanCekBoxEksklusif(CekSirJaundice, CekSirCyanosis);
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Kondisi Kulit", panelCekBox(CekSirJaundice, CekSirCyanosis, CekSirBerkeringat));

        CekSirHangat = new widget.CekBox(); CekSirHangat.setText("Hangat");
        CekSirDingin = new widget.CekBox(); CekSirDingin.setText("Dingin");
        tautkanCekBoxEksklusif(CekSirHangat, CekSirDingin);
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Akral", panelCekBox(CekSirHangat, CekSirDingin));

        CekSirKering = new widget.CekBox(); CekSirKering.setText("Kering");
        CekSirBasah = new widget.CekBox(); CekSirBasah.setText("Basah");
        tautkanCekBoxEksklusif(CekSirKering, CekSirBasah);
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Kondisi Akral", panelCekBox(CekSirKering, CekSirBasah));

        CekSirCrtKurang2 = new widget.CekBox(); CekSirCrtKurang2.setText("<2 detik");
        CekSirCrtLebih2 = new widget.CekBox(); CekSirCrtLebih2.setText(">2 detik");
        tautkanCekBoxEksklusif(CekSirCrtKurang2, CekSirCrtLebih2);
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "CRT", panelCekBox(CekSirCrtKurang2, CekSirCrtLebih2));

        CekSirAman = new widget.CekBox(); CekSirAman.setText("Aman");
        CekSirMengancamJiwa = new widget.CekBox(); CekSirMengancamJiwa.setText("Mengancam Jiwa");
        tautkanCekBoxEksklusif(CekSirAman, CekSirMengancamJiwa);
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Kesimpulan Sirkulasi", panelCekBox(CekSirAman, CekSirMengancamJiwa));

        // -- E. Kategori Pasien + Bayi Baru Lahir (APGAR/PAT) --
        // Pilihan kategori ditampilkan di header; objeknya dibuat di sini agar
        // listener dan seluruh jalur data lama tetap memakai instance yang sama.
        KategoriPasien = new widget.ComboBox();
        KategoriPasien.setPreferredSize(new java.awt.Dimension(200,23));
        KategoriPasien.addItem("Umum");
        KategoriPasien.addItem("Bayi Baru Lahir");
        KategoriPasien.addItemListener((java.awt.event.ItemEvent e) -> perbaruiVisibilitasBayiBaruLahir());

        buatPanelBayiBaruLahirPAT();
        perbaruiVisibilitasBayiBaruLahir();

        // -- F. Disabilitas --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "DISABILITAS");
        widget.PanelBiasa panelGcs = new widget.PanelBiasa(new java.awt.GridLayout(1,3,16,0));
        panelGcs.setOpaque(false);
        TGcsE = new widget.TextBox(); TGcsE.setPreferredSize(new java.awt.Dimension(110,TINGGI_FIELD_MEDIS));
        TGcsE.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TGcsE.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.BOLD, 12));
        TGcsV = new widget.TextBox(); TGcsV.setPreferredSize(new java.awt.Dimension(110,TINGGI_FIELD_MEDIS));
        TGcsV.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TGcsV.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.BOLD, 12));
        TGcsM = new widget.TextBox(); TGcsM.setPreferredSize(new java.awt.Dimension(110,TINGGI_FIELD_MEDIS));
        TGcsM.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TGcsM.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.BOLD, 12));
        panelGcs.add(kolomFieldSurveiPrimer("E (Eye)", TGcsE));
        panelGcs.add(kolomFieldSurveiPrimer("V (Verbal)", TGcsV));
        panelGcs.add(kolomFieldSurveiPrimer("M (Motorik)", TGcsM));
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "GCS", panelGcs);

        bgPupil = new javax.swing.ButtonGroup();
        RbPupilIsokor = new widget.RadioButton(); RbPupilIsokor.setText("Isokor");
        RbPupilIsokor.putClientProperty("radioMedisTanpaGaris", Boolean.TRUE);
        RbPupilAnisokor = new widget.RadioButton(); RbPupilAnisokor.setText("Anisokor");
        RbPupilAnisokor.putClientProperty("radioMedisTanpaGaris", Boolean.TRUE);
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Pupil", panelPilihan(bgPupil,RbPupilIsokor,RbPupilAnisokor));

        widget.PanelBiasa panelDiameter = new widget.PanelBiasa(new java.awt.GridLayout(1,2,16,0));
        panelDiameter.setOpaque(false);
        TDiameterPupilKiri = new widget.TextBox(); TDiameterPupilKiri.setPreferredSize(new java.awt.Dimension(190,TINGGI_FIELD_MEDIS));
        TDiameterPupilKiri.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TDiameterPupilKanan = new widget.TextBox(); TDiameterPupilKanan.setPreferredSize(new java.awt.Dimension(190,TINGGI_FIELD_MEDIS));
        TDiameterPupilKanan.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panelDiameter.add(kolomFieldSurveiPrimer("Kiri (mm)", TDiameterPupilKiri));
        panelDiameter.add(kolomFieldSurveiPrimer("Kanan (mm)", TDiameterPupilKanan));
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Diameter Pupil (mm)", panelDiameter);

        bgReflekKiri = new javax.swing.ButtonGroup();
        RbReflekKiriPositif = new widget.RadioButton(); RbReflekKiriPositif.setText("Positif");
        RbReflekKiriNegatif = new widget.RadioButton(); RbReflekKiriNegatif.setText("Negatif");
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Reflek Cahaya Kiri", panelPilihan(bgReflekKiri,RbReflekKiriPositif,RbReflekKiriNegatif));

        bgReflekKanan = new javax.swing.ButtonGroup();
        RbReflekKananPositif = new widget.RadioButton(); RbReflekKananPositif.setText("Positif");
        RbReflekKananNegatif = new widget.RadioButton(); RbReflekKananNegatif.setText("Negatif");
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Reflek Cahaya Kanan", panelPilihan(bgReflekKanan,RbReflekKananPositif,RbReflekKananNegatif));

        TMeningealSigns = new widget.TextBox();
        TMeningealSigns.setPreferredSize(new java.awt.Dimension(360,TINGGI_FIELD_MEDIS));
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Meningeal Signs", TMeningealSigns);

        bgLateralisasi = new javax.swing.ButtonGroup();
        RbLateralisasiKanan = new widget.RadioButton(); RbLateralisasiKanan.setText("Kanan");
        RbLateralisasiKiri = new widget.RadioButton(); RbLateralisasiKiri.setText("Kiri");
        RbLateralisasiTidakAda = new widget.RadioButton(); RbLateralisasiTidakAda.setText("Tidak Ada");
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Lateralisasi", panelPilihan(bgLateralisasi,RbLateralisasiKanan,RbLateralisasiKiri,RbLateralisasiTidakAda));

        // -- G. Eksposur --
        row = tambahBarisRm032Judul(panelRm032Atas, row, "EKSPOSUR");
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
        row = tambahBarisRm032SurveiPrimer(panelRm032Atas, row, "Temuan Eksposur",
            panelChecklistSurveiPrimer(CekEksDeformitas,CekEksEkskoriasi,CekEksContusio,CekEksAbrasi,
            CekEksPenetrasi,CekEksBurn,CekEksTenderness,CekEksLaserasi,CekEksSwelling,CekEksTidakTampakJelas));

        // ===== Panel TENGAH: Kepala Leher, Leher rinci, Thorax, Abdomen &
        // Pelvis - ditempel SEBELUM "IV. PEMERIKSAAN PENUNJANG" (jLabel101,
        // y asli 870) supaya urutannya sama dengan formulir fisik (mengikuti
        // "III. STATUS LOKALIS" yang sudah berisi diagram anatomi). =====
        panelRm032Tengah = new widget.PanelBiasa();
        panelRm032Tengah.setLayout(new java.awt.GridBagLayout());
        row = 0;

        // -- H. Kepala Leher --
        row = tambahBarisRm032Judul(panelRm032Tengah, row, "KEPALA LEHER");
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
        row = tambahBarisRm032Judul(panelRm032Tengah, row, "LEHER");
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
        row = tambahBarisRm032Judul(panelRm032Tengah, row, "THORAX");
        TJantung = new widget.TextArea(); TJantung.setRows(2); TJantung.setColumns(40);
        widget.ScrollPane scrollJantung = new widget.ScrollPane(); scrollJantung.setViewportView(TJantung);
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Jantung", scrollJantung);
        TParu = new widget.TextArea(); TParu.setRows(2); TParu.setColumns(40);
        widget.ScrollPane scrollParu = new widget.ScrollPane(); scrollParu.setViewportView(TParu);
        row = tambahBarisRm032Field(panelRm032Tengah, row, "Paru", scrollParu);

        // -- K. Abdomen & Pelvis --
        row = tambahBarisRm032Judul(panelRm032Tengah, row, "ABDOMEN & PELVIS");
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

        rowDiagnosis = tambahBarisRm032Judul(panelRm032Diagnosis, rowDiagnosis, "DIAGNOSIS");

        CekDiagnosaKerja = new widget.CekBox();
        KdDiagnosaKerja = new widget.TextBox();
        TDiagnosaKerja = new widget.TextBox();
        TDiagnosaKerja.setEditable(true);
        TDiagnosaKerja.setPreferredSize(new java.awt.Dimension(0,TINGGI_FIELD_MEDIS));
        TDiagnosaKerja.setDocument(new batasInput((int)500).getKata(TDiagnosaKerja));
        TDiagnosaKerja.putClientProperty("JTextField.placeholderText","Masukkan diagnosis kerja...");
        TDiagnosaKerja.putClientProperty("Component.arc", 4);
        TDiagnosaKerja.putClientProperty("ModernStyle.active", Boolean.TRUE);
        TDiagnosaKerja.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent evt) {
                Valid.pindah(evt, PemeriksaanLainnya, TDiagnosaBanding);
            }
        });
        rowDiagnosis = tambahBarisRm032Field(panelRm032Diagnosis, rowDiagnosis, "Diagnosa Kerja", TDiagnosaKerja);

        CekDiagnosaBanding = new widget.CekBox();
        TDiagnosaBanding = new widget.TextBox();
        TDiagnosaBanding.setEditable(true);
        TDiagnosaBanding.setPreferredSize(new java.awt.Dimension(0,TINGGI_FIELD_MEDIS));
        TDiagnosaBanding.setDocument(new batasInput((int)500).getKata(TDiagnosaBanding));
        TDiagnosaBanding.putClientProperty("JTextField.placeholderText","Masukkan diagnosis banding (opsional)...");
        TDiagnosaBanding.putClientProperty("Component.arc", 4);
        TDiagnosaBanding.putClientProperty("ModernStyle.active", Boolean.TRUE);
        TDiagnosaBanding.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent evt) {
                Valid.pindah(evt, TDiagnosaKerja, Tatalaksana);
            }
        });
        rowDiagnosis = tambahBarisRm032Field(panelRm032Diagnosis, rowDiagnosis, "Diagnosa Banding", TDiagnosaBanding);

        tambahGlueVertikalMedis(panelRm032Diagnosis, rowDiagnosis);

        // ===== Panel BAWAH: sisanya (Konsul dr. Spesialis .. Tanda Vital
        // Terakhir), tetap ditempel di bawah semua konten. =====
        panelRm032 = new widget.PanelBiasa();
        panelRm032.setLayout(new java.awt.GridBagLayout());
        row = 0;

        // -- N. Konsul dr. Spesialis --
        row = tambahBarisRm032Judul(panelRm032, row, "KONSUL DR. SPESIALIS");
        KonsulKdDokter = new widget.TextBox(); KonsulKdDokter.setPreferredSize(new java.awt.Dimension(90,TINGGI_FIELD_MEDIS)); KonsulKdDokter.setEditable(false);
        KonsulNmDokter = new widget.TextBox(); KonsulNmDokter.setPreferredSize(new java.awt.Dimension(0,TINGGI_FIELD_MEDIS)); KonsulNmDokter.setEditable(false);
        BtnKonsulDokter = new widget.Button();
        BtnKonsulDokter.setIcon(widget.IconStyle.muat("search.svg",14,widget.IconStyle.ABU_MUDA));
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
        BtnKonsulDokter.setPreferredSize(new java.awt.Dimension(TINGGI_FIELD_MEDIS,TINGGI_FIELD_MEDIS));
        widget.PanelBiasa panelKonsulDokter = new widget.PanelBiasa(new java.awt.BorderLayout(5,0));
        panelKonsulDokter.add(KonsulKdDokter,java.awt.BorderLayout.WEST);
        panelKonsulDokter.add(KonsulNmDokter,java.awt.BorderLayout.CENTER);
        panelKonsulDokter.add(BtnKonsulDokter,java.awt.BorderLayout.EAST);
        row = tambahBarisRm032Field(panelRm032, row, "Dokter Konsul", panelKonsulDokter);

        KonsulKdPoli = new widget.TextBox(); KonsulKdPoli.setPreferredSize(new java.awt.Dimension(90,TINGGI_FIELD_MEDIS)); KonsulKdPoli.setEditable(false);
        KonsulNmPoli = new widget.TextBox(); KonsulNmPoli.setPreferredSize(new java.awt.Dimension(0,TINGGI_FIELD_MEDIS)); KonsulNmPoli.setEditable(false);
        BtnKonsulUnit = new widget.Button();
        BtnKonsulUnit.setIcon(widget.IconStyle.muat("search.svg",14,widget.IconStyle.ABU_MUDA));
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
        BtnKonsulUnit.setPreferredSize(new java.awt.Dimension(TINGGI_FIELD_MEDIS,TINGGI_FIELD_MEDIS));
        widget.PanelBiasa panelKonsulUnit = new widget.PanelBiasa(new java.awt.BorderLayout(5,0));
        panelKonsulUnit.add(KonsulKdPoli,java.awt.BorderLayout.WEST);
        panelKonsulUnit.add(KonsulNmPoli,java.awt.BorderLayout.CENTER);
        panelKonsulUnit.add(BtnKonsulUnit,java.awt.BorderLayout.EAST);
        row = tambahBarisRm032Field(panelRm032, row, "Unit/Poli Konsul", panelKonsulUnit);

        // -- O. Edukasi --
        row = tambahBarisRm032Judul(panelRm032, row, "EDUKASI");
        CekEdukasiDiberikan = new widget.CekBox(); CekEdukasiDiberikan.setText("Diberikan kepada Pasien/Keluarga");
        // Chip dibungkus FlowLayout agar tidak melar mengikuti kolom field.
        // Baris alasan di bawah memakai BorderLayout supaya keterangannya
        // mengambil seluruh sisa lebar tanpa ikut melebarkan chip.
        widget.PanelBiasa panelEdukasiDiberikan = new widget.PanelBiasa(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,0,0));
        panelEdukasiDiberikan.add(CekEdukasiDiberikan);
        row = tambahBarisRm032Field(panelRm032, row, "Edukasi Diberikan", panelEdukasiDiberikan);
        CekEdukasiTidakBisa = new widget.CekBox(); CekEdukasiTidakBisa.setText("Tidak dapat memberikan edukasi karena");
        TEdukasiTidakBisaKet = new widget.TextBox(); TEdukasiTidakBisaKet.setPreferredSize(new java.awt.Dimension(0,TINGGI_FIELD_MEDIS));
        widget.PanelBiasa panelEdukasiTidakBisa = new widget.PanelBiasa(new java.awt.BorderLayout(8,0));
        panelEdukasiTidakBisa.add(CekEdukasiTidakBisa,java.awt.BorderLayout.WEST);
        panelEdukasiTidakBisa.add(TEdukasiTidakBisaKet,java.awt.BorderLayout.CENTER);
        row = tambahBarisRm032Field(panelRm032, row, "Edukasi Tidak Diberikan", panelEdukasiTidakBisa);

        // -- P. Kondisi Saat Ini / Keluar IGD --
        row = tambahBarisRm032Judul(panelRm032, row, "KONDISI SAAT INI / KELUAR IGD");
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
        row = tambahBarisHalamanLegacyMedis(panelRm032, row, "Kondisi Saat Ini / Keluar IGD", panelPilihan(bgKondisiPulang,
            RbKondisiMembaik,RbKondisiMemburuk,RbKondisiTetap,RbKondisiMeninggal,RbKondisiDoa), false);
        DTPKondisiPulangJam = new PanelTanggalDanJamMedis(TINGGI_FIELD_MEDIS);
        row = tambahBarisHalamanLegacyMedis(panelRm032, row, "Tanggal & Jam Meninggal", DTPKondisiPulangJam, false);
        DTPKondisiPulangJam.setEnabled(false);

        // -- Q. Tanda Vital Terakhir --
        // Disusun satu baris horizontal (pola sama dengan "Tanda Vital &
        // Antropometri" di Keadaan Umum lewat selTandaVitalMedis()) sesuai
        // mockup, bukan lagi 4 baris vertikal terpisah. Instance TextBox
        // tetap sama, hanya susunannya yang berubah.
        row = tambahBarisRm032Judul(panelRm032, row, "TANDA VITAL TERAKHIR");
        TTdAkhir = new widget.TextBox();
        TNadiAkhir = new widget.TextBox();
        TRrAkhir = new widget.TextBox();
        TSuhuAkhir = new widget.TextBox();
        widget.PanelBiasa vitalAkhir = new widget.PanelBiasa(new java.awt.GridLayout(1,4,28,12));
        vitalAkhir.setOpaque(false);
        vitalAkhir.add(selTandaVitalMedis("Tekanan Darah Akhir", TTdAkhir, "mmHg"));
        vitalAkhir.add(selTandaVitalMedis("Nadi Akhir", TNadiAkhir, "x/menit"));
        vitalAkhir.add(selTandaVitalMedis("RR Akhir", TRrAkhir, "x/menit"));
        vitalAkhir.add(selTandaVitalMedis("Suhu Akhir (SB)", TSuhuAkhir, "°C"));
        row = tambahBarisHalamanLegacyMedis(panelRm032, row, "Tanda Vital Terakhir", vitalAkhir, true);

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

    private void perbaruiVisibilitasDiagnosisMedis(){
        // Keduanya sekarang selalu tampil sebagai inputan manual langsung
    }

    private String teksDiagnosisKerjaMedis(){
        return TDiagnosaKerja != null ? TDiagnosaKerja.getText().trim() : "";
    }

    private String teksDiagnosisBandingMedis(){
        return TDiagnosaBanding != null ? TDiagnosaBanding.getText().trim() : "";
    }

    private boolean diagnosisMedisValid(){
        return true;
    }

    /**
     * Teruskan roda mouse ke halaman utama saat TextArea sudah mencapai batas
     * atas/bawah. TextArea tetap dapat menggulir isinya sendiri selama masih
     * ada isi yang belum terlihat.
     */
    private void pasangScrollBertingkatTextAreaMedis(java.awt.Component komponen){
        if(komponen instanceof javax.swing.JScrollPane){
            final javax.swing.JScrollPane scrollDalam = (javax.swing.JScrollPane)komponen;
            java.awt.Component isi = scrollDalam.getViewport().getView();
            if(isi instanceof javax.swing.JTextArea
                    && !Boolean.TRUE.equals(scrollDalam.getClientProperty("scrollBertingkatMedis"))){
                scrollDalam.putClientProperty("scrollBertingkatMedis",Boolean.TRUE);
                scrollDalam.addMouseWheelListener((java.awt.event.MouseWheelEvent evt) -> {
                    javax.swing.JScrollBar barDalam = scrollDalam.getVerticalScrollBar();
                    int arah = evt.getWheelRotation();
                    boolean bisaKeAtas = arah < 0 && barDalam.getValue() > barDalam.getMinimum();
                    boolean bisaKeBawah = arah > 0
                            && barDalam.getValue()+barDalam.getVisibleAmount() < barDalam.getMaximum();
                    if(bisaKeAtas || bisaKeBawah){ return; }

                    java.awt.Container induk = scrollDalam.getParent();
                    while(induk!=null && !(induk instanceof javax.swing.JScrollPane)){
                        induk = induk.getParent();
                    }
                    if(induk instanceof javax.swing.JScrollPane){
                        javax.swing.JScrollBar barLuar = ((javax.swing.JScrollPane)induk).getVerticalScrollBar();
                        int unit = Math.max(16,barLuar.getUnitIncrement(arah));
                        barLuar.setValue(barLuar.getValue()+(evt.getUnitsToScroll()*unit));
                        evt.consume();
                    }
                });
            }
        }
        if(komponen instanceof java.awt.Container){
            for(java.awt.Component anak : ((java.awt.Container)komponen).getComponents()){
                pasangScrollBertingkatTextAreaMedis(anak);
            }
        }
    }

    /**
     * [Bagian C] Selipkan 5 judul section LEGACY (jLabel99/94/100/101/103) ke
     * posisi yg benar dalam seksiMedis - satu-satunya bagian yg TIDAK lewat
     * funnel tambahBarisRm032Judul() (lihat komentar struktural di kelas ini).
     * jLabel102 ("V", sudah disembunyikan pensiunkanFieldLama()) SENGAJA
     * dikecualikan. Index dihitung eksplisit (bukan cari string judul) supaya
     * gampang diperiksa: sesudah initRm032Tambahan(), seksiMedis berisi 15
     * entri urut [I..IV, VI..VII, XI..XIV, XVI, XVIII..XXI]. Kategori
     * pasien berada di header, sehingga sengaja bukan seksi halaman.
     * Splice di titik ini menghasilkan urutan akhir I..IV, VI..XXI.
     */
    private void daftarkanSeksiLegacyMedis(){
        seksiMedis.add(6, new SeksiMedis(jLabel99.getText(), jLabel99));   // VIII
        seksiMedis.add(7, new SeksiMedis(jLabel94.getText(), jLabel94));   // IX
        seksiMedis.add(8, new SeksiMedis(jLabel100.getText(), jLabel100)); // X
        // Sesudah splice, "XI..XIV" kini di indeks 9..12, "XVI" di indeks 13.
        seksiMedis.add(13, new SeksiMedis(jLabel101.getText(), jLabel101)); // XV, sebelum XVI
        // "XVI" kini di indeks 14, "XVIII.." mulai indeks 15.
        seksiMedis.add(15, new SeksiMedis(jLabel103.getText(), jLabel103)); // XVII, sebelum XVIII
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
    /** Tinggi field banner mengikuti Pengkajian Awal Keperawatan IGD. */
    private static final int TINGGI_FIELD_HEADER_MEDIS = 26;
    /** Lebar seragam kolom label kiri (232px) sesuai design handoff. */
    private static final int LEBAR_LABEL_KIRI_MEDIS = 232;
    /**
     * Flag konfigurasi untuk menampilkan atau menyembunyikan bagian TTV & Template/Triase
     * pada kartu Keadaan Umum.
     * - false : Tampilan disesuaikan dengan form fisik RM 03 IGD (hanya Keadaan Umum,
     *           Kesadaran, dan Keterangan Pemeriksaan Fisik; TTV tidak ditampilkan).
     * - true  : Menampilkan grid TTV (TD, Nadi, RR, Suhu, SpO2, TB, BB) beserta tombol
     *           Template TTV dan Tarik Data Triase.
     */
    private static final boolean TAMPILKAN_TTV_MEDIS = true;
    private static final java.awt.Color WARNA_BORDER_KOTAK_MEDIS = new java.awt.Color(0xD8, 0xDB, 0xD6);
    private static final javax.swing.border.Border BORDER_KOTAK_TEXTFIELD_MEDIS =
        javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(WARNA_BORDER_KOTAK_MEDIS, 1),
            javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8));
    private static final javax.swing.border.Border BORDER_KOTAK_GARIS_MEDIS =
        javax.swing.BorderFactory.createLineBorder(WARNA_BORDER_KOTAK_MEDIS, 1);

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

    // =========================================================================
    // [Sesi 3a - Restrukturisasi UI, Bagian D] 106 pilihan radio/checkbox
    // digayakan jadi CHIP (pil). Pola & alasan desain DISALIN dari
    // RMPenilaianAwalKeperawatanIGD.java (gayaChipKep/pasangChipKep/
    // warnaiChipKep, lihat PROGRESS.md Sesi 3a Keperawatan) dgn akhiran
    // "Medis" - termasuk DUA bug yg di sana baru ketemu lewat render PNG,
    // sengaja dibangun benar sejak awal di sini (tidak diulang temuannya):
    // (1) BasicRadioButtonUI/BasicCheckBoxUI memaksa borderPainted=false di
    // installDefaults(), dan AbstractButton.paintBorder() diam2 melompati
    // pengecatan kalau flag itu false - jadi setBorderPainted(true) WAJIB
    // dipanggil SESUDAH javax.swing.JCheckBox/JRadioButton terbentuk (bukan
    // sebelum), kalau tidak pil-nya memakan ruang insets tapi tak pernah
    // dilukis. (2) GridLayout (dipakai panelChecklist()) memberi semua sel
    // ukuran SAMA dan mengisinya penuh, jadi tiap pil melar selebar kolom -
    // ditukar GridBagLayout 2-kolom (anchor=WEST, fill=NONE) + kolom pengisi
    // berbobot di kanan, persis pelukChipGridKep().
    //
    // Dipasang HANYA pada 4 panel funnel RM032 (panelRm032Atas/Tengah/
    // Diagnosis/panelRm032) - sudah diverifikasi lewat Grep bahwa SELURUH
    // 105 dari 106 titik "new widget.RadioButton(...)"/"new widget.CekBox(...)"
    // berada di range baris initRm032Tambahan() (2579-3109); satu-satunya
    // sisanya (ChkAccor, baris 177) adalah tombol kolaps/perluas panel
    // ringkasan HTML (ikon panah, BUKAN pilihan klinis) - kalau digayakan
    // jadi pil, ikonnya akan tertutup padding chip. Menelusuri dari akar
    // FormInput/internalFrame2 akan ikut menyentuh ChkAccor tanpa sengaja;
    // menelusuri dari 4 panel funnel ini tidak mungkin mengenainya.
    // =========================================================================

    /** Latar chip terpilih: PRIMARY dilarutkan ~10% ke putih. Nilai sama
     *  persis dgn CHIP_PILIH_BG_KEP - token warna dipakai bersama. */
    private static final java.awt.Color CHIP_PILIH_BG_MEDIS = new java.awt.Color(0xE6,0xF2,0xF3);

    /** Latar chip saat kursor di atasnya. */
    private static final java.awt.Color CHIP_HOVER_BG_MEDIS = new java.awt.Color(0xEE,0xF1,0xEC);

    /** Border kotak/pil utk JCheckBox/JRadioButton, identik BorderChipKep. */
    private static final class BorderChipMedis implements javax.swing.border.Border {
        private java.awt.Color garis = widget.ThemeConfig.BORDER_SOFT;
        void setGaris(java.awt.Color c){ garis = c; }
        @Override public java.awt.Insets getBorderInsets(java.awt.Component c){
            return new java.awt.Insets(4,10,4,10);
        }
        @Override public boolean isBorderOpaque(){ return false; }
        @Override public void paintBorder(java.awt.Component c, java.awt.Graphics g,
                                          int x, int y, int w, int h){
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setColor(garis);
            g2.drawRect(x, y, w - 1, h - 1);
            g2.dispose();
        }
    }

    /** Panggil sekali dari konstruktor, sesudah 4 panel funnel RM032 selesai
     *  dibangun (initRm032Tambahan()). */
    private void pasangChipSemuaMedis(){
        gayaChipMedis(panelRm032Atas);
        gayaChipMedis(panelRm032Tengah);
        gayaChipMedis(panelRm032Diagnosis);
        gayaChipMedis(panelRm032);
    }

    /** Turun rekursif dan gayakan tiap JRadioButton/JCheckBox jadi chip.
     *  Hanya JPanel yg ditelusuri, sama pola gayaChipKep(). Sesudah anak
     *  digayakan, kalau wadah ini sendiri ber-GridLayout dan isinya chip
     *  (panelChecklist()), layoutnya ditukar supaya chip tidak melar. */
    private void gayaChipMedis(java.awt.Container wadah){
        for(java.awt.Component k : wadah.getComponents()){
            if(k instanceof javax.swing.JRadioButton || k instanceof javax.swing.JCheckBox){
                pasangChipMedis((javax.swing.JToggleButton) k);
            } else if(k instanceof javax.swing.JPanel){
                gayaChipMedis((java.awt.Container) k);
            }
        }
        if(wadah.getLayout() instanceof java.awt.GridLayout){
            pelukChipGridMedis(wadah);
        }
    }

    /** Pasang gaya chip + pemantau status pada satu radio/checkbox. */
    private void pasangChipMedis(final javax.swing.JToggleButton t){
        // Penanda supaya aman dipanggil dua kali - tanpa ini, tiap
        // pemanggilan menambah 1 listener baru.
        if(Boolean.TRUE.equals(t.getClientProperty("chipMedis"))){ return; }
        t.putClientProperty("chipMedis", Boolean.TRUE);
        t.setOpaque(true);
        t.setContentAreaFilled(true);
        // FlatLaf memakai nilai default UI saat melukis JToggleButton.
        // Kunci latar normal di level style agar tidak kembali abu-abu.
        t.putClientProperty("FlatLaf.style",
            "background: #FFFFFF; disabledBackground: #FFFFFF;");
        // WAJIB dipanggil SESUDAH UI terpasang - lihat catatan panjang di
        // komentar blok Sesi 3a di atas kelas ini. Pupil memakai radio polos
        // karena garis kotak chip pada dua opsi ini terlihat seperti garis
        // memanjang di baris field.
        final boolean tanpaGaris = Boolean.TRUE.equals(t.getClientProperty("radioMedisTanpaGaris"));
        t.setBorderPainted(!tanpaGaris);
        t.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        final BorderChipMedis bd = new BorderChipMedis();
        t.setBorder(tanpaGaris
                ? javax.swing.BorderFactory.createEmptyBorder(4,10,4,10)
                : bd);
        java.awt.Dimension ukuranChip = t.getPreferredSize();
        t.setPreferredSize(new java.awt.Dimension(ukuranChip.width, 28));
        t.setMinimumSize(new java.awt.Dimension(ukuranChip.width, 28));
        warnaiChipMedis(t, bd, false);
        // itemStateChanged, bukan actionPerformed: pilihan ButtonGroup yg
        // TER-DESELEKSI tidak pernah memancarkan action.
        t.addItemListener(new java.awt.event.ItemListener(){
            @Override public void itemStateChanged(java.awt.event.ItemEvent e){
                warnaiChipMedis(t, bd, false);
            }
        });
        t.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseEntered(java.awt.event.MouseEvent e){ warnaiChipMedis(t,bd,true); }
            @Override public void mouseExited(java.awt.event.MouseEvent e){ warnaiChipMedis(t,bd,false); }
        });
    }

    /** Tiga status chip, token palet yg sama dgn warnaiChipKep (lihat
     *  komentar panjang di sana soal kontras TEXT_FAINT) - tidak diulang
     *  di sini supaya penjelasannya tidak bercabang dua tempat. */
    private void warnaiChipMedis(javax.swing.JToggleButton t, BorderChipMedis bd, boolean hover){
        if(t.isSelected()){
            t.setForeground(widget.ThemeConfig.PRIMARY);
            t.setBackground(CHIP_PILIH_BG_MEDIS);
            bd.setGaris(widget.ThemeConfig.PRIMARY);
        } else if(hover){
            t.setForeground(widget.ThemeConfig.TEXT_NORMAL);
            t.setBackground(CHIP_HOVER_BG_MEDIS);
            bd.setGaris(widget.ThemeConfig.PRIMARY);
        } else {
            t.setForeground(widget.ThemeConfig.TEXT_NORMAL);
            // Sama dengan chip Keperawatan IGD: kondisi normal putih,
            // tanpa sapuan abu-abu di belakang pilihan.
            t.setBackground(java.awt.Color.WHITE);
            bd.setGaris(widget.ThemeConfig.TEXT_FAINT);
        }
        t.repaint();
    }

    /** Bikin chip di dalam panelChecklist() (GridLayout 2 kolom) MEMELUK
     *  teksnya, bukan melar selebar sel grid - persis pelukChipGridKep().
     *  Jumlah kolom TIDAK dihitung ulang dari lebar (beda dgn Keperawatan):
     *  Medis belum punya sistem anggaran-lebar responsif di Sesi 3a ini
     *  (itu pekerjaan Sesi 3b saat halaman dipecah) - kolom dipertahankan
     *  apa adanya dari GridLayout asli panelChecklist() (selalu 2). */
    private void pelukChipGridMedis(java.awt.Container box){
        java.awt.GridLayout grid = (java.awt.GridLayout) box.getLayout();
        int kolom = grid.getColumns();
        if(kolom<1){ kolom = box.getComponentCount(); }
        if(kolom<1 || box.getComponentCount()<1){ return; }
        boolean adaChip = false;
        for(java.awt.Component k : box.getComponents()){
            if(k instanceof javax.swing.JToggleButton){ adaChip = true; }
        }
        if(!adaChip){ return; }
        int hgap = Math.max(10, grid.getHgap());
        int vgap = Math.max(7, grid.getVgap());
        java.awt.Component[] anak = box.getComponents();
        box.removeAll();
        box.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.anchor = java.awt.GridBagConstraints.WEST;
        for(int i=0;i<anak.length;i++){
            c.gridx = i % kolom;
            c.gridy = i / kolom;
            c.insets = new java.awt.Insets(0, 0, vgap, c.gridx==kolom-1 ? 0 : hgap);
            // Chip selalu memakai ukuran preferred; ruang sisa diserap oleh
            // pengisi sesudah kolom terakhir, bukan oleh kolom pertama.
            c.fill = java.awt.GridBagConstraints.NONE;
            c.weightx = 0.0;
            box.add(anak[i], c);
        }
        // Jika bobot dipasang pada kolom pertama, kolom kedua terdorong jauh
        // ke kanan. Pengisi di ujung menjaga dua kolom chip tetap berdekatan
        // dan seluruh checklist melekat pada sisi kiri area field.
        java.awt.GridBagConstraints pengisi = new java.awt.GridBagConstraints();
        pengisi.gridx = kolom;
        pengisi.gridy = 0;
        pengisi.gridheight = java.awt.GridBagConstraints.REMAINDER;
        pengisi.weightx = 1.0;
        pengisi.weighty = 1.0;
        pengisi.fill = java.awt.GridBagConstraints.BOTH;
        javax.swing.JPanel filler = new javax.swing.JPanel();
        // JPanel polos default OPAQUE dgn latar abu-abu FlatLaf - tanpa ini
        // pengisi yg mestinya tak kasat mata malah tampil sbg kotak abu-abu
        // di sisi tiap grid chip 2 kolom (ketahuan dari medis-hal-01.png).
        filler.setOpaque(false);
        box.add(filler, pengisi);
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
     * Cek apakah pasien tergolong neonatus (usia 0 - 28 hari) berdasarkan tanggal lahir
     * dan tanggal referensi (tanggal registrasi atau hari ini).
     */
    private boolean isNeonatusMedis(String tglLahir, java.util.Date tglRef){
        if(tglLahir == null || tglLahir.trim().length() < 10 || tglLahir.trim().startsWith("0000")){
            return false;
        }
        try{
            String s = tglLahir.trim().substring(0,10);
            java.text.SimpleDateFormat sdf = s.charAt(4) == '-'
                    ? new java.text.SimpleDateFormat("yyyy-MM-dd")
                    : new java.text.SimpleDateFormat("dd-MM-yyyy");
            java.util.Date lahir = sdf.parse(s);
            java.util.Date ref = (tglRef != null) ? tglRef : new java.util.Date();
            long diffMs = ref.getTime() - lahir.getTime();
            long diffHari = diffMs / (1000L * 60 * 60 * 24);
            return diffHari >= 0 && diffHari <= 28;
        }catch(Exception e){
            return false;
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

    /** Komponen bulat yang menggambar inisial nama pasien pada kartu identitas. */
    private static final class AvatarBulatMedis extends javax.swing.JComponent {
        private String inisial = "?";

        void setInisial(String s){
            inisial = s;
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g){
            java.awt.Graphics2D g2 = (java.awt.Graphics2D)g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            int diameter = Math.max(0, Math.min(getWidth(), getHeight()) - 1);
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;
            g2.setColor(widget.ThemeConfig.ACCENT);
            g2.fillOval(x, y, diameter, diameter);
            g2.setColor(java.awt.Color.WHITE);
            g2.setFont(getFont());
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int lebarTeks = fm.stringWidth(inisial);
            int tx = x + (diameter - lebarTeks) / 2;
            int ty = y + (diameter - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(inisial, tx, ty);
            g2.dispose();
        }
    }

    private static String inisialDariNamaMedis(String nama){
        StringBuilder inisial = new StringBuilder();
        if(nama!=null){
            for(String bagian : nama.trim().split("\\s+")){
                if(!bagian.isEmpty() && inisial.length()<2){
                    inisial.append(Character.toUpperCase(bagian.charAt(0)));
                }
            }
        }
        return inisial.length()==0 ? "?" : inisial.toString();
    }

    /** Isi label kartu identitas dari TextBox tersembunyi (display-only). */
    private void sinkronKartuMedis(){
        if(lblKartuNama==null){ return; }
        String nama = TPasien.getText()==null ? "" : TPasien.getText().trim();
        lblKartuNama.setText(nama.isEmpty() ? "—" : nama);
        if(avatarKartuMedis!=null){
            avatarKartuMedis.setInisial(inisialDariNamaMedis(nama));
        }
        lblKartuRM.setText(barisNomorMedis("No. RM", TNoRM.getText()));
        lblKartuRawat.setText(barisNomorMedis("No. Rawat", TNoRw.getText()));
        if(lblKartuLokasi!=null){
            lblKartuLokasi.setText(barisNomorMedis("Lokasi", "IGD"));
        }
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
        c.setBackground(java.awt.Color.WHITE);
    }

    /** Satu sel banner: label kecil di atas field (field dipaksa tinggi seragam). */
    private widget.PanelBiasa hdrCellMedis(String label, java.awt.Component field, boolean wajib){
        widget.PanelBiasa sel = new widget.PanelBiasa();
        sel.setOpaque(false);
        sel.setLayout(new java.awt.BorderLayout(0,3));
        widget.Label l = new widget.Label();
        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        l.setText(wajib ? "<html>"+label+" <font color='#B3261E'>*</font></html>" : label);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        l.setForeground(widget.ThemeConfig.TEXT_MUTED);
        sel.add(l, java.awt.BorderLayout.NORTH);
        int lebarField = field.getPreferredSize()!=null && field.getPreferredSize().width>0
                ? field.getPreferredSize().width : 0;
        field.setPreferredSize(new java.awt.Dimension(lebarField,TINGGI_FIELD_HEADER_MEDIS));
        sel.add(field, java.awt.BorderLayout.CENTER);
        return sel;
    }

    /** Pasangan [field lookup]. */
    private widget.PanelBiasa hdrLookupMedis(widget.TextBox tf, widget.Button btn){
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setOpaque(true);
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT));
        p.setLayout(new java.awt.BorderLayout(0,0));
        p.setPreferredSize(new java.awt.Dimension(220,TINGGI_FIELD_HEADER_MEDIS));
        tf.setPreferredSize(new java.awt.Dimension(180,TINGGI_FIELD_HEADER_MEDIS));
        tf.setBackground(java.awt.Color.WHITE);
        tf.setOpaque(true);
        tf.setBorder(javax.swing.BorderFactory.createEmptyBorder(2,8,2,4));
        tf.putClientProperty("lookupField", Boolean.TRUE);
        btn.setPreferredSize(new java.awt.Dimension(TINGGI_FIELD_HEADER_MEDIS+2,TINGGI_FIELD_HEADER_MEDIS));
        btn.setBackground(java.awt.Color.WHITE);
        btn.setGlassColor(java.awt.Color.WHITE);
        btn.setRoundRect(false);
        btn.setBorder(javax.swing.BorderFactory.createMatteBorder(0,1,0,0,widget.ThemeConfig.BORDER_SOFT));
        btn.putClientProperty("JButton.buttonType", "square");
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
        TglAsuhan.setVisible(true);
        NmDokter.setVisible(true);
        BtnDokter.setVisible(true);
        KategoriPasien.setVisible(true);
        Anamnesis.setVisible(true);
        Hubungan.setVisible(true);
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

        // ---- kartu identitas (kiri), mengikuti hierarki visual referensi ----
        widget.PanelBiasa kartu = new widget.PanelBiasa();
        latarMedis(kartu, widget.ThemeConfig.SURFACE_ALT);
        kartu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
            javax.swing.BorderFactory.createEmptyBorder(12,14,12,14)));
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

        avatarKartuMedis = new AvatarBulatMedis();
        java.awt.Dimension ukuranAvatar = new java.awt.Dimension(40,40);
        avatarKartuMedis.setPreferredSize(ukuranAvatar);
        avatarKartuMedis.setMinimumSize(ukuranAvatar);
        avatarKartuMedis.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));

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
        k.gridx=0; k.gridy=0; k.gridheight=6;
        k.anchor=java.awt.GridBagConstraints.NORTHWEST;
        k.fill=java.awt.GridBagConstraints.NONE; k.weightx=0.0;
        k.insets=new java.awt.Insets(0,0,0,10);
        kartu.add(avatarKartuMedis,k);

        k.gridx=1; k.gridy=0; k.gridheight=1; k.gridwidth=1;
        k.anchor=java.awt.GridBagConstraints.WEST;
        k.fill=java.awt.GridBagConstraints.HORIZONTAL; k.weightx=1.0;
        k.insets=new java.awt.Insets(0,0,0,0);
        kartu.add(judulKartu,k);

        k.gridx=2; k.weightx=0.0; k.fill=java.awt.GridBagConstraints.NONE;
        k.anchor=java.awt.GridBagConstraints.EAST;
        kartu.add(badgeKunci,k);

        k.gridx=1; k.gridwidth=2; k.weightx=1.0;
        k.anchor=java.awt.GridBagConstraints.WEST;
        k.fill=java.awt.GridBagConstraints.HORIZONTAL;
        k.gridy=1; k.insets=new java.awt.Insets(7,0,0,0); kartu.add(lblKartuNama,k);
        k.gridy=2; k.insets=new java.awt.Insets(2,0,0,0); kartu.add(lblKartuInfo,k);
        k.gridy=3; k.insets=new java.awt.Insets(7,0,0,0); kartu.add(lblKartuRM,k);
        k.gridy=4; k.insets=new java.awt.Insets(3,0,0,0); kartu.add(lblKartuRawat,k);
        k.gridy=5; k.insets=new java.awt.Insets(3,0,0,0); kartu.add(lblKartuLokasi,k);
        k.gridx=0; k.gridwidth=3; k.gridy=6; k.weighty=1.0;
        k.fill=java.awt.GridBagConstraints.BOTH; k.insets=new java.awt.Insets(0,0,0,0);
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

        try{
            com.formdev.flatlaf.extras.FlatSVGIcon ikonCari =
                    new com.formdev.flatlaf.extras.FlatSVGIcon("icon/search.svg",13,13);
            ikonCari.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(
                    warna -> widget.ThemeConfig.TEXT_MUTED));
            BtnDokter.setIcon(ikonCari);
        }catch(Throwable ex){
            // Ikon bawaan NetBeans tetap dipakai bila SVG tidak tersedia.
        }
        BtnDokter.setText("");
        tandaiReadOnlyMedis(NmDokter);

        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.gridy=0; g.fill=java.awt.GridBagConstraints.HORIZONTAL; g.anchor=java.awt.GridBagConstraints.NORTH;
        g.weightx=1.0;
        g.gridx=0; g.insets=new java.awt.Insets(0,0,0,12); grid.add(hdrCellMedis("TANGGAL", TglAsuhan, true),g);
        g.gridx=1; grid.add(hdrCellMedis("DOKTER", hdrLookupMedis(NmDokter,BtnDokter), true),g);
        g.gridx=2; grid.add(hdrCellMedis("KATEGORI PASIEN", KategoriPasien, true),g);
        g.gridx=3; grid.add(hdrCellMedis("ANAMNESIS", Anamnesis, false),g);
        g.gridx=4; g.insets=new java.awt.Insets(0,0,0,0); grid.add(hdrCellMedis("KETERANGAN ANAMNESIS", Hubungan, false),g);

        // ---- rakit banner: kartu (kiri, lebar tetap) + grid (kanan, fleksibel) ----
        // Samakan proporsi header dengan Pengkajian Awal Keperawatan IGD.
        java.awt.Dimension dk = new java.awt.Dimension(310,138);
        kartu.setMinimumSize(dk); kartu.setPreferredSize(dk);
        java.awt.GridBagConstraints f = new java.awt.GridBagConstraints();
        f.gridx=0; f.gridy=0; f.fill=java.awt.GridBagConstraints.VERTICAL; f.weighty=1.0;
        f.insets=new java.awt.Insets(0,0,0,16);
        banner.add(kartu,f);
        f.gridx=1; f.weightx=1.0; f.fill=java.awt.GridBagConstraints.HORIZONTAL;
        f.anchor=java.awt.GridBagConstraints.NORTH; f.insets=new java.awt.Insets(0,0,0,0);
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

    /** Ruang isi yg diikutkan saat menggulung ke sebuah halaman. */
    private static final int TINGGI_KONTEKS_MEDIS = 220;

    /** Lebar teks judul di rail (300px rail - aksen - padding - scrollbar). */
    private static final int LEBAR_TEKS_RAIL_MEDIS = 185;

    // =========================================================================
    // [Sesi 3b - Restrukturisasi UI, Bagian D] bagian klinis menjadi halaman kelompok.
    //
    // FormInput berlayout NULL dan isinya campuran: 4 panel funnel
    // ber-GridBagLayout (panelRm032Atas/Tengah/Diagnosis/panelRm032, isi
    // dari tambahBarisRm032Judul()/Field()) + 5 header legacy yg ditempel
    // langsung ke FormInput dgn posisi absolut hasil geser/geser2/geser3
    // di initRm032Tambahan(). Dua sumber beda butuh dua mekanisme beda:
    //   - pecahBarisPanelMedis()  : jalan baris GridBag milik SATU funnel
    //     panel, potong jadi panel baru tiap ketemu strip yg terdaftar
    //     sbg batas halaman (dipakai utk panelRm032Atas dan panelRm032).
    //   - ekstrakPitaYMedis()     : sapu anak langsung FormInput yg posisi
    //     Y-nya (LIVE, sudah lewat geser) ada di antara dua batas, pindah
    //     ke panel baru berlayout null (dipakai utk 3 bagian legacy).
    // panelRm032Tengah dan panelRm032Diagnosis kebetulan = 1 halaman utuh,
    // dipakai langsung tanpa dipecah.
    // =========================================================================

    private static final java.awt.Color KOTAK_BAWAAN_MEDIS = new java.awt.Color(239,244,234);

    /**
     * Sapuan rekursif border bawaan konstruktor tanpa-argumen
     * widget.PanelBiasa (LineBorder #EFF4EA) - dipasang proaktif sejak 3b,
     * bukan ditunggu sampai 3c, krn sudah diketahui dari histori
     * Keperawatan bahwa border ini muncul diam-diam pada panel pembungkus
     * baru yg lupa dibuat lewat konstruktor ber-LayoutManager.
     *
     * HARUS dibatasi ke instance widget.PanelBiasa saja - warna #EFF4EA
     * BUKAN penanda unik "border liar", dipakai juga sebagai border
     * DISENGAJA oleh widget.ScrollPane dan beberapa widget lain (lihat
     * widget/ScrollPane.java, editorpane.java, dst). Sapuan awal yang
     * mencocokkan SEMUA JComponent lewat warna sempat membuat setiap
     * widget.ScrollPane di form (mis. TJantung/TParu Halaman 4) kehilangan
     * bingkainya - dengan setOpaque(false) + teks kosong, field itu jadi
     * tidak terlihat sama sekali. Ketahuan dari render Halaman 4.
     */
    private void buangKotakBawaanMedis(java.awt.Component k){
        if(k instanceof widget.PanelBiasa){
            javax.swing.JComponent jc = (javax.swing.JComponent) k;
            javax.swing.border.Border b = jc.getBorder();
            if(b instanceof javax.swing.border.LineBorder
                    && KOTAK_BAWAAN_MEDIS.equals(((javax.swing.border.LineBorder) b).getLineColor())){
                jc.setBorder(null);
            }
        }
        if(k instanceof java.awt.Container){
            for(java.awt.Component anak : ((java.awt.Container) k).getComponents()){
                buangKotakBawaanMedis(anak);
            }
        }
    }

    /**
     * Tambah pengisi kolom kanan berbobot pada panel GridBagLayout.
     *
     * WAJIB, bukan sekadar jaga-jaga: rm032Constraint() (baris section
     * Medis) TIDAK PERNAH menyetel weightx dan memakai gridwidth tetap
     * (bukan REMAINDER) - artinya baris-baris ini punya bobot kolom nol
     * hari ini. Begitu dibungkus kartu halaman yg lebih lebar dari lebar
     * alami panelnya, gotcha GridBagLayout (CLAUDE.md: menengahkan isi
     * pada kedua sumbu bila container lebih besar) PASTI muncul tanpa ini.
     */
    private void pasangGlueKananMedis(widget.PanelBiasa panel){
        java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) panel.getLayout();
        int kolom = 1;
        for(java.awt.Component k : panel.getComponents()){
            java.awt.GridBagConstraints kc = gbl.getConstraints(k);
            int lebar = kc.gridwidth>0 ? kc.gridwidth : 1;
            kolom = Math.max(kolom, Math.max(0,kc.gridx) + lebar);
        }
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = kolom; c.gridy = 0; c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panel.add(javax.swing.Box.createHorizontalGlue(), c);
    }

    /**
     * Pecah baris GridBag `induk` jadi beberapa panel, potong tiap kali
     * ketemu strip yg terdaftar di `potongDiStrip`. Komponen SEBELUM batas
     * pertama tetap di `induk` sendiri (jadi konten halaman pertama grup
     * ini); strip batas sendiri ikut pindah ke panel BARU (bukan tertinggal
     * di induk) - sama seperti pecahSatuIndukKep() milik Keperawatan.
     * Constraint asli (gridx/gridy/gridwidth/insets/dst) dipertahankan apa
     * adanya, hanya dipindah induknya - GridBagLayout tetap benar walau
     * nomor baris tidak mulai dari 0 di panel barunya.
     */
    private void pecahBarisPanelMedis(widget.PanelBiasa induk,
            java.util.LinkedHashMap<javax.swing.JComponent,widget.PanelBiasa> potongDiStrip){
        java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) induk.getLayout();
        java.util.List<java.awt.Component> anak = new java.util.ArrayList<>(
                java.util.Arrays.asList(induk.getComponents()));
        final java.util.Map<java.awt.Component,java.awt.GridBagConstraints> peta = new java.util.IdentityHashMap<>();
        for(java.awt.Component k : anak){ peta.put(k, gbl.getConstraints(k)); }
        anak.sort((a,b) -> {
            java.awt.GridBagConstraints ca = peta.get(a), cb = peta.get(b);
            if(ca.gridy!=cb.gridy){ return Integer.compare(ca.gridy, cb.gridy); }
            return Integer.compare(ca.gridx, cb.gridx);
        });
        induk.removeAll();
        widget.PanelBiasa tujuan = null;
        for(java.awt.Component k : anak){
            if(potongDiStrip.containsKey(k)){ tujuan = potongDiStrip.get(k); }
            widget.PanelBiasa target = (tujuan==null) ? induk : tujuan;
            target.add(k, peta.get(k));
        }
    }

    /** Lebar isi halaman legacy hasil ekstraksi pita-Y (samakan dgn funnel panel). */
    private static final int LEBAR_ISI_MEDIS = 840;

    /**
     * Ekstrak semua anak langsung FormInput yg posisi Y LIVE-nya (sudah
     * lewat geser/geser2/geser3) berada di [yAtas,yBawah) ke panel baru
     * berlayout null - generalisasi langsung dari idiom geser massal
     * berdasar ambang Y yg sudah dipakai initRm032Tambahan() sendiri.
     * HARUS dipanggil sebelum 4 funnel panel dilepas dari FormInput
     * (batas bawah tiap pita dibaca dari getY() funnel panel tetangganya).
     */
    private widget.PanelBiasa ekstrakPitaYMedis(int yAtas, int yBawah){
        widget.PanelBiasa panelBaru = new widget.PanelBiasa(null);
        java.util.List<java.awt.Component> anak = new java.util.ArrayList<>();
        for(java.awt.Component k : FormInput.getComponents()){
            if(k==panelRm032Atas || k==panelRm032Tengah || k==panelRm032Diagnosis || k==panelRm032){ continue; }
            int y = k.getY();
            if(y>=yAtas && y<yBawah){ anak.add(k); }
        }
        for(java.awt.Component k : anak){
            FormInput.remove(k);
            k.setLocation(k.getX(), k.getY()-yAtas);
            panelBaru.add(k);
        }
        panelBaru.setPreferredSize(new java.awt.Dimension(LEBAR_ISI_MEDIS, Math.max(1, yBawah-yAtas)));
        return panelBaru;
    }

    /**
     * Lebar kartu halaman (funnel panel + margin kartu). 880 sempat
     * memotong "Kg"/"%" di Halaman 3 (jLabel13/jLabel35, posisi absolut
     * legacy sampai x=872) krn insets kartu kiri+kanan 14+14=28 menyisakan
     * cuma 852 utk isi - ketahuan dari render-lihat Sesi 3c. Dinaikkan ke
     * 910 (sisa 882 utk isi, +10 marjin) drpd menggeser posisi absolut
     * legacy itu sendiri (di luar GEN, tapi tetap posisi asli form lama).
     */
    private static final int LEBAR_HALAMAN_MEDIS = 1230;
    private static final int MARGIN_HALAMAN_MEDIS = 14;

    /**
     * Samakan field Medis IGD dengan Pengkajian Awal Keperawatan IGD:
     * seluruh input memakai sudut siku-siku dan garis tepi datar.
     */
    private static void buatKotakMedis(java.awt.Component c){
        if(c==null){ return; }
        if(c instanceof javax.swing.JComponent){
            javax.swing.JComponent jc = (javax.swing.JComponent)c;
            jc.putClientProperty("Component.arc", 0);
            jc.putClientProperty("Button.arc", 0);
            jc.putClientProperty("TextComponent.arc", 0);
            jc.putClientProperty("JTextField.arc", 0);
            jc.putClientProperty("ComboBox.arc", 0);
            jc.putClientProperty("JComponent.roundRect", false);

            if(c instanceof javax.swing.text.JTextComponent){
                if(!(c instanceof javax.swing.JTextArea)
                        && !Boolean.TRUE.equals(jc.getClientProperty("lookupField"))){
                    jc.setBorder(BORDER_KOTAK_TEXTFIELD_MEDIS);
                }
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                    "background: #FFFFFF; inactiveBackground: #FFFFFF; disabledBackground: #FFFFFF;");
            } else if(c instanceof javax.swing.JComboBox){
                jc.setBorder(BORDER_KOTAK_GARIS_MEDIS);
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                    "background: #FFFFFF; disabledBackground: #FFFFFF; buttonBackground: #FFFFFF; editableBackground: #FFFFFF;");
            } else if(c instanceof javax.swing.JScrollPane){
                jc.setBorder(BORDER_KOTAK_GARIS_MEDIS);
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
            } else if(c instanceof uz.ncipro.calendar.JDateTimePicker){
                jc.setBorder(BORDER_KOTAK_GARIS_MEDIS);
                jc.setBackground(java.awt.Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style", "background: #FFFFFF; buttonBackground: #FFFFFF;");
            } else if(c instanceof widget.Button){
                ((widget.Button)c).setRoundRect(false);
            }
        }
        if(c instanceof java.awt.Container){
            for(java.awt.Component anak : ((java.awt.Container)c).getComponents()){
                buatKotakMedis(anak);
            }
        }
    }

    /** Samakan ukuran dan bentuk bar pencarian dengan Awal Keperawatan IGD. */
    private void rapikanBarPencarianMedis(){
        DTPCari1.setPreferredSize(new java.awt.Dimension(125,26));
        DTPCari2.setPreferredSize(new java.awt.Dimension(125,26));
        TCari.setPreferredSize(new java.awt.Dimension(205,26));
        TCari.putClientProperty("JTextField.placeholderText", "Ketik kata kunci...");
        BtnCari.setPreferredSize(new java.awt.Dimension(28,26));
        buatKotakMedis(panelGlass9);
        panelGlass9.revalidate();
        panelGlass9.repaint();
    }

    private String judulBersihMedis(String judul){
        if(judul==null){ return ""; }
        return judul.replaceFirst("^[IVX]+\\.\\s*", "").trim();
    }

    /**
     * Bungkus konten satu halaman dgn kartu sederhana (judul, status, dan isi).
     * SENGAJA jauh lebih sederhana dari buatHalamanKep() milik
     * Keperawatan (badge status/tidakRelevan/nav bawah itu Bagian E - di
     * luar cakupan 3b Medis).
     */
    private widget.PanelBiasa buatKartuHalamanMedis(HalamanMedis h, widget.PanelBiasa isi, int idx){
        widget.PanelBiasa kartu = new widget.PanelBiasa(new java.awt.GridBagLayout());
        latarMedis(kartu, widget.ThemeConfig.SURFACE);
        kartu.setBorder(javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT));

        widget.PanelBiasa strip = new widget.PanelBiasa(new java.awt.BorderLayout(12,0));
        latarMedis(strip, widget.ThemeConfig.SURFACE);
        strip.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0,0,1,0, widget.ThemeConfig.BORDER_SOFT),
            javax.swing.BorderFactory.createEmptyBorder(10,16,10,16)));

        widget.PanelBiasa blokJudul = new widget.PanelBiasa();
        blokJudul.setOpaque(false);
        blokJudul.setLayout(new javax.swing.BoxLayout(blokJudul, javax.swing.BoxLayout.Y_AXIS));
        widget.Label lJudul = new widget.Label();
        lJudul.setText(judulBersihMedis(h.judul));
        lJudul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lJudul.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 13));
        lJudul.setForeground(widget.ThemeConfig.TEXT);
        widget.Label lSubjudul = new widget.Label();
        lSubjudul.setText(h.subjudul);
        lSubjudul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lSubjudul.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 10));
        lSubjudul.setForeground(widget.ThemeConfig.TEXT_FAINT);
        blokJudul.add(lJudul);
        blokJudul.add(lSubjudul);

        // Sesi 5 (Bagian E): badge status wajib + tombol tandai tidak-relevan.
        widget.PanelBiasa kanan = new widget.PanelBiasa();
        kanan.setOpaque(false);
        kanan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 4));

        widget.PanelBiasa pnlStatus = new widget.PanelBiasa();
        pnlStatus.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 8, 4));
        pnlStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(1,6,1,6));
        widget.Label lStatus = new widget.Label();
        lStatus.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11).deriveFont(11.5f));
        lStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pnlStatus.add(lStatus);
        h.pnlBadgeStatus = pnlStatus;
        h.lblBadgeStatus = lStatus;
        kanan.add(pnlStatus);

        final widget.Button btnRel = new widget.Button();
        btnRel.setText(h.tidakRelevan ? "↺ Aktifkan kembali" : "Tandai tidak relevan");
        btnRel.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        btnRel.setPreferredSize(new java.awt.Dimension(140, 26));
        btnRel.setGlassColor(java.awt.Color.WHITE);
        btnRel.setBackground(java.awt.Color.WHITE);
        btnRel.setForeground(widget.ThemeConfig.TEXT);
        btnRel.setBorder(javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT));
        final HalamanMedis hh = h;
        final widget.PanelBiasa isiFinal = isi;
        btnRel.addActionListener((java.awt.event.ActionEvent e) -> {
            hh.tidakRelevan = !hh.tidakRelevan;
            btnRel.setText(hh.tidakRelevan ? "↺ Aktifkan kembali" : "Tandai tidak relevan");
            setHalamanRelevanMedis(hh, isiFinal, !hh.tidakRelevan);
            hitungProgresMedis();
        });
        h.btnTidakRelevan = btnRel;
        kanan.add(btnRel);

        strip.add(blokJudul, java.awt.BorderLayout.CENTER);
        strip.add(kanan, java.awt.BorderLayout.EAST);

        isi.setOpaque(false);

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx=0; c.gridy=0; c.weightx=1.0; c.fill=java.awt.GridBagConstraints.HORIZONTAL;
        kartu.add(strip, c);

        java.awt.GridBagConstraints ci = new java.awt.GridBagConstraints();
        ci.gridx=0; ci.gridy=1; ci.weightx=1.0; ci.fill=java.awt.GridBagConstraints.BOTH;
        ci.insets=new java.awt.Insets(20,22,22,22);
        kartu.add(isi, ci);

        // Sesi 5 (Bagian E): navigasi bawah (prev/next + "Bagian N dari 21").
        widget.PanelBiasa nav = buatNavigasiBawahMedis(idx);
        java.awt.GridBagConstraints cn = new java.awt.GridBagConstraints();
        cn.gridx=0; cn.gridy=2; cn.weightx=1.0; cn.fill=java.awt.GridBagConstraints.HORIZONTAL;
        kartu.add(nav, cn);

        java.awt.GridBagConstraints cg = new java.awt.GridBagConstraints();
        cg.gridx=0; cg.gridy=3; cg.weighty=1.0; cg.fill=java.awt.GridBagConstraints.BOTH;
        kartu.add(javax.swing.Box.createGlue(), cg);

        return kartu;
    }

    /**
     * Sesi 5 (Bagian E): tombol kartu navigasi prev/next, lebar otomatis
     * mengikuti panjang teks. Salinan pola buatKartuTombolNavigasiKep.
     */
    private widget.PanelBiasa buatKartuTombolNavigasiMedis(String teks, String chevron, boolean arahKanan, boolean enabled, final Runnable onClick){
        final widget.PanelBiasa card = new widget.PanelBiasa();
        card.setOpaque(true);
        card.setBackground(java.awt.Color.WHITE);
        card.setLayout(new java.awt.BorderLayout(8, 0));
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
            javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));

        widget.Label lblTeks = new widget.Label();
        lblTeks.setHorizontalAlignment(arahKanan ? javax.swing.SwingConstants.LEFT : javax.swing.SwingConstants.RIGHT);
        lblTeks.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        lblTeks.setForeground(enabled ? widget.ThemeConfig.TEXT_NORMAL : widget.ThemeConfig.TEXT_FAINT);
        lblTeks.setText(teks == null ? "" : teks.trim());

        widget.Label lblChevron = new widget.Label();
        lblChevron.setText(chevron);
        lblChevron.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 13));
        lblChevron.setForeground(enabled ? widget.ThemeConfig.TEXT_MUTED : widget.ThemeConfig.TEXT_FAINT);

        if(arahKanan){
            card.add(lblTeks, java.awt.BorderLayout.CENTER);
            card.add(lblChevron, java.awt.BorderLayout.EAST);
        } else {
            card.add(lblChevron, java.awt.BorderLayout.WEST);
            card.add(lblTeks, java.awt.BorderLayout.CENTER);
        }

        java.awt.FontMetrics fmTeks = lblTeks.getFontMetrics(lblTeks.getFont());
        java.awt.FontMetrics fmChev = lblChevron.getFontMetrics(lblChevron.getFont());
        int pTeks = fmTeks != null ? fmTeks.stringWidth(lblTeks.getText()) : (lblTeks.getText().length() * 7);
        int pChev = fmChev != null ? fmChev.stringWidth(chevron) : 10;
        int lebarCard = pTeks + pChev + 8 + 24 + 10;
        card.setPreferredSize(new java.awt.Dimension(lebarCard, 32));

        if(enabled){
            card.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            lblTeks.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            lblChevron.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBackground(widget.ThemeConfig.SURFACE_ALT);
                    card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.PRIMARY),
                        javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12)
                    ));
                    card.repaint();
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBackground(java.awt.Color.WHITE);
                    card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
                        javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12)
                    ));
                    card.repaint();
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if(onClick != null){ onClick.run(); }
                }
            };
            card.addMouseListener(ma);
            lblTeks.addMouseListener(ma);
            lblChevron.addMouseListener(ma);
        }

        return card;
    }

    /**
     * Sesi 5 (Bagian E): navigasi bawah kartu halaman (prev/next +
     * "Bagian N dari total halaman"). Dipakai JUDUL_HALAMAN_MEDIS langsung
     * (bukan halamanMedis.get(idx±1)) krn dipanggil dari
     * buatKartuHalamanMedis() SAAT susunHalamanMedis() masih membangun
     * daftar halamanMedis satu per satu - entri idx+1 belum tentu ada
     * di list itu saat method ini jalan utk idx yg lebih kecil.
     * keHalamanMedis() Medis sudah memanggil sorotHalamanMedis() sendiri
     * di akhir (beda dari Keperawatan), jadi cukup satu panggilan.
     */
    private widget.PanelBiasa buatNavigasiBawahMedis(final int idx){
        widget.PanelBiasa nav = new widget.PanelBiasa();
        latarMedis(nav, widget.ThemeConfig.SURFACE);
        nav.setBorder(javax.swing.BorderFactory.createMatteBorder(1,0,0,0, widget.ThemeConfig.BORDER_SOFT));
        nav.setLayout(new java.awt.BorderLayout(0,0));
        nav.setPreferredSize(new java.awt.Dimension(10, 48));

        widget.PanelBiasa pnlKiri = new widget.PanelBiasa();
        pnlKiri.setOpaque(false);
        pnlKiri.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 8));
        if(idx > 0){
            pnlKiri.add(buatKartuTombolNavigasiMedis(halamanMedis.get(idx-1).judul, "‹", false, true, () -> {
                keHalamanMedis(idx - 1);
            }));
        } else {
            pnlKiri.add(buatKartuTombolNavigasiMedis("Awal", "‹", false, false, null));
        }

        widget.Label lblPos = new widget.Label();
        lblPos.setText("Bagian " + (idx+1) + " dari " + halamanMedis.size());
        lblPos.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        lblPos.setForeground(widget.ThemeConfig.TEXT_FAINT);
        lblPos.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        widget.PanelBiasa pnlKanan = new widget.PanelBiasa();
        pnlKanan.setOpaque(false);
        pnlKanan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 8));
        if(idx < halamanMedis.size() - 1){
            pnlKanan.add(buatKartuTombolNavigasiMedis(halamanMedis.get(idx+1).judul, "›", true, true, () -> {
                keHalamanMedis(idx + 1);
            }));
        } else {
            pnlKanan.add(buatKartuTombolNavigasiMedis("Akhir", "›", true, false, null));
        }

        nav.add(pnlKiri, java.awt.BorderLayout.WEST);
        nav.add(lblPos, java.awt.BorderLayout.CENTER);
        nav.add(pnlKanan, java.awt.BorderLayout.EAST);
        return nav;
    }

    private static final String[] JUDUL_HALAMAN_MEDIS = {
        "Intervensi Pre Hospital",
        "Survei Primer – Jalan Napas",
        "Survei Primer – Pernapasan",
        "Survei Primer – Sirkulasi",
        "Disabilitas",
        "Eksposur",
        "Riwayat Kesehatan",
        "Pemeriksaan Fisik",
        "Status Lokalis",
        "Kepala Leher",
        "Leher",
        "Thorax",
        "Abdomen & Pelvis",
        "Pemeriksaan Penunjang",
        "Diagnosis",
        "Rencana/Tindakan/Terapi",
        "Konsul Dr. Spesialis",
        "Edukasi",
        "Kondisi Saat Ini / Keluar IGD",
        "Tanda Vital Terakhir",
    };

    private static final String[] SUBJUDUL_HALAMAN_MEDIS = {
        "Tindakan sebelum tiba di IGD",
        "Penilaian patensi dan ancaman jalan napas",
        "Pola, usaha, dan kesimpulan pernapasan",
        "Nadi, perfusi, kulit, dan kesimpulan sirkulasi",
        "Kesadaran, pupil, dan status neurologis",
        "Temuan cedera atau paparan",
        "Keluhan dan riwayat kesehatan pasien",
        "Keadaan umum dan pemeriksaan awal",
        "Pemetaan temuan pada tubuh",
        "Pemeriksaan kepala dan leher",
        "Pemeriksaan leher terperinci",
        "Pemeriksaan thoraks",
        "Pemeriksaan abdomen dan pelvis",
        "EKG, radiologi, laboratorium, dan pemeriksaan lain",
        "Diagnosis kerja dan diagnosis banding",
        "Rencana terapi dan tindakan",
        "Konsultasi dokter spesialis",
        "Edukasi kepada pasien atau keluarga",
        "Kondisi pasien ketika meninggalkan IGD",
        "Tanda vital sebelum pindah atau pulang",
    };

    /**
     * [Bagian D] Bangun 20 sumber halaman dari 20 SeksiMedis. Kategori pasien
     * tidak menjadi seksi karena berada di header; APGAR/PAT tetap ditambahkan
     * sebagai halaman dinamis ketika kategori bayi baru lahir dipilih.
     */
    /** Cari scroll pane yang sudah menaungi textarea legacy tanpa membuat
     *  instance input baru. */
    private javax.swing.JScrollPane scrollDariAreaMedis(javax.swing.text.JTextComponent area){
        java.awt.Component p = area;
        while(p!=null && !(p instanceof javax.swing.JScrollPane)){ p=p.getParent(); }
        return p instanceof javax.swing.JScrollPane ? (javax.swing.JScrollPane)p : null;
    }

    /** Form narasi dua baris yang memakai textarea serta Document legacy.
     *  Tiap baris mengikuti pola label kiri dan field kanan section 1–5. */
    private widget.PanelBiasa siapkanKontenNarasiMedis(String labelPertama,
            javax.swing.text.JTextComponent areaPertama, String labelKedua,
            javax.swing.text.JTextComponent areaKedua){
        widget.PanelBiasa panel = new widget.PanelBiasa(new java.awt.GridBagLayout());
        latarMedis(panel, widget.ThemeConfig.SURFACE);
        javax.swing.JScrollPane pertama = scrollDariAreaMedis(areaPertama);
        javax.swing.JScrollPane kedua = scrollDariAreaMedis(areaKedua);
        if(pertama!=null){ pertama.setPreferredSize(new java.awt.Dimension(640,80)); }
        if(kedua!=null){ kedua.setPreferredSize(new java.awt.Dimension(640,80)); }
        int row=0;
        row = tambahBarisHalamanLegacyMedis(panel, row, labelPertama, pertama, true);
        row = tambahBarisHalamanLegacyMedis(panel, row, labelKedua, kedua, true);
        tambahGlueVertikalMedis(panel,row);
        return panel;
    }

    private widget.PanelBiasa selAreaTeksMedis(String judul, java.awt.Component area, int tinggiArea){
        widget.PanelBiasa p = new widget.PanelBiasa(new java.awt.BorderLayout(0, 5));
        p.setOpaque(false);
        widget.Label lbl = labelRm032(judul);
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl.setFont(lbl.getFont().deriveFont(java.awt.Font.BOLD, 11.5f));
        lbl.setForeground(widget.ThemeConfig.TEXT);
        if(area!=null){
            area.setPreferredSize(new java.awt.Dimension(300, tinggiArea));
        }
        p.add(lbl, java.awt.BorderLayout.NORTH);
        if(area!=null){
            p.add(area, java.awt.BorderLayout.CENTER);
        }
        return p;
    }

    private widget.PanelBiasa buatDualPaneMedis(String labelKiri, java.awt.Component compKiri,
            String labelKanan, java.awt.Component compKanan, int tinggiArea){
        widget.PanelBiasa panel = new widget.PanelBiasa(new java.awt.GridLayout(1, 2, 14, 0));
        panel.setOpaque(false);
        panel.add(selAreaTeksMedis(labelKiri, compKiri, tinggiArea));
        panel.add(selAreaTeksMedis(labelKanan, compKanan, tinggiArea));
        return panel;
    }

    /** [Argon] Pemeriksaan Penunjang Medis:
     *  Grid 2x2 elegan RS Pemerintah (Laborat, Radiologi, EKG Split + Foto/Viewer, Pemeriksaan Lainnya)
     *  Lengkap dengan status badges, preset chips, sync buttons, dan toolbar tarik semua.
     *  Keempat textarea tetap memakai instance legacy yang sama sehingga kompatibilitas data 100% terjaga. */
    private widget.PanelBiasa siapkanKontenPenunjangMedis(widget.PanelBiasa pitaLama){
        widget.PanelBiasa panel = new widget.PanelBiasa(new java.awt.BorderLayout());
        latarMedis(panel, widget.ThemeConfig.SURFACE);
        Laborat.setPreferredSize(null);
        Laborat.setMinimumSize(null);
        Radiologi.setPreferredSize(null);
        Radiologi.setMinimumSize(null);
        EKG.setPreferredSize(null);
        EKG.setMinimumSize(null);
        PemeriksaanLainnya.setPreferredSize(null);
        PemeriksaanLainnya.setMinimumSize(null);

        panelPenunjangMedis = new PanelPenunjangMedis(Laborat, Radiologi, EKG, PemeriksaanLainnya);
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
        panelPenunjangMedis.btnCekEkg.addActionListener(e -> {
            tarikDataEKG(false);
            if (panelPenunjangMedis != null) panelPenunjangMedis.perbaruiSemuaBadge();
        });

        panel.add(panelPenunjangMedis, java.awt.BorderLayout.CENTER);
        return panel;
    }

    /** Rencana terapi dilengkapi toolbar Master Template dan Preset Cepat,
     *  tetap memakai instance textarea legacy yang sama sehingga kompatibilitas data terjaga. */
    private widget.PanelBiasa siapkanKontenRencanaMedis(widget.PanelBiasa pitaLama){
        widget.PanelBiasa panel = new widget.PanelBiasa(new java.awt.GridBagLayout());
        latarMedis(panel, widget.ThemeConfig.SURFACE);
        int row = tambahBarisHalamanLegacyMedis(panel, 0, "Rencana / Tindakan / Terapi", siapkanAreaRencanaTerapiMedis(), true);
        tambahGlueVertikalMedis(panel, row);
        return panel;
    }

    private void susunHalamanMedis(){
        kontenSumberMedis = new widget.PanelBiasa[20];
        kontenSumberMedis[0] = panelRm032Atas;
        java.util.LinkedHashMap<javax.swing.JComponent,widget.PanelBiasa> potongAtas = new java.util.LinkedHashMap<>();
        for(int i=1;i<=5;i++){
            kontenSumberMedis[i] = new widget.PanelBiasa(new java.awt.GridBagLayout());
            potongAtas.put(seksiMedis.get(i).strip, kontenSumberMedis[i]);
        }
        pecahBarisPanelMedis(panelRm032Atas, potongAtas);

        // Dua section legacy sesudah Survei Primer sudah direparent ke panel
        // GridBag terstruktur, bukan lagi dipotong dari koordinat absolut GEN.
        kontenSumberMedis[6] = panelRiwayatKesehatanMedis;
        kontenSumberMedis[7] = panelKeadaanUmumMedis;
        kontenSumberMedis[8] = siapkanKontenStatusLokalisMedis(
                ekstrakPitaYMedis(jLabel100.getY(), panelRm032Tengah.getY()));

        kontenSumberMedis[9] = panelRm032Tengah;
        java.util.LinkedHashMap<javax.swing.JComponent,widget.PanelBiasa> potongTengah = new java.util.LinkedHashMap<>();
        for(int i=10;i<=12;i++){
            kontenSumberMedis[i] = new widget.PanelBiasa(new java.awt.GridBagLayout());
            potongTengah.put(seksiMedis.get(i).strip, kontenSumberMedis[i]);
        }
        pecahBarisPanelMedis(panelRm032Tengah, potongTengah);
        // Dua section narasi memakai objek textarea dan ScrollPane yang sama,
        // tetapi mengikuti label kiri/field kanan sesuai mockup.
        kontenSumberMedis[11] = siapkanKontenNarasiMedis("Jantung", TJantung,
                "Paru", TParu);
        kontenSumberMedis[12] = siapkanKontenNarasiMedis("Punggung & Pinggang",
                TPunggungPinggang, "Ekstremitas", TEkstremitasPelvis);

        kontenSumberMedis[13] = siapkanKontenPenunjangMedis(
                ekstrakPitaYMedis(jLabel101.getY(), panelRm032Diagnosis.getY()));
        kontenSumberMedis[14] = panelRm032Diagnosis;
        kontenSumberMedis[15] = siapkanKontenRencanaMedis(
                ekstrakPitaYMedis(jLabel103.getY(), panelRm032.getY()));

        kontenSumberMedis[16] = panelRm032;
        java.util.LinkedHashMap<javax.swing.JComponent,widget.PanelBiasa> potongBawah = new java.util.LinkedHashMap<>();
        for(int i=17;i<=19;i++){
            kontenSumberMedis[i] = new widget.PanelBiasa(new java.awt.GridBagLayout());
            potongBawah.put(seksiMedis.get(i).strip, kontenSumberMedis[i]);
        }
        pecahBarisPanelMedis(panelRm032, potongBawah);

        FormInput.remove(panelRm032Atas);
        FormInput.remove(panelRm032Tengah);
        FormInput.remove(panelRm032Diagnosis);
        FormInput.remove(panelRm032);
        for(int i=0;i<kontenSumberMedis.length;i++){
            seksiMedis.get(i).strip.setVisible(false);
            rapikanKontenBagianMedis(kontenSumberMedis[i], i);
        }
        halamanMedisSiap = true;
        susunKelompokHalamanMedis();
    }

    /** Bentuk 12 kelompok umum, dengan satu kelompok APGAR/PAT tambahan untuk bayi baru lahir. */
    private void susunKelompokHalamanMedis(){
        if(!halamanMedisSiap || kontenSumberMedis==null || menyusunHalamanMedis){ return; }
        menyusunHalamanMedis = true;
        try{
            int aktifLama = Math.max(0, halamanAktifMedis);
            for(HalamanMedis lama : halamanMedis){ FormInput.remove(lama.konten); }
            halamanMedis.clear();

            boolean bayi = "Bayi Baru Lahir".equals(KategoriPasien.getSelectedItem());
            tambahKelompokHalamanMedis("Intervensi Pre Hospital", "Tindakan sebelum tiba di IGD", new int[]{0});
            tambahKelompokHalamanMedis("Survei Primer", "Jalan napas, pernapasan, sirkulasi, disabilitas, dan eksposur", new int[]{1,2,3,4,5});
            if(bayi){
                tambahKelompokBayiMedis();
            }
            tambahKelompokHalamanMedis("Riwayat Kesehatan", "Keluhan dan riwayat kesehatan pasien", new int[]{6});
            tambahKelompokHalamanMedis("Keadaan Umum & Pemeriksaan Fisik", "Keadaan umum, tanda vital, dan pemeriksaan fisik head-to-toe", new int[]{7});
            tambahKelompokHalamanMedis("Status Lokalis", "Pemetaan temuan pada tubuh", new int[]{8});
            tambahKelompokHalamanMedis("Pemeriksaan Penunjang", "EKG, radiologi, laboratorium, dan pemeriksaan lain", new int[]{13});
            tambahKelompokHalamanMedis("Diagnosis", "Diagnosis kerja dan diagnosis banding", new int[]{14});
            tambahKelompokHalamanMedis("Rencana / Tindakan / Terapi", "Rencana terapi, konsultasi, dan edukasi", new int[]{15,16,17});
            tambahKelompokHalamanMedis("Kondisi Saat Ini / Keluar IGD & Tanda Vital Terakhir", "Kondisi pasien dan tanda vital sebelum pindah atau keluar IGD", new int[]{18,19});

            for(int i=0;i<halamanMedis.size();i++){
                HalamanMedis h = halamanMedis.get(i);
                h.konten = buatKartuHalamanMedis(h, h.isi, i);
                FormInput.add(h.konten);
            }
            halamanAktifMedis = Math.min(aktifLama, halamanMedis.size()-1);
            buangKotakBawaanMedis(FormInput);
            terapkanModeHalamanMedis();
            if(railMedis!=null){ isiRailMedis(); }
            hitungProgresMedis();
        } finally {
            menyusunHalamanMedis = false;
        }
    }

    private void tambahKelompokHalamanMedis(String judul, String subjudul, int[] sumber){
        HalamanMedis h = new HalamanMedis(judul, subjudul);
        h.sumber = sumber;
        h.isi = bungkusKontenKelompokMedis(sumber, null);
        halamanMedis.add(h);
    }

    private void tambahKelompokBayiMedis(){
        HalamanMedis h = new HalamanMedis("Penilaian Bayi Baru Lahir", "Penilaian APGAR dan segitiga PAT");
        h.isi = bungkusKontenKelompokMedis(new int[0], panelBayiBaruLahir);
        halamanMedis.add(h);
    }

    private widget.PanelBiasa buatSubDividerMedis(String teksJudul){
        widget.PanelBiasa p = new widget.PanelBiasa(new java.awt.BorderLayout(0, 4));
        p.setOpaque(false);
        widget.Label lbl = labelRm032(teksJudul);
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        lbl.setForeground(widget.ThemeConfig.TEXT_MUTED);
        p.add(lbl, java.awt.BorderLayout.CENTER);
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, widget.ThemeConfig.BORDER_SOFT),
                javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return p;
    }

    private String getSubJudulSumberMedis(int sumberIdx){
        switch(sumberIdx){
            case 1: return "A — AIRWAY (JALAN NAPAS)";
            case 2: return "B — BREATHING (PERNAPASAN)";
            case 3: return "C — CIRCULATION (SIRKULASI)";
            case 4: return "D — DISABILITY (DISABILITAS)";
            case 5: return "E — EXPOSURE (EKSPOSUR)";
            case 9: return "KEPALA";
            case 10: return "LEHER RINCI";
            case 15: return "RENCANA / TINDAKAN / TERAPI";
            case 16: return "KONSUL DOKTER SPESIALIS";
            case 17: return "EDUKASI PASIEN / KELUARGA";
            case 18: return "KONDISI SAAT INI / KELUAR IGD";
            case 19: return "TANDA VITAL TERAKHIR";
            default:
                if(sumberIdx >= 0 && sumberIdx < seksiMedis.size()){
                    return seksiMedis.get(sumberIdx).judul;
                }
                return "";
        }
    }

    private widget.PanelBiasa bungkusKontenKelompokMedis(int[] sumber, widget.PanelBiasa tambahan){
        widget.PanelBiasa isi = new widget.PanelBiasa(new java.awt.GridBagLayout());
        latarMedis(isi, widget.ThemeConfig.SURFACE);
        int y=0;
        boolean multiSumber = sumber!=null && sumber.length > 1;
        if(sumber!=null){
            for(int i : sumber){
                if(multiSumber){
                    String subJudul = getSubJudulSumberMedis(i);
                    if(!subJudul.isEmpty()){
                        widget.PanelBiasa divider = buatSubDividerMedis(subJudul);
                        java.awt.GridBagConstraints cDiv = new java.awt.GridBagConstraints();
                        cDiv.gridx = 0; cDiv.gridy = y++; cDiv.weightx = 1.0;
                        cDiv.fill = java.awt.GridBagConstraints.HORIZONTAL;
                        cDiv.insets = new java.awt.Insets(y > 1 ? 16 : 4, 0, 4, 0);
                        isi.add(divider, cDiv);
                    }
                }
                java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
                c.gridx=0; c.gridy=y++; c.weightx=1.0; c.fill=java.awt.GridBagConstraints.HORIZONTAL;
                c.insets = new java.awt.Insets(0,0,0,0);
                isi.add(kontenSumberMedis[i],c);
            }
        }
        if(tambahan!=null){
            java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
            c.gridx=0; c.gridy=y; c.weightx=1.0; c.fill=java.awt.GridBagConstraints.HORIZONTAL;
            isi.add(tambahan,c);
        }
        return isi;
    }

    /**
     * Membuat isi tiap bagian memenuhi lebar kartu seperti referensi. Ini
     * hanya mengubah constraint layout; objek field dan listener tetap sama.
     */
    private void rapikanKontenBagianMedis(widget.PanelBiasa panel, int bagian){
        latarMedis(panel, widget.ThemeConfig.SURFACE);
        if(!(panel.getLayout() instanceof java.awt.GridBagLayout)){ return; }
        java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) panel.getLayout();
        java.awt.Component kandidatIntervensi = null;
        java.awt.Component checklistJalanNapas = null;
        java.util.List<java.awt.Component> checklistSurveiPrimer = new java.util.ArrayList<>();
        for(java.awt.Component komponen : panel.getComponents()){
            java.awt.GridBagConstraints c = gbl.getConstraints(komponen);
            if(komponen==seksiMedis.get(bagian).strip){ continue; }
            if(bagian==0 && c.gridy==1 && c.gridwidth==2){
                kandidatIntervensi = komponen;
                continue;
            }
            if(komponen instanceof javax.swing.JComponent && Boolean.TRUE.equals(
                    ((javax.swing.JComponent)komponen).getClientProperty("checklistJalanNapas"))){
                checklistJalanNapas = komponen;
                // Judul Airway tetap di kolom kiri seperti mockup, sedangkan
                // checklist berada utuh pada kolom field sebelah kanan.
                c.gridx = 1;
                c.gridwidth = 1;
                c.anchor = java.awt.GridBagConstraints.WEST;
                c.fill = java.awt.GridBagConstraints.HORIZONTAL;
                c.weightx = 1.0;
                c.insets = new java.awt.Insets(9,10,9,10);
                gbl.setConstraints(komponen, c);
                continue;
            }
            if(komponen instanceof javax.swing.JComponent && Boolean.TRUE.equals(
                    ((javax.swing.JComponent)komponen).getClientProperty("checklistSurveiPrimer"))){
                checklistSurveiPrimer.add(komponen);
                // Checklist Survei Primer selalu menjadi field di kolom kanan;
                // label utama tetap berada di kolom kiri seperti Airway.
                c.gridx = 1;
                c.gridwidth = 1;
                c.anchor = java.awt.GridBagConstraints.WEST;
                c.fill = java.awt.GridBagConstraints.HORIZONTAL;
                c.weightx = 1.0;
                c.insets = new java.awt.Insets(9,10,9,10);
                gbl.setConstraints(komponen, c);
                continue;
            }
            // Field obat Section 1 adalah isian pendek pada mockup, bukan
            // narasi yang mengisi seluruh lebar kolom kartu.
            if(bagian==0 && komponen==TPreObatKet){
                c.anchor = java.awt.GridBagConstraints.WEST;
                c.fill = java.awt.GridBagConstraints.NONE;
                c.weightx = 0.0;
                c.insets = new java.awt.Insets(9,10,9,10);
                gbl.setConstraints(komponen, c);
                continue;
            }
            // Field Meningeal Signs mengikuti lebar isian pendek pada mockup
            // Disabilitas, bukan narasi yang memenuhi kartu.
            if(bagian==4 && komponen==TMeningealSigns){
                c.anchor = java.awt.GridBagConstraints.WEST;
                c.fill = java.awt.GridBagConstraints.NONE;
                c.weightx = 0.0;
                c.insets = new java.awt.Insets(9,10,9,10);
                gbl.setConstraints(komponen, c);
                continue;
            }
            c.insets = new java.awt.Insets(9,10,9,10);
            if(c.gridx==0 && komponen instanceof javax.swing.JLabel && c.gridwidth==1){
                java.awt.Dimension lama = komponen.getPreferredSize();
                komponen.setPreferredSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, Math.max(23, lama.height)));
                komponen.setMinimumSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, Math.max(23, lama.height)));
                c.anchor = java.awt.GridBagConstraints.NORTHWEST;
                c.weightx = 0.0;
                if(bagian==9 || bagian==10){
                    javax.swing.JLabel label = (javax.swing.JLabel)komponen;
                    label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                    label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD));
                    label.setBorder(javax.swing.BorderFactory.createMatteBorder(
                            0,0,1,0,widget.ThemeConfig.BORDER_SOFT));
                }
            }
            if((bagian==9 || bagian==10) && c.gridx==1){
                c.anchor = java.awt.GridBagConstraints.WEST;
                c.fill = java.awt.GridBagConstraints.HORIZONTAL;
                if(komponen instanceof javax.swing.JComponent){
                    ((javax.swing.JComponent)komponen).setBorder(javax.swing.BorderFactory.createMatteBorder(
                            0,0,1,0,widget.ThemeConfig.BORDER_SOFT));
                }
            }
            if(komponen instanceof javax.swing.JComponent && Boolean.TRUE.equals(
                    ((javax.swing.JComponent)komponen).getClientProperty("fieldSurveiPrimerRingkas"))){
                // Hgap FlowLayout memberi jarak antar radio; inset kiri
                // dikompensasi agar awal field tetap sejajar checklist.
                c.insets = new java.awt.Insets(9,0,9,10);
            }
            if(c.gridx==1 || c.gridwidth>1){
                c.weightx = 1.0;
                c.fill = java.awt.GridBagConstraints.HORIZONTAL;
            }
            gbl.setConstraints(komponen, c);
        }
        for(java.awt.Component checklist : checklistSurveiPrimer){
            rapikanChecklistSurveiPrimerMedis((java.awt.Container) checklist);
        }
        if(checklistJalanNapas!=null){
            rapikanChecklistJalanNapasMedis((java.awt.Container) checklistJalanNapas);
        }
        if(kandidatIntervensi!=null){
            java.awt.GridBagConstraints ci = gbl.getConstraints(kandidatIntervensi);
            ci.gridx=1; ci.gridwidth=1; ci.weightx=1.0;
            ci.fill=java.awt.GridBagConstraints.HORIZONTAL;
            ci.insets=new java.awt.Insets(9,10,9,10);
            gbl.setConstraints(kandidatIntervensi, ci);

            widget.Label label = new widget.Label();
            label.setText("<html><b>Intervensi Diberikan</b> <font color='#C2413B'>*</font></html>");
            label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            label.setForeground(widget.ThemeConfig.TEXT);
            label.setPreferredSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, 23));
            label.setMinimumSize(new java.awt.Dimension(LEBAR_LABEL_KIRI_MEDIS, 23));
            java.awt.GridBagConstraints cl = new java.awt.GridBagConstraints();
            cl.gridx=0; cl.gridy=ci.gridy; cl.anchor=java.awt.GridBagConstraints.NORTHWEST;
            cl.weightx = 0.0;
            cl.insets=new java.awt.Insets(13,10,9,10);
            panel.add(label, cl);
            rapikanChecklistIntervensiMedis((java.awt.Container) kandidatIntervensi);
        }
    }

    /**
     * Checklist Section 1 mengikuti mockup: dua kolom dengan sel seimbang
     * yang mengisi area field. Checklist lain sengaja tetap memakai chip
     * selebar teks melalui pelukChipGridMedis().
     */
    private void rapikanChecklistIntervensiMedis(java.awt.Container box){
        if(!(box.getLayout() instanceof java.awt.GridBagLayout)){ return; }
        java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) box.getLayout();
        for(java.awt.Component komponen : box.getComponents()){
            if(!(komponen instanceof javax.swing.JToggleButton)){ continue; }
            java.awt.GridBagConstraints c = gbl.getConstraints(komponen);
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.anchor = java.awt.GridBagConstraints.WEST;
            // Penanda hanya untuk harness: chip Section 1 memang sengaja
            // selebar sel kolom sesuai mockup, tidak dihitung sebagai melar.
            ((javax.swing.JComponent)komponen).putClientProperty("chipIntervensiLebar", Boolean.TRUE);
            gbl.setConstraints(komponen, c);
        }
    }

    /** Jalan Napas memakai struktur visual Intervensi Pre Hospital: dua
     *  kolom sama lebar memenuhi area field, dengan ruang kosong hanya pada
     *  sel ke-12 yang memang tidak memiliki pilihan. */
    private void rapikanChecklistJalanNapasMedis(java.awt.Container box){
        if(!(box.getLayout() instanceof java.awt.GridBagLayout)){ return; }
        java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) box.getLayout();
        gbl.columnWidths = new int[]{0,0,0};
        gbl.columnWeights = new double[]{1.0,1.0,0.0};
        for(java.awt.Component komponen : box.getComponents()){
            if(!(komponen instanceof javax.swing.JToggleButton)){ continue; }
            java.awt.GridBagConstraints c = gbl.getConstraints(komponen);
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.anchor = java.awt.GridBagConstraints.WEST;
            ((javax.swing.JComponent)komponen)
                    .putClientProperty("chipJalanNapasLebar", Boolean.TRUE);
            gbl.setConstraints(komponen, c);
        }
    }

    /** Checklist klinis Survei Primer memakai dua kolom seimbang seperti
     *  mockup ABCDE. Dipisahkan dari Section 1 agar masing-masing mudah
     *  diverifikasi dan checklist section lain tetap memeluk teks. */
    private void rapikanChecklistSurveiPrimerMedis(java.awt.Container box){
        if(!(box.getLayout() instanceof java.awt.GridBagLayout)){ return; }
        java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) box.getLayout();
        int lebarKolom = 0;
        int jumlahChip = 0;
        for(java.awt.Component komponen : box.getComponents()){
            if(!(komponen instanceof javax.swing.JToggleButton)){ continue; }
            lebarKolom = Math.max(lebarKolom, komponen.getPreferredSize().width);
            jumlahChip++;
        }
        if(jumlahChip==0){ return; }

        // Checklist mengikuti pola Airway/Intervensi: dua kolom seimbang
        // memenuhi area field kanan. Lebar eksplisit menahan GridBagLayout
        // agar tidak membagi ruang utama menjadi kolom ketiga yang kosong.
        int lebarSel = Math.max(280, Math.min(380, lebarKolom));
        int celahKolom = 10;
        int tinggiBaris = 30;
        int jumlahBaris = (jumlahChip+1)/2;
        java.awt.Dimension ukuran = new java.awt.Dimension(
                lebarSel*2+celahKolom,
                jumlahBaris*tinggiBaris + Math.max(0, jumlahBaris-1)*7);
        box.setPreferredSize(ukuran);
        box.setMaximumSize(ukuran);
        gbl.columnWidths = new int[]{lebarSel,lebarSel,0};
        gbl.columnWeights = new double[]{1.0,1.0,0.0};

        for(java.awt.Component komponen : box.getComponents()){
            if(!(komponen instanceof javax.swing.JToggleButton)){ continue; }
            java.awt.GridBagConstraints c = gbl.getConstraints(komponen);
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.anchor = java.awt.GridBagConstraints.WEST;
            ((javax.swing.JComponent)komponen).putClientProperty("chipSurveiPrimerLebar", Boolean.TRUE);
            gbl.setConstraints(komponen, c);
        }
    }

    /**
     * Tumpuk (mode "semua halaman") atau tampilkan satu (mode "satu
     * halaman") kartu halaman di FormInput (tetap berlayout null).
     * Dipanggil ulang tiap ganti mode atau ganti halaman aktif.
     */
    private void terapkanModeHalamanMedis(){
        if(halamanMedis.isEmpty()){ return; }
        int lebar = lebarHalamanResponsifMedis();
        lebarHalamanAktualMedis = lebar;
        if(satuHalamanMedis){
            int aktif = Math.min(Math.max(0, halamanAktifMedis), halamanMedis.size() - 1);
            for(int i=0;i<halamanMedis.size();i++){
                HalamanMedis h = halamanMedis.get(i);
                boolean aktifIni = (i==aktif);
                if(h.konten != null){
                    h.konten.setVisible(aktifIni);
                    if(aktifIni){ h.konten.setBounds(MARGIN_HALAMAN_MEDIS,16,lebar,h.konten.getPreferredSize().height); }
                }
            }
            if(aktif >= 0 && aktif < halamanMedis.size()){
                javax.swing.JComponent kontenAktif = halamanMedis.get(aktif).konten;
                if(kontenAktif != null){
                    FormInput.setPreferredSize(new java.awt.Dimension(
                            lebar+(MARGIN_HALAMAN_MEDIS*2), kontenAktif.getPreferredSize().height+32));
                }
            }
        } else {
            int runningY = 16;
            for(HalamanMedis h : halamanMedis){
                if(h.konten != null){
                    h.konten.setVisible(true);
                    h.konten.setBounds(MARGIN_HALAMAN_MEDIS, runningY, lebar, h.konten.getPreferredSize().height);
                    runningY += h.konten.getPreferredSize().height + 18;
                }
            }
            FormInput.setPreferredSize(new java.awt.Dimension(
                    lebar+(MARGIN_HALAMAN_MEDIS*2), runningY));
        }
        FormInput.revalidate();
        FormInput.repaint();
    }

    /**
     * Kartu memakai lebar viewport yang tersedia agar scroll horizontal tidak
     * diperlukan. Pada layar lebar ukuran tetap dibatasi 1230 px supaya ritme
     * baca sama dengan handoff; pada layar lebih kecil kartu ikut menyusut.
     */
    private int lebarHalamanResponsifMedis(){
        if(scrollInput==null || scrollInput.getViewport()==null){
            return LEBAR_HALAMAN_MEDIS;
        }
        int lebarViewport = scrollInput.getViewport().getExtentSize().width;
        if(lebarViewport<=0){ return LEBAR_HALAMAN_MEDIS; }
        return Math.max(1, Math.min(LEBAR_HALAMAN_MEDIS,
                lebarViewport-(MARGIN_HALAMAN_MEDIS*2)));
    }

    /** Hitung ulang bounds hanya ketika lebar viewport benar-benar berubah. */
    private void sesuaikanLebarHalamanMedis(){
        if(halamanMedis.isEmpty()){ return; }
        int lebarBaru = lebarHalamanResponsifMedis();
        if(lebarBaru==lebarHalamanAktualMedis){ return; }
        terapkanModeHalamanMedis();
    }

    /**
     * Toggle "Semua halaman" <-> "Satu halaman", meniru
     * buatToggleModeKep() milik Keperawatan - dua widget.RadioButton
     * digayakan jadi chip pil lewat gayaChipMedis() (Sesi 3a), bukan dua
     * widget.Button (Button melukis latarnya sendiri, sulit diberi gaya
     * chip terpilih/tidak).
     */
    private widget.PanelBiasa buatToggleModeMedis(){
        widget.PanelBiasa panel = new widget.PanelBiasa(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,6,0));
        panel.setOpaque(false);

        final widget.RadioButton rbSemua = new widget.RadioButton();
        rbSemua.setText("Semua halaman");
        final widget.RadioButton rbSatu = new widget.RadioButton();
        rbSatu.setText("Satu halaman");
        javax.swing.ButtonGroup grup = new javax.swing.ButtonGroup();
        grup.add(rbSemua); grup.add(rbSatu);
        rbSatu.setSelected(satuHalamanMedis);
        rbSemua.setSelected(!satuHalamanMedis);

        rbSemua.addActionListener((java.awt.event.ActionEvent e) -> {
            satuHalamanMedis = false;
            terapkanModeHalamanMedis();
            keHalamanMedis(Math.max(0, halamanAktifMedis));
        });
        rbSatu.addActionListener((java.awt.event.ActionEvent e) -> {
            satuHalamanMedis = true;
            terapkanModeHalamanMedis();
            keHalamanMedis(Math.max(0, halamanAktifMedis));
        });

        panel.add(rbSemua);
        panel.add(rbSatu);
        gayaChipMedis(panel);
        return panel;
    }

    /**
     * [Bagian C+D] Satu baris rail = panel yg bisa diklik (bukan widget.Button).
     *
     * Dipilih panel + MouseListener supaya keadaan aktif/hover dan batang
     * aksen kiri sepenuhnya dikendalikan di sini; widget.Button melukis
     * latarnya sendiri lewat glass color dan sulit diberi batang aksen.
     */
    private widget.PanelBiasa buatBarisRailMedis(final int idx){
        final HalamanMedis h = halamanMedis.get(idx);
        widget.PanelBiasa baris = new widget.PanelBiasa();
        latarMedis(baris, widget.ThemeConfig.SURFACE);
        baris.setLayout(new java.awt.BorderLayout(0, 0));
        baris.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 0, 1, 0));
        baris.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        // Batang aksen kiri 3px - penanda halaman aktif tanpa mengandalkan
        // warna latar saja (pembeda tetap terbaca di layar berkontras rendah).
        widget.PanelBiasa aksen = new widget.PanelBiasa();
        latarMedis(aksen, widget.ThemeConfig.SURFACE);
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
        lblJudul.setToolTipText(h.judul);
        lblJudul.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        lblJudul.setForeground(widget.ThemeConfig.TEXT_NORMAL);
        lblJudul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        // Sesi 5 (Bagian E): label hitungan "x/y" wajib terisi, diisi/
        // diwarnai oleh hitungProgresMedis().
        widget.Label lblHitung = new widget.Label();
        lblHitung.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 11));
        lblHitung.setForeground(widget.ThemeConfig.TEXT_FAINT);
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

        java.awt.event.MouseAdapter klikRail = new java.awt.event.MouseAdapter(){
            @Override public void mouseClicked(java.awt.event.MouseEvent e){
                sorotHalamanMedis(idx);
                keHalamanMedis(idx);
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e){
                if(idx!=halamanAktifMedis){
                    latarMedis(h.baris, widget.ThemeConfig.SURFACE_ALT);
                    h.baris.repaint();
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e){
                if(idx!=halamanAktifMedis){
                    latarMedis(h.baris, widget.ThemeConfig.SURFACE);
                    h.baris.repaint();
                }
            }
        };
        // Label dan hitungan adalah komponen anak yang menerima mouse event
        // sendiri; listener harus dipasang pada seluruh area baris agar klik
        // judul, nomor, angka x/y, maupun ruang kosong sama-sama berpindah halaman.
        java.awt.Component[] areaKlik = {baris, aksen, isi, lblNomor, lblJudul, lblHitung};
        for(java.awt.Component komponen : areaKlik){
            komponen.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            komponen.addMouseListener(klikRail);
        }
        return baris;
    }

    /** [Bagian C+D] Pindahkan sorotan rail ke halaman ke-idx (tidak menggulung/ganti mode). */
    private void sorotHalamanMedis(int idx){
        if(idx<0 || idx>=halamanMedis.size()){ return; }
        for(int i=0;i<halamanMedis.size();i++){
            HalamanMedis h = halamanMedis.get(i);
            if(h.baris==null){ continue; }
            latarMedis(h.baris, widget.ThemeConfig.SURFACE);
            latarMedis(h.aksen, widget.ThemeConfig.SURFACE);
            if(h.lblJudul != null){
                h.lblJudul.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
                h.lblJudul.setForeground(widget.ThemeConfig.TEXT_NORMAL);
            }
            if(h.lblNomor != null){
                h.lblNomor.setForeground(widget.ThemeConfig.TEXT_FAINT);
            }
            h.baris.repaint();
        }
        halamanAktifMedis = idx;
        HalamanMedis h = halamanMedis.get(idx);
        if(h.baris != null){
            latarMedis(h.baris, new java.awt.Color(0xF2, 0xF7, 0xFB));
            latarMedis(h.aksen, widget.ThemeConfig.PRIMARY);
            if(h.lblJudul != null){
                h.lblJudul.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
                h.lblJudul.setForeground(widget.ThemeConfig.PRIMARY);
            }
            if(h.lblNomor != null){
                h.lblNomor.setForeground(widget.ThemeConfig.PRIMARY);
            }
            h.baris.repaint();
        }
        if(lblProgresJudulMedis!=null){
            lblProgresJudulMedis.setText("<html><div style='width:"+LEBAR_TEKS_RAIL_MEDIS+"px'>"
                    + escHtmlMedis(h.judul) + "</div></html>");
            lblProgresHitungMedis.setText("Halaman " + (idx+1) + " dari " + halamanMedis.size());
        }
        railMedis.revalidate();
        railMedis.repaint();
    }

    /**
     * [Bagian D] Pindah ke halaman ke-idx: mode "satu halaman" langsung
     * tukar kartu yg tampil (tanpa perlu scroll - selalu mulai dari
     * atas); mode "semua halaman" tetap menggulung FormInput seperti
     * kePosisiSeksiMedis() lama, memakai bounds kartu (anak LANGSUNG
     * FormInput, jadi tak perlu convertRectangle lintas induk lagi).
     */
    private void keHalamanMedis(int idx){
        if(idx<0 || idx>=halamanMedis.size()){ return; }
        halamanAktifMedis = idx;
        if(satuHalamanMedis){
            terapkanModeHalamanMedis();
        } else {
            HalamanMedis h = halamanMedis.get(idx);
            java.awt.Rectangle r = new java.awt.Rectangle(h.konten.getBounds());
            r.height = Math.min(r.height + TINGGI_KONTEKS_MEDIS, Math.max(1, FormInput.getHeight()-r.y));
            FormInput.scrollRectToVisible(r);
        }
        sorotHalamanMedis(idx);
    }

    /**
     * [Bagian C] Kartu progres di kepala rail.
     *
     * Sengaja HANYA menampilkan posisi halaman ("Halaman N dari 10"),
     * bukan hitungan isian wajib/opsional/persentase - itu Bagian E (sesi
     * lain); menaruh angka yg belum dihitung apa pun lebih menyesatkan
     * daripada tidak ada. Keputusan sama persis dgn Keperawatan Sesi 2.
     */
    private widget.PanelBiasa buatKartuProgresMedis(){
        widget.PanelBiasa kartu = new widget.PanelBiasa(new java.awt.GridBagLayout());
        latarMedis(kartu, widget.ThemeConfig.SURFACE_ALT);
        kartu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
            javax.swing.BorderFactory.createEmptyBorder(10,12,10,12)));

        widget.Label judul = new widget.Label();
        judul.setText("HALAMAN AKTIF");
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

    /**
     * Sesi 5 (Bagian E): kartu progres ISIAN WAJIB global - persen +
     * progress bar + hitungan, dihitung ulang oleh hitungProgresMedis().
     * Kartu BARU terpisah dari buatKartuProgresMedis() (posisi halaman) -
     * lihat catatan "Perbedaan struktural" di plan Sesi 5.
     */
    private widget.PanelBiasa buatKartuWajibMedis(){
        widget.PanelBiasa kartu = new widget.PanelBiasa(new java.awt.GridBagLayout());
        latarMedis(kartu, widget.ThemeConfig.SURFACE_ALT);
        kartu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(widget.ThemeConfig.BORDER_SOFT),
            javax.swing.BorderFactory.createEmptyBorder(10,12,10,12)));

        lblWajibJudulMedis = new widget.Label();
        lblWajibJudulMedis.setText("ISIAN WAJIB");
        lblWajibJudulMedis.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblWajibJudulMedis.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
        lblWajibJudulMedis.setForeground(widget.ThemeConfig.TEXT_MUTED);

        lblWajibPersenMedis = new widget.Label();
        lblWajibPersenMedis.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblWajibPersenMedis.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 16));
        lblWajibPersenMedis.setForeground(widget.ThemeConfig.TEXT);
        widget.PanelBiasa kepala = new widget.PanelBiasa(new java.awt.BorderLayout());
        kepala.setOpaque(false);
        kepala.add(lblWajibJudulMedis, java.awt.BorderLayout.WEST);
        kepala.add(lblWajibPersenMedis, java.awt.BorderLayout.EAST);

        pbWajibMedis = new javax.swing.JProgressBar(0,100);
        pbWajibMedis.setPreferredSize(new java.awt.Dimension(10,6));
        pbWajibMedis.setBorderPainted(false);
        pbWajibMedis.setForeground(widget.ThemeConfig.PRIMARY);
        pbWajibMedis.setBackground(widget.ThemeConfig.BORDER_SOFT);

        lblWajibHitungMedis = new widget.Label();
        lblWajibHitungMedis.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblWajibHitungMedis.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        lblWajibHitungMedis.setForeground(widget.ThemeConfig.TEXT_MUTED);

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx=0; c.weightx=1.0; c.anchor=java.awt.GridBagConstraints.WEST;
        c.fill=java.awt.GridBagConstraints.HORIZONTAL;
        c.gridy=0; kartu.add(kepala,c);
        c.gridy=1; c.insets=new java.awt.Insets(8,0,0,0); kartu.add(pbWajibMedis,c);
        c.gridy=2; c.insets=new java.awt.Insets(7,0,0,0); kartu.add(lblWajibHitungMedis,c);
        return kartu;
    }

    /** Isi rail sesuai referensi: progres global, judul, lalu halaman klinis. */
    private void isiRailMedis(){
        railMedis.removeAll();
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx=0; c.weightx=1.0; c.fill=java.awt.GridBagConstraints.HORIZONTAL;
        c.anchor=java.awt.GridBagConstraints.NORTH;

        widget.Label judulBagian = new widget.Label();
        judulBagian.setText("BAGIAN PENGKAJIAN");
        judulBagian.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        judulBagian.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
        judulBagian.setForeground(widget.ThemeConfig.TEXT_MUTED);
        judulBagian.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,8,6,8));
        c.gridy=0; c.insets=new java.awt.Insets(12,14,0,14);
        railMedis.add(judulBagian, c);

        c.insets=new java.awt.Insets(0,8,0,8);
        int gy = 1;
        for(int i=0;i<halamanMedis.size();i++){
            c.gridy=gy++;
            railMedis.add(buatBarisRailMedis(i), c);
        }
        // Pengisi vertikal - tanpa ini GridBagLayout menengahkan seluruh
        // daftar saat rail lebih tinggi dari isinya.
        c.gridy=gy; c.weighty=1.0; c.fill=java.awt.GridBagConstraints.BOTH;
        c.insets=new java.awt.Insets(0,0,0,0);
        railMedis.add(javax.swing.Box.createGlue(), c);

        if(!halamanMedis.isEmpty()){ sorotHalamanMedis(Math.max(0,halamanAktifMedis)); }
    }

    /**
     * Sesi 5 (Bagian E): enable/disable seluruh isi halaman saat ditandai
     * tidak relevan - flag UI/session saja, TIDAK disimpan ke DB. Beda
     * dari setHalamanRelevanKep milik Keperawatan yg meloop h.anggota
     * (daftar sub-seksi): HalamanMedis cuma punya SATU JComponent konten
     * per halaman, jadi cukup dipanggil sekali atas isi itu langsung.
     */
    private void setHalamanRelevanMedis(HalamanMedis h, javax.swing.JComponent isi, boolean relevan){
        if(isi != null){
            setKomponenEnabledRekursifMedis(isi, relevan);
        }
        if(h.konten != null){
            widget.PanelBiasa kartu = (h.konten instanceof widget.PanelBiasa) ? (widget.PanelBiasa) h.konten : null;
            if(kartu != null){
                latarMedis(kartu, relevan ? widget.ThemeConfig.SURFACE : new java.awt.Color(0xF6,0xF7,0xF4));
                kartu.repaint();
            }
        }
    }

    /** Salinan persis setKomponenEnabledRekursif milik Keperawatan. */
    private void setKomponenEnabledRekursifMedis(java.awt.Container c, boolean enabled){
        for(java.awt.Component comp : c.getComponents()){
            if(comp instanceof javax.swing.JComponent && !(comp instanceof widget.Label)){
                comp.setEnabled(enabled);
            }
            if(comp instanceof java.awt.Container){
                setKomponenEnabledRekursifMedis((java.awt.Container) comp, enabled);
            }
        }
    }

    /**
     * Sesi 5 (Bagian E): meniru hitungProgresKep (RMPenilaianAwalKeperawatanIGD,
     * baris 6430-6637) TAPI totalWajibSemua DIHITUNG DINAMIS dari penjumlahan
     * h.totalWajib tiap halaman - bukan konstanta literal terpisah seperti
     * "17" milik Keperawatan, yang berisiko basi diam-diam kalau salah satu
     * case di bawah diubah tanpa mengubah konstanta itu. Tabel field wajib
     * per halaman ada di plan Sesi 5.
     */
    private void hitungProgresMedis(){
        // Dua isian wajib berada di banner (tanggal asesmen dan kategori
        // pasien); sisanya ditampilkan per bagian pada rail.
        int totalWajibSemua = 2;
        int totalTerisiWajib = (TglAsuhan!=null && TglAsuhan.getSelectedItem()!=null ? 1 : 0)
                + (KategoriPasien!=null && KategoriPasien.getSelectedIndex()>=0 ? 1 : 0);
        int totalOpsional = 0;

        for(int i=0;i<halamanMedis.size();i++){
            HalamanMedis h = halamanMedis.get(i);
            int wajib = 0;
            int terisi = 0;

            for(int sumber : h.sumber){
                int[] progresSumber = progresSumberMedis(sumber);
                wajib += progresSumber[0];
                terisi += progresSumber[1];
            }

            h.totalWajib = wajib;
            h.terisiWajib = h.tidakRelevan ? wajib : Math.min(wajib, terisi);
            totalWajibSemua += wajib;
            totalTerisiWajib += h.terisiWajib;
            if(wajib==0){ totalOpsional += terisi; }

            if(h.lblHitunganRail != null){
                if(h.tidakRelevan){
                    h.lblHitunganRail.setText("N/A");
                    h.lblHitunganRail.setForeground(widget.ThemeConfig.TEXT_FAINT);
                } else if(wajib==0){
                    h.lblHitunganRail.setText("-");
                    h.lblHitunganRail.setForeground(widget.ThemeConfig.TEXT_FAINT);
                } else {
                    h.lblHitunganRail.setText(terisi+"/"+wajib);
                    h.lblHitunganRail.setForeground(terisi>=wajib ? widget.ThemeConfig.OK : widget.ThemeConfig.WARN);
                }
            }

            if(h.pnlBadgeStatus != null && h.lblBadgeStatus != null){
                if(h.tidakRelevan){
                    latarMedis(h.pnlBadgeStatus, new java.awt.Color(0xF2,0xF4,0xF7));
                    h.lblBadgeStatus.setText("Tidak relevan");
                    h.lblBadgeStatus.setForeground(widget.ThemeConfig.TEXT_FAINT);
                } else if(wajib==0){
                    latarMedis(h.pnlBadgeStatus, new java.awt.Color(0xF2,0xF4,0xF7));
                    h.lblBadgeStatus.setText("Opsional");
                    h.lblBadgeStatus.setForeground(widget.ThemeConfig.TEXT_FAINT);
                } else if(h.terisiWajib>=wajib){
                    latarMedis(h.pnlBadgeStatus, widget.ThemeConfig.OK_BG);
                    h.lblBadgeStatus.setText("Wajib lengkap");
                    h.lblBadgeStatus.setForeground(widget.ThemeConfig.OK);
                } else {
                    latarMedis(h.pnlBadgeStatus, widget.ThemeConfig.WARN_BG);
                    h.lblBadgeStatus.setText(terisi+"/"+wajib+" wajib terisi");
                    h.lblBadgeStatus.setForeground(widget.ThemeConfig.WARN);
                }
                h.pnlBadgeStatus.repaint();
            }

        }

        int persen = totalWajibSemua>0 ? (totalTerisiWajib*100)/totalWajibSemua : 100;
        if(lblWajibPersenMedis != null){ lblWajibPersenMedis.setText(persen+"%"); }
        if(pbWajibMedis != null){
            pbWajibMedis.setValue(persen);
            pbWajibMedis.setForeground(persen>=100 ? widget.ThemeConfig.OK : widget.ThemeConfig.PRIMARY);
        }
        if(lblWajibJudulMedis != null){
            lblWajibJudulMedis.setText(persen>=100 ? "SEMUA ISIAN WAJIB TERPENUHI" : "ISIAN WAJIB");
        }
        if(lblWajibHitungMedis != null){
            lblWajibHitungMedis.setText(totalTerisiWajib+"/"+totalWajibSemua+" isian wajib · "+totalOpsional+" opsional terisi");
        }
        if(lblProgresBawahMedis != null){
            if(persen>=100){
                lblProgresBawahMedis.setText("Isian wajib lengkap · siap disimpan");
                lblProgresBawahMedis.setForeground(widget.ThemeConfig.OK);
            } else {
                lblProgresBawahMedis.setText("Isian wajib "+totalTerisiWajib+"/"+totalWajibSemua+" - "+(totalWajibSemua-totalTerisiWajib)+" belum terisi");
                lblProgresBawahMedis.setForeground(widget.ThemeConfig.TEXT_MUTED);
            }
        }
        if(railMedis!=null){
            railMedis.revalidate();
            railMedis.repaint();
        }
    }

    private int[] progresSumberMedis(int i){
        int wajib = 0;
        int terisi = 0;
        switch(i){
                case 0:
                    wajib = 1;
                    if(CekPreCervicalCollar.isSelected() || CekPreDefibrilasi.isSelected() ||
                       CekPreVtp.isSelected() || CekPreBalutBidai.isSelected() ||
                       CekPreNgt.isSelected() || CekPreRjp.isSelected() ||
                       CekPreIntubasi.isSelected() || CekPreDekompresiWsd.isSelected() ||
                       CekPreKateterUrin.isSelected() || CekPreInfus.isSelected() ||
                       CekPreTidakAda.isSelected()){ terisi++; }
                    break;
                case 1:
                    wajib = 2;
                    if(CekJnPaten.isSelected() || CekJnObstruksiPartial.isSelected() ||
                       CekJnStridor.isSelected() || CekJnSnoring.isSelected() ||
                       CekJnGurgling.isSelected() || CekJnWheezing.isSelected() ||
                       CekJnObstruksiTotal.isSelected() || CekJnTraumaJalanNapas.isSelected() ||
                       CekJnFasialLeherInhalasi.isSelected() || CekJnRisikoAspirasi.isSelected() ||
                       CekJnBendaAsing.isSelected()){ terisi++; }
                    if(bgJnKesimpulan!=null && bgJnKesimpulan.getSelection()!=null){ terisi++; }
                    break;
                case 2:
                    wajib = 3;
                    if(bgNapPola!=null && bgNapPola.getSelection()!=null){ terisi++; }
                    if(bgNapReguler!=null && bgNapReguler.getSelection()!=null){ terisi++; }
                    if(bgNapKesimpulan!=null && bgNapKesimpulan.getSelection()!=null){ terisi++; }
                    break;
                case 3:
                    wajib = 4;
                    if(CekSirReguler!=null && (CekSirReguler.isSelected() || CekSirIrreguler.isSelected())){ terisi++; }
                    if(CekSirKuat!=null && (CekSirKuat.isSelected() || CekSirLemah.isSelected())){ terisi++; }
                    if(CekSirKulitNormal!=null && (CekSirKulitNormal.isSelected() || CekSirKulitPucat.isSelected())){ terisi++; }
                    if(CekSirAman!=null && (CekSirAman.isSelected() || CekSirMengancamJiwa.isSelected())){ terisi++; }
                    break;
                case 4:
                    wajib = 2;
                    if(TGcsE!=null && TGcsV!=null && TGcsM!=null &&
                       (!TGcsE.getText().trim().isEmpty() || !TGcsV.getText().trim().isEmpty() || !TGcsM.getText().trim().isEmpty())){ terisi++; }
                    if(bgPupil!=null && bgPupil.getSelection()!=null){ terisi++; }
                    break;
                case 5:
                    wajib = 2;
                    if(CekEksDeformitas.isSelected() || CekEksEkskoriasi.isSelected() ||
                       CekEksContusio.isSelected() || CekEksAbrasi.isSelected() ||
                       CekEksPenetrasi.isSelected() || CekEksBurn.isSelected() ||
                       CekEksTenderness.isSelected() || CekEksLaserasi.isSelected() ||
                       CekEksSwelling.isSelected()){ terisi++; }
                    if(CekEksTidakTampakJelas.isSelected()){ terisi++; }
                    break;
                case 6:
                    wajib = 1;
                    if(KeluhanUtama!=null && !KeluhanUtama.getText().trim().isEmpty()){ terisi++; }
                    break;
                case 7:
                    wajib = 1;
                    if(TAMPILKAN_TTV_MEDIS){
                        if(TD!=null && Nadi!=null && RR!=null && Suhu!=null &&
                           (!TD.getText().trim().isEmpty() || !Nadi.getText().trim().isEmpty() || !RR.getText().trim().isEmpty() || !Suhu.getText().trim().isEmpty() || (KetFisik!=null && !KetFisik.getText().trim().isEmpty()))){
                            terisi++;
                        }
                    } else {
                        // Sesuai form fisik RM 03 IGD: Keadaan Umum & Kesadaran
                        if(Keadaan!=null && Kesadaran!=null &&
                           Keadaan.getSelectedItem()!=null && Kesadaran.getSelectedItem()!=null){
                            terisi++;
                        }
                    }
                    break;
                case 9:
                    wajib = 0;
                    terisi = 0;
                    break;
                case 11:
                    wajib = 0;
                    terisi = 0;
                    break;
                case 14:
                    wajib = 1;
                    if(TDiagnosaKerja!=null && !TDiagnosaKerja.getText().trim().isEmpty()){ terisi++; }
                    break;
                case 15:
                    wajib = 1;
                    if(Tatalaksana!=null && !Tatalaksana.getText().trim().isEmpty()){ terisi++; }
                    break;
                case 17:
                    wajib = 1;
                    if((CekEdukasiDiberikan!=null && CekEdukasiDiberikan.isSelected()) ||
                       (CekEdukasiTidakBisa!=null && CekEdukasiTidakBisa.isSelected())){
                        terisi++;
                    }
                    break;
                case 18:
                    wajib = 1;
                    if(bgKondisiPulang!=null && bgKondisiPulang.getSelection()!=null){ terisi++; }
                    break;
                case 19:
                    wajib = 1;
                    if(TTdAkhir!=null && TNadiAkhir!=null && TRrAkhir!=null && TSuhuAkhir!=null &&
                       (!TTdAkhir.getText().trim().isEmpty() || !TNadiAkhir.getText().trim().isEmpty()
                         || !TRrAkhir.getText().trim().isEmpty() || !TSuhuAkhir.getText().trim().isEmpty())){
                        terisi++;
                    }
                    break;
                default:
                    break;
            }
            return new int[]{wajib, terisi};
    }

    /**
     * Sesi 5 (Bagian E): listener reaktif utk field wajib/opsional - salinan
     * persis pasangListenerProgresKep (dispatch by type, tanpa TableModelListener
     * krn tab Input Pengkajian Medis tidak punya tabel - lihat Sesi 4).
     */
    private void pasangListenerProgresMedis(java.awt.Component comp){
        if(comp instanceof javax.swing.text.JTextComponent){
            ((javax.swing.text.JTextComponent)comp).getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e){ hitungProgresMedis(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e){ hitungProgresMedis(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e){ hitungProgresMedis(); }
            });
        } else if(comp instanceof javax.swing.AbstractButton){
            ((javax.swing.AbstractButton)comp).addItemListener((java.awt.event.ItemEvent e) -> hitungProgresMedis());
        } else if(comp instanceof widget.ComboBox){
            ((widget.ComboBox)comp).addItemListener((java.awt.event.ItemEvent e) -> hitungProgresMedis());
        }
    }

    private void pasangSemuaListenerProgresMedis(){
        javax.swing.text.JTextComponent[] texts = {
            KeluhanUtama, TD, Nadi, RR, Suhu, TGcsE, TGcsV, TGcsM,
            TJantung, TParu, TDiagnosaKerja, TDiagnosaBanding, Tatalaksana,
            TTdAkhir, TNadiAkhir, TRrAkhir, TSuhuAkhir, KetFisik
        };
        for(javax.swing.text.JTextComponent t : texts){
            if(t != null){ pasangListenerProgresMedis(t); }
        }

        javax.swing.AbstractButton[] btns = {
            RbJnAman, RbJnMengancamJiwa, RbNapAman, RbNapMengancamJiwa,
            CekSirReguler, CekSirIrreguler, CekSirKuat, CekSirLemah,
            CekSirKulitNormal, CekSirKulitPucat, CekSirJaundice, CekSirCyanosis, CekSirBerkeringat,
            CekSirHangat, CekSirDingin, CekSirKering, CekSirBasah,
            CekSirCrtKurang2, CekSirCrtLebih2, CekSirAman, CekSirMengancamJiwa,
            RbKonjungtivaPucat, RbKonjungtivaPink,
            RbSkleraIkterik, RbSkleraTidakIkterik, CekDiagnosaKerja,
            CekEdukasiDiberikan, CekEdukasiTidakBisa,
            RbKondisiMembaik, RbKondisiMemburuk, RbKondisiTetap, RbKondisiMeninggal, RbKondisiDoa
        };
        for(javax.swing.AbstractButton b : btns){
            if(b != null){ pasangListenerProgresMedis(b); }
        }

        widget.ComboBox[] combos = { KategoriPasien };
        for(widget.ComboBox c : combos){
            if(c != null){ pasangListenerProgresMedis(c); }
        }
    }

    private void gayaTombolCardFooterMedis(final widget.Button b, int lebar){
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

    /** Bilah judul dan tab datar bergaris bawah seperti referensi user. */
    private void initBingkaiMedis(){
        internalFrame1.setBorder(null);
        widget.PanelBiasa bilahJudul = new widget.PanelBiasa(new java.awt.BorderLayout());
        latarMedis(bilahJudul, new java.awt.Color(0x12,0x43,0x3F));
        bilahJudul.setPreferredSize(new java.awt.Dimension(10,34));
        bilahJudul.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,18,0,18));
        widget.Label judul = new widget.Label();
        judul.setText("Pengkajian Awal Medis IGD");
        judul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        judul.setForeground(java.awt.Color.WHITE);
        judul.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 12));
        bilahJudul.add(judul, java.awt.BorderLayout.CENTER);
        internalFrame1.add(bilahJudul, java.awt.BorderLayout.NORTH);

        TabRawat.putClientProperty("JTabbedPane.tabType", "underlined");
        TabRawat.putClientProperty("JTabbedPane.showContentSeparator", Boolean.TRUE);
        TabRawat.putClientProperty("JTabbedPane.tabHeight", 34);
        TabRawat.setBorder(null);
        TabRawat.setBackground(widget.ThemeConfig.SURFACE);
        TabRawat.setForeground(widget.ThemeConfig.TEXT_NORMAL);
        TabRawat.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if (TabRawat.getSelectedIndex() == 1) {
                    runBackground(() -> tampil());
                }
            }
        });
    }

    /** Restyle action bar bawah (panelGlass8) agar tombol berwujud card yang jelas. */
    private void initActionBarMedis(){
        BtnSimpan.setText("Simpan");
        gayaTombolCardFooterMedis(BtnSimpan, 96);
        gayaTombolCardFooterMedis(BtnBatal, 90);
        gayaTombolCardFooterMedis(BtnEdit, 90);
        gayaTombolCardFooterMedis(BtnHapus, 92);
        gayaTombolCardFooterMedis(BtnPrint, 90);
        gayaTombolCardFooterMedis(BtnAll, 96);
        gayaTombolCardFooterMedis(BtnKeluar, 96);

        BtnHapus.setForeground(widget.ThemeConfig.DANGER);
        BtnKeluar.setForeground(widget.ThemeConfig.DANGER);

        // Pasang icon SVG modern dari IconStyle jika belum terpasang
        widget.IconStyle.pasang(panelGlass8);

        lblProgresBawahMedis = new widget.Label();
        // Placeholder netral, bukan pecahan literal spt "0/17..." milik Kep -
        // totalWajibSemua Medis dihitung dinamis (Sesi 5), langsung ditimpa
        // oleh hitungProgresMedis() di akhir konstruktor.
        lblProgresBawahMedis.setText("Memuat status isian wajib...");
        lblProgresBawahMedis.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11).deriveFont(11.5f));
        lblProgresBawahMedis.setForeground(widget.ThemeConfig.TEXT_MUTED);

        // Aksi transaksi tetap berkelompok di kiri. Status dan Keluar berada
        // di kanan, dengan Keluar sebagai tombol terakhir agar konsisten dengan
        // keputusan UI global. BtnAll ("Semua") adalah tombol cari pasien,
        // bukan toggle "Tampilkan Semua Bagian", sehingga tetap di kiri.
        widget.PanelBiasa kiri = new widget.PanelBiasa();
        kiri.setOpaque(false);
        kiri.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 8));
        kiri.add(BtnSimpan); kiri.add(BtnBatal); kiri.add(BtnEdit);
        kiri.add(BtnHapus); kiri.add(BtnPrint); kiri.add(BtnAll);
        kiri.add(BtnKeluar);
        widget.PanelBiasa kanan = new widget.PanelBiasa();
        kanan.setOpaque(false);
        kanan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 8));
        kanan.add(lblProgresBawahMedis);

        panelGlass8.removeAll();
        panelGlass8.setLayout(new java.awt.BorderLayout());
        panelGlass8.setBorder(javax.swing.BorderFactory.createMatteBorder(1,0,0,0, widget.ThemeConfig.BORDER_SOFT));
        panelGlass8.add(kiri, java.awt.BorderLayout.CENTER);
        panelGlass8.add(kanan, java.awt.BorderLayout.EAST);
        panelGlass8.revalidate();
        panelGlass8.repaint();
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
        scrollRailMedis.setBorder(null);
        scrollRailMedis.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollRailMedis.setPreferredSize(new java.awt.Dimension(300,10));
        scrollRailMedis.getVerticalScrollBar().setUnitIncrement(16);
        scrollRailMedis.getViewport().setBackground(widget.ThemeConfig.SURFACE);
        // Area kerja putih bersih; pengguna meminta tanpa latar abu-abu.
        latarMedis(FormInput, widget.ThemeConfig.SURFACE);
        scrollInput.getViewport().setBackground(widget.ThemeConfig.SURFACE);
        scrollInput.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollInput.getViewport().addComponentListener(new java.awt.event.ComponentAdapter(){
            @Override public void componentResized(java.awt.event.ComponentEvent e){
                sesuaikanLebarHalamanMedis();
            }
        });

        widget.PanelBiasa sidebar = new widget.PanelBiasa(new java.awt.BorderLayout());
        latarMedis(sidebar, widget.ThemeConfig.SURFACE);
        sidebar.setPreferredSize(new java.awt.Dimension(300,10));
        sidebar.setBorder(javax.swing.BorderFactory.createMatteBorder(0,0,0,1, widget.ThemeConfig.BORDER_SOFT));
        widget.PanelBiasa kepalaSidebar = new widget.PanelBiasa(new java.awt.BorderLayout());
        latarMedis(kepalaSidebar, widget.ThemeConfig.SURFACE);
        kepalaSidebar.setBorder(javax.swing.BorderFactory.createEmptyBorder(14,14,10,14));
        kepalaSidebar.add(buatKartuWajibMedis(), java.awt.BorderLayout.CENTER);
        sidebar.add(kepalaSidebar, java.awt.BorderLayout.NORTH);
        sidebar.add(scrollRailMedis, java.awt.BorderLayout.CENTER);

        internalFrame2.removeAll();
        internalFrame2.setLayout(new java.awt.BorderLayout());
        internalFrame2.add(barPasienMedis, java.awt.BorderLayout.NORTH);
        internalFrame2.add(sidebar, java.awt.BorderLayout.WEST);
        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);
        // Isi rail SESUDAH scrollInput ada - kePosisiSeksiMedis() memakai
        // FormInput.scrollRectToVisible() yg butuh FormInput sudah ternaung
        // JViewport/JScrollPane.
        isiRailMedis();
        internalFrame2.revalidate();
        internalFrame2.repaint();
        javax.swing.SwingUtilities.invokeLater(() -> sesuaikanLebarHalamanMedis());
    }

    /**
     * [RM 03-2] Toggle panel APGAR/Segitiga PAT - tampil hanya kalau
     * KategoriPasien = "Bayi Baru Lahir", pola sama seperti
     * perbaruiVisibilitasMekanismeTrauma() di RMTriaseIGD.java.
     */
    private void perbaruiVisibilitasBayiBaruLahir(){
        boolean bayi = "Bayi Baru Lahir".equals(KategoriPasien.getSelectedItem());
        if(panelBayiBaruLahir != null){
            panelBayiBaruLahir.setVisible(bayi);
        }
        if(!bayi){
            if(TApgarCukupBulan != null) TApgarCukupBulan.setText("");
            if(TApgarCairanAmnionJernih != null) TApgarCairanAmnionJernih.setText("");
            if(TApgarPernapasanMenangis != null) TApgarPernapasanMenangis.setText("");
            if(TApgarTonusOtot != null) TApgarTonusOtot.setText("");
            if(TPatPenampilan != null) TPatPenampilan.setText("");
            if(TPatUsahaNapas != null) TPatUsahaNapas.setText("");
            if(TPatPerfusiKulit != null) TPatPerfusiKulit.setText("");
        } else {
            if(TApgarCukupBulan != null){
                if(TApgarCukupBulan.getText().trim().isEmpty()){
                    resetKomponenBayiBaruLahir();
                } else {
                    sinkronkanKomponenBayiBaruLahirDariTeks();
                }
            }
        }
        if(halamanMedisSiap && !menyusunHalamanMedis){
            susunKelompokHalamanMedis();
        }
    }

    // =========================================================================
    // [ARGON] IMPLEMENTASI MODERN PENILAIAN BAYI BARU LAHIR & SEGITIGA PAT
    // =========================================================================
    private void buatPanelBayiBaruLahirPAT(){
        panelBayiBaruLahir = new widget.PanelBiasa(new java.awt.BorderLayout(0, 0));
        panelBayiBaruLahir.setOpaque(false);

        // Body Grid 2 Kolom: Kiri = 4 Kriteria Awal + APGAR, Kanan = Segitiga PAT Interaktif
        widget.PanelBiasa bodyPanel = new widget.PanelBiasa(new java.awt.GridLayout(1, 2, 16, 0));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14));

        bodyPanel.add(buatKolomKiriBayiPAT());
        bodyPanel.add(buatKolomKananBayiPAT());

        panelBayiBaruLahir.add(bodyPanel, java.awt.BorderLayout.CENTER);
    }

    private widget.PanelBiasa buatSubCardBayi(String judul){
        widget.PanelBiasa p = new widget.PanelBiasa(new java.awt.BorderLayout(0, 10));
        p.setBackground(new java.awt.Color(248, 250, 252));
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
            javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        javax.swing.JLabel lbl = new javax.swing.JLabel(judul);
        lbl.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        lbl.setForeground(new java.awt.Color(15, 61, 62));
        lbl.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(15, 118, 110)),
            javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0)
        ));
        p.add(lbl, java.awt.BorderLayout.NORTH);
        return p;
    }

    private widget.PanelBiasa buatKolomKiriBayiPAT(){
        widget.PanelBiasa card = buatSubCardBayi("PENILAIAN AWAL BAYI BARU LAHIR");
        widget.PanelBiasa content = new widget.PanelBiasa();
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setOpaque(false);

        TApgarCukupBulan = new widget.TextBox();
        TApgarCukupBulan.setText("Ya (Aterm)");
        btnCukupYa = buatToggleBtnBayi("Ya", true, true);
        btnCukupTidak = buatToggleBtnBayi("Tidak", false, false);
        content.add(buatBarisYaTidakBayi("Cukup Bulan :", TApgarCukupBulan, btnCukupYa, btnCukupTidak,
            "Ya (Aterm)", "Tidak (Preterm / Postterm)", val -> { cukupBulanYa = val; updateBannerResusitasi(); }));
        content.add(javax.swing.Box.createVerticalStrut(7));

        TApgarCairanAmnionJernih = new widget.TextBox();
        TApgarCairanAmnionJernih.setText("Ya (Jernih)");
        btnAmnionYa = buatToggleBtnBayi("Ya", true, true);
        btnAmnionTidak = buatToggleBtnBayi("Tidak", false, false);
        content.add(buatBarisYaTidakBayi("Cairan Amnion Jernih :", TApgarCairanAmnionJernih, btnAmnionYa, btnAmnionTidak,
            "Ya (Jernih)", "Tidak (Keruh / Mekonium)", val -> { amnionYa = val; updateBannerResusitasi(); }));
        content.add(javax.swing.Box.createVerticalStrut(7));

        TApgarPernapasanMenangis = new widget.TextBox();
        TApgarPernapasanMenangis.setText("Ya (Menangis kuat)");
        btnNapasYa = buatToggleBtnBayi("Ya", true, true);
        btnNapasTidak = buatToggleBtnBayi("Tidak", false, false);
        content.add(buatBarisYaTidakBayi("Pernapasan/Menangis :", TApgarPernapasanMenangis, btnNapasYa, btnNapasTidak,
            "Ya (Menangis kuat)", "Tidak (Megap-megap / Apnea)", val -> { napasYa = val; updateBannerResusitasi(); }));
        content.add(javax.swing.Box.createVerticalStrut(7));

        TApgarTonusOtot = new widget.TextBox();
        TApgarTonusOtot.setText("Ya (Baik / Aktif)");
        btnTonusYa = buatToggleBtnBayi("Ya", true, true);
        btnTonusTidak = buatToggleBtnBayi("Tidak", false, false);
        content.add(buatBarisYaTidakBayi("Tonus Otot :", TApgarTonusOtot, btnTonusYa, btnTonusTidak,
            "Ya (Baik / Aktif)", "Tidak (Lemah / Lunglai)", val -> { tonusYa = val; updateBannerResusitasi(); }));
        content.add(javax.swing.Box.createVerticalStrut(8));

        // Banner Kesimpulan Resusitasi
        lblBannerResusitasi = new javax.swing.JLabel("<html><b>[VIGOROUS BABY]</b> &nbsp; Alur Klinis: Perawatan Rutin Neonatal</html>");
        lblBannerResusitasi.setOpaque(true);
        lblBannerResusitasi.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        lblBannerResusitasi.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(167, 243, 208)),
            javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        lblBannerResusitasi.setBackground(new java.awt.Color(236, 253, 245));
        lblBannerResusitasi.setForeground(new java.awt.Color(6, 95, 70));

        widget.PanelBiasa bannerWrap = new widget.PanelBiasa(new java.awt.BorderLayout());
        bannerWrap.setOpaque(false);
        bannerWrap.setPreferredSize(new java.awt.Dimension(0, 32));
        bannerWrap.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 32));
        bannerWrap.add(lblBannerResusitasi, java.awt.BorderLayout.CENTER);
        content.add(bannerWrap);
        content.add(javax.swing.Box.createVerticalStrut(12));

        // SubCard Skor APGAR
        widget.PanelBiasa apgarBox = new widget.PanelBiasa(new java.awt.BorderLayout(0, 6));
        apgarBox.setBackground(java.awt.Color.WHITE);
        apgarBox.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225)),
            javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        widget.PanelBiasa apgarTop = new widget.PanelBiasa(new java.awt.BorderLayout());
        apgarTop.setOpaque(false);
        javax.swing.JLabel lblApgarTitle = new javax.swing.JLabel("SKOR APGAR");
        lblApgarTitle.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        lblApgarTitle.setForeground(new java.awt.Color(15, 61, 62));
        javax.swing.JLabel lblApgarLegend = new javax.swing.JLabel("7-10 Normal | 4-6 Asfiksia Sedang | 0-3 Berat");
        lblApgarLegend.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 10));
        lblApgarLegend.setForeground(new java.awt.Color(100, 116, 139));
        apgarTop.add(lblApgarTitle, java.awt.BorderLayout.WEST);
        apgarTop.add(lblApgarLegend, java.awt.BorderLayout.EAST);

        widget.PanelBiasa apgarInputs = new widget.PanelBiasa(new java.awt.GridBagLayout());
        apgarInputs.setOpaque(false);
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(0, 0, 0, 8);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        spnApgar1 = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(8, 0, 10, 1));
        spnApgar5 = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(9, 0, 10, 1));
        txtSkorApgar = new widget.TextBox();
        txtSkorApgar.setText("8 / 9 (Normal - Tidak Asfiksia)");
        txtSkorApgar.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        txtSkorApgar.setForeground(new java.awt.Color(15, 118, 110));
        styleSpinnerBayi(spnApgar1);
        styleSpinnerBayi(spnApgar5);
        styleTextFieldBayi(txtSkorApgar);

        spnApgar1.addChangeListener(e -> hitungApgarOtomatis());
        spnApgar5.addChangeListener(e -> hitungApgarOtomatis());

        gbc.gridx = 0; gbc.weightx = 0;
        apgarInputs.add(buatFieldKecilBayi("Menit Ke-1", spnApgar1, 65), gbc);
        gbc.gridx = 1;
        apgarInputs.add(buatFieldKecilBayi("Menit Ke-5", spnApgar5, 65), gbc);
        gbc.gridx = 2; gbc.weightx = 1.0; gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        apgarInputs.add(buatFieldKecilBayi("Nilai & Interpretasi Skor APGAR", txtSkorApgar, 180), gbc);

        apgarBox.add(apgarTop, java.awt.BorderLayout.NORTH);
        apgarBox.add(apgarInputs, java.awt.BorderLayout.CENTER);
        content.add(apgarBox);

        card.add(content, java.awt.BorderLayout.CENTER);
        return card;
    }

    private widget.PanelBiasa buatKolomKananBayiPAT(){
        widget.PanelBiasa card = buatSubCardBayi("KRITERIA PENILAIAN ANAK (SEGITIGA PAT)");
        widget.PanelBiasa content = new widget.PanelBiasa(new java.awt.BorderLayout(0, 10));
        content.setOpaque(false);

        // Visual Segitiga PAT Box + Legend
        widget.PanelBiasa visualBox = new widget.PanelBiasa(new java.awt.BorderLayout(10, 0));
        visualBox.setBackground(java.awt.Color.WHITE);
        visualBox.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
            javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 12)
        ));

        canvasSegitigaPAT = new SegitigaCanvasPAT();

        widget.PanelBiasa legendBox = new widget.PanelBiasa();
        legendBox.setLayout(new javax.swing.BoxLayout(legendBox, javax.swing.BoxLayout.Y_AXIS));
        legendBox.setOpaque(false);
        legendBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 0, 10, 0));

        javax.swing.JLabel l1 = new javax.swing.JLabel("<html><b>Interaksi Segitiga PAT:</b><br>&bull; Klik garis segitiga atau tombol<br>&bull; <font color='#059669'><b>Hijau</b></font> = Sisi Normal<br>&bull; <font color='#e11d48'><b>Merah</b></font> = Sisi Abnormal</html>");
        l1.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        l1.setForeground(new java.awt.Color(30, 41, 59));

        lblKesanPAT = new javax.swing.JLabel("Kesan: STABIL");
        lblKesanPAT.setOpaque(true);
        lblKesanPAT.setBackground(new java.awt.Color(236, 253, 245));
        lblKesanPAT.setForeground(new java.awt.Color(6, 95, 70));
        lblKesanPAT.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        lblKesanPAT.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(167, 243, 208)),
            javax.swing.BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        legendBox.add(l1);
        legendBox.add(javax.swing.Box.createVerticalStrut(8));
        legendBox.add(lblKesanPAT);

        visualBox.add(canvasSegitigaPAT, java.awt.BorderLayout.CENTER);
        visualBox.add(legendBox, java.awt.BorderLayout.EAST);

        // Baris Kontrol 3 Sisi PAT
        widget.PanelBiasa patRows = new widget.PanelBiasa(new java.awt.GridLayout(3, 1, 0, 7));
        patRows.setOpaque(false);

        TPatPenampilan = new widget.TextBox();
        TPatPenampilan.setText("Normal (Interaktif / Aktif)");
        btnPatAppNorm = buatToggleBtnBayi("Normal", true, true);
        btnPatAppAbn  = buatToggleBtnBayi("Abnormal", false, false);
        patRows.add(buatBarisPATBayi("Penampilan (PAT) :", btnPatAppNorm, btnPatAppAbn, TPatPenampilan, "app"));

        TPatUsahaNapas = new widget.TextBox();
        TPatUsahaNapas.setText("Normal (Tanpa Retraksi)");
        btnPatWorkNorm = buatToggleBtnBayi("Normal", true, true);
        btnPatWorkAbn  = buatToggleBtnBayi("Abnormal", false, false);
        patRows.add(buatBarisPATBayi("Usaha Napas (PAT) :", btnPatWorkNorm, btnPatWorkAbn, TPatUsahaNapas, "work"));

        TPatPerfusiKulit = new widget.TextBox();
        TPatPerfusiKulit.setText("Normal (Kemerahan / Hangat)");
        btnPatCircNorm = buatToggleBtnBayi("Normal", true, true);
        btnPatCircAbn  = buatToggleBtnBayi("Abnormal", false, false);
        patRows.add(buatBarisPATBayi("Perfusi Kulit (PAT) :", btnPatCircNorm, btnPatCircAbn, TPatPerfusiKulit, "circ"));

        content.add(visualBox, java.awt.BorderLayout.CENTER);
        content.add(patRows, java.awt.BorderLayout.SOUTH);

        card.add(content, java.awt.BorderLayout.CENTER);
        return card;
    }

    private interface ToggleCallbackBayi { void onToggle(boolean isYes); }

    private widget.PanelBiasa buatBarisYaTidakBayi(String labelText, widget.TextBox textField,
            javax.swing.JButton btnYa, javax.swing.JButton btnTidak,
            String defaultYa, String defaultTidak, ToggleCallbackBayi cb) {
        widget.PanelBiasa row = new widget.PanelBiasa(new java.awt.BorderLayout(8, 0));
        row.setBackground(java.awt.Color.WHITE);
        row.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
            javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        javax.swing.JLabel lbl = new javax.swing.JLabel(labelText);
        lbl.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        lbl.setForeground(new java.awt.Color(30, 41, 59));
        lbl.setPreferredSize(new java.awt.Dimension(140, 24));

        widget.PanelBiasa leftWrap = new widget.PanelBiasa(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        leftWrap.setOpaque(false);

        widget.PanelBiasa toggleBox = new widget.PanelBiasa(new java.awt.GridLayout(1, 2, 2, 0));
        toggleBox.setBackground(new java.awt.Color(241, 245, 249));
        toggleBox.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225)));
        toggleBox.setPreferredSize(new java.awt.Dimension(110, 26));

        btnYa.addActionListener(e -> {
            warnaiToggleBayi(btnYa, btnTidak, true);
            textField.setText(defaultYa);
            cb.onToggle(true);
        });
        btnTidak.addActionListener(e -> {
            warnaiToggleBayi(btnYa, btnTidak, false);
            textField.setText(defaultTidak);
            textField.requestFocus();
            textField.selectAll();
            cb.onToggle(false);
        });

        toggleBox.add(btnYa);
        toggleBox.add(btnTidak);

        leftWrap.add(lbl);
        leftWrap.add(toggleBox);

        styleTextFieldBayi(textField);
        row.add(leftWrap, java.awt.BorderLayout.WEST);
        row.add(textField, java.awt.BorderLayout.CENTER);
        return row;
    }

    private widget.PanelBiasa buatBarisPATBayi(String labelText, javax.swing.JButton btnNorm, javax.swing.JButton btnAbn,
            widget.TextBox textField, String sisi) {
        widget.PanelBiasa row = new widget.PanelBiasa(new java.awt.BorderLayout(8, 0));
        row.setBackground(java.awt.Color.WHITE);
        row.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
            javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        javax.swing.JLabel lbl = new javax.swing.JLabel(labelText);
        lbl.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 11));
        lbl.setForeground(new java.awt.Color(30, 41, 59));
        lbl.setPreferredSize(new java.awt.Dimension(130, 24));

        widget.PanelBiasa leftWrap = new widget.PanelBiasa(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        leftWrap.setOpaque(false);

        widget.PanelBiasa toggleBox = new widget.PanelBiasa(new java.awt.GridLayout(1, 2, 2, 0));
        toggleBox.setBackground(new java.awt.Color(241, 245, 249));
        toggleBox.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225)));
        toggleBox.setPreferredSize(new java.awt.Dimension(145, 26));

        btnNorm.addActionListener(e -> ubahSisiPAT(sisi, true, true));
        btnAbn.addActionListener(e -> ubahSisiPAT(sisi, false, true));

        toggleBox.add(btnNorm);
        toggleBox.add(btnAbn);

        leftWrap.add(lbl);
        leftWrap.add(toggleBox);

        styleTextFieldBayi(textField);
        row.add(leftWrap, java.awt.BorderLayout.WEST);
        row.add(textField, java.awt.BorderLayout.CENTER);
        return row;
    }

    private javax.swing.JButton buatToggleBtnBayi(String text, boolean isPositive, boolean active) {
        javax.swing.JButton b = new javax.swing.JButton(text);
        b.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        b.setMargin(new java.awt.Insets(2, 4, 2, 4));
        if (active) {
            b.setBackground(isPositive ? new java.awt.Color(13, 148, 136) : new java.awt.Color(225, 29, 72));
            b.setForeground(java.awt.Color.WHITE);
        } else {
            b.setBackground(new java.awt.Color(241, 245, 249));
            b.setForeground(new java.awt.Color(100, 116, 139));
        }
        return b;
    }

    private void warnaiToggleBayi(javax.swing.JButton btnPos, javax.swing.JButton btnNeg, boolean posAktif) {
        if (btnPos == null || btnNeg == null) return;
        btnPos.setBackground(posAktif ? new java.awt.Color(13, 148, 136) : new java.awt.Color(241, 245, 249));
        btnPos.setForeground(posAktif ? java.awt.Color.WHITE : new java.awt.Color(100, 116, 139));
        btnNeg.setBackground(!posAktif ? new java.awt.Color(225, 29, 72) : new java.awt.Color(241, 245, 249));
        btnNeg.setForeground(!posAktif ? java.awt.Color.WHITE : new java.awt.Color(100, 116, 139));
    }

    private void styleTextFieldBayi(widget.TextBox tf) {
        tf.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.PLAIN, 11));
        tf.setForeground(new java.awt.Color(30, 41, 59));
        tf.putClientProperty("Component.arc", 4);
        tf.putClientProperty("ModernStyle.active", Boolean.TRUE);
        tf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225)),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private void styleSpinnerBayi(javax.swing.JSpinner sp) {
        sp.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 12));
        javax.swing.JComponent editor = sp.getEditor();
        if (editor instanceof javax.swing.JSpinner.DefaultEditor) {
            javax.swing.JTextField tf = ((javax.swing.JSpinner.DefaultEditor) editor).getTextField();
            tf.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        }
    }

    private widget.PanelBiasa buatFieldKecilBayi(String label, javax.swing.JComponent comp, int width) {
        widget.PanelBiasa p = new widget.PanelBiasa(new java.awt.BorderLayout(0, 3));
        p.setOpaque(false);
        javax.swing.JLabel l = new javax.swing.JLabel(label);
        l.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
        l.setForeground(new java.awt.Color(100, 116, 139));
        comp.setPreferredSize(new java.awt.Dimension(width, 26));
        p.add(l, java.awt.BorderLayout.NORTH);
        p.add(comp, java.awt.BorderLayout.CENTER);
        return p;
    }

    private void updateBannerResusitasi() {
        if (lblBannerResusitasi == null) return;
        boolean semuaYa = cukupBulanYa && amnionYa && napasYa && tonusYa;
        if (semuaYa) {
            lblBannerResusitasi.setText("<html><b>[VIGOROUS BABY]</b> &nbsp; Alur Klinis: Perawatan Rutin Neonatal</html>");
            lblBannerResusitasi.setBackground(new java.awt.Color(236, 253, 245));
            lblBannerResusitasi.setForeground(new java.awt.Color(6, 95, 70));
            lblBannerResusitasi.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(167, 243, 208)),
                javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
        } else {
            lblBannerResusitasi.setText("<html><b>[PERINGATAN RESUSITASI]</b> &nbsp; Langkah Awal Resusitasi Neonatus!</html>");
            lblBannerResusitasi.setBackground(new java.awt.Color(254, 242, 242));
            lblBannerResusitasi.setForeground(new java.awt.Color(153, 27, 27));
            lblBannerResusitasi.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(254, 202, 202)),
                javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
        }
    }

    private void hitungApgarOtomatis() {
        if (spnApgar1 == null || spnApgar5 == null || txtSkorApgar == null) return;
        int m1 = (Integer) spnApgar1.getValue();
        int m5 = (Integer) spnApgar5.getValue();
        String kategori = "Normal - Tidak Asfiksia";
        if (m1 <= 3) kategori = "Asfiksia Berat";
        else if (m1 <= 6) kategori = "Asfiksia Ringan-Sedang";
        txtSkorApgar.setText(m1 + " / " + m5 + " (" + kategori + ")");
    }

    private void ubahSisiPAT(String sisi, boolean isNorm, boolean fokusKeText) {
        if ("app".equals(sisi)) {
            patAppNormal = isNorm;
            warnaiToggleBayi(btnPatAppNorm, btnPatAppAbn, isNorm);
            if (TPatPenampilan != null) {
                TPatPenampilan.setText(isNorm ? "Normal (Interaktif / Aktif)" : "Abnormal (Letargi / Lemah)");
                if (!isNorm && fokusKeText) { TPatPenampilan.requestFocus(); TPatPenampilan.selectAll(); }
            }
        } else if ("work".equals(sisi)) {
            patWorkNormal = isNorm;
            warnaiToggleBayi(btnPatWorkNorm, btnPatWorkAbn, isNorm);
            if (TPatUsahaNapas != null) {
                TPatUsahaNapas.setText(isNorm ? "Normal (Tanpa Retraksi)" : "Abnormal (Retraksi / Cuping Hidung)");
                if (!isNorm && fokusKeText) { TPatUsahaNapas.requestFocus(); TPatUsahaNapas.selectAll(); }
            }
        } else if ("circ".equals(sisi)) {
            patCircNormal = isNorm;
            warnaiToggleBayi(btnPatCircNorm, btnPatCircAbn, isNorm);
            if (TPatPerfusiKulit != null) {
                TPatPerfusiKulit.setText(isNorm ? "Normal (Kemerahan / Hangat)" : "Abnormal (Pucat / Sianosis)");
                if (!isNorm && fokusKeText) { TPatPerfusiKulit.requestFocus(); TPatPerfusiKulit.selectAll(); }
            }
        }
        if (canvasSegitigaPAT != null) canvasSegitigaPAT.repaint();
        updateKesimpulanPAT();
    }

    private String getStringKesimpulanPAT() {
        if (patAppNormal && patWorkNormal && patCircNormal) return "STABIL";
        if (patAppNormal && !patWorkNormal && patCircNormal) return "GAWAT NAPAS";
        if (!patAppNormal && !patWorkNormal && patCircNormal) return "GAGAL NAPAS";
        if (patAppNormal && patWorkNormal && !patCircNormal) return "SYOK KOMPENSASI";
        if (!patAppNormal && patWorkNormal && !patCircNormal) return "SYOK DEKOMPENSASI";
        if (!patAppNormal && patWorkNormal && patCircNormal) return "GANGGUAN SSP/METABOLIK";
        return "GAGAL KARDIOPULMONER";
    }

    private void updateKesimpulanPAT() {
        if (lblKesanPAT == null) return;
        String kesan = getStringKesimpulanPAT();
        lblKesanPAT.setText("Kesan: " + kesan);
        if ("STABIL".equals(kesan)) {
            lblKesanPAT.setBackground(new java.awt.Color(236, 253, 245));
            lblKesanPAT.setForeground(new java.awt.Color(6, 95, 70));
            lblKesanPAT.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(167, 243, 208)),
                javax.swing.BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
        } else if (kesan.contains("GAGAL") || kesan.contains("DEKOMPENSASI")) {
            lblKesanPAT.setBackground(new java.awt.Color(254, 242, 242));
            lblKesanPAT.setForeground(new java.awt.Color(153, 27, 27));
            lblKesanPAT.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(254, 202, 202)),
                javax.swing.BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
        } else {
            lblKesanPAT.setBackground(new java.awt.Color(254, 243, 199));
            lblKesanPAT.setForeground(new java.awt.Color(146, 64, 14));
            lblKesanPAT.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(253, 230, 138)),
                javax.swing.BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
        }
    }

    private void sinkronkanKomponenBayiBaruLahirDariTeks() {
        if (TApgarCukupBulan == null) return;
        String tc = TApgarCukupBulan.getText().trim();
        cukupBulanYa = tc.isEmpty() || !tc.toLowerCase().startsWith("tidak");
        warnaiToggleBayi(btnCukupYa, btnCukupTidak, cukupBulanYa);

        String ta = TApgarCairanAmnionJernih.getText().trim();
        amnionYa = ta.isEmpty() || !ta.toLowerCase().startsWith("tidak");
        warnaiToggleBayi(btnAmnionYa, btnAmnionTidak, amnionYa);

        String tn = TApgarPernapasanMenangis.getText().trim();
        napasYa = tn.isEmpty() || !tn.toLowerCase().startsWith("tidak");
        warnaiToggleBayi(btnNapasYa, btnNapasTidak, napasYa);

        String to = TApgarTonusOtot.getText().trim();
        tonusYa = to.isEmpty() || !to.toLowerCase().startsWith("tidak");
        warnaiToggleBayi(btnTonusYa, btnTonusTidak, tonusYa);

        updateBannerResusitasi();

        String pp = TPatPenampilan.getText().trim();
        patAppNormal = pp.isEmpty() || !pp.toLowerCase().startsWith("abnormal");
        warnaiToggleBayi(btnPatAppNorm, btnPatAppAbn, patAppNormal);

        String pw = TPatUsahaNapas.getText().trim();
        patWorkNormal = pw.isEmpty() || !pw.toLowerCase().startsWith("abnormal");
        warnaiToggleBayi(btnPatWorkNorm, btnPatWorkAbn, patWorkNormal);

        String pc = TPatPerfusiKulit.getText().trim();
        patCircNormal = pc.isEmpty() || !pc.toLowerCase().startsWith("abnormal");
        warnaiToggleBayi(btnPatCircNorm, btnPatCircAbn, patCircNormal);

        updateKesimpulanPAT();
        if (canvasSegitigaPAT != null) canvasSegitigaPAT.repaint();
    }

    private void resetKomponenBayiBaruLahir() {
        cukupBulanYa = true; amnionYa = true; napasYa = true; tonusYa = true;
        patAppNormal = true; patWorkNormal = true; patCircNormal = true;
        if (TApgarCukupBulan != null) TApgarCukupBulan.setText("Ya (Aterm)");
        if (TApgarCairanAmnionJernih != null) TApgarCairanAmnionJernih.setText("Ya (Jernih)");
        if (TApgarPernapasanMenangis != null) TApgarPernapasanMenangis.setText("Ya (Menangis kuat)");
        if (TApgarTonusOtot != null) TApgarTonusOtot.setText("Ya (Baik / Aktif)");
        if (TPatPenampilan != null) TPatPenampilan.setText("Normal (Interaktif / Aktif)");
        if (TPatUsahaNapas != null) TPatUsahaNapas.setText("Normal (Tanpa Retraksi)");
        if (TPatPerfusiKulit != null) TPatPerfusiKulit.setText("Normal (Kemerahan / Hangat)");
        if (spnApgar1 != null) spnApgar1.setValue(8);
        if (spnApgar5 != null) spnApgar5.setValue(9);
        hitungApgarOtomatis();
        warnaiToggleBayi(btnCukupYa, btnCukupTidak, true);
        warnaiToggleBayi(btnAmnionYa, btnAmnionTidak, true);
        warnaiToggleBayi(btnNapasYa, btnNapasTidak, true);
        warnaiToggleBayi(btnTonusYa, btnTonusTidak, true);
        warnaiToggleBayi(btnPatAppNorm, btnPatAppAbn, true);
        warnaiToggleBayi(btnPatWorkNorm, btnPatWorkAbn, true);
        warnaiToggleBayi(btnPatCircNorm, btnPatCircAbn, true);
        updateBannerResusitasi();
        updateKesimpulanPAT();
        if (canvasSegitigaPAT != null) canvasSegitigaPAT.repaint();
    }

    private class SegitigaCanvasPAT extends javax.swing.JPanel {
        private java.awt.geom.Line2D.Double lineKiri, lineKanan, lineBawah;

        public SegitigaCanvasPAT() {
            setPreferredSize(new java.awt.Dimension(230, 155));
            setBackground(java.awt.Color.WHITE);
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    java.awt.Point p = e.getPoint();
                    double tol = 14.0;
                    if (lineKiri != null && lineKiri.ptSegDist(p) <= tol) {
                        ubahSisiPAT("app", !patAppNormal, false);
                    } else if (lineKanan != null && lineKanan.ptSegDist(p) <= tol) {
                        ubahSisiPAT("work", !patWorkNormal, false);
                    } else if (lineBawah != null && lineBawah.ptSegDist(p) <= tol) {
                        ubahSisiPAT("circ", !patCircNormal, false);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            double topX = cx, topY = 20;
            double leftX = cx - 72, leftY = 122;
            double rightX = cx + 72, rightY = 122;

            lineKiri = new java.awt.geom.Line2D.Double(topX, topY, leftX, leftY);
            lineKanan = new java.awt.geom.Line2D.Double(topX, topY, rightX, rightY);
            lineBawah = new java.awt.geom.Line2D.Double(leftX, leftY, rightX, rightY);

            // Isi Segitiga
            java.awt.geom.Path2D poly = new java.awt.geom.Path2D.Double();
            poly.moveTo(topX, topY);
            poly.lineTo(leftX, leftY);
            poly.lineTo(rightX, rightY);
            poly.closePath();

            boolean aman = patAppNormal && patWorkNormal && patCircNormal;
            g2.setColor(aman ? new java.awt.Color(240, 253, 244) : new java.awt.Color(254, 242, 242));
            g2.fill(poly);

            // Gambar 3 Garis Sisi
            g2.setStroke(new java.awt.BasicStroke(7.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            java.awt.Color cNorm = new java.awt.Color(16, 185, 129);
            java.awt.Color cAbn  = new java.awt.Color(225, 29, 72);

            g2.setColor(patAppNormal ? cNorm : cAbn);
            g2.draw(lineKiri);

            g2.setColor(patWorkNormal ? cNorm : cAbn);
            g2.draw(lineKanan);

            g2.setColor(patCircNormal ? cNorm : cAbn);
            g2.draw(lineBawah);

            // Titik Sudut Segitiga
            g2.setColor(new java.awt.Color(15, 61, 62));
            g2.fillOval((int) topX - 4, (int) topY - 4, 8, 8);
            g2.fillOval((int) leftX - 4, (int) leftY - 4, 8, 8);
            g2.fillOval((int) rightX - 4, (int) rightY - 4, 8, 8);

            // Label Sisi Sesuai Kertas Fisik
            g2.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 10));
            g2.setColor(new java.awt.Color(30, 41, 59));
            g2.drawString("Penampilan", (int) leftX - 38, (int) ((topY + leftY) / 2) - 4);
            g2.drawString("Usaha Napas", (int) rightX - 18, (int) ((topY + rightY) / 2) - 4);

            java.awt.FontMetrics fm = g2.getFontMetrics();
            String sBawah = "Perfusi Kulit";
            g2.drawString(sBawah, cx - fm.stringWidth(sBawah) / 2, (int) leftY + 20);

            // Teks Kesimpulan di Tengah Segitiga
            String tKesan = getStringKesimpulanPAT();
            g2.setFont(new java.awt.Font(widget.ThemeConfig.FONT_FAMILY, java.awt.Font.BOLD, 9));
            java.awt.FontMetrics fm2 = g2.getFontMetrics();
            g2.setColor(aman ? new java.awt.Color(6, 95, 70) : new java.awt.Color(153, 27, 27));
            g2.drawString(tKesan, cx - fm2.stringWidth(tKesan) / 2, (int) leftY - 28);

            g2.dispose();
        }
    }

    /**
     * [RM 03-2] Menghasilkan InputStream PNG visual Segitiga PAT (Pediatric Assessment Triangle)
     * untuk dicetak di laporan rptCetakPenilaianAwalMedisIGD.jrxml / jasper.
     */
    public static java.io.InputStream gambarSegitigaPATStream(String penampilan, String usahaNapas, String perfusiKulit) {
        try {
            boolean appNorm = penampilan == null || !penampilan.toLowerCase().startsWith("abnormal");
            boolean workNorm = usahaNapas == null || !usahaNapas.toLowerCase().startsWith("abnormal");
            boolean circNorm = perfusiKulit == null || !perfusiKulit.toLowerCase().startsWith("abnormal");

            int width = 360;
            int height = 180;
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int cx = width / 2;
            double topX = cx, topY = 24;
            double leftX = cx - 95, leftY = 138;
            double rightX = cx + 95, rightY = 138;

            java.awt.geom.Line2D.Double lineKiri = new java.awt.geom.Line2D.Double(topX, topY, leftX, leftY);
            java.awt.geom.Line2D.Double lineKanan = new java.awt.geom.Line2D.Double(topX, topY, rightX, rightY);
            java.awt.geom.Line2D.Double lineBawah = new java.awt.geom.Line2D.Double(leftX, leftY, rightX, rightY);

            // Isi Segitiga
            java.awt.geom.Path2D poly = new java.awt.geom.Path2D.Double();
            poly.moveTo(topX, topY);
            poly.lineTo(leftX, leftY);
            poly.lineTo(rightX, rightY);
            poly.closePath();

            boolean aman = appNorm && workNorm && circNorm;
            g2.setColor(aman ? new java.awt.Color(240, 253, 244) : new java.awt.Color(254, 242, 242));
            g2.fill(poly);

            // Garis Sisi
            g2.setStroke(new java.awt.BasicStroke(7.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            java.awt.Color cNorm = new java.awt.Color(16, 185, 129); // Hijau Emerald
            java.awt.Color cAbn  = new java.awt.Color(225, 29, 72);  // Merah Rose

            g2.setColor(appNorm ? cNorm : cAbn);
            g2.draw(lineKiri);

            g2.setColor(workNorm ? cNorm : cAbn);
            g2.draw(lineKanan);

            g2.setColor(circNorm ? cNorm : cAbn);
            g2.draw(lineBawah);

            // Titik Sudut Segitiga
            g2.setColor(new java.awt.Color(15, 61, 62));
            g2.fillOval((int) topX - 5, (int) topY - 5, 10, 10);
            g2.fillOval((int) leftX - 5, (int) leftY - 5, 10, 10);
            g2.fillOval((int) rightX - 5, (int) rightY - 5, 10, 10);

            // Label Sisi
            g2.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
            g2.setColor(new java.awt.Color(30, 41, 59));

            // Sisi Kiri: Penampilan
            g2.drawString("Penampilan", (int) leftX - 68, (int) ((topY + leftY) / 2) - 4);
            g2.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 8));
            g2.setColor(appNorm ? new java.awt.Color(5, 150, 105) : new java.awt.Color(225, 29, 72));
            g2.drawString(appNorm ? "[ Normal ]" : "[ Abnormal ]", (int) leftX - 68, (int) ((topY + leftY) / 2) + 9);

            // Sisi Kanan: Usaha Napas
            g2.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
            g2.setColor(new java.awt.Color(30, 41, 59));
            g2.drawString("Usaha Napas", (int) rightX + 12, (int) ((topY + rightY) / 2) - 4);
            g2.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 8));
            g2.setColor(workNorm ? new java.awt.Color(5, 150, 105) : new java.awt.Color(225, 29, 72));
            g2.drawString(workNorm ? "[ Normal ]" : "[ Abnormal ]", (int) rightX + 12, (int) ((topY + rightY) / 2) + 9);

            // Sisi Bawah: Perfusi Kulit
            g2.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 10));
            g2.setColor(new java.awt.Color(30, 41, 59));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            String sBawah = "Perfusi Kulit";
            g2.drawString(sBawah, cx - fm.stringWidth(sBawah) / 2, (int) leftY + 18);
            g2.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 8));
            g2.setColor(circNorm ? new java.awt.Color(5, 150, 105) : new java.awt.Color(225, 29, 72));
            String stBawah = circNorm ? "[ Normal ]" : "[ Abnormal ]";
            g2.drawString(stBawah, cx - g2.getFontMetrics().stringWidth(stBawah) / 2, (int) leftY + 30);

            // Teks Kesimpulan di Tengah Segitiga
            String tKesan = "STABIL";
            if (appNorm && workNorm && circNorm) tKesan = "STABIL";
            else if (appNorm && !workNorm && circNorm) tKesan = "GAWAT NAPAS";
            else if (!appNorm && !workNorm && circNorm) tKesan = "GAGAL NAPAS";
            else if (appNorm && workNorm && !circNorm) tKesan = "SYOK KOMPENSASI";
            else if (!appNorm && workNorm && !circNorm) tKesan = "SYOK DEKOMPENSASI";
            else if (!appNorm && workNorm && circNorm) tKesan = "GANGGUAN SSP/METABOLIK";
            else tKesan = "GAGAL KARDIOPULMONER";

            g2.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));
            java.awt.FontMetrics fm2 = g2.getFontMetrics();
            g2.setColor(aman ? new java.awt.Color(6, 95, 70) : new java.awt.Color(153, 27, 27));
            g2.drawString(tKesan, cx - fm2.stringWidth(tKesan) / 2, (int) leftY - 32);

            g2.dispose();

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "PNG", baos);
            return new java.io.ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * [RM 03-2] Menghasilkan string rekomendasi klinis Segitiga PAT berdasarkan 3 sisi.
     */
    public static String getKesanPAT(String penampilan, String usahaNapas, String perfusiKulit) {
        boolean appNorm = penampilan == null || !penampilan.toLowerCase().startsWith("abnormal");
        boolean workNorm = usahaNapas == null || !usahaNapas.toLowerCase().startsWith("abnormal");
        boolean circNorm = perfusiKulit == null || !perfusiKulit.toLowerCase().startsWith("abnormal");

        if (appNorm && workNorm && circNorm) {
            return "STABIL\nResusitasi & Perawatan Rutin Bayi Bugar";
        } else if (appNorm && !workNorm && circNorm) {
            return "GAWAT NAPAS (RESPIRATORY DISTRESS)\nPerlu Terapi Oksigenasi / CPAP Segera";
        } else if (!appNorm && !workNorm && circNorm) {
            return "GAGAL NAPAS (RESPIRATORY FAILURE)\nBantu Ventilasi Tekanan Positif (VTP)";
        } else if (appNorm && workNorm && !circNorm) {
            return "SYOK KOMPENSASI (COMPENSATED SHOCK)\nResusitasi Cairan / Akses Vaskuler";
        } else if (!appNorm && workNorm && !circNorm) {
            return "SYOK DEKOMPENSASI (DECOMPENSATED SHOCK)\nResusitasi Cairan Agresif & Inotropik";
        } else if (!appNorm && workNorm && circNorm) {
            return "GANGGUAN SSP / METABOLIK\nEvaluasi Neurologis & Gula Darah Segera";
        } else {
            return "GAGAL KARDIOPULMONER\nResusitasi Jantung Paru & Bantuan Napas Segera";
        }
    }

    /**
     * [RM 03-2] Struktur data helper untuk parsing baris order laboratorium pada cetakan rptCetakPenilaianAwalMedisIGD.
     */
    public static class LabOrderItem {
        public String noOrder = "";
        public String waktu = "";
        public String pemeriksaan = "";
        public String status = "Sedang dalam proses alat lab / menunggu hasil bridging";
    }

    public static java.util.List<LabOrderItem> parseLabOrders(String raw) {
        java.util.List<LabOrderItem> list = new java.util.ArrayList<LabOrderItem>();
        if (raw == null || raw.trim().isEmpty()) {
            return list;
        }

        java.util.regex.Pattern pOrder = java.util.regex.Pattern.compile(
            "(?:•|\\*|-)?\\s*(?:No\\.?\\s*(?:Order|Permintaan)\\s*:\\s*)?([A-Z]{1,4}\\d{6,16})\\s*(?:\\(([^)]+)\\))?",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher m = pOrder.matcher(raw);

        java.util.List<Integer> startIdxs = new java.util.ArrayList<Integer>();
        java.util.List<String> orders = new java.util.ArrayList<String>();
        java.util.List<String> waktus = new java.util.ArrayList<String>();

        while (m.find()) {
            startIdxs.add(m.start());
            orders.add(m.group(1));
            waktus.add(m.group(2) != null ? m.group(2).trim() : "");
        }

        for (int i = 0; i < startIdxs.size(); i++) {
            int start = startIdxs.get(i);
            int end = (i + 1 < startIdxs.size()) ? startIdxs.get(i + 1) : raw.length();
            String block = raw.substring(start, end);

            LabOrderItem item = new LabOrderItem();
            item.noOrder = orders.get(i);
            item.waktu = waktus.get(i);

            java.util.regex.Matcher mPem = java.util.regex.Pattern.compile(
                "(?:Pemeriksaan|Permintaan)\\s*:\\s*(.+?)(?=(?:Status\\s*:|$))",
                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL
            ).matcher(block);
            if (mPem.find()) {
                String p = mPem.group(1).trim();
                p = p.replace("\\n", " ").replace("\\r", " ").replaceAll("\\r?\\n\\s*", " ").replaceAll("\\s+", " ").trim();
                item.pemeriksaan = p;
            } else {
                int relEnd = m.end() - start;
                String sub = (relEnd < block.length()) ? block.substring(relEnd).trim() : "";
                if (sub.startsWith(":")) sub = sub.substring(1).trim();
                String[] lines = sub.split("\\r?\\n");
                StringBuilder sb = new StringBuilder();
                for (String line : lines) {
                    String lt = line.replace("\\n", " ").replace("\\r", " ").trim();
                    if (!lt.isEmpty() && !lt.toLowerCase().startsWith("status")) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(lt);
                    }
                }
                item.pemeriksaan = sb.toString().replaceAll("\\s+", " ").trim();
            }

            java.util.regex.Matcher mStat = java.util.regex.Pattern.compile(
                "Status\\s*:\\s*(.+)",
                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL
            ).matcher(block);
            if (mStat.find()) {
                String s = mStat.group(1).trim();
                s = s.replace("\\n", " ").replace("\\r", " ").replaceAll("\\r?\\n\\s*", " ").replaceAll("\\s+", " ").trim();
                item.status = s;
            } else {
                item.status = "Sedang dalam proses alat lab / menunggu hasil bridging";
            }

            list.add(item);
        }
        return list;
    }

    public static boolean isLabOrderFormat(String lab) {
        if (lab == null || lab.trim().isEmpty()) return false;
        return lab.contains("No. Order") || lab.contains("No.Order") || lab.contains("PK20") || 
               lab.contains("DALAM PROSES ALAT") || lab.contains("Pemeriksaan :");
    }

    public static boolean hasLabOrder(String lab, int index) {
        java.util.List<LabOrderItem> items = parseLabOrders(lab);
        return index >= 0 && index < items.size();
    }

    public static String getLabOrderNo(String lab, int index) {
        java.util.List<LabOrderItem> items = parseLabOrders(lab);
        if (index >= 0 && index < items.size()) {
            return items.get(index).noOrder;
        }
        return "";
    }

    public static String getLabOrderWaktu(String lab, int index) {
        java.util.List<LabOrderItem> items = parseLabOrders(lab);
        if (index >= 0 && index < items.size()) {
            return items.get(index).waktu;
        }
        return "";
    }

    public static String getLabOrderPemeriksaan(String lab, int index) {
        java.util.List<LabOrderItem> items = parseLabOrders(lab);
        if (index >= 0 && index < items.size()) {
            return items.get(index).pemeriksaan;
        }
        return "";
    }

    public static String getLabOrderStatus(String lab, int index) {
        java.util.List<LabOrderItem> items = parseLabOrders(lab);
        if (index >= 0 && index < items.size()) {
            return items.get(index).status;
        }
        return "";
    }

    public static String getNamaPemeriksaanLab(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.trim().equals("-")) {
            return "-";
        }
        java.util.List<LabOrderItem> items = parseLabOrders(raw);
        if (!items.isEmpty()) {
            java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<String>();
            for (LabOrderItem it : items) {
                if (it.pemeriksaan != null && !it.pemeriksaan.trim().isEmpty()) {
                    String[] parts = it.pemeriksaan.split(",");
                    for (String p : parts) {
                        String pt = p.trim();
                        if (!pt.isEmpty() && !pt.equals("-")) {
                            set.add(pt);
                        }
                    }
                }
            }
            if (!set.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String s : set) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(s);
                }
                return sb.toString();
            }
        }
        
        String[] lines = raw.split("\\r?\\n");
        java.util.LinkedHashSet<String> namaTindakan = new java.util.LinkedHashSet<String>();
        for (String line : lines) {
            String l = line.trim();
            if (l.isEmpty() || l.startsWith("[") || l.startsWith("•") || l.startsWith("*") || 
                l.toLowerCase().startsWith("status") || l.toLowerCase().startsWith("no.") ||
                l.toLowerCase().startsWith("no_rawat") || l.toLowerCase().startsWith("no rawat") ||
                l.toLowerCase().startsWith("tanggal") || l.toLowerCase().startsWith("jam")) {
                if (l.toLowerCase().startsWith("permintaan :") || l.toLowerCase().startsWith("pemeriksaan :")) {
                    String sub = l.substring(l.indexOf(":") + 1).trim();
                    for (String s : sub.split(",")) { 
                        String st = s.trim();
                        if (!st.isEmpty() && !st.equals("-")) namaTindakan.add(st); 
                    }
                }
                continue;
            }
            if (l.toLowerCase().startsWith("permintaan :") || l.toLowerCase().startsWith("pemeriksaan :")) {
                String sub = l.substring(l.indexOf(":") + 1).trim();
                for (String s : sub.split(",")) { 
                    String st = s.trim();
                    if (!st.isEmpty() && !st.equals("-")) namaTindakan.add(st); 
                }
            } else if (l.endsWith(":") && !l.startsWith("-")) {
                String nama = l.substring(0, l.length() - 1).trim();
                if (!nama.equalsIgnoreCase("Permintaan") && !nama.equalsIgnoreCase("Pemeriksaan") && !nama.equalsIgnoreCase("Hasil")) {
                    namaTindakan.add(nama);
                }
            } else if (!l.startsWith("-") && !l.contains(" : ") && !l.contains(":")) {
                for (String s : l.split(",")) {
                    String st = s.trim();
                    if (!st.isEmpty() && !st.equals("-")) namaTindakan.add(st);
                }
            }
        }
        if (!namaTindakan.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String s : namaTindakan) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(s);
            }
            return sb.toString();
        }

        String cleaned = raw.replaceAll("(?i)\\[permintaan.*?\\]", "")
                            .replaceAll("(?i)•\\s*no\\.?\\s*order\\s*:[^\\n]+", "")
                            .replaceAll("(?i)status\\s*:[^\\n]+", "")
                            .replaceAll("(?i)no\\.?\\s*rawat\\s*:[^\\n]+", "")
                            .replaceAll("\\r?\\n", ", ")
                            .replaceAll("\\s+", " ")
                            .replaceAll(",\\s*,", ",")
                            .trim();
        if (cleaned.endsWith(":")) cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        if (cleaned.startsWith(",")) cleaned = cleaned.substring(1).trim();
        return cleaned.isEmpty() ? "-" : cleaned;
    }

    public static String getStatusPenunjangLab(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.trim().equals("-")) {
            return "-";
        }
        if (raw.contains("DALAM PROSES ALAT") || raw.contains("menunggu hasil bridging") || 
            raw.toLowerCase().contains("sedang dalam proses") || raw.toLowerCase().contains("menunggu hasil")) {
            return "Sedang Proses Alat / Menunggu Hasil";
        }
        return "Hasil Selesai / Terlampir";
    }

    public static String getPemeriksaanRad(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.trim().equals("-")) {
            return "-";
        }
        String[] lines = raw.split("\\r?\\n");
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<String>();
        for (String line : lines) {
            String l = line.trim();
            if (l.isEmpty() || l.startsWith("[") || l.startsWith("•") || l.startsWith("*") || 
                l.toLowerCase().startsWith("status") || l.toLowerCase().startsWith("no.") ||
                l.toLowerCase().startsWith("no_rawat") || l.toLowerCase().startsWith("no rawat") ||
                l.toLowerCase().startsWith("tanggal") || l.toLowerCase().startsWith("jam")) {
                if (l.toLowerCase().startsWith("permintaan :") || l.toLowerCase().startsWith("pemeriksaan :")) {
                    String sub = l.substring(l.indexOf(":") + 1).trim();
                    for (String s : sub.split(",")) { 
                        String st = s.trim();
                        if (!st.isEmpty() && !st.equals("-")) set.add(st); 
                    }
                }
                continue;
            }
            if (l.toLowerCase().startsWith("permintaan :") || l.toLowerCase().startsWith("pemeriksaan :")) {
                String sub = l.substring(l.indexOf(":") + 1).trim();
                for (String s : sub.split(",")) { 
                    String st = s.trim();
                    if (!st.isEmpty() && !st.equals("-")) set.add(st); 
                }
            } else if (l.endsWith(":") && !l.startsWith("-")) {
                String nama = l.substring(0, l.length() - 1).trim();
                if (!nama.equalsIgnoreCase("Permintaan") && !nama.equalsIgnoreCase("Pemeriksaan") && !nama.equalsIgnoreCase("Hasil")) {
                    set.add(nama);
                }
            } else if (lines.length == 1 && !l.contains(":")) {
                for (String s : l.split(",")) {
                    String st = s.trim();
                    if (!st.isEmpty() && !st.equals("-")) set.add(st);
                }
            }
        }
        if (!set.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String s : set) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(s);
            }
            return sb.toString();
        }
        String cleaned = raw.replaceAll("(?i)\\[permintaan.*?\\]", "")
                            .replaceAll("(?i)•\\s*no\\.?\\s*order\\s*:[^\\n]+", "")
                            .replaceAll("(?i)status\\s*:[^\\n]+", "")
                            .replaceAll("(?i)no\\.?\\s*rawat\\s*:[^\\n]+", "")
                            .replaceAll("\\r?\\n", ", ")
                            .replaceAll("\\s+", " ")
                            .replaceAll(",\\s*,", ",")
                            .trim();
        if (cleaned.endsWith(":")) cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        if (cleaned.startsWith(",")) cleaned = cleaned.substring(1).trim();
        return cleaned.isEmpty() ? "-" : cleaned;
    }

    public static String getStatusRad(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.trim().equals("-")) {
            return "-";
        }
        if (raw.toLowerCase().contains("dalam proses") || raw.toLowerCase().contains("menunggu")) {
            return "Sedang Proses / Menunggu Hasil";
        }
        return "Hasil Selesai / Terlampir";
    }

    public static String getPemeriksaanEKG(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.trim().equals("-")) {
            return "-";
        }
        String r = raw.trim();
        if (r.toLowerCase().contains("12 lead") || r.toLowerCase().contains("12-lead")) {
            return "EKG 12 Lead";
        }
        if (r.toLowerCase().startsWith("ekg :") || r.toLowerCase().startsWith("ekg:")) {
            return "EKG";
        }
        if (r.toLowerCase().contains("ekg")) {
            return "EKG";
        }
        return "EKG";
    }

    public static String getStatusEKG(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.trim().equals("-")) {
            return "-";
        }
        return "Hasil Selesai / Terlampir";
    }

    /**
     * [RM 03-2] Field "Jam Meninggal" hanya aktif kalau Kondisi Pulang =
     * Meninggal, dikosongkan otomatis kalau bukan.
     */
    private void perbaruiVisibilitasJamMeninggal(){
        boolean meninggal = RbKondisiMeninggal.isSelected();
        DTPKondisiPulangJam.setEnabled(meninggal);
        if(!meninggal){
            DTPKondisiPulangJam.setSekarang();
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
        pulihkanRegioStatusLokalisMedis("");
        if(kanvasLokalis != null){ kanvasLokalis.hapusCoretan(); }
        if(panelPenunjangMedis != null){
            panelPenunjangMedis.hapusFotoEKG();
            panelPenunjangMedis.perbaruiSemuaBadge();
        }
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
        CekSirReguler.setSelected(false); CekSirIrreguler.setSelected(false);
        CekSirKuat.setSelected(false); CekSirLemah.setSelected(false);
        CekSirKulitNormal.setSelected(false); CekSirKulitPucat.setSelected(false);
        CekSirJaundice.setSelected(false); CekSirCyanosis.setSelected(false); CekSirBerkeringat.setSelected(false);
        CekSirHangat.setSelected(false); CekSirDingin.setSelected(false);
        CekSirKering.setSelected(false); CekSirBasah.setSelected(false);
        CekSirCrtKurang2.setSelected(false); CekSirCrtLebih2.setSelected(false);
        CekSirAman.setSelected(false); CekSirMengancamJiwa.setSelected(false);

        // -- E. Kategori Pasien + Bayi Baru Lahir (APGAR/PAT) --
        boolean isBayiAuto = isNeonatusMedis(TglLahir.getText(), DTPCari1.getDate());
        if(isBayiAuto){
            KategoriPasien.setSelectedItem("Bayi Baru Lahir");
            resetKomponenBayiBaruLahir();
        } else {
            KategoriPasien.setSelectedIndex(0);
            TApgarCukupBulan.setText(""); TApgarCairanAmnionJernih.setText("");
            TApgarPernapasanMenangis.setText(""); TApgarTonusOtot.setText("");
            TPatPenampilan.setText(""); TPatUsahaNapas.setText(""); TPatPerfusiKulit.setText("");
        }
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
        if(CekDiagnosaKerja!=null){ CekDiagnosaKerja.setSelected(false); }
        if(KdDiagnosaKerja!=null){ KdDiagnosaKerja.setText(""); }
        if(TDiagnosaKerja!=null){ TDiagnosaKerja.setText(""); }
        if(CekDiagnosaBanding!=null){ CekDiagnosaBanding.setSelected(false); }
        if(TDiagnosaBanding!=null){ TDiagnosaBanding.setText(""); }
        perbaruiVisibilitasDiagnosisMedis();

        // -- N. Konsul dr. Spesialis --
        KonsulKdDokter.setText(""); KonsulNmDokter.setText(""); KonsulKdPoli.setText(""); KonsulNmPoli.setText("");

        // -- O. Edukasi --
        CekEdukasiDiberikan.setSelected(false);
        CekEdukasiTidakBisa.setSelected(false); TEdukasiTidakBisaKet.setText("");

        // -- P. Kondisi Saat Pulang --
        bgKondisiPulang.clearSelection();
        DTPKondisiPulangJam.setSekarang();
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

    /** Baris judul section (bgcolor teal lembut, span 2 kolom). */
    private String judulBaris(String judul){
        return "<tr class='isi'><td colspan='2' valign='middle' bgcolor='#E6F4F2' align='center'><font color='#0F766E'><b>"+judul+"</b></font></td></tr>";
    }

    /** Baris label:nilai dgn latar warna (utk hasil/kesimpulan yg perlu ditonjolkan). */
    private String barisWarna(String label,String nilai,String warna){
        return "<tr class='isi'><td valign='middle' width='35%'>"+label+"</td>"+
               "<td valign='middle' width='65%' bgcolor='"+warna+"' color='#ffffff'>"+((nilai==null||nilai.trim().isEmpty())?"-":nilai)+"</td></tr>";
    }

    /** Nilai dan satuan tanpa menghasilkan teks satuan menggantung saat nilai kosong. */
    private String nilaiSatuanRingkasan(String nilai,String satuan){
        if(nilai==null||nilai.trim().isEmpty()){
            return "-";
        }
        return nilai.trim()+(satuan==null||satuan.isEmpty()?"":" "+satuan);
    }

    /**
     * Grid ringkas dua kelompok per baris untuk angka klinis yang tidak nyaman
     * dibaca bila dirangkai sebagai satu kalimat panjang dengan pemisah koma.
     */
    private String gridDuaKolomRingkasan(String[][] item){
        StringBuilder grid = new StringBuilder();
        grid.append("<tr class='isi'><td colspan='2' valign='top' cellpadding='0' cellspacing='0'>")
            .append("<table width='100%' border='0' cellpadding='3px' cellspacing='0'>");
        for(int i=0;i<item.length;i+=2){
            grid.append("<tr>")
                .append("<td width='22%' bgcolor='#F7F9F8'>").append(item[i][0]).append("</td>")
                .append("<td width='28%'><b>").append(item[i][1]).append("</b></td>");
            if(i+1<item.length){
                grid.append("<td width='22%' bgcolor='#F7F9F8'>").append(item[i+1][0]).append("</td>")
                    .append("<td width='28%'><b>").append(item[i+1][1]).append("</b></td>");
            }else{
                grid.append("<td width='22%' bgcolor='#F7F9F8'>&nbsp;</td><td width='28%'>&nbsp;</td>");
            }
            grid.append("</tr>");
        }
        grid.append("</table></td></tr>");
        return grid.toString();
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

        // 1. PASIEN & PETUGAS
        h.append(judulBaris("PASIEN & PETUGAS"));
        h.append(baris("No.Rawat",TNoRw.getText()));
        h.append(baris("No.RM",TNoRM.getText()));
        h.append(baris("Nama Pasien",TPasien.getText()));
        h.append(baris("Tgl Lahir",TglLahir.getText()));
        h.append(baris("J.K.",Jk.getText()));
        h.append(baris("Dokter",KdDokter.getText()+" "+NmDokter.getText()));
        h.append(baris("Tgl Asuhan",TglAsuhan.getSelectedItem()==null?"-":TglAsuhan.getSelectedItem().toString()));
        String kategoriPasienStr = KategoriPasien.getSelectedItem()==null?"":KategoriPasien.getSelectedItem().toString();
        h.append(baris("Kategori Pasien",kategoriPasienStr));

        // 2. INTERVENSI PRE HOSPITAL
        h.append(judulBaris("INTERVENSI PRE HOSPITAL"));
        h.append(baris("Tindakan Pre Hospital",cekList(CekPreCervicalCollar,CekPreDefibrilasi,CekPreVtp,CekPreBalutBidai,CekPreNgt,CekPreRjp,CekPreIntubasi,CekPreDekompresiWsd,CekPreKateterUrin,CekPreInfus,CekPreTidakAda)));
        h.append(baris("Keterangan Obat",TPreObatKet.getText()));

        // 3. SURVEI PRIMER - JALAN NAPAS
        h.append(judulBaris("SURVEI PRIMER - JALAN NAPAS"));
        h.append(baris("Kondisi",cekList(CekJnPaten,CekJnObstruksiPartial,CekJnStridor,CekJnSnoring,CekJnGurgling,CekJnWheezing,CekJnObstruksiTotal,CekJnTraumaJalanNapas,CekJnFasialLeherInhalasi,CekJnRisikoAspirasi,CekJnBendaAsing)));
        h.append(baris("Kesimpulan",pilihan(RbJnAman,RbJnMengancamJiwa)));

        // 4. SURVEI PRIMER - PERNAPASAN
        h.append(judulBaris("SURVEI PRIMER - PERNAPASAN"));
        h.append(baris("Pola / Reguler / Gerakan Dada",pilihan(RbNapSpontan,RbNapTidakSpontan)+" / "+pilihan(RbNapReguler,RbNapIrreguler)+" / "+pilihan(RbNapSimetris,RbNapAsimetris)));
        h.append(baris("Jejas",cekList(CekNapJejasKanan,CekNapJejasKiri)));
        h.append(baris("Tipe Napas",cekList(CekNapTipeNormal,CekNapTipeTakipneu,CekNapTipeKussmaul,CekNapTipeHiperventilasi,CekNapTipeBiot,CekNapTipeCheyneStoke,CekNapTipeApneustic,CekNapTipeRetraktif,CekNapTipeFlare)));
        h.append(baris("Kesimpulan",pilihan(RbNapAman,RbNapMengancamJiwa)));

        // 5. SURVEI PRIMER - SIRKULASI
        h.append(judulBaris("SURVEI PRIMER - SIRKULASI"));
        h.append(baris("Nadi",pilihan(CekSirReguler,CekSirIrreguler)+", "+pilihan(CekSirKuat,CekSirLemah)));
        h.append(baris("Kulit",pilihan(CekSirKulitNormal,CekSirKulitPucat)+", "+pilihan(CekSirJaundice,CekSirCyanosis)+(CekSirBerkeringat.isSelected()?", Berkeringat":"")));
        h.append(baris("Akral",pilihan(CekSirHangat,CekSirDingin)+", "+pilihan(CekSirKering,CekSirBasah)));
        h.append(baris("CRT",pilihan(CekSirCrtKurang2,CekSirCrtLebih2)));
        h.append(baris("Kesimpulan",pilihan(CekSirAman,CekSirMengancamJiwa)));

        // 6. SURVEI PRIMER - DISABILITAS
        h.append(judulBaris("SURVEI PRIMER - DISABILITAS"));
        h.append(baris("GCS","E"+TGcsE.getText()+" V"+TGcsV.getText()+" M"+TGcsM.getText()));
        h.append(baris("Pupil",pilihan(RbPupilIsokor,RbPupilAnisokor)+" (Kiri: "+TDiameterPupilKiri.getText()+", Kanan: "+TDiameterPupilKanan.getText()+")"));
        h.append(baris("Reflek Cahaya","Kiri: "+pilihan(RbReflekKiriPositif,RbReflekKiriNegatif)+", Kanan: "+pilihan(RbReflekKananPositif,RbReflekKananNegatif)));
        h.append(baris("Meningeal Signs",TMeningealSigns.getText()));
        h.append(baris("Lateralisasi",pilihan(RbLateralisasiKanan,RbLateralisasiKiri,RbLateralisasiTidakAda)));

        // 7. SURVEI PRIMER - EKSPOSUR
        h.append(judulBaris("SURVEI PRIMER - EKSPOSUR"));
        h.append(baris("Kondisi",cekList(CekEksDeformitas,CekEksEkskoriasi,CekEksContusio,CekEksAbrasi,CekEksPenetrasi,CekEksBurn,CekEksTenderness,CekEksLaserasi,CekEksSwelling,CekEksTidakTampakJelas)));

        // 8. KHUSUS BAYI BARU LAHIR (BILA KATEGORI = BAYI BARU LAHIR)
        if("Bayi Baru Lahir".equalsIgnoreCase(kategoriPasienStr)){
            h.append(judulBaris("EVALUASI BAYI BARU LAHIR & SEGITIGA PAT"));
            h.append(baris("Evaluasi APGAR","Cukup Bulan: "+TApgarCukupBulan.getText()+", Cairan Amnion Jernih: "+TApgarCairanAmnionJernih.getText()+", Pernapasan/Menangis: "+TApgarPernapasanMenangis.getText()+", Tonus Otot: "+TApgarTonusOtot.getText()));
            h.append(baris("Segitiga PAT","Penampilan: "+TPatPenampilan.getText()+", Usaha Napas: "+TPatUsahaNapas.getText()+", Perfusi Kulit: "+TPatPerfusiKulit.getText()));
            h.append(baris("Kesan PAT",getKesanPAT(TPatPenampilan.getText(),TPatUsahaNapas.getText(),TPatPerfusiKulit.getText()).replace("\n"," - ")));
        }

        // 9. RIWAYAT KESEHATAN (ANAMNESIS) - HANYA FIELD YANG DIINPUT DOKTER
        h.append(judulBaris("RIWAYAT KESEHATAN (ANAMNESIS)"));
        h.append(baris("Jenis Anamnesis",Anamnesis.getSelectedItem()==null?"-":Anamnesis.getSelectedItem().toString()));
        if(Hubungan.getText()!=null && !Hubungan.getText().trim().isEmpty() && !"-".equals(Hubungan.getText().trim())){
            h.append(baris("Hubungan Keluarga",Hubungan.getText().trim()));
        }
        h.append(baris("Keluhan Utama / Anamnesis",KeluhanUtama.getText()));

        // 10. PEMERIKSAAN UMUM, FISIK & STATUS LOKALIS
        h.append(judulBaris("PEMERIKSAAN UMUM, FISIK & STATUS LOKALIS"));
        h.append(baris("Keadaan Umum",Keadaan.getSelectedItem()==null?"-":Keadaan.getSelectedItem().toString()));
        h.append(baris("Kesadaran",Kesadaran.getSelectedItem()==null?"-":Kesadaran.getSelectedItem().toString()));
        String kf = KetFisik.getText()!=null ? KetFisik.getText().trim() : "";
        if(!kf.isEmpty() && !kf.equals(TEMPLATE_FISIK_KOSONG.trim()) && !kf.equals("-")){
            h.append(baris("Pemeriksaan Fisik (Head-to-Toe)",kf));
        } else {
            h.append(baris("Pemeriksaan Fisik (Head-to-Toe)","-"));
        }
        h.append(baris("Keterangan Lokalis",KetLokalis.getText().trim().isEmpty() ? "-" : KetLokalis.getText().trim()));

        // 11. PEMERIKSAAN PENUNJANG
        h.append(judulBaris("PEMERIKSAAN PENUNJANG"));
        h.append(baris("Laborat", getNamaPemeriksaanLab(Laborat.getText())));
        h.append(baris("Radiologi", getPemeriksaanRad(Radiologi.getText())));
        h.append(baris("EKG", getPemeriksaanEKG(EKG.getText())));
        String pLain = PemeriksaanLainnya.getText() != null ? PemeriksaanLainnya.getText().trim() : "";
        if (!pLain.isEmpty() && !pLain.equals("-")) {
            h.append(baris("Pemeriksaan Lainnya", pLain));
        }

        // 12. DIAGNOSIS
        h.append(judulBaris("DIAGNOSIS"));
        String dKerjaRingkas = teksDiagnosisKerjaMedis();
        String dBandingRingkas = teksDiagnosisBandingMedis();
        h.append(baris("Diagnosa Kerja", dKerjaRingkas.isEmpty() ? "-" : dKerjaRingkas));
        h.append(baris("Diagnosa Banding", dBandingRingkas.isEmpty() ? "-" : dBandingRingkas));

        // 13. RENCANA / TINDAKAN / TERAPI
        h.append(judulBaris("RENCANA / TINDAKAN / TERAPI"));
        h.append(baris("Tata Laksana / Terapi Medis", Tatalaksana.getText()));
        h.append(baris("Konsul Dokter Spesialis",KonsulKdDokter.getText().trim().isEmpty()?"-":KonsulKdDokter.getText()+" "+KonsulNmDokter.getText()));
        h.append(baris("Unit/Poli Konsul",KonsulKdPoli.getText().trim().isEmpty()?"-":KonsulKdPoli.getText()+" "+KonsulNmPoli.getText()));
        h.append(baris("Edukasi",CekEdukasiDiberikan.isSelected()?"Diberikan":(CekEdukasiTidakBisa.isSelected()?"Tidak Bisa ("+TEdukasiTidakBisaKet.getText()+")":"-")));

        // 14. KONDISI SAAT INI / KELUAR IGD & TANDA VITAL TERAKHIR
        h.append(judulBaris("KONDISI SAAT INI / KELUAR IGD"));
        String kondisiPulangStr = pilihan(RbKondisiMembaik,RbKondisiMemburuk,RbKondisiTetap,RbKondisiMeninggal,RbKondisiDoa);
        h.append(barisWarna("Kondisi Saat Ini / Keluar IGD",kondisiPulangStr+(RbKondisiMeninggal.isSelected()?(", Meninggal "+DTPKondisiPulangJam.getTampil()):""),
            RbKondisiMeninggal.isSelected()||RbKondisiDoa.isSelected()||RbKondisiMemburuk.isSelected()?"#AA0000":"#00AA00"));

        h.append(judulBaris("TANDA VITAL TERAKHIR"));
        h.append(gridDuaKolomRingkasan(new String[][]{
            {"Tekanan Darah",nilaiSatuanRingkasan(TTdAkhir.getText(),"mmHg")},
            {"Nadi",nilaiSatuanRingkasan(TNadiAkhir.getText(),"x/menit")},
            {"Pernapasan",nilaiSatuanRingkasan(TRrAkhir.getText(),"x/menit")},
            {"Suhu",nilaiSatuanRingkasan(TSuhuAkhir.getText(),"°C")}
        }));

        LoadHTML.setText(
            "<html><table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
            h.toString()+
            "</table></html>"
        );
        posisikanRingkasanHtmlKeAtas();
    }

    /** Pastikan ringkasan dibuka dari awal setelah layout HTML selesai. */
    private void posisikanRingkasanHtmlKeAtas(){
        LoadHTML.setCaretPosition(0);
        ScrollHTML.getViewport().setViewPosition(new java.awt.Point(0, 0));
        javax.swing.SwingUtilities.invokeLater(() -> {
            LoadHTML.setCaretPosition(0);
            ScrollHTML.getVerticalScrollBar().setValue(ScrollHTML.getVerticalScrollBar().getMinimum());
            ScrollHTML.getViewport().setViewPosition(new java.awt.Point(0, 0));
        });
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
                        pulihkanRegioStatusLokalisMedis(KetLokalis.getText());
                        if(kanvasLokalis != null){
                            try {
                                kanvasLokalis.setCoretanBase64(rsData.getString("lokalis_canvas_gambar"));
                            } catch(Exception eCol){
                                kanvasLokalis.hapusCoretan();
                            }
                        }
                        String ekgDb = rsData.getString("ekg");
                        String ekgClean = getPemeriksaanEKG(ekgDb);
                        EKG.setText("-".equals(ekgClean) ? "" : ekgClean);

                        String radDb = rsData.getString("rad");
                        String radClean = getPemeriksaanRad(radDb);
                        Radiologi.setText("-".equals(radClean) ? "" : radClean);

                        String labDb = rsData.getString("lab");
                        String labClean = getNamaPemeriksaanLab(labDb);
                        Laborat.setText("-".equals(labClean) ? "" : labClean);
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
                        CekSirReguler.setSelected("Reguler".equals(sirNadi));
                        CekSirIrreguler.setSelected("Irreguler".equals(sirNadi));

                        String sirNadiKekuatan = rsData.getString("sir_nadi_kekuatan");
                        CekSirKuat.setSelected("Kuat".equals(sirNadiKekuatan));
                        CekSirLemah.setSelected("Lemah".equals(sirNadiKekuatan));

                        String sirKulitWarna = rsData.getString("sir_kulit_warna");
                        CekSirKulitNormal.setSelected("Normal".equals(sirKulitWarna));
                        CekSirKulitPucat.setSelected("Pucat".equals(sirKulitWarna));

                        String sirKulitKondisi = rsData.getString("sir_kulit_kondisi");
                        CekSirJaundice.setSelected("Jaundice".equals(sirKulitKondisi));
                        CekSirCyanosis.setSelected("Cyanosis".equals(sirKulitKondisi));

                        CekSirBerkeringat.setSelected("Ya".equals(rsData.getString("sir_berkeringat")));

                        String sirAkral = rsData.getString("sir_akral");
                        CekSirHangat.setSelected("Hangat".equals(sirAkral));
                        CekSirDingin.setSelected("Dingin".equals(sirAkral));

                        String sirAkralKondisi = rsData.getString("sir_akral_kondisi");
                        CekSirKering.setSelected("Kering".equals(sirAkralKondisi));
                        CekSirBasah.setSelected("Basah".equals(sirAkralKondisi));

                        String sirCrt = rsData.getString("sir_crt");
                        CekSirCrtKurang2.setSelected("<2 detik".equals(sirCrt));
                        CekSirCrtLebih2.setSelected(">2 detik".equals(sirCrt));

                        String sirKesimpulan = rsData.getString("sir_kesimpulan");
                        CekSirAman.setSelected("Aman".equals(sirKesimpulan));
                        CekSirMengancamJiwa.setSelected("Mengancam Jiwa".equals(sirKesimpulan));

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
                        sinkronkanKomponenBayiBaruLahirDariTeks();

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
                        String dKerja = rsData.getString("diagnosa_kerja");
                        String dKerjaCek = rsData.getString("diagnosa_kerja_cek");
                        if(CekDiagnosaKerja!=null){
                            CekDiagnosaKerja.setSelected("Ya".equals(dKerjaCek) || (dKerja!=null && !dKerja.trim().isEmpty()));
                        }
                        if(KdDiagnosaKerja!=null){
                            KdDiagnosaKerja.setText(rsData.getString("diagnosa_kerja_kd")!=null ? rsData.getString("diagnosa_kerja_kd") : "");
                        }
                        if(TDiagnosaKerja!=null){
                            TDiagnosaKerja.setText(dKerja!=null ? dKerja : "");
                        }

                        String dBanding = rsData.getString("diagnosa_banding");
                        String dBandingCek = rsData.getString("diagnosa_banding_cek");
                        if(CekDiagnosaBanding!=null){
                            CekDiagnosaBanding.setSelected("Ya".equals(dBandingCek) || (dBanding!=null && !dBanding.trim().isEmpty()));
                        }
                        if(TDiagnosaBanding!=null){
                            TDiagnosaBanding.setText(dBanding!=null ? dBanding : "");
                        }
                        perbaruiVisibilitasDiagnosisMedis();

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
                            DTPKondisiPulangJam.setNilai(rsData.getString("tanggal"),kondisiPulangJam);
                        }
                        perbaruiVisibilitasJamMeninggal();

                        // -- Q. Tanda Vital Terakhir --
                        TTdAkhir.setText(rsData.getString("td_akhir"));
                        TNadiAkhir.setText(rsData.getString("nadi_akhir"));
                        TRrAkhir.setText(rsData.getString("rr_akhir"));
                        TSuhuAkhir.setText(rsData.getString("suhu_akhir"));

                        if (panelPenunjangMedis != null) {
                            panelPenunjangMedis.perbaruiSemuaBadge();
                        }

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
                    if (DTPCari1.getDate() != null && DTPCari2.getDate() != null && DTPCari1.getDate().after(DTPCari2.getDate())) {
                        DTPCari1.setDate(DTPCari2.getDate());
                    }
                    TPasien.setText(rs.getString("nm_pasien"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    TanggalRegistrasi.setText(rs.getString("tgl_registrasi")+" "+rs.getString("jam_reg"));
                    if(Sequel.cariInteger("select count(*) from penilaian_medis_igd where no_rawat=?", TNoRw.getText()) == 0) {
                        if(TAMPILKAN_TTV_MEDIS) {
                            tarikDataTriase(TNoRw.getText(), false);
                        }
                        if(isNeonatusMedis(rs.getString("tgl_lahir"), rs.getDate("tgl_registrasi"))) {
                            KategoriPasien.setSelectedItem("Bayi Baru Lahir");
                        } else {
                            KategoriPasien.setSelectedItem("Umum");
                        }
                        perbaruiVisibilitasBayiBaruLahir();
                    } else {
                        String katSaved = Sequel.cariIsi("select kategori_pasien from penilaian_medis_igd where no_rawat=?", TNoRw.getText());
                        if(katSaved != null && !katSaved.trim().isEmpty()) {
                            KategoriPasien.setSelectedItem(katSaved.trim());
                            perbaruiVisibilitasBayiBaruLahir();
                        }
                    }
                    if (panelPenunjangMedis != null) {
                        panelPenunjangMedis.setKoneksiServer(koneksi, KdDokter.getText().trim());
                        panelPenunjangMedis.perbaruiSemuaBadge();
                    }
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
        DTPCari2.setDate(tgl2 != null ? tgl2 : new java.util.Date());
        if (DTPCari2.getDate() != null && DTPCari2.getDate().before(new java.util.Date())) {
            DTPCari2.setDate(new java.util.Date());
        }
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
        "keadaan","kesadaran","td","nadi","rr","suhu","spo","bb","tb","ket_fisik","ket_lokalis","lokalis_canvas_gambar","ekg","rad","lab","tata",

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

        "diagnosa_kerja_cek","diagnosa_kerja_kd","diagnosa_kerja","diagnosa_banding_cek","diagnosa_banding",

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

        String sirNadi = CekSirReguler.isSelected() ? "Reguler" : (CekSirIrreguler.isSelected() ? "Irreguler" : null);
        String sirNadiKekuatan = CekSirKuat.isSelected() ? "Kuat" : (CekSirLemah.isSelected() ? "Lemah" : null);
        String sirKulitWarna = CekSirKulitNormal.isSelected() ? "Normal" : (CekSirKulitPucat.isSelected() ? "Pucat" : null);
        String sirKulitKondisi = CekSirJaundice.isSelected() ? "Jaundice" : (CekSirCyanosis.isSelected() ? "Cyanosis" : null);
        String sirAkral = CekSirHangat.isSelected() ? "Hangat" : (CekSirDingin.isSelected() ? "Dingin" : null);
        String sirAkralKondisi = CekSirKering.isSelected() ? "Kering" : (CekSirBasah.isSelected() ? "Basah" : null);
        String sirCrt = CekSirCrtKurang2.isSelected() ? "<2 detik" : (CekSirCrtLebih2.isSelected() ? ">2 detik" : null);
        String sirKesimpulan = CekSirAman.isSelected() ? "Aman" : (CekSirMengancamJiwa.isSelected() ? "Mengancam Jiwa" : null);

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
        String kondisiPulangJam = RbKondisiMeninggal.isSelected() ? DTPKondisiPulangJam.getJamSql() : null;

        return new String[]{
            Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),
            KdDokter.getText(),Anamnesis.getSelectedItem().toString(),Hubungan.getText(),KeluhanUtama.getText(),
            RPS.getText(),RPD.getText(),RPK.getText(),RPO.getText(),Alergi.getText(),
            Keadaan.getSelectedItem().toString(),Kesadaran.getSelectedItem().toString(),TD.getText(),Nadi.getText(),
            RR.getText(),Suhu.getText(),SPO.getText(),BB.getText(),TB.getText(),KetFisik.getText(),
            KetLokalis.getText(),(kanvasLokalis!=null?kanvasLokalis.getCoretanBase64():""),EKG.getText(),Radiologi.getText(),Laborat.getText(),Tatalaksana.getText(),

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

            (!TDiagnosaKerja.getText().trim().isEmpty() ? "Ya" : ya(CekDiagnosaKerja)),
            (KdDiagnosaKerja!=null ? KdDiagnosaKerja.getText() : ""),
            TDiagnosaKerja.getText().trim(),
            (TDiagnosaBanding!=null && !TDiagnosaBanding.getText().trim().isEmpty() ? "Ya" : ya(CekDiagnosaBanding)),
            (TDiagnosaBanding!=null ? TDiagnosaBanding.getText().trim() : ""),

            KonsulKdDokter.getText(),KonsulKdPoli.getText(),

            ya(CekEdukasiDiberikan),ya(CekEdukasiTidakBisa),TEdukasiTidakBisaKet.getText(),

            kondisiPulang,kondisiPulangJam,

            TTdAkhir.getText(),TNadiAkhir.getText(),TRrAkhir.getText(),TSuhuAkhir.getText()
        };
    }

    private void simpanDiagnosisBandingMedis(String noRawat){
        try{
            PreparedStatement psHapusDiagnosis = koneksi.prepareStatement(
                "delete from penilaian_medis_igd_diagnosa_banding where no_rawat=?");
            try{
                psHapusDiagnosis.setString(1,noRawat);
                psHapusDiagnosis.executeUpdate();
            }finally{
                psHapusDiagnosis.close();
            }
        }catch(Exception e){
            // Opsional: bersihkan jika tabel ada
        }
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

        boolean autoCommitLama = true;
        boolean sukses = false;
        try{
            autoCommitLama = koneksi.getAutoCommit();
            if(autoCommitLama){ koneksi.setAutoCommit(false); }
            if(!Sequel.mengedittf("penilaian_medis_igd","no_rawat=?",set.toString(),param.length,param)){
                throw new SQLException("Pembaruan data induk RM 03-2 gagal");
            }
            simpanDiagnosisBandingMedis(param[param.length-1]);
            if(autoCommitLama){ koneksi.commit(); }
            sukses = true;
        }catch(Exception e){
            if(autoCommitLama){
                try{ koneksi.rollback(); }catch(Exception rollbackError){ System.out.println("Notif rollback Ganti RM 03-2 : "+rollbackError); }
            }
            System.out.println("Notif Ganti RM 03-2 : "+e);
            JOptionPane.showMessageDialog(null,"Maaf, gagal mengganti data penilaian medis IGD.");
        }finally{
            if(autoCommitLama){
                try{ koneksi.setAutoCommit(true); }catch(Exception e){ System.out.println("Notif autoCommit Ganti RM 03-2 : "+e); }
            }
        }
        if(sukses){
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
        boolean autoCommitLama = true;
        try{
            autoCommitLama = koneksi.getAutoCommit();
            if(autoCommitLama){ koneksi.setAutoCommit(false); }
            PreparedStatement psSimpan = koneksi.prepareStatement(
                "insert into penilaian_medis_igd ("+kolom+") values ("+placeholder+")");
            try{
                for(int i=0;i<param.length;i++){
                    psSimpan.setString(i+1,param[i]);
                }
                psSimpan.executeUpdate();
            } finally {
                if(psSimpan!=null){ psSimpan.close(); }
            }
            simpanDiagnosisBandingMedis(TNoRw.getText());
            if(autoCommitLama){ koneksi.commit(); }
            sukses = true;
        }catch(Exception e){
            if(autoCommitLama){
                try{ koneksi.rollback(); }catch(Exception rollbackError){ System.out.println("Notif rollback Simpan RM 03-2 : "+rollbackError); }
            }
            System.out.println("Notif Simpan RM 03-2 : "+e);
            JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan No.Rawat yang sama sudah dimasukkan sebelumnya...!");
        }finally{
            if(autoCommitLama){
                try{ koneksi.setAutoCommit(true); }catch(Exception e){ System.out.println("Notif autoCommit Simpan RM 03-2 : "+e); }
            }
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
