import scala.annotation.tailrec

// Struktur Data Taninote
// Kejadian: apa yang terjadi di lahan
enum Kejadian:
  case Tanam(hari: Int, tanaman: String, luasHa: Double)
  case Pupuk(hari: Int, jenis: String, jumlahKg: Double)
  case Siram(hari: Int, jumlahLiter: Double)
  case Hama(hari: Int, nama: String, tingkat: Int)
  case Panen(hari: Int, hasilTon: Double)

// 'hari' dimiliki semua jenis kejadian, diambil lewat match expression
  def hariKe: Int = this match
    case Tanam(h, _, _) => h
    case Pupuk(h, _, _) => h
    case Siram(h, _) => h
    case Hama(h, _, _) => h
    case Panen(h, _) => h

// KondisiLahan: hasil hitungan dari kejadian
case class KondisiLahan(
  tanaman: Option[String],
  luasHa: Double,
  totalPupukKg: Double,
  totalAirLiter: Double,
  tingkatHama: Int,
  totalPanenTon: Double
)

object KondisiLahan:
  // Keadaan awal: lahan kosong, titik mulai saat menghitung kejadian
  val kosong: KondisiLahan = KondisiLahan(None, 0.0, 0.0, 0.0, 0, 0.0)


// Pure function: lahan lama + satu kejadian -> lahan baru
def terapkan(lahan: KondisiLahan, kejadian: Kejadian): KondisiLahan =
  kejadian match
    case Kejadian.Tanam(_, tanaman, luas) => lahan.copy(tanaman = Some(tanaman), luasHa = luas)
    case Kejadian.Pupuk(_, _, jumlah) => lahan.copy(totalPupukKg = lahan.totalPupukKg + jumlah)
    case Kejadian.Siram(_, liter) => lahan.copy(totalAirLiter = lahan.totalAirLiter + liter)
    case Kejadian.Hama(_, _, tingkat) => lahan.copy(tingkatHama = lahan.tingkatHama + tingkat)
    case Kejadian.Panen(_, hasil) => lahan.copy(totalPanenTon = lahan.totalPanenTon + hasil)

// Higher-Order Function + tail recursion
// 'aturan' ini itu function value yang dikirim sebagai parameter
@tailrec
def replay(
  riwayat: List[Kejadian],
  kondisi: KondisiLahan,
  aturan: (KondisiLahan, Kejadian) => KondisiLahan
): KondisiLahan =
  riwayat match
    case Nil => kondisi // base case
    case kejadian :: sisa => replay(sisa, aturan(kondisi, kejadian), aturan) // recursive case

// Kondisi lahan dari seluruh riwayat
def kondisiSaatIni(riwayat: List[Kejadian]): KondisiLahan =
  replay(riwayat, KondisiLahan.kosong, terapkan)

// Kondisi lahan pada hari tertentu: saring kejadian sampai hari itu, lalu replay
def kondisiPadaHari(riwayat: List[Kejadian], hari: Int): KondisiLahan =
  kondisiSaatIni(riwayat.filter(_.hariKe <= hari))


// Fungsi yang mengembalikan fungsi: membuat pengecek kejadian berdasarkan riwayat saat ini
def validasi(riwayat: List[Kejadian]): Kejadian => Either[String, Kejadian] =
  kejadian =>
    val hariTerakhir = riwayat.map(_.hariKe).maxOption.getOrElse(0)
    val sudahTanam = riwayat.exists(_.isInstanceOf[Kejadian.Tanam])

    kejadian match
      case k if k.hariKe < 1 =>
        Left("Hari harus minimal 1")
      case k if k.hariKe < hariTerakhir =>
        Left(s"Hari tidak boleh lebih kecil dari kejadian terakhir (hari $hariTerakhir)")
      case Kejadian.Tanam(_, tanaman, _) if tanaman.trim.isEmpty =>
        Left("Nama tanaman tidak boleh kosong")
      case Kejadian.Tanam(_, _, luas) if luas <= 0 =>
        Left("Luas lahan harus lebih dari 0")
      case Kejadian.Pupuk(_, _, jumlah) if jumlah <= 0 =>
        Left("Jumlah pupuk harus lebih dari 0")
      case Kejadian.Siram(_, liter) if liter <= 0 =>
        Left("Jumlah air harus lebih dari 0")
      case Kejadian.Hama(_, _, tingkat) if tingkat < 1 || tingkat > 3 =>
        Left("Tingkat hama harus 1 sampai 3")
      case Kejadian.Panen(_, hasil) if hasil <= 0 =>
        Left("Hasil panen harus lebih dari 0")
      case Kejadian.Panen(_, _) if !sudahTanam =>
        Left("Belum ada kejadian tanam, tidak bisa panen")
      case _ =>
        Right(kejadian)

// Riwayat baru = riwayat lama + kejadian (kalau valid). Riwayat lama tidak berubah.
def tambahKejadian(riwayat: List[Kejadian], kejadian: Kejadian): Either[String, List[Kejadian]] =
  validasi(riwayat)(kejadian).map(valid => riwayat :+ valid)

// Function value: tingkat hama -> label
val statusHama: Int => String =
  tingkat => if tingkat == 0 then "Aman" else if tingkat <= 3 then "Ringan" else "Berat"

// Function value: kondisi lahan -> teks laporan
val formatKondisi: KondisiLahan => String =
  k => List(
    s"Tanaman : ${k.tanaman.getOrElse("-")}",
    s"Luas : ${k.luasHa} ha",
    s"Pupuk : ${k.totalPupukKg} kg",
    s"Air : ${k.totalAirLiter} liter",
    s"Hama : ${k.tingkatHama} (${statusHama(k.tingkatHama)})",
    s"Panen : ${k.totalPanenTon} ton"
  ).mkString("\n")

// Function value: riwayat -> kondisi lahan
val hitung: List[Kejadian] => KondisiLahan = kondisiSaatIni

// Composition: tiga fungsi kecil digabung jadi satu fungsi baru
// Belum menjalankan apa-apa sampai diberi riwayat.
def laporanPadaHari(hari: Int): List[Kejadian] => String =
  val saring: List[Kejadian] => List[Kejadian] = _.filter(_.hariKe <= hari)
  saring andThen hitung andThen formatKondisi


// Nama jenis kejadian sebagai String
def namaJenis(kejadian: Kejadian): String =
  kejadian match
    case _: Kejadian.Tanam => "Tanam"
    case _: Kejadian.Pupuk => "Pupuk"
    case _: Kejadian.Siram => "Siram"
    case _: Kejadian.Hama  => "Hama"
    case _: Kejadian.Panen => "Panen"

// Higher-Order Function: aturan filter dikirim sebagai parameter
def saring(riwayat: List[Kejadian], aturan: Kejadian => Boolean): List[Kejadian] =
  riwayat.filter(aturan)

// Function value: satu kejadian -> satu baris teks
val formatKejadian: Kejadian => String =
  case Kejadian.Tanam(h, t, l) => f"Hari $h%3d | Tanam | $t, $l%.1f ha"
  case Kejadian.Pupuk(h, j, k) => f"Hari $h%3d | Pupuk | $j, $k%.1f kg"
  case Kejadian.Siram(h, l) => f"Hari $h%3d | Siram | $l%.1f liter"
  case Kejadian.Hama(h, n, t) => f"Hari $h%3d | Hama  | $n, tingkat $t"
  case Kejadian.Panen(h, t) => f"Hari $h%3d | Panen | $t%.1f ton"

// Chaining: filter -> sortBy (hari terbaru dulu) -> map ke teks
def cariJenis(riwayat: List[Kejadian], jenis: String): List[String] =
  riwayat
    .filter(k => namaJenis(k).equalsIgnoreCase(jenis))
    .sortBy(k => -k.hariKe)
    .map(formatKejadian)

// CHAINING: groupBy -> map -> toList -> sortBy
def ringkasanJenis(riwayat: List[Kejadian]): List[(String, Int)] =
  riwayat
    .groupBy(namaJenis)
    .map((jenis, daftar) => (jenis, daftar.length))
    .toList
    .sortBy(_._1)

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