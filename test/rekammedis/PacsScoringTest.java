package rekammedis;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit test batas nilai untuk setiap kategori skor PACS (PacsScoring).
 * Aturan yang diuji sudah dikonfirmasi dari formulir RM 03-1 - lihat
 * komentar di PacsScoring.java. Fokus test: nilai tepat di garis batas
 * tiap kategori, bukan cuma nilai "aman" di tengah rentang.
 */
public class PacsScoringTest {

    // ---------- Kesadaran ----------

    @Test
    public void kesadaran_sadarPenuh_skor0(){
        assertEquals(0, PacsScoring.skorKesadaran("Sadar Penuh"));
    }

    @Test
    public void kesadaran_responSuara_skor3(){
        assertEquals(3, PacsScoring.skorKesadaran("Respon Suara"));
    }

    @Test
    public void kesadaran_responNyeri_skor3(){
        assertEquals(3, PacsScoring.skorKesadaran("Respon Nyeri"));
    }

    @Test
    public void kesadaran_tidakRespon_skor3(){
        assertEquals(3, PacsScoring.skorKesadaran("Tidak Respon"));
    }

    @Test
    public void kesadaran_null_skor3(){
        assertEquals(3, PacsScoring.skorKesadaran(null));
    }

    // ---------- Tekanan Darah Sistolik (batas: 99 / 100) ----------

    @Test
    public void tdSistolik_tepat100_skor0(){
        assertEquals(0, PacsScoring.skorTekananDarahSistolik(100));
    }

    @Test
    public void tdSistolik_tepat99_skor2(){
        assertEquals(2, PacsScoring.skorTekananDarahSistolik(99));
    }

    @Test
    public void tdSistolik_jauhDiAtas100_skor0(){
        assertEquals(0, PacsScoring.skorTekananDarahSistolik(150));
    }

    @Test
    public void tdSistolik_jauhDiBawah99_skor2(){
        assertEquals(2, PacsScoring.skorTekananDarahSistolik(50));
    }

    // ---------- Nadi (batas: <=101 skor 0, >=102 skor 1) ----------

    @Test
    public void nadi_tepat101_skor0(){
        assertEquals(0, PacsScoring.skorNadi(101));
    }

    @Test
    public void nadi_dibawah101_skor0(){
        assertEquals(0, PacsScoring.skorNadi(60));
    }

    @Test
    public void nadi_tepat102_skor1(){
        assertEquals(1, PacsScoring.skorNadi(102));
    }

    @Test
    public void nadi_tepat103_skor1(){
        assertEquals(1, PacsScoring.skorNadi(103));
    }

    @Test
    public void nadi_jauhDiAtas103_skor1(){
        assertEquals(1, PacsScoring.skorNadi(140));
    }

    // ---------- Respirasi (batas: 19 / 20-21 / 22) ----------

    @Test
    public void respirasi_tepat19_skor0(){
        assertEquals(0, PacsScoring.skorRespirasi(19));
    }

    @Test
    public void respirasi_tepat20_skor1(){
        assertEquals(1, PacsScoring.skorRespirasi(20));
    }

    @Test
    public void respirasi_tepat21_skor1(){
        assertEquals(1, PacsScoring.skorRespirasi(21));
    }

    @Test
    public void respirasi_tepat22_skor2(){
        assertEquals(2, PacsScoring.skorRespirasi(22));
    }

    @Test
    public void respirasi_jauhDiAtas22_skor2(){
        assertEquals(2, PacsScoring.skorRespirasi(40));
    }

    @Test
    public void respirasi_sangatRendah_skor0(){
        assertEquals(0, PacsScoring.skorRespirasi(5));
    }

    // ---------- Temperatur (batas: 35,3) ----------

    @Test
    public void temperatur_tepat35_3_skor0(){
        assertEquals(0, PacsScoring.skorTemperatur(35.3));
    }

    @Test
    public void temperatur_35_29_skor3(){
        assertEquals(3, PacsScoring.skorTemperatur(35.29));
    }

    @Test
    public void temperatur_jauhDiAtas35_3_skor0(){
        assertEquals(0, PacsScoring.skorTemperatur(39.0));
    }

    @Test
    public void temperatur_jauhDiBawah35_3_skor3(){
        assertEquals(3, PacsScoring.skorTemperatur(30.0));
    }

    // ---------- Saturasi O2 (batas: 92 / 93-94 / 95-96) ----------

    @Test
    public void saturasi_tepat96_skor0(){
        assertEquals(0, PacsScoring.skorSaturasiO2(96));
    }

    @Test
    public void saturasi_tepat100_skor0(){
        assertEquals(0, PacsScoring.skorSaturasiO2(100));
    }

    @Test
    public void saturasi_tepat95_skor1(){
        assertEquals(1, PacsScoring.skorSaturasiO2(95));
    }

    @Test
    public void saturasi_tepat94_skor1(){
        assertEquals(1, PacsScoring.skorSaturasiO2(94));
    }

    @Test
    public void saturasi_tepat93_skor2(){
        assertEquals(2, PacsScoring.skorSaturasiO2(93));
    }

    @Test
    public void saturasi_tepat92_skor2(){
        assertEquals(2, PacsScoring.skorSaturasiO2(92));
    }

    @Test
    public void saturasi_tepat91_skor3(){
        assertEquals(3, PacsScoring.skorSaturasiO2(91));
    }

    @Test
    public void saturasi_sangatRendah_skor3(){
        assertEquals(3, PacsScoring.skorSaturasiO2(50));
    }

    // ---------- Total ----------

    @Test
    public void total_semuaNol(){
        assertEquals(0, PacsScoring.hitungTotal(0,0,0,0,0,0));
    }

    @Test
    public void total_campuran(){
        assertEquals(9, PacsScoring.hitungTotal(3,2,0,2,0,2));
    }

    // ---------- Warna Total (batas: 1/2, 4/5) ----------

    @Test
    public void warnaTotal_tepat0_hijau(){
        assertEquals("Hijau", PacsScoring.warnaTotal(0));
    }

    @Test
    public void warnaTotal_tepat1_hijau(){
        assertEquals("Hijau", PacsScoring.warnaTotal(1));
    }

    @Test
    public void warnaTotal_tepat2_kuning(){
        assertEquals("Kuning", PacsScoring.warnaTotal(2));
    }

    @Test
    public void warnaTotal_tepat4_kuning(){
        assertEquals("Kuning", PacsScoring.warnaTotal(4));
    }

    @Test
    public void warnaTotal_tepat5_merah(){
        assertEquals("Merah", PacsScoring.warnaTotal(5));
    }

    @Test
    public void warnaTotal_jauhDiAtas5_merah(){
        assertEquals("Merah", PacsScoring.warnaTotal(15));
    }
}
