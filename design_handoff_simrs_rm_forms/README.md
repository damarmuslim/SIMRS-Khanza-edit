# Handoff: Redesain Formulir RM SIMRS Khanza — RSUD Demang Sepulau Raya

## Overview
Paket ini berisi desain ulang (redesign) untuk ~20 formulir rekam medis (RM) dan dua halaman portal webapps pada SIMRS Khanza yang dimodifikasi untuk RSUD Demang Sepulau Raya. Tujuannya: menyeragamkan tampilan seluruh formulir digital rawat inap, IGD, dan rawat jalan ke satu bahasa desain, sambil tetap mengikuti struktur data (kolom tabel) yang sudah ada di basis data.

## Tentang Berkas Desain
Berkas dalam paket ini adalah **referensi desain berbentuk HTML** (prototipe hidup, bukan kode produksi). Setiap berkas adalah "Design Component" (`.dc.html`) yang bisa dibuka langsung di browser untuk melihat tampilan, interaksi (radio/checkbox, expand/collapse, tambah-baris, dsb.), dan nilai gaya (warna, ukuran, spasi) — tugas developer adalah **membangun ulang tampilan ini di dalam aplikasi Java Swing SIMRS Khanza yang sudah ada** (kelas `RM*.java` / `Frm*.java`), bukan menempelkan HTML ini ke aplikasi.

## Fidelity
**High-fidelity.** Semua nilai warna, tipografi, spasi, dan radius yang tercantum di bawah adalah nilai final yang harus direplikasi persis — bukan sketsa kasar.

## Desain Sistem (berlaku di SEMUA formulir RM)

**Kerangka layar**: 1600×900px (atau lebar mengikuti window Swing), struktur vertikal:
1. Bar teal `#123c3a` tinggi 34px: judul form (putih `#dfe9e6`, 12.5px, bold 600) + badge kode RM (mono, `10.5px`, border `#2c5a56`, warna teks `#8fb5ae`) + "RSUD Demang Sepulau Raya" rata kanan.
2. (Opsional) Bar tab putih 38px, border-bottom `#cfd3ce`; tab aktif warna `#0d7a70` + border-bottom 2px.
3. Baris header form: kartu identitas pasien (kiri, 262px, `#f7f8f5`, border `#e0e2dd`, badge "TERKUNCI") + field-field spesifik form (kanan, grid kolom).
4. Konten utama: grid `300px | 1fr`, gap 20px, padding 16px 20px.
   - Sidebar kiri: kartu progress ("X% · Y/Z isian wajib"), kartu navigasi antar-bagian (badge angka Romawi, judul, hitung X/Y), tombol "Tampilkan Semua Bagian".
   - Kanan: satu atau lebih kartu section — header `padding:16px 24px`, badge nomor 26×26px `background:#0d7a70` (section pertama kadang `#123c3a`), judul 14px/600 + subjudul 11.5px `#9aa19c`, tally kanan (badge status: hijau `#e8f3ed`/`#1a6b4c` = lengkap, oranye `#fdf3e7`/`#8a5a12` = kurang, abu `#f2f3f0`/`#9aa19c` = opsional).
5. Baris terakhir — action bar putih 56px: ikon+teks status kiri; kanan berurutan **Baru → Ganti → Hapus → Keluar** (outline) → **Cetak Formulir** (outline) → **Simpan** (solid `#0d7a70`). Untuk formulir data-satu-kali (bukan pengkajian klinis panjang) cukup **Batal → Cetak → Simpan**.

**Kontrol input**:
- Label field: `10.5px` bold 700 uppercase, `letter-spacing:.07em`, warna `#8b928d`; label wajib ditambah warna `#1c1f1d` bold + tanda `*`.
- Pilihan tunggal (radio) = pil solid: aktif `background:#f1f7f6` atau `#0d7a70` (pill kontras tinggi di form landing), border teal, ikon Material Symbols `radio_button_checked`/`_unchecked`.
- Pilihan jamak (checkbox) = chip dengan ikon `check_box`/`check_box_outline_blank`: aktif `background:#e9f5f2`, teks & border `#0d7a70`.
- Input teks: border `#cfd3ce`, radius `2px`, `padding:7-10px`, `13px`; angka pakai IBM Plex Mono.
- Field terkunci/read-only (data dari tabel lain): `background:#f4f6f2` atau `#fbfcfa`, teks `#5d645f`, ikon gembok (`lock`).
- Lookup master (dokter/petugas/ruangan): kotak dengan kode mono `#0d7a70` | nama | ikon `search`.

**Warna status**: normal `#1c1f1d`, sekunder `#6c736e`, redup `#9aa19c`, nonaktif `#c2c7c2`, bahaya `#a8442e`, peringatan `#8a6d3b`, aman `#0d7a70`.

**Tipografi**: IBM Plex Sans (teks umum), IBM Plex Mono (angka/kode). Ikon: Material Symbols Rounded.

**Tabel "Data Tersimpan"**: header row `background:#f6f8f5`, label `10.5px` uppercase `#8b928d`, body baris selang-seling putih/`#fcfdfb`, badge status Final (`#e9f5f2`/`#0d7a70`) vs Draft (`#f8f2e6`/`#8a6d3b`). **Tidak** memakai warna solid di header tabel.

## Daftar Formulir & Berkas

| Berkas | Kode RM | Modul | Isi Utama |
|---|---|---|---|
| `Triase IGD Redesign.dc.html` | — | IGD | Triase awal |
| `Triase PACS IGD Redesign.dc.html` | — | IGD | Triase PACS |
| `Pengkajian Awal Keperawatan IGD Redesign.dc.html` | — | IGD | Asesmen keperawatan IGD |
| `Pengkajian Awal Medis IGD Redesign.dc.html` | — | IGD | Asesmen medis IGD |
| `Transfer Pasien Antar Ruang RM04 Redesign.dc.html` | RM 04 | Ranap | Transfer antar ruang |
| `Pengkajian Awal Keperawatan Ranap RM19 Redesign.dc.html` | RM 19 | Ranap | Asesmen awal keperawatan (12 bagian: alergi, riwayat, pemeriksaan fisik, psikososial, nyeri, gizi, edukasi, dst.) |
| `Pengkajian Awal Medis Rawat Inap Redesign.dc.html` | — | Ranap | Anamnesa umum + pemeriksaan medis |
| `Asesmen Medis Rawat Inap RM20 Redesign.dc.html` | RM 20 | Ranap | Anamnesa, pemeriksaan medis (GCS, TTV) |
| `Asesmen Medis Rawat Inap RM20-1 Redesign.dc.html` | RM 20-1 | Ranap | Pemeriksaan fisik a–n + asesmen & program |
| `Asesmen Pendidikan Pasien RM21 Redesign.dc.html` | RM 21 | Ranap | Asesmen kebutuhan edukasi + perencanaan pemberian edukasi |
| `CPPT RM27 Redesign.dc.html` | RM 27 | Ranap | Catatan perkembangan pasien terintegrasi (log kronologis lintas profesi + verifikasi elektronik) |
| `Catatan Perawat Bidan RM32 Redesign.dc.html` | RM 32 | Ranap | Log catatan perawat/bidan per shift |
| `Resume Keperawatan Kebidanan RM33 Redesign.dc.html` | RM 33 | Ranap | Resume keperawatan (diagnosa timbul/teratasi, tindakan, rencana pulang) |
| `Ringkasan Pasien Keluar Masuk RM6 Redesign.dc.html` | RM 17-01 | Ranap | Ringkasan masuk-keluar, keluarga, diagnosa |
| `Asuhan Gizi Pasien RM Gizi Redesign.dc.html` | — | Ranap | Antropometri, riwayat gizi, alergi, monitoring & evaluasi |
| `EWS Dewasa RM30 Redesign.dc.html` | RM 30-1A | Ranap | Bagan skor peringatan dini (matriks parameter × waktu) |
| `Pelaksanaan Informasi Edukasi Redesign.dc.html` | — | Ranap/Ralan | Log pelaksanaan sesi edukasi + verifikasi |
| `Resume Rawat Jalan RM01 Redesign.dc.html` | RM 01 | Ralan | Kondisi pulang, diagnosis, prosedur, obat pulang |
| `Edukasi Pasien Keluarga RM02 Redesign.dc.html` | RM 02-1 | Ralan | Asesmen kebutuhan edukasi (bagian A) |
| `Asesmen Medis Awal Rawat Jalan RM02-1 Redesign.dc.html` | RM 02-1 | Ralan | Riwayat penyakit, risiko jatuh, anamnesis, diagnosis |
| `Asesmen Awal Keperawatan Rawat Jalan RM02-2 Redesign.dc.html` | RM 02-2 | Ralan | Pemeriksaan fisik 20 regio, pola kebiasaan, balita/reproduksi bersyarat |
| `Formulir Pendaftaran Pasien RM09 Redesign.dc.html` | RM 09 | Pendaftaran | Data diri, alamat, identitas & pembiayaan |
| `Webapps Login Redesign.dc.html` | — | Portal | Halaman login webapps |
| `Webapps Launcher Redesign.dc.html` | — | Portal | Grid peluncur formulir, dikelompokkan per kategori |

## Struktur Data
Setiap formulir dipetakan ke tabel MySQL yang SUDAH ADA atau baru dibuat via migrasi additive (`sik_*.sql` di root proyek, mis. `sik_asesmen_medis_awal_ralan_rm02_1.sql`). Nama field pada tiap Design Component (state key, mis. `keluhan_utama`, `diagnosa_utama`) **sengaja disamakan dengan nama kolom tabel** supaya pemetaan form→database langsung terlihat. Developer harus membaca migrasi SQL terkait sebelum mengimplementasikan form Java-nya.

## Interaksi & Perilaku
- Navigasi antar-bagian: klik item sidebar → pindah section aktif (state `active`), atau tombol "Tampilkan Semua Bagian" untuk lihat semua section sekaligus (state `showAll`).
- Progress dihitung dari field bertanda wajib (`*`) yang terisi, ditampilkan sebagai persentase + hitungan.
- Field bersyarat (radio dengan `extraTrigger`) memunculkan kotak teks tambahan saat opsi tertentu dipilih (mis. "Ya, sebutkan…").
- Verifikasi elektronik (CPPT, Pelaksanaan Edukasi): tombol "Verifikasi" sekali klik merekam nama + waktu dari akun login sebagai pengganti stempel/paraf fisik — tidak ada tanda tangan gambar.
- Semua tombol Baru/Ganti/Hapus/Keluar, Cetak, dan Simpan pada mock ini adalah **placeholder tampilan** — logic penyimpanan/cetak sesungguhnya ada di kelas Java masing-masing form.

## Screenshot
Folder `screenshots/<nama form>/` berisi cuplikan layar tiap bagian/section dari setiap formulir (bernomor sesuai urutan section, mis. `4.a.png`, `4.b.png` untuk sub-bagian ke-4a, 4b). Gunakan ini sebagai referensi visual cepat tanpa perlu membuka `.dc.html` — cocokkan nomor section pada gambar dengan urutan section di berkas HTML terkait.

## Assets
Tidak ada aset gambar eksternal lain — seluruh ikon memakai font ikon Google **Material Symbols Rounded** (dimuat via Google Fonts CDN), font teks **IBM Plex Sans** & **IBM Plex Mono**.

## Catatan Penting untuk Developer
- Field checklist per-regio (Pemeriksaan Fisik RM 02-2) disimpan sebagai SATU teks per regio, temuan digabung koma — lihat komentar di `sik_asesmen_awal_keperawatan_ralan_rm02_2.sql`.
- Semua form pakai `ROW_FORMAT=DYNAMIC` bila kolom TEXT banyak (lihat catatan ukuran baris InnoDB di file SQL terkait) — jangan ubah jadi VARCHAR panjang tanpa memeriksa batas 8126 byte per baris.
- Ruangan/nomor bed pada RM 32 & RM 33 **tidak disimpan di tabel form** — selalu diambil live dari `kamar_inap` (baris terakhir) saat form dibuka & saat cetak.
