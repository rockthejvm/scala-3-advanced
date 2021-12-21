package com.rockthejvm.part1advancedscala

import scala.util.Try

object DarkSugars {

  // 1 - sugar for methods with one arg
  def singleArgMethod(arg: Int): Int = arg + 1

  val aMethodCall = singleArgMethod({
    // code block
    42
  })

  val aMethodCall2 = singleArgMethod {
    // code block
    42
  }

  // example - try, future
  val aTryInstance = Try({
    throw new RuntimeException
  })

  val aTryInstance2 = Try {
    throw new RuntimeException
  }

  // with hofs
  val anIncrementedList = List(1, 2, 3).map { x =>
    // code block
    x + 1
  }

  // 2 - single abstract method pattern (since scala 2.12)
  // you can take advantage of this pattern if there are implemented methods
  // the key is that there is ONE abstract method
  trait Action {
    def act(x: Int): Int
  }

  // v combersome boiler plate
  val aNewAction = new Action {
    override def act(x: Int): Int = x + 1
  }

  // use a lambda instead
  val anotherAction: Action = (x: Int) => x + 1

  // examples - Runnables
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("Hello from another thread")
  })

  val aSweeterThread = new Thread(() => println("Hi scala"))

  // 3 - methods ending in a : are RIGHT associatve
  // which is why it looks like a method on int not list in aPrependedList
  val aList = List(1, 2, 3)
  val aPrependedList = 0 :: aList
  val aThing = aList.::(0) // .:: method lives on List

  class MyStream[T] {
    infix def -->:(value: T): MyStream[T] = this // impl not important
  }

  val myStream = 1 -->: 2 -->: 3 -->: 4 -->: new MyStream[Int]

  // 4 - multi word identifiers
  class Talker(name: String) {
    infix def `and then said`(gossip: String) = println(s"$name said $gossip")
  }

  val daniel = new Talker("Daniel")
  val dansStatement = daniel `and then said` "I love scala"

  // example - HTTP libraies inc AKKA
  object `Content-Type` {
    val `appplication/json` = "application/JSON"
  }

  // 5 - infix types
  import scala.annotation.targetName
  // this means the --> compiles to "Arrow" in java byte code
  // so you can refer to it as Arrow from Java
  @targetName("Arrow")
  infix class -->[A, B]

  val compositeType: -->[Int, String] = new -->[Int, String]
  val compositeType2: Int --> String = new -->[Int, String]

  // 6 - update()
  val anArray = Array(1, 2, 3, 4)
  anArray.update(2, 45) // 1,2,45,4
  anArray(2) = 45 // same thing

  // 7 - mutable fields
  class Mutable {
    private var internalMemeber: Int = 0

    def member = internalMemeber // getter

    def member_=(value: Int): Unit =
      internalMemeber = value // setter
  }

  val aMutableContainer = new Mutable
  aMutableContainer.member = 42 // aMutableContainer.member_=(42)

  // 8 - variable arguments (varargs)
  def methodWithVarargs(args: Int*) = {
    // returm the num of args provided
    args.length
  }

  val callWithOneArgs = methodWithVarargs(78)
  val callWithTwoArgs = methodWithVarargs(78, 111)
  val callWithThreeArgs = methodWithVarargs(78, 111, 56)

  val aCollection = List(1, 2, 3, 4)
  val callWithDynamicArgs = methodWithVarargs(aCollection *)


  def main(args: Array[String]): Unit = {

  }

}
