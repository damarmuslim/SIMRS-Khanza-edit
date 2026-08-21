
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
        // Fase 2.5: setForeground/setBackground hardcode dihapus - biarkan
        // FlatLaf UIManager yang menentukan warna dasar komponen ini.
        setHorizontalAlignment(LEFT);
        setSize(WIDTH,23);
    }
}
