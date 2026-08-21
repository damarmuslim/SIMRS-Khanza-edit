/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package widget;

import javax.swing.JCheckBox;

/**
 *
 * @author dosen3
 */
public class CekBox extends JCheckBox{

    private static final long serialVersionUID = 1L;

    public CekBox(){
        super();
        setFont(new java.awt.Font("Segoe UI", 0, 11));
        setFocusPainted(false);
        // Fase 2.5: setBackground/setForeground/setBorder hardcode dihapus -
        // biarkan FlatLaf UIManager yang menentukan warna & border dasar
        // komponen ini.
        setOpaque(true);
        setSize(WIDTH,23);
    }
}
