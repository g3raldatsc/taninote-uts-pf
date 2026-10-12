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