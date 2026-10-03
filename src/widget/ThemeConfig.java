package widget;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

/**
 * Design System SIMRS Khanza (Fase 2 & 2.5) - kumpulan konstanta warna/font
 * terpusat (Single Source of Truth) + method utk terapkan tema FlatLaf
 * secara global.
 *
 * Sejak Fase 2.5, applyGlobalTheme() DIPANGGIL di SIMRSKhanza.main()
 * (setelah FlatLightLaf.setup() + widget.ModernStyle.pasang()) - lihat
 * catatan urutan panggilan di javadoc applyGlobalTheme() di bawah.
 * widget.ModernStyle sekarang mengambil palet warnanya dari sini juga
 * (bukan lagi hex literal terpisah).
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
    // ButtonGlass melukis glassColor dengan alpha sekitar 43% lalu memudar.
    // Nilai ini sengaja lebih pekat agar hasil akhirnya tetap berupa teal muda.
    public static final Color ACTION_HOVER_BG = new Color(0xA9, 0xDB, 0xD8);
    public static final Color ACTION_DANGER_HOVER_BG = new Color(0xF0, 0xB4, 0xAF);
    // Muted navy blue (turunan NAVY, dilembutkan ~15% ke arah putih) -
    // "senada" dgn header tabel tapi tidak sekontras/segelap header, dipakai
    // utk baris tabel terpilih (ganti PRIMARY/teal yg dirasa terlalu terang).
    public static final Color TABLE_SELECTION_BG   = new Color(0x3A, 0x51, 0x68);
    public static final Color TABLE_SELECTION_TEXT = Color.WHITE;

    // ---------- STATUS & PERMUKAAN TAMBAHAN ----------
    // Dipakai sub-tab PACS form Triase IGD (indikator skor EWS 0-3, banner
    // total, kartu Level Triage, pembungkus Keputusan). Ditambahkan ke sini
    // (Single Source of Truth), BUKAN kelas warna terpisah, sesuai keputusan
    // "SATU tema dari ThemeConfig" - lihat javadoc applyGlobalTheme().
    // ACCENT & TEXT sengaja jadi ALIAS ke PRIMARY/TEXT_NORMAL supaya kode
    // pemanggil terbaca sesuai spec desain tanpa memecah nilai warnanya
    // (aksen app tetap teal 0x087E8B, bukan 0x0D7A70 dari draf spec).
    public static final Color ACCENT       = PRIMARY;
    public static final Color TEXT         = TEXT_NORMAL;
    public static final Color BORDER_SOFT  = new Color(0xE4, 0xE6, 0xE2);
    public static final Color SURFACE_ALT  = new Color(0xF7, 0xF8, 0xF5);
    public static final Color TEXT_FAINT   = new Color(0x9A, 0xA1, 0x9C);
    public static final Color TABLE_HEADER_BG   = SURFACE_ALT;
    public static final Color TABLE_HEADER_TEXT = TEXT_NORMAL;
    public static final Color OK           = new Color(0x1A, 0x6B, 0x4C);
    public static final Color OK_BG        = new Color(0xE3, 0xF4, 0xEC);
    public static final Color WARN         = new Color(0xA1, 0x5C, 0x00);
    public static final Color WARN_BG      = new Color(0xFD, 0xF1, 0xDC);
    public static final Color DANGER       = new Color(0xB3, 0x26, 0x1E);
    public static final Color DANGER_BG    = new Color(0xFB, 0xEC, 0xEB);
    public static final Color PACS3        = new Color(0x6B, 0x7A, 0x12);
    public static final Color PACS3_BG     = new Color(0xF5, 0xF7, 0xE2);

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
     * Dipanggil di SIMRSKhanza.main(), SESUDAH FlatLightLaf.setup() +
     * widget.ModernStyle.pasang() - dipanggil PALING TERAKHIR dari
     * ketiganya supaya nilai ThemeConfig menang di properti UIManager
     * yang tumpang-tindih (Component.arc dkk, sebelumnya di-set arc=8
     * langsung di SIMRSKhanza.java - sekarang arc=6 dari sini yang jadi
     * nilai final, sesuai keputusan "SATU tema dari ThemeConfig").
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
        UIManager.put("Table.selectionBackground", TABLE_SELECTION_BG);
        UIManager.put("Table.selectionForeground", TABLE_SELECTION_TEXT);

        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Component.borderColor", BORDER);

        UIManager.put("Label.foreground", TEXT_NORMAL);
        UIManager.put("Button.font", FONT_NORMAL);
        UIManager.put("Label.font", FONT_NORMAL);
        UIManager.put("TextField.font", FONT_NORMAL);
        UIManager.put("ComboBox.font", FONT_NORMAL);
        UIManager.put("Table.font", FONT_NORMAL);
        UIManager.put("TableHeader.font", FONT_BOLD);

        // Input fields background - putih bersih (#FFFFFF), hilangkan warna abu-abu pada inactive/disabled
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.inactiveBackground", SURFACE);
        UIManager.put("TextField.disabledBackground", SURFACE);
        UIManager.put("TextField.focusedBackground", SURFACE);
        UIManager.put("FormattedTextField.background", SURFACE);
        UIManager.put("FormattedTextField.inactiveBackground", SURFACE);
        UIManager.put("FormattedTextField.disabledBackground", SURFACE);
        UIManager.put("FormattedTextField.focusedBackground", SURFACE);
        UIManager.put("PasswordField.background", SURFACE);
        UIManager.put("PasswordField.inactiveBackground", SURFACE);
        UIManager.put("PasswordField.disabledBackground", SURFACE);
        UIManager.put("ComboBox.background", SURFACE);
        UIManager.put("ComboBox.disabledBackground", SURFACE);
        UIManager.put("ComboBox.buttonBackground", SURFACE);
        UIManager.put("ComboBox.buttonEditableBackground", SURFACE);
        UIManager.put("ComboBox.editableBackground", SURFACE);
        UIManager.put("ComboBox.focusedBackground", SURFACE);
        UIManager.put("TextArea.background", SURFACE);
        UIManager.put("TextArea.inactiveBackground", SURFACE);
        UIManager.put("TextArea.disabledBackground", SURFACE);
        UIManager.put("Spinner.background", SURFACE);
        UIManager.put("Spinner.disabledBackground", SURFACE);
        UIManager.put("CheckBox.background", SURFACE);
        UIManager.put("RadioButton.background", SURFACE);
        UIManager.put("List.background", SURFACE);
        UIManager.put("PopupMenu.background", SURFACE);
    }
}
