package widget;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * ModernStyle â memodernkan tampilan seluruh aplikasi.
 *
 * TIDAK menyentuh layout (gridx, gridy, weightx, setBounds).
 * Hanya mengubah: border, warna, dan font.
 *
 * CARA PAKAI:
 *   Di kelas main aplikasi, SEBELUM form pertama dibuat:
 *       ModernStyle.pasang();
 *
 *   Itu saja. Semua form ikut berubah otomatis.
 *
 * MEMBATALKAN:
 *   Beri komentar pada baris ModernStyle.pasang(), jalankan ulang.
 *
 * Fase 2.5 (konsolidasi tema): widget.ThemeConfig sekarang jadi Single
 * Source of Truth untuk warna/font - palet di kelas ini di atas cuma
 * delegasi ke ThemeConfig, tidak lagi hex literal terpisah. Kelas ini
 * TIDAK dihapus krn RMPenilaianAwalKeperawatanIGD.java (form klinis,
 * di luar batas task ini) masih memanggil ModernStyle.terapkan(this).
 */
public class ModernStyle {

    // ---------- PALET (Navy/Teal, design system SIMRS Khanza) ----------
    // Fase 2.5: konsolidasi tema - nilai diambil dari widget.ThemeConfig
    // (Single Source of Truth), bukan lagi hex literal terpisah di sini.
    public static final Color BG      = ThemeConfig.BACKGROUND;   // Background
    public static final Color CARD    = ThemeConfig.SURFACE;      // Surface
    public static final Color TEKS    = ThemeConfig.TEXT_NORMAL;  // Text Primary
    public static final Color LABEL   = ThemeConfig.TEXT_MUTED;   // Text Secondary
    public static final Color GARIS   = ThemeConfig.BORDER;       // Border
    public static final Color AKSEN   = ThemeConfig.PRIMARY;      // Primary Teal
    public static final Color AKSEN_HOVER = new Color(0x0A, 0x9B, 0xA8); // Teal Hover (belum ada padanan di ThemeConfig)
    public static final Color HEADING = ThemeConfig.NAVY;         // Primary Navy
    public static final Color SUKSES  = new Color(0x2E, 0x9B, 0x68);
    public static final Color WARNING = new Color(0xE6, 0xA2, 0x3C);
    public static final Color DANGER  = new Color(0xD9, 0x53, 0x4F);

    public static final Font F_LABEL = ThemeConfig.FONT_NORMAL;
    public static final Font F_INPUT = ThemeConfig.FONT_NORMAL;

    /** Panggil sekali di awal aplikasi. */
    public static void pasang() {
        UIManager.put("text", TEKS);

        UIManager.put("Label.font", F_LABEL);
        UIManager.put("Label.foreground", LABEL);
        UIManager.put("TextField.font", F_INPUT);
        UIManager.put("TextArea.font", F_INPUT);
        UIManager.put("ComboBox.font", F_INPUT);
        UIManager.put("CheckBox.font", F_LABEL);
        UIManager.put("RadioButton.font", F_LABEL);
        UIManager.put("Button.font", F_LABEL);
        UIManager.put("Table.font", F_INPUT);
        UIManager.put("TableHeader.font",
                new Font("Segoe UI Semibold", Font.PLAIN, 12));
    }

    /**
     * Opsional: dipanggil di konstruktor form, setelah initComponents().
     * Menelusuri semua komponen dan menerapkan gaya modern.
     *
     * Contoh:
     *     public FormPasien() {
     *         initComponents();
     *         ModernStyle.terapkan(this);
     *     }
     */
    public static void terapkan(Container root) {
        for (Component c : root.getComponents()) {

            if (c instanceof JTextField) {
                JTextField t = (JTextField) c;
                t.setBorder(new UnderlineBorder());
                t.setBackground(CARD);
                t.setForeground(TEKS);
                t.setFont(F_INPUT);

            } else if (c instanceof JTextArea) {
                JTextArea t = (JTextArea) c;
                t.setForeground(TEKS);
                t.setFont(F_INPUT);
                t.setBorder(new EmptyBorder(6, 8, 6, 8));

            } else if (c instanceof JScrollPane) {
                JScrollPane s = (JScrollPane) c;
                s.setBorder(new LineBorder(GARIS, 1, true));
                s.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                s.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            } else if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                l.setFont(F_LABEL);
                // heading dikenali dari huruf kapital semua
                String s = l.getText();
                if (s != null && !s.isEmpty()
                        && s.equals(s.toUpperCase())
                        && s.length() > 3) {
                    l.setForeground(HEADING);
                    l.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
                } else {
                    l.setForeground(LABEL);   // SATU warna untuk semua label
                }

            } else if (c instanceof JCheckBox || c instanceof JRadioButton) {
                AbstractButton b = (AbstractButton) c;
                b.setFont(F_LABEL);
                b.setForeground(TEKS);
                b.setOpaque(false);

            } else if (c instanceof JComboBox) {
                ((JComboBox<?>) c).setFont(F_INPUT);

            } else if (c instanceof JPanel) {
                Border br = ((JPanel) c).getBorder();
                if (br instanceof TitledBorder) {
                    TitledBorder tb = (TitledBorder) br;
                    tb.setTitleFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
                    tb.setTitleColor(HEADING);
                    tb.setBorder(new LineBorder(GARIS, 1, true));
                }
            }

            // telusuri anak komponen
            if (c instanceof Container) {
                terapkan((Container) c);
            }
        }
    }

    /** Border garis bawah; menebal dan berwarna aksen saat difokus. */
    public static class UnderlineBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics gr,
                                int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) gr.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            boolean fokus = c.hasFocus();
            g2.setColor(fokus ? AKSEN : GARIS);
            g2.setStroke(new BasicStroke(fokus ? 2f : 1f));
            g2.drawLine(x + 2, y + h - 2, x + w - 3, y + h - 2);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 6, 6, 6);
        }
    }
}
