---
version: anydesign-1
name: "RM 04 Transfer Pasien Antar Ruangan"
source: "Argon/rmtransferpasienantarruangrm04.html & Argon/rmtransferpasienantarruangrm04.java"
captured_at: "2026-10-26"
description: |
  Standar Desain Modern UI Form Rekam Medis RM 04-1 & RM 04-2 Transfer Pasien Antar Ruangan
  RSUD Demang Sepulau Raya berbasis arsitektur Argon (Tailwind Slate & Medical Teal).
  Menerapkan tata letak satu halaman vertikal scroll penuh, side rail kiri dengan kartu identitas pasien di atas,
  indikator kelengkapan live, 7 kartu seksi modular berpenanda nomor, verifikasi ganda serah terima klinis,
  dan otorisasi 4 barcode TTE terintegrasi.
colors:
  primary: "#0D9488"
  primary-dark: "#0A2E2C"
  surface: "#FFFFFF"
  surface-card: "#F8FAFC"
  surface-bg: "#F1F5F9"
  text-primary: "#0F172A"
  text-muted: "#64748B"
  border: "#CBD5E1"
  border-soft: "#E2E8F0"
  teal-soft: "#F0FDFA"
  teal-border: "#99F6E4"
  rose: "#E11D48"
  amber: "#FEFCE8"
typography:
  display: { fontFamily: "Segoe UI, sans-serif", fontSize: 15px, fontWeight: 700 }
  section-header: { fontFamily: "Segoe UI, sans-serif", fontSize: 13px, fontWeight: 700 }
  row-label: { fontFamily: "Segoe UI, sans-serif", fontSize: 12px, fontWeight: 700 }
  sub-label: { fontFamily: "Segoe UI, sans-serif", fontSize: 11px, fontWeight: 400 }
  micro-header: { fontFamily: "Segoe UI, sans-serif", fontSize: 10px, fontWeight: 700 }
  body: { fontFamily: "Segoe UI, sans-serif", fontSize: 12px, fontWeight: 400 }
  mono: { fontFamily: "Monospaced", fontSize: 11px, fontWeight: 700 }
spacing:
  base: 4px
  scale: [2, 4, 6, 8, 12, 16, 20]
rounded:
  card: 12px
  input: 8px
  button: 8px
  pill: 6px
components:
  top-header: { backgroundColor: "{colors.primary-dark}", textColor: "{colors.surface}", height: 48px }
  patient-card: { backgroundColor: "{colors.surface}", borderColor: "{colors.border-soft}", rounded: "{rounded.card}", padding: 14px }
  progress-card: { backgroundColor: "{colors.surface}", borderColor: "{colors.border-soft}", rounded: "{rounded.card}", padding: 12px }
  nav-section-card: { backgroundColor: "{colors.surface}", borderColor: "{colors.border-soft}", rounded: "{rounded.card}", padding: 12px }
  section-card: { backgroundColor: "{colors.surface}", borderColor: "{colors.border}", rounded: "{rounded.card}", padding: 16px }
  segmented-button-group: { backgroundColor: "{colors.surface-bg}", borderColor: "{colors.border-soft}", rounded: "{rounded.pill}", height: 32px }
  barcode-tte-panel: { backgroundColor: "{colors.surface}", borderColor: "{colors.border-soft}", rounded: "{rounded.input}", padding: 10px }
---

# Design Analysis: RM 04 Transfer Pasien Antar Ruangan

> Analisis desain sistem diekstraksi dengan skill `anydesign` dari file referensi Argon: `Argon/rmtransferpasienantarruangrm04.html` dan `Argon/rmtransferpasienantarruangrm04.java`. Tanggal: 26 Oktober 2026. Penekanan: Rekonstruksi & Integrasi SIMRS Khanza.

## Source
- Berkas Sumber: `Argon/rmtransferpasienantarruangrm04.html` (prototipe Tailwind modern) dan `Argon/rmtransferpasienantarruangrm04.java` (implementasi Swing Java2D).
- Target Modul: `src/rekammedis/RMTransferPasienAntarRuang.java` (SIMRS Khanza RSUD Demang Sepulau Raya).
- Skema Database: Tabel `transfer_pasien_antar_ruang` dan `transfer_pasien_antar_ruang_konfirmasi`.

## TL;DR
Desain Argon RM 04 mentransformasi formulir transfer pasien antar ruangan menjadi tampilan klinis modern berkepadatan tinggi yang intuitif. Memadukan sticky sidebar navigasi di sisi kiri (kartu pasien, indikator kelengkapan live, tombol lompat seksi, dan tombol contekan SIMRS) dengan area formulir vertikal scroll di sisi kanan yang terdiri dari 7 kartu modul terstandarisasi, verifikasi serah terima ganda, serta panel otorisasi 4 barcode TTE.

## 1. Visual identity

### 1.1 Identitas Visual & Tema
- Nuansa Warna: Medical Teal (`{colors.primary}`, `{colors.primary-dark}`) dikombinasikan dengan Slate netral (`{colors.surface-bg}`, `{colors.text-primary}`, `{colors.text-muted}`) dan aksen fungsional (Rose `{colors.rose}` untuk status bahaya/hapus, Amber `{colors.amber}` untuk mode edit).
- Tipografi: Segoe UI / SansSerif untuk keterbacaan optimal di Java Swing Windows, Monospaced untuk No. RM, No. Rawat, dan kode barcode.
- Hierarki Radius:
  - Card kontainer utama: membulat `{rounded.card}`.
  - Input box, combobox, tanggal, dan tombol aksi: membulat `{rounded.input}`.
  - Pill segmented radio toggle: membulat `{rounded.pill}` di dalam wadah track `{rounded.input}`.

## 2. Layout Structure & Side Rail

### 2.1 Side Rail Kiri (Sticky Sidebar ~300px)
1. **Kartu Identitas Pasien (Paling Atas)**:
   - Avatar inisial lingkaran dengan background soft teal (`{colors.teal-soft}`) dan border `{colors.teal-border}`.
   - Nama lengkap pasien, jenis kelamin, usia, No. RM, No. Rawat, dan rute transfer aktif (Ruang Asal -> Ruang Tujuan).
2. **Indikator Kelengkapan & Ceklis Serah Terima**:
   - Persentase kelengkapan live dan progress bar visual.
   - Ringkasan parameter terisi dan tombol cepat "Verifikasi Semua Menyerahkan & Menerima".
3. **Navigasi Cepat 7 Seksi ("Lompat ke Seksi")**:
   - 01. Informasi Transfer & Ruangan
   - 02. Kondisi Pasien & TTV
   - 03. Data Serah Terima Klinis
   - 04. Aktifitas, Drainase & Luka
   - 05. Investigasi & Penunjang
   - 06. Terapi, Rencana & Dokumen
   - 07. Otorisasi & TTE
4. **Tombol Bantuan SIMRS**:
   - Tombol "Tarik TTV & Terapi dari IGD / RM 20".
   - Tombol "Isi Standar Dokumen Serah Terima".

### 2.2 Formulir Utama Kanan (Satu Halaman Penuh Vertikal Scroll)
- Tidak ada tab di dalam formulir. Seluruh 7 bagian tersusun dari atas ke bawah dalam satu scroll viewport yang mulus.
- Setiap seksi dibungkus Card putih `{colors.surface}` ber-arc `{rounded.card}` dengan header ber-badge angka lingkaran, judul seksi tebal, sub-deskripsi, dan status serah terima ganda.

## 3. Komponen Spesifik (Design Tokens)

#### top-header
Bar navigasi gelap atas (`{colors.primary-dark}`) memuat logo/badge "RM", judul "Transfer Pasien Antar Ruangan", tag mono "RM 04-1 & RM 04-2", serta identitas instansi RSUD Demang Sepulau Raya. Di bawahnya terdapat baris sub-toolbar yang memuat tab peralihan Formulir vs Riwayat.

#### patient-card
Kotak identitas pasien di paling atas side rail navigasi kiri. Berlatar putih bersih `{colors.surface}` dengan header slate halus (`{colors.surface-card}`), border `{colors.border-soft}`, avatar inisial 42px, dan pasangan key-value data rekam medis.

#### progress-card
Widget pengukur kelengkapan pengisian form dengan JProgressBar custom, kalkulasi persentase reaktif, counter seksi serah terima, dan tombol aksi cepat verifikasi. Berlatar `{colors.surface}` berbingkai `{colors.border-soft}`.

#### nav-section-card
Daftar 7 tautan tombol navigasi seksi dengan indikator rollover, status aktif bersisi teal tebal 4px `{colors.primary}`, dan scroll instan ke koordinat target.

#### section-card
Kontainer utama per seksi formulir (01 sampai 07). Memiliki header abu-abu lembut (`{colors.surface-card}`), border pemisah seksi (`{colors.border}`), layout grid form responsif 2-3 kolom, dan pembungkus micro-header di atas setiap input box.

#### segmented-button-group
Tombol pill radio segmen interaktif yang menggantikan radio button standar. Memiliki track abu-abu muda (`{colors.surface-bg}`), sudut membulat `{rounded.pill}`, efek hover cerah, dan status terpilih berwarna solid teal (`{colors.primary}`) atau rose (`{colors.rose}`) dengan teks putih tebal.

#### barcode-tte-panel
Panel verifikasi tanda tangan elektronik (TTE) 4 pihak (Petugas Menyerahkan, Petugas Menerima, Dokter DPJP, Pasien/Keluarga) yang merender kode QR / Barcode grafis Java2D presisi, stempel waktu validasi, nama berpenutup kurung dinamis, dan tombol aksi "Sahkan TTE". Berlatar `{colors.surface}` dengan border `{colors.border-soft}`.

## 4. Tab Riwayat Data & Pratinjau Dokumen Dual-Pane
- Tab 2 (Data Riwayat Transfer) menerapkan arsitektur Dual-Pane split view (`JSplitPane` rasio ~60:40):
  - Kiri: Tabel riwayat data transfer + filter pencarian tanggal/keyword + hitungan record.
  - Kanan: Pratinjau dokumen cetak HTML presisi berkas fisik RM 04 dengan toolbar navigasi dan tombol modal "Pratinjau Layar Penuh" (`bukaDialogPreviewFullscreen`).
- Setiap kali baris tabel dipilih atau ditekan panah atas/bawah pada keyboard, pratinjau HTML di sebelah kanan langsung me-render ulang dokumen pasien secara instan.

## 5. Banner Peringatan Mode Edit
- Saat tombol "Ganti" dipilih dari tabel riwayat data, formulir otomatis beralih ke Tab 1 dengan memunculkan banner peringatan warna amber `{colors.amber}` mencolok di bagian paling atas:
  `MODE EDIT AKTIF: Anda sedang memperbarui data transfer tanggal [waktu].` disertai tombol cepat "Batal Edit / Input Baru".

## 6. Do's and Don'ts

### Do
- Tulis judul tab, header, dan bab hanya nama intinya saja, TANPA keterangan dalam tanda kurung.
- Pasang `setRolloverEnabled(true)` dan listener mouse hover pada setiap tombol Swing.
- Pastikan pemilihan dokter dan petugas selalu melewati dialog master (`DlgCariDokter` dan `DlgCariPetugas`).
- Amankan penyimpanan data ke database `transfer_pasien_antar_ruang` secara non-destruktif dan sinkron dengan kolom yang ada.
- Gunakan hierarki sudut membulat konsisten: card `{rounded.card}`, input `{rounded.input}`, pill `{rounded.pill}`.

### Don't
- Jangan pernah menaruh kartu identitas pasien di header horizontal formulir atas yang memakan ruang vertikal.
- Jangan memecah seksi 01-07 ke dalam sub-tab terpisah di dalam formulir pengkajian.
- Jangan gunakan tombol bersudut tajam tanpa styling hierarki border rounded.
- Jangan membiarkan input dokter dan perawat diketik manual tanpa validasi dialog master.

## 7. Open Questions
- Material sufficient: Seluruh kebutuhan desain, arsitektur tata letak, palet token, dan fungsionalitas CRUD SIMRS sudah lengkap dan diverifikasi dengan kode referensi `Argon/rmtransferpasienantarruangrm04.java` serta form preseden `RMAsesmenMedisRanap20_1.java`.
