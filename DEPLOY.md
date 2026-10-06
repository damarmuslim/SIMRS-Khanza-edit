# DEPLOY.md — status migrasi database

Daftar tiap berkas migrasi `sik_*.sql` dan di mana ia sudah dijalankan.

## Kenapa berkas ini ada

Aturan proyek: **migrasi dan perubahan Java WAJIB naik ke produksi
BERSAMAAN.** Penyimpanan lewat `Sequel.menyimpantf` merakit
`insert into tabel values(?,?,?)` **tanpa nama kolom**, jadi kalau salah satu
sampai duluan, seluruh penyimpanan modul itu gagal. Sudah terjadi tiga kali:
kolom `jam` di Asuhan Gizi, dua kolom di RM 32, dan `resume_pasien`.

Selama ini status itu hanya tersebar sebagai prosa di `PROGRESS.md`. Ada
36 berkas migrasi di repo — terlalu banyak untuk diingat.

## Cara membaca

- **aik** — diperiksa langsung ke database, bukan dari ingatan. Skripnya
  membaca tiap `CREATE TABLE` dan `ADD/MODIFY COLUMN` lalu mencocokkannya ke
  `information_schema`. `sebagian` berarti ada tabel/kolom yang sudah ada dan
  ada yang belum.
- **Produksi** — **hanya Anda yang tahu**, sengaja dikosongkan. Jangan diisi
  dengan tebakan; kolom yang salah di sini lebih berbahaya daripada kolom yang
  kosong.

Isi kolom Produksi dengan tanggalnya (mis. `2026-09-01`) begitu dijalankan.

## Sebelum menaikkan jar baru ke produksi

1. Lihat baris mana yang kolom Produksinya masih kosong.
2. Jalankan migrasinya lebih dulu, atau bersamaan dengan jar-nya.
3. Isi tanggalnya di tabel ini, lalu commit.
4. Beri tag pada commit itu, mis. `git tag prod-2026-09-01`, supaya jelas jar
   mana yang berpasangan dengan migrasi mana.

| Migrasi | aik | Produksi | Lahir di commit | Catatan |
|---|---|---|---|---|
| `sik_asesmen_awal_keperawatan_ralan_rm02_2.sql` | ya |  | b3cf53a 2026-08-29 |  |
| `sik_asesmen_medis_awal_ralan_rm02_1.sql` | ya |  | 46c0b74 2026-08-29 |  |
| `sik_asesmen_medis_ranap_rm20.sql` | ya |  | 78b6c25 2026-08-26 |  |
| `sik_asesmen_pendidikan_pasien_ranap_rm21.sql` | ya |  | ab60ff7 2026-08-27 |  |
| `sik_asuhan_gizi_jam.sql` | ya |  | 1aaf53c 2026-08-27 |  |
| `sik_bridging_lab.sql` | **sebagian** |  | 89ddef4 2026-08-17 | kurang: detail_hasil_lab (tabel), detail_permintaan_lab (tabel), lis_order (tabel) |
| `sik_bridging_radiologi.sql` | **belum** |  | 89ddef4 2026-08-17 | order_in (tabel) dst |
| `sik_bukti_pelaksanaan_edukasi_pemberi.sql` | ya |  | 6640316 2026-08-22 |  |
| `sik_catatan_perawat_bidan_rm32.sql` | ya |  | 1cf61b3 2026-08-28 |  |
| `sik_edukasi_pasien_keluarga_rm02_1.sql` | ya |  | 6640316 2026-08-22 |  |
| `sik_ews_dewasa_rm30_1a.sql` | ya |  | a424acd 2026-08-29 |  |
| `sik_hak_akses_asesmen_medis_ranap.sql` | ya |  | 78b6c25 2026-08-26 |  |
| `sik_hak_akses_ringkasan_masuk_keluar.sql` | ya |  | f7eaf3b 2026-08-27 |  |
| `sik_pengkajian_ranap_alasan_masuk_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_discharge_manual_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_edukasi_discharge_penunjang_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_fix_rowsize_innodb.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_gcs_pupil_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_keluhan_utama_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_kesadaran_lla_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_neurosensori_ekstremitas_kulit_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_nyeri_fungsional_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_pemeriksaan_fisik_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_pernafasan_kemih_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_psikososial_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_reproduksi_branden_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_riwayat_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_pengkajian_ranap_sosial_ekonomi_rm19.sql` | ya |  | 200b1be 2026-08-26 |  |
| `sik_penilaian_awal_keperawatan_igd_rm0345.sql` | ya |  | 89ddef4 2026-08-17 |  |
| `sik_penilaian_medis_igd_rm032.sql` | ya |  | 89ddef4 2026-08-17 |  |
| `sik_rekonsiliasi_obat_rm0306a.sql` | ya |  | 6640316 2026-08-22 |  |
| `sik_resume_keperawatan_ranap_rm33.sql` | ya |  | 398f47e 2026-08-28 |  |
| `sik_resume_rawat_jalan_rm01.sql` | ya |  | 39d8d91 2026-08-28 |  |
| `sik_resume_rawat_jalan_rm01_eparaf.sql` | ya (2026-10-06) |  | perubahan lokal | Snapshot kunjungan dan e-paraf internal DPJP; Java dan migrasi wajib naik bersama |
| `sik_ringkasan_pasien_masuk_keluar_rm17.sql` | ya |  | f7eaf3b 2026-08-27 |  |
| `sik_transfer_pasien_antar_ruang_rm04.sql` | ya |  | 89ddef4 2026-08-17 |  |
| `sik_triase_pacs.sql` | ya |  | 89ddef4 2026-08-17 |  |

## Yang belum masuk aik pun

Dua berkas bridging di bawah belum pernah dijalankan di `aik`. Keduanya untuk
integrasi LIS/PACS, bukan modul rekam medis, jadi kemungkinan memang belum
dipakai — bukan tertinggal. Perlu dipastikan sebelum dianggap selesai.

