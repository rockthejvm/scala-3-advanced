package com.rockthejvm.part2advancedfp

import com.rockthejvm.part1advancedscala.Recap.x

object CurryingPAFs {

  // currying
  val superAdder: Int => Int => Int =
    x => y => x + y

  val add3: Int => Int = superAdder(3) // y => 3 + y
  val eight = add3(5) // 8
  val eight_v2 = superAdder(3)(5)

  // curried methods
  def curriedAdder(x: Int)(y: Int): Int =
    x + y

  // methods != function values

  // converting methods to functions
  val add4 = curriedAdder(4) // eta-expansion => process through which compiler can turn a method into a function value
  val nine = add4(5) // 9

  def increment(x: Int): Int = x + 1 // <== this gets converted to function value below
  val aList = List(1,2,3)
  val anIncrementedList = aList.map(increment) // another example of eta-expansion

  // underscores ate powerful
  def concatenator(a: String, b: String, c: String): String = a + b + c

  val aListOfWords = List("Hello", "Scala", "Is", "Fun")

  val insertName = concatenator("Hello, my name is", _: String, " I'm going to show you a nice Scala trick")
  // x => concatenator("...", x, "...")

  val danielsGreeting = insertName("Daniel") // conca...("...", Daniel, "...")


  // exercises
  val simpleAddFunction = (x: Int, y: Int) => x + y
  def simpleAddMethod(x: Int, y: Int) = x + y
  def curriedAddMethod(x: Int)(y: Int) = x + y

  // obtain add7 - x => x + 7
  def add7 = (x: Int) => simpleAddMethod(x, 7)
  def add7_1 = (x: Int) => simpleAddMethod(x, 7)
  def add7_2 = (x: Int) => curriedAddMethod(7)(x)
  val add7_4 = curriedAddMethod(7)
  val add7_5 = simpleAddMethod(7, _)


  def main(args: Array[String]): Unit = {

  }

}
