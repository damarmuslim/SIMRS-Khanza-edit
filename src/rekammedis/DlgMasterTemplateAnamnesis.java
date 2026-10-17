package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import kepegawaian.DlgCariDokter;
import widget.ThemeConfig;

/**
 * MASTER TEMPLATE ANAMNESIS DOKTER (MODERN ARGON DESIGN)
 * Digunakan untuk mengelola format narasi Anamnesis (Keluhan Utama, RPS, RPD, RPK, RPO, Alergi)
 * dengan dukungan kepemilikan pribadi dokter / umum RS dan tombol chip klinis interaktif.
 * 
 * @author SIMRS Khanza Team
 */
public final class DlgMasterTemplateAnamnesis extends JDialog {

    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private String kodeDokterAktif = "-";
    private String namaDokterAktif = "";
    private String kodeDokterLogin = "";
    private String namaDokterLogin = "";
    private boolean isUpdatingCombo = false;
    private boolean isMemuat = false;
    private DlgCariDokter dlgDokter;

    private void deteksiDokterLogin() {
        if (!kodeDokterLogin.isEmpty()) return;
        String loginKode = akses.getkode();
        if (loginKode != null && !loginKode.trim().isEmpty() && !loginKode.equals("-")) {
            try {
                PreparedStatement ps = koneksi.prepareStatement(
                    "select kd_dokter, nm_dokter from dokter where kd_dokter=? or nik=? limit 1"
                );
                ps.setString(1, loginKode.trim());
                ps.setString(2, loginKode.trim());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    this.kodeDokterLogin = rs.getString("kd_dokter");
                    this.namaDokterLogin = rs.getString("nm_dokter");
                }
                rs.close();
                ps.close();
            } catch (Exception ignored) {}

            if (this.kodeDokterLogin.isEmpty()) {
                if ("Admin Utama".equalsIgnoreCase(loginKode.trim())) {
                    this.kodeDokterLogin = "Admin Utama";
                    this.namaDokterLogin = "Admin Utama";
                } else {
                    try {
                        String nmPtg = Sequel.cariIsi("select nama from pegawai where nik=?", loginKode.trim());
                        this.kodeDokterLogin = loginKode.trim();
                        this.namaDokterLogin = (nmPtg != null && !nmPtg.isEmpty()) ? nmPtg : loginKode.trim();
                    } catch (Exception ignored) {
                        this.kodeDokterLogin = loginKode.trim();
                        this.namaDokterLogin = loginKode.trim();
                    }
                }
            }
        }
    }

    // Callback saat template dipilih
    public interface TemplateAnamnesisListener {
        void onTemplateDipilih(String namaTemplate, String isiAnamnesis);
    }
    private TemplateAnamnesisListener listener;

    // === KOMPONEN INPUT ===
    public final JTextField txtNoTemplate = new JTextField();
    public final JTextField txtNamaTemplate = new JTextField();
    public final JComboBox<String> cmbDokter = new JComboBox<>(new String[]{
        "Semua (Umum RS)", "Pribadi Dokter"
    });
    public final JButton btnCariDokter = new JButton();
    public final JComboBox<String> cmbPreset = new JComboBox<>(new String[]{
        "Umum / Sacred Seven (S-O-A-P)", "Trauma & Kecelakaan Lalu Lintas",
        "Demam / Febris (Pediatrik & Dewasa)", "Nyeri Dada / Kardiovaskular (ACS)",
        "Sesak Napas / Respirasi (Asma/PPOK)", "Nyeri Perut / Abdomen Akut"
    });
    public final JTextArea txtAnamnesis = new JTextArea(5, 20);
    public final JTextField txtCari = new JTextField(24);

    // === TOMBOL AKSI MODERN ===
    public final JButton btnSimpan = new JButton("Simpan");
    public final JButton btnBaru = new JButton("Baru");
    public final JButton btnUbah = new JButton("Ganti");
    public final JButton btnHapus = new JButton("Hapus");
    public final JButton btnPilih = new JButton("Pilih / Terapkan");
    public final JButton btnCari = new JButton("Cari");
    public final JButton btnKeluar = new JButton("Keluar");

    public final JTable tbTemplate;
    private DefaultTableModel tabMode;

    // === CHIP PANEL & PREFERENSI ===
    private final Preferences prefsChip = Preferences.userNodeForPackage(DlgMasterTemplateAnamnesis.class);
    private final JPanel chipContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 5, 4));
    private final List<String[]> listCustomChips = new ArrayList<>();

    // === PALET WARNA ARGON MODERN ===
    private static final Color WARNA_HEADER     = new Color(15, 61, 62);    // #0F3D3E Deep Teal
    private static final Color WARNA_TEAL_MUDA   = new Color(128, 203, 196); // #80CBC4 Mint
    private static final Color BORDER_INPUT     = new Color(203, 213, 225); // #CBD5E1
    private static final Color BORDER_HALUS     = new Color(226, 232, 240); // #E2E8F0
    private static final Color BG_CARD          = new Color(248, 250, 252); // #F8FAFC
    private static final Color TEKS_GELAP       = new Color(30, 41, 59);    // #1E293B
    private static final Color TEKS_MUTED       = new Color(100, 116, 139); // #64748B
    private static final Color IKON_BIRU        = new Color(37, 99, 235);   // #2563EB
    private static final Color IKON_HIJAU       = new Color(22, 163, 74);   // #16A34A
    private static final Color IKON_ORANYE      = new Color(217, 119, 6);   // #D97706
    private static final Color IKON_MERAH       = new Color(220, 38, 38);   // #DC2626
    private static final Color IKON_SLATE       = new Color(71, 85, 105);   // #475569

    // =========================================================================
    // IKON VEKTOR MODERN ARGON (JAVA 2D RENDERED - CRISP, FLAT, & CLEAN)
    // =========================================================================
    public static class ArgonVectorIcon implements Icon {
        public enum Type { SIMPAN, BARU, UBAH, HAPUS, TERAPKAN, CARI, KELUAR, CHIP_ADD, CHIP_SAVE, STAR, DOKTER }
        private final Type type;
        private final int size;
        private final Color color;
        private final float stroke;

        public ArgonVectorIcon(Type type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
            this.stroke = Math.max(1.75f, size / 8.5f);
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            float s = size;
            switch (type) {
                case SIMPAN:
                    Path2D disk = new Path2D.Float();
                    disk.moveTo(s * 0.20f, s * 0.88f);
                    disk.lineTo(s * 0.80f, s * 0.88f);
                    disk.lineTo(s * 0.88f, s * 0.80f);
                    disk.lineTo(s * 0.88f, s * 0.32f);
                    disk.lineTo(s * 0.68f, s * 0.12f);
                    disk.lineTo(s * 0.20f, s * 0.12f);
                    disk.closePath();
                    g2.draw(disk);
                    g2.draw(new Rectangle2D.Float(s * 0.34f, s * 0.12f, s * 0.32f, s * 0.28f));
                    g2.draw(new Rectangle2D.Float(s * 0.30f, s * 0.54f, s * 0.40f, s * 0.34f));
                    break;

                case BARU:
                case CHIP_ADD:
                    g2.draw(new Line2D.Float(s * 0.50f, s * 0.18f, s * 0.50f, s * 0.82f));
                    g2.draw(new Line2D.Float(s * 0.18f, s * 0.50f, s * 0.82f, s * 0.50f));
                    break;

                case UBAH:
                    g2.draw(new Line2D.Float(s * 0.45f, s * 0.86f, s * 0.88f, s * 0.86f));
                    Path2D pen = new Path2D.Float();
                    pen.moveTo(s * 0.68f, s * 0.15f);
                    pen.lineTo(s * 0.85f, s * 0.32f);
                    pen.lineTo(s * 0.38f, s * 0.80f);
                    pen.lineTo(s * 0.18f, s * 0.85f);
                    pen.lineTo(s * 0.23f, s * 0.65f);
                    pen.closePath();
                    g2.draw(pen);
                    g2.draw(new Line2D.Float(s * 0.56f, s * 0.27f, s * 0.73f, s * 0.44f));
                    break;

                case HAPUS:
                    g2.draw(new Line2D.Float(s * 0.14f, s * 0.26f, s * 0.86f, s * 0.26f));
                    Path2D handle = new Path2D.Float();
                    handle.moveTo(s * 0.35f, s * 0.26f);
                    handle.lineTo(s * 0.35f, s * 0.14f);
                    handle.lineTo(s * 0.65f, s * 0.14f);
                    handle.lineTo(s * 0.65f, s * 0.26f);
                    g2.draw(handle);
                    Path2D can = new Path2D.Float();
                    can.moveTo(s * 0.22f, s * 0.26f);
                    can.lineTo(s * 0.26f, s * 0.86f);
                    can.lineTo(s * 0.74f, s * 0.86f);
                    can.lineTo(s * 0.78f, s * 0.26f);
                    g2.draw(can);
                    g2.draw(new Line2D.Float(s * 0.42f, s * 0.42f, s * 0.42f, s * 0.72f));
                    g2.draw(new Line2D.Float(s * 0.58f, s * 0.42f, s * 0.58f, s * 0.72f));
                    break;

                case TERAPKAN:
                    Path2D check = new Path2D.Float();
                    check.moveTo(s * 0.16f, s * 0.52f);
                    check.lineTo(s * 0.42f, s * 0.78f);
                    check.lineTo(s * 0.85f, s * 0.22f);
                    g2.draw(check);
                    break;

                case CARI:
                    g2.draw(new Ellipse2D.Float(s * 0.15f, s * 0.15f, s * 0.52f, s * 0.52f));
                    g2.draw(new Line2D.Float(s * 0.54f, s * 0.54f, s * 0.86f, s * 0.86f));
                    break;

                case KELUAR:
                    Path2D frame = new Path2D.Float();
                    frame.moveTo(s * 0.44f, s * 0.86f);
                    frame.lineTo(s * 0.20f, s * 0.86f);
                    frame.lineTo(s * 0.20f, s * 0.14f);
                    frame.lineTo(s * 0.44f, s * 0.14f);
                    g2.draw(frame);
                    g2.draw(new Line2D.Float(s * 0.38f, s * 0.50f, s * 0.86f, s * 0.50f));
                    Path2D arr = new Path2D.Float();
                    arr.moveTo(s * 0.66f, s * 0.30f);
                    arr.lineTo(s * 0.86f, s * 0.50f);
                    arr.lineTo(s * 0.66f, s * 0.70f);
                    g2.draw(arr);
                    break;

                case CHIP_SAVE:
                    Path2D bm = new Path2D.Float();
                    bm.moveTo(s * 0.25f, s * 0.15f);
                    bm.lineTo(s * 0.75f, s * 0.15f);
                    bm.lineTo(s * 0.75f, s * 0.85f);
                    bm.lineTo(s * 0.50f, s * 0.65f);
                    bm.lineTo(s * 0.25f, s * 0.85f);
                    bm.closePath();
                    g2.draw(bm);
                    break;

                case STAR:
                    Path2D star = new Path2D.Float();
                    double cx = s * 0.50, cy = s * 0.50;
                    double rOuter = s * 0.46, rInner = s * 0.20;
                    for (int i = 0; i < 10; i++) {
                        double r = (i % 2 == 0) ? rOuter : rInner;
                        double angle = i * Math.PI / 5.0 - Math.PI / 2.0;
                        float px = (float)(cx + r * Math.cos(angle));
                        float py = (float)(cy + r * Math.sin(angle));
                        if (i == 0) star.moveTo(px, py);
                        else star.lineTo(px, py);
                    }
                    star.closePath();
                    g2.fill(star);
                    break;

                case DOKTER:
                    g2.draw(new Ellipse2D.Float(s * 0.32f, s * 0.12f, s * 0.36f, s * 0.36f));
                    Path2D sh = new Path2D.Float();
                    sh.moveTo(s * 0.16f, s * 0.86f);
                    sh.curveTo(s * 0.16f, s * 0.60f, s * 0.84f, s * 0.60f, s * 0.84f, s * 0.86f);
                    g2.draw(sh);
                    g2.draw(new Line2D.Float(s * 0.50f, s * 0.66f, s * 0.50f, s * 0.82f));
                    g2.draw(new Line2D.Float(s * 0.42f, s * 0.74f, s * 0.58f, s * 0.74f));
                    break;
            }
            g2.dispose();
        }
    }

    // =========================================================================
    // WRAPLAYOUT: CHIP OTOMATIS WRAPPING TURUN KE BARIS BARU
    // =========================================================================
    public static class WrapLayout extends FlowLayout {
        public WrapLayout() { super(); }
        public WrapLayout(int align) { super(align); }
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension min = layoutSize(target, false);
            min.width -= (getHgap() + 1);
            return min;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int maxwidth = targetWidth - (insets.left + insets.right + hgap * 2);

                int nmembers = target.getComponentCount();
                int x = 0;
                int y = insets.top + vgap;
                int rowHeight = 0;

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if ((x == 0) || ((x + d.width) <= maxwidth)) {
                            if (x > 0) x += hgap;
                            x += d.width;
                            rowHeight = Math.max(rowHeight, d.height);
                        } else {
                            x = d.width;
                            y += vgap + rowHeight;
                            rowHeight = d.height;
                        }
                    }
                }
                return new Dimension(targetWidth, y + rowHeight + insets.bottom + vgap);
            }
        }
    }

    // =========================================================================
    // KONSTRUKTOR UTAMA
    // =========================================================================
    public DlgMasterTemplateAnamnesis(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Master Template Anamnesis Dokter");
        setSize(1000, 680);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        deteksiDokterLogin();
        if (!this.kodeDokterLogin.isEmpty()) {
            this.kodeDokterAktif = this.kodeDokterLogin;
            this.namaDokterAktif = this.namaDokterLogin;
        }

        initTableDanSkema();

        // 1. HEADER BAR ARGON (Tanpa Tombol Close di Atas Sesuai Preferensi User)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WARNA_HEADER);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLeft.setOpaque(false);
        JLabel lblTitle = new JLabel("Master Template Anamnesis Dokter");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 13));

        JLabel lblBadge = new JLabel("Keluhan Utama & Riwayat (RPS/RPD/RPO/Alergi)");
        lblBadge.setOpaque(true);
        lblBadge.setBackground(new Color(27, 94, 95));
        lblBadge.setForeground(WARNA_TEAL_MUDA);
        lblBadge.setFont(new Font("Tahoma", Font.BOLD, 10));
        lblBadge.setBorder(new EmptyBorder(2, 8, 2, 8));

        headerLeft.add(lblTitle);
        headerLeft.add(lblBadge);

        header.add(headerLeft, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // 2. BODY UTAMA
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel topWrapper = new JPanel(new BorderLayout(0, 10));
        topWrapper.setOpaque(false);
        topWrapper.add(buatCardInput(), BorderLayout.CENTER);
        topWrapper.add(buatActionBar(), BorderLayout.SOUTH);

        body.add(topWrapper, BorderLayout.NORTH);

        // Tabel Data (Kolom ke-4 hidden untuk menyimpan ID no_template database)
        String[] kolom = {"No", "Nama Template", "Anamnesis (Keluhan & Riwayat)", "Dokter", "no_template"};
        tabMode = new DefaultTableModel(null, kolom) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tbTemplate = new JTable(tabMode) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row > -1) {
                    Object val = getValueAt(row, 2);
                    if (val != null) {
                        return "<html><body style='width: 380px; font-size:11px;'>" +
                               val.toString().replace("  |  ", "<br>") + "</body></html>";
                    }
                }
                return super.getToolTipText(e);
            }
        };

        tbTemplate.setFont(new Font("Tahoma", Font.PLAIN, 11));
        tbTemplate.setRowHeight(26);
        tbTemplate.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbTemplate.setSelectionBackground(ThemeConfig.TABLE_SELECTION_BG);
        tbTemplate.setSelectionForeground(ThemeConfig.TABLE_SELECTION_TEXT);
        tbTemplate.setGridColor(BORDER_HALUS);
        tbTemplate.setShowVerticalLines(true);
        tbTemplate.setShowHorizontalLines(true);
        tbTemplate.setDefaultRenderer(Object.class, new WarnaTable());

        tbTemplate.getColumnModel().getColumn(0).setPreferredWidth(45);
        tbTemplate.getColumnModel().getColumn(1).setPreferredWidth(210);
        tbTemplate.getColumnModel().getColumn(2).setPreferredWidth(520);
        tbTemplate.getColumnModel().getColumn(3).setPreferredWidth(160);

        // Sembunyikan kolom index 4 (no_template)
        tbTemplate.getColumnModel().getColumn(4).setMinWidth(0);
        tbTemplate.getColumnModel().getColumn(4).setMaxWidth(0);
        tbTemplate.getColumnModel().getColumn(4).setPreferredWidth(0);
        tbTemplate.getColumnModel().getColumn(4).setResizable(false);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (isSelected) {
                    c.setBackground(ThemeConfig.TABLE_SELECTION_BG);
                    c.setForeground(ThemeConfig.TABLE_SELECTION_TEXT);
                } else {
                    c.setBackground((row % 2 == 1) ? ThemeConfig.ROW_ALT : ThemeConfig.SURFACE);
                    c.setForeground(ThemeConfig.TEXT_NORMAL);
                }
                return c;
            }
        };
        tbTemplate.getColumnModel().getColumn(0).setCellRenderer(centerRender);

        tbTemplate.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String valStr = value != null ? value.toString() : "";
                if (isSelected) {
                    l.setBackground(ThemeConfig.TABLE_SELECTION_BG);
                    l.setForeground(ThemeConfig.TABLE_SELECTION_TEXT);
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                } else {
                    l.setBackground((row % 2 == 1) ? ThemeConfig.ROW_ALT : ThemeConfig.SURFACE);
                    if (valStr.startsWith("Semua")) {
                        l.setForeground(TEKS_MUTED);
                        l.setFont(l.getFont().deriveFont(Font.PLAIN));
                    } else if (valStr.startsWith("Diri Sendiri")) {
                        l.setForeground(new Color(22, 101, 52)); // Hijau Tua
                        l.setFont(l.getFont().deriveFont(Font.BOLD));
                    } else {
                        l.setForeground(new Color(29, 78, 216)); // Biru
                        l.setFont(l.getFont().deriveFont(Font.BOLD));
                    }
                }
                return l;
            }
        });

        tbTemplate.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !isMemuat) {
                pilihBarisTabel();
            }
        });

        tbTemplate.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    terapkanTemplateTerpilih();
                }
            }
        });

        tbTemplate.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    terapkanTemplateTerpilih();
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(tbTemplate);
        scrollTable.setBorder(BorderFactory.createLineBorder(BORDER_INPUT));
        scrollTable.getViewport().setBackground(Color.WHITE);

        body.add(scrollTable, BorderLayout.CENTER);
        body.add(buatFooterBar(), BorderLayout.SOUTH);

        add(body, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override public void windowActivated(WindowEvent e) {
                txtNamaTemplate.requestFocusInWindow();
            }
        });

        muatCustomChips();
        renderChipPanel();
        updateComboDokter();
        tampil();
    }

    private JPanel buatCardInput() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_HALUS, 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));

        // Baris 1: Nama Template, Dokter/Kepemilikan, & Preset Cepat
        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 12);

        txtNoTemplate.setVisible(false);
        styleInput(txtNamaTemplate);
        txtNamaTemplate.setDocument(new batasInput((byte) 100).getKata(txtNamaTemplate));

        cmbDokter.setFont(new Font("Tahoma", Font.PLAIN, 11));
        cmbDokter.setBackground(Color.WHITE);
        cmbDokter.addActionListener(e -> {
            if (isUpdatingCombo) return;
            Object sel = cmbDokter.getSelectedItem();
            if (sel == null) return;
            String s = sel.toString();

            if (s.contains("Pilih Dokter")) {
                SwingUtilities.invokeLater(() -> bukaCariDokter());
            } else if (s.startsWith("Diri Sendiri")) {
                deteksiDokterLogin();
                this.kodeDokterAktif = !this.kodeDokterLogin.isEmpty() ? this.kodeDokterLogin : "-";
                this.namaDokterAktif = this.namaDokterLogin;
                tampil();
            } else if (s.startsWith("Semua")) {
                this.kodeDokterAktif = "-";
                tampil();
            }
        });

        btnCariDokter.setPreferredSize(new Dimension(30, 26));
        btnCariDokter.setBackground(Color.WHITE);
        btnCariDokter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCariDokter.setFocusPainted(false);
        btnCariDokter.setToolTipText("Pilih Dokter dari Master Dokter");
        btnCariDokter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_INPUT),
            new EmptyBorder(2, 4, 2, 4)
        ));
        btnCariDokter.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.DOKTER, 14, WARNA_HEADER));
        btnCariDokter.addActionListener(e -> bukaCariDokter());

        JPanel pnlDokter = new JPanel(new BorderLayout(4, 0));
        pnlDokter.setOpaque(false);
        pnlDokter.add(cmbDokter, BorderLayout.CENTER);
        pnlDokter.add(btnCariDokter, BorderLayout.EAST);

        cmbPreset.setFont(new Font("Tahoma", Font.PLAIN, 11));
        cmbPreset.setBackground(Color.WHITE);
        cmbPreset.addActionListener(e -> renderChipPanel());

        gbc.gridx = 0; gbc.weightx = 0.50;
        row1.add(bungkusLabel("Nama Template :", txtNamaTemplate), gbc);

        gbc.gridx = 1; gbc.weightx = 0.25;
        row1.add(bungkusLabel("Dokter / Kepemilikan :", pnlDokter), gbc);

        gbc.gridx = 2; gbc.weightx = 0.25; gbc.insets = new Insets(0, 0, 0, 0);
        row1.add(bungkusLabel("Preset Tombol Cepat (Chip) :", cmbPreset), gbc);

        // Baris 2: Header Area + Tombol Kelola Chip + Kotak Chip + TextArea
        JPanel row2 = new JPanel(new BorderLayout(0, 6));
        row2.setOpaque(false);

        JPanel headerArea = new JPanel(new BorderLayout());
        headerArea.setOpaque(false);
        JLabel lblAnamnesis = new JLabel("Isi Template Anamnesis (Keluhan Utama & Riwayat Penyakit) :");
        lblAnamnesis.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblAnamnesis.setForeground(TEKS_GELAP);

        JPanel chipTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        chipTools.setOpaque(false);

        JButton btnSimpanBlok = new JButton("Simpan Blok Teks Jadi Tag");
        btnSimpanBlok.setFont(new Font("Tahoma", Font.BOLD, 10));
        btnSimpanBlok.setBackground(Color.WHITE);
        btnSimpanBlok.setForeground(WARNA_HEADER);
        btnSimpanBlok.setFocusPainted(false);
        btnSimpanBlok.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpanBlok.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_INPUT),
            new EmptyBorder(3, 8, 3, 8)
        ));
        btnSimpanBlok.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.CHIP_SAVE, 12, WARNA_HEADER));
        btnSimpanBlok.setToolTipText("Blok kalimat di kotak teks lalu klik tombol ini untuk membuat tombol pintasan baru");
        btnSimpanBlok.addActionListener(e -> simpanBlokTeksKeChip());

        JButton btnTagBaru = new JButton("+ Tag Custom Baru");
        btnTagBaru.setFont(new Font("Tahoma", Font.BOLD, 10));
        btnTagBaru.setBackground(new Color(240, 253, 250));
        btnTagBaru.setForeground(WARNA_HEADER);
        btnTagBaru.setFocusPainted(false);
        btnTagBaru.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTagBaru.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(94, 234, 212)),
            new EmptyBorder(3, 9, 3, 9)
        ));
        btnTagBaru.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.CHIP_ADD, 11, WARNA_HEADER));
        btnTagBaru.setToolTipText("Tambah tombol pintasan anamnesis kustom sendiri");
        btnTagBaru.addActionListener(e -> dialogTambahAtauEditChip(-1));

        chipTools.add(btnSimpanBlok);
        chipTools.add(btnTagBaru);

        headerArea.add(lblAnamnesis, BorderLayout.WEST);
        headerArea.add(chipTools, BorderLayout.EAST);

        // Chip Panel Container
        JPanel chipBox = new JPanel(new BorderLayout());
        chipBox.setOpaque(false);
        chipContainer.setOpaque(false);
        chipBox.add(chipContainer, BorderLayout.CENTER);

        JLabel lblPetunjukChip = new JLabel("[i] Klik Kiri untuk menyisipkan ke teks. Klik Kanan pada tag untuk jadikan favorit / ubah / hapus.");
        lblPetunjukChip.setFont(new Font("Tahoma", Font.ITALIC, 10));
        lblPetunjukChip.setForeground(TEKS_MUTED);
        chipBox.add(lblPetunjukChip, BorderLayout.SOUTH);

        // Text Area Input
        txtAnamnesis.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtAnamnesis.setLineWrap(true);
        txtAnamnesis.setWrapStyleWord(true);
        txtAnamnesis.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane scrollTeks = new JScrollPane(txtAnamnesis);
        scrollTeks.setPreferredSize(new Dimension(0, 140));
        scrollTeks.setBorder(BorderFactory.createLineBorder(BORDER_INPUT));

        row2.add(headerArea, BorderLayout.NORTH);
        row2.add(chipBox, BorderLayout.CENTER);
        row2.add(scrollTeks, BorderLayout.SOUTH);

        card.add(row1, BorderLayout.NORTH);
        card.add(row2, BorderLayout.CENTER);
        return card;
    }

    private void muatCustomChips() {
        listCustomChips.clear();
        String saved = prefsChip.get("custom_chips_anamnesis_argon", "");
        if (!saved.isEmpty()) {
            for (String item : saved.split("\\|\\|")) {
                String[] kv = item.split("::", 3);
                if (kv.length == 3) {
                    listCustomChips.add(kv);
                } else if (kv.length == 2) {
                    listCustomChips.add(new String[]{kv[0], kv[1], "0"});
                }
            }
        }
    }

    private void simpanCustomChipsKePrefs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listCustomChips.size(); i++) {
            if (i > 0) sb.append("||");
            String[] c = listCustomChips.get(i);
            String isFav = (c.length >= 3 && "1".equals(c[2])) ? "1" : "0";
            sb.append(c[0]).append("::").append(c[1]).append("::").append(isFav);
        }
        prefsChip.put("custom_chips_anamnesis_argon", sb.toString());
    }

    private void renderChipPanel() {
        chipContainer.removeAll();
        String preset = (String) cmbPreset.getSelectedItem();
        if (preset == null) preset = "Umum / Sacred Seven (S-O-A-P)";

        List<String[]> presetChips = new ArrayList<>();
        if (preset.startsWith("Umum")) {
            presetChips.add(new String[]{"+ Sacred Seven Lengkap", "KELUHAN UTAMA : \nRIWAYAT PENYAKIT SEKARANG (RPS) :\n- Onset : \n- Lokasi & Penjalaran : \n- Kualitas : \n- Kuantitas / Skala Nyeri : \n- Faktor memperberat : \n- Faktor memperingan : \n- Gejala penyerta : \nRIWAYAT PENYAKIT DAHULU (RPD) : \nRIWAYAT PENGOBATAN (RPO) : \nRIWAYAT ALERGI : Tidak ada"});
            presetChips.add(new String[]{"+ Keluhan Utama", "KELUHAN UTAMA : "});
            presetChips.add(new String[]{"+ RPS Onset Waktu", "- Keluhan dirasakan sejak ... hari/jam yang lalu secara tiba-tiba/perlahan"});
            presetChips.add(new String[]{"+ Sifat Nyeri / Rasa", "- Kualitas keluhan : rasa tertindih / melilit / berdenyut / perih / tajam"});
            presetChips.add(new String[]{"+ Faktor Pemicu", "- Keluhan memberat saat beraktivitas / cuaca dingin / setelah makan"});
            presetChips.add(new String[]{"+ Gejala Penyerta (-)", "- Demam (-), mual (-), muntah (-), sesak napas (-), keringat dingin (-)"});
            presetChips.add(new String[]{"+ Riwayat RPD & RPO", "RIWAYAT PENYAKIT DAHULU : Riwayat Hipertensi (-), DM (-), Sakit Jantung (-)\nRIWAYAT PENGOBATAN : Belum mengonsumsi obat"});
            presetChips.add(new String[]{"+ Alergi Tidak Ada", "RIWAYAT ALERGI : Tidak ada riwayat alergi obat dan makanan"});
            presetChips.add(new String[]{"[Reset] Format Kosong", "KELUHAN UTAMA : \nRIWAYAT PENYAKIT SEKARANG : \nRIWAYAT PENYAKIT DAHULU : \nRIWAYAT PENGOBATAN : \nRIWAYAT ALERGI : "});
        } else if (preset.startsWith("Trauma")) {
            presetChips.add(new String[]{"+ Kronologi KLL", "MEKANISME TRAUMA : Pasien datang dibawa warga / keluarga setelah mengalami kecelakaan lalu lintas ... jam yang lalu. Pasien sebagai pengendara / penumpang, memakai helm (+/-). Kronologi : tabrakan / terjatuh sendiri."});
            presetChips.add(new String[]{"+ Hilang Kesadaran (-)", "- Riwayat pingsan / hilang kesadaran saat kejadian (-)"});
            presetChips.add(new String[]{"+ Muntah Menyemprot (-)", "- Mual (-), muntah menyemprot (-), perdarahan telinga/hidung (-)"});
            presetChips.add(new String[]{"+ Nyeri & Lokasi Cedera", "- Pasien mengeluhkan nyeri dan luka lecet/robek pada bagian : "});
            presetChips.add(new String[]{"+ Pertolongan Pertama", "- Pertolongan pertama di lokasi : belum ada tindakan / dibersihkan dengan air"});
            presetChips.add(new String[]{"+ Mekanisme Jatuh", "MEKANISME TRAUMA : Pasien terpeleset / terjatuh dari ketinggian ... meter posisi mendarat ..." });
        } else if (preset.startsWith("Demam")) {
            presetChips.add(new String[]{"+ Pola Panas Badan", "- Demam dirasakan sejak ... hari yang lalu, panas terus menerus / naik turun"});
            presetChips.add(new String[]{"+ Saluran Napas Atas", "- Batuk (+/-), dahak (+/-), pilek (+/-), nyeri tenggorokan (+/-)"});
            presetChips.add(new String[]{"+ Gastrointestinal", "- Mual (+/-), muntah (+/-), nyeri ulu hati (+/-), BAB cair (+/-)"});
            presetChips.add(new String[]{"+ Tanda Pendarahan (-)", "- Mimisan (-), gusi berdarah (-), bintik merah pada kulit (-)"});
            presetChips.add(new String[]{"+ Riwayat Kejang (-)", "- Riwayat kejang demam (-), penurunan kesadaran (-)"});
            presetChips.add(new String[]{"+ Respon Paracetamol", "- Sudah diberikan obat penurun panas (paracetamol), demam turun lalu naik kembali"});
        } else if (preset.startsWith("Nyeri Dada")) {
            presetChips.add(new String[]{"+ Nyeri Dada Khas Jantung", "KELUHAN UTAMA : Nyeri dada substernal\nRPS : Nyeri dada dirasakan sejak ... jam yang lalu, rasa seperti tertindih beban berat / diremas, menjalar ke lengan kiri dan rahang, durasi > 20 menit."});
            presetChips.add(new String[]{"+ Keringat Dingin Profus", "- Disertai keringat dingin membasahi pakaian (+), sesak napas (+), lemas (+)"});
            presetChips.add(new String[]{"+ Riwayat Kardiovaskular", "RIWAYAT PENYAKIT DAHULU : Riwayat PJK / Pasang Ring (+/-), Hipertensi (+/-), DM (+/-), Kolesterol (+/-)"});
            presetChips.add(new String[]{"+ Riwayat Merokok", "RIWAYAT SOSIAL : Perokok aktif ... batang per hari"});
        } else if (preset.startsWith("Sesak Napas")) {
            presetChips.add(new String[]{"+ Sesak Disertai Mengi", "- Sesak napas dirasakan sejak ... jam yang lalu, bunyi napas 'ngik-ngik' / mengi (+), batuk (+/-)"});
            presetChips.add(new String[]{"+ Faktor Pemicu Dingin/Debu", "- Sesak kambuh dipicu cuaca dingin / debu / kelelahan"});
            presetChips.add(new String[]{"+ Posisi Duduk Lebih Nyaman", "- Pasien lebih nyaman dengan posisi setengah duduk / orthopnea (+/-)"});
            presetChips.add(new String[]{"+ Riwayat Asma & Inhaler", "RPD : Riwayat Asma sejak kecil (+), rutin kontrol (-)\nRPO : Menggunakan obat semprot / inhaler"});
        } else if (preset.startsWith("Nyeri Perut")) {
            presetChips.add(new String[]{"+ Nyeri Ulu Hati / Epigastrium", "- Nyeri ulu hati / perih terasa melilit sejak ... jam yang lalu, memburuk setelah makan"});
            presetChips.add(new String[]{"+ Mual & Muntah", "- Disertai mual (+), muntah frekuensi ... kali isi cairan / makanan"});
            presetChips.add(new String[]{"+ Riwayat Maag / Gastritis", "RPD : Riwayat sakit maag / gastritis (+)\nRPO : Konsumsi antasida / jamu-jamuan / obat antinyeri"});
            presetChips.add(new String[]{"+ Pola BAB & Flatus", "- BAB normal / belum BAB ... hari, flatus / buang angin (+)"});
        }

        for (String[] chip : presetChips) {
            chipContainer.add(buatTombolChipPreset(chip[0], chip[1]));
        }

        for (int i = 0; i < listCustomChips.size(); i++) {
            final int index = i;
            String[] c = listCustomChips.get(i);
            boolean isFav = c.length >= 3 && "1".equals(c[2]);
            chipContainer.add(buatTombolChipCustom(c[0], c[1], isFav, index));
        }

        chipContainer.revalidate();
        chipContainer.repaint();
    }

    private JButton buatTombolChipPreset(String label, String isiTeks) {
        String display = label;
        if (display.length() > 32) {
            display = display.substring(0, 29) + "...";
        }
        JButton btn = new JButton(display);
        btn.setFont(new Font("Tahoma", Font.BOLD, 10));
        btn.setBackground(new Color(248, 250, 252));
        btn.setForeground(new Color(51, 65, 85));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_INPUT),
            new EmptyBorder(2, 7, 2, 7)
        ));
        btn.setToolTipText("<html><b>" + label + "</b><br><b>Klik Kiri:</b> Sisipkan ke teks<hr>" + isiTeks.replace("\n", "<br>") + "</html>");
        btn.addActionListener(e -> sisipkanTeksKeAnamnesis(isiTeks));
        return btn;
    }

    private JButton buatTombolChipCustom(String label, String isiTeks, boolean isFav, int index) {
        String display = label;
        if (display.length() > 30) {
            display = display.substring(0, 27) + "...";
        }
        JButton btn = new JButton(display);
        btn.setFont(new Font("Tahoma", Font.BOLD, 10));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isFav) {
            btn.setBackground(new Color(239, 246, 255));
            btn.setForeground(new Color(29, 78, 216));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253)),
                new EmptyBorder(2, 6, 2, 6)
            ));
            btn.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.STAR, 10, new Color(234, 179, 8)));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(71, 85, 105));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_INPUT),
                new EmptyBorder(2, 7, 2, 7)
            ));
        }

        btn.setToolTipText("<html><b>" + label + "</b> (Tag Custom Dokter)<br><b>Klik Kiri:</b> Sisipkan ke teks<br><b>Klik Kanan:</b> Ubah / Jadikan Favorit / Hapus<hr>" + isiTeks.replace("\n", "<br>") + "</html>");
        btn.addActionListener(e -> sisipkanTeksKeAnamnesis(isiTeks));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) tampilkanMenuChip(e.getComponent(), e.getX(), e.getY(), index, isFav);
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) tampilkanMenuChip(e.getComponent(), e.getX(), e.getY(), index, isFav);
            }
        });

        return btn;
    }

    private void tampilkanMenuChip(Component comp, int x, int y, int index, boolean isFav) {
        JPopupMenu pop = new JPopupMenu();

        JMenuItem mFav = new JMenuItem(isFav ? "Hapus dari Favorit" : "Jadikan Favorit (Bintang)");
        mFav.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.STAR, 11, isFav ? Color.GRAY : new Color(234, 179, 8)));
        mFav.addActionListener(e -> {
            String[] item = listCustomChips.get(index);
            item[2] = isFav ? "0" : "1";
            simpanCustomChipsKePrefs();
            renderChipPanel();
        });
        pop.add(mFav);

        JMenuItem mEdit = new JMenuItem("Ubah Tag Ini");
        mEdit.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.UBAH, 11, WARNA_HEADER));
        mEdit.addActionListener(e -> dialogTambahAtauEditChip(index));
        pop.add(mEdit);

        JMenuItem mHapus = new JMenuItem("Hapus Tag");
        mHapus.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.HAPUS, 11, IKON_MERAH));
        mHapus.addActionListener(e -> {
            int tanya = JOptionPane.showConfirmDialog(this,
                "Hapus tombol pintasan '" + listCustomChips.get(index)[0] + "'?",
                "Konfirmasi Hapus Tag", JOptionPane.YES_NO_OPTION);
            if (tanya == JOptionPane.YES_OPTION) {
                listCustomChips.remove(index);
                simpanCustomChipsKePrefs();
                renderChipPanel();
            }
        });
        pop.add(mHapus);

        pop.show(comp, x, y);
    }

    private void sisipkanTeksKeAnamnesis(String teks) {
        String curr = txtAnamnesis.getText();
        int caret = txtAnamnesis.getCaretPosition();
        if (curr.isEmpty()) {
            txtAnamnesis.setText(teks);
        } else {
            String prefix = curr.substring(0, caret);
            String suffix = curr.substring(caret);
            String spasi = (prefix.endsWith("\n") || prefix.isEmpty()) ? "" : "\n";
            txtAnamnesis.setText(prefix + spasi + teks + suffix);
        }
        txtAnamnesis.requestFocusInWindow();
    }

    private void simpanBlokTeksKeChip() {
        String blok = txtAnamnesis.getSelectedText();
        if (blok == null || blok.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Silakan blok / sorot kalimat di dalam kotak teks yang ingin dijadikan tombol pintasan cepat!",
                "Pemberitahuan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String saranJudul = blok.trim();
        if (saranJudul.length() > 24) saranJudul = saranJudul.substring(0, 24) + "...";

        JTextField inpJudul = new JTextField("+ " + saranJudul);
        JCheckBox chkFavorit = new JCheckBox("Jadikan Tag Favorit (Bintang)");
        chkFavorit.setFont(new Font("Tahoma", Font.BOLD, 11));
        chkFavorit.setForeground(new Color(29, 78, 216));

        JPanel panelPop = new JPanel(new BorderLayout(0, 6));
        panelPop.add(new JLabel("Beri nama tombol tag untuk blok kalimat ini:"), BorderLayout.NORTH);
        panelPop.add(inpJudul, BorderLayout.CENTER);
        panelPop.add(chkFavorit, BorderLayout.SOUTH);

        int res = JOptionPane.showConfirmDialog(this, panelPop, "Simpan Blok Teks Jadi Tag",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String label = inpJudul.getText().trim();
            if (!label.isEmpty()) {
                boolean isFav = chkFavorit.isSelected();
                listCustomChips.add(new String[]{label, blok.trim(), isFav ? "1" : "0"});
                simpanCustomChipsKePrefs();
                renderChipPanel();
                JOptionPane.showMessageDialog(this, "Tombol tag '" + label + "' berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void dialogTambahAtauEditChip(int editIndex) {
        JTextField inpJudul = new JTextField(editIndex >= 0 ? listCustomChips.get(editIndex)[0] : "+ ");
        JTextArea inpIsi = new JTextArea(4, 28);
        inpIsi.setLineWrap(true);
        inpIsi.setWrapStyleWord(true);

        String blokTeks = txtAnamnesis.getSelectedText();
        if (editIndex >= 0) {
            inpIsi.setText(listCustomChips.get(editIndex)[1]);
        } else if (blokTeks != null && !blokTeks.trim().isEmpty()) {
            inpIsi.setText(blokTeks.trim());
        }

        JCheckBox chkFavorit = new JCheckBox("Jadikan Tag Favorit (Bintang)");
        chkFavorit.setFont(new Font("Tahoma", Font.BOLD, 11));
        chkFavorit.setForeground(new Color(29, 78, 216));
        chkFavorit.setOpaque(false);
        boolean currentFav = editIndex >= 0 && listCustomChips.get(editIndex).length >= 3 && "1".equals(listCustomChips.get(editIndex)[2]);
        chkFavorit.setSelected(currentFav);

        JPanel panelPop = new JPanel(new BorderLayout(0, 8));
        panelPop.add(bungkusLabel("Nama Tombol Tag (Contoh: + Onset Mendadak) :", inpJudul), BorderLayout.NORTH);
        panelPop.add(bungkusLabel("Teks yang Akan Disisipkan Saat Diklik :", new JScrollPane(inpIsi)), BorderLayout.CENTER);
        panelPop.add(chkFavorit, BorderLayout.SOUTH);

        int res = JOptionPane.showConfirmDialog(this, panelPop,
                editIndex >= 0 ? "Ubah Tag Cepat" : "Tambah Tag Cepat Baru",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String j = inpJudul.getText().trim();
            String isi = inpIsi.getText().trim();
            if (!j.isEmpty() && !isi.isEmpty()) {
                boolean isFav = chkFavorit.isSelected();
                if (editIndex >= 0) {
                    listCustomChips.set(editIndex, new String[]{j, isi, isFav ? "1" : "0"});
                } else {
                    listCustomChips.add(new String[]{j, isi, isFav ? "1" : "0"});
                }
                simpanCustomChipsKePrefs();
                renderChipPanel();
            }
        }
    }

    // =========================================================================
    // ACTION BAR, TABEL, & FOOTER DENGAN IKON VEKTOR ARGON
    // =========================================================================
    private JPanel buatActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_HALUS),
            new EmptyBorder(0, 0, 10, 0)
        ));

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBtns.setOpaque(false);

        styleCleanButton(btnSimpan, new ArgonVectorIcon(ArgonVectorIcon.Type.SIMPAN, 15, IKON_BIRU), TEKS_GELAP);
        styleCleanButton(btnBaru,   new ArgonVectorIcon(ArgonVectorIcon.Type.BARU,   15, IKON_HIJAU), TEKS_GELAP);
        styleCleanButton(btnUbah,   new ArgonVectorIcon(ArgonVectorIcon.Type.UBAH,  15, IKON_ORANYE), TEKS_GELAP);
        styleCleanButton(btnHapus,  new ArgonVectorIcon(ArgonVectorIcon.Type.HAPUS,  15, IKON_MERAH), IKON_MERAH);
        stylePrimaryButton(btnPilih, new ArgonVectorIcon(ArgonVectorIcon.Type.TERAPKAN, 15, WARNA_TEAL_MUDA));

        btnSimpan.setMnemonic(KeyEvent.VK_S);
        btnBaru.setMnemonic(KeyEvent.VK_B);
        btnUbah.setMnemonic(KeyEvent.VK_G);
        btnHapus.setMnemonic(KeyEvent.VK_H);
        btnPilih.setMnemonic(KeyEvent.VK_P);

        btnSimpan.setToolTipText("Simpan template baru (Alt+S)");
        btnBaru.setToolTipText("Kosongkan isian untuk membuat template baru (Alt+B)");
        btnUbah.setToolTipText("Perbarui template terpilih pada tabel (Alt+G)");
        btnHapus.setToolTipText("Hapus template terpilih pada tabel (Alt+H)");
        btnPilih.setToolTipText("Pilih dan terapkan template ke rekam medis pasien (Alt+P / Enter / Dobel Klik)");

        btnSimpan.addActionListener(e -> simpan());
        btnUbah.addActionListener(e -> ganti());
        btnHapus.addActionListener(e -> hapus());
        btnBaru.addActionListener(e -> kosongkan());
        btnPilih.addActionListener(e -> terapkanTemplateTerpilih());

        leftBtns.add(btnSimpan);
        leftBtns.add(btnBaru);
        leftBtns.add(btnUbah);
        leftBtns.add(btnHapus);

        bar.add(leftBtns, BorderLayout.WEST);
        bar.add(btnPilih, BorderLayout.EAST);
        return bar;
    }

    private JPanel buatFooterBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setOpaque(false);
        JLabel lblCari = new JLabel("Cari Template :");
        lblCari.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblCari.setForeground(TEKS_GELAP);

        styleInput(txtCari);
        txtCari.setToolTipText("Ketik kata kunci nama atau isi anamnesis lalu tekan Enter...");
        txtCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                }
            }
        });

        styleCleanButton(btnCari, new ArgonVectorIcon(ArgonVectorIcon.Type.CARI, 15, IKON_SLATE), TEKS_GELAP);
        btnCari.setMnemonic(KeyEvent.VK_C);
        btnCari.setToolTipText("Cari data template (Alt+C / Enter)");
        btnCari.addActionListener(e -> tampil());

        styleCleanButton(btnKeluar, new ArgonVectorIcon(ArgonVectorIcon.Type.KELUAR, 15, IKON_MERAH), IKON_MERAH);
        btnKeluar.setMnemonic(KeyEvent.VK_K);
        btnKeluar.setToolTipText("Tutup dialog (Alt+K / ESC)");
        btnKeluar.addActionListener(e -> dispose());

        searchBox.add(lblCari);
        searchBox.add(txtCari);
        searchBox.add(btnCari);

        footer.add(searchBox, BorderLayout.WEST);
        footer.add(btnKeluar, BorderLayout.EAST);
        return footer;
    }

    private JPanel bungkusLabel(String judul, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(judul);
        l.setFont(new Font("Tahoma", Font.BOLD, 11));
        l.setForeground(TEKS_GELAP);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void styleInput(JTextField tf) {
        tf.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tf.setForeground(TEKS_GELAP);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_INPUT),
            new EmptyBorder(5, 8, 5, 8)
        ));
    }

    private void styleCleanButton(JButton btn, Icon icon, Color textColor) {
        btn.setIcon(icon);
        btn.setIconTextGap(7);
        btn.setFont(new Font("Tahoma", Font.BOLD, 11));
        btn.setBackground(Color.WHITE);
        btn.setForeground(textColor);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_INPUT),
            new EmptyBorder(5, 12, 5, 12)
        ));
    }

    private void stylePrimaryButton(JButton btn, Icon icon) {
        btn.setIcon(icon);
        btn.setIconTextGap(7);
        btn.setFont(new Font("Tahoma", Font.BOLD, 11));
        btn.setBackground(WARNA_HEADER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));
    }

    // =========================================================================
    // DATABASE, SKEMA, DAN LOGIKA CRUD
    // =========================================================================
    private void initTableDanSkema() {
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

            int count = Sequel.cariInteger("select count(*) from template_anamnesis_dokter");
            if (count == 0) {
                simpanTemplateAwal("Umum / Sacred Seven (S-O-A-P)",
                    "KELUHAN UTAMA : \n" +
                    "RIWAYAT PENYAKIT SEKARANG (RPS) :\n" +
                    "- Onset / Sejak kapan : \n" +
                    "- Lokasi & Penjalaran : \n" +
                    "- Karakteristik / Kualitas : \n" +
                    "- Derajat keparahan / Kuantitas : \n" +
                    "- Faktor memperberat : \n" +
                    "- Faktor memperingan : \n" +
                    "- Gejala penyerta / keluhan lain : \n" +
                    "RIWAYAT PENYAKIT DAHULU (RPD) : \n" +
                    "RIWAYAT PENGOBATAN (RPO) : \n" +
                    "RIWAYAT ALERGI : Tidak ada / ");

                simpanTemplateAwal("Demam / Febris (Pediatrik & Dewasa)",
                    "KELUHAN UTAMA : Demam / Panas badan\n" +
                    "RIWAYAT PENYAKIT SEKARANG :\n" +
                    "- Demam dirasakan sejak ... hari yang lalu, pola panas terus-menerus / naik-turun / menggigil\n" +
                    "- Kejang demam (-), penurunan kesadaran (-)\n" +
                    "- Batuk (-), pilek (-), nyeri menelan (-)\n" +
                    "- Mual (-), muntah (-), nyeri perut (-), BAB cair / diare (-)\n" +
                    "- Nyeri BAK (-), warna urin normal\n" +
                    "- Perdarahan spontan : mimisan (-), gusi berdarah (-), bintik merah / petechiae (-)\n" +
                    "- Makan dan minum : nafsu makan menurun, minum mau\n" +
                    "RIWAYAT PENYAKIT DAHULU : Riwayat kejang demam (-)\n" +
                    "RIWAYAT PENGOBATAN : Sudah minum paracetamol, demam turun sementara lalu naik kembali\n" +
                    "RIWAYAT ALERGI : Tidak ada");

                simpanTemplateAwal("Nyeri Dada / Chest Pain (Curiga ACS / Jantung)",
                    "KELUHAN UTAMA : Nyeri dada\n" +
                    "RIWAYAT PENYAKIT SEKARANG :\n" +
                    "- Nyeri dada dirasakan sejak ... jam/menit yang lalu, timbul tiba-tiba saat ...\n" +
                    "- Lokasi : Retrosternal / substernal / prekordial\n" +
                    "- Karakteristik : Seperti tertindih beban berat / diremas / rasa terbakar\n" +
                    "- Penjalaran : Menjalar ke lengan kiri / leher / rahang / punggung (-)\n" +
                    "- Durasi nyeri : > 20 menit, tidak membaik dengan istirahat\n" +
                    "- Gejala penyerta : Keringat dingin profus (+), sesak napas (+), mual / muntah (-)\n" +
                    "RIWAYAT PENYAKIT DAHULU : Riwayat sakit jantung (-), Hipertensi (+), Diabetes Melitus (-), Kolesterol (-)\n" +
                    "RIWAYAT PENGOBATAN : Rutin / tidak rutin minum obat antihipertensi\n" +
                    "RIWAYAT MEROKOK : Perokok aktif / pasif\n" +
                    "RIWAYAT ALERGI : Tidak ada");

                simpanTemplateAwal("Sesak Napas / Dyspnea (Respirasi / Asma / PPOK)",
                    "KELUHAN UTAMA : Sesak napas\n" +
                    "RIWAYAT PENYAKIT SEKARANG :\n" +
                    "- Sesak napas memberat sejak ... hari/jam yang lalu\n" +
                    "- Bunyi napas mencuit / mengi (+/-), sesak dipicu oleh debu / dingin / aktivitas\n" +
                    "- Batuk (+/-) berdahak putih/kuning encer/kental, demam (+/-)\n" +
                    "- Posisi nyaman : Lebih nyaman posisi duduk / setengah duduk, orthopnea (+/-)\n" +
                    "- Riwayat bengkak kedua kaki (-)\n" +
                    "RIWAYAT PENYAKIT DAHULU : Riwayat Asma (+/-), Riwayat PPOK (+/-), TB Paru (-)\n" +
                    "RIWAYAT PENGOBATAN : Pernah menggunakan inhaler / nebulizer\n" +
                    "RIWAYAT ALERGI : Debu, cuaca dingin, makanan");

                simpanTemplateAwal("Nyeri Perut / Abdominal Pain (Gastrointestinal / Kolik)",
                    "KELUHAN UTAMA : Nyeri perut\n" +
                    "RIWAYAT PENYAKIT SEKARANG :\n" +
                    "- Nyeri perut dirasakan sejak ... hari/jam yang lalu\n" +
                    "- Lokasi nyeri : Epigastrium / Ulu hati / Periumbilikal / Kuadran kanan bawah / Seluruh perut\n" +
                    "- Karakteristik : Melilit / tertusuk / perih / hilang timbul (kolik)\n" +
                    "- Mual (+/-), muntah (+/-) frekuensi ... kali isi makanan / cairan lambung\n" +
                    "- BAB : normal / cair / konstipasi / kehitaman (-), BAK normal\n" +
                    "- Terakhir makan jam ...\n" +
                    "RIWAYAT PENYAKIT DAHULU : Riwayat sakit maag / gastritis (+/-), batu empedu (-), operasi perut (-)\n" +
                    "RIWAYAT PENGOBATAN : Antasida / Omeprazole\n" +
                    "RIWAYAT ALERGI : Tidak ada");

                simpanTemplateAwal("Kasus Trauma / Kecelakaan Lalu Lintas (KLL)",
                    "KELUHAN UTAMA : Nyeri dan luka akibat kecelakaan lalu lintas\n" +
                    "MEKANISME TRAUMA (CHRONOLOGY) :\n" +
                    "- Pasien datang rujukan / dibawa warga setelah mengalami kecelakaan lalu lintas pada jam ... WIB di ...\n" +
                    "- Posisi pasien : Pengendara motor / penumpang / pejalan kaki, memakai helm (+/-)\n" +
                    "- Kronologi : Menabrak / ditabrak / terjatuh sendiri, kecepatan sedang/tinggi\n" +
                    "- Riwayat pingsan / hilang kesadaran saat/setelah kejadian (-/+) durasi ... menit\n" +
                    "- Muntah menyemprot (-), perdarahan dari hidung/telinga/mulut (-)\n" +
                    "- Nyeri dirasakan pada bagian : Kepala / leher / dada / perut / tangan / kaki\n" +
                    "- Pertolongan pertama di TKP : Diberi antiseptik / dipasang bidai / belum ada tindakan\n" +
                    "RIWAYAT ALERGI : Tidak ada");

                simpanTemplateAwal("Format Kosong (Headings)",
                    "KELUHAN UTAMA : \n" +
                    "RIWAYAT PENYAKIT SEKARANG (RPS) : \n" +
                    "RIWAYAT PENYAKIT DAHULU (RPD) : \n" +
                    "RIWAYAT PENGOBATAN (RPO) : \n" +
                    "RIWAYAT PENYAKIT KELUARGA (RPK) : \n" +
                    "RIWAYAT ALERGI : ");
            }
        } catch (Exception e) {
            System.out.println("Notif init skema template_anamnesis_dokter: " + e);
        }
    }

    private void simpanTemplateAwal(String nama, String isi) {
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "insert into template_anamnesis_dokter(kd_dokter,nama_template,anamnesis) values('-' ,?,?)"
            );
            ps.setString(1, nama);
            ps.setString(2, isi);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            System.out.println("Notif seed template anamnesis: " + e);
        }
    }

    public void setDokter(String kdDokter) {
        setDokter(kdDokter, "");
    }

    public void setDokter(String kdDokter, String nmDokter) {
        deteksiDokterLogin();
        if (kdDokter != null && !kdDokter.trim().isEmpty() && !kdDokter.trim().equals("-")) {
            this.kodeDokterAktif = kdDokter.trim();
            this.namaDokterAktif = (nmDokter != null && !nmDokter.trim().isEmpty()) ? nmDokter.trim() : "";
            if (this.namaDokterAktif.isEmpty()) {
                try {
                    this.namaDokterAktif = Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", this.kodeDokterAktif);
                } catch (Exception ignored) {}
            }
        } else {
            if (!this.kodeDokterLogin.isEmpty()) {
                this.kodeDokterAktif = this.kodeDokterLogin;
                this.namaDokterAktif = this.namaDokterLogin;
            } else {
                this.kodeDokterAktif = "-";
                this.namaDokterAktif = "";
            }
        }
        updateComboDokter();
        tampil();
    }

    private void updateComboDokter() {
        isUpdatingCombo = true;
        try {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            
            // Opsi 1: Diri Sendiri (Langsung baca dari login)
            String labelDiri;
            if (!kodeDokterLogin.isEmpty()) {
                labelDiri = "Diri Sendiri (" + (!namaDokterLogin.isEmpty() ? namaDokterLogin : kodeDokterLogin) + ")";
            } else {
                labelDiri = "Diri Sendiri (Login)";
            }
            model.addElement(labelDiri);

            // Opsi 2: Semua (Umum RS)
            model.addElement("Semua (Umum RS)");

            // Opsi 3: Dokter lain jika saat ini memilih dokter spesifik di luar login
            if (!kodeDokterAktif.equals("-") && !kodeDokterAktif.equals(kodeDokterLogin)) {
                String labelDokterLain = "Pribadi (" + (!namaDokterAktif.isEmpty() ? namaDokterAktif : kodeDokterAktif) + ")";
                model.addElement(labelDokterLain);
            }

            // Opsi 4: Pilih Dokter Lain...
            model.addElement("Pilih Dokter Lain...");

            cmbDokter.setModel(model);

            // Tentukan pilihan yang aktif
            if (kodeDokterAktif.equals("-")) {
                cmbDokter.setSelectedItem("Semua (Umum RS)");
            } else if (kodeDokterAktif.equals(kodeDokterLogin)) {
                cmbDokter.setSelectedItem(labelDiri);
            } else {
                cmbDokter.setSelectedIndex(2);
            }
        } finally {
            isUpdatingCombo = false;
        }
    }

    private void bukaCariDokter() {
        if (dlgDokter == null) {
            dlgDokter = new DlgCariDokter(this, true);
        }
        dlgDokter.isCek();
        dlgDokter.setSize(Math.min(900, getWidth() - 40), Math.min(600, getHeight() - 40));
        dlgDokter.setLocationRelativeTo(this);
        dlgDokter.emptTeks();
        dlgDokter.setVisible(true);

        if (dlgDokter.getTable().getSelectedRow() != -1) {
            String kd = dlgDokter.getTable().getValueAt(dlgDokter.getTable().getSelectedRow(), 0).toString();
            String nm = dlgDokter.getTable().getValueAt(dlgDokter.getTable().getSelectedRow(), 1).toString();
            setDokter(kd, nm);
        } else {
            updateComboDokter();
        }
    }

    public void setTemplateListener(TemplateAnamnesisListener listener) {
        this.listener = listener;
    }

    public void setListener(TemplateAnamnesisListener listener) {
        this.listener = listener;
    }

    public void tampil() {
        int selectedRowSebelumnya = tbTemplate.getSelectedRow();
        String selectedNoSebelumnya = "";
        if (selectedRowSebelumnya != -1 && selectedRowSebelumnya < tabMode.getRowCount()) {
            Object val = tabMode.getValueAt(selectedRowSebelumnya, 4);
            if (val != null) selectedNoSebelumnya = val.toString();
        }

        isMemuat = true;
        Valid.tabelKosong(tabMode);
        try {
            String cari = txtCari.getText().trim();
            String sql = "select no_template, nama_template, anamnesis, kd_dokter " +
                         "from template_anamnesis_dokter where (kd_dokter=? or kd_dokter='-') " +
                         (cari.isEmpty() ? "" : "and (nama_template like ? or anamnesis like ?) ") +
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
                String kdDok = rs.getString("kd_dokter");
                String labelDokter;
                if (kdDok.equals("-")) {
                    labelDokter = "Semua (Umum RS)";
                } else if (kdDok.equals(kodeDokterLogin) || (!kodeDokterLogin.isEmpty() && kdDok.equalsIgnoreCase(kodeDokterLogin))) {
                    labelDokter = "Diri Sendiri (" + (!namaDokterLogin.isEmpty() ? namaDokterLogin : "Saya") + ")";
                } else if (kdDok.equals(kodeDokterAktif)) {
                    labelDokter = "Pribadi (" + (!namaDokterAktif.isEmpty() ? namaDokterAktif : kdDok) + ")";
                } else {
                    String nmLain = "";
                    try {
                        nmLain = Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", kdDok);
                    } catch (Exception ignored) {}
                    labelDokter = "Pribadi (" + (!nmLain.isEmpty() ? nmLain : kdDok) + ")";
                }
                String ringkas = rs.getString("anamnesis").replace("\n", "  |  ");
                if (!selectedNoSebelumnya.isEmpty() && selectedNoSebelumnya.equals(noTemp)) {
                    rowToSelect = tabMode.getRowCount();
                }
                tabMode.addRow(new Object[]{
                    String.valueOf(noUrut++),
                    rs.getString("nama_template"),
                    ringkas,
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
            System.out.println("Notif tampil template_anamnesis_dokter: " + e);
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
            String no = tabMode.getValueAt(row, 4).toString();
            txtNoTemplate.setText(no);
            txtNamaTemplate.setText(tabMode.getValueAt(row, 1).toString());
            String dokterCol = tabMode.getValueAt(row, 3).toString();

            isUpdatingCombo = true;
            try {
                if (dokterCol.startsWith("Semua")) {
                    for (int i = 0; i < cmbDokter.getItemCount(); i++) {
                        if (cmbDokter.getItemAt(i).startsWith("Semua")) {
                            cmbDokter.setSelectedIndex(i);
                            break;
                        }
                    }
                } else if (dokterCol.startsWith("Diri Sendiri")) {
                    for (int i = 0; i < cmbDokter.getItemCount(); i++) {
                        if (cmbDokter.getItemAt(i).startsWith("Diri Sendiri")) {
                            cmbDokter.setSelectedIndex(i);
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < cmbDokter.getItemCount(); i++) {
                        if (cmbDokter.getItemAt(i).startsWith("Pribadi")) {
                            cmbDokter.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } finally {
                isUpdatingCombo = false;
            }

            try {
                PreparedStatement ps = koneksi.prepareStatement(
                    "select anamnesis from template_anamnesis_dokter where no_template=?"
                );
                ps.setString(1, no);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtAnamnesis.setText(rs.getString("anamnesis"));
                }
                rs.close();
                ps.close();
            } catch (Exception e) {
                System.out.println("Notif baca isi template anamnesis: " + e);
            }
        }
    }

    private void terapkanTemplateTerpilih() {
        int row = tbTemplate.getSelectedRow();
        String nama = txtNamaTemplate.getText().trim();
        String isi = txtAnamnesis.getText().trim();

        // 1. Jika teks di editor kosong, coba ambil dari tabel/database
        if (isi.isEmpty()) {
            if (row == -1 && tbTemplate.getRowCount() > 0) {
                row = 0;
                tbTemplate.setRowSelectionInterval(0, 0);
            }
            if (row != -1 && row < tabMode.getRowCount()) {
                nama = tabMode.getValueAt(row, 1).toString();
                try {
                    PreparedStatement ps = koneksi.prepareStatement(
                        "select anamnesis from template_anamnesis_dokter where no_template=?"
                    );
                    ps.setString(1, tabMode.getValueAt(row, 4).toString());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        isi = rs.getString("anamnesis");
                    }
                    rs.close();
                    ps.close();
                } catch (Exception e) {
                    System.out.println("Notif terapkan template anamnesis: " + e);
                }
            }
        } else {
            // 2. Jika teks di editor sudah ada, tentukan nama template
            if (nama.isEmpty() && row != -1 && row < tabMode.getRowCount()) {
                nama = tabMode.getValueAt(row, 1).toString();
            } else if (nama.isEmpty()) {
                nama = "Template Anamnesis";
            }
        }

        if (isi == null || isi.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu template pada tabel atau isi teks template terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (listener != null) {
            listener.onTemplateDipilih(nama, isi);
            dispose();
        } else {
            txtAnamnesis.setText(isi);
            JOptionPane.showMessageDialog(this, "Template '" + nama + "' berhasil dimuat ke editor!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void simpan() {
        if (txtNamaTemplate.getText().trim().isEmpty()) {
            Valid.textKosong(txtNamaTemplate, "Nama Template");
            return;
        }
        if (txtAnamnesis.getText().trim().isEmpty()) {
            Valid.textKosong(txtAnamnesis, "Isi Template Anamnesis");
            return;
        }

        Object selDokter = cmbDokter.getSelectedItem();
        boolean isUmum = selDokter != null && selDokter.toString().startsWith("Semua");
        boolean isDiri = selDokter != null && selDokter.toString().startsWith("Diri Sendiri");

        String targetDokter;
        if (isUmum) {
            targetDokter = "-";
        } else if (isDiri) {
            if (kodeDokterLogin.isEmpty()) deteksiDokterLogin();
            targetDokter = !kodeDokterLogin.isEmpty() ? kodeDokterLogin : kodeDokterAktif;
        } else {
            targetDokter = kodeDokterAktif;
        }

        if (!isUmum && targetDokter.equals("-")) {
            int tanya = JOptionPane.showConfirmDialog(this,
                "Template ini diatur sebagai pribadi dokter, namun dokter belum dipilih.\n" +
                "Apakah Anda ingin memilih dokter dari Master Dokter sekarang?",
                "Pilih Dokter", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (tanya == JOptionPane.YES_OPTION) {
                bukaCariDokter();
            }
            return;
        }

        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "insert into template_anamnesis_dokter(kd_dokter,nama_template,anamnesis) values(?,?,?)"
            );
            ps.setString(1, targetDokter);
            ps.setString(2, txtNamaTemplate.getText().trim());
            ps.setString(3, txtAnamnesis.getText().trim());
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, 
                "Template berhasil disimpan sebagai template " + (isUmum ? "Semua (Umum RS)" : (isDiri ? "Diri Sendiri" : "Pribadi Dokter")) + "!", 
                "Informasi", JOptionPane.INFORMATION_MESSAGE);
            kosongkan();
            tampil();
        } catch (Exception e) {
            System.out.println("Notif simpan template anamnesis: " + e);
            JOptionPane.showMessageDialog(this, "Gagal menyimpan template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ganti() {
        if (txtNoTemplate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih template pada tabel yang ingin diubah terlebih dahulu!", "Pemberitahuan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txtNamaTemplate.getText().trim().isEmpty()) {
            Valid.textKosong(txtNamaTemplate, "Nama Template");
            return;
        }
        if (txtAnamnesis.getText().trim().isEmpty()) {
            Valid.textKosong(txtAnamnesis, "Isi Template Anamnesis");
            return;
        }

        String no = txtNoTemplate.getText().trim();
        String pemilikAsli = Sequel.cariIsi("select kd_dokter from template_anamnesis_dokter where no_template=?", no);

        if (pemilikAsli != null && pemilikAsli.equals("-") && !kodeDokterAktif.equals("-")) {
            int tanya = JOptionPane.showConfirmDialog(this,
                "PERHATIAN: Template ini adalah template UMUM RS yang dipakai oleh seluruh dokter.\n" +
                "Apakah Anda yakin ingin memperbarui template umum ini?",
                "Konfirmasi Ubah Template Umum", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (tanya != JOptionPane.YES_OPTION) {
                return;
            }
        }

        Object selDokter = cmbDokter.getSelectedItem();
        boolean isUmum = selDokter != null && selDokter.toString().startsWith("Semua");
        boolean isDiri = selDokter != null && selDokter.toString().startsWith("Diri Sendiri");

        String targetDokter;
        if (isUmum) {
            targetDokter = "-";
        } else if (isDiri) {
            if (kodeDokterLogin.isEmpty()) deteksiDokterLogin();
            targetDokter = !kodeDokterLogin.isEmpty() ? kodeDokterLogin : kodeDokterAktif;
        } else {
            targetDokter = kodeDokterAktif;
        }

        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "update template_anamnesis_dokter set kd_dokter=?, nama_template=?, anamnesis=? where no_template=?"
            );
            ps.setString(1, targetDokter);
            ps.setString(2, txtNamaTemplate.getText().trim());
            ps.setString(3, txtAnamnesis.getText().trim());
            ps.setString(4, no);
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, "Template berhasil diperbarui!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            kosongkan();
            tampil();
        } catch (Exception e) {
            System.out.println("Notif ganti template anamnesis: " + e);
            JOptionPane.showMessageDialog(this, "Gagal mengubah template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        if (txtNoTemplate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih template yang ingin dihapus pada tabel!", "Pemberitahuan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String no = txtNoTemplate.getText().trim();
        String pemilikAsli = Sequel.cariIsi("select kd_dokter from template_anamnesis_dokter where no_template=?", no);

        if (pemilikAsli != null && pemilikAsli.equals("-") && !kodeDokterAktif.equals("-")) {
            int konfirm = JOptionPane.showConfirmDialog(this,
                "PERHATIAN: Template '" + txtNamaTemplate.getText() + "' adalah template UMUM yang digunakan oleh SEMUA dokter.\n" +
                "Apakah Anda yakin benar-benar ingin menghapusnya untuk semua dokter?",
                "Konfirmasi Hapus Template Umum", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (konfirm != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus template '" + txtNamaTemplate.getText() + "'?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (konfirmasi != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            PreparedStatement ps = koneksi.prepareStatement("delete from template_anamnesis_dokter where no_template=?");
            ps.setString(1, no);
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, "Template berhasil dihapus!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            kosongkan();
            tampil();
        } catch (Exception e) {
            System.out.println("Notif hapus template anamnesis: " + e);
            JOptionPane.showMessageDialog(this, "Gagal menghapus template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kosongkan() {
        txtNoTemplate.setText("");
        txtNamaTemplate.setText("");
        txtAnamnesis.setText("");
        boolean found = false;
        for (int i = 0; i < cmbDokter.getItemCount(); i++) {
            if (cmbDokter.getItemAt(i).startsWith("Diri Sendiri")) {
                cmbDokter.setSelectedIndex(i);
                found = true;
                break;
            }
        }
        if (!found && !kodeDokterAktif.equals("-")) {
            for (int i = 0; i < cmbDokter.getItemCount(); i++) {
                if (cmbDokter.getItemAt(i).startsWith("Pribadi")) {
                    cmbDokter.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            cmbDokter.setSelectedIndex(0);
        }
        txtNamaTemplate.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DlgMasterTemplateAnamnesis dialog = new DlgMasterTemplateAnamnesis(null, true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }
}
