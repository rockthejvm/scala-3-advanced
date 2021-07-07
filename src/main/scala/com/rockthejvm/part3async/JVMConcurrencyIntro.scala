package com.rockthejvm.part3async

import java.util.concurrent.Executors

object JVMConcurrencyIntro {

  def basicThreads(): Unit = {
    val runnable = new Runnable {
      override def run(): Unit = {
        println("waiting...")
        Thread.sleep(2000)
        println("running on some thread")
      }
    }

    // threads on the JVM
    val aThread = new Thread(runnable)
    aThread.start() // will run the runnable on some JVM thread
    // JVM thread == OS thread (soon to change via Project Loom)
    aThread.join() // block until thread finishes
  }

  // order of operations is NOT guaranteed
  // different runs = different results!
  def orderOfExecution(): Unit = {
    val threadHello = new Thread(() => (1 to 100).foreach(_ => println("hello")))
    val threadGoodbye = new Thread(() => (1 to 100).foreach(_ => println("goodbye")))
    threadHello.start()
    threadGoodbye.start()
  }

  // executors
  def demoExecutors(): Unit = {
    val threadPool = Executors.newFixedThreadPool(4)
    // submit a computation
    threadPool.execute(() => println("something in the thread pool"))

    threadPool.execute { () =>
      Thread.sleep(1000)
      println("done after one second")
    }

    threadPool.execute { () =>
      Thread.sleep(1000)
      println("almost done")
      Thread.sleep(1000)
      println("done after 2 seconds")
    }

    threadPool.shutdown()
    // threadPool.execute(() => println("this should NOT apeear")) // should throw an exception in the calling thread
  }

  def main(args: Array[String]): Unit = {
    demoExecutors()
  }
}
