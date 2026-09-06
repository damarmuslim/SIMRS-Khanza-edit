
package widget;

import java.awt.Color;
import static java.awt.image.ImageObserver.WIDTH;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author usu
 */
public class TextArea extends JTextArea {
    public TextArea() {
        super();
        setOpaque(false);
        setLineWrap(true);
        setWrapStyleWord(true);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setFont(new java.awt.Font("Segoe UI", 0, 11));        
        setSelectionColor(new Color(255,255,255));
        setSelectedTextColor(new Color(255,0,0));
        setForeground(new Color(50,50,50));
        setSize(WIDTH,23);
        setSize(WIDTH,23);

        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
        });
    }

    private String placeholder = "";

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        String ph = placeholder;
        if (ph == null || ph.isEmpty()) {
            Object prop = getClientProperty("JTextField.placeholderText");
            if (prop == null) { prop = getClientProperty("placeholder"); }
            if (prop != null) { ph = prop.toString(); }
        }
        if (ph != null && !ph.isEmpty() && getText().isEmpty()) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new java.awt.Color(160, 160, 160));
            g2.setFont(getFont());
            java.awt.Insets in = getInsets();
            java.awt.FontMetrics fm = g2.getFontMetrics();
            g2.drawString(ph, in.left + 2, in.top + fm.getAscent());
            g2.dispose();
        }
    }
}
