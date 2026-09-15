
package widget;

import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author usu
 */
public class PasswordBox extends usu.widget.glass.PasswordBox {
    public PasswordBox() {
        super();
        setSelectionColor(Color.BLUE.brighter());
        setCaretColor(Color.red);
        setFont(getFont().deriveFont(Font.BOLD,12));
        setForeground(Color.WHITE);
        setHorizontalAlignment(LEFT);
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        Object arc = getClientProperty("Component.arc");
        Object roundRect = getClientProperty("JComponent.roundRect");
        Object modern = getClientProperty("ModernStyle.active");

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
    }
}
