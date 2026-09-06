
package widget;

import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import usu.widget.glass.ViewPortGlass;

/**
 *
 * @author usu
 */
public class ScrollPane extends JScrollPane {


    public ScrollPane() {
        super();
        // ViewPortGlass.paintChildren() menggambar wash PUTIH diagonal (GeneralPath
        // melengkung dari kiri-bawah ke kanan-atas) SETELAH super.paintChildren() -
        // jadi menimpa/menutupi wallpaper landing page, bukan ketutup olehnya
        // ("gradasi putih" yg dilaporkan di PanelLandingHospital). Alpha dimatikan
        // di sini (satu2nya titik yg bisa diedit, krn ViewPortGlass cuma ada di
        // UsuLibrary.jar tanpa source) - pola sama dgn PanelWall.setAlpha(0f).
        ViewPortGlass vpGlass = new ViewPortGlass();
        vpGlass.setAlpha(0f);
        setViewport(vpGlass);
        setOpaque(false);
        //setBorder(new LineBorder(new Color(235,140,235)));
        //setBackground(new Color(255,235,255));
        setBorder(new LineBorder(new Color(239,244,234)));
        setBackground(new Color(255,255,255));
        getVerticalScrollBar().setUnitIncrement(15);
    }
}
