package dodoop.cache

import org.specs.Specification

/**
 * from http://igoro.com/archive/gallery-of-processor-cache-effects/
 */
class CacheSpecs extends Specification {
  val len = 64 * 1024 * 1024
  val nbLoop = 16

  "Gallery of Processor Cache Effects" should {
    "Memory accesses and performance" in {
      val arr = new Array[Int](len)
      warmIt(arr)

      val loop1 = repeat(()=>{
        // Loop 1
        for(i <- 0 until len)
          arr(i) = arr(i)*3
      })

      val loop2 = repeat(()=>{
        // Loop 2
        for(i <- 0 until len by 16)
          arr(i) = arr(i)*3
      })

      println("Loop 1.....: " + (loop1)/(1e6*nbLoop))
      println("Loop 2.....: " + (loop2)/(1e6*nbLoop))
    }

    "Impact of cache lines" in {
      val arr = new Array[Int](len)
      warmIt(arr)

      val steps = Array(1, 2, 4, 8, 15, 16, 17, 31, 32, 33, 62, 63, 64, 65, 128,256,512,1024)
      for(inc <- steps) {
        val loopInc = repeat(()=>{
          for(i <- 0 until len by inc)
            arr(i) = arr(i)*3
        })
        println(inc + "\t\t" + loopInc/(1e6*nbLoop))
      }
    }
  }

  def warmIt(arr:Array[Int]) {
    for(k <- 1 until 5)
    for(i <- 0 until len)
       arr(i) = arr(i)*3
  }

  def repeat(c:()=>Any):Long = {
    val loopBeg = System.nanoTime()
    for(k <- 0 until nbLoop) {
      c()
    }
    val loopEnd = System.nanoTime()
    loopEnd-loopBeg
  }
}