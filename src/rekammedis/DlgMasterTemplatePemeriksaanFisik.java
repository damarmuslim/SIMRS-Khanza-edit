package rekammedis;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import kepegawaian.DlgCariDokter;
import widget.ThemeConfig;
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
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Master Template Pemeriksaan Fisik Dokter IGD / Poliklinik.
 * Mengadopsi desain visual modern Argon dengan ikon vektor resolusi tinggi (SVG-style),
 * sistem preset spesialisasi (Umum, Bedah, Anak, Obgyn), chip cepat, kustom tag dokter,
 * integrasi pencarian master dokter (DlgCariDokter), serta kepatuhan token ThemeConfig SIMRS Khanza.
 */
public final class DlgMasterTemplatePemeriksaanFisik extends JDialog {

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

            // Jika tidak ada di tabel dokter, cek apakah Admin Utama atau Pegawai
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
    public interface TemplateFisikListener {
        void onTemplateSelected(String nama, String teksFisik);
    }
    private TemplateFisikListener listener;

    // === KOMPONEN INPUT ===
    public final JTextField txtNoTemplate = new JTextField();
    public final JTextField txtNamaTemplate = new JTextField();
    public final JComboBox<String> cmbDokter = new JComboBox<>(new String[]{
        "Semua (Umum RS)", "Pribadi Dokter"
    });
    public final JButton btnCariDokter = new JButton();
    public final JComboBox<String> cmbPreset = new JComboBox<>(new String[]{
        "Umum / IGD (Head-to-Toe)", "Trauma / Bedah", "Pediatrik / Anak", "Obgyn / Kebidanan"
    });
    public final JTextArea txtPemeriksaanFisik = new JTextArea(5, 20);
    public final JTextField txtCari = new JTextField(24);

    // === TOMBOL AKSI MODERN ===
    public final JButton btnSimpan = new JButton("Simpan");
    public final JButton btnBaru = new JButton("Baru");
    public final JButton btnGanti = new JButton("Ganti");
    public final JButton btnUbah = btnGanti; // Kompatibilitas alias kode lama
    public final JButton btnHapus = new JButton("Hapus");
    public final JButton btnPilih = new JButton("Pilih / Terapkan");
    public final JButton btnCari = new JButton("Cari");
    public final JButton btnKeluar = new JButton("Keluar");

    public final JTable tbTemplate;

    // === CHIP PANEL & PREFERENSI ===
    private final Preferences prefsChip = Preferences.userNodeForPackage(DlgMasterTemplatePemeriksaanFisik.class);
    private final JPanel chipContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 5, 4));
    private final List<String[]> listCustomChips = new ArrayList<>();

    // === PALET WARNA ARGON MODERN TERSINKRON DENGAN THEME CONFIG ===
    private static final Color WARNA_HEADER     = new Color(15, 61, 62);    // #0F3D3E Deep Teal Elegan
    private static final Color WARNA_TEAL_MUDA   = new Color(128, 203, 196); // #80CBC4 Mint
    private static final Color BORDER_INPUT     = ThemeConfig.BORDER;       // #CBD5E1 / #D9E1E7
    private static final Color BORDER_HALUS     = ThemeConfig.BORDER_SOFT;  // #E4E6E2 Soft Border
    private static final Color BG_CARD          = ThemeConfig.ROW_ALT;      // #F8FAFC
    private static final Color TEKS_GELAP       = ThemeConfig.TEXT_NORMAL;  // #2D3748
    private static final Color TEKS_MUTED       = ThemeConfig.TEXT_MUTED;   // #718096
    private static final Color IKON_BIRU        = new Color(37, 99, 235);   // #2563EB
    private static final Color IKON_HIJAU       = ThemeConfig.OK;           // #16A34A
    private static final Color IKON_ORANYE      = ThemeConfig.WARN;         // #D97706 / #F59E0B
    private static final Color IKON_MERAH       = ThemeConfig.DANGER;       // #DC2626
    private static final Color IKON_SLATE       = ThemeConfig.TEXT_MUTED;   // #475569

    // =========================================================================
    // IKON VEKTOR MODERN ARGON (JAVA 2D RENDERED - CRISP, FLAT, & CLEAN)
    // =========================================================================
    public static class ArgonVectorIcon implements Icon {
        public enum Type { SIMPAN, BARU, UBAH, GANTI, HAPUS, TERAPKAN, CARI, KELUAR, CHIP_ADD, CHIP_SAVE, STAR, DOKTER }
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
                    // SVG: M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z
                    Path2D disk = new Path2D.Float();
                    disk.moveTo(s * 0.20f, s * 0.88f);
                    disk.lineTo(s * 0.80f, s * 0.88f);
                    disk.lineTo(s * 0.88f, s * 0.80f);
                    disk.lineTo(s * 0.88f, s * 0.32f);
                    disk.lineTo(s * 0.68f, s * 0.12f);
                    disk.lineTo(s * 0.20f, s * 0.12f);
                    disk.closePath();
                    g2.draw(disk);
                    // Shutter slider top
                    g2.draw(new Rectangle2D.Float(s * 0.34f, s * 0.12f, s * 0.32f, s * 0.28f));
                    // Label bottom
                    g2.draw(new Rectangle2D.Float(s * 0.30f, s * 0.54f, s * 0.40f, s * 0.34f));
                    break;

                case BARU:
                case CHIP_ADD:
                    // SVG: line x1="12" y1="5" x2="12" y2="19", line x1="5" y1="12" x2="19" y2="12"
                    g2.draw(new Line2D.Float(s * 0.50f, s * 0.18f, s * 0.50f, s * 0.82f));
                    g2.draw(new Line2D.Float(s * 0.18f, s * 0.50f, s * 0.82f, s * 0.50f));
                    break;

                case UBAH:
                case GANTI:
                    // SVG: path d="M12 20h9", path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"
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
                    // SVG: M3 6h18, M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6, M8 6V4...
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
                    // SVG: polyline points="20 6 9 17 4 12"
                    Path2D check = new Path2D.Float();
                    check.moveTo(s * 0.16f, s * 0.52f);
                    check.lineTo(s * 0.42f, s * 0.78f);
                    check.lineTo(s * 0.85f, s * 0.22f);
                    g2.draw(check);
                    break;

                case CARI:
                    // SVG: circle cx="11" cy="11" r="8", line x1="21" y1="21" x2="16.65" y2="16.65"
                    g2.draw(new Ellipse2D.Float(s * 0.15f, s * 0.15f, s * 0.52f, s * 0.52f));
                    g2.draw(new Line2D.Float(s * 0.54f, s * 0.54f, s * 0.86f, s * 0.86f));
                    break;

                case KELUAR:
                    // SVG: path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4", polyline...
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
                    // Bookmark / tag
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
    // WRAPLAYOUT: CHIP AKAN OTOMATIS TURUN KE BARIS BARU TANPA SALING TINDIH
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

    public DlgMasterTemplatePemeriksaanFisik(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Master Template Pemeriksaan Fisik Dokter");
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

        // 1. HEADER BAR ARGON
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WARNA_HEADER);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLeft.setOpaque(false);
        JLabel lblTitle = new JLabel("Master Template Pemeriksaan Fisik Dokter");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 13));

        JLabel lblBadge = new JLabel("Head-to-Toe");
        lblBadge.setOpaque(true);
        lblBadge.setBackground(new Color(27, 94, 95));
        lblBadge.setForeground(WARNA_TEAL_MUDA);
        lblBadge.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
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
        String[] kolom = {"No", "Nama Template", "Pemeriksaan Fisik (Head-to-Toe)", "Dokter / Kepemilikan", "no_template"};
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
                        return "<html><body style='width:380px; padding:6px; font-family:" + ThemeConfig.FONT_FAMILY + "; font-size:11px; color:#1E293B;'>"
                                + val.toString().replace("  |  ", "<br>").replace(" | ", "<br>")
                                + "</body></html>";
                    }
                }
                return super.getToolTipText(e);
            }
        };

        tbTemplate.setRowHeight(26);
        tbTemplate.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        tbTemplate.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbTemplate.setSelectionBackground(ThemeConfig.TABLE_SELECTION_BG);
        tbTemplate.setSelectionForeground(ThemeConfig.TABLE_SELECTION_TEXT);
        tbTemplate.setShowVerticalLines(true);
        tbTemplate.setGridColor(BORDER_HALUS);

        // Header Tabel Terang Netral sesuai standar SIMRS Khanza
        tbTemplate.getTableHeader().setBackground(ThemeConfig.TABLE_HEADER_BG);
        tbTemplate.getTableHeader().setForeground(ThemeConfig.TABLE_HEADER_TEXT);
        tbTemplate.getTableHeader().setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        tbTemplate.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConfig.BORDER_SOFT));
        tbTemplate.getTableHeader().setReorderingAllowed(false);

        // Lebar Kolom
        int[] lebarKolom = {45, 215, 510, 160};
        for (int i = 0; i < lebarKolom.length; i++) {
            TableColumn column = tbTemplate.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebarKolom[i]);
        }
        tbTemplate.getColumnModel().getColumn(0).setMinWidth(40);
        tbTemplate.getColumnModel().getColumn(0).setMaxWidth(60);

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

        // Renderer Standar Kolom 1 & 2
        DefaultTableCellRenderer normalRenderer = new DefaultTableCellRenderer() {
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
                return c;
            }
        };
        tbTemplate.getColumnModel().getColumn(1).setCellRenderer(normalRenderer);
        tbTemplate.getColumnModel().getColumn(2).setCellRenderer(normalRenderer);

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
                        c.setForeground(new Color(29, 78, 216)); // Biru #1D4ED8
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(ThemeConfig.TEXT_MUTED);
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

        // Shortcut ESC untuk menutup jendela
        getRootPane().registerKeyboardAction(e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Fokus awal
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                txtNamaTemplate.requestFocusInWindow();
            }
        });

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

        // Baris 1: Nama Template, Dokter/Kategori, & Preset Cepat
        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 12);

        txtNoTemplate.setVisible(false);
        styleInput(txtNamaTemplate);
        txtNamaTemplate.setDocument(new batasInput((byte) 100).getKata(txtNamaTemplate));
        txtNamaTemplate.addActionListener(e -> txtPemeriksaanFisik.requestFocusInWindow());
        
        cmbDokter.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        cmbDokter.setBackground(Color.WHITE);
        cmbDokter.addActionListener(e -> {
            if (isUpdatingCombo) return;
            Object sel = cmbDokter.getSelectedItem();
            if (sel == null) return;
            String s = sel.toString();

            if (s.contains("Pilih Dokter")) {
                SwingUtilities.invokeLater(() -> bukaCariDokter());
            } else if (s.startsWith("Diri Sendiri")) {
                // LANGSUNG BACA DARI LOGIN! TIDAK BUKA CARI DOKTER!
                deteksiDokterLogin();
                this.kodeDokterAktif = !this.kodeDokterLogin.isEmpty() ? this.kodeDokterLogin : "-";
                this.namaDokterAktif = this.namaDokterLogin;
                tampil();
            } else if (s.startsWith("Semua")) {
                this.kodeDokterAktif = "-";
                tampil();
            }
        });

        // Tombol Cari Dokter dari Master (kepegawaian.DlgCariDokter)
        btnCariDokter.setPreferredSize(new Dimension(30, 26));
        btnCariDokter.setBackground(Color.WHITE);
        btnCariDokter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCariDokter.setFocusPainted(false);
        btnCariDokter.setToolTipText("Pilih Dokter dari Master Dokter (kepegawaian.DlgCariDokter)");
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

        cmbPreset.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
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
        JLabel lblFisik = new JLabel("Pemeriksaan Fisik (Head-to-Toe) :");
        lblFisik.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        lblFisik.setForeground(TEKS_GELAP);

        JPanel chipTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        chipTools.setOpaque(false);

        JButton btnSimpanBlok = new JButton("Simpan Blok Teks Jadi Tag");
        btnSimpanBlok.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
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
        btnTagBaru.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
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

        headerArea.add(lblFisik, BorderLayout.WEST);
        headerArea.add(chipTools, BorderLayout.EAST);

        // Container Kotak Chip
        chipContainer.setOpaque(true);
        chipContainer.setBackground(Color.WHITE);
        chipContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_HALUS),
            new EmptyBorder(4, 6, 4, 6)
        ));

        // Area TextArea
        txtPemeriksaanFisik.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
        txtPemeriksaanFisik.setLineWrap(true);
        txtPemeriksaanFisik.setWrapStyleWord(true);
        txtPemeriksaanFisik.setMargin(new Insets(6, 8, 6, 8));

        JScrollPane scrollFisik = new JScrollPane(txtPemeriksaanFisik);
        scrollFisik.setBorder(BorderFactory.createLineBorder(BORDER_INPUT));
        scrollFisik.setPreferredSize(new Dimension(940, 110));

        // Hint bar
        JPanel hintBar = new JPanel(new BorderLayout());
        hintBar.setOpaque(false);
        JLabel lblHint1 = new JLabel("[i] Klik Kiri untuk menyisipkan ke teks. Klik Kanan pada tag untuk jadikan favorit / ubah / hapus.");
        lblHint1.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 10));
        lblHint1.setForeground(TEKS_MUTED);

        JLabel lblHint2 = new JLabel("Blok kalimat di kotak teks lalu klik 'Simpan Blok Teks Jadi Tag'.");
        lblHint2.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 10));
        lblHint2.setForeground(TEKS_MUTED);

        hintBar.add(lblHint1, BorderLayout.WEST);
        hintBar.add(lblHint2, BorderLayout.EAST);

        JPanel areaWrapper = new JPanel(new BorderLayout(0, 4));
        areaWrapper.setOpaque(false);
        areaWrapper.add(chipContainer, BorderLayout.NORTH);
        areaWrapper.add(scrollFisik, BorderLayout.CENTER);
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
        String saved = prefsChip.get("custom_chips_fisik_argon", "");
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
        prefsChip.put("custom_chips_fisik_argon", sb.toString());
    }

    private void renderChipPanel() {
        chipContainer.removeAll();
        String preset = (String) cmbPreset.getSelectedItem();
        if (preset == null) preset = "Umum / IGD (Head-to-Toe)";

        List<String[]> presetChips = new ArrayList<>();
        if (preset.startsWith("Umum")) {
            presetChips.add(new String[]{"+ Kepala", "KEPALA : Normocephal, deformitas (-)"});
            presetChips.add(new String[]{"+ Mata", "MATA : Konjungtiva anemis (-/-), sklera ikterik (-/-), pupil isokor Ø 3mm/3mm, RC (+/+)"});
            presetChips.add(new String[]{"+ THT/Leher", "THT/LEHER : Faring hiperemis (-), pembesaran KGB (-), deviasi trakea (-), JVP normal"});
            presetChips.add(new String[]{"+ Thorax Normal", "THORAX :\n  - Cor : BJ I-II murni reguler, murmur (-), gallop (-)\n  - Pulmo : Suara napas vesikuler (+/+), ronkhi (-/-), wheezing (-/-)"});
            presetChips.add(new String[]{"+ Abdomen Normal", "ABDOMEN : Datar, supel, bising usus (+) normal, nyeri tekan (-)"});
            presetChips.add(new String[]{"+ Ekstremitas", "EKSTREMITAS : Akral hangat, CRT < 2 detik, edema (-/-/-/-)"});
            presetChips.add(new String[]{"+ Status Lokalis", "STATUS LOKALIS (Regio ...) :\n- Look : \n- Feel : \n- Move : "});
            presetChips.add(new String[]{"[Reset] Format Kosong", "KEPALA : \nMATA : \nTHT/LEHER : \nTHORAX : \nABDOMEN : \nEKSTREMITAS : "});
        } else if (preset.startsWith("Trauma")) {
            presetChips.add(new String[]{"+ Primary Survey", "AIRWAY : Clear\nBREATHING : Spontan, simetris\nCIRCULATION : Akral hangat, nadi kuat\nDISABILITY : GCS E4M6V5, pupil isokor\nEXPOSURE : Jejas tertera di status lokalis"});
            presetChips.add(new String[]{"+ Status Lokalis (L-F-M)", "STATUS LOKALIS (Regio ...) :\n- Look : Deformitas (-), vulnus (-), hematoma (-)\n- Feel : Nyeri tekan (-), krepitasi (-), pulsasi distal kuat\n- Move : ROM aktif/pasif bebas"});
            presetChips.add(new String[]{"+ Trauma Kepala", "KEPALA : Hematoma (-), vulnus (-), battle sign (-), raccoon eyes (-), otorrhea/rhinorrhea (-)"});
            presetChips.add(new String[]{"+ Thorax Trauma", "THORAX : Simetris, jejas (-), krepitasi (-), suara napas vesikuler (+/+), sonor (+/+)"});
            presetChips.add(new String[]{"+ Pelvis & Ekstremitas", "PELVIS : Stabilitas pelvis intak, krepitasi (-)\nEKSTREMITAS : Fraktur (-), akral hangat, pulsasi distal teraba, CRT < 2 detik"});
        } else if (preset.startsWith("Pediatrik")) {
            presetChips.add(new String[]{"+ Keadaan Anak", "KEADAAN UMUM : Tampak sakit ringan/sedang, menangis kuat, tonus aktif, reaktif"});
            presetChips.add(new String[]{"+ Ubun-ubun & Mata", "KEPALA/MATA : Ubun-ubun besar datar/tidak cekung, mata cekung (-/-), air mata (+/+)"});
            presetChips.add(new String[]{"+ Thorax Anak", "THORAX : Pernapasan cuping hidung (-), retraksi dada (-), cor BJ I-II reguler, pulmo vesikuler (+/+), ronkhi (-/-)"});
            presetChips.add(new String[]{"+ Turgor Kulit", "ABDOMEN : Supel, turgor kulit kembali cepat (< 2 detik), meteorismus (-)"});
            presetChips.add(new String[]{"+ Ekstremitas Anak", "EKSTREMITAS : Akral hangat, CRT < 2 detik, sianosis (-)"});
        } else if (preset.startsWith("Obgyn")) {
            presetChips.add(new String[]{"+ Obstetri Luar (TFU/DJJ)", "ABDOMEN/OBSTETRI : TFU ... cm, punggung ..., presentasi kepala, DJJ ... x/m reguler, His ...x/10 menit durasi ... detik"});
            presetChips.add(new String[]{"+ Pemeriksaan Dalam (VT)", "VT (VAGINAL TOUCHER) : Portio lunak/tebal, pembukaan ... cm, ketuban (+/-), presentasi kepala Hodge ..., pelepasan lendir darah (+/-)"});
            presetChips.add(new String[]{"+ Inspekulo", "INSPEKULO : Fluksus (-), fluor (-), erosi (-), massa (-), ostium tertutup"});
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
        btn.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        btn.setBackground(new Color(248, 250, 252));
        btn.setForeground(new Color(51, 65, 85));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_INPUT),
            new EmptyBorder(2, 7, 2, 7)
        ));
        btn.setToolTipText("<html><b>" + label + "</b><br><b>Klik Kiri:</b> Sisipkan ke teks<hr>" + isiTeks.replace("\n", "<br>") + "</html>");
        btn.addActionListener(e -> sisipkanTeksKeFisik(isiTeks));
        return btn;
    }

    private JButton buatTombolChipCustom(String label, String isiTeks, boolean isFav, int index) {
        String display = label;
        if (display.length() > 30) {
            display = display.substring(0, 27) + "...";
        }
        JButton btn = new JButton(display);
        btn.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isFav) {
            btn.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.STAR, 11, new Color(245, 158, 11)));
            btn.setBackground(new Color(239, 246, 255)); // #EFF6FF biru lembut
            btn.setForeground(new Color(29, 78, 216));   // #1D4ED8 biru tegas
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

        btn.addActionListener(e -> sisipkanTeksKeFisik(isiTeks));

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
        mnEdit.setIcon(new ArgonVectorIcon(ArgonVectorIcon.Type.GANTI, 12, IKON_ORANYE));
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

    private void sisipkanTeksKeFisik(String teks) {
        String current = txtPemeriksaanFisik.getText().trim();
        txtPemeriksaanFisik.setText((current.isEmpty() ? "" : current + "\n") + teks);
        txtPemeriksaanFisik.requestFocusInWindow();
        txtPemeriksaanFisik.setCaretPosition(txtPemeriksaanFisik.getText().length());
    }

    private void simpanBlokTeksKeChip() {
        String blok = txtPemeriksaanFisik.getSelectedText();
        if (blok == null || blok.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Silakan blok/seleksi kalimat pada kotak Pemeriksaan Fisik yang ingin dijadikan tombol pintasan!",
                "Petunjuk", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String namaDefault = "+ " + (blok.length() > 20 ? blok.substring(0, 20).trim() + "..." : blok.trim());
        JTextField inpJudul = new JTextField(namaDefault);
        JCheckBox chkFavorit = new JCheckBox("Jadikan Tag Favorit (Warna Biru & Icon Bintang)");
        chkFavorit.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
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

        String blokTeks = txtPemeriksaanFisik.getSelectedText();
        if (editIndex >= 0) {
            inpIsi.setText(listCustomChips.get(editIndex)[1]);
        } else if (blokTeks != null && !blokTeks.trim().isEmpty()) {
            inpIsi.setText(blokTeks.trim());
        }

        JCheckBox chkFavorit = new JCheckBox("Jadikan Tag Favorit (Warna Biru & Icon Bintang)");
        chkFavorit.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        chkFavorit.setForeground(new Color(29, 78, 216));
        chkFavorit.setOpaque(false);
        boolean currentFav = editIndex >= 0 && listCustomChips.get(editIndex).length >= 3 && "1".equals(listCustomChips.get(editIndex)[2]);
        chkFavorit.setSelected(currentFav);

        JPanel panelPop = new JPanel(new BorderLayout(0, 8));
        panelPop.add(bungkusLabel("Nama Tombol Tag (Contoh: + Status Lokalis) :", inpJudul), BorderLayout.NORTH);
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

        // Ikon Vektor Argon: Clean, Elegan, dan Berwarna Semantis
        styleCleanButton(btnSimpan, new ArgonVectorIcon(ArgonVectorIcon.Type.SIMPAN, 15, IKON_BIRU), TEKS_GELAP);
        styleCleanButton(btnBaru,   new ArgonVectorIcon(ArgonVectorIcon.Type.BARU,   15, IKON_HIJAU), TEKS_GELAP);
        styleCleanButton(btnGanti,  new ArgonVectorIcon(ArgonVectorIcon.Type.GANTI,  15, IKON_ORANYE), TEKS_GELAP);
        styleCleanButton(btnHapus,  new ArgonVectorIcon(ArgonVectorIcon.Type.HAPUS,  15, IKON_MERAH), ThemeConfig.DANGER);
        stylePrimaryButton(btnPilih, new ArgonVectorIcon(ArgonVectorIcon.Type.TERAPKAN, 15, WARNA_TEAL_MUDA));

        btnSimpan.setMnemonic(KeyEvent.VK_S);
        btnBaru.setMnemonic(KeyEvent.VK_B);
        btnGanti.setMnemonic(KeyEvent.VK_G);
        btnHapus.setMnemonic(KeyEvent.VK_H);
        btnPilih.setMnemonic(KeyEvent.VK_P);

        btnSimpan.setToolTipText("Simpan template baru (Alt+S)");
        btnBaru.setToolTipText("Kosongkan isian untuk membuat template baru (Alt+B)");
        btnGanti.setToolTipText("Perbarui template terpilih pada tabel (Alt+G)");
        btnHapus.setToolTipText("Hapus template terpilih pada tabel (Alt+H)");
        btnPilih.setToolTipText("Pilih dan terapkan template ke rekam medis pasien (Alt+P / Enter / Dobel Klik)");

        btnSimpan.addActionListener(e -> simpan());
        btnGanti.addActionListener(e -> ganti());
        btnHapus.addActionListener(e -> hapus());
        btnBaru.addActionListener(e -> kosongkan());
        btnPilih.addActionListener(e -> terapkanTemplateTerpilih());

        leftBtns.add(btnSimpan);
        leftBtns.add(btnBaru);
        leftBtns.add(btnGanti);
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
        lblCari.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        lblCari.setForeground(TEKS_GELAP);

        styleInput(txtCari);
        txtCari.setToolTipText("Ketik kata kunci nama atau isi pemeriksaan fisik lalu tekan Enter...");
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

        styleCleanButton(btnKeluar, new ArgonVectorIcon(ArgonVectorIcon.Type.KELUAR, 15, IKON_MERAH), ThemeConfig.DANGER);
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
        l.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        l.setForeground(TEKS_GELAP);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void styleInput(JTextField tf) {
        tf.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
        tf.setForeground(TEKS_GELAP);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_INPUT),
            new EmptyBorder(5, 8, 5, 8)
        ));
    }

    private void styleCleanButton(JButton btn, Icon icon, Color textColor) {
        btn.setIcon(icon);
        btn.setIconTextGap(7);
        btn.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
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
        btn.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
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

            int count = Sequel.cariInteger("select count(*) from template_pemeriksaan_fisik_dokter");
            if (count == 0) {
                simpanTemplateAwal("Normal (Dewasa)",
                    "KEPALA : Normocephal, deformitas (-)\n" +
                    "MATA : Konjungtiva anemis (-/-), sklera ikterik (-/-), pupil isokor Ø 3mm/3mm, RC (+/+)\n" +
                    "THT : Sekret (-/-), perdarahan (-/-), deviasi septum (-)\n" +
                    "MULUT : Mukosa bibir lembab, sianosis (-)\n" +
                    "LEHER : Deviasi trakea (-), pembesaran KGB (-), JVP normal, tiroid tidak teraba\n" +
                    "THORAX :\n" +
                    "  - Cor : BJ I-II murni reguler, murmur (-), gallop (-)\n" +
                    "  - Pulmo : Suara napas vesikuler (+/+), ronkhi (-/-), wheezing (-/-)\n" +
                    "ABDOMEN : Datar, supel, bising usus (+) normal, nyeri tekan (-), timpani\n" +
                    "EKSTREMITAS : Akral hangat, CRT < 2 detik, edema (-/-/-/-)");

                simpanTemplateAwal("Kasus Trauma / Bedah",
                    "KEPALA : Jejas (-), hematoma (-), deformitas (-)\n" +
                    "MATA : Pupil isokor Ø 3mm/3mm, RC (+/+), racoon eyes (-)\n" +
                    "THT : Otorrhea (-), rhinorrhea (-), battle sign (-)\n" +
                    "LEHER : Jejas (-), deviasi trakea (-), hematoma (-), nyeri tekan (-)\n" +
                    "THORAX : Bentuk simetris, jejas (-), krepitasi (-), vesikuler (+/+), ronkhi (-/-)\n" +
                    "ABDOMEN : Datar, supel, jejas (-), defans muskular (-), nyeri tekan (-)\n" +
                    "PELVIS : Stabilitas pelvis intak, krepitasi (-)\n" +
                    "EKSTREMITAS : Deformitas (-), fraktur (-), akral hangat, pulsasi distal kuat, CRT < 2 detik");

                simpanTemplateAwal("Pediatrik / Anak",
                    "KEPALA : Normocephal, ubun-ubun besar datar/tidak cekung\n" +
                    "MATA : Konjungtiva anemis (-/-), sklera ikterik (-/-), mata cekung (-/-), air mata (+/+)\n" +
                    "THT : Sekret (-/-), pernapasan cuping hidung (-)\n" +
                    "MULUT : Mukosa bibir lembab, sianosis (-)\n" +
                    "LEHER : Pembesaran KGB (-), retraksi suprasternal (-)\n" +
                    "THORAX : Simetris, retraksi dinding dada (-), cor BJ I-II reguler, pulmo vesikuler (+/+), ronkhi (-/-), wheezing (-/-)\n" +
                    "ABDOMEN : Supel, turgor kulit kembali cepat, bising usus (+) normal, meteorismus (-)\n" +
                    "EKSTREMITAS : Akral hangat, CRT < 2 detik, sianosis (-)");

                simpanTemplateAwal("Format Kosong (Head-to-Toe)",
                    "KEPALA : \n" +
                    "MATA : \n" +
                    "THT : \n" +
                    "MULUT : \n" +
                    "LEHER : \n" +
                    "THORAX :\n" +
                    "  - Cor : \n" +
                    "  - Pulmo : \n" +
                    "ABDOMEN : \n" +
                    "EKSTREMITAS : ");

                simpanTemplateAwal("Kardiovaskular (Jantung)",
                    "KEPALA/LEHER : Sianosis (-), pucat (-), JVP normal/tidak meningkat\n" +
                    "THORAX/COR :\n" +
                    "  - Inspeksi : Iktus kordis tidak tampak\n" +
                    "  - Palpasi : Iktus kordis teraba di ICS V linea midklavikularis sinistra, thrill (-)\n" +
                    "  - Perkusi : Batas jantung dalam batas normal\n" +
                    "  - Auskultasi : BJ I-II murni reguler, murmur (-), gallop (-)\n" +
                    "PULMO : Suara napas vesikuler (+/+), ronkhi basah halus (-/-), wheezing (-/-)\n" +
                    "ABDOMEN : Supel, bising usus normal, hepatomegali (-), hepatojugular reflux (-)\n" +
                    "EKSTREMITAS : Akral hangat, CRT < 2 detik, edema pretibial (-/-)");

                simpanTemplateAwal("Respirasi (Paru)",
                    "KEPALA/LEHER : Pernapasan cuping hidung (-), deviasi trakea (-), retraksi otot napas (-)\n" +
                    "THORAX/PULMO :\n" +
                    "  - Inspeksi : Simetris statis dan dinamis, sela iga tidak melebar\n" +
                    "  - Palpasi : Fremitus taktil kanan = kiri, krepitasi (-)\n" +
                    "  - Perkusi : Sonor pada kedua hemitoraks\n" +
                    "  - Auskultasi : Suara napas vesikuler (+/+), ronkhi (-/-), wheezing (-/-)\n" +
                    "COR : BJ I-II reguler, bising (-)\n" +
                    "ABDOMEN : Datar, supel, bising usus (+) normal\n" +
                    "EKSTREMITAS : Sianosis perifer (-), akral hangat, CRT < 2 detik");
            }
        } catch (Exception e) {
            System.out.println("Notif init skema template_pemeriksaan_fisik_dokter: " + e);
        }
    }

    private void simpanTemplateAwal(String nama, String isi) {
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "insert into template_pemeriksaan_fisik_dokter(kd_dokter,nama_template,pemeriksaan_fisik) values('-' ,?,?)"
            );
            ps.setString(1, nama);
            ps.setString(2, isi);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            System.out.println("Notif seed template fisik: " + e);
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

    public void setListener(TemplateFisikListener listener) {
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
            String sql = "select no_template, nama_template, pemeriksaan_fisik, kd_dokter " +
                         "from template_pemeriksaan_fisik_dokter where (kd_dokter=? or kd_dokter='-') " +
                         (cari.isEmpty() ? "" : "and (nama_template like ? or pemeriksaan_fisik like ?) ") +
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
                String ringkasFisik = rs.getString("pemeriksaan_fisik").replace("\n", "  |  ");
                if (!selectedNoSebelumnya.isEmpty() && selectedNoSebelumnya.equals(noTemp)) {
                    rowToSelect = tabMode.getRowCount();
                }
                tabMode.addRow(new Object[]{
                    String.valueOf(noUrut++),
                    rs.getString("nama_template"),
                    ringkasFisik,
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
            System.out.println("Notif tampil template_pemeriksaan_fisik_dokter: " + e);
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

            // Ambil teks lengkap asli dari database agar indentasi & newline tidak hilang
            String isiAsli = Sequel.cariIsi("select pemeriksaan_fisik from template_pemeriksaan_fisik_dokter where no_template=?", no);
            txtPemeriksaanFisik.setText(isiAsli != null ? isiAsli : "");
        }
    }

    private void terapkanTemplateTerpilih() {
        int row = tbTemplate.getSelectedRow();
        String nama = txtNamaTemplate.getText().trim();
        String teksFisik = txtPemeriksaanFisik.getText().trim();

        if (teksFisik.isEmpty()) {
            if (row == -1 && tbTemplate.getRowCount() > 0) {
                row = 0;
                tbTemplate.setRowSelectionInterval(0, 0);
            }
            if (row != -1 && row < tabMode.getRowCount()) {
                String no = tabMode.getValueAt(row, 4).toString();
                nama = tabMode.getValueAt(row, 1).toString();
                teksFisik = Sequel.cariIsi("select pemeriksaan_fisik from template_pemeriksaan_fisik_dokter where no_template=?", no);
            }
        } else {
            if (nama.isEmpty() && row != -1 && row < tabMode.getRowCount()) {
                nama = tabMode.getValueAt(row, 1).toString();
            } else if (nama.isEmpty()) {
                nama = "Template Pemeriksaan Fisik";
            }
        }

        if (teksFisik == null || teksFisik.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu template pada tabel atau isi teks template terlebih dahulu!", "Pemberitahuan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (listener != null) {
            listener.onTemplateSelected(nama, teksFisik);
        }
        dispose();
    }

    private void simpan() {
        if (txtNamaTemplate.getText().trim().isEmpty()) {
            Valid.textKosong(txtNamaTemplate, "Nama Template");
            return;
        }
        if (txtPemeriksaanFisik.getText().trim().isEmpty()) {
            Valid.textKosong(txtPemeriksaanFisik, "Pemeriksaan Fisik");
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
                "insert into template_pemeriksaan_fisik_dokter(kd_dokter,nama_template,pemeriksaan_fisik) values(?,?,?)"
            );
            ps.setString(1, targetDokter);
            ps.setString(2, txtNamaTemplate.getText().trim());
            ps.setString(3, txtPemeriksaanFisik.getText().trim());
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, 
                "Template berhasil disimpan sebagai template " + (isUmum ? "Semua (Umum RS)" : (isDiri ? "Diri Sendiri" : "Pribadi Dokter")) + "!", 
                "Informasi", JOptionPane.INFORMATION_MESSAGE);
            kosongkan();
            tampil();
        } catch (Exception e) {
            System.out.println("Notif simpan template: " + e);
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
        if (txtPemeriksaanFisik.getText().trim().isEmpty()) {
            Valid.textKosong(txtPemeriksaanFisik, "Pemeriksaan Fisik");
            return;
        }

        String no = txtNoTemplate.getText().trim();
        String pemilikAsli = Sequel.cariIsi("select kd_dokter from template_pemeriksaan_fisik_dokter where no_template=?", no);

        // Proteksi jika dokter biasa ingin mengubah template umum RS
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
                "update template_pemeriksaan_fisik_dokter set kd_dokter=?, nama_template=?, pemeriksaan_fisik=? where no_template=?"
            );
            ps.setString(1, targetDokter);
            ps.setString(2, txtNamaTemplate.getText().trim());
            ps.setString(3, txtPemeriksaanFisik.getText().trim());
            ps.setString(4, no);
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, "Template berhasil diperbarui!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            kosongkan();
            tampil();
        } catch (Exception e) {
            System.out.println("Notif ganti template: " + e);
            JOptionPane.showMessageDialog(this, "Gagal mengubah template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        if (txtNoTemplate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih template yang ingin dihapus pada tabel!", "Pemberitahuan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String no = txtNoTemplate.getText().trim();
        String pemilikAsli = Sequel.cariIsi("select kd_dokter from template_pemeriksaan_fisik_dokter where no_template=?", no);

        // Jika template umum RS, berikan konfirmasi keamanan ekstra
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
            PreparedStatement ps = koneksi.prepareStatement("delete from template_pemeriksaan_fisik_dokter where no_template=?");
            ps.setString(1, no);
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(this, "Template berhasil dihapus!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            kosongkan();
            tampil();
        } catch (Exception e) {
            System.out.println("Notif hapus template: " + e);
            JOptionPane.showMessageDialog(this, "Gagal menghapus template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kosongkan() {
        txtNoTemplate.setText("");
        txtNamaTemplate.setText("");
        txtPemeriksaanFisik.setText("");
        // Jika dokter aktif terdeteksi, default ke Diri Sendiri
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
            DlgMasterTemplatePemeriksaanFisik dialog = new DlgMasterTemplatePemeriksaanFisik(null, true);
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
