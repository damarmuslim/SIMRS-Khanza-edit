
package widget;

import java.awt.Color;
import usu.widget.glass.TextBoxGlass;

/**
 *
 * @author usu
 */
public class TextBox extends TextBoxGlass {
    public TextBox() {
        super();
        setFont(new java.awt.Font("Segoe UI", 0, 11));
        setSelectionColor(new Color(255,252,252));
        setSelectedTextColor(new Color(255,0,0));
        setOpaque(true);
        setBackground(Color.WHITE);
        setHorizontalAlignment(LEFT);
        setSize(WIDTH,23);
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        Object arc = getClientProperty("Component.arc");
        Object roundRect = getClientProperty("JComponent.roundRect");
        Object modern = getClientProperty("ModernStyle.active");

        // Option 2: Jika tidak secara eksplisit meminta modern/arc/roundRect,
        // kembalikan ke lukisan asli TextBoxGlass (supaya form Khanza lain tetap berujung bulat/glass bawaan).
        if (arc == null && roundRect == null && !Boolean.TRUE.equals(modern)) {
            super.paintComponent(g);
            return;
        }

        int r = 0;
        if (arc instanceof Number) {
            r = ((Number) arc).intValue();
        } else if (Boolean.TRUE.equals(roundRect)) {
            r = 6;
        }

        if (Boolean.FALSE.equals(roundRect) || r == 0) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            try {
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
            } finally {
                g2.dispose();
            }
        }
        if (ui != null) {
            java.awt.Graphics g2 = g.create();
            try {
                ui.paint(g2, this);
            } finally {
                g2.dispose();
            }
        }
        boolean isFlatLaf = (ui != null && ui.getClass().getName().toLowerCase().contains("flat"))
                || (javax.swing.UIManager.getLookAndFeel() != null && javax.swing.UIManager.getLookAndFeel().getClass().getName().toLowerCase().contains("flat"));
        String ph = placeholder;
        if (ph == null || ph.isEmpty()) {
            Object prop = getClientProperty("JTextField.placeholderText");
            if (prop == null) { prop = getClientProperty("placeholder"); }
            if (prop != null) { ph = prop.toString(); }
        }
        if (!isFlatLaf && ph != null && !ph.isEmpty() && (getText() == null || getText().isEmpty())) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            try {
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(175, 175, 175));
                g2.setFont(getFont());
                java.awt.Insets in = getInsets();
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int x = (in != null ? in.left : 4) + 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(ph, x, y);
            } finally {
                g2.dispose();
            }
        }
    }

    private String placeholder = "";

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        putClientProperty("JTextField.placeholderText", placeholder);
        repaint();
    }

    public String getPlaceholder() {
        return this.placeholder;
    }
}
