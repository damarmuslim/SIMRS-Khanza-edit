package rekammedis;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Daftar pilihan kotak centang RM 02-2 dan perakit barisnya untuk CETAKAN.
 *
 * <p><b>Kenapa kelas terpisah.</b> Daftar pilihan ini dipakai DUA pihak: form
 * {@link RMAsesmenAwalKeperawatanRalan} untuk merakit kotak centang di layar,
 * dan berkas {@code rptAsesmenAwalKeperawatanRalanRM02.jrxml} untuk mencetak
 * deretan kotak centang di kertas. Kalau daftarnya disalin ke JRXML, keduanya
 * akan berbeda diam-diam begitu ada pilihan yang ditambah - persis jenis
 * ketidaksinkronan yang sudah beberapa kali menggigit proyek ini. Karena itu
 * JRXML tidak memuat daftar apa pun; ia hanya memanggil
 * {@link #baris(String, String)} dengan NAMA KOLOM, dan daftarnya diambil dari
 * sini.</p>
 *
 * <p><b>Glif kotaknya karakter UTF-8 langsung</b>, bersandar pada
 * {@code source.encoding=UTF-8} di {@code nbproject/project.properties}. Kalau
 * berkas ini kelak tersimpan ulang dengan encoding lain, kotak di kertas
 * berubah jadi sampah - itu yang dicek pertama kalau cetakannya tiba-tiba
 * aneh.</p>
 *
 * <p>Font pemakainya WAJIB punya glif itu. Tahoma TIDAK punya dan gagalnya
 * DIAM-DIAM (jadi kotak kosong atau spasi), jadi di JRXML baris hasil method
 * ini dicetak dengan DejaVu Sans yang disediakan
 * {@code jasperreports-fonts-6.8.0.jar}.</p>
 */
public final class CentangRM022 {

    /** BALLOT BOX (U+2610). */
    private static final String KOTAK = "☐";
    /** BALLOT BOX WITH CHECK (U+2611). */
    private static final String KOTAK_CENTANG = "☑";

    /** Pemeriksaan fisik: {kolom, judul di layar, pilihan dipisah '|'}. */
    public static final String[][] REGIO = {
        {"kepala", "Kepala", "Simetris|Tidak simetris|Benjolan|Nyeri tekan|Hematoma"},
        {"rambut", "Rambut", "Bersih|Kotor|Rontok|Beruban|Mudah dicabut|Ketombe"},
        {"mata", "Mata", "Simetris|Konjungtiva anemis|Konjungtiva merah muda|Sklera ikterik"
            + "|Pupil isokor|Pupil anisokor|Reflek cahaya positif|Sekret|Memakai alat bantu lihat"
            + "|Penglihatan kabur"},
        {"hidung", "Hidung", "Simetris|Polip|Sekret|Epistaksis|Pernafasan cuping hidung"
            + "|Terpasang oksigen"},
        {"telinga", "Telinga", "Simetris|Serumen|Sekret|Nyeri|Gangguan pendengaran"
            + "|Memakai alat bantu dengar"},
        {"mulut", "Mulut", "Mukosa lembab|Mukosa kering|Sianosis|Stomatitis|Bibir pecah-pecah"
            + "|Bau mulut"},
        {"gigi", "Gigi", "Lengkap|Tidak lengkap|Caries|Karang gigi|Gigi palsu|Nyeri gigi"},
        {"lidah", "Lidah", "Bersih|Kotor|Tremor|Sariawan"},
        {"tenggorokan", "Tenggorokan", "Normal|Nyeri telan|Pembesaran tonsil|Serak"},
        {"leher", "Leher", "Normal|Pembesaran kelenjar tiroid|Pembesaran kelenjar getah bening"
            + "|Peningkatan tekanan vena jugularis|Kaku kuduk|Keterbatasan gerak"},
        {"integumen", "Integumen/Kulit", "Turgor baik|Turgor jelek|Pucat|Sianosis|Ikterik"
            + "|Kemerahan|Oedema|Luka|Akral hangat|Akral dingin|Gatal|Bekas garukan"},
        {"payudara_ukuran_bentuk", "Payudara - ukuran & bentuk",
            "Simetris|Tidak simetris|Membesar|Mengecil"},
        {"payudara_warna", "Payudara - warna",
            "Normal|Hiperpigmentasi areola|Kemerahan|Kehitaman"},
        {"payudara_kelainan", "Payudara - kelainan",
            "Tidak ada kelainan|Benjolan|Nyeri tekan|Puting masuk ke dalam|Pengeluaran ASI"
            + "|Pengeluaran cairan|Luka"},
        {"thorax_paru", "Thorax/Paru", "Simetris|Retraksi dinding dada|Vesikuler|Ronchi"
            + "|Wheezing|Sesak nafas|Batuk|Nyeri dada|Penggunaan otot bantu nafas"},
        {"jantung", "Jantung", "Bunyi jantung normal|Murmur|Gallop|Palpitasi|Nyeri dada"
            + "|Pembesaran jantung"},
        {"abdomen", "Abdomen", "Datar|Distensi|Nyeri tekan|Bising usus normal"
            + "|Bising usus meningkat|Bising usus menurun|Asites|Teraba massa|Hepatomegali"
            + "|Luka operasi"},
        {"genitalia", "Genitalia", "Bersih|Kotor|Terpasang kateter|Keputihan|Perdarahan"
            + "|Nyeri berkemih|Lesi"},
        {"anus", "Anus", "Normal|Hemoroid|Fissura|Teraba massa|Nyeri"},
        {"muskuloskeletal", "Muskuloskeletal", "Normal|Kelemahan otot|Atrofi|Kontraktur"
            + "|Fraktur|Oedema ekstremitas|Nyeri sendi|Keterbatasan gerak|Memakai alat bantu jalan"}
    };

    /** Pola kebiasaan pasien: {kolom, judul, pilihan}. */
    public static final String[][] KEBIASAAN = {
        {"nutrisi", "Nutrisi", "Nafsu makan baik|Nafsu makan menurun|Mual|Muntah"
            + "|Sulit menelan|Diet khusus|Pantangan makanan"},
        {"eliminasi", "Eliminasi", "BAK lancar|BAK nyeri|BAK tidak lancar|Sering BAK malam"
            + "|BAB teratur|Konstipasi|Diare|BAB berdarah"},
        {"istirahat", "Istirahat/Tidur", "Cukup|Kurang|Sulit memulai tidur|Sering terbangun"
            + "|Memakai obat tidur"},
        {"aktivitas", "Aktivitas", "Mandiri|Dibantu sebagian|Dibantu penuh|Rutin berolahraga"
            + "|Merokok|Minum alkohol"}
    };

    /** Data psikologis, sosiologis, dan spiritual: {kolom, judul, pilihan}. */
    public static final String[][] PSIKOSOSIAL = {
        {"psikologi", "Psikologi", "Tenang|Cemas|Takut|Marah|Sedih|Menarik diri|Gelisah"},
        {"cemas", "Tingkat kecemasan", "Tidak ada|Ringan|Sedang|Berat"},
        {"sosiologi", "Sosiologi", "Hubungan dengan keluarga baik|Hubungan dengan lingkungan baik"
            + "|Ada masalah keluarga|Ada masalah ekonomi|Tinggal sendiri"},
        {"spiritual", "Spiritual", "Taat beribadah|Kadang-kadang beribadah|Tidak beribadah"
            + "|Perlu bimbingan rohani"},
        {"kebutuhan_informasi", "Kebutuhan informasi", "Penyakit|Pengobatan|Diet"
            + "|Perawatan di rumah|Jadwal kontrol|Pencegahan"}
    };

    /** Blok balita yang berupa pilihan centang: {kolom, judul, pilihan}. */
    public static final String[][] BALITA_CENTANG = {
        {"balita_usia_kehamilan", "Usia kehamilan saat lahir", "Aterm (cukup bulan)|Prematur|Postterm"},
        {"balita_lahir_di", "Lahir di", "Rumah Sakit|Puskesmas|Praktik Bidan|Rumah"},
        {"balita_ditolong", "Ditolong oleh", "Dokter|Bidan|Perawat|Dukun|Keluarga"},
        {"balita_mendapat", "Mendapat", "ASI eksklusif|ASI tidak eksklusif|Susu formula"
            + "|MPASI|Vitamin A"},
        {"balita_imunisasi_dasar", "Imunisasi dasar", "HB-0|BCG|Polio|DPT-HB-Hib|Campak"},
        {"balita_imunisasi_ulang", "Imunisasi ulang", "DPT ulangan|Polio ulangan|Campak ulangan"
            + "|Belum pernah"}
    };

    /** Blok reproduksi yang berupa pilihan centang: {kolom, judul, pilihan}. */
    public static final String[][] REPRO_CENTANG = {
        {"repro_siklus", "Siklus haid", "Teratur|Tidak teratur"},
        {"repro_bercak", "Perdarahan bercak", "Tidak ada|Ada|Di luar siklus"},
        {"repro_kb", "Kontrasepsi", "Tidak memakai|Suntik|Pil|IUD|Implan|Kondom|MOW/MOP"},
        {"repro_pernah", "Riwayat", "Tidak ada|Keputihan|Kista|Mioma|Kanker serviks"
            + "|Operasi kandungan|Keguguran"},
        {"janin_pergerakan", "Pergerakan janin", "Aktif|Kurang aktif|Tidak terasa|Belum terasa"},
        {"janin_posisi", "Posisi janin", "Memanjang|Melintang|Oblique"},
        {"palpasi_presentasi", "Presentasi", "Kepala|Bokong|Bahu|Belum jelas"},
        {"palpasi_masuk_pap", "Masuk PAP", "Belum|Sudah"}
    };

    /**
     * Kolom ENUM yang di FORM berupa radio (pilihan tunggal). Di kertas
     * bentuknya sama saja dengan kotak centang, jadi ikut dirakit di sini
     * supaya seluruh lembar seragam. Bedanya hanya di form: radio memaksa
     * satu pilihan, kotak centang tidak.
     */
    public static final String[][] TUNGGAL = {
        {"keadaan_umum", "Keadaan umum", "Baik|Lemah|Buruk"},
        {"crt", "CRT", "<2 detik|>2 detik"}
    };

    /** kolom -> daftar pilihan, dirakit sekali dari keenam tabel di atas. */
    private static final Map<String, String> PILIHAN = new LinkedHashMap<>();

    static {
        for (String[][] tabel : new String[][][]{REGIO, KEBIASAAN, PSIKOSOSIAL,
            BALITA_CENTANG, REPRO_CENTANG, TUNGGAL}) {
            for (String[] baris : tabel) {
                PILIHAN.put(baris[0], baris[2]);
            }
        }
    }

    private CentangRM022() {
    }

    /**
     * Merakit satu baris kotak centang untuk CETAKAN.
     *
     * <p>Dipanggil langsung dari ekspresi JRXML dengan NAMA KOLOM, jadi daftar
     * pilihannya tidak perlu ada di berkas laporan sama sekali.</p>
     *
     * <p>Seluruh pilihan dicetak; yang ada di {@code isi} diberi kotak
     * bercentang. Potongan yang TIDAK dikenali - biasanya isian "Lain-lain" di
     * form - ditambahkan di belakang sebagai teks, bukan dibuang. Itu penting:
     * temuan yang diketik petugas tidak boleh hilang dari kertas hanya karena
     * ia bukan pilihan baku.</p>
     *
     * @param kolom nama kolom database, mis. {@code "mata"}
     * @param isi   teks tersimpan, temuan dipisah koma; boleh null
     */
    public static String baris(String kolom, String isi) {
        String daftar = PILIHAN.get(kolom);
        if (daftar == null) {
            // Kolom yang bukan kotak centang - kembalikan apa adanya supaya
            // salah pasang di JRXML terlihat sebagai teks biasa, bukan hilang.
            return isi == null ? "" : isi;
        }

        java.util.List<String> dipilih = new java.util.ArrayList<>();
        if (isi != null) {
            for (String potong : isi.split(",")) {
                String t = potong.trim();
                if (!t.isEmpty()) {
                    dipilih.add(t);
                }
            }
        }

        StringBuilder b = new StringBuilder();
        for (String opsi : daftar.split("\\|")) {
            boolean ada = false;
            for (java.util.Iterator<String> it = dipilih.iterator(); it.hasNext();) {
                if (it.next().equalsIgnoreCase(opsi)) {
                    it.remove();
                    ada = true;
                    break;
                }
            }
            if (b.length() > 0) {
                b.append("   ");
            }
            b.append(ada ? KOTAK_CENTANG : KOTAK).append(" ").append(opsi);
        }

        // Sisa yang tidak cocok dengan pilihan mana pun = isian Lain-lain.
        if (!dipilih.isEmpty()) {
            b.append("   ").append(KOTAK_CENTANG).append(" Lain-lain : ");
            for (int n = 0; n < dipilih.size(); n++) {
                b.append(n > 0 ? ", " : "").append(dipilih.get(n));
            }
        }
        return b.toString();
    }
}
