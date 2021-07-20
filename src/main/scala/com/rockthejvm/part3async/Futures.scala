package com.rockthejvm.part3async

import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Failure, Random, Success, Try}
import scala.concurrent.duration.*

object Futures {

  def calculateMeaningOfLife(): Int = {
    // simulate long compute
    Thread.sleep(1000)
    42
  }

  // thread pool (Java-specific)
  val executor = Executors.newFixedThreadPool(4)
  // thread pool (Scala-specific)
  given executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executor)

  // a future = an async computation that will finish at some point
  val aFuture: Future[Int] = Future(calculateMeaningOfLife()) // given executionContext will be passed here

  // Option[Try[Int]], because
  // - we don't know if we have a value
  // - if we do, that can be a failed computation
  val futureInstantResult: Option[Try[Int]] = aFuture.value // inspect the value of the future RIGHT NOW

  // callbacks
  aFuture.onComplete {
    case Success(value) => println(s"I've completed with the meaning of life: $value")
    case Failure(ex) => println(s"My async computation failed: $ex")
  } // on SOME other thread

  /*
    Functional Programming on Futures
    Motivation: onComplete is a hassle
   */
  case class Profile(id: String, name: String) {
    def sendMessage(anotherProfile: Profile, message: String) =
      println(s"${this.name} sending message to ${anotherProfile.name}: $message")
  }

  object SocialNetwork {
    // "database"
    val names = Map(
      "rtjvm.id.1-daniel" -> "Daniel",
      "rtjvm.id.2-jane" -> "Jane",
      "rtjvm.id.3-mark" -> "Mark",
    )

    // friends "database"
    val friends = Map(
      "rtjvm.id.2-jane" -> "rtjvm.id.3-mark"
    )

    val random = new Random()

    // "API"
    def fetchProfile(id: String): Future[Profile] = Future {
      // fetch something from the database
      Thread.sleep(random.nextInt(300)) // simulate the time delay
      Profile(id, names(id))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(400))
      val bestFriendId = friends(profile.id)
      Profile(bestFriendId, names(bestFriendId))
    }
  }

  // problem: sending a message to my best friend
  def sendMessageToBestFriend(accountId: String, message: String): Unit = {
    // 1 - call fetchProfile
    // 2 - call fetchBestFriend
    // 3 - call profile.sendMessage(bestFriend)
    val profileFuture = SocialNetwork.fetchProfile(accountId)
    profileFuture.onComplete {
      case Success(profile) => // "code block"
        val friendProfileFuture = SocialNetwork.fetchBestFriend(profile)
        friendProfileFuture.onComplete {
          case Success(friendProfile) => profile.sendMessage(friendProfile, message)
          case Failure(e) => e.printStackTrace()
        }
      case Failure(ex) => ex.printStackTrace()
    }
  } // onComplete is such a pain - callback hell!

  def sendMessageToBestFriend_v2(accountId: String, message: String): Unit = {
    val profileFuture = SocialNetwork.fetchProfile(accountId)
    val action = profileFuture.flatMap { profile => // Future[Unit]
      SocialNetwork.fetchBestFriend(profile).map { bestFriend => // Future[Unit]
        profile.sendMessage(bestFriend, message) // unit
      }
    }
  }

  def sendMessageToBestFriend_v3(accountId: String, message: String): Unit =
    for {
      profile <- SocialNetwork.fetchProfile(accountId)
      bestFriend <- SocialNetwork.fetchBestFriend(profile)
    } yield profile.sendMessage(bestFriend, message) // identical to v2

  val janeProfileFuture = SocialNetwork.fetchProfile("rtjvm.id.2-jane")
  val janeFuture: Future[String] = janeProfileFuture.map(profile => profile.name) // map transforms value contained inside, ASYNCHRONOUSLY
  val janesBestFriend: Future[Profile] = janeProfileFuture.flatMap(profile => SocialNetwork.fetchBestFriend(profile))
  val janesBestFriendFilter: Future[Profile] = janesBestFriend.filter(profile => profile.name.startsWith("Z"))

  // fallbacks
  val profileNoMatterWhat: Future[Profile] = SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => Profile("rtjvm.id.0-dummy", "Forever alone")
  }

  val aFetchedProfileNoMatterWhat: Future[Profile] = SocialNetwork.fetchProfile("unknown id").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("rtjvm.id.0-dummy")
  }

  val fallBackProfile: Future[Profile] = SocialNetwork.fetchProfile("unknown id").fallbackTo(SocialNetwork.fetchProfile("rtjvm.id.0-dummy"))

  /*
    Blocking the calling thread for future completion
    Example: a transaction that must go through.
   */
  case class User(name: String)
  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    // "APIs"
    def fetchUser(name: String): Future[User] = Future {
      // simulate some DB fetching
      Thread.sleep(500)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transaction] = Future {
      // simulate payment
      Thread.sleep(1000)
      Transaction(user.name, merchantName, amount, "SUCCESS")
    }

    // "external API"
    def purchase(username: String, item: String, merchantName: String, price: Double): String = {
      /*
        1. fetch user
        2. create transaction
        3. WAIT for the txn to finish
       */
      val transactionStatusFuture: Future[String] = for {
        user <- fetchUser(username)
        transaction <- createTransaction(user, merchantName, price)
      } yield transaction.status

      // blocking call - not recommended unless in VERY STRICT circumstances
      Await.result(transactionStatusFuture, 2.seconds) // throws TimeoutException if the future doesn't finish within 2s
    }
  }

  /*
    Promises: a technique for controlling the completion of Futures
   */
  def demoPromises(): Unit = {
    val promise = Promise[Int]()
    val futureInside: Future[Int] = promise.future

    // thread 1 - "consumer": monitor the future for completion
    futureInside.onComplete {
      case Success(value) => println(s"[consumer] I've just been completed with $value")
      case Failure(ex) => ex.printStackTrace()
    }

    // thread 2 - "producer"
    val producerThread = new Thread(() => {
      println("[producer] Crunching numbers...")
      Thread.sleep(1000)
      // "fulfil" the promise
      promise.success(42)
      println("[producer] I'm done.")
    })

    producerThread.start()
  }

  /**
    Exercises
    1) fulfil a future IMMEDIATELY with a value
    2) in sequence: make sure the first Future has been completed before returning the second
    3) first(fa, fb) => new Future with the value of the first Future to complete
    4) last(fa, fb) => new Future with the value of the LAST Future to complete
    5) retry an action returning a Future until a predicate holds true
   */

  // 1
  def completeImmediately[A](value: A): Future[A] = Future(value) // async completion as soon as possible
  def completeImmediately_v2[A](value: A): Future[A] = Future.successful(value) // synchronous completion

  // 2
  def inSequence[A, B](first: Future[A], second: Future[B]): Future[B] =
    first.flatMap(_ => second)

  // 3
  def first[A](f1: Future[A], f2: Future[A]): Future[A] = {
    val promise = Promise[A]()
    f1.onComplete(result1 => promise.tryComplete(result1))
    f2.onComplete(result2 => promise.tryComplete(result2))

    promise.future
  }

  // 4
  def last[A](f1: Future[A], f2: Future[A]): Future[A] = {
    val bothPromise = Promise[A]()
    val lastPromise = Promise[A]()

    def checkAndComplete(result: Try[A]): Unit =
      if (!bothPromise.tryComplete(result))
        lastPromise.complete(result)

    f1.onComplete(checkAndComplete)
    f2.onComplete(checkAndComplete)

    lastPromise.future
  }

  def testFirstLast(): Unit = {
    lazy val fast = Future {
      Thread.sleep(100)
      1
    }
    lazy val slow = Future {
      Thread.sleep(200)
      2
    }
    first(fast, slow).foreach(result => println(s"FIRST: $result"))
    last(fast, slow).foreach(result => println(s"LAST: $result"))
  }

  // 5
  def retryUntil[A](action: () => Future[A], predicate: A => Boolean): Future[A] =
    action()
      .filter(predicate)
      .recoverWith {
        case _ => retryUntil(action, predicate)
      }

  def testRetries(): Unit = {
    val random = new Random()
    val action = () => Future {
      Thread.sleep(100)
      val nextValue = random.nextInt(100)
      println(s"Generated $nextValue")
      nextValue
    }

    val predicate = (x: Int) => x < 10

    retryUntil(action, predicate).foreach(finalResult => println(s"Settled at $finalResult"))
  }

  def main(args: Array[String]): Unit = {
    testRetries()
    Thread.sleep(2000)
    executor.shutdown()
  }
}
