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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.CardLayout;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Container;
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
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import widget.ThemeConfig;

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
            "","NAMA OBAT","DOSIS OBAT","FREKUENSI","CARA PEMBERIAN","TINDAK LANJUT","PERUBAHAN ATURAN PAKAI","WAKTU TERAKHIR"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbPemeriksaan.setModel(tabMode);
        tbPemeriksaan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbPemeriksaan.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbPemeriksaan.setRowHeight(32);
        tbPemeriksaan.setShowGrid(false);
        tbPemeriksaan.setIntercellSpacing(new Dimension(0, 1));
        tbPemeriksaan.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        tbPemeriksaan.getColumnModel().getColumn(0).setPreferredWidth(32);
        tbPemeriksaan.getColumnModel().getColumn(0).setMaxWidth(32);
        tbPemeriksaan.getColumnModel().getColumn(0).setMinWidth(32);
        tbPemeriksaan.getColumnModel().getColumn(1).setPreferredWidth(250);
        tbPemeriksaan.getColumnModel().getColumn(2).setPreferredWidth(90);
        tbPemeriksaan.getColumnModel().getColumn(3).setPreferredWidth(90);
        tbPemeriksaan.getColumnModel().getColumn(4).setPreferredWidth(120);
        tbPemeriksaan.getColumnModel().getColumn(5).setPreferredWidth(170);
        tbPemeriksaan.getColumnModel().getColumn(6).setPreferredWidth(230);
        tbPemeriksaan.getColumnModel().getColumn(7).setMinWidth(0);
        tbPemeriksaan.getColumnModel().getColumn(7).setMaxWidth(0);
        tbPemeriksaan.getColumnModel().getColumn(7).setPreferredWidth(0);

        tbPemeriksaan.getColumnModel().getColumn(0).setCellRenderer(new RadioCellRenderer());
        tbPemeriksaan.getColumnModel().getColumn(1).setCellRenderer(new BoldTextCellRenderer());
        tbPemeriksaan.getColumnModel().getColumn(2).setCellRenderer(new ModernTextCellRenderer(false, true));
        tbPemeriksaan.getColumnModel().getColumn(3).setCellRenderer(new ModernTextCellRenderer(false, true));
        tbPemeriksaan.getColumnModel().getColumn(4).setCellRenderer(new ModernTextCellRenderer(false, true));
        tbPemeriksaan.getColumnModel().getColumn(5).setCellRenderer(new BadgeCellRenderer());
        tbPemeriksaan.getColumnModel().getColumn(6).setCellRenderer(new ModernTextCellRenderer(true, false));

        javax.swing.table.DefaultTableCellRenderer tblHeaderRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(new Color(246, 248, 245));
                l.setForeground(new Color(139, 146, 141));
                l.setFont(new Font("Segoe UI", Font.BOLD, 10));
                l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(207, 211, 206)),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)
                ));
                if (column == 2 || column == 3 || column == 4 || column == 5) {
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
        this.setMinimumSize(new Dimension(1180, 650));
        this.setSize(1360, 720);
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
            int jawab = JOptionPane.showConfirmDialog(rootPane,
                    "Hapus obat yang dipilih dari daftar rekonsiliasi?",
                    "Konfirmasi Hapus Baris",JOptionPane.YES_NO_OPTION);
            if(jawab==JOptionPane.YES_OPTION){
                tabMode.removeRow(tbPemeriksaan.getSelectedRow());
                emptTeksTambahRekon();
                updateCounterObat();
            }
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
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        Valid.pindah(evt, BtnBatal, BtnKeluar);
    }//GEN-LAST:event_BtnCariKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        Valid.tabelKosong(tabMode);
        aturModeEdit(false);
        emptTeks();
        updateCounterObat();
    }//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnBatalActionPerformed(null);
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
    }//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Pasien");
        }else if(KdPetugas.getText().trim().equals("")||NmPetugas.getText().trim().equals("")){
            Valid.textKosong(BtnPetugas,"Petugas");
        }else if(TNoRekonsialiasi.getText().trim().equals("")||TNoRekonsialiasi.getText().trim().equals("-")){
            Valid.textKosong(TNoRekonsialiasi,"No.Rekonsiliasi");
        }else if("Admisi".equals(RekonsiliasiSaat.getSelectedItem()) &&
                !RbObatSebelumAdmisiYa.isSelected() && !RbObatSebelumAdmisiTidak.isSelected()){
            JOptionPane.showMessageDialog(rootPane,"Pilih penggunaan obat sebelum admisi.");
            RbObatSebelumAdmisiYa.requestFocus();
        }else if("Admisi".equals(RekonsiliasiSaat.getSelectedItem()) &&
                !RbSumberRM.isSelected() && !RbSumberPasien.isSelected() && !RbSumberLain.isSelected()){
            JOptionPane.showMessageDialog(rootPane,"Pilih sumber informasi rekonsiliasi.");
            RbSumberRM.requestFocus();
        }else if("Admisi".equals(RekonsiliasiSaat.getSelectedItem()) &&
                RbSumberLain.isSelected() && nilaiKeteranganSumber()==null){
            Valid.textKosong(TSumberLainKet,"Keterangan Sumber Informasi Lain");
        }else if(tabMode.getRowCount()==0){
            Valid.textKosong(TNoRekonsialiasi,"Data Obat Rekonsiliasi");
        }else{
            String aksiKonfirmasi = noRekonsiliasiEdit==null ? "disimpan" : "diganti";
            reply = JOptionPane.showConfirmDialog(rootPane,
                    "Pastikan data yang akan "+aksiKonfirmasi+" sudah benar.",
                    noRekonsiliasiEdit==null ? "Konfirmasi Simpan" : "Konfirmasi Ganti",
                    JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                if(noRekonsiliasiEdit!=null){
                    gantiRekonsiliasiDalamTransaksi();
                    return;
                }
                if(Sequel.menyimpantf("rekonsiliasi_obat","?,?,?,?,?,?,?,?,?,?,?","No.Rawat",11,new String[]{
                        TNoRekonsialiasi.getText(),TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+CmbJam.getSelectedItem()+":"+CmbMenit.getSelectedItem()+":"+CmbDetik.getSelectedItem(),
                        RekonsiliasiSaat.getSelectedItem().toString(),AlergiObat.getText(),ManifestasiAlergi.getText(),DampakAlergi.getSelectedItem().toString(),KdPetugas.getText(),
                        pilihRadio(RbObatSebelumAdmisiYa,"Ya",RbObatSebelumAdmisiTidak,"Tidak"),
                        pilihRadio(RbSumberRM,"Status Pasien di Rekam Medis",RbSumberPasien,"Pasien/Keluarga Pasien",RbSumberLain,"Lain-lain"),
                        nilaiKeteranganSumber(),
                    })==true){
                        for (i = 0; i < tbPemeriksaan.getRowCount(); i++) {
                           Sequel.menyimpan2("rekonsiliasi_obat_detail_obat","?,?,?,?,?,?,?,?",8,new String[]{
                               TNoRekonsialiasi.getText(),
                               tbPemeriksaan.getValueAt(i,1).toString(),
                               tbPemeriksaan.getValueAt(i,2).toString(),
                               tbPemeriksaan.getValueAt(i,3).toString(),
                               tbPemeriksaan.getValueAt(i,4).toString(),
                               null,
                               tbPemeriksaan.getValueAt(i,5).toString(),
                               tbPemeriksaan.getValueAt(i,6).toString()
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
        String cara = AturanPakai.getText().trim();
        if (cara.equals("mis. Oral / IV") || cara.equals("mis. Oral / IV / Drop")) cara = "";
        String ubah = PerubahanAturanPakai.getText().trim();
        if (ubah.equals("Kosongkan bila tidak berubah")) ubah = "";

        if(nm.equals("")){
            javax.swing.JOptionPane.showMessageDialog(DlgTambahObatRekonsiliasi, "Silakan pilih nama obat terlebih dahulu melalui tombol pencarian!");
            bukaCariObat();
        }else if(dos.equals("")){
            Valid.textKosong(DosisObat,"Dosis Obat");
        }else if(frek.equals("")){
            Valid.textKosong(Frekuensi,"Frekuensi");
        }else if(cara.equals("")){
            Valid.textKosong(AturanPakai,"Cara Pemberian");
        }else if("Lanjut aturan pakai berubah".equals(selectedTindakLanjut) && ubah.equals("")){
            Valid.textKosong(PerubahanAturanPakai,"Perubahan Aturan Pakai");
        }else if(tabMode.getRowCount()>=10){
            JOptionPane.showMessageDialog(DlgTambahObatRekonsiliasi,
                    "Form RM 03-06A menyediakan maksimal 10 baris obat.");
        }else{
            String tl = selectedTindakLanjut != null && !selectedTindakLanjut.isEmpty() ? 
                        selectedTindakLanjut : 
                        (TindakLanjut != null && TindakLanjut.getSelectedItem() != null ? TindakLanjut.getSelectedItem().toString() : "Lanjut aturan pakai sama");
            tabMode.addRow(new Object[]{
                "", nm, dos, frek, (cara.isEmpty() ? "-" : cara), tl, ubah
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
    private javax.swing.JPanel pnlTopSection;
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
    private javax.swing.JPanel pnlFooterActionCards;
    private java.awt.CardLayout footerActionLayout;
    private widget.Button BtnBaruData, BtnGantiData, BtnHapusData;
    private String noRekonsiliasiEdit;
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
    private widget.Label LCountData;
    private widget.Table tbDataRekonsiliasi;
    private widget.PanelBiasa PanelAccor, FormMenu;
    private widget.CekBox ChkAccor;
    private widget.TextBox TNoRM1, TPasien1;
    private widget.ScrollPane ScrollHTML;
    private widget.editorpane LoadHTML;
    private widget.Button BtnPrintRekon;

    /**
     * [RM 03-06A] Penyesuaian form dgn formulir fisik "REKONSILIASI OBAT"
     * (RSUD Demang Sepulau Raya):
     * 1) Riwayat alergi dipertahankan sebagai informasi klinis tambahan,
     *    tetapi diberi penanda jelas bahwa bagian tersebut tidak tercetak
     *    pada formulir fisik RM 03-06A.
     * 2) "Rekonsiliasi Saat" TETAP bisa dipilih bebas (dikoreksi user -
     *    rekonsiliasi obat py 3 momen proses: Admisi/Transfer/Discharge),
     *    model dipangkas dari 4 opsi jadi 3 ("Admisi"/"Transfer Antar
     *    Ruang"/"Pulang") - "Pindah Faskes Lain" dibuang dari pilihan
     *    (bukan dihapus dari DB, cuma tidak lagi ditawarkan di combo).
     * 3) Label "Petugas :" -> "Apoteker :" (sesuai kolom tanda tangan
     *    fisik "Apoteker yang melakukan rekonsiliasi obat").
     * 4) Model combo TindakLanjut (di DlgTambahObatRekonsiliasi) diperluas
     *    dari 2 opsi (Stop/Lanjut) jadi 3 opsi sesuai fisik.
     * 5) Field "Penggunaan Obat Sebelum Admisi" dan "Sumber Informasi"
     *    disusun dengan GridBagLayout agar responsif dan dapat benar-benar
     *    melipat saat tahap Transfer/Pulang dipilih.
     * 6) Header memakai patient card dan grid field responsif, tanpa
     *    koordinat absolut yang mudah terpotong pada ukuran layar berbeda.
     * Dipanggil sekali dari constructor, setelah initComponents().
     */
    private void initRekonsiliasiTambahan(){
        RekonsiliasiSaat.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            "Admisi","Transfer Antar Ruang","Pulang"
        }));

        jLabel9.setText("Apoteker :");

        jLabel111.setVisible(false);
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
        RbSumberRM.addActionListener(e -> {
            perbaruiKeteranganSumber();
            updateStatusRekon();
        });

        RbSumberPasien = new widget.RadioButton();
        RbSumberPasien.setText("Pasien/Keluarga Pasien");
        RbSumberPasien.addActionListener(e -> {
            perbaruiKeteranganSumber();
            updateStatusRekon();
        });

        RbSumberLain = new widget.RadioButton();
        RbSumberLain.setText("Lain-lain");
        RbSumberLain.addActionListener(e -> {
            perbaruiKeteranganSumber();
            updateStatusRekon();
        });

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
        // 1. Top Header Bar (Dark Teal #123c3a, height 34px)
        pnlTopBar = new JPanel(new BorderLayout());
        pnlTopBar.setBackground(new Color(18, 60, 58));
        pnlTopBar.setPreferredSize(new Dimension(1320, 34));
        pnlTopBar.setBorder(new EmptyBorder(0, 16, 0, 16));

        JPanel pnlWestTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pnlWestTitle.setOpaque(false);

        JLabel lblTitle = new JLabel("Rekonsiliasi Obat");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(Color.WHITE);
        pnlWestTitle.add(lblTitle);

        JLabel lblCode = new JLabel("RM 03-06A", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(26, 74, 71));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                g2.setColor(new Color(44, 90, 86));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblCode.setForeground(new Color(143, 181, 174));
        lblCode.setBorder(new EmptyBorder(1, 6, 1, 6));
        pnlWestTitle.add(lblCode);
        pnlTopBar.add(pnlWestTitle, BorderLayout.WEST);

        JLabel lblHospital = new JLabel("RSUD Demang Sepulau Raya");
        lblHospital.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHospital.setForeground(new Color(143, 181, 174));
        pnlTopBar.add(lblHospital, BorderLayout.EAST);

        // 2. TabbedPane setup with modern styling
        TabPilihan = new javax.swing.JTabbedPane();
        TabPilihan.setFont(new Font("Segoe UI", Font.BOLD, 11));
        TabPilihan.putClientProperty("JTabbedPane.tabHeight", 38);
        TabPilihan.putClientProperty("JTabbedPane.tabInsets", new Insets(0, 16, 0, 16));
        TabPilihan.putClientProperty("JTabbedPane.underlineColor", new Color(13, 122, 112));
        TabPilihan.putClientProperty("JTabbedPane.selectedBackground", Color.WHITE);
        TabPilihan.putClientProperty("JTabbedPane.selectedForeground", new Color(13, 122, 112));
        TabPilihan.putClientProperty("JTabbedPane.showTabSeparators", false);

        // 3. TAB 1: Reconstruct internalFrame1 (Input Tab)
        internalFrame1.removeAll();
        internalFrame1.setBorder(null);
        internalFrame1.setLayout(new BorderLayout(0, 8));
        internalFrame1.setBackground(Color.WHITE);
        internalFrame1.setBorder(new EmptyBorder(10, 16, 10, 16));

        // Top Section: Patient Card (West) + Form Controls (Center)
        pnlTopSection = new JPanel(new BorderLayout(14, 0));
        pnlTopSection.setBackground(Color.WHITE);
        pnlTopSection.setPreferredSize(new Dimension(1320, 165));

        // LEFT: Patient Identity Card, mengikuti ukuran dan hierarki visual RM IGD
        pnlCardPatient = createPatientCard();
        pnlTopSection.add(pnlCardPatient, BorderLayout.WEST);

        // RIGHT: Header Form Controls
        JPanel pnlRightHeader = createRightHeaderPanel();
        pnlTopSection.add(pnlRightHeader, BorderLayout.CENTER);

        internalFrame1.add(pnlTopSection, BorderLayout.NORTH);

        // MIDDLE: Table Section & Allergy Card
        JPanel pnlTableSection = createTableSection();
        internalFrame1.add(pnlTableSection, BorderLayout.CENTER);

        // BOTTOM: Action Bar & Footer (56px height), dipasang global agar
        // tetap terlihat pada tab Input maupun Data seperti form IGD.
        JPanel pnlBottomSection = createBottomSection();

        // Add Tabs
        TabPilihan.addTab("Input Rekonsiliasi Obat", internalFrame1);
        initDataRekonsiliasiTab();
        TabPilihan.addChangeListener((javax.swing.event.ChangeEvent evt) -> {
            if(TabPilihan.getSelectedIndex()==1){
                tampilDataRekonsiliasi();
            }
            updateFooterUntukTab();
        });

        // Outer layout on JDialog
        getContentPane().removeAll();
        JPanel pnlRoot = new JPanel(new BorderLayout(0, 0));
        pnlRoot.setBackground(Color.WHITE);
        pnlRoot.add(pnlTopBar, BorderLayout.PAGE_START);
        pnlRoot.add(TabPilihan, BorderLayout.CENTER);
        pnlRoot.add(pnlBottomSection, BorderLayout.PAGE_END);
        getContentPane().add(pnlRoot, BorderLayout.CENTER);

        // Initial updates
        perbaruiVisibilitasFieldAdmisi();
        updateSegmentedButtons();
        updatePatientCard();
        updateCounterObat();
        updateStatusRekon();
        updateFooterMeta();
        updateFooterUntukTab();
        buatKotakFieldRekonsiliasi(internalFrame1);
        buatKotakFieldRekonsiliasi(NamaObat);
        buatKotakFieldRekonsiliasi(DosisObat);
        buatKotakFieldRekonsiliasi(Frekuensi);
        buatKotakFieldRekonsiliasi(AturanPakai);
        buatKotakFieldRekonsiliasi(PerubahanAturanPakai);
    }

    private JPanel createPatientCard() {
        JPanel card = new JPanel(null);
        card.setOpaque(true);
        card.setBackground(ThemeConfig.SURFACE_ALT);
        card.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT));
        Dimension ukuranKartu = new Dimension(310, 160);
        card.setPreferredSize(ukuranKartu);
        card.setMinimumSize(ukuranKartu);

        JLabel lblHeader = new JLabel("IDENTITAS PASIEN");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHeader.setForeground(new Color(139, 146, 141));
        lblHeader.setBounds(14, 11, 150, 16);
        card.add(lblHeader);

        JLabel lblBadge = new JLabel("  TERKUNCI  ", SwingConstants.CENTER);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(ThemeConfig.BORDER_SOFT);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblBadge.setForeground(new Color(123, 130, 125));
        lblBadge.setBounds(230, 10, 65, 17);
        card.add(lblBadge);

        lblCardInitials = new JLabel("-", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int diameter = Math.max(0, Math.min(getWidth(), getHeight()) - 1);
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;
                g2.setColor(ThemeConfig.ACCENT);
                g2.fillOval(x, y, diameter, diameter);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblCardInitials.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCardInitials.setForeground(Color.WHITE);
        lblCardInitials.setBounds(14, 36, 40, 40);
        card.add(lblCardInitials);

        lblCardNama = new JLabel("Nama Pasien");
        lblCardNama.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCardNama.setForeground(new Color(31, 36, 33));
        lblCardNama.setBounds(64, 36, 231, 20);
        card.add(lblCardNama);

        lblCardJk = new JLabel("-");
        lblCardJk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCardJk.setForeground(new Color(139, 146, 141));
        lblCardJk.setBounds(64, 58, 231, 16);
        card.add(lblCardJk);

        int y = 88;
        addCardKeyValue(card, "No. RM", lblCardNoRM = new JLabel("-"), y);
        addCardKeyValue(card, "No. Rawat", lblCardNoRw = new JLabel("-"), y + 19);
        addCardKeyValue(card, "Tgl. Lahir", lblCardTglLahir = new JLabel("-"), y + 38);

        return card;
    }

    private void addCardKeyValue(JPanel p, String key, JLabel valLabel, int y) {
        JLabel k = new JLabel(key);
        k.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        k.setForeground(new Color(139, 146, 141));
        k.setBounds(14, y, 76, 16);
        p.add(k);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        valLabel.setForeground(new Color(31, 36, 33));
        valLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valLabel.setBounds(96, y, 199, 16);
        p.add(valLabel);
    }

    /** Samakan bentuk kontrol input dengan RM IGD: sudut siku-siku dan garis datar. */
    private static void buatKotakFieldRekonsiliasi(Component component) {
        if (component == null) return;
        if (component instanceof javax.swing.JComponent) {
            javax.swing.JComponent jc = (javax.swing.JComponent) component;
            jc.putClientProperty("Component.arc", 0);
            jc.putClientProperty("TextComponent.arc", 0);
            jc.putClientProperty("JTextField.arc", 0);
            jc.putClientProperty("ComboBox.arc", 0);
            jc.putClientProperty("JComponent.roundRect", false);

            if (component instanceof javax.swing.text.JTextComponent) {
                jc.setBackground(Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                    "background: #FFFFFF; inactiveBackground: #FFFFFF; disabledBackground: #FFFFFF;");
            } else if (component instanceof javax.swing.JComboBox) {
                jc.setBackground(Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                    "background: #FFFFFF; disabledBackground: #FFFFFF; buttonBackground: #FFFFFF; editableBackground: #FFFFFF;");
            } else if (component instanceof uz.ncipro.calendar.JDateTimePicker) {
                jc.setBackground(Color.WHITE);
                jc.setOpaque(true);
                jc.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT));
                jc.putClientProperty("FlatLaf.style", "background: #FFFFFF; buttonBackground: #FFFFFF;");
            }
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                buatKotakFieldRekonsiliasi(child);
            }
        }
    }

    private JPanel createRightHeaderPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(ThemeConfig.SURFACE);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weighty = 0;

        JPanel pnlApotekerInput = new JPanel(new BorderLayout(0, 0));
        pnlApotekerInput.setOpaque(true);
        pnlApotekerInput.setBackground(Color.WHITE);
        pnlApotekerInput.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT));
        pnlApotekerInput.setPreferredSize(new Dimension(220, 30));
        // Kode/NIP tetap menjadi nilai internal untuk validasi dan penyimpanan,
        // tetapi tampilan mengikuti form IGD yang hanya memperlihatkan nama.
        KdPetugas.setEditable(false);
        KdPetugas.setVisible(false);
        NmPetugas.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        NmPetugas.setVisible(true);
        NmPetugas.setEditable(false);
        NmPetugas.setBackground(Color.WHITE);
        NmPetugas.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        NmPetugas.putClientProperty("lookupField", Boolean.TRUE);
        BtnPetugas.setPreferredSize(new Dimension(32, 30));
        BtnPetugas.setBackground(Color.WHITE);
        BtnPetugas.setGlassColor(Color.WHITE);
        BtnPetugas.setRoundRect(false);
        BtnPetugas.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ThemeConfig.BORDER_SOFT));
        BtnPetugas.putClientProperty("JButton.buttonType", "square");
        pnlApotekerInput.add(NmPetugas, BorderLayout.CENTER);
        pnlApotekerInput.add(BtnPetugas, BorderLayout.EAST);

        TNoRekonsialiasi.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        TNoRekonsialiasi.setEditable(false);
        TNoRekonsialiasi.setBackground(Color.WHITE);
        TNoRekonsialiasi.setPreferredSize(new Dimension(220, 30));
        TNoRekonsialiasi.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));

        // Mengikuti konsep Registrasi IGD: [tanggal] Jam : [JJ] [MM] [DD].
        JPanel pnlTglJam = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        pnlTglJam.setOpaque(false);
        pnlTglJam.setBorder(null);
        pnlTglJam.setPreferredSize(new Dimension(360, 30));
        Dimension ukuranTanggal = new Dimension(112, 28);
        Dimension ukuranJam = new Dimension(62, 28);
        Tanggal.setPreferredSize(ukuranTanggal);
        Tanggal.setMinimumSize(ukuranTanggal);
        CmbJam.setPreferredSize(ukuranJam);
        CmbJam.setMinimumSize(ukuranJam);
        CmbMenit.setPreferredSize(ukuranJam);
        CmbMenit.setMinimumSize(ukuranJam);
        CmbDetik.setVisible(true);
        CmbDetik.setPreferredSize(ukuranJam);
        CmbDetik.setMinimumSize(ukuranJam);
        JLabel lblJam = new JLabel("Jam :");
        lblJam.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 10));
        lblJam.setForeground(ThemeConfig.TEXT_MUTED);
        lblJam.setHorizontalAlignment(SwingConstants.RIGHT);
        lblJam.setPreferredSize(new Dimension(38, 28));
        pnlTglJam.add(Tanggal);
        pnlTglJam.add(lblJam);
        pnlTglJam.add(CmbJam);
        pnlTglJam.add(CmbMenit);
        pnlTglJam.add(CmbDetik);

        JPanel[] fieldUtama = {
            panelFieldRekonsiliasi("APOTEKER", pnlApotekerInput),
            panelFieldRekonsiliasi("NO. REKONSILIASI", TNoRekonsialiasi),
            panelFieldRekonsiliasi("TANGGAL & JAM WAWANCARA", pnlTglJam)
        };
        for (int kolom = 0; kolom < fieldUtama.length; kolom++) {
            c.gridx = kolom;
            c.gridy = 0;
            c.weightx = 1;
            c.insets = new Insets(0, kolom == 0 ? 0 : 10, 6, 0);
            p.add(fieldUtama[kolom], c);
        }

        JPanel pnlTahap = new JPanel(new BorderLayout(0, 3));
        pnlTahap.setOpaque(false);
        pnlTahap.add(labelFieldRekonsiliasi("REKONSILIASI SAAT"), BorderLayout.NORTH);
        JPanel segPills = new JPanel(new GridLayout(1, 3, -1, 0));
        segPills.setOpaque(false);
        segPills.setPreferredSize(new Dimension(430, 28));
        btnSegAdmisi = createSegmentedButton("Admisi");
        btnSegTransfer = createSegmentedButton("Transfer Antar Ruang");
        btnSegPulang = createSegmentedButton("Pulang");
        segPills.add(btnSegAdmisi);
        segPills.add(btnSegTransfer);
        segPills.add(btnSegPulang);
        JPanel pnlTahapKiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTahapKiri.setOpaque(false);
        pnlTahapKiri.add(segPills);
        pnlTahap.add(pnlTahapKiri, BorderLayout.CENTER);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 6, 0);
        p.add(pnlTahap, c);

        pnlGrupAdmisi = new JPanel(new GridBagLayout());
        pnlGrupAdmisi.setBackground(ThemeConfig.SURFACE);
        GridBagConstraints a = new GridBagConstraints();
        a.gridy = 0;
        a.fill = GridBagConstraints.HORIZONTAL;
        a.anchor = GridBagConstraints.NORTHWEST;
        a.weightx = 1;

        JPanel pnlObatAdmisi = new JPanel(new BorderLayout(0, 3));
        pnlObatAdmisi.setOpaque(false);
        pnlObatAdmisi.add(labelFieldRekonsiliasi("PENGGUNAAN OBAT SEBELUM ADMISI *"), BorderLayout.NORTH);
        JPanel pilihanObat = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pilihanObat.setOpaque(false);
        styleRadioButton(RbObatSebelumAdmisiYa, false);
        styleRadioButton(RbObatSebelumAdmisiTidak, false);
        pilihanObat.add(RbObatSebelumAdmisiYa);
        pilihanObat.add(RbObatSebelumAdmisiTidak);
        pnlObatAdmisi.add(pilihanObat, BorderLayout.CENTER);
        a.gridx = 0;
        a.insets = new Insets(0, 0, 0, 12);
        pnlGrupAdmisi.add(pnlObatAdmisi, a);

        JPanel pnlSumber = new JPanel(new BorderLayout(0, 3));
        pnlSumber.setOpaque(false);
        pnlSumber.add(labelFieldRekonsiliasi("SUMBER INFORMASI *"), BorderLayout.NORTH);
        JPanel pilihanSumber = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pilihanSumber.setOpaque(false);
        styleRadioButton(RbSumberRM, false);
        styleRadioButton(RbSumberPasien, false);
        styleRadioButton(RbSumberLain, false);
        pilihanSumber.add(RbSumberRM);
        pilihanSumber.add(RbSumberPasien);
        pilihanSumber.add(RbSumberLain);
        TSumberLainKet.setPreferredSize(new Dimension(175, 26));
        TSumberLainKet.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 10));
        TSumberLainKet.setBackground(ThemeConfig.SURFACE_ALT);
        TSumberLainKet.setText("Sebutkan...");
        TSumberLainKet.setForeground(ThemeConfig.TEXT_FAINT);
        pilihanSumber.add(TSumberLainKet);
        pnlSumber.add(pilihanSumber, BorderLayout.CENTER);
        a.gridx = 1;
        a.insets = new Insets(0, 0, 0, 0);
        pnlGrupAdmisi.add(pnlSumber, a);

        TSumberLainKet.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if ("Sebutkan...".equals(TSumberLainKet.getText())) {
                    TSumberLainKet.setText("");
                    TSumberLainKet.setForeground(ThemeConfig.TEXT_NORMAL);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (TSumberLainKet.getText().trim().isEmpty()) {
                    TSumberLainKet.setText("Sebutkan...");
                    TSumberLainKet.setForeground(ThemeConfig.TEXT_FAINT);
                }
            }
        });
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        p.add(pnlGrupAdmisi, c);

        // Glue pengisi vertikal agar baris Apoteker, No Rekonsiliasi, dan Rekonsiliasi Saat
        // selalu terkunci di paling atas (tidak melayang turun ke tengah saat grup Admisi sembunyi).
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        p.add(javax.swing.Box.createGlue(), c);

        return p;
    }

    private JLabel labelFieldRekonsiliasi(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        label.setForeground(ThemeConfig.TEXT_FAINT);
        return label;
    }

    private JPanel panelFieldRekonsiliasi(String label, Component field) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);
        panel.add(labelFieldRekonsiliasi(label), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        int lebarField = field.getPreferredSize() == null ? 220 : field.getPreferredSize().width;
        panel.setPreferredSize(new Dimension(Math.max(220, lebarField), 50));
        return panel;
    }

    private JButton createSegmentedButton(final String text) {
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                boolean isSel = text.equalsIgnoreCase(RekonsiliasiSaat.getSelectedItem() != null ? RekonsiliasiSaat.getSelectedItem().toString() : "");
                if (isSel) {
                    g2.setColor(new Color(13, 122, 112));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(new Color(224, 226, 221));
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                }

                g2.setFont(getFont());
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(text)) / 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, textX, textY);
                g2.dispose();
            }
        };
        b.setText(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        btnSegAdmisi.setForeground("Admisi".equalsIgnoreCase(saat) ? Color.WHITE : new Color(47, 54, 50));
        btnSegTransfer.setFont(new Font("Segoe UI", "Transfer Antar Ruang".equalsIgnoreCase(saat) ? Font.BOLD : Font.PLAIN, 10));
        btnSegTransfer.setForeground("Transfer Antar Ruang".equalsIgnoreCase(saat) ? Color.WHITE : new Color(47, 54, 50));
        btnSegPulang.setFont(new Font("Segoe UI", "Pulang".equalsIgnoreCase(saat) ? Font.BOLD : Font.PLAIN, 10));
        btnSegPulang.setForeground("Pulang".equalsIgnoreCase(saat) ? Color.WHITE : new Color(47, 54, 50));
        btnSegAdmisi.repaint();
        btnSegTransfer.repaint();
        btnSegPulang.repaint();
    }

    private JPanel createAllergyCard() {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(224, 226, 221));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(8, 12, 8, 12));
        card.setPreferredSize(new Dimension(1000, 116));

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("INFORMASI KLINIS TAMBAHAN · RIWAYAT ALERGI OBAT (TIDAK TERCETAK DI RM 03-06A)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(new Color(139, 146, 141));
        pnlTitle.add(lblTitle);
        card.add(pnlTitle, BorderLayout.NORTH);

        JPanel pnlInputs = new JPanel(new GridBagLayout());
        pnlInputs.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 8);
        c.weighty = 1.0;

        // Col 1: Obat Penyebab Alergi
        c.gridx = 0;
        c.weightx = 0.42;
        JPanel p1 = new JPanel(new BorderLayout(0, 2));
        p1.setOpaque(false);
        JLabel l1 = new JLabel("OBAT PENYEBAB ALERGI");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l1.setForeground(new Color(139, 146, 141));
        p1.add(l1, BorderLayout.NORTH);

        AlergiObat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        AlergiObat.setForeground(new Color(31, 36, 33));
        AlergiObat.setRows(3);
        scrollPane2.setVisible(true);
        AlergiObat.setVisible(true);
        scrollPane2.setPreferredSize(new Dimension(240, 56));
        scrollPane2.setMinimumSize(new Dimension(120, 56));
        scrollPane2.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 226, 221), 1),
            BorderFactory.createEmptyBorder(1, 4, 1, 4)
        ));
        p1.add(scrollPane2, BorderLayout.CENTER);
        pnlInputs.add(p1, c);

        // Col 2: Manifestasi Alergi
        c.gridx = 1;
        c.weightx = 0.42;
        JPanel p2 = new JPanel(new BorderLayout(0, 2));
        p2.setOpaque(false);
        JLabel l2 = new JLabel("GEJALA / MANIFESTASI KLINIS");
        l2.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l2.setForeground(new Color(139, 146, 141));
        p2.add(l2, BorderLayout.NORTH);

        ManifestasiAlergi.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ManifestasiAlergi.setForeground(new Color(31, 36, 33));
        ManifestasiAlergi.setRows(3);
        scrollPane3.setVisible(true);
        ManifestasiAlergi.setVisible(true);
        scrollPane3.setPreferredSize(new Dimension(240, 56));
        scrollPane3.setMinimumSize(new Dimension(120, 56));
        scrollPane3.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 226, 221), 1),
            BorderFactory.createEmptyBorder(1, 4, 1, 4)
        ));
        p2.add(scrollPane3, BorderLayout.CENTER);
        pnlInputs.add(p2, c);

        // Col 3: Dampak Alergi
        c.gridx = 2;
        c.weightx = 0.16;
        c.insets = new Insets(0, 0, 0, 0);
        JPanel p3 = new JPanel(new BorderLayout(0, 2));
        p3.setOpaque(false);
        JLabel l3 = new JLabel("DAMPAK ALERGI");
        l3.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l3.setForeground(new Color(139, 146, 141));
        p3.add(l3, BorderLayout.NORTH);

        DampakAlergi.setVisible(true);
        DampakAlergi.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        DampakAlergi.setPreferredSize(new Dimension(110, 30));
        JPanel pnlDampakInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlDampakInput.setOpaque(false);
        pnlDampakInput.add(DampakAlergi);
        p3.add(pnlDampakInput, BorderLayout.CENTER);
        pnlInputs.add(p3, c);

        card.add(pnlInputs, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTableSection() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);

        // Card I: Tabel Obat
        JPanel cardTabel = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(224, 226, 221));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cardTabel.setOpaque(false);
        cardTabel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel pnlTableHeader = new JPanel(new BorderLayout());
        pnlTableHeader.setOpaque(false);

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTitle.setOpaque(false);
        JLabel lTitle = new JLabel("DAFTAR OBAT DIREKONSILIASI");
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lTitle.setForeground(new Color(139, 146, 141));
        pnlTitle.add(lTitle);

        lblCountObat = new JLabel("0 obat", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(232, 234, 229));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblCountObat.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblCountObat.setForeground(new Color(123, 130, 125));
        lblCountObat.setBorder(new EmptyBorder(1, 6, 1, 6));
        pnlTitle.add(lblCountObat);
        pnlTableHeader.add(pnlTitle, BorderLayout.WEST);

        JPanel pnlTableBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlTableBtns.setOpaque(false);

        styleOutlineButton(BtnHapus);
        BtnHapus.setText("Hapus Baris");
        BtnHapus.setPreferredSize(new Dimension(90, 26));
        pnlTableBtns.add(BtnHapus);

        styleSolidTealButton(BtnTambah);
        BtnTambah.setText("+ Tambah Obat");
        BtnTambah.setPreferredSize(new Dimension(120, 26));
        pnlTableBtns.add(BtnTambah);

        pnlTableHeader.add(pnlTableBtns, BorderLayout.EAST);
        cardTabel.add(pnlTableHeader, BorderLayout.NORTH);

        Scroll2.setBorder(BorderFactory.createLineBorder(new Color(224, 226, 221)));
        Scroll2.getViewport().setBackground(Color.WHITE);
        cardTabel.add(Scroll2, BorderLayout.CENTER);

        p.add(cardTabel, BorderLayout.CENTER);

        // Card II: Riwayat Alergi Obat
        JPanel cardAlergi = createAllergyCard();
        p.add(cardAlergi, BorderLayout.SOUTH);

        return p;
    }

    private JPanel createBottomSection() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(1320, 56));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(224, 226, 221)),
            new EmptyBorder(8, 0, 4, 0)
        ));

        JPanel pnlActRow = new JPanel(new BorderLayout());
        pnlActRow.setBackground(Color.WHITE);

        // Aksi di kiri berubah sesuai konteks tab; status dan Keluar tetap di kanan.
        JPanel pnlInputBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlInputBtns.setBackground(Color.WHITE);

        styleFooterActionButton(BtnSimpan, 100);
        BtnSimpan.setText("Simpan");
        pnlInputBtns.add(BtnSimpan);

        styleFooterActionButton(BtnBatal, 90);
        BtnBatal.setText("Baru");
        pnlInputBtns.add(BtnBatal);

        JPanel pnlDataBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlDataBtns.setBackground(Color.WHITE);

        BtnBaruData = new widget.Button();
        BtnBaruData.setText("Rekonsiliasi Baru");
        BtnBaruData.setIcon(widget.IconStyle.muat("plus.svg",14,ThemeConfig.PRIMARY));
        BtnBaruData.setToolTipText("Buka tab Input untuk membuat rekonsiliasi baru");
        styleFooterActionButton(BtnBaruData, 150);
        BtnBaruData.addActionListener(e -> mulaiRekonsiliasiBaruDariData());
        pnlDataBtns.add(BtnBaruData);

        BtnGantiData = new widget.Button();
        BtnGantiData.setText("Ganti");
        BtnGantiData.setIcon(widget.IconStyle.muat("pencil.svg",14,widget.IconStyle.ORANYE));
        BtnGantiData.setToolTipText("Muat rekonsiliasi terpilih ke tab Input untuk diubah");
        styleFooterActionButton(BtnGantiData, 92);
        BtnGantiData.setEnabled(false);
        BtnGantiData.addActionListener(e -> muatRekonsiliasiTerpilihUntukGanti());
        pnlDataBtns.add(BtnGantiData);

        BtnHapusData = new widget.Button();
        BtnHapusData.setText("Hapus");
        BtnHapusData.setIcon(widget.IconStyle.muat("trash.svg",14,ThemeConfig.DANGER));
        BtnHapusData.setForeground(ThemeConfig.DANGER);
        BtnHapusData.setToolTipText("Hapus permanen rekonsiliasi terpilih");
        styleFooterActionButton(BtnHapusData, 96);
        BtnHapusData.setForeground(ThemeConfig.DANGER);
        BtnHapusData.setEnabled(false);
        BtnHapusData.addActionListener(e -> hapusRekonsiliasiTerpilih());
        pnlDataBtns.add(BtnHapusData);

        BtnAllData = new widget.Button();
        BtnAllData.setText("Semua");
        BtnAllData.setIcon(widget.IconStyle.muat("list.svg",14,widget.IconStyle.ABU));
        BtnAllData.setToolTipText("Tampilkan semua data rekonsiliasi obat");
        styleFooterActionButton(BtnAllData, 94);
        BtnAllData.addActionListener(e -> tampilkanSemuaData());
        pnlDataBtns.add(BtnAllData);

        footerActionLayout = new CardLayout();
        pnlFooterActionCards = new JPanel(footerActionLayout);
        pnlFooterActionCards.setBackground(Color.WHITE);
        pnlFooterActionCards.add(pnlInputBtns,"input");
        pnlFooterActionCards.add(pnlDataBtns,"data");
        pnlActRow.add(pnlFooterActionCards, BorderLayout.WEST);

        JPanel pnlRight = new JPanel(new BorderLayout(12, 0));
        pnlRight.setBackground(Color.WHITE);
        JPanel pnlStatus = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlStatus.setBackground(Color.WHITE);

        lblStatusRekon = new JLabel("Rekonsiliasi saat Admisi");
        lblStatusRekon.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblStatusRekon.setForeground(new Color(47, 54, 50));
        pnlStatus.add(lblStatusRekon);

        lblFooterMeta = new JLabel("-");
        lblFooterMeta.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblFooterMeta.setForeground(new Color(139, 146, 141));
        pnlStatus.add(lblFooterMeta);
        pnlRight.add(pnlStatus, BorderLayout.CENTER);

        styleFooterActionButton(BtnKeluar, 90);
        BtnKeluar.setText("Keluar");
        BtnKeluar.setForeground(ThemeConfig.DANGER);
        pnlRight.add(BtnKeluar, BorderLayout.EAST);
        pnlActRow.add(pnlRight, BorderLayout.EAST);
        // Pastikan ikon SVG dan hover aksi konsisten dengan action bar form IGD.
        widget.IconStyle.pasang(pnlActRow);
        p.add(pnlActRow, BorderLayout.CENTER);

        return p;
    }

    /** Tombol card footer mengikuti pola action bar RM IGD. */
    private void styleFooterActionButton(widget.Button button, int width) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(Color.WHITE);
        button.setGlassColor(Color.WHITE);
        button.setRoundRect(false);
        button.putClientProperty("Component.arc", 4);
        button.putClientProperty("Button.arc", 4);
        button.putClientProperty("JButton.buttonType", "square");
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setIconTextGap(6);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        button.setPreferredSize(new Dimension(width, 32));
    }

    private void updateFooterUntukTab(){
        if(TabPilihan==null || footerActionLayout==null || pnlFooterActionCards==null){ return; }
        boolean tabData = TabPilihan.getSelectedIndex()==1;
        footerActionLayout.show(pnlFooterActionCards,tabData?"data":"input");
        if(tabData){
            perbaruiAksiDataTerpilih();
        }else{
            updateStatusRekon();
            updateFooterMeta();
        }
        pnlFooterActionCards.revalidate();
        pnlFooterActionCards.repaint();
    }

    private void perbaruiAksiDataTerpilih(){
        boolean terpilih = tbDataRekonsiliasi!=null && tbDataRekonsiliasi.getSelectedRow()>=0;
        boolean bolehUbah = akses.getrekonsiliasi_obat();
        if(BtnBaruData!=null){ BtnBaruData.setEnabled(bolehUbah); }
        if(BtnGantiData!=null){ BtnGantiData.setEnabled(terpilih && bolehUbah); }
        if(BtnHapusData!=null){ BtnHapusData.setEnabled(terpilih && bolehUbah); }
        if(lblStatusRekon!=null){
            lblStatusRekon.setText(terpilih
                ? "Data Rekonsiliasi · "+tbDataRekonsiliasi.getValueAt(tbDataRekonsiliasi.getSelectedRow(),0)
                : "Data Rekonsiliasi Obat");
        }
        if(lblFooterMeta!=null){
            lblFooterMeta.setText(terpilih
                ? tbDataRekonsiliasi.getValueAt(tbDataRekonsiliasi.getSelectedRow(),1).toString()
                : "Pilih data untuk melihat rincian, mengganti, atau menghapus");
        }
    }

    private void aturModeEdit(boolean edit){
        if(!edit){ noRekonsiliasiEdit=null; }
        if(BtnSimpan!=null){
            BtnSimpan.setText(edit?"Simpan Perubahan":"Simpan");
            BtnSimpan.setPreferredSize(new Dimension(edit?142:100,32));
        }
        updateStatusRekon();
    }

    private void mulaiRekonsiliasiBaruDariData(){
        if(!akses.getrekonsiliasi_obat()){
            JOptionPane.showMessageDialog(rootPane,"Anda tidak memiliki hak akses Rekonsiliasi Obat.");
            return;
        }
        if(tabMode.getRowCount()>0){
            int jawab = JOptionPane.showConfirmDialog(rootPane,
                    "Daftar obat pada form Input akan dikosongkan. Lanjut membuat rekonsiliasi baru?",
                    "Konfirmasi Rekonsiliasi Baru",JOptionPane.YES_NO_OPTION);
            if(jawab!=JOptionPane.YES_OPTION){ return; }
        }
        Valid.tabelKosong(tabMode);
        aturModeEdit(false);
        emptTeks();
        TabPilihan.setSelectedIndex(0);
    }

    private void tampilkanSemuaData(){
        TCariData.setText("Ketik kata kunci...");
        TCariData.setForeground(ThemeConfig.TEXT_FAINT);
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.YEAR,-10);
        Tgl1Data.setDate(cal.getTime());
        cal.add(Calendar.YEAR,20);
        Tgl2Data.setDate(cal.getTime());
        tampilDataRekonsiliasi();
    }

    private void muatRekonsiliasiTerpilihUntukGanti(){
        if(!akses.getrekonsiliasi_obat()){
            JOptionPane.showMessageDialog(rootPane,"Anda tidak memiliki hak akses untuk mengganti rekonsiliasi obat.");
            return;
        }
        if(tbDataRekonsiliasi==null || tbDataRekonsiliasi.getSelectedRow()<0){
            JOptionPane.showMessageDialog(rootPane,"Pilih data rekonsiliasi yang akan diganti.");
            return;
        }

        String noRek = tbDataRekonsiliasi.getValueAt(tbDataRekonsiliasi.getSelectedRow(),0).toString();
        PreparedStatement psHeader=null, psDetail=null;
        ResultSet rsHeader=null, rsDetail=null;
        try{
            psHeader=koneksi.prepareStatement(
                "select rekonsiliasi_obat.*,petugas.nama as nama_petugas from rekonsiliasi_obat "+
                "inner join petugas on rekonsiliasi_obat.nip=petugas.nip where rekonsiliasi_obat.no_rekonsiliasi=?");
            psHeader.setString(1,noRek);
            rsHeader=psHeader.executeQuery();
            if(!rsHeader.next()){
                JOptionPane.showMessageDialog(rootPane,"Data rekonsiliasi tidak ditemukan.");
                return;
            }

            String noRawat=rsHeader.getString("no_rawat");
            Timestamp tanggalWawancara=rsHeader.getTimestamp("tanggal_wawancara");
            String saat=rsHeader.getString("rekonsiliasi_obat_saat");
            String alergi=rsHeader.getString("alergi_obat");
            String manifestasi=rsHeader.getString("manifestasi_alergi");
            String dampak=rsHeader.getString("dampak_alergi");
            String nip=rsHeader.getString("nip");
            String namaPetugas=rsHeader.getString("nama_petugas");
            String penggunaan=safeGetString(rsHeader,"penggunaan_obat_sebelum_admisi");
            String sumber=safeGetString(rsHeader,"sumber_informasi");
            String sumberLain=safeGetString(rsHeader,"sumber_informasi_lain");

            ArrayList<Object[]> daftarObat=new ArrayList<>();
            psDetail=koneksi.prepareStatement(
                "select nama_obat,dosis_obat,frekuensi,cara_pemberian,waktu_pemberian_terakhir,tindak_lanjut,perubahan_aturan_pakai "+
                "from rekonsiliasi_obat_detail_obat where no_rekonsiliasi=?");
            psDetail.setString(1,noRek);
            rsDetail=psDetail.executeQuery();
            while(rsDetail.next()){
                daftarObat.add(new Object[]{"",rsDetail.getString("nama_obat"),rsDetail.getString("dosis_obat"),
                    rsDetail.getString("frekuensi"),rsDetail.getString("cara_pemberian"),
                    rsDetail.getString("tindak_lanjut"),rsDetail.getString("perubahan_aturan_pakai"),
                    rsDetail.getString("waktu_pemberian_terakhir")});
            }

            setNoRm(noRawat);
            TNoRekonsialiasi.setText(noRek);
            ChkJln.setSelected(false);
            if(tanggalWawancara!=null){
                Date tanggalEdit=new Date(tanggalWawancara.getTime());
                Tanggal.setDate(tanggalEdit);
                Calendar kalender=Calendar.getInstance();
                kalender.setTime(tanggalEdit);
                CmbJam.setSelectedItem(String.format("%02d",kalender.get(Calendar.HOUR_OF_DAY)));
                CmbMenit.setSelectedItem(String.format("%02d",kalender.get(Calendar.MINUTE)));
                CmbDetik.setSelectedItem(String.format("%02d",kalender.get(Calendar.SECOND)));
            }
            RekonsiliasiSaat.setSelectedItem(saat);
            AlergiObat.setText(alergi==null?"":alergi);
            ManifestasiAlergi.setText(manifestasi==null?"":manifestasi);
            DampakAlergi.setSelectedItem(dampak==null?"-":dampak);
            KdPetugas.setText(nip==null?"":nip);
            NmPetugas.setText(namaPetugas==null?"":namaPetugas);

            bgObatSebelumAdmisi.clearSelection();
            bgSumberInformasi.clearSelection();
            if("Ya".equals(penggunaan)){ RbObatSebelumAdmisiYa.setSelected(true); }
            else if("Tidak".equals(penggunaan)){ RbObatSebelumAdmisiTidak.setSelected(true); }
            if("Status Pasien di Rekam Medis".equals(sumber)){ RbSumberRM.setSelected(true); }
            else if("Pasien/Keluarga Pasien".equals(sumber)){ RbSumberPasien.setSelected(true); }
            else if("Lain-lain".equals(sumber)){ RbSumberLain.setSelected(true); }
            perbaruiKeteranganSumber();
            if(RbSumberLain.isSelected() && sumberLain!=null && !sumberLain.trim().isEmpty()){
                TSumberLainKet.setText(sumberLain);
                TSumberLainKet.setForeground(ThemeConfig.TEXT_NORMAL);
            }

            Valid.tabelKosong(tabMode);
            for(Object[] obat:daftarObat){ tabMode.addRow(obat); }
            noRekonsiliasiEdit=noRek;
            aturModeEdit(true);
            updatePatientCard();
            updateCounterObat();
            perbaruiVisibilitasFieldAdmisi();
            TabPilihan.setSelectedIndex(0);
            BtnSimpan.requestFocusInWindow();
        }catch(Exception e){
            System.out.println("Notif muat Ganti Rekonsiliasi : "+e);
            JOptionPane.showMessageDialog(rootPane,"Gagal memuat data rekonsiliasi untuk diganti.");
        }finally{
            try{ if(rsDetail!=null){rsDetail.close();} }catch(Exception e){}
            try{ if(psDetail!=null){psDetail.close();} }catch(Exception e){}
            try{ if(rsHeader!=null){rsHeader.close();} }catch(Exception e){}
            try{ if(psHeader!=null){psHeader.close();} }catch(Exception e){}
        }
    }

    private String tanggalWawancaraForm(){
        return Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+CmbJam.getSelectedItem()+":"+
               CmbMenit.getSelectedItem()+":"+CmbDetik.getSelectedItem();
    }

    private void gantiRekonsiliasiDalamTransaksi(){
        final String noRekLama=noRekonsiliasiEdit;
        boolean autoCommitLama=true;
        Savepoint savepoint=null;
        PreparedStatement psUpdate=null, psHapusDetail=null, psTambahDetail=null;
        boolean sukses=false;
        try{
            autoCommitLama=koneksi.getAutoCommit();
            if(autoCommitLama){ koneksi.setAutoCommit(false); }
            else{ savepoint=koneksi.setSavepoint("ganti_rekonsiliasi_obat"); }

            psUpdate=koneksi.prepareStatement(
                "update rekonsiliasi_obat set no_rawat=?,tanggal_wawancara=?,rekonsiliasi_obat_saat=?,"+
                "alergi_obat=?,manifestasi_alergi=?,dampak_alergi=?,nip=?,penggunaan_obat_sebelum_admisi=?,"+
                "sumber_informasi=?,sumber_informasi_lain=? where no_rekonsiliasi=?");
            psUpdate.setString(1,TNoRw.getText());
            psUpdate.setString(2,tanggalWawancaraForm());
            psUpdate.setString(3,RekonsiliasiSaat.getSelectedItem().toString());
            psUpdate.setString(4,AlergiObat.getText());
            psUpdate.setString(5,ManifestasiAlergi.getText());
            psUpdate.setString(6,DampakAlergi.getSelectedItem().toString());
            psUpdate.setString(7,KdPetugas.getText());
            psUpdate.setString(8,pilihRadio(RbObatSebelumAdmisiYa,"Ya",RbObatSebelumAdmisiTidak,"Tidak"));
            psUpdate.setString(9,pilihRadio(RbSumberRM,"Status Pasien di Rekam Medis",RbSumberPasien,"Pasien/Keluarga Pasien",RbSumberLain,"Lain-lain"));
            psUpdate.setString(10,nilaiKeteranganSumber());
            psUpdate.setString(11,noRekLama);
            if(psUpdate.executeUpdate()!=1){ throw new SQLException("Header rekonsiliasi tidak ditemukan"); }

            psHapusDetail=koneksi.prepareStatement("delete from rekonsiliasi_obat_detail_obat where no_rekonsiliasi=?");
            psHapusDetail.setString(1,noRekLama);
            psHapusDetail.executeUpdate();

            psTambahDetail=koneksi.prepareStatement(
                "insert into rekonsiliasi_obat_detail_obat "+
                "(no_rekonsiliasi,nama_obat,dosis_obat,frekuensi,cara_pemberian,waktu_pemberian_terakhir,tindak_lanjut,perubahan_aturan_pakai) "+
                "values (?,?,?,?,?,?,?,?)");
            for(int baris=0;baris<tabMode.getRowCount();baris++){
                psTambahDetail.setString(1,noRekLama);
                psTambahDetail.setString(2,nilaiTabelObat(baris,1));
                psTambahDetail.setString(3,nilaiTabelObat(baris,2));
                psTambahDetail.setString(4,nilaiTabelObat(baris,3));
                psTambahDetail.setString(5,nilaiTabelObat(baris,4));
                psTambahDetail.setString(6,nilaiTabelObat(baris,7));
                psTambahDetail.setString(7,nilaiTabelObat(baris,5));
                psTambahDetail.setString(8,nilaiTabelObat(baris,6));
                psTambahDetail.addBatch();
            }
            psTambahDetail.executeBatch();
            if(autoCommitLama){ koneksi.commit(); }
            sukses=true;
        }catch(Exception e){
            try{
                if(autoCommitLama){ koneksi.rollback(); }
                else if(savepoint!=null){ koneksi.rollback(savepoint); }
            }catch(Exception rollbackError){ System.out.println("Notif rollback Ganti Rekonsiliasi : "+rollbackError); }
            System.out.println("Notif Ganti Rekonsiliasi : "+e);
            JOptionPane.showMessageDialog(rootPane,"Gagal mengganti rekonsiliasi. Tidak ada perubahan yang disimpan.");
        }finally{
            try{ if(psTambahDetail!=null){psTambahDetail.close();} }catch(Exception e){}
            try{ if(psHapusDetail!=null){psHapusDetail.close();} }catch(Exception e){}
            try{ if(psUpdate!=null){psUpdate.close();} }catch(Exception e){}
            if(autoCommitLama){
                try{ koneksi.setAutoCommit(true); }catch(Exception e){ System.out.println("Notif autoCommit Ganti Rekonsiliasi : "+e); }
            }
        }
        if(sukses){
            String noRawatAktif=TNoRw.getText();
            JOptionPane.showMessageDialog(rootPane,"Data rekonsiliasi berhasil diganti.");
            Valid.tabelKosong(tabMode);
            aturModeEdit(false);
            emptTeks();
            TCariData.setText(noRawatAktif);
            TCariData.setForeground(ThemeConfig.TEXT_NORMAL);
            TabPilihan.setSelectedIndex(1);
        }
    }

    private String nilaiTabelObat(int baris,int kolom){
        Object nilai=tabMode.getValueAt(baris,kolom);
        return nilai==null?null:nilai.toString();
    }

    private void hapusRekonsiliasiTerpilih(){
        if(!akses.getrekonsiliasi_obat()){
            JOptionPane.showMessageDialog(rootPane,"Anda tidak memiliki hak akses untuk menghapus rekonsiliasi obat.");
            return;
        }
        if(tbDataRekonsiliasi==null || tbDataRekonsiliasi.getSelectedRow()<0){
            JOptionPane.showMessageDialog(rootPane,"Pilih data rekonsiliasi yang akan dihapus.");
            return;
        }
        String noRek=tbDataRekonsiliasi.getValueAt(tbDataRekonsiliasi.getSelectedRow(),0).toString();
        int jawab=JOptionPane.showConfirmDialog(rootPane,
            "Hapus permanen "+noRek+" beserta detail obat dan data konfirmasinya?\nTindakan ini tidak dapat dibatalkan.",
            "Konfirmasi Hapus Rekonsiliasi",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(jawab!=JOptionPane.YES_OPTION){ return; }

        boolean autoCommitLama=true;
        Savepoint savepoint=null;
        PreparedStatement psKonfirmasi=null, psDetail=null, psHeader=null;
        boolean sukses=false;
        try{
            autoCommitLama=koneksi.getAutoCommit();
            if(autoCommitLama){ koneksi.setAutoCommit(false); }
            else{ savepoint=koneksi.setSavepoint("hapus_rekonsiliasi_obat"); }

            psKonfirmasi=koneksi.prepareStatement("delete from rekonsiliasi_obat_konfirmasi where no_rekonsiliasi=?");
            psKonfirmasi.setString(1,noRek);
            psKonfirmasi.executeUpdate();
            psDetail=koneksi.prepareStatement("delete from rekonsiliasi_obat_detail_obat where no_rekonsiliasi=?");
            psDetail.setString(1,noRek);
            psDetail.executeUpdate();
            psHeader=koneksi.prepareStatement("delete from rekonsiliasi_obat where no_rekonsiliasi=?");
            psHeader.setString(1,noRek);
            if(psHeader.executeUpdate()!=1){ throw new SQLException("Header rekonsiliasi tidak ditemukan"); }

            if(autoCommitLama){ koneksi.commit(); }
            sukses=true;
        }catch(Exception e){
            try{
                if(autoCommitLama){ koneksi.rollback(); }
                else if(savepoint!=null){ koneksi.rollback(savepoint); }
            }catch(Exception rollbackError){ System.out.println("Notif rollback Hapus Rekonsiliasi : "+rollbackError); }
            System.out.println("Notif Hapus Rekonsiliasi : "+e);
            JOptionPane.showMessageDialog(rootPane,"Gagal menghapus rekonsiliasi. Tidak ada data yang dihapus.");
        }finally{
            try{ if(psHeader!=null){psHeader.close();} }catch(Exception e){}
            try{ if(psDetail!=null){psDetail.close();} }catch(Exception e){}
            try{ if(psKonfirmasi!=null){psKonfirmasi.close();} }catch(Exception e){}
            if(autoCommitLama){
                try{ koneksi.setAutoCommit(true); }catch(Exception e){ System.out.println("Notif autoCommit Hapus Rekonsiliasi : "+e); }
            }
        }
        if(sukses){
            JOptionPane.showMessageDialog(rootPane,"Data rekonsiliasi berhasil dihapus.");
            TNoRM1.setText("");
            TPasien1.setText("");
            LoadHTML.setText("");
            ChkAccor.setSelected(false);
            isMenu();
            tampilDataRekonsiliasi();
        }
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
        String modeCetak = (String) JOptionPane.showInputDialog(null, "Silahkan pilih mode cetak..!",
                "Cetak Form Rekonsiliasi Obat (RM 03-06A)", JOptionPane.QUESTION_MESSAGE, null,
                new Object[] { "Lembar", "PDF" }, "Lembar");
        if (modeCetak == null) {
            return;
        }
        cetakSatuTahapRekonsiliasi(noRekon, modeCetak);
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
        if(TabPilihan!=null && TabPilihan.getSelectedIndex()==1){
            perbaruiAksiDataTerpilih();
            return;
        }
        String saat = RekonsiliasiSaat.getSelectedItem() != null ? RekonsiliasiSaat.getSelectedItem().toString() : "Admisi";
        String sumber = pilihRadio(RbSumberRM, "Status Pasien di Rekam Medis", RbSumberPasien, "Pasien/Keluarga Pasien", RbSumberLain, "Lain-lain");
        String awalan = noRekonsiliasiEdit==null ? "" : "Mode Ganti "+noRekonsiliasiEdit+" · ";
        if (sumber != null && !sumber.isEmpty()) {
            lblStatusRekon.setText(awalan+"Rekonsiliasi saat " + saat + " · Sumber: " + sumber);
        } else {
            lblStatusRekon.setText(awalan+"Rekonsiliasi saat " + saat);
        }
    }

    public void updateFooterMeta() {
        if (lblFooterMeta == null) return;
        if(TabPilihan!=null && TabPilihan.getSelectedIndex()==1){
            perbaruiAksiDataTerpilih();
            return;
        }
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
        if(pnlTopSection != null){
            // Tinggi kartu identitas tidak boleh ikut menyusut saat grup Admisi disembunyikan.
            pnlTopSection.setPreferredSize(new Dimension(1320, 165));
            pnlTopSection.revalidate();
        }
        if(!admisi){
            bgObatSebelumAdmisi.clearSelection();
            bgSumberInformasi.clearSelection();
            TSumberLainKet.setText("");
        }
        perbaruiKeteranganSumber();
        updateStatusRekon();
    }

    private void perbaruiKeteranganSumber(){
        if(TSumberLainKet == null){ return; }
        boolean aktif = RbSumberLain != null && RbSumberLain.isSelected() &&
                         "Admisi".equals(RekonsiliasiSaat.getSelectedItem());
        TSumberLainKet.setEnabled(aktif);
        TSumberLainKet.setEditable(aktif);
        if(!aktif){
            TSumberLainKet.setText("");
        }else if(TSumberLainKet.getText().trim().isEmpty()){
            TSumberLainKet.setText("Sebutkan...");
            TSumberLainKet.setForeground(ThemeConfig.TEXT_FAINT);
        }
    }

    private String nilaiKeteranganSumber(){
        if(RbSumberLain == null || !RbSumberLain.isSelected()){ return null; }
        String nilai = TSumberLainKet.getText().trim();
        return nilai.equals("Sebutkan...") || nilai.isEmpty() ? null : nilai;
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
                        g2.setColor(new Color(13, 122, 112));
                        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                        g2.fillOval(cx - 3, cy - 3, 6, 6);
                    } else {
                        g2.setColor(new Color(207, 211, 206));
                        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                    }
                    g2.dispose();
                }
            };
            p.setOpaque(true);
            boolean sel = (row == table.getSelectedRow());
            p.setBackground(sel ? new Color(238, 246, 245) : Color.WHITE);
            return p;
        }
    }

    static class BoldTextCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            l.setForeground(new Color(31, 36, 33));
            boolean sel = (row == table.getSelectedRow());
            l.setBackground(sel ? new Color(238, 246, 245) : Color.WHITE);
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
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton b = (AbstractButton) c;
                if (b.getModel().isPressed()) {
                    g2.setColor(new Color(9, 82, 76));
                } else if (b.getModel().isRollover()) {
                    g2.setColor(new Color(11, 102, 94));
                } else {
                    g2.setColor(new Color(13, 122, 112));
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
        btn.setForeground(new Color(47, 54, 50));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton b = (AbstractButton) c;
                if (b.getModel().isPressed()) {
                    g2.setColor(new Color(235, 237, 233));
                } else if (b.getModel().isRollover()) {
                    g2.setColor(new Color(244, 245, 242));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 6, 6);
                g2.setColor(new Color(224, 226, 221));
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
            g2.setColor(selected ? new Color(13, 122, 112) : new Color(207, 211, 206));
            g2.setStroke(new BasicStroke(selected ? 1.5f : 1.2f));
            g2.drawOval(x + 1, y + 1, 12, 12);
            if (selected) {
                g2.setColor(new Color(13, 122, 112));
                g2.fillOval(x + 4, y + 4, 6, 6);
            }
            g2.dispose();
        }
    }

    private void styleRadioButton(widget.RadioButton rb, boolean isBold) {
        rb.setOpaque(false);
        rb.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 10));
        rb.setForeground(new Color(31, 36, 33));
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
            l.setForeground(new Color(31, 36, 33));
            l.setHorizontalAlignment(isCentered ? SwingConstants.CENTER : SwingConstants.LEFT);
            l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            String text = (value == null) ? "" : value.toString().trim();
            if (isDash && (text.isEmpty() || text.equals("-"))) {
                text = "—";
                l.setForeground(new Color(139, 146, 141));
            }
            l.setText(text);
            boolean sel = (row == table.getSelectedRow());
            l.setBackground(sel ? new Color(238, 246, 245) : Color.WHITE);
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

        // ----- Panel filter data (PAGE_END) -----
        widget.PanelBiasa panelCariData = new widget.PanelBiasa();
        panelCariData.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,8));
        panelCariData.setBackground(ThemeConfig.SURFACE);
        panelCariData.setPreferredSize(new Dimension(44, 44));
        panelCariData.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeConfig.BORDER_SOFT),
            BorderFactory.createEmptyBorder(0, 16, 0, 16)
        ));

        widget.Label lblTglData = new widget.Label();
        lblTglData.setText("Tgl. Wawancara :");
        lblTglData.setHorizontalAlignment(SwingConstants.LEFT);
        lblTglData.setPreferredSize(new Dimension(92,26));
        panelCariData.add(lblTglData);

        Tgl1Data = new widget.Tanggal();
        Tgl1Data.setDisplayFormat("dd-MM-yyyy");
        Tgl1Data.setDate(new Date());
        Tgl1Data.setPreferredSize(new Dimension(125,26));
        panelCariData.add(Tgl1Data);

        widget.Label lblSdData = new widget.Label();
        lblSdData.setText("s.d.");
        lblSdData.setHorizontalAlignment(SwingConstants.CENTER);
        lblSdData.setPreferredSize(new Dimension(24,26));
        panelCariData.add(lblSdData);

        Tgl2Data = new widget.Tanggal();
        Tgl2Data.setDisplayFormat("dd-MM-yyyy");
        Tgl2Data.setDate(new Date());
        Tgl2Data.setPreferredSize(new Dimension(125,26));
        panelCariData.add(Tgl2Data);

        widget.Label lblCariData = new widget.Label();
        lblCariData.setText("Kata Kunci :");
        lblCariData.setHorizontalAlignment(SwingConstants.LEFT);
        lblCariData.setPreferredSize(new Dimension(75,26));
        panelCariData.add(lblCariData);

        TCariData = new widget.TextBox();
        TCariData.setPreferredSize(new java.awt.Dimension(205,26));
        pasangPlaceholderPencarian(TCariData,"Ketik kata kunci...");
        TCariData.addActionListener(e -> tampilDataRekonsiliasi());
        panelCariData.add(TCariData);

        BtnCariData = new widget.Button();
        BtnCariData.setText("");
        BtnCariData.setName("BtnCariData");
        BtnCariData.setIcon(widget.IconStyle.muat("search.svg", 14, ThemeConfig.TEXT_MUTED));
        BtnCariData.setToolTipText("Cari data rekonsiliasi obat");
        styleDataFooterIconButton(BtnCariData);
        BtnCariData.addActionListener((java.awt.event.ActionEvent evt) -> tampilDataRekonsiliasi());
        panelCariData.add(BtnCariData);

        widget.Label lblRecordData = new widget.Label();
        lblRecordData.setText("Record :");
        lblRecordData.setPreferredSize(new Dimension(55, 26));
        panelCariData.add(lblRecordData);

        LCountData = new widget.Label();
        LCountData.setText("0");
        LCountData.setHorizontalAlignment(SwingConstants.LEFT);
        LCountData.setPreferredSize(new Dimension(50, 26));
        panelCariData.add(LCountData);

        styleDataFooterField(TCariData);
        styleDataFooterField(Tgl1Data);
        styleDataFooterField(Tgl2Data);

        // Panel cari ditaruh di PAGE_END (footer) - pola sama panelGlass9 di
        // "Data Triase" RMTriaseIGD.java, bukan di header/PAGE_START.
        internalFrameData.add(panelCariData, java.awt.BorderLayout.PAGE_END);

        // ----- Tabel bersih: 1 baris = 1 record (CENTER) -----
        tabModeData = new DefaultTableModel(null, new Object[]{
            "No. Rekonsiliasi","Pasien","Tanggal Wawancara","Rekonsiliasi Saat","Apoteker"
        }){
            @Override public boolean isCellEditable(int rowIndex,int colIndex){ return false; }
        };
        tbDataRekonsiliasi = new widget.Table();
        tbDataRekonsiliasi.setModel(tabModeData);
        tbDataRekonsiliasi.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbDataRekonsiliasi.setDefaultRenderer(Object.class, new WarnaTable());
        tbDataRekonsiliasi.setRowHeight(26);
        tbDataRekonsiliasi.setFillsViewportHeight(true);
        tbDataRekonsiliasi.setShowVerticalLines(false);
        tbDataRekonsiliasi.setShowHorizontalLines(true);
        tbDataRekonsiliasi.setIntercellSpacing(new Dimension(0, 1));
        tbDataRekonsiliasi.getTableHeader().setBackground(ThemeConfig.TABLE_HEADER_BG);
        tbDataRekonsiliasi.getTableHeader().setForeground(ThemeConfig.TABLE_HEADER_TEXT);
        tbDataRekonsiliasi.getTableHeader().setFont(ThemeConfig.FONT_BOLD);
        tbDataRekonsiliasi.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tbDataRekonsiliasi.getTableHeader().setReorderingAllowed(false);
        tbDataRekonsiliasi.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseClicked(java.awt.event.MouseEvent evt){
                tbDataRekonsiliasiMouseClicked(evt);
            }
        });
        tbDataRekonsiliasi.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()){ perbaruiAksiDataTerpilih(); }
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
        FormMenu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(1,0,1,0,ThemeConfig.BORDER_SOFT),
                javax.swing.BorderFactory.createEmptyBorder(0,8,0,4)));
        FormMenu.setPreferredSize(new java.awt.Dimension(115,43));
        FormMenu.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,4,9));

        widget.Label lblPasienAccor = new widget.Label();
        lblPasienAccor.setText("Pasien :");
        lblPasienAccor.setPreferredSize(new java.awt.Dimension(55,23));
        FormMenu.add(lblPasienAccor);

        TNoRM1 = new widget.TextBox();
        TNoRM1.setEditable(false);
        TNoRM1.setHighlighter(null);
        TNoRM1.setName("TNoRM1");
        TNoRM1.setPreferredSize(new java.awt.Dimension(100,23));
        buatKotakFieldRekonsiliasi(TNoRM1);
        TNoRM1.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT, 1),
            javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        FormMenu.add(TNoRM1);

        TPasien1 = new widget.TextBox();
        TPasien1.setEditable(false);
        TPasien1.setBackground(new java.awt.Color(245, 250, 240));
        TPasien1.setHighlighter(null);
        TPasien1.setName("TPasien1");
        TPasien1.setPreferredSize(new java.awt.Dimension(250,23));
        buatKotakFieldRekonsiliasi(TPasien1);
        TPasien1.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT, 1),
            javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
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
        LoadHTML.setBackground(Color.WHITE);
        javax.swing.text.html.HTMLEditorKit kitData = new javax.swing.text.html.HTMLEditorKit();
        LoadHTML.setEditorKit(kitData);
        javax.swing.text.html.StyleSheet styleSheetData = kitData.getStyleSheet();
        styleSheetData.addRule(
            ".isi td{border-right:1px solid #e2e7dd;font:8.5px tahoma;height:12px;"+
            "border-bottom:1px solid #e2e7dd;background:#ffffff;color:#323232;}"+
            ".isi2 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#323232;}"+
            ".isi3 td{border-right:1px solid #e2e7dd;font:8.5px tahoma;height:12px;"+
            "border-top:1px solid #e2e7dd;background:#ffffff;color:#323232;}"+
            ".isi4 td{font:11px tahoma;height:12px;border-top:1px solid #e2e7dd;background:#ffffff;color:#323232;}"
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

    private void styleDataFooterField(javax.swing.JComponent field){
        field.putClientProperty("Component.arc", 0);
        field.putClientProperty("TextComponent.arc", 0);
        field.putClientProperty("JTextField.arc", 0);
        field.putClientProperty("JComponent.roundRect", false);
        field.setBackground(Color.WHITE);
        field.setOpaque(true);
        field.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER_SOFT));
    }

    private void styleDataFooterIconButton(final widget.Button button){
        button.setPreferredSize(new Dimension(28,26));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(Color.WHITE);
        button.setGlassColor(Color.WHITE);
        button.setRoundRect(false);
        button.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseEntered(java.awt.event.MouseEvent e){
                if(button.isEnabled()){
                    button.setBackground(ThemeConfig.ACTION_HOVER_BG);
                    button.setGlassColor(ThemeConfig.ACTION_HOVER_BG);
                    button.setBorder(BorderFactory.createLineBorder(ThemeConfig.PRIMARY));
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e){
                button.setBackground(Color.WHITE);
                button.setGlassColor(Color.WHITE);
                button.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER));
            }
        });
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
            String kataKunci = nilaiPencarianData();
            psData=koneksi.prepareStatement(
                "select rekonsiliasi_obat.no_rekonsiliasi,rekonsiliasi_obat.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,"+
                "reg_periksa.umurdaftar,reg_periksa.sttsumur,rekonsiliasi_obat.tanggal_wawancara,rekonsiliasi_obat.rekonsiliasi_obat_saat,"+
                "rekonsiliasi_obat.nip,petugas.nama from rekonsiliasi_obat "+
                "inner join reg_periksa on reg_periksa.no_rawat=rekonsiliasi_obat.no_rawat "+
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join petugas on rekonsiliasi_obat.nip=petugas.nip "+
                "where rekonsiliasi_obat.tanggal_wawancara between ? and ? "+
                (kataKunci.equals("")?"":" and (rekonsiliasi_obat.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "+
                "pasien.nm_pasien like ? or petugas.nip like ? or petugas.nama like ?) ")+
                "order by rekonsiliasi_obat.tanggal_wawancara desc");
            psData.setString(1, Valid.SetTgl(Tgl1Data.getSelectedItem()+"")+" 00:00:00");
            psData.setString(2, Valid.SetTgl(Tgl2Data.getSelectedItem()+"")+" 23:59:59");
            if(!kataKunci.equals("")){
                String kw = "%"+kataKunci+"%";
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
            if(LCountData != null){
                LCountData.setText(Integer.toString(tabModeData.getRowCount()));
            }
            perbaruiAksiDataTerpilih();
        } catch (Exception e) {
            System.out.println("Notif tampilDataRekonsiliasi : "+e);
        } finally {
            try{ if(rsData!=null){ rsData.close(); } if(psData!=null){ psData.close(); } }catch(Exception e){}
        }
    }

    private void pasangPlaceholderPencarian(final widget.TextBox field, final String placeholder){
        field.setText(placeholder);
        field.setForeground(ThemeConfig.TEXT_FAINT);
        field.addFocusListener(new java.awt.event.FocusAdapter(){
            @Override public void focusGained(java.awt.event.FocusEvent e){
                if(placeholder.equals(field.getText())){
                    field.setText("");
                    field.setForeground(ThemeConfig.TEXT_NORMAL);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e){
                if(field.getText().trim().isEmpty()){
                    field.setText(placeholder);
                    field.setForeground(ThemeConfig.TEXT_FAINT);
                }
            }
        });
    }

    private String nilaiPencarianData(){
        String nilai = TCariData.getText().trim();
        return "Ketik kata kunci...".equals(nilai) ? "" : nilai;
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
            perbaruiAksiDataTerpilih();
        }
    }

    /** Kosongkan tampilan "-" utk nilai null/kosong di ringkasan HTML. */
    private String kosongkeStrip(String s){
        return (s==null || s.trim().equals(""))?"-":s;
    }

    /** Baris label:nilai mengikuti pola ringkasan HTML form IGD. */
    private String barisData(String label,String nilai){
        String tampil=(nilai==null || nilai.trim().isEmpty())?"-":nilai;
        return "<tr class='isi'><td valign='middle' width='35%'>"+escapeHtml(label)+"</td>"+
               "<td valign='middle' width='65%'>"+escapeHtml(tampil)+"</td></tr>";
    }

    private String judulBarisData(String judul){
        return "<tr class='isi'><td colspan='2' valign='middle' bgcolor='#FFFAF8' align='center' style='border:1px solid #e2e7dd;'><b>"+
               escapeHtml(judul)+"</b></td></tr>";
    }

    private String escapeHtml(String nilai){
        if(nilai==null){ return ""; }
        return nilai.replace("&","&amp;")
                    .replace("<","&lt;")
                    .replace(">","&gt;")
                    .replace("\"","&quot;")
                    .replace("'","&#39;");
    }

    private String formatTanggalWawancaraHtml(String tglStr){
        if(tglStr == null || tglStr.trim().isEmpty()){
            return "-";
        }
        String bersih = tglStr.trim();
        if(bersih.length() >= 16 && bersih.charAt(4) == '-' && bersih.charAt(7) == '-'){
            return bersih.substring(8, 10) + "-" + bersih.substring(5, 7) + "-" + bersih.substring(0, 4) + " " + bersih.substring(11, 16);
        }
        try {
            java.text.SimpleDateFormat sdfIn = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date d = sdfIn.parse(bersih);
            return new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(d);
        } catch (Exception e) {
            return bersih;
        }
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
        html.append("<html><table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>");

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
                    html.append(barisData("Tanggal Wawancara", formatTanggalWawancaraHtml(rsH.getString("tanggal_wawancara"))));
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

                    html.append("<tr class='isi'><td colspan='2' valign='top'><table width='100%' border='0' cellpadding='3px' cellspacing='0'>");
                    html.append("<tr class='isi3'>"+
                        "<td width='22%' align='left'><b>Nama Obat</b></td>"+
                        "<td width='13%' align='center'><b>Dosis Obat</b></td>"+
                        "<td width='13%' align='center'><b>Frekuensi</b></td>"+
                        "<td width='16%' align='center'><b>Cara<br>Pemberian</b></td>"+
                        "<td width='18%' align='center'><b>Tindak<br>Lanjut</b></td>"+
                        "<td width='18%' align='center'><b>Perubahan<br>Aturan Pakai</b></td></tr>");
                    java.sql.PreparedStatement psO=null; java.sql.ResultSet rsO=null;
                    try {
                        psO = koneksi.prepareStatement("select * from rekonsiliasi_obat_detail_obat where no_rekonsiliasi=?");
                        psO.setString(1, noRekonsiliasiTahap);
                        rsO = psO.executeQuery();
                        boolean adaObat=false;
                        while(rsO.next()){
                            adaObat=true;
                            html.append("<tr class='isi2'><td>").append(escapeHtml(kosongkeStrip(rsO.getString("nama_obat")))).append("</td>")
                                .append("<td>").append(escapeHtml(kosongkeStrip(rsO.getString("dosis_obat")))).append("</td>")
                                .append("<td>").append(escapeHtml(kosongkeStrip(rsO.getString("frekuensi")))).append("</td>")
                                .append("<td>").append(escapeHtml(kosongkeStrip(rsO.getString("cara_pemberian")))).append("</td>")
                                .append("<td>").append(escapeHtml(kosongkeStrip(rsO.getString("tindak_lanjut")))).append("</td>")
                                .append("<td>").append(escapeHtml(kosongkeStrip(rsO.getString("perubahan_aturan_pakai")))).append("</td></tr>");
                        }
                        if(!adaObat){
                            html.append("<tr class='isi2'><td colspan='6'><i>Belum ada data obat</i></td></tr>");
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

        html.append("</table></html>");
        LoadHTML.setText(html.toString());
        resetPosisiRingkasan();
    }

    private void resetPosisiRingkasan(){
        if(LoadHTML==null || ScrollHTML==null){ return; }
        LoadHTML.setCaretPosition(0);
        ScrollHTML.getViewport().setViewPosition(new java.awt.Point(0,0));
        ScrollHTML.getVerticalScrollBar().setValue(0);
        javax.swing.SwingUtilities.invokeLater(() -> {
            LoadHTML.setCaretPosition(0);
            ScrollHTML.getVerticalScrollBar().setValue(ScrollHTML.getVerticalScrollBar().getMinimum());
            ScrollHTML.getViewport().setViewPosition(new java.awt.Point(0,0));
        });
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

        String modeCetak = (String) JOptionPane.showInputDialog(null, "Silahkan pilih mode cetak..!",
                "Cetak Form Rekonsiliasi Obat (RM 03-06A)", JOptionPane.QUESTION_MESSAGE, null,
                new Object[] { "Lembar", "PDF" }, "Lembar");
        if (modeCetak == null) {
            return;
        }

        cetakSatuTahapRekonsiliasi(noRekonsiliasiKlik, modeCetak);
    }

    /**
     * [BARU] Cetak 1 tahap (1 halaman) form RM 03-06A
     * (rptRekonsiliasiObatForm.jasper) - replika layout formulir fisik RSUD
     * Demang Sepulau Raya. Identitas pasien/checkbox/apoteker dikirim
     * lewat parameter Jasper (1 nilai per print, di-query manual di sini);
     * dataset utama (daftar obat) di-query LANGSUNG oleh Valid.MyReportqry/
     * MyReportqrypdf.
     */
    private void cetakSatuTahapRekonsiliasi(String noRekonsiliasi, String modeCetak){
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
            param.put("pTanggalWawancara", formatTanggalWawancaraHtml(rsH.getString("tanggal_wawancara")));
            param.put("pSumberInformasi", safeGetString(rsH,"sumber_informasi"));
            param.put("pSumberInformasiLain", safeGetString(rsH,"sumber_informasi_lain"));
            param.put("pApotekerNama", namaApoteker);
            param.put("pApotekerNip", nipApoteker);
            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+
                "\nDitandatangani secara elektronik oleh "+namaApoteker+
                "\nID "+((fingerHash==null||fingerHash.isEmpty())?nipApoteker:fingerHash)+
                "\n"+formatTanggalWawancaraHtml(rsH.getString("tanggal_wawancara")));

            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            String qryCetak = "select nama_obat, dosis_obat, frekuensi, cara_pemberian, tindak_lanjut, perubahan_aturan_pakai "+
                              "from rekonsiliasi_obat_detail_obat where no_rekonsiliasi='"+noRekonsiliasi+"'";
            if ("PDF".equals(modeCetak)) {
                Valid.MyReportqrypdf("rptRekonsiliasiObatForm.jasper","report","::[ Form Rekonsiliasi Obat - RM 03-06A ]::",
                    qryCetak, param);
            } else {
                Valid.MyReportqry("rptRekonsiliasiObatForm.jasper","report","::[ Form Rekonsiliasi Obat - RM 03-06A ]::",
                    qryCetak, param);
            }
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
        perbaruiKeteranganSumber();

        autoNomor();
        updatePatientCard();
        updateCounterObat();
        updateStatusRekon();
        if(BtnPetugas.isEnabled()){
            BtnPetugas.requestFocusInWindow();
        }else if(btnSegAdmisi!=null){
            btnSegAdmisi.requestFocusInWindow();
        }
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
        if(TCariData!=null){
            TCariData.setText(norwt);
            TCariData.setForeground(ThemeConfig.TEXT_NORMAL);
        }
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
                    if(Tgl1Data!=null && rs.getDate("tgl_registrasi")!=null){
                        Tgl1Data.setDate(rs.getDate("tgl_registrasi"));
                    }
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
        boolean bolehRekonsiliasi=akses.getrekonsiliasi_obat();
        BtnSimpan.setEnabled(bolehRekonsiliasi);
        if(BtnBaruData!=null){ BtnBaruData.setEnabled(bolehRekonsiliasi); }
        if(BtnGantiData!=null){ BtnGantiData.setEnabled(false); }
        if(BtnHapusData!=null){ BtnHapusData.setEnabled(false); }
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
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
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

                g2.setFont(getFont());
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(text)) / 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, textX, textY);
                g2.dispose();
            }
        };
        b.setText(text);
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

        if(PerubahanAturanPakai != null){
            PerubahanAturanPakai.setEnabled(sel2);
            PerubahanAturanPakai.setEditable(sel2);
            if(!sel2){
                PerubahanAturanPakai.setText("Kosongkan bila tidak berubah");
                PerubahanAturanPakai.setForeground(ThemeConfig.TEXT_FAINT);
            }
        }

        btnTindakSama.repaint();
        btnTindakBerubah.repaint();
        btnTindakStop.repaint();
    }

    private void styleModalTextBox(widget.TextBox tb, final String placeholder) {
        tb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tb.setForeground(new Color(15, 23, 42));
        tb.setBackground(Color.WHITE);
        tb.putClientProperty("Component.arc", 0);
        tb.putClientProperty("TextComponent.arc", 0);
        tb.putClientProperty("JTextField.arc", 0);
        tb.putClientProperty("JComponent.roundRect", false);
        tb.putClientProperty("ModernStyle.active", Boolean.TRUE);
        tb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        // Samakan tinggi seluruh isian modal dengan field Nama Obat.
        tb.setPreferredSize(new Dimension(160, 30));
        tb.setMinimumSize(new Dimension(80, 30));
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
        DlgTambahObatRekonsiliasi.setSize(580, 365);
        DlgTambahObatRekonsiliasi.getContentPane().removeAll();

        JPanel pnlRoot = new JPanel(new BorderLayout(0, 0));
        pnlRoot.setBackground(Color.WHITE);
        pnlRoot.setBorder(BorderFactory.createLineBorder(new Color(224, 226, 221), 1));

        // 1. Header (North)
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(14, 18, 6, 14));

        JLabel lblTitle = new JLabel("Tambah Obat Direkonsiliasi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(31, 36, 33));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JButton btnClose = new JButton("\u00D7");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 17));
        btnClose.setForeground(new Color(139, 146, 141));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> DlgTambahObatRekonsiliasi.dispose());
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnClose.setForeground(new Color(31, 36, 33));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnClose.setForeground(new Color(139, 146, 141));
            }
        });
        pnlHeader.add(btnClose, BorderLayout.EAST);
        pnlRoot.add(pnlHeader, BorderLayout.NORTH);

        // 2. Form Content (Center)
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createEmptyBorder(2, 18, 2, 18));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        // Row 1: Nama Obat & Tombol Pencarian
        JLabel lNama = new JLabel("NAMA OBAT (KLIK CARI DARI FORMULARIUM)");
        lNama.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lNama.setForeground(new Color(139, 146, 141));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 3; c.weightx = 1;
        c.insets = new Insets(0, 0, 3, 0);
        pnlForm.add(lNama, c);

        NamaObat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        NamaObat.setForeground(new Color(139, 146, 141));
        NamaObat.setBackground(Color.WHITE);
        NamaObat.setEditable(false);
        NamaObat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        NamaObat.setToolTipText("Klik untuk mencari nama obat di database formularium");
        NamaObat.putClientProperty("Component.arc", 0);
        NamaObat.putClientProperty("TextComponent.arc", 0);
        NamaObat.putClientProperty("JTextField.arc", 0);
        NamaObat.putClientProperty("JComponent.roundRect", false);
        NamaObat.putClientProperty("ModernStyle.active", Boolean.TRUE);
        NamaObat.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 226, 221), 1),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        NamaObat.setPreferredSize(new Dimension(420, 30));
        NamaObat.setMinimumSize(new Dimension(180, 30));
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
        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.weightx = 1;
        c.insets = new Insets(0, 0, 10, 6);
        pnlForm.add(NamaObat, c);

        JButton btnCariObat = new JButton();
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon ikonCari =
                new com.formdev.flatlaf.extras.FlatSVGIcon("icon/search.svg", 14, 14);
            ikonCari.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(
                warna -> ThemeConfig.TEXT_MUTED));
            btnCariObat.setIcon(ikonCari);
        } catch (Throwable ex) {
            // Tetap bermakna bila SVG tidak tersedia; jangan kembali ke ikon klip lama.
            btnCariObat.setText("Cari");
        }
        btnCariObat.setPreferredSize(new Dimension(36, 30));
        btnCariObat.setMinimumSize(new Dimension(36, 30));
        btnCariObat.setBackground(Color.WHITE);
        btnCariObat.setBorder(BorderFactory.createLineBorder(new Color(224, 226, 221), 1));
        btnCariObat.setFocusPainted(false);
        btnCariObat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCariObat.setToolTipText("Cari Obat di Formularium (databarang)");
        btnCariObat.addActionListener(e -> bukaCariObat());
        c.gridx = 2; c.gridy = 1; c.gridwidth = 1; c.weightx = 0;
        c.insets = new Insets(0, 0, 10, 0);
        pnlForm.add(btnCariObat, c);

        // Row 2: Dosis Obat, Frekuensi, & Cara Pemberian (3 columns)
        JLabel lDosis = new JLabel("DOSIS OBAT");
        lDosis.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lDosis.setForeground(new Color(139, 146, 141));
        c.gridx = 0; c.gridy = 2; c.weightx = 1; c.insets = new Insets(0, 0, 3, 8);
        pnlForm.add(lDosis, c);

        styleModalTextBox(DosisObat, "mis. 500mg");
        c.gridy = 3; c.insets = new Insets(0, 0, 10, 8);
        pnlForm.add(DosisObat, c);

        JLabel lFrek = new JLabel("FREKUENSI");
        lFrek.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lFrek.setForeground(new Color(139, 146, 141));
        c.gridx = 1; c.gridy = 2; c.insets = new Insets(0, 0, 3, 8);
        pnlForm.add(lFrek, c);

        styleModalTextBox(Frekuensi, "mis. 3x1");
        c.gridy = 3; c.insets = new Insets(0, 0, 10, 8);
        pnlForm.add(Frekuensi, c);

        JLabel lCara = new JLabel("CARA PEMBERIAN");
        lCara.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lCara.setForeground(new Color(139, 146, 141));
        c.gridx = 2; c.gridy = 2; c.insets = new Insets(0, 0, 3, 0);
        pnlForm.add(lCara, c);

        AturanPakai.setVisible(true);
        styleModalTextBox(AturanPakai, "mis. Oral / IV");
        c.gridy = 3; c.insets = new Insets(0, 0, 10, 0);
        pnlForm.add(AturanPakai, c);

        // Row 3: Tindak Lanjut
        JLabel lTindak = new JLabel("TINDAK LANJUT");
        lTindak.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lTindak.setForeground(new Color(139, 146, 141));
        c.gridx = 0; c.gridy = 4; c.gridwidth = 3; c.insets = new Insets(0, 0, 3, 0);
        pnlForm.add(lTindak, c);

        JPanel segTindak = new JPanel(new GridLayout(1, 3, -1, 0));
        segTindak.setPreferredSize(new Dimension(540, 28));
        segTindak.setOpaque(false);

        btnTindakSama = createSegmentedTindakLanjutButton("Lanjut aturan pakai sama");
        btnTindakBerubah = createSegmentedTindakLanjutButton("Lanjut aturan pakai berubah");
        btnTindakStop = createSegmentedTindakLanjutButton("Stop");

        segTindak.add(btnTindakSama);
        segTindak.add(btnTindakBerubah);
        segTindak.add(btnTindakStop);
        c.gridy = 5; c.insets = new Insets(0, 0, 10, 0);
        pnlForm.add(segTindak, c);

        // Row 4: Perubahan Aturan Pakai
        JLabel lPerubahan = new JLabel("PERUBAHAN ATURAN PAKAI");
        lPerubahan.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lPerubahan.setForeground(new Color(139, 146, 141));
        c.gridy = 6; c.insets = new Insets(0, 0, 3, 0);
        pnlForm.add(lPerubahan, c);

        styleModalTextBox(PerubahanAturanPakai, "Kosongkan bila tidak berubah");
        c.gridy = 7; c.insets = new Insets(0, 0, 0, 0);
        pnlForm.add(PerubahanAturanPakai, c);

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
                    NamaObat.setForeground(new Color(31, 36, 33));
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
        NamaObat.setForeground(new Color(139, 146, 141));
        NamaObat.setEditable(false);

        DosisObat.setText("mis. 500mg");
        DosisObat.setForeground(new Color(139, 146, 141));

        Frekuensi.setText("mis. 3x1");
        Frekuensi.setForeground(new Color(139, 146, 141));

        AturanPakai.setText("mis. Oral / IV");
        AturanPakai.setForeground(new Color(139, 146, 141));

        PerubahanAturanPakai.setText("Kosongkan bila tidak berubah");
        PerubahanAturanPakai.setForeground(new Color(139, 146, 141));

        selectedTindakLanjut = "Lanjut aturan pakai sama";
        if (TindakLanjut != null) {
            try { TindakLanjut.setSelectedItem("Lanjut aturan pakai sama"); } catch (Exception e) {}
        }
        updateSegmentedTindakLanjut();
        NamaObat.requestFocus();
    }
}
