/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.AbstractButton;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
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
        perbesarKalenderPopup(popupEditor);
    }

    /**
     * Kalender popup (JCalendar pihak ketiga) tampil kecil & tombol panah
     * bulan/tahun terlihat janggal setelah FlatLaf dipasang:
     * 1) JMonthChooser/JYearChooser (kelas internal privat, tak bisa
     *    direferensikan langsung dari sini) sebenarnya JSpinField:
     *    JTextField + JScrollBar sbg tombol naik/turun.
     *    - **Revisi**: percobaan awal memaksa `MetalScrollBarUI` (gaya 3D
     *      bevel klasik) supaya tombol panahnya muncul - MEMPERBAIKI
     *      "tombol hilang", tapi menciptakan masalah baru: gaya Metal yg
     *      3D itu tabrakan visual dgn seluruh UI lain yg flat (FlatLaf),
     *      inilah sumber kesan "aneh" yg dilaporkan user. Diperbaiki:
     *      JScrollBar dibiarkan pakai UI delegate FlatLaf-nya sendiri
     *      (`FlatScrollBarUI`, otomatis terpasang krn FlatLaf aktif
     *      app-wide - TIDAK di-`setUI()` paksa lagi), cuma properti
     *      client `"JScrollBar.showButtons"` diset `true` PER-KOMPONEN
     *      (bukan `UIManager.put("ScrollBar.showButtons",...)` global -
     *      supaya scrollbar lain di app, kalau ada, tidak ikut berubah).
     *      Ini bikin tombol naik/turun tampil dgn gaya flat yg konsisten
     *      dgn sisa aplikasi, bukan lagi gaya Metal yg mencolok/asing.
     * 2) Tombol tanggal (JDayChooser, grid 7x7) & kotak teks bulan/tahun
     *    diperbesar eksplisit (font DAN preferred size, bukan cuma font) -
     *    margin tombol sudah dipaksa nol oleh pustaka asli jadi ukuran sel
     *    murni ikut font, dan ternyata FlatLaf tidak menambah padding
     *    otomatis spt tombol lain di aplikasi (beda dari margin FlatLaf
     *    yg biasa), jadi kalau cuma font yg dibesarkan hasilnya malah bisa
     *    lebih kecil dari sebelumnya - makanya preferred size dipatok
     *    eksplisit juga supaya hasilnya pasti lebih besar & nyaman diklik.
     *    **Revisi**: ukuran dinaikkan sekali lagi atas permintaan user
     *    ("ukuran pop up kalender sedikit lebih besar").
     * Diverifikasi visual via render offscreen (Robot screen-capture thd
     * popup sungguhan, bukan cuma tebakan) sebelum ukuran final ini dipilih.
     * TIDAK menyentuh nilai/fungsi/format tanggal apa pun - murni tampilan.
     */
    private void perbesarKalenderPopup(Container c){
        if (c == null) return;
        Font fontKalender = new Font("Dialog", Font.PLAIN, 15);
        for (Component child : c.getComponents()){
            if (child instanceof JScrollBar){
                JScrollBar sb = (JScrollBar)child;
                sb.putClientProperty("JScrollBar.showButtons", Boolean.TRUE);
                sb.setPreferredSize(new Dimension(18, 30));
            } else if (child instanceof AbstractButton){
                child.setFont(fontKalender);
                child.setPreferredSize(new Dimension(37, 30));
            } else if (child instanceof JTextField){
                child.setFont(fontKalender);
                child.setPreferredSize(new Dimension(child.getPreferredSize().width + 12, 30));
            }
            if (child instanceof Container){
                perbesarKalenderPopup((Container)child);
            }
        }
    }

}
