-- =====================================================================
-- ALAT DIAGNOSA: capaian Antrean Online (Antrol) BPJS
-- RSUD Demang Sepulau Raya (0803R001)
--
-- SELURUHNYA HANYA MEMBACA (SELECT). Tidak mengubah, menambah, atau
-- menghapus apa pun. Aman dijalankan di PRODUKSI pada jam kerja.
--
-- CARA PAKAI: jalankan BERURUTAN dari 1 ke 3. Query 1 hampir selalu sudah
-- cukup menjawab; lanjut ke 2 dan 3 hanya kalau 1 belum menjelaskan.
-- Ganti rentang tanggalnya sesuai periode laporan BPJS.
--
-- ---------------------------------------------------------------------
-- CATATAN PENTING DARI PENELUSURAN 2026-08-28
--
-- Modul Khanza ERM baru aktif 26 Agustus 2026. Sebelum tanggal itu antrean
-- pasien BPJS ONSITE tidak pernah terkirim sama sekali - nol selama 25 hari
-- (1-25 Agustus). Capaian 38,95% pada laporan BPJS periode itu adalah
-- WARISAN masa sebelum modul aktif, bukan cerminan keadaan sesudahnya.
--
-- Sesudah aktif: 26 Agt 18/18 onsite (100%), 27 Agt 24/37, 28 Agt 7/20.
--
-- Dua hipotesis SALAH yang sempat dikejar - jangan diulang:
--   (a) "rujukan kedaluwarsa"  -> terbantah: rujukan 89 hari BERHASIL
--       (dikirim sbg Kontrol), rujukan 7 hari DITOLAK (dikirim sbg Rujukan
--       FKTP). Umur rujukan bukan pembedanya.
--   (b) "pemetaan dokter/poli belum lengkap" -> terbantah: 1226 SEP dengan
--       dokter+poli+jadwal lengkap, hanya 114 berhasil.
-- Yang akhirnya menjelaskan semuanya adalah SEBARAN PER HARI (query 1).
-- ---------------------------------------------------------------------
--
-- DUA HAL YANG WAJIB DIINGAT SAAT MEMBACA HASILNYA
--
-- 1. SEP RJTL = `bridging_sep` dengan jnspelayanan='2'. Tanpa penyaring ini
--    angkanya tercampur rawat inap dan tidak akan pernah cocok dengan BPJS.
--
-- 2. Baris di `referensi_mobilejkn_bpjs_taskid` HANYA bertahan kalau BPJS
--    menjawab 200 - kode menghapusnya lagi bila ditolak (frmUtama.java
--    ~baris 418 di KhanzaHMSServiceMobileJKNERM). Jadi tabel itu bukti sah
--    "diterima BPJS", bukan catatan lokal yang optimistis.
--
-- Pemisah onsite vs MJKN: pasien yang ADA di `referensi_mobilejkn_bpjs`
-- = booking lewat Mobile JKN; yang TIDAK ada = onsite.
-- =====================================================================


-- =====================================================================
-- QUERY 1 - SEBARAN PER HARI.  MULAI DARI SINI.
--
-- Inilah yang memecahkan kasus Agustus 2026. Cara membacanya:
--   * Ada hari-hari 0% beruntun lalu tiba-tiba tinggi
--       -> layanannya memang belum jalan pada hari-hari itu. Bukan masalah
--          data pasien. Cek tanggal aktifnya modul/layanan.
--   * Semua hari rata di angka rendah
--       -> baru ini masalah per-pasien; lanjut ke query 2 dan 3.
--   * Persen menurun ke tanggal-tanggal terakhir
--       -> layanan dijalankan manual dan pasien yang mendaftar belakangan
--          tidak ikut terproses. Jalankan ulang untuk tanggal itu, lalu
--          ulangi query ini; kalau angkanya naik, terbukti soal cakupan.
-- =====================================================================
SELECT
    b.tglsep,
    COUNT(*)                                                AS sep_hari_itu,
    SUM(CASE WHEN t.no_rawat IS NOT NULL THEN 1 ELSE 0 END) AS berhasil,
    CONCAT(ROUND(100 * SUM(CASE WHEN t.no_rawat IS NOT NULL THEN 1 ELSE 0 END)
                 / COUNT(*), 0), '%')                       AS persen
FROM bridging_sep b
LEFT JOIN referensi_mobilejkn_bpjs_taskid t
       ON t.no_rawat = b.no_rawat AND t.taskid = '1'
WHERE b.tglsep BETWEEN '2026-08-01' AND '2026-08-28'
  AND b.jnspelayanan = '2'
GROUP BY b.tglsep
ORDER BY b.tglsep;


-- =====================================================================
-- QUERY 2 - ONSITE vs MJKN.
--
-- Menjawab "apakah pasien onsite benar-benar terkirim, atau yang berhasil
-- itu semuanya MJKN". Ini pembeda yang tidak bisa dilihat dari tabel taskid
-- saja, karena keduanya disimpan dengan kunci no_rawat yang sama.
--
--   onsite_berhasil > 0  -> pasien onsite memang sampai ke BPJS
--   onsite_berhasil = 0  -> yang berhasil semuanya MJKN; onsite masih nol
-- =====================================================================
SELECT
    b.tglsep,
    SUM(CASE WHEN mj.no_rawat IS NULL THEN 1 ELSE 0 END)                                AS onsite_total,
    SUM(CASE WHEN mj.no_rawat IS NULL AND t.no_rawat IS NOT NULL THEN 1 ELSE 0 END)     AS onsite_berhasil,
    SUM(CASE WHEN mj.no_rawat IS NOT NULL THEN 1 ELSE 0 END)                            AS mjkn_total,
    SUM(CASE WHEN mj.no_rawat IS NOT NULL AND t.no_rawat IS NOT NULL THEN 1 ELSE 0 END) AS mjkn_berhasil
FROM bridging_sep b
LEFT JOIN referensi_mobilejkn_bpjs mj        ON mj.no_rawat = b.no_rawat
LEFT JOIN referensi_mobilejkn_bpjs_taskid t  ON t.no_rawat  = b.no_rawat AND t.taskid = '1'
WHERE b.tglsep BETWEEN '2026-08-26' AND '2026-08-28'
  AND b.jnspelayanan = '2'
GROUP BY b.tglsep
ORDER BY b.tglsep;


-- =====================================================================
-- QUERY 3 - SEBAB KEGAGALAN, PASIEN ONSITE SAJA.
--
-- Jalankan hanya kalau query 1 sudah menunjukkan layanannya memang jalan,
-- tapi masih ada yang gagal. Hasilnya satu baris per pasien yang ditolak.
--
-- Arti kolom `dugaan`:
--   A. Kunjungan pertama, rujukan segar
--        -> TIDAK ADA alasan rujukan untuk ditolak. Perlu PESAN BPJS ASLI
--           dari log layanan Mobile JKN (baris "JSON JKN Onsite" yang
--           LENGKAP beserta "respon WS BPJS"-nya). Jangan menebak dari
--           database - sudah dicoba dan buntu.
--   B. Kunjungan ulang, noskdp kosong
--        -> rujukan FKTP dipakai ulang. Penolakan WAJAR; perbaikannya di
--           pembuatan surat kontrol, bukan di kode.
--   C. Rujukan antar RS
--        -> perlu dipastikan apakah sebenarnya rujukan INTERNAL. Kode di
--           KhanzaHMSServiceMobileJKNERM TIDAK PERNAH mengirim
--           jeniskunjungan=2, jadi rujukan internal pasti gagal.
--   D. Kontrol (punya surat kontrol)
--        -> bandingkan tglsep dengan tgl_rencana di
--           bridging_surat_kontrol_bpjs; kalau beda, penolakan wajar.
--   E. TIDAK DIKIRIM
--        -> kelompok BERBEDA: dilewati sebelum sampai ke BPJS. Jangan
--           tertukar dengan yang ditolak.
-- =====================================================================
SELECT
    b.tglsep,
    b.no_rawat,
    b.no_kartu,
    b.no_rujukan,
    b.noskdp,
    b.asal_rujukan,
    DATEDIFF(b.tglsep, b.tglrujukan) AS umur_rujukan_hari,
    (SELECT COUNT(*) FROM bridging_sep b5
      WHERE b5.no_kartu = b.no_kartu AND b5.no_rujukan = b.no_rujukan
        AND b5.tglsep < b.tglsep)    AS dipakai_sebelumnya,
    CASE
        WHEN b.no_kartu IS NULL OR b.no_kartu = ''            THEN 'E1. TIDAK DIKIRIM - no_kartu kosong'
        WHEN (b.noskdp IS NULL OR b.noskdp='') AND (b.no_rujukan IS NULL OR b.no_rujukan='')
                                                             THEN 'E2. TIDAK DIKIRIM - rujukan kosong'
        WHEN b.noskdp IS NOT NULL AND b.noskdp <> ''          THEN 'D. Kontrol (punya surat kontrol)'
        WHEN b.asal_rujukan LIKE '2%'                         THEN 'C. Rujukan antar RS'
        WHEN (SELECT COUNT(*) FROM bridging_sep b6
               WHERE b6.no_kartu = b.no_kartu AND b6.no_rujukan = b.no_rujukan
                 AND b6.tglsep < b.tglsep) > 0                THEN 'B. Kunjungan ulang, noskdp kosong'
        ELSE 'A. Kunjungan pertama, rujukan segar'
    END AS dugaan
FROM bridging_sep b
LEFT JOIN referensi_mobilejkn_bpjs mj       ON mj.no_rawat = b.no_rawat
LEFT JOIN referensi_mobilejkn_bpjs_taskid t ON t.no_rawat  = b.no_rawat AND t.taskid = '1'
WHERE b.tglsep BETWEEN '2026-08-26' AND '2026-08-28'
  AND b.jnspelayanan = '2'
  AND mj.no_rawat IS NULL      -- onsite saja
  AND t.no_rawat  IS NULL      -- yang gagal saja
ORDER BY dugaan, b.tglsep;
