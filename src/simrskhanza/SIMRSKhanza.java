/*
  Dilarang keras memperjualbelikan/mengambil keuntungan dari Software 
  ini dalam bentuk apapun tanpa seijin pembuat software
  (Khanza.Soft Media). Bagi yang sengaja membajak softaware ini ta
  npa ijin, kami sumpahi sial 1000 turunan, miskin sampai 500 turu
  nan. Selalu mendapat kecelakaan sampai 400 turunan. Anak pertama
  nya cacat tidak punya kaki sampai 300 turunan. Susah cari jodoh
  sampai umur 50 tahun sampai 200 turunan. Ya Alloh maafkan kami 
  karena telah berdoa buruk, semua ini kami lakukan karena kami ti
  dak pernah rela karya kami dibajak tanpa ijin.
 */
package simrskhanza;

import javax.swing.UIManager;
import usu.widget.util.WidgetUtilities;

/**
 *
 * @author khanzasoft
 */
public class SIMRSKhanza {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        UIManager.put("ComboBox.buttonStyle", "auto");
        UIManager.put("Component.arrowType", "triangle");
        UIManager.put("Spinner.buttonStyle", "button");
        UIManager.put("ComboBox.squareButton", false);
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("defaultFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        UIManager.put("Button.toolbar.hoverBackground", new java.awt.Color(10, 155, 168));
        UIManager.put("Button.toolbar.pressedBackground", new java.awt.Color(8, 126, 139));

        widget.ModernStyle.pasang();

        // Pre-load class Batik SVG di awal (single-threaded, sebelum ada
        // report/viewer jalan) - classpath Class-Path manifest jar ini
        // sangat panjang (~280 entri), dan JDK 8 (URLClassPath$JarLoader)
        // punya bug lawas: kalau class ini baru di-load PERTAMA KALI di
        // tengah render report/thread lain jalan bareng, kadang muncul
        // sun.misc.InvalidJarIndexException. Load duluan di sini supaya
        // resolusinya kelar sebelum ada thread lain berebut classloader.
        try {
            Class.forName("org.apache.batik.anim.dom.SVGDOMImplementation");
        } catch (Throwable ignored) {
        }

        WidgetUtilities.invokeLater(() -> {
           frmUtama utama=frmUtama.getInstance();
           utama.isWall();
           utama.setVisible(true);
        });
    }
    
}
