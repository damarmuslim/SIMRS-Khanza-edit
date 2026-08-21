package widget;

import java.awt.Color;
import java.util.Date;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

/**
 *
 * @author khanzasoft
 */
public class Jam extends JSpinner {

    public Jam() {
        super(new SpinnerDateModel());
        setEditor(new JSpinner.DateEditor(this, "HH:mm:ss"));
        setFont(new java.awt.Font("Segoe UI", 0, 11));
        setForeground(new Color(50,50,50));
        setBackground(new Color(255,255,255));
    }

    public Date getJam() {
        return (Date) getValue();
    }

    public void setJam(Date jam) {
        setValue(jam);
    }

    public String getTeksJam() {
        JSpinner.DateEditor editor = (JSpinner.DateEditor) getEditor();
        return editor.getFormat().format(getJam());
    }

    /**
     * Ganti editor supaya tanggal ikut ditampilkan & bisa diedit (bukan
     * cuma jam) - dipakai field yang tanggalnya bisa beda dari tanggal
     * kunjungan (mis. pasien dipulangkan/meninggal lewat tengah malam).
     */
    public void tampilkanTanggal() {
        setEditor(new JSpinner.DateEditor(this, "dd-MM-yyyy HH:mm:ss"));
    }

    /**
     * Format tetap "yyyy-MM-dd HH:mm:ss" (format DATETIME MySQL),
     * independen dari format yang sedang ditampilkan di layar - dipakai
     * saat simpan ke database.
     */
    public String getTeksSql() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getJam());
    }
}
