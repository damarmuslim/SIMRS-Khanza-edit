package rekammedis;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import widget.Button;
import widget.ComboBox;
import widget.InternalFrame;
import widget.Label;
import widget.PanelBiasa;
import widget.ScrollPane;
import widget.Table;
import widget.TextBox;

/**
 * Master Template Tanda Vital (TTV) Dokter.
 * Memungkinkan dokter IGD/Poliklinik membuat, mengedit, dan menerapkan template TTV
 * siap pakai untuk mempercepat pengisian rekam medis dan mengamankan klaim BPJS.
 */
public final class DlgMasterTemplateTTV extends JDialog {
    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private String kodeDokterAktif = "-";

    // Listener callback saat template dipilih
    public interface TemplateTtvListener {
        void onTemplateSelected(String nama, String tensi, String nadi, String respirasi, String suhu, String spo2, String gcs, String kesadaran);
    }
    private TemplateTtvListener listener;

    // Komponen GUI
    private InternalFrame internalFrame1;
    private PanelBiasa panelAtas;
    private PanelBiasa panelTengah;
    private PanelBiasa panelBawah;
    private Table tbTemplate;
    private ScrollPane scrollPane;

    // Field Input
    private TextBox TNoTemplate;
    private TextBox TNamaTemplate;
    private TextBox TTensi;
    private TextBox TNadi;
    private TextBox TRespirasi;
    private TextBox TSuhu;
    private TextBox TSpo2;
    private TextBox TGcs;
    private ComboBox CmbKesadaran;
    private TextBox TCari;
    private boolean isMemuat = false;

    // Tombol
    private Button BtnSimpan;
    private Button BtnBaru;
    private Button BtnHapus;
    private Button BtnGanti;
    private Button BtnPilih;
    private Button BtnKeluar;
    private Button BtnCari;

    public DlgMasterTemplateTTV(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Template Tanda Vital (TTV) Dokter");
        initTableDanSkema();
        initComponentsCustom();
        widget.IconStyle.pasang(this);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No", "Nama Template", "TD (mmHg)", "Nadi (x/m)", "RR (x/m)", "Suhu (°C)", "SpO2 (%)", "GCS", "Kesadaran", "Dokter", "no_template"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbTemplate.setModel(tabMode);
        tbTemplate.setPreferredScrollableViewportSize(new Dimension(750, 260));
        tbTemplate.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbTemplate.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] lebarKolom = {40, 200, 80, 75, 75, 75, 70, 60, 110, 110};
        for (int i = 0; i < lebarKolom.length; i++) {
            TableColumn column = tbTemplate.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebarKolom[i]);
        }
        // Sembunyikan kolom index 10 (no_template)
        tbTemplate.getColumnModel().getColumn(10).setMinWidth(0);
        tbTemplate.getColumnModel().getColumn(10).setMaxWidth(0);
        tbTemplate.getColumnModel().getColumn(10).setPreferredWidth(0);
        tbTemplate.getColumnModel().getColumn(10).setResizable(false);

        tbTemplate.setDefaultRenderer(Object.class, new WarnaTable());

        tbTemplate.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !isMemuat) {
                pilihBarisTabel();
            }
        });

        tampil();
    }

    private void initTableDanSkema() {
        try {
            PreparedStatement psCreate = koneksi.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `template_ttv_dokter` (" +
                "  `no_template` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `kd_dokter` VARCHAR(20) NOT NULL DEFAULT '-'," +
                "  `nama_template` VARCHAR(100) NOT NULL," +
                "  `tensi` VARCHAR(10) DEFAULT ''," +
                "  `nadi` VARCHAR(10) DEFAULT ''," +
                "  `respirasi` VARCHAR(10) DEFAULT ''," +
                "  `suhu` VARCHAR(10) DEFAULT ''," +
                "  `spo2` VARCHAR(10) DEFAULT ''," +
                "  `gcs` VARCHAR(10) DEFAULT ''," +
                "  `kesadaran` VARCHAR(30) DEFAULT ''," +
                "  INDEX(`kd_dokter`)" +
                ") ENGINE=InnoDB;"
            );
            psCreate.execute();
            psCreate.close();

            int count = Sequel.cariInteger("select count(*) from template_ttv_dokter");
            if (count == 0) {
                // Template awal bawaan (Kuning & Merah untuk klaim IGD, dan Normal Dewasa)
                simpanTemplateAwal("Demam / Febris (Kuning)", "120/80", "105", "22", "38.6", "98", "15", "Compos Mentis");
                simpanTemplateAwal("Sesak / Asma (Kuning)", "130/85", "102", "28", "36.8", "93", "15", "Compos Mentis");
                simpanTemplateAwal("Hipertensi Urgensi (Kuning)", "170/100", "92", "22", "36.7", "97", "15", "Compos Mentis");
                simpanTemplateAwal("Nyeri Kolik Akut (Kuning)", "135/85", "98", "22", "37.0", "98", "15", "Compos Mentis");
                simpanTemplateAwal("Normal Dewasa (Hijau)", "120/80", "80", "20", "36.5", "99", "15", "Compos Mentis");
                simpanTemplateAwal("Hipotensi / Syok (Merah)", "80/50", "120", "28", "36.0", "90", "13", "Somnolen");
            }
        } catch (Exception e) {
            System.out.println("Notif init skema template_ttv_dokter: " + e);
        }
    }

    private void simpanTemplateAwal(String nama, String tensi, String nadi, String resp, String suhu, String spo2, String gcs, String kesadaran) {
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "insert into template_ttv_dokter(kd_dokter,nama_template,tensi,nadi,respirasi,suhu,spo2,gcs,kesadaran) values('-' ,?,?,?,?,?,?,?,?)"
            );
            ps.setString(1, nama);
            ps.setString(2, tensi);
            ps.setString(3, nadi);
            ps.setString(4, resp);
            ps.setString(5, suhu);
            ps.setString(6, spo2);
            ps.setString(7, gcs);
            ps.setString(8, kesadaran);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            System.out.println("Notif seed template: " + e);
        }
    }

    private void initComponentsCustom() {
        setSize(860, 520);
        setLocationRelativeTo(null);

        internalFrame1 = new InternalFrame();
        internalFrame1.setLayout(new BorderLayout());

        // 1. Panel Atas: Input Form
        panelAtas = new PanelBiasa(new GridBagLayout());
        panelAtas.setPreferredSize(new Dimension(840, 125));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        TNoTemplate = new TextBox();
        TNoTemplate.setVisible(false);

        TNamaTemplate = new TextBox();
        TNamaTemplate.setDocument(new batasInput((byte) 100).getKata(TNamaTemplate));
        TNamaTemplate.setPreferredSize(new Dimension(220, 23));

        TTensi = new TextBox();
        TTensi.setDocument(new batasInput((byte) 8).getKata(TTensi));
        TTensi.setPreferredSize(new Dimension(70, 23));

        TNadi = new TextBox();
        TNadi.setDocument(new batasInput((byte) 5).getKata(TNadi));
        TNadi.setPreferredSize(new Dimension(50, 23));

        TRespirasi = new TextBox();
        TRespirasi.setDocument(new batasInput((byte) 5).getKata(TRespirasi));
        TRespirasi.setPreferredSize(new Dimension(50, 23));

        TSuhu = new TextBox();
        TSuhu.setDocument(new batasInput((byte) 5).getKata(TSuhu));
        TSuhu.setPreferredSize(new Dimension(50, 23));

        TSpo2 = new TextBox();
        TSpo2.setDocument(new batasInput((byte) 5).getKata(TSpo2));
        TSpo2.setPreferredSize(new Dimension(50, 23));

        TGcs = new TextBox();
        TGcs.setDocument(new batasInput((byte) 8).getKata(TGcs));
        TGcs.setPreferredSize(new Dimension(50, 23));

        CmbKesadaran = new ComboBox();
        CmbKesadaran.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            "Compos Mentis", "Apatis", "Somnolen", "Sopor", "Koma"
        }));
        CmbKesadaran.setPreferredSize(new Dimension(125, 23));

        // Baris 0: Nama Template & Kesadaran
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.gridwidth = 1;
        panelAtas.add(buatLabel("Nama Template :"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.gridwidth = 5;
        panelAtas.add(TNamaTemplate, gbc);

        gbc.gridx = 6; gbc.gridy = 0; gbc.weightx = 0.0; gbc.gridwidth = 1;
        panelAtas.add(buatLabel("Kesadaran :"), gbc);
        gbc.gridx = 7; gbc.gridy = 0; gbc.weightx = 0.5; gbc.gridwidth = 5;
        panelAtas.add(CmbKesadaran, gbc);

        // Baris 1: 6 Nilai TTV Berjajar Rapi
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panelAtas.add(buatLabel("TD :"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.2;
        panelAtas.add(TTensi, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0;
        panelAtas.add(buatLabel("Nadi :"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.2;
        panelAtas.add(TNadi, gbc);

        gbc.gridx = 4; gbc.gridy = 1; gbc.weightx = 0.0;
        panelAtas.add(buatLabel("RR :"), gbc);
        gbc.gridx = 5; gbc.gridy = 1; gbc.weightx = 0.2;
        panelAtas.add(TRespirasi, gbc);

        gbc.gridx = 6; gbc.gridy = 1; gbc.weightx = 0.0;
        panelAtas.add(buatLabel("Suhu :"), gbc);
        gbc.gridx = 7; gbc.gridy = 1; gbc.weightx = 0.2;
        panelAtas.add(TSuhu, gbc);

        gbc.gridx = 8; gbc.gridy = 1; gbc.weightx = 0.0;
        panelAtas.add(buatLabel("SpO2 :"), gbc);
        gbc.gridx = 9; gbc.gridy = 1; gbc.weightx = 0.2;
        panelAtas.add(TSpo2, gbc);

        gbc.gridx = 10; gbc.gridy = 1; gbc.weightx = 0.0;
        panelAtas.add(buatLabel("GCS :"), gbc);
        gbc.gridx = 11; gbc.gridy = 1; gbc.weightx = 0.2;
        panelAtas.add(TGcs, gbc);

        // Baris 2: Tombol CRUD di kanan
        PanelBiasa panelTombolCrud = new PanelBiasa(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelTombolCrud.setOpaque(false);
        BtnSimpan = new Button();
        BtnSimpan.setText("Simpan");
        BtnSimpan.setPreferredSize(new Dimension(95, 26));
        BtnSimpan.addActionListener(e -> simpan());

        BtnGanti = new Button();
        BtnGanti.setText("Ubah");
        BtnGanti.setPreferredSize(new Dimension(90, 26));
        BtnGanti.addActionListener(e -> ganti());

        BtnHapus = new Button();
        BtnHapus.setText("Hapus");
        BtnHapus.setPreferredSize(new Dimension(90, 26));
        BtnHapus.addActionListener(e -> hapus());

        BtnBaru = new Button();
        BtnBaru.setText("Baru");
        BtnBaru.setPreferredSize(new Dimension(90, 26));
        BtnBaru.addActionListener(e -> kosongkan());

        panelTombolCrud.add(BtnSimpan);
        panelTombolCrud.add(BtnGanti);
        panelTombolCrud.add(BtnHapus);
        panelTombolCrud.add(BtnBaru);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 1.0; gbc.gridwidth = 12;
        panelAtas.add(panelTombolCrud, gbc);

        internalFrame1.add(panelAtas, BorderLayout.NORTH);

        // 2. Panel Tengah: Tabel Template
        panelTengah = new PanelBiasa(new BorderLayout());
        tbTemplate = new Table();
        scrollPane = new ScrollPane();
        scrollPane.setViewportView(tbTemplate);
        panelTengah.add(scrollPane, BorderLayout.CENTER);

        tbTemplate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tbTemplate.getSelectedRow() != -1) {
                    pilihBarisTabel();
                    if (e.getClickCount() == 2) {
                        terapkanTemplateTerpilih();
                    }
                }
            }
        });

        tbTemplate.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    terapkanTemplateTerpilih();
                }
            }
        });

        internalFrame1.add(panelTengah, BorderLayout.CENTER);

        // 3. Panel Bawah: Pencarian & Tombol Pilih/Keluar
        panelBawah = new PanelBiasa(new BorderLayout(5, 5));
        panelBawah.setPreferredSize(new Dimension(840, 46));

        PanelBiasa panelCari = new PanelBiasa(new FlowLayout(FlowLayout.LEFT, 6, 8));
        panelCari.add(buatLabel("Cari Template :"));
        TCari = new TextBox();
        TCari.setPreferredSize(new Dimension(240, 26));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                }
            }
        });
        panelCari.add(TCari);

        BtnCari = new Button();
        BtnCari.setText("Cari");
        BtnCari.setPreferredSize(new Dimension(75, 26));
        BtnCari.addActionListener(e -> tampil());
        panelCari.add(BtnCari);

        PanelBiasa panelAksiBawah = new PanelBiasa(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        BtnPilih = new Button();
        BtnPilih.setText("Pilih / Terapkan");
        BtnPilih.setFont(BtnPilih.getFont().deriveFont(Font.BOLD));
        BtnPilih.setPreferredSize(new Dimension(145, 26));
        BtnPilih.addActionListener(e -> terapkanTemplateTerpilih());

        BtnKeluar = new Button();
        BtnKeluar.setText("Keluar");
        BtnKeluar.setPreferredSize(new Dimension(95, 26));
        BtnKeluar.addActionListener(e -> dispose());

        panelAksiBawah.add(BtnPilih);
        panelAksiBawah.add(BtnKeluar);

        panelBawah.add(panelCari, BorderLayout.WEST);
        panelBawah.add(panelAksiBawah, BorderLayout.EAST);

        internalFrame1.add(panelBawah, BorderLayout.SOUTH);

        getContentPane().add(internalFrame1);
        pack();
    }

    private Label buatLabel(String teks) {
        Label lbl = new Label();
        lbl.setText(teks);
        lbl.setFont(new Font(widget.ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        return lbl;
    }

    public void setDokter(String kdDokter) {
        this.kodeDokterAktif = (kdDokter == null || kdDokter.trim().isEmpty()) ? "-" : kdDokter.trim();
        tampil();
    }

    public void setListener(TemplateTtvListener listener) {
        this.listener = listener;
    }

    public void tampil() {
        int selectedRowSebelumnya = tbTemplate.getSelectedRow();
        String selectedNoSebelumnya = "";
        if (selectedRowSebelumnya != -1 && selectedRowSebelumnya < tabMode.getRowCount()) {
            Object val = tabMode.getValueAt(selectedRowSebelumnya, 10);
            if (val != null) selectedNoSebelumnya = val.toString();
        }

        isMemuat = true;
        Valid.tabelKosong(tabMode);
        try {
            String cari = TCari.getText().trim();
            String sql = "select no_template, nama_template, tensi, nadi, respirasi, suhu, spo2, gcs, kesadaran, kd_dokter " +
                         "from template_ttv_dokter where (kd_dokter=? or kd_dokter='-') " +
                         (cari.isEmpty() ? "" : "and (nama_template like ? or tensi like ?) ") +
                         "order by (kd_dokter=?) desc, nama_template asc";
            PreparedStatement ps = koneksi.prepareStatement(sql);
            ps.setString(1, kodeDokterAktif);
            if (!cari.isEmpty()) {
                ps.setString(2, "%" + cari + "%");
                ps.setString(3, "%" + cari + "%");
                ps.setString(4, kodeDokterAktif);
            } else {
                ps.setString(2, kodeDokterAktif);
            }
            ResultSet rs = ps.executeQuery();
            int noUrut = 1;
            int rowToSelect = -1;
            while (rs.next()) {
                String noTemp = rs.getString("no_template");
                String labelDokter = rs.getString("kd_dokter").equals("-") ? "Semua (Umum)" : "Pribadi";
                if (!selectedNoSebelumnya.isEmpty() && selectedNoSebelumnya.equals(noTemp)) {
                    rowToSelect = tabMode.getRowCount();
                }
                tabMode.addRow(new Object[]{
                    String.valueOf(noUrut++),
                    rs.getString("nama_template"),
                    rs.getString("tensi"),
                    rs.getString("nadi"),
                    rs.getString("respirasi"),
                    rs.getString("suhu"),
                    rs.getString("spo2"),
                    rs.getString("gcs"),
                    rs.getString("kesadaran"),
                    labelDokter,
                    noTemp
                });
            }
            rs.close();
            ps.close();

            if (tbTemplate.getRowCount() > 0) {
                int target = (rowToSelect != -1) ? rowToSelect : 0;
                tbTemplate.setRowSelectionInterval(target, target);
                tbTemplate.scrollRectToVisible(tbTemplate.getCellRect(target, 0, true));
            }
        } catch (Exception e) {
            System.out.println("Notif tampil template_ttv_dokter: " + e);
        } finally {
            isMemuat = false;
        }

        if (tbTemplate.getSelectedRow() != -1) {
            pilihBarisTabel();
        }
    }

    private void pilihBarisTabel() {
        if (isMemuat) return;
        int row = tbTemplate.getSelectedRow();
        if (row != -1 && row < tabMode.getRowCount()) {
            TNoTemplate.setText(tabMode.getValueAt(row, 10).toString());
            TNamaTemplate.setText(tabMode.getValueAt(row, 1).toString());
            TTensi.setText(tabMode.getValueAt(row, 2).toString());
            TNadi.setText(tabMode.getValueAt(row, 3).toString());
            TRespirasi.setText(tabMode.getValueAt(row, 4).toString());
            TSuhu.setText(tabMode.getValueAt(row, 5).toString());
            TSpo2.setText(tabMode.getValueAt(row, 6).toString());
            TGcs.setText(tabMode.getValueAt(row, 7).toString());
            CmbKesadaran.setSelectedItem(tabMode.getValueAt(row, 8).toString());
        }
    }

    private void terapkanTemplateTerpilih() {
        int row = tbTemplate.getSelectedRow();
        if (row == -1 && tbTemplate.getRowCount() > 0) {
            row = 0;
            tbTemplate.setRowSelectionInterval(0, 0);
            pilihBarisTabel();
        }

        String nama = TNamaTemplate.getText().trim();
        String tensi = TTensi.getText().trim();
        String nadi = TNadi.getText().trim();
        String respirasi = TRespirasi.getText().trim();
        String suhu = TSuhu.getText().trim();
        String spo2 = TSpo2.getText().trim();
        String gcs = TGcs.getText().trim();
        String kesadaran = CmbKesadaran.getSelectedItem() != null ? CmbKesadaran.getSelectedItem().toString() : "Compos Mentis";

        if (tensi.isEmpty() && nadi.isEmpty() && respirasi.isEmpty() && suhu.isEmpty()) {
            if (row != -1) {
                nama = tabMode.getValueAt(row, 1).toString();
                tensi = tabMode.getValueAt(row, 2).toString();
                nadi = tabMode.getValueAt(row, 3).toString();
                respirasi = tabMode.getValueAt(row, 4).toString();
                suhu = tabMode.getValueAt(row, 5).toString();
                spo2 = tabMode.getValueAt(row, 6).toString();
                gcs = tabMode.getValueAt(row, 7).toString();
                kesadaran = tabMode.getValueAt(row, 8).toString();
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih salah satu template pada tabel atau isi parameter TTV terlebih dahulu!", "Pemberitahuan", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (listener != null) {
            listener.onTemplateSelected(nama, tensi, nadi, respirasi, suhu, spo2, gcs, kesadaran);
        }
        dispose();
    }

    private void simpan() {
        if (TNamaTemplate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama template harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            TNamaTemplate.requestFocus();
            return;
        }

        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "insert into template_ttv_dokter(kd_dokter,nama_template,tensi,nadi,respirasi,suhu,spo2,gcs,kesadaran) values(?,?,?,?,?,?,?,?,?)"
            );
            ps.setString(1, kodeDokterAktif);
            ps.setString(2, TNamaTemplate.getText().trim());
            ps.setString(3, TTensi.getText().trim());
            ps.setString(4, TNadi.getText().trim());
            ps.setString(5, TRespirasi.getText().trim());
            ps.setString(6, TSuhu.getText().trim());
            ps.setString(7, TSpo2.getText().trim());
            ps.setString(8, TGcs.getText().trim());
            ps.setString(9, CmbKesadaran.getSelectedItem().toString());
            ps.executeUpdate();
            ps.close();

            kosongkan();
            tampil();
            JOptionPane.showMessageDialog(this, "Template berhasil disimpan!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            System.out.println("Notif simpan template: " + e);
            JOptionPane.showMessageDialog(this, "Gagal menyimpan template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ganti() {
        if (TNoTemplate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih template yang ingin diubah terlebih dahulu pada tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (TNamaTemplate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama template tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            TNamaTemplate.requestFocus();
            return;
        }

        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "update template_ttv_dokter set nama_template=?, tensi=?, nadi=?, respirasi=?, suhu=?, spo2=?, gcs=?, kesadaran=? " +
                "where no_template=?"
            );
            ps.setString(1, TNamaTemplate.getText().trim());
            ps.setString(2, TTensi.getText().trim());
            ps.setString(3, TNadi.getText().trim());
            ps.setString(4, TRespirasi.getText().trim());
            ps.setString(5, TSuhu.getText().trim());
            ps.setString(6, TSpo2.getText().trim());
            ps.setString(7, TGcs.getText().trim());
            ps.setString(8, CmbKesadaran.getSelectedItem().toString());
            ps.setString(9, TNoTemplate.getText().trim());
            ps.executeUpdate();
            ps.close();

            kosongkan();
            tampil();
            JOptionPane.showMessageDialog(this, "Template berhasil diperbarui!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            System.out.println("Notif ubah template: " + e);
            JOptionPane.showMessageDialog(this, "Gagal mengubah template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        if (TNoTemplate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih template yang ingin dihapus pada tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus template '" + TNamaTemplate.getText() + "'?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement ps = koneksi.prepareStatement("delete from template_ttv_dokter where no_template=?");
                ps.setString(1, TNoTemplate.getText().trim());
                ps.executeUpdate();
                ps.close();

                kosongkan();
                tampil();
                JOptionPane.showMessageDialog(this, "Template berhasil dihapus!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                System.out.println("Notif hapus template: " + e);
                JOptionPane.showMessageDialog(this, "Gagal menghapus template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void kosongkan() {
        TNoTemplate.setText("");
        TNamaTemplate.setText("");
        TTensi.setText("");
        TNadi.setText("");
        TRespirasi.setText("");
        TSuhu.setText("");
        TSpo2.setText("");
        TGcs.setText("");
        CmbKesadaran.setSelectedIndex(0);
        tbTemplate.clearSelection();
        TNamaTemplate.requestFocus();
    }
}
