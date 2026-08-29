## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).

---

# Aturan Proyek SIMRS Khanza

Ditulis dari pelajaran nyata modul RM 03/04/17-01/20/21/32/33/01/27-1, Asuhan Gizi, dan penelusuran bridging BPJS. Setiap aturan di bawah pernah menimbulkan bug sungguhan — bukan gaya penulisan.

## Bahasa

Balas dalam **Bahasa Indonesia**. Komentar kode dan pesan commit juga Bahasa Indonesia.

## Build

```
& "C:\Program Files\NetBeans-14\netbeans\extide\ant\bin\ant.bat" jar
```

- **Hanya lewat jalur itu.** `ant` biasa tidak ada di PATH.
- **Jangan `ant clean`** — build ulang penuh sangat lama dan tidak pernah diperlukan.
- `DlgUser.java` butuh `javac.fork=true` + `-J-Xss64m` di `nbproject/project.properties` (sudah ada, jangan dihapus).
- `frmUtama.initKhanza()` sudah di batas 64 KB bytecode JVM. Tombol menu utama yang baru masuk ke `initKhanzaLanjutan()` — nama `initKhanza2()` sudah terpakai.

## Form warisan yang punya berkas `.form`

Kalau sebuah `.java` punya `.form` bersebelahan, **seluruh tambahan ditulis DI LUAR blok GEN** (`//GEN-BEGIN:initComponents` … `//GEN-END:initComponents`). Apa pun yang ditulis di dalamnya akan **hilang** begitu form dibuka lagi di Form Editor NetBeans.

- Cari batas blok GEN dulu sebelum mengedit.
- Kalau tidak ada kode lain yang merujuk widget baru, **buat variabel lokal, bukan field** — area deklarasi GEN jadi tidak perlu disentuh sama sekali.
- Body method `//GEN-FIRST:event_… … //GEN-LAST:event_…` aman diedit; NetBeans mempertahankannya.

Form BARU ditulis tangan dengan `GridBagLayout`, **tanpa `.form`, tanpa blok GEN** (preseden RM 20/17-01/21/33/01).

## Gotcha widget

- `widget.TextBox` / `ComboBox` / `Tanggal` constructor-nya diam-diam memanggil `setSize(WIDTH, 23)` dengan `WIDTH = 1` (`ImageObserver.WIDTH`). **Wajib `setPreferredSize()` eksplisit**, kalau tidak field-nya menciut ~1px.
- `widget.Label` defaultnya rata **KANAN**. Untuk rata kiri harus `setHorizontalAlignment(SwingConstants.LEFT)`.
- `widget.Tanggal` buatan tangan **tidak** otomatis dapat `displayFormat`. Tanpa `setDisplayFormat(...)`, `substring(11,19)` untuk mengambil jam akan meledak.
- `GridBagLayout` men-TENGAH-kan isinya pada **kedua sumbu** kalau container lebih besar. Butuh **dua** pengisi: glue horizontal (`weightx=1.0`) di kolom terakhir DAN glue vertikal (`weighty=1.0`) di baris terakhir. Tanpa yang vertikal, isi form melayang di tengah.

## Petugas dan dokter DIPILIH dari master, tidak pernah diketik

**Aturan baku, berlaku untuk seluruh modul.** Setiap isian petugas, perawat,
bidan, atau dokter diambil dari tabel masternya lewat dialog pencari — tidak
pernah lewat kotak teks yang bisa diketik, dan tidak pernah lewat combo yang
diisi tangan.

- Petugas/perawat/bidan → `kepegawaian.DlgCariPetugas` (tabel `petugas`)
- Dokter → `kepegawaian.DlgCariDokter` (tabel `dokter`)

Polanya: dialog dibuka **tidak modal**, nilainya dipungut di `windowClosed`
dari `getTable()` — **kolom 0 = kode/NIP, kolom 1 = nama**. Kalau ditutup tanpa
memilih, isian lama **dibiarkan**, bukan dikosongkan. Kotak NIP dan kotak nama
keduanya `setEditable(false)`.

**Kenapa keras:** NIP yang diketik tangan gampang salah satu digit, dan
akibatnya **tidak kelihatan**. JOIN ke `pegawai`/`dokter` gagal diam-diam,
sehingga nama dan QR tanda tangan tercetak **kosong** pada lembar yang sudah
ditandatangani — baru ketahuan setelah kertasnya di tangan pasien.

`petugas.nip` dan `pegawai.nik` memakai nilai yang sama, jadi pencarian nama
dan sidik jari lewat `pegawai` tetap ketemu setelah dipilih dari `petugas`.

Prefill dari petugas yang sedang login (`akses.getkode()`) boleh, itu bukan
ketikan. `"Admin Utama"` bukan NIP — jangan dipakai sebagai nilai.

## ⚠️ Penyimpanan POSISIONAL — penyebab bug paling mahal di proyek ini

`Sequel.menyimpantf("tabel", "?,?,?", "pesan", 3, nilai)` merakit
`insert into tabel values(?,?,?)` **TANPA NAMA KOLOM**. Urutan nilai harus sama persis dengan urutan kolom tabel.

`ALTER TABLE ... ADD COLUMN` tanpa `AFTER` **selalu menempel di ujung**.

Akibatnya: **migrasi dan perubahan Java WAJIB naik ke produksi BERSAMAAN.** Kalau salah satu duluan sampai, seluruh penyimpanan gagal. Sudah terjadi tiga kali: kolom `jam` di Asuhan Gizi, dua kolom di RM 32, dan `resume_pasien` (26 tanda tanya).

`Sequel.mengedittf` sebaliknya memakai nama kolom — kolom yang **tidak disebut** di klausa SET dibiarkan apa adanya. Manfaatkan itu untuk melindungi kolom yang diisi pihak lain (mis. tanda tangan pasien dari webapp) agar tidak terhapus saat teks diedit.

## Migrasi database

- **Aditif saja**: `CREATE TABLE` / `ADD COLUMN` nullable. Jangan mengubah tipe, jangan menghapus.
- **Tunjukkan dulu isinya, jalankan HANYA atas instruksi eksplisit user.**
- Sesudah import, buktikan tabel lain tidak tersentuh (mis. `user` tetap 1215 kolom).
- Verifikasi kesejajaran nama kolom ↔ nilai Java dengan membaca **kelas terkompilasi lewat refleksi**, bukan teks sumber — dan pastikan pemeriksanya memang bisa GAGAL (adu dengan migrasi modul lain).

## Jangan simpan ulang data yang sudah ada di tabel lain

Ruangan, tanggal masuk, tanggal keluar, nomor bed **diambil dari `kamar_inap` lewat JOIN**, tidak disalin ke tabel form. Salinan akan basi diam-diam saat pasien pindah kamar atau tanggal dikoreksi.

`kamar_inap` bisa punya **beberapa baris** per `no_rawat` (pindah kamar). Aturannya:
- **tanggal masuk** → baris paling **AWAL** (`min(tgl_masuk)`)
- **ruangan & tanggal keluar** → baris **TERAKHIR** (`order by tgl_masuk desc, jam_masuk desc limit 1`)
- `tgl_keluar='0000-00-00'` berarti **belum pulang**, bukan tanggal

Aturan yang sama harus dipakai di form dan di query cetak, supaya layar dan kertas tidak berbeda.

## Laporan Jasper

Tiga gotcha **wajib** di setiap `.jrxml`:
1. semua `<textField>` memakai `isStretchWithOverflow="true"`
2. semua `<staticText>` tingginya **>= 11** (di bawah itu teksnya tidak tampil)
3. semua `<band>` memakai `splitType="Prevent"`

Selain itu:
- **`--` ILEGAL di dalam komentar XML.** Pemisah `-----` akan menolak berkasnya saat compile.
- Elemen `<rectangle>`/`<line>` pembentuk kotak di band **detail** butuh `stretchType="RelativeToBandHeight"`, kalau tidak teks panjang meluber keluar kotaknya sementara garisnya tetap.
- Kop diletakkan di **`pageHeader`**, bukan `title`, untuk laporan yang bisa berlembar-lembar — kalau di `title`, lembar kedua jadi kertas tanpa identitas pasien.
- Nilai yang sama untuk seluruh halaman dikirim sebagai **parameter**; yang berganti tiap baris sebagai **field**. Dengan `whenNoDataType="AllSectionsNoDetail"`, lembar tetap tercetak utuh walau query nol baris.
- `Valid.MyReport` mengoper Connection → `.jasper` menjalankan `<queryString>`-nya sendiri (`$P{...}`). `Valid.MyReportqry` menjalankan query dari Java lalu membungkusnya `JRResultSetDataSource`. Pilih yang **sama dengan tetangganya** di berkas itu.
- `.jasper` hasil compile disalin ke **tiga** tempat: `report/`, `dist/report/`, `build/report/`. `.jasper` yang hilang membuat tombol cetak gagal diam-diam.
- Ekspresi L/P harus menerima bentuk **mentah `'L'`/`'P'` maupun `'Laki-Laki'`** — pernah membuat SELURUH pasien tercetak "P".
- Kolom tanda tangan basah **dikosongkan**, jangan dikarang.

## Disiplin verifikasi

Urutan ini berkali-kali menemukan bug yang tidak terlihat dari pemeriksaan angka:

1. **Render lalu LIHAT hasilnya.** Bug centering GridBag, kotak meluber, label suhu "S" yang tertukar dengan SOAP, dan jenis kelamin selalu "P" — semuanya hanya ketahuan dari melihat, bukan dari cek numerik.
2. **Jalankan jalur SQL yang SEBENARNYA** ke DB dalam `START TRANSACTION` + `ROLLBACK`, dengan memanggil **method aplikasinya sendiri** (refleksi kalau private) — bukan menyalin query ke harness. Query yang disalin bisa desinkron dari kodenya.
3. **Pakai data uji yang SENGAJA setengah kosong**, supaya jalur "tidak diisi" ikut teruji.
4. Pastikan nol sisa data uji sesudahnya.

Catatan: `koneksiDB.condb()` mengembalikan `Connection` **singleton statis**, jadi `setAutoCommit(false)` membungkus form dan `Sequel` sekaligus.

Menguji query lewat klien `mysql` **tidak cukup**: koneksinya latin1 sedangkan JDBC utf8mb4. Bug "Illegal mix of collations" pada `ifnull(sha1(...), nip)` hanya muncul lewat aplikasi.

## webapps

Webapp yang **benar-benar dilayani** ada di `C:\xampp\htdocs\webapps\`; folder `webapps/` di proyek hanya salinan sumber. Setiap perubahan harus **disalin ke keduanya**, termasuk `webapps/index.php` kalau menambah entri baru — kalau tidak, halamannya ada tapi tidak ada tautan menuju ke sana.

Pola tanda tangan: satu tabel `antri<modul>` berisi SATU baris penanda record yang menunggu tanda tangan; tablet menekan Refresh untuk mengambilnya. Ada ~20 tabel `antri*` yang mengikuti pola ini.

## Git

- `main` adalah trunk. **Satu cabang per modul RM**, dicabangkan dari `main`.
- Alat/berkas yang tidak terkait modul mana pun boleh langsung di `main`.
- Pesan commit **menjelaskan ALASAN**, bukan daftar perubahan: keputusan yang diambil, alternatif yang ditolak beserta sebabnya, bug yang ditemukan dan bagaimana ketahuannya.
- Pesan panjang ditulis lewat berkas (`git commit -F`) — PowerShell here-string merusak tanda kutip, dan `Out-File -Encoding utf8` menyisipkan BOM.
- Menambah ke `PROGRESS.md` pakai `[System.IO.File]::AppendAllText` dengan `UTF8Encoding($false)`; `Get-Content` tanpa `-Encoding` membaca UTF-8 sebagai ANSI dan menghasilkan mojibake.

## Sebelum membuat form baru — PERIKSA DULU

Tiga dari sekian modul ternyata **sudah ada** dan hanya perlu penyesuaian (RM 32, Resume Ralan BPJS, CPPT). Sebelum menulis tabel atau form baru, cek:

1. Nama tabel yang mirip di `information_schema.tables`
2. Berkas `.java` dan `.jrxml` yang namanya mirip
3. Apakah laporan yang ada ternyata **lembar kosong** (query-nya hanya identitas pasien)

Membuat tabel baru saat sudah ada yang serupa akan **memecah data pasien ke dua tempat**.
