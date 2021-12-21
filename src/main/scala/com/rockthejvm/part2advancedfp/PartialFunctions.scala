package com.rockthejvm.part2advancedfp

import com.rockthejvm.part2advancedfp.PartialFunctions.Person

object PartialFunctions {

  val aFunction: Int => Int = x => x + 1

  val aFussyFunction = (x: Int) =>
    if (x == 1) 42
    else if (x == 2) 56
    else if (x ==5) 999
    else throw new RuntimeException("No suitable cases")

  // partial because it only covers 1,2,5 nums
  val aFussyFunction_v2 = (x: Int) => x match {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  }

  // partial functions
  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  }

  val canCallOn37 = aPartialFunction.isDefinedAt(37)
  val liftedPartialFunction = aPartialFunction.lift // lifts function result into a Some or None

  val anotherPF: PartialFunction[Int, Int] = {
    case 45 => 666
  }
  // if cases are not supported by aPartialFunction
  // it runs into anotherPF and checks there!
  val pfChain = aPartialFunction.orElse[Int, Int](anotherPF)

  // HOFs accept PFs as arguments
  val aList = List(1,2,3,4)
  val aChangedList = aList.map(x => x match {
    case 1 => 42
    case 2 => 56
    case 5 => 999
    case _ => 0
  })

  val aChangedList2 = aList.map({ // possible because PF[A, B] extends Function1[A,B]
    case 1 => 42
    case 2 => 56
    case 5 => 999
    case _ => 0
  })

  val aChangedList3 = aList.map { // {} sugar
    case 1 => 42
    case 2 => 56
    case 5 => 999
    case _ => 0
  }

  case class Person(name: String, age: Int)
  val somePeople = List(
    Person("Alice", 3),
    Person("Bob", 10),
    Person("Jeff", 31)
  )
  val peopleGrowUp = somePeople.map(person => Person(person.name, person.age +1))
  // nicer way to deconstruct
  val peopleGrowUp2 = somePeople.map {
    case Person(name, age) => Person(name, age +1)
  }


  def main(args: Array[String]): Unit = {
    println(liftedPartialFunction(2)) // Some(56)
    println(liftedPartialFunction(100)) // None
    println(pfChain(45)) // Some(666)
  }
}
