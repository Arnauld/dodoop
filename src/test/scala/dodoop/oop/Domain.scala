package dodoop.oop

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.lang.IllegalStateException

trait HasHints {
  var hints = Map.empty[String, Int]
  var required = Set.empty[String]

  def isFulfilledBy(other: HasHints): Boolean = {
    for (req <- required)
      if (!other.hints.contains(req))
        return false
    true
  }
}

case class Account(id: Int) extends HasHints {
  var visibles: List[Request] = Nil
}

case class Request(id: Int) extends HasHints

object Domain {
  val NB_REQ = 1024 * 4
  val NB_ACC = 512
  val NB_HINTS = 128
  var requests: List[Request] = Nil
  var accounts: List[Account] = Nil

  def loadAccounts() {
    accounts = (for (i <- 0 until NB_REQ)
    yield loadAccount(i)).toList
  }

  def loadAccount(accountId: Int): Account = {
    val acc = Account(accountId)
    acc.hints = weights(accountId)
    acc
  }

  def fillPool() {
    requests = (for (i <- 0 until NB_REQ)
    yield loadRequest(i)).toList
  }

  def loadRequest(requestId: Int): Request = {
    val req = Request(requestId)
    req.required = (mailboxOf(requestId) :: localeOf(requestId) :: requiredKeywordOf(requestId) :: Nil).toSet
    req.hints = weights(requestId)
    req
  }

  val mailboxes = Array("Support", "Marketing", "Sales", "Engineering", "CustomerCare", "Training", "PreSale")

  def mailboxOf(id: Int) = mailboxes(id % mailboxes.length)

  val locales = Array("fr_FR", "en_US", "en_GB", "it_IT", "pt_BR")

  def localeOf(id: Int) = locales(id % locales.length)

  val keywords = "contract,meeting,article,social,opportunity,angry,unhappy,broken,bug,performance,crash,OutOfMemory,release,behavior,functionality,regression,lawyer".split(",")

  def requiredKeywordOf(id: Int) = keywords(id % keywords.length)

  def weights(id: Int): Map[String, Int] = {
    var map = Map.empty[String, Int]
    map += (mailboxOf(id) -> 250)
    map += (localeOf(id) -> 345)
    for (i <- 1 until 5)
      map += (requiredKeywordOf(id * i) -> i * 35)
    map
  }

  def calculateVisibiliyOOP() {
    for (account <- accounts) {
      var visibles: List[Request] = Nil
      for (request <- requests) {
        if (request.isFulfilledBy(account))
          visibles = request :: visibles
      }
      account.visibles = visibles
    }
  }

  def hintCount = {
    val required = mailboxes.length + locales.length + keywords.length
    if(required<NB_HINTS)
      NB_HINTS
    else
      required
  }

  def numberOfMasksForHints = (hintCount/32 +1)

  def allRequestsRequired: Array[Int] = {
    val hCount = hintCount
    val requestsRequired = newArray(NB_REQ * hCount)
    var offset = 0
    for (i <- 0 until requests.length) {
      val request = requests(i)
      request.required.foreach({
        t =>
          requestsRequired(offset + indexOf(t)) = 1
      })
      offset = offset + hCount
    }
    requestsRequired
  }

  def allAccountsHints: Array[Int] = {
    val hCount = hintCount
    val accountsHints = newArray(NB_ACC * hCount)
    var offset = 0
    for (i <- 0 until NB_ACC) {
      accounts(i).hints.foreach({
        t =>
          accountsHints(offset + indexOf(t._1)) = 1
      })
      offset = offset + hCount
    }
    accountsHints
  }

  def newArray(size: Int) = {
    val array = new Array[Int](size)
    for (j <- 0 until size)
      array(j) = 0
    array
  }

  val hintIndexGen = new AtomicInteger()
  val hintIndices = new ConcurrentHashMap[String, Int]()

  def indexOf(hintName: String) = {
    val index = synchronized {
      if (hintIndices.containsKey(hintName)) {
        hintIndices.get(hintName)
      }
      else {
        val index = hintIndexGen.getAndIncrement
        hintIndices.put(hintName, index)
        index
      }
    }
    if(index>=hintCount) {
      import scala.collection.JavaConversions._
      val debug = hintIndices.toList.sortWith({(lt1,lt2) => lt1._2 > lt2._2}).foldLeft("")({ _ + "\n" + _})
      throw new IllegalStateException("Ouch! got: " + index +" should be <" + hintCount + ": " + debug)
    }
    index
  }

}