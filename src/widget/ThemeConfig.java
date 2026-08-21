package widget;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

/**
 * Design System SIMRS Khanza (Fase 2) - kumpulan konstanta warna/font
 * terpusat + method opsional utk terapkan tema FlatLaf secara global.
 *
 * TIDAK dipanggil di mana pun secara otomatis (lihat catatan di
 * applyGlobalTheme()) - dibuat murni sbg persiapan, menunggu instruksi
 * lanjutan sebelum benar-benar diaktifkan.
 */
public final class ThemeConfig {

    private ThemeConfig(){}

    // ---------- WARNA ----------
    public static final Color PRIMARY      = new Color(0x08, 0x7E, 0x8B); // Teal
    public static final Color NAVY         = new Color(0x17, 0x32, 0x4D); // Dark/Navy
    public static final Color BACKGROUND   = new Color(0xF7, 0xF9, 0xFB);
    public static final Color SURFACE      = Color.WHITE;                 // #FFFFFF
    public static final Color BORDER       = new Color(0xD9, 0xE1, 0xE7);
    public static final Color TEXT_NORMAL  = new Color(0x2D, 0x37, 0x48);
    public static final Color TEXT_MUTED   = new Color(0x71, 0x80, 0x96);

    // ---------- INTERAKSI ----------
    public static final Color FOCUS        = PRIMARY;
    public static final Color TAB_ACTIVE   = PRIMARY;
    public static final Color TABLE_HEADER_BG   = NAVY;   // alternatif: PRIMARY
    public static final Color TABLE_HEADER_TEXT = Color.WHITE;

    // ---------- FONT ----------
    public static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_NORMAL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_BOLD   = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font FONT_LARGE  = new Font(FONT_FAMILY, Font.PLAIN, 14);

    // ---------- GEOMETRI & SPACING ----------
    public static final int ARC = 6;              // border radius input/button
    public static final int SPACING_SMALL  = 8;
    public static final int SPACING_MEDIUM = 12;
    public static final int SPACING_LARGE  = 16;

    /**
     * Terapkan Design System ke UIManager secara global (FlatLaf UIDefaults).
     *
     * CATATAN PENTING: method ini SENGAJA BELUM dipanggil di mana pun
     * (bukan di SIMRSKhanza.java, bukan di frmUtama.java) - menunggu
     * instruksi lanjutan sesuai batasan task Fase 2. Kalau nanti
     * dipanggil, panggil SEBELUM form pertama dibuat (pola yg sama dgn
     * FlatLightLaf.setup()/ModernStyle.pasang() di SIMRSKhanza.java).
     *
     * PERLU DIPERHATIKAN sebelum diaktifkan: SIMRSKhanza.java SAAT INI
     * sudah py "Component.arc"=8 (dari sesi modernisasi Main Window
     * sebelumnya) - beda dgn ARC=6 di sini. Kalau method ini dipanggil
     * SETELAH baris itu, arc=6 akan menang (dipanggil belakangan) - perlu
     * diputuskan mana yg dipakai, bukan otomatis diselaraskan di sini.
     */
    public static void applyGlobalTheme(){
        UIManager.put("Component.arc", ARC);
        UIManager.put("Button.arc", ARC);
        UIManager.put("TextComponent.arc", ARC);
        UIManager.put("ComboBox.arc", ARC);

        UIManager.put("Component.focusColor", FOCUS);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);

        UIManager.put("TabbedPane.underlineColor", TAB_ACTIVE);
        UIManager.put("TabbedPane.selectedBackground", SURFACE);
        UIManager.put("TabbedPane.hoverColor", BACKGROUND);

        UIManager.put("TableHeader.background", TABLE_HEADER_BG);
        UIManager.put("TableHeader.foreground", TABLE_HEADER_TEXT);

        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Component.borderColor", BORDER);

        UIManager.put("Label.foreground", TEXT_NORMAL);
        UIManager.put("Button.font", FONT_NORMAL);
        UIManager.put("Label.font", FONT_NORMAL);
        UIManager.put("TextField.font", FONT_NORMAL);
        UIManager.put("ComboBox.font", FONT_NORMAL);
        UIManager.put("Table.font", FONT_NORMAL);
        UIManager.put("TableHeader.font", FONT_BOLD);
    }
}
