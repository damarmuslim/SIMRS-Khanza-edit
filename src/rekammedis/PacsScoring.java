/*
  Dilarang keras menggandakan/mengcopy/menyebarkan/membajak/mendecompile
  Software ini dalam bentuk apapun tanpa seijin pembuat software
  (Khanza.Soft Media). Bagi yang sengaja membajak softaware ini ta
  npa ijin, kami sumpahi sial 1000 turunan, miskin sampai 500 turu
  nan. Selalu mendapat kecelakaan sampai 400 turunan. Anak pertama
  nya cacat tidak punya kaki sampai 300 turunan. Susah cari jodoh
  sampai umur 50 tahun sampai 200 turunan. Ya Alloh maafkan kami
  karena telah berdoa buruk, semua ini kami lakukan karena kami ti
  dak pernah rela karya kami dibajak tanpa ijin.
 */

package rekammedis;

/**
 * Mesin scoring EWS untuk Level Triage PACS (formulir RM 03-1).
 *
 * Aturan skor di bawah ini SUDAH DIKONFIRMASI oleh pengguna dari formulir
 * kertas asli - tidak ada angka yang ditambahkan/ditebak/diasumsikan secara
 * klinis oleh pihak pengembang. Kalau formulir direvisi, ubah angka di sini
 * saja (satu tempat), lalu jalankan PacsScoringTest untuk memastikan tidak
 * ada regresi.
 *
 * Kelas ini sengaja dipisah dari RMTriaseIGD.java (tanpa dependency Swing/DB)
 * supaya bisa diuji langsung dengan JUnit tanpa perlu membuka form/koneksi
 * database.
 */
public final class PacsScoring {

    public static final String KESADARAN_SADAR_PENUH = "Sadar Penuh";

    private PacsScoring(){}

    /**
     * Kesadaran: "Sadar Penuh" menjadi skor 0, selain itu (Respon Suara/Respon
     * Nyeri/Tidak Respon) menjadi skor 3. SKOR 1 dan SKOR 2 memang kosong di formulir.
     */
    public static int skorKesadaran(String kesadaran){
        if(KESADARAN_SADAR_PENUH.equals(kesadaran)){
            return 0;
        }
        return 3;
    }

    /**
     * Tekanan Darah Sistolik (mmHg): nilai &gt;=100 menjadi skor 0, nilai &lt;=99 menjadi skor 2.
     * SKOR 1 dan SKOR 3 memang kosong di formulir.
     */
    public static int skorTekananDarahSistolik(int sistolik){
        if(sistolik>=100){
            return 0;
        }
        return 2;
    }

    /**
     * Nadi (x/menit): nilai &lt;=101 menjadi skor 0, nilai &gt;=102 menjadi skor 1.
     * SKOR 2 dan SKOR 3 memang kosong di formulir.
     */
    public static int skorNadi(int nadi){
        if(nadi<=101){
            return 0;
        }
        return 1;
    }

    /**
     * Respirasi (x/menit): nilai &lt;=19 menjadi skor 0, 20-21 menjadi skor 1, nilai &gt;=22 menjadi skor 2.
     * SKOR 3 memang kosong di formulir.
     */
    public static int skorRespirasi(int respirasi){
        if(respirasi<=19){
            return 0;
        }else if(respirasi<=21){
            return 1;
        }
        return 2;
    }

    /**
     * Temperatur (Celsius): nilai &gt;=35,3 menjadi skor 0, nilai &lt;35,3 menjadi skor 3.
     * SKOR 1 dan SKOR 2 memang kosong di formulir.
     */
    public static int skorTemperatur(double suhu){
        if(suhu>=35.3){
            return 0;
        }
        return 3;
    }

    /**
     * Saturasi O2 (%): 96-100 menjadi skor 0, 94-95 menjadi skor 1, 92-93 menjadi skor 2, nilai &lt;92 menjadi skor 3.
     */
    public static int skorSaturasiO2(int saturasi){
        if(saturasi>=96){
            return 0;
        }else if(saturasi>=94){
            return 1;
        }else if(saturasi>=92){
            return 2;
        }
        return 3;
    }

    /** Jumlah 6 skor parameter menjadi Total. */
    public static int hitungTotal(int skorKesadaran,int skorTekananDarahSistolik,int skorNadi,int skorRespirasi,int skorTemperatur,int skorSaturasiO2){
        return skorKesadaran+skorTekananDarahSistolik+skorNadi+skorRespirasi+skorTemperatur+skorSaturasiO2;
    }

    /**
     * Warna klasifikasi baris "Total" di formulir: nilai &gt;=5 menjadi Merah, 2-4 menjadi Kuning,
     * 0-1 menjadi Hijau. HANYA untuk tampilan warna Total - TIDAK dipakai untuk
     * memilih otomatis Level PACS atau Keputusan (itu tetap pilihan manual
     * petugas, tidak diminta untuk diotomatisasi).
     */
    public static String warnaTotal(int total){
        if(total>=5){
            return "Merah";
        }else if(total>=2){
            return "Kuning";
        }
        return "Hijau";
    }
}
