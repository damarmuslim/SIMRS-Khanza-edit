package rekammedis;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit test untuk PacsLevel: nilai, label, warna cetak, dan fungsi
 * penentuan (tentukanPacsLevel). PACS adalah pilihan manual petugas
 * (dikonfirmasi pengguna) - jadi tidak ada test "skor -> PACS", hanya
 * test bahwa struktur data & fungsi pembaca pilihan berjalan benar.
 */
public class PacsLevelTest {

    // ---------- nilai + label + warna per level ----------

    @Test
    public void pacs1_nilaiLabelWarna(){
        assertEquals(1, PacsLevel.PACS_1.getNilai());
        assertEquals("PACS 1", PacsLevel.PACS_1.getLabel());
        assertEquals("Merah", PacsLevel.PACS_1.getNamaWarna());
        assertEquals("#AA0000", PacsLevel.PACS_1.getWarnaCetakHex());
    }

    @Test
    public void pacs2_nilaiLabelWarna(){
        assertEquals(2, PacsLevel.PACS_2.getNilai());
        assertEquals("PACS 2", PacsLevel.PACS_2.getLabel());
        assertEquals("Kuning", PacsLevel.PACS_2.getNamaWarna());
        assertEquals("#C8C800", PacsLevel.PACS_2.getWarnaCetakHex());
    }

    @Test
    public void pacs3_nilaiLabelWarna(){
        assertEquals(3, PacsLevel.PACS_3.getNilai());
        assertEquals("PACS 3", PacsLevel.PACS_3.getLabel());
        assertEquals("Kuning-Hijau", PacsLevel.PACS_3.getNamaWarna());
        assertEquals("#AACC00", PacsLevel.PACS_3.getWarnaCetakHex());
    }

    @Test
    public void pacs4_nilaiLabelWarna(){
        assertEquals(4, PacsLevel.PACS_4.getNilai());
        assertEquals("PACS 4", PacsLevel.PACS_4.getLabel());
        assertEquals("Hijau", PacsLevel.PACS_4.getNamaWarna());
        assertEquals("#00AA00", PacsLevel.PACS_4.getWarnaCetakHex());
    }

    // ---------- dariLabel ----------

    @Test
    public void dariLabel_pacs1(){
        assertEquals(PacsLevel.PACS_1, PacsLevel.dariLabel("PACS 1"));
    }

    @Test
    public void dariLabel_pacs2(){
        assertEquals(PacsLevel.PACS_2, PacsLevel.dariLabel("PACS 2"));
    }

    @Test
    public void dariLabel_pacs3(){
        assertEquals(PacsLevel.PACS_3, PacsLevel.dariLabel("PACS 3"));
    }

    @Test
    public void dariLabel_pacs4(){
        assertEquals(PacsLevel.PACS_4, PacsLevel.dariLabel("PACS 4"));
    }

    @Test
    public void dariLabel_tidakDikenal_null(){
        assertNull(PacsLevel.dariLabel("PACS 5"));
    }

    @Test
    public void dariLabel_null_null(){
        assertNull(PacsLevel.dariLabel(null));
    }

    @Test
    public void dariLabel_kosong_null(){
        assertNull(PacsLevel.dariLabel(""));
    }

    // ---------- dariNilai ----------

    @Test
    public void dariNilai_1(){
        assertEquals(PacsLevel.PACS_1, PacsLevel.dariNilai(1));
    }

    @Test
    public void dariNilai_4(){
        assertEquals(PacsLevel.PACS_4, PacsLevel.dariNilai(4));
    }

    @Test
    public void dariNilai_0_null(){
        assertNull(PacsLevel.dariNilai(0));
    }

    @Test
    public void dariNilai_5_null(){
        assertNull(PacsLevel.dariNilai(5));
    }

    @Test
    public void dariNilai_negatif_null(){
        assertNull(PacsLevel.dariNilai(-1));
    }

    // ---------- tentukanPacsLevel (pembacaan pilihan manual, bukan hitungan) ----------

    @Test
    public void tentukan_pacs1Terpilih(){
        assertEquals(PacsLevel.PACS_1, PacsLevel.tentukanPacsLevel(true,false,false,false));
    }

    @Test
    public void tentukan_pacs2Terpilih(){
        assertEquals(PacsLevel.PACS_2, PacsLevel.tentukanPacsLevel(false,true,false,false));
    }

    @Test
    public void tentukan_pacs3Terpilih(){
        assertEquals(PacsLevel.PACS_3, PacsLevel.tentukanPacsLevel(false,false,true,false));
    }

    @Test
    public void tentukan_pacs4Terpilih(){
        assertEquals(PacsLevel.PACS_4, PacsLevel.tentukanPacsLevel(false,false,false,true));
    }

    @Test
    public void tentukan_belumAdaYangDipilih_null(){
        assertNull(PacsLevel.tentukanPacsLevel(false,false,false,false));
    }
}
