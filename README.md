# ****Taninote: Sistem Pencatatan Riwayat Lahan Berbasis Event****

---

### Permasalahan Utama

Petani sering lupa atau salah mencatat apa yang sudah terjadi di lahannya, seperti kapan terakhir dipupuk, berapa kali disiram, kapan panen, dan kenapa hasil panen turun. Catatan yang ditimpa-timpa (misalnya “stok pupuk: 50 → 30”) membuat riwayat hilang, sehingga kesalahan tidak bisa ditelusuri.

Solusi dari kami, jangan simpan kondisi lahan, simpan kejadiannya. Kondisi lahan hari ini didapat dengan menjumlahkan semua kejadian dari awal. Dari solusi ini pastinya menerapkan immutability.

### Cara Kerjanya

Ini simpelnya kayak buku tulis yang ga pernah dihapus atau coret2 isinya, hanya menambah baris baru di bawah.

### Fitur Menu Console

Taninote: Sistem Pencatatan Riwayat Lahan Berbasis Event

 1. Lihat seluruh riwayat kejadian
 2. Lihat kondisi lahan saat ini
 3. Lihat kondisi lahan pada hari tertentu
 4. Tambah kejadian baru
 5. Batalkan kejadian terakhir (undo)
 6. Cari kejadian berdasarkan jenis
 7. Laporan ringkasan musim tanam
 0. Keluar

Pilih menu:

>Kejadian yang didukung cukup 5 jenis, yaitu:
1. Tanam
2. Pupuk
3. Siram
4. Hama
5. Panen

---

### Struktur Data

Disini, bakal ada dua struktur data dan keduanya immutable.

1. `Kejadian`: apa yang terjadi di lahan (ini bakal disimpan di riwayat)
2. `KondisiLahan`: keadaan lahan hasil menghitung kejadian

(1) Kejadian

- Tanam: hari, nama tanaman, luas (hektar)
- Pupuk: hari, jenis pupuk, jumlah (kg)
- Siram: hari, jumlah air (liter)
- Hama: hari, nama hama, tingkat serangan (1 sampai 3)
- Panen: hari, hasil(ton)

>Kalau ada yang nanya kenapa semua kejadian punya hari, jawabannya supaya nanti mudah mengurutkan dan memfilter berdasarkan hari, sehingga dibuat hari sebagai yang wajib ada di semua jenis kejadian.

(2) KondisiLahan

- tanaman -> Option[String]: tanaman yang sedang ditanam (None kalau lahan kosong)
- luasHa -> Double: luas lahan yang ditanami
- totalPupukKg -> Double: akumulasi pupuk
- totalAirLiter -> Double: akumulasi air
- tingkatHama -> Int: akumulasi serangan hama
- totalPanenTon -> Double: akumulasi hasil panen

>Lahan juga butuh keadaan awal, yaitu lahan kosong dengan semua angka 0 dan tanaman = None. Keadaan ini jadi titik mulai saat kejadian dihitung satu per satu. Nanti diletakkan di companion object dengan nama kosong.

Sebagai pengujian, kami menggunakan pembuktian dengan code di bawah untuk melihat struktur data bekerja dengan baik:

```scala
// Pengujian
@main def Taninote(): Unit =
  val riwayat: List[Kejadian] = List(
    Kejadian.Tanam(1, "Padi", 1.0),
    Kejadian.Pupuk(5, "Urea", 50.0),
    Kejadian.Siram(10, 200.0),
    Kejadian.Hama(30, "Wereng", 2),
    Kejadian.Panen(90, 4.0)
  )

  println("Riwayat")
  riwayat.foreach(println)

  println("\nHari tiap kejadian")
  println(riwayat.map(_.hariKe))

  println("\nUji immutability")
  val awal = KondisiLahan.kosong
  val setelahTanam = awal.copy(tanaman = Some("Padi"), luasHa = 1.0)
  println(s"Awal: $awal")
  println(s"Setelah tanam: $setelahTanam")
  println(s"Awal masih: $awal")
```

![Pengujian Struktur Data](pengujian.png)

Gambar di atas adalah hasil dari pengujian struktur datanya.

### Fungsi Inti

- `terapkan`: satu lahan + satu kejadian -> lahan baru
- `replay`: menerapkan seluruh riwayat satu per satu
- `kondisiPadaHari`: kondisi lahan di hari tertentu

Dilanjut untuk menguji fungsi inti yang telah dibuat, kami mennggunakan code di bawah:

```scala
@main def Taninote(): Unit =
  val riwayat: List[Kejadian] = List(
    Kejadian.Tanam(1, "Padi", 1.0),
    Kejadian.Pupuk(5, "Urea", 50.0),
    Kejadian.Siram(10, 200.0),
    Kejadian.Hama(30, "Wereng", 2),
    Kejadian.Panen(90, 4.0)
  )

  println("Kondisi saat ini")
  println(kondisiSaatIni(riwayat))

  println("\nKondisi pada hari ke-30")
  println(kondisiPadaHari(riwayat, 30))

  println("\nKondisi pada hari ke-10")
  println(kondisiPadaHari(riwayat, 10))

  println("\nUndo kejadian terakhir (riwayat.init)")
  println(kondisiSaatIni(riwayat.init))

  println("\nRiwayat asli masih utuh")
  println(s"Jumlah kejadian: ${riwayat.length}")
```

![Pengujian Fungsi](fungsi_inti.png)

Gambar di atas adalah hasil dari pengujian fungsi inti yang telah dibuat.

### Validasi dan Function Composition

- `validasi`: membuat fungsi pengecek kejadian dari riwayat yang ada
- `tambahKejadian`: riwayat + kejadian baru → riwayat baru (atau pesan error)
- `formatKondisi`: KondisiLahan -> teks laporan
- `laporanPadaHari`: saring -> hitung -> format, digabung

Seperti biasa, pengujian dilakukan dengan kumpulan code di bawah ini:

```scala
@main def Taninote(): Unit =
  val riwayat: List[Kejadian] = List(
    Kejadian.Tanam(1, "Padi", 1.0),
    Kejadian.Pupuk(5, "Urea", 50.0),
    Kejadian.Siram(10, 200.0),
    Kejadian.Hama(30, "Wereng", 2),
    Kejadian.Panen(90, 4.0)
  )

  println("Validasi: kejadian valid")
  println(validasi(riwayat)(Kejadian.Panen(95, 1.0)))

  println("\nValidasi: hari mundur")
  println(validasi(riwayat)(Kejadian.Siram(20, 100.0)))

  println("\nValidasi: tingkat hama salah")
  println(validasi(riwayat)(Kejadian.Hama(100, "Wereng", 5)))

  println("\nTambah kejadian valid")
  println(tambahKejadian(riwayat, Kejadian.Panen(95, 1.0)).map(_.length))
  println(s"Riwayat asli: ${riwayat.length} kejadian")

  println("\nLaporan hari ke-30 (composition andThen)")
  println(laporanPadaHari(30)(riwayat))

  println("\nLaporan lengkap (composition compose)")
  val laporanLengkap = formatKondisi compose hitung
  println(laporanLengkap(riwayat))
```

![Pengujian Composition](composition.png)

Gambar di atas adalah hasil pengujiannya.

### Chaining

Tahap ini memakai chaining, yaitu method berantai langsung pada data.

- `namaJenis`: kejadian -> nama jenisnya (string)
- `saring`: riwayat + aturan filter -> kejadian yang lolos
- `cariJenis`: cari kejadian berdasarkan jenis, urut hari terbaru
- `ringkasanJenis`: hitung jumlah kejadian tiap jenis
- `formatKejadian`: kejadian -> satu baris teks

Seperti biasa, pengujian dilakukan dengan kumpulan code di bawah ini:

```scala
@main def Taninote(): Unit =
  val riwayat: List[Kejadian] = List(
    Kejadian.Tanam(1, "Padi", 1.0),
    Kejadian.Pupuk(5, "Urea", 50.0),
    Kejadian.Siram(10, 200.0),
    Kejadian.Siram(15, 150.0),
    Kejadian.Hama(30, "Wereng", 2),
    Kejadian.Panen(90, 4.0)
  )

  println("Seluruh riwayat")
  riwayat.map(formatKejadian).foreach(println)

  println("\nCari jenis Siram (terbaru dulu)")
  cariJenis(riwayat, "siram").foreach(println)

  println("\nCari jenis Hama")
  cariJenis(riwayat, "Hama").foreach(println)

  println("\nCari jenis Pupuk (tidak ada hasil?)")
  println(cariJenis(riwayat, "Irigasi"))

  println("\nSaring dengan aturan lambda: kejadian setelah hari ke-10")
  saring(riwayat, k => k.hariKe > 10).map(formatKejadian).foreach(println)

  println("\nRingkasan jumlah kejadian per jenis")
  ringkasanJenis(riwayat).foreach((jenis, jumlah) => println(s"$jenis: $jumlah"))
```

![Pengujian Chaining](chaining.png)

### Menu Console Interaktif

- `bacaAngka`, `bacaDesimal`, `bacaTeks`: membaca input dengan aman
- `bacaKejadian`: membentuk kejadian dari input
- `proses`: menjalankan satu pilihan menu, mengembalikan riwayat (baru atau tetap)
- `loop`: mengulang menu sampai pengguna memilih keluar

Pengujian sistemnya, di presentasi aja hehe :v

---

### Tujuan dan Manfaat Sistem

Taninote adalah aplikasi console untuk mencatat dan menelusuri riwayat kegiatan di satu lahan pertanian. Sistem ini tidak menyimpan "keadaan lahan" secara langsung, tetapi menyimpan **kejadian-kejadian** yang pernah terjadi (tanam, pupuk, siram, hama, panen). Keadaan lahan dihitung ulang dari kejadian tersebut setiap kali dibutuhkan. Pendekatan ini dikenal sebagai *event sourcing* sederhana.

Tujuan sistem:

1. **Mencatat riwayat tanpa menimpa data.** Kejadian baru hanya ditambahkan, data lama tidak pernah diubah.
2. **Menghitung kondisi lahan kapan saja**, baik kondisi saat ini maupun pada hari tertentu di masa lalu.
3. **Membatalkan kesalahan input (undo)** tanpa merusak kejadian lain.
4. **Mencegah data yang tidak masuk akal** lewat validasi, misalnya jumlah negatif atau hari mundur.
5. **Menjadi contoh penerapan Functional Programming** di Scala 3: immutability, pure function, higher-order function, composition, chaining, dan rekursi.

Alur data secara umum:

```
List[Kejadian]  --replay(terapkan)-->  KondisiLahan  --formatKondisi-->  Teks laporan
 (riwayat)         (rekursi)            (hasil hitung)                    (ditampilkan)
```

---

### Cara Menjalankan Program

**Prasyarat**

- JDK 17 atau lebih baru (kami memakai Eclipse Temurin 21)
- sbt dan Scala 3 (dipasang lewat Coursier: `cs setup`)
- Opsional: VS Code dengan ekstensi *Scala (Metals)*

Versi Scala 3 yang dipakai project ini diatur di file `build.sbt`.

**Struktur project**

```
taninote/
├── build.sbt
├── project/
└── src/
    └── main/
        └── scala/
            └── Main.scala
```

**Cara 1: lewat VS Code (Metals)**

1. Buka VS Code, pilih **File → Open Folder**, lalu pilih folder project (folder yang berisi `build.sbt`, bukan folder induknya).
2. Klik **Import build** saat Metals menanyakannya, lalu tunggu sampai selesai.
3. Buka `src/main/scala/Main.scala`.
4. Klik tulisan **run** di atas baris `@main def Taninote()`.
5. Ketik pilihan menu langsung di panel **Terminal** VS Code.

**Cara 2: lewat terminal (sbt)**

```
cd taninote
sbt run
```

Eksekusi pertama kali lebih lama karena sbt mengunduh komponen yang dibutuhkan.

**Cara 3: lewat scala-cli**

```
cd taninote
scala-cli run src/main/scala/Main.scala
```

Untuk keluar dari aplikasi, ketik `0` pada menu utama.

---

### Panduan Penggunaan Menu

Program dimulai dengan **data contoh** (5 kejadian: tanam padi, pupuk urea, dua kali siram, dan serangan wereng) agar fitur langsung bisa dicoba. Data ini tidak disimpan ke file, sehingga setiap program dijalankan ulang, riwayat kembali ke data contoh.

| Menu | Fungsi | Input | Hasil |
|---|---|---|---|
| 1 | Lihat seluruh riwayat | tidak ada | Daftar kejadian, satu baris per kejadian, sesuai urutan pencatatan |
| 2 | Lihat kondisi lahan saat ini | tidak ada | Tanaman, luas, total pupuk, total air, tingkat hama, total panen |
| 3 | Lihat kondisi lahan pada hari tertentu | hari (angka) | Kondisi lahan yang dihitung hanya dari kejadian sampai hari tersebut |
| 4 | Tambah kejadian baru | jenis (1-5) lalu data kejadian | "Kejadian berhasil ditambahkan", atau "Gagal: (alasan)" |
| 5 | Batalkan kejadian terakhir | tidak ada | Kejadian terakhir dibuang dari riwayat dan ditampilkan |
| 6 | Cari kejadian berdasarkan jenis | Tanam / Pupuk / Siram / Hama / Panen | Kejadian sejenis, hari terbaru di atas |
| 7 | Laporan ringkasan musim tanam | tidak ada | Total kejadian, jumlah per jenis, dan kondisi akhir lahan |
| 0 | Keluar | tidak ada | Program berhenti |

**Data yang diminta pada menu 4**

| Jenis | Input yang diminta (berurutan) |
|---|---|
| 1. Tanam | hari, nama tanaman, luas (hektar) |
| 2. Pupuk | hari, jenis pupuk, jumlah (kg) |
| 3. Siram | hari, jumlah air (liter) |
| 4. Hama | hari, nama hama, tingkat serangan (1-3) |
| 5. Panen | hari, hasil panen (ton) |

Angka desimal boleh ditulis dengan titik atau koma (`3.5` atau `3,5`).

**Contoh 1: menambah kejadian yang valid**

```
Pilih menu: 4
Jenis kejadian: 1.Tanam  2.Pupuk  3.Siram  4.Hama  5.Panen
Pilih jenis: 5
Hari ke-: 95
Hasil panen (ton): 3.5
Kejadian berhasil ditambahkan
```

**Contoh 2: kejadian ditolak karena hari mundur** (hari terakhir pada riwayat adalah 30)

```
Pilih menu: 4
Jenis kejadian: 1.Tanam  2.Pupuk  3.Siram  4.Hama  5.Panen
Pilih jenis: 3
Hari ke-: 20
Jumlah air (liter): 100
Gagal: Hari tidak boleh lebih kecil dari kejadian terakhir (hari 30)
```

**Contoh 3: kondisi lahan pada hari ke-10** (menu 3, input `10`)

```
Kondisi lahan pada hari ke-10
Tanaman : Padi
Luas    : 1.0 ha
Pupuk   : 50.0 kg
Air     : 200.0 liter
Hama    : 0 (Aman)
Panen   : 0.0 ton
```

**Status hama.** Tingkat serangan bersifat akumulatif (dijumlahkan dari semua kejadian hama): total 0 = *Aman*, total 1 sampai 3 = *Ringan*, total di atas 3 = *Berat*.

---

### Aturan Validasi

Setiap kejadian baru diperiksa sebelum masuk ke riwayat. Jika salah satu aturan dilanggar, kejadian ditolak dan riwayat tidak berubah.

| Aturan | Pesan jika dilanggar |
|---|---|
| Hari minimal 1 | Hari harus minimal 1 |
| Hari tidak boleh lebih kecil dari hari kejadian terakhir | Hari tidak boleh lebih kecil dari kejadian terakhir (hari N) |
| Nama tanaman tidak kosong | Nama tanaman tidak boleh kosong |
| Luas lahan lebih dari 0 | Luas lahan harus lebih dari 0 |
| Jenis pupuk tidak kosong | Jenis pupuk tidak boleh kosong |
| Jumlah pupuk lebih dari 0 | Jumlah pupuk harus lebih dari 0 |
| Jumlah air lebih dari 0 | Jumlah air harus lebih dari 0 |
| Nama hama tidak kosong | Nama hama tidak boleh kosong |
| Tingkat hama 1 sampai 3 | Tingkat hama harus 1 sampai 3 |
| Hasil panen lebih dari 0 | Hasil panen harus lebih dari 0 |
| Panen hanya boleh jika sudah ada kejadian tanam | Belum ada kejadian tanam, tidak bisa panen |

Input yang bukan angka pada kolom angka (misalnya huruf pada kolom hari) juga ditolak dengan pesan "Input tidak valid, kejadian tidak ditambahkan".

---

### Penerapan Konsep Functional Programming

| Konsep | Fungsi / bagian kode | Penjelasan |
|---|---|---|
| Function value dan lambda | `formatKejadian`, `formatKondisi`, `statusHama`, `hitung`, `k => k.hariKe > 10` | Fungsi disimpan sebagai nilai (`val`) dan dipakai seperti data |
| Higher-Order Function | `replay` (menerima `aturan`), `saring` (menerima aturan filter) | Perilaku fungsi ditentukan oleh fungsi lain yang dikirim sebagai parameter |
| Fungsi sebagai nilai kembalian | `validasi(riwayat)`, `laporanPadaHari(hari)` | Keduanya mengembalikan fungsi baru |
| Pure function | `terapkan`, `validasi`, `kondisiPadaHari`, `cariJenis`, `ringkasanJenis` | Hasil hanya bergantung pada input, tanpa efek samping |
| Immutability | `case class` dan `enum`, `.copy(...)`, `:+`, `.init`, tanpa `var` | Data lama tidak berubah, setiap perubahan menghasilkan data baru |
| Conditional expression | `match` pada `terapkan`, `validasi`, `namaJenis`; `if/else` pada `statusHama` | Keputusan ditulis sebagai ekspresi yang menghasilkan nilai |
| Operasi String dan List | `mkString`, `trim`, `equalsIgnoreCase`, interpolasi `s"..."` dan `f"..."`, `filter`, `map`, `groupBy` | Pengolahan data tanpa mengubah data asal |
| Function composition | `laporanPadaHari`: `sampaiHari andThen hitung andThen formatKondisi`; `formatKondisi compose hitung` | **Menggabungkan fungsi** menjadi fungsi baru yang belum berjalan sampai diberi data |
| Chaining | `cariJenis`: `filter → sortBy → map`; `ringkasanJenis`: `groupBy → map → toList → sortBy` | **Memanggil method berantai pada data** yang langsung diproses |
| Rekursi dan tail recursion | `replay` (`@tailrec`) dan `loop` (`@tailrec`) | Base case dan recursive case dijelaskan di bawah |

**Composition vs chaining.** Composition menggabungkan fungsi lebih dulu, lalu dijalankan nanti saat diberi data (`laporanPadaHari(30)(riwayat)`). Chaining langsung memproses data lewat method berantai (`riwayat.filter(...).sortBy(...).map(...)`).

**Rekursi pada `replay`**

- *Base case:* riwayat kosong (`Nil`), maka kembalikan kondisi yang sudah terkumpul.
- *Recursive case:* ambil satu kejadian, terapkan ke kondisi, lalu panggil `replay` untuk sisa riwayat.

Contoh alur untuk riwayat `[Tanam, Pupuk, Siram]`:

```
replay([Tanam, Pupuk, Siram], kosong)
 → replay([Pupuk, Siram], kosong + Tanam)
 → replay([Siram], ... + Pupuk)
 → replay([], ... + Siram)
 → Nil: kembalikan kondisi akhir
```

**Rekursi pada `loop` (menu).** Menggantikan `while`.

- *Base case:* pengguna memilih `0`, program menampilkan pesan penutup dan berhenti.
- *Recursive case:* proses pilihan menu, lalu panggil `loop` lagi dengan riwayat terbaru.

Riwayat tidak disimpan di `var`, tetapi dioper sebagai parameter ke putaran berikutnya. Kedua pemanggilan rekursif berada di posisi terakhir sehingga memenuhi tail recursion, dan dijamin oleh anotasi `@tailrec`.

---

### Batasan Sistem

- Data tidak disimpan ke file. Perubahan hilang saat program ditutup.
- Sistem mencatat satu lahan dan satu tanaman. Jika ada kejadian tanam kedua, nama tanaman dan luas lahan ditimpa, sedangkan total pupuk, air, hama, dan panen tetap dijumlahkan.
- Undo hanya membatalkan kejadian terakhir (bisa diulang berkali-kali), dan tidak ada fitur redo.
- Pengembangan lanjutan (final project): penyimpanan ke file, banyak lahan, dan laporan produktivitas per hektar.