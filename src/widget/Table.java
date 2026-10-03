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
        // [Modernisasi highlight tabel] PRIMARY (teal) dirasa terlalu
        // terang/mencolok - diganti muted navy blue (TABLE_SELECTION_BG)
        // senada header tabel.
        setSelectionBackground(ThemeConfig.TABLE_SELECTION_BG);
        setSelectionForeground(ThemeConfig.TABLE_SELECTION_TEXT);
        // Header terang netral menjadi standar global. Gunakan token khusus
        // agar nilai instance dan UIManager selalu berasal dari sumber sama.
        getTableHeader().setBackground(ThemeConfig.TABLE_HEADER_BG);
        getTableHeader().setForeground(ThemeConfig.TABLE_HEADER_TEXT);
        getTableHeader().setFont(ThemeConfig.FONT_BOLD);
    }
}
