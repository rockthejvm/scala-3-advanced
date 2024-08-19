package com.rockthejvm.practice

import scala.annotation.tailrec

abstract class FSet[A] extends (A => Boolean) {
  // main api
  def contains(elem: A): Boolean
  def apply(elem: A): Boolean = contains(elem)

  infix def +(elem: A): FSet[A]
  infix def ++(anotherSet: FSet[A]): FSet[A]

  // "classics"
  def map[B](f: A => B): FSet[B]
  def flatMap[B](f: A => FSet[B]): FSet[B]
  def filter(predicate: A => Boolean): FSet[A]
  def foreach(f: A => Unit): Unit

  // utilities
  infix def -(elem: A): FSet[A]
  infix def --(anotherSet: FSet[A]): FSet[A]
  infix def &(anotherSet: FSet[A]): FSet[A]

  // "negation" == all the elements of type A EXCEPT the elements in this set
  def unary_! : FSet[A] = new PBSet(!contains(_))
}

// example { x in N | x % 2 == 0 }
// property-based set
class PBSet[A](property: A => Boolean) extends FSet[A] {
  // main api
  def contains(elem: A): Boolean = property(elem)

  infix def +(elem: A): FSet[A] =
    new PBSet(x => x == elem || property(x))
  infix def ++(anotherSet: FSet[A]): FSet[A] =
    new PBSet(x => property(x) || anotherSet(x))

  // "classics"
  def map[B](f: A => B): FSet[B] =
    politelyFail()
  def flatMap[B](f: A => FSet[B]): FSet[B] =
    politelyFail()
  def filter(predicate: A => Boolean): FSet[A] =
    new PBSet(x => property(x) && predicate(x))
  def foreach(f: A => Unit): Unit =
    politelyFail()

  // utilities
  infix def -(elem: A): FSet[A] =
    filter(x => x != elem)
  infix def --(anotherSet: FSet[A]): FSet[A] =
    filter(!anotherSet)
  infix def &(anotherSet: FSet[A]): FSet[A] =
    filter(anotherSet)

  // extra utilities (internal)
  private def politelyFail() = throw new RuntimeException("I don't know if this set is iterable...")
}

case class Empty[A]() extends FSet[A] { // PBSet(x => false)
  override def contains(elem: A) = false
  infix def +(elem: A): FSet[A] = Cons(elem, this)
  infix def ++(anotherSet: FSet[A]): FSet[A] = anotherSet

  // "classics"
  def map[B](f: A => B): FSet[B] = Empty()
  def flatMap[B](f: A => FSet[B]): FSet[B] = Empty()
  def filter(predicate: A => Boolean): FSet[A] = this
  def foreach(f: A => Unit): Unit = ()

  // utilities
  infix def -(elem: A): FSet[A] = this
  infix def --(anotherSet: FSet[A]): FSet[A] = this
  infix def &(anotherSet: FSet[A]): FSet[A] = this

}

case class Cons[A](head: A, tail: FSet[A]) extends FSet[A] {
  override def contains(elem: A) = elem == head || tail.contains(elem)
  infix def +(elem: A): FSet[A] =
    if (contains(elem)) this
    else Cons(elem, this)

  infix def ++(anotherSet: FSet[A]): FSet[A] = tail ++ anotherSet + head

  // "classics"
  def map[B](f: A => B): FSet[B] = tail.map(f) + f(head)
  def flatMap[B](f: A => FSet[B]): FSet[B] = tail.flatMap(f) ++ f(head)
  def filter(predicate: A => Boolean): FSet[A] = {
    val filteredTail = tail.filter(predicate)
    if predicate(head) then filteredTail + head
    else filteredTail
  }

  def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }

  // utilities
  infix def -(elem: A): FSet[A] =
    if (head == elem) tail
    else tail - elem + head

  infix def --(anotherSet: FSet[A]): FSet[A] = filter(!anotherSet)
  infix def &(anotherSet: FSet[A]): FSet[A] = filter(anotherSet) // intersection = filtering
}

object FSet {
  def apply[A](values: A*): FSet[A] = {
    @tailrec
    def buildSet(valuesSeq: Seq[A], acc: FSet[A]): FSet[A] =
      if (valuesSeq.isEmpty) acc
      else buildSet(valuesSeq.tail, acc + valuesSeq.head)

    buildSet(values, Empty())
  }
}

object FunctionalSetPlayground {

  def main(args: Array[String]): Unit = {

    val first5 = FSet(1,2,3,4,5)
    val someNumbers = FSet(4,5,6,7,8)
    println(first5.contains(5)) // true
    println(first5(6))          // false
    println((first5 + 10).contains(10)) // true
    println(first5.map(_ * 2).contains(10)) // true
    println(first5.map(_ % 2).contains(1))  // true
    println(first5.flatMap(x => FSet(x, x + 1)).contains(7)) // false

    println((first5 - 3).contains(3)) // false
    println((first5 -- someNumbers).contains(4)) // false
    println((first5 & someNumbers).contains(4)) // true

    val naturals = new PBSet[Int](_ => true)
    println(naturals.contains(5237548)) // true
    println(!naturals.contains(0)) // false
    println((!naturals + 1 + 2 + 3).contains(3)) // true
    // println(!naturals.map(_ + 1)) // throws - map/flatMap/foreach will not work
  }
}
