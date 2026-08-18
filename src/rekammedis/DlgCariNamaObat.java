package rekammedis;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Pencarian nama obat/infus dari data master `databarang` (Kode/Nama saja),
 * dipakai field "Nama Obat/Infus" di RMPenilaianAwalKeperawatanIGD - CATATAN
 * PENTING kolom `stok`/`gudangbarang`/`data_batch` SENGAJA TIDAK disentuh
 * sama sekali (tidak ada INSERT/UPDATE apapun di file ini) - dialog ini
 * murni baca nama utk dokumentasi keperawatan, BUKAN transaksi farmasi,
 * jadi tidak pernah mengurangi stok obat yg ada (permintaan eksplisit user).
 *
 * @author khanzasoft
 */
public final class DlgCariNamaObat extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;

    private widget.InternalFrame internalFrame1;
    private widget.ScrollPane Scroll;
    private widget.Table tbObat;
    private widget.panelisi panelisi3;
    private widget.Label label9;
    private widget.TextBox TCari;
    private widget.Button BtnCari;
    private widget.Button BtnAll;
    private widget.Label label10;
    private widget.Label LCount;
    private widget.Button BtnKeluar;

    public DlgCariNamaObat(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(10, 2);
        setSize(500, 250);

        Object[] kolom = {"Kode", "Nama Obat/Infus"};
        tabMode = new DefaultTableModel(null, kolom) {
            @Override public boolean isCellEditable(int rowIndex, int colIndex) { return false; }
        };
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(460, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn kolomKode = tbObat.getColumnModel().getColumn(0);
        kolomKode.setPreferredWidth(100);
        TableColumn kolomNama = tbObat.getColumnModel().getColumn(1);
        kolomNama.setPreferredWidth(340);
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        panelisi3 = new widget.panelisi();
        label9 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        label10 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Cari Nama Obat/Infus ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
        internalFrame1.setName("internalFrame1");
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll");
        Scroll.setOpaque(true);

        tbObat.setAutoCreateRowSorter(true);
        tbObat.setName("tbObat");
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelisi3.setName("panelisi3");
        panelisi3.setPreferredSize(new java.awt.Dimension(100, 43));
        panelisi3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label9.setText("Nama Obat :");
        label9.setName("label9");
        label9.setPreferredSize(new java.awt.Dimension(75, 23));
        panelisi3.add(label9);

        TCari.setName("TCari");
        TCari.setPreferredSize(new java.awt.Dimension(200, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelisi3.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setMnemonic('1');
        BtnCari.setToolTipText("Alt+1");
        BtnCari.setName("BtnCari");
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener((java.awt.event.ActionEvent evt) -> BtnCariActionPerformed(evt));
        panelisi3.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setMnemonic('2');
        BtnAll.setToolTipText("Alt+2");
        BtnAll.setName("BtnAll");
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener((java.awt.event.ActionEvent evt) -> BtnAllActionPerformed(evt));
        panelisi3.add(BtnAll);

        label10.setText("Record :");
        label10.setName("label10");
        label10.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi3.add(label10);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount");
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelisi3.add(LCount);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('3');
        BtnKeluar.setToolTipText("Alt+3");
        BtnKeluar.setName("BtnKeluar");
        BtnKeluar.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnKeluar.addActionListener((java.awt.event.ActionEvent evt) -> dispose());
        panelisi3.add(BtnKeluar);

        internalFrame1.add(panelisi3, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        pack();
    }

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            tbObat.requestFocus();
        }
    }

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {
        runBackground(() -> tampil(TCari.getText().trim()));
    }

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {
        TCari.setText("");
        runBackground(() -> tampil(""));
    }

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {
        if (tabMode.getRowCount() != 0) {
            if (evt.getClickCount() == 2) {
                dispose();
            }
        }
    }

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {
        if (tabMode.getRowCount() != 0) {
            if (evt.getKeyCode() == KeyEvent.VK_SPACE || evt.getKeyCode() == KeyEvent.VK_ENTER) {
                dispose();
            }
        }
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {
        runBackground(() -> tampil(""));
        TCari.requestFocus();
    }

    /**
     * Ambil nama obat dari `databarang` (kode_brng, nama_brng) yg aktif
     * (status='1') - HANYA SELECT, tidak ada join/query ke tabel stok
     * (`gudangbarang`/`data_batch`) sama sekali di method ini.
     */
    private void tampil(String kata) {
        Valid.tabelKosong(tabMode);
        try {
            ps = koneksi.prepareStatement(
                "select kode_brng, nama_brng from databarang where status='1' and nama_brng like ? order by nama_brng limit 300");
            ps.setString(1, "%" + kata + "%");
            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[]{ rs.getString("kode_brng"), rs.getString("nama_brng") });
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    public JTable getTable() {
        return tbObat;
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
