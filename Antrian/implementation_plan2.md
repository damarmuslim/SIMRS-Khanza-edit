# Create Missing Poli Tables

The user is encountering an error because the `simpan_antrian_poli_wira` table is missing from the `sik` database. This table is used to log patient calls in the Poliklinik.

## Proposed Changes

### Database [sik]

#### [NEW] Create `simpan_antrian_poli_wira` table
This table stores the history of patient calls per polyclinic and doctor.
```sql
CREATE TABLE IF NOT EXISTS simpan_antrian_poli_wira (
    no_rawat VARCHAR(20) NOT NULL,
    tgl_panggil DATE NOT NULL,
    no_rkm_medis VARCHAR(20),
    no_antrian VARCHAR(20),
    nm_poli VARCHAR(100),
    nm_pasien VARCHAR(100),
    kd_poli VARCHAR(20),
    kd_dokter VARCHAR(20),
    nm_dokter VARCHAR(100),
    jml_panggil INT DEFAULT 1,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (no_rawat, tgl_panggil),
    INDEX (tgl_panggil),
    INDEX (kd_poli),
    INDEX (kd_dokter)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Verification Plan

### Automated Tests
1. Verify tables exist in `sik`:
   ```powershell
   C:\xampp\mysql\bin\mysql.exe -u root -D sik -e "SHOW TABLES LIKE 'simpan_antrian_poli_wira';"
   ```

### Manual Verification
1. Open the **Display Poliklinik** page.
2. Verify that the table-not-found error no longer appears.
3. Try calling a patient from the polyclinic control panel (if available) and verify it appears on the display.
