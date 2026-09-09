# Create Missing Database Tables

Resolve SQL errors caused by missing tables in the `sik` database. This plan consolidates three previous plans found in the workspace.

## Proposed Changes

### Database [sik]

#### [NEW] Create `antrian_wira` table
Manages the general queue for various services.
```sql
CREATE TABLE IF NOT EXISTS antrian_wira (
    id INT AUTO_INCREMENT PRIMARY KEY,
    jenis VARCHAR(50) NOT NULL,
    nomor VARCHAR(20) NOT NULL,
    no_rkm_medis VARCHAR(20),
    status VARCHAR(50) DEFAULT 'Menunggu',
    loket_id INT,
    waktu_panggil DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX (jenis),
    INDEX (status),
    INDEX (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### [NEW] Create `loket_admisi_wira` table
Defines service counters for Admisi.
```sql
CREATE TABLE IF NOT EXISTS loket_admisi_wira (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nama_loket VARCHAR(100) NOT NULL,
    keterangan VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### [NEW] Create `simpan_antrian_poli_wira` table
Stores history of patient calls for Poliklinik.
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

#### [NEW] Create `simpan_antrian_farmasi_wira` table
Stores history of patient calls for Pharmacy.
```sql
CREATE TABLE IF NOT EXISTS simpan_antrian_farmasi_wira (
    no_resep VARCHAR(20) NOT NULL,
    tgl_panggil DATE NOT NULL,
    no_rawat VARCHAR(20),
    no_rkm_medis VARCHAR(20),
    no_antrian VARCHAR(20),
    nm_pasien VARCHAR(100),
    nm_poli VARCHAR(100),
    nm_dokter VARCHAR(100),
    jenis_resep VARCHAR(50),
    jml_panggil INT DEFAULT 1,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (no_resep, tgl_panggil),
    INDEX (tgl_panggil),
    INDEX (jenis_resep)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### [NEW] Create `antriapotek3` table
Required for prescription handover process.
```sql
CREATE TABLE IF NOT EXISTS antriapotek3 (
    no_resep VARCHAR(20) NOT NULL,
    status ENUM('0','1','2') NOT NULL DEFAULT '0',
    no_rawat VARCHAR(20) NOT NULL,
    PRIMARY KEY (no_resep)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Verification Plan

### Automated Tests
1. Run a script to verify all tables exist:
   ```powershell
   C:\xampp\mysql\bin\mysql.exe -u root -D sik -e "SHOW TABLES LIKE 'antrian_wira'; SHOW TABLES LIKE 'loket_admisi_wira'; SHOW TABLES LIKE 'simpan_antrian_poli_wira'; SHOW TABLES LIKE 'simpan_antrian_farmasi_wira'; SHOW TABLES LIKE 'antriapotek3';"
   ```

### Manual Verification
1. **Admisi**: Open "Antrian Admisi" and take a queue number. Verify no SQL errors.
2. **Poli**: Open "Display Poliklinik" and verify no "table not found" errors.
3. **Farmasi**: Open "Data Antrian Farmasi", try calling a patient and clicking the handover (camera) icon.
