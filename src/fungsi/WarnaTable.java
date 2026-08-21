/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import widget.ThemeConfig;

/**
 *
 * @author Owner
 */
public class WarnaTable extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            // Bugfix: baris di bawah dulu SELALU menimpa background ke
            // warna zebra (putih/pink) tanpa peduli isSelected, sementara
            // foreground baris terpilih sudah putih (dari
            // JTable.selectionForeground) - hasilnya teks tak terbaca
            // saat baris diklik. Paksa eksplisit ikut Design System.
            component.setBackground(ThemeConfig.PRIMARY);
            component.setForeground(ThemeConfig.SURFACE);
        } else if (row % 2 == 1){
            // Logika zebra (selang-seling) bawaan Khanza - biarkan saja
            component.setBackground(new Color(255,244,244));
            component.setForeground(ThemeConfig.TEXT_NORMAL);
        }else{
            component.setBackground(new Color(255,255,255));
            component.setForeground(ThemeConfig.TEXT_NORMAL);
        }
        return component;
    }

}
