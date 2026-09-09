-- =====================================================
-- QUERY PERBAIKAN LENGKAP
-- Tabel: satu_sehat_mapping_obat
-- =====================================================

-- BACKUP DATA SEBELUM UPDATE!
-- CREATE TABLE satu_sehat_mapping_obat_backup AS SELECT * FROM satu_sehat_mapping_obat;

-- =====================================================
-- BAGIAN 1: PERBAIKI DENOMINATOR_CODE
-- =====================================================

-- 1.1. Perbaiki INHLPWD menjadi IVSOL (untuk vial/ampul/injeksi)
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'IVSOL'
WHERE denominator_code = 'INHLPWD'
  AND (LOWER(form_display) LIKE '%vial%' 
       OR LOWER(form_display) LIKE '%ampul%'
       OR LOWER(form_display) LIKE '%injeksi%'
       OR LOWER(form_display) LIKE '%injection%');

-- 1.2. Perbaiki INVSOL menjadi IVSOL (typo)
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'IVSOL'
WHERE denominator_code = 'INVSOL';

-- 1.3. Perbaiki SYR menjadi SYRUP
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'SYRUP'
WHERE denominator_code = 'SYR';

-- 1.4. Update denominator_code berdasarkan form_display - TABLET
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'TAB'
WHERE LOWER(form_display) = 'tablet'
  AND denominator_code != 'TAB';

-- 1.5. Update denominator_code berdasarkan form_display - KAPLET
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'CAPLET'
WHERE LOWER(form_display) LIKE '%kaplet%'
  AND denominator_code != 'CAPLET';

-- 1.6. Update denominator_code berdasarkan form_display - KAPSUL
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'CAP'
WHERE LOWER(form_display) LIKE '%kapsul%'
  AND denominator_code != 'CAP';

-- 1.7. Update denominator_code berdasarkan form_display - SIRUP/SYRUP
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'SYRUP'
WHERE (LOWER(form_display) LIKE '%sirup%' 
       OR LOWER(form_display) LIKE '%syrup%'
       OR LOWER(form_display) LIKE '%syr%')
  AND LOWER(form_display) NOT LIKE '%kering%'
  AND denominator_code != 'SYRUP';

-- 1.8. Update denominator_code - SUSPENSI
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'ORSUSP'
WHERE LOWER(form_display) LIKE '%suspensi%'
  AND denominator_code NOT IN ('ORSUSP', 'SUSP');

-- 1.9. Update denominator_code - BOTOL (cairan oral)
UPDATE satu_sehat_mapping_obat 
SET denominator_code = CASE 
    WHEN LOWER(obat_display) LIKE '%sirup%' OR LOWER(obat_display) LIKE '%syrup%' THEN 'SYRUP'
    WHEN LOWER(obat_display) LIKE '%suspensi%' OR LOWER(obat_display) LIKE '%suspension%' THEN 'ORSUSP'
    WHEN LOWER(obat_display) LIKE '%larutan%' OR LOWER(obat_display) LIKE '%solution%' THEN 'ORALSOL'
    WHEN LOWER(obat_display) LIKE '%eliksir%' OR LOWER(obat_display) LIKE '%elixir%' THEN 'ELIXIR'
    ELSE 'ORALSOL'
END
WHERE LOWER(form_display) = 'botol'
  AND route_code = 'O';

-- 1.10. Update denominator_code - TUBE (semi-solid topikal)
UPDATE satu_sehat_mapping_obat 
SET denominator_code = CASE 
    WHEN LOWER(obat_display) LIKE '%gel%' OR LOWER(obat_display) LIKE '%jelly%' THEN 'GEL'
    WHEN LOWER(obat_display) LIKE '%cream%' OR LOWER(obat_display) LIKE '%krim%' THEN 'CRM'
    WHEN LOWER(obat_display) LIKE '%salep%' OR LOWER(obat_display) LIKE '%ointment%' THEN 'OINT'
    WHEN LOWER(obat_display) LIKE '%pasta%' OR LOWER(obat_display) LIKE '%paste%' THEN 'PASTE'
    ELSE 'TOPCRM'
END
WHERE LOWER(form_display) = 'tube'
  AND (route_code = 'OINTMENT' OR LOWER(route_display) LIKE '%topikal%');

-- 1.11. Update denominator_code - VIAL (injeksi)
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'IVSOL'
WHERE LOWER(form_display) LIKE '%vial%'
  AND denominator_code != 'IVSOL';

-- 1.12. Update denominator_code - AMPUL (injeksi)
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'IVSOL'
WHERE LOWER(form_display) LIKE '%ampul%'
  AND denominator_code != 'IVSOL';

-- 1.13. Update denominator_code - SYRINGE (injeksi pre-filled)
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'IVSOL'
WHERE LOWER(form_display) LIKE '%syringe%'
  AND denominator_code != 'IVSOL';

-- 1.14. Update denominator_code - INJEKSI (generic)
UPDATE satu_sehat_mapping_obat 
SET denominator_code = 'IVSOL'
WHERE LOWER(form_display) = 'injeksi'
  AND denominator_code != 'IVSOL';


-- =====================================================
-- BAGIAN 2: PERBAIKI FORM_CODE
-- =====================================================

-- 2.1. Perbaiki form_code untuk TABLET (BS066)
UPDATE satu_sehat_mapping_obat 
SET form_code = 'BS066'
WHERE LOWER(form_display) = 'tablet'
  AND form_code NOT IN ('BS066', 'BS023', 'BS024', 'BS025', 'BS026', 'BS027', 'BS028', 'BS029', 'BS030');

-- 2.2. Perbaiki form_code untuk KAPLET (BS023)
UPDATE satu_sehat_mapping_obat 
SET form_code = 'BS023'
WHERE LOWER(form_display) LIKE '%kaplet%'
  AND form_code != 'BS023';

-- 2.3. Perbaiki form_code untuk KAPSUL (BS032)
UPDATE satu_sehat_mapping_obat 
SET form_code = 'BS032'
WHERE LOWER(form_display) LIKE '%kapsul%'
  AND LOWER(form_display) NOT LIKE '%lunak%'
  AND form_code != 'BS032';

-- 2.4. Perbaiki form_code untuk SIRUP (BS046)
UPDATE satu_sehat_mapping_obat 
SET form_code = 'BS046'
WHERE (LOWER(form_display) LIKE '%sirup%' OR LOWER(form_display) LIKE '%syrup%')
  AND LOWER(form_display) NOT LIKE '%kering%'
  AND form_code != 'BS046';

-- 2.5. Perbaiki form_code untuk BOTOL berisi sirup
UPDATE satu_sehat_mapping_obat 
SET form_code = 'BS046'
WHERE LOWER(form_display) = 'botol'
  AND (LOWER(obat_display) LIKE '%sirup%' OR LOWER(obat_display) LIKE '%syrup%')
  AND route_code = 'O';

-- 2.6. Perbaiki form_code untuk BOTOL berisi suspensi
UPDATE satu_sehat_mapping_obat 
SET form_code = 'BS051'
WHERE LOWER(form_display) = 'botol'
  AND (LOWER(obat_display) LIKE '%suspensi%' OR LOWER(obat_display) LIKE '%suspension%')
  AND route_code = 'O';

-- 2.7. Perbaiki form_code untuk VIAL (BS064)
UPDATE satu_sehat_mapping_obat 
SET form_code = 'BS064'
WHERE LOWER(form_display) LIKE '%vial%'
  AND form_code != 'BS064';

-- 2.8. Perbaiki form_code untuk AMPUL (BS065)
UPDATE satu_sehat_mapping_obat 
SET form_code = 'BS065'
WHERE LOWER(form_display) LIKE '%ampul%'
  AND form_code != 'BS065';

-- 2.9. Perbaiki form_code untuk SYRINGE (BS067)
UPDATE satu_sehat_mapping_obat 
SET form_code = 'BS067'
WHERE LOWER(form_display) LIKE '%syringe%'
  AND form_code != 'BS067';

-- 2.10. Perbaiki form_code untuk INJEKSI generic (BS063)
UPDATE satu_sehat_mapping_obat 
SET form_code = 'BS063'
WHERE LOWER(form_display) = 'injeksi'
  AND form_code != 'BS063';

-- 2.11. Perbaiki form_code untuk TUBE topikal (BS013)
UPDATE satu_sehat_mapping_obat 
SET form_code = 'BS013'
WHERE LOWER(form_display) = 'tube'
  AND (route_code = 'OINTMENT' OR LOWER(route_display) LIKE '%topikal%')
  AND form_code != 'BS013';


-- =====================================================
-- BAGIAN 3: STANDARDISASI FORM_DISPLAY
-- =====================================================

-- 3.1. Standardisasi kapitalisasi form_display
UPDATE satu_sehat_mapping_obat 
SET form_display = CONCAT(UPPER(SUBSTRING(form_display, 1, 1)), LOWER(SUBSTRING(form_display, 2)))
WHERE form_display != CONCAT(UPPER(SUBSTRING(form_display, 1, 1)), LOWER(SUBSTRING(form_display, 2)));

-- 3.2. Perbaiki typo umum
UPDATE satu_sehat_mapping_obat SET form_display = 'Tablet' WHERE LOWER(form_display) = 'tablet';
UPDATE satu_sehat_mapping_obat SET form_display = 'Kaplet' WHERE LOWER(form_display) = 'kaplet';
UPDATE satu_sehat_mapping_obat SET form_display = 'Kapsul' WHERE LOWER(form_display) = 'kapsul';
UPDATE satu_sehat_mapping_obat SET form_display = 'Sirup' WHERE LOWER(form_display) IN ('sirup', 'syrup');
UPDATE satu_sehat_mapping_obat SET form_display = 'Vial' WHERE LOWER(form_display) = 'vial';
UPDATE satu_sehat_mapping_obat SET form_display = 'Ampul' WHERE LOWER(form_display) = 'ampul';
UPDATE satu_sehat_mapping_obat SET form_display = 'Injeksi' WHERE LOWER(form_display) = 'injeksi';
UPDATE satu_sehat_mapping_obat SET form_display = 'Botol' WHERE LOWER(form_display) = 'botol';
UPDATE satu_sehat_mapping_obat SET form_display = 'Tube' WHERE LOWER(form_display) = 'tube';
UPDATE satu_sehat_mapping_obat SET form_display = 'Syringe' WHERE LOWER(form_display) = 'syringe';


-- =====================================================
-- BAGIAN 4: VERIFIKASI HASIL
-- =====================================================

-- 4.1. Cek data yang masih bermasalah
SELECT 
    'Masalah: denominator_code tidak valid' as issue_type,
    kode_brng, 
    obat_display, 
    form_display, 
    denominator_code
FROM satu_sehat_mapping_obat
WHERE denominator_code NOT IN (
    'TAB', 'CAPLET', 'CAP', 'ERCAP', 'ERCAP12', 'ERCAP24',
    'SUPP', 'RECSUPP', 'VAGSUPP', 'PATCH', 'TPATCH',
    'SYRUP', 'ORALSOL', 'ELIXIR', 'SUSP', 'ORSUSP', 'ERSUSP',
    'DROP', 'OPDROP', 'OTDROP', 'NDROP', 'ORDROP',
    'IVSOL', 'SOL', 'IPSOL', 'IRSOL',
    'CRM', 'TOPCRM', 'VAGCRM', 'OINT', 'TOPOINT', 'OPOINT',
    'GEL', 'TOPGEL', 'VAGGEL', 'LTN', 'TOPLTN', 'PASTE',
    'AER', 'NASSPRY', 'DERMSPRY', 'INHL', 'MDINHL', 'FOAM',
    'GRAN', 'POWD', 'TOPPWD'
)

UNION ALL

-- 4.2. Cek form_code vs form_display yang tidak match
SELECT 
    'Masalah: form_code tidak sesuai form_display' as issue_type,
    kode_brng, 
    obat_display, 
    CONCAT(form_code, ' - ', form_display) as form_info,
    denominator_code
FROM satu_sehat_mapping_obat
WHERE 
    (form_code = 'BS066' AND LOWER(form_display) != 'tablet') OR
    (form_code = 'BS023' AND LOWER(form_display) NOT LIKE '%kaplet%') OR
    (form_code = 'BS032' AND LOWER(form_display) NOT LIKE '%kapsul%') OR
    (form_code = 'BS046' AND LOWER(form_display) NOT LIKE '%sirup%' AND LOWER(form_display) NOT LIKE '%syrup%') OR
    (form_code = 'BS064' AND LOWER(form_display) NOT LIKE '%vial%') OR
    (form_code = 'BS065' AND LOWER(form_display) NOT LIKE '%ampul%')

UNION ALL

-- 4.3. Cek data NULL
SELECT 
    'Masalah: field NULL' as issue_type,
    kode_brng, 
    obat_display, 
    form_display,
    CASE 
        WHEN form_code IS NULL THEN 'form_code NULL'
        WHEN denominator_code IS NULL THEN 'denominator_code NULL'
    END as denominator_code
FROM satu_sehat_mapping_obat
WHERE form_code IS NULL OR denominator_code IS NULL;


-- =====================================================
-- BAGIAN 5: RINGKASAN HASIL UPDATE
-- =====================================================

-- 5.1. Ringkasan per form_display
SELECT 
    form_display,
    form_code,
    denominator_code,
    COUNT(*) as jumlah
FROM satu_sehat_mapping_obat
GROUP BY form_display, form_code, denominator_code
ORDER BY form_display, jumlah DESC;

-- 5.2. Ringkasan per denominator_code
SELECT 
    denominator_code,
    COUNT(*) as jumlah,
    GROUP_CONCAT(DISTINCT form_display SEPARATOR ', ') as form_displays
FROM satu_sehat_mapping_obat
GROUP BY denominator_code
ORDER BY jumlah DESC;

-- 5.3. Cek total records
SELECT 
    COUNT(*) as total_records,
    COUNT(DISTINCT kode_brng) as unique_drugs,
    COUNT(DISTINCT form_display) as unique_forms,
    COUNT(DISTINCT denominator_code) as unique_denominators
FROM satu_sehat_mapping_obat;


-- =====================================================
-- COMMIT CHANGES
-- =====================================================

COMMIT;

-- =====================================================
-- CATATAN PENGGUNAAN:
-- =====================================================

/*
1. BACKUP DATA DULU!
   CREATE TABLE satu_sehat_mapping_obat_backup AS SELECT * FROM satu_sehat_mapping_obat;

2. Jalankan query ini secara berurutan
   - Bagian 1: Perbaiki denominator_code
   - Bagian 2: Perbaiki form_code
   - Bagian 3: Standardisasi form_display
   - Bagian 4: Verifikasi hasil

3. Review hasil verifikasi di Bagian 4
   - Jika ada yang masih salah, perbaiki manual

4. Jika sudah OK, COMMIT

5. Untuk rollback jika ada masalah:
   TRUNCATE TABLE satu_sehat_mapping_obat;
   INSERT INTO satu_sehat_mapping_obat SELECT * FROM satu_sehat_mapping_obat_backup;
*/
