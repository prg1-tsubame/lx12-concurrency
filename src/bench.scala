package lx12.benchmark

import collection.parallel.mutable.ParTrieMap
import collection.parallel.ForkJoinTaskSupport
import scala.util.Random

object Benchmark {

  def triX(): Unit = {
    val length = 300000//sys.props("length").toInt
    val par = 1//sys.props("par").toInt
    val partrie = ParTrieMap((0 until length) zip (0 until length): _*)

    partrie.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(par))
    //partrie.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(par))

    var r = 0

    for (i <- 1 to 10) {
      val t_start = System.nanoTime()
      val pt = partrie map { kv => kv }
      print(f"  ${(System.nanoTime() - t_start) / 1000000}msec")
      r = r + partrie(Random.nextInt(length))
    }
    println()
    println(r)
  }

  @main def run() = {
    triX()
  }
}
