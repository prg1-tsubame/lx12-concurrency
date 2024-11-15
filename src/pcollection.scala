package lx12.pcollections

import scala.util.Random
import scala.collection.parallel.CollectionConverters._

// 参考文献
// A. Prokopec and H. Miller, "Parallel collections,"
// http://docs.scala-lang.org/ja/overviews/parallel-collections/overview
// Eugene Yokota 訳

object Par {

  def fib(n: Int, i: Int, f1: Int, f2: Int): Int = {
    if (i < n) fib(n, i + 1, f1 + f2, f1)
    else if (i == n) f1
    else if (i == 0) f2
    else 0
  }

  val plist = (1 to 1000).toList.par

  def simple(): Int = {

    val N = 1 << 16
    val vec = Range(0, N).toArray
    val pvec = vec.par
    val c = 100
    var a = 0

    {
      println("普通の配列を使った場合")
      var t_start = System.nanoTime()
      for (i <- -5 to c) {
        if (i == 0) t_start = System.nanoTime()
        val vecfib = vec.map((v: Int) => fib(v % 1000, 1, 1, 1))
        a = a + vecfib(Random.nextInt(vecfib.length))
      }
      println(f"${(System.nanoTime() - t_start) * 1e-9}%2.2fsec")
    }

    {
      println("並列化した配列を使った場合")
      var t_start = System.nanoTime()
      for (i <- -5 to c) {
        if (i == 0) t_start = System.nanoTime()
        val vecfib = pvec.map((v: Int) => fib(v % 1000, 1, 1, 1))
        a = a + vecfib(Random.nextInt(vecfib.length))
      }
      println(f"${(System.nanoTime() - t_start) * 1e-9}%2.2fsec")
    }

    a
  }

  def mapX(): Unit = {
    val lastNames = List("Smith", "Jones", "Frankenstein", "Bach", "Jackson", "Rodin").par

    print(lastNames.map(_.toUpperCase))
  }

  def foldX(): Unit = {
    val pvec = (1 to 10000).toArray.par
    val sum = pvec.fold(0)((accu: Int, v: Int) => accu + v)
    println(f"1 + 2 + ... + 10000 = $sum")
  }

  def sideEffectX(): Unit = {
    println("\n同じ変数に並列にさわるとおかしなことになる。")
    for (i <- 1 to 3) {
      var sum = 0
      plist.foreach((v: Int) => sum = sum + v)
      println(f"sum = $sum")
    }
  }

  def assocX(): Unit = {
    // Out-of-order実行のため並列演算は結合律が成立するものでなければならない。

    // x + (y + z) == (x + y) + z
    println("\n結合律が成立する演算に対しては結果は安定している")
    for (i <- 1 to 3) println(plist.reduce((accu: Int, v: Int) => accu + v))

    // x - (y - z) != (x - y) - z
    println("\n結合律が成立しない演算だと結果は不確定")
    for (i <- 1 to 3) println(plist.reduce((accu: Int, v: Int) => accu - v))

    // s1 ++ s2 != s2 ++ s1
    // s1 ++ (s2 ++ s3) == (s1 ++ s2) ++ s3
    println("\n交換律は成立しないが、結合律は成立する例（文字列の連結）")
    val strings = List("abc","def","ghi","jkl","mno","pqr","stu","vwx","yz").par
    println(f"${strings.reduce((s1: String, s2: String) => s1 ++ " " ++ s2)}")
  }

  @main def run(command: String): Unit = {
    println(f"CPUコア数 = ${Runtime.getRuntime().availableProcessors()}")

    command match {
      case "map" => mapX()
      case "fold" => foldX()
      case "sideeffect" => sideEffectX()
      case "assoc" => assocX()
      case "simple" => simple()
      case "help" | _ => println("実行方法は sbt から: runMain lx12.pcollections.run (map|fold|sideeffect|assoc|simple|help)")
    }
  }
}
