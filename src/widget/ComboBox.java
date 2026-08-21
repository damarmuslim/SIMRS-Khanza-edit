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
        // Fase 2.5: setBackground/setForeground hardcode dihapus - biarkan
        // FlatLaf UIManager yang menentukan warna dasar komponen ini.
        setSize(WIDTH,23);
    }
}
