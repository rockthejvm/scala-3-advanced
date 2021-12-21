package com.rockthejvm.part2advancedfp

object FunctionalCollections {


  // sets are functions A => Boolean
  val aSet: Set[String] = Set("I", "love", "Scala")
  val setContainsScala = aSet.contains("Scala")
  val setContainsScalas = aSet("Scala") // <- works because sets are a function

  // seq extend PartialFunction[Int, A]
  val aSeq: Seq[Int] = Seq(1,2,3,4)
  val anElement = aSeq(2) // 3
  //  val aNonExistingElement = aSeq(100) // throw an out of bounds exception

  // Map[K,V] extends partialFunction[K,V]
  val aPhonebook: Map[String, Int] = Map(
    "Alice" -> 12345,
    "Bob" -> 6765432
  )

  val alicesPhone = aPhonebook("Alice")

  def main(args: Array[String]): Unit = {

  }

}
