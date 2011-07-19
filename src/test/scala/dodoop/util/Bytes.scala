package dodoop.util

object Bytes {

  def nearestSuperiorPowerOfTwo(value:Int) = {
    if(value==0)
      0
    else
      32 - Integer.numberOfLeadingZeros(value - 1)
  }
}