package widget;

import java.awt.Color;
import javax.swing.JTable;

/**
 *
 * @author usu
 */
public class Table extends JTable {

    /*
     * Serial version UID
     */
    private static final long serialVersionUID = 1L;

    public Table() {
        super();
        setBackground(new Color(255,255,255));
        setGridColor(new Color(226,231,221));
        setForeground(new Color(50,50,50));
        setFont(new java.awt.Font("Segoe UI", 0, 11));
        setRowHeight(22);
        // Fase 2.5: warna baris terpilih dipindah ke tema Teal/Navy
        // terpusat (widget.ThemeConfig), bukan lagi hex literal lawas.
        setSelectionBackground(ThemeConfig.PRIMARY);
        setSelectionForeground(ThemeConfig.SURFACE);
        // Fase 2.5: header JTable dimodernisasi - Navy/Surface/Bold sesuai
        // Design System (widget.ThemeConfig), border header lama (garis
        // off-white) dihapus supaya diambil alih FlatLaf.
        getTableHeader().setBackground(ThemeConfig.NAVY);
        getTableHeader().setForeground(ThemeConfig.SURFACE);
        getTableHeader().setFont(ThemeConfig.FONT_BOLD);
    }
}
