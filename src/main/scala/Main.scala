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
    case Siram(h, _)    => h
    case Hama(h, _, _)  => h
    case Panen(h, _)    => h

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