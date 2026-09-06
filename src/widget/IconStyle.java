package widget;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * IconStyle - memasang ikon otomatis ke tombol berdasarkan teksnya.
 *
 * TIDAK menyentuh layout, nama variabel, atau fungsi.
 * Hanya memanggil setIcon(), setIconTextGap(), dan setHorizontalTextPosition().
 *
 * PERSIAPAN:
 *   1. Pastikan flatlaf-extras JAR ada di classpath
 *   2. Unduh ikon SVG dari lucide.dev (gratis, gaya garis)
 *   3. Taruh di folder: src/icon/
 *      File yang dibutuhkan:
 *        save.svg, plus.svg, trash.svg, pencil.svg, printer.svg,
 *        list.svg, log-out.svg, search.svg, refresh.svg,
 *        user.svg, calendar.svg, server.svg
 *
 * CARA PAKAI:
 *   Di konstruktor form, setelah initComponents():
 *       IconStyle.pasang(this);
 *
 *   Ikon dipasang otomatis berdasarkan teks tombol.
 */
public class IconStyle {

    // ---------- WARNA AKSI ----------
    public static final Color BIRU   = new Color(37, 99, 235);   // simpan
    public static final Color HIJAU  = new Color(22, 163, 74);   // baru
    public static final Color MERAH  = new Color(220, 38, 38);   // hapus
    public static final Color ORANYE = new Color(234, 88, 12);   // ganti
    public static final Color ABU    = new Color(71, 85, 105);   // netral
    public static final Color ABU_MUDA = new Color(100, 116, 139); // status bar

    private static final int UKURAN_TOMBOL = 16;
    private static final int UKURAN_STATUS = 14;

    /** Peta: kata kunci pada teks tombol -> [nama file svg, warna] */
    private static final Map<String, Object[]> PETA = new HashMap<>();
    static {
        PETA.put("simpan",  new Object[]{"save.svg",     BIRU});
        PETA.put("baru",    new Object[]{"plus.svg",     HIJAU});
        PETA.put("tambah",  new Object[]{"plus.svg",     HIJAU});
        PETA.put("hapus",   new Object[]{"trash.svg",    MERAH});
        PETA.put("ganti",   new Object[]{"pencil.svg",   ORANYE});
        PETA.put("edit",    new Object[]{"pencil.svg",   ORANYE});
        PETA.put("ubah",    new Object[]{"pencil.svg",   ORANYE});
        PETA.put("cetak",   new Object[]{"printer.svg",  ABU});
        PETA.put("print",   new Object[]{"printer.svg",  ABU});
        PETA.put("semua",   new Object[]{"list.svg",     ABU});
        PETA.put("keluar",  new Object[]{"log-out.svg",  ABU_MUDA});
        PETA.put("tutup",   new Object[]{"log-out.svg",  ABU_MUDA});
        PETA.put("cari",    new Object[]{"search.svg",   ABU});
        PETA.put("refresh", new Object[]{"refresh.svg",  ABU});
        PETA.put("batal",   new Object[]{"refresh.svg",  ABU_MUDA});
        PETA.put("reset",   new Object[]{"refresh.svg",  ABU_MUDA});
        // [BARU] Aksi non-CRUD yg belum kebagian icon (dilaporkan user -
        // tombol "Validasi" resep obat, "Sampel"/"Hasil" lab, dkk).
        PETA.put("validasi",   new Object[]{"circle-check.svg",    HIJAU});
        PETA.put("verifikasi", new Object[]{"circle-check.svg",    HIJAU});
        PETA.put("sampel",     new Object[]{"test-tube.svg",       ABU});
        PETA.put("hasil",      new Object[]{"clipboard-check.svg", ABU});
        // [BARU] tombol navigasi ke form "Pelaksanaan ..." (mis. Pelaksanaan
        // Informasi Edukasi) - clipboard-list mencerminkan pelaksanaan/
        // tindak lanjut suatu rencana/checklist, beda dari "hasil" (sudah
        // dicentang/selesai).
        PETA.put("pelaksanaan", new Object[]{"clipboard-list.svg", ABU});
    }

    /**
     * Telusuri seluruh komponen dan pasang ikon pada tombol yang cocok.
     */
    public static void pasang(Container root) {
        for (Component c : root.getComponents()) {

            if (c instanceof AbstractButton && !(c instanceof JRadioButton) && !(c instanceof JCheckBox)) {
                AbstractButton b = (AbstractButton) c;
                pasangIkonTombol(b);
            }

            if (c instanceof Container) {
                pasang((Container) c);
            }
        }
    }

    private static void pasangIkonTombol(AbstractButton b) {
        // Jangan pasang icon pada RadioButton dan CheckBox
        if (b instanceof JRadioButton || b instanceof JCheckBox) return;

        // Jangan timpa jika sudah memakai SVG modern
        if (b.getIcon() instanceof FlatSVGIcon) return;

        String teks = b.getText();
        if (teks == null || teks.trim().isEmpty()) {
            // Tombol icon-only (misal tombol cari petugas, all/refresh, tambah)
            if (b.getIcon() != null) {
                String desc = b.getIcon().toString().toLowerCase();
                String name = (b.getName() != null ? b.getName().toLowerCase() : "");
                String tip  = (b.getToolTipText() != null ? b.getToolTipText().toLowerCase() : "");

                if (name.contains("all") || tip.contains("semua") || tip.contains("refresh")) {
                    Icon ikon = muat("refresh.svg", 14, ABU);
                    if (ikon != null) {
                        b.setIcon(ikon);
                    }
                } else if (name.contains("tambah") || desc.contains("plus")) {
                    Icon ikon = muat("plus.svg", 14, HIJAU);
                    if (ikon != null) {
                        b.setIcon(ikon);
                    }
                } else if (desc.contains("190.png") || desc.contains("accept.png") || desc.contains("search") || name.contains("cari")) {
                    Icon ikon = muat("search.svg", 14, ABU_MUDA);
                    if (ikon != null) {
                        b.setIcon(ikon);
                    }
                }
            }
            return;
        }

        String kunci = teks.trim().toLowerCase();

        for (Map.Entry<String, Object[]> e : PETA.entrySet()) {
            String kataKunci = e.getKey();
            // Cocokkan sebagai kata utuh (word boundary), agar misal 'keluarga' tidak kena 'keluar'
            if (kunci.matches(".*\\b" + java.util.regex.Pattern.quote(kataKunci) + "\\b.*") || kunci.equals(kataKunci)) {
                String file  = (String) e.getValue()[0];
                Color  warna = (Color)  e.getValue()[1];

                Icon ikon = muat(file, UKURAN_TOMBOL, warna);
                if (ikon != null) {
                    b.setIcon(ikon);
                    b.setIconTextGap(6);
                    b.setHorizontalTextPosition(SwingConstants.RIGHT);
                    b.setHorizontalAlignment(SwingConstants.CENTER);
                }
                return;
            }
        }
    }

    /**
     * Pasang ikon kecil abu untuk label di status bar.
     * Dipanggil manual karena label tidak bisa dikenali dari teksnya.
     *
     * Contoh:
     *     IconStyle.status(lblAdmin,   "user.svg");
     *     IconStyle.status(lblTanggal, "calendar.svg");
     *     IconStyle.status(lblIP,      "server.svg");
     */
    public static void status(JLabel label, String namaFile) {
        if (label == null) return;
        Icon ikon = muat(namaFile, UKURAN_STATUS, ABU_MUDA);
        if (ikon != null) {
            label.setIcon(ikon);
            label.setIconTextGap(5);
            label.setForeground(ABU_MUDA);
        }
    }

    /** Memuat SVG dan mewarnainya. Mengembalikan null bila file tidak ada. */
    public static Icon muat(String namaFile, int ukuran, Color warna) {
        try {
            FlatSVGIcon ikon = new FlatSVGIcon("icon/" + namaFile, ukuran, ukuran);
            ikon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> warna));
            return ikon;
        } catch (Exception e) {
            System.err.println("Ikon tidak ditemukan: " + namaFile);
            return null;
        }
    }
}
