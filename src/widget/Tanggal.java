/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JScrollBar;
import javax.swing.plaf.metal.MetalScrollBarUI;
import uz.ncipro.calendar.JDateTimePicker;

/**
 *
 * @author khanzasoft
 */
public final class Tanggal extends JDateTimePicker {
    public Tanggal(){
        super();
        //setBackground(new Color(245,160,245));
        //setForeground(new Color(90,90,90));
        setForeground(new Color(50,50,50));
        setBackground(new Color(255,255,255));
        setFont(new java.awt.Font("Tahoma", 0, 11));
        //setBorder(javax.swing.BorderFactory.createLineBorder(new Color(212,212,152)));
        setSize(WIDTH,23);
        paksaScrollBarMetal(popupEditor);
    }

    /**
     * JMonthChooser/JYearChooser (kelas internal privat di uz.ncipro.calendar.
     * JCalendar, tidak bisa direferensikan langsung dari sini) sebenarnya
     * JSpinField: JTextField + JScrollBar sbg tombol naik/turun. Di bawah
     * FlatLaf, JScrollBar-nya kehilangan tombol panah krn UI delegate custom
     * library ini (WindowsJDateTimePickerUI - dipakai utk L&F apa pun selain
     * Metal/Motif, lihat JDateTimePicker.updateUI()) tidak cocok dgn FlatLaf.
     * Paksa tiap JScrollBar yg ditemukan memakai MetalScrollBarUI (tombol
     * panah bawaan Swing) - tidak menyentuh JTextField/format/nilai tanggal.
     */
    private void paksaScrollBarMetal(Container c){
        if (c == null) return;
        for (Component child : c.getComponents()){
            if (child instanceof JScrollBar){
                ((JScrollBar)child).setUI(new MetalScrollBarUI());
            }
            if (child instanceof Container){
                paksaScrollBarMetal((Container)child);
            }
        }
    }

}
