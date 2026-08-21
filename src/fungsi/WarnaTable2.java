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
public class WarnaTable2 extends DefaultTableCellRenderer {
    public int kolom;
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            // Bugfix: logika zebra/highlight-kolom di bawah dulu SELALU
            // menimpa background/foreground terlepas dari isSelected,
            // sehingga baris yg diklik tak pernah benar-benar terlihat
            // "terpilih" (teks putih dari selection ketimpa lagi). Paksa
            // eksplisit ikut Design System saat baris dipilih.
            component.setBackground(ThemeConfig.PRIMARY);
            component.setForeground(ThemeConfig.SURFACE);
            return component;
        }
        // Logika zebra (selang-seling) + highlight kolom bawaan Khanza - biarkan saja
        if (row % 2 == 1){
            component.setBackground(new Color(255,244,244));
        }else{
            component.setBackground(new Color(255,255,255));
        }
        if (column == kolom){
            component.setBackground(new Color(215,215,255));
            component.setForeground(new Color(255,255,255));
            try {
                if(!table.getValueAt(row,kolom).toString().equals("")){
                    component.setBackground(new Color(255,255,255));
                    component.setForeground(new Color(55,55,175));
                }
            } catch (Exception e) {
            }
        }else{
            component.setForeground(ThemeConfig.TEXT_NORMAL);
        }
        return component;
    }

}
