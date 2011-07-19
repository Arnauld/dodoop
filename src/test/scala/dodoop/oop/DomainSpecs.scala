package dodoop.oop

import org.specs.Specification

class DomainSpecs extends Specification {

  "HasHints" should {
    "work on simple case" in {
      Domain.fillPool()
      Domain.loadAccounts()
      val beg = System.nanoTime()
      Domain.calculateVisibiliyOOP()
      val end = System.nanoTime()
      println("Elapsed OOP: " + (end-beg)/1e6)

      val hintCount = Domain.hintCount
      val requests = Domain.allRequestsRequired
      val accounts = Domain.allAccountsHints


      var aOffset = 0
      var rOffset = 0
      /*
       *  iterate over accounts and then requests
       */
      val allAccountsVisibles = newArrayOfNil(Domain.NB_ACC)
      val begDOP = System.nanoTime()
      for(ai <- 0 until Domain.NB_ACC) {
        var accountVisibles:List[Int] = Nil
        rOffset = 0
        for(ri <- 0 until Domain.NB_REQ) {
          var visible = true
          for(k <- 0 until hintCount) {
            if(requests(rOffset+k) > 0)
              visible = visible & (accounts(aOffset+k) > 0)
          }
          if(visible)
            accountVisibles = ri :: accountVisibles
          rOffset = rOffset + hintCount
        }
        allAccountsVisibles(ai) = accountVisibles
        aOffset = aOffset + hintCount
      }
      val endDOP = System.nanoTime()
      println("Elapsed DOP (acc->req): " + (endDOP-begDOP)/1e6)

      // check both results are the same
      for(ai <- 0 until Domain.NB_ACC) {
        val oopVisibles = Domain.accounts(ai).visibles.map(_.id).sorted
        val dopVisibles = allAccountsVisibles(ai).sorted
        dopVisibles must_== oopVisibles
      }

      /*
       *  iterate over requests and then accounts
       */
      val allAccountsVisibles2 = newArrayOfNil(Domain.NB_ACC)
      val begDOP2 = System.nanoTime()
      rOffset = 0
      for(ri <- 0 until Domain.NB_REQ) {
        aOffset = 0
        for(ai <- 0 until Domain.NB_ACC) {
          var visible = true
          for(k <- 0 until hintCount) {
            if(requests(rOffset+k) > 0)
              visible = visible & (accounts(aOffset+k) > 0)
          }
          if(visible) {
            allAccountsVisibles2(ai) = ri :: allAccountsVisibles2(ai)
          }
          aOffset = aOffset + hintCount
        }
        rOffset = rOffset + hintCount
      }
      val endDOP2 = System.nanoTime()
      println("Elapsed DOP (req->acc): " + (endDOP2-begDOP2)/1e6)

      // check both results are the same
      for(ai <- 0 until Domain.NB_ACC) {
        val oopVisibles = Domain.accounts(ai).visibles.map(_.id).sorted
        val dopVisibles = allAccountsVisibles2(ai).sorted
        dopVisibles must_== oopVisibles
      }
    }
  }

  def newArrayOfNil(size:Int) = {
    val array = new Array[List[Int]](size)
      for(i <- 0 until size)
        array(i) = Nil
    array
  }
}