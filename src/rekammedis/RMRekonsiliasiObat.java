/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgPerawatan.java
 *
 * Created on May 23, 2010, 6:36:30 PM
 */

package rekammedis;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Component;
import java.awt.BasicStroke;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.AbstractButton;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import kepegawaian.DlgCariPetugas;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import javax.swing.WindowConstants;

/**
 *
 * @author dosen
 */
public final class RMRekonsiliasiObat extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private DefaultTableModel tabModeData;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private DlgCariPetugas petugas;
    private PreparedStatement ps;
    private ResultSet rs;
    private int i,reply;
    

    /** Creates new form DlgPerawatan
     * @param parent
     * @param modal */
    public RMRekonsiliasiObat(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        widget.IconStyle.pasang(this);
        initRekonsiliasiTambahan();

        initModernModalTambahObat();

        tabMode=new DefaultTableModel(null,new Object[]{
            "","NAMA OBAT","DOSIS OBAT","FREKUENSI","TINDAK LANJUT","PERUBAHAN ATURAN PAKAI"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbPemeriksaan.setModel(tabMode);
        tbPemeriksaan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbPemeriksaan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbPemeriksaan.setRowHeight(32);
        tbPemeriksaan.setShowGrid(false);
        tbPemeriksaan.setIntercellSpacing(new Dimension(0, 1));
        tbPemeriksaan.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        tbPemeriksaan.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbPemeriksaan.getColumnModel().getColumn(0).setPreferredWidth(35);
        tbPemeriksaan.getColumnModel().getColumn(0).setMaxWidth(35);
        tbPemeriksaan.getColumnModel().getColumn(0).setMinWidth(35);
        tbPemeriksaan.getColumnModel().getColumn(1).setPreferredWidth(280);
        tbPemeriksaan.getColumnModel().getColumn(2).setPreferredWidth(110);
        tbPemeriksaan.getColumnModel().getColumn(3).setPreferredWidth(110);
        tbPemeriksaan.getColumnModel().getColumn(4).setPreferredWidth(200);
        tbPemeriksaan.getColumnModel().getColumn(5).setPreferredWidth(280);

        tbPemeriksaan.getColumnModel().getColumn(0).setCellRenderer(new RadioCellRenderer());
        tbPemeriksaan.getColumnModel().getColumn(1).setCellRenderer(new BoldTextCellRenderer());
        tbPemeriksaan.getColumnModel().getColumn(2).setCellRenderer(new ModernTextCellRenderer(false, true));
        tbPemeriksaan.getColumnModel().getColumn(3).setCellRenderer(new ModernTextCellRenderer(false, true));
        tbPemeriksaan.getColumnModel().getColumn(4).setCellRenderer(new BadgeCellRenderer());
        tbPemeriksaan.getColumnModel().getColumn(5).setCellRenderer(new ModernTextCellRenderer(true, false));

        javax.swing.table.DefaultTableCellRenderer tblHeaderRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(new Color(15, 43, 70));
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 10));
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (column == 2 || column == 3 || column == 4) {
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                }
                return l;
            }
        };
        for (int c = 0; c < tbPemeriksaan.getColumnCount(); c++) {
            tbPemeriksaan.getColumnModel().getColumn(c).setHeaderRenderer(tblHeaderRenderer);
        }

        tbPemeriksaan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tbPemeriksaan.repaint();
            }
        });

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        NamaObat.setDocument(new batasInput((int)100).getKata(NamaObat));
        DosisObat.setDocument(new batasInput((byte)20).getKata(DosisObat));
        Frekuensi.setDocument(new batasInput((byte)10).getKata(Frekuensi));
        PerubahanAturanPakai.setDocument(new batasInput((int)150).getKata(PerubahanAturanPakai));
        TNoRekonsialiasi.setDocument(new batasInput((byte)20).getKata(TNoRekonsialiasi));
        AlergiObat.setDocument(new batasInput((int)70).getKata(AlergiObat));
        ManifestasiAlergi.setDocument(new batasInput((int)70).getKata(ManifestasiAlergi));
        
        ChkJln.setSelected(true);
        jam();

        initModernLayout();
        this.setSize(1360, 680);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        DlgTambahObatRekonsiliasi = new javax.swing.JDialog();
        internalFrame4 = new widget.InternalFrame();
        panelBiasa2 = new widget.PanelBiasa();
        jLabel99 = new widget.Label();
        BtnKeluarRekon = new widget.Button();
        BtnSimpanRekon = new widget.Button();
        NamaObat = new widget.TextBox();
        jLabel105 = new widget.Label();
        DosisObat = new widget.TextBox();
        jLabel108 = new widget.Label();
        TindakLanjut = new widget.ComboBox();
        jLabel109 = new widget.Label();
        Frekuensi = new widget.TextBox();
        jLabel111 = new widget.Label();
        AturanPakai = new widget.TextBox();
        jLabel112 = new widget.Label();
        PemberianTerakhir = new widget.TextBox();
        jLabel113 = new widget.Label();
        PerubahanAturanPakai = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        Scroll2 = new widget.ScrollPane();
        tbPemeriksaan = new widget.Table();
        panelGlass8 = new widget.panelisi();
        BtnTambah = new widget.Button();
        BtnHapus = new widget.Button();
        label9 = new widget.Label();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnCari = new widget.Button();
        BtnKeluar = new widget.Button();
        PanelInput = new widget.PanelBiasa();
        jLabel3 = new widget.Label();
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        jLabel9 = new widget.Label();
        Tanggal = new widget.Tanggal();
        CmbJam = new widget.ComboBox();
        CmbMenit = new widget.ComboBox();
        CmbDetik = new widget.ComboBox();
        ChkJln = new widget.CekBox();
        KdPetugas = new widget.TextBox();
        NmPetugas = new widget.TextBox();
        BtnPetugas = new widget.Button();
        jLabel15 = new widget.Label();
        jLabel4 = new widget.Label();
        TNoRekonsialiasi = new widget.TextBox();
        jLabel5 = new widget.Label();
        RekonsiliasiSaat = new widget.ComboBox();
        jLabel55 = new widget.Label();
        scrollPane2 = new widget.ScrollPane();
        AlergiObat = new widget.TextArea();
        jLabel56 = new widget.Label();
        scrollPane3 = new widget.ScrollPane();
        ManifestasiAlergi = new widget.TextArea();
        jLabel6 = new widget.Label();
        DampakAlergi = new widget.ComboBox();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        jLabel11 = new widget.Label();
        Jk = new widget.TextBox();

        DlgTambahObatRekonsiliasi.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        DlgTambahObatRekonsiliasi.setName("DlgTambahObatRekonsiliasi"); // NOI18N
        DlgTambahObatRekonsiliasi.setUndecorated(true);
        DlgTambahObatRekonsiliasi.setResizable(false);

        internalFrame4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 235, 225)), "::[ Obat Yang Mau Direkonsiliasi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(50, 70, 50))); // NOI18N
        internalFrame4.setName("internalFrame4"); // NOI18N
        internalFrame4.setLayout(new java.awt.BorderLayout(1, 1));

        panelBiasa2.setName("panelBiasa2"); // NOI18N
        panelBiasa2.setLayout(null);

        jLabel99.setText("Nama Obat :");
        jLabel99.setName("jLabel99"); // NOI18N
        panelBiasa2.add(jLabel99);
        jLabel99.setBounds(0, 10, 85, 23);

        BtnKeluarRekon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cross.png"))); // NOI18N
        BtnKeluarRekon.setMnemonic('U');
        BtnKeluarRekon.setText("Tutup");
        BtnKeluarRekon.setToolTipText("Alt+U");
        BtnKeluarRekon.setName("BtnKeluarRekon"); // NOI18N
        BtnKeluarRekon.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluarRekon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarRekonActionPerformed(evt);
            }
        });
        panelBiasa2.add(BtnKeluarRekon);
        BtnKeluarRekon.setBounds(520, 100, 100, 30);

        BtnSimpanRekon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpanRekon.setMnemonic('S');
        BtnSimpanRekon.setText("Simpan");
        BtnSimpanRekon.setToolTipText("Alt+S");
        BtnSimpanRekon.setName("BtnSimpanRekon"); // NOI18N
        BtnSimpanRekon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanRekonActionPerformed(evt);
            }
        });
        BtnSimpanRekon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanRekonKeyPressed(evt);
            }
        });
        panelBiasa2.add(BtnSimpanRekon);
        BtnSimpanRekon.setBounds(415, 100, 100, 30);

        NamaObat.setHighlighter(null);
        NamaObat.setName("NamaObat"); // NOI18N
        NamaObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NamaObatKeyPressed(evt);
            }
        });
        panelBiasa2.add(NamaObat);
        NamaObat.setBounds(89, 10, 532, 23);

        jLabel105.setText("Dosis Obat :");
        jLabel105.setName("jLabel105"); // NOI18N
        panelBiasa2.add(jLabel105);
        jLabel105.setBounds(0, 40, 85, 23);

        DosisObat.setHighlighter(null);
        DosisObat.setName("DosisObat"); // NOI18N
        DosisObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DosisObatKeyPressed(evt);
            }
        });
        panelBiasa2.add(DosisObat);
        DosisObat.setBounds(89, 40, 90, 23);

        jLabel108.setText("Tindak Lanjut :");
        jLabel108.setName("jLabel108"); // NOI18N
        panelBiasa2.add(jLabel108);
        jLabel108.setBounds(367, 70, 150, 23);

        TindakLanjut.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Stop", "Lanjut" }));
        TindakLanjut.setName("TindakLanjut"); // NOI18N
        TindakLanjut.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TindakLanjutKeyPressed(evt);
            }
        });
        panelBiasa2.add(TindakLanjut);
        TindakLanjut.setBounds(521, 70, 100, 23);

        jLabel109.setText("Frekuensi :");
        jLabel109.setName("jLabel109"); // NOI18N
        panelBiasa2.add(jLabel109);
        jLabel109.setBounds(190, 40, 70, 23);

        Frekuensi.setHighlighter(null);
        Frekuensi.setName("Frekuensi"); // NOI18N
        Frekuensi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FrekuensiKeyPressed(evt);
            }
        });
        panelBiasa2.add(Frekuensi);
        Frekuensi.setBounds(264, 40, 90, 23);

        jLabel111.setText("Aturan Pakai :");
        jLabel111.setName("jLabel111"); // NOI18N
        panelBiasa2.add(jLabel111);
        jLabel111.setBounds(0, 70, 85, 23);

        AturanPakai.setHighlighter(null);
        AturanPakai.setName("AturanPakai"); // NOI18N
        AturanPakai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AturanPakaiKeyPressed(evt);
            }
        });
        panelBiasa2.add(AturanPakai);
        AturanPakai.setBounds(89, 70, 260, 23);

        jLabel112.setText("Waktu Pemberian Terakhir :");
        jLabel112.setName("jLabel112"); // NOI18N
        panelBiasa2.add(jLabel112);
        jLabel112.setBounds(367, 40, 150, 23);

        PemberianTerakhir.setHighlighter(null);
        PemberianTerakhir.setName("PemberianTerakhir"); // NOI18N
        PemberianTerakhir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PemberianTerakhirKeyPressed(evt);
            }
        });
        panelBiasa2.add(PemberianTerakhir);
        PemberianTerakhir.setBounds(521, 40, 100, 23);

        jLabel113.setText("Perubahan Aturan Pakai :");
        jLabel113.setName("jLabel113"); // NOI18N
        panelBiasa2.add(jLabel113);
        jLabel113.setBounds(0, 100, 140, 23);

        PerubahanAturanPakai.setHighlighter(null);
        PerubahanAturanPakai.setName("PerubahanAturanPakai"); // NOI18N
        PerubahanAturanPakai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PerubahanAturanPakaiKeyPressed(evt);
            }
        });
        panelBiasa2.add(PerubahanAturanPakai);
        PerubahanAturanPakai.setBounds(144, 100, 260, 23);

        internalFrame4.add(panelBiasa2, java.awt.BorderLayout.CENTER);

        DlgTambahObatRekonsiliasi.getContentPane().add(internalFrame4, java.awt.BorderLayout.CENTER);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Rekonsiliasi Obat ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(816, 102));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(300, 102));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        tbPemeriksaan.setName("tbPemeriksaan"); // NOI18N
        Scroll2.setViewportView(tbPemeriksaan);

        jPanel3.add(Scroll2, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);

        internalFrame1.add(jPanel1, java.awt.BorderLayout.CENTER);

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnTambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/add-file-16x16.png"))); // NOI18N
        BtnTambah.setMnemonic('T');
        BtnTambah.setText("Tambah");
        BtnTambah.setToolTipText("Alt+T");
        BtnTambah.setName("BtnTambah"); // NOI18N
        BtnTambah.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahActionPerformed(evt);
            }
        });
        BtnTambah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnTambahKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnTambah);

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

        label9.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        label9.setName("label9"); // NOI18N
        label9.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass8.add(label9);

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
        BtnBatal.setText("Clear");
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

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnCari.setMnemonic('E');
        BtnCari.setText("Cari");
        BtnCari.setToolTipText("Alt+E");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(100, 30));
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
        panelGlass8.add(BtnCari);

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

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setPreferredSize(new java.awt.Dimension(560, 185));
        PanelInput.setLayout(null);

        jLabel3.setText("No.Rawat :");
        jLabel3.setName("jLabel3"); // NOI18N
        PanelInput.add(jLabel3);
        jLabel3.setBounds(0, 12, 94, 23);

        TNoRw.setEditable(false);
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        PanelInput.add(TNoRw);
        TNoRw.setBounds(98, 12, 128, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        PanelInput.add(TNoRM);
        TNoRM.setBounds(228, 12, 100, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        PanelInput.add(TPasien);
        TPasien.setBounds(330, 12, 225, 23);

        jLabel9.setText("Petugas :");
        jLabel9.setName("jLabel9"); // NOI18N
        PanelInput.add(jLabel9);
        jLabel9.setBounds(197, 40, 69, 23);

        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "07-02-2026" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                TanggalItemStateChanged(evt);
            }
        });
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        PanelInput.add(Tanggal);
        Tanggal.setBounds(395, 70, 90, 23);

        CmbJam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        CmbJam.setName("CmbJam"); // NOI18N
        PanelInput.add(CmbJam);
        CmbJam.setBounds(489, 70, 62, 23);

        CmbMenit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        CmbMenit.setName("CmbMenit"); // NOI18N
        PanelInput.add(CmbMenit);
        CmbMenit.setBounds(555, 70, 62, 23);

        CmbDetik.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        CmbDetik.setName("CmbDetik"); // NOI18N
        PanelInput.add(CmbDetik);
        CmbDetik.setBounds(621, 70, 62, 23);

        ChkJln.setBorder(null);
        ChkJln.setSelected(true);
        ChkJln.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        ChkJln.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkJln.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkJln.setName("ChkJln"); // NOI18N
        PanelInput.add(ChkJln);
        ChkJln.setBounds(687, 70, 23, 23);

        KdPetugas.setEditable(false);
        KdPetugas.setName("KdPetugas"); // NOI18N
        PanelInput.add(KdPetugas);
        KdPetugas.setBounds(270, 40, 128, 23);

        NmPetugas.setEditable(false);
        NmPetugas.setHighlighter(null);
        NmPetugas.setName("NmPetugas"); // NOI18N
        PanelInput.add(NmPetugas);
        NmPetugas.setBounds(400, 40, 280, 23);

        BtnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPetugas.setMnemonic('4');
        BtnPetugas.setToolTipText("ALt+4");
        BtnPetugas.setName("BtnPetugas"); // NOI18N
        BtnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPetugasActionPerformed(evt);
            }
        });
        PanelInput.add(BtnPetugas);
        BtnPetugas.setBounds(682, 40, 28, 23);

        jLabel15.setText("Tanggal Wawancara :");
        jLabel15.setName("jLabel15"); // NOI18N
        PanelInput.add(jLabel15);
        jLabel15.setBounds(261, 70, 130, 23);

        jLabel4.setText("No.Rekonsiliasi :");
        jLabel4.setName("jLabel4"); // NOI18N
        PanelInput.add(jLabel4);
        jLabel4.setBounds(0, 70, 94, 23);

        TNoRekonsialiasi.setHighlighter(null);
        TNoRekonsialiasi.setName("TNoRekonsialiasi"); // NOI18N
        TNoRekonsialiasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRekonsialiasiKeyPressed(evt);
            }
        });
        PanelInput.add(TNoRekonsialiasi);
        TNoRekonsialiasi.setBounds(98, 70, 150, 23);

        jLabel5.setText("Rekonsiliasi Saat :");
        jLabel5.setName("jLabel5"); // NOI18N
        PanelInput.add(jLabel5);
        jLabel5.setBounds(346, 150, 120, 23);

        RekonsiliasiSaat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Admisi", "Transfer Antar Ruang", "Pindah Faskes Lain", "Pulang" }));
        RekonsiliasiSaat.setName("RekonsiliasiSaat"); // NOI18N
        RekonsiliasiSaat.setPreferredSize(new java.awt.Dimension(45, 23));
        RekonsiliasiSaat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RekonsiliasiSaatKeyPressed(evt);
            }
        });
        PanelInput.add(RekonsiliasiSaat);
        RekonsiliasiSaat.setBounds(470, 150, 160, 23);

        jLabel55.setText("Alergi Obat :");
        jLabel55.setName("jLabel55"); // NOI18N
        PanelInput.add(jLabel55);
        jLabel55.setBounds(0, 100, 94, 23);

        scrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane2.setName("scrollPane2"); // NOI18N

        AlergiObat.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        AlergiObat.setColumns(20);
        AlergiObat.setRows(5);
        AlergiObat.setName("AlergiObat"); // NOI18N
        AlergiObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AlergiObatKeyPressed(evt);
            }
        });
        scrollPane2.setViewportView(AlergiObat);

        PanelInput.add(scrollPane2);
        scrollPane2.setBounds(98, 100, 240, 43);

        jLabel56.setText("Manifestasi Alergi :");
        jLabel56.setName("jLabel56"); // NOI18N
        PanelInput.add(jLabel56);
        jLabel56.setBounds(346, 100, 120, 23);

        scrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane3.setName("scrollPane3"); // NOI18N

        ManifestasiAlergi.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ManifestasiAlergi.setColumns(20);
        ManifestasiAlergi.setRows(5);
        ManifestasiAlergi.setName("ManifestasiAlergi"); // NOI18N
        ManifestasiAlergi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ManifestasiAlergiKeyPressed(evt);
            }
        });
        scrollPane3.setViewportView(ManifestasiAlergi);

        PanelInput.add(scrollPane3);
        scrollPane3.setBounds(470, 100, 240, 43);

        jLabel6.setText("Dampak Alergi :");
        jLabel6.setName("jLabel6"); // NOI18N
        PanelInput.add(jLabel6);
        jLabel6.setBounds(0, 150, 94, 23);

        DampakAlergi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "Ringan", "Sedang", "Berat" }));
        DampakAlergi.setName("DampakAlergi"); // NOI18N
        DampakAlergi.setPreferredSize(new java.awt.Dimension(45, 23));
        DampakAlergi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DampakAlergiKeyPressed(evt);
            }
        });
        PanelInput.add(DampakAlergi);
        DampakAlergi.setBounds(98, 150, 90, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        PanelInput.add(jLabel8);
        jLabel8.setBounds(566, 10, 60, 23);

        TglLahir.setEditable(false);
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        PanelInput.add(TglLahir);
        TglLahir.setBounds(630, 10, 80, 23);

        jLabel11.setText("J.K. :");
        jLabel11.setName("jLabel11"); // NOI18N
        PanelInput.add(jLabel11);
        jLabel11.setBounds(0, 40, 94, 23);

        Jk.setEditable(false);
        Jk.setHighlighter(null);
        Jk.setName("Jk"); // NOI18N
        PanelInput.add(Jk);
        Jk.setBounds(98, 40, 80, 23);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah(evt, TNoRekonsialiasi, AlergiObat);
    }//GEN-LAST:event_TanggalKeyPressed

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

    private void TNoRekonsialiasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRekonsialiasiKeyPressed
        Valid.pindah(evt, BtnPetugas, AlergiObat);
    }//GEN-LAST:event_TNoRekonsialiasiKeyPressed

    private void TanggalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_TanggalItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if(Tanggal.getSelectedItem()!=null){
                autoNomor();
            }
        }
    }//GEN-LAST:event_TanggalItemStateChanged

    private void RekonsiliasiSaatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RekonsiliasiSaatKeyPressed
        Valid.pindah2(evt, DampakAlergi, BtnTambah);
    }//GEN-LAST:event_RekonsiliasiSaatKeyPressed

    private void AlergiObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AlergiObatKeyPressed
        Valid.pindah2(evt, TNoRekonsialiasi, ManifestasiAlergi);
    }//GEN-LAST:event_AlergiObatKeyPressed

    private void ManifestasiAlergiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ManifestasiAlergiKeyPressed
        Valid.pindah2(evt, AlergiObat, DampakAlergi);
    }//GEN-LAST:event_ManifestasiAlergiKeyPressed

    private void DampakAlergiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DampakAlergiKeyPressed
        Valid.pindah(evt, ManifestasiAlergi, RekonsiliasiSaat);
    }//GEN-LAST:event_DampakAlergiKeyPressed

    private void BtnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahActionPerformed
        if(TNoRM.getText().equals("")){
            JOptionPane.showMessageDialog(null,"Pilih terlebih dahulu pasien yang mau dimasukkan data rekonsiliasinya...");
            BtnTambah.requestFocus();
        }else{
            emptTeksTambahRekon();
            DlgTambahObatRekonsiliasi.setLocationRelativeTo(this);
            DlgTambahObatRekonsiliasi.setVisible(true);
        }
    }//GEN-LAST:event_BtnTambahActionPerformed

    private void BtnTambahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnTambahKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnTambahActionPerformed(null);
        }else{
            Valid.pindah(evt,RekonsiliasiSaat,BtnBatal);
        }
    }//GEN-LAST:event_BtnTambahKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(tbPemeriksaan.getSelectedRow()>-1){
            tabMode.removeRow(tbPemeriksaan.getSelectedRow());
            emptTeksTambahRekon();
            updateCounterObat();
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
        }   
    }//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnTambah, BtnCari);
        }
    }//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        DlgTambahObatRekonsiliasi.dispose();
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnCari,BtnTambah);}
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        TabPilihan.setSelectedIndex(1);
        tampilDataRekonsiliasi();
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        Valid.pindah(evt, BtnBatal, BtnKeluar);
    }//GEN-LAST:event_BtnCariKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        Valid.tabelKosong(tabMode);
        emptTeks();
        updateCounterObat();
    }//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
    }//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Pasien");
        }else if(KdPetugas.getText().trim().equals("")||NmPetugas.getText().trim().equals("")){
            Valid.textKosong(BtnPetugas,"Petugas");
        }else if(TNoRekonsialiasi.getText().trim().equals("")||TNoRekonsialiasi.getText().trim().equals("-")){
            Valid.textKosong(TNoRekonsialiasi,"No.Rekonsiliasi");
        }else if(tabMode.getRowCount()==0){
            Valid.textKosong(TNoRekonsialiasi,"Data Obat Rekonsiliasi");
        }else{
            reply = JOptionPane.showConfirmDialog(rootPane,"Eeiiiiiits, udah bener belum data yang mau disimpan..??","Konfirmasi",JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                if(Sequel.menyimpantf("rekonsiliasi_obat","?,?,?,?,?,?,?,?,?,?,?","No.Rawat",11,new String[]{
                        TNoRekonsialiasi.getText(),TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+CmbJam.getSelectedItem()+":"+CmbMenit.getSelectedItem()+":"+CmbDetik.getSelectedItem(),
                        RekonsiliasiSaat.getSelectedItem().toString(),AlergiObat.getText(),ManifestasiAlergi.getText(),DampakAlergi.getSelectedItem().toString(),KdPetugas.getText(),
                        pilihRadio(RbObatSebelumAdmisiYa,"Ya",RbObatSebelumAdmisiTidak,"Tidak"),
                        pilihRadio(RbSumberRM,"Status Pasien di Rekam Medis",RbSumberPasien,"Pasien/Keluarga Pasien",RbSumberLain,"Lain-lain"),
                        TSumberLainKet.getText(),
                    })==true){
                        for (i = 0; i < tbPemeriksaan.getRowCount(); i++) {
                           Sequel.menyimpan2("rekonsiliasi_obat_detail_obat","?,?,?,?,?,?,?,?",8,new String[]{
                               TNoRekonsialiasi.getText(),
                               tbPemeriksaan.getValueAt(i,1).toString(),
                               tbPemeriksaan.getValueAt(i,2).toString(),
                               tbPemeriksaan.getValueAt(i,3).toString(),
                               null,null,
                               tbPemeriksaan.getValueAt(i,4).toString(),
                               tbPemeriksaan.getValueAt(i,5).toString()
                           });
                        }
                        Valid.tabelKosong(tabMode);
                        emptTeks();
                        updateCounterObat();
                }
            }
        }
    }//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{Valid.pindah(evt, BtnHapus, BtnBatal);}
    }//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnKeluarRekonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarRekonActionPerformed
        DlgTambahObatRekonsiliasi.dispose();
    }//GEN-LAST:event_BtnKeluarRekonActionPerformed

    private void BtnSimpanRekonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanRekonActionPerformed
        String nm = NamaObat.getText().trim();
        if (nm.equals("Nama obat...") || nm.equals("Pilih nama obat (klik cari)...")) nm = "";
        String dos = DosisObat.getText().trim();
        if (dos.equals("mis. 500mg")) dos = "";
        String frek = Frekuensi.getText().trim();
        if (frek.equals("mis. 3x1")) frek = "";
        String ubah = PerubahanAturanPakai.getText().trim();
        if (ubah.equals("Kosongkan bila tidak berubah")) ubah = "";

        if(nm.equals("")){
            javax.swing.JOptionPane.showMessageDialog(DlgTambahObatRekonsiliasi, "Silakan pilih nama obat terlebih dahulu melalui tombol pencarian!");
            bukaCariObat();
        }else if(dos.equals("")){
            Valid.textKosong(DosisObat,"Dosis Obat");
        }else if(frek.equals("")){
            Valid.textKosong(Frekuensi,"Frekuensi");
        }else{
            String tl = selectedTindakLanjut != null && !selectedTindakLanjut.isEmpty() ? 
                        selectedTindakLanjut : 
                        (TindakLanjut != null && TindakLanjut.getSelectedItem() != null ? TindakLanjut.getSelectedItem().toString() : "Lanjut aturan pakai sama");
            tabMode.addRow(new Object[]{
                "", nm, dos, frek, tl, ubah
            });
            emptTeksTambahRekon();
            updateCounterObat();
            DlgTambahObatRekonsiliasi.dispose();
        }
    }//GEN-LAST:event_BtnSimpanRekonActionPerformed

    private void NamaObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NamaObatKeyPressed
        Valid.pindah(evt,BtnKeluarRekon,DosisObat);
    }//GEN-LAST:event_NamaObatKeyPressed

    private void DosisObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DosisObatKeyPressed
        Valid.pindah(evt,NamaObat,Frekuensi);
    }//GEN-LAST:event_DosisObatKeyPressed

    private void TindakLanjutKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TindakLanjutKeyPressed
        Valid.pindah(evt,AturanPakai,PerubahanAturanPakai);
    }//GEN-LAST:event_TindakLanjutKeyPressed

    private void FrekuensiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_FrekuensiKeyPressed
        Valid.pindah(evt,DosisObat,PemberianTerakhir);
    }//GEN-LAST:event_FrekuensiKeyPressed

    private void AturanPakaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AturanPakaiKeyPressed
        Valid.pindah(evt,PemberianTerakhir,TindakLanjut);
    }//GEN-LAST:event_AturanPakaiKeyPressed

    private void PemberianTerakhirKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PemberianTerakhirKeyPressed
        Valid.pindah(evt,Frekuensi,AturanPakai);
    }//GEN-LAST:event_PemberianTerakhirKeyPressed

    private void PerubahanAturanPakaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PerubahanAturanPakaiKeyPressed
        Valid.pindah(evt,TindakLanjut,BtnSimpanRekon);
    }//GEN-LAST:event_PerubahanAturanPakaiKeyPressed

    private void BtnSimpanRekonKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanRekonKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanRekonActionPerformed(null);
        }else{
            Valid.pindah(evt,PerubahanAturanPakai,BtnKeluarRekon);
        }
    }//GEN-LAST:event_BtnSimpanRekonKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMRekonsiliasiObat dialog = new RMRekonsiliasiObat(new javax.swing.JFrame(), true);
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
    private widget.TextArea AlergiObat;
    private widget.TextBox AturanPakai;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnKeluarRekon;
    private widget.Button BtnPetugas;
    private widget.Button BtnSimpan;
    private widget.Button BtnSimpanRekon;
    private widget.Button BtnTambah;
    private widget.CekBox ChkJln;
    private widget.ComboBox CmbDetik;
    private widget.ComboBox CmbJam;
    private widget.ComboBox CmbMenit;
    private widget.ComboBox DampakAlergi;
    private javax.swing.JDialog DlgTambahObatRekonsiliasi;
    private widget.TextBox DosisObat;
    private widget.TextBox Frekuensi;
    private widget.TextBox Jk;
    private widget.TextBox KdPetugas;
    private widget.TextArea ManifestasiAlergi;
    private widget.TextBox NamaObat;
    private widget.TextBox NmPetugas;
    private widget.PanelBiasa PanelInput;
    private widget.TextBox PemberianTerakhir;
    private widget.TextBox PerubahanAturanPakai;
    private widget.ComboBox RekonsiliasiSaat;
    private widget.ScrollPane Scroll2;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRekonsialiasi;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal Tanggal;
    private widget.TextBox TglLahir;
    private widget.ComboBox TindakLanjut;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame4;
    private widget.Label jLabel105;
    private widget.Label jLabel108;
    private widget.Label jLabel109;
    private widget.Label jLabel11;
    private widget.Label jLabel111;
    private widget.Label jLabel112;
    private widget.Label jLabel113;
    private widget.Label jLabel15;
    private widget.Label jLabel3;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel55;
    private widget.Label jLabel56;
    private widget.Label jLabel6;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private widget.Label jLabel99;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private widget.Label label9;
    private widget.PanelBiasa panelBiasa2;
    private widget.panelisi panelGlass8;
    private widget.ScrollPane scrollPane2;
    private widget.ScrollPane scrollPane3;
    private widget.Table tbPemeriksaan;
    // End of variables declaration//GEN-END:variables

    // =====================================================================
    // [RM 03-06A] "Penggunaan Obat Sebelum Admisi" + "Sumber Informasi" -
    // field baru dari formulir fisik RM 03-06A yang sebelumnya sama sekali
    // tidak punya widget. Ditambahkan terisolasi di luar blok GEN-BEGIN/
    // GEN-END - lihat initRekonsiliasiTambahan(), dipanggil dari konstruktor
    // setelah initComponents().
    // =====================================================================
        // Modern UI components matching user's mockup
    private javax.swing.JPanel pnlTopBar;
    private javax.swing.JPanel pnlCardPatient;
    private javax.swing.JLabel lblCardInitials;
    private javax.swing.JLabel lblCardNama;
    private javax.swing.JLabel lblCardJk;
    private javax.swing.JLabel lblCardNoRM;
    private javax.swing.JLabel lblCardNoRw;
    private javax.swing.JLabel lblCardTglLahir;
    private javax.swing.JButton btnSegAdmisi, btnSegTransfer, btnSegPulang;
    private javax.swing.JPanel pnlGrupAdmisi;
    private javax.swing.JLabel lblCountObat;
    private javax.swing.JLabel lblStatusRekon;
    private javax.swing.JLabel lblFooterMeta;
    private widget.Label lblObatSebelumAdmisi, lblSumberInformasi;
    private widget.RadioButton RbObatSebelumAdmisiYa, RbObatSebelumAdmisiTidak;
    private javax.swing.ButtonGroup bgObatSebelumAdmisi;
    private widget.RadioButton RbSumberRM, RbSumberPasien, RbSumberLain;
    private javax.swing.ButtonGroup bgSumberInformasi;
    private widget.TextBox TSumberLainKet;

    // =====================================================================
    // [Tab "Data Rekonsiliasi Obat"] Restrukturisasi dialog jadi 2 tab
    // (Input/Data) - lihat initDataRekonsiliasiTab(), dipanggil dari
    // konstruktor. Pola identik "Data Triase" milik RMTriaseIGD.java
    // (TabPilihan/PanelAccor/ChkAccor/FormMenu/ScrollHTML/LoadHTML).
    // =====================================================================
    private javax.swing.JTabbedPane TabPilihan;
    private widget.InternalFrame internalFrameData;
    private widget.TextBox TCariData;
    private widget.Tanggal Tgl1Data, Tgl2Data;
    private widget.Button BtnCariData, BtnAllData;
    private widget.Table tbDataRekonsiliasi;
    private widget.PanelBiasa PanelAccor, FormMenu;
    private widget.CekBox ChkAccor;
    private widget.Label TNoRM1, TPasien1;
    private widget.ScrollPane ScrollHTML;
    private widget.editorpane LoadHTML;
    private widget.Button BtnPrintRekon;

    /**
     * [RM 03-06A] Penyesuaian form dgn formulir fisik "REKONSILIASI OBAT"
     * (RSUD Demang Sepulau Raya):
     * 1) Sembunyikan field yg tidak ada di lembar fisik ini (Alergi Obat/
     *    Manifestasi Alergi/Dampak Alergi) - TIDAK dihapus, cuma
     *    setVisible(false) (additive-only, kolom DB tetap ada).
     * 2) "Rekonsiliasi Saat" TETAP bisa dipilih bebas (dikoreksi user -
     *    rekonsiliasi obat py 3 momen proses: Admisi/Transfer/Discharge),
     *    model dipangkas dari 4 opsi jadi 3 ("Admisi"/"Transfer Antar
     *    Ruang"/"Pulang") - "Pindah Faskes Lain" dibuang dari pilihan
     *    (bukan dihapus dari DB, cuma tidak lagi ditawarkan di combo).
     * 3) Label "Petugas :" -> "Apoteker :" (sesuai kolom tanda tangan
     *    fisik "Apoteker yang melakukan rekonsiliasi obat").
     * 4) Model combo TindakLanjut (di DlgTambahObatRekonsiliasi) diperluas
     *    dari 2 opsi (Stop/Lanjut) jadi 3 opsi sesuai fisik.
     * 5) 2 baris baru ditambahkan ke PanelInput (null-layout, .setBounds()
     *    absolut - konsisten gaya file ini): "Penggunaan Obat Sebelum
     *    Admisi" & "Sumber Informasi".
     * 6) PanelInput ditata ulang total jadi kartu "Patient Banner" (putih +
     *    TitledBorder, grid rapat 28px/baris tanpa celah) - pola sama
     *    RMTriaseIGD.java Fase 4. WAJIB krn PanelInput pakai null-layout:
     *    menyembunyikan field (poin 1) TIDAK bikin baris di bawahnya
     *    otomatis naik, jadi SEMUA field (lama+baru) direposisi eksplisit.
     * Dipanggil sekali dari constructor, setelah initComponents().
     */
    private void initRekonsiliasiTambahan(){
        jLabel55.setVisible(false);
        AlergiObat.setVisible(false);
        scrollPane2.setVisible(false);
        jLabel56.setVisible(false);
        ManifestasiAlergi.setVisible(false);
        scrollPane3.setVisible(false);
        jLabel6.setVisible(false);
        DampakAlergi.setVisible(false);

        RekonsiliasiSaat.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            "Admisi","Transfer Antar Ruang","Pulang"
        }));

        jLabel9.setText("Apoteker :");

        jLabel111.setVisible(false);
        AturanPakai.setVisible(false);
        jLabel112.setVisible(false);
        PemberianTerakhir.setVisible(false);

        TindakLanjut.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            "Lanjut aturan pakai sama","Lanjut aturan pakai berubah","Stop"
        }));

        lblObatSebelumAdmisi = new widget.Label();
        lblObatSebelumAdmisi.setText("Penggunaan Obat Sebelum Admisi");

        RbObatSebelumAdmisiYa = new widget.RadioButton();
        RbObatSebelumAdmisiYa.setText("Ya, Dengan Rincian Sebagai Berikut");
        RbObatSebelumAdmisiYa.addActionListener(e -> updateStatusRekon());

        RbObatSebelumAdmisiTidak = new widget.RadioButton();
        RbObatSebelumAdmisiTidak.setText("Tidak Menggunakan Obat Sebelum Admisi");
        RbObatSebelumAdmisiTidak.addActionListener(e -> updateStatusRekon());

        bgObatSebelumAdmisi = new javax.swing.ButtonGroup();
        bgObatSebelumAdmisi.add(RbObatSebelumAdmisiYa);
        bgObatSebelumAdmisi.add(RbObatSebelumAdmisiTidak);

        lblSumberInformasi = new widget.Label();
        lblSumberInformasi.setText("Sumber Informasi");

        RbSumberRM = new widget.RadioButton();
        RbSumberRM.setText("Status Pasien di Rekam Medis");
        RbSumberRM.addActionListener(e -> updateStatusRekon());

        RbSumberPasien = new widget.RadioButton();
        RbSumberPasien.setText("Pasien/Keluarga Pasien");
        RbSumberPasien.addActionListener(e -> updateStatusRekon());

        RbSumberLain = new widget.RadioButton();
        RbSumberLain.setText("Lain-lain");
        RbSumberLain.addActionListener(e -> updateStatusRekon());

        bgSumberInformasi = new javax.swing.ButtonGroup();
        bgSumberInformasi.add(RbSumberRM);
        bgSumberInformasi.add(RbSumberPasien);
        bgSumberInformasi.add(RbSumberLain);

        TSumberLainKet = new widget.TextBox();

        RekonsiliasiSaat.addItemListener((java.awt.event.ItemEvent e) -> {
            if(e.getStateChange()==java.awt.event.ItemEvent.SELECTED){
                perbaruiVisibilitasFieldAdmisi();
                updateSegmentedButtons();
                updateStatusRekon();
            }
        });
    }

    private void initModernLayout() {
        // 1. Top Header Bar (Dark Teal #0e3d36)
        pnlTopBar = new JPanel(new BorderLayout());
        pnlTopBar.setBackground(new Color(14, 61, 54));
        pnlTopBar.setPreferredSize(new Dimension(1320, 36));
        pnlTopBar.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel lblTitle = new JLabel("Rekonsiliasi Obat");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(Color.WHITE);
        pnlTopBar.add(lblTitle, BorderLayout.WEST);

        JLabel lblCode = new JLabel("RM 03-06A");
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCode.setForeground(new Color(128, 165, 159));
        pnlTopBar.add(lblCode, BorderLayout.EAST);

        // 2. TabbedPane setup with FlatLaf styling
        TabPilihan = new javax.swing.JTabbedPane();
        TabPilihan.setFont(new Font("Segoe UI", Font.BOLD, 11));
        TabPilihan.putClientProperty("JTabbedPane.tabHeight", 34);
        TabPilihan.putClientProperty("JTabbedPane.tabInsets", new Insets(0, 16, 0, 16));
        TabPilihan.putClientProperty("JTabbedPane.underlineColor", new Color(0, 91, 82));
        TabPilihan.putClientProperty("JTabbedPane.selectedBackground", Color.WHITE);
        TabPilihan.putClientProperty("JTabbedPane.selectedForeground", new Color(0, 91, 82));
        TabPilihan.putClientProperty("JTabbedPane.showTabSeparators", false);

        // 3. TAB 1: Reconstruct internalFrame1 (Input Tab)
        internalFrame1.removeAll();
        internalFrame1.setBorder(null);
        internalFrame1.setLayout(new BorderLayout(0, 8));
        internalFrame1.setBackground(Color.WHITE);
        internalFrame1.setBorder(new EmptyBorder(10, 16, 10, 16));

        // Top Section: Patient Card (West) + Form Controls (Center)
        JPanel pnlTopSection = new JPanel(new BorderLayout(14, 0));
        pnlTopSection.setBackground(Color.WHITE);
        pnlTopSection.setPreferredSize(new Dimension(1320, 165));

        // LEFT: Patient Identity Card
        pnlCardPatient = createPatientCard();
        pnlTopSection.add(pnlCardPatient, BorderLayout.WEST);

        // RIGHT: Header Form Controls
        JPanel pnlRightHeader = createRightHeaderPanel();
        pnlTopSection.add(pnlRightHeader, BorderLayout.CENTER);

        internalFrame1.add(pnlTopSection, BorderLayout.NORTH);

        // MIDDLE: Table Section
        JPanel pnlTableSection = createTableSection();
        internalFrame1.add(pnlTableSection, BorderLayout.CENTER);

        // BOTTOM: Action Bar & Footer
        JPanel pnlBottomSection = createBottomSection();
        internalFrame1.add(pnlBottomSection, BorderLayout.SOUTH);

        // Add Tabs
        TabPilihan.addTab("Input Rekonsiliasi Obat", internalFrame1);
        initDataRekonsiliasiTab();

        // Outer layout on JDialog
        getContentPane().removeAll();
        JPanel pnlRoot = new JPanel(new BorderLayout(0, 0));
        pnlRoot.setBackground(Color.WHITE);
        pnlRoot.add(pnlTopBar, BorderLayout.PAGE_START);
        pnlRoot.add(TabPilihan, BorderLayout.CENTER);
        getContentPane().add(pnlRoot, BorderLayout.CENTER);

        // Initial updates
        perbaruiVisibilitasFieldAdmisi();
        updateSegmentedButtons();
        updatePatientCard();
        updateCounterObat();
        updateStatusRekon();
        updateFooterMeta();
    }

    private JPanel createPatientCard() {
        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(245, 160));

        JLabel lblHeader = new JLabel("IDENTITAS PASIEN");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblHeader.setForeground(new Color(100, 116, 139));
        lblHeader.setBounds(12, 10, 130, 16);
        card.add(lblHeader);

        JLabel lblBadge = new JLabel("TERKUNCI", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 8));
        lblBadge.setForeground(new Color(71, 85, 105));
        lblBadge.setBounds(175, 9, 58, 16);
        card.add(lblBadge);

        lblCardInitials = new JLabel("-", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(204, 251, 241));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblCardInitials.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCardInitials.setForeground(new Color(0, 91, 82));
        lblCardInitials.setBounds(12, 34, 34, 34);
        card.add(lblCardInitials);

        lblCardNama = new JLabel("Nama Pasien");
        lblCardNama.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCardNama.setForeground(new Color(15, 23, 42));
        lblCardNama.setBounds(54, 34, 180, 18);
        card.add(lblCardNama);

        lblCardJk = new JLabel("-");
        lblCardJk.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCardJk.setForeground(new Color(100, 116, 139));
        lblCardJk.setBounds(54, 52, 180, 14);
        card.add(lblCardJk);

        int y = 78;
        addCardKeyValue(card, "No. RM", lblCardNoRM = new JLabel("-"), y);
        addCardKeyValue(card, "No. Rawat", lblCardNoRw = new JLabel("-"), y + 18);
        addCardKeyValue(card, "Tgl. Lahir", lblCardTglLahir = new JLabel("-"), y + 36);

        return card;
    }

    private void addCardKeyValue(JPanel p, String key, JLabel valLabel, int y) {
        JLabel k = new JLabel(key);
        k.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        k.setForeground(new Color(100, 116, 139));
        k.setBounds(12, y, 65, 15);
        p.add(k);

        valLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        valLabel.setForeground(new Color(15, 23, 42));
        valLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valLabel.setBounds(85, y, 148, 15);
        p.add(valLabel);
    }

    private JPanel createRightHeaderPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);

        // Row 1: 3 fields (Apoteker, No.Rekonsiliasi, Tanggal & Jam Wawancara)
        JLabel lApoteker = new JLabel("Apoteker");
        lApoteker.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lApoteker.setForeground(new Color(100, 116, 139));
        lApoteker.setBounds(0, 0, 120, 14);
        p.add(lApoteker);

        JPanel pnlApotekerInput = new JPanel(new BorderLayout(2, 0));
        pnlApotekerInput.setOpaque(false);
        pnlApotekerInput.setBounds(0, 16, 265, 26);
        NmPetugas.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        NmPetugas.putClientProperty("Component.arc", 6);
        NmPetugas.putClientProperty("ModernStyle.active", Boolean.TRUE);
        BtnPetugas.setPreferredSize(new Dimension(28, 26));
        BtnPetugas.putClientProperty("Component.arc", 6);
        pnlApotekerInput.add(NmPetugas, BorderLayout.CENTER);
        pnlApotekerInput.add(BtnPetugas, BorderLayout.EAST);
        p.add(pnlApotekerInput);

        JLabel lNoRekon = new JLabel("No.Rekonsiliasi");
        lNoRekon.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lNoRekon.setForeground(new Color(100, 116, 139));
        lNoRekon.setBounds(280, 0, 120, 14);
        p.add(lNoRekon);

        TNoRekonsialiasi.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        TNoRekonsialiasi.putClientProperty("Component.arc", 6);
        TNoRekonsialiasi.putClientProperty("ModernStyle.active", Boolean.TRUE);
        TNoRekonsialiasi.setBounds(280, 16, 265, 26);
        p.add(TNoRekonsialiasi);

        JLabel lTglJam = new JLabel("Tanggal & Jam Wawancara");
        lTglJam.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lTglJam.setForeground(new Color(100, 116, 139));
        lTglJam.setBounds(560, 0, 180, 14);
        p.add(lTglJam);

        JPanel pnlTglJam = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
        pnlTglJam.setOpaque(true);
        pnlTglJam.setBackground(Color.WHITE);
        pnlTglJam.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(0, 4, 0, 4)
        ));
        pnlTglJam.setBounds(560, 16, 265, 26);
        Tanggal.setPreferredSize(new Dimension(105, 22));
        CmbJam.setPreferredSize(new Dimension(50, 22));
        CmbMenit.setPreferredSize(new Dimension(50, 22));
        JLabel colon = new JLabel(":");
        colon.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pnlTglJam.add(Tanggal);
        pnlTglJam.add(CmbJam);
        pnlTglJam.add(colon);
        pnlTglJam.add(CmbMenit);
        p.add(pnlTglJam);

        // Row 2: Rekonsiliasi Saat
        JLabel lSaat = new JLabel("Rekonsiliasi Saat");
        lSaat.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lSaat.setForeground(new Color(100, 116, 139));
        lSaat.setBounds(0, 48, 120, 14);
        p.add(lSaat);

        JPanel segPills = new JPanel(new GridLayout(1, 3, -1, 0));
        segPills.setBounds(0, 64, 375, 26);
        segPills.setOpaque(false);

        btnSegAdmisi = createSegmentedButton("Admisi");
        btnSegTransfer = createSegmentedButton("Transfer Antar Ruang");
        btnSegPulang = createSegmentedButton("Pulang");
        segPills.add(btnSegAdmisi);
        segPills.add(btnSegTransfer);
        segPills.add(btnSegPulang);
        p.add(segPills);

        // Row 3: Grup Admisi (Penggunaan Obat Sebelum Admisi & Sumber Informasi)
        pnlGrupAdmisi = new JPanel(null);
        pnlGrupAdmisi.setBackground(Color.WHITE);
        pnlGrupAdmisi.setBounds(0, 96, 950, 65);

        JLabel lObat = new JLabel("Penggunaan Obat Sebelum Admisi");
        lObat.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lObat.setForeground(new Color(15, 23, 42));
        lObat.setBounds(0, 0, 250, 15);
        pnlGrupAdmisi.add(lObat);

        styleRadioButton(RbObatSebelumAdmisiYa, true);
        RbObatSebelumAdmisiYa.setBounds(0, 17, 210, 20);
        pnlGrupAdmisi.add(RbObatSebelumAdmisiYa);

        styleRadioButton(RbObatSebelumAdmisiTidak, false);
        RbObatSebelumAdmisiTidak.setBounds(215, 17, 250, 20);
        pnlGrupAdmisi.add(RbObatSebelumAdmisiTidak);

        JLabel lSumber = new JLabel("Sumber Informasi");
        lSumber.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lSumber.setForeground(new Color(15, 23, 42));
        lSumber.setBounds(480, 0, 200, 15);
        pnlGrupAdmisi.add(lSumber);

        styleRadioButton(RbSumberRM, false);
        RbSumberRM.setBounds(480, 17, 180, 20);
        pnlGrupAdmisi.add(RbSumberRM);

        styleRadioButton(RbSumberPasien, true);
        RbSumberPasien.setBounds(665, 17, 150, 20);
        pnlGrupAdmisi.add(RbSumberPasien);

        styleRadioButton(RbSumberLain, false);
        RbSumberLain.setBounds(820, 17, 80, 20);
        pnlGrupAdmisi.add(RbSumberLain);

        TSumberLainKet.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        TSumberLainKet.setBounds(480, 39, 160, 22);
        TSumberLainKet.setBackground(new Color(248, 250, 252));
        TSumberLainKet.putClientProperty("Component.arc", 6);
        TSumberLainKet.putClientProperty("ModernStyle.active", Boolean.TRUE);
        TSumberLainKet.setText("Sebutkan...");
        TSumberLainKet.setForeground(new Color(148, 163, 184));
        TSumberLainKet.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if ("Sebutkan...".equals(TSumberLainKet.getText())) {
                    TSumberLainKet.setText("");
                    TSumberLainKet.setForeground(new Color(15, 23, 42));
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (TSumberLainKet.getText().trim().isEmpty()) {
                    TSumberLainKet.setText("Sebutkan...");
                    TSumberLainKet.setForeground(new Color(148, 163, 184));
                }
            }
        });
        pnlGrupAdmisi.add(TSumberLainKet);

        p.add(pnlGrupAdmisi);

        return p;
    }

    private JButton createSegmentedButton(final String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isSel = text.equalsIgnoreCase(RekonsiliasiSaat.getSelectedItem() != null ? RekonsiliasiSaat.getSelectedItem().toString() : "");
                if (isSel) {
                    g2.setColor(new Color(0, 91, 82));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(new Color(226, 232, 240));
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.addActionListener(e -> {
            RekonsiliasiSaat.setSelectedItem(text);
            updateSegmentedButtons();
            perbaruiVisibilitasFieldAdmisi();
            updateStatusRekon();
        });
        return b;
    }

    private void updateSegmentedButtons() {
        if (btnSegAdmisi == null) return;
        String saat = RekonsiliasiSaat.getSelectedItem() != null ? RekonsiliasiSaat.getSelectedItem().toString() : "Admisi";
        btnSegAdmisi.setFont(new Font("Segoe UI", "Admisi".equalsIgnoreCase(saat) ? Font.BOLD : Font.PLAIN, 10));
        btnSegAdmisi.setForeground("Admisi".equalsIgnoreCase(saat) ? Color.WHITE : new Color(15, 23, 42));
        btnSegTransfer.setFont(new Font("Segoe UI", "Transfer Antar Ruang".equalsIgnoreCase(saat) ? Font.BOLD : Font.PLAIN, 10));
        btnSegTransfer.setForeground("Transfer Antar Ruang".equalsIgnoreCase(saat) ? Color.WHITE : new Color(15, 23, 42));
        btnSegPulang.setFont(new Font("Segoe UI", "Pulang".equalsIgnoreCase(saat) ? Font.BOLD : Font.PLAIN, 10));
        btnSegPulang.setForeground("Pulang".equalsIgnoreCase(saat) ? Color.WHITE : new Color(15, 23, 42));
        btnSegAdmisi.repaint();
        btnSegTransfer.repaint();
        btnSegPulang.repaint();
    }

    private JPanel createTableSection() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(Color.WHITE);

        JPanel pnlTableHeader = new JPanel(new BorderLayout());
        pnlTableHeader.setBackground(Color.WHITE);

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlTitle.setBackground(Color.WHITE);
        JLabel lTitle = new JLabel("DAFTAR OBAT DIREKONSILIASI");
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lTitle.setForeground(new Color(15, 23, 42));
        pnlTitle.add(lTitle);

        lblCountObat = new JLabel("0 obat");
        lblCountObat.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCountObat.setForeground(new Color(100, 116, 139));
        pnlTitle.add(lblCountObat);
        pnlTableHeader.add(pnlTitle, BorderLayout.WEST);

        JPanel pnlTableBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlTableBtns.setBackground(Color.WHITE);

        styleOutlineButton(BtnHapus);
        BtnHapus.setText("Hapus");
        BtnHapus.setPreferredSize(new Dimension(75, 26));
        pnlTableBtns.add(BtnHapus);

        styleSolidTealButton(BtnTambah);
        BtnTambah.setText("+ Tambah Obat");
        BtnTambah.setPreferredSize(new Dimension(120, 26));
        pnlTableBtns.add(BtnTambah);

        pnlTableHeader.add(pnlTableBtns, BorderLayout.EAST);
        p.add(pnlTableHeader, BorderLayout.NORTH);

        Scroll2.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249)));
        Scroll2.getViewport().setBackground(Color.WHITE);
        p.add(Scroll2, BorderLayout.CENTER);

        return p;
    }

    private JPanel createBottomSection() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(Color.WHITE);

        JPanel pnlActRow = new JPanel(new BorderLayout());
        pnlActRow.setBackground(Color.WHITE);

        JPanel pnlLeftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlLeftBtns.setBackground(Color.WHITE);

        styleSolidTealButton(BtnSimpan);
        BtnSimpan.setText("Simpan");
        BtnSimpan.setPreferredSize(new Dimension(85, 28));
        pnlLeftBtns.add(BtnSimpan);

        styleOutlineButton(BtnBatal);
        BtnBatal.setText("Clear");
        BtnBatal.setPreferredSize(new Dimension(75, 28));
        pnlLeftBtns.add(BtnBatal);

        widget.Button btnCetakForm = new widget.Button();
        styleOutlineButton(btnCetakForm);
        btnCetakForm.setText("Cetak Form (RM 03-06A)");
        btnCetakForm.setPreferredSize(new Dimension(180, 28));
        btnCetakForm.addActionListener(e -> cetakFormDariInput());
        pnlLeftBtns.add(btnCetakForm);

        pnlActRow.add(pnlLeftBtns, BorderLayout.WEST);

        JPanel pnlRightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRightBtns.setBackground(Color.WHITE);

        lblStatusRekon = new JLabel("Rekonsiliasi saat Admisi");
        lblStatusRekon.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblStatusRekon.setForeground(new Color(100, 116, 139));
        pnlRightBtns.add(lblStatusRekon);

        styleOutlineButton(BtnKeluar);
        BtnKeluar.setText("Keluar");
        BtnKeluar.setPreferredSize(new Dimension(75, 28));
        pnlRightBtns.add(BtnKeluar);

        pnlActRow.add(pnlRightBtns, BorderLayout.EAST);
        p.add(pnlActRow, BorderLayout.NORTH);

        JPanel pnlMetaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        pnlMetaRow.setBackground(Color.WHITE);
        lblFooterMeta = new JLabel("-");
        lblFooterMeta.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblFooterMeta.setForeground(new Color(148, 163, 184));
        pnlMetaRow.add(lblFooterMeta);
        p.add(pnlMetaRow, BorderLayout.SOUTH);

        return p;
    }

    private void cetakFormDariInput() {
        String noRekon = TNoRekonsialiasi.getText().trim();
        if (noRekon.isEmpty() || noRekon.equals("-")) {
            JOptionPane.showMessageDialog(null, "Pilih atau simpan terlebih dahulu data rekonsiliasi yang mau dicetak.");
            return;
        }
        String exists = Sequel.cariIsi("select no_rekonsiliasi from rekonsiliasi_obat where no_rekonsiliasi=?", noRekon);
        if (exists == null || exists.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Simpan data rekonsiliasi ini terlebih dahulu sebelum mencetak.");
            return;
        }
        cetakSatuTahapRekonsiliasi(noRekon);
    }

    public void updatePatientCard() {
        if (lblCardNama == null) return;
        String nama = TPasien.getText().trim();
        if (nama.isEmpty()) {
            lblCardInitials.setText("-");
            lblCardNama.setText("Nama Pasien");
        } else {
            lblCardInitials.setText(getInitials(nama));
            lblCardNama.setText(nama);
        }
        lblCardJk.setText(Jk.getText().isEmpty() ? "-" : Jk.getText());
        lblCardNoRM.setText(TNoRM.getText().isEmpty() ? "-" : TNoRM.getText());
        lblCardNoRw.setText(TNoRw.getText().isEmpty() ? "-" : TNoRw.getText());
        lblCardTglLahir.setText(TglLahir.getText().isEmpty() ? "-" : TglLahir.getText());
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "-";
        String clean = name.trim().replaceAll("(?i)^(tn\\.|ny\\.|nn\\.|an\\.|by\\.)\\s*", "");
        String[] words = clean.split("\\s+");
        if (words.length == 0 || words[0].isEmpty()) return "-";
        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        } else {
            return ("" + words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
        }
    }

    public void updateCounterObat() {
        if (lblCountObat != null && tabMode != null) {
            lblCountObat.setText(tabMode.getRowCount() + " obat");
        }
    }

    public void updateStatusRekon() {
        if (lblStatusRekon == null) return;
        String saat = RekonsiliasiSaat.getSelectedItem() != null ? RekonsiliasiSaat.getSelectedItem().toString() : "Admisi";
        String sumber = pilihRadio(RbSumberRM, "Status Pasien di Rekam Medis", RbSumberPasien, "Pasien/Keluarga Pasien", RbSumberLain, "Lain-lain");
        if (sumber != null && !sumber.isEmpty()) {
            lblStatusRekon.setText("Rekonsiliasi saat " + saat + " · Sumber: " + sumber);
        } else {
            lblStatusRekon.setText("Rekonsiliasi saat " + saat);
        }
    }

    public void updateFooterMeta() {
        if (lblFooterMeta == null) return;
        String pet = NmPetugas.getText().trim();
        if (pet.isEmpty() && !akses.getkode().equals("")) {
            try {
                pet = Sequel.cariIsi("select nama from pegawai where nik=?", akses.getkode());
            } catch (Exception e) {}
        }
        if (pet == null || pet.isEmpty()) pet = "Petugas Farmasi";

        String tgl = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String host = koneksiDB.HOST();
        if (host == null || host.isEmpty() || host.equals("localhost")) {
            try {
                host = java.net.InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                host = "127.0.0.1";
            }
        }
        lblFooterMeta.setText(pet + "   ·   " + tgl + "   ·   " + host);
    }

    private void perbaruiVisibilitasFieldAdmisi(){
        boolean admisi = "Admisi".equals(RekonsiliasiSaat.getSelectedItem());
        if(pnlGrupAdmisi != null){
            pnlGrupAdmisi.setVisible(admisi);
        }
        if(!admisi){
            bgObatSebelumAdmisi.clearSelection();
            bgSumberInformasi.clearSelection();
            TSumberLainKet.setText("");
        }
        updateStatusRekon();
    }

    // Custom Table Renderers
    static class RadioCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JPanel p = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    int r = 6;
                    boolean sel = (row == table.getSelectedRow());
                    if (sel) {
                        g2.setColor(new Color(0, 91, 82));
                        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                        g2.fillOval(cx - 3, cy - 3, 6, 6);
                    } else {
                        g2.setColor(new Color(203, 213, 225));
                        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                    }
                    g2.dispose();
                }
            };
            p.setOpaque(true);
            boolean sel = (row == table.getSelectedRow());
            p.setBackground(sel ? new Color(240, 253, 250) : Color.WHITE);
            return p;
        }
    }

    static class BoldTextCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            l.setForeground(new Color(15, 23, 42));
            boolean sel = (row == table.getSelectedRow());
            l.setBackground(sel ? new Color(240, 253, 250) : Color.WHITE);
            return l;
        }
    }

    private void styleSolidTealButton(widget.Button btn) {
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setIcon(null);
        btn.setMnemonic(0);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton b = (AbstractButton) c;
                if (b.getModel().isPressed()) {
                    g2.setColor(new Color(0, 75, 68));
                } else if (b.getModel().isRollover()) {
                    g2.setColor(new Color(0, 105, 95));
                } else {
                    g2.setColor(new Color(0, 91, 82));
                }
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 6, 6);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    private void styleOutlineButton(widget.Button btn) {
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setIcon(null);
        btn.setMnemonic(0);
        btn.setForeground(new Color(51, 65, 85));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton b = (AbstractButton) c;
                if (b.getModel().isPressed()) {
                    g2.setColor(new Color(241, 245, 249));
                } else if (b.getModel().isRollover()) {
                    g2.setColor(new Color(248, 250, 252));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 6, 6);
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 6, 6);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    private static class ModernRadioIcon implements javax.swing.Icon {
        private final boolean selected;
        public ModernRadioIcon(boolean selected) { this.selected = selected; }
        @Override public int getIconWidth() { return 14; }
        @Override public int getIconHeight() { return 14; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillOval(x + 1, y + 1, 12, 12);
            g2.setColor(selected ? new Color(0, 91, 82) : new Color(203, 213, 225));
            g2.setStroke(new BasicStroke(selected ? 1.5f : 1.2f));
            g2.drawOval(x + 1, y + 1, 12, 12);
            if (selected) {
                g2.setColor(new Color(0, 91, 82));
                g2.fillOval(x + 4, y + 4, 6, 6);
            }
            g2.dispose();
        }
    }

    private void styleRadioButton(widget.RadioButton rb, boolean isBold) {
        rb.setOpaque(false);
        rb.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 10));
        rb.setForeground(new Color(15, 23, 42));
        rb.setIcon(new ModernRadioIcon(false));
        rb.setSelectedIcon(new ModernRadioIcon(true));
        rb.setFocusPainted(false);
    }

    static class ModernTextCellRenderer extends DefaultTableCellRenderer {
        private final boolean isDash;
        private final boolean isCentered;
        public ModernTextCellRenderer(boolean isDash, boolean isCentered) {
            this.isDash = isDash;
            this.isCentered = isCentered;
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            l.setForeground(new Color(15, 23, 42));
            l.setHorizontalAlignment(isCentered ? SwingConstants.CENTER : SwingConstants.LEFT);
            l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            String text = (value == null) ? "" : value.toString().trim();
            if (isDash && (text.isEmpty() || text.equals("-"))) {
                text = "—";
                l.setForeground(new Color(148, 163, 184));
            }
            l.setText(text);
            boolean sel = (row == table.getSelectedRow());
            l.setBackground(sel ? new Color(240, 253, 250) : Color.WHITE);
            return l;
        }
    }

    static class BadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String val = value == null ? "" : value.toString();
            final Color bg, fg;
            if (val.toLowerCase().contains("sama")) {
                bg = new Color(220, 252, 231); // #dcfce7
                fg = new Color(21, 128, 61);   // #15803d
            } else if (val.toLowerCase().contains("berubah")) {
                bg = new Color(254, 243, 199); // #fef3c7
                fg = new Color(180, 83, 9);    // #b45309
            } else {
                bg = new Color(254, 226, 226); // #fee2e2
                fg = new Color(185, 28, 28);   // #b91c1c
            }

            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
            p.setOpaque(true);
            boolean sel = (row == table.getSelectedRow());
            p.setBackground(sel ? new Color(240, 253, 250) : Color.WHITE);

            JLabel badge = new JLabel(val, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            badge.setForeground(fg);
            badge.setBorder(new EmptyBorder(2, 8, 2, 8));
            p.add(badge);
            return p;
        }
    }

    /** Helper generik utk baca pilihan 1 dari 2 RadioButton (null kalau belum ada yg dipilih). */
    private String pilihRadio(widget.RadioButton a,String na,widget.RadioButton b,String nb){
        return a.isSelected()?na:(b.isSelected()?nb:null);
    }

    /** Helper generik utk baca pilihan 1 dari 3 RadioButton (null kalau belum ada yg dipilih). */
    private String pilihRadio(widget.RadioButton a,String na,widget.RadioButton b,String nb,widget.RadioButton c,String nc){
        return a.isSelected()?na:(b.isSelected()?nb:(c.isSelected()?nc:null));
    }

    /**
     * [Tab "Data Rekonsiliasi Obat"] Dibangun dari nol (bukan reuse
     * RMCariRekonsiliasiObat.java - class itu JDialog utuh, tidak bisa
     * ditempel sbg tab, & TETAP dipertahankan utuh krn dipakai jg langsung
     * dari frmUtama.java). Pola identik "Data Triase" milik RMTriaseIGD.java:
     * panel cari (PAGE_START) + tabel bersih 1-baris-per-record (CENTER) +
     * panel ringkasan HTML yg bisa dilipat/dibuka (EAST, ChkAccor/PanelAccor/
     * FormMenu/ScrollHTML/LoadHTML). Dipanggil sekali dari constructor.
     */
    private void initDataRekonsiliasiTab(){
        internalFrameData = new widget.InternalFrame();
        internalFrameData.setLayout(new java.awt.BorderLayout(1,1));

        // ----- Panel cari (PAGE_START) -----
        widget.PanelBiasa panelCariData = new widget.PanelBiasa();
        panelCariData.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,6,6));

        widget.Label lblCariData = new widget.Label();
        lblCariData.setText("Cari (No.Rawat/No.RM/Nama/Petugas) :");
        panelCariData.add(lblCariData);

        TCariData = new widget.TextBox();
        TCariData.setPreferredSize(new java.awt.Dimension(200,23));
        panelCariData.add(TCariData);

        widget.Label lblTglData = new widget.Label();
        lblTglData.setText("Tanggal :");
        panelCariData.add(lblTglData);

        Tgl1Data = new widget.Tanggal();
        Tgl1Data.setDisplayFormat("dd-MM-yyyy");
        Tgl1Data.setDate(new Date());
        panelCariData.add(Tgl1Data);

        widget.Label lblSdData = new widget.Label();
        lblSdData.setText("s/d");
        panelCariData.add(lblSdData);

        Tgl2Data = new widget.Tanggal();
        Tgl2Data.setDisplayFormat("dd-MM-yyyy");
        Tgl2Data.setDate(new Date());
        panelCariData.add(Tgl2Data);

        BtnCariData = new widget.Button();
        BtnCariData.setText("Cari");
        BtnCariData.addActionListener((java.awt.event.ActionEvent evt) -> tampilDataRekonsiliasi());
        panelCariData.add(BtnCariData);

        BtnAllData = new widget.Button();
        BtnAllData.setText("Tampil Semua");
        BtnAllData.addActionListener((java.awt.event.ActionEvent evt) -> {
            TCariData.setText("");
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.YEAR,-10);
            Tgl1Data.setDate(cal.getTime());
            cal.add(java.util.Calendar.YEAR,20);
            Tgl2Data.setDate(cal.getTime());
            tampilDataRekonsiliasi();
        });
        panelCariData.add(BtnAllData);

        // Panel cari ditaruh di PAGE_END (footer) - pola sama panelGlass9 di
        // "Data Triase" RMTriaseIGD.java, bukan di header/PAGE_START.
        internalFrameData.add(panelCariData, java.awt.BorderLayout.PAGE_END);

        // ----- Tabel bersih: 1 baris = 1 record (CENTER) -----
        tabModeData = new DefaultTableModel(null, new Object[]{
            "No.Rekonsiliasi","Pasien","Tanggal Wawancara","Rekonsiliasi Saat","Apoteker"
        }){
            @Override public boolean isCellEditable(int rowIndex,int colIndex){ return false; }
        };
        tbDataRekonsiliasi = new widget.Table();
        tbDataRekonsiliasi.setModel(tabModeData);
        tbDataRekonsiliasi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbDataRekonsiliasi.setDefaultRenderer(Object.class, new WarnaTable());
        tbDataRekonsiliasi.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseClicked(java.awt.event.MouseEvent evt){
                tbDataRekonsiliasiMouseClicked(evt);
            }
        });
        for(int kolom=0; kolom<5; kolom++){
            TableColumn column = tbDataRekonsiliasi.getColumnModel().getColumn(kolom);
            if(kolom==0){ column.setPreferredWidth(120); }
            else if(kolom==1){ column.setPreferredWidth(280); }
            else if(kolom==2){ column.setPreferredWidth(130); }
            else if(kolom==3){ column.setPreferredWidth(140); }
            else if(kolom==4){ column.setPreferredWidth(200); }
        }
        widget.ScrollPane ScrollDataRekonsiliasi = new widget.ScrollPane();
        ScrollDataRekonsiliasi.setViewportView(tbDataRekonsiliasi);
        internalFrameData.add(ScrollDataRekonsiliasi, java.awt.BorderLayout.CENTER);

        // ----- Panel ringkasan (EAST) - pola identik "Data Triase" RMTriaseIGD -----
        PanelAccor = new widget.PanelBiasa();
        PanelAccor.setBackground(new java.awt.Color(255,255,255));
        PanelAccor.setPreferredSize(new java.awt.Dimension(470,43));
        PanelAccor.setLayout(new java.awt.BorderLayout(1,1));

        ChkAccor = new widget.CekBox();
        ChkAccor.setBackground(new java.awt.Color(255,250,250));
        ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png")));
        ChkAccor.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png")));
        ChkAccor.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png")));
        ChkAccor.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png")));
        ChkAccor.setFocusable(false);
        ChkAccor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkAccor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkAccor.setPreferredSize(new java.awt.Dimension(15,20));
        ChkAccor.addActionListener((java.awt.event.ActionEvent evt) -> isMenu());
        PanelAccor.add(ChkAccor, java.awt.BorderLayout.WEST);

        FormMenu = new widget.PanelBiasa();
        FormMenu.setBackground(new java.awt.Color(255,255,255));
        FormMenu.setBorder(null);
        FormMenu.setPreferredSize(new java.awt.Dimension(115,43));
        FormMenu.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,4,9));

        widget.Label lblPasienAccor = new widget.Label();
        lblPasienAccor.setText("Pasien :");
        FormMenu.add(lblPasienAccor);

        TNoRM1 = new widget.Label();
        TNoRM1.setPreferredSize(new java.awt.Dimension(70,23));
        FormMenu.add(TNoRM1);

        TPasien1 = new widget.Label();
        TPasien1.setPreferredSize(new java.awt.Dimension(140,23));
        FormMenu.add(TPasien1);

        // [BARU] Cetak form RM 03-06A - pola identik BtnPrint1 RMTriaseIGD
        // (icon-only, tanpa teks - lihat widget.Button, icon TIDAK
        // disembunyikan krn tombol ini tidak punya teks).
        BtnPrintRekon = new widget.Button();
        BtnPrintRekon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item (copy).png")));
        BtnPrintRekon.setMnemonic('T');
        BtnPrintRekon.setToolTipText("Cetak Form Rekonsiliasi Obat (RM 03-06A)");
        BtnPrintRekon.setPreferredSize(new java.awt.Dimension(28,23));
        BtnPrintRekon.addActionListener((java.awt.event.ActionEvent evt) -> cetakFormRekonsiliasi());
        FormMenu.add(BtnPrintRekon);

        PanelAccor.add(FormMenu, java.awt.BorderLayout.NORTH);

        ScrollHTML = new widget.ScrollPane();
        ScrollHTML.setBorder(null);
        ScrollHTML.setOpaque(true);
        ScrollHTML.setPreferredSize(new java.awt.Dimension(470,16));

        LoadHTML = new widget.editorpane();
        LoadHTML.setBorder(null);
        LoadHTML.setEditable(false);
        javax.swing.text.html.HTMLEditorKit kitData = new javax.swing.text.html.HTMLEditorKit();
        LoadHTML.setEditorKit(kitData);
        javax.swing.text.html.StyleSheet styleSheetData = kitData.getStyleSheet();
        styleSheetData.addRule(
            "table{border-collapse:collapse;font:11px 'Segoe UI';color:#323232;}"+
            ".isi td{border:1px solid #e2e7dd;padding:2px 5px;background:#ffffff;}"+
            ".isi2 td{border:1px solid #e2e7dd;padding:2px 5px;background:#ffffff;font-size:10px;}"
        );
        LoadHTML.setDocument(kitData.createDefaultDocument());
        ScrollHTML.setViewportView(LoadHTML);
        PanelAccor.add(ScrollHTML, java.awt.BorderLayout.CENTER);

        internalFrameData.add(PanelAccor, java.awt.BorderLayout.EAST);

        TabPilihan.addTab("Data Rekonsiliasi Obat", internalFrameData);

        // Default terlipat (ChkAccor belum pernah diklik) - konsisten pola
        // "panel ringkasan tersembunyi sampai baris diklik" yg sudah dipakai
        // di sesi2 RM lain.
        ChkAccor.setSelected(false);
        isMenu();
    }

    /**
     * [Tab "Data Rekonsiliasi Obat"] Query header saja (TANPA nested-loop ke
     * detail obat/konfirmasi spt RMCariRekonsiliasiObat.tampil() lama - itu
     * yg bikin tabelnya berantakan). Detail dimuat terpisah per-klik lewat
     * bangunRingkasanDataRekonsiliasi(). ps/rs LOKAL (bukan field kelas)
     * spy tidak tabrakan dgn pemakaian ps/rs kelas di method lain.
     */
    private void tampilDataRekonsiliasi(){
        java.sql.PreparedStatement psData=null;
        java.sql.ResultSet rsData=null;
        try {
            Valid.tabelKosong(tabModeData);
            psData=koneksi.prepareStatement(
                "select rekonsiliasi_obat.no_rekonsiliasi,rekonsiliasi_obat.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,"+
                "reg_periksa.umurdaftar,reg_periksa.sttsumur,rekonsiliasi_obat.tanggal_wawancara,rekonsiliasi_obat.rekonsiliasi_obat_saat,"+
                "rekonsiliasi_obat.nip,petugas.nama from rekonsiliasi_obat "+
                "inner join reg_periksa on reg_periksa.no_rawat=rekonsiliasi_obat.no_rawat "+
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join petugas on rekonsiliasi_obat.nip=petugas.nip "+
                "where rekonsiliasi_obat.tanggal_wawancara between ? and ? "+
                (TCariData.getText().trim().equals("")?"":" and (rekonsiliasi_obat.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "+
                "pasien.nm_pasien like ? or petugas.nip like ? or petugas.nama like ?) ")+
                "order by rekonsiliasi_obat.tanggal_wawancara desc");
            psData.setString(1, Valid.SetTgl(Tgl1Data.getSelectedItem()+"")+" 00:00:00");
            psData.setString(2, Valid.SetTgl(Tgl2Data.getSelectedItem()+"")+" 23:59:59");
            if(!TCariData.getText().trim().equals("")){
                String kw = "%"+TCariData.getText().trim()+"%";
                psData.setString(3,kw); psData.setString(4,kw); psData.setString(5,kw); psData.setString(6,kw); psData.setString(7,kw);
            }
            rsData=psData.executeQuery();
            while(rsData.next()){
                tabModeData.addRow(new Object[]{
                    rsData.getString("no_rekonsiliasi"),
                    rsData.getString("no_rawat")+" "+rsData.getString("no_rkm_medis")+" "+rsData.getString("nm_pasien")+" ("+rsData.getString("jk")+") ("+rsData.getString("umurdaftar")+rsData.getString("sttsumur")+")",
                    rsData.getString("tanggal_wawancara"),
                    rsData.getString("rekonsiliasi_obat_saat"),
                    rsData.getString("nip")+" "+rsData.getString("nama")
                });
            }
        } catch (Exception e) {
            System.out.println("Notif tampilDataRekonsiliasi : "+e);
        } finally {
            try{ if(rsData!=null){ rsData.close(); } if(psData!=null){ psData.close(); } }catch(Exception e){}
        }
    }

    /** Toggle lipat/buka panel ringkasan - pola identik isMenu() milik RMTriaseIGD.java. */
    private void isMenu(){
        if(ChkAccor.isSelected()){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new java.awt.Dimension(470, PanelAccor.getHeight()));
            FormMenu.setVisible(true);
            ScrollHTML.setVisible(true);
            ChkAccor.setVisible(true);
        }else{
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new java.awt.Dimension(15, PanelAccor.getHeight()));
            FormMenu.setVisible(false);
            ScrollHTML.setVisible(false);
            ChkAccor.setVisible(true);
        }
    }

    private void tbDataRekonsiliasiMouseClicked(java.awt.event.MouseEvent evt){
        if(tbDataRekonsiliasi.getSelectedRow()>-1){
            ChkAccor.setSelected(true);
            isMenu();
            bangunRingkasanDataRekonsiliasi();
        }
    }

    /** Kosongkan tampilan "-" utk nilai null/kosong di ringkasan HTML. */
    private String kosongkeStrip(String s){
        return (s==null || s.trim().equals(""))?"-":s;
    }

    /** Baris label:nilai ringkasan HTML - dilewati (string kosong) kalau nilainya kosong/null/"-". */
    private String barisData(String label,String nilai){
        if(nilai==null || nilai.trim().equals("") || nilai.trim().equals("-")){ return ""; }
        return "<tr><td width='170'>"+label+"</td><td>: "+nilai+"</td></tr>";
    }

    private String judulBarisData(String judul){
        return "<tr><td colspan='2' bgcolor='#FFFAF8'><b>"+judul+"</b></td></tr>";
    }

    /**
     * [Tab "Data Rekonsiliasi Obat"] Ringkasan GABUNGAN semua tahap
     * rekonsiliasi (Admisi/Transfer Antar Ruang/Pulang) milik 1 episode
     * rawat (no_rawat) yang sama dengan baris diklik - BUKAN cuma 1 record
     * yang diklik. Diminta user: kalau pasien baru punya 2 tahap terisi
     * (mis. Admisi+Transfer), tampilkan 2 seksi saja; kalau ketiganya sudah
     * terisi (termasuk Pulang), tampilkan 3 seksi; kalau baru 1, tampilkan
     * 1 - JUMLAH SEKSI MENGIKUTI JUMLAH RECORD YANG BENAR-BENAR ADA di DB,
     * tidak dipaksa selalu 3.
     *
     * Alur: (1) resolve no_rawat + identitas pasien dari no_rekonsiliasi yg
     * diklik (ditampilkan SEKALI di atas, sama utk semua tahap krn 1 pasien/
     * 1 episode rawat), (2) SELECT semua rekonsiliasi_obat milik no_rawat
     * itu diurut sesuai tahapan (Admisi -> Transfer Antar Ruang -> Pindah
     * Faskes Lain -> Pulang, lalu tanggal_wawancara), (3) render tiap
     * record yang DITEMUKAN sbg seksi terpisah (header tahap + daftar obat
     * + konfirmasi kalau ada) - loop nested query per tahap, pola sama
     * (query lokal per iterasi) dgn RMCariRekonsiliasiObat.tampil() lama.
     * Kolom penggunaan_obat_sebelum_admisi/sumber_informasi/
     * sumber_informasi_lain BARU (migrasi sik_rekonsiliasi_obat_rm0306a.sql)
     * dibungkus try/catch TERPISAH per tahap supaya kalau migrasi blm
     * diimport, bagian LAIN ringkasan tetap tampil (bukan gagal total).
     */
    private void bangunRingkasanDataRekonsiliasi(){
        String noRekonsiliasiKlik = tbDataRekonsiliasi.getValueAt(tbDataRekonsiliasi.getSelectedRow(),0).toString();
        StringBuilder html = new StringBuilder();
        html.append("<html><body><table class='isi' width='100%' cellspacing='0'>");

        String noRawat = null;
        java.sql.PreparedStatement psId=null; java.sql.ResultSet rsId=null;
        try {
            psId = koneksi.prepareStatement(
                "select rekonsiliasi_obat.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, "+
                "reg_periksa.umurdaftar, reg_periksa.sttsumur "+
                "from rekonsiliasi_obat "+
                "inner join reg_periksa on reg_periksa.no_rawat=rekonsiliasi_obat.no_rawat "+
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "where rekonsiliasi_obat.no_rekonsiliasi=?");
            psId.setString(1, noRekonsiliasiKlik);
            rsId = psId.executeQuery();
            if(rsId.next()){
                noRawat = rsId.getString("no_rawat");
                TNoRM1.setText(rsId.getString("no_rkm_medis"));
                TPasien1.setText(rsId.getString("nm_pasien"));

                html.append(judulBarisData("Identitas Pasien"));
                html.append(barisData("No.Rawat", noRawat));
                html.append(barisData("No.RM / Nama", rsId.getString("no_rkm_medis")+" / "+rsId.getString("nm_pasien")+" ("+rsId.getString("jk")+", "+rsId.getString("umurdaftar")+rsId.getString("sttsumur")+")"));
            }
        } catch (Exception e) {
            System.out.println("Notif ringkasan identitas : "+e);
        } finally {
            try{ if(rsId!=null){ rsId.close(); } if(psId!=null){ psId.close(); } }catch(Exception e){}
        }

        if(noRawat != null){
            java.sql.PreparedStatement psH=null; java.sql.ResultSet rsH=null;
            try {
                psH = koneksi.prepareStatement(
                    "select rekonsiliasi_obat.*, petugas.nama as nama_petugas "+
                    "from rekonsiliasi_obat "+
                    "inner join petugas on rekonsiliasi_obat.nip=petugas.nip "+
                    "where rekonsiliasi_obat.no_rawat=? "+
                    "order by field(rekonsiliasi_obat.rekonsiliasi_obat_saat,'Admisi','Transfer Antar Ruang','Pindah Faskes Lain','Pulang'), rekonsiliasi_obat.tanggal_wawancara");
                psH.setString(1, noRawat);
                rsH = psH.executeQuery();
                while(rsH.next()){
                    String noRekonsiliasiTahap = rsH.getString("no_rekonsiliasi");

                    html.append(judulBarisData("Rekonsiliasi Saat: "+kosongkeStrip(rsH.getString("rekonsiliasi_obat_saat"))));
                    html.append(barisData("No.Rekonsiliasi", noRekonsiliasiTahap));
                    html.append(barisData("Tanggal Wawancara", rsH.getString("tanggal_wawancara")));
                    html.append(barisData("Apoteker", rsH.getString("nip")+" "+rsH.getString("nama_petugas")));

                    try {
                        html.append(barisData("Penggunaan Obat Sebelum Admisi", rsH.getString("penggunaan_obat_sebelum_admisi")));
                        html.append(barisData("Sumber Informasi", rsH.getString("sumber_informasi")));
                        html.append(barisData("Keterangan Sumber Lain", rsH.getString("sumber_informasi_lain")));
                    } catch (Exception eKolomBaru) {
                        // Kolom RM 03-06A blm ada (migrasi blm diimport) - dilewati saja.
                    }

                    html.append(barisData("Alergi Obat", rsH.getString("alergi_obat")));
                    html.append(barisData("Manifestasi Alergi", rsH.getString("manifestasi_alergi")));
                    html.append(barisData("Dampak Alergi", rsH.getString("dampak_alergi")));

                    html.append("<tr><td colspan='2'><table class='isi2' width='100%' cellspacing='0'>");
                    html.append("<tr><td><b>Nama Obat</b></td><td><b>Dosis</b></td><td><b>Frekuensi</b></td><td><b>Cara Pemberian</b></td><td><b>Tindak Lanjut</b></td><td><b>Perubahan Aturan Pakai</b></td></tr>");
                    java.sql.PreparedStatement psO=null; java.sql.ResultSet rsO=null;
                    try {
                        psO = koneksi.prepareStatement("select * from rekonsiliasi_obat_detail_obat where no_rekonsiliasi=?");
                        psO.setString(1, noRekonsiliasiTahap);
                        rsO = psO.executeQuery();
                        boolean adaObat=false;
                        while(rsO.next()){
                            adaObat=true;
                            html.append("<tr><td>").append(kosongkeStrip(rsO.getString("nama_obat"))).append("</td>")
                                .append("<td>").append(kosongkeStrip(rsO.getString("dosis_obat"))).append("</td>")
                                .append("<td>").append(kosongkeStrip(rsO.getString("frekuensi"))).append("</td>")
                                .append("<td>").append(kosongkeStrip(rsO.getString("cara_pemberian"))).append("</td>")
                                .append("<td>").append(kosongkeStrip(rsO.getString("tindak_lanjut"))).append("</td>")
                                .append("<td>").append(kosongkeStrip(rsO.getString("perubahan_aturan_pakai"))).append("</td></tr>");
                        }
                        if(!adaObat){
                            html.append("<tr><td colspan='6'><i>Belum ada data obat</i></td></tr>");
                        }
                    } catch (Exception e) {
                        System.out.println("Notif ringkasan obat : "+e);
                    } finally {
                        try{ if(rsO!=null){ rsO.close(); } if(psO!=null){ psO.close(); } }catch(Exception e){}
                    }
                    html.append("</table></td></tr>");

                    java.sql.PreparedStatement psK=null; java.sql.ResultSet rsK=null;
                    try {
                        psK = koneksi.prepareStatement(
                            "select rekonsiliasi_obat_konfirmasi.*, petugas.nama from rekonsiliasi_obat_konfirmasi "+
                            "inner join petugas on rekonsiliasi_obat_konfirmasi.nip=petugas.nip "+
                            "where rekonsiliasi_obat_konfirmasi.no_rekonsiliasi=?");
                        psK.setString(1, noRekonsiliasiTahap);
                        rsK = psK.executeQuery();
                        if(rsK.next()){
                            html.append(barisData("Diterima Farmasi", rsK.getString("diterima_farmasi")));
                            html.append(barisData("Dikonfirmasi Apoteker", rsK.getString("dikonfirmasi_apoteker")));
                            html.append(barisData("Petugas Farmasi/Apoteker", rsK.getString("nip")+" "+rsK.getString("nama")));
                            html.append(barisData("Diserahkan Ke Pasien", rsK.getString("diserahkan_pasien")));
                        }
                    } catch (Exception e) {
                        System.out.println("Notif ringkasan konfirmasi : "+e);
                    } finally {
                        try{ if(rsK!=null){ rsK.close(); } if(psK!=null){ psK.close(); } }catch(Exception e){}
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif ringkasan tahap : "+e);
            } finally {
                try{ if(rsH!=null){ rsH.close(); } if(psH!=null){ psH.close(); } }catch(Exception e){}
            }
        }

        html.append("</table></body></html>");
        LoadHTML.setText(html.toString());
        LoadHTML.setCaretPosition(0);
    }

    /** Baca kolom yg mungkin blm ada (migrasi RM 03-06A blm diimport) - null kalau gagal, bukan exception. */
    private String safeGetString(java.sql.ResultSet rs, String kolom){
        try { return rs.getString(kolom); } catch (Exception e) { return null; }
    }

    /**
     * [BARU] Tombol cetak - resolve episode rawat (no_rawat) dari baris yg
     * diklik, cari SEMUA tahap (Admisi/Transfer Antar Ruang/Pulang) yg
     * punya record utk episode itu, lalu tampilkan dialog checkbox supaya
     * user PILIH tahap mana yg mau dicetak (bisa lebih dari satu - tiap
     * tahap = 1 halaman/1 jendela JasperViewer terpisah, krn 1 fill Jasper
     * = 1 parameter set = 1 tahap, lihat cetakSatuTahapRekonsiliasi()).
     * Checkbox tahap yg TIDAK punya record didisable (bukan disembunyikan)
     * spy user tetap lihat ke-3 opsi standar tapi jelas mana yg tersedia.
     * Default tercentang: tahap dari baris yg diklik saja.
     */
    private void cetakFormRekonsiliasi(){
        if(tbDataRekonsiliasi.getSelectedRow()<0){
            JOptionPane.showMessageDialog(null,"Pilih dulu baris data yang mau dicetak.");
            return;
        }
        String noRekonsiliasiKlik = tbDataRekonsiliasi.getValueAt(tbDataRekonsiliasi.getSelectedRow(),0).toString();

        String noRawat = null;
        java.sql.PreparedStatement psR=null; java.sql.ResultSet rsR=null;
        try {
            psR = koneksi.prepareStatement("select no_rawat from rekonsiliasi_obat where no_rekonsiliasi=?");
            psR.setString(1, noRekonsiliasiKlik);
            rsR = psR.executeQuery();
            if(rsR.next()){ noRawat = rsR.getString("no_rawat"); }
        } catch (Exception e) {
            System.out.println("Notif resolve no_rawat cetak : "+e);
        } finally {
            try{ if(rsR!=null){ rsR.close(); } if(psR!=null){ psR.close(); } }catch(Exception e){}
        }
        if(noRawat==null){
            JOptionPane.showMessageDialog(null,"Data tidak ditemukan.");
            return;
        }

        String[] urutanTahap = {"Admisi","Transfer Antar Ruang","Pulang"};
        java.util.Map<String,String> tahapKeNoRekon = new java.util.LinkedHashMap<>();
        java.sql.PreparedStatement psT=null; java.sql.ResultSet rsT=null;
        try {
            psT = koneksi.prepareStatement("select no_rekonsiliasi, rekonsiliasi_obat_saat from rekonsiliasi_obat where no_rawat=?");
            psT.setString(1, noRawat);
            rsT = psT.executeQuery();
            while(rsT.next()){
                tahapKeNoRekon.put(rsT.getString("rekonsiliasi_obat_saat"), rsT.getString("no_rekonsiliasi"));
            }
        } catch (Exception e) {
            System.out.println("Notif daftar tahap cetak : "+e);
        } finally {
            try{ if(rsT!=null){ rsT.close(); } if(psT!=null){ psT.close(); } }catch(Exception e){}
        }

        javax.swing.JPanel panelPilihan = new javax.swing.JPanel(new java.awt.GridLayout(0,1,4,4));
        panelPilihan.add(new javax.swing.JLabel("Pilih tahap rekonsiliasi yang akan dicetak:"));
        javax.swing.JCheckBox[] chkTahap = new javax.swing.JCheckBox[urutanTahap.length];
        for(int i=0;i<urutanTahap.length;i++){
            chkTahap[i] = new javax.swing.JCheckBox(urutanTahap[i]);
            boolean ada = tahapKeNoRekon.containsKey(urutanTahap[i]);
            chkTahap[i].setEnabled(ada);
            chkTahap[i].setSelected(ada && tahapKeNoRekon.get(urutanTahap[i]).equals(noRekonsiliasiKlik));
            panelPilihan.add(chkTahap[i]);
        }

        int pilih = JOptionPane.showConfirmDialog(null, panelPilihan, "Cetak Form Rekonsiliasi Obat (RM 03-06A)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(pilih != JOptionPane.OK_OPTION){ return; }

        boolean adaDipilih=false;
        for(int i=0;i<urutanTahap.length;i++){
            if(chkTahap[i].isSelected()){
                adaDipilih=true;
                cetakSatuTahapRekonsiliasi(tahapKeNoRekon.get(urutanTahap[i]));
            }
        }
        if(!adaDipilih){
            JOptionPane.showMessageDialog(null,"Tidak ada tahap yang dipilih untuk dicetak.");
        }
    }

    /**
     * [BARU] Cetak 1 tahap (1 halaman) form RM 03-06A
     * (rptRekonsiliasiObatForm.jasper) - replika layout formulir fisik RSUD
     * Demang Sepulau Raya. Identitas pasien/checkbox/apoteker dikirim
     * lewat parameter Jasper (1 nilai per print, di-query manual di sini);
     * dataset utama (daftar obat) di-query LANGSUNG oleh Valid.MyReportqry
     * sendiri (pola sama BtnPrintActionPerformed di RMCariRekonsiliasiObat.java
     * - qry string biasa, BUKAN PreparedStatement, krn MyReportqry cuma
     * terima 1 string query lalu dieksekusinya sendiri).
     * TTD Apoteker pakai QR-code tanda tangan elektronik (parameter
     * "finger") - pola PERSIS sama dgn BtnPrint1ActionPerformed RMTriaseIGD.java
     * (SHA1 sidik jari dari tabel sidikjari via pegawai.nik, fallback ke
     * NIP kalau blm rekam sidik jari). petugas.nip di app ini = pegawai.nik
     * (nilai sama, tabel beda - dicek langsung ke DB sebelum implementasi).
     */
    private void cetakSatuTahapRekonsiliasi(String noRekonsiliasi){
        if(noRekonsiliasi==null){ return; }
        java.sql.PreparedStatement psH=null; java.sql.ResultSet rsH=null;
        try {
            psH = koneksi.prepareStatement(
                "select rekonsiliasi_obat.*, pasien.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, petugas.nama as nama_petugas "+
                "from rekonsiliasi_obat "+
                "inner join reg_periksa on reg_periksa.no_rawat=rekonsiliasi_obat.no_rawat "+
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join petugas on rekonsiliasi_obat.nip=petugas.nip "+
                "where rekonsiliasi_obat.no_rekonsiliasi=?");
            psH.setString(1, noRekonsiliasi);
            rsH = psH.executeQuery();
            if(!rsH.next()){
                JOptionPane.showMessageDialog(null,"Data tidak ditemukan.");
                return;
            }

            String nipApoteker = rsH.getString("nip");
            String namaApoteker = rsH.getString("nama_petugas");
            String fingerHash = Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", nipApoteker);

            java.util.Map<String,Object> param = new java.util.HashMap<>();
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());
            param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
            param.put("pNoRM", rsH.getString("no_rkm_medis"));
            param.put("pNamaPasien", rsH.getString("nm_pasien"));
            param.put("pTglLahir", rsH.getString("tgl_lahir"));
            param.put("pPenggunaanObatSebelumAdmisi", safeGetString(rsH,"penggunaan_obat_sebelum_admisi"));
            param.put("pRekonsiliasiSaat", rsH.getString("rekonsiliasi_obat_saat"));
            param.put("pTanggalWawancara", rsH.getString("tanggal_wawancara"));
            param.put("pSumberInformasi", safeGetString(rsH,"sumber_informasi"));
            param.put("pSumberInformasiLain", safeGetString(rsH,"sumber_informasi_lain"));
            param.put("pApotekerNama", namaApoteker);
            param.put("pApotekerNip", nipApoteker);
            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+
                "\nDitandatangani secara elektronik oleh "+namaApoteker+
                "\nID "+((fingerHash==null||fingerHash.isEmpty())?nipApoteker:fingerHash)+
                "\n"+rsH.getString("tanggal_wawancara"));

            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.MyReportqry("rptRekonsiliasiObatForm.jasper","report","::[ Form Rekonsiliasi Obat - RM 03-06A ]::",
                "select nama_obat, dosis_obat, frekuensi, cara_pemberian, tindak_lanjut, perubahan_aturan_pakai "+
                "from rekonsiliasi_obat_detail_obat where no_rekonsiliasi='"+noRekonsiliasi+"'", param);
            this.setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            System.out.println("Notif cetak form rekonsiliasi : "+e);
            JOptionPane.showMessageDialog(null,"Gagal cetak : "+e);
        } finally {
            try{ if(rsH!=null){ rsH.close(); } if(psH!=null){ psH.close(); } }catch(Exception e){}
        }
    }

    public void emptTeks() {
        Tanggal.setDate(new Date());
        AlergiObat.setText("");
        ManifestasiAlergi.setText("");
        DampakAlergi.setSelectedIndex(0);

        // [RM 03-06A] reset field baru - lihat initRekonsiliasiTambahan()
        bgObatSebelumAdmisi.clearSelection();
        bgSumberInformasi.clearSelection();
        TSumberLainKet.setText("");

        autoNomor();
        updatePatientCard();
        updateCounterObat();
        updateStatusRekon();
        TNoRekonsialiasi.requestFocus();
    }
    
    private void jam(){
        ActionListener taskPerformer = new ActionListener(){
            private int nilai_jam;
            private int nilai_menit;
            private int nilai_detik;
            @Override
            public void actionPerformed(ActionEvent e) {
                String nol_jam = "";
                String nol_menit = "";
                String nol_detik = "";
                // Membuat Date
                //Date dt = new Date();
                Date now = Calendar.getInstance().getTime();

                // Mengambil nilaj JAM, MENIT, dan DETIK Sekarang
                if(ChkJln.isSelected()==true){
                    nilai_jam = now.getHours();
                    nilai_menit = now.getMinutes();
                    nilai_detik = now.getSeconds();
                }else if(ChkJln.isSelected()==false){
                    nilai_jam =CmbJam.getSelectedIndex();
                    nilai_menit =CmbMenit.getSelectedIndex();
                    nilai_detik =CmbDetik.getSelectedIndex();
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
                CmbJam.setSelectedItem(jam);
                CmbMenit.setSelectedItem(menit);
                CmbDetik.setSelectedItem(detik);
            }
        };
        // Timer
        new Timer(1000, taskPerformer).start();
    } 
    
    public void setNoRm(String norwt) {
        TNoRw.setText(norwt);   
        try {
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien, if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?");
            try {
                ps.setString(1,TNoRw.getText());
                rs=ps.executeQuery();
                if(rs.next()){
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    updatePatientCard();
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
    
    public void isCek(){        
        BtnSimpan.setEnabled(akses.getrekonsiliasi_obat());
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
        updateFooterMeta();
    }

    private void autoNomor() {
        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(rekonsiliasi_obat.no_rekonsiliasi,4),signed)),0) from rekonsiliasi_obat where left(rekonsiliasi_obat.tanggal_wawancara,10)='"+Valid.SetTgl(Tanggal.getSelectedItem()+"")+"' ","RO"+Valid.SetTgl(Tanggal.getSelectedItem()+"").replaceAll("-",""),4,TNoRekonsialiasi);           
    }

    private String selectedTindakLanjut = "Lanjut aturan pakai sama";
    private JButton btnTindakSama;
    private JButton btnTindakBerubah;
    private JButton btnTindakStop;

    private JButton createSegmentedTindakLanjutButton(final String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isSel = text.equalsIgnoreCase(selectedTindakLanjut);
                if (isSel) {
                    g2.setColor(new Color(0, 91, 82));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(new Color(203, 213, 225));
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            selectedTindakLanjut = text;
            if (TindakLanjut != null) {
                try { TindakLanjut.setSelectedItem(text); } catch (Exception ex) {}
            }
            updateSegmentedTindakLanjut();
        });
        return b;
    }

    private void updateSegmentedTindakLanjut() {
        if (btnTindakSama == null) return;
        boolean sel1 = "Lanjut aturan pakai sama".equalsIgnoreCase(selectedTindakLanjut);
        boolean sel2 = "Lanjut aturan pakai berubah".equalsIgnoreCase(selectedTindakLanjut);
        boolean sel3 = "Stop".equalsIgnoreCase(selectedTindakLanjut);

        btnTindakSama.setForeground(sel1 ? Color.WHITE : new Color(51, 65, 85));
        btnTindakSama.setFont(new Font("Segoe UI", sel1 ? Font.BOLD : Font.PLAIN, 11));

        btnTindakBerubah.setForeground(sel2 ? Color.WHITE : new Color(51, 65, 85));
        btnTindakBerubah.setFont(new Font("Segoe UI", sel2 ? Font.BOLD : Font.PLAIN, 11));

        btnTindakStop.setForeground(sel3 ? Color.WHITE : new Color(51, 65, 85));
        btnTindakStop.setFont(new Font("Segoe UI", sel3 ? Font.BOLD : Font.PLAIN, 11));

        btnTindakSama.repaint();
        btnTindakBerubah.repaint();
        btnTindakStop.repaint();
    }

    private void styleModalTextBox(widget.TextBox tb, final String placeholder) {
        tb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tb.setForeground(new Color(15, 23, 42));
        tb.setBackground(Color.WHITE);
        tb.putClientProperty("Component.arc", 6);
        tb.putClientProperty("ModernStyle.active", Boolean.TRUE);
        tb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        tb.setText(placeholder);
        tb.setForeground(new Color(148, 163, 184));
        tb.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (placeholder.equals(tb.getText())) {
                    tb.setText("");
                    tb.setForeground(new Color(15, 23, 42));
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tb.getText().trim().isEmpty()) {
                    tb.setText(placeholder);
                    tb.setForeground(new Color(148, 163, 184));
                }
            }
        });
    }

    private void initModernModalTambahObat() {
        DlgTambahObatRekonsiliasi.setSize(560, 320);
        DlgTambahObatRekonsiliasi.getContentPane().removeAll();

        JPanel pnlRoot = new JPanel(new BorderLayout(0, 0));
        pnlRoot.setBackground(Color.WHITE);
        pnlRoot.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));

        // 1. Header (North)
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(14, 18, 6, 14));

        JLabel lblTitle = new JLabel("Tambah Obat Direkonsiliasi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(15, 23, 42));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JButton btnClose = new JButton("\u00D7");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 17));
        btnClose.setForeground(new Color(148, 163, 184));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> DlgTambahObatRekonsiliasi.dispose());
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnClose.setForeground(new Color(15, 23, 42));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnClose.setForeground(new Color(148, 163, 184));
            }
        });
        pnlHeader.add(btnClose, BorderLayout.EAST);
        pnlRoot.add(pnlHeader, BorderLayout.NORTH);

        // 2. Form Content (Center)
        JPanel pnlForm = new JPanel(null);
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setPreferredSize(new Dimension(556, 212));

        // Row 1: Nama Obat & Tombol Pencarian
        JLabel lNama = new JLabel("Nama Obat (Gunakan Pencarian)");
        lNama.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lNama.setForeground(new Color(100, 116, 139));
        lNama.setBounds(18, 2, 200, 14);
        pnlForm.add(lNama);

        NamaObat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        NamaObat.setForeground(new Color(148, 163, 184));
        NamaObat.setBackground(Color.WHITE);
        NamaObat.setEditable(false);
        NamaObat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        NamaObat.setToolTipText("Klik untuk mencari nama obat di database formularium");
        NamaObat.putClientProperty("Component.arc", 6);
        NamaObat.putClientProperty("ModernStyle.active", Boolean.TRUE);
        NamaObat.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        NamaObat.setBounds(18, 18, 484, 28);
        NamaObat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                bukaCariObat();
            }
        });
        NamaObat.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER || e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
                    bukaCariObat();
                }
            }
        });
        pnlForm.add(NamaObat);

        JButton btnCariObat = new JButton();
        btnCariObat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        btnCariObat.setBounds(506, 18, 32, 28);
        btnCariObat.setBackground(Color.WHITE);
        btnCariObat.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        btnCariObat.setFocusPainted(false);
        btnCariObat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCariObat.setToolTipText("Cari Obat di Formularium (databarang)");
        btnCariObat.addActionListener(e -> bukaCariObat());
        pnlForm.add(btnCariObat);

        // Row 2: Dosis Obat & Frekuensi (2 equal columns)
        JLabel lDosis = new JLabel("Dosis Obat");
        lDosis.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lDosis.setForeground(new Color(100, 116, 139));
        lDosis.setBounds(18, 54, 150, 14);
        pnlForm.add(lDosis);

        styleModalTextBox(DosisObat, "mis. 500mg");
        DosisObat.setBounds(18, 70, 254, 28);
        pnlForm.add(DosisObat);

        JLabel lFrek = new JLabel("Frekuensi");
        lFrek.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lFrek.setForeground(new Color(100, 116, 139));
        lFrek.setBounds(284, 54, 150, 14);
        pnlForm.add(lFrek);

        styleModalTextBox(Frekuensi, "mis. 3x1");
        Frekuensi.setBounds(284, 70, 254, 28);
        pnlForm.add(Frekuensi);

        // Row 3: Tindak Lanjut
        JLabel lTindak = new JLabel("Tindak Lanjut");
        lTindak.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lTindak.setForeground(new Color(100, 116, 139));
        lTindak.setBounds(18, 106, 150, 14);
        pnlForm.add(lTindak);

        JPanel segTindak = new JPanel(null);
        segTindak.setBounds(18, 122, 520, 28);
        segTindak.setOpaque(false);

        btnTindakSama = createSegmentedTindakLanjutButton("Lanjut aturan pakai sama");
        btnTindakSama.setBounds(0, 0, 190, 28);

        btnTindakBerubah = createSegmentedTindakLanjutButton("Lanjut aturan pakai berubah");
        btnTindakBerubah.setBounds(189, 0, 215, 28);

        btnTindakStop = createSegmentedTindakLanjutButton("Stop");
        btnTindakStop.setBounds(403, 0, 117, 28);

        segTindak.add(btnTindakSama);
        segTindak.add(btnTindakBerubah);
        segTindak.add(btnTindakStop);
        pnlForm.add(segTindak);

        // Row 4: Perubahan Aturan Pakai
        JLabel lPerubahan = new JLabel("Perubahan Aturan Pakai");
        lPerubahan.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lPerubahan.setForeground(new Color(100, 116, 139));
        lPerubahan.setBounds(18, 158, 200, 14);
        pnlForm.add(lPerubahan);

        styleModalTextBox(PerubahanAturanPakai, "Kosongkan bila tidak berubah");
        PerubahanAturanPakai.setBounds(18, 174, 520, 28);
        pnlForm.add(PerubahanAturanPakai);

        pnlRoot.add(pnlForm, BorderLayout.CENTER);

        // 3. Footer (South)
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createEmptyBorder(6, 18, 14, 18));

        styleOutlineButton(BtnKeluarRekon);
        BtnKeluarRekon.setText("Tutup");
        BtnKeluarRekon.setPreferredSize(new Dimension(80, 28));
        pnlFooter.add(BtnKeluarRekon, BorderLayout.WEST);

        styleSolidTealButton(BtnSimpanRekon);
        BtnSimpanRekon.setText("Simpan");
        BtnSimpanRekon.setPreferredSize(new Dimension(95, 28));
        pnlFooter.add(BtnSimpanRekon, BorderLayout.EAST);

        pnlRoot.add(pnlFooter, BorderLayout.SOUTH);

        DlgTambahObatRekonsiliasi.getContentPane().add(pnlRoot, BorderLayout.CENTER);

        emptTeksTambahRekon();
    }

    private void bukaCariObat() {
        DlgCariNamaObat dlgObat = new DlgCariNamaObat(null, false);
        dlgObat.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (dlgObat.getTable().getSelectedRow() != -1) {
                    String nm = dlgObat.getTable().getValueAt(dlgObat.getTable().getSelectedRow(), 1).toString();
                    NamaObat.setText(nm);
                    NamaObat.setForeground(new Color(15, 23, 42));
                    DosisObat.requestFocus();
                }
            }
        });
        dlgObat.setSize(620, 420);
        dlgObat.setLocationRelativeTo(DlgTambahObatRekonsiliasi);
        dlgObat.setVisible(true);
    }

    private void emptTeksTambahRekon() {
        NamaObat.setText("Pilih nama obat (klik cari)...");
        NamaObat.setForeground(new Color(148, 163, 184));
        NamaObat.setEditable(false);

        DosisObat.setText("mis. 500mg");
        DosisObat.setForeground(new Color(148, 163, 184));

        Frekuensi.setText("mis. 3x1");
        Frekuensi.setForeground(new Color(148, 163, 184));

        PerubahanAturanPakai.setText("Kosongkan bila tidak berubah");
        PerubahanAturanPakai.setForeground(new Color(148, 163, 184));

        selectedTindakLanjut = "Lanjut aturan pakai sama";
        if (TindakLanjut != null) {
            try { TindakLanjut.setSelectedItem("Lanjut aturan pakai sama"); } catch (Exception e) {}
        }
        updateSegmentedTindakLanjut();
        NamaObat.requestFocus();
    }
}
