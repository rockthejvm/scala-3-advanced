package com.rockthejvm.practice

import scala.annotation.tailrec

// Write a lazily evaluated, potentially INFINITE linked list

abstract class LzList[A] {
  def isEmpty: Boolean
  def head: A
  def tail: LzList[A]

  // utils
  def #::(element: A): LzList[A] // prepending
  def ++(another: LzList[A]): LzList[A] // TODO warning

  // classics
  def forEach(f: A => Unit): Unit
  def map[B](f: A => B): LzList[B]
  def flatMap[B](f: A => LzList[B]): LzList[B]
  def filter(predicate: A => Boolean): LzList[A]
  def withFilter(predicate: A => Boolean): LzList[A] = filter(predicate)

  def take(n: Int): LzList[A] // first n elems from list
  def takeAsList(n: Int): List[A] =
    take(n).toList

  def toList: List[A] = {
    @tailrec
    def toListAux(remaining: LzList[A], acc: List[A]): List[A] =
      if (remaining.isEmpty) acc.reverse
      else toListAux(remaining.tail, remaining.head :: acc)

    toListAux(this, List())
  }
}

case class LzEmpty[A]() extends LzList[A] {
  override def isEmpty: Boolean = true
  override def head: A = throw new NoSuchElementException("Empty head")
  override def tail: LzList[A] = throw new NoSuchElementException("Empty tail")

  override def #::(element: A): LzList[A] = new LzCons(element, this)
  override def ++(another: LzList[A]): LzList[A] = another

  override def forEach(f: A => Unit): Unit = ()
  override def map[B](f: A => B): LzList[B] = LzEmpty()
  override def flatMap[B](f: A => LzList[B]): LzList[B] = LzEmpty()
  override def filter(predicate: A => Boolean): LzList[A] = this

  override def take(n: Int): LzList[A] =
    if (n == 0) this
    else throw new RuntimeException("Cannot take from an empty list")
}

class LzCons[A](hd: => A, tl: => LzList[A]) extends LzList[A] {
  override def isEmpty: Boolean = false
  override lazy val head: A = hd
  override lazy val tail: LzList[A] = tl

  override def #::(element: A): LzList[A] = new LzCons(element, this)
  override def ++(another: LzList[A]): LzList[A] = new LzCons(head, tail ++ another)

  override def forEach(f: A => Unit): Unit = {
    f(head)
    tail.forEach(f)
  }
  override def map[B](f: A => B): LzList[B] = new LzCons(f(head), tail.map(f))
  override def flatMap[B](f: A => LzList[B]): LzList[B] = f(head) ++ tail.flatMap(f)
  override def filter(predicate: A => Boolean): LzList[A] =
    if (predicate(head)) new LzCons(head, tail.filter(predicate)) // preserve lazy eval
    else tail.filter((predicate))

  override def take(n: Int): LzList[A] =
    if (n <= 0) LzEmpty()
    else if (n == 1) new LzCons(head, LzEmpty())
    else new LzCons(head, tail.take(n-1))
}

object LzList {
  def empty[A]: LzList[A] = LzEmpty()

  def generate[A](start: A)(generator: A => A): LzList[A] =
    new LzCons(start, LzList.generate(generator(start))(generator))

  def from[A](list: List[A]): LzList[A] = list.foldLeft(LzList.empty) {
    (currentLzList, newElement) =>
      new LzCons(newElement, currentLzList)
  }
}


object LzListPlayground {

  def main(args: Array[String]): Unit = {
    val naturals = LzList.generate(1)(n => n + 1) // infinite natural nums
  }

}
