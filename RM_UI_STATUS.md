# Status Evaluasi UI Form Rekam Medis

Tracker resmi ini mencakup formulir prioritas yang memiliki acuan visual di `design_handoff_simrs_rm_forms` atau formulir fisik yang telah disetujui pengguna, dibagi menurut alur layanan **IGD**, **Rawat Inap (Ranap)**, dan **Rawat Jalan (Ralan)**. Tracker tidak lagi memakai seluruh file `RM*.java` sebagai denominator.

## Sumber Keputusan

1. Keputusan eksplisit pengguna terbaru.
2. Berkas desain handoff atau formulir fisik yang cocok dengan formulir.
3. Form selesai pada layanan yang sama sebagai referensi implementasi Swing.
4. `ThemeConfig` dan pola legacy bila belum diatur oleh sumber sebelumnya.

Satu item mewakili satu formulir/desain kerja, bukan selalu satu file Java. Triase klasik dan PACS dihitung satu item karena memakai `RMTriaseIGD.java`. Form lintas layanan mempunyai satu pemilik tracker agar tidak dihitung ganda, tetapi seluruh jalur pemakaiannya tetap harus diuji.

## Arti Status

- `[ ]` **Belum**: belum dievaluasi terhadap desain handoff dan standar layanan.
- `SEDANG`: sedang ditata atau masih memiliki keputusan/verifikasi terbuka.
- `VERIFIKASI`: implementasi selesai dan menunggu pemeriksaan visual atau persetujuan pengguna.
- `[x]` **Selesai**: visual disetujui, render dan alur utama diperiksa, build lulus, dokumentasi serta commit tersedia, dan tag akhir dibuat.

## Ringkasan Resmi

- Total item implementasi: **21**
- IGD: **3 dari 3 selesai**
- Ranap: **2 dari 14 selesai**
- Ralan: **0 dari 4 selesai**
- Keseluruhan: **5 dari 21 selesai**
- Sedang: **0**
- Menunggu verifikasi: **0**

## IGD — 3 Item

- [x] [`RMTriaseIGD.java`](src/rekammedis/RMTriaseIGD.java) — mencakup `Triase IGD` dan `Triase PACS IGD`; disetujui 2026-09-26 dan ditutup dengan tag `design-baru-triase-igd`.
- [x] [`RMPenilaianAwalKeperawatanIGD.java`](src/rekammedis/RMPenilaianAwalKeperawatanIGD.java) — disetujui 2026-09-27 dan ditutup dengan tag `design-baru-penilaian-awal-keperawatan-igd`.
- [x] [`RMPenilaianAwalMedisIGD.java`](src/rekammedis/RMPenilaianAwalMedisIGD.java) — disetujui 2026-09-28 dan ditutup dengan tag `design-baru-penilaian-awal-medis-igd`.

## Rawat Inap (Ranap) — 14 Item

- [x] [`RMTransferPasienAntarRuang.java`](src/rekammedis/RMTransferPasienAntarRuang.java) — Transfer Pasien Antar Ruang RM 04; layout input, navigasi, dan cetakan standar IGD (grid bergaris, kop instansi, stiker identitas RM 04-1/04-2, serah terima 4 kolom e-paraf, rekap HTML) disetujui pengguna pada 2026-10-14 dan ditutup dengan tag `design-baru-transfer-pasien-antar-ruang`.
- [ ] [`RMPenilaianAwalKeperawatanRanap.java`](src/rekammedis/RMPenilaianAwalKeperawatanRanap.java) — Pengkajian Awal Keperawatan Ranap RM 19.
- [ ] [`RMPenilaianAwalMedisRanapDewasa.java`](src/rekammedis/RMPenilaianAwalMedisRanapDewasa.java) — Pengkajian Awal Medis Rawat Inap.
- [ ] [`RMAsesmenMedisRanap.java`](src/rekammedis/RMAsesmenMedisRanap.java) — Asesmen Medis Rawat Inap RM 20.
- [ ] [`RMAsesmenMedisRanap20_1.java`](src/rekammedis/RMAsesmenMedisRanap20_1.java) — Asesmen Medis Rawat Inap RM 20-1.
- [ ] [`RMAsesmenPendidikanPasien.java`](src/rekammedis/RMAsesmenPendidikanPasien.java) — Asesmen Pendidikan Pasien RM 21.
- [ ] [`DlgRawatInap.java`](src/simrskhanza/DlgRawatInap.java) dan [`DlgRawatJalan.java`](src/simrskhanza/DlgRawatJalan.java) — CPPT RM 27; satu item lintas layanan, pemilik tracker Ranap, kedua jalur wajib diuji.
- [ ] [`RMDataCatatanKeperawatanRanap.java`](src/rekammedis/RMDataCatatanKeperawatanRanap.java) — Catatan Perawat/Bidan RM 32.
- [ ] [`RMResumeKeperawatanKebidanan.java`](src/rekammedis/RMResumeKeperawatanKebidanan.java) — Resume Keperawatan Kebidanan RM 33.
- [ ] [`RMRingkasanPasienMasukKeluar.java`](src/rekammedis/RMRingkasanPasienMasukKeluar.java) — Ringkasan Pasien Masuk/Keluar RM 17-01.
- [ ] [`RMDataAsuhanGizi.java`](src/rekammedis/RMDataAsuhanGizi.java) — Asuhan Gizi Pasien.
- [ ] [`RMPemantauanEWSD.java`](src/rekammedis/RMPemantauanEWSD.java) — EWS Dewasa RM 30-1A.
- [ ] [`RMPelaksanaanInformasiEdukasi.java`](src/rekammedis/RMPelaksanaanInformasiEdukasi.java) — Pelaksanaan Informasi Edukasi; item lintas Ranap/Ralan, kedua jalur wajib diuji.
- [x] [`RMRekonsiliasiObat.java`](src/rekammedis/RMRekonsiliasiObat.java) — diklasifikasikan khusus sebagai form Ranap; mencakup Admisi, Transfer Antar Ruang, dan Pulang. Verifikasi visual aplikasi serta cetak nyata Ranap disetujui pengguna pada 2026-10-03. Implementasi ditutup melalui commit `1fa0584` dan tag penutup `design-baru-rekonsiliasi-obat-verifikasi-2026-10-03`; tag `design-baru-rekonsiliasi-obat` tetap dipertahankan sebagai penanda historis.

## Rawat Jalan (Ralan) — 4 Item

- [ ] [`RMResumeRawatJalan.java`](src/rekammedis/RMResumeRawatJalan.java) — Resume Rawat Jalan RM 01.
- [ ] [`RMEdukasiPasienKeluargaRawatJalan.java`](src/rekammedis/RMEdukasiPasienKeluargaRawatJalan.java) — Edukasi Pasien dan Keluarga RM 02-1.
- [ ] [`RMAsesmenMedisAwalRalan.java`](src/rekammedis/RMAsesmenMedisAwalRalan.java) — Asesmen Medis Awal Rawat Jalan RM 02-1.
- [ ] [`RMAsesmenAwalKeperawatanRalan.java`](src/rekammedis/RMAsesmenAwalKeperawatanRalan.java) — Asesmen Awal Keperawatan Rawat Jalan RM 02-2.

## Di Luar Cakupan Tracker Ini

- Form `RM*.java` lain yang tidak mempunyai desain handoff tidak dihitung otomatis.
- `Formulir Pendaftaran Pasien RM09` berada pada layanan Pendaftaran, bukan IGD/Ranap/Ralan.
- `Webapps Launcher` dan `Webapps Login` merupakan halaman portal, bukan form RM desktop.
- Form di luar daftar hanya ditambahkan setelah pengguna menetapkan kategori dan acuan visual berupa desain handoff atau formulir fisik.

## Gerbang Penyelesaian per Item

1. Cocokkan implementasi dengan desain handoff dan alur layanan pemiliknya.
2. Amankan perubahan lokal serta buat tag `pre-redesign-<form>` sebelum commit redesign pertama.
3. Implementasikan bertahap tanpa merusak logika bisnis, database, permission, report, dan kontrak data.
4. Render dan lihat seluruh keadaan penting; jalankan `ant jar`; uji alur utama serta semua jalur layanan untuk item lintas layanan.
5. Setelah pengguna menyetujui visual, perbarui tracker/`PROGRESS.md`/`DEPLOY.md` bila relevan, commit, lalu buat tag `design-baru-<form>`.
