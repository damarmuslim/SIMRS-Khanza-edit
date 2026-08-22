package widget;

import java.awt.Color;
import java.awt.Insets;

/**
 *
 * @author usu
 */
public class Button extends usu.widget.ButtonGlass {

    /*
     * Serial version UID
     */
    private static final long serialVersionUID = 1L;

    public Button() {
        super();
        setFont(new java.awt.Font("Segoe UI", 1, 11));
        setForeground(new Color(50,50,50));
        setGlassColor(new Color(245,240,240));
        setMargin(new Insets(2, 12, 2, 12));
        // [Formatting tombol text-only] Icon lawas sudah disembunyikan kalau
        // ada teks (lihat setIcon()/setText() override di bawah) - tombol
        // jadi text-only, jadi iconTextGap (jarak sisa bekas icon) sudah
        // tidak relevan & dinolkan, teks diratakan tengah eksplisit supaya
        // tidak nyisa ruang kosong di kiri.
        setIconTextGap(0);
        setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        setRoundRect(true);
        putClientProperty("JButton.buttonType", "roundRect");
    }

    /*
     * [Modernisasi tombol footer] Icon RASTER lawas (.png/.gif pecah di
     * resolusi tinggi, mis. save-16x16.png dari GEN block) disembunyikan
     * kalau tombol punya teks (mis. Simpan/Batal/Hapus/Keluar) - teks tebal
     * (font style=1/BOLD di atas) sudah cukup tegas tanpa icon. Tombol
     * icon-only (tanpa teks, mis. tombol mini "Cari" ber-icon accept.png
     * tanpa label) TIDAK terpengaruh - kalau tidak ada teks, icon tetap
     * dipakai supaya tombol tidak kosong.
     *
     * Icon SVG modern (FlatSVGIcon, mis. dari widget.IconStyle) TIDAK ikut
     * disembunyikan sekalipun tombol punya teks - beda kasus dgn raster
     * lawas: SVG dipasang SENGAJA sbg upgrade visual (bukan sisa GEN block
     * lama), jadi lolos dari guard ini via pengecekan tipe instance.
     *
     * Diterapkan lewat override setIcon()/setText() (bukan edit tiap
     * pemanggilan .setIcon(...) di ratusan form) karena pemanggilan asli
     * ada di dalam blok GEN-BEGIN/GEN-END (initComponents() hasil NetBeans
     * Form Editor) yang tidak boleh diedit manual. Urutan generate NetBeans
     * selalu .setIcon(...) dulu baru .setText(...), tapi override di kedua
     * method ini membuatnya aman utk urutan sebaliknya juga.
     */
    private boolean adaTeks(){
        String t = getText();
        return t != null && !t.trim().isEmpty();
    }

    private boolean iconRasterLawas(javax.swing.Icon icon){
        return icon != null && !(icon instanceof com.formdev.flatlaf.extras.FlatSVGIcon);
    }

    @Override
    public void setIcon(javax.swing.Icon icon) {
        if (iconRasterLawas(icon) && adaTeks()) {
            super.setIcon(null);
            return;
        }
        super.setIcon(icon);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (iconRasterLawas(getIcon()) && adaTeks()) {
            super.setIcon(null);
        }
    }
}
