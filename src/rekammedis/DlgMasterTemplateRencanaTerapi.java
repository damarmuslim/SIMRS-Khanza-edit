package rekammedis;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import kepegawaian.DlgCariDokter;
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
import java.awt.Point;
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
import javax.swing.table.TableColumn;
import widget.ThemeConfig;

/**
 * Master Template Rencana / Tindakan / Terapi (Tatalaksana) Dokter IGD / Poliklinik.
 * Mengadopsi desain visual modern Argon dengan ikon vektor resolusi tinggi (SVG-style),
 * sistem preset spesialisasi (Umum/Simptomatik, Trauma/Bedah, Jantung/ACS, Respirasi, Pediatrik),
 * chip cepat, kustom tag dokter, dan integrasi penuh dengan database template_rencana_terapi_dokter.
 */
public final class DlgMasterTemplateRencanaTerapi extends JDialog {

    private final DefaultTableModel tabMode;
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
    public interface TemplateTerapiListener {
        void onTemplateDipilih(String namaTemplate, String isiRencanaTerapi);
    }
    private TemplateTerapiListener listener;

    // === KOMPONEN INPUT ===
    public final JTextField txtNoTemplate = new JTextField();
    public final JTextField txtNamaTemplate = new JTextField();
    public final JComboBox<String> cmbDokter = new JComboBox<>(new String[]{
        "Semua (Umum RS)", "Pribadi Dokter"
    });
    public final JButton btnCariDokter = new JButton();
    public final JComboBox<String> cmbPreset = new JComboBox<>(new String[]{
        "Umum / Simptomatik (Dewasa)", "Trauma / Bedah (Emergency)", "Kardiovaskular (Sindrom Koroner Akut)",
        "Respirasi / Asma Akut / PPOK", "Pediatrik / Anak (Febris / Dehidrasi)"
    });
    public final JTextArea txtRencanaTerapi = new JTextArea(5, 20);
    public final JTextField txtCari = new JTextField(24);

    // === TOMBOL AKSI MODERN ===
    public final JButton btnSimpan = new JButton("Simpan");
    public final JButton btnBaru = new JButton("Baru");
    public final JButton btnUbah = new JButton("Ubah");
    public final JButton btnHapus = new JButton("Hapus");
    public final JButton btnPilih = new JButton("Pilih / Terapkan");
    public final JButton btnCari = new JButton("Cari");
    public final JButton btnKeluar = new JButton("Keluar");

    public final JTable tbTemplate;

    // === CHIP PANEL & PREFERENSI ===
    private final Preferences prefsChip = Preferences.userNodeForPackage(DlgMasterTemplateRencanaTerapi.class);
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
                    // Kepala
                    g2.draw(new Ellipse2D.Float(s * 0.32f, s * 0.12f, s * 0.36f, s * 0.36f));
                    // Bahu
                    Path2D sh = new Path2D.Float();
                    sh.moveTo(s * 0.16f, s * 0.86f);
                    sh.curveTo(s * 0.16f, s * 0.60f, s * 0.84f, s * 0.60f, s * 0.84f, s * 0.86f);
                    g2.draw(sh);
                    // Garis medikal stetoskop / salib
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
                int targetWidth = target.getWidth();
                Container c = target;
                while (targetWidth == 0 && c.getParent() != null) {
                    c = c.getParent();
                    targetWidth = c.getWidth();
                }
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int maxRowHeight = 0;
                int nmembers = target.getComponentCount();

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, maxRowHeight);
                            rowWidth = 0;
                            maxRowHeight = 0;
                        }
                        if (rowWidth != 0) {
                            rowWidth += hgap;
                        }
                        rowWidth += d.width;
                        maxRowHeight = Math.max(maxRowHeight, d.height);
                    }
                }
                addRow(dim, rowWidth, maxRowHeight);
                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;

                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) {
                dim.height += getVgap();
            }
            dim.height += rowHeight;
        }
    }

    public DlgMasterTemplateRencanaTerapi(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Master Template Rencana / Tindakan / Terapi Dokter");
        setSize(1000, 660);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        deteksiDokterLogin();
        if (!this.kodeDokterLogin.isEmpty()) {
            this.kodeDokterAktif = this.kodeDokterLogin;
            this.namaDokterAktif = this.namaDokterLogin;
        }

        initTableDanSkema();

        // 1. HEADER BAR ARGON
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WARNA_HEADER);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLeft.setOpaque(false);
        JLabel lblTitle = new JLabel("Master Template Rencana & Terapi Dokter");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 13));

        JLabel lblBadge = new JLabel("Tatalaksana / Tindak Lanjut");
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

        // Tabel Data (Kolom ke-4 hidden untuk no_template)
        String[] kolom = {"No", "Nama Template", "Rencana / Tindakan / Terapi", "Dokter", "no_template"};
        tabMode = new DefaultTableModel(null, kolom) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tbTemplate = new JTable(tabMode) {
            @Override
            public String getToolTipText(MouseEvent e) {
                Point p = e.getPoint();
                int row = rowAtPoint(p);
                int col = columnAtPoint(p);
                if (row >= 0 && col == 2) {
                    Object val = getValueAt(row, col);
                    if (val != null) {
                        return "<html><body style='width:380px; padding:6px; font-family:Tahoma; font-size:11px; color:#1E293B;'>"
                                + val.toString().replace("  |  ", "<br>").replace(" | ", "<br>")
                                + "</body></html>";
                    }
                }
                return super.getToolTipText(e);
            }
        };

        tbTemplate.setRowHeight(26);
        tbTemplate.setFont(new Font("Tahoma", Font.PLAIN, 11));
        tbTemplate.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbTemplate.setSelectionBackground(ThemeConfig.TABLE_SELECTION_BG);
        tbTemplate.setSelectionForeground(ThemeConfig.TABLE_SELECTION_TEXT);
        tbTemplate.setShowVerticalLines(true);
        tbTemplate.setGridColor(BORDER_HALUS);
        tbTemplate.setDefaultRenderer(Object.class, new WarnaTable());

        int[] lebarKolom = {45, 230, 500, 145};
        for (int i = 0; i < lebarKolom.length; i++) {
            TableColumn column = tbTemplate.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebarKolom[i]);
        }

        // Sembunyikan kolom index 4 (no_template)
        tbTemplate.getColumnModel().getColumn(4).setMinWidth(0);
        tbTemplate.getColumnModel().getColumn(4).setMaxWidth(0);
        tbTemplate.getColumnModel().getColumn(4).setPreferredWidth(0);
        tbTemplate.getColumnModel().getColumn(4).setResizable(false);

        // Renderer Kolom 0 (No)
        tbTemplate.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(ThemeConfig.TABLE_SELECTION_BG);
                    c.setForeground(ThemeConfig.TABLE_SELECTION_TEXT);
                } else {
                    c.setBackground((row % 2 == 0) ? Color.WHITE : ThemeConfig.ROW_ALT);
                    c.setForeground(ThemeConfig.TEXT_NORMAL);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        // Renderer Kolom 3 (Dokter / Kepemilikan)
        tbTemplate.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(ThemeConfig.TABLE_SELECTION_BG);
                    c.setForeground(ThemeConfig.TABLE_SELECTION_TEXT);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    String valStr = value != null ? value.toString() : "";
                    if (valStr.startsWith("Pribadi")) {
                        c.setForeground(new Color(29, 78, 216));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(new Color(100, 116, 139));
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                    c.setBackground((row % 2 == 0) ? Color.WHITE : ThemeConfig.ROW_ALT);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        tbTemplate.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !isMemuat) {
                pilihBarisTabel();
            }
        });

        tbTemplate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    terapkanTemplateTerpilih();
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

        JScrollPane spTabel = new JScrollPane(tbTemplate);
        spTabel.setBorder(BorderFactory.createLineBorder(BORDER_HALUS));
        spTabel.setPreferredSize(new Dimension(950, 240));

        body.add(spTabel, BorderLayout.CENTER);
        body.add(buatFooterBar(), BorderLayout.SOUTH);

        add(body, BorderLayout.CENTER);

        // Muat custom chip & render preset awal
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
        JLabel lblTerapi = new JLabel("Rencana / Tindakan / Terapi (Tatalaksana) :");
        lblTerapi.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblTerapi.setForeground(TEKS_GELAP);

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
        btnSimpanBlok.setToolTipText("Blok teks di bawah lalu klik tombol ini untuk membuat pintasan cepat baru");
        btnSimpanBlok.addActionListener(e -> simpanBlokTeksKeChip());

        JButton btnTagBaru = new JButton("Tag Custom Baru");
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
        btnTagBaru.setToolTipText("Tambah tombol pintasan klinis buatan sendiri");
        btnTagBaru.addActionListener(e -> dialogTambahAtauEditChip(-1));

        chipTools.add(btnSimpanBlok);
        chipTools.add(btnTagBaru);

        headerArea.add(lblTerapi, BorderLayout.WEST);
        headerArea.add(chipTools, BorderLayout.EAST);

        // Container Kotak Chip
        chipContainer.setOpaque(true);
        chipContainer.setBackground(Color.WHITE);
        chipContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_HALUS),
            new EmptyBorder(4, 6, 4, 6)
        ));

        // Area TextArea
        txtRencanaTerapi.setFont(new Font("Tahoma", Font.PLAIN, 12));
        txtRencanaTerapi.setLineWrap(true);
        txtRencanaTerapi.setWrapStyleWord(true);
        txtRencanaTerapi.setMargin(new Insets(6, 8, 6, 8));

        JScrollPane scrollTerapi = new JScrollPane(txtRencanaTerapi);
        scrollTerapi.setBorder(BorderFactory.createLineBorder(BORDER_INPUT));
        scrollTerapi.setPreferredSize(new Dimension(940, 110));

        // Hint bar
        JPanel hintBar = new JPanel(new BorderLayout());
        hintBar.setOpaque(false);
        JLabel lblHint1 = new JLabel("[i] Klik Kiri untuk menyisipkan ke teks. Klik Kanan pada tag untuk jadikan favorit / ubah / hapus.");
        lblHint1.setFont(new Font("Tahoma", Font.PLAIN, 10));
        lblHint1.setForeground(TEKS_MUTED);

        JLabel lblHint2 = new JLabel("Blok kalimat di kotak teks lalu klik 'Simpan Blok Teks Jadi Tag'.");
        lblHint2.setFont(new Font("Tahoma", Font.PLAIN, 10));
        lblHint2.setForeground(TEKS_MUTED);

        hintBar.add(lblHint1, BorderLayout.WEST);
        hintBar.add(lblHint2, BorderLayout.EAST);

        JPanel areaWrapper = new JPanel(new BorderLayout(0, 4));
        areaWrapper.setOpaque(false);
        areaWrapper.add(chipContainer, BorderLayout.NORTH);
        areaWrapper.add(scrollTerapi, BorderLayout.CENTER);
        areaWrapper.add(hintBar, BorderLayout.SOUTH);

        row2.add(headerArea, BorderLayout.NORTH);
        row2.add(areaWrapper, BorderLayout.CENTER);

        card.add(row1, BorderLayout.NORTH);
        card.add(row2, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // FITUR PRESET CHIP & KUSTOM DOKTER
    // =========================================================================
    private void muatCustomChips() {
        listCustomChips.clear();
        String saved = prefsChip.get("custom_chips_rencana_terapi_argon", "");
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
        prefsChip.put("custom_chips_rencana_terapi_argon", sb.toString());
    }

    private void renderChipPanel() {
        chipContainer.removeAll();
        String preset = (String) cmbPreset.getSelectedItem();
        if (preset == null) preset = "Umum / Simptomatik (Dewasa)";

        List<String[]> presetChips = new ArrayList<>();
        if (preset.startsWith("Umum")) {
            presetChips.add(new String[]{"+ Tirah Baring", "Tirah baring (bed rest), posisi semi fowler nyaman"});
            presetChips.add(new String[]{"+ O2 Kanul", "Oksigenasi via kanul nasal 2-4 lpm bila SpO2 < 95%"});
            presetChips.add(new String[]{"+ Infus RL", "Infus Cairan: RL 500 cc / 8 jam (20 tpm)"});
            presetChips.add(new String[]{"+ Infus NaCl", "Infus Cairan: NaCl 0.9% 500 cc / 8 jam (20 tpm)"});
            presetChips.add(new String[]{"+ Ranitidine IV", "Injeksi Ranitidine 50 mg IV (tiap 12 jam)"});
            presetChips.add(new String[]{"+ Ondansetron IV", "Injeksi Ondansetron 4 mg IV (bila mual/muntah)"});
            presetChips.add(new String[]{"+ Ketorolac IV", "Injeksi Ketorolac 30 mg IV (bila nyeri sedang/berat)"});
            presetChips.add(new String[]{"+ Paracetamol Drip", "Paracetamol drip 1 gr IV (bila demam tinggi > 38.5 C)"});
            presetChips.add(new String[]{"+ Cek Lab & Rontgen", "Cek Laboratorium cito (DL, GDS, Ureum, Creatinin) & Foto Thorax AP/PA"});
            presetChips.add(new String[]{"+ Konsul DPJP", "Lapor DPJP untuk advis lanjutan & rencana rawat inap"});
            presetChips.add(new String[]{"[Reset] Format Terstruktur",
                "1. MONITORING & OBSERVASI :\n" +
                "   - Observasi tanda vital dan keadaan umum per 15-30 menit\n" +
                "2. TINDAKAN MEDIS & KEPERAWATAN :\n" +
                "   - Tirah baring, pasang IV line jarum 20G\n" +
                "3. TERAPI CAIRAN & MEDIKAMENTOSA :\n" +
                "   - Infus RL 20 tpm\n" +
                "   - Injeksi Ranitidine 50 mg IV\n" +
                "4. EDUKASI PASIEN / KELUARGA :\n" +
                "   - Penjelasan kondisi klinis dan rencana pemeriksaan\n" +
                "5. RENCANA TINDAK LANJUT :\n" +
                "   - Lapor DPJP untuk advis lanjutan & rawat inap"});
        } else if (preset.startsWith("Trauma")) {
            presetChips.add(new String[]{"+ Resusitasi ABCDE", "Airway & C-Spine control (cervical collar bila curiga cedera leher), O2 NRM 10-15 lpm"});
            presetChips.add(new String[]{"+ 2 Jalur IV Line", "Pasang 2 jalur IV line jarum besar (16G/18G), guyur RL bila syok hipovolemik"});
            presetChips.add(new String[]{"+ Wound Toilet & Hecting", "Wound toilet / debridement luka, jahit situasi & balut tekan perdarahan aktif"});
            presetChips.add(new String[]{"+ Fiksasi Bidai", "Fiksasi dan pasang bidai (splint) pada regio kecurigaan fraktur"});
            presetChips.add(new String[]{"+ ATS / TT", "ATS 1500 IU IM (setelah skin test) / TT 0.5 cc IM"});
            presetChips.add(new String[]{"+ Ceftriaxone IV", "Injeksi Ceftriaxone 1 gr IV (profilaksis luka kotor pasca skin test)"});
            presetChips.add(new String[]{"+ Analgetik Trauma", "Injeksi Ketorolac 30 mg IV / Tramadol 50 mg drip"});
            presetChips.add(new String[]{"+ Foto Rontgen & Konsul Bedah", "Rontgen area cedera / Thorax-Pelvis AP & Konsul CITO dr. Spesialis Bedah (DPJP)"});
        } else if (preset.startsWith("Kardiovaskular")) {
            presetChips.add(new String[]{"+ Tirah Baring Total", "Tirah baring total, posisi semi fowler nyaman, pasang monitor EKG kontinyu"});
            presetChips.add(new String[]{"+ IV Line KVO", "Pasang IV line NaCl 0.9% maintenance (Keep Vein Open)"});
            presetChips.add(new String[]{"+ ISDN Sublingual", "ISDN 5 mg sublingual (dapat diulang tiap 5 mnt max 3x bila tensi sistolik > 90 mmHg)"});
            presetChips.add(new String[]{"+ Aspilet & CPG", "Aspilet 160-320 mg kunyah + Clopidogrel 300 mg PO (loading dose awal ACS)"});
            presetChips.add(new String[]{"+ Serial EKG 12 Lead", "Rekam serial EKG 12 lead per 15-30 menit atau bila ada nyeri dada berulang"});
            presetChips.add(new String[]{"+ Enoxaparin / Heparin", "Injeksi Fondaparinux 2.5 mg SC atau Enoxaparin sesuai advis DPJP"});
            presetChips.add(new String[]{"+ Cek Troponin Cito", "Cek Troponin I/T cito, CK-MB, elektrolit, fungsi ginjal"});
            presetChips.add(new String[]{"+ Konsul Sp.JP / ICCU", "Konsul CITO dr. Spesialis Jantung (Sp.JP) & persiapan transfer ICCU / PCI"});
        } else if (preset.startsWith("Respirasi")) {
            presetChips.add(new String[]{"+ Posisi Fowler & O2", "Posisi fowler / duduk tegak, O2 nasal kanul 2-4 lpm (titrasi SpO2 94-98%)"});
            presetChips.add(new String[]{"+ Nebu Combivent", "Nebulisasi Combivent 1 respule + Pulmicort 1 respule (dapat diulang tiap 20-30 menit)"});
            presetChips.add(new String[]{"+ Metilprednisolon IV", "Injeksi Metilprednisolon 62.5 mg IV atau Deksametason 5 mg IV"});
            presetChips.add(new String[]{"+ Aminofilin Drip", "Injeksi Aminofilin drip dalam D5% sesuai advis DPJP"});
            presetChips.add(new String[]{"+ Evaluasi Sesak", "Evaluasi frekuensi napas, retraksi dada, dan suara wheezing pasca nebulisasi"});
            presetChips.add(new String[]{"+ Foto Thorax & Konsul Paru", "Foto Thorax AP/PA & Lapor DPJP Paru / Penyakit Dalam untuk advis rawat"});
        } else if (preset.startsWith("Pediatrik")) {
            presetChips.add(new String[]{"+ Bebaskan Jalan Napas", "Bebaskan jalan napas, longgarkan pakaian, kompres hangat bila demam tinggi"});
            presetChips.add(new String[]{"+ Oksigenasi Anak", "Oksigenasi kanul nasal 1-2 lpm"});
            presetChips.add(new String[]{"+ Paracetamol Anak", "Paracetamol sirup/drop 10-15 mg/kgBB PO atau rectal bila demam/kejang demam"});
            presetChips.add(new String[]{"+ Diazepam Rectal", "Bila kejang aktif: Diazepam rectal 5 mg (<10 kg) / 10 mg (>10 kg)"});
            presetChips.add(new String[]{"+ Rehidrasi KaEN 1B", "Rehidrasi cairan KaEN 1B / RL mikro drip sesuai kebutuhan derajat dehidrasi"});
            presetChips.add(new String[]{"+ Injeksi Ampisilin/Gentamisin", "Antibiotik spektrum luas sesuai advis DPJP Anak"});
            presetChips.add(new String[]{"+ Cek Lab Cito", "Pemeriksaan Darah Lengkap, GDS, Elektrolit cito"});
            presetChips.add(new String[]{"+ Konsul Sp.A", "Konsul Dokter Spesialis Anak (Sp.A) untuk advis terapi dan rencana rawat"});
        }

        // 1. Tampilkan preset bawaan
        for (String[] chip : presetChips) {
            chipContainer.add(buatTombolChipPreset(chip[0], chip[1]));
        }

        // 2. Tampilkan custom chips dokter (favorit didahulukan atau sesuai urutan)
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
        btn.addActionListener(e -> sisipkanTeksKeTerapi(isiTeks));
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
            btn.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.STAR, 11, new Color(245, 158, 11)));
            btn.setBackground(new Color(239, 246, 255));
            btn.setForeground(new Color(29, 78, 216));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253)),
                new EmptyBorder(2, 7, 2, 7)
            ));
            btn.setToolTipText("<html><b>[Favorit] " + label + "</b><br><b>Klik Kiri:</b> Sisipkan ke teks<br><b>Klik Kanan:</b> Ubah / Hapus / Batal Favorit<hr>"
                    + isiTeks.replace("\n", "<br>") + "</html>");
        } else {
            btn.setIcon(null);
            btn.setBackground(new Color(248, 250, 252));
            btn.setForeground(new Color(51, 65, 85));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_INPUT),
                new EmptyBorder(2, 7, 2, 7)
            ));
            btn.setToolTipText("<html><b>Tag Kustom (" + label + ")</b><br><b>Klik Kiri:</b> Sisipkan ke teks<br><b>Klik Kanan:</b> Ubah / Hapus / Jadikan Favorit<hr>"
                    + isiTeks.replace("\n", "<br>") + "</html>");
        }

        btn.addActionListener(e -> sisipkanTeksKeTerapi(isiTeks));

        // Klik Kanan: Ubah / Hapus / Toggle Favorit
        JPopupMenu pop = new JPopupMenu();
        JMenuItem mnFav = new JMenuItem(isFav ? "Hapus dari Favorit" : "Jadikan Tag Favorit (Bintang)");
        mnFav.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.STAR, 12, isFav ? Color.GRAY : new Color(245, 158, 11)));
        mnFav.addActionListener(e -> {
            String[] item = listCustomChips.get(index);
            String newFav = isFav ? "0" : "1";
            listCustomChips.set(index, new String[]{item[0], item[1], newFav});
            simpanCustomChipsKePrefs();
            renderChipPanel();
        });

        JMenuItem mnEdit = new JMenuItem("Ubah Tag '" + label + "'");
        mnEdit.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.UBAH, 12, IKON_ORANYE));
        mnEdit.addActionListener(e -> dialogTambahAtauEditChip(index));

        JMenuItem mnHapus = new JMenuItem("Hapus Tag '" + label + "'");
        mnHapus.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.HAPUS, 12, IKON_MERAH));
        mnHapus.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,
                "Hapus tag kustom '" + label + "'?", "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                listCustomChips.remove(index);
                simpanCustomChipsKePrefs();
                renderChipPanel();
            }
        });

        pop.add(mnFav);
        pop.addSeparator();
        pop.add(mnEdit);
        pop.add(mnHapus);
        btn.setComponentPopupMenu(pop);

        return btn;
    }

    private void sisipkanTeksKeTerapi(String teks) {
        String current = txtRencanaTerapi.getText().trim();
        txtRencanaTerapi.setText((current.isEmpty() ? "" : current + "\n") + teks);
        txtRencanaTerapi.requestFocusInWindow();
        txtRencanaTerapi.setCaretPosition(txtRencanaTerapi.getText().length());
    }

    private void simpanBlokTeksKeChip() {
        String blok = txtRencanaTerapi.getSelectedText();
        if (blok == null || blok.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Silakan blok/seleksi kalimat pada kotak Rencana / Terapi yang ingin dijadikan tombol pintasan!",
                "Petunjuk", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String namaDefault = "+ " + (blok.length() > 20 ? blok.substring(0, 20).trim() + "..." : blok.trim());
        JTextField inpJudul = new JTextField(namaDefault);
        JCheckBox chkFavorit = new JCheckBox("Jadikan Tag Favorit (Warna Biru & Icon Bintang)");
        chkFavorit.setFont(new Font("Tahoma", Font.BOLD, 11));
        chkFavorit.setForeground(new Color(29, 78, 216));
        chkFavorit.setOpaque(false);

        JPanel panelSimpan = new JPanel(new BorderLayout(0, 8));
        panelSimpan.add(bungkusLabel("Nama Tombol Tag :", inpJudul), BorderLayout.NORTH);
        panelSimpan.add(chkFavorit, BorderLayout.SOUTH);

        int res = JOptionPane.showConfirmDialog(this, panelSimpan,
            "Simpan Blok Jadi Tag Baru",
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

        String blokTeks = txtRencanaTerapi.getSelectedText();
        if (editIndex >= 0) {
            inpIsi.setText(listCustomChips.get(editIndex)[1]);
        } else if (blokTeks != null && !blokTeks.trim().isEmpty()) {
            inpIsi.setText(blokTeks.trim());
        }

        JCheckBox chkFavorit = new JCheckBox("Jadikan Tag Favorit (Warna Biru & Icon Bintang)");
        chkFavorit.setFont(new Font("Tahoma", Font.BOLD, 11));
        chkFavorit.setForeground(new Color(29, 78, 216));
        chkFavorit.setOpaque(false);
        boolean currentFav = editIndex >= 0 && listCustomChips.get(editIndex).length >= 3 && "1".equals(listCustomChips.get(editIndex)[2]);
        chkFavorit.setSelected(currentFav);

        JPanel panelPop = new JPanel(new BorderLayout(0, 8));
        panelPop.add(bungkusLabel("Nama Tombol Tag (Contoh: + Infus RL 20 tpm) :", inpJudul), BorderLayout.NORTH);
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
        styleCleanButton(btnUbah,   new ArgonVectorIcon(ArgonVectorIcon.Type.UBAH,   15, IKON_ORANYE), TEKS_GELAP);
        styleCleanButton(btnHapus,  new ArgonVectorIcon(ArgonVectorIcon.Type.HAPUS,  15, IKON_MERAH), new Color(185, 28, 28));
        stylePrimaryButton(btnPilih, new ArgonVectorIcon(ArgonVectorIcon.Type.TERAPKAN, 15, WARNA_TEAL_MUDA));

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
        txtCari.setToolTipText("Ketik kata kunci nama atau isi rencana terapi lalu tekan Enter...");
        txtCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                }
            }
        });

        styleCleanButton(btnCari, new ArgonVectorIcon(ArgonVectorIcon.Type.CARI, 15, IKON_SLATE), TEKS_GELAP);
        btnCari.addActionListener(e -> tampil());

        styleCleanButton(btnKeluar, new ArgonVectorIcon(ArgonVectorIcon.Type.KELUAR, 15, IKON_MERAH), new Color(185, 28, 28));
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

            int count = Sequel.cariInteger("select count(*) from template_rencana_terapi_dokter");
            if (count == 0) {
                simpanTemplateAwal("Terapi Umum / Simptomatik (Dewasa)",
                    "1. MONITORING & OBSERVASI:\n" +
                    "   - Observasi tanda vital dan keadaan umum per 15-30 menit\n" +
                    "   - Pasang monitor SpO2 / EKG bila diperlukan\n" +
                    "2. NON-FARMAKOLOGI:\n" +
                    "   - Tirah baring (bed rest), posisi semi fowler\n" +
                    "   - Oksigenasi via kanul nasal 2-4 lpm bila SpO2 < 95%\n" +
                    "   - Pasang jalur IV line jarum 20G\n" +
                    "3. FARMAKOLOGI:\n" +
                    "   - Infus Cairan: RL / NaCl 0.9% 500 cc / 8 jam (20 tpm)\n" +
                    "   - Injeksi Ranitidine 50 mg / Ondansetron 4 mg IV (bila mual/muntah)\n" +
                    "   - Injeksi Ketorolac 30 mg / Paracetamol drip 1 gr IV (bila nyeri/demam)\n" +
                    "4. RENCANA LANJUTAN:\n" +
                    "   - Evaluasi hasil pemeriksaan laboratorium / radiologi\n" +
                    "   - Lapor DPJP untuk advis lanjutan & rencana rawat inap");

                simpanTemplateAwal("Kasus Trauma / Bedah (Emergency)",
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
                    "   - Konsul CITO Dokter Spesialis Bedah (DPJP) untuk tatalaksana definitif");

                simpanTemplateAwal("Kardiovaskular (Sindrom Koroner Akut)",
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
                    "   - Konsul CITO Dokter Spesialis Jantung (Sp.JP) / persiapan rujukan ICCU");

                simpanTemplateAwal("Respirasi / Asma Akut / PPOK",
                    "1. TATALAKSANA RESPIRASI:\n" +
                    "   - Posisi fowler / tegak\n" +
                    "   - Oksigenasi 2-4 lpm via kanul (titrasi SpO2 94-98% atau 88-92% pada PPOK)\n" +
                    "2. NEBULISASI & MEDIKAMENTOSA:\n" +
                    "   - Nebulisasi Combivent 1 respule + Pulmicort 1 respule (bila perlu)\n" +
                    "   - Injeksi Deksametason 5 mg IV atau Metilprednisolon 62.5 mg IV\n" +
                    "   - Injeksi Aminofilin drip (bila diinstruksikan DPJP)\n" +
                    "3. RENCANA LANJUTAN:\n" +
                    "   - Evaluasi keluhan wheezing dan retraksi otot napas pasca nebulisasi\n" +
                    "   - Foto Thorax AP/PA");

                simpanTemplateAwal("Pediatrik / Anak (Febris / Dehidrasi)",
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
                    "   - Konsul Dokter Spesialis Anak (Sp.A) untuk advis terapi rawat");

                simpanTemplateAwal("Format Terstruktur Kosong",
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
                    "   - Konsul DPJP / Rencana Rawat Inap / Rawat Jalan");
            }
        } catch (Exception e) {
            System.out.println("Notif init skema template_rencana_terapi_dokter: " + e);
        }
    }

    private void simpanTemplateAwal(String nama, String isi) {
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "insert into template_rencana_terapi_dokter(kd_dokter,nama_template,rencana_terapi) values('-' ,?,?)"
            );
            ps.setString(1, nama);
            ps.setString(2, isi);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            System.out.println("Notif seed template rencana terapi: " + e);
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
            // Jika kdDokter dari pemanggil kosong/dash, otomatis pakai Diri Sendiri (login)
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
            // Jika dialog ditutup tanpa memilih dokter, kembalikan seleksi combo
            updateComboDokter();
        }
    }

    public void setTemplateListener(TemplateTerapiListener listener) {
        this.listener = listener;
    }

    public void setListener(TemplateTerapiListener listener) {
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
            String sql = "select no_template, nama_template, rencana_terapi, kd_dokter " +
                         "from template_rencana_terapi_dokter where (kd_dokter=? or kd_dokter='-') " +
                         (cari.isEmpty() ? "" : "and (nama_template like ? or rencana_terapi like ?) ") +
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
                String ringkasTerapi = rs.getString("rencana_terapi").replace("\n", "  |  ");
                if (!selectedNoSebelumnya.isEmpty() && selectedNoSebelumnya.equals(noTemp)) {
                    rowToSelect = tabMode.getRowCount();
                }
                tabMode.addRow(new Object[]{
                    String.valueOf(noUrut++),
                    rs.getString("nama_template"),
                    ringkasTerapi,
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
            System.out.println("Notif tampil template_rencana_terapi_dokter: " + e);
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

            String isiAsli = Sequel.cariIsi("select rencana_terapi from template_rencana_terapi_dokter where no_template=?", no);
            txtRencanaTerapi.setText(isiAsli != null ? isiAsli : "");
        }
    }

    private void terapkanTemplateTerpilih() {
        int row = tbTemplate.getSelectedRow();
        String nama = txtNamaTemplate.getText().trim();
        String teksTerapi = txtRencanaTerapi.getText().trim();

        if (teksTerapi.isEmpty()) {
            if (row == -1 && tbTemplate.getRowCount() > 0) {
                row = 0;
                tbTemplate.setRowSelectionInterval(0, 0);
            }
            if (row != -1 && row < tabMode.getRowCount()) {
                String no = tabMode.getValueAt(row, 4).toString();
                nama = tabMode.getValueAt(row, 1).toString();
                teksTerapi = Sequel.cariIsi("select rencana_terapi from template_rencana_terapi_dokter where no_template=?", no);
            }
        } else {
            if (nama.isEmpty() && row != -1 && row < tabMode.getRowCount()) {
                nama = tabMode.getValueAt(row, 1).toString();
            } else if (nama.isEmpty()) {
                nama = "Template Rencana Terapi";
            }
        }

        if (teksTerapi == null || teksTerapi.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu template pada tabel atau isi teks template terlebih dahulu!", "Pemberitahuan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (listener != null) {
            listener.onTemplateDipilih(nama, teksTerapi);
        }
        dispose();
    }

    private void simpan() {
        if (txtNamaTemplate.getText().trim().isEmpty()) {
            Valid.textKosong(txtNamaTemplate, "Nama Template");
            return;
        }
        if (txtRencanaTerapi.getText().trim().isEmpty()) {
            Valid.textKosong(txtRencanaTerapi, "Rencana / Tindakan / Terapi");
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
                "insert into template_rencana_terapi_dokter(kd_dokter,nama_template,rencana_terapi) values(?,?,?)"
            );
            ps.setString(1, targetDokter);
            ps.setString(2, txtNamaTemplate.getText().trim());
            ps.setString(3, txtRencanaTerapi.getText().trim());
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, 
                "Template berhasil disimpan sebagai template " + (isUmum ? "Semua (Umum RS)" : (isDiri ? "Diri Sendiri" : "Pribadi Dokter")) + "!", 
                "Informasi", JOptionPane.INFORMATION_MESSAGE);
            kosongkan();
            tampil();
        } catch (Exception e) {
            System.out.println("Notif simpan template terapi: " + e);
            JOptionPane.showMessageDialog(this, "Gagal menyimpan template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ganti() {
        if (txtNoTemplate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih template yang ingin diubah terlebih dahulu!", "Pemberitahuan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txtNamaTemplate.getText().trim().isEmpty()) {
            Valid.textKosong(txtNamaTemplate, "Nama Template");
            return;
        }
        if (txtRencanaTerapi.getText().trim().isEmpty()) {
            Valid.textKosong(txtRencanaTerapi, "Rencana / Tindakan / Terapi");
            return;
        }

        String no = txtNoTemplate.getText().trim();
        String pemilikAsli = Sequel.cariIsi("select kd_dokter from template_rencana_terapi_dokter where no_template=?", no);

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
                "update template_rencana_terapi_dokter set kd_dokter=?, nama_template=?, rencana_terapi=? where no_template=?"
            );
            ps.setString(1, targetDokter);
            ps.setString(2, txtNamaTemplate.getText().trim());
            ps.setString(3, txtRencanaTerapi.getText().trim());
            ps.setString(4, no);
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, "Template berhasil diperbarui!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            kosongkan();
            tampil();
        } catch (Exception e) {
            System.out.println("Notif ganti template terapi: " + e);
            JOptionPane.showMessageDialog(this, "Gagal mengubah template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        if (txtNoTemplate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih template yang ingin dihapus pada tabel!", "Pemberitahuan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String no = txtNoTemplate.getText().trim();
        String pemilikAsli = Sequel.cariIsi("select kd_dokter from template_rencana_terapi_dokter where no_template=?", no);

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
            PreparedStatement ps = koneksi.prepareStatement("delete from template_rencana_terapi_dokter where no_template=?");
            ps.setString(1, no);
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, "Template berhasil dihapus!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            kosongkan();
            tampil();
        } catch (Exception e) {
            System.out.println("Notif hapus template terapi: " + e);
            JOptionPane.showMessageDialog(this, "Gagal menghapus template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kosongkan() {
        txtNoTemplate.setText("");
        txtNamaTemplate.setText("");
        txtRencanaTerapi.setText("");
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
            DlgMasterTemplateRencanaTerapi dialog = new DlgMasterTemplateRencanaTerapi(null, true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }
}
