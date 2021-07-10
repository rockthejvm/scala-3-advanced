package com.rockthejvm.part3async

import scala.collection.mutable
import scala.util.Random

object JVMThreadCommunication {
  def main(args: Array[String]): Unit = {
    ProdConsV4.start(4, 2, 5)
  }
}

// example: the producer-consumer problem
class SimpleContainer {
  private var value: Int = 0

  def isEmpty: Boolean =
    value == 0

  def set(newValue: Int): Unit =
    value = newValue

  def get: Int = {
    val result = value
    value = 0
    result
  }
}

// one producer, one consumer, busy waiting
object ProdConsV1 {
  def start(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      // busy waiting
      while (container.isEmpty) {
        println("[consumer] waiting for a value..")
      }

      println(s"[consumer] I have consumed a value: ${container.get}")
    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(500)
      val value = 42
      println(s"[producer] I am producing, after LONG work, the value $value")
      container.set(value)
    })

    consumer.start()
    producer.start()
  }
}

// wait + notify
object ProdConsV2 {
  def start(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")

      container.synchronized { // block all other threads trying to "lock" this object
        // thread-safe code
        if (container.isEmpty)
          container.wait() // release the lock + suspend the thread
        // reacquire the lock here
        // continue execution
        println(s"[consumer] I have consumed a value: ${container.get}")
      }
    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(500)
      val value = 42

      container.synchronized {
        println(s"[producer] I am producing, after LONG work, the value $value")
        container.set(value)
        container.notify() // awaken ONE suspended thread on this object
      } // release the lock
    })

    consumer.start()
    producer.start()
  }
}

// insert a larger container
// producer -> [ _ _ _ ] -> consumer
object ProdConsV3 {
  def start(containerCapacity: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]

    val consumer = new Thread(() => {
      val random = new Random(System.nanoTime())

      while (true) {
        buffer.synchronized {
          if (buffer.isEmpty) {
            println("[consumer] buffer empty, waiting...")
            buffer.wait()
          }

          // buffer must not be empty
          val x = buffer.dequeue()
          println(s"[consumer] I've just consumed $x")

          // producer, gimmme more elements!
          buffer.notify() // wake up the producer if it's asleep
        }

        Thread.sleep(random.nextInt(500))
      }
    })

    val producer = new Thread(() => {
      val random = new Random(System.nanoTime())
      var counter = 0

      while (true) {
        buffer.synchronized {
          if (buffer.size == containerCapacity) {
            println("[producer] buffer full, waiting...")
            buffer.wait()
          }

          // buffer is not empty
          val newElement = counter
          counter += 1
          println(s"[producer] I'm producing $newElement")
          buffer.enqueue(newElement)

          // consumer, don't be lazy!
          buffer.notify() // wakes up the consumer (if it's asleep)
        }

        Thread.sleep(random.nextInt(500))
      }
    })

    consumer.start()
    producer.start()
  }
}

/*
   large container, multiple producers/consumers
   producer1 -> [ _ _ _ ] -> consumer1
   producer2 ---^       +---> consumer2
 */
object ProdConsV4 {
  class Consumer(id: Int, buffer: mutable.Queue[Int]) extends Thread {
    override def run(): Unit = {
      val random = new Random(System.nanoTime())

      while (true) {
        buffer.synchronized {
          /*
            we need to constantly check if the buffer is empty - otherwise, crashing scenario:
              one producer, two consumers
              producer produces 1 value in the buffer
              both consumers are waiting
              producer calls notify, awakens one consumer
              consumer dequeues, calls notify, awakens the other consumer
              the other consumer awakens, tries dequeuing, CRASH
           */
          while (buffer.isEmpty) {
            println(s"[consumer $id] buffer empty, waiting...")
            buffer.wait()
          }

          // buffer is non-empty
          val newValue = buffer.dequeue()
          println(s"[consumer $id] consumed $newValue")

          /*
            We need to use notifyAll. Otherwise, deadlock scenario:
              Scenario: 2 producers, one consumer, capacity = 1
                producer1 produces a value, then waits
                producer2 sees buffer full, waits
                consumer consumes value, notifies one producer (producer1)
                consumer sees buffer empty, wait
                producer1 produces a value, calls notify - signal goes to producer2
                producer1 sees buffer full, waits
                producer2 sees buffer full, waits
                DEADLOCK
           */
          buffer.notifyAll() // signal all the waiting threads on the buffer
        }

        Thread.sleep(random.nextInt(500))
      }
    }
  }

  class Producer(id: Int, buffer: mutable.Queue[Int], capacity: Int) extends Thread {
    override def run(): Unit = {
      val random = new Random(System.nanoTime())
      var currentCount = 0

      while (true) {
        buffer.synchronized {
          while (buffer.size == capacity) { // buffer full
            println(s"[producer $id] buffer is full, waiting...")
            buffer.wait()
          }

          // there is space in the buffer
          println(s"[producer $id] producing $currentCount")
          buffer.enqueue(currentCount)

          // wake up everyone (similar/symmetrical deadlocking scenario: see producer code)
          buffer.notifyAll()

          // upcoming produced value
          currentCount += 1
        }

        Thread.sleep(random.nextInt(500))
      }
    }
  }

  def start(nProducers: Int, nConsumers: Int, containerCapacity: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val producers = (1 to nProducers).map(id => new Producer(id, buffer, containerCapacity))
    val consumers = (1 to nConsumers).map(id => new Consumer(id, buffer))

    producers.foreach(_.start())
    consumers.foreach(_.start())
  }
}