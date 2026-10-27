package simrskhanza.ui.theme;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Helper Styling Medical Teal untuk Form Pengkajian Awal Keperawatan IGD (RM 03-4 / 03-5)
 * RSUD Demang Sepulau Raya
 */
public class KhanzaMedicalTealTheme {

    // Palet Warna Medical Teal
    public static final Color MED_950 = new Color(4, 47, 46);      // #042f2e (Header Utama)
    public static final Color MED_700 = new Color(15, 118, 110);   // #0f766e (Tombol Utama / Badge)
    public static final Color MED_600 = new Color(13, 148, 136);   // #0d9488 (Border Aktif & Indikator)
    public static final Color MED_50  = new Color(230, 255, 250);  // #e6fffa (Background Saat Aktif)
    public static final Color SLATE_700 = new Color(51, 65, 85);   // #334155 (Teks Normal)
    public static final Color SLATE_300 = new Color(203, 213, 225);// #cbd5e1 (Border Normal)

    public static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_OPTION_NORMAL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_OPTION_ACTIVE = new Font("SansSerif", Font.BOLD, 12);

    /**
     * 1. Membuat Checkbox Card Berbingkai (Tinggi 42px, Kotak Centang 18x18px)
     */
    public static JCheckBox createCheckboxCard(String text) {
        JCheckBox chk = new JCheckBox(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean selected = isSelected();
                g2.setColor(selected ? MED_50 : Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 10, 10));

                g2.setStroke(new BasicStroke(selected ? 1.8f : 1.2f));
                g2.setColor(selected ? MED_600 : SLATE_300);
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 10, 10));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        chk.setOpaque(false);
        chk.setFocusPainted(false);
        chk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chk.setFont(FONT_OPTION_NORMAL);
        chk.setForeground(SLATE_700);
        chk.setBorder(new EmptyBorder(8, 12, 8, 14));
        chk.setPreferredSize(new Dimension(chk.getPreferredSize().width + 28, 42));
        chk.setMinimumSize(new Dimension(110, 42));

        // Custom Icon 18x18 px
        chk.setIcon(new CheckBoxIcon18(false));
        chk.setSelectedIcon(new CheckBoxIcon18(true));
        chk.setIconTextGap(10);

        chk.addItemListener(e -> {
            chk.setFont(chk.isSelected() ? FONT_OPTION_ACTIVE : FONT_OPTION_NORMAL);
            chk.setForeground(chk.isSelected() ? MED_950 : SLATE_700);
            chk.repaint();
        });
        return chk;
    }

    /**
     * 2. Membuat Radio Button Pill Berbingkai (Tinggi 40px, Radio 18x18px)
     */
    public static JRadioButton createRadioPill(String text) {
        JRadioButton rad = new JRadioButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean selected = isSelected();
                g2.setColor(selected ? MED_50 : Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 10, 10));

                g2.setStroke(new BasicStroke(selected ? 1.8f : 1.2f));
                g2.setColor(selected ? MED_600 : SLATE_300);
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 10, 10));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        rad.setOpaque(false);
        rad.setFocusPainted(false);
        rad.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rad.setFont(FONT_OPTION_NORMAL);
        rad.setForeground(SLATE_700);
        rad.setBorder(new EmptyBorder(8, 14, 8, 16));
        rad.setPreferredSize(new Dimension(rad.getPreferredSize().width + 30, 40));
        rad.setMinimumSize(new Dimension(90, 40));

        rad.setIcon(new RadioIcon18(false));
        rad.setSelectedIcon(new RadioIcon18(true));
        rad.setIconTextGap(10);

        rad.addItemListener(e -> {
            rad.setFont(rad.isSelected() ? FONT_OPTION_ACTIVE : FONT_OPTION_NORMAL);
            rad.setForeground(rad.isSelected() ? MED_950 : SLATE_700);
            rad.repaint();
        });
        return rad;
    }

    /**
     * 3. Fix Bug Mengecilnya Kotak Tindakan Penunjang (Laborat & Radiologi) & Textarea
     * Panggil fungsi ini pada JScrollPane Laborat, Radiologi, dan Tabel Masalah Keperawatan.
     */
    public static void fixScrollPaneHeight(JScrollPane scrollPane, int minHeightPx) {
        if (scrollPane == null) return;
        Dimension minDim = new Dimension(200, minHeightPx);
        Dimension prefDim = new Dimension(600, minHeightPx + 12);
        scrollPane.setMinimumSize(minDim);
        scrollPane.setPreferredSize(prefDim);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    // Ikon Checkbox 18x18 px
    public static class CheckBoxIcon18 implements Icon {
        private final boolean checked;
        public CheckBoxIcon18(boolean checked) { this.checked = checked; }
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (checked) {
                g2.setColor(MED_600);
                g2.fillRoundRect(x, y, 18, 18, 5, 5);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 4, y + 9, x + 8, y + 13);
                g2.drawLine(x + 8, y + 13, x + 14, y + 5);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, 18, 18, 5, 5);
                g2.setColor(new Color(148, 163, 184));
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawRoundRect(x + 1, y + 1, 16, 16, 5, 5);
            }
            g2.dispose();
        }
    }

    // Ikon Radio 18x18 px
    public static class RadioIcon18 implements Icon {
        private final boolean selected;
        public RadioIcon18(boolean selected) { this.selected = selected; }
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, 18, 18);
            if (selected) {
                g2.setColor(MED_600);
                g2.setStroke(new BasicStroke(5f));
                g2.drawOval(x + 2, y + 2, 14, 14);
            } else {
                g2.setColor(new Color(148, 163, 184));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawOval(x + 1, y + 1, 16, 16);
            }
            g2.dispose();
        }
    }
}

