/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package widget;

import javax.swing.JComboBox;

/**
 *
 * @author dosen3
 */
public final class ComboBox extends JComboBox {

    public ComboBox(){
        setFont(new java.awt.Font("Segoe UI", 0, 11));
        setBackground(java.awt.Color.WHITE);
        setOpaque(true);
        putClientProperty("FlatLaf.style", "background: #FFFFFF; disabledBackground: #FFFFFF; buttonBackground: #FFFFFF; editableBackground: #FFFFFF;");
        setSize(WIDTH,23);
    }
}
