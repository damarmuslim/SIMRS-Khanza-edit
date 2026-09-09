# Create Missing SIMRS Tables

The user is encountering errors because the `antrian_wira` and `loket_admisi_wira` tables are missing from the `sik` database. I will create these tables based on their usage in the codebase.

## Proposed Changes

INSERT INTO setting_simrs (nama_simrs, host, username, password, database_name, updated_at) 
VALUES ('SIMRS Khanza', 'localhost', 'root', '', 'sik', NOW());

### Database [sik]

#### [NEW] Create `antrian_wira` table
This table manages the queue for various services (Admisi, Farmasi, etc.).
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
This table defines the available service counters for Admisi.
```sql
CREATE TABLE IF NOT EXISTS loket_admisi_wira (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nama_loket VARCHAR(100) NOT NULL,
    keterangan VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Verification Plan

### Automated Tests
1. Verify tables exist in `sik`:
   ```powershell
   C:\xampp\mysql\bin\mysql.exe -u root -D sik -e "SHOW TABLES LIKE 'antrian_wira'; SHOW TABLES LIKE 'loket_admisi_wira';"
   ```
2. Test the reset functionality in [reset_antri.php](file:///c:/xampp/htdocs/MediFix/reset_antri.php) (if possible) or verify the error no longer appears in logs.

### Manual Verification
1. Open the "Reset Antrian" menu and perform a reset.
2. Open "Setting Loket Admisi" and add a new loket.
3. Open "Antrian Admisi" and take a new queue number.



