package widget;

import java.awt.Color;
import java.awt.Insets;
import javax.swing.SwingConstants;

/**
 * Modern Clean Button untuk SIMRS Khanza.
 * Mendukung pembedaan cerdas:
 * 1. Tombol aksi utama berteks (Simpan, Baru, Hapus, Cetak, dll) -> card border rounded 8px.
 * 2. Menu samping kiri (FormMenu, alignment LEFT atau roundRect false) -> flat / borderless list item.
 * 3. Tombol tool / cari / tambah icon-only (tanpa teks) -> flat toolBarButton.
 */
public class Button extends usu.widget.ButtonGlass {

    private static final long serialVersionUID = 1L;

    public Button() {
        super();
        setFont(new java.awt.Font("Segoe UI", 1, 11));
        setForeground(new Color(50, 50, 50));
        setGlassColor(new Color(245, 240, 240));
        setMargin(new Insets(2, 8, 2, 8));
        setIconTextGap(6);
        setHorizontalAlignment(SwingConstants.CENTER);
        setHorizontalTextPosition(SwingConstants.RIGHT);
        setRoundRect(true);
        setFocusPainted(false);
        updateGayaBorder();
    }

    public void updateGayaBorder() {
        if (!adaTeks()) {
            // Tombol tool / cari / tambah icon-only tanpa teks
            setContentAreaFilled(false);
            setBorderPainted(false);
            putClientProperty("JButton.buttonType", "toolBarButton");
        } else if (getHorizontalAlignment() == SwingConstants.LEFT || !isRoundRect()) {
            // Menu samping kiri (FormMenu / sidebar list)
            setContentAreaFilled(false);
            setBorderPainted(false);
            putClientProperty("JButton.buttonType", "toolBarButton");
        } else {
            // Tombol aksi utama berteks (Simpan, Baru, Hapus, Ganti, Cetak, Semua, Keluar, dll)
            setContentAreaFilled(true);
            setBorderPainted(true);
            putClientProperty("JButton.buttonType", null);
            putClientProperty("JButton.arc", 8);
        }
    }

    private boolean adaTeks() {
        String t = getText();
        return t != null && !t.trim().isEmpty();
    }

    private boolean iconRasterLawas(javax.swing.Icon icon) {
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
        updateGayaBorder();
    }

    @Override
    public void setHorizontalAlignment(int alignment) {
        super.setHorizontalAlignment(alignment);
        updateGayaBorder();
    }

    @Override
    public void setRoundRect(boolean roundRect) {
        super.setRoundRect(roundRect);
        updateGayaBorder();
    }
}
