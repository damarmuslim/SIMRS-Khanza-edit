package rekammedis;
import fungsi.*;
import java.lang.reflect.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/** Regresi Simpan RM19 terhadap data staging; seluruh perubahan dibatalkan. */
public class Rm19SimpanVerification {
    static int n;
    static Object get(Object o,String nama)throws Exception{Field f=o.getClass().getDeclaredField(nama);f.setAccessible(true);return f.get(o);}
    static void panggil(Object o,String nama)throws Exception{Method m=o.getClass().getDeclaredMethod(nama);m.setAccessible(true);m.invoke(o);}
    static void teks(Object o,String nama,String s)throws Exception{((JTextComponent)get(o,nama)).setText(s);}
    static void cek(boolean v,String s){if(!v)throw new AssertionError(s);n++;System.out.println("LULUS_RM19 "+s);}
    static void akun(String s)throws Exception{Field f=akses.class.getDeclaredField("kode");f.setAccessible(true);f.set(null,s);}
    public static void main(String[] args){try{
        widget.ThemeConfig.pasangAccentFlatLaf();com.formdev.flatlaf.FlatLightLaf.setup();widget.ThemeConfig.applyGlobalTheme();
        Connection c=koneksiDB.condb();String rawat;int awal;
        try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT DATABASE(),ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='penilaian_awal_keperawatan_ranap'")){r.next();if(!"aik".equals(r.getString(1)) || !"InnoDB".equalsIgnoreCase(r.getString(2)))throw new Exception("Hanya boleh staging aik transaksional");}
        try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT no_rawat FROM penilaian_awal_keperawatan_ranap LIMIT 1")){if(!r.next())throw new Exception("Tidak ada referensi staging");rawat=r.getString(1);}
        awal=hitung(c);c.setAutoCommit(false);akun("Admin Utama");
        try{SwingUtilities.invokeAndWait(()->{RMPenilaianAwalKeperawatanRanap f=null;try{
            f=new RMPenilaianAwalKeperawatanRanap(null,false);
            ((widget.Tanggal)get(f,"DTPCari1")).setDate(new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01"));
            teks(f,"TCari",rawat);panggil(f,"tampil");JTable t=(JTable)get(f,"tbObat");cek(t.getRowCount()>0,"data lama tersedia pada riwayat");t.setRowSelectionInterval(0,0);
            panggil(f,"getMasalah");panggil(f,"getData");panggil(f,"getDataRiwayatRM19");
            cek(rawat.equals(get(f,"noRawatEditRanap")),"memuat data lama mengaktifkan konteks edit");
            teks(f,"RPS","UJI REGRESI RM19 - ROLLBACK");
            Method simpan=f.getClass().getDeclaredMethod("BtnSimpanActionPerformed",java.awt.event.ActionEvent.class);simpan.setAccessible(true);simpan.invoke(f,new Object[]{null});
            cek(hitung(c)==awal,"Simpan mode edit tidak menambah record");
            try(PreparedStatement p=c.prepareStatement("SELECT rps FROM penilaian_awal_keperawatan_ranap WHERE no_rawat=?")){p.setString(1,rawat);try(ResultSet r=p.executeQuery()){r.next();cek("UJI REGRESI RM19 - ROLLBACK".equals(r.getString(1)),"Simpan memperbarui record yang dimuat");}}
            cek("".equals(get(f,"noRawatEditRanap")),"reset setelah edit mengakhiri konteks edit");
            Field kolom=f.getClass().getDeclaredField("KOLOM_PENILAIAN_RANAP");kolom.setAccessible(true);String cols=(String)kolom.get(null);String[] nilai=new String[cols.split(",").length];
            try(PreparedStatement p=c.prepareStatement("SELECT "+cols+" FROM penilaian_awal_keperawatan_ranap WHERE no_rawat=?")){p.setString(1,rawat);try(ResultSet r=p.executeQuery()){r.next();for(int i=0;i<nilai.length;i++)nilai[i]=r.getString(i+1);}}
            Method simpanBaru=f.getClass().getDeclaredMethod("simpanRanap",String[].class);simpanBaru.setAccessible(true);
            final RMPenilaianAwalKeperawatanRanap form=f;
            javax.swing.Timer penutup=new javax.swing.Timer(150,e->{for(java.awt.Window w:form.getOwnedWindows())if(w instanceof JDialog && w.isVisible())w.dispose();});penutup.start();
            try{cek(Boolean.FALSE.equals(simpanBaru.invoke(f,new Object[]{nilai})),"input baru menolak nomor rawat yang sudah tersimpan");}finally{penutup.stop();}
            cek(hitung(c)==awal,"penolakan duplikat tidak mengubah jumlah data");
            cek("UJI REGRESI RM19 - ROLLBACK".equals(nilaiRps(c,rawat)),"penolakan duplikat mempertahankan data lama");
            teks(f,"RPS","KETIKAN BELUM DISIMPAN");teks(f,"TNoRw","RAWAT-BERBEDA");penutup.start();
            try{Method edit=f.getClass().getDeclaredMethod("BtnEditActionPerformed",java.awt.event.ActionEvent.class);edit.setAccessible(true);edit.invoke(f,new Object[]{null});}finally{penutup.stop();}
            cek("KETIKAN BELUM DISIMPAN".equals(((JTextComponent)get(f,"RPS")).getText()),"konteks berbeda ditolak tanpa menghapus ketikan");
            try(Statement p=c.createStatement();ResultSet r=p.executeQuery("SELECT r.no_rawat FROM reg_periksa r LEFT JOIN penilaian_awal_keperawatan_ranap a ON a.no_rawat=r.no_rawat WHERE a.no_rawat IS NULL LIMIT 1")){if(!r.next())throw new Exception("Tidak ada referensi kunjungan baru");nilai[0]=r.getString(1);}
            cek(Boolean.TRUE.equals(simpanBaru.invoke(f,new Object[]{nilai})),"pengkajian baru tetap dapat disimpan dengan 516 kolom");
            cek(hitung(c)==awal+1,"input baru menambah tepat satu record");
        }catch(Throwable e){throw new RuntimeException(e);}finally{if(f!=null)f.dispose();}});}finally{c.rollback();c.setAutoCommit(true);akun("");}
        cek(hitung(c)==awal,"jumlah record tetap setelah rollback");System.out.println("RM19_SIMPAN_OK pemeriksaan="+n);System.exit(0);
    }catch(Throwable e){e.printStackTrace();System.exit(1);}}
    static int hitung(Connection c)throws Exception{try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM penilaian_awal_keperawatan_ranap")){r.next();return r.getInt(1);}}
    static String nilaiRps(Connection c,String rawat)throws Exception{try(PreparedStatement p=c.prepareStatement("SELECT rps FROM penilaian_awal_keperawatan_ranap WHERE no_rawat=?")){p.setString(1,rawat);try(ResultSet r=p.executeQuery()){r.next();return r.getString(1);}}}
}
