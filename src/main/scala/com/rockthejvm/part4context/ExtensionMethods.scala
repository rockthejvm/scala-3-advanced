package com.rockthejvm.part4context

object ExtensionMethods {

  case class Person(name: String) {
    def greet: String = s"Hi, my name is $name, nice to meet you!"
  }

  extension (string: String)
    def greetAsPerson: String = Person(string).greet

  val danielGreeting = "Daniel".greetAsPerson

  // generic extension methods
  extension [A](list: List[A])
    def ends: (A, A) = (list.head, list.last)

  val aList = List(1,2,3,4)
  val firstLast = aList.ends

  // reason: make APIs very expressive
  // reason 2: enhance CERTAIN types with new capabilities
  // => super-powerful code
  trait Semigroup[A] {
    def combine(x: A, y: A): A
  }

  extension [A](list: List[A])
    def combineAll(using combinator: Semigroup[A]): A =
      list.reduce(combinator.combine)

  given intCombinator: Semigroup[Int] with
    override def combine(x: Int, y: Int) = x + y

  val firstSum = aList.combineAll // works, sum is 10
  val someStrings = List("I", "love", "Scala")
  // val stringsSum = someStrings.combineAll // does not compile - no given Combinator[String] in scope

  // grouping extensions
  object GroupedExtensions {
    extension [A](list: List[A]) {
      def ends: (A, A) = (list.head, list.last)
      def combineAll(using combinator: Semigroup[A]): A =
        list.reduce(combinator.combine)
    }
  }

  // call extension methods directly
  val firstLast_v2 = ends(aList) // same as aList.ends

  /**
   * Exercises
   * 1. Add an isPrime method to the Int type.
   *    You should be able to write 7.isPrime
   * 2. Add extensions to Tree:
   *    - map(f: A => B): Tree[B]
   *    - forall(predicate: A => Boolean): Boolean
   *    - sum => sum of all elements of the tree
   */

  // 1
  extension (number: Int)
    def isPrime: Boolean = {
      def isPrimeAux(potentialDivisor: Int): Boolean =
        if (potentialDivisor > number / 2) true
        else if (number % potentialDivisor == 0) false
        else isPrimeAux(potentialDivisor + 1)

      assert(number >= 0)
      if (number == 0 || number == 1) false
      else isPrimeAux(2)
    }

  // 2
  // "library code" = cannot change
  sealed abstract class Tree[A]
  case class Leaf[A](value: A) extends Tree[A]
  case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

  extension [A](tree: Tree[A]) {
    def map[B](f: A => B): Tree[B] = tree match {
      case Leaf(value) => Leaf(f(value))
      case Branch(left, right) => Branch(left.map(f), right.map(f))
    }

    def forall(predicate: A => Boolean): Boolean = tree match {
      case Leaf(value) => predicate(value)
      case Branch(left, right) => left.forall(predicate) && right.forall(predicate)
    }

    def combineAll(using combinator: Semigroup[A]): A = tree match {
      case Leaf(value) => value
      case Branch(left, right) => combinator.combine(left.combineAll, right.combineAll)
    }
  }

  extension (tree: Tree[Int]) {
    def sum: Int = tree match {
      case Leaf(value) => value
      case Branch(left, right) => left.sum + right.sum
    }
  }

  def main(args: Array[String]): Unit = {
    val aTree: Tree[Int] = Branch(Branch(Leaf(3), Leaf(1)), Leaf(10))
    println(aTree.map(_ + 1))
    println(aTree.forall(_ % 2 == 0)) // false
    println(aTree.sum) // 14
    println(aTree.combineAll) // 14
  }
}
