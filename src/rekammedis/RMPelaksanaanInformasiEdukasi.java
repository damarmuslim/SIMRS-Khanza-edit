/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
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
public final class RMPelaksanaanInformasiEdukasi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;   
    private String dpjp="";
    private String TANGGALMUNDUR="yes";
    private StringBuilder htmlContent;
    private MasterCariTemplateInformasiEdukasi templateedukasi;
    private DlgCariPegawai pegawai;
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public RMPelaksanaanInformasiEdukasi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        widget.IconStyle.pasang(this);
        initRM02_1BagianB();
        this.setLocation(8,1);
        setSize(628,674);

        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat","No.R.M.","Nama Pasien","Umur","JK","Tgl.Lahir","Tanggal","Kode Petugas","Nama Petugas/Dokter/Perawat","Materi Edukasi",
            "Keterangan","Lama Edukasi","Status","Edukasi Kepada","Keterangan Edukasi Diberikan","Metode","Hasil Edukasi","Kode Leaflet","Tgl Re-Edukasi"
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 17; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(160);
            }else if(i==3){
                column.setPreferredWidth(35);
            }else if(i==4){
                column.setPreferredWidth(20);
            }else if(i==5){
                column.setPreferredWidth(65);
            }else if(i==6){
                column.setPreferredWidth(115);
            }else if(i==7){
                column.setPreferredWidth(90);
            }else if(i==8){
                column.setPreferredWidth(170);
            }else if(i==9){
                column.setPreferredWidth(250);
            }else if(i==10){
                column.setPreferredWidth(150);
            }else if(i==11){
                column.setPreferredWidth(75);
            }else if(i==12){
                column.setPreferredWidth(40);
            }else if(i==13){
                column.setPreferredWidth(85);
            }else if(i==14){
                column.setPreferredWidth(155);
            }else if(i==15){
                column.setPreferredWidth(70);
            }else if(i==16){
                column.setPreferredWidth(87);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        NIP.setDocument(new batasInput((byte)20).getKata(NIP));
        Keterangan.setDocument(new batasInput((byte)50).getKata(Keterangan));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
        Materi.setDocument(new batasInput((int)1000).getKata(Materi));
        Lama.setDocument(new batasInput((byte)10).getKata(Lama));
        KeteranganKepada.setDocument(new batasInput((byte)40).getKata(KeteranganKepada));
        
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
        // Document TERPISAH utk masing2 editorpane - sebelumnya keduanya
        // pakai 1 instance Document yg SAMA, jadi begitu LoadHTML2 (tab
        // "Bukti Pelaksanaan") ditulis panggilPhoto() ("Kosong"/foto), tab
        // "Ringkasan" (LoadHTML) IKUT ketiban krn merender document yg
        // identik - ditemukan dari laporan user (tab Ringkasan selalu
        // tampil "Kosong" walau ada data).
        LoadHTML.setDocument(kit.createDefaultDocument());
        LoadHTML2.setDocument(kit.createDefaultDocument());
        initRingkasanPelaksanaan();

        ChkInput.setSelected(false);
        isForm();
        jam();
        
        ChkAccor.setSelected(false);
        isPhoto();
        
        try {
            TANGGALMUNDUR=koneksiDB.TANGGALMUNDUR();
        } catch (Exception e) {
            TANGGALMUNDUR="yes";
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetakPelaksanaanEdukasi = new javax.swing.JMenuItem();
        JK = new widget.TextBox();
        Umur = new widget.TextBox();
        TanggalRegistrasi = new widget.TextBox();
        LoadHTML = new widget.editorpane();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        Tanggal = new widget.Tanggal();
        TNoRM = new widget.TextBox();
        jLabel16 = new widget.Label();
        Jam = new widget.ComboBox();
        Menit = new widget.ComboBox();
        Detik = new widget.ComboBox();
        ChkKejadian = new widget.CekBox();
        jLabel18 = new widget.Label();
        NIP = new widget.TextBox();
        NamaPetugas = new widget.TextBox();
        btnPetugas = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        jLabel12 = new widget.Label();
        Keterangan = new widget.TextBox();
        scrollPane1 = new widget.ScrollPane();
        Materi = new widget.TextArea();
        jLabel24 = new widget.Label();
        Kepada = new widget.ComboBox();
        jLabel20 = new widget.Label();
        KeteranganKepada = new widget.TextBox();
        jLabel13 = new widget.Label();
        Lama = new widget.TextBox();
        jLabel14 = new widget.Label();
        jLabel22 = new widget.Label();
        Metode = new widget.ComboBox();
        Hasil = new widget.ComboBox();
        jLabel23 = new widget.Label();
        jLabel25 = new widget.Label();
        Status = new widget.ComboBox();
        BtnSeek = new widget.Button();
        ChkInput = new widget.CekBox();
        PanelAccor = new widget.PanelBiasa();
        ChkAccor = new widget.CekBox();
        FormPhoto = new widget.PanelBiasa();
        FormPass3 = new widget.PanelBiasa();
        btnAmbil = new widget.Button();
        BtnRefreshPhoto1 = new widget.Button();
        Scroll5 = new widget.ScrollPane();
        LoadHTML2 = new widget.editorpane();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnCetakPelaksanaanEdukasi.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakPelaksanaanEdukasi.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        MnCetakPelaksanaanEdukasi.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakPelaksanaanEdukasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCetakPelaksanaanEdukasi.setText("Formulir Pelaksanaan Informasi & Edukasi");
        MnCetakPelaksanaanEdukasi.setName("MnCetakPelaksanaanEdukasi"); // NOI18N
        MnCetakPelaksanaanEdukasi.setPreferredSize(new java.awt.Dimension(260, 26));
        MnCetakPelaksanaanEdukasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCetakPelaksanaanEdukasiActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCetakPelaksanaanEdukasi);

        JK.setHighlighter(null);
        JK.setName("JK"); // NOI18N

        Umur.setHighlighter(null);
        Umur.setName("Umur"); // NOI18N

        TanggalRegistrasi.setHighlighter(null);
        TanggalRegistrasi.setName("TanggalRegistrasi"); // NOI18N

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pelaksanaan Informasi & Edukasi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

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

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass8.add(LCount);

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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tanggal :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "06-02-2026" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "06-02-2026" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(310, 23));
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

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
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
        panelGlass9.add(BtnAll);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 204));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setBackground(new java.awt.Color(250, 255, 245));
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 225));
        FormInput.setLayout(null);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 80, 23);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(84, 10, 136, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPasienKeyPressed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(336, 10, 285, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "06-02-2026" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(84, 40, 90, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(222, 10, 112, 23);

        jLabel16.setText("Tanggal :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 40, 80, 23);

        Jam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        Jam.setName("Jam"); // NOI18N
        Jam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JamKeyPressed(evt);
            }
        });
        FormInput.add(Jam);
        Jam.setBounds(178, 40, 62, 23);

        Menit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Menit.setName("Menit"); // NOI18N
        Menit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenitKeyPressed(evt);
            }
        });
        FormInput.add(Menit);
        Menit.setBounds(243, 40, 62, 23);

        Detik.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Detik.setName("Detik"); // NOI18N
        Detik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DetikKeyPressed(evt);
            }
        });
        FormInput.add(Detik);
        Detik.setBounds(308, 40, 62, 23);

        ChkKejadian.setBorder(null);
        ChkKejadian.setSelected(true);
        ChkKejadian.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        ChkKejadian.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setName("ChkKejadian"); // NOI18N
        FormInput.add(ChkKejadian);
        ChkKejadian.setBounds(373, 40, 23, 23);

        jLabel18.setText("Kepada :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(0, 150, 80, 23);

        NIP.setEditable(false);
        NIP.setHighlighter(null);
        NIP.setName("NIP"); // NOI18N
        NIP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NIPKeyPressed(evt);
            }
        });
        FormInput.add(NIP);
        NIP.setBounds(474, 40, 94, 23);

        NamaPetugas.setEditable(false);
        NamaPetugas.setName("NamaPetugas"); // NOI18N
        FormInput.add(NamaPetugas);
        NamaPetugas.setBounds(570, 40, 187, 23);

        btnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPetugas.setMnemonic('2');
        btnPetugas.setToolTipText("ALt+2");
        btnPetugas.setName("btnPetugas"); // NOI18N
        btnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPetugasActionPerformed(evt);
            }
        });
        btnPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPetugasKeyPressed(evt);
            }
        });
        FormInput.add(btnPetugas);
        btnPetugas.setBounds(761, 40, 28, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(625, 10, 60, 23);

        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(689, 10, 100, 23);

        jLabel12.setText("Keterangan :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(0, 120, 80, 23);

        Keterangan.setFocusTraversalPolicyProvider(true);
        Keterangan.setName("Keterangan"); // NOI18N
        Keterangan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganKeyPressed(evt);
            }
        });
        FormInput.add(Keterangan);
        Keterangan.setBounds(84, 120, 335, 23);

        scrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane1.setName("scrollPane1"); // NOI18N

        Materi.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Materi.setColumns(20);
        Materi.setRows(5);
        Materi.setName("Materi"); // NOI18N
        Materi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MateriKeyPressed(evt);
            }
        });
        scrollPane1.setViewportView(Materi);

        FormInput.add(scrollPane1);
        scrollPane1.setBounds(84, 70, 705, 43);

        jLabel24.setText("Materi :");
        jLabel24.setName("jLabel24"); // NOI18N
        jLabel24.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel24);
        jLabel24.setBounds(0, 70, 80, 23);

        Kepada.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pasien", "Keluarga", "Lain-lain" }));
        Kepada.setName("Kepada"); // NOI18N
        Kepada.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KepadaKeyPressed(evt);
            }
        });
        FormInput.add(Kepada);
        Kepada.setBounds(84, 150, 95, 23);

        jLabel20.setText("Petugas :");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(400, 40, 70, 23);

        KeteranganKepada.setFocusTraversalPolicyProvider(true);
        KeteranganKepada.setName("KeteranganKepada"); // NOI18N
        KeteranganKepada.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganKepadaKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganKepada);
        KeteranganKepada.setBounds(182, 150, 190, 23);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel13.setText("menit");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(594, 120, 50, 23);

        Lama.setFocusTraversalPolicyProvider(true);
        Lama.setName("Lama"); // NOI18N
        Lama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LamaKeyPressed(evt);
            }
        });
        FormInput.add(Lama);
        Lama.setBounds(542, 120, 50, 23);

        jLabel14.setText("Lama Edukasi :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(448, 120, 90, 23);

        jLabel22.setText("Metode :");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(393, 150, 70, 23);

        Metode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ceramah", "Diskusi", "Demonstrasi" }));
        Metode.setName("Metode"); // NOI18N
        Metode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MetodeKeyPressed(evt);
            }
        });
        FormInput.add(Metode);
        Metode.setBounds(467, 150, 115, 23);

        Hasil.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sudah Mengerti", "Re-Edukasi", "Re-Demonstrasi" }));
        Hasil.setName("Hasil"); // NOI18N
        Hasil.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HasilKeyPressed(evt);
            }
        });
        FormInput.add(Hasil);
        Hasil.setBounds(659, 150, 130, 23);

        jLabel23.setText("Hasil :");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(585, 150, 70, 23);

        jLabel25.setText("Status :");
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(635, 120, 70, 23);

        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Awal", "Ulang" }));
        Status.setName("Status"); // NOI18N
        Status.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                StatusKeyPressed(evt);
            }
        });
        FormInput.add(Status);
        Status.setBounds(709, 120, 80, 23);

        BtnSeek.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek.setMnemonic('1');
        BtnSeek.setToolTipText("Alt+1");
        BtnSeek.setName("BtnSeek"); // NOI18N
        BtnSeek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeekActionPerformed(evt);
            }
        });
        FormInput.add(BtnSeek);
        BtnSeek.setBounds(52, 89, 28, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('I');
        ChkInput.setText(".: Input Data");
        ChkInput.setToolTipText("Alt+I");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        PanelAccor.setBackground(new java.awt.Color(255, 255, 255));
        PanelAccor.setName("PanelAccor"); // NOI18N
        PanelAccor.setPreferredSize(new java.awt.Dimension(430, 43));
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

        FormPhoto.setBackground(new java.awt.Color(255, 255, 255));
        FormPhoto.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), " Bukti Pelaksanaan : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        FormPhoto.setName("FormPhoto"); // NOI18N
        FormPhoto.setPreferredSize(new java.awt.Dimension(115, 73));
        FormPhoto.setLayout(new java.awt.BorderLayout());

        FormPass3.setBackground(new java.awt.Color(255, 255, 255));
        FormPass3.setBorder(null);
        FormPass3.setName("FormPass3"); // NOI18N
        FormPass3.setPreferredSize(new java.awt.Dimension(115, 40));

        btnAmbil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        btnAmbil.setMnemonic('U');
        btnAmbil.setText("Ambil");
        btnAmbil.setToolTipText("Alt+U");
        btnAmbil.setName("btnAmbil"); // NOI18N
        btnAmbil.setPreferredSize(new java.awt.Dimension(100, 30));
        btnAmbil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAmbilActionPerformed(evt);
            }
        });
        FormPass3.add(btnAmbil);

        BtnRefreshPhoto1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/refresh.png"))); // NOI18N
        BtnRefreshPhoto1.setMnemonic('U');
        BtnRefreshPhoto1.setText("Refresh");
        BtnRefreshPhoto1.setToolTipText("Alt+U");
        BtnRefreshPhoto1.setName("BtnRefreshPhoto1"); // NOI18N
        BtnRefreshPhoto1.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnRefreshPhoto1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRefreshPhoto1ActionPerformed(evt);
            }
        });
        FormPass3.add(BtnRefreshPhoto1);

        FormPhoto.add(FormPass3, java.awt.BorderLayout.PAGE_END);

        Scroll5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll5.setName("Scroll5"); // NOI18N
        Scroll5.setOpaque(true);
        Scroll5.setPreferredSize(new java.awt.Dimension(200, 200));

        LoadHTML2.setBorder(null);
        LoadHTML2.setName("LoadHTML2"); // NOI18N
        Scroll5.setViewportView(LoadHTML2);

        FormPhoto.add(Scroll5, java.awt.BorderLayout.CENTER);

        PanelAccor.add(FormPhoto, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelAccor, java.awt.BorderLayout.EAST);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{            
            Valid.pindah(evt,TCari,Tanggal);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPasienKeyPressed
        Valid.pindah(evt,TCari,BtnSimpan);
}//GEN-LAST:event_TPasienKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
        }else if(NIP.getText().trim().equals("")||NamaPetugas.getText().trim().equals("")){
            Valid.textKosong(NIP,"Petugas");
        }else{
            if(akses.getkode().equals("Admin Utama")){
                simpan();
            }else{
                if(TanggalRegistrasi.getText().equals("")){
                    TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                }
                if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem())==true){
                    simpan();
                }
            } 
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,Hasil,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
        ChkInput.setSelected(true);
        isForm(); 
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
                if(NIP.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString())){
                    if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString(),Sequel.ambiltanggalsekarang())==true){
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
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
        }else if(NIP.getText().trim().equals("")||NamaPetugas.getText().trim().equals("")){
            Valid.textKosong(NIP,"Petugas");
        }else{ 
            if(tbObat.getSelectedRow()>-1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else{
                    if(NIP.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString())){
                        if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString(),Sequel.ambiltanggalsekarang())==true){
                            if(TanggalRegistrasi.getText().equals("")){
                                TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                            }
                            if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem())==true){
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
                htmlContent = new StringBuilder();
                htmlContent.append(                             
                    "<tr class='isi'>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>No.Rawat</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>No.R.M.</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Nama Pasien</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Umur</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>JK</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Tgl.Lahir</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Tanggal</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Kode Petugas</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Nama Petugas/Dokter/Perawat</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Materi Edukasi</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Keterangan</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Lama Edukasi</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Status</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Edukasi Kepada</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Keterangan Edukasi Diberikan</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Metode</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Hasil Edukasi</b></td>"+
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
                        "</tr>");
                }
                
                LoadHTML.setText(
                    "<html>"+
                      "<table width='1700' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
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

                File f = new File("DataPelaksanaanEdukasiInformasi.html");            
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
                bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
                            "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                            "<table width='1700px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                "<tr class='isi2'>"+
                                    "<td valign='top' align='center'>"+
                                        "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                        akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                        akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                        "<font size='2' face='Tahoma'>DATA LAPORAN PELAKSANAAN INFORMASI & EDUKASI<br><br></font>"+        
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
            runBackground(() ->tampil());
            TCari.setText("");
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah(evt,TCari,Jam);
}//GEN-LAST:event_TanggalKeyPressed

    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed
        // Valid.pindah(evt, TNm, BtnSimpan);
}//GEN-LAST:event_TNoRMKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
            try {
                // Tab "Ringkasan" otomatis kebuka begitu klik baris - user tdk
                // perlu klik toggle accordion (ChkAccor) manual tiap kali.
                ChkAccor.setSelected(true);
                isPhoto();
                panggilPhoto();
            } catch (java.lang.NullPointerException e) {
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
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void JamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JamKeyPressed
        Valid.pindah(evt,Tanggal,Menit);
    }//GEN-LAST:event_JamKeyPressed

    private void MenitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MenitKeyPressed
        Valid.pindah(evt,Jam,Detik);
    }//GEN-LAST:event_MenitKeyPressed

    private void DetikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DetikKeyPressed
        Valid.pindah(evt,Menit,btnPetugas);
    }//GEN-LAST:event_DetikKeyPressed

    private void NIPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NIPKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            NamaPetugas.setText(Sequel.CariPegawai(NIP.getText()));
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Detik.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            Keterangan.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            btnPetugasActionPerformed(null);
        }
    }//GEN-LAST:event_NIPKeyPressed

    private void btnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugasActionPerformed
        if (pegawai == null || !pegawai.isDisplayable()) {
            pegawai=new DlgCariPegawai(null,false);
            pegawai.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            pegawai.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(pegawai.getTable().getSelectedRow()!= -1){                   
                        NIP.setText(pegawai.getTable().getValueAt(pegawai.getTable().getSelectedRow(),0).toString());
                        NamaPetugas.setText(pegawai.getTable().getValueAt(pegawai.getTable().getSelectedRow(),1).toString());
                    }  
                    NIP.requestFocus();
                    pegawai=null;
                }
            }); 
            pegawai.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            pegawai.setLocationRelativeTo(internalFrame1);
        }
        if (pegawai == null) return;
        if (!pegawai.isVisible()) {
            pegawai.emptTeks();
        }  
        if (pegawai.isVisible()) {
            pegawai.toFront();
            return;
        }     
        pegawai.setVisible(true);
    }//GEN-LAST:event_btnPetugasActionPerformed

    private void btnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPetugasKeyPressed
        Valid.pindah(evt,Detik,Materi);
    }//GEN-LAST:event_btnPetugasKeyPressed

    private void MnCetakPelaksanaanEdukasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCetakPelaksanaanEdukasiActionPerformed
        if(tbObat.getSelectedRow()>-1){
            Map<String, Object> param = new HashMap<>();
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());   
            dpjp=Sequel.cariIsi("select dokter.nm_dokter from dpjp_ranap inner join dokter on dpjp_ranap.kd_dokter=dokter.kd_dokter where dpjp_ranap.no_rawat=?",tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            if(dpjp.equals("")){
                dpjp=Sequel.cariIsi("select dokter.nm_dokter from reg_periksa inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter where reg_periksa.no_rawat=?",tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            }
            param.put("dpjp",dpjp);   
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            Valid.MyReportqry("rptFormulirPelaksanaanInformasiEdukasi.jasper","report","::[ Formulir Pelaksanaan Informasi & Edukasi ]::",
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,pasien.jk,pasien.tgl_lahir,pelaksanaan_informasi_edukasi.tanggal,pegawai.jbtn,reg_periksa.tgl_registrasi,"+
                    "pelaksanaan_informasi_edukasi.nik,pegawai.nama,pelaksanaan_informasi_edukasi.materi_edukasi,pelaksanaan_informasi_edukasi.keterangan,pelaksanaan_informasi_edukasi.diberikan_pada,reg_periksa.jam_reg,"+
                    "pelaksanaan_informasi_edukasi.keterangan_diberikan_pada,pelaksanaan_informasi_edukasi.lama_edukasi,pelaksanaan_informasi_edukasi.metode_edukasi,pelaksanaan_informasi_edukasi.hasil_verifikasi,"+
                    "pelaksanaan_informasi_edukasi.status,concat('http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/pelaksanaanedukasi/',bukti_pelaksanaan_informasi_edukasi.photo) as photo  "+
                    "from pelaksanaan_informasi_edukasi inner join reg_periksa on pelaksanaan_informasi_edukasi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join pegawai on pelaksanaan_informasi_edukasi.nik=pegawai.nik "+
                    "inner join bukti_pelaksanaan_informasi_edukasi on pelaksanaan_informasi_edukasi.no_rawat=bukti_pelaksanaan_informasi_edukasi.no_rawat "+
                    "and pelaksanaan_informasi_edukasi.tanggal=bukti_pelaksanaan_informasi_edukasi.tanggal "+     
                    "where reg_periksa.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"' "+
                    "order by pelaksanaan_informasi_edukasi.tanggal",param);
        }
    }//GEN-LAST:event_MnCetakPelaksanaanEdukasiActionPerformed

    private void KeteranganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganKeyPressed
        Valid.pindah(evt,Materi,Lama);
    }//GEN-LAST:event_KeteranganKeyPressed

    private void MateriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MateriKeyPressed
        Valid.pindah2(evt,btnPetugas,Keterangan);
    }//GEN-LAST:event_MateriKeyPressed

    private void KepadaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KepadaKeyPressed
        Valid.pindah(evt,Status,KeteranganKepada);
    }//GEN-LAST:event_KepadaKeyPressed

    private void KeteranganKepadaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganKepadaKeyPressed
        Valid.pindah(evt,Kepada,Metode);
    }//GEN-LAST:event_KeteranganKepadaKeyPressed

    private void LamaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LamaKeyPressed
        Valid.pindah(evt,Keterangan,Status);
    }//GEN-LAST:event_LamaKeyPressed

    private void MetodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MetodeKeyPressed
        Valid.pindah(evt,KeteranganKepada,Hasil);
    }//GEN-LAST:event_MetodeKeyPressed

    private void HasilKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HasilKeyPressed
        Valid.pindah(evt,Metode,BtnSimpan);
    }//GEN-LAST:event_HasilKeyPressed

    private void StatusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_StatusKeyPressed
        Valid.pindah(evt,Lama,Kepada);
    }//GEN-LAST:event_StatusKeyPressed

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
        if(tbObat.getSelectedRow()!= -1){
            isPhoto();
            panggilPhoto();
        }else{
            ChkAccor.setSelected(false);
            JOptionPane.showMessageDialog(null,"Silahkan pilih No.Pernyataan..!!!");
        }
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void btnAmbilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAmbilActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()>-1){
                Sequel.queryu("delete from antripelaksanaanedukasi");
                Sequel.queryu("insert into antripelaksanaanedukasi values('"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"','"+tbObat.getValueAt(tbObat.getSelectedRow(),6).toString()+"')");
                Sequel.queryu("delete from bukti_pelaksanaan_informasi_edukasi where no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"' and tanggal='"+tbObat.getValueAt(tbObat.getSelectedRow(),6).toString()+"'");
                // Antrian sudah disiapkan diatas - langsung buka webapp
                // pelaksanaanedukasi (Peran 1: Pasien/Keluarga, kamera.php)
                // di browser, spy staf tak perlu ketik URL manual.
                bukaWebappPelaksanaanEdukasi();
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
            }
        }
    }//GEN-LAST:event_btnAmbilActionPerformed

    /** Buka webapps/pelaksanaanedukasi/login.php (Peran 1, kamera.php) lgs di
     * browser default stlh antrian disiapkan - user tak perlu ketik URL
     * manual/scan QR fisik. login.php butuh parameter "?iyem=<JSON
     * terenkripsi>" (lihat webapps/conf/conf.php fungsi encrypt_decrypt) -
     * BELUM ada versi Java-nya di codebase ini sblm sesi ini (dicek dulu via
     * riset, bukan diasumsikan), jadi direplikasi manual di
     * encryptIyem()/sha256Hex() - diverifikasi byte-persis cocok dgn PHP
     * (roundtrip test lwt `php -r`) sebelum dipasang ke tombol sungguhan. */
    private void bukaWebappPelaksanaanEdukasi(){
        try{
            String iyem = encryptIyem(koneksiDB.USERHYBRIDWEB(), koneksiDB.PASHYBRIDWEB());
            Valid.panggilUrl("pelaksanaanedukasi/login.php?iyem="+java.net.URLEncoder.encode(iyem,"UTF-8"));
        }catch(Exception e){
            System.out.println("Notif bukaWebappPelaksanaanEdukasi : "+e);
        }
    }

    /** Replika PERSIS fungsi PHP encrypt_decrypt($string,"e") di
     * webapps/conf/conf.php: AES-256-CBC, key = 32 karakter PERTAMA (bukan
     * byte hex-decoded) dari sha256(secret_key) dlm bentuk hex-string, iv =
     * 16 karakter pertama sha256(secret_iv) - PHP openssl_encrypt() dgn
     * $options=0 (bukan OPENSSL_RAW_DATA) SUDAH auto base64-encode hasilnya,
     * lalu encrypt_decrypt() bungkus base64_encode() SEKALI LAGI di atasnya
     * (double base64) - kalau cuma di-base64 sekali, login.php PHP akan
     * gagal decode & selalu redirect ke Home (silent fail, tak ada pesan
     * error) - ditemukan lewat verifikasi byte-persis, bukan tebakan. */
    private String encryptIyem(String usere, String passwordte) throws Exception {
        String json = "{\"usere\":\""+usere+"\",\"passwordte\":\""+passwordte+"\"}";
        String secretKey = "Bar12345Bar12345";
        String secretIv = "sayangsamakhanza";
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        String keyHex = sha256Hex(sha256.digest(secretKey.getBytes(StandardCharsets.UTF_8)));
        String ivHex = sha256Hex(sha256.digest(secretIv.getBytes(StandardCharsets.UTF_8))).substring(0,16);
        byte[] keyBytes = keyHex.substring(0,32).getBytes(StandardCharsets.UTF_8);
        byte[] ivBytes = ivHex.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes,"AES"), new IvParameterSpec(ivBytes));
        byte[] enc = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));
        String once = Base64.getEncoder().encodeToString(enc);
        return Base64.getEncoder().encodeToString(once.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256Hex(byte[] digestBytes){
        StringBuilder sb = new StringBuilder();
        for(byte b : digestBytes){ sb.append(String.format("%02x", b)); }
        return sb.toString();
    }

    private void BtnRefreshPhoto1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRefreshPhoto1ActionPerformed
        if(tbObat.getSelectedRow()>-1){
            panggilPhoto();
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih No.Pernyataan terlebih dahulu..!!");
        }
    }//GEN-LAST:event_BtnRefreshPhoto1ActionPerformed

    private void BtnSeekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeekActionPerformed
        if (templateedukasi == null || !templateedukasi.isDisplayable()) {
            templateedukasi=new MasterCariTemplateInformasiEdukasi(null,false);
            templateedukasi.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            templateedukasi.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {}
                @Override
                public void windowClosing(WindowEvent e) {}
                @Override
                public void windowClosed(WindowEvent e) {
                    if(templateedukasi.getTable().getSelectedRow()!= -1){                   
                        Materi.setText(templateedukasi.getTable().getValueAt(templateedukasi.getTable().getSelectedRow(),1).toString());
                        Lama.setText(templateedukasi.getTable().getValueAt(templateedukasi.getTable().getSelectedRow(),2).toString());
                        Metode.setSelectedItem(templateedukasi.getTable().getValueAt(templateedukasi.getTable().getSelectedRow(),3).toString());
                    } 
                    Materi.requestFocus();
                    templateedukasi=null;
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
            templateedukasi.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            templateedukasi.setLocationRelativeTo(internalFrame1);
        }
        if (templateedukasi == null) return;
        if (!templateedukasi.isVisible()) {
            templateedukasi.isCek();    
            templateedukasi.emptTeks();
        }  
        if (templateedukasi.isVisible()) {
            templateedukasi.toFront();
            return;
        }    
        templateedukasi.setVisible(true);
    }//GEN-LAST:event_BtnSeekActionPerformed

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
            RMPelaksanaanInformasiEdukasi dialog = new RMPelaksanaanInformasiEdukasi(new javax.swing.JFrame(), true);
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
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnRefreshPhoto1;
    private widget.Button BtnSeek;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkAccor;
    private widget.CekBox ChkInput;
    private widget.CekBox ChkKejadian;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.ComboBox Detik;
    private widget.PanelBiasa FormInput;
    private widget.PanelBiasa FormPass3;
    private widget.PanelBiasa FormPhoto;
    private widget.ComboBox Hasil;
    private widget.TextBox JK;
    private widget.ComboBox Jam;
    private widget.ComboBox Kepada;
    private widget.TextBox Keterangan;
    private widget.TextBox KeteranganKepada;
    private widget.Label LCount;
    private widget.TextBox Lama;
    private widget.editorpane LoadHTML;
    private widget.editorpane LoadHTML2;
    private widget.TextArea Materi;
    private widget.ComboBox Menit;
    private widget.ComboBox Metode;
    private javax.swing.JMenuItem MnCetakPelaksanaanEdukasi;
    private widget.TextBox NIP;
    private widget.TextBox NamaPetugas;
    private widget.PanelBiasa PanelAccor;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll5;
    private widget.ComboBox Status;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal Tanggal;
    private widget.TextBox TanggalRegistrasi;
    private widget.TextBox TglLahir;
    private widget.TextBox Umur;
    private widget.Button btnAmbil;
    private widget.Button btnPetugas;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel16;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel4;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    // [RM 02-1 Bagian B] 2 field baru sesuai formulir fisik ("Kode Leaflet",
    // "Tgl Re-Edukasi") - lihat initRM02_1BagianB(), dipanggil dari
    // konstruktor setelah initComponents().
    private widget.TextBox KodeLeaflet;
    private widget.Tanggal TglReEdukasi;

    private void initRM02_1BagianB(){
        widget.Label lblKodeLeaflet = new widget.Label();
        lblKodeLeaflet.setText("Kode Leaflet :");
        lblKodeLeaflet.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        FormInput.add(lblKodeLeaflet);
        lblKodeLeaflet.setBounds(0, 180, 80, 23);

        KodeLeaflet = new widget.TextBox();
        FormInput.add(KodeLeaflet);
        KodeLeaflet.setBounds(84, 180, 200, 23);

        widget.Label lblTglReEdukasi = new widget.Label();
        lblTglReEdukasi.setText("Tgl Re-Edukasi :");
        lblTglReEdukasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        FormInput.add(lblTglReEdukasi);
        lblTglReEdukasi.setBounds(300, 180, 100, 23);

        TglReEdukasi = new widget.Tanggal();
        TglReEdukasi.setDisplayFormat("dd-MM-yyyy");
        FormInput.add(TglReEdukasi);
        TglReEdukasi.setBounds(404, 180, 100, 23);

        FormInput.setPreferredSize(new java.awt.Dimension(810, 210));
    }

    /** Kode Leaflet otomatis dari No.Rawat (format "yyyy/mm/dd/xxxxxx") -
     * permintaan user: tahun+bulan+tanggal + 4 digit terakhir No.Rawat.
     * Dibuat dgn buang semua karakter non-angka dulu (bukan split("/") kaku)
     * biar tetap jalan walau separator No.Rawat beda2 antar instalasi. */
    private String generateKodeLeaflet(String noRawat){
        if(noRawat==null) return "";
        String angka = noRawat.replaceAll("[^0-9]", "");
        if(angka.length() < 8) return angka;
        String tanggal = angka.substring(0, 8);
        String empatDigit = angka.substring(angka.length() - 4);
        return tanggal + empatDigit;
    }

    private widget.ScrollPane ScrollHTML;
    private javax.swing.JTabbedPane TabRingkasanTtd;

    /** Tab "Ringkasan" (LoadHTML, isi dari bangunRingkasanHtml()) + tab
     * "Bukti Pelaksanaan" (FormPass3+Scroll5/LoadHTML2, direparent dari GEN
     * block) di dalam FormPhoto - pola identik initRingkasanTransfer() di
     * RMTransferPasienAntarRuang.java (LoadHTML sendiri sudah ada dari GEN
     * block sejak awal, cuma dipakai buffer cetak/export - lihat
     * BtnPrintActionPerformed - belum pernah ditampilkan di panel manapun). */
    private widget.Button BtnPrint1;

    private void initRingkasanPelaksanaan(){
        // Tombol cetak RM 02-1 (Lembar/PDF) - DI DALAM tab Ringkasan (bukan
        // toolbar utama BtnPrint yg bulk-export) - pola identik BtnPrint1 di
        // RMTriaseIGD.java/RMTransferPasienAntarRuang.java.
        BtnPrint1 = new widget.Button();
        BtnPrint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item (copy).png")));
        BtnPrint1.setToolTipText("Cetak Formulir RM 02-1 (Edukasi Pasien & Keluarga) pasien ini");
        BtnPrint1.addActionListener((java.awt.event.ActionEvent evt) -> BtnPrint1ActionPerformed(evt));

        widget.PanelBiasa panelToolbarRingkasan = new widget.PanelBiasa();
        panelToolbarRingkasan.setLayout(new java.awt.BorderLayout());
        widget.Label lblRingkasan = new widget.Label();
        lblRingkasan.setText("Ringkasan & Cetak RM 02-1 :");
        panelToolbarRingkasan.add(lblRingkasan, java.awt.BorderLayout.WEST);
        widget.PanelBiasa panelBtnPrint1 = new widget.PanelBiasa();
        panelBtnPrint1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT,5,2));
        panelBtnPrint1.add(BtnPrint1);
        panelToolbarRingkasan.add(panelBtnPrint1, java.awt.BorderLayout.EAST);

        ScrollHTML = new widget.ScrollPane();
        ScrollHTML.setViewportView(LoadHTML);

        widget.PanelBiasa panelRingkasan = new widget.PanelBiasa();
        panelRingkasan.setLayout(new java.awt.BorderLayout());
        panelRingkasan.add(panelToolbarRingkasan, java.awt.BorderLayout.NORTH);
        panelRingkasan.add(ScrollHTML, java.awt.BorderLayout.CENTER);

        FormPhoto.remove(FormPass3);
        FormPhoto.remove(Scroll5);
        widget.PanelBiasa panelBukti = new widget.PanelBiasa();
        panelBukti.setLayout(new java.awt.BorderLayout());
        panelBukti.add(FormPass3, java.awt.BorderLayout.PAGE_END);
        panelBukti.add(Scroll5, java.awt.BorderLayout.CENTER);

        TabRingkasanTtd = new javax.swing.JTabbedPane();
        TabRingkasanTtd.addTab("Ringkasan", panelRingkasan);
        TabRingkasanTtd.addTab("Bukti Pelaksanaan", panelBukti);

        FormPhoto.add(TabRingkasanTtd, java.awt.BorderLayout.CENTER);
    }

    private void BtnPrint1ActionPerformed(java.awt.event.ActionEvent evt){
        if(TNoRw.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu..!!!!");
            return;
        }
        String modeCetak = (String)JOptionPane.showInputDialog(null,"Silahkan pilih mode cetak..!","Cetak Formulir Edukasi Pasien & Keluarga (RM 02-1)",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Lembar","PDF"},"Lembar");
        if(modeCetak==null){
            return;
        }
        cetakFormulirRM021(modeCetak);
    }

    /** Cetak RM 02-1 gabungan Bagian A (edukasi_pasien_keluarga_rj, 1 record
     * per no_rawat - dikirim scalar via parameter) + Bagian B
     * (pelaksanaan_informasi_edukasi, 1..N baris - dataset utama query) -
     * pola sama persis cetakLaporanTransfer(mode) di
     * RMTransferPasienAntarRuang.java. */
    private void cetakFormulirRM021(String mode){
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try{
            String noRawatCetak = TNoRw.getText();
            Map<String,Object> param = new HashMap<>();
            // Ambil dari objek "akses" (setting sudah di-load sekali saat
            // login), BUKAN query manual ke tabel setting - kolom SQL tebakan
            // sebelumnya (alamat/kota/telepon) SALAH, nama kolom sungguhan di
            // DB adalah alamat_instansi/kabupaten/kontak (ditemukan pas user
            // lapor kop cetak kosong) - pola akses.getXxx() ini persis sama
            // dgn yg sudah dipakai MnCetakPelaksanaanEdukasiActionPerformed
            // di file ini sendiri & RMTransferPasienAntarRuang.java.
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

            ps=koneksi.prepareStatement(
                "select pasien.no_rkm_medis,pasien.nm_pasien,date_format(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir,pasien.jk,poliklinik.nm_poli "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli where reg_periksa.no_rawat=?");
            try{
                ps.setString(1,noRawatCetak);
                rs=ps.executeQuery();
                if(rs.next()){
                    param.put("pNoRM", rs.getString("no_rkm_medis"));
                    param.put("pNamaPasien", rs.getString("nm_pasien"));
                    param.put("pTglLahir", rs.getString("tgl_lahir"));
                    param.put("pJk", "L".equals(rs.getString("jk"))?"L":"P");
                    param.put("pRuangan", rs.getString("nm_poli")==null?"":rs.getString("nm_poli"));
                }
            } finally {
                if(rs!=null){ rs.close(); }
                if(ps!=null){ ps.close(); }
            }

            ps=koneksi.prepareStatement(
                "select tinggal_bersama_anak,tinggal_bersama_orang_tua,tinggal_bersama_sendiri,tinggal_bersama_suami_istri,"+
                "hambatan_ada,hambatan_gangguan_pendengaran,hambatan_gangguan_penglihatan,hambatan_gangguan_bicara,hambatan_gangguan_emosi,"+
                "hambatan_motivasi_kurang,hambatan_memori_hilang,hambatan_fisik_lemah,hambatan_alkoholik,hambatan_perokok,hambatan_fisiologis_mampu_belajar,"+
                "kepada_pasien,kepada_keluarga,keterangan_kepada_keluarga,"+
                "kemampuan_bahasa,keterangan_kemampuan_bahasa,perlu_penerjemah,baca_tulis,kepercayaan_lainnya,kesediaan_menerima_informasi,cara_edukasi,"+
                "kebutuhan_hak_partisipasi,kebutuhan_informed_consent,kebutuhan_alat_medis_aman,kebutuhan_cuci_tangan,"+
                "kebutuhan_rujukan_edukasi,kebutuhan_diet_nutrisi,kebutuhan_manajemen_nyeri,kebutuhan_bahaya_merokok,"+
                "kebutuhan_kondisi_kesehatan_diagnosis,kebutuhan_penggunaan_obat,kebutuhan_teknik_rehabilitasi,kebutuhan_lain_lain,"+
                "keterangan_kebutuhan_lain_lain from edukasi_pasien_keluarga_rj where no_rawat=?");
            try{
                ps.setString(1,noRawatCetak);
                rs=ps.executeQuery();
                if(rs.next()){
                    param.put("aTinggalAnak", ambil(rs,"tinggal_bersama_anak"));
                    param.put("aTinggalOrangTua", ambil(rs,"tinggal_bersama_orang_tua"));
                    param.put("aTinggalSendiri", ambil(rs,"tinggal_bersama_sendiri"));
                    param.put("aTinggalSuamiIstri", ambil(rs,"tinggal_bersama_suami_istri"));
                    param.put("aHambatanAda", ambil(rs,"hambatan_ada"));
                    param.put("aHambatanPendengaran", ambil(rs,"hambatan_gangguan_pendengaran"));
                    param.put("aHambatanEmosi", ambil(rs,"hambatan_gangguan_emosi"));
                    param.put("aHambatanPenglihatan", ambil(rs,"hambatan_gangguan_penglihatan"));
                    param.put("aHambatanBicara", ambil(rs,"hambatan_gangguan_bicara"));
                    param.put("aHambatanMotivasi", ambil(rs,"hambatan_motivasi_kurang"));
                    param.put("aHambatanMemori", ambil(rs,"hambatan_memori_hilang"));
                    param.put("aHambatanFisikLemah", ambil(rs,"hambatan_fisik_lemah"));
                    param.put("aHambatanAlkoholik", ambil(rs,"hambatan_alkoholik"));
                    param.put("aHambatanPerokok", ambil(rs,"hambatan_perokok"));
                    param.put("aHambatanFisiologis", ambil(rs,"hambatan_fisiologis_mampu_belajar"));
                    param.put("aKepadaPasien", ambil(rs,"kepada_pasien"));
                    param.put("aKepadaKeluarga", ambil(rs,"kepada_keluarga"));
                    param.put("aKepadaKeluargaKet", ambil(rs,"keterangan_kepada_keluarga"));
                    param.put("aBahasaIndonesia", ambil(rs,"kemampuan_bahasa"));
                    param.put("aBahasaAsing", ambil(rs,"kemampuan_bahasa"));
                    param.put("aBahasaAsingKet", ambil(rs,"keterangan_kemampuan_bahasa"));
                    param.put("aPerluPenerjemah", ambil(rs,"perlu_penerjemah"));
                    param.put("aBacaBisa", ambil(rs,"baca_tulis"));
                    param.put("aBacaTidakBisa", ambil(rs,"baca_tulis"));
                    param.put("aKepercayaanAda", ambil(rs,"kepercayaan_lainnya"));
                    param.put("aKepercayaanTidakAda", ambil(rs,"kepercayaan_lainnya"));
                    param.put("aKesediaanEdukasi", ambil(rs,"kesediaan_menerima_informasi"));
                    param.put("aCaraLisan", ambil(rs,"cara_edukasi"));
                    param.put("aCaraTulisan", ambil(rs,"cara_edukasi"));
                    param.put("aCaraProsedur", ambil(rs,"cara_edukasi"));
                    param.put("aKebHakPartisipasi", ambil(rs,"kebutuhan_hak_partisipasi"));
                    param.put("aKebInformedConsent", ambil(rs,"kebutuhan_informed_consent"));
                    param.put("aKebAlatMedisAman", ambil(rs,"kebutuhan_alat_medis_aman"));
                    param.put("aKebCuciTangan", ambil(rs,"kebutuhan_cuci_tangan"));
                    param.put("aKebRujukanEdukasi", ambil(rs,"kebutuhan_rujukan_edukasi"));
                    param.put("aKebDietNutrisi", ambil(rs,"kebutuhan_diet_nutrisi"));
                    param.put("aKebManajemenNyeri", ambil(rs,"kebutuhan_manajemen_nyeri"));
                    param.put("aKebBahayaMerokok", ambil(rs,"kebutuhan_bahaya_merokok"));
                    param.put("aKebKondisiKesehatan", ambil(rs,"kebutuhan_kondisi_kesehatan_diagnosis"));
                    param.put("aKebPenggunaanObat", ambil(rs,"kebutuhan_penggunaan_obat"));
                    param.put("aKebTeknikRehabilitasi", ambil(rs,"kebutuhan_teknik_rehabilitasi"));
                    param.put("aKebLainLain", ambil(rs,"kebutuhan_lain_lain"));
                    param.put("aKebLainLainKet", ambil(rs,"keterangan_kebutuhan_lain_lain"));
                }
            } finally {
                if(rs!=null){ rs.close(); }
                if(ps!=null){ ps.close(); }
            }

            String qryCetak =
                "select date_format(pelaksanaan_informasi_edukasi.tanggal,'%d-%m-%Y %H:%i') as tgl_jam_edukasi,"+
                "pelaksanaan_informasi_edukasi.materi_edukasi,pelaksanaan_informasi_edukasi.kode_leaflet,pelaksanaan_informasi_edukasi.lama_edukasi,"+
                "pelaksanaan_informasi_edukasi.hasil_verifikasi,date_format(pelaksanaan_informasi_edukasi.tanggal_re_edukasi,'%d-%m-%Y') as tanggal_re_edukasi,"+
                "pegawai.nama as nama_petugas,pelaksanaan_informasi_edukasi.diberikan_pada,pelaksanaan_informasi_edukasi.keterangan_diberikan_pada "+
                "from pelaksanaan_informasi_edukasi left join pegawai on pelaksanaan_informasi_edukasi.nik=pegawai.nik "+
                "where pelaksanaan_informasi_edukasi.no_rawat='"+noRawatCetak+"' order by pelaksanaan_informasi_edukasi.tanggal";

            if("PDF".equals(mode)){
                Valid.MyReportqrypdf("rptFormulirEdukasiRM021.jasper","report","::[ Formulir Edukasi Pasien & Keluarga RM 02-1 ]::",qryCetak,param);
            }else{
                Valid.MyReportqry("rptFormulirEdukasiRM021.jasper","report","::[ Formulir Edukasi Pasien & Keluarga RM 02-1 ]::",qryCetak,param);
            }
        }catch(Exception e){
            System.out.println("Notif cetakFormulirRM021 : "+e);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

    /** ResultSet.getString() aman thd null - kembalikan "" bukan null, spy
     * parameter Jasper (String) tidak pernah null. */
    private String ambil(ResultSet rs, String kolom){
        try{
            String v = rs.getString(kolom);
            return v==null?"":v;
        }catch(Exception e){
            return "";
        }
    }

    /** class='isi' -> border grid tipis (border-right+border-bottom #e2e7dd),
     * konsep sama persis dgn ringkasan LoadHTML di RMTriaseIGD.java/
     * RMPenilaianAwalKeperawatanIGD.java (CSS .isi sudah ada dari
     * styleSheet.addRule() bawaan konstruktor, cuma belum dipakai). */
    private String judulBarisPelaksanaan(String judul){
        return "<tr class='isi'><td colspan='2' bgcolor='#FFFAF8' align='center'><b>"+judul+"</b></td></tr>";
    }

    private String barisPelaksanaan(String label, String value){
        return "<tr class='isi'><td width='40%' valign='top'>"+label+"</td><td width='60%' valign='top'>"+((value==null||value.trim().isEmpty())?"-":value)+"</td></tr>";
    }

    /** widget.Tanggal (JDateTimePicker pihak ketiga) tidak py getText() -
     * ambil java.util.Date via getDate() lalu format manual. */
    private String formatTgl(widget.Tanggal t){
        if(t==null || t.getDate()==null) return "";
        return new java.text.SimpleDateFormat("dd-MM-yyyy").format(t.getDate());
    }

    /** Ringkasan HTML data pelaksanaan edukasi yg sedang dipilih di tabel,
     * dibaca LANGSUNG dari widget yg sudah terisi getData() (bukan query
     * ulang) - pola sama persis bangunRingkasanHtml() di
     * RMTransferPasienAntarRuang.java. Dipanggil di baris terakhir getData(). */
    private void bangunRingkasanHtml(){
        StringBuilder h = new StringBuilder();
        h.append("<html><body><table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>");

        h.append(judulBarisPelaksanaan("PASIEN"));
        h.append(barisPelaksanaan("No.Rawat", TNoRw.getText()));
        h.append(barisPelaksanaan("No.RM / Nama", TNoRM.getText()+" / "+TPasien.getText()));
        h.append(barisPelaksanaan("Tgl.Lahir / Umur / J.K.", TglLahir.getText()+" / "+Umur.getText()+" / "+JK.getText()));

        h.append(judulBarisPelaksanaan("WAKTU & PETUGAS"));
        h.append(barisPelaksanaan("Tanggal / Jam", formatTgl(Tanggal)+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem()));
        h.append(barisPelaksanaan("Petugas", NIP.getText()+" - "+NamaPetugas.getText()));

        h.append(judulBarisPelaksanaan("EDUKASI"));
        h.append(barisPelaksanaan("Materi", Materi.getText()));
        h.append(barisPelaksanaan("Keterangan", Keterangan.getText()));
        h.append(barisPelaksanaan("Lama Edukasi", Lama.getText().trim().isEmpty()?"-":Lama.getText()+" menit"));
        h.append(barisPelaksanaan("Status", Status.getSelectedItem()+""));

        h.append(judulBarisPelaksanaan("PENERIMA, METODE & HASIL"));
        h.append(barisPelaksanaan("Edukasi Kepada", Kepada.getSelectedItem()+(KeteranganKepada.getText().trim().isEmpty()?"":" - "+KeteranganKepada.getText())));
        h.append(barisPelaksanaan("Metode", Metode.getSelectedItem()+""));
        h.append(barisPelaksanaan("Hasil Edukasi", Hasil.getSelectedItem()+""));

        h.append(judulBarisPelaksanaan("LEAFLET & RE-EDUKASI"));
        h.append(barisPelaksanaan("Kode Leaflet", KodeLeaflet.getText()));
        h.append(barisPelaksanaan("Tgl Re-Edukasi", formatTgl(TglReEdukasi)));

        h.append("</table></body></html>");
        LoadHTML.setText(h.toString());
        LoadHTML.setCaretPosition(0);
    }

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            if(TCari.getText().toString().equals("")){
                ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,pasien.jk,pasien.tgl_lahir,pelaksanaan_informasi_edukasi.tanggal,"+
                    "pelaksanaan_informasi_edukasi.nik,pegawai.nama,pelaksanaan_informasi_edukasi.materi_edukasi,pelaksanaan_informasi_edukasi.keterangan,pelaksanaan_informasi_edukasi.diberikan_pada,"+
                    "pelaksanaan_informasi_edukasi.keterangan_diberikan_pada,pelaksanaan_informasi_edukasi.lama_edukasi,pelaksanaan_informasi_edukasi.metode_edukasi,pelaksanaan_informasi_edukasi.hasil_verifikasi,"+
                    "pelaksanaan_informasi_edukasi.status,pelaksanaan_informasi_edukasi.kode_leaflet,pelaksanaan_informasi_edukasi.tanggal_re_edukasi "+
                    "from pelaksanaan_informasi_edukasi inner join reg_periksa on pelaksanaan_informasi_edukasi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join pegawai on pelaksanaan_informasi_edukasi.nik=pegawai.nik where "+
                    "pelaksanaan_informasi_edukasi.tanggal between ? and ? order by pelaksanaan_informasi_edukasi.tanggal");
            }else{
                ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,pasien.jk,pasien.tgl_lahir,pelaksanaan_informasi_edukasi.tanggal,"+
                    "pelaksanaan_informasi_edukasi.nik,pegawai.nama,pelaksanaan_informasi_edukasi.materi_edukasi,pelaksanaan_informasi_edukasi.keterangan,pelaksanaan_informasi_edukasi.diberikan_pada,"+
                    "pelaksanaan_informasi_edukasi.keterangan_diberikan_pada,pelaksanaan_informasi_edukasi.lama_edukasi,pelaksanaan_informasi_edukasi.metode_edukasi,pelaksanaan_informasi_edukasi.hasil_verifikasi,"+
                    "pelaksanaan_informasi_edukasi.status,pelaksanaan_informasi_edukasi.kode_leaflet,pelaksanaan_informasi_edukasi.tanggal_re_edukasi "+
                    "from pelaksanaan_informasi_edukasi inner join reg_periksa on pelaksanaan_informasi_edukasi.no_rawat=reg_periksa.no_rawat "+
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join pegawai on pelaksanaan_informasi_edukasi.nik=pegawai.nik where "+
                    "pelaksanaan_informasi_edukasi.tanggal between ? and ? and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or pelaksanaan_informasi_edukasi.nik like ? or pegawai.nama like ?) "+
                    "order by pelaksanaan_informasi_edukasi.tanggal ");
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
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                        rs.getString("umurdaftar")+" "+rs.getString("sttsumur"),rs.getString("jk"),rs.getDate("tgl_lahir"),
                        rs.getString("tanggal"),rs.getString("nik"),rs.getString("nama"),rs.getString("materi_edukasi"),
                        rs.getString("keterangan"),rs.getString("lama_edukasi"),rs.getString("status"),rs.getString("diberikan_pada"),
                        rs.getString("keterangan_diberikan_pada"),rs.getString("metode_edukasi"),rs.getString("hasil_verifikasi"),
                        rs.getString("kode_leaflet"),rs.getString("tanggal_re_edukasi")
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
        Materi.setText("");
        Keterangan.setText("");
        Lama.setText("");
        Status.setSelectedIndex(0);
        Kepada.setSelectedIndex(0);
        KeteranganKepada.setText("");
        Metode.setSelectedIndex(0);
        Hasil.setSelectedIndex(0);
        // Kode Leaflet di-generate ulang (bukan dikosongkan) - identitas
        // pasien (TNoRw) TIDAK ikut direset oleh emptTeks(), jadi "Baru"
        // dipakai utk menambah SESI BARU pasien yg sama (bukan cari ulang
        // pasien) - lihat cetakFormulirRM021()/generateKodeLeaflet().
        KodeLeaflet.setText(TNoRw.getText().trim().isEmpty()?"":generateKodeLeaflet(TNoRw.getText()));
        TglReEdukasi.setDate(new Date());
        Tanggal.setDate(new Date());
        ChkKejadian.setSelected(true);
        Materi.requestFocus();
    }

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            Umur.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
            JK.setText(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            Materi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            Keterangan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            Lama.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            Status.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            Kepada.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            KeteranganKepada.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            Metode.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            Hasil.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
            KodeLeaflet.setText(tbObat.getValueAt(tbObat.getSelectedRow(),17)==null?"":tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());
            String tglReEdukasi = tbObat.getValueAt(tbObat.getSelectedRow(),18)==null?"":tbObat.getValueAt(tbObat.getSelectedRow(),18).toString();
            if(!tglReEdukasi.trim().equals("")){
                Valid.SetTgl(TglReEdukasi,tglReEdukasi);
            }else{
                TglReEdukasi.setDate(new Date());
            }
            ChkKejadian.setSelected(false);
            Jam.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString().substring(11,13));
            Menit.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString().substring(14,16));
            Detik.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString().substring(17,19));
            Valid.SetTgl(Tanggal,tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
            bangunRingkasanHtml();
        }
    }
    
    private void isRawat() {
        try {
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi,reg_periksa.umurdaftar,"+
                    "reg_periksa.sttsumur,reg_periksa.jam_reg from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?");
            try {
                ps.setString(1,TNoRw.getText());
                rs=ps.executeQuery();
                if(rs.next()){
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    JK.setText(rs.getString("jk"));
                    Umur.setText(rs.getString("umurdaftar")+" "+rs.getString("sttsumur"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    TanggalRegistrasi.setText(rs.getString("tgl_registrasi")+" "+rs.getString("jam_reg"));
                    KodeLeaflet.setText(generateKodeLeaflet(TNoRw.getText()));
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
    }
    
    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        isRawat();
        ChkInput.setSelected(true);
        isForm();
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            // 204 -> 230: baris "Kode Leaflet"/"Tgl Re-Edukasi" (initRM02_1BagianB(),
            // y=180-203) kepotong kalau tetap 204 - PanelInput.setPreferredSize()
            // ini (bukan FormInput punya) yg sebenarnya membatasi tinggi terlihat,
            // krn FormInput adalah child BorderLayout.CENTER (preferred size sendiri
            // diabaikan begitu parent py preferred size tetap).
            PanelInput.setPreferredSize(new Dimension(WIDTH,230));
            FormInput.setVisible(true);
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
    }
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getpelaksanaan_informasi_edukasi());
        BtnHapus.setEnabled(akses.getpelaksanaan_informasi_edukasi());
        BtnEdit.setEnabled(akses.getpelaksanaan_informasi_edukasi());
        BtnPrint.setEnabled(akses.getpelaksanaan_informasi_edukasi()); 
        if(akses.getjml2()>=1){
            NIP.setEditable(false);
            btnPetugas.setEnabled(false);
            NIP.setText(akses.getkode());
            NamaPetugas.setText(Sequel.CariPegawai(NIP.getText()));
        }   
        
        if(TANGGALMUNDUR.equals("no")){
            if(!akses.getkode().equals("Admin Utama")){
                Tanggal.setEditable(false);
                Tanggal.setEnabled(false);
                ChkKejadian.setEnabled(false);
                Jam.setEnabled(false);
                Menit.setEnabled(false);
                Detik.setEnabled(false);
            }
        }
    }

    private void jam(){
        ActionListener taskPerformer = new ActionListener(){
            private int nilai_jam;
            private int nilai_menit;
            private int nilai_detik;
            public void actionPerformed(ActionEvent e) {
                String nol_jam = "";
                String nol_menit = "";
                String nol_detik = "";
                
                Date now = Calendar.getInstance().getTime();

                // Mengambil nilaj JAM, MENIT, dan DETIK Sekarang
                if(ChkKejadian.isSelected()==true){
                    nilai_jam = now.getHours();
                    nilai_menit = now.getMinutes();
                    nilai_detik = now.getSeconds();
                }else if(ChkKejadian.isSelected()==false){
                    nilai_jam =Jam.getSelectedIndex();
                    nilai_menit =Menit.getSelectedIndex();
                    nilai_detik =Detik.getSelectedIndex();
                }

                // Jika nilai JAM lebih kecil dari 10 (hanya 1 digit)
                if (nilai_jam <= 9) {
                    // Tambahkan "0" didepannya
                    nol_jam = "0";
                }
                // Jika nilai MENIT lebih kecil dari 10 (hanya 1 digit)
                if (nilai_menit <= 9) {
                    // Tambahkan "0" didepannya
                    nol_menit = "0";
                }
                // Jika nilai DETIK lebih kecil dari 10 (hanya 1 digit)
                if (nilai_detik <= 9) {
                    // Tambahkan "0" didepannya
                    nol_detik = "0";
                }
                // Membuat String JAM, MENIT, DETIK
                String jam = nol_jam + Integer.toString(nilai_jam);
                String menit = nol_menit + Integer.toString(nilai_menit);
                String detik = nol_detik + Integer.toString(nilai_detik);
                // Menampilkan pada Layar
                //tampil_jam.setText("  " + jam + " : " + menit + " : " + detik + "  ");
                Jam.setSelectedItem(jam);
                Menit.setSelectedItem(menit);
                Detik.setSelectedItem(detik);
            }
        };
        // Timer
        new Timer(1000, taskPerformer).start();
    }

    private void ganti() {
        if(Sequel.mengedittf("pelaksanaan_informasi_edukasi","tanggal=? and no_rawat=?","no_rawat=?,tanggal=?,nik=?,materi_edukasi=?,keterangan=?,diberikan_pada=?,keterangan_diberikan_pada=?,lama_edukasi=?,metode_edukasi=?,hasil_verifikasi=?,status=?,kode_leaflet=?,tanggal_re_edukasi=?",15,new String[]{
            TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem(),NIP.getText(),Materi.getText(),Keterangan.getText(),Kepada.getSelectedItem().toString(),KeteranganKepada.getText(),
            Lama.getText(),Metode.getSelectedItem().toString(),Hasil.getSelectedItem().toString(),Status.getSelectedItem().toString(),
            KodeLeaflet.getText(),(TglReEdukasi.getSelectedItem()==null||TglReEdukasi.getSelectedItem().toString().trim().equals(""))?null:Valid.SetTgl(TglReEdukasi.getSelectedItem()+""),
            tbObat.getValueAt(tbObat.getSelectedRow(),6).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })==true){
            tbObat.setValueAt(TNoRw.getText(),tbObat.getSelectedRow(),0);
            tbObat.setValueAt(TNoRM.getText(),tbObat.getSelectedRow(),1);
            tbObat.setValueAt(TPasien.getText(),tbObat.getSelectedRow(),2);
            tbObat.setValueAt(Umur.getText(),tbObat.getSelectedRow(),3);
            tbObat.setValueAt(JK.getText(),tbObat.getSelectedRow(),4);
            tbObat.setValueAt(TglLahir.getText(),tbObat.getSelectedRow(),5);
            tbObat.setValueAt(Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem(),tbObat.getSelectedRow(),6);
            tbObat.setValueAt(NIP.getText(),tbObat.getSelectedRow(),7);
            tbObat.setValueAt(NamaPetugas.getText(),tbObat.getSelectedRow(),8);
            tbObat.setValueAt(Materi.getText(),tbObat.getSelectedRow(),9);
            tbObat.setValueAt(Keterangan.getText(),tbObat.getSelectedRow(),10);
            tbObat.setValueAt(Lama.getText(),tbObat.getSelectedRow(),11);
            tbObat.setValueAt(Status.getSelectedItem().toString(),tbObat.getSelectedRow(),12);
            tbObat.setValueAt(Kepada.getSelectedItem().toString(),tbObat.getSelectedRow(),13);
            tbObat.setValueAt(KeteranganKepada.getText(),tbObat.getSelectedRow(),14);
            tbObat.setValueAt(Metode.getSelectedItem().toString(),tbObat.getSelectedRow(),15);
            tbObat.setValueAt(Hasil.getSelectedItem().toString(),tbObat.getSelectedRow(),16);
            tbObat.setValueAt(KodeLeaflet.getText(),tbObat.getSelectedRow(),17);
            tbObat.setValueAt(TglReEdukasi.getSelectedItem()==null?"":TglReEdukasi.getSelectedItem().toString(),tbObat.getSelectedRow(),18);
            emptTeks();
        }
    }

    private void hapus() {
        if(Sequel.queryu2tf("delete from pelaksanaan_informasi_edukasi where tanggal=? and no_rawat=?",2,new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(),6).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })==true){
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText(""+tabMode.getRowCount());
            emptTeks();
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
        }
    }

    private void simpan() {
        if(Sequel.menyimpantf("pelaksanaan_informasi_edukasi","?,?,?,?,?,?,?,?,?,?,?,?,?","Tanggal & Jam Pelaksanaan (klik dulu tombol Baru utk menambah sesi baru)",13,new String[]{
                TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem(),
                NIP.getText(),Materi.getText(),Keterangan.getText(),Kepada.getSelectedItem().toString(),KeteranganKepada.getText(),Lama.getText(),
                Metode.getSelectedItem().toString(),Hasil.getSelectedItem().toString(),Status.getSelectedItem().toString(),
                KodeLeaflet.getText(),(TglReEdukasi.getSelectedItem()==null||TglReEdukasi.getSelectedItem().toString().trim().equals(""))?null:Valid.SetTgl(TglReEdukasi.getSelectedItem()+"")
        })==true){
            tabMode.addRow(new Object[]{
                TNoRw.getText(),TNoRM.getText(),TPasien.getText(),Umur.getText(),JK.getText(),TglLahir.getText(),
                Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem(),
                NIP.getText(),NamaPetugas.getText(),Materi.getText(),Keterangan.getText(),Lama.getText(),Status.getSelectedItem().toString(),
                Kepada.getSelectedItem().toString(),KeteranganKepada.getText(),Metode.getSelectedItem().toString(),Hasil.getSelectedItem().toString(),
                KodeLeaflet.getText(),TglReEdukasi.getSelectedItem()==null?"":TglReEdukasi.getSelectedItem().toString()
            });
            LCount.setText(""+tabMode.getRowCount());
            emptTeks();
        } 
    }
    
    private void isPhoto(){
        if(ChkAccor.isSelected()==true){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(480,HEIGHT));
            FormPhoto.setVisible(true);  
            ChkAccor.setVisible(true);
        }else if(ChkAccor.isSelected()==false){    
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(15,HEIGHT));
            FormPhoto.setVisible(false);  
            ChkAccor.setVisible(true);
        }
    }

    /** Ambil label+nama+foto/ttd 1 peran dari tabel bukti-nya sendiri - pola
     * identik fotoImgTag() di RMTransferPasienAntarRuang.java. "Kosong" jadi
     * default kalau belum ada rekam bukti/photo null-kosong-strip. */
    private String fotoImgTag(String tabelBukti, String label, String nama, String noRawat, String tanggal){
        String namaTampil = (nama==null||nama.trim().isEmpty())?"-":nama;
        String html = "<b>"+label+"</b><br><font face='tahoma' size='2'>"+namaTampil+"</font><br><font face='tahoma' size='2' color='#434343'>Kosong</font>";
        try {
            ps=koneksi.prepareStatement("select photo from "+tabelBukti+" where no_rawat=? and tanggal=?");
            try{
                ps.setString(1,noRawat);
                ps.setString(2,tanggal);
                rs=ps.executeQuery();
                if(rs.next() && rs.getString("photo")!=null && !rs.getString("photo").equals("") && !rs.getString("photo").equals("-")){
                    html = "<b>"+label+"</b><br><font face='tahoma' size='2'>"+namaTampil+"</font><br><img src='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/pelaksanaanedukasi/"+rs.getString("photo")+"' alt='photo' width='220' height='220'/>";
                }
            } finally {
                if(rs!=null){ rs.close(); }
                if(ps!=null){ ps.close(); }
            }
        } catch (Exception e) {
            System.out.println("Notif fotoImgTag : "+e);
        }
        return html;
    }

    /** 2 peran (Pasien/Keluarga + Pemberi Edukasi), masing2 dari tabel bukti
     * sendiri-sendiri (bukti_pelaksanaan_informasi_edukasi / baru
     * bukti_pelaksanaan_edukasi_pemberi - lihat
     * sik_bukti_pelaksanaan_edukasi_pemberi.sql) - pola identik grid 2x2
     * RMTransferPasienAntarRuang.panggilPhoto(), disini cuma 2 kolom krn 2
     * peran. Nama diambil LANGSUNG dari widget yg sudah terisi getData()
     * (KeteranganKepada/NamaPetugas), bukan query ulang. */
    private void panggilPhoto() {
        if(FormPhoto.isVisible()==true){
            String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();
            String tanggal = tbObat.getValueAt(tbObat.getSelectedRow(),6).toString();
            String htmlPasien = fotoImgTag("bukti_pelaksanaan_informasi_edukasi","Pasien/Keluarga",KeteranganKepada.getText(),noRawat,tanggal);
            String htmlPemberi = fotoImgTag("bukti_pelaksanaan_edukasi_pemberi","Pemberi Edukasi",NamaPetugas.getText(),noRawat,tanggal);
            LoadHTML2.setText("<html><body><table width='100%'><tr>"+
                "<td align='center' valign='top' width='50%'>"+htmlPasien+"</td>"+
                "<td align='center' valign='top' width='50%'>"+htmlPemberi+"</td>"+
                "</tr></table></body></html>");
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
