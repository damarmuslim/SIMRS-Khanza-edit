/*
 * Kontribusi dari M. Syukur RS. Jiwa Prov Sultra
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
import java.io.FileInputStream;
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
import kepegawaian.DlgCariPetugas;


/**
 *
 * @author perpustakaan
 */
public final class RMTransferPasienAntarRuang extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;
    private DlgCariPetugas petugas;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private StringBuilder htmlContent;
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public RMTransferPasienAntarRuang(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        // [RM 04-1/04-2] tabMode diringkas dari 38 kolom jadi 12 kolom
        // pencarian saja (pola sama seperti modernisasi RM 03-2/03-4-05-06) -
        // ~130 kolom aktif form baru tidak mungkin ditampung JTable, jadi
        // getData() sekarang query-ulang-DB per klik/Space, bukan baca
        // langsung dari tbObat. Index 0 (No.Rawat) & 5 (Tanggal Masuk) SENGAJA
        // dipertahankan di posisi yang sama seperti sebelumnya supaya
        // ganti()/simpan()/hapus()/panggilPhoto()/btnAmbilActionPerformed yang
        // masih membaca tbObat.getValueAt(row,0)/(row,5) tidak perlu diubah.
        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat","No.RM","Nama Pasien","Tgl.Lahir","J.K.","Tanggal Masuk","Tanggal Pindah","Diagnosa Utama",
            "NIP Menyerahkan","Petugas Yang Menyerahkan","NIP Menerima","Petugas Yang Menerima"
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };

        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 12; i++) {
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
                column.setPreferredWidth(115);
            }else if(i==6){
                column.setPreferredWidth(115);
            }else if(i==7){
                column.setPreferredWidth(160);
            }else if(i==8){
                column.setPreferredWidth(95);
            }else if(i==9){
                column.setPreferredWidth(150);
            }else if(i==10){
                column.setPreferredWidth(95);
            }else if(i==11){
                column.setPreferredWidth(150);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        AsalRuang.setDocument(new batasInput((byte)30).getKata(AsalRuang));
        RuangSelanjutnya.setDocument(new batasInput((byte)30).getKata(RuangSelanjutnya));
        // Batas dinaikkan 50->100 (mentok kapasitas kolom DB varchar(100)) -
        // nilai sekarang diisi via picker ICD "kode - nama diagnosa" (lihat
        // BtnDiagnosaUtama di initTransferTambahan()), bisa lebih panjang
        // drpd input manual singkat sebelumnya.
        DiagnosaUtama.setDocument(new batasInput((int)100).getKata(DiagnosaUtama));
        DiagnosaSekunder.setDocument(new batasInput((int)100).getKata(DiagnosaSekunder));
        KeteranganIndikasiPindahRuang.setDocument(new batasInput((int)50).getKata(KeteranganIndikasiPindahRuang));
        ProsedurDilakukan.setDocument(new batasInput((int)800).getKata(ProsedurDilakukan));
        ObatYangDiberikan.setDocument(new batasInput((int)800).getKata(ObatYangDiberikan));
        KeteranganPeralatan.setDocument(new batasInput((int)50).getKata(KeteranganPeralatan));
        PemeriksaanPenunjang.setDocument(new batasInput((int)500).getKata(PemeriksaanPenunjang));
        NamaMenyetujui.setDocument(new batasInput((int)50).getKata(NamaMenyetujui));
        KeluhanUtamaSebelumTransfer.setDocument(new batasInput((int)200).getKata(KeluhanUtamaSebelumTransfer));
        TDSebelumTransfer.setDocument(new batasInput((int)7).getKata(TDSebelumTransfer));
        NadiSebelumTransfer.setDocument(new batasInput((int)5).getKata(NadiSebelumTransfer));
        RRSebelumTransfer.setDocument(new batasInput((int)5).getKata(RRSebelumTransfer));
        SuhuSebelumTransfer.setDocument(new batasInput((int)5).getKata(SuhuSebelumTransfer));
        KeluhanUtamaSetelahTransfer.setDocument(new batasInput((int)200).getKata(KeluhanUtamaSetelahTransfer));
        TDSetelahTransfer.setDocument(new batasInput((int)7).getKata(TDSetelahTransfer));
        NadiSetelahTransfer.setDocument(new batasInput((int)5).getKata(NadiSetelahTransfer));
        RRSetelahTransfer.setDocument(new batasInput((int)5).getKata(RRSetelahTransfer));
        SuhuSetelahTransfer.setDocument(new batasInput((int)5).getKata(SuhuSetelahTransfer));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
        
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
        LoadHTML.setDocument(kit.createDefaultDocument());
        LoadHTML2.setDocument(kit.createDefaultDocument());
        
        ChkAccor.setSelected(false);
        isPhoto();

        pensiunkanFieldTransferLama();
        initTransferTambahan();
        initRingkasanTransfer();
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
        jSeparator14 = new javax.swing.JSeparator();
        MenyetujuiPemindahan = new widget.ComboBox();
        jLabel57 = new widget.Label();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        Jk = new widget.TextBox();
        jLabel10 = new widget.Label();
        label11 = new widget.Label();
        jLabel11 = new widget.Label();
        TanggalMasuk = new widget.Tanggal();
        label12 = new widget.Label();
        TanggalPindah = new widget.Tanggal();
        KeteranganIndikasiPindahRuang = new widget.TextBox();
        jSeparator1 = new javax.swing.JSeparator();
        KeteranganPeralatan = new widget.TextBox();
        jLabel13 = new widget.Label();
        RuangSelanjutnya = new widget.TextBox();
        jLabel14 = new widget.Label();
        jLabel15 = new widget.Label();
        PeralatanMenyertai = new widget.ComboBox();
        jLabel16 = new widget.Label();
        DiagnosaUtama = new widget.TextBox();
        jLabel18 = new widget.Label();
        jLabel20 = new widget.Label();
        jLabel30 = new widget.Label();
        scrollPane1 = new widget.ScrollPane();
        DiagnosaSekunder = new widget.TextArea();
        jLabel31 = new widget.Label();
        scrollPane2 = new widget.ScrollPane();
        ProsedurDilakukan = new widget.TextArea();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel32 = new widget.Label();
        jLabel33 = new widget.Label();
        scrollPane3 = new widget.ScrollPane();
        ObatYangDiberikan = new widget.TextArea();
        scrollPane4 = new widget.ScrollPane();
        PemeriksaanPenunjang = new widget.TextArea();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel34 = new widget.Label();
        IndikasiPindah = new widget.ComboBox();
        AsalRuang = new widget.TextBox();
        jLabel35 = new widget.Label();
        MetodePemindahan = new widget.ComboBox();
        jLabel36 = new widget.Label();
        jLabel37 = new widget.Label();
        NamaMenyetujui = new widget.TextBox();
        jLabel38 = new widget.Label();
        HubunganMenyetujui = new widget.ComboBox();
        jLabel39 = new widget.Label();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel40 = new widget.Label();
        jLabel41 = new widget.Label();
        scrollPane5 = new widget.ScrollPane();
        KeluhanUtamaSebelumTransfer = new widget.TextArea();
        jLabel42 = new widget.Label();
        KeadaanUmumSebelumTransfer = new widget.ComboBox();
        TDSebelumTransfer = new widget.TextBox();
        jLabel23 = new widget.Label();
        jLabel17 = new widget.Label();
        jLabel22 = new widget.Label();
        NadiSebelumTransfer = new widget.TextBox();
        jLabel26 = new widget.Label();
        RRSebelumTransfer = new widget.TextBox();
        jLabel25 = new widget.Label();
        jLabel24 = new widget.Label();
        SuhuSebelumTransfer = new widget.TextBox();
        jLabel27 = new widget.Label();
        jLabel28 = new widget.Label();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel43 = new widget.Label();
        jLabel44 = new widget.Label();
        KeadaanUmumSetelahTransfer = new widget.ComboBox();
        jLabel29 = new widget.Label();
        TDSetelahTransfer = new widget.TextBox();
        jLabel45 = new widget.Label();
        jLabel46 = new widget.Label();
        scrollPane6 = new widget.ScrollPane();
        KeluhanUtamaSetelahTransfer = new widget.TextArea();
        jLabel47 = new widget.Label();
        NadiSetelahTransfer = new widget.TextBox();
        jLabel48 = new widget.Label();
        jLabel49 = new widget.Label();
        RRSetelahTransfer = new widget.TextBox();
        jLabel50 = new widget.Label();
        jLabel51 = new widget.Label();
        SuhuSetelahTransfer = new widget.TextBox();
        jLabel52 = new widget.Label();
        jSeparator7 = new javax.swing.JSeparator();
        label14 = new widget.Label();
        KdPetugasMenyerahkan = new widget.TextBox();
        NmPetugasMenyerahkan = new widget.TextBox();
        BtnDokter = new widget.Button();
        label15 = new widget.Label();
        KdPetugasMenerima = new widget.TextBox();
        NmPetugasMenerima = new widget.TextBox();
        BtnMenerima = new widget.Button();
        label16 = new widget.Label();
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
        FormPhoto = new widget.PanelBiasa();
        FormPass3 = new widget.PanelBiasa();
        btnAmbil = new widget.Button();
        BtnRefreshPhoto1 = new widget.Button();
        Scroll5 = new widget.ScrollPane();
        LoadHTML2 = new widget.editorpane();

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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Transfer Pasien Antar Ruang ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
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
        FormInput.setPreferredSize(new java.awt.Dimension(870, 663));
        FormInput.setLayout(null);

        jSeparator14.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator14.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator14.setName("jSeparator14"); // NOI18N
        FormInput.add(jSeparator14);
        jSeparator14.setBounds(0, 861, 880, 0);

        MenyetujuiPemindahan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ya", "Tidak" }));
        MenyetujuiPemindahan.setName("MenyetujuiPemindahan"); // NOI18N
        MenyetujuiPemindahan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenyetujuiPemindahanKeyPressed(evt);
            }
        });
        FormInput.add(MenyetujuiPemindahan);
        MenyetujuiPemindahan.setBounds(775, 330, 80, 23);

        jLabel57.setText("Indikasi Pindah :");
        jLabel57.setName("jLabel57"); // NOI18N
        FormInput.add(jLabel57);
        jLabel57.setBounds(390, 40, 92, 23);

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

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(580, 10, 60, 23);

        TglLahir.setEditable(false);
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(644, 10, 80, 23);

        Jk.setEditable(false);
        Jk.setHighlighter(null);
        Jk.setName("Jk"); // NOI18N
        FormInput.add(Jk);
        Jk.setBounds(774, 10, 80, 23);

        jLabel10.setText("No.Rawat :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 10, 70, 23);

        label11.setText("Masuk :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label11);
        label11.setBounds(0, 40, 70, 23);

        jLabel11.setText("J.K. :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(740, 10, 30, 23);

        TanggalMasuk.setForeground(new java.awt.Color(50, 70, 50));
        TanggalMasuk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "07-02-2026 13:32:18" }));
        TanggalMasuk.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TanggalMasuk.setName("TanggalMasuk"); // NOI18N
        TanggalMasuk.setOpaque(false);
        TanggalMasuk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalMasukKeyPressed(evt);
            }
        });
        FormInput.add(TanggalMasuk);
        TanggalMasuk.setBounds(74, 40, 130, 23);

        label12.setText("Pindah :");
        label12.setName("label12"); // NOI18N
        label12.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label12);
        label12.setBounds(201, 40, 55, 23);

        TanggalPindah.setForeground(new java.awt.Color(50, 70, 50));
        TanggalPindah.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "07-02-2026 13:32:18" }));
        TanggalPindah.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TanggalPindah.setName("TanggalPindah"); // NOI18N
        TanggalPindah.setOpaque(false);
        TanggalPindah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalPindahKeyPressed(evt);
            }
        });
        FormInput.add(TanggalPindah);
        TanggalPindah.setBounds(260, 40, 130, 23);

        KeteranganIndikasiPindahRuang.setHighlighter(null);
        KeteranganIndikasiPindahRuang.setName("KeteranganIndikasiPindahRuang"); // NOI18N
        KeteranganIndikasiPindahRuang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganIndikasiPindahRuangKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganIndikasiPindahRuang);
        KeteranganIndikasiPindahRuang.setBounds(724, 40, 130, 23);

        jSeparator1.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator1.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(0, 70, 880, 1);

        KeteranganPeralatan.setHighlighter(null);
        KeteranganPeralatan.setName("KeteranganPeralatan"); // NOI18N
        KeteranganPeralatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganPeralatanKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganPeralatan);
        KeteranganPeralatan.setBounds(288, 330, 160, 23);

        jLabel13.setText(":");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(91, 120, 10, 23);

        RuangSelanjutnya.setHighlighter(null);
        RuangSelanjutnya.setName("RuangSelanjutnya"); // NOI18N
        RuangSelanjutnya.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RuangSelanjutnyaKeyPressed(evt);
            }
        });
        FormInput.add(RuangSelanjutnya);
        RuangSelanjutnya.setBounds(468, 80, 150, 23);

        jLabel14.setText("Metode Pemindahan :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(629, 80, 106, 23);

        jLabel15.setText("Ruang Rawat Selanjutnya :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(324, 80, 140, 23);

        PeralatanMenyertai.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Oksigen Portable", "Infus", "NGT", "Syringe Pump", "Suction", "Kateter Urin", "Tidak Ada" }));
        PeralatanMenyertai.setName("PeralatanMenyertai"); // NOI18N
        PeralatanMenyertai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PeralatanMenyertaiKeyPressed(evt);
            }
        });
        FormInput.add(PeralatanMenyertai);
        PeralatanMenyertai.setBounds(150, 330, 135, 23);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText("Diagnosa Utama");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(15, 120, 90, 23);

        DiagnosaUtama.setHighlighter(null);
        DiagnosaUtama.setName("DiagnosaUtama"); // NOI18N
        DiagnosaUtama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosaUtamaKeyPressed(evt);
            }
        });
        FormInput.add(DiagnosaUtama);
        DiagnosaUtama.setBounds(105, 120, 750, 23);

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel18.setText("Asal Ruang Rawat / Poliklinik");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(15, 80, 149, 23);

        jLabel20.setText(":");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(155, 80, 10, 23);

        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel30.setText("Diagnosa Sekunder :");
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(15, 150, 170, 23);

        scrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane1.setName("scrollPane1"); // NOI18N

        DiagnosaSekunder.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        DiagnosaSekunder.setColumns(20);
        DiagnosaSekunder.setRows(5);
        DiagnosaSekunder.setName("DiagnosaSekunder"); // NOI18N
        DiagnosaSekunder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosaSekunderKeyPressed(evt);
            }
        });
        scrollPane1.setViewportView(DiagnosaSekunder);

        FormInput.add(scrollPane1);
        scrollPane1.setBounds(15, 170, 410, 43);

        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel31.setText("Prosedur Yang Sudah Dilakukan :");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(445, 150, 170, 23);

        scrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane2.setName("scrollPane2"); // NOI18N

        ProsedurDilakukan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ProsedurDilakukan.setColumns(20);
        ProsedurDilakukan.setRows(5);
        ProsedurDilakukan.setName("ProsedurDilakukan"); // NOI18N
        ProsedurDilakukan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ProsedurDilakukanKeyPressed(evt);
            }
        });
        scrollPane2.setViewportView(ProsedurDilakukan);

        FormInput.add(scrollPane2);
        scrollPane2.setBounds(445, 170, 410, 43);

        jSeparator2.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator2.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(0, 110, 880, 1);

        jSeparator3.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator3.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator3.setName("jSeparator3"); // NOI18N
        FormInput.add(jSeparator3);
        jSeparator3.setBounds(0, 220, 880, 1);

        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel32.setText("Obat Yang Telah Diberikan :");
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(15, 220, 170, 23);

        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel33.setText("Pemeriksaan Penunjang Yang Sudah Dilakukan :");
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput.add(jLabel33);
        jLabel33.setBounds(445, 220, 370, 23);

        scrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane3.setName("scrollPane3"); // NOI18N

        ObatYangDiberikan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ObatYangDiberikan.setColumns(20);
        ObatYangDiberikan.setRows(5);
        ObatYangDiberikan.setName("ObatYangDiberikan"); // NOI18N
        ObatYangDiberikan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ObatYangDiberikanKeyPressed(evt);
            }
        });
        scrollPane3.setViewportView(ObatYangDiberikan);

        FormInput.add(scrollPane3);
        scrollPane3.setBounds(15, 240, 410, 73);

        scrollPane4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane4.setName("scrollPane4"); // NOI18N

        PemeriksaanPenunjang.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        PemeriksaanPenunjang.setColumns(20);
        PemeriksaanPenunjang.setRows(5);
        PemeriksaanPenunjang.setName("PemeriksaanPenunjang"); // NOI18N
        PemeriksaanPenunjang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemeriksaanPenunjangKeyPressed(evt);
            }
        });
        scrollPane4.setViewportView(PemeriksaanPenunjang);

        FormInput.add(scrollPane4);
        scrollPane4.setBounds(445, 240, 410, 73);

        jSeparator4.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator4.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator4.setName("jSeparator4"); // NOI18N
        FormInput.add(jSeparator4);
        jSeparator4.setBounds(0, 320, 880, 1);

        jLabel34.setText("Pasien/Keluarga Mengetahui & Menyetujui Alasan Pemindahan :");
        jLabel34.setName("jLabel34"); // NOI18N
        FormInput.add(jLabel34);
        jLabel34.setBounds(451, 330, 320, 23);

        IndikasiPindah.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Kondisi Pasien Stabil", "Kondisi Pasien Tidak Ada Perubahan", "Kondisi Pasien Memburuk", "Fasilitas Kurang Memadai", "Fasilitas Butuh Lebih Baik", "Tenaga Membutuhkan Yang Lebih Ahli", "Tenaga Kurang", "Lain-lain" }));
        IndikasiPindah.setName("IndikasiPindah"); // NOI18N
        IndikasiPindah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IndikasiPindahKeyPressed(evt);
            }
        });
        FormInput.add(IndikasiPindah);
        IndikasiPindah.setBounds(486, 40, 235, 23);

        AsalRuang.setHighlighter(null);
        AsalRuang.setName("AsalRuang"); // NOI18N
        AsalRuang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AsalRuangKeyPressed(evt);
            }
        });
        FormInput.add(AsalRuang);
        AsalRuang.setBounds(169, 80, 150, 23);

        jLabel35.setText(":");
        jLabel35.setName("jLabel35"); // NOI18N
        FormInput.add(jLabel35);
        jLabel35.setBounds(131, 330, 15, 23);

        MetodePemindahan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Kursi Roda", "Tempat Tidur", "Brankar", "Jalan Sendiri", "-" }));
        MetodePemindahan.setName("MetodePemindahan"); // NOI18N
        MetodePemindahan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MetodePemindahanKeyPressed(evt);
            }
        });
        FormInput.add(MetodePemindahan);
        MetodePemindahan.setBounds(739, 80, 115, 23);

        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel36.setText("Peralatan Yang Menyertai");
        jLabel36.setName("jLabel36"); // NOI18N
        FormInput.add(jLabel36);
        jLabel36.setBounds(15, 330, 130, 23);

        jLabel37.setText(":");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(374, 360, 20, 23);

        NamaMenyetujui.setHighlighter(null);
        NamaMenyetujui.setName("NamaMenyetujui"); // NOI18N
        NamaMenyetujui.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NamaMenyetujuiKeyPressed(evt);
            }
        });
        FormInput.add(NamaMenyetujui);
        NamaMenyetujui.setBounds(398, 360, 230, 23);

        jLabel38.setText("Hubungan :");
        jLabel38.setName("jLabel38"); // NOI18N
        FormInput.add(jLabel38);
        jLabel38.setBounds(621, 360, 80, 23);

        HubunganMenyetujui.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Kakak", "Adik", "Saudara", "Keluarga", "Kakek", "Nenek", "Orang Tua", "Suami", "Istri", "Penanggung Jawab", "Menantu", "Ipar", "Mertua", "-" }));
        HubunganMenyetujui.setName("HubunganMenyetujui"); // NOI18N
        HubunganMenyetujui.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HubunganMenyetujuiKeyPressed(evt);
            }
        });
        FormInput.add(HubunganMenyetujui);
        HubunganMenyetujui.setBounds(705, 360, 150, 23);

        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel39.setText("Bila Pemberi Persetujuan Adalah Keluarga/Penanggung Jawab Pasien, Nama");
        jLabel39.setName("jLabel39"); // NOI18N
        FormInput.add(jLabel39);
        jLabel39.setBounds(15, 360, 380, 23);

        jSeparator5.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator5.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator5.setName("jSeparator5"); // NOI18N
        FormInput.add(jSeparator5);
        jSeparator5.setBounds(0, 390, 880, 1);

        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel40.setText("Keadaan Pasien Saat Pindah Sebelum Transfer :");
        jLabel40.setName("jLabel40"); // NOI18N
        FormInput.add(jLabel40);
        jLabel40.setBounds(15, 390, 320, 23);

        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel41.setText("Keluhan Utama :");
        jLabel41.setName("jLabel41"); // NOI18N
        FormInput.add(jLabel41);
        jLabel41.setBounds(445, 410, 170, 23);

        scrollPane5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane5.setName("scrollPane5"); // NOI18N

        KeluhanUtamaSebelumTransfer.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        KeluhanUtamaSebelumTransfer.setColumns(20);
        KeluhanUtamaSebelumTransfer.setRows(5);
        KeluhanUtamaSebelumTransfer.setName("KeluhanUtamaSebelumTransfer"); // NOI18N
        KeluhanUtamaSebelumTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeluhanUtamaSebelumTransferKeyPressed(evt);
            }
        });
        scrollPane5.setViewportView(KeluhanUtamaSebelumTransfer);

        FormInput.add(scrollPane5);
        scrollPane5.setBounds(445, 430, 410, 63);

        jLabel42.setText("Keadaan Umum :");
        jLabel42.setName("jLabel42"); // NOI18N
        FormInput.add(jLabel42);
        jLabel42.setBounds(46, 410, 90, 23);

        KeadaanUmumSebelumTransfer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Compos Mentis", "Gelisah", "Delirium", "Koma" }));
        KeadaanUmumSebelumTransfer.setName("KeadaanUmumSebelumTransfer"); // NOI18N
        KeadaanUmumSebelumTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeadaanUmumSebelumTransferKeyPressed(evt);
            }
        });
        FormInput.add(KeadaanUmumSebelumTransfer);
        KeadaanUmumSebelumTransfer.setBounds(140, 410, 130, 23);

        TDSebelumTransfer.setFocusTraversalPolicyProvider(true);
        TDSebelumTransfer.setName("TDSebelumTransfer"); // NOI18N
        TDSebelumTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TDSebelumTransferKeyPressed(evt);
            }
        });
        FormInput.add(TDSebelumTransfer);
        TDSebelumTransfer.setBounds(317, 410, 60, 23);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setText("mmHg");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(380, 410, 50, 23);

        jLabel17.setText("Nadi :");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(46, 440, 90, 23);

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel22.setText("x/menit");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(203, 440, 50, 23);

        NadiSebelumTransfer.setFocusTraversalPolicyProvider(true);
        NadiSebelumTransfer.setName("NadiSebelumTransfer"); // NOI18N
        NadiSebelumTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NadiSebelumTransferKeyPressed(evt);
            }
        });
        FormInput.add(NadiSebelumTransfer);
        NadiSebelumTransfer.setBounds(140, 440, 60, 23);

        jLabel26.setText("RR :");
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(283, 440, 30, 23);

        RRSebelumTransfer.setFocusTraversalPolicyProvider(true);
        RRSebelumTransfer.setName("RRSebelumTransfer"); // NOI18N
        RRSebelumTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RRSebelumTransferKeyPressed(evt);
            }
        });
        FormInput.add(RRSebelumTransfer);
        RRSebelumTransfer.setBounds(317, 440, 60, 23);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel25.setText("x/menit");
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(380, 440, 50, 23);

        jLabel24.setText("Suhu :");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(46, 470, 90, 23);

        SuhuSebelumTransfer.setFocusTraversalPolicyProvider(true);
        SuhuSebelumTransfer.setName("SuhuSebelumTransfer"); // NOI18N
        SuhuSebelumTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SuhuSebelumTransferKeyPressed(evt);
            }
        });
        FormInput.add(SuhuSebelumTransfer);
        SuhuSebelumTransfer.setBounds(140, 470, 60, 23);

        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel27.setText("°C");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(203, 470, 30, 23);

        jLabel28.setText("TD :");
        jLabel28.setName("jLabel28"); // NOI18N
        FormInput.add(jLabel28);
        jLabel28.setBounds(283, 410, 30, 23);

        jSeparator6.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator6.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator6.setName("jSeparator6"); // NOI18N
        FormInput.add(jSeparator6);
        jSeparator6.setBounds(0, 500, 880, 1);

        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel43.setText("Keadaan Pasien Saat Pindah Setelah Transfer :");
        jLabel43.setName("jLabel43"); // NOI18N
        FormInput.add(jLabel43);
        jLabel43.setBounds(15, 500, 320, 23);

        jLabel44.setText("Keadaan Umum :");
        jLabel44.setName("jLabel44"); // NOI18N
        FormInput.add(jLabel44);
        jLabel44.setBounds(46, 520, 90, 23);

        KeadaanUmumSetelahTransfer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Compos Mentis", "Gelisah", "Delirium", "Koma" }));
        KeadaanUmumSetelahTransfer.setName("KeadaanUmumSetelahTransfer"); // NOI18N
        KeadaanUmumSetelahTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeadaanUmumSetelahTransferKeyPressed(evt);
            }
        });
        FormInput.add(KeadaanUmumSetelahTransfer);
        KeadaanUmumSetelahTransfer.setBounds(140, 520, 130, 23);

        jLabel29.setText("TD :");
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(283, 520, 30, 23);

        TDSetelahTransfer.setFocusTraversalPolicyProvider(true);
        TDSetelahTransfer.setName("TDSetelahTransfer"); // NOI18N
        TDSetelahTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TDSetelahTransferKeyPressed(evt);
            }
        });
        FormInput.add(TDSetelahTransfer);
        TDSetelahTransfer.setBounds(317, 520, 60, 23);

        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel45.setText("mmHg");
        jLabel45.setName("jLabel45"); // NOI18N
        FormInput.add(jLabel45);
        jLabel45.setBounds(380, 520, 50, 23);

        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel46.setText("Keluhan Utama :");
        jLabel46.setName("jLabel46"); // NOI18N
        FormInput.add(jLabel46);
        jLabel46.setBounds(445, 520, 170, 23);

        scrollPane6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane6.setName("scrollPane6"); // NOI18N

        KeluhanUtamaSetelahTransfer.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        KeluhanUtamaSetelahTransfer.setColumns(20);
        KeluhanUtamaSetelahTransfer.setRows(5);
        KeluhanUtamaSetelahTransfer.setName("KeluhanUtamaSetelahTransfer"); // NOI18N
        KeluhanUtamaSetelahTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeluhanUtamaSetelahTransferKeyPressed(evt);
            }
        });
        scrollPane6.setViewportView(KeluhanUtamaSetelahTransfer);

        FormInput.add(scrollPane6);
        scrollPane6.setBounds(445, 540, 410, 63);

        jLabel47.setText("Nadi :");
        jLabel47.setName("jLabel47"); // NOI18N
        FormInput.add(jLabel47);
        jLabel47.setBounds(46, 550, 90, 23);

        NadiSetelahTransfer.setFocusTraversalPolicyProvider(true);
        NadiSetelahTransfer.setName("NadiSetelahTransfer"); // NOI18N
        NadiSetelahTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NadiSetelahTransferKeyPressed(evt);
            }
        });
        FormInput.add(NadiSetelahTransfer);
        NadiSetelahTransfer.setBounds(140, 550, 60, 23);

        jLabel48.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel48.setText("x/menit");
        jLabel48.setName("jLabel48"); // NOI18N
        FormInput.add(jLabel48);
        jLabel48.setBounds(203, 550, 50, 23);

        jLabel49.setText("RR :");
        jLabel49.setName("jLabel49"); // NOI18N
        FormInput.add(jLabel49);
        jLabel49.setBounds(283, 550, 30, 23);

        RRSetelahTransfer.setFocusTraversalPolicyProvider(true);
        RRSetelahTransfer.setName("RRSetelahTransfer"); // NOI18N
        RRSetelahTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RRSetelahTransferKeyPressed(evt);
            }
        });
        FormInput.add(RRSetelahTransfer);
        RRSetelahTransfer.setBounds(317, 550, 60, 23);

        jLabel50.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel50.setText("x/menit");
        jLabel50.setName("jLabel50"); // NOI18N
        FormInput.add(jLabel50);
        jLabel50.setBounds(380, 550, 50, 23);

        jLabel51.setText("Suhu :");
        jLabel51.setName("jLabel51"); // NOI18N
        FormInput.add(jLabel51);
        jLabel51.setBounds(46, 580, 90, 23);

        SuhuSetelahTransfer.setFocusTraversalPolicyProvider(true);
        SuhuSetelahTransfer.setName("SuhuSetelahTransfer"); // NOI18N
        SuhuSetelahTransfer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SuhuSetelahTransferKeyPressed(evt);
            }
        });
        FormInput.add(SuhuSetelahTransfer);
        SuhuSetelahTransfer.setBounds(140, 580, 60, 23);

        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel52.setText("°C");
        jLabel52.setName("jLabel52"); // NOI18N
        FormInput.add(jLabel52);
        jLabel52.setBounds(203, 580, 30, 23);

        jSeparator7.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator7.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator7.setName("jSeparator7"); // NOI18N
        FormInput.add(jSeparator7);
        jSeparator7.setBounds(0, 610, 880, 1);

        label14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label14.setText("Petugas / Perawat :");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label14);
        label14.setBounds(15, 610, 130, 23);

        KdPetugasMenyerahkan.setEditable(false);
        KdPetugasMenyerahkan.setName("KdPetugasMenyerahkan"); // NOI18N
        KdPetugasMenyerahkan.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(KdPetugasMenyerahkan);
        KdPetugasMenyerahkan.setBounds(140, 630, 100, 23);

        NmPetugasMenyerahkan.setEditable(false);
        NmPetugasMenyerahkan.setName("NmPetugasMenyerahkan"); // NOI18N
        NmPetugasMenyerahkan.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmPetugasMenyerahkan);
        NmPetugasMenyerahkan.setBounds(242, 630, 180, 23);

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
        BtnDokter.setBounds(424, 630, 28, 23);

        label15.setText("Menerima :");
        label15.setName("label15"); // NOI18N
        label15.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label15);
        label15.setBounds(469, 630, 70, 23);

        KdPetugasMenerima.setEditable(false);
        KdPetugasMenerima.setName("KdPetugasMenerima"); // NOI18N
        KdPetugasMenerima.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(KdPetugasMenerima);
        KdPetugasMenerima.setBounds(543, 630, 100, 23);

        NmPetugasMenerima.setEditable(false);
        NmPetugasMenerima.setName("NmPetugasMenerima"); // NOI18N
        NmPetugasMenerima.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmPetugasMenerima);
        NmPetugasMenerima.setBounds(645, 630, 180, 23);

        BtnMenerima.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnMenerima.setMnemonic('2');
        BtnMenerima.setToolTipText("Alt+2");
        BtnMenerima.setName("BtnMenerima"); // NOI18N
        BtnMenerima.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnMenerima.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnMenerimaActionPerformed(evt);
            }
        });
        BtnMenerima.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnMenerimaKeyPressed(evt);
            }
        });
        FormInput.add(BtnMenerima);
        BtnMenerima.setBounds(827, 630, 28, 23);

        label16.setText("Menyerahkan :");
        label16.setName("label16"); // NOI18N
        label16.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label16);
        label16.setBounds(46, 630, 90, 23);

        scrollInput.setViewportView(FormInput);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Transfer Pasien", internalFrame2);

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

        jLabel19.setText("Tgl.Pindah :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(68, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "07-02-2026" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "07-02-2026" }));
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
        TCari.setPreferredSize(new java.awt.Dimension(197, 23));
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
        FormPhoto.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), " Bukti Pengambilan Persetujuan : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
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

        internalFrame3.add(PanelAccor, java.awt.BorderLayout.EAST);

        TabRawat.addTab("Data Transfer Pasien", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(NmPetugasMenyerahkan.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Petugas Yang Menyerahkan");
        }else if(NmPetugasMenerima.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Petugas Yang Menerima");
        }else if(AsalRuang.getText().trim().equals("")){
            Valid.textKosong(BtnMenerima,"Petugas Yang Menerima");
        }else if(AsalRuang.getText().trim().equals("")){
            Valid.textKosong(AsalRuang,"Asal Ruang");
        }else if(RuangSelanjutnya.getText().trim().equals("")){
            Valid.textKosong(RuangSelanjutnya,"Ruang Selanjutnya");
        }else if(DiagnosaUtama.getText().trim().equals("")){
            Valid.textKosong(DiagnosaUtama,"Diagnosa Utama");
        }else if(ProsedurDilakukan.getText().trim().equals("")){
            Valid.textKosong(ProsedurDilakukan,"Prosedur Dilakukan");
        }else if(ObatYangDiberikan.getText().trim().equals("")){
            Valid.textKosong(ObatYangDiberikan,"Obat Yang Diberikan");
        }else{
            if(akses.getkode().equals("Admin Utama")){
                simpan();
            }else {
                if(akses.getkode().equals(KdPetugasMenerima.getText())||akses.getkode().equals(KdPetugasMenyerahkan.getText())){
                    simpan();
                }else{
                    JOptionPane.showMessageDialog(null,"Harus salah satu petugas sesuai user login..!!");
                }
            }
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnMenerima,BtnBatal);
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
            }else {
                if(akses.getkode().equals(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString())||akses.getkode().equals(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString())){
                    hapus();
                }else{
                    JOptionPane.showMessageDialog(null,"Harus salah satu petugas sesuai user login..!!");
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
        }else if(NmPetugasMenyerahkan.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Petugas Yang Menyerahkan");
        }else if(NmPetugasMenerima.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Petugas Yang Menerima");
        }else if(AsalRuang.getText().trim().equals("")){
            Valid.textKosong(BtnMenerima,"Petugas Yang Menerima");
        }else if(AsalRuang.getText().trim().equals("")){
            Valid.textKosong(AsalRuang,"Asal Ruang");
        }else if(RuangSelanjutnya.getText().trim().equals("")){
            Valid.textKosong(RuangSelanjutnya,"Ruang Selanjutnya");
        }else if(DiagnosaUtama.getText().trim().equals("")){
            Valid.textKosong(DiagnosaUtama,"Diagnosa Utama");
        }else if(ProsedurDilakukan.getText().trim().equals("")){
            Valid.textKosong(ProsedurDilakukan,"Prosedur Dilakukan");
        }else if(ObatYangDiberikan.getText().trim().equals("")){
            Valid.textKosong(ObatYangDiberikan,"Obat Yang Diberikan");
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else {
                    if(akses.getkode().equals(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString())||akses.getkode().equals(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString())){
                        ganti();
                    }else{
                        JOptionPane.showMessageDialog(null,"Harus salah satu petugas sesuai user login..!!");
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

    /**
     * [RM 04-1/04-2] DIKEMBALIKAN ke perilaku ASLI (cetak SEMUA data dlm
     * rentang tanggal pencarian, dump HTML+CSS ke file2.css/
     * TransferPasienAntarRuang.html lalu buka browser) - permintaan eksplisit
     * user. Cetak SATU record (JasperReports Lembar/PDF) dipindah ke
     * `BtnPrint1` di panel ringkasan (LoadHTML) tab Data Transfer Pasien,
     * lihat cetakLaporanTransfer()/BtnPrint1ActionPerformed.
     */
    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            try{
                if(TCari.getText().trim().equals("")){
                    ps=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,"+
                            "transfer_pasien_antar_ruang.tanggal_masuk,transfer_pasien_antar_ruang.tanggal_pindah,transfer_pasien_antar_ruang.asal_ruang,"+
                            "transfer_pasien_antar_ruang.ruang_selanjutnya,transfer_pasien_antar_ruang.diagnosa_utama,transfer_pasien_antar_ruang.diagnosa_sekunder,"+
                            "transfer_pasien_antar_ruang.indikasi_pindah_ruang,transfer_pasien_antar_ruang.keterangan_indikasi_pindah_ruang,"+
                            "transfer_pasien_antar_ruang.prosedur_yang_sudah_dilakukan,transfer_pasien_antar_ruang.obat_yang_telah_diberikan,"+
                            "transfer_pasien_antar_ruang.metode_pemindahan_pasien,transfer_pasien_antar_ruang.peralatan_yang_menyertai,"+
                            "transfer_pasien_antar_ruang.keterangan_peralatan_yang_menyertai,transfer_pasien_antar_ruang.pemeriksaan_penunjang_yang_dilakukan,"+
                            "transfer_pasien_antar_ruang.pasien_keluarga_menyetujui,transfer_pasien_antar_ruang.nama_menyetujui,transfer_pasien_antar_ruang.hubungan_menyetujui,"+
                            "transfer_pasien_antar_ruang.keluhan_utama_sebelum_transfer,transfer_pasien_antar_ruang.keadaan_umum_sebelum_transfer,"+
                            "transfer_pasien_antar_ruang.td_sebelum_transfer,transfer_pasien_antar_ruang.nadi_sebelum_transfer,transfer_pasien_antar_ruang.rr_sebelum_transfer,"+
                            "transfer_pasien_antar_ruang.suhu_sebelum_transfer,transfer_pasien_antar_ruang.keluhan_utama_sesudah_transfer,"+
                            "transfer_pasien_antar_ruang.keadaan_umum_sesudah_transfer,transfer_pasien_antar_ruang.td_sesudah_transfer,"+
                            "transfer_pasien_antar_ruang.nadi_sesudah_transfer,transfer_pasien_antar_ruang.rr_sesudah_transfer,transfer_pasien_antar_ruang.suhu_sesudah_transfer,"+
                            "transfer_pasien_antar_ruang.nip_menyerahkan,petugasmenyerahkan.nama as petugasmenyerahkan,transfer_pasien_antar_ruang.nip_menerima,"+
                            "petugasmenerima.nama as petugasmenerima "+
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "+
                            "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                            "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip where "+
                            "transfer_pasien_antar_ruang.tanggal_pindah between ? and ? order by transfer_pasien_antar_ruang.tanggal_pindah");
                }else{
                    ps=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,"+
                            "transfer_pasien_antar_ruang.tanggal_masuk,transfer_pasien_antar_ruang.tanggal_pindah,transfer_pasien_antar_ruang.asal_ruang,"+
                            "transfer_pasien_antar_ruang.ruang_selanjutnya,transfer_pasien_antar_ruang.diagnosa_utama,transfer_pasien_antar_ruang.diagnosa_sekunder,"+
                            "transfer_pasien_antar_ruang.indikasi_pindah_ruang,transfer_pasien_antar_ruang.keterangan_indikasi_pindah_ruang,"+
                            "transfer_pasien_antar_ruang.prosedur_yang_sudah_dilakukan,transfer_pasien_antar_ruang.obat_yang_telah_diberikan,"+
                            "transfer_pasien_antar_ruang.metode_pemindahan_pasien,transfer_pasien_antar_ruang.peralatan_yang_menyertai,"+
                            "transfer_pasien_antar_ruang.keterangan_peralatan_yang_menyertai,transfer_pasien_antar_ruang.pemeriksaan_penunjang_yang_dilakukan,"+
                            "transfer_pasien_antar_ruang.pasien_keluarga_menyetujui,transfer_pasien_antar_ruang.nama_menyetujui,transfer_pasien_antar_ruang.hubungan_menyetujui,"+
                            "transfer_pasien_antar_ruang.keluhan_utama_sebelum_transfer,transfer_pasien_antar_ruang.keadaan_umum_sebelum_transfer,"+
                            "transfer_pasien_antar_ruang.td_sebelum_transfer,transfer_pasien_antar_ruang.nadi_sebelum_transfer,transfer_pasien_antar_ruang.rr_sebelum_transfer,"+
                            "transfer_pasien_antar_ruang.suhu_sebelum_transfer,transfer_pasien_antar_ruang.keluhan_utama_sesudah_transfer,"+
                            "transfer_pasien_antar_ruang.keadaan_umum_sesudah_transfer,transfer_pasien_antar_ruang.td_sesudah_transfer,"+
                            "transfer_pasien_antar_ruang.nadi_sesudah_transfer,transfer_pasien_antar_ruang.rr_sesudah_transfer,transfer_pasien_antar_ruang.suhu_sesudah_transfer,"+
                            "transfer_pasien_antar_ruang.nip_menyerahkan,petugasmenyerahkan.nama as petugasmenyerahkan,transfer_pasien_antar_ruang.nip_menerima,"+
                            "petugasmenerima.nama as petugasmenerima "+
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "+
                            "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                            "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip where "+
                            "transfer_pasien_antar_ruang.tanggal_pindah between ? and ? and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "+
                            "transfer_pasien_antar_ruang.nip_menyerahkan like ? or petugasmenyerahkan.nama like ?) order by transfer_pasien_antar_ruang.tanggal_pindah");
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
                    htmlContent = new StringBuilder();
                    htmlContent.append(
                        "<tr class='isi'>"+
                           "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>No.Rawat</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>No.RM</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Nama Pasien</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Tgl.Lahir</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>J.K.</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Tanggal Masuk</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Tanggal Pindah</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Indikasi Pindah</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Keterangan Indikasi Pindah</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Asal Ruang Rawat / Poliklinik</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Ruang Rawat Selanjutnya</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Metode Pemindahan</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Diagnosa Utama</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Diagnosa Sekunder</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Prosedur Yang Sudah Dilakukan</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Obat Yang Telah Diberikan</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Pemeriksaan Penunjang Yang Sudah Dilakukan</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Peralatan Yang Menyertai</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Keterangan Peralatan Menyertai</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Menyetujui Pemindahan</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Nama Keluarga/Penanggung Jawab</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Hubungan</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Keadaan Umum SbT</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>TD SbT</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Nadi SbT</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>RR SbT</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Suhu Sbt</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Keluhan Utama Sebelum Transfer</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Keadaan Umum StT</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>TD StT</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Nadi StT</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>RR StT</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Suhu Stt</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Keluhan Utama Setelah Transfer</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>NIP Menyerahkan</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Petugas Yang Menyerahkan</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>NIP Menerima</b></td>"+
                            "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Petugas Yang Menerima</b></td>"+
                        "</tr>"
                    );
                    while(rs.next()){
                        htmlContent.append(
                            "<tr class='isi'>"+
                               "<td valign='top'>"+rs.getString("no_rawat")+"</td>"+
                               "<td valign='top'>"+rs.getString("no_rkm_medis")+"</td>"+
                               "<td valign='top'>"+rs.getString("nm_pasien")+"</td>"+
                               "<td valign='top'>"+rs.getString("tgl_lahir")+"</td>"+
                               "<td valign='top'>"+rs.getString("jk")+"</td>"+
                               "<td valign='top'>"+rs.getString("tanggal_masuk")+"</td>"+
                               "<td valign='top'>"+rs.getString("tanggal_pindah")+"</td>"+
                               "<td valign='top'>"+rs.getString("indikasi_pindah_ruang")+"</td>"+
                               "<td valign='top'>"+rs.getString("keterangan_indikasi_pindah_ruang")+"</td>"+
                               "<td valign='top'>"+rs.getString("asal_ruang")+"</td>"+
                               "<td valign='top'>"+rs.getString("ruang_selanjutnya")+"</td>"+
                               "<td valign='top'>"+rs.getString("metode_pemindahan_pasien")+"</td>"+
                               "<td valign='top'>"+rs.getString("diagnosa_utama")+"</td>"+
                               "<td valign='top'>"+rs.getString("diagnosa_sekunder")+"</td>"+
                               "<td valign='top'>"+rs.getString("prosedur_yang_sudah_dilakukan")+"</td>"+
                               "<td valign='top'>"+rs.getString("obat_yang_telah_diberikan")+"</td>"+
                               "<td valign='top'>"+rs.getString("pemeriksaan_penunjang_yang_dilakukan")+"</td>"+
                               "<td valign='top'>"+rs.getString("peralatan_yang_menyertai")+"</td>"+
                               "<td valign='top'>"+rs.getString("keterangan_peralatan_yang_menyertai")+"</td>"+
                               "<td valign='top'>"+rs.getString("pasien_keluarga_menyetujui")+"</td>"+
                               "<td valign='top'>"+rs.getString("nama_menyetujui")+"</td>"+
                               "<td valign='top'>"+rs.getString("hubungan_menyetujui")+"</td>"+
                               "<td valign='top'>"+rs.getString("keadaan_umum_sebelum_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("td_sebelum_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("nadi_sebelum_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("rr_sebelum_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("suhu_sebelum_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("keluhan_utama_sebelum_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("keadaan_umum_sesudah_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("td_sesudah_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("nadi_sesudah_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("rr_sesudah_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("suhu_sesudah_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("keluhan_utama_sesudah_transfer")+"</td>"+
                               "<td valign='top'>"+rs.getString("nip_menyerahkan")+"</td>"+
                               "<td valign='top'>"+rs.getString("petugasmenyerahkan")+"</td>"+
                               "<td valign='top'>"+rs.getString("nip_menerima")+"</td>"+
                               "<td valign='top'>"+rs.getString("petugasmenerima")+"</td>"+
                            "</tr>");
                    }
                    LoadHTML.setText(
                        "<html>"+
                          "<table width='4000' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
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

                    File f = new File("TransferPasienAntarRuang.html");
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                    bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
                                "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                                "<table width='4000px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                    "<tr class='isi2'>"+
                                        "<td valign='top' align='center'>"+
                                            "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                            akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                            akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                            "<font size='2' face='Tahoma'>DATA TRANSFER PASIEN ANTAR RUANG<br><br></font>"+
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

    /**
     * [RM 04-1/04-2] Resolve path relatif kolom `photo` (mis.
     * "pages/upload/xxx.jpg") jadi InputStream lokal utk parameter gambar
     * JRXML. Webapp `persetujuantransferruang` ternyata dilayani (dan
     * upload foto/TTD-nya ditulis) dari `C:\xampp\htdocs\webapps\...`,
     * BUKAN dari folder project ini (lihat catatan di PROGRESS.md) - versi
     * lama method ini cuma cek `<project>/webapps/...` yg TIDAK PERNAH ada
     * filenya, jadi cetakan selalu blank utk TTD yg sudah ter-upload lewat
     * browser. Diperbaiki mengikuti pola `getSecondOpinionPhotoURL()` di
     * `SuratPermintaanSecondOpinion.java` - coba beberapa lokasi htdocs
     * umum lintas-platform dulu, baru fallback ke folder project lama.
     */
    private java.io.InputStream resolveBukti(String relPath){
        if(relPath==null || relPath.trim().isEmpty()){ return null; }
        String cleanedPath = relPath.replace("\\","/");
        String[] localRoots = {
            "/Applications/XAMPP/xamppfiles/htdocs",
            "/opt/lampp/htdocs",
            "C:/xampp/htdocs",
            "D:/xampp/htdocs"
        };
        try{
            for(String root : localRoots){
                File f = new File(root, koneksiDB.HYBRIDWEB()+"/persetujuantransferruang/"+cleanedPath);
                if(f.isFile()){
                    return new FileInputStream(f);
                }
            }
            File fallback = new File(System.getProperty("user.dir")+File.separator+"webapps"+File.separator+"persetujuantransferruang"+File.separator+cleanedPath);
            return fallback.isFile() ? new FileInputStream(fallback) : null;
        }catch(Exception e){
            return null;
        }
    }

    private void cetakLaporanTransfer(String mode){
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try{
            String noRawatCetak = tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();
            String tanggalMasukCetak = tbObat.getValueAt(tbObat.getSelectedRow(),5).toString();

            String fotoPengirim = null, fotoPenerima = null, fotoDokter = null, fotoPasien = null;
            ps = koneksi.prepareStatement("select photo from bukti_transfer_petugas_pengirim where no_rawat=? and tanggal_masuk=?");
            ps.setString(1,noRawatCetak); ps.setString(2,tanggalMasukCetak);
            rs = ps.executeQuery();
            if(rs.next()){ fotoPengirim = rs.getString("photo"); }
            rs.close(); ps.close();

            ps = koneksi.prepareStatement("select photo from bukti_transfer_petugas_penerima where no_rawat=? and tanggal_masuk=?");
            ps.setString(1,noRawatCetak); ps.setString(2,tanggalMasukCetak);
            rs = ps.executeQuery();
            if(rs.next()){ fotoPenerima = rs.getString("photo"); }
            rs.close(); ps.close();

            ps = koneksi.prepareStatement("select photo from bukti_transfer_dokter_merawat where no_rawat=? and tanggal_masuk=?");
            ps.setString(1,noRawatCetak); ps.setString(2,tanggalMasukCetak);
            rs = ps.executeQuery();
            if(rs.next()){ fotoDokter = rs.getString("photo"); }
            rs.close(); ps.close();

            ps = koneksi.prepareStatement("select photo from bukti_persetujuan_transfer_pasien_antar_ruang where no_rawat=? and tanggal_masuk=?");
            ps.setString(1,noRawatCetak); ps.setString(2,tanggalMasukCetak);
            rs = ps.executeQuery();
            if(rs.next()){ fotoPasien = rs.getString("photo"); }
            rs.close(); ps.close();

            Map<String,Object> param = new HashMap<>();
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());
            param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
            param.put("sig_pengirim",resolveBukti(fotoPengirim));
            param.put("sig_penerima",resolveBukti(fotoPenerima));
            param.put("sig_dokter",resolveBukti(fotoDokter));
            param.put("sig_pasien",resolveBukti(fotoPasien));

            String qryCetak =
                "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"+
                "transfer_pasien_antar_ruang.*,"+
                "petugasmenyerahkan.nama as nm_menyerahkan,petugasmenerima.nama as nm_menerima,"+
                "dokterasal1.nm_dokter as nm_dokter_asal_1,dokterasal2.nm_dokter as nm_dokter_asal_2,dokterasal3.nm_dokter as nm_dokter_asal_3,"+
                "doktertujuan1.nm_dokter as nm_dokter_tujuan_1,doktertujuan2.nm_dokter as nm_dokter_tujuan_2,doktertujuan3.nm_dokter as nm_dokter_tujuan_3 "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "+
                "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip "+
                "left join dokter as dokterasal1 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_1=dokterasal1.kd_dokter "+
                "left join dokter as dokterasal2 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_2=dokterasal2.kd_dokter "+
                "left join dokter as dokterasal3 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_3=dokterasal3.kd_dokter "+
                "left join dokter as doktertujuan1 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_1=doktertujuan1.kd_dokter "+
                "left join dokter as doktertujuan2 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_2=doktertujuan2.kd_dokter "+
                "left join dokter as doktertujuan3 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_3=doktertujuan3.kd_dokter "+
                "where transfer_pasien_antar_ruang.no_rawat='"+noRawatCetak+"' and transfer_pasien_antar_ruang.tanggal_masuk='"+tanggalMasukCetak+"'";

            if("PDF".equals(mode)){
                Valid.MyReportqrypdf("rptCetakTransferPasienAntarRuang.jasper","report","::[ Laporan Transfer Pasien Antar Ruang ]::",qryCetak,param);
            }else{
                Valid.MyReportqry("rptCetakTransferPasienAntarRuang.jasper","report","::[ Laporan Transfer Pasien Antar Ruang ]::",qryCetak,param);
            }
        }catch(Exception e){
            System.out.println("Notif cetakLaporanTransfer : "+e);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

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
                // [RM 04-1/04-2] Accordion (PanelAccor/FormPhoto) dibuka
                // otomatis begitu baris pasien diklik (permintaan user),
                // BUKAN cuma nunggu ChkAccor diklik manual - pola sama dgn
                // panel ringkasan RM 03-2/03-4-05-06. Diset SEBELUM
                // panggilPhoto() (bukan sesudah getData()) supaya grid TTD
                // ikut ter-refresh utk baris yg baru diklik ini juga (guard
                // `if(FormPhoto.isVisible())` di panggilPhoto() butuh
                // FormPhoto sudah visible SAAT dipanggil).
                ChkAccor.setSelected(true);
                isPhoto();
                panggilPhoto();
                // getData() dipanggil juga di klik biasa (bukan cuma
                // Space/Enter/Panah spt sebelumnya) - panel ringkasan
                // (LoadHTML, lihat bangunRingkasanHtml() di akhir getData())
                // perlu terisi begitu baris diklik, bukan cuma pas navigasi
                // keyboard. getData() cuma SELECT (tidak nulis), aman dipanggil
                // dari klik biasa.
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

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==1){
            runBackground(() ->tampil());
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void MenyetujuiPemindahanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MenyetujuiPemindahanKeyPressed
        Valid.pindah(evt,KeteranganPeralatan,NamaMenyetujui);
    }//GEN-LAST:event_MenyetujuiPemindahanKeyPressed

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{
            //Valid.pindah(evt,TCari,BtnDokter);
        }
    }//GEN-LAST:event_TNoRwKeyPressed

    private void TanggalMasukKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalMasukKeyPressed
        Valid.pindah2(evt,TNoRw,TanggalPindah);
    }//GEN-LAST:event_TanggalMasukKeyPressed

    private void TanggalPindahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalPindahKeyPressed
        Valid.pindah2(evt,TanggalMasuk,IndikasiPindah);
    }//GEN-LAST:event_TanggalPindahKeyPressed

    private void KeteranganIndikasiPindahRuangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganIndikasiPindahRuangKeyPressed
        Valid.pindah(evt,IndikasiPindah,AsalRuang);
    }//GEN-LAST:event_KeteranganIndikasiPindahRuangKeyPressed

    private void KeteranganPeralatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganPeralatanKeyPressed
        Valid.pindah(evt,PeralatanMenyertai,MenyetujuiPemindahan);
    }//GEN-LAST:event_KeteranganPeralatanKeyPressed

    private void RuangSelanjutnyaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RuangSelanjutnyaKeyPressed
        Valid.pindah(evt,AsalRuang,MetodePemindahan);
    }//GEN-LAST:event_RuangSelanjutnyaKeyPressed

    private void PeralatanMenyertaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PeralatanMenyertaiKeyPressed
        Valid.pindah(evt,PemeriksaanPenunjang,KeteranganPeralatan);
    }//GEN-LAST:event_PeralatanMenyertaiKeyPressed

    private void DiagnosaSekunderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosaSekunderKeyPressed
        Valid.pindah2(evt,DiagnosaUtama,ProsedurDilakukan);
    }//GEN-LAST:event_DiagnosaSekunderKeyPressed

    private void DiagnosaUtamaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosaUtamaKeyPressed
        Valid.pindah(evt,MetodePemindahan,DiagnosaSekunder);
    }//GEN-LAST:event_DiagnosaUtamaKeyPressed

    private void ProsedurDilakukanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ProsedurDilakukanKeyPressed
        Valid.pindah2(evt,DiagnosaSekunder,ObatYangDiberikan);
    }//GEN-LAST:event_ProsedurDilakukanKeyPressed

    private void ObatYangDiberikanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ObatYangDiberikanKeyPressed
        Valid.pindah2(evt,ProsedurDilakukan,PemeriksaanPenunjang);
    }//GEN-LAST:event_ObatYangDiberikanKeyPressed

    private void PemeriksaanPenunjangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemeriksaanPenunjangKeyPressed
        Valid.pindah2(evt,ObatYangDiberikan,PeralatanMenyertai);
    }//GEN-LAST:event_PemeriksaanPenunjangKeyPressed

    private void IndikasiPindahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_IndikasiPindahKeyPressed
        Valid.pindah(evt,TanggalPindah,KeteranganIndikasiPindahRuang);
    }//GEN-LAST:event_IndikasiPindahKeyPressed

    private void AsalRuangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AsalRuangKeyPressed
        Valid.pindah(evt,KeteranganIndikasiPindahRuang,RuangSelanjutnya);
    }//GEN-LAST:event_AsalRuangKeyPressed

    private void MetodePemindahanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MetodePemindahanKeyPressed
        Valid.pindah(evt,RuangSelanjutnya,DiagnosaUtama);
    }//GEN-LAST:event_MetodePemindahanKeyPressed

    private void NamaMenyetujuiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NamaMenyetujuiKeyPressed
        Valid.pindah(evt,MenyetujuiPemindahan,HubunganMenyetujui);
    }//GEN-LAST:event_NamaMenyetujuiKeyPressed

    private void HubunganMenyetujuiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HubunganMenyetujuiKeyPressed
        Valid.pindah(evt,NamaMenyetujui,KeadaanUmumSebelumTransfer);
    }//GEN-LAST:event_HubunganMenyetujuiKeyPressed

    private void KeluhanUtamaSebelumTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeluhanUtamaSebelumTransferKeyPressed
        Valid.pindah2(evt,SuhuSebelumTransfer,KeadaanUmumSetelahTransfer);
    }//GEN-LAST:event_KeluhanUtamaSebelumTransferKeyPressed

    private void KeadaanUmumSebelumTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeadaanUmumSebelumTransferKeyPressed
        Valid.pindah(evt,HubunganMenyetujui,TDSebelumTransfer);
    }//GEN-LAST:event_KeadaanUmumSebelumTransferKeyPressed

    private void TDSebelumTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TDSebelumTransferKeyPressed
        Valid.pindah(evt,KeadaanUmumSebelumTransfer,NadiSebelumTransfer);
    }//GEN-LAST:event_TDSebelumTransferKeyPressed

    private void NadiSebelumTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NadiSebelumTransferKeyPressed
        Valid.pindah(evt,TDSebelumTransfer,RRSebelumTransfer);
    }//GEN-LAST:event_NadiSebelumTransferKeyPressed

    private void RRSebelumTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RRSebelumTransferKeyPressed
        Valid.pindah(evt,NadiSebelumTransfer,SuhuSebelumTransfer);
    }//GEN-LAST:event_RRSebelumTransferKeyPressed

    private void SuhuSebelumTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SuhuSebelumTransferKeyPressed
        Valid.pindah(evt,RRSebelumTransfer,KeluhanUtamaSebelumTransfer);
    }//GEN-LAST:event_SuhuSebelumTransferKeyPressed

    private void KeadaanUmumSetelahTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeadaanUmumSetelahTransferKeyPressed
        Valid.pindah(evt,KeluhanUtamaSebelumTransfer,TDSetelahTransfer);
    }//GEN-LAST:event_KeadaanUmumSetelahTransferKeyPressed

    private void TDSetelahTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TDSetelahTransferKeyPressed
        Valid.pindah(evt,KeadaanUmumSetelahTransfer,NadiSetelahTransfer);
    }//GEN-LAST:event_TDSetelahTransferKeyPressed

    private void KeluhanUtamaSetelahTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeluhanUtamaSetelahTransferKeyPressed
        Valid.pindah2(evt,SuhuSetelahTransfer,BtnDokter);
    }//GEN-LAST:event_KeluhanUtamaSetelahTransferKeyPressed

    private void NadiSetelahTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NadiSetelahTransferKeyPressed
        Valid.pindah(evt,TDSetelahTransfer,RRSetelahTransfer);
    }//GEN-LAST:event_NadiSetelahTransferKeyPressed

    private void RRSetelahTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RRSetelahTransferKeyPressed
        Valid.pindah(evt,NadiSetelahTransfer,SuhuSetelahTransfer);
    }//GEN-LAST:event_RRSetelahTransferKeyPressed

    private void SuhuSetelahTransferKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SuhuSetelahTransferKeyPressed
        Valid.pindah(evt,RRSetelahTransfer,KeluhanUtamaSetelahTransfer);
    }//GEN-LAST:event_SuhuSetelahTransferKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        if (petugas == null || !petugas.isDisplayable()) {
            petugas=new DlgCariPetugas(null,false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(petugas.getTable().getSelectedRow()!= -1){
                        KdPetugasMenyerahkan.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                        NmPetugasMenyerahkan.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());
                        KdPetugasMenyerahkan.requestFocus();
                    }   
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
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
        Valid.pindah(evt,KeluhanUtamaSetelahTransfer,BtnMenerima);
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void BtnMenerimaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnMenerimaActionPerformed
        if (petugas == null || !petugas.isDisplayable()) {
            petugas=new DlgCariPetugas(null,false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(petugas.getTable().getSelectedRow()!= -1){
                        KdPetugasMenerima.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                        NmPetugasMenerima.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());
                        KdPetugasMenerima.requestFocus();
                    }   
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
    }//GEN-LAST:event_BtnMenerimaActionPerformed

    private void BtnMenerimaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnMenerimaKeyPressed
        Valid.pindah(evt,KeluhanUtamaSetelahTransfer,BtnSimpan);
    }//GEN-LAST:event_BtnMenerimaKeyPressed

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
                String noRawatAmbil = tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();
                String tanggalMasukAmbil = tbObat.getValueAt(tbObat.getSelectedRow(),5).toString();
                Sequel.queryu("delete from antripersetujuantransferantarruang");
                Sequel.queryu("insert into antripersetujuantransferantarruang values('"+noRawatAmbil+"','"+tanggalMasukAmbil+"')");
                // [RM 04-1/04-2] bersihkan bukti ke-4 peran (Pasien/Keluarga +
                // 3 tabel baru Petugas Pengirim/Penerima/Dokter) supaya
                // re-queue selalu mulai dari kosong utk ke-4 tanda tangan.
                Sequel.queryu("delete from bukti_persetujuan_transfer_pasien_antar_ruang where no_rawat='"+noRawatAmbil+"' and tanggal_masuk='"+tanggalMasukAmbil+"'");
                Sequel.queryu("delete from bukti_transfer_petugas_pengirim where no_rawat='"+noRawatAmbil+"' and tanggal_masuk='"+tanggalMasukAmbil+"'");
                Sequel.queryu("delete from bukti_transfer_petugas_penerima where no_rawat='"+noRawatAmbil+"' and tanggal_masuk='"+tanggalMasukAmbil+"'");
                Sequel.queryu("delete from bukti_transfer_dokter_merawat where no_rawat='"+noRawatAmbil+"' and tanggal_masuk='"+tanggalMasukAmbil+"'");
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
            }
        }
    }//GEN-LAST:event_btnAmbilActionPerformed

    private void BtnRefreshPhoto1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRefreshPhoto1ActionPerformed
        if(tbObat.getSelectedRow()>-1){
            panggilPhoto();
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih No.Pernyataan terlebih dahulu..!!");
        }
    }//GEN-LAST:event_BtnRefreshPhoto1ActionPerformed

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
            RMTransferPasienAntarRuang dialog = new RMTransferPasienAntarRuang(new javax.swing.JFrame(), true);
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
    private widget.TextBox AsalRuang;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnDokter;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnMenerima;
    private widget.Button BtnPrint;
    private widget.Button BtnRefreshPhoto1;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkAccor;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.TextArea DiagnosaSekunder;
    private widget.TextBox DiagnosaUtama;
    private widget.PanelBiasa FormInput;
    private widget.PanelBiasa FormPass3;
    private widget.PanelBiasa FormPhoto;
    private widget.ComboBox HubunganMenyetujui;
    private widget.ComboBox IndikasiPindah;
    private widget.TextBox Jk;
    private widget.TextBox KdPetugasMenerima;
    private widget.TextBox KdPetugasMenyerahkan;
    private widget.ComboBox KeadaanUmumSebelumTransfer;
    private widget.ComboBox KeadaanUmumSetelahTransfer;
    private widget.TextArea KeluhanUtamaSebelumTransfer;
    private widget.TextArea KeluhanUtamaSetelahTransfer;
    private widget.TextBox KeteranganIndikasiPindahRuang;
    private widget.TextBox KeteranganPeralatan;
    private widget.Label LCount;
    private widget.editorpane LoadHTML;
    private widget.editorpane LoadHTML2;
    private widget.ComboBox MenyetujuiPemindahan;
    private widget.ComboBox MetodePemindahan;
    private widget.TextBox NadiSebelumTransfer;
    private widget.TextBox NadiSetelahTransfer;
    private widget.TextBox NamaMenyetujui;
    private widget.TextBox NmPetugasMenerima;
    private widget.TextBox NmPetugasMenyerahkan;
    private widget.TextArea ObatYangDiberikan;
    private widget.PanelBiasa PanelAccor;
    private widget.TextArea PemeriksaanPenunjang;
    private widget.ComboBox PeralatanMenyertai;
    private widget.TextArea ProsedurDilakukan;
    private widget.TextBox RRSebelumTransfer;
    private widget.TextBox RRSetelahTransfer;
    private widget.TextBox RuangSelanjutnya;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll5;
    private widget.TextBox SuhuSebelumTransfer;
    private widget.TextBox SuhuSetelahTransfer;
    private widget.TextBox TCari;
    private widget.TextBox TDSebelumTransfer;
    private widget.TextBox TDSetelahTransfer;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal TanggalMasuk;
    private widget.Tanggal TanggalPindah;
    private widget.TextBox TglLahir;
    private widget.Button btnAmbil;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
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
    private widget.Label jLabel35;
    private widget.Label jLabel36;
    private widget.Label jLabel37;
    private widget.Label jLabel38;
    private widget.Label jLabel39;
    private widget.Label jLabel40;
    private widget.Label jLabel41;
    private widget.Label jLabel42;
    private widget.Label jLabel43;
    private widget.Label jLabel44;
    private widget.Label jLabel45;
    private widget.Label jLabel46;
    private widget.Label jLabel47;
    private widget.Label jLabel48;
    private widget.Label jLabel49;
    private widget.Label jLabel50;
    private widget.Label jLabel51;
    private widget.Label jLabel52;
    private widget.Label jLabel57;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private widget.Label label11;
    private widget.Label label12;
    private widget.Label label14;
    private widget.Label label15;
    private widget.Label label16;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane1;
    private widget.ScrollPane scrollPane2;
    private widget.ScrollPane scrollPane3;
    private widget.ScrollPane scrollPane4;
    private widget.ScrollPane scrollPane5;
    private widget.ScrollPane scrollPane6;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    // ===================================================================
    // [RM 04-1/04-2] Field baru mengikuti formulir fisik "Transfer Pasien
    // Antar Ruangan" (RSUD Demang Sepulau Raya). Ditambahkan sbg panel
    // GridBagLayout baru. Reorder total (permintaan user, kirim foto RM
    // 04-1/04-2) sudah dikerjakan: widget lama yg py padanan foto
    // DIREPARENT (panel.add(widgetLama,...)) ke panel baru sesuai urutan
    // fisik formulir, label GEN block lama yg jadi yatim disembunyikan
    // (lihat pensiunkanFieldTransferLama()), widget lama yg tidak dipakai
    // sama sekali (superseded oleh field baru) ikut disembunyikan.
    // Tanggal Masuk/Tanggal Transfer & Diagnosa SENGAJA dikecualikan dari
    // reparenting ini (permintaan eksplisit user) - tetap di header/area
    // atas FormInput (posisi GEN block asli, cuma direposisi/diresize via
    // .setBounds() di initTransferTambahan(), BUKAN direparent ke panel
    // GridBagLayout manapun).
    // ===================================================================
    private widget.PanelBiasa panelTransferKeluargaTambahan, panelTransferKelasDokter,
        panelTransferAlasanCaraPindah, panelTransferKondisiPasien,
        panelTransferOksigenServahTerima, panelTransferPage2A, panelTransferPage2B;

    private widget.TextBox TNoTlpMenyetujui;
    private widget.ComboBox SudahDijelaskanDiagnosa;
    private widget.Button BtnDiagnosaUtama;

    // -- Ruangan & Kelas: SEKARANG diambil dari tabel kamar (JOIN bangsal),
    // bukan input manual - AsalRuang/RuangSelanjutnya (widget GEN block lama)
    // dibuat read-only, diisi otomatis lewat PilihRuangAsal/PilihRuangTujuan;
    // KelasAsalRuang/KelasRuangSelanjutnya jadi TextBox read-only (bukan lagi
    // ComboBox pilihan manual), ikut terisi otomatis dari kamar.kelas.
    private widget.ComboBox PilihRuangAsal, PilihRuangTujuan;
    private widget.TextBox KelasAsalRuang, KelasRuangSelanjutnya;
    // -- Dokter Merawat Asal/Tujuan: SEMUA 3 slot (bukan cuma slot 1) diambil
    // dari data dokter lewat DlgCariDokter, tidak ada lagi input nama manual.
    private widget.TextBox KdDokterMerawatAsal1, NmDokterMerawatAsal1, KdDokterMerawatAsal2, NmDokterMerawatAsal2, KdDokterMerawatAsal3, NmDokterMerawatAsal3;
    private widget.Button BtnDokterMerawatAsal1, BtnDokterMerawatAsal2, BtnDokterMerawatAsal3;
    private widget.TextBox KdDokterMerawatTujuan1, NmDokterMerawatTujuan1, KdDokterMerawatTujuan2, NmDokterMerawatTujuan2, KdDokterMerawatTujuan3, NmDokterMerawatTujuan3;
    private widget.Button BtnDokterMerawatTujuan1, BtnDokterMerawatTujuan2, BtnDokterMerawatTujuan3;

    private widget.ComboBox KesadaranTransfer;
    private widget.TextBox TDTransfer, HRTransfer, RRTransfer, SuhuTransfer, GCSTransfer, SpO2Transfer;
    private widget.ComboBox TingkatNyeriTransfer;
    private widget.Tanggal ObservasiTerakhirPukul;

    private widget.ComboBox OksigenStatus;
    private widget.CekBox CekOksigenNasalKanule, CekOksigenNasalKateter, CekOksigenFaceMask, CekOksigenSungkupRebreathing,
        CekOksigenSungkupNonRebreathing, CekOksigenTracheostomy, CekOksigenEtt;

    private widget.RadioButton RbLineTidakAda, RbLineCvpPicc, RbLineIvLine; private javax.swing.ButtonGroup bgLineInvasif;
    private widget.Tanggal LineInvasifTanggal;
    private widget.CekBox CekKewaspadaanStandar, CekKewaspadaanKontak, CekKewaspadaanAirborne, CekKewaspadaanDroplet;
    private widget.CekBox CekNutrisiPuasa, CekNutrisiOral, CekNutrisiNgt, CekNutrisiDietKhusus;
    private widget.TextBox TNutrisiBatasanCairanCc, TNutrisiDietKhususJelaskan;
    private widget.RadioButton RbEliminasiBabNormal, RbEliminasiBabDiareKonstipasi, RbEliminasiBabIleustomyColostomy,
        RbEliminasiBabInkontinensiaAlvi; private javax.swing.ButtonGroup bgEliminasiBab;
    private widget.RadioButton RbEliminasiBakNormal, RbEliminasiBakInkontinensiaUrine, RbEliminasiBakKateterUrine;
    private javax.swing.ButtonGroup bgEliminasiBak;
    private widget.TextBox TEliminasiBakKateterNo;
    private widget.Tanggal DTPEliminasiBakKateterTanggalPasang;

    private widget.RadioButton RbAktifitasMandiri, RbAktifitasDibantuSebagian, RbAktifitasDibantuPenuh;
    private javax.swing.ButtonGroup bgAktifitas;
    private widget.RadioButton RbMobilisasiTirahBaring, RbMobilisasiDuduk, RbMobilisasiJalan;
    private javax.swing.ButtonGroup bgMobilisasi;
    private widget.CekBox CekDrainaseWsd, CekDrainaseDrainLuka, CekDrainaseStoma, CekDrainaseLain;
    private widget.TextBox TDrainaseLainKet;
    private widget.CekBox CekLukaTidakAda, CekLukaOperasi, CekLukaDekubitus, CekLukaSkinLost, CekLukaGangren, CekLukaLain;
    private widget.TextBox TLukaOperasiUkuran, TLukaOperasiLokasi, TLukaOperasiKondisi;
    private widget.TextBox TLukaDekubitusUkuran, TLukaDekubitusLokasi, TLukaDekubitusKondisi;
    private widget.TextBox TLukaSkinLostUkuran, TLukaSkinLostLokasi, TLukaSkinLostKondisi;
    private widget.TextBox TLukaGangrenUkuran, TLukaGangrenLokasi, TLukaGangrenKondisi;
    private widget.TextBox TLukaLainKet;
    private widget.TextArea HasilInvestigasiAbnormal;
    private widget.ScrollPane ScrollHasilInvestigasiAbnormal;

    private widget.CekBox CekTindakanResikoJatuh, CekTindakanPemasanganRestrain, CekTindakanPerawatanLuka, CekTindakanHygiene;
    private widget.ComboBox RiwayatAlergi;
    private widget.TextBox TJenisAlergi, TReaksiAlergi;
    private widget.CekBox CekAlatBantuTanpa, CekAlatBantuDengar, CekAlatBantuKacaMata, CekAlatBantuGigiPalsu,
        CekAlatBantuTongkatKruk, CekAlatBantuLain;
    private widget.TextBox TAlatBantuLainKet;
    private widget.TextArea TindakanYangSudahDilakukan;
    private widget.ScrollPane ScrollTindakanYangSudahDilakukan;
    private widget.CekBox CekPenunjangLaboratorium, CekPenunjangRadiologi, CekPenunjangCtScan, CekPenunjangLain;
    private widget.TextBox TPenunjangLaboratoriumKet, TPenunjangRadiologiKet, TPenunjangCtScanKet, TPenunjangLainKet;
    private widget.TextArea RencanaTindakLanjut, MasalahUtamaKeperawatan, DokumenYangDiserahterimakan;
    private widget.ScrollPane ScrollRencanaTindakLanjut, ScrollMasalahUtamaKeperawatan, ScrollDokumenYangDiserahterimakan;

    // [RM 04-1/04-2] Panel ringkasan (LoadHTML) + tombol cetak satu-record,
    // pola sama dgn form-form lain (RM 03-2/03-4-05-06) - LoadHTML sendiri
    // sudah ada sejak awal file ini (dulu cuma dipakai internal utk cetak
    // massal, tidak pernah dipasang ke container manapun). FormPhoto sekarang
    // dipecah jadi 2 tab (TabRingkasanTtd): tab 1 = ringkasan seluruh data
    // input (panelRingkasan, isi LoadHTML), tab 2 = grid foto/TTD
    // (FormPass3+Scroll5/LoadHTML2, direparent dari GEN block).
    private widget.ScrollPane ScrollHTML;
    private widget.Button BtnPrint1;
    private javax.swing.JTabbedPane TabRingkasanTtd;

    private widget.Label labelTransfer(String teks){
        widget.Label lbl = new widget.Label();
        lbl.setText(teks);
        return lbl;
    }

    /**
     * [RM 04-1/04-2] Item ComboBox Ruangan - membawa serta kd_kamar & kelas
     * (dari tabel kamar, JOIN bangsal) supaya pemilihan Ruangan otomatis
     * ikut mengisi Kelas, tanpa perlu ComboBox Kelas terpisah yg bisa
     * disetel manual tidak sinkron dgn Ruangan yg dipilih.
     */
    private static final class KamarItem {
        final String label, kdKamar, kelas;
        KamarItem(String label, String kdKamar, String kelas){ this.label=label; this.kdKamar=kdKamar; this.kelas=kelas; }
        @Override public String toString(){ return label; }
    }

    /**
     * [RM 04-1/04-2] Isi ComboBox Ruangan dari tabel `kamar` (JOIN `bangsal`
     * utk label yg enak dibaca) - menggantikan input manual free-text
     * AsalRuang/RuangSelanjutnya. Dipanggil sekali per combo saat
     * initTransferTambahan().
     */
    private void muatPilihanKamar(widget.ComboBox combo){
        javax.swing.DefaultComboBoxModel<KamarItem> model = new javax.swing.DefaultComboBoxModel<>();
        model.addElement(new KamarItem("- Pilih Ruangan -", "", ""));
        try {
            PreparedStatement psKamar = koneksi.prepareStatement(
                "select kamar.kd_kamar,kamar.kelas,bangsal.nm_bangsal from kamar "+
                "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
                "where kamar.statusdata='1' order by bangsal.nm_bangsal,kamar.kd_kamar");
            try {
                ResultSet rsKamar = psKamar.executeQuery();
                try {
                    while(rsKamar.next()){
                        String label = rsKamar.getString("nm_bangsal")+" ("+rsKamar.getString("kd_kamar")+")";
                        model.addElement(new KamarItem(label, rsKamar.getString("kd_kamar"), rsKamar.getString("kelas")));
                    }
                } finally { rsKamar.close(); }
            } finally { psKamar.close(); }
        } catch (Exception e) {
            System.out.println("Notif muatPilihanKamar : "+e);
        }
        combo.setModel(model);
    }

    private widget.ComboBox comboModelTransfer(int lebar, String... item){
        widget.ComboBox c = new widget.ComboBox();
        c.setPreferredSize(new java.awt.Dimension(lebar,23));
        c.setModel(new javax.swing.DefaultComboBoxModel(item));
        return c;
    }

    private widget.ComboBox comboYaTidak(){
        return comboModelTransfer(80,"","Ya","Tidak");
    }

    private widget.TextBox textBoxTransfer(int lebar){
        widget.TextBox t = new widget.TextBox();
        t.setPreferredSize(new java.awt.Dimension(lebar,23));
        return t;
    }

    private widget.PanelBiasa panelPilihanTransfer(javax.swing.ButtonGroup bg, widget.RadioButton... rb){
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,10,0));
        for(widget.RadioButton r : rb){ bg.add(r); p.add(r); }
        return p;
    }

    private widget.PanelBiasa panelChecklistTransfer(widget.CekBox... cek){
        // 2 kolom (bukan 3) - GridLayout(0,3) berpotensi memaksa lebar total
        // >840px pada checklist berlabel panjang & mengacaukan GridBagLayout
        // induk (pelajaran dari RMPenilaianAwalMedisIGD.panelChecklist()).
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new java.awt.GridLayout(0,2,10,2));
        for(widget.CekBox c : cek){ p.add(c); }
        return p;
    }

    private java.awt.GridBagConstraints transferConstraint(int gridx,int gridy){
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = gridx; c.gridy = gridy;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.insets = new java.awt.Insets(3,5,3,5);
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        return c;
    }

    private int tambahBarisTransferJudul(widget.PanelBiasa panel,int row,String judul){
        widget.Label lbl = new widget.Label();
        lbl.setText(judul);
        lbl.setFont(lbl.getFont().deriveFont(java.awt.Font.BOLD));
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        java.awt.GridBagConstraints c = transferConstraint(0,row);
        c.gridwidth = 2;
        panel.add(lbl,c);
        return row+1;
    }

    private int tambahBarisTransferField(widget.PanelBiasa panel,int row,String label,java.awt.Component field){
        widget.Label lbl = new widget.Label();
        lbl.setText(label+" :");
        java.awt.GridBagConstraints c1 = transferConstraint(0,row);
        panel.add(lbl,c1);
        java.awt.GridBagConstraints c2 = transferConstraint(1,row);
        panel.add(field,c2);
        return row+1;
    }

    private widget.PanelBiasa panelPickerDokter(widget.TextBox kd, widget.TextBox nm, widget.Button btn){
        kd.setPreferredSize(new java.awt.Dimension(70,23)); kd.setEditable(false);
        nm.setPreferredSize(new java.awt.Dimension(220,23)); nm.setEditable(false);
        btn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        btn.addActionListener((java.awt.event.ActionEvent evt) -> {
            DlgCariDokter dlg = new DlgCariDokter(null,false);
            dlg.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if(dlg.getTable().getSelectedRow()!= -1){
                        kd.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),0).toString());
                        nm.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),1).toString());
                    }
                    btn.requestFocus();
                }
            });
            dlg.isCek();
            dlg.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dlg.setLocationRelativeTo(internalFrame1);
            dlg.setVisible(true);
        });
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        p.add(kd); p.add(nm); p.add(btn);
        return p;
    }

    private widget.PanelBiasa panelLukaDetail(widget.CekBox cek, widget.TextBox ukuran, widget.TextBox lokasi, widget.TextBox kondisi){
        cek.setText("Ada");
        ukuran.setPreferredSize(new java.awt.Dimension(90,23));
        lokasi.setPreferredSize(new java.awt.Dimension(120,23));
        kondisi.setPreferredSize(new java.awt.Dimension(120,23));
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        p.add(cek);
        p.add(labelTransfer("Ukuran:")); p.add(ukuran);
        p.add(labelTransfer("Lokasi:")); p.add(lokasi);
        p.add(labelTransfer("Kondisi:")); p.add(kondisi);
        return p;
    }

    private widget.PanelBiasa panelCekKet(widget.CekBox cek, String teksCek, widget.TextBox ket, int lebarKet){
        cek.setText(teksCek);
        ket.setPreferredSize(new java.awt.Dimension(lebarKet,23));
        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,0));
        p.add(cek); p.add(ket);
        return p;
    }

    private widget.ScrollPane scrollTextAreaTransfer(widget.TextArea ta){
        ta.setColumns(20); ta.setRows(5);
        ta.setPreferredSize(new java.awt.Dimension(102,52));
        widget.ScrollPane sp = new widget.ScrollPane();
        sp.setViewportView(ta);
        sp.setPreferredSize(new java.awt.Dimension(260,63));
        return sp;
    }

    /**
     * [RM 04-1/04-2] Sembunyikan (bukan hapus) widget lama yg tidak ada
     * padanannya di formulir fisik RM 04-1/04-2 - datanya tetap ada di DB
     * (additive-only, lihat sik_transfer_pasien_antar_ruang_rm04.sql),
     * cuma tidak lagi ditampilkan/ditulis oleh form baru: 12 field vital
     * sebelum/sesudah transfer (formulir fisik cuma 1 observasi tunggal,
     * lihat panelTransferKondisiPasien), Peralatan Yang Menyertai
     * (redistribusi ke Oksigen/Nutrisi-NGT/Eliminasi BAK-Kateter), dan
     * Pemeriksaan Penunjang lama (digantikan pola 4-baris Lab/Radiologi/
     * CT Scan/Lain-lain di panelTransferPage2B).
     *
     * [Reorder sesuai foto] Ditambah 2 kelompok lagi: (a) label GEN block
     * lama yg jadi YATIM krn widget pasangannya direparent ke panel baru
     * di initTransferTambahan() (tambahBarisTransferField() bikin label
     * BARU sendiri dari teks yg dioper, jadi label GEN block lama harus
     * disembunyikan manual - pola sama RM 03-4/05/06); (b) AsalRuang/
     * RuangSelanjutnya (TextBox lama) DISEMBUNYIKAN TOTAL (bukan
     * direparent) krn perannya sudah digantikan penuh oleh ComboBox
     * PilihRuangAsal/PilihRuangTujuan di panelTransferKelasDokter - kedua
     * TextBox lama ini tetap ada di memori (dibutuhkan simpan/ganti/
     * getData()) & otomatis terisi lewat ItemListener, cuma tidak perlu
     * tampil dobel di layar.
     */
    private void pensiunkanFieldTransferLama(){
        jLabel40.setVisible(false);
        jLabel42.setVisible(false); KeadaanUmumSebelumTransfer.setVisible(false);
        jLabel17.setVisible(false); jLabel22.setVisible(false); NadiSebelumTransfer.setVisible(false);
        jLabel26.setVisible(false); jLabel25.setVisible(false); RRSebelumTransfer.setVisible(false);
        jLabel24.setVisible(false); jLabel27.setVisible(false); SuhuSebelumTransfer.setVisible(false);
        jLabel28.setVisible(false); jLabel23.setVisible(false); TDSebelumTransfer.setVisible(false);
        jLabel41.setVisible(false); scrollPane5.setVisible(false); KeluhanUtamaSebelumTransfer.setVisible(false);

        jLabel43.setVisible(false);
        jLabel44.setVisible(false); KeadaanUmumSetelahTransfer.setVisible(false);
        jLabel47.setVisible(false); jLabel48.setVisible(false); NadiSetelahTransfer.setVisible(false);
        jLabel49.setVisible(false); jLabel50.setVisible(false); RRSetelahTransfer.setVisible(false);
        jLabel51.setVisible(false); jLabel52.setVisible(false); SuhuSetelahTransfer.setVisible(false);
        jLabel29.setVisible(false); jLabel45.setVisible(false); TDSetelahTransfer.setVisible(false);
        jLabel46.setVisible(false); scrollPane6.setVisible(false); KeluhanUtamaSetelahTransfer.setVisible(false);

        jLabel36.setVisible(false); jLabel35.setVisible(false); jLabel37.setVisible(false);
        PeralatanMenyertai.setVisible(false); KeteranganPeralatan.setVisible(false);

        jLabel33.setVisible(false); scrollPane4.setVisible(false); PemeriksaanPenunjang.setVisible(false);

        // Asal/Tujuan Ruang lama - digantikan PilihRuangAsal/PilihRuangTujuan
        jLabel18.setVisible(false); jLabel20.setVisible(false); AsalRuang.setVisible(false);
        jLabel15.setVisible(false); RuangSelanjutnya.setVisible(false);

        // Label lama yg jadi yatim krn widget pasangannya direparent ke
        // panelTransferKeluargaTambahan/panelTransferAlasanCaraPindah/
        // panelTransferOksigenServahTerima/panelTransferPage2B (lihat
        // initTransferTambahan()). label11/label12 (Tanggal Masuk/Transfer)
        // & jLabel16/jLabel13 (Diagnosa)/jLabel30 (Diagnosa Sekunder)
        // SENGAJA TIDAK ikut disembunyikan - field pasangannya (TanggalMasuk/
        // TanggalPindah/DiagnosaUtama/scrollPane1) TETAP di header, tidak
        // direparent (permintaan eksplisit user).
        jLabel31.setVisible(false); jLabel32.setVisible(false);
        jLabel57.setVisible(false); jLabel14.setVisible(false);
        jLabel39.setVisible(false); jLabel38.setVisible(false); jLabel34.setVisible(false);

        // 8 garis pemisah horizontal GEN block (jSeparator1-7 @ y=70-610,
        // jSeparator14 @ y=861) dulu memisahkan baris-baris section lama -
        // sekarang tidak relevan lagi krn hampir semua kontennya sudah
        // direparent/disembunyikan (jSeparator1-7) atau posisinya (y=861)
        // sekarang jatuh di tengah tumpukan panel baru (jSeparator14),
        // disembunyikan semua supaya tidak jadi garis nyasar.
        jSeparator1.setVisible(false); jSeparator2.setVisible(false); jSeparator3.setVisible(false);
        jSeparator4.setVisible(false); jSeparator5.setVisible(false); jSeparator6.setVisible(false);
        jSeparator7.setVisible(false); jSeparator14.setVisible(false);

        // Blok Petugas/Perawat (label14/16/15+field, Menyerahkan & Menerima
        // dua-duanya) digeser naik dari y=610/630 ke HEADER (permintaan
        // eksplisit user) - ditempel tepat di bawah baris Tanggal Masuk/
        // Transfer (y=40, tinggi 23, berakhir y=63), SEBELUM Diagnosa
        // (y=120, masih cukup jarak: baris Petugas berakhir y=113).
        // .setLocation() dipakai (BUKAN edit .setBounds() di GEN block)
        // supaya GEN block tetap tidak disentuh sama sekali, pola sama dgn
        // reposisi widget lama di Triase/RM 03-2/03-4-05-06.
        label14.setLocation(label14.getX(), 70);
        label16.setLocation(label16.getX(), 90);
        KdPetugasMenyerahkan.setLocation(KdPetugasMenyerahkan.getX(), 90);
        NmPetugasMenyerahkan.setLocation(NmPetugasMenyerahkan.getX(), 90);
        BtnDokter.setLocation(BtnDokter.getX(), 90);
        label15.setLocation(label15.getX(), 90);
        KdPetugasMenerima.setLocation(KdPetugasMenerima.getX(), 90);
        NmPetugasMenerima.setLocation(NmPetugasMenerima.getX(), 90);
        BtnMenerima.setLocation(BtnMenerima.getX(), 90);
    }

    /**
     * [RM 04-1/04-2] Membangun & mengurutkan seluruh section form Transfer
     * Pasien Antar Ruangan mengikuti urutan PERSIS formulir fisik RM
     * 04-1/04-2 (permintaan user, dikirim foto formulir). Tanggal Masuk/
     * Tanggal Transfer & Diagnosa SENGAJA tetap di header (tidak ditumpuk
     * bareng 7 panel di bawah - permintaan eksplisit user setelah revisi
     * lanjutan), Diagnosa Utama sekaligus diubah dari input manual jadi
     * picker `laporan.DlgCariPenyakit` (master ICD-9/10). 7 panel
     * GridBagLayout urut: Keluarga, Asal Pasien/Pindah Ke, Alasan & Cara
     * Pindah, Kondisi Pasien, Therapy Oksigen + Data Serah Terima (Line
     * Invasif/Prosedur Pembedahan-Invasif/Kewaspadaan/Nutrisi/Eliminasi),
     * Aktifitas/Mobilisasi/Drainase/Integritas Kulit-Luka (Halaman 2A),
     * Tindakan-Kebutuhan Khusus/Alergi/Alat Bantu/Tindakan Sudah
     * Dilakukan/Pemeriksaan Penunjang/Therapy Diberikan/Rencana-Masalah-
     * Dokumen (Halaman 2B) - sebagian isinya widget BARU sesi ini,
     * sebagian lagi widget LAMA GEN block yg direparent ke sini
     * (panel.add(widgetLama,...) - Swing otomatis melepasnya dari
     * FormInput/null-layout) supaya urutannya persis mengikuti foto -
     * label GEN block lama yg jadi yatim karenanya disembunyikan di
     * pensiunkanFieldTransferLama() (dipanggil TEPAT SEBELUM method ini
     * dari konstruktor, urutan panggilan penting krn widget yg mau
     * direparent di sini harus dalam keadaan siap/tidak error dulu).
     */
    private void initTransferTambahan(){
        // Perluasan enum Cara Pindah, additive - nilai lama tetap ada di
        // model supaya data lama (Tempat Tidur/Brankar/Jalan Sendiri/-)
        // tidak kehilangan pilihan terpilihnya saat diedit.
        MetodePemindahan.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            "Jalan","Kursi Roda","Strecher","Lain-lain","Tempat Tidur","Brankar","Jalan Sendiri","-"
        }));

        // ===== Tanggal Masuk/Transfer & Diagnosa TETAP di header (permintaan
        // eksplisit user) - TIDAK direparent ke panel manapun, dibiarkan di
        // posisi GEN block asli (TanggalMasuk/TanggalPindah y=40, Diagnosa
        // Sekunder y=170 - tak disentuh sama sekali). Diagnosa Utama
        // dipersempit (750->700) supaya muat tombol cari ICD di sebelahnya,
        // dan diubah jadi read-only - user minta diagnosa diambil dari
        // master ICD-9/10 (tabel `penyakit`), bukan diketik manual. Pola
        // dialog+isi field disalin dari DlgRujuk.btnDiagnosaActionPerformed
        // (satu-satunya preseden "pilih ICD -> isi 1 TextBox 'kode - nama'"
        // di codebase ini, ditemukan lewat pencarian pemakaian
        // laporan.DlgCariPenyakit). =====
        DiagnosaUtama.setBounds(105,120,700,23);
        DiagnosaUtama.setEditable(false);
        BtnDiagnosaUtama = new widget.Button();
        BtnDiagnosaUtama.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnDiagnosaUtama.setToolTipText("Cari Diagnosa (ICD-9/10)");
        BtnDiagnosaUtama.setBounds(810,120,28,23);
        BtnDiagnosaUtama.addActionListener((java.awt.event.ActionEvent evt) -> {
            laporan.DlgCariPenyakit penyakit = new laporan.DlgCariPenyakit(null,false);
            penyakit.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    if(penyakit.getTable().getSelectedRow()!=-1){
                        DiagnosaUtama.setText(
                            penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),0).toString()+" - "+
                            penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(),1).toString());
                    }
                }
            });
            penyakit.isCek();
            penyakit.emptTeks();
            penyakit.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            penyakit.setLocationRelativeTo(internalFrame1);
            penyakit.setVisible(true);
        });
        FormInput.add(BtnDiagnosaUtama);

        int row = 0;

        // ===== Keluarga (Nama/Hubungan direparent dari GEN block, No.Telp
        // & Sudah Dijelaskan baru, Menyetujui Pemindahan lama ditaruh di
        // akhir - lihat Keputusan Teknis #5 di PROGRESS.md, sengaja belum
        // dihapus krn tak ada padanan eksplisit di foto) =====
        panelTransferKeluargaTambahan = new widget.PanelBiasa();
        panelTransferKeluargaTambahan.setLayout(new java.awt.GridBagLayout());
        row = 0;
        row = tambahBarisTransferJudul(panelTransferKeluargaTambahan, row, "KELUARGA");
        NamaMenyetujui.setPreferredSize(new java.awt.Dimension(230,23));
        row = tambahBarisTransferField(panelTransferKeluargaTambahan, row, "Nama", NamaMenyetujui);
        HubunganMenyetujui.setPreferredSize(new java.awt.Dimension(150,23));
        row = tambahBarisTransferField(panelTransferKeluargaTambahan, row, "Hubungan Keluarga", HubunganMenyetujui);
        TNoTlpMenyetujui = textBoxTransfer(180);
        row = tambahBarisTransferField(panelTransferKeluargaTambahan, row, "No.Telp", TNoTlpMenyetujui);
        SudahDijelaskanDiagnosa = comboYaTidak();
        row = tambahBarisTransferField(panelTransferKeluargaTambahan, row, "Sudah Dijelaskan Mengenai Diagnosa", SudahDijelaskanDiagnosa);
        MenyetujuiPemindahan.setPreferredSize(new java.awt.Dimension(100,23));
        row = tambahBarisTransferField(panelTransferKeluargaTambahan, row, "Menyetujui Pemindahan", MenyetujuiPemindahan);

        // ===== Kelas Ruangan & Dokter Yang Merawat (Asal/Tujuan) =====
        panelTransferKelasDokter = new widget.PanelBiasa();
        panelTransferKelasDokter.setLayout(new java.awt.GridBagLayout());
        row = 0;
        row = tambahBarisTransferJudul(panelTransferKelasDokter, row, "ASAL PASIEN");

        // -- Ruangan Asal: dipilih dari tabel kamar (JOIN bangsal), BUKAN
        // input manual. AsalRuang (widget GEN block lama) jadi read-only,
        // otomatis terisi label kamar terpilih; Kelas ikut otomatis dari
        // kamar.kelas - tidak ada lagi ComboBox Kelas yg bisa disetel manual
        // lepas dari Ruangan yg dipilih.
        AsalRuang.setEditable(false);
        PilihRuangAsal = new widget.ComboBox();
        PilihRuangAsal.setPreferredSize(new java.awt.Dimension(250,23));
        muatPilihanKamar(PilihRuangAsal);
        KelasAsalRuang = textBoxTransfer(150); KelasAsalRuang.setEditable(false);
        PilihRuangAsal.addItemListener((java.awt.event.ItemEvent e) -> {
            if(e.getStateChange()==java.awt.event.ItemEvent.SELECTED && PilihRuangAsal.getSelectedItem() instanceof KamarItem){
                KamarItem item = (KamarItem)PilihRuangAsal.getSelectedItem();
                AsalRuang.setText(item.kdKamar.isEmpty() ? "" : item.label);
                KelasAsalRuang.setText(item.kelas==null ? "" : item.kelas);
            }
        });
        row = tambahBarisTransferField(panelTransferKelasDokter, row, "Ruangan Asal", PilihRuangAsal);
        row = tambahBarisTransferField(panelTransferKelasDokter, row, "Kelas Ruang Asal", KelasAsalRuang);

        KdDokterMerawatAsal1 = new widget.TextBox(); NmDokterMerawatAsal1 = new widget.TextBox(); BtnDokterMerawatAsal1 = new widget.Button();
        row = tambahBarisTransferField(panelTransferKelasDokter, row, "Dokter Merawat Asal 1",
            panelPickerDokter(KdDokterMerawatAsal1, NmDokterMerawatAsal1, BtnDokterMerawatAsal1));
        KdDokterMerawatAsal2 = new widget.TextBox(); NmDokterMerawatAsal2 = new widget.TextBox(); BtnDokterMerawatAsal2 = new widget.Button();
        row = tambahBarisTransferField(panelTransferKelasDokter, row, "Dokter Merawat Asal 2",
            panelPickerDokter(KdDokterMerawatAsal2, NmDokterMerawatAsal2, BtnDokterMerawatAsal2));
        KdDokterMerawatAsal3 = new widget.TextBox(); NmDokterMerawatAsal3 = new widget.TextBox(); BtnDokterMerawatAsal3 = new widget.Button();
        row = tambahBarisTransferField(panelTransferKelasDokter, row, "Dokter Merawat Asal 3",
            panelPickerDokter(KdDokterMerawatAsal3, NmDokterMerawatAsal3, BtnDokterMerawatAsal3));

        row = tambahBarisTransferJudul(panelTransferKelasDokter, row, "PINDAH KE");

        // -- Ruangan Tujuan: pola sama persis dgn Ruangan Asal di atas --
        RuangSelanjutnya.setEditable(false);
        PilihRuangTujuan = new widget.ComboBox();
        PilihRuangTujuan.setPreferredSize(new java.awt.Dimension(250,23));
        muatPilihanKamar(PilihRuangTujuan);
        KelasRuangSelanjutnya = textBoxTransfer(150); KelasRuangSelanjutnya.setEditable(false);
        PilihRuangTujuan.addItemListener((java.awt.event.ItemEvent e) -> {
            if(e.getStateChange()==java.awt.event.ItemEvent.SELECTED && PilihRuangTujuan.getSelectedItem() instanceof KamarItem){
                KamarItem item = (KamarItem)PilihRuangTujuan.getSelectedItem();
                RuangSelanjutnya.setText(item.kdKamar.isEmpty() ? "" : item.label);
                KelasRuangSelanjutnya.setText(item.kelas==null ? "" : item.kelas);
            }
        });
        row = tambahBarisTransferField(panelTransferKelasDokter, row, "Ruangan Tujuan", PilihRuangTujuan);
        row = tambahBarisTransferField(panelTransferKelasDokter, row, "Kelas Ruang Tujuan", KelasRuangSelanjutnya);

        KdDokterMerawatTujuan1 = new widget.TextBox(); NmDokterMerawatTujuan1 = new widget.TextBox(); BtnDokterMerawatTujuan1 = new widget.Button();
        row = tambahBarisTransferField(panelTransferKelasDokter, row, "Dokter Merawat Tujuan 1",
            panelPickerDokter(KdDokterMerawatTujuan1, NmDokterMerawatTujuan1, BtnDokterMerawatTujuan1));
        KdDokterMerawatTujuan2 = new widget.TextBox(); NmDokterMerawatTujuan2 = new widget.TextBox(); BtnDokterMerawatTujuan2 = new widget.Button();
        row = tambahBarisTransferField(panelTransferKelasDokter, row, "Dokter Merawat Tujuan 2",
            panelPickerDokter(KdDokterMerawatTujuan2, NmDokterMerawatTujuan2, BtnDokterMerawatTujuan2));
        KdDokterMerawatTujuan3 = new widget.TextBox(); NmDokterMerawatTujuan3 = new widget.TextBox(); BtnDokterMerawatTujuan3 = new widget.Button();
        row = tambahBarisTransferField(panelTransferKelasDokter, row, "Dokter Merawat Tujuan 3",
            panelPickerDokter(KdDokterMerawatTujuan3, NmDokterMerawatTujuan3, BtnDokterMerawatTujuan3));

        // ===== Alasan & Cara Pindah (IndikasiPindah/KeteranganIndikasiPindahRuang/
        // MetodePemindahan - ketiganya widget lama GEN block, direparent ke
        // sini) =====
        panelTransferAlasanCaraPindah = new widget.PanelBiasa();
        panelTransferAlasanCaraPindah.setLayout(new java.awt.GridBagLayout());
        row = 0;
        row = tambahBarisTransferJudul(panelTransferAlasanCaraPindah, row, "ALASAN & CARA PINDAH");
        IndikasiPindah.setPreferredSize(new java.awt.Dimension(250,23));
        row = tambahBarisTransferField(panelTransferAlasanCaraPindah, row, "Alasan Pindah", IndikasiPindah);
        KeteranganIndikasiPindahRuang.setPreferredSize(new java.awt.Dimension(300,23));
        row = tambahBarisTransferField(panelTransferAlasanCaraPindah, row, "Keterangan Alasan Pindah", KeteranganIndikasiPindahRuang);
        MetodePemindahan.setPreferredSize(new java.awt.Dimension(200,23));
        row = tambahBarisTransferField(panelTransferAlasanCaraPindah, row, "Cara Pindah", MetodePemindahan);

        // ===== Kondisi Pasien (observasi tunggal, menggantikan 12 field
        // sebelum/sesudah transfer lama - lihat pensiunkanFieldTransferLama()) =====
        panelTransferKondisiPasien = new widget.PanelBiasa();
        panelTransferKondisiPasien.setLayout(new java.awt.GridBagLayout());
        row = 0;
        row = tambahBarisTransferJudul(panelTransferKondisiPasien, row, "KONDISI PASIEN (OBSERVASI TERAKHIR)");
        KesadaranTransfer = comboModelTransfer(150,"","Compos Mentis","Gelisah","Delirium","Koma");
        row = tambahBarisTransferField(panelTransferKondisiPasien, row, "Kesadaran", KesadaranTransfer);
        TDTransfer = textBoxTransfer(80);
        row = tambahBarisTransferField(panelTransferKondisiPasien, row, "TD", TDTransfer);
        HRTransfer = textBoxTransfer(80);
        row = tambahBarisTransferField(panelTransferKondisiPasien, row, "HR (Nadi)", HRTransfer);
        RRTransfer = textBoxTransfer(80);
        row = tambahBarisTransferField(panelTransferKondisiPasien, row, "RR", RRTransfer);
        SuhuTransfer = textBoxTransfer(80);
        row = tambahBarisTransferField(panelTransferKondisiPasien, row, "Suhu", SuhuTransfer);
        GCSTransfer = textBoxTransfer(80);
        row = tambahBarisTransferField(panelTransferKondisiPasien, row, "GCS", GCSTransfer);
        SpO2Transfer = textBoxTransfer(80);
        row = tambahBarisTransferField(panelTransferKondisiPasien, row, "SpO2", SpO2Transfer);
        TingkatNyeriTransfer = comboModelTransfer(80,"","0","1","2","3","4","5","6","7","8","9","10");
        row = tambahBarisTransferField(panelTransferKondisiPasien, row, "Tingkat Nyeri", TingkatNyeriTransfer);
        ObservasiTerakhirPukul = new widget.Tanggal();
        ObservasiTerakhirPukul.setPreferredSize(new java.awt.Dimension(180,23));
        ObservasiTerakhirPukul.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        ObservasiTerakhirPukul.setDate(new Date());
        row = tambahBarisTransferField(panelTransferKondisiPasien, row, "Observasi Terakhir Pukul", ObservasiTerakhirPukul);

        // ===== Therapy Oksigen + DATA SERAH TERIMA (Line Invasif,
        // Kewaspadaan, Nutrisi, Eliminasi BAB/BAK) =====
        panelTransferOksigenServahTerima = new widget.PanelBiasa();
        panelTransferOksigenServahTerima.setLayout(new java.awt.GridBagLayout());
        row = 0;
        row = tambahBarisTransferJudul(panelTransferOksigenServahTerima, row, "THERAPY OKSIGEN");
        OksigenStatus = comboYaTidak();
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Status Oksigen", OksigenStatus);
        CekOksigenNasalKanule = new widget.CekBox(); CekOksigenNasalKanule.setText("Nasal Kanule");
        CekOksigenNasalKateter = new widget.CekBox(); CekOksigenNasalKateter.setText("Nasal Kateter");
        CekOksigenFaceMask = new widget.CekBox(); CekOksigenFaceMask.setText("Face Mask");
        CekOksigenSungkupRebreathing = new widget.CekBox(); CekOksigenSungkupRebreathing.setText("Sungkup Rebreathing");
        CekOksigenSungkupNonRebreathing = new widget.CekBox(); CekOksigenSungkupNonRebreathing.setText("Sungkup Non Rebreathing");
        CekOksigenTracheostomy = new widget.CekBox(); CekOksigenTracheostomy.setText("Tracheostomy");
        CekOksigenEtt = new widget.CekBox(); CekOksigenEtt.setText("ETT");
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Jenis Oksigen", panelChecklistTransfer(
            CekOksigenNasalKanule, CekOksigenNasalKateter, CekOksigenFaceMask, CekOksigenSungkupRebreathing,
            CekOksigenSungkupNonRebreathing, CekOksigenTracheostomy, CekOksigenEtt));

        row = tambahBarisTransferJudul(panelTransferOksigenServahTerima, row, "DATA SERAH TERIMA");
        bgLineInvasif = new javax.swing.ButtonGroup();
        RbLineTidakAda = new widget.RadioButton(); RbLineTidakAda.setText("Tidak Ada Line Invasive");
        RbLineCvpPicc = new widget.RadioButton(); RbLineCvpPicc.setText("CVP/PICC");
        RbLineIvLine = new widget.RadioButton(); RbLineIvLine.setText("IV Line");
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Line Invasif",
            panelPilihanTransfer(bgLineInvasif, RbLineTidakAda, RbLineCvpPicc, RbLineIvLine));
        LineInvasifTanggal = new widget.Tanggal();
        LineInvasifTanggal.setPreferredSize(new java.awt.Dimension(180,23));
        LineInvasifTanggal.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        LineInvasifTanggal.setDate(new Date());
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Tanggal Line Invasif", LineInvasifTanggal);

        // ProsedurDilakukan (widget lama GEN block, label "Prosedur Yang
        // Sudah Dilakukan") direparent ke sini - persis posisinya di foto
        // fisik ("PROSEDUR PEMBEDAHAN/INVASIF YANG SUDAH DILAKUKAN"),
        // antara Line Invasif & Kewaspadaan/Precaution.
        scrollPane2.setPreferredSize(new java.awt.Dimension(400,50));
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Prosedur Pembedahan/Invasif", scrollPane2);

        CekKewaspadaanStandar = new widget.CekBox(); CekKewaspadaanStandar.setText("Standar");
        CekKewaspadaanKontak = new widget.CekBox(); CekKewaspadaanKontak.setText("Kontak");
        CekKewaspadaanAirborne = new widget.CekBox(); CekKewaspadaanAirborne.setText("Airborne");
        CekKewaspadaanDroplet = new widget.CekBox(); CekKewaspadaanDroplet.setText("Droplet");
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Kewaspadaan/Precaution", panelChecklistTransfer(
            CekKewaspadaanStandar, CekKewaspadaanKontak, CekKewaspadaanAirborne, CekKewaspadaanDroplet));

        CekNutrisiPuasa = new widget.CekBox(); CekNutrisiPuasa.setText("Puasa");
        CekNutrisiOral = new widget.CekBox(); CekNutrisiOral.setText("Oral");
        CekNutrisiNgt = new widget.CekBox(); CekNutrisiNgt.setText("NGT");
        CekNutrisiDietKhusus = new widget.CekBox(); CekNutrisiDietKhusus.setText("Diet Khusus");
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Nutrisi", panelChecklistTransfer(
            CekNutrisiPuasa, CekNutrisiOral, CekNutrisiNgt, CekNutrisiDietKhusus));
        TNutrisiBatasanCairanCc = textBoxTransfer(80);
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Batasan Cairan (Cc)", TNutrisiBatasanCairanCc);
        TNutrisiDietKhususJelaskan = textBoxTransfer(250);
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Diet Khusus, Jelaskan", TNutrisiDietKhususJelaskan);

        bgEliminasiBab = new javax.swing.ButtonGroup();
        RbEliminasiBabNormal = new widget.RadioButton(); RbEliminasiBabNormal.setText("Normal");
        RbEliminasiBabDiareKonstipasi = new widget.RadioButton(); RbEliminasiBabDiareKonstipasi.setText("Diare/Konstipasi");
        RbEliminasiBabIleustomyColostomy = new widget.RadioButton(); RbEliminasiBabIleustomyColostomy.setText("Ileustomy/Colostomy");
        RbEliminasiBabInkontinensiaAlvi = new widget.RadioButton(); RbEliminasiBabInkontinensiaAlvi.setText("Inkontinensia Alvi");
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Eliminasi BAB", panelPilihanTransfer(bgEliminasiBab,
            RbEliminasiBabNormal, RbEliminasiBabDiareKonstipasi, RbEliminasiBabIleustomyColostomy, RbEliminasiBabInkontinensiaAlvi));

        bgEliminasiBak = new javax.swing.ButtonGroup();
        RbEliminasiBakNormal = new widget.RadioButton(); RbEliminasiBakNormal.setText("Normal");
        RbEliminasiBakInkontinensiaUrine = new widget.RadioButton(); RbEliminasiBakInkontinensiaUrine.setText("Inkontinensia Urine");
        RbEliminasiBakKateterUrine = new widget.RadioButton(); RbEliminasiBakKateterUrine.setText("Kateter Urine");
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Eliminasi BAK", panelPilihanTransfer(bgEliminasiBak,
            RbEliminasiBakNormal, RbEliminasiBakInkontinensiaUrine, RbEliminasiBakKateterUrine));
        TEliminasiBakKateterNo = textBoxTransfer(100);
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "No. Kateter Urine", TEliminasiBakKateterNo);
        DTPEliminasiBakKateterTanggalPasang = new widget.Tanggal();
        DTPEliminasiBakKateterTanggalPasang.setPreferredSize(new java.awt.Dimension(180,23));
        DTPEliminasiBakKateterTanggalPasang.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        DTPEliminasiBakKateterTanggalPasang.setDate(new Date());
        row = tambahBarisTransferField(panelTransferOksigenServahTerima, row, "Tanggal Pasang Kateter", DTPEliminasiBakKateterTanggalPasang);

        // ===== Halaman 2 (RM 04-2), bagian A: Aktifitas/Mobilisasi/
        // Drainase/Integritas Kulit-Luka/Hasil Investigasi Abnormal =====
        panelTransferPage2A = new widget.PanelBiasa();
        panelTransferPage2A.setLayout(new java.awt.GridBagLayout());
        row = 0;
        row = tambahBarisTransferJudul(panelTransferPage2A, row, "AKTIFITAS & MOBILISASI");
        bgAktifitas = new javax.swing.ButtonGroup();
        RbAktifitasMandiri = new widget.RadioButton(); RbAktifitasMandiri.setText("Mandiri");
        RbAktifitasDibantuSebagian = new widget.RadioButton(); RbAktifitasDibantuSebagian.setText("Dibantu Sebagian");
        RbAktifitasDibantuPenuh = new widget.RadioButton(); RbAktifitasDibantuPenuh.setText("Dibantu Penuh");
        row = tambahBarisTransferField(panelTransferPage2A, row, "Aktifitas", panelPilihanTransfer(bgAktifitas,
            RbAktifitasMandiri, RbAktifitasDibantuSebagian, RbAktifitasDibantuPenuh));
        bgMobilisasi = new javax.swing.ButtonGroup();
        RbMobilisasiTirahBaring = new widget.RadioButton(); RbMobilisasiTirahBaring.setText("Tirah Baring");
        RbMobilisasiDuduk = new widget.RadioButton(); RbMobilisasiDuduk.setText("Duduk");
        RbMobilisasiJalan = new widget.RadioButton(); RbMobilisasiJalan.setText("Jalan");
        row = tambahBarisTransferField(panelTransferPage2A, row, "Mobilisasi", panelPilihanTransfer(bgMobilisasi,
            RbMobilisasiTirahBaring, RbMobilisasiDuduk, RbMobilisasiJalan));

        row = tambahBarisTransferJudul(panelTransferPage2A, row, "DRAINASE");
        CekDrainaseWsd = new widget.CekBox(); CekDrainaseWsd.setText("WSD");
        CekDrainaseDrainLuka = new widget.CekBox(); CekDrainaseDrainLuka.setText("Drain Luka");
        CekDrainaseStoma = new widget.CekBox(); CekDrainaseStoma.setText("Stoma");
        CekDrainaseLain = new widget.CekBox(); CekDrainaseLain.setText("Lain-lain");
        row = tambahBarisTransferField(panelTransferPage2A, row, "Drainase", panelChecklistTransfer(
            CekDrainaseWsd, CekDrainaseDrainLuka, CekDrainaseStoma, CekDrainaseLain));
        TDrainaseLainKet = textBoxTransfer(250);
        row = tambahBarisTransferField(panelTransferPage2A, row, "Drainase Lain-lain, Jelaskan", TDrainaseLainKet);

        row = tambahBarisTransferJudul(panelTransferPage2A, row, "INTEGRITAS KULIT/LUKA");
        CekLukaTidakAda = new widget.CekBox(); CekLukaTidakAda.setText("Tidak Ada Luka");
        row = tambahBarisTransferField(panelTransferPage2A, row, "", CekLukaTidakAda);
        CekLukaOperasi = new widget.CekBox();
        TLukaOperasiUkuran = new widget.TextBox(); TLukaOperasiLokasi = new widget.TextBox(); TLukaOperasiKondisi = new widget.TextBox();
        row = tambahBarisTransferField(panelTransferPage2A, row, "Luka Operasi",
            panelLukaDetail(CekLukaOperasi, TLukaOperasiUkuran, TLukaOperasiLokasi, TLukaOperasiKondisi));
        CekLukaDekubitus = new widget.CekBox();
        TLukaDekubitusUkuran = new widget.TextBox(); TLukaDekubitusLokasi = new widget.TextBox(); TLukaDekubitusKondisi = new widget.TextBox();
        row = tambahBarisTransferField(panelTransferPage2A, row, "Dekubitus",
            panelLukaDetail(CekLukaDekubitus, TLukaDekubitusUkuran, TLukaDekubitusLokasi, TLukaDekubitusKondisi));
        CekLukaSkinLost = new widget.CekBox();
        TLukaSkinLostUkuran = new widget.TextBox(); TLukaSkinLostLokasi = new widget.TextBox(); TLukaSkinLostKondisi = new widget.TextBox();
        row = tambahBarisTransferField(panelTransferPage2A, row, "Skin Lost",
            panelLukaDetail(CekLukaSkinLost, TLukaSkinLostUkuran, TLukaSkinLostLokasi, TLukaSkinLostKondisi));
        CekLukaGangren = new widget.CekBox();
        TLukaGangrenUkuran = new widget.TextBox(); TLukaGangrenLokasi = new widget.TextBox(); TLukaGangrenKondisi = new widget.TextBox();
        row = tambahBarisTransferField(panelTransferPage2A, row, "Gangren",
            panelLukaDetail(CekLukaGangren, TLukaGangrenUkuran, TLukaGangrenLokasi, TLukaGangrenKondisi));
        CekLukaLain = new widget.CekBox();
        TLukaLainKet = textBoxTransfer(250);
        row = tambahBarisTransferField(panelTransferPage2A, row, "Luka Lain-lain",
            panelCekKet(CekLukaLain, "Ada", TLukaLainKet, 250));

        HasilInvestigasiAbnormal = new widget.TextArea();
        ScrollHasilInvestigasiAbnormal = scrollTextAreaTransfer(HasilInvestigasiAbnormal);
        row = tambahBarisTransferField(panelTransferPage2A, row, "Hasil Investigasi Abnormal", ScrollHasilInvestigasiAbnormal);

        // ===== Halaman 2 (RM 04-2), bagian B: Tindakan/Kebutuhan Khusus,
        // Riwayat Alergi, Alat Bantu, Tindakan Yang Sudah Dilakukan,
        // Pemeriksaan Penunjang, Rencana/Masalah/Dokumen Diserahterimakan =====
        panelTransferPage2B = new widget.PanelBiasa();
        panelTransferPage2B.setLayout(new java.awt.GridBagLayout());
        row = 0;
        row = tambahBarisTransferJudul(panelTransferPage2B, row, "TINDAKAN/KEBUTUHAN KHUSUS & RIWAYAT ALERGI");
        CekTindakanResikoJatuh = new widget.CekBox(); CekTindakanResikoJatuh.setText("Resiko Jatuh");
        CekTindakanPemasanganRestrain = new widget.CekBox(); CekTindakanPemasanganRestrain.setText("Pemasangan Restrain");
        CekTindakanPerawatanLuka = new widget.CekBox(); CekTindakanPerawatanLuka.setText("Perawatan Luka");
        CekTindakanHygiene = new widget.CekBox(); CekTindakanHygiene.setText("Hygiene");
        row = tambahBarisTransferField(panelTransferPage2B, row, "Tindakan/Kebutuhan Khusus", panelChecklistTransfer(
            CekTindakanResikoJatuh, CekTindakanPemasanganRestrain, CekTindakanPerawatanLuka, CekTindakanHygiene));
        RiwayatAlergi = comboYaTidak();
        row = tambahBarisTransferField(panelTransferPage2B, row, "Riwayat Alergi", RiwayatAlergi);
        TJenisAlergi = textBoxTransfer(250);
        row = tambahBarisTransferField(panelTransferPage2B, row, "Jenis Alergi", TJenisAlergi);
        TReaksiAlergi = textBoxTransfer(250);
        row = tambahBarisTransferField(panelTransferPage2B, row, "Reaksi Alergi", TReaksiAlergi);

        row = tambahBarisTransferJudul(panelTransferPage2B, row, "ALAT BANTU YANG DIGUNAKAN");
        CekAlatBantuTanpa = new widget.CekBox(); CekAlatBantuTanpa.setText("Tanpa Alat Bantu");
        CekAlatBantuDengar = new widget.CekBox(); CekAlatBantuDengar.setText("Alat Bantu Dengar");
        CekAlatBantuKacaMata = new widget.CekBox(); CekAlatBantuKacaMata.setText("Kaca Mata");
        CekAlatBantuGigiPalsu = new widget.CekBox(); CekAlatBantuGigiPalsu.setText("Gigi Palsu");
        CekAlatBantuTongkatKruk = new widget.CekBox(); CekAlatBantuTongkatKruk.setText("Tongkat/Kruk");
        CekAlatBantuLain = new widget.CekBox(); CekAlatBantuLain.setText("Lain-lain");
        row = tambahBarisTransferField(panelTransferPage2B, row, "Alat Bantu", panelChecklistTransfer(
            CekAlatBantuTanpa, CekAlatBantuDengar, CekAlatBantuKacaMata, CekAlatBantuGigiPalsu, CekAlatBantuTongkatKruk, CekAlatBantuLain));
        TAlatBantuLainKet = textBoxTransfer(250);
        row = tambahBarisTransferField(panelTransferPage2B, row, "Alat Bantu Lain-lain, Jelaskan", TAlatBantuLainKet);

        row = tambahBarisTransferJudul(panelTransferPage2B, row, "TINDAKAN YANG SUDAH DILAKUKAN");
        TindakanYangSudahDilakukan = new widget.TextArea();
        ScrollTindakanYangSudahDilakukan = scrollTextAreaTransfer(TindakanYangSudahDilakukan);
        row = tambahBarisTransferField(panelTransferPage2B, row, "Tindakan Yang Sudah Dilakukan", ScrollTindakanYangSudahDilakukan);

        row = tambahBarisTransferJudul(panelTransferPage2B, row, "PEMERIKSAAN PENUNJANG YANG SUDAH DILAKUKAN");
        CekPenunjangLaboratorium = new widget.CekBox(); TPenunjangLaboratoriumKet = new widget.TextBox();
        row = tambahBarisTransferField(panelTransferPage2B, row, "", panelCekKet(CekPenunjangLaboratorium,"Laboratorium",TPenunjangLaboratoriumKet,220));
        CekPenunjangRadiologi = new widget.CekBox(); TPenunjangRadiologiKet = new widget.TextBox();
        row = tambahBarisTransferField(panelTransferPage2B, row, "", panelCekKet(CekPenunjangRadiologi,"Radiologi",TPenunjangRadiologiKet,220));
        CekPenunjangCtScan = new widget.CekBox(); TPenunjangCtScanKet = new widget.TextBox();
        row = tambahBarisTransferField(panelTransferPage2B, row, "", panelCekKet(CekPenunjangCtScan,"CT Scan",TPenunjangCtScanKet,220));
        CekPenunjangLain = new widget.CekBox(); TPenunjangLainKet = new widget.TextBox();
        row = tambahBarisTransferField(panelTransferPage2B, row, "", panelCekKet(CekPenunjangLain,"Lain-lain",TPenunjangLainKet,220));

        // ObatYangDiberikan (widget lama GEN block, label "Obat Yang Telah
        // Diberikan") direparent ke sini - padanan foto "THERAPY YANG
        // DIBERIKAN", tepat sebelum Rencana Tindak Lanjut.
        row = tambahBarisTransferJudul(panelTransferPage2B, row, "THERAPY YANG DIBERIKAN");
        scrollPane3.setPreferredSize(new java.awt.Dimension(400,60));
        row = tambahBarisTransferField(panelTransferPage2B, row, "Therapy Yang Diberikan", scrollPane3);

        row = tambahBarisTransferJudul(panelTransferPage2B, row, "RENCANA TINDAK LANJUT, MASALAH UTAMA KEPERAWATAN & DOKUMEN DISERAHTERIMAKAN");
        RencanaTindakLanjut = new widget.TextArea();
        ScrollRencanaTindakLanjut = scrollTextAreaTransfer(RencanaTindakLanjut);
        row = tambahBarisTransferField(panelTransferPage2B, row, "Rencana Tindak Lanjut", ScrollRencanaTindakLanjut);
        MasalahUtamaKeperawatan = new widget.TextArea();
        ScrollMasalahUtamaKeperawatan = scrollTextAreaTransfer(MasalahUtamaKeperawatan);
        row = tambahBarisTransferField(panelTransferPage2B, row, "Masalah Utama Keperawatan Saat Ini", ScrollMasalahUtamaKeperawatan);
        DokumenYangDiserahterimakan = new widget.TextArea();
        ScrollDokumenYangDiserahterimakan = scrollTextAreaTransfer(DokumenYangDiserahterimakan);
        row = tambahBarisTransferField(panelTransferPage2B, row, "Dokumen Yang Diserahterimakan", ScrollDokumenYangDiserahterimakan);

        // ===== Penempatan: ke-7 panel ditumpuk vertikal mengikuti urutan
        // fisik formulir RM 04-1/04-2 (Keluarga -> Asal Pasien/Pindah Ke ->
        // Alasan&Cara Pindah -> Kondisi Pasien -> Oksigen+Data Serah
        // Terima -> Halaman 2A -> Halaman 2B), ditempel mulai tepat di
        // bawah HEADER (Tanggal Masuk/Transfer y=40, blok Petugas/Perawat
        // y=70-90, Diagnosa y=120, Diagnosa Sekunder y=170-213 - semuanya
        // TETAP di header, TIDAK ikut ditumpuk di sini, lihat catatan di
        // kepala method ini & pensiunkanFieldTransferLama()). =====
        int yStack = 213 + 20;
        widget.PanelBiasa[] panelUrutan = {
            panelTransferKeluargaTambahan, panelTransferKelasDokter,
            panelTransferAlasanCaraPindah, panelTransferKondisiPasien,
            panelTransferOksigenServahTerima, panelTransferPage2A, panelTransferPage2B
        };
        for(widget.PanelBiasa p : panelUrutan){
            java.awt.Dimension ukuran = p.getPreferredSize();
            FormInput.add(p);
            p.setBounds(5, yStack, 840, ukuran.height);
            yStack += ukuran.height + 20;
        }
        FormInput.setPreferredSize(new java.awt.Dimension(870, yStack + 20));
    }

    /**
     * [RM 04-1/04-2] FormPhoto (accordion tab Data Transfer Pasien) dipecah
     * jadi 2 tab lewat TabRingkasanTtd: tab 1 "Data Inputan" = ringkasan
     * seluruh data input (LoadHTML, sudah ada sejak awal file ini tapi dulu
     * cuma dipakai internal utk fitur cetak massal - sekarang dipasang di
     * sini) + tombol cetak satu-record (BtnPrint1), pola sama dgn
     * `ScrollHTML`/`BtnPrint1` di RM 03-2/03-4-05-06. Tab 2 "TTD" = grid
     * 2x2 foto/tanda tangan (FormPass3+Scroll5/LoadHTML2) - direparent dari
     * FormPhoto (dipasang GEN block/initComponents() ke PAGE_END & CENTER)
     * ke panel tab tersendiri, supaya tidak numpuk vertikal dgn ringkasan.
     * Cetak satu-record (dialog "Lembar"/"PDF") DIPINDAH ke sini dari
     * tombol "Cetak" bar bawah, yg dikembalikan ke perilaku aslinya (cetak
     * massal, lihat BtnPrintActionPerformed).
     */
    private void initRingkasanTransfer(){
        BtnPrint1 = new widget.Button();
        BtnPrint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item (copy).png")));
        BtnPrint1.setToolTipText("Cetak Lembar Transfer Pasien Ini");
        BtnPrint1.addActionListener((java.awt.event.ActionEvent evt) -> BtnPrint1ActionPerformed(evt));

        widget.PanelBiasa panelToolbarRingkasan = new widget.PanelBiasa();
        panelToolbarRingkasan.setLayout(new java.awt.BorderLayout());
        panelToolbarRingkasan.add(labelTransfer("Ringkasan & Cetak Lembar :"), java.awt.BorderLayout.WEST);
        widget.PanelBiasa panelBtnPrint1 = new widget.PanelBiasa();
        panelBtnPrint1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT,5,2));
        panelBtnPrint1.add(BtnPrint1);
        panelToolbarRingkasan.add(panelBtnPrint1, java.awt.BorderLayout.EAST);

        ScrollHTML = new widget.ScrollPane();
        ScrollHTML.setViewportView(LoadHTML);
        ScrollHTML.setPreferredSize(new java.awt.Dimension(460,260));

        widget.PanelBiasa panelRingkasan = new widget.PanelBiasa();
        panelRingkasan.setLayout(new java.awt.BorderLayout());
        panelRingkasan.add(panelToolbarRingkasan, java.awt.BorderLayout.NORTH);
        panelRingkasan.add(ScrollHTML, java.awt.BorderLayout.CENTER);

        FormPhoto.remove(FormPass3);
        FormPhoto.remove(Scroll5);
        widget.PanelBiasa panelTtd = new widget.PanelBiasa();
        panelTtd.setLayout(new java.awt.BorderLayout());
        panelTtd.add(FormPass3, java.awt.BorderLayout.PAGE_END);
        panelTtd.add(Scroll5, java.awt.BorderLayout.CENTER);

        TabRingkasanTtd = new javax.swing.JTabbedPane();
        TabRingkasanTtd.addTab("Data Inputan", panelRingkasan);
        TabRingkasanTtd.addTab("TTD", panelTtd);

        FormPhoto.add(TabRingkasanTtd, java.awt.BorderLayout.CENTER);
    }

    private void BtnPrint1ActionPerformed(java.awt.event.ActionEvent evt){
        if(tbObat.getSelectedRow()==-1){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu..!!!!");
            return;
        }
        String modeCetak = (String)JOptionPane.showInputDialog(null,"Silahkan pilih mode cetak..!","Cetak Lembar Transfer Pasien Antar Ruang",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Lembar","PDF"},"Lembar");
        if(modeCetak==null){
            return;
        }
        cetakLaporanTransfer(modeCetak);
    }

    private String judulBarisTransfer(String judul){
        return "<tr><td colspan='2' bgcolor='#FFFAF8'><b>"+judul+"</b></td></tr>";
    }

    private String barisTransfer(String label, String value){
        return "<tr><td width='40%' valign='top'>"+label+"</td><td width='60%' valign='top'>"+((value==null||value.trim().isEmpty())?"-":value)+"</td></tr>";
    }

    /**
     * [RM 04-1/04-2] Ringkasan HTML seluruh field aktif, dibaca LANGSUNG
     * dari widget yg sudah terisi getData() (bukan query ulang terpisah) -
     * pola sama dgn bangunRingkasanHtml() di RM 03-2/03-4-05-06. Dipanggil
     * di baris terakhir getData().
     */
    private void bangunRingkasanHtml(){
        StringBuilder h = new StringBuilder();
        h.append("<html><body><table width='100%' cellpadding='2px' cellspacing='0' style='font-family:tahoma;font-size:8.5px;'>");

        h.append(judulBarisTransfer("PASIEN & PETUGAS"));
        h.append(barisTransfer("No.RM / Nama", TNoRM.getText()+" / "+TPasien.getText()));
        h.append(barisTransfer("Tgl.Lahir / J.K.", TglLahir.getText()+" / "+Jk.getText()));
        h.append(barisTransfer("Petugas Menyerahkan", KdPetugasMenyerahkan.getText()+" - "+NmPetugasMenyerahkan.getText()));
        h.append(barisTransfer("Petugas Menerima", KdPetugasMenerima.getText()+" - "+NmPetugasMenerima.getText()));

        h.append(judulBarisTransfer("KELUARGA"));
        h.append(barisTransfer("Nama", NamaMenyetujui.getText()));
        h.append(barisTransfer("Hubungan", HubunganMenyetujui.getSelectedItem()+""));
        h.append(barisTransfer("No.Telp", TNoTlpMenyetujui.getText()));
        h.append(barisTransfer("Sudah Dijelaskan Diagnosa", SudahDijelaskanDiagnosa.getSelectedItem()+""));
        h.append(barisTransfer("Menyetujui Pemindahan", MenyetujuiPemindahan.getSelectedItem()+""));

        h.append(judulBarisTransfer("ASAL PASIEN"));
        h.append(barisTransfer("Ruangan / Kelas", AsalRuang.getText()+" / "+KelasAsalRuang.getText()));
        h.append(barisTransfer("Dokter Merawat", NmDokterMerawatAsal1.getText()+
            (DokterMerawatAsal2NonNull()?"; "+NmDokterMerawatAsal2.getText():"")+
            (DokterMerawatAsal3NonNull()?"; "+NmDokterMerawatAsal3.getText():"")));

        h.append(judulBarisTransfer("PINDAH KE"));
        h.append(barisTransfer("Ruangan / Kelas", RuangSelanjutnya.getText()+" / "+KelasRuangSelanjutnya.getText()));
        h.append(barisTransfer("Dokter Merawat", NmDokterMerawatTujuan1.getText()+
            (DokterMerawatTujuan2NonNull()?"; "+NmDokterMerawatTujuan2.getText():"")+
            (DokterMerawatTujuan3NonNull()?"; "+NmDokterMerawatTujuan3.getText():"")));

        h.append(judulBarisTransfer("ALASAN & CARA PINDAH"));
        h.append(barisTransfer("Alasan Pindah", IndikasiPindah.getSelectedItem()+" - "+KeteranganIndikasiPindahRuang.getText()));
        h.append(barisTransfer("Cara Pindah", MetodePemindahan.getSelectedItem()+""));
        h.append(barisTransfer("Diagnosa", DiagnosaUtama.getText()+(DiagnosaSekunder.getText().trim().isEmpty()?"":" / "+DiagnosaSekunder.getText())));
        h.append(barisTransfer("Prosedur Pembedahan/Invasif", ProsedurDilakukan.getText()));

        h.append(judulBarisTransfer("KONDISI PASIEN"));
        h.append(barisTransfer("Observasi Terakhir Pukul", ObservasiTerakhirPukul.getSelectedItem()+""));
        h.append(barisTransfer("Kesadaran / GCS", KesadaranTransfer.getSelectedItem()+" / "+GCSTransfer.getText()));
        h.append(barisTransfer("TD / HR / RR / Suhu", TDTransfer.getText()+" / "+HRTransfer.getText()+" / "+RRTransfer.getText()+" / "+SuhuTransfer.getText()));
        h.append(barisTransfer("SpO2 / Tingkat Nyeri", SpO2Transfer.getText()+" / "+TingkatNyeriTransfer.getSelectedItem()));
        h.append(barisTransfer("Therapy Oksigen", "Ya".equals(OksigenStatus.getSelectedItem()+"") ?
            cekListTransfer(CekOksigenNasalKanule,"Nasal Kanule")+cekListTransfer(CekOksigenNasalKateter,"Nasal Kateter")+
            cekListTransfer(CekOksigenFaceMask,"Face Mask")+cekListTransfer(CekOksigenSungkupRebreathing,"Sungkup Rebreathing")+
            cekListTransfer(CekOksigenSungkupNonRebreathing,"Sungkup Non Rebreathing")+cekListTransfer(CekOksigenTracheostomy,"Tracheostomy")+
            cekListTransfer(CekOksigenEtt,"ETT") : "Tidak"));

        h.append(judulBarisTransfer("DATA SERAH TERIMA"));
        String lineInvasifRingkasan = RbLineTidakAda.isSelected()?"Tidak Ada Line Invasive":(RbLineCvpPicc.isSelected()?"CVP/PICC":(RbLineIvLine.isSelected()?"IV Line":"-"));
        h.append(barisTransfer("Line Invasif", lineInvasifRingkasan));
        h.append(barisTransfer("Kewaspadaan", cekListTransfer(CekKewaspadaanStandar,"Standar")+cekListTransfer(CekKewaspadaanKontak,"Kontak")+
            cekListTransfer(CekKewaspadaanAirborne,"Airborne")+cekListTransfer(CekKewaspadaanDroplet,"Droplet")));
        h.append(barisTransfer("Nutrisi", cekListTransfer(CekNutrisiPuasa,"Puasa")+cekListTransfer(CekNutrisiOral,"Oral")+cekListTransfer(CekNutrisiNgt,"NGT")));
        String eliminasiBabRingkasan = RbEliminasiBabNormal.isSelected()?"Normal":(RbEliminasiBabDiareKonstipasi.isSelected()?"Diare/Konstipasi":
            (RbEliminasiBabIleustomyColostomy.isSelected()?"Ileustomy/Colostomy":(RbEliminasiBabInkontinensiaAlvi.isSelected()?"Inkontinensia Alvi":"-")));
        h.append(barisTransfer("Eliminasi BAB", eliminasiBabRingkasan));
        String eliminasiBakRingkasan = RbEliminasiBakNormal.isSelected()?"Normal":(RbEliminasiBakInkontinensiaUrine.isSelected()?"Inkontinensia Urine":
            (RbEliminasiBakKateterUrine.isSelected()?"Kateter Urine No."+TEliminasiBakKateterNo.getText():"-"));
        h.append(barisTransfer("Eliminasi BAK", eliminasiBakRingkasan));

        h.append(judulBarisTransfer("HALAMAN 2 (RM 04-2)"));
        String aktifitasRingkasan = RbAktifitasMandiri.isSelected()?"Mandiri":(RbAktifitasDibantuSebagian.isSelected()?"Dibantu Sebagian":(RbAktifitasDibantuPenuh.isSelected()?"Dibantu Penuh":"-"));
        h.append(barisTransfer("Aktifitas", aktifitasRingkasan));
        String mobilisasiRingkasan = RbMobilisasiTirahBaring.isSelected()?"Tirah Baring":(RbMobilisasiDuduk.isSelected()?"Duduk":(RbMobilisasiJalan.isSelected()?"Jalan":"-"));
        h.append(barisTransfer("Mobilisasi", mobilisasiRingkasan));
        h.append(barisTransfer("Drainase", cekListTransfer(CekDrainaseWsd,"WSD")+cekListTransfer(CekDrainaseDrainLuka,"Drain Luka")+
            cekListTransfer(CekDrainaseStoma,"Stoma")+cekListTransfer(CekDrainaseLain,"Lain-lain")));
        h.append(barisTransfer("Integritas Kulit/Luka", CekLukaTidakAda.isSelected()?"Tidak Ada Luka":
            cekListTransfer(CekLukaOperasi,"Luka Operasi")+cekListTransfer(CekLukaDekubitus,"Dekubitus")+
            cekListTransfer(CekLukaSkinLost,"Skin Lost")+cekListTransfer(CekLukaGangren,"Gangren")+cekListTransfer(CekLukaLain,"Lain-lain")));
        h.append(barisTransfer("Hasil Investigasi Abnormal", HasilInvestigasiAbnormal.getText()));
        h.append(barisTransfer("Tindakan/Kebutuhan Khusus", cekListTransfer(CekTindakanResikoJatuh,"Resiko Jatuh")+
            cekListTransfer(CekTindakanPemasanganRestrain,"Pemasangan Restrain")+cekListTransfer(CekTindakanPerawatanLuka,"Perawatan Luka")+
            cekListTransfer(CekTindakanHygiene,"Hygiene")));
        h.append(barisTransfer("Riwayat Alergi", "Ya".equals(RiwayatAlergi.getSelectedItem()+"") ? "Ya - "+TJenisAlergi.getText()+" / "+TReaksiAlergi.getText() : "Tidak"));
        h.append(barisTransfer("Alat Bantu", cekListTransfer(CekAlatBantuTanpa,"Tanpa Alat Bantu")+cekListTransfer(CekAlatBantuDengar,"Alat Bantu Dengar")+
            cekListTransfer(CekAlatBantuKacaMata,"Kaca Mata")+cekListTransfer(CekAlatBantuGigiPalsu,"Gigi Palsu")+
            cekListTransfer(CekAlatBantuTongkatKruk,"Tongkat/Kruk")+cekListTransfer(CekAlatBantuLain,"Lain-lain")));
        h.append(barisTransfer("Tindakan Yang Sudah Dilakukan", TindakanYangSudahDilakukan.getText()));
        h.append(barisTransfer("Pemeriksaan Penunjang", cekListTransfer(CekPenunjangLaboratorium,"Laboratorium")+cekListTransfer(CekPenunjangRadiologi,"Radiologi")+
            cekListTransfer(CekPenunjangCtScan,"CT Scan")+cekListTransfer(CekPenunjangLain,"Lain-lain")));
        h.append(barisTransfer("Therapy Yang Diberikan", ObatYangDiberikan.getText()));
        h.append(barisTransfer("Rencana Tindak Lanjut", RencanaTindakLanjut.getText()));
        h.append(barisTransfer("Masalah Utama Keperawatan", MasalahUtamaKeperawatan.getText()));
        h.append(barisTransfer("Dokumen Diserahterimakan", DokumenYangDiserahterimakan.getText()));

        h.append("</table></body></html>");
        LoadHTML.setText(h.toString());
        // JEditorPane scroll ke bawah dokumen setelah setText() (kuirk Swing
        // baku) - paksa kembali ke atas, sama pola RMPenilaianAwalKeperawatanIGD.
        LoadHTML.setCaretPosition(0);
    }

    private boolean DokterMerawatAsal2NonNull(){ return !NmDokterMerawatAsal2.getText().trim().isEmpty(); }
    private boolean DokterMerawatAsal3NonNull(){ return !NmDokterMerawatAsal3.getText().trim().isEmpty(); }
    private boolean DokterMerawatTujuan2NonNull(){ return !NmDokterMerawatTujuan2.getText().trim().isEmpty(); }
    private boolean DokterMerawatTujuan3NonNull(){ return !NmDokterMerawatTujuan3.getText().trim().isEmpty(); }

    private String cekListTransfer(javax.swing.AbstractButton c, String label){
        return c.isSelected() ? "["+label+"] " : "";
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            if(TCari.getText().trim().equals("")){
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,"+
                        "transfer_pasien_antar_ruang.tanggal_masuk,transfer_pasien_antar_ruang.tanggal_pindah,transfer_pasien_antar_ruang.diagnosa_utama,"+
                        "transfer_pasien_antar_ruang.nip_menyerahkan,petugasmenyerahkan.nama as petugasmenyerahkan,transfer_pasien_antar_ruang.nip_menerima,"+
                        "petugasmenerima.nama as petugasmenerima "+
                        "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "+
                        "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                        "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip where "+
                        "transfer_pasien_antar_ruang.tanggal_pindah between ? and ? order by transfer_pasien_antar_ruang.tanggal_pindah");
            }else{
                ps=koneksi.prepareStatement(
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,"+
                        "transfer_pasien_antar_ruang.tanggal_masuk,transfer_pasien_antar_ruang.tanggal_pindah,transfer_pasien_antar_ruang.diagnosa_utama,"+
                        "transfer_pasien_antar_ruang.nip_menyerahkan,petugasmenyerahkan.nama as petugasmenyerahkan,transfer_pasien_antar_ruang.nip_menerima,"+
                        "petugasmenerima.nama as petugasmenerima "+
                        "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                        "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "+
                        "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                        "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip where "+
                        "transfer_pasien_antar_ruang.tanggal_pindah between ? and ? and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "+
                        "transfer_pasien_antar_ruang.nip_menyerahkan like ? or petugasmenyerahkan.nama like ?) order by transfer_pasien_antar_ruang.tanggal_pindah");
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
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getDate("tgl_lahir"),rs.getString("jk"),rs.getString("tanggal_masuk"),
                        rs.getString("tanggal_pindah"),rs.getString("diagnosa_utama"),
                        rs.getString("nip_menyerahkan"),rs.getString("petugasmenyerahkan"),rs.getString("nip_menerima"),rs.getString("petugasmenerima")
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
        TanggalMasuk.setDate(new Date());
        TanggalPindah.setDate(new Date());
        IndikasiPindah.setSelectedIndex(0);
        KeteranganIndikasiPindahRuang.setText("");
        AsalRuang.setText("");
        RuangSelanjutnya.setText("");
        MetodePemindahan.setSelectedIndex(0);
        DiagnosaUtama.setText("");
        DiagnosaSekunder.setText("");
        ProsedurDilakukan.setText("");
        ObatYangDiberikan.setText("");
        MenyetujuiPemindahan.setSelectedIndex(0);
        NamaMenyetujui.setText("");
        HubunganMenyetujui.setSelectedIndex(0);

        // [RM 04-1/04-2] reset field baru
        TNoTlpMenyetujui.setText(""); SudahDijelaskanDiagnosa.setSelectedIndex(0);
        PilihRuangAsal.setSelectedIndex(0); AsalRuang.setText(""); KelasAsalRuang.setText("");
        PilihRuangTujuan.setSelectedIndex(0); RuangSelanjutnya.setText(""); KelasRuangSelanjutnya.setText("");
        KdDokterMerawatAsal1.setText(""); NmDokterMerawatAsal1.setText("");
        KdDokterMerawatAsal2.setText(""); NmDokterMerawatAsal2.setText("");
        KdDokterMerawatAsal3.setText(""); NmDokterMerawatAsal3.setText("");
        KdDokterMerawatTujuan1.setText(""); NmDokterMerawatTujuan1.setText("");
        KdDokterMerawatTujuan2.setText(""); NmDokterMerawatTujuan2.setText("");
        KdDokterMerawatTujuan3.setText(""); NmDokterMerawatTujuan3.setText("");
        KesadaranTransfer.setSelectedIndex(0);
        TDTransfer.setText(""); HRTransfer.setText(""); RRTransfer.setText(""); SuhuTransfer.setText(""); GCSTransfer.setText(""); SpO2Transfer.setText("");
        TingkatNyeriTransfer.setSelectedIndex(0);
        ObservasiTerakhirPukul.setDate(new Date());
        OksigenStatus.setSelectedIndex(0);
        CekOksigenNasalKanule.setSelected(false); CekOksigenNasalKateter.setSelected(false); CekOksigenFaceMask.setSelected(false);
        CekOksigenSungkupRebreathing.setSelected(false); CekOksigenSungkupNonRebreathing.setSelected(false);
        CekOksigenTracheostomy.setSelected(false); CekOksigenEtt.setSelected(false);
        bgLineInvasif.clearSelection();
        LineInvasifTanggal.setDate(new Date());
        CekKewaspadaanStandar.setSelected(false); CekKewaspadaanKontak.setSelected(false); CekKewaspadaanAirborne.setSelected(false); CekKewaspadaanDroplet.setSelected(false);
        CekNutrisiPuasa.setSelected(false); CekNutrisiOral.setSelected(false); CekNutrisiNgt.setSelected(false); CekNutrisiDietKhusus.setSelected(false);
        TNutrisiBatasanCairanCc.setText(""); TNutrisiDietKhususJelaskan.setText("");
        bgEliminasiBab.clearSelection(); bgEliminasiBak.clearSelection();
        TEliminasiBakKateterNo.setText(""); DTPEliminasiBakKateterTanggalPasang.setDate(new Date());
        bgAktifitas.clearSelection(); bgMobilisasi.clearSelection();
        CekDrainaseWsd.setSelected(false); CekDrainaseDrainLuka.setSelected(false); CekDrainaseStoma.setSelected(false); CekDrainaseLain.setSelected(false);
        TDrainaseLainKet.setText("");
        CekLukaTidakAda.setSelected(false);
        CekLukaOperasi.setSelected(false); TLukaOperasiUkuran.setText(""); TLukaOperasiLokasi.setText(""); TLukaOperasiKondisi.setText("");
        CekLukaDekubitus.setSelected(false); TLukaDekubitusUkuran.setText(""); TLukaDekubitusLokasi.setText(""); TLukaDekubitusKondisi.setText("");
        CekLukaSkinLost.setSelected(false); TLukaSkinLostUkuran.setText(""); TLukaSkinLostLokasi.setText(""); TLukaSkinLostKondisi.setText("");
        CekLukaGangren.setSelected(false); TLukaGangrenUkuran.setText(""); TLukaGangrenLokasi.setText(""); TLukaGangrenKondisi.setText("");
        CekLukaLain.setSelected(false); TLukaLainKet.setText("");
        HasilInvestigasiAbnormal.setText("");
        CekTindakanResikoJatuh.setSelected(false); CekTindakanPemasanganRestrain.setSelected(false);
        CekTindakanPerawatanLuka.setSelected(false); CekTindakanHygiene.setSelected(false);
        RiwayatAlergi.setSelectedIndex(0); TJenisAlergi.setText(""); TReaksiAlergi.setText("");
        CekAlatBantuTanpa.setSelected(false); CekAlatBantuDengar.setSelected(false); CekAlatBantuKacaMata.setSelected(false);
        CekAlatBantuGigiPalsu.setSelected(false); CekAlatBantuTongkatKruk.setSelected(false); CekAlatBantuLain.setSelected(false);
        TAlatBantuLainKet.setText("");
        TindakanYangSudahDilakukan.setText("");
        CekPenunjangLaboratorium.setSelected(false); TPenunjangLaboratoriumKet.setText("");
        CekPenunjangRadiologi.setSelected(false); TPenunjangRadiologiKet.setText("");
        CekPenunjangCtScan.setSelected(false); TPenunjangCtScanKet.setText("");
        CekPenunjangLain.setSelected(false); TPenunjangLainKet.setText("");
        RencanaTindakLanjut.setText(""); MasalahUtamaKeperawatan.setText(""); DokumenYangDiserahterimakan.setText("");

        TabRawat.setSelectedIndex(0);
        IndikasiPindah.requestFocus();
    }

    private String nz(String s){
        return s==null ? "" : s;
    }

    private void setChk(javax.swing.AbstractButton c, String val){
        c.setSelected("Ya".equals(val));
    }

    /**
     * [RM 04-1/04-2] Query-ulang-DB per klik/Space (BUKAN lagi baca langsung
     * dari tbObat seperti sebelumnya) - tabMode sudah diringkas jadi 12
     * kolom pencarian saja (lihat konstruktor), tidak lagi bisa menampung
     * ~130 kolom aktif form baru. Pola sama seperti getData() di
     * RMPenilaianAwalMedisIGD.java (RM 03-2).
     */
    private void getData() {
        if(tbObat.getSelectedRow()== -1){ return; }
        String noRawatPilih = tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();
        String tanggalMasukPilih = tbObat.getValueAt(tbObat.getSelectedRow(),5).toString();
        try {
            ps = koneksi.prepareStatement(
                "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,"+
                "transfer_pasien_antar_ruang.*,"+
                "petugasmenyerahkan.nama as nm_menyerahkan,petugasmenerima.nama as nm_menerima,"+
                "dokterasal1.nm_dokter as nm_dokter_asal_1,dokterasal2.nm_dokter as nm_dokter_asal_2,dokterasal3.nm_dokter as nm_dokter_asal_3,"+
                "doktertujuan1.nm_dokter as nm_dokter_tujuan_1,doktertujuan2.nm_dokter as nm_dokter_tujuan_2,doktertujuan3.nm_dokter as nm_dokter_tujuan_3 "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "+
                "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip "+
                "left join dokter as dokterasal1 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_1=dokterasal1.kd_dokter "+
                "left join dokter as dokterasal2 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_2=dokterasal2.kd_dokter "+
                "left join dokter as dokterasal3 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_3=dokterasal3.kd_dokter "+
                "left join dokter as doktertujuan1 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_1=doktertujuan1.kd_dokter "+
                "left join dokter as doktertujuan2 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_2=doktertujuan2.kd_dokter "+
                "left join dokter as doktertujuan3 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_3=doktertujuan3.kd_dokter "+
                "where transfer_pasien_antar_ruang.no_rawat=? and transfer_pasien_antar_ruang.tanggal_masuk=?");
            try {
                ps.setString(1,noRawatPilih);
                ps.setString(2,tanggalMasukPilih);
                rs = ps.executeQuery();
                if(rs.next()){
                    TNoRw.setText(rs.getString("no_rawat"));
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    Jk.setText(rs.getString("jk"));
                    Valid.SetTgl2(TanggalMasuk, rs.getString("tanggal_masuk"));
                    Valid.SetTgl2(TanggalPindah, rs.getString("tanggal_pindah"));
                    IndikasiPindah.setSelectedItem(nz(rs.getString("indikasi_pindah_ruang")));
                    KeteranganIndikasiPindahRuang.setText(nz(rs.getString("keterangan_indikasi_pindah_ruang")));
                    AsalRuang.setText(nz(rs.getString("asal_ruang")));
                    RuangSelanjutnya.setText(nz(rs.getString("ruang_selanjutnya")));
                    MetodePemindahan.setSelectedItem(nz(rs.getString("metode_pemindahan_pasien")));
                    DiagnosaUtama.setText(nz(rs.getString("diagnosa_utama")));
                    DiagnosaSekunder.setText(nz(rs.getString("diagnosa_sekunder")));
                    ProsedurDilakukan.setText(nz(rs.getString("prosedur_yang_sudah_dilakukan")));
                    ObatYangDiberikan.setText(nz(rs.getString("obat_yang_telah_diberikan")));
                    MenyetujuiPemindahan.setSelectedItem(nz(rs.getString("pasien_keluarga_menyetujui")));
                    NamaMenyetujui.setText(nz(rs.getString("nama_menyetujui")));
                    HubunganMenyetujui.setSelectedItem(nz(rs.getString("hubungan_menyetujui")));
                    KdPetugasMenyerahkan.setText(rs.getString("nip_menyerahkan"));
                    NmPetugasMenyerahkan.setText(rs.getString("nm_menyerahkan"));
                    KdPetugasMenerima.setText(rs.getString("nip_menerima"));
                    NmPetugasMenerima.setText(rs.getString("nm_menerima"));

                    // -- [RM 04-1/04-2] field baru --
                    TNoTlpMenyetujui.setText(nz(rs.getString("no_tlp_menyetujui")));
                    SudahDijelaskanDiagnosa.setSelectedItem(nz(rs.getString("sudah_dijelaskan_diagnosa")));
                    // Ruangan/Kelas: PilihRuangAsal/Tujuan sengaja TIDAK dicoba
                    // di-restore ke pilihan lama (data lama disimpan sbg label
                    // teks bebas, tidak selalu bisa dicocokkan balik ke 1 baris
                    // combo dgn pasti) - dibiarkan di "- Pilih Ruangan -" spy
                    // user pilih ulang kalau mau ganti; AsalRuang/KelasAsalRuang
                    // (read-only) tetap menampilkan nilai TERSIMPAN apa adanya.
                    PilihRuangAsal.setSelectedIndex(0);
                    PilihRuangTujuan.setSelectedIndex(0);
                    KelasAsalRuang.setText(nz(rs.getString("kelas_asal_ruang")));
                    KelasRuangSelanjutnya.setText(nz(rs.getString("kelas_ruang_selanjutnya")));
                    KdDokterMerawatAsal1.setText(nz(rs.getString("kd_dokter_merawat_asal_1")));
                    NmDokterMerawatAsal1.setText(nz(rs.getString("nm_dokter_asal_1")));
                    KdDokterMerawatAsal2.setText(nz(rs.getString("kd_dokter_merawat_asal_2")));
                    NmDokterMerawatAsal2.setText(nz(rs.getString("nm_dokter_asal_2")));
                    KdDokterMerawatAsal3.setText(nz(rs.getString("kd_dokter_merawat_asal_3")));
                    NmDokterMerawatAsal3.setText(nz(rs.getString("nm_dokter_asal_3")));
                    KdDokterMerawatTujuan1.setText(nz(rs.getString("kd_dokter_merawat_tujuan_1")));
                    NmDokterMerawatTujuan1.setText(nz(rs.getString("nm_dokter_tujuan_1")));
                    KdDokterMerawatTujuan2.setText(nz(rs.getString("kd_dokter_merawat_tujuan_2")));
                    NmDokterMerawatTujuan2.setText(nz(rs.getString("nm_dokter_tujuan_2")));
                    KdDokterMerawatTujuan3.setText(nz(rs.getString("kd_dokter_merawat_tujuan_3")));
                    NmDokterMerawatTujuan3.setText(nz(rs.getString("nm_dokter_tujuan_3")));

                    KesadaranTransfer.setSelectedItem(nz(rs.getString("kesadaran_transfer")));
                    TDTransfer.setText(nz(rs.getString("td_transfer")));
                    HRTransfer.setText(nz(rs.getString("hr_transfer")));
                    RRTransfer.setText(nz(rs.getString("rr_transfer")));
                    SuhuTransfer.setText(nz(rs.getString("suhu_transfer")));
                    GCSTransfer.setText(nz(rs.getString("gcs_transfer")));
                    SpO2Transfer.setText(nz(rs.getString("spo2_transfer")));
                    TingkatNyeriTransfer.setSelectedItem(nz(rs.getString("tingkat_nyeri_transfer")));
                    if(rs.getString("observasi_terakhir_pukul")!=null){ Valid.SetTgl2(ObservasiTerakhirPukul, rs.getString("observasi_terakhir_pukul")); }
                    else{ ObservasiTerakhirPukul.setDate(new Date()); }

                    SudahDijelaskanDiagnosa.setSelectedItem(nz(rs.getString("sudah_dijelaskan_diagnosa")));
                    OksigenStatus.setSelectedItem(nz(rs.getString("oksigen_status")));
                    setChk(CekOksigenNasalKanule, rs.getString("oksigen_nasal_kanule"));
                    setChk(CekOksigenNasalKateter, rs.getString("oksigen_nasal_kateter"));
                    setChk(CekOksigenFaceMask, rs.getString("oksigen_face_mask"));
                    setChk(CekOksigenSungkupRebreathing, rs.getString("oksigen_sungkup_rebreathing"));
                    setChk(CekOksigenSungkupNonRebreathing, rs.getString("oksigen_sungkup_non_rebreathing"));
                    setChk(CekOksigenTracheostomy, rs.getString("oksigen_tracheostomy"));
                    setChk(CekOksigenEtt, rs.getString("oksigen_ett"));

                    String lineInvasif = rs.getString("line_invasif");
                    bgLineInvasif.clearSelection();
                    if("Tidak Ada Line Invasive".equals(lineInvasif)) RbLineTidakAda.setSelected(true);
                    else if("CVP/PICC".equals(lineInvasif)) RbLineCvpPicc.setSelected(true);
                    else if("IV Line".equals(lineInvasif)) RbLineIvLine.setSelected(true);
                    if(rs.getString("line_invasif_tanggal")!=null){ Valid.SetTgl2(LineInvasifTanggal, rs.getString("line_invasif_tanggal")); }
                    else{ LineInvasifTanggal.setDate(new Date()); }

                    setChk(CekKewaspadaanStandar, rs.getString("kewaspadaan_standar"));
                    setChk(CekKewaspadaanKontak, rs.getString("kewaspadaan_kontak"));
                    setChk(CekKewaspadaanAirborne, rs.getString("kewaspadaan_airborne"));
                    setChk(CekKewaspadaanDroplet, rs.getString("kewaspadaan_droplet"));

                    setChk(CekNutrisiPuasa, rs.getString("nutrisi_puasa"));
                    setChk(CekNutrisiOral, rs.getString("nutrisi_oral"));
                    setChk(CekNutrisiNgt, rs.getString("nutrisi_ngt"));
                    setChk(CekNutrisiDietKhusus, rs.getString("nutrisi_diet_khusus"));
                    TNutrisiBatasanCairanCc.setText(nz(rs.getString("nutrisi_batasan_cairan_cc")));
                    TNutrisiDietKhususJelaskan.setText(nz(rs.getString("nutrisi_diet_khusus_jelaskan")));

                    String eliminasiBab = rs.getString("eliminasi_bab");
                    bgEliminasiBab.clearSelection();
                    if("Normal".equals(eliminasiBab)) RbEliminasiBabNormal.setSelected(true);
                    else if("Diare/Konstipasi".equals(eliminasiBab)) RbEliminasiBabDiareKonstipasi.setSelected(true);
                    else if("Ileustomy/Colostomy".equals(eliminasiBab)) RbEliminasiBabIleustomyColostomy.setSelected(true);
                    else if("Inkontinensia Alvi".equals(eliminasiBab)) RbEliminasiBabInkontinensiaAlvi.setSelected(true);

                    String eliminasiBak = rs.getString("eliminasi_bak");
                    bgEliminasiBak.clearSelection();
                    if("Normal".equals(eliminasiBak)) RbEliminasiBakNormal.setSelected(true);
                    else if("Inkontinensia Urine".equals(eliminasiBak)) RbEliminasiBakInkontinensiaUrine.setSelected(true);
                    else if("Kateter Urine".equals(eliminasiBak)) RbEliminasiBakKateterUrine.setSelected(true);
                    TEliminasiBakKateterNo.setText(nz(rs.getString("eliminasi_bak_kateter_no")));
                    if(rs.getString("eliminasi_bak_kateter_tanggal_pasang")!=null){ Valid.SetTgl2(DTPEliminasiBakKateterTanggalPasang, rs.getString("eliminasi_bak_kateter_tanggal_pasang")); }
                    else{ DTPEliminasiBakKateterTanggalPasang.setDate(new Date()); }

                    String aktifitas = rs.getString("aktifitas");
                    bgAktifitas.clearSelection();
                    if("Mandiri".equals(aktifitas)) RbAktifitasMandiri.setSelected(true);
                    else if("Dibantu Sebagian".equals(aktifitas)) RbAktifitasDibantuSebagian.setSelected(true);
                    else if("Dibantu Penuh".equals(aktifitas)) RbAktifitasDibantuPenuh.setSelected(true);

                    String mobilisasi = rs.getString("mobilisasi");
                    bgMobilisasi.clearSelection();
                    if("Tirah Baring".equals(mobilisasi)) RbMobilisasiTirahBaring.setSelected(true);
                    else if("Duduk".equals(mobilisasi)) RbMobilisasiDuduk.setSelected(true);
                    else if("Jalan".equals(mobilisasi)) RbMobilisasiJalan.setSelected(true);

                    setChk(CekDrainaseWsd, rs.getString("drainase_wsd"));
                    setChk(CekDrainaseDrainLuka, rs.getString("drainase_drain_luka"));
                    setChk(CekDrainaseStoma, rs.getString("drainase_stoma"));
                    setChk(CekDrainaseLain, rs.getString("drainase_lain"));
                    TDrainaseLainKet.setText(nz(rs.getString("drainase_lain_ket")));

                    setChk(CekLukaTidakAda, rs.getString("luka_tidak_ada"));
                    setChk(CekLukaOperasi, rs.getString("luka_operasi_cek"));
                    TLukaOperasiUkuran.setText(nz(rs.getString("luka_operasi_ukuran")));
                    TLukaOperasiLokasi.setText(nz(rs.getString("luka_operasi_lokasi")));
                    TLukaOperasiKondisi.setText(nz(rs.getString("luka_operasi_kondisi")));
                    setChk(CekLukaDekubitus, rs.getString("luka_dekubitus_cek"));
                    TLukaDekubitusUkuran.setText(nz(rs.getString("luka_dekubitus_ukuran")));
                    TLukaDekubitusLokasi.setText(nz(rs.getString("luka_dekubitus_lokasi")));
                    TLukaDekubitusKondisi.setText(nz(rs.getString("luka_dekubitus_kondisi")));
                    setChk(CekLukaSkinLost, rs.getString("luka_skin_lost_cek"));
                    TLukaSkinLostUkuran.setText(nz(rs.getString("luka_skin_lost_ukuran")));
                    TLukaSkinLostLokasi.setText(nz(rs.getString("luka_skin_lost_lokasi")));
                    TLukaSkinLostKondisi.setText(nz(rs.getString("luka_skin_lost_kondisi")));
                    setChk(CekLukaGangren, rs.getString("luka_gangren_cek"));
                    TLukaGangrenUkuran.setText(nz(rs.getString("luka_gangren_ukuran")));
                    TLukaGangrenLokasi.setText(nz(rs.getString("luka_gangren_lokasi")));
                    TLukaGangrenKondisi.setText(nz(rs.getString("luka_gangren_kondisi")));
                    setChk(CekLukaLain, rs.getString("luka_lain_cek"));
                    TLukaLainKet.setText(nz(rs.getString("luka_lain_ket")));

                    HasilInvestigasiAbnormal.setText(nz(rs.getString("hasil_investigasi_abnormal")));

                    setChk(CekTindakanResikoJatuh, rs.getString("tindakan_resiko_jatuh"));
                    setChk(CekTindakanPemasanganRestrain, rs.getString("tindakan_pemasangan_restrain"));
                    setChk(CekTindakanPerawatanLuka, rs.getString("tindakan_perawatan_luka"));
                    setChk(CekTindakanHygiene, rs.getString("tindakan_hygiene"));
                    RiwayatAlergi.setSelectedItem(nz(rs.getString("riwayat_alergi")));
                    TJenisAlergi.setText(nz(rs.getString("jenis_alergi")));
                    TReaksiAlergi.setText(nz(rs.getString("reaksi_alergi")));

                    setChk(CekAlatBantuTanpa, rs.getString("alat_bantu_tanpa"));
                    setChk(CekAlatBantuDengar, rs.getString("alat_bantu_dengar"));
                    setChk(CekAlatBantuKacaMata, rs.getString("alat_bantu_kaca_mata"));
                    setChk(CekAlatBantuGigiPalsu, rs.getString("alat_bantu_gigi_palsu"));
                    setChk(CekAlatBantuTongkatKruk, rs.getString("alat_bantu_tongkat_kruk"));
                    setChk(CekAlatBantuLain, rs.getString("alat_bantu_lain"));
                    TAlatBantuLainKet.setText(nz(rs.getString("alat_bantu_lain_ket")));

                    TindakanYangSudahDilakukan.setText(nz(rs.getString("tindakan_yang_sudah_dilakukan")));

                    setChk(CekPenunjangLaboratorium, rs.getString("penunjang_laboratorium_cek"));
                    TPenunjangLaboratoriumKet.setText(nz(rs.getString("penunjang_laboratorium_ket")));
                    setChk(CekPenunjangRadiologi, rs.getString("penunjang_radiologi_cek"));
                    TPenunjangRadiologiKet.setText(nz(rs.getString("penunjang_radiologi_ket")));
                    setChk(CekPenunjangCtScan, rs.getString("penunjang_ct_scan_cek"));
                    TPenunjangCtScanKet.setText(nz(rs.getString("penunjang_ct_scan_ket")));
                    setChk(CekPenunjangLain, rs.getString("penunjang_lain_cek"));
                    TPenunjangLainKet.setText(nz(rs.getString("penunjang_lain_ket")));

                    RencanaTindakLanjut.setText(nz(rs.getString("rencana_tindak_lanjut")));
                    MasalahUtamaKeperawatan.setText(nz(rs.getString("masalah_utama_keperawatan")));
                    DokumenYangDiserahterimakan.setText(nz(rs.getString("dokumen_yang_diserahterimakan")));

                    bangunRingkasanHtml();
                }
            } catch (Exception e) {
                System.out.println("Notif getData RM 04 : "+e);
            } finally {
                if(rs!=null){ rs.close(); }
                if(ps!=null){ ps.close(); }
            }
        } catch (Exception e) {
            System.out.println("Notif getData RM 04 : "+e);
        }
    }

    private void isRawat() {
        try {
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien, if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi "+
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
 
    public void setNoRm(String norwt,Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);    
        isRawat(); 
        runBackground(() ->tampil());
    }
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.gettransfer_pasien_antar_ruang());
        BtnHapus.setEnabled(akses.gettransfer_pasien_antar_ruang());
        BtnEdit.setEnabled(akses.gettransfer_pasien_antar_ruang());
        BtnPrint.setEnabled(akses.gettransfer_pasien_antar_ruang());
    }
    
    public void setTampil(){
       TabRawat.setSelectedIndex(1);
    }

    private void hapus() {
        if(Sequel.queryu2tf("delete from transfer_pasien_antar_ruang where no_rawat=? and tanggal_masuk=?",2,new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),5).toString()
        })==true){
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText(""+tabMode.getRowCount());
            TabRawat.setSelectedIndex(1);
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
        }
    }

    /**
     * [RM 04-1/04-2] Kolom aktif tabel `transfer_pasien_antar_ruang` yang
     * ditulis oleh simpan()/ganti() - urutan HARUS tetap sinkron dengan
     * nilaiTransfer(). `no_rawat`/`tanggal_masuk` SENGAJA disertakan (bukan
     * dikecualikan spt pola RM 03-2) - dikonfirmasi user tetap boleh diedit
     * lewat form, sama seperti perilaku lama. 15 kolom lama yang dipensiunkan
     * (6 vital SbT + 6 vital StT + peralatan_yang_menyertai/keterangannya +
     * pemeriksaan_penunjang_yang_dilakukan) sengaja TIDAK disebut - datanya
     * tetap ada di DB (additive-only), cuma tidak lagi ditulis form baru.
     */
    private static final String[] KOLOM_TRANSFER = {
        "no_rawat","tanggal_masuk","tanggal_pindah","asal_ruang","ruang_selanjutnya","diagnosa_utama","diagnosa_sekunder",
        "indikasi_pindah_ruang","keterangan_indikasi_pindah_ruang","prosedur_yang_sudah_dilakukan","obat_yang_telah_diberikan",
        "metode_pemindahan_pasien","pasien_keluarga_menyetujui","nama_menyetujui","hubungan_menyetujui","nip_menyerahkan","nip_menerima",

        "no_tlp_menyetujui","sudah_dijelaskan_diagnosa","kelas_asal_ruang","kelas_ruang_selanjutnya",
        "kd_dokter_merawat_asal_1","kd_dokter_merawat_asal_2","kd_dokter_merawat_asal_3",
        "kd_dokter_merawat_tujuan_1","kd_dokter_merawat_tujuan_2","kd_dokter_merawat_tujuan_3",

        "kesadaran_transfer","td_transfer","hr_transfer","rr_transfer","suhu_transfer","gcs_transfer","spo2_transfer",
        "tingkat_nyeri_transfer","observasi_terakhir_pukul",

        "oksigen_status","oksigen_nasal_kanule","oksigen_nasal_kateter","oksigen_face_mask","oksigen_sungkup_rebreathing",
        "oksigen_sungkup_non_rebreathing","oksigen_tracheostomy","oksigen_ett",

        "line_invasif","line_invasif_tanggal",
        "kewaspadaan_standar","kewaspadaan_kontak","kewaspadaan_airborne","kewaspadaan_droplet",

        "nutrisi_puasa","nutrisi_oral","nutrisi_ngt","nutrisi_batasan_cairan_cc","nutrisi_diet_khusus","nutrisi_diet_khusus_jelaskan",

        "eliminasi_bab","eliminasi_bak","eliminasi_bak_kateter_no","eliminasi_bak_kateter_tanggal_pasang",

        "aktifitas","mobilisasi","drainase_wsd","drainase_drain_luka","drainase_stoma","drainase_lain","drainase_lain_ket",

        "luka_tidak_ada",
        "luka_operasi_cek","luka_operasi_ukuran","luka_operasi_lokasi","luka_operasi_kondisi",
        "luka_dekubitus_cek","luka_dekubitus_ukuran","luka_dekubitus_lokasi","luka_dekubitus_kondisi",
        "luka_skin_lost_cek","luka_skin_lost_ukuran","luka_skin_lost_lokasi","luka_skin_lost_kondisi",
        "luka_gangren_cek","luka_gangren_ukuran","luka_gangren_lokasi","luka_gangren_kondisi",
        "luka_lain_cek","luka_lain_ket",

        "hasil_investigasi_abnormal",

        "tindakan_resiko_jatuh","tindakan_pemasangan_restrain","tindakan_perawatan_luka","tindakan_hygiene",
        "riwayat_alergi","jenis_alergi","reaksi_alergi",

        "alat_bantu_tanpa","alat_bantu_dengar","alat_bantu_kaca_mata","alat_bantu_gigi_palsu","alat_bantu_tongkat_kruk",
        "alat_bantu_lain","alat_bantu_lain_ket",

        "tindakan_yang_sudah_dilakukan",

        "penunjang_laboratorium_cek","penunjang_laboratorium_ket","penunjang_radiologi_cek","penunjang_radiologi_ket",
        "penunjang_ct_scan_cek","penunjang_ct_scan_ket","penunjang_lain_cek","penunjang_lain_ket",

        "rencana_tindak_lanjut","masalah_utama_keperawatan","dokumen_yang_diserahterimakan"
    };

    private String ya(javax.swing.AbstractButton c){
        return c.isSelected() ? "Ya" : "Tidak";
    }

    private String comboValOrNull(widget.ComboBox c){
        Object v = c.getSelectedItem();
        if(v==null){ return null; }
        String s = v.toString();
        return s.isEmpty() ? null : s;
    }

    private String txtOrNull(widget.TextBox t){
        String s = t.getText();
        return (s==null || s.trim().isEmpty()) ? null : s;
    }

    /**
     * [RM 04-1/04-2] Nilai dari semua widget aktif, urutan HARUS sinkron
     * dengan KOLOM_TRANSFER di atas. Dipakai bersama oleh ganti() dan
     * simpan().
     */
    private String[] nilaiTransfer(){
        String lineInvasif = RbLineTidakAda.isSelected() ? "Tidak Ada Line Invasive" :
            (RbLineCvpPicc.isSelected() ? "CVP/PICC" : (RbLineIvLine.isSelected() ? "IV Line" : null));
        String lineInvasifTanggal = (RbLineCvpPicc.isSelected()||RbLineIvLine.isSelected()) ?
            Valid.SetTgl(LineInvasifTanggal.getSelectedItem()+"")+" "+LineInvasifTanggal.getSelectedItem().toString().substring(11,19) : null;

        String eliminasiBab = RbEliminasiBabNormal.isSelected() ? "Normal" :
            (RbEliminasiBabDiareKonstipasi.isSelected() ? "Diare/Konstipasi" :
            (RbEliminasiBabIleustomyColostomy.isSelected() ? "Ileustomy/Colostomy" :
            (RbEliminasiBabInkontinensiaAlvi.isSelected() ? "Inkontinensia Alvi" : null)));
        String eliminasiBak = RbEliminasiBakNormal.isSelected() ? "Normal" :
            (RbEliminasiBakInkontinensiaUrine.isSelected() ? "Inkontinensia Urine" :
            (RbEliminasiBakKateterUrine.isSelected() ? "Kateter Urine" : null));
        String kateterTanggal = RbEliminasiBakKateterUrine.isSelected() ?
            Valid.SetTgl(DTPEliminasiBakKateterTanggalPasang.getSelectedItem()+"")+" "+DTPEliminasiBakKateterTanggalPasang.getSelectedItem().toString().substring(11,19) : null;

        String aktifitas = RbAktifitasMandiri.isSelected() ? "Mandiri" :
            (RbAktifitasDibantuSebagian.isSelected() ? "Dibantu Sebagian" : (RbAktifitasDibantuPenuh.isSelected() ? "Dibantu Penuh" : null));
        String mobilisasi = RbMobilisasiTirahBaring.isSelected() ? "Tirah Baring" :
            (RbMobilisasiDuduk.isSelected() ? "Duduk" : (RbMobilisasiJalan.isSelected() ? "Jalan" : null));

        return new String[]{
            TNoRw.getText(),
            Valid.SetTgl(TanggalMasuk.getSelectedItem()+"")+" "+TanggalMasuk.getSelectedItem().toString().substring(11,19),
            Valid.SetTgl(TanggalPindah.getSelectedItem()+"")+" "+TanggalPindah.getSelectedItem().toString().substring(11,19),
            AsalRuang.getText(),RuangSelanjutnya.getText(),DiagnosaUtama.getText(),DiagnosaSekunder.getText(),
            IndikasiPindah.getSelectedItem().toString(),KeteranganIndikasiPindahRuang.getText(),
            ProsedurDilakukan.getText(),ObatYangDiberikan.getText(),MetodePemindahan.getSelectedItem().toString(),
            MenyetujuiPemindahan.getSelectedItem().toString(),NamaMenyetujui.getText(),HubunganMenyetujui.getSelectedItem().toString(),
            KdPetugasMenyerahkan.getText(),KdPetugasMenerima.getText(),

            TNoTlpMenyetujui.getText(),comboValOrNull(SudahDijelaskanDiagnosa),txtOrNull(KelasAsalRuang),txtOrNull(KelasRuangSelanjutnya),
            KdDokterMerawatAsal1.getText(),KdDokterMerawatAsal2.getText(),KdDokterMerawatAsal3.getText(),
            KdDokterMerawatTujuan1.getText(),KdDokterMerawatTujuan2.getText(),KdDokterMerawatTujuan3.getText(),

            comboValOrNull(KesadaranTransfer),TDTransfer.getText(),HRTransfer.getText(),RRTransfer.getText(),SuhuTransfer.getText(),
            GCSTransfer.getText(),SpO2Transfer.getText(),comboValOrNull(TingkatNyeriTransfer),
            Valid.SetTgl(ObservasiTerakhirPukul.getSelectedItem()+"")+" "+ObservasiTerakhirPukul.getSelectedItem().toString().substring(11,19),

            comboValOrNull(OksigenStatus),ya(CekOksigenNasalKanule),ya(CekOksigenNasalKateter),ya(CekOksigenFaceMask),
            ya(CekOksigenSungkupRebreathing),ya(CekOksigenSungkupNonRebreathing),ya(CekOksigenTracheostomy),ya(CekOksigenEtt),

            lineInvasif,lineInvasifTanggal,
            ya(CekKewaspadaanStandar),ya(CekKewaspadaanKontak),ya(CekKewaspadaanAirborne),ya(CekKewaspadaanDroplet),

            ya(CekNutrisiPuasa),ya(CekNutrisiOral),ya(CekNutrisiNgt),TNutrisiBatasanCairanCc.getText(),
            ya(CekNutrisiDietKhusus),TNutrisiDietKhususJelaskan.getText(),

            eliminasiBab,eliminasiBak,TEliminasiBakKateterNo.getText(),kateterTanggal,

            aktifitas,mobilisasi,ya(CekDrainaseWsd),ya(CekDrainaseDrainLuka),ya(CekDrainaseStoma),ya(CekDrainaseLain),TDrainaseLainKet.getText(),

            ya(CekLukaTidakAda),
            ya(CekLukaOperasi),TLukaOperasiUkuran.getText(),TLukaOperasiLokasi.getText(),TLukaOperasiKondisi.getText(),
            ya(CekLukaDekubitus),TLukaDekubitusUkuran.getText(),TLukaDekubitusLokasi.getText(),TLukaDekubitusKondisi.getText(),
            ya(CekLukaSkinLost),TLukaSkinLostUkuran.getText(),TLukaSkinLostLokasi.getText(),TLukaSkinLostKondisi.getText(),
            ya(CekLukaGangren),TLukaGangrenUkuran.getText(),TLukaGangrenLokasi.getText(),TLukaGangrenKondisi.getText(),
            ya(CekLukaLain),TLukaLainKet.getText(),

            HasilInvestigasiAbnormal.getText(),

            ya(CekTindakanResikoJatuh),ya(CekTindakanPemasanganRestrain),ya(CekTindakanPerawatanLuka),ya(CekTindakanHygiene),
            comboValOrNull(RiwayatAlergi),TJenisAlergi.getText(),TReaksiAlergi.getText(),

            ya(CekAlatBantuTanpa),ya(CekAlatBantuDengar),ya(CekAlatBantuKacaMata),ya(CekAlatBantuGigiPalsu),ya(CekAlatBantuTongkatKruk),
            ya(CekAlatBantuLain),TAlatBantuLainKet.getText(),

            TindakanYangSudahDilakukan.getText(),

            ya(CekPenunjangLaboratorium),TPenunjangLaboratoriumKet.getText(),ya(CekPenunjangRadiologi),TPenunjangRadiologiKet.getText(),
            ya(CekPenunjangCtScan),TPenunjangCtScanKet.getText(),ya(CekPenunjangLain),TPenunjangLainKet.getText(),

            RencanaTindakLanjut.getText(),MasalahUtamaKeperawatan.getText(),DokumenYangDiserahterimakan.getText()
        };
    }

    private void ganti() {
        StringBuilder set = new StringBuilder();
        for(String kolom : KOLOM_TRANSFER){
            set.append(kolom).append("=?,");
        }
        set.setLength(set.length()-1);

        String[] nilai = nilaiTransfer();
        String[] param = new String[nilai.length+2];
        System.arraycopy(nilai,0,param,0,nilai.length);
        param[nilai.length] = tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();
        param[nilai.length+1] = tbObat.getValueAt(tbObat.getSelectedRow(),5).toString();

        if(Sequel.mengedittf("transfer_pasien_antar_ruang","no_rawat=? and tanggal_masuk=?",set.toString(),param.length,param)==true){
            tbObat.setValueAt(TNoRw.getText(),tbObat.getSelectedRow(),0);
            tbObat.setValueAt(TNoRM.getText(),tbObat.getSelectedRow(),1);
            tbObat.setValueAt(TPasien.getText(),tbObat.getSelectedRow(),2);
            tbObat.setValueAt(TglLahir.getText(),tbObat.getSelectedRow(),3);
            tbObat.setValueAt(Jk.getText(),tbObat.getSelectedRow(),4);
            tbObat.setValueAt(Valid.SetTgl(TanggalMasuk.getSelectedItem()+"")+" "+TanggalMasuk.getSelectedItem().toString().substring(11,19),tbObat.getSelectedRow(),5);
            tbObat.setValueAt(Valid.SetTgl(TanggalPindah.getSelectedItem()+"")+" "+TanggalPindah.getSelectedItem().toString().substring(11,19),tbObat.getSelectedRow(),6);
            tbObat.setValueAt(DiagnosaUtama.getText(),tbObat.getSelectedRow(),7);
            tbObat.setValueAt(KdPetugasMenyerahkan.getText(),tbObat.getSelectedRow(),8);
            tbObat.setValueAt(NmPetugasMenyerahkan.getText(),tbObat.getSelectedRow(),9);
            tbObat.setValueAt(KdPetugasMenerima.getText(),tbObat.getSelectedRow(),10);
            tbObat.setValueAt(NmPetugasMenerima.getText(),tbObat.getSelectedRow(),11);
            emptTeks();
            TabRawat.setSelectedIndex(1);
        }
    }

    /**
     * [RM 04-1/04-2] INSERT manual bernama kolom (bukan Sequel.menyimpantf,
     * yang posisional murni mengikuti urutan fisik SEMUA kolom tabel - tidak
     * cocok utk tabel ini dgn ~130 kolom). Pola sama seperti simpan() di
     * RMPenilaianAwalMedisIGD.java (RM 03-2).
     */
    private void simpan() {
        StringBuilder kolom = new StringBuilder();
        StringBuilder placeholder = new StringBuilder();
        for(String k : KOLOM_TRANSFER){
            if(kolom.length()>0){ kolom.append(","); placeholder.append(","); }
            kolom.append(k); placeholder.append("?");
        }
        String[] param = nilaiTransfer();

        boolean sukses = false;
        try{
            PreparedStatement psSimpan = koneksi.prepareStatement(
                "insert into transfer_pasien_antar_ruang ("+kolom+") values ("+placeholder+")");
            try{
                for(int idx=0;idx<param.length;idx++){
                    psSimpan.setString(idx+1,param[idx]);
                }
                psSimpan.executeUpdate();
                sukses = true;
            } finally {
                if(psSimpan!=null){ psSimpan.close(); }
            }
        }catch(Exception e){
            System.out.println("Notif Simpan RM 04 : "+e);
            JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan No.Rawat & Tanggal Masuk yang sama sudah dimasukkan sebelumnya...!");
        }

        if(sukses){
            tabMode.addRow(new Object[]{
                TNoRw.getText(),TNoRM.getText(),TPasien.getText(),TglLahir.getText(),Jk.getText(),
                Valid.SetTgl(TanggalMasuk.getSelectedItem()+"")+" "+TanggalMasuk.getSelectedItem().toString().substring(11,19),
                Valid.SetTgl(TanggalPindah.getSelectedItem()+"")+" "+TanggalPindah.getSelectedItem().toString().substring(11,19),
                DiagnosaUtama.getText(),KdPetugasMenyerahkan.getText(),NmPetugasMenyerahkan.getText(),KdPetugasMenerima.getText(),NmPetugasMenerima.getText()
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

    /**
     * [RM 04-1/04-2] Ambil 1 bukti (foto/TTD) dari salah satu dari 4 tabel
     * peran, kembalikan potongan HTML utk 1 sel grid 2x2 (dipakai bareng
     * oleh panggilPhoto()). Query lokal per panggilan (bukan pakai field
     * ps/rs kelas) supaya tidak saling tabrak antar 4 pemanggilan berturut.
     * `nama` (nama penandatangan, sudah di-resolve oleh panggilPhoto())
     * ditampilkan di bawah label peran, pola sama dgn baris "Ditandatangani
     * oleh : NAMA" di halaman webapp (kamera*.php).
     */
    private String fotoImgTag(String tabelBukti, String label, String nama, String noRawat, String tanggalMasuk){
        String namaTampil = (nama==null||nama.trim().isEmpty()) ? "-" : nama;
        String html = "<b>"+label+"</b><br><font face='tahoma' size='2'>"+namaTampil+"</font><br><font face='tahoma' size='2' color='#434343'>Kosong</font>";
        try {
            PreparedStatement psFoto = koneksi.prepareStatement(
                "select photo from "+tabelBukti+" where no_rawat=? and tanggal_masuk=?");
            try {
                psFoto.setString(1,noRawat);
                psFoto.setString(2,tanggalMasuk);
                ResultSet rsFoto = psFoto.executeQuery();
                try {
                    if(rsFoto.next() && rsFoto.getString("photo")!=null && !rsFoto.getString("photo").equals("") && !rsFoto.getString("photo").equals("-")){
                        html = "<b>"+label+"</b><br><font face='tahoma' size='2'>"+namaTampil+"</font><br><img src='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/persetujuantransferruang/"+rsFoto.getString("photo")+"' alt='photo' width='220' height='220'/>";
                    }
                } finally {
                    if(rsFoto!=null){ rsFoto.close(); }
                }
            } finally {
                if(psFoto!=null){ psFoto.close(); }
            }
        } catch (Exception e) {
            System.out.println("Notif fotoImgTag : "+e);
        }
        return html;
    }

    /**
     * [RM 04-1/04-2] Tampilkan ke-4 bukti tanda tangan/foto (Petugas
     * Pengirim, Petugas Penerima, Dokter Yang Merawat, Pasien/Keluarga)
     * sekaligus sbg grid 2x2 - sebelumnya cuma 1 foto (Pasien/Keluarga).
     * Nama tiap penandatangan diambil lewat query lokal tersendiri (BUKAN
     * dari widget header, krn `panggilPhoto()` dipanggil SEBELUM
     * `getData()` di `tbObatMouseClicked` - widget header masih berisi
     * data baris sebelumnya/kosong saat method ini jalan), pola sama
     * `fotoImgTag()` (JOIN lokal, tidak pakai field ps/rs kelas).
     */
    private void panggilPhoto() {
        if(FormPhoto.isVisible()==true){
            String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();
            String tanggalMasuk = tbObat.getValueAt(tbObat.getSelectedRow(),5).toString();

            String namaPengirim = "", namaPenerima = "", namaDokter = "", namaPasien = "";
            try {
                PreparedStatement psNama = koneksi.prepareStatement(
                    "select petugasmenyerahkan.nama as nm_menyerahkan, petugasmenerima.nama as nm_menerima, "+
                    "coalesce(dokterasal1.nm_dokter,doktertujuan1.nm_dokter) as nm_dokter, transfer_pasien_antar_ruang.nama_menyetujui "+
                    "from transfer_pasien_antar_ruang "+
                    "left join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                    "left join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip "+
                    "left join dokter as dokterasal1 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_1=dokterasal1.kd_dokter "+
                    "left join dokter as doktertujuan1 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_1=doktertujuan1.kd_dokter "+
                    "where transfer_pasien_antar_ruang.no_rawat=? and transfer_pasien_antar_ruang.tanggal_masuk=?");
                try {
                    psNama.setString(1,noRawat);
                    psNama.setString(2,tanggalMasuk);
                    ResultSet rsNama = psNama.executeQuery();
                    try {
                        if(rsNama.next()){
                            namaPengirim = rsNama.getString("nm_menyerahkan");
                            namaPenerima = rsNama.getString("nm_menerima");
                            namaDokter = rsNama.getString("nm_dokter");
                            namaPasien = rsNama.getString("nama_menyetujui");
                        }
                    } finally {
                        if(rsNama!=null){ rsNama.close(); }
                    }
                } finally {
                    if(psNama!=null){ psNama.close(); }
                }
            } catch (Exception e) {
                System.out.println("Notif panggilPhoto (nama) : "+e);
            }

            String htmlPengirim = fotoImgTag("bukti_transfer_petugas_pengirim","Petugas Pengirim",namaPengirim,noRawat,tanggalMasuk);
            String htmlPenerima = fotoImgTag("bukti_transfer_petugas_penerima","Petugas Penerima",namaPenerima,noRawat,tanggalMasuk);
            String htmlDokter = fotoImgTag("bukti_transfer_dokter_merawat","Dokter Yang Merawat",namaDokter,noRawat,tanggalMasuk);
            String htmlPasien = fotoImgTag("bukti_persetujuan_transfer_pasien_antar_ruang","Pasien/Keluarga",namaPasien,noRawat,tanggalMasuk);
            LoadHTML2.setText("<html><body><table width='100%'><tr>"+
                "<td align='center' valign='top'>"+htmlPengirim+"</td><td align='center' valign='top'>"+htmlPenerima+"</td></tr><tr>"+
                "<td align='center' valign='top'>"+htmlDokter+"</td><td align='center' valign='top'>"+htmlPasien+"</td></tr></table></body></html>");
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
