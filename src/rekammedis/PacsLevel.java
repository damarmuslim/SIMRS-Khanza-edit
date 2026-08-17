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
 * Level Triage PACS (Patient's Acuity Categorization Scale), formulir RM 03-1.
 *
 * PENTING - ini BUKAN hasil hitungan dari Total EWS. Dikonfirmasi pengguna:
 * PACS tetap keputusan klinis manual petugas (petugas memilih salah satu dari
 * 4 kotak PACS 1-4 di formulir berdasarkan penilaiannya sendiri). Total EWS
 * (lihat PacsScoring.hitungTotal/warnaTotal) adalah angka terpisah, TIDAK
 * dipetakan ke PACS - sengaja tidak dihubungkan, sesuai keputusan pengguna.
 *
 * label persis sama dengan enum kolom database `level_pacs` di
 * data_triase_pacs (lihat sik_triase_pacs.sql) - JANGAN ubah teks label tanpa
 * mengubah skema database juga.
 *
 * warnaCetak dibaca dari legenda warna tercetak di formulir kertas asli
 * (kotak PACS 1 merah, PACS 2 kuning, PACS 3 kuning-hijau, PACS 4 hijau) -
 * representasi visual utk ditampilkan di form/dicetak, bukan aturan klinis.
 * Kode warna dipilih mengikuti palet yang SUDAH dipakai project ini untuk
 * skala urgensi lain (lihat RMTriaseIGD.getData(): #AA0000/#FF0000/#C8C800/
 * #00AA00/#969696) - kecuali PACS 3 (kuning-hijau) yang belum ada padanan
 * persis di palet lama, jadi nilainya perkiraan visual dari foto formulir.
 */
public enum PacsLevel {
    PACS_1(1, "PACS 1", "Merah", "#AA0000"),
    PACS_2(2, "PACS 2", "Kuning", "#C8C800"),
    PACS_3(3, "PACS 3", "Kuning-Hijau", "#AACC00"),
    PACS_4(4, "PACS 4", "Hijau", "#00AA00");

    private final int nilai;
    private final String label;
    private final String namaWarna;
    private final String warnaCetakHex;

    PacsLevel(int nilai,String label,String namaWarna,String warnaCetakHex){
        this.nilai = nilai;
        this.label = label;
        this.namaWarna = namaWarna;
        this.warnaCetakHex = warnaCetakHex;
    }

    public int getNilai(){
        return nilai;
    }

    public String getLabel(){
        return label;
    }

    public String getNamaWarna(){
        return namaWarna;
    }

    /** Kode warna cetak dalam format hex "#RRGGBB", siap dipakai java.awt.Color.decode(...) atau parameter Jasper. */
    public String getWarnaCetakHex(){
        return warnaCetakHex;
    }

    /** Cari PacsLevel dari label persis seperti tersimpan di kolom level_pacs. Null kalau tidak cocok/kosong. */
    public static PacsLevel dariLabel(String label){
        if(label==null){
            return null;
        }
        for(PacsLevel p : values()){
            if(p.label.equals(label)){
                return p;
            }
        }
        return null;
    }

    /** Cari PacsLevel dari nilai 1-4. Null kalau di luar 1-4. */
    public static PacsLevel dariNilai(int nilai){
        for(PacsLevel p : values()){
            if(p.nilai==nilai){
                return p;
            }
        }
        return null;
    }

    /**
     * Aturan penentuan PACS: dikonfirmasi pengguna PACS adalah pilihan
     * manual petugas (bukan hasil hitungan). Fungsi ini murni membaca
     * exactly satu dari 4 pilihan checkbox/radio form dan mengembalikan
     * PacsLevel yang sesuai - tidak ada logika/skor yang dihitung di sini.
     * Null kalau belum ada satupun yang dipilih.
     */
    public static PacsLevel tentukanPacsLevel(boolean pacs1Terpilih,boolean pacs2Terpilih,boolean pacs3Terpilih,boolean pacs4Terpilih){
        if(pacs1Terpilih){
            return PACS_1;
        }else if(pacs2Terpilih){
            return PACS_2;
        }else if(pacs3Terpilih){
            return PACS_3;
        }else if(pacs4Terpilih){
            return PACS_4;
        }
        return null;
    }
}
